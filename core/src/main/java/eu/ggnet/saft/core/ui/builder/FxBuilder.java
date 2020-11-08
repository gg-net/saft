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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.subsystem.CoreUiFuture;
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
    public <V extends Pane> void show(Callable<V> javafxPaneProducer) {
        internalShow2(null, javafxPaneProducer).proceed().handle(Ui.handler());
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
    public <P, V extends Pane & Consumer<P>> void show(Callable<P> preProducer, Callable<V> javafxPaneProducer) {
        internalShow2(preProducer, javafxPaneProducer).proceed().handle(Ui.handler());
    }

    //TODO: Reconsider after change of core handling.
    public <V extends Pane> Runnable showable(Callable<V> javafxPaneProducer) {

        return null;
    }

    // TODO: Zusatzvariante um eine Managementinstance über cdi zu erzeugen, das fx elemente final sind und damit keinen @Scope bekommen können.
    // Supplier wird via cdi erzeugt und get liefert dann die ui komponente. Der entwickler kann überlegen, wie er den Supplier mit ui bei erzeugen füllt
    // Außerdem: Defaultsupplier um nur eine Fx Element zu erzeugen, class MyPane. inner class MySupplier extends DefaultSupplier<MyPane> und im constructor
    // super(MyPane.class)
    // und das ganze auf construct und eval vertreilen.
    // ggf auch nur im CDI mode laufen lassen, sonst exception.
    public <P, V extends Pane & Consumer<P>> void show(Callable<P> preProducer, Class<Supplier<V>> javafxPaneClass) {

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
    public <T, V extends Pane & ResultProducer<T>> Result<T> eval(Callable<V> javafxPaneProducer) {
        return new Result<>(internalShow2(null, javafxPaneProducer).proceed()
                .thenApplyAsync(BuilderUtil::waitAndProduceResult, saft.executorService()));
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
    public <T, P, V extends Pane & Consumer<P> & ResultProducer<T>> Result<T> eval(Callable<P> preProducer, Callable<V> javafxPaneProducer) {
        return new Result<>(internalShow2(preProducer, javafxPaneProducer).proceed()
                .thenApplyAsync(BuilderUtil::waitAndProduceResult, saft.executorService()));
    }

    /**
     * Internal implementation, breaks the compile safty of the public methodes.
     * For now we have two normal execptions. The UiWorkflowBreak (allready open) and the NoSuchElementException (no result)
     *
     * @param <P>                type of the result
     * @param <V>                type of the result
     * @param preProducer        a pre processor, must not be null
     * @param javafxPaneProducer the producer, must not be null and must not return null.
     * @return a completeable future processing in the background for the result
     */
    private <P, V extends Pane> CoreUiFuture internalShow2(Callable<P> preProducer, Callable<V> javafxPaneProducer) {
        Objects.requireNonNull(javafxPaneProducer, "The javafxPaneProducer is null, not allowed");

        /*
        // Was for gluon.
        if ( UiCore.isGluon() ) {
            return uniChain
                    .thenApply(UiCore.global().gluonSupport().get()::constructJavaFx); // Allready on JavaFx Thread.
         */
        // TODO: the parent handling must be optimized. And the javaFx
        return saft.core().prepare(() -> {
            UiParameter parm = UiParameter.fromPreBuilder(preBuilder).type(TYPE).build();

            // Produce the ui instance
            CompletableFuture<UiParameter> uiChain = CompletableFuture
                    .runAsync(() -> L.debug("Starting new Ui Element creation"), preBuilder.saft().executorService()) // Make sure we are not switching from Swing to JavaFx directly, which fails sometimes.
                    .thenApplyAsync(v -> BuilderUtil.producePane(javafxPaneProducer, parm), Platform::runLater)
                    .thenApplyAsync((UiParameter p) -> p.withPreResult(Optional.ofNullable(preProducer).map(pp -> exceptionRun(pp)).orElse(null)), preBuilder.saft().executorService())
                    .thenApplyAsync(BuilderUtil::consumePreResult, Platform::runLater);
            return uiChain;
        }, TYPE);

    }
}
