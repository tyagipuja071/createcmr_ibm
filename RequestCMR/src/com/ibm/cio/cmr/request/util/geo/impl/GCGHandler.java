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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
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
  private static final List<String> INSTALL_AT_FIXED_SEQ = Arrays.asList("E");
  private static final List<String> BILL_TO_FIXED_SEQ = Arrays.asList("B", "C", "D");
  private static final List<String> SHIP_TO_FIXED_SEQ = Arrays.asList("040");
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
    LOG.debug("Converting search results..");
    List<FindCMRRecordModel> converted = new ArrayList<>();
    List<FindCMRRecordModel> records = source.getItems();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      LOG.debug("Converting search results creates..");
      for (FindCMRRecordModel record : records) {
        LOG.debug(" - Main address, importing from FindCMR main record.");
        if ("ZS01".equals(record.getCmrAddrTypeCode())) {
          record.setCmrAddrSeq(SOLD_TO_FIXED_SEQ);
          converted.add(record);
        }
        converted.add(record);
      }
    } else {
      LOG.debug("Converting search results update..");
      for (FindCMRRecordModel record : records) {
        if ("ZS01".equals(record.getCmrAddrTypeCode()) && "90".equals(record.getCmrOrderBlock())) {
          continue;
        }
        converted.add(record);
      }
    }
    doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
    Collections.sort(converted);
    source.setItems(converted);
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

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(reqId);
    Admin admin = entityManager.find(Admin.class, adminPK);

    boolean isUpdate = CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType());

    switch (addrType) {
    case SOLD_TO_ADDR_TYPE:
      newAddrSeq = SOLD_TO_FIXED_SEQ;
      break;
    case BILL_TO_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, BILL_TO_FIXED_SEQ, isUpdate);
      break;
    case INSTALL_AT_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, INSTALL_AT_FIXED_SEQ, isUpdate);
      break;
    case SHIP_TO_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, SHIP_TO_FIXED_SEQ, isUpdate);
      break;
    default:
      newAddrSeq = "";
      break;
    }
    return newAddrSeq;
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

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    if (!prospectCmrChosen) {
      // data.setApCustClusterId(this.currentRecord.get(WtaasQueryKeys.Data.ClusterNo));
      // data.setRepTeamMemberNo(this.currentRecord.get(WtaasQueryKeys.Data.SalesmanNo));
      // data.setCollectionCd(this.currentRecord.get(WtaasQueryKeys.Data.IBMCode));
      // data.setIsbuCd(this.currentRecord.get(WtaasQueryKeys.Data.SellDept));
      // data.setMrcCd(this.currentRecord.get(WtaasQueryKeys.Data.MrktRespCode));
      // data.setIsbuCd(this.currentRecord.get(WtaasQueryKeys.Data.SellDept));
      // data.setBusnType(this.currentRecord.get(WtaasQueryKeys.Data.SellBrnchOff));
      // data.setGovType(this.currentRecord.get(WtaasQueryKeys.Data.GovCustInd));
      // data.setMiscBillCd(this.currentRecord.get(WtaasQueryKeys.Data.RegionCode));
      data.setClientTier(mainRecord.getCmrTier());
      data.setTerritoryCd(mainRecord.getCmrIssuedBy());
    } else {
      data.setCmrNo("");
    }

    autoSetAbbrevLocnNMOnImport(admin, data, results, mainRecord);
    data.setIsuCd(mainRecord.getCmrIsu());
    data.setCollectionCd(mainRecord.getCmrAccRecvBo());

    if (admin.getReqType().equals("U")) {
      data.setAbbrevLocn(mainRecord.getCmrDataLine());
    }

    if (mainRecord.getCmrCountryLandedDesc().isEmpty() && !prospectCmrChosen) {
      data.setAbbrevLocn(mainRecord.getCmrDataLine());
    }

    // fix Defect 1732232: Abbreviated Location issue
    if (!StringUtils.isBlank(data.getAbbrevLocn()) && data.getAbbrevLocn().length() > 9) {
      data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 9));
    }

  }

  private void autoSetAbbrevLocnNMOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) {
    if (SystemLocation.HONG_KONG.equals(data.getCmrIssuingCntry())) {
      if (admin.getReqType().equals("C")) {
        data.setAbbrevLocn("00 HK");
      }
      if ("AQSTN".equalsIgnoreCase(data.getCustSubGrp()) || "XAQST".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Acquisition use only");
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA use only");
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Consumer only");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Market Place Order");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer use only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
    }

    if (SystemLocation.MACAO.equals(data.getCmrIssuingCntry())) {
      if (admin.getReqType().equals("C")) {
        data.setAbbrevLocn("00 HK");
      }
      if ("AQSTN".equalsIgnoreCase(data.getCustSubGrp()) || "XAQST".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Acquisition use only");
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA use only");
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Consumer only");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Market Place Order");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer use only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
    }
  }

  private void setAbbrevNM(Data data, String abbrevNM) {

    if (!StringUtils.isBlank(abbrevNM))
      if (abbrevNM.length() > 21)
        data.setAbbrevNm(abbrevNM.substring(0, 21));
      else
        data.setAbbrevNm(abbrevNM);
  }

  @Override
  public List<String> getDataFieldsForUpdate(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CUST_PREF_LANG", "ISIC_CD", "SUB_INDUSTRY_CD", "INAC_CD", "ABBREV_NM", "CUST_CLASS", "ISU_CD",
        "CLIENT_TIER", "MRC_CD", "BP_REL_TYPE", "BP_NAME", "COLLECTION_CD", "MISC_BILL_CD", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC",
        "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD", "DUNS_NO"));
    return fields;
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields
        .addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "LAND_CNTRY", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "STATE_PROV", "CITY2", "POST_CD", "CONTACT"));
    return fields;
  }
  
  private String getNewSeqFromSeqToCheck(EntityManager entityManager, long reqId, String addrType, List<String> seqToCheck, boolean isUpdate) {
    Set<String> existingSeq = getExistingAddrSeqInclRdc(entityManager, reqId);
    boolean isAddrTypeExist = false;

    if (isUpdate) {
      List<Addr> allAddrByTypeFromAddr = getAddressByType(entityManager, addrType, reqId);
      int addrCount = allAddrByTypeFromAddr.size();
      if (addrCount > 0) {
        isAddrTypeExist = true;
      }
    }

    if (existingSeq.isEmpty()) {
      return seqToCheck.get(0);
    }

    if (!areAllElementsPresent(seqToCheck, existingSeq)) {
      for (String b : seqToCheck) {
        if (!existingSeq.contains(b)) {
          return b;
        }
      }
    } else {
      return String.valueOf(getNewSeqAdditionalAddr(existingSeq, addrType, isAddrTypeExist, isUpdate));
    }

    return "";
  }

  private boolean areAllElementsPresent(List<String> seqToCheck, Set<String> existingSeq) {
    return existingSeq.containsAll(seqToCheck);
  }

  private String getNewSeqAdditionalAddr(Set<String> existingAddrSeqSet, String addrType, boolean isAddrTypeExist, boolean isUpdate) {
    int candidateSeqNum = 0;
    switch (addrType) {
    case BILL_TO_ADDR_TYPE:
      candidateSeqNum = 21;
      break;
    case INSTALL_AT_ADDR_TYPE:
      candidateSeqNum = 51;
      break;
    case SHIP_TO_ADDR_TYPE:
      candidateSeqNum = 40;
      break;
    default:
      candidateSeqNum = 0;
      LOG.debug("HKMO additional address failed to assign sequence");
      break;
    }

    LOG.info("Candidate Seq: " + candidateSeqNum);
    return getAvailAddrSeqNumInclRdc(existingAddrSeqSet, candidateSeqNum);
  }

  private String getAvailAddrSeqNumInclRdc(Set<String> existingAddrSeqSet, int candidateSeqNum) {
    int availSeqNum = 0;
    if (existingAddrSeqSet.contains(String.format("%03d", candidateSeqNum))) {
      availSeqNum = candidateSeqNum;
      while (existingAddrSeqSet.contains(String.format("%03d", availSeqNum))) {
        availSeqNum++;
        if (availSeqNum > 999) {
          availSeqNum = 1;
        }
      }
    } else {
      availSeqNum = candidateSeqNum;
    }

    LOG.info("Avail: " + availSeqNum);

    return String.format("%03d", availSeqNum);
  }

  private List<Addr> getAddressByType(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GET.BYTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    List<Addr> addrList = query.getResults(Addr.class);
    return addrList;
  }

  private Set<String> getExistingAddrSeqInclRdc(EntityManager entityManager, long reqId) {
    DataPK pk = new DataPK();
    pk.setReqId(reqId);
    Data data = entityManager.find(Data.class, pk);

    String cmrNo = data.getCmrNo();
    Set<String> allAddrSeqFromAddr = getAllSavedSeqFromAddr(entityManager, reqId);
    Set<String> allAddrSeqFromRdc = getAllSavedSeqFromRdc(entityManager, cmrNo, data.getCmrIssuingCntry());

    Set<String> mergedAddrSet = new HashSet<>();
    mergedAddrSet.addAll(allAddrSeqFromAddr);
    mergedAddrSet.addAll(allAddrSeqFromRdc);

    return mergedAddrSet;
  }

  private Set<String> getAllSavedSeqFromAddr(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("CA.GET.ADDRSEQ.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<String> results = query.getResults(String.class);

    Set<String> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(results);

    return addrSeqSet;
  }

  private Set<String> getAllSavedSeqFromRdc(EntityManager entityManager, String cmrNo, String cmrIssuingCntry) {
    String sql = ExternalizedQuery.getSql("CA.GET.KNA1_ZZKV_SEQNO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<String> resultsRDC = query.getResults(String.class);
    Set<String> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(resultsRDC);

    return addrSeqSet;
  }

}
