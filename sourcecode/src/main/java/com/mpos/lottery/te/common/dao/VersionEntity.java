package com.mpos.lottery.te.common.dao;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/*
 * All jpa entity should extends this class.
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class VersionEntity extends BaseEntity {

    @Column(name = "VERSION")
    private long version;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
