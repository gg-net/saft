/*
 * Copyright (C) 2018 GG-Net GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.ggnet.saft.core;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.FxSaft;

import static eu.ggnet.saft.core.ui.FxSaft.loadView;

/**
 * Static utility classes for global useage.
 *
 * @author oliver.guenther
 */
public class UiUtil {

    private final static Logger L = LoggerFactory.getLogger(UiUtil.class);

    /**
     * Constructs (loads) an FXML and controller pair, finding all elements base on the class and calling load, so direct calls to getRoot() or getController()
     * are possible.
     * Resources are discovered as described in {@link FxSaft#loadView(java.lang.Class) }.
     *
     * @param <T>             type parameter
     * @param <R>             type parameter
     * @param controllerClazz the controller class.
     * @return a loaded loader.
     * @throws IllegalArgumentException see {@link FxSaft#loadView(java.lang.Class) }
     * @throws IllegalStateException    see {@link FxSaft#loadView(java.lang.Class) }
     * @throws NullPointerException     see {@link FxSaft#loadView(java.lang.Class) }
     * @throws RuntimeException         wrapped IOException of {@link FXMLLoader#load() }.
     */
    public static <T, R extends FxController> FXMLLoader constructFxml(Class<R> controllerClazz) throws IllegalArgumentException, NullPointerException, IllegalStateException, RuntimeException {
        if ( !Platform.isFxApplicationThread() )
            throw new IllegalStateException("Method constructFxml is not called from the JavaFx Ui Thread, illegal (e.g. construct of WebView fails on other threads)");
        FXMLLoader loader = new FXMLLoader(loadView(controllerClazz));
        try {
            loader.load();
            return loader;
        } catch (IOException ex) {
            L.error("Exeption while loading fxml", ex);
            throw new RuntimeException(ex);
        }
    }

}
