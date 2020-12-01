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

import java.awt.Component;
import java.util.Arrays;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.stage.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage for Location of windows and frames.
 * Default implementation uses the Preferences.userNode(). May be extended for different usage scenarios.
 *
 * @author oliver.guenther
 */
public class LocationStorage {

    public final static String WINDOW_X = "window.x";

    public final static String WINDOW_Y = "window.y";

    public final static String WINDOW_HEIGHT = "window.height";

    public final static String WINDOW_WIDTH = "window.width";

    public final static String ACTIVE_LAF = "activeLaf";

    private final static Logger L = LoggerFactory.getLogger(LocationStorage.class);

    /**
     * Clears all stored information, starting at the package level of the supplied key and all subpackages.
     *
     * @param key the key as reference to the lowest package, must not be null.
     */
    public void clearAll(Class<?> key) {
        Preferences p = Preferences.userNodeForPackage(key);
        try {
            p.clear();
            String[] names = p.childrenNames();
            for (String name : names) {
                p.node(name).removeNode();
            }
            p.flush();
            L.info("clearAll(key={}) cleared {} and removed subnodes: {}", key, Arrays.toString(names));
        } catch (BackingStoreException ex) {
            L.error("clearAll(key={}) cound not store Preferences", key, ex);
        }

    }

    /**
     * Stores the location of a component in the user preferences using the class as reference.
     *
     * @param key the key must not be null.
     * @param c   the component, must not be null.
     */
    public void storeLocation(Class<?> key, Component c) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(c, "Component must not be null");
        Preferences p = Preferences.userNodeForPackage(key).node(key.getSimpleName());
        p.putInt(WINDOW_X, c.getX());
        p.putInt(WINDOW_Y, c.getY());
        p.putInt(WINDOW_HEIGHT, c.getHeight());
        p.putInt(WINDOW_WIDTH, c.getWidth());
        try {
            p.flush();
            L.debug("storeLocation(key={}, component) successful", key);
        } catch (BackingStoreException ex) {
            L.error("Cound not store Preferences", ex);
        }
    }

    /**
     * Stores the location of a component in the user preferences using the class as reference.
     *
     * @param key the key must not be null
     * @param c   the component must not be null
     */
    public void storeLocation(Class<?> key, Window c) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(c, "Window must not be null");
        Preferences p = Preferences.userNodeForPackage(key).node(key.getSimpleName());
        p.putDouble(WINDOW_X, c.getX());
        p.putDouble(WINDOW_Y, c.getY());
        p.putDouble(WINDOW_HEIGHT, c.getHeight());
        p.putDouble(WINDOW_WIDTH, c.getWidth());
        try {
            p.flush();
            L.debug("storeLocation(key={}, Window) successful", key);
        } catch (BackingStoreException ex) {
            L.error("Cound not store Preferences", ex);
        }
    }

    /**
     * Loads and sets the location and size on the component if existing in the store.
     *
     * @param key the key must not be null
     * @param c   the window must not be null
     */
    public void loadLocation(Class<?> key, Component c) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(c, "Component must not be null");
        try {
            if ( !Preferences.userNodeForPackage(key).nodeExists(key.getSimpleName()) ) {
                L.debug("loadLocation(key={}, Component) no information in preferences found, ignoring", key);
                return;
            }
            Preferences p = Preferences.userNodeForPackage(key).node(key.getSimpleName());
            c.setLocation(p.getInt(WINDOW_X, 100), p.getInt(WINDOW_Y, 100));
            c.setSize(p.getInt(WINDOW_WIDTH, 200), p.getInt(WINDOW_HEIGHT, 200));
            L.debug("loadLocation(key={}, Component) successful", key);
        } catch (BackingStoreException ex) {
            L.error("Cound not load Preferences", ex);
        }
    }

    /**
     * Loads and sets the location and size on the component if existing in the store.
     *
     * @param key the key must not be null
     * @param c   the window must not be null
     */
    public void loadLocation(Class<?> key, Window c) {
        Objects.requireNonNull(key, "Key must not be null");
        Objects.requireNonNull(c, "Window must not be null");
        try {
            if ( !Preferences.userNodeForPackage(key).nodeExists(key.getSimpleName()) ) {
                L.debug("loadLocation(key={}, Window) no information in preferences found, ignoring", key);
                return;
            }
            Preferences p = Preferences.userNodeForPackage(key).node(key.getSimpleName());
            c.setX(p.getDouble(WINDOW_X, 100));
            c.setY(p.getDouble(WINDOW_Y, 100));
            c.setWidth(p.getDouble(WINDOW_WIDTH, 200));
            c.setHeight(p.getDouble(WINDOW_HEIGHT, 200));
            L.debug("loadLocation(key={}, Window) successful", key);
        } catch (BackingStoreException ex) {
            L.error("Cound not load Preferences", ex);
        }
    }

}
