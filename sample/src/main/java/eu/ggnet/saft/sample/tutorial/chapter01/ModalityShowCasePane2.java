package eu.ggnet.saft.sample.tutorial.chapter01;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import static eu.ggnet.saft.core.UiCore.global;

/**
 *
 * @author pascal.perau
 */
public class ModalityShowCasePane2 extends BorderPane {

    public ModalityShowCasePane2(Modality modality, Label text) {
        Button bAnother = new Button("another one!");
        bAnother.setOnAction(e -> global().build().modality(modality).fx().show(() -> new ModalityShowCasePane2(modality, text)));

        this.setTop(bAnother);
        this.setCenter(text);
    }
}
