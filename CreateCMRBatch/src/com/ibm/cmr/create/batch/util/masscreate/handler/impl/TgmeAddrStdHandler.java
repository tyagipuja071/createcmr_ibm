/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.TgmeClient;
import com.ibm.cmr.services.client.tgme.AddressStdRequest;
import com.ibm.cmr.services.client.tgme.AddressStdResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public class TgmeAddrStdHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(TgmeAddrStdHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (row.getAddresses() != null) {
      // special handling for Postal code and County
      String batchUrl = SystemConfiguration.getValue("BATCH_SERVICES_URL");
      TgmeClient client = CmrServicesFactory.getInstance().createClient(batchUrl, TgmeClient.class);
      client.setUser(SystemConfiguration.getSystemProperty("cmrservices.user"));
      client.setPassword(SystemConfiguration.getSystemProperty("cmrservices.password"));

      AddressStdRequest request = null;
      AddressStdResponse response = null;
      for (MassCreateAddr addr : row.getAddresses()) {
        LOG.debug("Checking Postal Code values for  " + addr.getId().getParReqId() + " Type " + addr.getId().getAddrType() + " Sequence "
            + addr.getId().getSeqNo() + " Postal Code: " + addr.getPostCd());

        if (StringUtils.isBlank(addr.getPostCd()) || (addr.getPostCd() != null && addr.getPostCd().trim().length() < 9)) {
          LOG.debug("Postal Code is empty or less than 9 characters. Value: '" + addr.getPostCd() + "'");
          request = new AddressStdRequest();
          request.setAddressId("CCMR-" + addr.getId().getParReqId() + "-" + addr.getId().getAddrType() + "-" + addr.getId().getParReqId());
          request.setAddressType(addr.getId().getAddrType());
          request.setCity(addr.getCity1());
          request.setCountryCode(addr.getLandCntry());
          request.setStateCode(addr.getStateProv());
          request.setStreet(addr.getAddrTxt());
          request.setSystemId("CreateCMR");

          response = client.executeAndWrap(TgmeClient.ADDRESS_STD_APP_ID, request, AddressStdResponse.class);

          if (response != null && response.getData() != null) {
            LOG.debug("TGME response code: " + response.getData().getTgmeResponseCode());
          } else {
            LOG.debug("TGME response is null.");
          }
          if (response != null && response.getData() != null && !StringUtils.isEmpty(response.getData().getPostalCode())) {
            if (row.getRawValue(addr.getId().getAddrType() + "-CITY1") != null
                && !StringUtils.isEmpty(row.getRawValue(addr.getId().getAddrType() + "-CITY1").toString())) {
              String postalCode = response.getData().getPostalCode();
              LOG.debug("TGME Postal Code: " + postalCode);
              postalCode = USPostCodeAndStateHandler.formatPostalCode(postalCode);
              String inputPost = addr.getPostCd() != null ? addr.getPostCd().trim() : "";
              inputPost = USPostCodeAndStateHandler.formatPostalCode(inputPost);
              // blank post code, or TGME has more digits
              if (StringUtils.isBlank(addr.getPostCd())
                  || (postalCode.substring(0, 5).equals(inputPost.substring(0, 5)) && "0000".equals(inputPost.substring(6)))) {
                LOG.debug("Setting to TGME value : " + postalCode);
                row.mapRawValue(addr.getId().getAddrType() + "-POST_CD", postalCode);
                row.addUpdateCol(addr.getId().getAddrType() + "-POST_CD");
                addr.setPostCd(postalCode);
              } else {
                LOG.debug("Keeping existing value " + addr.getPostCd());
              }
            }
          }
        }
      }

    }
    return RowResult.passed();
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
