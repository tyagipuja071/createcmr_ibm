/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.Sadr;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for BELUX
 * 
 * @author max
 * 
 */
public class BELUXHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(BELUXHandler.class);
  private static final boolean RETRIEVE_INVALID_CUSTOMERS = true;

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.BELGIUM, "BE");

  }

  private static final String[] BELUX_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "GeoLocationCode", "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode",
      "LocalTax2", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "Office", "Floor", "Building", "County", "City2",
      "Department", "SalRepNameNo", "EngineeringBo", "SpecialTaxCd" };

  private static final List<String> ME_COUNTRIES_LIST = Arrays.asList(SystemLocation.BAHRAIN, SystemLocation.MOROCCO, SystemLocation.GULF,
      SystemLocation.UNITED_ARAB_EMIRATES, SystemLocation.ABU_DHABI, SystemLocation.IRAQ, SystemLocation.JORDAN, SystemLocation.KUWAIT,
      SystemLocation.LEBANON, SystemLocation.LIBYA, SystemLocation.OMAN, SystemLocation.PAKISTAN, SystemLocation.QATAR, SystemLocation.SAUDI_ARABIA,
      SystemLocation.YEMEN, SystemLocation.SYRIAN_ARAB_REPUBLIC, SystemLocation.EGYPT, SystemLocation.TUNISIA_SOF, SystemLocation.GULF);

  protected static final String[] MASS_UPDATE_SHEET_NAMES = { "Sold-to", "Mail-to", "Bill-to", "Ship-to", "Install-at" };

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;

      if (!StringUtils.isBlank(record.getCmrPOBox())) {
        if (!record.getCmrPOBox().startsWith("PO")) {
          record.setCmrPOBox(record.getCmrPOBox());
        }
      }
      if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
        record.setCmrAddrSeq("00001");
      } else {
        if (StringUtils.isNotBlank(reqEntry.getCmrIssuingCntry()) && "624".equals(reqEntry.getCmrIssuingCntry())
            && StringUtils.isNotBlank(record.getCmrNum()) && record.getCmrNum().startsWith("P") && record.getCmrAddrSeq().equals("A")) {
          record.setCmrAddrSeq("00001");
        } else {
          record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
        }
      }

      record.setCmrName2Plain(record.getCmrName2Plain());
      record.setCmrDept(null);
      record.setCmrSitePartyID(record.getCmrSitePartyID());

      // if
      // (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())
      // && "AT".equals(record.getCmrCountryLanded())) {
      if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode()) && "618".equals(reqEntry.getCmrIssuingCntry())) {

        record.setCmrAddrSeq("1");
      }
      converted.add(record);
    } else {
      String cmrIssueCd = reqEntry.getCmrIssuingCntry();
      String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
        if (source.getItems() != null) {
          String addrType = null;
          String seqNo = null;
          List<String> sofUses = null;
          FindCMRRecordModel addr = null;

          // map RDc - SOF - CreateCMR by sequence no
          for (FindCMRRecordModel record : source.getItems()) {
            seqNo = record.getCmrAddrSeq();
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo) && StringUtils.isEmpty(record.getExtWalletId())) {
              addrType = record.getCmrAddrTypeCode();
              if (!StringUtils.isEmpty(addrType)) {
                addr = cloneAddress(record, addrType);
                addr.setCmrDept(record.getCmrCity2());
                addr.setCmrName4(record.getCmrName4());
                converted.add(addr);
              }
            } else if ("ZP01".equals(record.getCmrAddrTypeCode()) && StringUtils.isNotEmpty(record.getExtWalletId())) {
              addrType = record.getCmrAddrTypeCode();
              if (!StringUtils.isEmpty(addrType)) {
                addr = cloneAddress(record, "PG01");
                addr.setCmrDept(record.getCmrCity2());
                addr.setCmrName4(record.getCmrName4());
                converted.add(addr);
              }
            }
          }

          // BELUX logic
          List<FindCMRRecordModel> rdcRecords = source.getItems();
          List<CmrtAddr> cmrtAddres = this.legacyObjects.getAddresses();
          String cmrAddrSeq = null;

          // todo - add seq mismatch message

          // import legacy addr which not in rdc
          for (CmrtAddr cmrAddr : cmrtAddres) {
            cmrAddrSeq = cmrAddr.getId().getAddrNo();
            if (!isShareSeq(cmrAddr)) {
              if (!seqIsIncluded(cmrAddr, source.getItems()) && isLegalDb2Addr(cmrAddr)) {
                FindCMRRecordModel newRecord = new FindCMRRecordModel();
                PropertyUtils.copyProperties(newRecord, mainRecord);
                newRecord.setCmrName2(null);
                newRecord.setCmrName3(null);
                newRecord.setCmrName4(null);
                newRecord.setCmrSapNumber(null);
                mapCmrtAddr2FindCMRRec(newRecord, cmrAddr);
                newRecord.setCmrAddrTypeCode(getSingleAddrType(cmrAddr));
                if ("ZS02".equals(newRecord.getCmrAddrTypeCode())) {
                  newRecord.setCmrSitePartyID(null);
                  newRecord.setCmrSapNumber(null);
                }
                converted.add(newRecord);
              }
            }
          }

          // handle share_seq addr
          int maxSeq = 1;
          int maxIntSeq = getMaxSequenceOnAddr(entityManager, SystemConfiguration.getValue("MANDT"), reqEntry.getCmrIssuingCntry(),
              mainRecord.getCmrNum());
          int maxintSeqLegacy = getMaxSequenceOnLegacyAddr(entityManager, reqEntry.getCmrIssuingCntry(), mainRecord.getCmrNum());

          if (maxIntSeq > 0 && maxintSeqLegacy > 0) {
            if (maxIntSeq > maxintSeqLegacy) {
              maxSeq = maxIntSeq;
            } else {
              maxSeq = maxintSeqLegacy;
            }
          }

          String benchmarkAddrType = null;
          List<String> shareSeqAddrList = new ArrayList<String>();
          for (CmrtAddr cmrtaddr : cmrtAddres) {
            String seq = cmrtaddr.getId().getAddrNo();
            String addrUseMail = cmrtaddr.getIsAddrUseMailing();
            String addrUseBill = cmrtaddr.getIsAddrUseBilling();
            String addrUseInst = cmrtaddr.getIsAddrUseInstalling();
            String addrUseShip = cmrtaddr.getIsAddrUseShipping();
            String addrUseEpl = cmrtaddr.getIsAddrUseEPL();

            benchmarkAddrType = getBenchmarkAddr(seq, rdcRecords);
            shareSeqAddrList = getShareSeqAddrList(cmrtaddr);

            if (isShareSeq(cmrtaddr) && StringUtils.isNotEmpty(benchmarkAddrType) && !shareSeqAddrList.isEmpty()) {
              for (String shareSeqAddrType : shareSeqAddrList) {
                if (!benchmarkAddrType.equals(shareSeqAddrType)) {
                  splitSharedAddr(benchmarkAddrType, seq, null, shareSeqAddrType, maxSeq, converted);
                  maxSeq++;
                }
              }
            }

          }

        }
      } else {

        Map<String, FindCMRRecordModel> zi01Map = new HashMap<String, FindCMRRecordModel>();

        // parse the rdc records
        String cmrCountry = mainRecord != null ? mainRecord.getCmrIssuedBy() : "";

        if (source.getItems() != null) {
          for (FindCMRRecordModel record : source.getItems()) {

            if ((!CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())) && (!"618".equals(reqEntry.getCmrIssuingCntry()))) {
              LOG.trace("Non Sold-to will be ignored. Will get from SOF");
              this.rdcShippingRecords.add(record);
              continue;
            }

            if ("618".equals(reqEntry.getCmrIssuingCntry()) && CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())
                && StringUtils.isAlpha(record.getCmrAddrSeq())) {
              record.setCmrAddrSeq("1");
            }

            if (!StringUtils.isBlank(record.getCmrPOBox())) {
              if (!record.getCmrPOBox().startsWith("PO")) {
                record.setCmrPOBox(record.getCmrPOBox());
              }
            }

            if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
              record.setCmrAddrSeq("00001");
            } else if (!"618".equals(reqEntry.getCmrIssuingCntry())) {
              record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
            }
            // if
            // (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())
            // && "AT".equals(record.getCmrCountryLanded())) {
            // System.out.println("CmrCountryLanded = " +
            // record.getCmrCountryLanded());
            // record.setCmrAddrSeq("1");
            // }

            int parvmCount = getKnvpParvmCount(record.getCmrSapNumber());
            System.out.println("parvmCount = " + parvmCount);

            if ((CmrConstants.ADDR_TYPE.ZD01.toString().equals(record.getCmrAddrTypeCode())) && (parvmCount > 1)
                && "618".equals(reqEntry.getCmrIssuingCntry())) {
              record.setCmrAddrTypeCode("ZS02");
            }

            if ("618".equals(reqEntry.getCmrIssuingCntry()) && (CmrConstants.ADDR_TYPE.ZD01.toString().equals(record.getCmrAddrTypeCode()))
                && "598".equals(record.getCmrAddrSeq())) {
              record.setCmrAddrTypeCode("ZD02");
            }

            if ("618".equals(reqEntry.getCmrIssuingCntry()) && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(record.getCmrAddrTypeCode()))
                && "599".equals(record.getCmrAddrSeq())) {
              record.setCmrAddrTypeCode("ZP02");
            }

            // if
            // ((CmrConstants.ADDR_TYPE.ZD01.toString().equals(record.getCmrAddrTypeCode())
            // && (!"Z000000001".equals(record.getCmrTransportZone()))
            // && "AT"
            // .equals(record.getCmrCountryLanded()))) {
            // record.setCmrAddrTypeCode("ZS02");
            // }

            converted.add(record);

          }
        }

        // add the missing records
        if (mainRecord != null) {

          FindCMRRecordModel record = null;

          if ("618".equals(reqEntry.getCmrIssuingCntry())) {
            return;
          }

          // import all shipping from SOF
          List<String> sequences = this.shippingSequences;
          if (sequences != null && !sequences.isEmpty()) {
            LOG.debug("Shipping Sequences is not empty. Importing " + sequences.size() + " shipping addresses.");
            for (String seq : sequences) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping_" + seq + "_", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }
          } else {
            LOG.debug("Shipping Sequences is empty. ");
            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping", zi01Map);
            if (record != null) {
              converted.add(record);
            }
          }

          importOtherSOFAddresses(entityManager, cmrCountry, zi01Map, converted);
        }
      }
    }
  }

  private boolean isShareSeq(CmrtAddr cmrtAddr) {
    boolean result = false;
    String addrMail = cmrtAddr.getIsAddrUseMailing();
    String addrBill = cmrtAddr.getIsAddrUseBilling();
    String addrInst = cmrtAddr.getIsAddrUseInstalling();
    String addrShip = cmrtAddr.getIsAddrUseShipping();
    String addrEpl = cmrtAddr.getIsAddrUseEPL();
    int shareCount = 0;
    if ("Y".equals(addrMail)) {
      shareCount++;
    }
    if ("Y".equals(addrBill)) {
      shareCount++;
    }
    if ("Y".equals(addrInst)) {
      shareCount++;
    }
    if ("Y".equals(addrShip)) {
      shareCount++;
    }
    if ("Y".equals(addrEpl)) {
      shareCount++;
    }
    if (shareCount > 1) {
      result = true;
    }
    return result;
  }

  private boolean seqIsIncluded(CmrtAddr cmrtAddr, List<FindCMRRecordModel> findCMRRecordList) {
    boolean result = false;
    if (findCMRRecordList == null || isShareSeq(cmrtAddr)) {
      return false;
    }
    String cmrtAddrSeq = cmrtAddr.getId().getAddrNo();
    String cmrtAddrTYpe = getSingleAddrType(cmrtAddr);
    List<String> rdcAddrSeqList = new ArrayList<String>();
    for (FindCMRRecordModel record : findCMRRecordList) {
      if (cmrtAddrTYpe.equals(record.getCmrAddrTypeCode()) || cmrtAddrSeq.equals(record.getCmrAddrSeq())) {
        rdcAddrSeqList.add(StringUtils.leftPad(String.valueOf(record.getCmrAddrSeq()), 5, '0'));
      }
    }
    if (rdcAddrSeqList.contains(cmrtAddrSeq)) {
      result = true;
    } else {
      result = false;
    }
    return result;
  }

  private boolean isLegalDb2Addr(CmrtAddr cmrtAddr) {
    boolean output = true;
    String addrMail = cmrtAddr.getIsAddrUseMailing();
    String addrBill = cmrtAddr.getIsAddrUseBilling();
    String addrInst = cmrtAddr.getIsAddrUseInstalling();
    String addrShip = cmrtAddr.getIsAddrUseShipping();
    String addrEpl = cmrtAddr.getIsAddrUseEPL();
    if (!"Y".equals(addrMail) && !"Y".equals(addrBill) && !"Y".equals(addrInst) && !"Y".equals(addrShip) && !"Y".equals(addrEpl)) {
      output = false;
    }
    return output;
  }

  private String getSingleAddrType(CmrtAddr cmrtAddr) {
    String result = "";
    String addrMail = cmrtAddr.getIsAddrUseMailing();
    String addrBill = cmrtAddr.getIsAddrUseBilling();
    String addrInst = cmrtAddr.getIsAddrUseInstalling();
    String addrShip = cmrtAddr.getIsAddrUseShipping();
    String addrEpl = cmrtAddr.getIsAddrUseEPL();
    if ("Y".equals(addrMail)) {
      result = "ZS02";
    }
    if ("Y".equals(addrBill)) {
      result = "ZP01";
    }
    if ("Y".equals(addrInst)) {
      result = "ZS01";
    }
    if ("Y".equals(addrShip)) {
      result = "ZD01";
    }
    if ("Y".equals(addrEpl)) {
      result = "ZI01";
    }
    return result;
  }

  private String getBenchmarkAddr(String seq, List<FindCMRRecordModel> rdcRecords) {
    String output = null;
    String addrSeq = null;
    String addrType = null;
    if (seq == null || rdcRecords == null) {
      return null;
    }
    for (FindCMRRecordModel addr : rdcRecords) {
      addrSeq = addr.getCmrAddrSeq();
      addrType = addr.getCmrAddrTypeCode();
      if (seq.equals((StringUtils.leftPad(String.valueOf(addrSeq), 5, '0')))) {
        output = addrType;
      }
    }
    return output;
  }

  private List<String> getShareSeqAddrList(CmrtAddr cmrtaddr) {
    List<String> output = new ArrayList<String>();
    String addrMail = cmrtaddr.getIsAddrUseMailing();
    String addrBill = cmrtaddr.getIsAddrUseBilling();
    String addrInst = cmrtaddr.getIsAddrUseInstalling();
    String addrShip = cmrtaddr.getIsAddrUseShipping();
    String addrEpl = cmrtaddr.getIsAddrUseEPL();

    if ("Y".equals(addrInst)) {
      output.add("ZS01");
    }
    if ("Y".equals(addrBill)) {
      output.add("ZP01");
    }
    if ("Y".equals(addrMail)) {
      output.add("ZS02");
    }
    if ("Y".equals(addrEpl)) {
      output.add("ZI01");
    }
    if ("Y".equals(addrShip)) {
      output.add("ZD01");
    }
    if (!output.isEmpty() && output.size() > 1) {
      return output;
    } else {
      return null;
    }
  }

  private void splitSharedAddr(String sourceAddrType, String SourceAddrSeq, FindCMRRecordModel sourceAddr, String targetAddrType, int targetAddrSeq,
      List<FindCMRRecordModel> converted) {
    FindCMRRecordModel newAddr = new FindCMRRecordModel();

    if (sourceAddr == null) {
      sourceAddr = getSourceAddr(sourceAddrType, SourceAddrSeq, converted);
    }

    try {
      PropertyUtils.copyProperties(newAddr, sourceAddr);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      LOG.error("Importing Mail-to address failed.");
    }

    newAddr.setCmrAddrTypeCode(targetAddrType);
    newAddr.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(targetAddrSeq), 5, '0'));
    newAddr.setParentCMRNo("");
    newAddr.setCmrSapNumber(sourceAddr.getCmrSapNumber());
    newAddr.setCmrDept(sourceAddr.getCmrCity2());
    converted.add(newAddr);
  }

  private FindCMRRecordModel getSourceAddr(String addrType, String addrSeq, List<FindCMRRecordModel> converted) {
    FindCMRRecordModel output = new FindCMRRecordModel();
    for (FindCMRRecordModel addr : converted) {
      if (addrType.equals(addr.getCmrAddrTypeCode()) && addrSeq.equals(StringUtils.leftPad(addr.getCmrAddrSeq(), 5, '0'))) {
        output = addr;
      }
    }
    return output;
  }

  private void mapCmrtAddr2FindCMRRec(FindCMRRecordModel zs02Addr, CmrtAddr cmrtAddr) {
    if (cmrtAddr == null) {
      return;
    }
    String countryNm = null;
    String cmrNo = cmrtAddr.getId().getCustomerNo() != null ? (String) cmrtAddr.getId().getCustomerNo() : "";
    String addrSeq = cmrtAddr.getId().getAddrNo() != null ? (String) cmrtAddr.getId().getAddrNo() : "";
    String addrl1 = cmrtAddr.getAddrLine1() != null ? (String) cmrtAddr.getAddrLine1() : "";
    String addrl2 = cmrtAddr.getAddrLine2() != null ? (String) cmrtAddr.getAddrLine2() : "";
    String addrl3 = cmrtAddr.getAddrLine3() != null ? (String) cmrtAddr.getAddrLine3() : "";
    String addrl4 = cmrtAddr.getAddrLine4() != null ? (String) cmrtAddr.getAddrLine4() : "";
    String addrl5 = cmrtAddr.getAddrLine5() != null ? (String) cmrtAddr.getAddrLine5() : "";
    String addrl6 = cmrtAddr.getAddrLine6() != null ? (String) cmrtAddr.getAddrLine6() : "";
    String addrli = cmrtAddr.getAddrLineI() != null ? (String) cmrtAddr.getAddrLineI() : "";
    String addrInst = cmrtAddr.getIsAddrUseInstalling() != null ? (String) cmrtAddr.getIsAddrUseInstalling() : "";
    String addrBill = cmrtAddr.getIsAddrUseBilling() != null ? (String) cmrtAddr.getIsAddrUseBilling() : "";
    String addrShip = cmrtAddr.getIsAddrUseShipping() != null ? (String) cmrtAddr.getIsAddrUseShipping() : "";
    String addrMail = cmrtAddr.getIsAddrUseMailing() != null ? (String) cmrtAddr.getIsAddrUseMailing() : "";
    String addrEpl = cmrtAddr.getIsAddrUseEPL() != null ? (String) cmrtAddr.getIsAddrUseEPL() : "";

    String postCd = null;
    String city1 = null;
    String dept = null;
    String custNm2 = null;
    String custNm3 = null;
    String custNm4 = null;
    String pobox = null;
    String addrTxt = null;
    String addrTxt2 = null;
    String poBox = null;
    boolean isLocal = true;
    boolean luxIndc = false;

    zs02Addr.setCmrAddrSeq(addrSeq);
    zs02Addr.setCmrNum(cmrNo);

    zs02Addr.setCmrName1Plain(addrl1);

    if ("".equals(addrl3) && !"".equals(addrl4) && !"".equals(addrl5) && !"".equals(addrl6)) {

      if (hasPostCd(addrl5)) {
        luxIndc = isLux(addrl5);
        postCd = getPostCd(addrl5);
        city1 = getCity1(addrl5);
        zs02Addr.setCmrName2(addrl2);
        handleAddrL3(zs02Addr, addrl3);
        zs02Addr.setCmrStreetAddress(addrl4);
        zs02Addr.setCmrPostalCode(postCd);
        zs02Addr.setCmrCity(city1);

        if (!"".equals(addrl6)) {
          isLocal = false;
          countryNm = addrl6;
        }
      }
    } else if (!"".equals(addrl3) && "".equals(addrl4) && "".equals(addrl5) && "".equals(addrl6)) {

      if (hasPostCd(addrl3)) {
        isLocal = true;
        luxIndc = isLux(addrl3);
        postCd = getPostCd(addrl3);
        city1 = getCity1(addrl3);
        zs02Addr.setCmrPostalCode(postCd);
        zs02Addr.setCmrCity(city1);
        zs02Addr.setCmrStreetAddress(addrl2);
      }
    } else if (!"".equals(addrl3) && !"".equals(addrl4) && "".equals(addrl5) && "".equals(addrl6)) {

      if (hasPostCd(addrl4)) {
        isLocal = true;
        luxIndc = isLux(addrl4);
        postCd = getPostCd(addrl4);
        city1 = getCity1(addrl4);
        zs02Addr.setCmrPostalCode(postCd);
        zs02Addr.setCmrCity(city1);

        if (hasStreetNo(addrl3)) {
          if (addrl2 != null && (addrl2.startsWith("ATT") || addrl2.startsWith("PO"))) {
            handleAddrL3(zs02Addr, addrl2);
          } else {
            zs02Addr.setCmrName3(addrl2);
          }
          zs02Addr.setCmrStreetAddress(addrl3);
        } else {
          zs02Addr.setCmrName2Plain(addrl2);
          zs02Addr.setCmrStreetAddress(addrl3);
        }

      } else if (hasStreetNo(addrl4)) {
        zs02Addr.setCmrName2Plain(addrl2);
        zs02Addr.setCmrStreetAddress(addrl4);
        handleAddrL3(zs02Addr, addrl3);
      }
    } else if (!"".equals(addrl3) && !"".equals(addrl4) && !"".equals(addrl5) && !"".equals(addrl6)) {

      if (!hasPoBox(addrl2) && !hasPoBox(addrl3) && !hasPoBox(addrl4) && !hasPoBox(addrl5) && !hasPoBox(addrl6)) {
        if (hasPostCd(addrl6)) {
          isLocal = true;
          luxIndc = isLux(addrl6);
          postCd = getPostCd(addrl6);
          city1 = getCity1(addrl6);
          zs02Addr.setCmrPostalCode(postCd);
          zs02Addr.setCmrCity(city1);
          zs02Addr.setCmrName2(addrl2);
          zs02Addr.setCmrName2Plain(addrl2);
          handleAddrL3(zs02Addr, addrl3);
          zs02Addr.setCmrStreetAddress(addrl4);
          zs02Addr.setCmrStreetAddressCont(addrl5);
        }
      }
    } else if (!"".equals(addrl3) && !"".equals(addrl4) && !"".equals(addrl5) && "".equals(addrl6)) {

      if (!hasPoBox(addrl2) && !hasPoBox(addrl3) && !hasPoBox(addrl4) && !hasPoBox(addrl5)) {
        if (hasPostCd(addrl5)) {
          isLocal = true;
          luxIndc = isLux(addrl5);
          postCd = getPostCd(addrl5);
          city1 = getCity1(addrl5);
          zs02Addr.setCmrPostalCode(postCd);
          zs02Addr.setCmrCity(city1);
          zs02Addr.setCmrName2(addrl2);
          zs02Addr.setCmrName2Plain(addrl2);
          handleAddrL3(zs02Addr, addrl3);
          zs02Addr.setCmrStreetAddress(addrl4);
        }
      }
    }
    if (isLocal) {
      if (postCd != null && luxIndc) {
        zs02Addr.setCmrCountryLanded("LU");
      } else {
        zs02Addr.setCmrCountryLanded("BE");
      }
    } else {
      if (!StringUtils.isBlank(countryNm)) {
        String countryCd = getCountryCode(countryNm);
        zs02Addr.setCmrCountryLanded(countryCd);
      }
    }

  }

  protected String getCountryCode(String desc) {
    String countryCd = "";
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String sql = ExternalizedQuery.getSql("GEN.GET_COUNTRY_CD");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("DESC", desc.toUpperCase());
      query.setParameter("DESC2", "%" + desc.toUpperCase() + "%");
      if (desc.length() > 6) {
        query.setParameter("DESC3", "%" + desc.toUpperCase().substring(0, 7) + "%");
      } else {
        query.setParameter("DESC3", "%" + desc.toUpperCase() + "%");
      }
      if (desc.length() > 5) {
        query.setParameter("DESC4", "%" + desc.toUpperCase().substring(0, 6) + "%");
      } else {
        query.setParameter("DESC4", "%" + desc.toUpperCase() + "%");
      }
      query.setParameter("DESC5", desc != null ? desc.toUpperCase().trim() : "");
      List<String> codes = query.getResults(String.class);
      if (codes != null && !codes.isEmpty()) {
        countryCd = codes.get(0);
      }
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return countryCd;
  }

  private boolean hasPostCd(String input) {
    if (input == null) {
      return false;
    }
    String regEx1 = "^[0-9]{4}[ ]";
    String regEx2 = "^[L][- ][0-9]{4}[ ]";
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      return true;
    } else if (matcher2.find()) {
      return true;
    }
    return false;
  }

  private boolean isLux(String input) {
    if (input == null) {
      return false;
    }
    String regEx1 = "^[0-9]{4}[ ]";
    String regEx2 = "^[L][- ][0-9]{4}[ ]";
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      return false;
    } else if (matcher2.find()) {
      return true;
    }
    return false;
  }

  private String getPostCd(String input) {
    if (input == null) {
      return null;
    }
    String regEx1 = "^[0-9]{4}[ ]";
    String regEx2 = "^[L][- ][0-9]{4}[ ]";
    int regExStart = 0;
    int regExEnd = 0;
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      regExStart = matcher1.start();
      regExEnd = matcher1.end();
      String postCd = input.substring(regExStart, regExStart + 4);
      return postCd;
    } else if (matcher2.find()) {
      regExStart = matcher2.start();
      regExEnd = matcher2.end();
      String postCd = input.substring(regExStart + 2, regExStart + 6);
      return postCd;
    }
    return null;
  }

  private String getCity1(String input) {
    if (input == null) {
      return null;
    }
    String regEx1 = "^[0-9]{4}[ ]";
    String regEx2 = "^[L][- ][0-9]{4}[ ]";
    int regExStart = 0;
    int regExEnd = 0;
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      regExStart = matcher1.start();
      regExEnd = matcher1.end();
      String city1 = input.substring(regExEnd);
      return city1;
    } else if (matcher2.find()) {
      regExStart = matcher2.start();
      regExEnd = matcher2.end();
      String city1 = input.substring(regExEnd);
      return city1;
    }
    return null;
  }

  private boolean hasPoBox(String input) {
    if (input == null) {
      return false;
    }
    String regEx1 = "^[0-9]{4}[ ]";
    String regEx2 = "^L[- ][0-9]{4}[ ]";
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      return false;
    } else if (matcher2.find()) {
      return false;
    }
    if (StringUtils.isNumeric(input)) {
      return true;
    }
    return false;
  }

  private boolean hasStreetNo(String input) {
    if (input == null) {
      return false;
    }
    String regEx1 = "[A-Za-z][ ][0-9]+$";
    String regEx2 = "^[0-9]+[ ][A-Za-z]*";
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      return true;
    } else if (matcher2.find()) {
      return true;
    }
    return false;
  }

  private String getStreet(String input) {
    if (input == null) {
      return null;
    }
    String regEx1 = "[ ][0-9]+$";
    String regEx2 = "^[0-9]+[ ]";
    int regExStart = 0;
    int regExEnd = 0;
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      regExStart = matcher1.start();
      regExEnd = matcher1.end();
      String street = input.substring(0, regExStart);
      return street;
    } else if (matcher2.find()) {
      regExStart = matcher2.start();
      regExEnd = matcher2.end();
      String street = input.substring(regExEnd);
      return street;
    }
    return null;
  }

  private String getStreetNo(String input) {
    if (input == null) {
      return null;
    }
    String regEx1 = "[ ][0-9]+$";
    String regEx2 = "^[0-9]+[ ]";
    int regExStart = 0;
    int regExEnd = 0;
    Pattern pattern1 = Pattern.compile(regEx1);
    Pattern pattern2 = Pattern.compile(regEx2);
    Matcher matcher1 = pattern1.matcher(input);
    Matcher matcher2 = pattern2.matcher(input);
    if (matcher1.find()) {
      regExStart = matcher1.start();
      regExEnd = matcher1.end();
      String streetNo = input.substring(regExStart + 1);
      return streetNo;
    } else if (matcher2.find()) {
      regExStart = matcher2.start();
      regExEnd = matcher2.end();
      String streetNo = input.substring(0, regExEnd - 1);
      return streetNo;
    }
    return null;
  }

  private String getTitle(String input) {
    if (input == null) {
      return null;
    }
    String output = null;
    int spaceInd1 = input.indexOf(" "); // get first space
    String title = input.substring(0, spaceInd1);
    String temp = input.substring(spaceInd1);
    int spaceInd2 = temp.indexOf(" "); // get second space
    String firstNm = temp.substring(0, spaceInd2);
    String lastNm = temp.substring(spaceInd2);

    if (lastNm.indexOf(" ") < 0) {
      output = title;
    }
    return output;
  }

  private String getFirstNm(String input) {
    if (input == null) {
      return null;
    }
    String output = null;
    int spaceInd1 = input.indexOf(" "); // get first space
    String title = input.substring(0, spaceInd1);
    String temp = input.substring(spaceInd1);
    int spaceInd2 = temp.indexOf(" "); // get second space
    String firstNm = temp.substring(0, spaceInd2);
    String lastNm = temp.substring(spaceInd2);

    if (lastNm.indexOf(" ") < 0) {
      output = firstNm;
    }
    return output;
  }

  private String getLastNm(String input) {
    if (input == null) {
      return null;
    }
    String output = null;
    int spaceInd1 = input.indexOf(" "); // get first space
    String title = input.substring(0, spaceInd1);
    String temp = input.substring(spaceInd1);
    int spaceInd2 = temp.indexOf(" "); // get second space
    String firstNm = temp.substring(0, spaceInd2);
    String lastNm = temp.substring(spaceInd2);

    if (lastNm.indexOf(" ") < 0) {
      output = lastNm;
    }
    return output;
  }

  private void handleAddrL3(FindCMRRecordModel zs02Addr, String addrl3) {
    if (addrl3 == null || zs02Addr == null) {
      return;
    }
    if (addrl3.startsWith("PO BOX")) {
      if (addrl3.indexOf("PO BOX ") > 0 || addrl3.indexOf("PO BOX-") > 0) {
        zs02Addr.setCmrPOBox(addrl3.substring(7));
      } else {
        zs02Addr.setCmrPOBox(addrl3.substring(6));
      }
    } else if (hasPoBox(addrl3)) {
      zs02Addr.setCmrPOBox(addrl3);
    } else if (addrl3.startsWith("ATT")) {
      if (addrl3.indexOf("ATT ") > 0 || addrl3.indexOf("ATT:") > 0) {
        zs02Addr.setCmrName4(addrl3.substring(4));
      } else {
        zs02Addr.setCmrName4(addrl3.substring(3));
      }
    } else {
      zs02Addr.setCmrName3(addrl3);
    }
  }

  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {
    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Mailing", zi01Map);
    if (record != null) {
      record.setCmrBldg(null);
      converted.add(record);
    }

    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
    if (record != null) {
      record.setCmrBldg(null);
      converted.add(record);
    }
    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
    if (record != null) {
      record.setCmrBldg(null);
      converted.add(record);
    }
    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP02.toString(), "CtryUseG", zi01Map);
    if (record != null) {
      String localName = this.currentImportValues.get("CtryUseGAddress6");
      record.setCmrBldg(localName);
      record.setCmrCountryLanded(null);
      converted.add(record);
    }
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {

    // Domestic and Cross-border - CEE, ME
    // line2 = Name2 or (Name2 + PoBox)
    // line3 = Name3 or (Name3 + PoBox)
    // line4 = Street
    // line5 = City + Postal Code
    // line6 = Country

    String line1 = getCurrentValue(addressKey, "Address1");
    String line2 = getCurrentValue(addressKey, "Address2");
    String line3 = getCurrentValue(addressKey, "Address3");
    String line4 = getCurrentValue(addressKey, "Address4");
    String line5 = getCurrentValue(addressKey, "Address5");
    String line6 = getCurrentValue(addressKey, "Address6");

    address.setCmrName1Plain(line1);

    // --Start: extract poBox from line2/line3
    String line2Extra = "";
    String line3Extra = "";
    String[] parts = null;
    if (isPOBox(line2)) {
      if (line2.contains(",")) {
        parts = line2.split(",");
        for (String part : parts) {
          if (isPOBox(part)) {
            address.setCmrPOBox(part);
          } else {
            line2Extra = part;
          }
        }
      } else {
        address.setCmrPOBox(line2);
      }
    } else if (isPOBox(line3)) {
      if (line3.contains(",")) {
        parts = line3.split(",");
        for (String part : parts) {
          if (isPOBox(part)) {
            address.setCmrPOBox(part);
          } else {
            line3Extra = part;
          }
        }
      } else {
        address.setCmrPOBox(line3);
      }
    } else { // no poBox
      line2Extra = line2;
      line3Extra = line3;
    }
    // --End

    if (isPhone(line2Extra)) {
      address.setCmrCustPhone(line2Extra);
    } else {
      address.setCmrName2Plain(line2Extra);
    }

    if (isAttn(line3Extra)) {
      address.setCmrName4(line3Extra);
    } else {
      address.setCmrName3(line3Extra);
    }

    // line4 should be street address
    if (isPOBox(line4)) {
      address.setCmrPOBox(line4);
    } else {
      address.setCmrStreetAddress(line4);
    }

    handleCityAndPostCode(line5, cmrIssuingCntry, address, addressKey);

    String countryCd = null;
    if (!StringUtils.isEmpty(line6)) {
      countryCd = getCountryCode(entityManager, line6);
      if (!StringUtils.isEmpty(countryCd)) {
        address.setCmrCountryLanded(countryCd);
      }
    }

    if (StringUtils.isEmpty(address.getCmrCity()) && !StringUtils.isEmpty(line6) && countryCd == null) {
      address.setCmrCity(line6);
    }
    if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
      if (!StringUtils.isEmpty(address.getCmrName3())) {
        address.setCmrStreetAddress(address.getCmrName3());
        address.setCmrName3(null);
      } else if (!StringUtils.isEmpty(address.getCmrName2Plain())) {
        address.setCmrStreetAddress(address.getCmrName2Plain());
        address.setCmrName2Plain(null);
      }
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    // Remove ICE to customer tab
    // Story 1733554: Morocco: new mandatory ICE field
    // if (SystemLocation.MOROCCO.equals(cmrIssuingCntry) &&
    // "ZP01".equalsIgnoreCase(address.getCmrAddrTypeCode())) {
    // address.setCmrDept(this.currentImportValues.get("ICE"));
    // }

    formatAddressFields(address);
    trimAddressToFit(address);

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Ice: " + address.getCmrDept());
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());

    if (SystemLocation.AUSTRIA.equals(cmrIssuingCntry) && "ZS01".equalsIgnoreCase(address.getCmrAddrTypeCode())) {
      address.setCmrAddrSeq("1");
    }

  }

  @Override
  protected boolean isPhone(String data) {
    if (data == null) {
      return false;
    }
    return data.matches("[0-9\\-\\+ ]*");
  }

  private void formatAddressFields(FindCMRRecordModel address) {
    if (address.getCmrPOBox() != null) {
      address.setCmrPOBox(address.getCmrPOBox().replaceAll("P.*BOX", ""));
    }
  }

  @Override
  protected String getCurrentValue(String addressKey, String valueKey) {
    String val = this.currentImportValues.get(addressKey + valueKey);
    if (StringUtils.isEmpty(val)) {
      return val;
    }
    return "-/X".equalsIgnoreCase(val) ? "" : ("*".equalsIgnoreCase(val) ? "" : val);
  }

  protected void handlePhoneAndAttn(String line, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    if (line == null || StringUtils.isEmpty(line)) {
      return;
    }
    String[] parts = line.split("[, ]");

    StringBuilder sbPhone = new StringBuilder();
    StringBuilder sbAttn = new StringBuilder();
    boolean attnStart = false;
    for (String part : parts) {
      if (!attnStart && (StringUtils.isNumeric(part) || (part.length() > 1 && part.startsWith("+") && StringUtils.isNumeric(part.substring(1))))) {
        sbPhone.append(part);
      } else {
        attnStart = true;
        sbAttn.append(sbAttn.length() > 0 ? " " : "");
        sbAttn.append(part);
      }
    }

    address.setCmrCustPhone(sbPhone.toString());
    address.setCmrName4(sbAttn.toString());

  }

  protected void handleCityAndPostCode(String line, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    if (line == null || StringUtils.isEmpty(line)) {
      return;
    }

    String postalCode = null;
    String city = null;
    if (line.contains(",")) {
      String ending = line.substring(line.indexOf(",") + 1);
      if (ending.matches(".*\\d{1}.*")) {
        postalCode = line.substring(line.indexOf(",") + 1).trim();
        city = line.substring(0, line.indexOf(",")).trim();
      }
    } else {
      String[] parts = line.split("[ ]");

      if (parts.length > 0 && StringUtils.isNumeric(parts[0])) {
        postalCode = parts[0];
        city = line.substring(parts[0].length()).trim();
      } else {
        city = line;
      }

    }

    address.setCmrPostalCode(postalCode);
    address.setCmrCity(city);
  }

  protected void handleCityAndPostCodeCMEA(String line, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    if (line == null || StringUtils.isEmpty(line)) {
      return;
    }
    String postalCode = "";
    String city = line;
    Pattern pattern = Pattern.compile("[0-9]+");
    Matcher matcher = pattern.matcher(line);
    List<String> numericMatch = new ArrayList<String>();
    while (matcher.find()) {
      numericMatch.add(matcher.group());
    }
    if (!numericMatch.isEmpty()) {
      int postalLength = line.indexOf(numericMatch.get(numericMatch.size() - 1)) + numericMatch.get(numericMatch.size() - 1).length();
      postalCode = line.substring(0, postalLength);
      if (line.substring(line.substring(0, postalLength).length()).trim().startsWith(",")) {
        city = line.substring(line.substring(0, postalLength).length()).trim().substring(1).trim();
      } else {
        city = line.substring(line.substring(0, postalLength).length()).trim();
      }
      address.setCmrPostalCode(postalCode);
      address.setCmrCity(city);
    }
  }

  protected void extractPhone(String line, FindCMRRecordModel address) {
    if (line == null) {
      return;
    }
    String[] parts = line.split("[.,\\- ]");
    for (String part : parts) {
      if (StringUtils.isNumeric(part)) {
        address.setCmrCustPhone(part);
        return;
      }
    }
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "Installing";
    String seqNoFromSOF = null;
    for (FindCMRRecordModel record : records) {
      if (!"ZS01".equals(record.getCmrAddrTypeCode())) {
        continue;
      }
      seqNoFromSOF = this.currentImportValues.get(addrKey + "AddressNumber");
      if (!StringUtils.isEmpty(seqNoFromSOF)) {
        LOG.trace("Assigning SOF Sequence " + seqNoFromSOF + " to " + addrKey);
        record.setCmrAddrSeq(seqNoFromSOF);
      }

    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    data.setSearchTerm(this.currentImportValues.get("SR"));
    LOG.trace("SR: " + data.getSearchTerm());

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

    data.setCustPrefLang(this.currentImportValues.get("LangCode"));
    LOG.trace("LangCode: " + data.getCustPrefLang());

    data.setTaxCd1(this.currentImportValues.get("TaxCode"));
    LOG.trace("TaxCode: " + data.getTaxCd1());

    data.setIbmDeptCostCenter(this.currentImportValues.get("AccAdBo"));
    LOG.trace("AccAdBo: " + data.getIbmDeptCostCenter());

    data.setEconomicCd(this.currentImportValues.get("EconomicCd"));
    LOG.trace("Economic Code: " + data.getEconomicCd());

    data.setEnterprise(this.currentImportValues.get("EnterpriseNo"));
    LOG.trace("Enterprise: " + data.getEnterprise());

    data.setInstallBranchOff("");
    data.setInacType("");
    data.setIbmDeptCostCenter(getInternalDepartment(mainRecord.getCmrNum()));
    data.setCommercialFinanced(results.getItems().get(0).getCmrSortl());

    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    if (prospectCmrChosen) {
      data.setCmrNo("");
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setPpsceid("");
    }
  }

  private String getInternalDepartment(String cmrNo) throws Exception {
    String department = "";
    List<String> results = new ArrayList<String>();

    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String mandt = SystemConfiguration.getValue("MANDT");
      String sql = ExternalizedQuery.getSql("GET.DEPT.KNA1.BYCMR");
      sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
      sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
      sql = StringUtils.replace(sql, ":KATR6", "'" + "624" + "'");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      results = query.getResults(String.class);
      if (results != null && results.size() > 0) {
        department = results.get(0);

        if (department != null && department.length() > 6) {
          department = department.substring(department.length() - 6, department.length());
        }
      }
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return department;
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {

    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    address.setCustNm3(currentRecord.getCmrName3());
    if (currentRecord.getCmrName4() != null && currentRecord.getCmrName4().startsWith("ATT ")) {
      String custNm4 = currentRecord.getCmrName4().substring(currentRecord.getCmrName4().indexOf("ATT ") + 4);
      address.setCustNm4(custNm4);
    } else {
      address.setCustNm4(currentRecord.getCmrName4());
    }

    if (currentRecord.getCmrPOBox() != null && currentRecord.getCmrPOBox().startsWith("PO BOX ")) {
      String pobox = currentRecord.getCmrPOBox().substring(currentRecord.getCmrPOBox().indexOf("PO BOX ") + 7);
      address.setPoBox(pobox);
    } else {
      address.setPoBox(currentRecord.getCmrPOBox());
    }

    // address.setDept(currentRecord.getCmrDept());
    address.setCity1(currentRecord.getCmrCity());
    address.setCustPhone(currentRecord.getCmrCustPhone());
    address.setTransportZone(currentRecord.getCmrTransportZone());
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {

  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCmrOwner("IBM");
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      admin.setDelInd(null);
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    // request.setSORTL(data.getSalesBusOffCd());
    // request.setCompanyNumber(data.getEnterprise());
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    addr.setTransportZone("Z000000001");
    if (!"Y".equals(addr.getImportInd())) {
      String addrSeq = addr.getId().getAddrSeq();
      addrSeq = StringUtils.leftPad(addrSeq, 5, '0');
      addr.getId().setAddrSeq(addrSeq);
    }
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {

    // handle changed share_seq address
    // 1, get shared seq addr list, might be two
    // 2, set importInd "N" for new splited addr
    // 3, set changeInd "Y" for the shared seq addr that did not splited
    // tips: new splited addr has the biggest addrSeq in its addrType
    if ("U".equals(admin.getReqType())) {
      Long reqId = data.getId().getReqId();
      List<Addr> addresses = getAddresses(entityManager, reqId);
      List<Kna1> kna1Records = getKna1Records(entityManager, data.getCmrIssuingCntry(), data.getCmrNo());
      List<CmrtAddr> legacyAddrs = getCmrtaddr(entityManager, data.getCmrIssuingCntry(), data.getCmrNo());

      String addrSeq = null;
      String addrType = null;
      String cmrtAddrSeq = null;
      List<String> cmrtAddrTypeList = null;

      boolean shareSeq1ExistInd = false;
      boolean shareSeq2ExistInd = false;
      List<List<String>> shareSeqAddrList = new ArrayList<List<String>>();
      String shareSeq1 = null;
      String shareSeq2 = null;

      String addrUseMail = null;
      String addrUseBill = null;
      String addrUseInst = null;
      String addrUseShip = null;
      String addrUseEpl = null;
      boolean typeIncluded = false;

      if (addresses == null || addresses.size() == 0) {
        return;
      }

      // get share seq list
      // there are max two shared-seq addresses exist
      for (CmrtAddr cmrtaddr : legacyAddrs) {
        List<String> shareSeqAddrList1 = getShareSeqAddrList(cmrtaddr);
        if (shareSeqAddrList1 != null && !shareSeqAddrList1.isEmpty()) {
          shareSeqAddrList.add(shareSeqAddrList1);
        }
      }

      // handle addr importIndc and changeIndc
      // changeIndc = "Y" means "Changed" on GUI addr grid
      // importIndc = "N" means "New" on GUI addr grid
      for (Addr addr : addresses) {
        addrSeq = addr.getId().getAddrSeq();
        addrType = addr.getId().getAddrType();
        if (isSharedSeq(addrSeq, addrType, legacyAddrs)) {
          if (benchmarkAddrType(addrSeq, addrType, kna1Records, legacyAddrs, shareSeqAddrList)) {
            addr.setChangedIndc("Y");
          } else {
            if (isNewSplitedAddrType(addr, kna1Records, legacyAddrs)) {
              addr.setImportInd("N");
            }
          }
        } else if (isNewSplitedAddrType(addr, kna1Records, legacyAddrs)) {
          addr.setImportInd("N");
        }
        entityManager.merge(addr);
      }
      // update data_rdc table with SO_PRJ_CD field as Department Number field
      String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", data.getId().getReqId());
      List<DataRdc> records = query.getResults(DataRdc.class);
      if (records != null && records.size() > 0) {
        DataRdc rdc = records.get(0);
        if ("624".equals(data.getCmrIssuingCntry())) {
          rdc.setSoProjectCd(data.getIbmDeptCostCenter());
        }
        entityManager.merge(rdc);
      }
      entityManager.flush();

    }

  }

  private List<Addr> getAddresses(EntityManager entityManager, Long reqId) {
    List<Addr> addresses = null;
    String sql = ExternalizedQuery.getSql("BENELUX.GET_ADDR_BYID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    addresses = query.getResults(Addr.class);
    return addresses;
  }

  private List<Kna1> getKna1Records(EntityManager entityManager, String country, String cmrNo) {
    List<Kna1> addresses = null;
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("QUERY.GET.CMR.CEE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", country);
    query.setParameter("CMRNO", cmrNo);
    query.setParameter("MANDT", mandt);
    addresses = query.getResults(Kna1.class);
    return addresses;
  }

  private List<CmrtAddr> getCmrtaddr(EntityManager entityManager, String country, String cmrNo) {
    List<CmrtAddr> addresses = null;
    String sql = ExternalizedQuery.getSql("LEGACYD.GETADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    addresses = query.getResults(CmrtAddr.class);
    return addresses;
  }

  private boolean isSharedSeqType(String addrType, List<List<String>> shareSeqAddrList) {
    boolean result = false;
    String type = addrType;
    if (addrType == null || shareSeqAddrList.isEmpty()) {
      return false;
    }
    if ("ZP02".equals(addrType)) {
      type = "ZP01";
    }
    for (List<String> list : shareSeqAddrList) {
      if (list.contains(addrType)) {
        result = true;
      }
    }
    return result;
  }

  // benchmark : is share seq in db2 and exist in rdc
  private boolean benchmarkAddrType(String addrSeq, String addrType, List<Kna1> kna1Records, List<CmrtAddr> cmrtaddrRecords,
      List<List<String>> shareSeqAddrMap) {
    boolean result = false;
    String type = addrType;

    for (Kna1 record : kna1Records) {
      if (type != null && addrSeq != null && type.equals(record.getKtokd()) && addrSeq.equals(record.getZzkvSeqno())) {
        // share seq in db2 and exist in kna1, then split other addresses
        result = true;
      }
    }
    return result;
  }

  private boolean isSharedSeq(String seq, String addrType, List<CmrtAddr> legacyAddrs) {
    if (seq == null || addrType == null || legacyAddrs == null) {
      return false;
    }
    seq = StringUtils.leftPad(String.valueOf(seq), 5, '0');
    String addrSeq = null;
    String addrMail = null;
    String addrBill = null;
    String addrInst = null;
    String addrShip = null;
    String addrEpl = null;
    int shareCount = 0;
    boolean typeIncluded = false;

    for (CmrtAddr addr : legacyAddrs) {

      addrSeq = addr.getId().getAddrNo();
      if (addrSeq != null && seq.equals(addrSeq)) {
        addrMail = addr.getIsAddrUseMailing();
        addrBill = addr.getIsAddrUseBilling();
        addrInst = addr.getIsAddrUseInstalling();
        addrShip = addr.getIsAddrUseShipping();
        addrEpl = addr.getIsAddrUseEPL();

        if ("Y".equals(addrMail)) {
          shareCount++;
          if ("ZS02".equals(addrType)) {
            typeIncluded = true;
          }
        }
        if ("Y".equals(addrBill)) {
          shareCount++;
          if ("ZP01".equals(addrType)) {
            typeIncluded = true;
          }
        }
        if ("Y".equals(addrInst)) {
          shareCount++;
          if ("ZS01".equals(addrType)) {
            typeIncluded = true;
          }
        }
        if ("Y".equals(addrShip)) {
          shareCount++;
          if ("ZD01".equals(addrType)) {
            typeIncluded = true;
          }
        }
        if ("Y".equals(addrEpl)) {
          shareCount++;
          if ("ZI01".equals(addrType)) {
            typeIncluded = true;
          }
        }
      }
    }
    if (shareCount > 1 && typeIncluded) {
      return true;
    }
    return false;
  }

  private List<String> getAddrTypeList(CmrtAddr cmrtAddr) {
    List<String> output = new ArrayList<>();
    if (cmrtAddr == null) {
      return null;
    }
    String addrMail = cmrtAddr.getIsAddrUseMailing();
    String addrBill = cmrtAddr.getIsAddrUseBilling();
    String addrInst = cmrtAddr.getIsAddrUseInstalling();
    String addrShip = cmrtAddr.getIsAddrUseShipping();
    String addrEpl = cmrtAddr.getIsAddrUseEPL();
    if ("Y".equals(addrMail)) {
      output.add("ZS02");
    }
    if ("Y".equals(addrBill)) {
      output.add("ZP01");
    }
    if ("Y".equals(addrInst)) {
      output.add("ZS01");
    }
    if ("Y".equals(addrShip)) {
      output.add("ZD01");
    }
    if ("Y".equals(addrEpl)) {
      output.add("ZI01");
    }

    return output;
  }

  private boolean isNewSplitedAddrType(Addr addr, List<Kna1> kna1Records, List<CmrtAddr> legacyAddrs) {
    boolean result = true;
    if (addr == null || kna1Records == null || legacyAddrs == null) {
      return false;
    }
    if (isExistInRdc(addr, kna1Records) || isExistInDb2(addr, legacyAddrs)) {
      result = false;
    }
    return result;
  }

  private boolean isExistInRdc(Addr addr, List<Kna1> kna1Records) {
    boolean result = false;
    String addrSeq = addr.getId().getAddrSeq();
    String addrType = addr.getId().getAddrType();
    for (Kna1 kna1 : kna1Records) {
      if ((addrType.equals(kna1.getKtokd()) && addrSeq.equals(kna1.getZzkvSeqno())) || "PG01".equals(addr.getId().getAddrType())) {
        result = true;
      }
    }
    return result;
  }

  private boolean isExistInDb2(Addr addr, List<CmrtAddr> legacyAddrs) {
    boolean result = false;
    String addrSeq = StringUtils.leftPad(String.valueOf(addr.getId().getAddrSeq()), 5, '0');
    String addrType = addr.getId().getAddrType();
    String addrMail = null;
    String addrBill = null;
    String addrInst = null;
    String addrShip = null;
    String addrEpl = null;
    String cmrtAddrSeq = null;
    for (CmrtAddr cmrtAddr : legacyAddrs) {
      addrMail = cmrtAddr.getIsAddrUseMailing();
      addrBill = cmrtAddr.getIsAddrUseBilling();
      addrInst = cmrtAddr.getIsAddrUseInstalling();
      addrShip = cmrtAddr.getIsAddrUseShipping();
      addrEpl = cmrtAddr.getIsAddrUseEPL();
      cmrtAddrSeq = cmrtAddr.getId().getAddrNo();

      if (addrSeq.equals(cmrtAddrSeq)) {
        if ("ZS01".equals(addrType) && "Y".equals(addrInst)) {
          result = true;
        }
        if ("ZS02".equals(addrType) && "Y".equals(addrMail)) {
          result = true;
        }
        if ("ZP01".equals(addrType) && "Y".equals(addrBill)) {
          result = true;
        }
        if ("ZD01".equals(addrType) && "Y".equals(addrShip)) {
          result = true;
        }
        if ("ZI01".equals(addrType) && "Y".equals(addrEpl)) {
          result = true;
        }
      }
    }
    return result;
  }

  private void updateSeq4SharedAddr(Addr addr, int maxSeq) {
    if (addr == null || maxSeq < 1) {
      return;
    }
    String maxSeqStr = StringUtils.leftPad(String.valueOf(maxSeq), 5, '0');
    addr.getId().setAddrSeq(maxSeqStr);
  }

  private void updateImportInd4SharedAddr(String indc, Addr addr) {
    if (addr == null) {
      return;
    }
    addr.setImportInd(indc);
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "ADDR_TXT", "CITY1", "STATE_PROV", "POST_CD", "LAND_CNTRY", "DEPT",
        "PO_BOX", "CUST_PHONE", "TRANSPORT_ZONE", "ADDR_TXT_2"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {

    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEconomicCd(), newData.getEconomicCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EconomicCd2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEconomicCd(), "EconomicCd2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEconomicCd(), "EconomicCd2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSoProjectCd(), newData.getIbmDeptCostCenter())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "IbmDeptCostCenter", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getIbmDeptCostCenter(), "IbmDeptCostCenter", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSoProjectCd(), "IbmDeptCostCenter", cmrCountry));
      results.add(update);
    }
    // SORTL
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CommercialFinanced", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(BELUX_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    if (!equals(addr.getDept(), addr.getDeptOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "", "Title"));
      update.setNewData(addr.getDept());
      update.setOldData(addr.getDeptOld());
      results.add(update);
    }
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return RETRIEVE_INVALID_CUSTOMERS;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##SearchTerm", "searchTerm");
    map.put("##CMROwner", "cmrOwner");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustLang", "custPrefLang");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
    map.put("##BPRelationType", "bpRelType");
    map.put("##CAP", "capInd");
    map.put("##RequestReason", "reqReason");
    map.put("##POBox", "poBox");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##CustPhone", "custPhone");
    map.put("##VATExempt", "vatExempt");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##City1", "city1");
    map.put("##StateProv", "stateProv");
    map.put("##InternalDept", "ibmDeptCostCenter");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##CollectionCd", "collectionCd");
    map.put("##VAT", "vat");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CountrySubRegion", "countryUse");
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SitePartyID", "sitePartyId");
    map.put("##SAPNumber", "sapNo");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Department", "dept");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##LocalTax_BE", "taxCd1");
    map.put("##LocalTax_LU", "taxCd1");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##CustomerName1", "custNm1");
    map.put("##CustomerName2", "custNm2");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName3", "custNm3");
    map.put("##CustomerName4", "custNm4");
    map.put("##Enterprise", "enterprise");
    map.put("##PostalCode", "postCd");
    map.put("##TransportZone", "transportZone");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##EconomicCd2", "economicCd");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return true;
  }

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD",
        "DUNS_NO", "ABBREV_LOCN"));// CMR-1947:add
    // Abbrev_locn
    // field
    // change
    // to
    // check
    // update
    return fields;
  }

  public static boolean isDataUpdated(Data data, DataRdc dataRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    for (Field field : Data.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if at least one of the fields is updated
        if (getDataFieldsForUpdateCheck(cmrIssuingCntry).contains(srcName)) {
          try {
            trgField = DataRdc.class.getDeclaredField(field.getName());

            field.setAccessible(true);
            trgField.setAccessible(true);

            Object srcVal = field.get(data);
            Object trgVal = trgField.get(dataRdc);

            if (String.class.equals(field.getType())) {
              String srcStringVal = (String) srcVal;
              if (srcStringVal == null) {
                srcStringVal = "";
              }
              String trgStringVal = (String) trgVal;
              if (trgStringVal == null) {
                trgStringVal = "";
              }
              if (!StringUtils.equals(srcStringVal.trim(), trgStringVal.trim())) {
                LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
                return true;
              }
            } else {
              if (!ObjectUtils.equals(srcVal, trgVal)) {
                LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
                return true;
              }
            }
          } catch (NoSuchFieldException e) {
            // noop
            continue;
          } catch (Exception e) {
            LOG.trace("General error when trying to access field.", e);
            // no stored value or field not on addr rdc, return null
            // for no
            // changes
            continue;
          }
        } else {
          continue;
        }
      }
    }

    return false;
  }

  public boolean isAddrUpdated(Addr addr, AddrRdc addrRdc, String cmrIssuingCntry) {
    String srcName = null;
    Column srcCol = null;
    Field trgField = null;

    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);

    for (Field field : Addr.class.getDeclaredFields()) {
      if (!(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isAbstract(field.getModifiers()))) {
        srcCol = field.getAnnotation(Column.class);
        if (srcCol != null) {
          srcName = srcCol.name();
        } else {
          srcName = field.getName().toUpperCase();
        }

        // check if field is part of exemption list or is part of what
        // to check
        // for the handler, if specified
        if (GEOHandler.ADDRESS_FIELDS_SKIP_CHECK.contains(srcName)
            || (handler != null && handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry) != null
                && !handler.getAddressFieldsForUpdateCheck(cmrIssuingCntry).contains(srcName))) {
          continue;
        }

        if ("ID".equals(srcName) || "PCSTATEMANAGER".equals(srcName) || "PCDETACHEDSTATE".equals(srcName)) {
          continue;
        }

        try {
          trgField = AddrRdc.class.getDeclaredField(field.getName());

          field.setAccessible(true);
          trgField.setAccessible(true);

          Object srcVal = field.get(addr);
          Object trgVal = trgField.get(addrRdc);

          if (String.class.equals(field.getType())) {
            String srcStringVal = (String) srcVal;
            if (srcStringVal == null) {
              srcStringVal = "";
            }
            String trgStringVal = (String) trgVal;
            if (trgStringVal == null) {
              trgStringVal = "";
            }
            if (!StringUtils.equals(srcStringVal.trim(), trgStringVal.trim())) {
              LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
              return true;
            }
          } else {
            if (!ObjectUtils.equals(srcVal, trgVal)) {
              LOG.trace(" - Field: " + srcName + " Not equal " + srcVal + " - " + trgVal);
              return true;
            }
          }
        } catch (NoSuchFieldException e) {
          // noop
          continue;
        } catch (Exception e) {
          LOG.trace("General error when trying to access field.", e);
          // no stored value or field not on addr rdc, return null for
          // no
          // changes
          continue;
        }

      }
    }
    return false;
  }

  @Override
  public void doBeforeDPLCheck(EntityManager entityManager, Data data, List<Addr> addresses) throws Exception {
    // No DPL check for non-latin addresses
    for (Addr addr : addresses) {
      if (Arrays.asList("ZP02").contains(addr.getId().getAddrType())) {
        addr.setDplChkResult("N");
      }
    }
  }

  private int getKnvpParvmCount(String kunnr) throws Exception {
    int knvpParvmCount = 0;

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNVP.PARVW");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("PARVW");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing SPRAS value from RDc DB..For PARVW ");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      // Map<String, Object> record = records.get(0);
      knvpParvmCount = records.size();
      LOG.debug("GET.KNVP.PARVW " + knvpParvmCount + " WHERE KUNNR IS > " + kunnr);
    }
    return knvpParvmCount;
  }

  public String getaddAddressAdrnr(EntityManager entityManager, String katr6, String mandt, String kunnr, String ktokd, String seq) {
    String adrnr = "";
    String sql = ExternalizedQuery.getSql("CEE.GETADRNR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KATR6", katr6);
    query.setParameter("MANDT", mandt);
    query.setParameter("KUNNR", kunnr);
    query.setParameter("ADDR_TYPE", ktokd);
    query.setParameter("ADDR_SEQ", seq);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      adrnr = sResult[1].toString();
    }
    System.out.println("adrnr = " + adrnr);

    return adrnr;
  }

  public static String getStkznFromDataRdc(EntityManager entityManager, String kunnr, String mandt) {
    String stkzn = "";
    String sql = ExternalizedQuery.getSql("CEE.GET_STKZN_FROM_DATA_RDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KUNNR", kunnr);
    query.setParameter("MANDT", mandt);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      stkzn = result;
    }
    LOG.debug("stkzn of Data_RDC>" + stkzn);
    return stkzn;
  }

  public Sadr getCEEAddtlAddr(EntityManager entityManager, String adrnr, String mandt) {
    Sadr sadr = new Sadr();
    String qryAddlAddr = ExternalizedQuery.getSql("GET.CEE_SADR_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryAddlAddr);
    query.setParameter("ADRNR", adrnr);
    query.setParameter("MANDT", mandt);
    sadr = query.getSingleResult(Sadr.class);

    return sadr;
  }

  private int getMaxSequenceOnAddr(EntityManager entityManager, String mandt, String katr6, String cmrNo) {
    String maxAddrSeq = null;
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("CEE.GETADDRSEQ.MAX");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      addrSeq = Integer.parseInt(maxAddrSeq);
      addrSeq = ++addrSeq;
      System.out.println("maxseq = " + addrSeq);
    }

    return addrSeq;
  }

  private int getMaxSequenceOnLegacyAddr(EntityManager entityManager, String rcyaa, String cmrNo) {
    String maxAddrSeq = null;
    int addrSeq = 0;
    String sql = ExternalizedQuery.getSql("CEE.GETADDRSEQ.MAX.FROM.LEGACY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmrNo);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 ? result[0] : "0");
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "0";
      }
      addrSeq = Integer.parseInt(maxAddrSeq);
      addrSeq = ++addrSeq;
      System.out.println("maxseq = " + addrSeq);
    }

    return addrSeq;
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD", "ABBREV_NM",
        "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL",
        "BP_REL_TYPE", "COMMERCIAL_FINANCED", "ENTERPRISE", "PHONE1", "PHONE3"));
    return fields;
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = null;
    if (!StringUtils.isEmpty(addrType)) {
      if ("ZD02".equals(addrType)) {
        return "598";
      } else if ("ZP03".equals(addrType)) {
        return "599";
      }
    }
    int addrSeq = 0;
    String maxAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_CEE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "00000");

      if (!(Integer.valueOf(maxAddrSeq) >= 00000 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "00000";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }

    newAddrSeq = "0000" + Integer.toString(addrSeq);

    newAddrSeq = newAddrSeq.substring(newAddrSeq.length() - 5, newAddrSeq.length());
    return newAddrSeq;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newSeq;
  }

  public static String getGaddressSeqFromLegacy(EntityManager entityManager, String rcyaa, String cmr_no) {
    String gSeq = "";
    String sql = ExternalizedQuery.getSql("CEE.GET_G_SEQ_FROM_LEGACY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      gSeq = result;
    }
    LOG.debug("gSeq of Legacy" + gSeq);
    return gSeq;
  }

  public static String getZS01SeqFromLegacy(EntityManager entityManager, String rcyaa, String cmr_no) {
    String gSeq = "";
    String sql = ExternalizedQuery.getSql("CEE.GET_ZS01_SEQ_FROM_LEGACY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      gSeq = result;
    }
    LOG.debug("gSeq of Legacy" + gSeq);
    return gSeq;
  }

  public static String getGaddressAddLN6FromLegacy(EntityManager entityManager, String rcyaa, String cmr_no) {
    String gLn6 = "";
    String sql = ExternalizedQuery.getSql("CEE.GET_G_ADRLN6_FROM_LEGACY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    List<Object[]> results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      if (sResult[0] != null) {
        String addln6 = sResult[0].toString();
        if (!StringUtils.isBlank(addln6)) {
          gLn6 = addln6;
        } else if (sResult[1] != null) {
          String addln4 = sResult[1].toString();
          if (!StringUtils.isBlank(addln4)) {
            gLn6 = addln4;
          }
        }
      }
    }

    LOG.debug("gLn6 of Legacy" + gLn6);
    return gLn6;
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    XSSFRow row = null;
    XSSFCell currCell = null;

    String[] countryAddrss = null;

    countryAddrss = MASS_UPDATE_SHEET_NAMES;

    XSSFSheet sheet = book.getSheet("Data");// validate Data sheet
    row = sheet.getRow(0);// data field name row
    int ordBlkIndex = 16;// default index
    int cmrNoIndex = 0;// 0
    String cmrNo = null;
    for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
      currCell = row.getCell(cellIndex);
      String cellVal = validateColValFromCell(currCell);
      if ("Order block code".equals(cellVal)) {
        ordBlkIndex = cellIndex;
        break;
      }
    }

    TemplateValidation error = new TemplateValidation("Data");
    for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
      error = new TemplateValidation("Data");
      row = sheet.getRow(rowIndex);
      if (row == null) {
        break; // stop immediately when row is blank
      }
      currCell = row.getCell(ordBlkIndex);
      String ordBlk = validateColValFromCell(currCell);
      if (StringUtils.isNotBlank(ordBlk) && !("@".equals(ordBlk) || "D".equals(ordBlk) || "P".equals(ordBlk) || "J".equals(ordBlk))) {
        LOG.trace("Order Block Code should only @, D, P, J. >> ");
        error.addError((rowIndex + 1), "Order Block Code", "Order Block Code should be only @, D, P, J. ");
      }

      currCell = row.getCell(cmrNoIndex);
      cmrNo = validateColValFromCell(currCell);
      if (isDivCMR(cmrNo)) {
        LOG.trace("The row " + (rowIndex + 1) + ":Note the CMR number is a divestiture CMR records.");
        error.addError((rowIndex + 1), "CMR No.", "The row " + (rowIndex + 1) + ":Note the CMR number is a divestiture CMR records.<br>");
      }

      if (is93CMR(cmrNo)) {
        LOG.trace("The row " + (row.getRowNum() + 1) + ":Note the CMR number is a deleted record in RDC.");
        error.addError((row.getRowNum() + 1), "CMR No.", "The row " + (row.getRowNum() + 1) + ":Note the CMR number is a deleted record in RDC.<br>");
      }

      currCell = row.getCell(7);
      String ctc = validateColValFromCell(currCell);
      String isuCd = ""; // 6
      currCell = row.getCell(6);
      isuCd = validateColValFromCell(currCell);

      if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
        if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(ctc)) || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isuCd))) {
          LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that both ISU and CTC value needs to be filled..");
          error.addError((row.getRowNum() + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
        } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"Q".equals(ctc)) {
            LOG.trace("The row " + (row.getRowNum() + 1) + ":Client Tier should be 'Q' for the selected ISU code.");
            error.addError((row.getRowNum() + 1), "Client Tier", ":Client Tier should be 'Q' for the selected ISU code:" + isuCd + ".<br>");
          }
        } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"Y".contains(ctc)) {
            LOG.trace("The row " + (row.getRowNum() + 1) + ":Client Tier should be 'Y' for the selected ISU code.");
            error.addError((row.getRowNum() + 1), "Client Tier", ":Client Tier should be 'Y' for the selected ISU code: " + isuCd + ".<br>");
          }
        } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"T".contains(ctc)) {
            LOG.trace("The row " + (row.getRowNum() + 1) + ":Client Tier should be 'T' for the selected ISU code.");
            error.addError((row.getRowNum() + 1), "Client Tier", ":Client Tier should be 'T' for the selected ISU code :" + isuCd + ".<br>");
          }
        } else if ((!StringUtils.isBlank(isuCd) && !Arrays.asList("32", "34", "36").contains(isuCd)) && !"@".equalsIgnoreCase(ctc)) {
          LOG.trace("Client Tier should be '@' for the selected ISU Code.");
          error.addError(row.getRowNum() + 1, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
        }
      }
      if (error.hasErrors()) {
        validations.add(error);
      }
    }

    for (String name : countryAddrss) {
      sheet = book.getSheet(name);
      for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {

        row = sheet.getRow(rowIndex);
        if (row == null) {
          break; // stop immediately when row is blank
        }
        String name3 = ""; // 4
        String attPerson = ""; // 5
        String pobox = ""; // 7
        int addrFldCnt1 = 0;

        currCell = row.getCell(4);
        name3 = validateColValFromCell(currCell);
        currCell = row.getCell(5);
        attPerson = validateColValFromCell(currCell);
        currCell = row.getCell(7);
        pobox = validateColValFromCell(currCell);

        if (!StringUtils.isEmpty(name3)) {
          addrFldCnt1++;
        }
        if (!StringUtils.isEmpty(attPerson)) {
          addrFldCnt1++;
        }
        if (!StringUtils.isEmpty(pobox)) {
          addrFldCnt1++;
        }

        if (addrFldCnt1 > 1) {
          TemplateValidation errorAddr = new TemplateValidation(name);
          LOG.trace("Customer Name (3) and PO BOX should not be input at the sametime.");
          errorAddr.addError((rowIndex + 1), "PO BOX", "Customer Name 3, Attention person and PO Box - only 1 out of 3 can be filled.");
          validations.add(errorAddr);
        }
      }
    }
  }

  private static boolean isDivCMR(String cmrNo) {
    boolean isDivestiture = false;
    if (StringUtils.isEmpty(cmrNo)) {
      return false;
    }
    String mandt = SystemConfiguration.getValue("MANDT");
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("FR.GET.ZS01KATR10");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("KATR6", "624");
    query.setParameter("MANDT", mandt);
    query.setParameter("CMR", cmrNo);

    Kna1 zs01 = query.getSingleResult(Kna1.class);
    if (zs01 != null) {
      if (!StringUtils.isBlank(zs01.getKatr10())) {
        isDivestiture = true;
      }
    }
    entityManager.close();
    return isDivestiture;
  }

  private static boolean is93CMR(String cmrNo) {
    boolean is93cmr = false;
    if (StringUtils.isEmpty(cmrNo)) {
      return false;
    }
    String mandt = SystemConfiguration.getValue("MANDT");
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("BENELUX.GET_RDC_ZS01");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CNTRY", "624");
    query.setParameter("MANDT", mandt);
    query.setParameter("CMRNO", cmrNo);
    query.setParameter("ADDRTYPE", "ZS01");

    Object[] result = query.getSingleResult();
    if (result != null) {
      String aufsd = result[0].toString();
      String loevm = result[1].toString();
      if ("93".equals(aufsd) || "X".equals(loevm)) {
        is93cmr = true;
      }
    }
    entityManager.close();
    return is93cmr;
  }

  private int getCeeKnvpParvmCount(String kunnr) throws Exception {
    int knvpParvmCount = 0;

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("CEE.GET.KNVP.PARVW");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("PARVW");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing SPRAS value from RDc DB..For PARVW ");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      // Map<String, Object> record = records.get(0);
      knvpParvmCount = records.size();
      LOG.debug("GET.KNVP.PARVW " + knvpParvmCount + " WHERE KUNNR IS > " + kunnr);
    }
    return knvpParvmCount;
  }

  public static String getZP02UpdateInit(EntityManager entityManager, long req_id) {
    String ZP02UpdateInit = "";
    String sql = ExternalizedQuery.getSql("CEE.GET.ZP02UPDATEINIT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", req_id);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      ZP02UpdateInit = result;
    }
    LOG.debug("ZP02UpdateInit Addr>" + ZP02UpdateInit);
    return ZP02UpdateInit;
  }

  public static String getSoldtoaddrSeqFromLegacy(EntityManager entityManager, String rcyaa, String cmr_no) {
    String zs01Seq = "";
    String sql = ExternalizedQuery.getSql("CEE.GET_SOLDTO_SEQ_FROM_LEGACY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      zs01Seq = result;
    }
    LOG.debug("zs01Seq of Legacy" + zs01Seq);
    return zs01Seq;
  }

  public static String isShareZP01(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String zp01Seq = null;
    String sql = ExternalizedQuery.getSql("CEE.ISSHAREZP01");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      zp01Seq = result;
    }
    return zp01Seq;
  }

  public static String isShareZS02(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String zs02Seq = null;
    String sql = ExternalizedQuery.getSql("CEE.ISSHAREZS02");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      zs02Seq = result;
    }
    return zs02Seq;
  }

  public static String isShareZD01(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String zd01Seq = null;
    String sql = ExternalizedQuery.getSql("CEE.ISSHAREZD01");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      zd01Seq = result;
    }
    return zd01Seq;
  }

  public static String isShareZI01(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String zi01Seq = null;
    String sql = ExternalizedQuery.getSql("CEE.ISSHAREZI01");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      zi01Seq = result;
    }
    return zi01Seq;
  }

  public void updateImportIndToNForSharezp01Addr(EntityManager entityManager, long reqId, String mandt, String katr6, String cmrNo) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.UPDATE.ZP01SHARE.N.IMPORTIND"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.executeSql();
  }

  public void updateImportIndToNForSharezs02Addr(EntityManager entityManager, long reqId, String mandt, String katr6, String cmrNo) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.UPDATE.ZS02SHARE.N.IMPORTIND"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.executeSql();
  }

  public void updateImportIndToNForSharezd01Addr(EntityManager entityManager, long reqId, String mandt, String katr6, String cmrNo) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.UPDATE.ZD01SHARE.N.IMPORTIND"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.executeSql();
  }

  public void updateImportIndToNForSharezi01Addr(EntityManager entityManager, long reqId, String mandt, String katr6, String cmrNo) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.UPDATE.ZI01SHARE.N.IMPORTIND"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.executeSql();
  }

  public void updateChangeindToYForSharezs01Addr(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.UPDATE.ZS01SHARE.Y.CHANGEINCD"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
  }

  // 4606
  public static Map<String, Object> getDupCMRFieldValue(String katr6, String cmrNo) throws Exception {
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("CEE.GET_DUPCMR_FIELD_VALUE");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + katr6 + "'");
    sql = StringUtils.replace(sql, ":CMRNO", "'" + cmrNo + "'");
    String dbId = QueryClient.RDC_APP_ID;
    Map<String, Object> dupRecord = null;
    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("MANDT");
    query.addField("KATR6");
    query.addField("CMRNO");
    query.addField("KATR3");
    query.addField("BRSCH");
    query.addField("ZZKV_NODE1");
    query.addField("ZZKV_NODE2");
    LOG.debug("Check Dup CMR .. Getting existing SPRAS value from RDc DB.." + "KATR6 =" + katr6);
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      dupRecord = records.get(0);
    }
    return dupRecord;
  }

  public static String getZP02importInit(EntityManager entityManager, long req_id) {
    String ZP02ImportInit = null;
    String sql = ExternalizedQuery.getSql("CEE.GET.ZP02IMPORTINIT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", req_id);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      ZP02ImportInit = result;
    }
    LOG.debug("ZP02ImportInit " + ZP02ImportInit);
    return ZP02ImportInit;
  }

  @Override
  public boolean setAddrSeqByImport(AddrPK addrPk, EntityManager entityManager, FindCMRResultModel result) {
    return true;
  }

}
