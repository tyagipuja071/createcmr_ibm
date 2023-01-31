/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
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
import com.ibm.cio.cmr.request.entity.BaseEntity;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.CmrtCustExt;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.entity.KunnrExt;
import com.ibm.cio.cmr.request.entity.Sadr;
import com.ibm.cio.cmr.request.entity.SuppCntry;
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
import com.ibm.cio.cmr.request.util.MQProcessUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.legacy.LegacyCommonUtil;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Import Converter for EMEA
 * 
 * @author Priy Ranjan
 * 
 */
public class CyprusHandler extends BaseSOFHandler {

  private static final Logger LOG = Logger.getLogger(CyprusHandler.class);

  protected static final String[] CY_MASS_UPDATE_SHEET_NAMES = { "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address (Update)", "EPL Address", "Data" };

  private static Map<String, List<List<String>>> MASS_UPDT_DUP_ENTRY_MAP = new HashMap<String, List<List<String>>>();

  private static final String[] UKI_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "CAP", "CMROwner", "Company", "CustClassCode", "LocalTax1",
      "LocalTax2", "Enterprise", "SearchTerm", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office",
      "Floor", "Building", "County", "City2" };
  private static final String[] ISRAEL_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode", "LocalTax1",
      "LocalTax2", "SearchTerm", "SitePartyID", "StreetAddress2", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office",
      "Floor", "Building", "County", "City2", "CustomerName2" };

  private static final String[] GREECE_CYPRUS_TURKEY_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "Affiliate", "Company", "CAP", "CMROwner", "CustClassCode",
      "LocalTax1", "LocalTax2", "SearchTerm", "SitePartyID", "Division", "POBoxCity", "POBoxPostalCode", "CustFAX", "TransportZone", "Office",
      "Floor", "Building", "County", "City2", "Department", "INACType", "SalRepNameNo" };

  private static final List<String> EMEA_COUNTRY_VAL = Arrays.asList(SystemLocation.UNITED_KINGDOM, SystemLocation.IRELAND, SystemLocation.ISRAEL,
      SystemLocation.TURKEY, SystemLocation.GREECE, SystemLocation.CYPRUS, SystemLocation.ITALY);

  private static final List<String> INVALID_CUST_FOR_COUNTRY = Arrays.asList(SystemLocation.UNITED_KINGDOM, SystemLocation.IRELAND);

  public static final String EMEA_MASSCHANGE_TEMPLATE_ID = "EMEA";

  private static final List<String> ENABLE_MASSCHANGE_AUTO_TEMPLATE = Arrays.asList(SystemLocation.SPAIN, SystemLocation.UNITED_KINGDOM,
      SystemLocation.IRELAND);

  private static final String UK_INFSL_ABBREVNM_SUFFIX = "ZG33";

  private static final String IR_INFSL_ABBREVNM_SUFFIX = "ZG35";

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();

  public static final String[] HRDWRE_MSTR_FLAG_ADDRS = { "ZI01", "ZS01" };

  // *abner revert begin
  // protected static final String[] LD_MASS_UPDATE_SHEET_NAMES = { "Local Lang
  // Translation Sold-To", "Billing Address",
  // "Mailing Address", "Installing Address", "Shipping Address (Update)", "EPL
  // Address", "Sold-To Address",
  // "Install-At Address", "Ship-To Address" };
  protected static final String[] LD_MASS_UPDATE_SHEET_NAMES = { "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address (Update)", "EPL Address" };
  // *abner revert end

  // CMR-1728
  protected static final String[] TR_MASS_UPDATE_SHEET_NAMES = { "Installing Address", "Shipping Address", "EPL Address" };

  protected static final String[] GR_MASS_UPDATE_SHEET_NAMES = { "Local Lang translation Sold-to", "Sold To Address", "Ship To Address",
      "Install At Address" };

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

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      // only add zs01 equivalent for create by model
      FindCMRRecordModel record = mainRecord;
      // name4 in rdc = Attn on SOF
      record.setCmrDept(record.getCmrName4());
      // record.setCmrName4(null);
      if (SystemLocation.UNITED_KINGDOM.equals(record.getCmrIssuedBy()) || SystemLocation.IRELAND.equals(record.getCmrIssuedBy())) {
        record.setCmrStreetAddressCont(record.getCmrName4());
        record.setCmrName3(record.getCmrName3());
      } else {
        // name3 in rdc = Address Con't on SOF
        record.setCmrStreetAddressCont(record.getCmrName3());
        record.setCmrName3(null);
        if (record.getCmrName4().startsWith("ATT")) {
          record.setCmrName4(LegacyCommonUtil.removeATT(record.getCmrName4()));
        }
      }

      if (!StringUtils.isBlank(record.getCmrPOBox())) {
        String poBox = LegacyCommonUtil.doFormatPoBox(record.getCmrPOBox());
        if (poBox.length() > 5) {
          record.setCmrPOBox(poBox.substring(0, 5));
        } else {
          record.setCmrPOBox(poBox);
        }
      }
      if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
        record.setCmrAddrSeq("00001");
      } else {
        // CREATCMR-6139 Prospect CMR Conversion - address sequence A
        if (StringUtils.isNotBlank(reqEntry.getCmrIssuingCntry()) && "666".equals(reqEntry.getCmrIssuingCntry())
            && StringUtils.isNotBlank(record.getCmrNum()) && record.getCmrNum().startsWith("P") && record.getCmrAddrSeq().equals("A")) {
          record.setCmrAddrSeq("00001");
        } else {
          record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
        }
      }

      if (SystemLocation.CYPRUS.equals(record.getCmrIssuedBy())) {
        LOG.debug("CY Nickname: " + record.getCmrName2Plain());
        record.setCmrName2Plain(record.getCmrName2Plain());
        record.setCmrDept(null);
      }

      if (SystemLocation.GREECE.equals(record.getCmrIssuedBy())) {
        LOG.debug("GR Nickname: " + record.getCmrName2Plain());
        record.setCmrName2Plain(record.getCmrName2Plain());
        record.setCmrDept(null);
        if (!StringUtils.isBlank(record.getCmrPOBox())) {
          record.setCmrPOBox(record.getCmrPOBox());
        }
      }

      if (SystemLocation.TURKEY.equals(record.getCmrIssuedBy())) {
        record.setCmrName2Plain(record.getCmrName2Plain());
        record.setCmrDept(record.getCmrCity2());
      }

      if (SystemLocation.ISRAEL.equals(record.getCmrIssuedBy()) || SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(record.getCmrIssuedBy())) {
        // imported ZS01 will be Country Use A, and imported Country Use
        // A will
        // be ZS01 on the tool. This
        // will be reversed on #doAfterImport
        FindCMRRecordModel mailingSOF = createAddress(entityManager, record.getCmrIssuedBy(), CmrConstants.ADDR_TYPE.CTYA.toString(), "Mailing",
            new HashMap<String, FindCMRRecordModel>());
        if (mailingSOF != null) {
          converted.add(mailingSOF);
        }
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
          FindCMRRecordModel addr = new FindCMRRecordModel();

          // map RDc - SOF - CreateCMR by sequence no
          for (FindCMRRecordModel record : source.getItems()) {
            seqNo = record.getCmrAddrSeq();
            if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
              sofUses = this.legacyObjects.getUsesBySequenceNo(seqNo);
              for (String sofUse : sofUses) {
                addrType = getAddressTypeByUse(sofUse);
                if (!StringUtils.isEmpty(addrType)) {
                  addr = cloneAddress(record, addrType);
                  LOG.trace("Adding address type " + addrType + " for sequence " + seqNo);

                  // name3 in rdc = Address Con't on SOF
                  addr.setCmrStreetAddressCont(record.getCmrName3());
                  addr.setCmrName3(null);

                  addr.setCmrName2Plain(!StringUtils.isEmpty(record.getCmrName2Plain()) ? record.getCmrName2Plain() : record.getCmrName4());
                  if (!StringUtils.isBlank(record.getCmrPOBox())) {
                    String poBox = LegacyCommonUtil.doFormatPoBox(record.getCmrPOBox());
                    addr.setCmrPOBox(poBox);
                  }
                  if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                    addr.setCmrAddrSeq("00001");
                  }

                  converted.add(addr);
                }
              }
            }
          }
        }
      } else {
        // old SOF import flow

        Map<String, FindCMRRecordModel> zi01Map = new HashMap<String, FindCMRRecordModel>();

        boolean hasInstalling = false;
        boolean hasBilling = false;
        // parse the rdc records
        String cmrCountry = mainRecord != null ? mainRecord.getCmrIssuedBy() : "";
        if (SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(cmrCountry)) {
          cmrCountry = SystemLocation.ISRAEL;
        }
        if (source.getItems() != null) {
          for (FindCMRRecordModel record : source.getItems()) {

            if (!SystemLocation.ITALY.equals(record.getCmrIssuedBy())) {
              if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(record.getCmrAddrTypeCode())) {
                LOG.trace("Shipping from RDc ignored. Will get from SOF");
                this.rdcShippingRecords.add(record);
                continue;
              }
            } else {
              if (CmrConstants.ADDR_TYPE.ZI01.toString().equals(record.getCmrAddrTypeCode())) {
                if (((!StringUtils.isEmpty(mainRecord.getCmrNum())) && (!StringUtils.isEmpty(record.getCmrCompanyNo())))
                    && (mainRecord.getCmrNum().equalsIgnoreCase(record.getCmrCompanyNo()))) {
                  LOG.debug("ZI01:For Italy::CMRNUM==COMPANYNO" + mainRecord.getCmrNum() + "==" + record.getCmrCompanyNo());
                  // In cases where ZI01- NODE1 and ZZKV_CUSNO
                  // are the same that
                  // means that both Sold-To and Install-At
                  // are Installing
                  // Addresses. That means we only need to
                  // import 1 (the
                  // Sold-To)
                  // and hide the others.

                  record.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZS01.toString());

                } else {
                  // It is a different address from the
                  // installing and should be
                  // also imported as a Company address

                  // Import ZI01 from Anagrafico.

                  continue;
                }
              }

              if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(record.getCmrAddrTypeCode())) {
                if (((!StringUtils.isEmpty(mainRecord.getCmrNum())) && (!StringUtils.isEmpty(record.getCmrCompanyNo())))
                    && (mainRecord.getCmrNum().equalsIgnoreCase(record.getCmrCompanyNo()))) {
                  LOG.debug("ZP01:For Italy::CMRNUM==COMPANYNO" + mainRecord.getCmrNum() + "==" + record.getCmrCompanyNo());
                  // In cases where ZP01- NODE1 and ZZKV_CUSNO
                  // are the same that
                  // means that both Sold-To and Install-At
                  // are Installing
                  // Addresses. That means we only need to
                  // import 1 (the
                  // Sold-To)
                  // and hide the others.

                  record.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.ZS01.toString());

                } else {
                  // It is a different address from the
                  // installing
                  // and should be also imported as a Mailing
                  // address (ZP01).

                  // Import ZP01 from Anagrafico or RDc
                  converted.add(record);

                }
              }
              if (CmrConstants.ADDR_TYPE.ZS02.toString().equals(record.getCmrAddrTypeCode())) {
                LOG.debug("ZS02: For Italy::Import ZS02 From RDc");
                // Import ZS02 From RDc
                converted.add(record);

              }
            }

            if ((SystemLocation.GREECE.equals(record.getCmrIssuedBy()) || SystemLocation.CYPRUS.equals(record.getCmrIssuedBy())
                || SystemLocation.TURKEY.equals(record.getCmrIssuedBy()))
                && !CmrConstants.ADDR_TYPE.ZS01.toString().equals(record.getCmrAddrTypeCode())) {
              // greece/cyprus/turkey only ZS01; ZP01 and ZD01
              // from SOF directly
              continue;
            }

            // name4 in rdc = Attn on SOF
            record.setCmrDept(record.getCmrName4());
            record.setCmrName4(null);
            // name3 in rdc = Address Con't on SOF
            record.setCmrStreetAddressCont(record.getCmrName3());
            record.setCmrName3(null);

            if (!StringUtils.isBlank(record.getCmrPOBox())) {
              if (SystemLocation.UNITED_KINGDOM.equals(record.getCmrIssuedBy()) || SystemLocation.IRELAND.equals(record.getCmrIssuedBy())) {
                record.setCmrPOBox(record.getCmrPOBox());
              } else {
                record.setCmrPOBox("PO BOX " + record.getCmrPOBox());
              }
            }

            if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
              record.setCmrAddrSeq("00001");
            } else {
              record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
            }

            if (SystemLocation.GREECE.equals(record.getCmrIssuedBy()) || SystemLocation.CYPRUS.equals(record.getCmrIssuedBy())) {
              LOG.debug("GR/CY Nickname: " + record.getCmrName2Plain());
              record.setCmrName2Plain(record.getCmrName2Plain());
              record.setCmrDept(null);
            }

            if (SystemLocation.TURKEY.equals(record.getCmrIssuedBy())) {
              record.setCmrName2Plain(record.getCmrName2Plain());
              record.setCmrDept(record.getCmrCity2());
            }

            if (!SystemLocation.ITALY.equals(record.getCmrIssuedBy())) {
              if (CmrConstants.ADDR_TYPE.ZI01.toString().equals(record.getCmrAddrTypeCode())) {
                hasInstalling = true;
                zi01Map.put(record.getCmrAddrSeq(), record);

              }
            }

            if (SystemLocation.ISRAEL.equals(record.getCmrIssuedBy()) || SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(record.getCmrIssuedBy())) {
              record.setCmrStreetAddressCont(null);
              if (CmrConstants.ADDR_TYPE.ZP01.toString().equals(record.getCmrAddrTypeCode())) {
                record.setCmrAddrTypeCode(CmrConstants.ADDR_TYPE.CTYB.toString());
                hasBilling = true;
              }
            }

            converted.add(record);

          }
        }

        // add the missing records
        if (mainRecord != null) {

          FindCMRRecordModel record = null;

          // import all shipping from SOF
          List<String> sequences = this.shippingSequences;
          if (sequences != null && !sequences.isEmpty()) {
            boolean importShipping = true;

            if (SystemLocation.GREECE.equals(cmrCountry) || SystemLocation.TURKEY.equals(cmrCountry) || SystemLocation.CYPRUS.equals(cmrCountry)) {
              if (sequences.size() == 1) {
                // only 1 shipping, do not import as additional
                importShipping = false;
              }
            }

            if (importShipping) {
              LOG.debug("Shipping Sequences is not empty. Importing " + sequences.size() + " shipping addresses.");
              for (String seq : sequences) {
                record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping_" + seq + "_", zi01Map);
                if (record != null) {
                  if (SystemLocation.GREECE.equals(cmrCountry) || SystemLocation.TURKEY.equals(cmrCountry)
                      || SystemLocation.CYPRUS.equals(cmrCountry)) {
                    String installingSeq = this.currentImportValues.get("InstallingAddressNumber");
                    if (installingSeq != null && !installingSeq.equals(seq)) {
                      LOG.debug("Importing GR/CY/TU additional shipping " + seq);
                      converted.add(record);
                    } else {
                      LOG.debug("Shipping sequence " + seq + " is main shipping. Skipping.");
                    }
                  } else {
                    converted.add(record);
                  }
                }
              }
            }
          } else {
            LOG.debug("Shipping Sequences is empty. ");
            boolean importShipping = true;

            if (SystemLocation.GREECE.equals(cmrCountry) || SystemLocation.TURKEY.equals(cmrCountry) || SystemLocation.CYPRUS.equals(cmrCountry)) {
              if (sequences.size() == 1) {
                // only 1 shipping, do not import as additional
                importShipping = false;
              }
            }

            if (importShipping) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZD01.toString(), "Shipping", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }
          }

          if (SystemLocation.UNITED_KINGDOM.equals(cmrCountry) || SystemLocation.IRELAND.equals(cmrCountry)) {
            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Mailing", zi01Map);
            if (record != null) {
              converted.add(record);
            }
            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
            if (record != null) {
              converted.add(record);
            }
            if (!hasInstalling) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }

          }

          if (SystemLocation.ISRAEL.equals(cmrCountry) || SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(cmrCountry)) {

            // import all shipping from RDc
            sequences = this.countryCSequences;
            if (sequences != null && !sequences.isEmpty()) {
              LOG.debug("Country Use C Sequences is not empty. Importing " + sequences.size() + " country C addresses.");
              for (String seq : sequences) {
                record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.CTYC.toString(), "CtryUseC_" + seq + "_", zi01Map);
                if (record != null) {
                  converted.add(record);
                }
              }
            } else {
              LOG.debug("Country Use C Sequences is empty. ");
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.CTYC.toString(), "CtryUseC", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }

            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.CTYA.toString(), "Mailing", zi01Map);
            if (record != null) {
              converted.add(record);
            }

            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Billing", zi01Map);
            if (record != null) {
              converted.add(record);
            }

            if (!hasBilling) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.CTYB.toString(), "CtryUseB", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }

            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS02.toString(), "EplMailing", zi01Map);
            if (record != null) {
              converted.add(record);
            }

            if (!hasInstalling) {
              record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZI01.toString(), "Installing", zi01Map);
              if (record != null) {
                converted.add(record);
              }
            }

          }

          if (SystemLocation.GREECE.equals(cmrCountry) || SystemLocation.TURKEY.equals(cmrCountry)) {
            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZP01.toString(), "Mailing", zi01Map);
            if (record != null) {
              converted.add(record);
            }
          }
          if (SystemLocation.ITALY.equals(cmrCountry)) {
            record = createAddress(entityManager, cmrCountry, CmrConstants.ADDR_TYPE.ZS01.toString(), "Installing", zi01Map);
            if (record != null) {
              converted.add(record);
            }
          }

        }
      }
    }
  }

  private void importItalySequences(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
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

  private void importUKISequences(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = null;
    String seqNoFromSOF = null;
    for (FindCMRRecordModel record : records) {
      switch (record.getCmrAddrTypeCode()) {
      case "ZS01":
        addrKey = "Billing";
        break;
      case "ZI01":
        addrKey = "Installing";
        break;
      case "ZS02":
        addrKey = "EplMailing";
        break;
      default:
        addrKey = "";
        break;
      }

      if (StringUtils.isBlank(addrKey)) {
        return;
      }

      seqNoFromSOF = this.currentImportValues.get(addrKey + "AddressNumber");
      if (!StringUtils.isEmpty(seqNoFromSOF)) {
        LOG.trace("Assigning SOF Sequence " + seqNoFromSOF + " to " + addrKey);
        record.setCmrAddrSeq(seqNoFromSOF);
      }

      String transAddressNo = this.currentImportValues.get(addrKey + "TransAddressNumber");
      if (!StringUtils.isEmpty(transAddressNo) && StringUtils.isNumeric(transAddressNo) && transAddressNo.length() == 5) {
        record.setTransAddrNo(transAddressNo);
        LOG.trace("Translated Address No.: '" + record.getTransAddrNo() + "'");
      }

    }
  }

  private void importIsraelSequences(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = null;
    String seqNoFromSOF = null;
    for (FindCMRRecordModel record : records) {
      switch (record.getCmrAddrTypeCode()) {
      case "CTYA":
        addrKey = "Mailing";
        break;
      case "ZP01":
        addrKey = "Billing";
        break;
      case "ZI01":
        addrKey = "Installing";
        break;
      case "ZS02":
        addrKey = "EplMailing";
        break;
      case "ZS01":
        addrKey = "CtryUseA";
        break;
      case "CTYB":
        addrKey = "CtryUseB";
        break;
      default:
        addrKey = "";
        break;
      }

      if (!StringUtils.isBlank(addrKey)) {
        seqNoFromSOF = this.currentImportValues.get(addrKey + "AddressNumber");
        if (!StringUtils.isEmpty(seqNoFromSOF)) {
          LOG.trace("Assigning SOF Sequence " + seqNoFromSOF + " to " + addrKey);
          record.setCmrAddrSeq(seqNoFromSOF);
        }

        String transAddressNo = this.currentImportValues.get(addrKey + "TransAddressNumber");
        if (!StringUtils.isEmpty(transAddressNo) && StringUtils.isNumeric(transAddressNo) && transAddressNo.length() == 5) {
          record.setTransAddrNo(transAddressNo);
          LOG.trace("Translated Address No.: '" + record.getTransAddrNo() + "'");
        }

      }

    }

    // check for pairing, rough fix for improper pairing
    List<String> unpairedShipping = new ArrayList<String>();
    List<String> allShipping = new ArrayList<String>();
    boolean paired = false;
    for (FindCMRRecordModel shipping : records) {
      if (CmrConstants.ADDR_TYPE.ZD01.toString().equals(shipping.getCmrAddrTypeCode())) {
        paired = false;
        allShipping.add(shipping.getCmrAddrSeq());
        for (FindCMRRecordModel countryC : records) {
          if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(countryC.getCmrAddrTypeCode())) {
            if (shipping.getCmrAddrSeq().equals(countryC.getTransAddrNo())) {
              paired = true;
              break;
            }
          }
        }
        if (!paired) {
          unpairedShipping.add(shipping.getCmrAddrSeq());
        }
      }
    }
    int index = 0;
    for (FindCMRRecordModel countryC : records) {
      if (CmrConstants.ADDR_TYPE.CTYC.toString().equals(countryC.getCmrAddrTypeCode())) {
        if (!allShipping.contains(countryC.getTransAddrNo())) {
          // the paired seq is invalid, try to assign a correct one
          if (unpairedShipping.size() > index) {
            countryC.setTransAddrNo(unpairedShipping.get(index));
            index++;
            LOG.trace("Assigning corrected Sequence no " + countryC.getTransAddrNo() + " to Country C " + countryC.getCmrAddrSeq());
          }
        }
      }
    }

  }

  private void importGreeceCyprusTurkeySequences(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String addrKey = null;
    String seqNoFromSOF = null;
    for (FindCMRRecordModel record : records) {
      switch (record.getCmrAddrTypeCode()) {
      case "ZP01":
        addrKey = "Mailing";
        break;
      case "ZS01":
        addrKey = "Installing";
        break;
      default:
        addrKey = "";
        break;
      }

      if (!StringUtils.isBlank(addrKey)) {
        seqNoFromSOF = this.currentImportValues.get(addrKey + "AddressNumber");
        if (!StringUtils.isEmpty(seqNoFromSOF)) {
          LOG.trace("Assigning SOF Sequence " + seqNoFromSOF + " to " + addrKey);
          record.setCmrAddrSeq(seqNoFromSOF);
        }

      }

    }

  }

  @Override
  protected void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey) {

    // String zipCode = this.currentImportValues.get(addressKey + "ZipCode");
    handleCyprusSOFAddress(entityManager, address, cmrIssuingCntry, addressKey);
    /*
     * // zip codes in SOF with **** *** are cross borders if (zipCode != null
     * && zipCode.startsWith("****")) {
     * handleCrossBorderSOFAddress(entityManager, address, addressKey); } else {
     * handleLocalSOFAddress(entityManager, address, addressKey); }
     */

  }

  /**
   * Parses the Italy SOF address and sets the address values
   * 
   * @param entityManager
   * @param address
   * @param addressKey
   */
  private void handleItalySOFAddress(EntityManager entityManager, FindCMRRecordModel address, String cmrIssuingCntry, String addressKey) {

    String line1 = this.currentImportValues.get(addressKey + "Address1");
    String line2 = this.currentImportValues.get(addressKey + "Address2");
    String line3 = this.currentImportValues.get(addressKey + "Address3");
    String line4 = this.currentImportValues.get(addressKey + "Address4");
    String line5 = this.currentImportValues.get(addressKey + "Address5");
    String line6 = this.currentImportValues.get(addressKey + "Address6");

    int lineCount = 0;
    for (String line : Arrays.asList(line1, line2, line3, line4, line5, line6)) {
      if (!StringUtils.isBlank(line)) {
        lineCount++;
      }
    }
    // line 1 is always customer name 1
    address.setCmrName1Plain(line1);

    // line 2 is attn or name2
    if (isAttn(line2)) {
      address.setCmrName4(line2);
    } else {
      address.setCmrName2Plain(line2);
    }
    // Need to add logic here

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
    LOG.trace("State: " + address.getCmrState());
    LOG.trace("Country: " + address.getCmrCountryLanded());

  }

  @Override
  protected void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    switch (cmrIssuingCntry) {
    case SystemLocation.UNITED_KINGDOM:
      importUKISequences(records, cmrIssuingCntry);
      break;
    case SystemLocation.IRELAND:
      importUKISequences(records, cmrIssuingCntry);
      break;
    case SystemLocation.ISRAEL:
      importIsraelSequences(records, cmrIssuingCntry);
      break;
    case SystemLocation.GREECE:
      importGreeceCyprusTurkeySequences(records, cmrIssuingCntry);
      break;
    case SystemLocation.CYPRUS:
      importGreeceCyprusTurkeySequences(records, cmrIssuingCntry);
      break;
    case SystemLocation.TURKEY:
      importGreeceCyprusTurkeySequences(records, cmrIssuingCntry);
      break;
    case SystemLocation.ITALY:
      importItalySequences(records, cmrIssuingCntry);
      break;
    }
  }

  /**
   * Parses the Local SOF address and sets the address values
   * 
   * @param entityManager
   * @param address
   * @param addressKey
   */
  private void handleLocalSOFAddress(EntityManager entityManager, FindCMRRecordModel address, String addressKey) {
    // parse based on rules

    String line1 = this.currentImportValues.get(addressKey + "Address1");
    String line2 = this.currentImportValues.get(addressKey + "Address2");
    String line3 = this.currentImportValues.get(addressKey + "Address3");
    String line4 = this.currentImportValues.get(addressKey + "Address4");

    // count address lines first
    int lineCount = 1 + (!StringUtils.isBlank(line2) ? 1 : 0) + (!StringUtils.isBlank(line3) ? 1 : 0) + (!StringUtils.isBlank(line4) ? 1 : 0);
    LOG.trace(lineCount + " address lines.");

    String street = "";
    String streetCont = "";
    String city = "";
    String nameCont = null;
    String poBox = null;
    String attn = null;
    String phone = null;
    String lines[] = null;
    switch (lineCount) {
    case 4:
      // line 1 = attn
      attn = line1;

      // line 2 = PO Box or street or mixed
      if (isPOBox(line2)) {
        if (line2.contains(",")) {
          lines = line2.split(",");
          for (String line : lines) {
            if (isPOBox(line)) {
              poBox = line;
            } else {
              street += street.length() > 0 ? " " + line : line;
            }
          }
        } else {
          poBox = line2;
        }
      } else {
        street = line2;
      }
      // line 3 is street con't or street if line 2 is PO Box only
      if (!StringUtils.isBlank(street)) {
        streetCont = line3;
      } else {
        street = line3;
      }

      // line 4 = city
      city = line4;
      break;
    case 3:
      // line 1 = attn
      attn = line1;

      // line 2 = PO Box or street or mixed
      if (isPOBox(line2)) {
        if (line2.contains(",")) {
          lines = line2.split(",");
          for (String line : lines) {
            if (isPOBox(line)) {
              poBox = line;
            } else {
              street += street.length() > 0 ? " " + line : line;
            }
          }
        } else {
          poBox = line2;
        }
      } else {
        street = line2;
      }
      // line 3 = city
      city = line3;
      break;
    case 2:
      street = line1;
      city = line2;
      break;
    case 1:
      street = line1;
      break;
    }

    // general handling. if city contains , then it's street cont + city
    if (city != null && city.contains(",")) {
      streetCont += (streetCont.length() > 0 ? " " : "") + city.substring(0, city.lastIndexOf(","));
      city = city.substring(city.lastIndexOf(",") + 1);
    }

    LOG.trace(addressKey + " " + this.currentImportValues.get("CustomerNo"));
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Name Cont: " + nameCont);
    LOG.trace("Attn: " + attn);
    LOG.trace("Phone: " + phone);
    LOG.trace("Street: " + street);
    LOG.trace("Cont: " + streetCont);
    LOG.trace("PO Box: " + poBox);
    LOG.trace("City: " + city);
    address.setCmrName2Plain(nameCont);
    address.setCmrCustPhone(phone);
    address.setCmrStreetAddress(street);
    address.setCmrStreetAddressCont(streetCont);
    address.setCmrDept(attn);
    address.setCmrPOBox(poBox);
    address.setCmrCity(city);

    if (address.getCmrPOBox() != null && address.getCmrPOBox().length() > 20) {
      address.setCmrPOBox(address.getCmrPOBox().substring(0, 20));
    }
    if (address.getCmrPostalCode() != null && address.getCmrPostalCode().length() > 10) {
      address.setCmrPostalCode(address.getCmrPostalCode().substring(0, 10));
    }

    // post code
    address.setCmrPostalCode(this.currentImportValues.get(addressKey + "ZipCode"));

    // country
    address.setCmrCountryLanded(this.currentImportValues.get(addressKey + "Country"));
    formatAddressFields(address);
  }

  /**
   * Parses the Cross-border SOF address and sets the address values
   * 
   * @param entityManager
   * @param address
   * @param addressKey
   */
  private void handleCrossBorderSOFAddress(EntityManager entityManager, FindCMRRecordModel address, String addressKey) {

    String street = "";
    String city = "";
    String postCode = "";
    String attn = "";
    String lines[] = null;
    String poBox = null;
    String countryName = null;

    String line1 = this.currentImportValues.get(addressKey + "Address1");
    String line2 = this.currentImportValues.get(addressKey + "Address2");
    String line3 = this.currentImportValues.get(addressKey + "Address3");
    String line4 = this.currentImportValues.get(addressKey + "Address4");

    // count address lines first
    int lineCount = 1 + (!StringUtils.isBlank(line2) ? 1 : 0) + (!StringUtils.isBlank(line3) ? 1 : 0) + (!StringUtils.isBlank(line4) ? 1 : 0);
    LOG.trace(lineCount + " address lines.");

    // for cross border, only 3 or 4 lines always
    switch (lineCount) {
    case 4:
      // line 1 either PO Box or Attn
      if (isPOBox(line1)) {
        poBox = line1;
      } else {
        attn = line1;
      }

      // line 2 is either PO Box or street, or mixed
      if (isPOBox(line2)) {
        if (line2.contains(",")) {
          lines = line2.split(",");
          for (String line : lines) {
            if (isPOBox(line)) {
              poBox = line;
            } else {
              street += street.length() > 0 ? " " + line : line;
            }
          }
        } else {
          poBox = line2;
        }
      } else {
        street = line2;
      }

      // line 3 is city + post code, parse multiple formats
      if (line3.contains(",")) {
        lines = new String[2];
        lines[0] = line3.substring(0, line3.lastIndexOf(","));
        lines[1] = line3.substring(line3.lastIndexOf(",") + 1);
        if (lines[0].matches(".*\\d{1}.*")) {
          postCode = lines[0];
          city = lines[1];
        } else {
          city = lines[0];
          postCode = lines[1];
        }
      } else if (line3.contains(" ")) {
        lines = new String[2];
        lines[0] = line3.substring(0, line3.lastIndexOf(" "));
        lines[1] = line3.substring(line3.lastIndexOf(" ") + 1);
        if (lines[0].matches(".*\\d{1}.*")) {
          postCode = lines[0];
          city = lines[1];
        } else {
          lines = new String[2];
          lines[0] = line3.substring(0, line3.indexOf(" "));
          lines[1] = line3.substring(line3.indexOf(" ") + 1);
          city = lines[0];
          postCode = lines[1];
          if (lines[0].matches(".*\\d{1}.*")) {
            postCode = lines[0];
            city = lines[1];
          } else {
            city = lines[0];
            postCode = lines[1];
          }
        }

      }
      // line 4 is country
      countryName = line4;
      break;
    case 3:
      if (isPOBox(line1)) {
        if (line1.contains(",")) {
          lines = line1.split(",");
          for (String line : lines) {
            if (isPOBox(line)) {
              poBox = line;
            } else {
              street += street.length() > 0 ? " " + line : line;
            }
          }
        } else {
          poBox = line1;
        }
      } else {
        street = line1;
      }

      // city + post code parsing, hell
      if (line2.contains(",")) {
        lines = new String[2];
        lines[0] = line2.substring(0, line2.lastIndexOf(","));
        lines[1] = line2.substring(line2.lastIndexOf(",") + 1);
        if (lines[0].matches(".*\\d{1}.*")) {
          postCode = lines[0];
          city = lines[1];
        } else {
          city = lines[0];
          postCode = lines[1];
        }
      } else if (line2.contains(" ")) {
        lines = new String[2];
        lines[0] = line2.substring(0, line2.lastIndexOf(" "));
        lines[1] = line2.substring(line2.lastIndexOf(" ") + 1);
        if (lines[0].matches(".*\\d{1}.*")) {
          postCode = lines[0];
          city = lines[1];
        } else {
          lines = new String[2];
          lines[0] = line2.substring(0, line2.indexOf(" "));
          lines[1] = line2.substring(line2.indexOf(" ") + 1);
          city = lines[0];
          postCode = lines[1];
          if (lines[0].matches(".*\\d{1}.*")) {
            postCode = lines[0];
            city = lines[1];
          } else {
            city = lines[0];
            postCode = lines[1];
          }
        }
      }

      countryName = line3;
      break;
    }

    LOG.trace(addressKey + " " + this.currentImportValues.get("CustomerNo"));
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Attn: " + attn);
    LOG.trace("Street: " + street);
    LOG.trace("PO Box: " + poBox);
    LOG.trace("Zip: " + postCode);
    LOG.trace("City: " + city);
    LOG.trace("Country: " + countryName);

    address.setCmrStreetAddress(street);
    address.setCmrDept(attn);
    address.setCmrPOBox(poBox);
    address.setCmrCity(city);
    address.setCmrPostalCode(postCode);

    if (address.getCmrPOBox() != null && address.getCmrPOBox().length() > 20) {
      address.setCmrPOBox(address.getCmrPOBox().substring(0, 20));
    }
    if (address.getCmrPostalCode() != null && address.getCmrPostalCode().length() > 10) {
      address.setCmrPostalCode(address.getCmrPostalCode().substring(0, 10));
    }

    if (!StringUtils.isBlank(countryName)) {
      String code = getCountryCode(entityManager, countryName);
      address.setCmrCountryLanded(code);
      LOG.trace("Landed: " + code);
    }
    formatAddressFields(address);
  }

  /**
   * Parses the Israel SOF address and sets the address values
   * 
   * @param entityManager
   * @param address
   * @param addressKey
   */
  private void handleIsraelSOFAddress(EntityManager entityManager, FindCMRRecordModel address, String addressKey) {
    String name = this.currentImportValues.get(addressKey + "Name");
    String attn = this.currentImportValues.get(addressKey + "Address1");
    String poBox = this.currentImportValues.get(addressKey + "Address2");
    String streetOrPOBox = this.currentImportValues.get(addressKey + "Address3");

    address.setCmrName1Plain(name);
    address.setCmrDept(attn);

    if (poBox != null) {
      if (!poBox.matches(".*\\d{1}.*")) {
        if (streetOrPOBox != null && streetOrPOBox.matches(".*\\d{1}.*")) {
          // interchange here
          poBox = this.currentImportValues.get(addressKey + "Address3");
          streetOrPOBox = this.currentImportValues.get(addressKey + "Address2");
        } else {
          // empty po box
          poBox = null;
        }
      } else {
        // check length
        if (poBox.length() > 20) {
          // check if street has a value
          if (StringUtils.isEmpty(streetOrPOBox)) {
            streetOrPOBox = poBox;
            poBox = null;
          } else if (streetOrPOBox.length() < 20) {
            poBox = this.currentImportValues.get(addressKey + "Address3");
            streetOrPOBox = this.currentImportValues.get(addressKey + "Address2");
          } else {
            poBox = poBox.substring(0, 20);
          }
        }
      }
    }

    // check if street or PO Box
    if (isPOBox(streetOrPOBox)) {
      address.setCmrPOBox(streetOrPOBox);
    } else {
      address.setCmrStreetAddress(streetOrPOBox);
    }

    if (!StringUtils.isEmpty(poBox)) {
      // another po box but PO Box not empty, move
      if (!StringUtils.isEmpty(address.getCmrPOBox())) {
        address.setCmrStreetAddress(streetOrPOBox);
      } else {
        address.setCmrPOBox(poBox);
      }
    }

    String cityAndPostCode = this.currentImportValues.get(addressKey + "City");
    String city = null;
    String postCode = null;

    // city + post code parsing, simplified, just extract numeric portion

    if (cityAndPostCode != null) {
      StringBuilder sbCity = new StringBuilder();
      String[] lines = cityAndPostCode.split("[,. ]");
      for (String line : lines) {
        if (StringUtils.isNumeric(line)) {
          postCode = line;
        } else {
          sbCity.append(sbCity.length() > 0 ? " " : "");
          sbCity.append(line);
        }
      }
      city = sbCity.toString();

      if (postCode != null && postCode.length() > 10) {
        postCode = postCode.substring(0, 10);
      }
      address.setCmrPostalCode(postCode);
      address.setCmrCity(city);
    }
    String country = this.currentImportValues.get(addressKey + "Address4");
    if (StringUtils.isEmpty(country)) {
      address.setCmrCountryLanded("IL");
    } else {
      String code = getCountryCode(entityManager, country);
      address.setCmrCountryLanded(code);
    }

    if (address.getCmrPOBox() != null && address.getCmrPOBox().length() > 20) {
      address.setCmrPOBox(address.getCmrPOBox().substring(0, 20));
    }
    if (address.getCmrPostalCode() != null && address.getCmrPostalCode().length() > 10) {
      address.setCmrPostalCode(address.getCmrPostalCode().substring(0, 10));
    }
    LOG.trace(addressKey + " " + this.currentImportValues.get("CustomerNo"));
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Attn: " + attn);
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("PO Box: " + poBox);
    LOG.trace("Zip: " + postCode);
    LOG.trace("City: " + city);
    LOG.trace("Country: " + country + " (" + address.getCmrCountryLanded() + ")");

  }

  private void formatAddressFields(FindCMRRecordModel address) {
    if (address.getCmrPOBox() != null) {
      address.setCmrPOBox(address.getCmrPOBox().replaceAll("P.*BOX", ""));
    }
  }

  /**
   * Parses the Greece/Cyprus SOF address and sets the address values
   * 
   * @param entityManager
   * @param address
   * @param addressKey
   */
  private void handleCyprusSOFAddress(EntityManager entityManager, FindCMRRecordModel address, String cmrIssuingCntry, String addressKey) {
    String name = this.currentImportValues.get(addressKey + "Address1");
    String nickName = this.currentImportValues.get(addressKey + "Address2");
    String occuOrPOBoxOrPhone = this.currentImportValues.get(addressKey + "Address3");
    String street = this.currentImportValues.get(addressKey + "Address4");
    String cityAndPostCode = this.currentImportValues.get(addressKey + "Address5");
    String country = this.currentImportValues.get(addressKey + "Address6");
    String vat = this.currentImportValues.get(addressKey + "AddressU");
    String phone = this.currentImportValues.get(addressKey + "Phone");

    address.setCmrName1Plain(name);
    address.setCmrTaxNumber(vat);

    if (nickName == null || "*".equals(nickName)) {
      nickName = "";
    }
    if (occuOrPOBoxOrPhone == null || "*".equals(occuOrPOBoxOrPhone)) {
      occuOrPOBoxOrPhone = "";
    }

    if (nickName.matches(".*\\d{1}.*")) {
      // try to handle bad data with Nickname + Phone
      int index = -1;
      for (int i = 0; i < nickName.length() - 1; i++) {
        if (StringUtils.isNumeric(nickName.substring(i, i + 1))) {
          index = i;
          break;
        }
      }
      if (index > 0) {
        address.setCmrName2Plain(nickName.substring(0, index).trim());
        address.setCmrCustPhone(nickName.substring(index).trim());
      }
    } else {
      address.setCmrName2Plain(nickName);
    }

    if (isPOBox(occuOrPOBoxOrPhone)) {
      // this is a pobox
      address.setCmrPOBox(occuOrPOBoxOrPhone);
    } else if (isPhone(occuOrPOBoxOrPhone) || occuOrPOBoxOrPhone.matches(".*\\d{1}.*")) {
      // NOT a po box so numbers indicate telephone

      // strip non numbers
      String formattedPhone = occuOrPOBoxOrPhone.replaceAll("[^\\d\\-\\+ ]", "").trim();
      if (formattedPhone.length() > 16) {
        // further remove spaces and - to fit
        formattedPhone = formattedPhone.replaceAll("[\\-\\+ ]", "").trim();
      }
      address.setCmrCustPhone(formattedPhone);

    } else {
      // this is an occupation
      address.setCmrStreetAddressCont(occuOrPOBoxOrPhone);
    }

    address.setCmrStreetAddress(street);

    String city = null;
    String postCode = null;

    // city + post code parsing, simplified, just extract numeric portion

    if (cityAndPostCode != null) {
      StringBuilder sbCity = new StringBuilder();
      String[] lines = cityAndPostCode.split("[,. ]");
      StringBuilder sbPostCode = new StringBuilder();

      // start from left to right. until words have numbers add them to
      // post
      // code
      for (String line : lines) {
        if (StringUtils.isNumeric(line) && sbCity.length() == 0) {
          sbPostCode.append(sbPostCode.length() > 0 ? " " : "");
          sbPostCode.append(line);
        } else {
          sbCity.append(sbCity.length() > 0 ? " " : "");
          sbCity.append(line);
        }
      }
      postCode = sbPostCode.toString();
      city = sbCity.toString();

      if (postCode != null && postCode.length() > 10) {
        postCode = postCode.substring(0, 10);
      }
      address.setCmrPostalCode(postCode);
      address.setCmrCity(city);
    }

    if (StringUtils.isBlank(address.getCmrCustPhone()) && phone != null && !phone.equals(address.getCmrStreetAddressCont())
        && !phone.equals(address.getCmrPOBox())) {
      address.setCmrCustPhone(phone);
    }

    if (!StringUtils.isEmpty(country)) {
      String code = getCountryCode(entityManager, country);
      address.setCmrCountryLanded(code);
    }
    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      switch (cmrIssuingCntry) {
      case SystemLocation.GREECE:
        address.setCmrCountryLanded("GR");
        break;
      case SystemLocation.CYPRUS:
        address.setCmrCountryLanded("CY");
        break;
      }
    }

    trimAddressToFit(address);

    LOG.trace(addressKey + " " + this.currentImportValues.get("CustomerNo"));
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Nickname: " + address.getCmrName2Plain());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Occupation: " + address.getCmrStreetAddressCont());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Country: " + country + " (" + address.getCmrCountryLanded() + ")");

  }

  /**
   * Parses the Greece/Cyprus SOF address and sets the address values
   * 
   * @param entityManager
   * @param address
   * @param addressKey
   */
  private void handleTurkeySOFAddress(EntityManager entityManager, FindCMRRecordModel address, String cmrIssuingCntry, String addressKey) {
    String name = this.currentImportValues.get(addressKey + "Address1");
    String nameCont = this.currentImportValues.get(addressKey + "Address2");
    String street = this.currentImportValues.get(addressKey + "Address3");
    String streetCont = this.currentImportValues.get(addressKey + "Address4");
    String districtCityPostCode = this.currentImportValues.get(addressKey + "Address5");
    String country = this.currentImportValues.get(addressKey + "Address6");
    String vat = this.currentImportValues.get(addressKey + "AddressU");
    String phone = this.currentImportValues.get(addressKey + "Phone");

    address.setCmrName1Plain(name);
    address.setCmrName2Plain(nameCont);
    address.setCmrTaxNumber(vat);
    address.setCmrStreetAddress(street);
    address.setCmrStreetAddressCont(streetCont);

    String city = null;
    String postCode = null;
    String district = null;

    // district city + post code parsing, simplified, just extract numeric
    // portion

    if (districtCityPostCode != null) {

      String[] lines = districtCityPostCode.split("[,. ]");

      StringBuilder sbCity = new StringBuilder();
      StringBuilder sbPostCode = new StringBuilder();
      StringBuilder sbDistrict = new StringBuilder();

      // start from left to right. until words have numbers add them to
      // post
      // code
      for (String line : lines) {
        if (StringUtils.isNumeric(line) && line.length() > 3) {
          // numeric, and it already has district, so this is post
          // code
          sbPostCode.append(line);
        } else if (sbPostCode.length() == 0) {
          sbDistrict.append(sbDistrict.length() > 0 ? " " : "");
          sbDistrict.append(line);
        } else {
          sbCity.append(sbCity.length() > 0 ? " " : "");
          sbCity.append(line);
        }
      }

      city = sbCity.toString();
      district = sbDistrict.toString();
      postCode = sbPostCode.toString();
    }

    address.setCmrCity(city);
    address.setCmrDept(district);
    address.setCmrPostalCode(postCode);

    if (StringUtils.isBlank(address.getCmrCustPhone()) && phone != null && !phone.equals(address.getCmrStreetAddressCont())
        && !phone.equals(address.getCmrPOBox())) {
      address.setCmrCustPhone(phone);
    }

    if (!StringUtils.isEmpty(country)) {
      String code = getCountryCode(entityManager, country);
      address.setCmrCountryLanded(code);
    }
    if (StringUtils.isEmpty(address.getCmrCountryLanded())) {
      address.setCmrCountryLanded("TR");
    }

    if (address.getCmrPOBox() != null && address.getCmrPOBox().length() > 20) {
      address.setCmrPOBox(address.getCmrPOBox().substring(0, 20));
    }
    if (address.getCmrPostalCode() != null && address.getCmrPostalCode().length() > 10) {
      address.setCmrPostalCode(address.getCmrPostalCode().substring(0, 10));
    }
    if (address.getCmrCustPhone() != null && address.getCmrCustPhone().length() > 16) {
      address.setCmrCustPhone(address.getCmrCustPhone().substring(0, 16));
    }

    LOG.trace(addressKey + " " + this.currentImportValues.get("CustomerNo"));
    LOG.trace("Name: " + address.getCmrName1Plain());
    LOG.trace("Nickname: " + address.getCmrName2Plain());
    LOG.trace("Street: " + address.getCmrStreetAddress());
    LOG.trace("Occupation: " + address.getCmrStreetAddressCont());
    LOG.trace("Phone: " + address.getCmrCustPhone());
    LOG.trace("PO Box: " + address.getCmrPOBox());
    LOG.trace("Zip: " + address.getCmrPostalCode());
    LOG.trace("City: " + address.getCmrCity());
    LOG.trace("Country: " + country + " (" + address.getCmrCountryLanded() + ")");

  }

  /**
   * Connects to the SOF Query service to retrieve the legacy values of the
   * data, using the mapping on cmr-mq.properties to automatically assign ALL
   * values from SOF
   * 
   * @param entity
   * @param results
   * @param mainRecord
   * @throws Exception
   */
  protected void queryAndAssign(BaseEntity<?> entity, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    String cmrIssuingCntry = mainRecord.getCmrIssuedBy();
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }
    if (SystemLocation.ISRAEL.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.SAP_ISRAEL_SOF_ONLY;
    }

    if ((this.currentImportValues == null || this.currentImportValues.isEmpty()) && !this.serviceError) {
      retrieveSOFValues(mainRecord);
      if (this.currentImportValues != null && !this.currentImportValues.isEmpty()) {
        String mappedElement = null;
        String entityName = null;
        String field = null;

        Field entityField = null;
        Class<?> entityClass = null;
        for (String xmlElement : currentImportValues.keySet()) {
          mappedElement = MQProcessUtil.getMappingValue(cmrIssuingCntry, xmlElement);
          if (!StringUtils.isBlank(mappedElement) && mappedElement.trim().split("[.]").length == 2) {
            entityName = mappedElement.trim().split("[.]")[0];
            field = mappedElement.trim().split("[.]")[1];

            // map entity name against the supplied entity
            if (entityName.equalsIgnoreCase(entity.getClass().getSimpleName())) {
              entityClass = entity.getClass();
              try {
                entityField = entityClass.getDeclaredField(field.substring(0, 1).toLowerCase() + field.substring(1));
                if (entityField != null) {
                  entityField.setAccessible(true);
                  if (entityField.getType().equals(String.class)) {
                    entityField.set(entity, this.currentImportValues.get(xmlElement));
                  } else {
                    // handle here other types
                  }
                }
              } catch (NoSuchFieldException nsfe) {
                // proceed
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    data.setEmbargoCd(this.currentImportValues.get("EmbargoCode"));
    LOG.trace("EmbargoCode: " + data.getEmbargoCd());

    if (!SystemLocation.ITALY.equalsIgnoreCase(data.getCmrIssuingCntry())) {

      // defect 1299146
      if (mainRecord.getCmrSortl() != null && mainRecord.getCmrSortl().length() >= 10) {
        data.setRepTeamMemberNo(mainRecord.getCmrSortl().substring(0, 6));
        LOG.trace("Rep No from Sortl : " + data.getRepTeamMemberNo());
        data.setSalesBusOffCd(mainRecord.getCmrSortl().substring(7, 10));
        LOG.trace("SBO from Sortl : " + data.getSalesBusOffCd());
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
        if ("U".equals(admin.getReqType()) && !StringUtils.isEmpty(this.currentImportValues.get("COD"))) {
          data.setCreditCd(this.currentImportValues.get("COD"));
        }
      }

      // For IBO, SBO & EBO values exceeding 3 char length
      String iBO = this.currentImportValues.get("IBO");
      String sBO = this.currentImportValues.get("SBO");
      String eBo = this.currentImportValues.get("DPCEBO");
      String accAdBo = this.currentImportValues.get("AccAdBo");

      if (SystemLocation.UNITED_KINGDOM.equalsIgnoreCase(data.getCmrIssuingCntry())) {
        if (!(StringUtils.isEmpty(accAdBo))) {
          data.setAcAdminBo(accAdBo);
        }
      }

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

      if ("U".equals(admin.getReqType()) && (SystemLocation.UNITED_KINGDOM.equalsIgnoreCase(data.getCmrIssuingCntry())
          || SystemLocation.IRELAND.equalsIgnoreCase(data.getCmrIssuingCntry()))) {
        data.setAbbrevLocn((this.currentImportValues.get("AbbreviatedLocation")));
        LOG.trace("AbbreviatedLocation: " + data.getAbbrevLocn());
      }
      if (legacyObjects != null && legacyObjects.getCustomer() != null) {
        data.setCrosSubTyp(legacyObjects.getCustomer().getCustType());
        if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
          data.setSalesTeamCd(legacyObjects.getCustomer().getSalesRepNo());
          data.setRepTeamMemberNo(legacyObjects.getCustomer().getSalesGroupRep());
        } else {
          data.setSalesTeamCd("000000");
          data.setRepTeamMemberNo("000000");
        }
      }
    } else { // Story 1389065: SBO and Sales rep auto-population : Mukesh

      String collCd = this.currentImportValues.get("CollectionCode");
      String sr = this.currentImportValues.get("SR");
      String sBO = this.currentImportValues.get("SBO");

      // Story 1374616: Requirement for INAC / NAC :Mukesh
      String inac = data.getInacCd();
      if ((inac != null || !StringUtils.isEmpty(inac)) && inac.length() > 4) {
        data.setInacCd(inac.substring(0, 4));
      }
      // Checking Company CMR
      if (((!StringUtils.isEmpty(data.getCompany())) && (!StringUtils.isEmpty(data.getCmrNo())))
          && (data.getCompany().equalsIgnoreCase(data.getCmrNo()))) {
        LOG.debug("It is Company CMR (Node1 equal to CMR No):: " + data.getCompany() + " equal to " + data.getCmrNo());

        if (mainRecord.getCmrSortl() != null && mainRecord.getCmrSortl().length() >= 10) {
          data.setSalesBusOffCd(mainRecord.getCmrSortl().substring(1, 3)); // lenght2
          data.setRepTeamMemberNo(mainRecord.getCmrSortl().substring(4, 10)); // lenght6
        }
        // data.setCollectionCd(StringUtils.isEmpty(collCd) ? "" :
        // collCd.substring(0, 5)); // lenght5

      } else {
        LOG.debug("It is not Company CMR (Node1 not equal to CMR No): " + data.getCompany() + " not equal to " + data.getCmrNo());

        if (!StringUtils.isEmpty(data.getIsuCd()) && !"32".equalsIgnoreCase(data.getIsuCd().substring(0, 2))) {
          data.setRepTeamMemberNo("");
          data.setSalesBusOffCd("");
        }

      }

      String iBO = this.currentImportValues.get("IBO");
      String eBo = this.currentImportValues.get("DPCEBO");

      if (!(StringUtils.isEmpty(iBO))) {
        if (iBO.length() > 3) {
          data.setInstallBranchOff(iBO.substring(0, 3));
        }
      }
      if (!(StringUtils.isEmpty(eBo))) {
        if (eBo.length() > 3) {
          data.setEngineeringBo(eBo.substring(0, 3));
        }
      }
      // Story 1374607 : Mukesh
      String abbrerLocn = this.currentImportValues.get("AbbreviatedLocation");
      if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
        if (!StringUtils.isEmpty(abbrerLocn)) {
          data.setAbbrevLocn(abbrerLocn);
          if (abbrerLocn != null && abbrerLocn.length() > 12) {
            data.setAbbrevLocn(abbrerLocn.substring(0, 12));
          }
        } else {
          data.setAbbrevLocn("");
        }
      }

    } // End of Story 1389065

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && SystemLocation.GREECE.equalsIgnoreCase(data.getCmrIssuingCntry())) {
      data.setMemLvl(mainRecord.getCmrMembLevel());
      data.setBpRelType(mainRecord.getCmrBPRelType());
      data.setEnterprise(mainRecord.getCmrEnterpriseNumber());

      if (!StringUtils.isEmpty(mainRecord.getCmrSortl())) {
        String repTeamMmberNo = mainRecord.getCmrSortl().substring(0, 6);
        data.setRepTeamMemberNo(repTeamMmberNo);
      }
    }
    if (CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())) {
      data.setPpsceid("");
    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    if (currentRecord != null) {

      String country = currentRecord.getCmrIssuedBy();

      if (!SystemLocation.GREECE.equals(country) && !SystemLocation.CYPRUS.equals(country) && !SystemLocation.TURKEY.equals(country)
          && !SystemLocation.UNITED_KINGDOM.equals(country) && !SystemLocation.IRELAND.equals(country)) {
        String[] names = splitName(currentRecord.getCmrName1Plain(), currentRecord.getCmrName2Plain(), 30, 30);
        address.setCustNm1(names[0]);
        address.setCustNm2(names[1]);

      } else {
        address.setCustNm1(currentRecord.getCmrName1Plain());
        address.setCustNm2(currentRecord.getCmrName2Plain());

      }

      if (!StringUtils.isEmpty(address.getCustPhone())) {
        if (!"ZS01".equals(address.getId().getAddrType()) && !"ZD01".equals(address.getId().getAddrType())
            && !"ZP02".equals(address.getId().getAddrType())) {
          address.setCustPhone("");
        }
      }

      if ("ZD01".equals(address.getId().getAddrType())) {
        String phone = getShippingPhoneFromLegacy(currentRecord);
        address.setCustPhone(phone != null ? phone : "");
      }

      address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
      // if (StringUtils.isNotBlank(currentRecord.getCmrCountryLanded())
      // &&
      // currentRecord.getCmrCountryLanded().equalsIgnoreCase("IE")) {
      // address.setPostCd(StringUtils.isNotBlank(currentRecord.getCmrPostalCode())
      // ? currentRecord.getCmrPostalCode() : "II1 1II");
      // }

      // no addr std for israel
      if ("IL".equals(currentRecord.getCmrCountryLanded()) && (SystemLocation.ISRAEL.equals(currentRecord.getCmrIssuedBy())
          || SystemLocation.SAP_ISRAEL_SOF_ONLY.equals(currentRecord.getCmrIssuedBy()))) {
        address.setAddrStdResult("X");
      }

      // CREATCMR-5741 - no addr std
      address.setAddrStdResult("X");
      address.setPairedAddrSeq(currentRecord.getTransAddrNo());

      address.setVat(currentRecord.getCmrTaxNumber());
      // set tax office here

      address.setVat(currentRecord.getCmrTaxNumber());

      if (SystemLocation.TURKEY.equals(country)) {
        address.setDept(currentRecord.getCmrDept());
      }

      if (SystemLocation.CYPRUS.equals(country)) {
        if (currentRecord.getCmrName4() != null) {
          address.setCustNm4(LegacyCommonUtil.removeATT(currentRecord.getCmrName4()));
        }
        // GR - old record
        if (isOldRecordsGR) {
          address.setImportInd("N");
        }
      }

      if (SystemLocation.UNITED_KINGDOM.equals(country) || SystemLocation.IRELAND.equals(country)) {
        if (!StringUtils.isEmpty(currentRecord.getCmrName3())) {
          address.setDept(removeATT(currentRecord.getCmrName3()));
        }
        if (!StringUtils.isEmpty(address.getCustPhone()) && !"ZS01".equals(address.getId().getAddrType())
            && !"ZD01".equals(address.getId().getAddrType())) {
          address.setCustPhone("");
        }
      }

      String processingType = PageManager.getProcessingType(country, "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
        if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
            && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
          String seq = address.getId().getAddrSeq();
          seq = StringUtils.leftPad(seq, 5, '0');
          address.getId().setAddrSeq(seq);
        }
        if ("D".equals(address.getImportInd())) {
          String seq = StringUtils.leftPad(address.getId().getAddrSeq(), 5, '0');
          address.getId().setAddrSeq(seq);
        }
      }
    }
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
      data.setCustPrefLang("B");
      data.setCollectionCd("TC0");
      data.setSpecialTaxCd("AA");
    } else if (SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
      data.setCustPrefLang("I");
    } else {
      data.setCustPrefLang("E");
      data.setSalesTeamCd("000000");
      data.setRepTeamMemberNo("000000");
      data.setSalesBusOffCd("000");
    }
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
    if (!StringUtils.isEmpty(data.getRepTeamMemberNo())) {
      switch (data.getCmrIssuingCntry()) {
      case SystemLocation.UNITED_KINGDOM:
        request.setSORTL(data.getSalesBusOffCd() + " " + data.getRepTeamMemberNo());
        break;
      case SystemLocation.IRELAND:
        request.setSORTL(data.getSalesBusOffCd() + " " + data.getRepTeamMemberNo());
        break;
      case SystemLocation.ISRAEL:
        request.setSORTL(data.getSalesBusOffCd() + "-" + data.getRepTeamMemberNo());
        break;
      }
    }
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  private void doSetInFSLAbbrevName(Data data) {
    // 1. we check if it has the correct scenario type
    if ("INFSL".equals(data.getCustSubGrp())) {
      // 2. Get the last four chars of the abbrev nm and check if it is
      // correct
      if (!StringUtils.isEmpty(data.getAbbrevNm())) {
        int abbrevNmLen = data.getAbbrevNm().length();
        String abbrevNm = data.getAbbrevNm();

        if (abbrevNmLen == 23) {
          // we chop off 5 chars
          abbrevNm = abbrevNm.substring(0, 18);
        } else {
          // we figure out the starting char
          if (abbrevNmLen > 18) {
            abbrevNm = abbrevNm.substring(0, 18);
          }
        }

        if (SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry())) {
          abbrevNm = abbrevNm + " " + UK_INFSL_ABBREVNM_SUFFIX;
        } else if (SystemLocation.IRELAND.equals(data.getCmrIssuingCntry())) {
          abbrevNm = abbrevNm + " " + IR_INFSL_ABBREVNM_SUFFIX;
        }
        data.setAbbrevNm(abbrevNm);
      }
    }
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {

    DataRdc rdcData = null;
    rdcData = getOldData(entityManager, String.valueOf(data.getId().getReqId()));
    if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType())) {
      if (rdcData != null) {
        data.setInacType(rdcData.getInacType());
      }
    }

    data.setInstallBranchOff(data.getSalesBusOffCd());
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);

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

    case SystemLocation.GREECE:
      if (data != null && admin.getReqType().equals("C")) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          LOG.debug("Computing Abbreviated Name/Location for address of Greece. Request " + addr.getId().getReqId());
          data.setAbbrevNm(addr.getCustNm1());
          if (data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
            data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
          }
          LOG.debug("- Abbreviated Name: " + data.getAbbrevNm());
          data.setAbbrevLocn(addr.getCity1());
          if (!"GR".equals(addr.getLandCntry())) {
            // for cross-border, use country name
            String country = DropdownListController.getDescription("LandedCountry", addr.getLandCntry(), SystemLocation.GREECE);
            if (!StringUtils.isEmpty(country)) {
              data.setAbbrevLocn(country);
            }
          }
          if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
            data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
          }
          LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

          entityManager.merge(data);
          entityManager.flush();
        }

        if ((CmrConstants.CUSTGRP_CROSS.equals(data.getCustGrp()) || !"GR".equals(addr.getLandCntry()))
            && ("ZS01".equals(addr.getId().getAddrType()) || "ZP01".equals(addr.getId().getAddrType()))) {
          updateLandCntryGR(entityManager, addr);

          // -- START --- missing codes in main
          // auto generate zp01/zs01 if either one is created
          if ("ZS01".equals(addr.getId().getAddrType())) {
            if (getAddressByType(entityManager, "ZP01", data.getId().getReqId()) == null) {
              saveAddrCopyForGR(entityManager, addr, "ZP01");
            }
          } else if ("ZP01".equals(addr.getId().getAddrType())) {
            if (getAddressByType(entityManager, "ZS01", data.getId().getReqId()) == null) {
              saveAddrCopyForGR(entityManager, addr, "ZS01");
            }
          }
          // -- END --- missing codes in main
        }
      }

      break;

    case SystemLocation.CYPRUS:
      if (data != null && admin.getReqType().equals("C")) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          LOG.debug("Computing Abbreviated Name/Location for address of CYPRUS. Request " + addr.getId().getReqId());
          data.setAbbrevNm(addr.getCustNm1());
          if (data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
            data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
          }
          LOG.debug("- Abbreviated Name: " + data.getAbbrevNm());
          data.setAbbrevLocn(addr.getCity1());
          if (!"CY".equals(addr.getLandCntry())) {
            // for cross-border, use country name
            String country = DropdownListController.getDescription("LandedCountry", addr.getLandCntry(), SystemLocation.CYPRUS);
            if (!StringUtils.isEmpty(country)) {
              data.setAbbrevLocn(country);
            }
          }
          if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
            data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
          }
          LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

          entityManager.merge(data);
          entityManager.flush();
        }

        if ("ZS01".equals(addr.getId().getAddrType())) {
          copyAndSaveAddressesCreateScratch(entityManager, data.getId().getReqId(), addr);
        }
      }

      if (!StringUtils.isEmpty(addr.getCustPhone()) && !"ZS01".equals(addr.getId().getAddrType()) && !"ZD01".equals(addr.getId().getAddrType())) {
        addr.setCustPhone("");
      }

      if (!StringUtils.isEmpty(addr.getPoBox()) && !"ZS01".equals(addr.getId().getAddrType()) && !"ZP01".equals(addr.getId().getAddrType())) {
        addr.setPoBox("");
      }
      break;

    case SystemLocation.TURKEY:
      if (data != null && admin.getReqType().equals("C")) {
        if ("ZS01".equals(addr.getId().getAddrType())) {
          LOG.debug("Computing Abbreviated Name/Location for address of TURKEY. Request " + addr.getId().getReqId());
          data.setAbbrevNm(addr.getCustNm1());
          if (data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
            data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
          }
          LOG.debug("- Abbreviated Name: " + data.getAbbrevNm());
          data.setAbbrevLocn(addr.getCity1());
          if (!"TR".equals(addr.getLandCntry())) {
            // for cross-border, use country name
            String country = DropdownListController.getDescription("LandedCountry", addr.getLandCntry(), SystemLocation.TURKEY);
            if (!StringUtils.isEmpty(country)) {
              data.setAbbrevLocn(country);
            }
          }
          if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
            data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
          }
          LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

          entityManager.merge(data);
          entityManager.flush();
        }

        if (CmrConstants.CUSTGRP_CROSS.equals(data.getCustGrp()) || !"TR".equals(addr.getLandCntry())) {
          updateLandCntry(entityManager, addr);
        }
      }

      if ("ZS01".equals(addr.getId().getAddrType()) || "ZD01".equals(addr.getId().getAddrType())) {
        // change turkish chars upon copying from mailing

        char[] problematicCharList = new char[12];

        problematicCharList[0] = '\u00c7'; // Ã‡
        problematicCharList[1] = '\u00e7'; // Ã§
        problematicCharList[2] = '\u011e'; // Äž
        problematicCharList[3] = '\u011f'; // ÄŸ
        problematicCharList[4] = '\u0130'; // Ä°
        problematicCharList[5] = '\u0131'; // Ä±
        problematicCharList[6] = '\u00d6'; // Ã–
        problematicCharList[7] = '\u00f6'; // Ã¶
        problematicCharList[8] = '\u015e'; // Åž
        problematicCharList[9] = '\u015f'; // ÅŸ
        problematicCharList[10] = '\u00dc'; // Ãœ
        problematicCharList[11] = '\u00fc'; // Ã¼

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

        for (String key : addressDataMap.keySet()) {
          for (char problematicChar : problematicCharList) {
            if (!(StringUtils.isEmpty(addressDataMap.get(key)))) {
              for (int i = 0; i < addressDataMap.get(key).length(); i++) {
                int index = addressDataMap.get(key).indexOf(problematicChar);
                if (index >= 0) {
                  String field = null;
                  switch (addressDataMap.get(key).charAt(index)) {
                  case '\u00c7':// Ã‡
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'C');
                    addressDataMap.put(key, field);
                    break;
                  case '\u00e7': // Ã§
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'c');
                    addressDataMap.put(key, field);
                    break;
                  case '\u011e': // Äž
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'G');
                    addressDataMap.put(key, field);
                    break;
                  case '\u011f': // ÄŸ
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'g');
                    addressDataMap.put(key, field);
                    break;
                  case '\u0130': // Ä°
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'I');
                    addressDataMap.put(key, field);
                    break;
                  case '\u0131': // Ä±
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'i');
                    addressDataMap.put(key, field);
                    break;
                  case '\u00d6': // Ã–
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'O');
                    addressDataMap.put(key, field);
                    break;
                  case '\u00f6': // Ã¶
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'o');
                    addressDataMap.put(key, field);
                    break;
                  case '\u015e': // Åž
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'S');
                    addressDataMap.put(key, field);
                    break;
                  case '\u015f': // ÅŸ
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 's');
                    addressDataMap.put(key, field);
                    break;
                  case '\u00dc': // Ãœ
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'U');
                    addressDataMap.put(key, field);
                    break;
                  case '\u00fc': // Ã¼
                    field = addressDataMap.get(key).replace(addressDataMap.get(key).charAt(index), 'u');
                    addressDataMap.put(key, field);
                    break;
                  }
                }
              }
            }
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

        }
      }

      break;

    case SystemLocation.ITALY:
      if ("ZS01".equals(addr.getId().getAddrType())) {
        // this is italy installing, populate abbrev name and location
        LOG.debug("Computing Abbreviated Name/Location for Installing address of Italy. Request " + addr.getId().getReqId());
        if (data != null && admin.getReqType().equals("C")) {
          data.setAbbrevNm(addr.getCustNm1());
          if (data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
            data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
          }

          // Story 1374607 : Mukesh
          data.setAbbrevLocn(addr.getCity1());
          if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
            data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
          }
          LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

        }
      }
      break;

    case SystemLocation.IRELAND:
      if ("ZI01".equals(addr.getId().getAddrType())) {
        LOG.debug("Computing Abbreviated Name/Location for Installing address of Ireland. Request " + addr.getId().getReqId());
        if (data != null && admin.getReqType().equals("C")) {
          String abbrevNmValue = addr.getCustNm1();
          if (abbrevNmValue != null && abbrevNmValue.length() > 22) {
            abbrevNmValue = abbrevNmValue.substring(0, 22);
          }
          if (data.getCustSubGrp() == null) {
          } else if (data.getCustSubGrp() != null) {
            if (data.getCustSubGrp().equalsIgnoreCase("COOEM")) {
              if (abbrevNmValue != null && abbrevNmValue.length() > 18) {
                abbrevNmValue = abbrevNmValue.substring(0, 18) + " OEM";
              } else {
                abbrevNmValue = abbrevNmValue + " OEM";
              }
            }
          }
          // data.setAbbrevNm(addr.getCustNm1());

          String abbrevLocnValue = null;
          if (data.getCustSubGrp() == null) {
          } else if (data.getCustSubGrp() != null) {
            if (data.getCustSubGrp().equalsIgnoreCase("SOFTL")) {
              abbrevLocnValue = "IBM Cloud";
            } else if (data.getCustSubGrp().equalsIgnoreCase("CROSS")) {
              abbrevLocnValue = addr.getLandCntry();
            } else {
              abbrevLocnValue = addr.getCity1();
            }
          }
          if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
            abbrevLocnValue = abbrevLocnValue.substring(0, 12);
          }
          data.setAbbrevLocn(abbrevLocnValue);
          LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

        }
        entityManager.merge(data);
        entityManager.flush();
      }

      if ("ZS01".equals(addr.getId().getAddrType())) {
        admin.setMainCustNm1(addr.getCustNm1() != null ? addr.getCustNm1() : "");
        admin.setMainCustNm2(addr.getCustNm2() != null ? addr.getCustNm2() : "");
        entityManager.merge(admin);
        entityManager.flush();
      }

      if (!StringUtils.isEmpty(addr.getCustPhone()) && !"ZS01".equals(addr.getId().getAddrType()) && !"ZD01".equals(addr.getId().getAddrType())) {
        addr.setCustPhone("");
      }

      if (!Arrays.asList(HRDWRE_MSTR_FLAG_ADDRS).contains(addr.getId().getAddrType()) && "Y".equalsIgnoreCase(addr.getHwInstlMstrFlg())) {
        addr.setHwInstlMstrFlg("");
      }
      break;

    case SystemLocation.UNITED_KINGDOM:
      if ("ZI01".equals(addr.getId().getAddrType())) {
        // 1482148 - this is UK installing, populate abbrev name and
        // location
        LOG.debug("Computing Abbreviated Name/Location for Installing address of UK. Request " + addr.getId().getReqId());
        if (data != null && admin.getReqType().equals("C")) {
          /*
           * String abbrevNmValue = addr.getCustNm1(); if (abbrevNmValue != null
           * && abbrevNmValue.length() > 22) { abbrevNmValue =
           * abbrevNmValue.substring(0, 22); }
           * data.setAbbrevNm(addr.getCustNm1());
           */

          data.setAbbrevLocn(addr.getPostCd());
          if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
            data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
          }
          LOG.debug("- Abbreviated Location: " + data.getAbbrevLocn());

        }
        entityManager.merge(data);
        entityManager.flush();
      }
      // Story 1864101: DEV: UK - DPCEBO and ACC ADMIN BO values (Defect:
      // 1857584)
      if ("ZS01".equals(addr.getId().getAddrType())) {

        boolean crossBorder = isCrossBorder(addr);
        String postCd = addr.getPostCd();

        if (crossBorder) {
          data.setEngineeringBo("554");
          data.setAcAdminBo("HO");
        } else {
          if (!StringUtils.isBlank(postCd) && postCd.indexOf(" ") > 0) {
            postCd = postCd.substring(0, postCd.indexOf(" "));
            setEngBoAndAdminBo(data, entityManager, postCd);
          } else {
            data.setEngineeringBo("");
            data.setAcAdminBo("");
          }
        }

        entityManager.merge(data);
        entityManager.flush();

        admin.setMainCustNm1(addr.getCustNm1() != null ? addr.getCustNm1() : "");
        admin.setMainCustNm2(addr.getCustNm2() != null ? addr.getCustNm2() : "");
        entityManager.merge(admin);
        entityManager.flush();
      }

      if (!StringUtils.isEmpty(addr.getCustPhone()) && !"ZS01".equals(addr.getId().getAddrType()) && !"ZD01".equals(addr.getId().getAddrType())) {
        addr.setCustPhone("");
      }

      if (!Arrays.asList(HRDWRE_MSTR_FLAG_ADDRS).contains(addr.getId().getAddrType()) && "Y".equalsIgnoreCase(addr.getHwInstlMstrFlg())) {
        addr.setHwInstlMstrFlg("");
      }
      break;
    }
  }

  private void copyAndSaveAddressesCreateScratch(EntityManager entityManager, long reqId, Addr addr) {
    if (getCountAddrSaved(entityManager, reqId) == 0) {
      doActualCopySaveAllAddrs(entityManager, addr);
    }
  }

  private void copyAndSaveAddressesDNBImport(EntityManager entityManager, long reqId, Addr addr) {
    if (getCountAddrSaved(entityManager, reqId) == 1) {
      doActualCopySaveAllAddrs(entityManager, addr);
    }
  }

  private void doActualCopySaveAllAddrs(EntityManager entityManager, Addr addr) {
    String[] addrTypeToBeCopied = { "ZP01", "ZD01", "ZI01", "ZS02" };
    for (String addrType : addrTypeToBeCopied) {
      saveAddrCopyForCy(entityManager, addr, addrType);
    }
  }

  /**
   * Checks if this is a cross-border scenario
   */
  protected boolean isCrossBorder(Addr addr) {
    return !"GB".equals(addr.getLandCntry());
  }

  protected boolean isCrossBorderLegacyAddressUKI(CmrtAddr addr) {
    boolean isCrossBorderLegacyAddressUKI = false;

    if (addr != null) {
      // check the last address line for the country

    }

    return isCrossBorderLegacyAddressUKI;
  }

  private void setEngBoAndAdminBo(Data data, EntityManager entityManager, String postCd) {

    List<Object[]> results = new ArrayList<Object[]>();
    String qryEngBoAndAdminBo = ExternalizedQuery.getSql("GET.ENG_BO.ADMIN_BO");
    PreparedQuery query = new PreparedQuery(entityManager, qryEngBoAndAdminBo);

    query.setParameter("POSTCD", postCd);
    query.setParameter("CNTRY", data.getCmrIssuingCntry());
    results = query.getResults();
    Object[] sResult = null;

    if (results != null && !results.isEmpty()) {
      sResult = results.get(0);
      data.setEngineeringBo(sResult[0] != null ? sResult[0].toString() : "");
      data.setAcAdminBo(sResult[1] != null ? sResult[1].toString() : "");
    } else {
      // retry
      postCd = postCd.substring(0, 2);
      query = new PreparedQuery(entityManager, qryEngBoAndAdminBo);

      query.setParameter("POSTCD", postCd);
      query.setParameter("CNTRY", data.getCmrIssuingCntry());
      results = query.getResults();

      if (results != null && !results.isEmpty()) {
        sResult = results.get(0);
        data.setEngineeringBo(sResult[0] != null ? sResult[0].toString() : "");
        data.setAcAdminBo(sResult[1] != null ? sResult[1].toString() : "");
      }
    }
  }

  public void setEngBoAndAdminBoOnLegacy(CmrtCust cust, Data data, EntityManager entityManager, String postCd) {

    List<Object[]> results = new ArrayList<Object[]>();
    String qryEngBoAndAdminBo = ExternalizedQuery.getSql("GET.ENG_BO.ADMIN_BO");
    PreparedQuery query = new PreparedQuery(entityManager, qryEngBoAndAdminBo);

    query.setParameter("POSTCD", postCd);
    query.setParameter("CNTRY", data.getCmrIssuingCntry());
    results = query.getResults();
    Object[] sResult = null;

    if (results != null && !results.isEmpty()) {
      sResult = results.get(0);
      cust.setCeBo(sResult[0].toString());
      cust.setAccAdminBo(sResult[1].toString());
    } else {
      // retry
      postCd = postCd.substring(0, 2);
      query = new PreparedQuery(entityManager, qryEngBoAndAdminBo);

      query.setParameter("POSTCD", postCd);
      query.setParameter("CNTRY", data.getCmrIssuingCntry());
      results = query.getResults();

      if (results != null && !results.isEmpty()) {
        sResult = results.get(0);
        cust.setCeBo(sResult[0].toString());
        cust.setAccAdminBo(sResult[1].toString());
      }
    }
  }

  private void updateLandCntry(EntityManager entityManager, Addr addr) throws Exception {
    // GRCYTR cross border, all landCntry should be the same
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("ADDR.UPDATE.LANDEDCNTRY"));
    query.setParameter("LAND_CNTRY", addr.getLandCntry());
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.executeSql();
  }

  private void updateLandCntryGR(EntityManager entityManager, Addr addr) throws Exception {
    PreparedQuery query = new PreparedQuery(entityManager, ExternalizedQuery.getSql("ADDR.UPDATE.LANDEDCNTRY.GR"));
    query.setParameter("LAND_CNTRY", addr.getLandCntry());
    query.setParameter("REQ_ID", addr.getId().getReqId());
    query.executeSql();
  }

  @Override
  public void doBeforeDPLCheck(EntityManager entityManager, Data data, List<Addr> addresses) throws Exception {

    // for cross borders per country...
    if ("CROSS".equalsIgnoreCase(data.getCustGrp())) {
      if (SystemLocation.GREECE.equals(data.getCmrIssuingCntry())) {
        return;
      }
    }

    // No DPL check for non-latin addresses
    if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
      for (Addr addr : addresses) {
        if (Arrays.asList("ZS01", "ZP01", "ZD01").contains(addr.getId().getAddrType())) {
          addr.setDplChkResult("N");
        }
      }
    } else if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
      for (Addr addr : addresses) {
        if ("ZP01".equals(addr.getId().getAddrType())) {
          addr.setDplChkResult("N");
        }
      }
    } else if (SystemLocation.GREECE.equals(data.getCmrIssuingCntry())) {
      for (Addr addr : addresses) {
        if ("ZP01".equals(addr.getId().getAddrType())) {
          addr.setDplChkResult("N");
        }
      }
    }

  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {

    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getEmbargoCd(), newData.getEmbargoCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EmbargoCode", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEmbargoCd(), "EmbargoCode", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getModeOfPayment(), newData.getModeOfPayment())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ModeOfPayment", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getModeOfPayment(), "ModeOfPayment", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "ISR", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "ISR", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepTeamMemberNo(), "ISR", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSalesTeamCd(), newData.getSalesTeamCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesSR", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesTeamCd(), "SalesSR", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesTeamCd(), "SalesSR", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCrosSubTyp(), newData.getCrosSubTyp())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "TypeOfCustomer", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCrosSubTyp(), "TypeOfCustomer", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCrosSubTyp(), "TypeOfCustomer", cmrCountry));
      results.add(update);
    }
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    switch (cntry) {
    case SystemLocation.UNITED_KINGDOM:
      return Arrays.asList(UKI_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    case SystemLocation.IRELAND:
      return Arrays.asList(UKI_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    case SystemLocation.ISRAEL:
      return Arrays.asList(ISRAEL_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    case SystemLocation.GREECE:
      return Arrays.asList(GREECE_CYPRUS_TURKEY_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    case SystemLocation.CYPRUS:
      return Arrays.asList(GREECE_CYPRUS_TURKEY_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    case SystemLocation.TURKEY:
      return Arrays.asList(GREECE_CYPRUS_TURKEY_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
    }
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry())) {
      LOG.debug("Switching mailing address locations..");
      String sql = null;
      PreparedQuery query = null;
      for (int i = 1; i <= 3; i++) {
        sql = ExternalizedQuery.getSql("ISRAEL.REVERT_MAILING." + i);
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", data.getId().getReqId());
        query.executeSql();

        sql = ExternalizedQuery.getSql("ISRAEL.REVERT_MAILING_RDC." + i);
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("REQ_ID", data.getId().getReqId());
        query.executeSql();

      }

    }
    if (SystemLocation.IRELAND.equals(data.getCmrIssuingCntry()) || SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry())
        || SystemLocation.GREECE.equals(data.getCmrIssuingCntry()) || SystemLocation.CYPRUS.equals(data.getCmrIssuingCntry())
        || SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
      LOG.debug("Handling AbbrevLocn via doAfterImport..");
      autoSetAbbrevNmAfterImport(entityManager, admin, data);
      autoSetAbbrevLocnAfterImport(entityManager, admin, data);
    }

    if (SystemLocation.ISRAEL.equals(data.getCmrIssuingCntry()) && data.getAbbrevNm() != null && data.getAbbrevNm().length() > 22) {
      data.setAbbrevNm(data.getAbbrevNm().substring(0, 22));
      entityManager.merge(data);
      entityManager.flush();
    }
    if (SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
      autoSetAbbrevLocnAfterImport(entityManager, admin, data);
    }
    if (SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry()) || SystemLocation.IRELAND.equals(data.getCmrIssuingCntry())
        || SystemLocation.SPAIN.equals(data.getCmrIssuingCntry())) {
      autoSetHwMasterInstallFlagAfterImport(entityManager, admin, data);
    }

  }

  private void autoSetHwMasterInstallFlagAfterImport(EntityManager entityManager, Admin admin, Data data) {
    // BATCH.GET_ADDR_FOR_SAP_NO
    String sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_FOR_SAP_NO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    List<Addr> listAddrs = query.getResults(Addr.class);
    HashMap<String, Integer> sequences = new HashMap<String, Integer>();

    if (listAddrs != null && listAddrs.size() > 0) {
      for (Addr singleAddr : listAddrs) {
        if (!sequences.containsKey(singleAddr.getId().getAddrSeq())) {
          sequences.put(singleAddr.getId().getAddrSeq(), 0);
        }
        sequences.put(singleAddr.getId().getAddrSeq(), sequences.get(singleAddr.getId().getAddrSeq()) + 1);
      }

      for (Addr singleAddr : listAddrs) {
        // LD.GET.HW_MASTER_INSTALL_FLAG
        sql = ExternalizedQuery.getSql("LD.GET.HW_MASTER_INSTALL_FLAG");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("KUNNR", singleAddr.getSapNo());
        query.setForReadOnly(true);
        KunnrExt singleKE = query.getSingleResult(KunnrExt.class);

        if (sequences.get(singleAddr.getId().getAddrSeq()) > 1) {
          if ("ZI01".equals(singleAddr.getId().getAddrType())) {
            if (singleKE != null) {
              singleAddr.setHwInstlMstrFlg(singleKE.getHwInstlMstrFlg());
            } else {
              singleAddr.setHwInstlMstrFlg("");
            }
          } else {
            singleAddr.setHwInstlMstrFlg("");
          }
        } else {
          if (singleKE != null) {
            singleAddr.setHwInstlMstrFlg(singleKE.getHwInstlMstrFlg());
          } else {
            singleAddr.setHwInstlMstrFlg("");
          }

        }

        entityManager.merge(singleAddr);
        entityManager.flush();
      }
    }

    sql = ExternalizedQuery.getSql("BATCH.GET_ADDR_RDC_FOR_SAP_NO");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    List<AddrRdc> listAddrsRdc = query.getResults(AddrRdc.class);

    if (listAddrsRdc != null && listAddrsRdc.size() > 0) {
      for (AddrRdc singleAddrRdc : listAddrsRdc) {
        // LD.GET.HW_MASTER_INSTALL_FLAG
        sql = ExternalizedQuery.getSql("LD.GET.HW_MASTER_INSTALL_FLAG");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("KUNNR", singleAddrRdc.getSapNo());
        query.setForReadOnly(true);
        KunnrExt singleKE = query.getSingleResult(KunnrExt.class);

        if (sequences.get(singleAddrRdc.getId().getAddrSeq()) > 1) {
          if ("ZI01".equals(singleAddrRdc.getId().getAddrType())) {
            if (singleKE != null) {
              singleAddrRdc.setHwInstlMstrFlg(singleKE.getHwInstlMstrFlg());
            } else {
              singleAddrRdc.setHwInstlMstrFlg("");
            }
          } else {
            singleAddrRdc.setHwInstlMstrFlg("");
          }
        } else {
          if (singleKE != null) {
            singleAddrRdc.setHwInstlMstrFlg(singleKE.getHwInstlMstrFlg());
          } else {
            singleAddrRdc.setHwInstlMstrFlg("");
          }
        }

        entityManager.merge(singleAddrRdc);
        entityManager.flush();
      }
    }

  }

  private void autoSetAbbrevNmAfterImport(EntityManager entityManager, Admin admin, Data data) {
    String abbrevNmValue = null;
    String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setParameter("ADDR_TYPE", "ZI01");
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      abbrevNmValue = results.get(0);
    }

    String abbrevNmSoldto = null;
    String sql2 = ExternalizedQuery.getSql("QUERY.ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP");
    PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
    query2.setParameter("REQ_ID", data.getId().getReqId());
    query2.setParameter("ADDR_TYPE", "ZS01");
    List<String> record = query2.getResults(String.class);
    if (record != null && !record.isEmpty()) {
      abbrevNmSoldto = record.get(0);
    }

    if (abbrevNmValue != null && abbrevNmValue.length() > 22) {
      abbrevNmValue = abbrevNmValue.substring(0, 22);
    }
    if (SystemLocation.IRELAND.equals(data.getCmrIssuingCntry())) {
      if (admin.getReqType().equalsIgnoreCase("C")) {
        if (data.getCustSubGrp() == null) {
        } else if (data.getCustSubGrp() != null) {
          if (data.getCustSubGrp().equalsIgnoreCase("COOEM")) {
            if (abbrevNmValue != null && abbrevNmValue.length() > 18) {
              abbrevNmValue = abbrevNmValue.substring(0, 18) + " OEM";
            } else {
              abbrevNmValue = abbrevNmValue + " OEM";
            }
          }
        }
        data.setAbbrevNm(abbrevNmValue);
      }
    } else if (SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry())) {
      if (admin.getReqType().equalsIgnoreCase("C")) {
        data.setAbbrevNm(abbrevNmValue);
      }
    } else if (SystemLocation.GREECE.equals(data.getCmrIssuingCntry()) || SystemLocation.CYPRUS.equals(data.getCmrIssuingCntry())
        || SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
      if (admin.getReqType().equalsIgnoreCase("C")) {
        if (data.getCustSubGrp() == null) {
        } else if (data.getCustSubGrp() != null) {
          if (data.getCustSubGrp().equalsIgnoreCase("COOEM")) {
            if (abbrevNmValue != null && abbrevNmValue.length() > 18) {
              abbrevNmValue = abbrevNmValue.substring(0, 18) + " OEM";
            } else {
              abbrevNmValue = abbrevNmValue + " OEM";
            }
          }
        }
        data.setAbbrevNm(abbrevNmValue);
      }
    }

    String processingType = PageManager.getProcessingType(SystemLocation.GREECE, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (SystemLocation.GREECE.equalsIgnoreCase(data.getCmrIssuingCntry())) {
        if ((admin.getProspLegalInd() != null && admin.getProspLegalInd().equals("Y")) || admin.getReqType().equalsIgnoreCase("C")) {
          data.setAbbrevNm(abbrevNmSoldto);
          if (abbrevNmSoldto != null && abbrevNmSoldto.length() > 22) {
            data.setAbbrevNm(abbrevNmSoldto.substring(0, 22));
          }
        }
      }
    }

    entityManager.merge(data);
    entityManager.flush();

  }

  private void autoSetAbbrevLocnAfterImport(EntityManager entityManager, Admin admin, Data data) {
    if (SystemLocation.IRELAND.equals(data.getCmrIssuingCntry())) {
      String abbrevLocnValue = null;
      if (admin.getReqType().equalsIgnoreCase("C")) {
        if (data.getCustSubGrp() == null) {
        } else if (data.getCustSubGrp() != null) {

          if (data.getCustSubGrp().equalsIgnoreCase("CROSS")) {
            String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP");
            PreparedQuery query = new PreparedQuery(entityManager, sql);
            query.setParameter("REQ_ID", data.getId().getReqId());
            query.setParameter("ADDR_TYPE", "ZS01");
            List<String> results = query.getResults(String.class);
            if (results != null && !results.isEmpty()) {
              abbrevLocnValue = results.get(0);
            }
          } else {
            String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CITY1.BY_REQID_ADDRTYP");
            PreparedQuery query = new PreparedQuery(entityManager, sql);
            query.setParameter("REQ_ID", data.getId().getReqId());
            query.setParameter("ADDR_TYPE", "ZS01");
            List<String> results = query.getResults(String.class);
            if (results != null && !results.isEmpty()) {
              abbrevLocnValue = results.get(0);
            }
          }
          if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
            abbrevLocnValue = abbrevLocnValue.substring(0, 12);
          }
        }
        data.setAbbrevLocn(abbrevLocnValue);
      }
      entityManager.merge(data);
      entityManager.flush();

    } else if (SystemLocation.UNITED_KINGDOM.equals(data.getCmrIssuingCntry())) {
      String abbrevLocnValue = null;
      if (admin.getReqType().equalsIgnoreCase("C")) {
        if (data.getCustSubGrp() == null) {
        } else if (data.getCustSubGrp() != null) {

          String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.POSTCD.BY_REQID_ADDRTYP");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("REQ_ID", data.getId().getReqId());
          query.setParameter("ADDR_TYPE", "ZS01");
          List<String> results = query.getResults(String.class);
          if (results != null && !results.isEmpty()) {
            abbrevLocnValue = results.get(0);
          }

          if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
            abbrevLocnValue = abbrevLocnValue.substring(0, 12);
          }
        }
        data.setAbbrevLocn(abbrevLocnValue);
      }
      entityManager.merge(data);
      entityManager.flush();

    } else if (SystemLocation.GREECE.equals(data.getCmrIssuingCntry()) || SystemLocation.CYPRUS.equals(data.getCmrIssuingCntry())
        || SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
      String abbrevLocnValue = null;
      String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CITY1.BY_REQID");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", data.getId().getReqId());
      List<String> results = query.getResults(String.class);
      if (results != null && !results.isEmpty()) {
        abbrevLocnValue = results.get(0);
      }
      if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
        abbrevLocnValue = abbrevLocnValue.substring(0, 12);
      }
      if (admin.getReqType().equalsIgnoreCase("C")) {
        data.setAbbrevLocn(abbrevLocnValue);
      }

      entityManager.merge(data);
      entityManager.flush();
    } else if (SystemLocation.ITALY.equals(data.getCmrIssuingCntry())) {
      // Code Story 1374607 : Mukesh
      String abbrevLocnValue = null;
      String sql = ExternalizedQuery.getSql("QUERY.ADDR.GET.CITY1.BY_REQID");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", data.getId().getReqId());
      List<String> results = query.getResults(String.class);
      if (results != null && !results.isEmpty()) {
        abbrevLocnValue = results.get(0);
      }
      if (abbrevLocnValue != null && abbrevLocnValue.length() > 12) {
        abbrevLocnValue = abbrevLocnValue.substring(0, 12);
      }
      if (admin.getReqType().equalsIgnoreCase("C")) {
        data.setAbbrevLocn(abbrevLocnValue);
      }

      entityManager.merge(data);
      entityManager.flush();
    }
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "ADDR_TXT", "DEPT", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX"));

    if (SystemLocation.UNITED_KINGDOM.equals(cmrIssuingCntry) || SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      fields.add("ADDR_TXT_2");
      fields.add("CUST_PHONE");
      fields.add("HW_INSTL_MSTR_FLG");
    }
    if (SystemLocation.SPAIN.equals(cmrIssuingCntry)) {
      fields.add("HW_INSTL_MSTR_FLG");
    }
    if (SystemLocation.GREECE.equals(cmrIssuingCntry) || SystemLocation.TURKEY.equals(cmrIssuingCntry)
        || SystemLocation.CYPRUS.equals(cmrIssuingCntry)) {
      fields.remove("DEPT");
      fields.add("ADDR_TXT_2");
      fields.add("CUST_PHONE");
    }
    return fields;
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    Addr address = getAddressByType(entityManager, "ZS01", data.getId().getReqId());
    if (SystemLocation.CYPRUS.equals(data.getCmrIssuingCntry()) && StringUtils.isNotBlank(address.getImportInd())
        && address.getImportInd().equalsIgnoreCase("D")) {
      copyAndSaveAddressesDNBImport(entityManager, data.getId().getReqId(), address);
    }
  }

  public static boolean isITIssuingCountry(String cmrIssuingCntry) {

    boolean isITIssuingCntry = false;

    if (SystemLocation.ITALY.equalsIgnoreCase(cmrIssuingCntry)) {
      isITIssuingCntry = true;
    } else {
      isITIssuingCntry = false;
    }

    return isITIssuingCntry;

  }

  public static boolean isEMEAIssuingCountry(String cntry) {
    return EMEA_COUNTRY_VAL.contains(cntry);
  }

  public static boolean isSpainIssuingCountry(String cntry) {
    return SystemLocation.SPAIN.equals(cntry);
  }

  public static boolean isAutoMassChangeTemplateEnabled(String cntry) {
    return ENABLE_MASSCHANGE_AUTO_TEMPLATE.contains(cntry);
  }

  @Override
  protected String getAddressTypeByUse(String addressUse) {
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
    }
    return null;
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = null;
    return newAddrSeq;

  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newSeq = null;
    return newSeq;
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.UNITED_KINGDOM.equals(cmrIssuingCntry) || SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01", "ZS01", "ZI01");
    } else if (SystemLocation.GREECE.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01", "ZS01", "ZD01", "ZI01");
    } else if (SystemLocation.TURKEY.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01", "ZS01");
    } else if (SystemLocation.CYPRUS.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01", "ZS01", "ZD01", "ZI01", "ZS02");
    }
    return null;
  }

  @Override
  public List<String> getOptionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.UNITED_KINGDOM.equals(cmrIssuingCntry) || SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZD01", "ZS02");
    }
    return null;
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.UNITED_KINGDOM.equals(cmrIssuingCntry) || SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZD01", "ZI01");
    }
    if (SystemLocation.TURKEY.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZP01");
    }
    if (SystemLocation.GREECE.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZD01", "ZI01");
    }
    if (SystemLocation.CYPRUS.equals(cmrIssuingCntry)) {
      return Arrays.asList("ZD01", "ZI01");
    }

    return null;
  }

  @Override
  public List<String> getReservedSeqForLDSeqGen(String cmrIssuingCntry) {
    if (SystemLocation.GREECE.equals(cmrIssuingCntry)) {
      return Arrays.asList("5");
    }
    return null;
  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
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
    map.put("##ISR", "repTeamMemberNo");
    // *abner revert begin
    // map.put("##CommercialFinanced", "commercialFinanced");
    // *abner revert end
    return map;
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "SPECIAL_TAX_CD", "VAT", "ISIC_CD", "EMBARGO_CD", "COLLECTION_CD", "ABBREV_NM",
        "SENSITIVE_FLAG", "CLIENT_TIER", "COMPANY", "INAC_TYPE", "INAC_CD", "ISU_CD", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "MEM_LVL",
        "BP_REL_TYPE", "MODE_OF_PAYMENT", "CROS_SUB_TYP"));
    return fields;
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    if (INVALID_CUST_FOR_COUNTRY.contains(cmrIssuingCntry)) {
      return true;
    }
    return false;
  }

  private void setMailingAddressFromLegacy(EntityManager entityManager, String cmrIssuingCntry, String addressKey, FindCMRRecordModel address) {
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      if ("Y".equals(cmrtAddr.getIsAddrUseMailing())) {
        address.setCmrAddrSeq(cmrtAddr.getId().getAddrNo());
        handleSOFAddressImportUKI(entityManager, cmrIssuingCntry, address, addressKey, cmrtAddr);
      }
    }
  }

  protected void handleSOFAddressImportUKI(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey,
      CmrtAddr cmrtAddr) {

    // UKI order
    /*
     * line1= name 1 line2=name2/street con't/att+phone/po box/street
     * line3=street con't/att+phone/po box/street/city line4=street/city/postal
     * code/cross border country line5=city/postal code/cross border country
     * line6=postal code/cross border country
     */

    String line1 = cmrtAddr.getAddrLine1();
    String line2 = cmrtAddr.getAddrLine2();
    String line3 = cmrtAddr.getAddrLine3();
    String line4 = cmrtAddr.getAddrLine4();
    String line5 = cmrtAddr.getAddrLine5();
    String line6 = cmrtAddr.getAddrLine6();

    // line1 = Customer name
    address.setCmrName1Plain(line1);
    address.setCmrName(line1);

    String[] lines = new String[] { line1, line2, line3, line4, line5, line6 };
    int lineNo = 1, attLine = 0, boxLine = 0, zipLine = 0, streetLine = 0;
    for (String line : lines) {
      if (isAttn(line)) {
        address.setCmrName4(line.substring(4));
        attLine = lineNo;
      } else if (isPOBox(line)) {
        String tempLine = line.replace("PO BOX", "");
        address.setCmrPOBox(tempLine);
        boxLine = lineNo;
      } else if (isStreet(line)) {
        address.setCmrStreetAddress(line);
        streetLine = lineNo;
      } else if (handlePostCity(line, addressKey, address, cmrtAddr)) {
        zipLine = lineNo;
      }
      lineNo++;
    }

    // attn and customer name cont lines
    if (attLine == 3) {
      if (!line2.contains("PO BOX")) {
        address.setCmrName2Plain(line2);
      }
    }

    // poBox & zipCode lines
    if (zipLine == 3) {
      if (attLine == 0 && boxLine == 0) {
        address.setCmrStreetAddress(line2);
      }
      setLandedCountry(entityManager, address, line4);
      address.setCmrPostalCode(StringUtils.isBlank(address.getCmrPostalCode()) ? line3 : address.getCmrPostalCode());
    } else if (zipLine == 4) {
      if (streetLine == 3 && attLine == 0 && boxLine == 0) {
        if (!line2.contains("PO BOX")) {
          address.setCmrName2Plain(line2);
        }
      } else {
        address.setCmrStreetAddress(line2);
        address.setCmrCity(StringUtils.isBlank(address.getCmrCity()) ? line3 : address.getCmrCity());
      }
      setLandedCountry(entityManager, address, line5);
      address.setCmrPostalCode(StringUtils.isBlank(address.getCmrPostalCode()) ? line4 : address.getCmrPostalCode());
    } else if (zipLine == 5) {
      if (streetLine == 4) {
        if (boxLine == 0) {
          if (!line3.contains("PO BOX")) {
            address.setCmrStreetAddressCont(line3);
          }
        }
        if (attLine == 0) {
          if (!line2.contains("PO BOX")) {
            address.setCmrName2Plain(line2);
          }
        }
      } else {
        address.setCmrCity(StringUtils.isBlank(address.getCmrCity()) ? line4 : address.getCmrCity());
        if (attLine == 0 && boxLine == 0) {
          if (!line2.contains("PO BOX")) {
            address.setCmrName2Plain(line2);
          }
        }
      }
      setLandedCountry(entityManager, address, line6);
      address.setCmrPostalCode(StringUtils.isBlank(address.getCmrPostalCode()) ? line5 : address.getCmrPostalCode());
      // if (StringUtils.isBlank(address.getCmrStreetAddress())) {
      // address.setCmrStreetAddress(line3);
      // }
    } else if (zipLine == 6) {
      address.setCmrCity(StringUtils.isBlank(address.getCmrCity()) ? line5 : address.getCmrCity());
      if (!line3.contains("PO BOX")) {
        address.setCmrStreetAddressCont(line3);
      }
      if (attLine == 0 && boxLine == 0) {
        if (!line3.contains("PO BOX")) {
          address.setCmrStreetAddressCont(line3);
        }
      }
      address.setCmrPostalCode(StringUtils.isBlank(address.getCmrPostalCode()) ? line6 : address.getCmrPostalCode());
      if (StringUtils.isBlank(address.getCmrStreetAddress())) {
        address.setCmrStreetAddress(line4);
      }
    }

    if (StringUtils.isEmpty(address.getCmrCountryLanded()) && (!StringUtils.isEmpty(line6) || !StringUtils.isEmpty(line5))) {
      // try landed country on line 6/5 all the time
      String lineToCheck = !StringUtils.isEmpty(line6 != null ? line6.trim() : "") ? line6 : line5;

      if (lineToCheck != null && StringUtils.isEmpty(lineToCheck.trim())) {
        lineToCheck = line4;
      }

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

    LOG.debug(addressKey + " " + address.getCmrAddrSeq());
    LOG.debug("Name: " + address.getCmrName1Plain());
    LOG.debug("Name 2: " + address.getCmrName2Plain());
    LOG.debug("Attn: " + address.getCmrName4());
    LOG.debug("Street: " + address.getCmrStreetAddress());
    LOG.debug("Street 2: " + address.getCmrStreetAddressCont());
    LOG.debug("PO Box: " + address.getCmrPOBox());
    LOG.debug("Postal Code: " + address.getCmrPostalCode());
    LOG.debug("Phone: " + address.getCmrCustPhone());
    LOG.debug("City: " + address.getCmrCity());
    LOG.debug("Country: " + address.getCmrCountryLanded());
  }

  private void setLandedCountryFromLegacy(EntityManager entityManager, FindCMRRecordModel address, String line) {
    if (!StringUtils.isEmpty(line)) {
      String countryCd = getCountryCode(entityManager, line);
      if (!StringUtils.isEmpty(countryCd)) {
        address.setCmrCountryLanded(countryCd);
      } else {
        try {
          SuppCntry suppCntry = getCurrentSuppCntryRecord(address.getCmrIssuedBy(), entityManager);
          address.setCmrCountryLanded(suppCntry.getDefaultLandedCntry());
        } catch (Exception e) {
          LOG.error(e.getMessage());
        }
      }
    }
  }

  private void setLandedCountry(EntityManager entityManager, FindCMRRecordModel address, String line) {
    if (!StringUtils.isEmpty(line)) {
      String countryCd = getCountryCode(entityManager, line);
      if (!StringUtils.isEmpty(countryCd)) {
        address.setCmrCountryLanded(countryCd);
      }
    }
  }

  private SuppCntry getCurrentSuppCntryRecord(String cntry, EntityManager entityManager) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.SUPP_CNTRY_BY_CNTRY_CD");
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY_CD", cntry);
    return q.getSingleResult(SuppCntry.class);
  }

  private boolean handlePostCity(String data, String addressKey, FindCMRRecordModel address, CmrtAddr cmrtAddr) {
    if (StringUtils.isEmpty(data)) {
      return false;
    }

    // check if postCode or city matches the line
    String postCd = cmrtAddr.getZipCode();
    String city = cmrtAddr.getCity();
    postCd = !StringUtils.isEmpty(postCd) ? postCd : "";
    city = !StringUtils.isEmpty(city) ? city : "";

    if (data.trim().startsWith(postCd.trim()) || data.trim().endsWith(city.trim())) {
      address.setCmrPostalCode(postCd);
      address.setCmrCity(city);
      return true;
    }
    return false;
  }

  private boolean checkIZP01ExistsOnRDCUKI(EntityManager entityManager, String cmrIssuingCntry, String cmrNo) {
    String sql = ExternalizedQuery.getSql("CHECK.IF_RDC_HAS_MAILING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMRNO", cmrNo);
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setForReadOnly(true);
    return query.exists();
  }

  private String removeATT(String addrLine) {
    addrLine = StringUtils.replace(addrLine, "ATT ", "");
    addrLine = StringUtils.replace(addrLine, "ATTN ", "");
    addrLine = StringUtils.replace(addrLine, "ATT.", "");
    addrLine = StringUtils.replace(addrLine, "ATT", "");
    addrLine = StringUtils.replace(addrLine, "ATT:", "");
    addrLine = StringUtils.replace(addrLine, "ATT :", "");
    addrLine = StringUtils.replace(addrLine, "ATTN:", "");
    addrLine = StringUtils.replace(addrLine, "ATTN :", "");
    addrLine = StringUtils.replace(addrLine, "ATTN.", "");
    addrLine = StringUtils.replace(addrLine, "ATTN", "");

    return addrLine.trim();
  }

  private DataRdc getOldData(EntityManager entityManager, String reqId) {
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<DataRdc> records = query.getResults(DataRdc.class);
    DataRdc oldData = new DataRdc();

    if (records != null && records.size() > 0) {
      oldData = records.get(0);
    } else {
      oldData = null;
    }

    return oldData;
  }

  public String getPostalCd_LandedCntry(EntityManager entityManager, Data data) throws Exception {
    List<Object[]> results = new ArrayList<Object[]>();
    String retVal = "";
    String postCd = "*";
    String qryLandedCntryPostCD = ExternalizedQuery.getSql("GET_LANDED_AND_POSTAL_CD");
    PreparedQuery query = new PreparedQuery(entityManager, qryLandedCntryPostCD);

    query.setParameter("REQ_ID", data.getId().getReqId());
    results = query.getResults();

    if (results != null && !results.isEmpty()) {
      Object[] sResult = results.get(0);
      String landCntry = sResult[0].toString();
      if (sResult[1] != null && !"".equals(sResult[1]))
        postCd = sResult[1].toString();

      retVal = landCntry + "@" + postCd;
    }
    return retVal;
  }

  public boolean isTemplateFieldForDualMarked(String columnName, boolean isCrossBorder) {
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
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    // add this condition entry for Cyprus in main branch
    if (SystemLocation.CYPRUS.equals(issuingCountry)) {
      return true;
    } else {
      return false;
    }

  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    XSSFCell currCell = null;
    for (String name : CY_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      if (sheet != null) {
        for (Row row : sheet) {
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
            String dataCmrNo = ""; // 0
            String cmrNo = ""; // 0
            String seqNo = "";// 1
            String street = ""; // 4
            String addressCont = ""; // 5
            String attPerson = ""; // 6
            String localPostal = ""; // 7
            String localCity = ""; // 8
            String crossCity = ""; // 9
            String cbPostal = ""; // 10
            String phoneNo = ""; // 12
            String poBox = ""; // 13
            String landCountry = ""; // 11
            String enterpriseNo = ""; // 5
            String isuCd = ""; // 7
            String ctc = ""; // 8
            List<String> checkList = null;
            long count = 0;
            if (row.getRowNum() == 2001) {
              continue;
            }

            if (!"Data".equalsIgnoreCase(sheet.getSheetName())) {
              // iterate all the rows and check each column value
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(1);
              seqNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(4);
              street = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(5);
              addressCont = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(6);
              attPerson = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(9);
              localPostal = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(7);
              localCity = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(8);
              crossCity = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(10);
              cbPostal = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(12);
              phoneNo = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(13);
              poBox = validateColValFromCell(currCell);

              currCell = (XSSFCell) row.getCell(11);
              landCountry = validateColValFromCell(currCell);
            }

            checkList = Arrays.asList(street, addressCont, poBox, attPerson);
            count = checkList.stream().filter(field -> !field.isEmpty()).count();

            if ("Mailing Address".equalsIgnoreCase(sheet.getSheetName())) {
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                poBox = df.formatCellValue(row.getCell(13));
              }
            }

            if ("Mailing Address".equalsIgnoreCase(sheet.getSheetName()) || "Shipping Address (Update)".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(12);
              phoneNo = validateColValFromCell(currCell);
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                phoneNo = df.formatCellValue(row.getCell(12));
              }
            }

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              dataCmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              enterpriseNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              isuCd = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              ctc = validateColValFromCell(currCell);
            }

            TemplateValidation error = new TemplateValidation(name);
            if (!StringUtils.isEmpty(crossCity) && !StringUtils.isEmpty(localCity)) {
              LOG.trace("Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty. >> ");
              error.addError((row.getRowNum() + 1), "City",
                  "Cross Border City and Local City must not be populated at the same time. If one is populated, the other must be empty.");
              // validations.add(error);
            }
            if (!StringUtils.isEmpty(cbPostal) && !StringUtils.isEmpty(localPostal)) {
              LOG.trace("Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                  + "If one is populated, the other must be empty. >>");
              error.addError((row.getRowNum() + 1), "Postal Code",
                  "Cross Border Postal Code and Local Postal Code must not be populated at the same time. "
                      + "If one is populated, the other must be empty.");
              // validations.add(error);
            }

            if (!StringUtils.isEmpty(crossCity) && !StringUtils.isEmpty(cbPostal)) {
              int maxlengthcomputed = crossCity.length() + cbPostal.length();
              if (maxlengthcomputed > 32) {
                LOG.trace("Crossborder city and crossborder postal code should have a maximun of 30 characters.");
                error.addError((row.getRowNum() + 1), "Crossborder City/Postal",
                    "Crossborder city and crossborder postal code should have a maximun of 30 characters.");
                // validations.add(error);
              }
            }
            if (count > 2) {
              LOG.trace("Out of Street, Address Con't, PO BOX and Att Person only 2 can be filled at the same time .");
              error.addError((row.getRowNum() + 1), "Address Con't/PO BOX",
                  "Out of Street, Address Con't, PO BOX and Att Person only 2 can be filled at the same time . ");
              // validations.add(error);
              count = 0;
            }
            if (!StringUtils.isEmpty(crossCity) && !StringUtils.isEmpty(localPostal)) {
              LOG.trace(
                  "Cross Border City and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              error.addError((row.getRowNum() + 1), "Local Postal Code",
                  "Cross Border City and Local Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              // validations.add(error);
            }
            if (!StringUtils.isEmpty(localCity) && !StringUtils.isEmpty(cbPostal)) {
              LOG.trace(
                  "Local City and Cross Border Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              error.addError((row.getRowNum() + 1), "Local City",
                  "Local City and Cross Border Postal Code must not be populated at the same time. If one is populated, the other must be empty.");
              // validations.add(error);
            }

            if (!StringUtils.isEmpty(landCountry)) {
              if (!("CY").equals(landCountry) && (!StringUtils.isEmpty(localCity) || !StringUtils.isEmpty(localPostal))) {
                LOG.trace("Landed Country should be CY for Local Scenario.");
                error.addError((row.getRowNum() + 1), "Landed Country", "Landed Country should be CY for Local Scenario.");
                // validations.add(error);
              } else if (("CY").equals(landCountry) && (!StringUtils.isEmpty(crossCity) || !StringUtils.isEmpty(cbPostal))) {
                LOG.trace("Landed Country shouldn't be CY for Cross Borders.");
                error.addError((row.getRowNum() + 1), "Landed Country", "Landed Country shouldn't be CY for Cross Borders.");
                // validations.add(error);
              }
            }

            if (!StringUtils.isEmpty(localPostal) && !localPostal.matches("-?\\d+(\\.\\d+)?")) {
              LOG.trace("Local postal code should have numeric values only.");
              error.addError((row.getRowNum() + 1), "Local Postal Code", "Only numeric values are allowed.");
              // validations.add(error);
            }

            if ("Mailing Address".equalsIgnoreCase(sheet.getSheetName()) || "Billing Address".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isEmpty(street) && !StringUtils.isEmpty(poBox)) {
                LOG.trace("Note that Street/PO Box cannot be filled at same time. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Street/PO Box",
                    "Note that Street/PO Box cannot be filled at same time. Please fix and upload the template again.");
                // validations.add(error);
              }
              if (poBox.contains("+")) {
                LOG.trace("Please input value in numeric format. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "PO Box", "Please input value in numeric format. Please fix and upload the template again.");
                // validations.add(error);
              }
            }
            if (StringUtils.isEmpty(street) && !StringUtils.isEmpty(addressCont)) {
              LOG.trace("Address Con't cannot be filled if Street is empty .Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Street/Address Con't",
                  "Address Con't cannot be filled if Street is empty .Please fix and upload the template again.");
              // validations.add(error);
            }

            if (!StringUtils.isEmpty(addressCont) && !StringUtils.isEmpty(attPerson)) {
              LOG.trace("Note that Address Con't/Att. Person cannot be filled at same time. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Address Con't/Att. Person",
                  "Note that Address Con't/Att. Person cannot be filled at same time. Please fix and upload the template again.");
              // validations.add(error);
            }

            if (!StringUtils.isBlank(cmrNo) && StringUtils.isBlank(seqNo)) {
              LOG.trace("Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Address Sequence No.",
                  "Note that CMR No. and Sequence No. should be filled at same time. Please fix and upload the template again.");
              // validations.add(error);
            }

            if (StringUtils.isEmpty(dataCmrNo)) {
              if (row.getRowNum() == 1) {
                return;
              }
              LOG.trace("Note that CMR No. is mandatory. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "CMR No.", "Note that CMR No. is mandatory. Please fix and upload the template again.");
              // validations.add(error);
            }

            if ("Mailing Address".equalsIgnoreCase(sheet.getSheetName()) || "Shipping Address (Update)".equalsIgnoreCase(sheet.getSheetName())) {
              if (phoneNo.contains("+") || (!StringUtils.isEmpty(phoneNo) && !phoneNo.matches("-?\\d+(\\.\\d+)?"))) {
                LOG.trace("Please input value in numeric format. Please fix and upload the template again.");
                error.addError((row.getRowNum() + 1), "Phone No.", "Please input value in numeric format. Please fix and upload the template again.");
                // validations.add(error);
              }
            }

            if (localCity.contains("@") && localCity.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Local City", "Field contains invalid character. Please fix and upload the template again.");
              // validations.add(error);
            }

            if (crossCity.contains("@") && crossCity.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Cross Border City",
                  "Field contains invalid character. Please fix and upload the template again.");
              // validations.add(error);
            }

            if (localPostal.contains("@") && localPostal.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Local Postal Code",
                  "Field contains invalid character. Please fix and upload the template again.");
              // validations.add(error);
            }

            if (cbPostal.contains("@") && cbPostal.length() > 0) {
              LOG.trace("Field contains invalid character. Please fix and upload the template again.");
              error.addError((row.getRowNum() + 1), "Cross Border Postal Code",
                  "Field contains invalid character. Please fix and upload the template again.");
              // validations.add(error);
            }

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              if (!StringUtils.isBlank(enterpriseNo)) {
                if (!StringUtils.isNumeric(enterpriseNo) && !enterpriseNo.equals("@@@@@@")) {
                  LOG.trace("Enterprise number should have numeric values only.");
                  error.addError((row.getRowNum() + 1), "Enterprise No.", "Enterprise number should have numeric values only. ");
                }
              }
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
            if (error.hasErrors()) {
              validations.add(error);
            }
          }
        }
      }
    }
  }

  @Override
  public void addSummaryUpdatedFieldsForAddress(RequestSummaryService service, String cmrCountry, String addrTypeDesc, String sapNumber,
      UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager) {

    if (SystemLocation.UNITED_KINGDOM.equals(cmrCountry) || SystemLocation.IRELAND.equals(cmrCountry)) {
      if (!equals(addr.getHwInstlMstrFlg(), addr.getHwInstlMstrFlgOld())) {
        UpdatedNameAddrModel update = new UpdatedNameAddrModel();
        update.setAddrType(addrTypeDesc);
        update.setSapNumber(sapNumber);
        update.setDataField(PageManager.getLabel(cmrCountry, "", "HW Master Install Flag"));
        update.setNewData(addr.getHwInstlMstrFlg());
        update.setOldData(addr.getHwInstlMstrFlgOld());
        results.add(update);
      }
    }
  }

  public String getaddAddressAdrnr(EntityManager entityManager, String mandt, String kunnr, String ktokd, String seq) {
    String adrnr = "";
    String sql = ExternalizedQuery.getSql("TR.GETADRNR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KATR6", SystemLocation.TURKEY);
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

  public Sadr getTRAddtlAddr(EntityManager entityManager, String adrnr, String mandt) {
    Sadr sadr = new Sadr();
    String qryAddlAddr = ExternalizedQuery.getSql("GET.TR_SADR_BY_ID");
    PreparedQuery query = new PreparedQuery(entityManager, qryAddlAddr);
    query.setParameter("ADRNR", adrnr);
    query.setParameter("MANDT", mandt);
    sadr = query.getSingleResult(Sadr.class);

    return sadr;
  }

  private void copyAddrData(FindCMRRecordModel record, Addr addr) {
    record.setCmrAddrTypeCode("ZP01");
    record.setCmrAddrSeq("00002");
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

  private Addr getCurrentInstallingAddress(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("TR.GETINSTALLING");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getSingleResult(Addr.class);
  }

  private CmrtAddr getLegacyMailingAddress(EntityManager entityManager, String cmrNo) {
    String sql = ExternalizedQuery.getSql("TR.GETLEGACYMAIL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_NO", cmrNo);
    query.setForReadOnly(true);
    return query.getSingleResult(CmrtAddr.class);
  }

  private CmrtCustExt getCustExt(EntityManager entityManager, String cmrCntry, String cmrNo) {
    String sql = ExternalizedQuery.getSql("LEGACYD.GETCEXT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", cmrCntry);
    query.setParameter("CMR_NO", cmrNo);
    return query.getSingleResult(CmrtCustExt.class);
  }

  // START -- missing code greece code
  private void saveAddrCopyForGR(EntityManager entityManager, Addr addr, String addrType) {
    Addr addrCopy = SerializationUtils.clone(addr);
    addrCopy.getId().setAddrType(addrType);

    if (addrType.equals("ZP01")) {
      addrCopy.setCustPhone(null);
    }

    entityManager.persist(addrCopy);
    entityManager.flush();
  }

  // END -- missing code greece code

  private void saveAddrCopyForCy(EntityManager entityManager, Addr addr, String addrType) {
    Addr addrCopy = SerializationUtils.clone(addr);
    addrCopy.getId().setAddrType(addrType);

    if (addrType.equals("ZI01") || addrType.equals("ZD01") || addrType.equals("ZS02")) {
      addrCopy.setPoBox(null);
    }
    if (addrType.equals("ZP01") || addrType.equals("ZI01") || addrType.equals("ZS02")) {
      addrCopy.setCustPhone(null);
    }

    entityManager.persist(addrCopy);
    entityManager.flush();
  }

  private int getCountAddrSaved(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.COUNT.ALLTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    int count = query.getSingleResult(Integer.class);
    return count;
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

  private boolean checkIfNewCmrRecords(List<FindCMRRecordModel> items) {

    int rdcRecordsCount = items.size();
    int legacyRecordsCount = legacyObjects.getAddresses().size();

    // for new rdc records it is all db2 addr records except mailing and billing
    // legacy has 5 records min for new cmr for greece
    if (rdcRecordsCount < 3 || legacyRecordsCount < 5) {
      return false;
    }
    return true;
  }

  boolean isOldRecordsGR = false;

  private List<FindCMRRecordModel> handleOldCmrRecordsGR(EntityManager entityManager, FindCMRRecordModel record, String cmrIssueCd, int recCount)
      throws Exception {
    List<FindCMRRecordModel> records = new ArrayList<FindCMRRecordModel>();
    isOldRecordsGR = true;
    int currentSeq = recCount == 0 ? 2 : recCount;
    int rdcRecSeq = Integer.parseInt(record.getCmrAddrSeq());
    for (CmrtAddr cmrtAddr : legacyObjects.getAddresses()) {
      List<String> addrUseList = this.legacyObjects.getUsesBySequenceNo(cmrtAddr.getId().getAddrNo());

      for (String addrUse : addrUseList) {
        String addrType = getAddressTypeByUseGR(addrUse);

        if (!StringUtils.isEmpty(addrType)) {
          FindCMRRecordModel newRecord = new FindCMRRecordModel();
          newRecord = cloneAddress(record, addrType);

          if (currentSeq == rdcRecSeq) {
            currentSeq++;
          }

          if (!(record.getCmrAddrTypeCode().equals(addrType))) {
            newRecord.setCmrAddrSeq(String.format("%05d", currentSeq++));
            newRecord.setCmrSapNumber(null);
          }

          newRecord.setCmrName1Plain(cmrtAddr.getAddrLine1());
          newRecord.setCmrName2Plain(cmrtAddr.getAddrLine2());
          if (!cmrtAddr.getAddrLine3().startsWith("ATT") && !cmrtAddr.getAddrLine3().startsWith("PO BOX")) {
            newRecord.setCmrStreetAddressCont(cmrtAddr.getAddrLine3());
          } else if (cmrtAddr.getAddrLine3().startsWith("ATT")) {
            newRecord.setCmrName4(cmrtAddr.getAddrLine3());
          } else if (cmrtAddr.getAddrLine3().startsWith("PO BOX")) {
            newRecord.setCmrPOBox(cmrtAddr.getAddrLine3());
          }

          if (cmrtAddr.getAddrLine4().startsWith("PO BOX")) {
            newRecord.setCmrPOBox(cmrtAddr.getAddrLine4());
          } else {
            newRecord.setCmrStreetAddress(cmrtAddr.getAddrLine4());
          }

          if (!StringUtils.isEmpty(cmrtAddr.getAddrLine5())) {
            String[] line5 = cmrtAddr.getAddrLine5().split(" ");
            String postalCode = "";
            String city = "";
            for (int i = 0; i < line5.length; i++) {
              if (i < 2 && StringUtils.isNumeric(line5[i])) {
                postalCode += line5[i];
              } else {
                city += " " + line5[i];
              }
            }
            newRecord.setCmrCity(city.trim());
            newRecord.setCmrPostalCode(postalCode);
          }

          CmrtCustExt custExt = getCustExt(entityManager, cmrIssueCd, record.getCmrNum());
          records.add(newRecord);
        }
      }
    }
    return records;
  }

  private FindCMRRecordModel handleNewCmrRecordsGR(FindCMRRecordModel record) throws Exception {
    FindCMRRecordModel addr = null;
    String seqNo = record.getCmrAddrSeq();
    if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
      String addrType = record.getCmrAddrTypeCode();
      if (!StringUtils.isEmpty(addrType)) {
        addr = cloneAddress(record, addrType);
      }
    }
    return addr;
  }

  private String getAddressTypeByUseGR(String addressUse) {
    switch (addressUse) {
    case "2":
      return "ZP01";
    case "3":
      return "ZS01";
    case "4":
      return "ZD01";
    case "5":
      return "ZI01";
    }
    return null;
  }

  private String getShippingPhoneFromLegacy(FindCMRRecordModel address) {
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      String seqNo = (address.getCmrAddrSeq().length() != 5) ? StringUtils.leftPad(address.getCmrAddrSeq(), 5, '0') : address.getCmrAddrSeq();
      if ("Y".equals(cmrtAddr.getIsAddrUseShipping()) && seqNo.equals(cmrtAddr.getId().getAddrNo())) {
        return cmrtAddr.getAddrPhone();
      }
    }
    return null;
  }

  private FindCMRRecordModel mapLocalLanguageTranslationOfSoldTo(EntityManager entityManager, FindCMRRecordModel record, String cmrIssueCd)
      throws Exception {
    // Map local language translation of sold to value -- for greece recommit
    CmrtAddr db2LocalTransAddr = LegacyDirectUtil.getLegacyBillingAddress(entityManager, record.getCmrNum(), cmrIssueCd);
    CmrtCustExt custExt = getCustExt(entityManager, cmrIssueCd, record.getCmrNum());
    FindCMRRecordModel localTransAddr = new FindCMRRecordModel();
    PropertyUtils.copyProperties(localTransAddr, record);
    localTransAddr.setCmrAddrSeq(db2LocalTransAddr.getId().getAddrNo());
    localTransAddr.setCmrAddrTypeCode("ZP01");

    // If not in sadr look in DB2
    if (record.getCmrCountryLanded().equals("GR")) {
      if (!StringUtils.isBlank(record.getCmrIntlName1())) {
        localTransAddr.setCmrName1Plain(record.getCmrIntlName1());
      } else {
        localTransAddr.setCmrName1Plain(db2LocalTransAddr.getAddrLine1());
      }
      if (!StringUtils.isBlank(record.getCmrIntlName2())) {
        localTransAddr.setCmrName2Plain(record.getCmrIntlName2());
      } else {
        localTransAddr.setCmrName2Plain(db2LocalTransAddr.getAddrLine2());
      }

      if (!StringUtils.isBlank(record.getCmrIntlName4())) {
        localTransAddr.setCmrName4(record.getCmrIntlName4());
      } else if (!StringUtils.isBlank(record.getCmrName4())) {
        if (db2LocalTransAddr.getAddrLine3().startsWith("ATT")) {
          localTransAddr.setCmrName4(db2LocalTransAddr.getAddrLine3().replaceFirst("ATT ", ""));
        }
      }

      if (!StringUtils.isBlank(record.getCmrIntlAddress())) {
        localTransAddr.setCmrStreetAddress(record.getCmrIntlAddress());
      } else if (!StringUtils.isBlank(record.getCmrStreetAddress())) {
        localTransAddr.setCmrStreetAddress(db2LocalTransAddr.getStreet());
      }

      if (!StringUtils.isBlank(record.getCmrOtherIntlAddress())) {
        localTransAddr.setCmrStreetAddressCont(record.getCmrOtherIntlAddress());
      } else if (!StringUtils.isBlank(record.getCmrStreetAddressCont())) {
        if (!db2LocalTransAddr.getAddrLine3().startsWith("ATT") && !db2LocalTransAddr.getAddrLine3().startsWith("PO BOX")) {
          localTransAddr.setCmrStreetAddressCont(db2LocalTransAddr.getAddrLine3());
        }

      }

      if (!StringUtils.isBlank(record.getCmrIntlCity1())) {
        localTransAddr.setCmrCity(record.getCmrIntlCity1());
      } else {
        localTransAddr.setCmrCity(db2LocalTransAddr.getCity());
      }

      if (!StringUtils.isBlank(record.getCmrState())) {
        localTransAddr.setCmrState(db2LocalTransAddr.getItCompanyProvCd());
      }
      if (!StringUtils.isBlank(record.getCmrPostalCode())) {
        localTransAddr.setCmrPostalCode(db2LocalTransAddr.getZipCode());
      }

      String poBox = db2LocalTransAddr.getPoBox();
      if (poBox.contains("PO BOX")) {
        poBox = poBox.substring(6).trim();
      } else if (poBox.contains("APTO")) {
        poBox = poBox.substring(5).trim();
      }
      if (!StringUtils.isBlank(record.getCmrPOBox())) {
        localTransAddr.setCmrPOBox(poBox);
      }
    }

    return localTransAddr;
  }

  @Override
  public boolean checkCopyToAdditionalAddress(EntityManager entityManager, Addr copyAddr, String cmrIssuingCntry) throws Exception {

    if (copyAddr != null && copyAddr.getId().getAddrSeq().compareTo("00006") >= 0) {
      return true;
    }
    return false;
  }

}
