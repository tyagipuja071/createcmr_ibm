/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Arrays;

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
 * <li>744 - India</li>
 * <li>615 - Bangladesh</li>
 * <li>790 - Nepal</li>
 * <li>652 - Sri Lanka</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */

public abstract class ISATransformer extends APTransformer {

  private static final String[] clusterIndiaMrc2 = { "05224", "04477", "04490", "04467", "05225" };
  private static final String[] clusterIndiaMrc3 = { "09062", "10193", "10194", "10195", "10196", "10197", "10198", "10199", "10200", "10201",
      "10202", "10203", "10204", "10205", "10206", "10207", "10208", "10209", "10210", "10211", "10212", "10213", "10214", "10215", "10590", "10591",
      "10592", "10593", "10594", "10595", "10596", "10597", "10598", "10599", "10600", "10601", "10602", "10603", "10604", "10605", "10606", "10607",
      "10608", "10609", "10610", "10611", "10612", "10613", "10614", "10615", "10616", "10617", "10618", "10619", "10620", "10621", "10622", "10623",
      "10624", "10625", "10626", "10627", "10628", "10629", "10630", "10631", "10632", "10633", "10634", "10635", "10636", "10637", "10638", "10639",
      "10640", "10641", "10642", "10643", "10644", "10645", "10654", "10655", "10656", "10657" };

  /**
   * @param cmrIssuingCntry
   * @throws Exception
   */
  public ISATransformer(String cmrIssuingCntry) throws Exception {
    super(cmrIssuingCntry);

  }

  @Override
  protected void handleDataDefaults(MQMessageHandler handler) {
    super.handleDataDefaults(handler);
    APHandler aphandler = (APHandler) RequestUtils.getGEOHandler(handler.cmrData.getCmrIssuingCntry());
    if ("NA".equalsIgnoreCase(handler.cmrData.getVat())) {
      handler.messageHash.put("CtryText", "");
    } else {
      handler.messageHash.put("CtryText", handler.cmrData.getVat());
    }

    String isu = handler.cmrData.getIsuCd();
    if ("0".equalsIgnoreCase(handler.cmrData.getClientTier())) {
      handler.messageHash.put("GB_SegCode", "");
    }
    String abbloc = "";
    abbloc = handler.cmrData.getAbbrevLocn();
    if (StringUtils.isEmpty(handler.cmrData.getAbbrevLocn())) {
      abbloc = "";
    }
    handler.messageHash.put("AbbrLoc", "   " + abbloc);

    String clusterID = handler.cmrData.getApCustClusterId();
    if (clusterID != null && clusterID.contains("BLAN")) {
      handler.messageHash.put("ClusterNo", "");
    } else {
      handler.messageHash.put("ClusterNo", clusterID);
    }
    String reqType = handler.adminData.getReqType();
    String cntry = handler.cmrData.getCmrIssuingCntry();
    String scenario = handler.cmrData.getCustSubGrp();
    if (clusterID != null && cntry != null && cntry.equals(SystemLocation.INDIA) && Arrays.asList(clusterIndiaMrc2).contains(clusterID)
        && !StringUtils.equalsIgnoreCase(reqType, "U")) {
      handler.messageHash.put("MrktRespCode", "2");
    } else if (clusterID != null && cntry.equals(SystemLocation.INDIA) && Arrays.asList(clusterIndiaMrc3).contains(clusterID)
        && !StringUtils.equalsIgnoreCase(reqType, "U")) {
      handler.messageHash.put("MrktRespCode", "3");
    }

    if (cntry != null && StringUtils.equalsIgnoreCase(reqType, "C") && ((cntry.equals(SystemLocation.INDIA) && scenario.equalsIgnoreCase("IGF"))
        || ((cntry.equals(SystemLocation.BANGLADESH) || cntry.equals(SystemLocation.SRI_LANKA)) && scenario.equalsIgnoreCase("DUMMY")))) {
      handler.messageHash.put("MrktRespCode", "2");
    }

    // Handling obsolete data
    DataRdc oldDataRdc = aphandler.getAPClusterDataRdc(handler.cmrData.getId().getReqId());
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
