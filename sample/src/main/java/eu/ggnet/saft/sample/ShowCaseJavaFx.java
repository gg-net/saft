/*
 * Copyright (C) 2018 GG-Net GmbH
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
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.Title;
import eu.ggnet.saft.sample.ShowCaseUniversal.Sitem;
import eu.ggnet.saft.sample.ShowCaseUniversal.Smenu;

/**
 *
 * @author oliver.guenther
 */
public class ShowCaseJavaFx {

    @Title("JavaFx Showcase")
    public static class FxPane extends BorderPane {

        public FxPane() {
            ShowCaseUniversal u = new ShowCaseUniversal();

            MenuBar mb = new MenuBar();
            for (Smenu smenu : u.menu()) {
                Menu submenu = new Menu(smenu.name);
                for (Sitem item : smenu.items) {
                    MenuItem menuItem = new MenuItem(item.key);
                    menuItem.setOnAction((e) -> item.value.run());
                    submenu.getItems().add(menuItem);
                }
                mb.getMenus().add(submenu);
            }

            Label mainLabel = new Label("Main Applikation");
            mainLabel.setFont(new Font("Arial", 48));

            setTop(mb);
            setCenter(mainLabel);
        }
    }

    public static class ShowCaseJavaFxApplication extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            UiCore.startJavaFx(primaryStage, () -> new FxPane());
        }

        @Override
        public void stop() throws Exception {
            System.out.println("Stop called: Showing open threads");
            Thread.getAllStackTraces().keySet().stream().forEach(System.out::println);
        }

    }

    public static void main(String[] args) {
        Application.launch(ShowCaseJavaFxApplication.class, args);
    }

}
