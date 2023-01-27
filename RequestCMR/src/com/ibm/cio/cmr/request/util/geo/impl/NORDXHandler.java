/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.MachinesToInstall;
import com.ibm.cio.cmr.request.entity.MachinesToInstallPK;
import com.ibm.cio.cmr.request.entity.Sadr;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.MachineModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.ValidatorClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.validator.PostalCodeValidateRequest;
import com.ibm.cmr.services.client.validator.ValidationResult;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for NORDX
 * 
 * @author Rangoli Saxena
 * 
 */
public class NORDXHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(NORDXHandler.class);

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  public Map<String, List<MachineModel>> MACHINES_MAP = new HashMap<String, List<MachineModel>>();

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.SWEDEN, "SE");
    LANDED_CNTRY_MAP.put(SystemLocation.NORWAY, "NO");
    LANDED_CNTRY_MAP.put(SystemLocation.FINLAND, "FI");
    LANDED_CNTRY_MAP.put(SystemLocation.DENMARK, "DK");
    LANDED_CNTRY_MAP.put("702LV", "LV");
    LANDED_CNTRY_MAP.put("702LT", "LT");
    LANDED_CNTRY_MAP.put("702EE", "EE");
    LANDED_CNTRY_MAP.put("678GL", "GL");
    LANDED_CNTRY_MAP.put("678FO", "FO");
    LANDED_CNTRY_MAP.put("678IS", "IS");
  }

  private static final String[] NORDX_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CustLang", "GeoLocationCode", "Affiliate", "CAP", "CMROwner",
      "CustClassCode", "LocalTax2", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor",
      "Building", "County", "City2", "Department", "SearchTerm", "SpecialTaxCd", "SalesBusOff", "CollectionCd" };

  private static final List<String> ND_COUNTRIES_LIST = Arrays.asList(SystemLocation.SWEDEN, SystemLocation.NORWAY, SystemLocation.FINLAND,
      SystemLocation.DENMARK);

  protected static final String[] ND_MASS_UPDATE_SHEET_NAMES = { "Mailing", "Billing", "Shipping", "Installing", "EPL", };

  public static boolean isNordicsCountry(String issuingCntry) {
    if (SystemLocation.SWEDEN.equals(issuingCntry) || SystemLocation.NORWAY.equals(issuingCntry) || SystemLocation.DENMARK.equals(issuingCntry)) {
      return true;
    } else {
      return false;
    }
  }

  public String InstalingShareSeq = "";
  public String SecondaryZS01ShareSeqflag = "";

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {
    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;

      if (!StringUtils.isEmpty(record.getCmrName4())) {
        // name4 in rdc is street con't
        record.setCmrStreetAddressCont(record.getCmrName4());
        record.setCmrName4(null);
      }

      // name3 in rdc = attn on SOF
      if (!StringUtils.isEmpty(record.getCmrName3())) {
        record.setCmrName4(record.getCmrName3());
        record.setCmrName3(null);
      }

      // if (!StringUtils.isBlank(record.getCmrPOBox())) {
      // record.setCmrPOBox("PO BOX " + record.getCmrPOBox());
      // }
      if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
        record.setCmrAddrSeq("00001");
      } else {
        record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
      }

      record.setCmrName2Plain(record.getCmrName2Plain());
      // record.setCmrTaxOffice(this.currentImportValues.get("InstallingAddressT"));
      record.setCmrDept(null);

      if (StringUtils.isEmpty(record.getCmrCustPhone())) {
        record.setCmrCustPhone(this.currentImportValues.get("MailingPhone"));
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

          int maxintSeqLegacy = 0;
          int maxintSeqFromLegacy = getMaxSequenceOnLegacyAddr(entityManager, reqEntry.getCmrIssuingCntry(), mainRecord.getCmrNum());
          int maxintSeqFromRdc = getMaxSequenceFromRdc(entityManager, SystemConfiguration.getValue("MANDT"), reqEntry.getCmrIssuingCntry(),
              mainRecord.getCmrNum());

          if (maxintSeqFromRdc > maxintSeqFromLegacy) {
            maxintSeqLegacy = maxintSeqFromRdc;
          } else {
            maxintSeqLegacy = maxintSeqFromLegacy;
          }

          // String sourceID = getSourceidFromAdmin(entityManager,
          // reqEntry.getReqId());

          String zi01Flag = null;
          String zs02Flag = null;
          // map RDc - SOF - CreateCMR by sequence no
          for (FindCMRRecordModel record : source.getItems()) {
            seqNo = record.getCmrAddrSeq();
            String legacyseqNoformat = StringUtils.leftPad(seqNo, 5, '0');
            String legacyAddressSeq = getLegacyAddressSeq(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), legacyseqNoformat);

            if (StringUtils.isBlank(legacyAddressSeq)) {
              continue;
            }

            // if (StringUtils.isBlank(legacyAddressSeq)) {
            // if ("ZP01".equals(record.getCmrAddrTypeCode()) &&
            // "PG".equals(record.getCmrOrderBlock())) {
            // record.setCmrAddrTypeCode("PG01");
            // } else {
            // continue;
            // }
            // }

            // if
            // ((CmrConstants.ADDR_TYPE.ZD01.toString().equals(record.getCmrAddrTypeCode()))
            // && (parvmCount > 1)) {
            // record.setCmrAddrTypeCode("ZS02");
            // }

            System.out.println("seqNo = " + seqNo);
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
              addrType = record.getCmrAddrTypeCode();
              if (!StringUtils.isEmpty(addrType)) {
                addr = cloneAddress(record, addrType);
                addr.setCmrDept(record.getCmrCity2());
                addr.setCmrName4(record.getCmrName4());
                if (ND_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                    && (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getCmrAddrTypeCode())) && "598".equals(addr.getCmrAddrSeq())) {
                  addr.setCmrAddrTypeCode("ZD02");
                }
                if (ND_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                    && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addr.getCmrAddrTypeCode())) && "599".equals(addr.getCmrAddrSeq())) {
                  addr.setCmrAddrTypeCode("ZP03");
                }
                if ((CmrConstants.ADDR_TYPE.ZI01.toString().equals(addr.getCmrAddrTypeCode()))) {
                  String stkzn = "";
                  stkzn = getStkznFromDataRdc(entityManager, addr.getCmrSapNumber(), SystemConfiguration.getValue("MANDT"));
                  int parvmCount = getNorKnvpParvmCount(addr.getCmrSapNumber());
                  LOG.debug("---------parvmCount---------------" + parvmCount);
                  if ("0".equals(stkzn) || parvmCount > 2) {
                    addr.setCmrAddrTypeCode("ZS02");
                    zs02Flag = "Y";
                    System.out.println("---------zs02Flag---------------" + zs02Flag);
                  } else {
                    zi01Flag = "Y";
                    System.out.println("---------zi01Flag---------------" + zi01Flag);
                  }
                }

                /*
                 * String seqSecondaryZS01 = getSecondaryZS01Seq(entityManager,
                 * cmrIssueCd, SystemConfiguration.getValue("MANDT"),
                 * record.getCmrNum());
                 * System.out.println("---------seqSecondaryZS01---------------"
                 * + seqSecondaryZS01); if
                 * (!StringUtils.isBlank(seqSecondaryZS01) &&
                 * seqSecondaryZS01.equals(addr.getCmrAddrSeq())) { String
                 * seqSecondaryZS01Legacy =
                 * StringUtils.leftPad(seqSecondaryZS01, 5, '0');
                 * LOG.debug("---------seqSecondaryZS01Legacy---------------" +
                 * seqSecondaryZS01Legacy); System.out.println(
                 * "---------seqSecondaryZS01Legacy---------------" +
                 * seqSecondaryZS01Legacy); String isSecondaryInst =
                 * isSecondaryInst(entityManager, reqEntry.getCmrIssuingCntry(),
                 * record.getCmrNum(), seqSecondaryZS01Legacy); String
                 * isSecondaryBill = isSecondaryBill(entityManager,
                 * reqEntry.getCmrIssuingCntry(), record.getCmrNum(),
                 * seqSecondaryZS01Legacy); String isSecondaryShip =
                 * isSecondaryShip(entityManager, reqEntry.getCmrIssuingCntry(),
                 * record.getCmrNum(), seqSecondaryZS01Legacy); String
                 * isSecondaryEpl = isSecondaryEpl(entityManager,
                 * reqEntry.getCmrIssuingCntry(), record.getCmrNum(),
                 * seqSecondaryZS01Legacy);
                 * 
                 * if ("Y".equals(isSecondaryInst)) {
                 * addr.setCmrAddrTypeCode("ZI01"); } else if
                 * ("N".equals(isSecondaryInst) && "Y".equals(isSecondaryBill))
                 * { addr.setCmrAddrTypeCode("ZP01"); } else if
                 * ("N".equals(isSecondaryInst) && "N".equals(isSecondaryBill)
                 * && "Y".equals(isSecondaryShip)) {
                 * addr.setCmrAddrTypeCode("ZD01"); } else if
                 * ("N".equals(isSecondaryInst) && "N".equals(isSecondaryBill)
                 * && "N".equals(isSecondaryShip) && "Y".equals(isSecondaryEpl))
                 * { addr.setCmrAddrTypeCode("ZS02"); } else {
                 * addr.setCmrAddrTypeCode("ZI01"); } }
                 * 
                 * converted.add(addr); }
                 */

                if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())
                    && !record.getCmrAddrSeq().equals(mainRecord.getCmrAddrSeq())) {
                  String seqSecondaryZS01 = record.getCmrAddrSeq();
                  String seqSecondaryZS01Legacy = StringUtils.leftPad(seqSecondaryZS01, 5, '0');
                  LOG.debug("---------seqSecondaryZS01Legacy---------------" + seqSecondaryZS01Legacy);
                  System.out.println("---------seqSecondaryZS01Legacy---------------" + seqSecondaryZS01Legacy);
                  String isSecondaryInst = isSecondaryInst(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), seqSecondaryZS01Legacy);
                  String isSecondaryBill = isSecondaryBill(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), seqSecondaryZS01Legacy);
                  String isSecondaryShip = isSecondaryShip(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), seqSecondaryZS01Legacy);
                  String isSecondaryEpl = isSecondaryEpl(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), seqSecondaryZS01Legacy);

                  if ("Y".equals(isSecondaryBill)) {
                    addr.setCmrAddrTypeCode("ZP01");
                  } else if ("N".equals(isSecondaryBill) && "Y".equals(isSecondaryInst)) {
                    addr.setCmrAddrTypeCode("ZI01");
                  } else if ("N".equals(isSecondaryBill) && "N".equals(isSecondaryInst) && "Y".equals(isSecondaryShip)) {
                    addr.setCmrAddrTypeCode("ZD01");
                  } else if ("N".equals(isSecondaryBill) && "N".equals(isSecondaryInst) && "N".equals(isSecondaryShip)
                      && "Y".equals(isSecondaryEpl)) {
                    addr.setCmrAddrTypeCode("ZS02");
                  } else {
                    addr.setCmrAddrTypeCode("ZI01");
                  }

                }

                converted.add(addr);
              }

              if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())
                  && record.getCmrAddrSeq().equals(mainRecord.getCmrAddrSeq())) {
                String kunnr = addr.getCmrSapNumber();
                // String adrnr = getaddAddressAdrnr(entityManager, cmrIssueCd,
                // SystemConfiguration.getValue("MANDT"), kunnr,
                // addr.getCmrAddrTypeCode(),
                // addr.getCmrAddrSeq());
                // int maxintSeq = getMaxSequenceOnAddr(entityManager,
                // SystemConfiguration.getValue("MANDT"),
                // reqEntry.getCmrIssuingCntry(),
                // record.getCmrNum());

                // String maxSeq =
                // StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0');
                // String legacyGaddrSeq =
                // getGaddressSeqFromLegacy(entityManager,
                // reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                // String legacyzs01Seq = getZS01SeqFromLegacy(entityManager,
                // reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                // String legacyGaddrLN6 =
                // getGaddressAddLN6FromLegacy(entityManager,
                // reqEntry.getCmrIssuingCntry(), record.getCmrNum());

                // int maxintSeqLegacy =
                // getMaxSequenceOnLegacyAddr(entityManager,
                // reqEntry.getCmrIssuingCntry(), record.getCmrNum());

                // add new here
                String soldtoseq = getSoldtoaddrSeqFromLegacy(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                // int maxintSeqadd = getMaxSequenceOnLegacyAddr(entityManager,
                // reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                // String maxSeqs =
                // StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0');

                // check if share seq address
                String isShareZP01 = isShareZP01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), soldtoseq);
                String isShareZS02 = isShareZS02(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), soldtoseq);
                String isShareZD01 = isShareZD01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), soldtoseq);
                String isShareZI01 = isShareZI01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), soldtoseq);

                // add share ZP01
                if (isShareZP01 != null) {
                  FindCMRRecordModel sharezp01 = new FindCMRRecordModel();
                  PropertyUtils.copyProperties(sharezp01, mainRecord);
                  sharezp01.setCmrAddrTypeCode("ZP01");
                  sharezp01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                  maxintSeqLegacy++;
                  sharezp01.setParentCMRNo("");
                  sharezp01.setCmrSapNumber(mainRecord.getCmrSapNumber());
                  sharezp01.setCmrDept(mainRecord.getCmrCity2());
                  converted.add(sharezp01);
                }
                // add share ZS02
                if (isShareZS02 != null) {
                  FindCMRRecordModel sharezs02 = new FindCMRRecordModel();
                  PropertyUtils.copyProperties(sharezs02, mainRecord);
                  sharezs02.setCmrAddrTypeCode("ZS02");
                  sharezs02.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                  maxintSeqLegacy++;
                  sharezs02.setParentCMRNo("");
                  sharezs02.setCmrSapNumber(mainRecord.getCmrSapNumber());
                  sharezs02.setCmrDept(mainRecord.getCmrCity2());
                  converted.add(sharezs02);
                }
                // add share ZD01
                if (isShareZD01 != null) {
                  FindCMRRecordModel sharezd01 = new FindCMRRecordModel();
                  PropertyUtils.copyProperties(sharezd01, mainRecord);
                  sharezd01.setCmrAddrTypeCode("ZD01");
                  sharezd01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                  maxintSeqLegacy++;
                  sharezd01.setParentCMRNo("");
                  sharezd01.setCmrSapNumber(mainRecord.getCmrSapNumber());
                  sharezd01.setCmrDept(mainRecord.getCmrCity2());
                  converted.add(sharezd01);
                }
                // add share ZI01
                if (isShareZI01 != null) {
                  FindCMRRecordModel sharezi01 = new FindCMRRecordModel();
                  PropertyUtils.copyProperties(sharezi01, mainRecord);
                  sharezi01.setCmrAddrTypeCode("ZI01");
                  sharezi01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                  maxintSeqLegacy++;
                  sharezi01.setParentCMRNo("");
                  sharezi01.setCmrSapNumber(mainRecord.getCmrSapNumber());
                  sharezi01.setCmrDept(mainRecord.getCmrCity2());
                  converted.add(sharezi01);
                }
                System.out.println("---------ZS01 Share Seq Max Seq is ---------------" + maxintSeqLegacy);

              }

              if (CmrConstants.ADDR_TYPE.ZI01.toString().equals(record.getCmrAddrTypeCode())) {
                String installingseq = record.getCmrAddrSeq();
                String installingseqLegacy = StringUtils.leftPad(String.valueOf(installingseq), 5, '0');
                // check if share installing address
                String isShareZP01 = isShareZP01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), installingseqLegacy);
                String isShareZS02 = isShareZS02(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), installingseqLegacy);
                String isShareZD01 = isShareZD01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), installingseqLegacy);
                String isShareZI01 = isShareZI01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), installingseqLegacy);

                if ((isShareZI01 != null && isShareZS02 != null) || (isShareZS02 != null && isShareZD01 != null)
                    || (isShareZI01 != null && isShareZD01 != null)) {
                  // add share ZP01
                  if (isShareZP01 != null && (!installingseq.equals(mainRecord.getCmrAddrSeq()))) {
                    FindCMRRecordModel sharezp01 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezp01, record);
                    sharezp01.setCmrAddrTypeCode("ZP01");
                    sharezp01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezp01.setParentCMRNo("");
                    sharezp01.setCmrSapNumber(record.getCmrSapNumber());
                    sharezp01.setCmrDept(record.getCmrCity2());
                    converted.add(sharezp01);
                    InstalingShareSeq = record.getCmrAddrSeq();
                  }
                  // add share ZS02
                  if (isShareZS02 != null && (!installingseq.equals(mainRecord.getCmrAddrSeq())) && "Y".equals(zi01Flag)) {
                    FindCMRRecordModel sharezs02 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezs02, record);
                    sharezs02.setCmrAddrTypeCode("ZS02");
                    sharezs02.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezs02.setParentCMRNo("");
                    sharezs02.setCmrSapNumber(record.getCmrSapNumber());
                    sharezs02.setCmrDept(record.getCmrCity2());
                    converted.add(sharezs02);
                    InstalingShareSeq = record.getCmrAddrSeq();
                  }
                  // add share ZD01
                  if (isShareZD01 != null && (!installingseq.equals(mainRecord.getCmrAddrSeq()))) {
                    FindCMRRecordModel sharezd01 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezd01, record);
                    sharezd01.setCmrAddrTypeCode("ZD01");
                    sharezd01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezd01.setParentCMRNo("");
                    sharezd01.setCmrSapNumber(record.getCmrSapNumber());
                    sharezd01.setCmrDept(record.getCmrCity2());
                    converted.add(sharezd01);
                    InstalingShareSeq = record.getCmrAddrSeq();
                  }
                  // add share ZI01
                  if (isShareZI01 != null && (!installingseq.equals(mainRecord.getCmrAddrSeq())) && "Y".equals(zs02Flag)) {
                    FindCMRRecordModel sharezi01 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezi01, record);
                    sharezi01.setCmrAddrTypeCode("ZI01");
                    sharezi01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezi01.setParentCMRNo("");
                    sharezi01.setCmrSapNumber(record.getCmrSapNumber());
                    sharezi01.setCmrDept(record.getCmrCity2());
                    converted.add(sharezi01);
                    InstalingShareSeq = record.getCmrAddrSeq();
                  }
                  System.out.println("---------ZI01 Share Seq Max Seq is ---------------" + maxintSeqLegacy);
                }
                zs02Flag = null;
                zi01Flag = null;
              }

              if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())
                  && !record.getCmrAddrSeq().equals(mainRecord.getCmrAddrSeq())) {

                String SecondaryZS01ShareSeq = record.getCmrAddrSeq();
                String SecondaryZS01ShareSeqLegacy = StringUtils.leftPad(SecondaryZS01ShareSeq, 5, '0');

                // check if share seq address
                String isShareZP01 = isShareZP01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), SecondaryZS01ShareSeqLegacy);
                String isShareZS02 = isShareZS02(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), SecondaryZS01ShareSeqLegacy);
                String isShareZD01 = isShareZD01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), SecondaryZS01ShareSeqLegacy);
                String isShareZI01 = isShareZI01(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum(), SecondaryZS01ShareSeqLegacy);

                // cheeck if SecondaryZS01 share ZP01
                if (isShareZP01 != null) {
                  // add share ZS02
                  if (isShareZS02 != null) {
                    FindCMRRecordModel sharezs02 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezs02, mainRecord);
                    sharezs02.setCmrAddrTypeCode("ZS02");
                    sharezs02.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezs02.setParentCMRNo("");
                    sharezs02.setCmrSapNumber(mainRecord.getCmrSapNumber());
                    sharezs02.setCmrDept(mainRecord.getCmrCity2());
                    converted.add(sharezs02);
                    SecondaryZS01ShareSeqflag = record.getCmrAddrSeq();
                  }
                  // add share ZD01
                  if (isShareZD01 != null) {
                    FindCMRRecordModel sharezd01 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezd01, mainRecord);
                    sharezd01.setCmrAddrTypeCode("ZD01");
                    sharezd01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezd01.setParentCMRNo("");
                    sharezd01.setCmrSapNumber(mainRecord.getCmrSapNumber());
                    sharezd01.setCmrDept(mainRecord.getCmrCity2());
                    converted.add(sharezd01);
                    SecondaryZS01ShareSeqflag = record.getCmrAddrSeq();
                  }
                  // add share ZI01
                  if (isShareZI01 != null) {
                    FindCMRRecordModel sharezi01 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezi01, mainRecord);
                    sharezi01.setCmrAddrTypeCode("ZI01");
                    sharezi01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezi01.setParentCMRNo("");
                    sharezi01.setCmrSapNumber(mainRecord.getCmrSapNumber());
                    sharezi01.setCmrDept(mainRecord.getCmrCity2());
                    converted.add(sharezi01);
                    SecondaryZS01ShareSeqflag = record.getCmrAddrSeq();
                  }
                }

                // cheeck if SecondaryZS01 share ZI01
                if (isShareZI01 != null && isShareZP01 == null) {
                  // add share ZS02
                  if (isShareZS02 != null) {
                    FindCMRRecordModel sharezs02 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezs02, mainRecord);
                    sharezs02.setCmrAddrTypeCode("ZS02");
                    sharezs02.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezs02.setParentCMRNo("");
                    sharezs02.setCmrSapNumber(mainRecord.getCmrSapNumber());
                    sharezs02.setCmrDept(mainRecord.getCmrCity2());
                    converted.add(sharezs02);
                    SecondaryZS01ShareSeqflag = record.getCmrAddrSeq();
                  }
                  // add share ZD01
                  if (isShareZD01 != null) {
                    FindCMRRecordModel sharezd01 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezd01, mainRecord);
                    sharezd01.setCmrAddrTypeCode("ZD01");
                    sharezd01.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezd01.setParentCMRNo("");
                    sharezd01.setCmrSapNumber(mainRecord.getCmrSapNumber());
                    sharezd01.setCmrDept(mainRecord.getCmrCity2());
                    converted.add(sharezd01);
                    SecondaryZS01ShareSeqflag = record.getCmrAddrSeq();
                  }
                }

                if (isShareZD01 != null && isShareZP01 == null && isShareZI01 == null) {
                  // add share ZS02
                  if (isShareZS02 != null) {
                    FindCMRRecordModel sharezs02 = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(sharezs02, mainRecord);
                    sharezs02.setCmrAddrTypeCode("ZS02");
                    sharezs02.setCmrAddrSeq(StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0'));
                    maxintSeqLegacy++;
                    sharezs02.setParentCMRNo("");
                    sharezs02.setCmrSapNumber(mainRecord.getCmrSapNumber());
                    sharezs02.setCmrDept(mainRecord.getCmrCity2());
                    converted.add(sharezs02);
                    SecondaryZS01ShareSeqflag = record.getCmrAddrSeq();
                  }
                }

                System.out.println("---------SecondaryZS01ShareSeq Max Seq is ---------------" + maxintSeqLegacy);
              }
            }

            // int parvmCount = getKnvpParvmCount(record.getCmrSapNumber());
            // System.out.println("parvmCount = " + parvmCount);
            //
            // if
            // ((CmrConstants.ADDR_TYPE.ZD01.toString().equals(record.getCmrAddrTypeCode()))
            // && (parvmCount > 1)) {
            // record.setCmrAddrTypeCode("ZS02");
            // }
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

  protected void importOtherSOFAddresses(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted) {

    /*
     * FindCMRRecordModel record = createAddress(entityManager, cmrCountry,
     * CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing", zi01Map); if
     * (record != null) { converted.add(record); }
     */

    FindCMRRecordModel record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
    if (record != null) {
      converted.add(record);
    }
    record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
    if (record != null) {
      converted.add(record);
    }

  }

  protected FindCMRRecordModel createZS01Address(EntityManager entityManager, String cmrIssuingCntry, String addressType, String addressKey,
      Map<String, FindCMRRecordModel> zi01Map, FindCMRRecordModel record) {
    if (!this.currentImportValues.containsKey(addressKey + "AddressNumber")) {
      return record;
    }
    LOG.debug("Adding " + addressKey + " address from SOF to request");
    // FindCMRRecordModel address = new FindCMRRecordModel();
    record.setCmrAddrTypeCode(addressType);
    record.setCmrAddrSeq(this.currentImportValues.get(addressKey + "AddressNumber"));

    if ("EplMailing".equals(addressKey) && zi01Map.containsKey(record.getCmrAddrSeq()) && zi01Map.size() > 1) {
      FindCMRRecordModel epl = zi01Map.get(record.getCmrAddrSeq());
      LOG.debug("Switching address " + record.getCmrAddrSeq() + " to Epl");
      epl.setCmrAddrTypeCode("ZS02"); // switch ZI01 to EPL then do nothing
      return record;
    }
    record.setCmrName1Plain(this.currentImportValues.get(addressKey + "Name"));

    // run the specific handler import handler
    handleSOFAddressImport(entityManager, cmrIssuingCntry, record, addressKey);

    String transAddressNo = this.currentImportValues.get(addressKey + "TransAddressNumber");
    if (!StringUtils.isEmpty(transAddressNo) && StringUtils.isNumeric(transAddressNo) && transAddressNo.length() == 5) {
      record.setTransAddrNo(transAddressNo);
      LOG.trace("Translated Address No.: '" + record.getTransAddrNo() + "'");
    }

    record.setCmrIssuedBy(cmrIssuingCntry);

    return record;
  }

  protected void importZS01AddressFromSOF(EntityManager entityManager, String cmrCountry, Map<String, FindCMRRecordModel> zi01Map,
      List<FindCMRRecordModel> converted, FindCMRRecordModel record) {

    record = createZS01Address(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS01.toString(), "Mailing", zi01Map, record);
    /*
     * if (record != null) { converted.add(record); }
     */
  }

  public static void main(String[] args) {
    PropertyConfigurator.configure(CmrContextListener.class.getClassLoader().getResource("cmr-log4j.properties"));
    FindCMRRecordModel record = new FindCMRRecordModel();
    NORDXHandler handler = new NORDXHandler();
    handler.currentImportValues = new HashMap<String, String>();
    handler.currentImportValues.put("Address1", "CROSS BP");
    handler.currentImportValues.put("Address2", "ATT PERSN");
    handler.currentImportValues.put("Address3", "STREET ADDRESS");
    handler.currentImportValues.put("Address4", "PO BOX 1234");
    handler.currentImportValues.put("Address5", "AL-74 CITY OF INSTA");
    handler.currentImportValues.put("Address6", "ALBANIA");
    handler.currentImportValues.put("City", "CITY OF INSTA");
    handler.currentImportValues.put("ZipCode", "AL-74");

    handler.handleSOFAddressImport(null, "678", record, "");
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {

    // NORDX Order = ..., Street/PO BOX - postal code city - Country for CB
    // line1 = Customer name
    // line2 = Customer Name Cont/Att/Street/PO BOX
    // line3 = Att/Street/PO BOX/postal code city
    // line4 = Street/PO BOX/postal code city/Country
    // line5 = PO BOX/postal code city/Country
    // line6 = Postal code city/Country

    String line1 = getCurrentValue(addressKey, "Address1");
    String line2 = getCurrentValue(addressKey, "Address2");
    String line3 = getCurrentValue(addressKey, "Address3");
    String line4 = getCurrentValue(addressKey, "Address4");
    String line5 = getCurrentValue(addressKey, "Address5");
    String line6 = getCurrentValue(addressKey, "Address6");

    // line1 = Customer name
    address.setCmrName1Plain(line1);

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1, attLine = 0, boxLine = 0, zipLine = 0;
    for (String line : lines) {
      if (isAttn(line)) {
        address.setCmrName4(line.substring(4));
        attLine = lineNo;
      } else if (isPOBox(line) && line.length() > 7) {
        address.setCmrPOBox(line.substring(7));
        boxLine = lineNo;
      } else if (handlePostCity(line, addressKey, address)) {
        zipLine = lineNo;
      }
      lineNo++;
    }

    // attn and customer name cont lines
    if (attLine == 3) {
      address.setCmrName2Plain(line2);
    }

    // poBox & zipCode lines
    if (zipLine == 3) {
      if (attLine == 0 && boxLine == 0) {
        address.setCmrStreetAddress(line2);
      }
      setLandedCountry(entityManager, address, line4);
    } else if (zipLine == 4) {
      if (attLine == 0 && boxLine == 0) {
        address.setCmrName2Plain(line2);
        address.setCmrStreetAddress(line3);
      } else if (isAttn(line2)) {
        address.setCmrStreetAddress(line3);
      } else {
        address.setCmrName2Plain(line2);
      }
      setLandedCountry(entityManager, address, line5);
    } else if (zipLine == 5) {
      if (boxLine == 0) {
        address.setCmrStreetAddress(line4);
      } else if (isAttn(line2)) {
        address.setCmrStreetAddress(line3);
      } else if (isAttn(line3)) {
        address.setCmrName2Plain(line2);
      }
      setLandedCountry(entityManager, address, line6);
    } else if (zipLine == 6) {
      address.setCmrName2Plain(line2);
      address.setCmrStreetAddress(line4);
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded()) && (!StringUtils.isEmpty(line6) || !StringUtils.isEmpty(line5))) {
      // try landed country on line 6/5 all the time
      String lineToCheck = !StringUtils.isEmpty(line6) ? line6 : line5;
      String code = getCountryCode(entityManager, lineToCheck);
      if (!StringUtils.isEmpty(code)) {
        address.setCmrCountryLanded(code);
      }
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    trimAddressToFit(address);
    if (this.installingSequences.contains(address.getCmrAddrSeq()))
      MACHINES_MAP.put(address.getCmrAddrSeq(), getMachinesList(address));

    LOG.trace(addressKey + " " + address.getCmrAddrSeq());
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name 2: " + address.getCmrName2Plain());
    LOG.trace("Attn: " + address.getCmrName4());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Street 2: " + address.getCmrStreetAddressCont());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Postal Code: " + address.getCmrPostalCode());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Country: " + address.getCmrCountryLanded());
  }

  private void setLandedCountry(EntityManager entityManager, FindCMRRecordModel address, String line) {
    if (!StringUtils.isEmpty(line)) {
      String countryCd = getCountryCode(entityManager, line);
      if (!StringUtils.isEmpty(countryCd)) {
        address.setCmrCountryLanded(countryCd);
      }
    }
  }

  private boolean handlePostCity(String data, String addressKey, FindCMRRecordModel address) {
    if (StringUtils.isEmpty(data)) {
      return false;
    }

    // check if postCode or city matches the line
    String postCd = getCurrentValue(addressKey, "ZipCode");
    String city = getCurrentValue(addressKey, "City");
    postCd = !StringUtils.isEmpty(postCd) ? postCd : "";
    city = !StringUtils.isEmpty(city) ? city : "";

    if (data.trim().startsWith(postCd.trim()) || data.trim().endsWith(city.trim())) {
      address.setCmrPostalCode(postCd);
      address.setCmrCity(city);
      return true;
    }
    return false;
  }

  @Override
  protected boolean isPhone(String data) {
    if (data == null) {
      return false;
    }
    return data.matches("[0-9\\-\\+ ]*");
  }

  @Override
  protected String getCurrentValue(String addressKey, String valueKey) {
    String val = this.currentImportValues.get(addressKey + valueKey);
    if (StringUtils.isEmpty(val)) {
      return val;
    }
    return "-/X".equalsIgnoreCase(val) ? "" : ("*".equalsIgnoreCase(val) ? "" : val);
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = "Mailing";
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
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
      data.setAbbrevNm("");
      data.setAffiliate("");
      data.setClientTier("");
      data.setCompany("");
      data.setCustClass("");
      data.setEnterprise("");
      data.setInacCd("");
      data.setInacType("");
      data.setIsicCd("");
      data.setIsuCd("");
      data.setSearchTerm("");
      data.setSitePartyId("");
      data.setSubIndustryCd("");
      data.setTaxCd1("");
      data.setCustPrefLang("");
      data.setTaxCd2("");
      data.setPpsceid("");
      data.setMemLvl("");
      data.setBpRelType("");
      data.setCovId("");
      data.setBgId("");
      data.setGeoLocationCd("");
      if (prospectCmrChosen) {
        data.setOrdBlk(mainRecord.getCmrOrderBlock());
      } else {
        data.setOrdBlk("");
      }
      data.setCovDesc("");
      data.setBgDesc("");
      data.setBgRuleId("");
      data.setGeoLocDesc("");
      data.setGbgId("");
      data.setGbgDesc("");
      data.setSearchTerm("");
      data.setMilitary(null);
      return;
    }
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    // CREATCMR-1653
    String zs01sapNo = getKunnrSapr3Kna1ForNordx(data.getCmrNo(), mainRecord.getCmrOrderBlock(), data.getCmrIssuingCntry());
    String currencyCd = getCurrencyCode(zs01sapNo);
    data.setCurrencyCd(currencyCd);
    // CREATCMR-1653

    // CREATCMR-1651
    String kuklaCd = getKunnrSapr3Kna1ForNordxKUKLA(data.getCmrNo(), data.getCmrIssuingCntry());
    data.setCustClass(kuklaCd);
    // CREATCMR-1651

    // CREATCMR-1638
    String modeOfPayment = getModeOfPayment(zs01sapNo);
    data.setModeOfPayment(modeOfPayment);
    // CREATCMR-1638

    // CREATCMR-2144
    String sprasCd = getSprasSapr3Kna1ForNordx(data.getCmrNo(), mainRecord.getCmrOrderBlock(), data.getCmrIssuingCntry());
    data.setCustPrefLang(sprasCd);
    // CREATCMR-2144

    // CREATCMR-1657
    data.setDunsNo(mainRecord.getCmrDuns());
    // CREATCMR-1657

    // data.setEngineeringBo(this.currentImportValues.get("ACAdmDSC"));
    // LOG.trace("ACAdmDSC: " + data.getEngineeringBo());
    // CMR-1746 Change Start
    String cmrNo = data.getCmrNo();
    String cntry = data.getCmrIssuingCntry();

    // CREATCMR-2674
    // String engineeringBo = getACAdminFromLegacy(cntry, cmrNo);
    // data.setEngineeringBo(engineeringBo);
    // LOG.trace("ACAdmDSC: " + data.getEngineeringBo());

    String salesRep = getSRFromLegacy(cntry, cmrNo);
    data.setRepTeamMemberNo(salesRep);
    LOG.trace("Sales Rep No: " + data.getRepTeamMemberNo());
    // CMR-1746 change end

    data.setTaxCd1(this.currentImportValues.get("TaxCode"));
    LOG.trace("TaxCode: " + data.getTaxCd1());

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

    // value = leading account no + mrc
    String value = this.currentImportValues.get("LeadingAccountNo");
    if (!StringUtils.isEmpty(value) && value.length() > 6) {
      data.setCompany(value.substring(0, 6));
    }
    LOG.trace("LeadingAccountNo: " + data.getCompany());

    data.setLocationNumber((this.currentImportValues.get("LocationNumber")));
    LOG.trace("LocationNumber: " + data.getLocationNumber());

    data.setInstallBranchOff("");
    data.setInacType("");
    // if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
    // data.setSitePartyId("");
    // data.setPpsceid("");
    // }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    address.setCustNm3(currentRecord.getCmrName3());
    address.setCustNm4(currentRecord.getCmrName4());
    address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
    address.setTransportZone("");
    if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
        && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
      address.getId().setAddrSeq("00001");
    }
    if (!"ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
      address.setCustPhone("");
    }
    String spid = "";
    if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType())) {
      spid = getRDcIerpSitePartyId(currentRecord.getCmrSapNumber());
      address.setIerpSitePrtyId(spid);
    } else {
      address.setIerpSitePrtyId(spid);
    }
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
    mv.addObject("machines", new MachineModel());
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      admin.setDelInd(null);
    }
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    String[] sheetNames = { "Data", "Mailing", "Billing", "Shipping", "Installing", "EPL" };
    XSSFCell currCell = null;
    for (String name : sheetNames) {
      XSSFSheet sheet = book.getSheet(name);
      LOG.debug("validating for sheet " + name);
      if (sheet != null) {
        int rowCount = 0;
        for (Row row : sheet) {
          TemplateValidation error = new TemplateValidation(name);
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
            rowCount++;
            if (rowCount < 2) {
              continue;
            }
            DataFormatter df = new DataFormatter();
            String cmrNo = ""; // 0
            String abbName = ""; // 1
            String abbLocation = ""; // 2
            String ISIC = ""; // 3
            String currencyCd = ""; // 4
            String tax = ""; // 5
            String inac = ""; // 6
            String collection = ""; // 7
            String payment = ""; // 8
            String embargo = ""; // 9
            String isu = ""; // 10
            String ctc = ""; // 11
            String leadingAccount = ""; // 12
            String sortl = ""; // 13
            String vat = ""; // 14
            String salesRep = ""; // 15
            String phone = ""; // 16
            String language = ""; // 17

            currCell = (XSSFCell) row.getCell(0);
            cmrNo = validateColValFromCell(currCell);

            if (row.getRowNum() == 2001) {
              continue;
            }

            if (StringUtils.isEmpty(cmrNo)) {
              LOG.trace("The row " + (row.getRowNum() + 1)
                  + ":Note that if the ROW format is changed then CMR No. is mandatory field to be filled. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "CMR No.",
                  ":Note that if the ROW format is changed then CMR No. is mandatory field to be filled. Please fix and upload the template again.<br>");
            }
            if (isDivCMR(cmrNo, country)) {
              LOG.trace("The row " + (row.getRowNum() + 1)
                  + ":Note the entered CMR number is either cancelled, divestiture or doesn't exist.Please check the template and correct.");
              error.addError((row.getRowNum() + 1), "CMR No.",
                  ":Note the entered CMR number is either cancelled, divestiture or doesn't exist.Please check the template and correct.<br>");
            }

            boolean dummyUpd = true;
            // assume mailing/billing addr 12 fields,other 11 fields
            int loopFlag = "Data".equals(name) ? 16 : "Mailing,Billing".contains(name) ? 12 : 11;
            int beginPos = "Data".equals(name) ? 1 : 2;
            for (int i = beginPos; i < loopFlag; i++) {
              XSSFCell cell = (XSSFCell) row.getCell(i);
              String addrField = validateColValFromCell(cell);
              if (StringUtils.isNotBlank(addrField)) {
                dummyUpd = false;
                break;
              }
            }
            if ("Data".equals(name)) {
              if (dummyUpd) {
                continue;
              }
              currCell = (XSSFCell) row.getCell(1);
              abbName = validateColValFromCell(currCell);
              if ("@".equals(abbName)) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Abbreviated Name is not allowed blank out. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Abbreviated Name",
                    ":Note that Abbreviated Name is not allowed blank out. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(2);
              abbLocation = validateColValFromCell(currCell);
              if ("@".equals(abbLocation)) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Abbreviated Location is not allowed blank out. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Abbreviated Location",
                    ":Note that Abbreviated Location is mandatory. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(7);
              collection = validateColValFromCell(currCell);
              if (!StringUtils.isBlank(collection) && !(collection.matches("^[A-Za-z0-9]+$") || "@@@@@@".equals(collection))) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Collection code only accept AlphaNumeric. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Collection Code",
                    ":Note that Collection Code only accept AlphaNumeric. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(8);
              payment = validateColValFromCell(currCell);
              if ("@".equals(payment)) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Payment terms is not allowed blank out. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Payment terms",
                    ":Note that Payment terms is not allowed blank out. Please fix and upload the template again.<br>");
              }

              if (!StringUtils.isBlank(payment) && !payment.matches("^[A-Za-z0-9]+$")) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Payment terms only accept AlphaNumeric. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Payment terms",
                    ":Note that Payment terms only accept AlphaNumeric. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(9);
              embargo = validateColValFromCell(currCell);
              if (StringUtils.isNotBlank(embargo) && !"@JDK".contains(embargo)) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Embargo code only accept @,J,D,K values. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Embargo code",
                    ":Note that Embargo code only accept @,J,D,K values. Please fix and upload the template again.<br>");
              }
              currCell = (XSSFCell) row.getCell(11);
              ctc = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(10);
              isu = validateColValFromCell(currCell);
              if ((StringUtils.isNotBlank(isu) && StringUtils.isBlank(ctc)) || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isu))) {
                LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that both ISU and CTC value needs to be filled..");
                error.addError((row.getRowNum() + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
              } else if (!StringUtils.isBlank(isu) && "34".equals(isu)) {
                if (!"Q".contains(ctc) || StringUtils.isBlank(ctc)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isu) && "32".equals(isu)) {
                if (!"T".contains(ctc) || StringUtils.isBlank(ctc)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if (!StringUtils.isBlank(isu) && "36".equals(isu)) {
                if (!"Y".contains(ctc) || StringUtils.isBlank(ctc)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Client Tier",
                      ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
                }
              } else if ((!StringUtils.isBlank(isu) && !Arrays.asList("32", "34", "36").contains(isu)) && !"@".equalsIgnoreCase(ctc)) {
                LOG.trace("Client Tier should be '@' for the selected ISU Code.");
                error.addError(row.getRowNum() + 1, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isu + ".<br>");
              }
              currCell = (XSSFCell) row.getCell(12);
              leadingAccount = validateColValFromCell(currCell);
              if ("@".equals(leadingAccount)) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Leading Account Number is not allowed blank out. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Leading Account Number",
                    ":Note that Leading Account Number is not allowed blank out. Please fix and upload the template again.<br>");
              }
              if (!StringUtils.isBlank(leadingAccount) && !leadingAccount.matches("^[0-9]*$")) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Leading Account Number should be only numeric. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Leading Account Number",
                    ":Note that Leading Account Number should be only numeric. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(6);
              inac = validateColValFromCell(currCell);
              if (!StringUtils.isBlank(inac) && !(inac.matches("^[a-zA-z]{2}[0-9]{2}") || StringUtils.isNumeric(inac) || "@@@@".equals(inac))) {
                LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that INAC format is incorrect. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "INAC", ":Note that INAC format is incorrect. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(13);
              sortl = validateColValFromCell(currCell);
              if (!StringUtils.isBlank(sortl) && !sortl.matches("^[A-Za-z0-9]+$")) {
                LOG.trace(
                    "The row " + (row.getRowNum() + 1) + ":Note that SORTL should be only alphanumeric. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "SORTL",
                    ":Note that SORTL should be only alphanumeric. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(14);
              vat = validateColValFromCell(currCell);
              String vatTxt = df.formatCellValue(currCell);
              // if (!StringUtils.isBlank(phone) &&
              // !phoneTxt.matches("^[0-9]*$")) {
              if (!StringUtils.isBlank(vat) && !vat.equals(vatTxt)) {
                LOG.trace("The row " + (row.getRowNum() + 1) + " Note that VAT format is incorrect. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "VAT",
                    "The row " + (row.getRowNum() + 1) + ":Note that VAT format is incorrect. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(15);
              salesRep = validateColValFromCell(currCell);
              if ("@".equals(salesRep)) {
                LOG.trace(
                    "The row " + (row.getRowNum() + 1) + ":Note that Sales Rep is not allowed blank out. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Sales Rep",
                    ":Note that Sales Rep is not allowed blank out. Please fix and upload the template again.<br>");
              }
              if (!StringUtils.isBlank(salesRep) && !salesRep.matches("^[A-Za-z0-9]+$")) {
                LOG.trace(
                    "The row " + (row.getRowNum() + 1) + ":Note that Sales Rep only accept AlphaNumeric. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Sales Rep",
                    ":Note that Sales Rep only accept AlphaNumeric. Please fix and upload the template again.<br>");
              }

              currCell = (XSSFCell) row.getCell(16);
              phone = validateColValFromCell(currCell);
              String phoneTxt = df.formatCellValue(currCell);
              // if (!StringUtils.isBlank(phone) &&
              // !phoneTxt.matches("^[0-9]*$")) {
              if (!StringUtils.isBlank(phone) && !phone.equals(phoneTxt)) {
                LOG.trace("The row " + (row.getRowNum() + 1) + " Note that Phone format is incorrect. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Phone",
                    "The row " + (row.getRowNum() + 1) + ":Note that Phone format is incorrect. Please fix and upload the template again.<br>");
              }
            } else {
              String seq = "";// 1
              currCell = (XSSFCell) row.getCell(1);
              seq = validateColValFromCell(currCell);
              if (StringUtils.isEmpty(seq)) {
                LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that Sequence number is mandatory. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Seq Number",
                    ":Note that Sequence number is mandatory. Please fix and upload the template again.<br>");
              }

              if (dummyUpd) {
                continue;
              }

              String custNm = "";// 2
              String custNmCond = "";// 3
              String additionalInfo = "";// 4
              String attPerson = "";// 5
              String street = "";// 6
              String streetCon = "";// 7
              String postCd = "";// 8
              String city = "";// 9
              String landedCntry = "";// 10
              String poBox = "";// 11

              currCell = (XSSFCell) row.getCell(2);
              custNm = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(3);
              custNmCond = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(4);
              additionalInfo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              attPerson = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              street = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              streetCon = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              postCd = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              city = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(10);
              landedCntry = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(11);
              poBox = validateColValFromCell(currCell);

              if ("Installing,Shipping,EPL".contains(name)) {
                if (StringUtils.isBlank(custNm) || StringUtils.isBlank(street) || StringUtils.isBlank(city) || StringUtils.isBlank(landedCntry)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Customer name, Street, City, Landed Country must be filled. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), name,
                      ":Note that Customer name, Street, City, Landed Country must be filled. Please fix and upload the template again.<br>");
                }
              } else {
                if (SystemLocation.DENMARK.equals(country) || (SystemLocation.FINLAND.equals(country) && !"FI".equals(landedCntry))) {
                  boolean streetPoBoxValid = false;
                  if (StringUtils.isBlank(street)) {
                    if (StringUtils.isNotBlank(poBox)) {
                      streetPoBoxValid = true;
                    }
                  } else {
                    if (StringUtils.isBlank(poBox)) {
                      streetPoBoxValid = true;
                    }
                  }
                  if (StringUtils.isBlank(custNm) || StringUtils.isBlank(city) || !streetPoBoxValid || StringUtils.isBlank(landedCntry)) {
                    LOG.trace("The row " + (row.getRowNum() + 1)
                        + ":Note that Customer name, Street OR POBox, City, Landed Country all must be filled. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), name,
                        ":Note that Customer name, Street OR POBox, City, Landed Country must be filled. Please fix and upload the template again.<br>");

                  }
                } else if (SystemLocation.NORWAY.equals(country) || SystemLocation.SWEDEN.equals(country)
                    || (SystemLocation.FINLAND.equals(country) && "FI".equals(landedCntry))) {
                  if (StringUtils.isBlank(custNm) || StringUtils.isBlank(city) || StringUtils.isBlank(landedCntry)) {
                    LOG.trace("The row " + (row.getRowNum() + 1)
                        + ":Note that Customer name, City, Landed Country must be filled. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), name,
                        ":Note that Customer name, City, Landed Country must be filled. Please fix and upload the template again.<br>");
                  }
                }
              }

              boolean isCrossBoarder = true;
              String cntryDesc = "";
              if (SystemLocation.DENMARK.equals(country)) {
                cntryDesc = "DE";
              } else if (SystemLocation.FINLAND.equals(country)) {
                cntryDesc = "FI";
              } else if (SystemLocation.NORWAY.equals(country)) {
                cntryDesc = "NO";
              } else if (SystemLocation.SWEDEN.equals(country)) {
                cntryDesc = "SE";
              }
              if (StringUtils.isNotBlank(landedCntry) && cntryDesc.equals(landedCntry.substring(0, 2))) {
                isCrossBoarder = false;
              }

              int fieldCount = 0;
              if (StringUtils.isNotBlank(custNmCond)) {
                fieldCount++;
              }
              if (StringUtils.isNotBlank(additionalInfo)) {
                fieldCount++;
              }
              if (StringUtils.isNotBlank(attPerson)) {
                fieldCount++;
              }
              if (StringUtils.isNotBlank(street)) {
                fieldCount++;
              }

              if (isCrossBoarder) {
                if (StringUtils.isNotBlank(poBox) || StringUtils.isNotBlank(streetCon)) {
                  fieldCount++;
                }
                if (fieldCount > 3) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that only 3 fields of Customer name con't, Addtional Info, Att Person, Street, Street con't and/or Po Box can be filled at once. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), name,
                      ":Note that only 3 fields of Customer name con't, Addtional Info, Att Person, Street, Street con't and/or Po Box can be filled at once. Please fix and upload the template again.<br>");
                }
              } else {
                if (StringUtils.isNotBlank(poBox)) {
                  fieldCount++;
                }
                if (fieldCount == 5) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that only 4 fields of Customer name con't,Addtional Info, Att Person, Street, Po Box can be filled at once. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), name,
                      ":Note that only 4 fields of Customer name con't,Addtional Info, Att Person, Street, Po Box can be filled at once. Please fix and upload the template again.<br>");
                }
              }
              fieldCount = 0;

              if (StringUtils.isBlank(street)) {
                if (StringUtils.isNotBlank(streetCon)) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Street con't can't be filled without Street. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Street con't & Additional Info",
                      ":Note that Street con't can't be filled without Street. Please fix and upload the template again.<br>");
                }
              }

              if (StringUtils.isNotBlank(streetCon) && StringUtils.isNotBlank(additionalInfo)) {
                LOG.trace("The row " + (row.getRowNum() + 1)
                    + ":Note that Street con't and Additional Info both filled not allowed. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Street con't & Additional Info",
                    ":Note that Street con't and Additional Info both filled not allowed. Please fix and upload the template again.<br>");

              }

              if (StringUtils.isNotBlank(poBox) && !StringUtils.isNumericSpace(poBox)) {
                LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that PO Box only accept digits. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "PO BOX",
                    ":Note that PO Box only accept digits. Please fix and upload the template again.<br>");

              }

              if (StringUtils.isBlank(poBox)) {
                if (streetCon.length() > 30) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Street con't should less than 30 chars. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Street con't",
                      ":Note that Street con't should less than 30 chars. Please fix and upload the template again.<br>");

                }
              } else {
                String combingStr = streetCon + ", PO BOX " + poBox;
                if (combingStr.length() > 30) {
                  LOG.trace("The row " + (row.getRowNum() + 1)
                      + ":Note that Combo Street con't and PO Box(with prefix) should less than 30 chars. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "PO Box",
                      ":Note that Combo Street con't and PO Box(with prefix) should less than 30 chars. Please fix and upload the template again.<br>");
                }
              }

              if (!StringUtils.isEmpty(postCd)) {
                if (StringUtils.isEmpty(landedCntry)) {
                  LOG.trace("Please input landed Country when postal code is filled. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "Landed Country",
                      ":Please input landed Country when postal code is filled. Please fix and upload the template again.<br>");
                } else {
                  try {
                    ValidationResult validation = checkPostalCode(landedCntry.substring(0, 2), postCd, country);
                    if (!validation.isSuccess()) {
                      LOG.trace(validation.getErrorMessage());
                      error.addError((row.getRowNum() + 1), "Postal code.", validation.getErrorMessage() + "<br>");
                    }
                  } catch (Exception e) {
                    LOG.error("Error occured on connecting postal code validation service.");
                    e.printStackTrace();
                  }
                }
              }

              if (StringUtils.isNotBlank(city)) {
                if (city.length() > 30) {
                  LOG.trace(
                      "The row " + (row.getRowNum() + 1) + ":Note that City should less than 30 chars. Please fix and upload the template again.");
                  error.addError((row.getRowNum() + 1), "City",
                      ":Note that City should less than 30 chars. Please fix and upload the template again.<br>");
                }
                if (StringUtils.isNotBlank(postCd)) {
                  if ((city + postCd).length() > 30) {
                    LOG.trace("The row " + (row.getRowNum() + 1)
                        + ":Note that Combo City and postal code should less than 30 chars. Please fix and upload the template again.");
                    error.addError((row.getRowNum() + 1), "Post Code",
                        ":Note that Combo City and postal code should less than 30 chars. Please fix and upload the template again.<br>");

                  }
                }
              }
            }
          }
          if (error.hasErrors()) {
            validations.add(error);
          }
        }
      }
    }
  }

  private static ValidationResult checkPostalCode(String landedCountry, String postalCode, String cntryCode) throws Exception {
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");

    PostalCodeValidateRequest zipRequest = new PostalCodeValidateRequest();
    zipRequest.setMandt(mandt);
    zipRequest.setPostalCode(postalCode);
    zipRequest.setSysLoc(cntryCode);
    zipRequest.setCountry(landedCountry);

    LOG.debug("Validating Postal Code " + postalCode + " for landedCountry " + landedCountry + " (mandt: " + mandt + " sysloc:  " + cntryCode + ")");

    ValidatorClient client = CmrServicesFactory.getInstance().createClient(baseUrl, ValidatorClient.class);
    try {
      ValidationResult validation = client.executeAndWrap(ValidatorClient.POSTAL_CODE_APP_ID, zipRequest, ValidationResult.class);
      return validation;
    } catch (Exception e) {
      LOG.error("Error in postal code validation", e);
      return null;
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

    // if (!StringUtils.isBlank(data.getCustSubGrp())) {
    // if (data.getCustSubGrp().contains("BP") ||
    // data.getCustSubGrp().contains("BUS")) {
    // data.setBpRelType("CA");
    // }
    // }
    if ("U".equals(admin.getReqType())) {
      if ("Y".equals(data.getCapInd())) {
        data.setCapInd("Y");
      } else {
        data.setCapInd("N");
      }
    }

    if ("U".equals(admin.getReqType()) && "TREC".equals(admin.getReqReason())) {
      changeShareAddrFroTrce(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
          data.getCmrNo());
      updateChangeindToNForTrce(entityManager, data.getId().getReqId());
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);
    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);
    if ("ZI01".equals(addr.getId().getAddrType()) || "ZD01".equals(addr.getId().getAddrType()) || "ZS02".equals(addr.getId().getAddrType())) {
      addr.setPoBox("");
    }
    if (!"ZS01".equals(addr.getId().getAddrType())) {
      addr.setCustPhone("");
    }
    if ("ZS01".equals(addr.getId().getAddrType())) {
      String landCntry = addr.getLandCntry();
      if (data.getCustGrp() != null && data.getCustGrp().contains("LOC")) {
        if (data.getCountryUse() != null && !data.getCountryUse().isEmpty()) {
          landCntry = LANDED_CNTRY_MAP.get(data.getCountryUse());
        } else {
          landCntry = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
        }
      }
      if ("U".equals(admin.getReqType())) {
        String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.LANDCNTRY.BY_REQID");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", data.getId().getReqId());
        List<String> results = query.getResults(String.class);
        if (results != null && !results.isEmpty()) {
          landCntry = results.get(0);
        }
      }
      addr.setLandCntry(landCntry);
    }
    addr.setRol("Y");
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {

    if (!MACHINES_MAP.isEmpty()) {
      for (Map.Entry<String, List<MachineModel>> entry : MACHINES_MAP.entrySet()) {
        List<MachineModel> machinesList = entry.getValue();
        for (MachineModel model : machinesList) {
          deleteSingleMachine(entityManager, admin, data, model);
          createMachines(entityManager, admin, data, model);
        }
      }
    }

    String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);

    // for (Addr addr : addresses) {
    // try {
    // addr.setIerpSitePrtyId(data.getSitePartyId());
    // entityManager.merge(addr);
    // entityManager.flush();
    // } catch (Exception e) {
    // LOG.error("Error occured on setting SPID after import.");
    // }
    // }

    // for (Addr addr : addresses) {
    // if ("ZS01".equals(addr.getId().getAddrType())) {
    // String adrnr = getaddAddressAdrnr(entityManager,
    // data.getCmrIssuingCntry(), SystemConfiguration.getValue("MANDT"),
    // addr.getSapNo(),
    // addr.getId().getAddrType(), addr.getId().getAddrSeq());
    // String legacyGaddrSeq = getGaddressSeqFromLegacy(entityManager,
    // data.getCmrIssuingCntry(), data.getCmrNo());
    // if (StringUtils.isBlank(adrnr) && !StringUtils.isBlank(legacyGaddrSeq)) {
    // changeZS01AddrUpdate(entityManager, data.getId().getReqId());
    // changeZP01AddrUpdate(entityManager, data.getId().getReqId());
    // }
    // }
    // }

    if (ND_COUNTRIES_LIST.contains(data.getCmrIssuingCntry())) {
      String soldtoseq = getSoldtoaddrSeqFromLegacy(entityManager, data.getCmrIssuingCntry(), data.getCmrNo());
      // check if share seq address
      String isShareZP01 = isShareZP01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), soldtoseq);
      String isShareZS02 = isShareZS02(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), soldtoseq);
      String isShareZD01 = isShareZD01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), soldtoseq);
      String isShareZI01 = isShareZI01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), soldtoseq);

      if (isShareZP01 != null) {
        updateImportIndToNForSharezp01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZS02 != null) {
        updateImportIndToNForSharezs02Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZD01 != null) {
        updateImportIndToNForSharezd01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZI01 != null) {
        updateImportIndToNForSharezi01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZP01 != null || isShareZS02 != null || isShareZD01 != null || isShareZI01 != null) {
        updateChangeindToYForSharezs01Addr(entityManager, data.getId().getReqId());
      }
    }

    if (!StringUtils.isBlank(InstalingShareSeq) || !StringUtils.isBlank(SecondaryZS01ShareSeqflag)) {
      String installingseqLegacy = StringUtils.leftPad(String.valueOf(InstalingShareSeq), 5, '0');
      // check if share installing address
      String isShareZP01 = isShareZP01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);
      String isShareZS02 = isShareZS02(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);
      String isShareZD01 = isShareZD01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);
      String isShareZI01 = isShareZI01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);

      if (isShareZP01 != null) {
        updateImportIndToNForSharezp01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZS02 != null) {
        updateImportIndToNForSharezs02Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZD01 != null) {
        updateImportIndToNForSharezd01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZI01 != null) {
        updateImportIndToNForSharezi01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZP01 != null || isShareZS02 != null || isShareZD01 != null || isShareZI01 != null) {
        updateChangeindToYForSharezi01Addr(entityManager, data.getId().getReqId(), InstalingShareSeq);
      }
    }

    if (!StringUtils.isBlank(SecondaryZS01ShareSeqflag)) {
      String installingseqLegacy = StringUtils.leftPad(String.valueOf(SecondaryZS01ShareSeqflag), 5, '0');
      // check if share installing address
      String isShareZP01 = isShareZP01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);
      String isShareZS02 = isShareZS02(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);
      String isShareZD01 = isShareZD01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);
      String isShareZI01 = isShareZI01(entityManager, data.getCmrIssuingCntry(), data.getCmrNo(), installingseqLegacy);

      if (isShareZP01 != null) {
        updateImportIndToNForSharezp01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZS02 != null) {
        updateImportIndToNForSharezs02Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZD01 != null) {
        updateImportIndToNForSharezd01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZI01 != null) {
        updateImportIndToNForSharezi01Addr(entityManager, data.getId().getReqId(), SystemConfiguration.getValue("MANDT"), data.getCmrIssuingCntry(),
            data.getCmrNo());
      }
      if (isShareZP01 != null || isShareZS02 != null || isShareZD01 != null || isShareZI01 != null) {
        updateChangeindToYForSharezi01Addr(entityManager, data.getId().getReqId(), SecondaryZS01ShareSeqflag);
      }
    }

  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(
        Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM4", "ADDR_TXT", "CITY1", "STATE_PROV", "POST_CD", "LAND_CNTRY", "PO_BOX", "CUST_PHONE"));
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
      update.setDataField(PageManager.getLabel(cmrCountry, "Collection Code", "Collection Code"));
      update.setNewData(newData.getCollectionCd());
      update.setOldData(oldData.getCollectionCd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCurrencyCd(), newData.getCurrencyCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Currency", "Currency"));
      update.setNewData(newData.getCurrencyCd());
      update.setOldData(oldData.getCurrencyCd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Payment Terms", "PaymentTerms"));
      update.setNewData(newData.getModeOfPayment());
      update.setOldData(oldData.getModeOfPayment());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "KUKLA", "KUKLA"));
      update.setNewData(newData.getCustClass());
      update.setOldData(oldData.getCustClass());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCapInd(), newData.getCapInd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CAP Record", "CAP Record"));
      update.setNewData(newData.getCapInd());
      update.setOldData(oldData.getCapInd());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustPrefLang(), newData.getCustPrefLang())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Preferred Language", "Preferred Language"));
      update.setNewData(newData.getCustPrefLang());
      update.setOldData(oldData.getCustPrefLang());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSearchTerm(), newData.getSearchTerm())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SORTL", "SORTL"));
      update.setNewData(newData.getSearchTerm());
      update.setOldData(oldData.getSearchTerm());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(NORDX_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_MACHINES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

    List<MachinesToInstall> machines = query.getResults(MachinesToInstall.class);

    for (MachinesToInstall machine : machines) {
      if (StringUtils.isBlank(machine.getCurrentIndc())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "Machine", "Machine"));
        update.setNewData(machine.getId().getMachineTyp() + machine.getId().getMachineSerialNo());
        update.setOldData("");
        results.add(update);
      }
    }
  }

  private List<MachineModel> getMachinesList(FindCMRRecordModel address) {

    List<MachineModel> machineList = new ArrayList<MachineModel>();

    for (int i = 1; i == i; i++) {
      String machineType = this.currentImportValues.get("Installing_" + address.getCmrAddrSeq() + "_MachineType" + Integer.toString(i));
      String serialNumber = this.currentImportValues.get("Installing_" + address.getCmrAddrSeq() + "_MachineSerial" + Integer.toString(i));
      if (!StringUtils.isBlank(machineType) && !StringUtils.isBlank(serialNumber)) {
        MachineModel machineModel = new MachineModel();
        machineModel.setAddrType(address.getCmrAddrTypeCode());
        machineModel.setAddrSeq(address.getCmrAddrSeq());
        machineModel.setMachineTyp(machineType);
        machineModel.setMachineSerialNo(serialNumber);
        machineModel.setCurrentIndc("Y");
        machineList.add(machineModel);
      } else {
        break;
      }
    }
    return machineList;
  }

  private void createMachines(EntityManager entityManager, Admin admin, Data data, MachineModel model) {

    LOG.trace("Creating Machines To Install for  Addr record:  " + " [Request ID: " + admin.getId().getReqId() + " ,Addr Type: " + model.getAddrType()
        + " ,Addr Seq: " + model.getAddrSeq() + "]");

    // AppUser user = AppUser.getUser(request);

    MachinesToInstall machines = new MachinesToInstall();
    MachinesToInstallPK machinesPK = new MachinesToInstallPK();

    // Setting primary key fields
    machinesPK.setReqId(admin.getId().getReqId());
    machinesPK.setAddrType(model.getAddrType());
    machinesPK.setAddrSeq(model.getAddrSeq());
    machinesPK.setMachineTyp(model.getMachineTyp());
    machinesPK.setMachineSerialNo(model.getMachineSerialNo());

    // setting remaining fields
    machines.setId(machinesPK);
    machines.setCreateBy(admin.getRequesterId());
    machines.setCurrentIndc("Y"); // Confirm from Jeff
    machines.setLastUpdtBy(admin.getRequesterId());

    machines.setCreateTs(SystemUtil.getCurrentTimestamp());
    machines.setLastUpdtTs(machines.getCreateTs());

    entityManager.persist(machines);
    entityManager.flush();
    // createEntity(machines, entityManager);

  }

  public void deleteSingleMachine(EntityManager entityManager, Admin admin, Data data, MachineModel model) {
    LOG.trace("Deleting Machine To Install for  Addr record:  " + " [Request ID: " + admin.getId().getReqId() + " ,Addr Type: " + model.getAddrType()
        + " ,Addr Seq: " + model.getAddrSeq() + "]");

    String sql = ExternalizedQuery.getSql("MACHINES.SEARCH_SINGLE_MACHINE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    query.setParameter("ADDR_TYPE", model.getAddrType());
    query.setParameter("ADDR_SEQ", model.getAddrSeq());
    query.setParameter("MACHINE_SERIAL_NO", model.getMachineSerialNo());
    query.setParameter("MACHINE_TYP", model.getMachineTyp());

    List<MachinesToInstall> machines = query.getResults(MachinesToInstall.class);

    for (MachinesToInstall machine : machines) {
      MachinesToInstall merged = entityManager.merge(machine);
      if (merged != null) {
        entityManager.remove(merged);
      }
      entityManager.flush();
    }
  }

  @Override
  public boolean isAddressChanged(EntityManager entityManager, Addr addr, String cmrIssuingCntry, boolean computedChangeInd) {

    boolean machineUpdated = isMachineUpdated(entityManager, addr);
    if (computedChangeInd || machineUpdated)
      return true;
    else
      return computedChangeInd;
  }

  @Override
  public boolean checkCopyToAdditionalAddress(EntityManager entityManager, Addr copyAddr, String cmrIssuingCntry) throws Exception {

    if (copyAddr != null && copyAddr.getId() != null) {
      Admin adminRec = LegacyCommonUtil.getAdminByReqId(entityManager, copyAddr.getId().getReqId());
      if (adminRec != null) {
        boolean isCreateReq = CmrConstants.REQ_TYPE_CREATE.equals(adminRec.getReqType());
        if (isCreateReq && copyAddr.getId().getAddrSeq().compareTo("00006") >= 0) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";
    if (!StringUtils.isEmpty(addrType)) {
      int addrSeq = 0;
      if ("ZS01".equals(addrType)) {
        addrSeq = 1;
      } else if ("ZP01".equals(addrType)) {
        addrSeq = 2;
      } else if ("ZI01".equals(addrType)) {
        addrSeq = 3;
      } else if ("ZD01".equals(addrType)) {
        addrSeq = 4;
      } else if ("ZS02".equals(addrType)) {
        addrSeq = 5;
      }

      String reqType = getReqType(entityManager, reqId);
      String sql = ExternalizedQuery.getSql("ADDRESS.GETMADDRSEQ_CREATECMR");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", reqId);

      List<Object[]> resultsCMR = query.getResults();
      String maxAddrSeq = null;
      int maxSeq = 0;
      if (resultsCMR != null && resultsCMR.size() > 0) {
        boolean seqExistCMR = false;
        List<Integer> seqListCMR = new ArrayList<Integer>();
        // Get create cmr seq list
        for (int i = 0; i < resultsCMR.size(); i++) {
          String item = String.valueOf(resultsCMR.get(i));
          if (!StringUtils.isEmpty(item) && !"10001".equals(item) && !"20001".equals(item) && !"40001".equals(item) && !"60001".equals(item)
              && !"70001".equals(item) && !"70002".equals(item) && !"90001".equals(item) && !"99997".equals(item)) {
            seqListCMR.add(Integer.parseInt(item));
          }
        }
        // Check if seq is already exist in create cmr
        seqExistCMR = seqListCMR.contains(addrSeq);
        // Get Max seq from create cmr
        maxSeq = seqListCMR.get(0);
        for (int i = 0; i < seqListCMR.size(); i++) {
          if (maxSeq < seqListCMR.get(i)) {
            maxSeq = seqListCMR.get(i);
          }
        }
        if (seqExistCMR) {
          if (maxSeq < 6) {
            addrSeq = 6;
          } else {
            addrSeq = maxSeq + 1;
          }
        }
      }
      if (CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
        String cmrNo = getCMRNo(entityManager, reqId);
        if (!StringUtils.isEmpty(cmrNo)) {
          // String sqlRDC =
          // ExternalizedQuery.getSql("FR.ADDRESS.GETMADDRSEQ_RDC");
          // PreparedQuery queryRDC = new PreparedQuery(entityManager, sqlRDC);
          // queryRDC.setParameter("MANDT",
          // SystemConfiguration.getValue("MANDT"));
          // queryRDC.setParameter("ZZKV_CUSNO", cmrNo);

          String sqlRDC = ExternalizedQuery.getSql("NORDX.GETMADDRSEQ_CREATECMR");
          PreparedQuery queryRDC = new PreparedQuery(entityManager, sqlRDC);
          queryRDC.setParameter("REQ_ID", reqId);

          // List<Object[]> resultsRDC = queryRDC.getResults();
          // List<Integer> seqListRDC = new ArrayList<Integer>();
          // for (int i = 0; i < resultsRDC.size(); i++) {
          // String item = String.valueOf(resultsRDC.get(i));
          // if (!StringUtils.isEmpty(item)) {
          // seqListRDC.add(Integer.parseInt(item));
          // }
          // }
          // if (addrSeq < 6 && seqListRDC.contains(addrSeq)) {
          // if (maxSeq < 6) {
          // addrSeq = 6;
          // } else {
          // addrSeq = maxSeq + 1;
          // }
          // }
          // while (seqListRDC.contains(addrSeq)) {
          // addrSeq++;
          // }
          List<Object[]> results = queryRDC.getResults();
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
        }
      }

      // Old logic:if seq range is not 0-99999, set seq to 1
      if (!(addrSeq >= 1 && addrSeq <= 99999)) {
        addrSeq = 1;
      }
      newAddrSeq = "0000" + Integer.toString(addrSeq);
      newAddrSeq = newAddrSeq.substring(newAddrSeq.length() - 5, newAddrSeq.length());
    }
    return newAddrSeq;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = "";
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  public String getReqType(EntityManager entityManager, long reqId) {
    String reqType = "";
    String sql = ExternalizedQuery.getSql("ADMIN.GETREQTYPE.FR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      reqType = results.get(0);
    }
    return reqType;
  }

  public String getCMRNo(EntityManager entityManager, long reqId) {
    String cmrNo = "";
    String sql = ExternalizedQuery.getSql("DATA.GETCMRNO.FR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      cmrNo = results.get(0);
    }
    return cmrNo;
  }

  public boolean isMachineUpdated(EntityManager entityManager, Addr addr) {

    boolean machineUpdated = false;
    String sql = ExternalizedQuery.getSql("MACHINES.COUNT_IMP_MACHINES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

    List<MachinesToInstall> machinesImp = query.getResults(MachinesToInstall.class);

    String sql2 = ExternalizedQuery.getSql("MACHINES.COUNT_NEW_MACHINES");
    PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
    query2.setParameter("REQ_ID", addr.getId().getReqId());
    query2.setParameter("ADDR_TYPE", addr.getId().getAddrType());
    query2.setParameter("ADDR_SEQ", addr.getId().getAddrSeq());

    List<MachinesToInstall> machinesNew = query.getResults(MachinesToInstall.class);

    if (machinesImp != null && machinesImp.size() > 0 && machinesNew != null && machinesNew.size() > 0) {
      machineUpdated = true;
    }
    return machineUpdated;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustLang", "custPrefLang");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
    // map.put("##BPRelationType", "bpRelType");
    map.put("##LocalTax1", "taxCd1");
    map.put("##CAP", "capInd");
    map.put("##MachineType", "machineTyp");
    map.put("##RequestReason", "reqReason");
    map.put("##POBox", "poBox");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##CustPhone", "custPhone");
    map.put("##VATExempt", "vatExempt");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##City1", "city1");
    map.put("##ModeOfPayment", "paymentMode");
    map.put("##StateProv", "stateProv");
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
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    // map.put("##SitePartyID", "sitePartyId");
    map.put("##IERPSitePrtyId", "ierpSitePrtyId");
    map.put("##MachineSerialNo", "machineSerialNo");
    map.put("##SAPNumber", "sapNo");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Company", "company");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##EngineeringBo", "engineeringBo");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##CustomerName1", "custNm1");
    map.put("##ISIC", "isicCd");
    map.put("##CustomerName2", "custNm2");
    map.put("##CustomerName4", "custNm4");
    map.put("##PostalCode", "postCd");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    // map.put("##MembLevel", "memLvl");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##CountrySubRegion", "countryUse");
    map.put("##LocalTax_FI", "taxCd1");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return true;
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

  private int getNorKnvpParvmCount(String kunnr) throws Exception {
    int knvpParvmCount = 0;

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("ND.GET.KNVP.PARVW");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.CMMA_APP_ID;

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

  public Sadr getCEEAddtlAddr(EntityManager entityManager, String adrnr, String mandt) {
    Sadr sadr = new Sadr();
    String qryAddlAddr = ExternalizedQuery.getSql("GET.CEE_SADR_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryAddlAddr);
    query.setParameter("ADRNR", adrnr);
    query.setParameter("MANDT", mandt);
    sadr = query.getSingleResult(Sadr.class);

    return sadr;
  }

  private CmrtAddr getLegacyGAddress(EntityManager entityManager, String rcyaa, String cmrNo) {
    String sql = ExternalizedQuery.getSql("CEE.GETLEGACYGADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(true);
    return query.getSingleResult(CmrtAddr.class);
  }

  public static String getSoldtoaddrSeqFromLegacy(EntityManager entityManager, String rcyaa, String cmr_no) {
    String zs01Seq = "";
    String sql = ExternalizedQuery.getSql("ND.GET_SOLDTO_SEQ_FROM_LEGACY");
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
    String sql = ExternalizedQuery.getSql("ND.ISSHAREZP01");
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
    String sql = ExternalizedQuery.getSql("ND.ISSHAREZS02");
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
    String sql = ExternalizedQuery.getSql("ND.ISSHAREZD01");
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
    String sql = ExternalizedQuery.getSql("ND.ISSHAREZI01");
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

  private int getKnvpParvmCount(String kunnr) throws Exception {
    int knvpParvmCount = 0;

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNVP.PARVW");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.CMMA_APP_ID;

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

  private void changeZS01AddrUpdate(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.CHANGE.ZS01UPDATE"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
  }

  private void changeZP01AddrUpdate(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.CHANGE.ZP01UPDATE"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
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

  public void updateChangeindToYForSharezi01Addr(EntityManager entityManager, long reqId, String seq) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("ND.ADDR.UPDATE.ZI01SHARE.Y.CHANGEINCD"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("SEQ", seq);
    query.executeSql();
  }

  public void changeShareAddrFroTrce(EntityManager entityManager, long reqId, String mandt, String katr6, String cmrNo) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("ND.ADDR.UPDATE.SHARE.TRCE"));
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.executeSql();
  }

  public void updateChangeindToNForTrce(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("ND.ADDR.UPDATE.N.CHANGEINCD.TRCE"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
  }

  private static boolean isDivCMR(String cmrNo, String cntry) {
    boolean isDivestiture = true;
    String mandt = SystemConfiguration.getValue("MANDT");
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("ND.GET.ZS01KATR10");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("KATR6", cntry);
    query.setParameter("MANDT", mandt);
    query.setParameter("CMR", cmrNo);

    Kna1 zs01 = query.getSingleResult(Kna1.class);
    if (zs01 != null) {
      if (StringUtils.isBlank(zs01.getKatr10())) {
        isDivestiture = false;
      }
    }
    return isDivestiture;
  }

  // CREATCMR-1653
  private String getKunnrSapr3Kna1ForNordx(String cmrNo, String ordBlk, String countryCd) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNA1.KUNNR_U_NORDX");
    sql = StringUtils.replace(sql, ":KATR6", "'" + countryCd + "'");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":AUFSD", "'" + ordBlk + "'");

    String dbId = QueryClient.CMMA_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUNNR");
    query.addField("ZZKV_CUSNO");

    LOG.debug("Getting existing KUNNR value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      kunnr = record.get("KUNNR") != null ? record.get("KUNNR").toString() : "";
      LOG.debug("***RETURNING KUNNR > " + kunnr);
    }
    return kunnr;
  }

  private String getCurrencyCode(String kunnr) throws Exception {
    String currCode = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNVV.WAERS");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.CMMA_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("WAERS");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing WAERS value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      currCode = record.get("WAERS") != null ? record.get("WAERS").toString() : "";
      LOG.debug("***RETURNING WAERS > " + currCode + " WHERE KUNNR IS > " + kunnr);
    }
    return currCode;
  }
  // CREATCMR-1653

  // CREATCMR-1651
  private String getKunnrSapr3Kna1ForNordxKUKLA(String cmrNo, String countryCd) throws Exception {
    String kukla = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("QUERY.BR.AUTO.GET_UPDATE_INFO");
    sql = StringUtils.replace(sql, ":KATR6", "'" + countryCd + "'");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":KTOKD", "'ZS01'");

    String dbId = QueryClient.CMMA_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUKLA");
    query.addField("STCEG");
    query.addField("TELX1");
    query.addField("AUFSD");

    LOG.debug("Getting existing KUKLA value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      kukla = record.get("KUKLA") != null ? record.get("KUKLA").toString() : "";

      LOG.debug("***RETURNING KUKLA > " + kukla);
    }
    return kukla;
  }
  // CREATCMR-1651

  // CREATCMR-1638
  private String getModeOfPayment(String kunnr) throws Exception {
    String paymentCode = "";
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNVV.ZTERM");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.CMMA_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("ZTERM");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing WAERS value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      paymentCode = record.get("ZTERM") != null ? record.get("ZTERM").toString() : "";
      LOG.debug("***RETURNING ZTERM > " + paymentCode + " WHERE KUNNR IS > " + kunnr);
    }

    return paymentCode;
  }
  // CREATCMR-1638

  // CREATCMR-2144
  private String getSprasSapr3Kna1ForNordx(String cmrNo, String ordBlk, String countryCd) throws Exception {
    String spras = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KNA1.SPRAS");
    sql = StringUtils.replace(sql, ":KATR6", "'" + countryCd + "'");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":AUFSD", "'" + ordBlk + "'");

    String dbId = QueryClient.CMMA_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("SPRAS");
    query.addField("ZZKV_CUSNO");

    LOG.debug("Getting existing SPRAS value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      spras = record.get("SPRAS") != null ? record.get("SPRAS").toString() : "";

      LOG.debug("***RETURNING SPRAS > " + spras);
    }
    return spras;
  }
  // CREATCMR-2144

  // CMR-1746
  private String getACAdminFromLegacy(String rcyaa, String cmr_no) throws Exception {
    String acAdmin = "";
    String sql = ExternalizedQuery.getSql("ND.GETACADMIN");
    EntityManager em = JpaManager.getEntityManager();
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    List<String> results = new ArrayList<String>();
    results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      if (results.get(0) != null) {
        acAdmin = results.get(0);
      }
    }
    LOG.debug("acAdmin of Legacy" + acAdmin);
    return acAdmin;
  }

  // CMR-1746
  private String getSRFromLegacy(String rcyaa, String cmr_no) {
    String salesRep = "";
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("ND.GETSALESREP");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      if (results.get(0) != null) {
        salesRep = results.get(0);
      }
    }
    LOG.debug("saresRep of Legacy" + salesRep);
    return salesRep;
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD", "ABBREV_NM",
        "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL",
        "BP_REL_TYPE", "COMMERCIAL_FINANCED", "ENTERPRISE", "PHONE1", "PHONE3"));
    return fields;
  }

  private String getRDcIerpSitePartyId(String kunnr) throws Exception {
    String spid = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.IERP.BRAN5");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.CMMA_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BRAN5");
    query.addField("MANDT");
    query.addField("KUNNR");

    LOG.debug("Getting existing BRAN5 value from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      spid = record.get("BRAN5") != null ? record.get("BRAN5").toString() : "";
      LOG.debug("***RETURNING BRAN5 > " + spid + " WHERE KUNNR IS > " + kunnr);
    }
    return spid;
  }

  public String getSecondaryZS01Seq(EntityManager entityManager, String katr6, String mandt, String cmrno) {
    String hasSecondaryZS01 = null;
    String sql = ExternalizedQuery.getSql("ND.GET.ADRND.FROMKNA1");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KATR6", katr6);
    query.setParameter("MANDT", mandt);
    query.setParameter("ZZKV_CUSNO", cmrno);
    query.setForReadOnly(true);
    String result = query.getSingleResult(String.class);
    if (result != null) {
      hasSecondaryZS01 = result;
    }
    return hasSecondaryZS01;
  }

  public String isSecondaryInst(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String secondaryInst = null;
    String sql = ExternalizedQuery.getSql("ND.ISSECONDARYINST");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      secondaryInst = result;
    }
    return secondaryInst;
  }

  public String isSecondaryBill(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String secondaryInst = null;
    String sql = ExternalizedQuery.getSql("ND.ISSECONDARYBILL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      secondaryInst = result;
    }
    return secondaryInst;
  }

  public String isSecondaryShip(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String secondaryInst = null;
    String sql = ExternalizedQuery.getSql("ND.ISSECONDARYSHIP");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      secondaryInst = result;
    }
    return secondaryInst;
  }

  public String isSecondaryEpl(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String secondaryInst = null;
    String sql = ExternalizedQuery.getSql("ND.ISSECONDARYEPL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      secondaryInst = result;
    }
    return secondaryInst;
  }

  public String getLegacyAddressSeq(EntityManager entityManager, String rcyaa, String cmr_no, String seq) {
    String secondaryInst = null;
    String sql = ExternalizedQuery.getSql("ND.GETLEGACYADDRESSSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("RCUXA", cmr_no);
    query.setParameter("SEQ", seq);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      secondaryInst = result;
    }
    return secondaryInst;
  }

  public String getSourceidFromAdmin(EntityManager entityManager, long reqId) {
    String secondaryInst = null;
    String sql = ExternalizedQuery.getSql("ND.GETSOURCEIDFROMADMIN");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      secondaryInst = result;
    }
    return secondaryInst;
  }

  private int getMaxSequenceFromRdc(EntityManager entityManager, String mandt, String katr6, String cmrNo) {
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

}
