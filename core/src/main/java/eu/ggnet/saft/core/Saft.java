/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core;

import java.awt.Component;
import java.awt.Window;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

import javax.swing.JPanel;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.impl.Core;
import eu.ggnet.saft.core.impl.Core.In;
import eu.ggnet.saft.core.impl.Swing;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.*;
import eu.ggnet.saft.core.ui.exception.*;

/**
 * The core of saft, everything that is keept in a singleton way, is registered or held here.
 * One Instance of Saft per Ui/Client. No static values. See the jpro.one runtime restrictions.
 *
 * @author oliver.guenther
 */
public class Saft {

    private final static Core<Object> DEAD_CORE = new Core<Object>() {
        private final Logger log = LoggerFactory.getLogger(Core.class);

        @Override
        public void parentIfPresent(UiParent parent, Consumer<Object> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public void parentIfPresent(Optional<UiParent> parent, Consumer<Object> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public void parentIfPresent(Consumer<Object> consumer) {
            log.warn("parentIfPresent() call on dead core");
        }

        @Override
        public Optional<Object> unwrap(UiParent parent) {
            log.warn("unwrap() call on dead core");
            return Optional.empty();
        }

        @Override
        public Optional<Object> unwrap(Optional<UiParent> parent) {
            log.warn("unwrap() call on dead core");
            return Optional.empty();
        }

        @Override
        public Optional<Object> unwrapMain() {
            log.warn("unwrapMain() call on dead core");
            return Optional.empty();
        }

        @Override
        public void shutdown() {
            log.warn("shutdown() call on dead core");
        }

        @Override
        public void add(Object window) {
            log.warn("add() call on dead core");
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

        @Override
        public void registerOnce(String key, In<?, ?> in) throws NullPointerException, IllegalArgumentException {
            throw new IllegalStateException("registerOnce() call on dead core");
        }

        @Override
        public boolean showOnce(String key) throws NullPointerException {
            throw new IllegalStateException("registerOnce() call on dead core");
        }

        @Override
        public <R, S extends R> CompletableFuture<Object> show(PreBuilder prebuilder, Optional<Callable<?>> preProducer, In<R, S> in) {
            throw new IllegalStateException("registerOnce() call on dead core");
        }

        @Override
        public <Q, R, S extends R> Result<Q> eval(PreBuilder prebuilder, Optional<Callable<?>> preProducer, In<R, S> in) {
            throw new IllegalStateException("registerOnce() call on dead core");
        }

    };

    public final static String HOME = "Home";

    private final LocationStorage locationStorage;

    private final ExecutorService executorService;

    private Optional<GluonSupport> gluonSupport = Optional.empty();

    private final Map<Class<? extends Throwable>, BiConsumer<Optional<UiParent>, ? extends Throwable>> exceptionConsumers = new HashMap<>();

    private Core<?> core = null;

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false); // Shut down handler.

    private final Set<Runnable> onShutdown = new HashSet<>();

    // TODO: Implement a default implementation. Do this after the change in the builder.
    // This implementation only handles parents in swing mode. in Fx mode it's displayed anythere.
    private BiConsumer<Optional<UiParent>, Throwable> exceptionConsumerFinal = (Optional<UiParent> parent, Throwable b) -> {
        if ( b instanceof CancellationException || b.getCause() instanceof CancellationException ) {
            log().debug("FinalExceptionConsumer catches CancellationException, which is ignored by default");
            return;
        }

        Core<Window> sc = Saft.this.core(Swing.class);
        Window p = sc.unwrap(parent).orElse(sc.unwrapMain().orElse(null));

        Runnable r = () -> {
            SwingExceptionDialog.show(p, "Systemfehler", ExceptionUtil.extractDeepestMessage(b),
                    ExceptionUtil.toMultilineStacktraceMessages(b), ExceptionUtil.toStackStrace(b));
        };

        SwingSaft.run(r);
    };

    /**
     * Default Constructor, ready for own implementations.To ensure that no one will make an instance of Saft by error, the constructor is package private.In
     * the classic mode, use {@link UiCore#initGlobal()} and {@link UiCore#global()}
     * .<p>
     * If more that one instance is needed (using multiple cdi container in one vm for example) extend Saft.
     * For transition purposes the {@link UiCore#initGlobal(eu.ggnet.saft.core.Saft) } is designed.
     * </p>
     *
     * @param locationStorage
     * @param executorService
     */
    public Saft(LocationStorage locationStorage, ExecutorService executorService) {
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
    public <T extends Core<V>, V> Core<V> core(Class<T> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass must not be null");
        if ( core == null ) {
            log().warn("core({}) called in, but core is not yet set. Returning dead core");
            return (Core<V>)DEAD_CORE;
        }
        if ( typeClass.isAssignableFrom(core.getClass()) ) {
            return (T)core;
        }

        log().warn("core(typeClass={}) called, but core is {}. Returning dead core", typeClass, core.getClass().getName());
        return (Core<V>)DEAD_CORE;
    }

    /**
     * Returns subsystem core if active or a dead core.
     *
     * @return
     */
    public Core<?> core() {
        if ( core == null ) return DEAD_CORE;
        return core;
    }

    /**
     * Init Saft with the supplied core and first window.
     * May only be called once. But Saft builder can be used before.
     *
     * @param <T>
     * @param core must not be null.
     * @throws NullPointerException  if typeclass or mainParen are null
     * @throws IllegalStateException if core is allready initialised
     */
    public <T extends Core<V>, V> void init(T core) throws NullPointerException, IllegalStateException {
        Objects.requireNonNull(core, "core must not be null");
        if ( this.core != null ) throw new IllegalStateException("Core is allready initialized. Second call not allowed");
        this.core = core;
        log().info("init() complete with core " + core.getClass().getName());

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

    public PreBuilder build() {
        return new PreBuilder(this);
    }

    /**
     * Returns a new Ui builder.
     *
     * @param swingParent optional swing parrent
     * @return a new Ui builder.
     */
    public PreBuilder build(Component swingParent) {
        return new PreBuilder(this).parent(swingParent);
    }

    /**
     * Returns a new Ui builder.
     *
     * @param javaFxParent optional javafx parrent
     * @return a new Ui builder.
     */
    public PreBuilder build(Parent javaFxParent) {
        return new PreBuilder(this).parent(javaFxParent);
    }

    /**
     * Registers a Supplier with a key in the core for once useage.
     *
     * @param key          the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param paneSupplier the supplier of the pane
     * @throws NullPointerException     if the key was null or blank or the supplier was null.
     * @throws IllegalArgumentException if core is not active.
     */
    public void registerOnceFx(String key, Supplier<? extends Pane> paneSupplier) throws NullPointerException, IllegalArgumentException {
        if ( !core().isActiv() ) throw new IllegalArgumentException("core is not active");
        core().registerOnce(key, new In<>(Pane.class, paneSupplier));
    }

    /**
     * Registers a pane class with a key in the core for once useage.
     * Will be created via reflections. Intended usage pattern is in the cdi environment.
     *
     * @param key       the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param paneClass the class of the pane.
     * @throws NullPointerException     if the key was null or blank or the controllerClass was null.
     * @throws IllegalArgumentException if core is not active.
     */
    public void registerOnceFx(String key, Class<? extends Pane> paneClass) throws NullPointerException, IllegalArgumentException {
        if ( !core().isActiv() ) throw new IllegalArgumentException("core is not active");
        core().registerOnce(key, new In<>(paneClass));
    }

    /**
     * Registers a Supplier with a key in the core for once useage.
     *
     * @param key           the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param panelSupplier the supplier for the panel
     * @throws NullPointerException     if the key was null or blank or the supplier was null.
     * @throws IllegalArgumentException if core is not active.
     */
    public void registerOnceSwing(String key, Supplier<? extends JPanel> panelSupplier) throws NullPointerException, IllegalArgumentException {
        if ( !core().isActiv() ) throw new IllegalArgumentException("core is not active");
        core().registerOnce(key, new In<>(JPanel.class, panelSupplier));
    }

    /**
     * Registers a panel class with a key in the core for once useage.
     * Will be created via reflections. Intended usage pattern is in the cdi environment.
     *
     * @param key        the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param panelClass the class of the panel.
     * @throws NullPointerException     if the key was null or blank or the panelClass was null.
     * @throws IllegalArgumentException if core is not active.
     */
    public void registerOnceSwing(String key, Class<? extends JPanel> panelClass) throws NullPointerException, IllegalArgumentException {
        if ( !core().isActiv() ) throw new IllegalArgumentException("core is not active");
        core().registerOnce(key, new In<>(panelClass));
    }

    /**
     * Registers an FxController with a key in the core for once useage.
     *
     * @param key             the unique key for usage with {@link #showOnce(java.lang.String) }.
     * @param controllerClass the fx controller class
     * @throws NullPointerException     if the key was null or blank or the controllerClass was null.
     * @throws IllegalArgumentException if core is not active.
     */
    public void registerOnceFxml(String key, Class<? extends FxController> controllerClass) throws NullPointerException, IllegalArgumentException {
        if ( !core().isActiv() ) throw new IllegalArgumentException("core is not active");
        core().registerOnce(key, new In<>(controllerClass));
    }

    /**
     * Shows a before registerd once element either creating it or refocusing, if still acitve.
     *
     * @param key the registered key
     * @throws NullPointerException     if the key was null or blank.
     * @throws IllegalArgumentException if key was not registerd before or core is not active.
     */
    public void showOnce(String key) throws NullPointerException, IllegalArgumentException {
        if ( !core().isActiv() ) throw new IllegalArgumentException("core is not active");
        boolean result = core().showOnce(key);
        if ( !result ) throw new IllegalArgumentException("key " + key + " was not registered before");
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
    public void handle(Optional<UiParent> parent, Throwable exception) {
        for (Class<? extends Throwable> clazz : exceptionConsumers.keySet()) {
            if ( ExceptionUtil.containsInStacktrace(clazz, exception) ) {
                // The cast is needed, cause of the different generic types in the map. But it is safe because of the way the map is filled. See the register methods.
                BiConsumer<Optional<UiParent>, Throwable> consumer = (BiConsumer<Optional<UiParent>, Throwable>)exceptionConsumers.get(clazz);
                Throwable extractedException = ExceptionUtil.extractFromStraktrace(clazz, exception);
                consumer.accept(parent, extractedException);
                return;
            }
        }
        exceptionConsumerFinal.accept(parent, exception);
    }

    public void handle(Node javafxParentAnchor, Throwable exception) {
        handle(Optional.of(UiParent.of(javafxParentAnchor)), exception);
    }

    public void handle(Component swingParentAnchor, Throwable exception) {
        handle(Optional.of(UiParent.of(swingParentAnchor)), exception);
    }

    public void handle(Throwable exception) {
        handle(Optional.empty(), exception);
    }

    /**
     * Returns a Handler to be used in {@link CompletableFuture#handle(java.util.function.BiFunction) }.
     * Use the register methods to define how exception should be handled.
     *
     * @param <Z>
     * @param parent a ui parent to show there to display this.
     * @return the BiFunction.
     */
    public <Z> BiFunction<Z, Throwable, Z> handler(Optional<UiParent> parent) {
        return new AndFinallyHandler<>(this, parent);
    }

    public <Z> BiFunction<Z, Throwable, Z> handler() {
        return handler(Optional.empty());
    }

    public <Z> BiFunction<Z, Throwable, Z> handler(Node javafxParentAnchor) {
        return handler(Optional.of(UiParent.of(javafxParentAnchor)));
    }

    public <Z> BiFunction<Z, Throwable, Z> handler(Component swingParentAnchor) {
        return handler(Optional.of(UiParent.of(swingParentAnchor)));
    }

    /**
     * Registers an extra renderer for an Exception in any stacktrace. HINT: There is no order or hierachy in the engine. So if you register duplicates or have
     * more than one match in a StackTrace, no one knows what might happen.
     *
     * @param <T>      type of the Exception
     * @param clazz    the class of the Exception
     * @param consumer the consumer to handle it.
     */
    public <T extends Throwable> void registerExceptionConsumer(Class<T> clazz, BiConsumer<Optional<UiParent>, T> consumer) {
        exceptionConsumers.put(clazz, consumer);
    }

    /**
     * Allows to overwrite the default final consumer of all exceptions.
     * Make sure to ignore the {@link UiWorkflowBreak} wrapped into a {@link CompletionException}.
     *
     * @param consumer the consumer, must not be null
     */
    public void overwriteFinalExceptionConsumer(BiConsumer<Optional<UiParent>, Throwable> consumer) {
        exceptionConsumerFinal = Objects.requireNonNull(consumer, "Null for ExceptionConsumer not allowed");
    }

    public void addOnShutdown(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        log().debug("addOnShutdown({})", runnable);
        onShutdown.add(runnable);
    }

    public void shutdown() {
        if ( !shuttingDown.compareAndSet(false, true) ) {
            log().debug("shutdown() called after shutdown. Ignored");
            return;
        }
        log().info("shutdown()");
        log().debug("shutdown() running onShutdown");
        onShutdown.forEach(Runnable::run);
        log().debug("shutdown() shutdown the executor service");
        // TODO: Reconsider, the executorservice could be global in a multisaft environment.
        executorService().shutdownNow();
        core().shutdown();
    }

    protected Logger log() {
        // TODO: Consider Posibility of different loggers/Safts in one vm (jpro.one idea)
        return LoggerFactory.getLogger(Saft.class);
    }

}
