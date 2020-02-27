package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class MyanmarTransformer extends ASEANTransformer {

  public MyanmarTransformer() throws Exception {
    super(SystemLocation.MYANMAR);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    handleDataDefaults(handler);

    Data cmrData = handler.cmrData;
    if (!MQMsgConstants.CUSTGRP_BLUEMIX.equalsIgnoreCase(cmrData.getCustGrp())
        && !MQMsgConstants.CUSTGRP_MKPLA.equalsIgnoreCase(cmrData.getCustGrp()))
      handler.messageHash.put("CntryNo", SystemLocation.THAILAND);

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

    String line6 = "<Myanmar> ";
    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }

    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ASEAN");
  }

}
