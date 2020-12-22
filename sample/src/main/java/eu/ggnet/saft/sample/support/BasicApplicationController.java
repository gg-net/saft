package eu.ggnet.saft.sample.support;

import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.UiCore;
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

    @FXML
    private MenuItem exceptionItem;

    @Inject
    private CdiInput cdiInput;

    @FXML
    private void initialize() {
        L.debug("initialize() cdiInput set ? (" + (cdiInput != null) + "), textArea set ? (" + (textArea != null) + ")");
        if ( cdiInput != null ) textArea.setText(cdiInput.msg());
        exceptionItem.setOnAction(e -> {
            CompletableFuture.runAsync(() -> {
                throw new RuntimeException("Blub");
            }).handle(UiCore.global().handler(textArea));
        });
    }

    @PostConstruct
    private void postCdi() {
        L.debug("postCdi() cdiInput set ? (" + (cdiInput != null) + ") textArea set ? (" + (textArea != null) + ")");
    }

}
