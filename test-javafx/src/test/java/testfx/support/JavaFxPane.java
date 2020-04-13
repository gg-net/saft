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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.Bind;

import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

/**
 *
 * @author oliver.guenther
 */
public class JavaFxPane extends BorderPane {

    public final static String LABEL_ID = "javafxpane-label";

    public final static String LABEL_TEXT = "This is a simple javafx pane with id: " + LABEL_ID;

    public final static String TITLE_TEXT_FIELD_ID = "javafxpane-title-textfield";

    public final static String CLOSE_BUTTON_ID = "javafxpane-close";

    @Bind(TITLE)
    public final StringProperty titleProperty = new SimpleStringProperty();

    public JavaFxPane() {
        Label label = new Label(LABEL_TEXT);
        label.setId(LABEL_ID);
        setTop(label);
        setLeft(new Label("Title"));
        TextField titleTextField = new TextField("Der Title");
        titleTextField.setId(TITLE_TEXT_FIELD_ID);
        titleProperty.bind(titleTextField.textProperty());
        setCenter(titleTextField);
        Button close = new Button("Close");
        close.setId(CLOSE_BUTTON_ID);
        close.setOnAction(e -> Ui.closeWindowOf(this));
        setBottom(close);
    }

}
