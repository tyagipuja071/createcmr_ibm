package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class MalaysiaTransformer extends ASEANTransformer {

  public MalaysiaTransformer() throws Exception {
    super(SystemLocation.MALAYSIA);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    handleDataDefaults(handler);
    // String custSubGrp = handler.cmrData.getCustSubGrp();
    // String mrcCode = "";
    // if (handler.cmrData.getCustSubGrp() != null) {
    // if ("DUMMY".equalsIgnoreCase(custSubGrp) ||
    // "INTERNAL".equalsIgnoreCase(custSubGrp))
    // mrcCode = "3";
    // }
    //
    // handler.messageHash.put("MrktRespCode", mrcCode);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    handleAddressDefaults(handler);
    Addr addrData = handler.addrData;

    String line6 = "";
    if (!StringUtils.isBlank(addrData.getCity1())) {
      // line6 += LandedCountryMap.getStateDescription(addrData.getStateProv());
      line6 += addrData.getCity1();
    }
    line6 += " ";
    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }

    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ASEAN");
    arrangeAddressLinesData(handler);
  }

}
