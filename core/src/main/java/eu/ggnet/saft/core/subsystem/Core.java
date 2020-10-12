/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

import java.util.Optional;
import java.util.function.Consumer;

import eu.ggnet.saft.core.ui.UiParent;

/**
 *
 * T type of the ui window class (swing = JFrame, javafx = Stage).
 * All methods have an optional characteristic, meaning, they never fail,
 *
 * @author oliver.guenther
 */
public interface Core<T> {

    /**
     * Suppling unwrapped parent or main to the consumer.
     * if parent not null and the relevant window exists (has been created vie saft) -> this to the consumer
     * else if main parent is set -> this to the consumer
     * else don't call the consumer
     *
     * @param parent
     * @param consumer
     */
    void parentIfPresent(UiParent parent, Consumer<T> consumer);

    void parentIfPresent(Optional<UiParent> parent, Consumer<T> consumer);

    void parentIfPresent(Consumer<T> consumer);

    Optional<T> unwrap(UiParent parent);

    Optional<T> unwrap(Optional<UiParent> parent);

    Optional<T> unwrapMain();

    /**
     * Closes the container/window of the supplied anchor.
     *
     * @param parent the parent, must not be null.
     */
    void closeOf(UiParent parent);

    /**
     * Relocate all active windows on the visible screen.
     * Usefull to rescue offscreen windows.
     */
    void relocate();

    /**
     * Weak register of windows.
     * Remove is not needed, as it will be weak referenced.
     *
     * @param window a window, which the core should know about and close at the end.
     */
    void add(T window);

    /**
     * Shutdown the core, closing open windows.
     */
    void shutdown();

    /**
     * Returns true if the core of this type is active.
     *
     * @return
     */
    boolean isActiv();

}
