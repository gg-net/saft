/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.impl;

import javax.swing.JPanel;

/**
 *
 * @author oliver.guenther
 */
public class CoreTest {

    public static void main(String[] args) {
        System.out.println(Swing.selectType(new Core.In<>(JPanel.class)));
        //  Swing.selectType(new Core.In<>(() -> new BorderPane()));
    }
}
