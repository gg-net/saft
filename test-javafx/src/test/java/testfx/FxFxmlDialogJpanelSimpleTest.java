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
package testfx;


import java.util.Optional;

import javafx.scene.control.*;
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

import testfx.support.*;

import static org.testfx.assertions.api.Assertions.assertThat;

/**
 * Testing the opening of each type once.
 * 
 * @author oliver.guenther
 */
@ExtendWith(ApplicationExtension.class)
public class FxFxmlDialogJpanelSimpleTest  {    

    private static Logger L = LoggerFactory.getLogger(FxFxmlDialogJpanelSimpleTest.class);

    @Start
    public void start(Stage stage) throws Exception {
        UiCore.startJavaFx(stage, () -> new MainPane());
    }

    @Test    
    public void openingAndCloseOfAllTypes(FxRobot r) {       
        /*
        +---------------------------------+
        | Open and Close of a JavaFx Pane.|
        | Created via source code.        |
        +---------------------------------+
        */        
        Button show = r.lookup("#" + MainPane.SHOW_JAVAFX_PANE_ID).queryButton();
        r.clickOn(show);

        // Finding the label in the opened window. If it exists, it implies, that the dialog is visible.
        Labeled label = r.lookup("#" + JavaFxPane.LABEL_ID).queryLabeled();
        assertThat(label).isNotNull().hasText(JavaFxPane.LABEL_TEXT);

        // Closeing the Window
        Button close = r.lookup("#" + JavaFxPane.CLOSE_BUTTON_ID).queryButton();
        r.clickOn(close);

        /*
        +---------------------------------+
        | Open and Close of a JavaFx Pane.|
        | Created via fxml.               |
        +---------------------------------+
        */        
        show = r.lookup("#" + MainPane.SHOW_JAVAFX_FXML_ID).queryButton();
        r.clickOn(show);

        // Finding the label in the opened window. If it exists, it implies, that the dialog is visible.
        label = r.lookup("#" + BasicApplicationController.LABEL_ID).queryLabeled();
        assertThat(label).isNotNull().hasText(BasicApplicationController.LABEL_TEXT);

        // Closeing the Window
        close = r.lookup("#" + BasicApplicationController.CLOSE_ID).queryButton();
        r.clickOn(close);

        /*
        +-----------------------------------+
        | Open and Close of a JavaFx Dialog.|
        +-----------------------------------+
        */        
        show = r.lookup("#" + MainPane.SHOW_JAVAFX_DIALOG_ID).queryButton();
        r.clickOn(show);

        // Finding the dialogpane in the opened window and the Text in it. If it exists, it implies, that the dialog is visible.
        DialogPane dialogPane = r.lookup("#" + DialogMaker.DIALOG_PANE_ID).queryAs(DialogPane.class);
        
        assertThat(dialogPane.getContentText()).isNotNull().isNotEmpty().isEqualTo(DialogMaker.CONTENT_TEXT);

        // Closeing the Window. The Buttonfinder looks a little bit complecated, but works for now. Please optimize.
        Optional<Button> shoudBeClose = r.lookup(".button").queryAllAs(Button.class).stream().filter(b -> b.getText().equals("OK")).findFirst();
        assertThat(shoudBeClose.isPresent()).as("Ok Button finder").isTrue();        
        r.clickOn(shoudBeClose.get());

        
        /*
        +---------------------------------+
        | Open and find of a Swing JPanel.|
        +---------------------------------+
        */        
 
        assertThat(SwingJPanel.active()).isFalse();
        show = r.lookup("#" + MainPane.SHOW_SWING_JPANEL_ID).queryButton();
        r.clickOn(show);

        // Workarround test, that the swing window is visible.
        assertThat(SwingJPanel.active()).isTrue();
        
    }

    @AfterEach
    public void tearDown(FxRobot robot) throws Exception {
        FxToolkit.hideStage();
        robot.release(new KeyCode[]{});
        robot.release(new MouseButton[]{});
    }
}
