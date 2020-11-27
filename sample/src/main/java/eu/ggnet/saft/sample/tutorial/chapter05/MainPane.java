package eu.ggnet.saft.sample.tutorial.chapter05;

import eu.ggnet.saft.core.Ui;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

public class MainPane extends FlowPane {
    
    public MainPane() {
        
        Button button = new Button("FxFrame via Annotation");
        Button button2 = new Button("SwingFrame via Annotation");
        Button button3 = new Button("FxFrame via frame(true)");
        Button button4 = new Button("SwingFrame via frame(true)");

        
        button.setOnAction(e -> {
            Ui.build().fx().show(FxFrameComponent.class);
        });
        
        button2.setOnAction(e -> {
            Ui.build().swing().show(SwingFrameComponent.class);
        });
        
        button3.setOnAction(e -> {
            Ui.build().frame(true).fx().show(FxComponent.class);
        });
        
        button4.setOnAction(e -> {
            Ui.build().frame(true).swing().show(SwingComponent.class);
        });

        
        this.getChildren().addAll(button, button2, button3, button4);
        
    }
    
}
