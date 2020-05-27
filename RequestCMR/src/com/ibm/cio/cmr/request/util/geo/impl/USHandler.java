/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * @author Jeffrey Zamora
 * 
 */
public class USHandler extends GEOHandler {

  private static final String LANG_ENGLISH = "E";
  private static final Logger LOG = Logger.getLogger(USHandler.class);

  private static final String[] USABLE_ADDRESSES = new String[] { "ZS01", "ZI01", "ZP01" };

  private static final String[] USABLE_SEQUENCES = new String[] { "001", "002" };

  private static final String[] US_TERRITORIES = new String[] { "PR", "AS", "GU", "MP", "UM", "VI" };

  private String legalName;

  private EntityManager entityManager;

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    this.entityManager = entityManager;
    LOG.trace("Converting records for US");
    List<FindCMRRecordModel> converted = new ArrayList<>();

    List<FindCMRRecordModel> records = source.getItems();

    FindCMRRecordModel main = records != null && records.size() > 0 ? records.get(0) : new FindCMRRecordModel();
    boolean prospectConversion = CmrConstants.PROSPECT_ORDER_BLOCK.equals(main.getCmrOrderBlock());
    for (FindCMRRecordModel record : records) {

      record.setCmrName1Plain(null);
      record.setCmrName2Plain(null);

      // if landed country is not US but is a US territory, set to 'US'
      String land1 = !StringUtils.isEmpty(record.getCmrCountryLanded()) ? record.getCmrCountryLanded().trim() : "";
      if (prospectConversion && !"US".equalsIgnoreCase(land1) && checkIfTerritory(land1)) {
        record.setCmrCountryLanded("US");
        record.setCmrState(land1);
      }

      // if address is usable, process and add to converted
      if (Arrays.asList(USABLE_ADDRESSES).contains(record.getCmrAddrTypeCode())
          && (Arrays.asList(USABLE_SEQUENCES).contains(record.getCmrAddrSeq()) || prospectConversion)) {
        if ("ZS01".equals(record.getCmrAddrTypeCode()) || "ZI01".equals(record.getCmrAddrTypeCode())) {
          // set the address type to Install At for CreateCMR
          record.setCmrAddrTypeCode("ZS01");
        } else if ("ZP01".equals(record.getCmrAddrTypeCode())) {
          // set the address type to Invoice To for CreateCMR
          record.setCmrAddrTypeCode("ZI01");
        }

        converted.add(record);

        // move Tax Code 2 to Tax Code 1 for US
        record.setCmrBusinessReg(record.getCmrLocalTax2());
        record.setCmrLocalTax2(null);

        // do some manipulations of DATA fields here
        if (StringUtils.isBlank(main.getCmrBusinessReg()) && !StringUtils.isBlank(record.getCmrBusinessReg())) {
          // move the TAX code from non-Main to main
          main.setCmrBusinessReg(record.getCmrBusinessReg());
        }

        if (StringUtils.isEmpty(record.getCmrTier()) || CmrConstants.FIND_CMR_BLANK_CLIENT_TIER.equals(record.getCmrTier())) {
          record.setCmrTier(CmrConstants.CLIENT_TIER_UNASSIGNED);
        }
      }
    }

    Collections.sort(converted);
    source.setItems(converted);
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel cmr) throws Exception {

    String cmrNo = cmr.getCmrNum();
    if (!NumberUtils.isDigits(cmrNo)) {
      LOG.debug("Non-digits found. Maybe Prospect/Lite conversion.");
      return;
    }
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");

    String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
    // try US CMR DB first
    String sql = ExternalizedQuery.getSql("IMPORT.US.USCMR", usSchema);
    sql = StringUtils.replace(sql, ":CMR_NO", "'" + data.getCmrNo() + "'");
    String dbId = QueryClient.USCMR_APP_ID;

    LOG.debug("Getting existing values from US CMR DB..");
    boolean retrieved = queryAndAssign(url, sql, results, data, dbId);

    LOG.debug("US CMR Data retrieved? " + retrieved);
    if (retrieved) {
      boolean closeMgr = false;
      if (this.entityManager == null) {
        this.entityManager = JpaManager.getEntityManager();
        closeMgr = true;
      }
      setCodesFromLOV(this.entityManager, url, data);
      // Check if OEM CMR, throw error if yes
      if ("C".equals(admin.getReqType()) && StringUtils.isNotBlank(data.getRestrictTo()) && "OEMHQ".equals(data.getRestrictTo())) {
        throw new CmrException(MessageUtil.OEM_IMPORT_US_CREATE);
      }

      if (closeMgr) {
        this.entityManager.clear();
        this.entityManager.close();
      }
      // setCodes(url, data);
    } else {
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
    }

  }

  private boolean queryAndAssign(String url, String sql, FindCMRResultModel model, Data data, String dbId) throws Exception {

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.setRows(1);
    query.addField("CMR_NO");
    query.addField("CUST_TYPE");
    query.addField("RESTRICT_IND");
    query.addField("RESTRICT_TO");
    query.addField("OEM_IND");
    query.addField("BP_ACCT_TYP");
    query.addField("MKTG_DEPT");
    query.addField("MTKG_AR_DEPT");
    query.addField("PCC_MKTG_DEPT");
    query.addField("PCC_AR_DEPT");
    query.addField("SVC_AR_OFFICE");
    query.addField("SVC_TERRITORY_ZONE");
    query.addField("OUT_CITY_LIMIT");
    query.addField("CSO_SITE");
    query.addField("FED_SITE_IND");
    query.addField("SIZE_CD");
    query.addField("MISC_BILL_CD");
    query.addField("TAX_CD3");
    query.addField("BP_NAME");
    query.addField("ICC_TAX_CLASS");
    query.addField("ICC_TAX_EXEMPT_STATUS");
    query.addField("NON_IBM_COMPANY_IND");
    query.addField("COMPANY_NM");
    query.addField("DIV");
    query.addField("DEPT");
    query.addField("FUNC");
    query.addField("USER");
    query.addField("LOC");
    query.addField("TAX_CD2");
    query.addField("TAX_CD1");
    query.addField("ABBREV_NM");
    query.addField("TAX_EXEMPT_STATUS");
    query.addField("SICMEN");
    query.addField("ISIC");
    query.addField("I_CUST_ADDR_TYPE");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);

    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (!response.isSuccess()) {
      return false;
    } else if (response.getRecords() == null || response.getRecords().size() == 0) {
      return false;
    } else {
      Map<String, Object> record = response.getRecords().get(0);

      // data.set((String) record.get("CUST_TYPE"));
      data.setRestrictInd((String) record.get("RESTRICT_IND"));
      data.setRestrictTo((String) record.get("RESTRICT_TO"));
      data.setOemInd((String) record.get("OEM_IND"));
      data.setBpAcctTyp((String) record.get("BP_ACCT_TYP"));
      data.setMktgDept((String) record.get("MKTG_DEPT"));
      data.setMtkgArDept((String) record.get("MTKG_AR_DEPT"));
      data.setPccMktgDept((String) record.get("PCC_MKTG_DEPT"));
      data.setPccArDept((String) record.get("PCC_AR_DEPT"));
      data.setSvcArOffice((String) record.get("SVC_AR_OFFICE"));

      data.setSvcTerritoryZone(record.get("SVC_TERRITORY_ZONE") != null ? record.get("SVC_TERRITORY_ZONE").toString() : null);
      if ("0".equals(data.getSvcTerritoryZone())) {
        data.setSvcTerritoryZone("0000");
      }
      data.setOutCityLimit((String) record.get("OUT_CITY_LIMIT"));
      data.setCsoSite((String) record.get("CSO_SITE"));
      data.setFedSiteInd((String) record.get("FED_SITE_IND"));
      data.setSizeCd((String) record.get("SIZE_CD"));
      data.setMiscBillCd((String) record.get("MISC_BILL_CD"));
      if (data.getMiscBillCd() != null && data.getMiscBillCd().trim().length() > 3) {
        data.setMiscBillCd(data.getMiscBillCd().trim().substring(0, 3));
      }
      data.setTaxCd3((String) record.get("TAX_CD3"));
      // commenting this for JIRA CMR-3872
      // data.setSpecialTaxCd((String) record.get("TAX_EXEMPT_STATUS"));
      // always reset to blank
      data.setSpecialTaxCd("");
      data.setBpName((String) record.get("BP_NAME"));
      data.setIccTaxClass((String) record.get("ICC_TAX_CLASS"));
      data.setIccTaxExemptStatus((String) record.get("ICC_TAX_EXEMPT_STATUS"));
      data.setNonIbmCompanyInd((String) record.get("NON_IBM_COMPANY_IND"));
      // data.setcom((String) record.get("COMPANY_NM"));
      data.setDiv((String) record.get("DIV"));
      data.setDept((String) record.get("DEPT"));
      data.setFunc((String) record.get("FUNC"));
      data.setUser((String) record.get("USER"));
      data.setLoc((String) record.get("LOC"));
      data.setTaxCd2((String) record.get("TAX_CD2"));
      data.setUsSicmen((String) record.get("SICMEN"));
      // data.set((String) record.get("TAX_CD1"));
      if (StringUtils.isBlank(data.getAbbrevNm())) {
        data.setAbbrevNm((String) record.get("ABBREV_NM"));
      }

      if (record.get("COMPANY_NM") != null && !StringUtils.isEmpty(record.get("COMPANY_NM").toString())) {
        this.legalName = (String) record.get("COMPANY_NM");
      }

      String isic = (String) record.get("ISIC");
      if (!StringUtils.isBlank(isic)) {
        data.setIsicCd((String) record.get("ISIC"));
        // String newSubInd = getSubIndusryValue(entityManager,
        // record.get("ISIC").toString());
        // if (StringUtils.isNotBlank(newSubInd)) {
        // data.setSubIndustryCd(newSubInd);
        // }
      }

      if ((model.getItems() != null && model.getItems().size() == 1)) {
        LOG.debug("Forcing a second address...");
        FindCMRRecordModel newRecord = new FindCMRRecordModel();
        FindCMRRecordModel oldRecord = model.getItems().get(0);
        newRecord.setCmrAddrTypeCode("ZI01");
        newRecord.setCmrCountryLanded(oldRecord.getCmrCountryLanded());
        newRecord.setCmrCity(oldRecord.getCmrCity());
        newRecord.setCmrCity2(oldRecord.getCmrCity2());
        newRecord.setCmrPostalCode(oldRecord.getCmrPostalCode());
        newRecord.setCmrCountyCode(oldRecord.getCmrCountyCode());
        newRecord.setCmrCounty(oldRecord.getCmrCounty());
        model.getItems().add(newRecord);
      }
      return true;
    }
  }

  /**
   * 
   * @param url
   * @param data
   * @throws Exception
   * @deprecated - use setCodesFromLOV
   */
  @Deprecated
  protected void setCodes(String url, Data data) throws Exception {
    LOG.debug("Getting code equivalents from PDA..");
    String sql = ExternalizedQuery.getSql("IMPORT.US.CODES");

    String restrictTo = StringUtils.isEmpty(data.getRestrictTo()) ? "'$$$'" : data.getRestrictTo().trim();
    String bpName = StringUtils.isEmpty(data.getBpName()) ? "'$$$'" : data.getBpName().trim();
    sql = StringUtils.replace(sql, ":RESTRICT_TO", "'" + restrictTo + "'");
    sql = StringUtils.replace(sql, ":BP_NAME", "'" + bpName + "'");

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("FIELD");
    query.addField("US_VAL");
    query.addField("CMR_VAL");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);

    // no PDA id anymore in QueryClient
    QueryResponse response = client.executeAndWrap("", query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null) {
      for (Map<String, Object> record : response.getRecords()) {
        if ("RESTRICT_TO".equals(record.get("FIELD"))) {
          data.setRestrictTo((String) record.get("CMR_VAL"));
        } else if ("BP_NAME".equals(record.get("FIELD"))) {
          data.setBpName((String) record.get("CMR_VAL"));
        }
      }
    }

  }

  private void setCodesFromLOV(EntityManager entityManager, String url, Data data) throws Exception {
    LOG.debug("Getting code equivalents from LOV..");
    String sql = ExternalizedQuery.getSql("US.GETCODES.LOV");

    String restrictTo = StringUtils.isEmpty(data.getRestrictTo()) ? "Z#" : data.getRestrictTo().trim();
    String bpName = StringUtils.isEmpty(data.getBpName()) ? "Z#" : data.getBpName().trim();
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RESTRICT_TO", restrictTo);
    query.setParameter("BP_NAME", bpName);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      for (Object[] values : results) {
        if ("RESTRICT_TO".equals(values[0])) {
          data.setRestrictTo((String) values[1]);
        }
        if ("BP_NAME".equals(values[0])) {
          data.setBpName((String) values[1]);
        }
      }
      LOG.debug("Code Values retrieved " + data.getRestrictTo() + " and " + data.getBpName());
    } else {
      LOG.debug("Cannot determine Code Values for " + data.getRestrictTo() + " and " + data.getBpName());
      data.setRestrictTo(null);
      data.setBpName(null);
    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel cmr, String cmrNo) throws Exception {
    // try US CMR DB first
    if (cmrNo != null) {
      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
      String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
      String sql = ExternalizedQuery.getSql("IMPORT.US.USCMR.ADDR", usSchema);
      QueryRequest query = new QueryRequest();
      query.setRows(1);
      query.addField("T_ADDR_LINE_1");
      query.addField("T_ADDR_LINE_2");
      query.addField("T_ADDR_LINE_3");
      query.addField("T_ADDR_LINE_4");
      query.addField("N_ST");
      query.addField("N_CITY");
      query.addField("POST_CD");
      sql = StringUtils.replace(sql, ":CMR_NO", "'" + cmrNo + "'");

      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(address.getId().getAddrType())) {
        sql = StringUtils.replace(sql, ":ADDR_TYPE", "1");
      } else if (CmrConstants.ADDR_TYPE.ZI01.toString().equals(address.getId().getAddrType())) {
        sql = StringUtils.replace(sql, ":ADDR_TYPE", "3");
      }
      query.setSql(sql);

      QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);

      QueryResponse response = client.executeAndWrap(QueryClient.USCMR_APP_ID, query, QueryResponse.class);
      if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() > 0) {
        Map<String, Object> record = response.getRecords().get(0);
        address.setDivn((String) record.get("T_ADDR_LINE_1"));
        address.setDept((String) record.get("T_ADDR_LINE_2"));
        address.setAddrTxt((String) record.get("T_ADDR_LINE_3"));
        address.setStateProv((String) record.get("N_ST"));
        if ("US".equals(address.getLandCntry())) {
          address.setCity1((String) record.get("N_CITY"));
          address.setAddrTxt2((String) record.get("T_ADDR_LINE_4"));
        } else {
          address.setCity1((String) record.get("T_ADDR_LINE_4"));
          address.setAddrTxt2(null);
        }
        String post = (String) record.get("POST_CD");
        if (post != null && post.length() > 5) {
          while (post.length() < 9) {
            post = "0" + post;
          }
          address.setPostCd(post.substring(0, 5) + "-" + post.substring(5));
        } else {
          while (post.length() < 5) {
            post = "0" + post;
          }
          address.setPostCd(post);
        }
      } else {
        // break everything here if needed
        String addrTxt = address.getAddrTxt();
        if (addrTxt != null && addrTxt.length() > 24) {
          splitAddress(address, address.getAddrTxt(), "", 24, 24);
        }
      }
    } else {
      // break everything here if needed
      String addrTxt = address.getAddrTxt();
      if (addrTxt != null && addrTxt.length() > 24) {
        splitAddress(address, address.getAddrTxt(), "", 24, 24);
      }
    }
    // no cmr no, manual parse
    String postCd = address.getPostCd();
    if (postCd.trim().length() == 9 && !postCd.contains("-")) {
      postCd = postCd.substring(0, 5) + "-" + postCd.substring(5);
      LOG.debug("Postal code formatted: " + postCd);
      address.setPostCd(postCd);
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    String[] parts = null;
    if (!StringUtils.isBlank(this.legalName)) {
      parts = splitName(this.legalName, null, 28, 24);
      admin.setMainCustNm1(parts[0]);
      admin.setOldCustNm1(parts[0]);
      admin.setMainCustNm2(parts[1]);
      admin.setOldCustNm2(parts[1]);
    } else {
      String name1 = admin.getMainCustNm1();
      String name2 = admin.getMainCustNm2();
      parts = splitName(name1, name2, 28, 24);
      admin.setMainCustNm1(parts[0]);
      admin.setOldCustNm1(parts[0]);
      admin.setMainCustNm2(parts[1]);
      admin.setOldCustNm2(parts[1]);
    }

  }

  public boolean checkIfTerritory(String land1) throws CmrException {
    // EntityManager entityManager = JpaManager.getEntityManager();
    //
    // try {
    // String sql = ExternalizedQuery.getSql("QUERY.US.STATE.PROV");
    // PreparedQuery query = new PreparedQuery(entityManager, sql);
    // query.setParameter("STATE_PROV_CD", land1);
    // return query.exists();
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // // only wrap non CmrException errors
    // if (e instanceof CmrException) {
    // throw (CmrException) e;
    // } else {
    // e.printStackTrace();
    // throw new CmrException(MessageUtil.ERROR_GENERAL);
    // }
    // } finally {
    // // empty the manager
    // entityManager.clear();
    // entityManager.close();
    // }
    return Arrays.asList(US_TERRITORIES).contains(land1);
  }

  @Override
  public int getName1Length() {
    return 28;
  }

  @Override
  public int getName2Length() {
    return 24;
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
    // noop
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCustPrefLang(LANG_ENGLISH);
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    // noop
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    if (importing) {
      if (!CmrConstants.REQ_TYPE_UPDATE.equals(requestType)) {
        admin.setCustType(null);
        data.setCustGrp(CmrConstants.CREATE_BY_MODEL_GROUP);
        data.setCustSubGrp(CmrConstants.CREATE_BY_MODEL_SUB_GROUP);
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(requestType)) {
        data.setCustGrp(null);
        data.setCustSubGrp(null);
      }
    }
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getIsicCd(), newData.getIsicCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "USSicmen", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getIsicCd(), "USSicmen", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getIsicCd(), "USSicmen", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getRestrictTo(), newData.getRestrictTo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "RestrictTo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRestrictTo(), "RestrictTo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRestrictTo(), "RestrictTo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getRestrictInd(), newData.getRestrictInd())) {
      update = new UpdatedDataModel();
      update.setDataField("Restricted");
      update.setNewData(newData.getRestrictInd());
      update.setOldData(oldData.getRestrictInd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOemInd(), newData.getOemInd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OEMInd", "-"));
      update.setNewData(newData.getOemInd());
      update.setOldData(oldData.getOemInd());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getFedSiteInd(), newData.getFedSiteInd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "FederalSiteInd", "-"));
      update.setNewData(newData.getFedSiteInd());
      update.setOldData(oldData.getFedSiteInd());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOutCityLimit(), newData.getOutCityLimit())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OutOfCityLimits", "-"));
      update.setNewData(newData.getOutCityLimit());
      update.setOldData(oldData.getOutCityLimit());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getRestrictTo(), newData.getRestrictTo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "NonIBMCompInd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getNonIbmCompanyInd(), "NonIBMCompInd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getNonIbmCompanyInd(), "NonIBMCompInd", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCsoSite(), newData.getCsoSite())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CSOSite", "-"));
      update.setNewData(newData.getCsoSite());
      update.setOldData(oldData.getCsoSite());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getIccTaxClass(), newData.getIccTaxClass())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ICCTaxClass", "-"));
      update.setNewData(newData.getIccTaxClass());
      update.setOldData(oldData.getIccTaxClass());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getIccTaxExemptStatus(), newData.getIccTaxExemptStatus())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ICCTaxExemptStatus", "-"));
      update.setNewData(newData.getIccTaxExemptStatus());
      update.setOldData(oldData.getIccTaxExemptStatus());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSizeCd(), newData.getSizeCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SizeCode", "-"));
      update.setNewData(newData.getSizeCd());
      update.setOldData(oldData.getSizeCd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getMiscBillCd(), newData.getMiscBillCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "MiscBillCode", "-"));
      update.setNewData(newData.getMiscBillCd());
      update.setOldData(oldData.getMiscBillCd());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getBpAcctTyp(), newData.getBpAcctTyp())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "BPAccountType", "-"));
      update.setNewData(newData.getBpAcctTyp());
      update.setOldData(oldData.getBpAcctTyp());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getBpName(), newData.getBpName())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "BPName", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getBpName(), "BPName", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getBpName(), "BPName", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getDiv(), newData.getDiv())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InternalDivision", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getDiv(), "InternalDivision", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getDiv(), "InternalDivision", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getDept(), newData.getDept())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InternalDivDept", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getDept(), "InternalDivDept", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getDept(), "InternalDivDept", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getUser(), newData.getUser())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InternalUser", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getUser(), "InternalUser", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getUser(), "InternalUser", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getFunc(), newData.getFunc())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InternalFunction", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getFunc(), "InternalFunction", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getFunc(), "InternalFunction", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getLoc(), newData.getLoc())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InternalFunction", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getLoc(), "InternalLocation", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getLoc(), "InternalLocation", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getMktgDept(), newData.getMktgDept())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "MarketingDept", "-"));
      update.setNewData(newData.getMktgDept());
      update.setOldData(oldData.getMktgDept());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getMtkgArDept(), newData.getMtkgArDept())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "MarketingARDept", "-"));
      update.setNewData(newData.getMtkgArDept());
      update.setOldData(oldData.getMtkgArDept());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getPccMktgDept(), newData.getPccMktgDept())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "PCCMarketingDept", "-"));
      update.setNewData(newData.getPccMktgDept());
      update.setOldData(oldData.getPccMktgDept());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getPccArDept(), newData.getPccArDept())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "PCCARDept", "-"));
      update.setNewData(newData.getPccArDept());
      update.setOldData(oldData.getPccArDept());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSvcTerritoryZone(), newData.getSvcTerritoryZone())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SVCTerritoryZone", "-"));
      update.setNewData(newData.getSvcTerritoryZone());
      update.setOldData(oldData.getSvcTerritoryZone());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSvcArOffice(), newData.getSvcArOffice())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SVCAROffice", "-"));
      update.setNewData(newData.getSvcArOffice());
      update.setOldData(oldData.getSvcArOffice());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSpecialTaxCd(), newData.getSpecialTaxCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SpecialTaxCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSpecialTaxCd(), "SpecialTaxCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSpecialTaxCd(), "SpecialTaxCd", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getUsSicmen(), newData.getUsSicmen())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ISIC", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getUsSicmen(), "ISIC", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getUsSicmen(), "ISIC", cmrCountry));
      results.add(update);
    }

  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  private boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    String scenario = data.getCustSubGrp();
    if ("CSP".equals(scenario) || "CSP".equals(data.getReqReason())) {
      LOG.debug("Setting KUKLA to CSP for Coverage");
      request.setClassification("52");
    }
    if (!"XCSP".equals(data.getReqReason()) && "52".equals(data.getCustClass())) {
      LOG.debug("Clear CSP KUKLA for Coverage");
      request.setClassification(null);
    }
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return false;
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    if (admin != null && "CSP".equals(admin.getReqReason())) {
      data.setIsuCd("32");
      data.setClientTier("N");
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public boolean customerNamesOnAddress() {
    return false;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return false;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    if ("ISIC".equals(field)) {
      return true;
    }
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    return Arrays.asList("LAND_CNTRY", "ADDR_TXT", "ADDR_TXT2", "STATE_PROV", "CITY1", "CITY2", "COUNTY", "POST_CD", "DIVN", "DEPT", "PO_BOX", "BLDG",
        "FLOOR", "CUST_PHONE", "CUST_FAX");
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void convertDnBImportValues(EntityManager entityManager, Admin admin, Data data) {
    // move ISIC to SICMEN
    data.setUsSicmen(data.getIsicCd());
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##AbbrevName", "abbrevNm");
    map.put("##AddressTypeInput", "addrType");
    map.put("##Affiliate", "affiliate");
    map.put("##BPAccountType", "bpAcctTyp");
    map.put("##BPName", "bpName");
    map.put("##BPRelationType", "bpRelType");
    map.put("##Building", "bldg");
    map.put("##BuyingGroupID", "bgId");
    map.put("##CAP", "capInd");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##CMRNumber", "cmrNo");
    map.put("##CMROwner", "cmrOwner");
    map.put("##CSOSite", "csoSite");
    map.put("##City1", "city1");
    map.put("##City2", "city2");
    map.put("##ClientTier", "clientTier");
    map.put("##Company", "company");
    map.put("##County", "county");
    map.put("##CoverageID", "covId");
    map.put("##CustFAX", "custFax");
    map.put("##CustLang", "custPrefLang");
    map.put("##CustPhone", "custPhone");
    map.put("##CustomerGroup", "custGrp");
    map.put("##CustomerSubGroup", "custSubGrp");
    map.put("##CustomerType", "custType");
    map.put("##DUNS", "dunsNo");
    map.put("##Department", "dept");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Division", "divn");
    map.put("##Enterprise", "enterprise");
    map.put("##Expedite", "expediteInd");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##FederalSiteInd", "fedSiteInd");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##ICCTaxClass", "iccTaxClass");
    map.put("##ICCTaxExemptStatus", "iccTaxExemptStatus");
    map.put("##INACCode", "inacCd");
    map.put("##INACType", "inacType");
    map.put("##ISIC", "isicCd");
    map.put("##ISU", "isuCd");
    map.put("##InternalDept", "dept");
    map.put("##InternalDivDept", "dept");
    map.put("##InternalDivision", "div");
    map.put("##InternalFunction", "func");
    map.put("##InternalLocation", "loc");
    map.put("##InternalUser", "user");
    map.put("##LandedCountry", "landCntry");
    map.put("##LocalTax1", "taxCd1");
    map.put("##LocalTax2", "taxCd2");
    map.put("##LocalTax3", "taxCd3");
    map.put("##MainCustomerName1", "mainCustNm1");
    map.put("##MainCustomerName2", "mainCustNm1");
    map.put("##MarketingARDept", "mtkgArDept");
    map.put("##MarketingDept", "mktgDept");
    map.put("##MembLevel", "memLvl");
    map.put("##MiscBillCode", "miscBillCd");
    map.put("##NonIBMCompInd", "nonIbmCompanyInd");
    map.put("##OEMInd", "oemInd");
    map.put("##Office", "office");
    map.put("##OriginatorID", "originatorId");
    map.put("##OutOfCityLimits", "outCityLimit");
    map.put("##PCCARDept", "pccArDept");
    map.put("##PCCMarketingDept", "pccMktgDept");
    map.put("##PPSCEID", "ppsceid");
    map.put("##PostalCode", "postCd");
    map.put("##RequestID", "reqId");
    map.put("##RequestReason", "reqReason");
    map.put("##RequestStatus", "reqStatus");
    map.put("##RequestType", "reqType");
    map.put("##RequesterID", "requesterId");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##RestrictTo", "restrictTo");
    map.put("##RestrictedInd", "restrictInd");
    map.put("##SAPNumber", "sapNo");
    map.put("##SOENumber", "soeReqNo");
    map.put("##SVCAROffice", "svcArOffice");
    map.put("##SVCTerritoryZone", "svcTerritoryZone");
    map.put("##SearchTerm", "searchTerm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##StateProv", "stateProv");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##TransportZone", "transportZone");
    map.put("##SizeCode", "sizeCd");
    map.put("##CustClass", "custClass");
    return map;
  }

  @Override
  public void setGBGValues(EntityManager entityManager, RequestData requestData, String ldeField, String ldeValue) {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    if (!"C".equals(admin.getReqType())) {
      return;
    }
    if ("AFFNO".equals(ldeField)) {
      if (StringUtils.isNumeric(ldeValue)) {
        data.setEnterprise(ldeValue);
      }
      String inac = getINACForAffiliate(entityManager, ldeValue);
      if (!StringUtils.isBlank(inac)) {
        data.setInacCd(inac);
        data.setInacType(StringUtils.isNumeric(inac) ? "I" : "N");
      }
    } else if ("INAC".equals(ldeField)) {
      String affiliate = getAffiliateForINAC(entityManager, ldeValue);
      if (!StringUtils.isBlank(affiliate)) {
        data.setAffiliate(affiliate);
        if (StringUtils.isNumeric(affiliate)) {
          data.setEnterprise(affiliate);
        }
      }
    }
  }

  /**
   * Gets the Affiliate value assigned with the most CMRs under the INAC
   * 
   * @param entityManager
   * @param inac
   * @return
   */
  private String getAffiliateForINAC(EntityManager entityManager, String inac) {
    LOG.debug("Getting Affiliate for INAC " + inac);
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_AFF_FOR_INAC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("INAC", inac);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] top = results.get(0);
      LOG.debug("Found Affiliate: " + top[0] + " with " + top[1] + " CMRs assigned.");
      return (String) top[0];
    }
    return null;
  }

  /**
   * Gets the Affiliate value assigned with the most CMRs under the INAC
   * 
   * @param entityManager
   * @param inac
   * @return
   */
  private String getINACForAffiliate(EntityManager entityManager, String affiliate) {
    LOG.debug("Getting INAC for Affiliate " + affiliate);
    String sql = ExternalizedQuery.getSql("AUTO.US.GET_INAC_FOR_AFF");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("AFFILIATE", affiliate);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] top = results.get(0);
      LOG.debug("Found INAC: " + top[0] + " with " + top[1] + " CMRs assigned.");
      return (String) top[0];
    }
    return null;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

  protected String getSubIndusryValue(EntityManager entityManager, String isic) {
    String subInd = "";
    String sql = ExternalizedQuery.getSql("US.GET.SUBIND_FOR_ISIC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISIC", isic);
    subInd = query.getSingleResult(String.class);
    if (subInd != null) {
      return subInd;
    }
    return subInd;
  }
}
