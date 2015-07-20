package com.mpos.lottery.te.common.encrypt;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.util.Base64Coder;

import org.junit.Test;

public class HMacMd5CipherUnitTest {

    @Test
    public void testGenerateMACKey() throws Exception {
        String macKey = HMacMd5Cipher.generateMACKey();
        assertEquals(24, Base64Coder.decode(macKey).length);

        String macKey2 = HMacMd5Cipher.generateMACKey();
        assertEquals(false, macKey.equals(macKey2));
    }

    @Test
    public void testDoDigest() throws Exception {
        String macKey = "tGoqdCZWHyqhrvKby5BDrC2RwvKzgFbd";
        // String macKey = HMacMd5Cipher.generateMACKey();
        String input = "Protocal-Version:1.0|GPE_Id:GPE-111|Terminal-Id:111|TractMsg-Id:20090420152224|"
                + "Timestamp:20090420152224|Transaction-Type:200|Transaction-Id:|Response-Code:|"
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<LottoTicket multipleDraws=\"1\" totalAmount=\"250.01\" "
                + "PIN=\"123456\"><GameDraw/><Entry selectedNumber=\"1, 2,3,4,5,9\" betOption=\"1\" "
                + "isQuickPick=\"1\"/><Entry selectedNumber=\"1, 2,3,4,5,10\" betOption=\"1\" "
                + "isQuickPick=\"1\"/><Entry selectedNumber=\"1, 2,3,4,5,11\" betOption=\"1\" "
                + "isQuickPick=\"1\"/></LottoTicket>";
        // File file = new File("e:/macString.txt");
        // BufferedReader br = new BufferedReader(new InputStreamReader(new
        // FileInputStream(file)));
        // // String tmp = br.readLine();
        // StringBuffer buffer = new StringBuffer();
        // for (String tmp = br.readLine(); tmp != null; tmp = br.readLine()){
        // buffer.append(tmp);
        // }
        // String input = buffer.toString();
        // System.out.println(input.hashCode() + ":" + input);
        String output1 = HMacMd5Cipher.doDigest(input, macKey);
        String output2 = HMacMd5Cipher.doDigest(input, macKey);
        String output3 = HMacMd5Cipher.doDigest(input, macKey);
        String output4 = HMacMd5Cipher.doDigest(input, macKey);

        assertEquals(output1, output2);
        assertEquals(output2, output3);
        assertEquals(output3, output4);
        System.out.println(output1);
    }
}
