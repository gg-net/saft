/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ggnet.saft.core.subsystem;

/**
 * Interface to be implemented by other core suppliers as ServiceLoader.
 *
 * @author oliver.guenther
 */
public interface CoreFactory {

    /**
     * Returns an new active core of the supplied type or null, never fails.
     *
     * @param <T>
     * @param <V>
     * @param typeClass the typeclass.
     * @return an new core of the supplied type or null.
     */
    <T extends Core<V>, V> T create(Class<T> typeClass);

    /**
     * Returns a dead core of the supplied type or null.
     *
     * @param <T>
     * @param <V>
     * @param typeClass
     * @return a dead core of the supplied type or null.
     */
    <T extends Core<V>, V> T dead(Class<T> typeClass);

}
