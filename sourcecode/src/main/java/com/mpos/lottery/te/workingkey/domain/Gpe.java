package com.mpos.lottery.te.workingkey.domain;

import com.mpos.lottery.te.common.dao.VersionEntity;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "GPE")
public class Gpe extends VersionEntity {
    private static final long serialVersionUID = -6359142296385016348L;
    public static final int TYPE_IGPE = BaseTicket.TICKET_FROM_POS;
    // public static final int TYPE_HGPE = 2;
    public static final int TYPE_WGPESMS = BaseTicket.TICKET_FROM_WGPESMS;
    public static final int TYPE_SGPE = BaseTicket.TICKET_FROM_SGPE;
    public static final int TYPE_MGPE = BaseTicket.TICKET_FROM_MGPE;
    public static final int TYPE_WGPEPOS = BaseTicket.TICKET_FROM_WGPEPOS;
    public static final int TYPE_TGPE = BaseTicket.TICKET_FROM_TGPE;

    @Column(name = "IP_SOURCE")
    private String ipSource;

    @Column(name = "TYPE")
    private int type;

    @Column(name = "DESCRIPTION")
    private String desc;

    public String getIpSource() {
        return ipSource;
    }

    public void setIpSource(String ipSource) {
        this.ipSource = ipSource;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Simple return the type of GPE.
     */
    public int getTicketFrom() {
        // int ticketFrom = -1;
        // switch (this.type) {
        // case TYPE_IGPE :
        // ticketFrom = BaseTicket.TICKET_FROM_POS;
        // break;
        // case TYPE_SGPE :
        // ticketFrom = BaseTicket.TICKET_FROM_SGPE;
        // break;
        // case TYPE_WGPESMS :
        // ticketFrom = BaseTicket.TICKET_FROM_WGPESMS;
        // break;
        // case TYPE_MGPE :
        // ticketFrom = BaseTicket.TICKET_FROM_MGPE;
        // break;
        // case TYPE_WGPEPOS :
        // ticketFrom = BaseTicket.TICKET_FROM_WGPEPOS;
        // break;
        // default :
        // throw new SystemException("Unsupport gpe type:" + this.type);
        // }
        // return ticketFrom;
        return this.type;
    }
}
