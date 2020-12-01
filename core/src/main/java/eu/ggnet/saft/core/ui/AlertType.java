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

import javax.swing.JOptionPane;

import javafx.scene.control.Alert;

/**
 * Neutral type of an alert.
 * Type can be mapped to {@link JOptionPane} or {@link javafx.scene.control.Alert.AlertType}.
 *
 * @author oliver.guenther
 */
public enum AlertType {

    /**
     * Represents an informational type.
     */
    INFO(JOptionPane.INFORMATION_MESSAGE, javafx.scene.control.Alert.AlertType.INFORMATION),
    /**
     * Represents a warning type.
     */
    WARNING(JOptionPane.WARNING_MESSAGE, javafx.scene.control.Alert.AlertType.WARNING),
    /**
     * Represents a error type
     */
    ERROR(JOptionPane.ERROR_MESSAGE, javafx.scene.control.Alert.AlertType.ERROR);

    private AlertType(int optionPaneType, Alert.AlertType javaFxType) {
        this.optionPaneType = optionPaneType;
        this.javaFxType = javaFxType;
    }

    private final int optionPaneType;

    private final javafx.scene.control.Alert.AlertType javaFxType;

    /**
     * Returns type mapping for {@link JOptionPane}.
     *
     * @return type mapping for {@link JOptionPane}
     */
    public int getOptionPaneType() {
        return optionPaneType;
    }

    /**
     * Returns type mapping for {@link javafx.scene.control.Alert.AlertType}.
     *
     * @return type mapping for {@link javafx.scene.control.Alert.AlertType}
     */
    public Alert.AlertType getJavaFxType() {
        return javaFxType;
    }

}
