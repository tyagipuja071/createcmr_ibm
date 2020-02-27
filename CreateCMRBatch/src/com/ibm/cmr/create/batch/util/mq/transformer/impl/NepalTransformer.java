package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class NepalTransformer extends ISATransformer {

  public NepalTransformer() throws Exception {
    super(SystemLocation.NEPAL);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    handleDataDefaults(handler);
    handler.messageHash.put("CntryNo", SystemLocation.BANGLADESH);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    handleAddressDefaults(handler);
    Addr addrData = handler.addrData;

    String line6 = "<Nepal> ";

    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }

    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ISA");
  }

}
