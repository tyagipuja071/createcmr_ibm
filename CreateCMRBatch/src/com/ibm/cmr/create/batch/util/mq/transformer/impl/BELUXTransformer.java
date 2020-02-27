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
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.impl.BELUXHandler;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * @author Rangoli Saxena
 * 
 */
public class BELUXTransformer extends MessageTransformer {

  private static final Logger LOG = Logger.getLogger(BELUXTransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02" };

  protected static Map<String, String> SOURCE_CODE_MAP = new HashMap<String, String>();

  static {
    SOURCE_CODE_MAP.put(SystemLocation.BELGIUM, "DBA");
  }

  public BELUXTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    Admin adminData = handler.adminData;
    boolean update = "U".equals(handler.adminData.getReqType());

    boolean crossBorder = isCrossBorder(cmrData, handler, addrData);
    String countrySubRegion = getSubRegionCode(cmrData, handler);

    LOG.debug("Handling Data for " + (update ? "update" : "create") + " request.");
    String custType = !StringUtils.isEmpty(cmrData.getCustSubGrp()) ? cmrData.getCustSubGrp() : "";
    String cntryUse = !StringUtils.isEmpty(cmrData.getCountryUse()) ? cmrData.getCountryUse() : "";
    String subIndCd = !StringUtils.isEmpty(cmrData.getSubIndustryCd()) ? cmrData.getSubIndustryCd() : "";
    String addrTyp = !StringUtils.isEmpty(addrData.getId().getAddrType()) ? addrData.getId().getAddrType() : "";
    String company = !StringUtils.isEmpty(cmrData.getCompany()) ? cmrData.getCompany() : "";
    String adminAcDSC = !StringUtils.isEmpty(cmrData.getEngineeringBo()) ? cmrData.getEngineeringBo() : "";
    String taxCode = !StringUtils.isEmpty(cmrData.getTaxCd1()) ? cmrData.getTaxCd1() : "";
    String enterpriseNo = !StringUtils.isEmpty(cmrData.getEnterprise()) ? cmrData.getEnterprise() : "";
    String dept = !StringUtils.isEmpty(cmrData.getIbmDeptCostCenter()) ? cmrData.getIbmDeptCostCenter() : "";
    String prefLang = !StringUtils.isEmpty(cmrData.getCustPrefLang()) ? cmrData.getCustPrefLang() : "";
    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    String economicCode = !StringUtils.isEmpty(cmrData.getEconomicCd()) ? cmrData.getEconomicCd() : "";
    String accTeamNo = !StringUtils.isEmpty(cmrData.getSearchTerm()) ? cmrData.getSearchTerm() : "";
    String requestReason = !StringUtils.isEmpty(adminData.getReqReason()) ? adminData.getReqReason() : "";
    String billingCntry = getBillingCountry(handler);
    String cd = ""; 
    String subRegionCd = "";
    
    boolean hasEconomicCode = !StringUtils.isEmpty(economicCode) ? true : false;
   
    messageHash.put("CustIdentCode", "2");
    /**
     * Defect 1813639:BELUX - customer identity code.
     */
    String instCntry = getInstingCountry(handler);
    String cntryStr= !StringUtils.isEmpty(billingCntry )? billingCntry : instCntry;
    
      if (!StringUtils.isBlank(cntryUse) && cntryUse.length() > 3) {
        subRegionCd = cntryUse.substring(3); 
        if(subRegionCd.equals(cntryStr) && "LU".equalsIgnoreCase(cntryStr)){
          messageHash.put("CustIdentCode", "1"); 
        }
      } else { 
          cd = BELUXHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry()); 
          if(cd !=null && cd.equalsIgnoreCase(cntryStr) && "BE".equalsIgnoreCase(cntryStr)){
            messageHash.put("CustIdentCode", "");
          }
      } 

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.put("IMS", subIndCd);
    messageHash.put("TaxCode", taxCode);
    messageHash.put("CurrencyCode", "EUR");
    messageHash.put("LangCode", prefLang);
    messageHash.put("EmbargoCode", embargoCode);
    messageHash.put("EnterpriseNo", enterpriseNo);
    messageHash.put("MarketingResponseCode", "3");
    messageHash.put("AllianceType", "");
    messageHash.put("AccAdBo", dept);
    // messageHash.put("LatePayCharge", "ST");
    messageHash.put("SR", accTeamNo);
    messageHash.put("EmployeeSize", "000001");
    
    setVATValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    
   

    if ("BKSC".equalsIgnoreCase(adminData.getReqReason()) && hasEconomicCode && update) {
      messageHash.put("EconomicCode", economicCode.replace("K", "F"));
    } else {
      messageHash.put("EconomicCode", economicCode);
    }

    /*
     * if (custType.equalsIgnoreCase("LUISO") || custType.contains("BEISO")) {
     * messageHash.put("AllianceType", "AA"); } else if
     * (custType.equalsIgnoreCase("BEPUB") || custType.contains("LUPUB")) {
     * messageHash.put("LatePayCharge", ""); }
     */

    if ("BE".equalsIgnoreCase(countrySubRegion))
      setDefaultBelgiumValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if ("LU".equalsIgnoreCase(countrySubRegion))
      setDefaultLuxembourgValues(handler, messageHash, cmrData, crossBorder, addrData, update);
    if (!update) {
      if (custType.equalsIgnoreCase("LUISO") || custType.contains("BEISO")) {
        // Internal SO
        messageHash.put("AllianceType", "AA");
        // if ("BE".equalsIgnoreCase(countrySubRegion))
        messageHash.put("LeasingCompany", "YES");
      }
      if (custType.equalsIgnoreCase("BEPUB") || custType.contains("LUPUB")) {
        messageHash.put("LatePayCharge", "");
      } else {
        messageHash.put("LatePayCharge", "ST");
      }
      messageHash.put("ModeOfPayment", "0");
      messageHash.put("CreditLimit", "");
    } else {
      if ("BKSC".equalsIgnoreCase(requestReason)){
        messageHash.put("ModeOfPayment", "1");
        messageHash.put("CreditLimit", "0.02");
      } else {
        messageHash.put("ModeOfPayment", "0");
        messageHash.put("CreditLimit", "@");
      }
    }
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    boolean crossBorder = isCrossBorder(cmrData, handler, addrData);
    String countrySubRegion = getSubRegionCode(cmrData, handler);
    String addrKey = getAddressKey(addrData.getId().getAddrType());

    LOG.debug("Handling Address for " + (update ? "update" : "create") + " request.");

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");
    messageHash.remove(addrKey + "Title");
    messageHash.remove(addrKey + "FirstName");
    messageHash.remove(addrKey + "LastName");
    messageHash.remove(addrKey + "Street");
    messageHash.remove(addrKey + "StreetNo");

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";

    // get values of address fields from UI
    String customerName = !StringUtils.isEmpty(addrData.getCustNm1()) ? addrData.getCustNm1() : "";
    String nameCont = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
    String postalCd = !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "";
    String city = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
    String title = !StringUtils.isEmpty(addrData.getDept()) ? addrData.getDept() : "";
    String firstName = !StringUtils.isEmpty(addrData.getCustNm3()) ? addrData.getCustNm3() : "";
    String lastName = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4() : "";
    String street = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
    String streetNo = !StringUtils.isEmpty(addrData.getAddrTxt2()) ? addrData.getAddrTxt2() : "";
    String poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
    String phoneNo = !StringUtils.isEmpty(addrData.getCustPhone()) ? addrData.getCustPhone() : "";
    String landCntry = !StringUtils.isEmpty(addrData.getLandCntry()) ? addrData.getLandCntry() : "";
    String regex = "\\d+";

    // checkers if value is present on UI
    boolean hasNameCont = !StringUtils.isEmpty(nameCont) ? true : false;
    boolean hasPostalCd = !StringUtils.isEmpty(postalCd) ? true : false;
    boolean hasCity = !StringUtils.isEmpty(city) ? true : false;
    boolean hasTitle = !StringUtils.isEmpty(title) ? true : false;
    boolean hasFirstName = !StringUtils.isEmpty(firstName) ? true : false;
    boolean hasLastName = !StringUtils.isEmpty(lastName) ? true : false;
    boolean hasStreet = !StringUtils.isEmpty(street) ? true : false;
    boolean hasStreetNo = !StringUtils.isEmpty(streetNo) ? true : false;
    boolean hasPoBox = !StringUtils.isEmpty(poBox) ? true : false;
    boolean hasPhoneNo = !StringUtils.isEmpty(phoneNo) ? true : false;

    if (hasPoBox) {
      poBox = "PO BOX " + poBox;
    }

    if ("LU".equalsIgnoreCase(landCntry)) {
      postalCd = "L " + postalCd;
    }

    messageHash.put(addrKey + "NameCont", nameCont);
    messageHash.put(addrKey + "Title", title);
    messageHash.put(addrKey + "FirstName", firstName);
    messageHash.put(addrKey + "LastName", lastName);
    messageHash.put(addrKey + "Street", street);
    messageHash.put(addrKey + "StreetNo", streetNo);
    messageHash.put(addrKey + "POBOX", poBox);
    messageHash.put(addrKey + "Phone", phoneNo);
    messageHash.put(addrKey + "ZipCode", postalCd);
    messageHash.put(addrKey + "City", city);

    // line1 is always customer name
    line1 = customerName;
    line2 = "";
    line3 = "";
    line4 = "";
    line5 = "";

/*    if (hasNameCont && (hasTitle || hasFirstName || hasLastName)) {
      line2 = nameCont;
      line3 = "";
      if (hasStreet || hasStreetNo) {
        line4 = "";
        line5 = poBox;
      } else {
        line4 = poBox;
        line5 = "";
      }
    } else if (hasNameCont) {
      line2 = nameCont;
      if (hasStreet || hasStreetNo) {
        line3 = "";
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
    } else if (hasTitle || hasFirstName || hasLastName) {
      line2 = "";
      if (hasStreet || hasStreetNo) {
        line3 = "";
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
      if (hasStreet || hasStreetNo) {
        line2 = "";
        line3 = poBox;
        line4 = "";
        line5 = "";
      } else {
        line2 = poBox;
        line3 = "";
        line4 = "";
        line5 = "";
      }
    }*/

    if (crossBorder)
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }
  }

  
  protected boolean isCrossBorder(Data cmrData, MQMessageHandler handler,Addr addr) { 
    String cntryUse = cmrData.getCountryUse(); 
    String landCntry = addr.getLandCntry(); 
    String cd = ""; 
    String subRegionCd = "";
   
    if (!StringUtils.isBlank(cntryUse) && cntryUse.length() > 3) {
      subRegionCd = cntryUse.substring(3); 
      return !subRegionCd.equals(landCntry); 
      } else { 
        cd = BELUXHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry()); 
        return cd !=null && !cd.equals(landCntry); 
    } 
  }
  
  /*protected boolean isCrossBorder(Addr addr) {
    String cd = BELUXHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    return cd != null && !cd.equals(addr.getLandCntry());
  }*/

  protected String getSubRegionCode(Data cmrData, MQMessageHandler handler) {
    String cntryUse = cmrData.getCountryUse();
    String subRegionCd = "";

    if (!StringUtils.isBlank(cntryUse) && cntryUse.length() > 3) {
      subRegionCd = cntryUse.substring(3);
    } else if (!StringUtils.isBlank(cntryUse) && cntryUse.length() == 3) {
      subRegionCd = BELUXHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    }
    return subRegionCd;
  }

  private void setVATValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Setting VAT for BELUX");

    String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
    String landCtryZP01 = getLandedCntryInZP01(handler);

    boolean hasVat = !StringUtils.isEmpty(vat) ? true : false;
    boolean hasLandCtryZP01 = !StringUtils.isEmpty(landCtryZP01) ? true : false;

    if (hasLandCtryZP01 && "BE".equalsIgnoreCase(landCtryZP01)) {
      if (hasVat && vat.length() >= 12)
        messageHash.put("VAT", "BE" + vat.substring(3, 12));
    } else {
      if (hasVat)
        messageHash.put("VAT", vat);
    }
  }

  private void setDefaultBelgiumValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData,
      boolean update) {
    LOG.debug("Handling Data for Belgium");
    int len = 0;
    /*
     * String vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() :
     * ""; boolean hasVat = !StringUtils.isEmpty(vat) ? true : false;
     */
    String accTeamNo = !StringUtils.isEmpty(cmrData.getSearchTerm()) ? cmrData.getSearchTerm() : "";
    String sbo = !StringUtils.isEmpty(cmrData.getSalesBusOffCd()) ? cmrData.getSalesBusOffCd() : "";
    messageHash.put("SBO", sbo);
    // messageHash.put("SBO", "0" + accTeamNo);
    messageHash.put("IBO", messageHash.get("SBO"));
    /*
     * if (hasVat) { len = vat.length(); messageHash.put("VAT",
     * "BE".concat(vat.substring((len - 9), len))); } else {
     * messageHash.put("VAT", vat); }
     */
  }

  private void setDefaultLuxembourgValues(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder,
      Addr addrData, boolean update) {
    LOG.debug("Handling Data for Luxembourg");

    String accTeamNo = !StringUtils.isEmpty(cmrData.getSearchTerm()) ? cmrData.getSearchTerm() : "";
    String sbo = !StringUtils.isEmpty(cmrData.getSalesBusOffCd()) ? cmrData.getSalesBusOffCd() : "";
    boolean hasSBO = !StringUtils.isEmpty(sbo) ? true : false;
    messageHash.put("SBO", sbo);
    if (hasSBO) {
      // messageHash.put("SBO", "0" + accTeamNo.substring(0, 5) + "1");
      // messageHash.put("IBO", "0" + accTeamNo.substring(0, 5) + "0");
      messageHash.put("IBO", sbo.substring(0, sbo.length() - 1) + "0");
    } else {
      messageHash.put("IBO", "");
    }
  }

  private String getLandedCntryInZP01(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZP01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getLandCntry();
      }
    return "";
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Inst";
    case "ZI01":
      return "Mail";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Bill";
    case "ZS02":
      return "Soft";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Installing";
    case "ZI01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EPL";
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
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    }
  }

  private String getBillingCountry(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZP01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getLandCntry();
      }
    return "";
  }
  
  private String getInstingCountry(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getLandCntry();
      }
    return "";
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
}
