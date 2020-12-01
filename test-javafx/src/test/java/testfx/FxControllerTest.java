/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx;

import org.junit.jupiter.api.Test;

import eu.ggnet.saft.core.ui.FxController;

import testfx.support.BasicApplicationController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author oliver.guenther
 */
public class FxControllerTest {

    @Test
    public void controllerVaild() {
        assertThat(FxController.validationMessage(BasicApplicationController.class)).isNull();
    }

}
