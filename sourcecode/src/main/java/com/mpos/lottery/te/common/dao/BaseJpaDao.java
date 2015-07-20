package com.mpos.lottery.te.common.dao;

import com.jolbox.bonecp.ConnectionHandle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

/**
 * Multiple entities which inherit from same superclass, each entity will maps to different table.
 * <p/>
 * The <code>JpaDaoSupport</code> of Spring has been deprecated since Springv4.0, the recommended way of coding JPA is
 * depending on <code>EntityManager</code> directly.
 */
public class BaseJpaDao implements DAO {
    /**
     * The injected entity manager is a shared, thread-safe proxy for the actual transactional EntityManager.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Sometimes some DAO implementation needs to access underlying database connection directly, the helper class
     * <code>DataSourceUtils.getConnction(ds:DataSource)</code> can help.
     */
    private DataSource dataSource;
    protected Log logger = LogFactory.getLog(BaseJpaDao.class);

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void insert(Object entity) {
        this.getEntityManager().persist(entity);
    }

    @Override
    public void insert(List entities) {
        for (Object entity : entities) {
            this.insert(entity);
        }
    }

    @Override
    public void update(Object entity) {
        this.getEntityManager().merge(entity);
    }

    @Override
    public void update(List entities) {
        for (Object entity : entities) {
            this.update(entity);
        }
    }

    @Override
    public <T> T findById(Class<T> clazz, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("Argument 'id' can NOT be null(id=" + id + ",clazz=" + clazz + ").");
        }
        return this.findById(clazz, id, true);
    }

    /**
     * Find entity by id. null will be returned if no found.
     */
    @Override
    public <T> T findById(Class<T> clazz, Object id, LockModeType lockType) {
        if (id == null) {
            throw new IllegalArgumentException("Argument 'id' can NOT be null(id=" + id + ",clazz=" + clazz + ").");
        }
        return this.getEntityManager().find(clazz, id, lockType);
    }

    @Override
    public <T> T findById(Class<T> clazz, Object id, boolean allowNull) {
        T entity = this.getEntityManager().find(clazz, id);
        if (entity == null) {
            if (!allowNull) {
                throw new DataIntegrityViolationException("No entity(" + clazz + ") found by id(" + id + ").");
            }
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> all(Class<T> entityClass) {
        Query query = this.getEntityManager().createQuery("from " + entityClass.getCanonicalName());
        return query.getResultList();
    }

    @Override
    public <T> T single(Class<T> entityClass) {
        List<T> entities = this.all(entityClass);
        if (entities.size() == 0) {
            throw new DataIntegrityViolationException("No entity found(" + entityClass + ").");
        }
        return entities.get(0);
    }

    /**
     * Return single entity from multiple entities. The JPA interface always return a list of entities, however in some
     * cases, we do want only one entity returned, this method will return the 1st entity in the list.
     * 
     * @param entities
     *            The list of entities.
     * @param allowMultiple
     *            Whether there are multiple entities allowed when a querying criteria given. If true, this method will
     *            return the 1sts entity, if false, a <code>DataIntegrityViolationException</code> will be thrown out.
     * @return the 1st entity or null if no entities at all.
     */
    protected <T> T single(List<T> entities, boolean allowMultiple) {
        T result = null;
        if (entities.size() > 1) {
            String msg = "Should be at most only 1 entity(" + entities.get(0).getClass().getCanonicalName()
                    + ") found, however total " + entities.size() + " entities found.";
            if (allowMultiple) {
                result = entities.get(0);
                logger.warn(msg + ".. the 1st entity(" + result + ") will be returned.");
            } else {
                throw new DataIntegrityViolationException(msg);
            }
        } else if (entities.size() == 1) {
            result = entities.get(0);
        }
        return result;
    }

    // ------------------------------------------------------------
    // HELPER METHODS
    // ------------------------------------------------------------

    protected String getEntityName(Class<? extends VersionEntity> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("argument 'clazz' can't be null.");
        }
        return clazz.getCanonicalName();
    }

    /**
     * Only single result should be found by the querying criteria.
     * 
     * @param sql
     *            The querying SQL.
     * @param params
     *            The parameter of querying.
     * @return a single result, or null if no result found.
     * @throws DataIntegrityViolationException
     *             if found multiple results.
     */
    protected Object findSingleByNamedParams(String sql, Map<String, Object> params) {
        // List result = this.findByNamedParams(sql, params);
        // return single(result, false);
        Query query = this.assembleQueryParameter(sql, params);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    /**
     * Only single result should be returned if multiple or a single result found.
     * 
     * @param sql
     *            The querying sql.
     * @param params
     *            The parameter of querying.
     * @return a single result, or null if no result found.
     * @throws DataIntegrityViolationException
     *             if found multiple results and no allowing multiple entities.
     */
    protected Object findSingleFromListByNamedParams(String sql, Map<String, Object> params) {
        List result = this.findByNamedParams(sql, params, 1);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * Simulate the convenient method <code>findByNamedParams</code> of Spring <code>LJpaDaoSupport</code> which has
     * been deprecated since Spring v4.x.
     * 
     * @param sql
     *            The JPQL.
     * @param params
     *            THe parameters map of JPQL.
     * @param maxResult
     *            The max count of returned result. If wanna a single result, simply set it to 1(the official JPA API,
     *            Query.getSingleResult() will throw <code>NonUniqueResultException</code> if multiple result found.
     */
    protected List findByNamedParams(String sql, Map<String, Object> params, int maxResult) {
        Query query = this.assembleQueryParameter(sql, params);
        query.setMaxResults(maxResult);
        return query.getResultList();
    }

    protected List findByNamedParams(String sql, Map<String, Object> params) {
        return this.findByNamedParams(sql, params, Integer.MAX_VALUE);
    }

    protected Query assembleQueryParameter(String sql, Map<String, Object> params) {
        Query query = this.getEntityManager().createQuery(sql);
        Iterator<String> keyIt = params.keySet().iterator();
        while (keyIt.hasNext()) {
            String paramName = keyIt.next();
            query.setParameter(paramName, params.get(paramName));
        }
        return query;
    }

    /**
     * Query a result list by given raw SQL.
     */
    protected final List queryList(String nativeSql, JdbcQueryCallback callback) {
        /**
         * NOTE: Strange!! in my understanding no matter get connection by DataSourceUtils or Hibernate's
         * JdbcConnectionAccess, the same connection should be returned. However they are different, to guarantee all
         * operations are performed under same transaction, we must use DataSourceUtils to get connection if you want
         * database connection directly.
         */
        // SessionImpl o = (SessionImpl) this.getEntityManager().getDelegate();
        // ConnectionHandle conn = (ConnectionHandle)
        // o.getJdbcConnectionAccess().obtainConnection();
        // logger.debug("Retrieve database connection: " + conn);
        // logger.debug("Retrieve database connection: " +
        // conn.getInternalConnection());

        Connection conn = DataSourceUtils.getConnection(this.getDataSource());
        logger.debug("Retrieve database connection: " + ((ConnectionHandle) conn).getInternalConnection());
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Get a connection:" + conn + " from current transaction context.");
            }
            PreparedStatement ps = conn.prepareStatement(nativeSql);
            callback.setParameter(ps);
            ResultSet rs = ps.executeQuery();
            List result = new ArrayList();
            while (rs.next()) {
                Object o = callback.objectFromRow(rs);
                if (o != null) {
                    result.add(o);
                }
            }

            // Due to we get connection from current entity manager, the
            // transaction will be managed by entity manager.
            // conn.commit();
            rs.close();
            ps.close();

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(conn, this.getDataSource());
        }
    }

    /**
     * Batch operation of native SQL.
     */
    protected void batch(BatchCallback batchCallback) {
        // we must get the connection from current entity manager, otherwise
        // all data manipulation will be executed in a new transaction
        Connection conn = DataSourceUtils.getConnection(this.getDataSource());
        logger.debug("Retrieve database connection: " + ((ConnectionHandle) conn).getInternalConnection());
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Get a connection:" + conn + " from current transaction context.");
            }
            PreparedStatement ps = conn.prepareStatement(batchCallback.getQuery());
            batchCallback.assembleParameters(ps);
            ps.executeBatch();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(conn, this.getDataSource());
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static abstract class JdbcQueryCallback {

        /**
         * Set parameters to prepared statement.
         */
        public abstract void setParameter(PreparedStatement ps) throws SQLException;

        /**
         * Assemble a object from current result set row.
         */
        public abstract Object objectFromRow(ResultSet rs) throws SQLException;
    }
}
