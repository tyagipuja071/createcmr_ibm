/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.Kna1;
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
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ValidatorClient;
import com.ibm.cmr.services.client.validator.PostalCodeValidateRequest;
import com.ibm.cmr.services.client.validator.ValidationResult;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Import Converter for Israel
 * 
 * @author Jeffrey Zamora
 * 
 */
public class IsraelHandler extends EMEAHandler {

  private static final Logger LOG = Logger.getLogger(IsraelHandler.class);

  private static Map<String, List<List<String>>> MASS_UPDT_DUP_ENTRY_MAP = new HashMap<String, List<List<String>>>();

  private static final String[] ISRAEL_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode", "LocalTax1",
      "LocalTax2", "SearchTerm", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office", "Floor", "Building",
      "County", "City2", "BPRelationType", "MembLevel", "INACType" };

  private static final List<String> EMEA_COUNTRY_VAL = Arrays.asList(SystemLocation.UNITED_KINGDOM, SystemLocation.IRELAND, SystemLocation.ISRAEL,
      SystemLocation.TURKEY, SystemLocation.GREECE, SystemLocation.CYPRUS, SystemLocation.ITALY);

  public static final String EMEA_MASSCHANGE_TEMPLATE_ID = "EMEA";

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  protected static final String[] LD_MASS_UPDATE_SHEET_NAMES = { "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address (Update)", "EPL Address" };

  private static final String[] IL_MASSUPDATE_SHEET_NAMES = { "Data", "Mailing", "Billing", "Installing", "Shipping", "EPL",
      "Country Use A (Mailing)", "Country Use B (Billing)", "Country Use C (Shipping)" };

  private static enum IL_MASSUPDATE_ADDR {
    CMRNO, SEQNO, CUSTNAME, CUSTNAMECONT, ATTPERSON, STREET, POBOX, ADDRCONT, CITY, POSTCODE, LANDCOUNTRY
  };

  private static final String CUSTGRP_LOCAL = "LOCAL";
  private static final int MANDATORY_ADDR_COUNT = 8;
  private static final int MAX_ADDR_SEQ = 99999;
  private static final String DEFAULT_COUNTRY_CODE = "IL";

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.UNITED_KINGDOM, "GB");
    LANDED_CNTRY_MAP.put(SystemLocation.IRELAND, "IE");

    if (MASS_UPDT_DUP_ENTRY_MAP == null) {
      MASS_UPDT_DUP_ENTRY_MAP = new HashMap<String, List<List<String>>>();
    }

    // DTN: build the pattern list
    List<List<String>> patternsTop = new ArrayList<List<String>>();
    List<String> dupPatterns = new ArrayList<String>();
    dupPatterns = Arrays.asList(new String[] { "Street Con't", "Att. Person" });
    patternsTop.add(dupPatterns);
    dupPatterns = Arrays.asList(new String[] { "Street Con't", "Att. Person", "PO Box" });
    patternsTop.add(dupPatterns);

    MASS_UPDT_DUP_ENTRY_MAP.put("local", patternsTop);

    patternsTop = new ArrayList<List<String>>();
    dupPatterns = Arrays.asList(new String[] { "Street Con't", "PO Box" });
    patternsTop.add(dupPatterns);

    MASS_UPDT_DUP_ENTRY_MAP.put("crossb", patternsTop);

  }

  @Override
  public void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, FindCMRRecordModel mainRecord,
      List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    LOG.info("IsraelHandler processing type: " + processingType);
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // LD Implementation
      if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
        // only add zs01 equivalent for create by model
        FindCMRRecordModel record = mainRecord;
        // name4 in rdc = Attn on SOF
        record.setCmrDept(record.getCmrName4());
        record.setCmrName4(null);
        // name3 in rdc = Address Con't on SOF
        record.setCmrStreetAddressCont(record.getCmrName3());
        record.setCmrName3(null);

        if (!StringUtils.isBlank(record.getCmrPOBox())) {
          record.setCmrPOBox(record.getCmrPOBox());
        }

        if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
          record.setCmrAddrSeq("00001");
        } else {
          record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
        }

        boolean isProspects = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
        if (!isProspects) {
          String pairedAddrSeq = record.getCmrAddrSeq();
          List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
          CmrtAddr legacyAddr = getLegacyAddrByAddrPair(legacyAddrList, pairedAddrSeq);

          if (legacyAddr == null) {
            legacyAddr = getDefaultPairedLegacyAddr("ZS01");
          }
          FindCMRRecordModel addr = cloneAddress(record, "ZS01");

          converted.add(mapEnglishAddr(entityManager, addr, legacyAddr));
        } else {
          record.setCmrAddrSeq("00006");
          record.setCmrAddrTypeCode("CTYA");
          converted.add(record);
        }
      } else {
        if (source.getItems() != null) {
          String addrType = null;
          String seqNo = null;
          List<String> sofUses = null;
          FindCMRRecordModel addr = null;

          // map RDc - SOF - CreateCMR by sequence no
          for (FindCMRRecordModel record : source.getItems()) {
            seqNo = record.getCmrAddrSeq();
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
              sofUses = this.legacyObjects.getUsesBySequenceNo(seqNo);

              if (isEnglishAllSharedSequence(sofUses)) {
                if ("ZI01".equals(record.getCmrAddrTypeCode())) {
                  sofUses = setUsesIfDuplicate(sofUses, record);
                }
              }

              for (String sofUse : sofUses) {
                addrType = getAddressTypeByUse(sofUse);
                if (!StringUtils.isEmpty(addrType)) {
                  addr = cloneAddress(record, addrType);
                  LOG.info("Adding address type " + addrType + " for sequence " + seqNo);

                  // name3 in rdc = Address Con't on SOF
                  addr.setCmrStreetAddressCont(record.getCmrName3());
                  addr.setCmrName3(null);
                  addr.setCmrDept(record.getCmrName4());

                  if (!StringUtils.isBlank(record.getCmrPOBox())) {
                    addr.setCmrPOBox(record.getCmrPOBox());
                  }

                  if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                    addr.setCmrAddrSeq("00001");
                  }

                  if ("ZS01".equals(addrType) || "ZP01".equals(addrType) || "ZD01".equals(addrType)) {
                    addr.setCmrAddrTypeCode(record.getCmrAddrTypeCode());

                    String pairedAddrSeq = addr.getCmrAddrSeq();
                    List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();
                    CmrtAddr legacyAddr = getLegacyAddrByAddrPair(legacyAddrList, pairedAddrSeq);

                    if (legacyAddr == null) {
                      legacyAddr = getDefaultPairedLegacyAddr(addrType);
                    }

                    if ("ZS01".equals(addrType)) {
                      record.setCmrCustPhone(mainRecord.getCmrCustPhone());
                    }

                    converted.add(mapEnglishAddr(entityManager, addr, legacyAddr, addrType));
                    converted.add(mapLocalLanguageAddr(entityManager, record, addrType));

                    // Handle Sold to shared sequence -- EPL and Installing
                    if ("ZS01".equals(addrType)) {
                      if ("Y".equals(legacyAddr.getIsAddrUseInstalling()) && !isLegacyAddrInRdc(legacyAddr.getId().getAddrNo(), source.getItems())) {
                        FindCMRRecordModel installing = cloneAddress(addr, "ZI01");
                        installing.setTransAddrNo("");

                        converted.add(installing);
                      }
                      if ("Y".equals(legacyAddr.getIsAddrUseEPL()) && !isLegacyAddrInRdc(legacyAddr.getId().getAddrNo(), source.getItems())) {
                        FindCMRRecordModel epl = cloneAddress(addr, "ZS02");
                        epl.setTransAddrNo("");
                        converted.add(epl);
                      }
                    }
                  } else {
                    converted.add(addr);
                  }
                }
              }
            }
          }
          // iterate legacy address and add those not in RDC
          if (this.legacyObjects != null && this.legacyObjects.getAddresses() != null) {
            String legacyAddrSeqNo = null;
            String legacyAddrType = null;
            List<String> legacyAddrUses = null;

            for (CmrtAddr cmrtAddr : this.legacyObjects.getAddresses()) {
              legacyAddrSeqNo = cmrtAddr.getId().getAddrNo();
              if (!isLegacyAddrInRdc(legacyAddrSeqNo, converted)) {
                legacyAddrUses = this.legacyObjects.getUsesBySequenceNo(legacyAddrSeqNo);

                if (legacyAddrUses != null && legacyAddrUses.size() > 0) {
                  for (String legacyAddrUse : legacyAddrUses) {
                    legacyAddrType = getAddressTypeByUse(legacyAddrUse);
                    if (StringUtils.isNotEmpty(legacyAddrType)) {
                      FindCMRRecordModel addrModel = getAddrTypeAndClone(converted, legacyAddrType);
                      if (addrModel == null) {
                        if (CmrConstants.ADDR_TYPE.ZS01.equals(legacyAddrType) || CmrConstants.ADDR_TYPE.ZP01.equals(legacyAddrType)
                            || CmrConstants.ADDR_TYPE.ZD01.equals(legacyAddrType)) {
                          // clone ZS01 as object template for hebrew address
                          addrModel = getAddrTypeAndClone(converted, "ZS01");
                        } else {
                          // clone CTYA as object template for english address
                          addrModel = getAddrTypeAndClone(converted, "CTYA");
                        }
                      }
                      if (addrModel != null) {
                        // Identify address landed country
                        String addrlu = cmrtAddr.getAddrLineU();
                        if (StringUtils.isNotBlank(addrlu) && StringUtils.contains(addrlu, "J")) {
                          // iterate addrlines to get the landed country
                          String legacyAddrLandCntry = null;
                          for (int i = 6; i >= 4; i--) {
                            if (i == 6 && StringUtils.isNotBlank(cmrtAddr.getAddrLine6())) {
                              legacyAddrLandCntry = cmrtAddr.getAddrLine6();
                              break;
                            } else if (i == 5 && StringUtils.isNotBlank(cmrtAddr.getAddrLine5())) {
                              legacyAddrLandCntry = cmrtAddr.getAddrLine5();
                              break;
                            } else if (i == 4 && StringUtils.isNotBlank(cmrtAddr.getAddrLine4())) {
                              legacyAddrLandCntry = cmrtAddr.getAddrLine4();
                              break;
                            }
                          }
                          // Get country code
                          if (StringUtils.isNotEmpty(legacyAddrLandCntry)) {
                            String sql = null;
                            if (CmrConstants.ADDR_TYPE.ZS01.equals(legacyAddrType) || CmrConstants.ADDR_TYPE.ZP01.equals(legacyAddrType)
                                || CmrConstants.ADDR_TYPE.ZD01.equals(legacyAddrType)) {
                              sql = ExternalizedQuery.getSql("IL.GET.COUNGTRYCODE_BYLOCALCOUNTRYDESC");
                            } else {
                              sql = ExternalizedQuery.getSql("IL.GET.COUNTRYCODE_BYCOUNTRYDESC");
                            }
                            PreparedQuery query = new PreparedQuery(entityManager, sql);
                            query.setParameter("COUNTRY_DESC", legacyAddrLandCntry);

                            List<Object[]> results = query.getResults();
                            if (results != null && results.size() > 0) {
                              Object[] result = results.get(0);
                              if (result != null && result.length > 0) {
                                if (result[0] != null) {
                                  addrModel.setCmrCountryLanded((String) result[0]);
                                }
                                if (result[1] != null) {
                                  addrModel.setCmrCountryLandedDesc((String) result[1]);
                                }
                              }
                            }
                          }
                        }

                        addrModel.setCmrAddrTypeCode(legacyAddrType);
                        addrModel.setTransAddrNo(cmrtAddr.getAddrLineO());
                        addrModel.setCmrSitePartyID("");

                        // set to blank
                        // to be populated from legacy address lines based on
                        // ADDRLU
                        addrModel.setCmrName(cmrtAddr.getAddrLine1());
                        addrModel.setCmrStreet(cmrtAddr.getStreet());
                        addrModel.setCmrAddrSeq(cmrtAddr.getId().getAddrNo());
                        addrModel.setCmrName1Plain("");
                        addrModel.setCmrName2Plain("");
                        addrModel.setCmrStreetAddress("");
                        addrModel.setCmrCity("");
                        addrModel.setCmrPostalCode("");
                        addrModel.setCmrPOBox("");
                        addrModel.setCmrName3(null);
                        addrModel.setCmrSapNumber("");
                        addrModel.setCmrIntlName1("");
                        addrModel.setCmrIntlName2("");
                        addrModel.setCmrIntlName3("");
                        addrModel.setCmrIntlAddress("");
                        addrModel.setCmrIntlCity1("");
                        addrModel.setCmrDept("");

                        // fill the blank fields with data from legacy DB
                        if ("ZS01".equals(legacyAddrType) || "ZP01".equals(legacyAddrType) || "ZD01".equals(legacyAddrType)) {
                          fillInMissingAddrDataFromLegacy(entityManager, addrModel, cmrtAddr, true);
                        } else {
                          if ("CTYA".equals(legacyAddrType) || "CTYB".equals(legacyAddrType) || "CTYC".equals(legacyAddrType)) {
                            if (StringUtils.isNotBlank(cmrtAddr.getAddrLineO())) {
                              addrModel.setTransAddrNo(cmrtAddr.getAddrLineO());
                            } else {
                              CmrtAddr localAddr = getDefaultPairedLegacyAddr(legacyAddrType);
                              addrModel.setTransAddrNo(localAddr.getId().getAddrNo());
                            }
                          }
                          fillInMissingAddrDataFromLegacy(entityManager, addrModel, cmrtAddr, false);
                        }
                        converted.add(addrModel);
                      }
                    }
                  }
                }
              }
            }
          }

        }
      }
    } else {
      super.handleSOFConvertFrom(entityManager, source, reqEntry, mainRecord, converted, searchModel);
    }
  }

  private List<String> setUsesIfDuplicate(List<String> sofUses, FindCMRRecordModel record) {
    List<String> newSofUses = new ArrayList<>();
    newSofUses.add("3");
    newSofUses.add("5");
    return newSofUses;
  }

  private boolean isEnglishAllSharedSequence(List<String> sofUses) {

    if (sofUses != null) {
      List<String> allEngSharedAddrUses = new ArrayList<>();
      allEngSharedAddrUses.add("3");
      allEngSharedAddrUses.add("5");
      allEngSharedAddrUses.add("A");
      allEngSharedAddrUses.add("B");
      allEngSharedAddrUses.add("C");

      return CollectionUtils.isEqualCollection(sofUses, allEngSharedAddrUses);
    }

    return false;
  }

  private boolean isLegacyAddrInRdc(String addrNo, List<FindCMRRecordModel> converted) {
    if (converted != null && converted.size() > 0) {
      for (FindCMRRecordModel findCMRRecordModel : converted) {
        if (addrNo.equals(String.format("%05d", Integer.parseInt(findCMRRecordModel.getCmrAddrSeq())))) {
          return true;
        }
      }
    }
    return false;
  }

  private FindCMRRecordModel getAddrTypeAndClone(List<FindCMRRecordModel> converted, String addrType) throws Exception {
    if (converted != null && converted.size() > 0) {
      for (FindCMRRecordModel findCMRRecordModel : converted) {
        if (addrType.equals(findCMRRecordModel.getCmrAddrTypeCode())) {
          return cloneAddress(findCMRRecordModel, addrType);
        }
      }
    }
    return null;
  }

  private FindCMRRecordModel mapLocalLanguageAddr(EntityManager entityManager, FindCMRRecordModel record, String addrType)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    FindCMRRecordModel localLangAddr = new FindCMRRecordModel();
    PropertyUtils.copyProperties(localLangAddr, record);
    localLangAddr.setCmrName1Plain(record.getCmrIntlName1());
    localLangAddr.setCmrName2Plain(record.getCmrIntlName2());
    localLangAddr.setCmrStreetAddress(record.getCmrIntlAddress());
    localLangAddr.setCmrStreetAddressCont(record.getCmrIntlName3());
    localLangAddr.setCmrCity(record.getCmrIntlCity1());
    localLangAddr.setCmrAddrTypeCode(addrType);
    localLangAddr.setCmrDept(record.getCmrIntlName4());

    return localLangAddr;
  }

  private FindCMRRecordModel mapEnglishAddr(EntityManager entityManager, FindCMRRecordModel addr, CmrtAddr legacyAddr) {
    return mapEnglishAddr(entityManager, addr, legacyAddr, "ZS01");
  }

  private FindCMRRecordModel mapEnglishAddr(EntityManager entityManager, FindCMRRecordModel addr, CmrtAddr legacyAddr, String addrType) {
    if ("ZS01".equals(addrType)) {
      addr.setCmrAddrTypeCode("CTYA");
    } else if ("ZP01".equals(addrType)) {
      addr.setCmrAddrTypeCode("CTYB");
    } else if ("ZD01".equals(addrType)) {
      addr.setCmrAddrTypeCode("CTYC");
    }

    addr.setCmrAddrSeq(legacyAddr.getId().getAddrNo());

    if (StringUtils.isNotBlank(legacyAddr.getAddrLineO())) {
      addr.setTransAddrNo(legacyAddr.getAddrLineO());
    } else {
      CmrtAddr localAddr = getDefaultPairedLegacyAddr(addr.getCmrAddrTypeCode());
      addr.setTransAddrNo(localAddr.getId().getAddrNo());
    }

    return addr;
  }

  private void fillInMissingAddrDataFromLegacy(EntityManager entityManager, FindCMRRecordModel addr, CmrtAddr legacyAddr, boolean isLocalLanguage) {
    if (!isLocalLanguage) {
      mapEnglishAddrFromLegacyData(entityManager, addr, legacyAddr);
    } else {
      mapLocalLangAddrFromLegacyData(entityManager, addr, legacyAddr);
    }
  }

  private void mapLocalLangAddrFromLegacyData(EntityManager entityManager, FindCMRRecordModel addr, CmrtAddr legacyAddr) {
    if (StringUtils.isEmpty(addr.getCmrIntlName1())) {
      addr.setCmrName1Plain(getLegacyAddrBasedOnAddrlU(legacyAddr, "D"));
    }
    if (StringUtils.isEmpty(addr.getCmrIntlName2())) {
      addr.setCmrName2Plain(getLegacyAddrBasedOnAddrlU(legacyAddr, "E"));
    }
    if (StringUtils.isEmpty(addr.getCmrDept())) {
      addr.setCmrDept(getLegacyAddrBasedOnAddrlU(legacyAddr, "B"));
    }
    if (StringUtils.isEmpty(addr.getCmrIntlAddress())) {
      addr.setCmrStreetAddress(getLegacyAddrBasedOnAddrlU(legacyAddr, "F"));
    }
    if (StringUtils.isEmpty(addr.getCmrIntlName3())) {
      addr.setCmrStreetAddressCont(getLegacyAddrBasedOnAddrlU(legacyAddr, "G"));
    }
    if (StringUtils.isEmpty(addr.getCmrPOBox())) {
      addr.setCmrPOBox(getLegacyAddrBasedOnAddrlU(legacyAddr, "H"));
    }
    if (StringUtils.isEmpty(addr.getCmrIntlCity1())) {
      addr.setCmrCity(getLegacyAddrBasedOnAddrlU(legacyAddr, "IC"));
    }
    if (StringUtils.isEmpty(addr.getCmrPostalCode())) {
      addr.setCmrPostalCode(getLegacyAddrBasedOnAddrlU(legacyAddr, "IP"));
    }
    if (StringUtils.isEmpty(addr.getCmrCountryLanded())) {
      String landedCountryCode = getCountryCodeByDesc(entityManager, true, getLegacyAddrBasedOnAddrlU(legacyAddr, "J"));
      // landed country is not mapped in DB2 if the address is IL
      if (StringUtils.isEmpty(landedCountryCode)) {
        landedCountryCode = DEFAULT_COUNTRY_CODE;
      }
      addr.setCmrCountryLanded(landedCountryCode);
    }
  }

  private void mapEnglishAddrFromLegacyData(EntityManager entityManager, FindCMRRecordModel addr, CmrtAddr legacyAddr) {
    if (StringUtils.isEmpty(addr.getCmrName1Plain())) {
      addr.setCmrName1Plain(getLegacyAddrBasedOnAddrlU(legacyAddr, "D"));
    }
    if (StringUtils.isEmpty(addr.getCmrName2Plain())) {
      addr.setCmrName2Plain(getLegacyAddrBasedOnAddrlU(legacyAddr, "E"));
    }
    if (StringUtils.isEmpty(addr.getCmrDept())) {
      addr.setCmrDept(getLegacyAddrBasedOnAddrlU(legacyAddr, "B"));
    }
    if (StringUtils.isEmpty(addr.getCmrStreetAddress())) {
      addr.setCmrStreetAddress(getLegacyAddrBasedOnAddrlU(legacyAddr, "F"));
    }
    if (StringUtils.isEmpty(addr.getCmrStreetAddressCont())) {
      addr.setCmrStreetAddressCont(getLegacyAddrBasedOnAddrlU(legacyAddr, "G"));
    }
    if (StringUtils.isEmpty(addr.getCmrPOBox())) {
      addr.setCmrPOBox(getLegacyAddrBasedOnAddrlU(legacyAddr, "H"));
    }
    if (StringUtils.isEmpty(addr.getCmrCity())) {
      addr.setCmrCity(getLegacyAddrBasedOnAddrlU(legacyAddr, "IC"));
    }
    if (StringUtils.isEmpty(addr.getCmrPostalCode())) {
      addr.setCmrPostalCode(getLegacyAddrBasedOnAddrlU(legacyAddr, "IP"));
    }
    if (StringUtils.isEmpty(addr.getCmrCountryLanded())) {
      String landedCountryCode = getCountryCodeByDesc(entityManager, false, getLegacyAddrBasedOnAddrlU(legacyAddr, "J"));
      // landed country is not mapped in DB2 if the address is IL
      if (StringUtils.isEmpty(landedCountryCode)) {
        landedCountryCode = DEFAULT_COUNTRY_CODE;
      }
      addr.setCmrCountryLanded(landedCountryCode);
    }

  }

  private String getLegacyAddrBasedOnAddrlU(CmrtAddr legacyAddr, String addrlUVal) {
    char addrlUChar = addrlUVal.charAt(0);

    int addrlUCharIndex = legacyAddr.getAddrLineU().indexOf(addrlUChar);
    String addressLineVal;
    switch (addrlUCharIndex) {
    case 0:
      addressLineVal = legacyAddr.getAddrLine1();
      break;
    case 1:
      addressLineVal = legacyAddr.getAddrLine2();
      break;
    case 2:
      addressLineVal = legacyAddr.getAddrLine3();
      break;
    case 3:
      addressLineVal = legacyAddr.getAddrLine4();
      break;
    case 4:
      addressLineVal = legacyAddr.getAddrLine5();
      break;
    case 5:
      addressLineVal = legacyAddr.getAddrLine6();
      break;
    default:
      addressLineVal = "";
      break;
    }

    if (addrlUChar == 'I' && StringUtils.isNotEmpty(addressLineVal)) {
      if (addressLineVal.contains(" ")) {
        String postalCode = "";
        String city = "";

        String[] parts = addressLineVal.split(" ");
        for (int i = 0; i < parts.length; i++) {
          if ((i == 0 || i == parts.length - 1) && StringUtils.isBlank(postalCode) && StringUtils.isNumeric(parts[i])) {
            postalCode = parts[i];
          } else {
            city += " " + parts[i];
          }
        }

        if ("IP".equals(addrlUVal) && StringUtils.isNumeric(postalCode)) {
          addressLineVal = postalCode;
        } else if ("IC".equals(addrlUVal) && !StringUtils.isNumeric(city)) {
          addressLineVal = city.trim();
        }
      } else {
        if (StringUtils.isNumeric(addressLineVal)) {
          if ("IC".equals(addrlUVal)) {
            addressLineVal = "";
          }
        } else {
          if ("IP".equals(addrlUVal)) {
            addressLineVal = "";
          }
        }
      }
    } else if (addrlUChar == 'H' && StringUtils.isNotEmpty(addressLineVal)) {
      addressLineVal = addressLineVal.replace("מ.ד", "").replace("PO BOX", "").trim();
    }

    return addressLineVal;
  }

  private CmrtAddr getLegacyAddrByAddrPair(List<CmrtAddr> legacyAddrList, String pairedAddrSeq) {
    String pairedSeq = String.format("%05d", Integer.parseInt(pairedAddrSeq));
    CmrtAddr legacyAddr = legacyAddrList.stream().filter(a -> pairedSeq.equals(a.getAddrLineO())).findAny().orElse(null);
    return legacyAddr;
  }

  private CmrtAddr getDefaultPairedLegacyAddr(String pairAddrType) {
    List<CmrtAddr> legacyAddrList = legacyObjects.getAddresses();

    for (CmrtAddr legacyAddr : legacyAddrList) {

      if ("ZS01".equals(pairAddrType) && "Y".equals(legacyAddr.getIsAddressUseA())) {
        return legacyAddr;
      } else if ("ZP01".equals(pairAddrType) && "Y".equals(legacyAddr.getIsAddressUseB())) {
        return legacyAddr;
      } else if ("ZD01".equals(pairAddrType) && "Y".equals(legacyAddr.getIsAddressUseC())) {
        return legacyAddr;
      } else if ("CTYA".equals(pairAddrType) && "Y".equals(legacyAddr.getIsAddrUseMailing())) {
        return legacyAddr;
      } else if ("CTYB".equals(pairAddrType) && "Y".equals(legacyAddr.getIsAddrUseBilling())) {
        return legacyAddr;
      } else if ("CTYC".equals(pairAddrType) && "Y".equals(legacyAddr.getIsAddrUseShipping())) {
        return legacyAddr;
      }
    }
    return null;
  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD setting
    } else {
      super.handleSOFAddressImport(entityManager, cmrIssuingCntry, address, addressKey);
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD setting
    } else {
      super.setAdminValuesOnImport(admin, currentRecord);
    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      super.setDataValuesOnImport(admin, data, results, mainRecord);

      String embargoCode = (this.currentImportValues.get("EmbargoCode"));
      if (StringUtils.isBlank(embargoCode)) {
        embargoCode = getRdcAufsd(data.getCmrNo(), data.getCmrIssuingCntry());
      }
      if (embargoCode != null && embargoCode.length() < 2 && !"ST".equalsIgnoreCase(embargoCode)) {
        data.setEmbargoCd(embargoCode);
        LOG.trace("EmbargoCode: " + embargoCode);
      } else if ("ST".equalsIgnoreCase(embargoCode)) {
        data.setTaxExemptStatus3(embargoCode);
        LOG.trace(" STC Order Block Code : " + embargoCode);
      }

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        data.setPpsceid("");
        boolean isProspects = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
        if (isProspects) {
          data.setCmrNo("");
        }
      }

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        data.setIsicCd("");
        data.setEngineeringBo("");
        data.setIsuCd("");
        data.setClientTier("");
        data.setRepTeamMemberNo("");
        data.setSalesBusOffCd("");
        data.setEnterprise("");
        data.setInacCd("");
        data.setBgId("");
        data.setGbgDesc("");
        data.setBgRuleId("");
        data.setCovDesc("");
        data.setGeoLocationCd("");
        data.setDunsNo("");
        data.setGbgId("");
        data.setCovId("");
        data.setBgDesc("");
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        // defect 1299146
        if (mainRecord.getCmrSortl() != null && mainRecord.getCmrSortl().length() >= 10) {
          data.setSalesBusOffCd(mainRecord.getCmrSortl().substring(0, 3));
          LOG.trace("SBO from Sortl : " + data.getSalesBusOffCd());
          if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
            data.setRepTeamMemberNo(mainRecord.getCmrSortl().substring(4));
            LOG.trace("Rep No from Sortl (IL) : " + data.getRepTeamMemberNo());
          } else {
            // defect fix 1329919 changed from 5 to 4
            data.setRepTeamMemberNo(mainRecord.getCmrSortl().substring(4, 10));
            LOG.trace("Rep No from Sortl : " + data.getRepTeamMemberNo());
          }
        }

      }
      // 1299146
      // Translate and auto-populate for next release
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && StringUtils.isEmpty(data.getCollectionCd())) {
        data.setCollectionCd("TC0");
      }

      // Changed abbreviated location if cross border to country
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        if (!this.currentImportValues.isEmpty() && !(mainRecord.getCmrCountryLanded().equalsIgnoreCase("IL"))) {
          String country = DropdownListController.getDescription("LandedCountry", mainRecord.getCmrCountryLanded(), SystemLocation.ISRAEL);
          if (!StringUtils.isEmpty(country) && admin.getReqType() == "C") {
            data.setAbbrevLocn(country.length() > 12 ? country.substring(0, 12) : country);
          }
        }

      }

      // For IBO, SBO & EBO values exceeding 3 char length
      String iBO = this.currentImportValues.get("IBO");
      String sBO = this.currentImportValues.get("SBO");
      String eBo = this.currentImportValues.get("DPCEBO");
      String accAdBo = this.currentImportValues.get("AccAdBo");

      if (!(StringUtils.isEmpty(iBO))) {
        if (iBO.length() > 3) {
          data.setInstallBranchOff(iBO.substring(0, 3));
        }
      }
      if (!(StringUtils.isEmpty(sBO))) {
        if (sBO.length() > 3) {
          data.setSalesBusOffCd(sBO.substring(0, 3));
        }
      }
      if (!(StringUtils.isEmpty(eBo))) {
        if (eBo.length() > 3) {
          data.setEngineeringBo(eBo.substring(0, 3));
        }
      }

      if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        if (StringUtils.isNotEmpty(mainRecord.getCmrDuns())) {
          data.setDunsNo(mainRecord.getCmrDuns());
        } else { // manually query KNA1.zzkv_duns
          EntityManager entityManager = JpaManager.getEntityManager();
          String sql = ExternalizedQuery.getSql("IL.GET.KNA1_ZS01DUNS");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
          query.setParameter("ZZKV_CUSNO", data.getCmrNo());

          List<String> record = query.getResults(String.class);
          if (record != null && !record.isEmpty()) {
            data.setDunsNo(record.get(0));
          }
        }

        String codflag = !StringUtils.isEmpty(legacyObjects.getCustomer().getDeptCd()) ? legacyObjects.getCustomer().getDeptCd() : "";
        data.setCreditCd(codflag);

        String realcity = !StringUtils.isEmpty(legacyObjects.getCustomer().getRealCtyCd()) ? legacyObjects.getCustomer().getRealCtyCd() : "";
        String bankNo = !StringUtils.isEmpty(legacyObjects.getCustomer().getBankNo()) ? legacyObjects.getCustomer().getBankNo() : "";
        if (StringUtils.isNotBlank(data.getRepTeamMemberNo())) {
          boolean reservedSrep = false;
          int salesRepInt = Integer.parseInt(data.getRepTeamMemberNo());
          int minRange = 220;
          int maxRange = 239;
          if (salesRepInt >= minRange && salesRepInt <= maxRange) {
            reservedSrep = true;
          }
          if (StringUtils.isNotBlank(bankNo)) {
            if (realcity.equals("755") && bankNo.substring(0, 1).equals("0") && !reservedSrep) {
              data.setMiscBillCd("NO");
            } else if (realcity.equals("756") && bankNo.substring(0, 1).equals("9") && !reservedSrep) {
              data.setMiscBillCd("IBM");
            } else if (realcity.equals("756") && bankNo.substring(0, 1).equals("0") && reservedSrep) {
              data.setMiscBillCd("WTC");
            } else {
              data.setMiscBillCd("NO");
            }
          } else {
            data.setMiscBillCd("NO");
          }
        }
      }
    } else {
      super.setDataValuesOnImport(admin, data, results, mainRecord);
    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (currentRecord != null) {

        address.setCustNm1(currentRecord.getCmrName1Plain());
        address.setCustNm2(currentRecord.getCmrName2Plain());
        address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
        // no addr std for israel
        if ("IL".equals(currentRecord.getCmrCountryLanded()) && (SystemLocation.ISRAEL.equals(currentRecord.getCmrIssuedBy())
            || SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(currentRecord.getCmrIssuedBy()))) {
          address.setAddrStdResult("X");
        }

        // CREATCMR-5741 - no addr std
        address.setAddrStdResult("X");
        address.setPairedAddrSeq(currentRecord.getTransAddrNo());
        address.setVat(currentRecord.getCmrTaxNumber());
        address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
        address.setTaxOffice(currentRecord.getCmrTaxOffice());
        address.setVat(currentRecord.getCmrTaxNumber());
        if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
            && "CTYA".equalsIgnoreCase(address.getId().getAddrType())) {
          address.getId().setAddrSeq("00006");
          address.setPairedAddrSeq("");
        }

        if ("D".equals(address.getImportInd()) && "CTYA".equalsIgnoreCase(address.getId().getAddrType())) {
          address.getId().setAddrSeq("00006");
        }

        if (!"ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
          address.setCustPhone("");
        }
      }
      if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType()) && StringUtils.isBlank(address.getSapNo())) {
        address.setImportInd("L");
      }
    } else {
      super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);
    }

  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD here
    } else {
      super.setAdminDefaultsOnCreate(admin);
    }
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        data.setCustPrefLang("B");
        data.setCollectionCd("TC0");
        data.setSpecialTaxCd("AA");
      } else {
        data.setCustPrefLang("E");
      }
      data.setCmrOwner("IBM");
    } else {
      super.setDataDefaultsOnCreate(data, entityManager);
    }

  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD here
    } else {
      super.appendExtraModelEntries(entityManager, mv, model);
    }
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        admin.setDelInd(null);
      }
    } else {
      super.handleImportByType(requestType, admin, data, importing);
    }
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (!StringUtils.isEmpty(data.getRepTeamMemberNo())) {
        switch (data.getCmrIssuingCntry()) {
        case SystemLocation.ISRAEL:
          request.setSORTL(data.getSalesBusOffCd() + "-" + data.getRepTeamMemberNo());
          break;
        }
      }
    } else {
      super.convertCoverageInput(entityManager, request, mainAddr, data);
    }
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD here
    } else {
      super.doBeforeAdminSave(entityManager, admin, cmrIssuingCntry);
    }
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        if (data.getSubIndustryCd() != null) {
          data.setEconomicCd("0" + data.getSubIndustryCd());
        }

        if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
          String scenario = !StringUtils.isEmpty(data.getCustSubGrp()) ? data.getCustSubGrp() : "";
          String kukla = !StringUtils.isEmpty(data.getCustClass()) ? data.getCustClass() : "";
          String kuklaVal = "";

          if (!StringUtils.isEmpty(scenario)) {
            switch (scenario) {
            case "GOVRN":
              kuklaVal = "13";
              break;
            case "INTER":
              kuklaVal = "81";
              break;
            case "INTSO":
              kuklaVal = "85";
              break;
            case "PRIPE":
              kuklaVal = "60";
              break;
            case "THDPT":
              kuklaVal = "11";
              break;
            default:
              kuklaVal = kukla;
              break;
            }
          }
          if (!StringUtils.isBlank(kuklaVal)) {
            data.setCustClass(kuklaVal);
          }
        } else {
          // UPDATE
          DataRdc rdcData = null;
          rdcData = LegacyDirectUtil.getOldData(entityManager, String.valueOf(data.getId().getReqId()));

          if (rdcData != null) {
            if (StringUtils.isEmpty(data.getCustClass()) && !StringUtils.isEmpty(rdcData.getCustClass())) {
              data.setCustClass(rdcData.getCustClass());
            }
          }

          if (StringUtils.isEmpty(data.getIsuCd()) && !StringUtils.isEmpty(rdcData.getIsuCd())) {
            data.setIsuCd(rdcData.getIsuCd());
          }

        }

        if (StringUtils.isEmpty(data.getCapInd())) {
          data.setCapInd("N");
        }
      }
    } else {
      super.doBeforeDataSave(entityManager, admin, data, cmrIssuingCntry);
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {

      DataPK pk = new DataPK();
      pk.setReqId(addr.getId().getReqId());
      Data data = entityManager.find(Data.class, pk);

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(addr.getId().getReqId());
      Admin admin = entityManager.find(Admin.class, adminPK);

      if (!"ZS01".equals(addr.getId().getAddrType())) {
        addr.setCustPhone("");
      }

      lockLandedCountry(entityManager, data, admin, addr);
      assignPairedSequence(entityManager, addr, admin.getReqType());
      clearPOBox(addr);

      AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
      if (addrRdc != null && StringUtils.isNotBlank(addrRdc.getImportInd()) && "L".equals(addrRdc.getImportInd())) {
        addr.setImportInd("L");
      }

      switch (cmrIssuingCntry) {
      case SystemLocation.ISRAEL:
        if ("CTYA".equals(addr.getId().getAddrType())) {
          // this is translated mailing, populate abbrev name and location
          LOG.debug("Computing Abbreviated Name/Location for Country Use A address of Israel. Request " + addr.getId().getReqId());
          if (data != null && admin.getReqType().equals("C")) {
            data.setAbbrevNm(addr.getCustNm1());
            if (data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
              data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
            }
            LOG.debug("- Abbreviated Name: " + data.getAbbrevNm());
            data.setAbbrevLocn(addr.getCity1());
            if (!"IL".equals(addr.getLandCntry())) {
              // for cross-border, use country name
              String country = DropdownListController.getDescription("LandedCountry", addr.getLandCntry(), SystemLocation.ISRAEL);
              if (!StringUtils.isEmpty(country)) {
                data.setAbbrevLocn(country.length() > 12 ? country.substring(0, 12) : country);
              }
            }
            if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
              data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
            }
            LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

            entityManager.merge(data);
            entityManager.flush();
          }
        }
        // Using above logic to change abbrvLoc to country for cross border
        if (!"IL".equals(addr.getLandCntry()) && !("CTYA".equals(addr.getId().getAddrType()))) {
          if (data != null && admin.getReqType().equals("C")) {
            String country = DropdownListController.getDescription("LandedCountry", addr.getLandCntry(), SystemLocation.ISRAEL);
            if (!StringUtils.isEmpty(country)) {
              data.setAbbrevLocn(country.length() > 12 ? country.substring(0, 12) : country);
            }
            entityManager.flush();
          }
        }
        break;
      }
    } else {
      super.doBeforeAddrSave(entityManager, addr, cmrIssuingCntry);
    }
  }

  private void clearPOBox(Addr addr) {
    String[] hiddenPOBoxAddrs = { "ZD01", "ZI01", "ZS02", "CTYC" };
    String addrType = addr.getId().getAddrType();
    if (Arrays.asList(hiddenPOBoxAddrs).contains(addrType)) {
      addr.setPoBox("");
    }
  }

  private void assignPairedSequence(EntityManager entityManager, Addr addr, String reqType) {

    if ("C".equals(reqType)) {
      assignPairedSeqForNewAddr(entityManager, addr);
    } else if ("U".equals(reqType)) {
      if ("Y".equals(addr.getImportInd()) || "L".equals(addr.getImportInd())) {

        AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
        if (addrRdc != null && StringUtils.isNotBlank(addrRdc.getPairedAddrSeq())) {
          addr.setPairedAddrSeq(addrRdc.getPairedAddrSeq());
        }
      } else if ("N".equals(addr.getImportInd())) {
        assignPairedSeqForNewAddr(entityManager, addr);
      }
    }
  }

  private void assignPairedSeqForNewAddr(EntityManager entityManager, Addr addr) {
    if (StringUtils.isEmpty(addr.getPairedAddrSeq()) && "CTYC".equals(addr.getId().getAddrType())) {

      Addr existingRecord = getAddrByAddrSeq(entityManager, addr.getId().getReqId(), "CTYC", addr.getId().getAddrSeq());
      // Prevent overwriting the assigned paired sequence when Copying in create
      // request
      if (existingRecord != null) {
        addr.setPairedAddrSeq(existingRecord.getPairedAddrSeq());
      } else {
        String pairedShipping = getLatestShipping(entityManager, addr.getId().getReqId());
        addr.setPairedAddrSeq(pairedShipping);
      }
    } else if (!"CTYC".equals(addr.getId().getAddrType())) {
      addr.setPairedAddrSeq("");
    }
  }

  private String getLatestShipping(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("GET.ADDRSEQ.LATEST.BY_REQID_TYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE_LOCAL", "ZD01");
    query.setParameter("ADDR_TYPE_TRANS", "CTYC");
    List<String> results = query.getResults(String.class);
    if (!results.isEmpty() && !results.get(0).isEmpty()) {
      return results.get(0);
    }
    return null;
  }

  private void lockLandedCountry(EntityManager entityManager, Data data, Admin admin, Addr addr) {
    boolean isLocalScenario = CUSTGRP_LOCAL.equals(data.getCustGrp());
    boolean isMailingOrTranslated = "CTYA".equals(addr.getId().getAddrType()) || "ZS01".equals(addr.getId().getAddrType());
    // local scenario - mailing and translated mailing lock landed country
    // to israel
    if ("C".equals(admin.getReqType()) && isLocalScenario && isMailingOrTranslated) {
      addr.setLandCntry("IL");
    } else if ("U".equals(admin.getReqType())) {
      String marker = addr.getBldg();
      // in Update requests, Mailing and Country Use A must be locked upon
      // import and not editable for both requester and processor.
      if (isMailingOrTranslated) {
        if ("SUPERUSER".equals(marker)) {
          addr.setBldg("");
        } else if ("Y".equals(addr.getImportInd())) {
          AddrRdc addrRdc = LegacyCommonUtil.getAddrRdcRecord(entityManager, addr);
          addr.setLandCntry(addrRdc.getLandCntry());
        }
      }
    }
  }

  @Override
  public void doBeforeDPLCheck(EntityManager entityManager, Data data, List<Addr> addresses) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // No DPL check for non-latin addresses
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
        for (Addr addr : addresses) {
          if (Arrays.asList("ZS01", "ZP01", "ZD01").contains(addr.getId().getAddrType())) {
            addr.setDplChkResult("N");
          }
        }
      }
    } else {
      super.doBeforeDPLCheck(entityManager, data, addresses);
    }
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      UpdatedDataModel update = null;
      super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

      // Type of Customer
      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCustClass(), newData.getCustClass())) {
        update = new UpdatedDataModel();
        update.setDataField(PageManager.getLabel(cmrCountry, "CustClass", "-"));
        update.setNewData(newData.getCustClass());
        update.setOldData(oldData.getCustClass());
        results.add(update);
      }
    } else {
      super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return Arrays.asList(ISRAEL_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    } else {
      super.skipOnSummaryUpdate(cntry, field);
    }
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
        data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
        entityManager.merge(data);
        entityManager.flush();
      }
    } else {
      super.doAfterImport(entityManager, admin, data);
    }
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "ADDR_TXT", "ADDR_TXT_2", "DEPT", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX", "CUST_PHONE"));
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && "C".equals(admin.getReqType())) {
        String abbrevNmValue = null;
        String abbrevLocnValue = null;

        String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", data.getId().getReqId());
        query.setParameter("ADDR_TYPE", "CTYA");
        List<String> record = query.getResults(String.class);
        if (record != null && !record.isEmpty()) {
          abbrevNmValue = record.get(0);
        }

        if (abbrevNmValue != null && abbrevNmValue.length() > 22) {
          abbrevNmValue = abbrevNmValue.substring(0, 22);
        }

        data.setAbbrevNm(abbrevNmValue);

        String sql2 = ExternalizedQuery.getSql("QUERY.ADDR.GET.CITY1.BY_REQID_ADDRTYP");
        PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
        query2.setParameter("REQ_ID", data.getId().getReqId());
        query2.setParameter("ADDR_TYPE", "CTYA");
        List<String> results = query2.getResults(String.class);
        if (results != null && !results.isEmpty()) {
          abbrevLocnValue = results.get(0);
        }

        if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
          abbrevLocnValue = abbrevLocnValue.substring(0, 12);
        }
        data.setAbbrevLocn(abbrevLocnValue);
        entityManager.merge(data);
        entityManager.flush();
      }
    } else {
      super.createOtherAddressesOnDNBImport(entityManager, admin, data);
    }
  }

  @Override
  protected String getAddressTypeByUse(String addressUse) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      switch (addressUse) {
      case "1":
        return "ZS01";
      case "2":
        return "ZP01";
      case "3":
        return "ZI01";
      case "4":
        return "ZD01";
      case "5":
        return "ZS02";
      case "A":
        return "CTYA";
      case "B":
        return "CTYB";
      case "C":
        return "CTYC";
      }
      return null;
    } else {
      return super.getAddressTypeByUse(addressUse);
    }
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      String addrSeqNum = null;

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(reqId);
      Admin admin = entityManager.find(Admin.class, adminPK);

      if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
        addrSeqNum = getAddrSeqForCreate(entityManager, reqId, addrType);
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        addrSeqNum = getAddrSeqForUpdate(entityManager, reqId);
      }
      return addrSeqNum;
    } else {
      return super.generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    }
  }

  private String getAddrSeqForCreate(EntityManager entityManager, long reqId, String addrType) {
    int candidateSeqNum = 0;
    String addrSeqNum = null;
    if (!StringUtils.isEmpty(addrType)) {
      candidateSeqNum = getFixedSequenceForMandatoryAddresses(addrType);
      addrSeqNum = getAvailableAddrSeqNum(entityManager, reqId, candidateSeqNum);
    }

    return addrSeqNum;
  }

  private String getAddrSeqForUpdate(EntityManager entityManager, long reqId) {
    String addrSeqNum = null;
    addrSeqNum = getAvailAddrSeqNumInclRdc(entityManager, reqId);

    return addrSeqNum;
  }

  private String getAvailableAddrSeqNum(EntityManager entityManager, long reqId, int candidateSeqNum) {
    int availSeqNum = 0;
    Set<Integer> allAddrSeq = getAllSavedSeqFromAddr(entityManager, reqId);
    if (allAddrSeq.contains(candidateSeqNum)) {
      availSeqNum = MANDATORY_ADDR_COUNT + 1;
      while (allAddrSeq.contains(availSeqNum)) {
        availSeqNum++;
      }
    } else {
      availSeqNum = candidateSeqNum;
    }
    return String.format("%05d", availSeqNum);
  }

  private String getAvailAddrSeqNumInclRdc(EntityManager entityManager, long reqId) {
    DataPK pk = new DataPK();
    pk.setReqId(reqId);
    Data data = entityManager.find(Data.class, pk);

    String cmrNo = data.getCmrNo();
    Set<Integer> allAddrSeqFromAddr = getAllSavedSeqFromAddr(entityManager, reqId);
    Set<Integer> allAddrSeqFromRdc = getAllSavedSeqFromRdc(entityManager, cmrNo);

    Set<Integer> mergedAddrSet = new HashSet<>();
    mergedAddrSet.addAll(allAddrSeqFromAddr);
    mergedAddrSet.addAll(allAddrSeqFromRdc);

    int candidateSeqNum = 1;

    int availSeqNum = 0;
    if (mergedAddrSet.contains(candidateSeqNum)) {
      availSeqNum = candidateSeqNum;
      while (mergedAddrSet.contains(availSeqNum)) {
        availSeqNum++;
        if (availSeqNum > MAX_ADDR_SEQ) {
          availSeqNum = 1;
        }
      }
    } else {
      availSeqNum = candidateSeqNum;
    }

    return String.format("%05d", availSeqNum);
  }

  private int getFixedSequenceForMandatoryAddresses(String addrType) {
    int addrSeq = 0;
    // fixed sequence for mandatory addresses
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
    } else if ("CTYA".equals(addrType)) {
      addrSeq = 6;
    } else if ("CTYB".equals(addrType)) {
      addrSeq = 7;
    } else if ("CTYC".equals(addrType)) {
      addrSeq = 8;
    }

    return addrSeq;
  }

  private Set<Integer> getAllSavedSeqFromAddr(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("GET.ADDRSEQ.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<Integer> results = query.getResults(Integer.class);

    Set<Integer> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(results);

    return addrSeqSet;
  }

  private Set<Integer> getAllSavedSeqFromRdc(EntityManager entityManager, String cmrNo) {
    String sql = ExternalizedQuery.getSql("GET.KNA1_ZZKV_SEQNO_DISTINCT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", SystemLocation.ISRAEL);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<Integer> resultsRDC = query.getResults(Integer.class);
    Set<Integer> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(resultsRDC);

    return addrSeqSet;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      String newAddrSeq = "";
      newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
      return newAddrSeq;
    } else {
      return super.generateModifyAddrSeqOnCopy(entityManager, addrType, reqId, oldAddrSeq, cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getMandtAddrTypeForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getOptionalAddrTypeForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getAdditionalAddrTypeForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return null;
    } else {
      return super.getReservedSeqForLDSeqGen(cmrIssuingCntry);
    }
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {

      Map<String, String> map = new HashMap<String, String>();
      map.put("##OriginatorName", "originatorNm");
      map.put("##SensitiveFlag", "sensitiveFlag");
      map.put("##INACType", "inacType");
      map.put("##ISU", "isuCd");
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
      map.put("##SpecialTaxCd", "specialTaxCd");
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
      map.put("##VAT", "vat");
      map.put("##CollectionCd", "collectionCd");
      map.put("##CMRNumber", "cmrNo");
      map.put("##Subindustry", "subIndustryCd");
      map.put("##EnterCMR", "enterCMRNo");
      map.put("##DisableAutoProcessing", "disableAutoProc");
      map.put("##Expedite", "expediteInd");
      map.put("##Affiliate", "affiliate");
      map.put("##BGLDERule", "bgRuleId");
      map.put("##ProspectToLegalCMR", "prospLegalInd");
      map.put("##ClientTier", "clientTier");
      map.put("##AbbrevLocation", "abbrevLocn");
      map.put("##SalRepNameNo", "repTeamMemberNo");
      map.put("##SAPNumber", "sapNo");
      map.put("##Department", "dept");
      map.put("##StreetAddress2", "addrTxt2");
      map.put("##Company", "company");
      map.put("##StreetAddress1", "addrTxt");
      map.put("##AbbrevName", "abbrevNm");
      map.put("##EmbargoCode", "embargoCd");
      map.put("##CustomerName1", "custNm1");
      map.put("##ISIC", "isicCd");
      map.put("##CustomerName2", "custNm2");
      map.put("##Enterprise", "enterprise");
      map.put("##PostalCode", "postCd");
      map.put("##SOENumber", "soeReqNo");
      map.put("##DUNS", "dunsNo");
      map.put("##BuyingGroupID", "bgId");
      map.put("##RequesterID", "requesterId");
      map.put("##GeoLocationCode", "geoLocationCd");
      map.put("##MembLevel", "memLvl");
      map.put("##RequestType", "reqType");
      map.put("##CustomerScenarioSubType", "custSubGrp");
      map.put("##EngineeringBo", "engineeringBo");
      map.put("##CodFlag", "creditCd");
      map.put("##CommercialFinanced", "commercialFinanced");
      map.put("##CustClass", "custClass");
      map.put("##TypeOfCustomer", "crosSubTyp");
      return map;
    } else {
      return super.getUIFieldIdMap();
    }

  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD", "ABBREV_NM",
        "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL",
        "BP_REL_TYPE", "CUST_CLASS", "CUST_PREF_LANG", "MISC_BILL_CD", "CREDIT_CD"));
    return fields;
  }

  @Override
  public boolean isTemplateFieldForDualMarked(String columnName, boolean isCrossBorder) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      boolean isDualMarked = false;
      // row.get
      // DTN: We need to remember what was set for the other fields,
      ArrayList<List<String>> dupPattern = null;

      if (columnName != null) {
        if (isCrossBorder) {
          dupPattern = (ArrayList<List<String>>) MASS_UPDT_DUP_ENTRY_MAP.get("crossb");
        } else {
          dupPattern = (ArrayList<List<String>>) MASS_UPDT_DUP_ENTRY_MAP.get("local");
        }

        for (int i = 0; i < dupPattern.size(); i++) {
          List<String> patterns = dupPattern.get(i);

          if (patterns.contains(columnName)) {
            isDualMarked = true;
            break;
          }
        }
      }
      return isDualMarked;
    } else {
      return super.isTemplateFieldForDualMarked(columnName, isCrossBorder);
    }
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return true;
    } else {
      return super.isNewMassUpdtTemplateSupported(issuingCountry);
    }
  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    Map<String, HashSet<String>> mapCmrSeq = new HashMap<String, HashSet<String>>();
    List<String> divCmrList = new ArrayList<String>();
    HashMap<String, HashMap<String, String>> postalCdValidationCache = new HashMap<String, HashMap<String, String>>();
    boolean isSheetEmpty = true;

    for (String name : IL_MASSUPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      LOG.debug("validating for sheet " + name);
      if (sheet != null) {
        TemplateValidation error = new TemplateValidation(name);
        if (name.equals(IL_MASSUPDATE_SHEET_NAMES[0])) {// data sheet
          validateDataSheet(mapCmrSeq, divCmrList, error, sheet, maxRows, country);
        } else {
          validateAddressSheet(name, mapCmrSeq, divCmrList, error, sheet, maxRows, postalCdValidationCache);
        }

        if (error.hasErrors()) {
          isSheetEmpty = false;
          validations.add(error);
        }
      }
    }
    // Compare Address and Translation
    // Mailing vs Country Use A
    compareAddressSheets(book.getSheet(IL_MASSUPDATE_SHEET_NAMES[1]), book.getSheet(IL_MASSUPDATE_SHEET_NAMES[6]), maxRows, validations);
    // Billing vs Country Use B
    compareAddressSheets(book.getSheet(IL_MASSUPDATE_SHEET_NAMES[2]), book.getSheet(IL_MASSUPDATE_SHEET_NAMES[7]), maxRows, validations);
    // Shipping vs Country Use C
    compareAddressSheets(book.getSheet(IL_MASSUPDATE_SHEET_NAMES[4]), book.getSheet(IL_MASSUPDATE_SHEET_NAMES[8]), maxRows, validations);

    if (isSheetEmpty && mapCmrSeq.size() == 0) {
      TemplateValidation sheetEmptyError = new TemplateValidation(IL_MASSUPDATE_SHEET_NAMES[0]);
      sheetEmptyError.addError(1, "<br>Template File", "Mass Update file does not contain any record to update.");
      validations.add(sheetEmptyError);
    }

  }

  private void validateDataSheet(Map<String, HashSet<String>> mapCmrSeq, List<String> divCmrList, TemplateValidation error, XSSFSheet sheet,
      int maxRows, String country) {
    XSSFRow row = null;
    String embargoCodes[] = { "D", "J", "@" };

    for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
      row = sheet.getRow(rowIndex);
      if (row != null) {
        // CMR No
        String cmrNo = validateColValFromCell(row.getCell(0));
        if (StringUtils.isBlank(cmrNo)) {
          error.addError(rowIndex + 1, "<br>CMR No.", "CMR Number is required.");
        } else if (isDivCMR(cmrNo, country)) {
          error.addError(rowIndex + 1, "<br>CMR No.",
              "Note the entered CMR Number is either cancelled, divestiture or doesn't exist. Please check the template and correct.");
          divCmrList.add(cmrNo);
        } else if (mapCmrSeq.containsKey(cmrNo)) {
          error.addError(rowIndex + 1, "<br>CMR No.", "Duplicate CMR No. It should be entered only once in Data Sheet.");
        } else {
          mapCmrSeq.put(cmrNo, new HashSet<String>());
        }
        // Tax Code
        String taxCode = validateColValFromCell(row.getCell(5));
        if (StringUtils.isNotBlank(taxCode) && "@".equals(taxCode)) {
          error.addError(rowIndex + 1, "<br>Tax Code", "@ value for Tax Code is not allowed.");
        }
        // Collection Code
        String collectionCode = validateColValFromCell(row.getCell(6));
        if (StringUtils.isNotBlank(collectionCode)) {
          if (!StringUtils.isAlphanumeric(collectionCode) && !"@".equals(collectionCode)) {
            error.addError(rowIndex + 1, "<br>Collection Code", "Collection Code should be alphanumeric only.");
          } else if (collectionCode.length() < 3 && !"@".equals(collectionCode)) {
            error.addError(rowIndex + 1, "<br>Collection Code", "Collection Code should be exactly 3 characters.");
          }
        }
        // COD Flag
        String codFlag = validateColValFromCell(row.getCell(7));
        if (StringUtils.isNotBlank(codFlag) && "@".equals(codFlag)) {
          error.addError(rowIndex + 1, "<br>COD Flag", "@ value for COD Flag is not allowed.");
        }
        // Embargo Code
        String embargoCode = validateColValFromCell(row.getCell(8));
        if (StringUtils.isNotBlank(embargoCode) && !(Arrays.asList(embargoCodes).contains(embargoCode))) {
          error.addError(rowIndex + 1, "<br>Embargo Code", "Invalid Embargo Code. Only D, J and @ are valid.");
        }
        String stcOrdBlk = validateColValFromCell(row.getCell(9));
        if (StringUtils.isNotBlank(stcOrdBlk) && StringUtils.isNotBlank(embargoCode)) {
          LOG.trace("Please fill either STC Order Block Code or Order Block Code ");
          error.addError((row.getRowNum() + 1), "Order Block Code", "Please fill either STC Order Block Code or Order Block Code ");
        }
        // Client Tier
        // String ctc = validateColValFromCell(row.getCell(11));
        // if (StringUtils.isNotBlank(ctc) && (!"Q".equals(ctc) &&
        // !"Y".equals(ctc) && !"@".equals(ctc))) {
        // error.addError(rowIndex + 1, "<br>Client Tier", "Invalid Client Tier.
        // Only uppercase Q, Y and @ are valid.");
        // }
        // Enterprise Number
        String enterpriseNumber = validateColValFromCell(row.getCell(13));
        if (StringUtils.isNotBlank(enterpriseNumber) && !StringUtils.isNumeric(enterpriseNumber)) {
          error.addError(rowIndex + 1, "<br>Enterprise Number", "Enterprise Number should be numeric only.");
        }
        // SBO
        String sbo = validateColValFromCell(row.getCell(14));
        if (StringUtils.isNotBlank(sbo) && !StringUtils.isNumeric(sbo)) {
          error.addError(rowIndex + 1, "<br>SBO", "SBO should be numeric only.");
        }
        // INAC/NAC
        String inac = validateColValFromCell(row.getCell(15));
        if (StringUtils.isNotBlank(inac)) {
          if (!StringUtils.isNumeric(inac) && !"@@@@".equals(inac)) {
            String firstTwoInacChar = StringUtils.substring(inac, 0, 2);
            String lastTwoInacChar = StringUtils.substring(inac, 2);

            Pattern upperCaseChars = Pattern.compile("^[A-Z]*$");
            Matcher matcherFirstTwo = upperCaseChars.matcher(firstTwoInacChar);

            Pattern digitsChars = Pattern.compile("^[0-9]+$");
            Matcher matcherLastTwo = digitsChars.matcher(lastTwoInacChar);

            if (!matcherFirstTwo.matches() || !matcherLastTwo.matches()) {
              error.addError(rowIndex + 1, "<br>INAC/NAC",
                  "INAC should be 4 digits or two letters (Uppercase Latin characters) followed by 2 digits.");
            }
          }
        }
        // Sales Rep
        String salesRep = validateColValFromCell(row.getCell(16));
        if (StringUtils.isNotBlank(salesRep) && !StringUtils.isAlphanumeric(salesRep)) {
          error.addError(rowIndex + 1, "<br>Sales Rep", "Sales Rep should be alphanumeric only.");
        }
        String errMsg = validateSalesRep(cmrNo, country, salesRep);
        if (StringUtils.isNotBlank(errMsg)) {
          error.addError(rowIndex + 1, "<br>Sales Rep", errMsg);
        }
        // KUKLA
        String isic = validateColValFromCell(row.getCell(3));
        String kukla = validateColValFromCell(row.getCell(18));
        if (StringUtils.isEmpty(isic)) {
          isic = "";
        }
        if (StringUtils.isEmpty(kukla)) {
          kukla = "";
        }

        String err = validateISICKukla(cmrNo, country, isic, kukla);

        if (StringUtils.isNotBlank(err)) {
          error.addError(rowIndex + 1, "<br>ISIC/KUKLA", err);
        }

        // phone
        if (row.getCell(17) != null) {
          row.getCell(17).setCellType(CellType.STRING);
        }
        String phoneNum = validateColValFromCell(row.getCell(17));
        if (StringUtils.isNotBlank(phoneNum)) {
          if (phoneNum.charAt(0) == '+' || phoneNum.charAt(0) == '-') {
            phoneNum = phoneNum.substring(1);
          }

          if (StringUtils.isNumeric(phoneNum)) {
            if (!row.getCell(17).getCellStyle().getCoreXf().getQuotePrefix()) {
              error.addError(rowIndex + 1, "<br>Phone Number", "Please add leading apostrophe (').");
            }
          }
        }

        // validate ISU and CTC combination
        String isuCd = validateColValFromCell(row.getCell(11));
        String ctc = validateColValFromCell(row.getCell(12));
        if ((StringUtils.isNotBlank(isuCd) && StringUtils.isBlank(ctc)) || (StringUtils.isNotBlank(ctc) && StringUtils.isBlank(isuCd))) {
          LOG.trace("The row " + (row.getRowNum() + 1) + ":Note that both ISU and CTC value needs to be filled..");
          error.addError((row.getRowNum() + 1), "Data Tab", ":Please fill both ISU and CTC value.<br>");
        } else if (!StringUtils.isBlank(isuCd) && "34".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"Q".equals(ctc)) {
            LOG.trace("The row " + (row.getRowNum() + 1)
                + ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.");
            error.addError((row.getRowNum() + 1), "Client Tier",
                ":Note that Client Tier should be 'Q' for the selected ISU code. Please fix and upload the template again.<br>");
          }
        } else if (!StringUtils.isBlank(isuCd) && "36".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"Y".equals(ctc)) {
            LOG.trace("The row " + (row.getRowNum() + 1)
                + ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.");
            error.addError((row.getRowNum() + 1), "Client Tier",
                ":Note that Client Tier should be 'Y' for the selected ISU code. Please fix and upload the template again.<br>");
          }
        } else if (!StringUtils.isBlank(isuCd) && "32".equals(isuCd)) {
          if (StringUtils.isBlank(ctc) || !"T".equals(ctc)) {
            LOG.trace("The row " + (row.getRowNum() + 1)
                + ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.");
            error.addError((row.getRowNum() + 1), "Client Tier",
                ":Note that Client Tier should be 'T' for the selected ISU code. Please fix and upload the template again.<br>");
          }
        } else if ((!StringUtils.isBlank(isuCd) && !Arrays.asList("32", "34", "36").contains(isuCd)) && !"@".equalsIgnoreCase(ctc)) {
          LOG.trace("Client Tier should be '@' for the selected ISU Code.");
          error.addError(row.getRowNum() + 1, "Client Tier", "Client Tier Value should always be @ for IsuCd Value :" + isuCd + ".<br>");
        }
      }
    }
  }

  private void validateAddressSheet(String sheetName, Map<String, HashSet<String>> mapCmrSeq, List<String> divCmrList, TemplateValidation error,
      XSSFSheet sheet, int maxRows, HashMap<String, HashMap<String, String>> postalCdValidationCache) {

    XSSFRow row = null;
    for (int rowIndex = 1; rowIndex <= maxRows; rowIndex++) {
      row = sheet.getRow(rowIndex);
      if (row != null) {
        // validate CMR No
        String cmrNo = validateColValFromCell(row.getCell(0));
        if (StringUtils.isBlank(cmrNo)) {
          error.addError(rowIndex + 1, "<br>CMR No.", "CMR number is required.");
        } else if (StringUtils.isNotBlank(cmrNo)) {
          if (!StringUtils.isNumeric(cmrNo)) {
            error.addError(rowIndex + 1, "<br>CMR No.", "CMR number should be numeric.");
          } else if (mapCmrSeq != null && !mapCmrSeq.containsKey(cmrNo)) {
            error.addError(rowIndex + 1, "<br>CMR No.", "CMR number is not in Data sheet.");
          } else if (divCmrList != null && divCmrList.contains(cmrNo)) {
            error.addError(rowIndex + 1, "<br>CMR No.",
                "Note the entered CMR number is either cancelled, divestiture or doesn't exist. Please check the template and correct.");
          }
        }
        // Validate Addr Sequence No
        String addrSeqNo = validateColValFromCell(row.getCell(1));
        if (StringUtils.isNotBlank(cmrNo)) {
          if (StringUtils.isBlank(addrSeqNo)) {
            error.addError(rowIndex + 1, "<br>Sequence", "Address Sequence No is required.");
          } else if (StringUtils.isNotBlank(addrSeqNo) && !StringUtils.isNumeric(addrSeqNo)) {
            error.addError(rowIndex + 1, "<br>Sequence", "Address Sequence No should be numeric.");
          } else {
            if (mapCmrSeq != null && mapCmrSeq.containsKey(cmrNo) && !(mapCmrSeq.get(cmrNo).add(addrSeqNo))) {
              error.addError(rowIndex + 1, "<br>CMR No - Sequence",
                  "Duplicate CMR No and Sequence combination. Already existing in one of the Address Sheet.");
            }
          }
        }
        // Validate required fields
        validateAddrRequiredFields(row, error, sheetName);

        // Hebrew validation
        boolean validateHebrewField = false;
        if (sheetName.equals("Mailing") || sheetName.equals("Billing") || sheetName.equals("Shipping")) {
          validateHebrewField = true;
        }
        // Validate Customer Name
        if (isHebrewFieldNotBlank(row.getCell(2))) {
          String custName = row.getCell(2).getRichStringCellValue().getString();
          if (validateHebrewField && !containsHebrewChar(custName)) {
            error.addError(rowIndex + 1, "<br>Customer Name", sheetName + " Customer Name should be in Hebrew.");
          } else if (!validateHebrewField && containsHebrewChar(custName)) {
            error.addError(rowIndex + 1, "<br>Customer Name", sheetName + " Customer Name should be in Latin characters.");
          }
        }
        // Validate Customer Name Con't
        if (isHebrewFieldNotBlank(row.getCell(3))) {
          String custNameCont = row.getCell(3).getRichStringCellValue().getString();
          if (validateHebrewField && !containsHebrewChar(custNameCont)) {
            error.addError(rowIndex + 1, "<br>Customer Name Con't", sheetName + " Customer Name Continuation should be in Hebrew.");
          } else if (!validateHebrewField && containsHebrewChar(custNameCont)) {
            error.addError(rowIndex + 1, "<br>Customer Name Con't", sheetName + " Customer Name Continuation should be in Latin characters.");
          }
        }
        // Validate Att Person
        if (isHebrewFieldNotBlank(row.getCell(4))) {
          String attPerson = row.getCell(4).getRichStringCellValue().getString();
          if (validateHebrewField && !containsHebrewChar(attPerson)) {
            error.addError(rowIndex + 1, "<br>Att. Person", sheetName + " Attention Person should be in Hebrew.");
          } else if (!validateHebrewField && containsHebrewChar(attPerson)) {
            error.addError(rowIndex + 1, "<br>Att. Person", sheetName + " Attention Person should be in Latin characters.");
          }
        }
        // Validate Street
        if (isHebrewFieldNotBlank(row.getCell(5))) {
          String street = row.getCell(5).getRichStringCellValue().getString();
          if (validateHebrewField && !containsHebrewChar(street)) {
            error.addError(rowIndex + 1, "<br>Street", sheetName + " Street should be in Hebrew.");
          } else if (!validateHebrewField && containsHebrewChar(street)) {
            error.addError(rowIndex + 1, "<br>Street", sheetName + " Street should be in Latin characters.");
          }
        }
        // Validate Address Con't
        XSSFCell addressContCell = getAddressCell(IL_MASSUPDATE_ADDR.ADDRCONT, row, sheetName);
        if (isHebrewFieldNotBlank(addressContCell)) {
          String addrCont = addressContCell.getRichStringCellValue().getString();
          if (validateHebrewField && !containsHebrewChar(addrCont)) {
            error.addError(rowIndex + 1, "<br>Address Con't", sheetName + " Address Continuation should be in Hebrew.");
          } else if (!validateHebrewField && containsHebrewChar(addrCont)) {
            error.addError(rowIndex + 1, "<br>Address Con't", sheetName + " Address Continuation should be in Latin characters.");
          }
        }
        // Validate City
        XSSFCell cityCell = getAddressCell(IL_MASSUPDATE_ADDR.CITY, row, sheetName);
        if (isHebrewFieldNotBlank(cityCell)) {
          String city = cityCell.getRichStringCellValue().getString();
          if (validateHebrewField && !containsHebrewChar(city)) {
            error.addError(rowIndex + 1, "<br>City", sheetName + " City should be in Hebrew.");
          } else if (!validateHebrewField && containsHebrewChar(city)) {
            error.addError(rowIndex + 1, "<br>City", sheetName + " City should be in Latin characters.");
          }
        }
        // end validate Mailing Billing Shipping hebrew fields

        // Validate Postal Code
        String postalCd = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.POSTCODE, row, sheetName));
        String landedCntry = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.LANDCOUNTRY, row, sheetName));

        if (validateHebrewField || (sheetName.equals("Installing") || sheetName.equals("EPL"))) {
          String errKna1 = validateMassKna1AddrSeqExist(cmrNo, addrSeqNo, sheetName);

          if (StringUtils.isNotBlank(errKna1)) {
            error.addError(rowIndex + 1, "<br>", errKna1);
          }

          String errDb2 = validateMassLegacyAddrSeqExist(cmrNo, addrSeqNo, sheetName);

          if (StringUtils.isNotBlank(errDb2)) {
            error.addError(rowIndex + 1, "<br>", errDb2);
          }
        }

        if (StringUtils.isNotBlank(landedCntry)) {
          // Check postalCode cache
          boolean isLandCountryInCache = postalCdValidationCache.containsKey(landedCntry);
          if (isLandCountryInCache && postalCdValidationCache.get(landedCntry).containsKey(postalCd)
              && !(postalCdValidationCache.get(landedCntry).get(postalCd)).equals("")) {
            error.addError(rowIndex + 1, "<br>Postal Code.", postalCdValidationCache.get(landedCntry).get(postalCd));
          } else if (!isLandCountryInCache || (isLandCountryInCache && !postalCdValidationCache.get(landedCntry).containsKey(postalCd))) {
            try {
              ValidationResult validation = checkPostalCode(landedCntry, postalCd);
              if (!validation.isSuccess()) {
                error.addError(rowIndex + 1, "<br>Postal Code.", validation.getErrorMessage());
                // put invalid postal code in cache
                if (isLandCountryInCache) {
                  (postalCdValidationCache.get(landedCntry)).put(postalCd, validation.getErrorMessage());
                } else {
                  HashMap<String, String> postalCdValErrorMap = new HashMap<String, String>();
                  postalCdValErrorMap.put(postalCd, validation.getErrorMessage());
                  postalCdValidationCache.put(landedCntry, postalCdValErrorMap);
                }
              }
            } catch (Exception e) {
              LOG.error("Error occured on connecting postal code validation service.");
              e.printStackTrace();
            }
          }
        }

        if ("IL".equals(landedCntry)) {
          if (StringUtils.isNotBlank(postalCd)) {
            if (postalCd.length() < 5 || postalCd.length() == 6) {
              error.addError(rowIndex + 1, "<br>Postal Code", sheetName + " Postal Code should be either 5 or 7 characters long.");
            }
          }
        }

        // Validate Postal Code+City
        String cityString = "";
        if (cityCell != null && cityCell.getRichStringCellValue() != null) {
          cityString = cityCell.getRichStringCellValue().getString();
        }

        if (StringUtils.isNotBlank(cityString) || StringUtils.isNotBlank(postalCd)) {
          cityString += postalCd;
          if (cityString.length() > 29) {
            error.addError(rowIndex + 1, "<br>", "Total computed length of City and Postal Code should not exceed 29 characters.");
          }
        }

        // Validate address lines flow through
        if (StringUtils.isNotBlank(landedCntry)) {
          XSSFCell custNameCell = getAddressCell(IL_MASSUPDATE_ADDR.CUSTNAME, row, sheetName);
          XSSFCell custNameContCell = getAddressCell(IL_MASSUPDATE_ADDR.CUSTNAMECONT, row, sheetName);
          XSSFCell attPersonCell = getAddressCell(IL_MASSUPDATE_ADDR.ATTPERSON, row, sheetName);
          XSSFCell streetCell = getAddressCell(IL_MASSUPDATE_ADDR.STREET, row, sheetName);
          XSSFCell poBoxCell = getAddressCell(IL_MASSUPDATE_ADDR.POBOX, row, sheetName);
          XSSFCell addrContCell = getAddressCell(IL_MASSUPDATE_ADDR.ADDRCONT, row, sheetName);
          XSSFCell postCdCell = getAddressCell(IL_MASSUPDATE_ADDR.POSTCODE, row, sheetName);

          if ("IL".equals(landedCntry)) {
            if (isHebrewFieldNotBlank(custNameCell) && isHebrewFieldNotBlank(custNameContCell) && isHebrewFieldNotBlank(attPersonCell)
                && isHebrewFieldNotBlank(streetCell) && isHebrewFieldNotBlank(addrContCell)
                && StringUtils.isNotBlank(validateColValFromCell(poBoxCell)) && isHebrewFieldNotBlank(postCdCell)) {
              error.addError(rowIndex + 1, "<br> ",
                  "Please remove value from one of the optional fields. You exceeded limitation which allows only 6 lines to be sent to DB2.");
            }
          } else {
            int ctr = 0;
            if (isHebrewFieldNotBlank(custNameCell)) {
              ctr += 1;
            }
            if (isHebrewFieldNotBlank(custNameContCell)) {
              ctr += 1;
            }
            if (isHebrewFieldNotBlank(attPersonCell)) {
              ctr += 1;
            }
            if (isHebrewFieldNotBlank(streetCell)) {
              ctr += 1;
            }
            if (isHebrewFieldNotBlank(addrContCell)) {
              ctr += 1;
            }
            if (StringUtils.isNotBlank(validateColValFromCell(poBoxCell))) {
              ctr += 1;
            }
            if (isHebrewFieldNotBlank(postCdCell)) {
              ctr += 1;
            }
            if (StringUtils.isNotBlank(landedCntry)) {
              ctr += 1;
            }
            if (ctr == 7) {
              error.addError(rowIndex + 1, "<br> ",
                  "Please remove value from one of the optional fields. You exceeded limitation which allows only 6 lines to be sent to DB2.");
            } else if (ctr == 8) {
              error.addError(rowIndex + 1, "<br> ",
                  "Please remove value from two of the optional fields. You exceeded limitation which allows only 6 lines to be sent to DB2.");
            }
          }
        }
      }
    }
  }

  private void validateAddrRequiredFields(XSSFRow row, TemplateValidation error, String sheetName) {
    // Check required fields
    boolean checkRequiredFields = false;
    if (row != null) {
      for (int i = 2; i <= 10; i++) {
        if (isHebrewFieldNotBlank(row.getCell(i))) {
          checkRequiredFields = true;
          break;
        }
      }

      if (checkRequiredFields) {
        // Customer Name
        if (!isHebrewFieldNotBlank(row.getCell(2))) {
          error.addError(row.getRowNum() + 1, "<br>Customer Name", "Customer Name is required when updating " + sheetName + " address.");
        }
        // Street
        if (sheetName.equals("Mailing") || sheetName.equals("Billing") || sheetName.equals("Country Use A (Mailing)")
            || sheetName.equals("Country Use B (Billing)")) {
          // Street or PO Box
          if (!isHebrewFieldNotBlank(row.getCell(5)) && StringUtils.isBlank(validateColValFromCell(row.getCell(6)))) {
            error.addError(row.getRowNum() + 1, "<br>Street-PO Box", "Street or PO Box is required when updating " + sheetName + " address.");
          }
        } else { // Installing, Shipping, EPL and Country Use C
          if (!isHebrewFieldNotBlank(row.getCell(5))) {
            error.addError(row.getRowNum() + 1, "<br>Street", "Street is required when updating " + sheetName + " address.");
          }
        }
        // Validate Address Con't
        if (isHebrewFieldNotBlank(getAddressCell(IL_MASSUPDATE_ADDR.ADDRCONT, row, sheetName)) && !isHebrewFieldNotBlank(row.getCell(5))) {
          error.addError(row.getRowNum() + 1, "<br>Address Con't", "Address Con't can only be filled if Street is filled.");
        }
        // City
        if (!isHebrewFieldNotBlank(getAddressCell(IL_MASSUPDATE_ADDR.CITY, row, sheetName))) {
          error.addError(row.getRowNum() + 1, "<br>City", "City is required when updating " + sheetName + " address.");
        }
        // Land Country
        if (!isHebrewFieldNotBlank(getAddressCell(IL_MASSUPDATE_ADDR.LANDCOUNTRY, row, sheetName))) {
          error.addError(row.getRowNum() + 1, "<br>Landed Country", "Landed Country is required when updating " + sheetName + " address.");
        }
      }
    }

  }

  private XSSFCell getAddressCell(IL_MASSUPDATE_ADDR addrField, XSSFRow row, String sheetName) {
    if (StringUtils.isNotBlank(sheetName)) {
      boolean adjustColIndex = false;
      if (sheetName.equals(IL_MASSUPDATE_SHEET_NAMES[3]) || sheetName.equals(IL_MASSUPDATE_SHEET_NAMES[4])
          || sheetName.equals(IL_MASSUPDATE_SHEET_NAMES[5]) || sheetName.equals(IL_MASSUPDATE_SHEET_NAMES[8])) {
        adjustColIndex = true;
      }
      switch (addrField) {
      case CMRNO:
        return row.getCell(0);
      case SEQNO:
        return row.getCell(1);
      case CUSTNAME:
        return row.getCell(2);
      case CUSTNAMECONT:
        return row.getCell(3);
      case ATTPERSON:
        return row.getCell(4);
      case STREET:
        return row.getCell(5);
      case POBOX:
        return row.getCell(6);
      case ADDRCONT:
        if (adjustColIndex) {
          return row.getCell(6);
        } else {
          return row.getCell(7);
        }
      case CITY:
        if (adjustColIndex) {
          return row.getCell(7);
        } else {
          return row.getCell(8);
        }
      case POSTCODE:
        if (adjustColIndex) {
          return row.getCell(8);
        } else {
          return row.getCell(9);
        }
      case LANDCOUNTRY:
        if (adjustColIndex) {
          return row.getCell(9);
        } else {
          return row.getCell(10);
        }
      }
    }

    return null;
  }

  private static ValidationResult checkPostalCode(String landedCountry, String postalCode) throws Exception {
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");

    PostalCodeValidateRequest zipRequest = new PostalCodeValidateRequest();
    zipRequest.setMandt(mandt);
    zipRequest.setPostalCode(postalCode);
    zipRequest.setSysLoc(SystemLocation.ISRAEL);
    zipRequest.setCountry(landedCountry);

    LOG.debug("Validating Postal Code " + postalCode + " for landedCountry " + landedCountry + " (mandt: " + mandt + " sysloc: 755" + ")");

    ValidatorClient client = CmrServicesFactory.getInstance().createClient(baseUrl, ValidatorClient.class);
    try {
      ValidationResult validation = client.executeAndWrap(ValidatorClient.POSTAL_CODE_APP_ID, zipRequest, ValidationResult.class);
      return validation;
    } catch (Exception e) {
      LOG.error("Error in postal code validation", e);
      return null;
    }
  }

  private static boolean isDivCMR(String cmrNo, String cntry) {
    boolean isDivestiture = true;
    String mandt = SystemConfiguration.getValue("MANDT");
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("IL.GET.ZS01KATR10");

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

  private static String validateMassKna1AddrSeqExist(String cmrNo, String seqNo, String addrType) {
    LOG.info("Israel MU validate rdc address sequence " + seqNo + " for CMR No. " + cmrNo);
    String errMessage = "";

    if (StringUtils.isNotBlank(cmrNo) && StringUtils.isNotBlank(seqNo) && StringUtils.isNotBlank(addrType)) {
      EntityManager entityManager = JpaManager.getEntityManager();
      if (entityManager != null) {
        String sql = ExternalizedQuery.getSql("IL.MASS.GET.KNA1.ADDR.SEQ");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("KATR6", SystemLocation.ISRAEL);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("ZZKV_CUSNO", cmrNo);
        query.setParameter("ZZKV_SEQNO", Integer.valueOf(seqNo));
        query.setParameter("ZZKV_SEQNO_PAD", seqNo);
        query.setForReadOnly(true);
        String result = query.getSingleResult(String.class);

        if (StringUtils.isBlank(result)) {
          errMessage = "CMR " + cmrNo + ": Address with sequence " + seqNo + " (" + addrType + ") "
              + " does not exist in RDC. Please raise single update for CMR " + cmrNo + " so the address can be inserted to RDC.";
        }
      }
    }
    return errMessage;
  }

  private static String validateMassLegacyAddrSeqExist(String cmrNo, String seqNo, String addrType) {
    LOG.info("Israel MU validate legacy address sequence " + seqNo + " for CMR No. " + cmrNo);
    String errMessage = "";

    if (StringUtils.isNotBlank(cmrNo) && StringUtils.isNotBlank(seqNo) && StringUtils.isNotBlank(addrType)) {
      EntityManager entityManager = JpaManager.getEntityManager();
      if (entityManager != null) {
        String sql = ExternalizedQuery.getSql("IL.MASS.GET.LEGACY.ADDR.SEQ");
        PreparedQuery query = new PreparedQuery(entityManager, sql);
        query.setParameter("RCYAA", SystemLocation.ISRAEL);
        query.setParameter("RCUXA", cmrNo);
        query.setParameter("SEQ", seqNo);
        query.setForReadOnly(true);
        String result = query.getSingleResult(String.class);

        if (StringUtils.isBlank(result)) {
          errMessage = "CMR " + cmrNo + ": Address with sequence " + seqNo + " (" + addrType + ") "
              + " does not exist in DB2. Please contact CMDE team to review the CMR.";
        }
      }
    }
    return errMessage;
  }

  private static String validateISICKukla(String cmrNo, String cntry, String usrIsic, String usrKukla) {
    LOG.info("Israel validate ISIC and KUKLA for CMR No. " + cmrNo);
    String errMessage = "";
    boolean mismatch = false;

    if (StringUtils.isNotBlank(cmrNo) && StringUtils.isNotBlank(cntry) && (StringUtils.isNotBlank(usrIsic) || StringUtils.isNotBlank(usrKukla))) {
      EntityManager entityManager = JpaManager.getEntityManager();

      Kna1 kna1 = LegacyDirectUtil.getIsicKukla(entityManager, cmrNo, cntry);
      if (kna1 != null) {
        String isic = StringUtils.isNotBlank(kna1.getZzkvSic()) ? kna1.getZzkvSic() : "";
        String kukla = StringUtils.isNotBlank(kna1.getKukla()) ? kna1.getKukla() : "";

        if (StringUtils.isNotBlank(isic) && StringUtils.isNotBlank(kukla)) {
          if (isic.equals("9500") && kukla.equals("60")) {
            if (StringUtils.isNotBlank(usrIsic) && !usrIsic.equals("9500")) {
              if (StringUtils.isBlank(usrKukla)) {
                mismatch = true;
              } else if (StringUtils.isNotBlank(usrKukla) && usrKukla.equals("60")) {
                mismatch = true;
              }
            } else if (StringUtils.isNotBlank(usrKukla) && !usrKukla.equals("60")) {
              if (StringUtils.isBlank(usrIsic)) {
                mismatch = true;
              } else if (StringUtils.isNotBlank(usrIsic) && usrIsic.equals("9500")) {
                mismatch = true;
              }
            }
          } else if (isic.equals("9500") && !kukla.equals("60")) {
            if (StringUtils.isNotBlank(usrKukla) && !usrKukla.equals("60")) {
              mismatch = true;
            }
          } else if (!isic.equals("9500") && kukla.equals("60")) {
            if (StringUtils.isNotBlank(usrIsic) && !usrIsic.equals("9500")) {
              mismatch = true;
            }
          } else {
            if ("9500".equals(usrIsic) && !"60".equals(usrKukla)) {
              errMessage = "KUKLA value should be 60 if ISIC is 9500.";
            } else if (!"9500".equals(usrIsic) && "60".equals(usrKukla)) {
              errMessage = "ISIC value should be 9500 if KUKLA is 60.";
            }
          }
        }
      }
    }
    if (mismatch) {
      errMessage = "ISIC/KUKLA Mismatch. CMR currently has ISIC 9500/KUKLA 60.  Please change both ISIC and KUKLA.";
    }
    return errMessage;
  }

  private static String validateSalesRep(String cmrNo, String cntry, String userSalesRep) {
    String errMessage = "";

    if (StringUtils.isNotBlank(cmrNo) && StringUtils.isNotBlank(cntry) && StringUtils.isNotBlank(userSalesRep)) {
      EntityManager entityManager = JpaManager.getEntityManager();

      CmrtCust cmrtCust = LegacyDirectUtil.getRealCountryCodeBankNumber(entityManager, cmrNo, cntry);
      if (cmrtCust != null) {
        String realCtyCd = StringUtils.isNotEmpty(cmrtCust.getRealCtyCd()) ? cmrtCust.getRealCtyCd() : "";
        String bankNoInitial = StringUtils.isNotEmpty(cmrtCust.getBankNo()) ? cmrtCust.getBankNo().substring(0, 1) : "";
        if (!bankNoInitial.equals("9")) {
          bankNoInitial = "0";
        }

        if (StringUtils.isNotEmpty(realCtyCd) && StringUtils.isNotEmpty(bankNoInitial)) {
          int salesRepInt = Integer.parseInt(userSalesRep);
          int minRange = 220;
          int maxRange = 239;

          if (realCtyCd.equals("755") && bankNoInitial.equals("0") || realCtyCd.equals("756") && bankNoInitial.equals("9")) {
            if (salesRepInt >= minRange && salesRepInt <= maxRange) {
              errMessage = "Invalid Sales Rep value.  Sales Rep cannot be from 000220-000239 range. Please change it.";
            }
          } else if (realCtyCd.equals("756") && bankNoInitial.equals("0")) {
            if (salesRepInt < minRange || salesRepInt > maxRange) {
              errMessage = "Invalid Sales Rep value. Sales Rep must be from 000220-000239 range. Please change it.";
            }
          }
        }
      }
    }
    return errMessage;
  }

  private boolean containsHebrewChar(String str) {
    int strLen = str.length();
    if (StringUtils.isNotBlank(str) && StringUtils.isNumeric(str)) {
      return true;
    } else {
      for (int i = 0; i < strLen; i++) {
        if (Character.UnicodeBlock.HEBREW.equals(Character.UnicodeBlock.of(str.codePointAt(i)))) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isHebrewFieldNotBlank(XSSFCell cell) {
    boolean isHebrewFieldNotBlank = false;
    if (cell != null) {
      if (CellType.NUMERIC == cell.getCellType()) {
        String numericCellStrVal = validateColValFromCell(cell);
        if (StringUtils.isNotBlank(numericCellStrVal)) {
          isHebrewFieldNotBlank = true;
        }
      } else {
        if (cell.getRichStringCellValue() != null && StringUtils.isNotBlank(cell.getRichStringCellValue().getString())) {
          isHebrewFieldNotBlank = true;
        }
      }
    }

    return isHebrewFieldNotBlank;
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD Here
    } else {
      super.addSummaryUpdatedFieldsForAddress(service, cmrCountry, addrTypeDesc, sapNumber, addr, results, entityManager);
    }
  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      // Handle LD Here
    } else {
      super.handleSOFSequenceImport(records, cmrIssuingCntry);
    }
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    String processingType = PageManager.getProcessingType(SystemLocation.ISRAEL, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      return true;
    } else {
      return super.hasChecklist(cmrIssiungCntry);
    }
  }

  private void compareAddressSheets(XSSFSheet sheet1, XSSFSheet sheet2, int maxRows, List<TemplateValidation> validations) {
    if (sheet1 != null && sheet2 != null) {
      int firstRow1 = sheet1.getFirstRowNum();
      TemplateValidation error = new TemplateValidation(sheet1.getSheetName() + " - " + sheet2.getSheetName());
      for (int i = firstRow1 + 1; i <= maxRows; i++) {
        XSSFRow rowA = sheet1.getRow(i);
        XSSFRow rowB = sheet2.getRow(i);
        if (rowA == null && rowB == null) {
          i = maxRows;
          break;
        }

        if (!compareTwoRows(rowA, rowB, error)) {
          error.addError(i + 1, "<br>Mismatch",
              "Same fields needs to be filled for both " + sheet1.getSheetName() + " and " + sheet2.getSheetName() + " address.");
        } else {
          // check digits chars vs the translation address
          // Street
          String street1 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.STREET, rowA, sheet1.getSheetName()));
          String street2 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.STREET, rowB, sheet2.getSheetName()));
          if (!isNumericValueEqual(street1, street2)) {
            error.addError(i + 1, "<br>Street", "Mismatch numeric Street value.");
          }
          // PO Box
          if (!IL_MASSUPDATE_SHEET_NAMES[4].equals(sheet1.getSheetName())) {
            String poBox1 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.POBOX, rowA, sheet1.getSheetName()));
            String poBox2 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.POBOX, rowB, sheet2.getSheetName()));
            if (!isNumericValueEqual(poBox1, poBox2)) {
              error.addError(i + 1, "<br>PO Box", "Mismatch numeric PO Box value.");
            }
          }
          // Address Cont
          String addrCont1 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.ADDRCONT, rowA, sheet1.getSheetName()));
          String addrCont2 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.ADDRCONT, rowB, sheet2.getSheetName()));
          if (!isNumericValueEqual(addrCont1, addrCont2)) {
            error.addError(i + 1, "<br>Address Cont", "Mismatch numeric Address Cont value.");
          }
          // Postal Code
          String postalCd1 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.POSTCODE, rowA, sheet1.getSheetName()));
          String postalCd2 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.POSTCODE, rowB, sheet2.getSheetName()));
          if (!isNumericValueEqual(postalCd1, postalCd2)) {
            error.addError(i + 1, "<br>Postal Code", "Mismatch numeric Postal Code value.");
          }
          // Landed Country
          String landedCntry1 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.LANDCOUNTRY, rowA, sheet1.getSheetName()));
          String landedCntry2 = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.LANDCOUNTRY, rowB, sheet2.getSheetName()));
          if (StringUtils.isNotBlank(landedCntry1) && StringUtils.isNotBlank(landedCntry2) && !landedCntry1.equals(landedCntry2)) {
            error.addError(i + 1, "<br>Landed Country", "Mismatch Landed Country value.");
          }

          if (IL_MASSUPDATE_SHEET_NAMES[6].equals(sheet2.getSheetName()) || IL_MASSUPDATE_SHEET_NAMES[7].equals(sheet2.getSheetName())
              || IL_MASSUPDATE_SHEET_NAMES[8].equals(sheet2.getSheetName())) {
            String localSeqNumFromTemplate = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.SEQNO, rowA, sheet1.getSheetName()));
            String transSeqNumFromTemplate = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.SEQNO, rowB, sheet2.getSheetName()));

            String cmrNo = validateColValFromCell(getAddressCell(IL_MASSUPDATE_ADDR.CMRNO, rowB, sheet2.getSheetName()));

            EntityManager entityManager = JpaManager.getEntityManager();
            CmrtAddr transLegacyAddr = LegacyDirectUtil.getLegacyAddrBySeqNo(entityManager, cmrNo, SystemLocation.ISRAEL,
                (String.format("%05d", Integer.parseInt(transSeqNumFromTemplate))));

            boolean isSeqPairMismatch = false;
            boolean isInputMismatchWithLegacyPair = false;
            boolean isUseABC = checkIfAddrIsUseABC(transLegacyAddr);

            if (isUseABC) {
              isSeqPairMismatch = !checkIfLegacyPairMatches(entityManager, transLegacyAddr,
                  (String.format("%05d", Integer.parseInt(localSeqNumFromTemplate))));
            }

            if (transLegacyAddr == null || isInputMismatchWithLegacyPair || !isUseABC) {
              isSeqPairMismatch = true;
            }

            if (isSeqPairMismatch) {
              String errorMsg = "Please check and fix sequences of paired addresses " + sheet1.getSheetName() + " (" + localSeqNumFromTemplate
                  + ") and " + sheet2.getSheetName() + " (" + transSeqNumFromTemplate + "). " + "Sequences entered are not a matching pair.";
              error.addError(i + 1, "<br>Address Sequence", errorMsg);
            }
          }
        }
      }
      if (error.hasErrors()) {
        validations.add(error);
      }
    }
  }

  private boolean checkIfLegacyPairMatches(EntityManager entityManager, CmrtAddr legacyAddr, String localSeqNumFromTemplate) {
    if (StringUtils.isNotEmpty(legacyAddr.getAddrLineO())) {
      return legacyAddr.getAddrLineO().equals(localSeqNumFromTemplate);
    } else {
      String defaultPairSeq = null;
      if ("Y".equals(legacyAddr.getIsAddressUseA())) {
        defaultPairSeq = getDefaultPairSequence(entityManager, legacyAddr.getId().getCustomerNo(), legacyAddr.getId().getSofCntryCode(),
            "GET.ADDRNO.BY_MAILING");
      } else if ("Y".equals(legacyAddr.getIsAddressUseB())) {
        defaultPairSeq = getDefaultPairSequence(entityManager, legacyAddr.getId().getCustomerNo(), legacyAddr.getId().getSofCntryCode(),
            "GET.ADDRNO.BY_BILLING");
      } else if ("Y".equals(legacyAddr.getIsAddressUseC())) {
        defaultPairSeq = getDefaultPairSequence(entityManager, legacyAddr.getId().getCustomerNo(), legacyAddr.getId().getSofCntryCode(),
            "GET.ADDRNO.BY_SHIPPING");
      }
      if (StringUtils.isNotBlank(defaultPairSeq) && defaultPairSeq.equals(localSeqNumFromTemplate)) {
        return true;
      }
    }
    return false;
  }

  private boolean checkIfAddrIsUseABC(CmrtAddr legacyAddr) {
    if (legacyAddr == null) {
      return false;
    }

    boolean isUseA = "Y".equals(legacyAddr.getIsAddressUseA());
    boolean isUseB = "Y".equals(legacyAddr.getIsAddressUseB());
    boolean isUseC = "Y".equals(legacyAddr.getIsAddressUseC());

    return isUseA || isUseB || isUseC;
  }

  private String getDefaultPairSequence(EntityManager entityManager, String cmrNo, String country, String sqlQuery) {
    String sql = ExternalizedQuery.getSql(sqlQuery);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", country);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(true);

    String addrno = query.getSingleResult(String.class);

    return addrno;
  }

  private boolean compareTwoRows(XSSFRow rowA, XSSFRow rowB, TemplateValidation error) {
    boolean isRowEqual = true;
    if (rowA == null && rowB == null) {
      return isRowEqual;
    } else if ((rowA != null && rowB == null) || (rowA == null && rowB != null)) {
      return false;
    } else if (rowA != null && rowB != null) {
      // compare CMR No if same
      String cmrNoA = validateColValFromCell(rowA.getCell(0));
      String cmrNoB = validateColValFromCell(rowB.getCell(0));
      if ((StringUtils.isNotBlank(cmrNoA) && StringUtils.isNotBlank(cmrNoB) && !cmrNoA.equals(cmrNoB))
          || (StringUtils.isBlank(cmrNoA) && StringUtils.isNotBlank(cmrNoB)) || (StringUtils.isNotBlank(cmrNoA) && StringUtils.isBlank(cmrNoB))) {
        error.addError(rowA.getRowNum() + 1, "<br>CMR No.", "CMR No. does not match.");
        return false;
      }

      // Iterate other fields if filled-out the same
      int lastCell = rowA.getLastCellNum();
      for (int i = 1; i <= lastCell; i++) {
        String currCellA = validateColValFromCell(rowA.getCell(i));
        String currCellB = validateColValFromCell(rowB.getCell(i));
        if ((StringUtils.isNotBlank(currCellA) && StringUtils.isBlank(currCellB))
            || (StringUtils.isBlank(currCellA) && StringUtils.isNotBlank(currCellB))) {
          // always return true for name cont and address cont
          if (i == 3 || i == 7) {
            return true;
          }
          return false;
        }
      }
    }

    return isRowEqual;
  }

  private boolean isNumericValueEqual(String strA, String strB) {
    boolean isNumValEqual = true;

    if (StringUtils.isNotBlank(strA) && StringUtils.isNotBlank(strB)) {
      Integer[] arrIntA = getSortedDigitsFromString(strA);
      Integer[] arrIntB = getSortedDigitsFromString(strB);
      // compare numbers
      if (!Arrays.equals(arrIntA, arrIntB)) {
        return false;
      }
    }

    return isNumValEqual;
  }

  private Integer[] getSortedDigitsFromString(String str) {
    return Arrays.stream(str.replaceAll("[^0-9]", " ").trim().split(" ")).filter(StringUtils::isNotBlank).map(s -> Integer.parseInt(s)).sorted()
        .toArray(Integer[]::new);
  }

  private Addr getAddrByAddrSeq(EntityManager entityManager, long reqId, String addrType, String addrSeq) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GETRECORDS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    query.setParameter("ADDR_SEQ", addrSeq);
    List<Addr> addrList = query.getResults(Addr.class);

    if (!addrList.isEmpty()) {
      return addrList.get(0);
    }
    return null;
  }

  private String getCountryCodeByDesc(EntityManager entityManager, boolean isLocal, String countryDesc) {
    String sql = null;
    if (isLocal) {
      sql = ExternalizedQuery.getSql("IL.GET.COUNGTRYCODE_BYLOCALCOUNTRYDESC");
    } else {
      sql = ExternalizedQuery.getSql("IL.GET.COUNTRYCODE_BYCOUNTRYDESC");
    }

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY_DESC", countryDesc);
    String countryCode = query.getSingleResult(String.class);
    return countryCode;
  }

  @Override
  public boolean checkCopyToAdditionalAddress(EntityManager entityManager, Addr copyAddr, String cmrIssuingCntry) throws Exception {
    if (copyAddr != null && copyAddr.getId() != null) {
      Admin adminRec = LegacyCommonUtil.getAdminByReqId(entityManager, copyAddr.getId().getReqId());
      if (adminRec != null) {
        boolean isCreateReq = CmrConstants.REQ_TYPE_CREATE.equals(adminRec.getReqType());
        if (isCreateReq && copyAddr.getId().getAddrSeq().compareTo("00009") >= 0) {
          assignPairedSeqForNewAddr(entityManager, copyAddr);
          entityManager.merge(copyAddr);
          entityManager.flush();
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean shouldAutoDplSearchMassUpdate() {
    return true;
  }

}