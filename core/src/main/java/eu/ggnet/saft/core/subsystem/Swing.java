/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

import java.awt.Component;
import java.awt.Window;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.SwingSaft;
import eu.ggnet.saft.core.ui.UiParent;

/**
 *
 * @author oliver.guenther
 */
// TODO: GlobalWarning. The implementation has some global impact, which must be cleaned up in the final implementation
public class Swing implements Core<Window> {

    private final static Swing DEAD_CORE = new Swing(null) {

        private final Logger log = LoggerFactory.getLogger(Fx.class);

        @Override
        public void parentIfPresent(UiParent parent, Consumer<Window> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public void parentIfPresent(Optional<UiParent> parent, Consumer<Window> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public void parentIfPresent(Consumer<Window> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public Optional<Window> unwrap(UiParent parent) {
            log.warn("unwrap() call on dead core");
            return Optional.empty();
        }

        @Override
        public Optional<Window> unwrap(Optional<UiParent> parent) {
            log.warn("unwrap() call on dead core");
            return Optional.empty();
        }

        @Override
        public Optional<Window> unwrapMain() {
            log.warn("unwrapMain() call on dead core");
            return Optional.empty();
        }

        @Override
        public void shutdown() {
            log.warn("shutdown() call on dead core");
        }

        @Override
        public void add(Window window) {
            log.warn("add() call on dead core");
        }

        @Override
        public Optional<Window> windowAncestor(Node p) {
            log.warn("windowAncestor() call on dead core");
            return Optional.empty();
        }

        @Override
        public void mapParent(JFXPanel fxp) {
            log.warn("mapParent() call on dead core");
        }

        @Override
        public boolean isActiv() {
            return false;
        }
    };

    // TODO: Implement cyclic verification of null weak references and remove elements.
    private final List<WeakReference<Window>> allWindows = new ArrayList<>();

    private final JFrame mainParent;

    private final Logger log = LoggerFactory.getLogger(Swing.class);

    // TODO: Implement a cleanup for all references. Good candidate for WeakReferneces on Key and Value.
    private final Map<Scene, JFXPanel> SWING_PARENT_HELPER = new WeakHashMap<>();

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

    private Swing(JFrame mainParent) {
        // TODO: Global activity. reconsider.
        new JFXPanel(); // Start the Fx platform.
        Platform.setImplicitExit(false);
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

    public static Swing createCore(JFrame mainParent) {
        if ( mainParent != null ) return new Swing(mainParent);
        LoggerFactory.getLogger(Swing.class).warn("createCore(mainParent=null) returning dead core");
        return DEAD_CORE;
    }

}
