/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.title;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import eu.ggnet.saft.core.ui.Bind;

import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

/**
 *
 * @author oliver.guenther
 */
public class TitlePane extends FlowPane {

    @Bind(TITLE)
    private final StringProperty titleProperty;

    public TitlePane() {
        titleProperty = new SimpleStringProperty();
        TextField titleField = new TextField("The Title Pane");
        titleProperty.bind(titleField.textProperty());
        Label l = new Label("Title: ");
        getChildren().addAll(l, titleField);
    }

}
