/*
 * Swing and JavaFx Together (Saft)
 * Copyright (C) 2020  Oliver Guenther <oliver.guenther@gg-net.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 with
 * Classpath Exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with Classpath Exception along with this program.
 */
package eu.ggnet.saft.core.ui;

import java.net.URL;
import java.util.*;

import eu.ggnet.saft.core.impl.AbstractCore.IconConfig;

/**
 * Tagging interface for FX Controller of FXML Files.
 * <p>
 * Nameconvention:
 * A Controller class must end with Controller or Presenter and implement {@link FxController}.
 * The FXML file must be in the same package and may end with View.fxml or only the name either in bump writing or everything lowercase.
 * If Icons are wanted, look into {@link IconConfig}.
 * <p>
 * Example: LoginHelper
 * <ul>
 * <li>Controller: LoginHelperController.java or LoginHelperPresenter.java</li>
 * <li>Fxml file: LoginHelperView.fxml or LoginHelper.fxml or loginhelper.fxml</li>
 * </ul>
 *
 * @author oliver.guenther
 */
public interface FxController {

    /**
     * Allowed suffixes for controller classes.
     */
    public final static List<String> ALLOWED_SUFFIXES = Arrays.asList("Controller", "Presenter");

    /**
     * Returns a optional head.
     * Internal method, wrong usage may fail, will be private in jdk after 8.
     *
     * @param <R>             type of FxController.
     * @param controllerClazz the controller class
     * @return a optional head.
     */
    // TODO: Make private on switch to jdk after 8.
    static <R extends FxController> Optional<String> toHead(Class<R> controllerClazz) {
        return ALLOWED_SUFFIXES
                .stream()
                .filter(suffix -> controllerClazz.getSimpleName().endsWith(suffix))
                .map(suffix -> controllerClazz.getSimpleName().substring(0, controllerClazz.getSimpleName().length() - suffix.length()))
                .findFirst();
    }

    /**
     * Returns an optiona url of the fxml file.
     * Internal method, wrong usage may fail, will be private in jdk after 8.
     *
     * @param <R>             type of FxController.
     * @param controllerClazz the controller class
     * @return an optiona url of the fxml file.
     */
    // TODO: Make private on switch to jdk after 8.
    static <R extends FxController> Optional<URL> toUrl(Class<R> controllerClazz) {
        Optional<String> optHead = toHead(controllerClazz);
        List<String> names = Arrays.asList(optHead.get() + "View.fxml", optHead.get() + ".fxml", optHead.get().toLowerCase() + ".fxml");

        return names.stream()
                .map(n -> controllerClazz.getResource(n))
                .filter(Objects::nonNull)
                .findFirst();
    }

    /**
     * Returns true, if the controller is valid.
     * Validations: See {@link FxController}
     *
     * @param <R>             type of FxController.
     * @param controllerClazz the controller to be validated.
     * @return true if controler is valid.
     */
    public static <R extends FxController> boolean isValid(Class<R> controllerClazz) {
        return validationMessage(controllerClazz) == null;
    }

    /**
     * Returns a messeage, if this controller is not valid.
     * Validations: See {@link FxController}
     *
     * @param <R>             type of FxController.
     * @param controllerClazz the controller to be validated.
     * @return null if valid, else the error message.
     */
    public static <R extends FxController> String validationMessage(Class<R> controllerClazz) {
        if ( controllerClazz == null ) return "FxController class is null";
        if ( !toHead(controllerClazz).isPresent() )
            return "FxController " + controllerClazz.getName() + " does not match the nameing convention. Must end with any of " + ALLOWED_SUFFIXES;
        if ( !toUrl(controllerClazz).isPresent() ) {
            return "FxController " + controllerClazz.getName() + " does not have an fxml file. Either wrong package or missmatch file name.";
        }
        return null;
    }

    /**
     * Returns an Url of the FXML file based on the controllerClazz.
     * Name Convetions see {@link FxController}.
     *
     * @param <R>             the type of the contorller class
     * @param controllerClazz the controller class
     * @return a url of the FXML file, ready to be used in the FXMLLoader.
     * @throws IllegalArgumentException if the controller class is invalid. see {@link FxController#isValid(java.lang.Class) }.
     * @throws NullPointerException     if controller class is null.
     */
    public static <R extends FxController> URL loadView(Class<R> controllerClazz) throws IllegalArgumentException, NullPointerException {
        if ( !isValid(Objects.requireNonNull(controllerClazz, "controller class must not be null")) )
            throw new IllegalArgumentException(validationMessage(controllerClazz));
        return toUrl(controllerClazz).orElseThrow(() -> new IllegalArgumentException("No fxml found for " + controllerClazz.getName()));
    }

}
