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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Core;
import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.impl.UiParameter.Type;
import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.ResultProducer;

/*
    I - 4 Fälle:
    a. nur zeigen. Ui consumiert nix und prodziert kein result
    b. consumer ui of type v
    c. result producer of type r
    d. conumer and result producer of type v,r

    II - 3. Uis
    a. Swing JPanel
    b. JavaFx Pane
    c. JavaFxml + Controller Class


    Examples:
    Ui.build().fx().parrent().id("blaa").eval(fdsafdsafddsa);

    Ui.build().swing().show(()->Demo());

 */
/**
 * The Saft Fxml Builder.
 *
 * @author oliver.guenther
 */
public class FxmlBuilder {

    private static final Logger L = LoggerFactory.getLogger(FxmlBuilder.class);

    private final PreBuilder preBuilder;

    private final Saft saft;

    private static final Type TYPE = Type.FXML;

    /**
     * Creates the builder.
     *
     * @param pre the prebuilder, must not be null.
     */
    public FxmlBuilder(PreBuilder pre) {
        this.preBuilder = pre;
        this.saft = preBuilder.saft();
    }

    /**
     * Creates a fxml view/controller via the controllerClass and shows it on the correct thread.
     * <p>
     * Case: Ia.
     *
     * @param <V>                 the type of controller class.
     * @param fxmlControllerClass the controller class, must not be null.
     */
    public <V extends FxController> void show(Class<V> fxmlControllerClass) {
        saft.core().show(preBuilder, Optional.empty(), new Core.In<>(fxmlControllerClass));
    }

    /**
     * Creates a fxml view/controller via the controllerClass, supplies the consumer part with the result of the preProducer and shows it.
     * <p>
     * Case: Ib
     *
     * @param <P>                 result type of the preProducer
     * @param <V>                 the type of controller class.
     * @param fxmlControllerClass the controller class, must not be null.
     * @param preProducer         the preproducer, must not be null
     *                            javafxPaneProducer swingPanelProducer the swingPanelProducer, must not be null and must not return null.
     */
    public <P, V extends FxController & Consumer<P>> void show(Callable<P> preProducer, Class<V> fxmlControllerClass) {
        saft.core().show(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(fxmlControllerClass));
    }

    /**
     * Creates a fxml view/controller via the controllerClass, shows it and returns the evaluated result as Optional.
     * <p>
     * Case: Ic
     *
     * @param <T>                 type of the result
     * @param <V>                 the type of controller class.
     * @param fxmlControllerClass the controller class, must not be null.
     * @return the result of the evaluation, never null.
     */
    public <T, V extends FxController & ResultProducer<T>> Result<T> eval(Class<V> fxmlControllerClass) {
        return saft.core().eval(preBuilder, Optional.empty(), new Core.In<>(fxmlControllerClass));
    }

    /**
     * Creates a fxml view/controller via the controllerClass, supplies the consumer part with the result of the preProducer, shows it and returns the evaluated
     * result as
     * Optional.
     *
     * @param <T>                 type of the result
     * @param <P>                 result type of the preProducer
     * @param <V>                 the type of controller class.
     * @param preProducer         the preproducer, must not be null
     * @param fxmlControllerClass the controller class, must not be null.
     * @return the result of the evaluation, never null.
     */
    public <T, P, V extends FxController & Consumer<P> & ResultProducer<T>> Result<T> eval(Callable<P> preProducer, Class<V> fxmlControllerClass) {
        return saft.core().eval(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(fxmlControllerClass));
    }

}
