/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.internal;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.builder.UiWorkflowBreak;

import com.gluonhq.charm.glisten.control.Dialog;

/**
 * Like Builder Util but on the Gluon Layer.
 *
 * @author oliver.guenther
 */
public class GluonBuilderUtil {

    private final static Logger L = LoggerFactory.getLogger(GluonBuilderUtil.class);

    static <T, V extends Dialog<T>> GluonParameter produceDialog(Callable<V> producer) {
        try {
            V dialog = producer.call();
            L.debug("produceDialog: {}", dialog);
            return new GluonParameter.Builder().gluonDialog(dialog).build();
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    static GluonParameter consumePreResult(GluonParameter in) {
        return in.optionalConsumePreResult();
    }

    static GluonParameter constructGluonDialog(GluonParameter in) {
        Dialog<?> gluonDialog = in.gluonDialog();
        return in.toBuilder().result(gluonDialog.showAndWait()).build();
    }

    static <T> T waitAndProduceResult(GluonParameter in) {
        Optional<T> result = (Optional<T>)in.result();
        if ( !result.isPresent() ) throw new UiWorkflowBreak(UiWorkflowBreak.Type.NULL_RESULT);
        return result.get();

    }
}
