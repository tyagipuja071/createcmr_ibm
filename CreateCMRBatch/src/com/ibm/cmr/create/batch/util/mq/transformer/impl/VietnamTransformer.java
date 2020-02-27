package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GenerateCMRNoClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoResponse;

public class VietnamTransformer extends ASEANTransformer {
  private static final Logger LOG = Logger.getLogger(VietnamTransformer.class);

  public VietnamTransformer() throws Exception {
    super(SystemLocation.VIETNAM);

  }

  @Override
  protected String getDoubleCreateCountry(EntityManager entityManager, MQMessageHandler handler) {
    if (handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM) && "C".equals(handler.adminData.getReqType())) // VIETNAM
      return SystemLocation.SINGAPORE;
    return "";
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    handleDataDefaults(handler);
    handler.messageHash.put("CntryNo", SystemLocation.VIETNAM);
    String cmrPrefix = !StringUtils.isEmpty(handler.cmrData.getCmrNoPrefix()) ? handler.cmrData.getCmrNoPrefix() : "";
    String inacType = !StringUtils.isEmpty(handler.cmrData.getInacType()) ? handler.cmrData.getInacType() : "";

    /*
     * if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) &&
     * handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM) &&
     * !getLandedCntryInZS01(handler).equals(SystemLocation.CAMBODIA)) {
     * handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
     * handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
     * handler.messageHash.put("IBMCode", "V001");
     * handler.messageHash.put("ClusterNo", "");
     * handler.messageHash.put("GB_SegCode", "");
     * handler.messageHash.put("SalesmanNo", "000DSW");
     * handler.messageHash.put("SellBrnchOff", "000");
     * handler.messageHash.put("InstBrnchOff", "000");
     * handler.messageHash.put("CtryText", ""); if
     * ("N".equalsIgnoreCase(inacType)) { handler.messageHash.put("INAC", "");
     * handler.messageHash.put("NAC", ""); } } else if
     * (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) &&
     * handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM) &&
     * getLandedCntryInZS01(handler).equals(SystemLocation.CAMBODIA)) {
     * handler.messageHash.put("CntryNo", SystemLocation.SINGAPORE);
     * handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
     * handler.messageHash.put("IBMCode", "C001");
     * handler.messageHash.put("ClusterNo", "");
     * handler.messageHash.put("GB_SegCode", "");
     * handler.messageHash.put("SalesmanNo", "000DSW");
     * handler.messageHash.put("SellBrnchOff", "000");
     * handler.messageHash.put("InstBrnchOff", "000");
     * handler.messageHash.put("CtryText", ""); if
     * ("N".equalsIgnoreCase(inacType)) { handler.messageHash.put("INAC", "");
     * handler.messageHash.put("NAC", ""); } }
     */
    // String custSubGrp = handler.cmrData.getCustSubGrp();
    // String mrcCode = "";
    // if (handler.cmrData.getCustSubGrp() != null) {
    // if ("DUMMY".equalsIgnoreCase(custSubGrp) ||
    // "INTERNAL".equalsIgnoreCase(custSubGrp) ||
    // "BLUMX".equalsIgnoreCase(custSubGrp)
    // || "MKTPC".equalsIgnoreCase(custSubGrp) ||
    // "IGF".equalsIgnoreCase(custSubGrp))
    // mrcCode = "3";
    // }
    //
    // handler.messageHash.put("MrktRespCode", mrcCode);
    // handle reprocessing of dual create request when cmr number supplied
    if (MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus()) && isDoubleCreateProcess(handler)
        && !StringUtils.isBlank(handler.cmrData.getCmrNo())) {
      handler.messageHash.put("TransCode", "N");
    }
    // only generate CMR no for Dual Creates when cmr prefix is supplied
    if (MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus()) && isDoubleCreateProcess(handler) && !"".equals(cmrPrefix)) {
      LOG.debug("Local/Cross scenario for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID "
          + handler.cmrData.getId().getReqId());
      String cmrNo = generateCMRNoForDualCreate(handler, SystemLocation.SINGAPORE);
      if (cmrNo == null) {
        LOG.warn("Warning, a CMR No cannot be generated for the Local/Cross scenario.");
      } else {
        LOG.info("CMR No " + cmrNo + " found as next available for Local/Cross.");
        handler.messageHash.put("CustNo", cmrNo);
        handler.messageHash.put("TransCode", "N");
      }
    } else if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      // for correlated requests, add the CN
      LOG.debug("Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. "
          + handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("CustNo", handler.mqIntfReqQueue.getCmrNo());
      handler.messageHash.put("TransCode", "N");
    }
  }

  /**
   * Calls the Generate CMR no service to get the next available CMR no
   * 
   * @param handler
   * @param targetSystem
   * @return
   */
  protected String generateCMRNoForDualCreate(MQMessageHandler handler, String targetSystem) {

    try {
      String cmrPrefix = !StringUtils.isEmpty(handler.cmrData.getCmrNoPrefix()) ? handler.cmrData.getCmrNoPrefix() : "";
      int min = Integer.parseInt(cmrPrefix.substring(0, 3) + "000");
      int max = Integer.parseInt(cmrPrefix.substring(0, 3) + "999");
      GenerateCMRNoRequest request = new GenerateCMRNoRequest();
      request.setLoc1(handler.mqIntfReqQueue.getCmrIssuingCntry());
      request.setLoc2(targetSystem);
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setSystem("WTAAS");
      request.setMin(min);
      request.setMax(max);

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

  /**
   * Determines if this is a ASL/OEM scenario
   * 
   * @param handler
   * @return
   */
  protected boolean isDoubleCreateProcess(MQMessageHandler handler) {
    boolean create = MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType());
    Data cmrData = handler.cmrData;
    String cmrPrefix = !StringUtils.isEmpty(cmrData.getCmrNoPrefix()) ? cmrData.getCmrNoPrefix() : "";

    // should be
    // a. not a correlated request
    // b. create and all customer types
    return StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())
        && !SystemLocation.SINGAPORE.equals(cmrData.getCmrIssuingCntry())
        && (create && !StringUtils.isEmpty(cmrData.getCustGrp()) && ("LOCAL".equalsIgnoreCase(cmrData.getCustGrp()) || "CROSS"
            .equalsIgnoreCase(cmrData.getCustGrp())));
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    handleAddressDefaults(handler);

    Addr addrData = handler.addrData;

    String line6 = "";
    if (!StringUtils.isBlank(addrData.getCity1())) {
      line6 += addrData.getCity1();
    }
    line6 += " ";
    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }
    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ASEAN");

    // Defect 1735593: SG Pincode Issue
    /*
     * if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) &&
     * handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.VIETNAM)) {
     * String line66 = ""; if (!StringUtils.isBlank(addrData.getCity1())) {
     * line66 = addrData.getCity1(); line66 += " " + "<" +
     * LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">"; if
     * ("LA".equalsIgnoreCase(addrData.getLandCntry())) line66 =
     * addrData.getCity1() + " " + "<Laos>"; if (addrData.getPostCd() != null)
     * line66 += " " + addrData.getPostCd();
     * handler.messageHash.put("AddrLine6", line66); } else { line66 = "<" +
     * LandedCountryMap.getCountryName(addrData.getLandCntry()) + ">"; if
     * ("LA".equalsIgnoreCase(addrData.getLandCntry())) line66 =
     * addrData.getCity1() + " " + "<Laos>"; if (addrData.getPostCd() != null)
     * line66 += " " + addrData.getPostCd();
     * handler.messageHash.put("AddrLine6", line66); }
     * 
     * String line5 = ""; if (!StringUtils.isBlank(addrData.getDept())) { line5
     * += addrData.getDept(); }
     * 
     * handler.messageHash.put("AddrLine5", line5); String addr5 =
     * handler.messageHash.get("AddrLine5"); String addr6 =
     * handler.messageHash.get("AddrLine6");
     * 
     * if (addr5 == null) addr5 = ""; if (addr6 == null) addr6 = "";
     * 
     * if (handler.messageHash.get("AddrLine5").length() == 0 ||
     * handler.messageHash.get("AddrLine4").length() == 0) {
     * handler.messageHash.put("AddrLine5", addr6);
     * handler.messageHash.put("AddrLine6", ""); } }
     */
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
}
