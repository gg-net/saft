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
package eu.ggnet.saft.sample.tutorial.chapter15;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import javafx.application.Application;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiUtil;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.sample.support.BasicApplicationController;

/**
 *
 * @author oliver.guenther
 */
public class SaftCdi {

    public static class SaftCdiApplication extends Application {

        private SeContainer container;

        private Saft saft;

        @Override
        public void start(Stage stage) throws Exception {
            saft.core(Fx.class).initMain(UiUtil.startup(stage, () -> {
                MainApplicationPane p = new MainApplicationPane();

                p.getButtonOne().setOnAction(e -> saft.build(p).fx().show(FxWindow.class));
                p.getButtonTwo().setOnAction(e -> saft.build(p).fxml().show(FxmlController.class));

                return p;
            }));

        }

        @Override
        public void stop() throws Exception {
            saft.shutdown();
            container.close();
        }

        @Override
        public void init() throws Exception {
            SeContainerInitializer ci = SeContainerInitializer.newInstance();
            ci.addPackages(SaftCdi.class);
            ci.addPackages(BasicApplicationController.class);
            ci.addPackages(true, Saft.class);
            ci.disableDiscovery();
            container = ci.initialize();
            Instance<Object> instance = container.getBeanManager().createInstance();
            saft = instance.select(Saft.class).get();

        }

    }

    public static void main(String[] args) {
        Application.launch(SaftCdiApplication.class, args);
    }

}
