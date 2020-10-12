/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core;

import java.awt.Component;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.swing.JPanel;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.subsystem.Core;
import eu.ggnet.saft.core.subsystem.DefaultCoreFactory;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.GluonSupport;
import eu.ggnet.saft.core.ui.builder.UiWorkflowBreak;
import eu.ggnet.saft.core.ui.exception.AndFinallyHandler;
import eu.ggnet.saft.core.ui.exception.ExceptionUtil;

/**
 * The core of saft, everything that is keept in a singleton way, is registered or held here.
 * One Instance of Saft per Ui/Client. No static values. See the jpro.one runtime restrictions.
 *
 * @author oliver.guenther
 */
public class Saft {

    private final static Logger L = LoggerFactory.getLogger(Saft.class);

    public final static String HOME = "Home";

    private final static DefaultCoreFactory DEFAULT_CORE_FACTORY = new DefaultCoreFactory();

    private final LocationStorage locationStorage;

    private final ExecutorService executorService;

    private Optional<GluonSupport> gluonSupport = Optional.empty();

    private final Map<Class<? extends Throwable>, ParentShowConsume<? extends Throwable>> exceptionConsumers = new HashMap<>();

    private Core<?> core = null;

    private ParentShowConsume<Throwable> exceptionConsumerFinal = null; // TODO: Implement a default implementation. Do this after the change in the builder.

//    private static Consumer<Throwable> finalConsumer = (b) -> {
//        if ( b instanceof UiWorkflowBreak || b.getCause() instanceof UiWorkflowBreak ) {
//            L.debug("FinalExceptionConsumer catches UiWorkflowBreak, which is ignored by default");
//            return;
//        }
//        Runnable r = () -> {
//            SwingExceptionDialog.show(SwingCore.mainFrame(), "Systemfehler", ExceptionUtil.extractDeepestMessage(b),
//                    ExceptionUtil.toMultilineStacktraceMessages(b), ExceptionUtil.toStackStrace(b));
//        };
//
//        if ( EventQueue.isDispatchThread() ) r.run();
//        else {
//            try {
//                EventQueue.invokeAndWait(r);
//            } catch (InterruptedException | InvocationTargetException e) {
//                // This will never happen.
//            }
//        }
//
//    };
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
     * Returns the subsystem of
     *
     * @param <T>
     * @param typeClass
     * @return
     */
    public <T extends Core<V>, V> T core(Class<T> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass must not be null");
        if ( core == null ) {
            L.warn("core({}) called in, but core is not yet set. Returning dead core");
            // TODO: Implement ServiceLoader for different cores later.

            return DEFAULT_CORE_FACTORY.dead(typeClass);
        }
        if ( typeClass.isAssignableFrom(core.getClass()) ) {
            return (T)core;
        }

        L.warn("core({}) called in, but core is {}. Returning dead core", core.getClass().getName());

        // TODO: Implement ServiceLoader for different cores later.
        return DEFAULT_CORE_FACTORY.dead(typeClass);
    }

    /**
     * Returns subsystem core if active or a dead core.
     *
     * @return
     */
    public Core<?> core() {
        if ( core == null ) {
            return DEFAULT_CORE_FACTORY.dead();
        }
        return core;
    }

    /**
     * Init Saft with the supplied core and first window.
     * May only be called once. But Saft builder can be used before.
     *
     * @param <T>
     * @param typeClass  must not be null.
     * @param mainParent must not be null.
     * @throws NullPointerException if typeclass or mainParen are null
     */
    public <T> void init(Class<Core<T>> typeClass, T mainParent) throws NullPointerException {
        Objects.requireNonNull(typeClass, "typeClass must not be null");
        Objects.requireNonNull(mainParent, "mainParent must not be null");
        if ( core != null ) throw new IllegalStateException("Core is allready initialized. Second call not allowed");
        // TODO: Implement ServiceLoader for different cores later.

        if ( core == null ) core = DEFAULT_CORE_FACTORY.create(typeClass);
        if ( core == null ) throw new IllegalStateException("No CoreFactory produced a core for type " + typeClass.getName());
        // TODO: All Knowledge of continue and start must be merged here.
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
    // TODO: Reconsider at the end, if this method still makes sense
    public void closeWindowOf(Component c) {
        // TODO: Remember this later
        // if ( UiCore.isGluon() ) throw new IllegalStateException("closeWindowOf call with a swing component, not allowed in gluon model");
        core().closeOf(UiParent.of(c));
    }

    /**
     * Closes the wrapping Window (or equivalent) of the supplied node.
     *
     * @param n the node as refernece.
     */
    // TODO: Reconsider at the end, if this method still makes sense
    public void closeWindowOf(Node n) {
        // TODO: Remember this later
//        if ( UiCore.isGluon() ) {
//            L.debug("closeWindowOf({}) gluon mode", n);
//            UiCore.global().gluonSupport().ifPresent(g -> g.closeViewOrDialogOf(n));
//        } else {
        core().closeOf(UiParent.of(n));
    }

    /**
     * Tries to map any exception in the stacktrace to a registered exceptionhandler or uses the final exceptionconsumer.
     *
     * @param parent    an optional parent, if null main is used.
     * @param exception the exception to handle, if null nothing happens.
     */
    public void handle(UiParent parent, Throwable exception) {
        for (Class<? extends Throwable> clazz : exceptionConsumers.keySet()) {
            if ( ExceptionUtil.containsInStacktrace(clazz, exception) ) {
                // The cast is needed, cause of the different generic types in the map. But it is safe because of the way the map is filled. See the register methods.
                ParentShowConsume<Throwable> consumer = (ParentShowConsume<Throwable>)exceptionConsumers.get(clazz);
                Throwable extractedException = ExceptionUtil.extractFromStraktrace(clazz, exception);
                // TODO: Think about the parent. Who shound handle the null and find the main.
                consumer.show(parent, extractedException);
                return;
            }
        }
        exceptionConsumerFinal.show(parent, exception);
    }

    public void handle(Node javafxParentAnchor, Throwable exception) {
        handle(UiParent.of(javafxParentAnchor), exception);
    }

    public void handle(Component swingParentAnchor, Throwable exception) {
        handle(UiParent.of(swingParentAnchor), exception);
    }

    public void handle(Throwable exception) {
        handle((UiParent)null, exception);
    }

    /**
     * Returns a Handler to be used in {@link CompletableFuture#handle(java.util.function.BiFunction) }.
     * Use the register methods to define how exception should be handled.
     *
     * @param <Z>
     * @param parent a ui parent to show there to display this.
     * @return the BiFunction.
     */
    public <Z> BiFunction<Z, Throwable, Z> handler(UiParent parent) {
        return new AndFinallyHandler<>(this, parent);
    }

    public <Z> BiFunction<Z, Throwable, Z> handler() {
        return handler((UiParent)null);
    }

    public <Z> BiFunction<Z, Throwable, Z> handler(Node javafxParentAnchor) {
        return handler(UiParent.of(javafxParentAnchor));
    }

    public <Z> BiFunction<Z, Throwable, Z> handler(Component swingParentAnchor) {
        return handler(UiParent.of(swingParentAnchor));
    }

    /**
     * Registers an extra renderer for an Exception in any stacktrace. HINT: There is no order or hierachy in the engine. So if you register duplicates or have
     * more than one match in a StackTrace, no one knows what might happen.
     *
     * @param <T>      type of the Exception
     * @param clazz    the class of the Exception
     * @param consumer the consumer to handle it.
     */
    public <T extends Throwable> void registerExceptionConsumer(Class<T> clazz, ParentShowConsume<T> consumer) {
        exceptionConsumers.put(clazz, consumer);
    }

    /**
     * Allows to overwrite the default final consumer of all exceptions.
     * Make sure to ignore the {@link UiWorkflowBreak} wrapped into a {@link CompletionException}.
     *
     * @param consumer the consumer, must not be null
     */
    public void overwriteFinalExceptionConsumer(ParentShowConsume<Throwable> consumer) {
        exceptionConsumerFinal = Objects.requireNonNull(consumer, "Null for ExceptionConsumer not allowed");
    }

}
