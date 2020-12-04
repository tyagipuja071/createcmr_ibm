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
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
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
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for Malta
 * 
 * @author Garima Naraang
 * 
 */
public class MaltaHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(MaltaHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  protected static final String[] MT_MASS_UPDATE_SHEET_NAMES = { "Data", "Sold-To", "Bill-To", "Install-At", "Ship-To" };

  private static final String[] MT_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CustLang", "GeoLocationCode", "Affiliate", "Company", "CAP", "CMROwner",
      "CustClassCode", "LocalTax2", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor", "Building", "County",
      "City2", "Department", "CustomerName4" };

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    if ("MT".equals(mainRecord.getCmrCountryLanded())) {
      if (!StringUtils.isBlank(mainRecord.getCmrOwner())) {
        if (!"IBM".equals(mainRecord.getCmrOwner())) {
          throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
        }
      }
    }

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;

      record.setCmrName2Plain(record.getCmrName2Plain());

      // Name3 in rdc = CustNm3 on SOF
      if (!StringUtils.isEmpty(record.getCmrName3())) {
        record.setCmrName3(record.getCmrName3());
      }

      // name4 in rdc is street con't
      if (!StringUtils.isEmpty(record.getCmrName4())) {
        record.setCmrStreetAddressCont(record.getCmrName4());
      }

      if (!StringUtils.isBlank(record.getCmrPOBox())) {
        record.setCmrPOBox(record.getCmrPOBox());
      }

      record.setCmrAddrSeq("00001");

      converted.add(record);

    } else {
      // Import all address from RDC Main
      String reqType = reqEntry.getReqType();
      String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), reqType);
      if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) {
        if (source.getItems() != null) {
          String addrType = null;
          String seqNo = null;
          FindCMRRecordModel addr = null;

          // map RDc - SOF - CreateCMR by sequence no
          for (FindCMRRecordModel record : source.getItems()) {
            seqNo = record.getCmrAddrSeq();
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
              addrType = record.getCmrAddrTypeCode();
              if (!StringUtils.isEmpty(addrType)) {
                addr = cloneAddress(record, addrType);
                LOG.trace("Adding address type " + addrType + " for sequence " + seqNo);
                addr.setCmrStreetAddressCont(record.getCmrName3());

                // Name3 in rdc = CustNm3 on SOF
                if (!StringUtils.isEmpty(record.getCmrName3())) {
                  addr.setCmrName3(record.getCmrName3());
                }

                // Name4 In rdc = Street Address Con't on SOF
                if (!StringUtils.isEmpty(record.getCmrName4())) {
                  addr.setCmrStreetAddressCont(record.getCmrName4());
                }

                // PO BOX
                if (!StringUtils.isBlank(record.getCmrPOBox())) {
                  record.setCmrPOBox(record.getCmrPOBox());
                }

                if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                  addr.setCmrAddrSeq("00001");
                }
                converted.add(addr);
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
      // line2 = Name 2
      // line3 = Name 3
      // line4 = Name 4
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

    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Name 3: " + address.getCmrName3());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Name 4: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("City: " + address.getCmrCity());
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

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

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
    address.setCustNm3(currentRecord.getCmrName3());
    address.setAddrTxt(currentRecord.getCmrStreetAddress());
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
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CustClass", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCustClass(), "CustClass", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCustClass(), "CustClass", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOrdBlk(), newData.getOrdBlk())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OrderBlock", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getOrdBlk(), "OrderBlock", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOrdBlk(), "OrderBlock", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustPrefLang(), newData.getCustPrefLang())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "PrefLang", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCustPrefLang(), "PrefLang", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCustPrefLang(), "PrefLang", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SBO", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesBusOffCd(), "SBO", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesBusOffCd(), "SBO", cmrCountry));
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

  private void setBlankFieldsAtCopy(Addr addr) {
    if (!StringUtils.isEmpty(addr.getCustPhone())) {
      if (!"ZD01".equals(addr.getId().getAddrType())) {
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
    setBlankFieldsAtCopy(addr);
    if (!"ZP01".equals(addr.getId().getAddrType())) {
      addr.setDept("");
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(MT_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
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
    return true;
  }

  public String getReqType(EntityManager entityManager, long reqId) {
    String reqType = "";
    String sql = ExternalizedQuery.getSql("ADMIN.GETREQTYPE.MT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      reqType = results.get(0);
    }
    return reqType;
  }

  public String getCMRNo(EntityManager entityManager, long reqId) {
    String cmrNo = "";
    String sql = ExternalizedQuery.getSql("DATA.GETCMRNO.MT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      cmrNo = results.get(0);
    }
    return cmrNo;
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";
    String cmrNo = "";
    String reqType = "";

    if (!StringUtils.isEmpty(addrType)) {
      int addrSeq = 00000;
      String minAddrSeq = "00000";
      String maxAddrSeq = "99999";
      String sql = null;
      reqType = getReqType(entityManager, reqId);
      if (reqType.equalsIgnoreCase("U")) {
        cmrNo = getCMRNo(entityManager, reqId);
        sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.MT_U");
      } else {
        sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ.MT_C");
      }
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);
      query.setParameter("ADDR_TYPE", addrType);

      List<Object[]> results = query.getResults();
      if (results != null && results.size() > 0) {
        Object[] result = results.get(0);
        maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "00001");

        if (!(Integer.valueOf(maxAddrSeq) >= 00001 && Integer.valueOf(maxAddrSeq) <= 99999)) {
          maxAddrSeq = "";
        }
        if (StringUtils.isEmpty(maxAddrSeq) || addrType.equalsIgnoreCase("ZS01")) {
          maxAddrSeq = minAddrSeq;
        }
        try {
          addrSeq = Integer.parseInt(maxAddrSeq);
        } catch (Exception e) {
          // if returned value is invalid
        }
        addrSeq++;
      }
      newAddrSeq = String.format("%05d", Integer.parseInt(Integer.toString(addrSeq)));
      // newAddrSeq = Integer.toString(addrSeq);
      if (!StringUtils.isEmpty(cmrNo)) {
        newAddrSeq = "000" + cmrNo + "L" + newAddrSeq;
      }
    }
    return newAddrSeq;
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.MALTA.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZS01", "ZP01", "ZI01", "ZD01");
    }
    return null;
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.MALTA.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZD01", "ZI01");
    }
    return null;
  }

  @Override
  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return null;
  }

  @Override
  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.MALTA.equals(cmrIssuingCntry)) {
      return Arrays.asList("4");
    }
    return null;
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    // super.validateMassUpdateTemplateDupFills(validations, book, maxRows,
    // country);
    XSSFCell currCell = null;
    for (String name : MT_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      if (sheet != null) {
        for (Row row : sheet) {
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {

            String cmrNo = ""; // 0

            // Address Sheet
            String seqNo = ""; // 1
            String custName1 = ""; // 2
            String name2 = ""; // 3
            String name3 = ""; // 4
            String name4 = ""; // 5
            String street = ""; // 6
            String city = "";// 7
            String postalCode = ""; // 8
            String poBox = ""; // 09
            String landCntry = ""; // 10
            String phone = "";// 11

            // Data Sheet
            String isic = ""; // 3
            String classificationCd = ""; // 10
            String inac = ""; // 7
            String ordBlk = ""; // 11

            if (row.getRowNum() == 2001) {
              continue;
            }

            TemplateValidation error = new TemplateValidation(name);
            String rowNumber = "Row" + row.getRowNum() + ": ";

            if (!"Data".equalsIgnoreCase(sheet.getSheetName())) {
              // iterate all the rows and check each column value
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(1);
              seqNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(2);
              custName1 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(3);
              name2 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(4);
              name3 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(5);
              name4 = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              street = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              city = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(8);
              postalCode = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(9);
              poBox = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              landCntry = validateColValFromCell(currCell);

              if ("Ship-To".equalsIgnoreCase(sheet.getSheetName())) {
                currCell = (XSSFCell) row.getCell(11);
                phone = validateColValFromCell(currCell);

                if (!StringUtils.isBlank(cmrNo) && StringUtils.isBlank(seqNo) && !"Data".equalsIgnoreCase(sheet.getSheetName())) {
                  LOG.trace("Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
                  error.addError(row.getRowNum(), rowNumber + "Address Sequence No.",
                      "Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
                  validations.add(error);
                }

                if (!StringUtils.isBlank(phone) && !phone.contains("@") && !StringUtils.isNumeric(phone)) {
                  LOG.trace("Phone Number should contain only digits.");
                  error.addError(row.getRowNum(), rowNumber + "Phone #", "Phone Number should contain only digits.");
                  validations.add(error);
                }
              } else {
                if (!StringUtils.isBlank(cmrNo) && StringUtils.isBlank(seqNo) && !"Data".equalsIgnoreCase(sheet.getSheetName())) {
                  LOG.trace("Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
                  error.addError(row.getRowNum(), rowNumber + "Address Sequence No.",
                      "Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
                  validations.add(error);
                }

                if (!StringUtils.isBlank(city) && !StringUtils.isBlank(postalCode)) {
                  int count = city.length() + postalCode.length();
                  if (count > 28) {
                    LOG.trace("Total computed length of City and Postal Code should not exceed 28 characters.");
                    error.addError(row.getRowNum(), rowNumber + "City",
                        "Total computed length of City and Postal Code should not exceed 28 characters.");
                    validations.add(error);
                  }
                }
              }
            } else if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(3);
              isic = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              inac = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              classificationCd = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(11);
              ordBlk = validateColValFromCell(currCell);

              if (!StringUtils.isBlank(isic) && !StringUtils.isBlank(classificationCd)
                  && ((!"9500".equals(isic) && "60".equals(classificationCd)) || ("9500".equals(isic) && !"60".equals(classificationCd)))) {
                LOG.trace(
                    "Note that ISIC value 9500 can be entered only for CMR with Classification code 60. Please fix and upload the template again.");
                error.addError(row.getRowNum(), rowNumber + "Classification Code",
                    "Note that ISIC value 9500 can be entered only for CMR with Classification code 60. Please fix and upload the template again.");
                validations.add(error);
              }

              if (!StringUtils.isBlank(inac) && inac.length() == 4 && !StringUtils.isNumeric(inac) && !"@@@@".equals(inac)
                  && !inac.matches("^[a-zA-Z][a-zA-Z][0-9][0-9]$")) {
                LOG.trace("INAC should have all 4 digits or 2 letters and 2 digits in order.");
                error.addError(row.getRowNum(), rowNumber + "INAC/NAC", "INAC should have all 4 digits or 2 letters and 2 digits in order.");
              }

              if (!StringUtils.isBlank(ordBlk) && !("88".equals(ordBlk) || "94".equals(ordBlk) || "@".equals(ordBlk))) {
                LOG.trace("Note that value of Order block can only be 88 or 94 or @ or blank. Please fix and upload the template again.");
                error.addError(row.getRowNum(), rowNumber + "Order block",
                    "Note that value of Order block can only be 88 or 94 or @ or blank. Please fix and upload the template again.");
                validations.add(error);
              }
            }

            /*
             * if (!StringUtils.isBlank(inac) && inac.length() == 4 &&
             * !StringUtils.isNumeric(inac) && !"@@@@".equals(inac) &&
             * !inac.matches("^[a-zA-Z][a-zA-Z][0-9][0-9]$")) { LOG.
             * trace("INAC should have all 4 digits or 2 letters and 2 digits in order."
             * ); error.addError(row.getRowNum(), rowNumber + "INAC/NAC",
             * "INAC should have all 4 digits or 2 letters and 2 digits in order."
             * ); validations.add(error); }
             */

            if (StringUtils.isEmpty(cmrNo)) {
              LOG.trace("Note that CMR No. is mandatory. Please fix and upload the template again.");
              error.addError(row.getRowNum(), rowNumber + "CMR No.", "Note that CMR No. is mandatory. Please fix and upload the template again.");
              validations.add(error);
            }

          }
        }
      }
    }
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = "";
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  @Override
  public List<String> getDataFieldsForUpdate(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    // CMR-3171 - add ORB_BLK here
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "ORD_BLK"));
    return fields;
  }

}
