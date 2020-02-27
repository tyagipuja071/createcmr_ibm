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

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
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
 * Handler for NL
 * 
 * @author Rangoli Saxena
 * 
 */
public class NLHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(NLHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  static {

    LANDED_CNTRY_MAP.put(SystemLocation.NETHERLANDS, "NL");

  }

  private static final String[] NL_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode", "LocalTax2",
      "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor", "Building", "County", "City2",
      "Department", "SpecialTaxCd", "SearchTerm", "SalRepNameNo" };

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    FindCMRRecordModel mainRecord = source.getItems() != null && !source.getItems().isEmpty() ? source.getItems().get(0) : null;
    if (mainRecord != null) {
      retrieveSOFValues(mainRecord);

      // separate list for RDc from source
      List<FindCMRRecordModel> rdcSources = new ArrayList<FindCMRRecordModel>();
      for (FindCMRRecordModel orig : source.getItems()) {
        FindCMRRecordModel clone = new FindCMRRecordModel();
        PropertyUtils.copyProperties(clone, orig);
        rdcSources.add(clone);
      }

      List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
      handleSOFConvertFrom(entityManager, source, reqEntry, mainRecord, converted, searchModel);

      if (!CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
        String cmrCountry = mainRecord.getCmrIssuedBy();
        // have to put ISRAEL specific logic here, no other way
        if (SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(cmrCountry)) {
          cmrCountry = SystemLocation.ISRAEL;
        }
        converted = assignSOFSequencesAndCleanRecords(converted, cmrCountry);
      }

      //
      for (FindCMRRecordModel sofAddress : converted) {
        // try to match seq no
        String seqNo = sofAddress.getCmrAddrSeq();
        if (StringUtils.isNumeric(seqNo)) {
          seqNo = Integer.parseInt(seqNo) + ""; // change to string, remove
                                                // leading zeroes
        }
        if ("99901".equals(seqNo)) {
          seqNo = "1"; // special seq no for 99901
        }
        for (FindCMRRecordModel rdcRec : rdcSources) {
          if (seqNo != null && seqNo.equals(rdcRec.getCmrAddrSeq())) {
            // matched!
            // move city, postal code, country to sofAddress
            sofAddress.setCmrCity(rdcRec.getCmrCity());
            sofAddress.setCmrPostalCode(rdcRec.getCmrPostalCode());
            sofAddress.setCmrCountryLanded(rdcRec.getCmrCountryLanded());
          }
        }
      }

      Collections.sort(converted);
      source.setItems(converted);

    } else {
      // no records retrieved, skip all processing
      return;
    }
  }

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;
      FindCMRRecordModel kvk_addr = null;
      FindCMRRecordModel vat_addr = null;

      if (!StringUtils.isEmpty(record.getCmrName4())) {
        record.setCmrName4(null);
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
      record.setCmrDept(record.getCmrDept());

      // transfer formatted values from SOF
      FindCMRRecordModel generalSOF = createAddress(entityManager, mainRecord.getCmrIssuedBy(), "ZS01", "General",
          new HashMap<String, FindCMRRecordModel>());
      if (generalSOF != null) {
        record.setCmrName1Plain(generalSOF.getCmrName1Plain());
        record.setCmrName2Plain(generalSOF.getCmrName2Plain());
        record.setCmrName4(generalSOF.getCmrName4());
        record.setCmrDept(generalSOF.getCmrDept());
        record.setCmrStreetAddress(generalSOF.getCmrStreetAddress());
        record.setCmrCustPhone(generalSOF.getCmrCustPhone());
      }
      converted.add(record);

      LOG.debug("Adding a copy of General as KVK and VAT Addresses");

      kvk_addr = new FindCMRRecordModel();
      vat_addr = new FindCMRRecordModel();
      for (FindCMRRecordModel record_imp : source.getItems()) {
        if ("ZS01".equals(record_imp.getCmrAddrTypeCode())) {
          try {
            PropertyUtils.copyProperties(kvk_addr, record);
            PropertyUtils.copyProperties(vat_addr, record);
            kvk_addr.setCmrAddrTypeCode("ZKVK");
            kvk_addr.setCmrAddrSeq("21102");
            vat_addr.setCmrAddrTypeCode("ZVAT");
            vat_addr.setCmrAddrSeq("21400");
            converted.add(vat_addr);
            converted.add(kvk_addr);
          } catch (Exception e) {
            // noop
          }
          break;
        }
      }
    } else {

      // import process:
      // a. Import ZS01 record from RDc, only 1
      // b. Import Installing addresses from RDc if found
      // c. Import EplMailing from RDc, if found. This will also be an
      // installing in RDc
      // d. Import all shipping, fiscal, and mailing from SOF

      // customer phone is in BillingPhone commented by Rangoli

      if (StringUtils.isEmpty(mainRecord.getCmrCustPhone())) {
        mainRecord.setCmrCustPhone(this.currentImportValues.get("Phone"));
      }

      Map<String, FindCMRRecordModel> zi01Map = new HashMap<String, FindCMRRecordModel>();

      // parse the rdc records
      String cmrCountry = mainRecord != null ? mainRecord.getCmrIssuedBy() : "";
      FindCMRRecordModel kvk_addr = null;
      FindCMRRecordModel vat_addr = null;

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
            record.setCmrPOBox(record.getCmrPOBox());
          }

          if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
            record.setCmrAddrSeq("00001");
          } else {
            record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
          }
          // transfer formatted values from SOF
          FindCMRRecordModel generalSOF = createAddress(entityManager, mainRecord.getCmrIssuedBy(), "ZS01", "General",
              new HashMap<String, FindCMRRecordModel>());
          if (generalSOF != null) {
            record.setCmrName1Plain(generalSOF.getCmrName1Plain());
            record.setCmrName2Plain(generalSOF.getCmrName2Plain());
            record.setCmrName4(generalSOF.getCmrName4());
            record.setCmrDept(generalSOF.getCmrDept());
            record.setCmrStreetAddress(generalSOF.getCmrStreetAddress());
            record.setCmrCustPhone(generalSOF.getCmrCustPhone());
          }
          converted.add(record);
          LOG.debug("Adding a copy of General as KVK and VAT Addresses");

          kvk_addr = new FindCMRRecordModel();
          vat_addr = new FindCMRRecordModel();
          for (FindCMRRecordModel record_imp : source.getItems()) {
            if ("ZS01".equals(record_imp.getCmrAddrTypeCode())) {
              try {
                PropertyUtils.copyProperties(kvk_addr, record);
                PropertyUtils.copyProperties(vat_addr, record);
                kvk_addr.setCmrAddrTypeCode("ZKVK");
                kvk_addr.setCmrAddrSeq("21102");
                vat_addr.setCmrAddrTypeCode("ZVAT");
                vat_addr.setCmrAddrSeq("21400");
                converted.add(vat_addr);
                converted.add(kvk_addr);
              } catch (Exception e) {
                // noop
              }
              break;
            }
          }
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

    // this line
    // a. the ccnverted list
  }

  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {

    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "EplMailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS01.toString(), "General", zi01Map);
    if (record != null) {
      converted.add(record);
    }
  }

  // public static void main(String[] args) {
  // PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource("cmr-log4j.properties"));
  // FindCMRRecordModel record = new FindCMRRecordModel();
  // NLHandler handler = new NLHandler();
  // handler.currentImportValues = new HashMap<String, String>();
  // handler.currentImportValues.put("Address1", "ABM AB");
  // handler.currentImportValues.put("Address2", "C/O ELISABETH BERGSTROM");
  // handler.currentImportValues.put("Address3", "Street 101");
  // handler.currentImportValues.put("Address4", "");
  // handler.currentImportValues.put("Address5", "244 02 City");
  // handler.currentImportValues.put("Address6", "");
  //
  // handler.handleSOFAddressImport(null, "788", record, "");
  // }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    // line1 = Company name
    // line2 = Customer Name Cont/Att
    // line3 = Att/Street/POBOX
    // line4 = Street/POBOX/Postal code City
    // line5 = postal code city/country for cross-border only
    // line6 = not used for domestic/Country for cross-border
    LOG.debug("handleSOFAddressImport called... ");

    String cmpnyName = !StringUtils.isEmpty(getCurrentValue(addressKey, "Name")) ? getCurrentValue(addressKey, "Name") : "";
    String countryline = !StringUtils.isEmpty(getCurrentValue(addressKey, "Address6")) ? getCurrentValue(addressKey, "Address6") : "";
    String nameCont = !StringUtils.isEmpty(getCurrentValue(addressKey, "Ext")) ? getCurrentValue(addressKey, "Ext") : "";
    String dept = !StringUtils.isEmpty(getCurrentValue(addressKey, "Dpt")) ? getCurrentValue(addressKey, "Dpt") : "";
    String attPerson = !StringUtils.isEmpty(getCurrentValue(addressKey, "Att")) ? getCurrentValue(addressKey, "Att") : "";
    String street = !StringUtils.isEmpty(getCurrentValue(addressKey, "Street")) ? getCurrentValue(addressKey, "Street") : "";
    String postalCode = !StringUtils.isEmpty(getCurrentValue(addressKey, "Plz")) ? getCurrentValue(addressKey, "Plz") : "";
    String poBox = !StringUtils.isEmpty(getCurrentValue(addressKey, "POBox")) ? getCurrentValue(addressKey, "POBox") : "";
    String city = !StringUtils.isEmpty(getCurrentValue(addressKey, "City")) ? getCurrentValue(addressKey, "City") : "";
    String phone = !StringUtils.isEmpty(getCurrentValue("", "Phone")) ? getCurrentValue("", "Phone") : "";

    /*
     * String line1 = getCurrentValue(addressKey, "Address1"); String line2 =
     * getCurrentValue(addressKey, "Address2"); String line3 =
     * getCurrentValue(addressKey, "Address3"); String line4 =
     * getCurrentValue(addressKey, "Address4"); String line5 =
     * getCurrentValue(addressKey, "Address5"); String line6 =
     * getCurrentValue(addressKey, "Address6");
     */

    String countryCd = null;
    boolean hasPoBox = !StringUtils.isEmpty(poBox) ? true : false;

    // line 1 = always Name 1
    address.setCmrName1Plain(cmpnyName);
    address.setCmrName2Plain(nameCont);
    address.setCmrDept(dept);
    address.setCmrName4(attPerson);
    address.setCmrStreetAddress(street);
    address.setCmrCity(city);
    address.setCmrPOBox(poBox);
    address.setCmrPostalCode(postalCode);
    if ("ZS01".equals(address.getCmrAddrTypeCode()))
      address.setCmrCustPhone(phone);

    if (hasPoBox) {
      // po box parse
      address.setCmrPOBox(poBox.replaceAll("[^0-9]", "").trim());
    }

    countryCd = getCurrentValue(addressKey, "Country");
    if (!StringUtils.isBlank(countryCd) && countryCd.trim().length() == 2) {
      address.setCmrCountryLanded(countryCd);
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded()) && !StringUtils.isEmpty(countryline)) {
      countryCd = getCountryCode(entityManager, countryline);
      if (!StringUtils.isEmpty(countryCd)) {
        address.setCmrCountryLanded(countryCd);
      }
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    trimAddressToFit(address);
    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name Con't: " + address.getCmrName2Plain());
    LOG.trace("Title: " + address.getCmrDept());
    LOG.trace("First Name: " + address.getCmrName3());
    LOG.trace("Last Name: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street No.: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Postal Code: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Country: " + address.getCmrCountryLanded());
  }

  @Override
  protected boolean isPhone(String data) {
    if (data == null) {
      return false;
    }
    return data.matches("[0-9\\-\\+ ]*");
  }

  @Override
  protected String getCurrentValue(String addressKey, String valueKey) {
    String val = this.currentImportValues.get(addressKey + valueKey);
    if (StringUtils.isEmpty(val)) {
      return val;
    }
    return "-/X".equalsIgnoreCase(val) ? "" : ("*".equalsIgnoreCase(val) ? "" : val);
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "General";
    String seqNoFromSOF = null;
    for (FindCMRRecordModel record : records) {
      if (!"ZS01".equals(record.getCmrAddrTypeCode())) {
        continue;
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
    boolean hasKVK = false;
    String kvk = null;
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    data.setEngineeringBo(this.currentImportValues.get("DPCEBO"));
    LOG.trace("DPCEBO: " + data.getEngineeringBo());

    kvk = this.currentImportValues.get("KVK");
    hasKVK = !StringUtils.isEmpty(kvk) ? true : false;
    if (hasKVK && kvk.length() >= 8) {
      kvk = kvk.substring(0, 8);
    }
    data.setTaxCd2(kvk);
    LOG.trace("KVK: " + data.getTaxCd2());

    data.setTaxCd1(this.currentImportValues.get("TaxCode"));
    LOG.trace("TaxCode: " + data.getTaxCd1());

    data.setEconomicCd(this.currentImportValues.get("EconomicCode"));
    LOG.trace("EconomicCode: " + data.getEconomicCd());

    data.setEngineeringBo(this.currentImportValues.get("SBO"));
    LOG.trace("BOTeam: " + data.getEngineeringBo());

    data.setIbmDeptCostCenter(this.currentImportValues.get("DepartmentNumber"));
    LOG.trace("DepartmentNumber: " + data.getIbmDeptCostCenter());

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

    data.setEnterprise(this.currentImportValues.get("EnterpriseNo"));
    LOG.trace("EnterpriseNo: " + data.getEnterprise());

    data.setIsicCd(this.currentImportValues.get("ISIC"));
    LOG.trace("ISIC: " + data.getIsicCd());

    data.setInacCd(this.currentImportValues.get("INAC"));
    LOG.trace("INAC: " + data.getInacCd());

    data.setInstallBranchOff("");
    data.setInacType("");
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    address.setCustNm4(currentRecord.getCmrName4());
    // address.setAddrTxt(currentRecord.getCmrStreetAddress());
    address.setDept(currentRecord.getCmrDept());
    address.setCity1(currentRecord.getCmrCity());
    address.setTransportZone("");
    setAddressSeqNo(address, currentRecord);
  }

  private void setAddressSeqNo(Addr address, FindCMRRecordModel currentRecord) {
    if (StringUtils.isEmpty(currentRecord.getCmrAddrTypeCode())) {
      return;
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZI01")) {
      address.getId().setAddrSeq("20701");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZP01")) {
      address.getId().setAddrSeq("29901");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZS01")) {
      address.getId().setAddrSeq("99901");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZKVK")) {
      address.getId().setAddrSeq("21102");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZVAT")) {
      address.getId().setAddrSeq("21400");
    }
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCmrOwner("IBM");
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

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    // request.setSORTL(data.getSalesBusOffCd());
    // request.setCompanyNumber(data.getEnterprise());
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    addEditKVK_VATAddress(entityManager, addr);
    if (!"ZS01".equals(addr.getId().getAddrType())) {
      addr.setCustPhone("");
    }

  }

  private void addEditKVK_VATAddress(EntityManager entityManager, Addr addr) throws Exception {
    Addr kvkAdrr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZKVK.toString(), addr.getId().getReqId());
    Addr vatAddr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZVAT.toString(), addr.getId().getReqId());
    if (kvkAdrr == null) {
      // create KVK and VAT Address from General Address if not exists
      Addr generalAddr = null;
      if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType())) {
        generalAddr = addr;
      } else {
        generalAddr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZS01.toString(), addr.getId().getReqId());
      }
      if (generalAddr != null) {
        kvkAdrr = new Addr();
        vatAddr = new Addr();
        AddrPK newPk_kvk = new AddrPK();
        AddrPK newPK_vat = new AddrPK();
        newPk_kvk.setReqId(generalAddr.getId().getReqId());
        newPk_kvk.setAddrType(CmrConstants.ADDR_TYPE.ZKVK.toString());
        newPk_kvk.setAddrSeq("21102");
        newPK_vat.setReqId(generalAddr.getId().getReqId());
        newPK_vat.setAddrType(CmrConstants.ADDR_TYPE.ZVAT.toString());
        newPK_vat.setAddrSeq("21400");

        PropertyUtils.copyProperties(kvkAdrr, generalAddr);
        PropertyUtils.copyProperties(vatAddr, generalAddr);
        kvkAdrr.setImportInd(CmrConstants.YES_NO.N.toString());
        kvkAdrr.setSapNo(null);
        kvkAdrr.setRdcCreateDt(null);
        kvkAdrr.setId(newPk_kvk);
        vatAddr.setImportInd(CmrConstants.YES_NO.N.toString());
        vatAddr.setSapNo(null);
        vatAddr.setRdcCreateDt(null);
        vatAddr.setId(newPK_vat);

        entityManager.persist(kvkAdrr);
        entityManager.persist(vatAddr);
        entityManager.flush();
      }

    } else if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType())) {
      kvkAdrr.setCustNm1(addr.getCustNm1());
      kvkAdrr.setCustNm2(addr.getCustNm2());
      kvkAdrr.setCustNm4(addr.getCustNm4());
      kvkAdrr.setAddrTxt(addr.getAddrTxt());
      kvkAdrr.setAddrTxt2(addr.getAddrTxt2());
      kvkAdrr.setCity1(addr.getCity1());
      kvkAdrr.setPostCd(addr.getPostCd());
      kvkAdrr.setPoBox(addr.getPoBox());
      kvkAdrr.setCustPhone(addr.getCustPhone());
      kvkAdrr.setLandCntry(addr.getLandCntry());

      vatAddr.setCustNm1(addr.getCustNm1());
      vatAddr.setCustNm2(addr.getCustNm2());
      vatAddr.setCustNm4(addr.getCustNm4());
      vatAddr.setAddrTxt(addr.getAddrTxt());
      vatAddr.setAddrTxt2(addr.getAddrTxt2());
      vatAddr.setCity1(addr.getCity1());
      vatAddr.setPostCd(addr.getPostCd());
      vatAddr.setCustPhone(addr.getCustPhone());
      vatAddr.setLandCntry(addr.getLandCntry());
      vatAddr.setPoBox(addr.getPoBox());

      entityManager.merge(kvkAdrr);
      entityManager.merge(vatAddr);
      entityManager.flush();
    }
  }

  private Addr getAddressByType(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GET.BYTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    List<Addr> addrList = query.getResults(1, Addr.class);
    if (addrList != null && addrList.size() > 0) {
      return addrList.get(0);
    }
    return null;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {

  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM4", "ADDR_TXT", "CITY1", "STATE_PROV", "POST_CD", "LAND_CNTRY", "DEPT", "PO_BOX",
        "CUST_PHONE"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void doFilterAddresses(List<AddressModel> results) {
    List<AddressModel> addrsToRemove = new ArrayList<AddressModel>();
    for (AddressModel addrModel : results) {
      if (CmrConstants.ADDR_TYPE.ZKVK.toString().equalsIgnoreCase(addrModel.getAddrType())
          || CmrConstants.ADDR_TYPE.ZVAT.toString().equalsIgnoreCase(addrModel.getAddrType())) {
        addrsToRemove.add(addrModel);
      }
    }
    results.removeAll(addrsToRemove);
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {

    return false;
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(newData.getCollectionCd());
      update.setOldData(oldData.getCollectionCd());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd2(), newData.getTaxCd2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxCd2(), "LocalTax2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxCd2(), "LocalTax2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(NL_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {

  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = null;
    if ("ZD01".equals(addrType)) {
      newAddrSeq = generateShippingAddrSeqNL(entityManager, addrType, reqId);
    } else if ("ZP01".equals(addrType) || "ZI01".equals(addrType) || "ZS01".equals(addrType)) {
      newAddrSeq = generateAddrSeqNL(entityManager, addrType, reqId);
    }
    return newAddrSeq;
  }

  protected String generateShippingAddrSeqNL(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 20800;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "20800");

      if (!(Integer.valueOf(maxAddrSeq) >= 20800 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "20800";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  protected String generateAddrSeqNL(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 1;
    String newAddrSeq = null;
    if (addrType.equals("ZI01")) {
      addrSeq = 20701;
    }
    if (addrType.equals("ZP01")) {
      addrSeq = 29901;
    }
    if (addrType.equals("ZS01")) {
      addrSeq = 99901;
    }
    try {
      addrSeq = Integer.parseInt(newAddrSeq);
    } catch (Exception e) {
      // if returned value is invalid
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newSeq = null;
    if ("ZS01".equals(addrType)) {
      newSeq = "99901";
    }
    if ("ZI01".equals(addrType)) {
      newSeq = "20701";
    }
    if ("ZP01".equals(addrType)) {
      newSeq = "29901";
    }
    if ("ZD01".equals(addrType)) {
      newSeq = generateShippingAddrSeqNLCopy(entityManager, addrType, reqId);
    }
    return newSeq;
  }

  // NL(788) Shipping addr seq logic should work while copying of address
  protected String generateShippingAddrSeqNLCopy(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 20800;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "20800");

      if (!(Integer.valueOf(maxAddrSeq) >= 20800 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "20800";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorID", "originatorId");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##INACCode", "inacCd");
    map.put("##BPRelationType", "bpRelType");
    map.put("##VAT", "vat");
    map.put("##SitePartyID", "sitePartyId");
    map.put("##RequesterID", "requesterId");
    map.put("##Department", "dept");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##BuyingGroupID", "bgId");
    map.put("##LandedCountry", "landCntry");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##RequestReason", "reqReason");
    map.put("##SAPNumber", "sapNo");
    map.put("##CMRNumber", "cmrNo");
    map.put("##CAP", "capInd");
    map.put("##DUNS", "dunsNo");
    map.put("##CoverageID", "covId");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##MembLevel", "memLvl");
    map.put("##InternalDept", "ibmDeptCostCenter");
    map.put("##RequestType", "reqType");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##EngineeringBo", "engineeringBo");
    map.put("##POBox", "poBox");
    map.put("##Expedite", "expediteInd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##CustomerName2", "custNm2");
    map.put("##LocalTax2", "taxCd2");
    map.put("##CustomerName1", "custNm1");
    map.put("##PostalCode", "postCd");
    map.put("##LocalTax1", "taxCd1");
    map.put("##CustomerName4", "custNm4");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##EconomicCd2", "economicCd");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##ISIC", "isicCd");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##StateProv", "stateProv");
    map.put("##City1", "city1");
    map.put("##CustLang", "custPrefLang");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##ClientTier", "clientTier");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##SOENumber", "soeReqNo");
    map.put("##OriginatorName", "originatorNm");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CollectionCd", "collectionCd");
    map.put("##CustPhone", "custPhone");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##VATExempt", "vatExempt");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##Enterprise", "enterprise");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##GlobalBuyingGroupID", "gbgId");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }
}