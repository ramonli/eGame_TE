package com.mpos.lottery.te.gameimpl.magic100.sale;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "LK_REQUEUE_NUMBERS")
public class RequeuedNumbers extends BaseEntity {
    private static final long serialVersionUID = 6351996763582252933L;
    /**
     * THe associated cancellation transaction. In general, only cancellation transaction will generate requeued
     * numbers.
     */
    @Column(name = "TRANS_ID")
    private String transactionId;
    @Column(name = "LK_GAME_INSTACE_ID")
    private String gameInstanceId;
    /**
     * The beginning lucky number of the cancelled range. For example, if client cancel a range numbers of 2~7, this
     * field will be set to 2.
     * <p/>
     * This value will be changed if client buy part of the requeued numbers, for example a player buy 2~5 of the
     * requeued 2~7, then the begin of requeued number will be 6.
     * <p/>
     * !!This field isn't used now, use <code>RequeuedNumbersItem</code> now.!!
     */
    @Deprecated
    @Column(name = "NUMBER_SEQ ")
    private long beginOfValidNumbers;
    /**
     * How many requeued numbers are available for sale? For example, client cancelled a range numbers of 2~7, then both
     * the 'countOfValidNumbers' and 'countOfNumbers' are 6. If later 2~5 are sold, the 'countOfValidNumbers' will be 3,
     * and the 'countOfNumbers' is still 7.
     */
    @Column(name = "COUNT_OF_VALID_NUMBER")
    private int countOfValidNumbers;
    /**
     * How many numbers are cancelled?
     */
    @Column(name = "COUNT_OF_NUMBER")
    private int countOfNumbers;
    @OneToMany(mappedBy = "requeuedNumbers", fetch = FetchType.EAGER)
    // explicitly require underlying hibernate to generate SQL with
    // 'join'...seem doesn't work for @OneToMany
    // @Fetch(FetchMode.JOIN)
    private List<RequeuedNumbersItem> requeuedNumbersItemList = new LinkedList<RequeuedNumbersItem>();

    public List<RequeuedNumbersItem> lookupValidItems(int countOfNumbers) {
        // sort the items by sequence_No
        if (requeuedNumbersItemList.size() > 1) {
            Collections.sort(requeuedNumbersItemList, new Comparator<RequeuedNumbersItem>() {

                @Override
                public int compare(RequeuedNumbersItem o1, RequeuedNumbersItem o2) {
                    return (int) (o1.getSequenceOfNumber() - o2.getSequenceOfNumber());
                }

            });
        }

        List<RequeuedNumbersItem> validItems = new LinkedList<RequeuedNumbersItem>();
        for (RequeuedNumbersItem item : requeuedNumbersItemList) {
            if (RequeuedNumbersItem.STATE_VALID == item.getState()) {
                validItems.add(item);
            }
        }
        if (countOfNumbers > validItems.size()) {
            // should lookup numbers from main cycle
            return new LinkedList<RequeuedNumbersItem>();
        } else {
            return validItems.subList(0, countOfNumbers);
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public long getBeginOfValidNumbers() {
        return beginOfValidNumbers;
    }

    public void setBeginOfValidNumbers(long beginOfValidNumbers) {
        this.beginOfValidNumbers = beginOfValidNumbers;
    }

    public int getCountOfValidNumbers() {
        return countOfValidNumbers;
    }

    public void setCountOfValidNumbers(int countOfValidNumbers) {
        this.countOfValidNumbers = countOfValidNumbers;
    }

    public int getCountOfNumbers() {
        return countOfNumbers;
    }

    public void setCountOfNumbers(int countOfNumbers) {
        this.countOfNumbers = countOfNumbers;
    }

    public List<RequeuedNumbersItem> getRequeuedNumbersItemList() {
        return requeuedNumbersItemList;
    }

    public void setRequeuedNumbersItemList(List<RequeuedNumbersItem> requeuedNumbersItemList) {
        this.requeuedNumbersItemList = requeuedNumbersItemList;
    }

}
