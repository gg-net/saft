/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import java.awt.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.*;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.*;
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
    private final Map<String, Runnable> ONCES_BUILDER = new HashMap<>();

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

    private void registerActiveAndToFront(String key, Window w) {
        ONCES_ACTIVE.put(key, w);
        w.toFront();
    }

    private <X, Y extends X> void blub(Class<X> c, Supplier<Y> s) {

    }

    // TODO: Reconsider ob cores doch nur 1-2 nicht compilesafe methoden für register haben
    // TODO: Baue noch was ein, das wenn show (also das Runable aufgerufen wird, das bist etwas im der ONCES_ACTIVE map ankommt, es nicht noch ein 2 mal gestartet wird.
    // TODO: Registiere einen window closing adapter, der wenn es geclosed wird, es aus et active List rausgeschmissen wird.
    @Override
    public <U extends Pane> void registerOnceFx(String key, Supplier<U> paneSupplier) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        Objects.requireNonNull(paneSupplier, "paneSupplier must not be null");
        ONCES_BUILDER.put(key, new Runnable() {
            @Override
            public void run() {
                show(new PreBuilder(saft).frame(true), Optional.empty(), new Core.In<>(Pane.class, paneSupplier));
                //.thenAccept(w -> registerActiveAndToFront(key, w));
            }
        });
    }

    @Override
    public void registerOnceFx(String key, Class<? extends Pane> paneClass) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerOnceSwing(String key, Supplier<? extends JPanel> panelSupplier) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerOnceSwing(String key, Class<? extends JPanel> panelClass) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerOnceFxml(String key, Class<? extends FxController> controllerClass) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean showOnce(String key) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        if ( !ONCES_BUILDER.containsKey(key) ) return false;
        if ( ONCES_ACTIVE.containsKey(key) && ONCES_ACTIVE.get(key).isVisible() ) {
            log.debug("showOnce(key={}) is visible", key);
            Window window = ONCES_ACTIVE.get(key);
            EventQueue.invokeLater(() -> {
                if ( window instanceof JFrame ) ((JFrame)window).setExtendedState(JFrame.NORMAL);
                window.toFront();
            });
        } else {
            ONCES_BUILDER.get(key).run();
        }
        return true;
    }

    public <R, S extends R> CompletableFuture<Window> show(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in) {
        return prepareShowEval(prebuilder, preProducer, in).thenApply((UiParameter p) -> p.window().get());
    }

    public <T, R, S extends R> Result<T> eval(PreBuilder prebuilder, Optional<Callable<?>> preProducer, Core.In<R, S> in) {
        return new Result<>(prepareShowEval(prebuilder, preProducer, in)
                .thenApplyAsync((UiParameter p) -> BuilderUtil.waitAndProduceResult(p), saft.executorService()));
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
                        .thenApplyAsync(Swing::optionalConsumePreProducer, Platform::runLater)
                        .thenApply(BuilderUtil::modifyDialog)
                        .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                        .thenApplyAsync(BuilderUtil::wrapPane, Platform::runLater) // Swing Specific
                        .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater); // Swing Specific

            case SWING:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceJPanel(in, p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(Swing::optionalConsumePreProducer, EventQueue::invokeLater)
                        .thenApply(BuilderUtil::constructSwing);
            case FX:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> producePane(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(Swing::optionalConsumePreProducer, Platform::runLater)
                        //                        .thenApplyAsync(i -> i, saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which sometimes fails.
                        .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                        .thenApplyAsync(BuilderUtil::wrapPane, Platform::runLater) // Swing Specific
                        .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater); // Swing Specific
            case FXML:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceFxml(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(Swing::optionalConsumePreProducer, Platform::runLater)
                        //                        .thenApplyAsync(i -> i, saft.executorService()) // Make sure we are not switching from Swing to JavaFx directly, which sometimes fails.
                        .thenApplyAsync(BuilderUtil::createJFXPanel, EventQueue::invokeLater)
                        .thenApplyAsync(BuilderUtil::wrapPane, Platform::runLater) // Swing Specific
                        .thenApplyAsync(BuilderUtil::constructSwing, EventQueue::invokeLater); // Swing Specific

            default:
                throw new IllegalArgumentException(type + " not implemented");

        }
    }

    // TODO: keep as instance method, for future cdi usage.
    private Object createInstance(Core.In<?, ?> in) {
        Core.In<Object, Object> i2 = (Core.In<Object, Object>)in;
        return i2.supplier().map(Supplier::get).orElseGet(() -> {
            try {
                return i2.clazz().newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException("Error during " + i2.clazz().getName() + ".newInstance(), probablly no zero argument constructor available", ex);
            }
        });

    }

    private UiParameter produceJPanel(Core.In<?, ?> in, UiParameter param) {
        log.debug("produceJPanel(in={})", in);
        if ( selectType(in) != SWING )
            throw new IllegalArgumentException("produceJPanel(" + in + ") used illegal, as selected Type must be " + SWING + " but was " + selectType(in));
        JPanel panel = (JPanel)createInstance(in); // Safe cast as of line above.
        Builder b = param.toBuilder().rootClass(panel.getClass()).jPanel(panel);
        b.titleProperty(BuilderUtil.findTitleProperty(panel));
        b.showingProperty(BuilderUtil.findShowingProperty(panel));
        return b.build();
    }

    private UiParameter producePane(Core.In<?, ?> in, UiParameter param) {
        log.debug("producePane(in={})", in);
        if ( selectType(in) != FX )
            throw new IllegalArgumentException("producePane(" + in + ") used illegal, as selected Type must be " + FX + " but was " + selectType(in));
        Pane pane = (Pane)createInstance(in);
        Builder b = param.toBuilder().rootClass(pane.getClass()).pane(pane);
        b.titleProperty(findTitleProperty(pane));
        b.showingProperty(findShowingProperty(pane));
        return b.build();
    }

    private UiParameter produceFxml(Core.In<?, ?> in, UiParameter param) {
        log.debug("produceFxml(in={})", in);
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

    private UiParameter produceDialog(Core.In<?, ?> in, UiParameter parm) {
        log.debug("produceDialog(in={})", in);
        if ( selectType(in) != DIALOG )
            throw new IllegalArgumentException("produceFxml(" + in + ") used illegal, as selected Type must be " + DIALOG + " but was " + selectType(in));
        javafx.scene.control.Dialog<?> dialog = (javafx.scene.control.Dialog<?>)createInstance(in);
        // Dialog is special, allways use the title property.
        return parm.toBuilder().rootClass(dialog.getClass()).titleProperty(dialog.titleProperty()).dialog(dialog).pane(dialog.getDialogPane()).build();
    }

    protected static UiParameter optionalRunPreProducer(UiParameter in, Optional<Callable<?>> optPreProducer) {
        if ( !optPreProducer.isPresent() ) return in;
        return in.toBuilder().preResult(exceptionRun(optPreProducer.get())).build();
    }

    protected static UiParameter optionalConsumePreProducer(UiParameter in) {
        if ( in.preResult().isPresent() && (in.type().selectRelevantInstance(in) instanceof Consumer) ) {
            ((Consumer)in.type().selectRelevantInstance(in)).accept(in.preResult());
        }
        return in;
    }

    protected static UiParameter.Type selectType(Core.In<?, ?> in) {
        if ( in.clazz().isAssignableFrom(JPanel.class) ) return SWING;
        if ( in.clazz().isAssignableFrom(Pane.class) ) return FX;
        if ( in.clazz().isAssignableFrom(javafx.scene.control.Dialog.class) ) return DIALOG;
        if ( in.clazz().isAssignableFrom(FxController.class) ) return FXML;
        throw new IllegalArgumentException(Swing.class.getSimpleName() + " does not support " + in.clazz() + " for show or eval (selectType)");
    }

    protected static UiParameter init(PreBuilder preBuilder, UiParameter.Type type) {
        LoggerFactory.getLogger(Swing.class).debug("init(preBuilder={}, type={})", preBuilder, type);
        return UiParameter.fromPreBuilder(preBuilder).type(type).build();
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

}
