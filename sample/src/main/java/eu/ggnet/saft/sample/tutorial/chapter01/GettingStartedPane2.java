package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import static eu.ggnet.saft.core.UiCore.global;

/**
 *
 * @author pascal.perau
 */
public class GettingStartedPane2 extends BorderPane {

    private final StringProperty displayString = new SimpleStringProperty("");

    private final Label displayLabel = new Label();

    public GettingStartedPane2() {
        displayLabel.textProperty().bind(displayString);

        Button bHello = new Button("Hello");
        bHello.setOnAction(e -> displayString.set(displayString.get().concat(" Hallo")));

        Button bWorld = new Button("world");
        bWorld.setOnAction(e -> displayString.set(displayString.get().concat(" world")));

        Button bSaft = new Button("Saft!");
        bSaft.setOnAction(e -> displayString.set(displayString.get().concat(" Saft!")));

        Button bElsewhere = new Button("Elsewhere!");
        bElsewhere.setOnAction(e
                -> global().build(this).fx()
                        .show(() -> new AnchorPane(new Label(displayString.get())))
        );

        HBox topBox = new HBox(5, bHello, bWorld, bSaft, bElsewhere);

        this.setTop(topBox);
        this.setCenter(displayLabel);
    }

}
