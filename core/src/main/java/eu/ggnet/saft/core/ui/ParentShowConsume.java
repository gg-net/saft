/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.ui;

/**
 *
 *
 * @author oliver.guenther
 */
public interface ParentShowConsume<E> {

    /**
     * Shows the implementation with the parent and the consumable e
     *
     * @param parent the optional parent, may be null.
     * @param e      the consumable
     */
    void show(UiParent parent, E e);

}
