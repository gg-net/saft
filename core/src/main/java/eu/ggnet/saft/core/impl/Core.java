/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import eu.ggnet.saft.core.ui.UiParent;
import eu.ggnet.saft.core.ui.builder.PreBuilder;
import eu.ggnet.saft.core.ui.builder.Result;

/**
 *
 * T type of the ui window class (swing = JFrame, javafx = Stage).
 * All methods have an optional characteristic, meaning, they never fail,
 *
 * @author oliver.guenther
 */
public interface Core<T> {

    //The R and S construct is the only way to ensure, that the Suppliers result extends the class token.
    public static class In<R, S extends R> {

        private final Supplier<S> supplier;

        private final Class<R> clazz;

        /**
         * TODO:
         * Dokument me.
         * Jeder Core sollte (muss) mindestens folgende classes und unterclases supporten
         * - javafx Pane
         * - swing JPanel
         * - saft FxController
         * - javafx Dialog
         *
         * @param clazz    The type of implementation.
         * @param supplier
         */
        public In(Class<R> clazz, Supplier<S> supplier) {
            this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
            this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
        }

        public In(Class<R> clazz) {
            this.supplier = null;
            this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
        }

        public Class<R> clazz() {
            return clazz;
        }

        public Optional<Supplier<S>> supplier() {
            return Optional.ofNullable(supplier);
        }

        @Override
        public String toString() {
            return "In{" + "supplier=" + supplier + ", clazz=" + clazz + '}';
        }

    }

    /**
     * Suppling unwrapped parent or main to the consumer.
     * if parent not null and the relevant window exists (has been created vie saft) -> this to the consumer
     * else if main parent is set -> this to the consumer
     * else don't call the consumer
     *
     * @param parent
     * @param consumer
     */
    void parentIfPresent(UiParent parent, Consumer<T> consumer);

    void parentIfPresent(Optional<UiParent> parent, Consumer<T> consumer);

    void parentIfPresent(Consumer<T> consumer);

    Optional<T> unwrap(UiParent parent);

    Optional<T> unwrap(Optional<UiParent> parent);

    Optional<T> unwrapMain();

    /**
     * Closes the container/window of the supplied anchor.
     *
     * @param parent the parent, must not be null.
     */
    void closeOf(UiParent parent);

    /**
     * Relocate all active windows on the visible screen.
     * Usefull to rescue offscreen windows.
     */
    void relocate();

    /**
     * Weak register of windows.
     * Remove is not needed, as it will be weak referenced.
     *
     * @param window a window, which the core should know about and close at the end.
     */
    void add(T window);

    /**
     * Shutdown the core, closing open windows.
     */
    void shutdown();

    /**
     * Returns true if the core of this type is active.
     *
     * @return
     */
    boolean isActiv();

    /**
     * Register a ui element to be show only once via a supplied key.
     *
     * @param key the key identifing the once element, must not be null
     * @param in  the in of class token and optional supplier, must not be null
     * @throws NullPointerException     if key or in are null
     * @throws IllegalArgumentException if supplied in is not supported.
     */
    public void registerOnce(String key, Core.In<?, ?> in) throws NullPointerException, IllegalArgumentException;

    /**
     * Shows a before registerd once element either creating it or refocusing, if still acitve.
     *
     * @param key the registered key
     * @return ture, if the key was registered before.
     * @throws NullPointerException if the key was null or blank.
     */
    boolean showOnce(String key) throws NullPointerException;

    <R, S extends R> CompletableFuture<T> show(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in);

    <Q, R, S extends R> Result<Q> eval(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in);

}
