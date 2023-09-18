/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * o Handler for MCO Spain And Portugal
 * 
 * @author Jeffrey Zamora
 * 
 */
public class MCOPtEsHandler extends MCOHandler {

  private static final Logger LOG = Logger.getLogger(MCOPtEsHandler.class);

  protected static final String[] PT_MASS_UPDATE_SHEET_NAMES = { "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address (Update)", "EPL Address", "Data" };
  protected static final String[] ES_MASS_UPDATE_SHEET_NAMES = { "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address (Update)", "EPL Address", "Data" };

  private static final String[] SPAIN_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "LocalTax1" };

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      FindCMRRecordModel record = mainRecord;
      // Defect 1750097: PP Spain: Create- EPL address is getting imported
      // everytime for private Create scenario
      if ("ZS02".equals(record.getCmrAddrType())) {
        // only private Customer: map Rdc with Legacy for create by model
        String addrType = null;
        String seqNo = null;
        List<String> sofUses = null;

        if (source.getItems() != null) {
          for (FindCMRRecordModel rdcRecord : source.getItems()) {
            seqNo = rdcRecord.getCmrAddrSeq();
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {

              sofUses = this.legacyObjects.getUsesBySequenceNo(seqNo);
              for (String sofUse : sofUses) {
                addrType = getAddressTypeByUse(sofUse);
                if ("2".equals(seqNo) && "ZS01".equals(addrType)) {
                  break;
                }
              }
            }

            if ("2".equals(seqNo) && "ZS01".equals(addrType)) {
              LOG.trace("RDC Legacy seqNo and =" + seqNo + " for Legacy  address use.");
              rdcRecord.setCmrAddrType(addrType);
              rdcRecord.setCmrAddrTypeCode(addrType);
              rdcRecord.setCmrStreetAddressCont(rdcRecord.getCmrName3());
              rdcRecord.setCmrName3(null);

              if (!StringUtils.isBlank(rdcRecord.getCmrPOBox())) {
                String poBox = rdcRecord.getCmrPOBox().trim();
                setPoBox(rdcRecord, poBox);
              }
              if (StringUtils.isEmpty(rdcRecord.getCmrAddrSeq())) {
                rdcRecord.setCmrAddrSeq("00001");
              } else {
                rdcRecord.setCmrAddrSeq(StringUtils.leftPad(rdcRecord.getCmrAddrSeq(), 5, '0'));
              }

              rdcRecord.setCmrName2Plain(rdcRecord.getCmrName2Plain());
              rdcRecord.setCmrTaxOffice(this.currentImportValues.get("InstallingAddressT"));
              rdcRecord.setCmrDept(null);

              if (StringUtils.isEmpty(rdcRecord.getCmrCustPhone())) {
                rdcRecord.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
              }
              converted.add(rdcRecord);
              break;
            }
          }
        }

      } else {
        // only add zs01 equivalent for create by model
        // name3 in rdc = Address Con't on SOF
        record.setCmrStreetAddressCont(record.getCmrName3());
        record.setCmrName3(null);

        if (!StringUtils.isBlank(record.getCmrPOBox())) {
          String poBox = record.getCmrPOBox().trim();
          setPoBox(record, poBox);
        }

        record.setCmrAddrSeq("00001");

        record.setCmrName2Plain(record.getCmrName2Plain());
        record.setCmrTaxOffice(this.currentImportValues.get("InstallingAddressT"));
        record.setCmrDept(null);

        if (StringUtils.isEmpty(record.getCmrCustPhone())) {
          record.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
        }
        converted.add(record);
      }

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
                  LOG.info("Spain MCOPtEsHandler Update from CreateCMR: " + record.getCmrNum());
                  // name3 in rdc = Address Con't on SOF
                  addr.setCmrStreetAddressCont(record.getCmrName3());
                  addr.setCmrName3(null);

                  if (!StringUtils.isBlank(record.getCmrPOBox())) {
                    String poBox = record.getCmrPOBox().trim();
                    setPoBox(addr, poBox);
                  }

                  if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                    addr.setCmrAddrSeq("00001");
                  }
                  converted.add(addr);
                }
              }
            }
          }
          // add unmapped addresses
          FindCMRRecordModel record = createAddress(entityManager, mainRecord.getCmrIssuedBy(), CmrConstants.ADDR_TYPE.ZP02.toString(), "Fiscal",
              new HashMap<String, FindCMRRecordModel>());
          if (record == null && "838".equals(reqEntry.getCmrIssuingCntry())) {
            record = new FindCMRRecordModel();
            // record.setCmrAddrSeq("6");
            record.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZP02.toString());
            record.setCmrIssuedBy(mainRecord.getCmrIssuedBy());
            record.setCmrAddrType("Fiscal");// Fiscal
            setFAddressFromLegacy(record);
            converted.add(record);
          }

        }
      } else {
        // old SOF import process:
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

          record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing", zi01Map);
          if (record != null) {
            converted.add(record);
          }

          record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Mailing", zi01Map);
          if (record != null) {
            converted.add(record);
          }
          record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
          if (record != null) {
            converted.add(record);
          }
          record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP02.toString(), "Fiscal", zi01Map);
          if (record != null) {
            converted.add(record);
          } else {
            record = new FindCMRRecordModel();
            record.setCmrAddrSeq("A");
            record.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZP02.toString());
          }
        }
      }
    }
  }

  // Defect 1752996: 'APTO' should not be converted into the 'PO BOX' while
  // import
  /*
   * Removed "PO BOX" or "APTO" word from poBox field value
   */
  private void setPoBox(FindCMRRecordModel addr, String poBox) {
    boolean poBoxFlag = poBox.contains("PO BOX");
    boolean aptoFlag = poBox.contains("APTO");
    if (poBoxFlag) {
      addr.setCmrPOBox(poBox.substring(6).trim());
    } else if (aptoFlag) {
      addr.setCmrPOBox(poBox.substring(5).trim());
    } else {
      addr.setCmrPOBox(poBox);
    }
    // System.out.println("Set Post Box=" + addr.getCmrPOBox());
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    String line1 = this.currentImportValues.get(addressKey + "Address1");
    String line2 = this.currentImportValues.get(addressKey + "Address2");
    String line3 = this.currentImportValues.get(addressKey + "Address3");
    String line4 = this.currentImportValues.get(addressKey + "Address4");
    String line5 = this.currentImportValues.get(addressKey + "Address5");
    String line6 = this.currentImportValues.get(addressKey + "Address6");

    // line 1 is always customer name
    address.setCmrName1Plain(line1);

    // line 2 either name con't, street or attn
    if (isStreet(line2)) {
      address.setCmrStreetAddress(line2);
    } else if (isAttn(line2)) {
      address.setCmrName4(removeATT(line2));
    } else {
      address.setCmrName2Plain(line2);
    }

    // line 3 either post/city, street or att
    if (isPostCity(line3)) {
      extractPostCity(line3, address);
    } else if (isAttn(line3)) {
      address.setCmrName4(removeATT(line3));
    } else {
      if (StringUtils.isBlank(address.getCmrStreetAddress())) {
        address.setCmrStreetAddress(line3);
      } else {
        address.setCmrStreetAddressCont(line3);
      }
    }

    // line 4 either post/city, street con't/street or att
    if (isPostCity(line4)) {
      extractPostCity(line4, address);
    } else if (isAttn(line4)) {
      address.setCmrName4(removeATT(line4));
    } else {
      if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
        address.setCmrStreetAddress(line4);
      } else {
        address.setCmrStreetAddressCont(line4);
      }
    }

    // line 5 can be telephone, post city, or country
    if (isPostCity(line5)) {
      extractPostCity(line5, address);
    } else if (line5 != null && (line5.startsWith("TF") || line5.contains("TFN"))) {
      extractPhone(line5, address);
    } else if (!StringUtils.isBlank(line5)) {
      // this is country
      String cd = getCountryCode(entityManager, line5);
      if (cd != null) {
        address.setCmrCountryLanded(cd);
      }
    }

    // country
    if (!StringUtils.isBlank(line6)) {
      // this is country
      String cd = getCountryCode(entityManager, line6);
      if (cd != null) {
        address.setCmrCountryLanded(cd);
      }
    }

    // very improper format, city is empty
    if (StringUtils.isEmpty(address.getCmrCity())) {
      if (!StringUtils.isEmpty(line6) && !StringUtils.isEmpty(line5) && !StringUtils.isEmpty(address.getCmrCountryLanded())) {
        // line 6 was assigned as country, line 5 is city
        address.setCmrCity(line5);
      } else if (StringUtils.isEmpty(line6) && !StringUtils.isEmpty(line5) && !StringUtils.isEmpty(address.getCmrCountryLanded())) {
        // line 5 was assigned as country, line 4 is city
        address.setCmrCity(line4);
      } else if (!StringUtils.isEmpty(line5) && StringUtils.isEmpty(address.getCmrCountryLanded())) {
        address.setCmrCity(line5);
      } else {
        // just assign first non-empty line from below
        List<String> lines = Arrays.asList(line6, line5, line4, line3);
        for (String line : lines) {
          if (!StringUtils.isEmpty(line)) {
            address.setCmrCity(line);
            break;
          }
        }
      }
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      switch (cmrIssuingCntry) {
      case SystemLocation.SPAIN:
        address.setCmrCountryLanded("ES");
        break;
      case SystemLocation.PORTUGAL:
        address.setCmrCountryLanded("PT");
        break;
      }
    }

    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Country: " + address.getCmrCountryLanded());
  }

  private String removeATT(String addrLine) {
    addrLine = StringUtils.replace(addrLine, "ATTN ", "");
    addrLine = StringUtils.replace(addrLine, "ATT.", "");
    addrLine = StringUtils.replace(addrLine, "ATT", "");
    addrLine = StringUtils.replace(addrLine, "ATT:", "");
    addrLine = StringUtils.replace(addrLine, "ATT :", "");
    addrLine = StringUtils.replace(addrLine, "ATT ", "");
    return addrLine.trim();
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "Billing";
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
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);
    data.setLocationNumber((this.currentImportValues.get("LocationNumber")));
    LOG.trace("LocationNumber: " + data.getLocationNumber());
    data.setSpecialTaxCd((this.currentImportValues.get("SplTaxCode")));
    LOG.trace("SplTaxCode: " + data.getSpecialTaxCd());
    data.setSalesBusOffCd(this.currentImportValues.get("SBO"));
    LOG.trace("SBO: " + data.getSalesBusOffCd());

    String embargoCode = (this.currentImportValues.get("EmbargoCode"));
    if (StringUtils.isBlank(embargoCode)) {
      embargoCode = getRdcAufsd(data.getCmrNo(), data.getCmrIssuingCntry());
    }
    if (embargoCode != null && embargoCode.length() < 2 && !"ST".equalsIgnoreCase(embargoCode)) {
      data.setEmbargoCd(embargoCode);
      LOG.trace("EmbargoCode: " + embargoCode);
    } else if ("ST".equalsIgnoreCase(embargoCode)) {
      data.setTaxExemptStatus3(embargoCode);
      LOG.trace(" STC Order Block Code : " + embargoCode);
    }
    // Mukesh: Defect 1698949: FVT: Mismatch between Data.AC_ADMIN_BO and
    // CMRTCUST.RACBO
    data.setAcAdminBo(this.currentImportValues.get("AccAdBo"));
    LOG.trace("AccAdBo: " + data.getAcAdminBo());

    if (SystemLocation.SPAIN.equals(data.getCmrIssuingCntry())) {
      data.setCollectionCd(this.currentImportValues.get("DistrictCode"));
      LOG.trace("Collection Code: " + data.getCollectionCd());

      if ("U".equals(admin.getReqType())) {
        if (data.getAbbrevLocn() != null) {
          String abbrevLocn = data.getAbbrevLocn().trim();
          data.setAbbrevLocn(abbrevLocn.length() > 12 ? abbrevLocn.substring(0, 12) : abbrevLocn);
          data.getAbbrevLocn();
        }
      }

      if (this.legacyObjects != null) {
        CmrtCust cust = this.legacyObjects.getCustomer();
        if (cust != null) {
          data.setSpecialTaxCd(StringUtils.isEmpty(cust.getTaxCd()) ? "" : cust.getTaxCd());
        }
      }

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        data.setVatInd(StringUtils.isNotBlank(data.getVat()) ? "T" : "N");

        if ("Y".equals(admin.getProspLegalInd())) {
          data.setCustPrefLang("S");
        }
      }
    }

    if (SystemLocation.PORTUGAL.equalsIgnoreCase(data.getCmrIssuingCntry()) && "U".equals(admin.getReqType())) {
      CmrtCust cust = this.legacyObjects.getCustomer();
      if (cust != null) {
        String customerType = cust.getCustType();
        data.setCrosSubTyp(customerType);
        data.setSpecialTaxCd(StringUtils.isEmpty(cust.getTaxCd()) ? "" : cust.getTaxCd());
      }
      data.setTaxCd1("");
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
    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    if (!StringUtils.isEmpty(currentRecord.getCmrName4())) {
      address.setCustNm4(removeATT(currentRecord.getCmrName4()));
    }
    address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
    address.setTransportZone("Z000000001");
    address.setDept("");

    if ("ZP01".equals(address.getId().getAddrType()) || "ZI01".equals(address.getId().getAddrType())
        || "ZS02".equals(address.getId().getAddrType())) {
      address.setCustPhone("");
    } else if ("ZD01".equals(address.getId().getAddrType())) {
      String phone = getShippingPhoneFromLegacy(currentRecord);
      address.setCustPhone(phone != null ? phone : "");
    } else if ("ZS01".equals(address.getId().getAddrType())) {
      address.setCustPhone(currentRecord.getCmrCustPhone());
    }
    if (("ZD01".equals(address.getId().getAddrType()) || ("ZS01".equals(address.getId().getAddrType()))) && address.getCustPhone() != null) {
      removeTFFromShipingAddr(address);
    }
    if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      String seq = StringUtils.leftPad(currentRecord.getCmrAddrSeq(), 5, '0');
      address.getId().setAddrSeq(seq);
    }
    if (!StringUtils.isEmpty(currentRecord.getCmrStreetAddress()) && currentRecord.getCmrStreetAddress().length() > 30) {
      address.setAddrTxt(currentRecord.getCmrStreetAddress().substring(0, 30));
    }

    if ("D".equals(address.getImportInd())) {
      String seq = StringUtils.leftPad(address.getId().getAddrSeq(), 5, '0');
      address.getId().setAddrSeq(seq);
    }

  }

  private void removeTFFromShipingAddr(Addr address) {
    String custPhone = address.getCustPhone().trim();
    custPhone = custPhone.startsWith("TF") ? custPhone.substring(2) : custPhone;
    address.setCustPhone(custPhone);
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

  private void setFAddressFromLegacy(FindCMRRecordModel address) {
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      if ("Y".equals(cmrtAddr.getIsAddressUseF())) {
        address.setCmrAddrSeq(cmrtAddr.getId().getAddrNo());
        String line1 = cmrtAddr.getAddrLine1();
        line1 = line1.startsWith("J") || line1.startsWith("F") ? line1.substring(1) : line1;

        address.setCmrName1Plain(line1);
        address.setCmrStreetAddress(cmrtAddr.getAddrLine2().startsWith("CL") ? cmrtAddr.getAddrLine2().substring(2) : cmrtAddr.getAddrLine2());
        address.setCmrCity(cmrtAddr.getAddrLine4());
        address.setCmrPostalCode(cmrtAddr.getAddrLine5());
        address.setCmrCustPhone(cmrtAddr.getAddrPhone());
      }
    }

  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
    // TO DO
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    if (SystemLocation.PORTUGAL.equals(data.getCmrIssuingCntry())) {
      data.setCustPrefLang("P");
    } else {
      data.setCustPrefLang("S");
    }
    data.setCmrOwner("IBM");
    data.setSensitiveFlag(CmrConstants.REGULAR);
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    // TO DO
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    // TO DO
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType())) {
      // 1. Get old data
      DataRdc rdcData = null;
      rdcData = getOldData(entityManager, String.valueOf(data.getId().getReqId()));

      if (rdcData != null) {
        // ISU CD
        if (StringUtils.isEmpty(data.getIsuCd()) && !StringUtils.isEmpty(rdcData.getIsuCd())) {
          data.setIsuCd(rdcData.getIsuCd());
        }

        // Enterprise
        if (StringUtils.isEmpty(data.getEnterprise()) && !StringUtils.isEmpty(rdcData.getEnterprise())) {
          // data.setEnterprise(rdcData.getEnterprise());
        }

        // sales rep
        if (StringUtils.isEmpty(data.getSalesTeamCd()) && !StringUtils.isEmpty(rdcData.getSalesTeamCd())) {
          data.setSalesTeamCd(rdcData.getSalesTeamCd());
        }

        if (StringUtils.isEmpty(data.getSalesBusOffCd()) && !StringUtils.isEmpty(rdcData.getSalesBusOffCd())) {
          data.setSalesBusOffCd(rdcData.getSalesBusOffCd());
        }
      }
    }

    /*
     * 1763597: Add Mode of Payment indicator for CMR to RDC data model and RDC
     * GUI. DTN: As agreed with Adriana, we will set the value on KNA1 if the
     * mode if payment field on the GUI is 5.
     */
    if ("5".equals(data.getModeOfPayment()) && !"88".equals(data.getOrdBlk()) && !"92".equals(data.getOrdBlk())) {
      data.setOrdBlk("94");
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    addr.setTransportZone("Z000000001");
    serBlankFieldsAtCopy(addr);
    if ("838".equals(cmrIssuingCntry)) {
      addEditFiscalAddress(entityManager, addr);
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

  private void addEditFiscalAddress(EntityManager entityManager, Addr addr) throws Exception {
    Addr fiscalAddr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZP02.toString(), addr.getId().getReqId());
    if (fiscalAddr == null) {
      // create fiscal from billing if not exists
      Addr billingAddr = null;
      if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType())) {
        billingAddr = addr;
      } else {
        billingAddr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZS01.toString(), addr.getId().getReqId());
      }
      if (billingAddr != null) {
        fiscalAddr = new Addr();
        AddrPK newPk = new AddrPK();
        newPk.setReqId(billingAddr.getId().getReqId());
        newPk.setAddrType(CmrConstants.ADDR_TYPE.ZP02.toString());
        newPk.setAddrSeq("00001");

        PropertyUtils.copyProperties(fiscalAddr, billingAddr);
        fiscalAddr.setImportInd(CmrConstants.YES_NO.N.toString());
        fiscalAddr.setSapNo(null);
        fiscalAddr.setRdcCreateDt(null);
        fiscalAddr.setId(newPk);

        entityManager.persist(fiscalAddr);
        entityManager.flush();
      }

    } else if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType())) {
      // update fiscal when billing is updated
      fiscalAddr.setCustNm1(addr.getCustNm1());
      fiscalAddr.setCustNm2(addr.getCustNm2());
      fiscalAddr.setCustNm4(addr.getCustNm4());
      fiscalAddr.setAddrTxt(addr.getAddrTxt());
      fiscalAddr.setAddrTxt2(addr.getAddrTxt2());
      fiscalAddr.setCity1(addr.getCity1());
      fiscalAddr.setPostCd(addr.getPostCd());
      fiscalAddr.setCustPhone(addr.getCustPhone());
      fiscalAddr.setLandCntry(addr.getLandCntry());

      entityManager.merge(fiscalAddr);
      entityManager.flush();
    }
  }

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
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    if (SystemLocation.SPAIN.equals(cntry)) {
      return Arrays.asList(SPAIN_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    } else {
      return false;
    }
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

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

    // Prod Defect 1640184
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
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !service.equals(oldData.getTaxExempt3(), newData.getTaxExemptStatus3())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "TaxExemptStatus3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxExemptStatus3(), "TaxExemptStatus3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxExempt3(), "TaxExemptStatus3", cmrCountry));
      results.add(update);
    }

  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType()) && SystemLocation.SPAIN.equals(data.getCmrIssuingCntry())) {
      data.setVatInd(StringUtils.isNotBlank(data.getVat()) ? "T" : "N");
    }
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(
        Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM4", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX", "CUST_PHONE"));
    return fields;
  }

  @Override
  protected boolean isStreet(String data) {
    if (data == null) {
      return false;
    }
    String[] parts = data.split("[., ]");
    if (parts.length > 1 && StringUtils.isNumeric(parts[parts.length - 1])) {
      return true;
    }
    return false;
  }

  public static void main(String[] args) {
    FindCMRRecordModel addr = new FindCMRRecordModel();
    String data = "DP28030 MADRID";

    MCOPtEsHandler h = new MCOPtEsHandler();
    /*
     * if (h.isPostCity(data)) { h.extractPostCity(data, addr); }
     * System.out.println(addr.getCmrCity());
     * System.out.println(addr.getCmrPostalCode());
     */
    String line1 = "BILLING NAME";
    System.out.println("Origi=" + line1);
    // line1 = line1.startsWith("J") || line1.startsWith("F") ?
    // line1.substring(1) : line1;
    line1 = line1.startsWith("CL") ? line1.substring(2) : line1;

    System.out.println("TEst=" + line1);
    /*
     * StringBuilder street = new StringBuilder(); line1 =
     * StringUtils.replace(line1, "F", ""); line1 = StringUtils.replace(line1,
     * "J", "");
     */
    // System.out.println("Result=" + line1);

  }

  private boolean isPostCity(String data) {
    if (data == null) {
      return false;
    }
    String[] parts = data.split("[., ]");
    if (parts.length > 1 && StringUtils.isNumeric(parts[0])) {
      return true;
    }
    if (parts.length > 1 && parts[0].matches(".*\\d{1}.*")) {
      return true;
    }
    return false;
  }

  private void extractPostCity(String data, FindCMRRecordModel address) {
    if (data == null) {
      return;
    }
    String[] parts = data.split("[., ]");
    if (parts.length > 1 && StringUtils.isNumeric(parts[0])) {
      address.setCmrPostalCode(parts[0]);
      address.setCmrCity(data.substring(parts[0].length()).trim());
    } else if (parts.length > 1 && parts[0].matches(".*\\d{1}.*")) {
      address.setCmrPostalCode(parts[0]);
      address.setCmrCity(data.substring(parts[0].length()).trim());
    } else {
      address.setCmrPostalCode(null);
      address.setCmrCity(data);
    }
  }

  private void extractPhone(String data, FindCMRRecordModel address) {
    if (data == null) {
      return;
    }
    if (!data.contains("TF")) {
      return;
    }
    String[] parts = data.split("[.,\\- ]");
    for (String part : parts) {
      if (StringUtils.isNumeric(part)) {
        address.setCmrCustPhone(part);
        return;
      }
    }
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void doFilterAddresses(List<AddressModel> results) {
    List<AddressModel> addrsToRemove = new ArrayList<AddressModel>();
    for (AddressModel addrModel : results) {
      if (CmrConstants.ADDR_TYPE.ZP02.toString().equalsIgnoreCase(addrModel.getAddrType())) {
        addrsToRemove.add(addrModel);
      }
    }
    results.removeAll(addrsToRemove);
  }

  @Override
  public void doBeforeDPLCheck(EntityManager entityManager, Data data, List<Addr> addresses) throws Exception {
    for (Addr addr : addresses) {
      if ("ZP02".equals(addr.getId().getAddrType()) && "838".equals(data.getCmrIssuingCntry())) {
        addr.setDplChkResult("N");
      }
    }
  }

  @Override
  protected String getAddressTypeByUse(String addressUse) {
    switch (addressUse) {
    case "1":
      return "ZP01";
    case "2":
      return "ZS01";
    case "3":
      return "ZI01";
    case "4":
      return "ZD01";
    case "5":
      return "ZS02";
    case "F":
      return "ZP02";
    }
    return null;
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    if (SystemLocation.SPAIN.equals(cmrIssuingCntry) || SystemLocation.PORTUGAL.equals(cmrIssuingCntry)) {
      return true;
    }
    return false;
  }

  @Override
  public void doAddMassUpdtValidation(TemplateValidation validation, String country) {
    if (SystemLocation.SPAIN.equals(country) || SystemLocation.PORTUGAL.equals(country)) {
      // noop
    }
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "MODE_OF_PAYMENT", "VAT", "ISIC_CD", "EMBARGO_CD", "MAILING_COND", "ABBREV_NM",
        "LOCN_NO", "CLIENT_TIER", "ENGINEERING_BO", "ENTERPRISE", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "COLLECTION_CD", "SPECIAL_TAX_CD",
        "SEARCH_TERM", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL", "BP_REL_TYPE"));
    return fields;
  }

  /*
   * @Override public String generateAddrSeq(EntityManager entityManager, String
   * addrType, long reqId, String cmrIssuingCntry) { String newAddrSeq = null;
   * if ("ZD01".equals(addrType) || "ZI01".equals(addrType)) { newAddrSeq =
   * generateShipInstallAddrSeqESCreateUpdate(entityManager, addrType, reqId); }
   * else { String maxAddrSeq = null; int addrSeq = 0; String sql =
   * ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ"); PreparedQuery query = new
   * PreparedQuery(entityManager, sql); query.setParameter("REQ_ID", reqId);
   * query.setParameter("ADDR_TYPE", addrType);
   * 
   * List<Object[]> results = query.getResults(); if (results != null &&
   * results.size() > 0) { Object[] result = results.get(0); maxAddrSeq =
   * (String) (result != null && result.length > 0 ? result[0] : "0"); if
   * (StringUtils.isEmpty(maxAddrSeq)) { maxAddrSeq = "0"; } try { addrSeq =
   * Integer.parseInt(maxAddrSeq); } catch (Exception e) { // if returned value
   * is invalid } addrSeq++; } newAddrSeq = Integer.toString(addrSeq);
   * newAddrSeq = StringUtils.leftPad(newAddrSeq, 5, '0'); } return newAddrSeq;
   * }
   * 
   * @Override public String generateModifyAddrSeqOnCopy(EntityManager
   * entityManager, String addrType, long reqId, String oldAddrSeq, String
   * cmrIssuingCntry) { String newSeq = null; newSeq =
   * generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry); return
   * newSeq; }
   */
  private String generateShipInstallAddrSeqESCreateUpdate(EntityManager entityManager, String addrType, long reqId) {

    String maxAddrSeq = null;
    int addrSeq = 0;
    int addrSeqCopy = 6;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.ES.CREATE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      try {
        if ("0".equals(maxAddrSeq))
          addrSeq = Integer.parseInt(maxAddrSeq);
        else {
          addrSeq = Integer.parseInt(maxAddrSeq);

          sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.ES.MAX");
          query = new PreparedQuery(entityManager, sql);
          query.setParameter("REQ_ID", reqId);

          results = query.getResults();
          if (results != null && results.size() > 0) {
            result = results.get(0);
            maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
            addrSeq = Integer.parseInt(maxAddrSeq);
            if (addrSeq < 6) {
              addrSeq = addrSeqCopy;
            }
          }
        }
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    newAddrSeq = StringUtils.leftPad(newAddrSeq, 5, '0');
    return newAddrSeq;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##PrefSeqNo", "prefSeqNo");
    map.put("##CurrencyCd", "legacyCurrencyCd");
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
    map.put("##ModeOfPayment", "paymentMode");
    map.put("##StateProv", "stateProv");
    map.put("##LocationNumber", "locationNo");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##CollectionCd", "collectionCd");
    map.put("##VAT", "vat");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    map.put("##SAPNumber", "sapNo");
    map.put("##Department", "dept");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##EngineeringBo", "engineeringBo");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##CustomerName1", "custNm1");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName2", "custNm2");
    map.put("##CustomerName4", "custNm4");
    map.put("##MailingCond", "mailingCondition");
    map.put("##Enterprise", "enterprise");
    map.put("##PostalCode", "postCd");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##DistrictCd", "territoryCd");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    return map;
  }

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
    if (SystemLocation.SPAIN.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01", "ZS01", "ZI01", "ZD01", "ZS02", "ZP02");
    } else if (SystemLocation.PORTUGAL.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01", "ZS01", "ZI01", "ZD01", "ZS02");
    }
    return null;
  }

  @Override
  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.SPAIN.equals(cmrIssuingCntry) || SystemLocation.PORTUGAL.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZD01", "ZI01");
    }
    return null;
  }

  @Override
  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.SPAIN.equals(cmrIssuingCntry)) {
      return Arrays.asList("6");
    } else {
      return Arrays.asList("5");
    }
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    if (SystemLocation.SPAIN.equals(issuingCountry)) {
      return true;
    } else if (SystemLocation.PORTUGAL.equals(issuingCountry)) {
      return true;
    } else {
      return false;
    }

  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    XSSFCell currCell = null;
    String[] sheetNames = null;
    if (SystemLocation.SPAIN.equals(country)) {
      sheetNames = ES_MASS_UPDATE_SHEET_NAMES;
    } else {
      sheetNames = PT_MASS_UPDATE_SHEET_NAMES;
    }
    for (String name : sheetNames) {
      XSSFSheet sheet = book.getSheet(name);
      if (sheet != null) {
        for (Row row : sheet) {
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
            String cmrNo = ""; // 0
            String seqNo = "";// 1
            String street = ""; // 4
            String addressCont = ""; // 5
            String attPerson = ""; // 6
            String localPostal = ""; // 7
            String localCity = ""; // 8
            String crossCity = ""; // 9
            String cbPostal = ""; // 10
            String phoneNo = ""; // 12
            String poBox = ""; // 13
            String landCountry = ""; // 11
            String dataCmrNo = ""; // 0
            String dataEnterpriseNo = ""; // 5
            String isuCd = ""; // 9
            String clientTier = ""; // 10
            String ordBlk = "";
            String stcOrdBlk = "";
            List<String> checkList = null;
            long count = 0;
            if (row.getRowNum() == 2001) {
              continue;
            }

            if (!"Data".equalsIgnoreCase(sheet.getSheetName())) {
              // iterate all the rows and check each column value
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(1);
              seqNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(4);
              street = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(5);
              addressCont = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              attPerson = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              localPostal = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(8);
              localCity = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(9);
              crossCity = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              cbPostal = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(12);
              phoneNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(13);
              poBox = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(11);
              landCountry = validateColValFromCell(currCell);
            }

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              dataCmrNo = validateColValFromCell(currCell);
              if (country.equals("822")) {
                currCell = (XSSFCell) row.getCell(5);
                dataEnterpriseNo = validateColValFromCell(currCell);
                currCell = (XSSFCell) row.getCell(11);
                ordBlk = validateColValFromCell(currCell);
                currCell = (XSSFCell) row.getCell(12);
                stcOrdBlk = validateColValFromCell(currCell);
              } else {
                currCell = (XSSFCell) row.getCell(7);
                dataEnterpriseNo = validateColValFromCell(currCell);
                currCell = (XSSFCell) row.getCell(13);
                ordBlk = validateColValFromCell(currCell);
                currCell = (XSSFCell) row.getCell(14);
                stcOrdBlk = validateColValFromCell(currCell);
              }
              currCell = (XSSFCell) row.getCell(7);
              isuCd = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              clientTier = validateColValFromCell(currCell);
            }

            checkList = Arrays.asList(addressCont, poBox, attPerson);
            count = checkList.stream().filter(field -> !field.isEmpty()).count();
            if ("Billing Address".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(13);
              poBox = validateColValFromCell(currCell);
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                poBox = df.formatCellValue(row.getCell(13));
              }
            }

            if ("Billing Address".equalsIgnoreCase(sheet.getSheetName()) || "Shipping Address (Update)".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(12);
              phoneNo = validateColValFromCell(currCell);
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                phoneNo = df.formatCellValue(row.getCell(12));
              }
            }

            TemplateValidation error = new TemplateValidation(name);
            if (StringUtils.isEmpty(dataCmrNo)) {
              if (row.getRowNum() == 1) {
                return;
              }
              LOG.trace("Note that CMR No. is mandatory. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "CMR No.", "Note that CMR No. is mandatory. Please fix and upload the template again.");
              validations.add(error);
            }

            if (!StringUtils.isBlank(cmrNo) && StringUtils.isBlank(seqNo)) {
              LOG.trace("Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Address Sequence No.",
                  "Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
              validations.add(error);
            }
            if (StringUtils.isEmpty(street) && !StringUtils.isEmpty(addressCont)) {
              LOG.trace("Address Continuation cannot be filled if Street is empty. >> ");
              error.addError((row.getRowNum() + 1), "Address Continuation", "Address Continuation cannot be filled if Street is empty.");
              validations.add(error);
            }
            if (!StringUtils.isEmpty(crossCity) && !StringUtils.isEmpty(localCity)) {
              LOG.trace("Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty. >> ");
              error.addError((row.getRowNum() + 1), "City",
                  "Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty.");
              validations.add(error);
            }
            if (!StringUtils.isEmpty(cbPostal) && !StringUtils.isEmpty(localPostal)) {
              LOG.trace("Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                  + "If one is populated, the other must be empty. >>");
              error.addError((row.getRowNum() + 1), "Postal Code",
                  "Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                      + "If one is populated, the other must be empty.");
              validations.add(error);
            }
            if (!StringUtils.isEmpty(crossCity) && !StringUtils.isEmpty(cbPostal)) {
              int maxlengthcomputed = crossCity.length() + cbPostal.length();
              if (maxlengthcomputed > 32) {
                LOG.trace("Crossborder city and crossborder postal code should have a maximun of 30 characters.");
                error.addError((row.getRowNum() + 1), "Crossborder City/Postal",
                    "Crossborder city and crossborder postal code should have a maximun of 30 characters.");
                validations.add(error);
              }
            }
            if (count > 1) {
              LOG.trace("Out of Address Con't, PO BOX and Att Person only 1 can be filled at the same time .");
              error.addError((row.getRowNum() + 1), "Address Con't/PO BOX/Att Person",
                  "Out of Address Con't, PO BOX and Att Person only 1 can be filled at the same time. ");
              validations.add(error);
              count = 0;
            }

            if (!StringUtils.isEmpty(crossCity) && !StringUtils.isEmpty(localPostal)) {
              LOG.trace(
                  "Cross Border City and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              error.addError((row.getRowNum() + 1), "Local Postal Code",
                  "Cross Border City and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              validations.add(error);
            }

            if (!StringUtils.isEmpty(localCity) && !StringUtils.isEmpty(cbPostal)) {
              LOG.trace(
                  "Local City and Cross Border Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              error.addError((row.getRowNum() + 1), "Local City",
                  "Local City and Cross Border Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              validations.add(error);
            }

            if (!StringUtils.isEmpty(landCountry)) {
              if (!("PT").equals(landCountry) && (!StringUtils.isEmpty(localCity) || !StringUtils.isEmpty(localPostal))) {
                LOG.trace("Landed Country should be PT for Local Scenario.");
                error.addError((row.getRowNum() + 1), "Landed Country", "Landed Country should be PT for Local Scenario.");
                validations.add(error);
              } else if (("PT").equals(landCountry) && (!StringUtils.isEmpty(crossCity) || !StringUtils.isEmpty(cbPostal))) {
                LOG.trace("Landed Country shouldn't be PT for Cross borders.");
                error.addError((row.getRowNum() + 1), "Landed Country", "Landed Country shouldn't be PT for Cross Borders.");
                validations.add(error);
              }
            }
            if ("Billing Address".equalsIgnoreCase(sheet.getSheetName())) {
              if (poBox.contains("+")) {
                LOG.trace("Please input value in numeric format. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "PO Box", "Please input value in numeric format. Please fix and upload the template again.");
                validations.add(error);
              }
              if (phoneNo.contains("+")) {
                LOG.trace("Please input value in numeric format. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Phone No.", "Please input value in numeric format. Please fix and upload the template again.");
                validations.add(error);
              }
            }

            if ("Shipping Address (Update)".equalsIgnoreCase(sheet.getSheetName())) {
              if (phoneNo.contains("+")) {
                LOG.trace("Please input value in numeric format. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Phone No.", "Please input value in numeric format. Please fix and upload the template again.");
                validations.add(error);
              }
            }

            if (localCity.contains("@") && localCity.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Local City", "Field contains invalid character. Please fix and upload the template again.");
              validations.add(error);
            }

            if (crossCity.contains("@") && crossCity.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Cross Border City",
                  "Field contains invalid character. Please fix and upload the template again.");
              validations.add(error);
            }

            if (localPostal.contains("@") && localPostal.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Local Postal Code",
                  "Field contains invalid character. Please fix and upload the template again.");
              validations.add(error);
            }

            if (cbPostal.contains("@") && cbPostal.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Cross Border Postal Code",
                  "Field contains invalid character. Please fix and upload the template again.");
              validations.add(error);
            }

            if ("Data".equalsIgnoreCase(sheet.getSheetName()) && !country.equals(SystemLocation.SPAIN)) {
              if (!StringUtils.isBlank(dataEnterpriseNo) && !dataEnterpriseNo.equals("@@@@@@")) {
                if (!StringUtils.isNumeric(dataEnterpriseNo)) {
                  LOG.trace("Enterprise Number should have numeric values only.");
                  error.addError((row.getRowNum() + 1), "Enterprise No.", "Enterprise Number should have numeric values only. ");
                }
              }

              if (StringUtils.isNotBlank(stcOrdBlk) && StringUtils.isNotBlank(ordBlk)) {
                LOG.trace("Please fill either STC Order Block Code or Order Block Code ");
                error.addError((row.getRowNum() + 1), "Order Block Code", "Please fill either STC Order Block Code or Order Block Code ");
              }

              if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(clientTier))
                  || (StringUtils.isNotBlank(clientTier) && StringUtils.isBlank(isuCd))) {
                LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that both ISU and CTC value needs to be filled..");
                error.addError((row.getRowNum() + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
              } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                if (StringUtils.isBlank(clientTier) || !"Q".equals(clientTier)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
                if (StringUtils.isBlank(clientTier) || !"Y".equals(clientTier)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
                if (StringUtils.isBlank(clientTier) || !"T".equals(clientTier)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if ((!StringUtils.isBlank(isuCd) && !Arrays.asList("32", "34", "36").contains(isuCd)) && !"@".equalsIgnoreCase(clientTier)) {
                LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                error.addError(row.getRowNum() + 1, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
              } else if (!"@QYT".contains(clientTier)) {
                LOG.trace(
                    "The row " + (row.getRowNum() + 1) + ":Note that Client Tier only accept @,Q,Y or T. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Client Tier",
                    ":Note that Client Tier only accept @,Q,Y or T. Please fix and upload the template again.<br>");
              }
              if (error.hasErrors()) {
                validations.add(error);
              }
            }

            if (country.equals(SystemLocation.SPAIN)) {
              for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
                String collectionCd = null;
                String salesRep = null;
                String enterprise = null;
                String inacCd = null;
                row = sheet.getRow(rowIndex);
                if (row == null) {
                  validations.add(error);
                  return; // stop immediately when row is blank
                }
                if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
                  currCell = (XSSFCell) row.getCell(4);
                  collectionCd = validateColValFromCell(currCell);
                  currCell = (XSSFCell) row.getCell(7);
                  enterprise = validateColValFromCell(currCell);
                  currCell = (XSSFCell) row.getCell(8);
                  inacCd = validateColValFromCell(currCell);
                  currCell = (XSSFCell) row.getCell(12);
                  salesRep = validateColValFromCell(currCell);
                  currCell = (XSSFCell) row.getCell(9);
                  isuCd = validateColValFromCell(currCell);
                  currCell = (XSSFCell) row.getCell(10);
                  clientTier = validateColValFromCell(currCell);
                  currCell = (XSSFCell) row.getCell(13);
                  ordBlk = validateColValFromCell(currCell);
                  currCell = (XSSFCell) row.getCell(14);
                  stcOrdBlk = validateColValFromCell(currCell);
                }

                if ((!("@").equals(collectionCd)) && (!StringUtils.isEmpty(collectionCd))) {
                  if (collectionCd.length() == 2 && !collectionCd.chars().allMatch(Character::isLetterOrDigit)) {
                    LOG.trace("Collection code should be exactly 2 characters (alphanumeric). Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Collection Code",
                        "Collection code should be exactly 2 characters (alphanumeric). Please fix and upload the template again.");
                  } else if (collectionCd.length() != 2) {
                    LOG.trace("Collection code should be exactly 2 characters. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Collection Code",
                        "Collection code should be exactly 2 characters. Please fix and upload the template again.");
                  }
                }

                if (StringUtils.isNotBlank(stcOrdBlk) && StringUtils.isNotBlank(ordBlk)) {
                  LOG.trace("Please fill either STC Order Block Code or Order Block Code ");
                  error.addError((row.getRowNum() + 1), "Order Block Code", "Please fill either STC Order Block Code or Order Block Code ");
                }

                if ((!("@").equals(enterprise)) && (!StringUtils.isEmpty(enterprise))) {
                  if (enterprise.length() == 6 && !enterprise.chars().allMatch(Character::isLetterOrDigit)) {
                    LOG.trace("Enterprise Number should be exactly 6 characters (alphanumeric). Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Enterprise Number",
                        "Enterprise Number should be exactly 6 characters (alphanumeric). Please fix and upload the template again.");
                  } else if (enterprise.length() != 6) {
                    LOG.trace("Enterprise Number should be exactly 6 characters. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Enterprise Number",
                        "Enterprise Number should be exactly 6 characters. Please fix and upload the template again.");
                  }
                }

                // INAC Code
                if (StringUtils.isNotBlank(inacCd)) {
                  if (!StringUtils.isNumeric(inacCd) && !"@".equals(inacCd)) {
                    String firstTwoinacCdChar = StringUtils.substring(inacCd, 0, 2);
                    String lastTwoinacCdChar = StringUtils.substring(inacCd, 2);

                    Pattern upperCaseChars = Pattern.compile("^[A-Z]*$");
                    Matcher matcherFirstTwo = upperCaseChars.matcher(firstTwoinacCdChar);

                    Pattern digitsChars = Pattern.compile("^[0-9]+$");
                    Matcher matcherLastTwo = digitsChars.matcher(lastTwoinacCdChar);

                    if (!matcherFirstTwo.matches() || !matcherLastTwo.matches()) {
                      error.addError(rowIndex + 1, "<br>INAC/NAC",
                          "INAC should be 4 digits or two letters (Uppercase characters) followed by 2 digits.");
                    }
                  }
                }

                if (!StringUtils.isEmpty(salesRep)) {
                  if (salesRep.length() == 6 && !(salesRep.chars().allMatch(Character::isLetterOrDigit))) {
                    LOG.trace("Sales Rep. No. should be alphanumeric. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Sales Rep. No.",
                        "Sales Rep. No. should be alphanumeric. Please fix and upload the template again.");
                  }
                }

                // validate ISU and CTC combination
                if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(clientTier))
                    || (StringUtils.isNotBlank(clientTier) && StringUtils.isBlank(isuCd))) {
                  LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that both ISU and CTC value needs to be filled..");
                  error.addError((row.getRowNum() + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
                } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                  if (StringUtils.isBlank(clientTier) || !"Q".equals(clientTier)) {
                    LOG.trace("The row " + (row.getRowNum() + 1)
                        + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Client Tier",
                        ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                  }
                } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
                  if (StringUtils.isBlank(clientTier) || !"Y".equals(clientTier)) {
                    LOG.trace("The row " + (row.getRowNum() + 1)
                        + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Client Tier",
                        ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                  }
                } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
                  if (StringUtils.isBlank(clientTier) || !"T".equals(clientTier)) {
                    LOG.trace("The row " + (row.getRowNum() + 1)
                        + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Client Tier",
                        ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                  }
                } else if ((!StringUtils.isBlank(isuCd) && !Arrays.asList("32", "34", "36").contains(isuCd)) && !"@".equalsIgnoreCase(clientTier)) {
                  LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                  error.addError(row.getRowNum() + 1, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
                }
              }
              if (error.hasErrors()) {
                validations.add(error);
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void convertDnBImportValues(EntityManager entityManager, Admin admin, Data data) {

    super.convertDnBImportValues(entityManager, admin, data);
    if (SystemLocation.SPAIN.equals(data.getCmrIssuingCntry())) {
      data.setCustPrefLang("S");

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        data.setVatInd(StringUtils.isNotBlank(data.getVat()) ? "T" : "N");
      }
    }
  }

}