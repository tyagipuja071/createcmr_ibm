/**
 *
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.KnvvExt;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.USTaxData;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.DataService;
import com.ibm.cio.cmr.request.service.requestentry.RequestEntryService;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
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

  private static final List PROLIFERATION_CNTRY = Arrays.asList("AF", "AM", "AZ", "BH", "BY", "KH", "CN", "CU", "EG", "GE", "IR", "IQ", "IL", "JO",
      "KZ", "KP", "KW", "KG", "LA", "LB", "LY", "MO", "MD", "MN", "MM", "OM", "PK", "QA", "RU", "SA", "SD", "SY", "TW", "TJ", "TM", "UA", "AE", "UZ",
      "VE", "VN", "YE");

  private String legalName;

  private EntityManager entityManager;

  private String processType;

  private final DataService dataService = new DataService();

  private String copyZI01Flag = "N";

  private String addFlag = "N";

  private String addZI01Flag = "N";

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    this.entityManager = entityManager;
    LOG.trace("Converting records for US");
    String[] seqArray = new String[] { "001", "002" };
    List<String> seqList = Arrays.asList(seqArray);
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
          record.setCmrAddrSeq("001");
        } else if ("ZP01".equals(record.getCmrAddrTypeCode()) && seqList.contains(record.getCmrAddrSeq())) {
          // set the address type to Invoice To for CreateCMR
          record.setCmrAddrTypeCode("ZI01");
          record.setCmrAddrSeq("002");
          addZI01Flag = "Y";
        }
      }
      if (StringUtils.isNotBlank(record.getCmrAddrSeq()) && "ZP01".equals(record.getCmrAddrTypeCode())
          && Integer.parseInt(record.getCmrAddrSeq()) >= 200) {
        record.setCmrAddrTypeCode("PG01");
      }
      record.setCmrDept(record.getCmrName4());
      record.setCmrStreetAddressCont(record.getCmrName3());

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

    // check if ZP01 records exist in RDC & import
    // List<FindCMRRecordModel> record2 = source.getItems();
    // List<FindCMRRecordModel> addressesList = null;
    // addressesList = getZP01FromRDC(entityManager, main.getCmrNum());
    // if (!addressesList.isEmpty() && addressesList.size() > 0) {
    // converted.addAll(addressesList);
    // }

    // CREATCMR-6433
    if (!"Y".equals(addZI01Flag)) {
      for (FindCMRRecordModel record : converted) {
        if ("BPQS".equals(record.getUsCmrRestrictTo())) {
          if (!"ZI01".equals(record.getCmrAddrTypeCode())) {
            if (!"PG".equals(record.getCmrOrderBlock())) {
              copyZI01Flag = "Y";
            }
          }
        }
      }

      if ("Y".equals(copyZI01Flag)) {
        FindCMRRecordModel zi01 = new FindCMRRecordModel();
        zi01.setCmrAddrTypeCode("ZI01");
        zi01.setCmrAddrSeq("002");
        zi01.setCmrCountryLanded("US");
        zi01.setCmrState("NY");
        zi01.setCmrCity("ARMONK");
        zi01.setCmrStreetAddress("1 N CASTLE DR MD NC313");
        zi01.setCmrPostalCode("10504-1725");

        addFlag = "Y";
        converted.add(zi01);
      }
    }
    // CREATCMR-6433

    Collections.sort(converted);
    source.setItems(converted);

  }

  private List<FindCMRRecordModel> getZP01FromRDC(EntityManager entityManager, String cmrNo) {
    FindCMRRecordModel address = null;
    List<FindCMRRecordModel> addressList = new ArrayList<FindCMRRecordModel>();
    String sqlRDC = ExternalizedQuery.getSql("KNA1.US.MULTIPLE_BILLTO");
    PreparedQuery queryRDC = new PreparedQuery(entityManager, sqlRDC);
    queryRDC.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    queryRDC.setParameter("ZZKV_CUSNO", cmrNo);
    queryRDC.setForReadOnly(true);

    List<Kna1> kna1List = queryRDC.getResults(Kna1.class);
    if (kna1List != null && !kna1List.isEmpty() && kna1List.size() > 0) {
      for (Kna1 kna1 : kna1List) {
        address = new FindCMRRecordModel();
        // paygo billing
        address.setCmrAddrTypeCode(CmrConstants.RDC_PAYGO_BILLING);
        address.setCmrName(kna1.getName1());
        address.setCmrName2(kna1.getName2());
        address.setCmrAddrSeq(kna1.getZzkvSeqno());
        address.setCmrStreetAddress(kna1.getStras());
        address.setCmrCountryLanded(kna1.getLand1());
        address.setCmrPostalCode(kna1.getPstlz());
        if (kna1.getName4().length() >= 24) {
          address.setCmrDept(kna1.getName4().substring(0, 24));
        } else {
          address.setCmrDept(kna1.getName4());
        }
        address.setCmrCity(kna1.getOrt01());
        address.setCmrState(kna1.getRegio());
        address.setCmrCounty(kna1.getCounc());
        address.setCmrAddrType(kna1.getKtokd());
        address.setCmrSapNumber(kna1.getId().getKunnr());
        address.setCmrSitePartyID(kna1.getBran5());
        addressList.add(address);
      }
    }

    return addressList;
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel cmr) throws Exception {

    // String processingType = PageManager.getProcessingType("897", "U");

    String processingType = getProcessingTypeForUS(entityManager, "897");
    processType = processingType;
    LOG.debug("processType = " + processType);

    String cmrNo = cmr.getCmrNum();
    if (!NumberUtils.isDigits(cmrNo)) {
      LOG.debug("Non-digits found. Maybe Prospect/Lite conversion.");
      return;
    }

    String ordBlk = getAufsdCombFromRdc(entityManager, SystemConfiguration.getValue("MANDT"), cmr.getCmrNum());
    data.setOrdBlk(ordBlk);

    if (!StringUtils.isEmpty(cmr.getCmrIsic())) {
      data.setUsSicmen(cmr.getCmrIsic());
    }

    String zzkvLic = getZzkvLicFromRdc(entityManager, SystemConfiguration.getValue("MANDT"), cmr.getCmrNum());
    if (!StringUtils.isEmpty(zzkvLic)) {
      data.setIsicCd(zzkvLic);
    }

    // retrieve BP data on import
    // String bpType = getBPDataFromRdc(entityManager,
    // SystemConfiguration.getValue("MANDT"), cmr.getCmrSapNumber());

    String bpType = cmr.getUsCmrBpAccountType();
    if (!StringUtils.isEmpty(bpType)) {
      data.setBpAcctTyp(bpType);
    }

    // retrieve US_Tax_Data on import
    USTaxData uTxData = getUSTaxDataById(entityManager, SystemConfiguration.getValue("MANDT"), cmr.getCmrSapNumber());

    if (uTxData != null) {

      if (!StringUtils.isEmpty(uTxData.getcTeCertST1())) {
        data.setSpecialTaxCd(uTxData.getcTeCertST1());
        data.setTaxExemptStatus1(uTxData.getcTeCertST1());
      }
      if (!StringUtils.isEmpty(uTxData.getcTeCertST2())) {
        data.setTaxExemptStatus2(uTxData.getcTeCertST2());
      }
      if (!StringUtils.isEmpty(uTxData.getcTeCertST3())) {
        data.setTaxExemptStatus3(uTxData.getcTeCertST3());
      }

      String taxcd1 = uTxData.getiTypeCust1() + uTxData.getiTaxClass1();
      String taxcd2 = uTxData.getiTypeCust2() + uTxData.getiTaxClass2();
      String taxcd3 = uTxData.getiTypeCust3() + uTxData.getiTaxClass3();

      if (StringUtils.isEmpty(uTxData.getiTypeCust1()) || StringUtils.isEmpty(uTxData.getiTaxClass1())) {
        data.setTaxCd1("");
      } else {
        data.setTaxCd1(taxcd1);
      }
      if (StringUtils.isEmpty(uTxData.getiTypeCust2()) || StringUtils.isEmpty(uTxData.getiTaxClass2())) {
        data.setTaxCd2("");
      } else {
        data.setTaxCd2(taxcd2);
      }
      if (StringUtils.isEmpty(uTxData.getiTypeCust3()) || StringUtils.isEmpty(uTxData.getiTaxClass3())) {
        data.setTaxCd3("");
      } else {
        data.setTaxCd3(taxcd3);
      }

      if (!StringUtils.isEmpty(uTxData.getcICCTe())) {
        data.setIccTaxExemptStatus(uTxData.getcICCTe());
      }
      if (!StringUtils.isEmpty(uTxData.getCICCTaxClass())) {
        data.setIccTaxClass(uTxData.getCICCTaxClass());
      }
      if (!StringUtils.isEmpty(uTxData.getfOCL())) {
        data.setOutCityLimit(uTxData.getfOCL());
      }
      if (!StringUtils.isEmpty(uTxData.getEaStatus())) {
        data.setEducAllowCd(uTxData.getEaStatus());
      }
    }

    if (!StringUtils.isEmpty(cmr.getUsCmrOemInd())) {
      data.setOemInd(cmr.getUsCmrOemInd());
    }

    // retrieve knvvExt data on import
    KnvvExt knvvExt = getKnvvExtById(entityManager, SystemConfiguration.getValue("MANDT"), cmr.getCmrSapNumber());

    if (knvvExt != null) {
      // Miscellaneous Bill Code
      data.setMiscBillCd(knvvExt.getMiscBilling());

    }

    List<FindCMRRecordModel> records = results.getItems();
    FindCMRRecordModel main = records != null && records.size() > 0 ? records.get(0) : new FindCMRRecordModel();

    // CSO Site
    data.setCsoSite(main.getUsCmrCsoSite());

    // Marketing A/R Department
    data.setMtkgArDept(main.getUsCmrMktgArDept());

    // Marketing Department
    data.setMktgDept(main.getUsCmrMktgDept());

    // PCC A/R Department
    data.setPccArDept(main.getUsCmrPccArBo());

    // PCC Marketing Department
    data.setPccMktgDept(main.getUsCmrPccMktgBo());

    // SVC A/R Office
    data.setSvcArOffice(main.getUsCmrSvcArOfc());

    // Restrict To
    data.setRestrictTo(main.getUsCmrRestrictTo());

    // Tax Class / Code 1
    // String taxcd1 = main.getUsCmrTaxType1() + main.getUsCmrTaxClass1();
    // if (StringUtils.isEmpty(main.getUsCmrTaxType1()) ||
    // StringUtils.isEmpty(main.getUsCmrTaxClass1())) {
    // data.setTaxCd1("");
    // } else {
    // data.setTaxCd1(taxcd1);
    // }

    // Tax Class / Code 2
    // String taxcd2 = main.getUsCmrTaxType2() + main.getUsCmrTaxClass2();
    // if (StringUtils.isEmpty(main.getUsCmrTaxType2()) ||
    // StringUtils.isEmpty(main.getUsCmrTaxClass2())) {
    // data.setTaxCd2("");
    // } else {
    // data.setTaxCd2(taxcd2);
    // }

    // Tax Class / Code 3
    // String taxcd3 = main.getUsCmrTaxType3() + main.getUsCmrTaxClass3();
    // if (StringUtils.isEmpty(main.getUsCmrTaxType3()) ||
    // StringUtils.isEmpty(main.getUsCmrTaxClass3())) {
    // data.setTaxCd3("");
    // } else {
    // data.setTaxCd3(taxcd3);
    // }

    data.setBpName(cmr.getUsCmrBpAbbrevNm());

    // Non-IBM Company
    // data.setNonIbmCompanyInd(main.getUsCmrNonIbmCompInd());

    if ("TC".equals(processingType)) {

      String url = SystemConfiguration.getValue("CMR_SERVICES_URL");

      String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
      // try US CMR DB first
      String sql = ExternalizedQuery.getSql("IMPORT.US.USCMR", usSchema);
      sql = StringUtils.replace(sql, ":CMR_NO", "'" + data.getCmrNo() + "'");
      String dbId = QueryClient.USCMR_APP_ID;

      LOG.debug("Getting existing values from US CMR DB..");
      boolean retrieved = queryAndAssign(url, sql, results, data, admin, dbId);

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
      } else if (!retrieved && isPoolProcessing()) {
        // do nothing and return
        return;
      } else {
        throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
      }
    }

    // retrieve US_Tax_Data on import

    if ("TC".equals(processingType)) {
      if (uTxData != null) {

        if (!StringUtils.isEmpty(uTxData.getcTeCertST1())) {
          data.setSpecialTaxCd(uTxData.getcTeCertST1());
          data.setTaxExemptStatus1(uTxData.getcTeCertST1());
        }
        if (!StringUtils.isEmpty(uTxData.getcTeCertST2())) {
          data.setTaxExemptStatus2(uTxData.getcTeCertST2());
        }
        if (!StringUtils.isEmpty(uTxData.getcTeCertST3())) {
          data.setTaxExemptStatus3(uTxData.getcTeCertST3());
        }

        String taxcd1ForTC = uTxData.getiTypeCust1() + uTxData.getiTaxClass1();
        String taxcd2ForTC = uTxData.getiTypeCust2() + uTxData.getiTaxClass2();
        String taxcd3ForTC = uTxData.getiTypeCust3() + uTxData.getiTaxClass3();

        if (StringUtils.isEmpty(uTxData.getiTypeCust1()) || StringUtils.isEmpty(uTxData.getiTaxClass1())) {
          data.setTaxCd1("");
        } else {
          data.setTaxCd1(taxcd1ForTC);
        }
        if (StringUtils.isEmpty(uTxData.getiTypeCust2()) || StringUtils.isEmpty(uTxData.getiTaxClass2())) {
          data.setTaxCd2("");
        } else {
          data.setTaxCd2(taxcd2ForTC);
        }
        if (StringUtils.isEmpty(uTxData.getiTypeCust3()) || StringUtils.isEmpty(uTxData.getiTaxClass3())) {
          data.setTaxCd3("");
        } else {
          data.setTaxCd3(taxcd3ForTC);
        }

        if (!StringUtils.isEmpty(uTxData.getcICCTe())) {
          data.setIccTaxExemptStatus(uTxData.getcICCTe());
        }
        if (!StringUtils.isEmpty(uTxData.getCICCTaxClass())) {
          data.setIccTaxClass(uTxData.getCICCTaxClass());
        }
        if (!StringUtils.isEmpty(uTxData.getfOCL())) {
          data.setOutCityLimit(uTxData.getfOCL());
        }
        if (!StringUtils.isEmpty(uTxData.getEaStatus())) {
          data.setEducAllowCd(uTxData.getEaStatus());
        }
      }
    }

  }

  private boolean queryAndAssign(String url, String sql, FindCMRResultModel model, Data data, Admin admin, String dbId) throws Exception {

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
      // 2022.05.25
      // data.setSpecialTaxCd("");
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
      // 2022.05.25
      // data.set((String) record.get("TAX_CD1"));
      data.setTaxCd1((String) record.get("TAX_CD1"));
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

      // skipping below for FedCMR -> CREATCMR -6117
      if (!"FEDCMR".equalsIgnoreCase(admin.getSourceSystId()) && (model.getItems() != null && model.getItems().size() == 1)) {
        LOG.debug("Forcing a second address...");
        FindCMRRecordModel newRecord = new FindCMRRecordModel();
        FindCMRRecordModel oldRecord = model.getItems().get(0);
        newRecord.setCmrAddrTypeCode("ZI01");
        newRecord.setCmrAddrSeq("002");
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
    // String processingType = PageManager.getProcessingType("897", "U");
    // String processingType = getProcessingTypeForUS(entityManager, "897");

    if (cmrNo != null && "TC".equals(processType)) {
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

      boolean skipQuery = false;
      // add skip query
      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(address.getId().getAddrType())) {
        sql = StringUtils.replace(sql, ":ADDR_TYPE", "1");
      } else if (CmrConstants.ADDR_TYPE.ZI01.toString().equals(address.getId().getAddrType())) {
        sql = StringUtils.replace(sql, ":ADDR_TYPE", "3");
      } else {
        skipQuery = true;
      }
      query.setSql(sql);

      QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);

      QueryResponse response = null;

      if (skipQuery) {
        response = new QueryResponse();
        response.setSuccess(false);
      } else {
        response = client.executeAndWrap(QueryClient.USCMR_APP_ID, query, QueryResponse.class);
      }

      if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() > 0) {
        Map<String, Object> record = response.getRecords().get(0);
        address.setDivn((String) record.get("T_ADDR_LINE_1"));
        address.setDept((String) record.get("T_ADDR_LINE_2"));
        address.setAddrTxt((String) record.get("T_ADDR_LINE_3"));
        address.setStateProv((String) record.get("N_ST"));
        if ("US".equals(address.getLandCntry())) {
          address.setCity1((String) record.get("N_CITY"));
          if (StringUtils.isEmpty(address.getDivn()) && !"E".equals(cmr.getUsCmrBpAccountType())) {
            address.setAddrTxt2((String) record.get("T_ADDR_LINE_4"));
          } else if ("E".equals(cmr.getUsCmrBpAccountType())) {
            address.setAddrTxt2((String) record.get("T_ADDR_LINE_4"));
          }
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
        if (!"E".equals(cmr.getUsCmrBpAccountType())) {
          String strAddrTxt2 = address.getAddrTxt2();
          if (StringUtils.isEmpty(address.getDivn()) && StringUtils.isEmpty(cmr.getCmrName3())
              && StringUtils.isEmpty(cmr.getCmrStreetAddressCont())) {
            if (StringUtils.isNotBlank(strAddrTxt2)) {
              address.setAddrTxt2(strAddrTxt2);
            }
          }
        } else if ("E".equals(cmr.getUsCmrBpAccountType())) {
          String strAddrTxt2 = address.getAddrTxt2();
          if (StringUtils.isNotBlank(strAddrTxt2)) {
            address.setAddrTxt2(strAddrTxt2);
          }
        }
      }
    } else {
      // break everything here if needed
      LOG.debug("setAddressValuesOnImport processType = " + processType);
      String addrTxt = address.getAddrTxt();
      if (addrTxt != null && addrTxt.length() > 24) {
        LOG.debug("setAddressValuesOnImport addrTxt > 24 ");
        splitAddress(address, address.getAddrTxt(), "", 24, 24);
      }
    }
    // no cmr no, manual parse
    String postCd = address.getPostCd();
    if (StringUtils.isNotBlank(postCd) && postCd.trim().length() == 9 && !postCd.contains("-")) {
      postCd = postCd.substring(0, 5) + "-" + postCd.substring(5);
      LOG.debug("Postal code formatted: " + postCd);
      address.setPostCd(postCd);
    }

    if (!"TC".equals(processType)) {
      // CREATCMR-6182
      String strAddrTxt2 = address.getAddrTxt2();
      if (StringUtils.isNotBlank(cmr.getCmrName3())) {
        // CREATCMR-6255
        // address.setAddrTxt2(cmr.getCmrName3());
        address.setDivn(cmr.getCmrName3());
        LOG.debug("setAddressValuesOnImport name3 ");
      } else if (StringUtils.isNotBlank(strAddrTxt2)) {
        // CREATCMR-6255
        // address.setAddrTxt2(strAddrTxt2);
        address.setDivn(strAddrTxt2);
        LOG.debug("setAddressValuesOnImport strAddrTxt2 ");
      }
      address.setAddrTxt2(null);

      LOG.debug("setAddressValuesOnImport reset Divn : " + address.getDivn() + " AddrTxt2 : " + address.getAddrTxt2());
    }

    // if (!"E".equals(cmr.getUsCmrBpAccountType())) {
    // if (StringUtils.isNotBlank(address.getDivn())) {
    // address.setAddrTxt2(null);
    // }
    // } else if ("E".equals(cmr.getUsCmrBpAccountType())) {
    // String strAddrTxt2 = address.getAddrTxt2();
    // if (StringUtils.isNotBlank(strAddrTxt2)) {
    // address.setAddrTxt2(strAddrTxt2);
    // }
    // }

    // CREATCMR-6183
    if (!"TC".equals(processType)) {
      if ("E".equals(cmr.getUsCmrBpAccountType())) {
        if ("ZS01".equals(address.getId().getAddrType())) {
          // || "ZI01".equals(address.getId().getAddrType())
          address.setDivn(cmr.getCmrName());
        }
      }
    }
    // CREATCMR-6183

    // CREATCMR-6183
    if (!"TC".equals(processType)) {

      if ("ZI01".equals(address.getId().getAddrType())) {
        if ("Y".equals(addFlag)) {
          address.setDivn("IBM CREDIT LLC");
          addFlag = "N";
        }
      }

      if ("E".equals(cmr.getUsCmrBpAccountType())) {
        if ("ZS01".equals(address.getId().getAddrType())) {
          // || "ZI01".equals(address.getId().getAddrType())
          address.setDivn(cmr.getCmrName());
        }
      }
    }
    // CREATCMR-6183

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

    String processingType = getProcessingTypeForUS(entityManager, "897");
    if ("US".equals(processingType)) {
      if ("E".equals(currentRecord.getUsCmrBpAccountType())) {
        parts = splitName(currentRecord.getUsCmrCompanyNm(), null, 28, 24);
        admin.setMainCustNm1(parts[0]);
        admin.setOldCustNm1(parts[0]);
        admin.setMainCustNm2(parts[1]);
        admin.setOldCustNm2(parts[1]);
      }
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
    // CREATCMR-6987
    if ("C".equals(admin.getReqType())) {
      if ("KYN".equalsIgnoreCase(data.getCustSubGrp())
          || ("BYMODEL".equalsIgnoreCase(data.getCustSubGrp()) && "KYN".equalsIgnoreCase(data.getRestrictTo()))) {
        admin.setMainCustNm1("KYNDRYL INC");
      }
    }

    if (admin != null && "CSP".equals(admin.getReqReason())) {
      data.setIsuCd("32");
      data.setClientTier("N");
    }

    // CREATCMR-4569
    if ("C".equals(admin.getReqType())) {
      if (StringUtils.isEmpty(data.getAbbrevNm())) {
        if (!StringUtils.isEmpty(admin.getMainCustNm1())) {
          if (admin.getMainCustNm1().length() >= 15) {
            data.setAbbrevNm(admin.getMainCustNm1().substring(0, 15));
          } else {
            data.setAbbrevNm(admin.getMainCustNm1().substring(0, admin.getMainCustNm1().length()));
          }
        }
      }

      if (StringUtils.isEmpty(data.getSearchTerm())) {
        if (!StringUtils.isEmpty(data.getAbbrevNm())) {
          if (data.getAbbrevNm().length() >= 10) {
            data.setSearchTerm(data.getAbbrevNm().substring(0, 10));
          } else {
            data.setSearchTerm(data.getAbbrevNm().substring(0, data.getAbbrevNm().length()));
          }
        }
      }

      // CREATCMR-6081
      if (StringUtils.isEmpty(data.getIccTaxClass())) {
        data.setIccTaxClass("000");
      }
      // CREATCMR-6081

      if ("END USER".equals(data.getCustSubGrp())
          || ("E".equals(data.getBpAcctTyp()) && ("BPQS".equals(data.getRestrictTo()) || "IRCSO".equals(data.getRestrictTo())))) {
        data.setAbbrevNm("");
        data.setSearchTerm("");
      }

      // CREATCMR-7145
      if ("KYN".equals(data.getCustSubGrp())) {
        data.setCustClass("11");
      }

    }

    data.setTaxExemptStatus1(data.getSpecialTaxCd());
    // CREATCMR-4569

    // CREATCMR-6342
    String scc = getSCCByReqId(entityManager, data.getId().getReqId());
    data.setCompanyNm(scc);
    // CREATCMR-6342

    // CREATCMR-7581
    if (!StringUtils.isEmpty(admin.getMainCustNm2())) {
      if (admin.getMainCustNm1().length() > 25) {
        String custNm1 = admin.getMainCustNm1().substring(0, 25);
        admin.setMainCustNm1(custNm1);
      }
      if (admin.getMainCustNm2().length() > 24) {
        String custNm2 = admin.getMainCustNm2().substring(0, 24);
        admin.setMainCustNm2(custNm2);
      }
    }

    if ("U".equals(admin.getReqType())) {
      data.setCustSubGrp("");
    }

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {

    String addrTxt = addr.getAddrTxt();
    String addrTxt2 = addr.getAddrTxt2();
    if (addrTxt != null && addrTxt.length() > 24) {
      splitAddress(addr, addr.getAddrTxt(), "", 24, 24);
      if (!StringUtils.isEmpty(addr.getAddrTxt2())) {
        // CREATCMR-6255
        // addr.setAddrTxt2(addr.getAddrTxt2().trim());
        addr.setDivn(addr.getAddrTxt2().trim());
        addr.setAddrTxt2(null);
      }
    }
    // CREATCMR-6696
    if (addrTxt2 != null && addrTxt2.length() > 0) {
      addr.setDivn(addrTxt2.length() > 24 ? addrTxt2.trim().substring(0, 24) : addrTxt2.trim());
      addr.setAddrTxt2(null);
    }

    // CREATCMR-6342
    Data currentData = dataService.getCurrentRecordById(addr.getId().getReqId(), entityManager);
    String scc = getSCCByReqId(entityManager, currentData.getId().getReqId());
    currentData.setCompanyNm(scc);

    if ("END USER".equals(currentData.getCustSubGrp())
        || ("E".equals(currentData.getBpAcctTyp()) && ("BPQS".equals(currentData.getRestrictTo()) || "IRCSO".equals(currentData.getRestrictTo())))) {
      currentData.setAbbrevNm("");
      currentData.setSearchTerm("");
    }

    RequestEntryService service = new RequestEntryService();
    service.updateEntity(currentData, entityManager);
    // CREATCMR-6342

    // AddrPK pk = new AddrPK();
    // pk.setReqId(addr.getId().getReqId());
    // Addr currentAddr = entityManager.find(Addr.class, pk);

  }

  @Override
  public boolean customerNamesOnAddress() {
    return false;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return true;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    List<String> skipOnSummaryUpdateFields = Arrays.asList("ISIC", "CAP", "SitePartyID");
    if (skipOnSummaryUpdateFields.contains(field)) {
      return true;
    }
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    // update data_rdc table
    // CREATCMR-5447
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    List<DataRdc> records = query.getResults(DataRdc.class);
    if (records != null && records.size() > 0) {
      DataRdc rdc = records.get(0);
      rdc.setTaxExempt1(!StringUtils.isEmpty(data.getSpecialTaxCd()) ? data.getSpecialTaxCd().trim() : data.getTaxExemptStatus1());
      rdc.setTaxExempt2(data.getTaxExemptStatus2());
      rdc.setTaxExempt3(data.getTaxExemptStatus3());
      rdc.setAbbrevNm(data.getAbbrevNm());
      entityManager.merge(rdc);
    }
    entityManager.flush();
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
    if (!"C".equals(admin.getReqType()) || "KYN".equalsIgnoreCase(data.getCustSubGrp())) {
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

  @Override
  public boolean setAddrSeqByImport(AddrPK addrPk, EntityManager entityManager, FindCMRResultModel result) {
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

  public static String validateForSCC(EntityManager entityManager, Long reqId) {

    String flag = "N";

    String landCntry = "";
    String stateProv = "";
    String county = "";
    String city1 = "";

    String sql1 = ExternalizedQuery.getSql("US.GET.ADDRESS_BY_REQ_ID");
    PreparedQuery query1 = new PreparedQuery(entityManager, sql1);
    query1.setParameter("REQ_ID", reqId);

    List<Object[]> results1 = query1.getResults();
    if (results1 != null && !results1.isEmpty()) {
      Object[] scc = results1.get(0);
      landCntry = (String) scc[0];
      stateProv = (String) scc[1];
      county = (String) scc[2];
      city1 = (String) scc[3];
    }

    boolean isNumber = StringUtils.isNumeric(county);

    if (!isNumber) {
      county = "0";
    }

    if ("US".equals(landCntry)) {
      String sql2 = ExternalizedQuery.getSql("QUERY.US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY");
      PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
      query2.setParameter("LAND_CNTRY", landCntry);
      query2.setParameter("N_ST", stateProv);
      query2.setParameter("C_CNTY", county);
      query2.setParameter("N_CITY", city1);

      List<Object[]> results2 = query2.getResults();
      if (results2 != null && !results2.isEmpty()) {
        Object[] result = results2.get(0);
        if (!"".equals(result[0])) {
          flag = "Y";
        }
      }
    } else {
      String sql2 = ExternalizedQuery.getSql("QUERY.US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY_NON_US");
      PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
      query2.setParameter("LAND_CNTRY", landCntry);
      // query2.setParameter("N_ST", stateProv);
      // query2.setParameter("C_CNTY", county);
      query2.setParameter("N_CITY", city1);

      List<Object[]> results2 = query2.getResults();
      if (results2 != null && !results2.isEmpty()) {
        Object[] result = results2.get(0);
        if (!"".equals(result[0])) {
          flag = "Y";
        }
      }

    }

    return flag;

  }

  public static boolean isDataUpdated(Data data, DataRdc dataRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    for (Field field : Data.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if at least one of the fields is updated
        if (getDataFieldsForUpdateCheck(cmrIssuingCntry).contains(srcName)) {
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

    return true;
  }

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "AFFILIATE", "COMPANY", "ENTERPRISE", "TAX_CD1", "TAX_CD2", "TAX_CD3", "TAX_EXEMPT_STATUS_1", "TAX_EXEMPT_STATUS_2",
        "TAX_EXEMPT_STATUS_3", "ICC_TAX_CLASS", "ICC_TAX_EXEMPT_STATUS", "ORD_BLK", "US_SICMEN", "EDUC_ALLOW_CD"));
    return fields;
  }

  // CREATCMR-4872
  public static boolean isDataUpdatedTaxIn(Data data, DataRdc dataRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    for (Field field : Data.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if at least one of the fields is updated
        if (getDataFieldsForUpdateTaxInCheck(cmrIssuingCntry).contains(srcName)) {
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
            try {
              field.setAccessible(true);
              Object srcVal1 = field.get(data);
              String srcStringVal1 = (String) srcVal1;

              if ("TAX_EXEMPT_STATUS_1".equals(srcName)) {
                if (StringUtils.isNotBlank(dataRdc.getTaxExempt1()) && StringUtils.isNotBlank(data.getTaxExemptStatus1())) {
                  if (!dataRdc.getTaxExempt1().equals(srcStringVal1)) {
                    return true;
                  }
                } else if (!StringUtils.isNotBlank(dataRdc.getTaxExempt1()) && !StringUtils.isNotBlank(data.getTaxExemptStatus1())) {
                  continue;
                } else {
                  return true;
                }
              } else if ("TAX_EXEMPT_STATUS_2".equals(srcName)) {
                if (StringUtils.isNotBlank(dataRdc.getTaxExempt2()) && StringUtils.isNotBlank(data.getTaxExemptStatus2())) {
                  if (!dataRdc.getTaxExempt2().equals(srcStringVal1)) {
                    return true;
                  }
                } else if (!StringUtils.isNotBlank(dataRdc.getTaxExempt2()) && !StringUtils.isNotBlank(data.getTaxExemptStatus2())) {
                  continue;
                } else {
                  return true;
                }
              } else if ("TAX_EXEMPT_STATUS_3".equals(srcName)) {
                if (StringUtils.isNotBlank(dataRdc.getTaxExempt3()) && StringUtils.isNotBlank(data.getTaxExemptStatus3())) {
                  if (!dataRdc.getTaxExempt3().equals(srcStringVal1)) {
                    return true;
                  }
                } else if (!StringUtils.isNotBlank(dataRdc.getTaxExempt3()) && !StringUtils.isNotBlank(data.getTaxExemptStatus3())) {
                  continue;
                } else {
                  return true;
                }
              }
            } catch (Exception e1) {
              continue;
            }
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

  public static boolean isDataUpdatedTaxOut(Data data, DataRdc dataRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    for (Field field : Data.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if at least one of the fields is updated
        if (getDataFieldsForUpdateTaxOutCheck(cmrIssuingCntry).contains(srcName)) {
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
            try {
              field.setAccessible(true);
              Object srcVal1 = field.get(data);
              String srcStringVal1 = (String) srcVal1;
              if (srcStringVal1 != null || "".equals(srcStringVal1)) {
                return true;
              }
            } catch (Exception e1) {
              continue;
            }
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

  public static List<String> getDataFieldsForUpdateTaxInCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("TAX_CD1", "TAX_CD2", "TAX_CD3", "TAX_EXEMPT_STATUS_1", "TAX_EXEMPT_STATUS_2", "TAX_EXEMPT_STATUS_3", "ICC_TAX_CLASS",
        "ICC_TAX_EXEMPT_STATUS", "OUT_CITY_LIMIT"));
    return fields;
  }

  public static List<String> getDataFieldsForUpdateTaxOutCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "AFFILIATE", "COMPANY", "ENTERPRISE", "ORD_BLK", "US_SICMEN", "EDUC_ALLOW_CD"));
    return fields;
  }
  // CREATCMR-4872

  public boolean isAddrUpdated(Addr addr, AddrRdc addrRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);

    for (Field field : Addr.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if field is part of exemption list or is part of what to check
        // for the handler, if specified
        if (GEOHandler.ADDRESS_FIELDS_SKIP_CHECK.contains(srcName)
            || (handler != null && handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry) != null
                && !handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry).contains(srcName))) {
          continue;
        }

        if ("ID".equals(srcName) || "PCSTATEMANAGER".equals(srcName) || "PCDETACHEDSTATE".equals(srcName)) {
          continue;
        }

        try {
          trgField = AddrRdc.class.getDeclaredField(field.getName());

          field.setAccessible(true);
          trgField.setAccessible(true);

          Object srcVal = field.get(addr);
          Object trgVal = trgField.get(addrRdc);

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

      }
    }
    return false;
  }

  public String getBPDataFromRdc(EntityManager entityManager, String mandt, String kunnr) {
    String bptype = "";
    String sql = ExternalizedQuery.getSql("US.GETBPTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      bptype = result;
    }

    System.out.println("bptype = " + bptype);

    return bptype;
  }

  public String getAufsdCombFromRdc(EntityManager entityManager, String mandt, String cmrNum) {
    String ordBlk = "";
    String sql = ExternalizedQuery.getSql("US.GET.KNA1");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("ZZKV_CUSNO", cmrNum);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      String ordBlkZS01 = "";
      String ordBlkZP01 = "";
      for (Object[] values : results) {
        String ktokd = (String) values[0];
        String aufsd = (String) values[1];
        if ("ZS01,ZI01".contains(ktokd)) {
          if (StringUtils.isNotBlank(aufsd)) {
            if ("88".equals(aufsd)) {
              ordBlkZS01 = "8S";
            } else if ("PG".equals(aufsd)) {
              ordBlkZS01 = "PG";
            } else {
              ordBlkZS01 = "9S";
            }
          }
        } else {
          if (StringUtils.isNotBlank(aufsd)) {
            if ("88".equals(aufsd)) {
              ordBlkZP01 = "8I";
            } else if ("PG".equals(aufsd)) {
              ordBlkZP01 = "PG";
            } else {
              ordBlkZP01 = "9I";
            }
          }
        }
      }
      if ("8S".equals(ordBlkZS01) && "8I".equals(ordBlkZP01)) {
        ordBlk = "8B";
      } else if ("8S".equals(ordBlkZS01)) {
        ordBlk = "8S";
      } else if ("8I".equals(ordBlkZP01)) {
        ordBlk = "8I";
      } else if ("9S".equals(ordBlkZS01) && "9I".equals(ordBlkZP01)) {
        ordBlk = "9B";
      } else if ("9S".equals(ordBlkZS01)) {
        ordBlk = "9S";
      } else if ("9I".equals(ordBlkZP01)) {
        ordBlk = "9I";
      } else if ("PG".equals(ordBlkZS01) && "PG".equals(ordBlkZP01)) {
        ordBlk = "PG";
      }
    }
    return ordBlk;
  }

  public USTaxData getUSTaxDataById(EntityManager entityManager, String mandt, String kunnr) {
    if (StringUtils.isEmpty(mandt) || StringUtils.isEmpty(kunnr)) {
      return null;
    }

    try {
      String sql = ExternalizedQuery.getSql("US.GET.US_TAX_DATA");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("KUNNR", kunnr);
      query.setParameter("MANDT", mandt);
      query.setForReadOnly(true);
      return query.getSingleResult(USTaxData.class);
    } catch (Exception e) {
      LOG.error("An error encountered in RDC retrieve US TaxData using getUSTaxDataById().", e);
    }
    return null;
  }

  public KnvvExt getKnvvExtById(EntityManager entityManager, String mandt, String kunnr) {
    if (StringUtils.isEmpty(mandt) || StringUtils.isEmpty(kunnr)) {
      return null;
    }

    try {
      String sql = ExternalizedQuery.getSql("US.GET.KNVVEXT");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("KUNNR", kunnr);
      query.setParameter("MANDT", mandt);
      query.setForReadOnly(true);
      return query.getSingleResult(KnvvExt.class);
    } catch (Exception e) {
      LOG.error("An error encountered in RDC retrieve KnvvExt.", e);
    }
    return null;
  }

  // CREATCMR-4872
  public static DataRdc getDataRdcRecords(EntityManager entityManager, Data data) {
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    return query.getSingleResult(DataRdc.class);
  }

  public static boolean getUSDataSkipToPCP(EntityManager entityManager, Data data) {
    DataRdc dataRdc = getDataRdcRecords(entityManager, data);
    Addr addr = getAddrSoldToRecords(entityManager, data);
    AddrRdc addrRdc = getAddrSoldToRdcRecords(entityManager, data);

    boolean isSkipToPcp = false;
    boolean isTaxInUpdate = isDataUpdatedTaxIn(data, dataRdc, data.getCmrIssuingCntry());
    boolean isTaxOutUpdate = isDataUpdatedTaxOut(data, dataRdc, data.getCmrIssuingCntry());
    // CREATCMR-5447
    List<String> checkForUpdateAddrTaxOut = Arrays.asList("ADDR_TXT", "ADDR_TXT2", "CITY2", "POST_CD", "DIVN", "DEPT", "PO_BOX", "BLDG", "FLOOR",
        "CUST_PHONE", "CUST_FAX");
    List<String> checkForUpdateAddrTaxIn = Arrays.asList("CITY1", "COUNTY", "STATE_PROV", "LAND_CNTRY");

    boolean isTaxInAddrUpdate = isAddrUpdatedForTaxTeam(addr, addrRdc, checkForUpdateAddrTaxIn);
    boolean isTaxOutAddrUpdate = isAddrUpdatedForTaxTeam(addr, addrRdc, checkForUpdateAddrTaxOut);

    // if (isTaxInUpdate || isTaxInAddrUpdate) {
    // if (!isTaxOutUpdate && !isTaxOutAddrUpdate) {
    // isSkipToPcp = true;
    // }
    // }
    if (isTaxInUpdate) {
      if (!isTaxOutUpdate) {
        isSkipToPcp = true;
      }
    }
    return isSkipToPcp;
  }

  // CREATCMR-5447
  public static boolean getUSDataSkipToPPN(EntityManager entityManager, Data data) {
    DataRdc dataRdc = getDataRdcRecords(entityManager, data);
    boolean isSkipToPpn = isDataUpdatedTaxIn(data, dataRdc, data.getCmrIssuingCntry());
    return isSkipToPpn;
  }

  public static AddrRdc getAddrSoldToRdcRecords(EntityManager entityManager, Data data) {
    String sql = ExternalizedQuery.getSql("DNB.GET_CURR_SOLD_TO_RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    return query.getSingleResult(AddrRdc.class);
  }

  public static Addr getAddrSoldToRecords(EntityManager entityManager, Data data) {
    String sql = ExternalizedQuery.getSql("DNB.GET_CURR_SOLD_TO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    return query.getSingleResult(Addr.class);
  }

  public static boolean isAddrUpdatedForTaxTeam(Addr addr, AddrRdc addrRdc, List<String> checkForUpdateAddrList) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    for (Field field : Addr.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        if (checkForUpdateAddrList.contains(srcName)) {
          try {
            trgField = AddrRdc.class.getDeclaredField(field.getName());

            field.setAccessible(true);
            trgField.setAccessible(true);

            Object srcVal = field.get(addr);
            Object trgVal = trgField.get(addrRdc);

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
            continue;
          }
        }

      }
    }
    return false;
  }

  @Override
  public String getEquivalentAddressType(String addressType, String seqNo) {
    if (addressType.equals("ZS01")) {
      return "ZS01";
    } else if (addressType.equals("ZI01")) {
      return "ZP01";
    } else if (addressType.equals("PG01")) {
      return "ZP01";
    } else {
      return addressType;
    }
  }

  public String getZzkvLicFromRdc(EntityManager entityManager, String mandt, String cmrNum) {
    String zzkvLic = "";
    String sql = ExternalizedQuery.getSql("US.GET.KNA1.ZZKV_LIC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("ZZKV_CUSNO", cmrNum);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {

      for (Object[] values : results) {
        // String ktokd = (String) values[0];
        // String aufsd = (String) values[1];
        zzkvLic = (String) values[1];
      }
    }
    return zzkvLic;
  }

  public static String getProcessingTypeForUS(EntityManager entityManager, String country) {
    String sql = ExternalizedQuery.getSql("AUTO.GET_PROCESSING_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setForReadOnly(true);
    return query.getSingleResult(String.class);
  }

  // CREATCMR-6331
  public static String getUSEntCompToPPN(EntityManager entityManager, Admin admin) {
    String sql = ExternalizedQuery.getSql("QUERY.US_GETMASSUPDGETENTNO");
    String cmtUsEntCompToPpn = "";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", admin.getId().getReqId());
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<MassUpdtData> rs = query.getResults(MassUpdtData.class);

    for (MassUpdtData muData : rs) {
      if (!StringUtils.isEmpty(muData.getCompany()) || !StringUtils.isEmpty(muData.getEnterprise())) {
        if (StringUtils.isEmpty(muData.getCustNm1())) {
          if ("".equals(cmtUsEntCompToPpn)) {
            cmtUsEntCompToPpn = Integer.toString(muData.getId().getSeqNo()).trim();
          } else {
            cmtUsEntCompToPpn = cmtUsEntCompToPpn + ", row " + Integer.toString(muData.getId().getSeqNo()).trim();
          }
        }
      }
    }

    if (!StringUtils.isEmpty(cmtUsEntCompToPpn)) {
      cmtUsEntCompToPpn = "\nPlease check the fields in row " + cmtUsEntCompToPpn + ".";
      cmtUsEntCompToPpn = "If Enterprise# or Company# has been changes, Customer Name must be filled in please." + cmtUsEntCompToPpn;
    }

    return cmtUsEntCompToPpn;
  }

  // CREATCMR-6342
  private String getSCCByReqId(EntityManager entityManager, long req_id) {
    String scc = "";
    String sql = ExternalizedQuery.getSql("US.GET.US_CMR_SCC.GET_SCC_BY_REQ_ID_LAND_CNTRY_ST_CNTY_CITY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", req_id);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      scc = result;
    }
    return scc;
  }
  // CREATCMR-6342

  @Override
  public Boolean compareReshuffledAddress(String dnbAddress, String address, String country) {
    return false;
  }

  public static Boolean compareUSReshuffledAddress(String dnbAddress, String address, String country) {
    return false;
  }

  // CREATCMR-6987
  public static boolean isKynDataSkipDnB(Admin admin, Data data) {
    Boolean isKynDataFlag = false;
    if ("C".equals(admin.getReqType()) && "KYNDRYL INC".equalsIgnoreCase(admin.getMainCustNm1())) {
      if ("KYN".equalsIgnoreCase(data.getCustSubGrp())
          || ("BYMODEL".equalsIgnoreCase(data.getCustSubGrp()) && "KYN".equalsIgnoreCase(data.getRestrictTo()))) {
        isKynDataFlag = true;
      }
    }
    return isKynDataFlag;
  }

  public static boolean isProliferationLandedCntry(Addr addr) {
    boolean isProliferationLandedCntry = false;
    if (PROLIFERATION_CNTRY.contains(addr.getLandCntry())) {
      isProliferationLandedCntry = true;
    }
    return isProliferationLandedCntry;
  }
}