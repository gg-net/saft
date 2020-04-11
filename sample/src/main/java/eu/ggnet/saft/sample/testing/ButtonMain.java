/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.testing;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.TitleSupplier;

/**
 *
 * @author oliver.guenther
 */
public class ButtonMain extends FlowPane implements TitleSupplier {

    public ButtonMain() {
        Button fxmlButton = new Button("Fxml");
        fxmlButton.setOnAction(e -> {
            Ui.build(fxmlButton).fxml().show(ButtonController.class);
        });
        getChildren().add(fxmlButton);
    }

    @Override
    public StringProperty titleProperty() {
        return new SimpleStringProperty("Button Main");
    }

}
