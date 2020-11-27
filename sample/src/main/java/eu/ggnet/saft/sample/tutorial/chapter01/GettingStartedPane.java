package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

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

        Button bHello = new Button("Hello world Saft!");
        bHello.setOnAction(e -> displayString.set(displayString.get().concat(" Hello world Saft!")));

        Button bFree = new Button("no modality");
        bFree.setOnAction(e
                -> saft.build(this).fx()
                        .show(() -> new AnchorPane(new Label("You are free to use other windows as usual.")))
        );

        Button bWinModal = new Button("window modality!");
        bWinModal.setOnAction(e
                -> saft.build(this).modality(Modality.WINDOW_MODAL).fx()
                        .show(() -> new ModalityShowCasePane(saft, Modality.WINDOW_MODAL, new Label("Window modal windows will only block their parents usage.")))
        );

        Button bAppModal = new Button("application modality!");
        bAppModal.setOnAction(e
                -> saft.build(this).modality(Modality.APPLICATION_MODAL).fx()
                        .show(() -> new ModalityShowCasePane(saft, Modality.APPLICATION_MODAL, new Label("Application modal windows block the entire application until the window is closed.")))
        );

        HBox topBox = new HBox(5, bHello, bFree, bWinModal, bAppModal);

        this.setTop(topBox);
        this.setCenter(displayLabel);
    }

}
