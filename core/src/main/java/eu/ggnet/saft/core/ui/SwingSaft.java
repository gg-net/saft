package eu.ggnet.saft.core.ui;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.*;

import javax.swing.SwingUtilities;

import javafx.embed.swing.JFXPanel;

import org.slf4j.LoggerFactory;

/**
 *
 * @author oliver.guenther
 */
public class SwingSaft {

    /**
     * Executes the supplied callable on the EventQueue.
     * If this method is called from the EventQueue, the same thread is used, otherwise its dispaced to the EventQueue.
     *
     * @param <T>      the type parameter
     * @param callable the callable to be dispached
     * @return the result of the callable
     * @throws ExecutionException        see {@link Future#get() }
     * @throws InterruptedException      See {@link Future#get() }
     * @throws InvocationTargetException See {@link Future#get() }
     */
    //HINT: Internal
    public static <T> T dispatch(Callable<T> callable) throws ExecutionException, InterruptedException, InvocationTargetException {
        FutureTask<T> task = new FutureTask(callable);
        if ( EventQueue.isDispatchThread() ) task.run();
        else EventQueue.invokeLater(task);
        return task.get();
    }

    //HINT: Internal
    public static void run(Runnable runnable) {
        if ( EventQueue.isDispatchThread() ) runnable.run();
        else EventQueue.invokeLater(runnable);
    }

    /**
     * Special form of {@link SwingUtilities#getWindowAncestor(java.awt.Component) }, as it also verifies if the supplied parameter is of type Window and if
     * true returns it.
     *
     * @param c the component
     * @return a window.
     */
    //HINT: Internal & RedTapeController
    public static Optional<Window> windowAncestor(Component c) {
        LoggerFactory.getLogger(SwingSaft.class).debug("windowAncestor({})", c);
        if ( c == null ) return Optional.empty();
        if ( c instanceof Window ) return Optional.of((Window)c);
        return Optional.ofNullable(SwingUtilities.getWindowAncestor(c));
    }

    private static boolean started = false;

    /**
     * Holds a mapping of all Scenes in JFXPanels. Used to discover parent windows if in a wrapped JFXPanel.
     *
     * @deprecated This is not needed. in Fx mode, the platform is running. In Swingmode, the core does that.
     */
    //HINT: Internal
    @Deprecated
    public static void ensurePlatformIsRunning() {
        if ( !started ) {
            new JFXPanel(); // Implicit start of Fx Platform.
            started = true;
        }
    }

}
