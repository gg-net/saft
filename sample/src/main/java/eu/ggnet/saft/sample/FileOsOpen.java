package eu.ggnet.saft.sample;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

import javafx.application.Platform;
import javafx.stage.FileChooser;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.builder.Result;
import eu.ggnet.saft.sample.support.MainPanel;

/**
 * Shows a file handling.
 *
 * @author oliver.guenther
 */
public class FileOsOpen {

    public static void main(String[] args) {
        UiCore.startSwing(() -> new MainPanel());

        // New Stype
        Ui.exec(() -> {
            open("Bitte Datei auswählen, die das Betriebsystem öffnen kann").opt()
                    .ifPresent(file -> osOpen(file));
        });

    }

    public static Result<File> open(String title) {
        return new Result<>(CompletableFuture.supplyAsync(() -> {
            FileChooser fileChooser = new FileChooser();
            if ( title == null ) fileChooser.setTitle("Open File");
            else fileChooser.setTitle(title);
            File result = fileChooser.showOpenDialog(null);
            if ( result == null ) throw new CancellationException();
            return result;
        }, Platform::runLater).thenApplyAsync(r -> r, UiCore.global().executorService())); // the last Apply is for the thread change only
    }

    /**
     * Wrapper for Desktop.getDesktop().open() with UI Exception handling
     *
     * @param file a file to open via ui.
     * @return true if operation was successful, otherwise false. Can be used if the following operations should happen.
     */
    public static boolean osOpen(File file) {
        try {
            Desktop.getDesktop().open(file);
            return true;
        } catch (IOException e) {
            UiCore.global().handle(e);
        }
        return false;
    }

    public static <T> T dispatchFx(Callable<T> callable) throws RuntimeException {
        try {
            FutureTask<T> futureTask = new FutureTask<>(callable);
            final CountDownLatch cdl = new CountDownLatch(1);
            if ( Platform.isFxApplicationThread() ) {
                futureTask.run();
                cdl.countDown();
            } else {
                Platform.runLater(() -> {
                    futureTask.run();
                    cdl.countDown();
                });
            }
            cdl.await();
            return futureTask.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

}
