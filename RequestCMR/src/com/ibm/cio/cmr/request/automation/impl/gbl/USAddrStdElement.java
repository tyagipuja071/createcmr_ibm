package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.ibm.cio.cmr.request.entity.Admin;
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
import com.ibm.cmr.services.client.stdcity.County;
import com.ibm.cmr.services.client.stdcity.StandardCityResponse;
import com.ibm.cmr.services.client.tgme.AddressStdData;
import com.ibm.cmr.services.client.tgme.AddressStdRequest;
import com.ibm.cmr.services.client.tgme.AddressStdResponse;

public class USAddrStdElement extends OverridingElement {

  private static final String DUMMY_COUNTRY = "XXX";

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
    Admin admin = requestData.getAdmin();

    if (!SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry())) {
      results.setResults("Standardized");
      results.setDetails("Skipped for non US request.\n");
      results.setProcessOutput(overrides);
      results.setOnError(false);
      return results;
    }

    LOG.debug("Connecting to the TGME (Addr Std) service...");
    TgmeClient tgmeClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), TgmeClient.class);
    StringBuilder details = new StringBuilder();

    List<Addr> a = requestData.getAddresses();
    boolean hasIssues = false;
    for (Addr addr : a) {

      // CREATCMR-5741 - remove addr std
      // this flow is not executed now, keeping for reference
      if (DUMMY_COUNTRY.equals(data.getCmrIssuingCntry())) {
        String key = addr.getId().getReqId() + "_" + addr.getId().getAddrType() + "_"
            + (addr.getId().getAddrSeq() != null ? addr.getId().getAddrSeq() : "");
        // Calling Address Std Service
        AddressStdRequest requestadd = new AddressStdRequest();
        requestadd.setSystemId("CreateCMR");
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
        if (SystemLocation.UNITED_STATES.equals(data.getCmrIssuingCntry()) && !"US".equals(addr.getLandCntry())) {
          details.append(" - Postal Code set to 00000 for Non-US landed country.\n");
          overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "POST_CD", addr.getPostCd(), "00000");
        } else if (!StringUtils.isBlank(tgmeData.getPostalCode())) {
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

        // if (!StringUtils.isBlank(tgmeData.getStateProvinceCode())) {
        // LOG.debug("State is : " + tgmeData.getStateProvinceCode());
        // details.append("State/Province Code: " +
        // tgmeData.getStateProvinceCode() + "\n");
        // if
        // (!tgmeData.getStateProvinceCode().toUpperCase().equals(addr.getStateProv().toUpperCase()))
        // {
        // // overrides.addOverride(getProcessCode(),
        // // addr.getId().getAddrType(), "STATE_PROV", addr.getStateProv(),
        // // data1.getStateProvinceCode());
        // }
        // }

        // if (!StringUtils.isBlank(data1.getStateProvinceCode())) {
        // LOG.debug("State Code determined from TGME: " +
        // data1.getStateProvinceCode());
        // overrides.addOverride(getProcessCode(), addr.getId().getAddrType(),
        // "", addr.getStateProv(), data1.getStateProvinceCode());
        // }
      }

      AddressModel addrModel = new AddressModel();
      PropertyUtils.copyProperties(addrModel, addr);
      addrModel.setCmrIssuingCntry(SystemLocation.UNITED_STATES);

      CmrClientService stdCity = new CmrClientService();
      ModelMap map = new ModelMap();
      stdCity.getStandardCity(addrModel, SystemLocation.UNITED_STATES, map);
      StandardCityResponse stdCityResp = (StandardCityResponse) map.get("result");
      String addrTypeName = addr.getId().getAddrType();
      switch (addrTypeName) {
      case "ZS01":
        details.append("Install-at ");
        break;
      case "PG01":
        details.append("PayGo Billing ");
        break;
      case "ZI01":
        details.append("Invoice-to ");
        break;
      }

      String cityToUse = addr.getCity1();
      String countyCodeToUse = addr.getCounty();

      if (stdCityResp != null) {
        if (stdCityResp.isCityMatched()) {
          LOG.debug("Standard City Name: " + stdCityResp.getStandardCity());
          details.append(" Standard City Name: " + stdCityResp.getStandardCity() + "\n");

          if (!addr.getCity1().trim().equalsIgnoreCase(stdCityResp.getStandardCity().trim())) {
            overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "CITY1", addr.getCity1(), stdCityResp.getStandardCity());
            cityToUse = stdCityResp.getStandardCity();
          }
        } else {
          List<String> cityNames = new ArrayList<String>();
          if (stdCityResp.getSuggested() != null && !stdCityResp.getSuggested().isEmpty()) {
            for (County county : stdCityResp.getSuggested()) {
              if (!cityNames.contains(county.getCity().toUpperCase().trim())) {
                cityNames.add(county.getCity().toUpperCase().trim());
              }
            }
          }
          if (cityNames.size() == 1) {
            details.append(" Standard City Name: " + cityNames.get(0) + "\n");
            if (!addr.getCity1().trim().equalsIgnoreCase(cityNames.get(0))) {
              overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "CITY1", addr.getCity1(), cityNames.get(0));
              cityToUse = cityNames.get(0);
            }
          } else {
            LOG.debug("Standard City Name cannot be determined. Found: " + stdCityResp.getStandardCity());
            details.append(" Standard City cannot be determined. Multiple City name suggestions found.\n");
            hasIssues = true;
          }
        }
        if (stdCityResp.getSuggested() == null || stdCityResp.getSuggested().isEmpty()) {
          LOG.debug("County: " + stdCityResp.getStandardCountyCd() + " - " + stdCityResp.getStandardCountyName());
          details.append(" " + "County: " + stdCityResp.getStandardCountyCd() + " - " + stdCityResp.getStandardCountyName() + "\n");
          if (addr.getCounty() == null || !addr.getCounty().equals(stdCityResp.getStandardCountyCd())) {
            overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "COUNTY", addr.getCounty(), stdCityResp.getStandardCountyCd());
            countyCodeToUse = stdCityResp.getStandardCountyCd();
          }
          if (addr.getCountyName() == null || (stdCityResp.getStandardCountyName() != null
              && !addr.getCountyName().trim().equalsIgnoreCase(stdCityResp.getStandardCountyName().trim()))) {
            overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "COUNTY_NAME", addr.getCountyName(),
                stdCityResp.getStandardCountyName());
          }
        } else {
          if (!StringUtils.isBlank(addr.getCounty()) && !StringUtils.isBlank(addr.getCountyName())) {
            details.append("County cannot be determined. Multiple counties match the address. Using " + addr.getCounty() + " - "
                + addr.getCountyName() + " for the " + " " + " Address record.\n");
          } else {
            if ("PG01".equals(addr.getId().getAddrType())
                || ("Y".equals(requestData.getAdmin().getPaygoProcessIndc()) && !"Y".equals(addr.getImportInd()))) {
              // this is a new address with paygo processing, assign first
              // county

              County first = stdCityResp.getSuggested().get(0);
              if (StringUtils.isBlank(addr.getCounty())) {
                overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "COUNTY", addr.getCounty(), first.getCode());
                countyCodeToUse = first.getCode();
              }
              if (StringUtils.isBlank(addr.getCountyName())) {
                overrides.addOverride(getProcessCode(), addr.getId().getAddrType(), "COUNTY_NAME", addr.getCountyName(), first.getName());
              }
              details.append(" " + "County (PayGo Assignment): " + first.getCode() + " - " + first.getName() + "\n");

            } else {
              details.append("County cannot be determined and no county data on the request. Multiple counties match the address. "
                  + " Address record needs to be checked.\n");
              hasIssues = true;
            }
          }
        }

        // CREATCMR-6342
        if ("ZS01".equals(addrTypeName)) {

          String landCntry = addr.getLandCntry();
          String stateProv = addr.getStateProv();

          String scc = resetForSCC(entityManager, landCntry, stateProv, cityToUse, countyCodeToUse);

          if (scc != "") {
            try {
              data.setCompanyNm(scc);
              entityManager.merge(data);
              entityManager.flush();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

        }
        // CREATCMR-6342

      } else {
        details.append("-System error when connecting to the standard city service-.\n");
        hasIssues = true;
      }

      if ("ZS01".equals(addr.getId().getAddrType())) {
        // only check SCC for ZS01 address
        boolean invalidScc = false;
        if ("897".equals(requestData.getData().getCmrIssuingCntry())) {
          String setPPNFlag = validateForSCC(entityManager, addr.getLandCntry(), addr.getStateProv(), cityToUse, countyCodeToUse);
          if ("N".equals(setPPNFlag)) {
            invalidScc = true;
          }
        }

        if (invalidScc) {
          hasIssues = true;
        }
      }

      details.append("\n");

    }
    if (hasIssues) {
      String msg = "City and/or County Name/or SCC for one or more addresses cannot be determined.";
      engineData.addNegativeCheckStatus("_usstdcity", msg);
      details.append("\n").append(msg).append("\n");
    }

    results.setResults(hasIssues ? "City/County Issu" : "Validated");
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

  public static String validateForSCC(EntityManager entityManager, String landCntry, String stateProv, String city1, String county) {

    String flag = "N";

    if (StringUtils.isBlank(county) || !StringUtils.isNumeric(county)) {
      county = "0";
    }

    if ("US".equals(landCntry)) {
      String sql2 = ExternalizedQuery.getSql("QUERY.US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY");
      PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
      query2.setParameter("LAND_CNTRY", landCntry);
      query2.setParameter("N_ST", stateProv);
      query2.setParameter("C_CNTY", county);
      query2.setParameter("N_CITY", city1.toUpperCase());

      List<Object[]> results2 = query2.getResults();
      if (results2 != null && !results2.isEmpty()) {
        Object[] result = results2.get(0);
        if (!"".equals(result[0])) {
          flag = "Y";
        }
      }
    } else {
      String sql2 = ExternalizedQuery.getSql("QUERY.US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY_NON_US");
      PreparedQuery query2 = new PreparedQuery(entityManager, sql2);
      query2.setParameter("LAND_CNTRY", landCntry);
      query2.setParameter("N_CITY", city1);

      List<Object[]> results2 = query2.getResults();
      if (results2 != null && !results2.isEmpty()) {
        Object[] result = results2.get(0);
        if (!"".equals(result[0])) {
          flag = "Y";
        }
      }
    }

    return flag;

  }

  // CREATCMR-6342
  private String resetForSCC(EntityManager entityManager, String landCntry, String stateProv, String city1, String county) {
    String scc = "";

    if (StringUtils.isBlank(county) || !StringUtils.isNumeric(county)) {
      county = "0";
    }

    String sql = ExternalizedQuery.getSql("QUERY.US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("LAND_CNTRY", landCntry);
    query.setParameter("N_ST", stateProv);
    query.setParameter("C_CNTY", county);
    query.setParameter("N_CITY", city1.toUpperCase());

    List<Object[]> results = query.getResults();
    if (results != null && !results.isEmpty()) {
      Object[] result = results.get(0);
      if (!"".equals(result[0])) {
        scc = (String) result[0];
      }
    }

    return scc;
  }
  // CREATCMR-6342

}