/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.internal;

/**
 * Gloun PreBuilder.
 * 
 * @author oliver.guenther
 */
public class GluonPreBuilder {
    
    /**
     * Returns the compile-safe gloun dialog builder.
     * 
     * @return the gloun dialog builder.
     */
    public GluonDialogBuilder gluonDialog() {
        return new GluonDialogBuilder();
    }
}
