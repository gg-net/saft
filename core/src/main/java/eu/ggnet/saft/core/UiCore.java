package eu.ggnet.saft.core;

import java.awt.Component;
import java.awt.Window;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.swing.JFrame;

import javafx.scene.Parent;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.impl.Swing;
import eu.ggnet.saft.core.ui.LocationStorage;

/**
 * The Core of the Saft UI, containing methods for startup or registering things.
 *
 * @author oliver.guenther
 */
public class UiCore {

    private final static Logger L = LoggerFactory.getLogger(UiCore.class);

    private static Saft saft;

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
    public static synchronized Saft global() {
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
     * @deprecated will be remove later, should not be needed, but is used in dwoss on multiple old places. Use saft.core(Swing.class).unwrapMain().get();
     */
    @Deprecated
    public static Window getMainFrame() {
        return global().core(Swing.class).unwrapMain().get();
    }

    /**
     * @deprecated {@link Saft#executorService() }.
     */
    @Deprecated
    public static Executor getExecutor() {
        return saft.executorService();
    }

    /**
     * Shortcut for UiCore.global().init(new Swing(UiCore.global(), mainView));
     *
     * @param mainView the mainView to continue on.
     */
    public static void continueSwing(JFrame mainView) {
        global().init(new Swing(global(), mainView));
    }

    /**
     * Old Startup of Swing, must be run in the EventQueue thread.
     *
     * @param <T>     type parameter
     * @param builder the builder for swing.
     * @deprecated use continueSwing(UiUtil.startup(builder)); or even better UiCore.global().init(new Swing(UiCore.global(), UiUtil.startup(builder)));
     */
    @Deprecated
    public static <T extends Component> void startSwing(final Supplier<T> builder) {
        continueSwing(UiUtil.startup(builder));
    }

    /**
     * Shortcut for UiCore.global().init(new Fx(UiCore.global(), primaryStage));
     *
     * @param primaryStage the primaryStage for the application, not yet visible.
     */
    public static void continueJavaFx(final Stage primaryStage) {
        global().init(new Fx(global(), primaryStage));
        // TODO: Docu, that the shutdown must be done in the Application.stop()
//        primaryStage.setOnCloseRequest((e) -> {
//            global().shutdown();
//        });
    }

    /**
     * Old Startup of Java FX, must be run in the Platform thread.
     *
     * @param <T>          type restriction.
     * @param primaryStage the primaryStage for the application, not yet visible.
     * @param builder      the build for the main ui.
     * @deprecated Use continueJavaFx(UiUtil.startup(primaryStage, builder)); or better UiCore.global().init(new Fx(UiCore.global(),
     * UiUtil.startup(primaryStage, builder)));
     */
    @Deprecated
    public static <T extends Parent> void startJavaFx(final Stage primaryStage, final Supplier<T> builder) {
        continueJavaFx(UiUtil.startup(primaryStage, builder));
    }

}
