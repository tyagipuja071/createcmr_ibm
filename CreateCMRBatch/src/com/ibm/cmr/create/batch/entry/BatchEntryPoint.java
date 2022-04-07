/**
 * 
 */
package com.ibm.cmr.create.batch.entry;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.ui.UIMgr;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SBOFilterUtil;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
public abstract class BatchEntryPoint {

  private static Logger logger = null;

  public static final String DEFAULT_BATCH_PERSISTENCE_UNIT = "BATCH";

  protected static void initContext(String batchAppName) throws CmrException {
    initContext(batchAppName, false);
  }

  protected static void initContext(String batchAppName, boolean initUI) throws CmrException {
    System.out.println("Initializing batch context");
    System.setProperty("BATCH_APP", batchAppName);
    // start entity manager
    startBatchContext("log4j2-batch.xml", batchAppName, initUI);
  }

  protected static void initPlainContext(String batchAppName) throws CmrException {
    System.out.println("Initializing plain batch context");
    System.setProperty("BATCH_APP", batchAppName);
    // start entity manager
    startPlainBatchContext("log4j2-batch.xml");
  }

  private static void startBatchContext(String log4jFile, String batchAppName, boolean initUI) throws CmrException {
    URL url = BatchEntryPoint.class.getClassLoader().getResource(log4jFile);
    File log4jPath = null;
    try {
      log4jPath = new File(url.toURI());
      System.setProperty("log4j2.configurationFile", log4jPath.getAbsolutePath());
    } catch (URISyntaxException e2) {
      e2.printStackTrace();
    }

    ConfigUtil.initFromBatch();
    System.err.println("CMR Home Dir: " + System.getProperty("cmr.home"));
    System.err.println("Initializing Log4J for Request CMR...");

    // PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource(log4jFile));

    logger = Logger.getLogger(CmrContextListener.class);
    logger.debug("Log4j Inititialized.");

    logger.debug("Initializing System Configuration...");
    try {
      SystemConfiguration.refresh();
      logger.debug("System Configuration initialized.");
    } catch (Exception e1) {
      logger.error("Error in initializing System Configuration", e1);
    }
    String skipBatch = SystemConfiguration.getValue("SKIP_BATCH_APPS", "N");
    if ("Y".equals(skipBatch)) {
      throw new CmrException(new Exception("Batches are skipped on this environment."));
    }

    logger.debug("Initializing JPA Manager...");
    JpaManager.init();
    logger.debug("JPA Manager Inititialized.");
    JpaManager.setDefaultUnitName(DEFAULT_BATCH_PERSISTENCE_UNIT);
    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      logger.debug("Initializing Externalized Queries...");
      try {
        ExternalizedQuery.refresh();
        logger.debug("Externalized Queries initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing Externalized Queries", e1);
      }

      logger.debug("Initializing System Parameters...");
      try {
        SystemParameters.refresh(entityManager);
      } catch (Exception e1) {
        logger.error("Error in initializing system parameters", e1);
      }
      if (!shouldBatchRun(batchAppName)) {
        throw new CmrException(new Exception("XRUN param for " + batchAppName + " caused execution to stop."));
      }

      logger.debug("Initializing Message Util...");
      try {
        MessageUtil.refresh();
        logger.debug("Message Util initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing Message Util", e1);
      }

      logger.debug("Initializing SBO Filter Util...");
      try {
        SBOFilterUtil.refresh();
        logger.debug("SBO Filter Util initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing SBO Filter Util", e1);
      }

      logger.debug("Initializing MQ Util...");
      try {
        MQProcessUtil.refresh();
        logger.debug("MQ Util initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing MQ Util", e1);
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

  private static void startPlainBatchContext(String log4jFile) throws CmrException {
    System.err.println("Initializing Log4J for Request CMR...");
    System.setProperty("log4j2.configurationFile", "log4j2-batch.xml");
    // PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource(log4jFile));

    logger = Logger.getLogger(CmrContextListener.class);
    logger.debug("Log4j Inititialized.");

    logger.debug("Initializing System Configuration...");
    try {
      SystemConfiguration.refresh();
      logger.debug("System Configuration initialized.");
    } catch (Exception e1) {
      logger.error("Error in initializing System Configuration", e1);
    }
    String skipBatch = SystemConfiguration.getValue("SKIP_BATCH_APPS", "N");
    if ("Y".equals(skipBatch)) {
      throw new CmrException(new Exception("Batches are skipped on this environment."));
    }

  }

  /**
   * Checks if batch should run on this environment
   * 
   * @param contextName
   * @return
   */
  private static boolean shouldBatchRun(String contextName) {
    logger.info("Checking XRUN param for this application..");
    String environment = SystemConfiguration.getValue("SYSTEM_TYPE");
    if (StringUtils.isBlank(environment)) {
      // system type is not specified, run all the time
      logger.info("No environment specified, running..");
      return true;
    }
    String paramValue = SystemParameters.getString("XRUN." + contextName);
    if (StringUtils.isBlank(paramValue)) {
      // value is not specified, run all the time
      logger.info("No XRUN param specified, running..");
      return true;
    }

    // match param with environment
    boolean run = paramValue.equalsIgnoreCase(environment);
    if (!run) {
      logger.warn("XRUN param for " + contextName + " set to " + paramValue + ", currently: " + environment + ". Skipping execution.");
    }
    logger.info("XRUN param matches environment, running...");
    return run;
  }

}
