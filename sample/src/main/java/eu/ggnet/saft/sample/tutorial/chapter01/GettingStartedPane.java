package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import eu.ggnet.saft.core.Saft;

/**
 *
 * @author pascal.perau
 */
public class GettingStartedPane extends BorderPane {

    private final StringProperty displayString = new SimpleStringProperty("");

    private final Label displayLabel = new Label();

    public GettingStartedPane(Saft saft) {
        displayLabel.textProperty().bind(displayString);

        Button bHello = new Button("Hello");
        bHello.setOnAction(e -> displayString.set(displayString.get().concat(" Hallo")));

        Button bWorld = new Button("world");
        bWorld.setOnAction(e -> displayString.set(displayString.get().concat(" world")));

        Button bSaft = new Button("Saft!");
        bSaft.setOnAction(e -> displayString.set(displayString.get().concat(" Saft!")));

        Button bElsewhere = new Button("Elsewhere!");
        bElsewhere.setOnAction(e
                -> saft.build(this).fx()
                        .show(() -> new AnchorPane(new Label(displayString.get())))
        );

        HBox topBox = new HBox(5, bHello, bWorld, bSaft, bElsewhere);

        this.setTop(topBox);
        this.setCenter(displayLabel);
    }

}
