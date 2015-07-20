package com.mpos.lottery.te.valueaddservice.vat.web;

import java.util.List;

public class TicketPackDto {
    private long requestCount;

    private List<OfflineTicketDto> tickets;

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

    public List<OfflineTicketDto> getTickets() {
        return tickets;
    }

    public void setTickets(List<OfflineTicketDto> tickets) {
        this.tickets = tickets;
    }

}
