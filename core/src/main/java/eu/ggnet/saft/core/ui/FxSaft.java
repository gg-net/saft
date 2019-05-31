package eu.ggnet.saft.core.ui;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal API class for fx specific task.
 * Methods are public, cause I didn't clean it up yet.
 *
 * @author oliver.guenther
 */
public class FxSaft {

    private final static Logger L = LoggerFactory.getLogger(FxSaft.class);

    /**
     * Constructs (loads) an FXML and controller pair, finding all elements base on the class and calling load, so direct calls to getRoot() or getController()
     * are possible.
     * Resources are discovered as described in {@link FxSaft#loadView(java.lang.Class) }.
     *
     * @param <T>             type parameter
     * @param <R>             type parameter
     * @param controllerClazz the controller class.
     * @return a loaded loader.
     * @throws IllegalArgumentException see {@link FxSaft#loadView(java.lang.Class) }
     * @throws IllegalStateException    see {@link FxSaft#loadView(java.lang.Class) }
     * @throws NullPointerException     see {@link FxSaft#loadView(java.lang.Class) }
     * @throws RuntimeException         wrapped IOException of {@link FXMLLoader#load() }.
     */
    public static <T, R extends FxController> FXMLLoader constructFxml(Class<R> controllerClazz) throws IllegalArgumentException, NullPointerException, IllegalStateException, RuntimeException {
        if ( !Platform.isFxApplicationThread() )
            throw new IllegalStateException("Method constructFxml is not called from the JavaFx Ui Thread, illegal (e.g. construct of WebView fails on other threads)");
        FXMLLoader loader = new FXMLLoader(loadView(controllerClazz));
        try {
            loader.load();
            return loader;
        } catch (IOException ex) {
            L.error("Exeption while loading fxml", ex);
            throw new RuntimeException(ex);
        }
    }

    // Internal Api
    /**
     * Returns a url of the FXML file based on the controllerClazz.
     * Nameconvention:
     * <p>
     * A Controller class must end with Controller or Presenter and implement {@link FxController}.
     * The FXML file must be in the same package and may end with View.fxml or only the name either in bump writing or everything lowercase.
     * <p>
     * Example: LoginHelper
     * <ul>
     * <li>Controller: LoginHelperController.java or LoginHelperPresenter.java</li>
     * <li>FXMKL file: LoginHelperView.fxml or LoginHelper.fxml or loginhelper.fxml</li>
     * </ul>
     *
     * @param <R>             the type of the contorller class
     * @param controllerClazz the controller class
     * @return a url of the FXML file, ready to be used in the FXMLLoader.
     * @throws IllegalArgumentException if the controller class does conform to the nameing convetion, it must end with Controller or Presenter
     * @throws NullPointerException     if no resource can be found, hence there is no file in the same package trewe with the ending View.fxml
     */
    public static <R extends FxController> URL loadView(Class<R> controllerClazz) throws IllegalArgumentException, NullPointerException {
        String head = null;
        if ( controllerClazz.getSimpleName().endsWith("Controller") ) {
            head = controllerClazz.getSimpleName().substring(0, controllerClazz.getSimpleName().length() - "Controller".length());
        } else if ( controllerClazz.getSimpleName().endsWith("Presenter") ) {
            head = controllerClazz.getSimpleName().substring(0, controllerClazz.getSimpleName().length() - "Presenter".length());
        }
        if ( head == null ) throw new IllegalArgumentException(controllerClazz + " does not end with Controller or Presenter");

        List<String> names = Arrays.asList(head + "View.fxml", head + ".fxml", head.toLowerCase() + ".fxml");

        return names.stream()
                .map(n -> controllerClazz.getResource(n))
                .filter(Objects::nonNull)
                .findFirst().orElseThrow(() -> new NullPointerException("No fxml found with any of the names " + names));
    }

    // TODO: Move to UiUtil.
    /**
     * Dispatches the Callable to the Platform Ui Thread. If this method is called on the javafx ui thread, the supplied callable is called,
     * otherwise the exection on Platform.runLater ist synchrnized via a latch.
     *
     * @param <T>      Return type of callable
     * @param callable the callable to dispatch
     * @return the result of the callable
     * @throws RuntimeException wraps InterruptedException of {@link CountDownLatch#await() } and ExecutionException of {@link FutureTask#get() }
     */
    public static <T> T dispatch(Callable<T> callable) throws RuntimeException {
        try {
            FutureTask<T> futureTask = new FutureTask<>(callable);
            final CountDownLatch cdl = new CountDownLatch(1);
            if ( Platform.isFxApplicationThread() ) {
                futureTask.run();
                cdl.countDown();
            } else {
                Platform.runLater(() -> {
                    futureTask.run();
                    cdl.countDown();
                });
            }
            cdl.await();
            return futureTask.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Internal api
    /**
     * Run on the application thread, but looking into if we are on it already.
     *
     * @param r a runnable.
     */
    public static void run(Runnable r) {
        if ( Platform.isFxApplicationThread() ) r.run();
        else Platform.runLater(r);
    }

}
