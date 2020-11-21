package eu.ggnet.saft.core.ui;

import eu.ggnet.saft.core.ui.Bind.Type;

/**
 * Implement in Panel, Pane or FxController to get informed on closing.
 *
 * @author oliver.guenther
 * @deprecated Use the {@link Type#SHOWING}
 */
@Deprecated
public interface ClosedListener {

    @Deprecated
    void closed();

}
