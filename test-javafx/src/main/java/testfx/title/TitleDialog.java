/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.title;

import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

/**
 *
 * @author oliver.guenther
 */
public class TitleDialog extends Dialog<ButtonType> {

    public TitleDialog() {
        TextField titleField = new TextField("The Title Pane");
        titleProperty().bind(titleField.textProperty());
        Label l = new Label("Title: ");

        getDialogPane().setGraphic(new FlowPane(l, titleField));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }

}
