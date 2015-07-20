package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.common.util.HexCoder;

import net.mpos.apc.entry.GetAccountInfo.ReqGetAccountInfo;

import org.junit.Test;

public class ReqGetAccountInfoUnitTest {

    @Test
    public void testDecode() throws Exception {
        String hexInput = "dc9b57d196dd38a2fd5da216c829fd1c";
        byte[] raw = TriperDESCipher.decrypt(Base64Coder.decode("W0JAMTZkYWRmOTU3MTUwYWQxLWNkMTEt"),
                HexCoder.hexToBuffer(hexInput), TriperDESCipher.IV);
        ReqGetAccountInfo accountInfo = ReqGetAccountInfo.newBuilder().mergeFrom(raw).build();
        System.out.println(accountInfo);
        assertEquals(0, accountInfo.getFreeRequest());
        assertEquals("98800138000", accountInfo.getMobileNo());
    }
}
