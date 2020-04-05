package eu.ggnet.saft.sample.support;

import java.util.function.Consumer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import eu.ggnet.saft.core.ui.TitleSupplier;

import static javafx.scene.text.Font.font;

/**
 * A Pane, demonstrating the usage of a titleproperty.
 *
 * @author oliver.guenther
 */
public class TitlePropertyPane extends BorderPane implements Consumer<String>, TitleSupplier {

    private Label z;

    private final StringProperty titleProperty = new SimpleStringProperty();

    private int counter = 0;

    public TitlePropertyPane() {
        Label l = new Label("Pane As Cosumer of IdSupplier");
        l.setFont(font(50));
        Button b = new Button("Change Title Button");
        b.setOnAction(e -> titleProperty.set(this.getClass().getSimpleName() + " | Button pressed, counter=" + counter++));
        z = new Label("Nothing consumed yet");
        z.setFont(font(20));
        setTop(l);
        setCenter(b);
        setBottom(z);
    }

    @Override
    public void accept(String t) {
        z.setText("Consumed: " + t);
        titleProperty.set(this.getClass().getSimpleName() + " | Cosumed " + t);
    }

    @Override
    public StringProperty titleProperty() {
        return titleProperty;
    }

}
