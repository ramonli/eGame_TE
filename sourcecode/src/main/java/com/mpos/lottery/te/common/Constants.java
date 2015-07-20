package com.mpos.lottery.te.common;

import com.mpos.lottery.te.gamespec.game.Game;

public class Constants {
    /** * ID for game Type[lotto]. */
    public static final int GAME_TYPE_LOTTO = Game.TYPE_LOTT;

    /** * ID for game Type[soccer]. */
    public static final int GAME_TYPE_SOCCER = Game.TYPE_SOCCER;

    /** * ID for game Type[racing]. */
    public static final int GAME_TYPE_HORSE_RACING = Game.TYPE_RACING;

    /** * ID for game Type[instant]. */
    public static final int GAME_TYPE_INSTANT_GAME = Game.TYPE_INSTANT;

    /** * ID for game Type[toto]. */
    public static final int GAME_TYPE_TOTO = Game.TYPE_TOTO;

    /** * ID for game Type[bingo]. */
    public static final int GAME_TYPE_BINGO = Game.TYPE_BINGO;

    /** common game type. */
    public static final int COMMON_GAME_TYPE = Game.TYPE_UNDEF;

    /** Invalid. */
    public static final int STATUS_INVALID = 0;

    /** Accepted. */
    public static final int STATUS_ACCEPTED = 1;

    /** Canceled. */
    public static final int STATUS_CANCELED = 2;

    /** Returned. */
    public static final int STATUS_RETURNED = 3;

    /** cancel declined. */
    public static final int STATUS_CANCEL_DECLINED = 4;

    /** Payed. */
    public static final int STATUS_PAYED = 5;

    /** ticket already settlement flag. * */
    public static final int TICKET_SETTLEMENT_FLAG_YES = 5;

    /** ticket not settlement flag. * */
    public static final int TICKET_SETTLEMENT_FLAG_NO = 0;

    public static final int ZERO_CONSTANT = 0;

    public static final int ONE_CONSTANT = 1;

    public static final int TWO_CONSTANT = 2;

    public static final int THREE_CONSTANT = 3;

    /** 4D game selected number flag. */
    public static final String FOUR_DIGIT_FLAG = "X";

    /**
     * if lience check is false then check hasp,else check lience[lience = true; hasp = false].
     */
    public static final boolean LIENCE_CHECK = false;

    public static final String VERSION_OLD = "1.4";
    public static final String VERSION_NEW = "1.5";
    public static final int YES = 1;
    public static final int NO = 0;

    public static final int DESTINATION_TRANSTYPE_PREFIX = 9999;
}
