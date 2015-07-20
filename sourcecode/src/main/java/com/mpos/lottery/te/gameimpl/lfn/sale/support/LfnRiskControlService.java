package com.mpos.lottery.te.gameimpl.lfn.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.lfn.game.LfnGameInstance;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnEntry;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.AbstractRiskControlService;
import com.mpos.lottery.te.gamespec.sale.support.ChanceOdds;
import com.mpos.lottery.te.gamespec.sale.support.ChanceOfEntry;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class LfnRiskControlService extends AbstractRiskControlService {
    private Log logger = LogFactory.getLog(LfnRiskControlService.class);
    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;

    @Override
    protected List<ChanceOfEntry> determineSelectedNumbers(Context respCtx, BaseTicket ticket,
            BaseGameInstance gameInstance, BaseEntry entry) {
        if (entry.getBetOption() < LfnEntry.BETOPTION_INTERVAL) {
            return Arrays.asList(new ChanceOfEntry(entry.getSelectNumber(), entry.getEntryAmount(), entry
                    .getBetOption()));
        } else {
            int betOption = entry.getBetOption() - LfnEntry.BETOPTION_INTERVAL;
            return assembleSingleSelectedNumbers(entry, betOption, betOption);
        }
    }

    @Override
    protected String determineBettingNumberOfPrizeLevel(Context respCtx, BaseEntry entry, ChanceOdds chanceOdd,
            ChanceOfEntry chance) throws ApplicationException {
        chanceOdd.setBettingNumber(chance.getSelectedNumber());
        return chanceOdd.getBettingNumber();
    }

    /**
     * For LFN game, a give bet option can win only a single prize level.
     */
    @Override
    protected BigDecimal determineFinalLossAmountOfPrizeLevel(BigDecimal finalLimit, List<ChanceOdds> chanceOdds,
            BaseGameInstance gameInstance) throws ApplicationException {
        // String sql = "SELECT COUNT(*) FROM BD_PRIZE_LEVEL P, BD_PRIZE_LEVEL_ITEM PP WHERE "
        // + "P.BD_PRIZE_LOGIC_ID =:prizeLogicId AND P.ID = PP.BD_PRIZE_LEVEL_ID";
        // Query query = this.getEntityManager().createNativeQuery(sql);
        // query.setParameter("prizeLogicId", ((LfnGameInstance) gameInstance).getPrizeLogicId());
        // Object row = query.getSingleResult();
        // BigDecimal result = SimpleToolkit.mathDivide(finalLimit, (BigDecimal) row);
        //
        // if (logger.isDebugEnabled())
        // logger.debug("There total " + ((BigDecimal) row)
        // + " possible prize levels found, determine the final loss amount of prize level as "
        // + result + "=" + finalLimit + "/" + ((BigDecimal) row));
        // return result;
        return finalLimit;
    }

    @Override
    protected List<ChanceOdds> determineOddsOfEntry(Context respCtx, BaseTicket ticket, BaseGameInstance gameInstance,
            BaseEntry entry) throws ApplicationException {
        String sql = "SELECT P.PRIZE_LEVEL, P.PRIZE_NAME, PP.PRIZE_AMOUNT FROM BD_PRIZE_LEVEL P, "
                + "BD_PRIZE_LEVEL_ITEM PP WHERE P.BD_PRIZE_LOGIC_ID =:prizeLogicId AND P.ID = PP.BD_PRIZE_LEVEL_ID "
                + "AND P.PRIZE_LEVEL=:betOption";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter("prizeLogicId", ((LfnGameInstance) gameInstance).getPrizeLogicId());
        int betOption = entry.getBetOption();
        if (betOption > LfnEntry.BETOPTION_INTERVAL) {
            betOption = betOption - LfnEntry.BETOPTION_INTERVAL;
        }
        query.setParameter("betOption", betOption);

        List<ChanceOdds> chanceOdds = new LinkedList<ChanceOdds>();
        Object row = query.getSingleResult();
        if (row == null) {
            logger.warn("No prize level definition found for given entry(" + entry + "), ignore it.");
        } else {
            ChanceOdds odds = new ChanceOdds();
            Object[] columns = (Object[]) row;
            odds.setOdds((BigDecimal) columns[2]);
            // must set to N bet option
            int nBetOption = entry.getBetOption() < LfnEntry.BETOPTION_INTERVAL ? entry.getBetOption() : entry
                    .getBetOption() - LfnEntry.BETOPTION_INTERVAL;
            odds.setPrizelLevelType(nBetOption + "");
            chanceOdds.add(odds);
            if (logger.isDebugEnabled()) {
                logger.debug("THe odds for given entry(" + entry + ") is " + odds);
            }
        }
        return chanceOdds;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
