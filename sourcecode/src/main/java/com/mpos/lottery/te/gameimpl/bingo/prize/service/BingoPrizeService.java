package com.mpos.lottery.te.gameimpl.bingo.prize.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gameimpl.bingo.prize.BingoWinningStatistics;
import com.mpos.lottery.te.gameimpl.bingo.prize.support.second.BingoLuckyPrizeResult;
import com.mpos.lottery.te.gameimpl.bingo.prize.support.second.BingoWinningLuckyItem;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.BasePrizeObject;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.PrizeLevel;
import com.mpos.lottery.te.gamespec.prize.PrizeLevelItem;
import com.mpos.lottery.te.gamespec.prize.service.impl.AbstractPrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelObjectItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BingoPrizeService extends AbstractPrizeService {
    @Override
    public GameType supportedGameType() {
        return GameType.BINGO;
    }

    private BingoWinningStatistics lookupWinningStatistics(BaseGameInstance gameInstance, BaseWinningItem winningItem)
            throws ApplicationException {
        // should cache winning statistics
        BingoWinningStatistics stat = this.getBaseWinningStatisticsDao().getByGameDrawAndPrizeLevelAndVersion(
                BingoWinningStatistics.class, gameInstance.getId(), winningItem.getPrizeLevel(),
                gameInstance.getVersion());
        if (stat == null) {
            throw new ApplicationException(SystemException.CODE_INTERNAL_SERVER_ERROR,
                    "can NOT Bingo find winning statistics(gameDrawId=" + gameInstance.getId() + ",prizeLeve="
                            + winningItem.getPrizeLevel() + ",version=" + gameInstance.getVersion() + ").");
        }
        return stat;
    }

    @Override
    protected PrizeAmount lookupAnnouncedPrizeAmount(BaseWinningItem winningItem, BaseTicket ticket)
            throws ApplicationException {
        BingoWinningStatistics stat = this.lookupWinningStatistics(ticket.getGameInstance(), winningItem);
        return new PrizeAmount(stat.getPrizeAmount(), stat.getTaxAmount(), stat.getActualAmount());
    }

    @Override
    protected void assembleSecondPrize(PrizeDto prize, BaseTicket t, boolean isPrizeEnquiry)
            throws ApplicationException {
        StopWatch sw = new Log4JStopWatch();

        List<BingoWinningLuckyItem> bingoWinningLuckyItems = this.getBaseWinningItemDao().findSecondPrizeBySerialNo(
                t.getSerialNo());
        List<PrizeItemDto> bingoPrizeItemList = new ArrayList<PrizeItemDto>();
        if (bingoWinningLuckyItems != null && bingoWinningLuckyItems.size() > 0) {
            for (BingoWinningLuckyItem soldPrizeItem : bingoWinningLuckyItems) {
                /**
                 * lookup lucky-prize game instance ... refactor to reduce the enquiry of game instance, as multiple
                 * winning items may associate with same game instance
                 * <p>
                 * FIX - no worry, check the API doc of JPA2.0.
                 * <p>
                 * Find by primary key. Search for an entity of the specified class and primary key. If the entity
                 * instance is contained in the persistence context, it is returned from there.
                 */
                BingoGameInstance bingoGameInstance = this.getBaseJpaDao().findById(BingoGameInstance.class,
                        t.getGameInstance().getId());
                if (bingoGameInstance == null) {
                    throw new DataIntegrityViolationException("No lucky prize found by id("
                            + t.getGameInstance().getId() + ").");
                }
                PrizeItemDto prizeItem = prize.lookupPrizeItem(bingoGameInstance.getKey());
                if (null == prizeItem) {
                    prizeItem = new PrizeItemDto();
                    prizeItem.setGameInstance(bingoGameInstance);
                    prizeItem.getGameInstance().setGameId(bingoGameInstance.getGame().getId());
                    prize.addPrizeItem(prizeItem);
                    bingoPrizeItemList.add(prizeItem);
                }

                List<BingoLuckyPrizeResult> list = this.getBaseWinningItemDao()
                        .findLuckyPrizeResultByLuckyNoAndSerialNoAndVersion(soldPrizeItem.getGameInstanceId(),
                                soldPrizeItem.getLuckyNo(), soldPrizeItem.getVersion());
                for (BingoLuckyPrizeResult soldWinning : list) {
                    PrizeLevel soldWinningLevel = this.getPrizeLevelDao().findByPrizeLogicAndLevel(
                            bingoGameInstance.getLuckyPrizeLogicId(), soldWinning.getPrizeLevel());
                    PrizeLevelItemDto prizeLevelItemDto = assemblePrizeItemFromBasePrizeLeveDef(soldWinningLevel,
                            prizeItem);
                    this.updatePrizeStatPerDraw(prize, prizeItem, prizeLevelItemDto);

                }

                for (PrizeItemDto bingoPrizeItem : bingoPrizeItemList) {
                    this.calculateTaxBasedOnPerTicket(prize, t, bingoPrizeItem);
                }

            }
        }

        sw.stop("Assemble_Second_Prize", "Assemble Second prize of ticket(" + t.getSerialNo() + ")");
    }

    /**
     * Assemble a <code>PrizeItemDto</code> which represents a single winning item based on the base prize level
     * definition.
     */
    protected PrizeLevelItemDto assemblePrizeItemFromBasePrizeLeveDef(PrizeLevel prizeLevel, PrizeItemDto prizeItem)
            throws ApplicationException {
        // lookup prize level definition...for lucky draw, prize level
        // information must be retrieved from bd_prize_level.
        PrizeLevelItemDto prizeLevelItemDto = null;
        Collections.sort(prizeLevel.getLevelItems(), new Comparator<PrizeLevelItem>() {

            @Override
            public int compare(PrizeLevelItem o1, PrizeLevelItem o2) {
                return o1.getPrizeType() - o2.getPrizeType();
            }
        });
        for (PrizeLevelItem prizeLevelItem : prizeLevel.getLevelItems()) {
            if (PrizeLevel.PRIZE_TYPE_CASH == prizeLevelItem.getPrizeType()) {
                // for cash, the prizeLevelItem.getNumberOfObject() should
                // always be 1.
                prizeLevelItemDto = this.assemblePrizeLevelInfo(prizeItem.getGameInstance(),
                        prizeLevel.getPrizeLevel(), prizeLevelItem.getPrizeAmount(), prizeLevelItem.getTaxAmount(),
                        prizeLevelItem.getActualAmount(), 1);
            } else {
                if (prizeLevelItemDto == null) {
                    prizeLevelItemDto = new PrizeLevelItemDto();
                    prizeLevelItemDto.setPrizeLevel(prizeLevel.getPrizeLevel() + "");
                    prizeLevelItemDto.setNumberOfPrizeLevel(1);
                }
                PrizeLevelObjectItemDto objectItemDto = new PrizeLevelObjectItemDto();
                objectItemDto.setObjectId(prizeLevelItem.getObjectId());
                objectItemDto.setNumberOfObject(prizeLevelItem.getNumberOfObject());
                // retrieve information from bd_prize_object
                BasePrizeObject prizeObject = this.getBaseJpaDao().findById(BasePrizeObject.class,
                        prizeLevelItem.getObjectId());
                // objectItemDto.setObjectName(prizeLevelItem.getObjectName());
                // objectItemDto.setPrice(prizeLevelItem.getPrizeAmount());
                // objectItemDto.setTaxAmount(prizeLevelItem.getTaxAmount());
                objectItemDto.setObjectName(prizeObject.getName());
                objectItemDto.setPrice(prizeObject.getPrice());
                objectItemDto.setTaxAmount(prizeObject.getTax());

                prizeLevelItemDto.getPrizeLevelObjectItems().add(objectItemDto);
            }
        }

        PrizeLevelItemDto existLevelItem = prizeItem.lookupPrizeLevelItem(prizeLevel.getPrizeLevel(), false);
        if (existLevelItem == null) {
            existLevelItem = prizeLevelItemDto;
            prizeItem.getPrizeLevelItems().add(existLevelItem);
        } else {
            existLevelItem.setNumberOfPrizeLevel(existLevelItem.getNumberOfPrizeLevel()
                    + prizeLevelItemDto.getNumberOfPrizeLevel());
        }
        return prizeLevelItemDto;
    }

}
