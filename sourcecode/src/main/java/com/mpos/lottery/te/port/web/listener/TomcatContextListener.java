package com.mpos.lottery.te.port.web.listener;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Refer to http://tomcat.apache.org/tomcat-6.0-doc/manager-howto.html Access http://localhost:8080/manager/jmxproxy/ to
 * get all MBeans information.
 */
public class TomcatContextListener implements ServletContextListener {
    private static Log logger = LogFactory.getLog(TomcatContextListener.class);

    public void contextDestroyed(ServletContextEvent ctx) {
        MLotteryContext context = MLotteryContext.getInstance();
        int wait = context.getInt("seconds.beforeshutdown");
        logger.debug("Start to shutdown servlet context, wait " + wait + " secodes.");
        try {
            Thread.sleep(wait * 1000);
        } catch (Exception e) {
        }

        // This manually deregisters JDBC driver, which prevents Tomcat 6.0.24
        // or above from complaining about memory leaks wrto this class
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.info(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                logger.warn(String.format("Error deregistering driver %s", driver), e);
            }

        }
        logger.debug("Shutdown servlet context.");
    }

    public void contextInitialized(ServletContextEvent ctx) {
        // initialJMX
    }

    private void initialJMX() {
        MBeanServer mbserver = null;

        ArrayList mbservers = MBeanServerFactory.findMBeanServer(null);

        if (mbservers.size() > 0) {
            mbserver = (MBeanServer) mbservers.get(0);
        }

        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }
        logger.debug("Create new MBean server:" + mbserver + ",count of mbeans:" + mbserver.getMBeanCount());
        try {
            // Access tomcat's MBean
            Hashtable<String, String> prop = new Hashtable<String, String>();
            prop.put("type", "Connector");
            ObjectName objectName = new ObjectName("Catalina:type=Connector,*");
            Set<ObjectName> objectNames = mbserver.queryNames(objectName, null);
            for (ObjectName name : objectNames) {
                logger.debug(name.getDomain() + ":" + name.getKeyProperty("type"));
                logger.debug(mbserver.getAttribute(name, "port"));
                logger.debug(mbserver.getAttribute(name, "schema"));
            }
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }
}
