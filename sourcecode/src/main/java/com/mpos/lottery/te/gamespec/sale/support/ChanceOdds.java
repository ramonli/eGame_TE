package com.mpos.lottery.te.gamespec.sale.support;

import java.math.BigDecimal;

/**
 * A single entry may have multiple chance to win prize, and a chance may has possibility to win one of multiple prize
 * levels, for example a '4D' entry of digital game, may can win anyone of 'First 4D', 'First3D', 'Last3D' etc prize
 * levels. This class will represent all possible prize levels that a given bet option may win.
 * 
 * @author Ramon
 */
public class ChanceOdds {
    private BigDecimal odds = new BigDecimal("0");
    /**
     * The type of prize levels of the given bet option may win. To some game type, the value of
     * <code>prizeLevelType</code> may carry some extra information.
     * <p/>
     * For example to digital game, the prize type of (TypeB and TypeC) will be defined as below,
     * <table border="1">
     * <tr>
     * <td>prizeLevelType</td>
     * <td>Description</td>
     * </tr>
     * <tr>
     * <td>41</td>
     * <td>First 4D</td>
     * </tr>
     * <tr>
     * <td>31</td>
     * <td>First 3D</td>
     * </tr>
     * <tr>
     * <td>32</td>
     * <td>Last 3D</td>
     * </tr>
     * <tr>
     * <td>21</td>
     * <td>First 2D</td>
     * </tr>
     * <tr>
     * <td>22</td>
     * <td>Last 2D</td>
     * </tr>
     * <tr>
     * <td>11</td>
     * <td>First 1D</td>
     * </tr>
     * <tr>
     * <td>12</td>
     * <td>Last1D</td>
     * </tr>
     * </table>
     * In above case, the first digital of <code>prizeLevelType</code> is the count of betting number, the 2nd digit
     * represents retrieve number from head or tail.
     * <p/>
     * However for 'TypeA'(
     * {@link com.mpos.lottery.te.gameimpl.digital.sale.support.DigitalRiskControlService.ALGORITHM_TYPE_A} ) of digital
     * game, the first digital of <code>prizeLevelType</code> represents the prize level, for example '71' means the
     * exactly matched of 7th prize level, '73' means the mixed matched of 7th prize level.
     */
    private String prizelLevelType;

    /**
     * The betting number associate with given prize level. For example a '4D' entry '2,0,1,4' may win 'First4D',
     * 'first3D' and 'last3d', then the betting number of each prize level types will be,
     * <ul>
     * <li>first4d - 2,0,1,4</li>
     * <li>first3d - 2,0,1</li>
     * <li>last3d - 0,1,4</li>
     * </ul>
     */
    private String bettingNumber;

    public int getPrizeLevelTypeInt() {
        return Integer.parseInt(this.prizelLevelType);
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public String getPrizelLevelType() {
        return prizelLevelType;
    }

    public void setPrizelLevelType(String prizelLevelType) {
        this.prizelLevelType = prizelLevelType;
    }

    public String getBettingNumber() {
        return bettingNumber;
    }

    public void setBettingNumber(String bettingNumber) {
        this.bettingNumber = bettingNumber;
    }

    @Override
    public String toString() {
        return "ChanceOdds [odds=" + odds + ", prizelLevelType=" + prizelLevelType + ", bettingNumber=" + bettingNumber
                + "]";
    }

}
