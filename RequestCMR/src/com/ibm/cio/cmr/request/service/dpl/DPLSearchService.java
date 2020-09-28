/**
 * 
 */
package com.ibm.cio.cmr.request.service.dpl;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class DPLSearchService extends BaseSimpleService<Object> {

  @Override
  protected Object doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String processType = (String) params.getParam("processType");
    if (StringUtils.isBlank(processType)) {
      throw new Exception("Process Type is not defined.");
    }
    Object result = null;
    switch (processType) {
    case "REQ":
    case "SEARCH":
    case "ATTACH":
    }
    return null;
  }

  /**
   * Retrieves the details of the current request and performs DPL check
   * 
   * @param entityManager
   * @param params
   * @return
   */
  private Object processRequest(EntityManager entityManager, ParamContainer params) throws Exception {
    long reqId = (long) params.getParam("reqId");
    RequestData reqData = new RequestData(entityManager, reqId);
    if (reqData.getAdmin() == null) {
      throw new Exception("Request ");
    }
    return null;
  }

}
