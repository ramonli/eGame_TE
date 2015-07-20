package com.mpos.lottery.te.gameimpl.bingo.game;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BG_OPERATION_PARAMETERS")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "PARAMETERS_ID")) })
public class BingoOperationParameter extends BaseOperationParameter {

    private static final long serialVersionUID = -8405940090379771594L;

}
