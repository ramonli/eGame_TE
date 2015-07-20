package com.mpos.lottery.te.merchant.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DW_TOPUP_MERCHANT_RELATION")
public class TopupDistributor {

    @Id
    @Column(name = "ID")
    private String ID;

    /**
     * Represents the distributor which will be topuped. For example salesman of distributorA can topup to all retailers
     * belong to distributor B and C, here <code>TopupDistributor</code> represents B and C.
     */
    @Column(name = "TOPUP_MERCHANT_ID")
    private long distributorId;

    @Column(name = "MERCHANT_ID")
    private long masterDistributorId;

    public long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(long distributorId) {
        this.distributorId = distributorId;
    }

    public long getMasterDistributorId() {
        return masterDistributorId;
    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public void setMasterDistributorId(long masterDistributorId) {
        this.masterDistributorId = masterDistributorId;
    }

}
