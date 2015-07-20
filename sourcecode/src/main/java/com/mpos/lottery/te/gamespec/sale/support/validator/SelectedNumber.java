package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

public class SelectedNumber {
    public static final String DELEMETER_BASE = "-";
    public static final String DELEMETER_NUMBER = ",";

    private BaseEntry entry;
    private String selectedNumber;
    private String baseNumber;
    private String specialNumber;
    private String baseDelemeter = DELEMETER_BASE;
    private String numberDelemeter = DELEMETER_NUMBER;
    private int[] baseNumbers = new int[] {};
    private int[] specialNumbers = new int[] {};

    public SelectedNumber(BaseEntry entry) {
        this(entry, DELEMETER_BASE, DELEMETER_NUMBER);
    }

    /**
     * The base construcutor.
     */
    public SelectedNumber(BaseEntry entry, String baseDelemeter, String numberDelemeter) {
        if (entry == null) {
            throw new IllegalArgumentException("argument 'entry' can NOT be null");
        }
        if (baseDelemeter == null) {
            throw new IllegalArgumentException("argument 'baseDelemeter' can NOT be null");
        }
        if (numberDelemeter == null) {
            throw new IllegalArgumentException("argument 'numberDelemeter' can NOT be null");
        }

        this.entry = entry;
        this.selectedNumber = this.entry.getSelectNumber();
        this.baseDelemeter = baseDelemeter;
        this.numberDelemeter = numberDelemeter;

        // parse selected number
        String[] numberParts = this.selectedNumber.split(this.baseDelemeter);
        this.setBaseNumber(numberParts[0]);
        if (numberParts.length == 2) {
            this.setSpecialNumber(numberParts[1]);
        }
        this.setBaseNumbers(SimpleToolkit.string2IntArray(this.getBaseNumber(), this.numberDelemeter, true));
        if (this.specialNumber != null) {
            this.setSpecialNumbers(SimpleToolkit.string2IntArray(this.getSpecialNumber(), this.numberDelemeter, true));
        }

        // generate formatted selected number
        this.getEntry().setSelectNumber(this.formatSelectedNumber());
    }

    /**
     * If a selected number is started with '0', remove it. For example if selected number is '01,20,10,09', will be
     * formatted to '1,20,10,9'.
     */
    private String formatSelectedNumber() {
        StringBuffer formattedNumber = new StringBuffer("");
        formattedNumber.append(SimpleToolkit.formatNumericString(this.getBaseNumber(), this.getNumberDelemeter()));
        if (this.getSpecialNumber() != null && !"".equals(this.getSpecialNumber())) {
            formattedNumber.append(this.getBaseDelemeter()).append(
                    SimpleToolkit.formatNumericString(this.getSpecialNumber(), this.getNumberDelemeter()));
        }
        return formattedNumber.toString();
    }

    public String getBaseNumber() {
        return baseNumber;
    }

    private void setBaseNumber(String baseNumber) {
        this.baseNumber = baseNumber;
    }

    public String getSpecialNumber() {
        return specialNumber;
    }

    private void setSpecialNumber(String specialNumber) {
        this.specialNumber = specialNumber;
    }

    public int[] getBaseNumbers() {
        return baseNumbers;
    }

    private void setBaseNumbers(int[] baseNumbers) {
        this.baseNumbers = baseNumbers;
    }

    public int[] getSpecialNumbers() {
        return specialNumbers;
    }

    private void setSpecialNumbers(int[] specialNumbers) {
        this.specialNumbers = specialNumbers;
    }

    public String getSelectedNumber() {
        return selectedNumber;
    }

    public String getBaseDelemeter() {
        return baseDelemeter;
    }

    public String getNumberDelemeter() {
        return numberDelemeter;
    }

    public BaseEntry getEntry() {
        return entry;
    }

    /**
     * Return a int array of all numbers.
     */
    public int[] getAllNumbers() {
        int baseLength = baseNumbers != null ? baseNumbers.length : 0;
        int specialLength = specialNumbers != null ? specialNumbers.length : 0;
        int[] target = new int[baseLength + specialLength];
        if (baseNumbers != null) {
            System.arraycopy(baseNumbers, 0, target, 0, baseLength);
        }
        if (specialNumber != null) {
            System.arraycopy(specialNumbers, 0, target, baseLength, specialLength);
        }
        return target;
    }
}
