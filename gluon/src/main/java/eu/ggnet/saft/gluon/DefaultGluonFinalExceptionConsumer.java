/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.control.Dialog;

/**
 * A simple but nice exception consumer for gluon, using a fullscreen Dialog.
 *
 * @author oliver.guenther
 */
public class DefaultGluonFinalExceptionConsumer implements Consumer<Throwable> {

    private final static Logger L = LoggerFactory.getLogger(DefaultGluonFinalExceptionConsumer.class);

    @Override
    public void accept(Throwable t) {
        // TODO: Add some Warning Icon.
        CompletableFuture.runAsync(() -> {
            Dialog<Void> d = new Dialog<>(true);
            d.setTitleText(t.getClass().getSimpleName());

            TextField messageField = new TextField(t.getMessage());
            messageField.setEditable(false);

            String stacktrace = "";
            try (StringWriter w = new StringWriter();
                    PrintWriter p = new PrintWriter(w)) {
                t.printStackTrace(p);
                stacktrace = w.toString();
            } catch (IOException ex) {
                L.warn("Cound not print stacktrace in Exceptionconsumer", ex);
            }

            TextArea stackTraceArea = new TextArea(stacktrace);
            stackTraceArea.setEditable(false);

            BorderPane pane = new BorderPane(stackTraceArea);
            pane.setTop(messageField);
            L.error("Exception catched via {}, message: {}", this.getClass().getSimpleName(), t.getMessage(), t);
            d.setContent(pane);
            d.showAndWait();

        }, Platform::runLater);
    }

}
