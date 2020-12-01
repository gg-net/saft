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

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.scene.control.Dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Core;
import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.impl.UiParameter.Type;

/**
 * Saft Dialog Builder.
 *
 * @author oliver.guenther
 */
public class DialogBuilder {

    private static final Logger L = LoggerFactory.getLogger(DialogBuilder.class);

    private final PreBuilder preBuilder;

    private final Saft saft;

    private static final Type TYPE = Type.DIALOG;

    /**
     * Creates a new Dialogbuilder.
     *
     * @param pre the prebuilser, must not be null.
     */
    public DialogBuilder(PreBuilder pre) {
        this.preBuilder = pre;
        saft = preBuilder.saft();
    }

    /**
     * Creates the javafx Dialog via the producer, shows it and returns the evaluated result.
     *
     * @param <T>            type of the result
     * @param <V>            type of the dialog
     * @param dialogProducer the javafx Dialog producer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, V extends Dialog<T>> Result<T> eval(Supplier<V> dialogProducer) {
        return saft.core().eval(preBuilder, Optional.empty(), new Core.In<>(Dialog.class, () -> dialogProducer.get()));
    }

    /**
     * Creates the javafx Dialog via the producer, supplies the consumer part with the result of the preProducer, shows it and returns the evaluated result.
     *
     * @param <T>            type of the result
     * @param <P>            result type of the preProducer
     * @param <V>            type of the dialog
     * @param preProducer    the preproducer, must not be null
     * @param dialogProducer the javafx Dialog producer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, P, V extends Dialog<T> & Consumer<P>> Result<T> eval(Callable<P> preProducer, Supplier<V> dialogProducer) {
        return saft.core().eval(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(Dialog.class, () -> dialogProducer.get()));
    }

}
