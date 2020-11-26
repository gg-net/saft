/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.tutorial.chapter02;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.impl.Swing;
import eu.ggnet.saft.core.ui.LocationStorage;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 * Typische Swing Applikation - 02.
 *
 * @author oliver.guenther
 */
public class SaftSwing02 {

    public static void main(String[] args) throws InterruptedException, InvocationTargetException {
        JFrame frame = new JFrame();

        UiCore.initGlobal(new Saft(new LocationStorage(), Executors.newCachedThreadPool())); // Optional, der erste Aufruf von UiCore.global() macht das implizit
        UiCore.global().init(new Swing(UiCore.global())); // Starte saft im globalen Modus und initialisiert es mit eim Swing Kern.
        UiCore.global().core(Swing.class).initMain(frame); // Setzt das Hauptfenster, welche als Fallback für Elternfenster verwendet wird.

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        MainApplicationJPanel p = new MainApplicationJPanel();
        frame.getContentPane().add(p, BorderLayout.CENTER);
        p.getButtonOne().addActionListener(e -> {
            EventQueue.invokeLater(() -> {
                JDialog d = new JDialog(frame);
                d.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                d.setLayout(new BorderLayout());
                d.getContentPane().add(new WindowOne(), BorderLayout.CENTER);
                d.setLocationRelativeTo(frame);
                d.pack();
                d.setVisible(true);
            });
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
