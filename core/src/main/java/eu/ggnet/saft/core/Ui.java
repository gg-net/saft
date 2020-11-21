package eu.ggnet.saft.core;

import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;

import javafx.scene.Node;
import javafx.scene.Parent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.ExceptionWrapper;
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
     * Static global version of {@link Saft#build() } via {@link UiCore#global() }
     *
     * @see Saft for direct usage.
     * @return a new Ui builder.
     */
    public static PreBuilder build() {
        return UiCore.global().build();
    }

    /**
     * Static global version of {@link Saft#build() } via {@link UiCore#global() }
     *
     * @param swingParent optional swing parrent
     * @return a new Ui builder.
     */
    public static PreBuilder build(Component swingParent) {
        return UiCore.global().build(swingParent);
    }

    /**
     * Static global version of {@link Saft#build() } via {@link UiCore#global() }
     *
     * @param javaFxParent optional javafx parrent
     * @return a new Ui builder.
     */
    public static PreBuilder build(Parent javaFxParent) {
        return UiCore.global().build(javaFxParent);
    }

    /**
     * Wrapper around {@link ForkJoinPool#commonPool() } with Ui Exception handling.
     *
     * @param <V>      type parameter
     * @param callable a callable for the background.
     */
    public static <V> void exec(Callable<V> callable) {
        UiCore.global().executorService().execute(() -> {
            try {
                callable.call();
            } catch (Exception e) {
                UiCore.global().handle(e);
            }
        });
    }

    public static void exec(Runnable runnable) {
        UiCore.global().executorService().execute(() -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                UiCore.global().handle(e);
            }
        });
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
