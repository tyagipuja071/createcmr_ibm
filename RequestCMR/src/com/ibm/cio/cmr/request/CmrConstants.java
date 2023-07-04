package com.ibm.cio.cmr.request;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.ui.UIMgr;

/**
 * Class for all constants in the Request CMR project
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CmrConstants {

  public static final String SESSION_APPUSER_KEY = "cmrAppUser";

  /**
   * Enumeration for Yes/No short values
   * 
   * @author Jeffrey Zamora
   * 
   */
  public static enum YES_NO {
    Y, N
  }

  /**
   * Enumeration for BDS Info Display Data
   * 
   * @author Jeffrey Zamora
   * 
   */
  public static enum BDS_DISPLAYED_DATA {
    C, D, CD, DC
  }

  public static enum CLAIM_ROLE {
    /**
     * Delegate of the Locking Person
     */
    L,
    /**
     * Originator
     */
    O,
    /**
     * Processor
     */
    P,
    /**
     * Requester
     */
    R,
    /**
     * Delegate of the Requester, Originator, or Locking person
     */
    D
  }

  public static enum REQUEST_STATUS {
    /**
     * Draft
     */
    DRA, PVA, PCR, PCO, IIP, RIP, REP, ACU, SVA, SV2, PPN, PCP, COM, PRJ, AUT
  }

  public static enum ADDR_TYPE {
    /**
     * Sold-To
     */
    ZS01,
    /**
     * Install-At
     */
    ZI01,
    /**
     * Bill-To
     */
    ZP01,
    /**
     * Ship-To
     */
    ZD01,
    /**
     * Software
     */
    ZS02,
    /**
     * PayGo Billing
     */
    PG01,
    /**
     * Country Use A
     */
    CTYA,
    /**
     * Country Use B
     */
    CTYB,
    /**
     * Country Use C
     */
    CTYC,
    /**
     * Fiscal
     */
    ZP02,
    /**
     * H Address
     */
    ZD02,
    /**
     * KVK Address
     */
    ZKVK,
    /**
     * VAT Address
     */
    ZVAT,
    /**
     * Secondary Sold-To (AT)
     */
    ZS03,
    /**
     * List Price Record
     */
    ZLST
  }

  public static final String DNBSEARCH_NOT_DONE = "Not Done";
  public static final String RESULT_ACCEPTED = "Accepted";
  public static final String RESULT_REJECTED = "Rejected";
  public static final String RESULT_NO_RESULT = "No Results";
  public static final String RESULT_CANCELLED = "Cancelled";

  public static final String RDC_SOLD_TO = "ZS01";
  public static final String RDC_BILL_TO = "ZP01";
  public static final String RDC_INSTALL_AT = "ZI01";
  public static final String RDC_SHIP_TO = "ZD01";
  public static final String RDC_SECONDARY_SOLD_TO = "ZS02";
  public static final String RDC_SHIPPING = "ZH02";
  public static final String RDC_SECONDARY_SHIPPING = "ZH01";
  public static final String RDC_PAYGO_BILLING = "PG01";

  // REQUEST ACTIONS
  public static final String All_Processing_Complete() {
    return UIMgr.getText("trans.All_Processing_Complete");
  }

  public static final String Cancel_Request() {
    return UIMgr.getText("trans.Cancel_Request");
  }

  public static final String Cancel_Processing() {
    return UIMgr.getText("trans.Cancel_Processing");
  }

  public static final String Claim() {
    return UIMgr.getText("trans.Claim");
  }

  public static final String Processing_Validation_Complete() {
    return UIMgr.getText("trans.Processing_Validation_Complete");
  }

  public static final String Processing_Validation_Complete2() {
    return UIMgr.getText("trans.Processing_Validation_Complete2");
  }

  public static final String Create_Update_CMR() {
    return UIMgr.getText("trans.Create_Update_CMR");
  }

  public static final String Create_Update_Approved() {
    return UIMgr.getText("trans.Create_Update_Approved");
  }

  public static final String Reprocess_Checks() {
    return UIMgr.getText("trans.Reprocess_Checks");
  }

  public static final String Reject() {
    return UIMgr.getText("trans.Reject");
  }

  public static final String Save() {
    return UIMgr.getText("trans.Save");
  }

  public static final String Send_for_Processing() {
    return UIMgr.getText("trans.Send_for_Processing");
  }

  public static final String Unlock() {
    return UIMgr.getText("trans.Unlock");
  }

  public static final String Validate() {
    return UIMgr.getText("trans.Validate");
  }

  public static final String Processing_Create_Up_Complete() {
    return UIMgr.getText("trans.Processing_Create_Up_Complete");
  }

  public static final String Exit_without_Saving() {
    return UIMgr.getText("trans.Exit_without_Saving");
  }

  public static final String Mark_as_Completed() {
    return UIMgr.getText("trans.Mark_as_Completed");
  }

  public static final String Reprocess_Rdc() {
    return UIMgr.getText("trans.Reprocess_Rdc");
  }

  // User Role
  public static final String Role_Requester = "Requester";
  public static final String Role_Processor = "Processor";
  public static final String Role_Info_Provider = "Info Provider";
  public static final String Role_Reviewer = "Reviewer";
  public static final String Role_Viewer = "Viewer";

  // Overall Request Status
  public static final String ReqStatus_Draft = "Draft";
  // Scorecard Results
  public static final String Scorecard_Not_Done = "Not Done";
  public static final String Scorecard_None = "None";
  public static final String Scorecard_Not_Required = "NR";
  public static final String Scorecard_COMPLETED = "Completed";
  public static final String Scorecard_COMPLETED_WITH_ISSUES = "Completed with Issues";
  public static final String Scorecard_YES = "YES";
  public static final String Scorecard_NA = "N/A";
  // DPL Address results
  public static final String ADDRESS_Not_Required = "N";

  /**
   * Date Format to use for dates with no time component
   */
  public static SimpleDateFormat DATE_FORMAT() {
    SimpleDateFormat sdf = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    sdf.setTimeZone(TimeZone.getTimeZone(DATE_TIMEZONE));
    return sdf;
  }

  /**
   * Date Format to use for dates with time component
   */
  public static SimpleDateFormat DATE_TIME_FORMAT() {
    SimpleDateFormat sdf = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    sdf.setTimeZone(TimeZone.getTimeZone(DATE_TIMEZONE));
    return sdf;
  }

  public static String DATE_TIMEZONE = SystemConfiguration.getValue("DATE_TIMEZONE");

  public static final String TGME_ACCEPTED = "ACCEPTED";
  public static final String TGME_ADDRESS_CORRECT = "ADDRESS IS CORRECT";
  public static final String TGME_REJECTED = "REJECTED";
  public static final String TGME_NOT_REQUIRED = "NOT REQUIRED";
  public static final String TGME_NOT_DONE = "NOT DONE";

  public static final String TGME_WSDL = "TgmeStanService15.wsdl";
  public static final String CMT_LOCK_IND_NO = "N";
  public static final String CMT_LOCK_IND_YES = "Y";

  // batch program constants
  public static final String RDC_STATUS_ABORTED = "A";
  public static final String RDC_STATUS_COMPLETED = "C";
  public static final String RDC_STATUS_NOT_COMPLETED = "N";
  public static final String RDC_STATUS_COMPLETED_WITH_WARNINGS = "W";
  public static final String RDC_STATUS_WAITING = "T";
  public static final String RDC_STATUS_IGNORED = "I";
  public static final String NOTIFY_IND_NO = "N";
  public static final String NOTIFY_IND_YES = "Y";
  public static final String MQ_IND_YES = "Y";
  public static final String REQ_TYPE_CREATE = "C";
  public static final String REQ_TYPE_UPDATE = "U";
  public static final String REQ_TYPE_MASS_UPDATE = "M";
  public static final String REQ_TYPE_MASS_CREATE = "N";
  public static final String REQ_TYPE_REACTIVATE = "R";
  public static final String REQ_TYPE_SINGLE_REACTIVATE = "X";
  public static final String REQ_TYPE_UPDT_BY_ENT = "E";
  public static final String REQ_TYPE_DELETE = "D";
  public static final String PROSPECT_ORDER_BLOCK = "75";
  public static final String CLIENT_TIER_UNASSIGNED = "0";
  public static final String FIND_CMR_BLANK_CLIENT_TIER = "BLANK";

  public static final String LANGUAGE_ENGLISH = "E";
  public static final String DEACTIVATE_CMR_ORDER_BLOCK = "93";

  public static enum ROLES {
    ADMIN, PROCESSOR, REQUESTER, CMDE
  }

  public static final String LENOVO = "KAU";
  public static final String TRURO = "TRU";
  public static final String IBM = "IBM";
  public static final String FONSECA = "FON";

  public static final String SENSITIVE = "S";
  public static final String MASKED = "M";
  public static final String REGULAR = "REG";

  public static final List<String> SINGLE_REQUESTS_TYPES = Arrays.asList("C", "U", "X");
  public static final List<String> MASS_CHANGE_REQUESTS_TYPES = Arrays.asList("M", "D", "R", "N", "E");

  public static enum PROCESSING_STATUS {
    E, N, Y
  };

  public static String CREATE_BY_MODEL_GROUP = "14";
  public static String CREATE_BY_MODEL_SUB_GROUP = "BYMODEL";

  public static final String MASS_CREATE_ROW_STATUS_READY = "READY";
  public static final String MASS_CREATE_ROW_STATUS_FAIL = "FAIL";
  public static final String MASS_CREATE_ROW_STATUS_PASS = "PASS";
  public static final String MASS_CREATE_ROW_STATUS_UPDATE_FAILE = "UFAIL";
  public static final String MASS_CREATE_ROW_STATUS_DONE = "DONE";
  public static final String MASS_CREATE_ROW_STATUS_RDC_ERROR = "RDCER";

  // customer group
  public static final String CUSTGRP_CROSS = "CROSS";

  /*
   * Author: Dennis T Natad Date: June 16, 2017 Project: createCMR LA
   * Requirements Story 1165074: Automation of Legal Indicator field for LA
   */
  public static final String LEGAL_INDICATOR_P = "P";
  public static final String LEGAL_INDICATOR_C = "C";
  public static final String CUST_TYPE_PRIPE = "PRIPE";
  public static final String CUST_TYPE_BUSPR = "BUSPR";
  public static final String CUST_TYPE_LEASI = "LEASI";
  public static final String CUST_TYPE_BLUEM = "BLUEM";
  public static final String CUST_TYPE_IBMEM = "IBMEM";
  public static final String CUST_TYPE_INTER = "INTER";
  public static final String CUST_TYPE_CC3CCC = "CC3CC";
  public static final String CUST_CLASS_IBMEM = "71";
  public static final String CUST_CLASS_33 = "33";
  public static final String CUST_CLASS_34 = "34";
  public static final String DEFAULT_CROS_TYPE = "0";
  public static final String DEFAULT_CROS_SUB_TYPE = "PR";
  public static final String DEFAULT_BUSPR_PARTNERSHIP_IND = "Y";
  public static final String DEFAULT_NONBUSPR_PARTNERSHIP_IND = "N";
  public static final String DEAFULT_BUSPR_MARKETCONT_CD = "1";
  public static final String DEAFULT_NONBUSPR_MARKETCONT_CD = "0";
  public static final String DEFAULT_LEASI_LEASINGCOMP_IND = "Y";
  public static final String DEFAULT_LEASI_CROSTYP = "1";
  public static final String DEFAULT_LEASI_CROSSUBTYP = "PR";
  public static final String DEFAULT_LEASI_CUSTTYPE = "LEASI";
  public static final String DEFAULT_NONLEASI_LEASINGCOMP_IND = "N";
  public static final String DEFAULT_CUST_TYPE = "COMME";
  public static final String DEFAULT_CUSTOMERID_CD = "C";// --
  public static final String DEFAULT_TERRITORY_CD = "001";
  public static final String DEFAULT_TERRITORY_CD_MX = "000";
  public static final String DEFAULT_NATIONALCUS_ID = "N";
  public static final String DEFAULT_INSTALL_BRANCH_OFF = "204";
  public static final String DEFAULT_TEAM_CD = "T";
  public static final String DEFAULT_TEAM_CD_SSA = "E";
  public static final String DEFAULT_INSTALL_REP = "204199";
  public static final String DEFAULT_FOME_ZERO = "N";
  public static final String DEFAULT_CREDIT_CD_SA = "SA";
  public static final String DEFAULT_CREDIT_CD_CL = "CL";
  public static final String DEFAULT_COD_REASON = "00";
  public static final String DEFAULT_COD_CONDITION = "0";
  public static final String DEFAULT_REMOTE_CUSTOMER_IND = "Y";
  public static final String DEFAULT_TAX_PAYER_CUS_CD_1 = "1";
  public static final String DEFAULT_TAX_PAYER_CUS_CD_2 = "2";
  public static final String DEFAULT_TAX_PAYER_CUS_CD_4 = "4";
  public static final String CUST_TYPE_5PRIP = "5PRIP";
  public static final String CUST_TYPE_5COMP = "5COMP";
  public static final String DEFAULT_FOOTNOTE_NO = "1";

  // 1.4 changes
  public static final String APPROVAL_PENDING_APPROVAL = "PAPR";
  public static final String APPROVAL_PENDING_MAIL = "PMAIL";
  public static final String APPROVAL_OVERRIDE_PENDING = "OVERP";
  public static final String APPROVAL_OVERRIDE_APPROVED = "OVERA";
  public static final String APPROVAL_PENDING_REMINDER = "PREM";
  public static final String APPROVAL_PENDING_CANCELLATION = "PCAN";
  public static final String APPROVAL_CANCELLED = "CAN";
  public static final String APPROVAL_CONDITIONALLY_CANCELLED = "CCAN";
  public static final String APPROVAL_APPROVED = "APR";
  public static final String APPROVAL_REJECTED = "REJ";
  public static final String APPROVAL_CONDITIONALLY_APPROVED = "CAPR";
  public static final String APPROVAL_DRAFT = "DRA";
  public static final String APPROVAL_OBSOLETE = "OBSLT";
  public static final String APPROVAL_DEFUNCT = "DFNCT";

  public static final String APPROVAL_RESULT_NONE = "None";
  public static final String APPROVAL_RESULT_PENDING = "Pending";
  public static final String APPROVAL_RESULT_APPROVED = "Approved";
  public static final String APPROVAL_RESULT_COND_APPROVED = "Cond. Approved";
  public static final String APPROVAL_RESULT_REJECTED = "Rejected";
  public static final String APPROVAL_RESULT_CANCELLED = "Cancelled";
  public static final String APPROVAL_RESULT_COND_CANCELLED = "Cond. Cancelled";

  // Fixed Approval type for Mass Change requests
  public static final int MASS_REQUEST_APPROVAL_TYPE_ID = 13;

  public static final String APPROVAL_DEFLT_REQUIRED_INDC = "Y";
  public static final String DEFAULT_APPROVAL_FLAG = "DEFAULTAPPROVAL";

  // createCMR EMEA
  public static final String DEFAULT_SBO_IRELAND = "090";
  public static final String DEFAULT_COLLECTION_CD_FSL = "69";
  public static final String DEFAULT_SALES_REP_ISU_32 = "SPAIR6";

  // createCMR DE and CND
  public static final String CUST_CLASS_85 = "85";
  public static final String DEFAULT_IBM_DEPT_COST_CENTER = "00X306";
  public static final String LEGAL_ORDER_BLOCK = "";
  public static final String VAT_EXEMPT_TITLE = "Vat Exempt Approval";

  // Automation Engine
  public static final String VERIFY_COMPANY = "VERIFY_COMPANY";
  public static final String VERIFY_SCENARIO = "VERIFY_SCENARIO";
  public static final String OVERRIDE_DNB = "OVERRIDE_DNB";

  // Special Case For Brazil (631)
  public static enum CONTACT_TYPE_BR {

    LE_CONS("LE"), EM_CONS("EM"), CF_CONS("CF");

    private final String strLiteral;

    private CONTACT_TYPE_BR(String str) {
      strLiteral = str;
    }

    public String getStrLiteral() {
      return strLiteral;
    }

  }

  public static final List<String> CONTACT_TYPE_BR_LST = Arrays.asList("EM");

  public static Map<String, Object> HARDWARE_BO_REPTEAM_MAP;
  public static Map<String, Object> ES_POSTAL_CUSLOC_EBO_MAP;

  static {
    HARDWARE_BO_REPTEAM_MAP = new HashMap<String, Object>();
    HashMap<String, String> local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "69000");
    HARDWARE_BO_REPTEAM_MAP.put("AC", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "50000");
    HARDWARE_BO_REPTEAM_MAP.put("AL", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "69000");
    HARDWARE_BO_REPTEAM_MAP.put("AM", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "66000");
    HARDWARE_BO_REPTEAM_MAP.put("AP", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "260");
    local.put("hardwRTNo", "260100");
    local.put("locationNo", "40000");
    HARDWARE_BO_REPTEAM_MAP.put("BA", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "60000");
    HARDWARE_BO_REPTEAM_MAP.put("CE", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "250");
    local.put("hardwRTNo", "250300");
    local.put("locationNo", "70000");
    HARDWARE_BO_REPTEAM_MAP.put("DF", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "260");
    local.put("hardwRTNo", "260100");
    local.put("locationNo", "29000");
    HARDWARE_BO_REPTEAM_MAP.put("ES", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "60000");
    HARDWARE_BO_REPTEAM_MAP.put("FN", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "70000");
    HARDWARE_BO_REPTEAM_MAP.put("GO", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "65000");
    HARDWARE_BO_REPTEAM_MAP.put("MA", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "260");
    local.put("hardwRTNo", "260200");
    local.put("locationNo", "30000");
    HARDWARE_BO_REPTEAM_MAP.put("MG", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "70000");
    HARDWARE_BO_REPTEAM_MAP.put("MS", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "70000");
    HARDWARE_BO_REPTEAM_MAP.put("MT", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "66000");
    HARDWARE_BO_REPTEAM_MAP.put("PA", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "50000");
    HARDWARE_BO_REPTEAM_MAP.put("PB", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "50000");
    HARDWARE_BO_REPTEAM_MAP.put("PE", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "40000");
    HARDWARE_BO_REPTEAM_MAP.put("PI", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "235");
    local.put("hardwRTNo", "235300");
    local.put("locationNo", "80000");
    HARDWARE_BO_REPTEAM_MAP.put("PR", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "217");
    local.put("hardwRTNo", "217400");
    local.put("locationNo", "20000");
    HARDWARE_BO_REPTEAM_MAP.put("RJ", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "265");
    local.put("hardwRTNo", "265300");
    local.put("locationNo", "60000");
    HARDWARE_BO_REPTEAM_MAP.put("RN", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "69000");
    HARDWARE_BO_REPTEAM_MAP.put("RO", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "69000");
    HARDWARE_BO_REPTEAM_MAP.put("RR", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "230");
    local.put("hardwRTNo", "230300");
    local.put("locationNo", "90000");
    HARDWARE_BO_REPTEAM_MAP.put("RS", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "235");
    local.put("hardwRTNo", "235300");
    local.put("locationNo", "88000");
    HARDWARE_BO_REPTEAM_MAP.put("SC", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "260");
    local.put("hardwRTNo", "260100");
    local.put("locationNo", "40000");
    HARDWARE_BO_REPTEAM_MAP.put("SE", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "228");
    local.put("hardwRTNo", "228300");
    local.put("locationNo", "01000");
    HARDWARE_BO_REPTEAM_MAP.put("SP", local);
    local = new HashMap<String, String>();
    local.put("hardwBO", "267");
    local.put("hardwRTNo", "267000");
    local.put("locationNo", "40000");
    HARDWARE_BO_REPTEAM_MAP.put("TO", local);
    local.put("hardwBO", "228");
    local.put("hardwRTNo", "228300");
    local.put("locationNo", "01000");
    HARDWARE_BO_REPTEAM_MAP.put("EX", local);

    // DENNIS: Set values for Postal/CusLoc/EBO Map
    ES_POSTAL_CUSLOC_EBO_MAP = new HashMap<String, Object>();
    HashMap<String, String> locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "A0000");
    locEsPosCuslocEbo.put("ebo", "8608603");
    locEsPosCuslocEbo.put("stateProv", "A");
    ES_POSTAL_CUSLOC_EBO_MAP.put("03", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "AB000");
    locEsPosCuslocEbo.put("ebo", "8608602");
    locEsPosCuslocEbo.put("stateProv", "AB");
    ES_POSTAL_CUSLOC_EBO_MAP.put("02", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "AL000");
    locEsPosCuslocEbo.put("ebo", "8608604");
    locEsPosCuslocEbo.put("stateProv", "AL");
    ES_POSTAL_CUSLOC_EBO_MAP.put("04", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "AV000");
    locEsPosCuslocEbo.put("ebo", "8608605");
    locEsPosCuslocEbo.put("stateProv", "AV");
    ES_POSTAL_CUSLOC_EBO_MAP.put("05", locEsPosCuslocEbo); // 5

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "B0000");
    locEsPosCuslocEbo.put("ebo", "8608608");
    locEsPosCuslocEbo.put("stateProv", "B");
    ES_POSTAL_CUSLOC_EBO_MAP.put("08", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "BA000");
    locEsPosCuslocEbo.put("ebo", "8608606");
    locEsPosCuslocEbo.put("stateProv", "BA");
    ES_POSTAL_CUSLOC_EBO_MAP.put("06", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "BI000");
    locEsPosCuslocEbo.put("ebo", "8648648");
    locEsPosCuslocEbo.put("stateProv", "BI");
    ES_POSTAL_CUSLOC_EBO_MAP.put("48", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "BU000");
    locEsPosCuslocEbo.put("ebo", "8608609");
    locEsPosCuslocEbo.put("stateProv", "BU");
    ES_POSTAL_CUSLOC_EBO_MAP.put("09", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "C0000");
    locEsPosCuslocEbo.put("ebo", "8618615");
    locEsPosCuslocEbo.put("stateProv", "C");
    ES_POSTAL_CUSLOC_EBO_MAP.put("15", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "CA000");
    locEsPosCuslocEbo.put("ebo", "8618611");
    locEsPosCuslocEbo.put("stateProv", "CA");
    ES_POSTAL_CUSLOC_EBO_MAP.put("11", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "CC000");
    locEsPosCuslocEbo.put("ebo", "8618610");
    locEsPosCuslocEbo.put("stateProv", "CC");
    ES_POSTAL_CUSLOC_EBO_MAP.put("10", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "CE000");
    locEsPosCuslocEbo.put("ebo", "8618611");
    locEsPosCuslocEbo.put("stateProv", "CE");
    ES_POSTAL_CUSLOC_EBO_MAP.put("51", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "CO000");
    locEsPosCuslocEbo.put("ebo", "8618614");
    locEsPosCuslocEbo.put("stateProv", "CO");
    ES_POSTAL_CUSLOC_EBO_MAP.put("14", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "CR000");
    locEsPosCuslocEbo.put("ebo", "8618613");
    locEsPosCuslocEbo.put("stateProv", "CR");
    ES_POSTAL_CUSLOC_EBO_MAP.put("13", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "CS000");
    locEsPosCuslocEbo.put("ebo", "8618612");
    locEsPosCuslocEbo.put("stateProv", "CS");
    ES_POSTAL_CUSLOC_EBO_MAP.put("12", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "CU000");
    locEsPosCuslocEbo.put("ebo", "8618616");
    locEsPosCuslocEbo.put("stateProv", "CU");
    ES_POSTAL_CUSLOC_EBO_MAP.put("16", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "GC000");
    locEsPosCuslocEbo.put("ebo", "8638635");
    locEsPosCuslocEbo.put("stateProv", "GC");
    ES_POSTAL_CUSLOC_EBO_MAP.put("35", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "GE000");
    locEsPosCuslocEbo.put("ebo", "8618617");
    locEsPosCuslocEbo.put("stateProv", "GE");
    ES_POSTAL_CUSLOC_EBO_MAP.put("17", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "GR000");
    locEsPosCuslocEbo.put("ebo", "8618618");
    locEsPosCuslocEbo.put("stateProv", "GR");
    ES_POSTAL_CUSLOC_EBO_MAP.put("18", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "GU000");
    locEsPosCuslocEbo.put("ebo", "8618619");
    locEsPosCuslocEbo.put("stateProv", "GU");
    ES_POSTAL_CUSLOC_EBO_MAP.put("19", locEsPosCuslocEbo); // 22

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "H0000");
    locEsPosCuslocEbo.put("ebo", "8628621");
    locEsPosCuslocEbo.put("stateProv", "H");
    ES_POSTAL_CUSLOC_EBO_MAP.put("21", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "HU000");
    locEsPosCuslocEbo.put("ebo", "8628622");
    locEsPosCuslocEbo.put("stateProv", "HU");
    ES_POSTAL_CUSLOC_EBO_MAP.put("22", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "J0000");
    locEsPosCuslocEbo.put("ebo", "8628623");
    locEsPosCuslocEbo.put("stateProv", "J");
    ES_POSTAL_CUSLOC_EBO_MAP.put("23", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "LE000");
    locEsPosCuslocEbo.put("ebo", "8628624");
    locEsPosCuslocEbo.put("stateProv", "LE");
    ES_POSTAL_CUSLOC_EBO_MAP.put("24", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "L0000");
    locEsPosCuslocEbo.put("ebo", "8628625");
    locEsPosCuslocEbo.put("stateProv", "L");
    ES_POSTAL_CUSLOC_EBO_MAP.put("25", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "LO000");
    locEsPosCuslocEbo.put("ebo", "8628626");
    locEsPosCuslocEbo.put("stateProv", "LO");
    ES_POSTAL_CUSLOC_EBO_MAP.put("26", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "LU000");
    locEsPosCuslocEbo.put("ebo", "8628627");
    locEsPosCuslocEbo.put("stateProv", "LU");
    ES_POSTAL_CUSLOC_EBO_MAP.put("27", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "M0000");
    locEsPosCuslocEbo.put("ebo", "8628628");
    locEsPosCuslocEbo.put("stateProv", "M");
    ES_POSTAL_CUSLOC_EBO_MAP.put("28", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "MA000");
    locEsPosCuslocEbo.put("ebo", "8628629");
    locEsPosCuslocEbo.put("stateProv", "MA");
    ES_POSTAL_CUSLOC_EBO_MAP.put("29", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "ML000");
    locEsPosCuslocEbo.put("ebo", "8628629");
    locEsPosCuslocEbo.put("stateProv", "ML");
    ES_POSTAL_CUSLOC_EBO_MAP.put("52", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "MU000");
    locEsPosCuslocEbo.put("ebo", "8638630");
    locEsPosCuslocEbo.put("stateProv", "MU");
    ES_POSTAL_CUSLOC_EBO_MAP.put("30", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "NA000");
    locEsPosCuslocEbo.put("ebo", "8638631");
    locEsPosCuslocEbo.put("stateProv", "NA");
    ES_POSTAL_CUSLOC_EBO_MAP.put("31", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "O0000");
    locEsPosCuslocEbo.put("ebo", "8638633");
    locEsPosCuslocEbo.put("stateProv", "O");
    ES_POSTAL_CUSLOC_EBO_MAP.put("33", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "OR000");
    locEsPosCuslocEbo.put("ebo", "8638632");
    locEsPosCuslocEbo.put("stateProv", "OR");
    ES_POSTAL_CUSLOC_EBO_MAP.put("32", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "P0000");
    locEsPosCuslocEbo.put("ebo", "8638634");
    locEsPosCuslocEbo.put("stateProv", "P");
    ES_POSTAL_CUSLOC_EBO_MAP.put("34", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "PM000");
    locEsPosCuslocEbo.put("ebo", "8608607");
    locEsPosCuslocEbo.put("stateProv", "PM");
    ES_POSTAL_CUSLOC_EBO_MAP.put("07", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "PO000");
    locEsPosCuslocEbo.put("ebo", "8638636");
    locEsPosCuslocEbo.put("stateProv", "PO");
    ES_POSTAL_CUSLOC_EBO_MAP.put("36", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "S0000");
    locEsPosCuslocEbo.put("ebo", "8638639");
    locEsPosCuslocEbo.put("stateProv", "S");
    ES_POSTAL_CUSLOC_EBO_MAP.put("39", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "SA000");
    locEsPosCuslocEbo.put("ebo", "8638637");
    locEsPosCuslocEbo.put("stateProv", "SA");
    ES_POSTAL_CUSLOC_EBO_MAP.put("37", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "SE000");
    locEsPosCuslocEbo.put("ebo", "8648641");
    locEsPosCuslocEbo.put("stateProv", "SE");
    ES_POSTAL_CUSLOC_EBO_MAP.put("41", locEsPosCuslocEbo); // 42

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "SG000");
    locEsPosCuslocEbo.put("ebo", "8648640");
    locEsPosCuslocEbo.put("stateProv", "SG");
    ES_POSTAL_CUSLOC_EBO_MAP.put("40", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "SO000");
    locEsPosCuslocEbo.put("ebo", "8648642");
    locEsPosCuslocEbo.put("stateProv", "SO");
    ES_POSTAL_CUSLOC_EBO_MAP.put("42", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "SS000");
    locEsPosCuslocEbo.put("ebo", "8628620");
    locEsPosCuslocEbo.put("stateProv", "SS");
    ES_POSTAL_CUSLOC_EBO_MAP.put("20", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "T0000");
    locEsPosCuslocEbo.put("ebo", "8648643");
    locEsPosCuslocEbo.put("stateProv", "T");
    ES_POSTAL_CUSLOC_EBO_MAP.put("43", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "TE000");
    locEsPosCuslocEbo.put("ebo", "8648644");
    locEsPosCuslocEbo.put("stateProv", "TE");
    ES_POSTAL_CUSLOC_EBO_MAP.put("44", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "TF000");
    locEsPosCuslocEbo.put("ebo", "8638638");
    locEsPosCuslocEbo.put("stateProv", "TF");
    ES_POSTAL_CUSLOC_EBO_MAP.put("38", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "TO000");
    locEsPosCuslocEbo.put("ebo", "8648645");
    locEsPosCuslocEbo.put("stateProv", "TO");
    ES_POSTAL_CUSLOC_EBO_MAP.put("45", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "V0000");
    locEsPosCuslocEbo.put("ebo", "8648646");
    locEsPosCuslocEbo.put("stateProv", "V");
    ES_POSTAL_CUSLOC_EBO_MAP.put("46", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "VA000");
    locEsPosCuslocEbo.put("ebo", "8648647");
    locEsPosCuslocEbo.put("stateProv", "VA");
    ES_POSTAL_CUSLOC_EBO_MAP.put("47", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "VI000");
    locEsPosCuslocEbo.put("ebo", "8608601");
    locEsPosCuslocEbo.put("stateProv", "VI");
    ES_POSTAL_CUSLOC_EBO_MAP.put("01", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "Z0000");
    locEsPosCuslocEbo.put("ebo", "8658650");
    locEsPosCuslocEbo.put("stateProv", "Z");
    ES_POSTAL_CUSLOC_EBO_MAP.put("50", locEsPosCuslocEbo);

    locEsPosCuslocEbo = new HashMap<String, String>();
    locEsPosCuslocEbo.put("cusLocNum", "ZA000");
    locEsPosCuslocEbo.put("ebo", "8648649");
    locEsPosCuslocEbo.put("stateProv", "ZA");
    ES_POSTAL_CUSLOC_EBO_MAP.put("49", locEsPosCuslocEbo); // 54

  }

  public static List<String> SSAMX_INTERNAL_TYPES = Arrays.asList("INTER", "INTOU", "INTUS", "INIBM");

  public static final String DE_ISSUING_COUNTRY_VAL = "724";

  public static List<String> CN_NON_SPACED_CITIES = Arrays.asList("HARBIN", "HUHHOT", "URUMQI", "LHASA");

  public static final String CN_CITIES_UPPER_FIELD_ID = "##ChinaCityUpperList";

  public static final String PROCESSING_TYPE_LEGACY_DIRECT = "LD";
  public static final String PROCESSING_TYPE_RDC_MAIN = "MA";
  public static final String PROCESSING_TYPE_MQ = "MQ";
  public static final String PROCESSING_TYPE_TRANSACTION_CONNECT = "TC";
  public static final String PROCESSING_TYPE_IERP = "DR";

  public static final String SOF_TYPE_MAILING = "Mailing";
  public static final String SOF_TYPE_BILLING = "Billing";
  public static final String SOF_TYPE_INSTALLING = "Installing";
  public static final String SOF_TYPE_EPL = "EplMailing";
  public static final String SOF_TYPE_SHIPPING = "Shipping";

  public static final String SESSION_ERROR_KEY = "_currenterror";

  public static final int LD_MASS_UPDATE_UPPER_LIMIT = 40;

  public static final String CN_KUKLA81 = "81";
  public static final String CN_KUKLA85 = "85";
  public static final String CN_KUKLA45 = "45";
  public static final String CN_KUKLA81_KEYID = "CMRNO_ZS01_81";
  public static final String CN_KUKLA45_KEYID = "CMRNO_ZS01_45";
  public static final String CN_DEFAULT_KEYID = "CMRNO_ZS01";

  public static final String CN_ERO_APPROVAL_DESC = "Approval For both ERO Proliferation Checklist and DPL Matching (CN)";
  public static final String CN_ECO_LEADER_APPROVAL_DESC = "Ecosystem market leader Approval Request China";
  public static final String CN_TECH_LEADER_APPROVAL_DESC = "Technology market leader Approval Request China";

  public static final List<String> DE_CND_ISSUING_COUNTRY_VAL = Arrays.asList("724", "619", "621", "627", "647", "791", "640", "759", "839", "843",
      "859");
  public static final List<String> ORDER_BLK_LIST = Arrays.asList("88");

  public static final List<String> LA_COUNTRIES = Arrays.asList("613", "629", "655", "661", "663", "681", "683", "829", "731", "735", "799", "811",
      "813", "815", "869", "871", "631", "781");
  
  public static final List<String> BP_GBM_SBM_COUNTRIES = Arrays.asList("620", "677", "680", "767", "805", "823", "832");
  public static final List<String> CROSS_BORDER_COUNTRIES_GROUP1 = Arrays.asList("866", "754", "724", "848", "618", "788", "624", "678", "702", "806","846","706");
  public static final String VAT_ACKNOWLEDGE_YES = "Yes";
  public static final String VAT_ACKNOWLEDGE_NA = "N/A";
  public static final String CMRBPPortal = "CreateCMR-BP";
}
