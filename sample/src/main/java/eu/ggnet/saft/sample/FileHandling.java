package eu.ggnet.saft.sample;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.sample.support.MainPanel;

/**
 * Shows a file handling.
 *
 * @author oliver.guenther
 */
public class FileHandling {

    public static void main(String[] args) {
        UiCore.startSwing(() -> new MainPanel());

        Ui.exec(() -> {
            FileOsOpen.open(null).opt().ifPresent(f -> System.out.println("Ok pressed, File: " + f.getAbsolutePath()));
        });

    }

}
