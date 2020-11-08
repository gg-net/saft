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

import javafx.application.Platform;
import javafx.scene.control.Dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.subsystem.CoreUiFuture;
import eu.ggnet.saft.core.ui.builder.UiParameter.Type;

import static eu.ggnet.saft.core.UiUtil.exceptionRun;

/**
 *
 * @author oliver.guenther
 */
public class DialogBuilder {

    private static final Logger L = LoggerFactory.getLogger(DialogBuilder.class);

    private final PreBuilder preBuilder;

    private final Saft saft;

    private static final Type TYPE = Type.DIALOG;

    public DialogBuilder(PreBuilder pre) {
        this.preBuilder = pre;
        saft = preBuilder.saft();
    }

    /**
     * Creates the javafx Dialog via the producer, shows it and returns the evaluated result as Optional.
     *
     * @param <T>            type of the result
     * @param <V>            type of the result
     * @param dialogProducer the javafx Dialog producer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, V extends Dialog<T>> Result<T> eval(Callable<V> dialogProducer) {
        return new Result<>(internalShow2(null, dialogProducer).proceed()
                .thenApplyAsync(BuilderUtil::waitAndProduceResult, saft.executorService()));
    }

    /**
     * Creates the javafx Dialog via the producer, supplies the consumer part with the result of the preProducer, shows it and returns the evaluated result as
     * Optional.
     *
     * @param <T>            type of the result
     * @param <P>            result type of the preProducer
     * @param <V>            type of the result
     * @param preProducer    the preproducer, must not be null
     * @param dialogProducer the javafx Dialog producer, must not be null and must not return null.
     * @return the result of the evaluation, never null.
     */
    public <T, P, V extends Dialog<T> & Consumer<P>> Result<T> eval(Callable<P> preProducer, Callable<V> dialogProducer) {
        return new Result<>(internalShow2(preProducer, dialogProducer).proceed()
                .thenApplyAsync(BuilderUtil::waitAndProduceResult, saft.executorService()));
    }

    private <T, P, V extends Dialog<T>> CoreUiFuture internalShow2(Callable<P> preProducer, Callable<V> dialogProducer) {
        Objects.requireNonNull(dialogProducer, "The javafxPaneProducer is null, not allowed");
        if ( UiCore.isGluon() )
            throw new IllegalStateException("Javafx Dialog is not supported in gloun yet. If you really need this, call Olli. It's possible");

        return saft.core().prepare(() -> {
            UiParameter parm = UiParameter.fromPreBuilder(preBuilder).type(TYPE).build();

            // Produce the ui instance
            CompletableFuture<UiParameter> uiChain = CompletableFuture
                    .runAsync(() -> L.debug("Starting new Ui Element creation"), saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which fails.
                    .thenApplyAsync(v -> BuilderUtil.produceDialog(dialogProducer, parm), Platform::runLater)
                    .thenApplyAsync((UiParameter p) -> p.withPreResult(Optional.ofNullable(preProducer).map(pp -> exceptionRun(pp)).orElse(null)), saft.executorService())
                    .thenApply(BuilderUtil::consumePreResult);
            return uiChain;
        }, TYPE);
    }

}
