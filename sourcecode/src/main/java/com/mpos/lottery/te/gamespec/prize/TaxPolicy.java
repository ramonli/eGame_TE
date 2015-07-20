package com.mpos.lottery.te.gamespec.prize;

import java.io.Serializable;
import java.util.List;

//@Entity(name="WINNER_TAX_POLICY")
public class TaxPolicy implements Serializable {
    private static final long serialVersionUID = -3803317340112847296L;
    // @Id
    // @Column(name="WINNER_TAX_POLICY_ID")
    private String id;

    // @Column(name="POLICY_NAME")
    private String name;

    // @OneToMany(fetch=FetchType.EAGER, mappedBy="taxPolicy")
    private List<TaxThreshold> taxThresholdList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TaxThreshold> getTaxThresholdList() {
        return taxThresholdList;
    }

    public void setTaxThresholdList(List<TaxThreshold> taxThresholdList) {
        this.taxThresholdList = taxThresholdList;
    }

}
