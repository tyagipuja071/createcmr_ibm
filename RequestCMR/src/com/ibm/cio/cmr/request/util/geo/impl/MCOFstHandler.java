/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public class MCOFstHandler extends MCOHandler {

  protected static final Logger LOG = Logger.getLogger(MCOFstHandler.class);

  private static final List<String> FST_COUNTRY_LIST = Arrays.asList(SystemLocation.MAURITIUS, SystemLocation.MALI, SystemLocation.EQUATORIAL_GUINEA,
      SystemLocation.SENEGAL, SystemLocation.IVORY_COAST, SystemLocation.GABON, SystemLocation.DEMOCRATIC_CONGO, SystemLocation.CONGO_BRAZZAVILLE,
      SystemLocation.DJIBOUTI, SystemLocation.GUINEA_CONAKRY, SystemLocation.CAMEROON, SystemLocation.MADAGASCAR, SystemLocation.MAURITANIA,
      SystemLocation.TOGO, SystemLocation.GAMBIA, SystemLocation.CENTRAL_AFRICAN_REPUBLIC, SystemLocation.BENIN, SystemLocation.BURKINA_FASO,
      SystemLocation.SEYCHELLES, SystemLocation.GUINEA_BISSAU, SystemLocation.NIGER, SystemLocation.CHAD);

  protected static final String[] LD_MASS_UPDATE_SHEET_NAMES = { "Mail-to Address", "Bill-to Address", "Sold-to Address", "Ship-to Address",
      "Install-at Address", "Data" };

  private static final String[] MCO2_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "CAP", "CMROwner", "Company", "CustClassCode", "LocalTax2",
      "Enterprise", "SearchTerm", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor",
      "Building", "County", "City2", "INACType", "BPRelationType", "MembLevel", "ModeOfPayment", "CodFlag", "SalRepNameNo" };

  @Override
  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {
    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Mailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
    if (record != null) {
      converted.add(record);
    }
    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "Installing";
    String seqNoFromSOF = null;
    for (FindCMRRecordModel record : records) {
      if (!"ZS01".equals(record.getCmrAddrTypeCode())) {
        return;
      }
      seqNoFromSOF = this.currentImportValues.get(addrKey + "AddressNumber");
      if (!StringUtils.isEmpty(seqNoFromSOF)) {
        LOG.trace("Assigning SOF Sequence " + seqNoFromSOF + " to " + addrKey);
        record.setCmrAddrSeq(seqNoFromSOF);
      }

    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    String embargoCode = (this.currentImportValues.get("EmbargoCode"));
    if (StringUtils.isBlank(embargoCode)) {
      embargoCode = getRdcAufsd(data.getCmrNo(), data.getCmrIssuingCntry());
    }
    if (embargoCode != null && embargoCode.length() < 2 && !"ST".equalsIgnoreCase(embargoCode)) {
      data.setEmbargoCd(embargoCode);
      LOG.trace("EmbargoCode: " + embargoCode);
    } else if ("ST".equalsIgnoreCase(embargoCode)) {
      data.setTaxExemptStatus3(embargoCode);
      LOG.trace(" STC Order Block Code : " + embargoCode);
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      if (legacyObjects != null && legacyObjects.getCustomer() != null) {
        CmrtCust legacyCust = legacyObjects.getCustomer();

        if (legacyCust.getCustType() != null)
          data.setCrosSubTyp(legacyCust.getCustType());

        if (legacyCust.getTaxCd() != null)
          data.setSpecialTaxCd(legacyCust.getTaxCd());

        if (legacyCust.getEnterpriseNo() != null)
          data.setEnterprise(legacyCust.getEnterpriseNo());

      }

      data.setDunsNo(mainRecord.getCmrDuns());
      String zs01sapNo = getKunnrSapr3Kna1(data.getCmrNo(), data.getCmrIssuingCntry());
      data.setIbmDeptCostCenter(getDepartment(zs01sapNo));
      data.setAdminDeptLine(data.getIbmDeptCostCenter());
      String modeOfPayment = legacyObjects.getCustomer().getModeOfPayment();
      if (StringUtils.isNotBlank(modeOfPayment) && ("R".equals(modeOfPayment) || "S".equals(modeOfPayment) || "T".equals(modeOfPayment))) {
        data.setCommercialFinanced(modeOfPayment);
        data.setCreditCd("N");
      } else if (StringUtils.isNotBlank(modeOfPayment) && "5".equals(modeOfPayment)) {
        data.setCreditCd("Y");
        data.setCommercialFinanced("");
      } else {
        data.setCreditCd("");
        data.setCommercialFinanced("");
      }
    }
    if (legacyObjects != null && legacyObjects.getCustomer() != null) {
      CmrtCustExt legacyCustExt = legacyObjects.getCustomerExt();
      if ("700".equals(data.getCmrIssuingCntry()) && legacyCustExt != null && legacyCustExt.getiTaxCode() != null)
        data.setBusnType(legacyCustExt.getiTaxCode());

    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setPpsceid("");
    }
  }

  private String getKunnrSapr3Kna1(String cmrNo, String cntry) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.ZS01.KUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + cntry + "'");

    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUNNR");
    query.addField("ZZKV_CUSNO");

    LOG.debug("Getting existing KUNNR value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      kunnr = record.get("KUNNR") != null ? record.get("KUNNR").toString() : "";
      LOG.debug("***RETURNING KUNNR > " + kunnr);
    }
    return kunnr;
  }

  private String getDepartment(String kunnr) throws Exception {
    String department = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.DEPT.KNA1.BYKUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUNNR");
    query.addField("ZZKV_DEPT");

    LOG.debug("Getting existing ZZKV_DEPT value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      department = record.get("ZZKV_DEPT") != null ? record.get("ZZKV_DEPT").toString() : "";
      LOG.debug("***RETURNING ZZKV_DEPT > " + department + " WHERE KUNNR IS > " + kunnr);
    }
    return department;
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);

    String country = currentRecord.getCmrIssuedBy();
    String processingType = PageManager.getProcessingType(country, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
          && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
        String seq = address.getId().getAddrSeq();
        seq = StringUtils.leftPad(seq, 5, '0');
        address.getId().setAddrSeq(seq);
      }
      address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
      if (!("ZS01".equals(address.getId().getAddrType()) || "ZD01".equals(address.getId().getAddrType()))) {
        address.setCustPhone("");
      }
      if ("ZD01".equals(address.getId().getAddrType())) {
        String phone = getShippingPhoneFromLegacy(currentRecord);
        address.setCustPhone(phone != null ? phone : "");
      }
    }
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZP01", "ZS01", "ZD01", "ZI01", "ZS02");
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZD01", "ZI01");
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setRepTeamMemberNo("DUMMY1");
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    super.doBeforeAddrSave(entityManager, addr, cmrIssuingCntry);

    if (addr != null) {
      if (!("ZS01".equals(addr.getId().getAddrType()) || "ZD01".equals(addr.getId().getAddrType()))) {
        addr.setCustPhone("");
      }

      if (!("ZS01".equals(addr.getId().getAddrType()) || "ZP01".equals(addr.getId().getAddrType()) || "ZS02".equals(addr.getId().getAddrType()))) {
        addr.setPoBox("");
      }
    }
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "ABBREV_NM", "CLIENT_TIER", "CUST_PREF_LANG", "INAC_CD", "ISU_CD",
        "COLLECTION_CD", "SPECIAL_TAX_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "IBM_DEPT_COST_CENTER", "COMMERCIAL_FINANCED", "CREDIT_CD",
        "BUSN_TYP", "ADMIN_DEPT_LN"));
    return fields;
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(
        Arrays.asList("CUST_NM1", "CUST_NM2", "LAND_CNTRY", "CUST_NM4", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "CUST_PHONE", "POST_CD", "PO_BOX"));
    return fields;
  }

  @Override
  public void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, FindCMRRecordModel mainRecord,
      List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), "U");
    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());

    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
        if (prospectCmrChosen && "ZS01".equals(mainRecord.getCmrAddrTypeCode())) {
          LOG.debug("Installing Address prospect CMR No. " + mainRecord.getCmrNum());
          mainRecord.setCmrAddrSeq("00003");
        }
        mapCreateReqAddrLD(mainRecord, converted);
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType())) {
        mapUpdateReqAddrLD(source, converted);
        importMailingFromDB(entityManager, mainRecord, converted);

        if (mainRecord.getCmrNum() != null && mainRecord.getCmrNum().startsWith("99")) {
          addOtherLegacyAddresses(entityManager, mainRecord.getCmrIssuedBy(), converted);
        }

      }
    } else {
      super.handleSOFConvertFrom(entityManager, source, reqEntry, mainRecord, converted, searchModel);
    }
  }

  private void addOtherLegacyAddresses(EntityManager entityManager, String issuingCntry, List<FindCMRRecordModel> converted) {

    Set<Integer> legacySeq = getLegacySequence();
    Set<Integer> rdcSeq = getRdcSequence(converted);

    Set<Integer> diffSeq = new TreeSet<>(legacySeq);
    diffSeq.addAll(rdcSeq);
    diffSeq.removeAll(new TreeSet<>(rdcSeq));

    if (!diffSeq.isEmpty()) {
      List<String> sofUses = null;
      for (int seq : diffSeq) {
        String seqNo = String.format("%05d", seq);
        sofUses = this.legacyObjects.getUsesBySequenceNo(seqNo);
        for (String sofUse : sofUses) {
          String addressType = getAddressTypeByUse(sofUse);
          FindCMRRecordModel otherLegacyAddr = createAddress(entityManager, issuingCntry, addressType, getTargetAddressType(addressType),
              new HashMap<String, FindCMRRecordModel>());
          if (otherLegacyAddr != null) {
            converted.add(otherLegacyAddr);
          }
        }
      }
    }
  }

  private String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "EplMailing";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "Mailing";
    default:
      return "";
    }
  }

  private Set<Integer> getLegacySequence() {
    Set<Integer> legacySeqSet = new TreeSet<>();
    List<CmrtAddr> legacyAddr = legacyObjects.getAddresses();

    for (CmrtAddr addr : legacyAddr) {
      int seq = Integer.parseInt(addr.getId().getAddrNo());
      legacySeqSet.add(seq);
    }

    return legacySeqSet;
  }

  private Set<Integer> getRdcSequence(List<FindCMRRecordModel> converted) {
    Set<Integer> rdcSeqSet = new TreeSet<>();

    for (FindCMRRecordModel rdcRec : converted) {
      int seq = Integer.parseInt(rdcRec.getCmrAddrSeq());
      rdcSeqSet.add(seq);
    }
    return rdcSeqSet;
  }

  private boolean isOldCmrRecord(EntityManager entityManager, FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted) {
    // Old cmrs don't have mailing
    for (FindCMRRecordModel rec : converted) {
      if ("ZS02".equals(rec.getCmrAddrTypeCode())) {
        LOG.debug("FST - New cmr");
        return false;
      }
    }
    LOG.debug("FST - Old cmr");
    return true;
  }

  private void importMailingFromDB(EntityManager entityManager, FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted) {
    boolean isOldCmr = isOldCmrRecord(entityManager, mainRecord, converted);
    if (isOldCmr) {
      if (LegacyDirectUtil.isCountryLegacyDirectEnabled(entityManager, mainRecord.getCmrIssuedBy())) {
        FindCMRRecordModel mailingSOF = createAddress(entityManager, mainRecord.getCmrIssuedBy(), "ZS02", "Mailing",
            new HashMap<String, FindCMRRecordModel>());
        if (mailingSOF != null) {
          converted.add(mailingSOF);
        }
      }
    }
  }

  private void mapCreateReqAddrLD(FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted) {
    // only add zs01 equivalent for create by model
    FindCMRRecordModel record = mainRecord;
    if (!StringUtils.isEmpty(record.getCmrName3())) {
      record.setCmrName4(record.getCmrName3());
    }

    if (!StringUtils.isEmpty(record.getCmrName4())) {
      record.setCmrStreetAddressCont(record.getCmrName4());
    }

    if (!StringUtils.isBlank(record.getCmrPOBox())) {
      record.setCmrPOBox(record.getCmrPOBox());
    }
    if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
      record.setCmrAddrSeq("00001");
    } else {
      record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
    }

    record.setCmrName2Plain(record.getCmrName2Plain());

    if (StringUtils.isEmpty(record.getCmrCustPhone())) {
      record.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
    }
    converted.add(record);
  }

  private void mapUpdateReqAddrLD(FindCMRResultModel source, List<FindCMRRecordModel> converted) throws Exception {
    if (source.getItems() != null) {

      String addrType = null;
      String seqNo = null;
      List<String> sofUses = null;
      FindCMRRecordModel addr = null;

      // map RDc - SOF - CreateCMR by sequence no
      for (FindCMRRecordModel record : source.getItems()) {
        seqNo = record.getCmrAddrSeq();
        if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
          sofUses = this.legacyObjects.getUsesBySequenceNo(seqNo);
          for (String sofUse : sofUses) {
            addrType = getAddressTypeByUse(sofUse);
            if (!StringUtils.isEmpty(addrType)) {
              addr = cloneAddress(record, addrType);
              LOG.trace("Adding address type " + addrType + " for sequence " + seqNo);

              // name3 in rdc = Address Con't on SOF
              addr.setCmrStreetAddressCont(record.getCmrName4());
              addr.setCmrName4(record.getCmrName3());
              addr.setCmrName2Plain(record.getCmrName2Plain());
              addr.setCmrSitePartyID(record.getCmrSitePartyID());

              if (!StringUtils.isBlank(record.getCmrPOBox())) {
                addr.setCmrPOBox(record.getCmrPOBox());
              }

              if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                addr.setCmrAddrSeq("00001");
              }
              converted.add(addr);
            }
          }
        }
      }
    }
  }

  @Override
  protected String getAddressTypeByUse(String addressUse) {
    switch (addressUse) {
    case "1":
      return "ZS02";
    case "2":
      return "ZP01";
    case "3":
      return "ZS01";
    case "4":
      return "ZD01";
    case "5":
      return "ZI01";
    }
    return null;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    if (FST_COUNTRY_LIST.contains(issuingCountry)) {
      return true;
    }
    return false;
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType()) || CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(admin.getReqType())) {
      if (!StringUtils.isEmpty(data.getSalesBusOffCd())) {
        data.setSearchTerm(data.getSalesBusOffCd());
      }
    }

    if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(admin.getReqType())) {
      data.setRepTeamMemberNo("DUMMY1");
    }

    data.setAdminDeptLine(data.getIbmDeptCostCenter());
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !service.equals(oldData.getTaxExempt3(), newData.getTaxExemptStatus3())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "TaxExemptStatus3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxExemptStatus3(), "TaxExemptStatus3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxExempt3(), "TaxExemptStatus3", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCrosSubTyp(), newData.getCrosSubTyp())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Type of Customer", "Type of Customer"));
      update.setNewData(service.getCodeAndDescription(newData.getCrosSubTyp(), "Type of Customer", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCrosSubTyp(), "Type of Customer", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SBO/ Search Term (SORTL):", "SBO/ Search Term (SORTL):"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesBusOffCd(), "SBO/ Search Term (SORTL):", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesBusOffCd(), "SBO/ Search Term (SORTL):", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSpecialTaxCd(), newData.getSpecialTaxCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Tax Code", "Tax Code"));
      update.setNewData(service.getCodeAndDescription(newData.getSpecialTaxCd(), "Tax Code", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSpecialTaxCd(), "Tax Code", cmrCountry));
      results.add(update);
    }

    if ("700".equals(oldData.getCmrIssuingCntry())) {
      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getBusnType(), newData.getBusnType())) {
        update = new UpdatedDataModel();
        update.setDataField(PageManager.getLabel(cmrCountry, "Numero Statistique du Client", "Numero Statistique du Client"));
        update.setNewData(service.getCodeAndDescription(newData.getBusnType(), "Numero Statistique du Client", cmrCountry));
        update.setOldData(service.getCodeAndDescription(oldData.getBusnType(), "Numero Statistique du Client", cmrCountry));
        results.add(update);
      }
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Collection Code", "Collection Code"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "Collection Code", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "Collection Code", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CoF (Commercial Financed)", "CoF (Commercial Financed)"));
      update.setNewData(service.getCodeAndDescription(newData.getCommercialFinanced(), "CoF (Commercial Financed)", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCommercialFinanced(), "CoF (Commercial Financed)", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getAdminDeptLine(), newData.getAdminDeptLine())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Internal Department Number", "Internal Department Number"));
      update.setNewData(service.getCodeAndDescription(newData.getAdminDeptLine(), "Internal Department Number", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getAdminDeptLine(), "Internal Department Number", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevLocn(), newData.getAbbrevLocn())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevLocation", "-"));
      update.setNewData(newData.getAbbrevLocn());
      update.setOldData(oldData.getAbbrevLocn());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCreditCd(), newData.getCreditCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CodFlag", "-"));
      update.setNewData(newData.getCreditCd());
      update.setOldData(oldData.getCreditCd());
      results.add(update);
    }

  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    XSSFCell currCell = null;
    String defaultLanded = LANDED_CNTRY_MAP.get(country);
    for (String name : LD_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      if (sheet != null) {

        boolean isDummyUpdate = false;
        boolean isShippingPhoneUpdate = false;
        boolean islandedFilled = false;
        for (Row row : sheet) {
          TemplateValidation error = new TemplateValidation(name);
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
            String cmrNo = "";
            String seqNo = "";
            String custName1 = ""; // 2
            String nameCont = ""; // 3
            String street = ""; // 4
            String streetCont = ""; // 5
            String collectioncd = ""; // 4
            String sbo = ""; // 5
            String landedcountry = "";// 8
            String embargo = ""; // 9
            String cof = ""; // 10
            String cod = ""; // 11
            String vat = ""; // 12
            String deptNo = ""; // 14
            String city = "";
            String postalcd = "";
            String phoneNo = ""; // 10
            String phoneNoData = ""; // 13
            String stcont = ""; // 5
            String addnameinfo = ""; // 9
            String poBox = "";// 10
            String numero = "";// 15
            String isuCd = ""; // 7
            String clientTier = ""; // 8
            String stcOrdBlk = ""; // 10

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              collectioncd = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              sbo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              embargo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(10);
              stcOrdBlk = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(11);
              cof = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(12);
              cod = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(13);
              vat = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(15);
              deptNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(14);
              phoneNoData = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              clientTier = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              isuCd = validateColValFromCell(currCell);
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                phoneNoData = df.formatCellValue(row.getCell(14));
              }
              currCell = (XSSFCell) row.getCell(16);
              numero = validateColValFromCell(currCell);
            }

            if (!"Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              seqNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              custName1 = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              nameCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              street = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              stcont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              city = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              postalcd = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              landedcountry = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              addnameinfo = validateColValFromCell(currCell);

              if ("Mail-to Address".equalsIgnoreCase(sheet.getSheetName()) || "Bill-to Address".equalsIgnoreCase(sheet.getSheetName())
                  || "Sold-to Address".equalsIgnoreCase(sheet.getSheetName())) {
                currCell = (XSSFCell) row.getCell(10);
                poBox = validateColValFromCell(currCell);
                if (currCell != null) {
                  DataFormatter df = new DataFormatter();
                  poBox = df.formatCellValue(row.getCell(10));
                }
              }
            }

            if ("Ship-to Address".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(10);
              phoneNo = validateColValFromCell(currCell);
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                phoneNo = df.formatCellValue(row.getCell(10));
              }
            }

            if (row.getRowNum() == 2001) {
              continue;
            }

            if (StringUtils.isBlank(custName1) && StringUtils.isBlank(nameCont) && StringUtils.isBlank(street) && StringUtils.isBlank(streetCont)
                && StringUtils.isBlank(city) && StringUtils.isBlank(postalcd) && StringUtils.isBlank(addnameinfo)
                && StringUtils.isBlank(landedcountry) && StringUtils.isBlank(poBox)) {
              isDummyUpdate = true;
            }

            if ("Ship-to Address".equalsIgnoreCase(sheet.getSheetName()) && cmrNo.length() != 0 && seqNo.length() != 0 && phoneNo.length() != 0) {
              isShippingPhoneUpdate = true;
            }

            if (!"Data".equalsIgnoreCase(sheet.getSheetName()) && cmrNo.length() != 0) {
              if (isDummyUpdate && !isShippingPhoneUpdate) {
                LOG.debug("isDummyUpdate...");
              } else if (isDummyUpdate && isShippingPhoneUpdate) {
                LOG.debug("isShippingPhoneUpdate...");
              } else {

                if (StringUtils.isBlank(custName1)) {
                  LOG.trace("Customer Name is required.");
                  error.addError(row.getRowNum(), "Customer Name", "Customer Name is required. ");
                }

                if ("Ship-to Address".equalsIgnoreCase(sheet.getSheetName()) || "Install-at Address".equalsIgnoreCase(sheet.getSheetName())) {
                  if (StringUtils.isBlank(street)) {
                    LOG.trace("Street is required.");
                    error.addError(row.getRowNum(), "Street", "Street is required. ");
                  }
                }

                if (StringUtils.isBlank(city)) {
                  LOG.trace("City is required. ");
                  error.addError(row.getRowNum(), "City", "City is required. ");
                }

                if (StringUtils.isBlank(landedcountry)) {
                  LOG.trace("Landed Country is required. ");
                  error.addError(row.getRowNum(), "Landed Country", "Landed Country is required. ");
                  islandedFilled = false;
                } else {
                  islandedFilled = true;
                }

                if ("Mail-to Address".equalsIgnoreCase(sheet.getSheetName()) || "Bill-to Address".equalsIgnoreCase(sheet.getSheetName())
                    || "Sold-to Address".equalsIgnoreCase(sheet.getSheetName())) {
                  if (StringUtils.isBlank(street) && StringUtils.isBlank(poBox)) {
                    LOG.trace("Please fill-out either Street or PO Box.");
                    error.addError(row.getRowNum(), "Street/POBox", "Please fill-out either Street or PO Box. ");
                  }
                }
              }
            }

            if (islandedFilled) {
              if (!defaultLanded.equals(landedcountry) && !"Data".equalsIgnoreCase(sheet.getSheetName()) && cmrNo.length() != 0) {
                LOG.debug("isCrossborder");
                if (addnameinfo.length() != 0 && stcont.length() != 0 && poBox.length() != 0) {
                  if ("Mail-to Address".equalsIgnoreCase(sheet.getSheetName()) || "Bill-to Address".equalsIgnoreCase(sheet.getSheetName())
                      || "Sold-to Address".equalsIgnoreCase(sheet.getSheetName())) {
                    error.addError(row.getRowNum(), "",
                        "Additional name or address information and Street Cont/POBox cannot be filled at the same time. ");
                  } else {
                    error.addError(row.getRowNum(), "", "Additional name or address information and Street Cont cannot be filled at the same time. ");
                  }
                  LOG.trace("Additional name-info stcont/pobox cannot be filled at the same time.");
                } else if (addnameinfo.length() != 0 && (stcont.length() != 0 || poBox.length() != 0)) {
                  if ("Mail-to Address".equalsIgnoreCase(sheet.getSheetName()) || "Bill-to Address".equalsIgnoreCase(sheet.getSheetName())
                      || "Sold-to Address".equalsIgnoreCase(sheet.getSheetName())) {
                    error.addError(row.getRowNum(), "",
                        "Additional name or address information and Street Cont/POBox cannot be filled at the same time. ");
                  } else {
                    error.addError(row.getRowNum(), "", "Additional name or address information and Street Cont cannot be filled at the same time. ");
                  }
                  LOG.trace("Additional name-info stcont cannot be filled at the same time.");
                } else if (stcont.length() != 0 && poBox.length() != 0 && addnameinfo.length() == 0) {
                  if (stcont.length() + poBox.length() > 23) {
                    LOG.trace("Total computed length of Street Cont and PO Box should not exceed 21 characters.");
                    error.addError(row.getRowNum(), "", "Total computed length of Street Cont and PO Box should not exceed 21 characters. ");
                  }
                }
              } else {
                LOG.debug("isLocal");
                if (stcont.length() != 0 && poBox.length() != 0) {
                  if (stcont.length() + poBox.length() > 23) {
                    LOG.trace("Total computed length of Street Cont and PO Box should not exceed 21 characters.");
                    error.addError(row.getRowNum(), "", "Total computed length of Street Cont and PO Box should not exceed 21 characters. ");
                  }
                }
              }
            }

            if (cod.length() > 0 && cof.length() > 0) {
              LOG.trace("Note that COF and COD flag cannot be filled at same time.");
              error.addError(row.getRowNum(), "COF/COD", "Note that COF and COD flag cannot be filled at same time. ");
            }
            if (city.length() == 0 && postalcd.length() != 0) {
              LOG.trace("Note that city should be filled if postal is filled");
              error.addError(row.getRowNum(), "City/Postal Code", "Note that city should be filled if postal is filled. ");
            }
            if (!StringUtils.isBlank(cmrNo) && !cmrNo.startsWith("99") && !StringUtils.isBlank(deptNo)) {
              LOG.trace("CMR No. should start with 99 if internal department no. is filled.");
              error.addError(row.getRowNum(), "Internal Department No.", "CMR No. should start with 99 if internal department no. is filled. ");
            }
            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isBlank(phoneNoData)) {
                if (!StringUtils.isNumeric(phoneNoData.substring(0, phoneNoData.length()))) {
                  LOG.trace("Phone number should have numeric values only.");
                  error.addError(row.getRowNum(), "", "Phone number should have numeric values only. ");
                }
              }
              if (StringUtils.isNotBlank(stcOrdBlk) && StringUtils.isNotBlank(embargo)) {
                LOG.trace("Please fill either STC Order Block Code or Order Block Code ");
                error.addError((row.getRowNum() + 1), "Order Block Code", "Please fill either STC Order Block Code or Order Block Code ");
              }
            }
            if ("Ship-to Address".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isBlank(phoneNo)) {
                if (!StringUtils.isNumeric(phoneNo.substring(0, phoneNo.length()))) {
                  LOG.trace("Phone number should have numeric values only.");
                  error.addError(row.getRowNum(), "", "Phone number should have numeric values only. ");
                }
              }
            }
            if ("Mail-to Address".equalsIgnoreCase(sheet.getSheetName()) || "Bill-to Address".equalsIgnoreCase(sheet.getSheetName())
                || "Sold-to Address".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isBlank(poBox)) {
                if (!StringUtils.isNumeric(poBox.substring(0, poBox.length()))) {
                  LOG.trace("POBox number should have numeric values only.");
                  error.addError(row.getRowNum(), "", "POBox number should have numeric values only. ");
                }
              }
            }
            if (numero.length() != 0 && numero.length() == 21) {
              boolean isMatch = Pattern.matches("\\d{5}\\s\\d{2}\\s\\d{4}\\s\\d\\s\\d{5}$", numero);
              if (!isMatch) {
                LOG.trace("Invalid format for Numero Statistique du Client.");
                error.addError(row.getRowNum(), "Numero Statistique du Client",
                    "Invalid format for Numero Statistique du Client. Format should be NNNNN NN NNNN N NNNNN. ");
              }
            }
            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isBlank(sbo)) {
                if (!StringUtils.isNumeric(sbo.substring(0, 4))) {
                  LOG.trace("SBO should have numeric values only.");
                  error.addError(row.getRowNum(), "", "SBO should have numeric values only. ");
                }
              }
            }

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isBlank(collectioncd)) {
                if (!StringUtils.isAlphanumeric(collectioncd)) {
                  LOG.trace("Collection code should have alphanumeric values only.");
                  error.addError(row.getRowNum(), "", "Collection code should have alphanumeric values only. ");
                }
              }
            }

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isBlank(deptNo)) {
                if (!deptNo.equals("@@@@@@")) {
                  if (!StringUtils.isNumeric(deptNo.substring(0, 6))) {
                    LOG.trace("Internal Department Number should have numeric values only.");
                    error.addError(row.getRowNum(), "Internal Department No.", "Internal Department Number should have numeric values only. ");
                  }
                }
              }
            }
            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(clientTier))
                  || (StringUtils.isNotBlank(clientTier) && StringUtils.isBlank(isuCd))) {
                LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that both ISU and CTC value needs to be filled..");
                error.addError((row.getRowNum() + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
              } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                if (StringUtils.isBlank(clientTier) || !"Q".contains(clientTier)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                  error.addError(row.getRowNum() + 1, "Client Tier",
                      ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
                if (StringUtils.isBlank(clientTier) || !"Y".contains(clientTier)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
                if (StringUtils.isBlank(clientTier) || !"T".contains(clientTier)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if ((!StringUtils.isBlank(isuCd) && !("34".equals(isuCd) || "32".equals(isuCd) || "36".equals(isuCd)))
                  && !"@".equalsIgnoreCase(clientTier)) {
                LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                error.addError(row.getRowNum() + 1, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
              }
            }
          }
          if (error.hasErrors()) {
            validations.add(error);
          }
        } // end row loop

      }
    }
  }

  private String getShippingPhoneFromLegacy(FindCMRRecordModel address) {
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      if ("Y".equals(cmrtAddr.getIsAddrUseShipping()) && address.getCmrAddrSeq().equals(cmrtAddr.getId().getAddrNo())) {
        return cmrtAddr.getAddrPhone();
      }
    }
    return null;
  }

  @Override
  public boolean checkCopyToAdditionalAddress(EntityManager entityManager, Addr copyAddr, String cmrIssuingCntry) throws Exception {

    if (copyAddr != null && copyAddr.getId() != null) {
      Admin adminRec = LegacyCommonUtil.getAdminByReqId(entityManager, copyAddr.getId().getReqId());
      if (adminRec != null) {
        boolean isCreateReq = CmrConstants.REQ_TYPE_CREATE.equals(adminRec.getReqType());
        if (isCreateReq && copyAddr.getId().getAddrSeq().compareTo("00006") >= 0) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(MCO2_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

}
