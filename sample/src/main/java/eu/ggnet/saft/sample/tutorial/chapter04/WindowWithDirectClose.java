/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter04;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import eu.ggnet.saft.core.Ui;

import static javafx.scene.text.Font.font;

/**
 *
 * @author oliver.guenther
 */
public class WindowWithDirectClose extends BorderPane {

    public WindowWithDirectClose() {
        Label l = new Label("Window with direct Close");
        l.setFont(font(50));
        setCenter(l);

        Button close = new Button("Close");
        close.setOnAction(t -> Ui.closeWindowOf(close));
        setBottom(close);
    }

}
