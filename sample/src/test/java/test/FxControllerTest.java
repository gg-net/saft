/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import org.junit.jupiter.api.Test;

import eu.ggnet.saft.core.ui.FxController;
import eu.ggnet.saft.sample.support.BasicApplicationController;
import eu.ggnet.saft.sample.testing.ButtonController;
import eu.ggnet.saft.sample.tutorial.chapter15.FxmlController;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author oliver.guenther
 */
public class FxControllerTest {

    @Test
    public void controllerValid() {
        assertNull(FxController.validationMessage(BasicApplicationController.class));
        assertNull(FxController.validationMessage(ButtonController.class));
        assertNull(FxController.validationMessage(FxmlController.class));
    }
}
