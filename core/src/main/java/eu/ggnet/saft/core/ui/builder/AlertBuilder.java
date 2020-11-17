package eu.ggnet.saft.core.ui.builder;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import javafx.scene.control.Alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.*;
import eu.ggnet.saft.core.impl.Fx;
import eu.ggnet.saft.core.impl.Swing;
import eu.ggnet.saft.core.ui.*;

import static eu.ggnet.saft.core.ui.AlertType.INFO;

/**
 * Fluent Alert Dialog , replacement for JOptionPane.
 * See {@link Alert} as starting point.
 *
 * @author oliver.guenther
 */
// TODO: Don't know if it was used ever @EqualsAndHashCode
public class AlertBuilder {

    private final static Logger L = LoggerFactory.getLogger(AlertBuilder.class);

    /**
     * The title.
     */
    private String title = "Information";

    /**
     * The body or message.
     */
    private String message;

    private final UiParent parent;

    private final Saft saft;

    public AlertBuilder(PreBuilder pre) {
        parent = pre.uiParent().orElse(null);
        saft = pre.saft();
        pre.title().ifPresent(v -> title = v);
    }

    public AlertBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Set the message of the alert.
     *
     * @param message the message
     * @return the alert for fluent usage.
     */
    public AlertBuilder message(String message) {
        this.message = message;
        return this;
    }

    /**
     * Appends to the message prepended by a new line.
     *
     * @param message the message to append
     * @return the alert for fluent usage.
     */
    public AlertBuilder nl(String message) {
        this.message += "\n" + message;
        return this;
    }

    /**
     * Appends a new line to the message.
     *
     * @return the alert for fluent usage.
     */
    public AlertBuilder nl() {
        this.message += "\n";
        return this;
    }

    /**
     * Shows the final alert of type info.
     */
    public void show() {
        show(INFO);
    }

    /**
     * Shows the final alert.
     * For now the implementation is done with a JOptionPane.
     *
     * @param type the type of the alert.
     */
    public void show(AlertType type) {
        try {
            if ( UiCore.isGluon() ) {
                UiCore.global().gluonSupport().get().showAlert(title, message, type);
            } else if ( UiCore.isFx() ) {
                UiUtil.dispatchFx(() -> {
                    Alert alert = new Alert(type.getJavaFxType());
                    saft.core(Fx.class).parentIfPresent(parent, p -> alert.initOwner(p));
                    alert.setTitle(title);
                    alert.setContentText(message);
                    alert.showAndWait();
                    return null;
                });
            } else {
                SwingSaft.dispatch(() -> {
                    JOptionPane.showMessageDialog(saft.core(Swing.class).unwrap(parent).orElse(saft.core(Swing.class).unwrapMain().orElse(null)), message, title, type.getOptionPaneType());
                    return null;
                });
            }
        } catch (ExecutionException | InterruptedException | InvocationTargetException ex) {
            Ui.handle(ex);
        }
    }

    @Override
    public String toString() {
        return "AlertBuilder{" + "title=" + title + ", message=" + message + ", parent=" + parent + '}';
    }

}
