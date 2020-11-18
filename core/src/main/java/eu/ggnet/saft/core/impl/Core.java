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

import javax.swing.JPanel;

import javafx.scene.layout.Pane;

import eu.ggnet.saft.core.ui.FxController;
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
     * Registers a Supplier with a key in the core for once useage.
     *
     * @param <U>
     * @param key          the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param paneSupplier the supplier of the pane
     * @throws NullPointerException if the key was null or blank or the supplier was null.
     */
    <U extends Pane> void registerOnceFx(String key, Supplier<U> paneSupplier) throws NullPointerException;

    /**
     * Registers a pane class with a key in the core for once useage.
     * Will be created via reflections. Intended usage pattern is in the cdi environment.
     *
     * @param key       the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param paneClass the class of the pane.
     * @throws NullPointerException if the key was null or blank or the controllerClass was null.
     */
    void registerOnceFx(String key, Class<? extends Pane> paneClass) throws NullPointerException;

    /**
     * Registers a Supplier with a key in the core for once useage.
     *
     * @param key           the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param panelSupplier the supplier for the panel
     * @throws NullPointerException if the key was null or blank or the supplier was null.
     */
    void registerOnceSwing(String key, Supplier<? extends JPanel> panelSupplier) throws NullPointerException;

    /**
     * Registers a panel class with a key in the core for once useage.
     * Will be created via reflections. Intended usage pattern is in the cdi environment.
     *
     * @param key        the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param panelClass the class of the panel.
     * @throws NullPointerException
     */
    void registerOnceSwing(String key, Class<? extends JPanel> panelClass) throws NullPointerException;

    /**
     * Registers an FxController with a key in the core for once useage.
     *
     * @param key             the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param controllerClass the fx controller class
     * @throws NullPointerException if the key was null or blank or the controllerClass was null.
     */
    void registerOnceFxml(String key, Class<? extends FxController> controllerClass) throws NullPointerException;

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
