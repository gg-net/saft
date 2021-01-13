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

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import eu.ggnet.saft.core.*;
import eu.ggnet.saft.core.impl.DefaultExceptionConsumer;
import eu.ggnet.saft.core.impl.DetailView;
import eu.ggnet.saft.sample.support.BasicApplicationController;
import eu.ggnet.saft.sample.support.FramePanel;

/**
 *
 * @author mirko.schulze
 */
public class ShowOrExecptionDetailViewSwing {

    public static void main(String[] args) {
        JPanel p = new JPanel();
        JButton b1 = new JButton("Show DetailView");
        b1.addActionListener((ActionEvent arg0) -> {
            Ui.build().parent(p).title("Systemfehler").swing()
                    .show(() -> new DetailView(message(), longMessage(), veryLongMessage()));
        });
        p.add(b1);

        JButton b2 = new JButton("Exception over Panel");
        b2.addActionListener((ActionEvent arg0) -> {
            Ui.build(b2).swing().show(FramePanel.class);
        });
        p.add(b2);

        JButton b3 = new JButton("Exception over Fxml");
        b3.addActionListener((ActionEvent arg0) -> {
            Ui.build(b3).fxml().show(BasicApplicationController.class);
        });
        p.add(b3);

        UiCore.continueSwing(UiUtil.startup(() -> p));
        UiCore.global().overwriteFinalExceptionConsumer(new DefaultExceptionConsumer());

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
