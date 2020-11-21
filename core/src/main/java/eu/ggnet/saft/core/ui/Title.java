package eu.ggnet.saft.core.ui;

import java.lang.annotation.*;

import eu.ggnet.saft.core.ui.Bind.Type;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate a Panel, Pane or Controller to set an individual title. If {id} is used in the value, it will be replaced by id value of open.
 *
 * @see Type#TITLE for a more dynamic approach.
 * @author oliver.guenther
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Title {

    /**
     * Returns the Title
     *
     * @return the title
     */
    String value();

}
