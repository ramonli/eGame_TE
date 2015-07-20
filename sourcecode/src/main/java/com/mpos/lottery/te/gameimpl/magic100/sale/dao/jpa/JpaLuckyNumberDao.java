package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberDao;

import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

@SuppressWarnings({ "unchecked", "deprecation" })
public class JpaLuckyNumberDao extends BaseJpaDao implements LuckyNumberDao {

    @Override
    public List<LuckyNumber> findBySeuqnce(final long sequenceNum, final String gameInstanceId, final int countOfNumbers) {
        List<LuckyNumber> validNumbers = doFindSequence(sequenceNum, gameInstanceId, countOfNumbers);

        if (validNumbers.size() < countOfNumbers) {
            if (logger.isDebugEnabled()) {
                logger.debug("Reach the tail of main cycle and will start from begining to get enough lucky numbers:"
                        + (countOfNumbers - validNumbers.size()));
            }
            long startOfSeq = this.getEntityManager().getReference(Magic100GameInstance.class, gameInstanceId)
                    .getStartOfSequence();
            // lookup from the beginning of cycle to get enough lucky numbers
            List<LuckyNumber> moreValidItems = this.doFindSequence(startOfSeq, gameInstanceId, countOfNumbers
                    - validNumbers.size());
            validNumbers.addAll(moreValidItems);
        }

        if (validNumbers.size() != countOfNumbers) {
            throw new DataIntegrityViolationException("Total " + countOfNumbers
                    + " lucky numbers expected, however only " + validNumbers.size() + " found by (sequence="
                    + sequenceNum + ",gameInstanceID=" + gameInstanceId + ").");
        }
        return validNumbers;
    }

    protected List<LuckyNumber> doFindSequence(final long sequenceNum, final String gameInstanceId,
            final int countOfNumbers) {
        Query query = this.getEntityManager().createQuery(
                "from LuckyNumber l where l.sequenceOfNumber>=:sequence "
                        + "and l.gameInstance=:gameInstance order by l.sequenceOfNumber");
        query.setParameter("sequence", sequenceNum);
        query.setParameter("gameInstance",
                this.getEntityManager().getReference(Magic100GameInstance.class, gameInstanceId));
        query.setMaxResults(countOfNumbers);
        return (List<LuckyNumber>) query.getResultList();
    }

    @Override
    public List<LuckyNumber> findBySeuqnces(String gameInstanceId, Long... sequenceNum) {
        String sql = "from LuckyNumber l where l.gameInstance.id=:gameInstanceId and "
                + "l.sequenceOfNumber in :sequenceList";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("sequenceList", Arrays.asList(sequenceNum));
        return this.findByNamedParams(sql, params);
    }

    /**
     * @param gameInstanceId
     * @return int
     */
    @Override
    public int getMaxNumberSeq(String gameInstanceId) {
        String q = "select NVL(max(l.number_seq),0) from lk_prize_parameters l where "
                + "l.lk_game_instace_id=:gameInstanceId ";
        BigDecimal maxNumberSeq = (BigDecimal) this.getEntityManager().createNativeQuery(q)
                .setParameter("gameInstanceId", gameInstanceId).getSingleResult();
        return maxNumberSeq != null ? maxNumberSeq.intValue() : 0;
    }

    /**
     * find game instance by id. detailed explanation.
     * 
     * @param gameInstanceId
     * @return List
     */
    @Override
    public List<LuckyNumber> findByGameInstanceId(String gameInstanceId) {
        String sql = "from LuckyNumber l where l.gameInstance.id=:gameInstanceId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        return this.findByNamedParams(sql, params);
    }

}
