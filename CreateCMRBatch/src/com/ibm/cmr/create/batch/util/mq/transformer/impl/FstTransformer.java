/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FstTransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(FstTransformer.class);

  protected static final String[] ADDRESS_ORDER = { "ZI01", "ZP01", "ZS01", "ZD01", "ZS02" };
  protected static final String[] ADDRESS_ORDER_LD = { "ZS02", "ZP01", "ZS01", "ZD01", "ZI01" };

  private boolean isLDEnabled = true;

  public FstTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData FST Africa transformer...");

    legacyCust.setLeadingAccNo("");
    legacyCust.setMrcCd("");

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        legacyCust.setTelNoOrVat(addr.getCustPhone());
      }
    }

  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData FST Africa transformer...");
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }
  }

  @Override
  public String getAddressUse(Addr addr) {
    if (isLDEnabled) {
      LOG.info("getAddressUse -  LD - FST Africa ");
      return getAddressUseLD(addr);
    } else {
      LOG.info("getAddressUse -  MQ - FST Africa ");
      return getAddressUseMQ(addr);
    }
  }

  private String getAddressUseLD(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  private String getAddressUseMQ(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  @Override
  public String[] getAddressOrder() {
    if (isLDEnabled) {
      LOG.info("getAddressOrder -  LD - FST Africa ");
      return ADDRESS_ORDER_LD;
    } else {
      LOG.info("getAddressOrder -  MQ - FST Africa ");
      return ADDRESS_ORDER;
    }
  }

  @Override
  public String getAddressKey(String addrType) {
    if (isLDEnabled) {
      LOG.info("getAddressKey -  LD - FST Africa ");
      return getAddressKeyLD(addrType);
    } else {
      LOG.info("getAddressKey -  MQ - FST Africa ");
      return getAddressKeyMQ(addrType);
    }
  }

  private String getAddressKeyMQ(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "Mail";
    case "ZD01":
      return "Ship";
    case "ZS01":
      return "Install";
    case "ZS02":
      return "Soft";
    default:
      return "";
    }
  }

  private String getAddressKeyLD(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "EPL";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "Mailing";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    if (isLDEnabled) {
      LOG.info("getTargetAddressType -  LD -  FST Africa");
      return getTargetAddressTypeLD(addrType);
    } else {
      LOG.info("getTargetAddressType -  MQ -  FST Africa ");
      return getTargetAddressTypeMQ(addrType);
    }
  }

  private String getTargetAddressTypeMQ(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }

  private String getTargetAddressTypeLD(String addrType) {
    switch (addrType) {
    case "ZP01":
      return "Billing";
    case "ZI01":
      return "Mailing";
    case "ZD01":
      return "Shipping";
    case "ZS01":
      return "Installing";
    case "ZS02":
      return "EPL";
    default:
      return "";
    }
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For FST Africa...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "XINTE".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }
}
