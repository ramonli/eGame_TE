package com.mpos.lottery.te.gamespec.prize;

import com.mpos.lottery.te.common.dao.VersionEntity;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This entity represents the relationship between new printed ticket ans old ticket. When the payout mode is 'print new
 * ticket', a multiple-draw ticket maybe generate new ticket when do payout.
 */
@Entity
@Table(name = "NEWPRINT_TICKET")
public class NewPrintTicket extends VersionEntity {
    private static final long serialVersionUID = 2822565927770560479L;
    public static final int STATUS_REVERSED = BaseTicket.STATUS_INVALID;
    public static final int STATUS_WAITCONFIRM = 1;
    public static final int STATUS_CONFIRMED = 3;

    @Column(name = "OLD_TICKET_SERIALNO")
    private String oldTicketSerialNo;

    @Column(name = "NEW_TICKET_SERIALNO")
    private String newTicketSerialNo;

    @Column(name = "STATUS")
    private int status;

    public NewPrintTicket() {
    }

    public NewPrintTicket(String id, String oldTicketSerialNo, String newTicketSerialNo) {
        super();
        this.setId(id);
        this.setCreateTime(new Date());
        this.setUpdateTime(this.getCreateTime());
        this.oldTicketSerialNo = oldTicketSerialNo;
        this.newTicketSerialNo = newTicketSerialNo;
        this.status = STATUS_WAITCONFIRM;
    }

    public String getOldTicketSerialNo() {
        return oldTicketSerialNo;
    }

    public void setOldTicketSerialNo(String oldTicketSerialNo) {
        this.oldTicketSerialNo = oldTicketSerialNo;
    }

    public String getNewTicketSerialNo() {
        return newTicketSerialNo;
    }

    public void setNewTicketSerialNo(String newTicketSerialNo) {
        this.newTicketSerialNo = newTicketSerialNo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
