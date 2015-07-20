package com.mpos.lottery.te.common.dao;

import java.util.List;

import javax.persistence.LockModeType;

public interface DAO {

    void insert(Object entity);

    @SuppressWarnings("rawtypes")
    void insert(List entities);

    void update(Object entity);

    @SuppressWarnings("rawtypes")
    void update(List entities);

    <T> T findById(Class<T> clazz, Object id);

    <T> T findById(Class<T> clazz, Object id, LockModeType lockType);

    /**
     * Find entity by identity, if no entity found:
     * <ol>
     * <li>if allowed null, null will be returned.</li>
     * <li>if null is unallowed, a <code>DataIntegrityViolationExceptio</code> will be thrown out.
     * </ol>
     * 
     * @param clazz
     *            The class type of entity.
     * @param id
     *            The identify of entity.
     * @param allowNull
     *            Whether null can be returned.
     * @return a entity with given identity.
     */
    <T> T findById(Class<T> clazz, Object id, boolean allowNull);

    <T> List<T> all(Class<T> entityClass);

    /**
     * Lookup entity of a given type. If no entity found, <code>DataIntegrityViolationException</code> will be thrown
     * out. If multple entities found, the 1st one will be returned.
     * 
     * @param entityClass
     *            The type of entity.
     * @return 1st entity.
     */
    <T> T single(Class<T> entityClass);
}
