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
package eu.ggnet.saft.core;

import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import javafx.scene.Node;
import javafx.scene.Parent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.ExceptionWrapper;
import eu.ggnet.saft.core.ui.builder.PreBuilder;

/**
 * Contains usefull shortcuts to {@link UiCore#global() }.
 *
 * @author oliver.guenther
 */
public class Ui {

    private final static Logger L = LoggerFactory.getLogger(Ui.class);

    /**
     * Static global version of {@link Saft#build() } via {@link UiCore#global() }
     *
     * @see Saft for direct usage.
     * @return the prebuilder.
     */
    public static PreBuilder build() {
        return UiCore.global().build();
    }

    /**
     * Static global version of {@link Saft#build() } via {@link UiCore#global() }
     *
     * @param parent an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @return the prebuilder.
     */
    public static PreBuilder build(Component parent) {
        return UiCore.global().build(parent);
    }

    /**
     * Static global version of {@link Saft#build() } via {@link UiCore#global() }
     *
     * @param parent an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @return the prebuilder.
     */
    public static PreBuilder build(Parent parent) {
        return UiCore.global().build(parent);
    }

    /**
     * Static global version of {@link ExecutorService#execute(java.lang.Runnable) } on {@link Saft#executorService() } on {@link UiCore#global() }
     * with exception handling via {@link Saft#handle(java.lang.Throwable) }.
     *
     * @param <V>      type of the callable result.
     * @param callable a callable for the background.
     */
    public static <V> void exec(Callable<V> callable) {
        UiCore.global().executorService().execute(() -> {
            try {
                callable.call();
            } catch (Exception e) {
                UiCore.global().handle(e);
            }
        });
    }

    /**
     * Static global version of {@link ExecutorService#execute(java.lang.Runnable) } on {@link Saft#executorService() } on {@link UiCore#global() }
     * with exception handling via {@link Saft#handle(java.lang.Throwable) }.
     *
     * @param runnable a runnable for the background.
     */
    public static void exec(Runnable runnable) {
        UiCore.global().executorService().execute(() -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                UiCore.global().handle(e);
            }
        });
    }

    /**
     * Allows the wrapping of Exeptions into {@link java.util.concurrent.CompletionException}.
     *
     * @return the ExceptionWrapper.
     * @deprecated use {@link UiUtil#exceptionRun(eu.ggnet.saft.core.UiUtil.ExceptionRunnable) }.
     */
    @Deprecated
    public static ExceptionWrapper exception() {
        return ExceptionWrapper.getInstance();
    }

    /**
     * See {@link Saft#closeWindowOf(java.awt.Component) }.
     *
     * @param c the component which is the closest to the window.
     */
    public static void closeWindowOf(Component c) {
        UiCore.global().closeWindowOf(c);
    }

    /**
     * See {@link Saft#closeWindowOf(javafx.scene.Node) }.
     *
     * @param n the node as refernece.
     */
    public static void closeWindowOf(Node n) {
        UiCore.global().closeWindowOf(n);
    }

    /**
     * See {@link Saft#handle(java.lang.Throwable) }.
     *
     * @param b the throwable to be handled.
     */
    public static void handle(Throwable b) {
        UiCore.global().handle(b);
    }

    /**
     * See {@link Saft#handler() }.
     *
     * @param <Z> type parameter
     * @return a handler.
     */
    public static <Z> BiFunction<Z, Throwable, Z> handler() {
        return UiCore.global().handler();
    }
}
