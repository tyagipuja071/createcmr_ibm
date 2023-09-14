/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
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
import com.ibm.cio.cmr.request.entity.CmrCloningQueue;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.KunnrExt;
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
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.CloningRDCDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Handler for CEMEA
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CEMEAHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(CEMEAHandler.class);
  private static final boolean RETRIEVE_INVALID_CUSTOMERS = true;

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.ABU_DHABI, "AE");
    LANDED_CNTRY_MAP.put(SystemLocation.ALBANIA, "AL");
    LANDED_CNTRY_MAP.put(SystemLocation.ARMENIA, "AM");
    LANDED_CNTRY_MAP.put(SystemLocation.AUSTRIA, "AT");
    LANDED_CNTRY_MAP.put(SystemLocation.AZERBAIJAN, "AZ");
    LANDED_CNTRY_MAP.put(SystemLocation.BAHRAIN, "BH");
    LANDED_CNTRY_MAP.put(SystemLocation.BELARUS, "BY");
    LANDED_CNTRY_MAP.put(SystemLocation.BOSNIA_AND_HERZEGOVINA, "BA");
    LANDED_CNTRY_MAP.put(SystemLocation.BULGARIA, "BG");
    LANDED_CNTRY_MAP.put(SystemLocation.CROATIA, "HR");
    LANDED_CNTRY_MAP.put(SystemLocation.CZECH_REPUBLIC, "CZ");
    LANDED_CNTRY_MAP.put(SystemLocation.EGYPT, "EG");
    LANDED_CNTRY_MAP.put(SystemLocation.GEORGIA, "GE");
    LANDED_CNTRY_MAP.put(SystemLocation.HUNGARY, "HU");
    LANDED_CNTRY_MAP.put(SystemLocation.IRAQ, "IQ");
    LANDED_CNTRY_MAP.put(SystemLocation.JORDAN, "JO");
    LANDED_CNTRY_MAP.put(SystemLocation.KAZAKHSTAN, "KZ");
    LANDED_CNTRY_MAP.put(SystemLocation.KUWAIT, "KW");
    LANDED_CNTRY_MAP.put(SystemLocation.KYRGYZSTAN, "KG");
    LANDED_CNTRY_MAP.put(SystemLocation.LEBANON, "LB");
    LANDED_CNTRY_MAP.put(SystemLocation.LIBYA, "LY");
    LANDED_CNTRY_MAP.put(SystemLocation.MACEDONIA, "MK");
    LANDED_CNTRY_MAP.put(SystemLocation.MOLDOVA, "MD");
    LANDED_CNTRY_MAP.put(SystemLocation.MONTENEGRO, "ME");
    LANDED_CNTRY_MAP.put(SystemLocation.MOROCCO, "MA");
    LANDED_CNTRY_MAP.put(SystemLocation.OMAN, "OM");
    LANDED_CNTRY_MAP.put(SystemLocation.PAKISTAN, "PK");
    LANDED_CNTRY_MAP.put(SystemLocation.POLAND, "PL");
    LANDED_CNTRY_MAP.put(SystemLocation.QATAR, "QA");
    LANDED_CNTRY_MAP.put(SystemLocation.ROMANIA, "RO");
    LANDED_CNTRY_MAP.put(SystemLocation.RUSSIAN_FEDERATION, "RU");
    LANDED_CNTRY_MAP.put(SystemLocation.SAUDI_ARABIA, "SA");
    LANDED_CNTRY_MAP.put(SystemLocation.SERBIA, "CS");
    LANDED_CNTRY_MAP.put(SystemLocation.SLOVAKIA, "SK");
    LANDED_CNTRY_MAP.put(SystemLocation.SLOVENIA, "SI");
    LANDED_CNTRY_MAP.put(SystemLocation.SYRIAN_ARAB_REPUBLIC, "SY");
    LANDED_CNTRY_MAP.put(SystemLocation.TAJIKISTAN, "TJ");
    LANDED_CNTRY_MAP.put(SystemLocation.TURKMENISTAN, "TM");
    LANDED_CNTRY_MAP.put(SystemLocation.UKRAINE, "UA");
    LANDED_CNTRY_MAP.put(SystemLocation.UNITED_ARAB_EMIRATES, "AE");
    LANDED_CNTRY_MAP.put(SystemLocation.UZBEKISTAN, "UZ");
    LANDED_CNTRY_MAP.put(SystemLocation.YEMEN, "YE");
  }

  public static final List<String> CEMEA_CHECKLIST = Arrays.asList("358", "359", "363", "603", "607", "620", "626", "651", "675", "677", "680", "694",
      "695", "699", "705", "707", "713", "741", "752", "762", "767", "768", "772", "787", "805", "808", "821", "823", "832", "849", "850", "865",
      "889");
  private static final List<String> CEE_COUNTRY_LIST = Arrays.asList(SystemLocation.SLOVAKIA, SystemLocation.KYRGYZSTAN, SystemLocation.SERBIA,
      SystemLocation.ARMENIA, SystemLocation.AZERBAIJAN, SystemLocation.TURKMENISTAN, SystemLocation.TAJIKISTAN, SystemLocation.ALBANIA,
      SystemLocation.BELARUS, SystemLocation.BULGARIA, SystemLocation.GEORGIA, SystemLocation.KAZAKHSTAN, SystemLocation.BOSNIA_AND_HERZEGOVINA,
      SystemLocation.MACEDONIA, SystemLocation.SLOVENIA, SystemLocation.HUNGARY, SystemLocation.UZBEKISTAN, SystemLocation.MOLDOVA,
      SystemLocation.POLAND, SystemLocation.RUSSIAN_FEDERATION, SystemLocation.ROMANIA, SystemLocation.UKRAINE, SystemLocation.CROATIA,
      SystemLocation.CZECH_REPUBLIC);

  private static final List<String> ME_COUNTRY_LIST = Arrays.asList(SystemLocation.BAHRAIN, SystemLocation.MOROCCO, SystemLocation.GULF,
      SystemLocation.UNITED_ARAB_EMIRATES, SystemLocation.ABU_DHABI, SystemLocation.IRAQ, SystemLocation.JORDAN, SystemLocation.KUWAIT,
      SystemLocation.LEBANON, SystemLocation.LIBYA, SystemLocation.OMAN, SystemLocation.PAKISTAN, SystemLocation.QATAR, SystemLocation.SAUDI_ARABIA,
      SystemLocation.YEMEN, SystemLocation.SYRIAN_ARAB_REPUBLIC, SystemLocation.EGYPT);

  private static final String[] CEEME_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CustLang", "GeoLocationCode", "Affiliate", "Company", "CAP", "CMROwner",
      "CustClassCode", "LocalTax2", "SearchTerm", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office",
      "Floor", "Building", "County", "City2", "Department" };

  private static final String[] CZ_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "CustLang", "GeoLocationCode", "Affiliate", "CAP", "CMROwner", "CustClassCode",
      "LocalTax2", "SearchTerm", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor", "Building",
      "County", "City2", "Department", "Company", "LocalTax1" };

  private static final String[] AUSTRIA_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "GeoLocationCode", "Affiliate", "Company", "CAP", "CMROwner",
      "CustClassCode", "CurrencyCode", "LocalTax2", "SearchTerm", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX",
      "TransportZone", "Office", "Floor", "Building", "County", "City2", "Department", "BPRelationType", "MembLevel" };

  public static final List<String> CEMEA_POSTAL_FORMAT = Arrays.asList("603", "607", "644", "651", "740", "705", "708", "626", "694", "695", "826",
      "821", "363", "359", "741", "699", "704", "707", "707", "889", "668", "693", "787", "820", "358");

  private static final List<String> CIS_DUPLICATE_COUNTRIES = Arrays.asList("607", "358", "626", "651", "694", "695", "787", "363", "359", "889",
      "741");

  private static final List<String> CEE_COUNTRIES_LIST = Arrays.asList("358", "359", "363", "603", "607", "626", "644", "651", "668", "693", "694",
      "695", "699", "704", "705", "707", "708", "740", "741", "787", "820", "821", "826", "889");

  protected static final String[] CEE_MASS_UPDATE_SHEET_NAMES = { "Address in Local language", "Sold To", "Mail to", "Bill To", "Ship To",
      "Install At" };

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
        record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
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
      List<FindCMRRecordModel> recordsFromSearch = source.getItems();
      List<FindCMRRecordModel> filteredRecords = new ArrayList<>();

      if (recordsFromSearch != null && !recordsFromSearch.isEmpty() && recordsFromSearch.size() > 0 && "618".equals(reqEntry.getCmrIssuingCntry())) {
        doFilterAddresses(reqEntry, recordsFromSearch, filteredRecords);
        if (!filteredRecords.isEmpty() && filteredRecords.size() > 0 && filteredRecords != null) {
          source.setItems(filteredRecords);
        }
      }
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
            String soldtoseqinrdc = getSoldtoSeqFromRDC(entityManager, SystemConfiguration.getValue("MANDT"), reqEntry.getCmrIssuingCntry(),
                record.getCmrNum());
            System.out.println("soldtoseqinrdc = " + soldtoseqinrdc);
            System.out.println("seqNo = " + seqNo);
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
              addrType = record.getCmrAddrTypeCode();
              if (!StringUtils.isEmpty(addrType)) {
                addr = cloneAddress(record, addrType);
                addr.setCmrDept(record.getCmrCity2());
                addr.setCmrName4(record.getCmrName4());
                if (CEE_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                    && (CmrConstants.ADDR_TYPE.ZD01.toString().equals(addr.getCmrAddrTypeCode())) && "598".equals(addr.getCmrAddrSeq())) {
                  addr.setCmrAddrTypeCode("ZD02");
                }
                if (CEE_COUNTRIES_LIST.contains(reqEntry.getCmrIssuingCntry())
                    && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(addr.getCmrAddrTypeCode())) && "599".equals(addr.getCmrAddrSeq())) {
                  addr.setCmrAddrTypeCode("ZP03");
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
              if (CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode()) && record.getCmrAddrSeq().equals(soldtoseqinrdc)) {
                String kunnr = addr.getCmrSapNumber();
                String adrnr = getaddAddressAdrnr(entityManager, cmrIssueCd, SystemConfiguration.getValue("MANDT"), kunnr, addr.getCmrAddrTypeCode(),
                    addr.getCmrAddrSeq());
                int maxintSeq = getMaxSequenceOnAddr(entityManager, SystemConfiguration.getValue("MANDT"), reqEntry.getCmrIssuingCntry(),
                    record.getCmrNum());
                int maxintSeqLegacy = getMaxSequenceOnLegacyAddr(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                String maxSeq = StringUtils.leftPad(String.valueOf(maxintSeqLegacy), 5, '0');
                String legacyGaddrSeq = getGaddressSeqFromLegacy(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                String legacyGaddrLN6 = getGaddressAddLN6FromLegacy(entityManager, reqEntry.getCmrIssuingCntry(), record.getCmrNum());
                String gAddrSeq = "";
                if (!StringUtils.isEmpty(legacyGaddrSeq)) {
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
                    if (gAddr != null) {
                      LOG.debug("Adding installing to the records");
                      FindCMRRecordModel installing = new FindCMRRecordModel();
                      PropertyUtils.copyProperties(installing, mainRecord);
                      // copyAddrData(installing, installingAddr, gAddrSeq);
                      installing.setCmrAddrTypeCode("ZP02");
                      installing.setCmrAddrSeq(gAddrSeq);
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
                } else {
                  CmrtAddr gAddr = getLegacyGAddress(entityManager, reqEntry.getCmrIssuingCntry(), searchModel.getCmrNum());
                  if (gAddr != null) {
                    LOG.debug("Adding installing to the records");
                    FindCMRRecordModel installing = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(installing, mainRecord);
                    // copyAddrData(installing, installingAddr, gAddrSeq);
                    installing.setCmrAddrTypeCode("ZP02");
                    installing.setCmrAddrSeq(gAddrSeq);
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

            if ("618".equals(reqEntry.getCmrIssuingCntry()) && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(record.getCmrAddrTypeCode()))
                && "599".equals(record.getCmrAddrSeq())) {
              record.setCmrAddrTypeCode("ZP02");
            }

            if ("618".equals(reqEntry.getCmrIssuingCntry()) && (CmrConstants.ADDR_TYPE.ZP01.toString().equals(record.getCmrAddrTypeCode()))
                && StringUtils.isNotEmpty(record.getExtWalletId())) {
              record.setCmrAddrTypeCode("PG01");
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

    if (SystemLocation.AUSTRIA.equals(cmrIssuingCntry)) {
      handleAustriaAddress(entityManager, cmrIssuingCntry, address, addressKey);
    } else {

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

      if (CEMEA_POSTAL_FORMAT.contains(cmrIssuingCntry)) {
        handleCityAndPostCodeCMEA(line5, cmrIssuingCntry, address, addressKey);
      } else {
        handleCityAndPostCode(line5, cmrIssuingCntry, address, addressKey);
      }
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

    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      String code = LANDED_CNTRY_MAP.get(cmrIssuingCntry);
      address.setCmrCountryLanded(code);
    }

    // Story 1733554: Morocco: new mandatory ICE field
    if (SystemLocation.MOROCCO.equals(cmrIssuingCntry) && "ZP01".equalsIgnoreCase(address.getCmrAddrTypeCode())) {
      address.setCmrDept(this.currentImportValues.get("ICE"));
    }

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

  /**
   * Handles Austria specific addresses
   * 
   * @param entityManager
   * @param cmrIssuingCntry
   * @param address
   * @param addressKey
   */
  private void handleAustriaAddress(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    // Domestic and Cross-border - Austria
    // line2 = Name2
    // line3 = Name3 or (ATT + phone)
    // line4 = Street or (Street + PO Box)
    // line5 = Postal Code for domestic
    // line6 = City for domestic, Landed Country CODE + City for cross
    // border

    String line1 = getCurrentValue(addressKey, "Address1");
    String line2 = getCurrentValue(addressKey, "Address2");
    String line3 = getCurrentValue(addressKey, "Address3");
    String line4 = getCurrentValue(addressKey, "Address4");
    String line5 = getCurrentValue(addressKey, "Address5");
    String line6 = getCurrentValue(addressKey, "Address6");

    address.setCmrName1Plain(line1);
    address.setCmrName2Plain(line2);
    address.setCmrName3(line3);

    // if ("ZS01".equals(address.getCmrAddrTypeCode())) {
    // address.setCmrAddrSeq("00001");
    // } else if ("ZD01".equals(address.getCmrAddrTypeCode())) {
    // address.setCmrAddrSeq("00002");
    // } else if ("ZI01".equals(address.getCmrAddrTypeCode())) {
    // address.setCmrAddrSeq("00003");
    // } else if ("ZP01".equals(address.getCmrAddrTypeCode())) {
    // address.setCmrAddrSeq("00004");
    // } else if ("ZS02".equals(address.getCmrAddrTypeCode())) {
    // address.setCmrAddrSeq("00005");
    // }

    String[] parts = null;
    if (isPOBox(line4)) {
      if (line4.contains(",")) {
        parts = line4.split(",");
        for (String part : parts) {
          if (isPOBox(part)) {
            address.setCmrPOBox(part);
          } else {
            address.setCmrStreetAddress(part);
          }
        }
      } else {
        address.setCmrPOBox(line4);
      }
    } else {
      address.setCmrStreetAddress(line4);
    }

    if (!StringUtils.isEmpty(line5) && line5.matches("[0-9]*")) {
      // numeric, postal code
      address.setCmrPostalCode(line5);
    } else {
      handleCityAndPostCodeCMEA(line5, cmrIssuingCntry, address, addressKey);
    }

    String excessValue = null;
    // line6 = city OR code-city format
    if (!StringUtils.isEmpty(line6)) {
      // check CODE-City format
      if (line6.contains("-")) {
        String code = line6.substring(0, line6.indexOf("-")).trim();
        if (!StringUtils.isEmpty(code) && code.length() == 2) {
          excessValue = line6.substring(line6.indexOf("-") + 1).trim();
          address.setCmrCountryLanded(code);
        } else {
          // not a country code
          address.setCmrCity(line6);
        }
      } else {
        // then it's city
        address.setCmrCity(line6);
      }
    }

    if (!StringUtils.isEmpty(excessValue)) {
      // where do we put this? try empty fields
      if (StringUtils.isEmpty(address.getCmrCity())) {
        address.setCmrCity(excessValue);
      } else if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
        address.setCmrStreetAddress(excessValue);
      } else if (StringUtils.isEmpty(address.getCmrName4())) {
        address.setCmrName4(excessValue);
      } else if (StringUtils.isEmpty(address.getCmrName3())) {
        address.setCmrName3(excessValue);
      } else if (StringUtils.isEmpty(address.getCmrName2())) {
        address.setCmrName2(excessValue);
      }
    }

    // do a final check of the mandatory fields

    if (StringUtils.isEmpty(address.getCmrCity())) {
      // try to get it from the lowest line
      if (!StringUtils.isEmpty(address.getCmrStreetAddress())) {
        address.setCmrCity(address.getCmrStreetAddress());
        address.setCmrStreetAddress(null);
      } else if (!StringUtils.isEmpty(address.getCmrName4())) {
        address.setCmrCity(address.getCmrName4());
        address.setCmrName4(null);
      } else if (!StringUtils.isEmpty(address.getCmrName3())) {
        address.setCmrCity(address.getCmrName3());
        address.setCmrName3(null);
      }
    }

    if (StringUtils.isEmpty(address.getCmrStreetAddress())) {
      // try to get it from the lowest line
      if (!StringUtils.isEmpty(address.getCmrName4())) {
        address.setCmrStreetAddress(address.getCmrName4());
        address.setCmrName4(null);
      } else if (!StringUtils.isEmpty(address.getCmrName3())) {
        address.setCmrStreetAddress(address.getCmrName3());
        address.setCmrName3(null);
      } else if (!StringUtils.isEmpty(address.getCmrName2())) {
        address.setCmrStreetAddress(address.getCmrName2());
        address.setCmrName3(null);
      }
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
    // CMR-2001 data attribute not changed by SOF source for AT
    if (!this.currentImportValues.isEmpty() && !SystemLocation.AUSTRIA.equals(data.getCmrIssuingCntry())) {
      super.setDataValuesOnImport(admin, data, results, mainRecord);

      // CMR-2096-Austria - "Central order block code"
      data.setOrdBlk(mainRecord.getCmrOrderBlock());
      LOG.trace("OrdBlk ======= : " + data.getOrdBlk());

      String embargoCode = (this.currentImportValues.get("EmbargoCode"));
      if (StringUtils.isBlank(embargoCode)) {
        embargoCode = getRdcAufsd(data.getCmrNo(), data.getCmrIssuingCntry());
      }
      if (!"ST".equalsIgnoreCase(embargoCode)) {
        data.setEmbargoCd(embargoCode);
        LOG.trace("EmbargoCode: " + embargoCode);
      } else if ("ST".equalsIgnoreCase(embargoCode)) {
        data.setTaxExemptStatus3(embargoCode);
        LOG.trace(" STC Order Block Code : " + embargoCode);
      }

      data.setAgreementSignDate(this.currentImportValues.get("AECISUBDate"));
      LOG.trace("AECISubDate: " + data.getAgreementSignDate());
      data.setBpSalesRepNo(this.currentImportValues.get("TeleCovRep"));
      LOG.trace("TeleCovRep: " + data.getBpSalesRepNo());
      data.setCreditCd(this.currentImportValues.get("CreditCode"));
      LOG.trace("CreditCode: " + data.getCreditCd());
      data.setCommercialFinanced(this.currentImportValues.get("ModeOfPayment"));
      LOG.trace("CoF: " + data.getCommercialFinanced());

      data.setPhone1(this.currentImportValues.get("TelephoneNo"));
      if (data.getPhone1() != null) {
        // Phone - remove non numeric characters
        data.setPhone1(data.getPhone1().replaceAll("[^0-9]", ""));
        if (data.getPhone1().length() > 15) {
          data.setPhone1(data.getPhone1().substring(0, 15));
        }
      }
      LOG.trace("TelephoneNo: " + data.getPhone1());
    }

    if (SystemLocation.AUSTRIA.equals(data.getCmrIssuingCntry())) {
      // Currency code for Austria
      data.setLegacyCurrencyCd(this.currentImportValues.get("CurrencyCode"));
      LOG.trace("Currency: " + data.getLegacyCurrencyCd());

      EntityManager em = JpaManager.getEntityManager();
      String sql = ExternalizedQuery.getSql("AT.GET.ZS01.DATLT");
      PreparedQuery query = new PreparedQuery(em, sql);
      query.setParameter("COUNTRY", SystemLocation.AUSTRIA);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("CMR_NO", data.getCmrNo());
      String datlt = query.getSingleResult(String.class);
      data.setAbbrevLocn(datlt);
      LOG.trace("Abbrev Loc: " + data.getAbbrevLocn());

      // CMR-2046 change SBO get value from DNB search to avoid get value
      // from
      // SOF
      // Austria - For SBO values exceeding 3 char length
      // CMR-2053 add SR value get from DNB search
      if (!(StringUtils.isEmpty(mainRecord.getCmrSortl()))) {
        data.setSalesBusOffCd(mainRecord.getCmrSortl());
      }
      if (!(StringUtils.isEmpty(mainRecord.getSR()))) {
        data.setRepTeamMemberNo(mainRecord.getSR());
      }

    }
    // Phone
    if (CEE_COUNTRIES_LIST.contains(data.getCmrIssuingCntry())) {
      data.setPhone1(mainRecord.getCmrCustPhone());
      if (data.getPhone1() != null) {
        // Phone - remove non numeric characters
        data.setPhone1(data.getPhone1().replaceAll("[^0-9]", ""));
        if (data.getPhone1().length() > 15) {
          data.setPhone1(data.getPhone1().substring(0, 15));
        }
      }
      data.setTaxCd2(mainRecord.getCmrEnterpriseNumber());
    }
    // ICO field
    if (SystemLocation.SLOVAKIA.equals(data.getCmrIssuingCntry())) {
      data.setCompany(this.currentImportValues.get("BankBranchNo"));
      LOG.trace("BankBranchNo: " + data.getCompany());
    } else if (SystemLocation.CZECH_REPUBLIC.equals(data.getCmrIssuingCntry())) {
      data.setCompany(this.currentImportValues.get("BankBranchNo"));// RBBXA->ICO
      data.setTaxCd1(mainRecord.getCmrBusinessReg());// kna1.stcd1->DIC
      LOG.trace("BankAccountNo: " + data.getCompany());
      LOG.trace("DIC: " + data.getTaxCd1());
    } else {
      data.setCompany("");
    }

    // DIC and OIB fields
    if (SystemLocation.SLOVAKIA.equals(data.getCmrIssuingCntry()) || SystemLocation.CROATIA.equals(data.getCmrIssuingCntry())) {
      data.setTaxCd1(this.currentImportValues.get("BankAccountNo"));
      LOG.trace("BankAccountNo: " + data.getTaxCd1());
    }

    String node1 = !StringUtils.isEmpty(mainRecord.getCmrCompanyNo()) ? mainRecord.getCmrCompanyNo() : "";
    // CEMEA update - set enterprise from company (ZZKV_NODE1)
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      data.setEnterprise(node1);

      // 1504458: Import of Enterprise number
      if (!SystemLocation.AUSTRIA.equals(data.getCmrIssuingCntry()) && node1.equals(mainRecord.getCmrNum())) {
        data.setEnterprise("");
      }
    }

    // Austria create - enterprise is model cmrNo
    if (SystemLocation.AUSTRIA.equals(data.getCmrIssuingCntry())) {
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        data.setEnterprise(mainRecord.getCmrNum());
      }
    }

    // CMR-2001 AT:Preferred language not changed by SOF source
    // if (!SystemLocation.AUSTRIA.equals(data.getCmrIssuingCntry())) {
    // String prefLang = this.currentImportValues.get("LangCode");
    // if (!StringUtils.isEmpty(prefLang) && "1".equals(prefLang)) {
    // data.setCustPrefLang("D");
    // } else {
    // data.setCustPrefLang("E");
    // }
    //
    // }

    data.setInstallBranchOff("");
    data.setInacType("");
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setCmrNo("");
      data.setPpsceid("");
    }

    // Ticked - load CIS duplicate fields
    if (SystemLocation.RUSSIAN_FEDERATION.equals(data.getCmrIssuingCntry()) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())
        && "Y".equals(data.getCisServiceCustIndc())) {
      loadDuplicateCMR(data, data.getDupIssuingCntryCd(), mainRecord.getCmrNum());
    } else if (SystemLocation.RUSSIAN_FEDERATION.equals(data.getCmrIssuingCntry()) && CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      // Not ticked - check and load CIS duplicate fields
      for (String dupCntry : CIS_DUPLICATE_COUNTRIES) {
        if (loadDuplicateCMR(data, dupCntry, mainRecord.getCmrNum())) {
          // CMR-4606
          // data.setCisServiceCustIndc("Y");
          // data.setDupIssuingCntryCd(dupCntry);
          break;
        }
      }
      CmrtCustExt cmrtExt = this.legacyObjects.getCustomerExt();
      if (cmrtExt != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyy");
        String aECISUBDateString = sdf.format(cmrtExt.getAeciSubDt());
        data.setAgreementSignDate(aECISUBDateString);
      }
    }

    if (SystemLocation.RUSSIAN_FEDERATION.equals(data.getCmrIssuingCntry())
        && ("010101".equals(data.getAgreementSignDate()) || "123101".equals(data.getAgreementSignDate()))) {
      data.setAgreementSignDate("");
    }

    // Type of Customer
    data.setBpAcctTyp(this.currentImportValues.get("BpAcctTyp"));

    if (SystemLocation.SERBIA.equals(data.getCmrIssuingCntry()) && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setEngineeringBo("");
    }
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
          if (!dupCmrNo.equals(dupRecordV.get("ZZKV_NODE1").toString())) {
            data.setDupEnterpriseNo(dupRecordV.get("ZZKV_NODE1").toString());
            LOG.debug("CompanyNo2: " + data.getDupEnterpriseNo());
          }
          data.setDupSalesRepNo(this.currentImportValues.get("SR"));
          LOG.debug("SalRepNameNo2: " + data.getDupSalesRepNo());
          data.setDupSalesBoCd(dupRecordV.get("SORTL").toString());
          LOG.debug("SalesBusOff2: " + dupRecordV.get("SORTL").toString());
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
  public int getName1Length() {
    return 35;
  }

  /* CREATCMR-1308 - Import from KUNNR_EXT table */
  private KunnrExt getKunnrExtDetails(String kunnr) throws Exception {
    KunnrExt ke = new KunnrExt();
    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.KUNNR_EXT.BY_KUNNR_MANDT_AUSTRIA");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("BUILDING");
    query.addField("DEPARTMENT");

    LOG.debug("Getting existing KUNNNR_EXT details from RDc DB..");
    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);

      ke.setBuilding(record.get("BUILDING") != null ? record.get("BUILDING").toString() : "");
      ke.setDepartment(record.get("DEPARTMENT") != null ? record.get("DEPARTMENT").toString() : "");

      LOG.debug("***RETURNING BUILDING > " + ke.getBuilding());
      LOG.debug("***RETURNING DEPARTMENT > " + ke.getDepartment());
    }
    return ke;
  }

  @Override
  public int getName2Length() {
    return 35;
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    boolean doSplit = (currentRecord.getCmrName1Plain() != null && currentRecord.getCmrName1Plain().length() > 35)
        || (currentRecord.getCmrName2Plain() != null && currentRecord.getCmrName2Plain().length() > 35);

    String country = currentRecord.getCmrIssuedBy();
    ArrayList<String> addrDetailsList = new ArrayList<String>();
    if (doSplit) {
      String[] names = splitName(currentRecord.getCmrName1Plain(), currentRecord.getCmrName2Plain(), 35, 100);
      address.setCustNm1(names[0]);
      String extendedName = names[1];
      names = splitName(extendedName, currentRecord.getCmrName3(), 35, 30);
      address.setCustNm2(names[0]);
      address.setCustNm3(names[1]);
    } else {
      address.setCustNm1(currentRecord.getCmrName1Plain());
      address.setCustNm2(currentRecord.getCmrName2Plain());
      address.setCustNm3(currentRecord.getCmrName3());
    }

    // FINAL TRIM
    if (address.getCustNm1() != null && address.getCustNm1().trim().length() > 35) {
      address.setCustNm1(address.getCustNm1().trim().substring(0, 35));
    }
    if (address.getCustNm2() != null && address.getCustNm2().trim().length() > 35) {
      address.setCustNm2(address.getCustNm2().trim().substring(0, 35));
    }
    if (address.getCustNm3() != null && address.getCustNm3().trim().length() > 30) {
      address.setCustNm3(address.getCustNm3().trim().substring(0, 30));
    }
    doSplit = currentRecord.getCmrStreetAddress() != null && currentRecord.getCmrStreetAddress().length() > 35;
    if (doSplit) {
      splitAddress(address, currentRecord.getCmrStreetAddress(), currentRecord.getCmrStreetAddressCont(), 35, 35);
    } else {
      address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
    }
    address.setCustNm4(currentRecord.getCmrName4());
    address.setTransportZone("");

    if ("ZP02".equals(address.getId().getAddrType())) {
      address.setBldg(currentRecord.getCmrBldg());
    } else {
      address.setBldg(null);
    }
    if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
        && "ZS01".equalsIgnoreCase(address.getId().getAddrType()) && CEE_COUNTRIES_LIST.contains(country)) {
      address.getId().setAddrSeq("00001");
    }

    if (!CEE_COUNTRIES_LIST.contains(currentRecord.getCmrIssuedBy())) {
      KunnrExt addlAddDetail = getKunnrExtDetails(currentRecord.getCmrSapNumber());
      if (addlAddDetail != null) {
        if (SystemLocation.AUSTRIA.equals(country)) {
          address.setBldg(addlAddDetail.getBuilding() != null ? addlAddDetail.getBuilding() : "");
          address.setDept(addlAddDetail.getDepartment() != null ? addlAddDetail.getDepartment() : "");
        }

        if (!StringUtils.isEmpty(address.getDept())) {
          addrDetailsList.add(address.getDept());
        }
        if (!StringUtils.isEmpty(address.getBldg())) {
          addrDetailsList.add(address.getBldg());
        }
      }
    }

    if ("821".equals(currentRecord.getCmrIssuedBy())) {
      address.setStateProv("");
    }

  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setCmrOwner("IBM");
    // data.setBpAcctTyp("N");
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
    request.setSORTL(data.getSalesBusOffCd());
    request.setCompanyNumber(data.getEnterprise());
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

    if (("BUSPR".equals(data.getCustSubGrp()) || "XBP".equals(data.getCustSubGrp())) && CEE_COUNTRIES_LIST.contains(data.getCmrIssuingCntry())) {
      data.setBpRelType("CA");
    }
    if (CEE_COUNTRIES_LIST.contains(data.getCmrIssuingCntry())) {
      if (StringUtils.isNotBlank(data.getEngineeringBo())) {
        data.setEngineeringBo(StringUtils.rightPad(data.getEngineeringBo(), 7, "0"));
      }
    }
    if (CEE_COUNTRIES_LIST.contains(data.getCmrIssuingCntry()) && "U".equals(admin.getReqType())) {
      String legacyGaddrSeq = getGaddressSeqFromLegacy(entityManager, data.getCmrIssuingCntry(), data.getCmrNo());
      String zp02updateinit = getZP02UpdateInit(entityManager, data.getId().getReqId());
      String zp02importinit = getZP02importInit(entityManager, data.getId().getReqId());
      if (StringUtils.isBlank(legacyGaddrSeq)) {
        changeZP02AddrNew(entityManager, data.getId().getReqId());
      }
      if ("Y".equals(zp02updateinit)) {
        changeZS01AddrUpdate(entityManager, data.getId().getReqId());
        changeZP01AddrUpdate(entityManager, data.getId().getReqId());
      }
      if ("N".equals(zp02importinit)) {
        changeZS01AddrUpdate(entityManager, data.getId().getReqId());
        changeZP01AddrUpdate(entityManager, data.getId().getReqId());
      }
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {

    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    if (!("618".equals(cmrIssuingCntry))) {
      if (!"ZP01".equals(addr.getId().getAddrType())) {
        addr.setDept("");
      }
    }

    if (CEE_COUNTRIES_LIST.contains(data.getCmrIssuingCntry()) && data != null && "ZP02".equals(addr.getId().getAddrType())) {
      upperChar(addr);
    }

    if ("821".equals(cmrIssuingCntry)) {
      addr.setStateProv("");
    }

  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", admin.getId().getReqId());
    List<Addr> addresses = query.getResults(Addr.class);

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

    if (CEE_COUNTRIES_LIST.contains(data.getCmrIssuingCntry()) && !"707".equals(data.getCmrIssuingCntry())) {
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
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "CUST_NM3", "CUST_NM4", "ADDR_TXT", "ADDR_TXT2", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX",
        "CUST_PHONE", "BLDG", "DEPT"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    if (CEMEA_CHECKLIST.contains(cmrIssiungCntry)) {
      return true;
    }
    return false;
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {

    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getOrdBlk(), newData.getOrdBlk())
        && SystemLocation.AUSTRIA.equals(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Central order block code", "Central order block code"));
      update.setNewData(service.getCodeAndDescription(newData.getOrdBlk(), "Central order block code", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getOrdBlk(), "Central order block code", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())
        && SystemLocation.AUSTRIA.equals(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Customer Classification Code", "Customer Classification Code"));
      update.setNewData(service.getCodeAndDescription(newData.getCustClass(), "Customer Classification Code", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCustClass(), "Customer Classification Code", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !service.equals(oldData.getTaxExempt3(), newData.getTaxExemptStatus3())
        && !SystemLocation.AUSTRIA.equals(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "TaxExemptStatus3", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxExemptStatus3(), "TaxExemptStatus3", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxExempt3(), "TaxExemptStatus3", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getPhone1(), newData.getPhone1())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Phone1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getPhone1(), "Phone1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getPhone1(), "Phone1", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CommercialFinanced", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCommercialFinanced(), "CommercialFinanced", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getAgreementSignDate(), newData.getAgreementSignDate())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AECISubDate", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getAgreementSignDate(), "AECISubDate", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getAgreementSignDate(), "AECISubDate", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getLegacyCurrencyCd(), newData.getLegacyCurrencyCd())
        && !SystemLocation.AUSTRIA.equals(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CurrencyCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getLegacyCurrencyCd(), "CurrencyCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getLegacyCurrencyCd(), "CurrencyCode", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCreditCd(), newData.getCreditCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CreditCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCreditCd(), "CreditCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCreditCd(), "CreditCd", cmrCountry));
      results.add(update);
    }
    if (!SystemLocation.RUSSIAN_FEDERATION.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getDupSalesRepNo(), newData.getDupSalesRepNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalRepNameNo2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getDupSalesRepNo(), "SalRepNameNo2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getDupSalesRepNo(), "SalRepNameNo2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getDupEnterpriseNo(), newData.getDupEnterpriseNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Enterprise2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getDupEnterpriseNo(), "Enterprise2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getDupEnterpriseNo(), "Enterprise2", cmrCountry));
      results.add(update);
    }
    if (!SystemLocation.RUSSIAN_FEDERATION.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getDupSalesBoCd(), newData.getDupSalesBoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesBusOff2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getDupSalesBoCd(), "SalesBusOff2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getDupSalesBoCd(), "SalesBusOff2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getDupIsuCd(), newData.getDupIsuCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ISU2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getDupIsuCd(), "ISU2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getDupIsuCd(), "ISU2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getDupClientTierCd(), newData.getDupClientTierCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ClientTier2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getDupClientTierCd(), "ClientTier2", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getDupClientTierCd(), "ClientTier2", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())
        && !SystemLocation.AUSTRIA.equals(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getTaxCd2(), newData.getTaxCd2()) && CEE_COUNTRIES_LIST.contains(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxCd2(), "Enterprise", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxCd2(), "Enterprise", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd1(), newData.getTaxCd1())
        && SystemLocation.CZECH_REPUBLIC.equals(cmrCountry)) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax1", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getTaxCd1(), "LocalTax1", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getTaxCd1(), "LocalTax1", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCompany(), newData.getCompany())
        && (SystemLocation.SLOVAKIA.equals(cmrCountry) || SystemLocation.CZECH_REPUBLIC.equals(cmrCountry))) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Company", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCompany(), "Company", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCompany(), "Company", cmrCountry));
      results.add(update);
    }

  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    switch (cntry) {
    case SystemLocation.AUSTRIA:
      return Arrays.asList(AUSTRIA_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    case SystemLocation.CZECH_REPUBLIC:
      return Arrays.asList(CZ_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    default:
      return Arrays.asList(CEEME_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    }
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    if (!equals(addr.getBldg(), addr.getBldgOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "LocalLangCountryName", "-"));
      update.setNewData(addr.getBldg());
      update.setOldData(addr.getBldgOld());
      results.add(update);
    }
    if (!equals(addr.getDept(), addr.getDeptOld())) {
      UpdatedNameAddrModel update = new UpdatedNameAddrModel();
      update.setAddrType(addrTypeDesc);
      update.setSapNumber(sapNumber);
      update.setDataField(PageManager.getLabel(cmrCountry, "ICE#", "-"));
      update.setNewData(addr.getDept());
      update.setOldData(addr.getDeptOld());
      results.add(update);
    }
    if (CEE_COUNTRY_LIST.contains(cmrCountry)) {
      for (UpdatedNameAddrModel model : results) {
        if (model.getDataField() != null && model.getDataField().equals(PageManager.getLabel(cmrCountry, "CustPhone", "-"))) {
          results.remove(model);
          break;
        }
      }
    }
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
    map.put("##RequesterID", "requesterId");
    map.put("##CurrencyCd", "legacyCurrencyCd");
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
    map.put("##RequestType", "reqType");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##POBox", "poBox");
    map.put("##Expedite", "expediteInd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##CustomerName2", "custNm2");
    map.put("##CustomerName1", "custNm1");
    map.put("##PostalCode", "postCd");
    map.put("##CustomerName4", "custNm4");
    map.put("##CustomerName3", "custNm3");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##LocationNumber", "locationNo");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    map.put("##RequestingLOB", "requestingLob");
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
    map.put("##CustPhone", "custPhone");
    map.put("##EmbargoCode", "embargoCd");
    map.put("##VATExempt", "vatExempt");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##Enterprise", "enterprise");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CustClass", "custClass");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    /**
     * Austria support new template to mass update
     */
    if ("618".equals(issuingCountry)) {
      return true;
    } else if (CEE_COUNTRY_LIST.contains(issuingCountry)) {
      return true;
    } else if (ME_COUNTRY_LIST.contains(issuingCountry)) {
      return true;
    }
    return false;
  }

  public static List<String> getDataFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CLIENT_TIER", "CUST_CLASS", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "SEARCH_TERM", "ISIC_CD",
        "SUB_INDUSTRY_CD", "VAT", "VAT_IND", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC", "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC",
        "GEO_LOCATION_CD", "DUNS_NO", "ABBREV_LOCN", "TAX_CD1", "ORD_BLK", "ENTERPRISE", "TAX_EXEMPT_STATUS_3"));// CMR-1947:add
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
        "BP_REL_TYPE", "COMMERCIAL_FINANCED", "ENTERPRISE", "PHONE1", "TAX_CD1"));
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
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = null;
    if (CEE_COUNTRY_LIST.contains(cmrIssuingCntry)) {
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
    }
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
    if (CEE_COUNTRIES_LIST.contains(country)) {
      countryAddrss = CEE_MASS_UPDATE_SHEET_NAMES;

      XSSFSheet sheet = book.getSheet("Data");// validate Data sheet
      row = sheet.getRow(0);// data field name row
      int ordBlkIndex = 12;// default index
      int stcOrdBlkIndex = 13;
      int isuCdIndex = 6; //
      int ctcIndex = 7; //
      int fiscalCdIndex = 16; // default index
      for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
        currCell = row.getCell(cellIndex);
        String cellVal = validateColValFromCell(currCell);
        if ("Order block code".equals(cellVal)) {
          ordBlkIndex = cellIndex;
          // break;
        }
        if ("STC order block code".equals(cellVal)) {
          stcOrdBlkIndex = cellIndex;
          break;
        }
        if ("ISU Code".equals(cellVal)) {
          isuCdIndex = cellIndex;
          // break;
        }
        if ("Client Tier".equals(cellVal)) {
          ctcIndex = cellIndex;
          // break;
        }
        if ("Fiscal Code".equals(cellVal)) {
          fiscalCdIndex = cellIndex;
          break;
        }
      }

      for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
        TemplateValidation error = new TemplateValidation("Data");
        row = sheet.getRow(rowIndex);
        if (row == null) {
          return; // stop immediately when row is blank
        }
        currCell = row.getCell(ordBlkIndex);
        String ordBlk = validateColValFromCell(currCell);
        if (StringUtils.isNotBlank(ordBlk) && !("@".equals(ordBlk) || "E".equals(ordBlk) || "J".equals(ordBlk) || "R".equals(ordBlk))) {
          LOG.trace("Order Block Code should only @, E, R, J. >> ");
          error.addError((rowIndex + 1), "Order Block Code", "Order Block Code should be only @, E, R, J. ");
        }
        currCell = row.getCell(stcOrdBlkIndex);
        String stcOrdBlk = validateColValFromCell(currCell);
        if (StringUtils.isNotBlank(stcOrdBlk) && StringUtils.isNotBlank(ordBlk)) {
          LOG.trace("Please fill either STC Order Block Code or Order Block Code ");
          error.addError((row.getRowNum() + 1), "Order Block Code", "Please fill either STC Order Block Code or Order Block Code ");
        }
        currCell = row.getCell(fiscalCdIndex);
        String fiscalCd = validateColValFromCell(currCell);
        if (StringUtils.isNotBlank(fiscalCd) && !(fiscalCd.matches("^[0-9]*$")) && "826".equals(country)) {
          LOG.trace("fiscal code should consist only of digits");
          error.addError((rowIndex + 1), "Fiscal Code", " fiscal code should consist only of digits.");
        }
        currCell = row.getCell(isuCdIndex);
        String isuCd = validateColValFromCell(currCell);
        currCell = row.getCell(ctcIndex);
        String ctc = validateColValFromCell(currCell);
        if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(ctc)) || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isuCd))) {
          LOG.trace("The row " + (rowIndex + 1) + ":Note that both ISU and CTC value needs to be filled..");
          error.addError((rowIndex + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
        } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"Q".contains(ctc)) {
            LOG.trace("The row " + (rowIndex + 1)
                + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
            error.addError((rowIndex + 1), "Client Tier",
                ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
          }
        } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"Y".contains(ctc)) {
            LOG.trace("The row " + (rowIndex + 1)
                + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
            error.addError((rowIndex + 1), "Client Tier",
                ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
          }
        } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"T".contains(ctc)) {
            LOG.trace("The row " + (rowIndex + 1)
                + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
            error.addError((rowIndex + 1), "Client Tier",
                ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
          }
        } else if ((!StringUtils.isBlank(isuCd) && !("34".equals(isuCd) || "32".equals(isuCd) || "36".equals(isuCd))) && !"@".equalsIgnoreCase(ctc)) {
          LOG.trace("Client Tier should be '@' for the selected ISU Code.");
          error.addError((rowIndex + 1), "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
        }
        for (String name : countryAddrss) {
          sheet = book.getSheet(name);
          // Address Sheet

          String custNm1 = ""; // 2
          String custNm2 = ""; // 3
          String custNm3 = ""; // 4
          String custNm4 = ""; // 5
          String street = ""; // 6
          String postCode = ""; // 7
          String city = ""; // 8
          String stateProv = ""; // 9
          String landCntry = ""; // 10
          String hardWareMaster = ""; // 11

          row = sheet.getRow(rowIndex);
          if (row != null) {
            currCell = row.getCell(2);
            custNm1 = validateColValFromCell(currCell);

            currCell = row.getCell(3);
            custNm2 = validateColValFromCell(currCell);

            currCell = row.getCell(4);
            custNm3 = validateColValFromCell(currCell);

            currCell = row.getCell(5);
            custNm4 = validateColValFromCell(currCell);

            currCell = row.getCell(6);
            street = validateColValFromCell(currCell);

            currCell = row.getCell(7);
            postCode = validateColValFromCell(currCell);

            currCell = row.getCell(8);
            city = validateColValFromCell(currCell);

            currCell = row.getCell(9);
            stateProv = validateColValFromCell(currCell);

            currCell = row.getCell(10);
            landCntry = validateColValFromCell(currCell);

            currCell = row.getCell(11);
            hardWareMaster = validateColValFromCell(currCell);

            if ("RO".equals(landCntry) && "B".equals(stateProv)) {
              if (!(city.equals(city.toUpperCase()) && custNm1.equals(custNm1.toUpperCase()) && custNm2.equals(custNm2.toUpperCase())
                  && custNm3.equals(custNm3.toUpperCase()) && custNm4.equals(custNm4.toUpperCase()) && street.equals(street.toUpperCase())
                  && postCode.equals(postCode.toUpperCase()) && stateProv.equals(stateProv.toUpperCase())
                  && hardWareMaster.equals(hardWareMaster.toUpperCase()))) {
                error.addError((rowIndex + 1), "Tab Name : " + name + "," + "", "'The address data must be entered in capital letters'. <br>");
              } else if (!StringUtils.isBlank(city) && (city.equalsIgnoreCase("BUCHAREST") && ("Ship To".equals(name) || "Mail to".equals(name)
                  || "Bill To".equals(name) || "Sold To".equals(name) || "Install At".equals(name) || "Address in Local language".equals(name)))) {
                error.addError((rowIndex + 1), "Tab Name : " + name + "," + " City  ",
                    "Correct format for city is BUCHAREST SECTOR 'N'  (N = number 1,2,3,4,5 or 6) <br>");
              }
            }
          }
        }
        if (error.hasErrors()) {
          validations.add(error);
        }
      }

      // for (String name : countryAddrss) {
      // sheet = book.getSheet(name);
      // for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
      // String cbCity = ""; // 8
      // String localCity = ""; // 7
      //
      // row = sheet.getRow(rowIndex);
      // if (row == null) {
      // return; // stop immediately when row is blank
      // }
      // currCell = row.getCell(6);
      // localCity = validateColValFromCell(currCell);
      //
      // TemplateValidation error = new TemplateValidation(name);
      // }
      // }
    }
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
    query.addField("SORTL");
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

  @Override
  public String getCMRNo(EntityManager rdcMgr, String kukla, String mandt, String katr6, String cmrNo, CmrCloningQueue cloningQueue) {
    if (SystemLocation.AUSTRIA.equals(katr6)) {
      LOG.debug("generateCNDCmr :: START");
      String cndCMR = "";
      boolean internal = false;

      if ("11".equals(cloningQueue.getLastUpdtBy()) && cmrNo.startsWith("99")) {
        LOG.debug("Skip setting of CMR No for Internal for CMR : " + cmrNo);
      } else if (Arrays.asList("81", "85").contains(cloningQueue.getLastUpdtBy()) && !cmrNo.startsWith("99")) {
        internal = true;
      } else if (cmrNo.startsWith("99"))
        internal = true;

      int i = 0;

      if (internal) {
        while (i < 5) {
          String ran1 = "";
          ran1 = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
          // cndCMR = "99" + RDCRandomString.genNumericNumberSeries(4, kukla);
          cndCMR = "99" + ran1;

          LOG.debug("Generated AT Internal CMR No.:" + cndCMR);

          if (CloningRDCDirectUtil.checkCustNoForDuplicateRecord(rdcMgr, cndCMR, mandt, katr6)) {
            i++;
            LOG.debug("Alredy exist CMR No.: " + cndCMR + "  in rdc. Trying to generate next times:");
            if (i == 5) {
              LOG.debug("Max limit is 5 times to generate CMR No.: " + cndCMR + " Tried times:" + i);
              cndCMR = "";
            }
          } else
            break;
        }
      } else {

        while (i < 5) {

          int ran3 = (int) ((Math.random() * 9 + 1) * 100000);
          if (ran3 > 990000) {
            ran3 = ran3 - (int) ((Math.random() * 9 + 1) * 10000);
          }

          cndCMR = String.valueOf(ran3);
          // cndCMR = "6" + RDCRandomString.genNumericNumberSeries(5, kukla);

          LOG.debug("Generated AT Non Internal CMR No.:" + cndCMR);

          if (CloningRDCDirectUtil.checkCustNoForDuplicateRecord(rdcMgr, cndCMR, mandt, katr6)) {
            i++;
            LOG.debug("Alredy exist CMR No.: " + cndCMR + "  in rdc. Trying to generate next times:");
            if (i == 5) {
              LOG.debug("Max limit is 5 times to generate CMR No.: " + cndCMR + " Tried times:" + i);
              cndCMR = "";
            }
          } else
            break;
        }
      }
      LOG.debug("generateCNDCmr :: returnung cndCMR = " + cndCMR);
      LOG.debug("generateCNDCmr :: END");
      return cndCMR;
    }
    return null;
  }

  private String getSoldtoSeqFromRDC(EntityManager entityManager, String mandt, String katr6, String cmrNo) {
    String addrSeq = null;
    String sql = ExternalizedQuery.getSql("CEE.GETSOLDTOSEQFROMRDC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", katr6);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    String result = query.getSingleResult(String.class);

    if (result != null) {
      addrSeq = result;
    }

    return addrSeq;
  }

  @SuppressWarnings("unchecked")
  public static void doFilterAddresses(RequestEntryModel reqEntry, Object mainRecords, Object filteredRecords) {
    if (mainRecords instanceof java.util.List<?> && filteredRecords instanceof java.util.List<?>) {
      // during convertFrom
      if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(reqEntry.getReqType())) {
        List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
        List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
        for (Object tempRecObj : recordsToCheck) {
          if (tempRecObj instanceof FindCMRRecordModel) {
            FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
            if ("ZS01".equalsIgnoreCase(tempRec.getCmrAddrTypeCode()) && ("90".equalsIgnoreCase(tempRec.getCmrOrderBlock()))) {
              tempRec.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZS03.toString());
            }
            recordsToReturn.add(tempRec);
          }
        }
      }
    }
  }
}
