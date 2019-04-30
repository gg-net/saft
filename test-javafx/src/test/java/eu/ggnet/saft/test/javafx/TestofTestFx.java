/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.test.javafx;


import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.assertions.api.Assertions.*;

/**
 *
 * @author oliver.guenther
 */

@ExtendWith(ApplicationExtension.class)
public class TestofTestFx  {

    @Start
    public void start(Stage stage) {
        
        Parent sceneRoot = new ClickApplication.ClickPane();
        Scene scene = new Scene(sceneRoot, 100, 100);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void should_contain_button(FxRobot robot) {
        // expect:
        assertThat(robot.lookup(".button").queryButton()).hasText("click me!");
    }

    @Test
    public void should_click_on_button(FxRobot robot) {
        // when:
        robot.clickOn(".button");

        // then:
        assertThat(robot.lookup(".button").queryButton()).hasText("clicked!");
    }
}
