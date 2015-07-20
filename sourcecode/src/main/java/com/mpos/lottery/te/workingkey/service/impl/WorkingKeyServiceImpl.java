package com.mpos.lottery.te.workingkey.service.impl;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.encrypt.HMacMd5Cipher;
import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.workingkey.dao.WorkingKeyDao;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;
import com.mpos.lottery.te.workingkey.service.WorkingKeyService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WorkingKeyServiceImpl implements WorkingKeyService {
    private Log logger = LogFactory.getLog(WorkingKeyServiceImpl.class);
    private WorkingKeyDao workingKeyDao;
    private BaseJpaDao gpeDao;
    private UUIDService uuidManager;

    /**
     * Fetch working key according to the GPE and current date, if no, generate it. DataKey is a 3DES secret key, which
     * size is 24 bytes, and will be converted into base64 string. Also MACKey is HMacMd5 secret key, which size is 24
     * bytes, and will be converted into base64 string.
     * 
     * NOTE: must announced this service as <code>synchronized</code>, otherwise may generate multiple GPE keys for
     * single GPE client of same day due to un-repeated read.
     * 
     * @see WorkingKeyService#fetchWorkingKey(String)
     */
    public synchronized WorkingKey fetchWorkingKey(String gpeId) throws ApplicationException {
        String createDateStr = WorkingKey.getCurrentDateStr();
        // check whether the GPE exist
        this.getGpe(gpeId);
        WorkingKey key = this.getWorkingKeyDao().getWorkingKey(createDateStr, gpeId);
        if (key == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("can NOT find WorkingKey(gpeId=" + gpeId + ",createDateStr=" + createDateStr
                        + "), start to generate it.");
            }
            // generate working key
            key = this.generateWorkingKey(gpeId);
            key.setCreateDateStr(createDateStr);
            this.getWorkingKeyDao().insert(key);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Got working key for GPE(id=" + gpeId + ") : <DataKey>" + key.getDataKey()
                    + "</DataKey><MACKey>" + key.getMacKey() + "</MACKey>");
        }
        // just return it.
        return key;
    }

    /**
     * @see WorkingKeyService#getGpe(String).
     */
    public Gpe getGpe(String id) throws ApplicationException {
        Gpe gpe = this.getGpeDao().findById(Gpe.class, id);
        if (gpe == null) {
            throw new ApplicationException(SystemException.CODE_GPE_NOTEXIST, "can NOT find GPE(id=" + id + ").");
        }
        return gpe;
    }

    @Override
    public void checkAlive() throws ApplicationException {
        try {
            this.getGpeDao().all(Gpe.class);
        } catch (Exception e) {
            logger.warn(e);
            throw new ApplicationException(SystemException.CODE_INTERNAL_SERVER_ERROR, "internal error");
        }
    }

    // --------------------------------------------------
    // PRIVATE METHODS
    // --------------------------------------------------

    /**
     * Generate a new working key for GPE.
     */
    private WorkingKey generateWorkingKey(String gpeId) {
        WorkingKey key = new WorkingKey();
        try {
            String macKey = HMacMd5Cipher.generateMACKey();
            String dataKey = TriperDESCipher.generateDataKey();
            key.setId(this.getUuidManager().getGeneralID());
            key.setGpeId(gpeId);
            key.setDataKey(dataKey);
            key.setMacKey(macKey);
            return key;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    // --------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------
    public WorkingKeyDao getWorkingKeyDao() {
        return workingKeyDao;
    }

    public void setWorkingKeyDao(WorkingKeyDao workingKeyDao) {
        this.workingKeyDao = workingKeyDao;
    }

    public BaseJpaDao getGpeDao() {
        return gpeDao;
    }

    public void setGpeDao(BaseJpaDao gpeDao) {
        this.gpeDao = gpeDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

}
