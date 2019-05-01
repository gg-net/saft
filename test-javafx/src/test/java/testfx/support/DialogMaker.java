/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfx.support;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;

/**
 *
 * @author oliver.guenther
 */
public class DialogMaker {

    public final static String DIALOG_PANE_ID = "javafx-dialog";

    public final static String CONTENT_TEXT = "I have a great message for you!";
    
    public static Dialog makeDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Look, an Information Dialog");
        alert.setContentText(CONTENT_TEXT);
        alert.getDialogPane().setId(DIALOG_PANE_ID);

        
        return alert;
    }

}
