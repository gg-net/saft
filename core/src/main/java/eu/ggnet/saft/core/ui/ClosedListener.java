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

import eu.ggnet.saft.core.ui.Bind.Type;

/**
 * Implement in Panel, Pane or FxController to get informed on closing.
 *
 * @author oliver.guenther
 * @deprecated Use the binding of {@link Type#SHOWING}
 */
@Deprecated
public interface ClosedListener {

    @Deprecated
    void closed();

}
