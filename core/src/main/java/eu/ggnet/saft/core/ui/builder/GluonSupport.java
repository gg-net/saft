/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.ui.builder;

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
    public void showAlert(String title, String message, AlertType type);

}
