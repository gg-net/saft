/*
 * Swing and JavaFx Together (Saft)
 * Copyright (C) 2020  Oliver Guenther <oliver.guenther@gg-net.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 with
 * Classpath Exception.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with Classpath Exception along with this program.
 */
package eu.ggnet.saft.core;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Core.In;
import eu.ggnet.saft.core.impl.*;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.PreBuilder;
import eu.ggnet.saft.core.ui.builder.Result;

/**
 * The core of saft, everything that is keept in a singleton way, is registered or held here.
 * One Instance of Saft per Ui/Client. No static values.
 *
 * @author oliver.guenther
 */
public class Saft {

    private final static Core<Object> DEAD_CORE = new Core<Object>() {
        private final Logger log = LoggerFactory.getLogger(Core.class);

        @Override
        public void initMain(Object window) throws NullPointerException, IllegalStateException {
            log.warn("initMain() call on dead core");
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
            throw new IllegalStateException("showOnce() call on dead core");
        }

        @Override
        public <R, S extends R> CompletableFuture<Object> show(PreBuilder prebuilder, Optional<Callable<?>> preProducer, In<R, S> in) {
            throw new IllegalStateException("show() call on dead core");
        }

        @Override
        public <Q, R, S extends R> Result<Q> eval(PreBuilder prebuilder, Optional<Callable<?>> preProducer, In<R, S> in) {
            throw new IllegalStateException("eval() call on dead core");
        }

        @Override
        public void showAlert(String message, Optional<UiParent> uiparent, Optional<String> title, Optional<AlertType> type) throws NullPointerException {
            JOptionPane.showMessageDialog(null, "DeadCore Alert!\n" + message, "DeadCore Alert! " + title.orElse("No Title"), type.orElse(AlertType.WARNING).getOptionPaneType());
        }

    };

    /**
     * Default once Key for a home Ui.
     */
    public final static String HOME = "Home";

    private final LocationStorage locationStorage;

    private final ExecutorService executorService;

    private final Map<Class<? extends Throwable>, BiConsumer<Optional<UiParent>, ? extends Throwable>> exceptionConsumers = new HashMap<>();

    private Core<?> core = null;

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false); // Shut down handler.

    private final Set<Runnable> onShutdown = new HashSet<>();

    // This implementation only handles parents in swing mode. in Fx mode it's displayed anythere.
    private BiConsumer<Optional<UiParent>, Throwable> exceptionConsumerFinal = (Optional<UiParent> parent, Throwable throwable) -> {
        if ( throwable == null ) return;
        if ( throwable instanceof CancellationException ) {
            log().debug("FinalExceptionConsumer catches CancellationException({}), which is ignored by default", throwable.getMessage());
            return;
        }
        if ( throwable instanceof CancellationException || throwable.getCause() instanceof CancellationException ) {
            log().debug("FinalExceptionConsumer catches CancellationException({}), which is ignored by default", throwable.getCause().getMessage());
            return;
        }

        EventQueue.invokeLater(() -> SwingExceptionDialog.show(
                Saft.this.core(Swing.class).unwrap(parent).orElse(Saft.this.core(Swing.class).unwrapMain().orElse(null)),
                "Systemfehler",
                throwable));

    };

    /**
     * Default Constructor, ready for own implementations. In
     * global mode, use {@link UiCore#initGlobal(eu.ggnet.saft.core.Saft) } and {@link UiCore#global()}.
     * <p>
     * If more that one instance is needed (using multiple cdi container in one vm for example) extend Saft.
     * For transition purposes the {@link UiCore#initGlobal(eu.ggnet.saft.core.Saft) } is designed.
     * </p>
     *
     * @param locationStorage the locationstorage, must not be null.
     * @param executorService the excutorservice, must not be null.
     */
    public Saft(LocationStorage locationStorage, ExecutorService executorService) {
        this.locationStorage = Objects.requireNonNull(locationStorage, "LocationStorage must not be null");
        this.executorService = Objects.requireNonNull(executorService, "ExecutorService must not be null");
    }

    /**
     * Returns the core of the supplied type.If no core or a core of another type was initiated, a dead core is returned.
     *
     * @param <T>       type of the core
     * @param <V>       type of the window
     * @param typeClass core class token, must not be null.
     * @return the core of the supplied type or a dead core.
     * @throws NullPointerException if typeClass is null.
     */
    public <T extends Core<V>, V> Core<V> core(Class<T> typeClass) throws NullPointerException {
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
     * Returns the core if initiated or a dead core.
     *
     * @return the core if initiated or a dead core
     */
    public Core<?> core() {
        if ( core == null ) return DEAD_CORE;
        return core;
    }

    /**
     * Init Saft with the supplied core.
     * May only be called once.
     * But Saft builders can be used before.
     *
     * @param <T>  the type of the core
     * @param <V>  the type of the window element of the core.
     * @param core must not be null.
     * @throws NullPointerException  if core is null
     * @throws IllegalStateException if core is allready initialised
     */
    public <T extends Core<V>, V> void init(T core) throws NullPointerException, IllegalStateException {
        Objects.requireNonNull(core, "core must not be null");
        if ( this.core != null ) throw new IllegalStateException("Core is allready initialized. Second call not allowed");
        this.core = core;
        log().info("init() complete with core " + core.getClass().getName());
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
    // Todo: The ExecutorService should not be stopable.
    public ExecutorService executorService() {
        return executorService;
    }

    /**
     * Saft Builder.
     *
     * @return the prebuilder.
     */
    public PreBuilder build() {
        return new PreBuilder(this);
    }

    /**
     * Saft Builder with Swing parent.
     *
     * @param parent an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @return the prebuilder.
     */
    public PreBuilder build(Component parent) {
        return new PreBuilder(this).parent(parent);
    }

    /**
     * Saft Builder with JavaFx parent.
     *
     * @param parent an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @return the prebuilder.
     */
    public PreBuilder build(Parent parent) {
        return new PreBuilder(this).parent(parent);
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
     * Allows the closing of a surrounding window from any enclosing Uielement.
     *
     * @param c the component that is enclosed by the window.
     */
    public void closeWindowOf(Component c) {
        core().closeOf(UiParent.of(c));
    }

    /**
     * Allows the closing of a surrounding window from any enclosing Uielement.
     *
     * @param n the node that is enclosed by the window.
     */
    public void closeWindowOf(Node n) {
        core().closeOf(UiParent.of(n));
    }

    /**
     * Handles the supplied exception via the registered exceptionhandlers and the final exceptionconsumer.
     *
     * @param parent    an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @param exception the exception to handle, if null nothing happens.
     */
    public void handle(Optional<UiParent> parent, Throwable exception) {
        for (Class<? extends Throwable> clazz : exceptionConsumers.keySet()) {
            if ( containsInStacktrace(clazz, exception) ) {
                // The cast is needed, cause of the different generic types in the map. But it is safe because of the way the map is filled. See the register methods.
                BiConsumer<Optional<UiParent>, Throwable> consumer = (BiConsumer<Optional<UiParent>, Throwable>)exceptionConsumers.get(clazz);
                Throwable extractedException = extractFromStraktrace(clazz, exception);
                consumer.accept(parent, extractedException);
                return;
            }
        }
        exceptionConsumerFinal.accept(parent, exception);
    }

    /**
     * Handles the supplied exception via the registered exceptionhandlers and the final exceptionconsumer.
     *
     * @param javafxParentAnchor an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @param exception          the exception to handle, if null nothing happens.
     */
    public void handle(Node javafxParentAnchor, Throwable exception) {
        handle(Optional.of(UiParent.of(javafxParentAnchor)), exception);
    }

    /**
     * Handles the supplied exception via the registered exceptionhandlers and the final exceptionconsumer.
     *
     * @param swingParentAnchor an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @param exception         the exception to handle, if null nothing happens.
     */
    public void handle(Component swingParentAnchor, Throwable exception) {
        handle(Optional.of(UiParent.of(swingParentAnchor)), exception);
    }

    /**
     * Handles the supplied exception via the registered exceptionhandlers and the final exceptionconsumer.
     *
     * @param exception the exception to handle, if null nothing happens.
     */
    public void handle(Throwable exception) {
        handle(Optional.empty(), exception);
    }

    /**
     * Returns a Handler to be used in {@link CompletableFuture#handle(java.util.function.BiFunction) },
     * which uses {@link Saft#handle(java.util.Optional, java.lang.Throwable) } for exception handling.
     * In case of an exception, a {@link CancellationException} is thrown after the handling.
     *
     * @param <Z>    type of incomming value.
     * @param parent an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @return the BiFunction.
     */
    public <Z> BiFunction<Z, Throwable, Z> handler(Optional<UiParent> parent) {
        return new AndFinallyHandler<>(this, parent);
    }

    /**
     * Returns a Handler to be used in {@link CompletableFuture#handle(java.util.function.BiFunction) },
     * which uses {@link Saft#handle(java.util.Optional, java.lang.Throwable) } for exception handling.
     * In case of an exception, a {@link CancellationException} is thrown after the handling.
     *
     * @param <Z> type of incomming value.
     * @return the BiFunction.
     */
    public <Z> BiFunction<Z, Throwable, Z> handler() {
        return handler(Optional.empty());
    }

    /**
     * Returns a Handler to be used in {@link CompletableFuture#handle(java.util.function.BiFunction) },
     * which uses {@link Saft#handle(java.util.Optional, java.lang.Throwable) } for exception handling.
     * In case of an exception, a {@link CancellationException} is thrown after the handling.
     *
     * @param <Z>    type of incomming value.
     * @param parent an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @return the BiFunction.
     */
    public <Z> BiFunction<Z, Throwable, Z> handler(Node parent) {
        return handler(Optional.of(UiParent.of(parent)));
    }

    /**
     * Returns a Handler to be used in {@link CompletableFuture#handle(java.util.function.BiFunction) },
     * which uses {@link Saft#handle(java.util.Optional, java.lang.Throwable) } for exception handling.
     * In case of an exception, a {@link CancellationException} is thrown after the handling.
     *
     * @param <Z>    type of incomming value.
     * @param parent an optional uielement enclosed by the window which should be the parent, if null main is used.
     * @return the BiFunction.
     */
    public <Z> BiFunction<Z, Throwable, Z> handler(Component parent) {
        return handler(Optional.of(UiParent.of(parent)));
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
     * Make sure to ignore the {@link CancellationException} as this is default cancel closing behavior of all windows created by the builders.
     *
     * @param consumer the consumer to handle the exception, must not be null
     * @throws NullPointerException if consumer is null.
     */
    public void overwriteFinalExceptionConsumer(BiConsumer<Optional<UiParent>, Throwable> consumer) throws NullPointerException {
        exceptionConsumerFinal = Objects.requireNonNull(consumer, "Null for ExceptionConsumer not allowed");
    }

    /**
     * Register a shutdown listener.
     *
     * @param runnable a runable to be called on shutdown, must not be null.
     * @throws NullPointerException if runnable is null.
     */
    public void addOnShutdown(Runnable runnable) throws NullPointerException {
        Objects.requireNonNull(runnable, "runnable must not be null");
        log().debug("addOnShutdown({})", runnable);
        onShutdown.add(runnable);
    }

    /**
     * Shutdown this saft.
     */
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

    private Logger log() {
        // TODO: Consider Posibility of different loggers/Safts in one vm (jpro.one idea)
        return LoggerFactory.getLogger(Saft.class);
    }

    private boolean containsInStacktrace(Class<?> clazz, Throwable ex) {
        if ( ex == null ) return false;
        if ( ex.getClass().equals(clazz) ) return true;
        return containsInStacktrace(clazz, ex.getCause());
    }

    private <T extends Throwable> T extractFromStraktrace(Class<T> clazz, Throwable ex) {
        if ( ex == null ) throw new NullPointerException("No Class in Stacktrace : " + clazz);
        if ( ex.getClass().equals(clazz) ) return (T)ex;
        return extractFromStraktrace(clazz, ex.getCause());
    }

}
