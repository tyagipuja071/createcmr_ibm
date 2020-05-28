package com.ibm.cio.cmr.request.automation.impl.us;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.QueryClient;
import com.ibm.cmr.services.client.query.QueryRequest;
import com.ibm.cmr.services.client.query.QueryResponse;

/**
 * 
 * @author RoopakChugh
 *
 */

public class USDelReacCheckElement extends ValidatingElement {

  private static final Logger log = Logger.getLogger(USDelReacCheckElement.class);

  public USDelReacCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    log.debug("Performing US Delete/Reactivation Check");
    ChangeLogListener.setManager(entityManager);
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    AutomationResult<ValidationOutput> result = buildResult(admin.getId().getReqId());
    ValidationOutput output = new ValidationOutput();
    StringBuilder details = new StringBuilder();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    if (StringUtils.isNotBlank(admin.getReqType())) {
      switch (admin.getReqType()) {
      case "R":
        List<String> cmrNoList = getCMRNoList(entityManager, admin.getId().getReqId());
        if (!cmrNoList.isEmpty()) {
          // get date 6 months ago
          Calendar c = Calendar.getInstance();
          c.setTime(new Date());
          c.add(Calendar.MONTH, -6);
          boolean oldCMRExist = false;
          Date dateSixMonthsAgo = c.getTime();
          for (String cmrNo : cmrNoList) {
            // get last update ts from RDC
            String sql = ExternalizedQuery.getSql("AUTO.US.GET_LAST_UPD_TS_RDC");
            PreparedQuery query = new PreparedQuery(entityManager, sql);
            query.setParameter("ZZKV_CUSNO", cmrNo);
            query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
            query.setParameter("KATR6", data.getCmrIssuingCntry());
            query.setForReadOnly(true);
            String rdcUpdTs = query.getSingleResult(String.class);
            if (rdcUpdTs != null) {
              Date lastUpdTs = formatter.parse(rdcUpdTs);
              if (lastUpdTs != null && lastUpdTs.before(dateSixMonthsAgo)) {
                oldCMRExist = true;
                engineData.setScenarioVerifiedIndc("N");
                details.append("CMR No. " + cmrNo + " updated more than 6 months ago.").append("\n");
              }
            } else {
              String url = SystemConfiguration.getValue("BATCH_SERVICES_URL");
              String usSchema = SystemConfiguration.getValue("US_CMR_SCHEMA");
              String sql1 = ExternalizedQuery.getSql("AUTO.US.GET_LAST_UPD_TS_USCMR", usSchema);
              sql1 = StringUtils.replace(sql1, ":CMR_NO", "'" + cmrNo + "'");
              String dbId = QueryClient.USCMR_APP_ID;
              QueryRequest queryRequest = new QueryRequest();
              queryRequest.setSql(sql1);
              queryRequest.setRows(1);
              queryRequest.addField("D_CHANGE");
              QueryClient client = CmrServicesFactory.getInstance().createClient(url, QueryClient.class);

              QueryResponse response = client.executeAndWrap(dbId, queryRequest, QueryResponse.class);

              if (response.isSuccess() && response.getRecords() != null && !response.getRecords().isEmpty()) {
                String dateString = (String) response.getRecords().get(0).get("D_CHANGE");
                if (StringUtils.isNotBlank(dateString)) {
                  Date usCmrDate = formatter.parse(dateString);
                  if (usCmrDate != null && usCmrDate.before(dateSixMonthsAgo)) {
                    oldCMRExist = true;
                    admin.setScenarioVerifiedIndc("N");
                    details.append("CMR No. " + cmrNo + " updated more than 6 months ago.").append("\n");
                  }
                }
              } else {
                log.warn("Query to USCMR data base failed.");
              }
            }
          }

          if (oldCMRExist) {
            result.setResults("Approval Needed");
            details.append("\nCMRs updated before 6 months exist on the request. An approval from CMR Owner is required to proceed.").append("\n");
          } else {
            result.setResults("Validated");
            details.append("\nNo CMRs updated before 6 months exist on the request.").append("\n");
          }
        } else {
          result.setOnError(true);
          details.append("No CMRs found on the request for reactivation.").append("\n");
          result.setResults("No CMRs Found.");
        }
        break;
      case "D":
        List<String> cmrNos = getCMRNoList(entityManager, admin.getId().getReqId());
        if (!cmrNos.isEmpty()) {
          result.setResults("Approval Needed");
          details.append("Delete request detected, approval will be generated to CMR owner.").append("\n");
        } else {
          result.setOnError(true);
          details.append("No CMRs found on the request for deletion.").append("\n");
          result.setResults("No CMRs Found.");
        }
        break;
      default:
        details.append("Skipping Delete/Reactivation check");
        result.setResults("Skipped");
        break;
      }

    }

    result.setDetails(details.toString());
    return result;

  }

  private List<String> getCMRNoList(EntityManager entityManager, long reqId) {
    String sql = "SELECT CMR_NO FROM CREQCMR.MASS_UPDT WHERE PAR_REQ_ID=:PAR_REQ_ID";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("PAR_REQ_ID", reqId);
    query.setForReadOnly(true);
    return query.getResults(String.class);
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.US_DEL_REAC_CHECK;
  }

  @Override
  public String getProcessDesc() {
    return "US - Delete/Reactivation Check";
  }

}
