package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.common.encrypt.HMacMd5Cipher;
import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.MessageFormatException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProtocolSerializer {
    private Log logger = LogFactory.getLog(ProtocolSerializer.class);

    private MLotteryContext prop = MLotteryContext.getInstance();

    /**
     * Serialize response, and then do MAC and DES encryption.
     */
    public void serialize(Context responseCtx) {
        try {
            if (responseCtx.getModel() != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Start to serialize model("
                            + responseCtx.getModel().getClass()
                            + "),mappingFile="
                            + getMappingFile(responseCtx.getTransType(), responseCtx.getProtocalVersion(),
                                    responseCtx.getGameTypeId()));
                }
                String xml = CastorHelper.marshal(
                        responseCtx.getModel(),
                        getMappingFile(responseCtx.getTransType(), responseCtx.getProtocalVersion(),
                                responseCtx.getGameTypeId()));
                responseCtx.setOriginalBody(xml);
            }
            // if (responseCtx.getTransType() !=
            // TransactionType.GET_WORKING_KEY.getResponseType())
            if (responseCtx.getTransType() != TransactionType.GET_WORKING_KEY.getResponseType()
                    && responseCtx.getTransType() != TransactionType.CHECK_ALIVE.getResponseType()) {
                this.macAndEncrypt(responseCtx);
            } else {
                // only encryption message body will be write out.
                responseCtx.setEncrptedBody(responseCtx.getOriginalBody());
            }
            // if (logger.isDebugEnabled()){
            // logger.debug("Finish serializing response context(" +
            // responseCtx.getMacString() + ").");
            // }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SystemException(e);
        }
    }

    /**
     * Decrypt messagebody, verify MAC, and then deserialize message body.
     */
    public void deserialize(Context requestCtx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Start to deserialize request context(" + requestCtx.getMacString() + ").");
        }
        if (requestCtx.getTransType() != TransactionType.GET_WORKING_KEY.getRequestType()
                && requestCtx.getTransType() != TransactionType.CHECK_ALIVE.getRequestType()) {
            this.decryptAndMac(requestCtx);
        }
        try {
            if (requestCtx.getEncrptedBody() != null && !"".equals(requestCtx.getEncrptedBody())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Start to deserialize model: model="
                            + requestCtx.getModel()
                            + ",mappingFile="
                            + getMappingFile(requestCtx.getTransType(), requestCtx.getProtocalVersion(),
                                    requestCtx.getGameTypeIdIntValue() + ""));
                }
                Object model = CastorHelper.unmarshal(
                        requestCtx.getOriginalBody(),
                        this.getMappingFile(requestCtx.getTransType(), requestCtx.getProtocalVersion(),
                                requestCtx.getGameTypeId()));
                requestCtx.setModel(model);
            }
        } catch (Exception e) {
            throw new MessageFormatException(SystemException.CODE_WRONG_MESSAGEBODY,
                    "Fail to parse message body(mappingFile="
                            + this.getMappingFile(requestCtx.getTransType(), requestCtx.getProtocalVersion(),
                                    requestCtx.getGameTypeId()) + "):" + requestCtx.getOriginalBody(), e);
        }
    }

    /**
     * Do MAC and DES encryption on response
     */
    public void macAndEncrypt(Context responseCtx) throws Exception {
        WorkingKey key = responseCtx.getWorkingKey();
        if (key != null) {
            // do mac
            String macString = responseCtx.getMacString();
            String mac = HMacMd5Cipher.doDigest(macString, key.getMacKey());
            if (logger.isDebugEnabled()) {
                logger.debug("Finish mac(macKey=" + key.getMacKey() + ",macString=" + macString + "):" + mac);
            }
            responseCtx.setMac(mac);
            if (responseCtx.getModel() != null) {
                // des encryption
                String encrypt = TriperDESCipher.encrypt(key.getDataKey(), responseCtx.getOriginalBody(),
                        prop.getTriperDesIV());
                responseCtx.setEncrptedBody(encrypt);
            }
        } else {
            // logger.warn("No working key ")
        }
    }

    /**
     * decrypt message body and verify MAC
     */
    public void decryptAndMac(Context requestCtx) {
        WorkingKey key = requestCtx.getWorkingKey();
        String digest = null;
        try {
            if (requestCtx.getEncrptedBody() != null) {
                String originalBody = TriperDESCipher.decrypt(key.getDataKey(), requestCtx.getEncrptedBody(),
                        prop.getTriperDesIV());
                if (logger.isDebugEnabled()) {
                    logger.debug("Finish decrypting cipher into original xml:" + originalBody);
                }
                requestCtx.setOriginalBody(originalBody);
            }
            String macString = requestCtx.getMacString().trim();
            digest = HMacMd5Cipher.doDigest(macString, key.getMacKey());
            if (logger.isDebugEnabled()) {
                logger.debug("Start to do MAC(key=" + key.getMacKey() + ") on (" + macString + ",hash="
                        + macString.hashCode() + "), get digest:" + digest);
            }
        } catch (Exception e) {
            throw new SystemException(SystemException.CODE_FAILTO_DECRYPT,
                    "Fail to decrypt message body(traceMesageId=" + requestCtx.getTraceMessageId() + ",terminalId="
                            + requestCtx.getTerminalId() + ").", e);
        }
        // verify mac
        if (!digest.equalsIgnoreCase(requestCtx.getMac())) {
            throw new MessageFormatException(SystemException.CODE_UNMATCHED_MAC, "Excepted mac:" + digest + ", but:"
                    + requestCtx.getMac());
        }
    }

    // ---------------------------------------
    // PRIVATE METHODS
    // ---------------------------------------

    protected String getMappingFile(int transType, String protocolVersion, String gameTypeId) {
        return prop.getMappingFile(transType, protocolVersion, gameTypeId);
    }
}
