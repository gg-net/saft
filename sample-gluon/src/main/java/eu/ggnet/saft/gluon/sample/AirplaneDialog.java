/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.sample;

import com.gluonhq.charm.glisten.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author oliver.guenther
 */
public class AirplaneDialog extends Dialog<String> {

    public AirplaneDialog() {

        setTitleText("Airplane Dialog");
        setContent(new Label("Simple Gluon Dialog, click Ok to liftof Ariplane"));
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> {
            setResult("Airbus A380");
            hide();
        });
        Button cancelButton = new Button("OK");
        cancelButton.setOnAction(e -> hide());
        getButtons().add(okButton);

    }

}
