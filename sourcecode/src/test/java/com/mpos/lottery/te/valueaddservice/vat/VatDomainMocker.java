package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.DummyTicket;
import com.mpos.lottery.te.valueaddservice.vat.web.VatOfflineSaleUploadDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatSaleTransactionDto;

import java.math.BigDecimal;
import java.util.Date;

public class VatDomainMocker {

    public static VatSaleTransaction mockVatSaleTransaction() {
        VatSaleTransaction vat = new VatSaleTransaction();
        vat.setVatCode("foodA");
        vat.setVatTotalAmount(new BigDecimal("940"));
        return vat;
    }

    public static VatOfflineSaleUploadDto mockOfflineVatUploadDto() {
        VatOfflineSaleUploadDto dto = new VatOfflineSaleUploadDto();

        // --------------------------------------------
        // a raffle ticket of close game instance
        // --------------------------------------------
        VatSaleTransactionDto vatRaffleDto1 = new VatSaleTransactionDto();
        vatRaffleDto1.setVatCode("foodA");
        vatRaffleDto1.setVatTotalAmount(new BigDecimal("1000.0"));
        vatRaffleDto1.setVatRate(new BigDecimal("0.1"));
        vatRaffleDto1.setBuyerTaxNo("TAX-112");
        vatRaffleDto1.setVatRefNo("20140805001");
        vatRaffleDto1.setTransTimestampRef(new Date());
        // assemble ticket
        DummyTicket raffle1 = new DummyTicket();
        raffle1.setRawSerialNo("20140805001");
        raffle1.setTotalAmount(new BigDecimal("100.0"));
        raffle1.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        raffle1.setBarcode(false, "14cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/KasGYFkVHOyhvon3oFa5u");
        raffle1.setValidationCode("371842");
        raffle1.setTotalBets(1);
        raffle1.setMultipleDraws(1);
        vatRaffleDto1.setTicketDto(raffle1);
        // assemble game instance
        BaseGameInstance gameInstance1 = new BaseGameInstance();
        gameInstance1.setGameId("RA-1");
        gameInstance1.setNumber("11001");
        raffle1.setGameInstance(gameInstance1);

        dto.getVatSaleList().add(vatRaffleDto1);

        // --------------------------------------------
        // a raffle ticket of current active game instance
        // --------------------------------------------
        VatSaleTransactionDto vatRaffleDto2 = new VatSaleTransactionDto();
        vatRaffleDto2.setVatCode("foodA");
        vatRaffleDto2.setVatTotalAmount(new BigDecimal("1000.0"));
        vatRaffleDto2.setVatRate(new BigDecimal("0.1"));
        vatRaffleDto2.setBuyerTaxNo("TAX-112");
        vatRaffleDto2.setVatRefNo("20140805002");
        vatRaffleDto2.setTransTimestampRef(new Date());
        // assemble ticket
        DummyTicket raffle2 = new DummyTicket();
        raffle2.setRawSerialNo("20140805002");
        raffle2.setTotalAmount(new BigDecimal("100.0"));
        raffle2.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        raffle2.setBarcode(false, "14cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/KasGYFkVHOyyOxNj73VQT");
        raffle2.setValidationCode("371842");
        raffle2.setTotalBets(1);
        raffle2.setMultipleDraws(1);
        vatRaffleDto2.setTicketDto(raffle2);
        // assemble game instance
        BaseGameInstance gameInstance2 = new BaseGameInstance();
        gameInstance2.setGameId("RA-1");
        gameInstance2.setNumber("11002");
        raffle2.setGameInstance(gameInstance2);

        dto.getVatSaleList().add(vatRaffleDto2);

        // --------------------------------------------
        // a magic100 ticket of active game instance
        // --------------------------------------------
        VatSaleTransactionDto vatMagicDto1 = new VatSaleTransactionDto();
        vatMagicDto1.setVatCode("foodB");
        vatMagicDto1.setVatTotalAmount(new BigDecimal("1000.0"));
        vatMagicDto1.setVatRate(new BigDecimal("0.1"));
        vatMagicDto1.setBuyerTaxNo("TAX-112");
        vatMagicDto1.setVatRefNo("20140805003");
        vatMagicDto1.setTransTimestampRef(new Date());
        // assemble ticket
        DummyTicket magic1 = new DummyTicket();
        magic1.setRawSerialNo("20140805003");
        magic1.setTotalAmount(new BigDecimal("200.0"));
        magic1.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        magic1.setBarcode(false, "18cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/KasGYFkVHO7Q9AlJkdWsN");
        magic1.setValidationCode("371842");
        magic1.setTotalBets(2);
        magic1.setMultipleDraws(1);
        vatMagicDto1.setTicketDto(magic1);
        // assemble game instance
        BaseGameInstance magicGameInstance1 = new BaseGameInstance();
        magicGameInstance1.setGameId("LK-1");
        magicGameInstance1.setNumber("001");
        magic1.setGameInstance(magicGameInstance1);

        // assemble entries
        BaseEntry magicEntry1 = new BaseEntry();
        magicEntry1.setSelectNumber("59");
        magicEntry1.setBetOption(BaseEntry.BETOPTION_SINGLE);
        magicEntry1.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        magicEntry1.setTotalBets(1);
        magicEntry1.setEntryAmount(new BigDecimal("100.0"));
        magic1.getEntries().add(magicEntry1);
        BaseEntry magicEntry2 = new BaseEntry();
        magicEntry2.setSelectNumber("60");
        magicEntry2.setBetOption(BaseEntry.BETOPTION_SINGLE);
        magicEntry2.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        magicEntry2.setTotalBets(1);
        magicEntry2.setEntryAmount(new BigDecimal("100.0"));
        magic1.getEntries().add(magicEntry2);

        dto.getVatSaleList().add(vatMagicDto1);

        // --------------------------------------------
        // no ticket
        // --------------------------------------------
        VatSaleTransactionDto vatDto1 = new VatSaleTransactionDto();
        vatDto1.setVatCode("foodB");
        vatDto1.setVatTotalAmount(new BigDecimal("1000.0"));
        vatDto1.setVatRate(new BigDecimal("0.1"));
        vatDto1.setBuyerTaxNo("TAX-112");
        vatDto1.setVatRefNo("20140805004");
        vatDto1.setTransTimestampRef(new Date());
        dto.getVatSaleList().add(vatDto1);

        dto.setCount(dto.getVatSaleList().size());
        return dto;
    }
}
