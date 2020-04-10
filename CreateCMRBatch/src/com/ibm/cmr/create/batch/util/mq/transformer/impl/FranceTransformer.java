/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueue;
import com.ibm.cio.cmr.request.entity.MqIntfReqQueuePK;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.impl.FRHandler;
import com.ibm.cmr.create.batch.service.BaseBatchService;
import com.ibm.cmr.create.batch.util.mq.CEBOGenerate;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.GenerateCMRNoClient;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoResponse;

/**
 * @author Lv Guo Feng
 * 
 */
public class FranceTransformer extends MessageTransformer {

  private static final String[] ADDRESS_ORDER = { "ZS01", "ZS02", "ZP01", "ZD01", "ZI01", "ZD02", "ZSIR" };

  private static final String[] COMME_DEPARTMENT_NUMBER_ORDER = { "", "", "0392", "0388", "0385", "0381", "0388", "", "0381", "0386", "0381", "0386",
      "", "", "0371", "0382" };
  private static final String[] COMME_OVERSEAS_TERRITORY_ORDER = { "", "", "032", "035", "031", "033", "035", "", "033", "048", "033", "047", "", "",
      "011", "013" };

  private static final String[] NO_UPDATE_FIELDS = { MQMsgConstants.NATURE_CLIENT, "MarketingResponseCode", "CEdivision", "DPCEBO",
      MQMsgConstants.CODE_APE, MQMsgConstants.SIGLE_IDENTIF, MQMsgConstants.AUTH_REMARK, MQMsgConstants.PENALTIESDERETARD,
      MQMsgConstants.TYPEDEFACTURATION, MQMsgConstants.SOFTYPEOFALLIANCE, MQMsgConstants.OVERSEAS_TERRITORY };
  // private static final String[] INTSO_NATURE_CLIENT_ORDER = { "395", "396",
  // "397", "398", "399", "400", "402", "402", "403", "404", "405", "407",
  // "407", "408", "409", "410" };

  private static final Logger LOG = Logger.getLogger(FranceTransformer.class);

  // private static final List<String> FR_COUNTRIES = Arrays.asList("MC", "GP",
  // "GF", "MQ", "RE", "PM", "KM", "VU", "PF", "YT", "NC", "WF", "AD", "DZ",
  // "TN");

  private static final List<String> DOM = Arrays.asList(SystemLocation.FRENCH_GUIANA, SystemLocation.GUADELOUPE, SystemLocation.COMOROS,
      SystemLocation.MARTINIQUE, SystemLocation.SAINT_PIERRE_MIQUELON, SystemLocation.REUNION);

  private static final List<String> TOM = Arrays.asList(SystemLocation.NEW_CALEDONIA, SystemLocation.FRENCH_POLYNESIA_TAHITI, SystemLocation.VANUATU,
      SystemLocation.WALLIS_FUTUNA, SystemLocation.MAYOTTE);

  private String customerLocation = "";

  public FranceTransformer(String issuingCntry) {
    super(issuingCntry);

  }

  public FranceTransformer() {
    super(SystemLocation.FRANCE);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;
    boolean update = "U".equals(handler.adminData.getReqType());

    LOG.debug("Handling Data for " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    boolean crossBorder = isCrossBorder(cmrData, handler);
    String countrySubRegion = cmrData.getCountryUse();

    handleDataDefaults(handler, messageHash, cmrData, crossBorder, addrData);

    LOG.debug("CHECKING CUSTOMER LOCATION CODE---GEORGE---" + addrData.getId().getAddrType() + "---" + addrData.getPostCd() + "---"
        + addrData.getCity1() + " cusLoc==" + this.customerLocation);
    if (StringUtils.isEmpty(customerLocation)) {
      this.customerLocation = getLocationNumber(handler, addrData);
      cmrData.setLocationNumber(this.customerLocation);
      messageHash.put("LocationNumber", !StringUtils.isEmpty(cmrData.getLocationNumber()) ? cmrData.getLocationNumber() : "");
    }
    messageHash.put("CEdivision", "3");
    messageHash.put("MarketingResponseCode", "3");
    messageHash.put("EnterpriseNo", !StringUtils.isEmpty(cmrData.getEnterprise()) ? cmrData.getEnterprise() : "");
    messageHash.put("TaxCode", !StringUtils.isEmpty(cmrData.getTaxCd2()) ? cmrData.getTaxCd2() : "");
    messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
    messageHash.put("EmbargoCode", !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "");

    // 1484540 - DPCEBO logic
    if (crossBorder)
      messageHash.put("DPCEBO", "299");
    else {
      if (SystemLocation.FRANCE.equalsIgnoreCase(countrySubRegion)) {
        String postCd = getPostCodeInZS01(handler);
        if (!postCd.isEmpty() && postCd.length() > 2) {
          postCd = postCd.substring(0, 2);
        }
        messageHash.put("DPCEBO", CEBOGenerate.getFR_CEBO(postCd));
      } else if (SystemLocation.MONACO.equalsIgnoreCase(countrySubRegion)) {
        messageHash.put("DPCEBO", "646");
      } else if (SystemLocation.ANDORRA.equalsIgnoreCase(countrySubRegion)) {
        messageHash.put("DPCEBO", "663");
      } else if (SystemLocation.ALGERIA.equalsIgnoreCase(countrySubRegion) || SystemLocation.TUNISIA.equalsIgnoreCase(countrySubRegion)) {
        messageHash.put("DPCEBO", "299");
      } else {
        messageHash.put("DPCEBO", "669");
      }
    }

    // Defect 1490178 Liste Speciale is not flowing fully into SOF and missing
    // in Request Summary
    String topListSpeciale = !StringUtils.isEmpty(cmrData.getCommercialFinanced()) ? cmrData.getCommercialFinanced() : "";
    // Defect 1584855 - add '@' into the beginning if it contains 3 CHARs value
    if (topListSpeciale.length() == 3)
      topListSpeciale = "@" + topListSpeciale;
    messageHash.put(MQMsgConstants.TLSPE, topListSpeciale);

    // 1496470 - if Affacturage field is empty then send N
    if (!StringUtils.isEmpty(cmrData.getDupCmrIndc()) && "Y".equalsIgnoreCase(cmrData.getDupCmrIndc()))
      messageHash.put(MQMsgConstants.AFFAC, "Y");
    else
      messageHash.put(MQMsgConstants.AFFAC, "N");

    setVauleByCountry(cmrData.getCountryUse(), MQMsgConstants.DEPARTMENT_CODE, COMME_DEPARTMENT_NUMBER_ORDER, messageHash);
    setVauleByCountry(cmrData.getCountryUse(), MQMsgConstants.OVERSEAS_TERRITORY, COMME_OVERSEAS_TERRITORY_ORDER, messageHash);
    // setVauleByCountry(cmrData.getCountryUse(), MQMsgConstants.NATURE_CLIENT,
    // INTSO_NATURE_CLIENT_ORDER, messageHash);
    if (MQMsgConstants.CUSTSUBGRP_INTER.equals(cmrData.getCustSubGrp()) || MQMsgConstants.CUSTSUBGRP_CBTER.equals(cmrData.getCustSubGrp())
        || MQMsgConstants.CUSTSUBGRP_INTSO.equals(cmrData.getCustSubGrp()) || MQMsgConstants.CUSTSUBGRP_CBTSO.equals(cmrData.getCustSubGrp()))
      messageHash.put(MQMsgConstants.DEPARTMENT_CODE, cmrData.getIbmDeptCostCenter());

    messageHash.put(MQMsgConstants.SIRET, cmrData.getTaxCd1());
    messageHash.put(MQMsgConstants.CODE_APE, "9999");
    // only generate CMR no for Creates /Algeria or Tunisia
    if (MQMsgConstants.REQ_TYPE_CREATE.equals(handler.mqIntfReqQueue.getReqType())
        && MQMsgConstants.REQ_STATUS_NEW.equals(handler.mqIntfReqQueue.getReqStatus())) {
      if (isDoubleCreate(handler))
        if (StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
          LOG.debug(
              "TUNISIA or ALGERIA for MQ ID " + handler.mqIntfReqQueue.getId().getQueryReqId() + " Request ID " + handler.cmrData.getId().getReqId());
          String cmrNo = generateCMRNoForTunisiaAndAlgeria(handler, "729");
          if (cmrNo == null) {
            LOG.warn("Warning, a CMR No cannot be generated for TUNISIA or ALGERIA.");
          } else {
            LOG.info("CMR No " + cmrNo + " found as next available for TUNISIA or ALGERIA.");
            messageHash.put("CustomerNo", cmrNo);
          }
        } else {
          LOG.debug("Correlated request with MQ ID " + handler.mqIntfReqQueue.getCorrelationId() + ", setting CMR No. "
              + handler.mqIntfReqQueue.getCmrNo());
          messageHash.put("CustomerNo", handler.mqIntfReqQueue.getCmrNo());
        }
    }

    // SIRET -- dummy SIRET ('SC'+CN+'0'+Customer Location Number)
    // '9999' and Customer Location Number to the CN
    if (StringUtils.isEmpty(cmrData.getTaxCd1()))
      if (MQMsgConstants.CUSTSUBGRP_COMME.equalsIgnoreCase(cmrData.getCustSubGrp())
          || MQMsgConstants.CUSTSUBGRP_FIBAB.equalsIgnoreCase(cmrData.getCustSubGrp())
          || MQMsgConstants.CUSTSUBGRP_IBMEM.equalsIgnoreCase(cmrData.getCustSubGrp())
          || MQMsgConstants.CUSTSUBGRP_PRICU.equalsIgnoreCase(cmrData.getCustSubGrp()) || isCrossBorder(cmrData, handler)
          || DOM.contains(countrySubRegion) || TOM.contains(countrySubRegion) || SystemLocation.MONACO.equalsIgnoreCase(countrySubRegion))
        messageHash.put(MQMsgConstants.SIRET, "SC" + "xxxxxx" + "0" + messageHash.get("LocationNumber"));

    if (!StringUtils.isEmpty(messageHash.get(MQMsgConstants.SIRET)) && !"SC".equalsIgnoreCase(messageHash.get(MQMsgConstants.SIRET).substring(0, 2)))
      messageHash.put(MQMsgConstants.CODE_APE, "");

    setValueByScenario(messageHash, cmrData.getCustSubGrp(), cmrData.getCountryUse());

    // Sigle identificateur should be automatically changed to DF when Address H
    if (hasHAddress(handler))
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "DF");

    LOG.debug("Handling MQIntfReqQueue Issuing Country for " + handler.mqIntfReqQueue.getCmrIssuingCntry());
    if ("729".equalsIgnoreCase(handler.mqIntfReqQueue.getCmrIssuingCntry()))
      setDefault729Values(handler);

    if (update) {
      for (String field : NO_UPDATE_FIELDS) {
        messageHash.remove(field);
      }
      List<String> unChangedList = handler.removeUnChangedDataItems(handler.mqIntfReqQueue.getReqId(), cmrData);
      for (String field : unChangedList) {
        messageHash.remove(field);
      }
    }
  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {

    Addr addrData = handler.addrData;
    Data cmrData = handler.cmrData;

    if (FRHandler.DUMMY_SIRET_ADDRESS.equals(addrData.getId().getAddrType())) {
      // this is for a SIRET update. Send correct value for SIRET, even dummy or
      // not

      formatRequestforSIRETUpdate(handler);
    } else {
      boolean update = "U".equals(handler.adminData.getReqType());
      boolean crossBorder = isCrossBorder(cmrData, handler);

      String countrySubRegion = cmrData.getCountryUse();
      String postalCode = addrData.getPostCd();
      String city = addrData.getCity1();
      String addrKey = getAddressKey(addrData.getId().getAddrType());

      LOG.debug("Handling " + (update ? "update" : "create") + " request.");
      Map<String, String> messageHash = handler.messageHash;
      if ("729".equalsIgnoreCase(handler.mqIntfReqQueue.getCmrIssuingCntry()))
        setDefaultAddr729Values(handler);
      else
        messageHash.put("SourceCode", "21M");
      messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Contact", addrData.getCustNm4());
      messageHash.remove(addrKey + "Name");
      messageHash.remove(addrKey + "ZipCode");
      messageHash.remove(addrKey + "City");
      messageHash.remove(addrKey + "POBox");

      String line1 = "";
      String line2 = "";
      String line3 = "";
      String line4 = "";
      String line5 = "";
      String line6 = "";

      line1 = addrData.getCustNm1();
      line2 = addrData.getCustNm2();
      line3 = addrData.getCustNm3();
      line4 = addrData.getAddrTxt();

      if (countrySubRegion != null) {

        // Defect 1481996 One space should be added after the Postal Code for
        // creations of Domestic, DOM, TOM and Monaco CNs.
        if (!crossBorder || DOM.contains(countrySubRegion) || TOM.contains(countrySubRegion) || "706MC".equalsIgnoreCase(countrySubRegion)) {
          postalCode += " ";
        }

        if (!crossBorder || DOM.contains(countrySubRegion)) {
          line5 = addrData.getAddrTxt2();
          line6 = postalCode + city;
        }
        if (crossBorder || SystemLocation.ANDORRA.equalsIgnoreCase(countrySubRegion) || SystemLocation.ALGERIA.equalsIgnoreCase(countrySubRegion)
            || SystemLocation.TUNISIA.equalsIgnoreCase(countrySubRegion)) {

          int postalCodeLength = 0;
          if (postalCode != null) {
            postalCodeLength = postalCode.length();
          }

          if (postalCode == null || postalCode != null && postalCodeLength == 0)
            // Story 1103289 assign postalCode == 11 whitespace since address
            // line
            // 5th of city should starting at 12th position in case of cross
            // boarder scenario, Andorra, Algeria,Tunisia.
            postalCode = "           ";
          else if (postalCode != null && postalCodeLength > 0) {
            for (int i = 1; i <= (11 - postalCodeLength); i++) {
              postalCode += " ";
            }
          }
          line5 = postalCode + addrData.getCity1();
          line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
        }
        if (TOM.contains(countrySubRegion)) {
          line5 = postalCode + city;
          line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
        }

      } else if (countrySubRegion == null) {
        line5 = addrData.getAddrTxt2();
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }

      // line5 = addrData.getPostCd() + addrData.getCity1();

      String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
      int lineNo = 1;
      LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
      for (String line : lines) {
        messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo, line);
        lineNo++;
      }
      // 1417363 - Linkage when country in billing address differ from country
      // in installing address
      if (!StringUtils.isEmpty(addrData.getParCmrNo())) {
        lineNo = 1;
        for (String line : lines) {
          messageHash.remove(getAddressKey(addrData.getId().getAddrType()) + "Address" + lineNo);
          lineNo++;
        }
        messageHash.put("Operation", "create");
        messageHash.put("AddressNumber", "");
        messageHash.put("LinkedCN", addrData.getParCmrNo());
        messageHash.put("AddressUse", "2");
      }
    }
  }

  /**
   * Reformats the whole XML request to send only an update for the SIRET value
   * 
   * @param handler
   */
  private void formatRequestforSIRETUpdate(MQMessageHandler handler) {
    Data cmrData = handler.cmrData;

    Map<String, String> messageHash = handler.messageHash;
    messageHash.clear(); // no other values

    String siret = cmrData.getTaxCd1();
    if (StringUtils.isEmpty(siret) || (siret.toUpperCase().startsWith("SC") || siret.matches("[0]*"))) {
      // dummy SIRET format is either SCXXXXX+LOCN or all zeros
      if (StringUtils.isEmpty(this.customerLocation)) {
        this.customerLocation = getLocationNumber(handler, handler.addrData);
      }
      siret = "SC" + handler.mqIntfReqQueue.getCmrNo() + "0" + this.customerLocation;
      LOG.debug("Dummy SIRET value " + siret + " for Request ID " + cmrData.getId().getReqId());
    }
    if (siret == null) {
      siret = "";
    }
    messageHash.put("XML_DocumentNumber", "2");
    messageHash.put("UniqueNumber", handler.mqIntfReqQueue.getId().getQueryReqId() + "");
    messageHash.put("Operation", "update");
    messageHash.put("TransactionCode", "M");
    messageHash.put("CustomerNo", handler.mqIntfReqQueue.getCmrNo());
    messageHash.put("Country", handler.mqIntfReqQueue.getCmrIssuingCntry());
    messageHash.put("CountryID", LandedCountryMap.getSysLocDescription(handler.mqIntfReqQueue.getCmrIssuingCntry()));
    if ("729".equalsIgnoreCase(handler.mqIntfReqQueue.getCmrIssuingCntry()))
      messageHash.put("SourceCode", "C66");
    else
      messageHash.put("SourceCode", "21M");
    messageHash.put("Siret", siret);

  }

  @Override
  public void setValuesAfterInitialSuccess(MQMessageHandler handler) {
    Data cmrData = handler.cmrData;
    String siret = cmrData.getTaxCd1();
    if (StringUtils.isEmpty(siret) || (siret.toUpperCase().startsWith("SC") || siret.matches("[0]*"))) {
      // dummy SIRET format is either SCXXXXX+LOCN or all zeros
      if (StringUtils.isEmpty(this.customerLocation)) {
        this.customerLocation = getLocationNumber(handler, handler.addrData);
      }
      siret = "SC" + handler.mqIntfReqQueue.getCmrNo() + "0" + this.customerLocation;
      LOG.debug("Setting Dummy SIRET value " + siret + " for Request ID " + cmrData.getId().getReqId());
    }
  }

  protected void handleDataDefaults(MQMessageHandler handler, Map<String, String> messageHash, Data cmrData, boolean crossBorder, Addr addrData) {
    messageHash.put("SourceCode", "21M");
    String sbo = messageHash.get("SBO");
    if (!StringUtils.isEmpty(sbo) && sbo.length() < 4) {
      sbo = StringUtils.leftPad(sbo, 4, '0');
      messageHash.put("SBO", sbo);
    }
    messageHash.put("IBO", cmrData.getInstallBranchOff());
    String ibo = cmrData.getInstallBranchOff();
    if (!StringUtils.isEmpty(ibo) && ibo.length() < 4) {
      ibo = StringUtils.leftPad(ibo, 4, '0');
      messageHash.put("IBO", ibo);
    }

  }

  @Override
  public String[] getAddressOrder() {
    return ADDRESS_ORDER;
  }

  @Override
  public String getAddressKey(String addrType) {
    switch (addrType) {
    case "ZS02":
      return "Mail";
    case "ZS01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Billing";
    case "ZP02":
      return "Payment";
    case "ZI01":
      return "Soft";
    case "ZD02":
      return "CntryUseH";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS02":
      return "Mailing";
    case "ZS01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZP02":
      return "Payment";
    case "ZI01":
      return "EPL";
    case "ZD02":
      return "CountryUseH";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.FRANCE;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "1";
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_FR_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP02:
      return MQMsgConstants.SOF_ADDRESS_USE_FR_BILLING;
    case MQMsgConstants.ADDR_ZD02:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_H;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  protected boolean isCrossBorder(Data cmrData, MQMessageHandler handler) {
    String cntryUse = cmrData.getCountryUse();

    String landCntry = getCountryInZS01(handler);

    if (StringUtils.isEmpty(cntryUse) || StringUtils.isEmpty(landCntry))
      return true;
    String cmrIssuingCntry = "";
    if (cntryUse.length() > 3)
      cmrIssuingCntry = cntryUse.substring(3, 5);
    else
      cmrIssuingCntry = "FR";
    return !landCntry.equals(cmrIssuingCntry);

  }

  private void setValueByScenario(Map<String, String> messageHash, String scenario, String country) {
    if (MQMsgConstants.CUSTSUBGRP_COMME.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "D3");
      messageHash.put(MQMsgConstants.AUTH_REMARK, "NO");
    } else if (MQMsgConstants.CUSTSUBGRP_BPIEU.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "107");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "R5");
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put(MQMsgConstants.AUTH_REMARK, "YES");
    } else if (MQMsgConstants.CUSTSUBGRP_BPUEU.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "127");
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put(MQMsgConstants.AUTH_REMARK, "YES");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "R5");
    } else if (MQMsgConstants.CUSTSUBGRP_PRICU.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
    } else if (MQMsgConstants.CUSTSUBGRP_GOVRN.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
      messageHash.put(MQMsgConstants.PENALTIESDERETARD, "N");
      messageHash.put(MQMsgConstants.TYPEDEFACTURATION, "D");
    } else if (MQMsgConstants.CUSTSUBGRP_INTER.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
    } else if (MQMsgConstants.CUSTSUBGRP_INTSO.equals(scenario)) {
      messageHash.put(MQMsgConstants.SOFTYPEOFALLIANCE, "AA");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "FM");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "395");
    } else if (MQMsgConstants.CUSTSUBGRP_LCIFF.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "F3");
      messageHash.put("LeasingCompany", "Y");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "145");
    } else if (MQMsgConstants.CUSTSUBGRP_LCIFL.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "F3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "155");
    } else if (MQMsgConstants.CUSTSUBGRP_OTFIN.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "F3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "115");
    } else if (MQMsgConstants.CUSTSUBGRP_LEASE.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "L3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "114");
    } else if (MQMsgConstants.CUSTSUBGRP_FIBAB.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "D3");
      messageHash.put(MQMsgConstants.AUTH_REMARK, "NO");
    } else if (MQMsgConstants.CUSTSUBGRP_IBMEM.equals(scenario)) { // LOCAL IBM
                                                                   // EMPLOYEE
      /* messageHash.put(MQMsgConstants.NATURE_CLIENT, "111"); */
      messageHash.put(MQMsgConstants.AUTH_REMARK, "NO");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "D3");
      messageHash.put("CustomerType", "71");
    } else if (MQMsgConstants.CUSTSUBGRP_CBMME.equals(scenario)) {
    } else if (MQMsgConstants.CUSTSUBGRP_CBIEU.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "107");
      messageHash.put(MQMsgConstants.AUTH_REMARK, "YES");
      messageHash.put("MarketingResponseCode", "5");
    } else if (MQMsgConstants.CUSTSUBGRP_CBUEU.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "127");
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put(MQMsgConstants.AUTH_REMARK, "YES");
    } else if (MQMsgConstants.CUSTSUBGRP_CBICU.equals(scenario)) {
    } else if (MQMsgConstants.CUSTSUBGRP_CBVRN.equals(scenario)) {
      messageHash.put(MQMsgConstants.PENALTIESDERETARD, "N");
      messageHash.put(MQMsgConstants.TYPEDEFACTURATION, "D");
    } else if (MQMsgConstants.CUSTSUBGRP_CBTER.equals(scenario)) {
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
    } else if (MQMsgConstants.CUSTSUBGRP_CBTSO.equals(scenario)) {
      messageHash.put(MQMsgConstants.SOFTYPEOFALLIANCE, "AA");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "FM");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "395");
    } else if (MQMsgConstants.CUSTSUBGRP_CBIFF.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "F3");
      messageHash.put("LeasingCompany", "Y");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "145");
    } else if (MQMsgConstants.CUSTSUBGRP_CBIFL.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "F3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "155");
    } else if (MQMsgConstants.CUSTSUBGRP_CBFIN.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "F3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "115");
    } else if (MQMsgConstants.CUSTSUBGRP_CBASE.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "L3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "114");
    } else if (MQMsgConstants.CUSTSUBGRP_HOSTC.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "D3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
    } else if (MQMsgConstants.CUSTSUBGRP_CBSTC.equals(scenario)) {
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "D3");
      messageHash.put(MQMsgConstants.NATURE_CLIENT, "111");
    } else if (MQMsgConstants.CUSTSUBGRP_CBBAB.equals(scenario)) {
    } else if (MQMsgConstants.CUSTSUBGRP_CBIEM.equals(scenario)) { // CB IBM
                                                                   // EMPLOYEE
      messageHash.put(MQMsgConstants.AUTH_REMARK, "NO");
      messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "D3");
      messageHash.put("CustomerType", "71");
    }
  }

  /**
   * Calls the Generate CMR no service to get the next available CMR no
   * 
   * @param handler
   * @param targetSystem
   * @return
   */
  protected String generateCMRNoForTunisiaAndAlgeria(MQMessageHandler handler, String targetSystem) {

    try {
      GenerateCMRNoRequest request = new GenerateCMRNoRequest();
      request.setLoc1(handler.mqIntfReqQueue.getCmrIssuingCntry());
      request.setLoc2(targetSystem);
      request.setMandt(SystemConfiguration.getValue("MANDT"));
      request.setSystem(GenerateCMRNoClient.SYSTEM_SOF);
      if (!StringUtils.isEmpty(handler.cmrData.getAbbrevNm()) && handler.cmrData.getAbbrevNm().contains("IBM")) {
        request.setMin(990000);
        request.setMax(999999);
      }
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

  private void setVauleByCountry(String country, String type, String[] ArrayValue, Map<String, String> messageHash) {
    switch (country) {
    case SystemLocation.FRANCE:
      messageHash.put(type, ArrayValue[0]);
      break;
    case SystemLocation.MONACO:
      messageHash.put(type, ArrayValue[1]);
      break;
    case SystemLocation.GUADELOUPE:
      messageHash.put(type, ArrayValue[2]);
      break;
    case SystemLocation.FRENCH_GUIANA:
      messageHash.put(type, ArrayValue[3]);
      break;
    case SystemLocation.MARTINIQUE:
      messageHash.put(type, ArrayValue[4]);
      break;
    case SystemLocation.REUNION:
      messageHash.put(type, ArrayValue[5]);
      break;
    case SystemLocation.SAINT_PIERRE_MIQUELON:
      messageHash.put(type, ArrayValue[6]);
      break;
    case SystemLocation.COMOROS:
      messageHash.put(type, ArrayValue[7]);
      break;
    case SystemLocation.VANUATU:
      messageHash.put(type, ArrayValue[8]);
      break;
    case SystemLocation.FRENCH_POLYNESIA_TAHITI:
      messageHash.put(type, ArrayValue[9]);
      break;
    case SystemLocation.MAYOTTE:
      messageHash.put(type, ArrayValue[10]);
      break;
    case SystemLocation.NEW_CALEDONIA:
      messageHash.put(type, ArrayValue[11]);
      break;
    case SystemLocation.WALLIS_FUTUNA:
      messageHash.put(type, ArrayValue[12]);
      break;
    case SystemLocation.ANDORRA:
      messageHash.put(type, ArrayValue[13]);
      break;
    case SystemLocation.ALGERIA:
      messageHash.put(type, ArrayValue[14]);
      break;
    case SystemLocation.TUNISIA:
      messageHash.put(type, ArrayValue[15]);
      break;
    // default:
    // messageHash.put(type, "");
    }
  }

  private String getCountryInZS01(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getLandCntry();
      }
    return "";
  }

  private String getPostCodeInZS01(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return addr.getPostCd();
      }
    return "";
  }

  private boolean hasHAddress(MQMessageHandler handler) {
    List<Addr> addrList = handler.currentAddresses;
    if (addrList != null && addrList.size() > 0)
      for (Addr addr : addrList) {
        if (CmrConstants.ADDR_TYPE.ZD02.toString().equalsIgnoreCase(addr.getId().getAddrType()))
          return true;
      }
    return false;
  }

  @Override
  public boolean shouldCompleteProcess(EntityManager entityManager, MQMessageHandler handler, String responseStatus, boolean fromUpdateFlow) {
    if (isDoubleCreate(handler)) {
      try {
        if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId())) {
          return true;
        }
        MqIntfReqQueue currentQ = handler.mqIntfReqQueue;
        if ("Y".equals(currentQ.getMqInd()) || MQMsgConstants.REQ_STATUS_COM.equals(currentQ.getReqStatus())) {
          LOG.debug("MQ record already previously completed, skipping double creation process.");
          return true;
        }
        LOG.debug("Completing initial request " + currentQ.getId().getQueryReqId());
        Timestamp ts = SystemUtil.getCurrentTimestamp();
        currentQ.setReqStatus(MQMsgConstants.REQ_STATUS_COM);
        currentQ.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
        currentQ.setLastUpdtTs(ts);
        currentQ.setMqInd("Y");

        MqIntfReqQueue fr729Q = new MqIntfReqQueue();
        MqIntfReqQueuePK fr729QPk = new MqIntfReqQueuePK();
        long id = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "QUERY_REQ_ID", "CREQCMR");
        LOG.debug("Creating 729 request for Algeria or Tunisia with MQ ID " + id);
        fr729QPk.setQueryReqId(id);
        fr729Q.setId(fr729QPk);

        fr729Q.setCmrIssuingCntry("729");
        fr729Q.setCmrNo(currentQ.getCmrNo());
        fr729Q.setCorrelationId(currentQ.getId().getQueryReqId() + "");
        fr729Q.setCreateBy(MQMsgConstants.MQ_APP_USER);
        fr729Q.setCreateTs(ts);
        fr729Q.setLastUpdtBy(MQMsgConstants.MQ_APP_USER);
        fr729Q.setLastUpdtTs(ts);
        fr729Q.setMqInd("N");
        fr729Q.setReqId(currentQ.getReqId());
        fr729Q.setReqStatus(MQMsgConstants.REQ_STATUS_NEW);
        fr729Q.setReqType(currentQ.getReqType());
        fr729Q.setTargetSys(currentQ.getTargetSys());

        handler.createPartialComment("Handling 729 record for Algeria or Tunisia", handler.mqIntfReqQueue.getCmrNo());
        entityManager.merge(currentQ);
        entityManager.persist(fr729Q);
        entityManager.flush();

        return false;
      } catch (Exception e) {
        LOG.error("Error in completing 729 request. Skipping 729 record generation and completing request", e);
        return true;
      }
    } else {
      return true;
    }

  }

  private void setDefaultAddr729Values(MQMessageHandler handler) {
    handler.messageHash.put("Country", "729");
    handler.messageHash.put("SourceCode", "C66");
    if (SystemLocation.ALGERIA.equalsIgnoreCase(handler.cmrData.getCountryUse()))
      handler.messageHash.put("CountryID", "Algeria");
    if (SystemLocation.TUNISIA.equalsIgnoreCase(handler.cmrData.getCountryUse()))
      handler.messageHash.put("CountryID", "Tunisia");
  }

  private void setDefault729Values(MQMessageHandler handler) {
    LOG.debug("Handling Data CountryUse for " + handler.cmrData.getCountryUse());
    handler.messageHash.put("Country", "729");

    handler.messageHash.put("SourceCode", "C66");

    if (SystemLocation.ALGERIA.equalsIgnoreCase(handler.cmrData.getCountryUse())) {
      handler.messageHash.put("AbbreviatedLocation", handler.cmrData.getAbbrevLocn());
      handler.messageHash.put("CountryID", "Algeria");
      if ("C".equals(handler.adminData.getReqType())) {
        handler.messageHash.put("SBO", "7110000");
        handler.messageHash.put("IBO", "7110000");
        handler.messageHash.put("SR", "A99999");
      }
    }
    if (SystemLocation.TUNISIA.equalsIgnoreCase(handler.cmrData.getCountryUse())) {
      handler.messageHash.put("AbbreviatedLocation", handler.cmrData.getAbbrevLocn());
      handler.messageHash.put("CountryID", "Tunisia");
      if ("C".equals(handler.adminData.getReqType())) {
        handler.messageHash.put("SBO", "7210000");
        handler.messageHash.put("IBO", "7210000");
        handler.messageHash.put("SR", "D99999");
      }
    }
    if ("U".equals(handler.adminData.getReqType())) {
      String sbo = handler.cmrData.getSalesBusOffCd();
      if (!StringUtils.isEmpty(sbo) && sbo.length() < 7) {
        sbo = StringUtils.rightPad(sbo, 7, '0');
        handler.messageHash.put("SBO", sbo);
      }
      String ibo = handler.cmrData.getInstallBranchOff();
      if (!StringUtils.isEmpty(ibo) && ibo.length() < 7) {
        ibo = StringUtils.rightPad(ibo, 7, '0');
        handler.messageHash.put("IBO", ibo);
      }
    }
    handler.messageHash.put("MarketingResponseCode", "2");
    handler.messageHash.put("CEdivision", "2");
    handler.messageHash.put("DPCEBO", "0720000");
    handler.messageHash.put(MQMsgConstants.DATEOFORIGINALAGREEMENT, MQMsgConstants.DATE_FORMATTER.format(handler.mqIntfReqQueue.getCreateTs()));
    handler.messageHash.put(MQMsgConstants.SIGLE_IDENTIF, "");
    handler.messageHash.put(MQMsgConstants.NATURE_CLIENT, "");
    handler.messageHash.put(MQMsgConstants.AUTH_REMARK, "NO");
    handler.messageHash.put(MQMsgConstants.DEPARTMENT_CODE, "");
    handler.messageHash.put(MQMsgConstants.SIRET, "");
    handler.messageHash.put(MQMsgConstants.CODE_APE, "");
    handler.messageHash.put("TaxCode", "");
    handler.messageHash.put("ISU", handler.cmrData.getIsuCd() + handler.cmrData.getClientTier());
    handler.messageHash.put("VAT", "");

    LOG.debug("Request Type " + handler.mqIntfReqQueue.getReqType());
    if (CmrConstants.REQ_TYPE_UPDATE.equals(handler.mqIntfReqQueue.getReqType())) {
      String locNo = !StringUtils.isEmpty(handler.cmrData.getSubIndustryCd())
          ? handler.mqIntfReqQueue.getCmrIssuingCntry() + handler.cmrData.getSubIndustryCd() : "";
      LOG.debug("Computed Location No: " + locNo);
      handler.messageHash.put("LocationNumber", locNo);
      handler.messageHash.put("CustomerLocationNo", locNo);
    }
  }

  private boolean isDoubleCreate(MQMessageHandler handler) {
    return SystemLocation.TUNISIA.equalsIgnoreCase(handler.cmrData.getCountryUse())
        || (SystemLocation.ALGERIA.equalsIgnoreCase(handler.cmrData.getCountryUse()) && "Y".equalsIgnoreCase(handler.cmrData.getIdentClient()));
  }

  private String getLocationNumber(MQMessageHandler handler, Addr addr) {
    String postCD = getPostCodeInZS01(handler);

    try {
      return handler.getCustomerLocationCode(addr, postCD);
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public boolean shouldSendAddress(EntityManager entityManager, MQMessageHandler handler, Addr nextAddr) {
    if (nextAddr == null) {
      return false;
    }
    if (!StringUtils.isEmpty(handler.mqIntfReqQueue.getCorrelationId()) && "ZSIR".equals(nextAddr.getId().getAddrType())) {
      return false;
    }

    return true;
  }
}
