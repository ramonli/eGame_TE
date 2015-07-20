package com.mpos.lottery.te.gameimpl.digital.sale;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TE_FD_ENTRY")
public class DigitalEntry extends BaseEntry {
    /**
     * NOTE: At present, there are 1D,2D,3D,4D supported bet options, if plan to support 5D, 6D etc, simply add one more
     * entry(7.selectednumber.5=XXX) in mlottery_te.properties
     */
    public static final int DIGITAL_BETOPTION_SUM = -3;
    public static final int DIGITAL_BETOPTION_EVEN = -2;
    public static final int DIGITAL_BETOPTION_ODD = -1;
    public static final int DIGITAL_BETOPTION_1D = 1;
    public static final int DIGITAL_BETOPTION_2D = 2;
    public static final int DIGITAL_BETOPTION_3D = 3;
    public static final int DIGITAL_BETOPTION_4D = 4;

    private static final long serialVersionUID = 3361465258753494122L;

    /**
     * Whether the bet option of current entry is XD, such as 1D, 2D...
     * 
     * @return true if is XD, otherwise false.
     */
    public boolean isXD() {
        if (this.getBetOption() >= DIGITAL_BETOPTION_1D) {
            return true;
        }
        return false;
    }
}
