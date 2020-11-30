/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter15;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import eu.ggnet.saft.core.ui.Bind;

import static eu.ggnet.saft.core.ui.Bind.Type.SHOWING;
import static javafx.scene.text.Font.font;

/**
 *
 * @author oliver.guenther
 */
public class FxWindow extends BorderPane {

    @Bind(SHOWING)
    private BooleanProperty showing = new SimpleBooleanProperty();

    @Inject
    @Value
    private String value;

    private Label label;

    public FxWindow() {
        label = new Label("");
        label.setFont(font(50));
        setCenter(label);

        Button close = new Button("Close");
        close.setOnAction(t -> showing.set(false));
        setBottom(close);

    }

    @PostConstruct
    private void init() {
        label.setText(value);
    }

}
