/*
 * Copyright (C) 2018 GG-Net GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.ggnet.saft.core.ui.builder;

import java.awt.Dialog;
import java.awt.Window;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JComponent;

import javafx.beans.property.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.ui.*;

/**
 * Internal Parameter class.
 *
 * @author oliver.guenther
 */
@FreeBuilder
public abstract class UiParameter {

    private final static Logger L = LoggerFactory.getLogger(UiParameter.class);

    /**
     * Type of the build process.
     */
    public static enum Type {
        FX {

            @Override
            public Object selectRelevantInstance(UiParameter p) {
                return p.pane().get();
            }

        },
        FXML {

            @Override
            public Object selectRelevantInstance(UiParameter p) {
                return p.fxController().get();
            }
        },
        DIALOG {

            @Override
            public Object selectRelevantInstance(UiParameter p) {
                return p.dialog().get();
            }
        }, SWING {

            @Override
            public Object selectRelevantInstance(UiParameter p) {
                return p.jPanel().get();
            }
        };

        public abstract Object selectRelevantInstance(UiParameter p);

    }

    public abstract Saft saft();

    public abstract boolean frame();

    public abstract Optional<String> title();

    public abstract Optional<StringProperty> titleProperty();

    /**
     * Optional property for the showing status.
     *
     * @return an optional property.
     */
    public abstract Optional<BooleanProperty> showingProperty();

    public abstract Optional<Modality> modality();

    public abstract Type type();

    public abstract Optional<UiParent> uiParent();

    public abstract Optional<Object> preResult();

    public abstract Optional<Class<?>> rootClass();

    public abstract Optional<Pane> pane();

    public abstract Optional<JComponent> jPanel();

    public abstract Optional<Window> window();

    public abstract Optional<Stage> stage();

    public abstract Optional<FxController> fxController();

    public abstract Optional<javafx.scene.control.Dialog> dialog();

    public abstract Builder toBuilder();

    public static class Builder extends UiParameter_Builder {

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public Builder() {
            frame(false);
        }

    }

    @SuppressWarnings("NonPublicExported")
    public static UiParameter.Builder builder() {
        return new UiParameter.Builder();
    }

    @SuppressWarnings("NonPublicExported")
    public static UiParameter.Builder fromPreBuilder(PreBuilder preBuilder) {
        return new UiParameter.Builder()
                .saft(preBuilder.saft())
                .title(preBuilder.title())
                .modality(preBuilder.modality())
                .frame(preBuilder.frame())
                .uiParent(preBuilder.uiParent());
    }

    /**
     * Returns the reference class for icons and else.
     *
     * @return the reference class
     */
    public final Class<?> extractReferenceClass() {
        return type().selectRelevantInstance(this).getClass();
    }

    /**
     * Adds the pre result, and if type of id supplier, adds the id.
     *
     * @param preResult the pre result
     * @return a new UiParameter
     */
    public final UiParameter withPreResult(Object preResult) {
        if ( preResult == null ) return this;
        Builder builder = toBuilder().preResult(preResult);
        return builder.build();
    }

    public final UiParameter withRootClass(Class<?> clazz) {
        return toBuilder().rootClass(clazz).build();
    }

    /**
     * Returns the frame value, either set or inspect annotation on the root class.
     * The annotation has a higher priority over the set value.
     *
     * @return the frame value
     */
    public final boolean extractFrame() {
        return rootClass().map(c -> c.getAnnotation(Frame.class)).map(a -> true).orElse(frame());
    }

    /**
     * Returns the modality for swingOrMain.
     *
     * @return the modality for swingOrMain
     */
    public final Dialog.ModalityType asSwingModality() {
        if ( !modality().isPresent() ) return Dialog.ModalityType.MODELESS;
        switch (modality().get()) {
            case APPLICATION_MODAL:
                return Dialog.ModalityType.APPLICATION_MODAL;
            case WINDOW_MODAL:
                return Dialog.ModalityType.DOCUMENT_MODAL;
            case NONE:
                return Dialog.ModalityType.MODELESS;
        }
        return Dialog.ModalityType.MODELESS;
    }

    /**
     * Returns a key string based on the root class.
     *
     * @return a key string based on the root class.
     */
    public final String toKey() {
        return rootClass().get().getName();
    }

    /**
     * Returns the relevat implementation based on the type of a closedlistnener.
     *
     * @return the relevat implementation based on the type of a closedlistnener
     */
    public final Optional<ClosedListener> getClosedListenerImplemetation() {
        if ( type().selectRelevantInstance(this) instanceof ClosedListener ) {
            return Optional.of((ClosedListener)type().selectRelevantInstance(this));
        }
        return Optional.empty();
    }

    public final UiParameter optionalConsumePreResult() {
        if ( !preResult().isPresent() ) return this;
        if ( !(type().selectRelevantInstance(this) instanceof Consumer) ) return this;
        ((Consumer)type().selectRelevantInstance(this)).accept(preResult().get());
        return this;
    }

    /**
     * Returns true if the StoreLocation annotation was set.
     *
     * @return true if the StoreLocation annotation was set.
     */
    public boolean isStoreLocation() {
        return (extractReferenceClass().getAnnotation(StoreLocation.class) != null);
    }

    /**
     * Returns a titleProperty.
     * It is bound or set by the following rules.
     * <ol>
     * <li>If the supplied Controller, Pane, JPanel annotates a StringProperty with {@link eu.ggnet.saft.core.ui.Bind} and
     * {@link eu.ggnet.saft.core.ui.Bind.Type#TITLE}</li>
     * <li>If the supplied Controller, Pane, JPanel has the {@link Title} annotation set, a new property with the supplied value set</li>
     * <li>If the title method was called on the builder, a new property with the supplied value set</li>
     * <li>The simple class name of the supplied Controller, Pane, JPanel</li>
     * </ol>
     *
     * @return a titleProperty.
     */
    public StringProperty toTitleProperty() {
        if ( !rootClass().isPresent() ) throw new IllegalStateException(("RootClass not set yet, toTitleProperty() not allowed"));
        if ( titleProperty().isPresent() ) return titleProperty().get();
        if ( rootClass().get().getAnnotation(Title.class) != null ) return new SimpleStringProperty(rootClass().get().getAnnotation(Title.class).value());
        if ( title().isPresent() ) return new SimpleStringProperty(title().get());
        return new SimpleStringProperty(rootClass().get().getSimpleName());
    }
}
