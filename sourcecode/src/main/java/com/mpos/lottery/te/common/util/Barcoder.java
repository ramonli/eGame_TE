package com.mpos.lottery.te.common.util;

import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.config.exception.SystemException;

/**
 * To generate QR barcode. The algorithm of generating barcode is as below:
 * 
 * <pre>
 * Base64(3DES(input + DES_Key))
 * </pre>
 * 
 * For example, to generate barcode of ticket, the <code>input</code> will be the serialNo, however for daily summary
 * report, the input will be '${operatorID},${startTime},${endTime}'. It is better to know the input format at where the
 * barcode is generated.
 * 
 * @author Ramon
 */
public class Barcoder {
    // the 3DES key in BASE64 representation.
    private String desKey = "W0JAMWQ1MGZkMjc2N2U2M2Y2LWVkYTIt";
    private int sizeOfGameType = 2;

    private int gameType;
    private String serialNo;
    private String barcode;

    /**
     * Encode the serial number of ticket.
     * 
     * @param input
     *            THe source to generate barcode, such as serial number.
     * @param gameType
     *            the game type of ticket.
     */
    public Barcoder(int gameType, String input) {
        if (input == null) {
            throw new IllegalArgumentException("argument 'input' can not be null");
        }
        this.gameType = gameType;
        this.serialNo = input;

        try {
            this.barcode = SimpleToolkit.fillLeft(sizeOfGameType, '0', gameType + "")
                    + TriperDESCipher.encrypt(desKey, this.desKey + input, TriperDESCipher.STR_IV);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // public Barcoder(String desBase64Key) {
    // this.desKey = desBase64Key;
    // }

    /**
     * Decode the barcode of ticket.
     * 
     * @param barcode
     *            THe generated barcode in base64 representation.
     */
    public Barcoder(String barcode) {
        if (barcode == null) {
            throw new IllegalArgumentException("argument 'barcode' can not be null");
        }
        this.barcode = barcode;

        try {
            this.gameType = Integer.parseInt(barcode.substring(0, sizeOfGameType));
            String source = TriperDESCipher.decrypt(this.desKey, barcode.substring(sizeOfGameType),
                    TriperDESCipher.STR_IV);
            this.serialNo = source.substring(this.desKey.length());
        } catch (Exception e) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SERIALNO, e.getMessage());
        }
    }

    public int getGameType() {
        return gameType;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getBarcode() {
        return barcode;
    }

}
