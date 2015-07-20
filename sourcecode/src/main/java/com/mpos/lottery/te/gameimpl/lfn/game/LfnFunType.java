package com.mpos.lottery.te.gameimpl.lfn.game;

import com.mpos.lottery.te.gamespec.game.BaseFunType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LFN_FUN_TYPE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "LFT_ID")) })
public class LfnFunType extends BaseFunType {
    private static final long serialVersionUID = -2639363860618705002L;

}
