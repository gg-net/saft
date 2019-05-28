/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon;

import com.gluonhq.charm.glisten.control.Dialog;
import eu.ggnet.saft.core.ui.AlertType;
import eu.ggnet.saft.core.ui.FxSaft;
import eu.ggnet.saft.core.ui.builder.GluonSupport;
import javafx.scene.control.Button;

/**
 *
 * @author oliver.guenther
 */
// TODO: Verify, if we move everything to saft-gluon, if this works. Especaly on Android.
 // @ServiceProvider(service = GluonSupport.class)
public class GluonSupportService implements GluonSupport {

    @Override
    public void showAlert(String title, String message, AlertType type) {
        FxSaft.dispatch(() -> {
            Dialog<Void> d = new Dialog<>(title, message);
            // TODO: Make an Icon for the type.
            Button close = new Button("Schließen");
            close.setOnAction(e -> d.hide());
            d.getButtons().add(close);
            return d.showAndWait();
        });
        
    }
    
}
