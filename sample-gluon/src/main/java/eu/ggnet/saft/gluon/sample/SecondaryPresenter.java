package eu.ggnet.saft.gluon.sample;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.ui.FxController;
import javafx.fxml.FXML;

public class SecondaryPresenter implements FxController {

    @FXML
    private View secondary;

    public void initialize() {
        secondary.setShowTransitionFactory(BounceInRightTransition::new);

        FloatingActionButton fab = new FloatingActionButton(MaterialDesignIcon.INFO.text,
                e -> System.out.println("Info"));
        fab.showOn(secondary);

        secondary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e
                        -> MobileApplication.getInstance().getDrawer().open()));
                appBar.setTitleText("Secondary");
                appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e
                        -> System.out.println("Favorite")));
            }
        });
    }

    @FXML
    private void openSaftFxBorderPane() {
        Ui.build().fx().show(() -> new InfoView());
    }

    @FXML
    private void clickClose() {
        Ui.closeWindowOf(secondary);
    }
}
