package com.mpos.lottery.te.gameimpl.instantgame.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "IG_OPERATION_PARAMETERS")
@Entity
public class IgOperationParameter {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "MAX_VALIDATE_TIMES")
    private int maxValidateTimes;

    @Column(name = "DESCRIPTION")
    private String desc;

    @Column(name = "START_SERIAL_NO")
    private long beginTicketIndex;

    @Column(name = "END_SERIAL_NO")
    private long endTicketIndex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxValidateTimes() {
        return maxValidateTimes;
    }

    public void setMaxValidateTimes(int maxValidateTimes) {
        this.maxValidateTimes = maxValidateTimes;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getBeginTicketIndex() {
        return beginTicketIndex;
    }

    public void setBeginTicketIndex(long beginTicketIndex) {
        this.beginTicketIndex = beginTicketIndex;
    }

    public long getEndTicketIndex() {
        return endTicketIndex;
    }

    public void setEndTicketIndex(long endTicketIndex) {
        this.endTicketIndex = endTicketIndex;
    }

}
