package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
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
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.CloningRDCDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Import Converter for CND
 * 
 * @author Garima Narang
 * 
 */
public class CNDHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(CNDHandler.class);

  private static final String[] CND_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "LocalTax2", "SitePartyID", "Division", "POBoxCity", "CustFAX", "Affiliate",
      "INACType" };

  private static final List<String> CND_ISSUING_COUNTRY_VAL = Arrays.asList("619", "621", "627", "647", "791", "640", "759", "839", "843", "859");

  private EntityManager entityManager;

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    if (CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock())) {
      data.setProspectSeqNo(mainRecord.getCmrAddrSeq());
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) && StringUtils.isBlank(mainRecord.getCmrOrderBlock())) {
      data.setOrdBlk("");
    } else {
      data.setOrdBlk(mainRecord.getCmrOrderBlock());
    }

    try {

      // retrieve from knvv zterm data on import
      String modeOfPayment = getRdcModeOfPayment(mainRecord.getCmrSapNumber());

      if (modeOfPayment != null) {
        // Miscellaneous Mode Of Payment Term
        data.setModeOfPayment(modeOfPayment);
      }
    } catch (Exception e) {
      LOG.error("Error occured on setting Credit Code on import.");
      e.printStackTrace();
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    String[] parts = null;
    String name1 = currentRecord.getCmrName1Plain();
    String name2 = currentRecord.getCmrName2Plain();
    parts = splitName(name1, name2, 35, 35);
    address.setCustNm1(parts[0]);
    address.setCustNm2(parts[1]);
    address.setCustNm3(currentRecord.getCmrName3());
    address.setCustNm4(currentRecord.getCmrName4());

    if (!StringUtils.isEmpty(parts[2])) {
      address.setDept(parts[2]);
    }

    if (currentRecord.getCmrOrderBlock() != null && CmrConstants.LEGAL_ORDER_BLOCK.equals(currentRecord.getCmrOrderBlock())) {
      address.setPairedAddrSeq(currentRecord.getCmrAddrSeq());
    }
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
    UpdatedDataModel update = null;
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOrdBlk(), newData.getOrdBlk())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OrderBlock", "-"));
      update.setNewData(newData.getOrdBlk());
      update.setOldData(oldData.getOrdBlk());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(newData.getModeOfPayment());
      update.setOldData(oldData.getModeOfPayment());
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

    data.setSearchTerm(data.getCovId());

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
    return Arrays.asList(CND_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
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
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "DEPT", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX"));
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
    }

    return spid;
  }

  public String getRdcModeOfPayment(String kunnr) throws Exception {

    String modeOfPayment = "";
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");

    if (StringUtils.isEmpty(mandt) || StringUtils.isEmpty(kunnr)) {
      return null;
    }

    String sql = ExternalizedQuery.getSql("GET.IERP.MODE_OF_PAYMENT");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("ZTERM");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing CREDIT_CD value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      modeOfPayment = record.get("ZTERM") != null ? record.get("ZTERM").toString() : "";
    }

    return modeOfPayment;
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

  public static boolean isCNDCountry(String cntry) {
    return CND_ISSUING_COUNTRY_VAL.contains(cntry);
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getCMRNo(EntityManager rdcMgr, String kukla, String mandt, String katr6, String cmrNo, CmrCloningQueue cloningQueue) {
    String cndCMR = "";
    int i = 0;

    while (i < 5) {
      cndCMR = CloningRDCDirectUtil.genNumericNumberSeries(6, kukla);
      LOG.debug("Generated CMR No.:" + cndCMR);

      if (CloningRDCDirectUtil.checkCustNoForDuplicateRecord(rdcMgr, cndCMR, mandt, katr6)) {
        i++;
        LOG.debug("Alredy exist CMR No.: " + cndCMR + "  in rdc. Trying to generate next times:");
        if (i == 5) {
          LOG.debug("Max limit is 5 times to generate CMR No.: " + cndCMR + " Tried times:" + i);
          cndCMR = "";
        }
      } else
        break;
    }

    LOG.debug("generateCNDCmr :: returnung cndCMR = " + cndCMR);
    LOG.debug("generateCNDCmr :: END");
    return cndCMR;
  }

  private boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val2 == null & val1 != null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null & val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }
}
