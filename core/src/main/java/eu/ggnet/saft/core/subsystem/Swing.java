/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.*;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.*;

/**
 *
 * @author oliver.guenther
 */
// TODO: GlobalWarning. The implementation has some global impact, which must be cleaned up in the final implementation
public class Swing implements Core<Window> {

    // TODO: Implement cyclic verification of null weak references and remove elements.
    private final List<WeakReference<Window>> allWindows = new ArrayList<>();

    private final Saft saft;

    private final JFrame mainParent;

    private final Logger log = LoggerFactory.getLogger(Swing.class);

    // TODO: Implement a cleanup for all references. Good candidate for WeakReferneces on Key and Value.
    private final Map<Scene, JFXPanel> SWING_PARENT_HELPER = new WeakHashMap<>();

    /**
     * Contains all active once windows.
     */
    private final Map<String, Window> ONCES_ACTIVE = new HashMap<>();

    /**
     * Contains all wrapped builders to return the window for future usage.
     */
    private final Map<String, Supplier<Window>> ONCES_BUILDER = new HashMap<>();

    /**
     * Registers a jfx panel in the ui parent helper.
     * Needed for finding parents in javafx/swing mixed environments.
     *
     * @param fxp the jfxpanel
     */
    //TODO: Findout, if this can be keept more private
    public void mapParent(JFXPanel fxp) {
        SWING_PARENT_HELPER.put(fxp.getScene(), fxp);
    }

    /**
     * Returns the Swing Window in Swing Mode from a wrapped JavaFx Node.
     *
     * @param p the node
     * @return a window
     */
    //TODO: Findout, if this can be keept more private
    public Optional<Window> windowAncestor(Node p) {
        if ( p == null ) return Optional.empty();
        log.debug("windowAncestor(node) node.getScene()={}, SWING_PARENT_HELPER.keySet()={}", p.getScene(), SWING_PARENT_HELPER.keySet());
        return SwingSaft.windowAncestor(SWING_PARENT_HELPER.get(p.getScene()));
    }

    Swing(Saft saft, JFrame mainParent) {
        // TODO: Global activity. reconsider.
        new JFXPanel(); // Start the Fx platform.
        Platform.setImplicitExit(false);

        this.saft = saft;
        this.mainParent = mainParent;
    }

    @Override
    public void parentIfPresent(Optional<UiParent> parent, Consumer<Window> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        Optional<Window> optWindow = unwrap(parent);
        if ( optWindow.isPresent() ) consumer.accept(optWindow.get());
        else if ( unwrapMain().isPresent() ) consumer.accept(unwrapMain().get());
        else log.debug("parentIfPresent() neither supplied parent nor mainparent is set, consumer not called");
    }

    @Override
    public void parentIfPresent(UiParent parent, Consumer<Window> consumer) {
        parentIfPresent(Optional.ofNullable(parent), consumer);
    }

    @Override
    public void parentIfPresent(Consumer<Window> consumer) {
        parentIfPresent(Optional.empty(), consumer);
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
        return Optional.of(mainParent);
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
        unwrap(parent).ifPresent(p -> SwingSaft.run(() -> {
            p.setVisible(false);
            p.dispose();
        }));
    }

    @Override
    public void relocate() {
        unwrapMain().ifPresent(m -> {
            log.debug("relocate() relocating mainParent {}", m);
            m.setSize(800, 600);
            m.setLocation(20, 20);
        });

        int i = 40;

        for (Iterator<java.awt.Window> iterator = allWindows.stream().map(w -> w.get()).filter(w -> w != null).iterator();
                iterator.hasNext();) {
            Window w = iterator.next();
            log.debug("relocate() relocating {}", w);
            w.setSize(800, 600);
            w.setLocation(i, i);
            i = i + 20;
        }
    }

    @Override
    public CoreUiFuture prepare(final Supplier<CompletableFuture<UiParameter>> supplier, UiParameter.Type type) {
        Objects.requireNonNull(type, "type must not be null");
        return new CoreUiFuture() {

            @Override
            public CompletableFuture<UiParameter> proceed() {
                switch (type) {

                    case DIALOG:
                        return supplier.get()
                                .thenApply(BuilderUtil::modifyDialog)
                                .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                                .thenApplyAsync(BuilderUtil::wrapPane, Platform::runLater) // Swing Specific
                                .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater); // Swing Specific

                    case SWING:
                        return supplier.get()
                                .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater);

                    case FX:
                    case FXML:
                        return supplier.get()
                                .thenApplyAsync(in -> in, saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which sometimes fails.
                                .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                                .thenApplyAsync(BuilderUtil::wrapPane, Platform::runLater) // Swing Specific
                                .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater); // Swing Specific

                    default:
                        throw new IllegalArgumentException(type + " not implemented");
                }
            }

            @Override
            public CompletableFuture<Object> show() {
                return proceed().thenApply(in -> in.window().get());
            }
        };
    }

    @Override
    public void registerOnceFx(String key, Supplier<? extends Pane> paneSupplier) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.isBlank() ) throw new NullPointerException("key must not be blank");
        Objects.requireNonNull(paneSupplier, "paneSupplier must not be null");
//        ONCES_BUILDER.put(key, new Supplier<Window>() {
//            @Override
//            public Window get() {
//                // Ui.build().fx(). ...
//            }
//        });
    }

    @Override
    public void registerOnceFx(String key, Class<? extends Pane> paneClass) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.isBlank() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerOnceSwing(String key, Supplier<? extends JPanel> panelSupplier) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.isBlank() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerOnceSwing(String key, Class<? extends JPanel> panelClass) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.isBlank() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerOnceFxml(String key, Class<? extends FxController> controllerClass) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.isBlank() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean showOnce(String key) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.isBlank() ) throw new NullPointerException("key must not be blank");
        if ( !ONCES_BUILDER.containsKey(key) ) return false;
        if ( ONCES_ACTIVE.containsKey(key) && ONCES_ACTIVE.get(key).isVisible() ) {
            log.debug("showOnce(key={}) is visible", key);
            Window window = ONCES_ACTIVE.get(key);
            EventQueue.invokeLater(() -> {
                if ( window instanceof JFrame ) ((JFrame)window).setExtendedState(JFrame.NORMAL);
                window.toFront();
            });
        } else {
            Window window = ONCES_BUILDER.get(key).get();
            window.toFront();
            ONCES_ACTIVE.put(key, window);
        }
        return true;
    }

    public <V extends Pane> CompletableFuture<Window> showFx(PreBuilder prebuilder, Callable<V> javafxPaneProducer) {
        return null;
    }

    public <V extends Pane> CompletableFuture<Window> showFx(PreBuilder prebuilder, Class<V> javafxPaneClass) {
        return null;
    }

    public <P, V extends Pane & Consumer<P>> CompletableFuture<Window> showFx(PreBuilder prebuilder, Callable<P> preProducer, Callable<V> javafxPaneProducer) {
        return null;
    }

    public <P, V extends Pane & Consumer<P>> CompletableFuture<Window> showFx(PreBuilder prebuilder, Callable<P> preProducer, Class<V> javafxPaneClass) {
        return null;
    }

    public <T, V extends Pane & ResultProducer<T>> Result<T> evalFx(PreBuilder prebuilder, Callable<V> javafxPaneProducer) {
        return null;
    }

    public <T, V extends Pane & ResultProducer<T>> Result<T> evalFx(PreBuilder prebuilder, Class<V> javafxPaneClass) {
        return null;
    }

    public <T, P, V extends Pane & Consumer<P> & ResultProducer<T>> Result<T> evalFx(PreBuilder prebuilder, Callable<P> preProducer, Callable<V> javafxPaneProducer) {
        return null;
    }

    public <T, P, V extends Pane & Consumer<P> & ResultProducer<T>> Result<T> evalFx(PreBuilder prebuilder, Callable<P> preProducer, Class<V> javafxPaneClass) {
        return null;
    }

    public <V extends FxController> CompletableFuture<Window> showFxml(PreBuilder prebuilder, Class<V> fxmlControllerClass) {
        return null;
    }

    public <P, V extends FxController & Consumer<P>> CompletableFuture<Window> show(PreBuilder prebuilder, Callable<P> preProducer, Class<V> fxmlControllerClass) {
        return null;
    }

    public <T, V extends FxController & ResultProducer<T>> Result<T> eval(PreBuilder prebuilder, Class<V> fxmlControllerClass) {
        return null;
    }

    public <T, P, V extends FxController & Consumer<P> & ResultProducer<T>> Result<T> eval(PreBuilder prebuilder, Callable<P> preProducer, Class<V> fxmlControllerClass) {
        return null;
    }

}
