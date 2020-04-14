package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class CambodiaTransformer extends ASEANTransformer {

  public CambodiaTransformer() throws Exception {
    super(SystemLocation.CAMBODIA);

  }

  @Override
  protected String getDoubleCreateCountry(EntityManager entityManager, MQMessageHandler handler) {
    if (handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM))
      if (!handler.cmrData.getCustSubGrp().equals("BLUMX") && !handler.cmrData.getCustSubGrp().equals("MKTPC"))
        return SystemLocation.SINGAPORE;
    return "";
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    handleDataDefaults(handler);

    handler.messageHash.put("CntryNo", SystemLocation.CAMBODIA);
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM)
        && handler.addrData.getLandCntry().equals(SystemLocation.CAMBODIA)) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
      handler.messageHash.put("CustomerNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("IBMCode", "V001");
      handler.messageHash.put("SalesmanNo", "000DSW");
      handler.messageHash.put("ProvinceCode", "000");
    }
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
    handler.messageHash.put("AddrUseCA", getAddressUse(null));

    Addr addrData = handler.addrData;

    String line6 = "Cambodia ";
    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }
    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ASEAN");
    arrangeAddressLinesData(handler);
  }

}
