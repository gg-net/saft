/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.cdi.swing;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.LocationStorage;

/**
 *
 * @author oliver.guenther
 */
@ApplicationScoped
public class CdiSaft extends Saft {

    @Inject
    private Instance<Object> instance;

    public CdiSaft() {
        super(new LocationStorage(), Executors.newCachedThreadPool(new ThreadFactory() {

            private final ThreadGroup group = new ThreadGroup("saft-cdi-pool");

            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(group, r, "Thread-" + counter.incrementAndGet() + "-" + r.toString());
            }
        }));
        LoggerFactory.getLogger(CdiSaft.class).info("init<>");
    }

}
