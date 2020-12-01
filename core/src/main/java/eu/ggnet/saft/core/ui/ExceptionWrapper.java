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
package eu.ggnet.saft.core.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;

import eu.ggnet.saft.core.UiUtil;

/**
 * Helper class to wrap exeption into in a {@link CompletionException}.
 *
 * @author oliver.guenther
 * @deprecated use {@link UiUtil}
 */
@Deprecated
public class ExceptionWrapper {

    /**
     * A Runable implementation, that allows throwing an Exception.
     *
     * @author oliver.guenther
     */
    @Deprecated
    public static interface RunableWithException {

        /**
         * The run method.
         *
         * @throws Exception possible exception while running
         */
        void run() throws Exception;

    }

    private static ExceptionWrapper instance;

    /**
     * Returns a singleton instance of this class.
     * The util character of the class, makes the singleton pattern superfluous. But to use the class in the Ui via a method, we need an instance.
     *
     * @return the single instance of this class.
     */
    @Deprecated
    public static ExceptionWrapper getInstance() {
        if ( instance == null ) instance = new ExceptionWrapper();
        return instance;
    }

    /**
     * Wrappes a possible throw exception of the callable in a {@link CompletionException}.
     *
     * @param <T>      the type
     * @param callable the callable
     * @return the result of the callable
     * @throws CompletionException the exception thrown by the callable wrapped.
     * @deprecated use {@link UiUtil#exceptionRun(java.util.concurrent.Callable) }
     */
    @Deprecated
    public <T> T wrap(Callable<T> callable) throws CompletionException {
        try {
            return callable.call();
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    /**
     * @deprecated use {@link UiUtil#exceptionRun(eu.ggnet.saft.core.UiUtil.ExceptionRunnable) }
     *
     * @param runable the runable
     * @deprecated
     */
    @Deprecated
    public void wrap(RunableWithException runable) {
        wrap(() -> {
            runable.run();
            return null;
        });
    }
}
