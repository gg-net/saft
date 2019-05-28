/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon;

import com.gluonhq.charm.glisten.control.Dialog;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            return GluonParameter.dialog(dialog);
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    static GluonParameter consumePreResult(GluonParameter in) {
        return in.optionalConsumePreResult();
    }

    static GluonParameter constructGluonDialog(GluonParameter in) {
        Dialog<?> gluonDialog = in.gluonDialog().get();
        // if (!in.extractFrame()) stage.initOwner(in.uiParent().fxOrMain()); // no stages
        // in.modality().ifPresent(m -> stage.initModality(m)); // no Modality

        // stage.setTitle(in.toTitle());  // May be later
        // stage.getIcons().addAll(loadJavaFxImages(in.extractReferenceClass())); // may be later
        
        // registerActiveWindows(in.toKey(), stage); // only one active window in gluon
        // if (in.isStoreLocation()) registerAndSetStoreLocation(in.extractReferenceClass(), stage); // not in gluon
        
        // in.getClosedListenerImplemetation().ifPresent(elem -> stage.setOnCloseRequest(e -> elem.closed())); // may be later
        
        return in.toBuilder().result(gluonDialog.showAndWait()).build();
    }
    
        static <T> T waitAndProduceResult(GluonParameter in) {
            // INFO: for now we only have Gluon Dialogs, so there is no validation needed.
//        if ( !(in.type().selectRelevantInstance(in) instanceof ResultProducer || in.type().selectRelevantInstance(in) instanceof javafx.scene.control.Dialog) ) {
//            throw new IllegalStateException("Calling Produce Result on a none ResultProducer. Try show instead of eval");
//        }
//        try {
//            if ( UiCore.isSwing() ) BuilderUtil.wait(in.window().get()); // Only needed in Swing mode. In JavaFx the showAndWait() is allways used.
//        } catch (InterruptedException | IllegalStateException | NullPointerException ex) {
//            throw new CompletionException(ex);
//        }
//        if ( in.type().selectRelevantInstance(in) instanceof ResultProducer ) {
//            T result = ((ResultProducer<T>)in.type().selectRelevantInstance(in)).getResult();
//            if ( result == null ) throw new UiWorkflowBreak(UiWorkflowBreak.Type.NULL_RESULT);
//            return result;
//        } else {
//            T result = ((javafx.scene.control.Dialog<T>)in.type().selectRelevantInstance(in)).getResult();
//            if ( result == null ) throw new UiWorkflowBreak(UiWorkflowBreak.Type.NULL_RESULT);
//            return result;
//        }
            return (T)in.result();

    }
}
