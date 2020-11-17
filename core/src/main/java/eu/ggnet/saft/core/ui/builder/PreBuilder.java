/*
 * Copyright (C) 2018 GG-Net GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.ggnet.saft.core.ui.builder;

import java.awt.Component;
import java.util.Objects;
import java.util.Optional;

import javafx.scene.Parent;
import javafx.stage.Modality;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.UiParent;

import static eu.ggnet.saft.core.ui.UiParent.of;

/**
 *
 * @author oliver.guenther
 */
public class PreBuilder {

    // TODO: later options for inject
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

    public PreBuilder(Saft saft) {
        this(Objects.requireNonNull(saft, "saft must not be null"), null, null, null, false);
    }

    private PreBuilder(Saft saft, UiParent uiParent, String title, Modality modality, boolean frame) {
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
     * An optional title. If no title is given, the classname is used.
     *
     * @param title the title;
     * @return this as fluent usage
     */
    public PreBuilder title(String title) {
        if ( title != null && title.trim().isEmpty() ) return new PreBuilder(saft, uiParent, null, modality, frame);
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Enables the Frame mode, makeing the created window a first class element.
     *
     * @param frame if true frame is assumed.
     * @return this as fluent usage
     */
    public PreBuilder frame(boolean frame) {
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Represents the parent of the ui element, optional.
     *
     * @param uiParent the parent
     * @return this as fluent usage
     */
    public PreBuilder parent(UiParent uiParent) {
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Optional value for the modality.
     *
     * @param modality the modality to use
     * @return this as fluent usage
     */
    public PreBuilder modality(Modality modality) {
        return new PreBuilder(saft, uiParent, title, modality, frame);
    }

    /**
     * Represents the parent of the ui element, optional.
     *
     * @param swingParent the parent
     * @return this as fluent usage
     */
    public PreBuilder parent(Component swingParent) {
        return parent(of(swingParent));
    }

    /**
     * Represents the parent of the ui element, optional.
     *
     * @param javaFxParent the parent
     * @return this as fluent usage
     */
    public PreBuilder parent(Parent javaFxParent) {
        return parent(of(javaFxParent));
    }

    /**
     * Initializes a new swingOrMain component handling.
     * The mode: swingOrMain is relevant for the component to be wrapped. The Wrapping Ui is set in the UiCore.
     *
     * @return a new swingOrMain builder
     */
    public SwingBuilder swing() {
        return new SwingBuilder(this);
    }

    /**
     * Initializes a new fxOrMain dialog component handling.
     * The mode: the fxOrMain dialog is relevant for the component to be wrapped. The Wrapping Ui is set in the UiCore.
     *
     * @return a new dialog builder
     */
    public DialogBuilder dialog() {
        return new DialogBuilder(this);
    }

    /**
     * Initializes a new fxOrMain component handling.
     * The mode: the fxs pane is relevant for the component to be wrapped. The Wrapping Ui is set in the UiCore.
     *
     * @return a new fxbuilder
     */
    public FxBuilder fx() {
        return new FxBuilder(this);
    }

    /**
     * Initializes a new fxOrMain component handling.
     * The mode: the fxOrMain pane is relevant for the component to be wrapped. The Wrapping Ui is set in the UiCore.
     *
     * @return a new fxbuilder
     */
    public FxmlBuilder fxml() {
        return new FxmlBuilder(this);
    }

    /**
     * Initializes a alert, like the swingOrMain JOptionPane or the javafx 8u60 Alert.
     *
     * @return a new Ui.build().alert()builder.
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
