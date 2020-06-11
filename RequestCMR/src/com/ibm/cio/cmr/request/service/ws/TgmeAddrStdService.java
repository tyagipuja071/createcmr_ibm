/**
 * 
 */
package com.ibm.cio.cmr.request.service.ws;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.TgmeCodes;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.ws.TgmeAddrStdModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.TgmeClient;
import com.ibm.cmr.services.client.tgme.AddressStdData;
import com.ibm.cmr.services.client.tgme.AddressStdRequest;
import com.ibm.cmr.services.client.tgme.AddressStdResponse;
import com.ibm.cmr.services.client.tgme.RawAddressStdResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class TgmeAddrStdService extends BaseSimpleService<TgmeAddrStdModel> {

  private static final Logger LOG = Logger.getLogger(TgmeAddrStdService.class);

  @Override
  protected TgmeAddrStdModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) {

    TgmeAddrStdModel result = new TgmeAddrStdModel();

    try {
      boolean clearStateSuggestion = false;
      AddressModel address = (AddressModel) params.getParam("address");
      if ("''".equals(address.getStateProv())) {
        address.setStateProv(null);
        clearStateSuggestion = true;
      }
      String key = address.getReqId() + "_" + address.getAddrType() + "_" + (address.getAddrSeq() != null ? address.getAddrSeq() : "");

      PropertyUtils.copyProperties(result, address);

      String stateProvDesc = getStateProvDesc(entityManager, address.getLandCntry(), address.getStateProv());
      if (stateProvDesc != null && stateProvDesc.contains(",")) {
        stateProvDesc = stateProvDesc.split(",")[0];
      }

      String cityToUse = address.getCity1();
      if (!StringUtils.isEmpty(address.getStdCityNm())) {
        cityToUse = address.getStdCityNm();
      }

      LOG.debug("Connecting to the TGME (Addr Std) service...");
      TgmeClient tgmeClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), TgmeClient.class);

      tgmeClient.setUser(SystemConfiguration.getSystemProperty("cmrservices.user"));
      tgmeClient.setPassword(SystemConfiguration.getSystemProperty("cmrservices.password"));

      AddressStdRequest tgmeRequest = new AddressStdRequest();
      tgmeRequest.setSystemId(SystemConfiguration.getSystemProperty("tgme.appID"));
      tgmeRequest.setAddressId(key);
      tgmeRequest.setAddressType(address.getAddrType());
      tgmeRequest.setCity(cityToUse);
      tgmeRequest.setCountryCode(address.getLandCntry());
      tgmeRequest.setCountyCode(address.getCounty());
      tgmeRequest.setPostalCode(address.getPostCd());
      tgmeRequest.setStateCode(address.getStateProv());
      String street = address.getAddrTxt();
      if (!StringUtils.isBlank(address.getAddrTxt2())) {
        street += " " + address.getAddrTxt2();
      }
      tgmeRequest.setStreet(street);
      result.setAddrTxt(street);

      AddressStdResponse tgmeResponse = tgmeClient.executeAndWrap(TgmeClient.ADDRESS_STD_V2_APP_ID, tgmeRequest, AddressStdResponse.class);
      if (tgmeResponse.getStatus() != TgmeClient.STATUS_SUCCESSFUL) {
        // try v1 of the addr std
        tgmeResponse = tgmeClient.executeAndWrap(TgmeClient.ADDRESS_STD_APP_ID, tgmeRequest, AddressStdResponse.class);
        if (tgmeResponse.getStatus() != TgmeClient.STATUS_SUCCESSFUL) {
          throw new Exception("TGME Error");
        }
      }

      AddressStdData reply = tgmeResponse.getData();
      RawAddressStdResponse raw = reply.getRaw();
      result.setStdAddrTxt(reply.getStreetAddressLine1());
      result.setStdAddrTxt2(reply.getStreetAddressLine2());
      result.setStdCity(reply.getCity());
      result.setStdLandCntry(reply.getCountry());
      result.setStdPostCd(reply.getPostalCode());
      if (!StringUtils.isBlank(reply.getStateProvinceCode())) {
        LOG.debug("State Code determined from TGME: " + reply.getStateProvinceCode());
        result.setStdStateCode(reply.getStateProvinceCode());
        result.setStdStateProv(reply.getStateProvinceName());
      }
      if (StringUtils.isBlank(result.getStdStateCode())) {
        if (reply.getStateProvinceName() != null && reply.getStateProvinceName().trim().length() > 3) {
          String stateProvStanCode = getStateProvCd(entityManager, address.getLandCntry(), reply.getStateProvinceName());
          result.setStdStateProv(reply.getStateProvinceName());
          result.setStdStateCode(stateProvStanCode);

        } else {
          String stateProvStanDesc = getStateProvDesc(entityManager, address.getLandCntry(), reply.getStateProvinceName().toUpperCase());
          if (stateProvStanDesc != null && stateProvStanDesc.contains(",")) {
            result.setStdStateProv(stateProvStanDesc.split(",")[0]);
            result.setStdStateCode(stateProvStanDesc.split(",")[1]);
          } else {
            result.setStdStateProv(stateProvStanDesc);
            result.setStdStateCode(reply.getStateProvinceName().toUpperCase());
          }
        }
      }

      if (clearStateSuggestion) {
        result.setStdStateCode("''");
      }

      result.setStdResultCity(raw.getStatuscity());
      result.setStdResultPostCd(raw.getStatuspostalcode());
      result.setStateProvDesc(stateProvDesc);

      result.setStdResultProvince(raw.getStatusprovince());
      result.setStdResultStreet(raw.getStatusstreetaddressline1());

      result.setStdResultCode(raw.getStatusaddress());
      TgmeCodes codeDesc = getTgmeCode(entityManager, result.getStdResultCode());
      if (codeDesc != null) {
        result.setStdResultDesc(codeDesc.getDesc());
        result.setStdResultStatus(codeDesc.getStatus());
        result.setStdResultText(codeDesc.getText());
      }
      LOG.debug("Got result: " + reply.getTgmeResponseCode());
      if (StringUtils.isBlank(reply.getTgmeResponseCode())) {
        throw new Exception("TGME Code is null, service may be down.");
      }
      return result;
    } catch (Exception e) {
      LOG.error("Exception in TGME (Addr Std) call", e);
      result.setStdResultCode("U");
      TgmeCodes codeDesc = getTgmeCode(entityManager, result.getStdResultCode());
      if (codeDesc != null) {
        result.setStdResultDesc(codeDesc.getDesc());
        result.setStdResultStatus(codeDesc.getStatus());
        result.setStdResultText(codeDesc.getText());
      }
      return result;
    }
  }

  private TgmeCodes getTgmeCode(EntityManager entityManager, String code) {
    String sql = ExternalizedQuery.getSql("TGME.GETCODEDESC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CD", code);
    List<TgmeCodes> results = query.getResults(1, TgmeCodes.class);
    if (results != null && results.size() > 0) {
      return results.get(0);
    }
    return null;

  }

  private String getStateProvDesc(EntityManager entityManager, String land1, String stateCd) {
    try {
      String sql = ExternalizedQuery.getSql("TGME.GETSTATEPROVDESC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setForReadOnly(true);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("LAND1", land1);
      query.setParameter("STATE", stateCd);
      query.setParameter("CODE", stateCd + "%");
      List<Object[]> results = query.getResults(1);
      if (results != null && results.size() > 0) {
        Object[] record = results.get(0);
        return (String) record[0] + "," + record[1];
      }
      return null;
    } catch (Exception e) {
      return null;
    }

  }

  protected String getStateProvCd(EntityManager entityManager, String land1, String stateDesc) {
    try {
      String sql = ExternalizedQuery.getSql("TGME.GETSTATEPROVCD");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setForReadOnly(true);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("LAND1", land1);
      String stateNameClean = stateDesc != null ? stateDesc.toUpperCase().trim() : "";
      stateNameClean = stateNameClean.replaceAll("[^\\x00-\\x7F]", "_");
      query.setParameter("STATE", stateDesc != null ? stateDesc.toUpperCase().trim() : "");
      query.setParameter("CLEAN", stateNameClean);
      List<Object[]> results = query.getResults(1);
      if (results != null && results.size() > 0) {
        Object[] record = results.get(0);
        return (String) record[0];
      }
      return null;
    } catch (Exception e) {
      return null;
    }

  }

}
