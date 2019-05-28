/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Dl;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.builder.GluonSupport;

/**
 *
 * @author oliver.guenther
 */
public class Gi {
    
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    
    private static final Logger L = LoggerFactory.getLogger(Gi.class);
    
    public static void startUp() {
        L.debug("startUp() called");
        if (!RUNNING.compareAndSet(false, true)) return;
        L.info("startUp(): First call, registering saft-gluon services in Dl");
        Dl.local().add(GluonSupport.class, new GluonSupportService());
        UiCore.overwriteFinalExceptionConsumer(new DefaultGluonFinalExceptionConsumer());
    }
    
    public static GluonPreBuilder build() {
        return new GluonPreBuilder();
    }
}
