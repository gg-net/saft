package eu.ggnet.saft.gluon.sample;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import eu.ggnet.saft.core.ui.FxController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PrimaryPresenter implements FxController {

    @FXML
    private View primary;

    @FXML
    private Label label;

    public void initialize() {
        primary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e
                        -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Primary");
                appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e
                        -> System.out.println("Search")));
            }
        });
    }

    @FXML
    void buttonClick() {
        label.setText("Hello JavaFX Universe!");
    }

}
