/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.GeoContactInfo;
import com.ibm.cio.cmr.request.entity.GeoContactInfoPK;
import com.ibm.cio.cmr.request.entity.GeoTaxInfo;
import com.ibm.cio.cmr.request.entity.GeoTaxInfoPK;
import com.ibm.cio.cmr.request.entity.Scorecard;
import com.ibm.cio.cmr.request.entity.Stxl;
import com.ibm.cio.cmr.request.entity.TaxData;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel;
import com.ibm.cio.cmr.request.model.auto.BrazilV2ReqModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.GeoContactInfoModel;
import com.ibm.cio.cmr.request.model.requestentry.GeoTaxInfoModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.requestentry.SalesRepNameNoModel;
import com.ibm.cio.cmr.request.model.requestentry.SucursalCollBranchOffModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.service.requestentry.AdminService;
import com.ibm.cio.cmr.request.service.requestentry.GeoContactInfoService;
import com.ibm.cio.cmr.request.service.requestentry.TaxInfoService;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.BluePagesHelper;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Import Converter for LA
 * 
 * @author Jeffrey Zamora
 * 
 */
public class LAHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(LAHandler.class);
  /*
   * Author: Dennis T Natad Date: April 7, 2017 Project: createCMR LA
   * Requirements Defect 1180916: State/Province field should be optional for
   * landed country outside of LA
   */
  private static final List<String> LA_ISSUING_COUNTRY_VAL = Arrays.asList("613", "629", "631", "655", "661", "663", "681", "683", "829", "731",
      "735", "781", "799", "811", "813", "815", "869", "871");
  private static final List<String> LA_ISSUING_COUNTRY_LCR = Arrays.asList("663", "681", "829", "731", "735", "799", "811");

  private static final String[] BRAZIL_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Division", "LocalTax1" };

  private static final String[] LA_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Enterprise", "PPSCEID", "SitePartyID" };

  protected static final String[] LA_MASS_UPDATE_SHEET_NAMES = { "Sold-To", "Bill-To", "Ship-To", "Install-At", "TaxInfo", "Email",
      "AccountReceivable", "Data" };

  private static final String DEFAULT_SALES_REP = "111111";

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> converted = new ArrayList<>();
    List<FindCMRRecordModel> records = source.getItems();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : records) {
        if ("ZS01".equals(record.getCmrAddrTypeCode())) {

          if (SystemLocation.BRAZIL.equals(record.getCmrIssuedBy())) {
            record.setCmrAddrSeq(String.valueOf(getFixedStartingSeqNewAddrBR("ZS01")));
          } else {
            record.setCmrAddrSeq(String.valueOf(getFixedStartingSeqNewAddr("ZS01")));
          }
          converted.add(record);
        }
      }
      source.setItems(converted);
    }
  }

  @SuppressWarnings("unchecked")
  public static void doFilterAddresses(RequestEntryModel reqEntry, Object mainRecords, Object filteredRecords) {
    // #1308992
    String issuingCntry = reqEntry.getCmrIssuingCntry();
    if (isSSAMXBRIssuingCountry(issuingCntry) && (mainRecords instanceof java.util.List<?> && filteredRecords instanceof java.util.List<?>)) {
      if (reqEntry.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE)) {
        List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
        List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if (isBRIssuingCountry(issuingCntry)) {
              if (!StringUtils.isEmpty(reqEntry.getCustType())) {
                if (reqEntry.getCustType().equalsIgnoreCase(CmrConstants.CUST_TYPE_LEASI)) {
                  if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZS01") || tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZI01")) {
                    // add current record
                    recordsToReturn.add(tempRec);
                  }
                } else {
                  if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZS01")) {
                    // not LEASI
                    recordsToReturn.add(tempRec);
                  }
                }
              } else {
                if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZS01")) {
                  recordsToReturn.add(tempRec);
                }
              }
            } else {
              if (tempRec.getCmrAddrTypeCode().equalsIgnoreCase("ZS01")) {
                // FOR SSA MX RETURN ONLY THE LE ADDRESS
                recordsToReturn.add(tempRec);
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    String taxCd1 = mainRecord.getCmrBusinessReg();
    String taxCd2 = mainRecord.getCmrLocalTax2();
    String issuingCountry = mainRecord.getCmrIssuedBy();
    String sORTL = mainRecord.getCmrSortl();
    String subindustry = data.getSubIndustryCd();
    String mexicoBillingName = mainRecord.getCmrMexBillingName() != null ? mainRecord.getCmrMexBillingName() : "";
    AddressService addSvc = new AddressService();

    if (isMXIssuingCountry(issuingCountry)) {
      data.setMexicoBillingName(mexicoBillingName);
      data.setTaxCd3(mainRecord.getCmrMexFiscalRegime());
    }

    if (isBRIssuingCountry(issuingCountry)) {
      data.setProxiLocnNo(mainRecord.getCmrProxiLocn());

      String govType = "";
      if ("12".equals(mainRecord.getCmrClass())) {
        govType = "PF";
      } else if ("13".equals(mainRecord.getCmrClass())) {
        govType = "PE";
      } else {
        govType = "OU";
      }
      data.setGovType(govType);
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && mainRecord != null) {
      String kukla = mainRecord.getCmrClass() != null ? mainRecord.getCmrClass() : "";
      if (StringUtils.isNotEmpty(kukla) && kukla.substring(0, 1).equals("4")) {
        data.setPartnershipInd("Y");
        data.setMarketingContCd("1");
      } else {
        data.setPartnershipInd("N");
        data.setMarketingContCd("0");
      }
    }

    if (SystemLocation.CHILE.equalsIgnoreCase(issuingCountry)) {
      data.setBusnType(mainRecord.getCmrStxlTxtVal());
    } else if (SystemLocation.URUGUAY.equalsIgnoreCase(issuingCountry)) {
      String ibmBankNumberCdTxt = getLovCdByUpperTxt(SystemLocation.URUGUAY, "##IBMBankNumber", mainRecord.getCmrStxlTxtVal());
      data.setIbmBankNumber(ibmBankNumberCdTxt);

      // temporarily duplicates the value of IBM Bank Number after Import
      data.setBusnType(ibmBankNumberCdTxt);
    }

    if (StringUtils.isNotBlank(mainRecord.getCmrCollectorNo()) && mainRecord.getCmrCollectorNo().length() > 6) {
      data.setCollectorNameNo(mainRecord.getCmrCollectorNo().substring(0, 6));
    } else {
      data.setCollectorNameNo(mainRecord.getCmrCollectorNo());
    }

    data.setCollBoId(mainRecord.getCmrCollBo());

    // Defect 1267371 :Municipal Fiscal Code/ Tax Code 2 wrongly imported
    // from
    // RDC
    // if (!"".equals(issuingCountry) && issuingCountry.equals("631")) {
    // if (taxCd2 == null || "".equals(taxCd2)) {
    // data.setTaxCd2("");
    // }else{
    // data.setTaxCd2(mainRecord.getCmrLocalTax2());
    // }
    // }

    // if (!"".equals(issuingCountry) && !issuingCountry.equals("631")) {
    if (!isBRIssuingCountry(issuingCountry)) {
      // Rest of LA countries
      if (taxCd2 == null || "".equals(taxCd2)) {
        data.setTaxCd2("ISENTO");
      } else {
        data.setTaxCd2(taxCd2);
      }

      if (taxCd1 == null || "".equals(taxCd1)) {
        data.setTaxCd1("ISENTO");
      } else {
        data.setTaxCd1(taxCd1);
      }

      doSolveMrcIsuClientTierLogicOnImport(data, issuingCountry, sORTL);
      data.setBgId(mainRecord.getCmrBuyingGroup());
      data.setGbgId(mainRecord.getCmrGlobalBuyingGroup());
      data.setBgRuleId(mainRecord.getCmrLde());
      // addSvc.assignLocationCode(entityManager, address,
      // issuingCountry);
      Map<String, Object> hwBoRepTeam = addSvc.getHWBranchOffRepTeam(mainRecord.getCmrState());

      // Story 1247153
      if (hwBoRepTeam != null && !hwBoRepTeam.isEmpty()) {
        data.setHwSvcsBoNo(hwBoRepTeam.get("hardwBO") != null ? hwBoRepTeam.get("hardwBO").toString() : "");
        data.setHwSvcsRepTeamNo(hwBoRepTeam.get("hardwRTNo") != null ? hwBoRepTeam.get("hardwRTNo").toString() : "");
        data.setLocationNumber(hwBoRepTeam.get("locationNo") != null ? hwBoRepTeam.get("locationNo").toString() : "");
        data.setHwSvcsTeamCd(CmrConstants.DEFAULT_TEAM_CD);
        data.setHwSvcRepTmDateOfAssign(SystemUtil.getCurrentTimestamp());
      }

      if (!StringUtils.isEmpty(subindustry) && data != null) {
        try {
          HashMap industryMap = (HashMap) getZzkvSicValues(data.getSubIndustryCd());
          String industryVal = (String) industryMap.get(subindustry);

          if (!StringUtils.isEmpty(industryVal)) {
            LOG.debug("Auto selecting " + industryVal + " as CROS industry code.");
            data.setLegacyIndustryCode(industryVal);
          }

        } catch (Exception e) {
          LOG.error("An error has occured in retrieving Subindustry and ISU Map.");
          e.printStackTrace();
        }
      }
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        String[] name1Name2Val = getRDcName1Name2Values(mainRecord.getCmrSapNumber());
        String name1 = name1Name2Val != null && name1Name2Val[0] != null ? name1Name2Val[0].toString() : "";
        String name2 = name1Name2Val != null && name1Name2Val[1] != null ? name1Name2Val[1].toString() : "";
        name1Name2Val = splitName(name1, name2, 30, 40);
        name1 = name1Name2Val[0];

        if (!StringUtils.isEmpty(name1) && name1.length() <= 30) {
          data.setAbbrevNm(name1);
        } else if (!StringUtils.isEmpty(name1) && name1.length() > 30) {
          data.setAbbrevNm(name1.substring(0, 30));
        }

      }
    } else {
      doSolveMrcIsuClientTierLogicOnImport(data, issuingCountry, sORTL);
      data.setBgId(mainRecord.getCmrBuyingGroup());
      data.setGbgId(mainRecord.getCmrGlobalBuyingGroup());
      data.setBgRuleId(mainRecord.getCmrLde());
      // addSvc.assignLocationCode(entityManager, address,
      // issuingCountry);
      Map<String, Object> hwBoRepTeam = addSvc.getHWBranchOffRepTeam(mainRecord.getCmrState());

      // Story 1247153
      if (hwBoRepTeam != null && !hwBoRepTeam.isEmpty()) {
        data.setHwSvcsBoNo(hwBoRepTeam.get("hardwBO") != null ? hwBoRepTeam.get("hardwBO").toString() : "");
        data.setHwSvcsRepTeamNo(hwBoRepTeam.get("hardwRTNo") != null ? hwBoRepTeam.get("hardwRTNo").toString() : "");
        data.setLocationNumber(hwBoRepTeam.get("locationNo") != null ? hwBoRepTeam.get("locationNo").toString() : "");
        data.setHwSvcsTeamCd(CmrConstants.DEFAULT_TEAM_CD);
        data.setHwSvcRepTmDateOfAssign(SystemUtil.getCurrentTimestamp());
      }

      if (!StringUtils.isEmpty(subindustry) && data != null) {
        try {
          HashMap industryMap = (HashMap) getZzkvSicValues(data.getSubIndustryCd());
          String industryVal = (String) industryMap.get(subindustry);

          if (!StringUtils.isEmpty(industryVal)) {
            LOG.debug("Auto selecting " + industryVal + " as CROS industry code.");
            data.setLegacyIndustryCode(industryVal);
          }

        } catch (Exception e) {
          LOG.error("An error has occured in retrieving Subindustry and ISU Map.");
          e.printStackTrace();
        }
      }
    }
    if (CmrConstants.DEACTIVATE_CMR_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock()) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      boolean laReactivateCapable = PageManager.laReactivateEnabled(issuingCountry, "U");
      if (laReactivateCapable)
        data.setFunc("R");
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      data.setRepTeamMemberNo(DEFAULT_SALES_REP);
    }
  }

  private void importTaxInfo(EntityManager entityManager, Data data, long reqId, String sapNumber, String requesterId) {
    if (entityManager != null && sapNumber != null) {
      List<TaxData> taxDataList = getTaxDataByKunnr(entityManager, sapNumber);
      if (taxDataList != null && taxDataList.size() > 0) {
        List<GeoTaxInfo> taxInfoResults = getAllGeoTaxInfo(entityManager, reqId);
        TaxInfoService taxService = new TaxInfoService();

        if (taxInfoResults != null && !taxInfoResults.isEmpty() && taxInfoResults.size() > 0) {
          taxService.deleteAllTaxInfoById(taxInfoResults, entityManager, reqId);
        }

        int contactId = 1;
        for (TaxData taxData : taxDataList) {
          LOG.debug("***BEGIN PRINT RDC TAX INFO***");
          LOG.debug("taxData.getContractPrintIndc() >> " + taxData.getContractPrintIndc());
          LOG.debug("taxData.getCntryUse() >>" + taxData.getCntryUse());
          LOG.debug("taxData.getId().getTaxCd() >> " + taxData.getId().getTaxCd());
          LOG.debug("taxData.getTaxSeparationIndc() >> " + taxData.getTaxSeparationIndc());
          LOG.debug("taxData.getBillingPrintIndc() >> " + taxData.getBillingPrintIndc());
          LOG.debug("taxData.getTaxNum() >> " + taxData.getTaxNum());
          LOG.debug("***END PRINT RDC TAX INFO***");

          if (isBRIssuingCountry(data.getCmrIssuingCntry())) {
            if ("40".equals(taxData.getId().getTaxCd())) {
              setIcmsData(data, taxData.getTaxSeparationIndc());
              LOG.debug("BR ICMS ind: " + data.getIcmsInd());

              entityManager.merge(data);
              entityManager.flush();
              continue;
            }
          }

          GeoTaxInfo geoTaxInfo = new GeoTaxInfo();
          GeoTaxInfoPK geoTaxInfoPK = new GeoTaxInfoPK();

          geoTaxInfo.setContractPrintIndc(taxData.getContractPrintIndc());
          geoTaxInfo.setCntryUse(taxData.getCntryUse());
          geoTaxInfo.setTaxCd(taxData.getId().getTaxCd());
          geoTaxInfo.setTaxSeparationIndc(taxData.getTaxSeparationIndc());
          geoTaxInfo.setBillingPrintIndc(taxData.getBillingPrintIndc());
          geoTaxInfo.setTaxNum(taxData.getTaxNum());
          geoTaxInfoPK.setGeoTaxInfoId(contactId);
          geoTaxInfoPK.setReqId(reqId);
          geoTaxInfo.setId(geoTaxInfoPK);
          geoTaxInfo.setCreateTs(SystemUtil.getCurrentTimestamp());
          geoTaxInfo.setCreateById(requesterId);

          entityManager.persist(geoTaxInfo);
          entityManager.flush();
          contactId++;
        }
      }
    }
  }

  private void setIcmsData(Data data, String taxSepIndc) {
    if ("N".equals(taxSepIndc)) {
      data.setIcmsInd("1");
    } else if ("Y".equals(taxSepIndc)) {
      data.setIcmsInd("2");
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {

    String[] name1Name2Val = getRDcName1Name2Values(currentRecord.getCmrSapNumber());
    String name1 = StringUtils.isEmpty(name1Name2Val[0]) ? "" : name1Name2Val[0].toString();
    String name2 = StringUtils.isEmpty(name1Name2Val[1]) ? "" : name1Name2Val[1].toString();
    name1Name2Val = splitName(name1, name2, 30, 40);

    admin.setMainCustNm1(name1Name2Val[0]);
    admin.setOldCustNm1(name1Name2Val[0]);
    admin.setMainCustNm2(name1Name2Val[1]);
    admin.setOldCustNm2(name1Name2Val[1]);
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    String postalCode = currentRecord.getCmrPostalCode();
    postalCode = !StringUtils.isEmpty(postalCode) ? postalCode.replace("-", "") : "";
    String issuingCountry = currentRecord.getCmrIssuedBy();
    String streetAddr1 = address.getAddrTxt();
    address.setPostCd(postalCode);

    // Street
    address.setAddrTxt(currentRecord.getCmrStreetAddress());

    // Street Cont
    address.setAddrTxt2(currentRecord.getCmrName4());

    // Street 3 - might be changed to RDC Name3
    address.setCity2(currentRecord.getCmrCity2());

    // City
    address.setCity1(currentRecord.getCmrCity());

    // State Prov - computation
    address.setStateProv(currentRecord.getCmrState());

    if (StringUtils.isNotBlank(address.getCity1())) {
      address.setLocationCode(getLocationCd(issuingCountry, address.getCity1(), address.getStateProv()));
    }

    // #1180221: Name4 field for LA countries should populate Street Address
    // 2
    String orgName4 = !StringUtils.isEmpty(currentRecord.getCmrName4()) ? currentRecord.getCmrName4() : "";
    LOG.debug(orgName4);
    if (isMXIssuingCountry(issuingCountry)) {
      /* #1164406 */
      if (!StringUtils.isEmpty(streetAddr1)) {
        streetAddr1 = streetAddr1.replaceAll("(?i:\\bcommunity\\b)", "COL");
        // try 'com.' first
        /*
         * streetAddr1 = streetAddr1.replaceAll("(?i:\\bcom\\b[.])", "COL");
         */
        streetAddr1 = streetAddr1.replaceAll("(?i:\\bcom\\b)", "COL");
        streetAddr1 = streetAddr1.replaceAll("(?i:\\bcolonia\\b)", "COL");

        LOG.info("street address 1 after replace : " + streetAddr1);
        address.setAddrTxt(streetAddr1);
      }

      if (!StringUtils.isEmpty(orgName4)) {
        /* #1164406 */
        orgName4 = orgName4.replaceAll("(?i:\\bcommunity\\b)", "COL");
        // try 'com.' first
        /*
         * orgName4 = orgName4.replaceAll("(?i:\\bcom\\b[.])", "COL");
         */
        orgName4 = orgName4.replaceAll("(?i:\\bcom\\b)", "COL");
        orgName4 = orgName4.replaceAll("(?i:\\bcolonia\\b)", "COL");
        LOG.debug("ON IMPORT - Name4 after replaceAll() : " + orgName4);
      }

      if (orgName4.length() > 30) {
        /* #1164406 */
        // address.setAddrTxt2(orgName4.substring(0, 30));
        address.setCity2(orgName4.substring(31, orgName4.length()));
      } else {
        // address.setAddrTxt2(orgName4);
        address.setCity2("");
      }

      /* #1462767 */
      String cmrCity2 = !StringUtils.isEmpty(currentRecord.getCmrCity2()) ? currentRecord.getCmrCity2() : "";
      if (cmrCity2.length() > 30)
        address.setAddrTxt2(cmrCity2.substring(0, 30));
      else
        address.setAddrTxt2(cmrCity2);

      LOG.debug("ON IMPORT - Street Address 2 after replaceAll() : " + address.getAddrTxt2());

      // defect 1441287
      if ((address.getAddrTxt() != null && !address.getAddrTxt().contains("COL"))
          && (address.getAddrTxt2() != null && !address.getAddrTxt2().contains("COL"))) {
        /* #1433370 */
        String tempAddr2 = address.getAddrTxt2();

        if (tempAddr2.length() >= 28) {
          tempAddr2 = tempAddr2.substring(0, 27);
          tempAddr2 = "COL " + tempAddr2;
        } else {
          tempAddr2 = "COL " + tempAddr2;
        }
        address.setAddrTxt2(tempAddr2);
      }

      LOG.debug(">>> STREET ADDRESS 1 ON IMPORT >> " + address.getAddrTxt());
      LOG.debug(">>> STREET ADDRESS 2 ON IMPORT >> " + address.getAddrTxt2());

    }

    if (isSSAIssuingCountry(issuingCountry)) {
      /* #1166156 */
      if (SystemLocation.PERU.equalsIgnoreCase(issuingCountry)) {
        address.setPaymentAddrNo("1");
      }

      // if (StringUtils.isEmpty(address.getAddrTxt2())) {
      // address.setAddrTxt2(".");
      // }

      if (StringUtils.isEmpty(address.getCity2())) {
        address.setCity2(".");
      }

      /*
       * 1665319 SSA: Street Continuation should retrieve info from Name 4 and
       * Street Address 3 from Name 3
       */
      String addrTxt2 = !StringUtils.isEmpty(currentRecord.getCmrName4()) ? currentRecord.getCmrName4() : "";
      String addrTxt3 = !StringUtils.isEmpty(currentRecord.getCmrName3()) ? currentRecord.getCmrName3() : "";

      address.setAddrTxt2(addrTxt2);
      address.setCity2(addrTxt3);
      // if (!StringUtils.isEmpty(addrTxt2) && addrTxt2.length() > 30) {
      // address.setAddrTxt2(addrTxt2.substring(0, 30));
      // }
      // if (!StringUtils.isEmpty(addrTxt3) && addrTxt3.length() > 30) {
      // address.setCity2(addrTxt3.substring(0, 30));
      // }
      /*
       * End 1665319
       */
    }

    if (!isMXIssuingCountry(issuingCountry)) {
      if (!StringUtils.isEmpty(orgName4) && orgName4.length() > 35) {
        LOG.debug("Name 4 is more than 35 chars. . .");
        // get first 35 chars
        address.setAddrTxt2(orgName4.substring(0, 35));
        // get the other chars
        address.setCity2(orgName4.substring(36, orgName4.length())); // street
        // 3
      } else {
        LOG.debug("Name 4 is not more than 35 chars. . .");
        // address.setAddrTxt2(orgName4);
        // address.setCity2("");
      }
    } else {

    }

    if (SystemLocation.ARGENTINA.equalsIgnoreCase(issuingCountry) && StringUtils.isEmpty(address.getCity2())) {
      /* #1398828 */
      if (StringUtils.isEmpty(address.getCity2())) {
        address.setCity2(".");
      }
    }

    // 1245644 (special case for BR:(631) : add taxCd1 and taxCd2 fields to
    // each address type during import
    if (isBRIssuingCountry(issuingCountry)) {
      // MK: Defect 1641291: BR: Name 4 is not being imported into the
      // request
      String addrTxt2BR = !StringUtils.isEmpty(currentRecord.getCmrName4()) ? currentRecord.getCmrName4() : "";
      address.setAddrTxt2(addrTxt2BR);
      if (!StringUtils.isEmpty(addrTxt2BR) && addrTxt2BR.length() > 35) {
        address.setAddrTxt2(addrTxt2BR.substring(0, 35));
      }
      LOG.debug(">>> Name4 ADDRESS ON IMPORT >> " + address.getAddrTxt2());

      if (!StringUtils.isEmpty(currentRecord.getCmrBusinessReg())) {
        address.setTaxCd1(currentRecord.getCmrBusinessReg());
      } else {
        address.setTaxCd1("");// avoid NULL
      }

      String munFiscalCode = currentRecord.getCmrFiscalCd();
      address.setTaxCd2(StringUtils.isEmpty(munFiscalCode) ? "ISENTO" : munFiscalCode);
      assignLocationCodeOnImport(address, issuingCountry);

      // if (!StringUtils.isEmpty(currentRecord.getCmrLocalTax2())) {
      // address.setTaxCd2(currentRecord.getCmrLocalTax2());
      // } else {
      // 1245644 set taxcd1 value to taxcd2
      // if (!StringUtils.isEmpty(currentRecord.getCmrBusinessReg()) &&
      // "ISENTO".equalsIgnoreCase(currentRecord.getCmrBusinessReg())) {
      // address.setTaxCd2(currentRecord.getCmrBusinessReg());
      /*
       * Project : createCMR (Defect 1267371 :Municipal Fiscal Code/ Tax Code 2
       * wrongly imported from RDC) Created on (YYYY-MM-DD) : 2017-07-17 Author
       * : Mukesh Kumar
       */
      // address.setTaxCd2("");
      // }

      // }
      if (!StringUtils.isEmpty(currentRecord.getCmrVat())) {
        address.setVat(currentRecord.getCmrVat());
      } else {
        address.setVat("");// avoid NULL
      }
    } else {
      // avoid NULL
      address.setTaxCd1("");
      address.setTaxCd2("");
      address.setVat("");

      // address.setStateProv(getStateProvCROSMapping(issuingCountry,
      // currentRecord.getCmrState()));

      /*
       * Author: Dennis N. Defect 1457760: Provide city location code on import
       * for SSA and MX requests during create and update requests.
       */
      // address.setLocationCode();
      // assignLocationCodeonImportForSSA(address, issuingCountry);

    }

    // LA Reactivate change
    if (!CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(admin.getReqType())) {
      address.setDplChkResult(CmrConstants.ADDRESS_Not_Required);
      address.setDplChkInfo(null);
    }

    if (CmrConstants.DEACTIVATE_CMR_ORDER_BLOCK.equals(currentRecord.getCmrOrderBlock()) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      boolean laReactivateCapable = PageManager.laReactivateEnabled(issuingCountry, "U");
      if (laReactivateCapable)
        address.setDplChkResult("");
    }

    /*
     * 1665374: 'Street Address 1 is more than 30 CHAR' error should be
     * corrected with the truncate
     */
    String strAdd1 = address.getAddrTxt();
    String strAdd2 = address.getAddrTxt2();

    if (strAdd1.length() > 30) {
      String[] strAddrs = splitName(strAdd1, strAdd2, 30, 30);

      if (strAddrs != null && strAddrs.length > 0) {
        address.setAddrTxt(strAddrs[0]);
        address.setAddrTxt2(strAddrs[1]);
      }
    }
  }

  @Override
  public int getName1Length() {
    return 70;
  }

  @Override
  public int getName2Length() {
    return 70;
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public List<String> getDataFieldsForUpdate(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();

    if (StringUtils.isNotBlank(cmrIssuingCntry)) {
      if (SystemLocation.MEXICO.equalsIgnoreCase(cmrIssuingCntry)) {
        fields.addAll(Arrays.asList("MEXICO_BILLING_NAME", "TAX_CD3"));
      } else if (SystemLocation.BRAZIL.equalsIgnoreCase(cmrIssuingCntry)) {
        fields.addAll(Arrays.asList("SECONDARY_LOCN_NO", "LOCN_NO", "ICMS_IND"));
      } else if (SystemLocation.CHILE.equalsIgnoreCase(cmrIssuingCntry)) {
        fields.addAll(Arrays.asList("FOOTNOTE_TXT_LINE_1", "BUSN_TYP"));
      } else if (SystemLocation.URUGUAY.equalsIgnoreCase(cmrIssuingCntry)) {
        fields.addAll(Arrays.asList("IBM_BANK_NO", "BUSN_TYP"));
      }
    }

    fields.addAll(Arrays.asList("ABBREV_NM", "CUST_PREF_LANG", "SUB_INDUSTRY_CD", "ISIC_CD", "TAX_CD1", "CMR_OWNER", "ISU_CD", "CLIENT_TIER",
        "INAC_CD", "INAC_TYPE", "COMPANY", "PPSCEID", "COLL_BO_ID", "COLLECTOR_NO", "SALES_BO_CD", "EMAIL1", "EMAIL2", "EMAIL3", "COV_DESC", "COV_ID",
        "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD", "DUNS_NO"));

    return fields;
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    // STORY 1164429
    LOG.debug("setDataDefaultsOnCreate : start processing. . .");
    String issuingCntry = data.getCmrIssuingCntry();
    String sORTL = data.getSalesBusOffCd();
    String subindustry = data.getSubIndustryCd();
    String fiscalRegime = data.getTaxCd3();
    String mexBillingName = data.getMexicoBillingName();

    if ((!StringUtils.isEmpty(issuingCntry) && !StringUtils.isEmpty(sORTL) && null != data)) {
      // doSolveMrcIsuClientTierLogic(data, issuingCntry, sORTL);
      doSolveClientTierLogicOnSave(data, issuingCntry);
    }

    if (!StringUtils.isEmpty(subindustry) && data != null) {
      try {
        HashMap industryMap = (HashMap) getZzkvSicValues(data.getSubIndustryCd());
        String industryVal = (String) industryMap.get(subindustry);

        if (!StringUtils.isEmpty(industryVal)) {
          LOG.debug("Auto selecting " + industryVal + " as CROS industry code.");
          data.setLegacyIndustryCode(industryVal);
        }
      } catch (Exception e) {
        LOG.error("An error has occured in retrieving Subindustry and ISU Map.");
        e.printStackTrace();
      }
    }

    if (isBRIssuingCountry(issuingCntry)) {
      data.setCustomerIdCd(CmrConstants.DEFAULT_CUSTOMERID_CD); // C
      data.setTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD);// 001
      data.setNationalCusId(CmrConstants.DEFAULT_NATIONALCUS_ID);// N
      data.setInstallBranchOff(CmrConstants.DEFAULT_INSTALL_BRANCH_OFF);// 204
      data.setSalesTeamCd(CmrConstants.DEFAULT_TEAM_CD);// T
      data.setSalesTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD);// 001
      data.setSalesRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());
      data.setInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD);// T
      data.setInstallRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());
      data.setInstallRep(CmrConstants.DEFAULT_INSTALL_REP);// 204199

      data.setFomeZero(CmrConstants.DEFAULT_FOME_ZERO);
      data.setCodReason(CmrConstants.DEFAULT_COD_REASON); // 00
      data.setCodCondition(CmrConstants.DEFAULT_COD_CONDITION); // 0
      data.setRemoteCustInd(CmrConstants.DEFAULT_REMOTE_CUSTOMER_IND); // Y

      if (CmrConstants.YES_NO.N.equals(data.getIcmsInd())) {
        LOG.debug("*** ICMS Indicator is N, setting Tax Payet Cust cd to 1");
        data.setTaxPayerCustCd(CmrConstants.DEFAULT_TAX_PAYER_CUS_CD_1);
      } else if (CmrConstants.YES_NO.Y.equals(data.getIcmsInd())) {
        LOG.debug("*** ICMS Indicator is Y, setting Tax Payet Cust cd to 2");
        data.setTaxPayerCustCd(CmrConstants.DEFAULT_TAX_PAYER_CUS_CD_2);
      } else {
        LOG.debug("*** ICMS Indicator is either 1 or 2, setting Tax Payet Cust cd to either 1 or 2");
        data.setTaxPayerCustCd(data.getIcmsInd());
      }
    } else {
      data.setCustomerIdCd(CmrConstants.DEFAULT_CUSTOMERID_CD); // C
      data.setInstallBranchOff(CmrConstants.DEFAULT_INSTALL_BRANCH_OFF);// 204

      data.setSalesRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());

      data.setInstallRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());
      data.setInstallRep(data.getRepTeamMemberNo());
      data.setPaymentAddrCd("N");

    }

    if (isMXIssuingCountry(issuingCntry)) {
      data.setTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD_MX);// 000
      data.setSalesTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD_MX); // 000
      data.setTaxPayerCustCd(CmrConstants.DEFAULT_TAX_PAYER_CUS_CD_4);
      data.setCountryUse("000");
      data.setSalesTeamCd(CmrConstants.DEFAULT_TEAM_CD);// T
      data.setInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD);// T

      // CreatCMR-6683 - MX Predefined additional contact values
      createRecordTreatmentFunc(entityManager, "Sr.", ".", data.getId().getReqId(), issuingCntry, "LE");
    } else if (isSSAIssuingCountry(issuingCntry)) {
      data.setTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD);// 001
      data.setSalesTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD); // 001
      if (issuingCntry.equalsIgnoreCase(SystemLocation.CHILE)) {
        data.setCountryUse("001");
      }
    }

    if (isSSAIssuingCountry(issuingCntry)) {
      /* #1375515 */
      if (CmrConstants.SSAMX_INTERNAL_TYPES.contains(data.getCustSubGrp())) {
        if (SystemLocation.ECUADOR.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("", "1", data.getId().getReqId(), entityManager, false, false);
        } else if (SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry) || SystemLocation.PANAMA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.HONDURAS.equalsIgnoreCase(issuingCntry) || SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("Y", "1", data.getId().getReqId(), entityManager, false, false);
        } else if (SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry) || SystemLocation.GUATEMALA.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("N", "1", data.getId().getReqId(), entityManager, false, false);
        } else if (SystemLocation.CHILE.equalsIgnoreCase(issuingCntry) || SystemLocation.PERU.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("", "1", data.getId().getReqId(), entityManager, true, false);
        } else {
          doCreateDefaultTaxRecord("", "1", data.getId().getReqId(), entityManager, true, false);
        }
      } else {
        if (SystemLocation.ECUADOR.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("", "1", data.getId().getReqId(), entityManager, false, true);
        } else if (SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry) || SystemLocation.PANAMA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.HONDURAS.equalsIgnoreCase(issuingCntry) || SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("Y", "1", data.getId().getReqId(), entityManager, false, true);
        } else if (SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry) || SystemLocation.GUATEMALA.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("N", "1", data.getId().getReqId(), entityManager, false, true);
        } else if (SystemLocation.CHILE.equalsIgnoreCase(issuingCntry) || SystemLocation.PERU.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("", "1", data.getId().getReqId(), entityManager, true, false);
        } else if (SystemLocation.URUGUAY.equalsIgnoreCase(issuingCntry)) {
          doCreateDefaultTaxRecord("", "2", data.getId().getReqId(), entityManager, true, false);
        } else if (SystemLocation.ARGENTINA.equals(issuingCntry)) {
          // CREATCMR-6813 - AR Predefined tax info values
          String sql = ExternalizedQuery.getSql("AR.GET_GEOTAXINFORECORDS");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("REQ_ID", data.getId().getReqId());
          List<GeoTaxInfo> geoTaxInfoRecords = query.getResults(GeoTaxInfo.class);

          String taxCd1 = data.getTaxCd1();
          if ("LOCAL".equals(data.getCustGrp()) && CmrConstants.CUST_TYPE_IBMEM.equals(data.getCustSubGrp())) {
            if (StringUtils.isNotBlank(taxCd1) && taxCd1.length() >= 11) {
              String taxCd1Subtr = taxCd1.substring(3, 11);
              boolean createNewTaxRecords = false;

              if (geoTaxInfoRecords == null || geoTaxInfoRecords.isEmpty()) {
                createNewTaxRecords = true;
              } else {
                if (geoTaxInfoRecords.get(0) != null && StringUtils.isBlank(geoTaxInfoRecords.get(0).getTaxNum())) {
                  createNewTaxRecords = true;
                }
              }

              if (createNewTaxRecords) {
                deleteAllTaxInfoRecord(data, entityManager);
                doCreateARDefaultTaxRecord("01", taxCd1Subtr, data.getId().getReqId(), entityManager, true, true, true);
                doCreateARDefaultTaxRecord("11", taxCd1Subtr, data.getId().getReqId(), entityManager, false, false, false);
                doCreateARDefaultTaxRecord("02", taxCd1Subtr, data.getId().getReqId(), entityManager, false, false, false);
                doCreateARDefaultTaxRecord("07", taxCd1Subtr, data.getId().getReqId(), entityManager, false, false, false);
                doCreateARDefaultTaxRecord("12", taxCd1Subtr, data.getId().getReqId(), entityManager, false, false, false);
              } else {
                doUpdateARDefaultTaxRecord(taxCd1Subtr, data.getId().getReqId(), entityManager, data);
              }
            }
          } else {
            doCreateDefaultTaxRecord("", "1", data.getId().getReqId(), entityManager, true, true);
          }
        } else {
          doCreateDefaultTaxRecord("", "1", data.getId().getReqId(), entityManager, true, true);
        }
      }

      if ("683".equals(issuingCntry) || "661".equals(issuingCntry) || "629".equals(issuingCntry)) {
        // 1375486 ECUADOR COLOMBIA BOLIVIA_PLURINA
        createRecordTreatmentFunc(entityManager, "Sr.", ".", data.getId().getReqId(), issuingCntry, "EM");
        createRecordTreatmentFunc(entityManager, "Sr.", ".", data.getId().getReqId(), issuingCntry, "LE");
      }
      // else if ("815".equals(issuingCntry) || "655".equals(issuingCntry)
      // ||
      // "633".equalsIgnoreCase(issuingCntry) ||
      // "829".equals(issuingCntry)) {
      // // 1375486 PERU CHILE COSTA_RICA EL_SALVADOR
      // createRecordTreatmentFunc(entityManager, "Sr.", ".",
      // data.getId().getReqId(), issuingCntry, "LE");
      // }
      else if ("871".equals(issuingCntry)) {
        // 1375486 VENEZUELA_BOLIVARIAN
        createRecordTreatmentFunc(entityManager, "", ".", data.getId().getReqId(), issuingCntry, "EM");
        createRecordTreatmentFunc(entityManager, "", ".", data.getId().getReqId(), issuingCntry, "LE");
      } else {
        createRecordTreatmentFunc(entityManager, "Sr.", ".", data.getId().getReqId(), issuingCntry, "LE");
      }
    }

    if (isARIssuingCountry(issuingCntry) || "869".equals(issuingCntry)) {
      data.setInvoiceDistCd("00");
    }

  }

  public void createRecordTreatmentFunc(EntityManager entityManager, String treatmentVal, String funcVal, long reqId, String country,
      String contactType) {
    int contactId = 0;
    boolean checkRecord = false;
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CHECK.TREATMENT.FUNC.RECORD"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("CONTACT_TYPE", contactType);
    List<GeoContactInfo> results = query.getResults(GeoContactInfo.class);
    Admin reqAdmin = null;
    try {
      reqAdmin = new AdminService().getCurrentRecordById(reqId, entityManager);
    } catch (Exception ex) {
      LOG.error("createRecordTreatmentFunc : " + ex.getMessage());
      reqAdmin = new Admin();
    }

    if (results != null && !results.isEmpty() && results.size() > 0) {
      checkRecord = true;
    }
    if (!checkRecord && CmrConstants.REQ_TYPE_CREATE.equals(reqAdmin.getReqType())) {
      if (SystemLocation.PERU.equalsIgnoreCase(country)) {
        GeoContactInfoModel mod = new GeoContactInfoModel();
        mod.setCmrIssuingCntry(country);
        /* default to requester email address */
        mod.setContactEmail(reqAdmin.getRequesterId());
        mod.setContactName("N");
        mod.setContactPhone("");
        mod.setContactFunc(funcVal);
        mod.setContactTreatment(treatmentVal);
        List<GeoContactInfoModel> tempL = doContactInfoCreateStyle(mod);
        for (GeoContactInfoModel d : tempL) {
          GeoContactInfo e = new GeoContactInfo();
          GeoContactInfoPK ePk = new GeoContactInfoPK();
          e.setContactEmail(d.getContactEmail());
          e.setContactName(d.getContactName());
          e.setContactPhone(d.getContactPhone());
          e.setContactFunc(d.getContactFunc());
          e.setContactTreatment(d.getContactTreatment());
          e.setContactType(d.getContactType());
          e.setContactSeqNum(d.getContactSeqNum());
          e.setCreateById(reqAdmin.getRequesterId());
          e.setCreateTs(SystemUtil.getCurrentTimestamp());
          try {
            contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(reqId));
            ePk.setContactInfoId(contactId);
          } catch (CmrException ex) {
            LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
          }
          ePk.setReqId(reqId);
          e.setId(ePk);
          entityManager.persist(e);
          entityManager.flush();
        }
      } else {
        try {
          contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(reqId));
        } catch (CmrException ex) {
          LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
        }
        GeoContactInfo e = new GeoContactInfo();
        GeoContactInfoPK ePK = new GeoContactInfoPK();
        ePK.setReqId(reqId);
        ePK.setContactInfoId(contactId);
        e.setId(ePK);
        e.setContactType(contactType); // DEFAULT
        e.setContactSeqNum("001"); // DEFAULT
        e.setContactName("n");// DEFAULT
        e.setContactPhone(".");
        /* default to requester id */
        /*
         * Defect 1477871 - Email of requester shouldn't appear in the
         * Additional Contacts tab
         */
        e.setContactEmail("");
        e.setContactTreatment(treatmentVal);
        e.setContactFunc(funcVal);
        entityManager.persist(e);
        entityManager.flush();
      }
    }
  }

  /* 1205934 */
  public static List<GeoContactInfoModel> doContactInfoCreateStyle(GeoContactInfoModel modToCopy) {
    List<GeoContactInfoModel> tempList = new ArrayList<GeoContactInfoModel>();
    GeoContactInfoModel tempMod = null;
    int loopCount = 0;
    if (isBRIssuingCountry(modToCopy.getCmrIssuingCntry())) {
      while (loopCount < 3) {
        tempMod = new GeoContactInfoModel();
        copyContactModelProps(modToCopy, tempMod, loopCount);
        tempList.add(tempMod);
        loopCount++;
      }
    } else if (SystemLocation.PERU.equalsIgnoreCase(modToCopy.getCmrIssuingCntry())) {
      while (loopCount < 2) {
        tempMod = new GeoContactInfoModel();
        copyContactModelProps(modToCopy, tempMod, loopCount);
        tempList.add(tempMod);
        loopCount++;
      }
    } else if (isMXIssuingCountry(modToCopy.getCmrIssuingCntry())) {
      tempMod = new GeoContactInfoModel();
      copyContactModelProps(modToCopy, tempMod, loopCount);
      tempList.add(tempMod);
    }
    return tempList;
  }

  /* 1205934 */
  private static void copyContactModelProps(GeoContactInfoModel from, GeoContactInfoModel to, int loopCount) {
    to.setContactEmail(from.getContactEmail());
    to.setContactPhone(from.getContactPhone());
    to.setContactName(from.getContactName());
    to.setContactInfoId(loopCount); // just a dummy id
    to.setContactSeqNum("001");
    to.setContactType(CmrConstants.CONTACT_TYPE_BR_LST.get(loopCount));
    if (to.getContactType().equalsIgnoreCase("EM")
        && (from.getCmrIssuingCntry().equalsIgnoreCase(SystemLocation.BRAZIL) || from.getCmrIssuingCntry().equalsIgnoreCase(SystemLocation.PERU))) {
      String email1FromDb = new CmrClientService().getDataEmail1(from.getReqId(), false, null);
      if (!StringUtils.isEmpty(email1FromDb)) {
        if (!email1FromDb.toUpperCase().equals(to.getContactEmail().toUpperCase())) {
          // do nothing let js validation handle
        }
      }
    }
    to.setReqId(from.getReqId());
    to.setAction(from.getAction());
    to.setState(from.getState());
    to.setGridchk(from.getGridchk());
    to.setMassAction(from.getMassAction());
    to.setContactTreatment(from.getContactTreatment());
    to.setContactFunc(from.getContactFunc());
    to.setCmrIssuingCntry(from.getCmrIssuingCntry());
  }

  /* #1375515 */
  private void doCreateDefaultTaxRecord(String defaultBillingPrintIndc, String defaultTaxSepIndc, long reqId, EntityManager entMan, boolean indcFlag,
      boolean dumTaxCd) {
    TaxInfoService taxService = new TaxInfoService();
    GeoTaxInfo taxInfo = new GeoTaxInfo();
    GeoTaxInfoPK taxInfoPK = new GeoTaxInfoPK();
    Admin reqAdmin = null;

    try {
      reqAdmin = new AdminService().getCurrentRecordById(reqId, entMan);
    } catch (Exception ex) {
      LOG.error("doCreateDefaultTaxRecord : " + ex.getMessage());
      reqAdmin = new Admin();
    }

    if (taxService.getTaxInfoCountByReqId(entMan, reqId) <= 0 && reqAdmin.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE)) {
      taxInfoPK.setReqId(reqId);
      taxInfoPK.setGeoTaxInfoId(taxService.generateGeoTaxInfoID(entMan, reqId));
      if (indcFlag) {
        taxInfo.setTaxSeparationIndc(defaultTaxSepIndc);
      } else {
        taxInfo.setTaxSeparationIndc(defaultTaxSepIndc);
        taxInfo.setBillingPrintIndc(defaultBillingPrintIndc);
      }
      taxInfo.setContractPrintIndc("");/* dummy value */
      taxInfo.setCntryUse("");/* dummy value */

      if (dumTaxCd) {
        taxInfo.setTaxCd("");/* dummy value */
      } else {
        taxInfo.setTaxCd("01");/* dummy value */
      }

      taxInfo.setTaxNum("");/* dummy value */
      /* default to requester */
      taxInfo.setCreateById(reqAdmin.getRequesterId());
      taxInfo.setCreateTs(SystemUtil.getCurrentTimestamp());
      taxInfo.setId(taxInfoPK);
      entMan.persist(taxInfo);
      entMan.flush();
    }
  }

  // CREATCMR-6813
  private void doCreateARDefaultTaxRecord(String defaultTaxCd, String dataTaxCd, long reqId, EntityManager entityManager, boolean taxSepIndc,
      boolean contPntIndc, boolean cntryUse) {
    TaxInfoService taxService = new TaxInfoService();
    GeoTaxInfo taxInfo = new GeoTaxInfo();
    GeoTaxInfoPK taxInfoPK = new GeoTaxInfoPK();
    Admin reqAdmin = null;

    try {
      reqAdmin = new AdminService().getCurrentRecordById(reqId, entityManager);
    } catch (Exception ex) {
      LOG.error("doCreateARDefaultTaxRecord : " + ex.getMessage());
      reqAdmin = new Admin();
    }

    if (taxService.getTaxInfoCountByReqId(entityManager, reqId) <= 4 && reqAdmin.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE)) {
      taxInfoPK.setReqId(reqId);
      taxInfoPK.setGeoTaxInfoId(taxService.generateGeoTaxInfoID(entityManager, reqId));

      taxInfo.setTaxCd(defaultTaxCd); /* predefined value */

      if (taxSepIndc) {
        taxInfo.setTaxSeparationIndc("7"); /* predefined value */
      } else {
        taxInfo.setTaxSeparationIndc("N"); /* predefined value */
      }

      if (cntryUse) {
        taxInfo.setCntryUse(""); /* predefined value */
      } else {
        taxInfo.setCntryUse("0000"); /* predefined value */
      }

      if (contPntIndc) {
        taxInfo.setContractPrintIndc(""); /* predefined value */
      } else {
        taxInfo.setContractPrintIndc("N"); /* predefined value */
      }

      taxInfo.setTaxNum(dataTaxCd);
      /* default to requester */
      taxInfo.setCreateById(reqAdmin.getRequesterId());
      taxInfo.setCreateTs(SystemUtil.getCurrentTimestamp());
      taxInfo.setId(taxInfoPK);
      entityManager.merge(taxInfo);
      entityManager.flush();
    }
  }

  // CREATCMR-6813
  private void doUpdateARDefaultTaxRecord(String dataTaxCd, long reqId, EntityManager entityManager, Data data) {
    Admin reqAdmin = null;

    try {
      reqAdmin = new AdminService().getCurrentRecordById(reqId, entityManager);
    } catch (Exception ex) {
      LOG.error("doUpdateARDefaultTaxRecord : " + ex.getMessage());
      reqAdmin = new Admin();
    }

    if (reqAdmin.getReqType().equalsIgnoreCase(CmrConstants.REQ_TYPE_CREATE)) {

      String sql = ExternalizedQuery.getSql("AR.GET_GEOTAXINFORECORDS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", data.getId().getReqId());
      List<GeoTaxInfo> geoTaxInfoRecords = query.getResults(GeoTaxInfo.class);

      if (geoTaxInfoRecords != null) {
        for (GeoTaxInfo geoTaxInfoRecord : geoTaxInfoRecords) {
          geoTaxInfoRecord.setTaxNum(dataTaxCd);
          entityManager.merge(geoTaxInfoRecord);
          entityManager.flush();
        }
      }
    }
  }

  // CREATCMR-6813
  private void deleteAllTaxInfoRecord(Data data, EntityManager entityManager) {
    TaxInfoService taxService = new TaxInfoService();

    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.TAXINFO.SEARCH_BY_REQID"));
    query.setParameter("REQ_ID", data.getId().getReqId());
    List<GeoTaxInfo> results = query.getResults(GeoTaxInfo.class);

    // deletes any predefined entries
    if (results != null && !results.isEmpty() && results.size() > 0) {
      taxService.deleteAllTaxInfoById(results, entityManager, data.getId().getReqId());
    }
  }

  // STORY 1180239 1164429
  public void doSolveMrcIsuClientTierLogicOnImport(Data data, String issuingCountry, String sORTL) {
    LOG.debug("doSolveMrcIsuClientTierLogic : start processing. . .");
    LOG.debug("issuing country :" + issuingCountry);
    LOG.debug("sortL/branch office code :" + sORTL);
    CmrClientService cmrClientService = new CmrClientService();
    String retrievedMRCcode = (String) cmrClientService.getMRCFromSalesBranchOff(issuingCountry, sORTL);
    LOG.debug(retrievedMRCcode);
    if (!StringUtils.isEmpty(retrievedMRCcode)) {
      data.setMrcCd(retrievedMRCcode);
      String retrievedISUCode = (String) cmrClientService.getISUCode(issuingCountry, retrievedMRCcode);
      if (!StringUtils.isEmpty(retrievedISUCode)) {
        data.setIsuCd(retrievedISUCode);
        String retrievedClientTierCd = (String) cmrClientService.getClientTierCode(retrievedMRCcode, retrievedISUCode);
        if (!StringUtils.isEmpty(retrievedClientTierCd)) {
          data.setClientTier(retrievedClientTierCd);
        } else {
          LOG.debug("retrievedClientTierCd is blank. will set as blank.");
          data.setClientTier("");
        }
        LOG.debug(retrievedClientTierCd);
      } else {
        LOG.debug("No ISU_CODE retrieved or there are too many codes. . .");
        data.setIsuCd("");
      }
    } else {
      LOG.debug("No MRC_CODE retrieved or there are too many retrived codes. . . Setting default values");
      if (SystemLocation.BRAZIL.equals(issuingCountry) || SystemLocation.ARGENTINA.equals(issuingCountry)
          || SystemLocation.MEXICO.equals(issuingCountry) || SystemLocation.PERU.equals(issuingCountry)) {
        data.setMrcCd("M");
      } else if (SystemLocation.ECUADOR.equals(issuingCountry) || SystemLocation.PARAGUAY.equals(issuingCountry)
          || SystemLocation.URUGUAY.equals(issuingCountry)) {
        data.setMrcCd("P");
      } else {
        // This is worse case scenario if it does not fall on the first
        // two
        // conditions
        data.setMrcCd("");
      }

      if (!StringUtils.isEmpty(data.getMrcCd())) {
        String retrievedISUCode = (String) cmrClientService.getISUCode(issuingCountry, data.getMrcCd());
        if (!StringUtils.isEmpty(retrievedISUCode)) {
          data.setIsuCd(retrievedISUCode);
          String retrievedClientTierCd = (String) cmrClientService.getClientTierCode(data.getMrcCd(), retrievedISUCode);
          if (!StringUtils.isEmpty(retrievedClientTierCd)) {
            data.setClientTier(retrievedClientTierCd);
          } else {
            LOG.debug("retrievedClientTierCd is blank. will set as blank.");
            data.setClientTier("");
          }
          LOG.debug(retrievedClientTierCd);
        } else {
          LOG.debug("No ISU_CODE retrieved or there are too many codes. . .");
          data.setIsuCd("");
        }
      }
    }
    data.setSalesBusOffCd(sORTL);
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    mv.addObject("sucursalCollBO", new SucursalCollBranchOffModel());
    mv.addObject("salesRepNameNo", new SalesRepNameNoModel());
    mv.addObject("taxInfoModal", new GeoTaxInfoModel());
    mv.addObject("contactInfoModel", new GeoContactInfoModel());
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {

    String custType = admin.getCustType();
    String issuingCntry = data.getCmrIssuingCntry();

    final SimpleDateFormat NUCCHECKDATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    String nucDateToParse = NUCCHECKDATE_FORMAT.format(new Date());
    Date nucCheckDateToSave = null;
    try {
      nucCheckDateToSave = NUCCHECKDATE_FORMAT.parse(nucDateToParse);
    } catch (Exception ex) {
      nucCheckDateToSave = new Date();
      LOG.error(ex.getMessage() + "error parsing date. will create new date object.", ex);
    }

    if (isBRIssuingCountry(issuingCntry)) {
      if (CmrConstants.CUST_TYPE_PRIPE.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_IBMEM.equalsIgnoreCase(custType)) {// PRIPE,IBMEM
        data.setLegalIndicator(CmrConstants.LEGAL_INDICATOR_P);
        // admin.setDisableAutoProc(CmrConstants.CMT_LOCK_IND_YES);
      } else if (!"".equalsIgnoreCase(custType)) {
        data.setLegalIndicator(CmrConstants.LEGAL_INDICATOR_C);
      } else if ("".equalsIgnoreCase(custType)) {
        data.setLegalIndicator("");
      }

      if (CmrConstants.CUST_TYPE_BUSPR.equalsIgnoreCase(custType)) {// BUSPR
        data.setPartnershipInd(CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND);
        data.setMarketingContCd(CmrConstants.DEAFULT_BUSPR_MARKETCONT_CD);
      } else {
        data.setPartnershipInd(CmrConstants.DEFAULT_NONBUSPR_PARTNERSHIP_IND);
        data.setMarketingContCd(CmrConstants.DEAFULT_NONBUSPR_MARKETCONT_CD);
      }

      if (CmrConstants.CUST_TYPE_LEASI.equalsIgnoreCase(custType) && CmrConstants.REQ_TYPE_CREATE.equals(custType)) {// LEASI
        data.setLeasingCompanyIndc(CmrConstants.DEFAULT_LEASI_LEASINGCOMP_IND);
      } else {
        data.setLeasingCompanyIndc(CmrConstants.DEFAULT_NONLEASI_LEASINGCOMP_IND);
      }

      if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        if (CmrConstants.CUST_CLASS_33.equals(data.getCustClass()) || CmrConstants.CUST_CLASS_34.equals(data.getCustClass())) {
          data.setLeasingCompanyIndc(CmrConstants.DEFAULT_LEASI_LEASINGCOMP_IND);
          data.setCrosTyp(CmrConstants.DEFAULT_LEASI_CROSTYP);
          data.setCrosSubTyp(CmrConstants.DEFAULT_LEASI_CROSSUBTYP);
          admin.setCustType(CmrConstants.DEFAULT_LEASI_CUSTTYPE);
        } else {
          data.setLeasingCompanyIndc(CmrConstants.DEFAULT_NONLEASI_LEASINGCOMP_IND);
          data.setCrosTyp(CmrConstants.DEFAULT_CROS_TYPE);
          data.setCrosSubTyp(CmrConstants.DEFAULT_CROS_SUB_TYPE);
          admin.setCustType(CmrConstants.DEFAULT_CUST_TYPE);
        }
      }

      if (CmrConstants.CUST_TYPE_INTER.equalsIgnoreCase(custType)) {// INTER
        data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL); // CL
      } else {
        data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA); // SA
      }

      // if
      // (CmrConstants.CUST_TYPE_BLUEM.equalsIgnoreCase(admin.getCustType()))
      // {// BLUEM
      // admin.setDisableAutoProc(CmrConstants.CMT_LOCK_IND_YES);
      // }

      // if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      //
      // }

      if (CmrConstants.CUST_TYPE_IBMEM.equalsIgnoreCase(custType)) {// IBMEM
        data.setCustClass(CmrConstants.CUST_CLASS_IBMEM);
      }

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        data.setModeOfPayment(data.getCrosSubTyp());
      }

      // 1245941 IBM Bank Number mapping for BR(631)
      if (!StringUtils.isEmpty(custType)) {
        if (CmrConstants.CUST_TYPE_CC3CCC.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_LEASI.equalsIgnoreCase(custType)) {
          // will run only when IBMBankNumber is empty
          // to prevent overriding of value if current role is
          // 'Processor'
          if (StringUtils.isEmpty(data.getIbmBankNumber())) {
            data.setIbmBankNumber("001");
          }
        } else {
          // other customer types
          // optional for PRIPE 5COMP 5PRIP IBMEM BLUEM
          if (StringUtils.isEmpty(data.getIbmBankNumber()) && !(CmrConstants.CUST_TYPE_5PRIP.equalsIgnoreCase(custType)
              || CmrConstants.CUST_TYPE_5COMP.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_IBMEM.equalsIgnoreCase(custType)
              || CmrConstants.CUST_TYPE_PRIPE.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_BLUEM.equalsIgnoreCase(custType))) {
            data.setIbmBankNumber("34A");
          }
        }
      }
    }

    if (!importing || importing) {
      if (isMXIssuingCountry(issuingCntry)) {
        if (!StringUtils.isEmpty(custType)) {
          // #1375628
          if (custType.equalsIgnoreCase(CmrConstants.CUST_TYPE_IBMEM) || custType.equalsIgnoreCase(CmrConstants.CUST_TYPE_PRIPE)
              || custType.equalsIgnoreCase(CmrConstants.CUST_TYPE_5PRIP)) {
            data.setLegalIndicator("P");
          } else {
            data.setLegalIndicator("C");
          }
        } else {
          data.setLegalIndicator("");
        }
        /* #1381970 #1166142 */
        data.setCountryUse("000");
      }

      if (isSSAIssuingCountry(issuingCntry)) {
        /*
         * ARG,BOL,COL,COSRIC,DOMREP,ELSAL,GUAT,HOND,NICA,PAN,PAR,PER, URU,VEN
         */
        final List<String> minimalLstSSA_V1 = Arrays.asList(SystemLocation.ARGENTINA, SystemLocation.BOLIVIA_PLURINA, SystemLocation.COLOMBIA,
            SystemLocation.COSTA_RICA, SystemLocation.DOMINICAN_REP, SystemLocation.EL_SALVADOR, SystemLocation.GUATEMALA, SystemLocation.HONDURAS,
            SystemLocation.NICARAGUA, SystemLocation.PANAMA, SystemLocation.PARAGUAY, SystemLocation.PERU, SystemLocation.URUGUAY,
            SystemLocation.VENEZUELA_BOLIVARIAN);
        /* #1375431 */
        if (minimalLstSSA_V1.contains(issuingCntry)) {
          if (!SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry)) {
            // NOT FOR VENEZUELA
            data.setCusInvoiceCopies("01");
          }
          if (!SystemLocation.ARGENTINA.equalsIgnoreCase(issuingCntry) && !SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry)) {
            // NOT FOR ARGENTINA & VENEZUELA
            data.setIntlUseInvoiceCopies("00");
          }
          if (!SystemLocation.COLOMBIA.equalsIgnoreCase(issuingCntry) && !SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)
              && !SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry) && !SystemLocation.NICARAGUA.equalsIgnoreCase(issuingCntry)
              && !SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry)) {
            // NOT FOR COLOMBIA, COSTA RICA, EL SALVADOR, NICARAGUA
            // & VENEZUELA
            data.setDecentralizedOptIndc("N");
          }
          if (SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry)) {
            // FOR VENEZUELA
            data.setTaxPayerCustCd("1");
          }

          if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(requestType)) {
            /* #1373712 FOR DOMREP PAN HOND GUAT ELSALV COSTRIC */
            if (SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry) || SystemLocation.PANAMA.equalsIgnoreCase(issuingCntry)
                || SystemLocation.HONDURAS.equalsIgnoreCase(issuingCntry) || SystemLocation.GUATEMALA.equalsIgnoreCase(issuingCntry)
                || SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry) || SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)
                || SystemLocation.NICARAGUA.equalsIgnoreCase(issuingCntry)) {
              data.setCustAcctType("0");
            }
          }
        }

        /* #1166128 */
        data.setGenTermConCd("OW");

        /* #1165081 */
        /* ARGENTINA, PARAGUAY, URUGUAY */
        if (issuingCntry.equalsIgnoreCase(SystemLocation.ARGENTINA) || issuingCntry.equalsIgnoreCase(SystemLocation.PARAGUAY)
            || issuingCntry.equalsIgnoreCase(SystemLocation.URUGUAY)) {
          data.setEducAllowCd("0");
        }

        if (issuingCntry.equalsIgnoreCase(SystemLocation.CHILE)) {
          data.setCountryUse("001");/* #1166142 */
        }

        if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(requestType)) {
          data.setSalesTeamCd(CmrConstants.DEFAULT_TEAM_CD_SSA);// E
          data.setInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD_SSA);// E
        }

      }

      if (isSSAIssuingCountry(issuingCntry) || isMXIssuingCountry(issuingCntry)) {
        /* AR,BOL,CHIL,COL,COSTRIC,ECUAD,HOND,PAR,PER,DOMREP,URU,VEN */
        final List<String> minimalLstSSA_V2 = Arrays.asList(SystemLocation.ARGENTINA, SystemLocation.BOLIVIA_PLURINA, SystemLocation.CHILE,
            SystemLocation.COLOMBIA, SystemLocation.COSTA_RICA, SystemLocation.ECUADOR, SystemLocation.HONDURAS, SystemLocation.PARAGUAY,
            SystemLocation.PERU, SystemLocation.DOMINICAN_REP, SystemLocation.URUGUAY, SystemLocation.VENEZUELA_BOLIVARIAN);
        /* #1375405 */
        data.setDiversionRiskProf("Y");
        data.setDenialCusInd("N");
        if (data.getNuclChecklstDate() == null) {
          data.setNuclChecklstDate(nucCheckDateToSave);
        }

        if (minimalLstSSA_V2.contains(issuingCntry) || SystemLocation.MEXICO.equalsIgnoreCase(issuingCntry)) {
          data.setNuclCustCdByRiskLevel("NN");
          if (SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)) {
            // #1375405 FOR COSTA RICA
            data.setImportActIndc("N");
          }

          if (SystemLocation.ARGENTINA.equalsIgnoreCase(issuingCntry) || SystemLocation.URUGUAY.equalsIgnoreCase(issuingCntry)
              || SystemLocation.PARAGUAY.equalsIgnoreCase(issuingCntry)) {
            // #1375405 FOR ARGENTINA, URUGUAY & PARAGUAY
            data.setBioChemMissleMfg("N");
          }
        }

        /* #1250074 */
        // Defect 1465373 fix
        if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          if (CmrConstants.CUST_TYPE_BUSPR.equalsIgnoreCase(custType)) {// BUSPR
            data.setPartnershipInd(CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND);
            data.setMarketingContCd(CmrConstants.DEAFULT_BUSPR_MARKETCONT_CD);
          } else {
            data.setPartnershipInd(CmrConstants.DEFAULT_NONBUSPR_PARTNERSHIP_IND);
            data.setMarketingContCd(CmrConstants.DEAFULT_NONBUSPR_MARKETCONT_CD);
          }
        }

        /* #1373712 */
        if (!StringUtils.isEmpty(custType) && CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(requestType)) {
          if (CmrConstants.CUST_TYPE_INTER.equalsIgnoreCase(custType) || "INIBM".equalsIgnoreCase(custType) || "INGBM".equalsIgnoreCase(custType)
              || "INTEQ".equalsIgnoreCase(custType) || "INTUS".equalsIgnoreCase(custType) || "INTOU".equalsIgnoreCase(custType)
              || "INTPR".equalsIgnoreCase(custType)) {
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL);
          } else {
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA);
          }
        }
        // Mukesh :Defect 1470189
        if (!StringUtils.isEmpty(custType) && CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(requestType)) {
          final List<String> LstSSAMX_CreditCode_CL = Arrays.asList(SystemLocation.ARGENTINA, SystemLocation.BOLIVIA_PLURINA, SystemLocation.CHILE,
              SystemLocation.COLOMBIA, SystemLocation.ECUADOR, SystemLocation.MEXICO, SystemLocation.PERU, SystemLocation.URUGUAY,
              SystemLocation.VENEZUELA_BOLIVARIAN);
          final List<String> LstSSAMX_CreditCode_SA = Arrays.asList(SystemLocation.COSTA_RICA, SystemLocation.DOMINICAN_REP, SystemLocation.GUATEMALA,
              SystemLocation.HONDURAS, SystemLocation.NICARAGUA, SystemLocation.PANAMA, SystemLocation.PARAGUAY, SystemLocation.EL_SALVADOR);

          if (CmrConstants.CUST_TYPE_INTER.equalsIgnoreCase(custType) || "INIBM".equalsIgnoreCase(custType) || "INGBM".equalsIgnoreCase(custType)
              || "INTEQ".equalsIgnoreCase(custType) || "INTUS".equalsIgnoreCase(custType) || "INTOU".equalsIgnoreCase(custType)
              || "INTPR".equalsIgnoreCase(custType)) {
            if (LstSSAMX_CreditCode_CL.contains(issuingCntry)) {
              data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL);
            }
            if (LstSSAMX_CreditCode_SA.contains(issuingCntry)) {
              data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA);
            }
          }
        }

        if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(requestType)) {
          if (SystemLocation.MEXICO.equalsIgnoreCase(issuingCntry) || SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry)) {
            /* #1373712 */
            data.setCodCondition("0");
            if (SystemLocation.MEXICO.equalsIgnoreCase(issuingCntry)) {
              data.setCodReason("00");
            } else {
              data.setCodReason("01");
            }
          }
        }
      }
      // Mexico reactivation change
      if ("R".equals(data.getFunc()) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && isMXIssuingCountry(issuingCntry)) {
        boolean laReactivateCapable = PageManager.laReactivateEnabled(issuingCntry, "U");
        if (laReactivateCapable) {
          data.setCodCondition(CmrConstants.DEFAULT_COD_CONDITION); // 0
          if (data.getCmrNo().startsWith("99"))
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL); // CL
          else
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA); // SA
        }
      }
    }
  }

  public Map<String, Object> getZzkvSicValues(String bran1) throws Exception {
    HashMap<String, Object> zzkvSivVal = new HashMap<String, Object>();
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("ZZKV_SIC_FOR_INDUSTRY");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":BRAN1", "'" + bran1 + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BRAN1");
    query.addField("BRSCH");
    query.addField("ZZKV_GEO");

    LOG.debug("Getting existing values from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();

      for (int i = 0; i < records.size(); i++) {
        Map<String, Object> record = records.get(i);
        zzkvSivVal.put(record.get("BRAN1").toString(), record.get("BRSCH"));
      }

    }
    LOG.debug(">>> There are " + zzkvSivVal.size() + " records on the returned Map");
    return zzkvSivVal;
  }

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
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "SalesBusOff", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesBusOffCd(), "SalesBusOff", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesBusOffCd(), "SalesBusOff", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getBgId(), newData.getBgId())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "BuyingGroupID", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getBgId(), "BuyingGroupID", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getBgId(), "BuyingGroupID", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getGbgId(), newData.getGbgId())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "GlobalBuyingGroupID", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getGbgId(), "GlobalBuyingGroupID", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getGbgId(), "GlobalBuyingGroupID", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getBgRuleId(), newData.getBgRuleId())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "BGLDERule", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getBgRuleId(), "BGLDERule", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getBgRuleId(), "BGLDERule", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getRepresentativeTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "SalRepNameNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepresentativeTeamMemberNo(), "SalRepNameNo", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getMarketingChnlIndcValue(), newData.getMrktChannelInd())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "MrktChannelInd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getMrktChannelInd(), "MrktChannelInd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getMarketingChnlIndcValue(), "MrktChannelInd", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollBoId(), newData.getCollBoId())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "CollBranchOff", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollBoId(), "CollBranchOff", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollBoId(), "CollBranchOff", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollectorNo(), newData.getCollectorNameNo())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "CollectorNameNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectorNameNo(), "CollectorNameNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectorNo(), "CollectorNameNo", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getMrcCd(), newData.getMrcCd())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "MrcCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getMrcCd(), "MrcCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getMrcCd(), "MrcCd", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getIcmsInd(), newData.getIcmsInd())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "ICMSContribution", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getIcmsInd(), "ICMSContribution", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getIcmsInd(), "ICMSContribution", cmrCountry));
      results.add(update);
    }

    // phone
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getPhone1(), newData.getPhone1())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "Phone1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getPhone1(), "Phone1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getPhone1(), "Phone1", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getPhone2(), newData.getPhone2())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "Phone2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getPhone2(), "Phone2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getPhone2(), "Phone2", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getPhone3(), newData.getPhone3())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "Phone3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getPhone3(), "Phone3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getPhone3(), "Phone3", cmrCountry));
      results.add(update);
    }

    // contact name
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getContactName1(), newData.getContactName1())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "ContactName1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getContactName1(), "ContactName1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getContactName1(), "ContactName1", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getContactName2(), newData.getContactName2())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "ContactName2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getContactName2(), "ContactName2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getContactName2(), "ContactName2", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getContactName3(), newData.getContactName3())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "ContactName3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getContactName3(), "ContactName3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getContactName3(), "ContactName3", cmrCountry));
      results.add(update);
    }

    // emails
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmail1(), newData.getEmail1())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "Email1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmail1(), "Email1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmail1(), "Email1", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmail2(), newData.getEmail2())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "Email2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmail2(), "Email2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmail2(), "Email2", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmail3(), newData.getEmail3())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "Email3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmail3(), "Email3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmail3(), "Email3", cmrCountry));
      results.add(update);
    }

    // if(RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.get))

    /*
     * DENNIS: Commented code as we may not need this on update
     */
    // if (RequestSummaryService.TYPE_CUSTOMER.equals(type) &&
    // !equals(oldData.getGovType(), newData.getGovType())) {
    // update = new UpdatedDataModel();
    // String cntry = null;
    // update.setDataField(PageManager.getLabel(cntry, "GovernmentType",
    // "-"));
    // update.setNewData(service.getCodeAndDescription(newData.getGovType(),
    // "GovernmentType", cmrCountry));
    // update.setOldData(service.getCodeAndDescription(oldData.getGovType(),
    // "GovernmentType", cmrCountry));
    // results.add(update);
    // }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getIsbuCd(), newData.getProxiLocnNo())) {
      update = new UpdatedDataModel();
      String cntry = null;
      update.setDataField(PageManager.getLabel(cntry, "ProxiLocationNumber", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getProxiLocnNo(), "ProxiLocationNumber", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getIsbuCd(), "ProxiLocationNumber", cmrCountry));
      results.add(update);
    }

    if (isARIssuingCountry(cmrCountry)) {
      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd1(), newData.getTaxCd1())) {
        update = new UpdatedDataModel();
        String cntry = null;
        update.setDataField(PageManager.getLabel(cntry, "LocalTax1", "-"));
        update.setNewData(service.getCodeAndDescription(newData.getTaxCd1(), "LocalTax1", cmrCountry));
        update.setOldData(service.getCodeAndDescription(oldData.getTaxCd1(), "LocalTax1", cmrCountry));
        results.add(update);
      }
    }

    // Mexico Billing Name
    if (isMXIssuingCountry(cmrCountry)) {
      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getMexicoBillingName(), newData.getMexicoBillingName())) {
        update = new UpdatedDataModel();
        update.setDataField(PageManager.getLabel(cmrCountry, "BillingName", "-"));
        update.setNewData(newData.getMexicoBillingName());
        update.setOldData(oldData.getMexicoBillingName());
        results.add(update);
      }
    }

    // Uruguay Customer Invoice
    if (SystemLocation.URUGUAY.equals(cmrCountry)) {
      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getBusnType(), newData.getIbmBankNumber())) {
        update = new UpdatedDataModel();
        update.setDataField(PageManager.getLabel(cmrCountry, "IBMBankNumber", "-"));
        update.setNewData(service.getCodeAndDescription(newData.getIbmBankNumber(), "IBMBankNumber", cmrCountry));
        update.setOldData(service.getCodeAndDescription(oldData.getBusnType(), "IBMBankNumber", cmrCountry));
        results.add(update);
      }
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    request.setSORTL(data.getSalesBusOffCd());
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return false;
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {

    if (LAHandler.isMXIssuingCountry(cmrIssuingCntry) && "PPN".equalsIgnoreCase(admin.getReqStatus())
        && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      GeoContactInfoService contactService = new GeoContactInfoService();
      List<GeoContactInfo> leContacts = contactService.getCurrentLeRecords(entityManager, admin.getId().getReqId());

      if (leContacts == null || leContacts.size() == 0) {
        GeoContactInfoModel mod = new GeoContactInfoModel();
        mod.setCmrIssuingCntry(cmrIssuingCntry);
        mod.setContactEmail("");
        mod.setContactName("n");
        mod.setContactPhone(".");
        mod.setContactFunc("Sr.");
        mod.setContactTreatment("Sr.");
        List<GeoContactInfoModel> tempL = doContactInfoCreateStyle(mod);

        if (tempL != null && tempL.size() > 0) {
          GeoContactInfo e = new GeoContactInfo();
          GeoContactInfoPK ePk = new GeoContactInfoPK();
          GeoContactInfoModel ciModel = tempL.get(0);
          e.setContactEmail(ciModel.getContactEmail());
          e.setContactName(ciModel.getContactName());
          e.setContactPhone(ciModel.getContactPhone());
          e.setContactFunc(ciModel.getContactFunc());
          e.setContactTreatment(ciModel.getContactTreatment());
          e.setContactType(ciModel.getContactType());
          e.setContactSeqNum(ciModel.getContactSeqNum());
          e.setCreateById(admin.getRequesterId());
          e.setCreateTs(SystemUtil.getCurrentTimestamp());
          try {
            int contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(admin.getId().getReqId()));
            ePk.setContactInfoId(contactId);
          } catch (CmrException ex) {
            LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
          }
          ePk.setReqId(admin.getId().getReqId());
          e.setId(ePk);
          entityManager.persist(e);
          entityManager.flush();
        }
      }
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      // DENNIS: recalculate the the DPL by updating the sold to DPL flags
      if (admin != null) {
        // 2. If either is different, recalculate the DPL
        Scorecard score = getScorecardRecordByReqId(String.valueOf(admin.getId().getReqId()), entityManager);

        if (admin != null && score != null
            && CmrConstants.Scorecard_Not_Required.equalsIgnoreCase(score.getDplChkResult() != null ? score.getDplChkResult().trim() : "")) {
          if (!StringUtils.equals(admin.getMainCustNm1(), admin.getOldCustNm1())
              || !StringUtils.equals(admin.getMainCustNm2(), admin.getOldCustNm2())) {
            AddressService.clearDplResults(entityManager, admin.getId().getReqId());
          }
        }
      }
    }

  }

  protected static Scorecard getScorecardRecordByReqId(String reqId, EntityManager entityManager) throws CmrException {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.SCORECARD.SEARCH_BY_REQID");

    PreparedQuery query = new PreparedQuery(entityManager, sql);

    query.setParameter("REQ_ID", reqId);

    List<Scorecard> rs = query.getResults(1, Scorecard.class);

    if (rs != null && rs.size() > 0) {
      return rs.get(0);
    }
    return null;
  }

  public static void doSetScorecardNotDone(String reqId, EntityManager entityManager, String action, Admin admin, Scorecard score)
      throws CmrException {
    if (score == null) {
      score = getScorecardRecordByReqId(reqId, entityManager);
    }

    if (admin != null && score != null && CmrConstants.Scorecard_Not_Required.equalsIgnoreCase(score.getDplChkResult())) {
      if (!StringUtils.equals(admin.getMainCustNm1(), admin.getOldCustNm1()) || !StringUtils.equals(admin.getMainCustNm2(), admin.getOldCustNm2())) {
        AddressService.clearDplResults(entityManager, admin.getId().getReqId());
      }
    }

  }

  public static void doDPLNotDone(String reqId, EntityManager entityManager, String action, Admin admin, String lockedBy, String lockedByNm,
      String processedFlag) throws CmrException {
    Scorecard score = getScorecardRecordByReqId(reqId, entityManager);

    if (admin != null && score != null && CmrConstants.Scorecard_Not_Done.equalsIgnoreCase(score.getDplChkResult())) {

      admin.setReqStatus(CmrConstants.REQUEST_STATUS.PVA.toString());
      admin.setLockBy(lockedBy);
      admin.setLockByNm(lockedByNm);
      admin.setProcessedFlag(processedFlag);
      throw new CmrException(MessageUtil.ERROR_DPL_NOT_DONE);

    } else if (admin != null && score != null
        && !CmrConstants.Scorecard_Not_Required.equalsIgnoreCase(score.getDplChkResult() != null ? score.getDplChkResult().trim() : "")
        && !"AF".equals(score.getDplChkResult() != null ? score.getDplChkResult().trim() : "")
        && !"AP".equals(score.getDplChkResult() != null ? score.getDplChkResult().trim() : "")) {

      if (!StringUtils.equals(admin.getMainCustNm1(), admin.getOldCustNm1()) || !StringUtils.equals(admin.getMainCustNm2(), admin.getOldCustNm2())) {
        admin.setReqStatus(CmrConstants.REQUEST_STATUS.PVA.toString());
        admin.setLockBy(lockedBy);
        admin.setLockByNm(lockedByNm);
        admin.setProcessedFlag(processedFlag);
        throw new CmrException(MessageUtil.ERROR_DPL_NOT_DONE);
      }
    }
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

    String custType = admin.getCustType();
    String issuingCntry = data.getCmrIssuingCntry();
    String reqType = admin.getReqType();

    final SimpleDateFormat NUCCHECKDATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    String nucDateToParse = NUCCHECKDATE_FORMAT.format(new Date());
    Date nucCheckDateToSave = null;
    try {
      nucCheckDateToSave = NUCCHECKDATE_FORMAT.parse(nucDateToParse);
    } catch (Exception ex) {
      nucCheckDateToSave = new Date();
      LOG.error(ex.getMessage() + "error parsing date. will create new date object.", ex);
    }

    if (isBRIssuingCountry(issuingCntry)) {
      if (CmrConstants.CUST_TYPE_PRIPE.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_IBMEM.equalsIgnoreCase(custType)
          || CmrConstants.CUST_TYPE_5PRIP.equalsIgnoreCase(custType)) {// PRIPE,IBMEM,5PRIP
        data.setLegalIndicator(CmrConstants.LEGAL_INDICATOR_P);
      } else if (!"".equalsIgnoreCase(custType)) {
        data.setLegalIndicator(CmrConstants.LEGAL_INDICATOR_C);
      } else if ("".equalsIgnoreCase(custType)) {
        data.setLegalIndicator("");
      }

      if (CmrConstants.CUST_TYPE_BUSPR.equalsIgnoreCase(custType)) {// BUSPR
        data.setPartnershipInd(CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND);
        data.setMarketingContCd(CmrConstants.DEAFULT_BUSPR_MARKETCONT_CD);
      } else {
        data.setPartnershipInd(CmrConstants.DEFAULT_NONBUSPR_PARTNERSHIP_IND);
        data.setMarketingContCd(CmrConstants.DEAFULT_NONBUSPR_MARKETCONT_CD);
      }

      if (CmrConstants.CUST_TYPE_LEASI.equalsIgnoreCase(custType) && CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {// LEASI
        data.setLeasingCompanyIndc(CmrConstants.DEFAULT_LEASI_LEASINGCOMP_IND);
      } else {
        data.setLeasingCompanyIndc(CmrConstants.DEFAULT_NONLEASI_LEASINGCOMP_IND);
      }

      if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
        if (CmrConstants.CUST_CLASS_33.equals(data.getCustClass()) || CmrConstants.CUST_CLASS_34.equals(data.getCustClass())) {
          data.setLeasingCompanyIndc(CmrConstants.DEFAULT_LEASI_LEASINGCOMP_IND);
          data.setCrosTyp(CmrConstants.DEFAULT_LEASI_CROSTYP);
          data.setCrosSubTyp(CmrConstants.DEFAULT_LEASI_CROSSUBTYP);
          admin.setCustType(CmrConstants.DEFAULT_LEASI_CUSTTYPE);
        } else {
          data.setLeasingCompanyIndc(CmrConstants.DEFAULT_NONLEASI_LEASINGCOMP_IND);
          data.setCrosTyp(CmrConstants.DEFAULT_CROS_TYPE);
          data.setCrosSubTyp(CmrConstants.DEFAULT_CROS_SUB_TYPE);
          admin.setCustType(CmrConstants.DEFAULT_CUST_TYPE);
        }

        // DTN: New logic for setting sales team codes in update
        // requests
        // if the sales rep no is updated, then we default the sales
        // team
        // code as T. If it is not updated we retain what is there
        // stored
        // on the DB. That value should come from CROS.
        DataRdc dataRdc = new DataRdc();
        dataRdc = getOldData(entityManager, String.valueOf(data.getId().getReqId()));

        if (data != null && dataRdc != null && data.getRepTeamMemberNo() != null
            && !data.getRepTeamMemberNo().equals(dataRdc.getRepresentativeTeamMemberNo())) {
          data.setSalesTeamCd(CmrConstants.DEFAULT_TEAM_CD);
          data.setInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD);
        } else {
          // if it is equal, we need to make sure that the team code
          // is the same
          // as the one on DataRDc
          if (data != null && dataRdc != null && !data.getSalesTeamCd().equals(dataRdc.getVatExempt())) {
            data.setSalesTeamCd(dataRdc.getVatExempt());
            data.setInstallTeamCd(data.getIdentClient());
          }
        }

      }

      if (CmrConstants.CUST_TYPE_INTER.equalsIgnoreCase(custType)) {// INTER
        data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL); // CL
      } else {
        data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA); // SA
      }

      // if
      // (CmrConstants.CUST_TYPE_BLUEM.equalsIgnoreCase(admin.getCustType()))
      // {// BLUEM
      // admin.setDisableAutoProc(CmrConstants.CMT_LOCK_IND_YES);
      // }

      if (CmrConstants.CUST_TYPE_IBMEM.equalsIgnoreCase(custType)) {// IBMEM
        data.setCustClass(CmrConstants.CUST_CLASS_IBMEM);
      }

      if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
        // DTN:1669799:BR: Mode of Payment to be corrected for Internal
        // Cross
        // Boarder
        if (StringUtils.isBlank(data.getCustomerIdCd())) {
          data.setCustomerIdCd(CmrConstants.DEFAULT_CUSTOMERID_CD);
        }
        if (StringUtils.isBlank(data.getTerritoryCd())) {
          data.setTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD);// 001
        }
        if (StringUtils.isBlank(data.getNationalCusId())) {
          data.setNationalCusId(CmrConstants.DEFAULT_NATIONALCUS_ID);// N
        }
        if (StringUtils.isBlank(data.getInstallBranchOff())) {
          data.setInstallBranchOff(CmrConstants.DEFAULT_INSTALL_BRANCH_OFF);// 204
        }

        if (StringUtils.isBlank(data.getSalesTeamCd())) {
          data.setSalesTeamCd(CmrConstants.DEFAULT_TEAM_CD);// T
        }

        if (StringUtils.isBlank(data.getSalesTerritoryCd())) {
          data.setSalesTerritoryCd(CmrConstants.DEFAULT_TERRITORY_CD);// 001
        }
        if (StringUtils.isBlank(data.getInstallTeamCd())) {
          data.setInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD);// T
        }
        if (StringUtils.isBlank(data.getInstallRep())) {
          data.setInstallRep(CmrConstants.DEFAULT_INSTALL_REP);// 204199
        }

        if (StringUtils.isBlank(data.getFomeZero())) {
          data.setFomeZero(CmrConstants.DEFAULT_FOME_ZERO);
        }
        if (StringUtils.isBlank(data.getCodReason())) {
          data.setCodReason(CmrConstants.DEFAULT_COD_REASON); // 00
        }
        if (StringUtils.isBlank(data.getCodCondition())) {
          data.setCodCondition(CmrConstants.DEFAULT_COD_CONDITION); // 0
        }
        if (StringUtils.isBlank(data.getRemoteCustInd())) {
          data.setRemoteCustInd(CmrConstants.DEFAULT_REMOTE_CUSTOMER_IND); // Y
        }
        if (StringUtils.isBlank(data.getTaxPayerCustCd())) {
          if (CmrConstants.YES_NO.N.equals(data.getIcmsInd())) {
            LOG.debug("*** ICMS Indicator is N, setting Tax Payet Cust cd to 1");
            data.setTaxPayerCustCd(CmrConstants.DEFAULT_TAX_PAYER_CUS_CD_1);
          } else if (CmrConstants.YES_NO.Y.equals(data.getIcmsInd())) {
            LOG.debug("*** ICMS Indicator is Y, setting Tax Payet Cust cd to 2");
            data.setTaxPayerCustCd(CmrConstants.DEFAULT_TAX_PAYER_CUS_CD_2);
          } else {
            LOG.debug("*** ICMS Indicator is either 1 or 2, setting Tax Payet Cust cd to either 1 or 2");
            data.setTaxPayerCustCd(data.getIcmsInd());
          }
        }
        data.setSalesRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());
        data.setInstallRepTeamDateOfAssignment(SystemUtil.getCurrentTimestamp());

        if (StringUtils.isNotBlank(data.getLocationNumber()) && data.getLocationNumber().equals("90000")) {
          data.setLocationNumber("");
        }

        String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_ZS01");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", admin.getId().getReqId());
        Addr soldToAddr = query.getSingleResult(Addr.class);
        if (soldToAddr != null && StringUtils.isNotBlank(soldToAddr.getStateProv()) && soldToAddr.getStateProv().equals("RS")) {
          data.setLocationNumber("91000");
        }
        if ("CROSS".equals(data.getCustGrp())) {
          data.setModeOfPayment("CI");
        } else {
          data.setModeOfPayment(data.getCrosSubTyp());
        }
        // DTN:END 1669799
      }

      // 1245941 IBM Bank Number mapping for BR(631)
      if (!StringUtils.isEmpty(admin.getCustType())) {
        if (CmrConstants.CUST_TYPE_CC3CCC.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_LEASI.equalsIgnoreCase(custType)) {
          // will run only when IBMBankNumber is empty
          // to prevent overriding of value if current role is
          // 'Processor'
          if (StringUtils.isEmpty(data.getIbmBankNumber())) {
            data.setIbmBankNumber("001");
          }
        } else {
          // other customer types
          // optional for PRIPE 5COMP 5PRIP IBMEM BLUEM
          if (StringUtils.isEmpty(data.getIbmBankNumber()) && !(CmrConstants.CUST_TYPE_5PRIP.equalsIgnoreCase(custType)
              || CmrConstants.CUST_TYPE_5COMP.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_IBMEM.equalsIgnoreCase(custType)
              || CmrConstants.CUST_TYPE_PRIPE.equalsIgnoreCase(custType) || CmrConstants.CUST_TYPE_BLUEM.equalsIgnoreCase(custType))) {
            data.setIbmBankNumber("34A");
          }
        }
      }
      // 1202240 Denial Customer Indicator
      if (StringUtils.isEmpty(data.getDenialCusInd())) {
        data.setDenialCusInd("N"); // for BR default
      }
    }

    if (isMXIssuingCountry(issuingCntry)) {
      if (!StringUtils.isEmpty(custType)) {
        // #1375628
        if (custType.equalsIgnoreCase(CmrConstants.CUST_TYPE_IBMEM) || custType.equalsIgnoreCase(CmrConstants.CUST_TYPE_PRIPE)
            || custType.equalsIgnoreCase(CmrConstants.CUST_TYPE_5PRIP)) {
          data.setLegalIndicator("P");
        } else {
          data.setLegalIndicator("C");
        }
      } else {
        data.setLegalIndicator("");
      }
      /* #1381970 #1166142 */
      data.setCountryUse("000");

      // Story 1451797 set default value for SALES_TEAM_CD,
      // SECONDARY_SALES_TEAM_CD, TEAM_CD, SECONDARY_INSTALL_TEAM_CD
      data.setSalesTeamCd(CmrConstants.DEFAULT_TEAM_CD);
      data.setSecondarySalesTeamCd(CmrConstants.DEFAULT_TEAM_CD_SSA);
      data.setInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD);
      data.setSecondaryInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD_SSA);
    }

    if (isSSAIssuingCountry(issuingCntry)) {
      // Story 1451797 set default value for SALES_TEAM_CD,
      // SECONDARY_SALES_TEAM_CD, TEAM_CD, SECONDARY_INSTALL_TEAM_CD
      data.setSalesTeamCd(CmrConstants.DEFAULT_TEAM_CD_SSA);
      data.setSecondarySalesTeamCd(CmrConstants.DEFAULT_TEAM_CD);
      data.setInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD_SSA);
      data.setSecondaryInstallTeamCd(CmrConstants.DEFAULT_TEAM_CD);

      /*
       * defect 1467280 Apparently, a special case for internal
       */
      if (SystemLocation.ECUADOR.equals(issuingCntry)) {
        data.setCusInvoiceCopies("01");
        data.setIntlUseInvoiceCopies("00");
        data.setDecentralizedOptIndc("N");
      }

      /*
       * ARG,BOL,COL,COSRIC,DOMREP,ELSAL,GUAT,HOND,NICA,PAN,PAR,PER,URU, VEN
       */
      final List<String> minimalLstSSA_V1 = Arrays.asList(SystemLocation.ARGENTINA, SystemLocation.BOLIVIA_PLURINA, SystemLocation.COLOMBIA,
          SystemLocation.COSTA_RICA, SystemLocation.DOMINICAN_REP, SystemLocation.EL_SALVADOR, SystemLocation.GUATEMALA, SystemLocation.HONDURAS,
          SystemLocation.NICARAGUA, SystemLocation.PANAMA, SystemLocation.PARAGUAY, SystemLocation.PERU, SystemLocation.URUGUAY,
          SystemLocation.VENEZUELA_BOLIVARIAN);
      /* #1375431 */
      if (minimalLstSSA_V1.contains(issuingCntry)) {
        if (!SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry)) {
          // NOT FOR VENEZUELA
          data.setCusInvoiceCopies("01");
        }
        // if (!SystemLocation.ARGENTINA.equalsIgnoreCase(issuingCntry)
        // &&
        // !SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry))
        // {
        // NOT FOR ARGENTINA & VENEZUELA
        // #Defect 1461122 :
        if (SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry) || SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.GUATEMALA.equalsIgnoreCase(issuingCntry) || SystemLocation.PANAMA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry)) {
          // SET 01 FOR Costa Rica,Guatemala,Panama,Dominican Rep. ,El
          // Salvador
          data.setIntlUseInvoiceCopies("01");
        } else {
          // SET 00 FOR Others
          data.setIntlUseInvoiceCopies("00");
        }
        // }
        // if (!SystemLocation.COLOMBIA.equalsIgnoreCase(issuingCntry)
        // &&
        // !SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)
        // && !SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry)
        // &&
        // !SystemLocation.NICARAGUA.equalsIgnoreCase(issuingCntry)
        // &&
        // !SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry))
        // {
        // // NOT FOR COLOMBIA, COSTA RICA, EL SALVADOR, NICARAGUA &
        // VENEZUELA
        // data.setDecentralizedOptIndc("N");
        // }
        /*
         * Defect 1457802: Decentralized Option Indicator is required for
         * Argentina, bolivia, colombia, costa rica,el salvador, guatemala,
         * honduras, nicaragua, panama, paraguay, peru and uruguay
         */
        if (SystemLocation.ARGENTINA.equalsIgnoreCase(issuingCntry) || SystemLocation.BOLIVIA_PLURINA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.COLOMBIA.equalsIgnoreCase(issuingCntry) || SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry) || SystemLocation.GUATEMALA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.HONDURAS.equalsIgnoreCase(issuingCntry) || SystemLocation.NICARAGUA.equalsIgnoreCase(issuingCntry)
            || SystemLocation.PANAMA.equalsIgnoreCase(issuingCntry) || SystemLocation.PARAGUAY.equalsIgnoreCase(issuingCntry)
            || SystemLocation.PERU.equalsIgnoreCase(issuingCntry) || SystemLocation.URUGUAY.equalsIgnoreCase(issuingCntry)
            || SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry)) {

          data.setDecentralizedOptIndc("N");

        }

        if (SystemLocation.VENEZUELA_BOLIVARIAN.equalsIgnoreCase(issuingCntry)) {
          // FOR VENEZUELA
          data.setTaxPayerCustCd("1");
        }

        if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqType)) {
          /* #1373712 FOR DOMREP PAN HOND GUAT ELSALV COSTRIC */
          if (SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry) || SystemLocation.PANAMA.equalsIgnoreCase(issuingCntry)
              || SystemLocation.HONDURAS.equalsIgnoreCase(issuingCntry) || SystemLocation.GUATEMALA.equalsIgnoreCase(issuingCntry)
              || SystemLocation.EL_SALVADOR.equalsIgnoreCase(issuingCntry) || SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)
              || SystemLocation.NICARAGUA.equalsIgnoreCase(issuingCntry)) {
            data.setCustAcctType("0");
          }
        }
      }

      /* #1166128 */
      data.setGenTermConCd("OW");

      /* #1165081 */
      /* ARGENTINA, PARAGUAY, URUGUAY */
      if (issuingCntry.equalsIgnoreCase(SystemLocation.ARGENTINA) || issuingCntry.equalsIgnoreCase(SystemLocation.PARAGUAY)
          || issuingCntry.equalsIgnoreCase(SystemLocation.URUGUAY)) {
        data.setEducAllowCd("0");
      }

      // temporarily duplicates the value of IBM Bank Number
      if (issuingCntry.equalsIgnoreCase(SystemLocation.URUGUAY)) {
        data.setBusnType(data.getIbmBankNumber());
      }
    }

    if (isSSAIssuingCountry(issuingCntry) || isMXIssuingCountry(issuingCntry)) {
      /* AR,BOL,CHIL,COL,COSTRIC,ECUAD,HOND,PAR,PER,DOMREP,URU,VEN */
      final List<String> minimalLstSSA_V2 = Arrays.asList(SystemLocation.ARGENTINA, SystemLocation.BOLIVIA_PLURINA, SystemLocation.CHILE,
          SystemLocation.COLOMBIA, SystemLocation.COSTA_RICA, SystemLocation.ECUADOR, SystemLocation.HONDURAS, SystemLocation.PARAGUAY,
          SystemLocation.PERU, SystemLocation.DOMINICAN_REP, SystemLocation.URUGUAY, SystemLocation.VENEZUELA_BOLIVARIAN);
      /* #1375405 */
      data.setDiversionRiskProf("Y");
      data.setDenialCusInd("N");
      if (data.getNuclChecklstDate() == null) {
        data.setNuclChecklstDate(nucCheckDateToSave);
      }

      if (minimalLstSSA_V2.contains(issuingCntry) || SystemLocation.MEXICO.equalsIgnoreCase(issuingCntry)) {
        data.setNuclCustCdByRiskLevel("NN");
        if (SystemLocation.COSTA_RICA.equalsIgnoreCase(issuingCntry)) {
          // #1375405 FOR COSTA RICA
          data.setImportActIndc("N");
        }
        if (SystemLocation.ARGENTINA.equalsIgnoreCase(issuingCntry) || SystemLocation.URUGUAY.equalsIgnoreCase(issuingCntry)
            || SystemLocation.PARAGUAY.equalsIgnoreCase(issuingCntry)) {
          // #1375405 FOR ARGENTINA, URUGUAY & PARAGUAY
          data.setBioChemMissleMfg("N");
        }
      }

      /* #1250074 */
      // Defect 1465373 fix
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        if (CmrConstants.CUST_TYPE_BUSPR.equalsIgnoreCase(custType)) {// BUSPR
          data.setPartnershipInd(CmrConstants.DEFAULT_BUSPR_PARTNERSHIP_IND);
          data.setMarketingContCd(CmrConstants.DEAFULT_BUSPR_MARKETCONT_CD);
        } else {
          data.setPartnershipInd(CmrConstants.DEFAULT_NONBUSPR_PARTNERSHIP_IND);
          data.setMarketingContCd(CmrConstants.DEAFULT_NONBUSPR_MARKETCONT_CD);
        }
      }

      /* #1373712 */
      if (!StringUtils.isEmpty(custType) && CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqType)) {
        if (CmrConstants.CUST_TYPE_INTER.equalsIgnoreCase(custType) || "INIBM".equalsIgnoreCase(custType) || "INGBM".equalsIgnoreCase(custType)
            || "INTEQ".equalsIgnoreCase(custType) || "INTUS".equalsIgnoreCase(custType) || "INTOU".equalsIgnoreCase(custType)
            || "INTPR".equalsIgnoreCase(custType)) {
          data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL);
        } else {
          data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA);
        }
      }

      // Mukesh :Defect 1470189
      if (!StringUtils.isEmpty(custType) && CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqType)) {
        final List<String> LstSSAMX_CreditCode_CL = Arrays.asList(SystemLocation.ARGENTINA, SystemLocation.BOLIVIA_PLURINA, SystemLocation.CHILE,
            SystemLocation.COLOMBIA, SystemLocation.ECUADOR, SystemLocation.MEXICO, SystemLocation.PERU, SystemLocation.URUGUAY,
            SystemLocation.VENEZUELA_BOLIVARIAN);
        final List<String> LstSSAMX_CreditCode_SA = Arrays.asList(SystemLocation.COSTA_RICA, SystemLocation.DOMINICAN_REP, SystemLocation.GUATEMALA,
            SystemLocation.HONDURAS, SystemLocation.NICARAGUA, SystemLocation.PANAMA, SystemLocation.PARAGUAY, SystemLocation.EL_SALVADOR);

        if (CmrConstants.CUST_TYPE_INTER.equalsIgnoreCase(custType) || "INIBM".equalsIgnoreCase(custType) || "INGBM".equalsIgnoreCase(custType)
            || "INTEQ".equalsIgnoreCase(custType) || "INTUS".equalsIgnoreCase(custType) || "INTOU".equalsIgnoreCase(custType)
            || "INTPR".equalsIgnoreCase(custType)) {
          if (LstSSAMX_CreditCode_CL.contains(issuingCntry)) {
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL);
          }
          if (LstSSAMX_CreditCode_SA.contains(issuingCntry)) {
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA);
          }
        } else {
          data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA);
        }
      }

      if (CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(reqType)) {
        if (SystemLocation.MEXICO.equalsIgnoreCase(issuingCntry) || SystemLocation.DOMINICAN_REP.equalsIgnoreCase(issuingCntry)) {
          /* #1373712 */
          data.setCodCondition("0");
          if (SystemLocation.MEXICO.equalsIgnoreCase(issuingCntry)) {
            data.setCodReason("00");
          } else {
            data.setCodReason("01");
          }
        }
      }

      // Defect 1435291 - Processing Status = Aborted for the SSA
      // countries
      // create requests
      data.setModeOfPayment(data.getCrosSubTyp());
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        if (data.getAbbrevNm() == null || "".equals(data.getAbbrevNm())) {
          if (admin.getMainCustNm1() != null && admin.getMainCustNm1().length() > 30) {
            data.setAbbrevNm(admin.getMainCustNm1().substring(0, 30));
          } else {
            data.setAbbrevNm(admin.getMainCustNm1());
          }
        }
      }
    }

    /* 1375458 - Foot Note (Giro Del Negocio) field automation for Chile */
    if (isCLIssuingCountry(issuingCntry)) {
      data.setCountryUse("001");/* #1166142 */
      data.setFootnoteNo(CmrConstants.DEFAULT_FOOTNOTE_NO);

      String footNote1 = "";
      String footNote2 = "";
      String busnType = data.getBusnType() != null ? data.getBusnType() : "";
      int busnTypLen = data.getBusnType() != null ? data.getBusnType().length() : 0;
      LOG.debug("busnTypLen >> " + busnTypLen);

      if (busnTypLen > 0) {
        if (busnTypLen > 40) {
          footNote1 = busnType.substring(0, 40);
          footNote2 = busnType.substring(40, busnTypLen);
        } else {
          footNote1 = busnType;
          footNote2 = "";
        }
        LOG.debug("footNote1 >> " + footNote1);
        LOG.debug("footNote2 >> " + footNote2);
        data.setFootnoteTxt1(footNote1);
        data.setFootnoteTxt2(footNote2);
      }

    }

    /* 1165068 */
    if (isMXIssuingCountry(issuingCntry) || isSSAIssuingCountry(issuingCntry)) {
      data.setInstallBranchOff(data.getSalesBusOffCd());
    }

    boolean isLeasingBr = false;
    if (!StringUtils.isBlank(cmrIssuingCntry) && isBRIssuingCountry(cmrIssuingCntry)) {
      String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO_ZS01");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      Addr soldToAddr = query.getSingleResult(Addr.class);
      if (soldToAddr != null) {
        AddressService addrService = new AddressService();
        Map<String, Object> hwBoRepTeam = addrService.getHWBranchOffRepTeam(soldToAddr.getStateProv());
        if (hwBoRepTeam != null) {
          data.setLocationNumber(hwBoRepTeam.get("locationNo").toString());
          data.setHwSvcsBoNo(hwBoRepTeam.get("hardwBO") != null ? hwBoRepTeam.get("hardwBO").toString() : "");
          data.setHwSvcsRepTeamNo(hwBoRepTeam.get("hardwRTNo") != null ? hwBoRepTeam.get("hardwRTNo").toString() : "");
          data.setLocationNumber(hwBoRepTeam.get("locationNo") != null ? hwBoRepTeam.get("locationNo").toString() : "");
          data.setHwSvcsTeamCd(CmrConstants.DEFAULT_TEAM_CD);
        }
        if (hwBoRepTeam == null || hwBoRepTeam.isEmpty()) {
          addrService.updateDataForBRCreate(entityManager, null, soldToAddr);
        }

        if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
          if (CmrConstants.CUST_TYPE_LEASI.equals(data.getCustSubGrp()) && "34270520000136".equals(soldToAddr.getVat())) {
            data.setCustClass("33");
            isLeasingBr = true;
          } else if (CmrConstants.CUST_TYPE_LEASI.equals(data.getCustSubGrp()) && !"34270520000136".equals(soldToAddr.getVat())) {
            data.setCustClass("34");
            isLeasingBr = true;
          }

          // CreatCMR-6681 - BR Predefined enterprise value for local scenarios
          if ("LOCAL".equals(data.getCustGrp())
              && !(CmrConstants.CUST_TYPE_PRIPE.equals(data.getCustSubGrp()) || CmrConstants.CUST_TYPE_IBMEM.equals(data.getCustSubGrp()))
              && StringUtils.isNotBlank(soldToAddr.getVat())) {
            if (soldToAddr.getVat().length() >= 8) {
              data.setVat(soldToAddr.getVat());
              LOG.debug("Setting VAT in DATA table : " + soldToAddr.getVat());

              data.setEnterprise(soldToAddr.getVat().substring(0, 8));
              LOG.debug("Setting ENTERPRISE in DATA table : " + soldToAddr.getVat().substring(0, 8));
            }
          }
        }
      }
    }

    if ("Y".equals(data.getFunc()) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      boolean laReactivateCapable = PageManager.laReactivateEnabled(issuingCntry, "U");
      if (laReactivateCapable) {
        data.setFunc("R");
        if (isMXIssuingCountry(issuingCntry)) {
          data.setCodCondition(CmrConstants.DEFAULT_COD_CONDITION); // 0
          if (data.getCmrNo().startsWith("99"))
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_CL); // CL
          else
            data.setCreditCd(CmrConstants.DEFAULT_CREDIT_CD_SA); // SA
        }
      }
      // data.setFunc("R");
    }

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      if (CmrConstants.CUST_TYPE_INTER.equals(data.getCustSubGrp()) || "INTUS".equals(data.getCustSubGrp()) || "INIBM".equals(data.getCustSubGrp())) {
        data.setCustClass("81");
      } else if (CmrConstants.CUST_TYPE_BUSPR.equals(data.getCustSubGrp())) {
        data.setCustClass("45");
      } else if (CmrConstants.CUST_TYPE_PRIPE.equals(data.getCustSubGrp())) {
        data.setCustClass("60");
      } else if (CmrConstants.CUST_TYPE_IBMEM.equals(data.getCustSubGrp())) {
        data.setCustClass("71");
      } else if ("INTOU".equals(data.getCustSubGrp())) {
        data.setCustClass("85");
      } else {
        if (!isLeasingBr) {
          data.setCustClass("11");
        }
      }
    }

    // set custClass for Creates only
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      if (isBRIssuingCountry(issuingCntry)) {
        if (("GD".equals(data.getCrosSubTyp()) || "GI".equals(data.getCrosSubTyp())) && "PF".equals(data.getGovType())) {
          data.setCustClass("12");
        } else if ("GD".equals(data.getCrosSubTyp()) && !"PF".equals(data.getGovType())) {
          data.setCustClass("13");
        } else if ("GI".equals(data.getCrosSubTyp()) && !"PF".equals(data.getGovType())) {
          data.setCustClass("11");
        }
      }

      if (isMXIssuingCountry(issuingCntry)) {
        if ("GD".equals(data.getCrosSubTyp())) {
          data.setCustClass("12");
        } else if ("GI".equals(data.getCrosSubTyp())) {
          data.setCustClass("13");
        }
      }

      if (isARIssuingCountry(issuingCntry) || LA_ISSUING_COUNTRY_LCR.contains(cmrIssuingCntry)) {
        if ("GD".equals(data.getCrosSubTyp())) {
          data.setCustClass("12");
        }
      } else if (SystemLocation.URUGUAY.endsWith(cmrIssuingCntry) || SystemLocation.ECUADOR.equals(cmrIssuingCntry)
          || SystemLocation.PARAGUAY.equals(cmrIssuingCntry)) {
        if ("GD".equals(data.getCrosSubTyp())) {
          data.setCustClass("14");
        }
      } else if (SystemLocation.BOLIVIA_PLURINA.equals(cmrIssuingCntry) || SystemLocation.VENEZUELA_BOLIVARIAN.equals(cmrIssuingCntry)
          || SystemLocation.PERU.equals(cmrIssuingCntry) || SystemLocation.CHILE.equals(cmrIssuingCntry)
          || SystemLocation.COLOMBIA.equals(cmrIssuingCntry)) {
        if ("GD".equals(data.getCrosSubTyp())) {
          data.setCustClass("13");
        }
      }
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
      DataRdc dataRdc = getOldData(entityManager, String.valueOf(data.getId().getReqId()));
      if (StringUtils.isEmpty(data.getPpsceid())) {
        if (dataRdc != null) {
          data.setPpsceid(dataRdc.getPpsceid());
        }
      }
      if (data != null && dataRdc != null) {
        PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CHECK.CONTACT.INFO.RECORD"));
        query.setParameter("REQ_ID", data.getId().getReqId());
        List<GeoContactInfo> contacts = query.getResults(GeoContactInfo.class);

        for (GeoContactInfo contact : contacts) {
          if ("EM".equals(contact.getContactType())) {
            String email = StringUtils.isNotBlank(contact.getContactEmail()) ? contact.getContactEmail() : "";

            if ("001".equals(contact.getContactSeqNum())) {
              data.setEmail1(email);
              processContactsUpdate(email, dataRdc.getEmail1(), entityManager, contacts, contact, data);
            } else if ("002".equals(contact.getContactSeqNum())) {
              data.setEmail2(email);
              processContactsUpdate(email, dataRdc.getEmail2(), entityManager, contacts, contact, data);
            } else if ("003".equals(contact.getContactSeqNum())) {
              data.setEmail3(email);
              processContactsUpdate(email, dataRdc.getEmail3(), entityManager, contacts, contact, data);
            }
          }
        }
      }
    }
  }

  private void processContactsUpdate(String currentEmail, String oldEmail, EntityManager entityManager, List<GeoContactInfo> contacts,
      GeoContactInfo contactEM, Data data) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    if (!currentEmail.equalsIgnoreCase(oldEmail)) {
      saveOrUpdateAdditionalContactLE(entityManager, contacts, contactEM, data.getId().getReqId());
    }
  }

  private void saveOrUpdateAdditionalContactLE(EntityManager entityManager, List<GeoContactInfo> contacts, GeoContactInfo contactEM, long reqId)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    GeoContactInfoPK contactPk = new GeoContactInfoPK();

    String contactSeqEM = contactEM.getContactSeqNum();
    GeoContactInfo contactLE = contacts.stream().filter(r -> ("LE".equals(r.getContactType()) && contactSeqEM.equals(r.getContactSeqNum()))).findAny()
        .orElse(null);

    if (contactLE != null) {
      contactLE.setContactName(contactEM.getContactName());
      contactLE.setContactPhone(contactEM.getContactPhone());
      contactLE.setContactEmail(contactEM.getContactEmail());
      contactLE.setContactTreatment(contactEM.getContactTreatment());
      contactLE.setContactFunc(contactEM.getContactFunc());

      entityManager.merge(contactLE);
      entityManager.flush();

    } else {
      contactLE = new GeoContactInfo();
      PropertyUtils.copyProperties(contactLE, contactEM);
      contactLE.setContactType("LE");

      int contactId = 1;
      try {
        contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(reqId));
        contactPk.setContactInfoId(contactId);
      } catch (CmrException ex) {
        LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
      }

      contactPk.setReqId(reqId);
      contactLE.setId(contactPk);
      entityManager.persist(contactLE);
      entityManager.flush();
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {

    String streetAddr1 = addr.getAddrTxt();
    String streetAddr2 = addr.getAddrTxt2();
    String streetAddr3 = addr.getCity2();

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);

    if (isMXIssuingCountry(cmrIssuingCntry) && !(addr.getImportInd() == "Y")) {
      /* #1164406 FOR MANUAL CREATION OF ADDRESS */
      if (!StringUtils.isEmpty(streetAddr1)) {
        streetAddr1 = streetAddr1.replaceAll("(?i:\\bcommunity\\b)", "COL");
        // try 'com.' first
        /*
         * streetAddr1 = streetAddr1.replaceAll("(?i:\\bcom\\b[.])", "COL");
         */
        streetAddr1 = streetAddr1.replaceAll("(?i:\\bcom\\b)", "COL");
        streetAddr1 = streetAddr1.replaceAll("(?i:\\bcolonia\\b)", "COL");

        LOG.info("street address 1 after replace : " + streetAddr1);
        addr.setAddrTxt(streetAddr1);
      }

      if (!StringUtils.isEmpty(streetAddr2)) {
        streetAddr2 = streetAddr2.replaceAll("(?i:\\bcommunity\\b)", "COL");
        // try 'com.' first
        /*
         * streetAddr2 = streetAddr2.replaceAll("(?i:\\bcom\\b[.])", "COL");
         */
        streetAddr2 = streetAddr2.replaceAll("(?i:\\bcom\\b)", "COL");
        streetAddr2 = streetAddr2.replaceAll("(?i:\\bcolonia\\b)", "COL");
        LOG.info("street address 2 after replace : " + streetAddr2);

        addr.setAddrTxt2(streetAddr2);
      }

      // defect 1441287
      if ((!StringUtils.isEmpty(addr.getAddrTxt()) && !addr.getAddrTxt().contains("COL"))
          && (!StringUtils.isEmpty(addr.getAddrTxt2()) && !addr.getAddrTxt2().contains("COL"))) {
        /* #1433370 */
        String tempAddr2 = addr.getAddrTxt2();

        if (tempAddr2.length() >= 28) {
          tempAddr2 = tempAddr2.substring(0, 27);
          tempAddr2 = "COL " + tempAddr2;
        } else {
          tempAddr2 = "COL " + tempAddr2;
        }

        addr.setAddrTxt2(tempAddr2);
      } else if ((!StringUtils.isEmpty(addr.getAddrTxt()) && !addr.getAddrTxt().contains("COL")) && StringUtils.isEmpty(addr.getAddrTxt2())) {
        addr.setAddrTxt2("COL");
      }

      if (!StringUtils.isEmpty(streetAddr3)) {
        /*
         * streetAddr3 = streetAddr3.replaceAll("(?i:\\bcommunity\\b)", "COL");
         */
        // try 'com.' first
        /*
         * streetAddr3 = streetAddr3.replaceAll("(?i:\\bcom\\b[.])", "COL");
         */
        /*
         * streetAddr3 = streetAddr3.replaceAll("(?i:\\bcom\\b)", "COL");
         */
        /*
         * streetAddr3 = streetAddr3.replaceAll("(?i:\\bcolonia\\b)", "COL");
         */
        /*
         * LOG.info("street address 3 after replace : " + streetAddr3);
         */
        addr.setCity2(streetAddr3);
      }
    }

    if (isSSAIssuingCountry(cmrIssuingCntry)) {
      /* #1166156 */
      if (SystemLocation.PERU.equalsIgnoreCase(cmrIssuingCntry)) {
        addr.setPaymentAddrNo("1");
      }

      if (StringUtils.isEmpty(addr.getAddrTxt2())) {
        addr.setAddrTxt2(".");
      }

      if (StringUtils.isEmpty(addr.getCity2())) {
        addr.setCity2(".");
      }
    }

    if (SystemLocation.ARGENTINA.equalsIgnoreCase(cmrIssuingCntry)) {
      /* #1398828 */
      if (StringUtils.isEmpty(addr.getCity2())) {
        addr.setCity2(".");
      }
    }

    if (!StringUtils.isBlank(cmrIssuingCntry) && isBRIssuingCountry(cmrIssuingCntry) && RequestUtils.isRequesterAutomationEnabled(cmrIssuingCntry)) {
      // set values for Location code (required on backend only)
      AddressService addrService = new AddressService();
      if (addr.getId().getAddrType().equals("ZS01")) {
        addrService.updateDataForBRCreate(entityManager, null, addr);
      }
    }
  }

  @Override
  public boolean customerNamesOnAddress() {
    return false;
  }

  public String getBahnsValue(String kunnr) throws Exception {
    String bahnsVal = "";
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.LA.MUNICIPAL.FISCAL.CODE");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUNNR");
    query.addField("BAHNS");

    LOG.debug("Getting existing BAHNS value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      bahnsVal = record.get("BAHNS") != null ? record.get("BAHNS").toString() : "";

    }
    LOG.debug("Returning " + (StringUtils.isEmpty(bahnsVal) ? "EMPTY" : bahnsVal) + " as Trains value for " + kunnr);
    return bahnsVal;
  }

  public String[] getRDcName1Name2Values(String kunnr) throws Exception {
    String[] name1Name2Val = new String[2];

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.LA.NAME1.NAME2");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("NAME1");
    query.addField("NAME2");
    query.addField("KUNNR");

    LOG.debug("Getting existing NAME1, NAME2 value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      name1Name2Val[0] = record.get("NAME1") != null ? record.get("NAME1").toString() : "";
      name1Name2Val[1] = record.get("NAME2") != null ? record.get("NAME2").toString() : "";
    }

    return name1Name2Val;
  }

  @Deprecated
  public String[] getRDcName1Name2ValuesNonService(EntityManager em, String kunnr, String cmr, String katr6, String reqtype) {
    String[] name1Name2Val = new String[2];
    String mandt = SystemConfiguration.getValue("MANDT");
    LOG.debug("Getting existing NAME1, NAME2 value from RDc DB..");

    String strSelQuery = "";
    PreparedQuery query = null;

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqtype)) {
      strSelQuery = ExternalizedQuery.getSql("GET.LA.NAME1.NAME2.CREATE");
      query = new PreparedQuery(em, strSelQuery);
      query.setParameter("MANDT", mandt);
      query.setParameter("CMR", cmr);
      query.setParameter("KATR6", katr6);
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqtype)) {
      strSelQuery = ExternalizedQuery.getSql("GET.LA.NAME1.NAME2");
      query = new PreparedQuery(em, strSelQuery);
      query.setParameter("MANDT", mandt);
      query.setParameter("KUNNR", kunnr);
    }

    List<Object[]> names = query.getResults();

    if (names != null && names.size() > 0) {
      Object[] singleName = names.get(0);
      name1Name2Val[0] = singleName[0] != null ? (String) singleName[0] : "";
      name1Name2Val[1] = singleName[1] != null ? (String) singleName[1] : "";
    }

    return name1Name2Val;
  }

  private void assignLocationCodeOnImport(Addr address, String issuingCntry) throws Exception {
    EntityManager em = JpaManager.getEntityManager();
    AddressService addSvc = new AddressService();
    addSvc.assignLocationCode(em, address, issuingCntry);
  }

  public boolean isValidWWSubindustry(String bran1) throws Exception {
    boolean valid = true;
    HashMap<String, Object> zzkvSivVal = new HashMap<String, Object>();
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("ZZKV_SIC_FOR_INDUSTRY");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":BRAN1", "'" + bran1 + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BRAN1");
    query.addField("BRSCH");
    query.addField("ZZKV_GEO");

    LOG.debug("Getting existing values from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();

      if (records == null || records.isEmpty()) {
        LOG.debug("*** There are no records returned, " + bran1 + " is NOT a valid WW Subindustry.");
        valid = false;
      } else {
        LOG.debug("*** There are records returned, " + bran1 + " is a valid WW Subindustry.");
        valid = true;
      }

    } else {
      valid = false;
    }

    return valid;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return false;
  }

  public static boolean isARIssuingCountry(String issuingCountry) {
    boolean ret = false;
    if (issuingCountry != null && SystemLocation.ARGENTINA.equals(issuingCountry)) {
      ret = true;
    }
    return ret;
  }

  /**
   * Performs validation if the issuing country is BR Further improvement is
   * needed so that it also has a version that validates if the request has an
   * LA CMR Issuing Country.
   * 
   * @author Dennis Natad
   * @param model
   * @return true if request issuing country is '631', else false.
   */
  public static boolean isBRIssuingCountry(String issuingCntry) {
    boolean ret = false;
    if (issuingCntry != null && LA_ISSUING_COUNTRY_VAL.get(2).equals(issuingCntry)) {
      ret = true;
    }
    return ret;
  }

  public static boolean isLACountry(String issuingCntry) {
    return LA_ISSUING_COUNTRY_VAL.contains(issuingCntry);
  }

  /**
   * Performs validation if the issuing country is SSA
   * 
   * @param issuingCntry
   * @return
   */
  public static boolean isSSAIssuingCountry(String issuingCntry) {
    boolean ret = false;

    if (issuingCntry != null && !isBRIssuingCountry(issuingCntry) && !isMXIssuingCountry(issuingCntry)
        && LA_ISSUING_COUNTRY_VAL.contains(issuingCntry)) {
      ret = true;
    }

    return ret;
  }

  /**
   * Performs validation if the issuing country is SSA, BR, MX
   * 
   * @param issuingCntry
   * @return
   */
  public static boolean isSSAMXBRIssuingCountry(String issuingCntry) {
    boolean ret = false;

    if (issuingCntry != null && LA_ISSUING_COUNTRY_VAL.contains(issuingCntry)) {
      ret = true;
    }

    return ret;
  }

  public static boolean isCLIssuingCountry(String issuingCntry) {
    boolean ret = false;

    if (SystemLocation.CHILE.equalsIgnoreCase(issuingCntry)) {
      ret = true;
    }

    return ret;

  }

  /**
   * Performs validation if the issuing country is MX
   * 
   * @param issuingCntry
   * @return
   */
  public static boolean isMXIssuingCountry(String issuingCntry) {
    boolean ret = false;

    if (issuingCntry != null && LA_ISSUING_COUNTRY_VAL.get(11).equals(issuingCntry)) {
      ret = true;
    }

    return ret;

  }

  public static boolean isDRIssuingCountry(String issuingCntry) {
    if (issuingCntry.equals(SystemLocation.DOMINICAN_REP)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    boolean skipUpdate = Arrays.asList(LA_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    if (SystemLocation.BRAZIL.equals(cntry)) {
      return skipUpdate || Arrays.asList(BRAZIL_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    }
    return skipUpdate;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    LOG.debug("Inside doAfterImport method");

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      if (data.getId() != null) {
        if ("88".equalsIgnoreCase(data.getOrdBlk())) {
          data.setDenialCusInd("Y");
          data.setEmbargoCd("Y");
        } else {
          data.setDenialCusInd("N");
          data.setEmbargoCd("N");
        }

        String reqId = String.valueOf(data.getId().getReqId());
        // NOTE: As of MVP2 - no stories for secondary sold to
        // we only have one ZS01 (main sold-to)
        AddressService addrSvc = new AddressService();
        String sapNumber = addrSvc.getAddressSapNo(entityManager, reqId, "ZS01");
        importTaxInfo(entityManager, data, data.getId().getReqId(), sapNumber, admin.getRequesterId());
        importAddtlContacts(entityManager, data, admin, reqId, sapNumber);

        DataPK dataRdcPk = new DataPK();
        dataRdcPk.setReqId(data.getId().getReqId());
        DataRdc dataRdc = entityManager.find(DataRdc.class, dataRdcPk);

        PropertyUtils.copyProperties(dataRdc, data);
        dataRdc.setId(data.getId());

        dataRdc.setCollectorNo(data.getCollectorNameNo());

        entityManager.merge(dataRdc);
        entityManager.flush();
      }
    }
  }

  private void importAddtlContacts(EntityManager entityManager, Data data, Admin admin, String reqId, String sapNumber) {
    int contactId = 1;
    List<Stxl> stxlList = getStxlAddlContactsByKunnr(entityManager, sapNumber);
    if (stxlList != null && stxlList.size() > 0) {
      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CHECK.CONTACT.INFO.RECORD"));
      query.setParameter("REQ_ID", data.getId().getReqId());
      List<GeoContactInfo> results = query.getResults(GeoContactInfo.class);
      GeoContactInfoService contService = new GeoContactInfoService();

      if (results != null && !results.isEmpty() && results.size() > 0) {
        contService.deleteAllContactDetails(results, entityManager, data.getId().getReqId());
      }

      for (Stxl d : stxlList) {
        GeoContactInfo e = new GeoContactInfo();
        GeoContactInfoPK ePk = new GeoContactInfoPK();
        e.setContactType("EM");

        String addlConSeq = "";
        if ("ZOA1".equals(d.getId().getTdid())) {
          addlConSeq = "001";
          data.setEmail1(d.getClustd());
        } else if ("ZOA2".equals(d.getId().getTdid())) {
          addlConSeq = "002";
          data.setEmail2(d.getClustd());
        } else if ("ZOA3".equals(d.getId().getTdid())) {
          addlConSeq = "003";
          data.setEmail3(d.getClustd());
        }

        e.setContactSeqNum(addlConSeq);
        e.setContactName("N");
        e.setContactPhone(".");
        e.setContactEmail(d.getClustd());
        e.setContactFunc(".");
        e.setContactTreatment("Sr.");

        e.setCreateById(admin.getRequesterId());
        e.setCreateTs(SystemUtil.getCurrentTimestamp());
        try {
          contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(admin.getId().getReqId()));
          ePk.setContactInfoId(contactId);
        } catch (CmrException ex) {
          LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
        }
        ePk.setReqId(data.getId().getReqId());
        e.setId(ePk);
        entityManager.persist(e);
        entityManager.flush();
      }
    }

    // TODO: START - delete code block when we switch to DR
    if (stxlList != null && stxlList.size() > 0) {
      // contacts already deleted on EM check
      addDefaultAddtlContactLE(entityManager, data, admin);
    } else {
      // delete all contacts
      PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CHECK.CONTACT.INFO.RECORD"));
      query.setParameter("REQ_ID", data.getId().getReqId());
      List<GeoContactInfo> results = query.getResults(GeoContactInfo.class);
      GeoContactInfoService contService = new GeoContactInfoService();

      if (results != null && !results.isEmpty() && results.size() > 0) {
        contService.deleteAllContactDetails(results, entityManager, data.getId().getReqId());
      }

      addDefaultAddtlContactLE(entityManager, data, admin);
    }
    // TODO: END - delete code block when we switch to DR
  }

  private void addDefaultAddtlContactLE(EntityManager entityManager, Data data, Admin admin) {

    GeoContactInfo e = new GeoContactInfo();
    GeoContactInfoPK ePk = new GeoContactInfoPK();
    e.setContactType("LE");
    e.setContactSeqNum("001");
    e.setContactName("N");
    e.setContactPhone(".");
    e.setContactEmail("");
    e.setContactFunc(".");
    e.setContactTreatment("Sr.");

    e.setCreateById(admin.getRequesterId());
    e.setCreateTs(SystemUtil.getCurrentTimestamp());

    try {
      int contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(admin.getId().getReqId()));
      ePk.setContactInfoId(contactId);
    } catch (CmrException ex) {
      LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
    }
    ePk.setReqId(data.getId().getReqId());
    e.setId(ePk);
    entityManager.persist(e);
    entityManager.flush();
  }

  private List<Stxl> getStxlAddlContactsByKunnr(EntityManager entityManager, String kunnr) {
    String tdname = kunnr + "%";
    String sql = ExternalizedQuery.getSql("LA.GET_STXL_ADDL_CONTACTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("TDNAME", tdname);

    return query.getResults(Stxl.class);
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    if (StringUtils.isNotBlank(cmrIssuingCntry)) {
      if (SystemLocation.BRAZIL.equalsIgnoreCase(cmrIssuingCntry)) {
        fields.addAll(Arrays.asList("TAX_CD_1", "TAX_CD_2", "VAT"));
      }
    }

    fields.addAll(Arrays.asList("LAND_CNTRY", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "STATE_PROV", "CITY2", "POST_CD"));

    return fields;
  }

  @Override
  protected String[] splitName(String name1, String name2, int length1, int length2) {
    String name = name1 + (name2 != null ? " " + name2 : "");
    String[] parts = name.split("[ ]");

    String namePart1 = "";
    String namePart2 = "";
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
      namePart1 = StringUtils.isEmpty(name.trim()) ? "" : name.substring(0, length1);
      namePart2 = StringUtils.isEmpty(name.trim()) ? "" : name.substring(length1);
    }
    if (namePart1.length() > length1) {
      namePart1 = namePart1.substring(0, length1);
    }
    namePart2 = namePart2.trim();
    if (namePart2.length() > length2) {
      namePart2 = namePart2.substring(0, length2);
    }

    return new String[] { namePart1, namePart2 };

  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  // STORY 1406520 - Coverage information update 2018 - LA countries
  public void doSolveClientTierLogicOnSave(Data data, String issuingCntry) {
    LOG.debug("doSolveClientTierLogicOnSave : start processing. . .");
    LOG.debug("issuing country :" + issuingCntry);

    String mrcCode = data.getMrcCd();
    String isuCode = data.getIsuCd();
    CmrClientService cmrClientService = new CmrClientService();

    if (!StringUtils.isEmpty(mrcCode) && !StringUtils.isEmpty(isuCode)) {
      String retrievedClientTierCd = (String) cmrClientService.getClientTierCode(mrcCode, isuCode);
      LOG.debug(">>> retrievedClientTierCd >> " + retrievedClientTierCd);
      if (!StringUtils.isEmpty(retrievedClientTierCd)) {
        data.setClientTier(retrievedClientTierCd);
      } else {
        LOG.debug("retrievedClientTierCd is blank. will set as blank.");
        data.setClientTier("");
      }
    }
  }

  private String getStateProvCd(String issuingCntry, String state, String city) {
    LOG.debug("Get StateProv/Regio value...");
    EntityManager entityManager = JpaManager.getEntityManager();
    String txt = "";
    String sql = ExternalizedQuery.getSql("GET.LOV.CD_BY_TXT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", issuingCntry);
    query.setParameter("FIELD_ID", "##StateProv");
    query.setParameter("TXT", state);
    List<String> results = query.getResults(String.class);

    if (results != null && results.size() == 1) {
      txt = results.get(0);
    } else {
      txt = getStateProvByCity(entityManager, issuingCntry, results, city);
    }

    LOG.debug("Lov txt : " + txt);
    return txt;
  }

  private String getLovCdByUpperTxt(String issuingCntry, String fieldId, String txt) {
    EntityManager entityManager = JpaManager.getEntityManager();

    String lovTxt = "";
    String sql = ExternalizedQuery.getSql("GET.LOV.CD_BY_UPPER_TXT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", issuingCntry);
    query.setParameter("FIELD_ID", fieldId);
    query.setParameter("TXT", txt);
    List<String> results = query.getResults(String.class);

    if (results != null && results.size() > 0) {
      lovTxt = results.get(0);
    }

    return lovTxt;
  }

  private String getStateProvByCity(EntityManager entityManager, String issuingCntry, List<String> stateProvResults, String city) {
    String txt = "";
    String sql = ExternalizedQuery.getSql("GET.GEO_CITIES.ID_BY_DESC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CITY_DESC", city);
    query.setParameter("CMR_ISSUING_CNTRY", issuingCntry);
    List<String> results = query.getResults(String.class);

    // blank state/prov - determine state/prov value via cityId first 2 char
    if (stateProvResults.isEmpty() && !results.isEmpty()) {
      String cityId = results.get(0);
      if (StringUtils.isNotBlank(cityId) && cityId.length() >= 2) {
        txt = cityId.substring(0, 2);
      }
    } else if (!stateProvResults.isEmpty() && !results.isEmpty()) {
      // multiple state prov results - determine which state/prov is correct via
      // cityId
      for (String res : results) {
        if (StringUtils.isNotBlank(res) && res.length() >= 2) {
          String cityId = res.substring(0, 2);
          if (stateProvResults.contains(cityId)) {
            txt = cityId;
            break;
          }
        }
      }
    }
    return txt;
  }

  private String getLocationCd(String issuingCntry, String city, String stateProv) {
    EntityManager entityManager = JpaManager.getEntityManager();
    String txt = "";
    String sql = ExternalizedQuery.getSql("GET.GEO_CITIES.ID_BY_DESC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CITY_DESC", city);
    query.setParameter("CMR_ISSUING_CNTRY", issuingCntry);
    List<String> results = query.getResults(String.class);

    if (results != null && results.size() == 1) {
      txt = results.get(0);
    } else if (results != null && results.size() > 1) {
      for (String res : results) {
        if (res.startsWith(stateProv)) {
          txt = res;
        }
      }
    }

    if (txt != null && StringUtils.isNotEmpty(txt) && txt.length() >= 5) {
      txt = txt.substring(2, 5);
    }

    return txt;
  }

  public static boolean isClearDPL(AddressModel model, Addr addr, EntityManager entityManager) {

    String aAddrTxt = addr.getAddrTxt() != null ? addr.getAddrTxt().trim().toLowerCase() : "";
    String aAddrTxt2 = addr.getAddrTxt2() != null ? addr.getAddrTxt2().trim().toLowerCase() : "";
    String aDivision = addr.getDivn() != null ? addr.getDivn().trim().toLowerCase() : "";
    String aCity1 = addr.getCity1() != null ? addr.getCity1().trim().toLowerCase() : "";
    String aCity2 = addr.getCity2() != null ? addr.getCity2().trim().toLowerCase() : "";
    String aStateProv = addr.getStateProv() != null ? addr.getStateProv().trim().toLowerCase() : "";
    String aTaxCd1 = addr.getTaxCd1() != null ? addr.getTaxCd1().trim().toLowerCase() : "";
    String aTaxCd2 = addr.getTaxCd2() != null ? addr.getTaxCd2().trim().toLowerCase() : "";
    String aPostCd = addr.getPostCd() != null ? addr.getPostCd().trim().toLowerCase() : "";
    String aVat = addr.getVat() != null ? addr.getVat().trim().toLowerCase() : "";
    String aTransportZone = addr.getTransportZone() != null ? addr.getTransportZone().trim().toLowerCase() : "";

    String mAddrTxt = model.getAddrTxt() != null ? model.getAddrTxt().trim().toLowerCase() : "";
    String mAddrTxt2 = model.getAddrTxt2() != null ? model.getAddrTxt2().trim().toLowerCase() : "";
    String mDivision = model.getDivn() != null ? model.getDivn().trim().toLowerCase() : "";
    String mCity1 = model.getCity1() != null ? model.getCity1().trim().toLowerCase() : "";
    String mCity2 = model.getCity2() != null ? model.getCity2().trim().toLowerCase() : "";
    String mStateProv = model.getStateProv() != null ? model.getStateProv().trim().toLowerCase() : "";
    String mTaxCd1 = model.getTaxCd1() != null ? model.getTaxCd1().trim().toLowerCase() : "";
    String mTaxCd2 = model.getTaxCd2() != null ? model.getTaxCd2().trim().toLowerCase() : "";
    String mPostCd = model.getPostCd() != null ? model.getPostCd().trim().toLowerCase() : "";
    String mVat = model.getVat() != null ? model.getVat().trim().toLowerCase() : "";
    String mTransportZone = model.getTransportZone() != null ? model.getTransportZone().trim().toLowerCase() : "";

    // DECIDE which logic to use based on the LA country
    if (isBRIssuingCountry(model.getCmrIssuingCntry())) { // is BR
      if (!StringUtils.equals(aAddrTxt, mAddrTxt) || !StringUtils.equals(aAddrTxt2, mAddrTxt2) || !StringUtils.equals(aCity1, mCity1)
          || !StringUtils.equals(aDivision, mDivision) || !StringUtils.equals(aStateProv, mStateProv) || !StringUtils.equals(aTaxCd1, mTaxCd1)
          || !StringUtils.equals(aTaxCd2, mTaxCd2) || !StringUtils.equals(aVat, mVat) || !StringUtils.equals(aPostCd, mPostCd)
          || !StringUtils.equals(aTransportZone, mTransportZone)) {
        return true;
      }
    } else if (isMXIssuingCountry(model.getCmrIssuingCntry())) {// is MX
      if (!StringUtils.equals(aAddrTxt, mAddrTxt) || !StringUtils.equals(aAddrTxt2, mAddrTxt2) || !StringUtils.equals(aDivision, mDivision)
          || !StringUtils.equals(aCity1, mCity1) || !StringUtils.equals(aStateProv, mStateProv) || !StringUtils.equals(aPostCd, mPostCd)
          || !StringUtils.equals(aTransportZone, mTransportZone)) {
        return true;
      }
    } else if (isSSAIssuingCountry(model.getCmrIssuingCntry())) { // is SSA
      if (!StringUtils.equals(aAddrTxt, mAddrTxt) || !StringUtils.equals(aAddrTxt2, mAddrTxt2) || !StringUtils.equals(aDivision, mDivision)
          || !StringUtils.equals(aCity1, mCity1) || !StringUtils.equals(aStateProv, mStateProv) || !StringUtils.equals(aPostCd, mPostCd)
          || !StringUtils.equals(aTransportZone, mTransportZone) || !StringUtils.equals(aCity2, mCity2)) {
        return true;
      }
    } else {
      return false;

    }
    return false;

  }

  public void recomputeDPLResult(Admin admin, EntityManager entityManager, long reqId) {
    AddressService aService = new AddressService();
    Scorecard scorecard = aService.getScorecardRecord(entityManager, reqId);

    if (scorecard == null) {
      return;
    }

    LOG.debug("Recomputing DPL Results for Request ID " + reqId);
    String sql = ExternalizedQuery.getSql("DPL.GETDPLCOUNTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      int all = 0;
      int passed = 0;
      int failed = 0;
      int notdone = 0;
      int notrequired = 0;
      for (Object[] record : results) {
        if ("ALL".equals(record[0])) {
          all = Integer.parseInt(record[1].toString());
        } else if ("PASSED".equals(record[0])) {
          passed = Integer.parseInt(record[1].toString());
        } else if ("FAILED".equals(record[0])) {
          failed = Integer.parseInt(record[1].toString());
        } else if ("NOTDONE".equals(record[0])) {
          notdone = Integer.parseInt(record[1].toString());
        } else if ("NOTREQUIRED".equals(record[0])) {
          notrequired = Integer.parseInt(record[1].toString());
        }
      }

      if (all == notrequired) {
        scorecard.setDplChkResult("NR");
        // not required
      } else if (all == passed + notrequired) {
        scorecard.setDplChkResult("AP");
        // all passed
      } else if (all == failed + notrequired) {
        // all failed
        scorecard.setDplChkResult("AF");
      } else if (passed > 0 && all != passed) {
        // some passed, some failed/not done
        scorecard.setDplChkResult("SF");
      }

      // if there is at least one Not done, set to not done
      if (notdone > 0) {
        scorecard.setDplChkResult("Not Done");
      }
      if (notdone != all) {
        // update if DPL has indeed been performed
        scorecard.setDplChkTs(SystemUtil.getCurrentTimestamp());
        scorecard.setDplChkUsrId(admin.getLockBy());
        scorecard.setDplChkUsrNm(admin.getLockByNm());
      }
      LOG.debug(" - DPL Status for Request ID " + reqId + " : " + scorecard.getDplChkResult());
      entityManager.merge(scorecard);
    }
  }

  // MK: Defect 1641291: BR: Name 4 is not being imported into the request
  /*
   * @Override public void
   * addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String
   * cmrCountry, String addrTypeDesc, String sapNumber, UpdatedAddr addr,
   * List<UpdatedNameAddrModel> results, EntityManager entityManager) { if
   * (SystemLocation.BRAZIL.equals(cmrCountry) && !equals(addr.getCustNm4Old(),
   * addr.getCustNm4())) { UpdatedNameAddrModel update = new
   * UpdatedNameAddrModel(); update.setAddrType(addrTypeDesc);
   * update.setSapNumber(sapNumber);
   * update.setDataField(PageManager.getLabel(cmrCountry, "DistrictCd", "-"));
   * update.setNewData(addr.getCustNm4());
   * update.setOldData(addr.getCustNm4Old()); results.add(update);
   * 
   * } }
   */

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

  public static String getProxiLocnDesc(EntityManager entityManager, String cmrIssuingCntry, String proxiLocnNo) {
    String sql = ExternalizedQuery.getSql("GET.LA.PROXI_LOCN_DESC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CD", proxiLocnNo);

    if (cmrIssuingCntry != null && cmrIssuingCntry.length() > 3) {
      String tempKatr6 = cmrIssuingCntry.substring(0, 3);
      LOG.debug("Executing on PDF... getting KATR6 from what is on the value >> " + tempKatr6);
      cmrIssuingCntry = tempKatr6;
    }

    query.setParameter("KATR6", cmrIssuingCntry);
    query.setForReadOnly(true);
    String ret = query.getSingleResult(String.class);
    return ret;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    // CREATCMR-531
    map.put("##IERPSitePrtyId", "ierpSitePrtyId");
    map.put("##OriginatorName", "originatorNm");
    map.put("##OldMainCustomerName1", "oldCustNm1");
    map.put("##MainCustomerName2", "mainCustNm2");
    map.put("##OldMainCustomerName2", "oldCustNm2");
    map.put("##CollBranchOff", "collBoId");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##INACType", "inacType");
    map.put("##ISU", "isuCd");
    map.put("##POBoxCity", "poBoxCity");
    map.put("##ICMSContribution", "icmsInd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##POBoxPostalCode", "poBoxPostCd");
    map.put("##GovernmentType", "govType");
    map.put("##PPSCEID", "ppsceid");
    map.put("##ContactSeqNumber", "contactSeqNum");
    map.put("##CustLang", "custPrefLang");
    map.put("##ContactType", "contactType");
    map.put("##LocalTax2", "taxCd2");
    map.put("##LocalTax3", "taxCd3");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
    map.put("##BPRelationType", "bpRelType");
    map.put("##LocalTax1", "taxCd1");
    map.put("##MainCustomerName1", "mainCustNm1");
    map.put("##CAP", "capInd");
    map.put("##RequestReason", "reqReason");
    map.put("##CustomerType", "custType");
    map.put("##POBox", "poBox");
    map.put("##MrktChannelInd", "mrktChannelInd");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##CustPhone", "custPhone");
    map.put("##Floor", "floor");
    map.put("##TaxCd", "taxCd");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##Email", "contactEmail");
    map.put("##StateProv", "stateProv");
    map.put("##ContractPrintIndc", "contractPrintIndc");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##VAT", "vat");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Office", "office");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##Phone", "contactPhone");
    map.put("##Email1", "email1");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##Affiliate", "affiliate");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##ClientTier", "clientTier");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    map.put("##ProxiLocationNumber", "proxiLocnNo");
    map.put("##DropDownCity", "city1");
    map.put("##SAPNumber", "sapNo");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Company", "company");
    map.put("##BillingPrintIndc", "billingPrintIndc");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##CollectorNameNo", "collectorNameNo");
    map.put("##CustFAX", "custFax");
    map.put("##MrcCd", "mrcCd");
    map.put("##ISIC", "isicCd");
    map.put("##ContactName", "contactName");
    map.put("##TaxSeparationIndc", "taxSeparationIndc");
    map.put("##CountryUse", "cntryUse");
    map.put("##Enterprise", "enterprise");
    map.put("##PostalCode", "postCd");
    map.put("##County", "county");
    map.put("##TransportZone", "transportZone");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##BillingName", "mexicoBillingName");
    map.put("##IBMBankNumber", "ibmBankNumber");
    return map;
  }

  @Override
  public BaseV2RequestModel getAutomationRequestModel(String country, String reqType) {
    switch (country) {
    case SystemLocation.BRAZIL:
      return new BrazilV2ReqModel();
    }
    return null;
  }

  @Override
  public void alterModelBeforeSave(RequestEntryModel model) {
    switch (model.getCmrIssuingCntry()) {
    case SystemLocation.BRAZIL:
      model.setVat(null);
      break;
    }
  }

  @Override
  public void saveV2Entries(EntityManager entityManager, RequestEntryModel model, HttpServletRequest request, Admin admin, Data data, Addr soldTo)
      throws Exception {
    switch (model.getCmrIssuingCntry()) {
    case SystemLocation.BRAZIL:
      handleBrazilV2Request(entityManager, model, request, admin, data, soldTo);
      return;
    }
  }

  /**
   * Handle saving of VAT and new address for leasing scenario
   * 
   * @param entityManager
   * @param model
   * @param request
   * @param admin
   * @param data
   * @param soldTo
   * @throws Exception
   */

  private void handleBrazilV2Request(EntityManager entityManager, RequestEntryModel model, HttpServletRequest request, Admin admin, Data data,
      Addr addr) throws Exception {
    long reqId = model.getReqId();
    List<Addr> addrList = Arrays.asList(addr);
    LOG.debug("Handling Brazil specific entries for V2 Request " + reqId);
    BrazilV2ReqModel v2Model = BaseV2RequestModel.createFromRequest(model, request, BrazilV2ReqModel.class);

    LOG.debug("Setting Gov Type to : " + v2Model.getGovType());
    data.setGovType(v2Model.getGovType());
    LOG.debug("Setting VAT in DATA table to : " + v2Model.getVat());
    data.setVat(v2Model.getVat());

    if ("ZS01".equalsIgnoreCase(addr.getId().getAddrType())) {
      addr.setVat(v2Model.getVat());
      addr.setTaxCd2(v2Model.getMunicipalFiscalCode());
      if ("C".equals(v2Model.getReqType())) {
        addr.setTransportZone("Z000000001");
      }

      if (StringUtils.isBlank(addr.getLandCntry())) {
        addr.setLandCntry("BR");
      }

      // set default values for creates
      if ("C".equals(v2Model.getReqType())) {
        if (StringUtils.isBlank(addr.getImportInd())) {
          addr.setImportInd("N");
        }
        if (StringUtils.isNotBlank(data.getCustSubGrp()) && !data.getCustSubGrp().equals("CROSS")) {
          setCustScenarioValues(entityManager, admin, data);
        }
      }

      // create another address record as ZI01 for leasing scenario
      if (("C".equals(v2Model.getReqType()) && "LEASI".equalsIgnoreCase(model.getCustSubGrp()))
          || ("U".equals(v2Model.getReqType()) && "LEASI".equalsIgnoreCase(getScenarioTypeForUpdateBRV2(entityManager, data, addrList)))) {
        LOG.debug("Creating ZI01 address for Request " + reqId);
        AddrPK pkzi01 = new AddrPK();
        pkzi01.setReqId(reqId);
        pkzi01.setAddrType("ZI01");

        String seqzi01 = null;
        seqzi01 = generateAddrSeq(entityManager, "ZI01", reqId, model.getCmrIssuingCntry());
        pkzi01.setAddrSeq(seqzi01 != null ? seqzi01 : "1");

        Addr addrzi01 = new Addr();
        PropertyUtils.copyProperties(addrzi01, v2Model);
        addrzi01.setDplChkResult(null);
        addrzi01.setId(pkzi01);
        addrzi01.setVat(v2Model.getVatEndUser());
        addrzi01.setTaxCd2(v2Model.getMunicipalFiscalCodeEndUser());
        if ("C".equals(v2Model.getReqType())) {
          addrzi01.setTransportZone("Z000000001");
        }

        if (StringUtils.isBlank(addrzi01.getImportInd())) {
          addr.setImportInd("N");
        }

        if (StringUtils.isBlank(addrzi01.getLandCntry())) {
          addrzi01.setLandCntry("BR");
        }

        entityManager.persist(addrzi01);
        LOG.debug("ZI01 address for req id= " + reqId + " successfully created.");

      }

      // CreatCMR-6681 - BR Predefined enterprise value for local scenarios
      if ("C".equals(v2Model.getReqType())) {
        if ("LOCAL".equals(data.getCustGrp())
            && !(CmrConstants.CUST_TYPE_PRIPE.equals(data.getCustSubGrp()) || CmrConstants.CUST_TYPE_IBMEM.equals(data.getCustSubGrp()))
            && StringUtils.isNotBlank(v2Model.getVat())) {
          if (v2Model.getVat().length() >= 8) {
            LOG.debug("Setting ENTERPRISE in DATA table to : " + v2Model.getVat().substring(0, 8));
            data.setEnterprise(v2Model.getVat().substring(0, 8));
          }
        }
      }
    }

    // handling geoContactInfo here
    handleGeoContactDetails(data, admin, entityManager);

    // handle tax code
    if (!StringUtils.isBlank(v2Model.getTaxCode())) {
      GeoTaxInfo taxInfo = new GeoTaxInfo();
      GeoTaxInfoPK taxInfoPk = new GeoTaxInfoPK();
      taxInfoPk.setReqId(reqId);
      taxInfoPk.setGeoTaxInfoId(1);
      taxInfo.setId(taxInfoPk);

      taxInfo.setTaxCd("30");
      taxInfo.setTaxSeparationIndc(v2Model.getTaxCode());
      taxInfo.setCreateById(admin.getRequesterId());
      taxInfo.setCreateTs(SystemUtil.getCurrentTimestamp());
      taxInfo.setUpdtById(admin.getRequesterId());
      taxInfo.setUpdtTs(SystemUtil.getCurrentTimestamp());

      LOG.debug("Creating Tax Info for Request " + reqId);
      entityManager.persist(taxInfo);
    }

    // merge entities
    entityManager.merge(data);
    entityManager.merge(addr);
    entityManager.merge(admin);

    entityManager.flush();

  }

  /**
   * sets values of custType, crosTyp and crosSubTyp based on the CUST_SCENARIO
   * table
   * 
   * @param entityManager
   * @param admin
   * @param data
   */
  public void setCustScenarioValues(EntityManager entityManager, Admin admin, Data data) {
    PreparedQuery query = null;
    try {
      LOG.debug("In setCustScenarioValues method");
      String sql = ExternalizedQuery.getSql("BR.AUTO.GET_CUST_SCENARIO_FIELDS");

      // get custType
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("FIELD_NAME", "custType");
      query.setParameter("CUST_TYPE", data.getCustSubGrp());
      String custType = query.getSingleResult(String.class);
      LOG.debug("Setting value of custType= " + custType);
      admin.setCustType(custType);

      // get crosTyp
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("FIELD_NAME", "crosTyp");
      query.setParameter("CUST_TYPE", data.getCustSubGrp());
      String crosTyp = query.getSingleResult(String.class);
      LOG.debug("Setting value of crosType= " + crosTyp);
      data.setCrosTyp(crosTyp);

      // get crosSubType
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("Field_NAME", "crosSubTyp");
      query.setParameter("CUST_TYPE", data.getCustSubGrp());
      String crosSubTyp = query.getSingleResult(String.class);
      LOG.debug("Setting value of crosSubType= " + crosSubTyp);
      data.setCrosSubTyp(crosSubTyp);

    } catch (Exception e) {
      LOG.error(e);
    }
  }

  /**
   * Identifies & returns scenario type code for update requests
   * 
   * @param entityManager
   * @param data
   * @param addresses
   *          on request
   */
  public static String getScenarioTypeForUpdateBRV2(EntityManager entityManager, Data data, List<Addr> addresses) {
    LOG.debug("inside getScenarioTypeForUpdateBRV2 ");
    String custClass = data.getCustClass();
    String scenarioType = null;
    if (data.getAbbrevNm() != null && StringUtils.isNotEmpty(data.getAbbrevNm())
        && ("SOFTLAYER USE ONLY".equalsIgnoreCase(data.getAbbrevNm()) || data.getAbbrevNm().contains("SOFTLAYER"))) {
      scenarioType = "SOFTL";
    } else if (data.getAbbrevNm() != null && StringUtils.isNotEmpty(data.getAbbrevNm())
        && (data.getAbbrevNm().contains("CC3") || data.getAbbrevNm().endsWith("CC3 USE ONLY"))) {
      scenarioType = "CC3CC";
    } else if (addresses != null && !addresses.isEmpty()) {
      for (Addr addr : addresses) {
        if ("ZS01".equals(addr.getId().getAddrType()) && addr.getLandCntry() != null && StringUtils.isNotEmpty(addr.getLandCntry())
            && !"BR".equals(addr.getLandCntry())) {
          scenarioType = "CROSS";
        }
      }
    }
    if ((scenarioType == null || StringUtils.isEmpty(scenarioType)) && custClass != null && !"".equals(custClass)) {
      switch (custClass) {

      case "33":
        scenarioType = "LEASI";
        break;

      case "34":
        scenarioType = "LEASI";
        break;

      case "35":
        scenarioType = "LEASI";
        break;

      case "12":
        scenarioType = "GOVDI";
        break;

      case "13":
        scenarioType = "GOVDI";
        break;

      case "45":
        scenarioType = "BUSPR";
        break;

      case "81":
        scenarioType = "INTER";
        break;

      default:
        scenarioType = "COMME";
        break;
      }
    }
    LOG.debug("scenario type=  " + scenarioType);
    return scenarioType;
  }

  /**
   * Returns scenario type description based on the scenario code
   * 
   * @param scenario
   *          type code
   */
  public static String getScenarioDescBR(String code) {
    String desc = null;
    switch (code) {
    case "CROSS":
      desc = "Cross-Border";
      break;

    case "SOFTL":
      desc = "Softlayer";
      break;

    case "5COMP":
      desc = "SaaS/PaaS with clip level < $5 - Company";
      break;

    case "5PRIP":
      desc = "SaaS/PaaS with clip level < $5 - Private Person";
      break;

    case "IBMEM":
      desc = "IBM Employee";
      break;

    case "PRIPE":
      desc = "Private Person";
      break;

    default:
      desc = "Other";
      break;
    }

    return desc;
  }

  private void handleGeoContactDetails(Data data, Admin admin, EntityManager entityManager) {
    List<String> emailsCont = new ArrayList<>();
    String[] contTypes = { "EM", "LE", "CF" };
    emailsCont.add(data.getEmail1());
    emailsCont.add(data.getEmail2());
    emailsCont.add(data.getEmail3());

    if ("CC3CC".equalsIgnoreCase(data.getCustSubGrp())) {
      int seqC3 = 1;
      for (String email : emailsCont) {
        int index = emailsCont.indexOf(email);
        for (int i = 0; i < contTypes.length; i++) {
          if ((index == 0 && contTypes[i] == "LE") || (index == 2 && (contTypes[i] == "LE" || contTypes[i] == "CF")) || email == null)
            continue;
          GeoContactInfo e = new GeoContactInfo();
          GeoContactInfoPK ePk = new GeoContactInfoPK();
          e.setContactEmail(email);
          if (index == 0 && contTypes[i] == "CF") {
            e.setContactType("C3");
          } else {
            e.setContactType(contTypes[i]);
          }

          if (index == 1 && (contTypes[i] == "LE" || contTypes[i] == "CF")) {
            e.setContactSeqNum("00" + Integer.toString(seqC3 - 1));
          } else {
            e.setContactSeqNum("00" + Integer.toString(seqC3));
          }
          e.setCreateById(admin.getRequesterId());
          e.setCreateTs(SystemUtil.getCurrentTimestamp());
          try {
            int contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(admin.getId().getReqId()));
            ePk.setContactInfoId(contactId);
          } catch (CmrException ex) {
            LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
          }
          ePk.setReqId(admin.getId().getReqId());
          e.setId(ePk);
          entityManager.persist(e);
          entityManager.flush();
        }
        seqC3++;
      }
    }

    else {
      int seq = 1;
      for (String email : emailsCont) {
        int index = emailsCont.indexOf(email);
        for (int i = 0; i < contTypes.length; i++) {
          if ((index > 0 && i > 0) || email == null)
            continue;
          GeoContactInfo e = new GeoContactInfo();
          GeoContactInfoPK ePk = new GeoContactInfoPK();
          e.setContactName("N");
          e.setContactEmail(email);
          e.setContactType(contTypes[i]);
          e.setContactSeqNum("00" + Integer.toString(seq));
          e.setCreateById(admin.getRequesterId());
          e.setCreateTs(SystemUtil.getCurrentTimestamp());
          try {
            int contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(admin.getId().getReqId()));
            ePk.setContactInfoId(contactId);
          } catch (CmrException ex) {
            LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
          }
          ePk.setReqId(admin.getId().getReqId());
          e.setId(ePk);
          entityManager.persist(e);
          entityManager.flush();
        }
        seq++;
      }
    }
  }

  @Override
  public BaseV2RequestModel recreateModelFromRequest(EntityManager entityManager, RequestData requestData) {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    BrazilV2ReqModel model = new BrazilV2ReqModel();
    try {
      PropertyUtils.copyProperties(model, admin);
      PropertyUtils.copyProperties(model, data);
    } catch (Exception e) {
      LOG.warn("Cannot copy values to V2 model");
    }
    Addr soldTo = requestData.getAddress("ZS01");
    if (soldTo != null) {
      model.setVat(soldTo.getVat());
      model.setMunicipalFiscalCode(soldTo.getTaxCd2());
    }
    Addr installAt = requestData.getAddress("ZI01");
    if (installAt != null) {
      model.setVatEndUser(installAt.getVat());
      model.setMunicipalFiscalCodeEndUser(installAt.getTaxCd2());
    }

    LOG.debug("Getting contact info from V2 inputs..");
    String sql = ExternalizedQuery.getSql("BR.GET_DISTINCT_MAILS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    List<GeoContactInfo> contacts = query.getResults(GeoContactInfo.class);
    List<String> emails = new ArrayList<String>();
    if (contacts != null) {
      for (GeoContactInfo contact : contacts) {
        if (!StringUtils.isBlank(contact.getContactEmail()) && !emails.contains(contact.getContactEmail().trim().toUpperCase())) {
          emails.add(contact.getContactEmail().trim().toUpperCase());
        }
      }
    }
    if (emails.size() > 0) {
      model.setEmail1(emails.get(0));
    }
    if (emails.size() > 1) {
      model.setEmail2(emails.get(1));
    }
    if (emails.size() > 2) {
      model.setEmail3(emails.get(2));
    }

    LOG.debug("Getting tax info from V2 inputs..");
    sql = ExternalizedQuery.getSql("BR.GET_TAX_CD");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    List<GeoTaxInfo> taxInfo = query.getResults(1, GeoTaxInfo.class);
    if (taxInfo != null && !taxInfo.isEmpty()) {
      String taxCd = taxInfo.get(0).getTaxSeparationIndc();
      if (!StringUtils.isBlank(taxCd)) {
        model.setTaxCode(taxCd);
      }
    }

    LOG.debug(model);

    return model;
  };

  @Override
  public void doAfterImportCMRFromAutomation(EntityManager entityManager, BaseV2RequestModel model, RequestData requestData) {
    // locate the data on the request then overwrite
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();

    if (StringUtils.isBlank(data.getCountryUse()) && StringUtils.isNotBlank(data.getMrcCd())) {
      LOG.debug("Country Use found blank on import. Setting value equals to MRC code = " + data.getMrcCd());
      data.setCountryUse(data.getMrcCd());
    }

    Addr soldTo = requestData.getAddress("ZS01");
    BrazilV2ReqModel brModel = (BrazilV2ReqModel) model;

    soldTo.setTaxCd2(brModel.getMunicipalFiscalCode());
    soldTo.setVat(brModel.getVat());

    String sqlKey = ExternalizedQuery.getSql("BR.GET_INSTALL_AT");
    PreparedQuery q = new PreparedQuery(entityManager, sqlKey);
    q.setParameter("REQID", admin.getId().getReqId());
    Addr installAt = q.getSingleResult(Addr.class);

    LOG.debug("Req Id: " + admin.getId().getReqId() + ", ZI01 address after import: " + installAt);
    if (StringUtils.isNotBlank(brModel.getVatEndUser()) && installAt != null) {

      installAt.setVat(brModel.getVatEndUser());
      installAt.setTaxCd2(brModel.getMunicipalFiscalCodeEndUser());
      entityManager.merge(installAt);
      // Add ZI01 to request
      requestData.getAddresses().add(installAt);

    } else {
      LOG.error("ZI01 address not found on request.");
    }

    if (brModel.getProxiLocnNo() != null) {
      requestData.getData().setProxiLocnNo(brModel.getProxiLocnNo());
    }

    LOG.debug("Getting contact info from V2 inputs..");
    String sql = ExternalizedQuery.getSql("BR.GET_DISTINCT_MAILS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    List<GeoContactInfo> contacts = query.getResults(GeoContactInfo.class);
    int count = 0;
    if (contacts != null) {
      for (GeoContactInfo contact : contacts) {
        if (StringUtils.isBlank(contact.getContactName())) {
          contact.setContactName("N");
          entityManager.merge(contact);
        }
        switch (contact.getContactSeqNum()) {
        case "001":
          if (StringUtils.isNotEmpty(brModel.getEmail1())) {
            if (!data.getAbbrevNm().endsWith("/CC3") && !data.getAbbrevNm().endsWith("/CC3 USE ONLY")) {
              if ("EM".equals(contact.getContactType()) || "LE".equals(contact.getContactType()) || "CF".equals(contact.getContactType())) {
                if (count < 1) {
                  count = 1;
                }
                contact.setContactEmail(brModel.getEmail1());
                entityManager.merge(contact);
              }
            } else {
              if ("LE".equals(contact.getContactType()) || "CF".equals(contact.getContactType())) {
                if (count < 1) {
                  count = 1;
                }
                contact.setContactEmail(brModel.getEmail2());
                entityManager.merge(contact);
              }
            }
          }
          break;
        case "002":
          if (StringUtils.isNotEmpty(brModel.getEmail1()) && "EM".equals(contact.getContactType())) {
            if (count < 2) {
              count = 2;
            }
            if (StringUtils.isNotBlank(contact.getContactEmail()) && StringUtils.isBlank(brModel.getEmail2())) {
              GeoContactInfo merged = entityManager.merge(contact);
              if (merged != null) {
                entityManager.remove(merged);
              }
            } else {
              contact.setContactEmail(brModel.getEmail2());
              entityManager.merge(contact);
            }
          }
          break;
        case "003":
          if (StringUtils.isNotEmpty(brModel.getEmail1()) && "EM".equals(contact.getContactType())) {
            if (count < 3) {
              count = 3;
            }
            if (StringUtils.isNotBlank(contact.getContactEmail()) && StringUtils.isBlank(brModel.getEmail2())) {
              GeoContactInfo merged = entityManager.merge(contact);
              if (merged != null) {
                entityManager.remove(merged);
              }
            } else {
              contact.setContactEmail(brModel.getEmail3());
              entityManager.merge(contact);
            }
          }
          break;
        default:
          break;
        }
        entityManager.flush();
      }
    }

    // create new contacts if contacts available on the model but not on request
    if (count <= 1 && StringUtils.isNotBlank(brModel.getEmail2())) {
      String email = brModel.getEmail2();
      createNewEmailContact(entityManager, data, admin, email, "EM", "002");
      if (data.getAbbrevNm().endsWith("/CC3") || data.getAbbrevNm().endsWith("/CC3 USE ONLY")) {
        createNewEmailContact(entityManager, data, admin, email, "LE", "001");
        createNewEmailContact(entityManager, data, admin, email, "CF", "001");
      }
    }

    if (count <= 2 && StringUtils.isNotBlank(brModel.getEmail3())) {
      createNewEmailContact(entityManager, data, admin, brModel.getEmail3(), "EM", "003");
    }

    // set tax Info
    if (StringUtils.isNotBlank(brModel.getTaxCode())) {
      LOG.debug("Getting tax info from V2 inputs..");
      sql = ExternalizedQuery.getSql("BR.GET_TAX_CD");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      GeoTaxInfo taxInfo = query.getSingleResult(GeoTaxInfo.class);
      if (taxInfo != null) {
        taxInfo.setTaxSeparationIndc(brModel.getTaxCode());
        entityManager.merge(taxInfo);
      } else {
        taxInfo = new GeoTaxInfo();
        GeoTaxInfoPK taxInfoPk = new GeoTaxInfoPK();
        taxInfoPk.setReqId(admin.getId().getReqId());
        int taxInfoId = 1;
        try {
          taxInfoId = new TaxInfoService().generateGeoTaxInfoID(entityManager, admin.getId().getReqId());
        } catch (Exception e) {
          LOG.error("Not able to generate new tax info id for Req Id: " + admin.getId().getReqId());
        }
        taxInfoPk.setGeoTaxInfoId(taxInfoId);
        taxInfo.setId(taxInfoPk);

        taxInfo.setTaxCd("30");
        taxInfo.setTaxSeparationIndc(brModel.getTaxCode());
        taxInfo.setCreateById(admin.getRequesterId());
        taxInfo.setCreateTs(SystemUtil.getCurrentTimestamp());
        taxInfo.setUpdtById(admin.getRequesterId());
        taxInfo.setUpdtTs(SystemUtil.getCurrentTimestamp());

        LOG.debug("Creating Tax Info for Request " + admin.getId().getReqId());
        entityManager.persist(taxInfo);
      }
    } else {
      LOG.debug("Getting tax info from V2 inputs for blank Tax code...");
      sql = ExternalizedQuery.getSql("BR.GET_TAX_CD");
      query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      GeoTaxInfo taxInfo = query.getSingleResult(GeoTaxInfo.class);
      if (taxInfo != null) {
        if (StringUtils.isBlank(taxInfo.getTaxSeparationIndc())) {
          LOG.debug("Deleting Tax Info for Request " + admin.getId().getReqId() + " for tax seperation indc null.");
          GeoTaxInfo merged = entityManager.merge(taxInfo);
          if (merged != null) {
            entityManager.remove(merged);
          }
          entityManager.flush();
        }
      }
    }
  }

  public void createNewEmailContact(EntityManager entityManager, Data data, Admin admin, String email, String contactType, String contactSequence) {
    GeoContactInfo e = new GeoContactInfo();
    GeoContactInfoPK ePk = new GeoContactInfoPK();
    e.setContactEmail(email);
    e.setContactName("N");
    e.setContactType(contactType);
    e.setContactSeqNum(contactSequence);
    e.setCreateById(admin.getRequesterId());
    e.setCreateTs(SystemUtil.getCurrentTimestamp());
    try {
      int contactId = new GeoContactInfoService().generateNewContactId(null, entityManager, null, String.valueOf(admin.getId().getReqId()));
      ePk.setContactInfoId(contactId);
    } catch (CmrException ex) {
      LOG.debug("Exception while getting contactId : " + ex.getMessage(), ex);
    }
    ePk.setReqId(admin.getId().getReqId());
    e.setId(ePk);
    entityManager.persist(e);
    entityManager.flush();
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return true;
  }

  private static List<TaxData> getTaxDataByKunnr(EntityManager entityManager, String kunnr) {
    String sql = ExternalizedQuery.getSql("LA.GET_LAINTERIM_TAXDATA_BY_KUNNR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KUNNR", kunnr);

    return query.getResults(TaxData.class);
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String addrSeqNum = null;

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, adminPK);

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      addrSeqNum = getAddrSeqForCreate(entityManager, reqId, addrType, cmrIssuingCntry);
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      addrSeqNum = getAddrSeqForUpdate(entityManager, reqId, addrType, cmrIssuingCntry);
    }
    return addrSeqNum;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    return generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
  }

  private String getAddrSeqForCreate(EntityManager entityManager, long reqId, String addrType, String cmrIssuingCntry) {
    int startingSeqNum = 0;
    String addrSeqNum = null;
    if (!StringUtils.isEmpty(addrType)) {

      if (SystemLocation.BRAZIL.equals(cmrIssuingCntry)) {
        startingSeqNum = getFixedStartingSeqNewAddrBR(addrType);
      } else {
        startingSeqNum = getFixedStartingSeqNewAddr(addrType);
      }

      addrSeqNum = getAvailableAddrSeqNum(entityManager, reqId, startingSeqNum);
    }

    return addrSeqNum;
  }

  private String getAddrSeqForUpdate(EntityManager entityManager, long reqId, String addrType, String cmrIssuingCntry) {
    int startingSeqNum = 0;
    int maxSeqNum = 0;
    if (SystemLocation.BRAZIL.equals(cmrIssuingCntry)) {
      startingSeqNum = getFixedStartingSeqNewAddrBR(addrType);
      maxSeqNum = getMaxSeqBR(addrType);
    } else {
      startingSeqNum = getFixedStartingSeqNewAddr(addrType);
      maxSeqNum = getMaxSeq(addrType);
    }
    return getAvailAddrSeqNumInclRdc(entityManager, reqId, startingSeqNum, maxSeqNum);
  }

  private String getAvailAddrSeqNumInclRdc(EntityManager entityManager, long reqId, int startingSeqNum, int maxSeqNum) {
    DataPK pk = new DataPK();
    pk.setReqId(reqId);
    Data data = entityManager.find(Data.class, pk);

    String cmrNo = data.getCmrNo();
    Set<Integer> allAddrSeqFromAddr = getAllSavedSeqFromAddr(entityManager, reqId);
    Set<Integer> allAddrSeqFromRdc = getAllSavedSeqFromRdc(entityManager, cmrNo, data.getCmrIssuingCntry());

    Set<Integer> mergedAddrSet = new HashSet<>();
    mergedAddrSet.addAll(allAddrSeqFromAddr);
    mergedAddrSet.addAll(allAddrSeqFromRdc);

    int availSeqNum = startingSeqNum;
    if (mergedAddrSet.contains(availSeqNum)) {
      while (mergedAddrSet.contains(availSeqNum)) {
        availSeqNum++;
        if (availSeqNum > maxSeqNum) {
          availSeqNum = startingSeqNum;
        }
      }
    }
    return String.valueOf(availSeqNum);
  }

  private int getFixedStartingSeqNewAddrBR(String addrType) {
    int startSeq = 0;
    if ("ZS01".equals(addrType)) {
      startSeq = 100001;
    } else if ("ZP01".equals(addrType)) {
      startSeq = 200001;
    } else if ("ZI01".equals(addrType)) {
      startSeq = 300001;
    } else if ("ZD01".equals(addrType)) {
      startSeq = 400001;
    }
    return startSeq;
  }

  private int getMaxSeqBR(String addrType) {
    int maxSeq = 0;
    if ("ZS01".equals(addrType)) {
      maxSeq = 199999;
    } else if ("ZP01".equals(addrType)) {
      maxSeq = 299999;
    } else if ("ZI01".equals(addrType)) {
      maxSeq = 399999;
    } else if ("ZD01".equals(addrType)) {
      maxSeq = 499999;
    }
    return maxSeq;
  }

  private int getFixedStartingSeqNewAddr(String addrType) {
    int startSeq = 0;
    if ("ZS01".equals(addrType)) {
      startSeq = 10000001;
    } else if ("ZP01".equals(addrType)) {
      startSeq = 20000001;
    } else if ("ZI01".equals(addrType)) {
      startSeq = 30000001;
    } else if ("ZD01".equals(addrType)) {
      startSeq = 40000001;
    }
    return startSeq;
  }

  private int getMaxSeq(String addrType) {
    int maxSeq = 0;
    if ("ZS01".equals(addrType)) {
      maxSeq = 19999999;
    } else if ("ZP01".equals(addrType)) {
      maxSeq = 29999999;
    } else if ("ZI01".equals(addrType)) {
      maxSeq = 39999999;
    } else if ("ZD01".equals(addrType)) {
      maxSeq = 49999999;
    }
    return maxSeq;
  }

  private String getAvailableAddrSeqNum(EntityManager entityManager, long reqId, int startingSeqNum) {
    int availSeqNum = startingSeqNum;
    Set<Integer> allAddrSeq = getAllSavedSeqFromAddr(entityManager, reqId);
    if (allAddrSeq.contains(availSeqNum)) {
      while (allAddrSeq.contains(availSeqNum)) {
        availSeqNum++;
      }
    }
    return String.valueOf(availSeqNum);
  }

  private Set<Integer> getAllSavedSeqFromAddr(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("LA.GET.ADDRSEQ.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Integer> results = query.getResults(Integer.class);

    Set<Integer> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(results);

    return addrSeqSet;
  }

  private Set<Integer> getAllSavedSeqFromRdc(EntityManager entityManager, String cmrNo, String cmrIssuingCntry) {
    String sql = ExternalizedQuery.getSql("GET.KNA1_ZZKV_SEQNO_DISTINCT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<Integer> resultsRDC = query.getResults(Integer.class);
    Set<Integer> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(resultsRDC);

    return addrSeqSet;
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country, Admin admin) {
    LOG.debug("inside LA validateMassUpdateTemplateDupFills handler...");

    XSSFCell currCell = null;
    boolean isDataFilled = false;
    boolean isARFilled = false;
    boolean isEmailFilled = false;
    boolean isTaxInfoFilled = false;
    boolean isSoldToFilled = false;
    boolean isBillToFilled = false;
    boolean isShipToFilled = false;
    boolean isInstallAtFilled = false;
    boolean requesterFromCmdeGsi = false;
    boolean requesterFromAcctReciv = false;
    Map<String, HashSet<String>> mapCmrSeq = new HashMap<String, HashSet<String>>();

    String strRequesterId = admin.getRequesterId().toLowerCase();
    String groups = SystemParameters.getString("LA_CME_GSI_LIST");
    LOG.debug("strRequesterId : " + strRequesterId);
    LOG.debug("groups cmde/gsi : " + strRequesterId);

    requesterFromCmdeGsi = BluePagesHelper.isUserInLaBlueGroup(strRequesterId, groups);
    LOG.debug("requester from cmde/gsi : " + requesterFromCmdeGsi);

    groups = SystemParameters.getString("LA_AR_LIST");
    LOG.debug("groups ar : " + strRequesterId);
    requesterFromAcctReciv = BluePagesHelper.isUserInLaBlueGroup(strRequesterId, groups);
    LOG.debug("requester from acct receivable : " + requesterFromAcctReciv);

    for (String name : LA_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      LOG.debug("validating for sheet " + name);
      if (sheet != null) {
        TemplateValidation error = new TemplateValidation(name);
        for (Row row : sheet) {
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
            String cmrNo = "";
            String abbrevname = "";
            String subindustry = "";
            String isic = "";
            String inac = "";
            String company = "";
            String isu = "";
            String clientTier = "";
            String sbo = "";
            String kukla = "";
            String blockCode = "";
            String vat = "";
            String creditCode = "";
            String codCondition = "";
            String codReason = "";
            String collection = "";
            String collector = "";
            String paymentMode = "";
            String email1 = "";
            String email2 = "";
            String email3 = "";
            String fiscalRegime = "";
            String mexBillingName = "";
            String taxCode = "";
            String taxNumber = "";
            String taxSepIndc = "";
            String billPrintIndc = "";
            String contractPrintIndc = "";
            String countryUse = "";
            String addrNoSeq = "";
            String addrName = "";
            String addrNameCont = "";
            String street = "";
            String streetCont = "";
            String city = "";
            String stateProv = "";
            String postal = "";
            String landed = "";

            if (row.getRowNum() == 2001) {
              continue;
            }

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              abbrevname = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              subindustry = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              isic = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              inac = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              company = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              isu = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              clientTier = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              sbo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              kukla = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(10);
              blockCode = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(11);
              vat = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(12);
              fiscalRegime = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(13);
              mexBillingName = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(abbrevname) || StringUtils.isNotBlank(subindustry) || StringUtils.isNotBlank(isic)
                  || StringUtils.isNotBlank(inac) || StringUtils.isNotBlank(company) || StringUtils.isNotBlank(isu)
                  || StringUtils.isNotBlank(clientTier) || StringUtils.isNotBlank(sbo) || StringUtils.isNotBlank(kukla)
                  || StringUtils.isNotBlank(blockCode) || StringUtils.isNotBlank(vat) || StringUtils.isNotBlank(fiscalRegime)
                  || StringUtils.isNotBlank(mexBillingName)) {
                isDataFilled = true;
              }

              if (!requesterFromCmdeGsi) {
                if (isDataFilled) {
                  LOG.trace("User is not allowed to perform update on this tab.");
                  error.addError(row.getRowNum() + 1, "", "User is not allowed to perform update on this tab." + "<br>");
                }
              }

              if ((isDataFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              } else if (mapCmrSeq.containsKey(cmrNo)) {
                error.addError(row.getRowNum() + 1, "<br>CMR No.", "Duplicate CMR No. It should be entered only once.");
              } else {
                mapCmrSeq.put(cmrNo, new HashSet<String>());
              }

              // Abbreviated Name
              if (isDataFilled && "@".equals(abbrevname)) {
                error.addError((row.getRowNum() + 1), "<br>Abbreviated Name", "@ value for Abbreviated Name is not allowed.");
              }

              // INAC/NAC
              if (isDataFilled && "@@@@".equals(inac)) {
                error.addError((row.getRowNum() + 1), "<br>INAC/NAC", "@@@@ value for INAC/NAC is not allowed.");
              }

              // Company
              if (isDataFilled && "@".equals(company)) {
                error.addError((row.getRowNum() + 1), "<br>Company", "@ value for Company is not allowed.");
              }

              // Client Tier
              if (isDataFilled && "@".equals(clientTier)) {
                error.addError((row.getRowNum() + 1), "<br>Client Tier", "@ value for Client Tier is not allowed.");
              }

              // SBO
              if (isDataFilled && "@".equals(sbo)) {
                error.addError((row.getRowNum() + 1), "<br>SBO", "@ value for SBO is not allowed.");
              }

              // Kukla
              if (isDataFilled && "@".equals(kukla)) {
                error.addError((row.getRowNum() + 1), "<br>Kukla", "@ value for Kukla is not allowed.");
              }

              // VAT
              if (isDataFilled && "@".equals(vat)) {
                error.addError((row.getRowNum() + 1), "<br>VAT", "@ value for VAT is not allowed.");
              }

              // Tax Regime
              if (isDataFilled && "@".equals(fiscalRegime)) {
                error.addError((row.getRowNum() + 1), "<br>Tax Regime", "@ value for Tax Regime is not allowed.");
              }

              // Mexico Billing Name
              if (isDataFilled && "@".equals(mexBillingName)) {
                error.addError((row.getRowNum() + 1), "<br>Mexico Billing Name", "@ value for Mexico Billing Name is not allowed.");
              }
            }

            if ("AccountReceivable".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              creditCode = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              codCondition = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              codReason = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              collection = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              collector = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              paymentMode = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(creditCode) || StringUtils.isNotBlank(codCondition) || StringUtils.isNotBlank(codReason)
                  || StringUtils.isNotBlank(collection) || StringUtils.isNotBlank(collector) || StringUtils.isNotBlank(paymentMode)) {
                isARFilled = true;
              }

              if (!requesterFromAcctReciv) {
                if (isARFilled) {
                  LOG.trace("User is not allowed to perform update on this tab.");
                  error.addError(row.getRowNum() + 1, "", "User is not allowed to perform update on this tab." + "<br>");
                }
              }

              if ((isARFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              }

              // COD Reason
              if (isARFilled && "@".equals(codReason)) {
                error.addError((row.getRowNum() + 1), "<br>COD Reason", "@ value for COD Reason is not allowed.");
              }

              // Collector
              if (isARFilled && "@".equals(collector)) {
                error.addError((row.getRowNum() + 1), "<br>Collector", "@ value for Collector is not allowed.");
              }

            }

            if ("Email".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              email1 = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              email2 = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              email3 = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(email1) || StringUtils.isNotBlank(email2) || StringUtils.isNotBlank(email3)) {
                isEmailFilled = true;
              }

              if ((isEmailFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              }

              // Email1
              if (isEmailFilled && "@".equals(email1)) {
                error.addError((row.getRowNum() + 1), "<br>Email1", "@ value for Email1 is not allowed.");
              }
            }

            if ("TaxInfo".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              taxCode = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              taxNumber = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              taxSepIndc = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              billPrintIndc = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              contractPrintIndc = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              countryUse = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(cmrNo) || StringUtils.isNotBlank(taxCode) || StringUtils.isNotBlank(taxNumber)
                  || StringUtils.isNotBlank(taxSepIndc) || StringUtils.isNotBlank(billPrintIndc) || StringUtils.isNotBlank(contractPrintIndc)
                  || StringUtils.isNotBlank(countryUse)) {
                isTaxInfoFilled = true;
              }

              if ((isTaxInfoFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              }

              // Tax Code
              if (isTaxInfoFilled && "@".equals(taxCode)) {
                error.addError((row.getRowNum() + 1), "<br>Tax Code", "@ value for Tax Code is not allowed.");
              }

              // Tax Number
              if (isTaxInfoFilled && "@".equals(taxNumber)) {
                error.addError((row.getRowNum() + 1), "<br>Tax Number", "@ value for Tax Number is not allowed.");
              }

              // Tax Separation Indicator
              if (isTaxInfoFilled && "@".equals(taxSepIndc)) {
                error.addError((row.getRowNum() + 1), "<br>Tax Separation Indicator", "@ value for Tax Separation Indicator is not allowed.");
              }

              // Billing Print Indicator
              if (isTaxInfoFilled && "@".equals(billPrintIndc)) {
                error.addError((row.getRowNum() + 1), "<br>Billing Print Indicator", "@ value for Billing Print Indicator is not allowed.");
              }

              // Contract Print Indicator
              if (isTaxInfoFilled && "@".equals(contractPrintIndc)) {
                error.addError((row.getRowNum() + 1), "<br>Contract Print Indicator", "@ value for Contract Print Indicator is not allowed.");
              }

              // Country Use
              if (isTaxInfoFilled && "@@@@".equals(countryUse)) {
                error.addError((row.getRowNum() + 1), "<br>Country Use", "@ value for Country Use is not allowed.");
              }

            }

            if ("Install-At".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              addrNoSeq = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              addrName = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              addrNameCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              street = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              streetCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              city = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              stateProv = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              postal = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              landed = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(cmrNo) || StringUtils.isNotBlank(addrNoSeq) || StringUtils.isNotBlank(addrName)
                  || StringUtils.isNotBlank(addrNameCont) || StringUtils.isNotBlank(street) || StringUtils.isNotBlank(streetCont)
                  || StringUtils.isNotBlank(city) || StringUtils.isNotBlank(stateProv) || StringUtils.isNotBlank(postal)) {
                isInstallAtFilled = true;
              }

              if ((isInstallAtFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              }

              if ((isInstallAtFilled) && StringUtils.isBlank(addrNoSeq)) {
                LOG.trace("Address Sequence No is required.");
                error.addError((row.getRowNum() + 1), "<br>Sequence", "Address Sequence No is required.");
              }

              // Name
              if (isInstallAtFilled && "@".equals(addrName)) {
                error.addError((row.getRowNum() + 1), "<br>Name", "@ value for Name is not allowed.");
              }

              // Street Address
              if (isInstallAtFilled && "@".equals(street)) {
                error.addError((row.getRowNum() + 1), "<br>Street Address", "@ value for Street Address is not allowed.");
              }

              // City
              if (isInstallAtFilled && "@".equals(city)) {
                error.addError((row.getRowNum() + 1), "<br>City", "@ value for City is not allowed.");
              }

              // State/Province
              if (isInstallAtFilled && "@".equals(stateProv) && !Arrays.asList("DO", "PE", "NI", "CR", "PA", "GT", "HN", "SV").contains(landed)) {
                error.addError((row.getRowNum() + 1), "<br>State/Province", "@ value for State/Province is not allowed.");
              }

              // Postal Code
              if (isInstallAtFilled && "@".equals(postal)) {
                error.addError((row.getRowNum() + 1), "<br>Postal Code", "@ value for Postal Code is not allowed.");
              }

            }
            if ("Ship-To".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              addrNoSeq = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              addrName = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              addrNameCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              street = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              streetCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              city = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              stateProv = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              postal = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              landed = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(cmrNo) || StringUtils.isNotBlank(addrNoSeq) || StringUtils.isNotBlank(addrName)
                  || StringUtils.isNotBlank(addrNameCont) || StringUtils.isNotBlank(street) || StringUtils.isNotBlank(streetCont)
                  || StringUtils.isNotBlank(city) || StringUtils.isNotBlank(stateProv) || StringUtils.isNotBlank(postal)) {
                isShipToFilled = true;
              }

              if ((isShipToFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              }

              if ((isShipToFilled) && StringUtils.isBlank(addrNoSeq)) {
                LOG.trace("Address Sequence No is required.");
                error.addError((row.getRowNum() + 1), "<br>Sequence", "Address Sequence No is required.");
              }

              // Name
              if (isShipToFilled && "@".equals(addrName)) {
                error.addError((row.getRowNum() + 1), "<br>Name", "@ value for Name is not allowed.");
              }

              // Street Address
              if (isShipToFilled && "@".equals(street)) {
                error.addError((row.getRowNum() + 1), "<br>Street Address", "@ value for Street Address is not allowed.");
              }

              // City
              if (isShipToFilled && "@".equals(city)) {
                error.addError((row.getRowNum() + 1), "<br>City", "@ value for City is not allowed.");
              }

              // State/Province
              if (isShipToFilled && "@".equals(stateProv) && !Arrays.asList("DO", "PE", "NI", "CR", "PA", "GT", "HN", "SV").contains(landed)) {
                error.addError((row.getRowNum() + 1), "<br>State/Province", "@ value for State/Province is not allowed.");
              }

              // Postal Code
              if (isShipToFilled && "@".equals(postal)) {
                error.addError((row.getRowNum() + 1), "<br>Postal Code", "@ value for Postal Code is not allowed.");
              }
            }
            if ("Bill-To".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              addrNoSeq = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              addrName = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              addrNameCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              street = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              streetCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              city = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              stateProv = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              postal = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              landed = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(cmrNo) || StringUtils.isNotBlank(addrNoSeq) || StringUtils.isNotBlank(addrName)
                  || StringUtils.isNotBlank(addrNameCont) || StringUtils.isNotBlank(street) || StringUtils.isNotBlank(streetCont)
                  || StringUtils.isNotBlank(city) || StringUtils.isNotBlank(stateProv) || StringUtils.isNotBlank(postal)) {
                isBillToFilled = true;
              }

              if ((isBillToFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              }

              if ((isBillToFilled) && StringUtils.isBlank(addrNoSeq)) {
                LOG.trace("Address Sequence No is required.");
                error.addError((row.getRowNum() + 1), "<br>Sequence", "Address Sequence No is required.");
              }

              // Name
              if (isBillToFilled && "@".equals(addrName)) {
                error.addError((row.getRowNum() + 1), "<br>Name", "@ value for Name is not allowed.");
              }

              // Street Address
              if (isBillToFilled && "@".equals(street)) {
                error.addError((row.getRowNum() + 1), "<br>Street Address", "@ value for Street Address is not allowed.");
              }

              // City
              if (isBillToFilled && "@".equals(city)) {
                error.addError((row.getRowNum() + 1), "<br>City", "@ value for City is not allowed.");
              }

              // State/Province
              if (isBillToFilled && "@".equals(stateProv) && !Arrays.asList("DO", "PE", "NI", "CR", "PA", "GT", "HN", "SV").contains(landed)) {
                error.addError((row.getRowNum() + 1), "<br>State/Province", "@ value for State/Province is not allowed.");
              }

              // Postal Code
              if (isBillToFilled && "@".equals(postal)) {
                error.addError((row.getRowNum() + 1), "<br>Postal Code", "@ value for Postal Code is not allowed.");
              }
            }
            if ("Sold-To".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(1);
              addrNoSeq = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(2);
              addrName = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              addrNameCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              street = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              streetCont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              city = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              stateProv = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              postal = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              landed = validateColValFromCell(currCell);

              if (StringUtils.isNotBlank(cmrNo) || StringUtils.isNotBlank(addrNoSeq) || StringUtils.isNotBlank(addrName)
                  || StringUtils.isNotBlank(addrNameCont) || StringUtils.isNotBlank(street) || StringUtils.isNotBlank(streetCont)
                  || StringUtils.isNotBlank(city) || StringUtils.isNotBlank(stateProv) || StringUtils.isNotBlank(postal)) {
                isSoldToFilled = true;
              }

              if ((isSoldToFilled) && StringUtils.isBlank(cmrNo)) {
                LOG.trace("CMR No. is required.");
                error.addError((row.getRowNum() + 1), "<br>CMR No.", "CMR No. is required.");
              }

              if ((isSoldToFilled) && StringUtils.isBlank(addrNoSeq)) {
                LOG.trace("Address Sequence No is required.");
                error.addError((row.getRowNum() + 1), "<br>Sequence", "Address Sequence No is required.");
              }

              // Name
              if (isSoldToFilled && "@".equals(addrName)) {
                error.addError((row.getRowNum() + 1), "<br>Name", "@ value for Name is not allowed.");
              }

              // Street Address
              if (isSoldToFilled && "@".equals(street)) {
                error.addError((row.getRowNum() + 1), "<br>Street Address", "@ value for Street Address is not allowed.");
              }

              // City
              if (isSoldToFilled && "@".equals(city)) {
                error.addError((row.getRowNum() + 1), "<br>City", "@ value for City is not allowed.");
              }

              // State/Province
              if (isSoldToFilled && "@".equals(stateProv) && !Arrays.asList("DO", "PE", "NI", "CR", "PA", "GT", "HN", "SV").contains(landed)) {
                error.addError((row.getRowNum() + 1), "<br>State/Province", "@ value for State/Province is not allowed.");
              }

              // Postal Code
              if (isSoldToFilled && "@".equals(postal)) {
                error.addError((row.getRowNum() + 1), "<br>Postal Code", "@ value for Postal Code is not allowed.");
              }
            }

          }
        } // end row loop

        if (error.hasErrors()) {
          validations.add(error);
        }
      }
    }
  }

  public static boolean isTaxInfoUpdated(EntityManager entityManager, Long reqId) {
    List<GeoTaxInfo> taxInfoCreqCMRList = getAllGeoTaxInfo(entityManager, reqId);
    List<GeoTaxInfo> taxInfoRDCList = mapGeoTaxInfoFromRdcData(entityManager, reqId);

    Comparator<GeoTaxInfo> compareByTaxCd = (GeoTaxInfo o1, GeoTaxInfo o2) -> o1.getTaxCd().compareTo(o2.getTaxCd());
    Collections.sort(taxInfoRDCList, compareByTaxCd);
    Collections.sort(taxInfoCreqCMRList, compareByTaxCd);

    return checkIsTaxInfoUpdated(taxInfoRDCList, taxInfoCreqCMRList);
  }

  private static boolean checkIsTaxInfoUpdated(List<GeoTaxInfo> taxInfoRDCList, List<GeoTaxInfo> taxInfoCreqCMRList) {
    if (taxInfoRDCList.size() != taxInfoCreqCMRList.size()) {
      return true;
    }

    for (int i = 0; i < taxInfoRDCList.size(); i++) {
      GeoTaxInfo taxInfoRDC = taxInfoRDCList.get(i);
      GeoTaxInfo taxInfoCreqCMR = taxInfoCreqCMRList.get(i);

      if (taxInfoRDC != null && taxInfoCreqCMR != null) {
        String taxCdRdc = StringUtils.isNotBlank(taxInfoRDC.getTaxCd()) ? taxInfoRDC.getTaxCd() : "";
        String taxSepIndcRdc = StringUtils.isNotBlank(taxInfoRDC.getTaxSeparationIndc()) ? taxInfoRDC.getTaxSeparationIndc() : "";
        String taxBillPrintIndcRdc = StringUtils.isNotBlank(taxInfoRDC.getBillingPrintIndc()) ? taxInfoRDC.getBillingPrintIndc() : "";
        String taxContrPrintIndcRdc = StringUtils.isNotBlank(taxInfoRDC.getContractPrintIndc()) ? taxInfoRDC.getContractPrintIndc() : "";
        String taxCntryUseRdc = StringUtils.isNotBlank(taxInfoRDC.getCntryUse()) ? taxInfoRDC.getCntryUse() : "";

        boolean taxCdUpdated = !taxCdRdc.equals(taxInfoCreqCMR.getTaxCd());
        boolean taxSepIndcUpdated = !taxSepIndcRdc.equals(taxInfoCreqCMR.getTaxSeparationIndc());
        boolean billPrintIndcUpdated = !taxBillPrintIndcRdc.equals(taxInfoCreqCMR.getBillingPrintIndc());
        boolean contrPrintIndcUpdated = !taxContrPrintIndcRdc.equals(taxInfoCreqCMR.getContractPrintIndc());
        boolean cntryUseUpdated = !taxCntryUseRdc.equals(taxInfoCreqCMR.getCntryUse());

        if (taxCdUpdated || taxSepIndcUpdated || billPrintIndcUpdated || contrPrintIndcUpdated || cntryUseUpdated) {
          LOG.debug("Updated Tax Info tab, Tax Type: " + taxCdRdc);
          LOG.debug("Tax Type: " + taxCdUpdated);
          LOG.debug("Tax Separate Indc: " + taxSepIndcUpdated);
          LOG.debug("Billing Print Indc: " + billPrintIndcUpdated);
          LOG.debug("Contract Print Indc: " + contrPrintIndcUpdated);
          LOG.debug("Country Use: " + cntryUseUpdated);
          return true;
        }
      } else {
        return true;
      }

    }
    return false;
  }

  private static List<GeoTaxInfo> mapGeoTaxInfoFromRdcData(EntityManager entityManager, Long reqId) {
    Addr soldToAddr = LegacyDirectUtil.getSoldToAddress(entityManager, reqId);
    List<TaxData> taxDataList = getTaxDataByKunnr(entityManager, soldToAddr.getSapNo());
    List<GeoTaxInfo> taxInfoList = new ArrayList<>();
    for (TaxData taxData : taxDataList) {
      GeoTaxInfo geoTaxInfo = new GeoTaxInfo();
      GeoTaxInfoPK geoTaxInfoPK = new GeoTaxInfoPK();

      geoTaxInfo.setContractPrintIndc(taxData.getContractPrintIndc());
      geoTaxInfo.setCntryUse(taxData.getCntryUse());
      geoTaxInfo.setTaxCd(taxData.getId().getTaxCd());
      geoTaxInfo.setTaxSeparationIndc(taxData.getTaxSeparationIndc());
      geoTaxInfo.setBillingPrintIndc(taxData.getBillingPrintIndc());
      geoTaxInfo.setTaxNum(taxData.getTaxNum());
      geoTaxInfoPK.setGeoTaxInfoId(1);
      geoTaxInfoPK.setReqId(reqId);
      geoTaxInfo.setId(geoTaxInfoPK);

      taxInfoList.add(geoTaxInfo);
    }

    return taxInfoList;
  }

  private static List<GeoTaxInfo> getAllGeoTaxInfo(EntityManager entityManager, Long reqId) {
    PreparedQuery taxInfoQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("REQUESTENTRY.TAXINFO.SEARCH_BY_REQID"));
    taxInfoQuery.setParameter("REQ_ID", reqId);

    List<GeoTaxInfo> geoTaxInfoList = taxInfoQuery.getResults(GeoTaxInfo.class);
    return geoTaxInfoList;
  }

}
