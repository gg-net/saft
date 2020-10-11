package eu.ggnet.saft.core;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.subsystem.Fx;
import eu.ggnet.saft.core.subsystem.Swing;
import eu.ggnet.saft.core.ui.*;
import eu.ggnet.saft.core.ui.builder.BuilderUtil;

/**
 * The Core of the Saft UI, containing methods for startup or registering things.
 *
 * @author oliver.guenther
 */
public class UiCore {

    private final static Logger L = LoggerFactory.getLogger(UiCore.class);

    private static final Set<Runnable> ON_SHUTDOWN = new HashSet<>();

    private static JFrame mainFrame = null; // Frame in Swing Mode

    private static Stage mainStage = null; // Frame in Fx Mode

    private static boolean gluon = false; // Special FX Gloun mode. See https://gluonhq.com/products/mobile/

    private static AtomicBoolean shuttingDown = new AtomicBoolean(false); // Shut down handler.

    private static Saft saft;

    /**
     * Running means, that one startXXX oder contiuneXXX method was called.
     *
     * @return true, if running.
     */
    public static boolean isRunning() {
        return mainFrame != null || mainStage != null;
    }

    /**
     * Initialise the global with the supplied saft.
     * Transition method. If Saft is created by someone else, e.g. CDI, but there exists only one, it can be used to initialise the UiCore.
     *
     * @param newSaft the saft.
     */
    public static void initGlobal(Saft newSaft) {
        if ( saft != null ) throw new IllegalStateException("UiCore.global() already inited, call to initGlobal(Saft) not allowed.");
        L.info("Initialising Saft in classic mode using explizit instance: {}.", newSaft);
        saft = Objects.requireNonNull(newSaft, "Saft must not be null");
    }

    /**
     * Returns the one instance, if running in the classic way.
     *
     * @return the global saft.
     */
    // TODO: Reconsider, if an autoinit is something wanted. There may be cases, like ui elements displayed before the Saft core is up. A fail first might help solve these.
    public static Saft global() {
        if ( saft == null ) {
            L.info("Initialising Saft in classic mode using defaults.");
            // init defaults.
            saft = new Saft(new LocationStorage(), Executors.newCachedThreadPool(new ThreadFactory() {

                private final ThreadGroup group = new ThreadGroup("saft-uicore-pool");

                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(group, r, "Thread-" + counter.incrementAndGet() + "-" + r.toString());
                }
            }));
        }
        return saft;
    }

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
     * @deprecated {@link Saft#executorService() }.
     */
    @Deprecated
    public static Executor getExecutor() {
        return saft.executorService();
    }

    /**
     * interim Mode, Saft connects to a running environment.
     *
     * @param mainView the mainView to continue on.
     */
    public static void continueSwing(JFrame mainView) {
        if ( isRunning() ) throw new IllegalStateException("UiCore is already initialised and running");
        SwingSaft.ensurePlatformIsRunning();
        Platform.setImplicitExit(false); // Need this, as we asume many javafx elements opening and closing.
        mainFrame = mainView;
        mainView.addWindowListener(new WindowAdapter() {

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
        SwingSaft.ensurePlatformIsRunning();
        try {
            continueSwing(SwingSaft.dispatch(() -> {
                T p = builder.call();
                JFrame frame = new JFrame();
                Optional<StringProperty> optionalTitleProperty = BuilderUtil.findTitleProperty(p);
                if ( optionalTitleProperty.isPresent() ) {
                    optionalTitleProperty.get().addListener((ob, o, n) -> {
                        frame.setTitle(n);
                        frame.setName(n);
                    });
                } else if ( p.getClass().getAnnotation(Title.class) != null ) {
                    frame.setTitle(p.getClass().getAnnotation(Title.class).value());
                    frame.setName(p.getClass().getAnnotation(Title.class).value());
                }
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().add(p);
                frame.pack();
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
                return frame;
            }));
        } catch (InterruptedException | InvocationTargetException | ExecutionException ex) {
            saft.handle(ex);
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
        UiUtil.dispatchFx(() -> {
            Parent p = builder.call();
            Optional<StringProperty> optionalTitleProperty = BuilderUtil.findTitleProperty(p);
            if ( optionalTitleProperty.isPresent() ) {
                primaryStage.titleProperty().bind(optionalTitleProperty.get());
            } else if ( p.getClass().getAnnotation(Title.class) != null ) {
                primaryStage.setTitle(p.getClass().getAnnotation(Title.class).value());
            }
            primaryStage.setScene(new Scene(p));
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
     * Contiues the Ui in JavaFx variant.
     * <p>
     * This also assumes two things:
     * <ul>
     * <li>The JavaFX Platform is already running (as a Stage already exists), most likely created through default lifecycle of javaFx</li>
     * <li>This Stage will always be open or the final to be closed, so implicitExit is ok</li>
     * </ul>
     *
     * @param primaryStage the primaryStage for the application, not yet visible.
     */
    public static void contiuneJavaFx(final Stage primaryStage) {
        if ( isRunning() ) throw new IllegalStateException("UiCore is already initialised and running");
        mainStage = primaryStage;
        primaryStage.setOnCloseRequest((e) -> {
            UiCore.shutdown();
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
    // Todo: das kann eingentlich nach gloun. Wenn jemand gluon benutzt, dann hat er beim start auch die dependencie. Das muss ich gar nicht reflexif machen.
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

        if ( !UiCore.global().gluonSupport().isPresent() )
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
        saft.executorService().shutdownNow();
        // TODO: this can probably be done in saft directly.
        if ( isFx() && !isGluon() ) {
            L.debug("shutdown() closing all open fx stages.");
            global().core(Fx.class).shutdown();
        } else if ( isSwing() ) {
            global().core(Swing.class).shutdown();
        }
    }

    /**
     * This to be done after a contiue or start, but for every ui toolkit.
     */
    private static void postStartUp() {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            L.warn("Exception occured on {}", t, e);
            Ui.handle(e);
        });
    }

}
