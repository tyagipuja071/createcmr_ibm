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
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.KunnrExt;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.CloningRDCDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.ValidatorClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.validator.PostalCodeValidateRequest;
import com.ibm.cmr.services.client.validator.ValidationResult;
import com.ibm.cmr.services.client.validator.VatValidateRequest;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for France
 * 
 * 
 */
public class FranceHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(FranceHandler.class);
  private static final List<String> IERP_ISSUING_COUNTRY_VAL = Arrays.asList("706");

  private static final String[] CH_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "LocalTax2", "SitePartyID", "Division", "POBoxCity", "City2", "Affiliate",
      "Company", "INACType", "POBoxPostalCode", "TransportZone", "CurrencyCode", "MembLevel", "BPRelationType", "CustLangCd", "CustLang",
      "SearchTerm", "CAP" };

  public static final String FR_MASSCHANGE_TEMPLATE_ID = "France";

  public static final String DUMMY_SIRET_ADDRESS = "ZSIR";

  private static final List<String> ENABLE_MASSCHANGE_AUTO_TEMPLATE = Arrays.asList(SystemLocation.FRANCE);
  public static final String[] HRDWRE_MSTR_FLAG_ADDRS = { "ZI01", "ZS01", "ZS02" };

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> recordsFromSearch = source.getItems();
    List<FindCMRRecordModel> filteredRecords = new ArrayList<>();

    if (recordsFromSearch != null && !recordsFromSearch.isEmpty() && recordsFromSearch.size() > 0) {
      doFilterAddresses(reqEntry, recordsFromSearch, filteredRecords);
      if (filteredRecords != null && !filteredRecords.isEmpty() && filteredRecords.size() > 0) {
        Collections.sort(filteredRecords);
        source.setItems(filteredRecords);
      }
    }
  }

  // Story : 1824918 - Secondary Sold to address (order block is 90 for update
  // req)

  @SuppressWarnings("unchecked")
  public static void doFilterAddresses(RequestEntryModel reqEntry, Object mainRecords, Object filteredRecords) {
    if (mainRecords instanceof java.util.List<?> && filteredRecords instanceof java.util.List<?>) {
      // during convertFrom
      if (reqEntry.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_UPDATE)) {
        List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
        List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
        List<String> sapNoListSoldTo = new ArrayList<String>();
        String zs01kunnr = "";

        // loop to gather more than one sold tos in sapNoListSoldTo
        for (Object tempRecObj1 : recordsToCheck) {
          FindCMRRecordModel tempRec1 = (FindCMRRecordModel) tempRecObj1;
          if ("ZS01".equalsIgnoreCase(tempRec1.getCmrAddrTypeCode()) && ("88".equalsIgnoreCase(tempRec1.getCmrOrderBlock()))) {
            sapNoListSoldTo.add(tempRec1.getCmrSapNumber());
          }
        }
        if (!sapNoListSoldTo.isEmpty() && sapNoListSoldTo.size() > 0) {
          zs01kunnr = Collections.min(sapNoListSoldTo);
        }

        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if ("ZS01".equalsIgnoreCase(tempRec.getCmrAddrTypeCode()) && ("90".equalsIgnoreCase(tempRec.getCmrOrderBlock()))) {
              tempRec.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZS02.toString());
            }
            if ("ZS01".equalsIgnoreCase(tempRec.getCmrAddrTypeCode()) && ("88".equalsIgnoreCase(tempRec.getCmrOrderBlock()))
                && !zs01kunnr.equalsIgnoreCase(tempRec.getCmrSapNumber())) {
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
        List<String> sapNoListSoldTo = new ArrayList<String>();
        String zs01kunnr = "";
        // loop to gather more than one sold tos in sapNoListSoldTo
        for (Object tempRecObj1 : recordsToCheck) {
          FindCMRRecordModel tempRec1 = (FindCMRRecordModel) tempRecObj1;
          if ("ZS01".equalsIgnoreCase(tempRec1.getCmrAddrTypeCode()) && ("88".equalsIgnoreCase(tempRec1.getCmrOrderBlock()))) {
            sapNoListSoldTo.add(tempRec1.getCmrSapNumber());
          }
        }
        if (!sapNoListSoldTo.isEmpty() && sapNoListSoldTo.size() > 0) {
          zs01kunnr = Collections.min(sapNoListSoldTo);
        }
        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if ("ZS01".equalsIgnoreCase(tempRec.getCmrAddrTypeCode()) && ("90".equalsIgnoreCase(tempRec.getCmrOrderBlock()))) {
              tempRec.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZS02.toString());
            }
            if ("ZS01".equalsIgnoreCase(tempRec.getCmrAddrTypeCode()) && "".equalsIgnoreCase(tempRec.getCmrOrderBlock())) {
              // RETURN ONLY THE SOLD-TO(with min kunnr) ADDRESS FOR CREATES
              recordsToReturn.add(tempRec);
            } else if ("ZS01".equalsIgnoreCase(tempRec.getCmrAddrTypeCode()) && "88".equalsIgnoreCase(tempRec.getCmrOrderBlock())
                && zs01kunnr.equalsIgnoreCase(tempRec.getCmrSapNumber())) {
              recordsToReturn.add(tempRec);
            }
          }
        }
      }
    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {

    String zs01sapNo = getKunnrSapr3Kna1ForFR(data.getCmrNo(), mainRecord.getCmrOrderBlock());
    data.setMemLvl(getMembershipLevel(zs01sapNo));
    if (CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock())) {
      data.setProspectSeqNo(mainRecord.getCmrAddrSeq());
    }
    if (mainRecord.getCmrNum().startsWith("P") && !"88".equalsIgnoreCase(data.getOrdBlk())) {
      data.setOrdBlk("");
      data.setCmrNo("");
    }
    // changes made as part of defect CMR - 3242
    try {
      if ("88".equals(mainRecord.getCmrOrderBlock()) || "".equals(mainRecord.getCmrOrderBlock())) {
        data.setCurrencyCd(geCurrencyCode(zs01sapNo));
        data.setTaxCd2(getTaxCodeForFR(zs01sapNo));
        data.setAdminDeptLine(getDepartment(zs01sapNo));
        data.setIbmDeptCostCenter(getInternalDepartment((data.getCmrNo())));
      }
    } catch (Exception e) {
      LOG.error("Error occured on setting Currency Code/ tax code value during import.");
      e.printStackTrace();
    }
    EntityManager em = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("AT.GET.ZS01.DATLT");
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("COUNTRY", SystemLocation.FRANCE);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("CMR_NO", data.getCmrNo());
    String datlt = query.getSingleResult(String.class);
    data.setAbbrevLocn(datlt);
    LOG.trace("Abbrev Loc: " + data.getAbbrevLocn());
    // CMR-221
    String search_term = data.getSearchTerm();
    data.setSalesBusOffCd(search_term);
    LOG.trace("SORTL: " + data.getSalesBusOffCd());

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setPpsceid("");
    }

  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {

  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    if (currentRecord != null) {
      String spid = "";
      ArrayList<String> addrDetailsList = new ArrayList<String>();
      if (!StringUtils.isEmpty(currentRecord.getCmrSapNumber())) {
        address.setCustLangCd(getCustLangCd(currentRecord.getCmrSapNumber()));
        address.setHwInstlMstrFlg(getHardwareMasterFlag(currentRecord.getCmrSapNumber()));
      }
      if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType())) {
        address.getId().setAddrSeq(currentRecord.getCmrAddrSeq());
        spid = getRDcIerpSitePartyId(currentRecord.getCmrSapNumber());
        address.setIerpSitePrtyId(spid);

      } else {
        // String addrSeq = address.getId().getAddrSeq();
        // addrSeq = StringUtils.leftPad(addrSeq, 5, '0');
        address.getId().setAddrSeq("1");
        address.setIerpSitePrtyId(spid);
      }
      KunnrExt addlAddDetail = getKunnrExtDetails(currentRecord.getCmrSapNumber());
      if (addlAddDetail != null) {
        address.setBldg(addlAddDetail.getBuilding() != null ? addlAddDetail.getBuilding() : "");
        address.setFloor(addlAddDetail.getFloor() != null ? addlAddDetail.getFloor() : "");
        address.setDept(addlAddDetail.getDepartment() != null ? addlAddDetail.getDepartment() : "");

        if (!StringUtils.isEmpty(address.getDept())) {
          addrDetailsList.add(address.getDept());
        }
        if (!StringUtils.isEmpty(address.getBldg())) {
          addrDetailsList.add(address.getBldg());
        }
        if (!StringUtils.isEmpty(address.getFloor())) {
          addrDetailsList.add(address.getFloor());
        }

      }
      String name3 = getName3FrmKna1(currentRecord.getCmrSapNumber());
      String name4 = getName4FrmKna1(currentRecord.getCmrSapNumber());
      address.setCustNm4(name4);
      if ("U".equalsIgnoreCase(admin.getReqType())) {
        if (StringUtils.isEmpty(name3) && !addrDetailsList.isEmpty()) {
          String joinedCustNm3 = StringUtils.join(addrDetailsList, ", ");
          address.setCustNm3((joinedCustNm3.length() > 30 ? joinedCustNm3.substring(0, 30) : joinedCustNm3));
        } else {
          address.setCustNm3(name3);
        }
      } else {
        if (!addrDetailsList.isEmpty()) {
          String joinedCustNm3 = StringUtils.join(addrDetailsList, ", ");
          address.setCustNm3((joinedCustNm3.length() > 30 ? joinedCustNm3.substring(0, 30) : joinedCustNm3));
        } else {
          address.setCustNm3(name3);
        }
      }
      address.setCustNm1(currentRecord.getCmrName1Plain());
      address.setCustNm2(currentRecord.getCmrName2Plain());
      address.setAddrTxt(currentRecord.getCmrStreetAddress());
      if ("D".equals(address.getImportInd())) {
        address.setAddrTxt(currentRecord.getCmrStreet());
      }
      // if custLangCd is blank for create
      if ("CH".equals(address.getLandCntry()) || "LI".equals(address.getLandCntry())) {
        if (StringUtils.isBlank(address.getCustLangCd()) && StringUtils.isNotBlank(address.getPostCd())
            && StringUtils.isNumeric(address.getPostCd())) {
          int postCd = Integer.parseInt(address.getPostCd());
          if ((postCd >= 3000 && postCd <= 6499) || (postCd >= 6999 && postCd <= 9999)) {
            address.setCustLangCd("D");
          } else if (postCd >= 6500 && postCd <= 6999) {
            address.setCustLangCd("I");
          } else if (postCd >= 0000 && postCd <= 3000) {
            address.setCustLangCd("F");
          }
        }
      } else if (StringUtils.isBlank(address.getCustLangCd())) {
        address.setCustLangCd("E");
      }
    }
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";
    if (!StringUtils.isEmpty(addrType)) {
      int addrSeq = 0;
      if ("ZD02".equals(addrType)) {
        return "598";
      } else if ("ZP02".equals(addrType)) {
        return "599";
      } else if ("ZS01".equals(addrType)) {
        addrSeq = 1;
      } else if ("ZP01".equals(addrType)) {
        addrSeq = 2;
      } else if ("ZI01".equals(addrType)) {
        addrSeq = 3;
      } else if ("ZD01".equals(addrType)) {
        addrSeq = 4;
      }

      String reqType = getReqType(entityManager, reqId);
      String sql = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_CREATECMR");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);

      List<Object[]> resultsCMR = query.getResults();
      int maxSeq = 0;
      if (resultsCMR != null && resultsCMR.size() > 0) {
        boolean seqExistCMR = false;
        List<String> seqListCMR = new ArrayList<String>();
        // Get create cmr seq list
        for (int i = 0; i < resultsCMR.size(); i++) {
          String item = String.valueOf(resultsCMR.get(i));
          if (!StringUtils.isEmpty(item)) {
            seqListCMR.add(item);
          }
        }
        // Check if seq is already exist in create cmr
        seqExistCMR = seqListCMR.contains(Integer.toString(addrSeq));
        // Get Max seq from create cmr
        maxSeq = Integer.parseInt(seqListCMR.get(0));
        for (int i = 0; i < seqListCMR.size(); i++) {
          if (maxSeq < Integer.parseInt(seqListCMR.get(i))) {
            maxSeq = Integer.parseInt(seqListCMR.get(i));
          }
        }
        if (seqExistCMR) {
          if (maxSeq < 5) {
            addrSeq = 5;
          } else {
            addrSeq = maxSeq + 1;
          }
        }
      }
      if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
        String cmrNo = getCMRNo(entityManager, reqId);
        if (!StringUtils.isEmpty(cmrNo)) {
          String sqlRDC = ExternalizedQuery.getSql("FR.ADDRESS.GETMADDRSEQ_RDC");
          PreparedQuery queryRDC = new PreparedQuery(entityManager, sqlRDC);
          queryRDC.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          queryRDC.setParameter("ZZKV_CUSNO", cmrNo);
          List<Object[]> resultsRDC = queryRDC.getResults();
          List<String> seqListRDC = new ArrayList<String>();
          for (int i = 0; i < resultsRDC.size(); i++) {
            String item = String.valueOf(resultsRDC.get(i));
            if (!StringUtils.isEmpty(item)) {
              seqListRDC.add(item);
            }
          }
          if (addrSeq < 5 && seqListRDC.contains(Integer.toString(addrSeq))) {
            if (maxSeq < 5) {
              addrSeq = 5;
            } else {
              addrSeq = maxSeq + 1;
            }
          }
          while (seqListRDC.contains(Integer.toString(addrSeq))) {
            addrSeq++;
          }
        }
      }

      // Old logic:if seq range is not 0-99999, set seq to 1
      if (!(addrSeq >= 1 && addrSeq <= 99999)) {
        addrSeq = 1;
      }
      newAddrSeq = Integer.toString(addrSeq);
    }
    return newAddrSeq;
  }

  public String getCMRNo(EntityManager entityManager, long reqId) {
    String cmrNo = "";
    String sql = ExternalizedQuery.getSql("DATA.GETCMRNO.FR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      cmrNo = results.get(0);
    }
    return cmrNo;
  }

  public String getReqType(EntityManager entityManager, long reqId) {
    String reqType = "";
    String sql = ExternalizedQuery.getSql("ADMIN.GETREQTYPE.FR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      reqType = results.get(0);
    }
    return reqType;
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
    data.setCountryUse("706");
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      admin.setDelInd(null);
    }
  }

  /* Story : 1813127 */
  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOrdBlk(), newData.getOrdBlk())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(newData.getOrdBlk());
      update.setOldData(oldData.getOrdBlk());
      results.add(update);
    }
    // commented as part of defect CMR - 3242
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getCurrencyCd(), newData.getCurrencyCd())) {
    // update = new UpdatedDataModel();
    // update.setDataField(PageManager.getLabel(cmrCountry, "CurrencyCode",
    // "-"));
    // update.setNewData(newData.getCurrencyCd());
    // update.setOldData(oldData.getCurrencyCd());
    // results.add(update);
    // }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustClass", "-"));
      update.setNewData(newData.getCustClass());
      update.setOldData(oldData.getCustClass());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd2(), newData.getTaxCd2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Tax Code", "Tax Code"));
      update.setNewData(newData.getTaxCd2());
      update.setOldData(oldData.getTaxCd2());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevLocn(), newData.getAbbrevLocn())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Abbreviated Location", "Abbreviated Location"));
      update.setNewData(newData.getAbbrevLocn());
      update.setOldData(oldData.getAbbrevLocn());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getAdminDeptLine(), newData.getIbmDeptCostCenter())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Internal Department Number", "Internal Department Number"));
      update.setNewData(newData.getIbmDeptCostCenter());
      update.setOldData(oldData.getAdminDeptLine());
      results.add(update);
    }
    // CMR-221 For FR use SalesBusOff as SearchTerm
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesBusOff", "SalesBusOff"));
      update.setNewData(newData.getSalesBusOffCd());
      update.setOldData(oldData.getSalesBusOffCd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())) {
      for (UpdatedDataModel item : results) {
        if (item.getDataField().equals("Customer Class")) {
          results.remove(item);
        }
      }
    }

    /*
     * if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
     * !equals(oldData.getCustClass(), newData.getCustClass())) { update = new
     * UpdatedDataModel(); update.setDataField(PageManager.getLabel(cmrCountry,
     * "LocalTax1", "-")); update.setNewData(newData.getTaxCd1());
     * update.setOldData(oldData.getTaxCd1()); results.add(update); }
     */
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
    data.setCountryUse("706");

    String CMRDataRdc = getCmrFromDatardc(entityManager, data.getId().getReqId());
    if (!StringUtils.isBlank(CMRDataRdc) && CMRDataRdc.contains("P")) {
      admin.setProspLegalInd("Y");
    }

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    AdminPK adminPK = new AdminPK();
    ArrayList<String> addrDetailsList = new ArrayList<String>();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);
    String sapNo = addr.getSapNo();
    if (!StringUtils.isEmpty(sapNo) && "U".equals(admin.getReqType())) {
      if (addr.getSapNo().length() > 1) {
        addr.setIerpSitePrtyId("S".concat(addr.getSapNo().substring(1)));
      }
    }

    if (!Arrays.asList(HRDWRE_MSTR_FLAG_ADDRS).contains(addr.getId().getAddrType()) && "Y".equalsIgnoreCase(addr.getHwInstlMstrFlg())) {
      addr.setHwInstlMstrFlg("");
    }

    if (!StringUtils.isEmpty(addr.getDept())) {
      addrDetailsList.add(addr.getDept());
    }
    if (!StringUtils.isEmpty(addr.getBldg())) {
      addrDetailsList.add(addr.getBldg());
    }
    if (!StringUtils.isEmpty(addr.getFloor())) {
      addrDetailsList.add(addr.getFloor());
    }

    if (!addrDetailsList.isEmpty() && "C".equals(admin.getReqType())) {
      addr.setCustNm3(StringUtils.join(addrDetailsList, ", "));
    }
    if (addrDetailsList.isEmpty() && "C".equals(admin.getReqType())) {
      // addr.setCustNm3("");
    }

    if ("C".equals(admin.getReqType()) && "ZS01".equals(addr.getId().getAddrType())) {
      DataPK dataPK = new DataPK();
      dataPK.setReqId(addr.getId().getReqId());
      Data data = entityManager.find(Data.class, dataPK);
      setSortlBasedOnLanded(entityManager, addr, data);
    }
  }

  private void setSortlBasedOnLanded(EntityManager entityManager, Addr addr, Data data) {
    String currentLanded = addr.getLandCntry();

    if (shouldSetSBO(currentLanded, data.getCustSubGrp())) {
      String origLandedCntry = getOriginalLandedCountry(entityManager, data.getId().getReqId());

      if (StringUtils.isNotBlank(currentLanded) && !currentLanded.equals(origLandedCntry)) {
        if (currentLanded.equals("TF") || currentLanded.equals("RE")) {
          data.setSalesBusOffCd("ID1ID1");
        } else if (currentLanded.equals("MQ")) {
          data.setSalesBusOffCd("YF1YF1");
        } else if (currentLanded.equals("GP")) {
          data.setSalesBusOffCd("YD1YD1");
        } else if (currentLanded.equals("GF") || currentLanded.equals("PM")) {
          data.setSalesBusOffCd("XF1XF1");
        } else if (currentLanded.equals("YT")) {
          data.setSalesBusOffCd("XD1XD1");
        } else if (currentLanded.equals("NC") || currentLanded.equals("VU") || currentLanded.equals("WF")) {
          data.setSalesBusOffCd("GD1GD1");
        } else if (currentLanded.equals("PF")) {
          data.setSalesBusOffCd("DD1DD1");
        } else if (currentLanded.equals("AD") || currentLanded.equals("MC")) {
          data.setSalesBusOffCd("NNNNNN");
        } else if (currentLanded.equals("DZ")) {
          data.setSalesBusOffCd("711711");
        }
        entityManager.merge(data);
        entityManager.flush();
      }
    }
  }

  private String getOriginalLandedCountry(EntityManager entityManager, long reqId) {
    String origLandedCntry = "";
    String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.LANDCNTRY.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      origLandedCntry = results.get(0);
    }
    return origLandedCntry;
  }

  private boolean shouldSetSBO(String currentLanded, String scenario) {
    List<String> excludedScenarios = Arrays.asList("INTER", "BUSPR", "XBUSP", "CBTER");
    List<String> crossCoverageCountries = Arrays.asList("TF", "RE", "MQ", "GP", "GF", "PM", "YT", "NC", "VU", "WF", "PF", "DZ");
    if (!"FR".equals(currentLanded) && !excludedScenarios.contains(scenario) && crossCoverageCountries.contains(currentLanded)) {
      return true;
    }
    return false;
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
    // return false;
    return Arrays.asList(CH_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  /* Story : 1813127 */

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    String seqNo = addr.getId().getAddrSeq();
    String addrType = addr.getId().getAddrType();
    UpdatedNameAddrModel update = new UpdatedNameAddrModel();
    if (!equals(addr.getCustFax(), addr.getCustFaxOld())) {
      update.setAddrTypeCode(addrType);
      update.setAddrSeq(seqNo);
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setAddrSeq(seqNo);
      update.setDataField(PageManager.getLabel(cmrCountry, "CustFAX", "-"));
      update.setNewData(addr.getCustFax());
      update.setOldData(addr.getCustFax());
      results.add(update);
    }

    if (Arrays.asList(HRDWRE_MSTR_FLAG_ADDRS).contains(addr.getId().getAddrType())
        && !equals(addr.getHwInstlMstrFlg(), addr.getHwInstlMstrFlgOld())) {
      update = new UpdatedNameAddrModel();
      update.setAddrTypeCode(addrType);
      update.setAddrSeq(seqNo);
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "HwInstlMasterFlag", "-"));
      update.setNewData(addr.getHwInstlMstrFlg());
      update.setOldData(addr.getHwInstlMstrFlgOld());
      results.add(update);
    }
  }

  /* Story : 1813127 */

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

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {

    String sql1 = ExternalizedQuery.getSql("BATCH.GET_ADDR_ENTITY_CREATE_REQ");
    PreparedQuery query1 = new PreparedQuery(entityManager, sql1);
    query1.setParameter("REQ_ID", admin.getId().getReqId());
    query1.setParameter("ADDR_TYPE", "ZS01");
    Addr soldToAddr = query1.getSingleResult(Addr.class);
    String landCntry = soldToAddr != null ? soldToAddr.getLandCntry() : "";

    if ("CH".equalsIgnoreCase(landCntry) && "C".equalsIgnoreCase(admin.getReqType())) {
      data.setCurrencyCd("CHF");
    }

  }

  private String getKunnrSapr3Kna1(String cmrNo, String ordBlk) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNA1.KUNNR_U");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":AUFSD", "'" + ordBlk + "'");

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

  private String geCurrencyCode(String kunnr) throws Exception {
    String currCode = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNVV.WAERS");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("WAERS");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing WAERS value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      currCode = record.get("WAERS") != null ? record.get("WAERS").toString() : "";
      LOG.debug("***RETURNING WAERS > " + currCode + " WHERE KUNNR IS > " + kunnr);
    }
    return currCode;
  }

  private String getMembershipLevel(String kunnr) throws Exception {
    String memLevel = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.BP_MBR_LVL_TYPE");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BP_MBR_LVL_TYPE");
    query.addField("KUNNR");

    LOG.debug("Getting existing BP_MBR_LVL_TYPE value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      memLevel = record.get("BP_MBR_LVL_TYPE") != null ? record.get("BP_MBR_LVL_TYPE").toString() : "";
      LOG.debug("***RETURNING BP_MBR_LVL_TYPE > " + memLevel + " WHERE KUNNR IS > " + kunnr);
    }
    return memLevel;
  }

  /* Import from KUNNR_EXT table */
  private KunnrExt getKunnrExtDetails(String kunnr) throws Exception {
    KunnrExt ke = new KunnrExt();
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.BY_KUNNR_MANDT_FR");
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
      LOG.debug("***RETURNING FLOOR > " + ke.getFloor());
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

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "DEPT", "FLOOR", "BLDG", "OFFICE", "STATE_PROV", "CITY1", "POST_CD",
        "LAND_CNTRY", "PO_BOX", "ADDR_TXT", "CUST_PHONE", "CUST_LANG_CD", "HW_INSTL_MSTR_FLG"));
    return fields;
  }

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(
        Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD", "SUB_INDUSTRY_CD",
            "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD", "DUNS_NO"));
    return fields;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  private String getCustLangCd(String kunnr) throws Exception {
    String custLangCd = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.IERP.SPRAS");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("SPRAS");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing SPRAS value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      custLangCd = record.get("SPRAS") != null ? record.get("SPRAS").toString() : "";
      LOG.debug("***RETURNING SPRAS > " + custLangCd + " WHERE KUNNR IS > " + kunnr);
    }
    return custLangCd;
  }

  private String getHardwareMasterFlag(String kunnr) throws Exception {
    String hwFlag = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.HW_INSTL_MSTR_FLG");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("HW_INSTL_MSTR_FLG");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing HW_INSTL_MSTR_FLG value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      hwFlag = record.get("HW_INSTL_MSTR_FLG") != null ? record.get("HW_INSTL_MSTR_FLG").toString() : "";
      LOG.debug("***RETURNING HW_INSTL_MSTR_FLG > " + hwFlag + " WHERE KUNNR IS > " + kunnr);
    }
    return hwFlag;
  }

  /* defect : 1853577 */
  private String getTaxCode(String kunnr) throws Exception {
    String taxcode = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.TAXCODE.TAXKD");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("TAXKD");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing TAXKD value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      taxcode = record.get("TAXKD") != null ? record.get("TAXKD").toString() : "";
      LOG.debug("***RETURNING TAXKD > " + taxcode + " WHERE KUNNR IS > " + kunnr);
    }
    return taxcode;
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

  public static boolean isAutoMassChangeTemplateEnabled(String cntry) {
    return ENABLE_MASSCHANGE_AUTO_TEMPLATE.contains(cntry);
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##SearchTerm", "searchTerm");
    map.put("##Building", "bldg");
    map.put("##CMROwner", "cmrOwner");
    map.put("##PPSCEID", "ppsceid");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
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
    map.put("##InternalDept", "ibmDeptCostCenter");
    map.put("##CustLangCd", "custLangCd");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##VAT", "vat");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CountrySubRegion", "countryUse");
    map.put("##ClientTier", "clientTier");
    map.put("##IERPSitePrtyId", "ierpSitePrtyId");
    map.put("##SAPNumber", "sapNo");
    map.put("##Department", "dept");
    map.put("##CustClass", "custClass");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##HwInstlMasterFlag", "hwInstlMstrFlg");
    map.put("##CustFAX", "custFax");
    map.put("##EmbargoCode", "ordBlk");
    map.put("##CustomerName1", "custNm1");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName2", "custNm2");
    map.put("##CustomerName3", "custNm3");
    map.put("##CustomerName4", "custNm4");
    map.put("##CurrencyCode", "currencyCd");
    map.put("##PostalCode", "postCd");
    map.put("##TransportZone", "transportZone");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return true;
  }

  public static void validateFRMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    String[] sheetNames = { "Data", "Sold To", "Bill To", "Ship To", "Install At" };
    XSSFCell currCell = null;
    for (String name : sheetNames) {
      XSSFSheet sheet = book.getSheet(name);
      LOG.debug("validating for sheet " + name);
      if (sheet != null) {
        for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
          Row row = sheet.getRow(rowIndex);
          if (row == null) {
            return;
          }

          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
            DataFormatter df = new DataFormatter();
            String cmrNo = ""; // 0
            String seqNo = "";// 1
            String postCd = "";// 7
            String countryAddr = ""; // BillTo:10, other:9
            String legalName = ""; // 2
            String street = "";// 6
            String city = ""; // 8, billTo:9
            String poBox = ""; // billTo :8
            String phone = ""; // 10, BillTo:11

            currCell = (XSSFCell) row.getCell(0);
            cmrNo = validateColValFromCell(currCell);

            currCell = (XSSFCell) row.getCell(1);
            seqNo = validateColValFromCell(currCell);

            currCell = (XSSFCell) row.getCell(7);
            postCd = validateColValFromCell(currCell);

            if (row.getRowNum() == 2001) {
              continue;
            }

            if ("Data".equals(name)) {
              String isuCd = ""; // 5
              String ctc = ""; // 6
              currCell = (XSSFCell) row.getCell(5);
              isuCd = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              ctc = validateColValFromCell(currCell);

              if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(ctc)) || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isuCd))) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("The row " + rowIndex + ":Note that both ISU and CTC value needs to be filled..");
                error.addError(rowIndex, "Data Tab", ":Please fill both ISU and CTC value.<br>");
                validations.add(error);
              } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"Q".contains(ctc)) {
                  TemplateValidation error = new TemplateValidation(name);
                  LOG.trace("The row " + rowIndex
                      + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                  error.addError(rowIndex, "Client Tier",
                      ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                  validations.add(error);
                }
              } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"Y".contains(ctc)) {
                  TemplateValidation error = new TemplateValidation(name);
                  LOG.trace("The row " + rowIndex
                      + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                  error.addError(rowIndex, "Client Tier",
                      ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                  validations.add(error);
                }
              } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
                if (StringUtils.isBlank(ctc) || !"T".contains(ctc)) {
                  TemplateValidation error = new TemplateValidation(name);
                  LOG.trace("The row " + rowIndex
                      + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                  error.addError(rowIndex, "Client Tier",
                      ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                  validations.add(error);
                }
              } else if ((!StringUtils.isBlank(isuCd) && !("34".equals(isuCd) || "32".equals(isuCd) || "36".equals(isuCd)))
                  && !"@".equalsIgnoreCase(ctc)) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                error.addError(rowIndex, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
                validations.add(error);
              }

              // String vat = "";// 12
              // currCell = (XSSFCell) row.getCell(12);
              // vat = validateColValFromCell(currCell);
              // String vatTxt = df.formatCellValue(currCell);

              // if (!StringUtils.isBlank(vat) &&
              // !vatTxt.substring(2).matches("\\d+.\\d*")) {
              // TemplateValidation error = new TemplateValidation(name);
              // LOG.trace("The row " + (row.getRowNum() + 1) + " Note that VAT
              // should be numeric. Please fix and upload the template again.");
              // error.addError((row.getRowNum() + 1), "VAT",
              // "The row " + (row.getRowNum() + 1) + ":Note that VAT should be
              // numeric. Please fix and upload the template again.<br>");
              // validations.add(error);
              // }

              String isic = "";// 3
              currCell = (XSSFCell) row.getCell(3);
              isic = validateColValFromCell(currCell);
              String classificationCd = "";// 11
              currCell = (XSSFCell) row.getCell(11);
              classificationCd = validateColValFromCell(currCell);
              String inac = "";// 4
              currCell = (XSSFCell) row.getCell(4);
              inac = validateColValFromCell(currCell);

              String siret = "";// 10

              currCell = (XSSFCell) row.getCell(10);
              String siretTxt = df.formatCellValue(currCell);
              siret = validateColValFromCell(currCell);
              if (!StringUtils.isBlank(siret) && !siretTxt.matches("^[0-9]*$")) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("The row " + (row.getRowNum() + 1) + " Note that SIRET should be numeric. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "SIRET",
                    "The row " + (row.getRowNum() + 1) + ":Note that SIRET should be numeric. Please fix and upload the template again.<br>");
                validations.add(error);
              }
              // try{
              // if (!StringUtils.isBlank(vat)) {
              // if (!validateVAT(country, vat)) {
              // LOG.trace("The row " + (row.getRowNum() + 1) + ":VAT is not
              // valid
              // for
              // the Country.");
              // error.addError((row.getRowNum() + 1), "VAT.", "The row " +
              // (row.getRowNum() + 1) + ":VAT is not valid for the
              // Country.<br>");
              // validations.add(error);
              // }
              // }
              // } catch(Exception e){
              // LOG.error("Error occured on connecting VAT validation
              // service.");
              // e.printStackTrace();
              // }

              // if (!StringUtils.isBlank(isic) &&
              // !StringUtils.isBlank(classificationCd)
              // && ((!"9500".equals(isic) && "60".equals(classificationCd)) ||
              // ("9500".equals(isic) && !"60".equals(classificationCd)))) {
              // LOG.trace(
              // "Note that ISIC value 9500 can be entered only for CMR with
              // Classification code 60. Please fix and upload the template
              // again.");
              // error.addError((row.getRowNum() + 1), "Classification Code",
              // "Note that ISIC value 9500 can be entered only for CMR with
              // Classification code 60. Please fix and upload the template
              // again.");
              // }

              // if (!StringUtils.isBlank(inac) && inac.length() == 4 &&
              // !StringUtils.isNumeric(inac) && !"@@@@".equals(inac)
              // && !inac.matches("^[a-zA-Z][a-zA-Z][0-9][0-9]$")) {
              // LOG.trace("INAC should have all 4 digits or 2 letters and 2
              // digits in order.");
              // error.addError((row.getRowNum() + 1), "INAC/NAC", "INAC should
              // have all 4 digits or 2 letters and 2 digits in order.");
              // }
            }

            if (StringUtils.isEmpty(cmrNo)) {
              TemplateValidation error = new TemplateValidation(name);
              LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that CMR No. is mandatory. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "CMR No.",
                  "The row " + (row.getRowNum() + 1) + ":Note that CMR No. is mandatory. Please fix and upload the template again.<br>");
              validations.add(error);
            }
            if (isDivCMR(cmrNo)) {
              TemplateValidation error = new TemplateValidation(name);
              LOG.trace("The row " + (row.getRowNum() + 1) + ":Note the CMR number is not existed or a divestiture CMR records.");
              error.addError((row.getRowNum() + 1), "CMR No.",
                  "The row " + (row.getRowNum() + 1) + ":Note the CMR number is not existed or a divestiture CMR records.<br>");
              validations.add(error);

            }
            if (!"Data".equals(name)) {
              currCell = (XSSFCell) row.getCell(2);
              legalName = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              street = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              postCd = validateColValFromCell(currCell);

              int loopFlag = 9;
              String phoneTxt = "";
              if ("Bill To".equals(name)) {
                loopFlag = 10;
                currCell = (XSSFCell) row.getCell(9);
                city = validateColValFromCell(currCell);

                currCell = (XSSFCell) row.getCell(11);
                phone = validateColValFromCell(currCell);
                phoneTxt = df.formatCellValue(currCell);

                currCell = (XSSFCell) row.getCell(8);
                poBox = validateColValFromCell(currCell);
              } else {
                currCell = (XSSFCell) row.getCell(8);
                city = validateColValFromCell(currCell);

                currCell = (XSSFCell) row.getCell(10);
                phone = validateColValFromCell(currCell);
                phoneTxt = df.formatCellValue(currCell);
              }

              if (!StringUtils.isBlank(phone) && !phoneTxt.matches("^[0-9]*$")) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("The row " + (row.getRowNum() + 1) + " Note that Phone should be numeric. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Phone",
                    "The row " + (row.getRowNum() + 1) + ":Note that Phone should be numeric. Please fix and upload the template again.<br>");
                validations.add(error);
              }

              boolean dummyUpd = true;
              for (int i = 2; i < loopFlag; i++) {
                XSSFCell cell = (XSSFCell) row.getCell(i);
                String addrField = validateColValFromCell(cell);
                if (StringUtils.isNotBlank(addrField)) {
                  dummyUpd = false;
                  break;
                }
              }
              if (dummyUpd) {
                continue;
              }

              if (StringUtils.isEmpty(legalName)) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("Customer legal name is mandatory field. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Customer legal name",
                    "The row " + (row.getRowNum() + 1) + ":Customer legal name is mandatory field. Please fix and upload the template again.<br>");
                validations.add(error);
              }

              if (!"Bill To".equals(name)) {
                if (StringUtils.isEmpty(street)) {
                  TemplateValidation error = new TemplateValidation(name);
                  LOG.trace("Street is mandatory field. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Street",
                      "The row " + (row.getRowNum() + 1) + ":Street is mandatory field. Please fix and upload the template again.<br>");
                  validations.add(error);
                }
              } else {
                if (StringUtils.isEmpty(street) && StringUtils.isEmpty(poBox)) {
                  TemplateValidation error = new TemplateValidation(name);
                  LOG.trace("Street/Po Box is required to be filled one of them at least. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Street/Po Box", "The row " + (row.getRowNum() + 1)
                      + ":Street/ PoBox is required to be filled one of them at least. Please fix and upload the template again.<br>");
                  validations.add(error);
                }
              }

              if (StringUtils.isEmpty(city)) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("City is mandatory field. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "City",
                    "The row " + (row.getRowNum() + 1) + ":City is mandatory field. Please fix and upload the template again.<br>");
                validations.add(error);
              }

              if (StringUtils.isEmpty(postCd)) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("Postal Code is mandatory field. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Postal Code",
                    "The row " + (row.getRowNum() + 1) + ":Postal Code is mandatory field. Please fix and upload the template again.<br>");
                validations.add(error);
              }

              if ("Bill To".equals(name)) {
                currCell = (XSSFCell) row.getCell(10);
              } else {
                currCell = (XSSFCell) row.getCell(9);
              }
              countryAddr = validateColValFromCell(currCell);
              if (StringUtils.isEmpty(countryAddr)) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("Landed Country is mandatory field. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Country (Landed)",
                    "The row " + (row.getRowNum() + 1) + ":Landed Country is mandatory field. Please fix and upload the template again.<br>");
                validations.add(error);
              }

              if (!StringUtils.isEmpty(postCd)) {
                if (StringUtils.isEmpty(countryAddr)) {
                  TemplateValidation error = new TemplateValidation(name);
                  LOG.trace("Please input landed Country when postal code is filled. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Landed Country", "The row " + (row.getRowNum() + 1)
                      + ":Please input landed Country when postal code is filled. Please fix and upload the template again.<br>");
                  validations.add(error);
                } else {
                  try {
                    ValidationResult validation = checkPostalCode(countryAddr.substring(0, 2), postCd);
                    if (!validation.isSuccess()) {
                      TemplateValidation error = new TemplateValidation(name);
                      LOG.trace(validation.getErrorMessage());
                      error.addError((row.getRowNum() + 1), "Postal code.",
                          "The row " + (row.getRowNum() + 1) + ":" + validation.getErrorMessage() + "<br>");
                      validations.add(error);
                    }
                  } catch (Exception e) {
                    LOG.error("Error occured on connecting postal code validation service.");
                    e.printStackTrace();
                  }
                }
              }

              if (!StringUtils.isBlank(cmrNo) && StringUtils.isBlank(seqNo)) {
                TemplateValidation error = new TemplateValidation(name);
                LOG.trace("Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Address Sequence No.", "The row " + (row.getRowNum() + 1)
                    + ":Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.<br>");
                validations.add(error);
              }
            }
          }
        }
      }
    }
  }

  private static boolean isDivCMR(String cmrNo) {
    boolean isDivestiture = true;
    String mandt = SystemConfiguration.getValue("MANDT");
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("FR.GET.ZS01KATR10");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("KATR6", SystemLocation.FRANCE);
    query.setParameter("MANDT", mandt);
    query.setParameter("CMR", cmrNo.length() > 6 ? cmrNo.substring(0, 6) : cmrNo);

    Kna1 zs01 = query.getSingleResult(Kna1.class);
    if (zs01 != null) {
      if (StringUtils.isBlank(zs01.getKatr10())) {
        isDivestiture = false;
      }
    }
    return isDivestiture;
  }

  private static ValidationResult checkPostalCode(String landedCountry, String postalCode) throws Exception {
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");

    PostalCodeValidateRequest zipRequest = new PostalCodeValidateRequest();
    zipRequest.setMandt(mandt);
    zipRequest.setPostalCode(postalCode);
    zipRequest.setSysLoc("706");
    zipRequest.setCountry(landedCountry);

    LOG.debug("Validating Postal Code " + postalCode + " for landedCountry " + landedCountry + " (mandt: " + mandt + " sysloc: 706" + ")");

    ValidatorClient client = CmrServicesFactory.getInstance().createClient(baseUrl, ValidatorClient.class);
    try {
      ValidationResult validation = client.executeAndWrap(ValidatorClient.POSTAL_CODE_APP_ID, zipRequest, ValidationResult.class);
      return validation;
    } catch (Exception e) {
      LOG.error("Error in postal code validation", e);
      return null;
    }
  }

  /**
   * Connects to VAT validation service and validates VAT
   *
   * @param country
   * @param vat
   * @return
   * @throws Exception
   */
  private static boolean validateVAT(String country, String vat) throws Exception {
    LOG.debug("Validating VAT " + vat + " for " + country);
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    ValidatorClient client = CmrServicesFactory.getInstance().createClient(baseUrl, ValidatorClient.class);
    VatValidateRequest vatRequest = new VatValidateRequest();
    vatRequest.setCountry(country);
    vatRequest.setVat(vat);

    try {
      ValidationResult validation = client.executeAndWrap(ValidatorClient.VAT_APP_ID, vatRequest, ValidationResult.class);
      return validation.isSuccess();
    } catch (Exception e) {
      LOG.error("Error in VAT validation", e);
      return false;
    }
  }

  private String getKunnrSapr3Kna1ForFR(String cmrNo, String ordBlk) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNA1.KUNNR_U_FR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":AUFSD", "'" + ordBlk + "'");

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

  private String getTaxCodeForFR(String kunnr) throws Exception {
    String taxcode = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.TAXCODE.TAXKD_FR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("TAXKD");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing TAXKD value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      taxcode = record.get("TAXKD") != null ? record.get("TAXKD").toString() : "";
      LOG.debug("***RETURNING TAXKD > " + taxcode + " WHERE KUNNR IS > " + kunnr);
    }
    return taxcode;
  }

  private String getInternalDepartment(String cmrNo) throws Exception {
    String department = "";
    List<String> results = new ArrayList<String>();

    EntityManager entityManager = JpaManager.getEntityManager();
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.DEPT.KNA1.BYCMR");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + "706" + "'");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      department = results.get(0);
    }
    return department;
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

  public static String getCmrFromDatardc(EntityManager rdcMgr, long req_id) {
    String cmrdatardc = "";
    String sql = ExternalizedQuery.getSql("FR.GET_CMR_DATARDC");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("REQ_ID", req_id);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      cmrdatardc = result;
    }
    LOG.debug("cmrdatardc" + cmrdatardc);
    return cmrdatardc;
  }

  @Override
  public String getCMRNo(EntityManager rdcMgr, String kukla, String mandt, String katr6, String cmrNo, CmrCloningQueue cloningQueue) {
    LOG.debug("generateCNDCmr :: START");
    String cndCMR = "";
    boolean internal = false;

    if ("11".equals(cloningQueue.getLastUpdtBy()) && cmrNo.startsWith("99")) {
      LOG.debug("Skip setting of CMR No for Internal for CMR : " + cmrNo);
    } else if (Arrays.asList("81", "85").contains(cloningQueue.getLastUpdtBy()) && !cmrNo.startsWith("99")) {
      internal = true;
    } else if (cmrNo.startsWith("99"))
      internal = true;

    int i = 0;

    if (internal) {
      while (i < 5) {
        String ran1 = "";
        ran1 = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        // cndCMR = "99" + RDCRandomString.genNumericNumberSeries(4, kukla);
        cndCMR = "99" + ran1;

        LOG.debug("Generated France Internal CMR No.:" + cndCMR);

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
    } else {

      while (i < 5) {

        int ran3 = (int) ((Math.random() * 9 + 1) * 100000);
        if (ran3 > 990000) {
          ran3 = ran3 - (int) ((Math.random() * 9 + 1) * 10000);
        }

        cndCMR = String.valueOf(ran3);
        // cndCMR = "6" + RDCRandomString.genNumericNumberSeries(5, kukla);

        LOG.debug("Generated France Non Internal CMR No.:" + cndCMR);

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
    }
    LOG.debug("generateCNDCmr :: returnung cndCMR = " + cndCMR);
    LOG.debug("generateCNDCmr :: END");
    return cndCMR;
  }

  @Override
  public boolean setAddrSeqByImport(AddrPK addrPk, EntityManager entityManager, FindCMRResultModel result) {
    return true;
  }

}
