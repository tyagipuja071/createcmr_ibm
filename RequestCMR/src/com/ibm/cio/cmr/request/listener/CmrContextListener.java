/**
 * 
 */
package com.ibm.cio.cmr.request.listener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.ui.UIMgr;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * Listener for Startup
 * 
 * @author Jeffrey Zamora
 * 
 */
@WebListener
public class CmrContextListener implements ServletContextListener, HttpSessionListener {

  private static Logger logger = null;

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
  }

  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    startCMRContext();
  }

  public static void startCMRContext() {
    startCMRContext("cmr-log4j.properties", true);
  }

  public static void startCMRContext(String log4jFile, boolean initUI) {

    ConfigUtil.init();

    System.err.println("CMR Home Dir: " + System.getProperty("cmr.home"));
    System.err.println("Initializing Log4J for Request CMR...");
    PropertyConfigurator.configure(ConfigUtil.getResourceStream(log4jFile));

    logger = Logger.getLogger(CmrContextListener.class);
    logger.debug("Log4j Inititialized.");

    logger.debug("Initializing JPA Manager...");
    JpaManager.init();
    logger.debug("JPA Manager Inititialized.");

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
      SystemParameters.refresh();
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
        PageManager.init();
        logger.debug("Page Manager initialized.");
      } catch (Exception e) {
        logger.error("Error in initializing PageManager", e);
      }
    }
    String DBTZ = SystemUtil.getDBTimezone();
    TimeZone.setDefault(TimeZone.getTimeZone(DBTZ));
    Timestamp ts = SystemUtil.getCurrentTimestamp();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    logger.info("Current DB time: " + sdf.format(ts));

    // disable for now
    // logger.info("Starting refresh thread.. Last Refresh : " +
    // SystemConfiguration.LAST_REFRESH_TIME);
    // RefreshJob refresh = new RefreshJob();
    // Thread tRefresh = new Thread(refresh);
    // tRefresh.setName("Refresh");
    // tRefresh.start();
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    if (session != null) {
      Object source = event.getSource();
      if (source != null) {
        logger.debug("Session ID: " + session.getId() + " destroyed as due to " + source.getClass().getName());
      }
      AppUser user = (AppUser) session.getAttribute(CmrConstants.SESSION_APPUSER_KEY);
      if (user != null) {
        logger.debug(
            "Session ID: " + session.getId() + " = Removing session attribute for " + user.getBluePagesName() + " (" + user.getIntranetId() + ")");
        session.removeAttribute(CmrConstants.SESSION_APPUSER_KEY);
      }
    }
  }

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    if (session != null) {
      logger.debug("Session ID: " + session.getId() + " created.");
    }
  }

}
