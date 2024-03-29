/*
 * Swing and JavaFx Together (Saft)
 * Copyright (C) 2020  Oliver Guenther <oliver.guenther@gg-net.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 with
 * Classpath Exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with Classpath Exception along with this program.
 */
package eu.ggnet.saft.core.impl;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Core;
import eu.ggnet.saft.core.impl.UiParameter.Builder;
import eu.ggnet.saft.core.ui.Bind.Type;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.PreBuilder;

import static eu.ggnet.saft.core.UiUtil.exceptionRun;
import static eu.ggnet.saft.core.impl.UiParameter.Type.*;
import static eu.ggnet.saft.core.ui.Bind.Type.*;

/**
 *
 * @author oliver.guenther
 */
public abstract class AbstractCore {

    /**
     * A simple wrapper for the name generation to discover icons.
     * The name is build like this:
     * <ol>
     * <li>If the referencing class ends with one of {@link IconConfig#VIEW_SUFFIXES} remove that part</li>
     * <li>Generate name by permuting "Rest of referencing class
     * name"{@link IconConfig#ICON_SUFFIXES}{@link IconConfig#SIZE_SUFFIXES}{@link IconConfig#FILES}</li>
     * <li></li>
     * <li></li>
     * </ol>
     */
    public final static class IconConfig {

        private final static java.util.List<String> VIEW_SUFFIXES = Arrays.asList("Controller", "View", "ViewCask", "Presenter");

        private final static java.util.List<String> ICON_SUFFIXES = Arrays.asList("Icon");

        private final static java.util.List<String> SIZE_SUFFIXES = Arrays.asList("", "_016", "_024", "_032", "_048", "_064", "_128", "_256", "_512");

        private final static java.util.List<String> FILES = Arrays.asList(".png", ".jpg", ".gif");

        public static Set<String> possibleIcons(Class<?> clazz) {
            String head = VIEW_SUFFIXES
                    .stream()
                    .filter(s -> clazz.getSimpleName().endsWith(s))
                    .map(s -> clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - s.length()))
                    .findFirst()
                    .orElse(clazz.getSimpleName());

            return ICON_SUFFIXES.stream()
                    .map(e -> head + e)
                    .flatMap(h -> SIZE_SUFFIXES.stream().map(e -> h + e))
                    .flatMap(h -> FILES.stream().map(e -> h + e))
                    .collect(Collectors.toCollection(() -> new TreeSet<>()));
        }

    }

    private final static Logger L = LoggerFactory.getLogger(AbstractCore.class);

    /**
     * Extract a the {@link StringProperty} from the supplied instance, which is annotated by {@link Bind} and {@link Type#TITLE}.
     *
     * @param instance the instance to extract from.
     * @return returns an optional StringProperty.
     */
    // HINT: Core usage, but also in UiUtil,
    public static Optional<StringProperty> findTitleProperty(Object instance) {
        try {
            List<Field> fields = allDeclaredFields(instance.getClass());
            L.debug("findTitleProperty() inspecting fields for Bind(TITLE): {}", fields);
            for (Field field : fields) {
                Bind bind = field.getAnnotation(Bind.class);
                if ( bind != null && bind.value() == TITLE ) {
                    L.debug("findTitleProperty() found Bind(TITLE), extrating property");
                    field.setAccessible(true);
                    return Optional.ofNullable((StringProperty)field.get(instance)); // Cast is safe, Look at the BindingProcessor.
                }
            }
        } catch (IllegalAccessException e) {
            L.error("findTitleProperty() Exception on field.get()", e);
        }
        return Optional.empty();
    }

    /**
     * Extract a the {@link StringProperty} from the supplied instance, which is annotated by {@link Bind} and {@link Type#TITLE}.
     *
     * @param instance the instance to extract from.
     * @return returns an optional StringProperty.
     */
    // HINT: Core usage, but also in UiUtil,
    public static Optional<ObservableList<Image>> findIcons(Object instance) {
        try {
            List<Field> fields = allDeclaredFields(instance.getClass());
            L.debug("findIcons() inspecting fields for Bind(ICONS): {}", fields);
            for (Field field : fields) {
                Bind bind = field.getAnnotation(Bind.class);
                if ( bind != null && bind.value() == ICONS ) {
                    L.debug("findIcons() found Bind(ICONS), extrating property");
                    field.setAccessible(true);
                    return Optional.ofNullable((ObservableList<Image>)field.get(instance)); // Cast is safe, Look at the BindingProcessor.
                }
            }
        } catch (IllegalAccessException e) {
            L.error("findIcons() Exception on field.get()", e);
        }
        return Optional.empty();
    }

    protected abstract Optional<Callback<Class<?>, Object>> initializer();

    // TODO: keep as instance method, for future cdi usage.
    protected Object createInstance(Core.In<?, ?> in) {
        Core.In<Object, Object> i2 = (Core.In<Object, Object>)in;
        return i2.supplier().map(Supplier::get).orElseGet(() -> {
            try {
                if ( initializer().isPresent() ) return initializer().get().call(i2.clazz());
                return i2.clazz().newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException("Error during " + i2.clazz().getName() + ".newInstance(), probablly no zero argument constructor available", ex);
            }
        });

    }

    protected UiParameter produceJPanel(Core.In<?, ?> in, UiParameter param) {
        L.debug("produceJPanel(in={})", in);
        if ( selectType(in) != SWING )
            throw new IllegalArgumentException("produceJPanel(" + in + ") used illegal, as selected Type must be " + SWING + " but was " + selectType(in));
        JPanel panel = (JPanel)createInstance(in); // Safe cast as of line above.
        Builder b = param.toBuilder().rootClass(panel.getClass()).jPanel(panel);
        b.titleProperty(findTitleProperty(panel));
        b.showingProperty(findShowingProperty(panel));
        b.icons(findIcons(panel));
        return b.build();
    }

    protected UiParameter producePane(Core.In<?, ?> in, UiParameter param) {
        L.debug("producePane(in={})", in);
        if ( selectType(in) != FX )
            throw new IllegalArgumentException("producePane(" + in + ") used illegal, as selected Type must be " + FX + " but was " + selectType(in));
        Pane pane = (Pane)createInstance(in);
        Builder b = param.toBuilder().rootClass(pane.getClass()).pane(pane);
        b.titleProperty(findTitleProperty(pane));
        b.icons(findIcons(pane));
        b.showingProperty(findShowingProperty(pane));
        return b.build();
    }

    protected UiParameter produceFxml(Core.In<?, ?> in, UiParameter param) {
        L.debug("produceFxml(in={})", in);
        if ( selectType(in) != FXML )
            throw new IllegalArgumentException("produceFxml(" + in + ") used illegal, as selected Type must be " + FXML + " but was " + selectType(in));
        try {
            Class<FxController> controllerClazz = (Class<FxController>)in.clazz();  // Cast is a shortcut.
            FXMLLoader loader = initializer().isPresent()
                    ? new FXMLLoader(FxController.loadView(controllerClazz), null, null, initializer().get(), StandardCharsets.UTF_8)
                    : new FXMLLoader(FxController.loadView(controllerClazz));
            loader.load();
            Objects.requireNonNull(loader.getController(), "No controller based on " + controllerClazz + ". Controller set in Fxml ?");
            Pane pane = loader.getRoot();
            FxController controller = loader.getController();
            Builder b = param.toBuilder().pane(pane).fxController(controller).rootClass(in.clazz());
            b.showingProperty(findShowingProperty(controller));
            b.titleProperty(findTitleProperty(controller));
            b.icons(findIcons(controller));
            return b.build();
        } catch (IOException ex) {
            throw new CompletionException(ex);
        }
    }

    protected UiParameter produceDialog(Core.In<?, ?> in, UiParameter parm) {
        L.debug("produceDialog(in={})", in);
        if ( selectType(in) != DIALOG )
            throw new IllegalArgumentException("produceFxml(" + in + ") used illegal, as selected Type must be " + DIALOG + " but was " + selectType(in));
        javafx.scene.control.Dialog<?> dialog = (javafx.scene.control.Dialog<?>)createInstance(in);
        // Dialog is special, allways use the title property.
        return parm.toBuilder().rootClass(dialog.getClass()).titleProperty(dialog.titleProperty()).dialog(dialog).pane(dialog.getDialogPane()).build();
    }

    protected UiParameter optionalRunPreProducer(UiParameter in, Optional<Callable<?>> optPreProducer) {
        if ( !optPreProducer.isPresent() ) return in;
        return in.toBuilder().nullablePreResult(exceptionRun(optPreProducer.get())).preResultProduced(true).build();
    }

    protected UiParameter optionalConsumePreProducer(UiParameter in) {
        if ( in.preResultProduced() && (in.type().selectRelevantInstance(in) instanceof Consumer) ) {
            ((Consumer)in.type().selectRelevantInstance(in)).accept(in.preResult().orElse(null));
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
        L.debug("init(preBuilder={}, type={})", preBuilder, type);
        return UiParameter.fromPreBuilder(preBuilder).type(type).build();
    }

    protected void optionalWaitOnSwing(Optional<Window> optWindow) {
        try {
            if ( !optWindow.isPresent() ) return; // don't wait if no window is supplied.
            Window window = optWindow.get();
            if ( !window.isVisible() ) {
                L.debug("Wait on non visible window called, continue without latch");
                return;
            }

            final CountDownLatch latch = new CountDownLatch(1);
            // Removes on close.
            window.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                    latch.countDown();
                }

            });
            latch.await();
        } catch (InterruptedException ex) {
            throw new CompletionException(ex);
        }
    }

    protected <T> T waitAndProduceResult(UiParameter in) {
        if ( !(in.type().selectRelevantInstance(in) instanceof ResultProducer || in.type().selectRelevantInstance(in) instanceof Dialog) ) {
            throw new IllegalStateException("Calling Produce Result on a none ResultProducer. Try show instead of eval. Type: " + in.type());
        }
        // Only done in swing mode implictly, cause only in swing mode, window will not be null.
        optionalWaitOnSwing(in.window());
        if ( in.type().selectRelevantInstance(in) instanceof ResultProducer ) {
            T result = ((ResultProducer<T>)in.type().selectRelevantInstance(in)).getResult();
            if ( result == null ) throw new CancellationException();
            return result;
        } else {
            T result = ((Dialog<T>)in.type().selectRelevantInstance(in)).getResult();
            if ( result == null ) {
                throw new CancellationException("result == null");
            }
            if ( result == ButtonType.CANCEL || result == ButtonType.CLOSE || result == ButtonType.NO ) {
                throw new CancellationException("result == " + result);
            }
            return result;
        }
    }

    private static List<Field> allDeclaredFields(Class<?> clazz) {
        if ( clazz == null ) return Collections.emptyList();
        List<Field> as = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        as.addAll(allDeclaredFields(clazz.getSuperclass()));
        return as;
    }

    private Optional<BooleanProperty> findShowingProperty(Object o) {
        try {
            List<Field> fields = allDeclaredFields(o.getClass());
            L.debug("findShowingProperty() inspecting fields for Bind(SHOWING): {}", fields);
            for (Field field : fields) {
                Bind bind = field.getAnnotation(Bind.class);
                if ( bind != null && bind.value() == SHOWING ) {
                    L.debug("findShowingProperty() found Bind(SHOWING), extrating property");
                    field.setAccessible(true);
                    return Optional.ofNullable((BooleanProperty)field.get(o)); // Cast is safe, Look at the BindingProcessor.
                }
            }
        } catch (IllegalAccessException e) {
            L.error("findShowingProperty() Exception on field.get()", e);
        }
        return Optional.empty();
    }

}
