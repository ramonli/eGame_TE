package com.mpos.lottery.te.gameimpl.extraball.sale;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "EB_TE_ENTRY")
public class ExtraBallEntry extends BaseEntry {
    public static final int BET_OPTION_NUMBER = 1;
    public static final int BET_OPTION_COLOR = 3;
    public static final int BET_OPTION_RANGE = 2;

    @ManyToOne
    @JoinColumn(name = "EB_ALGORITHM_ID")
    private ExtraBallAlgorithm argorithm;

    public ExtraBallAlgorithm getArgorithm() {
        return argorithm;
    }

    public void setArgorithm(ExtraBallAlgorithm argorithm) {
        this.argorithm = argorithm;
    }

}
