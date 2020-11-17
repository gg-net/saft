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

import java.awt.EventQueue;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.impl.Core;
import eu.ggnet.saft.core.impl.CoreUiFuture;
import eu.ggnet.saft.core.ui.ResultProducer;
import eu.ggnet.saft.core.ui.builder.UiParameter.Type;

import static eu.ggnet.saft.core.UiUtil.exceptionRun;


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
 * Handles Swing elements on Saft.
 * This class has no impact how the emelemts are wrapped, only that the elements are based on Swing.
 *
 * @author oliver.guenther
 */
public class SwingBuilder {

    private static final Logger L = LoggerFactory.getLogger(SwingBuilder.class);

    private final PreBuilder preBuilder;

    private final Saft saft;

    private static final Type TYPE = Type.SWING;

    public SwingBuilder(PreBuilder pre) {
        this.preBuilder = pre;
        this.saft = preBuilder.saft();
    }

    /**
     * Creates the JPanel via the producer and shows it on the correct thread.
     * <p>
     * Case: Ia.
     *
     * @param <V>                the type
     * @param swingPanelProducer the swingPanelProducer of the JPanel, must not be null and must not return null.
     */
    public <V extends JPanel> void show(Supplier<V> swingPanelProducer) {
        saft.core().show(preBuilder, Optional.empty(), new Core.In<>(JPanel.class, () -> swingPanelProducer.get()));
//        internalShow2(null, swingPanelProducer).proceed().handle(Ui.handler());
    }

    /**
     * Creates the JPanel via the producer, supplies the consumer part with the result of the preProducer and shows it.
     * <p>
     * Case: Ib
     *
     * @param <P>                result type of the preProducer
     * @param <V>                type parameter
     * @param preProducer        the preproducer, must not be null
     * @param swingPanelProducer the swingPanelProducer, must not be null and must not return null.
     */
    public <P, V extends JPanel & Consumer<P>> void show(Callable<P> preProducer, Supplier<V> swingPanelProducer) {
        saft.core().show(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(JPanel.class, () -> swingPanelProducer.get()));

        // internalShow2(preProducer, swingPanelProducer).proceed().handle(Ui.handler());
    }

    /**
     * Creates the JPanel via the producer, shows it and returns the evaluated result as Optional.
     * <p>
     * Case: Ic
     *
     * @param <T>                type of the result
     * @param <V>                type parameter
     * @param swingPanelProducer the swingPanelProducer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, V extends JPanel & ResultProducer<T>> Result<T> eval(Supplier<V> swingPanelProducer) {
        return saft.core().eval(preBuilder, Optional.empty(), new Core.In<>(JPanel.class, () -> swingPanelProducer.get()));
//
//
//        return new Result<>(internalShow2(null, swingPanelProducer).proceed()
//                .thenApplyAsync(BuilderUtil::waitAndProduceResult, saft.executorService()));
    }

    /**
     * Creates the JPanel via the producer, supplies the consumer part with the result of the preProducer, shows it and returns the evaluated result as
     * Optional.
     *
     * @param <T>                type of the result
     * @param <P>                result type of the preProducer
     * @param <V>                type parameter
     * @param preProducer        the preproducer, must not be null
     * @param swingPanelProducer the swingPanelProducer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, P, V extends JPanel & Consumer<P> & ResultProducer<T>> Result<T> eval(Callable<P> preProducer, Supplier<V> swingPanelProducer) {
        return saft.core().eval(preBuilder, Optional.ofNullable(preProducer), new Core.In<>(JPanel.class, () -> swingPanelProducer.get()));
//        return new Result<>(internalShow2(preProducer, swingPanelProducer).proceed()
//                .thenApplyAsync(BuilderUtil::waitAndProduceResult, saft.executorService()));
    }

    private <T, P, V extends JPanel> CoreUiFuture internalShow2(Callable<P> preProducer, Callable<V> jpanelProducer) {
        Objects.requireNonNull(jpanelProducer, "The jpanelaneProducer is null, not allowed");
        if ( UiCore.isGluon() ) throw new IllegalStateException("Swing Elements are not supported in gloun (Wont be visible in Android or iOs");

        // TODO: the parent handling must be optimized. And the javaFx
        return preBuilder.saft().core().prepare(() -> {
            UiParameter parm = UiParameter.fromPreBuilder(preBuilder).type(TYPE).build();

            // Produce the ui instance
            CompletableFuture<UiParameter> uiChain = CompletableFuture
                    .runAsync(() -> L.debug("Starting new Ui Element creation"), saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which fails.
                    .thenApplyAsync(v -> BuilderUtil.produceJPanel(jpanelProducer, parm), EventQueue::invokeLater)
                    .thenApplyAsync((UiParameter p) -> p.withPreResult(Optional.ofNullable(preProducer).map(pp -> exceptionRun(pp)).orElse(null)), saft.executorService())
                    .thenApply(BuilderUtil::consumePreResult);
            return uiChain;
        }, TYPE);

    }

}
