/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.cdi.fx;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.ui.LocationStorage;

/**
 *
 * @author oliver.guenther
 */
@ApplicationScoped
public class CdiFxSaft extends Saft {

    private final static Logger L = LoggerFactory.getLogger(CdiFxSaft.class);

    @Inject
    private Instance<Object> instance;

    public CdiFxSaft() {
        super(new LocationStorage(), Executors.newCachedThreadPool(new ThreadFactory() {

            private final ThreadGroup group = new ThreadGroup("saft-cdi-pool");

            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(group, r, "Thread-" + counter.incrementAndGet() + "-" + r.toString());
            }
        }));
        L.info("init<>");
    }

    @PostConstruct
    private void postInit() {
        init(new Fx(this, (Class<?> param) -> {
            L.debug("initializing via cid: {} ", param.getName());
            return instance.select(param).get();
        }));
    }

}
