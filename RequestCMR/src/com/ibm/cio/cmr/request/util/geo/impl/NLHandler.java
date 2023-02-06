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
import com.ibm.cio.cmr.request.CmrException;
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
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
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
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for NL
 * 
 * @author Paul
 * 
 */
public class NLHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(NLHandler.class);
  private static final boolean RETRIEVE_INVALID_CUSTOMERS = true;

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  static {

    LANDED_CNTRY_MAP.put(SystemLocation.NETHERLANDS, "NL");

  }

  private static final String[] NL_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode", "LocalTax2",
      "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor", "Building", "County", "City2",
      "Department", "SpecialTaxCd", "SearchTerm", "SalRepNameNo" };

  private static final List<String> NL_COUNTRIES_LIST = Arrays.asList(SystemLocation.NETHERLANDS);

  protected static final String[] NL_MASS_UPDATE_SHEET_NAMES = { "Sold-to", "Bill-to", "Ship-to", "Install-at", "IGF Bill-to", };

  @Override
  protected void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {
    // CMR-2719 divestiture CMR not imported
    if ("AT".equals(mainRecord.getCmrCountryLanded())) {
      if (!StringUtils.isBlank(mainRecord.getCmrOwner())) {
        if (!"IBM".equals(mainRecord.getCmrOwner())) {
          throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
        }
      }
    }
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
        if (StringUtils.isNotBlank(reqEntry.getCmrIssuingCntry()) && "788".equals(reqEntry.getCmrIssuingCntry())
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

            // if
            // ((CmrConstants.ADDR_TYPE.ZD01.toString().equals(record.getCmrAddrTypeCode()))
            // && (parvmCount > 1)) {
            // record.setCmrAddrTypeCode("ZS02");
            // }

            System.out.println("seqNo = " + seqNo);
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
              sofUses = this.legacyObjects.getUsesBySequenceNo(seqNo);
              if (StringUtils.isNotBlank(record.getCmrAddrSeq()) && !sofUses.isEmpty()) {
                for (String sofUse : sofUses) {
                  addrType = record.getCmrAddrTypeCode();
                  if (!StringUtils.isEmpty(addrType)) {
                    addr = cloneAddress(record, addrType);
                    addr.setCmrDept(record.getCmrCity2());
                    addr.setCmrName4(record.getCmrName4());
                    if (NL_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                        && (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getCmrAddrTypeCode())) && "598".equals(addr.getCmrAddrSeq())) {
                      addr.setCmrAddrTypeCode("ZD02");
                    }
                    if (NL_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                        && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addr.getCmrAddrTypeCode())) && "28801".equals(addr.getCmrAddrSeq())) {
                      addr.setCmrAddrTypeCode("ZP02");
                    }
                    if ((CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getCmrAddrTypeCode()))) {
                      String stkzn = "";
                      stkzn = getStkznFromDataRdc(entityManager, addr.getCmrSapNumber(), SystemConfiguration.getValue("MANDT"));
                      int parvmCount = getCeeKnvpParvmCount(addr.getCmrSapNumber());
                      if ("0".equals(stkzn) || parvmCount > 0) {
                        addr.setCmrAddrTypeCode("ZS02");
                      }
                    }
                    converted.add(addr);
                  }
                }
              } else if (sofUses.isEmpty() && "ZP01".equals(record.getCmrAddrTypeCode()) && StringUtils.isNotEmpty(record.getExtWalletId())) {
                record.setCmrAddrTypeCode("PG01");
                addrType = record.getCmrAddrTypeCode();
                if (!StringUtils.isEmpty(addrType)) {
                  addr = cloneAddress(record, addrType);
                  addr.setCmrDept(record.getCmrCity2());
                  addr.setCmrName4(record.getCmrName4());
                  if (NL_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                      && (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getCmrAddrTypeCode())) && "598".equals(addr.getCmrAddrSeq())) {
                    addr.setCmrAddrTypeCode("ZD02");
                  }
                  if (NL_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                      && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addr.getCmrAddrTypeCode())) && "28801".equals(addr.getCmrAddrSeq())) {
                    addr.setCmrAddrTypeCode("ZP02");
                  }
                  if ((CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getCmrAddrTypeCode()))) {
                    String stkzn = "";
                    stkzn = getStkznFromDataRdc(entityManager, addr.getCmrSapNumber(), SystemConfiguration.getValue("MANDT"));
                    int parvmCount = getCeeKnvpParvmCount(addr.getCmrSapNumber());
                    if ("0".equals(stkzn) || parvmCount > 0) {
                      addr.setCmrAddrTypeCode("ZS02");
                    }
                  }
                  converted.add(addr);
                }
              }
              if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())) {
                String kunnr = addr.getCmrSapNumber();
                String adrnr = getaddAddressAdrnr(entityManager, cmrIssueCd, SystemConfiguration.getValue("MANDT"), kunnr, addr.getCmrAddrTypeCode(),
                    addr.getCmrAddrSeq());
                int maxintSeq = getMaxSequenceOnAddr(entityManager, SystemConfiguration.getValue("MANDT"), reqEntry.getCmrIssuingCntry(),
                    record.getCmrNum());
                int maxintSeqLegacy = getMaxSequenceOnLegacyAddr(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                String maxSeq = StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0');
                String legacyGaddrSeq = getGaddressSeqFromLegacy(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                String legacyzs01Seq = getZS01SeqFromLegacy(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                String legacyGaddrLN6 = getGaddressAddLN6FromLegacy(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                String gAddrSeq = "";
                if (!StringUtils.isEmpty(legacyGaddrSeq) && !legacyzs01Seq.equals(legacyGaddrSeq)) {
                  gAddrSeq = legacyGaddrSeq;
                } else {
                  gAddrSeq = maxSeq;
                  maxintSeqLegacy++;
                }
                if (!StringUtils.isBlank(adrnr)) {
                  Sadr sadr = getCEEAddtlAddr(entityManager, adrnr, SystemConfiguration.getValue("MANDT"));
                  if (sadr != null) {
                    LOG.debug("Adding installing to the records");
                    FindCMRRecordModel installing = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(installing, mainRecord);
                    installing.setCmrAddrTypeCode("ZP02");
                    installing.setCmrAddrSeq(gAddrSeq);
                    // installing.setParentCMRNo(mainRecord.getCmrNum());
                    installing.setCmrName1Plain(sadr.getName1());
                    installing.setCmrName2Plain(sadr.getName2());
                    installing.setCmrCity(sadr.getOrt01());
                    installing.setCmrCity2(sadr.getOrt02());
                    installing.setCmrStreetAddress(sadr.getStras());
                    installing.setCmrName3(sadr.getName3());
                    installing.setCmrName4(sadr.getName4());
                    installing.setCmrCountryLanded(sadr.getLand1());
                    installing.setCmrCountry(sadr.getSpras());
                    installing.setCmrStreetAddressCont(sadr.getStrs2());
                    installing.setCmrState(sadr.getRegio());
                    installing.setCmrPostalCode(sadr.getPstlz());
                    installing.setCmrDept(sadr.getOrt02());
                    installing.setCmrBldg(legacyGaddrLN6);
                    if (!StringUtils.isBlank(sadr.getTxjcd())) {
                      installing.setCmrTaxOffice(sadr.getTxjcd());
                    }
                    if (!StringUtils.isBlank(sadr.getTxjcd()) && !StringUtils.isBlank(sadr.getPfort())) {
                      installing.setCmrTaxOffice(sadr.getTxjcd() + sadr.getPfort());
                    }
                    installing.setCmrSapNumber("");
                    converted.add(installing);
                  } else {
                    CmrtAddr gAddr = getLegacyGAddress(entityManager, reqEntry.getCmrIssuingCntry(), searchModel.getCmrNum());
                    String legacycity = "";
                    if (gAddr != null) {
                      LOG.debug("Adding installing to the records");
                      FindCMRRecordModel installing = new FindCMRRecordModel();
                      PropertyUtils.copyProperties(installing, mainRecord);
                      // copyAddrData(installing, installingAddr, gAddrSeq);
                      installing.setCmrAddrTypeCode("ZP02");
                      installing.setCmrAddrSeq(gAddrSeq);
                      String gline5 = gAddr.getAddrLine5();
                      if (!StringUtils.isBlank(gline5)) {
                        String legacyposcd = gline5.split(" ")[0];
                        legacycity = gline5.substring(legacyposcd.length() + 1, gline5.length());
                      }
                      // add value
                      installing.setCmrName1Plain(gAddr.getAddrLine1());
                      if (!StringUtils.isBlank(gAddr.getAddrLine2())) {
                        installing.setCmrName2Plain(gAddr.getAddrLine2());
                      } else {
                        installing.setCmrName2Plain("");
                      }
                      // installing.setCmrStreetAddress(gAddr.getAddrLine3());
                      if (!StringUtils.isBlank(gAddr.getAddrLine3())) {
                        installing.setCmrStreetAddress(gAddr.getAddrLine3());
                      } else {
                        installing.setCmrStreetAddress(gAddr.getAddrLine4());
                      }
                      installing.setCmrCity(record.getCmrCity());
                      if ("865".equals(reqEntry.getCmrIssuingCntry())) {
                        installing.setCmrCity(legacycity);
                      }
                      installing.setCmrCity2(record.getCmrCity2());
                      installing.setCmrCountry(gAddr.getAddrLine6());
                      installing.setCmrCountryLanded("");
                      installing.setCmrPostalCode(record.getCmrPostalCode());
                      installing.setCmrState(record.getCmrState());
                      installing.setCmrBldg(legacyGaddrLN6);
                      if (StringUtils.isBlank(gAddr.getAddrLine3())) {
                        installing.setCmrStreetAddressCont("");
                      } else {
                        installing.setCmrStreetAddressCont(gAddr.getAddrLine4());
                      }
                      converted.add(installing);
                    }
                  }
                }
                if (StringUtils.isBlank(adrnr)) {
                  CmrtAddr gAddr = getLegacyGAddress(entityManager, reqEntry.getCmrIssuingCntry(), searchModel.getCmrNum());
                  String legacycity = "";
                  if (gAddr != null) {
                    LOG.debug("Adding installing to the records");
                    FindCMRRecordModel installing = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(installing, mainRecord);
                    // copyAddrData(installing, installingAddr, gAddrSeq);
                    installing.setCmrAddrTypeCode("ZP02");
                    installing.setCmrAddrSeq(gAddrSeq);
                    String gline5 = gAddr.getAddrLine5();
                    if (!StringUtils.isBlank(gline5)) {
                      String legacyposcd = gline5.split(" ")[0];
                      if (gline5.length() > legacyposcd.length()) {
                        legacycity = gline5.substring(legacyposcd.length() + 1, gline5.length());
                      }
                    }
                    // add value
                    installing.setCmrName1Plain(gAddr.getAddrLine1());
                    if (!StringUtils.isBlank(gAddr.getAddrLine2())) {
                      installing.setCmrName2Plain(gAddr.getAddrLine2());
                    } else {
                      installing.setCmrName2Plain("");
                    }
                    // installing.setCmrStreetAddress(gAddr.getAddrLine3());
                    if (!StringUtils.isBlank(gAddr.getAddrLine3())) {
                      installing.setCmrStreetAddress(gAddr.getAddrLine3());
                    } else {
                      installing.setCmrStreetAddress(gAddr.getAddrLine4());
                    }
                    installing.setCmrCity(record.getCmrCity());
                    if ("865".equals(reqEntry.getCmrIssuingCntry())) {
                      installing.setCmrCity(legacycity);
                    }
                    installing.setCmrCity2(record.getCmrCity2());
                    installing.setCmrCountry(gAddr.getAddrLine6());
                    installing.setCmrCountryLanded("");
                    installing.setCmrPostalCode(record.getCmrPostalCode());
                    installing.setCmrState(record.getCmrState());
                    installing.setCmrBldg(legacyGaddrLN6);
                    if (StringUtils.isBlank(gAddr.getAddrLine3())) {
                      installing.setCmrStreetAddressCont("");
                    } else {
                      installing.setCmrStreetAddressCont(gAddr.getAddrLine4());
                    }
                    converted.add(installing);
                  }
                }
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

            if ("788".equals(reqEntry.getCmrIssuingCntry()) && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(record.getCmrAddrTypeCode()))
                && "28801".equals(record.getCmrAddrSeq())) {
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

    // data.setEngineeringBo(this.currentImportValues.get("DPCEBO"));
    // LOG.trace("DPCEBO: " + data.getEngineeringBo());

    String kna1KVK = getKna1KVK(mainRecord.getCmrNum());
    String cmrtcextKVK = getCmrtcextKVK(mainRecord.getCmrNum());

    if (StringUtils.isNotEmpty(kna1KVK)) {
      data.setTaxCd2(kna1KVK);
      LOG.trace("KVK: " + data.getTaxCd2());
    } else {
      data.setTaxCd2(cmrtcextKVK);
      LOG.trace("KVK: " + data.getTaxCd2());
    }

    data.setTaxCd1(this.currentImportValues.get("TaxCode"));
    LOG.trace("TaxCode: " + data.getTaxCd1());

    data.setEconomicCd(this.currentImportValues.get("EconomicCd"));
    LOG.trace("EconomicCd: " + data.getEconomicCd());

    data.setEngineeringBo(this.currentImportValues.get("SBO"));
    LOG.trace("BOTeam: " + data.getEngineeringBo());

    data.setIbmDeptCostCenter(this.currentImportValues.get("DepartmentNumber"));
    LOG.trace("DepartmentNumber: " + data.getIbmDeptCostCenter());

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

    data.setEnterprise(this.currentImportValues.get("EnterpriseNo"));
    LOG.trace("EnterpriseNo: " + data.getEnterprise());

    data.setIsicCd(this.currentImportValues.get("ISIC"));
    LOG.trace("ISIC: " + data.getIsicCd());

    data.setInacCd(this.currentImportValues.get("INAC"));
    LOG.trace("INAC: " + data.getInacCd());

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
      sql = StringUtils.replace(sql, ":KATR6", "'" + "788" + "'");
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

  private String getKna1KVK(String cmrNo) throws Exception {
    String kvk = "";
    List<String> results = new ArrayList<String>();

    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String mandt = SystemConfiguration.getValue("MANDT");
      String sql = ExternalizedQuery.getSql("GET.KVK.KNA1.STCD1");
      sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
      sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
      sql = StringUtils.replace(sql, ":KATR6", "'" + "788" + "'");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      results = query.getResults(String.class);
      if (results != null && results.size() > 0) {
        kvk = results.get(0);
        if (kvk != null && kvk.length() >= 8) {
          kvk = kvk.substring(0, 8);
        }
      }
      LOG.debug("KVK of SAPR3.KNA1.STCD1 " + kvk);
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return kvk;
  }

  private String getCmrtcextKVK(String cmrNo) throws Exception {

    String kvk = "";
    List<String> results = new ArrayList<String>();
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      String sql = ExternalizedQuery.getSql("GET.KVK.CMRTCEXT.CODFIS");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("COUNTRY", "788");
      query.setParameter("CMR_NO", cmrNo);
      results = query.getResults(String.class);
      if (results != null && results.size() > 0) {
        kvk = results.get(0);
        if (kvk != null && kvk.length() >= 8) {
          kvk = kvk.substring(0, 8);
        }
      }
      LOG.debug("KVK of CMRDB2D.CMRTCEXT.CODFIS " + kvk);
    } finally {
      entityManager.clear();
      entityManager.close();
    }
    return kvk;
  }

  private boolean loadDuplicateCMR(Data data, String dupCntry, String dupCmrNo) throws Exception {
    FindCMRRecordModel dupRecord = new FindCMRRecordModel();
    dupRecord.setCmrIssuedBy(dupCntry);
    dupRecord.setCmrNum(dupCmrNo);
    // retrieveSOFValues(dupRecord);
    boolean checks = dupCMRExists(dupCntry, dupCmrNo);
    LOG.debug("dupCMRExists: " + checks);
    LOG.debug("dupCmrNo: " + dupCmrNo);

    if (checks) {
      if (this.currentImportValues != null && !this.currentImportValues.isEmpty()) {
        String abbrevNm = this.currentImportValues.get("CompanyName");
        if (!StringUtils.isEmpty(abbrevNm) && abbrevNm.endsWith(" CIS")) {
          // 4606 Russia CIS Duplicate CMR
          Map<String, Object> dupRecordV = getDupCMRFieldValue(dupCntry, dupCmrNo);
          data.setDupEnterpriseNo(dupRecordV.get("ZZKV_NODE1").toString());
          LOG.debug("CompanyNo2: " + data.getDupEnterpriseNo());
          data.setDupSalesRepNo(this.currentImportValues.get("SR"));
          LOG.debug("SalRepNameNo2: " + data.getDupSalesRepNo());
          data.setDupSalesBoCd(this.currentImportValues.get("SBO"));
          LOG.debug("SalesBusOff2: " + data.getDupSalesBoCd());
          data.setTaxCd3(dupRecordV.get("ZZKV_NODE2").toString());
          LOG.debug("SalesEnterpriseNo2: " + data.getTaxCd3());
          data.setDupIsuCd(dupRecordV.get("BRSCH").toString());
          LOG.debug("ISU2: " + data.getIsuCd());
          data.setDupClientTierCd(dupRecordV.get("KATR3").toString());
          LOG.debug("ClientTier2: " + data.getDupClientTierCd());
          data.setDupIssuingCntryCd(dupCntry);
          // String isuCtc = this.currentImportValues.get("ISU");
          // if (!StringUtils.isEmpty(isuCtc) && isuCtc.length() > 2) {
          // data.setDupIsuCd(isuCtc.substring(0, 2));
          // LOG.debug("ISU2: " + data.getIsuCd());
          // data.setDupClientTierCd(isuCtc.substring(2, 3));
          // LOG.debug("ClientTier2: " + data.getDupClientTierCd());
          // }
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    address.setCustNm4(currentRecord.getCmrName4());
    // address.setAddrTxt(currentRecord.getCmrStreetAddress());
    address.setDept(currentRecord.getCmrDept());
    address.setCity1(currentRecord.getCmrCity());
    address.setTransportZone("");
    setAddressSeqNo(address, currentRecord);
  }

  private void setAddressSeqNo(Addr address, FindCMRRecordModel currentRecord) {
    if (StringUtils.isEmpty(currentRecord.getCmrAddrTypeCode())) {
      return;
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZP01")) {
      address.getId().setAddrSeq("29901");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZS01")) {
      address.getId().setAddrSeq("00001");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZKVK")) {
      address.getId().setAddrSeq("21102");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZVAT")) {
      address.getId().setAddrSeq("21400");
    }
    if (currentRecord.getCmrAddrTypeCode().equals("ZP02")) {
      address.getId().setAddrSeq("28801");
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
    // addEditKVK_VATAddress(entityManager, addr);
    if (!"ZS01".equals(addr.getId().getAddrType())) {
      addr.setCustPhone("");
    }

  }

  private void addEditKVK_VATAddress(EntityManager entityManager, Addr addr) throws Exception {
    Addr kvkAdrr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZKVK.toString(), addr.getId().getReqId());
    Addr vatAddr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZVAT.toString(), addr.getId().getReqId());
    if (kvkAdrr == null) {
      // create KVK and VAT Address from General Address if not exists
      Addr generalAddr = null;
      if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType())) {
        generalAddr = addr;
      } else {
        generalAddr = getAddressByType(entityManager, CmrConstants.ADDR_TYPE.ZS01.toString(), addr.getId().getReqId());
      }
      if (generalAddr != null) {
        kvkAdrr = new Addr();
        vatAddr = new Addr();
        AddrPK newPk_kvk = new AddrPK();
        AddrPK newPK_vat = new AddrPK();
        newPk_kvk.setReqId(generalAddr.getId().getReqId());
        newPk_kvk.setAddrType(CmrConstants.ADDR_TYPE.ZKVK.toString());
        newPk_kvk.setAddrSeq("21102");
        newPK_vat.setReqId(generalAddr.getId().getReqId());
        newPK_vat.setAddrType(CmrConstants.ADDR_TYPE.ZVAT.toString());
        newPK_vat.setAddrSeq("21400");

        PropertyUtils.copyProperties(kvkAdrr, generalAddr);
        PropertyUtils.copyProperties(vatAddr, generalAddr);
        kvkAdrr.setImportInd(CmrConstants.YES_NO.N.toString());
        kvkAdrr.setSapNo(null);
        kvkAdrr.setRdcCreateDt(null);
        kvkAdrr.setId(newPk_kvk);
        vatAddr.setImportInd(CmrConstants.YES_NO.N.toString());
        vatAddr.setSapNo(null);
        vatAddr.setRdcCreateDt(null);
        vatAddr.setId(newPK_vat);

        entityManager.persist(kvkAdrr);
        entityManager.persist(vatAddr);
        entityManager.flush();
      }

    } else if (CmrConstants.ADDR_TYPE.ZS01.toString().equalsIgnoreCase(addr.getId().getAddrType())) {
      kvkAdrr.setCustNm1(addr.getCustNm1());
      kvkAdrr.setCustNm2(addr.getCustNm2());
      kvkAdrr.setCustNm4(addr.getCustNm4());
      kvkAdrr.setAddrTxt(addr.getAddrTxt());
      kvkAdrr.setAddrTxt2(addr.getAddrTxt2());
      kvkAdrr.setCity1(addr.getCity1());
      kvkAdrr.setPostCd(addr.getPostCd());
      kvkAdrr.setPoBox(addr.getPoBox());
      kvkAdrr.setCustPhone(addr.getCustPhone());
      kvkAdrr.setLandCntry(addr.getLandCntry());

      vatAddr.setCustNm1(addr.getCustNm1());
      vatAddr.setCustNm2(addr.getCustNm2());
      vatAddr.setCustNm4(addr.getCustNm4());
      vatAddr.setAddrTxt(addr.getAddrTxt());
      vatAddr.setAddrTxt2(addr.getAddrTxt2());
      vatAddr.setCity1(addr.getCity1());
      vatAddr.setPostCd(addr.getPostCd());
      vatAddr.setCustPhone(addr.getCustPhone());
      vatAddr.setLandCntry(addr.getLandCntry());
      vatAddr.setPoBox(addr.getPoBox());

      entityManager.merge(kvkAdrr);
      entityManager.merge(vatAddr);
      entityManager.flush();
    }
  }

  private Addr getAddressByType(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GET.BYTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    List<Addr> addrList = query.getResults(1, Addr.class);
    if (addrList != null && addrList.size() > 0) {
      return addrList.get(0);
    }
    return null;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);

    /*
     * for (Addr addr : addresses) { try {
     * addr.setIerpSitePrtyId(data.getSitePartyId()); entityManager.merge(addr);
     * entityManager.flush(); } catch (Exception e) {
     * LOG.error("Error occured on setting SPID after import."); } }
     */

    for (Addr addr : addresses) {
      if ("ZS01".equals(addr.getId().getAddrType())) {
        String adrnr = getaddAddressAdrnr(entityManager, data.getCmrIssuingCntry(), SystemConfiguration.getValue("MANDT"), addr.getSapNo(),
            addr.getId().getAddrType(), addr.getId().getAddrSeq());
        String legacyGaddrSeq = getGaddressSeqFromLegacy(entityManager, data.getCmrIssuingCntry(), data.getCmrNo());
        if (StringUtils.isBlank(adrnr) && !StringUtils.isBlank(legacyGaddrSeq)) {
          changeZS01AddrUpdate(entityManager, data.getId().getReqId());
          changeZP01AddrUpdate(entityManager, data.getId().getReqId());
        }
      }
    }

    if (NL_COUNTRIES_LIST.contains(data.getCmrIssuingCntry())) {
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

  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM4", "ADDR_TXT", "CITY1", "STATE_PROV", "POST_CD", "LAND_CNTRY", "DEPT", "PO_BOX",
        "CUST_PHONE"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void doFilterAddresses(List<AddressModel> results) {
    List<AddressModel> addrsToRemove = new ArrayList<AddressModel>();
    for (AddressModel addrModel : results) {
      if (CmrConstants.ADDR_TYPE.ZKVK.toString().equalsIgnoreCase(addrModel.getAddrType())
          || CmrConstants.ADDR_TYPE.ZVAT.toString().equalsIgnoreCase(addrModel.getAddrType())) {
        addrsToRemove.add(addrModel);
      }
    }
    results.removeAll(addrsToRemove);
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

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEconomicCd(), newData.getEconomicCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EconomicCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEconomicCd(), "EconomicCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEconomicCd(), "EconomicCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(newData.getModeOfPayment());
      update.setOldData(oldData.getModeOfPayment());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd2(), newData.getTaxCd2())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxCd2(), "LocalTax2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxCd2(), "LocalTax2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
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
    return Arrays.asList(NL_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {

  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = null;
    String reqType = getReqType(entityManager, reqId);
    if ("ZD01".equals(addrType)) {
      if ("C".equals(reqType)) {
        newAddrSeq = generateShippingAddrSeqNLCreate(entityManager, addrType, reqId);
      } else {
        newAddrSeq = generateShippingAddrSeqNLUpdate(entityManager, addrType, reqId);
      }
    } else if ("ZI01".equals(addrType)) {
      if ("C".equals(reqType)) {
        newAddrSeq = generateZI01AddrSeqCreate(entityManager, addrType, reqId);
      } else {
        newAddrSeq = generateZI01AddrSeqUpdate(entityManager, addrType, reqId);
      }
    } else if ("ZP01".equals(addrType) || "ZS01".equals(addrType) || "ZP02".equals(addrType)) {
      newAddrSeq = generateAddrSeqNL(entityManager, addrType, reqId);
    }
    return newAddrSeq;
  }

  protected String getReqType(EntityManager entityManager, long reqId) {
    String reqType = "";
    String sql = ExternalizedQuery.getSql("ADMIN.GETREQTYPE.SWISS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      reqType = results.get(0);
    }
    return reqType;
  }

  protected String generateShippingAddrSeqNLCreate(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 20800;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "20800");

      if (!(Integer.valueOf(maxAddrSeq) >= 20800 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "20800";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  protected String generateShippingAddrSeqNLUpdate(EntityManager entityManager, String addrType, long reqId) {
    String resultStr = null;
    int result = 0;
    String maxAddrSeqStr = getMaxAddrSeq(entityManager, reqId, "ZD01");
    int maxAddrSeq = !StringUtils.isEmpty(maxAddrSeqStr) ? Integer.valueOf(maxAddrSeqStr) : 0;
    int maxZD01SeqKna1 = 0;
    int maxZD01SeqCmrtaddr = 0;

    String cmrNo = getCmrNo(entityManager, reqId);
    List<Kna1> kna1Records = getKna1Records(entityManager, "788", cmrNo);
    String maxZD01SeqKna1Str = getMaxAddrSeqKna1(kna1Records, "ZD01");
    if (!StringUtils.isEmpty(maxZD01SeqKna1Str)) {
      maxZD01SeqKna1 = Integer.valueOf(maxZD01SeqKna1Str);
    }

    List<CmrtAddr> legacyAddrs = getCmrtaddr(entityManager, "788", cmrNo);
    String maxZD01SeqCmrtaddrStr = getMaxAddrSeqCmrtaddr(legacyAddrs, "ZD01");
    if (!StringUtils.isEmpty(maxZD01SeqCmrtaddrStr)) {
      maxZD01SeqCmrtaddr = Integer.valueOf(maxZD01SeqCmrtaddrStr);
    }

    result = maxAddrSeq;
    resultStr = maxAddrSeqStr;
    if (maxZD01SeqKna1 > result) {
      result = maxZD01SeqKna1;
      resultStr = maxZD01SeqKna1Str;
    }
    if (maxZD01SeqCmrtaddr > result) {
      result = maxZD01SeqCmrtaddr;
      resultStr = maxZD01SeqCmrtaddrStr;
    }

    // get next max seq
    if (!StringUtils.isEmpty(resultStr)) {
      result += 1;
      resultStr = String.valueOf(result);
    } else {
      // in case there is no ZD01 in addr, kna1 and cmrtaddr
      resultStr = "20801";
      result = 20801;
    }

    if (result >= 20801 && result <= 20849) {
      // resultStr is a selectable seq.
      // do nothing
    } else {
      // max seq is not an option,
      // select an un-used seq from 20801 to 20849
      List<String> kna1ZD01SeqList = getKna1AddrSeqList(kna1Records, "ZD01");
      List<String> cmrtaddrZD01SeqList = getCmrtaddrAddrSeqList(legacyAddrs, "ZD01");
      for (int i = 20801; i <= 20849; i++) {
        String temStr = String.valueOf(i);
        if (!kna1ZD01SeqList.contains(temStr) && !cmrtaddrZD01SeqList.contains(temStr) && i > maxAddrSeq) {
          resultStr = temStr;
          return resultStr;
        }
      }
    }
    return resultStr;
  }

  protected String generateZI01AddrSeqCreate(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 20700;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "20700");

      if (!(Integer.valueOf(maxAddrSeq) >= 20700 && Integer.valueOf(maxAddrSeq) <= 20749)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "20700";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  protected String generateZI01AddrSeqUpdate(EntityManager entityManager, String addrType, long reqId) {
    String resultStr = null;
    int result = 0;
    String maxAddrSeqStr = getMaxAddrSeq(entityManager, reqId, "ZI01");
    int maxAddrSeq = !StringUtils.isEmpty(maxAddrSeqStr) ? Integer.valueOf(maxAddrSeqStr) : 0;
    int maxZI01SeqKna1 = 0;
    int maxZI01SeqCmrtaddr = 0;

    String cmrNo = getCmrNo(entityManager, reqId);
    List<Kna1> kna1Records = getKna1Records(entityManager, "788", cmrNo);
    String maxZI01SeqKna1Str = getMaxAddrSeqKna1(kna1Records, "ZI01");
    if (!StringUtils.isEmpty(maxZI01SeqKna1Str)) {
      maxZI01SeqKna1 = Integer.valueOf(maxZI01SeqKna1Str);
    }

    List<CmrtAddr> legacyAddrs = getCmrtaddr(entityManager, "788", cmrNo);
    String maxZI01SeqCmrtaddrStr = getMaxAddrSeqCmrtaddr(legacyAddrs, "ZI01");
    if (!StringUtils.isEmpty(maxZI01SeqCmrtaddrStr)) {
      maxZI01SeqCmrtaddr = Integer.valueOf(maxZI01SeqCmrtaddrStr);
    }

    result = maxAddrSeq;
    resultStr = maxAddrSeqStr;
    if (maxZI01SeqKna1 > result) {
      result = maxZI01SeqKna1;
      resultStr = maxZI01SeqKna1Str;
    }
    if (maxZI01SeqCmrtaddr > result) {
      result = maxZI01SeqCmrtaddr;
      resultStr = maxZI01SeqCmrtaddrStr;
    }

    // get next max seq
    if (!StringUtils.isEmpty(resultStr)) {
      result += 1;
      resultStr = String.valueOf(result);
    } else {
      // in case there is no ZD01 in addr, kna1 and cmrtaddr
      resultStr = "20701";
      result = 20701;
    }

    if (result >= 20701 && result <= 20749) {
      // resultStr is a selectable seq.
      // do nothing
    } else {
      // max seq is not an option,
      // select an un-used seq from 207001 to 20749
      List<String> kna1ZI01SeqList = getKna1AddrSeqList(kna1Records, "ZI01");
      List<String> cmrtaddrZI01SeqList = getCmrtaddrAddrSeqList(legacyAddrs, "ZI01");
      for (int i = 20701; i <= 20749; i++) {
        String temStr = String.valueOf(i);
        if (!kna1ZI01SeqList.contains(temStr) && !cmrtaddrZI01SeqList.contains(temStr) && i > maxAddrSeq) {
          resultStr = temStr;
          return resultStr;
        }
      }
    }
    return resultStr;
  }

  protected String generateAddrSeqNL(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 1;
    String newAddrSeq = null;
    if (addrType.equals("ZP01")) {
      addrSeq = 29901;
    }
    if (addrType.equals("ZS01")) {
      addrSeq = 00001;
    }
    if (addrType.equals("ZP02")) {
      addrSeq = 28801;
    }
    try {
      addrSeq = Integer.parseInt(newAddrSeq);
    } catch (Exception e) {
      // if returned value is invalid
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
  }

  private String getMaxAddrSeq(EntityManager entityManager, long reqId, String addrType) {
    String maxAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "");
    }
    return maxAddrSeq;
  }

  private String getCmrNo(EntityManager entityManager, long reqId) {
    String cmrNo = "";
    String sql = ExternalizedQuery.getSql("DATA.GETCMRNO.SWISS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);

    List<String> results = query.getResults(String.class);
    if (results != null && results.size() > 0) {
      cmrNo = results.get(0);
    }
    return cmrNo;
  }

  private List<Kna1> getKna1Records(EntityManager entityManager, String country, String cmrNo) {
    List<Kna1> addresses = null;
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("QUERY.GET.CMR.ME");
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

  private String getMaxAddrSeqCmrtaddr(List<CmrtAddr> cmrtaddrRecords, String addrType) {
    String maxSeqStr = null;
    String seq = null;
    int maxSeq = 0;

    for (CmrtAddr record : cmrtaddrRecords) {
      switch (addrType) {
      case "ZI01":
        if ("Y".equals(record.getIsAddrUseEPL())) {
          seq = record.getId().getAddrNo() != null ? record.getId().getAddrNo() : "";
          if (!StringUtils.isEmpty(seq) && Integer.valueOf(seq) > maxSeq) {
            maxSeq = Integer.valueOf(seq);
          }
        }
        break;
      case "ZD01":
        if ("Y".equals(record.getIsAddrUseShipping())) {
          seq = record.getId().getAddrNo() != null ? record.getId().getAddrNo() : "";
          if (!StringUtils.isEmpty(seq) && Integer.valueOf(seq) > maxSeq) {
            maxSeq = Integer.valueOf(seq);
          }
        }
        break;
      }

    }
    maxSeqStr = String.valueOf(maxSeq);
    return maxSeqStr;
  }

  private String getMaxAddrSeqKna1(List<Kna1> kna1Records, String addrType) {
    String maxSeqStr = null;
    String seq = null;
    int maxSeq = 0;

    for (Kna1 record : kna1Records) {
      if (addrType != null && addrType.equals(record.getKtokd())) {
        seq = record.getZzkvSeqno();
        if (seq != null && Integer.valueOf(seq) > maxSeq) {
          maxSeq = Integer.valueOf(seq);
        }
      }
    }

    return maxSeqStr;
  }

  private List<String> getKna1AddrSeqList(List<Kna1> kna1Records, String addrType) {
    List<String> list = new ArrayList<String>();
    String seq = null;
    for (Kna1 record : kna1Records) {
      if (addrType != null && addrType.equals(record.getKtokd())) {
        seq = record.getZzkvSeqno();
        list.add(seq);
      }
    }
    return list;
  }

  private List<String> getCmrtaddrAddrSeqList(List<CmrtAddr> cmrtaddrRecords, String addrType) {
    List<String> list = new ArrayList<String>();
    String seq = null;
    for (CmrtAddr record : cmrtaddrRecords) {
      switch (addrType) {
      case "ZI01":
        if ("Y".equals(record.getIsAddrUseEPL())) {
          seq = record.getId().getAddrNo() != null ? record.getId().getAddrNo() : "";
          if (!StringUtils.isEmpty(seq)) {
            list.add(seq);
          }
        }
        break;
      case "ZD01":
        if ("Y".equals(record.getIsAddrUseShipping())) {
          seq = record.getId().getAddrNo() != null ? record.getId().getAddrNo() : "";
          if (!StringUtils.isEmpty(seq)) {
            list.add(seq);
          }
        }
        break;
      }
    }
    return list;
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return RETRIEVE_INVALID_CUSTOMERS;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorID", "originatorId");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##INACCode", "inacCd");
    map.put("##BPRelationType", "bpRelType");
    map.put("##VAT", "vat");
    map.put("##SitePartyID", "sitePartyId");
    map.put("##RequesterID", "requesterId");
    map.put("##Department", "dept");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##BuyingGroupID", "bgId");
    map.put("##LandedCountry", "landCntry");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##RequestReason", "reqReason");
    map.put("##SAPNumber", "sapNo");
    map.put("##CMRNumber", "cmrNo");
    map.put("##CAP", "capInd");
    map.put("##DUNS", "dunsNo");
    map.put("##CoverageID", "covId");
    map.put("##SalesBusOff", "salesBusOffCd");
    map.put("##MembLevel", "memLvl");
    map.put("##InternalDept", "ibmDeptCostCenter");
    map.put("##RequestType", "reqType");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##EngineeringBo", "engineeringBo");
    map.put("##POBox", "poBox");
    map.put("##Expedite", "expediteInd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##CustomerName2", "custNm2");
    map.put("##LocalTax2", "taxCd2");
    map.put("##CustomerName1", "custNm1");
    map.put("##PostalCode", "postCd");
    map.put("##LocalTax1", "taxCd1");
    map.put("##CustomerName4", "custNm4");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##EconomicCd2", "economicCd");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##ISIC", "isicCd");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##StateProv", "stateProv");
    map.put("##City1", "city1");
    map.put("##CustLang", "custPrefLang");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##ISU", "isuCd");
    map.put("##ClientTier", "clientTier");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##SOENumber", "soeReqNo");
    map.put("##OriginatorName", "originatorNm");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##CollectionCd", "collectionCd");
    map.put("##CustPhone", "custPhone");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##VATExempt", "vatExempt");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##Enterprise", "enterprise");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##GlobalBuyingGroupID", "gbgId");
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

  private Addr getCurrentInstallingAddress(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("CEE.GETINSTALLING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getSingleResult(Addr.class);
  }

  private void copyAddrData(FindCMRRecordModel record, Addr addr, String seq) {
    record.setCmrAddrTypeCode("ZP02");
    record.setCmrAddrSeq(seq);
    record.setCmrName1Plain(addr.getCustNm1());
    record.setCmrName2Plain(addr.getCustNm2());
    record.setCmrName3(addr.getCustNm3());
    record.setCmrName4(addr.getCustNm4());
    record.setCmrStreetAddress(addr.getAddrTxt());
    record.setCmrCity(addr.getCity1());
    record.setCmrCity2(addr.getCity2());
    record.setCmrState(addr.getStateProv());
    record.setCmrCountryLanded(addr.getLandCntry());
    record.setCmrCountry(addr.getLandCntry());
    record.setCmrPOBox(addr.getPoBox());
    record.setCmrPostalCode(addr.getPostCd());
    record.setParentCMRNo(addr.getParCmrNo());
  }

  private CmrtAddr getLegacyGAddress(EntityManager entityManager, String rcyaa, String cmrNo) {
    String sql = ExternalizedQuery.getSql("CEE.GETLEGACYGADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("RCYAA", rcyaa);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(true);
    return query.getSingleResult(CmrtAddr.class);
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

  private boolean dupCMRExists(String katr6, String cmrNo) throws Exception {

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("CEE.CHECKDUPCMR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + katr6 + "'");
    sql = StringUtils.replace(sql, ":CMRNO", "'" + cmrNo + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("MANDT");
    query.addField("KATR6");
    query.addField("CMRNO");

    LOG.debug("Check Dup CMR .. Getting existing SPRAS value from RDc DB.." + "KATR6 =" + katr6);
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newSeq = null;
    String reqType = getReqType(entityManager, reqId);
    if ("ZS01".equals(addrType)) {
      newSeq = "00001";
    }
    if ("ZI01".equals(addrType)) {
      if ("C".equals(reqType)) {
        newSeq = generateZI01AddrSeqCreate(entityManager, addrType, reqId);
      } else {
        newSeq = generateZI01AddrSeqUpdate(entityManager, addrType, reqId);
      }
    }
    if ("ZP01".equals(addrType)) {
      newSeq = "29901";
    }
    if ("ZP02".equals(addrType)) {
      newSeq = "28801";
    }
    if ("ZD01".equals(addrType)) {
      if ("C".equals(reqType)) {
        newSeq = generateShippingAddrSeqNLCreate(entityManager, addrType, reqId);
      } else {
        newSeq = generateShippingAddrSeqNLUpdate(entityManager, addrType, reqId);
      }
    }
    return newSeq;
  }

  // NL(788) Shipping addr seq logic should work while copying of address
  protected String generateShippingAddrSeqNLCopy(EntityManager entityManager, String addrType, long reqId) {
    int addrSeq = 20800;
    String maxAddrSeq = null;
    String newAddrSeq = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETADDRSEQ");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);

    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      maxAddrSeq = (String) (result != null && result.length > 0 && result[0] != null ? result[0] : "20800");

      if (!(Integer.valueOf(maxAddrSeq) >= 20800 && Integer.valueOf(maxAddrSeq) <= 20849)) {
        maxAddrSeq = "";
      }
      if (StringUtils.isEmpty(maxAddrSeq)) {
        maxAddrSeq = "20800";
      }
      try {
        addrSeq = Integer.parseInt(maxAddrSeq);
      } catch (Exception e) {
        // if returned value is invalid
      }
      addrSeq++;
    }
    newAddrSeq = Integer.toString(addrSeq);
    return newAddrSeq;
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
    if (NL_COUNTRIES_LIST.contains(country)) {
      countryAddrss = NL_MASS_UPDATE_SHEET_NAMES;

      XSSFSheet sheet = book.getSheet("Data");// validate Data sheet
      row = sheet.getRow(0);// data field name row
      int ordBlkIndex = 15;// default index
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
        if (StringUtils.isNotBlank(ordBlk) && !("@".equals(ordBlk) || "E".equals(ordBlk) || "P".equals(ordBlk) || "5".equals(ordBlk)
            || "6".equals(ordBlk) || "7".equals(ordBlk) || "J".equals(ordBlk))) {
          LOG.trace("Order Block Code should only @, E, P, 5, 6, 7, J. >> ");
          error.addError(rowIndex, "Order Block Code", "Order Block Code should be only @, E, P, 5, 6, 7, J. ");
        }

        currCell = row.getCell(cmrNoIndex);
        cmrNo = validateColValFromCell(currCell);
        String isuCd = ""; // 6
        String ctc = ""; // 7
        currCell = row.getCell(6);
        isuCd = validateColValFromCell(currCell);
        currCell = row.getCell(7);
        ctc = validateColValFromCell(currCell);

        if (isDivCMR(cmrNo)) {
          LOG.trace("The row " + (row.getRowNum() + 1) + ":Note the CMR number is a divestiture CMR records.");
          error.addError((row.getRowNum() + 1), "CMR No.",
              "The row " + (row.getRowNum() + 1) + ":Note the CMR number is a divestiture CMR records.<br>");
        }
        if (is93CMR(cmrNo)) {
          LOG.trace("The row " + (row.getRowNum() + 1) + ":Note the CMR number is a deleted record in RDC.");
          error.addError((row.getRowNum() + 1), "CMR No.",
              "The row " + (row.getRowNum() + 1) + ":Note the CMR number is a deleted record in RDC.<br>");
        }

        if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
      	  if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(ctc))
                    || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isuCd))) {
                  LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that both ISU and CTC value needs to be filled..");
                  error.addError((row.getRowNum() + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
                } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
                  if (StringUtils.isBlank(ctc) || !"Q".equals(ctc)) {
            LOG.trace("The row " + (row.getRowNum() + 1)
                + ":Client Tier should be 'Q' for the selected ISU code.");
            error.addError((row.getRowNum() + 1), "Client Tier",
                ":Client Tier should be 'Q' for the selected ISU code:" + isuCd + ".<br>");
          }
        }
        else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
            if (StringUtils.isBlank(ctc) || !"Y".contains(ctc)) {
              LOG.trace("The row " + (row.getRowNum() + 1)
                  + ":Client Tier should be 'Y' for the selected ISU code.");
              error.addError((row.getRowNum() + 1), "Client Tier",
                  ":Client Tier should be 'Y' for the selected ISU code:" + isuCd + ".<br>");
            }
          }
        else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
            if (StringUtils.isBlank(ctc) || !"T".contains(ctc)) {
              LOG.trace("The row " + (row.getRowNum() + 1)
                  + ":Client Tier should be 'T' for the selected ISU code.");
              error.addError((row.getRowNum() + 1), "Client Tier",
                  ":Client Tier should be 'T' for the selected ISU code:" + isuCd + ".<br>");
            }
          }
        else if ((!StringUtils.isBlank(isuCd) && !Arrays.asList("32", "34", "36").contains(isuCd)) && !"@".equalsIgnoreCase(ctc)) {
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
        TemplateValidation errorAddr = new TemplateValidation(name);
        for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {

          row = sheet.getRow(rowIndex);
          if (row == null) {
            break; // stop immediately when row is blank
          }
          String custName1 = ""; // 2
          String name3 = ""; // 4
          String attPerson = ""; // 5
          String street = ""; // 6
          String pobox = ""; // 7
          String city = ""; // 9
          int addrFldCnt1 = 0;

          currCell = row.getCell(2);
          custName1 = validateColValFromCell(currCell);
          currCell = row.getCell(4);
          name3 = validateColValFromCell(currCell);
          currCell = row.getCell(5);
          attPerson = validateColValFromCell(currCell);
          currCell = row.getCell(6);
          street = validateColValFromCell(currCell);
          currCell = row.getCell(7);
          pobox = validateColValFromCell(currCell);
          currCell = row.getCell(9);
          city = validateColValFromCell(currCell);

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
            LOG.trace("Customer Name (3) and PO BOX should not be input at the sametime.");
            errorAddr.addError(row.getRowNum(), "PO BOX", "Customer Name 3, Attention person and PO Box - only 1 out of 3 can be filled.");
          }

          if (StringUtils.isBlank(custName1)) {
            LOG.trace("Customer Name 1 is required. ");
            errorAddr.addError(row.getRowNum(), "Customer Name 1", "Customer Name 1 is required. ");
          }
          if (StringUtils.isBlank(street)) {
            LOG.trace("Street Address is required. ");
            errorAddr.addError(row.getRowNum(), "Street Address", "Street Address is required. ");
          }
          if (StringUtils.isBlank(city)) {
            LOG.trace("City is required. ");
            errorAddr.addError(row.getRowNum(), "City", "City is required. ");
          }

        }
        if (errorAddr.hasErrors()) {
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
    query.setParameter("KATR6", "788");
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
    query.setParameter("CNTRY", "788");
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

  private void changeZP02AddrNew(EntityManager entityManager, long reqId) {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("CEE.ADDR.CHANGE.ZP02NEW"));
    query.setParameter("REQ_ID", reqId);
    query.executeSql();
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

  // CMR-6019
  private boolean checkMEDupCMRExist(String cntry, String cmrNo) {
    EntityManager entityManager = JpaManager.getEntityManager();
    String CEBO = cntry + "0000";
    String sql = ExternalizedQuery.getSql("QUERY.CHECK.ME.DUP.EXIST.DB2");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMRNO", cmrNo);
    List<Object[]> results = query.getResults();
    if (results != null && results.size() > 0) {
      Object[] result = results.get(0);
      String dupCEBO = result[0].toString();
      if (CEBO.equals(dupCEBO)) {
        return true;
      }
    }
    return false;
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

  private void upperChar(Addr addr) {
    Map<String, String> addressDataMap = new HashMap<String, String>();
    addressDataMap.put("addrTxt", addr.getAddrTxt());
    addressDataMap.put("addrTxt2", addr.getAddrTxt2());
    addressDataMap.put("bldg", addr.getBldg());
    addressDataMap.put("city1", addr.getCity1());
    addressDataMap.put("city2", addr.getCity2());
    addressDataMap.put("county", addr.getCounty());
    addressDataMap.put("countyName", addr.getCountyName());
    addressDataMap.put("custNm1", addr.getCustNm1());
    addressDataMap.put("custNm2", addr.getCustNm2());
    addressDataMap.put("custNm3", addr.getCustNm3());
    addressDataMap.put("custNm4", addr.getCustNm4());
    addressDataMap.put("dept", addr.getDept());
    addressDataMap.put("division", addr.getDivn());
    addressDataMap.put("floor", addr.getFloor());
    addressDataMap.put("office", addr.getOffice());
    addressDataMap.put("poBox", addr.getPoBox());
    addressDataMap.put("poBoxCity", addr.getPoBoxCity());
    addressDataMap.put("poBoxPostCd", addr.getPoBoxPostCd());
    addressDataMap.put("postCd", addr.getPostCd());
    addressDataMap.put("stateProv", addr.getStateProv());
    addressDataMap.put("stdCityNm", addr.getStdCityNm());
    addressDataMap.put("taxOffice", addr.getTaxOffice());
    for (String key : addressDataMap.keySet()) {
      if (StringUtils.isNotEmpty(addressDataMap.get(key))) {
        addressDataMap.put(key, addressDataMap.get(key).toUpperCase());
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("addrTxt"))) && !(addressDataMap.get("addrTxt").equals(addr.getAddrTxt()))) {
        addr.setAddrTxt(addressDataMap.get("addrTxt"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("addrTxt2"))) && !(addressDataMap.get("addrTxt2").equals(addr.getAddrTxt2()))) {
        addr.setAddrTxt2(addressDataMap.get("addrTxt2"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("bldg"))) && !(addressDataMap.get("bldg").equals(addr.getBldg()))) {
        addr.setBldg(addressDataMap.get("bldg"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("city1"))) && !(addressDataMap.get("city1").equals(addr.getCity1()))) {
        addr.setCity1(addressDataMap.get("city1"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("city2"))) && !(addressDataMap.get("city2").equals(addr.getCity2()))) {
        addr.setCity2(addressDataMap.get("city2"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("county"))) && !(addressDataMap.get("county").equals(addr.getCounty()))) {
        addr.setCounty(addressDataMap.get("county"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("countyName"))) && !(addressDataMap.get("countyName").equals(addr.getCountyName()))) {
        addr.setCountyName(addressDataMap.get("countyName"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm1"))) && !(addressDataMap.get("custNm1").equals(addr.getCustNm1()))) {
        addr.setCustNm1(addressDataMap.get("custNm1"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm2"))) && !(addressDataMap.get("custNm2").equals(addr.getCustNm2()))) {
        addr.setCustNm2(addressDataMap.get("custNm2"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm3"))) && !(addressDataMap.get("custNm3").equals(addr.getCustNm3()))) {
        addr.setCustNm3(addressDataMap.get("custNm3"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("custNm4"))) && !(addressDataMap.get("custNm4").equals(addr.getCustNm4()))) {
        addr.setCustNm4(addressDataMap.get("custNm4"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("dept"))) && !(addressDataMap.get("dept").equals(addr.getDept()))) {
        addr.setDept(addressDataMap.get("dept"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("division"))) && !(addressDataMap.get("division").equals(addr.getDivn()))) {
        addr.setDept(addressDataMap.get("division"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("floor"))) && !(addressDataMap.get("floor").equals(addr.getFloor()))) {
        addr.setFloor(addressDataMap.get("floor"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("office"))) && !(addressDataMap.get("office").equals(addr.getOffice()))) {
        addr.setOffice(addressDataMap.get("office"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("poBox"))) && !(addressDataMap.get("poBox").equals(addr.getPoBox()))) {
        addr.setPoBox(addressDataMap.get("poBox"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("poBoxCity"))) && !(addressDataMap.get("poBoxCity").equals(addr.getPoBoxCity()))) {
        addr.setPoBoxCity(addressDataMap.get("poBoxCity"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("poBoxPostCd"))) && !(addressDataMap.get("poBoxPostCd").equals(addr.getPoBoxPostCd()))) {
        addr.setPoBoxPostCd(addressDataMap.get("poBoxPostCd"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("postCd"))) && !(addressDataMap.get("postCd").equals(addr.getPostCd()))) {
        addr.setPostCd(addressDataMap.get("postCd"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("stateProv"))) && !(addressDataMap.get("stateProv").equals(addr.getStateProv()))) {
        addr.setStateProv(addressDataMap.get("stateProv"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("stdCityNm"))) && !(addressDataMap.get("stdCityNm").equals(addr.getStdCityNm()))) {
        addr.setStdCityNm(addressDataMap.get("stdCityNm"));
      }
      if (!(StringUtils.isEmpty(addressDataMap.get("taxOffice"))) && !(addressDataMap.get("taxOffice").equals(addr.getTaxOffice()))) {
        addr.setTaxOffice(addressDataMap.get("taxOffice"));
      }
    }
  }

  @Override
  public boolean setAddrSeqByImport(AddrPK addrPk, EntityManager entityManager, FindCMRResultModel result) {
    return true;
  }
}
