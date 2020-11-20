/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Optional;

import javafx.util.Callback;

import eu.ggnet.saft.core.impl.AbstractCore;
import eu.ggnet.saft.core.impl.Core.In;
import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.builder.UiParameter.Type;

import static java.util.Optional.empty;

/**
 *
 * @author oliver.guenther
 */
public class CoreTest {

    private static class TestCore extends AbstractCore {

        @Override
        public Type selectType(In<?, ?> in) {
            return super.selectType(in); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected Optional<Callback<Class<?>, Object>> initializer() {
            return Optional.empty();
        }

    }

    private static class TestController implements FxController {

    }

    private static interface Stein<T> {

        public Optional<T> doSomething(T t);

    }

    private static class Tod implements Stein<Object> {

        public Optional<Object> doSomething(Object t) {
            return empty();
        }

    }

    private static class Kohle implements Stein<Long> {

        @Override
        public Optional<Long> doSomething(Long t) {
            return Optional.ofNullable(t);
        }

    }

    private static <T, X extends Stein<T>> Stein<T> doit(Class<X> x) {
        return (Stein<T>)new CoreTest.Tod();
    }

    public static void main(String[] args) {

        Stein<Long> it = doit(Kohle.class);
        Optional<Long> x = it.doSomething(11l);

        System.out.println("x=" + x);

        TestCore tc = new TestCore();
        Type selectType = tc.selectType(new In<>(TestController.class));
        System.out.println("Type = " + selectType);
    }
}
