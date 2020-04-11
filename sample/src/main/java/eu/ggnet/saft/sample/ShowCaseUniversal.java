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

import java.lang.ref.WeakReference;
import java.util.*;

import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.FxCore;
import eu.ggnet.saft.sample.support.*;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

/**
 *
 * @author oliver.guenther
 */
public class ShowCaseUniversal {

    public final static String ONCE = "Once";

    public final static String ONCE_WITH_SELF_CLOSER = "Once with SeflCloser";

    public final static String ONCE_FXML = "Once Fxml";

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

    protected final List<Smenu> MENUS;

    public ShowCaseUniversal() {
        UiCore.global().addFx(ONCE, () -> new PaneAsFrame());
        UiCore.global().addFx(ONCE_WITH_SELF_CLOSER, () -> new PaneAsFrameWithSelfCloser());
        UiCore.global().addFxml(ONCE_FXML, BasicApplicationController.class);

        MENUS = Arrays.asList(
                menu("SwingDialogs",
                        item("Once", () -> Ui.build().swing().show(() -> new PanelOnceDialog())),
                        item("Multiple : 1", () -> Ui.build().once(true).title("UnitViewer: 1").swing().show(() -> new UnitViewer())),
                        item("Multiple : 2", () -> Ui.build().once(true).title("UnitViewer: 2").swing().show(() -> new UnitViewer())),
                        item("Multiple : 3 , with precall", () -> Ui.build().swing().show(() -> "Das ist der Riesentext für Unit 3", () -> new UnitViewer())),
                        item("SelfCloser", () -> Ui.build().swing().show(() -> new PanelWithSelfCloser()))
                ),
                menu("SwingFrames",
                        item("Once Frame with ClosedListener", () -> Ui.build().swing().show(() -> new PanelAsFrame()))
                ),
                menu("JavaFxDialogs",
                        item("Once + Store Location", () -> Ui.build().fx().show(() -> new SimplePane())),
                        item("Mutiple 1 with Title + Store Location", () -> Ui.build().title("SimplePane: 1").fx().show(() -> new SimplePane())),
                        item("Mutiple 2 with Title + Store Location", () -> Ui.build().title("SimplePane: 2").fx().show(() -> new SimplePane())),
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
                        item("Once", () -> UiCore.global().show(ONCE)),
                        item("Once With Self Closer", () -> UiCore.global().show(ONCE_WITH_SELF_CLOSER)),
                        item("Once Fxml", () -> UiCore.global().show(ONCE_FXML))
                ),
                menu("Extras",
                        item("List JavaFx Icons", () -> listAllIcons(FxCore.ACTIVE_STAGES.values()))
                )
        );
    }

    private Smenu menu(String label, Sitem... items) {
        return new Smenu(label, items);
    }

    private Sitem item(String key, Runnable value) {
        return new Sitem(key, value);
    }

    private void listAllIcons(Collection<WeakReference<Stage>> stages) {
        stages.stream()
                .map(WeakReference::get)
                .filter(s -> s != null)
                .flatMap((Stage t) -> t.getIcons().stream().map(i -> t.toString() + "|" + i.toString()))
                .forEach(System.out::println);
    }

    public List<Smenu> menu() {
        return MENUS;
    }

}
