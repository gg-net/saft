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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.AlertType;
import eu.ggnet.saft.core.ui.UiParent;

import static eu.ggnet.saft.core.ui.AlertType.INFO;

/**
 * Saft Alert Builder.
 *
 * @author oliver.guenther
 */
public class AlertBuilder {

    private final static Logger L = LoggerFactory.getLogger(AlertBuilder.class);

    /**
     * The title.
     */
    private String title = null;

    /**
     * The body or message.
     */
    private String message;

    private final UiParent parent;

    private final Saft saft;

    /**
     * Create Builder.
     *
     * @param pre the prebuidler, must not be null.
     */
    public AlertBuilder(PreBuilder pre) {
        parent = pre.uiParent().orElse(null);
        saft = pre.saft();
        pre.title().ifPresent(v -> title = v);
    }

    /**
     * Fluen title setter.
     *
     * @param title the title to be set
     * @return this alert builder for future fluent usage.
     */
    public AlertBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Fluent message setter.
     *
     * @param message the message
     * @return this alert builder for future fluent usage.
     */
    public AlertBuilder message(String message) {
        this.message = message;
        return this;
    }

    /**
     * Fluent message setter preprend by a new line.
     *
     * @param message the message to append
     * @return this alert builder for future fluent usage.
     */
    public AlertBuilder nl(String message) {
        this.message += "\n" + message;
        return this;
    }

    /**
     * Fluent new line setter.
     *
     * @return this alert builder for future fluent usage.
     */
    public AlertBuilder nl() {
        this.message += "\n";
        return this;
    }

    /**
     * Shows the alert of type info.
     */
    public void show() {
        show(INFO);
    }

    /**
     * Shows the alert.
     *
     * @param type the type of the alert.
     */
    public void show(AlertType type) {
        saft.core().showAlert(message, Optional.ofNullable(parent), Optional.ofNullable(title), Optional.ofNullable(type));
    }

    @Override
    public String toString() {
        return "AlertBuilder{" + "title=" + title + ", message=" + message + ", parent=" + parent + '}';
    }

}
