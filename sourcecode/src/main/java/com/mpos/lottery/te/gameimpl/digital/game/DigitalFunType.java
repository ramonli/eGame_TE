package com.mpos.lottery.te.gameimpl.digital.game;

import com.mpos.lottery.te.gamespec.game.BaseFunType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "FD_FUN_TYPE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "LFT_ID")) })
public class DigitalFunType extends BaseFunType {
    private static final long serialVersionUID = -2639363860618705002L;

    /**
     * For digital game, the K/N represents the range of XD, for example 2/4 means system support bet options from 2D to
     * 4D.
     * <p/>
     * The X/Y represents the range of each selected number, for example 0/9 means each number must be range from 0 to
     * 9.
     */
}
