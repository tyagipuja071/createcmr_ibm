/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * Base {@link MessageTransformer} class that handles the following countries:
 * <ul>
 * <li>616 - Australia</li>
 * <li>796 - New Zealand</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class ANZTransformer extends APTransformer {

  public ANZTransformer(String cmrIssuingCntry) throws Exception {
    super(cmrIssuingCntry);
  }

  @Override
  protected void handleDataDefaults(MQMessageHandler handler) {
    super.handleDataDefaults(handler);

    handler.messageHash.put("SellBrnchOff", "000");
    handler.messageHash.put("SellDept", "0000");
    handler.messageHash.put("InstBrnchOff", "000");
    handler.messageHash.put("InstDept", "");

    String isu = handler.cmrData.getIsuCd();
    if ("32".equalsIgnoreCase(isu) || "34".equalsIgnoreCase(isu) || "21".equalsIgnoreCase(isu)) {
      handler.messageHash.put("MrktRespCode", "3");
    } else {
      handler.messageHash.put("MrktRespCode", "2");
    }
    String clusterCd = handler.cmrData.getApCustClusterId();
    String cmrIssuingCntry = handler.cmrData.getCmrIssuingCntry();
    List<String> clusterList = new ArrayList<String>();
    List<String> countryList = new ArrayList<String>();
    Collections.addAll(countryList, "616", "796");
    Collections.addAll(clusterList, "01147", "71101", "08037", "00002", "09056", "04694", "01150", "71100", "00001", "08039", "09057", "00035",
        "05221", "00105", "04485", "04500", "04746", "04744", "04745", "05222", "00001");
    if (!StringUtils.isBlank(clusterCd) && clusterList.contains(clusterCd) && countryList.contains(cmrIssuingCntry)) {
      handler.messageHash.put("MrktRespCode", handler.cmrData.getMrcCd());
    }

    String clusterID = handler.cmrData.getApCustClusterId();
    if (clusterID.contains("BLAN")) {
      handler.messageHash.put("ClusterNo", "");
    } else {
      handler.messageHash.put("ClusterNo", clusterID);
    }

    String gb_SegCode = handler.cmrData.getClientTier();
    if ("0".equalsIgnoreCase(handler.cmrData.getClientTier())) {
      handler.messageHash.put("GB_SegCode", "");
    } else {
      handler.messageHash.put("GB_SegCode", gb_SegCode);
    }

  }

  @Override
  protected void handleAddressDefaults(MQMessageHandler handler) {
    super.handleAddressDefaults(handler);

    /*
     * Addr addrData = handler.addrData;
     * 
     * String line3 = ""; if (!StringUtils.isBlank(addrData.getDept()) &&
     * StringUtils.isBlank(addrData.getAddrTxt2())) { line3 +=
     * addrData.getDept(); } if (StringUtils.isBlank(addrData.getDept()) &&
     * !StringUtils.isBlank(addrData.getAddrTxt2())) { line3 +=
     * addrData.getAddrTxt2(); } if (!StringUtils.isBlank(addrData.getDept()) &&
     * !StringUtils.isBlank(addrData.getAddrTxt2())) { line3 +=
     * addrData.getDept(); }
     * 
     * String line4 = ""; if (!StringUtils.isBlank(addrData.getAddrTxt())) {
     * line4 += addrData.getAddrTxt(); }
     * 
     * handler.messageHash.put("AddrLine3", line3);
     * handler.messageHash.put("AddrLine4", line4);
     */
  }
}
