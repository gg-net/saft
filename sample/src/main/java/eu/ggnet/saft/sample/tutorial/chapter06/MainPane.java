package eu.ggnet.saft.sample.tutorial.chapter06;

import javax.swing.JPanel;

import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import eu.ggnet.saft.core.Ui;

public class MainPane extends FlowPane {

    public MainPane() {
        
        Button button = new Button("FxPane with standard title");
        Button button1 = new Button("FxPane with title - prebuilder");
        Button button2 = new Button("FxPane with title - Annotation");
        Button button3 = new Button("FxPane with title - ProperyAnnotation");
        Button button4 = new Button("SwingPanel with title - prebuilder");
        Button button5 = new Button("SwingPanel with title - Annotation");

        
        button.setOnAction(e -> {
        //Hier werden Panes erstellt die das standartverhalten wiedergeben. Als title wird der Name der Klasse verwendet. In diesem Fall "Pane"
            Ui.build().fx().show(Pane.class);
            Ui.build().title("").fx().show(Pane.class);
            Ui.build().title(null).fx().show(Pane.class);
        });
        
        button1.setOnAction(e -> {
            //Hier wird der Titel über den Prebuilder festgelegt
            Ui.build().title("Titel per preBuilder").fx().show(Pane.class);
        });

        button2.setOnAction(e -> {
            //Hier wird der Titel innerhalb der Klasse "FxComponentWithTitleAnnotation" per Annotation fest gesetzt.
            Ui.build().fx().show(FxComponentWithTitleAnnotation.class);
        });

        button3.setOnAction(e -> {
            //Hier wird der Titel an den Konstruktor übergeben und per Annotation an der Property-Eigenschaft an der Stage gesetzt.
            Ui.build().fx().show(() -> new FxComponentWithTitlePropertyAnnotation("Titel per Property binding annotation!"));
        });

        button4.setOnAction(e -> {
            //Hier wird der Titel der Swing-Komponente über den PreBuilder gesetzt
            Ui.build().title("SwingPanel via prebuilder").swing().show(JPanel.class);
        });

        button5.setOnAction(e -> {
            //Hier wird der Titel innerhalb der Klasse "SwingComponentWithTitleAnnotation" per Annotation fest gesetzt.
            Ui.build().swing().show(SwingComponentWithTitleAnnotation.class);
        });

     

        this.getChildren().addAll(button,button1, button2, button3, button4, button5);
    }

}
