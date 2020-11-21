/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.internal;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.UiUtil;
import eu.ggnet.saft.core.ui.builder.Result;

import com.gluonhq.charm.glisten.control.Dialog;

/**
 * Compile-safe gluon dialog builder.
 *
 * @author oliver.guenther
 */
public class GluonDialogBuilder {

    private final static Logger L = LoggerFactory.getLogger(GluonDialogBuilder.class);

    // See eval methods of Ui.build().dialog()
    public <T, V extends Dialog<T>> Result<T> eval(Callable<V> dialogProducer) {
        return new Result<>(internalShow(null, dialogProducer)
                .thenApplyAsync(GluonBuilderUtil::waitAndProduceResult, UiCore.getExecutor()));
    }

    // See eval methods of Ui.build().dialog()
    public <T, P, V extends Dialog<T> & Consumer<P>> Result<T> eval(Callable<P> preProducer, Callable<V> dialogProducer) {
        return new Result<>(internalShow(preProducer, dialogProducer)
                .thenApplyAsync(GluonBuilderUtil::waitAndProduceResult, UiCore.getExecutor()));

    }

    private <T, P, V extends Dialog<T>> CompletableFuture<GluonParameter> internalShow(Callable<P> preProducer, Callable<V> dialogProducer) {

        // FIXME: For now, there is no prebuilder to set values, like title.
        CompletableFuture<GluonParameter> uniChain = CompletableFuture
                .runAsync(() -> L.debug("Starting new Ui Element creation of Type GluonDialog"), UiCore.getExecutor())
                .thenApplyAsync(v -> GluonBuilderUtil.produceDialog(dialogProducer), Platform::runLater)
                .thenApplyAsync((GluonParameter p) -> p.withPreResult(Optional.ofNullable(preProducer).map(pp -> UiUtil.exceptionRun(pp)).orElse(null)), UiCore.getExecutor())
                // no breakIfOnceAndActive is needed here.
                .thenApply(GluonBuilderUtil::consumePreResult);

        // TODO: Wenn geht das eh in den core.
//        if ( !UiCore.isGluon() ) {
//            throw new UnsupportedOperationException("GlounDialog in Swingmode or FXMode is not yet implemented");
//        }
        return uniChain
                .thenApplyAsync(GluonBuilderUtil::constructGluonDialog, Platform::runLater);
    }

}
