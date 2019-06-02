/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.sample;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.FxController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 *
 * @author oliver.guenther
 */
public class InfoPresenter implements FxController {

    @FXML
    private Label clazzLabel;

    public void initialize() {
        clazzLabel.setText(this.getClass().getName());
    }

    @FXML
    void clickClose() {
        Ui.closeWindowOf(clazzLabel);
    }

}
