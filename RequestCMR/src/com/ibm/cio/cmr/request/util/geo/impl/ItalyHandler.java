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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
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
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

//import com.ibm.cio.cmr.request.entity.AddrPK;

/**
 * Seperate class for Italy handling
 * 
 * @author JeffZAMORA
 * 
 */
public class ItalyHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(ItalyHandler.class);

  public static final String COMPANY_ADDR_TYPE = "ZI01";
  public static final String BILLING_ADDR_TYPE = "ZP01";
  public static final String INSTALLING_ADDR_TYPE = "ZS01";
  public static final String COMPANY_ADDR_TYPE_NEW = "ZORG";
  public static final String COMPANY_ADDR_SEQ_NO = "0000C";
  public static final String BILLING_INSTALLING_ADDR_TYPE_PP = "ZS02";

  private static final String[] ITALY_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CAP", "CMROwner", "CustClassCode", "LocalTax2", "SearchTerm", "POBoxCity",
      "POBoxPostalCode", "TransportZone", "Office", "Floor", "County", "City2", "StreetAddress2", "INACType", "CustLang" };

  private RequestEntryModel currentReqEntryModel;

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {
    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    // NOTE: We will be able to get basic billing details and installing from
    // RDc.
    // only company address will be from SOF
    this.currentReqEntryModel = reqEntry;

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // PROSPECT CMR FLOW
      if (prospectCmrChosen) {
        boolean billingFound = false;
        FindCMRRecordModel billing = null;
        boolean companyFound = false;
        FindCMRRecordModel company = null;

        for (FindCMRRecordModel record : source.getItems()) {
          if (INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
            LOG.debug("Loading Installing Address from CMR No. " + record.getCmrNum());
            record.setCmrAddrSeq("00001");
            converted.add(record);
          }
        }

        if (!billingFound) {
          LOG.debug("Adding a copy of installing as billing");
          // if billing is not found, it means installing = billing. we add a
          // copy
          // of installing as billing
          billing = new FindCMRRecordModel();
          for (FindCMRRecordModel record : source.getItems()) {
            if (INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
              try {
                PropertyUtils.copyProperties(billing, record);
                billing.setCmrAddrTypeCode(BILLING_ADDR_TYPE);
                billing.setCmrSapNumber(null);
                // billing.setCmrAddrSeq("00001");
                converted.add(billing);
              } catch (Exception e) {
                // noop
              }
              break;
            }
          }
        }

        if (!companyFound) {
          LOG.debug("Adding a copy of installing as company");
          // if billing is not found, it means installing = company. we add a
          // copy of installing as company
          company = new FindCMRRecordModel();
          for (FindCMRRecordModel record : source.getItems()) {
            if (INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
              try {
                PropertyUtils.copyProperties(company, record);
                company.setCmrAddrTypeCode(COMPANY_ADDR_TYPE);
                company.setCmrSapNumber(null);
                company.setCmrAddrSeq(COMPANY_ADDR_SEQ_NO);
                converted.add(company);
              } catch (Exception e) {
                // noop
              }
              break;
            }
          }
        }

      } else {
        String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), "U");
        if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
          importCreateDataLD(entityManager, source, reqEntry, mainRecord, converted, searchModel);
        } else {
          String chosenType = searchModel.getAddrType();
          if (!StringUtils.isEmpty(chosenType)) {
            // load the company address first
            String companyCmr = mainRecord.getCmrCompanyNo();
            if (StringUtils.isEmpty(companyCmr)) {
              // try to get CompanyNo from SOF
              LOG.debug("Checking Company No. from SOF..");
              companyCmr = this.currentImportValues.get("CompanyNo");
              mainRecord.setCmrCompanyNo(companyCmr);
            }
            LOG.debug("Company No: " + companyCmr);
            String cmrNo = mainRecord.getCmrNum();
            FindCMRResultModel companySearch = source;
            if (!cmrNo.equals(companyCmr)) {
              LOG.debug("Chosen CMR is not the Company CMR, looking for Company CMR details..");
              companySearch = SystemUtil.findCMRs(companyCmr, SystemLocation.ITALY, 10);
            }
            for (FindCMRRecordModel record : companySearch.getItems()) {
              if (INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
                LOG.debug("Loading Company Address from CMR No. " + record.getCmrNum());
                // Defect 1535000: PP: For VA Create, Dummy postal code is null
                // if
                // Company is Imported
                if (!StringUtils.isEmpty(record.getCmrCountryLanded())
                    && (record.getCmrCountryLanded().equalsIgnoreCase("SM") || record.getCmrCountryLanded().equalsIgnoreCase("VA"))) {
                  record.setCmrPOBoxPostCode("55100");
                }
                loadCompanyAddressData(record);
                converted.add(record);
              }
            }
          }
          if (BILLING_ADDR_TYPE.equals(chosenType)) {
            // when billing is chosen, only company will be imported
            LOG.debug("Billing was chosen so not importing anything but Company.");
          } else if (INSTALLING_ADDR_TYPE.equals(chosenType)) {
            // when installing is chosen, company and billing will be imported
            LOG.debug("Intsalling was chosen, also importing billing");

            boolean billingFound = false;
            FindCMRRecordModel billing = null;
            // try to get billing from FindCMR
            for (FindCMRRecordModel record : source.getItems()) {
              if (BILLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
                billing = record;
                billing.setParentCMRNo(record.getCmrNum());
                String sofSeq = this.currentImportValues.get("CtryUseBAddressNumber");
                if (!StringUtils.isEmpty(sofSeq) && sofSeq.trim().length() == 5) {
                  billing.setCmrAddrSeq(sofSeq);
                } else {
                  billing.setCmrAddrSeq("00001");
                }
                billing.setCmrStreetAddressCont(record.getCmrName3());
                billing.setCmrName3(null);
                billing.setCmrName4(null);
                billing.setCmrCity(record.getCmrPostalCode() + " " + record.getCmrCity());
                billing.setCmrPostalCode(record.getCmrCountryLandedDesc() + " " + record.getCmrPostalCode());
                billing.setCmrState(record.getCmrState());
                billing.setCmrSapNumber(record.getCmrSapNumber());
                billingFound = true;
              }
            }
            String billingCmrNo = this.currentImportValues.get("Anagrafico2BillingNo");

            if (!billingFound) {
              if (!StringUtils.isEmpty(billingCmrNo)) {
                // get from SOF/MAN
                billing = extractBillingAddressData(billingCmrNo);
              } else {
                LOG.debug("No separate billing address found, adding a copy of installing as billing");
                // if billing is not found, add a copy of installing as billing
                billing = new FindCMRRecordModel();
                for (FindCMRRecordModel record : source.getItems()) {
                  if (INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
                    try {
                      PropertyUtils.copyProperties(billing, record);
                      billing.setCmrAddrTypeCode(BILLING_ADDR_TYPE);
                      billing.setParentCMRNo(mainRecord.getCmrNum());
                      // billing.setCmrSapNumber(record.getCmrSapNumber());
                      String sofSeq = this.currentImportValues.get("CtryUseBAddressNumber");
                      if (!StringUtils.isEmpty(sofSeq) && sofSeq.trim().length() == 5) {
                        billing.setCmrAddrSeq(sofSeq);
                      } else {
                        billing.setCmrAddrSeq("00001");
                      }

                    } catch (Exception e) {
                      // noop
                    }
                    break;
                  }
                }
              }
            }
            converted.add(billing);

          } else {
            // import nothing, no address type chosen
            LOG.debug("Invalid chosen address type, nothing to import");
          }

          if (reqEntry.getReqId() > 0) {
            // this is an existing request, check if any installing is present
            LOG.debug("Checking any existing installing address on Request ID " + reqEntry.getReqId());
            Addr installingAddr = getCurrentInstallingAddress(entityManager, reqEntry.getReqId());
            if (installingAddr != null) {
              LOG.debug("Adding installing to the records");
              FindCMRRecordModel installing = new FindCMRRecordModel();
              PropertyUtils.copyProperties(installing, mainRecord);
              copyAddrData(installing, installingAddr);
              // installing.setParentCMRNo(mainRecord.getCmrNum());
              installing.setCmrAddrSeq("1");
              converted.add(installing);
              // do a dummy import based on the installing
            }
          }
        }
      }
    } else {
      // for updates, just import all CMRs from the current records
      String reqType = reqEntry.getReqType();
      String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), reqType);
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {

        LOG.debug("Importing Installing and Billing for update from CMR No. " + mainRecord.getCmrNum());
        boolean billingFound = false;
        // boolean zorgFound = false;

        // Handle ZS02 records..
        for (FindCMRRecordModel record : source.getItems()) {
          if (BILLING_INSTALLING_ADDR_TYPE_PP.equals(record.getCmrAddrTypeCode())
              && (StringUtils.isBlank(record.getCmrOrderBlock()) || Arrays.asList("88", "92", "94").contains(record.getCmrOrderBlock()))) {
            record.setCmrAddrTypeCode(INSTALLING_ADDR_TYPE);
          } else if (BILLING_INSTALLING_ADDR_TYPE_PP.equals(record.getCmrAddrTypeCode()) && "90".equals(record.getCmrOrderBlock())) {
            record.setCmrAddrTypeCode(BILLING_ADDR_TYPE);
          }
        }

        if (source.getItems().size() > 0) {
          Collections.sort(source.getItems());
        }

        for (FindCMRRecordModel record : source.getItems()) {
          if (BILLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode()) || INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
            record.setCmrStreetAddressCont(record.getCmrName3());
            record.setCmrName3(null);
            record.setCmrName4(null);
            record.setCmrCity(record.getCmrCity());
            record.setCmrPostalCode(record.getCmrPostalCode());
            record.setCmrState(record.getCmrState());
            if (BILLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
              billingFound = true;
            }
            /*
             * DENNIS T NATAD, BEGIN CMR-441: For Updates,all imported addresses
             * have 00001 sequences though should have real sequences
             */
            String seq = record.getCmrAddrSeq();
            // this.currentImportValues.get("CtryUseBAddressNumber");
            boolean isPadZeroes = seq != null && seq.length() != 5 ? true : false;
            if (!StringUtils.isEmpty(seq) && seq.trim().length() == 5) {
              record.setCmrAddrSeq(seq);
            } else {
              // record.setCmrAddrSeq("00001");
              if (!StringUtils.isEmpty(record.getCmrAddrSeq())) {
                record.setCmrAddrSeq(LegacyDirectUtil.handleLDSeqNoScenario(seq, isPadZeroes));
              } else if (!INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode()) && !BILLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
                record.setCmrAddrSeq("0000C");
              }
            }
            /*
             * DENNIS T NATAD, END CMR-441: For Updates,all imported addresses
             * have 00001 sequences though should have real sequences
             */
            record.setParentCMRNo(record.getCmrNum());
            converted.add(record);
          }
          // if ("ZORG".equals(record.getCmrAddrType())) {
          // zorgFound = true;
          // }
        }

        // Single reactivation : if Billing and company address not found in RDC
        // or DB2
        List<CmrtAddr> cmrtAddrs = null;
        if (CmrConstants.REQ_TYPE_SINGLE_REACTIVATE.equals(reqType)) {
          cmrtAddrs = LegacyDirectUtil.checkLDAddress(entityManager, mainRecord.getCmrNum(), mainRecord.getCmrIssuedBy());
        }

        if (CmrConstants.REQ_TYPE_SINGLE_REACTIVATE.equals(reqType) && source.getItems().size() == 1
            && INSTALLING_ADDR_TYPE.equals(source.getItems().get(0).getCmrAddrTypeCode()) && cmrtAddrs != null && cmrtAddrs.size() == 1) {
          LOG.debug("Single reactivation: Copy Comapany and Billing from Installing address of CMR No. " + mainRecord.getCmrNum());
          createMissingAddressForSingleReact(source, converted, mainRecord);
        } else {
          /*
           * if (!zorgFound) { // should we get it from CMRTADDR? CmrtAddr addrC
           * = getLegacyCompanyAddr(entityManager, mainRecord.getCmrNum());
           * FindCMRRecordModel recordC = new FindCMRRecordModel(); if (addrC !=
           * null) { recordC.setCmrAddrSeq(addrC.getId().getAddrNo());
           * recordC.setCmrName(addrC.getAddrLine1());
           * recordC.setCmrName2(addrC.getAddrLine2());
           * recordC.setCmrStreetAddress(addrC.getAddrLine4());
           * recordC.setCmrStreetAddressCont(addrC.getAddrLine2());
           * recordC.setCmrName3(null); recordC.setCmrName4(null);
           * recordC.setCmrCity(addrC.getCity());
           * recordC.setCmrPostalCode(addrC.getZipCode());
           * recordC.setCmrState(addrC.getItCompanyProvCd()); } }
           */

          String billingCmrNo = this.currentImportValues.get("Anagrafico2BillingNo");

          FindCMRRecordModel billing = null;
          if (!billingFound) {

            if (!StringUtils.isEmpty(billingCmrNo)) {
              billing = extractBillingAddressData(billingCmrNo);
              converted.add(billing);
            } else {

              if (!billingFound) {
                FindCMRRecordModel billingAddr = new FindCMRRecordModel();
                LOG.debug("No separate billing address found, loading billing from Legacy");
                loadBillingAddressDataLD(entityManager, billingAddr, mainRecord);
                converted.add(billingAddr);
              }
            }
          }

          LOG.debug("Adding Company Address from CMR No. " + mainRecord.getCmrCompanyNo());
          String companyCmr = mainRecord.getCmrCompanyNo();
          FindCMRResultModel resultsR = LegacyDirectUtil.findCmrByAddrSeq(companyCmr, mainRecord.getCmrIssuedBy(), COMPANY_ADDR_SEQ_NO, "");
          FindCMRRecordModel companyAddr = new FindCMRRecordModel();

          if (resultsR != null && resultsR.getItems() != null && resultsR.getItems().size() > 0) {
            List<FindCMRRecordModel> listResultItems = resultsR.getItems();
            // DENNIS:There should be only one company addr
            companyAddr = listResultItems.get(0);
            // DENNIS: Overriding the type and code for the ZORG to be ZI01
            companyAddr.setCmrAddrType("Company");
            companyAddr.setCmrAddrTypeCode("ZI01");
            companyAddr.setParentCMRNo(mainRecord.getCmrCompanyNo());
          } else {
            // DENNIS:If the company number is empty, we get the company addr
            // from
            // LD DB
            companyAddr.setParentCMRNo(mainRecord.getCmrCompanyNo());
            loadCompanyAddressDataLD(entityManager, companyAddr);
          }
          converted.add(companyAddr);
        }
      }
      source.setItems(converted);
    }
  }

  private void loadCompanyAddressDataLD(EntityManager entityManager, FindCMRRecordModel record) {
    LegacyDirectUtil.getItalyCompanyAddress(entityManager, record, record.getParentCMRNo());
  }

  private void loadBillingAddressDataLD(EntityManager entityManager, FindCMRRecordModel record, FindCMRRecordModel mainRecord) {
    try {
      LegacyDirectUtil.getItalyBillingAddress(entityManager, record, mainRecord.getCmrNum(), mainRecord.getCmrAddrSeq());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void loadCompanyAddressDataLD(FindCMRRecordModel record, FindCMRRecordModel source) {
    // record.setCmrName1Plain(source.getCmrName1Plain());
    // record.setCmrName2Plain(source.getCmrName2Plain());
    // record.setCmrName3(null);
    // record.setCmrName4(null);
    //
    // record.setCmrStreetAddress(source.getCmrStreetAddress());
    // record.setCmrStreetAddressCont(source.getCmrStreetAddressCont());
    // record.setCmrPostalCode(source.getCmrPostalCode());
    // record.setCmrCity(source.getCmrCity());
    //
    // record.setCmrCountryLanded(source.getCmrCountryLanded());
    // record.setCmrAddrTypeCode(COMPANY_ADDR_TYPE);
    // record.setCmrState(source.get);
    // record.setParentCMRNo(record.getCmrNum());
    //
    // String sofSeq = this.currentImportValues.get("CtryUseCAddressNumber");
    // if (!StringUtils.isEmpty(sofSeq) && sofSeq.trim().length() == 5) {
    // record.setCmrAddrSeq(sofSeq);
    // } else {
    // record.setCmrAddrSeq("00001");
    // }
    //
    // record.setCmrCounty(null);
  }

  /**
   * Loads the company address into this record
   * 
   * @param record
   */
  private void loadCompanyAddressData(FindCMRRecordModel record) {
    FindCMRRecordModel companyAddress = new FindCMRRecordModel();

    companyAddress.setCmrCountryLanded(record.getCmrCountryLanded());
    // handleSOFAddressImport(null, record.getCmrIssuedBy(), companyAddress,
    // "Anagrafico1");

    record.setCmrName1Plain(companyAddress.getCmrName1Plain());
    record.setCmrName2Plain(companyAddress.getCmrName2Plain());
    record.setCmrName3(null);
    record.setCmrName4(null);

    record.setCmrStreetAddress(companyAddress.getCmrStreetAddress());
    record.setCmrStreetAddressCont(companyAddress.getCmrStreetAddressCont());
    record.setCmrPostalCode(companyAddress.getCmrStreetAddress() + " " + companyAddress.getCmrPostalCode());
    record.setCmrCity(companyAddress.getCmrPostalCode() + " " + companyAddress.getCmrCity());

    record.setCmrCountryLanded(companyAddress.getCmrCountryLanded());
    record.setCmrAddrTypeCode(COMPANY_ADDR_TYPE);
    record.setCmrState(this.currentImportValues.get("CompanyProvinceCode"));
    record.setParentCMRNo(record.getCmrNum());

    String sofSeq = this.currentImportValues.get("CtryUseCAddressNumber");
    if (!StringUtils.isEmpty(sofSeq) && sofSeq.trim().length() == 5) {
      record.setCmrAddrSeq(sofSeq);
    } else {
      record.setCmrAddrSeq("00001");
    }

    record.setCmrCounty(null);
  }

  /**
   * Extract Billing Information for CMRs with a different Billing Address
   * 
   * @param record
   * @throws CmrException
   */
  private FindCMRRecordModel extractBillingAddressData(String cmrNo) throws CmrException {

    FindCMRRecordModel record = null;
    LOG.debug("Checking billing information from FindCMR..");
    FindCMRResultModel searchResult = SystemUtil.findCMRs(cmrNo, SystemLocation.ITALY, 20);
    if (searchResult != null && searchResult.getItems() != null && searchResult.getItems().size() > 0) {
      for (FindCMRRecordModel rec : searchResult.getItems()) {
        if ("ZP01".equals(rec.getCmrAddrTypeCode())) {
          // we found a billing
          record = rec;
          record.setParentCMRNo(cmrNo);
          LOG.debug("Billing CMR found in FindCMR");
          break;
        }
      }
    }
    if (record == null) {
      LOG.debug("No Billing CMR found in FindCMR, getting info from SOF...");
      record = new FindCMRRecordModel();
      FindCMRRecordModel billingAddress = new FindCMRRecordModel();

      handleSOFAddressImport(null, SystemLocation.ITALY, billingAddress, "Anagrafico2");

      record.setCmrName1Plain(billingAddress.getCmrName1Plain());
      record.setCmrName2Plain(billingAddress.getCmrName2Plain());
      record.setCmrName3(null);
      record.setCmrName4(null);
      record.setCmrStreetAddress(billingAddress.getCmrStreetAddress());
      record.setCmrStreetAddressCont(billingAddress.getCmrStreetAddressCont());
      record.setCmrPostalCode(billingAddress.getCmrPostalCode());
      record.setCmrCity(billingAddress.getCmrPostalCode() + " " + billingAddress.getCmrCity());
      record.setCmrCountryLanded(billingAddress.getCmrCountryLanded());
      record.setCmrState(this.currentImportValues.get("Anagrafico2ProvinceCode"));
      record.setParentCMRNo(cmrNo);
      record.setCmrSapNumber(billingAddress.getCmrSapNumber());
      if (StringUtils.isEmpty(record.getCmrCountryLanded())) {
        // set Italy as default
        record.setCmrCountryLanded("IT");
      }
    }

    record.setCmrAddrTypeCode(BILLING_ADDR_TYPE);
    String sofSeq = this.currentImportValues.get("CtryUseBAddressNumber");
    if (!StringUtils.isEmpty(sofSeq) && sofSeq.trim().length() == 5) {
      record.setCmrAddrSeq(sofSeq);
    } else {
      record.setCmrAddrSeq("00001");
    }
    record.setCmrCounty(null);

    return record;
  }

  /**
   * Loads the extra billing address fields from SOF
   * 
   * @param billing
   * @throws Exception
   */
  private void loadBillingAddressExtraFields(Addr billing, boolean ifProspect) throws Exception {

    String cmrNo = billing.getParCmrNo();

    ItalyHandler tempHandler = new ItalyHandler();
    FindCMRRecordModel tempRecord = new FindCMRRecordModel();
    tempRecord.setCmrIssuedBy(SystemLocation.ITALY);
    tempRecord.setCmrNum(cmrNo);
    String companyCMR = this.currentImportValues.get("CompanyNo");
    if (!ifProspect) {
      tempHandler.retrieveSOFValues(tempRecord);

      String nameAbbr = tempHandler.getCurrentValue("Anagrafico2", "NameAbbr");
      String streetAbbr = tempHandler.getCurrentValue("Anagrafico2", "StreetAbbr");
      String locAbbr = tempHandler.getCurrentValue("Anagrafico2", "LocAbbr");
      String stateProvince = tempHandler.getCurrentValue("Anagrafico2", "ProvinceCode");
      String postalAddress = tempHandler.getCurrentValue("Anagrafico2", "PostalAddress");

      String processingType = PageManager.getProcessingType(SystemLocation.ITALY, "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
        CmrtCustExt cmrtExt = null;
        if (!StringUtils.isEmpty(companyCMR) && !companyCMR.equals(cmrNo)) {
          cmrtExt = getBillingCustExtFields(cmrNo);
        } else {
          cmrtExt = this.legacyObjects.getCustomerExt();
        }
        List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
        if (cmrtExt != null) {
          nameAbbr = cmrtExt.getItBillingName();
          streetAbbr = cmrtExt.getItBillingStreet();
          locAbbr = cmrtExt.getItBillingCity();
        }

        for (CmrtAddr cmrtAddr : cmrtAddrs) {
          if ("Y".equals(cmrtAddr.getIsAddrUseBilling())) {
            stateProvince = cmrtAddr.getItCompanyProvCd();
            // postalAddress = cmrtAddr.getItPostalAddrss();
          }
        }
      }

      if (nameAbbr != null && !"".equals(nameAbbr)) {
        if (nameAbbr.length() > 19) {
          nameAbbr = nameAbbr.substring(0, 19);
        }
      }

      if (streetAbbr != null && !"".equals(streetAbbr)) {
        if (streetAbbr.length() > 18) {
          streetAbbr = streetAbbr.substring(0, 18);
        }
      }

      if (locAbbr != null && !"".equals(locAbbr)) {
        if (locAbbr.length() > 12) {
          locAbbr = locAbbr.substring(0, 12);
        }
      }

      LOG.debug("Extra Billing Fields:");
      LOG.debug(" - Name Abbr: " + nameAbbr);
      LOG.debug(" - Street Abbr: " + streetAbbr);
      LOG.debug(" - Loc Abbr: " + locAbbr);
      LOG.debug(" - StateProvince: " + stateProvince);
      // LOG.debug(" - PostalAddress: " + postalAddress);

      billing.setBldg(nameAbbr);
      billing.setDivn(streetAbbr);
      billing.setCustFax(locAbbr);
      billing.setStateProv(stateProvince);
      // billing.setBillingPstlAddr(postalAddress);
    } else {

      if (tempRecord.getCmrName1Plain() != null && !"".equals(tempRecord.getCmrName1Plain())) {
        if (tempRecord.getCmrName1Plain().length() > 19) {
          billing.setBldg(tempRecord.getCmrName1Plain().substring(0, 19));
        }
      } else {
        billing.setBldg(tempRecord.getCmrName1Plain());
      }

      if (tempRecord.getCmrStreetAddress() != null && !"".equals(tempRecord.getCmrStreetAddress())) {
        if (tempRecord.getCmrStreetAddress().length() > 18) {
          billing.setDivn(tempRecord.getCmrStreetAddress().substring(0, 18));
        }
      } else {
        billing.setDivn(tempRecord.getCmrStreetAddress());
      }

      if (tempRecord.getCmrCity() != null && !"".equals(tempRecord.getCmrCity())) {
        if (tempRecord.getCmrCity().length() > 12) {
          billing.setCustFax(tempRecord.getCmrStreetAddress().substring(0, 12));
        }
      } else {
        billing.setCustFax(tempRecord.getCmrCity());
      }
      // billing.setBillingPstlAddr("");
    }
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {

    // Italy Company Address Format (temporary)
    // name = Name
    // line1 = Name cont
    // line2 =
    // line3 = Street
    // line4 = Postal code
    // line5 = City and postal code

    String name = this.currentImportValues.get(addressKey + "Name");
    String line1 = this.currentImportValues.get(addressKey + "Address1");
    String line2 = this.currentImportValues.get(addressKey + "Address2");
    String line3 = this.currentImportValues.get(addressKey + "Address3");
    String line4 = this.currentImportValues.get(addressKey + "Address4");
    String line5 = this.currentImportValues.get(addressKey + "Address5");

    // name : is always customer name
    address.setCmrName1Plain(name);

    // line 1: is always customer name con't
    address.setCmrName2Plain(line1);

    // line 3: is street
    address.setCmrStreetAddress(line3);

    // line4: is postalcode
    address.setCmrPostalCode(line4);

    // line5 : is postalcode + City
    address.setCmrCity(line5);

    String landedCountry = address.getCmrCountryLanded();
    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      // set Italy as default
      if (address.getCmrCountryLanded().equals("IT")) {
        address.setCmrCountryLanded(line5);
      } else if (!address.getCmrCountryLanded().equals("IT")) {
        address.setCmrCountryLanded(landedCountry);
      }
    }
    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    // no need here
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    // noop
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    // noop
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    boolean prospectCmrChosen = currentRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(currentRecord.getCmrOrderBlock());
    if (currentRecord.getCmrName1Plain() != null && !"".equals(currentRecord.getCmrName1Plain())) {
      if (currentRecord.getCmrName1Plain().length() > 25) {
        address.setCustNm1(currentRecord.getCmrName1Plain().substring(0, 25));
      } else {
        address.setCustNm1(currentRecord.getCmrName1Plain());
      }
    } else {
      address.setCustNm1(currentRecord.getCmrName1Plain());
    }

    if (currentRecord.getCmrName2Plain() != null && !"".equals(currentRecord.getCmrName2Plain())) {
      if (currentRecord.getCmrName2Plain().length() > 25) {
        address.setCustNm2(currentRecord.getCmrName2Plain().substring(0, 25));
      } else {
        address.setCustNm2(currentRecord.getCmrName2Plain());
      }
    } else {
      address.setCustNm2(currentRecord.getCmrName2Plain());
    }
    address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
    address.setParCmrNo(currentRecord.getParentCMRNo());
    if (prospectCmrChosen) {
      address.setImportInd("N");
    }
    if (BILLING_ADDR_TYPE.equals(address.getId().getAddrType())) {
      LOG.debug("Loading extra fields for Billing address");
      // address.setSapNo(currentRecord.getCmrSapNumber());
      loadBillingAddressExtraFields(address, prospectCmrChosen);
    }

    address.getId().setAddrSeq(currentRecord.getCmrAddrSeq());

    // pad the sequence to 5 to match SOF values
    String addrSeq = currentRecord.getCmrAddrSeq();

    if (addrSeq == null) {
      addrSeq = "1";
      address.getId().setAddrSeq(addrSeq);
    }

    if (addrSeq != null && addrSeq.trim().length() < 5) {
      addrSeq = StringUtils.leftPad(addrSeq, 5, '0');
      address.getId().setAddrSeq(addrSeq);
    }
    // Story 1593951: iERP Site Party ID field should be part of addresses
    if ("U".equals(admin.getReqType()) || "X".equals(admin.getReqType())) {
      address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
    } else if ("C".equals(admin.getReqType()) && !StringUtils.isEmpty(currentRecord.getParentCMRNo())) {
      address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
      if (COMPANY_ADDR_TYPE.equals(address.getId().getAddrType()) || BILLING_ADDR_TYPE.equals(address.getId().getAddrType())) {
        address.setSapNo(currentRecord.getCmrSapNumber());
      }
    }

    if (INSTALLING_ADDR_TYPE.equals(address.getId().getAddrType()) && "C".equals(admin.getReqType())) {
      if (address != null && address.getId().getAddrSeq().trim().length() < 5) {
        addrSeq = StringUtils.leftPad(address.getId().getAddrSeq(), 5, '0');
        address.getId().setAddrSeq(addrSeq);
      }
      // address.getId().setAddrSeq("1");
      address.setImportInd(null);
    }
    if (INSTALLING_ADDR_TYPE.equals(address.getId().getAddrType())) {
      address.setCustPhone("");
      address.setCustFax("");
    }
    if ("IT".equals(currentRecord.getCmrCountryLanded())) {
      address.setStateProv(currentRecord.getCmrState());
    } else {
      address.setStateProv("");
    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    String identClient = this.currentImportValues.get("IdentClient");
    String fiscalCode = this.currentImportValues.get("CodiceFiscale");
    String vat = this.currentImportValues.get("VAT");
    String taxCode = this.currentImportValues.get("IVA");
    String abbrevNm = this.currentImportValues.get("CompanyName");
    String abbrevLoc = this.currentImportValues.get("AbbreviatedLocation");
    String sbo = this.currentImportValues.get("SBO");
    String isuClientTier = this.currentImportValues.get("ISU");
    String modePayment = this.currentImportValues.get("ModeOfPayment");
    String embargo = this.currentImportValues.get("EmbargoCode");
    String inac = this.currentImportValues.get("INAC");
    String enterprise = this.currentImportValues.get("EnterpriseNo");
    String affiliate = this.currentImportValues.get("Affiliate");

    String tipoClinte = null;
    String coddes = null;
    String pec = null;
    String indiemail = null;
    String customerType = null;
    String collectionCd = null;
    String salesRep = this.currentImportValues.get("SR");
    boolean isLD = false;
    CmrtCustExt cExt = null;
    CmrtCust cust = null;
    String processingType = PageManager.getProcessingType(SystemLocation.ITALY, "U");
    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      isLD = true;
      if (!prospectCmrChosen) {
        cExt = this.legacyObjects.getCustomerExt();
        cust = this.legacyObjects.getCustomer();
      }

      if (cExt != null) {
        identClient = cExt.getItIdentClient();
        fiscalCode = cExt.getiTaxCode();
        vat = cExt.getiTaxCode();
        taxCode = cExt.getItIVA();
        tipoClinte = cExt.getTipoCliente();
        coddes = cExt.getCoddes();
        pec = cExt.getPec();
        indiemail = cExt.getIndEmail();
        collectionCd = cExt.getItCodeSSV();
      }
      if (cust != null) {
        // vat = cust.getVat();
        abbrevNm = cust.getAbbrevNm();
        abbrevLoc = cust.getAbbrevLocn();
        sbo = cust.getSbo();
        isuClientTier = cust.getIsuCd();
        modePayment = cust.getModeOfPayment();
        embargo = cust.getEmbargoCd();
        inac = cust.getInacCd();
        salesRep = cust.getSalesRepNo();
        customerType = cust.getCustType();
      }
    }
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      String isu = mainRecord != null && mainRecord.getIsuCode() != null ? mainRecord.getIsuCode() : "";
      String ctc = mainRecord != null && mainRecord.getCmrTier() != null ? mainRecord.getCmrTier() : "";

      data.setIsuCd(isu);
      data.setClientTier(ctc);
    }
    if (sbo != null && sbo.length() == 7) {
      sbo = sbo.substring(1, 3);
    }

    // String collectionCode = this.currentImportValues.get("CollectionCode");

    if (StringUtils.isEmpty(data.getTaxCd1())) {
      // no data from RDc? get SOF
      data.setTaxCd1(fiscalCode);
    }
    if (StringUtils.isEmpty(data.getVat())) {
      // no data from RDc? get SOF
      data.setVat(vat);
    }
    data.setIdentClient(identClient);
    data.setSpecialTaxCd(taxCode);

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) || CmrConstants.REQ_TYPE_SINGLE_REACTIVATE.equals(admin.getReqType())) {
      data.setModeOfPayment(modePayment);
    } else {
      data.setModeOfPayment("");
    }

    // Defect 1492027: The Abbreviated Name (TELX1) field should be blank when
    // Installing address is not there in address tab :Mukesh
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setAbbrevNm("");
      data.setAbbrevLocn("");
      data.setPpsceid("");
    } else {
      data.setAbbrevNm(abbrevNm);
      data.setAbbrevLocn(abbrevLoc);
    }
    // Defect 1474362/1582107: Update Request: not all data imported :Mukesh
    // Collection code getting from Anagrafico2SSVCode
    String collectionCode = "";
    String countryLanded = "";
    for (FindCMRRecordModel result : results.getItems()) {
      if (result.getCmrAddrTypeCode().equals(BILLING_ADDR_TYPE)) {
        String cmrNo = mainRecord.getParentCMRNo();
        countryLanded = result.getCmrCountryLanded();
        ItalyHandler tempHandler = new ItalyHandler();
        FindCMRRecordModel tempRecord = new FindCMRRecordModel();
        tempRecord.setCmrIssuedBy(SystemLocation.ITALY);
        tempRecord.setCmrNum(cmrNo);
        tempHandler.retrieveSOFValues(result);
        // tempHandler.retrieveSOFValues(tempRecord);
        if (StringUtils.isEmpty(data.getSpecialTaxCd())) {
          taxCode = tempHandler.getCurrentValue("Anagrafico2", "IVA");
          data.setSpecialTaxCd(taxCode);
        }
        if (isLD && cExt != null) {
          // collectionCode = cExt.getItCodeSSV();
          data.setSpecialTaxCd(cExt.getItIVA());
        } else {
          collectionCode = tempHandler.getCurrentValue("Anagrafico2", "SSVCode");
          data.setCollectionCd(collectionCode);
        }
        break;
      }
      if (result.getCmrAddrTypeCode().equals(COMPANY_ADDR_TYPE)) {
        // For ZORG addresses
        countryLanded = result.getCmrCountryLanded();
      }
      if (result.getCmrAddrTypeCode().equals(INSTALLING_ADDR_TYPE)) {
        enterprise = result.getCmrEnterpriseNumber();
        affiliate = result.getCmrAffiliate();
      }
    }
    // no data from RDc? get DB2
    data.setEnterprise(!StringUtils.isEmpty(enterprise) ? enterprise : "");
    data.setAffiliate(!StringUtils.isEmpty(affiliate) ? affiliate : "");
    data.setInacCd(!StringUtils.isEmpty(inac) ? inac : "");

    data.setSalesBusOffCd(sbo);
    data.setRepTeamMemberNo(salesRep);

    data.setCollectionCd(!StringUtils.isEmpty(collectionCd) ? collectionCd : "");
    data.setSitePartyId("");

    // check if currentReqEntryModel is not null, check existing values
    if (this.currentReqEntryModel != null && "C".equals(admin.getReqType())) {
      if (!StringUtils.isEmpty(this.currentReqEntryModel.getAbbrevNm())) {
        LOG.debug("Preserving existing Abbreviated Name " + this.currentReqEntryModel.getAbbrevNm());
        data.setAbbrevNm(this.currentReqEntryModel.getAbbrevNm());
      }
      if (!StringUtils.isEmpty(this.currentReqEntryModel.getAbbrevLocn())) {
        LOG.debug("Preserving existing Abbreviated Location " + this.currentReqEntryModel.getAbbrevLocn());
        data.setAbbrevLocn(this.currentReqEntryModel.getAbbrevLocn());
      }
    }

    data.setEmbargoCd(embargo);
    // new IT fields
    data.setIcmsInd(tipoClinte);
    data.setHwSvcsRepTeamNo(coddes);
    data.setEmail2(pec);
    data.setEmail3(indiemail);
    data.setCrosSubTyp(customerType);

    if (!"IT".equals(countryLanded)) {
      data.setTaxCd1("");
      if (!StringUtils.isEmpty(vat) && vat.length() > 2) {
        data.setVat(vat);
      }
    }

    LOG.trace("EmbargoCode: " + data.getEmbargoCd());
    LOG.trace("Ident Client: " + data.getIdentClient());
    LOG.trace("Fiscal Code: " + data.getTaxCd1());
    LOG.trace("Tax Code: " + data.getSpecialTaxCd());
    LOG.trace("VAT: " + data.getVat());
    LOG.trace("Abbrev Name: " + data.getAbbrevNm());
    LOG.trace("Abbrev Loc: " + data.getAbbrevLocn());
    LOG.trace("SBO: " + data.getSalesBusOffCd());
    LOG.trace("SR: " + data.getRepTeamMemberNo());
    LOG.trace("Collection Code: " + data.getCollectionCd());
    LOG.trace("Enterprise: " + data.getEnterprise());
    LOG.trace("Affiliate: " + data.getAffiliate());
    LOG.trace("INAC: " + data.getInacCd());
    LOG.trace("TipoClinte: " + data.getIcmsInd());
    LOG.trace("CODDES: " + data.getHwSvcsRepTeamNo());
    LOG.trace("PEC: " + data.getEmail2());
    LOG.trace("IndirizzoEmail: " + data.getEmail3());
    LOG.trace("customerType: " + data.getCrosSubTyp());
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
    // noop
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCustPrefLang("I");
    if (data.getCustSubGrp() != null && ("BUSPR".equals(data.getCustSubGrp()) || "BUSSM".equals(data.getCustSubGrp())
        || "BUSVA".equals(data.getCustSubGrp()) || "CROBP".equals(data.getCustSubGrp()))) {
      data.setMrcCd("5");
    } else if (data.getCustSubGrp() != null && data.getIsuCd() != null && "34".equals(data.getIsuCd()) && ("COMME".equals(data.getCustSubGrp())
        || "COMSM".equals(data.getCustSubGrp()) || "COMVA".equals(data.getCustSubGrp()) || "CROCM".equals(data.getCustSubGrp()))) {
      data.setMrcCd("M");
    } else {
      data.setMrcCd("2");
    }
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    // noop
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      admin.setDelInd(null);
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    // noop
    // request.setSORTL(data.getSalesBusOffCd());
    String tempSBO = "";
    tempSBO = data.getSalesBusOffCd();
    if (tempSBO != null && !"".equals(tempSBO) && tempSBO.length() == 7) {
      tempSBO = tempSBO.substring(1, 3);
    }
    if (tempSBO != null && !"".equals(tempSBO) && tempSBO.length() == 2) {
      request.setSORTL("2" + tempSBO);
    } else {
      request.setSORTL(data.getSalesBusOffCd());
    }
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
    // noop
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    // Defect 1575335 :Set prefLang, due to It become null in database
    data.setCustPrefLang("I");
    setEngineeringBo(entityManager, data);
    // Story 1596166: MRC M for Commercial scenario & ISU=34
    if ("C".equals(admin.getReqType())) {
      if (data.getCustSubGrp() != null && data.getIsuCd() != null && "34".equals(data.getIsuCd()) && ("COMME".equals(data.getCustSubGrp())
          || "COMSM".equals(data.getCustSubGrp()) || "COMVA".equals(data.getCustSubGrp()) || "CROCM".equals(data.getCustSubGrp()))) {
        data.setMrcCd("M");
      }
    }

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    if (INSTALLING_ADDR_TYPE.equals(addr.getId().getAddrType())) {

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(addr.getId().getReqId());
      Admin admin = entityManager.find(Admin.class, adminPK);

      // this is italy installing, populate abbrev name and location
      LOG.debug("Computing Abbreviated Name/Location for Installing address of Italy. Request " + addr.getId().getReqId());
      if (data != null && admin.getReqType().equals("C")) {
        data.setAbbrevNm(addr.getCustNm1());
        if (data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
          data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
        }

        // Story 1374607 : Mukesh
        data.setAbbrevLocn(addr.getCity1());
        if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
          data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
        }
        LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

      }
      // Defect 1508530: Mukesh
      addr.setCustFax("");
      addr.setDivn("");
      addr.setBldg("");
      addr.setBillingPstlAddr("");
      // addr.setStateProv("");
    }
    if (COMPANY_ADDR_TYPE.equals(addr.getId().getAddrType())) {
      addr.setCustFax("");
      addr.setDivn("");
      addr.setBldg("");
      addr.setBillingPstlAddr("");

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(addr.getId().getReqId());
      Admin admin = entityManager.find(Admin.class, adminPK);
      if ("C".equals(admin.getReqType()) && !"0000C".equals(addr.getId().getAddrSeq())) {
        addr.getId().setAddrSeq("0000C");
      }

    }

    if (BILLING_ADDR_TYPE.equals(addr.getId().getAddrType())) {
      if (StringUtils.isEmpty(addr.getDivn()) && StringUtils.isEmpty(addr.getBldg()) && StringUtils.isEmpty(addr.getCustFax())) {
        // a new create, need to automatically set the abbrev values

        // abbrev name set 19 characters for addrAbbrivateName
        addr.setBldg(addr.getCustNm1());
        if (addr.getBldg().length() > 19) {
          addr.setBldg(addr.getBldg().substring(0, 19));
        }

        // abbrev street
        addr.setDivn(addr.getAddrTxt());
        if (addr.getDivn().length() > 18) {
          addr.setDivn(addr.getDivn().substring(0, 18));
        }

        // abbrev loc
        addr.setCustFax(addr.getCity1());
        if (addr.getCustFax().length() > 12) {
          addr.setCustFax(addr.getCustFax().substring(0, 12));
        }
      }
    }

    setEngineeringBo(entityManager, data);
    // Epic 1849787 : Story 1931715:
    // addEditEPLAddress(entityManager, addr);
  }

  // Defect 1513271: PP: For Update Request, Ident Client is not getting updated
  // in Request Summary
  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getIdentClient(), newData.getIdentClient())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "IdentClient", "-"));
      update.setNewData(newData.getIdentClient());
      update.setOldData(oldData.getIdentClient());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(newData.getModeOfPayment());
      update.setOldData(oldData.getModeOfPayment());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCrosSubTyp(), newData.getCrosSubTyp())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustomerType", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCrosSubTyp(), "CustomerType", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCrosSubTyp(), "CustomerType", cmrCountry));
      results.add(update);
    }
    // CMR-873 :Mukesh
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getHwSvcsRepTeamNo(), newData.getHwSvcsRepTeamNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CodiceDestinatarioUfficio", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getHwSvcsRepTeamNo(), "CodiceDestinatarioUfficio", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getHwSvcsRepTeamNo(), "CodiceDestinatarioUfficio", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getIcmsInd(), newData.getIcmsInd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "TipoCliente", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getIcmsInd(), "TipoCliente", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getIcmsInd(), "TipoCliente", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmail2(), newData.getEmail2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "PEC", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmail2(), "PEC", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmail2(), "PEC", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmail3(), newData.getEmail3())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "IndirizzoEmail", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmail3(), "IndirizzoEmail", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmail3(), "IndirizzoEmail", cmrCountry));
      results.add(update);
    }
  }

  // Defect 1575335: Update: Request summary
  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    // if (BILLING_ADDR_TYPE.equals(addr.getId().getAddrType()) &&
    // !equals(addr.getBillingPstlAddrOld(), addr.getBillingPstlAddr())) {
    // UpdatedNameAddrModel update = new UpdatedNameAddrModel();
    // update.setAddrType(addrTypeDesc);
    // update.setDataField(PageManager.getLabel(cmrCountry, "BillingPstlAddr",
    // "-"));
    // update.setNewData(addr.getBillingPstlAddr());
    // update.setOldData(addr.getBillingPstlAddrOld());
    // results.add(update);
    //
    // }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(ITALY_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    if ("U".equals(admin.getReqType()) || "X".equals(admin.getReqType())) {
      String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", admin.getId().getReqId());
      List<Addr> addresses = query.getResults(Addr.class);
      Addr bAddr = null;
      Addr iAddr = null;

      for (Addr addr : addresses) {
        if ("ZP01".equals(addr.getId().getAddrType()) && StringUtils.isEmpty(addr.getSapNo())) {
          bAddr = addr;
        }
        if ("ZS01".equals(addr.getId().getAddrType())) {
          iAddr = addr;
        }
      }

      if (bAddr != null) {
        // DTN: This means that the billing was imported but not from Rdc
        // bAddr.setSapNo("-");
        if (iAddr != null && iAddr.getParCmrNo().equals(bAddr.getParCmrNo())) {
          bAddr.setChangedIndc("Y");
          entityManager.merge(bAddr);
        }
      }

    }

    /*
     * String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
     * PreparedQuery query = new PreparedQuery(entityManager, sql);
     * query.setParameter("REQ_ID", admin.getId().getReqId()); List<Addr>
     * addresses = query.getResults(Addr.class);
     * 
     * for (Addr addr : addresses) { String sapNo = addr.getSapNo();
     * 
     * try { String spid = "";
     * 
     * if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) { spid =
     * getRDcIerpSitePartyId(sapNo); addr.setIerpSitePrtyId(spid); } else if
     * (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
     * addr.setIerpSitePrtyId(spid); }
     * 
     * 
     * entityManager.merge(addr); entityManager.flush(); } catch (Exception e) {
     * LOG.error("Error occured on setting SPID after import.");
     * e.printStackTrace(); }
     * 
     * }
     */
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

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "ADDR_TXT", "ADDR_TXT2", "DIVN", "CUST_FAX", "POST_CD", "LAND_CNTRY", "BLDG", "STATE_PROV",
        "CITY1", "CITY2"));
    return fields;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return true;
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return true;
  }

  /**
   * Computes for the Engineering Bo value
   * 
   * @param entityManager
   * @param reqId
   * @param data
   */
  private void setEngineeringBo(EntityManager entityManager, Data data) {
    LOG.debug("Computing Engineering BO value..");
    String sql = ExternalizedQuery.getSql("ADDR.GET_ENGGBO_IT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    String engBo = query.getSingleResult(String.class);
    if (engBo == null) {
      engBo = "8412";
    }
    data.setEngineeringBo(engBo);
    LOG.debug("Engineering Bo = " + engBo);
    entityManager.merge(data);
  }

  /**
   * Gets the current installing address on the record
   * 
   * @param entityManager
   * @param reqId
   * @return
   */
  private Addr getCurrentInstallingAddress(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("ITALY.GETINSTALLING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getSingleResult(Addr.class);
  }

  private void copyAddrData(FindCMRRecordModel record, Addr addr) {
    record.setCmrAddrTypeCode(addr.getId().getAddrType());
    record.setCmrAddrSeq(addr.getId().getAddrSeq());
    record.setCmrName1Plain(addr.getCustNm1());
    record.setCmrName2Plain(addr.getCustNm2());
    record.setCmrName3(addr.getCustNm3());
    record.setCmrName4(addr.getCustNm4());
    record.setCmrStreetAddress(addr.getAddrTxt());
    record.setCmrCity(addr.getCity1());
    record.setCmrCity2(addr.getCity2());
    record.setCmrState(addr.getStateProv());
    record.setCmrCountryLanded(addr.getLandCntry());
    record.setCmrCountry(addr.getLandCntry());
    record.setCmrPOBox(addr.getPoBox());
    record.setCmrPostalCode(addr.getPostCd());
    record.setParentCMRNo(addr.getParCmrNo());
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    // map.put("##BillingPstlAddr", "billingPstlAddr");
    map.put("##CMROwner", "cmrOwner");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustLang", "custPrefLang");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
    map.put("##BPRelationType", "bpRelType");
    map.put("##LocalTax1", "taxCd1");
    map.put("##CAP", "capInd");
    map.put("##RequestReason", "reqReason");
    map.put("##SpecialTaxCd", "specialTaxCd");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##AddrAbbrevLocn", "custFax");
    map.put("##VATExempt", "vatExempt");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##City1", "city1");
    map.put("##StateProv", "stateProv");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##VAT", "vat");
    map.put("##CollectionCd", "collectionCd");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##AddrAbbrevName", "bldg");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##CrossbStateProvPostalMapIT", "crossbStateProvPostalMapIT");
    map.put("##Expedite", "expediteInd");
    map.put("##Affiliate", "affiliate");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CrossbCntryStateProvMapIT", "crossbCntryStateProvMapIT");
    map.put("##CountrySubRegion", "countryUse");
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    map.put("##IERPSitePrtyId", "ierpSitePrtyId");
    map.put("##StateProvItalyOth", "city2");
    map.put("##SAPNumber", "sapNo");
    map.put("##Department", "dept");
    map.put("##Company", "company");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##CustomerName1", "custNm1");
    map.put("##IdentClient", "identClient");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName2", "custNm2");
    map.put("##StateProvItaly", "locationCode");
    map.put("##Enterprise", "enterprise");
    map.put("##PostalCode", "postCd");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##StreetAbbrev", "divn");
    map.put("##TipoCliente", "icmsInd");
    return map;
  }

  // Mukesh : Legacy Configuration
  // Story 1904492: IT: CreateCMR should generate Legacy Sequence Style values

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = null;
    return newAddrSeq;

  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newSeq = null;
    return newSeq;
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.ITALY.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01", "ZS01", "ZI01");
    }
    return null;
  }

  @Override
  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  @Override
  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  /*
   * private void addEditEPLAddress(EntityManager entityManager, Addr addr)
   * throws Exception {
   * 
   * Addr eplAddr = getAddressByType(entityManager,
   * CmrConstants.ADDR_TYPE.ZS02.toString(), addr.getId().getReqId()); if
   * (eplAddr == null) { // create epl from installing if not exists Addr
   * installingAddr = null; if
   * (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase
   * (addr.getId().getAddrType())) { installingAddr = addr; } else {
   * installingAddr = getAddressByType(entityManager,
   * CmrConstants.ADDR_TYPE.ZS01.toString(), addr.getId().getReqId()); } if
   * (installingAddr != null) { eplAddr = new Addr(); AddrPK newPk = new
   * AddrPK(); newPk.setReqId(installingAddr.getId().getReqId());
   * newPk.setAddrType(CmrConstants.ADDR_TYPE.ZS02.toString());
   * newPk.setAddrSeq("00001");
   * 
   * PropertyUtils.copyProperties(eplAddr, installingAddr);
   * eplAddr.setImportInd(CmrConstants.YES_NO.N.toString());
   * eplAddr.setSapNo(null); eplAddr.setRdcCreateDt(null); eplAddr.setId(newPk);
   * 
   * entityManager.persist(eplAddr); entityManager.flush(); }
   * 
   * } else if
   * (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId
   * ().getAddrType())) { // update epl when installing is updated
   * eplAddr.setCustNm1(addr.getCustNm1());
   * eplAddr.setCustNm2(addr.getCustNm2());
   * eplAddr.setLandCntry(addr.getLandCntry());
   * eplAddr.setAddrTxt(addr.getAddrTxt()); eplAddr.setCity1(addr.getCity1());
   * eplAddr.setPostCd(addr.getPostCd());
   * 
   * entityManager.merge(eplAddr); entityManager.flush(); } }
   */

  private Addr getAddressByType(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GET.BYTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    query.setForReadOnly(true);
    List<Addr> addrList = query.getResults(1, Addr.class);
    if (addrList != null && addrList.size() > 0) {
      return addrList.get(0);
    }
    return null;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return true;
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "CUST_CLASS", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD",
        "ABBREV_NM", "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID",
        "MEM_LVL", "BP_REL_TYPE", "CROS_SUB_TYP", "TAX_CD1", "NATIONAL_CUS_IND", "MODE_OF_PAYMENT", "ENTERPRISE"));
    return fields;
  }

  private void importCreateDataLD(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, FindCMRRecordModel mainRecord,
      List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {
    String chosenType = searchModel.getAddrType();
    if (!StringUtils.isEmpty(chosenType)) {
      // load the company address first
      String companyCmr = mainRecord.getCmrCompanyNo();
      if (StringUtils.isEmpty(companyCmr)) {
        // try to get CompanyNo from SOF
        LOG.debug("Checking Company No. from SOF..");
        companyCmr = this.currentImportValues.get("CompanyNo");
        mainRecord.setCmrCompanyNo(companyCmr);
      }
      LOG.debug("Company No: " + companyCmr);
      String cmrNo = mainRecord.getCmrNum();
      FindCMRResultModel companySearch = source;
      if (!cmrNo.equals(companyCmr)) {
        LOG.debug("Chosen CMR is not the Company CMR, looking for Company CMR details..");
        companySearch = SystemUtil.findCMRs(companyCmr, SystemLocation.ITALY, 10);
      }
      for (FindCMRRecordModel record : companySearch.getItems()) {
        if (COMPANY_ADDR_TYPE_NEW.equals(record.getCmrAddrTypeCode())) {
          LOG.debug("Loading Company Address from CMR No. " + record.getCmrNum());
          // Defect 1535000: PP: For VA Create, Dummy postal code is null if
          // Company is Imported
          if (!StringUtils.isEmpty(record.getCmrCountryLanded())
              && (record.getCmrCountryLanded().equalsIgnoreCase("SM") || record.getCmrCountryLanded().equalsIgnoreCase("VA"))) {
            record.setCmrPOBoxPostCode("55100");
          }
          record.setCmrAddrTypeCode(COMPANY_ADDR_TYPE);
          record.setParentCMRNo(record.getCmrNum());
          record.setCmrCounty(null);
          converted.add(record);
        }
      }
    }
    if ((BILLING_ADDR_TYPE.equals(chosenType) || INSTALLING_ADDR_TYPE.equals(chosenType)) && "impBill".equals(reqEntry.getLockByNm())) {
      // when installing is chosen, company and billing will be imported
      LOG.debug("Installing was chosen, also importing billing");

      boolean billingFound = false;
      FindCMRRecordModel billing = null;
      // try to get billing from FindCMR
      for (FindCMRRecordModel record : source.getItems()) {
        if (BILLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
          billing = record;
          billing.setParentCMRNo(record.getCmrNum());
          billing.setCmrStreetAddressCont(record.getCmrName3());
          billing.setCmrName3(null);
          billing.setCmrName4(null);
          billing.setCmrCity(record.getCmrCity());
          billing.setCmrPostalCode(record.getCmrPostalCode());
          billing.setCmrState(record.getCmrState());
          billing.setCmrSapNumber(record.getCmrSapNumber());
          billingFound = true;
        }
      }

      if (!billingFound) {
        String billingCmrNo = this.currentImportValues.get("Anagrafico2BillingNo");
        if (StringUtils.isEmpty(billingCmrNo)) {
          // get from SOF/MAN
          billing = new FindCMRRecordModel();
          // setBillingAddressFromLegacy(billing);
          LOG.debug("No separate billing address found, loading billing from Legacy for create type");
          loadBillingAddressDataLD(entityManager, billing, mainRecord);
        } else {
          LOG.debug("No separate billing address found, adding a copy of installing as billing");
          // if billing is not found, add a copy of installing as billing
          billing = new FindCMRRecordModel();
          for (FindCMRRecordModel record : source.getItems()) {
            if (INSTALLING_ADDR_TYPE.equals(record.getCmrAddrTypeCode())) {
              try {
                PropertyUtils.copyProperties(billing, record);
                billing.setCmrAddrTypeCode(BILLING_ADDR_TYPE);
                billing.setParentCMRNo(mainRecord.getCmrNum());
                // billing.setCmrSapNumber(record.getCmrSapNumber());

              } catch (Exception e) {
                // noop
              }
              break;
            }
          }
        }
      }
      reqEntry.setLockByNm("");
      converted.add(billing);
    }

    if (reqEntry.getReqId() > 0) {
      // this is an existing request, check if any installing is present
      LOG.debug("Checking any existing installing address on Request ID " + reqEntry.getReqId());
      Addr installingAddr = getCurrentInstallingAddress(entityManager, reqEntry.getReqId());
      if (installingAddr != null) {
        LOG.debug("Adding installing to the records");
        FindCMRRecordModel installing = new FindCMRRecordModel();
        PropertyUtils.copyProperties(installing, mainRecord);
        copyAddrData(installing, installingAddr);
        installing.setParentCMRNo(mainRecord.getCmrNum());
        converted.add(installing);
        // do a dummy import based on the installing
      }
    }

  }

  private void setBillingAddressFromLegacy(FindCMRRecordModel address) {
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      if ("Y".equals(cmrtAddr.getIsAddrUseBilling())) {
        address.setCmrAddrSeq(cmrtAddr.getId().getAddrNo());
        String line1 = cmrtAddr.getAddrLine1();
        address.setCmrName1Plain(line1);
        address.setCmrStreetAddress(cmrtAddr.getAddrLine4());
        address.setCmrCity(cmrtAddr.getCity());
        address.setCmrPostalCode(cmrtAddr.getZipCode());
        address.setCmrCustPhone(cmrtAddr.getAddrPhone());
      }
    }
  }

  private CmrtAddr getLegacyCompanyAddr(EntityManager entityManager, String cmr) {
    CmrtAddr addrL = null;
    String sql = ExternalizedQuery.getSql("ITALY.GET.COMPANYADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", SystemLocation.ITALY);
    query.setParameter("CMR", cmr);
    addrL = query.getSingleResult(CmrtAddr.class);
    return addrL;
  }

  private CmrtCustExt getBillingCustExtFields(String cmr) {
    EntityManager entityManager = JpaManager.getEntityManager();
    CmrtCustExt cmrtCExt = null;
    if (entityManager != null) {
      String sql = ExternalizedQuery.getSql("ITALY.GET.BILLINGFIELDS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY", SystemLocation.ITALY);
      query.setParameter("CMR", cmr);
      cmrtCExt = query.getSingleResult(CmrtCustExt.class);
    }
    return cmrtCExt;
  }

  @Override
  public boolean isAddressChanged(EntityManager entityManager, Addr addr, String cmrIssuingCntry, boolean computedChangeInd) {
    if (StringUtils.isEmpty(addr.getSapNo())) {
      return true;
    } else {
      return super.isAddressChanged(entityManager, addr, cmrIssuingCntry, computedChangeInd);
    }
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    XSSFRow row = null;
    XSSFCell currCell = null;

    String[] LD_MASS_UPDATE_SHEET_NAMES = { "Data" };

    for (String name : LD_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);

      for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {

        String fiscalCode = ""; // 3
        String vatNumPartitaIVA = ""; // 4
        String taxCodeIVACode = ""; // 5
        String identClient = ""; // 6
        String enterpriseNumber = ""; // 7
        String affiliateNumber = ""; // 8
        String collectionCode = ""; // 9
        String salesRepNo = ""; // 10
        String sbo = ""; // 11
        String inac = ""; // 12
        String tipoCliente = ""; // 13
        String typeOfCustomer = ""; // 14
        String codiceDestUfficio = "";// 15
        String pec = ""; // 16
        String indirizzoEmail = ""; // 17
        String isu = ""; // 18
        String clientTier = ""; // 19

        row = sheet.getRow(rowIndex);
        if (row == null) {
          return; // stop immediately when row is blank
        }
        // iterate all the rows and check each column value
        currCell = row.getCell(3);
        fiscalCode = validateColValFromCell(currCell);

        currCell = row.getCell(4);
        vatNumPartitaIVA = validateColValFromCell(currCell);

        currCell = row.getCell(5);
        taxCodeIVACode = validateColValFromCell(currCell);

        currCell = row.getCell(6);
        identClient = validateColValFromCell(currCell);

        currCell = row.getCell(7);
        enterpriseNumber = validateColValFromCell(currCell);

        currCell = row.getCell(8);
        affiliateNumber = validateColValFromCell(currCell);

        currCell = row.getCell(9);
        collectionCode = validateColValFromCell(currCell);

        currCell = row.getCell(10);
        salesRepNo = validateColValFromCell(currCell);

        currCell = row.getCell(11);
        sbo = validateColValFromCell(currCell);

        currCell = row.getCell(12);
        inac = validateColValFromCell(currCell);

        currCell = row.getCell(13);
        tipoCliente = validateColValFromCell(currCell);

        currCell = row.getCell(14);
        typeOfCustomer = validateColValFromCell(currCell);

        currCell = row.getCell(15);
        codiceDestUfficio = validateColValFromCell(currCell);

        currCell = row.getCell(16);
        pec = validateColValFromCell(currCell);

        currCell = row.getCell(17);
        indirizzoEmail = validateColValFromCell(currCell);

        currCell = row.getCell(18);
        isu = validateColValFromCell(currCell);

        currCell = row.getCell(19);
        clientTier = validateColValFromCell(currCell);

        LOG.debug("Fiscal Code =====> " + fiscalCode);
        LOG.debug("VAT #/ N.PARTITA I.V.A. =====> " + vatNumPartitaIVA);
        LOG.debug("Tax Code/ Code IVA =====> " + taxCodeIVACode);
        LOG.debug("Ident Client =====> " + identClient);
        LOG.debug("Enterprise Number/CODICE ENTERPRISE =====> " + enterpriseNumber);
        LOG.debug("Affiliate Number =====> " + affiliateNumber);
        LOG.debug("Collection Code =====> " + collectionCode);
        LOG.debug("Sales Rep. No. =====> " + salesRepNo);
        LOG.debug("SBO =====> " + sbo);
        LOG.debug("INAC =====> " + inac);
        LOG.debug("Tipo Cliente =====> " + tipoCliente);
        LOG.debug("Type Of Customer =====> " + typeOfCustomer);
        LOG.debug("Codice Destinatario/Ufficio =====> " + codiceDestUfficio);
        LOG.debug("PEC =====> " + pec);
        LOG.debug("Indirizzo Email =====> " + indirizzoEmail);
        LOG.debug("ISU =====> " + isu);
        LOG.debug("Client Tier =====> " + clientTier);

        TemplateValidation error = new TemplateValidation(name);

        if ((!StringUtils.isEmpty(fiscalCode) || !StringUtils.isEmpty(identClient) || !StringUtils.isEmpty(vatNumPartitaIVA)
            || !StringUtils.isEmpty(enterpriseNumber)) && (!StringUtils.isEmpty(taxCodeIVACode) || !StringUtils.isEmpty(collectionCode))) {
          LOG.trace("Company level fields and Billing level fields can not be filled at the same time");
          error.addError((row.getRowNum() + 1),
              "Company [Fiscal code, Vat#, Ident. Cliente, Enterprise number] | Billing [Tax Code/ Code IVA, Collection Code]",
              "Company level fields and Billing level fields can not be filled at the same time");
        }
        if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
          if ((StringUtils.isNotBlank(isu) && StringUtils.isBlank(clientTier)) || (StringUtils.isNotBlank(clientTier) && StringUtils.isBlank(isu))) {
            LOG.trace("The row " + rowIndex + ":Note that both ISU and CTC value needs to be filled..");
            error.addError(rowIndex, "Data Tab", ":Please fill both ISU and CTC value.<br>");
          } else if (!StringUtils.isBlank(isu) && "34".equals(isu)) {
            if (StringUtils.isBlank(clientTier) || !"QY".contains(clientTier)) {
              LOG.trace("The row " + rowIndex
                  + ":Note that Client Tier should be 'Y' or 'Q' for the selected ISU code. Please fix and upload the template again.");
              error.addError(rowIndex, "Client Tier",
                  ":Note that Client Tier should be 'Y' or 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
            }
          } else if ((!StringUtils.isBlank(isu) && !"34".equals(isu)) && !"@".equalsIgnoreCase(clientTier)) {
            LOG.trace("Client Tier should be '@' for the selected ISU Code.");
            error.addError(rowIndex, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isu + ".<br>");
          }
        }

        if (!StringUtils.isBlank(salesRepNo)) {
          if (!StringUtils.isAlphanumeric(salesRepNo)) {
            LOG.trace("Sales Rep Number should have Alphanumeric values only.");
            error.addError((row.getRowNum() + 1), "Sales Rep No.", "Sales Rep Number should have Alphanumeric values only. ");
          }
        }

        if (!StringUtils.isBlank(sbo)) {
          if (!StringUtils.isAlphanumeric(sbo)) {
            LOG.trace("SBO should have Alphanumeric values only.");
            error.addError((row.getRowNum() + 1), "SBO.", "SBO should have Alphanumeric values only. ");
          }
        }

        if (!StringUtils.isBlank(collectionCode)) {
          Pattern upperCaseNumeric = Pattern.compile("^[A-Z0-9]*$");
          Matcher matcher = upperCaseNumeric.matcher(collectionCode);
          if (!matcher.matches()) {
            LOG.trace("Collection Code should contain only upper-case latin and numeric characters.");
            error.addError((row.getRowNum() + 1), "Collection Code.",
                "Collection Code should contain only upper-case latin and numeric characters. ");
          }
        }

        if (error.hasErrors()) {
          validations.add(error);
        }
      }
    }
  }

  private void createMissingAddressForSingleReact(FindCMRResultModel source, List<FindCMRRecordModel> converted, FindCMRRecordModel mainRecord) {
    FindCMRRecordModel record = source.getItems().get(0);

    // Copy Installing address as Company address.
    FindCMRRecordModel company = new FindCMRRecordModel();
    try {
      PropertyUtils.copyProperties(company, record);
      company.setCmrAddrType("Company");
      company.setCmrAddrTypeCode(COMPANY_ADDR_TYPE);
      company.setCmrAddrSeq(COMPANY_ADDR_SEQ_NO);
      company.setParentCMRNo(mainRecord.getCmrCompanyNo());
      converted.add(company);
    } catch (Exception e) {
      // noop
    }

    // Copy Installing address as billing address.
    FindCMRRecordModel billing = new FindCMRRecordModel();
    try {
      PropertyUtils.copyProperties(billing, record);
      billing.setCmrAddrType("Billing");
      billing.setCmrAddrTypeCode(BILLING_ADDR_TYPE);
      billing.setCmrSapNumber(null);
      billing.setCmrAddrSeq("00002");
      converted.add(billing);
    } catch (Exception e) {
      // noop
    }
  }
}
