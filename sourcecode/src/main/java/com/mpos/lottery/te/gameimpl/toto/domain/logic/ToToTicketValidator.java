package com.mpos.lottery.te.gameimpl.toto.domain.logic;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.toto.dao.ToToGameInstanceDao;
import com.mpos.lottery.te.gameimpl.toto.dao.ToToTicketDao;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToEntry;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.validator.DefaultTicketValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.TicketValidator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * A total different TOTO implementation from {@link DefaultTicketValidator}.
 * 
 * @author Ramon
 */
public class ToToTicketValidator implements TicketValidator {
    private ToToTicketDao ticketDao;
    private ToToGameInstanceDao gameInstanceDao;

    @Override
    public void validate(Context respCtx, BaseTicket clientTicket, Game game) throws ApplicationException {
        this.validToToTicketParams((ToToTicket) clientTicket);
    }

    /**
     * valid toto sell ticket params.
     */
    private void validToToTicketParams(ToToTicket ticket) throws ApplicationException {
        Game<?> game = ticket.getGameInstance().getGame();
        // check if the operator can sell this game
        Transaction trans = ticket.getTransaction();
        trans.setGameId(game.getId());

        // only single-draw allowed, and a ticket can carry only one entry
        if (ticket.getMultipleDraws() != 1) {
            throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY,
                    "Only single draw allowed for a TOTO game.");
        }
        if (ticket.getEntries().size() != 1) {
            throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY,
                    "Only single entry allowed of a TOTO ticket.");
        }

        // valid ticket params
        validSelectedMatch(ticket);

    }

    /**
     * valid select team is match DB value.
     */
    private void validSelectedMatch(ToToTicket ticket) throws ApplicationException {
        ToToEntry totoEntry = (ToToEntry) ticket.getEntries().get(0);

        // get DB matchs count
        int matchsCount = this.getGameInstanceDao().getMatchsCountByGameIdAndDrawNo(
                ticket.getGameInstance().getGameId(), ticket.getGameInstance().getNumber());
        if (matchsCount <= 0) {
            throw new SystemException(SystemException.CODE_TOTO_NONE_MATCHS, "have none matchs! Game Name="
                    + ticket.getGameInstance().getGame().getName());
        }

        // valid db matchs count equal to sell ticket transfer matchs details
        if (totoEntry == null || totoEntry.getSelectNumber().split(",").length != matchsCount) {
            throw new SystemException(SystemException.CODE_TOTO_NONE_MATCHS, "Client matchs count("
                    + totoEntry.getSelectNumber().split(",").length + ") not equal to DB Matchs Count(" + matchsCount
                    + ")! Game Name =" + ticket.getGameInstance().getGame().getName());
        }

        /**
         * Get all matches bet option [order by match_seq asc], for example the 1st match only can buy 1(draw match) and
         * 0(visiting team wins), and the 2nd match can buy 3(host team wins) and 1.
         */
        List<String> matchOptions = this.getTicketDao().getBetOptionType(ticket.getGameInstance().getGameId(),
                ticket.getGameInstance().getNumber());

        String[] selectTeams = totoEntry.getSelectNumber().split(",");
        for (int i = 0; i < selectTeams.length; i++) {
            // valid select match'bet type
            if (!validMatchBetOption(selectTeams[i], matchOptions.get(i))) {
                throw new SystemException(SystemException.CODE_TOTO_SELECT_TEAM_IS_ERROR, "Bet type of SelectTeam["
                        + selectTeams[i] + "] is unmatched with allowed match bet bype[" + matchOptions.get(i)
                        + "] of Game instance =" + ticket.getGameInstance().getId());
            }
        }

        // valid triple count and double count
        validTripleAndDoubleSelectTeams(totoEntry.getSelectNumber(), ticket.getGameInstance().getGameId());

        // valid ticket amount
        checkTicketAmount(ticket);
    }

    /**
     * valid client select teams triple count and double count.
     */
    private void validTripleAndDoubleSelectTeams(String selectTeam, String gameId) throws ApplicationException {
        Map<Integer, Integer[]> map = this.getTicketDao().getToToOperatorParameters(gameId);
        if (map == null || map.size() <= 0) {
            throw new SystemException(SystemException.CODE_TOTO_TRIPLE_INFO_IS_NULL,
                    "Triple info is null in Server Side!");
        }
        String[] selectTeams = selectTeam.split(",");
        int tripleCount = 0;
        int doubleCount = 0;
        for (String str : selectTeams) {
            if (str.split("\\|").length == 3) {
                tripleCount++;
            }
            if (str.split("\\|").length == 2) {
                doubleCount++;
            }
        }
        Integer[] doubleInfo = map.get(tripleCount);
        if (doubleInfo == null) {
            throw new SystemException(SystemException.CODE_TOTO_TRIPLE_INFO_ERROR,
                    "No Triple info found for selected number(" + selectTeam + ")!");
        }
        if (!(doubleCount >= doubleInfo[0] && doubleCount <= doubleInfo[1])) {
            throw new SystemException(SystemException.CODE_TOTO_TRIPLE_INFO_ERROR, "Selected number(" + selectTeam
                    + ") is unmatched with pre-defined triple constraints!");
        }
    }

    /**
     * Valid selected team's bet type
     * 
     * @param clientBetType
     *            The bet type picked by player, usually it will be 0|1|3, or 1 etc.
     * @param dbBetType
     *            the bet type defined by the backend which limit which bet type can be played of a given matched. It
     *            has below options:
     *            <ul>
     *            <li>0 - 3 option[0/1/3]</li>
     *            <li>1 - 3 option handicap[0/1/3]</li>
     *            <li>2 - 2 option[0/1]</li>
     *            <li>3 - 2 option handicap[0/1]</li>
     *            <li>4 - Any value</li>
     *            </ul>
     */
    private boolean validMatchBetOption(String clientBetType, String dbBetType) {
        if (dbBetType == null || "".equals(dbBetType)) {
            return false;
        }
        String[] clientBetTypes = clientBetType.split("\\|");
        for (String splitValue : clientBetTypes) {
            if ("0".equals(dbBetType) || "1".equals(dbBetType)) {
                if ("0,1,3".indexOf(splitValue) == -1) {
                    return false;
                }
            } else if ("2".equals(dbBetType) || "3".equals(dbBetType)) {
                if ("0,1".indexOf(splitValue) == -1) {
                    return false;
                }
            } else {
                return true;
            }
        }
        return true;
    }

    /**
     * valid ticket amount.
     */
    private void checkTicketAmount(ToToTicket ticket) throws ApplicationException {
        // get base amount for current game draw
        BigDecimal baeseAmount = this.getGameInstanceDao().getBaseAmoutByGameIdAndDrawNo(
                ticket.getGameInstance().getGameId(), ticket.getGameInstance().getNumber());

        // get selected matches in messages
        List<BaseEntry> entries = ticket.getEntries();
        int totalBets = 0;
        for (BaseEntry entry : entries) {
            int entryBet = ((ToToEntry) entry).calTotalBet();
            entry.setTotalBets(entryBet);
            entry.setEntryAmount(SimpleToolkit.mathMultiple(baeseAmount, new BigDecimal(entryBet)));
            totalBets += entryBet;

        }
        // valid total amount is OK
        if (ticket.getTotalAmount().compareTo(baeseAmount.multiply(new BigDecimal(totalBets))) != 0) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The total amount("
                    + ticket.getTotalAmount() + ") of client ticket is "
                    + "unmatched with the server side(totalAmount=" + baeseAmount.multiply(new BigDecimal(totalBets))
                    + ",totalBets=" + totalBets + ")");
        }
    }

    public ToToTicketDao getTicketDao() {
        return ticketDao;
    }

    public void setTicketDao(ToToTicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    public ToToGameInstanceDao getGameInstanceDao() {
        return gameInstanceDao;
    }

    public void setGameInstanceDao(ToToGameInstanceDao gameInstanceDao) {
        this.gameInstanceDao = gameInstanceDao;
    }

}
