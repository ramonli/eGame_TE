package com.mpos.lottery.te.gameimpl.lfn.sale;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import javax.persistence.Entity;

@Entity(name = "LFN_TE_ENTRY")
public class LfnEntry extends BaseEntry implements Cloneable {
    private static final long serialVersionUID = -1416426793484836960L;
    public static final int BETOPTION_INTERVAL = 50;

    public static final int BETOPTION_N1 = 1;
    public static final int BETOPTION_N2 = 2;
    public static final int BETOPTION_N3 = 3;
    public static final int BETOPTION_N4 = 4;
    public static final int BETOPTION_N5 = 5;

    public static final int BETOPTION_P1 = BETOPTION_N1 + BETOPTION_INTERVAL;
    public static final int BETOPTION_P2 = BETOPTION_N2 + BETOPTION_INTERVAL;
    public static final int BETOPTION_P3 = BETOPTION_N3 + BETOPTION_INTERVAL;
    public static final int BETOPTION_P4 = BETOPTION_N4 + BETOPTION_INTERVAL;
    public static final int BETOPTION_P5 = BETOPTION_N5 + BETOPTION_INTERVAL;

    /**
     * Check whether the bet option of this entry is PX.
     * 
     * @return true if PX, false if NX
     */
    public boolean isP() {
        if (this.getBetOption() > BETOPTION_INTERVAL) {
            return true;
        }
        return false;
    }
}
