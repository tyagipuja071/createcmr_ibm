package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * Base {@link MessageTransformer} class that handles the following countries:
 * <ul>
 * <li>643 - Brunei</li>
 * <li>749 - Indonesia</li>
 * <li>714 - Laos</li>
 * <li>720 - Cambodia</li>
 * <li>646 - Myanmar</li>
 * <li>778 - Malaysia</li>
 * <li>818 - Philippines</li>
 * <li>834 - Singapore</li>
 * <li>856 - Thailand</li>
 * <li>852 - Vietnam</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class ASEANTransformer extends APTransformer {

  public ASEANTransformer(String cmrIssuingCntry) throws Exception {
    super(cmrIssuingCntry);

  }

  @Override
  protected void handleDataDefaults(MQMessageHandler handler) {
    super.handleDataDefaults(handler);
    handler.messageHash.put("RegionCode", handler.cmrData.getMiscBillCd());
    if (MQMsgConstants.CUSTGRP_BLUEMIX.equalsIgnoreCase(handler.cmrData.getCustGrp())
        || MQMsgConstants.CUSTGRP_MKPLA.equalsIgnoreCase(handler.cmrData.getCustGrp())) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
    }
    handler.messageHash.put("CtryText", handler.cmrData.getVat());

    if ("0".equalsIgnoreCase(handler.cmrData.getClientTier())) {
      handler.messageHash.put("GB_SegCode", "");
    }
    String isu = handler.cmrData.getIsuCd();
    if ("32".equalsIgnoreCase(isu) || "34".equalsIgnoreCase(isu) || "21".equalsIgnoreCase(isu))
      handler.messageHash.put("MrktRespCode", "3");
    else
      handler.messageHash.put("MrktRespCode", "2");
    setAbbLoc(handler);
  }

  @Override
  protected void handleAddressDefaults(MQMessageHandler handler) {
    super.handleAddressDefaults(handler);
    if (MQMsgConstants.CUSTGRP_BLUEMIX.equalsIgnoreCase(handler.cmrData.getCustGrp())
        || MQMsgConstants.CUSTGRP_MKPLA.equalsIgnoreCase(handler.cmrData.getCustGrp())) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
    }
    handler.messageHash.put("AddrUseCA", computeAddressUse(handler));
    Addr addrData = handler.addrData;
    String line3 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line3 += addrData.getAddrTxt();
    }

    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line4 += addrData.getAddrTxt2();
    }
    String line5 = "";
    if (!StringUtils.isBlank(addrData.getDept())) {
      line5 += addrData.getDept();
    }

    handler.messageHash.put("AddrLine3", line3);
    handler.messageHash.put("AddrLine4", line4);
    handler.messageHash.put("AddrLine5", line5);
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "AA";
  }

  @Override
  public String[] getAddressOrder() {
    return new String[] { "ZS01", "ZP01", "ZI01", "ZH01" };
  }

  @Override
  protected String getMainAddressUseCA() {
    return "1234567ABCDEFGH";
  }

}
