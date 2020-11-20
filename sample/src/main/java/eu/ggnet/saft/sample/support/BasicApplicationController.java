package eu.ggnet.saft.sample.support;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import eu.ggnet.saft.core.ui.*;

/**
 * FXML Controller class
 *
 * @author oliver.guenther
 */
@Frame
@StoreLocation
public class BasicApplicationController implements FxController {

    @FXML
    private TextArea textArea;

    @Inject
    private CdiInput cdiInput;

    @FXML
    private void initialize() {
        System.out.println("initialize() cdiInput set ? " + (cdiInput != null) + " textArea set ?" + (textArea != null));
        if ( cdiInput != null ) textArea.setText(cdiInput.msg());
    }

    @PostConstruct
    private void postCdi() {
        System.out.println("postCdi() cdiInput set ? " + (cdiInput != null) + " textArea set ?" + (textArea != null));
    }

}
