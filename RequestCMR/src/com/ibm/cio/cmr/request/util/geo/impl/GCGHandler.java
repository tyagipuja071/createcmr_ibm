/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasAddress;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys.Address;

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
  private static final String SHIP_TO_FIXED_SEQ = "F";
  private static final List<String> BILL_TO_FIXED_SEQ = Arrays.asList("B", "C", "D");

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

      String addrType = getMappedAddressType(record.getCmrIssuedBy(), record.getCmrAddrTypeCode(), record.getCmrAddrSeq());

      LOG.info("After Mapped Addrtype: " + addrType);

      if ("ZP02".equals(addrType) || "ZI01".equals(addrType)) {
        handleRDcRecordValues(record);
        record.setCmrAddrTypeCode(addrType);
        converted.add(record);
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
      return "ZP02"; // address B, bill-to
    }
    if ("ZI01".equals(rdcType) && "E".equals(addressSeq)) {
      return "ZI01"; // address E, install-at
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

    String[] inputs = { record.getCmrName3(), record.getCmrName4(), record.getCmrStreetAddress(), record.getCmrCity(), record.getCmrCity2() };
    List<String> currentFields = Arrays.asList(record.getCmrDept() != null ? record.getCmrDept().toUpperCase().trim() : "XXXX");
    record.setCmrName3(null);
    record.setCmrName4(null);
    record.setCmrCity(null);
    record.setCmrCity2(null);
    record.setCmrDept(null);
    Queue<String> streets = new LinkedList<>();
    for (String street : inputs) {
      if (!StringUtils.isBlank(street) && !currentFields.contains(street.toUpperCase().trim())) {
        streets.add(street);
      }
    }
    String current = streets.peek() != null ? streets.remove() : null;
    record.setCmrStreetAddress(current);

    current = streets.peek() != null ? streets.remove() : null;
    record.setCmrStreetAddressCont(current);

    current = streets.peek() != null ? streets.remove() : null;
    record.setCmrCity(current);

    current = streets.peek() != null ? streets.remove() : null;
    record.setCmrCity2(current);

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
      } else if (SHIP_TO_ADDR_TYPE.equals(addrType)) {
        newAddrSeq = SHIP_TO_FIXED_SEQ;
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

}
