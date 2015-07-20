package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.workingkey.domain.Gpe;

import net.mpos.apc.entry.GetAccountInfo.ResGetAccountInfo;

import org.junit.Test;

import java.util.Date;

import javax.annotation.Resource;

public class DefaultAccountInfoServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "accountInfoService")
    private AccountInfoService accountInfoService;

    @Test
    public void testEnquiry() throws Exception {
        printMethod();

        Context ctx = new Context();
        Gpe gpe = new Gpe();
        gpe.setType(Gpe.TYPE_IGPE);
        ctx.setGpe(gpe);
        Transaction trans = new Transaction();
        ctx.setTransaction(trans);
        trans.setUpdateTime(new Date());

        ResGetAccountInfo accInfo = this.getAccountInfoService().enquiry(ctx, "98800138000");
        System.out.println(accInfo.getUserId());
    }

    public AccountInfoService getAccountInfoService() {
        return accountInfoService;
    }

    public void setAccountInfoService(AccountInfoService accountInfoService) {
        this.accountInfoService = accountInfoService;
    }

}
