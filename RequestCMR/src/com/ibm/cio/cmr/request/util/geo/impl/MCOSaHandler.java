/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.ui.PageManager;
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
          record.setCmrName4(LegacyCommonUtil.removeATT(record.getCmrName3()));
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
                }
              }
            }
          }
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
    data.setCreditCd(mainRecord.getCreditCd());
    if (legacyObjects != null && legacyObjects.getCustomer() != null) {
      data.setCrosSubTyp(legacyObjects.getCustomer().getCustType());
    }
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZP01", "ZS01", "ZD01", "ZI01", "ZS02");
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
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZD01", "ZI01");
  }
}
