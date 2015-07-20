package com.mpos.lottery.te.valueaddservice.vat.web;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class VatOfflineSaleUploadDto {
    private int count;
    private int countOfSuccess;
    private int countOfFailure;
    private List<VatSaleTransactionDto> vatSaleList = new LinkedList<VatSaleTransactionDto>();

    public BigDecimal calVatTotalAmount() {
        BigDecimal total = new BigDecimal("0");
        for (VatSaleTransactionDto dto : this.getVatSaleList()) {
            total = total.add(dto.getVatTotalAmount());
        }
        return total;
    }

    /**
     * Check whether the count of <code>VatSaleTransaction</code> is matched with {@link #getCount()}
     * 
     * @return true if matched, otherwise false.
     */
    public boolean isCountMatched() {
        return this.getCount() == this.getVatSaleList().size();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<VatSaleTransactionDto> getVatSaleList() {
        return vatSaleList;
    }

    public void setVatSaleList(List<VatSaleTransactionDto> vatSaleList) {
        this.vatSaleList = vatSaleList;
    }

    public int getCountOfSuccess() {
        return countOfSuccess;
    }

    public void setCountOfSuccess(int countOfSuccess) {
        this.countOfSuccess = countOfSuccess;
    }

    public int getCountOfFailure() {
        return countOfFailure;
    }

    public void setCountOfFailure(int countOfFailure) {
        this.countOfFailure = countOfFailure;
    }

}
