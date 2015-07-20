package com.mpos.lottery.te.gamespec.prize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name = "BD_PRIZE_LEVEL")
@Entity
public class PrizeLevel implements Serializable {
    private static final long serialVersionUID = -5759308599431676228L;
    public static final int PRIZE_TYPE_CASH = 1;
    public static final int PRIZE_TYPE_OBJECT = 2;
    public static final int PRIZE_TYPE_BOTH = 3;

    public static final int STATUS_CODE_DUP_MATCHED = 2;
    public static final int STATUS_CODE_DUP_UNMATCHED = 1;
    public static final int STATUS_CODE_UNMATCHED = 3;

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "UPDATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
    @Column(name = "BD_PRIZE_LOGIC_ID")
    private String prizeLogicId;
    @Column(name = "PRIZE_LEVEL")
    private int prizeLevel;
    // This value will be set once the prize level has been calculated
    @Column(name = "PRIZE_LEVEL_TYPE")
    private int prizeType = PRIZE_TYPE_CASH;
    // How many players have won this prize level??
    @Column(name = "PRIZE_WIN_COUNT")
    private Integer numberOfWinner = 0;
    @OneToMany(mappedBy = "prizeLevel", fetch = FetchType.LAZY)
    private List<PrizeLevelItem> levelItems = new ArrayList<PrizeLevelItem>();

    public int getNumberOfObject() {
        int total = 0;
        for (PrizeLevelItem item : this.levelItems) {
            if (PrizeLevel.PRIZE_TYPE_CASH != item.getPrizeType()) {
                total += item.getNumberOfObject();
            }
        }
        return total;
    }

    public List<PrizeLevelItem> getItemByPrizeType(int prizeType) {
        List<PrizeLevelItem> prizeLevelItems = new ArrayList<PrizeLevelItem>();
        for (PrizeLevelItem item : this.levelItems) {
            if (prizeType == item.getPrizeType()) {
                prizeLevelItems.add(item);
            }
        }
        return prizeLevelItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(int prizeType) {
        this.prizeType = prizeType;
    }

    public Integer getNumberOfWinner() {
        return numberOfWinner;
    }

    public void setNumberOfWinner(Integer numberOfWinner) {
        this.numberOfWinner = numberOfWinner;
    }

    public List<PrizeLevelItem> getLevelItems() {
        return levelItems;
    }

    public void setLevelItems(List<PrizeLevelItem> levelItems) {
        this.levelItems = levelItems;
    }

}
