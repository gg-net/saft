/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.gluon.internal.GluonPreBuilder;

/**
 * Single entry point for all compile-safe gluon specific activities.
 *
 * @author oliver.guenther
 */
public class Gi {

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private static final Logger L = LoggerFactory.getLogger(Gi.class);

    /**
     * Startup the gloun specific parts of saft.
     * This method can be called multiple times, only the first is relevant.
     * In the default implementation, this method is called reflective from saft-core UiCore.continueGluon().
     */
    public static void startUp() {
        L.debug("startUp() called");
        if ( !RUNNING.compareAndSet(false, true) ) return;
        L.info("startUp(): First call, registering saft-gluon services in Dl");
        // TODO: würde auch über neuen Gluon core gehandelt.
//        UiCore.global().gluonSupport(new GluonSupportService());
        //       UiCore.overwriteFinalExceptionConsumer(new DefaultGluonFinalExceptionConsumer());
    }

    /**
     * Allows to build gluon ui components.
     *
     * @return returns the gluon pre builder.
     */
    public static GluonPreBuilder build() {
        return new GluonPreBuilder();
    }
}
