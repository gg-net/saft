/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core;

import java.util.Objects;
import java.util.Optional;

import eu.ggnet.saft.core.ui.LocationStorage;
import eu.ggnet.saft.core.ui.builder.GluonSupport;

/**
 * The core of saft, everything that is keept in a singleton way, is registered or held here.
 * No other statics should exist.
 *
 * @author oliver.guenther
 */
public class Saft {

    private final LocationStorage locationStorage;

    private Optional<GluonSupport> gluonSupport = Optional.empty();

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

    /**
     * Returns a gluon support if gluon is enabled.
     *
     * @return a gluon support if gluon is enabled.
     */
    public Optional<GluonSupport> gluonSupport() {
        return gluonSupport;
    }

    /**
     * Setting the gluon support.
     * By setting the gluon support, gluon is enabled in saft.
     *
     * @param gluonSupport the gluon support.
     */
    public void gluonSupport(GluonSupport gluonSupport) {
        this.gluonSupport = Optional.ofNullable(gluonSupport);
    }

}
