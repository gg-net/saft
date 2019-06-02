/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.internal;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.ui.AlertType;
import eu.ggnet.saft.core.ui.FxSaft;
import eu.ggnet.saft.core.ui.builder.GluonSupport;
import eu.ggnet.saft.core.ui.builder.UiParameter;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;

/**
 * Implementation fo the Support Service.
 *
 * @author oliver.guenther
 */
public class GluonSupportService implements GluonSupport {

    private final static Logger L = LoggerFactory.getLogger(GluonSupportService.class);

    private static class DialogOrView {

        private final Optional<Dialog<?>> dialog;

        private final Optional<View> view;

        public DialogOrView(Dialog dialog) {
            this.dialog = Optional.of(dialog);
            this.view = Optional.empty();
        }

        public DialogOrView(View view) {
            this.dialog = Optional.empty();
            this.view = Optional.of(view);
        }

        public DialogOrView ifDialog(Consumer<Dialog<?>> c) {
            dialog.ifPresent(c);
            return this;
        }

        public DialogOrView ifView(Consumer<View> c) {
            view.ifPresent(c);
            return this;
        }

    }

    @Override
    public void showAlert(String title, String message, AlertType type) {
        FxSaft.dispatch(() -> {
            Dialog<Void> d = new Dialog<>(title, message);
            switch (type) {
                case ERROR:
                    d.setGraphic(MaterialDesignIcon.ERROR.graphic());
                    break;
                case WARNING:
                    d.setGraphic(MaterialDesignIcon.WARNING.graphic());
                    break;
                case INFO: // Nothing
                default:
            }
            Button close = new Button("Schließen");
            close.setOnAction(e -> d.hide());
            d.getButtons().add(close);
            return d.showAndWait();
        });

    }

    @Override
    public UiParameter constructJavaFx(UiParameter in) {
        if ( !Platform.isFxApplicationThread() ) throw new IllegalStateException("construnctJavaFx called, but not from the ui thread, disallowed");
        Pane pane = in.pane().get();
        Dialog<Void> d = new Dialog<>();
        d.setTitleText(in.toTitle());
        d.setContent(pane);
        d.showAndWait();
        return in;
    }

    @Override
    public void closeViewOrDialogOf(Node n) {
        L.debug("closeViewOrDialogOf({}) entering", n);
        deepSearchForCloseOf(n, "init")
                .ifDialog(Dialog::hide)
                .ifView(v -> {
                    Optional<View> home = MobileApplication.getInstance().goHome();
                    if ( home.isPresent() ) {
                        L.debug("closeViewOrDialogOf({}), found {}, goHome() successful", n, v);
                        return;
                    }
                    Optional<View> previous = MobileApplication.getInstance().switchToPreviousView();
                    if ( previous.isPresent() ) {
                        L.debug("closeViewOrDialogOf({}), found {}, goHome() failed, switchPrevious successful", n, v);
                        return;
                    }
                    L.error("closeViewOrDialogOf({}), found {}, goHome() and switchPrevious() failed. Make sure a HOMEVIEW is registered.", n, v);
                    throw new IllegalStateException("goHome() and switchPrevious() failed.");
                });
    }

    /**
     * Searchs through the parent tree of node to find either a dialog or a view.
     *
     * @param n     the node to start with
     * @param stack
     * @return
     * @throws NullPointerException
     */
    private DialogOrView deepSearchForCloseOf(Node n, String stack) throws NullPointerException {
        Objects.requireNonNull(n, "Neither Dialog nor View discovert. Ur doing something wrong. FullStack: " + stack);
        stack = stack + ".[" + n.getClass().getName() + "|" + n.getId() + "]";
        if ( isDialogNode(n) ) {
            L.debug("deepSearchForCloseOf() found Dialog with stack {}", stack);
            return new DialogOrView(extractDialog(n));
        }
        if ( n instanceof View ) {
            L.debug("deepSearchForCloseOf() found View with stack {}", stack);
            return new DialogOrView((View)n);
        }
        return deepSearchForCloseOf(n.getParent(), stack);
    }

    /**
     * Identifies a node as Dialog node via reflections.
     * Criterias:
     * <ul>
     * <li>com.gluonhq.charm.glisten.control.a</li>
     * <li>has private field c of type Dialog</li>
     * </ul>
     *
     * @param n the node to test
     * @return true if dialog node.
     * @throws RuntimeException if the class com.gluonhq.charm.glisten.control.a is can not be found
     */
    private boolean isDialogNode(Node n) throws RuntimeException {
        try {
            if ( Class.forName("com.gluonhq.charm.glisten.control.a").isInstance(n) ) {
                L.debug("isDialogNode({}) is true", n);
                return true;
            }
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    /**
     * Extract the dialog from a dialog node.
     * See isDialogNode for criterias.
     *
     * @param n the node to extract the dialog
     * @return the dialog
     * @throws IllegalArgumentException if the node is not a dialog node.
     */
    private Dialog<?> extractDialog(Node n) throws IllegalArgumentException {
        try {
            Class<?> clazzA = Class.forName("com.gluonhq.charm.glisten.control.a");
            Field fieldC = clazzA.getDeclaredField("c");
            fieldC.setAccessible(true);
            Dialog<?> dialog = (Dialog<?>)fieldC.get(n);
            L.debug("extractDialog({}) found {}", n, dialog);
            return dialog;
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

}
