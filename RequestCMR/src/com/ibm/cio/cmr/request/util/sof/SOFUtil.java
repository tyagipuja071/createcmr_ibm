/**
 * 
 */
package com.ibm.cio.cmr.request.util.sof;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.SOFServiceClient;
import com.ibm.cmr.services.client.sof.SOFQueryRequest;
import com.ibm.cmr.services.client.sof.SOFQueryResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SOFUtil {

  public static SOFRecord getRecordByCN(String cntry, String cmrNo) throws Exception {
    List<SOFRecord> records = getRecords(cntry, cmrNo, null);
    if (records != null && records.size() > 0) {
      return records.get(0);
    }
    return null;
  }

  public static List<SOFRecord> getRecordsBySiret(String cntry, String siret) throws Exception {
    return getRecords(cntry, null, siret);
  }

  private static List<SOFRecord> getRecords(String cntry, String cmrNo, String siret) throws Exception {
    SOFServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), SOFServiceClient.class);

    SOFQueryRequest request = new SOFQueryRequest();
    request.setCmrIssuingCountry(cntry);
    request.setCmrNo(cmrNo);
    if (!StringUtils.isEmpty(siret)) {
      request.setSiret(siret);
    }

    SOFQueryResponse response = client.executeAndWrap(SOFServiceClient.QUERY_APP_ID, request, SOFQueryResponse.class);
    if (response.isSuccess()) {
      String xmlData = response.getData();
      SOFQueryHandler handler = new SOFQueryHandler();
      List<SOFRecord> resp = handler.extractRecord(xmlData.getBytes());
      return resp;
    } else {
      return new ArrayList<SOFRecord>();
    }
  }
}
