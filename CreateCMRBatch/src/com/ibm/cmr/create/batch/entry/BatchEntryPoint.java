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

import org.apache.commons.lang3.StringUtils;
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

  public static String currentContextName;

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
    System.out.println("CMR Home Dir: " + System.getProperty("cmr.home"));
    System.out.println("Initializing Log4J for Request CMR...");

    // PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource(log4jFile));

    logger = Logger.getLogger(CmrContextListener.class);
    logger.debug("Log4j Inititialized.");
    System.out.println("Log4j Inititialized.");

    System.out.println("Initializing System Configuration...");
    logger.debug("Initializing System Configuration...");
    try {
      SystemConfiguration.refresh();
      logger.debug("System Configuration initialized.");
      System.out.println("System Configuration initialized.");
    } catch (Exception e1) {
      logger.error("Error in initializing System Configuration", e1);
      System.err.println("System Configuration initialized.");
    }
    String skipBatch = SystemConfiguration.getValue("SKIP_BATCH_APPS", "N");
    if ("Y".equals(skipBatch)) {
      throw new CmrException(new Exception("Batches are skipped on this environment."));
    }

    logger.debug("Initializing JPA Manager...");
    System.out.println("Initializing JPA Manager...");
    JpaManager.init();
    logger.debug("JPA Manager Inititialized.");
    System.out.println("JPA Manager Inititialized.");

    JpaManager.setDefaultUnitName(DEFAULT_BATCH_PERSISTENCE_UNIT);
    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      logger.debug("Initializing Externalized Queries...");
      System.out.println("Initializing Externalized Queries...");

      try {
        ExternalizedQuery.refresh();
        logger.debug("Externalized Queries initialized.");
        System.out.println("Externalized Queries initialized.");

      } catch (Exception e1) {
        logger.error("Error in initializing Externalized Queries", e1);
        System.out.println("Error in initializing Externalized Queries");

      }

      logger.debug("Initializing System Parameters...");
      System.out.println("Initializing System Parameters...");

      try {
        SystemParameters.refresh(entityManager);
      } catch (Exception e1) {
        logger.error("Error in initializing system parameters", e1);
        System.out.println("Error in initializing system parameters.");

      }
      if (!shouldBatchRun(batchAppName)) {
        throw new CmrException(new Exception("XRUN param for " + batchAppName + " caused execution to stop."));
      }

      logger.debug("Initializing Message Util...");
      System.out.println("Initializing Message Util...");

      try {
        MessageUtil.refresh();
        logger.debug("Message Util initialized.");
        System.out.println("Message Util initialized.");

      } catch (Exception e1) {
        logger.error("Error in initializing Message Util", e1);
        System.out.println("Error in initializing Message Util");

      }

      logger.debug("Initializing SBO Filter Util...");
      System.out.println("Initializing SBO Filter Util...");

      try {
        SBOFilterUtil.refresh();
        logger.debug("SBO Filter Util initialized.");
        System.out.println("SBO Filter Util initialized.");

      } catch (Exception e1) {
        logger.error("Error in initializing SBO Filter Util", e1);
        System.out.println("Error in initializing SBO Filter Util");
      }

      logger.debug("Initializing MQ Util...");
      System.out.println("Initializing MQ Util...");
      try {
        MQProcessUtil.refresh();
        logger.debug("MQ Util initialized.");
        System.out.println("MQ Util initialized.");
      } catch (Exception e1) {
        logger.error("Error in initializing MQ Util", e1);
        System.out.println("Error in initializing MQ Util");
      }

      if (initUI) {
        logger.debug("Initializing UI Manager...");
        System.out.println("Initializing UI Manager...");
        try {
          UIMgr.refresh();
          logger.debug("UI Manager initialized.");
          System.out.println("UI Manager initialized.");
        } catch (Exception e1) {
          logger.error("Error in initializing UI Manager", e1);
          System.out.println("Error in initializing UI Manager");
        }

        logger.debug("Initializing Page Manager...");
        System.out.println("Initializing Page Manager...");
        try {
          PageManager.init(entityManager);
          logger.debug("Page Manager initialized.");
          System.out.println("Page Manager initialized.");
        } catch (Exception e) {
          logger.error("Error in initializing PageManager", e);
          System.out.println("Error in initializing PageManager");
        }
      }
      String DBTZ = SystemUtil.getDBTimezone(entityManager);
      TimeZone.setDefault(TimeZone.getTimeZone(DBTZ));
      Timestamp ts = SystemUtil.getCurrentTimestamp();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
      logger.info("Current DB time: " + sdf.format(ts));
      System.out.println("Current DB time: " + sdf.format(ts));

    } finally {
      entityManager.close();
    }

    System.out.println("Batch Context initialized...");
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
    currentContextName = contextName;
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
    if ("FILTER".equals(paramValue)) {
      logger.info("XRUN param is set to FILTER, running...");
      return true;
    }
    boolean run = paramValue.equalsIgnoreCase(environment);
    if (!run) {
      logger.warn("XRUN param for " + contextName + " set to " + paramValue + ", currently: " + environment + ". Skipping execution.");
    } else {
      logger.info("XRUN param matches environment, running...");
    }
    return run;
  }

}
