package com.mpos.lottery.te.gamespec.prize;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TAX_DATE_RANGE")
public class TaxDateRange implements Serializable {
    private static final long serialVersionUID = -2462076033491100755L;
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "START_DATE")
    private Date beginDate;
    @Column(name = "END_DATE")
    private Date endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
