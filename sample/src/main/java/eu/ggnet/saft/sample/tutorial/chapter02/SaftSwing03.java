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

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;

/**
 * Typische Swing Applikation - 03.
 *
 * @author oliver.guenther
 */
public class SaftSwing03 {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        JFrame frame = new JFrame();

        UiCore.continueSwing(frame); // Abkürzung für den Start.

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        MainApplicationJPanel p = new MainApplicationJPanel();
        frame.getContentPane().add(p, BorderLayout.CENTER);
        p.getButtonOne().addActionListener(e -> {
            /*
            EventQueue.invokeLater(() -> {
                JDialog d = new JDialog(frame);
                d.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                d.setLayout(new BorderLayout());
                d.getContentPane().add(new WindowOne(), BorderLayout.CENTER);
                d.setLocationRelativeTo(frame);
                d.pack();
                d.setVisible(true);
            });
             */
            Ui.build(p).swing().show(() -> new WindowOne()); // Nimmt all die Arbeit ab.
        });

        new JFXPanel(); // Implizit init javafx
        p.getButtonTwo().addActionListener(e -> {
            Platform.runLater(() -> {
                Stage s = new Stage();
                // s.initOwner();  Wie jetzt das tun ?
                // Wie die Modalität oder die relative Position setzen ?
                s.setScene(new Scene(new WindowTwo()));
                s.show();
            });
        });

        frame.pack();
        frame.setLocationByPlatform(true);
        EventQueue.invokeAndWait(() -> {
            frame.setVisible(true);
        });
    }

}
