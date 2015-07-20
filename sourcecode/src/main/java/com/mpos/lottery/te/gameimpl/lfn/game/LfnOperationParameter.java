package com.mpos.lottery.te.gameimpl.lfn.game;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LFN_OPERATION_PARAMETERS")
public class LfnOperationParameter extends BaseOperationParameter {

    private static final long serialVersionUID = -8405940090379771594L;

    /**
     * For LFN, base amount will be used to limit the minimal amount a player must buy.
     */
}
