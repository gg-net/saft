/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon;

import com.gluonhq.charm.glisten.control.Dialog;
import eu.ggnet.saft.api.IdSupplier;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author oliver.guenther
 */
// TODO: IF it works, convert to freebuilder.
public class GluonParameter {

    public static GluonParameter dialog(Dialog<?> dialog) {
        return new GluonParameter(dialog);
    }

    //TODO: Fakebuilder
    public class Builder {

        public Builder preResult(Object preResult) {
            GluonParameter.this.preResult = preResult;
            return this;
        }

        public Builder nullableId(String id) {
            GluonParameter.this.id = id;
            return this;
        }

        public Builder result(Optional<?> result) {
            GluonParameter.this.result = result;
            return this;
        }        
        
        public GluonParameter build() {
            return GluonParameter.this;
        }
    }

    private Dialog<?> gluonDialog;

    private Object preResult;

    private String id;
    
    private Optional<?> result;

    public GluonParameter(Dialog<?> dialog) {
        this.gluonDialog = dialog;
    }

    public Builder toBuilder() {
        return new Builder();
    }

    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    public Optional<Object> preResult() {
        return Optional.ofNullable(preResult);
    }

    public Optional<Dialog<?>> gluonDialog() {
        return Optional.ofNullable(gluonDialog);
    }
    
    public Optional<?> result() {
        return result;
    }
    
    public final GluonParameter withPreResult(Object preResult) {
        if (preResult == null) return this;
        GluonParameter.Builder builder = toBuilder().preResult(preResult);
        if (!id().isPresent() && preResult instanceof IdSupplier) builder.nullableId(((IdSupplier) preResult).id());
        return builder.build();
    }

    public final GluonParameter optionalConsumePreResult() {
        if (!preResult().isPresent()) return this;
        if (!(gluonDialog instanceof Consumer)) return this;
        ((Consumer) gluonDialog).accept(preResult().get());
        return this;
    }

}
