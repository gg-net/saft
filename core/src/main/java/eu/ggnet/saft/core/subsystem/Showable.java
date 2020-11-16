/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author oliver.guenther
 */
public interface Showable {

    public CompletableFuture<Object> show();

}
