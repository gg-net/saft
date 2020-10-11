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
package eu.ggnet.saft.core.ui;

import java.awt.Component;
import java.awt.Window;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Parent, that can hold either a swingOrMain or a javafx parent.
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
        Objects.requireNonNull(swingParent);
        return new UiParent(swingParent, null);
    }

    /**
     * Returns a new wrapped parrent of a javafx parent.
     *
     * @param javafxElement a javafxparent, must not be null.
     * @return a new wrapped parrent of a javafx parent
     */
    public static UiParent of(Node javafxElement) {
        Objects.requireNonNull(javafxElement);
        return new UiParent(null, javafxElement);
    }

    /**
     * The wrapped node or empty.
     *
     * @return
     */
    public Optional<Node> node() {
        return Optional.ofNullable(javafxElement);
    }

    public Optional<Component> component() {
        return Optional.ofNullable(swingParent);
    }

    /**
     * Multipatform consumer.
     *
     * @param swingConsumer  consumer, called only in swing mode and if a swing parent is not null.
     * @param javaFxConsumer consuer, called only in fx mode and if a fx parent is not null.
     */
    // TODO: This must be moved into saft.
    public void ifPresent(Consumer<Window> swingConsumer, Consumer<Stage> javaFxConsumer) {

//        final Logger L = LoggerFactory.getLogger(UiParent.class);
//        if ( UiCore.isSwing() && swing() != null ) {
//            L.debug("ifPresent() UiCore.isSwing()=true and swing() is set with {}", swing());
//            swingConsumer.accept(swing());
//        } else if ( UiCore.isFx() && fx() != null ) {
//            L.debug("ifPresent() UiCore.isFx()=true and fx() is set with {}", fx());
//            javaFxConsumer.accept(fx());
//        } else {
//            L.debug("ifPresent() UiCore.isSwing()={}, UiCore.isFx()={}, neither fx() nor swing() is set", UiCore.isSwing(), UiCore.isFx());
//        }
    }

}
