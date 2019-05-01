package testfx.support;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.Title;

/**
 * FXML Controller class
 *
 * @author oliver.guenther
 */
@Title("Bla")
public class BasicApplicationController implements FxController {

    public final static String LABEL_TEXT = "Ein ganz toller Text";
    public final static String LABEL_ID = "fxmlOutput";
    public final static String CLOSE_ID = "fxmlClose";
    
    
    @FXML
    private Label fxmlOutput;

    @FXML
    private Button fxmlClose;
    
    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;
    
    @FXML
    public void initialize() {
        fxmlClose.setOnAction(e -> Ui.closeWindowOf(fxmlClose));
        fxmlOutput.setText(LABEL_TEXT);
    }
}
