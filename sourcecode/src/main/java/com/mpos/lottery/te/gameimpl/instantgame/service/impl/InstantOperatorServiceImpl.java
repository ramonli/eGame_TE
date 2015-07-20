package com.mpos.lottery.te.gameimpl.instantgame.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGBatchReportDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGFailedTicketsReportDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGOperatorBatchDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGPayoutDetailTempDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGPayoutTempDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGBatchReport;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGOperatorBatch;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gameimpl.instantgame.service.InstantOperatorService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InstantOperatorServiceImpl implements InstantOperatorService {
    private Log logger = LogFactory.getLog(InstantOperatorServiceImpl.class);

    private IGOperatorBatchDao igOperatorBatchDao;
    // clear
    private IGPayoutTempDao iGPayoutTempDao;
    private IGBatchReportDao iGBatchReportDao;
    private IGFailedTicketsReportDao iGFailedTicketsReportDao;
    private IGPayoutDetailTempDao iGPayoutDetailTempDao;

    private UUIDService uuidManager;

    @Override
    public InstantBatchReportDto getConfirmBatchNumber(Context ctx) throws ApplicationException {
        // should write business logic here
        InstantBatchReportDto instantBatchReportDto = new InstantBatchReportDto();
        IGOperatorBatch igOperatorBatch = igOperatorBatchDao.getIGOperatorBatch(ctx.getOperatorId());
        if (igOperatorBatch == null) {
            igOperatorBatch = new IGOperatorBatch();
            igOperatorBatch.setId(this.getUuidManager().getGeneralID());
            igOperatorBatch.setBatchNumber(1);
            igOperatorBatch.setOperatorId(ctx.getOperatorId());
            igOperatorBatch.setCreateBy(ctx.getOperatorId());
            igOperatorBatch.setUpdateBy(ctx.getOperatorId());

            this.getIgOperatorBatchDao().insert(igOperatorBatch);
        } else {
            // response batch number =batch number +1

            long batchNumber = igOperatorBatch.getBatchNumber();
            igOperatorBatch.setBatchNumber(batchNumber + 1);
            this.getIgOperatorBatchDao().update(igOperatorBatch);
            // delete temporary payout records
            // 1,delete payout records
            // 2,delete payout detail records

        }

        // delete before batch number
        long previousBatchNumber = igOperatorBatch.getBatchNumber() - 1;

        // IF ig_batch_report has no data then delete other tables data
        // Get batch report data with previousBatchNumber
        IGBatchReport batchReport = this.getIGBatchReportDao().getByBatchId(ctx.getOperatorId(), previousBatchNumber);
        if (batchReport == null) {
            // 3,update status of all IG tickets from 'processing'to 'active'

            this.getiGPayoutTempDao().changeStatusOfAllIGTickets(InstantTicket.STATUS_ACTIVE, previousBatchNumber,
                    ctx.getOperatorId());
            // delete IG_PAYOUT_TEMP data only users themselves
            this.getiGPayoutTempDao().deleteDataByOperatorId(previousBatchNumber, ctx.getOperatorId());

            // delete ig_batch_report data(no need to delete the table)
            // this.getIGBatchReportDao().deleteDataByOperatorId(previousBatchNumber,ctx.getOperatorId());

            // delete IG_FAILED_TICKETS_REPORT data
            this.getIGFailedTicketsReportDao().deleteDataByOperatorId(previousBatchNumber, ctx.getOperatorId());

            // delete IG_PAYOUT_DETAIL_TEMP data
            this.getIGPayoutDetailTempDao().deleteDataByOperatorId(previousBatchNumber, ctx.getOperatorId());

        } else {

        }

        instantBatchReportDto.setBatchNumber(igOperatorBatch.getBatchNumber());
        return instantBatchReportDto;
    }

    public IGPayoutTempDao getiGPayoutTempDao() {
        return iGPayoutTempDao;
    }

    public void setiGPayoutTempDao(IGPayoutTempDao iGPayoutTempDao) {
        this.iGPayoutTempDao = iGPayoutTempDao;
    }

    public IGOperatorBatchDao getIgOperatorBatchDao() {
        return igOperatorBatchDao;
    }

    public void setIgOperatorBatchDao(IGOperatorBatchDao igOperatorBatchDao) {
        this.igOperatorBatchDao = igOperatorBatchDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

    public IGPayoutTempDao getIGPayoutTempDao() {
        return iGPayoutTempDao;
    }

    public void setIGPayoutTempDao(IGPayoutTempDao payoutTempDao) {
        iGPayoutTempDao = payoutTempDao;
    }

    public IGBatchReportDao getIGBatchReportDao() {
        return iGBatchReportDao;
    }

    public void setIGBatchReportDao(IGBatchReportDao batchReportDao) {
        iGBatchReportDao = batchReportDao;
    }

    public IGFailedTicketsReportDao getIGFailedTicketsReportDao() {
        return iGFailedTicketsReportDao;
    }

    public void setIGFailedTicketsReportDao(IGFailedTicketsReportDao failedTicketsReportDao) {
        iGFailedTicketsReportDao = failedTicketsReportDao;
    }

    public IGPayoutDetailTempDao getIGPayoutDetailTempDao() {
        return iGPayoutDetailTempDao;
    }

    public void setIGPayoutDetailTempDao(IGPayoutDetailTempDao payoutDetailTempDao) {
        iGPayoutDetailTempDao = payoutDetailTempDao;
    }

}
