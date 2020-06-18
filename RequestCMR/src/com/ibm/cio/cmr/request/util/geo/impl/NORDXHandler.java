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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MachinesToInstall;
import com.ibm.cio.cmr.request.entity.MachinesToInstallPK;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.MachineModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for NORDX
 * 
 * @author Rangoli Saxena
 * 
 */
public class NORDXHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(NORDXHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  public Map<String, List<MachineModel>> MACHINES_MAP = new HashMap<String, List<MachineModel>>();

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.SWEDEN, "SE");
    LANDED_CNTRY_MAP.put(SystemLocation.NORWAY, "NO");
    LANDED_CNTRY_MAP.put(SystemLocation.FINLAND, "FI");
    LANDED_CNTRY_MAP.put(SystemLocation.DENMARK, "DK");

  }

  private static final String[] NORDX_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CustLang", "GeoLocationCode", "Affiliate", "CAP", "CMROwner",
      "CustClassCode", "LocalTax2", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor",
      "Building", "County", "City2", "Department", "SearchTerm", "SpecialTaxCd" };

  public static boolean isNordicsCountry(String issuingCntry) {
    if (SystemLocation.SWEDEN.equals(issuingCntry) || SystemLocation.NORWAY.equals(issuingCntry) || SystemLocation.DENMARK.equals(issuingCntry)) {
      return true;
    } else {
      return false;
    }
  }

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

      record.setCmrName2Plain(record.getCmrName2Plain());
      // record.setCmrTaxOffice(this.currentImportValues.get("InstallingAddressT"));
      record.setCmrDept(null);

      if (StringUtils.isEmpty(record.getCmrCustPhone())) {
        record.setCmrCustPhone(this.currentImportValues.get("MailingPhone"));
      }

      converted.add(record);
    } else {

      // import process:
      // a. Import ZS01 record from RDc, only 1
      // b. Import Installing addresses from RDc if found
      // c. Import EplMailing from RDc, if found. This will also be an
      // installing in RDc
      // d. Import all shipping, fiscal, and mailing from SOF

      // customer phone is in BillingPhone commented by Rangoli
      /*
       * if (StringUtils.isEmpty(mainRecord.getCmrCustPhone())) {
       * mainRecord.setCmrCustPhone
       * (this.currentImportValues.get("BillingPhone")); }
       */

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

          importZS01AddressFromSOF(entityManager, cmrCountry, zi01Map, converted, record);
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

        // import all installing from SOF
        List<String> sequencesInstalling = this.installingSequences;
        List<Integer> seqInstallInt = new ArrayList<Integer>();
        if (sequencesInstalling != null && !sequencesInstalling.isEmpty() && sequencesInstalling.size() > 1) {
          LOG.debug("Installing Sequences is not empty. Importing " + sequencesInstalling.size() + " installing addresses.");

          for (String seq : sequencesInstalling) {
            seqInstallInt.add(Integer.parseInt(seq));
          }
          // find the address with least seq number
          int minSeqInstall = Collections.min(seqInstallInt);

          for (String seq : sequencesInstalling) {
            if (minSeqInstall > 0 && Integer.parseInt(seq) == minSeqInstall) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing_" + seq + "_", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            } else {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP02.toString(), "Installing_" + seq + "_", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }
          }
        } else {
          LOG.debug("Only one installing received. ");
          record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing", zi01Map);
          if (record != null) {
            converted.add(record);
          }
        }
        importOtherSOFAddresses(entityManager, cmrCountry, zi01Map, converted);
      }
    }

  }

  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {

    /*
     * FindCMRRecordModel record = createAddress(entityManager, cmrCountry,
     * CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing", zi01Map); if
     * (record != null) { converted.add(record); }
     */

    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
    if (record != null) {
      converted.add(record);
    }
    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

  }

  protected FindCMRRecordModel createZS01Address(EntityManager entityManager, String cmrIssuingCntry, String addressType, String addressKey,
      Map<String, FindCMRRecordModel> zi01Map, FindCMRRecordModel record) {
    if (!this.currentImportValues.containsKey(addressKey + "AddressNumber")) {
      return record;
    }
    LOG.debug("Adding " + addressKey + " address from SOF to request");
    // FindCMRRecordModel address = new FindCMRRecordModel();
    record.setCmrAddrTypeCode(addressType);
    record.setCmrAddrSeq(this.currentImportValues.get(addressKey + "AddressNumber"));

    if ("EplMailing".equals(addressKey) && zi01Map.containsKey(record.getCmrAddrSeq()) && zi01Map.size() > 1) {
      FindCMRRecordModel epl = zi01Map.get(record.getCmrAddrSeq());
      LOG.debug("Switching address " + record.getCmrAddrSeq() + " to Epl");
      epl.setCmrAddrTypeCode("ZS02"); // switch ZI01 to EPL then do nothing
      return record;
    }
    record.setCmrName1Plain(this.currentImportValues.get(addressKey + "Name"));

    // run the specific handler import handler
    handleSOFAddressImport(entityManager, cmrIssuingCntry, record, addressKey);

    String transAddressNo = this.currentImportValues.get(addressKey + "TransAddressNumber");
    if (!StringUtils.isEmpty(transAddressNo) && StringUtils.isNumeric(transAddressNo) && transAddressNo.length() == 5) {
      record.setTransAddrNo(transAddressNo);
      LOG.trace("Translated Address No.: '" + record.getTransAddrNo() + "'");
    }

    record.setCmrIssuedBy(cmrIssuingCntry);

    return record;
  }

  protected void importZS01AddressFromSOF(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted, FindCMRRecordModel record) {

    record = createZS01Address(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS01.toString(), "Mailing", zi01Map, record);
    /*
     * if (record != null) { converted.add(record); }
     */
  }

  public static void main(String[] args) {
    PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource("cmr-log4j.properties"));
    FindCMRRecordModel record = new FindCMRRecordModel();
    NORDXHandler handler = new NORDXHandler();
    handler.currentImportValues = new HashMap<String, String>();
    handler.currentImportValues.put("Address1", "CROSS BP");
    handler.currentImportValues.put("Address2", "ATT PERSN");
    handler.currentImportValues.put("Address3", "STREET ADDRESS");
    handler.currentImportValues.put("Address4", "PO BOX 1234");
    handler.currentImportValues.put("Address5", "AL-74 CITY OF INSTA");
    handler.currentImportValues.put("Address6", "ALBANIA");
    handler.currentImportValues.put("City", "CITY OF INSTA");
    handler.currentImportValues.put("ZipCode", "AL-74");

    handler.handleSOFAddressImport(null, "678", record, "");
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {

    // NORDX Order = ..., Street/PO BOX - postal code city - Country for CB
    // line1 = Customer name
    // line2 = Customer Name Cont/Att/Street/PO BOX
    // line3 = Att/Street/PO BOX/postal code city
    // line4 = Street/PO BOX/postal code city/Country
    // line5 = PO BOX/postal code city/Country
    // line6 = Postal code city/Country

    String line1 = getCurrentValue(addressKey, "Address1");
    String line2 = getCurrentValue(addressKey, "Address2");
    String line3 = getCurrentValue(addressKey, "Address3");
    String line4 = getCurrentValue(addressKey, "Address4");
    String line5 = getCurrentValue(addressKey, "Address5");
    String line6 = getCurrentValue(addressKey, "Address6");

    // line1 = Customer name
    address.setCmrName1Plain(line1);

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1, attLine = 0, boxLine = 0, zipLine = 0;
    for (String line : lines) {
      if (isAttn(line)) {
        address.setCmrName4(line.substring(4));
        attLine = lineNo;
      } else if (isPOBox(line) && line.length() > 7) {
        address.setCmrPOBox(line.substring(7));
        boxLine = lineNo;
      } else if (handlePostCity(line, addressKey, address)) {
        zipLine = lineNo;
      }
      lineNo++;
    }

    // attn and customer name cont lines
    if (attLine == 3) {
      address.setCmrName2Plain(line2);
    }

    // poBox & zipCode lines
    if (zipLine == 3) {
      if (attLine == 0 && boxLine == 0) {
        address.setCmrStreetAddress(line2);
      }
      setLandedCountry(entityManager, address, line4);
    } else if (zipLine == 4) {
      if (attLine == 0 && boxLine == 0) {
        address.setCmrName2Plain(line2);
        address.setCmrStreetAddress(line3);
      } else if (isAttn(line2)) {
        address.setCmrStreetAddress(line3);
      } else {
        address.setCmrName2Plain(line2);
      }
      setLandedCountry(entityManager, address, line5);
    } else if (zipLine == 5) {
      if (boxLine == 0) {
        address.setCmrStreetAddress(line4);
      } else if (isAttn(line2)) {
        address.setCmrStreetAddress(line3);
      } else if (isAttn(line3)) {
        address.setCmrName2Plain(line2);
      }
      setLandedCountry(entityManager, address, line6);
    } else if (zipLine == 6) {
      address.setCmrName2Plain(line2);
      address.setCmrStreetAddress(line4);
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded()) && (!StringUtils.isEmpty(line6) || !StringUtils.isEmpty(line5))) {
      // try landed country on line 6/5 all the time
      String lineToCheck = !StringUtils.isEmpty(line6) ? line6 : line5;
      String code = getCountryCode(entityManager, lineToCheck);
      if (!StringUtils.isEmpty(code)) {
        address.setCmrCountryLanded(code);
      }
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    trimAddressToFit(address);
    if (this.installingSequences.contains(address.getCmrAddrSeq()))
      MACHINES_MAP.put(address.getCmrAddrSeq(), getMachinesList(address));

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Postal Code: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Country: " + address.getCmrCountryLanded());
  }

  private void setLandedCountry(EntityManager entityManager, FindCMRRecordModel address, String line) {
    if (!StringUtils.isEmpty(line)) {
      String countryCd = getCountryCode(entityManager, line);
      if (!StringUtils.isEmpty(countryCd)) {
        address.setCmrCountryLanded(countryCd);
      }
    }
  }

  private boolean handlePostCity(String data, String addressKey, FindCMRRecordModel address) {
    if (StringUtils.isEmpty(data)) {
      return false;
    }

    // check if postCode or city matches the line
    String postCd = getCurrentValue(addressKey, "ZipCode");
    String city = getCurrentValue(addressKey, "City");
    postCd = !StringUtils.isEmpty(postCd) ? postCd : "";
    city = !StringUtils.isEmpty(city) ? city : "";

    if (data.trim().startsWith(postCd.trim()) || data.trim().endsWith(city.trim())) {
      address.setCmrPostalCode(postCd);
      address.setCmrCity(city);
      return true;
    }
    return false;
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
    String addrKey = "Mailing";
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
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    data.setEngineeringBo(this.currentImportValues.get("ACAdmDSC"));
    LOG.trace("ACAdmDSC: " + data.getEngineeringBo());

    data.setTaxCd1(this.currentImportValues.get("TaxCode"));
    LOG.trace("TaxCode: " + data.getTaxCd1());

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

    // value = leading account no + mrc
    String value = this.currentImportValues.get("LeadingAccountNo");
    if (!StringUtils.isEmpty(value) && value.length() > 6) {
      data.setCompany(value.substring(0, 6));
    }
    LOG.trace("LeadingAccountNo: " + data.getCompany());

    data.setLocationNumber((this.currentImportValues.get("LocationNumber")));
    LOG.trace("LocationNumber: " + data.getLocationNumber());

    data.setInstallBranchOff("");
    data.setInacType("");
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setSitePartyId("");
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    address.setCustNm4(currentRecord.getCmrName4());
    address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
    address.setTransportZone("");
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
    mv.addObject("machines", new MachineModel());
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

    if ("ZI01".equals(addr.getId().getAddrType()) || "ZD01".equals(addr.getId().getAddrType()) || "ZP02".equals(addr.getId().getAddrType())) {
      addr.setPoBox("");
    }

    if (!"ZS01".equals(addr.getId().getAddrType())) {
      addr.setCustPhone("");
    }

  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {

    if (!MACHINES_MAP.isEmpty()) {
      for (Map.Entry<String, List<MachineModel>> entry : MACHINES_MAP.entrySet()) {
        List<MachineModel> machinesList = entry.getValue();
        for (MachineModel model : machinesList) {
          deleteSingleMachine(entityManager, admin, data, model);
          createMachines(entityManager, admin, data, model);
        }
      }
    }
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(
        Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM4", "ADDR_TXT", "CITY1", "STATE_PROV", "POST_CD", "LAND_CNTRY", "PO_BOX", "CUST_PHONE"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
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
    return Arrays.asList(NORDX_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_MACHINES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

    List<MachinesToInstall> machines = query.getResults(MachinesToInstall.class);

    for (MachinesToInstall machine : machines) {
      if (StringUtils.isBlank(machine.getCurrentIndc())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "Machine", "Machine"));
        update.setNewData(machine.getId().getMachineTyp() + machine.getId().getMachineSerialNo());
        update.setOldData("");
        results.add(update);
      }
    }
  }

  private List<MachineModel> getMachinesList(FindCMRRecordModel address) {

    List<MachineModel> machineList = new ArrayList<MachineModel>();

    for (int i = 1; i == i; i++) {
      String machineType = this.currentImportValues.get("Installing_" + address.getCmrAddrSeq() + "_MachineType" + Integer.toString(i));
      String serialNumber = this.currentImportValues.get("Installing_" + address.getCmrAddrSeq() + "_MachineSerial" + Integer.toString(i));
      if (!StringUtils.isBlank(machineType) && !StringUtils.isBlank(serialNumber)) {
        MachineModel machineModel = new MachineModel();
        machineModel.setAddrType(address.getCmrAddrTypeCode());
        machineModel.setAddrSeq(address.getCmrAddrSeq());
        machineModel.setMachineTyp(machineType);
        machineModel.setMachineSerialNo(serialNumber);
        machineModel.setCurrentIndc("Y");
        machineList.add(machineModel);
      } else {
        break;
      }
    }
    return machineList;
  }

  private void createMachines(EntityManager entityManager, Admin admin, Data data, MachineModel model) {

    LOG.trace("Creating Machines To Install for  Addr record:  " + " [Request ID: " + admin.getId().getReqId() + " ,Addr Type: " + model.getAddrType()
        + " ,Addr Seq: " + model.getAddrSeq() + "]");

    // AppUser user = AppUser.getUser(request);

    MachinesToInstall machines = new MachinesToInstall();
    MachinesToInstallPK machinesPK = new MachinesToInstallPK();

    // Setting primary key fields
    machinesPK.setReqId(admin.getId().getReqId());
    machinesPK.setAddrType(model.getAddrType());
    machinesPK.setAddrSeq(model.getAddrSeq());
    machinesPK.setMachineTyp(model.getMachineTyp());
    machinesPK.setMachineSerialNo(model.getMachineSerialNo());

    // setting remaining fields
    machines.setId(machinesPK);
    machines.setCreateBy(admin.getRequesterId());
    machines.setCurrentIndc("Y"); // Confirm from Jeff
    machines.setLastUpdtBy(admin.getRequesterId());

    machines.setCreateTs(SystemUtil.getCurrentTimestamp());
    machines.setLastUpdtTs(machines.getCreateTs());

    entityManager.persist(machines);
    entityManager.flush();
    // createEntity(machines, entityManager);

  }

  public void deleteSingleMachine(EntityManager entityManager, Admin admin, Data data, MachineModel model) {
    LOG.trace("Deleting Machine To Install for  Addr record:  " + " [Request ID: " + admin.getId().getReqId() + " ,Addr Type: " + model.getAddrType()
        + " ,Addr Seq: " + model.getAddrSeq() + "]");

    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_SINGLE_MACHINE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setParameter("ADDR_TYPE", model.getAddrType());
    query.setParameter("ADDR_SEQ", model.getAddrSeq());
    query.setParameter("MACHINE_SERIAL_NO", model.getMachineSerialNo());
    query.setParameter("MACHINE_TYP", model.getMachineTyp());

    List<MachinesToInstall> machines = query.getResults(MachinesToInstall.class);

    for (MachinesToInstall machine : machines) {
      MachinesToInstall merged = entityManager.merge(machine);
      if (merged != null) {
        entityManager.remove(merged);
      }
      entityManager.flush();
    }
  }

  @Override
  public boolean isAddressChanged(EntityManager entityManager, Addr addr, String cmrIssuingCntry, boolean computedChangeInd) {

    boolean machineUpdated = isMachineUpdated(entityManager, addr);
    if (computedChangeInd || machineUpdated)
      return true;
    else
      return computedChangeInd;
  }

  public boolean isMachineUpdated(EntityManager entityManager, Addr addr) {

    boolean machineUpdated = false;
    String sql = ExternalizedQuery.getSql("MACHINES.COUNT_IMP_MACHINES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

    List<MachinesToInstall> machinesImp = query.getResults(MachinesToInstall.class);

    String sql2 = ExternalizedQuery.getSql("MACHINES.COUNT_NEW_MACHINES");
    PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
    query2.setParameter("REQ_ID", addr.getId().getReqId());
    query2.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query2.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

    List<MachinesToInstall> machinesNew = query.getResults(MachinesToInstall.class);

    if (machinesImp != null && machinesImp.size() > 0 && machinesNew != null && machinesNew.size() > 0) {
      machineUpdated = true;
    }
    return machineUpdated;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
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
    map.put("##MachineType", "machineTyp");
    map.put("##RequestReason", "reqReason");
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
    map.put("##SitePartyID", "sitePartyId");
    map.put("##MachineSerialNo", "machineSerialNo");
    map.put("##SAPNumber", "sapNo");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Company", "company");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##EngineeringBo", "engineeringBo");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##CustomerName1", "custNm1");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName2", "custNm2");
    map.put("##CustomerName4", "custNm4");
    map.put("##PostalCode", "postCd");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##CountrySubRegion", "countryUse");
    map.put("##LocalTax_FI", "taxCd1");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }
}
