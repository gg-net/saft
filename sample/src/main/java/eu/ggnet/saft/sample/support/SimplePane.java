package eu.ggnet.saft.sample.support;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import eu.ggnet.saft.core.ui.StoreLocation;
import eu.ggnet.saft.core.ui.Title;

import static javafx.scene.text.Font.font;

/**
 *
 * @author oliver.guenther
 */
@Title("Extra Title of Simple Pane")
@StoreLocation
public class SimplePane extends BorderPane {

    public SimplePane() {
        Label l = new Label("Die JavaFx Main Application");
        l.setFont(font(50));
        setCenter(l);
    }

}
