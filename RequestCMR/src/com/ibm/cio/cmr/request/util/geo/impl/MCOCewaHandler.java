/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CmrtAddr;
import com.ibm.cio.cmr.request.entity.CmrtCust;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * Handler for MCO Central, East, West Africa
 * 
 * @author Eduard Bernardo
 * 
 */
public class MCOCewaHandler extends MCOHandler {

  protected static final Logger LOG = Logger.getLogger(MCOCewaHandler.class);

  private static final List<String> CEWA_COUNTRY_LIST = Arrays.asList(SystemLocation.ANGOLA, SystemLocation.BOTSWANA, SystemLocation.BURUNDI,
      SystemLocation.CAPE_VERDE_ISLAND, SystemLocation.ETHIOPIA, SystemLocation.GHANA, SystemLocation.ERITREA, SystemLocation.KENYA,
      SystemLocation.MALAWI_CAF, SystemLocation.LIBERIA, SystemLocation.MOZAMBIQUE, SystemLocation.NIGERIA, SystemLocation.ZIMBABWE,
      SystemLocation.SAO_TOME_ISLANDS, SystemLocation.RWANDA, SystemLocation.SIERRA_LEONE, SystemLocation.SOMALIA, SystemLocation.SOUTH_SUDAN,
      SystemLocation.TANZANIA, SystemLocation.UGANDA, SystemLocation.ZAMBIA);

  protected static final String[] LD_MASS_UPDATE_SHEET_NAMES = { "Mailing Address", "Billing Address", "Installing Address", "Shipping Address",
      "EPL Address", "Data" };

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      if (legacyObjects != null && legacyObjects.getCustomer() != null) {
        CmrtCust legacyCust = legacyObjects.getCustomer();

        if (legacyCust.getCustType() != null)
          data.setCrosSubTyp(legacyCust.getCustType());

        if (legacyCust.getTaxCd() != null)
          data.setSpecialTaxCd(legacyCust.getTaxCd());

        if (legacyCust.getEnterpriseNo() != null)
          data.setEnterprise(legacyCust.getEnterpriseNo());

      }

      data.setDunsNo(mainRecord.getCmrDuns());

      String zs01sapNo = getKunnrSapr3Kna1(data.getCmrNo(), data.getCmrIssuingCntry());
      data.setIbmDeptCostCenter(getDepartment(zs01sapNo));

      String modeOfPayment = legacyObjects.getCustomer().getModeOfPayment();
      if (StringUtils.isNotBlank(modeOfPayment) && ("R".equals(modeOfPayment) || "S".equals(modeOfPayment) || "T".equals(modeOfPayment))) {
        data.setCommercialFinanced(modeOfPayment);
        data.setCreditCd("N");
      } else if (StringUtils.isNotBlank(modeOfPayment) && "5".equals(modeOfPayment)) {
        data.setCreditCd("Y");
        data.setCommercialFinanced("");
      } else {
        data.setCreditCd("");
        data.setCommercialFinanced("");
      }
    }

  }

  private String getKunnrSapr3Kna1(String cmrNo, String cntry) throws Exception {
    String kunnr = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.ZS01.KUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":ZZKV_CUSNO", "'" + cmrNo + "'");
    sql = StringUtils.replace(sql, ":KATR6", "'" + cntry + "'");

    String dbId = QueryClient.RDC_APP_ID;

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

  private String getDepartment(String kunnr) throws Exception {
    String department = "";

    String url = SystemConfiguration.getValue("CMR_SERVICES_URL");
    String mandt = SystemConfiguration.getValue("MANDT");
    String sql = ExternalizedQuery.getSql("GET.DEPT.KNA1.BYKUNNR");
    sql = StringUtils.replace(sql, ":MANDT", "'" + mandt + "'");
    sql = StringUtils.replace(sql, ":KUNNR", "'" + kunnr + "'");
    String dbId = QueryClient.RDC_APP_ID;

    QueryRequest query = new QueryRequest();
    query.setSql(sql);
    query.addField("KUNNR");
    query.addField("ZZKV_DEPT");

    LOG.debug("Getting existing ZZKV_DEPT value from RDc DB..");

    QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);
    QueryResponse response = client.executeAndWrap(dbId, query, QueryResponse.class);

    if (response.isSuccess() && response.getRecords() != null && response.getRecords().size() != 0) {
      List<Map<String, Object>> records = response.getRecords();
      Map<String, Object> record = records.get(0);
      department = record.get("ZZKV_DEPT") != null ? record.get("ZZKV_DEPT").toString() : "";
      LOG.debug("***RETURNING ZZKV_DEPT > " + department + " WHERE KUNNR IS > " + kunnr);
    }
    return department;
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);

    String country = currentRecord.getCmrIssuedBy();
    String processingType = PageManager.getProcessingType(country, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
          && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
        String seq = address.getId().getAddrSeq();
        seq = StringUtils.leftPad(seq, 5, '0');
        address.getId().setAddrSeq(seq);
      }
      address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
      if (!("ZS01".equals(address.getId().getAddrType()) || "ZD01".equals(address.getId().getAddrType()))) {
        address.setCustPhone("");
      }
      if ("ZD01".equals(address.getId().getAddrType())) {
        String phone = getShippingPhoneFromLegacy(currentRecord);
        address.setCustPhone(phone != null ? phone : "");
      }
    }
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZS01", "ZP01", "ZI01", "ZD01", "ZS02");
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZD01", "ZI01");
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setRepTeamMemberNo("DUMMY1");
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    super.doBeforeAddrSave(entityManager, addr, cmrIssuingCntry);

    if (addr != null) {
      if (!("ZS01".equals(addr.getId().getAddrType()) || "ZD01".equals(addr.getId().getAddrType()))) {
        addr.setCustPhone("");
      }

      if (!("ZS01".equals(addr.getId().getAddrType()) || "ZP01".equals(addr.getId().getAddrType()))) {
        addr.setPoBox("");
      }
    }
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "MODE_OF_PAYMENT", "VAT", "ISIC_CD", "EMBARGO_CD", "MAILING_COND", "ABBREV_NM",
        "LOCN_NO", "CLIENT_TIER", "ENGINEERING_BO", "ENTERPRISE", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "COLLECTION_CD", "SPECIAL_TAX_CD",
        "SEARCH_TERM", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "IBM_DEPT_COST_CENTER", "COMMERCIAL_FINANCED", "CREDIT_CD", "TAX_CD1"));
    return fields;
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(
        Arrays.asList("CUST_NM1", "CUST_NM2", "LAND_CNTRY", "CUST_NM4", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "CUST_PHONE", "POST_CD", "PO_BOX"));
    return fields;
  }

  @Override
  public void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, FindCMRRecordModel mainRecord,
      List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
        mapCreateReqAddrLD(mainRecord, converted);
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType())) {
        mapUpdateReqAddrLD(source, converted);
      }
    } else {
      super.handleSOFConvertFrom(entityManager, source, reqEntry, mainRecord, converted, searchModel);
    }
  }

  private void mapCreateReqAddrLD(FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted) {
    // only add zs01 equivalent for create by model
    FindCMRRecordModel record = mainRecord;

    if (!StringUtils.isEmpty(record.getCmrName3())) {
      record.setCmrName4(record.getCmrName3());
    }

    if (!StringUtils.isEmpty(record.getCmrName4())) {
      record.setCmrStreetAddressCont(record.getCmrName4());
    }

    if (!StringUtils.isBlank(record.getCmrPOBox())) {
      record.setCmrPOBox(record.getCmrPOBox());
    }
    if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
      record.setCmrAddrSeq("00001");
    } else {
      record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
    }

    record.setCmrName2Plain(record.getCmrName2Plain());

    if (StringUtils.isEmpty(record.getCmrCustPhone())) {
      record.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
    }
    converted.add(record);
  }

  private void mapUpdateReqAddrLD(FindCMRResultModel source, List<FindCMRRecordModel> converted) throws Exception {
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
          for (String sofUse : sofUses) {
            addrType = getAddressTypeByUse(sofUse);
            if (!StringUtils.isEmpty(addrType)) {
              addr = cloneAddress(record, addrType);
              LOG.trace("Adding address type " + addrType + " for sequence " + seqNo);

              // name3 in rdc = Address Con't on SOF
              addr.setCmrStreetAddressCont(record.getCmrName4());
              addr.setCmrName4(record.getCmrName3());
              addr.setCmrName2Plain(record.getCmrName2Plain());
              addr.setCmrSitePartyID(record.getCmrSitePartyID());

              if (!StringUtils.isBlank(record.getCmrPOBox())) {
                addr.setCmrPOBox(record.getCmrPOBox());
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
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    if (CEWA_COUNTRY_LIST.contains(issuingCountry)) {
      return true;
    }
    return false;
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    if (CmrConstants.REQ_TYPE_UPDATE.equalsIgnoreCase(admin.getReqType()) || CmrConstants.REQ_TYPE_CREATE.equalsIgnoreCase(admin.getReqType())) {
      if (!StringUtils.isEmpty(data.getSalesBusOffCd())) {
        data.setSearchTerm(data.getSalesBusOffCd());
      }
    }
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;
    super.addSummaryUpdatedFields(service, type, cmrCountry, newData, oldData, results);

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCrosSubTyp(), newData.getCrosSubTyp())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Type of Customer", "Type of Customer"));
      update.setNewData(service.getCodeAndDescription(newData.getCrosSubTyp(), "Type of Customer", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCrosSubTyp(), "Type of Customer", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SBO/ Search Term (SORTL):", "SBO/ Search Term (SORTL):"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesBusOffCd(), "SBO/ Search Term (SORTL):", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesBusOffCd(), "SBO/ Search Term (SORTL):", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSpecialTaxCd(), newData.getSpecialTaxCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Tax Code", "Tax Code"));
      update.setNewData(service.getCodeAndDescription(newData.getSpecialTaxCd(), "Tax Code", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSpecialTaxCd(), "Tax Code", cmrCountry));
      results.add(update);
    }

    if (SystemLocation.TANZANIA.equals(oldData.getCmrIssuingCntry())) {
      if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getTaxCd1(), newData.getTaxCd1())) {
        update = new UpdatedDataModel();
        update.setDataField(PageManager.getLabel(cmrCountry, "TIN number", "TIN number"));
        update.setNewData(service.getCodeAndDescription(newData.getTaxCd1(), "TIN number", cmrCountry));
        update.setOldData(service.getCodeAndDescription(oldData.getTaxCd1(), "TIN number", cmrCountry));
        results.add(update);
      }
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Collection Code", "Collection Code"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "Collection Code", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "Collection Code", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Sales Rep", "Sales Rep"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "Sales Rep", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepTeamMemberNo(), "Sales Rep", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getCommercialFinanced(), newData.getCommercialFinanced())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CoF (Commercial Financed)", "CoF (Commercial Financed)"));
      update.setNewData(service.getCodeAndDescription(newData.getCommercialFinanced(), "CoF (Commercial Financed)", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCommercialFinanced(), "CoF (Commercial Financed)", cmrCountry));
      results.add(update);
    }

  }

  @Override
  public void validateMassUpdateTemplateDupFills(List<TemplateValidation> validations, XSSFWorkbook book, int maxRows, String country) {
    XSSFCell currCell = null;
    String defaultLanded = LANDED_CNTRY_MAP.get(country);
    for (String name : LD_MASS_UPDATE_SHEET_NAMES) {
      XSSFSheet sheet = book.getSheet(name);
      if (sheet != null) {
        for (Row row : sheet) {
          if (row.getRowNum() > 0 && row.getRowNum() < 2002) {
            String cmrNo = "";
            String street = ""; // 4
            String landedcountry = "";// 8
            String embargo = ""; // 9
            String cof = ""; // 10
            String cod = ""; // 11
            String vat = ""; // 12
            String deptNo = ""; // 14
            String city = "";
            String postalcd = "";
            String phoneNo = "";
            String stcont = ""; // 5
            String addnameinfo = ""; // 9
            String poBox = "";// 10
            String tin = "";// 15

            if ("Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              embargo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(10);
              cof = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(11);
              cod = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(12);
              vat = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(14);
              deptNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(13);
              phoneNo = validateColValFromCell(currCell);
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                phoneNo = df.formatCellValue(row.getCell(13));
              }
              currCell = (XSSFCell) row.getCell(15);
              tin = validateColValFromCell(currCell);
            }

            if (!"Data".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(0);
              cmrNo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(5);
              stcont = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(6);
              city = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(7);
              postalcd = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(8);
              landedcountry = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(9);
              addnameinfo = validateColValFromCell(currCell);
              currCell = (XSSFCell) row.getCell(10);
              poBox = validateColValFromCell(currCell);
            }

            if ("Ship To Address".equalsIgnoreCase(sheet.getSheetName())) {
              currCell = (XSSFCell) row.getCell(10);
              phoneNo = validateColValFromCell(currCell);
              if (currCell != null) {
                DataFormatter df = new DataFormatter();
                phoneNo = df.formatCellValue(row.getCell(10));
              }
            }

            if (row.getRowNum() == 2001) {
              continue;
            }

            TemplateValidation error = new TemplateValidation(name);

            if (landedcountry.length() == 0 && !"Data".equalsIgnoreCase(sheet.getSheetName()) && cmrNo.length() != 0) {
              LOG.trace("Landed country is required to be filled.");
              error.addError(row.getRowNum(), "", "Landed country is required to be filled.");
              validations.add(error);
            } else if (landedcountry.length() > 0) {
              if (!defaultLanded.equals(landedcountry) && !"Data".equalsIgnoreCase(sheet.getSheetName()) && cmrNo.length() != 0) {
                LOG.debug("isCrossborder");
                if (addnameinfo.length() != 0 && stcont.length() != 0 && poBox.length() != 0) {
                  LOG.trace("Additional name or address information and Street Cont/POBox cannot be filled at the same time.");
                  error.addError(row.getRowNum(), "",
                      "Additional name or address information and Street Cont/POBox cannot be filled at the same time.");
                  validations.add(error);
                } else if (addnameinfo.length() != 0 && (stcont.length() != 0 || poBox.length() != 0)) {
                  LOG.trace("Additional name or address information and Street Cont/POBox cannot be filled at the same time.");
                  error.addError(row.getRowNum(), "",
                      "Additional name or address information and Street Cont/POBox cannot be filled at the same time.");
                  validations.add(error);
                } else if (stcont.length() != 0 && poBox.length() != 0 && addnameinfo.length() == 0) {
                  if (stcont.length() + poBox.length() > 23) {
                    LOG.trace("Total computed length of Street Cont and PO Box should not exceed 21 characters.");
                    error.addError(row.getRowNum(), "", "Total computed length of Street Cont and PO Box should not exceed 21 characters.");
                    validations.add(error);
                  }
                }
              } else {
                LOG.debug("isLocal");
                if (stcont.length() != 0 && poBox.length() != 0) {
                  if (stcont.length() + poBox.length() > 23) {
                    LOG.trace("Total computed length of Street Cont and PO Box should not exceed 21 characters.");
                    error.addError(row.getRowNum(), "", "Total computed length of Street Cont and PO Box should not exceed 21 characters.");
                    validations.add(error);
                  }
                }
              }
            }

            if (cod.length() > 0 && cof.length() > 0) {
              LOG.trace("Note that COF and COD flag cannot be filled at same time.");
              error.addError(row.getRowNum(), "COF/COD",
                  "Note that COF and COD flag cannot be filled at same time. Please fix and upload the template again.");
              validations.add(error);
            }
            if (city.length() == 0 && postalcd.length() != 0) {
              LOG.trace("Note that city should be filled if postal is filled");
              error.addError(row.getRowNum(), "City/Postal Code",
                  "Note that city should be filled if postal is filled. Please fix and upload the template again.");
              validations.add(error);
            }
            if (!StringUtils.isBlank(cmrNo) && !cmrNo.startsWith("99") && !StringUtils.isBlank(deptNo)) {
              LOG.trace("CMR No. should start with 99 if internal department no. is filled.");
              error.addError(row.getRowNum(), "Internal Department No.", "CMR No. should start with 99 if internal department no. is filled.");
              validations.add(error);
            }
            if ("Ship To Address".equalsIgnoreCase(sheet.getSheetName()) || "Data".equalsIgnoreCase(sheet.getSheetName())) {
              if (phoneNo.contains("+")) {
                LOG.trace("Please input value in numeric format. Please fix and upload the template again.");
                error.addError(row.getRowNum(), "Phone No.", "Please input value in numeric format. Please fix and upload the template again.");
                validations.add(error);
              }
            }
            if (tin.length() != 0) {
              boolean isMatch = Pattern.matches("\\d{3}[-]\\d{3}[-]\\d{3}$", tin);
              if (!isMatch) {
                LOG.trace("Invalid format for TIN Number.");
                error.addError(row.getRowNum(), "TIN Number", "Invalid format for TIN Number. Format should be NNN-NNN-NNN.");
                validations.add(error);
              }
            }
          }
        }
      }
    }
  }

  private String getShippingPhoneFromLegacy(FindCMRRecordModel address) {
    List<CmrtAddr> cmrtAddrs = this.legacyObjects.getAddresses();
    for (CmrtAddr cmrtAddr : cmrtAddrs) {
      if ("Y".equals(cmrtAddr.getIsAddrUseShipping()) && address.getCmrAddrSeq().equals(cmrtAddr.getId().getAddrNo())) {
        return cmrtAddr.getAddrPhone();
      }
    }
    return null;
  }

}
