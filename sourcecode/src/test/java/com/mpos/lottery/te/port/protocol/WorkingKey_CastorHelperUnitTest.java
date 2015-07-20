package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.junit.Test;

public class WorkingKey_CastorHelperUnitTest {

    @Test
    public void testUnmarshal() throws Exception {
        WorkingKey workingKey = new WorkingKey();
        workingKey.setGpeId("GPE-111");
        workingKey.setDataKey("W0JAOTVjMDgzMTU1YzZiYmEtMTI5OC00");
        workingKey.setMacKey("XNhO+/p2lZgRhwMqIZvel9YRFaSvkz6e1L/ar9mf+4J0a5EzLCq3sehTUOJ");

        String xml = CastorHelper.marshal(workingKey, "xml-mapping/GetWorkingKey_Res.xml");;
        System.out.println(xml);
    }

}
