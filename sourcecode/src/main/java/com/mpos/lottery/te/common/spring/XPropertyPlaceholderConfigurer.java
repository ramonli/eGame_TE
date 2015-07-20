package com.mpos.lottery.te.common.spring;

import net.mpos.core.util.SecUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

public class XPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    private Log logger = LogFactory.getLog(XPropertyPlaceholderConfigurer.class);
    public static final String JDBC_PASSWORD = "jdbc.password";
    public static final String JDBC_USER = "jdbc.user";

    public String resolvePlaceholder(String placeholder, Properties props) {
        String value = props.getProperty(placeholder);
        if (JDBC_PASSWORD.equalsIgnoreCase(placeholder) || JDBC_USER.equalsIgnoreCase(placeholder)) {
            value = SecUtil.decrypt(value);
            // logger.debug("The value of placeholder:" + placeholder + " is " +
            // value);
        }
        return value;
    }

}
