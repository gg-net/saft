package eu.ggnet.saft.sample.tutorial.chapter05;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import eu.ggnet.saft.core.ui.Frame;

@Frame
public class FxFrameComponent extends Pane {

    public FxFrameComponent() {
        this.setMinSize(400, 400);
        Label label = new Label("Dies ist ein Frame via Annotation!");
        this.getChildren().add(label);
    }
}
