/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample;

import javafx.application.Application;
import javafx.stage.Stage;

import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.sample.testing.ButtonMain;

/**
 * Demonstrate and verfiy the blocking/non-blocking features of eval, show and construct.
 *
 * @author oliver.guenther
 */
public class BlockingOfShowAndEval {

    public static class FxApplication extends Application {

        @Override
        public void start(Stage ps) throws Exception {
            UiCore.startJavaFx(ps, () -> new ButtonMain());
        }

    }

    public static void main(String[] args) {
        Application.launch(FxApplication.class);
    }

}
