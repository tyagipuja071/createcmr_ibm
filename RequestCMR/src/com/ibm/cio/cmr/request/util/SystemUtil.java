package com.ibm.cio.cmr.request.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.RequestChangeLog;
import com.ibm.cio.cmr.request.entity.RequestChangeLogPK;
import com.ibm.cio.cmr.request.entity.SystParameters;
import com.ibm.cio.cmr.request.entity.SystParametersPK;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * System Utility class
 * 
 * @author Jeffrey Zamora
 * 
 */
public class SystemUtil {

  private static final Logger LOG = Logger.getLogger(SystemUtil.class);

  // private static Date servertime = null;

  private static ThreadLocal<EntityManager> current = new ThreadLocal<>();

  public static synchronized void setManager(EntityManager entityManager) {
    current.set(entityManager);
  }

  @SuppressWarnings("unchecked")
  /**
   * Gets the database timestamp
   * 
   * @return
   */
  public static Timestamp getCurrentTimestamp() {
    try {
      // only query once the db's timestamp. use the offset to calculate
      // current time
      boolean own = false;
      EntityManager em = null;
      if (current.get() != null) {
        em = current.get();
      }
      if (em == null || !em.isOpen()) {
        own = true;
        em = JpaManager.getEntityManager();
      }
      try {
        String sql = "select current timestamp, '1' from sysibm.sysdummy1";
        Query q = em.createNativeQuery(sql);
        List<Object[]> results = q.getResultList();
        if (results != null && results.size() > 0) {
          Timestamp ts = (Timestamp) results.get(0)[0];
          return ts;
          // servertime = new Date();
        }
      } finally {
        if (own) {
          em.clear();
          em.close();
        }
      }
    } catch (Exception e) {
      LOG.error("Error in getting time.", e);
    }
    return new Timestamp(new Date().getTime());

  }

  /**
   * Logs system admin actions to {@link RequestChangeLog}
   * 
   * @param entityManager
   * @param user
   * @param table
   * @param action
   * @param oldValue
   * @param newValue
   */
  public static void logSystemAdminAction(EntityManager entityManager, AppUser user, String table, String action, String field, String country,
      String oldValue, String newValue) {
    try {
      LOG.debug("Logging system admin action for " + user.getIntranetId() + "(" + user.getBluePagesName() + ") - " + table + "/" + action);
      RequestChangeLog log = new RequestChangeLog();
      RequestChangeLogPK pk = new RequestChangeLogPK();
      pk.setRequestId(-1);
      pk.setTablName(table);
      pk.setAddrTyp(country);
      pk.setChangeTs(getCurrentTimestamp());
      pk.setFieldName(field.length() > 30 ? field.substring(0, 30) : field);
      log.setId(pk);
      log.setAction(action);
      log.setAddrSequence(" ");
      log.setOldValue(oldValue);
      log.setNewValue(newValue);
      log.setRequestStatus(" ");
      log.setUserId(user.getIntranetId());

      entityManager.persist(log);
      entityManager.flush();

    } catch (Exception e) {
      LOG.error("An error occurred while creating system admin log", e);
    }

  }

  /**
   * Gets the database timestamp
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Timestamp getActualTimestamp() {
    try {
      // only query once the db's timestamp. use the offset to calculate
      // current time
      EntityManager em = JpaManager.getEntityManager();
      try {
        String sql = "select current timestamp, '1' from sysibm.sysdummy1";
        Query q = em.createNativeQuery(sql);
        List<Object[]> results = q.getResultList();
        if (results != null && results.size() > 0) {
          Timestamp ts = (Timestamp) results.get(0)[0];
          return ts;
        }
      } finally {
        em.clear();
        em.close();
      }
    } catch (Exception e) {
      LOG.error("Error in getting time.", e);
    }
    return getCurrentTimestamp();
  }

  public static long getNextID(EntityManager entityManager, String mandt, String idType) throws CmrException, SQLException {
    return getNextID(entityManager, mandt, idType, "CREQCMR");
  }

  /**
   * 
   * @param mandt
   * @param idType
   * @return
   * @throws CmrException
   * @throws SQLException
   */
  public static synchronized long getNextID(EntityManager entityManager, String mandt, String idType, String schema)
      throws CmrException, SQLException {

    String schemaToUse = schema != null ? schema + "." : "";
    String sql = "select NEXT VALUE for " + schemaToUse + "SEQ_" + idType + ", '' from SYSIBM.SYSDUMMY1";
    PreparedQuery query = new PreparedQuery(entityManager, sql);

    List<Object[]> results = query.getResults(1);

    if (results != null && results.size() > 0) {
      LOG.debug(idType + " Value: " + results.get(0)[0]);
      return (long) results.get(0)[0];
    } else {
      throw new CmrException(MessageUtil.ERROR_GENERAL);
    }

  }

  public static FindCMRResultModel findCMRs(String cmrNo, String cmrIssuingCntry, int resultRows) throws CmrException {
    return findCMRs(cmrNo, cmrIssuingCntry, resultRows, null);
  }

  public static FindCMRResultModel findCMRs(String cmrNo, String cmrIssuingCntry, int resultRows, String searchCountry) throws CmrException {
    return findCMRs(cmrNo, cmrIssuingCntry, resultRows, searchCountry, null);
  }

  /**
   * Interfaces with Find CMR and gets the records
   * 
   * @param cmrNo
   * @param resultRows
   * @return
   * @throws CmrException
   */
  public static FindCMRResultModel findCMRs(String cmrNo, String cmrIssuingCntry, int resultRows, String searchCountry, String overrideParams)
      throws CmrException {
    String findCMRUrl = SystemConfiguration.getValue("FIND_CMR_URL");
    if (findCMRUrl == null) {
      throw new CmrException(MessageUtil.ERROR_NO_FIND_CMR_DEFINED);
    }

    String countryToUse = StringUtils.isEmpty(searchCountry) ? cmrIssuingCntry : searchCountry;
    findCMRUrl += "/getCMRData.json?customerNumber=" + cmrNo + "&issuingCountryCode=" + countryToUse + "&resultRows=" + resultRows;

    if (!StringUtils.isBlank(overrideParams)) {
      findCMRUrl = SystemConfiguration.getValue("FIND_CMR_URL");
      findCMRUrl += "/getCMRData.json?" + overrideParams + "&issuingCountryCode=" + countryToUse + "&resultRows=" + resultRows;
    }
    String credentials = "&svcId=" + SystemConfiguration.getSystemProperty("service.id") + "&svcPwd="
        + SystemConfiguration.getSystemProperty("service.password");

    String showProspects = "&showProspectCMRS=Y";

    findCMRUrl += credentials + showProspects;

    // piece that adds the order block code
    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);
    if (geoHandler != null && geoHandler.retrieveInvalidCustomersForCMRSearch(cmrIssuingCntry)) {
      findCMRUrl += "&includeOrdBlk93=Y";
    }

    return executeFindCMR(findCMRUrl, cmrIssuingCntry, searchCountry);
  }

  /**
   * Interfaces with Find CMR and gets the pool records
   * 
   * @param cmrNo
   * @param resultRows
   * @return
   * @throws CmrException
   */
  public static FindCMRResultModel findCMRs(String cmrNo, String cmrIssuingCntry, int resultRows, String searchCountry, boolean isPoolRecord)
      throws CmrException {
    String findCMRUrl = SystemConfiguration.getValue("FIND_CMR_URL");
    if (findCMRUrl == null) {
      throw new CmrException(MessageUtil.ERROR_NO_FIND_CMR_DEFINED);
    }

    String countryToUse = StringUtils.isEmpty(searchCountry) ? cmrIssuingCntry : searchCountry;
    findCMRUrl += "/getCMRData.json?customerNumber=" + cmrNo + "&issuingCountryCode=" + countryToUse + "&resultRows=" + resultRows;

    String credentials = "&svcId=" + SystemConfiguration.getSystemProperty("service.id") + "&svcPwd="
        + SystemConfiguration.getSystemProperty("service.password");

    String showProspects = "&showProspectCMRS=Y";

    findCMRUrl += credentials + showProspects;

    // piece that adds the order block code
    findCMRUrl += "&includeOrdBlk93=Y";

    return executeFindCMR(findCMRUrl, cmrIssuingCntry, searchCountry);
  }

  public static FindCMRResultModel findCMRsAltLang(String cmrIssuingCntry, int resultRows, String name, String street, String city,
      String extraParams) throws CmrException, UnsupportedEncodingException {
    String findCMRUrl = SystemConfiguration.getValue("FIND_CMR_URL");
    if (findCMRUrl == null) {
      throw new CmrException(MessageUtil.ERROR_NO_FIND_CMR_DEFINED);
    }

    findCMRUrl += "/getCMRData.json?issuingCountryCode=" + cmrIssuingCntry + "&resultRows=" + resultRows;

    if (!StringUtils.isBlank(name)) {
      findCMRUrl += "&internationalName=" + URLEncoder.encode(name, "UTF-8");
    }

    if (!StringUtils.isBlank(street)) {
      findCMRUrl += "&internationalAddress=" + URLEncoder.encode(street, "UTF-8");
    }
    if (!StringUtils.isBlank(city)) {
      findCMRUrl += "&internationalCity=" + URLEncoder.encode(city, "UTF-8");
    }

    findCMRUrl += "&altLangNameSearchField=A";

    if (extraParams != null) {
      findCMRUrl += "&" + extraParams;
    }

    String credentials = "&svcId=" + SystemConfiguration.getSystemProperty("service.id") + "&svcPwd="
        + SystemConfiguration.getSystemProperty("service.password");

    String showProspects = "&showProspectCMRS=Y";

    findCMRUrl += credentials + showProspects;

    System.out.println(findCMRUrl);
    return executeFindCMR(findCMRUrl, cmrIssuingCntry, null);
  }

  private static FindCMRResultModel executeFindCMR(String findCMRUrl, String cmrIssuingCntry, String searchCountry) throws CmrException {
    try {
      URL findCMR = new URL(findCMRUrl);
      LOG.debug("Connecting to find CMR " + findCMRUrl);
      HttpURLConnection conn = (HttpURLConnection) findCMR.openConnection();
      conn.setDoInput(true);
      conn.setConnectTimeout(1000 * 60 * 2); // 2 mins
      InputStream is = conn.getInputStream();
      StringBuilder records = new StringBuilder();
      try {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        try {
          BufferedReader br = new BufferedReader(isr);
          try {
            String line = null;
            while ((line = br.readLine()) != null) {
              records.append(line);
            }
          } finally {
            br.close();
          }
        } finally {
          isr.close();
        }
      } finally {
        is.close();
      }

      LOG.debug(" - FindCMR inputstream read..");

      is = conn.getErrorStream();
      StringBuilder error = new StringBuilder();
      if (is != null) {
        try {
          InputStreamReader isr = new InputStreamReader(is, "UTF-8");
          try {
            BufferedReader br = new BufferedReader(isr);
            try {
              String line = null;
              while ((line = br.readLine()) != null) {
                error.append(line);
              }
            } finally {
              br.close();
            }
          } finally {
            isr.close();
          }
        } finally {
          is.close();
        }
      }

      conn.disconnect();

      LOG.debug(" - FindCMR error stream read..");

      if (error.length() > 0) {
        throw new CmrException(MessageUtil.ERROR_FIND_CMR_ERROR);
      }

      FindCMRResultModel results = null;
      try {
        ObjectMapper mapper = new ObjectMapper();
        results = mapper.readValue(records.toString(), FindCMRResultModel.class);
        if (results != null && !results.isSuccess()) {
          LOG.error("Error in connecting to Find CMR: " + results.getMessage());
          throw new CmrException(MessageUtil.ERROR_FIND_CMR_ERROR);
        }

        if (!StringUtils.isEmpty(searchCountry) && results != null && results.getItems() != null) {
          LOG.debug("Reversing CMR isssuing country of search to " + cmrIssuingCntry);
          for (FindCMRRecordModel record : results.getItems()) {
            record.setCmrIssuedBy(cmrIssuingCntry);
          }
        }
        return results;
      } catch (Exception e) {
        throw new CmrException(MessageUtil.ERROR_FIND_CMR_ERROR);
      }
    } catch (Exception e) {
      LOG.error("Find CMR Connection", e);
      e.printStackTrace();
      throw new CmrException(MessageUtil.ERROR_FIND_CMR_ERROR);
    }
  }

  public static String getDBTimezone() {
    EntityManager em = JpaManager.getEntityManager();
    try {
      return getDBTimezone(em);
    } finally {
      em.close();
    }
  }

  public static String getDBTimezone(EntityManager em) {

    String sql = "select current timezone from SYSIBM.SYSDUMMY1";
    PreparedQuery query = new PreparedQuery(em, sql);
    BigDecimal dbTimezone = query.getSingleResult(BigDecimal.class);
    DecimalFormat DF = new DecimalFormat("#");
    String tzString = DF.format(dbTimezone);
    // System.out.println("DB Raw Timezone: " + tzString);
    String hours = "";
    if (tzString.startsWith("-") && tzString.length() == 6) {
      hours = tzString.substring(0, 2);
    } else if (tzString.startsWith("-") && tzString.length() == 7) {
      hours = tzString.substring(0, 3);
    } else if (tzString.length() == 6) {
      hours = "+" + tzString.substring(0, 2);
    } else if (tzString.length() == 5) {
      hours = "+" + tzString.substring(0, 1);
    }
    // System.out.println("DB Raw Timezone: GMT" + hours);
    return "GMT" + hours;
  }

  public static String getISOCountryCode(String systemLocCode) throws Exception {
    String mandt = SystemConfiguration.getValue("MANDT");
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    // try US CMR DB first
    String sql = ExternalizedQuery.getSql("UTIL.GETISOCODE");
    QueryRequest query = new QueryRequest();
    query.setRows(1);
    query.addField("ISO_CNTRY_CD");
    query.addField("DUMMY");
    sql = StringUtils.replace(sql, ":KATR6", "'" + systemLocCode + "'");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    query.setSql(sql);

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);

    QueryResponse response = client.executeAndWrap(QueryClient.RDC_APP_ID, query, QueryResponse.class);
    if (response.isSuccess()) {
      Map<String, Object> record = response.getRecords().get(0);
      return (String) record.get("ISO_CNTRY_CD");
    }
    return null;
  }

  public static String getIssuingCountry(Object entity) {
    try {
      Class<?> c = entity.getClass();
      Field cntryField = null;
      try {
        cntryField = c.getDeclaredField("cmrIssuingCntry");
      } catch (Exception e) {
      }
      if (cntryField == null) {
        try {
          cntryField = c.getDeclaredField("issuingCntry");
        } catch (Exception e) {
        }
      }
      if (cntryField == null) {
        try {
          cntryField = c.getDeclaredField("cntryCd");
        } catch (Exception e) {
        }
      }
      if (cntryField == null) {
        try {
          cntryField = c.getDeclaredField("locCd");
        } catch (Exception e) {
        }
      }
      if (cntryField != null) {
        cntryField.setAccessible(true);
        return (String) cntryField.get(entity);
      } else {
        try {
          Field id = c.getDeclaredField("id");
          if (id != null) {
            id.setAccessible(true);
            return getIssuingCountry(id.get(entity));
          }
        } catch (Exception e) {
        }
      }
      return "?";
    } catch (Exception e) {
      return "?";
    }
  }

  public static void main(String[] args) throws NoSuchFieldException, SecurityException {
    SystParameters t = new SystParameters();
    SystParametersPK pk = new SystParametersPK();
    pk.setParameterCd("XXX");
    t.setId(pk);
    System.out.println(getRelevantKey(t));
  }

  public static String getFieldId(Object entity) {
    try {
      Class<?> c = entity.getClass();
      Field idField = null;
      try {
        idField = c.getDeclaredField("fieldId");
      } catch (Exception e) {
      }
      if (idField != null) {
        idField.setAccessible(true);
        return (String) idField.get(entity);
      } else {
        try {
          Field id = c.getDeclaredField("id");
          if (id != null) {
            id.setAccessible(true);
            return getFieldId(id.get(entity));
          }
        } catch (Exception e) {
        }
      }
      return "?";
    } catch (Exception e) {
      return "?";
    }
  }

  public static String getRelevantKey(Object entity) {
    Class<?> c = entity.getClass();
    try {

      Field id = c.getDeclaredField("id");
      if (id != null) {
        id.setAccessible(true);
        Object idObj = id.get(entity);
        if (idObj != null) {
          for (Field field : idObj.getClass().getDeclaredFields()) {
            System.out.println(field.getName());
            if (!Arrays.asList("cmrIssuingCntry", "locCd", "issuingCntry", "cntryCd").contains(field.getName())
                && String.class.equals(field.getType())) {
              field.setAccessible(true);
              return (String) field.get(idObj);
            }
          }
        }
      }
    } catch (Exception e) {
    }
    return "?";
  }

  /**
   * Stores the current error message on the user's session
   * 
   * @param t
   */
  public static void storeToSession(HttpServletRequest request, Throwable t) {
    if (t == null) {
      LOG.debug("Cleared current error.");
      request.getSession().removeAttribute(CmrConstants.SESSION_ERROR_KEY);
    } else {
      LOG.debug("Stored current error.");
      request.getSession().setAttribute(CmrConstants.SESSION_ERROR_KEY, t);
    }
  }

  /**
   * Checks the current setting for the automation for the given country
   * 
   * @param entityManager
   * @param issuingCntry
   * @return
   */
  public static String getAutomationIndicator(EntityManager entityManager, String issuingCntry) {
    String sql = ExternalizedQuery.getSql("AUTOMATION.IS_AUTOMATED_PROCESSING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", issuingCntry);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  /**
   * returns the dummy default date as "0001-01-01".
   * 
   * @return
   * @throws Exception
   */
  public static Date getDummyDefaultDate() {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date dateToSave = null;
    try {
      dateToSave = sdf.parse("0001-01-01");
    } catch (Exception ex) {
      LOG.error(ex.getMessage() + "error parsing Dummy Default date.", ex);
    }
    return dateToSave;
  }
}
