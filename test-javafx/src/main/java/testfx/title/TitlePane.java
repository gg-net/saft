/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.title;

import java.util.function.Consumer;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

import eu.ggnet.saft.core.ui.Bind;

import static eu.ggnet.saft.core.ui.Bind.Type.SHOWING;
import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

/**
 *
 * @author oliver.guenther
 */
public class TitlePane extends FlowPane implements Consumer<BooleanProperty> {

    @Bind(TITLE)
    private final StringProperty titleProperty;

    @Bind(SHOWING)
    private final BooleanProperty showingProperty;

    public TitlePane() {
        titleProperty = new SimpleStringProperty();
        showingProperty = new SimpleBooleanProperty();
        TextField titleField = new TextField("The Title Pane");
        titleProperty.bind(titleField.textProperty());
        Label l = new Label("Title: ");
        Button close = new Button("Extra Close");
        close.setOnAction(e -> showingProperty.set(false));
        getChildren().addAll(l, titleField, close);
    }

    @Override
    public void accept(BooleanProperty showing) {
        showing.bindBidirectional(showingProperty);
    }

}
