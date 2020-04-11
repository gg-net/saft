package eu.ggnet.saft.sample.testing;

import java.util.function.Consumer;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.*;

import static eu.ggnet.saft.core.ui.Bind.Type.SHOWING;
import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

public class ButtonController implements FxController, ResultProducer<String>, Consumer<Handler> {

    @FXML
    private Button okButton;

    private boolean ok = false;

    @Bind(SHOWING)
    private final BooleanProperty showingProperty = new SimpleBooleanProperty();

    @Bind(TITLE)
    private final StringProperty titleProperty;

    public ButtonController() {
        this.titleProperty = new SimpleStringProperty();
    }

    public void initialize() {
        okButton.setOnAction(e -> {
            ok = true;
            Ui.closeWindowOf(okButton);
            // Später geht das
            // showingProperty.set(false);
        });
    }

    public BooleanProperty showingProperty() {
        return showingProperty;
    }

    @Override
    public String getResult() {
        Stage s = new Stage();
        if ( ok ) return "Ok";
        return null;
    }

    @Override
    public void accept(Handler h) {
        h.showingProperty().bindBidirectional(showingProperty);

    }

}
