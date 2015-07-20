package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PRIZE_PARAMETERS")
public class PrizeParameter {

    @Id
    @Column(name = "PARAMETER_ID")
    private String id;
    @Column(name = "PRIZE_LOGIC_ID")
    private String prizeLogicId;
    @Column(name = "PARAMETER_NAME")
    private String prizeLevel;
    @Column(name = "IS_PRIZE_OBJECT")
    private boolean objectPrize;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

    public String getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(String prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public boolean isObjectPrize() {
        return objectPrize;
    }

    public void setObjectPrize(boolean objectPrize) {
        this.objectPrize = objectPrize;
    }

}
