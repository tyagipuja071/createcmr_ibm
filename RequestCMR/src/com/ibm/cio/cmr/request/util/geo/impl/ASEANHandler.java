/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasAddress;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys.Address;

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
public class ASEANHandler extends APHandler {

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  private static final String[] ID_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] PH_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] SG_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] VN_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] TH_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] BN_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] MY_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] MM_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4" };

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
    ASEANHandler handler = new ASEANHandler();
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

    String line = lines.get(lines.size() - 1);
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
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }
}
