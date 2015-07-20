package com.mpos.lottery.te.gameimpl.lotto.draw;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "LOTTO_OPERATION_PARAMETERS")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "LOTTO_OP_PARAMETERS_ID")) })
public class LottoOperationParameter extends BaseOperationParameter {
    private static final long serialVersionUID = 49141039538703791L;

    @Column(name = "MULTIPLE_COUNT")
    private int maxMultipleCount;

    @Column(name = "ALLOW_OFFLINE")
    private boolean isAllowOfflineSale;

    @Column(name = "OFFLINE_UPLOAD_DEADLINE")
    private int offlineUploadDeadline;

    public boolean isAllowOfflineSale() {
        return isAllowOfflineSale;
    }

    public void setAllowOfflineSale(boolean isAllowOfflineSale) {
        this.isAllowOfflineSale = isAllowOfflineSale;
    }

    public int getOfflineUploadDeadline() {
        return offlineUploadDeadline;
    }

    public void setOfflineUploadDeadline(int offlineUploadDeadline) {
        this.offlineUploadDeadline = offlineUploadDeadline;
    }

    public int getMaxMultipleCount() {
        return maxMultipleCount;
    }

    public void setMaxMultipleCount(int maxMultipleCount) {
        this.maxMultipleCount = maxMultipleCount;
    }

}
