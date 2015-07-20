package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

import java.util.Date;

public class TimestampFieldHandler extends GeneralizedFieldHandler {

    /**
     * This method will be invoked when marshall.
     */
    @Override
    public Object convertUponGet(Object value) {
        if (value == null) {
            return null;
        }
        return SimpleToolkit.formatDate((Date) value, this.getDateFormat());
    }

    /**
     * This method will be invoked when unmarshall.
     */
    @Override
    public Object convertUponSet(Object value) {
        return SimpleToolkit.parseDate((String) value, this.getDateFormat());
    }

    @Override
    public Class getFieldType() {
        return Date.class;
    }

    private String getDateFormat() {
        MLotteryContext prop = MLotteryContext.getInstance();
        return prop.getTimestampFormat();
    }

}
