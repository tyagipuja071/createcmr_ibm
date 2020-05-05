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
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
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
 * Handler for France
 * 
 * 
 */
public class FRHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(FRHandler.class);
  private static final String[] FR_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "SearchTerm", "Company", "INACType", "SpecialTaxCd", "TransportZone",
      "Affiliate", "SitePartyID" };

  public static final String DUMMY_SIRET_ADDRESS = "ZSIR";

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {

      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;
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

      record.setCmrName2Plain(record.getCmrName2Plain());
      record.setCmrDept(null);
      converted.add(record);

      // add a dummy address for dummy SIRET processing
      FindCMRRecordModel dummy = new FindCMRRecordModel();
      dummy.setCmrAddrTypeCode(DUMMY_SIRET_ADDRESS);
      dummy.setCmrAddrSeq("X");

      converted.add(dummy);
    } else {
      // import process:
      // a. Import ZS01 record from RDc, only 1
      // b. Import Installing addresses from RDc if found
      // c. Import EplMailing from RDc, if found. This will also be an
      // installing in RDc
      // d. Import all shipping, fiscal, and mailing from SOF

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
          boolean importShipping = true;

          if (importShipping) {
            LOG.debug("Shipping Sequences is not empty. Importing " + sequences.size() + " shipping addresses.");
            for (String seq : sequences) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping_" + seq + "_", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }
          }
        } else {
          LOG.debug("Shipping Sequences is empty. ");
          record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping", zi01Map);
          if (record != null)
            converted.add(record);

        }

        record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "Mailing", zi01Map);
        if (record != null) {
          converted.add(record);
        }
        record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
        if (record != null) {
          converted.add(record);
        }
        record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "EplMailing", zi01Map);
        if (record != null) {
          converted.add(record);
        }
        record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD02.toString(), "CtryUseH", zi01Map);
        if (record != null) {
          converted.add(record);
        }

      }
    }
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    String line1 = this.currentImportValues.get(addressKey + "Address1");
    String line2 = this.currentImportValues.get(addressKey + "Address2");
    String line3 = this.currentImportValues.get(addressKey + "Address3");
    String line4 = this.currentImportValues.get(addressKey + "Address4");
    String line5 = this.currentImportValues.get(addressKey + "Address5");
    String line6 = this.currentImportValues.get(addressKey + "Address6");

    int lineCount = 0;
    for (String line : Arrays.asList(line1, line2, line3, line4, line5, line6)) {
      if (!StringUtils.isBlank(line)) {
        LOG.trace("line" + lineCount + line);
        lineCount++;
      }
    }
    // line 1 is always customer name 1
    address.setCmrName1Plain(line1);

    // line 2 is always customer name2
    address.setCmrName2Plain(line2);

    // line 3 is always customer name3
    address.setCmrName3(line3);

    // line 4 is street
    address.setCmrStreet(line4);

    // line 5
    // Domestic, DOM, Monaco ---Street Continuation
    // Cross Boarder, Andorra, Tunisia, Algeria---Postal code + City (starting
    // on the 12th position!!)
    // TOM ---Postal code + City

    // Cross Boarder, Andorra, Tunisia, Algeria / TOM
    if ((isCountry(entityManager, line6) || !first5CharIsDigital(line6)) && line5 != null) {
      String postalCD = null;
      if (line5.length() > 5) {
        postalCD = line5.substring(0, 5);
      } else {
        postalCD = line5;
      }
      address.setCmrPostalCode(postalCD);

      if (line5.length() > 11) {
        if (line5.substring(5, 11).equalsIgnoreCase("      ")) {
          address.setCmrCity(line5.substring(11));
        } else {
          address.setCmrCity(line5.substring(5));
        }
      } else {
        address.setCmrCity(line5.substring(5));
      }

      address.setCmrCountry(line6);
    }

    // line6
    // Domestic, DOM, Monaco---Postal code + City
    // Cross Boarder, Andorra, Tunisia, Algeria---Country
    // TOM---Country

    // Domestic, DOM, Monaco
    if (!isCountry(entityManager, line6) && first5CharIsDigital(line6)) {
      if ("ZP01".equalsIgnoreCase(address.getCmrAddrTypeCode().toString())) {
        address.setCmrPOBox(line5);
      } else if ("-/x".equalsIgnoreCase(line5)) {
        // do nothing for blank
      } else {
        address.setCmrStreetAddressCont(line5);
      }

      String postalCD = "";
      if (line6.length() > 5) {
        postalCD = line6.substring(0, 5);
        address.setCmrPostalCode(postalCD);
        address.setCmrCity(line6.substring(5));
      }
    }

    String phone = this.currentImportValues.get(addressKey + "Phone");
    String contact = this.currentImportValues.get(addressKey + "Contact");

    if (!StringUtils.isEmpty(phone)) {
      address.setCmrCustPhone(phone);
    }
    if (!StringUtils.isEmpty(contact)) {
      if (StringUtils.isEmpty(address.getCmrCustPhone()) && (contact.matches(".*[\\(]{0,1}\\d{3,}") || contact.matches("[\\(]{0,1}\\d{3,}.*"))) {
        String[] parts = contact.split("[, ]");
        StringBuilder sbCont = new StringBuilder();
        StringBuilder sbPhone = new StringBuilder();
        for (String part : parts) {
          if (part.matches("[\\(]{0,1}\\d{3,}.*")) {
            sbPhone.append(sbPhone.length() > 0 ? " " : "");
            sbPhone.append(part);
          } else {
            sbCont.append(sbCont.length() > 0 ? " " : "");
            sbCont.append(part);
          }
        }
        address.setCmrCustPhone(sbPhone.toString());
        address.setCmrName4(sbCont.toString());
      } else {
        address.setCmrName4(contact);
      }
    }
    // trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Customer Name/ Additional Address Information: " + address.getCmrName3());
    LOG.trace("Street: " + address.getCmrStreet());
    LOG.trace("Postal Code: " + address.getCmrPostalCode());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Country: " + address.getCmrCountry());

  }

  private boolean isCountry(EntityManager entityManager, String string) {
    if (string != null && string.length() > 0) {
      String sql = ExternalizedQuery.getSql("QUERY.CHECK.CNTRY_BY_DESC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY_DESC", string);
      List<String> results = query.getResults(String.class);
      if (results != null && !results.isEmpty()) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private boolean first5CharIsDigital(String string) {
    String testStr = string;
    if (testStr != null && testStr.length() > 5) {
      testStr = testStr.substring(0, 5);
      String formatRegExp = "[0-9]{5}";
      return testStr.matches(formatRegExp);
    } else {
      return false;
    }
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "Installing";
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
    data.setCurrencyCd(this.currentImportValues.get("CurrencyCode"));
    LOG.trace("Currency: " + data.getCurrencyCd());
    String sBO = this.currentImportValues.get("SBO");
    if (!StringUtils.isEmpty(sBO) && sBO.length() >= 4)
      data.setSalesBusOffCd(sBO.substring(1, 4));
    String iBO = this.currentImportValues.get("IBO");
    if (!StringUtils.isEmpty(iBO) && iBO.length() > 3)
      data.setInstallBranchOff(iBO.substring(iBO.length() - 3, iBO.length()));
    String siret = this.currentImportValues.get("Siret");
    LOG.trace("SIRET: " + siret);
    if (!StringUtils.isEmpty(siret))
      data.setTaxCd1(siret);
    String collCd = this.currentImportValues.get("CollectionCode");
    String sr = this.currentImportValues.get("SR");
    String TaxCode = this.currentImportValues.get("TaxCode");
    String embargoCd = this.currentImportValues.get("EmbargoCode");
    String topListeSpeciale = this.currentImportValues.get("TopListeSpeciale");
    if (!StringUtils.isEmpty(collCd))
      data.setCollectionCd(collCd);
    if (!StringUtils.isEmpty(sr))
      data.setRepTeamMemberNo(sr);
    if (!StringUtils.isEmpty(TaxCode))
      data.setTaxCd2(TaxCode);
    if (!StringUtils.isEmpty(embargoCd))
      data.setEmbargoCd(embargoCd);
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());
    if (!StringUtils.isEmpty(topListeSpeciale))
      data.setCommercialFinanced(topListeSpeciale);
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    if (address.getCustNm1() == null && currentRecord.getCmrName1Plain() != null) {
      address.setCustNm1(currentRecord.getCmrName1Plain().trim());
    }
    if (address.getCustNm2() == null && currentRecord.getCmrName2Plain() != null) {
      address.setCustNm2(currentRecord.getCmrName2Plain().trim());
    }
    if (address.getCustNm3() == null && currentRecord.getCmrName3() != null) {
      address.setCustNm3(currentRecord.getCmrName3().trim());
    }
    if (address.getAddrTxt() == null && currentRecord.getCmrStreet() != null) {
      address.setAddrTxt(currentRecord.getCmrStreet().trim());
    }
    if (address.getAddrTxt2() == null && currentRecord.getCmrStreetAddressCont() != null) {
      address.setAddrTxt2(currentRecord.getCmrStreetAddressCont().trim());
    }

    address.setCustPhone(currentRecord.getCmrCustPhone());
    address.setCustNm4(currentRecord.getCmrName4());

    // 1417363 - set Address Sequence 5 digital on import
    if (StringUtils.isEmpty(currentRecord.getCmrAddrSeq())) {
      address.getId().setAddrSeq("00001");
    } else {
      address.getId().setAddrSeq(StringUtils.leftPad(currentRecord.getCmrAddrSeq(), 5, '0'));
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType())) {
      address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
    }

  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCustPrefLang("E");
    data.setCmrOwner("IBM");
    data.setSensitiveFlag(CmrConstants.REGULAR);
    data.setCurrencyCd("EU");
    if (SystemLocation.FRENCH_POLYNESIA_TAHITI.equalsIgnoreCase(data.getCountryUse()))
      data.setCurrencyCd("FP");
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
    request.setSORTL(data.getSalesBusOffCd());
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    if(admin.getReqType().equals("U")){
      setAbbrevNameOnDataSave(entityManager,data);
    }
    // autoSetAbbrevNmAfterImport(entityManager, admin, data);
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    setAbbrevNmBeforeAddrSave(entityManager, addr);
    setAbbrevLocnBeforeAddrSave(entityManager, addr);
    setDummySIRETBeforeAddrSave(entityManager, addr);

    /*
     * add a dummy address for SIRET update to be triggered at the end of the MQ
     * processing
     */
    addDummyAddressAddressForSIRETUpdate(entityManager, addr);
  }

  /**
   * Add a dummy address for SIRET update to be triggered at the end of the MQ.
   * This address should not be shown in the UI
   * 
   * @param entityManager
   * @param addr
   * @throws Exception
   */
  private void addDummyAddressAddressForSIRETUpdate(EntityManager entityManager, Addr addr) throws Exception {
    if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType())) {
      boolean createDummy = shouldAddDummyAddress(entityManager, DUMMY_SIRET_ADDRESS, addr.getId().getReqId());
      if (createDummy) {
        LOG.debug("Adding dummy address for SIRET processing");
        Addr dummyAddr = new Addr();
        AddrPK newPk = new AddrPK();
        newPk.setReqId(addr.getId().getReqId());
        newPk.setAddrType(DUMMY_SIRET_ADDRESS);
        newPk.setAddrSeq("X");

        dummyAddr.setImportInd(CmrConstants.YES_NO.N.toString());
        dummyAddr.setId(newPk);

        entityManager.persist(dummyAddr);
        entityManager.flush();
      }
    }
  }

  /**
   * Gets {@link Addr} records by type
   * 
   * @param entityManager
   * @param addrType
   * @param reqId
   * @return
   */
  private boolean shouldAddDummyAddress(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.FR.CHECKDUMMY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    return query.exists();
  }

  @Override
  public void doFilterAddresses(List<AddressModel> results) {
    // do not show ZSIR (dummy) address on UI
    List<AddressModel> addrsToRemove = new ArrayList<AddressModel>();
    for (AddressModel addrModel : results) {
      if (DUMMY_SIRET_ADDRESS.equalsIgnoreCase(addrModel.getAddrType())) {
        addrsToRemove.add(addrModel);
      }
    }
    results.removeAll(addrsToRemove);
  }

  private void setDummySIRETBeforeAddrSave(EntityManager entityManager, Addr addr) {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    AdminPK adminpk = new AdminPK();
    adminpk.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminpk);
    if (!StringUtils.isEmpty(data.getTaxCd1()) && "U".equalsIgnoreCase(admin.getReqType()))
      return;

    String dummySIRETValue = null;
    String custLocNumValue = null;
    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();
    String countryUse = data.getCountryUse();
    if (StringUtils.isEmpty(custSubGrp)) {
      return;
    }

    if ("ZS01".equals(addr.getId().getAddrType())) {
      if (!StringUtils.isEmpty(addr.getLandCntry()) && !"FR".equalsIgnoreCase(addr.getLandCntry())) {
        String cusLocCntySql = ExternalizedQuery.getSql("QUERY.CUSLOC.GET.LOCN_BY_CNTRY");
        PreparedQuery cusLocCntyQuery = new PreparedQuery(entityManager, cusLocCntySql);
        cusLocCntyQuery.setParameter("LANDED_CNTRY", addr.getLandCntry());
        List<String> cusLocCntyResults = cusLocCntyQuery.getResults(String.class);
        if (cusLocCntyResults != null && !cusLocCntyResults.isEmpty()) {
          custLocNumValue = cusLocCntyResults.get(0);
        }
      }
      if ((StringUtils.isEmpty(custLocNumValue) || custLocNumValue == null) && addr.getCity1() != null && addr.getPostCd() != null) {
        String cusLocCitySql = ExternalizedQuery.getSql("QUERY.CUSLOC.GET.LOCN_BY_CITY_PSCD");
        PreparedQuery cusLocCityQuery = new PreparedQuery(entityManager, cusLocCitySql);
        cusLocCityQuery.setParameter("CITY", addr.getCity1());
        cusLocCityQuery.setParameter("POST_CD", addr.getPostCd());
        List<String> cusLocCityResults = cusLocCityQuery.getResults(String.class);
        if (cusLocCityResults != null && !cusLocCityResults.isEmpty()) {
          custLocNumValue = cusLocCityResults.get(0);
        }
      }
    }
    if ("COMME".equalsIgnoreCase(custSubGrp) || "FIBAB".equalsIgnoreCase(custSubGrp)) {
      if ("706MC".equalsIgnoreCase(countryUse) || "706VU".equalsIgnoreCase(countryUse) || "706PF".equalsIgnoreCase(countryUse)
          || "706YT".equalsIgnoreCase(countryUse) || "706NC".equalsIgnoreCase(countryUse) || "706AD".equalsIgnoreCase(countryUse)
          || "706DZ".equalsIgnoreCase(countryUse) || "706TN".equalsIgnoreCase(countryUse)) {
        if (!StringUtils.isEmpty(custLocNumValue) || custLocNumValue != null) {
          dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
          data.setTaxCd1(dummySIRETValue);
        }
      }
    } else if ("PRICU".equalsIgnoreCase(custSubGrp)) {
      if (!StringUtils.isEmpty(custLocNumValue) || custLocNumValue != null) {
        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
        data.setTaxCd1(dummySIRETValue);
      }
    } else if ("IBMEM".equalsIgnoreCase(custSubGrp)) {
      if (!StringUtils.isEmpty(custLocNumValue) || custLocNumValue != null) {
        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
        data.setTaxCd1(dummySIRETValue);
      }
    }

    if ("CROSS".equalsIgnoreCase(custGrp)) {
      if (!StringUtils.isEmpty(custLocNumValue) || custLocNumValue != null) {
        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
        data.setTaxCd1(dummySIRETValue);
      }
    }
  }

  private void setAbbrevNmBeforeAddrSave(EntityManager entityManager, Addr addr) {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);

    String abbrevNmValue = null;
    String singleIndValue = null;
    String departmentNumValue = null;
    String cntryCd = null;

    boolean havingZS01 = false;
    String checkZS01Sql = ExternalizedQuery.getSql("QUERY.GET.ADDR_BY_REQID_TYPE");
    PreparedQuery checkZS01Query = new PreparedQuery(entityManager, checkZS01Sql);
    checkZS01Query.setParameter("REQ_ID", data.getId().getReqId());
    checkZS01Query.setParameter("ADDR_TYPE", "ZS01");
    List<String> checkZS01Results = checkZS01Query.getResults(String.class);
    if (checkZS01Results != null && !checkZS01Results.isEmpty()) {
      havingZS01 = true;
    }

    boolean havingZD02 = false;
    String checkZD02Sql = ExternalizedQuery.getSql("QUERY.GET.ADDR_BY_REQID_TYPE");
    PreparedQuery checkZD02Query = new PreparedQuery(entityManager, checkZD02Sql);
    checkZD02Query.setParameter("REQ_ID", data.getId().getReqId());
    checkZD02Query.setParameter("ADDR_TYPE", "ZD02");
    List<String> checkZD02Results = checkZD02Query.getResults(String.class);
    if (checkZD02Results != null && !checkZD02Results.isEmpty()) {
      havingZD02 = true;
    }

    if (admin.getReqType().equalsIgnoreCase("C") && "ZS01".equals(addr.getId().getAddrType())) {

      abbrevNmValue = addr.getCustNm1();
      cntryCd = addr.getLandCntry();

      if (data.getCustSubGrp() == null) {
        return;
      } else if (data.getCustSubGrp() != null && abbrevNmValue != null) {
        if (data.getCustSubGrp().equalsIgnoreCase("INTER") || data.getCustSubGrp().equalsIgnoreCase("CBTER")) {
          if (cntryCd.equalsIgnoreCase("DZ")) {
            departmentNumValue = "0371";
          } else if (cntryCd.equalsIgnoreCase("TN")) {
            departmentNumValue = "0382";
          } else if (cntryCd.equalsIgnoreCase("YT") || cntryCd.equalsIgnoreCase("RE") || cntryCd.equalsIgnoreCase("VU")) {
            departmentNumValue = "0381";
          } else if (cntryCd.equalsIgnoreCase("MQ")) {
            departmentNumValue = "0385";
          } else if (cntryCd.equalsIgnoreCase("GP")) {
            departmentNumValue = "0392";
          } else if (cntryCd.equalsIgnoreCase("GF") || cntryCd.equalsIgnoreCase("PM")) {
            departmentNumValue = "0388";
          } else if (cntryCd.equalsIgnoreCase("NC") || cntryCd.equalsIgnoreCase("PF")) {
            departmentNumValue = "0386";
          } else {
            departmentNumValue = data.getIbmDeptCostCenter();
          }
          singleIndValue = departmentNumValue;
        } else if (data.getCustSubGrp().equalsIgnoreCase("BPIEU") || data.getCustSubGrp().equalsIgnoreCase("BPUEU")
            || data.getCustSubGrp().equalsIgnoreCase("CBIEU") || data.getCustSubGrp().equalsIgnoreCase("CBUEU")) {
          singleIndValue = "R5";
        } else if (data.getCustSubGrp().equalsIgnoreCase("INTSO") || data.getCustSubGrp().equalsIgnoreCase("CBTSO")) {
          singleIndValue = "FM";
        } else if (data.getCustSubGrp().equalsIgnoreCase("LCIFF") || data.getCustSubGrp().equalsIgnoreCase("LCIFL")
            || data.getCustSubGrp().equalsIgnoreCase("OTFIN") || data.getCustSubGrp().equalsIgnoreCase("CBIFF")
            || data.getCustSubGrp().equalsIgnoreCase("CBIFL") || data.getCustSubGrp().equalsIgnoreCase("CBFIN")) {
          singleIndValue = "F3";
        } else if (data.getCustSubGrp().equalsIgnoreCase("LEASE") || data.getCustSubGrp().equalsIgnoreCase("CBASE")) {
          singleIndValue = "L3";
        } else {
          singleIndValue = "D3";
        }
        if (havingZD02) {
          singleIndValue = "DF";
        }
        if (abbrevNmValue != null && abbrevNmValue.length() > 19
            && (!data.getCustSubGrp().equalsIgnoreCase("INTER") && !data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          abbrevNmValue = abbrevNmValue.substring(0, 19);
        } else if (abbrevNmValue != null && abbrevNmValue.length() < 19
            && (!data.getCustSubGrp().equalsIgnoreCase("INTER") && !data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          for (int i = abbrevNmValue.length(); i < 19; i++) {
            abbrevNmValue += ' ';
          }
        } else if (abbrevNmValue != null && abbrevNmValue.length() > 17
            && (data.getCustSubGrp().equalsIgnoreCase("INTER") || data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          abbrevNmValue = abbrevNmValue.substring(0, 17);
        } else if (abbrevNmValue != null && abbrevNmValue.length() < 17
            && (data.getCustSubGrp().equalsIgnoreCase("INTER") || data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          for (int i = abbrevNmValue.length(); i < 17; i++) {
            abbrevNmValue += ' ';
          }
        }

        if (singleIndValue != null && abbrevNmValue != null) {
          abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
          data.setAbbrevNm(abbrevNmValue);
        }
      }
    } else if (admin.getReqType().equalsIgnoreCase("C") && "ZD02".equals(addr.getId().getAddrType()) && havingZS01) {

      String abbNmSql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP");
      PreparedQuery abbNmQuery = new PreparedQuery(entityManager, abbNmSql);
      abbNmQuery.setParameter("REQ_ID", data.getId().getReqId());
      abbNmQuery.setParameter("ADDR_TYPE", "ZS01");
      List<String> abbNmResults = abbNmQuery.getResults(String.class);
      if (abbNmResults != null && !abbNmResults.isEmpty()) {
        abbrevNmValue = abbNmResults.get(0);
      } else if (abbNmResults == null || abbNmResults.isEmpty()) {
        data.setAbbrevNm("");
        entityManager.merge(data);
        entityManager.flush();
        return;
      }

      singleIndValue = "DF";

      if (abbrevNmValue != null && abbrevNmValue.length() > 19) {
        abbrevNmValue = abbrevNmValue.substring(0, 19);
      } else if (abbrevNmValue != null && abbrevNmValue.length() < 19) {
        for (int i = abbrevNmValue.length(); i < 19; i++) {
          abbrevNmValue += ' ';
        }
      }

      if (singleIndValue != null && abbrevNmValue != null) {
        abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
        data.setAbbrevNm(abbrevNmValue);
      }
    } else if (admin.getReqType().equalsIgnoreCase("U") && "ZS01".equals(addr.getId().getAddrType())){
      abbrevNmValue = addr.getCustNm1();
      if(!isZS01CustNameUpdated(entityManager,data.getId().getReqId(),abbrevNmValue)){
        return;
      }
      if(StringUtils.isNotBlank(abbrevNmValue) && abbrevNmValue.length()>22){
        abbrevNmValue.substring(0, 22);
      }
      data.setAbbrevNm(abbrevNmValue);
    }

    entityManager.merge(data);
    entityManager.flush();
  }

  private void setAbbrevLocnBeforeAddrSave(EntityManager entityManager, Addr addr) {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);

    String abbrevLocnValue = null;
    String countryUse = null;
    String countyCd = null;
    if (admin.getReqType().equalsIgnoreCase("C") && "ZS01".equals(addr.getId().getAddrType())) {

      countryUse = data.getCountryUse();
      if (!(StringUtils.isEmpty(countryUse))) {
        if (countryUse.length() > 3) {
          countyCd = countryUse.substring(3, 5);
        }
      } else if (StringUtils.isEmpty(countryUse)) {
        countyCd = "FR";
      }

      if (data.getCustGrp() == null) {
        return;
      } else if (data.getCustGrp().equalsIgnoreCase("LOCAL")) {
        if (!"VU".equalsIgnoreCase(countyCd) && !"PF".equalsIgnoreCase(countyCd) && !"YT".equalsIgnoreCase(countyCd)
            && !"NC".equalsIgnoreCase(countyCd) && !"WF".equalsIgnoreCase(countyCd) && !"AD".equalsIgnoreCase(countyCd)
            && !"DZ".equalsIgnoreCase(countyCd) && !"TN".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = addr.getCity1();
        } else if ("VU".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Vanuatu";
        } else if ("PF".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Polynesie Francaise";
        } else if ("YT".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Mayotte";
        } else if ("NC".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Noumea";
        } else if ("WF".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Wallis & Futuna";
        } else if ("AD".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Andorra";
        } else if ("DZ".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Algeria";
        } else if ("TN".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Tunisia";
        }
      } else {
        abbrevLocnValue = getLandCntryDesc(entityManager, addr.getLandCntry());
      }

      if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
        abbrevLocnValue = abbrevLocnValue.substring(0, 12);
      }
      data.setAbbrevLocn(abbrevLocnValue);
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private String getLandCntryDesc(EntityManager entityManager, String landCntryCd) {
    String landCntryDesc = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETCNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", landCntryCd);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      landCntryDesc = results.get(0);
    }
    entityManager.flush();
    return landCntryDesc;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(FR_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    autoSetAbbrevNmAfterImport(entityManager, admin, data);
    autoSetAbbrevLocnAfterImport(entityManager, admin, data);
    autoSetDummySIRETAfterImport(entityManager, admin, data);
  }

  private void autoSetDummySIRETAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String dummySIRETValue = null;
    String custLocNumValue = null;
    String cntryCd = null;
    String city = null;
    String postCd = null;
    String custGrp = data.getCustGrp();
    String custSubGrp = data.getCustSubGrp();
    String countryUse = data.getCountryUse();
    if (StringUtils.isEmpty(custSubGrp)) {
      return;
    }
    String cntryCdSql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CNTRY_CITY_PSCD.BY_REQID_ADDRTYPE");
    PreparedQuery cntryCdQuery = new PreparedQuery(entityManager, cntryCdSql);
    cntryCdQuery.setParameter("REQ_ID", data.getId().getReqId());
    cntryCdQuery.setParameter("ADDR_TYPE", "ZS01");
    // List<String> cntryCdResults = cntryCdQuery.getResults(String.class);
    List<Object[]> cntryCdResults = cntryCdQuery.getResults();
    if (cntryCdResults != null && !cntryCdResults.isEmpty()) {
      cntryCd = (String) cntryCdResults.get(0)[0];
      city = (String) cntryCdResults.get(0)[1];
      postCd = (String) cntryCdResults.get(0)[2];
    }

    if (!StringUtils.isEmpty(cntryCd) && !"FR".equalsIgnoreCase(cntryCd)) {
      String cusLocCntySql = ExternalizedQuery.getSql("QUERY.CUSLOC.GET.LOCN_BY_CNTRY");
      PreparedQuery cusLocCntyQuery = new PreparedQuery(entityManager, cusLocCntySql);
      cusLocCntyQuery.setParameter("LANDED_CNTRY", cntryCd);
      List<String> cusLocCntyResults = cusLocCntyQuery.getResults(String.class);
      if (cusLocCntyResults != null && !cusLocCntyResults.isEmpty()) {
        custLocNumValue = cusLocCntyResults.get(0);
      }
    }
    if ((StringUtils.isEmpty(custLocNumValue) || custLocNumValue == null) && city != null && postCd != null) {
      String cusLocCitySql = ExternalizedQuery.getSql("QUERY.CUSLOC.GET.LOCN_BY_CITY_PSCD");
      PreparedQuery cusLocCityQuery = new PreparedQuery(entityManager, cusLocCitySql);
      cusLocCityQuery.setParameter("CITY", city);
      cusLocCityQuery.setParameter("POST_CD", postCd);
      List<String> cusLocCityResults = cusLocCityQuery.getResults(String.class);
      if (cusLocCityResults != null && !cusLocCityResults.isEmpty()) {
        custLocNumValue = cusLocCityResults.get(0);
      }
    }

    if ("COMME".equalsIgnoreCase(custSubGrp) || "FIBAB".equalsIgnoreCase(custSubGrp)) {
      if ("706MC".equalsIgnoreCase(countryUse) || "706VU".equalsIgnoreCase(countryUse) || "706PF".equalsIgnoreCase(countryUse)
          || "706YT".equalsIgnoreCase(countryUse) || "706NC".equalsIgnoreCase(countryUse) || "706AD".equalsIgnoreCase(countryUse)
          || "706DZ".equalsIgnoreCase(countryUse) || "706TN".equalsIgnoreCase(countryUse)) {
        if (!StringUtils.isEmpty(custLocNumValue) || custLocNumValue != null) {
          dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
          data.setTaxCd1(dummySIRETValue);
        }
      }
    } else if ("PRICU".equalsIgnoreCase(custSubGrp) || "IBMEM".equalsIgnoreCase(custSubGrp)) {
      if (!StringUtils.isEmpty(custLocNumValue) || custLocNumValue != null) {
        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
        data.setTaxCd1(dummySIRETValue);
      }
    }

    if ("CROSS".equalsIgnoreCase(custGrp)) {
      if (!StringUtils.isEmpty(custLocNumValue) || custLocNumValue != null) {
        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
        data.setTaxCd1(dummySIRETValue);
      }
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private void autoSetAbbrevNmAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String abbrevNmValue = null;
    String singleIndValue = null;
    String departmentNumValue = null;
    String cntryCd = null;
    boolean havingZD02 = false;

    if (admin.getReqType().equalsIgnoreCase("C")) {

      String abbNmSql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP");
      PreparedQuery abbNmQuery = new PreparedQuery(entityManager, abbNmSql);
      abbNmQuery.setParameter("REQ_ID", data.getId().getReqId());
      abbNmQuery.setParameter("ADDR_TYPE", "ZS01");
      List<String> abbNmResults = abbNmQuery.getResults(String.class);
      if (abbNmResults != null && !abbNmResults.isEmpty()) {
        abbrevNmValue = abbNmResults.get(0);
      }

      String cntryCdSql = ExternalizedQuery.getSql("QUERY.ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE");
      PreparedQuery cntryCdQuery = new PreparedQuery(entityManager, cntryCdSql);
      cntryCdQuery.setParameter("REQ_ID", data.getId().getReqId());
      cntryCdQuery.setParameter("ADDR_TYPE", "ZS01");
      List<String> cntryCdResults = cntryCdQuery.getResults(String.class);
      if (cntryCdResults != null && !cntryCdResults.isEmpty()) {
        cntryCd = cntryCdResults.get(0);
      }

      String checkZD02Sql = ExternalizedQuery.getSql("QUERY.GET.ADDR_BY_REQID_TYPE");
      PreparedQuery checkZD02Query = new PreparedQuery(entityManager, checkZD02Sql);
      checkZD02Query.setParameter("REQ_ID", data.getId().getReqId());
      checkZD02Query.setParameter("ADDR_TYPE", "ZD02");
      List<String> checkZD02Results = checkZD02Query.getResults(String.class);
      if (checkZD02Results != null && !checkZD02Results.isEmpty()) {
        havingZD02 = true;
      }

      if (data.getCustSubGrp() == null) {
        return;
      } else if (data.getCustSubGrp() != null && abbrevNmValue != null) {
        if (data.getCustSubGrp().equalsIgnoreCase("INTER") || data.getCustSubGrp().equalsIgnoreCase("CBTER")) {
          if (cntryCd.equalsIgnoreCase("DZ")) {
            departmentNumValue = "0371";
          } else if (cntryCd.equalsIgnoreCase("TN")) {
            departmentNumValue = "0382";
          } else if (cntryCd.equalsIgnoreCase("YT") || cntryCd.equalsIgnoreCase("RE") || cntryCd.equalsIgnoreCase("VU")) {
            departmentNumValue = "0381";
          } else if (cntryCd.equalsIgnoreCase("MQ")) {
            departmentNumValue = "0385";
          } else if (cntryCd.equalsIgnoreCase("GP")) {
            departmentNumValue = "0392";
          } else if (cntryCd.equalsIgnoreCase("GF") || cntryCd.equalsIgnoreCase("PM")) {
            departmentNumValue = "0388";
          } else if (cntryCd.equalsIgnoreCase("NC") || cntryCd.equalsIgnoreCase("PF")) {
            departmentNumValue = "0386";
          } else {
            departmentNumValue = data.getIbmDeptCostCenter();
          }
          singleIndValue = departmentNumValue;
        } else if (data.getCustSubGrp().equalsIgnoreCase("BPIEU") || data.getCustSubGrp().equalsIgnoreCase("BPUEU")
            || data.getCustSubGrp().equalsIgnoreCase("CBIEU") || data.getCustSubGrp().equalsIgnoreCase("CBUEU")) {
          singleIndValue = "R5";
        } else if (data.getCustSubGrp().equalsIgnoreCase("INTSO") || data.getCustSubGrp().equalsIgnoreCase("CBTSO")) {
          singleIndValue = "FM";
        } else if (data.getCustSubGrp().equalsIgnoreCase("LCIFF") || data.getCustSubGrp().equalsIgnoreCase("LCIFL")
            || data.getCustSubGrp().equalsIgnoreCase("OTFIN") || data.getCustSubGrp().equalsIgnoreCase("CBIFF")
            || data.getCustSubGrp().equalsIgnoreCase("CBIFL") || data.getCustSubGrp().equalsIgnoreCase("CBFIN")) {
          singleIndValue = "F3";
        } else if (data.getCustSubGrp().equalsIgnoreCase("LEASE") || data.getCustSubGrp().equalsIgnoreCase("CBASE")) {
          singleIndValue = "L3";
        } else {
          singleIndValue = "D3";
        }

        if (havingZD02) {
          singleIndValue = "DF";
        }

        if (abbrevNmValue != null && abbrevNmValue.length() > 19
            && (!data.getCustSubGrp().equalsIgnoreCase("INTER") && !data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          abbrevNmValue = abbrevNmValue.substring(0, 19);
        } else if (abbrevNmValue != null && abbrevNmValue.length() < 19
            && (!data.getCustSubGrp().equalsIgnoreCase("INTER") && !data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          for (int i = abbrevNmValue.length(); i < 19; i++) {
            abbrevNmValue += ' ';
          }
        } else if (abbrevNmValue != null && abbrevNmValue.length() > 17
            && (data.getCustSubGrp().equalsIgnoreCase("INTER") || data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          abbrevNmValue = abbrevNmValue.substring(0, 17);
        } else if (abbrevNmValue != null && abbrevNmValue.length() < 17
            && (data.getCustSubGrp().equalsIgnoreCase("INTER") || data.getCustSubGrp().equalsIgnoreCase("CBTER"))) {
          for (int i = abbrevNmValue.length(); i < 17; i++) {
            abbrevNmValue += ' ';
          }
        }

        if (singleIndValue != null && abbrevNmValue != null) {
          abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
          data.setAbbrevNm(abbrevNmValue);
        }
      }

    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private void autoSetAbbrevLocnAfterImport(EntityManager entityManager, Admin admin, Data data) {

    String abbrevLocnValue = null;
    String countryUse = null;
    String countyCd = null;
    if (admin.getReqType().equalsIgnoreCase("C")) {

      countryUse = data.getCountryUse();
      if (!(StringUtils.isEmpty(countryUse))) {
        if (countryUse.length() > 3) {
          countyCd = countryUse.substring(3, 5);
        }
      } else if (StringUtils.isEmpty(countryUse)) {
        countyCd = "FR";
      }

      if (data.getCustGrp() == null) {
        return;
      } else if (data.getCustGrp().equalsIgnoreCase("LOCAL")) {
        if (!"VU".equalsIgnoreCase(countyCd) && !"PF".equalsIgnoreCase(countyCd) && !"YT".equalsIgnoreCase(countyCd)
            && !"NC".equalsIgnoreCase(countyCd) && !"WF".equalsIgnoreCase(countyCd) && !"AD".equalsIgnoreCase(countyCd)
            && !"DZ".equalsIgnoreCase(countyCd) && !"TN".equalsIgnoreCase(countyCd)) {
          String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CITY1.BY_REQID_ADDRTYP");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("REQ_ID", data.getId().getReqId());
          query.setParameter("ADDR_TYPE", "ZS01");
          List<String> results = query.getResults(String.class);
          if (results != null && !results.isEmpty()) {
            abbrevLocnValue = results.get(0);
          }
        } else if ("VU".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Vanuatu";
        } else if ("PF".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Polynesie Francaise";
        } else if ("YT".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Mayotte";
        } else if ("NC".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Noumea";
        } else if ("WF".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Wallis & Futuna";
        } else if ("AD".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Andorra";
        } else if ("DZ".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Algeria";
        } else if ("TN".equalsIgnoreCase(countyCd)) {
          abbrevLocnValue = "Tunisia";
        }
      } else if (data.getCustGrp().equalsIgnoreCase("CROSS")) {
        String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", data.getId().getReqId());
        query.setParameter("ADDR_TYPE", "ZS01");
        List<String> results = query.getResults(String.class);
        if (results != null && !results.isEmpty()) {
          abbrevLocnValue = results.get(0);
        }
      }

      if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
        abbrevLocnValue = abbrevLocnValue.substring(0, 12);
      }
      data.setAbbrevLocn(abbrevLocnValue);
    }
    entityManager.merge(data);
    entityManager.flush();

  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX",
        "CUST_PHONE"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {

    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (SystemLocation.FRANCE.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CommercialFinanced", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.FRANCE.equals(cmrCountry) && RequestSummaryService.TYPE_CUSTOMER.equals(type)
        && !equals(oldData.getCurrencyCd(), newData.getCurrencyCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CurrencyCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCurrencyCd(), "CurrencyCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCurrencyCd(), "CurrencyCode", cmrCountry));
      results.add(update);
    }
    if (SystemLocation.FRANCE.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getInstallBranchOff(), newData.getInstallBranchOff())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "InstallBranchOff", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getInstallBranchOff(), "InstallBranchOff", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getInstallBranchOff(), "InstallBranchOff", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##DoubleCreate", "identClient");
    map.put("##CMROwner", "cmrOwner");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##DuplicateCMR", "dupCmrIndc");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustLang", "custPrefLang");
    map.put("##LocalTax2", "taxCd2");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
    map.put("##BPRelationType", "bpRelType");
    map.put("##LocalTax1", "taxCd1");
    map.put("##CAP", "capInd");
    map.put("##RequestReason", "reqReason");
    map.put("##POBox", "poBox");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##CustPhone", "custPhone");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##City1", "city1");
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
    map.put("##BGLDERule", "bgRuleId");
    map.put("##InstallBranchOff", "installBranchOff");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CountrySubRegion", "countryUse");
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    map.put("##IERPSitePrtyId", "ierpSitePrtyId");
    map.put("##SAPNumber", "sapNo");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##CustomerName1", "custNm1");
    map.put("##CustomerName2", "custNm2");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName3", "custNm3");
    map.put("##CustomerName4", "custNm4");
    map.put("##CurrencyCode", "currencyCd");
    map.put("##PostalCode", "postCd");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##PrivIndc", "privIndc");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }
  
  private void setAbbrevNameOnDataSave(EntityManager entityManager, Data data){
    String abbrevNmValue ="";
    String abbNmSql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP");
    PreparedQuery abbNmQuery = new PreparedQuery(entityManager, abbNmSql);
    abbNmQuery.setParameter("REQ_ID", data.getId().getReqId());
    abbNmQuery.setParameter("ADDR_TYPE", "ZS01");
    List<String> abbNmResults = abbNmQuery.getResults(String.class);
    if (abbNmResults != null && !abbNmResults.isEmpty()) {
      abbrevNmValue = abbNmResults.get(0);
      if(!isZS01CustNameUpdated(entityManager,data.getId().getReqId(),abbrevNmValue)){
        return;
      }
    } else if (abbNmResults == null || abbNmResults.isEmpty()) {
      abbrevNmValue="";
    } 
    if(StringUtils.isNotBlank(abbrevNmValue) && abbrevNmValue.length()>22){
      abbrevNmValue.substring(0, 22);
    }
    data.setAbbrevNm(abbrevNmValue);
  
      entityManager.merge(data);
      entityManager.flush();
      return;
  }
  
  private boolean isZS01CustNameUpdated(EntityManager entityManager, Long requestId, String currentCustNm){
    boolean isNameUpdated= false;
    String sql = ExternalizedQuery.getSql("QUERY.GETZS01OLDCUSTNAME");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", requestId);
    List<String> results = query.getResults(String.class);
    if(results!=null && !results.isEmpty()){
      String oldCustNm=results.get(0);
      if(!oldCustNm.equals(currentCustNm)){
        isNameUpdated=true;
      }
    }
    return isNameUpdated;
  }
}
