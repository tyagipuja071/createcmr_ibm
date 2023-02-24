/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.ItalyHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cio.cmr.request.util.sof.GenericSOFMessageParser;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.handler.impl.SOFMessageHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;

/**
 * @author Dennis Natad
 * 
 */
public class ItalyTransformer extends EMEATransformer {

  private static final Logger LOG = Logger.getLogger(ItalyTransformer.class);

  // added marketing response code to preserve BP = 5 for updates
  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode", "ARemark", "IsBusinessPartner" };
  // jz: installing, billing, company, postal
  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZS02" };
  public static final String DEFAULT_LANDED_COUNTRY = "IT";
  protected String dummyFiscalCode = "";
  protected Map<String, String> dupCMRValues = new HashMap<String, String>();

  private boolean sofProcessingChecked;
  private boolean sofProcessingComplete;

  public static final String CMR_REQUEST_STATUS_CPR = "CPR";
  public static final String CMR_REQUEST_STATUS_PCR = "PCR";
  public static final String CMR_REQUEST_REASON_TEMP_REACT_EMBARGO = "TREC";

  private static final String DEFAULT_CLEAR_CHAR = "@";
  private static final String DEFAULT_CLEAR_6_CHAR = "@@@@@@";
  private static final String DEFAULT_CLEAR_4_CHAR = "@@@@";
  private static final String DEFAULT_CLEAR_IT_VAT = "@@@@@@@@@@@";
  private static final String DEFAULT_CLEAR_IT_FISCAL = "@@@@@@@@@@@@@@@@";

  public ItalyTransformer(String cmrIssuingCntry) {
    super(cmrIssuingCntry);
  }

  public ItalyTransformer() {
    super(SystemLocation.ITALY);
  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    if (!this.sofProcessingChecked) {
      this.sofProcessingComplete = checkSOFProcessing(handler.getEntityManager(), handler.mqIntfReqQueue.getReqId());
      this.sofProcessingChecked = true;
      LOG.debug(
          "SOF processing for Request " + handler.mqIntfReqQueue.getReqId() + " is " + (this.sofProcessingComplete ? " COMPLETE." : "Not Complete"));
    }
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Handling Data for " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    boolean crossBorder = isCrossBorder(addrData);

    handleEMEADefaults(handler, messageHash, cmrData, addrData, crossBorder);
    handleDataDefaults(handler, messageHash, cmrData, crossBorder, addrData);

    messageHash.remove("TaxCode"); // Remove TaxCode for Italy.
    messageHash.put("Affiliate", cmrData.getAffiliate());

    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    messageHash.put("EmbargoCode", embargoCode);

    if ("C".equals(handler.adminData.getReqType())) {
      if (cmrData.getCustSubGrp() != null && cmrData.getIsuCd() != null && "34".equals(cmrData.getIsuCd()) && ("COMME".equals(cmrData.getCustSubGrp())
          || "COMSM".equals(cmrData.getCustSubGrp()) || "COMVA".equals(cmrData.getCustSubGrp()) || "CROCM".equals(cmrData.getCustSubGrp()))) {
        messageHash.put("MarketingResponseCode", "M");
      }
    }

    if (update) {
      // send the current marketing response code to avoid loss
      String currMrc = handler.currentCMRValues.get("MarketingResponseCode");
      LOG.debug("Current MRC: " + currMrc);
      if (!StringUtils.isEmpty(currMrc)) {
        messageHash.put("MarketingResponseCode", currMrc);
      } else {
        messageHash.put("MarketingResponseCode", "2");
      }

      messageHash.put("ModeOfPayment", !StringUtils.isEmpty(cmrData.getModeOfPayment()) ? cmrData.getModeOfPayment() : "");

      boolean ifPureDataUpdate = checkIfPureDataUpdate(handler, handler.getEntityManager(), handler.mqIntfReqQueue.getReqId());
      if (ifPureDataUpdate) {
        messageHash.put("LastDoc", "Y");
        LOG.debug("Adding LastDoc=Y on data Xml");
      }
      for (String field : NO_UPDATE_FIELDS) {
        messageHash.remove(field);
      }
    }
    // Defect 1478052: IT, VA, SM: Under created BP CMR 'Auth. Remarketer' is
    // 'NO' in SOF :Mukesh
    if ("C".equals(handler.adminData.getReqType())) {
      setValueByScenario(messageHash, cmrData.getCustSubGrp(), cmrData.getCountryUse());
    }
    if (MQMsgConstants.REQ_TYPE_UPDATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus())) {
      if (isDoubleUpdate(handler) && !LegacyDirectUtil.isItalyLegacyDirect(handler.getEntityManager(), handler.adminData.getId().getReqId())) {
        if (StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
          LOG.debug("No correlation id found ,ITALY for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID "
              + handler.cmrData.getId().getReqId());
          // no action needed
        } else {
          LOG.debug("Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. "
              + handler.cmrData.getFiscalDataCompanyNo());
          messageHash.put("CustomerNo", handler.cmrData.getFiscalDataCompanyNo());
          messageHash.remove("EnterpriseNo");
          messageHash.remove("MarketingResponseCode");
          messageHash.remove("CustomerType");
          messageHash.remove("FSLICAM");
          messageHash.remove("DPCEBO");
          messageHash.remove("CollectionCode");
          messageHash.remove("CurrencyCode");
          messageHash.remove("DistrictCode");
          messageHash.remove("SBO");
          messageHash.remove("IBO");
          messageHash.remove("SR");
          messageHash.remove("Affiliate");
          messageHash.remove("IsBusinessPartner");
          messageHash.remove("LeasingCompany");
          messageHash.remove("EmbargoCode");
          messageHash.remove("ISU");
          messageHash.remove("VAT");
          messageHash.remove("CompanyName");
          messageHash.remove("IMS");
          messageHash.remove("INAC");
          messageHash.remove("ISIC");
          messageHash.remove("PrintSequenceNo");
          messageHash.remove("LenovoOnly");
          messageHash.remove("AbbreviatedLocation");
          messageHash.remove("ARemark");
          messageHash.remove("AccAdBo");
          messageHash.remove("ModeOfPayment");
        }
      }
    }
    changeToUpdateForCompletedSOFProcessing(handler);
  }

  private void setValueByScenario(Map<String, String> messageHash, String scenario, String country) {
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(scenario) || MQMsgConstants.CUSTSUBGRP_BUSSM.equals(scenario)
        || MQMsgConstants.CUSTSUBGRP_BUSVA.equals(scenario)) {
      messageHash.put(MQMsgConstants.AUTH_REMARK, "1");
    } else {
      messageHash.put(MQMsgConstants.AUTH_REMARK, "0");
    }
  }

  /**
   * Handles default data values
   * 
   * @param handler
   * @param messageHash
   * @param cmrData
   * @param crossBorder
   * @param addrData
   */
  protected void handleDataDefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData) {

    messageHash.put("SourceCode", "EFO");
    messageHash.put("MarketingResponseCode", StringUtils.isEmpty(cmrData.getMrcCd()) ? "2" : cmrData.getMrcCd());
    messageHash.put("CustomerType", cmrData.getCrosSubTyp());

    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(cmrData.getCustSubGrp()) || "CROBP".equals(cmrData.getCustSubGrp())) {
      messageHash.put("MarketingResponseCode", "5");
    }

    messageHash.put("FSLICAM", cmrData.getAbbrevNm());
    messageHash.put("DPCEBO", StringUtils.isEmpty(cmrData.getEngineeringBo()) ? "8412" : cmrData.getEngineeringBo());
    messageHash.put("CollectionCode", "");
    messageHash.put("CurrencyCode", "");
    messageHash.put("DistrictCode", "");

    if (cmrData.getSalesBusOffCd() != null && !"".equals(cmrData.getSalesBusOffCd()) && cmrData.getSalesBusOffCd().length() <= 2) {
      messageHash.put("SBO", "0" + cmrData.getSalesBusOffCd() + "B000");
      messageHash.put("IBO", "0" + cmrData.getSalesBusOffCd() + "B000");
    } else {
      messageHash.put("SBO", cmrData.getSalesBusOffCd());
      messageHash.put("IBO", cmrData.getSalesBusOffCd());
    }

    messageHash.put("SR", cmrData.getRepTeamMemberNo());
    messageHash.put("Affiliate", cmrData.getAffiliate());

    // jz: added on data level
    messageHash.put("IsBusinessPartner", "N");

    // 0 Confirmed by Ladislava on slack
    messageHash.put("LeasingCompany", "0");
    messageHash.put("CEdivision", "2"); // CCEDA
    messageHash.put("RepeatAgreement", "0"); // CAGXB
    messageHash.put("VAT", cmrData.getVat());
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    if (!this.sofProcessingChecked) {
      this.sofProcessingComplete = checkSOFProcessing(handler.getEntityManager(), handler.mqIntfReqQueue.getReqId());
      this.sofProcessingChecked = true;
      LOG.debug(
          "SOF processing for Request " + handler.mqIntfReqQueue.getReqId() + " is " + (this.sofProcessingComplete ? " COMPLETE." : "Not Complete"));
    }

    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Handling Address for " + (update ? "update" : "create") + " request.");

    if (update) {
      List<String> addrXMLList = countTotalAddrXmlToSend(handler, handler.getEntityManager());
      LOG.debug("List of address to be sent" + addrXMLList);
      LOG.debug("Last address to be sent= " + addrXMLList.get(addrXMLList.size() - 1) + " Current Address= " + addrData.getId().getAddrType());
      if (addrXMLList.size() != 0 && addrXMLList.get(addrXMLList.size() - 1) == addrData.getId().getAddrType()) {
        messageHash.put("LastDoc", "Y");
        LOG.debug("adding LastDoc=Y on Addr Xml for type" + addrData.getId().getAddrType());
      }
    } else if (!update && ItalyHandler.COMPANY_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
      messageHash.put("LastDoc", "Y");
      LOG.debug("Adding LastDoc=Y on Addr Xml for Company");
    }

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    messageHash.put("SourceCode", "EFO");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");
    messageHash.remove(addrKey + "Phone");
    messageHash.remove(addrKey + "Country");
    messageHash.remove("EnterpriseNo");
    messageHash.remove("VAT");

    messageHash.put(addrKey + "Address1", addrData.getCustNm1());
    messageHash.put(addrKey + "Address2", setAsteriskIfEmpty(addrData.getCustNm2()));
    messageHash.put(addrKey + "Address3", "*"); // always empty
    messageHash.put(addrKey + "Address4", addrData.getAddrTxt());

    if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
      if (addrData.getLandCntry() != null && !"".equals(addrData.getLandCntry()) && !"IT".equalsIgnoreCase(addrData.getLandCntry())) {
        messageHash.put(addrKey + "Address5",
            !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPoBoxPostCd() + " " + addrData.getPostCd() + " " + addrData.getCity1()
                : addrData.getPoBoxPostCd() + " " + "00000 " + addrData.getCity1());
      } else {
        messageHash.put(addrKey + "Address5",
            !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() + " " + addrData.getCity1() : "00000 " + addrData.getCity1());
      }
    }

    if (ItalyHandler.BILLING_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
      if (addrData.getLandCntry() != null && !"".equals(addrData.getLandCntry()) && "IT".equalsIgnoreCase(addrData.getLandCntry())) {
        messageHash.put(addrKey + "Address5",
            !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() + " " + addrData.getCity1() : "00000 " + addrData.getCity1());
      } else {
        messageHash.put(addrKey + "Address5", "00000 " + addrData.getPostCd() + " " + addrData.getCity1());
      }
    }

    if (ItalyHandler.INSTALLING_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
      if (addrData.getLandCntry() != null && !"".equals(addrData.getLandCntry()) && "IT".equalsIgnoreCase(addrData.getLandCntry())) {
        messageHash.put(addrKey + "Address5",
            !StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() + " " + addrData.getCity1() : "00000 " + addrData.getCity1());
      } else {
        messageHash.put(addrKey + "Address5", "00000 " + addrData.getPostCd() + " " + addrData.getCity1());
      }
    }

    // Prod Defect 1711162
    messageHash.put(addrKey + "Address6", LandedCountryMap.getCountryName(addrData.getLandCntry()));

    if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrData.getId().getAddrType())
        || ItalyHandler.BILLING_ADDR_TYPE.equals(addrData.getId().getAddrType())) {

      if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrData.getId().getAddrType()) && "N".equalsIgnoreCase(addrData.getImportInd())) {
        MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
        if (currentQ.getCmrNo() != null) {
          cmrData.setCompany(currentQ.getCmrNo());
        }
      }

      if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
        messageHash.put("IdentClient", cmrData.getIdentClient());
        if (addrData.getLandCntry().equals("IT")) {
          messageHash.put("CompanyProvinceCode", addrData.getStateProv());
        } else {
          messageHash.put("CompanyProvinceCode", "");
        }
        if ("".equals(checkIfDummyFiscalCodeAssigned(handler))) {
          messageHash.put("CodiceFiscale", cmrData.getTaxCd1());
        } else {
          LOG.debug("Setting dummy fiscal code for VA" + checkIfDummyFiscalCodeAssigned(handler));
          messageHash.put("CodiceFiscale", checkIfDummyFiscalCodeAssigned(handler));
        }
        messageHash.put("EnterpriseNo", cmrData.getEnterprise());

        if (cmrData.getVat() != null && !"".equals(cmrData.getVat()) && cmrData.getVat().length() == 13) {
          messageHash.put("VAT", cmrData.getVat().substring(2, 13));
        } else {
          messageHash.put("VAT", cmrData.getVat());
        }
        messageHash.put("CompanyNo", cmrData.getCompany());
      } else if (ItalyHandler.BILLING_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
        messageHash.put("BillingNameAbbr", addrData.getBldg());
        messageHash.put("BillingStreetAbbr", addrData.getDivn());
        messageHash.put("BillingLocAbbr", addrData.getCustFax());

        if (addrData.getLandCntry().equals("IT")) {
          messageHash.put("BillingProvinceCode", addrData.getStateProv());
        } else {
          messageHash.put("BillingProvinceCode", "");
        }
        // TODO update this with the value
        messageHash.put("PostalAddress", addrData.getBillingPstlAddr());
        messageHash.put("SSVCode", cmrData.getCollectionCd());
        messageHash.put("IVA", cmrData.getSpecialTaxCd());
        messageHash.put("BillingNo", getBillingNo(handler));
      }

      if (update && addrData.getId().getAddrSeq() != null && !"".equals(addrData.getId().getAddrSeq())
          && ("00001".equals(addrData.getId().getAddrSeq()) || "1".equals(addrData.getId().getAddrSeq()))) {
        // this address should be sent as create
        LOG.debug("Address " + addrData.getId().getAddrType() + " (" + addrData.getId().getAddrSeq()
            + ") to be sent as create. Setting to TransactionCode = N");
        messageHash.put("TransactionCode", MQMsgConstants.SOF_TRANSACTION_NEW);
        messageHash.put("AddressNumber", "-----");
        messageHash.put("Operation", MQMsgConstants.MQ_OPERATION_C);
      }

      // Defect 1558309 : For Creates only Company#/Billing# should be send in
      // case the address is imported
      if (!update && "Y".equals(addrData.getImportInd())) {
        // send address lines as empty when create and address is imported.
        messageHash.put(addrKey + "Address1", "$");
        messageHash.put(addrKey + "Address2", "$");
        messageHash.put(addrKey + "Address3", "$");
        messageHash.put(addrKey + "Address4", "$");
        messageHash.put(addrKey + "Address5", "$");
        messageHash.put(addrKey + "Address6", "$");
        if (ItalyHandler.BILLING_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
          messageHash.remove("BillingNameAbbr");
          messageHash.remove("BillingStreetAbbr");
          messageHash.remove("BillingLocAbbr");
          messageHash.remove("BillingProvinceCode");
          messageHash.remove("PostalAddress");
          messageHash.remove("SSVCode");
          messageHash.remove("IVA");
        }
        if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
          messageHash.remove("CompanyProvinceCode");
          messageHash.remove("IdentClient");
          messageHash.remove("CodiceFiscale");
          messageHash.remove("EnterpriseNo");
          messageHash.remove("VAT");
        }
      }

      if (MQMsgConstants.REQ_TYPE_UPDATE.equals(handler.mqIntfReqQueue.getReqType())
          && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus())) {
        if (isDoubleUpdate(handler)) {
          if (StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
            LOG.debug("No correlation id found ,ITALY for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID "
                + handler.cmrData.getId().getReqId());
            // no action needed
          } else {
            LOG.debug("Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. "
                + handler.cmrData.getFiscalDataCompanyNo());
            messageHash.put(addrKey + "Address1", "*");
            messageHash.put(addrKey + "Address2", "*");
            messageHash.put(addrKey + "Address3", "*");
            messageHash.put(addrKey + "Address4", "*");
            messageHash.put(addrKey + "Address5", "*");
            messageHash.put(addrKey + "Address6", "*");
            if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrData.getId().getAddrType())) {
              messageHash.remove("CompanyProvinceCode");
              messageHash.remove("IdentClient");
              messageHash.remove("CodiceFiscale");
              messageHash.remove("EnterpriseNo");
              messageHash.remove("VAT");
            }
          }
        }
      }
    }
    if (addrData.getLandCntry().equals("IT")) {
      messageHash.put("ProvinceCode", addrData.getStateProv());
    } else {
      messageHash.put("ProvinceCode", "");
    }
    changeToUpdateForCompletedSOFProcessing(handler);
  }

  /**
   * Gets the billing no from the billing address
   * 
   * @param handler
   * @return
   */
  private String getBillingNo(MQMessageHandler handler) {
    if (handler.currentAddresses != null) {
      for (Addr addr : handler.currentAddresses) {
        if (ItalyHandler.BILLING_ADDR_TYPE.equals(addr.getId().getAddrType())) {
          if (addr.getParCmrNo() != null && !"".equals(addr.getParCmrNo())) {
            return addr.getParCmrNo();
          } else {
            // pure creation, send the Installing CMR number as billing number
            MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
            if (currentQ.getCmrNo() != null && CmrConstants.REQ_TYPE_CREATE.equals(currentQ.getReqType())) {
              return currentQ.getCmrNo();
            }
            return "";
          }
        }
      }
    }
    return "";
  }

  /**
   * Gets the billing cmr no from the billing address for legacy direct
   * 
   * @param handler
   * @return
   */
  private String getBillingNoLegacyDirect(MQMessageHandler handler) {
    if (handler.currentAddresses != null) {
      for (Addr addr : handler.currentAddresses) {
        if (ItalyHandler.BILLING_ADDR_TYPE.equals(addr.getId().getAddrType())) {
          if (addr.getImportInd().equals("Y") && StringUtils.isNotBlank(handler.adminData.getModelCmrNo())) {
            return handler.adminData.getModelCmrNo();
          } else {
            // pure creation, send the New CMR number as billing number
            MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
            if (currentQ.getCmrNo() != null && CmrConstants.REQ_TYPE_CREATE.equals(currentQ.getReqType())) {
              return currentQ.getCmrNo();
            }
            return "";
          }
        }
      }
    }
    return "";
  }

  @Override
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    // need to handle the address to be sent based on the scenario
    MqIntfReqQueue queue = handler.mqIntfReqQueue;
    boolean create = CmrConstants.REQ_TYPE_CREATE.equals(queue.getReqType());
    if (create) {

      // return !"Y".equals(nextAddr.getImportInd()); - Defect 1558309
      return true;
    } else {
      return true;
    }
  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Installing";
    case "ZI01":
      return "Company";
    case "ZP01":
      return "Billing"; // jz: based on LOV
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Installing";
    case "ZI01":
      return "Company";
    case "ZP01":
      return "Billing";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.ITALY;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZP01:
      // Billing = B
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZI01:
      // Company = C
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_C;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  private boolean isDoubleUpdate(MQMessageHandler handler) {
    return false;
  }

  protected boolean isCrossBorder(Addr addr) {
    return !"IT".equals(addr.getLandCntry());
  }

  protected boolean isCrossBorderIT(String billLandCntry) {
    return !"IT".equals(billLandCntry);
  }

  protected String setAsteriskIfEmpty(String value) {
    if (StringUtils.isEmpty(value)) {
      return "*";
    } else {
      return value;
    }
  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    if (MQMsgConstants.SOF_STATUS_ANA.equals(responseStatus)) {
      if (isDoubleUpdate(handler)) {
        try {
          if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
            return true;
          }
          MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
          if ("Y".equals(currentQ.getMqInd()) || MQMsgConstants.REQ_STATUS_COM.equals(currentQ.getReqStatus())) {
            LOG.debug("MQ record already previously completed, skipping double updation process.");
            return true;
          }
          LOG.debug("Completing initial request " + currentQ.getId().getQueryReqId());
          Timestamp ts = SystemUtil.getCurrentTimestamp();
          currentQ.setReqStatus(MQMsgConstants.REQ_STATUS_COM);
          currentQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
          currentQ.setLastUpdtTs(ts);
          currentQ.setMqInd("Y");

          MqIntfReqQueue it758Q = new MqIntfReqQueue();
          MqIntfReqQueuePK it758QPk = new MqIntfReqQueuePK();
          long id = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "QUERY_REQ_ID", "CREQCMR");
          LOG.debug("Creating Correlation Update request for Italy with MQ ID " + id);
          it758QPk.setQueryReqId(id);
          it758Q.setId(it758QPk);

          it758Q.setCmrIssuingCntry("758");
          it758Q.setCmrNo(handler.cmrData.getFiscalDataCompanyNo());
          it758Q.setCorrelationId(currentQ.getId().getQueryReqId() + "");
          it758Q.setCreateBy(MQMsgConstants.MQ_APP_USER);
          it758Q.setCreateTs(ts);
          it758Q.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
          it758Q.setLastUpdtTs(ts);
          it758Q.setMqInd("N");
          it758Q.setReqId(currentQ.getReqId());
          it758Q.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
          it758Q.setReqType(currentQ.getReqType());
          it758Q.setTargetSys(currentQ.getTargetSys());

          handler.createPartialComment("Handling correlated update as Fiscal data on the request is Newer", handler.mqIntfReqQueue.getCmrNo());
          entityManager.merge(currentQ);
          entityManager.persist(it758Q);
          entityManager.flush();

          return false;
        } catch (Exception e) {
          LOG.error("Error in completing Correlated Update request for Italy. Skipping correlated Update request creation and completing request", e);
          return true;
        }
      } else {
        return true;
      }
    } else {
      try {
        // no anagrafico reply yet, add comment that SOF processing completed..
        handler.createPartialComment("SOF processing completed. Waiting for Anagrafico response.", handler.mqIntfReqQueue.getCmrNo());
        handler.skipPublish = true;
      } catch (Exception e) {
        LOG.error("Error in creating partial reply", e);
      }
      return false;
    }

  }

  private String checkIfDummyFiscalCodeAssigned(MQMessageHandler handler) {
    if (handler.cmrData.getCountryUse() != null && !"".equals(handler.cmrData.getCountryUse()) && handler.cmrData.getCountryUse().endsWith("VA")
        && CmrConstants.REQ_TYPE_CREATE.equals(handler.adminData.getReqType())) {
      boolean needQuerySOFAgain = true;
      boolean fiscalCodeIsFree = false;
      int tryCount = 1;
      while (needQuerySOFAgain && tryCount != 0) {
        dummyFiscalCode = "VT0000" + tryCount;
        fiscalCodeIsFree = loadDummyFiscalCode(handler.cmrData.getCmrNo(), handler.cmrData.getCmrIssuingCntry(), dummyFiscalCode);
        if (fiscalCodeIsFree == true) {
          needQuerySOFAgain = false;
          return dummyFiscalCode;
        } else {
          if (tryCount < 5) {
            tryCount++;
            needQuerySOFAgain = true;
          } else {
            needQuerySOFAgain = false;
            tryCount = 0;
            LOG.debug("some error,none of the 5 dummy fiscal code value is found to be free");
          }
        }
      }
      LOG.debug("Returning blank, as none of the fiscal code found free");
      return "";
    }
    // no condition is true , hence returning blank
    LOG.debug("no condition is true , hence returning blank");
    return "";
  }

  protected boolean loadDummyFiscalCode(String cmrNo, String dupCntry, String dummyFiscalCode) {
    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry(dupCntry);
    request.setCmrNo(cmrNo);
    request.setFiscalCode(dummyFiscalCode);

    LOG.info("Retrieving Legacy values for CMR Number having fiscal code = " + dummyFiscalCode + "CMR No=" + cmrNo + " from SOF (" + dupCntry + ")");

    try {
      SOFServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
          SOFServiceClient.class);
      SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
      if (response.isSuccess()) {
        String xmlData = response.getData();

        GenericSOFMessageParser dupHandler = new GenericSOFMessageParser();
        ByteArrayInputStream bis = new ByteArrayInputStream(xmlData.getBytes());
        try {
          SAXParserFactory factory = SAXParserFactory.newInstance();
          factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
          factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
          factory.newSAXParser().parse(new InputSource(bis), dupHandler);
        } finally {
          bis.close();
        }

        dupCMRValues = dupHandler.getValues();

        if (dupCMRValues != null && !dupCMRValues.isEmpty()) {
          String responseCmrNo = dupCMRValues.get("CustomerNo");
          if (StringUtils.isEmpty(responseCmrNo)) {
            LOG.debug("No CMR found with fiscal code i.e current fiscal code is free : " + dummyFiscalCode + " under ctyCode: " + dupCntry);
            return true;
          }
        }
      }
      LOG.debug("CMR found with fiscal code : " + dummyFiscalCode + " under ctyCode: " + dupCntry);
      return false; // need to query again to SOF.

    } catch (Exception e) {
      LOG.warn("An error has occurred during retrieval of the dummy fiscal code values.", e);
      dupCMRValues = new HashMap<String, String>();
      return false;
    }
  }

  @Override
  public boolean shouldForceSendAddress(MQMessageHandler handler, Addr addrToCheck) {
    if (addrToCheck == null) {
      return false;
    }
    if (MQMsgConstants.REQ_TYPE_UPDATE.equals(handler.mqIntfReqQueue.getReqType())) {
      String oldCollectionCd = "";
      String oldTaxCode = "";
      String oldIdentClient = "";
      String oldFiscalCd = "";
      String oldVat = "";
      String oldEnterprise = "";
      String oldCompany = "";
      try {
        String sql = ExternalizedQuery.getSql("QUERY.GETDATARDCVALUESIT");
        PreparedQuery query = new PreparedQuery(handler.getEntityManager(), sql);
        query.setParameter("REQ_ID", addrToCheck.getId().getReqId());
        query.setForReadOnly(true);
        List<Object[]> results = query.getResults();
        for (Object[] result : results) {
          oldCollectionCd = (String) result[0];
          oldTaxCode = (String) result[1];
          oldIdentClient = (String) result[2];
          oldFiscalCd = (String) result[3];
          oldVat = (String) result[4];
          oldEnterprise = (String) result[5];
          oldCompany = (String) result[6];
        }
        if (ItalyHandler.BILLING_ADDR_TYPE.equals(addrToCheck.getId().getAddrType())) {
          if ((oldCollectionCd != null && !oldCollectionCd.equals(handler.cmrData.getCollectionCd()))
              || (oldTaxCode != null && !oldTaxCode.equals(handler.cmrData.getSpecialTaxCd()))) {
            return true;
          }
        }

        if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrToCheck.getId().getAddrType())) {
          if ((oldFiscalCd != null && !oldFiscalCd.equals(handler.cmrData.getTaxCd1()))
              || (oldIdentClient != null && !oldIdentClient.equals(handler.cmrData.getIdentClient()))
              || (oldVat != null && !oldVat.equals(handler.cmrData.getVat()))
              || (oldEnterprise != null && !oldEnterprise.equals(handler.cmrData.getEnterprise()))
              || (oldCompany != null && !oldCompany.equals(handler.cmrData.getCompany()))) {
            return true;
          }
        }

        if (MQMsgConstants.REQ_TYPE_UPDATE.equals(handler.mqIntfReqQueue.getReqType())
            && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus())) {
          if (isDoubleUpdate(handler))
            if (StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
              LOG.debug("No correlation id found ,ITALY for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID "
                  + handler.cmrData.getId().getReqId() + " No address need to be sent on the correlated XML");
              return true;
            } else {
              LOG.debug("Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId()
                  + ", sending Company Address for the downgrade of CMR No. " + handler.cmrData.getFiscalDataCompanyNo());
              if (ItalyHandler.COMPANY_ADDR_TYPE.equals(addrToCheck.getId().getAddrType())) {
                return true;
              } else {
                return false;
              }
            }
        }
      } catch (Exception ex) {
        LOG.error(" Error is getting data rdc values for Italy on shouldForceSendAddress for Update Request " + ex);
        return false;
      }
    }
    return false;
  }

  /**
   * Checks if SOF processing has been completed or not
   * 
   * @param handler
   * @return
   */
  private boolean checkSOFProcessing(EntityManager entityManager, long reqId) {
    if (entityManager == null) {
      return false;
    }
    String sql = ExternalizedQuery.getSql("MQ.SOF.CHECK_COMPLETE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.exists();
  }

  private boolean checkIfPureDataUpdate(MQMessageHandler handler, EntityManager entityManager, long reqId) {
    if (entityManager == null) {
      return false;
    }
    String sql = ExternalizedQuery.getSql("CHECK_ADDR_IF_UPDATED");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    boolean res1 = !query.exists(); // if any address is updated
    boolean res2 = false;
    String oldCollectionCd = "";
    String oldTaxCode = "";
    String oldIdentClient = "";
    String oldFiscalCd = "";
    String oldVat = "";
    String oldEnterprise = "";
    String oldCompany = "";
    String sql2 = ExternalizedQuery.getSql("QUERY.GETDATARDCVALUESIT");
    PreparedQuery query2 = new PreparedQuery(handler.getEntityManager(), sql2);
    query2.setParameter("REQ_ID", handler.cmrData.getId().getReqId());
    query2.setForReadOnly(true);
    List<Object[]> results = query2.getResults();
    for (Object[] result : results) {
      oldCollectionCd = (String) result[0];
      oldTaxCode = (String) result[1];
      oldIdentClient = (String) result[2];
      oldFiscalCd = (String) result[3];
      oldVat = (String) result[4];
      oldEnterprise = (String) result[5];
      oldCompany = (String) result[6];
    }
    if ((oldCollectionCd != null && !oldCollectionCd.equals(handler.cmrData.getCollectionCd()))
        || (oldTaxCode != null && !oldTaxCode.equals(handler.cmrData.getSpecialTaxCd()))
        || (oldFiscalCd != null && !oldFiscalCd.equals(handler.cmrData.getTaxCd1()))
        || (oldIdentClient != null && !oldIdentClient.equals(handler.cmrData.getIdentClient()))
        || (oldVat != null && !oldVat.equals(handler.cmrData.getVat()))
        || (oldEnterprise != null && !oldEnterprise.equals(handler.cmrData.getEnterprise()))
        || (oldCompany != null && !oldCompany.equals(handler.cmrData.getCompany()))) {
      res2 = true; // forced update case
    }
    return (res1 && !res2);
  }

  private List<String> countTotalAddrXmlToSend(MQMessageHandler handler, EntityManager entityManager) {
    int addrCount = 0;
    List<Addr> totalAddr = ((SOFMessageHandler) handler).currentAddresses;
    LOG.debug("Total address=" + totalAddr.size());
    List<String> addressToSend = new ArrayList<String>();
    boolean ifAddrUpdated = false;
    boolean shared = false;
    if (totalAddr != null && !totalAddr.isEmpty()) {
      for (Addr addrToCheck : totalAddr) {
        ifAddrUpdated = RequestUtils.isUpdated(entityManager, addrToCheck, handler.cmrData.getCmrIssuingCntry());
        if (ifAddrUpdated == false) {
          ifAddrUpdated = shouldForceSendAddress(handler, addrToCheck);
        }
        if (ifAddrUpdated == false) {
          if (!CmrConstants.ADDR_TYPE.ZS01.toString().equals(addrToCheck.getId().getAddrType())) {
            LOG.debug("Checking if address shares sequence no with Main address for Italy");
            if (addrToCheck != null) {
              for (Addr currAddr : totalAddr) {
                if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(currAddr.getId().getAddrType())) {
                  // main address found
                  shared = addrToCheck.getId().getAddrSeq().equals(currAddr.getId().getAddrSeq());
                  LOG.debug(" - shared for Italy Transformer? " + shared);
                }
              }
            }

            ifAddrUpdated = ((SOFMessageHandler) handler).mainAddressUpdated && shared;
          }
        }
        if (ifAddrUpdated) {
          addressToSend.add(addrToCheck.getId().getAddrType());
          addrCount++;
        }
      }
    }
    LOG.debug("Address to be sent on update=" + addrCount);
    return addressToSend;
  }

  /**
   * Changes the SOF request type to update if SOF processing has already
   * completed
   * 
   * @param handler
   */
  private void changeToUpdateForCompletedSOFProcessing(MQMessageHandler handler) {
    if (MQMsgConstants.REQ_TYPE_UPDATE.equals(handler.mqIntfReqQueue.getReqType()) || !this.sofProcessingComplete) {
      // no need for update requests and not completed requests
      return;
    }
    Map<String, String> messageHash = handler.messageHash;
    messageHash.put("Operation", MQMsgConstants.MQ_OPERATION_U);
    messageHash.put("TransactionCode", MQMsgConstants.SOF_TRANSACTION_MODIFY);
    if (StringUtils.isEmpty(messageHash.get("CustomerNo"))) {
      String cmrNo = handler.cmrData.getCmrNo();
      if (StringUtils.isEmpty(cmrNo)) {
        cmrNo = handler.mqIntfReqQueue.getCmrNo();
      }
      messageHash.put("CustomerNo", cmrNo);
    }
  }

  /*
   * Legacy Direct Methods :Mukesh
   */
  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    LOG.debug("Set max and min range For Italy...");
    if (custSubGrp != null
        && ("INTER".equals(custSubGrp) || "INTVA".equals(custSubGrp) || "INTSM".equals(custSubGrp) || "CROIN".equals(custSubGrp))) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(998999);
    }
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    formatDataLines(dummyHandler);
    List<Addr> addresses = cmrObjects.getAddresses();
    String landedCountry = "";
    for (Addr addrVal : addresses) {
      if ("ZP01".equals(addrVal.getId().getAddrType())) {
        landedCountry = addrVal.getLandCntry();
      }
    }
    boolean crossBorder = isCrossBorderIT(landedCountry);

    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {

      // Leasing Company ind
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("LeasingCompany"))) {
        legacyCust.setLeasingInd(dummyHandler.messageHash.get("LeasingCompany"));
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("CEdivision"))) {
        legacyCust.setCeDivision(dummyHandler.messageHash.get("CEdivision"));
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get(MQMsgConstants.AUTH_REMARK))) {
        legacyCust.setAuthRemarketerInd(dummyHandler.messageHash.get(MQMsgConstants.AUTH_REMARK));
      }
      if (!StringUtils.isEmpty(dummyHandler.messageHash.get("RepeatAgreement"))) {
        legacyCust.setDcRepeatAgreement(dummyHandler.messageHash.get("RepeatAgreement"));
      }

      legacyCust.setLangCd("");// CLGXA confirmed by Martin

    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin);

      // permanent removal-single inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && !"TREC".equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && ("Y".equals(rdcEmbargoCd))) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      }
      // Support temporary reactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
          && admin.getReqStatus().equals(CMR_REQUEST_STATUS_CPR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && "Y".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        legacyCust.setEmbargoCd("");
        blankOrdBlockFromData(entityManager, data);
      }

      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason())
          && CMR_REQUEST_REASON_TEMP_REACT_EMBARGO.equals(admin.getReqReason()) && admin.getReqStatus() != null
          && admin.getReqStatus().equals(CMR_REQUEST_STATUS_PCR) && (rdcEmbargoCd != null && !StringUtils.isBlank(rdcEmbargoCd))
          && "Y".equals(rdcEmbargoCd) && (dataEmbargoCd == null || StringUtils.isBlank(dataEmbargoCd))) {
        legacyCust.setEmbargoCd(rdcEmbargoCd);
        resetOrdBlockToData(entityManager, data);
      }
    }

    // common data for C/U/X
    if (!StringUtils.isEmpty(dummyHandler.messageHash.get("ModeOfPayment"))) {
      legacyCust.setModeOfPayment(dummyHandler.messageHash.get("ModeOfPayment"));
    }

    // Type of Customer : CMRTCUST.CCUAI
    legacyCust.setCustType(!StringUtils.isBlank(data.getCrosSubTyp()) ? data.getCrosSubTyp() : "");

    // Enterprise Number / CODICE ENTERPRISE: CMRTCUST.RENXA // Enterprise
    // number from MAN
    legacyCust.setEnterpriseNo(!StringUtils.isEmpty(data.getEnterprise()) ? data.getEnterprise() : "");
    legacyCust.setSalesGroupRep(!StringUtils.isEmpty(data.getRepTeamMemberNo()) ? data.getRepTeamMemberNo() : "");

    if (StringUtils.isNotEmpty(data.getRepTeamMemberNo()) && data.getRepTeamMemberNo().length() > 5 && (data.getRepTeamMemberNo().charAt(4) == 'A')
        && StringUtils.isNotEmpty(data.getSalesBusOffCd()) && data.getSalesBusOffCd().length() <= 2) {
      legacyCust.setIbo("0" + data.getSalesBusOffCd() + "A000");
      legacyCust.setSbo("0" + data.getSalesBusOffCd() + "A000");
    } else if (StringUtils.isNotEmpty(data.getRepTeamMemberNo()) && data.getRepTeamMemberNo().length() > 5
        && (data.getRepTeamMemberNo().charAt(4) != 'A') && data.getSalesBusOffCd() != null && !"".equals(data.getSalesBusOffCd())
        && data.getSalesBusOffCd().length() <= 2) {
      legacyCust.setIbo("0" + data.getSalesBusOffCd() + "B000");
      legacyCust.setSbo("0" + data.getSalesBusOffCd() + "B000");
    } else {
      // CMR-1298 - do not set SBO if it is blank
      if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
        legacyCust.setIbo(data.getSalesBusOffCd());
        legacyCust.setSbo(data.getSalesBusOffCd());
      }
    }

    legacyCust.setCollectionCd("");
    legacyCust.setTaxCd("");
    // CREATCMR 3271
    if (!crossBorder) {
      // local
      legacyCust.setCeBo(StringUtils.isEmpty(data.getEngineeringBo()) ? "8412" : data.getEngineeringBo() + "000");

      // CMR-589-Removing prefix IT for local
      if (!StringUtils.isEmpty(dummyHandler.cmrData.getVat())) {
        if (dummyHandler.cmrData.getVat().matches("^[A-Z]{2}.*")) {
          legacyCust.setVat(dummyHandler.cmrData.getVat().substring(2));
        } else {
          legacyCust.setVat(dummyHandler.cmrData.getVat());
        }
      }
    } else {
      // CROSS
      legacyCust.setVat("");
      legacyCust.setCeBo(StringUtils.isEmpty(data.getEngineeringBo()) ? "9001" : data.getEngineeringBo() + "000");
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && StringUtils.isNotBlank(data.getFiscalDataStatus())
        && (data.getFiscalDataStatus().equals("W") || data.getFiscalDataStatus().equals("L"))) {
      try {
        LegacyDirectUtil.capsAndFillNulls(legacyCust, true);
        entityManager.flush();
      } catch (Exception e) {
        LOG.error("unable to fill nulls for legacy cust before double updates", e);
      }
      updateFiscalDataForDoubleUpdates(entityManager, data, cmrObjects, crossBorder);
    }

    List<String> isuCdList = Arrays.asList("34", "36", "32");
    if (!StringUtils.isEmpty(data.getIsuCd()) && !isuCdList.contains(data.getIsuCd())) {
      legacyCust.setIsuCd(data.getIsuCd() + "7");
    } else {
      String isuCtc;
      isuCtc = (!StringUtils.isEmpty(data.getIsuCd()) ? data.getIsuCd() : "")
          + (!StringUtils.isEmpty(data.getClientTier()) ? data.getClientTier() : "");
      if (isuCtc != null) {
        legacyCust.setIsuCd(isuCtc);
      }
    }

    // CREATCMR-4293
    if (!StringUtils.isEmpty(data.getIsuCd())) {
      if (StringUtils.isEmpty(data.getClientTier())) {
        legacyCust.setIsuCd(data.getIsuCd() + "7");
      }
    }
  }

  private void updateFiscalDataForDoubleUpdates(EntityManager entityManager, Data data, CMRRequestContainer cmrObjects, Boolean crossBorder) {

    LOG.debug(">>> Into updateFiscalDataForDoubleUpdates method <<<");

    List<CmrtCust> custRecords = new ArrayList<CmrtCust>();
    List<CmrtCustExt> custExtRecords = new ArrayList<CmrtCustExt>();
    String enterprise = data.getEnterprise();
    String fiscalCode = data.getTaxCd1();
    String vat = null;
    String identClient = data.getIdentClient();
    if (data.getVat() != null && !"".equals(data.getVat()) && data.getVat().length() == 13) {
      vat = data.getVat().substring(2, 13);
    } else {
      vat = data.getVat();
    }

    String company = data.getCompany();
    String oldCMRNo = "";
    if (data.getFiscalDataStatus().equals("W")) {
      if (StringUtils.isNotBlank(data.getFiscalDataCompanyNo())) {
        oldCMRNo = data.getFiscalDataCompanyNo();
        // CUST Records
        String sqlKey = ExternalizedQuery.getSql("ITALY.GET.LEGACY_CUST_RECORDS_FOR_COMPANY");
        PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
        query.setParameter("COMPANY", oldCMRNo);
        custRecords = query.getResults(CmrtCust.class);

        // CEXT Records
        sqlKey = ExternalizedQuery.getSql("ITALY.GET.LEGACY_CEXT_RECORDS_FOR_COMPANY");
        query = new PreparedQuery(entityManager, sqlKey);
        query.setParameter("COMPANY", oldCMRNo);
        custExtRecords = query.getResults(CmrtCustExt.class);
      } else {
        LOG.debug("No NEW CMR mentioned on the request correlated with the available fiscal data.");
      }
    } else if (data.getFiscalDataStatus().equals("L")) {
      oldCMRNo = data.getCmrNo();
      // CUST Records
      String sqlKey = ExternalizedQuery.getSql("ITALY.GET.LEGACY_CUST_RECORDS_FOR_COMPANY");
      PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
      query.setParameter("COMPANY", oldCMRNo);
      custRecords = query.getResults(CmrtCust.class);

      // CEXT Records
      sqlKey = ExternalizedQuery.getSql("ITALY.GET.LEGACY_CEXT_RECORDS_FOR_COMPANY");
      query = new PreparedQuery(entityManager, sqlKey);
      query.setParameter("COMPANY", oldCMRNo);
      custExtRecords = query.getResults(CmrtCustExt.class);
    }
    if (custRecords.size() > 0) {
      for (CmrtCust cust : custRecords) {
        if (StringUtils.isNotBlank(enterprise)) {
          cust.setEnterpriseNo(enterprise);
        } else {
          cust.setEnterpriseNo("");
        }
        LOG.debug(">>> CUST Record with CMR No. " + cust.getId().getCustomerNo() + " updated with latest fiscal data from CMR No. " + company);
        entityManager.merge(cust);
      }
    }

    if (custExtRecords.size() > 0) {
      for (CmrtCustExt custExt : custExtRecords) {
        if (StringUtils.isNotBlank(company)) {
          custExt.setItCompanyCustomerNo(company);
        } else {
          custExt.setItCompanyCustomerNo("");
        }
        if (crossBorder) {
          custExt.setiTaxCode((!StringUtils.isBlank(data.getVat()) ? data.getVat() : ""));
        } else {
          custExt.setiTaxCode((!StringUtils.isBlank(fiscalCode) ? fiscalCode : ""));
        }
        if (StringUtils.isNotBlank(identClient)) {
          custExt.setItIdentClient(identClient);
        } else {
          custExt.setItIdentClient("");
        }
        LOG.debug(">>> CEXT Record with CMR No. " + custExt.getId().getCustomerNo() + " updated with latest fiscal data from CMR No. " + company);
        entityManager.merge(custExt);
      }
    }

    if (custExtRecords.size() > 0 || custRecords.size() > 0) {
      String sqlKey = ExternalizedQuery.getSql("ITALY.GET.LEGACY_COMPANY_ADDR_RECORD");
      PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
      query.setParameter("COMPANY", oldCMRNo);
      CmrtAddr addr = query.getSingleResult(CmrtAddr.class);
      if (addr != null) {
        addr.setIsAddressUseC("N");
        // entityManager.remove(addr);
      }
      LOG.debug(">>>Company Record with CMR No. " + oldCMRNo + " set to deleted state");
      entityManager.merge(addr);
      entityManager.flush();
    }

  }

  private void blankOrdBlockFromData(EntityManager entityManager, Data data) {
    data.setOrdBlk("");
    entityManager.merge(data);
    entityManager.flush();
  }

  private void resetOrdBlockToData(EntityManager entityManager, Data data) {
    data.setOrdBlk("88");
    entityManager.merge(data);
    entityManager.flush();
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    List<Addr> addresses = cmrObjects.getAddresses();
    String landedCountry = "";
    for (Addr addrVal : addresses) {
      if ("ZP01".equals(addrVal.getId().getAddrType())) {
        landedCountry = addrVal.getLandCntry();
      }
    }
    boolean crossBorder = isCrossBorderIT(landedCountry);
    // CREATCMR-7470 All Address Types
    if (crossBorder) {
      legacyAddr.setItCompanyProvCd(!StringUtils.isBlank(currAddr.getLandCntry()) ? currAddr.getLandCntry() : "");
    } else {
      legacyAddr.setItCompanyProvCd(!StringUtils.isBlank(currAddr.getStateProv()) ? currAddr.getStateProv() : "");
    }
    formatAddressLinesLD(dummyHandler, legacyAddr);
  }

  /**
   * According to new Address Mapping onboarding sheet
   * 
   * @param handler
   * @param legacyAddr
   */
  private void formatAddressLinesLD(MQMessageHandler handler, CmrtAddr legacyAddr) {

    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Legacy Direct -Handling Address for " + (update ? "update" : "create") + " request.");

    String line1 = "";
    String line2 = "";
    String line3 = "";
    String line4 = "";
    String line5 = "";
    String line6 = "";
    String addrType = addrData.getId().getAddrType();

    LOG.trace("Handling " + (update ? "update" : "create") + " request.");

    // line1
    line1 = addrData.getCustNm1();
    line2 = addrData.getCustNm2();
    line3 = ""; // should be blank due to ESI requirement

    // Installing Address
    if (MQMsgConstants.ADDR_ZS01.equals(addrType)) {

      // Street
      if (!StringUtils.isBlank(addrData.getAddrTxt())) {
        line4 = addrData.getAddrTxt();
      }

      // Postal code + City
      if (!StringUtils.isBlank(addrData.getPostCd())) {
        line5 = addrData.getPostCd();
      } else if (!StringUtils.isBlank(addrData.getCity1())) {
        line5 = addrData.getCity1();
      }
      if (!StringUtils.isBlank(addrData.getPostCd()) && !StringUtils.isBlank(addrData.getCity1())) {
        line5 = addrData.getPostCd() + " " + addrData.getCity1();
      }

      // Country Landed :"full country name" based on landed cty
      if (!StringUtils.isBlank(addrData.getLandCntry())) {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }
      // addrData.setParCmrNo(getBillingNo(handler));
      // updateAddrParCMR(handler, addrData);
      if ("N".equalsIgnoreCase(addrData.getImportInd())
          || (!"N".equalsIgnoreCase(addrData.getImportInd()) && CmrConstants.PROSPECT_ORDER_BLOCK.equals(cmrData.getOrdBlk()))) {
        MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
        if (currentQ.getCmrNo() != null) {
          addrData.setParCmrNo(currentQ.getCmrNo());
          updateAddrParCMR(handler, addrData);
        }
      }
    }

    // Billing Address
    if (MQMsgConstants.ADDR_ZP01.equals(addrType)) {

      // Street
      if (!StringUtils.isBlank(addrData.getAddrTxt())) {
        line4 = addrData.getAddrTxt();
      }

      // Postal code + City
      if (!StringUtils.isBlank(addrData.getPostCd())) {
        line5 = addrData.getPostCd();
      } else if (!StringUtils.isBlank(addrData.getCity1())) {
        line5 = addrData.getCity1();
      }
      if (!StringUtils.isBlank(addrData.getPostCd()) && !StringUtils.isBlank(addrData.getCity1())) {
        line5 = addrData.getPostCd() + " " + addrData.getCity1();
      }

      // Country Landed :"full country name" based on landed cty
      if (!StringUtils.isBlank(addrData.getLandCntry())) {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }

      if ("N".equalsIgnoreCase(addrData.getImportInd())) {
        MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
        if (currentQ.getCmrNo() != null) {
          addrData.setParCmrNo(currentQ.getCmrNo());
          updateAddrParCMR(handler, addrData);
        }
      }
    }

    // Company Address
    if (MQMsgConstants.ADDR_ZI01.equals(addrType)) {

      // Street
      if (!StringUtils.isBlank(addrData.getAddrTxt())) {
        line4 = addrData.getAddrTxt();
      }

      // Postal code + City
      if (!StringUtils.isBlank(addrData.getPostCd())) {
        line5 = addrData.getPostCd();
      } else if (!StringUtils.isBlank(addrData.getCity1())) {
        line5 = addrData.getCity1();
      }
      if (!StringUtils.isBlank(addrData.getPostCd()) && !StringUtils.isBlank(addrData.getCity1())) {
        line5 = addrData.getPostCd() + " " + addrData.getCity1();
      }

      // Country Landed :"full country name" based on landed cty
      if (!StringUtils.isBlank(addrData.getLandCntry())) {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }

      if ("N".equalsIgnoreCase(addrData.getImportInd())) {
        MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
        if (currentQ.getCmrNo() != null) {
          cmrData.setCompany(currentQ.getCmrNo());
          addrData.setParCmrNo(currentQ.getCmrNo());
          updateAddrParCMR(handler, addrData);
        }
      }
    }

    if (addrData.getLandCntry().equals("IT")) {
      legacyAddr.setItCompanyProvCd(!StringUtils.isBlank(addrData.getStateProv()) ? addrData.getStateProv() : "");
    } else {
      legacyAddr.setItCompanyProvCd(!StringUtils.isBlank(addrData.getLandCntry()) ? addrData.getLandCntry() : "");
    }

    legacyAddr.setAddrLine1(line1);
    legacyAddr.setAddrLine2(line2);
    legacyAddr.setAddrLine3(line3);
    legacyAddr.setAddrLine4(line4);
    legacyAddr.setAddrLine5(line5);
    legacyAddr.setAddrLine6(line6);

  }

  @Override
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    Data data = cmrObjects.getData();
    List<Addr> addresses = cmrObjects.getAddresses();
    String landedCountry = "";
    for (Addr addrVal : addresses) {
      if ("ZP01".equals(addrVal.getId().getAddrType())) {
        landedCountry = addrVal.getLandCntry();
      }
    }
    boolean crossBorder = isCrossBorderIT(landedCountry);
    for (Addr addr : cmrObjects.getAddresses()) {
      // Billing Address
      if (MQMsgConstants.ADDR_ZP01.equals(addr.getId().getAddrType())) {
        if (!StringUtils.isBlank(addr.getBldg())) {
          // NORMABB -Abbrev Name
          legacyCustExt.setItBillingName(22 < addr.getBldg().length() ? addr.getBldg().substring(0, 22) : addr.getBldg());
        }
        if (!StringUtils.isBlank(addr.getCustFax())) {
          // CITABB -City Abbrev
          legacyCustExt.setItBillingCity(12 < addr.getCustFax().length() ? addr.getCustFax().substring(0, 12) : addr.getCustFax());
        }
        if (!StringUtils.isBlank(addr.getDivn())) {
          // INDABB - Street Abbrev
          legacyCustExt.setItBillingStreet(18 < addr.getDivn().length() ? addr.getDivn().substring(0, 18) : addr.getDivn());
        }
        legacyCustExt.setItBillingCustomerNo(!StringUtils.isBlank(addr.getParCmrNo()) ? addr.getParCmrNo() : "");
        if (crossBorder) {
          landedCountry = addr.getLandCntry();
          legacyCustExt.setiTaxCode((!StringUtils.isBlank(data.getVat()) ? data.getVat() : ""));
        } else {
          legacyCustExt.setiTaxCode(!StringUtils.isBlank(data.getTaxCd1()) ? data.getTaxCd1() : "");
        }
      }
    }

    // IBM Tab
    legacyCustExt.setItCompanyCustomerNo(!StringUtils.isEmpty(data.getCompany()) ? data.getCompany() : ""); // CODCP
    legacyCustExt.setAffiliate(!StringUtils.isBlank(data.getAffiliate()) ? data.getAffiliate() : "");
    legacyCustExt.setItCodeSSV(!StringUtils.isBlank(data.getCollectionCd()) ? data.getCollectionCd() : "");

    legacyCustExt.setItIVA(!StringUtils.isBlank(data.getSpecialTaxCd()) ? data.getSpecialTaxCd() : "");
    legacyCustExt.setItIdentClient(!StringUtils.isBlank(data.getIdentClient()) ? data.getIdentClient() : "");

    // 4 new fields
    legacyCustExt.setTipoCliente(!StringUtils.isBlank(data.getIcmsInd()) ? data.getIcmsInd() : "");
    legacyCustExt.setCoddes(!StringUtils.isBlank(data.getHwSvcsRepTeamNo()) ? data.getHwSvcsRepTeamNo() : "");
    legacyCustExt.setPec(!StringUtils.isBlank(data.getEmail2()) ? data.getEmail2() : "");
    legacyCustExt.setIndEmail(!StringUtils.isBlank(data.getEmail3()) ? data.getEmail3() : "");
  }

  private void updateAddrParCMR(MQMessageHandler handler, Addr addrData) {
    long reqId = addrData.getId().getReqId();
    String addrType = addrData.getId().getAddrType();
    String addrSeq = addrData.getId().getAddrSeq();
    String parCmrNo = addrData.getParCmrNo();
    EntityManager entityManager = handler.getEntityManager();
    String sql = ExternalizedQuery.getSql("ITALY.UPDATE_ADDR_PAR_CMR");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("PAR_CMR_NO", parCmrNo);
    q.setParameter("REQ_ID", reqId);
    q.setParameter("ADDR_SEQ", addrSeq);
    q.setParameter("ADDR_TYPE", addrType);
    q.executeSql();
    LOG.debug(
        "Request ID: " + reqId + "-Updated Parent CMR number for ADDR Type: " + addrType + " and ADDR Seq: " + addrSeq + " to '" + parCmrNo + "'");

  }

  @Override
  public boolean skipLegacyAddressData(EntityManager entityManager, CMRRequestContainer cmrObjects, Addr currAddr, boolean flag) {
    String addrType = currAddr.getId().getAddrType();
    Data data = cmrObjects.getData();
    // Company Address
    if (MQMsgConstants.ADDR_ZI01.equals(addrType) && "Y".equals(currAddr.getImportInd())) {
      if (flag)
        return true;
      else {
        if (!data.getCompany().equals(data.getCmrNo()))
          return true;
      }
    } else if (MQMsgConstants.ADDR_ZP01.equals(addrType) && "Y".equals(currAddr.getImportInd())) {
      if (flag)
        return true;
    }
    return false;
  }

  @Override
  public void transformLegacyCustomerExtDataMassUpdate(EntityManager entityManager, CmrtCustExt custExt, CMRRequestContainer cmrObjects,
      MassUpdtData muData, String cmr) throws Exception {
    LOG.debug("IT >> Mapping default CMRTCEXT values");
    boolean isUpdated = false;

    // CMR:1334 : MassUpdate: TAX code
    if (!StringUtils.isBlank(muData.getSpecialTaxCd())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getSpecialTaxCd().trim())) {
        custExt.setItIVA("");
      } else {
        custExt.setItIVA(muData.getSpecialTaxCd().trim());
      }
    }

    // this is for fiscal
    if (!StringUtils.isBlank(muData.getNewEntpName1())) {
      if ("@".equals(muData.getNewEntpName1().trim())) {
        custExt.setiTaxCode("");
      } else {
        custExt.setiTaxCode(muData.getNewEntpName1());
      }
      isUpdated = true;
    }

    // for ident client
    if (!StringUtils.isBlank(muData.getOutCityLimit())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getOutCityLimit().trim())) {
        custExt.setItIdentClient("");
      } else {
        custExt.setItIdentClient(muData.getOutCityLimit());
      }
      isUpdated = true;
    }

    // for company customer number
    if (!StringUtils.isBlank(muData.getCompany())) {
      if (DEFAULT_CLEAR_6_CHAR.equals(muData.getCompany().trim())) {
        custExt.setItCompanyCustomerNo("");
      } else {
        custExt.setItCompanyCustomerNo(muData.getCompany());
      }
    }

    if (!StringUtils.isBlank(muData.getAffiliate())) {
      if (DEFAULT_CLEAR_6_CHAR.equals(muData.getAffiliate())) {
        custExt.setAffiliate("");
      } else {
        custExt.setAffiliate(muData.getAffiliate());
      }
    }

    // Tipo Cliente
    if (!StringUtils.isBlank(muData.getEntpUpdtTyp())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getEntpUpdtTyp())) {
        custExt.setTipoCliente("");
      } else {
        custExt.setTipoCliente(muData.getEntpUpdtTyp());
      }
    }

    // Codice Destinatario/Ufficio
    if (!StringUtils.isBlank(muData.getSearchTerm())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getSearchTerm())) {
        custExt.setCoddes("");
      } else {
        custExt.setCoddes(muData.getSearchTerm());
      }
    }

    // PEC
    if (!StringUtils.isBlank(muData.getEmail2())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getEmail2())) {
        custExt.setPec("");
      } else {
        custExt.setPec(muData.getEmail2());
      }
    }

    // INDIRIZZO EMAIL
    if (!StringUtils.isBlank(muData.getEmail3())) {
      if (DEFAULT_CLEAR_CHAR.equals(muData.getEmail3())) {
        custExt.setIndEmail("");
      } else {
        custExt.setIndEmail(muData.getEmail3());
      }
    }

    // CMR-1332: MassUpdate: Collection code
    // Collection Code
    if (!StringUtils.isBlank(muData.getCollectionCd())) {
      if ("@".equals(muData.getCollectionCd())) {
        // cust.setCollectionCd("");
        custExt.setItCodeSSV("");
      } else {
        custExt.setItCodeSSV(muData.getCollectionCd());
      }
    }

    if (isUpdated) {
      custExt.setUpdateTs(SystemUtil.getCurrentTimestamp());
    }

  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust cust, CMRRequestContainer cmrObjects, MassUpdtData muData) {
    // default mapping for DATA and CMRTCUST
    LOG.debug("IT >> Mapping default Data values..");

    // LegacyDirectUtil.get
    String cntry = cust.getRealCtyCd();
    String status = cust.getStatus();
    if ("758".equals(cntry) && "A".equalsIgnoreCase(status)) {
      if (!StringUtils.isBlank(muData.getAbbrevNm())) {
        cust.setAbbrevNm(muData.getAbbrevNm());
      }
      if (!StringUtils.isBlank(muData.getAbbrevLocn())) {
        cust.setAbbrevLocn(muData.getAbbrevLocn());
      }
      if (!StringUtils.isBlank(muData.getModeOfPayment())) {
        if (DEFAULT_CLEAR_CHAR.equals(muData.getModeOfPayment().trim())) {
          cust.setModeOfPayment("");
        } else {
          cust.setModeOfPayment(muData.getModeOfPayment());
        }
      }

      List<String> isuCdList = Arrays.asList("5K", "14", "19", "3T", "4A");
      if (!StringUtils.isEmpty(muData.getIsuCd()) && isuCdList.contains(muData.getIsuCd())) {
        cust.setIsuCd(muData.getIsuCd() + "7");
      } else {
        String isuClientTier = (!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : "")
            + (!StringUtils.isEmpty(muData.getClientTier()) ? muData.getClientTier() : "");
        if (isuClientTier != null && isuClientTier.endsWith("@")) {
          cust.setIsuCd((!StringUtils.isEmpty(muData.getIsuCd()) ? muData.getIsuCd() : cust.getIsuCd().substring(0, 2)) + "7");
        } else if (isuClientTier != null && isuClientTier.length() == 3) {
          cust.setIsuCd(isuClientTier);
        }
      }

      if (!StringUtils.isBlank(muData.getRepTeamMemberNo())) {
        cust.setSalesRepNo(muData.getRepTeamMemberNo());
        cust.setSalesGroupRep(muData.getRepTeamMemberNo());
      }
      if (!StringUtils.isBlank(muData.getCustNm1())) {
        cust.setSbo("0" + muData.getCustNm1() + "B000"); // 0NMB000
        cust.setIbo("0" + muData.getCustNm1() + "B000");
      }

      if (!StringUtils.isBlank(muData.getEnterprise())) {
        if (DEFAULT_CLEAR_6_CHAR.equals(muData.getEnterprise().trim())) {
          cust.setEnterpriseNo("");
        } else {
          cust.setEnterpriseNo(muData.getEnterprise());
        }
      }

      if (!StringUtils.isBlank(muData.getCustNm2())) {
        cust.setCeBo(muData.getCustNm2());
      }
      if (!StringUtils.isBlank(muData.getIsicCd())) {
        cust.setIsicCd(muData.getIsicCd());
      }

      // CMR-1350
      if (!StringUtils.isBlank(muData.getVat())) {
        if ("@".equals(muData.getVat().trim())) {
          cust.setVat("");
        } else {
          cust.setVat(muData.getVat());
        }
      }

      if (!StringUtils.isBlank(muData.getInacCd())) {
        if (DEFAULT_CLEAR_4_CHAR.equals(muData.getInacCd().trim())) {
          cust.setInacCd("");
        } else {
          cust.setInacCd(muData.getInacCd());
        }
      }

      if (!StringUtils.isBlank(muData.getMiscBillCd())) {
        if (DEFAULT_CLEAR_CHAR.equals(muData.getMiscBillCd().trim())) {
          cust.setEmbargoCd("");
        } else {
          cust.setEmbargoCd(muData.getMiscBillCd());
        }
      }

      if (!StringUtils.isBlank(muData.getSubIndustryCd())) {
        String subInd = muData.getSubIndustryCd();
        cust.setImsCd(subInd);
        // Defect 1776715: Fix for Economic code
        String firstChar = String.valueOf(subInd.charAt(0));
        StringBuilder builder = new StringBuilder();
        builder.append(firstChar);
        builder.append(subInd);
        LOG.debug("***Auto setting Economic code as > " + builder.toString());
        cust.setEconomicCd(builder.toString());
      }

      // Type Of Customer
      if (!StringUtils.isBlank(muData.getCurrencyCd())) {
        if (DEFAULT_CLEAR_CHAR.equals(muData.getCurrencyCd() != null ? muData.getCurrencyCd().trim() : "")) {
          cust.setCustType("");
        } else {
          cust.setCustType(muData.getCurrencyCd() != null ? muData.getCurrencyCd().trim() : "");
        }
      }
      cust.setUpdateTs(SystemUtil.getCurrentTimestamp());
    }
  }

  @Override
  public boolean hasCmrtCustExtErrorMessage(EntityManager entityManager, CmrtCust cust, CmrtCustExt custExt, boolean flag) {
    String billingCMR = custExt.getItBillingCustomerNo();
    String companyCMR = custExt.getItCompanyCustomerNo();
    if (null == custExt.getItCompanyCustomerNo() || "".equals(custExt.getItCompanyCustomerNo()) || null == custExt.getItBillingCustomerNo()
        || "".equals(custExt.getItBillingCustomerNo())) {
      return true;
    }
    String sql = "";
    if (flag) {
      // reactivate case
      if (custExt.getId().getCustomerNo().equals(companyCMR)) {
        return false;
      } else if (custExt.getId().getCustomerNo().equals(billingCMR)) {
        // check company is active or not
        sql = ExternalizedQuery.getSql("LD.REACTIVATE_COMPANY_CHECK");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("COUNTRY", custExt.getId().getSofCntryCode());
        query.setParameter("CMR_NO", billingCMR);
        query.setForReadOnly(true);
        return query.exists();
      }
    } else {
      // delete case
      if (custExt.getId().getCustomerNo().equals(companyCMR)) {
        // check all child of company is deleted or not
        sql = ExternalizedQuery.getSql("LD.DELETE_COMPANY_CHECK");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("COUNTRY", custExt.getId().getSofCntryCode());
        query.setParameter("CMR_NO", companyCMR);
        query.setParameter("INPUTCMR", custExt.getId().getCustomerNo());
        query.setForReadOnly(true);
        return query.exists();
      } else if (custExt.getId().getCustomerNo().equals(billingCMR)) {
        // check billing child is deleted or not
        sql = ExternalizedQuery.getSql("LD.DELETE_BILLING_CHECK");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("COUNTRY", custExt.getId().getSofCntryCode());
        query.setParameter("CMR_NO", billingCMR);
        query.setParameter("INPUTCMR", custExt.getId().getCustomerNo());
        query.setForReadOnly(true);
        return query.exists();
      }
    }

    return false;
  }

}
