/**
 * 
 */
package com.ibm.cio.cmr.request.service.cris;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cmr.services.client.CRISServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.cris.CRISQueryRequest;
import com.ibm.cmr.services.client.cris.CRISQueryResponse;

/**
 * @author JeffZAMORA
 * 
 */
@Component
public class CRISService extends BaseSimpleService<CRISQueryResponse> {

  private static final Logger LOG = Logger.getLogger(CRISService.class);

  public static void main(String[] args) {
    for (Field f : CRISQueryRequest.class.getDeclaredFields()) {
      System.out.println("<form:hidden path=\"" + f.getName() + "\" />");
    }
  }

  @Override
  protected CRISQueryResponse doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    Object queryRequest = params.getParam("criteria");
    LOG.debug("Executin CRIS query..");
    CRISServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"),
        CRISServiceClient.class);
    CRISQueryResponse queryResponse = client.executeAndWrap(CRISServiceClient.QUERY_APP_ID, queryRequest, CRISQueryResponse.class);
    LOG.debug("CRIS query executed successfully. Return: " + (queryResponse != null && queryResponse.isSuccess() ? "Success" : "Error"));
    return queryResponse;
  }

}
