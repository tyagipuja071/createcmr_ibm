/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * {@link MessageTransformer} implementation for Greece.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GreeceTransformer extends EMEATransformer {

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode" };

  private static final String[] ADDRESS_ORDER = { "ZP01", "ZS01", "ZD01" };

  private static final Logger LOG = Logger.getLogger(IsraelTransformer.class);

  public GreeceTransformer() {
    super(SystemLocation.GREECE);

  }

  public GreeceTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    boolean update = "U".equals(handler.adminData.getReqType());
    Data cmrData = handler.cmrData;
    Addr addrData = handler.addrData;
    boolean crossBorder = isCrossBorder(addrData);

    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    handleEMEADefaults(handler, messageHash, cmrData, addrData, crossBorder);

    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");
    messageHash.put("SourceCode", "EFV");
    messageHash.put("CEdivision", "3");
    messageHash.put("MarketingResponseCode", "3");
    messageHash.put("ARemark", "");
    messageHash.put("EnterpriseNo", cmrData.getEnterprise());

    if (update) {
      messageHash.put("CollectionCode", cmrData.getCollectionCd());
    } else {
      messageHash.put("CollectionCode", "");
    }
    messageHash.put("EconomicCode", "");
    messageHash.put("InvNumber", "");
    messageHash.put("TaxCode", "");

    String sbo = messageHash.get("SBO");
    if (!StringUtils.isEmpty(sbo) && sbo.length() < 7) {
      sbo = StringUtils.rightPad(sbo, 7, '0');
    }

    String cebo = messageHash.get("DPCEBO");
    if (!StringUtils.isEmpty(cebo) && cebo.length() < 7) {
      cebo = StringUtils.rightPad(cebo, 7, '0');
    }
    messageHash.put("SBO", sbo);
    messageHash.put("IBO", sbo);
    messageHash.put("DPCEBO", cebo);

    messageHash.put("SMR", cmrData.getSalesTeamCd());
    messageHash.put("LangCode", "1");

    // add the CustomerType
    String custType = cmrData.getCustSubGrp();
    if (!StringUtils.isBlank(custType)) {
      if ("BUSPR".equals(custType) || "XBP".equals(custType)) {
        messageHash.put("MarketingResponseCode", "5");
        messageHash.put("ARemark", "YES");
        messageHash.put("CustomerType", "");
      } else if ("GOVRN".equals(custType)) {
        messageHash.put("CustomerType", "G");
      } else {
        messageHash.put("CustomerType", "");
      }
    } else {
      messageHash.put("CustomerType", "");
    }
    if (update) {
      for (String field : NO_UPDATE_FIELDS) {
        messageHash.remove(field);
      }
    }

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    boolean update = "U".equals(handler.adminData.getReqType());
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean crossBorder = isCrossBorder(addrData);

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    messageHash.put("SourceCode", "EFV");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());
    // <XXXAddress1> -> name [custNm1]
    // <XXXAddress2> -> nickname (occupation if not supplied) [custNm2, addrTxt]
    // <XXXAddress3> -> occupation or PO Box [addrTxt or poBox]
    // <XXXAddress4> -> street [addrTxt2]
    // <XXXAddress5> -> postal code and city [postCd, city1]
    // <XXXAddress6> -> country
    // <XXXAddressT> -> tax office
    // <XXXAddressU> -> vat
    // <XXXPhone> -> phone

    // customer name
    String line1 = addrData.getCustNm1();

    // nickname, or occupation
    String line2 = "";

    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
    } else {
      // move occupation up
      line2 = addrData.getAddrTxt2();
    }

    // occupation or PO Box or Phone (for additional shipping)
    String line3 = "";
    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      // nickname specified, so line3 = occupation or pobox or phone
      if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrData.getId().getAddrType())) {
        line3 = addrData.getCustPhone();
      } else {
        if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
          line3 = addrData.getAddrTxt2();
        } else {
          line3 = addrData.getPoBox();
        }
      }
    } else {
      // occupation moved up, this is pobox or phone
      if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addrData.getId().getAddrType())) {
        line3 = addrData.getCustPhone();
      } else {
        line3 = addrData.getPoBox();
      }
    }

    // Street
    String line4 = addrData.getAddrTxt();

    // postal code + city
    String line5 = (!StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "") + " "
        + (!StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "");
    line5 = line5.trim();

    // country
    String line6 = "";

    if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType())) {
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

    int lineNo = 1;
    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    // fixed mapping value, not move up if blank
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }

    // tax office
    messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressT",
        !StringUtils.isEmpty(addrData.getTaxOffice()) ? addrData.getTaxOffice() : "");

    // vat
    if (!MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType())) {
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressU", !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat()
          : "");
    } else {
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressU", "");
    }

    // main phone

    if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addrData.getId().getAddrType())
        || CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrData.getId().getAddrType())) {
      String phone = addrData.getCustPhone();
      if (handler.currentAddresses != null) {
        for (Addr addr : handler.currentAddresses) {
          if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addr.getId().getAddrType())) {
            phone = addr.getCustPhone();
            break;
          }
        }
      }
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", phone);
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "Phone", phone);
    } else {
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Phone", "");
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "Phone", "");
    }

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", crossBorder ? countryName : "");

  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Mail";
    case "ZS01":
      return "Install";
    case "ZD01":
      return "Ship";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Mailing";
    case "ZS01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.GREECE;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "2";
  }

  /**
   * Checks if this is a cross-border scenario
   * 
   * @param addr
   * @return
   */
  protected boolean isCrossBorder(Addr addr) {
    return !"GR".equals(addr.getLandCntry());
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING + MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING + MQMsgConstants.SOF_ADDRESS_USE_SHIPPING + MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

}
