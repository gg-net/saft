/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.internal;

import java.util.Optional;
import java.util.function.Consumer;

import org.inferred.freebuilder.FreeBuilder;

import eu.ggnet.saft.api.IdSupplier;

import com.gluonhq.charm.glisten.control.Dialog;

/**
 * Internal builder parameter.
 * 
 * @author oliver.guenther
 */
@FreeBuilder
public abstract class GluonParameter {

    /**
     * Id.
     * 
     * @return optional id.
     */
    abstract Optional<String> id();

    /**
     * PreResult.
     * 
     * @return optional pre result.
     */
    abstract Optional<Object> preResult();

    /**
     * Gloun Dialog.
     * 
     * @return optional gluon dialog.
     */
    abstract Dialog<?> gluonDialog();

    /**
     * Result.
     * 
     * @return optional result. 
     */
    abstract Optional<?> result();

    public static class Builder extends GluonParameter_Builder {
    };

    public abstract Builder toBuilder();

    public final GluonParameter withPreResult(Object preResult) {
        if ( preResult == null ) return this;
        GluonParameter.Builder builder = toBuilder().preResult(preResult);
        if ( !id().isPresent() && preResult instanceof IdSupplier ) builder.nullableId(((IdSupplier)preResult).id());
        return builder.build();
    }

    public final GluonParameter optionalConsumePreResult() {
        if ( !preResult().isPresent() ) return this;
        if ( !(gluonDialog() instanceof Consumer) ) return this;
        ((Consumer)gluonDialog()).accept(preResult().get());
        return this;
    }

}
