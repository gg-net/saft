/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import java.awt.Dialog.ModalityType;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.swing.*;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.AlertType;
import eu.ggnet.saft.core.ui.UiParent;
import eu.ggnet.saft.core.ui.builder.PreBuilder;
import eu.ggnet.saft.core.ui.builder.Result;

import static eu.ggnet.saft.core.impl.UiParameter.Type.*;

/**
 *
 * @author oliver.guenther
 */
// TODO: GlobalWarning. The implementation has some global impact, which must be cleaned up in the final implementation
public class Swing extends AbstractCore implements Core<Window> {

    private final static Logger L = LoggerFactory.getLogger(Swing.class);

    // TODO: Implement cyclic verification of null weak references and remove elements.
    private final List<WeakReference<Window>> allWindows = new ArrayList<>();

    private final Saft saft;

    private Window mainWindow;

    // TODO: Implement a cleanup for all references. Good candidate for WeakReferneces on Key and Value.
    private final Map<Scene, JFXPanel> SWING_PARENT_HELPER = new WeakHashMap<>();

    /**
     * Contains all active once windows.
     */
    private final Map<String, Window> ONCES_ACTIVE = new HashMap<>();

    /**
     * Contains all wrapped builders to return the window for future usage.
     */
    private final Map<String, Runnable> ONCES_BUILDER = new HashMap<>();

    private final Callback<Class<?>, Object> INSTANCE_INITIALZER;

    public Swing(Saft saft, JFrame mainParent) {
        this(saft, mainParent, null);
    }

    public Swing(final Saft saft) {
        this(saft, null, null);
    }

    public Swing(final Saft saft, Callback<Class<?>, Object> initialzier) {
        this(saft, null, initialzier);
    }

    public Swing(final Saft saft, JFrame mainWindow, Callback<Class<?>, Object> initialzier) {
        // TODO: Global activity. reconsider.
        new JFXPanel(); // Start the Fx platform.
        Platform.setImplicitExit(false);

        this.saft = Objects.requireNonNull(saft, "saft must not be null");
        this.INSTANCE_INITIALZER = initialzier;
        if ( mainWindow != null ) initMain(mainWindow);
    }

    @Override
    public void initMain(Window window) {
        if ( this.mainWindow != null ) throw new IllegalStateException("Main is allready set");
        if ( window == null ) throw new NullPointerException("window must not be null");
        L.debug("initMain(window={})", window.getClass().getName());
        this.mainWindow = window;
        mainWindow.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                saft.shutdown();
            }

        });
    }

    @Override
    protected Optional<Callback<Class<?>, Object>> initializer() {
        return Optional.ofNullable(INSTANCE_INITIALZER);
    }

    @Override
    public Optional<Window> unwrap(UiParent parent) {
        return unwrap(Optional.ofNullable(parent));
    }

    @Override
    public Optional<Window> unwrap(Optional<UiParent> parent) {
        Objects.requireNonNull(parent, "Optional parent must not be null");
        if ( !parent.isPresent() ) return Optional.empty();
        if ( parent.get().node().isPresent() ) return windowAncestor(parent.get().node().get());
        if ( parent.get().component().isPresent() ) {
            Component swingElement = parent.get().component().get();
            if ( swingElement instanceof Window ) return Optional.of((Window)swingElement);
            return Optional.ofNullable(SwingUtilities.getWindowAncestor(swingElement));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Window> unwrapMain() {
        return Optional.ofNullable(mainWindow);
    }

    @Override
    public void add(Window window) {
        Objects.requireNonNull(window, "window must not be null");
        allWindows.add(new WeakReference<>(window));
    }

    @Override
    public void shutdown() {
        for (WeakReference<Window> windowRef : allWindows) {
            if ( windowRef.get() == null ) continue;
            windowRef.get().setVisible(false); // Close all windows.
            windowRef.get().dispose();
        }
        // TODO: This is a global call. In the multiple safts in one vm, this cannot be used. Some other semantic is needed.
        for (Window window : java.awt.Frame.getWindows()) {
            window.setVisible(false);
            window.dispose();
        }
        Platform.exit();
    }

    @Override
    public boolean isActiv() {
        return true;
    }

    @Override
    public void closeOf(UiParent parent) {
        unwrap(parent).ifPresent(p -> run(() -> {
            p.setVisible(false);
            p.dispose();
        }));
    }

    private void run(Runnable runnable) {
        if ( EventQueue.isDispatchThread() ) runnable.run();
        else EventQueue.invokeLater(runnable);
    }

    @Override
    public void relocate() {
        unwrapMain().ifPresent(m -> {
            L.debug("relocate() relocating mainParent {}", m);
            m.setSize(800, 600);
            m.setLocation(20, 20);
        });

        int i = 40;

        for (Iterator<java.awt.Window> iterator = allWindows.stream().map(w -> w.get()).filter(w -> w != null).iterator();
                iterator.hasNext();) {
            Window w = iterator.next();
            L.debug("relocate() relocating {}", w);
            w.setSize(800, 600);
            w.setLocation(i, i);
            i = i + 20;
        }
    }

    // TODO: Baue noch was ein, das wenn show (also das Runable aufgerufen wird, das bist etwas im der ONCES_ACTIVE map ankommt, es nicht noch ein 2 mal gestartet wird.
    // TODO: Registiere einen window closing adapter, der wenn es geclosed wird, es aus et active List rausgeschmissen wird.
    @Override
    public void registerOnce(String key, Core.In<?, ?> in) {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        Objects.requireNonNull(in, "in must not be null");
        L.debug("registerOnce(key={})", key);
        ONCES_BUILDER.put(key, () -> show(new PreBuilder(saft).frame(true), Optional.empty(), in).thenAccept(w -> registerActiveAndToFront(key, w)));
    }

    @Override
    public boolean showOnce(String key) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        if ( !ONCES_BUILDER.containsKey(key) ) return false;
        if ( ONCES_ACTIVE.containsKey(key) && ONCES_ACTIVE.get(key).isVisible() ) {
            L.debug("showOnce(key={}) visible, focusing.", key);
            EventQueue.invokeLater(() -> {
                Window window = ONCES_ACTIVE.get(key);
                if ( window instanceof JFrame ) ((JFrame)window).setExtendedState(JFrame.NORMAL);
                window.toFront();
            });
        } else {
            L.debug("showOnce(key={}) not yet visible, creating.", key);
            ONCES_BUILDER.get(key).run();
        }
        return true;
    }

    @Override
    public <R, S extends R> CompletableFuture<Window> show(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in) {
        return prepareShowEval(prebuilder, preProducer, in).thenApply((UiParameter p) -> p.window().get());
    }

    @Override
    public <T, R, S extends R> Result<T> eval(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in) {
        return new Result<>(saft, prepareShowEval(prebuilder, preProducer, in)
                .thenApplyAsync((UiParameter p) -> waitAndProduceResult(p), saft.executorService()));
    }

    @Override
    public void showAlert(String message, Optional<UiParent> parent, Optional<String> title, Optional<AlertType> type) throws NullPointerException {
        invokeAndWait(() -> {
            JOptionPane.showMessageDialog(saft.core(Swing.class).unwrap(parent).orElse(saft.core(Swing.class).unwrapMain().orElse(null)), message, title.orElse("Information"), type.orElse(AlertType.INFO).getOptionPaneType());
        });
    }

    private <R, S extends R> CompletableFuture<UiParameter> prepareShowEval(PreBuilder preBuilder, Optional<Callable<?>> optPreProducer, Core.In<R, S> in) {
        Objects.requireNonNull(preBuilder, "preBuilder must not be null");
        Objects.requireNonNull(optPreProducer, "optPreProducer must not be null");
        Objects.requireNonNull(in, "in must not be null");

        UiParameter.Type type = selectType(in);

        switch (type) {

            case DIALOG:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceDialog(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        .thenApply(p -> modifyDialog(p))
                        .thenApplyAsync(p -> createJFXPanel(p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> wrapPane(p), Platform::runLater)
                        .thenApplyAsync(p -> constructSwing(p), EventQueue::invokeLater)
                        .handle(saft.handler());

            case SWING:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceJPanel(in, p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> constructSwing(p), EventQueue::invokeLater)
                        .handle(saft.handler());

            case FX:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> producePane(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        //                        .thenApplyAsync(i -> i, saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which sometimes fails.
                        .thenApplyAsync(p -> createJFXPanel(p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> wrapPane(p), Platform::runLater)
                        .thenApplyAsync(p -> constructSwing(p), EventQueue::invokeLater)
                        .handle(saft.handler());

            case FXML:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceFxml(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        //                        .thenApplyAsync(i -> i, saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which sometimes fails.
                        .thenApplyAsync(p -> createJFXPanel(p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> wrapPane(p), Platform::runLater)
                        .thenApplyAsync(p -> constructSwing(p), EventQueue::invokeLater)
                        .handle(saft.handler());

            default:
                throw new IllegalArgumentException(type + " not implemented");

        }
    }

    private UiParameter wrapPane(UiParameter in) {
        if ( !(in.jPanel().get() instanceof JFXPanel) ) throw new IllegalArgumentException("JPanel not instance of JFXPanel : " + in);
        JFXPanel fxp = (JFXPanel)in.jPanel().get();
        if ( in.pane().get().getScene() != null ) {
            L.debug("wrapPane(in): in.pane().getScene() is not null, probally a javafx dialog to wrap, reusing");
            fxp.setScene(in.pane().get().getScene());
        } else {
            L.debug("wrapPane(in): in.pane().getScene() is null, creating");
            fxp.setScene(new Scene(in.pane().get(), javafx.scene.paint.Color.TRANSPARENT));
        }
        SWING_PARENT_HELPER.put(fxp.getScene(), fxp);
        return in;
    }

    private UiParameter constructSwing(UiParameter in) {
        try {
            L.debug("constructSwing");
            JComponent component = in.jPanel().get(); // Must be set at this point.

            // TODO: look into the util methods if the saft.core(Swing.class).parentIfPresent(...) can be used.
            Window parent = in.saft().core(Swing.class).unwrap(in.uiParent()).orElse(in.saft().core(Swing.class).unwrapMain().orElse(null));

            final Window window = in.extractFrame()
                    ? newJFrame(in.toTitleProperty(), component)
                    : newJDailog(parent, in.toTitleProperty(), component, in.asSwingModality());
            setWindowProperties(in, window, in.extractReferenceClass(), parent, in.extractReferenceClass(), in.toKey());
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

    /**
     * New jframe based on parameters.
     *
     * @param titleProperty
     * @param component
     * @return
     */
    private JFrame newJFrame(StringProperty titleProperty, JComponent component) {
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
    private JDialog newJDailog(Window swingParent, StringProperty titleProperty, JComponent component, ModalityType modalityType) {
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
    private <T extends Window> T setWindowProperties(UiParameter in, T window, Class<?> iconReferenzClass, Window relativeLocationAnker, Class<?> storeLocationClass, String windowKey) throws IOException { // IO Exeception based on loadIcons
        window.setIconImages(loadAwtImages(iconReferenzClass));
        window.pack();
        window.setLocationRelativeTo(relativeLocationAnker);
        if ( in.isStoreLocation() ) saft.locationStorage().loadLocation(storeLocationClass, window);
        in.saft().core(Swing.class).add(window);
        // Removes on close.
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Store location.
                if ( in.isStoreLocation() ) saft.locationStorage().storeLocation(storeLocationClass, window);
            }
        });
        return window;
    }

    private java.util.List<java.awt.Image> loadAwtImages(Class<?> reference) throws IOException {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        return IconConfig.possibleIcons(reference).stream()
                .map(n -> reference.getResource(n))
                .filter(u -> u != null)
                .map(t -> toolkit.getImage(t))
                .collect(Collectors.toList());
    }

    /**
     * Modifies the Dialog to be used in a swingOrMain environment.
     *
     * @param in
     * @return
     */
    private UiParameter modifyDialog(UiParameter in) {
        javafx.scene.control.Dialog dialog = in.dialog().get();
        // Activates the closing of any surounding swing element.
        dialog.setOnCloseRequest((event) -> {
            L.debug("handle(event.getSource()={}) dialog.getScene() is set ? {}", event.getSource(), dialog.getDialogPane().getScene() != null);
            closeOf(UiParent.of(((javafx.scene.control.Dialog)event.getSource()).getDialogPane()));
        });

        return in;
    }

    /**
     * Returns the Swing Window in Swing Mode from a wrapped JavaFx Node.
     *
     * @param p the node
     * @return a window
     */
    private Optional<Window> windowAncestor(Node p) {
        if ( p == null ) return Optional.empty();
        L.debug("windowAncestor(node) node.getScene()={}, SWING_PARENT_HELPER.keySet()={}", p.getScene(), SWING_PARENT_HELPER.keySet());
        return windowAncestor(SWING_PARENT_HELPER.get(p.getScene()));
    }

    /**
     * Special form of {@link SwingUtilities#getWindowAncestor(java.awt.Component) }, as it also verifies if the supplied parameter is of type Window and if
     * true returns it.
     *
     * @param c the component
     * @return a window.
     */
    private Optional<Window> windowAncestor(Component c) {
        L.debug("windowAncestor({})", c);
        if ( c == null ) return Optional.empty();
        if ( c instanceof Window ) return Optional.of((Window)c);
        return Optional.ofNullable(SwingUtilities.getWindowAncestor(c));
    }

    private UiParameter createJFXPanel(UiParameter in) {
        return in.toBuilder().jPanel(new JFXPanel()).build();
    }

    private void registerActiveAndToFront(String key, Window w) {
        L.debug("registerActiveAndToFront(key={})", key);
        ONCES_ACTIVE.put(key, w);
        w.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                L.debug("windowClosing() once closeing " + key + ", removing from active map");
                ONCES_ACTIVE.remove(key);
            }
        });
        w.toFront();
    }

    private void invokeAndWait(Runnable runnable) {
        if ( EventQueue.isDispatchThread() ) runnable.run();
        else try {
            EventQueue.invokeAndWait(runnable);
        } catch (InterruptedException | InvocationTargetException ex) {
            saft.handle(ex);
        }
    }
}
