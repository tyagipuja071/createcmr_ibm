/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.StandardCityServiceClient;
import com.ibm.cmr.services.client.stdcity.County;
import com.ibm.cmr.services.client.stdcity.StandardCityRequest;
import com.ibm.cmr.services.client.stdcity.StandardCityResponse;

/**
 * Connects to the standard city service and validates City and County entries
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CityAndCountyHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(CityAndCountyHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    RowResult result = new RowResult();

    if (row.getAddresses() != null) {
      String batchUrl = SystemConfiguration.getValue("BATCH_SERVICES_URL");
      StandardCityServiceClient client = CmrServicesFactory.getInstance().createClient(batchUrl, StandardCityServiceClient.class);

      StandardCityRequest request = null;
      StandardCityResponse response = null;
      MassCreateData data = row.getData();
      for (MassCreateAddr addr : row.getAddresses()) {
        if (addr.isVirtual()) {
          LOG.debug(addr.getId().getAddrType() + " is virtual. Skipping.");
          continue;
        }

        request = new StandardCityRequest();
        try {
          request.setCity(addr.getCity1());
          request.setCountry(addr.getLandCntry());
          request.setCountyName(addr.getCounty());
          request.setPostalCode(addr.getPostCd());
          request.setState(addr.getStateProv());
          request.setSysLoc(data.getCmrIssuingCntry());
          LOG.debug("Checking City and County values for  " + data.getId().getParReqId() + " Type " + addr.getId().getAddrType() + " Sequence "
              + addr.getId().getSeqNo());

          client.setStandardCityRequest(request);
          response = client.executeAndWrap(StandardCityResponse.class);

          if (response.isSuccess()) {
            if (response.isCityMatched()) {
              if (!StringUtils.equals(addr.getCity1().toUpperCase().trim(), response.getStandardCity())) {
                row.mapRawValue(addr.getId().getAddrType() + "-CITY1", response.getStandardCity());
                row.addUpdateCol(addr.getId().getAddrType() + "-CITY1");
                addr.setCity1(response.getStandardCity());
              }
              if ((StringUtils.isBlank(addr.getCounty()) && !StringUtils.isBlank(response.getStandardCountyName()))
                  || (!StringUtils.equals(addr.getCounty().toUpperCase().trim(), response.getStandardCountyName()))) {
                row.mapRawValue(addr.getId().getAddrType() + "-COUNTY", response.getStandardCountyName());
                row.addUpdateCol(addr.getId().getAddrType() + "-COUNTY");
                addr.setCounty(response.getStandardCountyName());
              }
              String postCd = !StringUtils.isBlank(addr.getPostCd()) ? addr.getPostCd() : "00000-0000";
              postCd = USPostCodeAndStateHandler.formatPostalCode(postCd);
              String stdPostCd = !StringUtils.isBlank(response.getStandardPostalCd()) ? response.getStandardPostalCd() : "00000-0000";
              stdPostCd = USPostCodeAndStateHandler.formatPostalCode(stdPostCd);

              LOG.debug("City Matched. Post Code: " + postCd + " Suggested: " + stdPostCd);
              if ("00000-0000".equals(postCd)) {
                if (!stdPostCd.contains("-")) {
                  stdPostCd = stdPostCd.substring(0, 5) + "-" + stdPostCd.substring(5);
                }
                if (stdPostCd.length() > 10) {
                  stdPostCd = stdPostCd.substring(0, 10);
                }
                if (!"US".equals(addr.getLandCntry())) {
                  stdPostCd = "00000";
                }
                addr.setPostCd(stdPostCd);
                row.mapRawValue(addr.getId().getAddrType() + "-POST_CD", stdPostCd);
                row.addUpdateCol(addr.getId().getAddrType() + "-POST_CD");
              }
            } else {
              List<County> suggested = response.getSuggested();
              StringBuilder msg = new StringBuilder();

              if (suggested != null && suggested.size() > 0) {
                msg.append(addr.getId().getAddrType() + " City Name: Please choose from -\n");
                for (County county : suggested) {
                  msg.append("  City: " + county.getCity() + ", County: " + county.getName() + "\n");
                }
              } else {
                msg.append(addr.getId().getAddrType() + " City Name: Cannot match to any candidate.");
              }

              result.addError(msg.toString());
            }
          } else {
            result.addError(addr.getId().getAddrType() + " Cannot verify City Name. Ensure details are correct.");
          }
        } catch (Exception e) {
          LOG.warn("DPL check failed. Will not mark as a validation error.", e);
        }
      }
    }

    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    // to avoid multiple calls, the transform will happen above
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
