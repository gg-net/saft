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

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Binding properties to the outer container, Swing JFrame or JDialog, JavaFx Stage, or else.
 *
 * @author oliver.guenther
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Bind {

    enum Type {
        /**
         * Binding to the title of the container, must be of type {@link javafx.beans.property.StringProperty}.
         * <p>
         * Binds to or registers a {@link javafx.beans.value.ChangeListener} :
         * </p>
         * <ul>
         * <li>JavaFx: {@link javafx.stage.Stage#titleProperty() }</li>
         * <li>Swing: {@link javax.swing.JFrame#setTitle(java.lang.String) } or {@link javax.swing.JDialog#setTitle(java.lang.String) }</li>
         * </ul>
         */
        TITLE("javafx.beans.property.StringProperty"),
        /**
         * Binding to the showing, visible, closing state of the container, must be of type {@link javafx.beans.property.BooleanProperty}.
         * The showing binding is special, cause it combines the showing status, but also allows the closing of the surounding container.
         * <p>
         * Binds to or registers a {@link javafx.beans.value.ChangeListener} :
         * </p>
         * <ul>
         * <li>JavaFx: {@link javafx.stage.Stage#close() } and {@link javafx.stage.Stage#showingProperty() }</li>
         * <li>Swing: {@link javax.swing.JFrame#setVisible(boolean) } or {@link javax.swing.JDialog#setVisible(boolean) }</li>
         * </ul>
         */
        SHOWING("javafx.beans.property.BooleanProperty");

        // If using the class token, the annotation processor fails in netbeans 11.x with jdk8.
        private final String allowedClassName;

        private Type(String allowedClassName) {
            this.allowedClassName = allowedClassName;
        }

        /**
         * Returns the class name that this mapping allows.
         *
         * @return the class name that this mapping allows.
         */
        public String allowedClassName() {
            return allowedClassName;
        }

    }

    /**
     * The type of the Binding.
     *
     * @return the type.
     */
    Type value();

}
