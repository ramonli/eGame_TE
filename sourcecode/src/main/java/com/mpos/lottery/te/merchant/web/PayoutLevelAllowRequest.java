package com.mpos.lottery.te.merchant.web;

import java.util.Set;

/**
 * The request DTO of whether a prize level is granted to a operator.
 * 
 */
public class PayoutLevelAllowRequest {
    private Set<Integer> requestedPrizeLevels;
    // payout of which game type
    private int gameType;
    // refer to PrizeGroupItem.GROUP_XXX
    private int groupType;

    public PayoutLevelAllowRequest(Set<Integer> requestedPrizeLevels, int gameType, int prizeGroupType) {
        if (requestedPrizeLevels == null) {
            throw new IllegalArgumentException("argument 'requestedPrizeLevels' can't be null.");
        }

        this.requestedPrizeLevels = requestedPrizeLevels;
        this.gameType = gameType;
        this.groupType = prizeGroupType;
    }

    public Set<Integer> getRequestedPrizeLevels() {
        return requestedPrizeLevels;
    }

    public int getGameType() {
        return gameType;
    }

    public int getPrizeGroupType() {
        return groupType;
    }

}
