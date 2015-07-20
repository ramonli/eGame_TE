package com.mpos.lottery.te.gameimpl.lotto.draw.domain;

import com.mpos.lottery.te.gamespec.game.BaseFunType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "LOTTO_FUN_TYPE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "LFT_ID")) })
public class LottoFunType extends BaseFunType {
    private static final long serialVersionUID = -2639363860618705002L;

    @Column(name = "EXTRA_NO")
    private int extraNo;

    @Column(name = "FREE_BALLS")
    private int numberOfFreeBalls;

    public int getExtraNo() {
        return extraNo;
    }

    public void setExtraNo(int extraNo) {
        this.extraNo = extraNo;
    }

    public int getNumberOfFreeBalls() {
        return numberOfFreeBalls;
    }

    public void setNumberOfFreeBalls(int numberOfFreeBalls) {
        this.numberOfFreeBalls = numberOfFreeBalls;
    }
}
