/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter02;

import java.lang.reflect.InvocationTargetException;

import javafx.application.Application;
import javafx.stage.Stage;

import eu.ggnet.saft.core.*;

/**
 * Typische Swing Applikation - 05.
 *
 * @author oliver.guenther
 */
public class SaftSwing06 {

    public static class SaftSwingApplication extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            UiCore.continueJavaFx(UiUtil.startup(stage, () -> {
                MainApplicationPane p = new MainApplicationPane();

                p.getButtonOne().setOnAction(e -> Ui.build(p).swing().show(() -> new WindowOne()));
                p.getButtonTwo().setOnAction(e -> Ui.build(p).fx().show(() -> new WindowTwo()));

                return p;
            }));
        }

        @Override
        public void stop() throws Exception {
            UiCore.global().shutdown();
        }

    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        Application.launch(SaftSwingApplication.class, args);
    }

}
