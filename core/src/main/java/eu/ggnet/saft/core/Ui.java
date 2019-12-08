package eu.ggnet.saft.core;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.api.Reply;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.*;

/*
 Notes of olli:
- alles was mit ui zuständen des frameworks zu tun hat, startet hier. zb. mainFrame, progress, failure messager, excetion output.
 */
/**
 * The main entry point.
 * Some rules which I invented on the way:
 * <ul>
 * <li>Result of null is indicator to break the chain</li>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 *
 * @author oliver.guenther
 */
public class Ui {

    private final static Logger L = LoggerFactory.getLogger(Ui.class);

    /**
     * Returns a new Ui builder.
     *
     * @return a new Ui builder.
     */
    public static PreBuilder build() {
        return new PreBuilder();
    }

    /**
     * Returns a new Ui builder.
     *
     * @param swingParent optional swing parrent
     * @return a new Ui builder.
     */
    public static PreBuilder build(Component swingParent) {
        return new PreBuilder().parent(swingParent);
    }

    /**
     * Returns a new Ui builder.
     *
     * @param javaFxParent optional javafx parrent
     * @return a new Ui builder.
     */
    public static PreBuilder build(Parent javaFxParent) {
        return new PreBuilder().parent(javaFxParent);
    }

    /**
     * Shortcut to a file chooser.
     *
     * @return a file chooser builder.
     */
    public static FileChooserBuilder fileChooser() {
        return new FileChooserBuilder();
    }

    /**
     * Returns a new failure Handler, usefull in an Optional {@link Reply} chain.
     *
     * @return a new failure Handler.
     */
    // TODO: Consider removal or move to experimental.
    public static Failure failure() {
        return new Failure();
    }

    /**
     * Returns a new progress Handler.
     *
     * @return a new progress Handler.
     */
    public static ProgressBuilder progress() {
        return new ProgressBuilder();
    }

    /**
     * Wrapper around {@link ForkJoinPool#commonPool() } with Ui Exception handling.
     *
     * @param <V>      type parameter
     * @param callable a callable for the background.
     */
    public static <V> void exec(Callable<V> callable) {
        UiCore.EXECUTOR_SERVICE.execute(() -> {
            try {
                callable.call();
            } catch (Exception e) {
                UiCore.handle(e);
            }
        });
    }

    public static void exec(Runnable runnable) {
        UiCore.EXECUTOR_SERVICE.execute(() -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                UiCore.handle(e);
            }
        });
    }

    /**
     * Constructs (loads) an FXML and controller pair, finding all elements base on the class and calling load, so direct calls to getRoot() or getController()
     * are possible.
     * Resources are discovered as described in {@link FxSaft#loadView(java.lang.Class) }.
     *
     * @param <T>             type parameter
     * @param <R>             type parameter
     * @param controllerClazz the controller class.
     * @return a loaded loader.
     * @throws IllegalArgumentException see {@link FxSaft#loadView(java.lang.Class) }
     * @throws IllegalStateException    see {@link FxSaft#loadView(java.lang.Class) }
     * @throws NullPointerException     see {@link FxSaft#loadView(java.lang.Class) }
     * @throws RuntimeException         wrapped IOException of {@link FXMLLoader#load() }.
     */
    public static <T, R extends FxController> FXMLLoader construct(Class<R> controllerClazz) throws IllegalArgumentException, NullPointerException, IllegalStateException, RuntimeException {
        return FxSaft.dispatch(() -> FxSaft.constructFxml(controllerClazz));
    }

    /**
     * Allows the wrapping of Exeptions into {@link java.util.concurrent.CompletionException}.
     *
     * @return the ExceptionWrapper.
     */
    public static ExceptionWrapper exception() {
        return ExceptionWrapper.getInstance();
    }

    /**
     * Allows the closing of a window from within a Pane or Panel
     * <pre>
     * {@code
     * JFrame f = new JFrame();
     * JPanel p = new JPanel();
     * JButton b = new Button("Close");
     * p.add(b);
     * f.getContentPane().get(p);
     * b.addActionListener(() -> Ui.cloesWindowOf(p);
     * f.setVisible(true);
     * }
     * </pre>.
     *
     * @param c the component which is the closest to the window.
     */
    public static void closeWindowOf(Component c) {
        if ( UiCore.isGluon() ) throw new IllegalStateException("closeWindowOf call with a swing component, not allowed in gluon model");
        UiParent.of(c).ifPresent(
                p -> SwingSaft.run(() -> {
                    p.setVisible(false);
                    p.dispose();
                }),
                fx -> FxSaft.run(() -> fx.close()));
    }

    /**
     * Closes the wrapping Window (or equivalent) of the supplied node.
     *
     * @param n the node as refernece.
     */
    public static void closeWindowOf(Node n) {
        if ( UiCore.isGluon() ) {
            L.debug("closeWindowOf({}) gluon mode", n);
            Dl.local().lookup(GluonSupport.class).closeViewOrDialogOf(n);
        } else {
            L.debug("closeWindowOf({}) desktop mode", n);
            UiParent.of(n).ifPresent(
                    p -> SwingSaft.run(() -> {
                        p.setVisible(false);
                        p.dispose();
                    }),
                    fx -> FxSaft.run(() -> fx.close()));
        }
    }

    /**
     * Wrapper for Desktop.getDesktop().open() with UI Exception handling
     *
     * @param file a file to open via ui.
     * @return true if operation was successful, otherwise false. Can be used if the following operations should happen.
     */
    // TODO: implement for all Modes. Or remove and put back to the developer.
    public static boolean osOpen(File file) {
        if ( UiCore.isGluon() ) throw new IllegalStateException("Not yet implemented in gluon");
        try {
            Desktop.getDesktop().open(file);
            return true;
        } catch (IOException e) {
            UiCore.handle(e);
        }
        return false;
    }

    /**
     * Handles an Exception in the Ui, using the registered ExceptionCosumers form
     * {@link UiCore#registerExceptionConsumer(java.lang.Class, java.util.function.Consumer)}.
     *
     * @param b the throwable to be handled.
     */
    public static void handle(Throwable b) {
        UiCore.handle(b);
    }

    /**
     * Retruns a handler, to be used in a CompletableFuture.handle().
     *
     * @param <Z> type parameter
     * @return a handler.
     */
    public static <Z> BiFunction<Z, Throwable, Z> handler() {
        return (Z t, Throwable u) -> {
            if ( u != null ) Ui.handle(u);
            return null;
        };
    }
}
