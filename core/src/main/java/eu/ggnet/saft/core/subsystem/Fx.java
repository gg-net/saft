/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.UiParent;

/**
 *
 * @author oliver.guenther
 */
public class Fx implements Core<Stage> {

    public static Fx createCore(Stage mainParent) {
        if ( mainParent != null ) {
            return null; // Kommt gleich
        }
        LoggerFactory.getLogger(Fx.class).warn("createCore(mainParent=null) returning dead core");
        return DEAD_CORE;
    }

    private final static Fx DEAD_CORE = new Fx(null) {

        private final Logger log = LoggerFactory.getLogger(Fx.class);

        @Override
        public void parentIfPresent(UiParent parent, Consumer<Stage> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public void parentIfPresent(Optional<UiParent> parent, Consumer<Stage> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public void parentIfPresent(Consumer<Stage> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public Optional<Stage> unwrap(UiParent parent) {
            log.warn("unwrap() call on dead core");
            return Optional.empty();
        }

        @Override
        public Optional<Stage> unwrap(Optional<UiParent> parent) {
            log.warn("unwrap() call on dead core");
            return Optional.empty();
        }

        @Override
        public Optional<Stage> unwrapMain() {
            log.warn("unwrapMain() call on dead core");
            return Optional.empty();
        }

        @Override
        public void shutdown() {
            log.warn("shutdown() call on dead core");
        }

        @Override
        public void add(Stage window) {
            log.warn("add() call on dead core");
        }

        @Override
        public Stage find(Component c) {
            log.warn("find() call on dead core");
            return null;
        }

        @Override
        public void mapParent(Component c, SwingNode n) {
            log.warn("mapParent() call on dead core");
        }

        @Override
        public boolean isActiv() {
            return false;
        }

    };

    private final Stage mainParent;

    // TODO: Implement cyclic verification of null weak references and remove elements.
    private final List<WeakReference<Stage>> allStages = new ArrayList<>();

    private final Logger log = LoggerFactory.getLogger(Fx.class);

    /**
     * Ceates a new Fx Core, should only be used in Saft.
     *
     * @param mainParent the mainParent, if null core is dead.
     */
    // TODO: Consider a internal Class in Saft or package private handling or else.
    private Fx(Stage mainParent) {
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

}
