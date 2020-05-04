/**
 * 
 */
package com.ibm.cio.cmr.request.automation.impl.gbl;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.StandardProcessElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.EmptyOutput;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.auto.BaseV2RequestModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.requestentry.ImportCMRService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.BatchHttpRequest;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;

/**
 * Element that imports the current values of a CMR record to the request
 * 
 * @author JeffZAMORA
 *
 */
public class ImportCMRElement extends StandardProcessElement {

  private static final Logger LOG = Logger.getLogger(ImportCMRElement.class);

  /**
   * @param requestTypes
   * @param actionOnError
   * @param overrideData
   * @param stopOnError
   */
  public ImportCMRElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, false, stopOnError);

  }

  @Override
  public AutomationResult<EmptyOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    Admin admin = requestData.getAdmin();
    AutomationResult<EmptyOutput> result = buildResult(admin.getId().getReqId());
    result.setProcessOutput(new EmptyOutput());

    boolean importDone = hasImportedCMRs(entityManager, admin.getId().getReqId());
    if (importDone) {
      result.setDetails("A CMR has already been imported into the request. Import has been skipped.");
      result.setResults("Skipped");
      return result;
    }

    AppUser user = (AppUser) engineData.get(AutomationEngineData.APP_USER);
    Data data = requestData.getData();

    GEOHandler geoHandler = RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    BaseV2RequestModel originalV2Model = geoHandler != null ? geoHandler.recreateModelFromRequest(entityManager, requestData) : null;
    if (originalV2Model != null) {
      LOG.debug("V2 model created from current request with class: " + originalV2Model.getClass().getName());
    }

    FindCMRResultModel findCMRResults = SystemUtil.findCMRs(data.getCmrNo(), data.getCmrIssuingCntry(), 2000, null);
    if (findCMRResults == null || findCMRResults.getItems() == null || findCMRResults.getItems().size() == 0) {
      LOG.error("CMR No. " + data.getCmrNo() + " not found for Issuing Country " + data.getCmrIssuingCntry());
      result.setOnError(true);
      result.setDetails("CMR No. " + data.getCmrNo() + " not found for Issuing Country " + data.getCmrIssuingCntry());
      result.setResults("CMR Not Found");
      engineData.addRejectionComment("TYPR", "Wrong type of request.", "CMR " + data.getCmrNo() + " not found.", "");
      return result;
    }

    RequestEntryModel model = requestData.createModelFromRequest();
    BatchHttpRequest httpRequest = new BatchHttpRequest(user);
    httpRequest.extractParameters(model);
    ImportCMRModel importModel = new ImportCMRModel();
    importModel.setCmrIssuingCntry(data.getCmrIssuingCntry());
    importModel.setCmrNum(data.getCmrNo());
    importModel.setReqId(admin.getId().getReqId());
    importModel.setSystem("cmr");

    ParamContainer params = new ParamContainer();
    params.addParam("reqId", admin.getId().getReqId());
    params.addParam("system", "cmr");
    params.addParam("model", model);
    params.addParam("searchModel", importModel);
    params.addParam("skipAddress", false);
    params.addParam("results", findCMRResults != null ? findCMRResults : new FindCMRResultModel());

    ImportCMRService importCmr = new ImportCMRService();
    importCmr.setAutoEngineProcess(true);

    for (Addr addr : requestData.getAddresses()) {
      // detach and let the import process run properly
      entityManager.detach(addr);
    }
    LOG.debug("Importing data for CMR " + data.getCmrNo() + " under country " + data.getCmrIssuingCntry());
    importCmr.doProcess(entityManager, httpRequest, params);

    // refresh the data on request data
    RequestData newReqData = new RequestData(entityManager, data.getId().getReqId());
    requestData.setAdmin(newReqData.getAdmin());
    requestData.setData(newReqData.getData());
    requestData.setAddresses(newReqData.getAddresses());

    if (geoHandler != null && originalV2Model != null) {
      LOG.debug("Saving V2 model data after import..");
      geoHandler.doAfterImportCMRFromAutomation(entityManager, originalV2Model, requestData);
    }

    result.setResults("Imported Successfully");
    result.setDetails("Data for CMR No. " + data.getCmrNo() + " under Issuing Country " + data.getCmrIssuingCntry() + " imported into the request.");
    return result;
  }

  /**
   * Checks if any address record on the request has IMPORT_IND = 'Y' which
   * means FindCMR has been done already
   * 
   * @param entityManager
   * @param reqId
   * @return
   */
  private boolean hasImportedCMRs(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("AUTO.CHEKC_FIND_CMR");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.exists();
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_CMR_IMPORT;
  }

  @Override
  public String getProcessDesc() {
    return "Import CMR Data";
  }

}
