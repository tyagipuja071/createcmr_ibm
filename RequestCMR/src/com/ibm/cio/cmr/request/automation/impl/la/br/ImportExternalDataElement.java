package com.ibm.cio.cmr.request.automation.impl.la.br;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.geo.BrazilUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.util.geo.impl.LAHandler;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.la.br.MidasRequest;
import com.ibm.cmr.services.client.automation.la.br.MidasResponse;
import com.ibm.cmr.services.client.automation.la.br.SintegraResponse;

public class ImportExternalDataElement extends OverridingElement {

  private static final Logger log = Logger.getLogger(ImportExternalDataElement.class);

  public ImportExternalDataElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);
  }

  @Override
  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    long reqId = requestData.getAdmin().getId().getReqId();
    AutomationResult<OverrideOutput> results = buildResult(reqId);
    StringBuilder details = new StringBuilder();
    boolean callMIDAS = true;
    String installVat = null;
    String soldToVat = null;

    log.debug("Entering BRImportExternalDataElement()");
    try {
      ChangeLogListener.setManager(entityManager);
      String scenarioSubType = null;

      Data data = requestData.getData();
      Admin admin = requestData.getAdmin();

      String reqReason = admin.getReqReason();
      Addr zs01 = requestData.getAddress("ZS01");
      soldToVat = zs01 != null ? zs01.getVat() : null;

      if ("C".equals(admin.getReqType()) && data != null) {
        scenarioSubType = data.getCustSubGrp();
      } else if ("U".equals(admin.getReqType())) {
        scenarioSubType = LAHandler.getScenarioTypeForUpdateBRV2(entityManager, data, requestData.getAddresses());
      }

      if (scenarioSubType != null && StringUtils.isNotEmpty(scenarioSubType) && ("CROSS".equals(scenarioSubType) || "5COMP".equals(scenarioSubType)
          || "5PRIP".equals(scenarioSubType) || "PRIPE".equals(scenarioSubType) || "IBMEM".equals(scenarioSubType))) {

        details.append(LAHandler.getScenarioDescBR(scenarioSubType) + " scenario found on the request,hence skipping MIDAS import.\n");
        results.setResults("Skip MIDAS import");
        results.setDetails(details.toString());
        results.setOnError(false);

      } else if (scenarioSubType != null && StringUtils.isNotEmpty(scenarioSubType) && "SOFTL".equals(scenarioSubType)
          && !StringUtils.isEmpty(soldToVat) && (soldToVat.matches("0{14}") || soldToVat.matches("9{14}"))) {

        details.append(LAHandler.getScenarioDescBR(scenarioSubType)
            + " scenario found on the request and VAT contains all 0s or 9s, hence skipping MIDAS import.\n");
        results.setResults("Skip MIDAS import");
        results.setDetails(details.toString());
        results.setOnError(false);

      } else if (reqReason != null && StringUtils.isNotEmpty(reqReason) && "AUCO".equalsIgnoreCase(reqReason)) {

        details.append("Add/Update Contacts Only request reason found on the request so skipping MIDAS import.\n");
        results.setResults("Skip MIDAS import");
        results.setDetails(details.toString());
        results.setOnError(false);

      } else if (zs01 != null) {

        getMidasDetails(results, requestData, engineData, details, "ZS01", callMIDAS, scenarioSubType);

        if (scenarioSubType != null && StringUtils.isNotEmpty(scenarioSubType) && "LEASI".equalsIgnoreCase(scenarioSubType)) {

          if (requestData.getAddress("ZI01") != null) {
            installVat = requestData.getAddress("ZI01").getVat();
          }
          if (!StringUtils.isEmpty(soldToVat) && !StringUtils.isEmpty(installVat) && !soldToVat.equalsIgnoreCase(installVat)) {
            getMidasDetails(results, requestData, engineData, details, "ZI01", callMIDAS, scenarioSubType);
          } else if (!StringUtils.isEmpty(soldToVat) && !StringUtils.isEmpty(installVat) && soldToVat.equalsIgnoreCase(installVat)) {
            callMIDAS = false;
            getMidasDetails(results, requestData, engineData, details, "ZI01", callMIDAS, scenarioSubType);
          }
        }

        if ("INTER".equals(scenarioSubType)) {
          String mainCustNm = "";
          if (StringUtils.isNotBlank(zs01.getVat()) && engineData.get(zs01.getVat()) != null) {
            @SuppressWarnings("unchecked")
            AutomationResponse<MidasResponse> response = (AutomationResponse<MidasResponse>) engineData.get(zs01.getVat());
            if (response != null && response.isSuccess()) {
              mainCustNm = response.getRecord() != null ? " " + AutomationUtil.getCleanString(response.getRecord().getCompanyName()) + " " : "";
            }
          }
        }

      } else {
        if (requestData.getAddress("ZS01") == null) {
          details.append("Main address(ZS01) not found on the request,so skipping MIDAS import.\n");
          engineData.addRejectionComment("OTH", "Main address(ZS01) not found on the request.", "", "");
        } else if (StringUtils.isEmpty(scenarioSubType)) {
          details.append("Scenario can't be identified");
          engineData.addRejectionComment("OTH", "Scenario can't be identified.", "", "");
        }
        results.setResults("Skip MIDAS import");
        results.setDetails(details.toString());
        results.setOnError(true);
      }

      log.debug(details.toString());
      return results;

    } finally {
      ChangeLogListener.clearManager();
    }
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.BR_IMPORT;
  }

  @Override
  public String getProcessDesc() {
    return "Brazil - Import External Data";
  }

  @SuppressWarnings("unchecked")
  private void getMidasDetails(AutomationResult<OverrideOutput> results, RequestData requestData, AutomationEngineData engineData,
      StringBuilder details, String addrType, boolean callMIDAS, String scenarioSubType) throws Exception {

    Addr addr = requestData.getAddress(addrType);
    AutomationResponse<MidasResponse> response = null;

    if (callMIDAS) {
      AutomationServiceClient autoClient = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          AutomationServiceClient.class);
      autoClient.setReadTimeout(1000 * 60 * 5);
      autoClient.setRequestMethod(Method.Get);

      MidasRequest req = new MidasRequest();
      req.setCnpj(addr.getVat());

      log.debug("Connecting to the MIDAS Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      AutomationResponse<?> rawResponse = autoClient.executeAndWrap(AutomationServiceClient.BR_MIDAS_SERVICE_ID, req, AutomationResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);

      TypeReference<AutomationResponse<MidasResponse>> ref = new TypeReference<AutomationResponse<MidasResponse>>() {
      };
      response = mapper.readValue(json, ref);

      log.debug("MIDAS Service Response : " + json);

      if (response != null && response.isSuccess()) {
        configOverrideData(response, addrType, requestData, details, results, engineData, scenarioSubType);
      } else {
        results.setOnError(true);
        results.setResults("Execution Not Done.");
        results.setDetails(response.getMessage());
        log.debug("Error while connecting to MIDAS service " + response.getMessage());
        engineData.addRejectionComment("OTH", response.getMessage(), "", "");
      }

    } else {
      if (StringUtils.isNotBlank(addr.getVat()) && engineData.get(addr.getVat()) != null) {
        response = (AutomationResponse<MidasResponse>) engineData.get(addr.getVat());
        if (response != null) {
          log.debug("Skipping recall to MIDAS as sold to and Installing VAT value is same");
          configOverrideData(response, addrType, requestData, details, results, engineData, scenarioSubType);
        }
      }
    }
  }

  private void configOverrideData(AutomationResponse<MidasResponse> response, String addrType, RequestData requestData, StringBuilder details,
      AutomationResult<OverrideOutput> results, AutomationEngineData engineData, String scenarioSubType) {
    Addr addr = requestData.getAddress(addrType);
    OverrideOutput overrides = null;
    if (results.getProcessOutput() == null) {
      overrides = new OverrideOutput(false);
    } else {
      overrides = results.getProcessOutput();
    }
    RequestEntryModel model = requestData.createModelFromRequest();
    MidasResponse midasRecord = response.getRecord();

    String companyNm = midasRecord.getCompanyName();
    String abbrevNm = midasRecord.getCompanyName();
    String address = midasRecord.getAddress();
    String postalCd = midasRecord.getPostalCd();
    String city = midasRecord.getCity();
    String state = midasRecord.getState();
    String stateFiscalCode = midasRecord.getStateFiscalCode();
    String status = midasRecord.getStateFiscalCodeStatus();
    String observation = midasRecord.getStateFiscalCodeObservation();
    String name1 = null;
    String name2 = null;
    String icms = null;
    String code = null;
    String addrTxt1 = null;
    String addrTxt2 = null;

    if (scenarioSubType != null && !StringUtils.isEmpty(midasRecord.getAbbrevNm()) && !"***".equals(midasRecord.getAbbrevNm())
        && ("COMME".equals(scenarioSubType) || "GOVDI".equals(scenarioSubType) || "BUSPR".equals(scenarioSubType)
            || "GOVIN".equals(scenarioSubType))) {
      abbrevNm = midasRecord.getAbbrevNm();
    }

    log.debug(
        "Setting values for fields for Req_id : " + requestData.getAdmin().getId().getReqId() + " and Address Type =" + getAddressDesc(addrType));

    if ("ZS01".equals(addrType)) {

      if (!StringUtils.isEmpty(companyNm)) {
        if (companyNm.trim().length() > 30) {
          name1 = companyNm.substring(0, 30);
          name2 = ((companyNm.length() - 30 > 30) ? companyNm.substring(30, 60) : companyNm.substring(30));
        } else {
          name1 = companyNm;
          name2 = "";
        }
        details.append("Customer Name1  = " + name1 + "\n");
        overrides.addOverride(getProcessCode(), "ADMN", "MAIN_CUST_NM1", model.getMainCustNm1(), name1);
        if (name2 != null) {
          details.append("Customer Name2  = " + name2 + "\n");
          overrides.addOverride(getProcessCode(), "ADMN", "MAIN_CUST_NM2", model.getMainCustNm2(), name2);

        }
      }
    }

    if ("U".equals(requestData.getAdmin().getReqType()) && scenarioSubType != null
        && ("SOFTL".equals(scenarioSubType) || "CC3CC".equals(scenarioSubType))) {
      abbrevNm = null; // do not update abbrevNm for Softlayer and CC3CC
    }

    if (abbrevNm != null) {
      if ("ZI01".equals(addrType)) {
        String soldToAbbrevNm = getSoldToAbbrevName(requestData, engineData);
        abbrevNm = ((abbrevNm + " /" + soldToAbbrevNm).length() > 30 ? (abbrevNm + " /" + soldToAbbrevNm).substring(0, 30)
            : (abbrevNm + " /" + soldToAbbrevNm));
      } else {
        abbrevNm = ((!StringUtils.isEmpty(abbrevNm) && abbrevNm.length() > 30) ? abbrevNm.substring(0, 30) : abbrevNm);
      }
      details.append(getAddressDesc(addrType) + " Abbreviated Name = " + abbrevNm + "\n");

      overrides.addOverride(getProcessCode(), "DATA", "ABBREV_NM", model.getAbbrevNm(), abbrevNm);
    }

    if (address != null && !"".equals(address)) {
      if (address.length() > 30) {
        addrTxt1 = address.substring(0, 30);
        addrTxt2 = ((address.length() - 30 > 30) ? address.substring(30, 60) : address.substring(30));
      } else {
        addrTxt1 = address;
      }
      details.append(getAddressDesc(addrType) + " Address = " + address + "\n");
      overrides.addOverride(getProcessCode(), addrType, "ADDR_TXT", addr.getAddrTxt(), addrTxt1);
      if (addrTxt2 != null) {
        overrides.addOverride(getProcessCode(), addrType, "ADDR_TXT_2", addr.getAddrTxt(), addrTxt2);
      }
    }

    if (postalCd != null) {
      postalCd = ((!StringUtils.isEmpty(postalCd) && postalCd.length() > 10) ? postalCd.substring(0, 10) : postalCd);
      details.append(getAddressDesc(addrType) + " Postal Code = " + postalCd + "\n");
      overrides.addOverride(getProcessCode(), addrType, "POST_CD", addr.getPostCd(), postalCd);
    }

    if (state != null) {
      state = ((!StringUtils.isEmpty(state) && state.length() > 2) ? state.substring(0, 2) : state);
      details.append(getAddressDesc(addrType) + " State/Province = " + state + "\n");
      overrides.addOverride(getProcessCode(), addrType, "STATE_PROV", addr.getStateProv(), state);
    }

    if (city != null) {
      city = ((!StringUtils.isEmpty(city) && city.length() > 70) ? city.substring(0, 70) : city);
      details.append(getAddressDesc(addrType) + " City = " + city + "\n");
      overrides.addOverride(getProcessCode(), addrType, "CITY1", addr.getStateProv(), city);
    }

    if (StringUtils.isBlank(status)) {
      // try a second call to sintegra
      AutomationResponse<SintegraResponse> resp = null;
      SintegraResponse sintegraResponse = null;
      try {
        resp = BrazilUtil.querySintegra(addr.getVat(), state);
        if (response.isSuccess()) {
          sintegraResponse = resp.getRecord();
        } else {
          sintegraResponse = null;
        }
        if (sintegraResponse != null) {
          status = sintegraResponse.getStateFiscalCodeStatus();
          stateFiscalCode = sintegraResponse.getStateFiscalCode();
          observation = sintegraResponse.getStateFiscalCodeObservation();
          log.debug("State Fiscal Code retrieved from Sintegra for CNPJ " + addr.getVat() + ": " + stateFiscalCode + " (" + status + ")");
        } else {
          // status = "Unknown";
          details.append("Warning: State Fiscal Code cannot be determined from Sintegra.\n");
        }
      } catch (Exception e) {
        log.warn("Sintegra call still failed for CNPJ: " + addr.getVat() + ".", e);
        details.append("Warning: State Fiscal Code cannot be determined from Sintegra.\n");
      }
    }

    if (status != null) {
      if ("Habilitado".equalsIgnoreCase(status) || "Ativo".equalsIgnoreCase(status) || "habilitada".equalsIgnoreCase(status)
          || "Ativa".equalsIgnoreCase(status)) {
        if ("DF".equals(state) && observation != null && "NÃO CADASTRADO COMO CONTRIBUINTE ICMS".equalsIgnoreCase(observation)) {
          icms = "1";
        } else {
          icms = "2";
        }
        code = stateFiscalCode;
      } else {
        icms = "1";
        code = "ISENTO";
      }

      details.append(getAddressDesc(addrType) + " State Fiscal Code Status = " + status + "\n");
      if ("ZS01".equals(addrType)) {
        overrides.addOverride(getProcessCode(), "DATA", "ICMS_IND", model.getIcmsInd(), icms);
      }

      if (code != null) {
        String sfc = ((!StringUtils.isEmpty(code) && code.length() > 16) ? code.substring(0, 16) : code);
        details.append(getAddressDesc(addrType) + " State Fiscal Code= " + sfc + "\n");
        overrides.addOverride(getProcessCode(), addrType, "TAX_CD_1", addr.getTaxCd1(), sfc);

      }
    }

    if ("U".equals(requestData.getAdmin().getReqType()) && !"AUCO".equalsIgnoreCase(requestData.getAdmin().getReqReason())) {
      overrides.addOverride(getProcessCode(), addrType, "DPL_CHK_RESULT", addr.getDplChkResult(), " ");
    }

    engineData.put(addr.getVat(), response);

    results.setResults("Successful Execution");
    results.setOnError(false);
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);
  }

  private String getAddressDesc(String type) {
    String desc = null;
    if (!StringUtils.isEmpty(type)) {
      switch (type) {
      case "ZS01":
        desc = "Sold To";
        break;

      case "ZI01":
        desc = "Install At";
        break;
      }
    }
    return desc;
  }

  @SuppressWarnings("unchecked")
  private String getSoldToAbbrevName(RequestData requestData, AutomationEngineData engineData) {
    String soldToVat = null;
    String soldToAbbrevName = null;
    AutomationResponse<MidasResponse> responseSoldTo = null;
    soldToVat = requestData.getAddress("ZS01").getVat();
    if (engineData.get(soldToVat) != null && StringUtils.isNotBlank(soldToVat)) {
      responseSoldTo = (AutomationResponse<MidasResponse>) engineData.get(soldToVat);
    }
    soldToAbbrevName = responseSoldTo.getRecord().getCompanyName();
    log.debug("Sold To Abbrev name = " + soldToAbbrevName);
    return soldToAbbrevName;
  }
}
