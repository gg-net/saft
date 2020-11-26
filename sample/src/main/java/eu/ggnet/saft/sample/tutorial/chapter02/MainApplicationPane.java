/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter02;

import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import static javafx.scene.text.Font.font;

/**
 *
 * @author oliver.guenther
 */
public class MainApplicationPane extends FlowPane {

    private final Button buttonOne;

    private final Button buttonTwo;

    public MainApplicationPane() {
        this.buttonOne = new Button("One");
        buttonOne.setFont(font(24));
        this.buttonTwo = new Button("Two");
        buttonTwo.setFont(font(24));
        getChildren().addAll(buttonOne, buttonTwo);
    }

    public Button getButtonOne() {
        return buttonOne;
    }

    public Button getButtonTwo() {
        return buttonTwo;
    }

}
