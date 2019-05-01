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
package testfx.support;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.Title;


/**
 *
 * @author oliver.guenther
 */
@Title(JavaFxPane.LABEL_ID)
public class JavaFxPane extends BorderPane {

    public final static String LABEL_ID = "javafxpane-label";
    
    public final static String LABEL_TEXT = "This is a simple javafx pane with id: " + LABEL_ID;
    
    public final static String CLOSE_BUTTON_ID = "javafxpane-close";
    
    public JavaFxPane() {
        Label label = new Label(LABEL_TEXT);
        label.setId(LABEL_ID);
        setCenter(label);
        Button close = new Button("Close");
        close.setId(CLOSE_BUTTON_ID);
        close.setOnAction(e -> Ui.closeWindowOf(this));
        setBottom(close);
    }

}
