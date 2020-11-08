/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

import java.util.concurrent.CompletableFuture;

import eu.ggnet.saft.core.ui.builder.UiParameter;

/**
 *
 * @author oliver.guenther
 */
public interface CoreUiFuture {

    CompletableFuture<UiParameter> proceed();

    void show();

}
