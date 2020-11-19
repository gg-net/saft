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
package eu.ggnet.saft.sample.cdi.swing;

import java.awt.BorderLayout;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.swing.*;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.Title;
import eu.ggnet.saft.sample.support.*;
import eu.ggnet.saft.sample.support.ShowCaseUniversal.Sitem;
import eu.ggnet.saft.sample.support.ShowCaseUniversal.Smenu;

/**
 *
 * @author oliver.guenther
 */
public class ShowCaseCdiSwing {

    @Title("ShowCase Swing")
    public static class SwingPanel extends JPanel {

        public SwingPanel() {
            MainPanelAddButtons main = new MainPanelAddButtons();
            for (Smenu smenu : ShowCaseUniversal.menu()) {
                JMenu submenu = new JMenu(smenu.name);
                for (Sitem item : smenu.items) {
                    JMenuItem menuItem = new JMenuItem(item.key);
                    menuItem.addActionListener((e) -> item.value.run());
                    submenu.add(menuItem);
                }
                main.getMenuBar().add(submenu);
            }
            setLayout(new BorderLayout());
            add(main);
        }
    }

    public void start(final SeContainer container) {
        UiCore.startSwing(() -> new SwingPanel());
        UiCore.global().addOnShutdown(() -> {
            if ( container.isRunning() ) {
                // Shutdown the global executor.
//            container.getBeanManager().createInstance().select(ExecutorManager.class).get().shutdown();
                container.close();
            }
        });
        ShowCaseUniversal.registerGlobals();
    }

    public static void main(String[] args) {
        SeContainerInitializer ci = SeContainerInitializer.newInstance();
        ci.addPackages(ShowCaseCdiSwing.class);
        ci.addPackages(BasicApplicationController.class);
        ci.addPackages(true, Saft.class);
        ci.disableDiscovery();
        SeContainer container = ci.initialize();
        Instance<Object> instance = container.getBeanManager().createInstance();
        instance.select(ShowCaseCdiSwing.class).get().start(container);
    }

}
