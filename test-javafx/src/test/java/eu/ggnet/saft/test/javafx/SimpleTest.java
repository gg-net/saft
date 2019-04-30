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
package eu.ggnet.saft.test.javafx;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import eu.ggnet.saft.core.UiCore;

import static org.testfx.assertions.api.Assertions.assertThat;

/**
 *
 * @author oliver.guenther
 */
@ExtendWith(ApplicationExtension.class)
public class SimpleTest  {

    static Logger L = LoggerFactory.getLogger(SimpleTest.class);

    @Start
    public void start(Stage stage) throws Exception {
        UiCore.startJavaFx(stage, () -> new MainPane());
    }

    @Test
    // @Ignore // TODO: UI Tests seam to fail on different Screne sizes or OSs.
    public void test(FxRobot r) throws InterruptedException {
        Thread.sleep(250);
        Button showPaneButton = r.lookup("#showPane").queryButton();
        assertThat(showPaneButton).isNotNull();        
        r.clickOn(showPaneButton);
        
        Thread.sleep(250);

        // Finding the label in the opened window. If it exists, it implies, that the dialog is visible.
        Labeled label = r.lookup("#label").queryLabeled();
        assertThat(label).isNotNull().hasText("A Text");

    }

    @AfterEach
    public void tearDown(FxRobot robot) throws Exception {
        FxToolkit.hideStage();
        robot.release(new KeyCode[]{});
        robot.release(new MouseButton[]{});
    }
}
