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
public class CEWATransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(CEWATransformer.class);

  private boolean isLDEnabled = true;

  public CEWATransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData CEWA Africa transformer...");

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
    LOG.debug("transformLegacyAddressData CEWA Africa transformer...");
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    LOG.debug("transformLegacyAddressData CEWA Africa calling formatDataLines...");
    super.formatDataLines(handler);
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    LOG.debug("transformLegacyAddressData CEWA Africa calling formatAddressLines...");
    super.formatAddressLines(handler);
  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For CEWA Africa...");
    if (custSubGrp != null && "INTER".equals(custSubGrp) || custSubGrp != null && "XINTE".equals(custSubGrp)) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

}
