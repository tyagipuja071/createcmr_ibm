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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for BELUX
 * 
 * @author Rangoli Saxena
 * 
 */
public class BELUXHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(BELUXHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  static {

    LANDED_CNTRY_MAP.put(SystemLocation.BELGIUM, "BE");
  }

  private static final String[] BELUX_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "GeoLocationCode", "Affiliate", "Company", "CAP", "CMROwner",
      "CustClassCode", "LocalTax2", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "Office", "Floor", "Building", "County",
      "City2", "Department", "SalRepNameNo", "EngineeringBo", "SpecialTaxCd" };

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

      // name3 in rdc = InstallingAddress3 on SOF
      if (!StringUtils.isEmpty(record.getCmrName3())) {
        record.setCmrName4(record.getCmrName3());
        record.setCmrName3(null);
      }

      if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
        record.setCmrAddrSeq("00001");
      } else {
        record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
      }
      record.setCmrName2Plain(record.getCmrName2Plain());
      // record.setCmrTaxOffice(this.currentImportValues.get("InstallingAddressT"));
      record.setCmrDept(record.getCmrDept());

      // transfer formatted values from SOF
      FindCMRRecordModel installingSOF = createAddress(entityManager, mainRecord.getCmrIssuedBy(), "ZS01", "Installing",
          new HashMap<String, FindCMRRecordModel>());
      if (installingSOF != null) {
        record.setCmrName1Plain(installingSOF.getCmrName1Plain());
        record.setCmrName2Plain(installingSOF.getCmrName2Plain());
        record.setCmrName3(installingSOF.getCmrName3());
        record.setCmrName4(installingSOF.getCmrName4());
        record.setCmrDept(installingSOF.getCmrDept());
        record.setCmrStreetAddress(installingSOF.getCmrStreetAddress());
        record.setCmrStreetAddressCont(installingSOF.getCmrStreetAddressCont());
        record.setCmrCustPhone(installingSOF.getCmrCustPhone());
        record.setCmrCity(installingSOF.getCmrCity());
        record.setCmrCountryLanded(installingSOF.getCmrCountryLanded());
        if (!StringUtils.isBlank(installingSOF.getCmrCountryLanded()) && "LU".equalsIgnoreCase(installingSOF.getCmrCountryLanded())) {
          record.setCmrPostalCode(installingSOF.getCmrPostalCode().replaceAll("[^0-9]", "").trim());
        } else {
          record.setCmrPostalCode(installingSOF.getCmrPostalCode());
        }
      }
      converted.add(record);

    } else {

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

          if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
            record.setCmrAddrSeq("00001");
          } else {
            record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
          }

          // transfer formatted values from SOF
          FindCMRRecordModel installingSOF = createAddress(entityManager, mainRecord.getCmrIssuedBy(), "ZS01", "Installing",
              new HashMap<String, FindCMRRecordModel>());
          if (installingSOF != null) {
            record.setCmrName1Plain(installingSOF.getCmrName1Plain());
            record.setCmrName2Plain(installingSOF.getCmrName2Plain());
            record.setCmrName3(installingSOF.getCmrName3());
            record.setCmrName4(installingSOF.getCmrName4());
            record.setCmrDept(installingSOF.getCmrDept());
            record.setCmrStreetAddress(installingSOF.getCmrStreetAddress());
            record.setCmrStreetAddressCont(installingSOF.getCmrStreetAddressCont());
            record.setCmrCustPhone(installingSOF.getCmrCustPhone());
            record.setCmrCity(installingSOF.getCmrCity());
            record.setCmrCountryLanded(installingSOF.getCmrCountryLanded());
            if (!StringUtils.isBlank(installingSOF.getCmrCountryLanded()) && "LU".equalsIgnoreCase(installingSOF.getCmrCountryLanded())) {
              record.setCmrPostalCode(installingSOF.getCmrPostalCode().replaceAll("[^0-9]", "").trim());
            } else {
              record.setCmrPostalCode(installingSOF.getCmrPostalCode());
            }
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

  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {

    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Mailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
    if (record != null) {
      converted.add(record);
    }
    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {

    LOG.debug("handleSOFAddressImport called... ");

    // line1 = Customer name
    // line2 = Customer Name Cont/Att/Street Street No
    // line3 = Att/Street Street No/POBOX
    // line4 = Street/POBOX/Postal code City
    // line5 = postal code city/country for cross-border only
    // line6 = not used for domestic/Country for cross-border

    String custName = !StringUtils.isEmpty(getCurrentValue(addressKey, "Address1")) ? getCurrentValue(addressKey, "Address1") : "";
    String countryline = !StringUtils.isEmpty(getCurrentValue(addressKey, "Address6")) ? getCurrentValue(addressKey, "Address6") : "";
    String title = !StringUtils.isEmpty(getCurrentValue(addressKey, "Title")) ? getCurrentValue(addressKey, "Title") : "";
    String firstName = !StringUtils.isEmpty(getCurrentValue(addressKey, "NameFirst")) ? getCurrentValue(addressKey, "NameFirst") : "";
    String lastName = !StringUtils.isEmpty(getCurrentValue(addressKey, "NameLast")) ? getCurrentValue(addressKey, "NameLast") : "";
    String street = !StringUtils.isEmpty(getCurrentValue(addressKey, "Street")) ? getCurrentValue(addressKey, "Street") : "";
    String streetNo = !StringUtils.isEmpty(getCurrentValue(addressKey, "StreetNo")) ? getCurrentValue(addressKey, "StreetNo") : "";
    String city = !StringUtils.isEmpty(getCurrentValue(addressKey, "City")) ? getCurrentValue(addressKey, "City") : "";
    String postalCode = !StringUtils.isEmpty(getCurrentValue(addressKey, "ZipCode")) ? getCurrentValue(addressKey, "ZipCode") : "";
    String nameCont = !StringUtils.isEmpty(getCurrentValue(addressKey, "NameCont")) ? getCurrentValue(addressKey, "NameCont") : "";
    String poBox = !StringUtils.isEmpty(getCurrentValue(addressKey, "POBOX")) ? getCurrentValue(addressKey, "POBOX") : "";
    String phone = !StringUtils.isEmpty(getCurrentValue(addressKey, "Phone")) ? getCurrentValue(addressKey, "Phone") : "";

    /*
     * String countryline = getCurrentValue(addressKey, "Address6"); String
     * title = getCurrentValue(addressKey, "Title"); String firstName =
     * getCurrentValue(addressKey, "NameFirst"); String lastName =
     * getCurrentValue(addressKey, "NameLast"); String street =
     * getCurrentValue(addressKey, "Street"); String streetNo =
     * getCurrentValue(addressKey, "StreetNo"); String city =
     * getCurrentValue(addressKey, "City"); String postalCode =
     * getCurrentValue(addressKey, "ZipCode"); String nameCont =
     * getCurrentValue(addressKey, "NameCont"); String poBox =
     * getCurrentValue(addressKey, "POBOX");
     */

    String lineToParse = null;
    String countryCd = null;
    String sbo = !StringUtils.isEmpty(this.currentImportValues.get("SBO")) ? this.currentImportValues.get("SBO") : "";
    boolean hasSbo = !StringUtils.isEmpty(sbo) ? true : false;
    boolean hasPoBox = !StringUtils.isEmpty(poBox) ? true : false;
    boolean hasPostalCode = !StringUtils.isEmpty(postalCode) ? true : false;

    // line 1 = always Name 1
    address.setCmrName1Plain(custName);
    address.setCmrName2Plain(nameCont);
    address.setCmrDept(title);
    address.setCmrName3(firstName);
    address.setCmrName4(lastName);
    address.setCmrStreetAddress(street);
    address.setCmrStreetAddressCont(streetNo);
    address.setCmrCity(city);

    address.setCmrCustPhone(phone);

    if (hasPoBox) {
      // po box parse
      address.setCmrPOBox(poBox.replaceAll("[^0-9]", "").trim());
    }

    // fallback check for CB at line 6
    if (StringUtils.isEmpty(address.getCmrCountryLanded()) && !StringUtils.isEmpty(countryline)) {
      countryCd = getCountryCode(entityManager, countryline);
      if (!StringUtils.isEmpty(countryCd)) {
        address.setCmrCountryLanded(countryCd);
      }
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded()) && StringUtils.isEmpty(countryline) && hasSbo) {
      if ("1".equalsIgnoreCase(sbo.substring(sbo.length() - 1)))
        address.setCmrCountryLanded("LU");
      if ("0".equalsIgnoreCase(sbo.substring(sbo.length() - 1)))
        address.setCmrCountryLanded("BE");
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    if (!StringUtils.isBlank(address.getCmrCountryLanded()) && "LU".equalsIgnoreCase(address.getCmrCountryLanded())) {
      if (hasPostalCode)
        address.setCmrPostalCode(postalCode.replaceAll("[^0-9]", "").trim());
    } else {
      address.setCmrPostalCode(postalCode);
    }

    /*
     * // check TITLE/FIRST/LAST NAME int endIndex = 2;
     * 
     * if (!StringUtils.isEmpty(title) || !StringUtils.isEmpty(firstName) ||
     * !StringUtils.isEmpty(lastName)) { // line 2 is already ATTN
     * address.setCmrDept(title); address.setCmrName3(firstName);
     * address.setCmrName4(lastName); endIndex = 3; }
     * 
     * // parse from lowest line in terms of priority // line 6 reserved for CB
     * for (int lineNo = 5; lineNo >= endIndex; lineNo--) { lineToParse =
     * getCurrentValue(addressKey, "Address" + lineNo); if
     * (!StringUtils.isBlank(lineToParse)) {
     * 
     * // special handling for PO BOX
     * 
     * if (isPOBox(lineToParse) && StringUtils.isBlank(address.getCmrPOBox())) {
     * address.setCmrPOBox(lineToParse.replaceAll("[^0-9]", "").trim());
     * continue; }
     * 
     * // lowest line is city + postal code if
     * (StringUtils.isBlank(address.getCmrCity())) { // this is city and post
     * code, try formatted parsing String city = getCurrentValue(addressKey,
     * "City"); String postalCode = getCurrentValue(addressKey, "ZipCode"); if
     * (!StringUtils.isEmpty(city) && !StringUtils.isEmpty(postalCode)) {
     * address.setCmrCity(city); address.setCmrPostalCode(postalCode); } else {
     * handleCityAndPostCodeBelux(lineToParse, cmrIssuingCntry, address,
     * addressKey); } continue; }
     * 
     * // po box / street when city/postal code has been assigned if
     * (!StringUtils.isBlank(address.getCmrCity()) &&
     * StringUtils.isBlank(address.getCmrStreetAddress())) { if
     * (isPOBox(lineToParse) || StringUtils.isNumeric(lineToParse)) { // po box
     * parse address.setCmrPOBox(lineToParse.replaceAll("[^0-9]", "").trim());
     * continue; } else { // try formatted parsing String street =
     * getCurrentValue(addressKey, "Street"); String streetNo =
     * getCurrentValue(addressKey, "StreetNo"); if (!StringUtils.isBlank(street)
     * && !StringUtils.isBlank(streetNo)) { address.setCmrStreetAddress(street);
     * address.setCmrStreetAddressCont(streetNo); } else {
     * address.setCmrStreetAddress(lineToParse); } continue; } }
     * 
     * if ((!StringUtils.isEmpty(address.getCmrPOBox()) ||
     * !StringUtils.isEmpty(address.getCmrStreet())) && lineNo == 2) { //
     * special case for name con't, street and po box already have values
     * address.setCmrName2Plain(lineToParse); }
     * 
     * } }
     */

    /*
     * countryCd = getCurrentValue(addressKey, "Country"); if
     * (!StringUtils.isBlank(countryCd) && countryCd.trim().length() == 2) {
     * address.setCmrCountryLanded(countryCd); }
     */

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

  protected void handleCityAndPostCodeBelux(String line, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    if (line == null || StringUtils.isEmpty(line)) {
      return;
    }
    String postalCode = "";
    String city = line;
    Pattern pattern = Pattern.compile("[0-9]+");
    Matcher matcher = pattern.matcher(line);
    List<String> numericMatch = new ArrayList<String>();
    while (matcher.find()) {
      numericMatch.add(matcher.group());
    }
    if (!numericMatch.isEmpty()) {
      int postalLength = line.indexOf(numericMatch.get(numericMatch.size() - 1)) + numericMatch.get(numericMatch.size() - 1).length();
      postalCode = line.substring(0, postalLength);
      if (line.substring(line.substring(0, postalLength).length()).trim().startsWith(",")) {
        city = line.substring(line.substring(0, postalLength).length()).trim().substring(1).trim();
      } else {
        city = line.substring(line.substring(0, postalLength).length()).trim();
      }
      address.setCmrPostalCode(postalCode);
      address.setCmrCity(city);
    }
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

    data.setSearchTerm(this.currentImportValues.get("SR"));
    LOG.trace("SR: " + data.getSearchTerm());

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

    data.setCustPrefLang(this.currentImportValues.get("LangCode"));
    LOG.trace("LangCode: " + data.getCustPrefLang());

    data.setTaxCd1(this.currentImportValues.get("TaxCode"));
    LOG.trace("TaxCode: " + data.getTaxCd1());

    data.setIbmDeptCostCenter(this.currentImportValues.get("AccAdBo"));
    LOG.trace("AccAdBo: " + data.getIbmDeptCostCenter());

    data.setInstallBranchOff("");
    data.setInacType("");
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    if (currentRecord.getCmrStreetAddress() != null) {
      String streetAddr = currentRecord.getCmrStreetAddress();
      if (StringUtils.isBlank(currentRecord.getCmrStreetAddressCont())) {
        String strAddrTxt = streetAddr.replaceAll("[0-9]", "");
        String strNo = streetAddr.replaceAll("[^0-9]", "");
        address.setAddrTxt(strAddrTxt);
        address.setAddrTxt2(strNo);
      } else {
        address.setAddrTxt(streetAddr);
        address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
      }
    }
    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    address.setCustNm3(currentRecord.getCmrName3());
    address.setCustNm4(currentRecord.getCmrName4());
    address.setDept(currentRecord.getCmrDept());

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

  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {

  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "ADDR_TXT", "CITY1", "STATE_PROV", "POST_CD", "LAND_CNTRY", "DEPT",
        "PO_BOX", "CUST_PHONE", "TRANSPORT_ZONE", "ADDR_TXT_2"));
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
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEconomicCd(), newData.getEconomicCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EconomicCd2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEconomicCd(), "EconomicCd2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEconomicCd(), "EconomicCd2", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(BELUX_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    if (!equals(addr.getDept(), addr.getDeptOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "", "Title"));
      update.setNewData(addr.getDept());
      update.setOldData(addr.getDeptOld());
      results.add(update);
    }
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##SearchTerm", "searchTerm");
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
    map.put("##CollectionCd", "collectionCd");
    map.put("##VAT", "vat");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CountrySubRegion", "countryUse");
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SitePartyID", "sitePartyId");
    map.put("##SAPNumber", "sapNo");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Department", "dept");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##LocalTax_BE", "taxCd1");
    map.put("##LocalTax_LU", "taxCd1");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##CustomerName1", "custNm1");
    map.put("##CustomerName2", "custNm2");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName3", "custNm3");
    map.put("##CustomerName4", "custNm4");
    map.put("##Enterprise", "enterprise");
    map.put("##PostalCode", "postCd");
    map.put("##TransportZone", "transportZone");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##EconomicCd2", "economicCd");
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
}
