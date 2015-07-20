package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGPayoutDetailTempDao;

public class IGPayoutDetailTempDaoImpl extends BaseJpaDao implements IGPayoutDetailTempDao {

    @Override
    public void movePayoutDetailData(long batchNumber, String operatorId) {
        // TODO Auto-generated method stub

        String sql = "insert into payout_detail(id, payout_id, total_amount, cash_amount, topup_amount, topup_mode, payout_type, bg_lucky_prize_object_id, bg_lucky_prize_object_name, create_time, create_by, update_time, update_by, object_num, object_type, object_num_per_level_item) select id, payout_id, total_amount, cash_amount, topup_amount, topup_mode, payout_type, bg_lucky_prize_object_id, bg_lucky_prize_object_name, create_time, create_by, update_time, update_by, object_num, object_type, object_num_per_level_item from ig_payout_detail_temp where operator_id=:operatorId and ig_batch_number=:iGBatchNumber";
        int records = this.getEntityManager().createNativeQuery(sql).setParameter("operatorId", operatorId)
                .setParameter("iGBatchNumber", batchNumber).executeUpdate();
        logger.info("moved " + records + " records under batch number(" + batchNumber + ") and operator id("
                + operatorId + ") from payout detail temp table.");

    }

    @Override
    public void deleteDataByOperatorId(long batchNumber, String operatorId) {
        String sql = "delete from ig_payout_detail_temp t where t.operator_id = :operatorId and t.ig_batch_number = :batchNumber";
        int records = this.getEntityManager().createNativeQuery(sql).setParameter("operatorId", operatorId)
                .setParameter("batchNumber", batchNumber).executeUpdate();
        logger.info("delete ig_payout_detail_temp of  " + records + " records under batchNumber(" + batchNumber
                + ") operator id(" + operatorId + ") in ig_payout_detail_temp  table.");
    }

}
