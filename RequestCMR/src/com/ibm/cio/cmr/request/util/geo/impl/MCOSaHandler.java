/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
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
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;

/**
 * Handler for MCO South Africa
 * 
 * @author Eduard Bernardo
 * 
 */
public class MCOSaHandler extends MCOHandler {

  protected static final Logger LOG = Logger.getLogger(MCOSaHandler.class);

  protected static final String[] ZA_MASS_UPDATE_SHEET_NAMES = { "Data", "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address", "EPL Address" };

  private static final String[] SA_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CAP", "ModeOfPayment", "TransportZone", "POBoxCity", "POBoxPostalCode" };

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;

      if (!StringUtils.isEmpty(record.getCmrName4())) {
        // name4 in rdc is street con't
        if (record.getCmrName4() != null && (!record.getCmrName4().startsWith("ATT") && !record.getCmrName4().startsWith("ATT: "))) {
          record.setCmrStreetAddressCont(record.getCmrName4());
        }
        record.setCmrName4(null);
      }

      // name3 in rdc = attn on SOF
      if (!StringUtils.isEmpty(record.getCmrName3())) {
        if (record.getCmrName3().startsWith("ATT") || record.getCmrName3().startsWith("ATT: ")) {
          record.setCmrName4(record.getCmrName3());
        }
        record.setCmrName3(null);
      }

      if (!StringUtils.isBlank(record.getCmrPOBox())) {
        record.setCmrPOBox(record.getCmrPOBox());
      }
      if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
        record.setCmrAddrSeq("00001");
      } else {
        record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
      }

      if (prospectCmrChosen && "ZS01".equals(record.getCmrAddrTypeCode())) {
        LOG.debug("Mailing Address prospect CMR No. " + record.getCmrNum());
        record.setCmrAddrSeq("00001");
      }

      record.setCmrName2Plain(record.getCmrName2Plain());
      record.setCmrTaxOffice(this.currentImportValues.get("InstallingAddressT"));
      record.setCmrDept(null);

      if (StringUtils.isEmpty(record.getCmrCustPhone())) {
        record.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
      }
      converted.add(record);
    } else {

      String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
        List<String> importedAddr = new ArrayList<String>();
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
                  if (record.getCmrName4() != null && (!record.getCmrName4().startsWith("ATT") && !record.getCmrName4().startsWith("ATT: "))) {
                    addr.setCmrStreetAddressCont(record.getCmrName4());
                  }
                  addr.setCmrName3(null);
                  if (record.getCmrName3() != null && (record.getCmrName3().startsWith("ATT") || record.getCmrName3().startsWith("ATT: "))) {
                    addr.setCmrName4(record.getCmrName3());
                  }

                  if (!StringUtils.isBlank(record.getCmrPOBox())) {
                    String poBox = record.getCmrPOBox().trim();
                    addr.setCmrPOBox(LegacyCommonUtil.doFormatPoBox(poBox));
                  }

                  if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                    addr.setCmrAddrSeq("00001");
                  }
                  converted.add(addr);
                  importedAddr.add(addrType);
                }
              }
            }
          }

        }

        // add the missing records
        if (mainRecord != null && StringUtils.isNotBlank(mainRecord.getCmrNum()) && mainRecord.getCmrNum().startsWith("99")) {
          FindCMRRecordModel record = null;
          Map<String, FindCMRRecordModel> addrMap = new HashMap<String, FindCMRRecordModel>();
          // parse the rdc records
          String cmrCountry = mainRecord != null ? mainRecord.getCmrIssuedBy() : "";
          importOtherSOFAddressesLD(entityManager, cmrCountry, addrMap, converted, importedAddr);
        }
      } else {

        // import process:
        // a. Import ZS01 record from RDc, only 1
        // b. Import Installing addresses from RDc if found
        // c. Import EplMailing from RDc, if found. This will also be an
        // installing in RDc
        // d. Import all shipping, fiscal, and mailing from SOF

        // customer phone is in BillingPhone
        if (StringUtils.isEmpty(mainRecord.getCmrCustPhone())) {
          mainRecord.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
        }

        Map<String, FindCMRRecordModel> zi01Map = new HashMap<String, FindCMRRecordModel>();

        // parse the rdc records
        String cmrCountry = mainRecord != null ? mainRecord.getCmrIssuedBy() : "";

        if (source.getItems() != null) {
          for (FindCMRRecordModel record : source.getItems()) {

            if (!CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())) {
              LOG.trace("Non Sold-to will be ignored. Will get from SOF");
              this.rdcShippingRecords.add(record);
              continue;
            }

            if (!StringUtils.isEmpty(record.getCmrName4())) {
              // name4 in rdc is street con't
              record.setCmrStreetAddressCont(record.getCmrName4());
              record.setCmrName4(null);
            }

            // name3 in rdc = attn on SOF
            if (!StringUtils.isEmpty(record.getCmrName3())) {
              record.setCmrName4(record.getCmrName3());
              record.setCmrName3(null);
            }

            if (!StringUtils.isBlank(record.getCmrPOBox())) {
              record.setCmrPOBox("PO BOX " + record.getCmrPOBox());
            }

            if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
              record.setCmrAddrSeq("00001");
            } else {
              record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
            }

            converted.add(record);

          }
        }

        // add the missing records
        if (mainRecord != null) {

          FindCMRRecordModel record = null;

          // import all shipping from SOF
          List<String> sequences = this.shippingSequences;
          if (sequences != null && !sequences.isEmpty()) {
            LOG.debug("Shipping Sequences is not empty. Importing " + sequences.size() + " shipping addresses.");
            for (String seq : sequences) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping_" + seq + "_", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }
          } else {
            LOG.debug("Shipping Sequences is empty. ");
            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping", zi01Map);
            if (record != null) {
              converted.add(record);
            }
          }

          importOtherSOFAddresses(entityManager, cmrCountry, zi01Map, converted);
        }
      }
    }
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    String line1 = getCurrentValue(addressKey, "Address1");
    String line2 = getCurrentValue(addressKey, "Address2");
    String line3 = getCurrentValue(addressKey, "Address3");
    String line4 = getCurrentValue(addressKey, "Address4");
    String line5 = getCurrentValue(addressKey, "Address5");
    String line6 = getCurrentValue(addressKey, "Address6");

    if (StringUtils.isEmpty(line6) || "-/X".equalsIgnoreCase(line6)) {
      // for old format, use existing parser
      handleSOFAddressImportOLD(entityManager, cmrIssuingCntry, address, addressKey);
      return;
    }
    String countryCd = getCountryCode(entityManager, line6);

    boolean crossBorder = !StringUtils.isEmpty(countryCd);
    if (crossBorder) {
      // Cross-border - ZA
      // line2 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line3 = Street + PO BOX
      // line4 = City
      // line5 = Postal Code
      // line6 = State (Country)

      address.setCmrName1Plain(line1);

      handlePhoneAndAttn(line2, cmrIssuingCntry, address, addressKey);

      handleStreetContAndPoBox(line3, cmrIssuingCntry, address, addressKey);
      address.setCmrStreetAddress(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);

      handleCityAndPostCode(line4, cmrIssuingCntry, address, addressKey);

      if (!StringUtils.isEmpty(line5)) {
        if (StringUtils.isEmpty(address.getCmrPostalCode()) && !StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrPostalCode(line5);
        } else if (!StringUtils.isEmpty(address.getCmrPostalCode()) && StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrCity(line5);
        }

      }
      address.setCmrCountryLanded(countryCd);

    } else {
      // Domestic - ZA
      // line2 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line3 = Street
      // line4 = Street Con't + PO BOX
      // line5 = City
      // line6 = Postal Code

      address.setCmrName1Plain(line1);

      handlePhoneAndAttn(line2, cmrIssuingCntry, address, addressKey);

      address.setCmrStreetAddress(line3);

      handleStreetContAndPoBox(line4, cmrIssuingCntry, address, addressKey);

      handleCityAndPostCode(line5, cmrIssuingCntry, address, addressKey);

      if (!StringUtils.isEmpty(line6)) {
        if (StringUtils.isEmpty(address.getCmrPostalCode()) && !StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrPostalCode(line6);
        } else if (!StringUtils.isEmpty(address.getCmrPostalCode()) && StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrCity(line6);
        }

      }

    }

    if (StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrStreetAddress(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);
    }

    if (StringUtils.isEmpty(address.getCmrCity()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrCity(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);
    }

    if (!StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddress())
        && isStreet(address.getCmrStreetAddressCont()) && !isStreet(address.getCmrStreetAddress())) {
      // interchange street and street con't based on data
      String cont = address.getCmrStreetAddressCont();
      address.setCmrStreetAddressCont(address.getCmrStreetAddress());
      address.setCmrStreetAddress(cont);
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());

  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);
    boolean ifUpdt = false;
    ifUpdt = "U".equals(admin.getReqType());

    if (legacyObjects != null && legacyObjects.getCustomer() != null) {
      data.setCrosSubTyp(legacyObjects.getCustomer().getCustType());
      data.setSpecialTaxCd(legacyObjects.getCustomer().getTaxCd());
      data.setIbmDeptCostCenter(legacyObjects.getCustomer().getDeptCd());
    }

    if (legacyObjects != null && legacyObjects.getCustomer() != null) {
      if (legacyObjects.getCustomer().getAbbrevLocn().length() > 12) {
        data.setAbbrevLocn(legacyObjects.getCustomer().getAbbrevLocn().substring(0, 12));
      } else {
        data.setAbbrevLocn(legacyObjects.getCustomer().getAbbrevLocn());
      }
    }

    // if (StringUtils.isNotBlank(mainRecord.getCmrCity())) {
    // data.setAbbrevLocn(mainRecord.getCmrCity().length() > 12 ?
    // mainRecord.getCmrCity().substring(0, 12) : mainRecord.getCmrCity());
    // }

    if (ifUpdt && legacyObjects != null && legacyObjects.getCustomerExt() != null) {
      String collBo = legacyObjects.getCustomerExt().getTeleCovRep();
      if (StringUtils.isNotEmpty(collBo) && collBo.length() > 4) {
        collBo = collBo.substring(2, 6);
      }
      data.setCollBoId(collBo);
    }

    if (ifUpdt) {
      String mop = legacyObjects.getCustomer().getModeOfPayment();
      if (StringUtils.isNotBlank(mop) && ("R".equals(mop) || "S".equals(mop) || "T".equals(mop))) {
        data.setCommercialFinanced(mop);
        data.setCreditCd("N");
      } else if (StringUtils.isNotBlank(mop) && "5".equals(mop)) {
        data.setCreditCd("Y");
        data.setCommercialFinanced("");
      } else {
        data.setCreditCd("");
        data.setCommercialFinanced("");
      }

      data.setIbmDeptCostCenter(getInternalDepartment(mainRecord.getCmrNum()));
      data.setAdminDeptLine(data.getIbmDeptCostCenter());
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setPpsceid("");
    }

  }

  private String getInternalDepartment(String cmrNo) throws Exception {
    String department = "";
    List<String> results = new ArrayList<String>();

    EntityManager entityManager = JpaManager.getEntityManager();
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.DEPT.KNA1.BYCMR");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + "864" + "'");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      department = results.get(0);
    }
    return department;
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "Mailing";
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
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    if (currentRecord != null) {
      address.setCustNm1(currentRecord.getCmrName1Plain());
      address.setCustNm2(currentRecord.getCmrName2Plain());

      if (StringUtils.isBlank(address.getCustNm4())) {
        address.setCustNm4(currentRecord.getCmrName4());
      }

      if (StringUtils.isBlank(address.getAddrTxt2())) {
        address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
      }

      if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
          && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
        address.getId().setAddrSeq("00001");
      }

      if ("D".equals(address.getImportInd())) {
        String seq = StringUtils.leftPad(address.getId().getAddrSeq(), 5, '0');
        address.getId().setAddrSeq(seq);
      }

      if ("U".equals(admin.getReqType())) {
        address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
      }

      if ("ZP01".equals(address.getId().getAddrType()) || "ZI01".equals(address.getId().getAddrType())
          || "ZS02".equals(address.getId().getAddrType())) {
        address.setCustPhone("");
      } else if ("ZD01".equals(address.getId().getAddrType())) {
        String phone = getShippingPhoneFromLegacy(currentRecord);
        address.setCustPhone(phone != null ? phone : "");
      } else if ("ZS01".equals(address.getId().getAddrType())) {
        address.setCustPhone(currentRecord.getCmrCustPhone());
      }

    }

  }

  private void serBlankFieldsAtCopy(Addr addr) {
    if (!StringUtils.isEmpty(addr.getCustPhone())) {
      if (!"ZS01".equals(addr.getId().getAddrType()) && !"ZD01".equals(addr.getId().getAddrType())) {
        addr.setCustPhone("");
      }
    }

    if (!StringUtils.isEmpty(addr.getPoBox())) {
      if (!"ZS01".equals(addr.getId().getAddrType()) && !"ZP01".equals(addr.getId().getAddrType())) {
        addr.setPoBox("");
      }
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    serBlankFieldsAtCopy(addr);

    if ("ZS01".equals(addr.getId().getAddrType())) {
      Admin admin = LegacyCommonUtil.getAdminByReqId(entityManager, addr.getId().getReqId());

      if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType())) {
        AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
        addr.setLandCntry(addrRdc.getLandCntry());
      }
    }
  }

  @Override
  protected String getAddressTypeByUse(String addressUse) {
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
    }
    return null;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newSeq = null;
    return newSeq;
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZP01", "ZS01", "ZD01", "ZI01", "ZS02");
  }

  @Override
  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZD01", "ZI01");
  }

  @Override
  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("5");
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    if (SystemLocation.SOUTH_AFRICA.equals(issuingCountry)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD", "ABBREV_NM",
        "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL",
        "BP_REL_TYPE", "MODE_OF_PAYMENT", "ENTERPRISE", "COMMERCIAL_FINANCED", "COLL_BO_ID", "CREDIT_CD", "IBM_DEPT_COST_CENTER", "CROS_SUB_TYP"));
    return fields;
  }

  protected void importOtherSOFAddressesLD(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted, List<String> addrTypesImported) {
    List<String> addrToBeImported = Arrays.asList("ZP01", "ZI01", "ZS02");

    for (String addrType : addrToBeImported) {
      if (!addrTypesImported.contains(addrType)) {
        FindCMRRecordModel record = createAddressLD(entityManager, cmrCountry, addrType);
        if (record != null) {
          converted.add(record);
        }

      }
    }

    if (this.legacyObjects != null && this.legacyObjects.getAddresses() != null) {
      for (CmrtAddr addr : this.legacyObjects.getAddresses()) {
        if ("Y".equals(addr.getIsAddrUseShipping())) {
          FindCMRRecordModel record = new FindCMRRecordModel();
          handleSOFAddressImportLD(entityManager, cmrCountry, record, getTargetAddressTypeKey("ZD01"), addr, "ZD01");
          if (record != null && converted != null
              && !converted.stream().anyMatch(cmrAddr -> cmrAddr.getCmrAddrSeq().equals(record.getCmrAddrSeq()))) {
            converted.add(record);
          }
        }
      }
    }
  }

  private FindCMRRecordModel createAddressLD(EntityManager entityManager, String cmrIssuingCntry, String addressType) {

    LOG.debug("Adding " + addressType + " address from Legacy to request");
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();

    FindCMRRecordModel address = new FindCMRRecordModel();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      if ("ZI01".equals(addressType) && "Y".equals(cmrtAddr.getIsAddrUseInstalling())) {
        // run the specific handler import handler
        handleSOFAddressImportLD(entityManager, cmrIssuingCntry, address, getTargetAddressTypeKey(addressType), cmrtAddr, addressType);
        break;
      } else if ("ZS02".equals(addressType) && "Y".equals(cmrtAddr.getIsAddrUseEPL())) {
        handleSOFAddressImportLD(entityManager, cmrIssuingCntry, address, getTargetAddressTypeKey(addressType), cmrtAddr, addressType);
        break;
      } else if ("ZP01".equals(addressType) && "Y".equals(cmrtAddr.getIsAddrUseBilling())) {
        handleSOFAddressImportLD(entityManager, cmrIssuingCntry, address, getTargetAddressTypeKey(addressType), cmrtAddr, addressType);
        break;
      } else if ("ZD01".equals(addressType) && "Y".equals(cmrtAddr.getIsAddrUseShipping())) {
        handleSOFAddressImportLD(entityManager, cmrIssuingCntry, address, getTargetAddressTypeKey(addressType), cmrtAddr, addressType);
        break;
      }
    }
    return address;
  }

  private String getTargetAddressTypeKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mailing";
    case "ZI01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }

  protected void handleSOFAddressImportLD(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey,
      CmrtAddr cmrtAddr, String addressType) {

    // UKI order
    /*
     * line1= name 1 line2=name2/street con't/att+phone/po box/street
     * line3=street con't/att+phone/po box/street/city line4=street/city/postal
     * code/cross border country line5=city/postal code/cross border country
     * line6=postal code/cross border country
     */

    String line1 = cmrtAddr.getAddrLine1();
    String line2 = cmrtAddr.getAddrLine2();
    String line3 = cmrtAddr.getAddrLine3();
    String line4 = cmrtAddr.getAddrLine4();
    String line5 = cmrtAddr.getAddrLine5();
    String line6 = cmrtAddr.getAddrLine6();

    address.setCmrAddrSeq(cmrtAddr.getId().getAddrNo());
    address.setCmrAddrTypeCode(addressType);
    address.setCmrName1Plain(cmrtAddr.getAddrLine1());
    address.setCmrIssuedBy(cmrIssuingCntry);

    if (StringUtils.isEmpty(line6) || "-/X".equalsIgnoreCase(line6)) {
      // for old format, use existing parser
      handleSOFAddressImportOLDLD(entityManager, cmrIssuingCntry, address, addressKey, cmrtAddr);
      return;
    }
    String countryCd = getCountryCode(entityManager, line6);

    boolean crossBorder = !StringUtils.isEmpty(countryCd);
    if (crossBorder) {
      // Cross-border - ZA
      // line2 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line3 = Street + PO BOX
      // line4 = City
      // line5 = Postal Code
      // line6 = State (Country)

      address.setCmrName1Plain(line1);

      handlePhoneAndAttn(line2, cmrIssuingCntry, address, addressKey);

      handleStreetContAndPoBox(line4, cmrIssuingCntry, address, addressKey);
      address.setCmrStreetAddress(line3);
      // address.setCmrStreetAddressCont(null);

      handleCityAndPostCode(line5, cmrIssuingCntry, address, addressKey);

      if (!StringUtils.isEmpty(line5)) {
        if (StringUtils.isEmpty(address.getCmrPostalCode()) && !StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrPostalCode(line5);
        }
      }

      if (StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
        address.setCmrStreetAddress(address.getCmrStreetAddressCont());
        address.setCmrStreetAddressCont(null);
      }

      if (StringUtils.isEmpty(address.getCmrCity()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
        address.setCmrCity(address.getCmrStreetAddressCont());
        address.setCmrStreetAddressCont(null);
      }

      if (!StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())
          && isStreet(address.getCmrStreetAddressCont()) && !isStreet(address.getCmrStreetAddress())) {
        // interchange street and street con't based on data
        String cont = address.getCmrStreetAddressCont();
        address.setCmrStreetAddressCont(address.getCmrStreetAddress());
        address.setCmrStreetAddress(cont);
      }

      address.setCmrCountryLanded(countryCd);

    } else {
      // Domestic - ZA
      // line2 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line3 = Street
      // line4 = Street Con't + PO BOX
      // line5 = City
      // line6 = Postal Code
      String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
      int lineNo = 1, attLine = 0, boxLine = 0, zipLine = 0, streetLine = 0;
      for (String line : lines) {
        if (isAttn(line)) {
          address.setCmrName4(line.substring(4));
          attLine = lineNo;
        } else if (isPOBox(line)) {
          String tempLine = line.replace("PO BOX", "");
          address.setCmrPOBox(tempLine);
          boxLine = lineNo;
        } else if (isStreet(line)) {
          address.setCmrStreetAddress(line);
          streetLine = lineNo;
        }
        lineNo++;
      }

      address.setCmrName1Plain(line1); // line1

      if (attLine == 3) {
        address.setCmrName2Plain(line2); // line2= name cont, line 3 = ATT ,
                                         // line4=Street
        handlePhoneAndAttn(line3, cmrIssuingCntry, address, addressKey);
        if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
          address.setCmrStreetAddress(line4);
          streetLine = 4;
        }
        handleStreetContAndPoBox(line5, cmrIssuingCntry, address, addressKey);
        handleCityAndPostCode(line6, cmrIssuingCntry, address, addressKey);
      }

      if (attLine == 0) {
        if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
          address.setCmrStreetAddress(line2);
          streetLine = 2;
        }
        handleStreetContAndPoBox(line3, cmrIssuingCntry, address, addressKey);
        handleCityAndPostCode(line4, cmrIssuingCntry, address, addressKey);
        if (!StringUtils.isEmpty(line5)) {
          if (StringUtils.isEmpty(address.getCmrPostalCode()) && !StringUtils.isEmpty(address.getCmrCity())) {
            address.setCmrPostalCode(line5);
          }
        }
      } else if (attLine == 2) {
        if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
          address.setCmrStreetAddress(line3);
          streetLine = 3;
        }
        handleStreetContAndPoBox(line4, cmrIssuingCntry, address, addressKey);
        handleCityAndPostCode(line5, cmrIssuingCntry, address, addressKey);
      }

      if (!StringUtils.isEmpty(line6)) {
        if (StringUtils.isEmpty(address.getCmrPostalCode()) && !StringUtils.isEmpty(address.getCmrCity())) {
          address.setCmrPostalCode(line6);
        } else if (!StringUtils.isEmpty(address.getCmrPostalCode()) && StringUtils.isEmpty(address.getCmrCity()) && !StringUtils.isNumeric(line6)) {
          address.setCmrCity(line6);
        }

      }

    }

    if (StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrStreetAddress(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);
    }

    if (StringUtils.isEmpty(address.getCmrCity()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrCity(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);
    }

    if (!StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())
        && isStreet(address.getCmrStreetAddressCont()) && !isStreet(address.getCmrStreetAddress())) {
      // interchange street and street con't based on data
      String cont = address.getCmrStreetAddressCont();
      address.setCmrStreetAddressCont(address.getCmrStreetAddress());
      address.setCmrStreetAddress(cont);
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());

  }

  private void handleSOFAddressImportOLDLD(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey,
      CmrtAddr cmrtAddr) {
    LOG.debug("Parsing addresses using flexible parsing");
    String line1 = cmrtAddr.getAddrLine1();
    String line2 = cmrtAddr.getAddrLine2();
    String line3 = cmrtAddr.getAddrLine3();
    String line4 = cmrtAddr.getAddrLine4();
    String line5 = cmrtAddr.getAddrLine5();
    String line6 = cmrtAddr.getAddrLine6();

    int lineCount = 0;
    for (String line : Arrays.asList(line1, line2, line3, line4, line5, line6)) {
      if (!StringUtils.isBlank(line)) {
        lineCount++;
      }
    }
    // line 1 is always customer name 1
    address.setCmrName1Plain(line1);

    // line 2 is attn or name2
    if (isAttn(line2)) {
      address.setCmrName4(line2);
    } else {
      address.setCmrName2Plain(line2);
    }

    // line 3 is street, street con't attn, or po box
    if (isPOBox(line3)) {
      address.setCmrPOBox(line3);
    } else if (isAttn(line3)) {
      address.setCmrName4(line3);
    } else if (isStreet(line3)) {
      address.setCmrStreetAddress(line3);
    } else {
      address.setCmrStreetAddressCont(line3);
    }

    boolean countryAssigned = false;
    boolean postalOnLine6 = false;
    String stateOrCity = null;
    // work backwards according to line count
    if (lineCount == 6) {
      if (StringUtils.isNumeric(line6)) {
        // this is a bad formatted address. line6 - postal
        address.setCmrPostalCode(line6);
        postalOnLine6 = true;
      } else {
        if (!line6.contains(",")) {
          String cd = getCountryCode(entityManager, line6);
          if (cd != null) {
            address.setCmrCountryLanded(cd);
            countryAssigned = true;
          }
        } else {
          // state/city + country (cross-border)
          stateOrCity = line6.substring(0, line6.indexOf(",")).trim();
          String country = line6.substring(line6.indexOf(",") + 1).trim();
          String cd = getCountryCode(entityManager, country);
          if (cd != null) {
            address.setCmrCountryLanded(cd);
            countryAssigned = true;
          }

          address.setCmrState(stateOrCity);
        }
      }
    }

    boolean cityOnline5 = false;
    if (postalOnLine6) {
      address.setCmrCity(line5);
      cityOnline5 = true;
    } else {
      if (StringUtils.isNumeric(line5)) {
        // all numbers is postal code
        address.setCmrPostalCode(line5);
      } else if (isPOBox(line5)) {
        address.setCmrPOBox(line5);
      } else if (isStreet(line5)) {
        if (!StringUtils.isEmpty(address.getCmrStreetAddress()) && StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
          address.setCmrStreetAddressCont(address.getCmrStreetAddress());
          address.setCmrStreetAddress(line4);
        } else {
          address.setCmrStreetAddress(line5);
        }
      } else {
        if (line5 != null && line5.matches(".*\\d{1}.*")) {
          cityOnline5 = true;
          // this has a number plus text, parse at city + postal code

          StringBuilder sbCity = new StringBuilder();
          StringBuilder sbPost = new StringBuilder();
          for (String part : line5.split("[ ,]")) {
            if (!StringUtils.isNumeric(part)) {
              sbCity.append(sbCity.length() > 0 ? " " : "");
              sbCity.append(part);
            } else {
              sbPost.append(part);
            }
          }
          address.setCmrCity(sbCity.toString());
          address.setCmrPostalCode(sbPost.toString());
        }
      }
    }

    if (cityOnline5) {
      // if city is on 5, line4 is either po box or street con't
      if (isPOBox(line4)) {
        address.setCmrPOBox(line4);
      } else {
        if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
          address.setCmrStreetAddress(line4);
        } else {
          address.setCmrStreetAddressCont(line4);
        }
      }
    } else {
      if (StringUtils.isEmpty(address.getCmrCity())) {
        // address is still empty as of now, try to convert
        address.setCmrCity(line4);
        if (!StringUtils.isEmpty(stateOrCity)) {
          address.setCmrCity(stateOrCity);
          address.setCmrState(null);
        }
      } else {
        if (isStreet(line4) && StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
          address.setCmrStreetAddressCont(address.getCmrStreetAddress());
          address.setCmrStreetAddress(line4);
        } else {
          address.setCmrCity(line4);
        }
      }
    }

    if (countryAssigned && StringUtils.isEmpty(address.getCmrCity())) {
      // still no city
      if (!StringUtils.isEmpty(line5) && StringUtils.isNumeric(line5)) {
        if (!StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
          address.setCmrCity(address.getCmrStreetAddressCont());
          address.setCmrStreetAddressCont(null);
        }
      } else {
        address.setCmrCity(line5);
      }

    }
    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    if (!StringUtils.isEmpty(address.getCmrState())) {
      String stateCode = getStateCode(entityManager, address.getCmrCountryLanded(), address.getCmrState());
      if (stateCode != null && stateCode.length() < 4) {
        address.setCmrState(stateCode);
      } else {
        address.setCmrState(null);
      }
    }

    if (StringUtils.isEmpty(address.getCmrStreetAddress()) && !StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrStreetAddress(address.getCmrStreetAddressCont());
      address.setCmrStreetAddressCont(null);
    }

    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());

  }

  private String getShippingPhoneFromLegacy(FindCMRRecordModel address) {
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      String seqNo = (address.getCmrAddrSeq().length() != 5) ? StringUtils.leftPad(address.getCmrAddrSeq(), 5, '0') : address.getCmrAddrSeq();
      if ("Y".equals(cmrtAddr.getIsAddrUseShipping()) && seqNo.equals(cmrtAddr.getId().getAddrNo())) {
        return cmrtAddr.getAddrPhone();
      }
    }
    return null;
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    XSSFCell currCell = null;
    for (String name : ZA_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      if (sheet != null) {
        for (Row row : sheet) {
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {

            String cmrNo = ""; // 0

            // Address Sheet
            String seqNo = ""; // 1
            String custName1 = ""; // 2
            String nameCont = ""; // 3
            String street = ""; // 4
            String streetCont = ""; // 5
            String att = ""; // 6
            String city = "";// 7
            String postalCode = ""; // 8
            String landCountry = ""; // 09
            String poBox = ""; // 10

            // Data Sheet
            String cof = ""; // 6
            String inac = "";
            String codFlag = ""; // 13
            String zs01Phone = "";
            String intDeptNum = ""; // 16
            String isuCd = ""; // 10
            String clientTier = ""; // 11
            String enterprise = ""; // 17

            boolean isDummyUpdate = true;

            List<String> checkListSubRegion = null;
            long countSubRegion = 0;
            if (row.getRowNum() == 2001) {
              continue;
            }

            if (!"Data".equalsIgnoreCase(sheet.getSheetName())) {
              // iterate all the rows and check each column value
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
              streetCont = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              att = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              postalCode = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(8);
              city = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(9);
              landCountry = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              poBox = validateColValFromCell(currCell);

            } else if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              cof = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(9);
              inac = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(13);
              codFlag = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(15);
              zs01Phone = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(16);
              intDeptNum = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(11);
              clientTier = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              isuCd = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(17);
              enterprise = validateColValFromCell(currCell);
            }

            checkListSubRegion = Arrays.asList(nameCont, streetCont);
            countSubRegion = checkListSubRegion.stream().filter(field -> !field.isEmpty()).count();

            TemplateValidation error = new TemplateValidation(name);

            if (StringUtils.isNotBlank(cof) && ("R".equals(cof) || "S".equals(cof) || "T".equals(cof)) && "Y".equals(codFlag)) {
              LOG.trace("if COF is R/S/T, then COD will be N only >> ");
              error.addError((row.getRowNum() + 1), "COD and COF", "if COF is R/S/T, then COD will be N only <br>");
              // validations.add(error);
            } else if (StringUtils.isBlank(cof) && "N".equals(codFlag)) {
              LOG.trace("If COF is Blank, then COD will be Y only >> ");
              error.addError((row.getRowNum() + 1), "COD and COF", "If COF is Blank, then COD will be Y only. <br>");
              // validations.add(error);
            }

            if (!StringUtils.isBlank(cmrNo) && !cmrNo.startsWith("99") && !StringUtils.isBlank(intDeptNum)) {
              LOG.trace("Internal Department Number can be filled only when cmrNo Start with 99.");
              error.addError((row.getRowNum() + 1), "Internal Dept Number.",
                  "Internal Department Number can be filled only when cmrNo Start with 99.<br>");
              // validations.add(error);
            }

            if (!StringUtils.isBlank(zs01Phone) && !zs01Phone.contains("@") && !zs01Phone.matches("\\d+.\\d*")) {
              LOG.trace("Phone Number should contain only digits.");
              error.addError((row.getRowNum() + 1), "Phone #", "Phone Number should contain only digits. <br>");
              // validations.add(error);
            }

            if (!StringUtils.isBlank(enterprise) && !enterprise.contains("@") && !enterprise.matches("\\d+.\\d*")) {
              LOG.trace("Enterprise Number should contain only digits.");
              error.addError((row.getRowNum() + 1), "Enterprise #", "Enterprise Number should contain only digits. <br>");
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

            /*
             * if (!StringUtils.isBlank(inac) && inac.length() == 4 &&
             * !StringUtils.isNumeric(inac) && !"@@@@".equals(inac) &&
             * !inac.matches("^[a-zA-Z][a-zA-Z][0-9][0-9]$")) { LOG.
             * trace("INAC should have all 4 digits or 2 letters and 2 digits in order."
             * ); error.addError(row.getRowNum(), "INAC/NAC",
             * "INAC should have all 4 digits or 2 letters and 2 digits in order."
             * ); validations.add(error); }
             */

            if (!StringUtils.isBlank(custName1) || !StringUtils.isBlank(nameCont) || !StringUtils.isBlank(street) || !StringUtils.isBlank(streetCont)
                || !StringUtils.isBlank(att) || !StringUtils.isBlank(city) || !StringUtils.isBlank(postalCode) || !StringUtils.isBlank(landCountry)
                || !StringUtils.isBlank(poBox)) {
              isDummyUpdate = false;
            }

            if (StringUtils.isEmpty(cmrNo)) {
              LOG.trace("Note that CMR No. is mandatory. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "CMR No.", "Note that CMR No. is mandatory. Please fix and upload the template again.<br>");
              // validations.add(error);
            }
            if (!StringUtils.isBlank(cmrNo) && StringUtils.isBlank(seqNo) && !"Data".equalsIgnoreCase(sheet.getSheetName())) {
              LOG.trace("Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Address Sequence No.",
                  "Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again. <br>");
              // validations.add(error);
            }

            if (!isDummyUpdate) {

              if ("Mailing Address".equalsIgnoreCase(sheet.getSheetName()) || "Billing Address".equalsIgnoreCase(sheet.getSheetName())) {
                if (((!nameCont.isEmpty() && !streetCont.isEmpty()) || (!nameCont.isEmpty() && !poBox.isEmpty())) && !("ZA").equals(landCountry)) {
                  LOG.trace("Out of Name Con't and Street Con't/PO BOX only 1 can be filled at the same time.");
                  error.addError((row.getRowNum() + 1), "Name Con't, Street Con't, PO BOX",
                      "Out of Name Con't and Street Con't/PO BOX only 1 can be filled at the same time. <br>");
                }
              }

              if (!("Mailing Address".equalsIgnoreCase(sheet.getSheetName()) || "Billing Address".equalsIgnoreCase(sheet.getSheetName()))) {
                if (countSubRegion > 1 && !("ZA").equals(landCountry)) {
                  LOG.trace("Out of Name Con't and Street Con't only 1 can be filled at the same time.");
                  error.addError((row.getRowNum() + 1), "Name Con't, Street Con't",
                      "Out of Name Con't and Street Con't only 1 can be filled at the same time. <br>");
                  countSubRegion = 0;
                }
              }

              if (StringUtils.isBlank(custName1)) {
                LOG.trace("Customer Name is required.");
                error.addError((row.getRowNum() + 1), "Customer Name", "Customer Name is required.<br>");
              }

              if (StringUtils.isBlank(street)) {
                LOG.trace("Street is required.");
                error.addError((row.getRowNum() + 1), "Street", "Street is required.<br>");
              }

              if (StringUtils.isBlank(city)) {
                LOG.trace("City is required.");
                error.addError((row.getRowNum() + 1), "City", "City is required.<br>");
              }

              if (StringUtils.isBlank(landCountry)) {
                LOG.trace("Landed Country is required.");
                error.addError((row.getRowNum() + 1), "Landed Country", "Landed Country is required.<br>");
              }
              // validations.add(error);
            }
            if (error.hasErrors()) {
              validations.add(error);
            }
          }
        }
      }
    }
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
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
    map.put("##ISR", "repTeamMemberNo");
    // *abner revert begin
    // map.put("##CommercialFinanced", "commercialFinanced");
    // *abner revert end
    return map;
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalRepNameNo", "-"));
      update.setNewData(newData.getRepTeamMemberNo());
      update.setOldData(oldData.getRepTeamMemberNo());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesBusOff", "-"));
      update.setNewData(newData.getSalesBusOffCd());
      update.setOldData(oldData.getSalesBusOffCd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(newData.getCollectionCd());
      update.setOldData(oldData.getCollectionCd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollBoId(), newData.getCollBoId())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollBranchOff", "-"));
      update.setNewData(newData.getCollBoId());
      update.setOldData(oldData.getCollBoId());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getAdminDeptLine(), newData.getAdminDeptLine())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InternalDept", "-"));
      update.setNewData(newData.getAdminDeptLine());
      update.setOldData(oldData.getAdminDeptLine());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CommercialFinanced", "-"));
      update.setNewData(newData.getCommercialFinanced());
      update.setOldData(oldData.getCommercialFinanced());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCreditCd(), newData.getCreditCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CodFlag", "-"));
      update.setNewData(newData.getCreditCd());
      update.setOldData(oldData.getCreditCd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevLocn(), newData.getAbbrevLocn())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevLocation", "-"));
      update.setNewData(newData.getAbbrevLocn());
      update.setOldData(oldData.getAbbrevLocn());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSpecialTaxCd(), newData.getSpecialTaxCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SpecialTaxCd", "-"));
      update.setNewData(newData.getSpecialTaxCd());
      update.setOldData(oldData.getSpecialTaxCd());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCrosSubTyp(), newData.getCrosSubTyp())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "TypeOfCustomer", "-"));
      update.setNewData(newData.getCrosSubTyp());
      update.setOldData(oldData.getCrosSubTyp());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }

  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(SA_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

}
