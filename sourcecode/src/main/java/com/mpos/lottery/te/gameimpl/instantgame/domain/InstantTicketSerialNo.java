package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;

import java.util.regex.Pattern;

public class InstantTicketSerialNo {
    // GGG+BBB is book number
    private String GGG; // it is game instance name
    private String BBB; // it is the book sequence
    private String index; // a increment sequence.

    /**
     * The data format of serialNO should be 'GGGBBBBBBTTT'.
     */
    public InstantTicketSerialNo(String serialNo) throws ApplicationException {
        String serialNoFormat = MLotteryContext.getInstance().getInstantTicketSerialNoFormat();
        Pattern p = Pattern.compile(serialNoFormat);
        if (!p.matcher(serialNo).matches()) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_CRITERIA,
                    "Wrong format of instant ticket serial number(" + serialNo + ").");
        }
        this.GGG = serialNo.substring(0, 3);
        this.BBB = serialNo.substring(3, 9);
        this.index = serialNo.substring(9);
    }

    public String getGGG() {
        return GGG;
    }

    public String getBBB() {
        return BBB;
    }

    public String getIndex() {
        return index;
    }

    public long getLongIndex() {
        return Long.parseLong(this.index);
    }

    public long getLongGGG() {
        return Long.parseLong(this.GGG);
    }

    public long getLongBBB() {
        return Long.parseLong(this.BBB);
    }

    public String getBookNumber() {
        return this.getGGG() + this.getBBB();
    }

    public String getSerialNo() {
        return new StringBuffer().append(this.getGGG()).append(this.getBBB()).append(this.getIndex()).toString();
    }

    public static String getStringIndex(long index) {
        String tmp = "000" + index;
        tmp = tmp.substring(tmp.length() - 3);
        return tmp;
    }
}
