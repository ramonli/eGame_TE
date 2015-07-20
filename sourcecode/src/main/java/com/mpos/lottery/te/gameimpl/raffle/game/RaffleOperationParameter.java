package com.mpos.lottery.te.gameimpl.raffle.game;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "RA_OPERATION_PARAMETERS")
public class RaffleOperationParameter extends BaseOperationParameter {

    private static final long serialVersionUID = -3922870755557147781L;

}
