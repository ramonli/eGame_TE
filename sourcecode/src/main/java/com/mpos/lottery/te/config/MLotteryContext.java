package com.mpos.lottery.te.config;

import com.mpos.lottery.te.config.dao.SysConfigurationDao;
import com.mpos.lottery.te.config.exception.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

/**
 * Load properties from a customized properties file. This class follows the singleton design pattern.
 */
public class MLotteryContext {
    private Log logger = LogFactory.getLog(MLotteryContext.class);

    public static final String ENTRY_BEAN_WORKINGKEYDAO = "spring.workingkeydao.id";
    public static final String ENTRY_BEAN_SERVICEFACADE = "spring.servicefacade.id";
    public static final String ENTRY_BEAN_DATASOURCE = "spring.datasource.id";
    public static final String ENTRY_BEAN_TRANSACTION_RETRY_LOG = "spring.transactionretrylogdao.id";
    public static final String ENTRY_BEAN_SYSCONFDAO = "spring.sysconfigurationdao.id";
    public static final String ENTRY_BEAN_LOTTOOPDAO = "spring.lottooperatorparameter.id";
    public static final String ENTRY_BEAN_GAMEDAO = "spring.gamedao.id";
    public static final String ENTRY_BEAN_MERCHANTDAO = "spring.merchantdao.id";
    public static final String ENTRY_BEAN_OPERATORDAO = "spring.operatordao.id";
    public static final String ENTRY_BEAN_BASEDAO = "spring.basejpadao";
    public static final String ENTRY_BEAN_OPERATORCOMMISSIONDAO = "spring.operatorcommissiondao.id";
    public static final String ENTRY_BEAN_BINGOGAMEDRAWDAO = "spring.bingogameDrawDao.id";
    public static final String ENTRY_BEAN_JOB_SCHEDULER = "spring.jobscheduler.id";

    public static final String ENTRY_PROTOCAL_VERSION = "protocal.version";
    public static final String ENTRY_DATAFORMAT_TIMESTAMP = "dataformat.timestamp";
    public static final String ENTRY_ERRORCODE = "errorcode.";
    public static final String ENTRY_SQLLOG_ENABLE = "sqllog.enable";
    public static final String COMMISSION_BALANCE_PRECISION = "commission.balance.precision";

    // load resource from classpath
    private static final String RESOURCE_CLASSPATH = "classpath";
    // load resource from file path.
    private static final String RESOURCE_FILE = "file";
    private static final String RESOURCE_DELIM = ":";
    private static final String GLOBAL_KEY_PREFIX = "app.config.";

    private static MLotteryContext instance;
    private String defaultPropertiesFile = "classpath:mlottery_te.properties";
    // private String xmlmappingFile = "classpath:mlottery_xmlmapping.properties";
    private Properties appProp;
    // private Properties xmlmappingProp;
    private BeanFactory beanFactory;

    /**
     * Return a singleton instance.
     */
    public static synchronized MLotteryContext getInstance() {
        try {
            if (instance == null) {
                instance = new MLotteryContext();
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a int-type value.
     * 
     * @throws IllegalArgumentException
     *             if no key defined or the value isn't int type.
     */
    public int getInt(String key) {
        String tmp = this.get(key);
        try {
            return Integer.parseInt(tmp);
        } catch (Exception e) {
            throw new IllegalArgumentException("The entry(key=" + key + ",value=" + tmp + ") is not INT type.");
        }
    }

    /**
     * Get a int-type value. If no key defined, the default value will be returned.
     * 
     * @throws IllegalArgumentException
     *             if the value isn't int type.
     */
    public int getInt(String key, int defaultValue) {
        String tmp = this.getWithNull(key);
        if (tmp == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(tmp);
        } catch (Exception e) {
            throw new IllegalArgumentException("The entry(key=" + key + ",value=" + tmp + ") is not INT type.");
        }
    }

    /**
     * Get a boolean-type value.
     * 
     * @throws IllegalArgumentException
     *             if no key defined or the value isn't boolean type.
     */
    public boolean getBoolean(String key) {
        String value = this.get(key);
        if (value != null) {
            value = value.trim();
        }
        if (value != null && value.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    /**
     * Get a boolean-type value. If no key defined, the default value will be returned.
     * 
     * @throws IllegalArgumentException
     *             if the value isn't boolean type.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = appProp.getProperty(key);
        if (value != null) {
            if (value.trim().equalsIgnoreCase("true")) {
                return true;
            } else {
                return false;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * Get a double-type value.
     * 
     * @throws IllegalArgumentException
     *             if no key defined or the value isn't double type.
     */
    public double getDouble(String key) {
        String tmp = this.get(key);
        try {
            return Double.parseDouble(tmp);
        } catch (Exception e) {
            throw new IllegalArgumentException("The entry(key=" + key + ",value=" + tmp + ") is not DOUBLE type.");
        }
    }

    /**
     * Get a string-type value.
     * 
     * @throws IllegalArgumentException
     *             if the key doesn't exist.
     */
    public String get(String key) {
        String value = appProp.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("can NOT find entry(" + key + ") in properties file:"
                    + this.defaultPropertiesFile);
        }
        return value;
    }

    /**
     * Get a string-type value. If no key exist, the default value will be returned.
     */
    public String get(String key, String defaultValue) {
        String value = this.getWithNull(key);
        return (value == null) ? defaultValue : value;
    }

    public String getWithNull(String key) {
        return this.appProp.getProperty(key);
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    // -----------------------------------------------------
    // BUSINESS METHODS WRAPPER
    // -----------------------------------------------------

    public String getTimestampFormat() {
        return this.get(ENTRY_DATAFORMAT_TIMESTAMP);
    }

    public String getTriperDesIV() {
        return this.get("triperdes.iv");
    }

    public String getInstantTicketSerialNoFormat() {
        return this.get("dataformat.instantticket.serialno");
    }

    public String getDefaultEncoding() {
        return this.get("default.encoding");
    }

    public String getMappingFile(int transType) {
        return this.getMappingFile(transType, null, null);
    }

    /**
     * Lookup xml mapping file of a java class. The lookup order will be as below:
     * <ol>
     * <li>gameType+transType+protocolVersion</li>
     * <li>transType+protocolVersion</li>
     * <li>gameType+transType</li>
     * <li>transType</li>
     * </ol>
     */
    public String getMappingFile(int transType, String protocolVersion, String gameType) {
        String key = gameType + ".transtype." + transType + "." + protocolVersion;
        String value = this.getWithNull(key);
        if (value != null && logger.isDebugEnabled()) {
            logger.debug("find mapping file definition(" + value + ") by key(" + key + ").");
        }
        if (value == null) {
            key = "transtype." + transType + "." + protocolVersion;
            value = this.getWithNull(key);
            if (value != null && logger.isDebugEnabled()) {
                logger.debug("find mapping file definition(" + value + ") by key(" + key + ").");
            }
            if (value == null) {
                key = gameType + ".transtype." + transType;
                value = this.getWithNull(key);
                if (value != null && logger.isDebugEnabled()) {
                    logger.debug("find mapping file definition(" + value + ") by key(" + key + ").");
                }
                if (value == null) {
                    key = "transtype." + transType;
                    value = this.appProp.getProperty(key);
                    if (value == null) {
                        // refer to bug#4746
                        throw new SystemException(SystemException.CODE_UNSUPPORTED_TRANSTYPE,
                                "Unsupported transaction type(transType:" + transType + ", protocolVerion:"
                                        + protocolVersion + ", gameType:" + gameType
                                        + ") - NO mapping file found by key(" + key + ")");
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("find mapping file definition(" + value + ") by key(" + key + ").");
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * Get the port of protocol(http/https), which is defined by Tomcat(server.xml)
     */
    public int getProtocolPort(String protocol) {
        protocol = protocol.toLowerCase();
        return this.getInt("port." + protocol);
    }

    public String getSelectedNumberFormat(int gameType, int betOption) {
        return this.get(gameType + ".selectednumber." + betOption);
    }

    // add by jason for bingo
    public String getbingoNumberFormat() {
        return this.get("bingo.selectednumber");
    }

    public int getWaitLockTime() {
        return this.getInt("database.waitforlock");
    }

    public String getIgnoredPIN() {
        return this.get("ticket.pin.ignored");
    }

    public SysConfiguration getSysConfiguration() {
        // set SysConfiguration
        SysConfigurationDao sysConfDao = (SysConfigurationDao) this.beanFactory.getBean(this
                .get(MLotteryContext.ENTRY_BEAN_SYSCONFDAO));
        SysConfiguration sysConf = sysConfDao.getSysConfiguration();
        if (sysConf == null) {
            throw new SystemException("can NOT find System-Configuration!");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Found system configuration: " + sysConf);
        }
        return sysConf;
    }

    // -----------------------------------------------------
    // PRIVATE METHODS
    // -----------------------------------------------------

    private MLotteryContext() throws Exception {
        // load global configuration property file
        Properties propFiles = new Properties();
        InputStream propInputStream = openInputStream(defaultPropertiesFile);
        propFiles.load(propInputStream);
        propInputStream.close();

        this.appProp = new Properties();
        Set<Object> keyset = propFiles.keySet();
        // sort the key to make sure the module configuration files will be loaded as pre-defined order.
        String[] keys = new String[keyset.size()];
        keyset.toArray(keys);
        for (String key : keys) {
            if (!key.startsWith(GLOBAL_KEY_PREFIX)) {
                throw new RuntimeException("Illegal entry key:" + key);
            }
        }

        int[] orders = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            orders[i] = Integer.parseInt(keys[i].substring(GLOBAL_KEY_PREFIX.length()));
        }
        Arrays.sort(orders);

        for (int order : orders) {
            String propFile = propFiles.getProperty(GLOBAL_KEY_PREFIX + order);
            logger.debug("Load module property file[" + order + "]:" + propFile);
            appProp.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propFile));
        }
    }

    /**
     * Open a input stream from classpath or filepath.
     */
    private InputStream openInputStream(String propFile) throws Exception {
        if (propFile.startsWith(RESOURCE_CLASSPATH)) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                // support for jre1.3 or below
                cl = this.getClass().getClassLoader();
            }
            return cl.getResourceAsStream(extract(propFile));
        } else if (propFile.startsWith(RESOURCE_FILE)) {
            File file = new File(extract(propFile));
            return new FileInputStream(file);
        } else {
            throw new IllegalStateException("Unsupport resource type: " + propFile);
        }
    }

    private String extract(String propFile) {
        int index = propFile.indexOf(RESOURCE_DELIM);
        if (index == -1) {
            throw new IllegalStateException("Wrong format of properties file path:" + propFile);
        }
        return propFile.substring(index + 1);
    }

}
