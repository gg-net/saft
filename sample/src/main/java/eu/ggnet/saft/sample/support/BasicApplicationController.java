package eu.ggnet.saft.sample.support;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.*;

/**
 * FXML Controller class
 *
 * @author oliver.guenther
 */
@Frame
@StoreLocation
public class BasicApplicationController implements FxController {

    private final static Logger L = LoggerFactory.getLogger(BasicApplicationController.class);

    @FXML
    private TextArea textArea;

    @Inject
    private CdiInput cdiInput;

    @FXML
    private void initialize() {
        L.debug("initialize() cdiInput set ? (" + (cdiInput != null) + "), textArea set ? (" + (textArea != null) + ")");
        if ( cdiInput != null ) textArea.setText(cdiInput.msg());
    }

    @PostConstruct
    private void postCdi() {
        L.debug("postCdi() cdiInput set ? (" + (cdiInput != null) + ") textArea set ? (" + (textArea != null) + ")");
    }

}
