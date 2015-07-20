package com.mpos.lottery.te.gameimpl.bingo.sale;

import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "TE_BG_TICKET")
public class BingoTicket extends BaseTicket {
    private static final long serialVersionUID = 4575146870775685915L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BG_GAME_INSTANCE_ID", nullable = false)
    private BingoGameInstance gameInstance;

    @Column(name = "SERIAL")
    private String luckySerial;

    @Column(name = "BOOK")
    private String bookNo;

    @Column(name = "IMPORTED_SERIALNO")
    private String importedSerialNo;

    @Transient
    private BingoTicketRef ticketRef;

    public BingoTicket() {
    }

    public void merge(BingoTicketRef ticketRef) {
        this.ticketRef = ticketRef;
        // set serialNo to in-advanced generated one
        this.setRawSerialNo(ticketRef.getImportTicketSerialNo());
        // regenerate barcode
        this.setBarcode(new Barcoder(this.getGameInstance().getGame().getType(), this.getRawSerialNo()).getBarcode());
        this.setImportedSerialNo(ticketRef.getImportTicketSerialNo());
        // use the client supplied total_amount
        // this.setTotalAmount(ticketRef.getTotalAmount());
        // TODO what is difference with user supplied PIN??
        // this.setPIN(ticketRef.getPin());
        this.setBookNo(ticketRef.getBookNo());
        this.setLuckySerial(ticketRef.getLuckySerial());

        for (BingoEntryRef entryRef : ticketRef.getEntryRefs()) {
            this.getEntries().add(new BingoEntry(entryRef));
        }
    }

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (BingoGameInstance) gameInstance;
    }

    public String getLuckySerial() {
        return luckySerial;
    }

    public void setLuckySerial(String luckySerial) {
        this.luckySerial = luckySerial;
    }

    public String getBookNo() {
        return bookNo;
    }

    public void setBookNo(String bookNo) {
        this.bookNo = bookNo;
    }

    public String getImportedSerialNo() {
        return importedSerialNo;
    }

    public void setImportedSerialNo(String importedSerialNo) {
        this.importedSerialNo = importedSerialNo;
    }

    public BingoTicketRef getTicketRef() {
        return ticketRef;
    }

    public void setTicketRef(BingoTicketRef ticketRef) {
        this.ticketRef = ticketRef;
    }

}
