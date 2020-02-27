/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.MCOHandler;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GenerateCMRNoClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public abstract class MCOTransformer extends MessageTransformer {

  private static final Logger LOG = Logger.getLogger(MCOTransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02" };

  protected static Map<String, String> SOURCE_CODE_MAP = new HashMap<String, String>();

  static {
    SOURCE_CODE_MAP.put(SystemLocation.SOUTH_AFRICA, "FOQ");
    SOURCE_CODE_MAP.put(SystemLocation.MAURITIUS, "QE2");
    SOURCE_CODE_MAP.put(SystemLocation.MALI, "QE4");
    SOURCE_CODE_MAP.put(SystemLocation.EQUATORIAL_GUINEA, "QE6");
    SOURCE_CODE_MAP.put(SystemLocation.ANGOLA, "EFS");
    SOURCE_CODE_MAP.put(SystemLocation.SENEGAL, "XE6");
    SOURCE_CODE_MAP.put(SystemLocation.BOTSWANA, "XZ2");
    SOURCE_CODE_MAP.put(SystemLocation.IVORY_COAST, "QE7");
    SOURCE_CODE_MAP.put(SystemLocation.BURUNDI, "XZJ");
    SOURCE_CODE_MAP.put(SystemLocation.GABON, "QE8");
    SOURCE_CODE_MAP.put(SystemLocation.DEMOCRATIC_CONGO, "XEL");
    SOURCE_CODE_MAP.put(SystemLocation.CONGO_BRAZZAVILLE, "QE9");
    SOURCE_CODE_MAP.put(SystemLocation.CAPE_VERDE_ISLAND, "XZP");
    SOURCE_CODE_MAP.put(SystemLocation.DJIBOUTI, "XE7");
    SOURCE_CODE_MAP.put(SystemLocation.GUINEA_CONAKRY, "QEB");
    SOURCE_CODE_MAP.put(SystemLocation.CAMEROON, "QEC");
    SOURCE_CODE_MAP.put(SystemLocation.ETHIOPIA, "XGO");
    SOURCE_CODE_MAP.put(SystemLocation.MADAGASCAR, "QED");
    SOURCE_CODE_MAP.put(SystemLocation.MAURITANIA, "QEE");
    SOURCE_CODE_MAP.put(SystemLocation.TOGO, "QEF");
    SOURCE_CODE_MAP.put(SystemLocation.GHANA, "XGA");
    SOURCE_CODE_MAP.put(SystemLocation.ERITREA, "XWT");
    SOURCE_CODE_MAP.put(SystemLocation.GAMBIA, "XER");
    SOURCE_CODE_MAP.put(SystemLocation.KENYA, "XGN");
    SOURCE_CODE_MAP.put(SystemLocation.MALAWI_CAF, "XGE");
    SOURCE_CODE_MAP.put(SystemLocation.LIBERIA, "X0C");
    SOURCE_CODE_MAP.put(SystemLocation.MOZAMBIQUE, "XGF");
    SOURCE_CODE_MAP.put(SystemLocation.NIGERIA, "EFP");
    SOURCE_CODE_MAP.put(SystemLocation.CENTRAL_AFRICAN_REPUBLIC, "QEG");
    SOURCE_CODE_MAP.put(SystemLocation.ZIMBABWE, "EFT");
    SOURCE_CODE_MAP.put(SystemLocation.SAO_TOME_ISLANDS, "XZS");
    SOURCE_CODE_MAP.put(SystemLocation.RWANDA, "XZK");
    SOURCE_CODE_MAP.put(SystemLocation.SIERRA_LEONE, "XGB");
    SOURCE_CODE_MAP.put(SystemLocation.SOMALIA, "XGG");
    SOURCE_CODE_MAP.put(SystemLocation.BENIN, "QEH");
    SOURCE_CODE_MAP.put(SystemLocation.BURKINA_FASO, "QEI");
    SOURCE_CODE_MAP.put(SystemLocation.SOUTH_SUDAN, "EFZ");
    SOURCE_CODE_MAP.put(SystemLocation.TANZANIA, "XWH");
    SOURCE_CODE_MAP.put(SystemLocation.UGANDA, "XWI");
    SOURCE_CODE_MAP.put(SystemLocation.MALTA, "EF1");
    SOURCE_CODE_MAP.put(SystemLocation.SEYCHELLES, "QEJ");
    SOURCE_CODE_MAP.put(SystemLocation.GUINEA_BISSAU, "QEK");
    SOURCE_CODE_MAP.put(SystemLocation.NIGER, "QEL");
    SOURCE_CODE_MAP.put(SystemLocation.CHAD, "QEM");
    SOURCE_CODE_MAP.put(SystemLocation.ZAMBIA, "XWM");

  }

  public MCOTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;

    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    messageHash.put("EmbargoCode", embargoCode);

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.put("MarketingResponseCode", "3");
    messageHash.put("CEdivision", "3");

    String sbo = messageHash.get("SBO");

    if (!StringUtils.isEmpty(sbo)) {
      sbo = StringUtils.rightPad(sbo, 7, '0');
    }
    messageHash.put("SBO", sbo);
    messageHash.put("IBO", sbo);

    String custType = !StringUtils.isEmpty(cmrData.getCustSubGrp()) ? cmrData.getCustSubGrp() : "";
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(custType) || custType.contains("BP")) { // BUSPR,ccBP,ccXBP
      messageHash.put("ARemark", "YES");
      messageHash.put("CustomerType", "BP");
    } else if (custType.contains("GO")) { // GOVRN,XGOV,ccGOV,ccXGO
      messageHash.put("CustomerType", "G");
    } else if (custType.contains("IN")) { // INTER,XINTE,ccINT, ccXIN
      messageHash.put("CustomerType", "91");
    } else {
      messageHash.put("ARemark", "");
    }

    messageHash.put("LangCode", "1");
    messageHash.put("CICode", "3");

    String colBo = cmrData.getCollBoId();
    if (!StringUtils.isEmpty(colBo) && colBo.length() == 4) {
      messageHash.put("SOFCollBranch", "1" + cmrData.getCollBoId() + "Z");
    } else {
      messageHash.put("SOFCollBranch", "13100Z"); // by default, cannot be blank
    }

    // ZA - Always send CollectionCode 000001 for creates
    if (!update && SystemLocation.SOUTH_AFRICA.equals(handler.mqIntfReqQueue.getCmrIssuingCntry())) {
      messageHash.put("CollectionCode", "000001");
    }

    if (!StringUtils.isEmpty(cmrData.getCreditCd())) {
      messageHash.put("CreditCode", cmrData.getCreditCd());
    } else {
      messageHash.put("CreditCode", "");
    }
    if (!StringUtils.isEmpty(cmrData.getCommercialFinanced())) {
      messageHash.put("CoF", cmrData.getCommercialFinanced());
    } else {
      messageHash.put("CoF", "");
    }
    if (!StringUtils.isEmpty(cmrData.getIbmDeptCostCenter())) {
      messageHash.put("DepartmentNumber", cmrData.getIbmDeptCostCenter());
    } else {
      messageHash.put("DepartmentNumber", "");
    }
    messageHash.put("EnterpriseNo", !StringUtils.isEmpty(cmrData.getEnterprise()) ? cmrData.getEnterprise() : "");

    // only generate CMR no for Creates
    if (MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus()) && isGMLLCProcess(handler)) {
      LOG.debug("GM LLC scenario for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID " + handler.cmrData.getId().getReqId());
      String cmrNo = generateCMRNoForGMLLC(handler, SystemLocation.KENYA);
      if (cmrNo == null) {
        LOG.warn("Warning, a CMR No cannot be generated for the GM LLC scenario.");
      } else {
        LOG.info("CMR No " + cmrNo + " found as next available for GM LLC.");
        messageHash.put("CustomerNo", cmrNo);
      }
    } else if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      // for correlated requests, add the CN
      LOG.debug("Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. "
          + handler.mqIntfReqQueue.getCmrNo());
      messageHash.put("CustomerNo", handler.mqIntfReqQueue.getCmrNo());

      if (SystemLocation.KENYA.equals(handler.mqIntfReqQueue.getCmrIssuingCntry())) {
        LOG.debug("Setting SR for GM LLC Kenya");
        messageHash.put("SR", SystemLocation.KENYA + SystemLocation.KENYA);
      }

      // Abbreviated Location is the parent country
      Addr mainAddr = handler.addrData;
      if (mainAddr != null) {
        String abbrevLoc = LandedCountryMap.getCountryName(mainAddr.getLandCntry());
        if (!StringUtils.isEmpty(abbrevLoc)) {
          LOG.debug("Setting Abbreviated Location as parent country for GM LLC");
          messageHash.put("AbbreviatedLocation", abbrevLoc);
        }
      }
    }

    // append GM in AbbrevName for GM LLC
    if (!StringUtils.isEmpty(cmrData.getCustSubGrp()) && cmrData.getCustSubGrp().endsWith("LLC")) {
      String abbrevNm = cmrData.getAbbrevNm();
      if (!StringUtils.isEmpty(abbrevNm) && !abbrevNm.toUpperCase().endsWith(" GM")) {
        if (abbrevNm.length() > 19) {
          abbrevNm = abbrevNm.substring(0, 19);
        }
        LOG.debug("Setting Abbreviated Name for GM LLC");
        messageHash.put("CompanyName", abbrevNm + " GM");
      }
    }

  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    if (isGMLLCProcess(handler)) {
      try {

        MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
        if ("Y".equals(currentQ.getMqInd()) || MQMsgConstants.REQ_STATUS_COM.equals(currentQ.getReqStatus())) {
          LOG.debug("MQ record already previously completed, skipping LLC process.");
          return true;
        }
        LOG.debug("Completing initial request " + currentQ.getId().getQueryReqId());
        Timestamp ts = SystemUtil.getCurrentTimestamp();
        currentQ.setReqStatus(MQMsgConstants.REQ_STATUS_COM);
        currentQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
        currentQ.setLastUpdtTs(ts);
        currentQ.setMqInd("Y");

        MqIntfReqQueue kenyaQ = new MqIntfReqQueue();
        MqIntfReqQueuePK kenyaQPk = new MqIntfReqQueuePK();
        long id = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "QUERY_REQ_ID", "CREQCMR");
        LOG.debug("Creating LLC request for Kenya with MQ ID " + id);
        kenyaQPk.setQueryReqId(id);
        kenyaQ.setId(kenyaQPk);

        kenyaQ.setCmrIssuingCntry(SystemLocation.KENYA);
        kenyaQ.setCmrNo(currentQ.getCmrNo());
        kenyaQ.setCorrelationId(currentQ.getId().getQueryReqId() + "");
        kenyaQ.setCreateBy(MQMsgConstants.MQ_APP_USER);
        kenyaQ.setCreateTs(ts);
        kenyaQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
        kenyaQ.setLastUpdtTs(ts);
        kenyaQ.setMqInd("N");
        kenyaQ.setReqId(currentQ.getReqId());
        kenyaQ.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
        kenyaQ.setReqType(currentQ.getReqType());
        kenyaQ.setTargetSys(currentQ.getTargetSys());

        handler.createPartialComment("Handling Kenya record for GM LLC scenario", handler.mqIntfReqQueue.getCmrNo());
        entityManager.merge(currentQ);
        entityManager.persist(kenyaQ);
        entityManager.flush();

        return false;
      } catch (Exception e) {
        LOG.error("Error in completing LLC request. Skipping LLC generation and completing request", e);
        return true;
      }
    } else {
      return true;
    }

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    
    // Story 1718889: Tanzania: new mandatory TIN number field
    Data cmrData = handler.cmrData;
    if (SystemLocation.TANZANIA.equals(cmrData.getCmrIssuingCntry())) {
      String tin = !StringUtils.isEmpty(addrData.getDept()) ? addrData.getDept() : "";
      if (MQMsgConstants.ADDR_ZP01.equals(addrData.getId().getAddrType())){
        messageHash.put("TIN", tin);
      }
    }

    String addrKey = getAddressKey(addrData.getId().getAddrType());

    messageHash.put("SourceCode", SOURCE_CODE_MAP.get(getCmrIssuingCntry()));
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    boolean crossBorder = isCrossBorder(addrData);

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";

    // line1 is always customer name
    line1 = addrData.getCustNm1();

    if (!crossBorder) {
      // Domestic - CEWA
      // line2 = Name Con't
      // line3 = Phone + Attention Person (Phone for Shipping & EPL only)
      // line4 = Street
      // line5 = Street Con't + PO BOX
      // line6 = City / Postal Code or both

      line2 = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
      line3 = !StringUtils.isEmpty(addrData.getCustNm4()) ? addrData.getCustNm4() : "";
      if (MQMsgConstants.ADDR_ZD01.equals(addrData.getId().getAddrType()) || MQMsgConstants.ADDR_ZS02.equals(addrData.getId().getAddrType())) {
        if (!StringUtils.isEmpty(addrData.getCustPhone())) {
          line3 = !StringUtils.isEmpty(line3) ? (addrData.getCustPhone() + " " + line3) : addrData.getCustPhone();
        }
      }
      line4 = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";

      line5 = !StringUtils.isEmpty(addrData.getAddrTxt2()) ? addrData.getAddrTxt2() : "";
      if (!StringUtils.isEmpty(line5) && !StringUtils.isEmpty(addrData.getPoBox())) {
        line5 += ", " + addrData.getPoBox();
      } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
        line5 = addrData.getPoBox();
      }

      line6 = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
      if (!StringUtils.isEmpty(line6) && !StringUtils.isEmpty(addrData.getPostCd())) {
        line6 += ", " + addrData.getPostCd();
      }

    } else {
      // Cross-border - CEWA
      // line2 = Name Con't
      // line3 = Street
      // line4 = Street Con't + PO BOX
      // line5 = City / Postal Code or both
      // line6 = State (Country)

      line2 = !StringUtils.isEmpty(addrData.getCustNm2()) ? addrData.getCustNm2() : "";
      line3 = !StringUtils.isEmpty(addrData.getAddrTxt()) ? addrData.getAddrTxt() : "";

      line4 = !StringUtils.isEmpty(addrData.getAddrTxt2()) ? addrData.getAddrTxt2() : "";
      if (!StringUtils.isEmpty(line4) && !StringUtils.isEmpty(addrData.getPoBox())) {
        line4 += ", " + addrData.getPoBox();
      } else if (!StringUtils.isEmpty(addrData.getPoBox())) {
        line4 = addrData.getPoBox();
      }

      line5 = !StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "";
      if (!StringUtils.isEmpty(line5) && !StringUtils.isEmpty(addrData.getPostCd())) {
        line5 += ", " + addrData.getPostCd();
      }
      line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    for (String line : lines) {
      messageHash.put(addrKey + "Address" + lineNo, line);
      lineNo++;
    }
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  /**
   * Determines if this is a GM LLC process
   * 
   * @param handler
   * @return
   */
  protected boolean isGMLLCProcess(MQMessageHandler handler) {
    boolean create = MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType());
    Data cmrData = handler.cmrData;

    // should be
    // a. not a correlated request
    // b. create and scenario is GM LLC - ends with "LLC"
    // c. update and abbreviated name ends with GM
    return StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())
        && !SystemLocation.KENYA.equals(cmrData.getCmrIssuingCntry())
        && ((create && !StringUtils.isEmpty(cmrData.getCustSubGrp()) && cmrData.getCustSubGrp().endsWith("LLC")) || (!create && cmrData != null
            && cmrData.getAbbrevNm() != null && cmrData.getAbbrevNm().toUpperCase().endsWith(" GM")));
  }

  /**
   * Calls the Generate CMR no service to get the next available CMR no
   * 
   * @param handler
   * @param targetSystem
   * @return
   */
  protected String generateCMRNoForGMLLC(MQMessageHandler handler, String targetSystem) {

    try {
      GenerateCMRNoRequest request = new GenerateCMRNoRequest();
      request.setLoc1(handler.mqIntfReqQueue.getCmrIssuingCntry());
      request.setLoc2(targetSystem);
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setSystem(GenerateCMRNoClient.SYSTEM_SOF);

      GenerateCMRNoClient client = CmrServicesFactory.getInstance().createClient(BaseBatchService.BATCH_SERVICE_URL, GenerateCMRNoClient.class);

      GenerateCMRNoResponse response = client.executeAndWrap(request, GenerateCMRNoResponse.class);

      if (response.isSuccess()) {
        return response.getCmrNo();
      } else {
        LOG.error("CMR No cannot be generated. Error: " + response.getMsg());
        return null;
      }
    } catch (Exception e) {
      LOG.error("Error in generating CMR no", e);
      return null;
    }

  }

  protected boolean isCrossBorder(Addr addr) {
    String cd = MCOHandler.LANDED_CNTRY_MAP.get(getCmrIssuingCntry());
    return cd != null && !cd.equals(addr.getLandCntry());
  }

  @Override
  public String getSysLocToUse() {
    return getCmrIssuingCntry();
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "2";
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mail";
    case "ZI01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "Soft";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mailing";
    case "ZI01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }
}
