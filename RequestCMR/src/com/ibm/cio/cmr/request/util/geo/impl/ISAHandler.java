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

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasAddress;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys.Address;

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
public class ISAHandler extends APHandler {

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  private static final String[] IN_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4" };
  private static final String[] BD_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5" };
  private static final String[] LK_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4" };

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.INDIA, "IN");
    LANDED_CNTRY_MAP.put(SystemLocation.BANGLADESH, "BD");
    LANDED_CNTRY_MAP.put(SystemLocation.SRI_LANKA, "LK");
    LANDED_CNTRY_MAP.put(SystemLocation.NEPAL, "NP");
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
    ISAHandler handler = new ISAHandler();
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
}
