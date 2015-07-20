package com.mpos.lottery.te.trans.domain;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.RequestHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Define the transaction types.
 */
public enum TransactionType {
    GET_WORKING_KEY(501, false, false),
    CHECK_ALIVE(502, false, false),
    GAME_DRAW_ENQUIRY(101, RequestHeaders.HEADERS_BASE_MANDATORY),
    TRANSFER_CREDIT(116, RequestHeaders.HEADERS_BASE_MANDATORY),
    VAT_TRANSFER_CREDIT(118, RequestHeaders.HEADERS_BASE_MANDATORY),
    VAT_REPRINT_TICKET(119, RequestHeaders.HEADERS_BASE_MANDATORY),
    SELL_TICKET(200, true, true, RequestHeaders.HEADERS_GAME_MANDATORY),
    CANCEL_BY_TICKET(201, true, true, RequestHeaders.HEADERS_GAME_MANDATORY),
    TICKET_ENQUIRY(202, RequestHeaders.HEADERS_GAME_MANDATORY),
    OFFLINE_SALE_UPLOAD(203, false, true, RequestHeaders.HEADERS_GAME_MANDATORY),
    CANCEL_DECLINED(204),
    CONFIRM_PAYOUT(205, RequestHeaders.HEADERS_GAME_MANDATORY),
    CANCEL_BY_TRANSACTION(206, true, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    CANCEL_FROM_WEB(207),
    SELL_TICKET_IN_BATCH(208, RequestHeaders.HEADERS_BASE_MANDATORY),
    GENERAL_SELL_TICKET(209, RequestHeaders.HEADERS_GAME_MANDATORY),
    CANCEL_BY_CLIENT_MANUALLY(210, true, true, RequestHeaders.HEADERS_GAME_MANDATORY),
    ENQUIRY_QP_NUMBERS(211, RequestHeaders.HEADERS_GAME_MANDATORY),
    PRIZE_ENQUIRY(301, RequestHeaders.HEADERS_GAME_MANDATORY),
    PAYOUT(302, true, true, RequestHeaders.HEADERS_GAME_MANDATORY),
    REVERSAL(303, true, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    TRANSACTION_ENQUIRY(310, RequestHeaders.HEADERS_BASE_MANDATORY),
    UNCLAIMED_WINNER_ENQUIRY(311, RequestHeaders.HEADERS_GAME_MANDATORY),
    IG_VALIDATION_BATCH_UPLOAD(312, false, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    SETTLEMENT(321, RequestHeaders.HEADERS_BASE_MANDATORY),
    BATCH_OF_UPLOAD(322, RequestHeaders.HEADERS_BASE_MANDATORY),
    ACTIVITY_REPORT(330, RequestHeaders.HEADERS_BASE_MANDATORY),
    ACTIVE_IG_BY_CRITERIA(401, RequestHeaders.HEADERS_BASE_MANDATORY),
    RESERVED_NUMBERS(340, RequestHeaders.HEADERS_BASE_MANDATORY),
    INCOME_BALANCE_TRANSFER(350, RequestHeaders.HEADERS_BASE_MANDATORY),
    GET_CASH_OUT_PASS(351, RequestHeaders.HEADERS_BASE_MANDATORY),
    CASH_OUT_OPERATOR_PASS(352, RequestHeaders.HEADERS_BASE_MANDATORY),
    CASH_OUT_OPERATOR_MANUAL(353, RequestHeaders.HEADERS_BASE_MANDATORY),
    VALIDATE_INSTANT_TICKET(402, true, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    OFFLINE_INSTANT_TICKET_UPLOAD(403, RequestHeaders.HEADERS_BASE_MANDATORY),
    SELL_INSTANT_TICKET(404, RequestHeaders.HEADERS_BASE_MANDATORY),
    INSTANT_PACKET_RECEIPT(405, RequestHeaders.HEADERS_BASE_MANDATORY),
    ACTIVE_INSTANT_TICKET(406, RequestHeaders.HEADERS_BASE_MANDATORY),
    BATCH_VALIDATION(407, RequestHeaders.HEADERS_BASE_MANDATORY),
    IG_PRIZE_ENQUIRY(408, RequestHeaders.HEADERS_BASE_MANDATORY),
    IG_GET_CONFIRM_BATCH_NUMBER(409, false, false, RequestHeaders.HEADERS_BASE_MANDATORY),
    PARTIAL_BATCH_VALIDATION(410, RequestHeaders.HEADERS_BASE_MANDATORY),
    CONFIRM_BATCH_VALIDATION(411, true, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    REPORT_OF_CONFIRM_BATCH_VALIDATION(412, RequestHeaders.HEADERS_BASE_MANDATORY),
    PLAYER_CASH_OUT(445, true, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    PLAYER_TOPUP(446, true, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    OPERATOR_TOPUP_VOUCHER(447, false, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    VAT_REFUND(450, RequestHeaders.HEADERS_BASE_MANDATORY),
    VAT_UPLOAD_OFFLINESALE(451, RequestHeaders.HEADERS_BASE_MANDATORY),
    CASH_OUT_OF_CUSTOMER(445, RequestHeaders.HEADERS_BASE_MANDATORY),
    AIRTIME_TOPUP(455, true, true, RequestHeaders.HEADERS_BASE_MANDATORY),
    SELL_TELECO_VOUCHER(456, true, true, RequestHeaders.HEADERS_BASE_MANDATORY);

    private static final int INTERVAL = 4000;
    private int requestType;
    // whether the transaction require a request message body
    private boolean requireBody = true;
    // whether need to publish AMQP transaction message.
    private boolean publishMessage = false;
    private List<String> requiredHeaders = new ArrayList<String>();

    /**
     * Constructor of <code>TransactionType</code>. The argument 'requireHeaders' defines all required headers of the
     * request with given transaction type. However there are some header are built-in required for all transaction
     * types:
     * <ul>
     * <li>X-Protocal-Version</li>
     * <li>X-Timestamp</li>
     * <li>X-GPE-Id</li>
     * <li>X-Transaction-Type</li>
     * </ul>
     * That says you don't need to add them to required headers of a transaction type.
     */
    private TransactionType(int reqType, String... requireHeaders) {
        this.requestType = reqType;
        if (requireHeaders.length > 0) {
            this.requiredHeaders = Arrays.asList(requireHeaders);
        }
    }

    private TransactionType(int reqType, boolean publishTransMsg, boolean requireBody, String... requireHeaders) {
        this(reqType, requireHeaders);
        this.requireBody = requireBody;
        this.publishMessage = publishTransMsg;
    }

    /**
     * Determine whether a transaction type is cancellation or not.
     * 
     * @return true if cancellation, otherwise false.
     */
    public boolean isCancellation() {
        int transType = this.getRequestType();
        if (transType == TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType()
                || transType == TransactionType.CANCEL_BY_TICKET.getRequestType()
                || transType == TransactionType.CANCEL_BY_TRANSACTION.getRequestType()
                || transType == TransactionType.REVERSAL.getRequestType()) {
            return true;
        }
        return false;
    }

    public int getRequestType() {
        return this.requestType;
    }

    public int getResponseType() {
        return this.requestType + INTERVAL;
    }

    public boolean isRequiredHeader(String requestHeader) {
        return this.requiredHeaders.contains(requestHeader);
    }

    public boolean isPublishTransMsg() {
        return this.publishMessage;
    }

    public boolean isRequireBody() {
        return this.requireBody;
    }

    /**
     * Get the <code>TransactionType</code> by primitive transaction type.
     */
    public static TransactionType getTransactionType(int requestType) {
        TransactionType[] types = TransactionType.values();
        for (int i = 0; i < types.length; i++) {
            if (requestType == types[i].getRequestType()) {
                return types[i];
            }
        }
        throw new SystemException(SystemException.CODE_UNSUPPORTED_TRANSTYPE, "Unsupported transaction type:"
                + requestType);
    }

    /**
     * Check whether the given transaction is supported or not.
     */
    public static boolean isSupported(int transType) {
        TransactionType[] types = TransactionType.values();
        for (int i = 0; i < types.length; i++) {
            if (transType == types[i].getRequestType()) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
        System.out.println(isSupported(456));
    }
}
