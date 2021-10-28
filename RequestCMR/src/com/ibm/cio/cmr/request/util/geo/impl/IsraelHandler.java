/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
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
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Import Converter for Israel
 * 
 * @author Jeffrey Zamora
 * 
 */
public class IsraelHandler extends EMEAHandler {

  private static final Logger LOG = Logger.getLogger(IsraelHandler.class);

  private static Map<String, List<List<String>>> MASS_UPDT_DUP_ENTRY_MAP = new HashMap<String, List<List<String>>>();

  private static final String[] ISRAEL_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode", "LocalTax1",
      "LocalTax2", "SearchTerm", "SitePartyID", "StreetAddress2", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office",
      "Floor", "Building", "County", "City2", "CustomerName2" };

  private static final List<String> EMEA_COUNTRY_VAL = Arrays.asList(SystemLocation.UNITED_KINGDOM, SystemLocation.IRELAND, SystemLocation.ISRAEL,
      SystemLocation.TURKEY, SystemLocation.GREECE, SystemLocation.CYPRUS, SystemLocation.ITALY);

  public static final String EMEA_MASSCHANGE_TEMPLATE_ID = "EMEA";

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  protected static final String[] LD_MASS_UPDATE_SHEET_NAMES = { "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address (Update)", "EPL Address" };

  private static final String CUSTGRP_LOCAL = "LOCAL";
  private static final int MANDATORY_ADDR_COUNT = 8;
  private static final int MAX_ADDR_SEQ = 99999;

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.UNITED_KINGDOM, "GB");
    LANDED_CNTRY_MAP.put(SystemLocation.IRELAND, "IE");

    if (MASS_UPDT_DUP_ENTRY_MAP == null) {
      MASS_UPDT_DUP_ENTRY_MAP = new HashMap<String, List<List<String>>>();
    }

    // DTN: build the pattern list
    List<List<String>> patternsTop = new ArrayList<List<String>>();
    List<String> dupPatterns = new ArrayList<String>();
    dupPatterns = Arrays.asList(new String[] { "Street Con't", "Att. Person" });
    patternsTop.add(dupPatterns);
    dupPatterns = Arrays.asList(new String[] { "Street Con't", "Att. Person", "PO Box" });
    patternsTop.add(dupPatterns);

    MASS_UPDT_DUP_ENTRY_MAP.put("local", patternsTop);

    patternsTop = new ArrayList<List<String>>();
    dupPatterns = Arrays.asList(new String[] { "Street Con't", "PO Box" });
    patternsTop.add(dupPatterns);

    MASS_UPDT_DUP_ENTRY_MAP.put("crossb", patternsTop);

  }

  @Override
  public void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, FindCMRRecordModel mainRecord,
      List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    LOG.info("IsraelHandler processing type: " + processingType);
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // LD Implementation
      if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
        // only add zs01 equivalent for create by model
        FindCMRRecordModel record = mainRecord;
        // name4 in rdc = Attn on SOF
        record.setCmrDept(record.getCmrName4());
        record.setCmrName4(null);
        // name3 in rdc = Address Con't on SOF
        record.setCmrStreetAddressCont(record.getCmrName3());
        record.setCmrName3(null);

        if (!StringUtils.isBlank(record.getCmrPOBox())) {
          record.setCmrPOBox(record.getCmrPOBox());
        }

        if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
          record.setCmrAddrSeq("00001");
        } else {
          record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
        }

        boolean isProspects = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
        if (!isProspects) {
          String pairedAddrSeq = record.getCmrAddrSeq();
          List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
          CmrtAddr legacyAddr = getLegacyAddrByAddrPair(legacyAddrList, pairedAddrSeq);

          FindCMRRecordModel addr = cloneAddress(record, "ZS01");

          converted.add(mapEnglishAddr(addr, legacyAddr));
        } else {
          record.setCmrAddrSeq("00006");
          record.setCmrAddrTypeCode("CTYA");
          converted.add(record);
        }
      } else {
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
                  addr.setCmrStreetAddressCont(record.getCmrName3());
                  addr.setCmrName3(null);
                  addr.setCmrName2Plain(!StringUtils.isEmpty(record.getCmrName2Plain()) ? record.getCmrName2Plain() : record.getCmrName4());

                  if (!StringUtils.isBlank(record.getCmrPOBox())) {
                    addr.setCmrPOBox(record.getCmrPOBox());
                  }

                  if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                    addr.setCmrAddrSeq("00001");
                  }

                  if ("ZS01".equals(addrType) || "ZP01".equals(addrType) || "ZD01".equals(addrType)) {
                    addr.setCmrAddrTypeCode(record.getCmrAddrTypeCode());

                    String pairedAddrSeq = addr.getCmrAddrSeq();
                    List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
                    CmrtAddr legacyAddr = getLegacyAddrByAddrPair(legacyAddrList, pairedAddrSeq);

                    if ("ZS01".equals(addrType)) {
                      record.setCmrCustPhone(mainRecord.getCmrCustPhone());
                    }

                    converted.add(mapEnglishAddr(addr, legacyAddr));
                    converted.add(mapLocalLanguageAddr(record, legacyAddr));

                  } else {
                    converted.add(addr);
                  }
                }
              }
            }
          }
        }
      }
    } else {
      super.handleSOFConvertFrom(entityManager, source, reqEntry, mainRecord, converted, searchModel);
    }
  }

  // TODO: Local language mapping - waiting for final rdc sadr mapping
  private FindCMRRecordModel mapLocalLanguageAddr(FindCMRRecordModel record, CmrtAddr legacyAddr)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    FindCMRRecordModel localLangAddr = new FindCMRRecordModel();
    PropertyUtils.copyProperties(localLangAddr, record);
    localLangAddr.setCmrName1Plain(record.getCmrIntlName1());
    localLangAddr.setCmrName2Plain(record.getCmrIntlName2());
    localLangAddr.setCmrStreetAddress(record.getCmrIntlAddress());
    localLangAddr.setCmrStreetAddressCont(record.getCmrIntlName3());
    localLangAddr.setCmrCity(record.getCmrIntlCity1());

    return localLangAddr;
  }

  private FindCMRRecordModel mapEnglishAddr(FindCMRRecordModel addr, CmrtAddr legacyAddr) {
    if ("ZS01".equals(addr.getCmrAddrTypeCode())) {
      addr.setCmrAddrTypeCode("CTYA");
    } else if ("ZP01".equals(addr.getCmrAddrTypeCode())) {
      addr.setCmrAddrTypeCode("CTYB");
    } else if ("ZD01".equals(addr.getCmrAddrTypeCode())) {
      addr.setCmrAddrTypeCode("CTYC");
    }

    addr.setCmrAddrSeq(legacyAddr.getId().getAddrNo());
    addr.setTransAddrNo(legacyAddr.getAddrLineO());

    return addr;
  }

  private CmrtAddr getLegacyAddrByAddrPair(List<CmrtAddr> legacyAddrList, String pairedAddrSeq) {
    CmrtAddr legacyAddr = legacyAddrList.stream().filter(a -> pairedAddrSeq.equals(a.getAddrLineO())).findAny().orElse(null);
    return legacyAddr;
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD setting
    } else {
      super.handleSOFAddressImport(entityManager, cmrIssuingCntry, address, addressKey);
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD setting
    } else {
      super.setAdminValuesOnImport(admin, currentRecord);
    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      super.setDataValuesOnImport(admin, data, results, mainRecord);

      data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
      LOG.trace("EmbargoCode: " + data.getEmbargoCd());

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        data.setPpsceid("");
        boolean isProspects = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
        if (isProspects) {
          data.setCmrNo("");
        }
      }

      // defect 1299146
      if (mainRecord.getCmrSortl() != null && mainRecord.getCmrSortl().length() >= 10) {
        data.setSalesBusOffCd(mainRecord.getCmrSortl().substring(0, 3));
        LOG.trace("SBO from Sortl : " + data.getSalesBusOffCd());
        if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
          data.setRepTeamMemberNo(mainRecord.getCmrSortl().substring(4));
          LOG.trace("Rep No from Sortl (IL) : " + data.getRepTeamMemberNo());
        } else {
          // defect fix 1329919 changed from 5 to 4
          data.setRepTeamMemberNo(mainRecord.getCmrSortl().substring(4, 10));
          LOG.trace("Rep No from Sortl : " + data.getRepTeamMemberNo());
        }
      }
      // 1299146
      // Translate and auto-populate for next release
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && StringUtils.isEmpty(data.getCollectionCd())) {
        data.setCollectionCd("TC0");
      }

      // Changed abbreviated location if cross border to country
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        if (!this.currentImportValues.isEmpty() && !(mainRecord.getCmrCountryLanded().equalsIgnoreCase("IL"))) {
          String country = DropdownListController.getDescription("LandedCountry", mainRecord.getCmrCountryLanded(), SystemLocation.ISRAEL);
          if (!StringUtils.isEmpty(country) && admin.getReqType() == "C") {
            data.setAbbrevLocn(country.length() > 12 ? country.substring(0, 12) : country);
          }
        }

      }

      // For IBO, SBO & EBO values exceeding 3 char length
      String iBO = this.currentImportValues.get("IBO");
      String sBO = this.currentImportValues.get("SBO");
      String eBo = this.currentImportValues.get("DPCEBO");
      String accAdBo = this.currentImportValues.get("AccAdBo");

      if (!(StringUtils.isEmpty(iBO))) {
        if (iBO.length() > 3) {
          data.setInstallBranchOff(iBO.substring(0, 3));
        }
      }
      if (!(StringUtils.isEmpty(sBO))) {
        if (sBO.length() > 3) {
          data.setSalesBusOffCd(sBO.substring(0, 3));
        }
      }
      if (!(StringUtils.isEmpty(eBo))) {
        if (eBo.length() > 3) {
          data.setEngineeringBo(eBo.substring(0, 3));
        }
      }

      if (StringUtils.isNotBlank(data.getCmrNo())) {
        String kunnrExtCapInd = getZS01CapInd(data.getCmrNo());
        if (StringUtils.isNotEmpty(kunnrExtCapInd)) {
          data.setCapInd(kunnrExtCapInd);
        }
      }

      if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        if (StringUtils.isNotEmpty(mainRecord.getCmrDuns())) {
          data.setDunsNo(mainRecord.getCmrDuns());
        } else { // manually query KNA1.zzkv_duns
          EntityManager entityManager = JpaManager.getEntityManager();
          String sql = ExternalizedQuery.getSql("IL.GET.KNA1_ZS01DUNS");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          query.setParameter("ZZKV_CUSNO", data.getCmrNo());

          List<String> record = query.getResults(String.class);
          if (record != null && !record.isEmpty()) {
            data.setDunsNo(record.get(0));
          }
        }

        String codflag = !StringUtils.isEmpty(legacyObjects.getCustomer().getDeptCd()) ? legacyObjects.getCustomer().getDeptCd() : "";
        data.setCreditCd(codflag);
      }
    } else {
      super.setDataValuesOnImport(admin, data, results, mainRecord);
    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (currentRecord != null) {

        address.setCustNm1(currentRecord.getCmrName1Plain());
        address.setCustNm2(currentRecord.getCmrName2Plain());
        address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
        // no addr std for israel
        if ("IL".equals(currentRecord.getCmrCountryLanded()) && (SystemLocation.ISRAEL.equals(currentRecord.getCmrIssuedBy())
            || SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(currentRecord.getCmrIssuedBy()))) {
          address.setAddrStdResult("X");
        }

        address.setPairedAddrSeq(currentRecord.getTransAddrNo());
        address.setVat(currentRecord.getCmrTaxNumber());
        // set tax office here
        address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());

        address.setTaxOffice(currentRecord.getCmrTaxOffice());
        address.setVat(currentRecord.getCmrTaxNumber());
        if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
            && "CTYA".equalsIgnoreCase(address.getId().getAddrType())) {
          boolean isProspects = currentRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(currentRecord.getCmrOrderBlock());
          if (isProspects) {
            address.getId().setAddrSeq("00006");
          } else {
            address.getId().setAddrSeq("00001");
          }
        }
        if ("D".equals(address.getImportInd())) {
          String seq = StringUtils.leftPad(address.getId().getAddrSeq(), 5, '0');
          address.getId().setAddrSeq(seq);
        }

        if (!"ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
          address.setCustPhone("");
        }
      }
    } else {
      super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);
    }

  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD here
    } else {
      super.setAdminDefaultsOnCreate(admin);
    }
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        data.setCustPrefLang("B");
        data.setCollectionCd("TC0");
        data.setSpecialTaxCd("AA");
      } else {
        data.setCustPrefLang("E");
      }
      data.setCmrOwner("IBM");
    } else {
      super.setDataDefaultsOnCreate(data, entityManager);
    }

  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD here
    } else {
      super.appendExtraModelEntries(entityManager, mv, model);
    }
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        admin.setDelInd(null);
      }
    } else {
      super.handleImportByType(requestType, admin, data, importing);
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (!StringUtils.isEmpty(data.getRepTeamMemberNo())) {
        switch (data.getCmrIssuingCntry()) {
        case SystemLocation.ISRAEL:
          request.setSORTL(data.getSalesBusOffCd() + "-" + data.getRepTeamMemberNo());
          break;
        }
      }
    } else {
      super.convertCoverageInput(entityManager, request, mainAddr, data);
    }
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD here
    } else {
      super.doBeforeAdminSave(entityManager, admin, cmrIssuingCntry);
    }
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        // Auto-populate SBO, IBO field based on Sales rep number
        // jz: this is an israel requirement, so adding check
        if (data.getRepTeamMemberNo() != null && data.getRepTeamMemberNo().length() > 0) {
          StringBuffer sboIBO = new StringBuffer("00");

          char[] salesRepCode = data.getRepTeamMemberNo().toCharArray();
          int salesRepIndex = -1;

          for (int i = 0; i < salesRepCode.length; i++) {
            if (Character.isDigit(salesRepCode[i]) && salesRepCode[i] != '0') {
              salesRepIndex = i;
              break;
            }
          }

          if (salesRepIndex >= 0) {
            sboIBO.append(salesRepCode[salesRepIndex]);
            data.setSalesBusOffCd(sboIBO.toString());
            data.setInstallBranchOff(sboIBO.toString());
          }
        }

        if (data.getSubIndustryCd() != null) {
          data.setEconomicCd("0" + data.getSubIndustryCd());
        }

        if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          String scenario = !StringUtils.isEmpty(data.getCustSubGrp()) ? data.getCustSubGrp() : "";
          String kukla = !StringUtils.isEmpty(data.getCustClass()) ? data.getCustClass() : "";
          String kuklaVal = "";

          if (!StringUtils.isEmpty(scenario)) {
            switch (scenario) {
            case "GOVRN":
              kuklaVal = "13";
              break;
            case "INTER":
              kuklaVal = "81";
              break;
            case "INTSO":
              kuklaVal = "85";
              break;
            case "PRIPE":
              kuklaVal = "60";
              break;
            case "THDPT":
              kuklaVal = "11";
              break;
            default:
              kuklaVal = kukla;
              break;
            }
          }
          if (!StringUtils.isBlank(kuklaVal)) {
            data.setCustClass(kuklaVal);
          }
        } else {
          // UPDATE
          DataRdc rdcData = null;
          rdcData = LegacyDirectUtil.getOldData(entityManager, String.valueOf(data.getId().getReqId()));

          if (rdcData != null) {
            if (StringUtils.isEmpty(data.getCustClass()) && !StringUtils.isEmpty(rdcData.getCustClass())) {
              data.setCustClass(rdcData.getCustClass());
            }
          }
        }

        if (StringUtils.isEmpty(data.getCapInd())) {
          data.setCapInd("N");
        }
      }
    } else {
      super.doBeforeDataSave(entityManager, admin, data, cmrIssuingCntry);
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {

      DataPK pk = new DataPK();
      pk.setReqId(addr.getId().getReqId());
      Data data = entityManager.find(Data.class, pk);

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(addr.getId().getReqId());
      Admin admin = entityManager.find(Admin.class, adminPK);

      if (!"ZS01".equals(addr.getId().getAddrType())) {
        addr.setCustPhone("");
      }

      lockLandedCountry(entityManager, data, admin, addr);
      assignPairedSequence(entityManager, addr, admin.getReqType());

      switch (cmrIssuingCntry) {
      case SystemLocation.ISRAEL:
        if ("CTYA".equals(addr.getId().getAddrType())) {
          // this is translated mailing, populate abbrev name and location
          LOG.debug("Computing Abbreviated Name/Location for Country Use A address of Israel. Request " + addr.getId().getReqId());
          if (data != null && admin.getReqType().equals("C")) {
            data.setAbbrevNm(addr.getCustNm1());
            if (data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
              data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
            }
            LOG.debug("- Abbreviated Name: " + data.getAbbrevNm());
            data.setAbbrevLocn(addr.getCity1());
            if (!"IL".equals(addr.getLandCntry())) {
              // for cross-border, use country name
              String country = DropdownListController.getDescription("LandedCountry", addr.getLandCntry(), SystemLocation.ISRAEL);
              if (!StringUtils.isEmpty(country)) {
                data.setAbbrevLocn(country.length() > 12 ? country.substring(0, 12) : country);
              }
            }
            if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
              data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
            }
            LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

            entityManager.merge(data);
            entityManager.flush();
          }
        }
        // Using above logic to change abbrvLoc to country for cross border
        if (!"IL".equals(addr.getLandCntry()) && !("CTYA".equals(addr.getId().getAddrType()))) {
          if (data != null && admin.getReqType().equals("C")) {
            String country = DropdownListController.getDescription("LandedCountry", addr.getLandCntry(), SystemLocation.ISRAEL);
            if (!StringUtils.isEmpty(country)) {
              data.setAbbrevLocn(country.length() > 12 ? country.substring(0, 12) : country);
            }
            entityManager.flush();
          }
        }
        break;
      }
    } else {
      super.doBeforeAddrSave(entityManager, addr, cmrIssuingCntry);
    }
  }

  private void assignPairedSequence(EntityManager entityManager, Addr addr, String reqType) {

    if ("C".equals(reqType)) {
      assignPairedSeqForNewAddr(entityManager, addr);
    } else if ("U".equals(reqType)) {
      if ("Y".equals(addr.getImportInd())) {

        AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
        addr.setPairedAddrSeq(addrRdc.getPairedAddrSeq());

      } else if ("N".equals(addr.getImportInd())) {
        assignPairedSeqForNewAddr(entityManager, addr);
      }
    }
  }

  private void assignPairedSeqForNewAddr(EntityManager entityManager, Addr addr) {
    if (StringUtils.isEmpty(addr.getPairedAddrSeq()) && "CTYC".equals(addr.getId().getAddrType())) {

      Addr existingRecord = getAddrByAddrSeq(entityManager, addr.getId().getReqId(), "CTYC", addr.getId().getAddrSeq());
      // Prevent overwriting the assigned paired sequence when Copying in create
      // request
      if (existingRecord != null) {
        addr.setPairedAddrSeq(existingRecord.getPairedAddrSeq());
      } else {
        String pairedShipping = getLatestShipping(entityManager, addr.getId().getReqId());
        addr.setPairedAddrSeq(pairedShipping);
      }
    } else {
      addr.setPairedAddrSeq("");
    }
  }

  private String getLatestShipping(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("GET.ADDRSEQ.LATEST.BY_REQID_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", "ZD01");
    List<String> results = query.getResults(String.class);
    if (!results.isEmpty() && !results.get(0).isEmpty()) {
      return results.get(0);
    }
    return null;
  }

  private void lockLandedCountry(EntityManager entityManager, Data data, Admin admin, Addr addr) {
    boolean isLocalScenario = CUSTGRP_LOCAL.equals(data.getCustGrp());
    boolean isMailingOrTranslated = "CTYA".equals(addr.getId().getAddrType()) || "ZS01".equals(addr.getId().getAddrType());
    // local scenario - mailing and translated mailing lock landed country
    // to israel
    if ("C".equals(admin.getReqType()) && isLocalScenario && isMailingOrTranslated) {
      addr.setLandCntry("IL");
    } else if ("U".equals(admin.getReqType())) {
      String marker = addr.getBldg();
      // in Update requests, Mailing and Country Use A must be locked upon
      // import and not editable for both requester and processor.
      if (isMailingOrTranslated) {
        if ("SUPERUSER".equals(marker)) {
          addr.setBldg("");
        } else if ("Y".equals(addr.getImportInd())) {
          AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
          addr.setLandCntry(addrRdc.getLandCntry());
        }
      }
    }
  }

  @Override
  public void doBeforeDPLCheck(EntityManager entityManager, Data data, List<Addr> addresses) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // No DPL check for non-latin addresses
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        for (Addr addr : addresses) {
          if (Arrays.asList("ZS01", "ZP01", "ZD01").contains(addr.getId().getAddrType())) {
            addr.setDplChkResult("N");
          }
        }
      }
    } else {
      super.doBeforeDPLCheck(entityManager, data, addresses);
    }
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      UpdatedDataModel update = null;
      super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

      // Type of Customer
      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())) {
        update = new UpdatedDataModel();
        update.setDataField(PageManager.getLabel(cmrCountry, "CustClass", "-"));
        update.setNewData(newData.getCustClass());
        update.setOldData(oldData.getCustClass());
        results.add(update);
      }
    } else {
      super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return Arrays.asList(ISRAEL_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    } else {
      super.skipOnSummaryUpdate(cntry, field);
    }
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
        data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
        entityManager.merge(data);
        entityManager.flush();
      }
    } else {
      super.doAfterImport(entityManager, admin, data);
    }
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "ADDR_TXT", "DEPT", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX", "CUST_PHONE"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && "C".equals(admin.getReqType())) {
        String abbrevNmValue = null;
        String abbrevLocnValue = null;

        String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", data.getId().getReqId());
        query.setParameter("ADDR_TYPE", "CTYA");
        List<String> record = query.getResults(String.class);
        if (record != null && !record.isEmpty()) {
          abbrevNmValue = record.get(0);
        }

        if (abbrevNmValue != null && abbrevNmValue.length() > 22) {
          abbrevNmValue = abbrevNmValue.substring(0, 22);
        }

        data.setAbbrevNm(abbrevNmValue);

        String sql2 = ExternalizedQuery.getSql("QUERY.ADDR.GET.CITY1.BY_REQID_ADDRTYP");
        PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
        query2.setParameter("REQ_ID", data.getId().getReqId());
        query2.setParameter("ADDR_TYPE", "CTYA");
        List<String> results = query2.getResults(String.class);
        if (results != null && !results.isEmpty()) {
          abbrevLocnValue = results.get(0);
        }

        if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
          abbrevLocnValue = abbrevLocnValue.substring(0, 12);
        }
        data.setAbbrevLocn(abbrevLocnValue);
        entityManager.merge(data);
        entityManager.flush();
      }
    } else {
      super.createOtherAddressesOnDNBImport(entityManager, admin, data);
    }
  }

  @Override
  protected String getAddressTypeByUse(String addressUse) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      switch (addressUse) {
      case "1":
        return "ZS01";
      case "2":
        return "ZP01";
      case "3":
        return "ZI01";
      case "4":
        return "ZD01";
      case "5":
        return "ZS02";
      case "A":
        return "CTYA";
      case "B":
        return "CTYB";
      case "C":
        return "CTYC";
      }
      return null;
    } else {
      return super.getAddressTypeByUse(addressUse);
    }
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      String addrSeqNum = null;

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(reqId);
      Admin admin = entityManager.find(Admin.class, adminPK);

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        addrSeqNum = getAddrSeqForCreate(entityManager, reqId, addrType);
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        addrSeqNum = getAddrSeqForUpdate(entityManager, reqId);
      }
      return addrSeqNum;
    } else {
      return super.generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    }
  }

  private String getAddrSeqForCreate(EntityManager entityManager, long reqId, String addrType) {
    int candidateSeqNum = 0;
    String addrSeqNum = null;
    if (!StringUtils.isEmpty(addrType)) {
      candidateSeqNum = getFixedSequenceForMandatoryAddresses(addrType);
      addrSeqNum = getAvailableAddrSeqNum(entityManager, reqId, candidateSeqNum);
    }

    return addrSeqNum;
  }

  private String getAddrSeqForUpdate(EntityManager entityManager, long reqId) {
    String addrSeqNum = null;
    addrSeqNum = getAvailAddrSeqNumInclRdc(entityManager, reqId);

    return addrSeqNum;
  }

  private String getAvailableAddrSeqNum(EntityManager entityManager, long reqId, int candidateSeqNum) {
    int availSeqNum = 0;
    Set<Integer> allAddrSeq = getAllSavedSeqFromAddr(entityManager, reqId);
    if (allAddrSeq.contains(candidateSeqNum)) {
      availSeqNum = MANDATORY_ADDR_COUNT + 1;
      while (allAddrSeq.contains(availSeqNum)) {
        availSeqNum++;
      }
    } else {
      availSeqNum = candidateSeqNum;
    }
    return String.format("%05d", availSeqNum);
  }

  private String getAvailAddrSeqNumInclRdc(EntityManager entityManager, long reqId) {
    DataPK pk = new DataPK();
    pk.setReqId(reqId);
    Data data = entityManager.find(Data.class, pk);

    String cmrNo = data.getCmrNo();
    Set<Integer> allAddrSeqFromAddr = getAllSavedSeqFromAddr(entityManager, reqId);
    Set<Integer> allAddrSeqFromRdc = getAllSavedSeqFromRdc(entityManager, cmrNo);

    Set<Integer> mergedAddrSet = new HashSet<>();
    mergedAddrSet.addAll(allAddrSeqFromAddr);
    mergedAddrSet.addAll(allAddrSeqFromRdc);

    int candidateSeqNum = Collections.max(mergedAddrSet) + 1;

    if (candidateSeqNum > MAX_ADDR_SEQ) {
      candidateSeqNum = 1;
    }

    int availSeqNum = 0;
    if (mergedAddrSet.contains(candidateSeqNum)) {
      availSeqNum = candidateSeqNum;
      while (mergedAddrSet.contains(availSeqNum)) {
        availSeqNum++;
        if (availSeqNum > MAX_ADDR_SEQ) {
          availSeqNum = 1;
        }
      }
    } else {
      availSeqNum = candidateSeqNum;
    }

    return String.format("%05d", availSeqNum);
  }

  private int getFixedSequenceForMandatoryAddresses(String addrType) {
    int addrSeq = 0;
    // fixed sequence for mandatory addresses
    if ("ZS01".equals(addrType)) {
      addrSeq = 1;
    } else if ("ZP01".equals(addrType)) {
      addrSeq = 2;
    } else if ("ZI01".equals(addrType)) {
      addrSeq = 3;
    } else if ("ZD01".equals(addrType)) {
      addrSeq = 4;
    } else if ("ZS02".equals(addrType)) {
      addrSeq = 5;
    } else if ("CTYA".equals(addrType)) {
      addrSeq = 6;
    } else if ("CTYB".equals(addrType)) {
      addrSeq = 7;
    } else if ("CTYC".equals(addrType)) {
      addrSeq = 8;
    }

    return addrSeq;
  }

  private Set<Integer> getAllSavedSeqFromAddr(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("GET.ADDRSEQ.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Integer> results = query.getResults(Integer.class);

    Set<Integer> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(results);

    return addrSeqSet;
  }

  private Set<Integer> getAllSavedSeqFromRdc(EntityManager entityManager, String cmrNo) {
    String sql = ExternalizedQuery.getSql("GET.KNA1_ZZKV_SEQNO_DISTINCT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", SystemLocation.ISRAEL);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<Integer> resultsRDC = query.getResults(Integer.class);
    Set<Integer> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(resultsRDC);

    return addrSeqSet;
  }

  private String getZS01CapInd(String cmrNo) {
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("IL.GET.KUNNREXT_CAPIND");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", SystemLocation.ISRAEL);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<String> record = query.getResults(String.class);
    String zs01CapInd = null;
    if (record != null && !record.isEmpty()) {
      zs01CapInd = record.get(0);
    }

    return zs01CapInd;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      String newAddrSeq = "";
      newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
      return newAddrSeq;
    } else {
      return super.generateModifyAddrSeqOnCopy(entityManager, addrType, reqId, oldAddrSeq, cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getMandtAddrTypeForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getOptionalAddrTypeForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getAdditionalAddrTypeForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getReservedSeqForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {

      Map<String, String> map = new HashMap<String, String>();
      map.put("##OriginatorName", "originatorNm");
      map.put("##SensitiveFlag", "sensitiveFlag");
      map.put("##INACType", "inacType");
      map.put("##ISU", "isuCd");
      map.put("##CMROwner", "cmrOwner");
      map.put("##SalesBusOff", "salesBusOffCd");
      map.put("##PPSCEID", "ppsceid");
      map.put("##CustLang", "custPrefLang");
      map.put("##GlobalBuyingGroupID", "gbgId");
      map.put("##CoverageID", "covId");
      map.put("##OriginatorID", "originatorId");
      map.put("##BPRelationType", "bpRelType");
      map.put("##CAP", "capInd");
      map.put("##RequestReason", "reqReason");
      map.put("##SpecialTaxCd", "specialTaxCd");
      map.put("##POBox", "poBox");
      map.put("##LandedCountry", "landCntry");
      map.put("##CMRIssuingCountry", "cmrIssuingCntry");
      map.put("##INACCode", "inacCd");
      map.put("##CustPhone", "custPhone");
      map.put("##VATExempt", "vatExempt");
      map.put("##CustomerScenarioType", "custGrp");
      map.put("##City1", "city1");
      map.put("##StateProv", "stateProv");
      map.put("##InternalDept", "ibmDeptCostCenter");
      map.put("##RequestingLOB", "requestingLob");
      map.put("##AddrStdRejectReason", "addrStdRejReason");
      map.put("##ExpediteReason", "expediteReason");
      map.put("##VAT", "vat");
      map.put("##CollectionCd", "collectionCd");
      map.put("##CMRNumber", "cmrNo");
      map.put("##Subindustry", "subIndustryCd");
      map.put("##EnterCMR", "enterCMRNo");
      map.put("##DisableAutoProcessing", "disableAutoProc");
      map.put("##Expedite", "expediteInd");
      map.put("##Affiliate", "affiliate");
      map.put("##BGLDERule", "bgRuleId");
      map.put("##ProspectToLegalCMR", "prospLegalInd");
      map.put("##ClientTier", "clientTier");
      map.put("##AbbrevLocation", "abbrevLocn");
      map.put("##SalRepNameNo", "repTeamMemberNo");
      map.put("##SAPNumber", "sapNo");
      map.put("##Department", "dept");
      map.put("##StreetAddress2", "addrTxt2");
      map.put("##Company", "company");
      map.put("##StreetAddress1", "addrTxt");
      map.put("##AbbrevName", "abbrevNm");
      map.put("##EmbargoCode", "embargoCd");
      map.put("##CustomerName1", "custNm1");
      map.put("##ISIC", "isicCd");
      map.put("##CustomerName2", "custNm2");
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
      map.put("##EngineeringBo", "engineeringBo");
      map.put("##CodFlag", "creditCd");
      map.put("##CommercialFinanced", "commercialFinanced");
      map.put("##CustClass", "custClass");
      map.put("##TypeOfCustomer", "crosSubTyp");
      return map;
    } else {
      return super.getUIFieldIdMap();
    }

  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD", "ABBREV_NM",
        "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL",
        "BP_REL_TYPE", "CUST_CLASS"));
    return fields;
  }

  @Override
  public boolean isTemplateFieldForDualMarked(String columnName, boolean isCrossBorder) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      boolean isDualMarked = false;
      // row.get
      // DTN: We need to remember what was set for the other fields,
      ArrayList<List<String>> dupPattern = null;

      if (columnName != null) {
        if (isCrossBorder) {
          dupPattern = (ArrayList<List<String>>) MASS_UPDT_DUP_ENTRY_MAP.get("crossb");
        } else {
          dupPattern = (ArrayList<List<String>>) MASS_UPDT_DUP_ENTRY_MAP.get("local");
        }

        for (int i = 0; i < dupPattern.size(); i++) {
          List<String> patterns = dupPattern.get(i);

          if (patterns.contains(columnName)) {
            isDualMarked = true;
            break;
          }
        }
      }
      return isDualMarked;
    } else {
      return super.isTemplateFieldForDualMarked(columnName, isCrossBorder);
    }
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return true;
    } else {
      return super.isNewMassUpdtTemplateSupported(issuingCountry);
    }
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    String[] sheetNames = { "Data", "Mailing", "Billing", "Installing", "Shipping", "EPL", "Country Use A (Mailing)", "Country Use B (Billing)",
        "Country Use C (Shipping)" };
    XSSFRow row = null;

    for (String name : sheetNames) {
      XSSFSheet sheet = book.getSheet(name);
      LOG.debug("validating for sheet " + name);
      if (sheet != null) {
        TemplateValidation error = new TemplateValidation(name);
        for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
          row = sheet.getRow(rowIndex);
          if (row == null) {
            rowIndex = maxRows;
          }

          if (rowIndex != maxRows) {
            // Validate CMR No
            String cmrNo = ""; // 0
            cmrNo = validateColValFromCell(row.getCell(0));
            if (StringUtils.isBlank(cmrNo)) {
              error.addError(rowIndex, "<br>CMR No.", "CMR number is required.");
            } else if (isDivCMR(cmrNo, country)) {
              error.addError(rowIndex, "<br>CMR No.",
                  "Note the entered CMR number is either cancelled, divestiture or doesn't exist. Please check the template and correct.");
            }

            if (!name.equals("Data")) {
              // Validate Addr Sequence No
              String addrSeqNo = ""; // 1
              addrSeqNo = validateColValFromCell(row.getCell(1));
              if (StringUtils.isNotBlank(cmrNo) && StringUtils.isBlank(addrSeqNo)) {
                error.addError(rowIndex, "<br>Address Seq. No.", "Address Sequence No is required.");
              }

              String localCity = ""; // 5
              String cbCity = ""; // 6
              String localPostalCode = ""; // 7
              String cbPostalCode = ""; // 8
              // validate local and cross-border city
              localCity = validateColValFromCell(row.getCell(5));
              cbCity = validateColValFromCell(row.getCell(6));
              if (StringUtils.isNotBlank(localCity) && StringUtils.isNotBlank(cbCity)) {
                error.addError(rowIndex, "<br>Local City",
                    "Local City and Cross Border City must not be populated at the same time. If one is populated, the other must be empty.");
              }

              // validate Local and cross-border postal code
              localPostalCode = validateColValFromCell(row.getCell(7));
              cbPostalCode = validateColValFromCell(row.getCell(8));
              if (StringUtils.isNotBlank(localPostalCode) && StringUtils.isNotBlank(cbPostalCode)) {
                error.addError(rowIndex, "<br>Local Postal Code",
                    "Local Postal Code and Cross Border Postal Code must not be populated at the same time. "
                        + "If one is populated, the other must be empty.");
              }

              // validate hebrew addresses
              if (name.equals("Mailing") || name.equals("Billing") || name.equals("Shipping")) {
                // customer name
                String custName = ""; // 2
                if (isHebrewFieldNotBlank(row.getCell(2))) {
                  custName = row.getCell(2).getRichStringCellValue().getString();
                  if (!containsHebrewChar(custName)) {
                    error.addError(rowIndex, "<br>Customer Name", name + " address Customer Name must be in Hebrew.");
                  }
                }

                // customer name con't
                String custNameCont = ""; // 3
                if (isHebrewFieldNotBlank(row.getCell(3))) {
                  custNameCont = row.getCell(3).getRichStringCellValue().getString();
                  if (!containsHebrewChar(custNameCont)) {
                    error.addError(rowIndex, "<br>Customer Name Continuation", name + " address Customer Name Continuation must be in Hebrew.");
                  }
                }

                // Street
                String street = ""; // 4
                if (isHebrewFieldNotBlank(row.getCell(4))) {
                  street = row.getCell(4).getRichStringCellValue().getString();
                  if (!containsHebrewChar(street)) {
                    error.addError(rowIndex, "<br>Street", name + " address Street must be in Hebrew.");
                  }
                }

                // Local City
                if (isHebrewFieldNotBlank(row.getCell(5))) {
                  localCity = row.getCell(5).getRichStringCellValue().getString();
                  if (!containsHebrewChar(localCity)) {
                    error.addError(rowIndex, "<br>Local City", name + " address Local City must be in Hebrew.");
                  }
                }

                // Cross-border City
                if (isHebrewFieldNotBlank(row.getCell(6))) {
                  cbCity = row.getCell(6).getRichStringCellValue().getString();
                  if (!containsHebrewChar(cbCity)) {
                    error.addError(rowIndex, "<br>Cross Border City", name + " address Cross Border City must be in Hebrew.");
                  }
                }
              }
            }
          }
        }

        if (error.hasErrors()) {
          validations.add(error);
        }

      }
    }
    // compare sheet with translation
    // Mailing vs Country Use A
    compareAddressSheets(book.getSheet(sheetNames[1]), book.getSheet(sheetNames[6]), maxRows, validations);
    // Billing vs Country Use B
    compareAddressSheets(book.getSheet(sheetNames[2]), book.getSheet(sheetNames[7]), maxRows, validations);
    // Shipping vs Country Use C
    compareAddressSheets(book.getSheet(sheetNames[4]), book.getSheet(sheetNames[8]), maxRows, validations);

  }

  private static boolean isDivCMR(String cmrNo, String cntry) {
    boolean isDivestiture = true;
    String mandt = SystemConfiguration.getValue("MANDT");
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("IL.GET.ZS01KATR10");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("KATR6", cntry);
    query.setParameter("MANDT", mandt);
    query.setParameter("CMR", cmrNo);

    Kna1 zs01 = query.getSingleResult(Kna1.class);
    if (zs01 != null) {
      if (StringUtils.isBlank(zs01.getKatr10())) {
        isDivestiture = false;
      }
    }
    return isDivestiture;
  }

  private boolean containsHebrewChar(String str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (Character.UnicodeBlock.HEBREW.equals(Character.UnicodeBlock.of(str.codePointAt(i)))) {
        return true;
      }
    }
    return false;
  }

  private boolean isHebrewFieldNotBlank(XSSFCell cell) {
    boolean isHebrewFieldNotBlank = false;
    if (cell != null && cell.getRichStringCellValue() != null && StringUtils.isNotBlank(cell.getRichStringCellValue().getString())) {
      isHebrewFieldNotBlank = true;
    }
    return isHebrewFieldNotBlank;
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD Here
    } else {
      super.addSummaryUpdatedFieldsForAddress(service, cmrCountry, addrTypeDesc, sapNumber, addr, results, entityManager);
    }
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD Here
    } else {
      super.handleSOFSequenceImport(records, cmrIssuingCntry);
    }
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return true;
    } else {
      return super.hasChecklist(cmrIssiungCntry);
    }
  }

  private void compareAddressSheets(XSSFSheet sheet1, XSSFSheet sheet2, int maxRows, List<TemplateValidation> validations) {
    if (sheet1 != null && sheet2 != null) {
      int firstRow1 = sheet1.getFirstRowNum();
      TemplateValidation error = new TemplateValidation(sheet1.getSheetName() + " - " + sheet2.getSheetName());
      for (int i = firstRow1 + 1; i <= maxRows; i++) {
        XSSFRow rowA = sheet1.getRow(i);
        XSSFRow rowB = sheet2.getRow(i);
        if (rowA == null && rowB == null) {
          i = maxRows;
          break;
        }

        if (!compareTwoRows(rowA, rowB, error)) {
          error.addError(i, "<br>Mismatch",
              "Same fields needs to be filled for both " + sheet1.getSheetName() + " and " + sheet2.getSheetName() + " address.");
        }
      }
      if (error.hasErrors()) {
        validations.add(error);
      }
    }
  }

  private boolean compareTwoRows(XSSFRow rowA, XSSFRow rowB, TemplateValidation error) {
    boolean isRowEqual = true;
    if (rowA == null && rowB == null) {
      return isRowEqual;
    } else if ((rowA != null && rowB == null) || (rowA == null && rowB != null)) {
      return false;
    } else if (rowA != null && rowB != null) {
      // compare CMR No if same
      String cmrNoA = validateColValFromCell(rowA.getCell(0));
      String cmrNoB = validateColValFromCell(rowB.getCell(0));
      if ((StringUtils.isNotBlank(cmrNoA) && StringUtils.isNotBlank(cmrNoB) && !cmrNoA.equals(cmrNoB))
          || (StringUtils.isBlank(cmrNoA) && StringUtils.isNotBlank(cmrNoB)) || (StringUtils.isNotBlank(cmrNoA) && StringUtils.isBlank(cmrNoB))) {
        error.addError(rowA.getRowNum(), "<br>CMR No.", "CMR No. does not match.");
        return false;
      }

      // Iterate other fields if filled-out the same
      int lastCell = rowA.getLastCellNum();
      for (int i = 1; i <= lastCell; i++) {
        String currCellA = validateColValFromCell(rowA.getCell(i));
        String currCellB = validateColValFromCell(rowB.getCell(i));
        if ((StringUtils.isNotBlank(currCellA) && StringUtils.isBlank(currCellB))
            || (StringUtils.isBlank(currCellA) && StringUtils.isNotBlank(currCellB))) {
          return false;
        }
      }
    }

    return isRowEqual;
  }

  private Addr getAddrByAddrSeq(EntityManager entityManager, long reqId, String addrType, String addrSeq) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    query.setParameter("ADDR_SEQ", addrSeq);
    List<Addr> addrList = query.getResults(Addr.class);

    if (!addrList.isEmpty()) {
      return addrList.get(0);
    }
    return null;
  }

}