package com.mpos.lottery.te.port;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.perf4j.StopWatch;

public class InternalTEPortServlet extends TEPortServlet {
    private static final long serialVersionUID = 4952687368857612962L;

    /**
     * Use a pre-defined working key for internal system integration.
     */
    @Override
    protected void assembleWorkingKey(Context requestCtx, StopWatch watch) {
        if (requestCtx.getTransType() == TransactionType.GET_WORKING_KEY.getRequestType()) {
            throw new SystemException("No transaction(Get Working Key) allowed in internal integration mode");
        }

        MLotteryContext prop = MLotteryContext.getInstance();
        WorkingKey workingKey = new WorkingKey();
        workingKey.setDataKey(prop.get("te.datakey"));
        workingKey.setMacKey(prop.get("te.mackey"));

        // assemble GPE too
        BaseJpaDao baseDao = (BaseJpaDao) prop.getBeanFactory().getBean(prop.get(MLotteryContext.ENTRY_BEAN_BASEDAO));
        Gpe gpe = baseDao.findById(Gpe.class, requestCtx.getGpe().getId());
        if (gpe == null) {
            throw new SystemException("No Gpe found by id:" + requestCtx.getGpe().getId());
        }
        requestCtx.setGpe(gpe);

        requestCtx.setWorkingKey(workingKey);
        requestCtx.setInternalCall(true);
    }

    @Override
    protected void printCopyright(String contextPath) {
        // No need to print copyright information repeatedly
    }

    @Override
    protected void registerMBeans() {
        // Don't register MBean repeatedly
    }
}
