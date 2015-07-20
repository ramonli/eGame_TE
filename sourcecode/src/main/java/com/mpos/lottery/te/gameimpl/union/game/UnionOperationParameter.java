package com.mpos.lottery.te.gameimpl.union.game;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "UN_OPERATION_PARAMETERS")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "UN_OP_PARAMETERS_ID")) })
public class UnionOperationParameter extends BaseOperationParameter {
    private static final long serialVersionUID = -8839599266674735031L;

    @Transient
    private List<UnionCOperationParameter> cOperationParameters = new LinkedList<UnionCOperationParameter>();

    /**
     * Lookup constraints of the triple and double setting. If null returned, no any constraints will be applied.
     */
    public UnionCOperationParameter findByTriple(int tripleLength) {
        if (this.cOperationParameters == null) {
            return null;
        }
        for (UnionCOperationParameter cParam : this.cOperationParameters) {
            if (tripleLength == cParam.getTriple()) {
                return cParam;
            }
        }
        return null;
    }

    public List<UnionCOperationParameter> getcOperationParameters() {
        return cOperationParameters;
    }

    public void setcOperationParameters(List<UnionCOperationParameter> cOperationParameters) {
        this.cOperationParameters = cOperationParameters;
    }

}
