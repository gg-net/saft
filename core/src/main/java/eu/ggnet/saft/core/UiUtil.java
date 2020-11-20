/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.*;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;

import eu.ggnet.saft.core.ui.SwingSaft;
import eu.ggnet.saft.core.ui.Title;
import eu.ggnet.saft.core.ui.builder.BuilderUtil;

/**
 * Ui Utils.
 *
 * @author oliver.guenther
 */
public class UiUtil {

    /**
     * A Runable implementation, that allows throwing an Exception.
     *
     * @author oliver.guenther
     */
    public static interface ExceptionRunnable {

        /**
         * The run method.
         *
         * @throws Exception possible exception while running
         */
        void run() throws Exception;

    }

    private UiUtil() {
    }

    /**
     * Dispatches the Callable to the Platform Ui Thread. If this method is called on the javafx ui thread, the supplied callable is called,
     * otherwise the exection on Platform.runLater ist synchrnized via a latch.
     *
     * @param <T>      Return type of callable
     * @param callable the callable to dispatch
     * @return the result of the callable
     * @throws RuntimeException wraps InterruptedException of {@link CountDownLatch#await() } and ExecutionException of {@link FutureTask#get() }
     */
    public static <T> T dispatchFx(Callable<T> callable) throws RuntimeException {
        try {
            FutureTask<T> futureTask = new FutureTask<>(callable);
            final CountDownLatch cdl = new CountDownLatch(1);
            if ( Platform.isFxApplicationThread() ) {
                futureTask.run();
                cdl.countDown();
            } else {
                Platform.runLater(() -> {
                    futureTask.run();
                    cdl.countDown();
                });
            }
            cdl.await();
            return futureTask.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Calls the supplied callable and possible wraps a thrown exception in a {@link CompletionException}.
     *
     * @param <T>      the type
     * @param callable the callable
     * @return the result of the callable
     * @throws CompletionException the exception thrown by the callable wrapped.
     */
    public static <T> T exceptionRun(Callable<T> callable) throws CompletionException {
        try {
            return callable.call();
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    /**
     * Runs the supplied runable and possible wraps a thrown exception in a {@link CompletionException}.
     *
     * @param er the callable
     * @throws CompletionException the exception thrown by the callable wrapped.
     */
    public static void exceptionRun(ExceptionRunnable er) throws CompletionException {
        exceptionRun(() -> {
            er.run();
            return null;
        });
    }

    // TODO: Not nice, recosider name.
    public static <T extends Component> JFrame startSwing(final Callable<T> builder) throws RuntimeException {
        try {
            return SwingSaft.dispatch(() -> {
                T p = builder.call();
                JFrame frame = new JFrame();
                Optional<StringProperty> optionalTitleProperty = BuilderUtil.findTitleProperty(p);
                if ( optionalTitleProperty.isPresent() ) {
                    optionalTitleProperty.get().addListener((ob, o, n) -> {
                        frame.setTitle(n);
                        frame.setName(n);
                    });
                } else if ( p.getClass().getAnnotation(Title.class) != null ) {
                    frame.setTitle(p.getClass().getAnnotation(Title.class).value());
                    frame.setName(p.getClass().getAnnotation(Title.class).value());
                }
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(p);
                frame.pack();
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
                return frame;
            });
        } catch (InterruptedException | InvocationTargetException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

}
