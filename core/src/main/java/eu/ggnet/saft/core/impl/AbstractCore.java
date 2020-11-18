/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JPanel;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.builder.*;
import eu.ggnet.saft.core.ui.builder.UiParameter.Builder;

import static eu.ggnet.saft.core.UiUtil.exceptionRun;
import static eu.ggnet.saft.core.ui.FxSaft.loadView;
import static eu.ggnet.saft.core.ui.builder.BuilderUtil.findShowingProperty;
import static eu.ggnet.saft.core.ui.builder.BuilderUtil.findTitleProperty;
import static eu.ggnet.saft.core.ui.builder.UiParameter.Type.*;

/**
 *
 * @author oliver.guenther
 */
public class AbstractCore {

    // TODO: later use optional inject logger.
    protected Logger log() {
        return LoggerFactory.getLogger(AbstractCore.class);
    }

    // TODO: keep as instance method, for future cdi usage.
    protected Object createInstance(Core.In<?, ?> in) {
        Core.In<Object, Object> i2 = (Core.In<Object, Object>)in;
        return i2.supplier().map(Supplier::get).orElseGet(() -> {
            try {
                return i2.clazz().newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException("Error during " + i2.clazz().getName() + ".newInstance(), probablly no zero argument constructor available", ex);
            }
        });

    }

    protected UiParameter produceJPanel(Core.In<?, ?> in, UiParameter param) {
        log().debug("produceJPanel(in={})", in);
        if ( selectType(in) != SWING )
            throw new IllegalArgumentException("produceJPanel(" + in + ") used illegal, as selected Type must be " + SWING + " but was " + selectType(in));
        JPanel panel = (JPanel)createInstance(in); // Safe cast as of line above.
        Builder b = param.toBuilder().rootClass(panel.getClass()).jPanel(panel);
        b.titleProperty(BuilderUtil.findTitleProperty(panel));
        b.showingProperty(BuilderUtil.findShowingProperty(panel));
        return b.build();
    }

    protected UiParameter producePane(Core.In<?, ?> in, UiParameter param) {
        log().debug("producePane(in={})", in);
        if ( selectType(in) != FX )
            throw new IllegalArgumentException("producePane(" + in + ") used illegal, as selected Type must be " + FX + " but was " + selectType(in));
        Pane pane = (Pane)createInstance(in);
        Builder b = param.toBuilder().rootClass(pane.getClass()).pane(pane);
        b.titleProperty(findTitleProperty(pane));
        b.showingProperty(findShowingProperty(pane));
        return b.build();
    }

    protected UiParameter produceFxml(Core.In<?, ?> in, UiParameter param) {
        log().debug("produceFxml(in={})", in);
        if ( selectType(in) != FXML )
            throw new IllegalArgumentException("produceFxml(" + in + ") used illegal, as selected Type must be " + FXML + " but was " + selectType(in));
        try {
            Class<FxController> controllerClazz = (Class<FxController>)in.clazz();  // Cast is a shortcut.
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(loadView(controllerClazz), "No View for " + controllerClazz));
            loader.load();
            Objects.requireNonNull(loader.getController(), "No controller based on " + controllerClazz + ". Controller set in Fxml ?");
            Pane pane = loader.getRoot();
            FxController controller = loader.getController();
            Builder b = param.toBuilder().pane(pane).fxController(controller).rootClass(in.clazz());
            b.showingProperty(findShowingProperty(controller));
            b.titleProperty(findTitleProperty(controller));
            return b.build();
        } catch (IOException ex) {
            throw new CompletionException(ex);
        }
    }

    protected UiParameter produceDialog(Core.In<?, ?> in, UiParameter parm) {
        log().debug("produceDialog(in={})", in);
        if ( selectType(in) != DIALOG )
            throw new IllegalArgumentException("produceFxml(" + in + ") used illegal, as selected Type must be " + DIALOG + " but was " + selectType(in));
        javafx.scene.control.Dialog<?> dialog = (javafx.scene.control.Dialog<?>)createInstance(in);
        // Dialog is special, allways use the title property.
        return parm.toBuilder().rootClass(dialog.getClass()).titleProperty(dialog.titleProperty()).dialog(dialog).pane(dialog.getDialogPane()).build();
    }

    protected UiParameter optionalRunPreProducer(UiParameter in, Optional<Callable<?>> optPreProducer) {
        if ( !optPreProducer.isPresent() ) return in;
        return in.toBuilder().preResult(exceptionRun(optPreProducer.get())).build();
    }

    protected UiParameter optionalConsumePreProducer(UiParameter in) {
        if ( in.preResult().isPresent() && (in.type().selectRelevantInstance(in) instanceof Consumer) ) {
            ((Consumer)in.type().selectRelevantInstance(in)).accept(in.preResult().get());
        }
        return in;
    }

    protected UiParameter.Type selectType(Core.In<?, ?> in) {
        if ( JPanel.class.isAssignableFrom(in.clazz()) ) return SWING;
        if ( Pane.class.isAssignableFrom(in.clazz()) ) return FX;
        if ( javafx.scene.control.Dialog.class.isAssignableFrom(in.clazz()) ) return DIALOG;
        if ( FxController.class.isAssignableFrom(in.clazz()) ) return FXML;
        throw new IllegalArgumentException(Swing.class.getSimpleName() + " does not support " + in.clazz() + " for show or eval (selectType)");
    }

    protected UiParameter init(PreBuilder preBuilder, UiParameter.Type type) {
        log().debug("init(preBuilder={}, type={})", preBuilder, type);
        return UiParameter.fromPreBuilder(preBuilder).type(type).build();
    }

}
