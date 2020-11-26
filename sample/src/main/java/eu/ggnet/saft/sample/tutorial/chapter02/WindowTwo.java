/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter02;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import static javafx.scene.text.Font.font;

/**
 *
 * @author oliver.guenther
 */
public class WindowTwo extends BorderPane {

    public WindowTwo() {
        Label l = new Label("WindowTwo");
        l.setFont(font(50));
        setCenter(l);
    }

}
