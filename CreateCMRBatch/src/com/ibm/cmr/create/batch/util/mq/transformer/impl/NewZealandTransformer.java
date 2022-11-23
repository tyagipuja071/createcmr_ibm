package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class NewZealandTransformer extends ANZTransformer {

  private static final Logger LOG = Logger.getLogger(NewZealandTransformer.class);

  public NewZealandTransformer() throws Exception {
    super(SystemLocation.NEW_ZEALAND);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    handleDataDefaults(handler);

    Map<String, String> messageHash = handler.messageHash;
    messageHash.put("EngrDept", "0");
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    handleAddressDefaults(handler);
    handler.messageHash.put("AddrUseCA", computeAddressUse(handler));

    Addr addrData = handler.addrData;
    String addrTyp = addrData.getId().getAddrType();

    if (!"MAIL".equals(addrTyp) && !"XXXX".equals(addrTyp)) {

      handler.messageHash.remove("AddrLine5");
      handler.messageHash.remove("AddrLine6");

      String line1 = "";
      if (!StringUtils.isBlank(addrData.getDept())) {
        line1 += "ATTN:" + addrData.getDept();
      }

      String line2 = "";
      if (!StringUtils.isBlank(addrData.getAddrTxt()) && !StringUtils.isBlank(addrData.getAddrTxt2())) {
        line2 += "AT " + addrData.getAddrTxt();
      } else if (!StringUtils.isBlank(addrData.getAddrTxt())) {
        line2 += addrData.getAddrTxt();
      }

      String line3 = "";
      if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
        line3 += addrData.getAddrTxt2();
      }
      handler.messageHash.put("AddrLine1", line1);
      handler.messageHash.put("AddrLine2", line2);
      handler.messageHash.put("AddrLine3", line3);

      String line4 = "";
      String postCd = addrData.getPostCd();
      if (StringUtils.isEmpty(postCd)) {
        postCd = "";
      }
      if ("NZ".equals(addrData.getLandCntry())) {
        String city = addrData.getCity1();
        if (StringUtils.isEmpty(city)) {
          city = "";
        }
        if (city.length() > 24) {
          city = city.substring(0, 24);
        }
        line4 = StringUtils.rightPad(city.toString().trim(), 24, ' ');
      } else {
        List<String> abbrevLandCountries = new ArrayList<String>();
        abbrevLandCountries = (List<String>) Arrays.asList("US", "MY", "GB", "SG", "AE", "CH", "MV", "AU", "FR", "TH", "NL", "IE", "HK", "DE", "ID",
            "BD", "CA", "LK", "TW", "NZ", "VN", "PH", "KR", "MM", "KH", "BN", "PG");
        String scenario = handler.cmrData.getCustSubGrp();
        boolean update = "U".equals(handler.adminData.getReqType());
        line4 = addrData.getCity1();
        if (addrData.getLandCntry() != null && !addrData.getLandCntry().equalsIgnoreCase(convertIssuing2Cd(handler.cmrData.getCmrIssuingCntry()))) {
          if (!update && abbrevLandCountries.contains(addrData.getLandCntry()) && scenario.equals("CROSS"))
            line4 += " " + "<" + addrData.getLandCntry() + ">";
          else
            line4 += " " + "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";
        }
      }

      line4 = StringUtils.rightPad(line4, 24, ' ');
      line4 += postCd;

      handler.messageHash.put("AddrLine4", line4);

      // move up and remove blank links
      List<String> lines = new ArrayList<>();
      String line = null;
      for (int i = 1; i <= 6; i++) {
        line = handler.messageHash.get("AddrLine" + i);
        if (!StringUtils.isBlank(line)) {
          lines.add(line);
        }
      }
      for (int i = 1; i <= 6; i++) {
        line = lines.size() >= i ? lines.get(i - 1) : "";
        handler.messageHash.put("AddrLine" + i, line);
      }

      if (handler.messageHash.get("AddrLine1").length() == 0) {
        handler.messageHash.put("AddrLine1", handler.messageHash.get("AddrLine2"));
        handler.messageHash.put("AddrLine2", handler.messageHash.get("AddrLine3"));
        handler.messageHash.put("AddrLine3", handler.messageHash.get("AddrLine4"));
      }

      if (handler.messageHash.get("AddrLine1").length() == 0 && handler.messageHash.get("AddrLine3").length() == 0) {
        handler.messageHash.put("AddrLine2", handler.messageHash.get("AddrLine4"));
      }
    } else if (("MAIL").equals(addrTyp)) {
      formatMailingAddress(handler, addrData);
    } else if ("XXXX".equals(addrTyp)) {
      // this is the dummy address. if mailing exists, just get from mailing
      // if not exists, format from installing
      Addr installing = null;
      Addr mailing = null;
      boolean hasMailing = false;
      String type = null;
      for (Addr addr : handler.currentAddresses) {
        type = addr.getId().getAddrType();
        if ("MAIL".equals(type)) {
          hasMailing = true;
          mailing = addr;
        } else if ("ZS01".equals(type)) {
          installing = addr;
        }
      }
      if (hasMailing) {
        LOG.debug("Formatting dummy mail from MAILING");
        formatMailingAddress(handler, mailing);
      } else {
        LOG.debug("Formatting dummy mail from INSTALLING");
        formatMailingAddress(handler, installing);
      }
    }
  }

  private void formatMailingAddress(MQMessageHandler handler, Addr addrData) {
    String line1 = "";
    if (!StringUtils.isBlank(addrData.getCustNm1())) {
      line1 += addrData.getCustNm1();
    }

    String line2 = "";
    if (!StringUtils.isBlank(addrData.getCustNm2()) || StringUtils.isBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
    }

    String line3 = "";
    if (!StringUtils.isBlank(addrData.getDept())) {
      line3 += "ATTN:" + addrData.getDept();
    }

    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt()) && !StringUtils.isBlank(addrData.getAddrTxt2())) {
      line4 += "AT " + addrData.getAddrTxt();
    } else if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 += addrData.getAddrTxt();
    }

    String line5 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line5 += addrData.getAddrTxt2();
    }
    handler.messageHash.put("AddrLine1", line1);
    handler.messageHash.put("AddrLine2", line2);
    handler.messageHash.put("AddrLine3", line3);
    handler.messageHash.put("AddrLine4", line4);
    handler.messageHash.put("AddrLine5", line5);

    String line6 = "";
    String postCd = addrData.getPostCd();
    if (StringUtils.isEmpty(postCd)) {
      postCd = "";
    }
    if ("NZ".equals(addrData.getLandCntry())) {
      String city = addrData.getCity1();
      if (StringUtils.isEmpty(city)) {
        city = "";
      }
      if (city.length() > 24) {
        city = city.substring(0, 24);
      }
      line6 = StringUtils.rightPad(city.toString().trim(), 24, ' ');
    } else {
      List<String> abbrevLandCountries = new ArrayList<String>();
      abbrevLandCountries = (List<String>) Arrays.asList("US", "MY", "GB", "SG", "AE", "CH", "MV", "AU", "FR", "TH", "NL", "IE", "HK", "DE", "ID",
          "BD", "CA", "LK", "TW", "NZ", "VN", "PH", "KR", "MM", "KH", "BN", "PG");
      String scenario = handler.cmrData.getCustSubGrp();
      line6 = addrData.getCity1();
      if (addrData.getLandCntry() != null && !addrData.getLandCntry().equalsIgnoreCase(convertIssuing2Cd(handler.cmrData.getCmrIssuingCntry()))) {
        if (abbrevLandCountries.contains(addrData.getLandCntry()) && scenario.equals("CROSS"))
          line6 += " " + "<" + addrData.getLandCntry() + ">";
        else
          line6 += " " + "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";
      }
      line6 = StringUtils.rightPad(line6, 24, ' ');
    }

    line6 = StringUtils.rightPad(line6, 24, ' ');
    line6 += postCd;

    handler.messageHash.put("AddrLine6", line6);

    // move up and remove blank links
    List<String> lines = new ArrayList<>();
    String line = null;
    for (int i = 1; i <= 6; i++) {
      line = handler.messageHash.get("AddrLine" + i);
      if (!StringUtils.isBlank(line)) {
        lines.add(line);
      }
    }
    for (int i = 1; i <= 6; i++) {
      line = lines.size() >= i ? lines.get(i - 1) : "";
      handler.messageHash.put("AddrLine" + i, line);
    }

    if (handler.messageHash.get("AddrLine3").length() == 0) {
      handler.messageHash.put("AddrLine3", handler.messageHash.get("AddrLine4"));
      handler.messageHash.put("AddrLine4", handler.messageHash.get("AddrLine5"));
      handler.messageHash.put("AddrLine5", handler.messageHash.get("AddrLine6"));
    }

    if (handler.messageHash.get("AddrLine2").length() == 0 && handler.messageHash.get("AddrLine3").length() == 0) {
      handler.messageHash.put("AddrLine2", handler.messageHash.get("AddrLine4"));
      handler.messageHash.put("AddrLine3", handler.messageHash.get("AddrLine5"));
      handler.messageHash.put("AddrLine4", handler.messageHash.get("AddrLine6"));
    }

    if (handler.messageHash.get("AddrLine2").length() == 0 && handler.messageHash.get("AddrLine3").length() == 0
        && handler.messageHash.get("AddrLine5").length() == 0) {
      handler.messageHash.put("AddrLine3", handler.messageHash.get("AddrLine6"));
    }
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "02";
  }

  @Override
  public String[] getAddressOrder() {
    return new String[] { "ZS01", "ZP01", "ZI01", "MAIL", "XXXX" };
  }

  @Override
  protected String getMainAddressUseCA() {
    return "1234";
  }
}
