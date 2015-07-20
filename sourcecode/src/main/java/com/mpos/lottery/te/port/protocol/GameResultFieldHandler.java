package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.gameimpl.lotto.draw.domain.GameResult;
import com.mpos.lottery.te.port.Context;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

public class GameResultFieldHandler extends GeneralizedFieldHandler {

    @Override
    public Object convertUponGet(Object object) {
        if (object == null) {
            return null;
        }
        GameResult result = (GameResult) object;
        String r = result.getBaseNumber();
        if (result.getSpecialNumber() != Context.UNINITIAL_VALUE) {
            r += "-" + result.getSpecialNumber();
        }
        return r;
    }

    @Override
    public Object convertUponSet(Object object) {
        if (object == null) {
            return null;
        }
        String s = (String) object;
        GameResult result = new GameResult();
        result.setBaseNumber(s);
        return result;
    }

    @Override
    public Class getFieldType() {
        return GameResult.class;
    }

}
