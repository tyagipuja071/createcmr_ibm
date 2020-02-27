/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * Base EMEA transformer
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class EMEATransformer extends MessageTransformer {

  public EMEATransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  /**
   * Handles default values for EMEA countries
   * 
   * @param handler
   * @param messageHash
   * @param cmrData
   * @param addrData
   * @param crossBorder
   */
  protected void handleEMEADefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, Addr addrData, boolean crossBorder) {
    if (StringUtils.isBlank(cmrData.getIsuCd()) || "21".equals(cmrData.getIsuCd())) {
      // unassigned ISU + Client Tier
      // updated to 217 as per defect 1313016
      messageHash.put("ISU", "217");
    }

    // no district code for all
    messageHash.put("DistrictCode", "");

    // clear embargo code for now
    if (handler.doc_num != null && handler.doc_num.equalsIgnoreCase(MQMsgConstants.XML_DOCUMENTNUMBER_1))
      messageHash.put("EmbargoCode", "");
    // Only populate FSLICAM with data if the request is for the FSL scenario

    if (!MQMsgConstants.CUSTSUBGRP_INFSL.equals(cmrData.getCustSubGrp())) {
      messageHash.put("FSLICAM", "");
    }
  }
}
