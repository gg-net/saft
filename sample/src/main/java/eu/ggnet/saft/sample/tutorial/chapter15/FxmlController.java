/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter15;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.FxController;

/**
 *
 * @author oliver.guenther
 */
public class FxmlController implements FxController {

    @FXML
    private Label outputLabel;

    @FXML
    private Button closeButton;

    @Inject
    private Saft saft;

    @Inject
    @Value
    private String value;

    @FXML
    void initialize() {
        outputLabel.setText(value);
        closeButton.setOnAction(e -> saft.closeWindowOf(closeButton));
    }
}
