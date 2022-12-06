/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DPLCheckClient;
import com.ibm.cmr.services.client.dpl.DPLCheckRequest;
import com.ibm.cmr.services.client.dpl.DPLCheckResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public class DPLCheckHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(DPLCheckHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (row.isCreateByModel()) {
      return RowResult.passed();
    }
    RowResult result = new RowResult();

    if (row.getAddresses() != null) {
      String batchUrl = SystemConfiguration.getValue("BATCH_SERVICES_URL");
      String batchUserId = SystemConfiguration.getValue("BATCH_USERID");
      DPLCheckClient dplClient = CmrServicesFactory.getInstance().createClient(batchUrl, DPLCheckClient.class);
      DPLCheckRequest request = null;
      DPLCheckResponse response = null;

      MassCreateData data = row.getData();
      StringBuilder msg = new StringBuilder();
      boolean dplCheckPassed = true;
      boolean dplErr = false;
      for (MassCreateAddr addr : row.getAddresses()) {
        request = new DPLCheckRequest();
        request.setId("MC-" + addr.getId().getParReqId() + "-" + addr.getId().getAddrType() + "-" + addr.getId().getSeqNo());
        request.setApplication(batchUserId);
        request.setCompanyName(data.getCustNm1() + (StringUtils.isBlank(data.getCustNm2()) ? "" : " " + data.getCustNm2()));
        request.setCountry(addr.getLandCntry());
        request.setAddr1(addr.getAddrTxt());
        request.setAddr2(addr.getAddrTxt2());
        request.setCity(addr.getCity1());
        try {
          LOG.debug("Performing DPL Check on Mass Create ID " + data.getId().getParReqId() + " Type " + addr.getId().getAddrType() + " Sequence "
              + addr.getId().getSeqNo());
          String dplSystemId = SystemUtil.useKYCForDPLChecks() ? DPLCheckClient.KYC_APP_ID : DPLCheckClient.EVS_APP_ID;
          response = dplClient.executeAndWrap(dplSystemId, request, DPLCheckResponse.class);
          if (response.isSuccess() && !response.getResult().isPassed()) {
            dplCheckPassed = false;
          } else if (!response.isSuccess()) {
            dplErr = true;
          }

        } catch (Exception e) {
          dplErr = true;
          LOG.warn("DPL check failed. Will not mark as a validation error.", e);
        }
      }
      if (!dplCheckPassed) {
        msg.append("FAILED");
      } else if (dplErr) {
        msg.append("Cannot connect to service.");
      } else {
        msg.append("Passed");
      }
      row.mapRawValue("DPL_CHECK", msg.toString());
      row.addUpdateCol("DPL_CHECK");
    }

    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
