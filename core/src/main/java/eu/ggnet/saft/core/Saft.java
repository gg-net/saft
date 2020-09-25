/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core;

import java.awt.Component;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.swing.JPanel;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.GluonSupport;

/**
 * The core of saft, everything that is keept in a singleton way, is registered or held here.
 * One Instance of Saft per Ui/Client. No static values. See the jpro.one runtime restrictions.
 *
 * @author oliver.guenther
 */
public class Saft {

    private final static Logger L = LoggerFactory.getLogger(Saft.class);

    public final static String HOME = "Home";

    private final LocationStorage locationStorage;

    private final ExecutorService executorService;

    private Optional<GluonSupport> gluonSupport = Optional.empty();

    /**
     * Default Constructor, ready for own implementations.
     * To ensure that no one will make an instance of Saft by error, the constructor is package private.
     * In the classic mode, use {@link UiCore#initGlobal()} and {@link UiCore#global() }.
     * <p>
     * If more that one instance is needed (using multiple cdi container in one vm for example) extend Saft.
     * For transition purposes the {@link UiCore#initGlobal(eu.ggnet.saft.core.Saft) } is designed.
     * </p>
     */
    Saft(LocationStorage locationStorage, ExecutorService executorService) {
        this.locationStorage = Objects.requireNonNull(locationStorage, "LocationStorage must not be null");
        this.executorService = Objects.requireNonNull(executorService, "ExecutorService must not be null");
    }

    /**
     * Returns the location storage.
     *
     * @return the location storage.
     */
    public LocationStorage locationStorage() {
        return locationStorage;
    }

    /**
     * Returns the execturor service of saft.
     *
     * @return the execturor service of saft.
     */
    public ExecutorService executorService() {
        return executorService;
    }

    /**
     * Returns a gluon support if gluon is enabled.
     *
     * @return a gluon support if gluon is enabled.
     */
    public Optional<GluonSupport> gluonSupport() {
        return gluonSupport;
    }

    /**
     * Setting the gluon support.
     * By setting the gluon support, gluon is enabled in saft.
     *
     * @param gluonSupport the gluon support.
     */
    public void gluonSupport(GluonSupport gluonSupport) {
        this.gluonSupport = Optional.ofNullable(gluonSupport);
    }

    public void addFx(String name, Supplier<? extends Pane> pane) {

    }

    public void addSwing(String name, Supplier<? extends JPanel> pane) {

    }

    public void addFxml(String name, Class<? extends FxController> controllerClass) {

    }

    public void show(String name) {

    }

    /**
     * Allows the closing of a window from within a Pane or Panel
     * <pre>
     * {@code
     * JFrame f = new JFrame();
     * JPanel p = new JPanel();
     * JButton b = new Button("Close");
     * p.add(b);
     * f.getContentPane().get(p);
     * b.addActionListener(() -> Ui.cloesWindowOf(p);
     * f.setVisible(true);
     * }
     * </pre>.
     *
     * @param c the component which is the closest to the window.
     */
    public void closeWindowOf(Component c) {
        if ( UiCore.isGluon() ) throw new IllegalStateException("closeWindowOf call with a swing component, not allowed in gluon model");
        UiParent.of(c).ifPresent(
                p -> SwingSaft.run(() -> {
                    p.setVisible(false);
                    p.dispose();
                }),
                fx -> FxSaft.run(() -> fx.close()));
    }

    /**
     * Closes the wrapping Window (or equivalent) of the supplied node.
     *
     * @param n the node as refernece.
     */
    public void closeWindowOf(Node n) {
        if ( UiCore.isGluon() ) {
            L.debug("closeWindowOf({}) gluon mode", n);
            UiCore.global().gluonSupport().ifPresent(g -> g.closeViewOrDialogOf(n));
        } else {
            L.debug("closeWindowOf({}) desktop mode", n);
            UiParent.of(n).ifPresent(
                    p -> SwingSaft.run(() -> {
                        p.setVisible(false);
                        p.dispose();
                    }),
                    fx -> FxSaft.run(() -> fx.close()));
        }
    }

    /**
     * Handles an Exception in the Ui, using the registered ExceptionCosumers form
     * {@link UiCore#registerExceptionConsumer(java.lang.Class, java.util.function.Consumer)}.
     *
     * @param b the throwable to be handled.
     */
    public void handle(Throwable b) {
        UiCore.handle(b);
    }

    /**
     * Retruns a handler, to be used in a CompletableFuture.handle().
     *
     * @param <Z> type parameter
     * @return a handler.
     */
    public <Z> BiFunction<Z, Throwable, Z> handler() {
        return (Z t, Throwable u) -> {
            if ( u != null ) Ui.handle(u);
            return null;
        };
    }

    /**
     * Retruns a handler, to be used in a CompletableFuture.handle(), with an and block, also to be executed.
     *
     * @param <Z>      type parameter
     * @param runnable to be run before the final handle.
     * @return a handler.
     */
    public <Z> BiFunction<Z, Throwable, Z> handlerAnd(Runnable runnable) {
        Objects.requireNonNull(runnable, "Runnable must not be null");
        return (Z t, Throwable u) -> {
            try {
                runnable.run();
            } finally {
                if ( u != null ) Ui.handle(u);
            }
            return null;
        };
    }

}
