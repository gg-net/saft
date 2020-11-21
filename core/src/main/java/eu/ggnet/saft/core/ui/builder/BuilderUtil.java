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
package eu.ggnet.saft.core.ui.builder;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.swing.*;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Ui;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.impl.Swing;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.UiParameter.Builder;

import static eu.ggnet.saft.core.ui.Bind.Type.SHOWING;
import static eu.ggnet.saft.core.ui.Bind.Type.TITLE;
import static eu.ggnet.saft.core.ui.FxSaft.loadView;

/**
 * Util class for all Builder based work.
 *
 * @author oliver.guenther
 */
public final class BuilderUtil {

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
    private final static class IconConfig {

        private final static java.util.List<String> VIEW_SUFFIXES = Arrays.asList("Controller", "View", "ViewCask", "Presenter");

        private final static java.util.List<String> ICON_SUFFIXES = Arrays.asList("Icon");

        private final static java.util.List<String> SIZE_SUFFIXES = Arrays.asList("", "_016", "_024", "_032", "_048", "_064", "_128", "_256", "_512");

        private final static java.util.List<String> FILES = Arrays.asList(".png", ".jpg", ".gif");

        private static Set<String> possibleIcons(Class<?> clazz) {
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

    private final static Logger L = LoggerFactory.getLogger(BuilderUtil.class);

    private BuilderUtil() {
        // No instances of util classes.
    }

    /**
     * New jframe based on parameters.
     *
     * @param titleProperty
     * @param component
     * @return
     */
    public static JFrame newJFrame(StringProperty titleProperty, JComponent component) {
        JFrame jframe = new JFrame();
        jframe.setName(titleProperty.get());
        jframe.setTitle(titleProperty.get());
        titleProperty.addListener((ObservableValue<? extends String> ob, String o, String n) -> {
            jframe.setName(n);
            jframe.setTitle(n);
        });
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.getContentPane().add(component);
        return jframe;
    }

    /**
     * New JDialog based on parameters.
     *
     * @param swingParent
     * @param titleProperty
     * @param component
     * @param modalityType
     * @return
     */
    public static JDialog newJDailog(Window swingParent, StringProperty titleProperty, JComponent component, ModalityType modalityType) {
        JDialog dialog = new JDialog(swingParent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModalityType(modalityType);
        // Parse the Title somehow usefull.
        dialog.setName(titleProperty.get());
        dialog.setTitle(titleProperty.get());
        titleProperty.addListener((ObservableValue<? extends String> ob, String o, String n) -> {
            dialog.setName(n);
            dialog.setTitle(n);
        });
        dialog.getContentPane().add(component);
        return dialog;
    }

    /**
     * Sets default values.
     *
     * @param <T>
     * @param window                the window to modify
     * @param iconReferenzClass     reference class for icons. see SwingSaft.loadIcons.
     * @param relativeLocationAnker anker for relative location placement, propably also the parent.
     * @param storeLocationClass    class inspected if it has the StoreLocation annotation, probally the panel, pane or controller class.
     * @param windowKey             a string representtation for the internal window manager. Something like controller.getClass + optional id.
     * @return the window instance.
     * @throws IOException If icons could not be loaded.
     */
    public static <T extends Window> T setWindowProperties(UiParameter in, T window, Class<?> iconReferenzClass, Window relativeLocationAnker, Class<?> storeLocationClass, String windowKey) throws IOException { // IO Exeception based on loadIcons
        window.setIconImages(loadAwtImages(iconReferenzClass));
        window.pack();
        window.setLocationRelativeTo(relativeLocationAnker);
        if ( in.isStoreLocation() ) UiCore.global().locationStorage().loadLocation(storeLocationClass, window);
        in.saft().core(Swing.class).add(window);
        // Removes on close.
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Store location.
                if ( in.isStoreLocation() ) UiCore.global().locationStorage().storeLocation(storeLocationClass, window);
            }
        });
        return window;
    }

    public static void wait(Window window) throws InterruptedException, IllegalStateException, NullPointerException {
        Objects.requireNonNull(window, "Window is null");
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
    }

    public static <V extends JPanel> UiParameter produceJPanel(Callable<V> producer, UiParameter parm) {
        try {
            V panel = producer.call();
            L.debug("produceJPanel: {}", panel);
            Builder b = parm.toBuilder().rootClass(panel.getClass()).jPanel(panel);
            b.titleProperty(findTitleProperty(panel));
            b.showingProperty(findShowingProperty(panel));
            return b.build();

        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    public static <V extends Pane> UiParameter producePane(Callable<V> producer, UiParameter parm) {
        try {
            V pane = producer.call();
            L.debug("producePane() created instance of {}", pane.getClass().getName());
            Builder b = parm.toBuilder().rootClass(pane.getClass()).pane(pane);
            b.titleProperty(findTitleProperty(pane));
            b.showingProperty(findShowingProperty(pane));
            return b.build();
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    public static Optional<BooleanProperty> findShowingProperty(Object o) {
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

    public static Optional<StringProperty> findTitleProperty(Object o) {
        try {
            List<Field> fields = allDeclaredFields(o.getClass());
            L.debug("findTitleProperty() inspecting fields for Bind(TITLE): {}", fields);
            for (Field field : fields) {
                Bind bind = field.getAnnotation(Bind.class);
                if ( bind != null && bind.value() == TITLE ) {
                    L.debug("findTitleProperty() found Bind(TITLE), extrating property");
                    field.setAccessible(true);
                    return Optional.ofNullable((StringProperty)field.get(o)); // Cast is safe, Look at the BindingProcessor.
                }
            }
        } catch (IllegalAccessException e) {
            L.error("findTitleProperty() Exception on field.get()", e);
        }
        return Optional.empty();
    }

    private static List<Field> allDeclaredFields(Class<?> clazz) {
        if ( clazz == null ) return Collections.emptyList();
        List<Field> as = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        as.addAll(allDeclaredFields(clazz.getSuperclass()));
        return as;
    }

    public static <T, V extends Dialog<T>> UiParameter produceDialog(Callable<V> producer, UiParameter parm) {
        try {
            V dialog = producer.call();
            L.debug("produceDialog: {}", dialog);
            // Dialog is special, allways use the title property.
            return parm.toBuilder().rootClass(dialog.getClass()).titleProperty(dialog.titleProperty()).dialog(dialog).pane(dialog.getDialogPane()).build();
        } catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }

    /**
     * Modifies the Dialog to be used in a swingOrMain environment.
     *
     * @param in
     * @return
     */
    public static UiParameter modifyDialog(UiParameter in) {
        Dialog dialog = in.dialog().get();
        // Activates the closing of any surounding swing element.
        dialog.setOnCloseRequest((event) -> {
            L.debug("handle(event.getSource()={}) dialog.getScene() is set ? {}", event.getSource(), dialog.getDialogPane().getScene() != null);
            Ui.closeWindowOf(((Dialog)event.getSource()).getDialogPane());
        });

        return in;
    }

    public static UiParameter consumePreResult(UiParameter in) {
        return in.optionalConsumePreResult();
    }

    /**
     * Call from Platform: creates a SwingNode in a BorderPane and sets the pane on in
     *
     * @param in the uiparameter
     * @return the modified uiparameter
     */
    public static UiParameter createSwingNode(UiParameter in) {
        SwingNode sn = new SwingNode();
        BorderPane p = new BorderPane(sn);
        return in.toBuilder().pane(p).build();
    }

    // Call only from Swing EventQueue
    public static UiParameter createJFXPanel(UiParameter in) {
        return in.toBuilder().jPanel(new JFXPanel()).build();
    }

    public static UiParameter produceFxml(UiParameter in) {
        try {
            Class<FxController> controllerClazz = (Class<FxController>)in.rootClass().get();  // Cast is a shortcut.
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(loadView(controllerClazz), "No View for " + controllerClazz));
            loader.load();
            Objects.requireNonNull(loader.getController(), "No controller based on " + controllerClazz + ". Controller set in Fxml ?");
            Pane pane = loader.getRoot();
            FxController controller = loader.getController();
            Builder b = in.toBuilder().pane(pane).fxController(controller);
            b.showingProperty(findShowingProperty(controller));
            b.titleProperty(findTitleProperty(controller));
            return b.build();
        } catch (IOException ex) {
            throw new CompletionException(ex);
        }
    }

    public static void registerAndSetStoreLocation(Class<?> key, javafx.stage.Stage window) {
        UiCore.global().locationStorage().loadLocation(key, window);
        window.addEventHandler(javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST, e -> UiCore.global().locationStorage().storeLocation(key, window));
    }

    public static UiParameter constructSwing(UiParameter in) {
        try {
            L.debug("constructSwing");
            JComponent component = in.jPanel().get(); // Must be set at this point.

            // TODO: look into the util methods if the saft.core(Swing.class).parentIfPresent(...) can be used.
            Window parent = in.saft().core(Swing.class).unwrap(in.uiParent()).orElse(in.saft().core(Swing.class).unwrapMain().orElse(null));

            final Window window = in.extractFrame()
                    ? BuilderUtil.newJFrame(in.toTitleProperty(), component)
                    : BuilderUtil.newJDailog(parent, in.toTitleProperty(), component, in.asSwingModality());
            BuilderUtil.setWindowProperties(in, window, in.extractReferenceClass(), parent, in.extractReferenceClass(), in.toKey());
            in.getClosedListenerImplemetation().ifPresent(elem -> window.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                    elem.closed();
                }

            }));
            window.setVisible(true);
            L.debug("constructSwing.setVisible(true)");
            return in.toBuilder().window(window).build();
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    public static <T> T waitAndProduceResult(UiParameter in) {
        if ( !(in.type().selectRelevantInstance(in) instanceof ResultProducer || in.type().selectRelevantInstance(in) instanceof Dialog) ) {
            throw new IllegalStateException("Calling Produce Result on a none ResultProducer. Try show instead of eval. Type: " + in.type());
        }
        // TODO: Extract to swing core.
        try {
            if ( UiCore.isSwing() ) BuilderUtil.wait(in.window().get()); // Only needed in Swing mode. In JavaFx the showAndWait() is allways used.
        } catch (InterruptedException | IllegalStateException | NullPointerException ex) {
            throw new CompletionException(ex);
        }
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

    public static java.util.List<java.awt.Image> loadAwtImages(Class<?> reference) throws IOException {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        return IconConfig.possibleIcons(reference).stream()
                .map(n -> reference.getResource(n))
                .filter(u -> u != null)
                .map(t -> toolkit.getImage(t))
                .collect(Collectors.toList());
    }

    public static java.util.List<Image> loadJavaFxImages(Class<?> reference) {
        return IconConfig.possibleIcons(reference).stream()
                .map(n -> reference.getResourceAsStream(n))
                .filter(u -> u != null)
                .map(r -> new Image(r))
                .collect(Collectors.toList());
    }

}
