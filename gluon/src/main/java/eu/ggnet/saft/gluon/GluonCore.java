/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon;

import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 *
 * @author oliver.guenther
 */
public class GluonCore {

    /**
     * Contiunes the Ui in the Gloun mode with JavaFx.
     * This metod is intended to be used in the MobileApplication.postInit(Scene)
     *
     * @param <T>   type restriction.
     * @param scene the first and only scene of gluon.
     */
    // TODO: War in UiCore. nur hier als referenc, ggf, gar nicht mehr implementieren.
    public static <T extends Parent> void continueGluon(final Scene scene) {
//        if ( isRunning() ) throw new IllegalStateException("UiCore is already initialised and running");
//
//        try {
//            String clazzName = "eu.ggnet.saft.gluon.Gi";
//            String methodName = "startUp";
//            L.debug("continueGluon(): trying to start gluon specific code: reflective call to {}.{}", clazzName, methodName);
//            Class<?> clazz = Class.forName(clazzName);
//            Method method = clazz.getMethod(methodName);
//            method.invoke(null);
//        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
//            throw new RuntimeException(ex);
//        }
//
//        if ( !UiCore.global().gluonSupport().isPresent() )
//            throw new IllegalStateException("Trying to active gluon mode, but no local Service implementation of GluonSupport found. Is the dependency saft-gluon available ?");
//        L.info("Starting SAFT in Gloun Mode, using MainStage");
//        mainStage.setOnCloseRequest((e) -> {
//            global().shutdown();
//        });
    }
}
