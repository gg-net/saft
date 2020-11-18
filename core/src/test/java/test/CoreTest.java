/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import eu.ggnet.saft.core.impl.AbstractCore;
import eu.ggnet.saft.core.impl.Core.In;
import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.core.ui.builder.UiParameter.Type;

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

    }

    private static class TestController implements FxController {

    }

    public static void main(String[] args) {

        TestCore tc = new TestCore();
        Type selectType = tc.selectType(new In<>(TestController.class));
        System.out.println("Type = " + selectType);
    }
}
