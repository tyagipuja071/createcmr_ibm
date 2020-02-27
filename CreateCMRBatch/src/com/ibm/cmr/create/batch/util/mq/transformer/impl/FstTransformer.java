/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FstTransformer extends MCOTransformer {

  protected static final String[] ADDRESS_ORDER = { "ZI01", "ZP01", "ZS01", "ZD01", "ZS02" };

  public FstTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);

  }

  @Override
  public String getAddressUse(Addr addr) {
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
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
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

  @Override
  public String getTargetAddressType(String addrType) {
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
}
