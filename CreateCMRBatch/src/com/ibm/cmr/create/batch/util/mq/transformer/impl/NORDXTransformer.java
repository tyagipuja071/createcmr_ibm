/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MachinesToInstall;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.impl.NORDXHandler;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * @author Rangoli Saxena
 * 
 */
public class NORDXTransformer extends MessageTransformer {

  private static final Logger LOG = Logger.getLogger(NORDXTransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "ZP02" };

  protected static Map<String, String> SOURCE_CODE_MAP = new HashMap<String, String>();

  static {
    SOURCE_CODE_MAP.put(SystemLocation.SWEDEN, "SXX");
    SOURCE_CODE_MAP.put(SystemLocation.NORWAY, "NXX");
    SOURCE_CODE_MAP.put(SystemLocation.FINLAND, "FXX");
    SOURCE_CODE_MAP.put(SystemLocation.DENMARK, "DXX");
  }

  public NORDXTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    boolean crossBorder = isCrossBorder(addrData);
    String countrySubRegion = getSubRegionCode(cmrData, handler);

    LOG.debug("Handling Data for " + (update ? "update" : "create") + " request.");

    String custType = !StringUtils.isEmpty(cmrData.getCustSubGrp()) ? cmrData.getCustSubGrp() : "";
    String cntryUse = !StringUtils.isEmpty(cmrData.getCountryUse()) ? cmrData.getCountryUse() : "";
    String subIndCd = !StringUtils.isEmpty(cmrData.getSubIndustryCd()) ? cmrData.getSubIndustryCd() : "";
    String addrTyp = !StringUtils.isEmpty(addrData.getId().getAddrType()) ? addrData.getId().getAddrType() : "";
    String company = !StringUtils.isEmpty(cmrData.getCompany()) ? cmrData.getCompany() : "";
    String adminAcDSC = !StringUtils.isEmpty(cmrData.getEngineeringBo()) ? cmrData.getEngineeringBo() : "";
    String taxCode = !StringUtils.isEmpty(cmrData.getTaxCd1()) ? cmrData.getTaxCd1() : "";
    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    String sbo = !StringUtils.isEmpty(cmrData.getSalesBusOffCd()) ? cmrData.getSalesBusOffCd() : "";
    String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";

    boolean hasSbo = !StringUtils.isEmpty(sbo) ? true : false;

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.put("IMS", subIndCd);
    messageHash.put("TaxCode", taxCode);
    messageHash.put("EmbargoCode", embargoCode);
    messageHash.put("ACAdmDSC", adminAcDSC);
    messageHash.remove("IsBusinessPartner");
    messageHash.put("VAT", vat);
    // setVATValues(handler, messageHash, cmrData, crossBorder, addrData,
    // update);

    LOG.debug("Handling MQIntfReqQueue Issuing Country for " + handler.mqIntfReqQueue.getCmrIssuingCntry());
    if ("846".equalsIgnoreCase(handler.mqIntfReqQueue.getCmrIssuingCntry()))
      setDefaultSwedenValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("806".equalsIgnoreCase(handler.mqIntfReqQueue.getCmrIssuingCntry()))
      setDefaultNorwayValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("FI".equalsIgnoreCase(countrySubRegion))
      setDefaultFinlandValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("DK".equalsIgnoreCase(countrySubRegion))
      setDefaultDenmarkValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("FO".equalsIgnoreCase(countrySubRegion))
      setDefaultFaroeIslandsValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("IS".equalsIgnoreCase(countrySubRegion))
      setDefaultIcelandValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("GL".equalsIgnoreCase(countrySubRegion))
      setDefaultGreenlandValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("LV".equalsIgnoreCase(countrySubRegion))
      setDefaultLatviaValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("LT".equalsIgnoreCase(countrySubRegion))
      setDefaultLithuaniaValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("EE".equalsIgnoreCase(countrySubRegion))
      setDefaultEstoniaValues(handler, messageHash, cmrData, crossBorder, addrData, update);

    if (hasSbo) {
      messageHash.put("SBO", sbo + "0ISU");
      messageHash.put("IBO", messageHash.get("SBO"));
    } else {
      messageHash.put("SBO", "");
      messageHash.put("IBO", "");
    }

    /*
     * if (crossBorder) messageHash.put("VAT", "");
     */

    if (!update) {

      messageHash.put("CollectionCode", "");
      messageHash.put("MarketingResponseCode", "2");
      // confirm tag start
      // messageHash.put("EmployeeSize", "0");
      if (addrTyp.equalsIgnoreCase("ZS01")) {
        messageHash.put("LocationNumber", addrData.getLandCntry() + "000");
      }

      // messageHash.put("MktgCoordDiv", "2");
      // messageHash.put("EPLRegisteredInd", "1");
      messageHash.put("EPLLanguageCode", "1");
      // confirm tag end
      messageHash.put("CEdivision", "2");
      if (cmrData.getCmrIssuingCntry().equalsIgnoreCase("846")) {
        messageHash.put("LangCode", "S");
      } else if (cntryUse.equalsIgnoreCase("702EE") || cntryUse.equalsIgnoreCase("702LV") || cntryUse.equalsIgnoreCase("702LT")) {
        messageHash.put("LangCode", "3");
      } else {
        messageHash.put("LangCode", "1");
      }
      messageHash.put("ARemark", "NO");

      if (custType.contains("BUS")) {
        // Business Partner
        messageHash.put("MarketingResponseCode", "5");
        messageHash.put("ARemark", "YES");
      } else if (custType.contains("IBM")) { // GOVRN,XGOV,ccGOV,ccXGO
        messageHash.put("CustomerType", "98");
      } else if (!custType.equalsIgnoreCase("FISOF") && (custType.equalsIgnoreCase("INTSO") || custType.contains("ISO"))) {
        // Internal SO
        messageHash.put("CollectionCode", "0000SO");
      } else if (!custType.equalsIgnoreCase("INTSO") && (custType.contains("INT") || custType.contains("TER"))) {
        // Internal
        messageHash.put("CollectionCode", "000INT");
      } else {
        messageHash.put("ARemark", "NO");
      }

      if (!StringUtils.isEmpty(company))
        messageHash.put("LeadingAccountNo", company + messageHash.get("MarketingResponseCode"));
      else
        messageHash.put("LeadingAccountNo", company);

    } else {

      // send the current marketing response code to avoid loss
      String currMrc = handler.currentCMRValues.get("MarketingResponseCode");
      // String currAremark = handler.currentCMRValues.get("ARemark");
      // LOG.debug("Current ARemark: " + currAremark);
      LOG.debug("Current MRC: " + currMrc);

      // messageHash.put("ARemark", currAremark);
      if (!StringUtils.isEmpty(currMrc)) {
        messageHash.put("LeadingAccountNo", company + currMrc);
      } else {
        messageHash.put("LeadingAccountNo", company);
      }

      if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType()))
        messageHash.put("MailPhone", addrData.getCustPhone());
      messageHash.remove("VAT");
    }
  }

  private void setVATValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Setting VAT for Nordcis");

    String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
    String landCtryZS01 = getLandedCntryInZS01(handler);
    boolean hasVat = !StringUtils.isEmpty(vat) ? true : false;
    boolean hasLandCtryZS01 = !StringUtils.isEmpty(landCtryZS01) ? true : false;

    if (hasLandCtryZS01 && "SE".equalsIgnoreCase(landCtryZS01)) {
      if (hasVat && vat.length() >= 12)
        messageHash.put("VAT", vat.substring(2, 12));
    } else if (hasLandCtryZS01 && "NO".equalsIgnoreCase(landCtryZS01)) {
      if (hasVat && vat.length() > 2)
        messageHash.put("VAT", "0" + vat.substring(2));
    } else if (hasLandCtryZS01 && "FI".equalsIgnoreCase(landCtryZS01)) {
      if (hasVat && vat.length() > 2)
        messageHash.put("VAT", "00" + vat.substring(2));
    } else if (hasLandCtryZS01 && "DK".equalsIgnoreCase(landCtryZS01)) {
      if (hasVat && vat.length() > 2)
        messageHash.put("VAT", "00" + vat.substring(2));
    } else {
      messageHash.put("VAT", "");
    }
  }

  private void setDefaultSwedenValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Sweden");

    String landCtryZS01 = getLandedCntryInZS01(handler);
    boolean hasLandCtryZS01 = !StringUtils.isEmpty(landCtryZS01) ? true : false;

    if (hasLandCtryZS01 && "SE".equalsIgnoreCase(landCtryZS01)) {
      String postCd = getPostCodeInZS01(handler);
      boolean hasPostCd = !StringUtils.isEmpty(postCd) ? true : false;
      int postalCode = 0;
      if (hasPostCd && postCd.length() > 2)
        postalCode = Integer.parseInt(postCd.substring(0, 2));

      if (postalCode >= 10 && postalCode <= 19)
        messageHash.put("DPCEBO", "130");
      else if (postalCode >= 20 && postalCode <= 39)
        messageHash.put("DPCEBO", "140");
      else if (postalCode >= 40 && postalCode <= 57)
        messageHash.put("DPCEBO", "110");
      else if (postalCode >= 58 && postalCode <= 76)
        messageHash.put("DPCEBO", "130");
      else if (postalCode >= 77 && postalCode <= 98)
        messageHash.put("DPCEBO", "130");
    } else {
      messageHash.put("DPCEBO", "130");
    }
  }

  private void setDefaultNorwayValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Norway");

    // String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() :
    // "";
    // boolean hasVat = !StringUtils.isEmpty(vat) ? true : false;
    /*
     * String postCd = getPostCodeInZS01(handler); int postalCode =
     * Integer.parseInt(postCd); if (postalCode >= 0 && postalCode <= 3999)
     * messageHash.put("SBO", "1000ISU"); else if (postalCode >= 4000 &&
     * postalCode <= 4999) messageHash.put("SBO", "4000ISU"); else if
     * (postalCode >= 5000 && postalCode <= 6999) messageHash.put("SBO",
     * "5000ISU"); else if (postalCode >= 7000 && postalCode <= 9999)
     * messageHash.put("SBO", "7000ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    /*
     * if(hasVat) messageHash.put("VAT", "0" + vat.substring(2));
     */

  }

  private void setDefaultFinlandValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Finland");

    /*
     * String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() :
     * ""; boolean hasVat = !StringUtils.isEmpty(vat) ? true : false; if(hasVat)
     * messageHash.put("VAT", "00" + vat.substring(2));
     */
    /*
     * messageHash.put("SBO", "3450ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    messageHash.put("DPCEBO", "X900000");
  }

  private void setDefaultLatviaValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Latvia");

    // messageHash.put("VAT", "");
    /*
     * messageHash.put("SBO", "0380ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    messageHash.put("DPCEBO", "X900000");
    if (!update) {
      messageHash.put("OverseasTerritory", "105");
    }
  }

  private void setDefaultLithuaniaValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Lithuania");

    // messageHash.put("VAT", "");
    /*
     * messageHash.put("SBO", "0390ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    messageHash.put("DPCEBO", "X900000");
    if (!update) {
      messageHash.put("OverseasTerritory", "106");
    }
  }

  private void setDefaultEstoniaValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Estonia");

    // messageHash.put("VAT", "");
    /*
     * messageHash.put("SBO", "0370ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    messageHash.put("DPCEBO", "X900000");
    if (!update) {
      messageHash.put("OverseasTerritory", "104");
    }
  }

  private void setDefaultDenmarkValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Denmark");

    String landCtryZS01 = getLandedCntryInZS01(handler);
    boolean hasLandCtryZS01 = !StringUtils.isEmpty(landCtryZS01) ? true : false;

    if (hasLandCtryZS01 && "DK".equalsIgnoreCase(landCtryZS01)) {
      String postCd = getPostCodeInZS01(handler);
      int postalCode = Integer.parseInt(postCd);
      if (postalCode >= 0 && postalCode <= 4999)
        messageHash.put("DPCEBO", "000281X");
      else if (postalCode >= 5000 && postalCode <= 7399)
        messageHash.put("DPCEBO", "000246X");
      else if (postalCode >= 7400 && postalCode <= 9999)
        messageHash.put("DPCEBO", "000245X");
    } else {
      messageHash.put("DPCEBO", "000281X");
    }

  }

  private void setDefaultFaroeIslandsValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder,
      Addr addrData, boolean update) {
    LOG.debug("Handling Data for Faroe Islands");

    // messageHash.put("VAT", "");
    /*
     * messageHash.put("SBO", "3420ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    messageHash.put("DPCEBO", "000200F");
    if (!update) {
      messageHash.put("OverseasTerritory", "102");
    }
  }

  private void setDefaultIcelandValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Iceland");

    // messageHash.put("VAT", "");
    /*
     * messageHash.put("SBO", "3420ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    messageHash.put("DPCEBO", "000200I");
    if (!update) {
      messageHash.put("OverseasTerritory", "742");
    }

  }

  private void setDefaultGreenlandValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Greenland");

    // messageHash.put("VAT", "");
    /*
     * messageHash.put("SBO", "3420ISU"); messageHash.put("IBO",
     * messageHash.get("SBO"));
     */
    messageHash.put("DPCEBO", "000200G");
    if (!update) {
      messageHash.put("OverseasTerritory", "103");
    }
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    boolean crossBorder = isCrossBorder(addrData);
    String countrySubRegion = getSubRegionCode(cmrData, handler);
    String addrKey = getAddressKey(addrData.getId().getAddrType());

    LOG.debug("Handling Address for " + (update ? "update" : "create") + " request.");

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String ZipCode = "";
    String City = "";

    // get values of address fields from UI
    String customerName = !StringUtils.isEmpty(addrData.getCustNm1()) ? addrData.getCustNm1() : "";
    String nameCont = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
    String attPerson = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4() : "";
    String street = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
    String streetCont = !StringUtils.isEmpty(addrData.getAddrTxt2()) ? addrData.getAddrTxt2() : "";
    String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
    String postalCd = !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "";
    String city = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
    String regex = "\\d+";

    // checkers if value is present on UI
    boolean hasNameCont = !StringUtils.isEmpty(nameCont) ? true : false;
    boolean hasAttnPerson = !StringUtils.isEmpty(attPerson) ? true : false;
    boolean hasStreet = !StringUtils.isEmpty(street) ? true : false;
    boolean hasPoBox = !StringUtils.isEmpty(poBox) ? true : false;
    boolean hasPostalCd = !StringUtils.isEmpty(postalCd) ? true : false;
    boolean hasCity = !StringUtils.isEmpty(city) ? true : false;

    if (hasAttnPerson) {
      attPerson = "ATT " + attPerson;
    }
    if (hasPoBox) {
      poBox = "$PO BOX " + poBox;
    }
    if (hasStreet) {
      street = "#" + street;
    }
    if (addrData.getLandCntry().equalsIgnoreCase(countrySubRegion)
        && ("GL".equalsIgnoreCase(countrySubRegion) || "FO".equalsIgnoreCase(countrySubRegion) || "IS".equalsIgnoreCase(countrySubRegion))) {
      postalCd = postalCd;
    } else if (crossBorder) {
      if (hasPostalCd && postalCd.matches(regex)) {
        postalCd = addrData.getLandCntry() + "-" + postalCd;
      }
    } else {
      if (MQMsgConstants.ADDR_ZS02.equals(addrData.getId().getAddrType())) {
        if (SystemLocation.SWEDEN.equals(cmrData.getCmrIssuingCntry())) {
          postalCd = "S-" + postalCd;
        } else if ("FI".equalsIgnoreCase(countrySubRegion)) {
          postalCd = "FIN-" + postalCd;
        }
      }
      if ("EE".equalsIgnoreCase(countrySubRegion) || "LV".equalsIgnoreCase(countrySubRegion))
        postalCd = addrData.getLandCntry() + "-" + postalCd;
    }

    // line1 is always customer name
    line1 = customerName;
    // set address lines
    // Domestic and cross borders - all
    // line2 = Customer Name Cont / Att / Street / PO BOX
    // line3 = Att / Street / PO BOX / postal code city
    // line4 = Street / POBOX / postal code city / Country for CB
    // line5 = PO BOX / postal code city / Country for CB
    // InstallingZipCode + InstallingCity/InstallingAddress6 (CB) = Postal code
    // city / Country for CB

    if (hasNameCont && hasAttnPerson) {
      line2 = nameCont;
      line3 = attPerson;
      if (hasStreet) {
        line4 = street;
        line5 = poBox;
      } else {
        line4 = poBox;
        line5 = "";
      }
    } else if (hasNameCont) {
      line2 = nameCont;
      if (hasStreet) {
        line3 = street;
        line4 = poBox;
        line5 = "";
      } else if (hasPoBox) {
        line3 = poBox;
        line4 = "";
        line5 = "";
      } else {
        line3 = "";
        line4 = "";
        line5 = "";
      }
    } else if (hasAttnPerson) {
      line2 = attPerson;
      if (hasStreet) {
        line3 = street;
        line4 = poBox;
        line5 = "";
      } else if (hasPoBox) {
        line3 = poBox;
        line4 = "";
        line5 = "";
      } else {
        line3 = "";
        line4 = "";
        line5 = "";
      }
    } else {
      if (hasStreet) {
        line2 = street;
        line3 = poBox;
        line4 = "";
        line5 = "";
      } else {
        line2 = poBox;
        line3 = "";
        line4 = "";
        line5 = "";
      }
    }

    if (crossBorder)
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }

    // send machines (type + serial)
    List<MachinesToInstall> machines = getMachines(handler, addrData);
    if (machines != null && machines.size() > 0) {
      lineNo = 1;
      for (MachinesToInstall machine : machines) {
        messageHash.put("MachineType" + lineNo, machine.getId().getMachineTyp());
        messageHash.put("MachineSerial" + lineNo, machine.getId().getMachineSerialNo());
        lineNo++;
      }
    }

    if (!update) {
      if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType()))
        messageHash.put("MailPhone", addrData.getCustPhone());
    }

    messageHash.put(addrKey + "ZipCode", postalCd);
    messageHash.put(addrKey + "City", city);

    if (update && "ZS01".equalsIgnoreCase(addrData.getId().getAddrType())) {
      LOG.debug("Checking if current address is updated or added..");
      boolean isAddressUpdated = RequestUtils.isUpdated(handler.getEntityManager(), addrData, cmrData.getCmrIssuingCntry());
      LOG.debug(" - Updated/Added: " + isAddressUpdated);
      boolean isVATChanged = isVATChanged(handler);
      if (isVATChanged && isAddressUpdated) {
        String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
        messageHash.put("VAT", vat);
        messageHash.put("PrintSequence", handler.currentCMRValues.get("PrintSequence"));
      } else if (!isVATChanged && isAddressUpdated) {
        messageHash.remove("VAT");
        messageHash.remove("PrintSequence");
      } else if (isVATChanged && !isAddressUpdated) {

        messageHash.remove("VAT");
        messageHash.remove("PrintSequence");

        messageHash.remove(addrKey + "Name");
        messageHash.remove(addrKey + "ZipCode");
        messageHash.remove(addrKey + "City");
        messageHash.remove(addrKey + "POBox");
        messageHash.remove("MailPhone");
        messageHash.remove("AddressUse");

        int removelineNo = 1;
        LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
        for (String line : lines) {
          messageHash.remove(addrKey + "Address" + removelineNo);
          removelineNo++;
        }
        String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
        messageHash.put("VAT", vat);
        messageHash.put("PrintSequence", handler.currentCMRValues.get("PrintSequence"));
      }
    }

  }

  private String getPostCodeInZS01(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getPostCd();
      }
    return "";
  }

  private String getLandedCntryInZS01(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getLandCntry();
      }
    return "";
  }

  private List<MachinesToInstall> getMachines(MQMessageHandler handler, Addr addr) {
    if (handler.getEntityManager() != null) {
      String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_MACHINES");
      PreparedQuery query = new PreparedQuery(handler.getEntityManager(), sql);
      query.setParameter("REQ_ID", addr.getId().getReqId());
      query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
      query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());
      query.setForReadOnly(true);
      return query.getResults(MachinesToInstall.class);
    }
    return null;
  }

  @Override
  public boolean shouldForceSendAddress(MQMessageHandler handler, Addr nextAddr) {
    if (nextAddr == null) {
      return false;
    }

    boolean update = "U".equals(handler.adminData.getReqType());
    LOG.debug("Checking if current address is updated or added..");
    boolean isAddressUpdated = RequestUtils.isUpdated(handler.getEntityManager(), handler.addrData, handler.cmrData.getCmrIssuingCntry());
    LOG.debug(" - Updated/Added: " + isAddressUpdated);
    if (update && "ZS01".equalsIgnoreCase(nextAddr.getId().getAddrType()) && !isAddressUpdated) {
      boolean isVATChanged = isVATChanged(handler);
      if (isVATChanged) {
        return true;
      }
    }

    String sql = ExternalizedQuery.getSql("MACHINES.CHECK_NEWLY_ADDED");
    PreparedQuery query = new PreparedQuery(handler.getEntityManager(), sql);
    query.setParameter("REQ_ID", nextAddr.getId().getReqId());
    query.setParameter("ADDR_TYPE", nextAddr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", nextAddr.getId().getAddrSeq());
    query.setForReadOnly(true);

    return query.exists();
  }

  /*
   * protected boolean isCrossBorder(Data cmrData, MQMessageHandler handler,
   * Addr addr) { String cntryUse = cmrData.getCountryUse(); String landCntry =
   * addr.getLandCntry(); String cd = ""; String subRegionCd = "";
   * 
   * if (!StringUtils.isBlank(cntryUse) && cntryUse.length() > 3) { subRegionCd
   * = cntryUse.substring(3); return !landCntry.equals(subRegionCd); } else { cd
   * = NORDXHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry()); return cd !=
   * null && !cd.equals(landCntry); } }
   */

  protected boolean isCrossBorder(Addr addr) {
    String cd = NORDXHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    return cd != null && !cd.equals(addr.getLandCntry());
  }

  protected String getSubRegionCode(Data cmrData, MQMessageHandler handler) {
    String cntryUse = cmrData.getCountryUse();
    String subRegionCd = "";

    if (!StringUtils.isBlank(cntryUse) && cntryUse.length() > 3) {
      subRegionCd = cntryUse.substring(3);
    } else if (!StringUtils.isBlank(cntryUse) && cntryUse.length() == 3) {
      subRegionCd = NORDXHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    }
    return subRegionCd;
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mail";
    case "ZI01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "Soft";
    case "ZP02":
      return "Install";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mailing";
    case "ZI01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EPL";
    case "ZP02":
      return "Installing";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return getCmrIssuingCntry();
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "1";
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    }
  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {

    return true;
  }

  @Override
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    if (nextAddr == null) {
      return false;
    }
    return true;
  }

  /**
   * Gets imported VAT number
   * 
   * @param entityManager
   * @param model
   * @return
   */
  public boolean isVATChanged(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    // boolean update = "U".equals(handler.adminData.getReqType());
    boolean isVATChanged = false;
    String vatCurrent = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
    String vatImported = "";
    LOG.debug("inside isVATChanged: handler.getEntityManager() hai " + handler.getEntityManager());
    if (handler.getEntityManager() != null) {
      LOG.debug("Fetching imported Vat Number for request Id: " + handler.adminData.getId().getReqId());
      String sql = ExternalizedQuery.getSql("NORDX.GET_IMPORTED_VAT");
      PreparedQuery query = new PreparedQuery(handler.getEntityManager(), sql);
      query.setParameter("REQ_ID", handler.adminData.getId().getReqId());
      query.setForReadOnly(true);
      List<String> vatImportedList = query.getResults(String.class);
      if (vatImportedList != null && vatImportedList.size() > 0) {
        vatImported = !StringUtils.isEmpty(vatImportedList.get(0)) ? vatImportedList.get(0) : "";
      }
      if (!vatCurrent.trim().equalsIgnoreCase(vatImported.trim())) {
        isVATChanged = true;
      }
    }
    return isVATChanged;
  }
}
