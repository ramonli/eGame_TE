package com.mpos.lottery.te.port.protocol;

import static org.junit.Assert.fail;

import com.mpos.lottery.te.gameimpl.lotto.draw.domain.GameResult;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;

import org.junit.Test;

import java.util.Date;

public class OfflineSettingDownloadSerializerUnitTest {

    @Test
    public void testSerialize() {
        try {
            String xml = "<OfflineTicketPack xmlns:ns2=\"http://www.lottery.mpos.com/wgpe/port/schemas/\">"
                    + "    <VAT code=\"foodA\"/>" + "    <VatRefNoPack requestCount=\"2\"/>"
                    + "    <TicketPack requestCount=\"2\"/>" + "    <SelectedNumberPack requestCount=\"2\"/>"
                    + "</OfflineTicketPack>";
            Object o = CastorHelper.unmarshal(xml, "xml-mapping/vat/DownloadOfflineTicket_Req.xml");
            System.out.println(o);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private LottoGameInstance mock() {
        LottoGameInstance gameDraw = new LottoGameInstance();
        gameDraw.setNumber("20090408");
        gameDraw.setBeginTime(new Date());
        gameDraw.setEndTime(new Date());
        GameResult result = new GameResult();
        result.setBaseNumber("2,10,11,23,34,35");
        result.setSpecialNumber(9);
        gameDraw.setResult(result);
        gameDraw.setSnowBall(true);
        gameDraw.setState(LottoGameInstance.STATE_ACTIVE);
        gameDraw.setGameId("08123c7812a82d");

        return gameDraw;
    }
}
