package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.impl.APHandler;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * Base {@link MessageTransformer} class that handles the following countries:
 * <ul>
 * <li>738 - Hong Kong</li>
 * <li>736 - Macao</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class GCGTransformer extends APTransformer {

  public GCGTransformer(String cmrIssuingCntry) throws Exception {
    super(cmrIssuingCntry);

  }

  @Override
  protected void handleDataDefaults(MQMessageHandler handler) {
    super.handleDataDefaults(handler);
    APHandler aphandler = (APHandler) RequestUtils.getGEOHandler(handler.cmrData.getCmrIssuingCntry());

    String sellingDept = "Z2I0";
    String ZERO4 = "0000";
    String ZERO3 = "000";
    handler.messageHash.put("SellBrnchOff", "738");
    handler.messageHash.put("InstBrnchOff", "738");
    handler.messageHash.put("EngrBrnchOff", "738");

    handler.messageHash.put("AbbrLoc", "00 HK");

    String repTeamMemNo = handler.cmrData.getRepTeamMemberNo();
    if (StringUtils.isEmpty(repTeamMemNo))
      repTeamMemNo = "";
    if (StringUtils.isNotEmpty(repTeamMemNo))
      repTeamMemNo = repTeamMemNo.substring(3, 4);

    String subIndCd = handler.cmrData.getSubIndustryCd();
    if (StringUtils.isEmpty(subIndCd))
      subIndCd = "";
    if (StringUtils.isNotEmpty(subIndCd))
      subIndCd = subIndCd.substring(0, 1);

    String mrcCode = "";
    String isu = handler.cmrData.getIsuCd();
    if (SystemLocation.HONG_KONG.equals(handler.cmrData.getCmrIssuingCntry()) || SystemLocation.MACAO.equals(handler.cmrData.getCmrIssuingCntry())) {
      if ("32".equalsIgnoreCase(isu) || "34".equalsIgnoreCase(isu) || "36".equalsIgnoreCase(isu) || "5K".equalsIgnoreCase(isu))
        mrcCode = "3";
      else
        mrcCode = "2";
    } else {
      if ("32".equalsIgnoreCase(isu) || "34".equalsIgnoreCase(isu))
        mrcCode = "3";
      else
        mrcCode = "2";
    }

    handler.messageHash.put("MrktRespCode", mrcCode);
    String custSubGrp = handler.cmrData.getCustSubGrp();

    // Defect 1725994 added sub scenarios for cross border new in rel2
    if ("DUMMY".equalsIgnoreCase(custSubGrp) || "INTER".equalsIgnoreCase(custSubGrp) || "XDUMM".equalsIgnoreCase(custSubGrp)
        || "XINT".equalsIgnoreCase(custSubGrp)) {
      handler.messageHash.put("SellDept", sellingDept);
      handler.messageHash.put("InstDept", sellingDept);
      handler.messageHash.put("EngrDept", sellingDept);
      mrcCode = "2";
    } else if ("MKTPC".equalsIgnoreCase(custSubGrp) || "XMKTP".equalsIgnoreCase(custSubGrp)) {
      mrcCode = "3";
      handler.messageHash.put("SellBrnchOff", ZERO3);
      handler.messageHash.put("InstBrnchOff", ZERO3);
      handler.messageHash.put("EngrBrnchOff", ZERO3);
      handler.messageHash.put("SellDept", ZERO4);
      handler.messageHash.put("InstDept", ZERO4);
      handler.messageHash.put("EngrDept", ZERO4);
    } else if ("BUSPR".equalsIgnoreCase(custSubGrp) || "XBUSP".equalsIgnoreCase(custSubGrp)) {
      String bussinessPartner = "Z" + mrcCode + "S" + repTeamMemNo;
      handler.messageHash.put("SellDept", bussinessPartner);
      handler.messageHash.put("InstDept", bussinessPartner);
      handler.messageHash.put("EngrDept", bussinessPartner);
    } else if ("NRML".equalsIgnoreCase(custSubGrp) || "CROSS".equalsIgnoreCase(custSubGrp) || "AQSTN".equalsIgnoreCase(custSubGrp)
        || "SOFT".equalsIgnoreCase(custSubGrp) || "ASLOM".equalsIgnoreCase(custSubGrp) || "BLUMX".equalsIgnoreCase(custSubGrp)
        || "XNRML".equalsIgnoreCase(custSubGrp) || "XAQST".equalsIgnoreCase(custSubGrp) || "XSOFT".equalsIgnoreCase(custSubGrp)
        || "XASLM".equalsIgnoreCase(custSubGrp) || "XBLUM".equalsIgnoreCase(custSubGrp) || "ECOSY".equalsIgnoreCase(custSubGrp)) {
      String bussinessPartner = subIndCd + mrcCode + "T" + repTeamMemNo;
      handler.messageHash.put("SellDept", bussinessPartner);
      handler.messageHash.put("InstDept", bussinessPartner);
      handler.messageHash.put("EngrDept", bussinessPartner);
    } else if ("ESA".equalsIgnoreCase(custSubGrp) || "NRMLC".equalsIgnoreCase(custSubGrp) || "NRMLD".equalsIgnoreCase(custSubGrp)) {
      if (SystemLocation.HONG_KONG.equals(handler.cmrData.getCmrIssuingCntry())
          || SystemLocation.MACAO.equals(handler.cmrData.getCmrIssuingCntry())) {
        String bussinessPartner = subIndCd + mrcCode + "T" + repTeamMemNo;
        handler.messageHash.put("SellDept", bussinessPartner);
        handler.messageHash.put("InstDept", bussinessPartner);
        handler.messageHash.put("EngrDept", bussinessPartner);
      }
    }

    if ("0".equalsIgnoreCase(handler.cmrData.getClientTier())) {
      handler.messageHash.put("GB_SegCode", "");
    }
    // Handling obsolete data
    if (handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.TAIWAN)) {
      // Handling obsolete data
      DataRdc oldDataRdc = aphandler.getAPClusterDataRdc(handler.cmrData.getId().getReqId());
      String reqType = handler.adminData.getReqType();
      if (StringUtils.equalsIgnoreCase(reqType, "U")) {
        if (!StringUtils.isNotBlank(handler.cmrData.getApCustClusterId())) {
          handler.messageHash.put("ClusterNo", oldDataRdc.getApCustClusterId());
        }
        if (!StringUtils.isNotBlank(handler.cmrData.getClientTier())) {
          handler.messageHash.put("GB_SegCode", oldDataRdc.getClientTier());
        }
        if (!StringUtils.isNotBlank(handler.cmrData.getIsuCd())) {
          handler.messageHash.put("ISU", oldDataRdc.getIsuCd());
        }
        if (!StringUtils.isNotBlank(handler.cmrData.getInacType())) {
          handler.messageHash.put("inacType", oldDataRdc.getInacType());
        }
        if (!StringUtils.isNotBlank(handler.cmrData.getInacCd())) {
          handler.messageHash.put("inacCd", oldDataRdc.getInacCd());
        }
        if (!StringUtils.isNotBlank(handler.cmrData.getCollectionCd())) {
          handler.messageHash.put("IBMCode", oldDataRdc.getCollectionCd());
        }
        if (!StringUtils.isNotBlank(handler.cmrData.getEngineeringBo())) {
          handler.messageHash.put("EngrBrnchOff", oldDataRdc.getEngineeringBo());
        }
      }
    }
  }

  @Override
  protected void handleAddressDefaults(MQMessageHandler handler) {
    super.handleAddressDefaults(handler);
    Addr addrData = handler.addrData;
    if (addrData != null && "ZS01".equalsIgnoreCase(addrData.getId().getAddrType())) {
      String mainAddrUse = getMainAddressUseCA();
      handler.messageHash.put("AddrUseCA", mainAddrUse);
    } else {
      handler.messageHash.put("AddrUseCA", computeAddressUse(handler));
    }

    String line1 = "";
    if (!StringUtils.isBlank(addrData.getCustNm1())) {
      line1 += addrData.getCustNm1();
    }

    String line2 = "";
    if (!StringUtils.isBlank(addrData.getCustNm2()) || StringUtils.isBlank(addrData.getCustNm2())) {
      line2 = addrData.getCustNm2();
    }

    String line3 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line3 = addrData.getAddrTxt();
    }
    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt2())) {
      line4 = addrData.getAddrTxt2();
    }

    handler.messageHash.put("AddrLine1", line1);
    handler.messageHash.put("AddrLine2", line2);
    handler.messageHash.put("AddrLine3", line3);
    handler.messageHash.put("AddrLine4", line4);

  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "A";
  }

  @Override
  public String[] getAddressOrder() {
    return new String[] { "ZS01" };
  }

  @Override
  protected String getMainAddressUseCA() {
    return "1234567ABCDEFGH";
  }
}
