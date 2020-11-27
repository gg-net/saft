/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter02;

import java.lang.reflect.InvocationTargetException;

import eu.ggnet.saft.core.*;

/**
 * Typische Swing Applikation - 05.
 *
 * @author oliver.guenther
 */
public class SaftSwing05 {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        // Verwendung von UiUtil für typische Startverhalten.
        UiCore.continueSwing(UiUtil.startup(() -> {
            MainApplicationJPanel p = new MainApplicationJPanel();

            p.getButtonOne().addActionListener(e -> Ui.build(p).swing().show(() -> new WindowOne()));
            p.getButtonTwo().addActionListener(e -> Ui.build(p).fx().show(() -> new WindowTwo()));

            return p;
        }));

    }

}
