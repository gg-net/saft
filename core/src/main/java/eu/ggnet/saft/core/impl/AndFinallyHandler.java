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
package eu.ggnet.saft.core.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiUtil.ExceptionRunnable;
import eu.ggnet.saft.core.ui.UiParent;

/**
 * A implementation of the BiFunction used in {@link CompletableFuture#handle(java.util.function.BiFunction) }, which allows a extra call to a final Runnable.
 * An example usage scenarion is the stopping of some progress visualisation in case of an exception or cancelation.
 *
 * @author oliver.guenther
 */
public class AndFinallyHandler<Z> implements BiFunction<Z, Throwable, Z> {

    private final Saft saft;

    private final UiParent parent;

    /**
     * Creates a Handler.
     *
     * @param saft      the saft to use, must not be null.
     * @param optParent an optional parent, must not be null.
     * @throws NullPointerException if parent or saft is null.
     */
    public AndFinallyHandler(Saft saft, Optional<UiParent> optParent) throws NullPointerException {
        this.saft = Objects.requireNonNull(saft, "Saft must not be null");
        this.parent = Objects.requireNonNull(optParent, "optParent must not be null").orElse(null);
    }

    /**
     * Handels the supplied exception or returns the input value.
     *
     * @param in        input value.
     * @param exception if not null, is handled by {@link Saft#handle(java.util.Optional, java.lang.Throwable) } and a {@link CancellationException} is thrown.
     * @return the input value, if exception was null.
     */
    @Override
    public Z apply(Z in, Throwable exception) {
        if ( exception != null ) {
            saft.handle(Optional.ofNullable(parent), exception);
            throw new CancellationException("Exception " + exception.getClass().getSimpleName() + " allready handeld, canceling everything else");
        } else {
            return in;
        }
    }

    /**
     * Creates a new Handler combining the original Handler and the supplied runnable.
     *
     * @param runnable the runnable to be run on a final condition.
     * @return a new Handler.
     */
    public BiFunction<Z, Throwable, Z> andFinally(ExceptionRunnable runnable) {
        return (Z in, Throwable exception) -> {
            if ( exception != null ) {
                try {
                    runnable.run();
                } catch (Exception internalException) {
                    LoggerFactory.getLogger(AndFinallyHandler.class).warn("{}.andFinally(): internal exception {} with message {}",
                            AndFinallyHandler.class.getSimpleName(), internalException.getClass().getName(), internalException.getMessage());
                } finally {
                    saft.handle(Optional.ofNullable(parent), exception);
                }
                throw new CancellationException("Exception " + exception.getClass().getSimpleName() + " allready handeld, canceling everything else");
            } else {
                return in;
            }
        };

    }

    /**
     * Creates a new Handler combining the original Handler and the supplied runnable.
     *
     * @param runnable the runnable to be run on a final condition.
     * @return a new Handler.
     */
    public BiFunction<Z, Throwable, Z> andFinally(Runnable runnable) {
        return andFinally((ExceptionRunnable)() -> runnable.run());
    }

}
