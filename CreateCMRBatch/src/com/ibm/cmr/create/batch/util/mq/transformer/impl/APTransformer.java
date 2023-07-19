/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.APHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasRecord;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

public abstract class APTransformer extends MessageTransformer {

  private static final Logger LOG = Logger.getLogger(APTransformer.class);

  private APHandler geoHandler;

  protected WtaasRecord currentRecord;

  /*
   * input param is 3 digit number return 2 number country code
   */
  public String convertIssuing2Cd(String issuing) {

    HashMap<String, String> hm = new HashMap<String, String>();
    hm.put("615", "BD");
    hm.put("616", "AU");
    hm.put("643", "BN");
    hm.put("652", "LK");
    hm.put("738", "HK");
    hm.put("744", "IN");
    hm.put("749", "ID");
    hm.put("778", "MY");
    hm.put("796", "NZ");
    hm.put("818", "PH");
    hm.put("834", "SG");
    hm.put("852", "VN");
    hm.put("856", "TH");
    hm.put("646", "MM");
    hm.put("714", "LA");
    hm.put("720", "KH");
    hm.put("736", "MO");
    hm.put("790", "NP");

    return hm.get(issuing).toString();
  }

  public APTransformer(String cmrIssuingCntry) throws Exception {
    super(cmrIssuingCntry);
    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);
    if (handler != null && handler instanceof APHandler) {
      this.geoHandler = (APHandler) handler;
    } else {
      throw new Exception("Handler should be an instance of APHandler.");
    }
  }

  /**
   * Handles default values for AP countries
   * 
   * @param handler
   * @param messageHash
   * @param cmrData
   * @param addrData
   * @param crossBorder
   */
  protected void handleDataDefaults(MQMessageHandler handler) {
    // String mrcCode = (("32".equalsIgnoreCase(handler.cmrData.getIsuCd()) ||
    // ("34".equalsIgnoreCase(handler.cmrData.getIsuCd())))) ? "3" : "2";
    APHandler aphandler = (APHandler) RequestUtils.getGEOHandler(handler.cmrData.getCmrIssuingCntry());
    if (!handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.INDIA)) {
      handler.messageHash.put("MrktRespCode", "3");
    }
    handler.messageHash.put("SellBrnchOff", handler.cmrData.getTerritoryCd());
    handler.messageHash.put("SellDept", handler.cmrData.getIsbuCd());
    handler.messageHash.put("InstBrnchOff", handler.cmrData.getTerritoryCd());
    handler.messageHash.put("InstDept", handler.cmrData.getIsbuCd());

    if ("N".equalsIgnoreCase(handler.cmrData.getInacType())) {
      handler.messageHash.put("NAC", handler.cmrData.getInacCd());
      handler.messageHash.put("INAC", "");
    } else if ("I".equalsIgnoreCase(handler.cmrData.getInacType())) {
      handler.messageHash.put("NAC", "");
      handler.messageHash.put("INAC", handler.cmrData.getInacCd());
    } else {
      handler.messageHash.put("NAC", "");
      handler.messageHash.put("INAC", "");
    }

    String isu = handler.cmrData.getIsuCd();
    if ("32".equalsIgnoreCase(isu))
      handler.messageHash.put("ISUChar", "#");
    else if ("5B".equalsIgnoreCase(isu)) {
      handler.messageHash.put("ISUChar", "%");
      handler.messageHash.put("MrktRespCode", "2");
    } else
      handler.messageHash.put("ISUChar", "");

    /*
     * String restrictInd = handler.cmrData.getRestrictInd(); if
     * ("60".equalsIgnoreCase(restrictInd)) {
     * handler.messageHash.put("RestrictedInd", "60"); }
     */

    // CMR-3163 - make sure NotifierSrc2 = "060" if private customer
    // private customer is by scenario OR using ISIC 9500
    if ("C".equals(handler.adminData.getReqType())) {
      String isic = handler.cmrData.getIsicCd();
      String scenario = handler.cmrData.getCustSubGrp();
      if ("9500".equals(isic) || (scenario != null && scenario.contains("PRIV"))) {
        handler.messageHash.put("NotifierSrc2", "060");
      } else {
        handler.messageHash.put("NotifierSrc2", "");
      }
      // handler.messageHash.put("NotifierSrc1", "");
    } else {
      // handler.messageHash.put("NotifierSrc1", "");
      handler.messageHash.put("NotifierSrc2", "");
    }

    // Handling obsolete data
    if (handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.KOREA)) {
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
  }

  /**
   * Handles default address values for AP countries
   * 
   * @param handler
   */
  protected void handleAddressDefaults(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    String line1 = "";
    if (!StringUtils.isBlank(addrData.getCustNm1())) {
      line1 += addrData.getCustNm1();
    }

    String line2 = "";
    if (!StringUtils.isBlank(addrData.getCustNm2())) {
      line2 += addrData.getCustNm2();
    }

    handler.messageHash.put("AddrLine1", line1);
    handler.messageHash.put("AddrLine2", line2);
  }

  protected void setAbbLoc(MQMessageHandler handler) {
    String branchID = "";
    String abbloc = "";
    if (handler.cmrData.getCollBoId() != null || (!StringUtils.isEmpty(handler.cmrData.getCollBoId())))
      branchID = handler.cmrData.getCollBoId();
    if (handler.cmrData.getCollBoId() == null)
      branchID = "  ";

    if (StringUtils.isEmpty(handler.cmrData.getAbbrevLocn())) {
      abbloc = "";
    }
    if (!StringUtils.isEmpty(handler.cmrData.getAbbrevLocn())) {
      abbloc = handler.cmrData.getAbbrevLocn();
    }
    handler.messageHash.put("AbbrLoc", branchID + " " + abbloc);
  }

  protected void handleMove(MQMessageHandler handler, String geo) {
    Addr addrData = handler.addrData;

    List<String> abbrevLandCountries = new ArrayList<String>();
    abbrevLandCountries = Arrays.asList("US", "MY", "GB", "SG", "AE", "CH", "MV", "AU", "FR", "TH", "NL", "IE", "HK", "DE", "ID", "BD", "CA", "LK",
        "TW", "NZ", "VN", "PH", "KR", "MM", "KH", "BN", "PG");
    String line66 = "";
    String scenario = handler.cmrData.getCustSubGrp();
    boolean update = "U".equals(handler.adminData.getReqType());
    if (!StringUtils.isBlank(addrData.getCity1())) {
      line66 = addrData.getCity1();
      if (addrData.getLandCntry() != null && !addrData.getLandCntry().equalsIgnoreCase(convertIssuing2Cd(handler.cmrData.getCmrIssuingCntry()))) {
        if (!update && abbrevLandCountries.contains(addrData.getLandCntry()) && "CROSS".equals(scenario))
          line66 += " " + "<" + addrData.getLandCntry() + ">";
        else
          line66 += " " + "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";
      }
      if ("LA".equalsIgnoreCase(addrData.getLandCntry()))
        line66 = addrData.getCity1() + " " + "<Laos>";
      if (addrData.getPostCd() != null)
        line66 += " " + addrData.getPostCd();
      handler.messageHash.put("AddrLine6", line66);
    } else {
      if (addrData.getLandCntry() != null && !addrData.getLandCntry().equalsIgnoreCase(convertIssuing2Cd(handler.cmrData.getCmrIssuingCntry())))
        if (abbrevLandCountries.contains(addrData.getLandCntry()) && scenario.equals("CROSS"))
          line66 += " " + "<" + addrData.getLandCntry() + ">";
        else
          line66 += " " + "<" + LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">";

      if ("LA".equalsIgnoreCase(addrData.getLandCntry()))
        line66 = "<Laos>";
      if (addrData.getPostCd() != null)
        line66 += " " + addrData.getPostCd();
      handler.messageHash.put("AddrLine6", line66);
    }

    if ((handler.messageHash.get("AddrLine5").length() == 0 || handler.messageHash.get("AddrLine4").length() == 0) && "ASEAN".equalsIgnoreCase(geo)) {
      String addr6 = handler.messageHash.get("AddrLine6");
      if (addr6 == null) {
        addr6 = "";
      }
      handler.messageHash.put("AddrLine5", addr6);
      handler.messageHash.put("AddrLine6", "");
    }

    if ("ISA".equalsIgnoreCase(geo) || "GCG".equalsIgnoreCase(geo)) {
      arrangeAddressLinesData(handler);
    }

  }

  @Override
  public String getAddressUse(Addr addr) {
    return this.geoHandler.getMappedAddressUse(this.cmrIssuingCntry, addr.getId().getAddrType());
  }

  /**
   * Returns the AddrUseCA value for the main address (ZS01)
   * 
   * @return
   */
  protected abstract String getMainAddressUseCA();

  /**
   * Computes the address use to send
   * 
   * @return
   */
  protected String computeAddressUse(MQMessageHandler handler) {
    LOG.debug("Computing address use..");
    if (handler.addrData == null) {
      return getMainAddressUseCA();
    }
    if ("ZS01".equals(handler.addrData.getId().getAddrType())) {
      // change request by liezl - use default address uses like in R1 for main
      // address
      if (CmrConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())) {
        LOG.trace("Returning main address use: " + getMainAddressUseCA() + " for creates and ZS01 record");
        return getMainAddressUseCA();
      }

      String mainAddrUse = getMainAddressUseCA();
      LOG.trace("Main Address Use: " + mainAddrUse);
      String addrUse = null;
      for (Addr addr : handler.currentAddresses) {
        if (!"ZS01".equals(addr.getId().getAddrType())) {
          addrUse = this.geoHandler.getMappedAddressUse(this.cmrIssuingCntry, addr.getId().getAddrType());
          if (!StringUtils.isEmpty(addrUse)) {
            mainAddrUse = mainAddrUse.replaceAll(addrUse, "");
          }
        }
      }
      LOG.trace("Final address use: " + mainAddrUse);
      return mainAddrUse;
    } else {
      String addrUse = this.geoHandler.getMappedAddressUse(this.cmrIssuingCntry, handler.addrData.getId().getAddrType());
      LOG.trace("Type: " + handler.addrData.getId().getAddrType() + " Address use: " + addrUse);
      return addrUse;
    }
  }

  @Override
  public String getAddressKey(String addrType) {
    // not used
    return null;
  }

  @Override
  public String getTargetAddressType(String addrType) {
    // not used
    return null;
  }

  @Override
  public String getSysLocToUse() {
    return this.cmrIssuingCntry;
  }

  /**
   * Method that returns a non-null CMR Issuing Country if the
   * {@link MessageTransformer} determines that the data on the handler needs to
   * be a double create. The overriding method should check the data within the
   * method before returning a value. Return null if should not be a double
   * create.
   * 
   * @param handler
   * @return
   */
  protected String getDoubleCreateCountry(EntityManager entityManager, MQMessageHandler handler) {
    return "";
  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    String correlationId = handler.mqIntfReqQueue.getCorrelationId();
    if (!StringUtils.isEmpty(correlationId)) {
      // already a correlated request, set to true
      return true;
    }
    String doubleCreateCntry = getDoubleCreateCountry(entityManager, handler);
    if (StringUtils.isEmpty(doubleCreateCntry)) {
      // no double creates, complete
      return true;
    }

    try {

      MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
      if ("Y".equals(currentQ.getMqInd()) || MQMsgConstants.REQ_STATUS_COM.equals(currentQ.getReqStatus())) {
        LOG.debug("MQ record already previously completed, skipping double create process.");
        return true;
      }
      LOG.debug("Completing initial request " + currentQ.getId().getQueryReqId());
      Timestamp ts = SystemUtil.getCurrentTimestamp();
      currentQ.setReqStatus(MQMsgConstants.REQ_STATUS_COM);
      currentQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
      currentQ.setLastUpdtTs(ts);
      currentQ.setMqInd("Y");

      MqIntfReqQueue doubleQ = new MqIntfReqQueue();
      MqIntfReqQueuePK doubleQPk = new MqIntfReqQueuePK();
      long id = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "QUERY_REQ_ID", "CREQCMR");
      LOG.debug("Creating double create request for Country " + doubleCreateCntry + " with MQ ID " + id);
      doubleQPk.setQueryReqId(id);
      doubleQ.setId(doubleQPk);
      doubleQ.setCmrIssuingCntry(doubleCreateCntry);
      String cmrNo = currentQ.getCmrNo();
      if (StringUtils.isEmpty(cmrNo)) {
        cmrNo = handler.cmrData.getCmrNo();
      }
      LOG.debug("Assigning CMR No. " + cmrNo + " in correlated request.");
      doubleQ.setCmrNo(cmrNo);
      doubleQ.setCorrelationId(currentQ.getId().getQueryReqId() + "");
      doubleQ.setCreateBy(MQMsgConstants.MQ_APP_USER);
      doubleQ.setCreateTs(ts);
      doubleQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
      doubleQ.setLastUpdtTs(ts);
      doubleQ.setMqInd("N");
      doubleQ.setReqId(currentQ.getReqId());
      doubleQ.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
      doubleQ.setReqType(currentQ.getReqType());
      doubleQ.setTargetSys(currentQ.getTargetSys());

      handler.createPartialComment("Handling System Location " + doubleCreateCntry + " record for double create scenario",
          handler.mqIntfReqQueue.getCmrNo());
      entityManager.merge(currentQ);
      entityManager.persist(doubleQ);
      entityManager.flush();

      return false;
    } catch (Exception e) {
      LOG.error("Error in completing double create request. Skipping generation and completing request", e);
      return true;
    }

  }

  public void arrangeAddressLinesData(MQMessageHandler handler) {
    String addr5 = handler.messageHash.get("AddrLine5");
    String addr6 = handler.messageHash.get("AddrLine6");

    if (addr5 == null)
      addr5 = "";
    if (addr6 == null)
      addr6 = "";
    if (handler.messageHash.get("AddrLine4").length() == 0 && handler.messageHash.get("AddrLine5").length() == 0) {
      handler.messageHash.put("AddrLine4", addr6);
      handler.messageHash.put("AddrLine5", "");
      handler.messageHash.put("AddrLine6", "");
    } else if (handler.messageHash.get("AddrLine4").length() == 0 && handler.messageHash.get("AddrLine5").length() > 0) {
      handler.messageHash.put("AddrLine4", addr5);
      handler.messageHash.put("AddrLine5", addr6);
      handler.messageHash.put("AddrLine6", "");
    } else if (handler.messageHash.get("AddrLine4").length() > 0 && handler.messageHash.get("AddrLine5").length() == 0) {
      handler.messageHash.put("AddrLine5", addr6);
      handler.messageHash.put("AddrLine6", "");
    }

  }
}
