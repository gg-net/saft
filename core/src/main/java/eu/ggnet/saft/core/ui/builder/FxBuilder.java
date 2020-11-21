/*
 * Copyright (C) 2017 GG-Net GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.impl.Core;
import eu.ggnet.saft.core.ui.ResultProducer;
import eu.ggnet.saft.core.impl.UiParameter.Type;


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
 * Handles Fx elements on Saft.
 * This class has no impact how the emelemts are wrapped, only that the elements are based on Swing.
 *
 * @author oliver.guenther
 */
public class FxBuilder {

    private static final Logger L = LoggerFactory.getLogger(FxBuilder.class);

    private final PreBuilder preBuilder;

    private final Saft saft;

    private static final Type TYPE = Type.FX;

    public FxBuilder(PreBuilder preBuilder) {
        this.preBuilder = Objects.requireNonNull(preBuilder, "preBuilder must not be null");
        this.saft = preBuilder.saft();
    }

    /**
     * Creates the javafx Pane via the producer and shows it on the correct thread.
     * <p>
     * Case: Ia.
     *
     * @param <V>                the type
     * @param javafxPaneProducer the producer of the JPanel, must not be null and must not return null.
     */
    public <V extends Pane> void show(Supplier<V> javafxPaneProducer) {
        saft.core().show(preBuilder, Optional.empty(), new Core.In<>(Pane.class, () -> javafxPaneProducer.get()));
    }

    public <P, V extends Pane> void show(Class<V> javafxPaneClass) {
        saft.core().show(preBuilder, Optional.empty(), new Core.In<>(javafxPaneClass));
    }

    /**
     * Creates the javafx Pane via the producer, supplies the consumer part with the result of the preProducer and shows it.
     * <p>
     * Case: Ib
     *
     * @param <P>                result type of the preProducer
     * @param <V>                javafx Pane and Consumer type
     * @param preProducer        the preProducer, must not be null
     * @param javafxPaneProducer the producer of the JPanel, must not be null and must not return null.
     */
    public <P, V extends Pane & Consumer<P>> void show(Callable<P> preProducer, Supplier<V> javafxPaneProducer) {
        saft.core().show(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(Pane.class, () -> javafxPaneProducer.get()));
    }

    public <P, V extends Pane & Consumer<P>> void show(Callable<P> preProducer, Class<V> javafxPaneClass) {
        saft.core().show(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(javafxPaneClass));
    }

    /**
     * Creates the javafx Pane via the producer, shows it and returns the evaluated result as Optional.
     * <p>
     * Case: Ic
     *
     * @param <T>                type of the result
     * @param <V>                type of the result
     * @param javafxPaneProducer the producer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, V extends Pane & ResultProducer<T>> Result<T> eval(Supplier<V> javafxPaneProducer) {
        return saft.core().eval(preBuilder, Optional.empty(), new Core.In<>(Pane.class, () -> javafxPaneProducer.get()));
    }

    public <T, V extends Pane & ResultProducer<T>> Result<T> eval(Class<V> javafxPaneClass) {
        return saft.core().eval(preBuilder, Optional.empty(), new Core.In<>(javafxPaneClass));
    }

    /**
     * Creates the javafx Pane via the producer, supplies the consumer part with the result of the preProducer, shows it and returns the evaluated result as
     * Optional.
     *
     * @param <T>                type of the result
     * @param <P>                result type of the preProducer
     * @param <V>                type of the result
     * @param preProducer        the preproducer, must not be null
     * @param javafxPaneProducer the producer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, P, V extends Pane & Consumer<P> & ResultProducer<T>> Result<T> eval(Callable<P> preProducer, Supplier<V> javafxPaneProducer) {
        return saft.core().eval(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(Pane.class, () -> javafxPaneProducer.get()));
    }

    public <T, P, V extends Pane & Consumer<P> & ResultProducer<T>> Result<T> eval(Callable<P> preProducer, Class<V> javafxPaneClass) {
        return saft.core().eval(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(javafxPaneClass));
    }

}
