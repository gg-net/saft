package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import eu.ggnet.saft.core.Saft;

/**
 *
 * @author pascal.perau
 */
public class ModalityShowCasePane extends BorderPane {

    public ModalityShowCasePane(Saft saft, Modality modality, Label text) {
        Button bAnother = new Button("another one!");
        bAnother.setOnAction(e -> saft.build(this).modality(modality).fx().show(() -> new ModalityShowCasePane(saft, modality, text)));

        this.setTop(bAnother);
        this.setCenter(text);
    }
}
