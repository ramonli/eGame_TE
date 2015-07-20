package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.junit.Test;

public class TransactionSerilizerUnitTest {

    @Test
    public void testSerialize() throws Exception {
        Transaction tran = LottoDomainMocker.mockTransaction();
        tran.getTicket().setGameInstance(LottoDomainMocker.mockGameDraw());
        tran.getTicket().setRawSerialNo(false, "123456");
        tran.getTicket().setBarcode(false, "01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1");
        tran.getTicket().setValidationCode("122345");
        tran.getTicket().setLastDrawNo("20090416");
        String xml = CastorHelper.marshal(tran, "xml-mapping/game/lotto/TransactionEnquiry_Res.xml");
        System.out.println(xml);
    }

}
