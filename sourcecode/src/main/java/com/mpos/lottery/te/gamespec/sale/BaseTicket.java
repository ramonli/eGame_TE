package com.mpos.lottery.te.gamespec.sale;

import com.google.protobuf.Message;

import com.mpos.lottery.te.common.dao.SettlementEntity;
import com.mpos.lottery.te.common.encrypt.HMacMd5SelectedNumber;
import com.mpos.lottery.te.common.encrypt.KeyStore;
import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.MessageFormatException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.sale.Payment;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.amqp.AmqpMessageUtils;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessage;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessageSerializer;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.User;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * A base ticket definition for all game type.
 * 
 * @author Ramon Li
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseTicket extends SettlementEntity implements Cloneable, TeTransactionMessageSerializer {
    public static final int STATUS_INVALID = 0; // Invalid
    public static final int STATUS_ACCEPTED = 1; // Accepted
    public static final int STATUS_CANCELED = 2; // Canceled
    public static final int STATUS_RETURNED = 3; // Returned
    public static final int STATUS_CANCEL_DECLINED = 4; // cancel declined
    public static final int STATUS_PAID = 5; // Paid

    public static final int TICKET_TYPE_NORMAL = 1;
    public static final int TICKET_TYPE_PRIZE = 2;
    public static final int TICKET_TYPE_INCENTIVE = 3;

    public static final int TICKET_FROM_POS = 1;
    public static final int TICKET_FROM_IBETTING = 2;
    public static final int TICKET_FROM_SGPE = 3;
    public static final int TICKET_FROM_MGPE = 4;
    public static final int TICKET_FROM_WGPESMS = 5;
    public static final int TICKET_FROM_WGPEPOS = 6;
    public static final int TICKET_FROM_TGPE = 9;

    @Column(name = "VERSION")
    private long version;

    /**
     * The type of transaction depends on:
     * <ol>
     * <li>If a ticket is successful(accepted/paid), the transction will be sale transactin.</li>
     * <li>If ticket is cancelled, the transction will be cancel transaction.</li>
     * <li>If ticket is newly generated in payout(multi-draw), the transaction will be payout transction(while the
     * original paid ticket still associates with sale transaction).</li>
     * </ol>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_ID", nullable = false)
    private Transaction transaction;

    @Column(name = "SERIAL_NO")
    private String serialNo; // may be encrypted

    @Transient
    private String rawSerialNo; // retrieve from client request

    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @Column(name = "IS_WINNING")
    private boolean isWinning;

    /**
     * Whether this ticket wins a lucky draw?.
     */
    @Column(name = "IS_WINING_lUCKY_DRAW")
    private boolean isLuckyWinning;

    @Column(name = "LD_WINING_TOTAL_BETS")
    private int countOfLuckyWinning;

    @Column(name = "STATUS")
    private int status = STATUS_ACCEPTED;

    @Column(name = "IS_BLOCK_PAYOUT")
    private boolean isPayoutBlocked;

    @Column(name = "IS_OFFLINE")
    private boolean isOffline;

    // only cancel and confirmPayout
    // will influence this field.
    @Column(name = "IS_COUNT_IN_POOL")
    private boolean isCountInPool = true;

    /* Refer to TICKET_TYPE_XXX */
    @Column(name = "TICKET_TYPE")
    private int ticketType = TICKET_TYPE_NORMAL;
    /* Refer to TICKET_FROM_XXX */
    @Column(name = "TICKET_FROM")
    private int ticketFrom;

    @Column(name = "MUTLI_DRAW")
    private int multipleDraws = 1;

    @Column(name = "PIN")
    private String PIN = "!!!!";

    /**
     * The total bets of this ticket.
     */
    @Column(name = "TOTAL_BETS")
    private int totalBets;

    // the validation cod will be used to make sure only ticket holder can claim
    // prize...actually PIN already can achieved this goal.
    @Column(name = "VALIDATION_CODE")
    private String validationCode;

    @Column(name = "BARCODE")
    private String barcode;

    /* Player's mobile number */
    @Column(name = "MOBILE_NO")
    private String mobile;
    @Column(name = "CREDIT_CARD_NUM")
    private String creditCardSN;
    @Column(name = "USER_ID")
    private String userId;
    /**
     * What kind of transaction marks current ticket's status? Only sale and sale cancellation will affect this field.
     */
    @Column(name = "TRANS_TYPE")
    private int transType = TransactionType.SELL_TICKET.getRequestType();

    /**
     * If a cancel-declined ticket is paid, this field will be set to true.
     */
    @Column(name = "IS_ABSORPTION_PAID")
    private boolean absorptionPaid;

    @Transient
    private List<BaseEntry> entries = new LinkedList<BaseEntry>();
    /**
     * If a multiple-draw ticket, for example 3 draws ticket(draw#1, draw#2, draw#3), this field will be the number of
     * draw#3.
     */
    @Transient
    private String lastDrawNo;
    // THe input channel when payout.
    @Transient
    private int payoutInputChannel;

    @Transient
    private User user;
    /**
     * Is the cancellation manual or auto? only affect when 'cancel by ticket'(201).
     */
    @Transient
    private boolean manualCancel;

    /**
     * Whether the serial number is generated from barcode.
     */
    @Transient
    private boolean serialNoGeneratedFromBarcode;

    /**
     * This is a workaround of JPA's lack of polymorphic association. As <code>BaseGameInstance</code> is a
     * <code>@MappedSuperclass</code>, no entity association can be annotated on it. But we need a unified interface for
     * accessing game instance from <code>BaseTicket</code>, that is why these 2 abstract methods defined:
     * <code>getGameInstance()</code> and <code>setGameInstance()</code>.
     * <p>
     * Refer to
     * <ul>
     * <li>
     * https://hibernate.onjira.com/browse/EJB-199?focusedCommentId=32855&page =com
     * .atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment- 32855</li>
     * <li>
     * http://stackoverflow.com/questions/6853802/jpa-table-per-class-problems -with-manytoone</li>
     * </ul>
     * I do agree with Don Tam's solution.
     */
    public abstract BaseGameInstance getGameInstance();

    public abstract void setGameInstance(BaseGameInstance gameInstance);

    public BigDecimal calculateMultipleDrawAmount() {
        return this.getTotalAmount().divide(new BigDecimal(this.getMultipleDraws()), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Whether player buy this ticket by credit card.
     * 
     * @return true if buy by credit card, otherwise false will be returned.
     */
    public boolean isSoldByCreditCard() {
        if (this.getUser() != null && this.getUser().getCreditCardSN() != null) {
            return true;
        }
        if (this.getCreditCardSN() != null) {
            return true;
        }
        return false;
    }

    /**
     * Update raw serialNo, and encrypt raw serialNo to get serialNo if needed.
     * 
     * @param needToUpdateSerialNo
     *            Need to update serialNo based on raw serialNo??
     * @param rawSerialNo
     *            The raw serialNo.
     */
    public void setRawSerialNo(boolean needToUpdateSerialNo, String rawSerialNo) {
        this.rawSerialNo = rawSerialNo;
        if (needToUpdateSerialNo) {
            this.setSerialNo(encryptSerialNo(rawSerialNo));
        }
    }

    public void setRawSerialNo(String rawSerialNo) {
        this.setRawSerialNo(true, rawSerialNo);
    }

    /**
     * generate a 6 random digital number.
     */
    public static String generateValidationCode() {
        if (MLotteryContext.getInstance().getSysConfiguration().isGenValidationCode()) {
            int pin = SimpleToolkit.generatePin(1000000, 9999999);
            return (pin + "").substring(1);
        } else {
            return "FFFFFF";
        }
    }

    /**
     * Split the total amount among multiple tickets. For example, client buy a 3-draw ticket, and paid with
     * voucher(capitalAmount:1000 + freeAmount:200). As the backend will generate 3 tickets records according, how to
     * determine the total/free amount of each ticket records??
     * <p/>
     * The method will deduct capital amount of voucher first, then free amount. In our example the total amount,
     * including capital and free amount, of each ticket is 400, the for the 1st ticket, the 400 will be deducted from
     * capital amount.
     * <p/>
     * <table border="1">
     * <tr>
     * <td>No. of LottoTicket</td>
     * <td>capitalAmount</td>
     * <td>freeAmount</td>
     * <td>Status Of Voucher</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>400</td>
     * <td>0</td>
     * <td>capitalAmount:600, freeAmount:200</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>400</td>
     * <td>0</td>
     * <td>capitalAmount:600, freeAmount:200</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>200</td>
     * <td>200</td>
     * <td>capitalAmount:0, freeAmount:0</td>
     * </tr>
     * </table>
     * <p/>
     * Absolutely at last the capital amount and free amount of voucher should both be 0.
     * <p/>
     * If buy tickets for multiple recievers, the amount splitting will follow same logic.
     * <p/>
     * While when you call this method, must keep in mind that <code>respPayment</code> shoud be a shared reference
     * among multiple invocations, like below:
     * 
     * <pre>
     * <code>
     * Payment respPayment = new Payment(new BigDecimal("1000"), new BigDecimal("200");
     * BigDecimal requiredAmount = new BigDecimal("400");
     * for (int i = 0; i < tickets.size(); i++){
     * 	  BigDecimal amounts[] = BaseTicket.splitAmount(respPayment, requiredAmount, 
     * 	    i == tickets.size() - 1 ? true : false);
     * 	  tickets.get(i).setTotalAmount(amounts[0]);
     * 	  tickets.get(i).setFreeAmount(amounts[1]);
     * )
     * </code>
     * </pre>
     * 
     * @param respPayment
     *            The total amount which will be splitted, and following components must not be null: capitalAmount,
     *            freeAmount
     * @param requiredAmount
     *            The required amount. Every splitting must return the required amount.
     * @param isLast
     *            Is this split is the last? If true, then capital/free amount of <code>respPayment</code> must be 0.
     * @return a 2 length array of amount. The 1st will be capital amount, and the 2nd will be free amount.
     * @throws ApplicationException
     *             CODE_EVENLY_DIVIDED_BASE_AMOUNT
     */
    public static BigDecimal[] splitAmount(Payment respPayment, BigDecimal requiredAmount, boolean isLast)
            throws ApplicationException {
        BigDecimal[] amounts = new BigDecimal[] { new BigDecimal("0"), new BigDecimal("0") };
        if (respPayment.getCapitalAmount().compareTo(requiredAmount) >= 0) {
            amounts[0] = requiredAmount;
            respPayment.setCapitalAmount(respPayment.getCapitalAmount().subtract(requiredAmount));
        } else {
            amounts[0] = respPayment.getCapitalAmount();
            amounts[1] = requiredAmount.subtract(respPayment.getCapitalAmount());
            respPayment.setCapitalAmount(new BigDecimal("0"));
            respPayment.setFreeAmount(respPayment.getFreeAmount().subtract(amounts[1]));
        }
        if (isLast) {
            if (respPayment.getCapitalAmount().compareTo(new BigDecimal("0")) != 0
                    || respPayment.getFreeAmount().compareTo(new BigDecimal("0")) != 0) {
                throw new ApplicationException(SystemException.CODE_EVENLY_DIVIDED_BASE_AMOUNT,
                        "The reqiured amount is " + requiredAmount
                                + ", while after last splitting, there is still some amount "
                                + "remained(capitalAmount:" + respPayment.getCapitalAmount() + ",freeAmount:"
                                + respPayment.getFreeAmount() + ").");
            }
        }
        return amounts;
    }

    /**
     * Generate the HMAC of all entries of a ticket.
     */
    public static String generateExtendText(List<BaseEntry> entries) {
        String hmacInput = "";
        /**
         * No matter what is the sorting criteria, just make it follows a given criteria.
         */
        Collections.sort(entries, new Comparator<BaseEntry>() {

            @Override
            public int compare(BaseEntry o1, BaseEntry o2) {
                return o1.getSelectNumber().compareTo(o2.getSelectNumber());
            }

        });
        for (BaseEntry entry : entries) {
            hmacInput += entry.getSelectNumber();
        }
        return HMacMd5SelectedNumber.doDigestBySelectedNumbers(hmacInput);
    }

    /**
     * Whether a ticket is ready for payout.
     */
    public void allowPayout(Context respCtx, BaseTicket clientTicket, boolean isPrizeEnquiry,
            List<? extends BaseEntry> actualEntries) throws ApplicationException {
        // check whether ticket is payout blocked
        if (this.isPayoutBlocked()) {
            throw new ApplicationException(SystemException.CODE_TICKET_BLOCKPAYOUT, "Ticket(serialNo="
                    + this.getSerialNo() + ") has been blocked for payout.");
        }
        if (!clientTicket.isSerialNoGeneratedFromBarcode() && clientTicket.getValidationCode() == null) {
            throw new MessageFormatException(SystemException.CODE_WRONG_MESSAGEBODY,
                    "THe validation code must be provided.");
        }
        if (!clientTicket.isSerialNoGeneratedFromBarcode()) {
            if (!clientTicket.getValidationCode().equalsIgnoreCase(this.getValidationCode())) {
                throw new ApplicationException(SystemException.CODE_UNMATCHED_PIN,
                        "Unmatched validation code of ticket(serialNo=" + this.getSerialNo() + "), expect["
                                + this.getValidationCode() + "], client provide:" + clientTicket.getValidationCode());
            }
        }
        if (!isPrizeEnquiry) {
            if (respCtx.isInternalCall()) {
                // If internal call, only cancel-declined ticket is allowed for
                // this operation
                if (STATUS_CANCEL_DECLINED != this.getStatus()) {
                    throw new ApplicationException(SystemException.CODE_INVALID_PAYOUT, "Status of ticket(serialNo="
                            + this.getSerialNo() + ") is " + this.getStatus()
                            + ", can't be paid internally, only cancel-declined(" + BaseTicket.STATUS_CANCEL_DECLINED
                            + ") ticket allowed.");
                }
            } else {
                // check if this ticket is accepted
                if (STATUS_ACCEPTED != this.getStatus()) {
                    throw new ApplicationException(SystemException.CODE_INVALID_PAYOUT, "Status of ticket(serialNo="
                            + this.getSerialNo() + ") is " + this.getStatus() + ", can't be paid, only accepted("
                            + BaseTicket.STATUS_ACCEPTED + ") ticket allowed.");
                }
            }

            // need to check PIN??
            if (!this.getPIN().equals(MLotteryContext.getInstance().getIgnoredPIN())) {
                // md5 digest ticket.PIN
                String clientPin = SimpleToolkit.md5(clientTicket.getPIN());
                if (!clientPin.equals(this.getPIN())) {
                    throw new ApplicationException(SystemException.CODE_UNMATCHED_PIN, "Unmatched ticket 'PIN':"
                            + clientPin + ", expected:" + this.getPIN());
                }
            }
            if (this instanceof BaseTamperProofTicket) {
                this.verifyExtendTxt(actualEntries);
            }
        }
    }

    /**
     * Verify whether the MAC of ticket is matched when payout.
     */
    protected void verifyExtendTxt(List<? extends BaseEntry> actualEntries) throws ApplicationException {
        // template for sub-implementation
    }

    /**
     * Encrypt ticket serial number.
     */
    public static String encryptSerialNo(String plainSerialNO) {
        MLotteryContext context = MLotteryContext.getInstance();
        SysConfiguration sysConf = context.getSysConfiguration();
        if (sysConf.isEncryptSerialNo()) {
            if (sysConf.getSerialNoPublicKeyPath() == null) {
                throw new SystemException("can't find 'serialno_public_key_path' in sys-configuration.");
            }
            try {
                // retrieve DES key
                byte[] desKey = KeyStore.readKey(new File(sysConf.getSerialNoPublicKeyPath()),
                        new File(context.get("des.key")));
                // encrypt serial number by 3DES, and encoded into base64
                return new String(Base64Coder.encode(TriperDESCipher.encrypt(desKey, plainSerialNO.getBytes(),
                        TriperDESCipher.IV)));
            } catch (Exception e) {
                throw new SystemException(e);
            }
        } else {
            return plainSerialNO;
        }
    }

    /**
     * Decrypt ticket serial number.
     */
    public static String descryptSerialNo(String serialNo) {
        MLotteryContext context = MLotteryContext.getInstance();
        if (context.getSysConfiguration().isEncryptSerialNo()) {
            try {
                byte[] desKey = KeyStore.readKey(new File(context.getSysConfiguration().getSerialNoPublicKeyPath()),
                        new File(context.get("des.key")));
                String raw = new String(TriperDESCipher.decrypt(desKey, Base64Coder.decode(serialNo),
                        TriperDESCipher.IV));
                // if (logger.isDebugEnabled())
                // logger.debug("Decrypt serialNo(" + serialNo +") into (" + raw
                // + ").");
                return raw;
            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
        return serialNo;
    }

    /**
     * Deeply clone a <code>LottoTicket</code>. Here deeply means all entires will be cloned too, while associated
     * transaction and game instance will keep untouched.
     */
    @Override
    public Object clone() {
        try {
            BaseTicket t = (BaseTicket) super.clone();
            // deep clone... while all cloned tickets should associate with same
            // transaction.
            List entries = new LinkedList();
            for (Object entry : t.getEntries()) {
                entries.add(((BaseEntry) entry).clone());
            }
            t.setEntries(entries);
            return t;
        } catch (CloneNotSupportedException e) {
            throw new SystemException(e);
        }
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getRawSerialNo() {
        return rawSerialNo == null ? this.serialNo : this.rawSerialNo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isWinning() {
        return isWinning;
    }

    public void setWinning(boolean isWinning) {
        this.isWinning = isWinning;
    }

    public boolean isLuckyWinning() {
        return isLuckyWinning;
    }

    public void setLuckyWinning(boolean isLuckyWinning) {
        this.isLuckyWinning = isLuckyWinning;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public boolean isPayoutBlocked() {
        return isPayoutBlocked;
    }

    public void setPayoutBlocked(boolean isPayoutBlocked) {
        this.isPayoutBlocked = isPayoutBlocked;
    }

    public int getTicketType() {
        return ticketType;
    }

    public void setTicketType(int ticketType) {
        this.ticketType = ticketType;
    }

    public List<BaseEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BaseEntry> entries) {
        this.entries = entries;
    }

    public String getLastDrawNo() {
        return lastDrawNo;
    }

    public void setLastDrawNo(String lastDrawNo) {
        this.lastDrawNo = lastDrawNo;
    }

    /**
     * Retrieve the number of multiple game instances.
     */
    public int getMultipleDraws() {
        return multipleDraws;
    }

    /**
     * For a single draw ticket, the value of <code>multipleDraw</code> will be 1.
     * <p>
     * If a multple-draws ticket, for example 3-draws, there will be 3 ticket entities, and each of them will associate
     * with a game instance. The <code>multipleDraws</code> of 1st ticket entity which is associated with sold game
     * instance will be 3, while value of <code>multipleDraws</code> of other 2 ticket entities will be 0.
     */
    public void setMultipleDraws(int multipleDraws) {
        this.multipleDraws = multipleDraws;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String pIN) {
        PIN = pIN;
    }

    public int getPayoutInputChannel() {
        return payoutInputChannel;
    }

    public void setPayoutInputChannel(int payoutInputChannel) {
        this.payoutInputChannel = payoutInputChannel;
    }

    public int getTicketFrom() {
        return ticketFrom;
    }

    public void setTicketFrom(int ticketFrom) {
        this.ticketFrom = ticketFrom;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCreditCardSN() {
        return creditCardSN;
    }

    public void setCreditCardSN(String creditCardSN) {
        this.creditCardSN = creditCardSN;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public boolean isManualCancel() {
        return manualCancel;
    }

    public void setManualCancel(boolean manualCancel) {
        this.manualCancel = manualCancel;
    }

    public boolean isCountInPool() {
        return isCountInPool;
    }

    public void setCountInPool(boolean isCountInPool) {
        this.isCountInPool = isCountInPool;
    }

    public boolean isAbsorptionPaid() {
        return absorptionPaid;
    }

    public void setAbsorptionPaid(boolean absorptionPaid) {
        this.absorptionPaid = absorptionPaid;
    }

    // public boolean isOffline() {
    // return isOffline;
    // }
    //
    // public void setOffline(boolean isOffline) {
    // this.isOffline = isOffline;
    // }

    public int getTotalBets() {
        return totalBets;
    }

    public void setTotalBets(int totalBets) {
        this.totalBets = totalBets;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.setBarcode(true, barcode);
    }

    /**
     * Update the barcode, and serialNo as well if need.
     * 
     * @param needToUpdateSerialNo
     *            Whether need to update serialNo based on the provided barcode.
     * @param barcode
     *            the barcode of ticket.
     */
    public void setBarcode(boolean needToUpdateSerialNo, String barcode) {
        this.barcode = barcode;
        if (needToUpdateSerialNo && this.getRawSerialNo() == null) {
            // update both serialNo and rawSerialNo
            Barcoder barcoder = new Barcoder(barcode);
            this.setRawSerialNo(true, barcoder.getSerialNo());
            this.setSerialNoGeneratedFromBarcode(true);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSerialNoGeneratedFromBarcode() {
        return serialNoGeneratedFromBarcode;
    }

    public void setSerialNoGeneratedFromBarcode(boolean serialNoGeneratedFromBarcode) {
        this.serialNoGeneratedFromBarcode = serialNoGeneratedFromBarcode;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public int getCountOfLuckyWinning() {
        return countOfLuckyWinning;
    }

    public void setCountOfLuckyWinning(int countOfLuckyWinning) {
        this.countOfLuckyWinning = countOfLuckyWinning;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    /**
     * Give a string representation of instance.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(ID:").append(this.getId()).append(",TRANS_ID:")
                .append(this.getTransaction() == null ? "null" : this.getTransaction().getId());
        buffer.append(",SERIAL_NO:").append(this.getSerialNo()).append(",TOTAL_AMOUNT:").append(this.getTotalAmount());
        buffer.append(",STATUS:").append(this.getStatus()).append(",TRAN.GAME_ID:")
                .append(this.getTransaction() == null ? "null" : this.getTransaction().getGameId());
        buffer.append(",DRAW_NO:").append(this.getGameInstance() == null ? "null" : this.getGameInstance().getNumber());
        buffer.append(",GAMEDRAW.GAME_ID:")
                .append(this.getGameInstance() == null ? "null" : this.getGameInstance().getGame().getId()).append(")");
        return buffer.toString();
    }

    @Override
    public Message toProtoMessage(Context respCtx) {
        TeTransactionMessage.Sale.Builder builder = TeTransactionMessage.Sale.newBuilder();
        builder.setSerialNo(this.getSerialNo()).setTransaction(AmqpMessageUtils.assembleTransactionMsg(respCtx));
        builder.setMultiDraw(this.getMultipleDraws()).setTotalAmount(this.getTotalAmount().toString());
        builder.setGameInstance((TeTransactionMessage.GameInstance) this.getGameInstance().toProtoMessage(respCtx));

        for (int i = 0; i < this.getEntries().size(); i++) {
            builder.addEntries((TeTransactionMessage.Sale.Entry) this.getEntries().get(i).toProtoMessage(respCtx));
        }

        return builder.build();
    }
}
