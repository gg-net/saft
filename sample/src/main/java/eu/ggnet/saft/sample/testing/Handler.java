/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.testing;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author oliver.guenther
 */
public class Handler {

    private final BooleanProperty showingProperty = new SimpleBooleanProperty();

    public BooleanProperty showingProperty() {
        return showingProperty;
    }

}
