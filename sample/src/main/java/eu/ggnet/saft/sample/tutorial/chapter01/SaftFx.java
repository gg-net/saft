package eu.ggnet.saft.sample.tutorial.chapter01;

import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.ui.LocationStorage;
import eu.ggnet.saft.sample.SimpleJavaFx.MyPane;

/**
 *
 * @author pascal.perau
 */
public class SaftFx {

    public static class SaftFxApplication extends Application {

        private Saft saft;

        @Override
        public void start(Stage stage) throws Exception {
            saft = new Saft(new LocationStorage(), Executors.newCachedThreadPool());

            saft.init(new Fx(saft));
            GettingStartedPane pane = new GettingStartedPane(saft);
            stage.setScene(new Scene(pane, 800, 600));
            saft.core(Fx.class).initMain(stage);

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
