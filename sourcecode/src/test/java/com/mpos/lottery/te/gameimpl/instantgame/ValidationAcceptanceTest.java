package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class ValidationAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testValidateOk() throws Exception {
        Context response = this.doPost();
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType());
        InstantTicket ticket = LottoDomainMocker.mockInstantTicket();
        ticket.setSerialNo("198415681983");
        ticket.setTicketXOR3("37330200"); // ok
        // ticket.setTicketXOR2("37330219"); //fail

        PrizeLevelDto dto = new PrizeLevelDto();
        dto.setTicket(ticket);
        dto.setPrizeAmount(new BigDecimal("21111"));
        ctx.setModel(dto);
    }

    public static void main(String args[]) throws Exception {
        // Runnable r1 = new Runnable(){
        // public void run() {
        // try{
        // ValidationTest vt = new ValidationTest();
        // Context response = vt.doPost();
        // System.out.println("Transaction Id: " + response.getTransactionID());
        // }
        // catch(Exception e){
        // e.printStackTrace();
        // }
        // }
        // };

        Runnable r2 = new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 5; i++) {
                        ValidationAcceptanceTest vt = new ValidationAcceptanceTest();
                        Context response = vt.doPost();
                        System.out.println("Transaction Id: " + response.getTransactionID());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t1 = new Thread(r2);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r2);
        Thread t4 = new Thread(r2);
        Thread t5 = new Thread(r2);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
    }
}
