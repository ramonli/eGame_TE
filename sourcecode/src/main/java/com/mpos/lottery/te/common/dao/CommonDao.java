package com.mpos.lottery.te.common.dao;

import com.mpos.lottery.te.config.exception.ApplicationException;

/**
 * <Description functions in a word> <Detail description>
 * 
 * @author Administrator
 * @version [Version NO, May 18, 2010]
 * @since [product/Modul version]
 */
public interface CommonDao {

    /**
     * find game type by serial no,distinguish lotto/bingo/toto/IG game.
     */
    public int findGameTypeBySerialNo(String serialNo) throws ApplicationException;

    /**
     * find all suitable games by database config
     * 
     * @throws ApplicationException
     *             [Parameters description]
     * @param languageId
     *            : current system use language
     * 
     * @return String [1:Lotto,5:ToTo,6:Bingo,4:instant game]{"1,4,5"} default lotto[1]
     */
    public String getGamesByConfig(String languageId) throws ApplicationException;
}
