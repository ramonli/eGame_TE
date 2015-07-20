package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumberSequence;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberSequenceDao;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import javax.persistence.Query;

public class JpaLuckyNumberSequenceDao extends BaseJpaDao implements LuckyNumberSequenceDao {

    @Override
    public LuckyNumberSequence lookup(final String gameId) {

        long waitLockTime = MLotteryContext.getInstance().getWaitLockTime();

        String sql = "select * from LK_PRIZE_STATUS l where l.GAME_ID=? for update";
        if (waitLockTime > 0) {
            sql += " wait " + waitLockTime;
        } else {
            sql += " nowait";
        }
        Query query = this.getEntityManager().createNativeQuery(sql, LuckyNumberSequence.class);
        query.setParameter(1, gameId);
        List<LuckyNumberSequence> seqLogs = query.getResultList();
        if (seqLogs.size() != 1) {
            throw new DataIntegrityViolationException("There should be only one instance of "
                    + LuckyNumberSequence.class + "of game(id=" + gameId + "), but total " + seqLogs.size()
                    + " instances found.");
        }
        return seqLogs.get(0);
    }
}
