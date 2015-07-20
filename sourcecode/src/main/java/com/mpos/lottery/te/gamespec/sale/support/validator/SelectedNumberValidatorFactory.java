package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.gamespec.game.Game;

/**
 * In general case, you should implement a game type specific factory to return game type specific
 * <code>SelectedNumberValidator</code>.
 * <p>
 * A game specific factory may look like as below:
 * 
 * <pre>
 * public class GameTypeSelectedNumberValidatorFactory implements SelectedNumberValidatorFactory {
 * 
 *     &#064;Override
 *     public AbstractSelectedNumberValidator newSelectedNumberValidator(Game, game, int betOption) {
 *         MLotteryContext mlotteryContext = MLotteryContext.getInstance();
 *         AbstractSelectedNumberValidator validator = null;
 *         switch (betOption) {
 *             case BaseEntry.BETOPTION_SINGLE:
 *                 validator = new GameTypeSingleSelectedNumberValidator(
 *                         mlotteryContext.getSelectedNumberFormat(this.gameType, betOption));
 *                 break;
 *             case BaseEntry.BETOPTION_MULTIPLE:
 *                 validator = new GameTypeMultipleSelectedNumberValidator(
 *                         mlotteryContext.getSelectedNumberFormat(this.gameType, betOption));
 *                 break;
 *             case BaseEntry.BETOPTION_BANKER:
 *                 validator = new GameTypeBankerSelectedNumberValidator(
 *                         mlotteryContext.getSelectedNumberFormat(this.gameType, betOption));
 *                 break;
 *             case BaseEntry.BETOPTION_ROLL:
 *                 validator = new GameTypeRollSelectedNumberValidator(mlotteryContext.getSelectedNumberFormat(
 *                         this.gameType, betOption));
 *                 break;
 *             default:
 *                 throw new SystemException(&quot;Unsupported bet option:&quot; + betOption);
 *         }
 *         return validator;
 *     }
 * }
 * </pre>
 * 
 * @author Ramon
 * 
 */
public interface SelectedNumberValidatorFactory {

    /**
     * Construct a <code>SelectedNumber</code> by supplied bet option. A game type may support different bet option from
     * another.
     * <p>
     * Each game type must provide a dedicated implementation.
     */
    AbstractSelectedNumberValidator newSelectedNumberValidator(Game game, int betOption);

}
