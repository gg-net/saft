/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core;

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.impl.AbstractCore;
import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.Title;

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
     * Constructs (loads) an FXML and controller pair, finding all elements base on the class and calling load, so direct calls to getRoot() or getController()
     * are possible. Might be run on the Platfrom thread depending on the used widgets (e.g. If webview is used, it must be run on the ui thread.)
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
    // HINT: internal use
    public static <T, R extends FxController> FXMLLoader constructFxml(Class<R> controllerClazz) throws IllegalArgumentException, NullPointerException, IllegalStateException, RuntimeException {
        if ( !Platform.isFxApplicationThread() )
            throw new IllegalStateException("Method constructFxml is not called from the JavaFx Ui Thread, illegal (e.g. construct of WebView fails on other threads)");
        FXMLLoader loader = new FXMLLoader(AbstractCore.loadView(controllerClazz));
        try {
            loader.load();
            return loader;
        } catch (IOException ex) {
            throw new RuntimeException("Exception during constructFxml", ex);
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

    /**
     * Startup helper to do some typical work.
     *
     * @param <T>
     * @param builder
     * @return
     * @throws RuntimeException
     */
    public static <T extends Component> JFrame startup(final Supplier<T> builder) throws RuntimeException {
        T p = builder.get();
        JFrame frame = new JFrame();
        Optional<StringProperty> optionalTitleProperty = AbstractCore.findTitleProperty(p);
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
    }

    /**
     * Statup helper to do some typical work on the primary stage.
     *
     * @param <T>
     * @param primaryStage
     * @param builder
     * @return
     */
    public static <T extends Parent> Stage startup(final Stage primaryStage, final Supplier<T> builder) {
        Parent p = builder.get();
        Optional<StringProperty> optionalTitleProperty = AbstractCore.findTitleProperty(p);
        if ( optionalTitleProperty.isPresent() ) {
            primaryStage.titleProperty().bind(optionalTitleProperty.get());
        } else if ( p.getClass().getAnnotation(Title.class) != null ) {
            primaryStage.setTitle(p.getClass().getAnnotation(Title.class).value());
        }
        primaryStage.setScene(new Scene(p));
        primaryStage.centerOnScreen();
        primaryStage.sizeToScene();
        primaryStage.show();
        return primaryStage;
    }

    // public final static Map<String, WeakReference<Stage>> ACTIVE_STAGES = new ConcurrentHashMap<>();
    private static Supplier<List<javafx.stage.Window>> GetWindowsSupplier = null; // Will be set via getWindows.

    /**
     * Reflexive Method to get all open windows in any JDK from 8 upwards.
     * In JDK8 the only way to get all open Windows/Stages was via the unoffical API com.sun.javafx.stage.StageHelper.getStages()
     * Form JDK9 upwards there is the offical API Window.getWindows().
     * Both methos are implemented here via reflections.
     *
     * @return a List containing all open Windows.
     */
    // Hint: internal use
    // TODO: It is a util method. maybe move to UiUtil
    public static List<javafx.stage.Window> findAllOpenFxWindows() {
        if ( GetWindowsSupplier == null ) {
            try {
                GetWindowsSupplier = new Supplier<List<javafx.stage.Window>>() {

                    private final Method methodGetStages = Class.forName("com.sun.javafx.stage.StageHelper").getMethod("getStages");

                    @Override
                    public List<javafx.stage.Window> get() {
                        try {
                            return new ArrayList<>((List<javafx.stage.Window>)methodGetStages.invoke(null));
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            throw new RuntimeException("getWindows(): com.sun.jacafx.stage.StageHelper.getStages was found, but faild. Should never happen", ex);
                        }
                    }
                };
                LoggerFactory.getLogger(UiUtil.class).info("getWindows() ontime initial. Class StageHelper found, assuming JDK8");
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                try {
                    GetWindowsSupplier = new Supplier<List<javafx.stage.Window>>() {

                        private final Method methodGetStages = javafx.stage.Window.class.getMethod("getWindows");

                        @Override
                        public List<javafx.stage.Window> get() {
                            try {
                                return new ArrayList<>((List<javafx.stage.Window>)methodGetStages.invoke(null));
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                throw new RuntimeException("getWindows(): com.sun.jacafx.stage.StageHelper.getStages was found, but faild. Should never happen", ex);
                            }
                        }
                    };
                    LoggerFactory.getLogger(UiUtil.class).info("getWindows() ontime initial. Class StageHelper not found, so this must be JDK9 or newer");
                } catch (NoSuchMethodException | SecurityException ex1) {
                    throw new RuntimeException("getWindows(): neither StageHelper.getStages nor Window.getWindows was found. Something weird happend, read the source", ex1);
                }

            }
        };
        return GetWindowsSupplier.get();
    }

}
