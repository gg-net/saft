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

/**
 * Used to indicate, that a Workflowbeak should happen, should never been ignored in the CompletableFuture.handle() as its expected behavior.
 *
 * @author oliver.guenther
 */
public class UiWorkflowBreak extends RuntimeException {

    public static enum Type {
        ONCE, NULL_RESULT
    }

    private final Type type;

    public UiWorkflowBreak(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "UiWorkflowBreak{" + "type=" + type + '}';
    }

}
