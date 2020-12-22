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
import java.awt.Window;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.swing.JFrame;

import javafx.scene.Parent;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.impl.Swing;
import eu.ggnet.saft.core.ui.LocationStorage;

/**
 * Saft in global mode.
 *
 * @author oliver.guenther
 */
public class UiCore {

    private final static Logger L = LoggerFactory.getLogger(UiCore.class);

    private static Saft saft;

    /**
     * Initialise the global saft with the supplied saft.
     * Transition method. If Saft is created by someone else, e.g. CDI, but there exists only one, it can be used to initialise the UiCore.
     *
     * @param newSaft the saft, must not be null.
     * @throws NullPointerException     if newSaft is null.
     * @throws IllegalArgumentException if saft was allready initiated.
     */
    public static void initGlobal(Saft newSaft) throws NullPointerException, IllegalArgumentException {
        if ( saft != null ) throw new IllegalStateException("UiCore.global() already inited, call to initGlobal(Saft) not allowed.");
        L.info("Initialising Saft in classic mode using explizit instance: {}.", newSaft);
        saft = Objects.requireNonNull(newSaft, "Saft must not be null");
    }

    /**
     * Returns the one global saft instance.
     * If global saft is not initiated yet, it will be done with default values.
     *
     * @return the global saft.
     */
    public static Saft global() {
        if ( saft == null ) {
            L.info("Initialising Saft in classic mode using defaults.");
            // init defaults.
            saft = new Saft(new LocationStorage(), Executors.newCachedThreadPool(new ThreadFactory() {

                private final ThreadGroup group = new ThreadGroup("saft-uicore-pool");

                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(group, r, "Thread-" + counter.incrementAndGet() + "-" + r.toString());
                }
            }));
        }
        return saft;
    }

    /**
     * Returns the mainFrame in swing mode, otherwise null.
     *
     * @return the mainFrame in swing mode, otherwise null
     * @deprecated will be remove later, should not be needed, but is used in dwoss on multiple old places. Use saft.core(Swing.class).unwrapMain().get();
     */
    @Deprecated
    public static Window getMainFrame() {
        return global().core(Swing.class).unwrapMain().get();
    }

    /**
     * Returns the global executor.
     *
     * @return the global executor.
     * @deprecated {@link Saft#executorService() }.
     */
    @Deprecated
    public static Executor getExecutor() {
        return saft.executorService();
    }

    /**
     * Shortcut for UiCore.global().init(new Swing(UiCore.global(), mainView)) and global().core().captureMode(true).
     *
     * @param mainView the mainView to continue on.
     */
    public static void continueSwing(JFrame mainView) {
        global().init(new Swing(global(), mainView));
        global().core().captureMode(true);
    }

    /**
     * Old Startup of Swing, must be run in the EventQueue thread.
     *
     * @param <T>     type parameter
     * @param builder the builder for swing.
     * @deprecated use continueSwing(UiUtil.startup(builder)); or even better UiCore.global().init(new Swing(UiCore.global(), UiUtil.startup(builder)));
     */
    @Deprecated
    public static <T extends Component> void startSwing(final Supplier<T> builder) {
        continueSwing(UiUtil.startup(builder));
    }

    /**
     * Shortcut for UiCore.global().init(new Fx(UiCore.global(), primaryStage)) and global().core().captureMode(true).
     *
     * @param primaryStage the primaryStage for the application, not yet visible.
     */
    public static void continueJavaFx(final Stage primaryStage) {
        global().init(new Fx(global(), primaryStage));
        global().core().captureMode(true);
    }

    /**
     * Old Startup of Java FX, must be run in the Platform thread.
     *
     * @param <T>          type restriction.
     * @param primaryStage the primaryStage for the application, not yet visible.
     * @param builder      the build for the main ui.
     * @deprecated Use continueJavaFx(UiUtil.startup(primaryStage, builder)); or better UiCore.global().init(new Fx(UiCore.global(),
     * UiUtil.startup(primaryStage, builder)));
     */
    @Deprecated
    public static <T extends Parent> void startJavaFx(final Stage primaryStage, final Supplier<T> builder) {
        continueJavaFx(UiUtil.startup(primaryStage, builder));
    }

}
