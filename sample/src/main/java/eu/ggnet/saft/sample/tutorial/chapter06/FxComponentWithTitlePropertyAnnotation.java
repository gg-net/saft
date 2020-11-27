package eu.ggnet.saft.sample.tutorial.chapter06;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import eu.ggnet.saft.core.ui.Bind;

import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

public class FxComponentWithTitlePropertyAnnotation extends Pane {

    @Bind(TITLE)
    private final StringProperty titleProperty = new SimpleStringProperty();

    public FxComponentWithTitlePropertyAnnotation(String title) {
        titleProperty.setValue(title);

        this.setMinSize(400, 400);
        Label label = new Label("Hier wurde der Titel per Annotation an der StringProperty gesetzt!");
        this.getChildren().add(label);

    }

}
