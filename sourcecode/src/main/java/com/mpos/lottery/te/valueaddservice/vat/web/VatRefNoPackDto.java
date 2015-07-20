package com.mpos.lottery.te.valueaddservice.vat.web;

import java.util.List;

/**
 * <Description functions in a word> <Detail description>
 * 
 * @author terry
 */
public class VatRefNoPackDto {

    private long requestCount;

    private List<VatRefNoDto> vatRefNoDtos;

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public List<VatRefNoDto> getVatRefNoDtos() {
        return vatRefNoDtos;
    }

    public void setVatRefNoDtos(List<VatRefNoDto> vatRefNoDtos) {
        this.vatRefNoDtos = vatRefNoDtos;
    }
}
