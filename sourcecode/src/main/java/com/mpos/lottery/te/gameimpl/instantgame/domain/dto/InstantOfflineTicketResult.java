package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import java.util.List;

public class InstantOfflineTicketResult {
    private List<InstantTicketResult> results;

    public List<InstantTicketResult> getResults() {
        return results;
    }

    public void setResults(List<InstantTicketResult> results) {
        this.results = results;
    }

}
