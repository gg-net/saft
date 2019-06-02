/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.gluon.sample;

import com.gluonhq.charm.glisten.control.TextArea;
import eu.ggnet.saft.core.Ui;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author oliver.guenther
 */
public class InfoView extends BorderPane {

    public InfoView() {
        Label title = new Label("Borderpane");
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipisici elit,"
                + " sed eiusmod tempor incidunt ut labore et dolore magna aliqua. "
                + "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris "
                + "nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit "
                + "in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
                + "Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui "
                + "officia deserunt mollit anim id est laborum. ";

        TextArea ta = new TextArea(lorem);
        ta.setDisable(true);

        Button close = new Button("Close");
        close.setOnAction(e -> Ui.closeWindowOf(title));

        setTop(title);
        setCenter(ta);
        setBottom(close);

    }

}
