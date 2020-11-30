/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter04;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.Bind;

import static eu.ggnet.saft.core.ui.Bind.Type.SHOWING;
import static javafx.scene.text.Font.font;

/**
 *
 * @author oliver.guenther
 */
public class WindowWithCloseBinding extends BorderPane {

    @Bind(SHOWING)
    private BooleanProperty showing = new SimpleBooleanProperty();

    private final Logger log = LoggerFactory.getLogger(WindowWithCloseBinding.class);

    public WindowWithCloseBinding() {
        Label l = new Label("Window with close Binding");
        l.setFont(font(50));
        setCenter(l);

        showing.addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
            log.info("changed() showing={}", newValue);
        });

        Button close = new Button("Close");
        close.setOnAction(t -> showing.set(false));
        setBottom(close);

    }

}
