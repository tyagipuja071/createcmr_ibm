/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectObjectContainer;
import com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil;
import com.ibm.cio.cmr.request.util.sof.GenericSOFMessageParser;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;

/**
 * Handler for countries using the SOF interface
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class BaseSOFHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(BaseSOFHandler.class);
  protected Map<String, String> currentImportValues = null;
  protected List<String> shippingSequences = null;
  protected List<String> countryCSequences = null;
  protected List<String> installingSequences = null;
  protected boolean serviceError = false;
  protected List<FindCMRRecordModel> rdcShippingRecords = new ArrayList<FindCMRRecordModel>();
  protected LegacyDirectObjectContainer legacyObjects;
  protected static final String[] LD_MASS_UPDATE_SHEET_NAMES = { "Billing Address", "Mailing Address", "Installing Address",
      "Shipping Address (Update)", "EPL Address" };

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    FindCMRRecordModel mainRecord = source.getItems() != null && !source.getItems().isEmpty() ? source.getItems().get(0) : null;
    if (mainRecord != null) {
      retrieveSOFValues(mainRecord);

      List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
      handleSOFConvertFrom(entityManager, source, reqEntry, mainRecord, converted, searchModel);

      if (!CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
        String cmrCountry = mainRecord.getCmrIssuedBy();
        // have to put ISRAEL specific logic here, no other way
        converted = assignSOFSequencesAndCleanRecords(converted, cmrCountry);
      }

      Collections.sort(converted);
      source.setItems(converted);

    } else {
      // no records retrieved, skip all processing
      return;
    }
  }

  /**
   * Gets the exact address sequence from SOF instead of using the RDc sequence.
   * 
   * @param records
   * @param cmrIssuingCnry
   */
  protected List<FindCMRRecordModel> assignSOFSequencesAndCleanRecords(List<FindCMRRecordModel> records, String cmrIssuingCntry) {
    String processingType = PageManager.getProcessingType(cmrIssuingCntry, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      LOG.debug("Skipping SOF sequence assignment for Legacy Direct.");
      return records;
    }
    // For AT address change.
    if ("MA".equals(processingType)) {
      return records;
    }

    List<FindCMRRecordModel> finalList = new ArrayList<FindCMRRecordModel>();

    handleSOFSequenceImport(records, cmrIssuingCntry);

    List<String> keyList = new ArrayList<String>();
    String key = null;
    for (FindCMRRecordModel record : records) {
      key = record.getCmrAddrTypeCode() + "|" + record.getCmrAddrSeq();
      if (!keyList.contains(key)) {
        finalList.add(record);
        keyList.add(key);
      } else {
        LOG.warn("Not adding " + record.getCmrAddrTypeCode() + " | " + record.getCmrAddrSeq() + " to the request");
      }
    }
    return finalList;
  }

  /**
   * Retrieves SOF values
   * 
   * @param entityManager
   * @param mainRecord
   * @throws Exception
   */
  protected void retrieveSOFValues(FindCMRRecordModel mainRecord) throws Exception {
    this.currentImportValues = new HashMap<String, String>();
    String cmrIssuingCntry = mainRecord.getCmrIssuedBy();
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }
    String cmrNo = mainRecord.getCmrNum();

    boolean prospectCmrCLChosen = mainRecord != null
        && (CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock()) || "CL".equals(mainRecord.getCmrOrderBlock()));
    if (!prospectCmrCLChosen) {
      String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), "U");
      if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
        retrieveSOFValuesFromLegacyDB(null, mainRecord);
      } else if (CmrConstants.PROCESSING_TYPE_MQ.equals(processingType)) {
        // old query service
        retrieveSOFValuesViaQueryService(cmrIssuingCntry, cmrNo);
      }
    }

  }

  /**
   * Retrieves SOF values from the Query Service
   * 
   * @param cmrIssuingCntry
   * @param cmrNo
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws CmrException
   */
  private void retrieveSOFValuesViaQueryService(String cmrIssuingCntry, String cmrNo) throws NoSuchMethodException, SecurityException,
      InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CmrException {
    LOG.info("Retrieving Legacy values for CMR No " + cmrNo + " from SOF (" + cmrIssuingCntry + ")");
    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry(cmrIssuingCntry);
    request.setCmrNo(cmrNo);

    SOFServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), SOFServiceClient.class);
    try {
      SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
      if (response.isSuccess()) {
        String xmlData = response.getData();

        GenericSOFMessageParser handler = new GenericSOFMessageParser();
        ByteArrayInputStream bis = new ByteArrayInputStream(xmlData.getBytes());
        try {
          SAXParserFactory factory = SAXParserFactory.newInstance();
          factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
          factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
          factory.newSAXParser().parse(new InputSource(bis), handler);
        } finally {
          bis.close();
        }

        this.currentImportValues = handler.getValues();
        this.shippingSequences = handler.getShippingSequences();
        this.countryCSequences = handler.getCountryCSequences();
        this.installingSequences = handler.getInstallingSequences();

        if (this.shippingSequences != null && !this.shippingSequences.isEmpty()) {
          LOG.trace("Shipping Sequences: " + this.shippingSequences.toArray());
        } else {
          LOG.trace("Shipping Sequences is empty");
        }
      } else {
        throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE);
      }
    } catch (Exception e) {
      LOG.warn("An error has occurred during retrieval of the values.", e);
      this.serviceError = true;
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, e);
    }
  }

  /**
   * Creates address records based on SOF query response. This is a method to
   * add the missing addresses which are defined in SOF but not in RDc, like
   * Mailing.
   * 
   * @param entityManager
   * @param cmrIssuingCntry
   * @param addressType
   * @param addressKey
   * @return
   */
  protected FindCMRRecordModel createAddress(EntityManager entityManager, String cmrIssuingCntry, String addressType, String addressKey,
      Map<String, FindCMRRecordModel> zi01Map) {
    if (!this.currentImportValues.containsKey(addressKey + "AddressNumber")) {
      return null;
    }
    LOG.debug("Adding " + addressKey + " address from SOF to request");
    FindCMRRecordModel address = new FindCMRRecordModel();
    address.setCmrAddrTypeCode(addressType);
    address.setCmrAddrSeq(this.currentImportValues.get(addressKey + "AddressNumber"));

    if ("EplMailing".equals(addressKey) && zi01Map.containsKey(address.getCmrAddrSeq()) && zi01Map.size() > 1) {
      FindCMRRecordModel epl = zi01Map.get(address.getCmrAddrSeq());
      LOG.debug("Switching address " + address.getCmrAddrSeq() + " to Epl");
      epl.setCmrAddrTypeCode("ZS02"); // switch ZI01 to EPL then do nothing
      return null;
    }
    address.setCmrName1Plain(this.currentImportValues.get(addressKey + "Name"));

    // run the specific handler import handler
    handleSOFAddressImport(entityManager, cmrIssuingCntry, address, addressKey);

    String transAddressNo = this.currentImportValues.get(addressKey + "TransAddressNumber");
    if (!StringUtils.isEmpty(transAddressNo) && StringUtils.isNumeric(transAddressNo) && transAddressNo.length() == 5) {
      address.setTransAddrNo(transAddressNo);
      LOG.trace("Translated Address No.: '" + address.getTransAddrNo() + "'");
    }

    address.setCmrIssuedBy(cmrIssuingCntry);

    return address;
  }

  /**
   * Assigns other address values to the address based on the country
   * requirements
   * 
   * @param entityManager
   * @param address
   * @param addressKey
   */
  protected abstract void handleSOFAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel address, String addressKey);

  /**
   * Handles the import of correct sequences to the addresses from SOF service,
   * should be implemented per country/group
   * 
   * @param records
   * @param cmrIssuingCntry
   */
  protected abstract void handleSOFSequenceImport(List<FindCMRRecordModel> records, String cmrIssuingCntry);

  /**
   * The convertFrom function implemented per region
   * 
   * @param entityManager
   * @param source
   * @param reqEntry
   * @param mainRecord
   */
  protected abstract void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry,
      FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception;

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    if (!this.currentImportValues.isEmpty()) {
      // legacy values
      data.setOrgNo(this.currentImportValues.get("OrganizationNo"));
      LOG.trace("Org: " + data.getOrgNo());
      data.setSourceCd(this.currentImportValues.get("SourceCode"));
      LOG.trace("Src: " + data.getSourceCd());
      data.setMrcCd(this.currentImportValues.get("MarketingResponseCode"));
      LOG.trace("Mrc: " + data.getMrcCd());
      data.setCurrencyCd(this.currentImportValues.get("CurrencyCode"));
      LOG.trace("Currency: " + data.getCurrencyCd());
      data.setSpecialTaxCd(this.currentImportValues.get("TaxCode"));
      LOG.trace("Tax: " + data.getSpecialTaxCd());
      data.setEngineeringBo(this.currentImportValues.get("DPCEBO"));
      LOG.trace("CEBO: " + data.getEngineeringBo());
      data.setAbbrevLocn(this.currentImportValues.get("AbbreviatedLocation"));
      LOG.trace("Abbrev Loc: " + data.getAbbrevLocn());
      data.setSalesBusOffCd(this.currentImportValues.get("SBO"));
      LOG.trace("SBO: " + data.getSalesBusOffCd());
      data.setInstallBranchOff(this.currentImportValues.get("IBO"));
      LOG.trace("IBO: " + data.getInstallBranchOff());
      data.setRepTeamMemberNo(this.currentImportValues.get("SR"));
      LOG.trace("Rep No: " + data.getRepTeamMemberNo());
      data.setCollectionCd(this.currentImportValues.get("CollectionCode"));
      LOG.trace("Collection Code: " + data.getCollectionCd());
      data.setSalesTeamCd(this.currentImportValues.get("SMR"));
      LOG.trace("SMR: " + data.getSalesTeamCd());

      // For Turkey the key name is wrong, so add this branch only for turkey
      if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
        if (this.currentImportValues.get("EconomicCd") != null) {
          data.setEconomicCd(this.currentImportValues.get("EconomicCd"));
          LOG.trace("Economic Code: " + data.getEconomicCd());
        } else {
          // temporary solution to handle the wrong tag
          data.setEconomicCd(this.currentImportValues.get("EcononicCode"));
          LOG.trace("Economic Code: " + data.getEconomicCd());
        }
      } else {
        if (this.currentImportValues.get("EconomicCode") != null) {
          data.setEconomicCd(this.currentImportValues.get("EconomicCode"));
          LOG.trace("Economic Code: " + data.getEconomicCd());
        } else {
          // temporary solution to handle the wrong tag
          data.setEconomicCd(this.currentImportValues.get("EcononicCode"));
          LOG.trace("Economic Code: " + data.getEconomicCd());
        }
      }

      // For Turkey ModeOfPayment set as CoF
      if (SystemLocation.TURKEY.equals(data.getCmrIssuingCntry())) {
        data.setCommercialFinanced(this.currentImportValues.get("ModeOfPayment"));
      } else {
        data.setModeOfPayment(this.currentImportValues.get("ModeOfPayment"));
      }

      LOG.trace("Mode of Payment: " + data.getModeOfPayment());

      data.setMailingCondition(this.currentImportValues.get("MailingCondition"));
      LOG.trace("Mailing Condition: " + data.getMailingCondition());

    }
  }

  @Override
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {

    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrCountry);

    UpdatedDataModel update = null;
    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getVatExempt(), newData.getVatExempt())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "VATExempt", "-"));
      update.setNewData(newData.getVatExempt());
      update.setOldData(oldData.getVatExempt());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevLocn(), newData.getAbbrevLocn())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevLocation", "-"));
      update.setNewData(newData.getAbbrevLocn());
      update.setOldData(oldData.getAbbrevLocn());
      results.add(update);
    }

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getSpecialTaxCd(), newData.getSpecialTaxCd())
        && !geoHandler.skipOnSummaryUpdate(cmrCountry, "SpecialTaxCd")) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SpecialTaxCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSpecialTaxCd(), "SpecialTaxCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSpecialTaxCd(), "SpecialTaxCd", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getSalesBusOffCd(), newData.getSalesBusOffCd())
        && !geoHandler.skipOnSummaryUpdate(cmrCountry, "SalesBusOff")) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalesBusOff", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getSalesBusOffCd(), "SalesBusOff", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getSalesBusOffCd(), "SalesBusOff", cmrCountry));
      results.add(update);
    }

    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())
        && !geoHandler.skipOnSummaryUpdate(cmrCountry, "SalRepNameNo")) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalRepNameNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      results.add(update);
    }

    if (!(SystemLocation.BELGIUM.equals(cmrCountry)) && !(SystemLocation.NETHERLANDS.equals(cmrCountry))
        && RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())
        && !geoHandler.skipOnSummaryUpdate(cmrCountry, "CollectionCd")) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
      results.add(update);
    }

    if (SystemLocation.ISRAEL.equals(cmrCountry) && RequestSummaryService.TYPE_IBM.equals(type)
        && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      results.add(update);
    }

  }

  protected boolean isPOBox(String data) {
    if (data == null) {
      return false;
    }
    return data.toUpperCase().contains("P O BOX") || data.toUpperCase().contains("PO BOX") || data.toUpperCase().contains("POBOX")
        || data.toUpperCase().contains("P.O. BOX") || data.toUpperCase().contains("P.O.BOX");
  }

  protected boolean isPhone(String data) {
    if (data == null) {
      return false;
    }
    return data.toUpperCase().contains("PHONE") || data.toUpperCase().contains("TEL") || data.toUpperCase().contains("PHN");
  }

  protected boolean isAttn(String data) {
    if (data == null) {
      return false;
    }
    return data.toUpperCase().contains("ATTN") || data.toUpperCase().startsWith("ATT.") || data.toUpperCase().startsWith("AT ")
        || data.toUpperCase().startsWith("ATT ") || data.toUpperCase().startsWith("ATT:") || data.toUpperCase().startsWith("TO ")
        || data.toUpperCase().startsWith("TO:") || data.toUpperCase().startsWith("C/O");
  }

  protected boolean isStreet(String data) {
    if (data == null) {
      return false;
    }
    return data.toUpperCase().contains("ST.") || data.toUpperCase().contains("STREET") || (" " + data + " ").toUpperCase().contains(" AVE ")
        || data.toUpperCase().contains("AVENUE") || (" " + data + " ").toUpperCase().contains("ROAD")
        || (" " + data + " ").toUpperCase().contains(" RD ") || (" " + data + " ").toUpperCase().contains(" RD,")
        || (" " + data + " ").toUpperCase().contains("BOULEVARD") || (" " + data + " ").toUpperCase().contains(" BLVD ");
  }

  protected void trimAddressToFit(FindCMRRecordModel address) {
    if (address.getCmrPOBox() != null && address.getCmrPOBox().length() > 20) {
      address.setCmrPOBox(address.getCmrPOBox().substring(0, 20));
    }
    if (address.getCmrPostalCode() != null && address.getCmrPostalCode().length() > 10) {
      address.setCmrPostalCode(address.getCmrPostalCode().substring(0, 10));
    }
    if (address.getCmrCustPhone() != null && address.getCmrCustPhone().length() > 16) {
      address.setCmrCustPhone(address.getCmrCustPhone().substring(0, 16));
    }
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  protected boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  @Override
  public int getName1Length() {
    return 30;
  }

  @Override
  public int getName2Length() {
    return 30;
  }

  @Override
  public boolean customerNamesOnAddress() {
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return true;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return false;
  }

  protected String getCurrentValue(String addressKey, String valueKey) {
    String val = this.currentImportValues.get(addressKey + valueKey);
    if (StringUtils.isEmpty(val)) {
      return val;
    }
    return "-/X".equalsIgnoreCase(val) ? "" : ("*".equalsIgnoreCase(val) ? "" : val);
  }

  /*
   * Legacy Direct Methods
   */

  /**
   * Retrieves SOF values from the legacy CMR database and wraps it into the old
   * {@link Map} format for easier adaptation of existing functions
   * 
   * @param entityManager
   * @param mainRecord
   * @throws Exception
   */
  protected void retrieveSOFValuesFromLegacyDB(EntityManager entityManager, FindCMRRecordModel mainRecord) throws Exception {
    this.currentImportValues = new HashMap<String, String>();
    String cmrIssuingCntry = mainRecord.getCmrIssuedBy();
    if (SystemLocation.IRELAND.equals(cmrIssuingCntry)) {
      // for Ireland, also query from 866
      cmrIssuingCntry = SystemLocation.UNITED_KINGDOM;
    }
    String cmrNo = mainRecord.getCmrNum();
    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry(cmrIssuingCntry);
    request.setCmrNo(cmrNo);

    LOG.info("Retrieving Legacy Database values for CMR No " + cmrNo + " from SOF (" + cmrIssuingCntry + ")");

    boolean getLinks = false;
    // switch here per country if links are needed, like France

    this.legacyObjects = null;
    EntityManager em = entityManager;
    if (em == null) {
      em = JpaManager.getEntityManager();
      try {
        this.legacyObjects = LegacyDirectUtil.getLegacyDBValues(em, cmrIssuingCntry, cmrNo, true, getLinks);
      } finally {
        em.clear();
        em.close();
      }
    }

    GenericSOFMessageParser handler = LegacyDirectUtil.convertLegacyDataToMaps(this.legacyObjects);

    adjustLegacyValues(mainRecord.getCmrIssuedBy(), this.legacyObjects);

    this.currentImportValues = handler.getValues();
    this.shippingSequences = handler.getShippingSequences();
    this.countryCSequences = handler.getCountryCSequences();
    this.installingSequences = handler.getInstallingSequences();
  }

  /**
   * Adjusts the values on the map that holds current values according to
   * country specifications
   * 
   * @param legacyObjects
   */
  protected void adjustLegacyValues(String cmrIssuingCntry, LegacyDirectObjectContainer legacyObjects) {
    // noop, override as needed
  }

  /**
   * Based on the current map containing the legacy data, gets the address type
   * in SOF given the following sequence no
   * 
   * @param seqNo
   * @return
   */
  protected String getSOFTypeBySequence(String seqNo) {
    for (String key : this.currentImportValues.keySet()) {
      if (key.contains("AddressNumber") && seqNo.equals(this.currentImportValues.get(key))) {
        String sofType = key.substring(0, key.indexOf("AddressNumber"));
        if (sofType != null && sofType.contains("_")) {
          sofType = sofType.substring(0, sofType.indexOf("_"));
        }
        LOG.debug("SOF Type: " + sofType + " for sequence no " + seqNo);
        return sofType;
      }
    }
    return null;
  }

  /**
   * Returns the address type for the handler based on the supplied address use
   * 
   * @param addressUse
   * @return
   */
  protected String getAddressTypeByUse(String addressUse) {
    return null;
  }

  @Override
  public List<String> getDataFieldsForUpdate(String cmrIssuingCntry) {
    return null;
  }

  public static String getRdcAufsd(String cmrNo, String cntry) {
    String rdcAufsd = "";
    String mandt = SystemConfiguration.getValue("MANDT");
    EntityManager entityManager = JpaManager.getEntityManager();
    String sql = ExternalizedQuery.getSql("WW.GET_RDC_AUFSD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("KATR6", cntry);
    query.setParameter("MANDT", mandt);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setParameter("KTOKD", "ZS01");
    query.setForReadOnly(true);
    String result = query.getSingleResult(String.class);
    if (result != null) {
      rdcAufsd = result;
    }
    return rdcAufsd;
  }
}
