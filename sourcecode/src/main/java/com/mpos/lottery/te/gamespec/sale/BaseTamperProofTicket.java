package com.mpos.lottery.te.gamespec.sale;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A base ticket which will introduce extra means to protect ticket information from tampering.
 * 
 * @author Ramon Li
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseTamperProofTicket extends BaseTicket {

    @Column(name = "EXTEND_TEXT")
    private String extendText;

    @Override
    protected void verifyExtendTxt(List<? extends BaseEntry> actualEntries) throws ApplicationException {
        // verify extendTxt
        List<BaseEntry> baseActualEntries = new LinkedList<BaseEntry>();
        for (BaseEntry entry : actualEntries) {
            baseActualEntries.add(entry);
        }
        String actualExtendTxt = BaseTicket.generateExtendText(baseActualEntries);
        if (!actualExtendTxt.equalsIgnoreCase(this.getExtendText())) {
            throw new ApplicationException(SystemException.CODE_UN_SELECTED_NUMBER_CHANGED,
                    "Selected number of ticket(serialNo=" + this.getSerialNo()
                            + ") can't not be changed, expect extendTxt:" + this.getExtendText()
                            + ", actual extendTxt:" + actualExtendTxt);
        }
    }

    public String getExtendText() {
        return extendText;
    }

    public void setExtendText(String extendText) {
        this.extendText = extendText;
    }
}
