/*
 * Swing and JavaFx Together (Saft)
 * Copyright (C) 2020  Oliver Guenther <oliver.guenther@gg-net.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 with
 * Classpath Exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with Classpath Exception along with this program.
 */
package eu.ggnet.saft.core.ui.builder;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.Ui;

/**
 * Combined result handler for all eval methodes of the builders.
 * Result may be continued to a blocking {@link Optional} or a non-blocking {@link CompletableFuture}.
 *
 * @author oliver.guenther
 */
public class Result<T> {

    private final static Logger L = LoggerFactory.getLogger(Result.class);

    private CompletableFuture<T> cf = null;

    private Saft saft;

    /**
     * Creates a Result.
     *
     * @param saft the relevant saft.
     * @param cf   the sourceö
     */
    public Result(Saft saft, CompletableFuture<T> cf) {
        this.cf = Objects.requireNonNull(cf);
    }

    /**
     * Returns the result as optional, waiting for the completion of all possible async activity.
     * This method is blocking until a result is available or an exception happens. Make sure to
     * put this on a non ui thread. In global mode {@link Ui#exec(java.util.concurrent.Callable) } can be used.
     *
     * @return the result as optional, waiting for the completion of all possible async activity.
     */
    public Optional<T> opt() {
        try {
            return Optional.of(cf.get());
        } catch (InterruptedException ex) {
            saft.handle(ex);
        } catch (CancellationException ex) {
            L.debug("Cancelation({}), retruning empty", ex.getMessage());
            return Optional.empty();
        } catch (ExecutionException ex) {
            if ( ex.getCause() instanceof CancellationException ) {
                L.debug("Cancelation({}), retruning empty", ex.getMessage());
                return Optional.empty();
            }
            saft.handle(ex);
        }
        L.error("Impposible End, returning empty");
        return Optional.empty();
    }

    /**
     * Returns a CompletableFuture for further async usage.
     * The actual implementation contains either the value of the previous procession or throws a {@link CancellationException} if canceled.
     *
     * @return a CompletableFuture
     */
    public CompletableFuture<T> cf() {
        return cf;
    }

}
