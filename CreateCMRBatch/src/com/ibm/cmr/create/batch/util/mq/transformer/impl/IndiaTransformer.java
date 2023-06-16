package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

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

public class IndiaTransformer extends ISATransformer {
  private static final Logger LOG = Logger.getLogger(IndiaTransformer.class);

  public IndiaTransformer() throws Exception {
    super(SystemLocation.INDIA);

  }

  @Override
  protected String getDoubleCreateCountry(EntityManager entityManager, MQMessageHandler handler) {
    if (handler.cmrData.getCmrIssuingCntry().equals(SystemLocation.INDIA) && "C".equals(handler.adminData.getReqType())) // INDIA
      if (handler.cmrData.getCustSubGrp().equals("ESOSW") || handler.cmrData.getCustSubGrp().equals("XESO"))
        return SystemLocation.SINGAPORE;
    return "";
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    handleDataDefaults(handler);
    handler.messageHash.put("CntryNo", SystemLocation.INDIA);
    String cmrPrefix = !StringUtils.isEmpty(handler.cmrData.getCmrNoPrefix()) ? handler.cmrData.getCmrNoPrefix() : "";
    String inacType = !StringUtils.isEmpty(handler.cmrData.getInacType()) ? handler.cmrData.getInacType() : "";

    // handle reprocessing of dual create request when cmr number supplied
    if (MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus()) && isDoubleCreateProcess(handler)
        && !StringUtils.isBlank(handler.cmrData.getCmrNo())) {
      handler.messageHash.put("TransCode", "N");
    }
    // only generate CMR no for Dual Creates when cmr prefix is supplied
    if (MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus()) && isDoubleCreateProcess(handler) && !"".equals(cmrPrefix)) {
      LOG.debug("ASL/OEM scenario for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID " + handler.cmrData.getId().getReqId());
      String cmrNo = generateCMRNoForASLOEM(handler, SystemLocation.SINGAPORE);
      if (cmrNo == null) {
        LOG.warn("Warning, a CMR No cannot be generated for the ASL/OEM scenario.");
      } else {
        LOG.info("CMR No " + cmrNo + " found as next available for ASL/OEM.");
        handler.messageHash.put("CustNo", cmrNo);
        handler.messageHash.put("TransCode", "N");
      }
    } else if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
      // for correlated requests, add the CN
      LOG.debug(
          "Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. " + handler.mqIntfReqQueue.getCmrNo());
      String cmrNo = handler.mqIntfReqQueue.getCmrNo();
      handler.messageHash.put("CustNo", cmrNo);
      handler.messageHash.put("TransCode", "N");
    }

    if (StringUtils.isNotBlank(handler.mqIntfReqQueue.getCmrNo())) {
      LOG.debug("CREATECMR - 9713 INDIA MQ ISSUE LOGS -----> " + handler.mqIntfReqQueue.getCmrNo().startsWith("P"));
    }
    LOG.debug("Values before -----> ");
    LOG.debug("Values before -----> handler.cmrData.getCmrNo -> " + handler.cmrData.getCmrNo());
    LOG.debug("Values before -----> handler.mqIntfReqQueue.getCmrNo -> " + handler.mqIntfReqQueue.getCmrNo());
    LOG.debug("Values before -----> handler.messageHash.get(CustNo) -> " + handler.messageHash.get("CustNo"));

    if ("Y".equalsIgnoreCase(handler.adminData.getProspLegalInd()) && MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && StringUtils.isNotBlank(handler.mqIntfReqQueue.getCmrNo()) && handler.mqIntfReqQueue.getCmrNo().startsWith("P")) {
      LOG.debug("CREATECMR - 9713 INDIA MQ ISSUE LOGS ----> cmr number made null");
      String cmr = null;
      handler.cmrData.setCmrNo(cmr);
      handler.mqIntfReqQueue.setCmrNo(cmr);
      handler.messageHash.put("CustNo", cmr);
    }
  }

  /**
   * Calls the Generate CMR no service to get the next available CMR no
   * 
   * @param handler
   * @param targetSystem
   * @return
   */
  protected String generateCMRNoForASLOEM(MQMessageHandler handler, String targetSystem) {

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
    // b. create and scenario is ASL/OEM
    return StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && !SystemLocation.SINGAPORE.equals(cmrData.getCmrIssuingCntry())
        && (create && !StringUtils.isEmpty(cmrData.getCustSubGrp())
            && ("ESOSW".equalsIgnoreCase(cmrData.getCustSubGrp()) || "XESO".equalsIgnoreCase(cmrData.getCustSubGrp())));
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    handleAddressDefaults(handler);
    Addr addrData = handler.addrData;

    String line6 = "";
    if (!StringUtils.isBlank(addrData.getCity1())) {
      line6 += addrData.getCity1();
    }
    line6 = line6 + " ";
    if (!StringUtils.isBlank(addrData.getPostCd())) {
      line6 += addrData.getPostCd();
    }
    handler.messageHash.put("AddrLine6", line6);
    handleMove(handler, "ISA");
  }
}
