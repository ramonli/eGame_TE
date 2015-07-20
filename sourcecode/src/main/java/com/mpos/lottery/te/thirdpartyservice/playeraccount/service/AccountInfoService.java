package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;

import net.mpos.apc.entry.GetAccountInfo.ResGetAccountInfo;

public interface AccountInfoService {

    ResGetAccountInfo enquiry(Context<?> responseCtx, String mobileNo) throws ApplicationException;
}
