/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Map;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * {@link MessageTransformer} implementation for Cyprus.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CyprusTransformer extends GreeceTransformer {
  
  private static final Logger LOG = Logger.getLogger(CyprusTransformer.class);

  private static final String[] ADDRESS_ORDER = { "ZS01", "ZD01" };

  public CyprusTransformer() {
    super(SystemLocation.CYPRUS);
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.CYPRUS;
  }

  @Override
  public void formatAddressLines(MQMessageHandler mqHandler) {
    super.formatAddressLines(mqHandler);
    mqHandler.messageHash.put("SourceCode", "EFW");
    Addr addrData = mqHandler.addrData;
    Data cmrData = mqHandler.cmrData;
    Map<String, String> messageHash = mqHandler.messageHash;

    // vat
    if (MQMsgConstants.ADDR_ZS01.equals(addrData.getId().getAddrType())) {
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressU", !StringUtils.isEmpty(cmrData.getVat()) ? cmrData.getVat()
          : "");
    } else {
      messageHash.put(getTargetAddressType(addrData.getId().getAddrType()) + "AddressU", "");
    }

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    super.formatDataLines(handler);
    handler.messageHash.put("SourceCode", "EFW");
  }

  @Override
  protected boolean isCrossBorder(Addr addr) {
    return !"CY".equals(addr.getLandCntry());
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
    case "ZD01":
      return "Ship";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    default:
      return "";
    }
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "1";
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING + MQMsgConstants.SOF_ADDRESS_USE_BILLING + MQMsgConstants.SOF_ADDRESS_USE_INSTALLING
          + MQMsgConstants.SOF_ADDRESS_USE_SHIPPING + MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For CY...");
    if (custSubGrp != null
        && ("INTER".equals(custSubGrp) || "CRINT".equals(custSubGrp))) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(998999);
    }
  }
  
}
