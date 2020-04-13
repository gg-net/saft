/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.title;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

/**
 *
 * @author oliver.guenther
 */
public class TitleDialog extends Dialog<ReadOnlyBooleanProperty> {

    public TitleDialog() {
        TextField titleField = new TextField("The Title Pane");
        titleProperty().bind(titleField.textProperty());
        Label l = new Label("Title: ");

        Button extraClose = new Button("Extra Close");
        extraClose.setOnAction(e -> close());

        Button show = new Button("Show ShowingProperty");
        show.setOnAction(e -> System.out.println("PostRun: Dialog.showingProperty().get()=" + showingProperty().get()));

        getDialogPane().setGraphic(new FlowPane(l, titleField, extraClose, show));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter((ButtonType param) -> showingProperty());
    }

}
