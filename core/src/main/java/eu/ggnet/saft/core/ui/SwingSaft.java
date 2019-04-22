package eu.ggnet.saft.core.ui;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;
import java.util.function.Consumer;

import javax.swing.JPanel;

import org.slf4j.LoggerFactory;

/**
 *
 * @author oliver.guenther
 */
public class SwingSaft {

    public static <T, R extends JPanel> R construct(Class<R> panelClazz, T parameter) throws Exception {
        return dispatch(() -> {
            R panel = panelClazz.getConstructor().newInstance();
            if ( parameter != null && panel instanceof Consumer ) {
                try {
                    ((Consumer<T>)panel).accept(parameter);
                } catch (ClassCastException e) {
                    LoggerFactory.getLogger(SwingSaft.class).warn(panel.getClass() + " implements Consumer, but not of type " + parameter.getClass());
                }
            }
            return panel;
        });
    }

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
    public static <T> T dispatch(Callable<T> callable) throws ExecutionException, InterruptedException, InvocationTargetException {
        FutureTask<T> task = new FutureTask(callable);
        if ( EventQueue.isDispatchThread() ) task.run();
        else EventQueue.invokeLater(task);
        return task.get();
    }

    public static void run(Runnable runnable) {
        if ( EventQueue.isDispatchThread() ) runnable.run();
        else EventQueue.invokeLater(runnable);
    }

}
