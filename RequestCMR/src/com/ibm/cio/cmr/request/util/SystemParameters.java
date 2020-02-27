/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.SystParameters;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * Class to handle system parameters from CREQCMR.SYST_PARAMETERS
 * 
 * @author Jeffrey Zamora
 * 
 */
public class SystemParameters {

  private static final Logger LOG = Logger.getLogger(SystemParameters.class);

  private static Map<String, String> values = new HashMap<String, String>();

  
  public static void logUserAccess(String applicationName, String userId) {
    EntityTransaction tx = null;
    EntityManager em = JpaManager.getEntityManager();
    try {
      tx = em.getTransaction();
      tx.begin();

      String sql = createAppLoginSQL(applicationName, userId);

      LOG.trace("Updating app usage for " + userId);

      Query query = em.createNativeQuery(sql);
      query.executeUpdate();

      tx.commit();
    } catch (Exception e) {
      LOG.warn("Cannot log user login", e);
      if (tx != null) {
        tx.rollback();
      }
    } finally {
      em.clear();
      em.close();
    }
  }

  private static String createAppLoginSQL(String applicationName, String userId) {
    StringBuilder sql = new StringBuilder();

    sql.append(" merge into CMMA.APP_LOGINS a1");
    sql.append(" using (select 'CreateCMR' APPLICATION_NAME, '" + userId.toLowerCase().trim() + "' USER_ID ");
    sql.append("        from SYSIBM.SYSDUMMY1 ) a2");
    sql.append("    on (a1.APPLICATION_NAME = a2.APPLICATION_NAME");
    sql.append("   and a1.USER_ID = a2.USER_ID)");
    sql.append(" when not matched then");
    sql.append("   insert (APPLICATION_NAME, USER_ID, LAST_LOGIN_TS) values (a2.APPLICATION_NAME, a2.USER_ID, current timestamp)");
    sql.append(" when matched then");
    sql.append("   update set LAST_LOGIN_TS = current timestamp ");

    return sql.toString();
  }

  public static void refresh() {
    EntityManager em = JpaManager.getEntityManager();
    try {
      refresh(em);
    } finally {
      em.clear();
      em.close();
    }
  }

  public static void refresh(EntityManager em) {
    LOG.info("Initializing system parameters.");
    values.clear();
    String sql = ExternalizedQuery.getSql("SYSTEMPARAMETERS.INIT");
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setForReadOnly(true);
    List<SystParameters> params = query.getResults(SystParameters.class);
    if (params != null) {
      for (SystParameters param : params) {
        values.put(param.getId().getParameterCd(), param.getParameterValue());
      }
    }
    LOG.info(values.size() + " parameters retrieved.");
  }

  public static String getString(String code) {
    return values.get(code);
  }

  public static int getInt(String code) {
    String val = values.get(code);
    if (val == null) {
      return -1;
    }
    try {
      return Integer.parseInt(val);
    } catch (Exception e) {
      return -1;
    }
  }

  public static List<String> getList(String code) {
    String val = values.get(code);
    if (val != null) {
      return Arrays.asList(val.split(","));
    }
    return Collections.emptyList();
  }

  public static void update(String code, String value) {
    values.put(code, value);
  }
}
