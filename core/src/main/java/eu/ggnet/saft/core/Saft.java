/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core;

import java.util.Objects;

import eu.ggnet.saft.core.ui.LocationStorage;

/**
 * The core of saft, everything that is keept in a singleton way, is registered or held here.
 * No other statics should exist.
 *
 * @author oliver.guenther
 */
public class Saft {

    private final LocationStorage locationStorage;

    /**
     * Default Constructor, ready for own implementations.
     * To ensure that no one will make an instance of Saft by error, the constructor is package private.
     * In the classic mode, use {@link UiCore#initGlobal()} and {@link UiCore#global() }.
     * <p>
     * If more that one instance is needed (using multiple cdi container in one vm for example) extend Saft.
     * For transition purposes the {@link UiCore#initGlobal(eu.ggnet.saft.core.Saft) } is designed.
     * </p>
     */
    Saft(LocationStorage locationStorage) {
        this.locationStorage = Objects.requireNonNull(locationStorage, "LocationStorage must not be null");
    }

    /**
     * Returns the location storage.
     *
     * @return the location storage.
     */
    public LocationStorage locationStorage() {
        return locationStorage;
    }

}
