/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.bluepages.BPResults;
import com.ibm.bluepages.BluePages;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.swat.password.ReturnCode;
import com.ibm.swat.password.cwa2;
import com.ibm.swat.password.cwaapi;

/**
 * The Class BluePagesHelper.
 * 
 * @author Sandeep.Kanparthy May 16, 2011
 */
public class BluePagesHelper {

  /** The Constant LOG. */
  private static final Logger LOG = Logger.getLogger(BluePagesHelper.class);
  public static final String BLUEPAGES_KEY_EMP_NAME = "NAME";
  public static final String BLUEPAGES_KEY_EMP_COUNTRY_CODE = "EMPCC";
  public static final String BLUEPAGES_KEY_EMP_COMPANY_CODE = "HRCOMPANYCODE";
  public static final String BLUEPAGES_KEY_EMP_INTERNET_ID = "INTERNET";
  public static final String BLUEPAGES_KEY_EMP_CNUM = "CNUM";
  public static final String BLUEPAGES_KEY_NOTES_MAIL = "EMAILADDRESS";

  /**
   * Gets the name by cnum.
   * 
   * @param userCNUM
   *          the user cnum
   * @return the name by cnum
   */
  public static String getNameByCNUM(String userCNUM) {

    String name = null;
    if (userCNUM != null && userCNUM.length() > 1) {
      BPResults bpresults = BluePages.getPersonByCnum(userCNUM);

      if (bpresults.rows() == 0) {
        name = "No BluePages Name Found";
      } else if (bpresults.rows() > 1) {
        name = "MULTIPLE";
      } else if (bpresults.hasColumn("NAME")) {
        name = (String) bpresults.getRow(0).get("NAME");
      }
    }
    return name;
  }

  /**
   * Gets the email by cnum.
   * 
   * @param userCNUM
   *          the user cnum
   * @return the email by cnum
   */
  public static String getEmailByCNUM(String userCNUM) {
    BPResults bluePagesUserSearchedByCnum = BluePages.getPersonByCnum(userCNUM);
    if (bluePagesUserSearchedByCnum.rows() > 0) {
      return (String) bluePagesUserSearchedByCnum.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID);
    }
    return "No Email found";
  }

  /**
   * Gets the notes id by cnum.
   * 
   * @param userCNUM
   *          the user cnum
   * @return the notes id by cnum
   */
  public static String getNotesIdByCNUM(String userCNUM) {

    String notesID = null;
    if (userCNUM != null && userCNUM.length() > 1) {
      BPResults bpresults = BluePages.getPersonByCnum(userCNUM);

      if (bpresults.rows() == 0) {
        notesID = "NOT FOUND";
      } else if (bpresults.rows() > 1) {
        notesID = "MULTIPLE";
      } else if (bpresults.hasColumn("NOTESID") && ((String) bpresults.getRow(0).get("NOTESID")).length() > 0) {
        notesID = (String) bpresults.getRow(0).get("NOTESID");
        notesID = notesID.replace("/OU=", "/");
        notesID = notesID.replace("/O=", "/");
        notesID = notesID.replace("CN=", "");
        if (notesID.indexOf("@IBM") >= 0) {
          notesID = notesID.substring(0, notesID.indexOf("@IBM"));
        }
      }
    }
    return notesID;
  }

  /**
   * Gets the cNUM by intranet addr.
   * 
   * @param intranetAddr
   *          the intranet addr
   * @return the cNUM by intranet addr
   */
  public static String getCNUMByIntranetAddr(String intranetAddr) {
    BPResults bpresults = BluePages.getPersonsByInternet(intranetAddr);
    String cnum = null;
    if (bpresults.rows() == 0) {
      cnum = "NOTFOUND";
    } else if (bpresults.rows() > 1) {
      cnum = "MULTIPLE";
    } else if (bpresults.hasColumn(BLUEPAGES_KEY_EMP_CNUM)) {
      cnum = (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM);
    }
    return cnum;
  }

  /**
   * Gets the Person details like Name, Country Code, Company code, CNUM by
   * intranet addr.
   * 
   * @param intranetAddr
   *          the intranet addr
   * @return First and Last name as a single String
   */
  public static Map<String, String> getBluePagesDetailsByIntranetAddr(String intranetAddr) {

    BPResults bpresults = BluePages.getPersonsByInternet(intranetAddr);

    Map<String, String> returnMap = new HashMap<String, String>();

    if (bpresults.succeeded()) {
      if (bpresults.rows() == 1) {
        returnMap.put(BLUEPAGES_KEY_EMP_NAME, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_NAME));
        returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COMPANY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_CNUM, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM));
        returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID));
        returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_NOTES_MAIL));

      }
    } else {
      LOG.error("Error while doing Blue Pages look up ");
      return null;
    }
    return returnMap;
  }

  /**
   * Gets the Person details like Name, Country Code, Company code, CNUM by
   * name.
   * 
   * @param name
   * @return First and Last name as a single String
   */
  public static Map<String, String> getBluePagesDetailsByName(String name) {

    BPResults bpresults = BluePages.getPersonsByNameFuzzy(name);

    Map<String, String> returnMap = new HashMap<String, String>();

    if (bpresults.succeeded()) {
      if (bpresults.rows() == 1) {
        returnMap.put(BLUEPAGES_KEY_EMP_NAME, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_NAME));
        returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COMPANY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_CNUM, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM));
        returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID));
        returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_NOTES_MAIL));

      }
    } else {
      LOG.error("Error while doing Blue Pages look up ");
      return null;
    }
    return returnMap;
  }

  /**
   * Gets the Person details like Name, Country Code, Company code, CNUM by
   * intranet addr.
   * 
   * @param notesId
   * @return map of BP details
   */
  public static Map<String, String> getBluePagesDetailsByNotesId(String notesId) {

    BPResults bpresults = BluePages.getPersonsByNotesID(notesId);

    Map<String, String> returnMap = new HashMap<String, String>();

    if (bpresults.succeeded()) {
      if (bpresults.rows() == 1) {
        returnMap.put(BLUEPAGES_KEY_EMP_NAME, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_NAME));
        returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COMPANY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_CNUM, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM));
        returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID));
        returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, (String) bpresults.getRow(0).get(BLUEPAGES_KEY_NOTES_MAIL));

      }
    } else {
      LOG.error("Error while doing Blue Pages look up ");
      return null;
    }
    return returnMap;
  }

  /**
   * Format notes id in common format
   * 
   * @param notesId
   *          the notes id
   * @return the string
   */
  public static String formatNotesId(String notesId) {
    String formatedNotesId = notesId;
    if (formatedNotesId != null && formatedNotesId.length() > 0) {
      formatedNotesId = formatedNotesId.replace("/OU=", "/");
      formatedNotesId = formatedNotesId.replace("/O=", "/");
      formatedNotesId = formatedNotesId.replace("CN=", "");
      if (formatedNotesId.indexOf("@IBM") >= 0) {
        formatedNotesId = formatedNotesId.substring(0, formatedNotesId.indexOf("@IBM"));
      }
    }
    return formatedNotesId;
  }

  /**
   * Encodes notesId into long notes format
   * 
   * @param notesId
   * @return
   */
  private static String encodeNotesId(String notesId) {
    if (notesId == null) {
      return "";
    }
    if (notesId != null && notesId.contains("CN=")) {
      return notesId;
    }
    StringBuilder formatted = new StringBuilder();
    String[] parts = notesId.split("/");
    for (int i = 0; i < parts.length; i++) {
      if (i == 0) {
        formatted.append("CN=").append(parts[i]);
      } else if (parts[i].toUpperCase().contains("IBM")) {
        formatted.append("/").append("O=").append(parts[i]);
      } else {
        formatted.append("/").append("OU=").append(parts[i]);
      }
    }
    return formatted.toString();
  }

  /**
   * Blue Group authorization method. User has to belong to at least one of the
   * Blue Groups in the System Configuration to be granted access to the site.
   * If no group is set, it will return true (no restriction)
   * 
   * @param intranetId
   * @return
   */
  public static boolean isUserInBlueGroup(String intranetId) {
    cwa2 bpAPI = new cwa2();
    String groups = SystemConfiguration.getValue("BLUEGROUP_ACCESS", null);
    if (StringUtils.isEmpty(groups)) {
      // no bluegroup filter
      return true;
    }
    String[] blueGroups = groups.split(",");
    ReturnCode retCode = null;
    for (String blueGroup : blueGroups) {
      if (bpAPI.groupExist(blueGroup.trim())) {
        retCode = bpAPI.inAGroup(intranetId, blueGroup);
        if (cwaapi.SUCCESS.equals(retCode)) {
          // user belongs to one of the groups
          return true;
        }
      } else {
        LOG.debug("Group " + blueGroup + " does not exist. Skipping.");
      }
    }
    return false;
  }

  public static boolean isUserInBRBPBlueGroup(String intranetId) {
    cwa2 bpAPI = new cwa2();
    String groups = SystemParameters.getString("BR_BP_BLUEGROUPS");
    if (StringUtils.isEmpty(groups)) {
      return true;
    }
    String[] blueGroups = groups.split(",");
    ReturnCode retCode = null;
    for (String blueGroup : blueGroups) {
      if (bpAPI.groupExist(blueGroup.trim())) {
        retCode = bpAPI.inAGroup(intranetId, blueGroup);
        if (cwaapi.SUCCESS.equals(retCode)) {
          return true;
        }
      } else {
        LOG.debug("User " + intranetId + " does not belong to this bluegroup. Skipping.");
      }
    }
    return false;
  }

  public static boolean isUserInAdminBlueGroup(String intranetId) {
    cwa2 bpAPI = new cwa2();
    String groups = SystemConfiguration.getValue("BLUEGROUP_ADMIN", null);
    if (StringUtils.isEmpty(groups)) {
      // no bluegroup filter
      return true;
    }
    String[] blueGroups = groups.split(",");
    ReturnCode retCode = null;
    for (String blueGroup : blueGroups) {
      if (bpAPI.groupExist(blueGroup.trim())) {
        retCode = bpAPI.inAGroup(intranetId, blueGroup);
        if (cwaapi.SUCCESS.equals(retCode)) {
          // user belongs to one of the groups
          return true;
        }
      } else {
        LOG.debug("Group " + blueGroup + " does not exist. Skipping.");
      }
    }
    return false;
  }

  public static boolean isUserInProcessorBlueGroup(String intranetId) {
    cwa2 bpAPI = new cwa2();
    String groups = SystemConfiguration.getValue("BLUEGROUP_PROCESSOR", null);
    if (StringUtils.isEmpty(groups)) {
      // no bluegroup filter
      return true;
    }
    String[] blueGroups = groups.split(",");
    ReturnCode retCode = null;
    for (String blueGroup : blueGroups) {
      if (bpAPI.groupExist(blueGroup.trim())) {
        retCode = bpAPI.inAGroup(intranetId, blueGroup);
        if (cwaapi.SUCCESS.equals(retCode)) {
          // user belongs to one of the groups
          return true;
        }
      } else {
        LOG.debug("Group " + blueGroup + " does not exist. Skipping.");
      }
    }
    return false;
  }

  public static String getManagerEmail(String employeeId) {
    BPResults results = BluePages.getMgrChainOf(employeeId);
    if (results.rows() > 0) {
      String mgrEmail = (String) results.getRow(0).get("INTERNET");
      return mgrEmail;
    }
    return null;
  }

  /**
   * Gets the {@link Person} object using the internet ID
   * 
   * @param email
   * @return
   * @throws CmrException
   */
  public static Person getPerson(String email) throws CmrException {
    return getPerson(email, null);
  }

  /**
   * Gets the {@link Person} object using the internet ID
   * 
   * @param email
   * @param notesId
   * @return
   * @throws CmrException
   */
  public static Person getPerson(String email, String notesId) throws CmrException {
    Map<String, String> bpPersonDetails = BluePagesHelper.getBluePagesDetailsByIntranetAddr(email);
    if (bpPersonDetails != null) {
      Person p = new Person();
      p.setEmail(email);
      p.setName(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_NAME));
      p.setEmployeeId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_CNUM));
      p.setId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_CNUM));
      p.setNotesEmail(formatNotesId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_NOTES_MAIL)));
      if (StringUtils.isEmpty(p.getName())) {
        return null;
      }
      return p;
    } else {
      return null;
    }
  }

  /**
   * Gets the {@link Person} object using the name
   * 
   * @param email
   * @param notesId
   * @return
   * @throws CmrException
   */
  public static Person getPersonByName(String name) throws CmrException {
    Map<String, String> bpPersonDetails = BluePagesHelper.getBluePagesDetailsByName(name);
    if (bpPersonDetails != null) {
      Person p = new Person();
      p.setEmail(bpPersonDetails.get(BLUEPAGES_KEY_EMP_INTERNET_ID));
      p.setName(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_NAME));
      p.setEmployeeId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_CNUM));
      p.setId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_CNUM));
      p.setNotesEmail(formatNotesId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_NOTES_MAIL)));
      if (StringUtils.isEmpty(p.getName())) {
        return null;
      }
      return p;
    } else {
      return null;
    }
  }

  /**
   * Gets the {@link Person} object using the Notes ID
   * 
   * @param notesId
   * @return
   * @throws CmrException
   */
  public static Person getPersonByNotesId(String notesId) throws CmrException {

    Map<String, String> bpPersonDetails = BluePagesHelper.getBluePagesDetailsByNotesId(encodeNotesId(notesId));
    if (bpPersonDetails != null) {
      Person p = new Person();
      p.setName(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_NAME));
      p.setEmail(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_INTERNET_ID));
      p.setEmployeeId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_CNUM));
      p.setId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_EMP_CNUM));
      p.setNotesEmail(formatNotesId(bpPersonDetails.get(BluePagesHelper.BLUEPAGES_KEY_NOTES_MAIL)));
      if (StringUtils.isEmpty(p.getName())) {
        return null;
      }
      return p;
    } else {
      return null;
    }
  }

  public static boolean isBluePagesHeirarchyManager(String employeeEmail, String managerEmail) {
    if (StringUtils.isNotBlank(employeeEmail) && StringUtils.isNotBlank(managerEmail)) {
      String email = employeeEmail;
      while (StringUtils.isNotBlank(email)) {
        String employeeId = getCNUMByIntranetAddr(email);
        if (!"NOTFOUND".equals(employeeId) && !"MULTIPLE".equals(employeeId)) {
          email = BluePagesHelper.getManagerEmail(employeeId);
        }
        if (email != null && email.equals(managerEmail)) {
          return true;
        }
      }
    }
    return false;

  }

}
