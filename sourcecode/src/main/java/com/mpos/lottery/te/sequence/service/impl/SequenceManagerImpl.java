package com.mpos.lottery.te.sequence.service.impl;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.sequence.dao.SequenceDao;
import com.mpos.lottery.te.sequence.dao.jpa.SequenceDaoImpl;
import com.mpos.lottery.te.sequence.domain.NoSequenceException;
import com.mpos.lottery.te.sequence.domain.Sequence;
import com.mpos.lottery.te.sequence.service.SequenceManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigInteger;
import java.sql.Connection;

import javax.sql.DataSource;

/**
 * Due to the 'requires_new' transaction propagation will result in pausing previous transaction(not sure), TE try to
 * avoid use 'requires_new'. But 'fetchNewSequence' must be run in a isolation transaction(why??), here TE will manage
 * transaction itself by retrieve a database connection from data source.
 */
public class SequenceManagerImpl implements SequenceManager {
    private Log logger = LogFactory.getLog(SequenceManagerImpl.class);
    private DataSource dataSource;

    public Sequence fetchNewSequence(String name) {
        Connection conn = null;
        try {
            // manage transaction
            conn = this.getDataSource().getConnection();
            conn.setAutoCommit(false);

            // retrieve new sequence
            SequenceDao sequenceDao = new SequenceDaoImpl(conn);
            Sequence seq = sequenceDao.getByName(name);
            if (seq == null) {
                throw new NoSequenceException(name, null);
            } else {
                /**
                 * Assemble a new Sequence instance. Here we must instantiate a 'new' Sequence, otherwise the 'managed'
                 * Sequence instance will be synced with underlying database when the transaction is committed...This is
                 * only applicable to hibernate
                 */
                Sequence update = new Sequence();
                update.setInterval(seq.getInterval());
                update.setMaxValue(seq.getMaxValue());
                update.setMinValue(seq.getMinValue());
                update.setName(seq.getName());
                update.setNextMax(seq.getNextMax());
                update.setNextMin(seq.getNextMin());
                update.setId(seq.getId());
                update.setCycle(seq.isCycle());
                // update sequence...always calculate from nextMax
                BigInteger nextMin = update.getNextMax().add(BigInteger.ONE);
                if (nextMin.compareTo(update.getMaxValue()) > 0) {
                    if (!seq.isCycle()) {
                        throw new SystemException("Sequence[" + seq.getName()
                                + "] isn't cyclic, and has reache the end!!");
                    }
                    nextMin = update.getMinValue();
                }
                BigInteger nextMax = nextMin.add(update.getInterval()).subtract(BigInteger.ONE);
                if (nextMax.compareTo(update.getMaxValue()) > 0) {
                    nextMax = update.getMaxValue();
                }
                update.setNextMin(nextMin);
                update.setNextMax(nextMax);
                if (logger.isTraceEnabled()) {
                    logger.trace("Retrieve new sequence(name=" + name + "):" + seq.toString());
                    logger.trace("Update sequence(name=" + name + "):" + update.toString());
                }
                sequenceDao.update(update);
            }
            conn.commit();
            conn.setAutoCommit(true);

            return seq;
        } catch (Exception e) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.rollback();
                }
            } catch (Exception e1) {
                throw new SystemException(e1);
            }
            if (e instanceof SystemException) {
                throw (SystemException) e;
            }
            throw new SystemException(e);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
