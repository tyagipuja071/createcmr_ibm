/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * Transforms the current data/message on the handler using country specific
 * values
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class MessageTransformer {

  protected String cmrIssuingCntry;

  public MessageTransformer(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  /**
   * Transforms the current data/message on the handler with country specific
   * values if needed
   * 
   * @param handler
   * @param docNum
   */
  public abstract void formatDataLines(MQMessageHandler handler);

  /**
   * Transforms the address values/lines with country specific values if needed
   * 
   * @param mqHandler
   */
  public abstract void formatAddressLines(MQMessageHandler handler);

  /**
   * Gets the order of address types to use for querying the order of addresses
   * to send to the target system
   * 
   * @param cmrIssuingCntry
   * @return
   */
  public abstract String[] getAddressOrder();

  /**
   * Using the supplied address type, returns the Key to use on the message (not
   * needed by all countries)
   * 
   * @param addrType
   * @return
   */
  public abstract String getAddressKey(String addrType);

  /**
   * From the address type, returns the equivalent addrss type on the target
   * system
   * 
   * @param addrType
   * @return
   */
  public abstract String getTargetAddressType(String addrType);

  /**
   * Returns the system location to use for the give CMR issuing country
   * 
   * @param cmrIssuingCntry
   * @return
   */
  public abstract String getSysLocToUse();

  /**
   * Gets the CreateCMR issuing country for this handler
   * 
   * @return
   */
  public String getCmrIssuingCntry() {
    return this.cmrIssuingCntry;
  }

  /**
   * If the transformer returns a fixed value for the Prospect CMR address
   * sequence, returns a value greater than zero
   * 
   * @return
   */
  public abstract String getFixedAddrSeqForProspectCreation();

  /**
   * Gets the Address Use equivalent for the given address. Some countries have
   * aon type mapped to a single address use, others have multiple
   * 
   * @param addr
   * @return
   */
  public abstract String getAddressUse(Addr addr);

  /**
   * Reverses all numbers occuring on this text without changing the order
   * 
   * @param text
   * @return
   */
  protected String reverseNumbers(String text) {
    if (text == null) {
      return null;
    }
    StringBuilder sbFull = new StringBuilder();
    StringBuilder sbNum = new StringBuilder();

    String sub = null;
    for (int i = 0; i < text.length(); i++) {

      sub = i == text.length() - 1 ? text.substring(i) : text.substring(i, i + 1);
      if (StringUtils.isNumeric(sub)) {
        sbNum.append(sub);
      } else {
        if (sbNum.length() > 0) {
          sbFull.append(StringUtils.reverse(sbNum.toString()));
        }
        sbFull.append(sub);
        sbNum.delete(0, sbNum.length());
      }
    }
    if (sbNum.length() > 0) {
      sbFull.append(StringUtils.reverse(sbNum.toString()));
      sbNum.delete(0, sbNum.length());
    }
    return sbFull.toString();
  }

  /**
   * Reverses all numbers occuring on this text without changing the order
   * 
   * @param text
   * @return
   */
  protected String reversePerWord(String text) {
    if (text == null) {
      return null;
    }
    StringBuilder sbFull = new StringBuilder();

    for (String part : text.split("[ ]")) {
      sbFull.append(sbFull.length() > 0 ? " " : "");
      sbFull.append(StringUtils.reverse(part));
    }
    return sbFull.toString();
  }

  /**
   * 
   * @return
   */
  public XMLOutputter getXmlOutputter(Format format) {
    return null;
  }

  /**
   * Returns true if the process should complete the flow
   * 
   * @param entityManager
   *          - entityManager to use for queries
   * @param handler
   *          - current handler
   * @param responseStatus
   *          - the current status being set by the handler
   * @param fromUpdateFlow
   *          - true if the call was made on the update flow and no more
   *          addresses left, false if from create and last success was
   *          receieved
   * @return
   */
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    return true;
  }

  /**
   * Determines if the next address is to be sent
   * 
   * @param entityManager
   * @param handler
   * @param nextAddr
   * @return
   */
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    return true;
  }

  /**
   * Sets any value that needs to be set after success
   * 
   * @param handler
   */
  public void setValuesAfterInitialSuccess(MQMessageHandler handler) {
    // noop, override as needed
  }

  /**
   * Called when determining if an address changed. If this method returns true,
   * the address will be sent even if there are no updates
   * 
   * @param handler
   * @param addrToCheck
   * @return
   */
  public boolean shouldForceSendAddress(MQMessageHandler handler, Addr addrToCheck) {
    return false;
  }

  /**
   * Called after basic mapping of Legacy CMR data to CreateCMR data. This is
   * where all data manipulations should take place on the {@link CmrtCust}
   * object
   * 
   * @param entityManager
   * @param legacyCust
   * @param cmrObjects
   */
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    // noop for default class, override
  }

  /**
   * Called after basic mapping of Legacy CMR Address to CreateCMR address. This
   * is where all address manipulations should take place on the
   * {@link CmrtAddr} object
   * 
   * @param entityManager
   * @param legacyCust
   * @param cmrObjects
   */
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    // noop for default class, override
  }

  /**
   * Called after basic mapping of data and addresses. This is where all address
   * manipulations should take place and add any other objects to the
   * {@link LegacyDirectObjectContainer}
   * 
   * @param entityManager
   * @param legacyCust
   * @param cmrObjects
   */
  public void transformOtherData(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects, CMRRequestContainer cmrObjects) {
    // noop for default class, override
  }

  /**
   * Called after basic mapping of data and addresses. This is where new CMR No
   * generation should take place on the {@link GenerateCMRNoRequest} object
   * 
   * @param entityManager
   * @param generateCMRNoObj
   * @param dataObjects
   */
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    // noop for default class, override
  }

  /**
   * Indicates whether the country of this transformer supports address links or
   * not
   * 
   * @return
   */
  public boolean hasAddressLinks() {
    return false;
  }

  public String handleVatMassUpdateChanges(String newStartingLetter, String legacyVat) {
    String fullVat = "";
    return fullVat;
  }

  public boolean isCrossBorderForMass(MassUpdtAddr addr, CmrtAddr legacyAddr) {
    return false;
  }

  public void handlePostCdSpecialLogic(CmrtCust cust, Data data, String postcd, EntityManager entityManager) {

  }

  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr muAddr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {
    // noop for default class, override
  }

  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust legacyCust, CMRRequestContainer cmrObjects,
      MassUpdtData muData) {
    // noop for default class, override
  }

  public void formatMassUpdateAddressLines(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr massUpdtAddr, boolean isFAddr) {
    // noop for default class, override
  }

  /**
   * Indicates whether the country of this transformer supports CmrtCustExt for
   * address or not
   * 
   * @return
   */
  public boolean hasCmrtCustExt() {
    return false;
  }

  /**
   * Called after basic mapping of Legacy CMR data to CreateCMR data. This is
   * where all data manipulations should take place on the {@link CmrtCustExt}
   * object
   * 
   * @param entityManager
   * @param legacyCustExt
   * @param cmrObjects
   */
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    // noop for default class, override
  }

  public boolean skipLegacyAddressData(EntityManager entityManager, CMRRequestContainer cmrObjects, Addr currAddr, boolean flag) {
    return false;
  }

  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {
    // noop for default class, override
  }

  public boolean hasCmrtCustExtErrorMessage(EntityManager entityManager, CmrtCust cust, CmrtCustExt custExt, boolean flag) {
    return false;
  }

  public boolean sequenceNoUpdateLogic(EntityManager entityManager, CMRRequestContainer cmrObjects, Addr currAddr, boolean flag) {
    return true;
  }

  public boolean enableTempReactOnUpdates() {
    return true;
  }

}
