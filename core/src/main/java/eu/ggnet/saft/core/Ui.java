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

import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.FileChooserBuilder;
import eu.ggnet.saft.core.ui.builder.PreBuilder;

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
    // LATER
    public static FileChooserBuilder fileChooser() {
        return new FileChooserBuilder();
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
     * @deprecated {@link UiUtil}
     */
    @Deprecated
    public static ExceptionWrapper exception() {
        return ExceptionWrapper.getInstance();
    }

    /**
     * See {@link Saft#closeWindowOf(java.awt.Component) }.
     *
     * @param c the component which is the closest to the window.
     */
    public static void closeWindowOf(Component c) {
        UiCore.global().closeWindowOf(c);
    }

    /**
     * See {@link Saft#closeWindowOf(javafx.scene.Node) }.
     *
     * @param n the node as refernece.
     */
    public static void closeWindowOf(Node n) {
        UiCore.global().closeWindowOf(n);
    }

    /**
     * Wrapper for Desktop.getDesktop().open() with UI Exception handling
     *
     * @param file a file to open via ui.
     * @return true if operation was successful, otherwise false. Can be used if the following operations should happen.
     */
    // LATER
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
     * See {@link Saft#handle(java.lang.Throwable) }.
     *
     * @param b the throwable to be handled.
     */
    public static void handle(Throwable b) {
        UiCore.global().handle(b);
    }

    /**
     * See {@link Saft#handler() }.
     *
     * @param <Z> type parameter
     * @return a handler.
     */
    public static <Z> BiFunction<Z, Throwable, Z> handler() {
        return UiCore.global().handler();
    }
}
