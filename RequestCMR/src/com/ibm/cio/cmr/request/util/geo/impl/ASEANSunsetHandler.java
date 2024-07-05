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
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
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
 * <li>643 - Brunei</li>
 * <li>749 - Indonesia</li>
 * <li>714 - Laos</li>
 * <li>720 - Cambodia</li>
 * <li>646 - Myanmar</li>
 * <li>778 - Malaysia</li>
 * <li>818 - Philippines</li>
 * <li>834 - Singapore</li>
 * <li>856 - Thailand</li>
 * <li>852 - Vietnam</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public class ASEANSunsetHandler extends APHandler {

  private static final Logger LOG = Logger.getLogger(ASEANSunsetHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  private static final String[] ID_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] PH_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] SG_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] VN_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] TH_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] BN_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] MY_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] MM_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4" };

  private static Map<String, String> ASEAN_SEQ_1 = new HashMap<>();
  private static final String GRP1_SOLD_TO_FIXED_SEQ = "AA";

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.BRUNEI, "BN");
    LANDED_CNTRY_MAP.put(SystemLocation.INDONESIA, "ID");
    LANDED_CNTRY_MAP.put(SystemLocation.MALAYSIA, "MY");
    LANDED_CNTRY_MAP.put(SystemLocation.PHILIPPINES, "PH");
    LANDED_CNTRY_MAP.put(SystemLocation.SINGAPORE, "SG");
    LANDED_CNTRY_MAP.put(SystemLocation.VIETNAM, "VN");
    LANDED_CNTRY_MAP.put(SystemLocation.THAILAND, "TH");
    LANDED_CNTRY_MAP.put(SystemLocation.MYANMAR, "MM");
    LANDED_CNTRY_MAP.put(SystemLocation.LAOS, "LA");
    LANDED_CNTRY_MAP.put(SystemLocation.CAMBODIA, "KH");

    // NOTE: Key = CREQCMR addr type
    // ZS01 Mailing
    // ZP01 Billing
    // ZI01 Installing
    // ZH01 Shipping
    // ZP02 Software

    ASEAN_SEQ_1.put("ZS01", "AA");
    ASEAN_SEQ_1.put("ZP01", "BB");
    ASEAN_SEQ_1.put("ZI01", "CC");
    ASEAN_SEQ_1.put("ZH01", "DD");
    ASEAN_SEQ_1.put("ZP02", "EE");

  }

  public static void main(String[] args) {
    // parse testing

    WtaasAddress address = new WtaasAddress();
    address.getValues().put(Address.Line1, "CUSTOMER NAME");
    address.getValues().put(Address.Line2, "CUSTOMER NAME CON'T");
    address.getValues().put(Address.Line3, "STREET");
    address.getValues().put(Address.Line4, "STREET CON'T 1");
    address.getValues().put(Address.Line5, "STREET CON'T 2");
    address.getValues().put(Address.Line6, "MAKATI 1234");

    // ISA
    // address.getValues().put(Address.Line5, "STREET CON'T 2, CITY");
    // address.getValues().put(Address.Line6, "<SRI LANKA> 12345");

    FindCMRRecordModel record = new FindCMRRecordModel();
    ASEANSunsetHandler handler = new ASEANSunsetHandler();
    handler.handleWTAASAddressImport(null, SystemLocation.PHILIPPINES, null, record, address);

    record = new FindCMRRecordModel();
    record.setCmrName3("NAME3");
    // record.setCmrName4("NAME4");
    record.setCmrStreetAddress("STREET");
    handler.handleRDcRecordValues(record);
    System.out.println("Street: " + record.getCmrStreetAddress());
    System.out.println("Street Con't: " + record.getCmrStreetAddressCont());
    System.out.println("Dept: " + record.getCmrDept());

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
              record.setCmrAddrSeq(GRP1_SOLD_TO_FIXED_SEQ);
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
                  record.setTransAddrNo(wtaasAddress.getAddressNo());

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

        LOG.info("Is Prospect: " + prospectCmrChosen);
        if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType()) && prospectCmrChosen) {
          record.setCmrAddrSeq(GRP1_SOLD_TO_FIXED_SEQ);
          LOG.info("Sequence Number: " + record.getCmrAddrSeq());
        }
        handleRDcRecordValues(record);
        converted.add(record);
        doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
        Collections.sort(converted);
        source.setItems(converted);
      }
    }
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

        // it reached this logic so it means this is RDC only address not
        // present in wtaas
        if (CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType()) && SystemLocation.INDONESIA.equals(record.getCmrIssuedBy())) {
          LOG.info("Marking X for wtaas logic new create");
          record.setTransAddrNo("X");
        } else {
          record.setTransAddrNo(record.getCmrAddrSeq());
        }
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
      LOG.info("ASEAN getCmrAddrSeq: " + record.getCmrAddrSeq());
      LOG.info("ASEAN getCmrName4: " + record.getCmrName4());
      LOG.info("ASEAN getCmrStreetAddress: " + record.getCmrStreetAddress());
      LOG.info("ASEAN getCmrCity: " + record.getCmrCity());
      LOG.info("ASEAN getCmrName4: " + record.getCmrName4());
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

    // line 4 - always street con't1
    record.setCmrStreetAddressCont(line4.trim());

    List<String> lines = new ArrayList<String>();
    for (String line : Arrays.asList(line5, line6)) {
      if (!StringUtils.isEmpty(line)) {
        lines.add(line.trim());
      }
    }
    // max line count = 2
    int lineCount = lines.size();

    boolean parseCityState = true;
    String postalCode = null;
    String city = null;
    String state = null;

    // last line will always contain city/state/province + postal code or
    // country + postal code

    String[] countries = new String[] { "BRUNEI", "CAMBODIA", "LAOS", "MYANMAR", "SINGAPORE" };
    String line = "";

    if (lines.size() > 0) {
      line = lines.get(lines.size() - 1);
      for (String countryName : countries) {
        if (line.toUpperCase().replaceAll(" ", "").contains(countryName)) {
          parseCityState = false;
        }
      }
      if (!parseCityState) {
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

    line = null;
    if (lineCount == 2) {
      // line 5 = street con't 2
      line = lines.get(0);
      record.setCmrDept(line);
    }

    record.setCmrCountryLanded(LANDED_CNTRY_MAP.get(cmrIssuingCntry));
    record.setCmrCity(city);
    record.setCmrState(state);
    record.setCmrPostalCode(postalCode);

    if (StringUtils.isEmpty(record.getCmrStreetAddressCont()) && !StringUtils.isEmpty(record.getCmrDept())) {
      record.setCmrStreetAddressCont(record.getCmrDept());
      record.setCmrDept(null);
    }
    logExtracts(record);

  }

  @Override
  protected String getMappedAddressType(String country, String rdcType, String addressSeq) {
    switch (country) {
    case SystemLocation.INDONESIA:
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
    case SystemLocation.PHILIPPINES:
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
    case SystemLocation.SINGAPORE:
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
    case SystemLocation.VIETNAM:
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
    case SystemLocation.THAILAND:
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
    case SystemLocation.BRUNEI:
      if ("ZS01".equals(rdcType) && "AA".equals(addressSeq)) {
        return "ZS01"; // mailing
      }
      if ("ZP01".equals(rdcType) && "BB".equals(addressSeq)) {
        return "ZP01"; // billing
      }
      if ("ZI01".equals(rdcType) && "EE".equals(addressSeq)) {
        return "ZP02"; // software, not yet in LOV
      }
      return null;
    case SystemLocation.MALAYSIA:
      if ("ZS01".equals(rdcType) && "AA".equals(addressSeq)) {
        return "ZS01"; // mailing
      }
      if ("ZP01".equals(rdcType) && "BB".equals(addressSeq)) {
        return "ZP01"; // billing
      }
      if ("ZI01".equals(rdcType) && "EE".equals(addressSeq)) {
        return "ZP02"; // software, not yet in LOV
      }
      return null;
    case SystemLocation.MYANMAR:
      if ("ZS01".equals(rdcType) && "AA".equals(addressSeq)) {
        return "ZS01"; // mailing
      }
    }
    return null;
  }

  @Override
  public String getMappedAddressUse(String country, String createCmrAddrType) {
    switch (country) {
    case SystemLocation.INDONESIA:
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
    case SystemLocation.PHILIPPINES:
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
    case SystemLocation.SINGAPORE:
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
    case SystemLocation.VIETNAM:
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
    case SystemLocation.THAILAND:
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
    case SystemLocation.BRUNEI:
      switch (createCmrAddrType) {
      case "ZP01":
        // Billing
        return "2";
      case "ZS01":
        // Mailing
        return "1";
      case "ZI01":
        // Installing
        return "3";
      case "ZH01":
        // Shipping
        return "4";
      case "ZP02":
        // Software
        return "5";
      }
      return null;
    case SystemLocation.MALAYSIA:
      switch (createCmrAddrType) {
      case "ZP01":
        // Billing
        return "2";
      case "ZS01":
        // Mailing
        return "1";
      case "ZI01":
        // Installing
        return "3";
      case "ZH01":
        // Shipping
        return "4";
      case "ZP02":
        // Software
        return "5";
      }
      return null;
    case SystemLocation.MYANMAR:
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

    if (SystemLocation.INDONESIA.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(ID_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.PHILIPPINES.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(PH_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.SINGAPORE.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(SG_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.VIETNAM.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(VN_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.THAILAND.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(TH_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.BRUNEI.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(BN_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.MALAYSIA.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(MY_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.MYANMAR.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(MM_SUPPORTED_ADDRESS_USES);
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
        case SystemLocation.INDONESIA:
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
        case SystemLocation.PHILIPPINES:
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
        case SystemLocation.SINGAPORE:
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
        case SystemLocation.VIETNAM:
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
        case SystemLocation.THAILAND:
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
        case SystemLocation.BRUNEI:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "1":
            // Mailing
            return "ZS01";
          case "3":
            // Installing
            return "ZI01";
          case "4":
            // Shipping
            return "ZH01";
          case "5":
            // Software
            return "ZP02";
          }
          return null;
        case SystemLocation.MALAYSIA:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "1":
            // Mailing
            return "ZS01";
          case "3":
            // Installing
            return "ZI01";
          case "4":
            // Shipping
            return "ZH01";
          case "5":
            // Software
            return "ZP02";
          }
          return null;
        case SystemLocation.MYANMAR:
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
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##INACType", "inacType");
    map.put("##ISU", "isuCd");
    map.put("##Sector", "sectorCd");
    map.put("##Cluster", "apCustClusterId");
    map.put("##RestrictedInd", "restrictInd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##CustLang", "custPrefLang");
    map.put("##ProvinceCode", "territoryCd");
    map.put("##CmrNoPrefix", "cmrNoPrefix");
    map.put("##ProvinceName", "busnType");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
    map.put("##CAP", "capInd");
    map.put("##RequestReason", "reqReason");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##City1", "city1");
    map.put("##StateProv", "stateProv");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##VAT", "vat");
    map.put("##CollectionCd", "collectionCd");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##CollBoId", "collBoId");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##ISBU", "isbuCd");
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    map.put("##SAPNumber", "sapNo");
    map.put("##Department", "dept");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##CustomerName1", "custNm1");
    map.put("##MrcCd", "mrcCd");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName2", "custNm2");
    map.put("##PostalCode", "postCd");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##GovIndicator", "govType");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##RegionCode", "miscBillCd");
    map.put("##OrdBlk", "ordBlk");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = null;
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";

    if (!StringUtils.isEmpty(addrType)) {
      // For Primary Address
      newAddrSeq = getNewPrimaryAddressSeq(entityManager, reqId, addrType, cmrIssuingCntry);

      // maybe separate logic for additional addresses
    }
    return newAddrSeq;
  }

  private String getNewPrimaryAddressSeq(EntityManager entityManager, long reqId, String addrType, String cmrIssuingCntry) {
    String newAddrSeq = "";
    switch (cmrIssuingCntry) {
    // case SystemLocation.BANGLADESH: this is in ISAHandler
    case SystemLocation.INDONESIA:
    case SystemLocation.PHILIPPINES:
    case SystemLocation.SINGAPORE:
    case SystemLocation.VIETNAM:
    case SystemLocation.THAILAND:
      newAddrSeq = ASEAN_SEQ_1.get(addrType);
      break;
    default:
      newAddrSeq = "";
      break;
    }
    return newAddrSeq;
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

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {

    UpdatedDataModel update = null;

    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getOrdBlk(), newData.getOrdBlk())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "OrdBlk", "-"));
      update.setNewData(newData.getOrdBlk());
      update.setOldData(oldData.getOrdBlk());
      results.add(update);
    }
  }

}
