package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.mpos.lottery.te.common.encrypt.HMacMd5Cipher;
import com.mpos.lottery.te.common.util.HexCoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class EGameValidationStrategy extends AbstractValidationStrategy {
    private static Log logger = LogFactory.getLog(EGameValidationStrategy.class);

    /**
     * @see ValidationStrategy#validate(InstantTicket, String, boolean).
     */
    @Override
    public PrizeLevelDto validate(InstantTicket hostTicket, String virn, boolean isEnquiry) throws ApplicationException {
        String prizeLogicId = hostTicket.getGameDraw().getPrizeLogicId();
        // retrieve all prize level definition, no matter cash/object prize.
        List<com.mpos.lottery.te.gamespec.prize.PrizeLevel> prizeLevelDefs = this.getPrizeLevelDao().findByPrizeLogic(
                prizeLogicId);
        if (prizeLevelDefs.size() <= 0) {
            throw new SystemException("No prize level defined for IG " + "game instance(id="
                    + hostTicket.getGameDraw().getId() + ")");
        }
        int numberOfPrizeLevels = prizeLevelDefs.size();
        // calculate prize level
        // XOR is a hex string
        String xor = getXOR(hostTicket.getTicketXOR1(), hostTicket.getTicketXOR2(), virn);
        // check whether the MD5 is matched
        String hexMD5 = SimpleToolkit.md5(xor);
        if (!hexMD5.equalsIgnoreCase(hostTicket.getXorMD5())) {
            // if (!isEnquiry)
            checkValidationRetry(hostTicket);
            throw new ApplicationException(SystemException.CODE_XORMD5_NOTMATCH, "The MD5 of XOR(" + xor
                    + ") of ticket(serialNO=" + hostTicket.getSerialNo() + " is " + hexMD5 + ", expect:"
                    + hostTicket.getXorMD5());
        }

        int prizeLevel = calculatePrizeLevel(hostTicket.getRawSerialNo(), hostTicket.getTicketMAC(), xor,
                hostTicket.getPrizeLevelIndex(), numberOfPrizeLevels);
        if (logger.isDebugEnabled()) {
            logger.debug("The prize level for ticket(serialNo=" + hostTicket.getSerialNo() + ",ticketMac="
                    + hostTicket.getTicketMAC() + ",XOR=" + xor + ",prizeLevelIndex=" + hostTicket.getPrizeLevelIndex()
                    + ",totalPrizeLevel=" + numberOfPrizeLevels + ") is " + prizeLevel + ".");
        }

        // new a instance
        PrizeLevelDto prizeLevelDef = null;
        for (com.mpos.lottery.te.gamespec.prize.PrizeLevel pl : prizeLevelDefs) {
            if (prizeLevel == pl.getPrizeLevel()) {
                prizeLevelDef = new PrizeLevelDto(pl);
                break;
            }
        }
        if (prizeLevelDef == null) {
            // count the retry transaction
            // checkValidationRetry(hostTicket);
            throw new SystemException("No prize level #" + prizeLevel + " defined for IG " + "game instance(id="
                    + hostTicket.getGameDraw().getId() + ")");
        }
        return prizeLevelDef;
    }

    public static String getXOR(String xor1, String xor2, String virn) {
        StringBuffer xor = new StringBuffer();
        xor.append(xor1 == null ? "" : xor1.trim());
        xor.append(virn == null ? "" : virn.trim());
        xor.append(xor2 == null ? "" : xor2.trim());
        return xor.toString();
    }

    /**
     * Extract prize level from ticket mac-string, xor-string, prize level index, mac-string, xor-string, prize-level
     * index will be generated when physical ticket generation phase. About the algorithm, please refer to
     * "document/Function Specification/Instant LottoTicket Serial Generation Logic.doc"
     * 
     * @param rawSerialNo
     *            The serial number of instant ticket.
     * @param ticketMac
     *            The mac-string of ticket, a hex string.
     * @param xor
     *            The xor-String
     * @param prizeLevelIndex
     *            The index of prize level in hash byte array.
     * @param totalPrizeLevel
     *            How many prize level for a game instance.
     * @return the prize level if win a prize, or exception will be thrown out.
     */
    public static int calculatePrizeLevel(String rawSerialNo, String ticketMac, String xor, int prizeLevelIndex,
            int totalPrizeLevel) {
        // due to all character is ASCII(0-127), and all encoding is compatible
        // with ascii,
        // we don't need to specify the 'encoding' for getByte method.
        byte[] xorByte = HexCoder.hexToBuffer(xor);
        byte[] serialNoByte = rawSerialNo.getBytes();
        if (xorByte.length != serialNoByte.length) {
            throw new SystemException("The length of xor bytearray(hex:" + xor + ") is " + xorByte.length
                    + ", and the length of serial-number(" + rawSerialNo + ") bytearray is " + serialNoByte.length
                    + ", they are no equivalent.");
        }
        // XOR serial-number with XOR byte array
        for (int i = 0; i < xorByte.length; i++) {
            serialNoByte[i] ^= xorByte[i]; // use serialNoByte as temporary byte
                                           // array.
        }
        // decode hex MAC-string
        byte[] macKeyByte = HexCoder.hexToBuffer(ticketMac);
        try {
            // HMACMD5 Byte(tmp) with the key Byte(mac)
            byte[] hashByte = HMacMd5Cipher.doDigest(serialNoByte, macKeyByte);
            int prizeLevel = hashByte[prizeLevelIndex] % totalPrizeLevel;
            return prizeLevel;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    public static void main(String args[]) {
        if (args.length != 7) {
            System.out.println("Usage: ig_validation [ticket_serial_no] [ticket_mac] [xor1] [xor2] [virn] "
                    + "[n_index] [count_of_total_prize_level]");
            System.out.println("\t if either xor1 or xor2 is null, use '_' as placeholder. ");
            System.exit(0);
        }

        System.out.println(">>> Input Parameters:");
        System.out.println("\t ticket_serial_no: " + args[0]);
        System.out.println("\t ticket_mac: " + args[1]);
        System.out.println("\t xor1: " + (args[2].equals("_") ? null : args[2]));
        System.out.println("\t xor2: " + (args[3].equals("_") ? null : args[3]));
        System.out.println("\t virn: " + args[4]);
        System.out.println("\t n_index: " + args[5]);
        System.out.println("\t count_of_total_prize_level:" + args[6]);
        System.out.println("");
        String xor = EGameValidationStrategy.getXOR(args[2].equals("_") ? null : args[2], args[3].equals("_")
                ? null
                : args[3], args[4]);
        int prizeLevel = EGameValidationStrategy.calculatePrizeLevel(args[0], args[1], xor, Integer.parseInt(args[5]),
                Integer.parseInt(args[6]));
        System.out.println(">>> [TICKET:" + args[0] + "] wins prize level: " + prizeLevel);
    }
}
