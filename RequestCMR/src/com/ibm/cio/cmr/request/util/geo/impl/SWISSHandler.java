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
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.KunnrExt;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
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
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.CloningRDCDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for Switzerland
 * 
 * @author Rangoli Saxena
 * 
 */
public class SWISSHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(DEHandler.class);
  private static final List<String> IERP_ISSUING_COUNTRY_VAL = Arrays.asList("848");

  private static final String[] CH_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "LocalTax2", "SitePartyID", "POBoxCity", "Affiliate", "Company", "INACType",
      "POBoxPostalCode", "TransportZone", "CurrencyCode", "BPRelationType", "MembLevel", "CustomerName3" };

  public static final String SWISS_MASSCHANGE_TEMPLATE_ID = "SWISS";

  private static final List<String> ENABLE_MASSCHANGE_AUTO_TEMPLATE = Arrays.asList(SystemLocation.SWITZERLAND);
  public static final String[] HRDWRE_MSTR_FLAG_ADDRS = { "ZI01", "ZS01", "ZS02" };

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> recordsFromSearch = source.getItems();
    List<FindCMRRecordModel> filteredRecords = new ArrayList<>();
    List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();

    if (recordsFromSearch != null && !recordsFromSearch.isEmpty() && recordsFromSearch.size() > 0) {
      doFilterAddresses(reqEntry, recordsFromSearch, filteredRecords);
      if (filteredRecords != null && !filteredRecords.isEmpty() && filteredRecords.size() > 0) {
        Collections.sort(filteredRecords);
        source.setItems(filteredRecords);
      }
    }

    // CREATCMR-6139 Prospect CMR Conversion - address sequence A
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      FindCMRRecordModel record = source.getItems() != null && !source.getItems().isEmpty() ? source.getItems().get(0) : null;
      if (record != null) {
        if (StringUtils.isNotBlank(reqEntry.getCmrIssuingCntry()) && "848".equals(reqEntry.getCmrIssuingCntry())
            && StringUtils.isNotBlank(record.getCmrNum()) && record.getCmrNum().startsWith("P") && record.getCmrAddrSeq().equals("A")) {
          record.setCmrAddrSeq("00001");
          converted.add(record);
          source.setItems(converted);
        }
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

    String zs01sapNo = getKunnrSapr3Kna1(data.getCmrNo(), mainRecord.getCmrOrderBlock());
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
        data.setTaxCd1(getTaxCode(zs01sapNo));
      }
    } catch (Exception e) {
      LOG.error("Error occured on setting Currency Code/ tax code value during import.");
      e.printStackTrace();
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      if (StringUtils.isNotBlank(mainRecord.getCmrSortl())) {
        data.setSearchTerm(mainRecord.getCmrSortl());
      }
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
        String addrSeq = "1";
        addrSeq = StringUtils.leftPad(addrSeq, 5, '0');
        address.getId().setAddrSeq(addrSeq);
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
      // address.setCustNm4(name4);
      address.setDivn(name3);
      address.setCity2(name4);
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
    String cmrNo = "";
    String reqType = "";

    if (!StringUtils.isEmpty(addrType)) {
      if ("ZD02".equals(addrType)) {
        return "598";
      } else if ("ZP02".equals(addrType)) {
        return "599";
      }
      int addrSeq = 00000;
      String minAddrSeq = "00000";
      String maxAddrSeq = "99999";
      String sql = null;
      reqType = getReqType(entityManager, reqId);
      if (reqType.equalsIgnoreCase("U")) {
        cmrNo = getCMRNo(entityManager, reqId);
        sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.SWISS_U");
      } else {
        sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.SWISS_C");
      }
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setParameter("ADDR_TYPE", addrType);

      List<Object[]> results = query.getResults();
      if (results != null && results.size() > 0) {
        Object[] result = results.get(0);
        maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "00001");

        if (!(Integer.valueOf(maxAddrSeq) >= 00001 && Integer.valueOf(maxAddrSeq) <= 99999)) {
          maxAddrSeq = "";
        }
        if (StringUtils.isEmpty(maxAddrSeq) || addrType.equalsIgnoreCase("ZS01")) {
          maxAddrSeq = minAddrSeq;
        }
        try {
          addrSeq = Integer.parseInt(maxAddrSeq);
        } catch (Exception e) {
          // if returned value is invalid
        }
        addrSeq++;
      }
      newAddrSeq = String.format("%05d", Integer.parseInt(Integer.toString(addrSeq)));
      // newAddrSeq = Integer.toString(addrSeq);
      if (!StringUtils.isEmpty(cmrNo)) {
        newAddrSeq = "000" + cmrNo + "L" + newAddrSeq;
      }
    }
    return newAddrSeq;
  }

  public String getCMRNo(EntityManager entityManager, long reqId) {
    String cmrNo = "";
    String sql = ExternalizedQuery.getSql("DATA.GETCMRNO.SWISS");
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
    String sql = ExternalizedQuery.getSql("ADMIN.GETREQTYPE.SWISS");
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
    // CREATCMR-8304
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustPrefLang(), newData.getCustPrefLang())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustLangCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCustPrefLang(), "CustLangCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCustPrefLang(), "CustLangCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustClass", "-"));
      update.setNewData(newData.getCustClass());
      update.setOldData(oldData.getCustClass());
      results.add(update);
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

    List<String> custSubGrps = Arrays.asList("COM", "IBM", "PRI");
    if ("C".equals(admin.getReqType())) {
      data.setCurrencyCd("CHF");
    }
    if (StringUtils.isBlank(data.getCustPrefLang())) {
      String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.POST_CD.BY_REQID");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      query.setParameter("ADDR_TYPE", "ZS01");
      List<Object[]> results = query.getResults();
      if (!results.isEmpty() && results.get(0) != null) {
        int postCd;
        try {
          postCd = Integer.parseInt((String) results.get(0)[0]);
        } catch (NumberFormatException e) {
          postCd = 0;
          LOG.debug("Cannot parse postal code since it's alphanumeric.");
        }
        if ("C".equals(admin.getReqType())) {
          String landCntry = (String) results.get(0)[1];
          String custSubGrp = "";
          if (StringUtils.isNotBlank(cmrIssuingCntry) && StringUtils.isNotBlank(data.getCustSubGrp())) {
            custSubGrp = data.getCustSubGrp().substring(2);
          }
          if ("CH".equals(landCntry) || "LI".equals(landCntry) && custSubGrps.contains(custSubGrp)) {
            if ((postCd >= 3000 && postCd <= 6499) || (postCd >= 6999 && postCd <= 9999)) {
              data.setCustPrefLang("D");
            } else if (postCd >= 6500 && postCd <= 6999) {
              data.setCustPrefLang("I");
            } else if (postCd >= 0000 && postCd <= 3000) {
              data.setCustPrefLang("F");
            } else {
              data.setCustPrefLang("E");
            }

          } else {
            data.setCustPrefLang("E");
          }
        }
      }
    }

    AddrPK addrPk = new AddrPK();
    addrPk.setReqId(data.getId().getReqId());
    addrPk.setAddrSeq("00001");
    addrPk.setAddrType("ZS01");
    Addr addr = entityManager.find(Addr.class, addrPk);
    if (addr != null && StringUtils.isNotEmpty(data.getCustPrefLang())) {
      addr.setCustLangCd(data.getCustPrefLang());
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
      addr.setCustNm3("");
    }
  }

  @Override
  public boolean customerNamesOnAddress() {
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return false;
  }

  public static boolean isCHIssuingCountry(String issuingCntry) {
    if (SystemLocation.SWITZERLAND.equals(issuingCntry)) {
      return true;
    } else {
      return false;
    }
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

  /* Swiss Story : 1326413, 1834659 */
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

    if (soldToAddr != null) {
      admin.setOldCustNm1(soldToAddr.getCustNm1());
      admin.setOldCustNm2(soldToAddr.getCustNm2());
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
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "DEPT", "FLOOR", "BLDG", "OFFICE", "STATE_PROV", "CITY1", "CITY2",
        "DIVN", "POST_CD", "LAND_CNTRY", "PO_BOX", "ADDR_TXT", "CUST_PHONE", "CUST_LANG_CD", "HW_INSTL_MSTR_FLG"));
    return fields;
  }

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
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

  /* Swiss Story : 1326413 */
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
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##Division", "divn");
    map.put("##City2", "city2");

    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return true;
  }

  @Override
  public String getCMRNo(EntityManager rdcMgr, String kukla, String mandt, String katr6, String cmrNo, CmrCloningQueue cloningQueue) {
    LOG.debug("generateCNDCmr :: START");
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

  @Override
  public boolean setAddrSeqByImport(AddrPK addrPk, EntityManager entityManager, FindCMRResultModel result) {
    return true;
  }

  @Override
  public boolean isAddressChanged(EntityManager entityManager, Addr addr, String cmrIssuingCntry, boolean computedChangeInd) {
    boolean city2Updated = false;
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDRRDC.SEARCH_BY_REQID_TYPE_SEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
    Addr addrRdc = query.getSingleResult(Addr.class);

    if (addrRdc != null) {
      String addrRdcCity2 = !StringUtils.isBlank(addrRdc.getCity2()) ? addrRdc.getCity2() : "";
      String addrCity2 = !StringUtils.isBlank(addr.getCity2()) ? addr.getCity2() : "";

      if (!addrRdcCity2.equals(addrCity2)) {
        city2Updated = true;
      }
      return (city2Updated || computedChangeInd);
    }
    return true;
  }

}