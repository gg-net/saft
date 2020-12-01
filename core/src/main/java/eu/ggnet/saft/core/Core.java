/*
 * Swing and JavaFx Together (Saft)
 * Copyright (C) 2020  Oliver Guenther <oliver.guenther@gg-net.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 with
 * Classpath Exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with Classpath Exception along with this program.
 */
package eu.ggnet.saft.core;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import eu.ggnet.saft.core.ui.AlertType;
import eu.ggnet.saft.core.ui.UiParent;
import eu.ggnet.saft.core.ui.builder.PreBuilder;
import eu.ggnet.saft.core.ui.builder.Result;

/**
 * Core Interface, contains all Functions a Core must implement.
 * <p>
 * Most of the methods have an optional characteristic, meaning, they do not fail on a dead core.
 *
 * @author oliver.guenther
 * @param <T> type of the ui window class (swing = JFrame, javafx = Stage).
 */
public interface Core<T> {

    //The R and S construct is the only way to ensure, that the Suppliers result extends the class token.
    /**
     * Input helper class, to ensure a dependency between class tokens and suppliers.
     *
     * @param <R> type of the class.
     * @param <S> type of the supplier, which extends the class type.
     */
    public static class In<R, S extends R> {

        private final Supplier<S> supplier;

        private final Class<R> clazz;

        /**
         * Input of a class and a supplier.
         * Every Core must support for R and S:
         * <ul>
         * <li>javafx.scene.layout.Pane</li>
         * <li>javax.swing.JPanel</li>
         * <li>javafx.scene.control.Dialog<li>
         * </ul>
         *
         * @param clazz    The type of implementation.
         * @param supplier The supplier to create the instance.
         */
        public In(Class<R> clazz, Supplier<S> supplier) {
            this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
            this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
        }

        /**
         * Input of a class only.
         * Every Core must support all classes of {@link In } an:
         * <ul>
         * <li>eu.ggnet.saft.core.ui.FxController</li>
         * </ul>
         *
         * @param clazz the type of the implementation.
         */
        public In(Class<R> clazz) {
            this.supplier = null;
            this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
        }

        /**
         * Returns the type of implementation.
         *
         * @return the type of implementation.
         */
        public Class<R> clazz() {
            return clazz;
        }

        /**
         * Returns an optional supplier.
         *
         * @return an optional supplier.
         */
        public Optional<Supplier<S>> supplier() {
            return Optional.ofNullable(supplier);
        }

        @Override
        public String toString() {
            return "In{" + "supplier=" + supplier + ", clazz=" + clazz + '}';
        }

    }

    /**
     * Sets the main window once.
     * Saft works without the main window (e.g. Application in the tray), but it's highly advisable to set it.
     * It can only be set once.
     *
     * @param window the window to be set.
     * @throws NullPointerException  if window is null.
     * @throws IllegalStateException if window was set allready.
     */
    void initMain(T window) throws NullPointerException, IllegalStateException;

    /**
     * Unwraps a UiParent.
     *
     * @param parent the parent to unwrap. A null parent will retrunt an empty Optional.
     * @return an optional, containing the window implementation.
     */
    Optional<T> unwrap(UiParent parent);

    /**
     * Unwraps the optional ui parent.
     *
     * @param parent the optional parent to unwrap, must not be null
     * @return an optional, containing the window implementation.
     * @throws NullPointerException if parent is null.
     */
    Optional<T> unwrap(Optional<UiParent> parent) throws NullPointerException;

    /**
     * Returns an optional main ui parent.
     *
     * @return an optional main ui parent.
     */
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
     * @return true if the core of this type is active.
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

    /**
     * Show an alert.
     *
     * @param message  the message, must not be null
     * @param uiparent an optional parent, must not be null
     * @param title    the optional title, not be null
     * @param type     the optional type, not be null
     * @throws NullPointerException if one parameter is null.
     */
    void showAlert(String message, Optional<UiParent> uiparent, Optional<String> title, Optional<AlertType> type) throws NullPointerException;

    /**
     * Non-compilesafe show implementaion used by all builders.
     * The Core implementation will enforces all it's rules on the supplied parameters.
     *
     * @param <R>         type of the uielement.
     * @param <S>         type of supplier of the element.
     * @param prebuilder  the prebuilder, must not be null
     * @param preProducer an optional preproduce. If not empty, the R of In must implement a Consumer of the return type.
     * @param in          input for the uielement to be shown.
     * @return a CompletableFuture containing the active window element.
     */
    <R, S extends R> CompletableFuture<T> show(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in);

    /**
     * Non-comilesafe eval implementation used by all builders.
     * The Core implementation will enforces all it's rules on the supplied parameters.
     *
     * @param <Q>         type of the evaluated result.
     * @param <R>         type of the uielement.
     * @param <S>         type of supplier of the element.
     * @param prebuilder  prebuilder, must not be null.
     * @param preProducer an optional preproduce. If not empty, the R of In must implement a Consumer of the return type.
     * @param in          input for the uielement to be shown.
     * @return a Result of the ui.
     */
    <Q, R, S extends R> Result<Q> eval(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in);

}
