package com.mpos.lottery.te.thirdpartyservice.playeraccount;

import com.google.gson.Gson;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.service.CashoutService;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.service.PlayerTopupService;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutRequest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutResponse;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerTopupDto;
import com.mpos.lottery.te.trans.domain.TransactionMessage;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
public class PlayerController {
    @Resource(name = "cashoutService")
    private CashoutService cashoutService;
    @Resource(name = "playerTopupService")
    private PlayerTopupService topupService;

    @RequestMap("{transType:445}")
    public void cashout(Context request, Context response) throws ApplicationException {
        CashoutRequest dto = (CashoutRequest) request.getModel();
        CashoutResponse respDto = this.getCashoutService().cashout(response, dto);
        response.setModel(respDto);
    }

    /**
     * Topup player's account.
     */
    @RequestMap("{transType:446}")
    public void topup(Context request, Context response) throws ApplicationException {
        PlayerTopupDto dto = (PlayerTopupDto) request.getModel();
        this.getTopupService().topup(response, dto);

        // write transaction message for reversal.
        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(response.getTransaction().getId());
        transMsg.setRequestMsg(new Gson().toJson(dto));
        response.getTransaction().setTransMessage(transMsg);
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    // -----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------

    public CashoutService getCashoutService() {
        return cashoutService;
    }

    public void setCashoutService(CashoutService cashoutService) {
        this.cashoutService = cashoutService;
    }

    public PlayerTopupService getTopupService() {
        return topupService;
    }

    public void setTopupService(PlayerTopupService topupService) {
        this.topupService = topupService;
    }

}
