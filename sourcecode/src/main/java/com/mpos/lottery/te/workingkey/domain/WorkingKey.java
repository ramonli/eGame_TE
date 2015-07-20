package com.mpos.lottery.te.workingkey.domain;

import com.mpos.lottery.te.common.dao.VersionEntity;
import com.mpos.lottery.te.common.util.SimpleToolkit;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "GPE_KEY")
public class WorkingKey extends VersionEntity {
    private static final String DATE_PATTERN = "yyyyMMdd";
    private static final long serialVersionUID = -508271854572721271L;

    @Column(name = "DATA_KEY")
    private String dataKey; // A base64 string

    @Column(name = "MAC_KEY")
    private String macKey; // A base64 string

    @Column(name = "GPE_ID")
    private String gpeId;

    @Column(name = "CREATE_DATE")
    private String createDateStr;

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getMacKey() {
        return macKey;
    }

    public void setMacKey(String macKey) {
        this.macKey = macKey;
    }

    public String getGpeId() {
        return gpeId;
    }

    public void setGpeId(String gpeId) {
        this.gpeId = gpeId;
    }

    public String getCreateDateStr() {
        return createDateStr;
    }

    public void setCreateDateStr(String createDateStr) {
        this.createDateStr = createDateStr;
    }

    // public Date getCreateDate() {
    // assert this.createDateStr != null : "createDateStr can NOT be null.";
    // SimpleToolkit.parseDate(this.createDateStr, DATE_PATTERN);
    // }
    //
    // public void setCreateDate(Date createDate) {
    // SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
    // this.createDateStr = sdf.format(createDate);
    // }

    public static String getCurrentDateStr() {
        return SimpleToolkit.formatDate(new Date(), DATE_PATTERN);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
