package com.mpos.lottery.te.workingkey.web;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;
import com.mpos.lottery.te.workingkey.service.WorkingKeyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class WorkingKeyController {
    @Autowired
    private WorkingKeyService workingKeyService;

    @RequestMap("{transType:501}")
    public void lookupWorkingKey(Context request, Context response) throws ApplicationException {
        WorkingKey key = this.getWorkingKeyService().fetchWorkingKey(request.getGpe().getId());
        response.setModel(key);
    }

    @RequestMap("{transType:502}")
    public void checkAlive(Context request, Context response) throws ApplicationException {
        this.getWorkingKeyService().checkAlive();
    }

    public WorkingKeyService getWorkingKeyService() {
        return workingKeyService;
    }

    public void setWorkingKeyService(WorkingKeyService workingKeyService) {
        this.workingKeyService = workingKeyService;
    }

}
