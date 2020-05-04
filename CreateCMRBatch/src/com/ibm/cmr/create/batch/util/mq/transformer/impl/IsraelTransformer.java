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
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;
import com.ibm.cmr.create.batch.util.mq.MQMsgConstants;
import com.ibm.cmr.create.batch.util.mq.handler.MQMessageHandler;
import com.ibm.cmr.create.batch.util.mq.transformer.MessageTransformer;

/**
 * {@link MessageTransformer} implementation for Israel.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class IsraelTransformer extends EMEATransformer {

  private static final Logger LOG = Logger.getLogger(IsraelTransformer.class);

  private static final String[] NO_UPDATE_FIELDS = { "OrganizationNo", "CurrencyCode", "ARemark" };

  private static final String[] ADDRESS_ORDER = { "ZS01", "ZP01", "ZI01", "ZD01", "ZS02", "CTYA", "CTYB", "CTYC" };

  // private static final String RIGHT_TO_LEFT_MARKER = "\u202e";

  /**
   */
  public IsraelTransformer() {
    super(SystemLocation.ISRAEL);

  }

  @Override
  public void formatDataLines(MQMessageHandler handler) {
    boolean update = "U".equals(handler.adminData.getReqType());
    Data cmrData = handler.cmrData;
    Addr addrData = handler.addrData;
    boolean crossBorder = !"IL".equals(addrData.getLandCntry());

    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;

    handleEMEADefaults(handler, messageHash, cmrData, addrData, crossBorder);

    String embargoCode = !StringUtils.isEmpty(cmrData.getEmbargoCd()) ? cmrData.getEmbargoCd() : "";
    messageHash.put("EmbargoCode", embargoCode);

    messageHash.put("SourceCode", "EFO");
    messageHash.put("IBO", cmrData.getInstallBranchOff());
    messageHash.put("Country", SystemLocation.SAP_ISRAEL_SOF_ONLY);
    messageHash.put("MarketingResponseCode", "2");
    // 1317260 - For BP Scenarios send ARemark = YES when MRC code is 5
    if (MQMsgConstants.CUSTSUBGRP_BUSPR.equals(cmrData.getCustSubGrp())) {
      messageHash.put("MarketingResponseCode", "5");
      messageHash.put("ARemark", "YES");
    } else {
      messageHash.put("ARemark", "");
    }

    messageHash.put("CRCode", "90");

    // new tags for Israel
    messageHash.put("CICode", "3");
    messageHash.put("LangCode", "1");
    messageHash.put("CEdivision", "2");
    messageHash.put("InvNumber", "04");
    messageHash.put("DistrictCode", "0" + cmrData.getSubIndustryCd().substring(0, 1));

    messageHash.put("BankNumber", "");
    messageHash.put("BankBranchNumber", "");

    // MOD scenario
    String vat = cmrData.getVat();

    LOG.trace("Bank Number: " + handler.currentCMRValues.get("BankNumber"));
    boolean isMOD = handler.currentCMRValues.get("BankNumber") != null && handler.currentCMRValues.get("BankNumber").startsWith("9");
    LOG.debug("CMR is determined to be " + (isMOD ? "" : "non-") + "MOD from query service.");
    if ("MOD".equals(cmrData.getCustSubGrp()) || isMOD) {
      messageHash.put("CRCode", "01");
      if (!StringUtils.isEmpty(vat)) {
        messageHash.put("BankNumber", "9" + cmrData.getVat().substring(vat.length() - 1));
      }
    } else {
      if (!StringUtils.isEmpty(handler.cmrData.getVat())) {
        messageHash.put("BankNumber", "0" + cmrData.getVat().substring(vat.length() - 1));
      }
    }

    if (!crossBorder) {
      if (vat != null && vat.length() > 4) {
        messageHash.put("BankBranchNumber", vat.substring(2, vat.length() - 1));
      } else {
        messageHash.put("BankBranchNumber", "");
      }
    } else {
      // empty bank number and bank branch number for cross border
      messageHash.put("BankNumber", "");
      messageHash.put("BankBranchNumber", "");
    }

    if ("INTER".equals(cmrData.getCustSubGrp()) || "INTSO".equals(cmrData.getCustSubGrp())) {
      messageHash.put("CRCode", "91");
    }

    messageHash.put("EnterpriseNo", cmrData.getEnterprise());
    messageHash.put("CustomerType", "WR");

    if (StringUtils.isBlank(cmrData.getEconomicCd())) {
      if (cmrData.getSubIndustryCd() != null) {
        messageHash.put("EconomicCode", "0" + cmrData.getSubIndustryCd());
      }
    } else {
      messageHash.put("EconomicCode", cmrData.getEconomicCd());
    }

    // COD Flag
    String codFlag = !StringUtils.isEmpty(cmrData.getCreditCd()) ? cmrData.getCreditCd() : "";
    messageHash.put("COD", codFlag);

    if (update) {

      String currMrc = handler.currentCMRValues.get("MarketingResponseCode");
      LOG.debug("Current MRC: " + currMrc);
      if (!StringUtils.isEmpty(currMrc)) {
        messageHash.put("MarketingResponseCode", currMrc);
      } else {
        messageHash.put("MarketingResponseCode", "2");
      }

      for (String field : NO_UPDATE_FIELDS) {
        messageHash.remove(field);
      }
    }

  }

  @Override
  public void formatAddressLines(MQMessageHandler handler) {
    Addr addrData = handler.addrData;
    boolean update = "U".equals(handler.adminData.getReqType());
    boolean crossBorder = !"IL".equals(addrData.getLandCntry());

    String addrKey = getAddressKey(addrData.getId().getAddrType());
    LOG.debug("Handling " + (update ? "update" : "create") + " request.");
    Map<String, String> messageHash = handler.messageHash;
    boolean changeInd = "Y".equals(addrData.getChangedIndc());
    EntityManager entityManager = handler.getEntityManager();

    messageHash.put("SourceCode", "EFO");
    messageHash.remove(addrKey + "Name");
    messageHash.remove(addrKey + "ZipCode");
    messageHash.remove(addrKey + "City");
    messageHash.remove(addrKey + "POBox");

    LOG.debug("Handling  Data for " + addrData.getCustNm1());
    // <XXXAddress1> -> name
    // <XXXAddress2> -> ? no contact
    // <XXXAddress3> -> PO BOX (Department when street is not filled)
    // <XXXAddress4> -> Street (PO BOX when street is not filled)
    // <XXXAddress5> -> Postal code + City
    // <XXXAddress6> -> Country

    // customer name
    String line1 = addrData.getCustNm1();

    // name con't or attn
    String line2 = StringUtils.isBlank(addrData.getCustNm2()) ? addrData.getDept() : addrData.getCustNm2();

    // add phone to line 2
    if (!StringUtils.isBlank(addrData.getCustPhone())) {
      line2 += ", " + addrData.getCustPhone();
    }

    // PO BOX
    String line3 = "";
    if (!StringUtils.isBlank(addrData.getPoBox())) {
      line3 = addrData.getPoBox();
    }

    // Street
    String line4 = "";
    if (!StringUtils.isBlank(addrData.getAddrTxt())) {
      line4 = addrData.getAddrTxt();
    }

    // postal code + city
    String line5 = (!StringUtils.isEmpty(addrData.getPostCd()) ? addrData.getPostCd() : "") + " "
        + (!StringUtils.isEmpty(addrData.getCity1()) ? addrData.getCity1() : "");
    line5 = line5.trim();

    // country
    String line6 = "";

    boolean localAddressType = isLocalAddress(addrData);

    if (crossBorder) {
      if (localAddressType) {
        line6 = LandedCountryMap.getLovCountryName(addrData.getLandCntry());
      } else {
        line6 = LandedCountryMap.getCountryName(addrData.getLandCntry());
      }
    }

    int lineNo = 1;
    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    LOG.debug("Lines: " + line1 + " | " + line2 + " | " + line3 + " | " + line4 + " | " + line5 + " | " + line6);
    // fixed mapping value, not move up if blank
    if (!update) {
      for (String line : lines) {
        if (line != null && line.length() > 30) {
          line = line.substring(0, 30);
        }
        messageHash.put(addrKey + "Address" + lineNo, localAddressType ? reverseNumbers(line) : line);
        lineNo++;
      }
    } else {
      if (changeInd && localAddressType) {
        updateAddressLine(handler, entityManager, addrData, lines, addrKey);
      } else {
        for (String line : lines) {
          if (line != null && line.length() > 30) {
            line = line.substring(0, 30);
          }
          messageHash.put(addrKey + "Address" + lineNo, line);
          lineNo++;
        }
      }
    }

    String countryName = LandedCountryMap.getCountryName(addrData.getLandCntry());
    messageHash.put(getAddressKey(addrData.getId().getAddrType()) + "Country", crossBorder ? countryName : "");

    if (!update) {
      // for creates, send TransAddressNumber
      if (CmrConstants.ADDR_TYPE.CTYA.toString().equals(addrData.getId().getAddrType())) {
        messageHash.put("TransAddressNumber", "00001");
      } else if (CmrConstants.ADDR_TYPE.CTYB.toString().equals(addrData.getId().getAddrType())) {
        messageHash.put("TransAddressNumber", "00002");
      } else if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addrData.getId().getAddrType())) {
        List<Addr> addresses = handler.currentAddresses;

        int ctyCIndex = -1;
        int shipIndex = -1;

        int running = -1;
        for (Addr addr : addresses) {
          // count index from CTYC types only
          if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addr.getId().getAddrType())) {
            running++;
            if (addr.getId().getAddrType().equals(addrData.getId().getAddrType())
                && addr.getId().getAddrSeq().equals(addrData.getId().getAddrSeq())) {
              ctyCIndex = running;
              break;
            }
          }
        }

        running = -1;
        shipIndex = -1;
        if (ctyCIndex >= 0) {
          for (Addr addr : addresses) {
            running++;
            if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getId().getAddrType())) {
              shipIndex++;
              if (shipIndex == ctyCIndex) {
                // found the shipping index for the country use c
                messageHash.put("TransAddressNumber", StringUtils.leftPad((running + 1) + "", 5, '0'));
                LOG.trace("Country Use Trans Address Number: " + messageHash.get("TransAddressNumber"));
              }
            }
          }
        }
      }

    } else {
      LOG.debug("Checking TransNumber for new shipping addresses...");
      if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addrData.getId().getAddrType())) {
        List<Addr> addresses = handler.currentAddresses;

        int ctyCIndex = -1;
        int shipIndex = -1;

        int running = -1;
        for (Addr addr : addresses) {
          // count index from CTYC types only
          if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(addr.getId().getAddrType()) && !"Y".equals(addr.getImportInd())) {
            running++;
            if (addr.getId().getAddrType().equals(addrData.getId().getAddrType())
                && addr.getId().getAddrSeq().equals(addrData.getId().getAddrSeq())) {
              ctyCIndex = running;
              break;
            }
          }
        }

        running = -1;
        shipIndex = -1;
        if (ctyCIndex >= 0) {
          for (Addr addr : addresses) {
            running++;
            if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getId().getAddrType()) && !"Y".equals(addr.getImportInd())) {
              shipIndex++;
              if (shipIndex == ctyCIndex) {
                // found the shipping index for the country use c
                messageHash.put("TransAddressNumber",
                    addr.getId().getAddrSeq().length() == 5 ? addr.getId().getAddrSeq() : StringUtils.leftPad((running + 1) + "", 5, '0'));
                LOG.trace("Country Use Trans Address Number: " + messageHash.get("TransAddressNumber"));
              }
            }
          }
        }
      }

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
      return "Mail";
    case "ZI01":
      return "Install";
    case "ZD01":
      return "Ship";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "Soft";
    case "CTYA":
      return "CntryUseA";
    case "CTYB":
      return "CntryUseB";
    case "CTYC":
      return "CntryUseC";
    default:
      return "";
    }
  }

  @Override
  public String getTargetAddressType(String addrType) {
    switch (addrType) {
    case "ZS01":
      return "Mailing";
    case "ZI01":
      return "Installing";
    case "ZD01":
      return "Shipping";
    case "ZP01":
      return "Billing";
    case "ZS02":
      return "EPL";
    case "CTYA":
      return "CountryUseA";
    case "CTYB":
      return "CountryUseB";
    case "CTYC":
      return "CountryUseC";
    default:
      return "";
    }
  }

  @Override
  public String getSysLocToUse() {
    return SystemLocation.SAP_ISRAEL_SOF_ONLY;
  }

  @Override
  public String getFixedAddrSeqForProspectCreation() {
    return "1";
  }

  /**
   * Determins if this address is a local address for Israel
   * 
   * @param addr
   * @return
   */
  private boolean isLocalAddress(Addr addr) {
    return Arrays.asList("ZS01", "ZP01", "ZD01").contains(addr.getId().getAddrType());
  }

  @Override
  public String getAddressUse(Addr addr) {
    switch (addr.getId().getAddrType()) {
    case MQMsgConstants.ADDR_ZS01:
      return MQMsgConstants.SOF_ADDRESS_USE_MAILING;
    case MQMsgConstants.ADDR_ZP01:
      return MQMsgConstants.SOF_ADDRESS_USE_BILLING;
    case MQMsgConstants.ADDR_ZI01:
      return MQMsgConstants.SOF_ADDRESS_USE_INSTALLING;
    case MQMsgConstants.ADDR_ZD01:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    case MQMsgConstants.ADDR_ZS02:
      return MQMsgConstants.SOF_ADDRESS_USE_EPL;
    case MQMsgConstants.ADDR_CTYA:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_A;
    case MQMsgConstants.ADDR_CTYB:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_B;
    case MQMsgConstants.ADDR_CTYC:
      return MQMsgConstants.SOF_ADDRESS_USE_COUNTRY_USE_C;
    default:
      return MQMsgConstants.SOF_ADDRESS_USE_SHIPPING;
    }
  }

  // compare with old data in AddrRdc table
  public AddrRdc checkAddressLine(EntityManager entityManager, Addr addrData) {
    String sql = ExternalizedQuery.getSql("REQUESTENTRY.ADDRRDC.SEARCH_BY_REQID_TYPE_SEQ");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("REQ_ID", addrData.getId().getReqId());
    q.setParameter("ADDR_TYPE", addrData.getId().getAddrType());
    q.setParameter("ADDR_SEQ", addrData.getId().getAddrSeq());
    q.setForReadOnly(true);
    AddrRdc addr_rdc = new AddrRdc();
    try {
      addr_rdc = q.getSingleResult(AddrRdc.class);
    } catch (Exception e) {
      System.out.println(e);
      LOG.trace("Exception found i sql" + e);
    }
    return addr_rdc;
  }

  // To check for update scenario in case address fields are updated
  public void updateAddressLine(MQMessageHandler handler, EntityManager entityManager, Addr addrData, String[] lines, String addrKey) {
    Map<String, String> messageHash = handler.messageHash;
    AddrRdc oldAddrData = checkAddressLine(entityManager, addrData);
    boolean check;
    // customer name
    String line0 = oldAddrData.getCustNm1();
    check = line0.equals(lines[0]);
    if (lines[0] != null && lines[0].length() > 30) {
      lines[0] = lines[0].substring(0, 30);
    }
    messageHash.put(addrKey + "Address1", check ? lines[0] : reverseNumbers(lines[0]));
    // name con't or attn
    String line1 = StringUtils.isBlank(oldAddrData.getCustNm2()) ? oldAddrData.getDept() : oldAddrData.getCustNm2();
    // add phone to line 2
    if (!StringUtils.isBlank(oldAddrData.getCustPhone())) {
      line1 += ", " + oldAddrData.getCustPhone();
    }
    check = line1.equals(lines[1]);
    if (lines[1] != null && lines[1].length() > 30) {
      lines[1] = lines[1].substring(0, 30);
    }
    messageHash.put(addrKey + "Address2", check ? lines[1] : reverseNumbers(lines[1]));
    // PO BOX
    String line2 = "";
    if (!StringUtils.isBlank(oldAddrData.getPoBox())) {
      line2 = oldAddrData.getPoBox();
    }
    check = line2.equals(lines[2]);
    if (lines[2] != null && lines[2].length() > 30) {
      lines[2] = lines[2].substring(0, 30);
    }
    messageHash.put(addrKey + "Address3", check ? lines[2] : reverseNumbers(lines[2]));
    // Street
    String line3 = "";
    if (!StringUtils.isBlank(oldAddrData.getAddrTxt())) {
      line3 = oldAddrData.getAddrTxt();
    }
    check = line3.equals(lines[3]);
    if (lines[3] != null && lines[3].length() > 30) {
      lines[3] = lines[3].substring(0, 30);
    }
    messageHash.put(addrKey + "Address4", check ? lines[3] : reverseNumbers(lines[3]));
    // postal code + city
    String line4 = (!StringUtils.isEmpty(oldAddrData.getPostCd()) ? oldAddrData.getPostCd() : "") + " "
        + (!StringUtils.isEmpty(oldAddrData.getCity1()) ? oldAddrData.getCity1() : "");
    line4 = line4.trim();
    check = line4.equals(lines[4]);
    if (lines[4] != null && lines[4].length() > 30) {
      lines[4] = lines[4].substring(0, 30);
    }
    messageHash.put(addrKey + "Address5", check ? lines[4] : reverseNumbers(lines[4]));
    if (lines[5] != null && lines[5].length() > 30) {
      lines[5] = lines[5].substring(0, 30);
    }
    messageHash.put(addrKey + "Address6", lines[5]);
  }
}
