package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.ScenarioExceptionsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.UpdatedAddr;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.window.RequestSummaryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;

public class UpdateSwitchElement extends ValidatingElement {

  private static final Logger log = Logger.getLogger(UpdateSwitchElement.class);

  public UpdateSwitchElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
    // TODO Auto-generated constructor stub
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    log.debug("Entering global Update Switch Element");
    // get request admin and data
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    long reqId = requestData.getAdmin().getId().getReqId();

    AutomationResult<ValidationOutput> output = buildResult(reqId);
    ValidationOutput validation = new ValidationOutput();

    try {
      ChangeLogListener.setManager(entityManager);
      if ("C".equals(admin.getReqType())) {
        validation.setSuccess(true);
        validation.setMessage("Execution skipped.");
        output.setDetails("Processing is skipped for Create requests.");
        log.debug("Processing is skipped for Create requests.");
      } else if ("U".equals(admin.getReqType())) {
        List<UpdatedDataModel> updatedCustList = getUpdateCustomerDataList(entityManager, reqId);
        List<UpdatedDataModel> updatedIbmList = getUpdateIBMDataList(entityManager, reqId);
        List<UpdatedNameAddrModel> updatedAddrList = getUpdateNameAddressList(entityManager, reqId);
        if ((updatedCustList != null && updatedCustList.size() > 0) || (updatedIbmList != null && updatedIbmList.size() > 0)) {
          validation.setSuccess(false);
          validation.setMessage("Review needed.");
          output.setDetails("IBM/Legacy codes values changed. Hence sending back to the processor for review.");
          output.setOnError(true);
          engineData.addRejectionComment("IBM/Legacy codes values changed.");
          log.debug("IBM/Legacy codes values changed. Hence sending back to the processor for review.");
        } else if (updatedAddrList != null && updatedAddrList.size() > 0) {
          ScenarioExceptionsUtil scenarioExceptions = getScenarioExceptions(entityManager, requestData, engineData);
          log.debug("Addr types for skip check are: " + scenarioExceptions.getAddressTypesForSkipChecks().toString());
          boolean onlySkipAddr = false;
          if (scenarioExceptions.getAddressTypesForSkipChecks() != null && scenarioExceptions.getAddressTypesForSkipChecks().size() > 0) {
            onlySkipAddr = true;
            for (UpdatedNameAddrModel updatedAddrModel : updatedAddrList) {
              if (!scenarioExceptions.getAddressTypesForSkipChecks().contains(updatedAddrModel.getAddrType())) {
                onlySkipAddr = false;
                break;
              }
            }
          }
          if (onlySkipAddr) {
            engineData.setSkipChecks();
            output.setDetails("Name/address changes made on non-critical addresses only. No processor review is needed.");
            log.debug("Name/address changes made on non-critical addresses only. No processor review is needed.");
          } else if (!"Y".equalsIgnoreCase(admin.getCompVerifiedIndc())) {
            engineData.addNegativeCheckStatus("UPDATE_CHECK_FAIL", "Name/address changes made on critical addresses.");
            output.setDetails("Name/address changes made on critical addresses. Hence sending back to the processor for review.");
            log.debug("Name/address changes made on critical addresses. Hence sending back to the processor for review.");
          } else {
            output.setDetails("Name/address changes made on critical addresses but company is verified. Hence no processor review needed.");
            log.debug("Name/address changes made on critical addresses but company is verified. Hence no processor review needed.");
          }
          validation.setSuccess(true);
          validation.setMessage("Execution done.");
        } else {
          validation.setSuccess(true);
          validation.setMessage("Execution done.");
          output.setDetails("No data/address changes made on request.");
          log.debug("No data/address changes made on request.");
        }
      }
    } finally {
      ChangeLogListener.clearManager();
    }
    output.setResults(validation.getMessage());
    output.setProcessOutput(validation);
    return output;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_UPDATE_SWITCH;
  }

  @Override
  public String getProcessDesc() {
    return "Global - Update Switch";
  }

  public List<UpdatedDataModel> getUpdateCustomerDataList(EntityManager entityManager, long reqId) throws CmrException {

    RequestSummaryService summaryService = new RequestSummaryService();
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = doProcess(entityManager, reqId);
    Data newData = summary.getData();
    List<UpdatedDataModel> updatedList = summaryService.getUpdatedData(newData, reqId, "C");

    return updatedList;
  }

  public List<UpdatedDataModel> getUpdateIBMDataList(EntityManager entityManager, long reqId) throws CmrException {
    RequestSummaryService summaryService = new RequestSummaryService();
    ParamContainer params = new ParamContainer();
    params.addParam("reqId", reqId);
    RequestSummaryModel summary = doProcess(entityManager, reqId);
    Data newData = summary.getData();
    List<UpdatedDataModel> updatedList = summaryService.getUpdatedData(newData, reqId, "IBM");

    return updatedList;
  }

  public List<UpdatedNameAddrModel> getUpdateNameAddressList(EntityManager entityManager, long reqId) throws CmrException {
    RequestSummaryService summaryService = new RequestSummaryService();
    List<UpdatedNameAddrModel> updatedList = getUpdatedNameAddr(entityManager, reqId);
    return updatedList;
  }

  public RequestSummaryModel doProcess(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("REQUEST.SUMMARY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<CompoundEntity> results = query.getCompundResults(Data.class, Data.REQUEST_SUMMARY_MAPPING);
    if (results != null && results.size() > 0) {
      CompoundEntity entity = results.get(0);
      Admin admin = entity.getEntity(Admin.class);
      Data data = entity.getEntity(Data.class);
      RequestSummaryModel summary = new RequestSummaryModel();
      summary.setAdmin(admin);
      summary.setData(data);
      summary.setCountry((String) entity.getValue("COUNTRY_DESC"));
      summary.setProcessingDesc((String) entity.getValue("PROCESSING_DESC"));
      summary.setIsicDesc((String) entity.getValue("ISIC_DESC"));

      if (LAHandler.isBRIssuingCountry(data.getCmrIssuingCntry())) {
        summary.setProxyLocnDesc(LAHandler.getProxiLocnDesc(entityManager, data.getCmrIssuingCntry(), data.getProxiLocnNo()));
      }

      return summary;
    }
    return null;
  }

  /**
   * gets the list of Updated fields for the request's name/address data
   * 
   * @param reqId
   * @return
   * @throws CmrException
   */
  public List<UpdatedNameAddrModel> getUpdatedNameAddr(EntityManager entityManager, long reqId) throws CmrException {
    RequestSummaryService summaryService = new RequestSummaryService();
    List<UpdatedNameAddrModel> results = new ArrayList<UpdatedNameAddrModel>();

    String sql = ExternalizedQuery.getSql("SUMMARY.NAMEADDR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);

    List<UpdatedAddr> updatedRecords = query.getResults(UpdatedAddr.class);

    // add the removed addresses
    sql = ExternalizedQuery.getSql("SUMMARY.NAMEADDR.REMOVED");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);

    List<UpdatedAddr> removedRecords = query.getResults(UpdatedAddr.class);

    List<UpdatedAddr> records = new ArrayList<UpdatedAddr>();
    if (updatedRecords != null) {
      records.addAll(updatedRecords);
    }
    if (removedRecords != null) {
      records.addAll(removedRecords);
    }

    if (records != null && records.size() > 0) {
      Map<String, String> addressTypes = new HashMap<>();
      if (records.size() > 0) {
        addressTypes = getAddressTypes(records.get(0).getCmrCountry(), entityManager);
      }
      for (UpdatedAddr addr : records) {
        parseNameAddrDiff(addressTypes, addr, results, entityManager, summaryService);
      }
    }

    // add removed addresses
    sql = ExternalizedQuery.getSql("SUMMARY.NAMEADDR.REMOVED");
    query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);

    return results;
  }

  private Map<String, String> getAddressTypes(String cntry, EntityManager entityManager) {
    HashMap<String, String> map = new HashMap<>();
    String sql = ExternalizedQuery.getSql("SUMMARY.GET_ADDRESS_TYPES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cntry);
    List<Object[]> results = query.getResults();
    if (results != null) {
      for (Object[] result : results) {
        map.put((String) result[0], (String) result[1]);
      }
    }
    return map;
  }

  /**
   * Parses the difference between name/addresses
   * 
   * @param addr
   * @param results
   */
  private void parseNameAddrDiff(Map<String, String> addressTypes, UpdatedAddr addr, List<UpdatedNameAddrModel> results, EntityManager entityManager,
      RequestSummaryService summaryService) {

    UpdatedNameAddrModel update = null;
    String sapNumber = addr.getSapNo();
    String addrType = addr.getId().getAddrType();
    String cmrCountry = addr.getCmrCountry();
    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrCountry);

    // String addrTypeDesc = addressTypes.get(addrType);
    // if (StringUtils.isEmpty(addrTypeDesc)) {
    // addrTypeDesc = DropdownListController.getDescription("AddressType",
    // addrType, cmrCountry);
    // }
    //
    // if (geoHandler != null && geoHandler.customerNamesOnAddress()) {
    // addrTypeDesc += " (" + addr.getId().getAddrSeq() + ")";
    // }

    if ("X".equals(addr.getImportInd())) {
      update = new UpdatedNameAddrModel();
      update.setAddrType(addrType);
      update.setSapNumber("[removed]");
      update.setDataField("- Address Removed -");
      results.add(update);
    } else if (StringUtils.isEmpty(addr.getSapNo()) && !"Y".equals(addr.getImportInd())) {
      update = new UpdatedNameAddrModel();
      update.setAddrType(addrType);
      update.setSapNumber("[new]");
      update.setDataField("All fields");
      results.add(update);
    } else {
      if (!equals(addr.getSapNo(), addr.getSapNoOld())) {
        update = new UpdatedNameAddrModel();
        update.setAddrType(addrType);
        update.setSapNumber(addr.getSapNo());
        update.setDataField(PageManager.getLabel(cmrCountry, "SAPNumber", "-"));
        update.setNewData(addr.getSapNo());
        update.setOldData(addr.getSapNoOld());
        results.add(update);
      } else {
        if (geoHandler != null && geoHandler.customerNamesOnAddress()
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName1"))) {
          if (!equals(addr.getCustNm1(), addr.getCustNm1Old())) {
            update = new UpdatedNameAddrModel();
            update.setAddrType(addrType);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName1", "-"));
            update.setNewData(addr.getCustNm1());
            update.setOldData(addr.getCustNm1Old());
            results.add(update);
          }
          if (!equals(addr.getCustNm2(), addr.getCustNm2Old())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName2"))) {
            update = new UpdatedNameAddrModel();
            update.setAddrType(addrType);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName2", "-"));
            update.setNewData(addr.getCustNm2());
            update.setOldData(addr.getCustNm2Old());
            results.add(update);
          }
          if (!equals(addr.getCustNm3(), addr.getCustNm3Old())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName3"))) {
            update = new UpdatedNameAddrModel();
            update.setAddrType(addrType);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName3", "-"));
            update.setNewData(addr.getCustNm3());
            update.setOldData(addr.getCustNm3Old());
            results.add(update);
          }
          if (!equals(addr.getCustNm4(), addr.getCustNm4Old())
              && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustomerName4"))) {
            update = new UpdatedNameAddrModel();
            update.setAddrType(addrType);
            update.setSapNumber(sapNumber);
            update.setDataField(PageManager.getLabel(cmrCountry, "CustomerName4", "-"));
            update.setNewData(addr.getCustNm4());
            update.setOldData(addr.getCustNm4Old());
            results.add(update);
          }
          // if (!equals(addr.getCustNm3(), addr.getCustNm3Old())) {
          // update = new UpdatedNameAddrModel();
          // update.setAddrType(DropdownListController.getDescription("AddressType",
          // addrType, cmrCountry));
          // update.setSapNumber(sapNumber);
          // update.setDataField(PageManager.getLabel(cmrCountry,
          // "CustomerName1", "-"));
          // update.setDataField(update.getDataField().replace("1", "3"));
          // update.setNewData(addr.getCustNm3());
          // update.setOldData(addr.getCustNm3Old());
          // results.add(update);
          // }
          // if (!equals(addr.getCustNm4(), addr.getCustNm4Old())) {
          // update = new UpdatedNameAddrModel();
          // update.setAddrType(DropdownListController.getDescription("AddressType",
          // addrType, cmrCountry));
          // update.setSapNumber(sapNumber);
          // update.setDataField(PageManager.getLabel(cmrCountry,
          // "CustomerName1", "-"));
          // update.setDataField(update.getDataField().replace("1", "4"));
          // update.setNewData(addr.getCustNm4());
          // update.setOldData(addr.getCustNm4Old());
          // results.add(update);
          // }
        }
        if (!equals(addr.getAddrTxt(), addr.getAddrTxtOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "StreetAddress1"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "StreetAddress1", "-"));
          update.setNewData(addr.getAddrTxt());
          update.setOldData(addr.getAddrTxtOld());
          results.add(update);
        }
        if (!equals(addr.getCity1(), addr.getCity1Old()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "City1"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "City1", "-"));
          update.setNewData(addr.getCity1());
          update.setOldData(addr.getCity1Old());
          results.add(update);
        }
        if (!equals(addr.getCity2(), addr.getCity2Old()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "City2"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "City2", "-"));
          update.setNewData(addr.getCity2());
          update.setOldData(addr.getCity2Old());
          results.add(update);
        }
        if (!equals(addr.getStateProv(), addr.getStateProvOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "StateProv"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "StateProv", "-"));
          update.setNewData(addr.getStateProv());
          update.setOldData(addr.getStateProvOld());
          results.add(update);
        }
        if (!equals(addr.getPostCd(), addr.getPostCdOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "PostalCode"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "PostalCode", "-"));
          update.setNewData(addr.getPostCd());
          update.setOldData(addr.getPostCdOld());
          results.add(update);
        }
        if (!equals(addr.getLandCntry(), addr.getLandCntryOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "LandedCountry"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "LandedCountry", "-"));
          update.setNewData(addr.getLandCntry());
          update.setOldData(addr.getLandCntryOld());
          results.add(update);
        }
        if (!equals(addr.getCounty(), addr.getCountyOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "County"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "County", "-"));
          update.setNewData(addr.getCounty());
          update.setOldData(addr.getCountyOld());
          results.add(update);
        }
        if (!equals(addr.getBldg(), addr.getBldgOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Building"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Building", "-"));
          update.setNewData(addr.getBldg());
          update.setOldData(addr.getBldgOld());
          results.add(update);
        }
        if (!equals(addr.getFloor(), addr.getFloorOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Floor"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Floor", "-"));
          update.setNewData(addr.getFloor());
          update.setOldData(addr.getFloorOld());
          results.add(update);
        }
        if (!equals(addr.getOffice(), addr.getOfficeOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Office"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Office", "-"));
          update.setNewData(addr.getOffice());
          update.setOldData(addr.getOfficeOld());
          results.add(update);
        }
        if (!equals(addr.getDept(), addr.getDeptOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Department"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Department", "-"));
          update.setNewData(addr.getDept());
          update.setOldData(addr.getDeptOld());
          results.add(update);
        }
        if (!equals(addr.getPoBox(), addr.getPoBoxOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "POBox"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "POBox", "-"));
          update.setNewData(addr.getPoBox());
          update.setOldData(addr.getPoBoxOld());
          results.add(update);
        }
        if (!equals(addr.getPoBoxCity(), addr.getPoBoxCityOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "POBoxCity"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "POBoxCity", "-"));
          update.setNewData(addr.getPoBoxCity());
          update.setOldData(addr.getPoBoxCityOld());
          results.add(update);
        }
        if (!equals(addr.getPoBoxPostCd(), addr.getPoBoxPostCdOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "POBoxPostalCode"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "POBoxPostalCode", "-"));
          update.setNewData(addr.getPoBoxPostCd());
          update.setOldData(addr.getPoBoxPostCdOld());
          results.add(update);
        }
        if (!equals(addr.getCustFax(), addr.getCustFaxOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustFAX"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "CustFAX", "-"));
          update.setNewData(addr.getCustFax());
          update.setOldData(addr.getCustFaxOld());
          results.add(update);
        }
        if (!equals(addr.getCustLangCd(), addr.getCustLangCdOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustLangCd"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "CustLangCd", "-"));
          update.setNewData(addr.getCustLangCd());
          update.setOldData(addr.getCustLangCdOld());
          results.add(update);
        }
        if (!equals(addr.getCustPhone(), addr.getCustPhoneOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "CustPhone"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "CustPhone", "-"));
          update.setNewData(addr.getCustPhone());
          update.setOldData(addr.getCustPhoneOld());
          results.add(update);
        }
        if (!equals(addr.getTransportZone(), addr.getTransportZoneOld())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "TransportZone"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "TransportZone", "-"));
          update.setNewData(addr.getTransportZone());
          update.setOldData(addr.getTransportZoneOld());
          results.add(update);
        }
        if (!equals(addr.getDivn(), addr.getDivnOld()) && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "Division"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "Division", "-"));
          update.setNewData(addr.getDivn());
          update.setOldData(addr.getDivnOld());
          results.add(update);
        }
        if (!equals(addr.getAddrTxt2(), addr.getAddrTxt2Old())
            && (geoHandler == null || !geoHandler.skipOnSummaryUpdate(cmrCountry, "StreetAddress2"))) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "StreetAddress2", "-"));
          update.setNewData(addr.getAddrTxt2());
          update.setOldData(addr.getAddrTxt2Old());
          results.add(update);
        }
        /*
         * LA Defect 1299028: Request Summary Commented but will be uncommented
         * after 8/26 release
         */
        // addr tax code 1
        if (!equals(addr.getTaxCd1(), addr.getTaxCd1Old())) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax1", "-"));
          update.setNewData(addr.getTaxCd1());
          update.setOldData(addr.getTaxCd1Old());
          results.add(update);
        }
        // addr tax code 2
        if (!equals(addr.getTaxCd2(), addr.getTaxCd2Old())) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "LocalTax2", "-"));
          update.setNewData(addr.getTaxCd2());
          update.setOldData(addr.getTaxCd2Old());
          results.add(update);
        }
        // vat
        if (!equals(addr.getVat(), addr.getVatOld())) {
          update = new UpdatedNameAddrModel();
          update.setAddrType(addrType);
          update.setSapNumber(sapNumber);
          update.setDataField(PageManager.getLabel(cmrCountry, "VAT", "-"));
          update.setNewData(addr.getVat());
          update.setOldData(addr.getVatOld());
          results.add(update);
        }

        if (geoHandler != null) {
          geoHandler.addSummaryUpdatedFieldsForAddress(summaryService, cmrCountry, addrType, sapNumber, addr, results, entityManager);
        }
      }
    }
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  public boolean equals(String val1, String val2) {
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
}
