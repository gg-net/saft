/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.ui.exception;

import java.util.Objects;
import java.util.function.BiFunction;

import org.slf4j.LoggerFactory;

import eu.ggnet.saft.core.Saft;
import eu.ggnet.saft.core.UiUtil.ExceptionRunnable;
import eu.ggnet.saft.core.ui.UiParent;

/**
 *
 * @author oliver.guenther
 */
public class AndFinallyHandler<Z> implements BiFunction<Z, Throwable, Z> {

    private final Saft saft;

    private final UiParent parent;

    //TODO: Parent may be null, main will is used from saft
    public AndFinallyHandler(Saft saft, UiParent parent) {
        this.saft = Objects.requireNonNull(saft, "Saft must not be null");
        this.parent = parent;
    }

    @Override
    public Z apply(Z in, Throwable exception) {
        if ( exception != null ) {
            saft.handle(parent, exception);
            return null; // If an exception ocourd, we drop the result value.
        } else {
            return in;
        }
    }

    public BiFunction<Z, Throwable, Z> andFinally(ExceptionRunnable runnable) {
        return (Z in, Throwable exception) -> {
            if ( exception != null ) {
                try {
                    runnable.run();
                } catch (Exception internalException) {
                    LoggerFactory.getLogger(AndFinallyHandler.class).warn("{}.andFinally(): internal exception {} with message {}",
                            AndFinallyHandler.class.getSimpleName(), internalException.getClass().getName(), internalException.getMessage());
                } finally {
                    saft.handle(parent, exception);
                }
                return null; // If an exception ocourd, we drop the result value.
            } else {
                return in;
            }
        };

    }

    public BiFunction<Z, Throwable, Z> andFinally(Runnable runnable) {
        return andFinally((ExceptionRunnable)() -> runnable.run());
    }

}
