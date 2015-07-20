package com.mpos.lottery.te.port.protocol;

import com.mpos.lottery.te.common.util.SimpleToolkit;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

import java.util.Date;

public class DateFieldHandler extends GeneralizedFieldHandler {
    private static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * This method will be invoked when marshall.
     */
    @Override
    public Object convertUponGet(Object value) {
        if (value == null) {
            return null;
        }
        return SimpleToolkit.formatDate((Date) value, DATE_FORMAT);
    }

    /**
     * Thie method will be invoked when unmarshall.
     */
    @Override
    public Object convertUponSet(Object value) {
        return SimpleToolkit.parseDate((String) value, DATE_FORMAT);
    }

    @Override
    public Class getFieldType() {
        return Date.class;
    }

}
