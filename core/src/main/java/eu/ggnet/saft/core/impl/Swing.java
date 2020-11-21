/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import javax.swing.*;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.*;

import static eu.ggnet.saft.core.ui.builder.UiParameter.Type.*;

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
        unwrap(parent).ifPresent(p -> SwingSaft.run(() -> {
            p.setVisible(false);
            p.dispose();
        }));
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
        return new Result<>(prepareShowEval(prebuilder, preProducer, in)
                .thenApplyAsync((UiParameter p) -> BuilderUtil.waitAndProduceResult(p), saft.executorService()));
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
                        .thenApply(BuilderUtil::modifyDialog)
                        .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                        .thenApplyAsync(p -> wrapPane(p), Platform::runLater)
                        .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater)
                        .handle(saft.handler());

            case SWING:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceJPanel(in, p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), EventQueue::invokeLater)
                        .thenApply(BuilderUtil::constructSwing)
                        .handle(saft.handler());

            case FX:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> producePane(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        //                        .thenApplyAsync(i -> i, saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which sometimes fails.
                        .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                        .thenApplyAsync(p -> wrapPane(p), Platform::runLater)
                        .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater)
                        .handle(saft.handler());

            case FXML:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceFxml(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        //                        .thenApplyAsync(i -> i, saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which sometimes fails.
                        .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                        .thenApplyAsync(p -> wrapPane(p), Platform::runLater)
                        .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater)
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

    /**
     * Returns the Swing Window in Swing Mode from a wrapped JavaFx Node.
     *
     * @param p the node
     * @return a window
     */
    private Optional<Window> windowAncestor(Node p) {
        if ( p == null ) return Optional.empty();
        L.debug("windowAncestor(node) node.getScene()={}, SWING_PARENT_HELPER.keySet()={}", p.getScene(), SWING_PARENT_HELPER.keySet());
        return SwingSaft.windowAncestor(SWING_PARENT_HELPER.get(p.getScene()));
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
