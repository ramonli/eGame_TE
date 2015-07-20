package com.mpos.lottery.te.gamespec.sale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SELECTED_NUMBER_STAT")
public class StatOfSelectedNumber {
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;
    @Column(name = "SELECTED_NUMBER")
    private String selecteNumber;
    @Column(name = "SELECTED_NUMBER_COUNT")
    private int count;

    public StatOfSelectedNumber() {
    }

    public StatOfSelectedNumber(String gameInstanceId, String selecteNumber, int count) {
        super();
        this.gameInstanceId = gameInstanceId;
        this.selecteNumber = selecteNumber;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public String getSelecteNumber() {
        return selecteNumber;
    }

    public void setSelecteNumber(String selecteNumber) {
        this.selecteNumber = selecteNumber;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gameInstanceId == null) ? 0 : gameInstanceId.hashCode());
        result = prime * result + ((selecteNumber == null) ? 0 : selecteNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StatOfSelectedNumber other = (StatOfSelectedNumber) obj;
        if (gameInstanceId == null) {
            if (other.gameInstanceId != null) {
                return false;
            }
        } else if (!gameInstanceId.equals(other.gameInstanceId)) {
            return false;
        }
        if (selecteNumber == null) {
            if (other.selecteNumber != null) {
                return false;
            }
        } else if (!selecteNumber.equals(other.selecteNumber)) {
            return false;
        }
        return true;
    }

}
