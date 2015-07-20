package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGPayoutTempDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGPayoutTemp;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gamespec.game.Game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IGPayoutTempDaoImpl extends BaseJpaDao implements IGPayoutTempDao {

    private Log logger = LogFactory.getLog(IGPayoutTempDaoImpl.class);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IGPayoutTemp getPayoutTempByCondition(long batchNumber, String operatorId, String serialNumber) {
        // TODO Auto-generated method stub
        Map params = new HashMap();
        params.put("packetSerialNo", serialNumber);
        params.put("operatorId", operatorId);
        params.put("iGBatchNumber", batchNumber);
        String q = "from IGPayoutTemp t where t.ticketSerialNo=:packetSerialNo and t.operatorId=:operatorId and t.iGBatchNumber=:iGBatchNumber";
        return (IGPayoutTemp) this.findSingleByNamedParams(q, params);

    }

    @Override
    public boolean isUsedByAnotherOperatorId(String operatorId, String serialNumber) {
        boolean returnValue = false;
        Map params = new HashMap();
        params.put("packetSerialNo", serialNumber);
        params.put("operatorId", operatorId);
        // params.put("iGBatchNumber", batchNumber);
        String q = "from IGPayoutTemp t where t.ticketSerialNo=:packetSerialNo and t.operatorId!=:operatorId ";
        IGPayoutTemp payoutTemp = (IGPayoutTemp) this.findSingleByNamedParams(q, params);
        if (payoutTemp != null) {
            returnValue = true;
        }
        return returnValue;
    }

    @Override
    public BigDecimal getActualAmount(long batchNumber, String operatorId) {
        // TODO Auto-generated method stub
        /*
         * Map params = new HashMap();
         * 
         * params.put("operatorId", operatorId); params.put("iGBatchNumber", batchNumber);
         */
        // params.put("iGBatchNumber", batchNumber);
        String q = "select sum(tt.total_amount) from ig_payout_temp tt where tt.operator_id=:operatorId and tt.ig_batch_number=:iGBatchNumber ";
        BigDecimal actualAmount = (BigDecimal) this.getEntityManager().createNativeQuery(q)
                .setParameter("operatorId", operatorId).setParameter("iGBatchNumber", batchNumber).getSingleResult();
        return actualAmount != null ? actualAmount : new BigDecimal(0);

    }

    @Override
    public BigDecimal getTotoalAmountBeforeTax(long batchNumber, String operatorId) {
        // TODO Auto-generated method stub
        String q = "select sum(tt.total_amount_b4_tax) from ig_payout_temp tt where tt.operator_id=:operatorId and tt.ig_batch_number=:iGBatchNumber ";
        BigDecimal beforTaxAmount = (BigDecimal) this.getEntityManager().createNativeQuery(q)
                .setParameter("operatorId", operatorId).setParameter("iGBatchNumber", batchNumber).getSingleResult();
        return beforTaxAmount != null ? beforTaxAmount : new BigDecimal(0);
    }

    @Override
    public void movePayoutData(long batchNumber, String operatorId) {
        // TODO Auto-generated method stub

        String sql = "insert into  payout(id,version, create_time, update_time, game_instance_id, transaction_id, ticket_serialno, total_amount, type, is_valid, status, total_amount_b4_tax, is_by_manual, winner_id, winner_name, has_detail, dev_id, merchant_id, operator_id, is_payout_confirmation, object_num, object_amount, batch_no2, game_id, utc_createtime)  select id,version, create_time, update_time, game_instance_id, transaction_id, ticket_serialno, total_amount, type, is_valid, status, total_amount_b4_tax, is_by_manual, winner_id, winner_name, has_detail, dev_id, merchant_id, operator_id, is_payout_confirmation, object_num, object_amount, batch_no2, game_id, utc_createtime from ig_payout_temp  where operator_id=:operatorId and ig_batch_number=:iGBatchNumber";
        int records = this.getEntityManager().createNativeQuery(sql).setParameter("operatorId", operatorId)
                .setParameter("iGBatchNumber", batchNumber).executeUpdate();
        logger.info("moved " + records + " records under batch number(" + batchNumber + ") and operator id("
                + operatorId + ") from payout temp table.");

    }

    @Override
    public void validateAllTicket(long batchNumber, String operatorId) {
        // TODO Auto-generated method stub
        /*
         * String sql=
         * "update  instant_ticket t set t.status=:status where t.ticket_serial in (select ticket_serialno  from ig_payout_temp  where operator_id=:operatorId and ig_batch_number=:iGBatchNumber)"
         * ; int records= this.getEntityManager().createNativeQuery(sql) .setParameter("operatorId",
         * operatorId).setParameter("iGBatchNumber", batchNumber).setParameter("status",
         * InstantTicket.STATUS_VALIDATED).executeUpdate(); logger.info("changed status of  "
         * +records+" records under batch number("
         * +batchNumber+") and operator id("+operatorId+") in IG tickets  table.");
         */this.changeStatusOfAllIGTickets(InstantTicket.STATUS_VALIDATED, batchNumber, operatorId);
    }

    @Override
    public void changeStatusOfAllIGTickets(int status, long batchNumber, String operatorId) {
        String sql = "update  instant_ticket t set t.status=:status where t.ticket_serial in (select ticket_serialno  from ig_payout_temp  where operator_id=:operatorId and ig_batch_number=:iGBatchNumber)";
        int records = this.getEntityManager().createNativeQuery(sql).setParameter("operatorId", operatorId)
                .setParameter("iGBatchNumber", batchNumber).setParameter("status", status).executeUpdate();
        logger.info("changed status(" + status + ") of  " + records + " records under batch number(" + batchNumber
                + ") and operator id(" + operatorId + ") in IG tickets  table.");

    }

    @Override
    public List<Game> getAllGamesOfThisBatch(long batchNumber, String operatorId) {
        // TODO Auto-generated method stub
        String sql = "select g.* from game g where g.game_id in (select ig.game_id from ig_game_instance ig where ig.ig_game_instance_id in ( select distinct(ig_game_instance_id) from instant_ticket it ,ig_payout_temp ip where ip.ticket_serialno=it.ticket_serial and ip.operator_id=:operatorId and ip.ig_batch_number=:iGBatchNumber))  ";
        List<Game> list = this.getEntityManager().createNativeQuery(sql, Game.class)
                .setParameter("operatorId", operatorId).setParameter("iGBatchNumber", batchNumber).getResultList();
        return list;

    }

    @Override
    public BigDecimal getActualAmountByGame(Game game, long batchNumber, String operatorId) {
        String q = "select sum(tt.total_amount) from ig_payout_temp tt,instant_ticket it ,ig_game_instance ig  where tt.ticket_serialno=it.ticket_serial and it.ig_game_instance_id=ig.ig_game_instance_id and ig.game_id= :gameId and  tt.operator_id=:operatorId and tt.ig_batch_number=:iGBatchNumber ";
        BigDecimal actualAmount = (BigDecimal) this.getEntityManager().createNativeQuery(q)
                .setParameter("operatorId", operatorId).setParameter("iGBatchNumber", batchNumber)
                .setParameter("gameId", game.getId()).getSingleResult();
        return actualAmount != null ? actualAmount : new BigDecimal(0);
    }

    @Override
    public Long getSucceededTicketsCount(long batchNumber, String operatorId) {
        String q = "select count(*) from ig_payout_temp tt where tt.operator_id=:operatorId and tt.ig_batch_number=:iGBatchNumber ";
        BigDecimal actualAmount = (BigDecimal) this.getEntityManager().createNativeQuery(q)
                .setParameter("operatorId", operatorId).setParameter("iGBatchNumber", batchNumber).getSingleResult();
        return actualAmount != null ? actualAmount.longValue() : 0;
    }

    @Override
    public void deleteDataByOperatorId(long batchNumber, String operatorId) {
        String sql = "delete from ig_payout_temp t where t.operator_id = :operatorId and t.ig_batch_number=:batchNumber";
        int records = this.getEntityManager().createNativeQuery(sql).setParameter("operatorId", operatorId)
                .setParameter("batchNumber", batchNumber).executeUpdate();
        logger.info("delete ig_payout_temp of  " + records + " records under batchNumber(" + batchNumber
                + ") operator id(" + operatorId + ") in ig_payout_temp  table.");
    }

}
