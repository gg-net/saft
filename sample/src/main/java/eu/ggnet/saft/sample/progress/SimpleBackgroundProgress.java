/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.progress;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Worker;

/**
 * A simple way to handle some background progress.
 *
 * @author oliver.guenther
 */
public class SimpleBackgroundProgress {

    private final BooleanProperty BACKGROUND_ACTIVITY = new SimpleBooleanProperty(false);

    private static SimpleBackgroundProgress instance;

    /**
     * Initialize for global usage.
     *
     * @param i the saft.
     */
    public static void initGlobal(SimpleBackgroundProgress i) {
        if ( instance != null )
            throw new IllegalStateException(SimpleBackgroundProgress.class.getSimpleName() + " already inited, call to initGlobal() not allowed.");
        instance = Objects.requireNonNull(i, SimpleBackgroundProgress.class.getSimpleName() + " must not be null");
    }

    /**
     * Returns the one instance, if global usage is used.
     *
     * @return the global saft.
     */
    public static SimpleBackgroundProgress globalProgress() {
        if ( instance == null ) {
            throw new IllegalStateException("Not yet initialized");
        }
        return instance;
    }

    public BooleanProperty activityProperty() {
        return BACKGROUND_ACTIVITY;
    }

    public void activity(boolean status) {
        BACKGROUND_ACTIVITY.set(status);
    }

    public boolean activity() {
        return BACKGROUND_ACTIVITY.get();
    }

    /**
     * Wrapes a function with progress connectivity. Enables the progress information in the main ui while the supplied function is run.
     * Starts a progress display then the returned function is called and stops it, then its complete.
     * Uses the class name of the supplied function as monitor title.
     *
     * @param <U>      type parameter
     * @param <T>      type parameter
     * @param function the function to be wrapped into progress information.
     * @return a enhanced function
     */
    public <U, T> Function<T, U> wrap(Function<T, U> function) {
        // TODO: Progresshandling sucks, but its only internal, so we can live with it for now.
        return (T t) -> {
            BACKGROUND_ACTIVITY.set(true);
            try {
                return function.apply(t);
            } finally {
                BACKGROUND_ACTIVITY.set(false);
            }
        };
    }

    /**
     * Wrapes a runnalbe with progress connectivity. Enables the progress information in the main ui while the supplied function is run.
     * Starts a progress display then the returned function is called and stops it, then its complete.
     * Uses the class name of the supplied function as monitor title.
     *
     * @param runnable the runnable
     * @return a enhanced runnable
     */
    public Runnable wrap(Runnable runnable) {
        return () -> {
            BACKGROUND_ACTIVITY.set(true);
            try {
                runnable.run();
            } finally {
                BACKGROUND_ACTIVITY.set(false);
            }
        };
    }

    /**
     * Wrapes a callable with progress connectivity. Enables the progress information in the main ui while the supplied function is run.
     * Starts a progress display then the returned function is called and stops it, then its complete.
     * Uses the class name of the supplied function as monitor title.
     *
     * @param <V>      type parameter of the callable
     * @param callable the callable
     * @return a enhanced callable
     */
    public <V> Callable<V> wrap(Callable<V> callable) {
        return () -> {
            BACKGROUND_ACTIVITY.set(true);
            try {
                return callable.call();
            } finally {
                BACKGROUND_ACTIVITY.set(false);
            }
        };
    }

    /**
     * Calls the callable with progress.
     *
     * @param <V>      type of the callable parameter
     * @param callable the callable
     * @return the result of the callable.
     * @throws RuntimeException wrapped exception of the .call() method.
     */
    public <V> V call(Callable<V> callable) throws RuntimeException {
        try {
            return wrap(callable).call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Observers the progress on any javaFx worker.
     * If there is some form of central managed and displayed progress and status message system registered with saft, this can be used to show a worker
     * progress.
     *
     * @param <T>    type parameter
     * @param worker the worker to be observed.
     * @return the parameter worker, for fluent usage.
     */
    public <T extends Worker> T observe(T worker) {
        if ( worker != null ) worker.runningProperty().addListener((ob, o, n) -> BACKGROUND_ACTIVITY.setValue(n));
        return worker;
    }
}
