/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter02;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;

/**
 * Typische Swing Applikation - 04.
 *
 * @author oliver.guenther
 */
public class SaftSwing04 {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        JFrame frame = new JFrame();

        UiCore.continueSwing(frame);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        MainApplicationJPanel p = new MainApplicationJPanel();
        frame.getContentPane().add(p, BorderLayout.CENTER);
        p.getButtonOne().addActionListener(e -> Ui.build(p).swing().show(() -> new WindowOne()));

        p.getButtonTwo().addActionListener(e -> {
            /*
            Platform.runLater(() -> {
                Stage s = new Stage();
                // s.initOwner();  Wie jetzt das tun ?
                // Wie die Modalität oder die relative Position setzen ?
                s.setScene(new Scene(new WindowTwo()));
                s.show();
            });
             */
            Ui.build(p).fx().show(() -> new WindowTwo()); // Jetzt mit integration in die Swingwelt.
        });

        frame.pack();
        frame.setLocationByPlatform(true);
        EventQueue.invokeAndWait(() -> {
            frame.setVisible(true);
        });
    }

}
