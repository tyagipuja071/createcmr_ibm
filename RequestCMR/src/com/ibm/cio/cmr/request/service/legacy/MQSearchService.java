/**
 * 
 */
package com.ibm.cio.cmr.request.service.legacy;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.LegacyMQRecord;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.search.MQSearchResult;
import com.ibm.cio.cmr.request.util.search.MQSearchResult.System;
import com.ibm.cio.cmr.request.util.sof.SOFQueryHandler;
import com.ibm.cio.cmr.request.util.sof.SOFRecord;
import com.ibm.cio.cmr.request.util.wtaas.WtaasRecord;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.WtaasClient;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;
import com.ibm.cmr.services.client.wtaas.WtaasQueryRequest;
import com.ibm.cmr.services.client.wtaas.WtaasQueryResponse;

/**
 * @author JeffZAMORA
 *
 */
@Component
public class MQSearchService extends BaseSimpleService<MQSearchResult<?>> {

  private static final Logger LOG = Logger.getLogger(MQSearchService.class);

  @Override
  protected MQSearchResult<?> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    try {
      String country = (String) params.getParam("country");
      String cmrNo = (String) params.getParam("cmrNo");
      if (StringUtils.isBlank(country) || StringUtils.isBlank(cmrNo)) {
        throw new Exception("Country and/or CMR No. is missing.");
      }
      String sql = "select s.PROCESSING_TYP, g.GEO_CD from CREQCMR.SUPP_CNTRY s, CREQCMR.CNTRY_GEO_DEF g where s.CNTRY_CD = g.CMR_ISSUING_CNTRY and s.CNTRY_CD = :CNTRY";
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CNTRY", country);
      query.setForReadOnly(true);

      List<Object[]> results = query.getResults(1);
      if (results == null || results.isEmpty()) {
        throw new Exception("Contry " + country + " not supported.");
      }
      Object[] rec = results.get(0);
      String type = (String) rec[0];
      String geo = (String) rec[1];
      if (!"MQ".equals(type)) {
        throw new Exception("Contry " + country + " not supported.");
      }

      if ("AP".equals(geo)) {
        return queryWTAAS(country, cmrNo);
      } else {
        return querySOF(country, cmrNo);
      }
    } catch (Exception e) {
      LOG.error("Error when querying: " + e.getMessage());
      MQSearchResult<LegacyMQRecord> result = new MQSearchResult<LegacyMQRecord>();
      result.setSuccess(false);
      result.setMsg(e.getMessage());
      return result;
    }
  }

  /**
   * Connects to the WTAAS query service and gets the CMR details
   * 
   * @param country
   * @param cmrNo
   * @return
   * @throws Exception
   */
  private MQSearchResult<WtaasRecord> queryWTAAS(String country, String cmrNo) throws Exception {
    LOG.debug("Querying CMR " + cmrNo + " from WTAAS..");
    WtaasClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), WtaasClient.class);

    WtaasQueryRequest request = new WtaasQueryRequest();
    request.setCmrNo(cmrNo);
    request.setCountry(country);

    WtaasQueryResponse response = client.executeAndWrap(WtaasClient.QUERY_ID, request, WtaasQueryResponse.class);
    if (response == null || !response.isSuccess()) {
      LOG.warn("Error or no response from WTAAS query.");
      throw new Exception("Error or no response from WTAAS query.");
    }
    if ("F".equals(response.getData().get("Status"))) {
      LOG.warn("Customer " + cmrNo + " does not exist in WTAAS.");
      throw new Exception("Customer " + cmrNo + " does not exist in WTAAS.");
    }

    WtaasRecord record = WtaasRecord.createFrom(response);

    MQSearchResult<WtaasRecord> result = new MQSearchResult<>();
    result.setCmrNo(cmrNo);
    result.setCountry(country);
    result.setSystem(System.WTAAS);
    result.setRecord(record);
    result.setSuccess(true);
    return result;

  }

  /**
   * Connects to the SOF query service and gets the CMR details
   * 
   * @param country
   * @param cmrNo
   * @return
   * @throws Exception
   */
  private MQSearchResult<SOFRecord> querySOF(String country, String cmrNo) throws Exception {
    LOG.debug("Querying CMR " + cmrNo + " from SOF..");
    String useCountry = "";
    switch (country) {
    case SystemLocation.IRELAND:
      useCountry = "866";
      break;
    case SystemLocation.ISRAEL:
      useCountry = "754";
      break;
    default:
      useCountry = country;
    }

    SOFServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), SOFServiceClient.class);

    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry(useCountry);
    request.setCmrNo(cmrNo);

    SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
    if (response.isSuccess()) {
      String xmlData = response.getData();
      SOFQueryHandler handler = new SOFQueryHandler();
      List<SOFRecord> resp = handler.extractRecord(xmlData.getBytes());
      if (resp == null || resp.isEmpty()) {
        throw new Exception("CMR " + cmrNo + " not found.");
      }
      MQSearchResult<SOFRecord> result = new MQSearchResult<>();
      result.setCmrNo(cmrNo);
      result.setCountry(country);
      result.setSystem(System.SOF);
      result.setRecord(resp.get(0));
      result.setSuccess(true);
      return result;

    } else {
      throw new Exception(response.getMsg());
    }
  }

}
