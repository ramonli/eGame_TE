package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.web.VatSaleTransactionDto;

import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Test {@link org.springframework.beans.BeanUtils}
 * 
 * @author Ramon
 * 
 */
public class BeanUtilsUnitTest {

    @Test
    public void test() {
        VatSaleTransactionDto dto = new VatSaleTransactionDto();
        dto.setBusinessType("2");
        dto.setCreateTime(new Date());
        dto.setTicket(new RaffleTicket());
        dto.setVatRate(new BigDecimal("0.1"));

        VatSaleTransaction dest = new VatSaleTransaction();
        BeanUtils.copyProperties(dto, dest);

        assertEquals(dto.getBusinessType(), dest.getBusinessType());
        assertEquals(dto.getCreateTime().getTime(), dest.getCreateTime().getTime());
        assertEquals(dto.getVatRate().doubleValue(), dest.getVatRate().doubleValue(), 0);
        assertEquals(dto.getTicket(), dest.getTicket());
        assertEquals(0, dest.getVatTotalAmount().doubleValue(), 0);

    }

}
