package com.mpos.lottery.te.gamespec.game;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BD_CHANNEL_SETTING_ITEM")
public class ChannelGameInstanceTime {
    @Id
    @Column(name = "id")
    private String id;
    /* refer to GPE.type_XXX */
    @Column(name = "SELLING_CHANNEL_TYPE")
    private int channelType;
    @Column(name = "STOP_SELLING_TIME")
    private int stopSellingTimeInMinutes;
    @Column(name = "BD_CHANNEL_SETTING_ID")
    private String gameChannelSettingId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getChannelType() {
        return channelType;
    }

    public void setChannelType(int channelType) {
        this.channelType = channelType;
    }

    public int getStopSellingTimeInMinutes() {
        return stopSellingTimeInMinutes;
    }

    public void setStopSellingTimeInMinutes(int stopSellingTimeInMinutes) {
        this.stopSellingTimeInMinutes = stopSellingTimeInMinutes;
    }

    public String getGameChannelSettingId() {
        return gameChannelSettingId;
    }

    public void setGameChannelSettingId(String gameChannelSettingId) {
        this.gameChannelSettingId = gameChannelSettingId;
    }

    @Override
    public String toString() {
        return "ChannelGameInstanceTime [id=" + id + ", channelType=" + channelType + ", stopSellingTimeInMinutes="
                + stopSellingTimeInMinutes + ", gameChannelSettingId=" + gameChannelSettingId + "]";
    }

}
