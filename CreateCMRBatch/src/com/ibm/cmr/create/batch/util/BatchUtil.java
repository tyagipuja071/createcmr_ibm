/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.util.SystemParameters;

/**
 * Retrieves properties from batch-props.properties<br>
 * Contains other utility functions for the batch
 * 
 * @author Jeffrey Zamora
 * 
 */
public class BatchUtil {

  private static final Logger LOG = Logger.getLogger(BatchUtil.class);

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("batch-props");

  /**
   * Gets a property from batch-props.properties
   * 
   * @param key
   * @return
   */
  public static String getProperty(String key) {
    if (BUNDLE.containsKey(key)) {
      return BUNDLE.getString(key);
    }
    return null;
  }

  /**
   * Gets the application ID to use when connecting to the services for the
   * specific CMR issuing country
   * 
   * @param cmrIssuingCntry
   * @return
   */
  public static String getAppId(String cmrIssuingCntry) {
    return getProperty("appId." + cmrIssuingCntry);
  }

  /**
   * Returns the list of keys on the property file that starts with the given
   * prefix
   * 
   * @param keySuffix
   * @return
   */
  public static List<String> getKeysWithPrefix(String keyPrefix) {
    List<String> keys = new ArrayList<String>();
    for (String key : BUNDLE.keySet()) {
      if (key.startsWith(keyPrefix)) {
        keys.add(key);
      }
    }
    return keys;
  }

  public static List<String> getAsList(String key) {
    String prop = getProperty(key);
    if (!StringUtils.isEmpty(prop)) {
      return Arrays.asList(prop.split(","));
    }
    return new ArrayList<>();
  }

  /**
   * Global check for environment for batch exclusion.
   * 
   * @param admin
   * @return
   */
  public static boolean excludeForEnvironment(EntityManager entityManager, RequestData requestData) {
    Admin admin = requestData.getAdmin();
    boolean exclude = excludeForEnvironment(entityManager, admin);
    if (exclude) {
      entityManager.detach(requestData.getData());
      entityManager.detach(requestData.getScorecard());
      List<Addr> addresses = requestData.getAddresses();
      for (Addr addr : addresses) {
        entityManager.detach(addr);
      }
    }
    return exclude;
  }

  public static boolean excludeForEnvironment(EntityManager entityManager, long reqId) {
    boolean filter = "Y".equals(getProperty("env.filter"));
    if (!filter) {
      // no retrieval of admin if no filter
      return false;
    }
    AdminPK pk = new AdminPK();
    pk.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, pk);
    if (admin != null) {
      return excludeForEnvironment(entityManager, admin);
    } else {
      return false;
    }

  }

  /**
   * Global check for environment for batch exclusion.
   * 
   * @param admin
   * @return
   */
  public static boolean excludeForEnvironment(EntityManager entityManager, Admin admin) {
    boolean filter = "Y".equals(SystemParameters.getString("XRUN.FILTER"));
    if (filter) {
      String sysType = SystemConfiguration.getValue("SYSTEM_TYPE");
      if (!StringUtils.isBlank(sysType)) {
        sysType = sysType.substring(0, 1);
        String env = admin.getWaitInfoInd();
        LOG.debug("Running Request " + admin.getId().getReqId() + " checks for filter: Type = " + sysType + " Record: " + env);
        if (StringUtils.isBlank(env)) {
          env = "";
        }
        boolean exclude = !sysType.equals(env);
        if (exclude) {
          LOG.debug(" - Request " + admin.getId().getReqId() + " excluded, env was '" + env + "'");
          entityManager.detach(admin);
        }
        return exclude;
      }
    }
    return false;
  }

}
