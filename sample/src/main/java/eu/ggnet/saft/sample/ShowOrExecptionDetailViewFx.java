/*
 * Copyright (C) 2020 GG-Net GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.ggnet.saft.sample;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import eu.ggnet.saft.core.*;
import eu.ggnet.saft.core.impl.DefaultExceptionConsumer;
import eu.ggnet.saft.core.impl.DetailView;
import eu.ggnet.saft.sample.support.FramePanel;

/**
 *
 * @author mirko.schulze
 */
public class ShowOrExecptionDetailViewFx {

    public static class ShowOrExecptionDetailViewApplication extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {

            Button b1 = new Button("Show DetailView");
            b1.setOnAction((e) -> {
                Ui.build().parent(b1).title("Systemfehler").swing()
                        .show(() -> new DetailView(message(), longMessage(), veryLongMessage()));
            });

            Button b2 = new Button("Exception over Parent");
            b2.setOnAction((e) -> {
                Ui.build(b2).swing().show(FramePanel.class);
            });

            UiCore.continueJavaFx(UiUtil.startup(primaryStage, () -> new FlowPane(b1, b2)));
            UiCore.global().overwriteFinalExceptionConsumer(new DefaultExceptionConsumer());
        }

    }

    public static void main(String[] args) {
        Application.launch(ShowOrExecptionDetailViewApplication.class, args);
    }

    private static String message() {
        return "Eine Nachricht Nachricht Nachricht Nachricht Nachricht Nachricht Nachricht Nachricht Nachricht Nachricht";
    }

    private static String longMessage() {
        return "Oh Oh"
                + "Oh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh "
                + "OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh OhOh Oh";
    }

    private static String veryLongMessage() {
        return "Oh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh Ja"
                + "Oh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh Ja"
                + "Oh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh JaOh Ja";
    }
}
