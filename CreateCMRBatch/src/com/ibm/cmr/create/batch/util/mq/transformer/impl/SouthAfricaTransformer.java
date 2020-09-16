/**
 * 
 */
package com.ibm.cmr.create.batch.util.mq.transformer.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassUpdtAddr;
import com.ibm.cio.cmr.request.entity.MassUpdtData;
import com.ibm.cio.cmr.request.model.BatchEmailModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemParameters;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.create.batch.util.CMRRequestContainer;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.services.client.cmrno.GenerateCMRNoRequest;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SouthAfricaTransformer extends MCOTransformer {

  private static final Logger LOG = Logger.getLogger(MCOTransformer.class);
  private static final String DEFAULT_CLEAR_NUM = "0";

  public SouthAfricaTransformer() {
    super(SystemLocation.SOUTH_AFRICA);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {

    Map<String, String> messageHash = handler.messageHash;
    messageHash.put("MarketingResponseCode", "2");
    messageHash.put("CEdivision", "2");
    messageHash.put("CurrencyCode", "SA");

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Map<String, String> messageHash = handler.messageHash;
    Addr addrData = handler.addrData;

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
    String line7 = "";

    // line1 is always customer name
    line1 = addrData.getCustNm1();
    line2 = addrData.getCustNm2();

    String attnPerson = addrData.getCustNm4();
    if (StringUtils.isNotBlank(attnPerson) && !hasAttnPrefix(attnPerson)) {
      attnPerson = "Att: " + attnPerson;
    }

    line3 = attnPerson;
    line4 = addrData.getAddrTxt();

    String streetContPoBox = addrData.getAddrTxt2();
    if (StringUtils.isNotBlank(streetContPoBox) && StringUtils.isNotBlank(addrData.getPoBox())) {
      streetContPoBox += ", PO BOX " + addrData.getPoBox();
    } else if (StringUtils.isBlank(streetContPoBox) && StringUtils.isNotBlank(addrData.getPoBox())) {
      streetContPoBox = "PO BOX " + addrData.getPoBox();
    }

    line5 = streetContPoBox;

    String cityPostalCode = addrData.getCity1();
    if (StringUtils.isNotBlank(cityPostalCode) && StringUtils.isNotBlank(addrData.getPostCd())) {
      cityPostalCode += ", " + addrData.getPostCd();
    }

    line6 = cityPostalCode;
    if (crossBorder) {
      line7 = LandedCountryMap.getCountryName(addrData.getLandCntry());
    }

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6, line7 };
    int lineNo = 1;
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6 + " | " + line7);

    for (String line : lines) {
      if (StringUtils.isNotBlank(line)) {
        messageHash.put(addrKey + "Address" + lineNo, line);
        lineNo++;
      }
    }

    if (lineNo == 5) {
      messageHash.put(addrKey + "Address5", null);
      messageHash.put(addrKey + "Address6", null);
    } else if (lineNo == 6) {
      messageHash.put(addrKey + "Address6", null);
    }
  }

  private boolean hasAttnPrefix(String attnPerson) {
    String[] attPersonPrefix = { "Att:", "Att", "Attention Person", "ATT:" };
    boolean isPrefixFound = false;

    for (String prefix : attPersonPrefix) {
      if (!isPrefixFound) {
        if (attnPerson.startsWith(prefix)) {
          isPrefixFound = true;
        }
      }
    }
    return isPrefixFound;
  }

  @Override
  public void transformLegacyAddressData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust, CmrtAddr legacyAddr,
      CMRRequestContainer cmrObjects, Addr currAddr) {
    LOG.debug("transformLegacyAddressData South Africa transformer...");
    formatAddressLines(dummyHandler);
    if ("ZD01".equals(currAddr.getId().getAddrType())) {
      legacyAddr.setAddrPhone(currAddr.getCustPhone());
    }

    if ("ZS01".equals(currAddr.getId().getAddrType())) {
      String abbrevLoc = LandedCountryMap.getCountryName(currAddr.getLandCntry());
      if (!StringUtils.isEmpty(abbrevLoc)) {
        LOG.debug("Setting Abbreviated Location as parent country for GM LLC");
        legacyCust.setAbbrevLocn(abbrevLoc);
      }
    }

    String poBox = currAddr.getPoBox();
    if (!StringUtils.isEmpty(poBox)) {
      if (!poBox.startsWith("PO BOX ")) {
        legacyAddr.setPoBox("PO BOX " + currAddr.getPoBox());
      } else {
        legacyAddr.setPoBox(poBox);
      }
    }
  }

  @Override
  public void transformLegacyCustomerData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCust legacyCust,
      CMRRequestContainer cmrObjects) {
    LOG.debug("transformLegacyCustomerData South Africa transformer...");
    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    List<String> gmllcScenarios = Arrays.asList("NALLC", "LSLLC", "SZLLC", "NABLC", "LSBLC", "SZBLC");
    // formatDataLines(dummyHandler);
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      String custSubGrp = data.getCustSubGrp();
      String[] busPrSubGrp = { "LSBP", "SZBP", "ZABP", "NABP", "ZAXBP", "NAXBP", "LSXBP", "SZXBP" };

      boolean isBusPr = Arrays.asList(busPrSubGrp).contains(custSubGrp);
      if (isBusPr) {
        legacyCust.setAuthRemarketerInd("1");
      } else {
        legacyCust.setAuthRemarketerInd("0");
      }

      legacyCust.setCeDivision("2");
      legacyCust.setCurrencyCd("SA");
      legacyCust.setTaxCd(data.getSpecialTaxCd());
      legacyCust.setMrcCd("2");
      legacyCust.setCollectionCd("00001");

    }

    if (!StringUtils.isBlank(data.getSalesBusOffCd())) {
      legacyCust.setIbo(data.getSalesBusOffCd());
      legacyCust.setSbo(data.getSalesBusOffCd());
    } else {
      legacyCust.setIbo("");
      legacyCust.setSbo("");
    }

    if (!StringUtils.isBlank(data.getSalesTeamCd())) {
      legacyCust.setSalesGroupRep(data.getSalesTeamCd());
    } else {
      legacyCust.setSalesGroupRep("");
    }

    for (Addr addr : cmrObjects.getAddresses()) {
      if (MQMsgConstants.ADDR_ZS01.equals(addr.getId().getAddrType())) {
        legacyCust.setTelNoOrVat(addr.getCustPhone());
      }
    }

    if (!StringUtils.isBlank(data.getIbmDeptCostCenter())) {
      if (data.getIbmDeptCostCenter().length() == 6)
        legacyCust.setDeptCd(data.getIbmDeptCostCenter().substring(2, 6));
    }

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      legacyCust.setModeOfPayment(data.getCommercialFinanced());
      if (data.getCodCondition() != null) {
        String cod = data.getCreditCd();
        if ("Y".equals(cod)) {
          legacyCust.setModeOfPayment("5");
        } else if ("N".equals(cod)) {
          legacyCust.setModeOfPayment("");
        }
      }

      String dataEmbargoCd = data.getEmbargoCd();
      String rdcEmbargoCd = LegacyDirectUtil.getEmbargoCdFromDataRdc(entityManager, admin); // permanent
                                                                                            // removal-single
      // inactivation
      if (admin.getReqReason() != null && !StringUtils.isBlank(admin.getReqReason()) && !"TREC".equals(admin.getReqReason())) {
        if (!StringUtils.isBlank(rdcEmbargoCd) && ("Y".equals(rdcEmbargoCd))) {
          if (StringUtils.isBlank(data.getEmbargoCd())) {
            legacyCust.setEmbargoCd("");
          }
        }
      }
    }

    legacyCust.setAbbrevNm(data.getAbbrevNm());
    legacyCust.setLangCd("1");
    legacyCust.setMrcCd("2");
    legacyCust.setCustType(data.getCrosSubTyp());
    legacyCust.setSalesGroupRep(data.getRepTeamMemberNo());
    legacyCust.setBankBranchNo("");
    legacyCust.setCreditCd("");

    // append GM in AbbrevName for GM LLC
    if (!StringUtils.isEmpty(data.getCustSubGrp()) && gmllcScenarios.contains(data.getCustSubGrp())) {
      String abbrevNm = data.getAbbrevNm();
      if (!StringUtils.isEmpty(abbrevNm) && !abbrevNm.toUpperCase().endsWith(" GM")) {
        if (abbrevNm.length() > 19) {
          abbrevNm = abbrevNm.substring(0, 19);
        }
        LOG.debug("Setting Abbreviated Name for GM LLC");
        legacyCust.setAbbrevNm(abbrevNm + " GM");
      }
    }
  }

  @Override
  public boolean hasCmrtCustExt() {
    return true;
  }

  @Override
  public void transformLegacyCustomerExtData(EntityManager entityManager, MQMessageHandler dummyHandler, CmrtCustExt legacyCustExt,
      CMRRequestContainer cmrObjects) {
    Admin admin = cmrObjects.getAdmin();
    Data data = cmrObjects.getData();
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      legacyCustExt.setTeleCovRep("Z13100");
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      String collBO = data.getCollBoId();
      if (!StringUtils.isEmpty(collBO) && collBO.length() == 4) {
        collBO = "Z1" + collBO;
      } else if (StringUtils.isEmpty(collBO)) {
        collBO = "";
      }
      legacyCustExt.setTeleCovRep(collBO);
    }
  }

  @Override
  public void transformLegacyAddressDataMassUpdate(EntityManager entityManager, CmrtAddr legacyAddr, MassUpdtAddr muAddr, String cntry, CmrtCust cust,
      Data data, LegacyDirectObjectContainer legacyObjects) {

    LegacyCommonUtil.transformBasicLegacyAddressMassUpdate(entityManager, legacyAddr, muAddr, cntry, cust, data);

    if (!StringUtils.isBlank(muAddr.getPostCd())) {
      legacyAddr.setZipCode(muAddr.getPostCd());
    }

    if (!StringUtils.isBlank(muAddr.getCounty())) {
      if (muAddr.getId().getAddrType().equals("ZD01")) {
        if (DEFAULT_CLEAR_NUM.equals(muAddr.getCounty().trim())) {
          legacyAddr.setAddrPhone("");
        } else {
          legacyAddr.setAddrPhone(muAddr.getCounty());
        }
      }
    }
    formatMassUpdateAddressLines(entityManager, legacyAddr, muAddr, false);
    legacyObjects.addAddress(legacyAddr);
  }

  @Override
  public void transformLegacyCustomerDataMassUpdate(EntityManager entityManager, CmrtCust legacyCust, CMRRequestContainer cmrObjects,
      MassUpdtData muData) {
    LOG.debug("ZA >> Mapping default Data values..");
    LegacyCommonUtil.setlegacyCustDataMassUpdtFields(entityManager, legacyCust, muData);

    List<MassUpdtAddr> muaList = cmrObjects.getMassUpdateAddresses();
    if (muaList != null && muaList.size() > 0) {
      for (MassUpdtAddr mua : muaList) {
        if ("ZS01".equals(mua.getId().getAddrType())) {
          if (!StringUtils.isBlank(mua.getCounty())) {
            if (DEFAULT_CLEAR_NUM.equals(mua.getCounty().trim())) {
              legacyCust.setTelNoOrVat("");
            } else {
              legacyCust.setTelNoOrVat(mua.getCounty());
            }
          }
          break;
        }
      }
    }

  }

  @Override
  public void generateCMRNoByLegacy(EntityManager entityManager, GenerateCMRNoRequest generateCMRNoObj, CMRRequestContainer cmrObjects) {
    LOG.debug("Set max and min range For ZA...");
    Data data = cmrObjects.getData();
    String custSubGrp = data.getCustSubGrp();
    String[] interalSubGrp = { "ZAXIN", "NAXIN", "LSXIN", "SZXIN", "ZAINT", "NAINT", "LSINT", "SZINT" };
    boolean isInternal = Arrays.asList(interalSubGrp).contains(custSubGrp);

    if (isInternal) {
      generateCMRNoObj.setMin(990000);
      generateCMRNoObj.setMax(999999);
    }
  }

  @Override
  public boolean enableTempReactOnUpdates() {
    return true;
  }

  @Override
  public boolean isUpdateNeededOnAllAddressType(EntityManager entityManager, CMRRequestContainer cmrObjects) {
    List<Addr> addresses = cmrObjects.getAddresses();
    for (Addr addr : addresses) {
      if ("ZS01".equals(addr.getId().getAddrType())) {
        AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
        String currPhone = addr.getCustPhone() != null ? addr.getCustPhone() : "";
        String oldPhone = addrRdc.getCustPhone() != null ? addrRdc.getCustPhone() : "";
        if (addrRdc == null || (addrRdc != null && !currPhone.equals(oldPhone))) {
          return true;
        }
      } else if (!"ZS01".equals(addr.getId().getAddrType()) && StringUtils.isEmpty(addr.getSapNo())) {
        addr.setChangedIndc("Y");
      }
    }

    return false;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "00001";
  }

  @Override
  public String getGmllcDupCreation(Data data) {
    List<String> validScenarios = Arrays.asList("NALLC", "LSLLC", "SZLLC", "NABLC", "LSBLC", "SZBLC");
    if (data != null && StringUtils.isNotEmpty(data.getCustSubGrp()) && validScenarios.contains(data.getCustSubGrp())) {
      return "764";
    }
    return "NA";
  }

  @Override
  public void transformLegacyDataForDupCreation(EntityManager entityManager, LegacyDirectObjectContainer legacyObjects,
      CMRRequestContainer cmrObjects) {
    CmrtCust legacyCust = legacyObjects.getCustomer();
    Data data = cmrObjects.getData();
    List<String> bpGMLLCScenarios = Arrays.asList("NABLC", "LSBLC", "SZBLC");
    boolean isGMLLC = false;
    if (data != null) {
      if (!"NA".equals(getGmllcDupCreation(data))) {
        isGMLLC = true;
      }
      if (legacyCust != null && isGMLLC) {
        if (bpGMLLCScenarios.contains(data.getCustSubGrp())) {
          legacyCust.setIsuCd("8B7");
          legacyCust.setSalesRepNo("DUMMY1");
          legacyCust.setSbo("0010000");
          legacyCust.setIbo("0010000");
          legacyCust.setInacCd("");
        } else {
          legacyCust.setIsuCd("32S");
          legacyCust.setSalesRepNo("DUMMY1");
          legacyCust.setSbo("0080000");
          legacyCust.setIbo("0080000");
        }
      }
    }
  }

  @Override
  public String getMailSendingFlag(Data data, Admin admin, EntityManager entityManager) {
    String oldCOD = "";
    String oldCOF = "";
    String sql = ExternalizedQuery.getSql("GET.OLD_COD_COF_BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      oldCOD = result[0] != null ? (String) result[0] : "";
      oldCOF = result[1] != null ? (String) result[0] : "";
    }

    if (StringUtils.isNotBlank(oldCOF) && StringUtils.isBlank(data.getCommercialFinanced())) {
      return "COF";
    } else if (StringUtils.isNotBlank(oldCOD) && StringUtils.isBlank(data.getCreditCd())) {
      return "COD";
    } else {
      return "NA";
    }
  }

  @Override
  public String getEmailTemplateName(String type) {
    if ("COD".equals(type)) {
      return "cmr-email_cod.html";
    } else {
      return "cmr-email_cof.html";
    }

  }

  @Override
  public BatchEmailModel getMailFormatParams(EntityManager entityManager, CMRRequestContainer cmrObjects, String type) {
    BatchEmailModel params = new BatchEmailModel();
    Data data = cmrObjects.getData();
    Admin admin = cmrObjects.getAdmin();
    String directUrlLink = SystemConfiguration.getValue("APPLICATION_URL") + "/login?r=" + admin.getId().getReqId();
    directUrlLink = "Click <a href=\"" + directUrlLink + "\">Here</a>";
    params.setRequesterName(admin.getRequesterNm());
    params.setRequesterId(admin.getRequesterId());
    params.setRequestId(Long.toString(admin.getId().getReqId()));
    String custNm = admin.getMainCustNm1() + (StringUtils.isNotBlank(admin.getMainCustNm2()) ? ("" + admin.getMainCustNm2()) : "");
    params.setCustNm(custNm.trim());
    params.setIssuingCountry(data.getCmrIssuingCntry());
    params.setCmrNumber(data.getCmrNo());
    String subregion = data.getCountryUse();
    subregion = StringUtils.isNotBlank(subregion) && subregion.length() > 3 ? subregion.substring(3, 5) : "";
    params.setSubregion(subregion);
    params.setDirectUrlLink(directUrlLink);
    String receipent = "";
    String mailSubject = "";
    if ("COD".equals(type)) {
      mailSubject = SystemParameters.getString("COD_MAIL_SUBJECT");
      receipent = SystemParameters.getString("COD_MAIL_RECIEVER");
    } else if ("COF".equals(type)) {
      mailSubject = SystemParameters.getString("COF_MAIL_SUBJECT");
      receipent = SystemParameters.getString("COF_MAIL_RECIEVER");
    }
    params.setReceipent(receipent);
    params.setMailSubject(mailSubject);
    params.setStringToReplace("xxxxxx");
    params.setValToBeReplaceBy(data.getCmrNo());
    return params;
  }

  @Override
  public String getReqStatusForSendingMail(String mailFlag) {
    if ("COD".equals(mailFlag)) {
      return "PCO";
    } else if ("COF".equals(mailFlag)) {
      return "COM";
    }
    return null;
  }

}
