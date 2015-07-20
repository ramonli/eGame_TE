package com.mpos.lottery.te.gameimpl.extraball.sale;

import com.mpos.lottery.te.common.dao.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "EB_ALGORITHM")
public class ExtraBallAlgorithm extends BaseEntity {
    // refer to ExtraBallEntry.BET_OPTION_XX
    @Column(name = "ALGORITHM_TYPE")
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
