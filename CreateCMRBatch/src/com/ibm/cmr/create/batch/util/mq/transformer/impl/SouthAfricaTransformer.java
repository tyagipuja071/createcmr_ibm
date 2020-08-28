/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Arrays;
import java.util.List;
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
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
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
  private static final String DEFAULT_CLEAR_NUM = "0";

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
    if (StringUtils.isNotBlank(streetContPoBox) && StringUtils.isNotBlank(addrData.getPoBox())) {
      streetContPoBox += ",PO BOX " + addrData.getPoBox();
    } else if (StringUtils.isBlank(streetContPoBox) && StringUtils.isNotBlank(addrData.getPoBox())) {
      streetContPoBox = "PO BOX " + addrData.getPoBox();
    }

    line5 = streetContPoBox;

    String cityPostalCode = addrData.getCity1();
    if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(addrData.getPostCd())) {
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
    formatAddressLines(dummyHandler);
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }

    String poBox = currAddr.getPoBox();
    if (!StringUtils.isEmpty(poBox)) {
      if (!poBox.startsWith("PO BOX ")) {
        legacyAddr.setPoBox("PO BOX " + currAddr.getPoBox());
      } else {
        legacyAddr.setPoBox(poBox);
      }
    }
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData South Africa transformer...");
    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    formatDataLines(dummyHandler);
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      String custSubGrp = data.getCustSubGrp();
      String[] busPrSubGrp = { "LSBP", "SZBP", "ZABP", "NABP", "ZAXBP", "NAXBP", "LSXBP", "SZXBP" };

      boolean isBusPr = Arrays.asList(busPrSubGrp).contains(custSubGrp);

      if (isBusPr) {
        legacyCust.setAuthRemarketerInd("1");
      } else {
        legacyCust.setAuthRemarketerInd("0");
      }

      legacyCust.setCeDivision("2");
      legacyCust.setCurrencyCd("SA");

    }

    if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      legacyCust.setIbo(data.getSalesBusOffCd());
      legacyCust.setSbo(data.getSalesBusOffCd());
    } else {
      legacyCust.setIbo("");
      legacyCust.setSbo("");
    }

    if (!StringUtils.isBlank(data.getSalesTeamCd())) {
      legacyCust.setSalesGroupRep(data.getSalesTeamCd());
    } else {
      legacyCust.setSalesGroupRep("");
    }

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        legacyCust.setTelNoOrVat(addr.getCustPhone());
      }
    }

    if (!StringUtils.isBlank(data.getIbmDeptCostCenter())) {
      if (data.getIbmDeptCostCenter().length() == 6)
        legacyCust.setDeptCd(data.getIbmDeptCostCenter().substring(2, 6));
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      legacyCust.setModeOfPayment(data.getCommercialFinanced());
      if (data.getCodCondition() != null) {
        String cod = data.getCodCondition();
        if (cod == "Y") {
          legacyCust.setModeOfPayment("5");
        } else {
          legacyCust.setModeOfPayment("");
        }
      }
    }

    legacyCust.setAbbrevNm(data.getAbbrevNm());
    legacyCust.setLangCd("1");
    legacyCust.setMrcCd("2");
    legacyCust.setCustType(data.getCrosSubTyp());
    legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
    // cmrObjects.getData().setUser("");
  }

  @Override
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      legacyCustExt.setTeleCovRep("3100");
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      legacyCustExt.setTeleCovRep(!StringUtils.isEmpty(data.getCollBoId()) ? data.getCollBoId() : "");
    }
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr muAddr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {

    LegacyCommonUtil.transformBasicLegacyAddressMassUpdate(entityManager, legacyAddr, muAddr, cntry, cust, data);

    if (!StringUtils.isBlank(muAddr.getPostCd())) {
      legacyAddr.setZipCode(muAddr.getPostCd());
    }

    if (!StringUtils.isBlank(muAddr.getCounty())) {
      if (muAddr.getId().getAddrType().equals("ZD01")) {
        if (DEFAULT_CLEAR_NUM.equals(muAddr.getCounty().trim())) {
          legacyAddr.setAddrPhone("");
        } else {
          legacyAddr.setAddrPhone(muAddr.getCounty());
        }
      }
    }
    formatMassUpdateAddressLines(entityManager, legacyAddr, muAddr, false);
    legacyObjects.addAddress(legacyAddr);
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust legacyCust, CMRRequestContainer cmrObjects,
      MassUpdtData muData) {
    LOG.debug("ZA >> Mapping default Data values..");
    LegacyCommonUtil.setlegacyCustDataMassUpdtFields(entityManager, legacyCust, muData);

    List<MassUpdtAddr> muaList = cmrObjects.getMassUpdateAddresses();
    if (muaList != null && muaList.size() > 0) {
      for (MassUpdtAddr mua : muaList) {
        if ("ZS01".equals(mua.getId().getAddrType())) {
          if (!StringUtils.isBlank(mua.getCounty())) {
            if (DEFAULT_CLEAR_NUM.equals(mua.getCounty().trim())) {
              legacyCust.setTelNoOrVat("");
            } else {
              legacyCust.setTelNoOrVat(mua.getCounty());
            }
          }
          break;
        }
      }
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
