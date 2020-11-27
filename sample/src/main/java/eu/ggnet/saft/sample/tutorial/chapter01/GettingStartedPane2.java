package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

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

        Button bHello = new Button("Hello world Saft!");
        bHello.setOnAction(e -> displayString.set(displayString.get().concat(" Hello world Saft!")));

        Button bFree = new Button("no modality");
        bFree.setOnAction(e
                -> global().build(this).fx()
                        .show(() -> new AnchorPane(new Label("You are free to use other windows as usual.")))
        );

        Button bWinModal = new Button("window modality!");
        bWinModal.setOnAction(e
                -> global().build(this).modality(Modality.WINDOW_MODAL).fx()
                        .show(() -> new ModalityShowCasePane2(Modality.WINDOW_MODAL, new Label("Window modal windows will only block their parents usage.")))
        );

        Button bAppModal = new Button("application modality!");
        bAppModal.setOnAction(e
                -> global().build(this).modality(Modality.APPLICATION_MODAL).fx()
                        .show(() -> new ModalityShowCasePane2(Modality.APPLICATION_MODAL, new Label("Application modal windows block the entire application until the window is closed.")))
        );

        HBox topBox = new HBox(5, bHello, bFree, bWinModal, bAppModal);

        this.setTop(topBox);
        this.setCenter(displayLabel);
    }

}
