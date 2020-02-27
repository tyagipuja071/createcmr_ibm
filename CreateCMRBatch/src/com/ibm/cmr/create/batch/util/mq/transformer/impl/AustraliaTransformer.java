/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class AustraliaTransformer extends ANZTransformer {

  public AustraliaTransformer() throws Exception {
    super(SystemLocation.AUSTRALIA);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    handleAddressDefaults(handler);
    handler.messageHash.put("AddrUseCA", computeAddressUse(handler));

    Addr addrData = handler.addrData;

    String line2 = "";
    if (StringUtils.isBlank(addrData.getCustNm2()) || !StringUtils.isBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
    }

    String line3 = "";
    if (!StringUtils.isBlank(addrData.getDept())) {
      line3 += "ATTN:" + addrData.getDept();
    }

    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 += addrData.getAddrTxt();
    }

    String line5 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line5 += addrData.getAddrTxt2();
    }
    handler.messageHash.put("AddrLine2", line2);
    handler.messageHash.put("AddrLine3", line3);
    handler.messageHash.put("AddrLine4", line4);
    handler.messageHash.put("AddrLine5", line5);

    String line6 = "";

    String state = addrData.getStateProv();
    if (StringUtils.isEmpty(state)) {
      state = "";
    }
    if (state.length() > 3) {
      state = state.substring(0, 3);
    }
    String postCd = addrData.getPostCd();
    if (StringUtils.isEmpty(postCd)) {
      postCd = "";
    }
    if ("AU".equals(addrData.getLandCntry())) {
      String city = addrData.getCity1();
      if (StringUtils.isEmpty(city)) {
        city = "";
      }
      if (city.length() > 20) {
        city = city.substring(0, 20);
      }

      line6 = StringUtils.rightPad(city.toString().trim(), 20, ' ');
    } else {
      line6 = "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";
      line6 = StringUtils.rightPad(line6, 20, ' ');
    }

    line6 += state;
    line6 = StringUtils.rightPad(line6, 24, ' ');
    line6 += postCd;

    handler.messageHash.put("AddrLine6", line6);

    if (handler.messageHash.get("AddrLine3").length() == 0) {
      handler.messageHash.put("AddrLine3", handler.messageHash.get("AddrLine4"));
      handler.messageHash.put("AddrLine4", handler.messageHash.get("AddrLine5"));
      handler.messageHash.put("AddrLine5", handler.messageHash.get("AddrLine6"));
      handler.messageHash.put("AddrLine6", "");
    }
    if (handler.messageHash.get("AddrLine5").length() == 0) {
      handler.messageHash.put("AddrLine5", handler.messageHash.get("AddrLine6"));
      handler.messageHash.put("AddrLine6", "");
    }
    if (handler.messageHash.get("AddrLine4").length() == 0) {
      handler.messageHash.put("AddrLine4", handler.messageHash.get("AddrLine5"));
      handler.messageHash.put("AddrLine5", "");
      handler.messageHash.put("AddrLine6", "");
    }

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    handleDataDefaults(handler);

    Map<String, String> messageHash = handler.messageHash;
    String coGrp = "G";
    String sitCode = "0";
    String collBroff = "00";// Create CMR field. Apply 2char Collection Branch
                            // Office.
    String stmtInd = "0";
    String dpmSrceCode = "SWH";
    String spaceBeforeABN = "   ";

    Data cmrData = handler.cmrData;
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    String custType = cmrData.getCustGrp();
    String stateProv = addrData.getStateProv();
    if (!update) {
      if (custType.contains("LOCAL") && stateProv.contains("NSW")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "101");
      } else if (custType.contains("LOCAL") && stateProv.contains("SA")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "220");
      } else if (custType.contains("LOCAL") && stateProv.contains("ACT")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "102");
      } else if (custType.contains("LOCAL") && stateProv.contains("VIC")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "215");
      } else if (custType.contains("LOCAL") && stateProv.contains("WA")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "102");
      } else if (custType.contains("LOCAL") && stateProv.contains("TAS")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "215F");
      } else if (custType.contains("LOCAL") && stateProv.contains("QLD")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "130");
      } else if (custType.contains("LOCAL") && stateProv.contains("NT")) {
        handler.messageHash.put("EngrBrnchOff", stateProv + "125");
      } else if (custType.contains("CROSS")) {
        handler.messageHash.put("EngrBrnchOff", "101");
      }
    }
    String vat = cmrData.getVat();
    String abnNo = "";
    if (!StringUtils.isBlank(vat))
      abnNo = cmrData.getVat().substring(2);

    String ctryText = coGrp + sitCode + collBroff + stmtInd + dpmSrceCode + spaceBeforeABN + abnNo;

    messageHash.put("CtryText", ctryText);
    // messageHash.put("EngrBrnchOff", ctryText);
    messageHash.put("EngrDept", "");
    messageHash.put("IBMCode", cmrData.getCollectionCd());
    // messageHash.put("IBMCode", "0000");
    messageHash.put("RegionCode", "");

  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "07";
  }

  @Override
  public String[] getAddressOrder() {
    return new String[] { "ZS01", "ZP01", "ZI01", "ZF01", "MAIL", "CTYG", "CTYH", "EDUC", "PUBB", "PUBS", "STAT" };
  }

  @Override
  protected String getMainAddressUseCA() {
    return "123567ABDGH";
  }

}
