package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.impl.Fx;

import static eu.ggnet.saft.core.UiCore.global;

/**
 *
 * @author pascal.perau
 */
public class SaftFx2 {

    public static class SaftFxApplication extends Application {

        private Saft saft;

        @Override
        public void start(Stage stage) throws Exception {
            global().init(new Fx(global(), stage));

            GettingStartedPane pane = new GettingStartedPane(global());
            stage.setScene(new Scene(pane, 800, 600));
            stage.show();
        }

        @Override
        public void stop() throws Exception {
            saft.shutdown();
        }
    }

    public static void main(String[] args) {
        Application.launch(SaftFxApplication.class, args);
    }
}
