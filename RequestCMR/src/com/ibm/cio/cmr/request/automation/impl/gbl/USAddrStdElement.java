package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.TgmeCodes;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.service.ws.TgmeAddrStdService;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.TgmeClient;
import com.ibm.cmr.services.client.stdcity.StandardCityResponse;
import com.ibm.cmr.services.client.tgme.AddressStdData;
import com.ibm.cmr.services.client.tgme.AddressStdRequest;
import com.ibm.cmr.services.client.tgme.AddressStdResponse;

public class USAddrStdElement extends OverridingElement {

  public USAddrStdElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
    // TODO Auto-generated constructor stub
  }

  private static final Logger LOG = Logger.getLogger(TgmeAddrStdService.class);

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {

    long reqId = requestData.getAdmin().getId().getReqId();
    AutomationResult<OverrideOutput> results = buildResult(reqId);
    OverrideOutput overrides = new OverrideOutput(false);
    Data data = requestData.getData();

    if (!SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry())) {
      results.setResults("Standardized");
      results.setDetails("Skipped for non US request.\n");
      results.setProcessOutput(overrides);
      results.setOnError(false);
      return results;
    }

    LOG.debug("Connecting to the TGME (Addr Std) service...");
    TgmeClient tgmeClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), TgmeClient.class);
    tgmeClient.setUser(SystemConfiguration.getSystemProperty("cmrservices.user"));
    tgmeClient.setPassword(SystemConfiguration.getSystemProperty("cmrservices.password"));
    StringBuilder details = new StringBuilder();

    List<Addr> a = requestData.getAddresses();
    boolean hasIssues = false;
    for (Addr addr : a) {
      String key = addr.getId().getReqId() + "_" + addr.getId().getAddrType() + "_"
          + (addr.getId().getAddrSeq() != null ? addr.getId().getAddrSeq() : "");
      // Calling Address Std Service
      AddressStdRequest requestadd = new AddressStdRequest();
      requestadd.setSystemId(SystemConfiguration.getSystemProperty("tgme.appID"));
      requestadd.setAddressId(key);
      requestadd.setAddressType(addr.getId().getAddrType());
      requestadd.setCity(addr.getCity1());
      requestadd.setCountryCode(addr.getLandCntry());
      requestadd.setCountyCode(addr.getCounty());
      requestadd.setPostalCode(addr.getPostCd());
      requestadd.setStateCode(addr.getStateProv());
      // requestadd.setStateName(addr.getStdCityNm());
      String street = addr.getAddrTxt();
      String stateProvDesc = getStateProvDesc(entityManager, addr.getLandCntry(), addr.getStateProv());
      if (stateProvDesc != null && stateProvDesc.contains(",")) {
        stateProvDesc = stateProvDesc.split(",")[0];
      }

      String city = addr.getCity1();
      if (!StringUtils.isEmpty(addr.getStdCityNm())) {
        city = addr.getStdCityNm();
      }
      requestadd.setCity(city);

      if (!StringUtils.isBlank(addr.getAddrTxt2())) {
        street += " " + addr.getAddrTxt2();
      }

      if (!StringUtils.isBlank(street)) {
        if (street.length() <= 70) {
          requestadd.setStreet(street);
        } else {
          street = street.substring(0, 69);
        }
      }
      requestadd.setStreet(street);

      LOG.debug("Connecting to the AddressStd Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      AddressStdData tgmeData = null;
      boolean tgmeError = false;
      try {
        AddressStdResponse response = tgmeClient.executeAndWrap(TgmeClient.ADDRESS_STD_APP_ID, requestadd, AddressStdResponse.class);
        LOG.debug("Response status is " + response.getStatus());
        if (response.getStatus() != TgmeClient.STATUS_SUCCESSFUL) {
          tgmeError = true;
        } else {
          tgmeData = response.getData();
        }
      } catch (Exception e) {
        LOG.warn("Error in connecting to address standardization.", e);
        tgmeError = true;
      }
      if (tgmeError) {
        details.append("Cannot connect to address standardization service at the moment.");
        tgmeData = new AddressStdData();
        tgmeData.setTgmeResponseCode("U");
      }
      LOG.debug("Tgme Resonse code is : " + tgmeData.getTgmeResponseCode());

      TgmeCodes codeDesc = getTgmeCode(entityManager, tgmeData.getTgmeResponseCode());
      if (codeDesc != null) {
        LOG.debug("Address Standardization result : " + codeDesc.getText());
        details.append("Address Standardization result : " + codeDesc.getText() + "\n");
      }

      // create data elements for import for Street, City, and Postal Code.
      if (!StringUtils.isBlank(tgmeData.getPostalCode())) {
        LOG.debug(" Postal code is  : " + tgmeData.getPostalCode());
        details.append("Postal Code: " + tgmeData.getPostalCode() + "\n");
        if (tgmeData.getPostalCode().trim().length() > addr.getPostCd().length()) {
          details.append(" - Postal Code overwritten on request.\n");
          overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "POST_CD", addr.getPostCd(), tgmeData.getPostalCode().trim());
        }
      }

      if (!StringUtils.isBlank(tgmeData.getCity())) {
        LOG.debug("City is : " + tgmeData.getCity());
        details.append("City: " + tgmeData.getCity() + "\n");
        if (!tgmeData.getCity().toUpperCase().equals(addr.getCity1().toUpperCase())) {
          // overrides.addOverride(getProcessCode(),
          // addr.getId().getAddrType(), "CITY1", addr.getCity1(),
          // data1.getCity());
        }
      }

      if (!StringUtils.isBlank(tgmeData.getStreetAddressLine1())) {
        LOG.debug("Address is : " + tgmeData.getStreetAddressLine1());
        details.append("Address Line 1: " + tgmeData.getStreetAddressLine1() + "\n");
        // overrides.addOverride(getProcessCode(), addr.getId().getAddrType(),
        // "CITY1", addr.getCity1(), data1.getCity());
        if (!tgmeData.getStreetAddressLine1().toUpperCase().equals(addr.getAddrTxt().toUpperCase())) {
          // overrides.addOverride(getProcessCode(),
          // addr.getId().getAddrType(), "ADDR_TXT", addr.getAddrTxt(),
          // data1.getStreetAddressLine1());
        }
      }

      if (!StringUtils.isBlank(tgmeData.getStreetAddressLine2())) {
        LOG.debug("Address is : " + tgmeData.getStreetAddressLine2());
        details.append("AddressLine 2: " + tgmeData.getStreetAddressLine2() + "\n");
        if (!tgmeData.getStreetAddressLine2().toUpperCase().equals(addr.getAddrTxt2() != null ? addr.getAddrTxt2().toUpperCase() : "")) {
          // overrides.addOverride(getProcessCode(),
          // addr.getId().getAddrType(), "ADDR_TXT_2", addr.getAddrTxt2(),
          // data1.getStreetAddressLine2());
        }
      }

      if (!StringUtils.isBlank(tgmeData.getStateProvinceCode())) {
        LOG.debug("State is : " + tgmeData.getStateProvinceCode());
        details.append("State/Province Code: " + tgmeData.getStateProvinceCode() + "\n");
        if (!tgmeData.getStateProvinceCode().toUpperCase().equals(addr.getStateProv().toUpperCase())) {
          // overrides.addOverride(getProcessCode(),
          // addr.getId().getAddrType(), "STATE_PROV", addr.getStateProv(),
          // data1.getStateProvinceCode());
        }
      }

      // if (!StringUtils.isBlank(data1.getStateProvinceCode())) {
      // LOG.debug("State Code determined from TGME: " +
      // data1.getStateProvinceCode());
      // overrides.addOverride(getProcessCode(), addr.getId().getAddrType(),
      // "", addr.getStateProv(), data1.getStateProvinceCode());
      // }

      AddressModel addrModel = new AddressModel();
      PropertyUtils.copyProperties(addrModel, addr);
      addrModel.setCmrIssuingCntry(SystemLocation.UNITED_STATES);

      CmrClientService stdCity = new CmrClientService();
      ModelMap map = new ModelMap();
      stdCity.getStandardCity(addrModel, SystemLocation.UNITED_STATES, map);
      StandardCityResponse stdCityResp = (StandardCityResponse) map.get("result");
      if (stdCityResp != null) {
        if (stdCityResp.isCityMatched()) {
          LOG.debug("Standard City Name: " + stdCityResp.getStandardCity());
          details.append(("ZS01".equals(addr.getId().getAddrType()) ? "Install-at" : "Invoice-to") + " Standard City Name: "
              + stdCityResp.getStandardCity() + "\n");
          if (!addr.getCity1().trim().equalsIgnoreCase(stdCityResp.getStandardCity().trim())) {
            overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "CITY1", addr.getCity1(), stdCityResp.getStandardCity());
          }
        } else {
          LOG.debug("Standard City Name cannot be determined. Found: " + stdCityResp.getStandardCity());
          hasIssues = true;
        }
        if (stdCityResp.getSuggested() == null || stdCityResp.getSuggested().isEmpty()) {
          LOG.debug("County: " + stdCityResp.getStandardCountyCd() + " - " + stdCityResp.getStandardCountyName());
          details.append(("ZS01".equals(addr.getId().getAddrType()) ? "Install-at" : "Invoice-to") + " County: " + stdCityResp.getStandardCountyCd()
              + " - " + stdCityResp.getStandardCountyName() + "\n");
          if (addr.getCounty() == null || !addr.getCounty().equals(stdCityResp.getStandardCountyCd())) {
            overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "COUNTY", addr.getCounty(), stdCityResp.getStandardCountyCd());
          }
          if (addr.getCountyName() == null || !addr.getCountyName().trim().equalsIgnoreCase(stdCityResp.getStandardCountyName().trim())) {
            overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "COUNTY_NAME", addr.getCountyName(),
                stdCityResp.getStandardCountyName());
          }
        } else {
          if (!StringUtils.isBlank(addr.getCounty()) && !StringUtils.isBlank(addr.getCountyName())) {
            details
                .append("County cannot be determined. Multiple counties match the address. Using " + addr.getCounty() + " - " + addr.getCountyName()
                    + " for the " + ("ZS01".equals(addr.getId().getAddrType()) ? "Install-at" : "Invoice-to") + " Address record.\n");
          } else {
            details.append("County cannot be determined and no county data on the request. Multiple counties match the address. "
                + ("ZS01".equals(addr.getId().getAddrType()) ? "Install-at" : "Invoice-to") + " Address record needs to be checked.\n");
            hasIssues = true;
          }
        }
      } else {
        details.append("-System error when connecting to the standard city service-.\n");
        hasIssues = true;
      }

      details.append("\n");

    }
    if (hasIssues) {
      String msg = "City and/or County Name for one or more addresses cannot be determined.";
      engineData.addNegativeCheckStatus("_usstdcity", msg);
      details.append("\n").append(msg).append("\n");
    }

    results.setResults(hasIssues ? "City/County Issu" : "Standardized");
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);
    results.setOnError(false);

    return results;
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

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.GBL_ADDR_STD;
  }

  @Override
  public String getProcessDesc() {
    return "US-Address Standardization";
  }

  private TgmeCodes getTgmeCode(EntityManager entityManager, String code) {
    String sql = ExternalizedQuery.getSql("TGME.GETCODEDESC");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    query.setParameter("CD", code);
    List<TgmeCodes> results = query.getResults(TgmeCodes.class);
    if (!results.isEmpty()) {
      return results.get(0);
    }
    return null;
  }

}
