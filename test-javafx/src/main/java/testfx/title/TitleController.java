/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.title;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import eu.ggnet.saft.core.ui.Bind;
import eu.ggnet.saft.core.ui.FxController;

import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

public class TitleController implements FxController {

    @FXML
    private TextField titleField;

    @Bind(TITLE)
    private final StringProperty titleProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
        titleProperty.bind(titleField.textProperty());
    }

}
