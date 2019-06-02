package eu.ggnet.saft.gluon.sample;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.LifecycleService;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.charm.glisten.visual.Swatch;
import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.gluon.Gi;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SaftGluonSampleApplication extends MobileApplication {

    public static final String PRIMARY_VIEW = HOME_VIEW;
    public static final String SECONDARY_VIEW = "Secondary View";

    @Override
    public void init() {
        addViewFactory(PRIMARY_VIEW, () -> Ui.construct(PrimaryPresenter.class).getRoot());
        addViewFactory(SECONDARY_VIEW, () -> Ui.construct(SecondaryPresenter.class).getRoot());

        buildDrawer(this);
    }

    @Override
    public void postInit(Scene scene) {
        UiCore.continueGluon(scene);
        Swatch.BLUE.assignTo(scene);

        scene.getStylesheets().add(SaftGluonSampleApplication.class.getResource("style.css").toExternalForm());
        ((Stage) scene.getWindow()).getIcons().add(new Image(SaftGluonSampleApplication.class.getResourceAsStream("/icon.png")));
    }

    public static void buildDrawer(MobileApplication app) {
        NavigationDrawer drawer = app.getDrawer();

        NavigationDrawer.Header header = new NavigationDrawer.Header("Gluon Mobile",
                "Multi View Project",
                new Avatar(21, new Image(MobileApplication.class.getResourceAsStream("/icon.png"))));
        drawer.setHeader(header);

        final NavigationDrawer.Item primaryItem = new NavigationDrawer.ViewItem("Primary", MaterialDesignIcon.HOME.graphic(), PRIMARY_VIEW, ViewStackPolicy.SKIP);
        final NavigationDrawer.Item secondaryItem = new NavigationDrawer.ViewItem("Secondary", MaterialDesignIcon.DASHBOARD.graphic(), SECONDARY_VIEW);

        final NavigationDrawer.Item alertItem = new NavigationDrawer.Item("Saft - Alert", MaterialDesignIcon.ACCESSIBLE.graphic());
        alertItem.setOnMouseClicked(e -> Ui.build().alert("This is a Saft alert"));

        final NavigationDrawer.Item infoViewItem = new NavigationDrawer.Item("Saft - show info.fx (code)", MaterialDesignIcon.BOOK.graphic());
        infoViewItem.setOnMouseClicked(e -> Ui.build().fx().show(() -> new InfoView()));

        final NavigationDrawer.Item infoItem = new NavigationDrawer.Item("Saft - show (info.fxml)", MaterialDesignIcon.ALARM.graphic());
        infoItem.setOnMouseClicked(e -> Ui.build().fxml().show(InfoPresenter.class));

        final NavigationDrawer.Item personItem = new NavigationDrawer.Item("Saft - eval (person.fxml)", MaterialDesignIcon.PERSON.graphic());
        personItem.setOnMouseClicked(e -> Ui.build().fxml().eval(() -> new Person("Max", "Mustermann", 31), PersonPresenter.class).cf()
                .thenAcceptAsync((Person p) -> new Toast("Storing " + p).show(), Platform::runLater)
                .handle(Ui.handler()));

        final NavigationDrawer.Item airplaneItem = new NavigationDrawer.Item("Saft - eval Gloun Dialog", MaterialDesignIcon.AIRLINE_SEAT_FLAT.graphic());
        airplaneItem.setOnMouseClicked(e -> Gi.build().gluonDialog().eval(() -> new AirplaneDialog()).cf()
                .thenAcceptAsync(a -> new Toast("Lift of " + a).show(), Platform::runLater)
                .handle(Ui.handler()));

        drawer.getItems().addAll(primaryItem, secondaryItem, alertItem, infoViewItem, infoItem, personItem, airplaneItem);

        if (com.gluonhq.charm.down.Platform.isDesktop()) {
            final NavigationDrawer.Item quitItem = new NavigationDrawer.Item("Quit", MaterialDesignIcon.EXIT_TO_APP.graphic());
            quitItem.selectedProperty().addListener((obs, ov, nv) -> {
                if (nv) {
                    Services.get(LifecycleService.class).ifPresent(LifecycleService::shutdown);
                }
            });
            drawer.getItems().add(quitItem);
        }
    }
}
