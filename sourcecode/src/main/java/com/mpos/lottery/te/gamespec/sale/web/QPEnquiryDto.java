package com.mpos.lottery.te.gamespec.sale.web;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.util.LinkedList;
import java.util.List;

public class QPEnquiryDto {
    // how many numbers required for a generated QP number?
    private int countOfNumbers = 1;
    // How many QP selected-numbers should be gnerated?
    private int countOfEntries = 1;
    private BaseGameInstance gameInstance;
    private List<BaseEntry> entries = new LinkedList<BaseEntry>();

    public int getCountOfNumbers() {
        return countOfNumbers;
    }

    public void setCountOfNumbers(int countOfNumbers) {
        this.countOfNumbers = countOfNumbers;
    }

    public int getCountOfEntries() {
        return countOfEntries;
    }

    public void setCountOfEntries(int countOfEntries) {
        this.countOfEntries = countOfEntries;
    }

    public BaseGameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public List<BaseEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BaseEntry> entries) {
        this.entries = entries;
    }

}
