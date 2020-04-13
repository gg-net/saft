package testfx.support;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.*;

import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

/**
 * FXML Controller class
 *
 * @author oliver.guenther
 */
@Title("Bla")
public class BasicApplicationController implements FxController {

    public final static String LABEL_TEXT = "Ein ganz toller Text";

    public final static String LABEL_ID = "fxmlOutput";

    public final static String TITLE_TEXT_FIELD_ID = "titleTextField";

    public final static String CLOSE_ID = "fxmlClose";

    @FXML
    private Label fxmlOutput;

    @FXML
    private Button fxmlClose;

    @FXML
    private TextField titleTextField;

    @Bind(TITLE)
    private final StringProperty titleProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
//        titleTextField.setId(TITLE_TEXT_FIELD_ID);

        titleProperty.bind(titleTextField.textProperty());
        titleTextField.setText("Der Title");

        fxmlClose.setOnAction(e -> Ui.closeWindowOf(fxmlClose));
        fxmlOutput.setText(LABEL_TEXT);
    }
}
