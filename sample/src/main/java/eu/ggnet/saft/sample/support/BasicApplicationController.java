package eu.ggnet.saft.sample.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.*;

import static eu.ggnet.saft.core.ui.Bind.Type.ICONS;

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

    @Bind(ICONS)
    private final ObservableList<Image> icons;

    public BasicApplicationController() {
        this.icons = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        L.debug("initialize() cdiInput set ? (" + (cdiInput != null) + "), textArea set ? (" + (textArea != null) + ")");
        if ( cdiInput != null ) textArea.setText(cdiInput.msg());
        exceptionItem.setOnAction(e -> {
            CompletableFuture.runAsync(() -> {
                throw new RuntimeException("Blub");
            }).handle(UiCore.global().handler(textArea));
        });

        icons.addListener(new ListChangeListener<Image>() {
            @Override
            public void onChanged(Change<? extends Image> c) {
                System.out.println("Change detected");
            }

        });
    }

    @PostConstruct
    private void postCdi() {
        L.debug("postCdi() cdiInput set ? (" + (cdiInput != null) + ") textArea set ? (" + (textArea != null) + ")");
    }

    @FXML
    public void onActionIconOne() {
        try (InputStream is = this.getClass().getResourceAsStream("money.png")) {
            icons.clear();
            icons.add(new Image(is));

        } catch (IOException | NullPointerException ex) {
            throw new RuntimeException("Can't load money.png", ex);
        }

    }

    @FXML
    public void onActionIconTwo() {
        try (InputStream is = this.getClass().getResourceAsStream("cart.png")) {
            icons.clear();
            icons.add(new Image(is));
        } catch (IOException | NullPointerException ex) {
            throw new RuntimeException("Can't load cart.png", ex);
        }

    }

}
