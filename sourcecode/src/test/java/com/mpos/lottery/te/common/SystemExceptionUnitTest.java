package com.mpos.lottery.te.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.config.exception.SystemException;

import org.junit.Test;

public class SystemExceptionUnitTest {

    @Test
    public void testGetErrorCode() {
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            SystemException se = new SystemException(201, new String[] { "X-timestamp" }, e);
            System.out.println("Message: " + se.getMessage());
            System.out.println("Cause Message: " + se.getCause().getMessage());
            assertNotNull(se.getMessage());
            assertEquals(ArithmeticException.class, se.getCause().getClass());
            // assertEquals(new String[]{"X-timestamp"}, se.getMetaData());
        }

    }
}
