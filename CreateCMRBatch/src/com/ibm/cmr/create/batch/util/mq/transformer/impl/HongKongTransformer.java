package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang.StringUtils;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class HongKongTransformer extends GCGTransformer {

  public HongKongTransformer() throws Exception {
    super(SystemLocation.HONG_KONG);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    handleDataDefaults(handler);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    handleAddressDefaults(handler);
    Addr addrData = handler.addrData;
    
    String line5 = "";
    if (!StringUtils.isBlank(addrData.getCity1())) {
    	line5 += addrData.getCity1();
    }
    handler.messageHash.put("AddrLine5", line5);
    handler.messageHash.put("AddrLine6", "Hong Kong");
    
    String line66 = "";
    if (addrData.getLandCntry() != null && !addrData.getLandCntry().equalsIgnoreCase(convertIssuing2Cd(handler.cmrData.getCmrIssuingCntry()))) {
      line66 = "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";
      
      if (!StringUtils.isBlank(addrData.getPostCd())) {
        line66 += " " + addrData.getPostCd();
      }
      handler.messageHash.put("AddrLine6", line66);
    }
    
    String city1 = addrData.getCity1();
    if (city1 == null || (handler.messageHash.get("AddrLine5").length() == 0)) {
      handler.messageHash.put("AddrLine5", handler.messageHash.get("AddrLine6"));
      handler.messageHash.put("AddrLine6", "");
    }

    arrangeAddressLinesData(handler);
    /*
     * if (addrData.getLandCntry() != null &&
     * !addrData.getLandCntry().equalsIgnoreCase
     * (convertIssuing2Cd(handler.cmrData.getCmrIssuingCntry()))) { String
     * line66 = "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) +
     * ">"; if (addrData.getPostCd() != null) line66 += " " +
     * addrData.getPostCd(); handler.messageHash.put("AddrLine6", line66); }
     */
  }
}
