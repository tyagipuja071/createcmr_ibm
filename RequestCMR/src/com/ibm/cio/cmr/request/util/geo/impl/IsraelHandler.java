/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
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
import com.ibm.cio.cmr.request.util.SystemLocation;
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
          record.setCmrPOBox("PO BOX " + record.getCmrPOBox());
        }

        if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
          record.setCmrAddrSeq("00001");
        } else {
          record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
        }

        String pairedAddrSeq = record.getCmrAddrSeq();
        List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
        CmrtAddr legacyAddr = getLegacyAddrByAddrPair(legacyAddrList, pairedAddrSeq);

        FindCMRRecordModel addr = cloneAddress(record, "ZS01");

        converted.add(mapEnglishAddr(addr, legacyAddr));

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

      if (!SystemLocation.ITALY.equalsIgnoreCase(data.getCmrIssuingCntry())) {

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
          if ("U".equals(admin.getReqType()) && !StringUtils.isEmpty(this.currentImportValues.get("COD"))) {
            data.setCreditCd(this.currentImportValues.get("COD"));
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

      } else { // Story 1389065: SBO and Sales rep auto-population : Mukesh

        String collCd = this.currentImportValues.get("CollectionCode");
        String sr = this.currentImportValues.get("SR");
        String sBO = this.currentImportValues.get("SBO");

        // Story 1374616: Requirement for INAC / NAC :Mukesh
        String inac = data.getInacCd();
        if ((inac != null || !StringUtils.isEmpty(inac)) && inac.length() > 4) {
          data.setInacCd(inac.substring(0, 4));
        }
        // Checking Company CMR
        if (((!StringUtils.isEmpty(data.getCompany())) && (!StringUtils.isEmpty(data.getCmrNo())))
            && (data.getCompany().equalsIgnoreCase(data.getCmrNo()))) {
          LOG.debug("It is Company CMR (Node1 equal to CMR No):: " + data.getCompany() + " equal to " + data.getCmrNo());

          if (mainRecord.getCmrSortl() != null && mainRecord.getCmrSortl().length() >= 10) {
            data.setSalesBusOffCd(mainRecord.getCmrSortl().substring(1, 3)); // lenght2
            data.setRepTeamMemberNo(mainRecord.getCmrSortl().substring(4, 10)); // lenght6
          }
          // data.setCollectionCd(StringUtils.isEmpty(collCd) ? "" :
          // collCd.substring(0, 5)); // lenght5

        } else {
          LOG.debug("It is not Company CMR (Node1 not equal to CMR No): " + data.getCompany() + " not equal to " + data.getCmrNo());

          if (!StringUtils.isEmpty(data.getIsuCd()) && !"32".equalsIgnoreCase(data.getIsuCd().substring(0, 2))) {
            data.setRepTeamMemberNo("");
            data.setSalesBusOffCd("");
          }

        }

        String iBO = this.currentImportValues.get("IBO");
        String eBo = this.currentImportValues.get("DPCEBO");

        if (!(StringUtils.isEmpty(iBO))) {
          if (iBO.length() > 3) {
            data.setInstallBranchOff(iBO.substring(0, 3));
          }
        }
        if (!(StringUtils.isEmpty(eBo))) {
          if (eBo.length() > 3) {
            data.setEngineeringBo(eBo.substring(0, 3));
          }
        }
        // Story 1374607 : Mukesh
        String abbrerLocn = this.currentImportValues.get("AbbreviatedLocation");
        if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
          if (!StringUtils.isEmpty(abbrerLocn)) {
            data.setAbbrevLocn(abbrerLocn);
            if (abbrerLocn != null && abbrerLocn.length() > 12) {
              data.setAbbrevLocn(abbrerLocn.substring(0, 12));
            }
          } else {
            data.setAbbrevLocn("");
          }
        }
      } // End of Story 1389065
    } else {
      super.setDataValuesOnImport(admin, data, results, mainRecord);
    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (currentRecord != null) {
        String country = currentRecord.getCmrIssuedBy();

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

        address.setTaxOffice(currentRecord.getCmrTaxOffice());
        address.setVat(currentRecord.getCmrTaxNumber());
        if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
          if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
              && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
            String seq = address.getId().getAddrSeq();
            seq = StringUtils.leftPad(seq, 5, '0');
            address.getId().setAddrSeq(seq);
          }
          if ("D".equals(address.getImportInd())) {
            String seq = StringUtils.leftPad(address.getId().getAddrSeq(), 5, '0');
            address.getId().setAddrSeq(seq);
          }

          if (!"ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
            address.setCustPhone("");
          }
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

      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
        update = new UpdatedDataModel();
        update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
        update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
        update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
        results.add(update);
      } else {
        super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);
      }
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
      String newAddrSeq = null;
      return newAddrSeq;
    } else {
      return super.generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    }
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      String newSeq = null;
      return newSeq;
    } else {
      return super.generateModifyAddrSeqOnCopy(entityManager, addrType, reqId, oldAddrSeq, cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return Arrays.asList("ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "CTYA", "CTYB", "CTYC");
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
      return Arrays.asList("ZD01", "CTYC");
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
        "BP_REL_TYPE"));
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
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      XSSFRow row = null;
      XSSFCell currCell = null;

      String[] countryAddrss = null;

      countryAddrss = LD_MASS_UPDATE_SHEET_NAMES;

      for (String name : countryAddrss) {
        XSSFSheet sheet = book.getSheet(name);
        for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {

          String cbCity = ""; // 8
          String localCity = ""; // 7
          String cbPostal = ""; // 10
          String localPostal = ""; // 9

          String streetCont = ""; // 5
          String poBox = ""; // 11
          String attPerson = ""; // 13

          String district = "";// 12
          String taxOffice = ""; // 13
          String name4 = "";// 10

          row = sheet.getRow(rowIndex);
          if (row == null) {
            return; // stop immediately when row is blank
          }
          // iterate all the rows and check each column value
          currCell = row.getCell(6);
          localCity = validateColValFromCell(currCell);
          currCell = row.getCell(7);
          cbCity = validateColValFromCell(currCell);
          currCell = row.getCell(8);
          localPostal = validateColValFromCell(currCell);
          currCell = row.getCell(9);
          cbPostal = validateColValFromCell(currCell);

          TemplateValidation error = new TemplateValidation(name);

          // CMR-2731 Turkey: Mass Update: country modification
          if (SystemLocation.TURKEY.equals(country)) {
            currCell = row.getCell(12);
            district = validateColValFromCell(currCell);
            currCell = row.getCell(13);
            taxOffice = validateColValFromCell(currCell);
            currCell = row.getCell(5);
            streetCont = validateColValFromCell(currCell);
            currCell = row.getCell(10);
            name4 = validateColValFromCell(currCell);

            if (!StringUtils.isEmpty(cbCity) && !StringUtils.isEmpty(localCity)) {
              LOG.trace("Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty. >> ");
              error.addError(rowIndex, "Local City",
                  "Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty.");
              validations.add(error);
            }

            if (!StringUtils.isEmpty(cbPostal) && !StringUtils.isEmpty(localPostal)) {
              LOG.trace("Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                  + "If one is populated, the other must be empty. >>");
              error.addError(rowIndex, "Local Postal Code", "Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                  + "If one is populated, the other must be empty.");
              validations.add(error);
            }

            if (!StringUtils.isEmpty(name4) && !StringUtils.isEmpty(streetCont)) {
              LOG.trace("Name4 and Street Cont must not be populated at the same time. " + "If one is populated, the other must be empty. >>");
              error.addError(rowIndex, "Name4",
                  "Name4 and Street Cont must not be populated at the same time. " + "If one is populated, the other must be empty.");
              validations.add(error);
            }

            if ((!StringUtils.isEmpty(localCity) || !StringUtils.isEmpty(localPostal))) {
              if ("@".equals(district)) {
                LOG.trace("Local address must not be populate District with @. ");
                error.addError(rowIndex, "District", "Local address must not be populate District with @. ");
                validations.add(error);
              }

              if ("@".equals(taxOffice)) {
                LOG.trace("Local address must not be populate Tax Office with @. ");
                error.addError(rowIndex, "Tax Office", "Local address must not be populate Tax Office with @. ");
                validations.add(error);
              }
            }
          } else {
            currCell = row.getCell(5);
            streetCont = validateColValFromCell(currCell);
            currCell = row.getCell(11);
            poBox = validateColValFromCell(currCell);
            currCell = row.getCell(12);
            attPerson = validateColValFromCell(currCell);
            // DTN: Defect 1898300: UKI - mass updates - addresses
            /*
             * Adding a check that if any of the address lines values that are
             * set as either value and both are filled out, it will throw an
             * error message that both can not be filled out.
             */
            if (!StringUtils.isEmpty(cbCity) && !StringUtils.isEmpty(localCity)) {
              LOG.trace("Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty. >> ");
              error.addError(rowIndex, "Local City",
                  "Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty.");
              validations.add(error);
            }

            if (!StringUtils.isEmpty(cbPostal) && !StringUtils.isEmpty(localPostal)) {
              LOG.trace("Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                  + "If one is populated, the other must be empty. >>");
              error.addError(rowIndex, "Local Postal Code", "Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                  + "If one is populated, the other must be empty.");
              validations.add(error);
            }
            if ((!StringUtils.isEmpty(cbCity) || !StringUtils.isEmpty(cbPostal))
                && (!StringUtils.isEmpty(localCity) || !StringUtils.isEmpty(localPostal))) {
              // if local
              if (!StringUtils.isEmpty(streetCont) && !StringUtils.isEmpty(poBox)) {
                LOG.trace("Note that Street Con't/PO Box cannot be filled at same time. Please fix and upload the template again.");
                error.addError(rowIndex, "Street Con't/PO Box",
                    "Note that Street Con't/PO Box cannot be filled at same time. Please fix and upload the template again.");
                validations.add(error);
              } else if (!StringUtils.isEmpty(poBox) && !StringUtils.isEmpty(attPerson)) {
                LOG.trace("Note that PO Box/ATT Person cannot be filled at same time. Please fix and upload the template again.");
                error.addError(rowIndex, "PO Box/ATT Person",
                    "Note that PO Box/ATT Person cannot be filled at same time. Please fix and upload the template again.");
                validations.add(error);
              } else if (!StringUtils.isEmpty(attPerson) && !StringUtils.isEmpty(streetCont)) {
                LOG.trace("Note that ATT Person/Street Con't cannot be filled at same time. Please fix and upload the template again.");
                error.addError(rowIndex, "ATT Person/Street Con't",
                    "Note that ATT Person/Street Con't cannot be filled at same time. Please fix and upload the template again.");
                validations.add(error);
              }
            } else {
              // else cross border
              if (!StringUtils.isEmpty(streetCont) && !StringUtils.isEmpty(poBox)) {
                LOG.trace("Note that Street Con't/PO Box cannot be filled at same time. Please fix and upload the template again.");
                error.addError(rowIndex, "Street Con't/PO Box",
                    "Note that Street Con't/PO Box cannot be filled at same time. Please fix and upload the template again.");
                validations.add(error);
              }
            }
          }
        }
      }
    } else {
      super.validateMassUpdateTemplateDupFills(validations, book, maxRows, country);
    }
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

}