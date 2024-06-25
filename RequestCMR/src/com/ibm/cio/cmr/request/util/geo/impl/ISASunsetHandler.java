/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasAddress;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys.Address;
import com.ibm.cio.cmr.request.util.wtaas.WtaasRecord;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.WtaasClient;
import com.ibm.cmr.services.client.wtaas.WtaasQueryRequest;
import com.ibm.cmr.services.client.wtaas.WtaasQueryResponse;

/**
 * {@link GEOHandler} for:
 * <ul>
 * <li>744 - India</li>
 * <li>615 - Bangladesh</li>
 * <li>790 - Nepal</li>
 * <li>652 - Sri Lanka</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public class ISASunsetHandler extends APHandler {

  private static final Logger LOG = Logger.getLogger(ISASunsetHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  private static final String[] IN_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4" };
  private static final String[] BD_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] LK_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4" };

  private static final String MAILING_ADDR_TYPE = "ZS01";

  private static final String MAILING_FIXED_SEQ = "AA";
  private static Map<String, String> BD_ADDR_SEQ = new HashMap<>();

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.INDIA, "IN");
    LANDED_CNTRY_MAP.put(SystemLocation.BANGLADESH, "BD");
    LANDED_CNTRY_MAP.put(SystemLocation.SRI_LANKA, "LK");
    LANDED_CNTRY_MAP.put(SystemLocation.NEPAL, "NP");

    BD_ADDR_SEQ.put("ZS01", "AA");
    BD_ADDR_SEQ.put("ZP01", "BB");
    BD_ADDR_SEQ.put("ZI01", "CC");
    BD_ADDR_SEQ.put("ZH01", "DD");
    BD_ADDR_SEQ.put("ZP02", "EE");
  }

  public static void main(String[] args) {
    // parse testing
    WtaasAddress address = new WtaasAddress();
    address.getValues().put(Address.Line1, "IBM");
    // address.getValues().put(Address.Line2, "CUSTOMER NAME CON'T");
    address.getValues().put(Address.Line3, "LINE1");
    address.getValues().put(Address.Line4, "LINE2");
    // address.getValues().put(Address.Line5, "LINE2");
    address.getValues().put(Address.Line5, "IN 24100");
    // address.getValues().put(Address.Line6, "NEW DELHI 12345");

    // ISA
    // address.getValues().put(Address.Line5, "STREET CON'T 2, CITY");
    // address.getValues().put(Address.Line6, "<SRI LANKA> 12345");

    FindCMRRecordModel record = new FindCMRRecordModel();
    ISASunsetHandler handler = new ISASunsetHandler();
    handler.handleWTAASAddressImport(null, SystemLocation.INDIA, null, record, address);

  }

  @Override
  protected void handleWTAASAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel mainRecord,
      FindCMRRecordModel record, WtaasAddress address) {
    String line1 = address.get(Address.Line1);
    String line2 = address.get(Address.Line2);
    String line3 = address.get(Address.Line3);
    String line4 = address.get(Address.Line4);
    String line5 = address.get(Address.Line5);
    String line6 = address.get(Address.Line6);

    // line 1 - always customer name
    record.setCmrName1Plain(line1.trim());

    // line 2 - always customer name con't
    record.setCmrName2Plain(line2 != null ? line2.trim() : null);

    // line 3 - always street
    record.setCmrStreetAddress(line3.trim());

    List<String> lines = new ArrayList<String>();
    for (String line : Arrays.asList(line4, line5, line6)) {
      if (!StringUtils.isEmpty(line)) {
        lines.add(line.trim());
      }
    }
    // max line count = 3
    int lineCount = lines.size();

    boolean isIndia = false;
    String postalCode = null;
    String city = null;
    String state = null;
    String streetCont2 = null;

    // last line will always contain city + postal code or country + postal code
    String line = lines.get(lines.size() - 1);

    // parse the last line
    boolean crossBorder = line.startsWith("<");
    if (crossBorder) {
      String country = line.substring(1, line.lastIndexOf(">"));
      System.out.println(country);
      String code = getCountryCode(entityManager, country);
      if (!StringUtils.isEmpty(code)) {
        record.setCmrCountryLanded(code);
      }
    } else {
      if (line.toUpperCase().replaceAll(" ", "").contains("SRILANKA")) {
        record.setCmrCountryLanded(LANDED_CNTRY_MAP.get(SystemLocation.SRI_LANKA));
      } else if (line.toUpperCase().contains("BANGLADESH")) {
        record.setCmrCountryLanded(LANDED_CNTRY_MAP.get(SystemLocation.BANGLADESH));
      } else if (line.toUpperCase().contains("NEPAL")) {
        record.setCmrCountryLanded(LANDED_CNTRY_MAP.get(SystemLocation.NEPAL));
      } else {
        // this is india
        isIndia = true;
        record.setCmrCountryLanded(LANDED_CNTRY_MAP.get(SystemLocation.INDIA));

        if (!isIndia) {
          // postal code is right after country name
          if (line.contains(" ")) {
            postalCode = line.substring(line.indexOf(" ")).trim();
          }
        } else {
          if (line.contains(" ")) {
            city = line.substring(0, line.lastIndexOf(" ")).trim();
            postalCode = line.substring(line.lastIndexOf(" ")).trim();
          }
        }

      }
    }

    line = null;
    if (lineCount == 3) {

      // line 4 = street con't 1
      // line 5 = street con't 2 + city (non india) or state (india)
      // line 6 city + postal code
      line = lines.get(0);
      record.setCmrStreetAddressCont(line);

      line = lines.get(1);

      if (line != null) {
        if (line.contains(",")) {
          streetCont2 = line.substring(0, line.lastIndexOf(",")).trim();
          if (isIndia) {
            state = line.substring(line.lastIndexOf(",") + 1).trim();
          } else {
            city = line.substring(line.lastIndexOf(",") + 1).trim();
          }
        } else {
          if (isIndia) {
            state = line.trim();
          } else {
            city = line.trim();
          }
        }
        if (state != null && state.length() > 3) {
          streetCont2 = line;
          state = null;
          city = null;
        }
      }

    } else if (lineCount == 2) {
      // line 4 = street con't 1
      // last line = city + postal code
      line = lines.get(0);
      record.setCmrStreetAddressCont(line);

    }

    record.setCmrCity(city);
    record.setCmrState(state);
    record.setCmrPostalCode(postalCode);
    record.setCmrDept(streetCont2);

    if (StringUtils.isEmpty(record.getCmrStreetAddressCont()) && !StringUtils.isEmpty(record.getCmrDept())) {
      record.setCmrStreetAddressCont(record.getCmrDept());
      record.setCmrDept(null);
    }
    logExtracts(record);

  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);
  }

  @Override
  protected String getMappedAddressType(String country, String rdcType, String addressSeq) {
    switch (country) {
    case SystemLocation.SRI_LANKA:
      if ("ZS01".equals(rdcType) && "AA".equals(addressSeq)) {
        return "ZS01"; // mailing
      }
      return null;
    case SystemLocation.INDIA:
      if ("ZS01".equals(rdcType) && "AA".equals(addressSeq)) {
        return "ZS01"; // mailing
      }
      return null;
    case SystemLocation.BANGLADESH:
      if ("ZS01".equals(rdcType) && "AA".equals(addressSeq)) {
        return "ZS01"; // mailing
      }
      if ("ZP01".equals(rdcType) && "BB".equals(addressSeq)) {
        return "ZP01"; // billing
      }
      if ("ZI01".equals(rdcType) && "CC".equals(addressSeq)) {
        return "ZI01"; // install
      }
      if ("ZI01".equals(rdcType) && "DD".equals(addressSeq)) {
        return "ZH01"; // shipping
      }
      if ("ZI01".equals(rdcType) && "EE".equals(addressSeq)) {
        return "ZP02"; // software, not yet in LOV
      }
      return null;
    }
    return null;
  }

  @Override
  public String getMappedAddressUse(String country, String createCmrAddrType) {
    switch (country) {
    case SystemLocation.BANGLADESH:
      switch (createCmrAddrType) {
      case "ZP01":
        // Billing
        return "2";
      case "ZI01":
        // Installing
        return "3";
      case "ZS01":
        // Mailing
        return "1";
      case "ZH01":
        // Shipping
        return "4";
      case "ZP02":
        // Software
        return "5";
      }
      return null;
    case SystemLocation.SRI_LANKA:
      switch (createCmrAddrType) {
      case "ZP01":
        // Billing
        return "2";
      case "ZI01":
        // Installing
        return "3";
      case "ZS01":
        // Mailing
        return "1";
      case "ZH01":
        // Shipping
        return "4";
      }
      return null;
    case SystemLocation.INDIA:
      switch (createCmrAddrType) {
      case "ZP01":
        // Billing
        return "2";
      case "ZI01":
        // Installing
        return "3";
      case "ZS01":
        // Mailing
        return "1";
      case "ZH01":
        // Shipping
        return "4";
      }
      return null;
    }
    return null;
  }

  @Override
  public boolean shouldAddWTAASAddess(String country, WtaasAddress address) {

    boolean shouldAddWTAASAddr = false;

    List<String> toCheck = null;

    if (SystemLocation.INDIA.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(IN_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.SRI_LANKA.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(LK_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.BANGLADESH.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(BD_SUPPORTED_ADDRESS_USES);
    }

    if (toCheck != null) {
      for (String addrUse : toCheck) {
        if (address.getAddressUse().contains(addrUse)) {
          shouldAddWTAASAddr = true;
        }
      }
    }

    return shouldAddWTAASAddr;
  }

  @Override
  public String getAddrTypeForWTAASAddrUse(String country, String wtaasAddressUse) {
    String[] uses = wtaasAddressUse.split("");
    for (String use : uses) {
      if (!StringUtils.isBlank(use)) {
        switch (country) {
        case SystemLocation.BANGLADESH:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "3":
            // Installing
            return "ZI01";
          case "1":
            // Mailing
            return "ZS01";
          case "4":
            // Shipping
            return "ZH01";
          case "5":
            // Software
            return "ZP02";
          }
          return null;
        case SystemLocation.SRI_LANKA:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "3":
            // Installing
            return "ZI01";
          case "1":
            // Mailing
            return "ZS01";
          case "4":
            // Shipping
            return "ZH01";
          }
          return null;
        case SystemLocation.INDIA:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "3":
            // Installing
            return "ZI01";
          case "1":
            // Mailing
            return "ZS01";
          case "4":
            // Shipping
            return "ZH01";
          }
          return null;
        }
      }
    }
    return null;
  }

  @Override
  protected void handleRDcRecordValues(FindCMRRecordModel record) {
    String[] inputs = { record.getCmrName3(), record.getCmrName4(), record.getCmrStreetAddress() };
    Queue<String> streets = new LinkedList<>();
    for (String street : inputs) {
      if (!StringUtils.isBlank(street)) {
        streets.add(street);
      }
    }
    String current = streets.peek() != null ? streets.remove() : null;
    record.setCmrStreetAddress(current);

    current = streets.peek() != null ? streets.remove() : null;
    record.setCmrStreetAddressCont(current);

    current = streets.peek() != null ? streets.remove() : null;
    record.setCmrDept(current);

  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

  @Override
  public boolean has3AddressLines(String country) {
    if (SystemLocation.INDIA.equals(country)) {
      return true;
    }
    return false;
  }

  @Override
  public void setAddressLine3(String country, Addr addr, FindCMRRecordModel cmrModel, String line3) {
    if (SystemLocation.INDIA.equals(country)) {
      addr.setDept(line3);
      cmrModel.setCmrDept(line3);
    }
  }

  @Override
  public Boolean compareReshuffledAddress(String dnbAddress, String address, String country) {
    if (!country.equalsIgnoreCase(SystemLocation.INDIA)) {
      return false;
    }
    final List<String> dnb = new ArrayList<>(Arrays.asList(dnbAddress.toLowerCase().split("\\s+")));
    final List<String> addrCmr = new ArrayList<>(Arrays.asList(address.toLowerCase().split("\\s+")));

    Collections.sort(dnb);
    Collections.sort(addrCmr);
    StringBuilder sb1 = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
    for (Object obj : dnb) {
      sb1.append(obj.toString());
      sb1.append("\t");
    }
    String dnbAddr = sb1.toString();
    for (Object obj : addrCmr) {
      sb2.append(obj.toString());
      sb2.append("\t");
    }
    String cmrAddress = sb2.toString();

    if (StringUtils.isNotBlank(cmrAddress) && StringUtils.isNotBlank(dnbAddr)
        && StringUtils.getLevenshteinDistance(cmrAddress.toUpperCase(), dnbAddr.toUpperCase()) > 8) {
      return false;
    }
    return true;
  }

  @Override
  public String buildAddressForDnbMatching(String country, Addr addr) {
    if (SystemLocation.INDIA.equals(country)) {
      String address = addr.getAddrTxt() != null ? addr.getAddrTxt() : "";
      address += StringUtils.isNotBlank(addr.getAddrTxt2()) ? " " + addr.getAddrTxt2() : "";
      address += StringUtils.isNotBlank(addr.getDept()) ? " " + addr.getDept() : "";
      address = address.trim();
      return address;
    }
    return null;
  };

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";

    if (!StringUtils.isEmpty(addrType)) {
      if (SystemLocation.BANGLADESH.equals(cmrIssuingCntry)) {
        newAddrSeq = getNewPrimaryAddressSeqForBD(entityManager, reqId, addrType);
      } else {
        newAddrSeq = getNewAddressSeq(entityManager, reqId, addrType);
      }
    }
    return newAddrSeq;
  }

  private String getNewAddressSeq(EntityManager entityManager, long reqId, String addrType) {
    String newAddrSeq = "";
    switch (addrType) {
    case MAILING_ADDR_TYPE:
      newAddrSeq = MAILING_FIXED_SEQ;
      break;
    default:
      newAddrSeq = "";
      break;
    }
    return newAddrSeq;
  }

  private String getNewPrimaryAddressSeqForBD(EntityManager entityManager, long reqId, String addrType) {
    String newAddrSeq = "";
    newAddrSeq = BD_ADDR_SEQ.get(addrType);
    return newAddrSeq;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = null;
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    LOG.debug("Converting search results to WTAAS equivalent..");
    FindCMRRecordModel mainRecord = source.getItems() != null && !source.getItems().isEmpty() ? source.getItems().get(0) : null;
    if (mainRecord != null) {
      boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
      if (!prospectCmrChosen) {
        this.currentRecord = retrieveWTAASValues(mainRecord);
        if (this.currentRecord != null) {

          List<WtaasAddress> wtaasAddresses = this.currentRecord.uniqueAddresses();

          if (wtaasAddresses != null && !wtaasAddresses.isEmpty()) {

            String zs01AddressUse = getZS01AddressUse(mainRecord.getCmrIssuedBy());
            FindCMRRecordModel record = null;
            List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
            // import only ZS01 from RDC for Create request
            if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
              LOG.info("CREATE import");
              LOG.debug(" - Main address, importing from FindCMR main record.");
              // will import ZS01 from RDc directly
              record = mainRecord;
              record.setCmrAddrSeq(MAILING_FIXED_SEQ);
              handleRDcRecordValues(record);
              converted.add(record);
            } else {
              LOG.info("UPDATE import");
              LOG.info("wtaasAddresses size: " + wtaasAddresses.size());
              // only create the actual number of addresses in wtaas
              for (WtaasAddress wtaasAddress : wtaasAddresses) {
                LOG.debug("WTAAS Addr No: " + wtaasAddress.getAddressNo() + " Use: " + wtaasAddress.getAddressUse());
                if (!wtaasAddress.getAddressUse().contains(zs01AddressUse)) {
                  LOG.debug(" - Additional address, checking RDC equivalent..");
                  record = locateRdcRecord(source, wtaasAddress);

                  if (record != null) {
                    LOG.debug(" - RDC equivalent found with type = " + record.getCmrAddrTypeCode() + " seq = " + record.getCmrAddrSeq()
                        + ", using directly");
                    handleRDcRecordValues(record);
                  } else {
                    // create a copy of the main record, to preserve data fields
                    LOG.debug(" - not found. parsing WTAAS data..");
                    record = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(record, mainRecord);

                    // clear the address values
                    record.setCmrName1Plain(null);
                    record.setCmrName2Plain(null);
                    record.setCmrName3(null);
                    record.setCmrName4(null);
                    record.setCmrDept(null);
                    record.setCmrCity(null);
                    record.setCmrState(null);
                    record.setCmrCountryLanded(null);
                    record.setCmrStreetAddress(null);
                    record.setCmrStreetAddressCont(null);
                    record.setCmrSapNumber(null);

                    // handle here the different mappings
                    handleWTAASAddressImport(entityManager, record.getCmrIssuedBy(), mainRecord, record, wtaasAddress);
                    String supportedAddrType = getAddrTypeForWTAASAddrUse(record.getCmrIssuedBy(), wtaasAddress.getAddressUse());
                    if (supportedAddrType != null) {
                      record.setCmrAddrTypeCode(supportedAddrType);
                    }
                    LOG.info("address not found in RDC. setting sequence from wtaas: " + wtaasAddress.getAddressNo());
                    record.setCmrAddrSeq(wtaasAddress.getAddressNo());
                  }
                  LOG.info("Setting paired seq to: " + wtaasAddress.getAddressNo());
                  mainRecord.setTransAddrNo(wtaasAddress.getAddressNo());

                  if (shouldAddWTAASAddess(record.getCmrIssuedBy(), wtaasAddress)) {
                    converted.add(record);
                  }
                } else {
                  LOG.debug(" - Main address, importing from FindCMR main record.");
                  // will import ZS01 from RDc directly
                  LOG.info("Setting paired seq to: " + wtaasAddress.getAddressNo());
                  mainRecord.setTransAddrNo(wtaasAddress.getAddressNo());

                  handleRDcRecordValues(mainRecord);
                  converted.add(mainRecord);
                }
              }
            }

            doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
            Collections.sort(converted);
            source.setItems(converted);
          }
        }
      } else {
        FindCMRRecordModel record = null;
        List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
        LOG.debug(" - Main address, importing from FindCMR main record.");
        // will import ZS01 from RDc directly
        LOG.info("Main Record Addr Type: " + mainRecord.getCmrAddrType());

        record = mainRecord;
        handleRDcRecordValues(record);
        converted.add(record);
        doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
        Collections.sort(converted);
        source.setItems(converted);
      }
    }
  }

  private FindCMRRecordModel locateRdcRecord(FindCMRResultModel source, WtaasAddress address) {
    String addrType = null;
    String addrUse = null;
    for (FindCMRRecordModel record : source.getItems()) {
      addrType = getMappedAddressType(record.getCmrIssuedBy(), record.getCmrAddrTypeCode(), record.getCmrAddrSeq());
      if (!StringUtils.isEmpty(addrType)) {
        // it's mapped in createcmr, check equivalent address use if the wtaas
        // address has this use
        addrUse = getMappedAddressUse(record.getCmrIssuedBy(), addrType);
        if (!StringUtils.isEmpty(addrUse) && address.getAddressUse().contains(addrUse)) {
          record.setCmrAddrTypeCode(addrType);
          return record;
        }
      }
    }
    return null;
  }

  @Override
  public void doAfterConvert(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel,
      List<FindCMRRecordModel> converted) {
    // / add extra Rdc imports
    LOG.info("Calling doAfterConvert");

    LOG.info("Importing RDc records..");
    for (FindCMRRecordModel record : source.getItems()) {
      LOG.info("Before Mapped Addrtype: " + record.getCmrAddrTypeCode());
      LOG.info("TransAddressNo Orig: " + record.getTransAddrNo());

      if (StringUtils.isBlank(record.getTransAddrNo()) && StringUtils.isNotBlank(record.getCmrAddrSeq())) {
        record.setTransAddrNo(record.getCmrAddrSeq());
      }
      LOG.info("TransAddressNo After: " + record.getTransAddrNo());

      String addrType = getMappedAddressType(record.getCmrIssuedBy(), record.getCmrAddrTypeCode(), record.getCmrAddrSeq());

      LOG.info("After Mapped Addrtype: " + addrType);

      if (addrType != null && !"ZS01".equals(addrType) && CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType())) {
        handleRDcRecordValues(record);
        record.setCmrAddrTypeCode(addrType);
        converted.add(record);
        LOG.info("ADDED Addr Type: " + addrType);
      }
      LOG.info("ISA getCmrAddrSeq: " + record.getCmrAddrSeq());
      LOG.info("ISA getCmrName4: " + record.getCmrName4());
      LOG.info("ISA getCmrStreetAddress: " + record.getCmrStreetAddress());
      LOG.info("ISA getCmrCity: " + record.getCmrCity());
      LOG.info("ISA getCmrName4: " + record.getCmrName4());
    }
  }

  private WtaasRecord retrieveWTAASValues(FindCMRRecordModel mainRecord) throws Exception {
    String cmrIssuingCntry = mainRecord.getCmrIssuedBy();
    String cmrNo = mainRecord.getCmrNum();

    try {
      WtaasClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), WtaasClient.class);

      WtaasQueryRequest request = new WtaasQueryRequest();
      request.setCmrNo(cmrNo);
      request.setCountry(cmrIssuingCntry);

      WtaasQueryResponse response = client.executeAndWrap(WtaasClient.QUERY_ID, request, WtaasQueryResponse.class);
      if (response == null || !response.isSuccess()) {
        LOG.warn("Error or no response from WTAAS query.");
        throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, new Exception("Error or no response from WTAAS query."));
      }
      if ("F".equals(response.getData().get("Status"))) {
        LOG.warn("Customer " + cmrNo + " does not exist in WTAAS.");
        throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, new Exception("Customer " + cmrNo + " does not exist in WTAAS."));
      }

      WtaasRecord record = WtaasRecord.createFrom(response);
      // record = WtaasRecord.dummy();
      return record;

    } catch (Exception e) {
      LOG.warn("An error has occurred during retrieval of the values.", e);
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, e);
    }

  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    LOG.info("setAddressValuesOnImport");

    if (currentRecord != null) {
      LOG.info("paired seq no before: " + address.getPairedAddrSeq());
      address.setPairedAddrSeq(currentRecord.getTransAddrNo());
      LOG.info("paired seq no after: " + address.getPairedAddrSeq());

      super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);
    }
  }

  private String getZS01AddressUse(String country) {
    switch (country) {
    case SystemLocation.AUSTRALIA:
      return "B"; // contract
    case SystemLocation.NEW_ZEALAND:
      return "3"; // install
    case SystemLocation.BANGLADESH:
      return "1"; // mailing
    case SystemLocation.INDONESIA:
      return "1"; // mailing
    case SystemLocation.PHILIPPINES:
      return "1"; // mailing
    case SystemLocation.SINGAPORE:
      return "1"; // mailing
    case SystemLocation.VIETNAM:
      return "1"; // mailing
    case SystemLocation.THAILAND:
      return "1"; // mailing
    case SystemLocation.BRUNEI:
      return "1"; // mailing
    case SystemLocation.MALAYSIA:
      return "1"; // mailing
    case SystemLocation.MYANMAR:
      return "1"; // mailing
    case SystemLocation.SRI_LANKA:
      return "1"; // mailing
    case SystemLocation.INDIA:
      return "1"; // mailing
    case SystemLocation.MACAO:
      return "3"; // installing
    case SystemLocation.HONG_KONG:
      return "3"; // installing
    case SystemLocation.TAIWAN:
      return "B"; // legal address
    case SystemLocation.KOREA:
      return "B"; // ??

    default:
      return null;
    }
  }

}
