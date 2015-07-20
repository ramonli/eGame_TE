package com.mpos.lottery.te.port.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;

public interface FacadeService {

    void facade(Context request, Context response) throws ApplicationException;

}
