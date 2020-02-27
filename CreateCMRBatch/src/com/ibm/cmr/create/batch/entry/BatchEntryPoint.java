/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.ui.UIMgr;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
public abstract class BatchEntryPoint {

  private static Logger logger = null;

  protected static void initContext(String batchAppName) {
    initContext(batchAppName, false);
  }

  protected static void initContext(String batchAppName, boolean initUI) {
    System.setProperty("BATCH_APP", batchAppName);
    // start entity manager
    startBatchContext("batch-log4j.properties", initUI);
  }

  protected static void initPlainContext(String batchAppName) {
    System.setProperty("BATCH_APP", batchAppName);
    // start entity manager
    startPlainBatchContext("batch-log4j.properties");
  }

  private static void startBatchContext(String log4jFile, boolean initUI) {
    System.err.println("CMR Home Dir: " + System.getProperty("cmr.home"));
    System.err.println("Initializing Log4J for Request CMR...");
    PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource(log4jFile));

    logger = Logger.getLogger(CmrContextListener.class);
    logger.debug("Log4j Inititialized.");

    logger.debug("Initializing JPA Manager...");
    JpaManager.init();
    logger.debug("JPA Manager Inititialized.");

    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      logger.debug("Initializing System Configuration...");
      try {
        SystemConfiguration.refresh();
        logger.debug("System Configuration initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing System Configuration", e1);
      }

      logger.debug("Initializing Externalized Queries...");
      try {
        ExternalizedQuery.refresh();
        logger.debug("Externalized Queries initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing Externalized Queries", e1);
      }

      logger.debug("Initializing Message Util...");
      try {
        MessageUtil.refresh();
        logger.debug("Message Util initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing Message Util", e1);
      }

      logger.debug("Initializing MQ Util...");
      try {
        MQProcessUtil.refresh();
        logger.debug("MQ Util initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing MQ Util", e1);
      }

      logger.debug("Initializing System Parameters...");
      try {
        SystemParameters.refresh(entityManager);
        SystemConfiguration.LAST_REFRESH_TIME = SystemParameters.getString("LAST_REFRESH");
        logger.info("Last System Refresh " + SystemParameters.getString("LAST_REFRESH"));
      } catch (Exception e1) {
        logger.error("Error in initializing system parameters", e1);
      }

      if (initUI) {
        logger.debug("Initializing UI Manager...");
        try {
          UIMgr.refresh();
          logger.debug("UI Manager initialized.");
        } catch (Exception e1) {
          logger.error("Error in initializing UI Manager", e1);
        }

        logger.debug("Initializing Page Manager...");
        try {
          PageManager.init(entityManager);
          logger.debug("Page Manager initialized.");
        } catch (Exception e) {
          logger.error("Error in initializing PageManager", e);
        }
      }
      String DBTZ = SystemUtil.getDBTimezone(entityManager);
      TimeZone.setDefault(TimeZone.getTimeZone(DBTZ));
      Timestamp ts = SystemUtil.getCurrentTimestamp();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
      logger.info("Current DB time: " + sdf.format(ts));

    } finally {
      entityManager.close();
    }
  }

  private static void startPlainBatchContext(String log4jFile) {
    System.err.println("Initializing Log4J for Request CMR...");
    PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource(log4jFile));

    logger = Logger.getLogger(CmrContextListener.class);
    logger.debug("Log4j Inititialized.");

    logger.debug("Initializing System Configuration...");
    try {
      SystemConfiguration.refresh();
      logger.debug("System Configuration initialized.");
    } catch (Exception e1) {
      logger.error("Error in initializing System Configuration", e1);
    }

  }

}
