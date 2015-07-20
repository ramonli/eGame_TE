package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PRIZE_LOGIC")
public class PrizeLogic {
    @Id
    @Column(name = "PRIZE_LOGIC_ID")
    private String prizeLogicId;
    @Column(name = "BD_PRIZE_LOGIC_ID")
    private String objectPrizeLogicId;

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

    public String getObjectPrizeLogicId() {
        return objectPrizeLogicId;
    }

    public void setObjectPrizeLogicId(String objectPrizeLogicId) {
        this.objectPrizeLogicId = objectPrizeLogicId;
    }

}
