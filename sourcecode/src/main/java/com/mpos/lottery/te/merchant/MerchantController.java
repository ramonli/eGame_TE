package com.mpos.lottery.te.merchant;

import com.google.gson.Gson;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.service.ActivityReportService;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.IncomeBalanceService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.CashOutByManualDto;
import com.mpos.lottery.te.merchant.web.CashOutByOperatorPassDto;
import com.mpos.lottery.te.merchant.web.CashOutPassDto;
import com.mpos.lottery.te.merchant.web.CreditTransferDto;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.merchant.web.IncomeBalanceDto;
import com.mpos.lottery.te.merchant.web.OperatorTopupDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionMessage;

import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.Comparator;

import javax.annotation.Resource;

@Controller
public class MerchantController {
    @Resource(name = "merchantService")
    private MerchantService merchantService;
    @Resource(name = "creditService")
    private CreditService creditService;
    @Resource(name = "activityReportService")
    private ActivityReportService activityReportService;
    @Resource(name = "incomeBalanceService")
    private IncomeBalanceService incomeBalanceService;

    @RequestMap("{transType:116}")
    public void transferCredit(Context request, Context response) throws ApplicationException {
        CreditTransferDto dto = (CreditTransferDto) request.getModel();
        CreditTransferDto respDto = this.getCreditService().transferCredit(request, response, dto);
        response.setModel(respDto);

        response.getTransaction().setTotalAmount(dto.getAmount());
        response.getTransaction().setVirn(String.valueOf(dto.getCreditType())); // set credit type to VIRN
        // write transaction message for reversal.
        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(response.getTransaction().getId());
        transMsg.setRequestMsg(new Gson().toJson(dto));
        request.getTransaction().setTransMessage(transMsg);
    }

    @RequestMap("{transType:330}")
    public void enquiryActivityReport(Context request, Context response) throws ApplicationException {
        ActivityReport reportReq = (ActivityReport) request.getModel();
        ActivityReport resultReport = this.getActivityReportService().query(request, reportReq);
        resultReport.setStartTime(reportReq.getStartTime());
        resultReport.setEndTime(reportReq.getEndTime());

        // sort daily report items by date
        Collections.sort(resultReport.getDailyActivityReports(), new Comparator<DailyActivityReport>() {

            @Override
            public int compare(DailyActivityReport o1, DailyActivityReport o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        response.setModel(resultReport);
    }

    @RequestMap("{transType:447}")
    public void topupOperator(Context request, Context response) throws ApplicationException {
        OperatorTopupDto reqDto = (OperatorTopupDto) request.getModel();
        OperatorTopupDto respDto = this.getCreditService().topupOperator(response, reqDto);

        response.getTransaction().setTotalAmount(reqDto.getAmount());
        // save serialNo of voucher to ticket serialNo column
        response.getTransaction().setTicketSerialNo(reqDto.getVoucherSerialNo());

        response.setModel(respDto);
    }

    @RequestMap("{transType:350}")
    public void incomeBalanceTransfer(Context request, Context response) throws ApplicationException {
        IncomeBalanceDto reqDto = (IncomeBalanceDto) request.getModel();
        IncomeBalanceDto respDto = this.getIncomeBalanceService().incomeBalanceTransfer(response, reqDto);

        // write transaction message for reversal.
        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(response.getTransaction().getId());
        transMsg.setRequestMsg(new Gson().toJson(respDto));
        request.getTransaction().setTransMessage(transMsg);
        request.getTransaction().setTotalAmount(reqDto.getAmount());
        response.setModel(respDto);
    }

    @RequestMap("{transType:351}")
    public void getCashoutPass(Context request, Context response) throws ApplicationException {
        CashOutPassDto reqDto = (CashOutPassDto) request.getModel();
        CashOutPassDto respDto = this.getMerchantService().getCashoutPass(request, reqDto);

        response.setModel(respDto);
    }

    @RequestMap("{transType:352}")
    public void cashoutOperatorByPass(Context request, Context response) throws ApplicationException {
        CashOutByOperatorPassDto reqDto = (CashOutByOperatorPassDto) request.getModel();
        CashOutByOperatorPassDto respDto = this.getMerchantService().cashoutOperatorByPass(request, response, reqDto);

        response.setModel(respDto);
    }

    @RequestMap("{transType:353}")
    public void cashoutOperatorByManual(Context request, Context response) throws ApplicationException {
        CashOutByManualDto reqDto = (CashOutByManualDto) request.getModel();
        CashOutByManualDto respDto = this.getMerchantService().cashoutOperatorByManual(request, reqDto);

        response.setModel(respDto);
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    // -----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public ActivityReportService getActivityReportService() {
        return activityReportService;
    }

    public void setActivityReportService(ActivityReportService activityReportService) {
        this.activityReportService = activityReportService;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

    /**
     * @return incomeBalanceService
     */
    public IncomeBalanceService getIncomeBalanceService() {
        return incomeBalanceService;
    }

    /**
     * @param incomeBalanceService
     */
    public void setIncomeBalanceService(IncomeBalanceService incomeBalanceService) {
        this.incomeBalanceService = incomeBalanceService;
    }

}
