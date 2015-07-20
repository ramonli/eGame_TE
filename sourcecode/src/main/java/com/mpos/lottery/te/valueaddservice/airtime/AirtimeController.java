package com.mpos.lottery.te.valueaddservice.airtime;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.airtime.service.AirtimeService;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@SuppressWarnings({ "unchecked", "rawtypes" })
@Controller
public class AirtimeController {
    @Resource(name = "airtimeService")
    private AirtimeService airtimeService;

    /**
     * Controller for airtime topup. Multiple airtime providers are supported.
     */
    @RequestMap("{transType:455}")
    public void topup(Context request, Context response) throws ApplicationException {
        AirtimeTopup dto = (AirtimeTopup) request.getModel();
        AirtimeTopup respDto = this.getAirtimeService().topup(response, dto);

        response.setModel(respDto);
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    // -----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------

    public AirtimeService getAirtimeService() {
        return airtimeService;
    }

    public void setAirtimeService(AirtimeService airtimeService) {
        this.airtimeService = airtimeService;
    }

}
