package eu.ggnet.saft.sample.tutorial.chapter06;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import eu.ggnet.saft.core.ui.Title;

@Title("Titel per Class-Annotation")
public class FxComponentWithTitleAnnotation extends Pane {

    public FxComponentWithTitleAnnotation() {

        this.setMinSize(400, 400);
        Label label = new Label("Hier wurde der Titel per class-Annotation gesetzt!");
        this.getChildren().add(label);

    }

}
