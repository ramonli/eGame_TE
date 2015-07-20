package com.mpos.lottery.te.workingkey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.junit.Test;

public class GetWorkingKeyAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testGetWorkingKey() throws Exception {
        WorkingKey key = (WorkingKey) this.prepare(this.mockRequestContext()).getModel();
        assertNotNull(key);
        assertNotNull(key.getDataKey());
        assertNotNull(key.getMacKey());

        // If request working key multiple times, the returned working key
        // should be identical.
        WorkingKey key2 = (WorkingKey) this.prepare(this.mockRequestContext()).getModel();
        assertEquals(key.getDataKey(), key2.getDataKey());
        assertEquals(key.getMacKey(), key2.getMacKey());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        // silently ignore
    }

}
