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
import java.util.Objects;
import java.util.Optional;

import javafx.scene.Node;

/**
 * Wrapper for parent identification.
 *
 * @author oliver.guenther
 */
public class UiParent {

    private final Component swingParent;

    private final Node javafxElement;

    private UiParent(Component swingParent, Node javafxElement) {
        this.swingParent = swingParent;
        this.javafxElement = javafxElement;
    }

    /**
     * Returns a new wrapped parrent of a swingOrMain component.
     *
     * @param swingParent the swingparent to be wrapped, must not be null.
     * @return a new wrapped parrent of a swingOrMain component.
     */
    public static UiParent of(Component swingParent) {
        Objects.requireNonNull(swingParent, "swingParent must not be null");
        return new UiParent(swingParent, null);
    }

    /**
     * Returns a new wrapped parrent of a javafx parent.
     *
     * @param javafxElement a javafxparent, must not be null.
     * @return a new wrapped parrent of a javafx parent
     */
    public static UiParent of(Node javafxElement) {
        Objects.requireNonNull(javafxElement, "javafxElement must not be null");
        return new UiParent(null, javafxElement);
    }

    /**
     * The wrapped node or empty.
     *
     * @return the wrapped node or empty.
     */
    public Optional<Node> node() {
        return Optional.ofNullable(javafxElement);
    }

    /**
     * The wrapped component or empty.
     *
     * @return wrapped component or empty.
     */
    public Optional<Component> component() {
        return Optional.ofNullable(swingParent);
    }

}
