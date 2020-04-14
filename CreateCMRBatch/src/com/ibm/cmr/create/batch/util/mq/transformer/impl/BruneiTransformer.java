package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class BruneiTransformer extends ASEANTransformer {

  public BruneiTransformer() throws Exception {
    super(SystemLocation.BRUNEI);

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
    String line6 = "Brunei ";

    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }
    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ASEAN");
    // only for local
    if (StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && addrData.getLandCntry() != null
        && "BN".equalsIgnoreCase(addrData.getLandCntry())) {
      String lineSix = "Brunei ";
      if (!StringUtils.isBlank(addrData.getPostCd())) {
        lineSix += addrData.getPostCd();
      }
      handler.messageHash.put("AddrLine6", lineSix);
      String line5 = "";
      if (!StringUtils.isBlank(addrData.getDept())) {
        line5 += addrData.getDept();
      }

      handler.messageHash.put("AddrLine5", line5);
    }
    arrangeAddressLinesData(handler);
  }

}
