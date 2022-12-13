/**
 * 
 */
package com.ibm.cio.cmr.request.util.legacy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtAddrLink;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.masschange.obj.TemplateTab;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.sof.GenericSOFMessageParser;

/**
 * Utilities handling the legacy direct processing
 * 
 * @author JeffZAMORA
 * 
 */
public class LegacyDirectUtil {
  private static final Logger LOG = Logger.getLogger(LegacyDirectUtil.class);

  private static final String ADDRESS_USE_MAILING = "1";
  private static final String ADDRESS_USE_BILLING = "2";
  private static final String ADDRESS_USE_INSTALLING = "3";
  private static final String ADDRESS_USE_SHIPPING = "4";
  private static final String ADDRESS_USE_EPL_MAILING = "5";
  private static final String ADDRESS_USE_COUNTRY_A = "A";
  private static final String ADDRESS_USE_COUNTRY_B = "B";
  private static final String ADDRESS_USE_COUNTRY_C = "C";
  private static final String ADDRESS_USE_COUNTRY_D = "D";
  private static final String ADDRESS_USE_COUNTRY_E = "E";
  private static final String ADDRESS_USE_COUNTRY_F = "F";
  private static final String ADDRESS_USE_COUNTRY_G = "G";
  private static final String ADDRESS_USE_COUNTRY_H = "H";
  public static final String IT_COMPANY_ADDR_TYPE = "ZI01";
  public static final String IT_BILLING_ADDR_TYPE = "ZP01";
  private static final List<String> FIELDS_CLEAR_LIST = new ArrayList<String>();

  private static final List<String> LD_BYPASS_MASS_UPDT_DUP_FILLS_VAL = new ArrayList<String>();

  static {
    FIELDS_CLEAR_LIST.add("CollectionCd");
    FIELDS_CLEAR_LIST.add("SpecialTaxCd");
    FIELDS_CLEAR_LIST.add("ModeOfPayment");
    FIELDS_CLEAR_LIST.add("CrosSubTyp");
    FIELDS_CLEAR_LIST.add("TipoCliente");
    FIELDS_CLEAR_LIST.add("CommercialFinanced");
    FIELDS_CLEAR_LIST.add("EmbargoCode");
    FIELDS_CLEAR_LIST.add("Enterprise");
    FIELDS_CLEAR_LIST.add("TypeOfCustomer");
    FIELDS_CLEAR_LIST.add("CodFlag");

    // LD_BYPASS_MASS_UPDT_DUP_FILLS_VAL.add("758");
  }

  /**
   * Mapping between the SOF Address use value and the SOF Query Service Tag
   */
  private static final Map<String, String> ADDR_USE_XML_MAP = new HashMap<String, String>() {

    private static final long serialVersionUID = 1L;
    {
      put(ADDRESS_USE_MAILING, "Mailing");
      put(ADDRESS_USE_BILLING, "Billing");
      put(ADDRESS_USE_INSTALLING, "Installing");
      put(ADDRESS_USE_SHIPPING, "Shipping");
      put(ADDRESS_USE_EPL_MAILING, "EplMailing");
      put(ADDRESS_USE_COUNTRY_A, "CntryUseA");
      put(ADDRESS_USE_COUNTRY_B, "CntryUseB");
      put(ADDRESS_USE_COUNTRY_C, "CntryUseC");
      put(ADDRESS_USE_COUNTRY_D, "CntryUseD");
      put(ADDRESS_USE_COUNTRY_E, "CntryUseE");
      put(ADDRESS_USE_COUNTRY_F, "CntryUseF");
      put(ADDRESS_USE_COUNTRY_G, "CntryUseG");
      put(ADDRESS_USE_COUNTRY_H, "CntryUseH");
    }

  };

  /**
   * Mapping between the Legacy Address use value and the logical description
   */
  public static final Map<String, String> USES = new HashMap<String, String>() {

    private static final long serialVersionUID = 1L;
    {
      put(ADDRESS_USE_MAILING, "Mailing");
      put(ADDRESS_USE_BILLING, "Billing");
      put(ADDRESS_USE_INSTALLING, "Installing");
      put(ADDRESS_USE_SHIPPING, "Shipping");
      put(ADDRESS_USE_EPL_MAILING, "EPL");
      put(ADDRESS_USE_COUNTRY_A, "Country Use A");
      put(ADDRESS_USE_COUNTRY_B, "Country Use B");
      put(ADDRESS_USE_COUNTRY_C, "Country Use C");
      put(ADDRESS_USE_COUNTRY_D, "Country Use D");
      put(ADDRESS_USE_COUNTRY_E, "Country Use E");
      put(ADDRESS_USE_COUNTRY_F, "Country Use F");
      put(ADDRESS_USE_COUNTRY_G, "Country Use G");
      put(ADDRESS_USE_COUNTRY_H, "Country Use H");
    }

  };

  private LegacyDirectUtil() {
    //
  }

  public static LegacyDirectObjectContainer getLegacyDBValuesForMassUpdate(EntityManager entityManager, String country, String cmrNo,
      boolean readOnly, boolean getLinks) throws CmrException {
    LOG.debug("Getting Legacy DB values for Mass Update for " + country + " - " + cmrNo);
    String cmrIssuingCntry = country;
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    return legacyObjects;
  }

  /**
   * Retrieves the current values of the record from the common legacy schema
   * and wraps them as a {@link LegacyDirectObjectContainer} object
   * 
   * @param country
   * @param cmrNo
   * @return
   * @throws CmrException
   */
  public static LegacyDirectObjectContainer getLegacyDBValues(EntityManager entityManager, String country, String cmrNo, boolean readOnly,
      boolean getLinks) throws CmrException {
    LOG.debug("Getting Legacy DB values for " + country + " - " + cmrNo);
    String cmrIssuingCntry = country;
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    // CMRTCUST
    String sql = ExternalizedQuery.getSql("LEGACYD.GETCUST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCust cust = query.getSingleResult(CmrtCust.class);
    if (cust == null) {
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
    }
    legacyObjects.setCustomer(cust);
    legacyObjects.setCustomerNo(cmrNo);
    legacyObjects.setSofCntryCd(cmrIssuingCntry);

    // CMRTEXT
    sql = ExternalizedQuery.getSql("LEGACYD.GETCEXT");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCustExt custExt = query.getSingleResult(CmrtCustExt.class);
    legacyObjects.setCustomerExt(custExt);

    // CMRTADDR
    sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
    if (addresses != null) {
      LOG.debug(">> Adding this number of addresses for CMR# " + cmrNo + " > " + addresses.size());

      // for (CmrtAddr addr : addresses) {
      // CmrtAddrPK cmrAddPK = addr.getId();
      // LOG.debug(">> Adding CMR/CNTRY/SEQ_NO > " + cmrAddPK.getCustomerNo() +
      // "/" + cmrAddPK.getSofCntryCode() + "/" + cmrAddPK.getAddrNo());
      // }

      legacyObjects.getAddresses().addAll(addresses);
    }

    /*
     * // CMRTAUSE sql = ExternalizedQuery.getSql("LEGACYD.GETAUSE"); query =
     * new PreparedQuery(entityManager, sql); query.setParameter("COUNTRY",
     * cmrIssuingCntry); query.setParameter("CMR_NO", cmrNo);
     * query.setForReadOnly(readOnly); List<CmrtAddrUse> addressUses =
     * query.getResults(CmrtAddrUse.class);
     * 
     * if (addressUses != null) { legacyObjects.getUses().addAll(addressUses); }
     * // map addr to use for (CmrtAddr address : addresses) { String use = "";
     * for (CmrtAddrUse addrUse : addressUses) { if (addrUse.getId().getAddrNo()
     * == address.getId().getAddrNo()) { use += addrUse.getId().getAddrUse(); }
     * } address.setAddressUse(use); }
     */

    if (getLinks) {
      // CMRTALNK
      sql = ExternalizedQuery.getSql("LEGACYD.GETALNK");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", cmrIssuingCntry);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(readOnly);
      List<CmrtAddrLink> addressLinks = query.getResults(CmrtAddrLink.class);
      if (addressLinks != null) {
        legacyObjects.getLinks().addAll(addressLinks);
      }
    }

    return legacyObjects;

  }

  public static LegacyDirectObjectContainer getLegacyDBValuesForITMass(EntityManager entityManager, String country, String cmrNo, MassUpdtData muData,
      boolean readOnly) throws CmrException {
    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    // DENNIS: Grab first the CMRTCEXT record of the CMR
    // CMRTEXT
    String sql = ExternalizedQuery.getSql("LEGACYD.GETCEXT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCustExt custExt = query.getSingleResult(CmrtCustExt.class);

    String errTxt = "";

    if (isITCompanyLevelMassUpdateEnabled(muData)) {
      LOG.debug("***COMPANY level of mass update is to be performed");
      if (custExt == null) {
        errTxt = "CMRDB2D.CMRTCEXT record is missing for Italy CMR: " + cmrNo;
        legacyObjects.setErrTxt(errTxt);
        // throw new CmrException(new Exception(errTxt));
      } else if (StringUtils.isBlank(custExt.getItCompanyCustomerNo())) {
        errTxt = "CMRDB2D.CMRTCEXT record for CMR " + cmrNo + " is missing Italy Company Customer Number on the Legacy DB";
        legacyObjects.setErrTxt(errTxt);
      } else {
        sql = ExternalizedQuery.getSql("LD.MASS_UPDT.GET_ALL_CUST_IN_HIERARCHY_IT");
        String codcp = custExt.getItCompanyCustomerNo().trim();
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("CNTRY", country);
        query.setParameter("CODCP", codcp);
        query.setForReadOnly(readOnly);
        List<CmrtCust> custs = query.getResults(CmrtCust.class);

        legacyObjects.setCustomersIT(custs);

        sql = ExternalizedQuery.getSql("LD.MASS_UPDT.GET_ALL_CEXT_IN_HIERARCHY_IT");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("CNTRY", country);
        query.setParameter("CODCP", codcp);
        query.setForReadOnly(readOnly);
        List<CmrtCustExt> custsExt = query.getResults(CmrtCustExt.class);

        legacyObjects.setCustomersextIT(custsExt);
      }
    } else if (isITBillingLevelMassUpdateEnabled(muData)) {
      LOG.debug("***BILLING level of mass update is to be performed");
      // CMRTCUST
      if (custExt == null) {
        errTxt = "CMRDB2D.CMRTCEXT record is missing for Italy CMR: " + cmrNo;
        legacyObjects.setErrTxt(errTxt);
      } else if (StringUtils.isBlank(custExt.getItBillingCustomerNo())) {
        errTxt = "CMRDB2D.CMRTCEXT record for CMR " + cmrNo + " is missing Italy Billing Customer Number value on the Legacy DB";
        legacyObjects.setErrTxt(errTxt);
      } else {
        sql = ExternalizedQuery.getSql("LD.MASS_UPDT.GET_ALL_CUST_IN_BILLING_IT");
        String codcc = custExt.getItBillingCustomerNo().trim();
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("CNTRY", country);
        query.setParameter("CODCC", codcc);
        query.setForReadOnly(readOnly);
        List<CmrtCust> custs = query.getResults(CmrtCust.class);

        legacyObjects.setCustomersIT(custs);

        sql = ExternalizedQuery.getSql("LD.MASS_UPDT.GET_ALL_CEXT_IN_BILLING_IT");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("CNTRY", country);
        query.setParameter("CODCC", custExt.getItBillingCustomerNo());
        query.setForReadOnly(readOnly);
        List<CmrtCustExt> custsExt = query.getResults(CmrtCustExt.class);

        legacyObjects.setCustomersextIT(custsExt);
      }
    } else if (isITCMRLevelMassUpdateEnabled(muData)) {
      LOG.debug("***CMR level of mass update is to be performed");
      sql = ExternalizedQuery.getSql("LD.MASS_UPDT.GET_ALL_CUST_SAME_CMR");
      String cmr = custExt.getId().getCustomerNo();
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY", country);
      query.setParameter("CMR", cmr);
      query.setForReadOnly(readOnly);
      List<CmrtCust> custs = query.getResults(CmrtCust.class);

      legacyObjects.setCustomersIT(custs);

      sql = ExternalizedQuery.getSql("LD.MASS_UPDT.GET_ALL_CUSTEXT_SAME_CMR");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY", country);
      query.setParameter("CMR", cmr);
      query.setForReadOnly(readOnly);
      List<CmrtCustExt> custsExt = query.getResults(CmrtCustExt.class);

      legacyObjects.setCustomersextIT(custsExt);
    }

    return legacyObjects;
  }

  // public static LegacyDirectObjectcontainer
  // getLegaacyDBValuesForMassIT(EntityManager entityManager, String country,
  // String cmrNo, boolean readOnly,
  // boolean getLinks, String addrNos) throws CmrException {
  // LOG.debug("[getLegaacyDBValuesForMassIT] Getting Legacy DB values for " +
  // country + " - " + cmrNo);
  // LegacyDirectObjectContainer legacyObjects = new
  // LegacyDirectObjectContainer();
  // return legacyObjects;
  // }

  public static LegacyDirectObjectContainer getLegacyDBValuesForMass(EntityManager entityManager, String country, String cmrNo, boolean readOnly,
      boolean getLinks, String addrNos) throws CmrException {
    LOG.debug("Getting Legacy DB values for " + country + " - " + cmrNo);
    String cmrIssuingCntry = country;
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    // CMRTCUST
    String sql = ExternalizedQuery.getSql("LEGACYD.GETCUST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCust cust = query.getSingleResult(CmrtCust.class);

    // DTN: Defect 1795577: Spain - Mass Update - processing should not stop
    // when template contains non-existent CNs
    /*
     * if (cust == null) { throw new
     * CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE); }
     */

    legacyObjects.setCustomer(cust);
    legacyObjects.setCustomerNo(cmrNo);
    legacyObjects.setSofCntryCd(cmrIssuingCntry);

    // CMRTEXT
    sql = ExternalizedQuery.getSql("LEGACYD.GETCEXT");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCustExt custExt = query.getSingleResult(CmrtCustExt.class);
    legacyObjects.setCustomerExt(custExt);

    // CMRTADDR

    if (!StringUtils.isEmpty(addrNos)) {
      sql = ExternalizedQuery.getSql("LEGACYD.GETADDR_FORMASS");
      sql = sql.replace(":ADDRNO", addrNos);
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", cmrIssuingCntry);
      query.setParameter("CMR_NO", cmrNo);

      query.setForReadOnly(readOnly);
      List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
      if (addresses != null) {
        LOG.debug(">> Adding this number of addresses for CMR# " + cmrNo + " > " + addresses.size());
        legacyObjects.getAddresses().addAll(addresses);
      }
    } else {
      // DTN: In case the params for addresses is empty, then we pass an empty
      // list to the container.
      legacyObjects.getAddresses().addAll(new ArrayList<CmrtAddr>());
    }

    /*
     * // CMRTAUSE sql = ExternalizedQuery.getSql("LEGACYD.GETAUSE"); query =
     * new PreparedQuery(entityManager, sql); query.setParameter("COUNTRY",
     * cmrIssuingCntry); query.setParameter("CMR_NO", cmrNo);
     * query.setForReadOnly(readOnly); List<CmrtAddrUse> addressUses =
     * query.getResults(CmrtAddrUse.class);
     * 
     * if (addressUses != null) { legacyObjects.getUses().addAll(addressUses); }
     * // map addr to use for (CmrtAddr address : addresses) { String use = "";
     * for (CmrtAddrUse addrUse : addressUses) { if (addrUse.getId().getAddrNo()
     * == address.getId().getAddrNo()) { use += addrUse.getId().getAddrUse(); }
     * } address.setAddressUse(use); }
     */

    if (getLinks) {
      // CMRTALNK
      sql = ExternalizedQuery.getSql("LEGACYD.GETALNK");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", cmrIssuingCntry);
      query.setParameter("CMR_NO", cmrNo);
      query.setForReadOnly(readOnly);
      List<CmrtAddrLink> addressLinks = query.getResults(CmrtAddrLink.class);
      if (addressLinks != null) {
        legacyObjects.getLinks().addAll(addressLinks);
      }
    }

    return legacyObjects;

  }

  /**
   * Converts the values stores on the {@link LegacyDirectObjectContainer}
   * object into the current map form. This method aims to avoid any changes in
   * the current import logics
   * 
   * @param legacyObjects
   * @throws Exception
   */
  public static GenericSOFMessageParser convertLegacyDataToMaps(LegacyDirectObjectContainer legacyObjects) throws Exception {
    Map<String, String> values = new LinkedHashMap<String, String>();
    List<String> shippingSequences = new ArrayList<>();
    List<String> countryCSequences = new ArrayList<>();
    List<String> installingSequences = new ArrayList<>();

    extractTags(legacyObjects.getCustomer(), values, "");
    extractTags(legacyObjects.getCustomer().getId(), values, "");

    List<String> uses = null;

    String addrNo = null;
    String xmlAddrTag = null;
    for (CmrtAddr addr : legacyObjects.getAddresses()) {
      uses = legacyObjects.getUsesBySequenceNo(addr.getId().getAddrNo());
      addrNo = addr.getId().getAddrNo() + "";

      if (!uses.isEmpty()) {
        // create one mapping for each address no with uses
        for (String use : uses) {
          if (!"".equals(use)) {
            xmlAddrTag = ADDR_USE_XML_MAP.get(use);
            extractTags(addr, values, xmlAddrTag);
            extractTags(addr.getId(), values, xmlAddrTag);
            values.put(xmlAddrTag + "AddressNumber", addrNo);
            if (ADDRESS_USE_SHIPPING.equals(use)) {
              extractTags(addr, values, xmlAddrTag + "_" + addrNo + "_");
              extractTags(addr.getId(), values, xmlAddrTag + "_" + addrNo + "_");
              values.put(xmlAddrTag + "_" + addrNo + "_" + "AddressNumber", addrNo);
              shippingSequences.add(addrNo);
            }
            if (ADDRESS_USE_INSTALLING.equals(use)) {
              extractTags(addr, values, xmlAddrTag + "_" + addrNo + "_");
              extractTags(addr.getId(), values, xmlAddrTag + "_" + addrNo + "_");
              values.put(xmlAddrTag + "_" + addrNo + "_" + "AddressNumber", addrNo);
              installingSequences.add(addrNo);
            }
            if (ADDRESS_USE_COUNTRY_C.equals(use)) {
              extractTags(addr, values, xmlAddrTag + "_" + addrNo + "_");
              extractTags(addr.getId(), values, xmlAddrTag + "_" + addrNo + "_");
              values.put(xmlAddrTag + "_" + addrNo + "_" + "AddressNumber", addrNo);
              countryCSequences.add(addrNo);
            }
          }
        }
      }
    }

    for (String key : values.keySet()) {
      LOG.trace(key + " = " + values.get(key));
    }
    LOG.debug("Shipping Sequences: " + shippingSequences.size());
    LOG.debug("Country C Sequences: " + countryCSequences.size());
    LOG.debug("Installing Sequences: " + installingSequences.size());
    GenericSOFMessageParser dummyParser = new GenericSOFMessageParser();
    dummyParser.setValues(values);
    dummyParser.setCountryCSequences(countryCSequences);
    dummyParser.setInstallingSequences(installingSequences);
    dummyParser.setShippingSequences(shippingSequences);
    return dummyParser;
  }

  /**
   * Extracts the tags from the object and writes them to the target map
   * 
   * @param obj
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  private static void extractTags(Object obj, Map<String, String> values, String prefix) throws Exception {
    if (obj == null) {
      return;
    }
    Field[] fields = obj.getClass().getDeclaredFields();

    LegacyXmlTag tags = null;
    Object value = null;
    String tagValue = null;
    for (Field field : fields) {
      if (!Modifier.isAbstract(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
        field.setAccessible(true);
        tags = field.getAnnotation(LegacyXmlTag.class);
        if (tags != null) {
          value = field.get(obj);
          if (value != null) {
            tagValue = value.toString();
          } else {
            tagValue = "";
          }
          if (!StringUtils.isEmpty(tags.value())) {
            values.put(prefix + tags.value(), tagValue);
          } else if (tags.tags() != null && tags.tags().length > 0) {
            for (String tagName : tags.tags()) {
              values.put(prefix + tagName, tagValue);
            }
          }
        }
      }
    }
  }

  public static boolean isCountryLegacyDirectEnabled(EntityManager entityManager, String cntry) {

    if (entityManager == null) {
      entityManager = JpaManager.getEntityManager();
    }

    boolean isLD = false;
    String sql = ExternalizedQuery.getSql("LEGACYD.GET_SUPP_CNTRY_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    List<Integer> records = query.getResults(Integer.class);
    Integer singleObject = null;

    if (records != null && records.size() > 0) {
      singleObject = records.get(0);
      Integer val = singleObject != null ? singleObject : null;

      if (val != null) {
        isLD = true;
      } else {
        isLD = false;
      }

    } else {
      isLD = false;
    }

    return isLD;
  }

  /**
   * Checks if this {@link Data} record has been updated. This method compares
   * with the {@link DataRdc} equivalent and compares per field and filters
   * given the configuration on the corresponding {@link GEOHandler} for the
   * given CMR issuing country. If at least one field is not empty, it will
   * return true.
   * 
   * @param data
   * @param dataRdc
   * @param cmrIssuingCntry
   * @return
   */
  public static boolean isDataUpdated(Data data, DataRdc dataRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;
    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);

    for (Field field : Data.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if at least one of the fields is updated
        if (handler.getDataFieldsForUpdateCheckLegacy(cmrIssuingCntry).contains(srcName)) {
          try {
            trgField = DataRdc.class.getDeclaredField(field.getName());

            field.setAccessible(true);
            trgField.setAccessible(true);

            Object srcVal = field.get(data);
            Object trgVal = trgField.get(dataRdc);

            if (String.class.equals(field.getType())) {
              String srcStringVal = (String) srcVal;
              if (srcStringVal == null) {
                srcStringVal = "";
              }
              String trgStringVal = (String) trgVal;
              if (trgStringVal == null) {
                trgStringVal = "";
              }
              if (!StringUtils.equals(srcStringVal.trim(), trgStringVal.trim())) {
                LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
                return true;
              }
            } else {
              if (!ObjectUtils.equals(srcVal, trgVal)) {
                LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
                return true;
              }
            }
          } catch (NoSuchFieldException e) {
            // noop
            continue;
          } catch (Exception e) {
            LOG.trace("General error when trying to access field.", e);
            // no stored value or field not on addr rdc, return null for no
            // changes
            continue;
          }
        } else {
          continue;
        }
      }
    }

    return false;
  }

  public static GEOHandler getGEOHandler(String cmrIssuingCntry) {
    String handlerClass = SystemConfiguration.getSystemProperty("geoHandler." + cmrIssuingCntry);
    try {
      if (!StringUtils.isEmpty(handlerClass)) {
        GEOHandler converter = (GEOHandler) Class.forName(handlerClass).newInstance();
        return converter;
      }
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Legacy direct utility method the get mapped Subundustry with the provided
   * ISIC. It is used primarily for mass update template parsing.
   * 
   * @param entityManager
   * @param isic
   * @return
   */
  public static String getMappedSubindOnMassUpdt(EntityManager entityManager, String isic) {
    String mappedSubind = "";

    String sql = ExternalizedQuery.getSql("LEGACYD.GETMAPPEDSUBIND_FORMASS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISIC", isic);
    query.setForReadOnly(true);
    mappedSubind = query.getSingleResult(String.class);

    return mappedSubind;
  }

  public static LegacyDirectObjectContainer getLegacyAddrDBValuesForMass(EntityManager entityManager, String country, String cmrNo, boolean readOnly)
      throws CmrException {
    LOG.debug("Getting Legacy DB values for " + country + " - " + cmrNo);
    String cmrIssuingCntry = country;
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    // CMRTADDR
    String sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
    if (addresses != null) {
      LOG.debug(">> Adding this number of addresses for CMR# " + cmrNo + " > " + addresses.size());
      legacyObjects.getAddresses().addAll(addresses);
    }

    return legacyObjects;

  }

  public static CmrtAddr getLegacyFiscalAddr(EntityManager entityManager, String country, String cmrNo, boolean readOnly) {
    String cmrIssuingCntry = country;
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    String sql = ExternalizedQuery.getSql("LEGACYD.GETADDR_FISCAL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtAddr addr = query.getSingleResult(CmrtAddr.class);

    return addr;
  }

  public static String getEmbargoCdFromDataRdc(EntityManager entityManager, Admin admin) {
    LOG.debug("Batch: EmbargoCd in DATA_RDC req_id:" + admin.getId().getReqId());
    String oldEmbargoCd = "";
    String sql = ExternalizedQuery.getSql("GET.EMBARGO_CD_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      oldEmbargoCd = result[0] != null ? (String) result[0] : "";
    }

    LOG.debug("Embargo Code of Data_RDC>" + oldEmbargoCd);
    return oldEmbargoCd;
  }

  public static String handleLDSeqNoScenario(String seqNo, boolean isPadZeroes) {
    String ret = "";

    if (isPadZeroes) {
      ret = StringUtils.leftPad(seqNo, 5, "0");
    } else {
      ret = seqNo.replaceFirst("^0+(?!$)", "");
    }

    return ret;
  }

  public static List<String> addSpecialCharToLov(List<String> lovList, String cntry, boolean codeOnly, String fieldId) {
    List<String> tempList = new ArrayList<String>();
    // if (SystemLocation.UNITED_KINGDOM.equals(cntry) ||
    // SystemLocation.IRELAND.equals(cntry)) {
    if (FIELDS_CLEAR_LIST.contains(fieldId)) {
      LOG.debug("***Field " + fieldId + " is on clear list. Adding '@'");
      if (codeOnly) {
        tempList.add("@");
      } else {
        tempList.add("@ | Clear field");
      }
      for (String choice : lovList) {
        tempList.add(choice);
      }
    } else {
      LOG.debug("***Field " + fieldId + " is NOT on clear list. Returning ORIGINAL list.");
      tempList = lovList;
    }
    // } else {
    // tempList = lovList;
    // }
    return tempList;
  }

  private static String validateColValFromCell(XSSFCell cell) {
    String colVal = "";
    if (cell != null) {
      switch (cell.getCellType()) {
      case STRING:
        colVal = cell.getStringCellValue();
        break;
      case NUMERIC:
        double nvalue = cell.getNumericCellValue();
        if (nvalue > 0) {
          colVal = "" + nvalue;
        }
        break;
      default:
        break;
      }
    }
    return colVal;
  }

  public static void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    GEOHandler handler = getGEOHandler(country);

    if (LD_BYPASS_MASS_UPDT_DUP_FILLS_VAL.contains(country)) {
      return;
    }

    handler.validateMassUpdateTemplateDupFills(validations, book, maxRows, country);
  }

  public static void checkIsraelMassTemplate(List<TemplateTab> tabs, XSSFWorkbook book, String country) throws Exception {
    if (SystemLocation.ISRAEL.equals(country)) {
      for (TemplateTab templateTab : tabs) {
        XSSFSheet sheet = book.getSheet(templateTab.getName());
        if (sheet == null) {
          throw new Exception("Invalid Template. Only MassUpdateTemplateAutoIL is accepted.");
        }
      }
    }
  }

  public static void checkNordxlMassTemplate(List<TemplateTab> tabs, XSSFWorkbook book, String country) throws Exception {
    // CREATCMR-2673
    String cmrCountry = "";
    String errMsg = "";

    switch (country) {
    case (SystemLocation.DENMARK):
      cmrCountry = "DK";
      break;
    case (SystemLocation.NORWAY):
      cmrCountry = "NO";
      break;
    case (SystemLocation.SWEDEN):
      cmrCountry = "SE";
      break;
    case (SystemLocation.FINLAND):
      cmrCountry = "FI";
      break;
    }

    if (!"".equals(cmrCountry)) {
      errMsg = "Invalid Template. Only MassUpdateTemplateAuto" + cmrCountry + " is accepted.";
    }

    for (TemplateTab templateTab : tabs) {
      XSSFSheet sheet = book.getSheet(templateTab.getName());
      if (sheet == null) {
        throw new Exception(errMsg);
      } else {
        if ("Data".equals(templateTab.getName())) {
          if (!StringUtils.isEmpty(sheet.getRow(0).getCell(17).toString())) {
            if (!sheet.getRow(0).getCell(17).toString().contains(cmrCountry)) {
              throw new Exception(errMsg);
            }
          }
        }
        break;
      }

    }
  }

  public static List<MassUpdtAddr> getMassUpdtAddrsForDPLCheck(EntityManager entityManager, String reqId, String iterId) {
    String sql = ExternalizedQuery.getSql("GET.LD_MASS_UPDT_FOR_DPL_CHECK");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ITER_ID", iterId);
    query.setForReadOnly(true);
    return query.getResults(MassUpdtAddr.class);
  }

  public static CmrtAddr getLegacyBillingAddress(EntityManager entityManager, String cmrNo, String cmrIssuingCntry) {
    CmrtAddr billAddr = null;
    // LD.GET_LEGACY_BILLING_ADDR
    String sql = ExternalizedQuery.getSql("LD.GET_LEGACY_BILLING_ADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cmrIssuingCntry);
    query.setParameter("CMR", cmrNo);
    query.setForReadOnly(true);
    billAddr = query.getSingleResult(CmrtAddr.class);
    return billAddr;
  }

  public static FindCMRResultModel findCmrByAddrSeq(String cmrNo, String cmrIssuingCntry, String addrSeq, String searchCountry) throws CmrException {
    String findCMRUrl = SystemConfiguration.getValue("FIND_CMR_URL");
    if (findCMRUrl == null) {
      throw new CmrException(MessageUtil.ERROR_NO_FIND_CMR_DEFINED);
    }

    String countryToUse = StringUtils.isEmpty(searchCountry) ? cmrIssuingCntry : searchCountry;
    findCMRUrl += "/getCMRData.json?customerNumber=" + cmrNo + "&issuingCountryCode=" + countryToUse;

    if (addrSeq != null && "0000C".equals(addrSeq)) {
      findCMRUrl += "&addressType=ZORG";
    } else {
      findCMRUrl += "&32=" + addrSeq;
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

    try {
      URL findCMR = new URL(findCMRUrl);
      LOG.debug("Connecting to find CMR " + SystemConfiguration.getValue("FIND_CMR_URL"));
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
      throw new CmrException(MessageUtil.ERROR_FIND_CMR_ERROR);
    }

  }

  public static FindCMRRecordModel getItalyCompanyAddress(EntityManager entityManager, FindCMRRecordModel companyR, String parentCmr) {
    // establish query to LegacyD
    String sql = ExternalizedQuery.getSql("ITALY.GET.COMPANYADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", SystemLocation.ITALY);
    query.setParameter("CMR", parentCmr);
    query.setForReadOnly(true);
    CmrtAddr addrC = query.getSingleResult(CmrtAddr.class);

    if (addrC != null) {
      companyR.setCmrIssuedBy(SystemLocation.ITALY);
      companyR.setCmrAddrSeq(addrC.getId().getAddrNo());
      companyR.setCmrCountryLanded("IT");
      companyR.setCmrName1Plain(addrC.getAddrLine1());
      companyR.setCmrName2Plain(addrC.getAddrLine2());
      companyR.setCmrName3(null);
      companyR.setCmrName4(null);

      companyR.setCmrStreetAddress(addrC.getAddrLine4());
      companyR.setCmrStreetAddressCont(addrC.getAddrLine3());
      companyR.setCmrPostalCode(addrC.getZipCode());
      companyR.setCmrCity(addrC.getCity());

      companyR.setCmrAddrTypeCode(IT_COMPANY_ADDR_TYPE);
      companyR.setCmrState(addrC.getItCompanyProvCd());
      companyR.setParentCMRNo(parentCmr);
    }

    return companyR;
  }

  public static FindCMRRecordModel getItalyBillingAddress(EntityManager entityManager, FindCMRRecordModel billingR, String partnerCmr,
      String partnerSeq) throws CmrException {
    // establish query to LegacyD
    CmrtAddr addrB = LegacyDirectUtil.getLegacyBillingAddress(entityManager, partnerCmr, SystemLocation.ITALY);
    CmrtCustExt custExt = new CmrtCustExt();
    boolean cextHelped = false;

    // DTN: If it is null, then we need to go to CMRTCEXT
    if (addrB == null) {
      String sql = ExternalizedQuery.getSql("LEGACYD.GETCEXT");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", SystemLocation.ITALY);
      query.setParameter("CMR_NO", partnerCmr);
      query.setForReadOnly(true);
      custExt = query.getSingleResult(CmrtCustExt.class);

      if (custExt != null) {
        addrB = LegacyDirectUtil.getLegacyBillingAddress(entityManager, custExt.getItBillingCustomerNo(), SystemLocation.ITALY);
        cextHelped = true;
      }

    }

    if (addrB != null) {
      String nextSeq = "";
      if (StringUtils.isNumeric(partnerSeq) && !cextHelped) {
        // genreate the next possible seq
        int iNext = Integer.parseInt(partnerSeq) + 1;
        nextSeq = handleLDSeqNoScenario(String.valueOf(iNext), true);
        billingR.setCmrAddrSeq(nextSeq);
      } else if (cextHelped) {
        if (!StringUtils.isNumeric(addrB.getId().getAddrNo())) {
          // pass a dummy since this one is not going to be updated anyway
          billingR.setCmrAddrSeq("00002");
        } else {
          billingR.setCmrAddrSeq(addrB.getId().getAddrNo());
        }
      } else {
        billingR.setCmrAddrSeq("");
      }

      billingR.setCmrIssuedBy(SystemLocation.ITALY);
      billingR.setCmrNum(partnerCmr);
      billingR.setCmrCountryLanded("IT");
      billingR.setCmrName1Plain(addrB.getAddrLine1() != null ? addrB.getAddrLine1().trim() : null);
      billingR.setCmrName2Plain(addrB.getAddrLine2() != null ? addrB.getAddrLine2().trim() : null);
      billingR.setCmrName3(null);
      billingR.setCmrName4(null);

      billingR.setCmrStreetAddress(addrB.getAddrLine4() != null ? addrB.getAddrLine4().trim() : null);
      billingR.setCmrStreetAddressCont(addrB.getAddrLine3() != null ? addrB.getAddrLine3().trim() : null);
      billingR.setCmrPostalCode(addrB.getZipCode() != null ? addrB.getZipCode().trim() : null);
      billingR.setCmrCity(addrB.getCity() != null ? addrB.getCity().trim() : null);

      billingR.setCmrAddrTypeCode(IT_BILLING_ADDR_TYPE);
      billingR.setCmrState(addrB.getItCompanyProvCd() != null ? addrB.getItCompanyProvCd().trim() : null);
      billingR.setParentCMRNo(addrB.getId().getCustomerNo());

      if (cextHelped) {
        // set the sapNo
        // we query findCMR
        FindCMRResultModel resultsR = findCmrByAddrSeq(addrB.getId().getCustomerNo(), SystemLocation.ITALY, addrB.getId().getAddrNo(), "");
        // CMR-1021 Secondary ZS01 record is getting created for Billing Kunnr
        // when blanked out Embargo
        if (resultsR != null && resultsR.getItems() != null && resultsR.getItems().size() > 0) {
          for (FindCMRRecordModel result : resultsR.getItems()) {
            if (result.getCmrAddrTypeCode() != null && "ZP01".equals(result.getCmrAddrTypeCode())) {
              billingR.setCmrSapNumber(result.getCmrSapNumber());
              LOG.info("Seting Billing Sap No from RDC record. SAP NO:" + billingR.getCmrSapNumber());
              break;
            }
          }
        } else {
          LOG.info("Billing Sap No not found from RDC record for CMR No:" + addrB.getId().getCustomerNo());
          billingR.setCmrSapNumber("");
        }
      }
    }

    return billingR;
  }

  public static void updateItalyBillingAddrSeq(EntityManager entityManager, String cmr, String cntry, String newSeq) {
    String sql = ExternalizedQuery.getSql("LD.UPDT_LEGACY_BILLING_ADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("SEQNO", newSeq);
    query.setParameter("CNTRY", cntry);
    query.setParameter("CMR", cmr);
    query.executeSql();
  }

  public static boolean isItalyLegacyDirect(EntityManager entityManager, long reqId) {
    boolean isItaly = false;

    if (reqId != 0L) {
      // now we get the Data record
      String sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", String.valueOf(reqId));
      Data data = query.getSingleResult(Data.class);

      if (SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
        isItaly = true;
      }
    }

    return isItaly;
  }

  public static boolean updateITCmrtAddrSeqByReqId(EntityManager entityManager, Admin admin) {
    boolean isUpdated = false;
    String sql = ExternalizedQuery.getSql("BATCH.GET_DATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", String.valueOf(admin.getId().getReqId()));
    query.setForReadOnly(true);
    Data data = query.getSingleResult(Data.class);

    // at this point we only need to know what is empty sapno. we already know
    // it is IT and an update request
    sql = ExternalizedQuery.getSql("LEGACYD.GET.ADDR_EMPTY_SAPNO");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", String.valueOf(admin.getId().getReqId()));
    query.setForReadOnly(true);
    List<Addr> addrs = query.getResults(Addr.class);

    // if list is not null and more than 0, then we will have to update this on
    // legacy
    if (addrs != null && addrs.size() > 0) {
      LegacyDirectUtil.updateItalyBillingAddrSeq(entityManager, data.getCmrNo(), data.getCmrIssuingCntry(), "00002");
      isUpdated = true;
    }
    return isUpdated;
  }

  /**
   * Check existing CMR No in Legacy DB
   * 
   * @param entityManager
   * @param data
   */
  public static boolean checkCMRNoInLegacyDB(EntityManager entityManager, Data data) {
    String sql = ExternalizedQuery.getSql("LEGACY.CHECK_EXISTS_CMR_NO");
    boolean isCMRExist = false;
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("COUNTRY", data.getCmrIssuingCntry());
    q.setParameter("CMR_NO", data.getCmrNo());
    Object results = q.getSingleResult(Object.class);

    if (results != null) {
      String result = results.toString();
      isCMRExist = "1".equals(result) ? true : false;
    }
    return isCMRExist;
  }

  /*
   * ITALY Mass update method to determine if the mass update change is to be
   * done from the company level and to all addresses in the hierarchy
   */
  private static boolean isITCompanyLevelMassUpdateEnabled(MassUpdtData muData) {
    boolean isITCompanyLevelMassUpdateEnabled = false;
    /*
     * fields covered
     */
    // �?Fiscal code >> NEW_ENTP_NAME1
    // �?Vat# >> VAT
    // �?Intent. Cliente >> OUT_CITY_LIMIT
    // �?Enterprise number >> ENTERPRISE
    if (!StringUtils.isBlank(muData.getNewEntpName1()) || !StringUtils.isBlank(muData.getVat()) || !StringUtils.isBlank(muData.getOutCityLimit())
        || !StringUtils.isBlank(muData.getEnterprise())) {
      isITCompanyLevelMassUpdateEnabled = true;
    }

    return isITCompanyLevelMassUpdateEnabled;
  }

  private static boolean isITCMRLevelMassUpdateEnabled(MassUpdtData muData) {
    boolean isITCMRLevelMassUpdateEnabled = false;
    /*
     * fields covered
     */
    // Affiliate Number --> AFFILIATE
    // Sales Rep. No. --> REP_TEAM_MEMBER_NO
    // INAC --> INAC_CD
    // Tipo Cliente --> ENTP_UPDT_TYP
    // Type Of Customer --> CURRENCY_CD
    // Codice Destinatario/Ufficio --> SEARCH_TERM
    // PEC --> EMAIL2
    // Indirizzo Email --> EMAIL3
    // Industry Solution Unit (ISU) --> ISU_CD
    // Client Tier --> CLIENT_TIER
    // Mode of Payment --> MODE_OF_PAYMENT
    // ISIC --> ISIC_CD
    // Embargo Code --> EMBARGO_CD
    if (!StringUtils.isBlank(muData.getAffiliate()) || !StringUtils.isBlank(muData.getRepTeamMemberNo()) || !StringUtils.isBlank(muData.getInacCd())
        || !StringUtils.isBlank(muData.getEntpUpdtTyp()) || !StringUtils.isBlank(muData.getCurrencyCd())
        || !StringUtils.isBlank(muData.getSearchTerm()) || !StringUtils.isBlank(muData.getEmail2()) || !StringUtils.isBlank(muData.getEmail3())
        || !StringUtils.isBlank(muData.getIsuCd()) || !StringUtils.isBlank(muData.getClientTier()) || !StringUtils.isBlank(muData.getModeOfPayment())
        || !StringUtils.isBlank(muData.getIsicCd()) || !StringUtils.isBlank(muData.getMiscBillCd())) {
      isITCMRLevelMassUpdateEnabled = true;
    }

    return isITCMRLevelMassUpdateEnabled;
  }

  /*
   * ITALY Mass update method to determine if the mass update change is to be
   * done from the company address and towards the billing addresses in the
   * hierarchy only
   */
  private static boolean isITBillingLevelMassUpdateEnabled(MassUpdtData muData) {
    boolean isITBillingLevelMassUpdateEnabled = false;

    // Tax Code/ Code IVA >> SPECIAL_TAX_CD
    // Collection Code >> COLLECTION_CD
    if (!StringUtils.isBlank(muData.getSpecialTaxCd()) || !StringUtils.isBlank(muData.getCollectionCd())) {
      isITBillingLevelMassUpdateEnabled = true;
    }

    return isITBillingLevelMassUpdateEnabled;
  }

  public static LegacyDirectObjectContainer getLegacyCustDBValues(EntityManager entityManager, String country, String cmrNo, boolean readOnly,
      boolean getLinks) throws CmrException {
    LOG.debug("Getting getLegacyCustDBValues Cust Legacy DB values for " + country + " - " + cmrNo);
    String cmrIssuingCntry = country;
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }

    LegacyDirectObjectContainer legacyObjects = new LegacyDirectObjectContainer();

    // CMRTCUST
    String sql = ExternalizedQuery.getSql("LEGACYD.GETCUST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrIssuingCntry);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(readOnly);
    CmrtCust cust = query.getSingleResult(CmrtCust.class);
    if (cust == null) {
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
    }
    legacyObjects.setCustomer(cust);
    legacyObjects.setCustomerNo(cmrNo);
    legacyObjects.setSofCntryCd(cmrIssuingCntry);
    return legacyObjects;
  }

  public static String getBillingSeqOfRDC(EntityManager entityManager, String cmrNo, String cntry, String billAddress) {
    LOG.debug("Searching for billing sequence in RDC");
    String billingSeq = "";
    String sql = ExternalizedQuery.getSql("GET.RDC_BILLING_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("CNTRY", cntry);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KTOKD", billAddress);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      billingSeq = sResult[0].toString();
    }
    LOG.debug("Find Billing sequence :" + billingSeq);
    return billingSeq;
  }

  public static void capsAndFillNulls(Object entity, boolean capitalize) throws Exception {
    try {
      Class<?> entityClass = entity.getClass();
      Field[] fields = entityClass.getDeclaredFields();
      for (Field field : fields) {
        if (String.class.equals(field.getType()) && !Modifier.isAbstract(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
          field.setAccessible(true);
          Object val = field.get(entity);
          if (val == null) {
            field.set(entity, "");
          } else if (capitalize) {
            field.set(entity, ((String) val).toUpperCase().trim());
          }
        }
      }
    } catch (Exception e) {
      // noop
      LOG.warn("Warning: caps and null fill failed. Error = " + e.getMessage());
    }
  }

  public static List<CmrtCustExt> getBillingChildFromCustExt(EntityManager entityManager, String billingCustNo, String cntry) {

    String sql = ExternalizedQuery.getSql("ITALY.GET.BILLING_CHILD_RECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR", billingCustNo);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    return query.getResults(CmrtCustExt.class);

  }

  public static List<CmrtCustExt> getCompanyChildFromCustExt(EntityManager entityManager, String compCustNo, String cntry) {
    String sql = ExternalizedQuery.getSql("ITALY.GET.COMPANY_CHILD_RECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR", compCustNo);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    return query.getResults(CmrtCustExt.class);
  }

  public static void updateCompanyChildCustRecords(EntityManager entityManager, List<String> rcuxaList, String cntry, String vat, String enterprise) {
    vat = vat != null ? vat.trim() : "";
    enterprise = enterprise != null ? enterprise.trim() : "";
    if (vat != null && !StringUtils.isEmpty(vat) && vat.length() > 11) {
      vat = vat.substring(2);
    }
    for (String cExt : rcuxaList) {
      String sql = ExternalizedQuery.getSql("LD.UPDT_LEGACY_COMPANY_CHILD_RECORDS_CUST");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("VAT", vat);
      query.setParameter("RENXA", enterprise);
      query.setParameter("CMR", cExt);
      query.setParameter("CNTRY", cntry);
      query.executeSql();
    }
  }

  public static boolean isFisCodeUsed(EntityManager entityManager, String cntry, String fisCode, String cmr) {
    boolean isFisCodeUsed = false;
    String sql = ExternalizedQuery.getSql("LD.MASS_UPDT.GET_EXISTS_FISCAL_CD_FROM_LEGACY");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", cntry);
    q.setParameter("FISCOD", fisCode);
    q.setParameter("CMR", cmr);
    Object results = q.getSingleResult(Object.class);

    if (results != null) {
      String result = results.toString();
      isFisCodeUsed = "1".equals(result) ? true : false;
    }

    return isFisCodeUsed;
  }

  public static List<CmrtAddr> checkLDAddress(EntityManager entityManager, String cmrNo, String country) throws CmrException {

    String sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(true);
    List<CmrtAddr> addresses = query.getResults(CmrtAddr.class);
    if (addresses != null) {
      LOG.debug(">> checkLDAddress for CMR# " + cmrNo + " > " + addresses.size());
    }
    return addresses;
  }

  public static boolean checkFieldsUpdated(EntityManager entityManager, String cmrIssuingCntry, Admin admin, long reqId) throws Exception {
    RequestChangeContainer changes = new RequestChangeContainer(entityManager, cmrIssuingCntry, admin, reqId);
    if (changes != null && changes.hasDataChanges()) {
      for (UpdatedDataModel updatedDataModel : changes.getDataUpdates()) {
        if (updatedDataModel != null) {
          String field = updatedDataModel.getDataField();
          switch (field) {
          case "Abbreviated Name":
          case "ISIC":
          case "Subindustry":
          case "INAC/NAC Code":
          case "Client Tier":
          case "SBO":
          case "ISU Code":
          case "ISR":
          case "Collection Code":
          case "Abbreviated Location":
            return true;
          default:
            return false;
          }
        }
      }
    }
    return false;
  }

  public static boolean isCountryDREnabled(EntityManager entityManager, String cntry) {

    if (entityManager == null) {
      entityManager = JpaManager.getEntityManager();
    }

    boolean isDR = false;
    String sql = ExternalizedQuery.getSql("DR.GET_SUPP_CNTRY_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    query.setForReadOnly(true);
    List<Integer> records = query.getResults(Integer.class);
    Integer singleObject = null;

    if (records != null && records.size() > 0) {
      singleObject = records.get(0);
      Integer val = singleObject != null ? singleObject : null;

      if (val != null) {
        isDR = true;
      } else {
        isDR = false;
      }

    } else {
      isDR = false;
    }

    return isDR;
  }

  public static DataRdc getOldData(EntityManager entityManager, String reqId) {
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<DataRdc> records = query.getResults(DataRdc.class);
    DataRdc oldData = new DataRdc();

    if (records != null && records.size() > 0) {
      oldData = records.get(0);
    } else {
      oldData = null;
    }

    return oldData;
  }

  public static Kna1 getIsicKukla(EntityManager entityManager, String cmrNo, String cntry) {
    LOG.debug("Retrieving ISIC/KUKLA for " + cntry + " - " + cmrNo);
    if (entityManager == null) {
      entityManager = JpaManager.getEntityManager();
    }
    String sql = ExternalizedQuery.getSql("IL.GET.ISIC.KUKLA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", cntry);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setForReadOnly(true);

    Kna1 kna1 = query.getSingleResult(Kna1.class);
    return kna1;
  }

  public static CmrtCust getRealCountryCodeBankNumber(EntityManager entityManager, String cmrNo, String cntry) {
    LOG.debug("Retrieving Real Country Code/Bank Number for " + cntry + " - " + cmrNo);
    if (entityManager == null) {
      entityManager = JpaManager.getEntityManager();
    }

    String sql = ExternalizedQuery.getSql("IL.GET.REALCTY.RBKXA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", cntry);
    query.setParameter("RCUXA", cmrNo);
    query.setForReadOnly(true);

    CmrtCust cmrtCust = query.getSingleResult(CmrtCust.class);
    return cmrtCust;
  }

  public static CmrtAddr getLegacyAddrBySeqNo(EntityManager entityManager, String cmrNo, String country, String seqNo) {
    if (entityManager == null) {
      entityManager = JpaManager.getEntityManager();
    }

    String sql = ExternalizedQuery.getSql("LEGACYD.GETADDR.BY_ADDRNO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    query.setParameter("ADDR_SEQ", seqNo);
    query.setForReadOnly(true);

    CmrtAddr cmrtAddr = query.getSingleResult(CmrtAddr.class);

    return cmrtAddr;
  }

  public static Addr getSoldToAddress(EntityManager entityManager, Long reqId) {
    String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_ZS01");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    Addr soldToAddr = query.getSingleResult(Addr.class);

    return soldToAddr;
  }
}
