/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.title;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;

/**
 *
 * @author oliver.guenther
 */
public class TitlesFx {

    public static class FxApplication extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            UiCore.startJavaFx(primaryStage, () -> {
                return new FlowPane(5, 5,
                        button("Java Fx Pane with Title Binding", e -> Ui.build().fx().show(() -> new TitlePane())),
                        button("Java Fxml with Title Binding", e -> Ui.build().fxml().show(TitleController.class)),
                        button("Java Fx Dialog with Title Binding", e -> Ui.build().dialog().eval(() -> new TitleDialog()).cf().handle(Ui.handler())),
                        button("TitleDialog", e -> new TitleDialog().showAndWait().ifPresent(v -> System.out.println("Nach Dialog"))),
                        button("Swing JPanel with Title Binding", e -> Ui.build().swing().show(() -> new TitleJPanel()))
                );
            });
        }

    }

    public static Button button(String text, EventHandler<ActionEvent> value) {
        Button b = new Button(text);
        b.setOnAction(value);
        return b;
    }

    public static void main(String[] args) {
        Application.launch(FxApplication.class);
    }

}
