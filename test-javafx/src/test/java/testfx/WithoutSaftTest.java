/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.assertions.api.Assertions.assertThat;

/**
 * Test the simple workings of TestFx without using Saft. 
 * If these tests fail, something with the environment is broken.
 * 
 * @author oliver.guenther
 */
@ExtendWith(ApplicationExtension.class)
public class WithoutSaftTest  {

       // scene object for unit tests
    public static class ClickPane extends StackPane {

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public ClickPane() {
            super();
            Button button = new Button("click me!");
            button.setOnAction(actionEvent -> button.setText("clicked!"));
            getChildren().add(button);
        }
    }
    
    @Start
    public void start(Stage stage) {
        
        Parent sceneRoot = new ClickPane();
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
