package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.impl.APHandler;
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
    APHandler aphandler = (APHandler) RequestUtils.getGEOHandler(handler.cmrData.getCmrIssuingCntry());
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

    String clusterCd = handler.cmrData.getApCustClusterId();
    String cmrIssuingCntry = handler.cmrData.getCmrIssuingCntry();
    List<String> clusterList = new ArrayList<String>();
    List<String> countryList = new ArrayList<String>();
    Collections.addAll(countryList, "643", "749", "778", "818", "834", "852", "856");
    Collections.addAll(clusterList, "00000", "04462", "05219", "04483", "05220", "01211", "01241", "01231", "01222", "01251", "01277", "01273",
        "09050", "09051", "09052", "09053", "09054", "09055", "08042", "08040", "08038", "08044", "08047", "08046");
    if (!StringUtils.isBlank(clusterCd) && clusterList.contains(clusterCd) && countryList.contains(cmrIssuingCntry)) {
      handler.messageHash.put("MrktRespCode", handler.cmrData.getMrcCd());
    }

    if ("5K".equalsIgnoreCase(isu)) {
      handler.messageHash.put("ISU", "5K7");
    }
    String clusterID = handler.cmrData.getApCustClusterId();
    if (clusterID != null && clusterID.contains("BLAN")) {
      handler.messageHash.put("ClusterNo", "");
    } else {
      handler.messageHash.put("ClusterNo", clusterID);
    }
    // Handling obsolete data
    DataRdc oldDataRdc = aphandler.getAPClusterDataRdc(handler.cmrData.getId().getReqId());
    String reqType = handler.adminData.getReqType();
    if (StringUtils.equalsIgnoreCase(reqType, "U")) {
      if (StringUtils.isBlank(handler.cmrData.getApCustClusterId())) {
        handler.messageHash.put("ClusterNo", oldDataRdc.getApCustClusterId());
      }
      if (StringUtils.isBlank(handler.cmrData.getClientTier())) {
        handler.messageHash.put("GB_SegCode", oldDataRdc.getClientTier());
      }
      if (StringUtils.isBlank(handler.cmrData.getIsuCd())) {
        handler.messageHash.put("ISU", oldDataRdc.getIsuCd());
      }
      if (StringUtils.isBlank(handler.cmrData.getInacType())) {
        handler.messageHash.put("inacType", oldDataRdc.getInacType());
      }
      if (StringUtils.isBlank(handler.cmrData.getInacCd())) {
        handler.messageHash.put("inacCd", oldDataRdc.getInacCd());
      }
      if (StringUtils.isBlank(handler.cmrData.getCollectionCd())) {
        handler.messageHash.put("IBMCode", oldDataRdc.getCollectionCd());
      }
      if (StringUtils.isBlank(handler.cmrData.getEngineeringBo())) {
        handler.messageHash.put("EngrBrnchOff", oldDataRdc.getEngineeringBo());
      }
    }

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
