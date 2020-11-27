package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import eu.ggnet.saft.core.UiCore;

import static eu.ggnet.saft.core.UiCore.global;

/**
 *
 * @author pascal.perau
 */
public class SaftFx3 {

    public static class SaftFxApplication extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            UiCore.continueJavaFx(stage);

            GettingStartedPane2 pane = new GettingStartedPane2();
            stage.setScene(new Scene(pane, 800, 600));
            stage.show();
        }

        @Override
        public void stop() throws Exception {
            global().shutdown();
        }

    }

    public static void main(String[] args) {
        Application.launch(SaftFxApplication.class, args);
    }

}
