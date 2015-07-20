package com.mpos.lottery.te.workingkey.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

public interface WorkingKeyService {

    /**
     * Everyday client(GPE) needs to get working key before issuing any other requests. After get working key, client
     * can get DataKey and MACKey which will be used to encrypted transaction. Or Te will reject all requests from this
     * client. If client issue 'Get working key' multiple times, the working key generated at first time will be return.
     * 
     * @param gpeId
     *            gpeId The id of GPE. TE will exchange working key with this GPE.
     */
    WorkingKey fetchWorkingKey(String gpeId) throws ApplicationException;

    /**
     * Find Gpe by id.
     * 
     * @param id
     *            The identifier of Gpe.
     */
    Gpe getGpe(String id) throws ApplicationException;

    void checkAlive() throws ApplicationException;
}
