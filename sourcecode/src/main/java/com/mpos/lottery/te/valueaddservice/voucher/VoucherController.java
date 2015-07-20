package com.mpos.lottery.te.valueaddservice.voucher;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.voucher.service.VoucherService;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@SuppressWarnings({ "unchecked", "rawtypes" })
@Controller
public class VoucherController {
    @Resource(name = "telecoVoucherService")
    private VoucherService voucherService;

    /**
     * Controller for TELECO voucher.
     */
    @RequestMap("{transType:456}")
    public void topup(Context request, Context response) throws ApplicationException {
        Voucher dto = (Voucher) request.getModel();
        Voucher respDto = this.getVoucherService().sell(response, dto);

        response.setModel(respDto);
    }

    // -----------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------

    // -----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------

    public VoucherService getVoucherService() {
        return voucherService;
    }

    public void setVoucherService(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

}
