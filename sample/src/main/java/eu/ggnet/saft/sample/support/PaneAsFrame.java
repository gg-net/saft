package eu.ggnet.saft.sample.support;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import eu.ggnet.saft.core.ui.Frame;

import static javafx.scene.text.Font.font;

/**
 *
 * @author oliver.guenther
 */
@Frame
public class PaneAsFrame extends BorderPane {

    public PaneAsFrame() {
        Label l = new Label("Pane As Frame");
        l.setFont(font(50));
        setCenter(l);
    }

}
