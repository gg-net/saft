/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.ui.builder;

import javafx.scene.Node;

import eu.ggnet.saft.core.ui.AlertType;

/**
 * If we are running in GluonMode, there must be an implementation of this interface available via ServiceLookup.
 * 
 * @author oliver.guenther
 */
public interface GluonSupport {

    /**
     * Show an alert via a Gluon Dialog.
     * 
     * @param title the title, must not be null
     * @param message the message, must not be null,
     * @param type the type, must not be null
     */
    void showAlert(String title, String message, AlertType type);
    
    /**
     * Contruct a javafx element (UiParameter.pane()) in a Gloun Dialog.
     * 
     * @param in the in parameter
     * @return the dialog.
     */
    UiParameter constructJavaFx(UiParameter in);
    
    /**
     * Implementation should close the wrapping ui element of gluon, either a view or a dialog.
     * 
     * @param n the node.
     */
    void closeViewOrDialogOf(Node n);
}
