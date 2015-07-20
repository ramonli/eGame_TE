package com.mpos.lottery.te.port;

import com.mpos.lottery.te.common.util.ArrayHelper;

public class RequestHeaders {
    public static final String HEADER_PROTOCAL_VERSION = "X-Protocal-Version";
    public static final String HEADER_TRACE_MESSAGE_ID = "X-Trace-Message-Id";
    public static final String HEADER_TIMESTAMP = "X-Timestamp";
    public static final String HEADER_TRANSACTION_ID = "X-Transaction-Id";
    public static final String HEADER_TRANSACTION_TYPE = "X-Transaction-Type";
    public static final String HEADER_GPE_ID = "X-GPE-Id";
    public static final String HEADER_BATCHNUMBER = "X-Trans-BatchNumber";
    public static final String HEADER_TERMINAL_ID = "X-Terminal-Id";
    public static final String HEADER_OPERATOR_ID = "X-Operator-Id";
    public static final String HEADER_REPONSE_CODE = "X-Response-Code";
    public static final String HEADER_MAC = "X-MAC";
    public static final String HEADER_GAME_TYPE_ID = "X-Game-Type-Id";
    public static final String HEADER_GPS_LOCATION = "X-GPS-Location";

    /**
     * Define all headers may occur in request.
     */
    public static final String[] HEADERS_REQUEST = new String[] { HEADER_TRANSACTION_TYPE, HEADER_PROTOCAL_VERSION,
            HEADER_TIMESTAMP, HEADER_GPE_ID, HEADER_TRACE_MESSAGE_ID, HEADER_TERMINAL_ID, HEADER_OPERATOR_ID,
            HEADER_MAC, HEADER_GAME_TYPE_ID, HEADER_GPS_LOCATION };

    /**
     * Define required headers for all transactions.
     */
    public static final String[] HEADERS_GLOBAL_MANDATORY = new String[] { HEADER_TRANSACTION_TYPE,
            HEADER_PROTOCAL_VERSION, HEADER_TIMESTAMP, HEADER_GPE_ID };

    /**
     * Define required headers for general biz transactions.
     */
    public static final String[] HEADERS_BASE_MANDATORY = ArrayHelper.concatenate(HEADERS_GLOBAL_MANDATORY,
            new String[] { HEADER_TRACE_MESSAGE_ID, HEADER_OPERATOR_ID, HEADER_TERMINAL_ID, HEADER_MAC });

    /**
     * Define required header of a transaction which need game type header.
     */
    public static final String[] HEADERS_GAME_MANDATORY = ArrayHelper.concatenate(HEADERS_BASE_MANDATORY,
            HEADER_GAME_TYPE_ID);
}
