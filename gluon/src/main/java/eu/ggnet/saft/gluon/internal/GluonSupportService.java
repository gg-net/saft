/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.internal;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import eu.ggnet.saft.core.ui.AlertType;
import eu.ggnet.saft.core.ui.FxSaft;
import eu.ggnet.saft.core.ui.builder.GluonSupport;
import eu.ggnet.saft.core.ui.builder.UiParameter;

import com.gluonhq.charm.glisten.control.Dialog;

/**
 * Implementation fo the Support Service.
 * 
 * @author oliver.guenther
 */
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

    @Override
    public UiParameter constructJavaFx(UiParameter in) {
        if ( !Platform.isFxApplicationThread() ) throw new IllegalStateException("construnctJavaFx called, but not from the ui thread, disallowed");
        Pane pane = in.pane().get();
        Dialog<Void> d = new Dialog<>();
        d.setTitleText(in.toTitle());
        d.setContent(pane);
        d.showAndWait();
        return in;
    }

    @Override
    public void closeViewOrDialogOf(Node n) {
        // find either the view or the dialog the node is wrapped in.
        
        
    }
    
}
