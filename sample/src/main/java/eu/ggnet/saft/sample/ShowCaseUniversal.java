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

import java.util.*;

import javafx.scene.control.Alert;
import javafx.stage.Modality;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.sample.support.*;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

/**
 *
 * @author oliver.guenther
 */
public class ShowCaseUniversal {

    public final static String ONCE_JPANEL_CLASS = "ONCE_JP_C";

    public final static String ONCE_JPANEL_SUPPLIER = "ONCE_JP_S";

    public final static String ONCE_PANE_CLASS = "ONCE_P_C";

    public final static String ONCE_PANE_SUPPLIER = "ONCE_P_S";

    public final static String ONCE_FXML = "ONCE_FXML";

    private final static String[] NAMES = {"Hans", "Klaus", "Horst", "Charlotte", "Caroline", "Ivet"};

    private final static Random R = new Random();

    public static class Sitem {

        public Sitem(String key, Runnable value) {
            this.key = key;
            this.value = value;
        }

        public final String key;

        public final Runnable value;

    }

    public static class Smenu {

        public Smenu(String name, List<Sitem> items) {
            this.name = name;
            this.items = items;
        }

        public Smenu(String name, Sitem... items) {
            this.name = name;
            this.items = Arrays.asList(items);
        }

        public final String name;

        public final List<Sitem> items;
    }

//        UiCore.global().registerOnceFx(ONCE, () -> new PaneAsFrame());
//        UiCore.global().registerOnceFx(ONCE_WITH_SELF_CLOSER, PaneAsFrameWithSelfCloser.class);
//        UiCore.global().registerOnceFxml(ONCE_FXML, BasicApplicationController.class);
    // @RequestScoped, e.g. to observer events
//        class PaneSupplier implements Supplier<BorderPane> {
//
//            /*
//            @Inject
//            Remoteconnector mc
//             */
//            @Override
//            public BorderPane get() {
//                return null;
//            }
//
//        }

    /*
        Some Ideas
        - button ist eine referenz zum parent
        - Im injectfall muss eine Referenz zu Instance<Object> mit dazu. Damit am Ende Dinge in eval und show auf Cdi Features zurückgreifen können.


        @Inject
        UiBuilder ui;   oder   UiBuilder ui = Ui.build();

        ui.title("Blub").fxml(button).show(() -> new MyPane());

        ui.fx(button).show(MyPane.CdiSupplier.class);

        See later:
        Ui.osOpen(). -> If it can be transparent transferd to mobile and jpro.one.
        Ui.fileChooser() -> See, what is possible.in gluon and jpro. And move to uituil.
        In der DW: Alle direkten Ui.handle Blöcke anpassen mit SimpleBackgroundProgess.global().activity(false);
        Review UiCore.shutdown. e.g. Do we want to shutdwown the executorservice if we shutdown one saft.


     */
    private static Smenu menu(String label, Sitem... items) {
        return new Smenu(label, items);
    }

    private static Sitem item(String key, Runnable value) {
        return new Sitem(key, value);
    }

    public static void registerGlobals() {
        UiCore.global().registerOnceSwing(ONCE_JPANEL_CLASS, PanelAsOnceFrame.class);
        UiCore.global().registerOnceSwing(ONCE_JPANEL_SUPPLIER, () -> new PanelAsOnceFrame());
        UiCore.global().registerOnceFx(ONCE_PANE_CLASS, PaneAsFrameWithSelfCloser.class);
        UiCore.global().registerOnceFx(ONCE_PANE_SUPPLIER, () -> new PaneAsFrameWithSelfCloser());
        UiCore.global().registerOnceFxml(ONCE_FXML, BasicApplicationController.class);
    }

    public static List<Smenu> menu() {
        return Arrays.asList(
                menu("SwingDialogs",
                        item("JPanel via Supplier", () -> Ui.build().title("UnitViewer: 1").swing().show(() -> new UnitViewer())),
                        item("JPanel via Class", () -> Ui.build().title("UnitViewer: 2").swing().show(UnitViewer.class)),
                        item("JPanel , with precall", () -> Ui.build().swing().show(() -> "Das ist der Riesentext für Unit 3", () -> new UnitViewer())),
                        item("JPanel with SelfCloser", () -> Ui.build().swing().show(() -> new PanelWithSelfCloser()))
                ),
                menu("SwingFrames",
                        item("Once via Class", () -> UiCore.global().showOnce(ONCE_JPANEL_CLASS)),
                        item("Once via Supplier", () -> UiCore.global().showOnce(ONCE_JPANEL_SUPPLIER)),
                        item("Frame with ClosedListener via Supplier", () -> Ui.build().swing().show(() -> new PanelAsFrame())),
                        item("Frame with ClosedListener via Class", () -> Ui.build().swing().show(PanelAsFrame.class))
                ),
                menu("JavaFxDialogs",
                        item("Once + Store Location", () -> Ui.build().fx().show(() -> new SimplePane())),
                        item("Mutiple 1 with Title + Store Location via Supplier", () -> Ui.build().title("SimplePane: 1").fx().show(() -> new SimplePane())),
                        item("Mutiple 2 with Title + Store Location via Class", () -> Ui.build().title("SimplePane: 2").fx().show(SimplePane.class)),
                        item("HtmlPane", () -> Ui.build().fx().show(() -> "<h1>Ueberschrift</h1>", () -> new HtmlPane())),
                        item("Once InputPane via Fxml",
                                () -> Ui.exec(() -> {
                                    Ui.build().fxml().eval(SimpleFxmlController.class).opt().ifPresent(t -> Ui.build().alert().message("Ok pressed with Input: " + t).show());
                                })
                        ),
                        item("Ui.build.dialog with javfx Alert",
                                () -> Ui.exec(() -> {
                                    Ui.build().dialog().eval(() -> new Alert(CONFIRMATION, "Bitte eine Knopf drücken")).opt().ifPresent(t -> Ui.build().alert().message("Result: " + t).show());
                                })
                        ),
                        item("Ui.build.dialog with javfx Alert (window modal)",
                                () -> Ui.exec(() -> {
                                    Ui.build().modality(Modality.WINDOW_MODAL).dialog().eval(() -> new Alert(CONFIRMATION, "Bitte eine Knopf drücken")).opt().ifPresent(t -> Ui.build().alert().message("Ok pressed with Input: " + t).show());
                                })
                        ),
                        item("TitlePropertyChange and Random Consumer", () -> Ui.build().fx().show(() -> NAMES[R.nextInt(NAMES.length)], () -> new TitlePropertyPane())),
                        item("OnceInput with Optional Result handling",
                                () -> Ui.exec(() -> {
                                    Ui.build().fx().eval(() -> "Preload", () -> new OnceInputPane()).opt().ifPresent(r -> Ui.build().alert("Eingabe war:" + r));
                                })
                        ),
                        item("OnceInput with CompletableFuture Result handling",
                                () -> Ui.build().fx().eval(() -> "Preload", () -> new OnceInputPane()).cf()
                                        .thenAccept(r -> Ui.build().alert("Eingabe war:" + r)).handle(Ui.handler())),
                        item("Dialog of Dialogs", () -> Ui.build().fx().show(() -> new DialogOfDialogs()))
                ),
                menu("JavaFxFrames",
                        item("Frame", () -> Ui.build().fx().show(() -> new PaneAsFrame())),
                        item("Frame With Self Closer via Supplier", () -> Ui.build().fx().show(() -> new PaneAsFrameWithSelfCloser())),
                        item("Frame With Self Closer via Class", () -> Ui.build().fx().show(PaneAsFrameWithSelfCloser.class)),
                        item("Frame Fxml", () -> Ui.build().fxml().show(BasicApplicationController.class)),
                        item("Once Pane via Class", () -> UiCore.global().showOnce(ONCE_PANE_CLASS)),
                        item("Once Pane via Supplier", () -> UiCore.global().showOnce(ONCE_PANE_SUPPLIER)),
                        item("Once Fxml", () -> UiCore.global().showOnce(ONCE_FXML))
                )
        );
    }

}
