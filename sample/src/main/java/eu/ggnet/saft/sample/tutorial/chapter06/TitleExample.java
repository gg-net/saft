package eu.ggnet.saft.sample.tutorial.chapter06;

import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.ui.LocationStorage;

public class TitleExample {
    
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
    
    public static class App extends Application {
        
        private Saft saft;
        
        @Override
        public void start(Stage stage) throws Exception {
            
            saft = new Saft(new LocationStorage(), Executors.newCachedThreadPool());
            UiCore.initGlobal(saft);
            saft.init(new Fx(saft));
            saft.core(Fx.class).initMain(stage);
            MainPane mainPane = new MainPane();
            
            //Classisches ändern des Titels an der Stage
            stage.setTitle("Titel des MainPane!");
                        
            stage.setScene(new Scene(mainPane));
            
            stage.show();
        }
        
        @Override
        public void stop() {
            
        }
        
    }
    
}
