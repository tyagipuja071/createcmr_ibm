/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
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
 * <li>738 - Hong Kong</li>
 * <li>736 - Macao</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public class GCGHandler extends APHandler {

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  private static final String[] HK_SUPPORTED_ADDRESS_USES = { "1", "2", "3" };
  private static final String[] MO_SUPPORTED_ADDRESS_USES = { "1", "2", "3" };
  private static final Logger LOG = Logger.getLogger(GCGHandler.class);

  private static final String SOLD_TO_ADDR_TYPE = "ZS01";
  private static final String BILL_TO_ADDR_TYPE = "ZP01";
  private static final String SHIP_TO_ADDR_TYPE = "ZD01";
  private static final String INSTALL_AT_ADDR_TYPE = "MAIL"; // ZI01 in RDC
                                                             // won't change
                                                             // this due to
                                                             // existing WTAAS
                                                             // logic

  private static final String SOLD_TO_FIXED_SEQ = "A";
  private static final String INSTALL_AT_FIXED_SEQ = "E";
  private static final List<String> BILL_TO_FIXED_SEQ = Arrays.asList("B", "C", "D");
  private static final String HK_FULLNAME = "Hong Kong";
  private static final String MO_FULLNAME = "Macao";
  private static final String MO_FULLNAME2 = "Macau";

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.HONG_KONG, "HK");
    LANDED_CNTRY_MAP.put(SystemLocation.MACAO, "MO");
  }

  public static void main(String[] args) {
    // parse testing

    WtaasAddress address = new WtaasAddress();
    address.getValues().put(Address.Line1, "CUSTOMER NAME");
    address.getValues().put(Address.Line2, "CUSTOMER NAME CON'T");
    address.getValues().put(Address.Line3, "STREET");
    address.getValues().put(Address.Line4, "STREET CON'T 1");
    address.getValues().put(Address.Line5, "CITY");
    address.getValues().put(Address.Line6, "MACAO");

    // ISA
    // address.getValues().put(Address.Line5, "STREET CON'T 2, CITY");
    // address.getValues().put(Address.Line6, "<SRI LANKA> 12345");

    FindCMRRecordModel record = new FindCMRRecordModel();
    GCGHandler handler = new GCGHandler();
    handler.handleWTAASAddressImport(null, SystemLocation.HONG_KONG, null, record, address);

  }

  // START -- Override parent methods

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
          LOG.info("GCG WTAAS address count: " + wtaasAddresses.size());
          if (wtaasAddresses != null && !wtaasAddresses.isEmpty()) {

            String zs01AddressUse = getZS01AddressUse(mainRecord.getCmrIssuedBy());
            FindCMRRecordModel record = null;
            List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
            // import only ZS01 from RDC for Create request
            if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
              LOG.debug(" - Main address, importing from FindCMR main record.");
              // will import ZS01 from RDc directly
              record = mainRecord;
              if ("ZS01".equals(record.getCmrAddrTypeCode())) {
                handleRDcRecordValues(record);
                record.setCmrAddrSeq(SOLD_TO_FIXED_SEQ);
                converted.add(record);
              }

            } else {
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
                    if (supportedAddrType != null)
                      record.setCmrAddrTypeCode(supportedAddrType);
                  }
                  record.setCmrAddrSeq(wtaasAddress.getAddressNo());
                  LOG.info("setting record.setCmrAddrSeq : " + wtaasAddress.getAddressNo());

                  record.setTransAddrNo(wtaasAddress.getAddressNo());
                  LOG.info("setting record.setTransAddrNo : " + wtaasAddress.getAddressNo());

                  if (shouldAddWTAASAddess(record.getCmrIssuedBy(), wtaasAddress)) {
                    converted.add(record);
                  }
                } else {
                  LOG.debug(" - Main address, importing from FindCMR main record.");
                  if ("1234567ABCDEFGH".equals(wtaasAddress.getAddressUse())) {
                    LOG.info("Setting paired seq to: " + wtaasAddress.getAddressNo());
                    mainRecord.setTransAddrNo(wtaasAddress.getAddressNo());
                  }

                  // will import ZS01 from RDc directly
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
        record = mainRecord;
        handleRDcRecordValues(record);
        converted.add(record);
        doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
        Collections.sort(converted);
        source.setItems(converted);
      }

    }
  }

  /**
   * Connects to the WTAAS query service and gets the current values of the
   * record
   * 
   * @param mainRecord
   * @throws Exception
   */
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

  /**
   * static mapping of the ZS01 RDc address with the corresponding WTAAS address
   * use
   * 
   * @param country
   * @return
   */
  private String getZS01AddressUse(String country) {
    switch (country) {

    case SystemLocation.MACAO:
      return "3"; // installing
    case SystemLocation.HONG_KONG:
      return "3"; // installing

    default:
      return null;
    }

  }

  /**
   * Checks the equivalent RDc record from FindCMR of the {@link WtaasAddress}
   * record. The method uses a combination of address type mapping and mapped
   * address uses to locate the equivalent
   * 
   * @param source
   * @param address
   * @return
   */
  private FindCMRRecordModel locateRdcRecord(FindCMRResultModel source, WtaasAddress address) {
    String addrType = null;
    String addrUse = null;
    for (FindCMRRecordModel record : source.getItems()) {
      LOG.info("locateRdcRecord - record.getCmrAddrSeq(): " + record.getCmrAddrSeq());
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

  // END -- Override parent methods

  @Override
  protected void handleWTAASAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel mainRecord,
      FindCMRRecordModel record, WtaasAddress address) {
    LOG.info("Calling handleWTAASAddressImport");
    String line1 = address.get(Address.Line1);
    String line2 = address.get(Address.Line2);
    String line3 = address.get(Address.Line3);
    String line4 = address.get(Address.Line4);
    String line5 = address.get(Address.Line5);
    String line6 = address.get(Address.Line6);

    // always name1
    record.setCmrName1Plain(line1.trim());

    // always name2
    record.setCmrName2Plain(line2 != null ? line2.trim() : null);

    // always street
    record.setCmrStreetAddress(line3 != null ? line3.trim() : null);

    List<String> lines = new ArrayList<String>();
    for (String line : Arrays.asList(line4, line5, line6)) {
      if (!StringUtils.isEmpty(line)) {
        lines.add(line.trim());
      }
    }

    String city = null;
    String streetCont = null;
    // start backwards
    Collections.reverse(lines);
    for (String line : lines) {
      if (line.toUpperCase().replaceAll(" ", "").contains("HONGKONG") || line.toUpperCase().replaceAll(" ", "").contains("MACAU")
          || line.toUpperCase().replaceAll(" ", "").contains("MACAO")) {
        // ignore country
        continue;
      }
      if (city == null) {
        city = line;
      } else {
        streetCont = line;
      }
    }

    record.setCmrStreetAddressCont(streetCont);
    record.setCmrCity(city);
    record.setCmrCountryLanded(LANDED_CNTRY_MAP.get(cmrIssuingCntry));
    logExtracts(record);

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

      boolean isShipToInUpdate = CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType()) && "ZD01".equals(addrType);
      LOG.info("Is Ship to in Update: " + isShipToInUpdate);

      if ("ZP01".equals(addrType) || "MAIL".equals(addrType) || isShipToInUpdate) {
        handleRDcRecordValues(record);
        record.setCmrAddrTypeCode(addrType);
        converted.add(record);
        LOG.info("ADDED Addr Type: " + addrType);
      }
      LOG.info("GCG getCmrAddrSeq: " + record.getCmrAddrSeq());
      LOG.info("GCG getCmrName4: " + record.getCmrName4());
      LOG.info("GCG getCmrStreetAddress: " + record.getCmrStreetAddress());
      LOG.info("GCG getCmrCity: " + record.getCmrCity());
      LOG.info("GCG getCmrName4: " + record.getCmrName4());

    }
  }

  @Override
  protected String getMappedAddressType(String country, String rdcType, String addressSeq) {
    if ("ZS01".equals(rdcType) && "A".equals(addressSeq)) {
      return "ZS01"; // address A, sold-to
    }
    if ("ZP01".equals(rdcType) && Arrays.asList("B", "C", "D").contains(addressSeq)) {
      return "ZP01"; // address B, bill-to
    }
    if ("ZI01".equals(rdcType) && "E".equals(addressSeq)) {
      return "MAIL"; // address E, install-at
    }

    if (SHIP_TO_ADDR_TYPE.equals(rdcType)) {
      return SHIP_TO_ADDR_TYPE; // Ship To
    }

    return null;
  }

  @Override
  public String getMappedAddressUse(String country, String createCmrAddrType) {
    switch (country) {
    case SystemLocation.HONG_KONG:
      switch (createCmrAddrType) {
      case "ZS01":
        // Installing
        return "3";
      case "ZP01":
        // Billing
        return "2";
      case "MAIL":
        // Mailing
        return "1";
      }
      return null;
    case SystemLocation.MACAO:
      switch (createCmrAddrType) {
      case "ZS01":
        // Installing
        return "3";
      case "ZP01":
        // Billing
        return "2";
      case "MAIL":
        // Mailing
        return "1";
      }
      return null;
    }
    return null;
  }

  @Override
  public boolean shouldAddWTAASAddess(String country, WtaasAddress address) {

    boolean shouldAddWTAASAddr = false;

    List<String> toCheck = null;
    if (SystemLocation.HONG_KONG.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(HK_SUPPORTED_ADDRESS_USES);
    }
    if (SystemLocation.MACAO.equalsIgnoreCase(country)) {
      toCheck = Arrays.asList(MO_SUPPORTED_ADDRESS_USES);
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
        case SystemLocation.HONG_KONG:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "3":
            // Installing
            return "ZS01";
          case "1":
            // Mailing
            return "MAIL";
          }
          return null;
        case SystemLocation.MACAO:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "3":
            // Installing
            return "ZS01";
          case "1":
            // Mailing
            return "MAIL";
          }
          return null;
        }
      }
    }
    return null;
  }

  @Override
  protected void handleRDcRecordValues(FindCMRRecordModel record) {
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";

    if (!StringUtils.isEmpty(addrType)) {
      newAddrSeq = getNewAddressSeq(entityManager, reqId, addrType);
    }
    return newAddrSeq;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = null;
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  private String getNewAddressSeq(EntityManager entityManager, long reqId, String addrType) {
    String newAddrSeq = "";
    switch (addrType) {
    case SOLD_TO_ADDR_TYPE:
      newAddrSeq = SOLD_TO_FIXED_SEQ;
      break;
    case BILL_TO_ADDR_TYPE:
      newAddrSeq = getNewBillToAddrSeq(entityManager, reqId, addrType);
      break;
    case INSTALL_AT_ADDR_TYPE:
    case SHIP_TO_ADDR_TYPE:
      newAddrSeq = getNewInstallOrShipToSeq(entityManager, reqId, addrType);
      break;
    default:
      newAddrSeq = "";
      break;
    }
    return newAddrSeq;
  }

  private String getNewInstallOrShipToSeq(EntityManager entityManager, long reqId, String addrType) {
    String newAddrSeq = "";
    Set<String> existingAddrs = getAddrSeqByType(entityManager, reqId, addrType);
    if (existingAddrs.isEmpty()) {
      if (INSTALL_AT_ADDR_TYPE.equals(addrType)) {
        newAddrSeq = INSTALL_AT_FIXED_SEQ;
      }
    } else {
      return String.valueOf(existingAddrs.size() + 1);
    }

    return newAddrSeq;
  }

  private String getNewBillToAddrSeq(EntityManager entityManager, long reqId, String addrType) {
    Set<String> existingBillTo = getAddrSeqByType(entityManager, reqId, addrType);

    if (existingBillTo.isEmpty()) {
      return BILL_TO_FIXED_SEQ.get(0);
    }

    if (existingBillTo.size() < BILL_TO_FIXED_SEQ.size()) {
      for (String b : BILL_TO_FIXED_SEQ) {
        if (!existingBillTo.contains(b)) {
          return b;
        }
      }
    } else {
      return String.valueOf(existingBillTo.size() + 1);

    }
    return "";
  }

  private Set<String> getAddrSeqByType(EntityManager entityManager, long reqId, String addrType) {
    String sql = ExternalizedQuery.getSql("GCG.GET.ADDRSEQ.BY_ADDRTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    List<String> results = query.getResults(String.class);

    Set<String> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(results);

    return addrSeqSet;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = super.getUIFieldIdMap();
    map.put("##CustClass", "custClass");
    return map;
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    LOG.info("setAddressValuesOnImport");
    if (currentRecord != null) {
      LOG.info("paired seq no before: " + address.getPairedAddrSeq());
      address.setPairedAddrSeq(currentRecord.getTransAddrNo());
      LOG.info("paired seq no after: " + address.getPairedAddrSeq());

      // add condition for ABCDE sequence only or for ZD01
      List<String> rdcSeqValues = Arrays.asList("A", "B", "C", "D", "E");
      if (rdcSeqValues.contains(currentRecord.getCmrAddrSeq()) || "ZD01".equals(currentRecord.getCmrAddrTypeCode())) {
        address.setCustNm1(currentRecord.getCmrName1Plain());
        address.setCustNm2(currentRecord.getCmrName2Plain());
        if (rdcSeqValues.contains(currentRecord.getCmrAddrSeq())) {
          address.getId().setAddrType(getCorrectAddressTypeBySequence(currentRecord.getCmrAddrSeq()));
        }

        switch (countAddressLines(currentRecord)) {
        case 1:
          LOG.info("Addr Type: " + currentRecord.getCmrAddrTypeCode() + ", Addr Seq: " + currentRecord.getCmrAddrSeq() + ", Line 1");
          address.setAddrTxt(currentRecord.getCmrStreetAddress());
          address.setAddrTxt2("");
          address.setCity1("");
          break;
        case 2:
          LOG.info("Addr Type: " + currentRecord.getCmrAddrTypeCode() + ", Addr Seq: " + currentRecord.getCmrAddrSeq() + ", Line 2");
          address.setAddrTxt(currentRecord.getCmrStreetAddress());
          address.setAddrTxt2(currentRecord.getCmrCity());
          address.setCity1("");
          break;
        case 3:
          LOG.info("Addr Type: " + currentRecord.getCmrAddrTypeCode() + ", Addr Seq: " + currentRecord.getCmrAddrSeq() + ", Line 3");
          address.setAddrTxt(currentRecord.getCmrName4());
          address.setAddrTxt2(currentRecord.getCmrStreetAddress());
          address.setCity1(currentRecord.getCmrCity());
          break;
        default:
          LOG.info("Address lines are empty on import: Type = " + currentRecord.getCmrAddrTypeCode() + ", Seq = " + currentRecord.getCmrAddrSeq());
          break;
        }
      } else {
        LOG.debug(
            "Calling super for Non-RDC address mapping: Type = " + currentRecord.getCmrAddrTypeCode() + ", Seq = " + currentRecord.getCmrAddrSeq());
        super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);
      }

    }
  }

  private int countAddressLines(FindCMRRecordModel currentRecord) {

    int addrLinesCount = 0;

    if (currentRecord != null) {
      if (StringUtils.isNotBlank(currentRecord.getCmrStreetAddress())) {
        addrLinesCount++;
      }

      if (StringUtils.isNotBlank(currentRecord.getCmrCity()) && !isHKMOFullname(currentRecord.getCmrCity())) {
        addrLinesCount++;
      }

      if (StringUtils.isNotBlank(currentRecord.getCmrName4())) {
        addrLinesCount++;
      }
    }
    return addrLinesCount;

  }

  private static boolean isHKMOFullname(String val) {
    return HK_FULLNAME.equalsIgnoreCase(val) || MO_FULLNAME.equalsIgnoreCase(val) || MO_FULLNAME2.equalsIgnoreCase(val);
  }

  private String getCorrectAddressTypeBySequence(String seq) {
    LOG.info("getCorrectAddressTypeBySequence(");
    String correctAddrType = "";
    switch (seq) {
    case "A":
      correctAddrType = SOLD_TO_ADDR_TYPE;
      break;
    case "E":
      correctAddrType = INSTALL_AT_ADDR_TYPE;
      break;
    case "B":
    case "C":
    case "D":
      correctAddrType = BILL_TO_ADDR_TYPE;
      break;
    default:
      correctAddrType = "";
      break;
    }

    LOG.info("Seq " + seq + ", Addr Type: " + correctAddrType);

    return correctAddrType;
  }

}
