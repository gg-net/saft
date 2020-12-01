/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter15;

import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.*;
import javax.inject.Inject;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.ui.LocationStorage;

/**
 *
 * @author oliver.guenther
 */
@ApplicationScoped
@Specializes
public class CdiFxSaft extends Saft {

    // Wird nur verwendet um später in den Uielementen injected zu werden. Nicht notwendig für Cdi Saft.
    @Produces
    @Value
    private String value = "Ein Wert über CDI Inject";

    @Inject
    private Instance<Object> instance;

    public CdiFxSaft() {
        super(new LocationStorage(), Executors.newCachedThreadPool());
    }

    @PostConstruct
    private void postInit() {
        init(new Fx(this, p -> instance.select(p).get()));
    }

}
