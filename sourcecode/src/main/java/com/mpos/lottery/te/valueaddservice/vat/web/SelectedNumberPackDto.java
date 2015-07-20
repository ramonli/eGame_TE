package com.mpos.lottery.te.valueaddservice.vat.web;

import java.util.List;

public class SelectedNumberPackDto {
    private List<NumberDto> numberDtos;

    private long requestCount;

    public List<NumberDto> getNumberDtos() {
        return numberDtos;
    }

    public void setNumberDtos(List<NumberDto> numberDtos) {
        this.numberDtos = numberDtos;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }

}
