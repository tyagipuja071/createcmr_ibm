package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.KunnrExt;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.CloningRDCDirectUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Import Converter for DE
 * 
 * @author Garima Narang
 * 
 */
public class DEHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(DEHandler.class);
  private static final List<String> IERP_ISSUING_COUNTRY_VAL = Arrays.asList("724");
  protected static final String[] MT_MASS_UPDATE_SHEET_NAMES = { "Data", "Sold To", "Bill To", "Install-At", "Ship-To" };
  private static final String[] DE_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "LocalTax1", "LocalTax2", "SitePartyID", "Division", "POBoxCity", "CustFAX",
      "City2", "Affiliate", "Company", "INACType", "TransportZone", "Office", "Floor", "BPRelationType", "MembLevel" };
  protected LegacyDirectObjectContainer legacyObjects;

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> recordsFromSearch = source.getItems();
    List<FindCMRRecordModel> filteredRecords = new ArrayList<>();
    FindCMRRecordModel mainRecord = null;
    List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();

    if (recordsFromSearch != null && !recordsFromSearch.isEmpty() && recordsFromSearch.size() > 0) {
      doFilterAddresses(reqEntry, recordsFromSearch, filteredRecords);
      if (!filteredRecords.isEmpty() && filteredRecords.size() > 0 && filteredRecords != null) {
        source.setItems(filteredRecords);
      }
    }

    // CREATCMR-6139 Prospect CMR Conversion - address sequence A
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      FindCMRRecordModel record = source.getItems() != null && !source.getItems().isEmpty() ? source.getItems().get(0) : null;
      if (record != null) {
        if (StringUtils.isNotBlank(reqEntry.getCmrIssuingCntry()) && "724".equals(reqEntry.getCmrIssuingCntry())
            && StringUtils.isNotBlank(record.getCmrNum()) && record.getCmrNum().startsWith("P") && record.getCmrAddrSeq().equals("A")) {
          record.setCmrAddrSeq("1");
          converted.add(record);
          source.setItems(converted);
        }
      }
    }

  }

  @SuppressWarnings("unchecked")
  public static void doFilterAddresses(RequestEntryModel reqEntry, Object mainRecords, Object filteredRecords) {
    if (mainRecords instanceof java.util.List<?> && filteredRecords instanceof java.util.List<?>) {
      // during convertFrom
      if (reqEntry.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_UPDATE)) {
        List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
        List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if ("ZS01".equalsIgnoreCase(tempRec.getCmrAddrTypeCode()) && ("90".equalsIgnoreCase(tempRec.getCmrOrderBlock()))) {
              tempRec.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZS02.toString());
            }
            if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(tempRec.getCmrAddrTypeCode()) && "598".equals(tempRec.getCmrAddrSeq())) {
              tempRec.setCmrAddrTypeCode("ZD02");
            }

            if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(tempRec.getCmrAddrTypeCode()) && "599".equals(tempRec.getCmrAddrSeq())) {
              tempRec.setCmrAddrTypeCode("ZP02");
            }
            if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(tempRec.getCmrAddrTypeCode()) && StringUtils.isNotEmpty(tempRec.getExtWalletId())) {
              tempRec.setCmrAddrTypeCode("PG01");
            }
            recordsToReturn.add(tempRec);
          }
        }
      }

      if (reqEntry.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE)) {
        List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
        List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZS01")) {
              // RETURN ONLY THE SOLD-TO ADDRESS FOR CREATES
              recordsToReturn.add(tempRec);
            }
          }
        }
      }

    }

  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    if (CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock())) {
      data.setProspectSeqNo(mainRecord.getCmrAddrSeq());
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setPpsceid("");
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    boolean doSplitForName = (currentRecord.getCmrName1Plain() != null && currentRecord.getCmrName1Plain().length() > 35)
        || (currentRecord.getCmrName2Plain() != null && currentRecord.getCmrName2Plain().length() > 35);
    if (doSplitForName) {
      String[] parts = null;
      String name1 = currentRecord.getCmrName1Plain();
      String name2 = currentRecord.getCmrName2Plain();
      parts = splitName(name1, name2, 35, 35);
      address.setCustNm1(parts[0]);
      address.setCustNm2(parts[1]);
      if (!StringUtils.isEmpty(parts[2])) {
        address.setDept(parts[2]);
      }
    } else {
      address.setCustNm1(currentRecord.getCmrName1Plain());
      address.setCustNm2(currentRecord.getCmrName2Plain());
    }

    // addrtxt2 issue
    String name4 = currentRecord.getCmrName4();
    address.setBldg(name4);

    boolean doSplit = currentRecord.getCmrStreetAddress() != null && currentRecord.getCmrStreetAddress().length() > 35;
    if (doSplit) {
      splitAddress(address, currentRecord.getCmrStreetAddress(), currentRecord.getCmrStreetAddressCont(), 35, 35);
    } else {
      address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
    }

    // CMR-3171 - do not block seq import for OB records
    // if (currentRecord.getCmrOrderBlock() != null &&
    // CmrConstants.LEGAL_ORDER_BLOCK.equals(currentRecord.getCmrOrderBlock()))
    // {
    address.setPairedAddrSeq(currentRecord.getCmrAddrSeq());
    // }
    KunnrExt addlAddDetail = getKunnrExtDetails(currentRecord.getCmrSapNumber());
    if (addlAddDetail != null) {
      address.setBldg(addlAddDetail.getBuilding() != null ? addlAddDetail.getBuilding() : "");
      address.setDept(addlAddDetail.getDepartment() != null ? addlAddDetail.getDepartment() : "");
    }

    address.setCustNm3(getName3FrmKna1(currentRecord.getCmrSapNumber()));
    address.setCustNm4(getName4FrmKna1(currentRecord.getCmrSapNumber()));

  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    if (!StringUtils.isEmpty(addrType)) {
      if ("ZD02".equals(addrType)) {
        return "598";
      } else if ("ZP02".equals(addrType)) {
        return "599";
      }
    }

    // CREATCMR-6139
    // return super.generateAddrSeq(entityManager, addrType, reqId,
    // cmrIssuingCntry);
    String newAddrSeq = null;
    int addrSeq = 0;
    String maxAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_CEE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "0");

      if (!(Integer.valueOf(maxAddrSeq) >= 0 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }

    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
    // CREATCMR-6139

  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = "";
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  @Override
  public int getName1Length() {
    return 35;
  }

  @Override
  public int getName2Length() {
    return 35;
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {

  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !service.equals(oldData.getOrdBlk(), newData.getOrdBlk())) {
      UpdatedDataModel update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OrderBlock", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getOrdBlk(), "OrderBlock", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOrdBlk(), "OrderBlock", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !service.equals(oldData.getCustClass(), newData.getCustClass())) {
      UpdatedDataModel update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustClass", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCustClass(), "CustClass", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCustClass(), "CustClass", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
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
    if (data.getInacCd() != null) {
      if (data.getInacCd().matches("[a-zA-Z]{1}[a-zA-Z0-9]{3}")) {
        data.setInacType("N");
      } else if (data.getInacCd().matches("[0-9]{1}[a-zA-Z0-9]{3}")) {
        data.setInacType("I");
      } else if (data.getInacCd().equals("")) {
        data.setInacType("");
      }
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      if (data.getCustSubGrp() != null && "INTSO".equals(data.getCustSubGrp()) && data.getCustClass() != null
          && CmrConstants.CUST_CLASS_85.equals(data.getCustClass())) {
        if (data.getIbmDeptCostCenter() == null || "".equals(data.getIbmDeptCostCenter())) {
          data.setIbmDeptCostCenter(CmrConstants.DEFAULT_IBM_DEPT_COST_CENTER);
        }
      } else if (data.getCustSubGrp() != null && !"INTAM".equals(data.getCustSubGrp()) && !"INTIN".equals(data.getCustSubGrp())) {
        data.setIbmDeptCostCenter("");
      }
      if (data.getAbbrevNm() == null || "".equals(data.getAbbrevNm())) {
        if (admin.getMainCustNm1() != null && admin.getMainCustNm1().length() > 30) {
          data.setAbbrevNm(admin.getMainCustNm1().substring(0, 30));
        } else {
          data.setAbbrevNm(admin.getMainCustNm1());
        }
      }
    }

    // set inac type
    String inac = data.getInacCd();
    if (!StringUtils.isEmpty(inac)) {
      // get starting char of the inac code
      String startCd = inac.substring(0, 1);
      LOG.debug(">>> START Char of INAC CODE is " + startCd);

      if (StringUtils.isAlpha(startCd)) {
        // set type as N if alpha
        LOG.debug(">>> START Char of INAC CODE is ALPHA setting type to N");
        data.setInacType("N");
      } else if (StringUtils.isNumeric(startCd)) {
        LOG.debug(">>> START Char of INAC CODE is NUMERIC setting type to I");
        data.setInacType("I");
      } else {
        data.setInacType("");
      }
    }

    if (StringUtils.isEmpty(data.getCmrOwner())) {
      DataRdc rdcData = null;
      rdcData = getOldData(entityManager, String.valueOf(data.getId().getReqId()));

      if (rdcData != null) {
        data.setCmrOwner(rdcData.getCmrOwner());
      }
    }
    // if (!StringUtils.isEmpty(data.getCovId())) {
    // LOG.debug("*** Auto setting for Germany the Search Term Value as the
    // coverage ID");
    // data.setSearchTerm(data.getCovId());
    // }

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public boolean customerNamesOnAddress() {
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return false;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(DE_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    // QUERY RDC
    String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);

    for (Addr addr : addresses) {
      String sapNo = addr.getSapNo();

      try {
        String spid = "";

        if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
          spid = getRDcIerpSitePartyId(sapNo);
          addr.setIerpSitePrtyId(spid);
        } else if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          addr.setIerpSitePrtyId(spid);
        }

        entityManager.merge(addr);
        entityManager.flush();
      } catch (Exception e) {
        LOG.error("Error occured on setting SPID after import.");
        e.printStackTrace();
      }

    }

  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "DEPT", "FLOOR", "BLDG", "OFFICE", "STATE_PROV", "CITY1", "POST_CD",
        "LAND_CNTRY", "PO_BOX", "ADDR_TXT", "CUST_PHONE"));
    return fields;
  }

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    // CMR-3171 - add ORB_BLK here
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "ORD_BLK", "ENTERPRISE"));
    return fields;
  }

  @Override
  public List<String> getDataFieldsForUpdate(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "ORD_BLK"));
    return fields;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  private String getRDcIerpSitePartyId(String kunnr) throws Exception {
    String spid = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.IERP.BRAN5");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BRAN5");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing BRAN5 value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      spid = record.get("BRAN5") != null ? record.get("BRAN5").toString() : "";
      LOG.debug("***RETURNING BRAN5 > " + spid + " WHERE KUNNR IS > " + kunnr);
    }

    return spid;
  }

  @Override
  protected String[] splitName(String name1, String name2, int length1, int length2) {
    String name = name1 + " " + (name2 != null ? name2 : "");
    String[] parts = name.split("[ ]");

    String namePart1 = "";
    String namePart2 = "";
    String namePart3 = "";

    boolean part1Ok = false;
    for (String part : parts) {
      if ((namePart1 + " " + part).trim().length() > length1 || part1Ok) {
        part1Ok = true;
        namePart2 += " " + part;
      } else {
        namePart1 += " " + part;
      }
    }
    namePart1 = namePart1.trim();

    if (namePart1.length() == 0) {
      namePart1 = name.substring(0, length1);
      namePart2 = name.substring(length1);
    }
    if (namePart1.length() > length1) {
      namePart1 = namePart1.substring(0, length1);
    }
    namePart2 = namePart2.trim();
    if (namePart2.length() > 35) {

      String[] tmpAr = namePart2.split(" ");
      int idxStart = 0;
      String ret = "";
      for (int i = 0; i < tmpAr.length; i++) {
        ret = ret + " " + tmpAr[i];
        idxStart = i + 1;
        String base = ret + " " + tmpAr[i + 1];
        if (base.length() > 35) {
          break;
        }
      }

      String temp1 = ret;
      namePart2 = temp1.trim();

      for (int i = idxStart; i < tmpAr.length; i++) {
        namePart3 = namePart3 + " " + tmpAr[i];
      } // namePart3 = temp2;

      namePart3 = namePart3.trim();

      LOG.debug("namePart2 >>> " + namePart2);
      System.out.println("idxStart >>> " + idxStart);
      System.out.println("namePart3 >>>" + namePart3);

    }

    return new String[] { namePart1, namePart2, namePart3 };

  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  public static boolean isIERPCountry(String cntry) {
    return IERP_ISSUING_COUNTRY_VAL.contains(cntry);
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

    return false;
  }

  public static boolean isAddrUpdated(Addr addr, AddrRdc addrRdc, String cmrIssuingCntry) {
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

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##INACType", "inacType");
    map.put("##ISU", "isuCd");
    map.put("##SearchTerm", "searchTerm");
    map.put("##Building", "bldg");
    map.put("##CMROwner", "cmrOwner");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustLang", "custPrefLang");
    map.put("##LocalTax2", "taxCd2");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##BPRelationType", "bpRelType");
    map.put("##CAP", "capInd");
    map.put("##LocalTax1", "taxCd1");
    map.put("##RequestReason", "reqReason");
    map.put("##POBox", "poBox");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##CustPhone", "custPhone");
    map.put("##Floor", "floor");
    map.put("##VATExempt", "vatExempt");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##City1", "city1");
    map.put("##StateProv", "stateProv");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##VAT", "vat");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Office", "office");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##Affiliate", "affiliate");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##ClientTier", "clientTier");
    map.put("##IERPSitePrtyId", "ierpSitePrtyId");
    map.put("##SAPNumber", "sapNo");
    map.put("##CustClass", "custClass");
    map.put("##Department", "dept");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##CustomerName1", "custNm1");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName2", "custNm2");
    map.put("##Enterprise", "enterprise");
    map.put("##IbmDeptCostCenter", "ibmDeptCostCenter");
    map.put("##PostalCode", "postCd");
    map.put("##County", "county");
    map.put("##TransportZone", "transportZone");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##PrivacyIndc", "privIndc");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    if ("724".equals(issuingCountry)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {

    XSSFCell currCell = null;
    for (String name : MT_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      if (sheet != null) {
        for (Row row : sheet) {
          TemplateValidation error = new TemplateValidation(name);
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {

            String cmrNo = ""; // 0

            // Address Sheet
            String seqNo = ""; // 1
            String custName1 = ""; // 2
            String name2 = ""; // 3
            String name3 = ""; // 4
            String name4 = ""; // 5
            String deptExt = ""; // 6
            String BuildingExt = ""; // 7
            String street = ""; // 8
            String poBox = ""; // 9
            String city = "";// 10
            String postalCode = ""; // 11
            String landCntry = ""; // 12
            String phone = "";// 13

            // Data Sheet
            String cmrNodata = "";
            String isic = ""; // 3
            String vat = ""; // 4
            String sbo = ""; // 5
            String classificationCd = ""; // 10
            String inac = ""; // 8
            String ordBlk = ""; // 11
            String isuCd = ""; // 6
            String clientTier = ""; // 8

            if (row.getRowNum() == 2001) {
              continue;
            }

            String rowNumber = "Row" + (row.getRowNum() + 1) + ": ";

            if (!"Data".equalsIgnoreCase(sheet.getSheetName())) {
              // iterate all the rows and check each column value
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(1);
              seqNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(2);
              custName1 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(3);
              name2 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(4);
              name3 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(5);
              name4 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              deptExt = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              BuildingExt = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(8);
              street = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(9);
              poBox = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              city = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(11);
              postalCode = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(12);
              landCntry = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(13);
              phone = validateColValFromCell(currCell);

              if (!StringUtils.isBlank(cmrNo) && !StringUtils.isBlank(seqNo)) {
                if (StringUtils.isBlank(custName1)) {
                  LOG.trace("Customer Name is mandatory");
                  error.addError((row.getRowNum() + 1), "Customer Name", "Customer Name is mandatory.");
                }

                if (StringUtils.isBlank(street)) {
                  LOG.trace("Street is mandatory");
                  error.addError((row.getRowNum() + 1), "Street", "Street is mandatory.");
                }

                if (StringUtils.isBlank(city)) {
                  LOG.trace("City is mandatory");
                  error.addError((row.getRowNum() + 1), "City", "City is mandatory.");
                }

                if (StringUtils.isBlank(landCntry)) {
                  LOG.trace("Landed Country is mandatory");
                  error.addError((row.getRowNum() + 1), "Landed Country", "Landed Country is mandatory.");
                }

                if (StringUtils.isBlank(postalCode)) {
                  LOG.trace("Postal code is mandatory.");
                  error.addError((row.getRowNum() + 1), "Postal Code", "Postal code is mandatory.");
                }
              }

              if ((!StringUtils.isBlank(cmrNo) && StringUtils.isBlank(seqNo) && !"Data".equalsIgnoreCase(sheet.getSheetName()))
                  || (StringUtils.isBlank(cmrNo) && !StringUtils.isBlank(seqNo) && !"Data".equalsIgnoreCase(sheet.getSheetName()))) {
                LOG.trace("Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Address Sequence No.",
                    "Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
                // validations.add(error);
              }

            } else if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNodata = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(2);
              isic = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(3);
              vat = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(4);
              sbo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              inac = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(9);
              classificationCd = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              ordBlk = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              isuCd = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(8);
              clientTier = validateColValFromCell(currCell);

              if (!StringUtils.isBlank(isic) && !StringUtils.isBlank(classificationCd)
                  && ((!"9500".equals(isic) && "60".equals(classificationCd)) || ("9500".equals(isic) && !"60".equals(classificationCd)))) {
                LOG.trace(
                    "Note that ISIC value 9500 can be entered only for CMR with Classification code 60. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Classification Code",
                    "Note that ISIC value 9500 can be entered only for CMR with Classification code 60. Please fix and upload the template again.");
                // validations.add(error);
              }

              if (!StringUtils.isBlank(inac) && inac.length() == 4 && !StringUtils.isNumeric(inac) && !"@@@@".equals(inac)
                  && !inac.matches("^[a-zA-Z][a-zA-Z][0-9][0-9]$") && !inac.matches("^[a-zA-Z][0-9][0-9][0-9]$")) {
                LOG.trace("INAC should have all 4 digits or 2 letters and 2 digits or 1 letter and 3 digits in order.");
                error.addError((row.getRowNum() + 1), "INAC/NAC",
                    "INAC should have all 4 digits or 2 letters and 2 digits or 1 letter and 3 digits in order.");
              }

              if (!StringUtils.isBlank(sbo) && !StringUtils.isAlphanumeric(sbo)) {
                LOG.trace("Enter valid values for SBO/Search Term.");
                error.addError((row.getRowNum() + 1), "SBO/Search Term", "Enter valid values for SBO/Search Term");
              }

              if (!StringUtils.isBlank(ordBlk) && !("88".equals(ordBlk) || "94".equals(ordBlk) || "@".equals(ordBlk))) {
                LOG.trace("Note that value of Order block can only be 88 or 94 or @ or blank. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Order block",
                    "Note that value of Order block can only be 88 or 94 or @ or blank. Please fix and upload the template again.");
                // validations.add(error);
              }

              List<String> isuBlankCtc = Arrays.asList("32", "34", "36");
              if (StringUtils.isBlank(isuCd) && StringUtils.isNotBlank(clientTier) && !"@QYT".contains(clientTier)) {
                LOG.trace(
                    "The row " + (row.getRowNum() + 1) + ":Note that Client Tier only accept @,Q,Y or T. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Client Tier",
                    ":Note that Client Tier only accept @,Q,Y or T. Please fix and upload the template again.<br>");
              } else if (StringUtils.isNotBlank(isuCd) && StringUtils.isNotBlank(clientTier)) {
                if (!isuBlankCtc.contains(isuCd) && !clientTier.equalsIgnoreCase("@")) {
                  LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                  error.addError((row.getRowNum() + 1), "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + "\n");
                } else if (isuBlankCtc.contains(isuCd)) {
                  if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                    if (StringUtils.isBlank(clientTier) || !"Q".equals(clientTier)) {
                      LOG.trace("The row " + (row.getRowNum() + 1)
                          + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                      error.addError((row.getRowNum() + 1), "Client Tier",
                          ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                    }
                  } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
                    if (StringUtils.isBlank(clientTier) || !"Y".equals(clientTier)) {
                      LOG.trace("The row " + (row.getRowNum() + 1)
                          + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                      error.addError((row.getRowNum() + 1), "Client Tier",
                          ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                    }
                  } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
                    if (StringUtils.isBlank(clientTier) || !"T".equals(clientTier)) {
                      LOG.trace("The row " + (row.getRowNum() + 1)
                          + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                      error.addError((row.getRowNum() + 1), "Client Tier",
                          ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                    }
                  } else if (!"@QYT".contains(clientTier)) {
                    LOG.trace("The row " + (row.getRowNum() + 1)
                        + ":Note that Client Tier only accept @,Q,Y or T. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Client Tier",
                        ":Note that Client Tier only accept @,Q,Y or T. Please fix and upload the template again.<br>");
                  }
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
  }

  @Override
  public String getCMRNo(EntityManager rdcMgr, String kukla, String mandt, String katr6, String cmrNo, CmrCloningQueue cloningQueue) {
    // auto generate and store to zzkvCusNo
    String zzkvCusNo = "";
    LOG.debug("Generating Cmr no... ");
    int i = 0;
    while (i < 5) {
      CloningRDCDirectUtil rs = new CloningRDCDirectUtil(5);
      String newSpid1 = CloningRDCDirectUtil.genSingleRandomCharExcludeP();
      String newSpid2 = rs.nextString();
      zzkvCusNo = newSpid1.concat(newSpid2);
      LOG.debug("Generated CMR No.:" + zzkvCusNo);

      if (CloningRDCDirectUtil.checkCustNoForDuplicateRecord(rdcMgr, zzkvCusNo, mandt, katr6)) {
        i++;
        LOG.debug("Alredy exist CMR No.: " + zzkvCusNo + "  in rdc. Trying to generate next times:");
        if (i == 5) {
          zzkvCusNo = "";
          LOG.debug("Max limit is 5 times to generate CMR No.: " + zzkvCusNo + " Tried times:" + i);
        }
      } else
        break;

    }
    return zzkvCusNo;
  }

  /* Story : 1834659 - Import from KUNNR_EXT table */
  private KunnrExt getKunnrExtDetails(String kunnr) throws Exception {
    KunnrExt ke = new KunnrExt();
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.BY_KUNNR_MANDT_SWISS");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BUILDING");
    query.addField("FLOOR");
    query.addField("DEPARTMENT");

    LOG.debug("Getting existing KUNNNR_EXT details from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);

      ke.setBuilding(record.get("BUILDING") != null ? record.get("BUILDING").toString() : "");
      ke.setFloor(record.get("FLOOR") != null ? record.get("FLOOR").toString() : "");
      ke.setDepartment(record.get("DEPARTMENT") != null ? record.get("DEPARTMENT").toString() : "");

      LOG.debug("***RETURNING BUILDING > " + ke.getBuilding());
      LOG.debug("***RETURNING DEPARTMENT > " + ke.getDepartment());
    }
    return ke;
  }

  private String getName3FrmKna1(String kunnr) throws Exception {
    String name3 = "";
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.NAME3.KNA1.BYKUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("MANDT");
    query.addField("KUNNR");
    query.addField("NAME3");

    LOG.debug("Getting existing NAME3  from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      name3 = record.get("NAME3") != null ? record.get("NAME3").toString() : "";
      LOG.debug("***RETURNING NAME3 > " + name3);
    }
    if (name3.length() > 30) {
      return name3.substring(0, 30);
    } else {
      return name3;
    }
  }

  private String getName4FrmKna1(String kunnr) throws Exception {
    String name4 = "";
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.NAME4.KNA1.BYKUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("MANDT");
    query.addField("KUNNR");
    query.addField("NAME4");

    LOG.debug("Getting existing NAME4  from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      name4 = record.get("NAME4") != null ? record.get("NAME4").toString() : "";
      LOG.debug("***RETURNING NAME4 > " + name4);
    }
    return name4;
  }

  private DataRdc getOldData(EntityManager entityManager, String reqId) {
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
}
