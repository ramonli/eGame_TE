package com.mpos.lottery.te.config.exception;

/**
 * A unchecked exception, system can not handle this type of exception.
 */
public class SystemException extends RuntimeException implements TeException {
    private static final long serialVersionUID = -4368085228177695262L;
    // response code definition
    public static final int CODE_OK = 200;
    public static final int CODE_REQUIRED_HEADER_MISS = 202;
    public static final int CODE_ERRORFORMAT_TIMESTAMP = 205;
    public static final int CODE_UNMATCHED_MAC = 206;
    public static final int CODE_UNSUPPORTED_TRANSTYPE = 208;
    public static final int CODE_FAILTO_DECRYPT = 209;
    public static final int CODE_WRONG_MESSAGEBODY = 210;
    public static final int CODE_NO_ENTITY = 211;
    public static final int CODE_UNSUPPORTED_PROTOCAL_VERSION = 214;
    public static final int CODE_UNSUPPORTED_INPUT_CHANNEL = 215;
    public static final int CODE_NOLEGAL_WORKINGKEY = 220;
    public static final int CODE_GPE_NOTEXIST = 221;
    public static final int CODE_NO_DEVICE = 222;
    public static final int CODE_NO_OPERATOR = 223;
    public static final int CODE_OPERATOR_INACTIVE = 224;
    public static final int CODE_OPERATOR_SELL_NOPRIVILEDGE = 225;
    public static final int CODE_MERCHANT_INACTIVE = 226;
    public static final int CODE_NO_MERCHANT = 227;
    public static final int CODE_UNMATCHED_TRANSDATE = 228;
    public static final int CODE_OPERATOR_PAYOUT_NOPRIVILEDGE = 229;
    public static final int CODE_UNMATCHED_PIN_OF_SMART_CARD = 230;
    public static final int CODE_NO_SMART_CARD = 231;
    public static final int CODE_NEED_ENROLLMENT = 232;
    public static final int CODE_SMART_CARD_IN_BLACKLIST = 233;
    public static final int CODE_UNBIND_SMART_CARD = 234;
    public static final int CODE_NOT_SAME_MERCHANT = 235;
    public static final int CODE_TOPUP_NON_DEFINITIVE_MERCHANT = 236;
    public static final int CODE_OUT_OF_RISK_CONTROL = 237;
    public static final int CODE_OPERATOR_NO_MERCHANT = 240;
    public static final int CODE_DEVICE_INACTIVE = 241;
    public static final int CODE_MERCHANT_NO_GAME = 242;
    public static final int CODE_SALE_PAYOUT_DIFF_DISTRIBUTOR = 243;
    public static final int CODE_NO_ALLOCATED_DEVICE = 251;
    public static final int CODE_CANCEL_NONSAEL_TRANS = 260;
    public static final int CODE_MANUAL_CANCEL_DISABLED = 261;
    public static final int CODE_MERCHANT_UNALLOWED_PAY = 272;
    public static final int CODE_MERCHANT_UNALLOWED_CASHOUT = 273;
    public static final int CODE_MERCHANT_UNALLOWED_CASHOUT_SCOPE = 274;
    public static final int CODE_EXCEED_ALLOWED_MERCHANT_DAILY_CASHOUT_LIMIT = 275;
    public static final int CODE_EXCEED_ALLOWD_MULTI_DRAW = 280;
    public static final int CODE_GAME_INACTIVE = 281;
    public static final int CODE_REPEATED_TICKET = 300;
    public static final int CODE_UNSUPPORTED_GAME_TYPE = 301;
    public static final int CODE_NO_GAMEDRAW = 303;
    public static final int CODE_NOT_ACTIVE_DRAW = 304;
    public static final int CODE_WRONGFORMAT_SELECTEDNUMBER = 305;
    public static final int CODE_UNSUPPORTED_BETOPTION = 306;
    public static final int CODE_UNMATCHED_SALEAMOUNT = 307;
    public static final int CODE_DRAW_NOTPAYOUTSTARTED = 308;
    public static final int CODE_SALE_STOPPED_CHANNEL = 309;
    public static final int CODE_SUSPENDED_GAME_INSTANCE = 310;
    public static final int CODE_DULPLICATED_TRANSACTION = 312;
    public static final int CODE_NO_TICKET = 315;
    public static final int CODE_INVALID_PAYOUT = 316;
    public static final int CODE_CANCEL_CANCELED_TICKET = 317;
    public static final int CODE_CANCELLED_TRANS = 321;
    public static final int CODE_MULTIPLEDRAW_NEGATIVE = 325;
    public static final int CODE_OPERATOR_TOPUP_IGNORED = 326;
    public static final int CODE_EXCEED_CREDITLIMIT = 327;
    public static final int CODE_NO_TRANSACTION = 328;
    public static final int CODE_NO_ENTRIES = 329;
    public static final int CODE_WRONGFORMAT_SERIALNO = 330;
    public static final int CODE_NO_SERIALNO = 331;
    public static final int CODE_UNMATCHED_PIN = 332;
    public static final int CODE_FAILTO_CANCEL = 333;
    public static final int CODE_TICKET_TOTALAMOUNT_LESSTHAN_ZERO = 334;
    public static final int CODE_TICKET_BLOCKPAYOUT = 335;
    public static final int CODE_NOENOUGH_FUTUREGAMEDRAW = 336;
    public static final int CODE_CONFIRM_NONPAYEDTIKCET = 337;
    public static final int CODE_UNMATHCED_REFUND = 338;
    public static final int CODE_INPROGRESSOF_WINNINGANALYSIS = 339;
    public static final int CODE_CANCEL_SETTLEDTICKET = 340;
    public static final int CODE_EXCEED_MAX_PAYOUT = 341;
    public static final int CODE_EXCEED_LAST_CLAIMTIME = 342;
    public static final int CODE_SUSPEND_PAYOUT = 343;
    public static final int CODE_NO_ACTUALAMOUNT = 345;
    public static final int CODE_NOTMATCH_ACTUALAMOUNT = 346;
    public static final int CODE_NOTWINNINGTICKET = 347;
    public static final int CODE_EXCEED_MAX_MULTIPLE = 348;
    public static final int CODE_CANCELED_WINNING_TICKET = 349;

    public static final int CODE_FAILTO_SETTLEMENT = 350;
    // lock terminal after manual settlement.
    public static final int CODE_MANUAL_SETTLE_BATCH = 352;
    public static final int CODE_SETTLED_BATCH = 353;
    public static final int CODE_NOTRANS_ALLOWED_AFTER_SETTLEMNT = 354;

    public static final int CODE_STARTTIME_LATER_THAN_ENDTIME = 360;
    public static final int CODE_EXCEED_MAX_ALLOWED_QUERY_DAYS = 361;

    public static final int CODE_SUSPENDED_SALE = 370;

    // Cambodia cash out
    public static final int CODE_INSUFFICIENT_BALANCE = 372;
    public static final int CODE_CASHOUTPASS_INCORRECT = 373;
    public static final int CODE_CASHOUTPASS_EXPIRETIME = 374;
    public static final int CODE_CASHOUTPASS_EXCEED_MAXTIMES = 375;
    public static final int CODE_CASHOUTPASS_ALREADY_USED = 376;
    public static final int CODE_CASHOUTPASS_NO_EXIST_BARCODE = 377;
    public static final int CODE_CASHOUTMANUAL_INCORRECT_PASSWORD = 378;
    public static final int CODE_CASHOUT_OPERATOR_SHOULD_NOT_SAME = 379;
    public static final int CODE_CASHOUT_AMOUNT_LESSTHAN_ZERO = 380;

    // what is the fuck? there is already a 315.
    public static final int CODE_NO_IG_TICKET_FOUND = 399;
    public static final int CODE_DULPLICATED_ACTIVE = 400;
    public static final int CODE_VALIDATE_REPEAT = 401;
    public static final int CODE_NOPACKET = 402;
    public static final int CODE_VALIDATE_NOACTIVETICKET = 403;
    public static final int CODE_NO_INSTANTPRIZE = 404;
    public static final int CODE_ACTIVE_NOSOLDTICKET = 405;
    public static final int CODE_TICKET_INBLACKLIST = 406;
    public static final int CODE_NOTACTIVATIONTIME = 407;
    public static final int CODE_AFTER_STOPPAYOUTIME = 408;
    public static final int CODE_SELL_INACTIVETICKET = 409;
    public static final int CODE_EXCEED_MAX_VALIDATION_TIMES = 410;
    public static final int CODE_NOT_LAST_SERIAL = 411;
    public static final int CODE_NO_TICKETSFOUND = 412;
    public static final int CODE_XORMD5_NOTMATCH = 413;
    public static final int CODE_VIRN_VALIDATED = 414;
    public static final int CODE_FAIL_ACTIVEBYCRITERIA = 415;
    public static final int CODE_ONE_VALIDATIONCHANGE = 416;
    public static final int CODE_ONE_DAMAGETICKET = 417;
    public static final int CODE_WRONGFORMAT_CRITERIA = 418;
    public static final int CODE_FAIL_BATCHVALIDATION = 419;
    public static final int CODE_VALIDATION_FREE_IG = 420;
    public static final int CODE_VALIDATION_FREE_LOTTO = 421;
    public static final int CODE_BATCH_VALIDATION_NOT_SAME_TYPE = 422;
    public static final int CODE_NO_PRIZE_OBJECT = 426;
    public static final int CODE_NO_SETTLEMENT_REPORT = 427;

    public static final int CODE_BINGO_NO_TICKET_SELL = 430;
    public static final int CODE_BINGO_ENTITY_MATCH = 431;

    // VOUCHER RELATED
    public static final int CODE_EVENLY_DIVIDED_BASE_AMOUNT = 450;
    public static final int CODE_IN_ALLOWED_FACE_AMOUNT = 451;
    public static final int CODE_INVALID_VOUCHER = 452;
    public static final int CODE_INVALID_USER = 453;
    public static final int CODE_EXPIRED_VOUCHER = 454;
    public static final int CODE_CANCEL_NON_TOPUPED_VOUCHER = 455;
    public static final int CODE_NO_VOUCHER = 456;
    public static final int CODE_NOT_ENOUGH_PAYMENT = 457;

    public static final int CODE_NOT_EXIST_BATCH_NUMBER = 458;

    public static final int CODE_INTERNAL_SERVER_ERROR = 500;

    // vat specific
    public static final int CODE_VAT_NOFOUND = 505;
    public static final int CODE_VAT_NOT_ALLOCATED_TO_MERCHANT = 506;
    public static final int CODE_VAT_NOT_GAME_ALLOCATED = 507;
    public static final int CODE_EXCEED_MAX_TRANS_COUNT = 508;

    public static final int CODE_NO_LARGE_FOR_NUMBERS = 510;

    // bingo specific
    public static final int CODE_NO_ENOUGH_REF_TICKET = 515;
    public static final int CODE_NO_ENOUGH_REF_ENTRY = 516;
    public static final int CODE_UNMATCHED_ENTRY_COUNT = 517;

    public static final int CODE_REMOTE_SERVICE_TIMEOUT = 700;
    // Remote service respond failure message.
    public static final int CODE_REMOTE_SERVICE_FAILUER = 701;

    public static final int CODE_TOTO_NONE_MATCHS = 800;
    public static final int CODE_TOTO_SELECT_TEAM_IS_ERROR = 801;
    public static final int CODE_TOTO_TRIPLE_INFO_IS_NULL = 802;
    public static final int CODE_TOTO_TRIPLE_INFO_ERROR = 803;
    public static final int CODE_4D_LENGTH_ERROR = 804;
    public static final int CODE_4D_MIN_NUMBER_COUNT_ERROR = 805;
    public static final int CODE_4D_MAX_NUMBER_ERROR = 806;
    public static final int CODE_4D_FORMAT_ERROR = 807;
    public static final int CODE_4D_ENTRY_AMOUNT_ERROR = 808;
    public static final int CODE_HASP_LICENSE_NOT_SUPPORT_GAME = 900;

    /** hasp license is invalid. */
    public static final int CODE_HASP_LICENSE_INVALID = 901;
    /** selected number had changed. **/
    public static final int CODE_UN_SELECTED_NUMBER_CHANGED = 921;
    private int errorCode = CODE_INTERNAL_SERVER_ERROR;
    // metaData will be used to assemble response message.

    /** related to IG Batch validation. **/
    public static final int CODE_TICKET_IS_USING_BY_OTHER_OPERATOR = 922;
    private Object[] metaData;

    public SystemException(int code, Object[] metaData) {
        this.errorCode = code;
        this.metaData = metaData;
    }

    public SystemException(int code, String message) {
        super(message);
        this.errorCode = code;
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

    public SystemException(int code, Object[] metaData, Throwable cause) {
        super(cause);
        this.errorCode = code;
        this.metaData = metaData;
    }

    public SystemException(int code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code;
    }

    public SystemException(String message) {
        super(message);
        this.errorCode = CODE_INTERNAL_SERVER_ERROR;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public Object[] getMetaData() {
        return this.metaData;
    }
}
