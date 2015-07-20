package com.mpos.lottery.te.gameimpl.lotto.sale.domain;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TE_LOTTO_ENTRY")
public class LottoEntry extends BaseEntry implements Cloneable {
    private static final long serialVersionUID = 5822866924138705689L;

    /**
     * THe count of multiple of this entry.
     */
    @Column(name = "MULTIPLE_COUNT")
    private int multipleCount = 1;

    /**
     * The total boost amount of current entry.
     */
    @Column(name = "BOOST_AMOUNT")
    private BigDecimal boostAmount = new BigDecimal("0");

    public BigDecimal getBoostAmount() {
        return boostAmount;
    }

    public void setBoostAmount(BigDecimal boostAmount) {
        this.boostAmount = boostAmount;
    }

    public int getMultipleCount() {
        return multipleCount;
    }

    public void setMultipleCount(int multipleCount) {
        this.multipleCount = multipleCount;
    }

}
