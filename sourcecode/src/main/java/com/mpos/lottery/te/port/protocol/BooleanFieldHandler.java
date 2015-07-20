package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.config.exception.SystemException;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

public class BooleanFieldHandler extends GeneralizedFieldHandler {
    private static final int BOOL_YES = 1;
    private static final int BOOL_NO = 0;

    @Override
    public Object convertUponGet(Object arg0) {
        if (arg0 == null) {
            return null;
        }
        Boolean b = (Boolean) arg0;
        if (b) {
            return BOOL_YES;
        }
        return BOOL_NO;
    }

    @Override
    public Object convertUponSet(Object arg0) {
        Integer i = (Integer) arg0;
        if (BOOL_YES == i) {
            return true;
        } else if (BOOL_NO == i) {
            return false;
        }
        throw new SystemException("Unsupported boolean value:" + i);
    }

    @Override
    public Class getFieldType() {
        return boolean.class;
    }

}
