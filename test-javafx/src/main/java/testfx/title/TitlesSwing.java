/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.title;

import javax.swing.JButton;
import javax.swing.JPanel;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;

/**
 *
 * @author oliver.guenther
 */
public class TitlesSwing {

    public static void main(String[] args) {

        UiCore.startSwing(() -> {
            JPanel p = new JPanel();
            p.add(button("Java Fx Pane with Title Binding", () -> Ui.build().fx().show(() -> new TitlePane())));
            p.add(button("Java Fxml with Title Binding", () -> Ui.build().fxml().show(TitleController.class)));
            p.add(button("Java Fx Dialog with Title Binding", () -> Ui.build().dialog().eval(() -> {
                TitleDialog titleDialog = new TitleDialog();
                System.out.println("PostConstruct: Dialog.showingProperty().get()=" + titleDialog.showingProperty().get());
                return titleDialog;
            }).cf().thenAccept(s -> System.out.println("PostRun: Dialog.showingProperty().get()=" + s.get())).handle(Ui.handler())));

            p.add(button("Swing Pane with TitlePoperty", () -> Ui.build().swing().show(() -> new TitleJPanel())));
            return p;
        });

    }

    public static JButton button(String title, Runnable runnable) {
        JButton b = new JButton(title);
        b.addActionListener((e) -> runnable.run());
        return b;
    }

}
