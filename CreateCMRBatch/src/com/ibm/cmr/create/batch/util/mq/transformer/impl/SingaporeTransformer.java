package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;

public class SingaporeTransformer extends ASEANTransformer {

  private static final Logger LOG = Logger.getLogger(BangladeshTransformer.class);

  public SingaporeTransformer() throws Exception {
    super(SystemLocation.SINGAPORE);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    this.handleDataDefaults(handler);
    if ("SPHUB".equalsIgnoreCase(handler.cmrData.getCustSubGrp()))
      handler.messageHash.put("RegionCode", "HUB");
    else
      handler.messageHash.put("RegionCode", "");

    // double creates changes
    String inacType = !StringUtils.isEmpty(handler.cmrData.getInacType()) ? handler.cmrData.getInacType() : "";
    String cluster = !StringUtils.isEmpty(handler.cmrData.getApCustClusterId()) ? handler.cmrData.getApCustClusterId() : "";
    String gbSegCode = !StringUtils.isEmpty(handler.cmrData.getClientTier()) ? handler.cmrData.getClientTier() : "";
    String countryZS01 = getLandedCntryInZS01(handler);
    String CtryText = !StringUtils.isEmpty(handler.cmrData.getVat()) ? handler.cmrData.getVat() : "";    
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.BANGLADESH)) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
      handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("IBMCode", "S001");
      handler.messageHash.put("ClusterNo", "00000");
      handler.messageHash.put("GB_SegCode", "Z");
      handler.messageHash.put("SalesmanNo", "000DSW");
      handler.messageHash.put("SellBrnchOff", "000");
      handler.messageHash.put("InstBrnchOff", "000");
      handler.messageHash.put("CtryText", "");
      if ("N".equalsIgnoreCase(inacType)) {
        handler.messageHash.put("INAC", "");
        handler.messageHash.put("NAC", "");
      }
    }
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.SRI_LANKA)) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
      handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("IBMCode", "S013");
      handler.messageHash.put("ClusterNo", "00000");
      handler.messageHash.put("GB_SegCode", "Z");
      handler.messageHash.put("SalesmanNo", "000DSW");
      handler.messageHash.put("SellBrnchOff", "000");
      handler.messageHash.put("InstBrnchOff", "000");
      handler.messageHash.put("CtryText", "");
      if ("N".equalsIgnoreCase(inacType)) {
        handler.messageHash.put("INAC", "");
        handler.messageHash.put("NAC", "");
      }
    }
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM)
        && !getLandedCntryInZS01(handler).equals("KH")) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
      handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("IBMCode", "V001");
      handler.messageHash.put("ClusterNo", cluster);
      handler.messageHash.put("GB_SegCode", gbSegCode);
      handler.messageHash.put("SalesmanNo", "000DSW");
      handler.messageHash.put("SellBrnchOff", "000");
      handler.messageHash.put("InstBrnchOff", "000");
      handler.messageHash.put("CtryText", "");
      handler.messageHash.put("AbbrLoc", "VN".equals(countryZS01) ? "VIETNAM" : getLandCntryDesc(handler.getEntityManager(), countryZS01));
      if ("N".equalsIgnoreCase(inacType)) {
        handler.messageHash.put("INAC", "");
        handler.messageHash.put("NAC", "");
      }
    } else if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM)
        && getLandedCntryInZS01(handler).equals("KH")) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
      handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("IBMCode", "C001");
      handler.messageHash.put("ClusterNo", cluster);
      handler.messageHash.put("GB_SegCode", gbSegCode);
      handler.messageHash.put("SalesmanNo", "000DSW");
      handler.messageHash.put("SellBrnchOff", "000");
      handler.messageHash.put("InstBrnchOff", "000");
      handler.messageHash.put("CtryText", "");
      if ("N".equalsIgnoreCase(inacType)) {
        handler.messageHash.put("INAC", "");
        handler.messageHash.put("NAC", "");
      }
    }
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.INDIA)) {
      handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
      handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("IBMCode", "I001");
      handler.messageHash.put("ClusterNo", "00000");
      handler.messageHash.put("GB_SegCode", "Z");
      handler.messageHash.put("SalesmanNo", "000DSW");
      handler.messageHash.put("SellBrnchOff", "000");
      handler.messageHash.put("InstBrnchOff", "000");
      handler.messageHash.put("CtryText", "");
      if ("N".equalsIgnoreCase(inacType)) {
        handler.messageHash.put("INAC", "");
        handler.messageHash.put("NAC", "");
      }
    }
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    String custType = !StringUtils.isEmpty(cmrData.getCustGrp()) ? cmrData.getCustGrp() : "";
    String custSubType = !StringUtils.isEmpty(cmrData.getCustSubGrp()) ? cmrData.getCustSubGrp() : "";
    boolean create = "C".equalsIgnoreCase(handler.adminData.getReqType());
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      LOG.debug(
          "Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. " + handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("TransCode", "N");
    } else if ("CROSS".equalsIgnoreCase(custType) && "SPOFF".equalsIgnoreCase(custSubType) && create) {
      handler.messageHash.put("CustNo", handler.cmrData.getCmrNo());
      handler.messageHash.put("TransCode", "N");
    }

    // set CtryText blank for SG - CMR-4985
    // handler.messageHash.put("CtryText", "");
    
    // Connect UEN# to COUNTRY USE - CREATCMR-5258
    handler.messageHash.put("CtryText", CtryText);
    
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    handleAddressDefaults(handler);
    Addr addrData = handler.addrData;

    String line6 = "Singapore ";
    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }

    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ASEAN");
    // only for local
    if (StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && addrData.getLandCntry() != null
        && "SG".equalsIgnoreCase(addrData.getLandCntry())) {
      String lineSix = "Singapore ";
      if (!StringUtils.isBlank(addrData.getPostCd())) {
        lineSix += addrData.getPostCd();
      }
      handler.messageHash.put("AddrLine6", lineSix);
      String addr5 = handler.messageHash.get("AddrLine5");
      String addr6 = handler.messageHash.get("AddrLine6");

      String line5 = "";
      if (!StringUtils.isBlank(addrData.getDept())) {
        line5 += addrData.getDept();
      }

      handler.messageHash.put("AddrLine5", line5);
    }

    // only for double creates
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      String line66 = "";
      if (!StringUtils.isBlank(addrData.getCity1())) {
        line66 = addrData.getCity1();
        line66 += " " + "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";
        if ("VN".equalsIgnoreCase(addrData.getLandCntry()))
          line66 = addrData.getCity1() + " " + "<VIETNAM>";
        if ("LA".equalsIgnoreCase(addrData.getLandCntry()))
          line66 = addrData.getCity1() + " " + "<Laos>";
        if (addrData.getPostCd() != null)
          line66 += " " + addrData.getPostCd();
        handler.messageHash.put("AddrLine6", line66);
      } else {
        line66 = "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";
        if ("VN".equalsIgnoreCase(addrData.getLandCntry()))
          line66 = "<VIETNAM>";
        if ("LA".equalsIgnoreCase(addrData.getLandCntry()))
          line66 = "<Laos>";
        if (addrData.getPostCd() != null)
          line66 += " " + addrData.getPostCd();
        handler.messageHash.put("AddrLine6", line66);
      }

      String line5 = "";
      if (!StringUtils.isBlank(addrData.getDept())) {
        line5 += addrData.getDept();
      }

      handler.messageHash.put("AddrLine5", line5);
    }
    arrangeAddressLinesData(handler);
  }

  private String getLandedCntryInZS01(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getLandCntry();
      }
    return "";
  }

  private String getLandCntryDesc(EntityManager entityManager, String landCntryCd) {
    String landCntryDesc = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETCNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", landCntryCd);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      landCntryDesc = results.get(0);
    }
    entityManager.flush();
    return landCntryDesc;
  }
}
