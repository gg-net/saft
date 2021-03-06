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

import java.awt.BorderLayout;

import javax.swing.*;

import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.Title;
import eu.ggnet.saft.sample.support.MainPanelAddButtons;
import eu.ggnet.saft.sample.support.ShowCaseUniversal;
import eu.ggnet.saft.sample.support.ShowCaseUniversal.Sitem;
import eu.ggnet.saft.sample.support.ShowCaseUniversal.Smenu;

/**
 *
 * @author oliver.guenther
 */
public class ShowCaseSwing {

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

    public static void main(String[] args) {
        UiCore.startSwing(() -> new SwingPanel());
        ShowCaseUniversal.registerGlobals();
        UiCore.global().core().captureMode(true);
    }

}
