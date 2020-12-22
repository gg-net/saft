/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.sample.support;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.swing.*;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.Bind;
import eu.ggnet.saft.core.ui.Bind.Type;
import eu.ggnet.saft.core.ui.Frame;

/**
 *
 * @author oliver.guenther
 */
@Frame
public class FramePanel extends JPanel {

    private final Logger L = LoggerFactory.getLogger(FramePanel.class);

    @Bind(value = Type.SHOWING)
    private final BooleanProperty showingProperty = new SimpleBooleanProperty();

    public FramePanel() {
        showingProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            L.info("showingProperty={}", newValue);
        });
        JButton e = new JButton("Exception");
        e.addActionListener(ex -> {
            CompletableFuture.runAsync(() -> {
                throw new CompletionException(new IOException("Eine Exception"));
            }).handle(UiCore.global().handler(e));
        });
        add(new JLabel("Obendrüber"));
        add(e);
    }

}
