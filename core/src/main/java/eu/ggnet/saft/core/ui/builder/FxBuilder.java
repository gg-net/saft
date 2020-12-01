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
package eu.ggnet.saft.core.ui.builder;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Core;
import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.impl.UiParameter.Type;
import eu.ggnet.saft.core.ui.ResultProducer;

/**
 * Saft Fx Builder.
 *
 * @author oliver.guenther
 */
public class FxBuilder {

    private static final Logger L = LoggerFactory.getLogger(FxBuilder.class);

    private final PreBuilder preBuilder;

    private final Saft saft;

    private static final Type TYPE = Type.FX;

    /**
     * Creates the builder.
     *
     * @param preBuilder the prebuilder, must not be null.
     */
    public FxBuilder(PreBuilder preBuilder) {
        this.preBuilder = Objects.requireNonNull(preBuilder, "preBuilder must not be null");
        this.saft = preBuilder.saft();
    }

    /**
     * Creates the javafx Pane via the producer and shows it on the correct thread.
     *
     * @param <V>      the type of the pane
     * @param supplier the supplier of the JPanel, must not be null and must not return null.
     */
    public <V extends Pane> void show(Supplier<V> supplier) {
        saft.core().show(preBuilder, Optional.empty(), new Core.In<>(Pane.class, () -> supplier.get()));
    }

    /**
     * Creates the javafx Pane via the class token and shows it on the correct thread.
     *
     * @param <P>             result type of the preProducer
     * @param <V>             the type of the pane
     * @param javafxPaneClass the class of the JPanel, must not be null.
     */
    public <P, V extends Pane> void show(Class<V> javafxPaneClass) {
        saft.core().show(preBuilder, Optional.empty(), new Core.In<>(javafxPaneClass));
    }

    /**
     * Creates the javafx Pane via the supplier, supplies the consumer part with the result of the preProducer and shows it.
     *
     * @param <P>         result type of the preProducer
     * @param <V>         javafx Pane and Consumer type
     * @param preProducer the preProducer, must not be null
     * @param supplier    the supplier of the JPanel, must not be null and must not return null.
     */
    public <P, V extends Pane & Consumer<P>> void show(Callable<P> preProducer, Supplier<V> supplier) {
        saft.core().show(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(Pane.class, () -> supplier.get()));
    }

    /**
     * Creates the javafx Pane via the class token, supplies the consumer part with the result of the preProducer and shows it.
     *
     * @param <P>             result type of the preProducer
     * @param <V>             javafx Pane and Consumer type
     * @param preProducer     the preProducer, must not be null
     * @param javafxPaneClass the class of the JPanel, must not be null.
     */
    public <P, V extends Pane & Consumer<P>> void show(Callable<P> preProducer, Class<V> javafxPaneClass) {
        saft.core().show(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(javafxPaneClass));
    }

    /**
     * Creates the javafx Pane via the supplier, shows it and returns the evaluated result as Optional.
     *
     * @param <T>      type of the result
     * @param <V>      type of the pane
     * @param supplier the supplier, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, V extends Pane & ResultProducer<T>> Result<T> eval(Supplier<V> supplier) {
        return saft.core().eval(preBuilder, Optional.empty(), new Core.In<>(Pane.class, () -> supplier.get()));
    }

    /**
     * Creates the javafx Pane via the class token, shows it and returns the evaluated result as Optional.
     *
     * @param <T>             type of the result
     * @param <V>             type of the pane
     * @param javafxPaneClass the class token, must not be null.
     * @return the result of the evaluation, never null.
     */
    public <T, V extends Pane & ResultProducer<T>> Result<T> eval(Class<V> javafxPaneClass) {
        return saft.core().eval(preBuilder, Optional.empty(), new Core.In<>(javafxPaneClass));
    }

    /**
     * Creates the javafx Pane via the supplier, supplies the consumer part with the result of the preProducer, shows it and returns the evaluated result.
     *
     * @param <T>         type of the result
     * @param <P>         result type of the preProducer
     * @param <V>         type of the result
     * @param preProducer the preproducer, must not be null
     * @param supplier    the supplier, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, P, V extends Pane & Consumer<P> & ResultProducer<T>> Result<T> eval(Callable<P> preProducer, Supplier<V> supplier) {
        return saft.core().eval(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(Pane.class, () -> supplier.get()));
    }

    /**
     * Creates the javafx Pane via the class token, supplies the consumer part with the result of the preProducer, shows it and returns the evaluated result.
     *
     * @param <T>             type of the result
     * @param <P>             result type of the preProducer
     * @param <V>             type of the result
     * @param preProducer     the preproducer, must not be null
     * @param javafxPaneClass the class token, must not be null.
     * @return the result of the evaluation, never null.
     */
    public <T, P, V extends Pane & Consumer<P> & ResultProducer<T>> Result<T> eval(Callable<P> preProducer, Class<V> javafxPaneClass) {
        return saft.core().eval(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(javafxPaneClass));
    }

}
