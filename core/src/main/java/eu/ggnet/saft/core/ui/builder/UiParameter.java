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

import javafx.scene.layout.Pane;
import javafx.stage.Modality;

import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.ggnet.saft.api.IdSupplier;
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

    abstract boolean once();

    abstract boolean frame();

    abstract Optional<String> title();

    abstract Optional<String> id();

    abstract Optional<Modality> modality();

    abstract Type type();

    abstract UiParent uiParent();

    abstract Optional<Object> preResult();

    abstract Optional<Class<?>> rootClass();

    public abstract Optional<Pane> pane();

    abstract Optional<JComponent> jPanel();

    abstract Optional<Window> window();

    abstract Optional<FxController> fxController();

    abstract Optional<javafx.scene.control.Dialog> dialog();

    abstract Builder toBuilder();

    static class Builder extends UiParameter_Builder {

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public Builder() {
            once(false);
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
                .nullableId(preBuilder.id)
                .nullableTitle(preBuilder.title)
                .nullableModality(preBuilder.modality)
                .frame(preBuilder.frame)
                .once(preBuilder.once).uiParent(preBuilder.uiParent);
    }

    /**
     * Returns a title, either set or via class annotation.
     *
     * @return a title, either set or via class annotation.
     * @throws IllegalStateException if rootClass is not set.
     */
    public final String toTitle() throws IllegalStateException {
        if ( !rootClass().isPresent() ) throw new IllegalStateException(("RootClass not set, toTitle() not allowed"));
        return title().orElse(TitleUtil.title(rootClass().get(), id().orElse(null)));
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
        if ( !id().isPresent() && preResult instanceof IdSupplier ) builder.nullableId(((IdSupplier)preResult).id());
        return builder.build();
    }

    public final UiParameter withRootClass(Class<?> clazz) {
        return toBuilder().rootClass(clazz).build();
    }

    /**
     * Returns the once value, either set or inspected in the root class.
     * The annotation has a higher priority over the set value.
     *
     * @return the once value, either set or inspected in the root class.
     */
    public final boolean extractOnce() {
        return rootClass().map(c -> c.getAnnotation(Once.class)).map(annotation -> {
            L.debug("OnceAnnotation is set on {} with {}", rootClass().get(), annotation.value());
            return annotation.value();
        }).orElse(once());
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
     * Returns a key string based on the root class and the id.
     *
     * @return a key string based on the root class and the id
     */
    public final String toKey() {
        return rootClass().get().getName() + id().map(i -> ":" + i).orElse("");
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

}
