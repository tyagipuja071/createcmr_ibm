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
import org.apache.log4j.PropertyConfigurator;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for MCO Central, East, West Africa
 * 
 * @author Eduard Bernardo
 * 
 */
public class MCOHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(MCOHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.MAURITIUS, "MU");
    LANDED_CNTRY_MAP.put(SystemLocation.MALI, "ML");
    LANDED_CNTRY_MAP.put(SystemLocation.EQUATORIAL_GUINEA, "GQ");
    LANDED_CNTRY_MAP.put(SystemLocation.ANGOLA, "AO");
    LANDED_CNTRY_MAP.put(SystemLocation.SENEGAL, "SN");
    LANDED_CNTRY_MAP.put(SystemLocation.BOTSWANA, "BW");
    LANDED_CNTRY_MAP.put(SystemLocation.IVORY_COAST, "CI");
    LANDED_CNTRY_MAP.put(SystemLocation.BURUNDI, "BI");
    LANDED_CNTRY_MAP.put(SystemLocation.GABON, "GA");
    LANDED_CNTRY_MAP.put(SystemLocation.DEMOCRATIC_CONGO, "CD");
    LANDED_CNTRY_MAP.put(SystemLocation.CONGO_BRAZZAVILLE, "CG");
    LANDED_CNTRY_MAP.put(SystemLocation.CAPE_VERDE_ISLAND, "CV");
    LANDED_CNTRY_MAP.put(SystemLocation.DJIBOUTI, "DJ");
    LANDED_CNTRY_MAP.put(SystemLocation.GUINEA_CONAKRY, "GN");
    LANDED_CNTRY_MAP.put(SystemLocation.CAMEROON, "CM");
    LANDED_CNTRY_MAP.put(SystemLocation.ETHIOPIA, "ET");
    LANDED_CNTRY_MAP.put(SystemLocation.MADAGASCAR, "MG");
    LANDED_CNTRY_MAP.put(SystemLocation.MAURITANIA, "MR");
    LANDED_CNTRY_MAP.put(SystemLocation.TOGO, "TG");
    LANDED_CNTRY_MAP.put(SystemLocation.GHANA, "GH");
    LANDED_CNTRY_MAP.put(SystemLocation.ERITREA, "ER");
    LANDED_CNTRY_MAP.put(SystemLocation.GAMBIA, "GM");
    LANDED_CNTRY_MAP.put(SystemLocation.KENYA, "KE");
    LANDED_CNTRY_MAP.put(SystemLocation.MALAWI_CAF, "MW");
    LANDED_CNTRY_MAP.put(SystemLocation.LIBERIA, "LR");
    LANDED_CNTRY_MAP.put(SystemLocation.MOZAMBIQUE, "MZ");
    LANDED_CNTRY_MAP.put(SystemLocation.NIGERIA, "NG");
    LANDED_CNTRY_MAP.put(SystemLocation.CENTRAL_AFRICAN_REPUBLIC, "CF");
    LANDED_CNTRY_MAP.put(SystemLocation.ZIMBABWE, "ZW");
    LANDED_CNTRY_MAP.put(SystemLocation.SAO_TOME_ISLANDS, "ST");
    LANDED_CNTRY_MAP.put(SystemLocation.RWANDA, "RW");
    LANDED_CNTRY_MAP.put(SystemLocation.SIERRA_LEONE, "SL");
    LANDED_CNTRY_MAP.put(SystemLocation.SOMALIA, "SO");
    LANDED_CNTRY_MAP.put(SystemLocation.BENIN, "BJ");
    LANDED_CNTRY_MAP.put(SystemLocation.BURKINA_FASO, "BF");
    LANDED_CNTRY_MAP.put(SystemLocation.SOUTH_SUDAN, "SS");
    LANDED_CNTRY_MAP.put(SystemLocation.TANZANIA, "TZ");
    LANDED_CNTRY_MAP.put(SystemLocation.UGANDA, "UG");
    LANDED_CNTRY_MAP.put(SystemLocation.SOUTH_AFRICA, "ZA");
    LANDED_CNTRY_MAP.put(SystemLocation.MALTA, "MT");
    LANDED_CNTRY_MAP.put(SystemLocation.SEYCHELLES, "SC");
    LANDED_CNTRY_MAP.put(SystemLocation.GUINEA_BISSAU, "GW");
    LANDED_CNTRY_MAP.put(SystemLocation.NIGER, "NE");
    LANDED_CNTRY_MAP.put(SystemLocation.CHAD, "TD");
    LANDED_CNTRY_MAP.put(SystemLocation.ZAMBIA, "ZM");
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
      record.setCmrTaxOffice(this.currentImportValues.get("InstallingAddressT"));
      record.setCmrDept(null);

      if (StringUtils.isEmpty(record.getCmrCustPhone())) {
        record.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
      }
      converted.add(record);
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

  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {
    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing", zi01Map);
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

  public static void main(String[] args) {
    PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource("cmr-log4j.properties"));
    FindCMRRecordModel record = new FindCMRRecordModel();
    MCOHandler handler = new MCOHandler();
    handler.currentImportValues = new HashMap<String, String>();
    handler.currentImportValues.put("Address1", "TECH DATA FRANCE SAS");
    handler.currentImportValues.put("Address2", "DIVISION AFRIQUE");
    handler.currentImportValues.put("Address3", "PARC DU LEVANT");
    handler.currentImportValues.put("Address4", "AVENUE MARGUERITE PEREY");
    handler.currentImportValues.put("Address5", "77600 BUSSY SAINT GEORGES");
    handler.currentImportValues.put("Address6", "FRANCE");

    handler.handleSOFAddressImport(null, null, record, "");
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
      LOG.debug("Cross-border address..");
      // CROSS
      // line2 = ZA: Phone + Attention Person (Phone for Shipping & EPL only)
      // line2 = CEWA: Name Con't
      // line3 = Street
      // line4 = Street Con't + PO BOX
      // line5 = City / Postal Code or both
      // line6 = State (Country)

      address.setCmrName1Plain(line1);

      address.setCmrName2Plain(line2);

      address.setCmrStreetAddress(line3);

      handleStreetContAndPoBox(line4, cmrIssuingCntry, address, addressKey);
      handleCityAndPostCode(line5, cmrIssuingCntry, address, addressKey);
      address.setCmrCountryLanded(countryCd);

    } else {
      // Domestic
      // line2 = Name Con't
      // line3 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line4 = Street
      // line5 = Street Con't + PO BOX
      // line6 = City / Postal Code or both

      LOG.debug("Local address..");
      address.setCmrName1Plain(line1);

      address.setCmrName2Plain(line2);

      handlePhoneAndAttn(line3, cmrIssuingCntry, address, addressKey);

      address.setCmrStreetAddress(line4);

      handleStreetContAndPoBox(line5, cmrIssuingCntry, address, addressKey);

      handleCityAndPostCode(line6, cmrIssuingCntry, address, addressKey);
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

    // Story 1718889: Tanzania: new mandatory TIN number field
    if ((SystemLocation.TANZANIA.equals(cmrIssuingCntry)) && "ZP01".equalsIgnoreCase(address.getCmrAddrTypeCode())) {
      address.setCmrDept(this.currentImportValues.get("TIN"));
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
    LOG.trace("Tin: " + address.getCmrDept());
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());

  }

  protected void handlePhoneAndAttn(String line, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    if (line == null || StringUtils.isEmpty(line)) {
      return;
    }
    String[] parts = line.split("[, ]");

    StringBuilder sbPhone = new StringBuilder();
    StringBuilder sbAttn = new StringBuilder();
    boolean attnStart = false;
    for (String part : parts) {
      if (!attnStart && (StringUtils.isNumeric(part) || (part.length() > 1 && part.startsWith("+") && StringUtils.isNumeric(part.substring(1))))) {
        sbPhone.append(part);
      } else {
        attnStart = true;
        sbAttn.append(sbAttn.length() > 0 ? " " : "");
        sbAttn.append(part);
      }
    }

    address.setCmrCustPhone(sbPhone.toString());
    address.setCmrName4(sbAttn.toString());

  }

  protected void handleStreetContAndPoBox(String line, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    if (line == null || StringUtils.isEmpty(line)) {
      return;
    }

    String poBox = null;
    String streetCont = null;
    if (line.contains(",")) {
      String ending = line.substring(line.indexOf(",") + 1);
      if (ending.matches(".*\\d{1}.*")) {
        poBox = line.substring(line.indexOf(",") + 1).trim();
        streetCont = line.substring(0, line.indexOf(",")).trim();
      }
    } else {
      streetCont = line;
    }

    address.setCmrPOBox(poBox);
    address.setCmrStreetAddressCont(streetCont);

    if (StringUtils.isEmpty(address.getCmrStreetAddressCont())) {
      address.setCmrStreetAddressCont(line);
    }
  }

  protected void handleCityAndPostCode(String line, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    if (line == null || StringUtils.isEmpty(line)) {
      return;
    }

    String postalCode = null;
    String city = null;
    if (line.contains(",")) {
      String ending = line.substring(line.indexOf(",") + 1);
      if (ending.matches(".*\\d{1}.*")) {
        postalCode = line.substring(line.indexOf(",") + 1).trim();
        city = line.substring(0, line.indexOf(",")).trim();
      }
    } else {
      String[] parts = line.split("[ ]");
      if (parts.length > 0 && StringUtils.isNumeric(parts[0])) {
        postalCode = parts[0];
        city = line.substring(parts[0].length()).trim();
      } else {
        city = line;
      }
    }

    address.setCmrPostalCode(postalCode);
    address.setCmrCity(city);
  }

  protected void handleSOFAddressImportOLD(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    LOG.debug("Parsing addresses using flexible parsing");
    String line1 = this.currentImportValues.get(addressKey + "Address1");
    String line2 = this.currentImportValues.get(addressKey + "Address2");
    String line3 = this.currentImportValues.get(addressKey + "Address3");
    String line4 = this.currentImportValues.get(addressKey + "Address4");
    String line5 = this.currentImportValues.get(addressKey + "Address5");
    String line6 = this.currentImportValues.get(addressKey + "Address6");

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
      if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
        // address is still empty as of now, try to convert
        address.setCmrStreetAddress(line4);
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

    // Story 1718889: Tanzania: new mandatory TIN number field
    if ((SystemLocation.TANZANIA.equals(cmrIssuingCntry)) && "ZP01".equalsIgnoreCase(address.getCmrAddrTypeCode())) {
      address.setCmrDept(this.currentImportValues.get("TIN"));
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
    data.setLegacyCurrencyCd(this.currentImportValues.get("CurrencyCode"));
    LOG.trace("Currency: " + data.getLegacyCurrencyCd());

    // MCO Africa - For SBO values exceeding 4 char length
    String sBO = this.currentImportValues.get("SBO");
    if (!(StringUtils.isEmpty(sBO))) {
      if (sBO.length() > 4) {
        data.setSalesBusOffCd(sBO.substring(0, 4));
      }
    }

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

    data.setInstallBranchOff("");
    data.setSpecialTaxCd("");
    data.setCompany("");
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
    address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
    address.setTransportZone("");
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCustPrefLang("E");
    data.setCmrOwner("IBM");
    // data.setSensitiveFlag(CmrConstants.REGULAR);
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
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(newData.getModeOfPayment());
      update.setOldData(oldData.getModeOfPayment());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !service.equals(oldData.getTaxExempt3(), newData.getTaxExemptStatus3())
        && !SystemLocation.MALTA.equals(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "TaxExemptStatus3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxExemptStatus3(), "TaxExemptStatus3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxExempt3(), "TaxExemptStatus3", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {

    if (!"ZP01".equals(addr.getId().getAddrType())) {
      addr.setDept("");
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM4", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "POST_CD", "DEPT", "LAND_CNTRY", "PO_BOX",
        "CUST_PHONE"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

}
