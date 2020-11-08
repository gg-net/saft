/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

import java.awt.Component;
import java.awt.Window;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JFrame;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.UiParent;

/**
 *
 * @author oliver.guenther
 */
public class DefaultCoreFactory implements CoreFactory {

    private final static Fx DEAD_FX_CORE = new Fx(null, null) {

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

        @Override
        public void relocate() {
            log.warn("relocate() call on dead core");
        }

        @Override
        public void closeOf(UiParent parent) {
            log.warn("closeOf() call on dead core");
        }

    };

    private final static Swing DEAD_SWING_CORE = new Swing(null, null) {

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

        @Override
        public void relocate() {
            log.warn("relocate() call on dead core");
        }

        @Override
        public void closeOf(UiParent parent) {
            log.warn("closeOf() call on dead core");
        }

    };

    @Override
    public <T extends Core<V>, V> T dead(Class<T> typeClass) {
        if ( typeClass.equals(Swing.class) ) return (T)DEAD_SWING_CORE;
        if ( typeClass.equals(Fx.class) ) return (T)DEAD_FX_CORE;
        return null;
    }

    /**
     * Returns dead typeless core.
     *
     * @return dead typeless core.
     */
    public Core<?> dead() {
        return DEAD_FX_CORE;
    }

    @Override
    public <T extends Core<V>, V> T create(Saft saft, Class<T> typeClass, V mainParent) {
        if ( typeClass.equals(Swing.class) ) return (T)new Swing(saft, (JFrame)mainParent);
        if ( typeClass.equals(Fx.class) ) return (T)new Fx(saft, (Stage)mainParent);
        return null;
    }

}
