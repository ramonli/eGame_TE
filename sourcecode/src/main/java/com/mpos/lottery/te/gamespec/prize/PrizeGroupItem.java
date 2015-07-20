package com.mpos.lottery.te.gamespec.prize;

import java.util.List;

public class PrizeGroupItem {
    public final static int GROUP_TYPE_NORMAL_DRAW = 1;
    public final static int GROUP_TYPE_GLOBAL_LUCKYDRAW = 2;
    public final static int GROUP_TYPE_IG = 3; // what is the usage?
    public final static int GROUP_TYPE_DAILY_CASH_DRAW = 4;
    public final static int GROUP_TYPE_2NDCHANCE_DRAW_BINGO = 5;
    private String id;
    private String prizeGroupId;
    private String strPrizeLevel;
    private int gameType;
    // refer to GROUP_TYPE_XXX
    private int groupType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrizeGroupId() {
        return prizeGroupId;
    }

    public void setPrizeGroupId(String prizeGroupId) {
        this.prizeGroupId = prizeGroupId;
    }

    public String getStrPrizeLevel() {
        return strPrizeLevel;
    }

    public void setStrPrizeLevel(String strPrizeLevel) {
        this.strPrizeLevel = strPrizeLevel;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getPrizeLevel() {
        return Integer.parseInt(this.strPrizeLevel);
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int prizeType) {
        this.groupType = prizeType;
    }

    public static boolean allow(int prizeLevel, List<PrizeGroupItem> groupItems) {
        boolean allowed = false;
        for (PrizeGroupItem groupItem : groupItems) {
            if (groupItem.getPrizeLevel() == prizeLevel) {
                allowed = true;
            }
        }
        return allowed;
    }
}
