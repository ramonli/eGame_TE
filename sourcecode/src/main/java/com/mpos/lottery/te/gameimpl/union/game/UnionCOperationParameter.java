package com.mpos.lottery.te.gameimpl.union.game;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The settings of 'Triple' and 'Double' will be used to control risk. For example a single selected number of UNION may
 * be:
 * <p>
 * '1,2,3,4,5,6-2,4'
 * <p>
 * Here the 'triple' part is '1,2,3,4,5,6', and the 'double' part is '2,4'. The semantics of setting
 * <code>UnionCOperationParameter</code> is:
 * <p>
 * if the count of numbers of 'triple' part is N(triple), then the count of numbers of 'double' part must between
 * X(minDouble) and Y(maxDouble).
 * 
 * @author Ramon
 * 
 */
@Entity
@Table(name = "UN_C_OPERATION_PARAMETERS")
public class UnionCOperationParameter implements Serializable {
    private static final long serialVersionUID = 8113068780496773740L;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "SEQ_NUMBER")
    private int seqNumber;

    @Column(name = "TRIPLE")
    private int triple;

    @Column(name = "MIN_DOUBLE")
    private int minDouble;

    @Column(name = "MAX_DOUBLE")
    private int maxDouble;

    @Column(name = "OPERATOR_PARAMETER_ID")
    private String operationParameterId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public int getTriple() {
        return triple;
    }

    public void setTriple(int triple) {
        this.triple = triple;
    }

    public int getMinDouble() {
        return minDouble;
    }

    public void setMinDouble(int minDouble) {
        this.minDouble = minDouble;
    }

    public int getMaxDouble() {
        return maxDouble;
    }

    public void setMaxDouble(int maxDouble) {
        this.maxDouble = maxDouble;
    }

    public String getOperationParameterId() {
        return operationParameterId;
    }

    public void setOperationParameterId(String operationParameterId) {
        this.operationParameterId = operationParameterId;
    }

}
