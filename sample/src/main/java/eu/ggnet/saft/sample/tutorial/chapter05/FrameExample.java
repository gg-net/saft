package eu.ggnet.saft.sample.tutorial.chapter05;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.ui.LocationStorage;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FrameExample {

    public static class App extends Application {

        private Saft saft;

        @Override
        public void start(Stage stage) throws Exception {

            saft = new Saft(new LocationStorage(), Executors.newCachedThreadPool());
            UiCore.initGlobal(saft);
            saft.init(new Fx(saft));
            saft.core(Fx.class).initMain(stage);

            stage.setScene(new Scene(new MainPane()));

            stage.show();

        }

        @Override
        public void stop() {
            saft.shutdown();
        }

    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

}
