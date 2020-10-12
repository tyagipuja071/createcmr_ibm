/**
 * 
 */
package com.ibm.cio.cmr.request.service.dpl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.util.CommonWordsUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.dpl.DPLResultCompany;
import com.ibm.cio.cmr.request.util.dpl.DPLResultsItemizer;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DPLCheckClient;
import com.ibm.cmr.services.client.dpl.DPLRecord;
import com.ibm.cmr.services.client.dpl.DPLSearchRequest;
import com.ibm.cmr.services.client.dpl.DPLSearchResponse;
import com.ibm.cmr.services.client.dpl.DPLSearchResults;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class DPLSearchService extends BaseSimpleService<Object> {

  private static final Logger LOG = Logger.getLogger(DPLSearchService.class);

  @Override
  protected Object doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String processType = (String) params.getParam("processType");
    if (StringUtils.isBlank(processType)) {
      throw new Exception("Process Type is not defined.");
    }
    switch (processType) {
    case "REQ":
      return processRequest(entityManager, params);
    case "SEARCH":
      return doDplSearch(entityManager, params);
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
  private RequestData processRequest(EntityManager entityManager, ParamContainer params) throws Exception {
    long reqId = (long) params.getParam("reqId");
    LOG.debug("Retreiving Request data for Request ID " + reqId);
    RequestData reqData = new RequestData(entityManager, reqId);
    if (reqData.getAdmin() == null) {
      throw new Exception("Request " + reqId + " does not exist.");
    }
    return reqData;
  }

  /**
   * Queries the database and does a dpl search
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws Exception
   */
  private List<DPLResultsItemizer> doDplSearch(EntityManager entityManager, ParamContainer params) throws Exception {
    List<String> names = new ArrayList<String>();
    List<DPLSearchResults> results = new ArrayList<DPLSearchResults>();

    Long reqId = (long) params.getParam("reqId");
    if (reqId == null || reqId == 0) {
      String searchString = (String) params.getParam("searchString");
      if (searchString != null) {
        names.add(searchString.toUpperCase().trim());
      }
    } else {
      RequestData reqData = processRequest(entityManager, params);
      GEOHandler handler = RequestUtils.getGEOHandler(reqData.getData().getCmrIssuingCntry());
      if (handler != null && !handler.customerNamesOnAddress()) {
        String name = reqData.getAdmin().getMainCustNm1().toUpperCase();
        if (!StringUtils.isBlank(reqData.getAdmin().getMainCustNm2())) {
          name += " " + reqData.getAdmin().getMainCustNm2().toUpperCase();
        }
        names.add(name);
      } else {
        for (Addr addr : reqData.getAddresses()) {
          String name = addr.getCustNm1().toUpperCase();
          if (!StringUtils.isBlank(addr.getCustNm2())) {
            name += " " + addr.getCustNm2().toUpperCase();
          }
          if (!names.contains(name)) {
            names.add(name);
          }
        }
      }
    }

    if (names.isEmpty()) {
      LOG.debug("No name specified to search.");
      return null;
    }

    // get a minimized name from search name
    List<String> minimizedList = new ArrayList<String>();
    for (String name : names) {
      String minimized = CommonWordsUtil.minimize(name).toUpperCase();
      if (!names.contains(minimized) && !minimizedList.contains(minimized)) {
        minimizedList.add(minimized);
      }
    }
    names.addAll(minimizedList);

    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    DPLCheckClient client = CmrServicesFactory.getInstance().createClient(baseUrl, DPLCheckClient.class);
    for (String searchString : names) {
      DPLSearchRequest request = new DPLSearchRequest();
      request.setCompanyName(searchString);
      try {
        LOG.debug("Performing DPL Search on " + searchString);
        DPLSearchResponse resp = client.executeAndWrap(DPLCheckClient.DPL_SEARCH_APP_ID, request, DPLSearchResponse.class);
        if (resp.isSuccess()) {
          DPLSearchResults result = resp.getResults();
          result.setSearchArgument(searchString);
          results.add(result);
        }
      } catch (Exception e) {
        LOG.warn("DPL Search encountered an error for " + searchString, e);
      }

    }

    List<DPLResultsItemizer> list = new ArrayList<DPLResultsItemizer>();

    List<String> entityIds = new ArrayList<String>();
    for (DPLSearchResults result : results) {
      DPLResultsItemizer itemizer = new DPLResultsItemizer();
      itemizer.setSearchArgument(result.getSearchArgument());

      int itemNo = 1;
      for (DPLRecord record : result.getDeniedPartyRecords()) {
        String dplName = record.getCompanyName();
        if (StringUtils.isBlank(dplName) && !StringUtils.isBlank(record.getCustomerLastName())) {
          dplName = record.getCustomerFirstName() + " " + record.getCustomerLastName();
        }
        if (dplName == null) {
          dplName = "";
        }

        if (!entityIds.contains(record.getEntityId())) {
          DPLResultCompany company = itemizer.get(dplName);
          if (company == null) {
            company = new DPLResultCompany();
            company.setCompanyName(dplName);
            company.setItemNo(itemNo++);
            company.getRecords().add(record);
            itemizer.getRecords().add(company);
          } else {
            company.getRecords().add(record);
          }
          entityIds.add(record.getEntityId());
        }
      }
      list.add(itemizer);
    }
    return list;
  }
}
