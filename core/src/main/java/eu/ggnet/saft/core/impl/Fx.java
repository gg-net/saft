/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JComponent;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiCore;
import eu.ggnet.saft.core.ui.AlertType;
import eu.ggnet.saft.core.ui.UiParent;
import eu.ggnet.saft.core.ui.builder.*;

/**
 *
 * @author oliver.guenther
 */
public class Fx extends AbstractCore implements Core<Stage> {

    private final Saft saft;

    private Stage mainStage;

    // TODO: Implement cyclic verification of null weak references and remove elements.
    private final List<WeakReference<Stage>> allStages = new ArrayList<>();

    private final static Logger L = LoggerFactory.getLogger(Fx.class);

    // TODO: Implement a cleanup for all references. Good candidate for WeakReferneces on Key and Value.
    private final Map<Component, SwingNode> JAVAFX_PARENT_HELPER = new HashMap<>();

    /**
     * Contains all active once windows.
     */
    private final Map<String, Stage> ONCES_ACTIVE = new HashMap<>();

    /**
     * Contains all wrapped builders to return the window for future usage.
     */
    private final Map<String, Runnable> ONCES_BUILDER = new HashMap<>();

    private final Callback<Class<?>, Object> INSTANCE_INITIALZER;

    public Fx(Saft saft) {
        this(saft, null, null);
    }

    public Fx(Saft saft, Callback<Class<?>, Object> initialzier) {
        this(saft, null, initialzier);
    }

    /**
     * Ceates a new Fx Core, should only be used in Saft.
     *
     * @param saft       the saft using this core.
     * @param mainParent the mainParent.
     */
    public Fx(Saft saft, Stage mainParent) {
        this(saft, mainParent, null);
    }

    public Fx(Saft saft, Stage mainParent, Callback<Class<?>, Object> initialzier) {
        this.saft = Objects.requireNonNull(saft, "saft must not be null");
        this.mainStage = mainParent;
        this.INSTANCE_INITIALZER = initialzier;
    }

    @Override
    public void initMain(Stage stage) {
        if ( this.mainStage != null ) throw new IllegalStateException("mainStage is allready set");
        if ( stage == null ) throw new NullPointerException("stage must not be null");
        L.debug("initMain(stage={})", stage.getClass().getName());
        this.mainStage = stage;
        // TODO: Doku, that in fx, it should be called in the Application stop method.
        //  mainStage.setOnCloseRequest(t -> saft.shutdown());
    }

    @Override
    protected Optional<Callback<Class<?>, Object>> initializer() {
        return Optional.ofNullable(INSTANCE_INITIALZER);
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
                L.warn("unwrap() uiparent.node() was set, node had a scene but window was null");
                return Optional.empty();
            }
            if ( !(window instanceof Stage) ) {
                L.warn("unwrap() uiparent.node() was set, node had a scene and window, but window was not of type stage");
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
        return Optional.of(mainStage);
    }

    @Override
    public void add(Stage window) {
        allStages.add(new WeakReference<>(window));
    }

    @Override
    public void shutdown() {
        allStages.forEach(w -> Optional.ofNullable(w.get()).ifPresent(s -> s.hide()));
        // TODO: This is a global call. In the multiple safts in one vm, this cannot be used. Some other semantic is needed.
        getWindows().stream().filter(w -> w != mainStage).forEach(javafx.stage.Window::hide); // close/hide all free stages.
    }

    @Override
    public boolean isActiv() {
        return true;
    }

    /**
     * Returns the Stage containing the swingnode with the component or null if not found.
     *
     * @param c the component
     * @return the stage or null
     */
    // TODO: Look into the future if we need the stage or can use the window.
    private Stage find(Component c) {
        L.debug("find({})", c);
        SwingNode sn = deepfind(Objects.requireNonNull(c, "Component for find is null"));
        if ( sn == null ) return null;
        javafx.stage.Window window = sn.getScene().getWindow();
        if ( window instanceof Stage ) return (Stage)window;
        return null;
    }

    private SwingNode deepfind(Component c) {
        L.debug("deep({})", c);
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
                L.info("getWindows() ontime initial. Class StageHelper found, assuming JDK8");
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
                    L.info("getWindows() ontime initial. Class StageHelper not found, so this must be JDK9 or newer");
                } catch (NoSuchMethodException | SecurityException ex1) {
                    throw new RuntimeException("getWindows(): neither StageHelper.getStages nor Window.getWindows was found. Something weird happend, read the source", ex1);
                }

            }
        };
        return GetWindowsSupplier.get();
    }

    @Override
    public void closeOf(UiParent parent) {
        unwrap(parent).ifPresent(s -> run(() -> s.close()));
    }

    @Override
    public void relocate() {
        //TODO: Needs to be tested.
        unwrapMain().ifPresent(m -> {
            L.debug("relocate() relocating mainParent {}", m);
            m.setX(20);
            m.setY(20);
            m.setWidth(800);
            m.setHeight(600);

        });
        int i = 40;

        for (Iterator<Stage> iterator = allStages.stream().map(w -> w.get()).filter(w -> w != null).iterator();
                iterator.hasNext();) {
            Stage w = iterator.next();
            L.debug("relocate() relocating {}", w);
            w.setX(i);
            w.setY(i);
            w.setWidth(800);
            w.setHeight(600);
            i = i + 20;
        }
    }

    private void registerActiveAndToFront(String key, Stage s) {
        L.debug("registerActiveAndToFront(key={})", key);
        ONCES_ACTIVE.put(key, s);
        s.setOnCloseRequest((t) -> {
            L.debug("closeRequest() once closeing {}, removing from active map", key);
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
        L.debug("registerOnce(key={})", key);
        ONCES_BUILDER.put(key, () -> show(new PreBuilder(saft).frame(true), Optional.empty(), in).thenAccept(w -> registerActiveAndToFront(key, w)));
    }

    @Override
    public boolean showOnce(String key) throws NullPointerException {
        Objects.requireNonNull(key, "key must not be null");
        if ( key.trim().isEmpty() ) throw new NullPointerException("key must not be blank");
        if ( !ONCES_BUILDER.containsKey(key) ) return false;
        if ( ONCES_ACTIVE.containsKey(key) && ONCES_ACTIVE.get(key).isShowing() ) {
            L.debug("showOnce(key={}) visible, focusing", key);
            Platform.runLater(() -> {
                Stage stage = ONCES_ACTIVE.get(key);
                stage.setIconified(false);
                stage.requestFocus();
                stage.toFront();
            });
        } else {
            L.debug("showOnce(key={}) not yet visible, creating.", key);
            ONCES_BUILDER.get(key).run();
        }
        return true;
    }

    @Override
    public void showAlert(String message, Optional<UiParent> uiparent, Optional<String> title, Optional<AlertType> type) throws NullPointerException {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(uiparent, "uiparent must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(type, "type must not be null");
        runAndWait(() -> {
            Alert alert = new Alert(type.orElse(AlertType.INFO).getJavaFxType());
            Optional<Stage> owner = unwrap(uiparent);
            if ( !owner.isPresent() ) owner = unwrapMain();
            owner.ifPresent(s -> alert.initOwner(s));
            alert.setTitle(title.orElse("Information"));
            alert.setContentText(message);
            alert.showAndWait();
        });
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
                .thenApplyAsync((UiParameter p) -> waitAndProduceResult(p), saft.executorService()));
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
                        .thenApplyAsync(p -> createSwingNode(p), Platform::runLater)
                        .thenApplyAsync(p -> wrapJPanel(p), EventQueue::invokeLater)
                        .thenApplyAsync(p -> constructJavaFx(p), Platform::runLater)
                        .handle(saft.handler());

            case DIALOG:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceDialog(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        .thenApplyAsync(p -> constructDialog(p), Platform::runLater)
                        .handle(saft.handler());

            case FX:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> producePane(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        .thenApply(p -> constructJavaFx(p))
                        .handle(saft.handler());

            case FXML:
                return CompletableFuture
                        .supplyAsync(() -> init(preBuilder, type), saft.executorService())
                        .thenApplyAsync(p -> produceFxml(in, p), Platform::runLater)
                        .thenApplyAsync(p -> optionalRunPreProducer(p, optPreProducer), saft.executorService())
                        .thenApplyAsync(p -> optionalConsumePreProducer(p), Platform::runLater)
                        .thenApply(p -> constructJavaFx(p))
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

    /**
     * Call from EventQueue: Wraps the expected uiparameter.jpanel in the expected pane with a swingnode as children.
     * Also updates the global parent mapping and the prefered size of the pane
     *
     * @param in the uiparamter
     * @return the uiparamter
     */
    private UiParameter wrapJPanel(UiParameter in) {
        Pane pane = in.pane().orElseThrow(() -> new NoSuchElementException("Pane in UiParameter is null"));
        JComponent jpanel = in.jPanel().orElseThrow(() -> new NoSuchElementException("JPanel in UiParameter is null"));
        if ( pane.getChildren().isEmpty() ) throw new IllegalStateException("Supplied Pane has no children, but a SwingNode is expected");
        SwingNode sn = pane.getChildren().stream()
                .filter(n -> n instanceof SwingNode)
                .map(n -> (SwingNode)n)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No Node of the supplied Pane is of type SwingNode"));
        sn.setContent(jpanel);
        JAVAFX_PARENT_HELPER.put(jpanel, sn);

        Dimension preferredSize = jpanel.getPreferredSize();
        L.debug("wrapJPanel(in): setting in.pane().prefSize from in.jPanel().preferredSize={}", preferredSize);
        pane.setPrefHeight(preferredSize.getHeight());
        pane.setPrefWidth(preferredSize.getWidth());
        return in;
    }

    private UiParameter constructJavaFx(UiParameter in) {
        Pane pane = in.pane().get();
        Stage stage = new Stage();

        if ( !in.extractFrame() ) {
            Optional<Stage> owner = unwrap(in.uiParent());
            if ( !owner.isPresent() ) owner = unwrapMain();
            owner.ifPresent(s -> stage.initOwner(s));
        }
        in.modality().ifPresent(m -> stage.initModality(m));

        StringProperty titleProperty = in.toTitleProperty();
        stage.titleProperty().set(titleProperty.get());
        in.toTitleProperty().addListener((ob, o, n) -> Platform.runLater(() -> stage.titleProperty().set(n)));

        in.showingProperty().ifPresent(s -> {
            s.set(false);
            stage.showingProperty().addListener((ob, o, n) -> s.set(n));
            s.addListener((ob, o, n) -> {
                if ( !n ) stage.close();
            });
        });

        stage.getIcons().addAll(loadJavaFxImages(in.extractReferenceClass()));
        in.saft().core(Fx.class).add(stage);
        if ( in.isStoreLocation() ) registerAndSetStoreLocation(in.extractReferenceClass(), stage);
        in.getClosedListenerImplemetation().ifPresent(elem -> stage.setOnCloseRequest(e -> elem.closed()));
        stage.setScene(new Scene(pane));
        return in.toBuilder().stage(stage).build();
    }

    private UiParameter constructDialog(UiParameter in) {
        javafx.scene.control.Dialog<?> dialog = in.dialog().get();
        if ( !in.extractFrame() ) {
            Optional<Stage> owner = unwrap(in.uiParent());
            if ( !owner.isPresent() ) owner = unwrapMain();
            owner.ifPresent(s -> dialog.initOwner(s));
        }
        in.modality().ifPresent(m -> dialog.initModality(m));
        // in.toTitleProperty().addListener((ob, o, n) -> dialog.setTitle(n)); // In Dialog, we use the nativ implementation
        // stage.getIcons().addAll(loadJavaFxImages(in.getRefernceClass())); // Not in dialog avialable.
        if ( in.isStoreLocation() ) throw new IllegalArgumentException("Dialog with store location mode is not supported yet");
        in.getClosedListenerImplemetation().ifPresent(elem -> dialog.setOnCloseRequest(e -> elem.closed()));
        dialog.showAndWait();
        return in;
    }

    /**
     * Call from Platform: creates a SwingNode in a BorderPane and sets the pane on in
     *
     * @param in the uiparameter
     * @return the modified uiparameter
     */
    private UiParameter createSwingNode(UiParameter in) {
        SwingNode sn = new SwingNode();
        BorderPane p = new BorderPane(sn);
        return in.toBuilder().pane(p).build();
    }

    private void registerAndSetStoreLocation(Class<?> key, javafx.stage.Stage window) {
        UiCore.global().locationStorage().loadLocation(key, window);
        window.addEventHandler(javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST, e -> UiCore.global().locationStorage().storeLocation(key, window));
    }

    private java.util.List<javafx.scene.image.Image> loadJavaFxImages(Class<?> reference) {
        return IconConfig.possibleIcons(reference).stream()
                .map(n -> reference.getResourceAsStream(n))
                .filter(u -> u != null)
                .map(r -> new javafx.scene.image.Image(r))
                .collect(Collectors.toList());
    }

    /**
     * Dispatches the Callable to the Platform Ui Thread. If this method is called on the javafx ui thread, the supplied callable is called,
     * otherwise the exection on Platform.runLater ist synchrnized via a latch. This Method is blocking.
     *
     * @param <T>      Return type of callable
     * @param callable the callable to dispatch
     * @return the result of the callable
     * @throws RuntimeException wraps InterruptedException of {@link CountDownLatch#await() } and ExecutionException of {@link FutureTask#get() }
     */
    private void runAndWait(Runnable runnable) throws RuntimeException {
        if ( Platform.isFxApplicationThread() ) {
            runnable.run();
            return;
        }
        try {
            final CountDownLatch cdl = new CountDownLatch(1);
            Platform.runLater(() -> {
                runnable.run();
                cdl.countDown();
            });
            cdl.await();
        } catch (InterruptedException ex) {
            saft.handle(ex);
        }
    }

    // Internal api
    /**
     * Run on the application thread, but looking into if we are on it already.
     *
     * @param r a runnable.
     */
    private void run(Runnable r) {
        if ( Platform.isFxApplicationThread() ) r.run();
        else Platform.runLater(r);
    }

}
