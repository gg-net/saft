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
package testfx.support;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import eu.ggnet.saft.core.Ui;

/**
 *
 * @author oliver.guenther
 */
public class MainPane extends FlowPane {

    public final static String SHOW_JAVAFX_PANE_ID = "show-javafxpane";

    public final static String SHOW_SWING_JPANEL_ID = "show-swingJpanel";

    public final static String SHOW_JAVAFX_DIALOG_ID = "show-javafxdialog";

    public final static String SHOW_JAVAFX_FXML_ID = "show-javafxfxml";

    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public MainPane() {
        setPadding(new Insets(10));
        
        Button showPane = new Button("JavaFx Pane");
        showPane.setId(SHOW_JAVAFX_PANE_ID);
        showPane.setOnAction(e -> Ui.build(this).fx().show(() -> new JavaFxPane()));
        
        Button showJPanel = new Button("Swing JPanel");        
        showJPanel.setOnAction(e -> Ui.build(this).swing().show(() -> new SwingJPanel()));
        showJPanel.setId(SHOW_SWING_JPANEL_ID);
        
        Button showDialog = new Button("JavaFx Dialog");        
        showDialog.setOnAction(e -> Ui.build(this).dialog().eval(() -> DialogMaker.makeDialog()));
        showDialog.setId(SHOW_JAVAFX_DIALOG_ID);

        Button showFxml = new Button("JavaFx Fxml");
        showFxml.setOnAction(e -> Ui.build(this).fxml().show(BasicApplicationController.class));
        showFxml.setId(SHOW_JAVAFX_FXML_ID);
        
        
        getChildren().addAll(showJPanel, showPane, showDialog, showFxml);
    }

}
