package eu.ggnet.saft.core;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.GluonSupport;
import eu.ggnet.saft.core.ui.builder.UiWorkflowBreak;
import eu.ggnet.saft.core.ui.exception.ExceptionUtil;
import eu.ggnet.saft.core.ui.exception.SwingExceptionDialog;

/**
 * The Core of the Saft UI, containing methods for startup or registering things.
 *
 * @author oliver.guenther
 */
public class UiCore {

    private final static Logger L = LoggerFactory.getLogger(UiCore.class);

    // Package private for Ui usage.
    final static ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactory() {

        private final ThreadGroup group = new ThreadGroup("saft-uicore-pool");

        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(group, r, "Thread-" + counter.incrementAndGet() + "-" + r.toString());
        }
    });

    private static final BooleanProperty BACKGROUND_ACTIVITY = new SimpleBooleanProperty();

    private static final Map<Class<?>, Consumer> EXCEPTION_CONSUMER = new HashMap<>();

    private static final Set<Runnable> ON_SHUTDOWN = new HashSet<>();

    private static JFrame mainFrame = null; // Frame in Swing Mode

    private static Stage mainStage = null; // Frame in Fx Mode

    private static boolean gluon = false; // Special FX Gloun mode. See https://gluonhq.com/products/mobile/

    private static AtomicBoolean shuttingDown = new AtomicBoolean(false); // Shut down handler.

    private static Consumer<Throwable> finalConsumer = (b) -> {
        if ( b instanceof UiWorkflowBreak || b.getCause() instanceof UiWorkflowBreak ) {
            L.debug("FinalExceptionConsumer catches UiWorkflowBreak, which is ignored by default");
            return;
        }
        Runnable r = () -> {
            SwingExceptionDialog.show(SwingCore.mainFrame(), "Systemfehler", ExceptionUtil.extractDeepestMessage(b),
                    ExceptionUtil.toMultilineStacktraceMessages(b), ExceptionUtil.toStackStrace(b));
        };

        if ( EventQueue.isDispatchThread() ) r.run();
        else {
            try {
                EventQueue.invokeAndWait(r);
            } catch (InterruptedException | InvocationTargetException e) {
                // This will never happen.
            }
        }

    };

    /**
     * Returns the mainFrame in swing mode, otherwise null.
     *
     * @return the mainFrame in swing mode, otherwise null
     */
    public static JFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Returns the mainStage in fx mode, otherwise null.
     *
     * @return the mainStage in fx mode, otherwise null
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /**
     * Returns the Executor of the Ui.
     *
     * @return the Executor of the Ui.
     */
    public static Executor getExecutor() {
        return EXECUTOR_SERVICE;
    }

    /**
     * Returns a property that represents background activity.
     *
     * @return a property that represents background activity
     */
    // TODO: This is a very simple solution for now.
    public static BooleanProperty backgroundActivityProperty() {
        return BACKGROUND_ACTIVITY;
    }

    /**
     * interim Mode, Saft connects to a running environment.
     *
     * @param mainView the mainView to continue on.
     */
    public static void continueSwing(JFrame mainView) {
        if ( isRunning() ) throw new IllegalStateException("UiCore is already initialised and running");
        SwingCore.ensurePlatformIsRunning();
        Platform.setImplicitExit(false); // Need this, as we asume many javafx elements opening and closing.
        mainFrame = mainView;
        mainView.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                for (WeakReference<Window> windowRef : SwingCore.ACTIVE_WINDOWS.values()) {
                    if ( windowRef.get() == null ) continue;
                    windowRef.get().setVisible(false); // Close all windows.
                    windowRef.get().dispose();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                UiCore.shutdown();
            }

        });
        postStartUp();
    }

    /**
     * Adds a shutdown listener.
     *
     * @param runnable the runnable to called on shutdown
     */
    public static void addOnShutdown(Runnable runnable) {
        if ( runnable == null ) return;
        L.info("Adding on Shutdown {}", runnable);
        ON_SHUTDOWN.add(runnable);
    }

    /**
     * Starts the Core in Swing mode, may only be called once.
     *
     * @param <T>     type parameter
     * @param builder the builder for swing.
     */
    public static <T extends Component> void startSwing(final Callable<T> builder) {
        if ( isRunning() ) throw new IllegalStateException("UiCore is already initialised and running");
        SwingCore.ensurePlatformIsRunning();
        try {
            JFrame panel = SwingSaft.dispatch(() -> {
                T node = builder.call();
                JFrame p = new JFrame();
                p.setTitle(TitleUtil.title(node.getClass()));
                p.setName(TitleUtil.title(node.getClass()));
                p.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                p.getContentPane().add(builder.call());
                p.pack();
                p.setLocationByPlatform(true);
                p.setVisible(true);
                return p;
            });
            continueSwing(panel);
        } catch (InterruptedException | InvocationTargetException | ExecutionException ex) {
            handle(ex);
        }
    }

    /**
     * Starts the Ui in JavaFx variant.
     * <p>
     * This also assumes two things:
     * <ul>
     * <li>The JavaFX Platform is already running (as a Stage already exists), most likely created through default lifecycle of javaFx</li>
     * <li>This Stage will always be open or the final to be closed, so implicitExit is ok</li>
     * </ul>
     *
     * @param <T>          type restriction.
     * @param primaryStage the primaryStage for the application, not yet visible.
     * @param builder      the build for the main ui.
     */
    public static <T extends Parent> void startJavaFx(final Stage primaryStage, final Callable<T> builder) {
        if ( isRunning() ) throw new IllegalStateException("UiCore is already initialised and running");
        mainStage = primaryStage;
        FxSaft.dispatch(() -> {
            T node = builder.call();
            primaryStage.setTitle(TitleUtil.title(node.getClass()));
            primaryStage.setScene(new Scene(node));
            primaryStage.centerOnScreen();
            primaryStage.sizeToScene();
            primaryStage.show();
            primaryStage.setOnCloseRequest((e) -> {
                UiCore.shutdown();
            });
            return null;
        });
        postStartUp();
    }

    /**
     * Contiunes the Ui in the Gloun mode with JavaFx.
     * This metod is intended to be used in the MobileApplication.postInit(Scene)
     *
     * @param <T>   type restriction.
     * @param scene the first and only scene of gluon.
     */
    public static <T extends Parent> void continueGluon(final Scene scene) {
        if ( isRunning() ) throw new IllegalStateException("UiCore is already initialised and running");

        try {
            String clazzName = "eu.ggnet.saft.gluon.Gi";
            String methodName = "startUp";
            L.debug("continueGluon(): trying to start gluon specific code: reflective call to {}.{}", clazzName, methodName);
            Class<?> clazz = Class.forName(clazzName);
            Method method = clazz.getMethod(methodName);
            method.invoke(null);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        if ( !Dl.local().optional(GluonSupport.class).isPresent() )
            throw new IllegalStateException("Trying to active gluon mode, but no local Service implementation of GluonSupport found. Is the dependency saft-gluon available ?");
        mainStage = ((Stage)scene.getWindow());
        L.info("Starting SAFT in Gloun Mode, using MainStage {}", mainStage);
        gluon = true;
        mainStage.setOnCloseRequest((e) -> {
            UiCore.shutdown();
        });
        postStartUp();
    }

    /**
     * Registers an extra renderer for an Exception in any stacktrace. HINT: There is no order or hierachy in the engine. So if you register duplicates or have
     * more than one match in a StackTrace, no one knows what might happen.
     *
     * @param <T>      type of the Exception
     * @param clazz    the class of the Exception
     * @param consumer the consumer to handle it.
     */
    public static <T> void registerExceptionConsumer(Class<T> clazz, Consumer<T> consumer) {
        EXCEPTION_CONSUMER.put(clazz, consumer);
    }

    /**
     * Allows to overwrite the default final consumer of all exceptions.
     * Make sure to ignore the {@link UiWorkflowBreak} wrapped into a {@link CompletionException}.
     *
     * @param <T>      type of consumer
     * @param consumer the consumer, must not be null
     */
    public static <T> void overwriteFinalExceptionConsumer(Consumer<Throwable> consumer) {
        finalConsumer = Objects.requireNonNull(consumer, "Null for ExceptionConsumer not allowed");
    }

    /**
     * Running means, that one startXXX oder contiuneXXX method was called.
     *
     * @return true, if running.
     */
    public static boolean isRunning() {
        return mainFrame != null || mainStage != null;
    }

    /**
     * Fx mode.
     *
     * @return true if in fx mode.
     */
    public static boolean isFx() {
        return (mainStage != null);
    }

    /**
     * Returns true if Saft is running in a special fx mode, modified for gluon mobile. (https://gluonhq.com/products/mobile/)
     *
     * @return true if in gluon mode.
     */
    public static boolean isGluon() {
        return gluon;
    }

    /**
     * Returns true if in swing mode.
     *
     * @return true if in swing mode
     */
    public static boolean isSwing() {
        return (mainFrame != null);
    }

    /**
     * Shutdown the core, cleaning up every thing.
     * This should be called via listerners on the mainview or mainframe, but at least in gluon it dosn't work.
     * Tries to close all windows. Stops the executor. Other tasks.
     * Thread safe, may be called multiple times, will only excute once.
     */
    public static void shutdown() {
        if ( !shuttingDown.compareAndSet(false, true) ) {
            L.debug("shutdown() called after shutdown. Ignored");
            return;
        }
        L.info("shutdown() of UiCore");
        L.debug("shutdown() running shutdown listeners");
        ON_SHUTDOWN.forEach(Runnable::run);
        L.debug("shutdown() shutdownNow the executor service");
        EXECUTOR_SERVICE.shutdownNow();
        if ( isFx() && !isGluon() ) {
            L.debug("shutdown() closing all open fx stages.");
            FxCore.ACTIVE_STAGES.values().forEach(w -> Optional.ofNullable(w.get()).ifPresent(s -> s.hide()));
            FxCore.getWindows().stream().filter(w -> w != mainStage).forEach(javafx.stage.Window::hide); // close/hide all free stages.
        } else if ( isSwing() ) {
            L.debug("shutdown() closing all open swing dialogs and frames.");
            for (Window window : java.awt.Frame.getWindows()) {
                window.setVisible(false);
                window.dispose();
            }
            L.debug("shutdown() forcing the Platform.exit()");
            Platform.exit();
        }
    }

    /**
     * Intake from Ui.
     * <p>
     * @param b
     */
    static void handle(Throwable b) {
        BACKGROUND_ACTIVITY.set(false); // Cleanup
        for (Class<?> clazz : EXCEPTION_CONSUMER.keySet()) {
            if ( ExceptionUtil.containsInStacktrace(clazz, b) ) {
                EXCEPTION_CONSUMER.get(clazz).accept(ExceptionUtil.extractFromStraktrace(clazz, b));
                return;
            }
        }
        finalConsumer.accept(b);
    }

    /**
     * This to be done after a contiue or start, but for every ui toolkit.
     */
    private static void postStartUp() {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            L.warn("Exception occured on {}", t, e);
            Ui.handle(e);
        });
        Dl.local().add(UserPreferences.class, new UserPreferencesJdk()); // Hard added here.
    }

}
