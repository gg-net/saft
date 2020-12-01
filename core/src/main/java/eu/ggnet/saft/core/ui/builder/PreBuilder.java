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

import java.awt.Component;
import java.util.Objects;
import java.util.Optional;

import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.stage.Modality;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.*;

import static eu.ggnet.saft.core.ui.UiParent.of;

/**
 * The Prebuilder, entrypoint for all individual builders.
 *
 * @author oliver.guenther
 */
public class PreBuilder {

    private final Saft saft;

    /**
     * Represents the parent of the ui element, optional.
     * The default is in the swingmode SwingCore.mainFrame();
     */
    private final UiParent uiParent;

    /**
     * An optional title. If no title is given, the classname is used.
     * Default = null
     */
    private final String title;// = null;

    /**
     * Enables the Frame mode, makeing the created window a first class element.
     * Default = false
     */
    private final boolean frame;// = false;

    /**
     * Optional value for the modality.
     * Default = null
     */
    private final Modality modality;// = null;

    /**
     * Creates a prebuilder.
     *
     * @param saft the saft, must not be null
     * @throws NullPointerException if saft is null.
     */
    public PreBuilder(Saft saft) throws NullPointerException {
        this(Objects.requireNonNull(saft, "saft must not be null"), null, null, null, false);
    }

    /**
     * Creates a prebuilder.
     *
     * @param saft     the saft, must not be null.
     * @param uiParent a uiparent, may be null.
     * @param title    a title, may be null.
     * @param modality the modality, may be null.
     * @param frame    frame mode
     * @throws NullPointerException if saft is null.s
     */
    private PreBuilder(Saft saft, UiParent uiParent, String title, Modality modality, boolean frame) throws NullPointerException {
        this.saft = saft;
        this.uiParent = uiParent;
        this.title = title;
        this.frame = frame;
        this.modality = modality;
    }

    public Saft saft() {
        return saft;
    }

    public Optional<UiParent> parent() {
        return Optional.ofNullable(uiParent);
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    public boolean frame() {
        return frame;
    }

    public Optional<Modality> modality() {
        return Optional.ofNullable(modality);
    }

    public Optional<UiParent> uiParent() {
        return Optional.ofNullable(uiParent);
    }

    /**
     * Fluent setter of title.
     * The title can also be set vie {@link Title} Annotation or via {@link Bind}ing of a {@link StringProperty}.
     *
     * @param title the title;
     * @return new Prebuilder instance for fluent usage
     */
    public PreBuilder title(String title) {
        if ( title != null && title.trim().isEmpty() ) return new PreBuilder(saft, uiParent, null, modality, frame);
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Fluent setter of frame mode.
     * The frame mode can also be set via {@link Frame} Annotation.
     *
     * @param frame if true frame, frame mode is activated.
     * @return new Prebuilder instance for fluent usage
     */
    public PreBuilder frame(boolean frame) {
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Fluent setter of a uielement as reference to a prarent window.
     *
     * @param uiParent the parent
     * @return new Prebuilder instance for fluent usage
     */
    public PreBuilder parent(UiParent uiParent) {
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Fluent setter for the modality.
     *
     * @param modality the modality to use
     * @return new Prebuilder instance for fluent usage
     */
    public PreBuilder modality(Modality modality) {
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Fluent setter of a uielement as reference to a prarent window.
     *
     * @param swingParent the uielement
     * @return new Prebuilder instance for fluent usage
     */
    public PreBuilder parent(Component swingParent) {
        return parent(of(swingParent));
    }

    /**
     * Fluent setter of a uielement as reference to a prarent window.
     *
     * @param javaFxParent the element
     * @return new Prebuilder instance for fluent usage
     */
    public PreBuilder parent(Parent javaFxParent) {
        return parent(of(javaFxParent));
    }

    /**
     * Creates a SwingBuilder initialized with this PreBuilder.
     *
     * @return a new SwingBuilder
     */
    public SwingBuilder swing() {
        return new SwingBuilder(this);
    }

    /**
     * Creates a DialogBuilder initialized with this PreBuilder.
     *
     * @return a new DialogBuilder
     */
    public DialogBuilder dialog() {
        return new DialogBuilder(this);
    }

    /**
     * Creates a FxBuilder initialized with this PreBuilder.
     *
     * @return a new FxBuilder
     */
    public FxBuilder fx() {
        return new FxBuilder(this);
    }

    /**
     * Creates a FxmlBuilder initialized with this PreBuilder.
     *
     * @return a new FxmlBuilder
     */
    public FxmlBuilder fxml() {
        return new FxmlBuilder(this);
    }

    /**
     * Creates a AlertBuilder initialized with this PreBuilder.
     *
     * @return a new AlertBuilder
     */
    public AlertBuilder alert() {
        return new AlertBuilder(this);
    }

    /**
     * Shortcut for alert().message(xxxx).show().
     *
     * @param message the message to be shown
     */
    public void alert(String message) {
        new AlertBuilder(this).message(message).show();
    }
}
