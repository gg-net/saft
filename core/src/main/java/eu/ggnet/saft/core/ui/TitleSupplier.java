/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.ui;

import javafx.beans.property.StringProperty;

/**
 * Supplies a titleProperty to get controll of the title of the surounding window.
 *
 * @author oliver.guenther
 */
public interface TitleSupplier {

    /**
     * Returns a property, to be bound to the title of the surounding window.
     *
     * @return title property, never null.
     */
    StringProperty titleProperty();

}
