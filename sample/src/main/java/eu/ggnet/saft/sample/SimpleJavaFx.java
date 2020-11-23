/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import eu.ggnet.saft.core.*;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.sample.support.HtmlPane;

import static eu.ggnet.saft.core.ui.Bind.Type.SHOWING;
import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;

/**
 *
 * @author oliver.guenther
 */
public class SimpleJavaFx {

    public static String longServerCall() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            //
        }
        return "Nachicht vom Server";
    }

    public static void serverOperationMitException(String s) {
        throw new RuntimeException("Exception am Server");
    }

    public static class MyPane extends FlowPane {

        private final Saft saft;

        public MyPane(Saft saft) {
            this.saft = saft;
            Button b = new Button("Show HtmlPane");
            b.setOnAction(e -> {
                saft.build(this).fx().show(() -> new HtmlPane());
            });
            Button b2 = new Button("Show TestPane");
            b2.setOnAction(e -> {
                saft.build(b).fx().eval(() -> longServerCall(), () -> new TestPane(saft)).
                        cf().thenApply(s -> {
                            saft.build(b).alert(s);
                            System.out.println("Zu");
                            return s;
                        }).thenAccept(s -> serverOperationMitException(s)).handle(saft.handler());

            });

            Button b3 = new Button("Show TestPane2");
            b3.setOnAction(e -> {
                Ui.exec(() -> {
                    try {
                        saft.build(b).fx().eval(() -> longServerCall(), () -> new TestPane(saft)).
                                opt().ifPresent(s -> {
                                    saft.build(b).alert(s);
                                    System.out.println("Zu");
                                    serverOperationMitException(s);
                                });
                    } catch (Exception ex) {
                        saft.handle(ex);
                    }
                });
            });

            getChildren().addAll(b, b2, b3);
        }

    }

    @StoreLocation
    public static class TestPane extends BorderPane implements Consumer<String>, ResultProducer<String> {

        private final Saft saft;

        TextArea area;

        private boolean ok = false;

        @Bind(TITLE)
        private final StringProperty title = new SimpleStringProperty();

        @Bind(SHOWING)
        private final BooleanProperty show = new SimpleBooleanProperty();

        public TestPane(Saft saft) {
            this.saft = saft;
            area = new TextArea("Hallo Welt");
            title.bind(area.textProperty());
            setCenter(area);

            Button s = new Button("Status");
            s.setOnAction(e -> saft.build(area).alert().message("Status is: ").nl("" + show.get()).nl("von Olli").show(AlertType.WARNING));

            Button o = new Button("Ok");
            o.setOnAction(e -> {
                ok = true;
                show.set(false);
            });

            Button b = new Button("Close");
            b.setOnAction(e -> show.set(false));
            setBottom(new FlowPane(s, o, b));
        }

        @Override
        public void accept(String t) {
            area.setText(t);
        }

        @Override
        public String getResult() {
            if ( ok ) return area.getText();
            return null;
        }

    }

    public static class SimpleJavaFxApplication extends Application {

        private Saft saft;

        @Override
        public void start(Stage stage) throws Exception {
            saft = new Saft(new LocationStorage(), Executors.newCachedThreadPool());

            UiCore.initGlobal(saft);

            saft.init(new Fx(saft));
            MyPane p = new MyPane(saft);
            stage.setScene(new Scene(p));
            saft.core(Fx.class).initMain(stage);

            stage.show();
        }

        @Override
        public void stop() throws Exception {
            saft.shutdown();
        }

    }

    public static void main(String[] args) {
        Application.launch(SimpleJavaFxApplication.class, args);
    }

}
