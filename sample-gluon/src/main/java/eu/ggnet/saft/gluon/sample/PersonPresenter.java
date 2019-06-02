/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.sample;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.ResultProducer;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

/**
 *
 * @author oliver.guenther
 */
public class PersonPresenter implements FxController, Initializable, Consumer<Person>, ResultProducer<Person> {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Spinner<Integer> ageSpinner;

    private boolean ok = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ageSpinner.valueFactoryProperty().set(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 150, 21));

    }

    @FXML
    void clickCancel(ActionEvent event) {
        Ui.closeWindowOf(firstNameField);
    }

    @FXML
    void clickOk(ActionEvent event) {
        ok = true;
        Ui.closeWindowOf(firstNameField);

    }

    @Override
    public void accept(Person p) {
        if (p == null) {
            return;
        }
        firstNameField.setText(p.firstName);
        lastNameField.setText(p.lastName);
        ageSpinner.valueFactoryProperty().set(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 150, p.age));
    }

    @Override
    public Person getResult() {
        if (ok) {
            return new Person(firstNameField.getText(), lastNameField.getText(), ageSpinner.getValue());
        }
        return null;
    }

}
