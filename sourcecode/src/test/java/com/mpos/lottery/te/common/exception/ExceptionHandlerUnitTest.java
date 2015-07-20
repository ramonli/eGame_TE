package com.mpos.lottery.te.common.exception;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ExceptionHandler;
import com.mpos.lottery.te.config.exception.SystemException;

import org.junit.Test;

public class ExceptionHandlerUnitTest {

    @Test
    public void testGetErrorMessage() {
        MLotteryContext prop = MLotteryContext.getInstance();
        try {
            int a = 1 / 0;
        } catch (Exception e) {
            SystemException se = new SystemException(214, new Object[] { new Double(1.1), new Double(1.0) }, e);
            ExceptionHandler eh = new ExceptionHandler(se);
            // System.out.println(eh.getErrorMessage());
            assertEquals(214, eh.getErrorCode());
        }
    }

}
