/**
 * 
 */
package com.ibm.cio.cmr.request.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

  public static void main(String[] args) {
    String mgrMail = getManagerEmail("9esplar@ph.ibm.com");
    System.out.println(mgrMail);
    mgrMail = getManagerEmail("zamoraja@ph.ibm.com");
    System.out.println(mgrMail);
    mgrMail = getManagerEmail("136786PH1");
    System.out.println(mgrMail);
  }

  /** The Constant LOG. */
  private static final Logger LOG = Logger.getLogger(BluePagesHelper.class);
  public static final String BLUEPAGES_KEY_EMP_NAME = "NAME";
  public static final String BLUEPAGES_KEY_EMP_COUNTRY_CODE = "EMPCC";
  public static final String BLUEPAGES_KEY_EMP_COMPANY_CODE = "HRCOMPANYCODE";
  public static final String BLUEPAGES_KEY_EMP_INTERNET_ID = "INTERNET";
  public static final String BLUEPAGES_KEY_EMP_CNUM = "CNUM";
  public static final String BLUEPAGES_KEY_NOTES_MAIL = "EMAILADDRESS";

  /** Instanciate CWA2 */
  public static cwa2 cwa2;

  public static cwa2 getCWA2() {
    if (BluePagesHelper.cwa2 == null) {
      BluePagesHelper.cwa2 = new cwa2(SystemParameters.getString("BLUEPAGES_SERVER"), SystemParameters.getString("BLUEGROUPS_SERVER"));
    }
    LOG.info("Connecting to BluePage Server: " + SystemParameters.getString("BLUEPAGES_SERVER"));
    LOG.info("Connecting to BlueGroup Server: " + SystemParameters.getString("BLUEGROUPS_SERVER"));
    return BluePagesHelper.cwa2;
  }

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
        name = bpresults.getRow(0).get("NAME");
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
      return bluePagesUserSearchedByCnum.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID);
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
      } else if (bpresults.hasColumn("NOTESID") && bpresults.getRow(0).get("NOTESID").length() > 0) {
        notesID = bpresults.getRow(0).get("NOTESID");
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
      cnum = bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM);
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

    LOG.debug("Status: " + bpresults.getStatusCode() + " = " + bpresults.getStatusMsg());
    if (bpresults.succeeded()) {
      if (bpresults.rows() == 1) {
        returnMap.put(BLUEPAGES_KEY_EMP_NAME, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_NAME));
        returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COMPANY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_CNUM, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM));
        returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID));
        returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, bpresults.getRow(0).get(BLUEPAGES_KEY_NOTES_MAIL));

      }
    } else {
      LOG.error("Error while doing Blue Pages look up ");
      returnMap.put(BLUEPAGES_KEY_EMP_NAME, intranetAddr);
      returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_CNUM, "");
      returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, intranetAddr);
      returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, intranetAddr);
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

    LOG.debug("Status: " + bpresults.getStatusCode() + " = " + bpresults.getStatusMsg());

    if (bpresults.succeeded()) {
      if (bpresults.rows() == 1) {
        returnMap.put(BLUEPAGES_KEY_EMP_NAME, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_NAME));
        returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COMPANY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_CNUM, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM));
        returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID));
        returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, bpresults.getRow(0).get(BLUEPAGES_KEY_NOTES_MAIL));

      }
    } else {
      LOG.error("Error while doing Blue Pages look up ");
      returnMap.put(BLUEPAGES_KEY_EMP_NAME, name);
      returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_CNUM, "");
      returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, name);
      returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, name);
    }
    return returnMap;
  }

  /**
   * Gets the Person details like Name, Country Code, Company code, CNUM by name
   * per country.
   * 
   * @param name
   * @return First and Last name as a single String
   */
  public static Map<String, String> getBluePagesDetailsByName(String name, String country) {

    List<BPResults> allResults = new ArrayList<BPResults>();
    BPResults rearrangedNameResuls = null;

    BPResults resultsNormal = BluePages.getPersonsByNameFuzzy(name);
    allResults.add(resultsNormal);
    LOG.debug("Status for normal Name search: " + resultsNormal.getStatusCode() + " = " + resultsNormal.getStatusMsg());

    List<String> nameCombinations = getRearrangedNames(name);

    if (nameCombinations.size() > 0) {
      rearrangedNameResuls = BluePages.getPersonsByNameFuzzy(nameCombinations.get(0));
      allResults.add(rearrangedNameResuls);

      rearrangedNameResuls = BluePages.getPersonsByName(nameCombinations.get(1));
      allResults.add(rearrangedNameResuls);

      LOG.debug("Status for rearranged Name search: " + rearrangedNameResuls.getStatusCode() + " = " + rearrangedNameResuls.getStatusMsg());
    }

    Map<String, String> returnMap = new HashMap<String, String>();

    for (BPResults bpresults : allResults) {
      if (bpresults.succeeded() && returnMap.size() == 0) {
        for (int i = 0; i < bpresults.rows(); i++) {
          if (country.equals(bpresults.getRow(i).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE))) {
            returnMap.put(BLUEPAGES_KEY_EMP_NAME, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_NAME));
            returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, bpresults.getRow(i).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE));
            returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, bpresults.getRow(i).get(BLUEPAGES_KEY_EMP_COMPANY_CODE));
            returnMap.put(BLUEPAGES_KEY_EMP_CNUM, bpresults.getRow(i).get(BLUEPAGES_KEY_EMP_CNUM));
            returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, bpresults.getRow(i).get(BLUEPAGES_KEY_EMP_INTERNET_ID));
            returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, bpresults.getRow(i).get(BLUEPAGES_KEY_NOTES_MAIL));
            break;
          }
        }
      }
    }
    if (!resultsNormal.succeeded() && !rearrangedNameResuls.succeeded()) {
      LOG.error("Error while doing Blue Pages look up ");
      returnMap.put(BLUEPAGES_KEY_EMP_NAME, name);
      returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_CNUM, "");
      returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, name);
      returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, name);
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

    LOG.debug("Status: " + bpresults.getStatusCode() + " = " + bpresults.getStatusMsg());
    if (bpresults.succeeded()) {
      if (bpresults.rows() == 1) {
        returnMap.put(BLUEPAGES_KEY_EMP_NAME, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_NAME));
        returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COUNTRY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_COMPANY_CODE));
        returnMap.put(BLUEPAGES_KEY_EMP_CNUM, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_CNUM));
        returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, bpresults.getRow(0).get(BLUEPAGES_KEY_EMP_INTERNET_ID));
        returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, bpresults.getRow(0).get(BLUEPAGES_KEY_NOTES_MAIL));

      }
    } else {
      LOG.error("Error while doing Blue Pages look up ");
      returnMap.put(BLUEPAGES_KEY_EMP_NAME, notesId);
      returnMap.put(BLUEPAGES_KEY_EMP_COUNTRY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_COMPANY_CODE, "");
      returnMap.put(BLUEPAGES_KEY_EMP_CNUM, "");
      returnMap.put(BLUEPAGES_KEY_EMP_INTERNET_ID, notesId);
      returnMap.put(BLUEPAGES_KEY_NOTES_MAIL, notesId);
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
    cwa2 bpAPI = BluePagesHelper.getCWA2();
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
    cwa2 bpAPI = BluePagesHelper.getCWA2();
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
    cwa2 bpAPI = BluePagesHelper.getCWA2();
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
    cwa2 bpAPI = BluePagesHelper.getCWA2();
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
      String mgrEmail = results.getRow(0).get("INTERNET");
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
    if (StringUtils.isNotBlank(email)) {
      return getPerson(email, null);
    }
    return null;
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
  public static Person getPersonByName(String name, String country) throws CmrException {
    Map<String, String> bpPersonDetails = BluePagesHelper.getBluePagesDetailsByName(name, country);
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
      if (employeeEmail.toLowerCase().equals(managerEmail.toLowerCase())) {
        return true;
      }
      String cnum = getCNUMByIntranetAddr(employeeEmail);
      BPResults results = BluePages.getMgrChainOf(cnum);
      if (results != null) {
        for (int i = 0; i < results.rows(); i++) {
          Hashtable<String, String> row = results.getRow(i);
          if (row != null) {
            String mgrmail = row.get("INTERNET");
            if (!StringUtils.isBlank(mgrmail) && mgrmail.toLowerCase().equals(managerEmail.toLowerCase())) {
              return true;
            }
          }
        }
      }
    }
    return false;

  }

  /**
   * Checks if the employee is under any manager provided in the list
   * 
   * @param employeeEmail
   * @param managerEmails
   * @return
   */
  public static boolean isBluePagesHeirarchyManager(String employeeEmail, List<String> managerEmails) {
    if (StringUtils.isNotBlank(employeeEmail) && !managerEmails.isEmpty()) {
      List<String> managerEmailList = new ArrayList<>();
      for (String str : managerEmails) {
        managerEmailList.add(str.toLowerCase());
      }
      if (managerEmailList.contains(employeeEmail.toLowerCase())) {
        return true;
      }
      String cnum = getCNUMByIntranetAddr(employeeEmail);
      BPResults results = BluePages.getMgrChainOf(cnum);
      if (results != null) {
        for (int i = 0; i < results.rows(); i++) {
          Hashtable<String, String> row = results.getRow(i);
          if (row != null) {
            String mgrmail = row.get("INTERNET");
            if (!StringUtils.isBlank(mgrmail) && managerEmailList.contains(mgrmail.toLowerCase())) {
              return true;
            }
          }
        }
      }
    }
    return false;

  }

  // CREATCMR-5447
  public static boolean isUserInUSTAXBlueGroup(String intranetId) {
    cwa2 bpAPI = BluePagesHelper.getCWA2();
    String groups = SystemParameters.getString("US_TAX_BG_LIST");
    if (!StringUtils.isEmpty(groups)) {
      String[] blueGroups = groups.split(",");
      ReturnCode retCode = null;
      for (String blueGroup : blueGroups) {
        if (bpAPI.groupExist(blueGroup.trim())) {
          retCode = bpAPI.inAGroup(intranetId, blueGroup);
          if (cwaapi.SUCCESS.equals(retCode)) {
            return true;
          }
        } else {
          LOG.debug("Group " + blueGroup + " does not exist. Skipping.");
        }
      }
    }
    return false;
  }
  
  public static boolean isUserInJPBlueGroup(String intranetId) {
    cwa2 bpAPI = BluePagesHelper.getCWA2();
    String groups = SystemParameters.getString("JP_BLUE_GROUP");
    if (!StringUtils.isEmpty(groups)) {
      String[] blueGroups = groups.split(",");
      ReturnCode retCode = null;
      for (String blueGroup : blueGroups) {
        if (bpAPI.groupExist(blueGroup.trim())) {
          retCode = bpAPI.inAGroup(intranetId, blueGroup);
          if (cwaapi.SUCCESS.equals(retCode)) {
            return true;
          }
        } else {
          LOG.debug("Group " + blueGroup + " does not exist. Skipping.");
        }
      }
    }
    return false;
  }

  /**
   * Return an array of possible name arrangements of how names are stored in
   * BluePages. This list can be extended in future if more possible
   * arrangements are detected.
   * 
   * @param name
   * @return
   */
  public static List<String> getRearrangedNames(String name) {
    String[] splitName = name.trim().replaceAll(" +", " ").split(" ");
    List<String> rearrangedNames = new ArrayList<String>();
    if (splitName.length < 2) {
      return rearrangedNames;
    }
    String rearrangedName = "";

    // First arrangement
    for (int i = 1; i < splitName.length; i++) {
      rearrangedName = rearrangedName + splitName[i];
      if (i != splitName.length - 1)
        rearrangedName = rearrangedName + " ";
    }
    rearrangedName = rearrangedName + ", " + splitName[0];
    rearrangedNames.add(rearrangedName);
    rearrangedName = "";

    // Second arrangement
    if (splitName.length > 3) {
      for (int i = 2; i < splitName.length; i++) {
        rearrangedName = rearrangedName + splitName[i] + " ";
      }
      rearrangedName = rearrangedName.trim() + ", ";
      for (int i = 0; i < 2; i++) {
        rearrangedName = rearrangedName + splitName[i] + " ";
      }
    } else {
      for (int i = 0; i < splitName.length - 1; i++) {
        rearrangedName = rearrangedName + splitName[i];
        if (i != splitName.length - 2)
          rearrangedName = rearrangedName + " ";
      }
      rearrangedName = splitName[splitName.length - 1] + ", " + rearrangedName;
    }
    rearrangedNames.add(rearrangedName.trim());
    return rearrangedNames;
  }

  public static boolean isUserInLaBlueGroup(String intranetId, String groups) {
    cwa2 bpAPI = BluePagesHelper.getCWA2();
    if (!StringUtils.isEmpty(groups)) {
      String[] blueGroups = groups.split(",");
      ReturnCode retCode = null;
      for (String blueGroup : blueGroups) {
        if (bpAPI.groupExist(blueGroup.trim())) {
          retCode = bpAPI.inAGroup(intranetId, blueGroup);
          if (cwaapi.SUCCESS.equals(retCode)) {
            return true;
          }
        } else {
          LOG.debug("Group " + blueGroup + " does not exist. Skipping.");
        }
      }
    }
    return false;
  }

}
