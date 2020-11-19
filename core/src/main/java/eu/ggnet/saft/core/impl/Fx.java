/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.FxSaft;
import eu.ggnet.saft.core.ui.UiParent;
import eu.ggnet.saft.core.ui.builder.*;

/**
 *
 * @author oliver.guenther
 */
public class Fx extends AbstractCore implements Core<Stage> {

    private final Saft saft;

    private final Stage mainParent;

    // TODO: Implement cyclic verification of null weak references and remove elements.
    private final List<WeakReference<Stage>> allStages = new ArrayList<>();

    private final Logger log = LoggerFactory.getLogger(Fx.class);

    /**
     * Contains all active once windows.
     */
    private final Map<String, Stage> ONCES_ACTIVE = new HashMap<>();

    /**
     * Contains all wrapped builders to return the window for future usage.
     */
    private final Map<String, Runnable> ONCES_BUILDER = new HashMap<>();

    /**
     * Ceates a new Fx Core, should only be used in Saft.
     *
     * @param mainParent the mainParent, if null core is dead.
     */
    Fx(Saft saft, Stage mainParent) {
        this.saft = saft;
        this.mainParent = mainParent;
    }

    @Override
    public void parentIfPresent(UiParent parent, Consumer<Stage> consumer) {
        parentIfPresent(Optional.ofNullable(parent), consumer);
    }

    @Override
    public void parentIfPresent(Optional<UiParent> parent, Consumer<Stage> consumer) {
        Objects.requireNonNull(consumer, "consumer must not be null");
        Optional<Stage> optStage = unwrap(parent);
        if ( optStage.isPresent() ) consumer.accept(optStage.get());
        else if ( unwrapMain().isPresent() ) consumer.accept(unwrapMain().get());
        else log.debug("parentIfPresent() neither supplied parent nor mainparent is set, consumer not called");
    }

    @Override
    public void parentIfPresent(Consumer<Stage> consumer) {
        parentIfPresent(Optional.empty(), consumer);
    }

    @Override
    public Optional<Stage> unwrap(UiParent parent) {
        return unwrap(Optional.ofNullable(parent));
    }

    @Override
    public Optional<Stage> unwrap(Optional<UiParent> parent) {
        Objects.requireNonNull(parent, "Optional parent must not be null");
        if ( !parent.isPresent() ) return Optional.empty();
        if ( parent.get().node().isPresent() ) {
            Scene scene = parent.get().node().get().getScene();
            if ( scene == null ) return Optional.empty(); // The node was never added to a secen.
            javafx.stage.Window window = scene.getWindow();
            if ( window == null ) {
                log.warn("unwrap() uiparent.node() was set, node had a scene but window was null");
                return Optional.empty();
            }
            if ( !(window instanceof Stage) ) {
                log.warn("unwrap() uiparent.node() was set, node had a scene and window, but window was not of type stage");
                return Optional.empty();
            }
            return Optional.of((Stage)window);
        }
        if ( parent.get().component().isPresent() ) {
            Component swingElement = parent.get().component().get();
            return Optional.ofNullable(find(swingElement));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Stage> unwrapMain() {
        return Optional.of(mainParent);
    }

    @Override
    public void add(Stage window) {
        allStages.add(new WeakReference<>(window));
    }

    @Override
    public void shutdown() {
        allStages.forEach(w -> Optional.ofNullable(w.get()).ifPresent(s -> s.hide()));
        // TODO: This is a global call. In the multiple safts in one vm, this cannot be used. Some other semantic is needed.
        getWindows().stream().filter(w -> w != mainParent).forEach(javafx.stage.Window::hide); // close/hide all free stages.
    }

    @Override
    public boolean isActiv() {
        return true;
    }

    // TODO: Implement a cleanup for all references. Good candidate for WeakReferneces on Key and Value.
    private final Map<Component, SwingNode> JAVAFX_PARENT_HELPER = new HashMap<>();

    public void mapParent(Component c, SwingNode n) {
        JAVAFX_PARENT_HELPER.put(c, n);
    }

    /**
     * Returns the Stage containing the swingnode with the component or null if not found.
     *
     * @param c the component
     * @return the stage or null
     */
    // TODO: Look into the future if we need the stage or can use the window.
    public Stage find(Component c) {
        log.debug("find({})", c);
        SwingNode sn = deepfind(Objects.requireNonNull(c, "Component for find is null"));
        if ( sn == null ) return null;
        javafx.stage.Window window = sn.getScene().getWindow();
        if ( window instanceof Stage ) return (Stage)window;
        return null;
    }

    private SwingNode deepfind(Component c) {
        log.debug("deep({})", c);
        if ( c == null ) return null;
        if ( JAVAFX_PARENT_HELPER.containsKey(c) ) return JAVAFX_PARENT_HELPER.get(c);
        return deepfind(c.getParent());
    }

    // public final static Map<String, WeakReference<Stage>> ACTIVE_STAGES = new ConcurrentHashMap<>();
    private static Supplier<List<javafx.stage.Window>> GetWindowsSupplier = null; // Will be set via getWindows.

    /**
     * Reflexive Method to get all open windows in any JDK from 8 upwards.
     * In JDK8 the only way to get all open Windows/Stages was via the unoffical API com.sun.javafx.stage.StageHelper.getStages()
     * Form JDK9 upwards there is the offical API Window.getWindows().
     * Both methos are implemented here via reflections.
     *
     * @return a List containing all open Windows.
     */
    // Hint: internal use
    // TODO: It is a util method. maybe move to UiUtil
    private List<javafx.stage.Window> getWindows() {
        if ( GetWindowsSupplier == null ) {
            try {
                GetWindowsSupplier = new Supplier<List<javafx.stage.Window>>() {

                    private final Method methodGetStages = Class.forName("com.sun.javafx.stage.StageHelper").getMethod("getStages");

                    @Override
                    public List<javafx.stage.Window> get() {
                        try {
                            return new ArrayList<>((List<javafx.stage.Window>)methodGetStages.invoke(null));
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            throw new RuntimeException("getWindows(): com.sun.jacafx.stage.StageHelper.getStages was found, but faild. Should never happen", ex);
                        }
                    }
                };
                log.info("getWindows() ontime initial. Class StageHelper found, assuming JDK8");
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                try {
                    GetWindowsSupplier = new Supplier<List<javafx.stage.Window>>() {

                        private final Method methodGetStages = javafx.stage.Window.class.getMethod("getWindows");

                        @Override
                        public List<javafx.stage.Window> get() {
                            try {
                                return new ArrayList<>((List<javafx.stage.Window>)methodGetStages.invoke(null));
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                throw new RuntimeException("getWindows(): com.sun.jacafx.stage.StageHelper.getStages was found, but faild. Should never happen", ex);
                            }
                        }
                    };
                    log.info("getWindows() ontime initial. Class StageHelper not found, so this must be JDK9 or newer");
                } catch (NoSuchMethodException | SecurityException ex1) {
                    throw new RuntimeException("getWindows(): neither StageHelper.getStages nor Window.getWindows was found. Something weird happend, read the source", ex1);
                }

            }
        };
        return GetWindowsSupplier.get();
    }

    @Override
    public void closeOf(UiParent parent) {
        unwrap(parent).ifPresent(s -> FxSaft.run(() -> s.close()));
    }

    @Override
    public void relocate() {
        //TODO: Needs to be tested.
        unwrapMain().ifPresent(m -> {
            log.debug("relocate() relocating mainParent {}", m);
            m.setX(20);
            m.setY(20);
            m.setWidth(800);
            m.setHeight(600);

        });
        int i = 40;

        for (Iterator<Stage> iterator = allStages.stream().map(w -> w.get()).filter(w -> w != null).iterator();
                iterator.hasNext();) {
            Stage w = iterator.next();
            log.debug("relocate() relocating {}", w);
            w.setX(i);
            w.setY(i);
            w.setWidth(800);
            w.setHeight(600);
            i = i + 20;
        }
    }

    private void registerActiveAndToFront(String key, Stage s) {
        log().debug("registerActiveAndToFront(key={})", key);
        ONCES_ACTIVE.put(key, s);
        s.setOnCloseRequest((t) -> {
            log().debug("closeRequest() once closeing {}, removing from active map", key);
            ONCES_ACTIVE.remove(key);
        });
        s.toFront();
    }

    @Override
    public void registerOnce(String key, Core.In<?, ?> in) {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        Objects.requireNonNull(in, "in must not be null");
        if ( javafx.scene.control.Dialog.class.isAssignableFrom(in.clazz()) ) throw new IllegalArgumentException("Dialog ist not supported for registeronce");
        log().debug("registerOnce(key={})", key);
        ONCES_BUILDER.put(key, () -> show(new PreBuilder(saft).frame(true), Optional.empty(), in).thenAccept(w -> registerActiveAndToFront(key, w)));
    }

    @Override
    public boolean showOnce(String key) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        if ( !ONCES_BUILDER.containsKey(key) ) return false;
        if ( ONCES_ACTIVE.containsKey(key) && ONCES_ACTIVE.get(key).isShowing() ) {
            log().debug("showOnce(key={}) visible, focusing", key);
            Platform.runLater(() -> {
                Stage stage = ONCES_ACTIVE.get(key);
                stage.setIconified(false);
                stage.requestFocus();
                stage.toFront();
            });
        } else {
            log.debug("showOnce(key={}) not yet visible, creating.", key);
            ONCES_BUILDER.get(key).run();
        }
        return true;
    }

    @Override
    public <R, S extends R> CompletableFuture<Stage> show(PreBuilder prebuilder, Optional<Callable<?>> preProducer, In<R, S> in) {
        return prepareShowEval(prebuilder, preProducer, in)
                .thenApply((UiParameter p) -> showJavaFx(p))
                .thenApply((UiParameter p) -> p.stage().get());
    }

    @Override
    public <Q, R, S extends R> Result<Q> eval(PreBuilder prebuilder, Optional<Callable<?>> preProducer, In<R, S> in) {
        return new Result<>(prepareShowEval(prebuilder, preProducer, in)
                .thenApply((UiParameter p) -> showAndWaitJavaFx(p))
                .thenApplyAsync((UiParameter p) -> BuilderUtil.waitAndProduceResult(p), saft.executorService()));
    }

    private <R, S extends R> CompletableFuture<UiParameter> prepareShowEval(PreBuilder preBuilder, Optional<Callable<?>> optPreProducer, Core.In<R, S> in) {
        Objects.requireNonNull(preBuilder, "preBuilder must not be null");
        Objects.requireNonNull(optPreProducer, "optPreProducer must not be null");
        Objects.requireNonNull(in, "in must not be null");

        UiParameter.Type type = selectType(in);

        switch (type) {
            case SWING:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceJPanel(in, p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), EventQueue::invokeLater)
                        .thenApplyAsync(BuilderUtil::createSwingNode, Platform::runLater)
                        .thenApplyAsync(BuilderUtil::wrapJPanel, EventQueue::invokeLater)
                        .thenApplyAsync(BuilderUtil::constructJavaFx, Platform::runLater)
                        .handle(saft.handler());

            case DIALOG:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceDialog(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        .thenApplyAsync(BuilderUtil::constructDialog, Platform::runLater)
                        .handle(saft.handler());

            case FX:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> producePane(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        .thenApply(BuilderUtil::constructJavaFx)
                        .handle(saft.handler());

            case FXML:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceFxml(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        .thenApply(BuilderUtil::constructJavaFx)
                        .handle(saft.handler());

            default:
                throw new IllegalArgumentException(type + " not implemented");
        }

    }

    private UiParameter showAndWaitJavaFx(UiParameter in) {
        // Dialog has no stage set
        in.stage().ifPresent(Stage::showAndWait);
        return in;
    }

    private UiParameter showJavaFx(UiParameter in) {
        // Dialog has no stage set
        in.stage().ifPresent(Stage::show);
        return in;
    }

}