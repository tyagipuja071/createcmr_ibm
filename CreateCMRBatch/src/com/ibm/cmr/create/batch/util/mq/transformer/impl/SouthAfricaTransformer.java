/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
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

    // line1 is always customer name
    line1 = addrData.getCustNm1();

    // 1453939 - added nameCont for ZA
    boolean hasNameCont = !StringUtils.isEmpty(addrData.getCustNm2()) ? true : false;

    if (!crossBorder) {
      // Domestic - ZA
      // line2 = Customer Name Con’t
      // line3 = Attention Person + Phone (Phone for Shipping & EPL only)
      // line4 = Street + PO BOX
      // line5 = City
      // line6 = Postal Code
      // OR
      // line2 = Attention Person + Phone (Phone for Shipping & EPL only)
      // line3 = Street
      // line4 = Street Con't + PO BOX
      // line5 = City
      // line6 = Postal Code

      String attLine = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4() : "";
      if (MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType()) || MQMsgConstants.ADDR_ZS02.equals(addrData.getId().getAddrType())) {
        if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          attLine = !StringUtils.isEmpty(attLine) ? (attLine + " " + addrData.getCustPhone()) : addrData.getCustPhone();
        }
      }

      String strLine = !StringUtils.isEmpty(addrData.getAddrTxt2()) ? addrData.getAddrTxt2() : "";
      if (hasNameCont) {
        strLine = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
      }
      if (!StringUtils.isEmpty(strLine) && !StringUtils.isEmpty(addrData.getPoBox())) {
        strLine += ", " + addrData.getPoBox();
      } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
        strLine = addrData.getPoBox();
      }

      // set address lines
      if (hasNameCont) {
        line2 = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
        line3 = attLine;
      } else {
        line2 = attLine;
        line3 = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
      }
      line4 = strLine;
      line5 = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
      line6 = !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "";

    } else {
      // Cross-border - ZA
      // line2 = Customer Name Con’t
      // line3 = Attention Person + Phone (Phone for Shipping & EPL only)
      // line4 = Street + PO BOX
      // line5 = City
      // line6 = State (Country)
      // OR
      // line2 = Attention Person + Phone (Phone for Shipping & EPL only)
      // line3 = Street + PO BOX
      // line4 = City
      // line5 = Postal Code
      // line6 = State (Country)

      String attLine = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4() : "";
      if (MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType()) || MQMsgConstants.ADDR_ZS02.equals(addrData.getId().getAddrType())) {
        if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          attLine = !StringUtils.isEmpty(attLine) ? (attLine + " " + addrData.getCustPhone()) : addrData.getCustPhone();
        }
      }

      String strLine = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";
      if (!StringUtils.isEmpty(strLine) && !StringUtils.isEmpty(addrData.getPoBox())) {
        strLine += ", " + addrData.getPoBox();
      } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
        strLine = addrData.getPoBox();
      }

      // set address lines
      if (hasNameCont) {
        line2 = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
        line3 = attLine;
        line4 = strLine;
        line5 = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
      } else {
        line2 = attLine;
        line3 = strLine;
        line4 = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
        line5 = !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "";
      }

      // 1453939 - move country to line5 if line5 is blank
      if (StringUtils.isEmpty(line5)) {
        line5 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      } else {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }

  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    cmrObjects.getData().setUser("");
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For ZA...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "XINTR".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }
}
