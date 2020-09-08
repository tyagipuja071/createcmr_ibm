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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.ui.PageManager;
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

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;

      if (!StringUtils.isEmpty(record.getCmrName4())) {
        // name4 in rdc is street con't
        record.setCmrStreetAddressCont(record.getCmrName4());
        record.setCmrName4(null);
      }

      // name3 in rdc = attn on SOF
      if (!StringUtils.isEmpty(record.getCmrName3())) {
        if (record.getCmrName3().startsWith("ATT")) {
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
                  addr.setCmrStreetAddressCont(record.getCmrName4());
                  addr.setCmrName3(null);
                  if (record.getCmrName3() != null && record.getCmrName3().startsWith("ATT")) {
                    addr.setCmrName4(LegacyCommonUtil.removeATT(record.getCmrName3()));
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
    if (legacyObjects != null && legacyObjects.getCustomer() != null) {
      data.setCrosSubTyp(legacyObjects.getCustomer().getCustType());
      data.setCreditCd(legacyObjects.getCustomer().getCreditCd());
    }

    /*
     * String mop = legacyObjects.getCustomer().getModeOfPayment(); if (mop ==
     * "R" || mop == "S" || mop == "T") { data.setCommercialFinanced(mop); }
     * else if (mop == "5") { data.setCodCondition(mop); } else {
     * data.setCodCondition("N"); data.setCommercialFinanced(""); }
     */
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

    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());

    if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      String seq = StringUtils.leftPad(currentRecord.getCmrAddrSeq(), 5, '0');
      address.getId().setAddrSeq(seq);
    }

    if ("D".equals(address.getImportInd())) {
      String seq = StringUtils.leftPad(address.getId().getAddrSeq(), 5, '0');
      address.getId().setAddrSeq(seq);
    }

    if ("ZP01".equals(address.getId().getAddrType()) || "ZI01".equals(address.getId().getAddrType())) {
      address.setCustPhone("");
    } else if ("ZD01".equals(address.getId().getAddrType())) {
      String phone = getShippingPhoneFromLegacy(currentRecord);
      address.setCustPhone(phone != null ? phone : "");
    } else if ("ZS01".equals(address.getId().getAddrType())) {
      address.setCustPhone(currentRecord.getCmrCustPhone());
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
        "BP_REL_TYPE", "MODE_OF_PAYMENT", "ENTERPRISE", "COMMERCIAL_FINANCED", "COLL_BO_ID", "COD_CONDITION", "IBM_DEPT_COST_CENTER"));
    return fields;
  }

  protected void importOtherSOFAddressesLD(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted, List<String> addrTypesImported) {
    List<String> addrToBeImported = Arrays.asList("ZD01", "ZP01", "ZI01", "ZS02");

    for (String addrType : addrToBeImported) {
      if (!addrTypesImported.contains(addrType)) {
        FindCMRRecordModel record = createAddressLD(entityManager, cmrCountry, addrType);
        if (record != null) {
          converted.add(record);
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
      if ("Y".equals(cmrtAddr.getIsAddrUseShipping()) && address.getCmrAddrSeq().equals(cmrtAddr.getId().getAddrNo())) {
        return cmrtAddr.getAddrPhone();
      }
    }
    return null;
  }

}
