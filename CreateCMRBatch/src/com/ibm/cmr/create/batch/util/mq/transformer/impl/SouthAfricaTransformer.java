/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Arrays;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SouthAfricaTransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(MCOTransformer.class);

  public SouthAfricaTransformer() {
    super(SystemLocation.SOUTH_AFRICA);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    super.formatDataLines(handler);

    Map<String, String> messageHash = handler.messageHash;
    messageHash.put("MarketingResponseCode", "2");
    messageHash.put("CEdivision", "2");
    messageHash.put("CurrencyCode", "SA");

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;

    String addrKey = getAddressKey(addrData.getId().getAddrType());

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    boolean crossBorder = isCrossBorder(addrData);

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String line7 = "";

    // line1 is always customer name
    line1 = addrData.getCustNm1();
    line2 = addrData.getCustNm2();

    String attnPerson = addrData.getCustNm4();
    if (StringUtils.isNotBlank(attnPerson) && !hasAttnPrefix(attnPerson)) {
      attnPerson = "Att: " + attnPerson;
    }

    line3 = attnPerson;
    line4 = addrData.getAddrTxt();

    String streetContPoBox = addrData.getAddrTxt2();
    if (StringUtils.isNotBlank(streetContPoBox)) {
      streetContPoBox += ",PO BOX " + addrData.getPoBox();
    } else {
      streetContPoBox = "PO BOX " + addrData.getPoBox();
    }

    line5 = streetContPoBox;

    String cityPostalCode = addrData.getCity1();
    if (StringUtils.isNotBlank(cityPostalCode)) {
      cityPostalCode += ", " + addrData.getPostCd();
    }

    line6 = cityPostalCode;
    if (crossBorder) {
      line7 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6, line7 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6 + " | " + line7);

    for (String line : lines) {
      if (StringUtils.isNotBlank(line)) {
        messageHash.put(addrKey + "Address" + lineNo, line);
        lineNo++;
      }
    }
  }

  private boolean hasAttnPrefix(String attnPerson) {
    String[] attPersonPrefix = { "Att:", "Att", "Attention Person" };
    boolean isPrefixFound = false;

    for (String prefix : attPersonPrefix) {
      if (!isPrefixFound) {
        if (attnPerson.startsWith(prefix)) {
          isPrefixFound = true;
        }
      }
    }
    return isPrefixFound;
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData South Africa transformer...");

    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData South Africa transformer...");
    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      String custSubGrp = data.getCustSubGrp();
      String[] busPrSubGrp = { "LSBP", "SZBP", "ZABP", "NABP", "ZAXBP", "NAXBP", "LSXBP", "SZXBP" };

      boolean isBusPr = Arrays.asList(busPrSubGrp).contains(custSubGrp);

      if (isBusPr) {
        legacyCust.setAuthRemarketerInd("Y");
      } else {
        legacyCust.setAuthRemarketerInd("N");
      }

      legacyCust.setCeDivision(dummyHandler.messageHash.get("CEdivision"));
      legacyCust.setCurrencyCd(dummyHandler.messageHash.get("CurrencyCode"));
      legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());

    }

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        legacyCust.setTelNoOrVat(addr.getCustPhone());
      }
    }

    legacyCust.setAbbrevNm(data.getAbbrevNm());
    legacyCust.setLangCd("1");
    legacyCust.setMrcCd("2");
    cmrObjects.getData().setUser("");
  }

  @Override
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      legacyCustExt.setTeleCovRep("3100");
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    LOG.debug("Set max and min range For ZA...");
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    String[] interalSubGrp = { "ZAXIN", "NAXIN", "LSXIN", "SZXIN", "ZAINT", "NAINT", "LSINT", "SZINT" };
    boolean isInternal = Arrays.asList(interalSubGrp).contains(custSubGrp);

    if (isInternal) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }
}
