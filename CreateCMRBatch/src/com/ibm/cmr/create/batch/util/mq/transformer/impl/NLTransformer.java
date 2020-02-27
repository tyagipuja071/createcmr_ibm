/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Arrays;
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
import com.ibm.cio.cmr.request.util.geo.impl.NLHandler;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * @author Rangoli Saxena
 * 
 */
public class NLTransformer extends MessageTransformer {

  private static final Logger LOG = Logger.getLogger(NLTransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZS01", "ZKVK", "ZVAT", "ZP01", "ZD01", "ZI01" };
  protected boolean duplicateRecordFound = false;
  protected Map<String, String> dupCMRValues = new HashMap<String, String>();
  protected List<String> dupShippingSequences = null;

  protected static Map<String, String> SOURCE_CODE_MAP = new HashMap<String, String>();

  static {
    SOURCE_CODE_MAP.put(SystemLocation.NETHERLANDS, "EFO");
  }

  public NLTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    Admin adminData = handler.adminData;

    boolean update = "U".equals(handler.adminData.getReqType());
    LOG.debug("Handling Data for " + (update ? "update" : "create") + " request.");
    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    String custSubGrp = !StringUtils.isEmpty(cmrData.getCustSubGrp()) ? cmrData.getCustSubGrp() : "";
    String custGrp = !StringUtils.isEmpty(cmrData.getCustGrp()) ? cmrData.getCustGrp() : "";
    String cntry = !StringUtils.isEmpty(cmrData.getCmrIssuingCntry()) ? cmrData.getCmrIssuingCntry() : "";
    String subIndCd = !StringUtils.isEmpty(cmrData.getSubIndustryCd()) ? cmrData.getSubIndustryCd() : "";
    String sbo = !StringUtils.isEmpty(cmrData.getEngineeringBo()) ? cmrData.getEngineeringBo() : "";
    String taxCode = !StringUtils.isEmpty(cmrData.getTaxCd1()) ? cmrData.getTaxCd1() : "";
    String dept = !StringUtils.isEmpty(addrData.getDept()) ? addrData.getDept() : "";
    String collCd = !StringUtils.isEmpty(cmrData.getCollectionCd()) ? cmrData.getCollectionCd() : "";
    String abbrevNm = !StringUtils.isEmpty(cmrData.getAbbrevNm()) ? cmrData.getAbbrevNm() : "";
    String abbrevLoc = !StringUtils.isEmpty(cmrData.getAbbrevLocn()) ? cmrData.getAbbrevLocn() : "";
    String inacCd = !StringUtils.isEmpty(cmrData.getInacCd()) ? cmrData.getInacCd() : "";
    String isicCd = !StringUtils.isEmpty(cmrData.getIsicCd()) ? cmrData.getIsicCd() : "";
    String isuCd = !StringUtils.isEmpty(cmrData.getIsuCd()) ? cmrData.getIsuCd() : "";
    String clientTier = !StringUtils.isEmpty(cmrData.getClientTier()) ? cmrData.getClientTier() : "";
    String entp = !StringUtils.isEmpty(cmrData.getEnterprise()) ? cmrData.getEnterprise() : "";
    String ecoCd = !StringUtils.isEmpty(cmrData.getEconomicCd()) ? cmrData.getEconomicCd() : "";
    String departmentNumber = !StringUtils.isEmpty(cmrData.getIbmDeptCostCenter()) ? cmrData.getIbmDeptCostCenter() : "";
    String requestReason = !StringUtils.isEmpty(adminData.getReqReason()) ? adminData.getReqReason() : "";
    String addrKey = getAddressKey(addrData.getId().getAddrType());
    String phone = !StringUtils.isEmpty(addrData.getCustPhone()) ? addrData.getCustPhone() : "@";

    if (!update) {
      messageHash.put("TransactionCode", "N");
      if (custSubGrp.equals("INTER")) {
        messageHash.put("DepartmentNumber", departmentNumber);
      } else {
        messageHash.put("DepartmentNumber", "");
      }
    } else {
      messageHash.put("DepartmentNumber", departmentNumber);
    }
    messageHash.put("Country", cntry);
    messageHash.put("CountryID", "Netherlands");
    if (custSubGrp.equals("INTER")) {
      messageHash.put("AccAdBo", dept);
    }
    messageHash.put("EmbargoCode", embargoCode);
    messageHash.put("CurrencyCode", "EU");
    messageHash.put("CollectionCode", collCd);
    messageHash.put("CompanyName", abbrevNm);
    messageHash.put("DPCEBO", "");
    messageHash.put("EnterpriseNo", entp);
    messageHash.put("DistrictCode", "");
    messageHash.put("FSLICAM", abbrevNm);
    messageHash.put("IMS", subIndCd);
    messageHash.put("INAC", inacCd);
    messageHash.put("ISIC", isicCd);
    messageHash.put("ISU", isuCd + "" + clientTier);
    messageHash.put("MarketingResponseCode", "2");
    messageHash.put("TaxCode", taxCode);
    messageHash.put("PrintSequenceNo", "1");
    messageHash.put("SBO", sbo);
    messageHash.put("IBO", sbo);
    messageHash.put("LenovoOnly", "IBM");
    messageHash.put("AbbreviatedLocation", abbrevLoc);
    messageHash.put("LeasingCompany", "N");
    messageHash.put("IsBusinessPartner", "N");
    messageHash.put("ControlCodes", ecoCd);

    if (custGrp.equals("CROSS")) {
      messageHash.put("EGG", "000");
    } else {
      messageHash.put("EGG", "063");
    }

    if (update) {
      messageHash.put("TransactionCode", "M");
      if ("BKSC".equalsIgnoreCase(requestReason)) {
        messageHash.put("ModeOfPayment", "1");
      } else {
        messageHash.put("ModeOfPayment", "0");
      }
      if (addrKey.equals("General"))
        messageHash.put(addrKey + "Phone", phone);
    } else {
      messageHash.remove(addrKey + "Phone");
    }
    messageHash.remove("KVK");
    messageHash.remove("VAT");
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = isCrossBorder(addrData);

    String addrKey = getAddressKey(addrData.getId().getAddrType());

    LOG.debug("Handling Address for " + (update ? "update" : "create") + " request.");

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "Ext");
    messageHash.remove(addrKey + "Dpt");
    messageHash.remove(addrKey + "Att");
    messageHash.remove(addrKey + "Street");
    messageHash.remove(addrKey + "POBox");
    messageHash.remove(addrKey + "Plz");
    messageHash.remove(addrKey + "City");
    // messageHash.remove("KVK");
    // messageHash.remove("VAT");

    String companyName = "";
    String nameCont = "";
    String attPerson = "";
    String street = "";
    String poBox = "";
    String postalCd = "";
    String city = "";
    String dept = "";
    String kvk = "";
    String vat = "";
    String phone = "";
    String country = "";

    // get values of address fields from UI
    if (update || (!update && !CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrData.getId().getAddrType()))) {
      companyName = !StringUtils.isEmpty(addrData.getCustNm1()) ? addrData.getCustNm1() : "@";
      nameCont = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "@";
      attPerson = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4() : "@";
      street = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "@";
      poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "@";
      postalCd = !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "@";
      city = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "@";
      dept = !StringUtils.isEmpty(addrData.getDept()) ? addrData.getDept() : "@";
      kvk = !StringUtils.isEmpty(cmrData.getTaxCd2()) ? cmrData.getTaxCd2() : "";
      vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
      phone = !StringUtils.isEmpty(addrData.getCustPhone()) ? addrData.getCustPhone() : "@";
    } else {
      companyName = !StringUtils.isEmpty(addrData.getCustNm1()) ? addrData.getCustNm1() : "";
      nameCont = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
      attPerson = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4() : "";
      street = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
      poBox = !StringUtils.isEmpty(addrData.getPoBox()) ? addrData.getPoBox() : "";
      postalCd = !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "";
      city = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
      dept = !StringUtils.isEmpty(addrData.getDept()) ? addrData.getDept() : "";
      kvk = !StringUtils.isEmpty(cmrData.getTaxCd2()) ? cmrData.getTaxCd2() : "";
      vat = !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat() : "";
      phone = !StringUtils.isEmpty(addrData.getCustPhone()) ? addrData.getCustPhone() : "";
    }

    boolean hasPoBox = !StringUtils.isEmpty(poBox) ? true : false;
    boolean hasVat = !StringUtils.isEmpty(vat) ? true : false;
    boolean hasCity = !StringUtils.isEmpty(city) ? true : false;
    boolean hasKvk = !StringUtils.isEmpty(kvk) ? true : false;

    if (hasPoBox && !"@".equalsIgnoreCase(poBox)) {
      poBox = "PO BOX " + poBox;
    }

    if (crossBorder && hasCity) {
      country = LandedCountryMap.getCountryName(addrData.getLandCntry());
      city = city.concat(" " + country);
    }

    if (!CmrConstants.ADDR_TYPE.ZKVK.toString().equals(addrData.getId().getAddrType())
        && !CmrConstants.ADDR_TYPE.ZVAT.toString().equals(addrData.getId().getAddrType())) {
      messageHash.put(addrKey + "Name", companyName);
      messageHash.put(addrKey + "Ext", nameCont);
      messageHash.put(addrKey + "Dpt", dept);
      messageHash.put(addrKey + "Att", attPerson);
      messageHash.put(addrKey + "Street", street);
      messageHash.put(addrKey + "POBox", poBox);
      messageHash.put(addrKey + "Plz", postalCd);
      messageHash.put(addrKey + "City", city);

    } else if (CmrConstants.ADDR_TYPE.ZKVK.toString().equals(addrData.getId().getAddrType()) && hasKvk) {
      messageHash.put(addrKey + "Name", companyName);
      messageHash.put("KVK", kvk);
      messageHash.put(addrKey + "Address2", "@");
      messageHash.put(addrKey + "Ext", "@");
      messageHash.put(addrKey + "Dpt", "@");
      messageHash.put(addrKey + "Att", "@");
      messageHash.put(addrKey + "Street", street);
      messageHash.put(addrKey + "POBox", poBox);
      messageHash.put(addrKey + "Plz", postalCd);
      messageHash.put(addrKey + "City", city);

    } else if (CmrConstants.ADDR_TYPE.ZVAT.toString().equals(addrData.getId().getAddrType()) && hasVat) {
      messageHash.put(addrKey + "Name", companyName);
      if (hasVat && !"@".equalsIgnoreCase(vat) && vat.length() > 2) {
        messageHash.put("VAT", vat);
      }
      messageHash.put(addrKey + "Address2", "@");
      messageHash.put(addrKey + "Ext", "@");
      messageHash.put(addrKey + "Dpt", "@");
      messageHash.put(addrKey + "Att", "@");
      messageHash.put(addrKey + "Street", street);
      messageHash.put(addrKey + "POBox", poBox);
      messageHash.put(addrKey + "Plz", postalCd);
      messageHash.put(addrKey + "City", city);

    }

    if (!update) {
      messageHash.put(addrKey + "Phone", phone);
    } else {
      messageHash.remove(addrKey + "Phone");
    }

    if (update && "Y".equals(addrData.getImportInd())) {
      // send address lines as empty when update and address is imported.
      messageHash.put(addrKey + "Address1", "@");
      messageHash.put(addrKey + "Address2", "@");
      messageHash.put(addrKey + "Address3", "@");
      messageHash.put(addrKey + "Address4", "@");
      messageHash.put(addrKey + "Address5", "@");
    } else {
      // send address lines as empty when create
      messageHash.put(addrKey + "Address1", "");
      messageHash.put(addrKey + "Address2", "");
      messageHash.put(addrKey + "Address3", "");
      messageHash.put(addrKey + "Address4", "");
      messageHash.put(addrKey + "Address5", "");
    }

    if (Arrays.asList(ADDRESS_ORDER).contains(addrData.getId().getAddrType())) {
      messageHash.put("AddressNumber", addrData.getId().getAddrSeq());
    }

  }

  protected boolean isCrossBorder(Addr addr) {
    String cd = NLHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    return cd != null && !cd.equals(addr.getLandCntry());
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "General";
    case "ZI01":
      return "EPL";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZKVK":
      return "KVK";
    case "ZVAT":
      return "VAT";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "General";
    case "ZI01":
      return "EPL";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZKVK":
      return "KVK";
    case "ZVAT":
      return "VAT";
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
      return "1";
    case "ZKVK":
      return "2";
    case "ZVAT":
      return "3";
    case MQMsgConstants.ADDR_ZP01:
      return "4";
    case MQMsgConstants.ADDR_ZD01:
      return "5";
    case MQMsgConstants.ADDR_ZI01:
      return "6";
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    try {
      if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
        return true;
      }

      LOG.debug("No need to create duplicates. Completing request.");
      return true;

    } catch (Exception e) {
      LOG.error("Error in completing dual process request. Skipping dual process and completing request", e);
      return true;
    }
  }

  @Override
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    if (nextAddr == null) {
      return false;
    }
    Data cmrData = handler.cmrData;
    if (cmrData != null && StringUtils.isEmpty(cmrData.getTaxCd2()) && "ZKVK".equals(nextAddr.getId().getAddrType())) {
      // 1675871 - do not send VAT/KVK if VAT/KVK is empty
      return false;
    }
    if (cmrData != null && StringUtils.isEmpty(cmrData.getVat()) && "ZVAT".equals(nextAddr.getId().getAddrType())) {
      // 1675871 - do not send VAT/KVK if VAT/KVK is empty
      return false;
    }
    return true;
  }
}
