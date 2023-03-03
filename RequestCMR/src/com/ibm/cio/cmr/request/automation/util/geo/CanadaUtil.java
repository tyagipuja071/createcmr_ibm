package com.ibm.cio.cmr.request.automation.util.geo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.CalculateCoverageElement;
import com.ibm.cio.cmr.request.automation.impl.gbl.RetrieveIBMValuesElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.CoverageContainer;
import com.ibm.cio.cmr.request.automation.util.DummyServletRequest;
import com.ibm.cio.cmr.request.automation.util.RequestChangeContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.CustScenarios;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.model.window.UpdatedNameAddrModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.CmrClientService;
import com.ibm.cio.cmr.request.service.requestentry.AddressService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.dnb.DnBUtil;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

/**
 * 
 * @author clint
 *
 */

public class CanadaUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(CanadaUtil.class);
  private static List<DeSortlMapping> sortlMappings = new ArrayList<DeSortlMapping>();
  private static final String MATCHING = "matching";
  private static final String POSTAL_CD_RANGE = "postalCdRange";
  private static final String SORTL = "SORTL";
  private static final String SCENARIO_LOC_INTERNAL = "INTER";

  private static final String SCENARIO_COMMERCIAL = "COMME";
  private static final String SCENARIO_BUSINESS_PARTNER = "BUSP";
  private static final String SCENARIO_PRIVATE_HOUSEHOLD = "PRIV";
  private static final String SCENARIO_INTERNAL = "INTER";
  private static final String SCENARIO_OEM = "OEM";
  private static final String SCENARIO_STRATEGIC_OUTSOURCING = "SOCUS";
  private static final String SCENARIO_GOVERNMENT = "GOVT";
  private static final String SCENARIO_KYNDRYL = "KYND";
  private static final String SCENARIO_CROSS_BORDER_USA = "USA";
  private static final String SCENARIO_CROSS_BORDER_CARIB = "CND";

  private static final List<String> RELEVANT_ADDRESSES = Arrays.asList(CmrConstants.RDC_SOLD_TO, CmrConstants.RDC_BILL_TO,
      CmrConstants.RDC_INSTALL_AT, CmrConstants.RDC_SHIP_TO, "ZP02", "ZP03", "ZP04", "ZP05", "ZP06", "ZP07", "ZP08", "ZP09");

  private static final List<String> NON_RELEVANT_ADDRESS_FIELDS = Arrays.asList("Building", "Floor", "Office", "Department / Attn.", "Street con't",
      "Customer Name 2", "Phone #", "PostBox", "State/Province", "Transport Zone");

  private static final List<String> CARIB_CNTRIES = Arrays.asList("BS", "BB", "BM", "GY", "KY", "JM", "AW", "LC", "SR", "TT");

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> results, StringBuilder details, ValidationOutput output) {
    Data data = requestData.getData();
    Addr zs01 = requestData.getAddress("ZS01");
    boolean valid = true;
    String scenario = data.getCustSubGrp();

    if (StringUtils.isBlank(scenario)) {
      details.append("Scenario not correctly specified on the request");
      engineData.addNegativeCheckStatus("_atNoScenario", "Scenario not correctly specified on the request");
      return true;
    }
    LOG.info("Starting scenario validations for Request ID " + data.getId().getReqId());
    LOG.debug("Scenario to check: " + scenario);

    // if ("C".equals(requestData.getAdmin().getReqType())) {
    // // remove duplicates
    // removeDuplicateAddresses(entityManager, requestData, details);
    // }
    // engineData.setMatchDepartment(true);

    if (StringUtils.isNotBlank(scenario)) {
      switch (scenario) {
      case SCENARIO_COMMERCIAL:
      case SCENARIO_PRIVATE_HOUSEHOLD:
      case SCENARIO_INTERNAL:
      case SCENARIO_OEM:
      case SCENARIO_STRATEGIC_OUTSOURCING:
        if (!"CA".equalsIgnoreCase(zs01.getLandCntry())) {
          valid = false;
          engineData.addRejectionComment("LAND", "Invalid Landed Country.", "Landed country is not Canada", "");
          details.append("Landed Country is not Canada").append("\n");
        }
        break;
      case SCENARIO_GOVERNMENT:
        if (!"CA".equalsIgnoreCase(zs01.getLandCntry())) {
          valid = false;
          engineData.addRejectionComment("LAND", "Invalid Landed Country.", "Landed country is not Canada", "");
          details.append("Landed Country is not Canada").append("\n");
        }
        engineData.addNegativeCheckStatus("_caGovt", "Government/Public request needs further validation.");
        details.append("Government/Public request needs further validation.").append("\n");
        break;
      case SCENARIO_CROSS_BORDER_USA:
        if ("CA".equalsIgnoreCase(zs01.getLandCntry()) || CARIB_CNTRIES.contains(zs01.getLandCntry())) {
          valid = false;
          engineData.addRejectionComment("LAND", "Invalid Landed Country.", "Landed country contains Canada or a US Territory", "");
          details.append("Landed country contains Canada or a US Territory").append("\n");
        }
        break;
      case SCENARIO_CROSS_BORDER_CARIB:
        if (!CARIB_CNTRIES.contains(zs01.getLandCntry())) {
          valid = false;
          engineData.addRejectionComment("LAND", "Invalid Landed Country.", "Landed country is not a Carribean Country", "");
          details.append("Landed country is not a Carribean Country").append("\n");
        }
        break;
      case SCENARIO_BUSINESS_PARTNER:
        if (!"CA".equalsIgnoreCase(zs01.getLandCntry())) {
          valid = false;
          engineData.addRejectionComment("LAND", "Invalid Landed Country.", "Landed country is not Canada", "");
          details.append("Landed Country is not Canada").append("\n");
        }
        return doBusinessPartnerChecks(engineData, data.getPpsceid(), details);
      }
    } else {
      valid = false;
      engineData.addRejectionComment("TYPR", "Wrong type of request.", "No Scenario found on the request", "");
      details.append("No Scenario found on the request").append("\n");
    }
    return valid;
  }

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {

    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    Addr soldTo = requestData.getAddress("ZS01");
    String custSubGrp = data.getCustSubGrp();
    String sql = ExternalizedQuery.getSql("CA.QUERY.GET.CUSTSCENARIOS_BY_CUSTTYP");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CUST_TYP", custSubGrp);
    List<CustScenarios> custScenarioList = query.getResults(CustScenarios.class);
    boolean isFieldCompSuccessful = true;

    if ("C".equals(admin.getReqType()) && data != null) {
      String scenarioSubType = data.getCustSubGrp();
      if (SCENARIO_KYNDRYL.equals(scenarioSubType)) {
        AddressModel addrCopy = new AddressModel();
        Addr invoiceTo = requestData.getAddress(CmrConstants.ADDR_TYPE.ZP02.toString(), "00002");
        if (invoiceTo != null) {
          AddressService addrService = new AddressService();
          AddressModel addrRemModel = new AddressModel();
          addrService.copyValuesFromEntity(invoiceTo, addrCopy);
          addrService.copyValuesFromEntity(invoiceTo, addrRemModel);
          addrRemModel.setAction("REMOVE_ADDRESS");
          addrRemModel.setCmrIssuingCntry(data.getCmrIssuingCntry());
          try {
            AppUser user = new AppUser();
            user.setIntranetId(requestData.getAdmin().getRequesterId());
            user.setBluePagesName(requestData.getAdmin().getRequesterNm());
            DummyServletRequest dummyReq = new DummyServletRequest();
            if (dummyReq.getSession() != null) {
              LOG.trace("Session found for dummy req");
              dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
            } else {
              LOG.warn("Session not found for dummy req");
            }
            addrService.performTransaction(addrRemModel, entityManager, dummyReq);
          } catch (Exception e) {
            LOG.error("An error occurred while removing main invoice-to", e);
          }
          entityManager.flush();
        }

        String addrSeq = "00002";
        String divn = "";
        String dept = "";
        String address = "3600 STEELES AVE E";
        String address2 = "";
        String city = "MARKHAM";
        String state = "ON";
        String postCd = "L3R 9Z7";
        String landCntry = "CA";

        LOG.debug("Adding invoice-to address for Kyndryl scenario..");
        AddressService addrService = new AddressService();
        AddressModel addrModel = new AddressModel();
        addrModel.setReqId(data.getId().getReqId());
        addrModel.setAddrSeq(addrSeq);
        addrModel.setDivn(divn);
        addrModel.setDept(dept);
        addrModel.setLandCntry(landCntry);
        addrModel.setAddrTxt(address);
        addrModel.setAddrTxt2(address2);
        addrModel.setCity1(city);
        addrModel.setStateProv(state);
        addrModel.setPostCd(postCd);
        addrModel.setState(BaseModel.STATE_NEW);
        addrModel.setAction("ADD_ADDRESS");
        addrModel.setAddrType(CmrConstants.ADDR_TYPE.ZP02.toString());
        addrModel.setCmrIssuingCntry(data.getCmrIssuingCntry());

        try {
          AppUser user = new AppUser();
          user.setIntranetId(requestData.getAdmin().getRequesterId());
          user.setBluePagesName(requestData.getAdmin().getRequesterNm());
          DummyServletRequest dummyReq = new DummyServletRequest();
          if (dummyReq.getSession() != null) {
            LOG.trace("Session found for dummy req");
            dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
          } else {
            LOG.warn("Session not found for dummy req");
          }
          addrService.performTransaction(addrModel, entityManager, dummyReq);
        } catch (Exception e) {
          LOG.error("An error occurred while adding ZP01 address", e);
        }
        entityManager.flush();

        if (invoiceTo != null) {
          LOG.debug("Re-adding invoice-to address..");
          addrCopy.setState(BaseModel.STATE_NEW);
          addrCopy.setAction("ADD_ADDRESS");
          addrCopy.setAddrType(CmrConstants.ADDR_TYPE.ZP02.toString());
          addrCopy.setCmrIssuingCntry(data.getCmrIssuingCntry());

          try {
            AppUser user = new AppUser();
            user.setIntranetId(requestData.getAdmin().getRequesterId());
            user.setBluePagesName(requestData.getAdmin().getRequesterNm());
            DummyServletRequest dummyReq = new DummyServletRequest();
            if (dummyReq.getSession() != null) {
              LOG.trace("Session found for dummy req");
              dummyReq.getSession().setAttribute(CmrConstants.SESSION_APPUSER_KEY, user);
            } else {
              LOG.warn("Session not found for dummy req");
            }
            addrService.performTransaction(addrCopy, entityManager, dummyReq);
          } catch (Exception e) {
            LOG.error("An error occurred while adding cloned invoice-to address", e);
          }
          entityManager.flush();
        }
      }
    }

    if (custScenarioList != null && custScenarioList.size() > 0) {
      for (CustScenarios custScenario : custScenarioList) {
        if (StringUtils.isNotEmpty(custScenario.getFieldName())) {
          String scenariofieldValue = custScenario.getValue();
          // Sales Branch Office
          if (custScenario.getFieldName().equals("salesBusOffCd")) {
            String dataSalesBusOffCd = data.getSalesBusOffCd();
            if (StringUtils.isNotBlank(scenariofieldValue) && !dataSalesBusOffCd.equals(scenariofieldValue)) {
              details.append("Setting Sales Branch Office to ").append(scenariofieldValue).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_BO_CD", dataSalesBusOffCd, scenariofieldValue);
              data.setSalesBusOffCd(scenariofieldValue);
            }
          }
          // Marketing Rep
          else if (custScenario.getFieldName().equals("repTeamMemberNo")) {
            String dataRepTeamMemberNo = data.getRepTeamMemberNo();
            if (StringUtils.isNotBlank(scenariofieldValue) && !dataRepTeamMemberNo.equals(scenariofieldValue)) {
              details.append("Setting Mktg Rep to ").append(scenariofieldValue).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "REP_TEAM_MEMBER_NO", dataRepTeamMemberNo,
                  scenariofieldValue);
            }
          }
          // CS Branch
          else if (custScenario.getFieldName().equals("salesTeamCd")) {
            String dataSalesTeamCd = data.getSalesTeamCd();
            if (StringUtils.isNotBlank(scenariofieldValue) && !dataSalesTeamCd.equals(scenariofieldValue)) {
              details.append("Setting CS Branch to ").append(scenariofieldValue).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_TEAM_CD", dataSalesTeamCd, scenariofieldValue);
            } else if (StringUtils.isBlank(dataSalesTeamCd)) {
              if (soldTo != null && StringUtils.isNotBlank(soldTo.getPostCd()) && soldTo.getPostCd().length() >= 3) {
                String computedCsBranch = soldTo.getPostCd().substring(0, 3);
                details.append("Setting computed CS Branch to").append(computedCsBranch).append("\n");
                overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_TEAM_CD", dataSalesTeamCd, computedCsBranch);
              } else if (soldTo == null) {
                isFieldCompSuccessful = false;
                details.append("No Sold-To Address. Cannot compute CS Branch").append("\n");
              }
            }
          }
          // AR-FAAR
          else if (custScenario.getFieldName().equals("adminDeptCd")) {
            String dataAdminDeptCd = data.getAdminDeptCd();
            String sbo = data.getSalesBusOffCd();
            if (StringUtils.isNotBlank(scenariofieldValue) && !dataAdminDeptCd.equals(scenariofieldValue)) {
              details.append("Setting AR-FAAR to ").append(scenariofieldValue).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ADMIN_DEPT_CD", dataAdminDeptCd, scenariofieldValue);
            }
          }
          // Credit Code creditCd
          else if (custScenario.getFieldName().equals("creditCd")) {
            String dataCreditCd = data.getCreditCd();
            if (StringUtils.isNotBlank(scenariofieldValue) && !dataCreditCd.equals(scenariofieldValue)) {
              details.append("Setting Credit Code to ").append(scenariofieldValue).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CREDIT_CD", dataCreditCd, scenariofieldValue);
            }
          }
          // Pref Lang
          else if (custScenario.getFieldName().equals("custPrefLang")) {
            String dataCustPrefLang = data.getCustPrefLang();
            if (soldTo != null && "QC".equals(soldTo.getStateProv())) {
              details.append("Setting Preferred Language to French").append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CUST_PREF_LANG", dataCustPrefLang, "F");
              data.setCustPrefLang("F");
            } else if (StringUtils.isNotBlank(scenariofieldValue) && !dataCustPrefLang.equals(scenariofieldValue)) {
              details.append("Setting Preferred Language to ").append(scenariofieldValue).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CUST_PREF_LANG", dataCustPrefLang, scenariofieldValue);
            }
          }
          // Tax Code/Estab Function Code (EFC)
          else if (custScenario.getFieldName().equals("taxCd1")) {
            String dataTaxCd1 = data.getTaxCd1();
            if (StringUtils.isBlank(dataTaxCd1)
                || (StringUtils.isNotBlank(dataTaxCd1) && StringUtils.isNotBlank(scenariofieldValue) && !dataTaxCd1.equals(scenariofieldValue))) {
              details.append("Setting Tax Code/EFC to ").append(scenariofieldValue).append("\n");
              overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "TAX_CD1", dataTaxCd1, scenariofieldValue);
            }
          }
        }
      }
    }
    // Pref Lang - ensure French if sold-to prov is Quebec
    if (soldTo != null && "QC".equals(soldTo.getStateProv()) && !"F".equals(data.getCustPrefLang())) {
      details.append("Setting Preferred Language to French").append("\n");
      overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CUST_PREF_LANG", data.getCustPrefLang(), "F");
    }
    // Location Code
    if (soldTo != null) {
      Map<String, String> caLocationNumber = new HashMap<String, String>();
      caLocationNumber.put("AB", "01999");
      caLocationNumber.put("BC", "02999");
      caLocationNumber.put("MB", "03999");
      caLocationNumber.put("NB", "04999");
      caLocationNumber.put("NF", "05999");
      caLocationNumber.put("NL", "05999");
      caLocationNumber.put("NT", "06999");
      caLocationNumber.put("NS", "07999");
      caLocationNumber.put("NU", "13999");
      caLocationNumber.put("ON", "08999");
      caLocationNumber.put("PE", "09999");
      caLocationNumber.put("QC", "10999");
      caLocationNumber.put("SK", "11999");
      caLocationNumber.put("YT", "12999");

      List<String> caribNorthDistCntries = Arrays.asList("AG", "AI", "AW", "BS", "BB", "BM", "BQ", "BV", "CW", "DM", "DO", "GD", "GP", "GY", "HT",
          "KN", "KY", "JM", "LC", "MQ", "MS", "PR", "SR", "SX", "TC", "TT", "VC", "VG");

      String dataLocationNumber = data.getLocationNumber();
      String computedLocNumber = null;
      if ("CA".equals(soldTo.getLandCntry()) && !("USA".equals(data.getCustSubGrp()) || "CND".equals(data.getCustSubGrp()))) {
        computedLocNumber = caLocationNumber.get(soldTo.getStateProv());
      } else if (caribNorthDistCntries.contains(soldTo.getLandCntry())) {
        computedLocNumber = soldTo.getLandCntry() + "000";
      } else if ("USA".equals(data.getCustSubGrp())) {
        computedLocNumber = "99999";
      }

      if (StringUtils.isNotEmpty(computedLocNumber) && (StringUtils.isBlank(dataLocationNumber)
          || (StringUtils.isNotBlank(dataLocationNumber) && !dataLocationNumber.equals(computedLocNumber)))) {
        details.append("Setting Location Code to ").append(computedLocNumber).append("\n");
        overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "LOCN_NO", dataLocationNumber, computedLocNumber);
      } else if (StringUtils.isEmpty(computedLocNumber)) {
        details.append("Cannot compute Location Code\n");
        isFieldCompSuccessful = false;
      }
    } else {
      // error computation of Location Number
      isFieldCompSuccessful = false;
      String msg = "No Sold-To Address. Cannot compute Location Code.\n";
      details.append(msg);
      engineData.addNegativeCheckStatus("chLocNo", msg);
    }

    if (isFieldCompSuccessful) {
      results.setResults("Computed");
    } else {
      results.setResults("Cannot compute all fields");
      results.setOnError(true);
    }
    results.setDetails(details.toString());
    results.setProcessOutput(overrides);

    return results;
  }

  private HashMap<String, String> getSORTLFromPostalCodeMapping(String subIndustryCd, String postCd, String isuCd, String clientTier) {
    HashMap<String, String> response = new HashMap<String, String>();
    response.put(MATCHING, "");
    response.put(POSTAL_CD_RANGE, "");
    response.put(SORTL, "");
    if (!sortlMappings.isEmpty()) {
      int postalCd = Integer.parseInt(postCd);
      int distance = 1000;
      String nearbySortl = null;
      String nearbyPostalCdRange = null;
      for (DeSortlMapping mapping : sortlMappings) {
        List<String> subIndustryCds = Arrays.asList(mapping.getSubIndustryCds().replaceAll("\n", "").replaceAll(" ", "").split(","));
        if (subIndustryCds.contains(subIndustryCd) && isuCd.equals(mapping.getIsu()) && clientTier.equals(mapping.getCtc())) {
          if (StringUtils.isNotBlank(mapping.getPostalCdRanges())) {
            String[] postalCodeRanges = mapping.getPostalCdRanges().replaceAll("\n", "").replaceAll(" ", "").split(",");
            for (String postalCdRange : postalCodeRanges) {
              String[] range = postalCdRange.split("to");
              int start = 0;
              int end = 0;
              if (range.length == 2) {
                start = Integer.parseInt(range[0]);
                end = Integer.parseInt(range[1]);
              } else if (range.length == 1) {
                start = Integer.parseInt(range[0].replaceAll("x", "0"));
                end = Integer.parseInt(range[0].replaceAll("x", "9"));
              }
              String postalCodeRange = start + " to " + end;
              if (postalCd >= start && postalCd <= end) {
                response.put(MATCHING, "Exact Match");
                response.put(SORTL, mapping.getSortl());
                response.put(POSTAL_CD_RANGE, postalCodeRange);
                return response;
              } else if (postalCd > end) {
                int diff = postalCd - end;
                if (diff > 0 && diff < distance) {
                  distance = diff;
                  nearbySortl = mapping.getSortl();
                  nearbyPostalCdRange = postalCodeRange;
                }
              } else if (postalCd < start) {
                int diff = start - postalCd;
                if (diff > 0 && diff < distance) {
                  distance = diff;
                  nearbySortl = mapping.getSortl();
                  nearbyPostalCdRange = postalCodeRange;
                }
              }
            }
          } else {
            response.put(MATCHING, "Exact Match");
            response.put(SORTL, mapping.getSortl());
            response.put(POSTAL_CD_RANGE, "- No Postal Code Range Defined -");
            return response;
          }
        }
      }
      if (StringUtils.isNotBlank(nearbySortl)) {
        response.put(MATCHING, "Nearest Match");
        LOG.debug("SORTL Calculated by near by postal code range logic: " + nearbySortl);
      } else {
        response.put(MATCHING, "No Match Found");
      }
      response.put(SORTL, nearbySortl);
      response.put(POSTAL_CD_RANGE, nearbyPostalCdRange);
      return response;
    } else {
      response.put(MATCHING, "No Match Found");
      return response;
    }
  }

  @Override
  public boolean performCountrySpecificCoverageCalculations(CalculateCoverageElement covElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covFrom, CoverageContainer container, boolean isCoverageCalculated) throws Exception {

    if (!"C".equals(requestData.getAdmin().getReqType())) {
      details.append("Coverage Calculation skipped for Updates.");
      results.setResults("Skipped");
      results.setDetails(details.toString());
      return true;
    }

    Data data = requestData.getData();
    String scenario = data.getCustSubGrp();

    if (scenario.equals(SCENARIO_LOC_INTERNAL)) {
      details.append("Setting SBO to 922 for Local-Internal Scenario");
      overrides.addOverride(covElement.getProcessCode(), "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), "922");
      engineData.addPositiveCheckStatus(AutomationEngineData.COVERAGE_CALCULATED);
      results.setResults("Calculated");
    }

    if (isCoverageCalculated) {
      String coverageId = container.getFinalCoverage();
      // ISU CTC Based on Coverage
      if (StringUtils.isNotBlank(coverageId)) {
        String isu = "";
        String ctc = "";

        String firstChar = coverageId.substring(0, 1);

        List<String> ECOSYSTEM_LIST = Arrays.asList("T0007992", "T0007993", "T0007994", "T0008059");

        if (("T").equalsIgnoreCase(firstChar) && !ECOSYSTEM_LIST.contains(coverageId)) {
          isu = "34";
          ctc = "Q";
          setISUCTCBasedOnCoverage(details, overrides, coverageId, data, isu, ctc);
        } else if (ECOSYSTEM_LIST.contains(coverageId)) {
          isu = "34";
          ctc = "Y";
          setISUCTCBasedOnCoverage(details, overrides, coverageId, data, isu, ctc);
        } else if (("A").equalsIgnoreCase(firstChar) || ("I").equalsIgnoreCase(firstChar)) {
          isu = ""; // apply logic to set isu based on sub industry code
          ctc = "";
          String subIndustryCd = data != null && data.getSubIndustryCd() != null ? data.getSubIndustryCd() : "";
          String firstCharSubIndustry = StringUtils.isNotEmpty(subIndustryCd) ? subIndustryCd.substring(0, 1) : "";

          Map<String, String> industryCodeISUMap = new HashMap<String, String>();

          industryCodeISUMap.put("A", "3T");
          industryCodeISUMap.put("U", "12");
          industryCodeISUMap.put("K", "05");
          industryCodeISUMap.put("R", "1R");
          industryCodeISUMap.put("D", "18");

          industryCodeISUMap.put("W", "18");
          industryCodeISUMap.put("T", "19");
          industryCodeISUMap.put("F", "04");
          industryCodeISUMap.put("S", "4F");
          industryCodeISUMap.put("N", "31");

          industryCodeISUMap.put("J", "4A");
          industryCodeISUMap.put("V", "14");
          industryCodeISUMap.put("L", "5E");
          industryCodeISUMap.put("P", "15");
          industryCodeISUMap.put("M", "4D");

          industryCodeISUMap.put("Y", "28");
          industryCodeISUMap.put("G", "28");
          industryCodeISUMap.put("E", "40");
          industryCodeISUMap.put("H", "11");
          industryCodeISUMap.put("X", "8C");

          industryCodeISUMap.put("B", "5B");
          industryCodeISUMap.put("C", "5B");

          if (industryCodeISUMap.containsKey(firstCharSubIndustry)) {
            isu = industryCodeISUMap.get(firstCharSubIndustry);
          }
          setISUCTCBasedOnCoverage(details, overrides, coverageId, data, isu, ctc);
        }
      }
      // Compute SBO based on Coverage if blank
      if (StringUtils.isNotBlank(coverageId) && StringUtils.isBlank(data.getSalesBusOffCd())) {
        details.append("Calculating SBO based on Coverage: ").append(coverageId).append("\n");
        String sbo = getSbrFromCoverageId(coverageId, entityManager);
        if (StringUtils.isNotBlank(sbo)) {
          details.append("Setting SBO based on Coverage ").append(coverageId).append(" to ").append(sbo).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);
          // set Install Branch Office if blank
          if (StringUtils.isBlank(data.getInstallBranchOff())) {
            details.append("Setting IBO based on Coverage ").append(coverageId).append(" to ").append(sbo).append("\n");
            overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sbo);
          }
        } else {
          details.append("Cannot derive SBO from coverage");
          LOG.debug("Cannot derive SBO from coverage - using default sbo");
          LOG.debug("Final Coverage: " + coverageId);
          if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0007992")) {
            sbo = "458";
            setDefaultSBO(details, overrides, coverageId, data, sbo);
          } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0008059")) {
            sbo = "570";
            setDefaultSBO(details, overrides, coverageId, data, sbo);
          } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000604")) {
            sbo = "486";
            setDefaultSBO(details, overrides, coverageId, data, sbo);
          } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000549")) {
            sbo = "481";
            setDefaultSBO(details, overrides, coverageId, data, sbo);
          } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000595")) {
            sbo = "457";
            setDefaultSBO(details, overrides, coverageId, data, sbo);
          } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000597")) {
            sbo = "460";
            setDefaultSBO(details, overrides, coverageId, data, sbo);
          }
        }
        // set AR-FAAR after sbo computation
        String arfaar = getArfarrBySbo(sbo, entityManager);
        if (StringUtils.isNotBlank(arfaar)) {
          details.append("Setting AR-FAAR based on SBO ").append(sbo).append(" to ").append(arfaar).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ADMIN_DEPT_CD", data.getAdminDeptCd(), arfaar);
        } else if (StringUtils.isBlank(arfaar) && StringUtils.isNotBlank(data.getCustSubGrp()) && !"KYND".equals(data.getCustSubGrp())) {
          String defaultArfaar = "120V";
          details.append("Setting AR-FAAR based on SBO ").append(sbo).append(" to default ").append(defaultArfaar).append("\n");
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ADMIN_DEPT_CD", data.getAdminDeptCd(), defaultArfaar);
        }
      }
    }

    return true;
  }

  @Override
  public boolean runUpdateChecksForData(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    List<Addr> addresses = null;
    addresses = requestData.getAddresses();

    boolean isVatDocRequired = false;

    for (Addr addr : addresses) {
      String stateProv = addr.getStateProv();
      List<String> STATES_TO_VALIDATE_TAXDOC = Arrays.asList("MC", "MD", "SK");
      if (STATES_TO_VALIDATE_TAXDOC.contains(stateProv)) {
        isVatDocRequired = true;
      }
    }

    // if (handlePrivatePersonRecord(entityManager, admin, output, validation,
    // engineData)) {
    // return true;
    // }
    String sqlKey = ExternalizedQuery.getSql("AUTO.US.CHECK_CMDE");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("EMAIL", admin.getRequesterId());
    query.setForReadOnly(true);
    if (query.exists()) {
      // skip checks if requester is from Canada CMDE team
      admin.setScenarioVerifiedIndc("Y");
      LOG.debug("Requester is from CA CMDE team, skipping update checks.");
      output.setDetails("Requester is from CA CMDE team, skipping update checks.\n");
      validation.setMessage("Skipped");
      validation.setSuccess(true);
    } else {
      StringBuilder details = new StringBuilder();
      boolean cmdeReview = false;
      EntityManager cedpManager = JpaManager.getEntityManager("CEDP");
      List<String> ignoredUpdates = new ArrayList<String>();
      boolean isicCheckDone = false;
      for (UpdatedDataModel change : changes.getDataUpdates()) {
        switch (change.getDataField()) {
        case "Order Block Code":
          if ("94".equals(change.getOldData()) || "94".equals(change.getNewData())) {
            cmdeReview = true;
          }
          break;
        case "ISIC":
        case "Subindustry":
          if (!isicCheckDone) {
            String error = "This CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle. Thank you. \nJira link https://jsw.ibm.com/projects/CMDE/summary";
            if (StringUtils.isNotBlank(error)) {
              LOG.debug(error);
              output.setDetails(error);
              validation.setMessage("Validation Failed");
              validation.setSuccess(false);
              if (StringUtils.isBlank(admin.getSourceSystId())) {
                engineData.addRejectionComment("OTH", error, "", "");
                output.setOnError(true);
              } else {
                engineData.addNegativeCheckStatus("BP_" + change.getDataField(), error);
              }
              return true;
            }
            isicCheckDone = true;
          }
          break;
        case "NAT/INAC":
          details.append(
              "\nUpdate of NAC/INAC should be done via JIRA. Please submit the request in JIRA.\nLink:- https://jsw.ibm.com/projects/CMDE/summary");
          engineData.addRejectionComment("NAC",
              "Update of NAC/INAT should be done via JIRA. Please submit the request in JIRA. \nLink:- https://jsw.ibm.com/projects/CMDE/summary", "",
              "");
          output.setOnError(true);
          // String error = performInacCheck(cedpManager, entityManager,
          // requestData);
          // if (StringUtils.isNotBlank(error)) {
          // if ("BG_ERROR".equals(error)) {
          // cmdeReview = true;
          // engineData.addNegativeCheckStatus("_chINACCheckFailed",
          // "The projected global buying group during INAC checks did not match
          // the one on the request.");
          // details.append("The projected global buying group during INAC
          // checks did not match the one on the request.\n");
          // } else {
          // LOG.debug(error);
          // output.setDetails(error);
          // validation.setMessage("Validation Failed");
          // validation.setSuccess(false);
          // if (StringUtils.isBlank(admin.getSourceSystId())) {
          // engineData.addRejectionComment("OTH", error, "", "");
          // output.setOnError(false);
          // } else {
          // engineData.addNegativeCheckStatus("BP_" + change.getDataField(),
          // error);
          // }
          // return true;
          // }
          // } else {
          // String ageError = performCMRNewCheck(cedpManager, entityManager,
          // requestData);
          // if (StringUtils.isNotBlank(ageError)) {
          // engineData.addNegativeCheckStatus("_chINACCheckFailed", ageError);
          // details.append(ageError);
          // validation.setSuccess(false);
          // validation.setMessage("Validation Failed");
          // if (StringUtils.isBlank(admin.getSourceSystId())) {
          // engineData.addRejectionComment("OTH", error, "", "");
          // output.setOnError(false);
          // } else {
          // engineData.addNegativeCheckStatus("BP_" + change.getDataField(),
          // error);
          // }
          // return true;
          // }
          // }
          break;
        case "Tax Code / Estab. Function Code":
        case "PST Exemption License Number":
          if (isVatDocRequired) {
            if (!isVatDocAttachmentProvided(entityManager, admin.getId().getReqId())) {
              details.append("\nVAT/TAX Documentation required to be attached when updating EFC/PST Exemption License Number.\n");
              engineData.addRejectionComment("TAX", "VAT/TAX Documentation required when updating EFC/PST Exemption Licenso No.",
                  "VAT/TAX Documentation required when updating EFC/PST Exemption Licenso No", "");
              output.setOnError(true);
            }
          }
          break;
        case "Client Tier":
          details.append(
              "\nUpdate of Client Tier should be done via JIRA. Please submit the request in JIRA.\nLink:- https://jsw.ibm.com/projects/CMDE/summary");
          engineData.addRejectionComment("CTC",
              "Update of Client Tier should be done via JIRA. Please submit the request in JIRA.\n Link:- https://jsw.ibm.com/projects/CMDE/summary",
              "", "");
          output.setOnError(true);
          break;
        case "ISU Code":
          details.append(
              "\nUpdate of ISU Code should be done via JIRA. Please submit the request in JIRA.\nLink:- https://jsw.ibm.com/projects/CMDE/summary");
          engineData.addRejectionComment("ISU",
              "Update of ISU Code should be done via JIRA. Please submit the request in JIRA.\n Link:- https://jsw.ibm.com/projects/CMDE/summary", "",
              "");
          output.setOnError(true);
          break;
        case "SORTL":
          // noop, for switch handling only
          break;
        default:
          ignoredUpdates.add(change.getDataField());
          break;
        }
      }

      LOG.debug("Verifying PayGo Accreditation for " + admin.getSourceSystId());
      boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());

      if (changes.isLegalNameChanged() && !payGoAddredited) {
        engineData.addNegativeCheckStatus("_legalNameChanged", "Legal Name change should be validated.");
        details.append("Legal Name change should be validated.\n");
        validation.setSuccess(false);
        validation.setMessage("Not Validated");
      }

      if (cmdeReview) {
        engineData.addNegativeCheckStatus("_chDataCheckFailed", "Updates to one or more fields cannot be validated.");
        details.append("Updates to one or more fields cannot be validated.\n");
        validation.setSuccess(false);
        validation.setMessage("Not Validated");
      } else {
        validation.setSuccess(true);
        validation.setMessage("Successful");
      }
      if (!ignoredUpdates.isEmpty()) {
        details.append("Updates to the following fields skipped validation:\n");
        for (String field : ignoredUpdates) {
          details.append(" - " + field + "\n");
        }
      }
      output.setDetails(details.toString());
      output.setProcessOutput(validation);
    }
    return true;
  }

  @Override
  public boolean runUpdateChecksForAddress(EntityManager entityManager, AutomationEngineData engineData, RequestData requestData,
      RequestChangeContainer changes, AutomationResult<ValidationOutput> output, ValidationOutput validation) throws Exception {
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    // if (handlePrivatePersonRecord(entityManager, admin, output, validation,
    // engineData)) {
    // return true;
    // }

    List<Addr> addresses = null;

    StringBuilder duplicateDetails = new StringBuilder();
    StringBuilder checkDetails = new StringBuilder();

    // D - duplicates, R - review
    Set<String> resultCodes = new HashSet<String>();

    String sqlKey = ExternalizedQuery.getSql("AUTO.US.CHECK_CMDE");
    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
    query.setParameter("EMAIL", admin.getRequesterId());
    query.setForReadOnly(true);
    if (query.exists()) {
      // skip checks if requester is from Canada CMDE team
      admin.setScenarioVerifiedIndc("Y");
      LOG.debug("Requester is from CA CMDE team, skipping update checks.");
      output.setDetails("Requester is from CA CMDE team, skipping update checks.\n");
      validation.setMessage("Skipped");
      validation.setSuccess(true);
    } else {

      for (String addrType : RELEVANT_ADDRESSES) {
        if (changes.isAddressChanged(addrType)) {
          if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
            addresses = Collections.singletonList(requestData.getAddress(CmrConstants.RDC_SOLD_TO));
          } else {
            addresses = requestData.getAddresses(addrType);
          }
          for (Addr addr : addresses) {
            if ("N".equals(addr.getImportInd())) {

              if (addrType.startsWith("ZP")) {
                LOG.debug("Addition of " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Addition of new " + addrType + "(" + addr.getId().getAddrSeq() + ") address skipped in the checks.\n");
              } else if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
                closelyMatchAddressWithDnbRecords(entityManager, requestData, engineData, "ZS01", checkDetails, validation, output);
              } else if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
                closelyMatchAddressWithDnbRecords(entityManager, requestData, engineData, "ZI01", checkDetails, validation, output);
              } else {
                List<DnBMatchingResponse> matches = getMatches(requestData, engineData, addr, false);
                boolean matchesDnb = false;
                boolean companyProofProvided = DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId());
                if (matches != null) {
                  // check against D&B
                  matchesDnb = ifaddressCloselyMatchesDnb(matches, addr, admin, data.getCmrIssuingCntry());
                }
                if (!matchesDnb) {
                  LOG.debug("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") does not match D&B");
                  if (companyProofProvided) {
                    checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
                    checkDetails.append(
                        "Supporting documentation is provided by the requester as attachment for " + addrType + "(" + addr.getId().getAddrSeq() + ")")
                        .append("\n");
                  } else {
                    resultCodes.add("R");
                    checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") did not match D&B records.\n");
                  }
                } else {
                  checkDetails.append("New address " + addrType + "(" + addr.getId().getAddrSeq() + ") matches D&B records. Matches:\n");
                  for (DnBMatchingResponse dnb : matches) {
                    checkDetails.append(" - DUNS No.:  " + dnb.getDunsNo() + " \n");
                    checkDetails.append(" - Name.:  " + dnb.getDnbName() + " \n");
                    checkDetails.append(" - Address:  " + dnb.getDnbStreetLine1() + " " + dnb.getDnbCity() + " " + dnb.getDnbPostalCode() + " "
                        + dnb.getDnbCountry() + "\n\n");
                  }
                }
                // }
              }
            } else if ("Y".equals(addr.getChangedIndc())) {
              // updated addresses
              if (CmrConstants.RDC_SOLD_TO.equals(addrType)) {
                closelyMatchAddressWithDnbRecords(entityManager, requestData, engineData, "ZS01", checkDetails, validation, output);
              } else if (CmrConstants.RDC_INSTALL_AT.equals(addrType)) {
                closelyMatchAddressWithDnbRecords(entityManager, requestData, engineData, "ZI01", checkDetails, validation, output);
              } else if (CmrConstants.RDC_SHIP_TO.equals(addrType) || addrType.startsWith("ZP")) {
                // just proceed for billing and shipping updates
                LOG.debug("Update to " + addrType + "(" + addr.getId().getAddrSeq() + ")");
                checkDetails.append("Updates to (" + addr.getId().getAddrSeq() + ") skipped in the checks.\n");
              } else {
                // update to other relevant addresses
                if (isRelevantAddressFieldUpdated(changes, addr)) {
                  checkDetails.append("Updates to address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") need to be verified.")
                      .append("\n");
                  resultCodes.add("R");
                } else {
                  checkDetails.append("Updates to non-address fields for " + addrType + "(" + addr.getId().getAddrSeq() + ") skipped in the checks.")
                      .append("\n");
                }
              }
            }
          }
        }
      }
    }
    if (resultCodes.contains("D")) {
      // prioritize duplicates, set error
      output.setOnError(true);
      engineData.addRejectionComment("DUPADDR", "One or more new addresses matches existing addresses on record.", "", "");
      validation.setSuccess(false);
      validation.setMessage("Duplicate Address");
    } else if (resultCodes.contains("R")) {
      validation.setSuccess(false);
      validation.setMessage("Not Validated");
      engineData.addNegativeCheckStatus("_chCheckFailed", "Updated elements cannot be checked automatically.");
    } else {
      validation.setSuccess(true);
      validation.setMessage("Successful");
    }

    String details = (output.getDetails() != null && output.getDetails().length() > 0) ? output.getDetails() : "";
    details += duplicateDetails.length() > 0 ? duplicateDetails.toString() : "";
    details += checkDetails.length() > 0 ? "\n" + checkDetails.toString() : "";
    output.setDetails(details);
    output.setProcessOutput(validation);

    return true;
  }

  /**
   * Checks if the address is added on the Update Request
   *
   * @param addr
   * @return
   */
  private boolean isAddressAdded(Addr addr) {
    if (StringUtils.isNotEmpty(addr.getImportInd()) && "N".equals(addr.getImportInd())) {
      return true;
    }
    return false;
  }

  private boolean isRelevantAddressFieldUpdated(RequestChangeContainer changes, Addr addr) {
    List<UpdatedNameAddrModel> addrChanges = changes.getAddressChanges(addr.getId().getAddrType(), addr.getId().getAddrSeq());
    if (addrChanges == null) {
      return false;
    }
    for (UpdatedNameAddrModel change : addrChanges) {
      if (!NON_RELEVANT_ADDRESS_FIELDS.contains(change.getDataField())) {
        return true;
      }
    }
    return false;
  }

  private boolean isOnlyDnBRelevantFieldUpdated(RequestChangeContainer changes, String addrTypeCode) {
    boolean isDnBRelevantFieldUpdated = false;
    String[] addressFields = { "Customer Name 1", "Country (Landed)", "Street Address", "Postal Code", "City" };
    List<String> relevantFieldNames = Arrays.asList(addressFields);
    for (String fieldId : relevantFieldNames) {
      UpdatedNameAddrModel addressChange = changes.getAddressChange(addrTypeCode, fieldId);
      if (addressChange != null) {
        isDnBRelevantFieldUpdated = true;
        break;
      }
    }
    return isDnBRelevantFieldUpdated;
  }

  @Override
  public List<String> getSkipChecksRequestTypesforCMDE() {
    return Arrays.asList("C", "U", "M", "R", "D");
  }

  /**
   * Checks to perform if INAC field updated.
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @return An error message if validation failed, null if validated.
   * @throws Exception
   */
  private String performInacCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "This CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle. Thank you. \nJira link https://jsw.ibm.com/projects/CMDE/summary";
    String sql = ExternalizedQuery.getSql("AUTO.CA.GET_CMR_REVENUE");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      BigDecimal revenue = new BigDecimal(0);
      if (results.get(0)[1] != null) {
        revenue = (BigDecimal) results.get(0)[1];
      }
      if (revenue.floatValue() > 0) {
        return error + "\n- CMR with revenue";
      } else if (revenue.floatValue() == 0) {
        sql = ExternalizedQuery.getSql("AUTO.CA.INAC_DUNS_CHECK");
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
        query.setParameter("CMR_NO", data.getCmrNo());
        query.setParameter("INAC", data.getInacCd());
        query.setForReadOnly(true);
        results = query.getResults(1);
        if (results != null && !results.isEmpty()) {
          // String guDunsNo = (String) results.get(0)[0];
          String gbgIdDb = (String) results.get(0)[1];

          CmrClientService odmService = new CmrClientService();
          RequestEntryModel model = requestData.createModelFromRequest();
          Addr soldTo = requestData.getAddress("ZS01");
          ModelMap response = new ModelMap();

          odmService.getBuyingGroup(entityManager, soldTo, model, response);
          String gbgId = (String) response.get("globalBuyingGroupID");
          if (StringUtils.isBlank(gbgId)) {
            gbgId = gbgIdDb;
          }

          if (StringUtils.isBlank(gbgId) || (StringUtils.isNotBlank(gbgId) && !gbgId.equals(data.getGbgId()))) {
            return "BG_ERROR";
          }

        } else {
          return error + "\n- Target INAC is not under the same GU DUNs/parent";
        }
      }
    }
    return null;
  }

  /**
   * Checks to perform if CMR is new (age within 30 days)
   * 
   * @param cedpManager
   * @param entityManager
   * @param requestData
   * @return An error message if validation failed, null if validated.
   * @throws Exception
   */
  private String performCMRNewCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData) throws Exception {
    Data data = requestData.getData();
    String error = "This CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle. Thank you. \nJira link https://jsw.ibm.com/projects/CMDE/summary";
    String sql = ExternalizedQuery.getSql("AUTO.CA.CHECK_CMR_NEW");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      String creationCapChanged = (String) results.get(0)[2];
      if (!"Ok".equals(creationCapChanged)) {
        if (!"Ok".equals(creationCapChanged)) {
          error += "\n- Not new CMR (for CMR pass the 30 days period)";
        }
        return error;
      }
    }
    return null;
  }

  private String performISICCheck(EntityManager cedpManager, EntityManager entityManager, RequestData requestData, UpdatedDataModel updatedDataModel)
      throws Exception {
    Data data = requestData.getData();
    String updatedValue = updatedDataModel.getNewData();
    String error = "This CMR does not fulfill the criteria to be updated in execution cycle, please contact CMDE via Jira to verify possibility of update in Preview cycle. Thank you. \nJira link https://jsw.ibm.com/projects/CMDE/summary";
    String sql = ExternalizedQuery.getSql("AUTO.CA.GET_CMR_REVENUE");
    PreparedQuery query = new PreparedQuery(cedpManager, sql);
    query.setParameter("CMR_NO", data.getCmrNo());
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults(1);
    if (results != null && results.size() > 0) {
      BigDecimal revenue = new BigDecimal(0);
      if (results.get(0)[1] != null) {
        revenue = (BigDecimal) results.get(0)[1];
      }
      if (revenue.floatValue() > 100000) {
        return error + "\n- CMR with revenue > 100K";
      } else if (revenue.floatValue() == 0) {
        String dunsNo = "";
        if (StringUtils.isNotBlank(data.getDunsNo())) {
          dunsNo = data.getDunsNo();
        } else {
          MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, null, "ZS01");
          if (response != null && DnBUtil.hasValidMatches(response)) {
            DnBMatchingResponse dnbRecord = response.getMatches().get(0);
            if (dnbRecord.getConfidenceCode() >= 8) {
              dunsNo = dnbRecord.getDunsNo();
            }
          }
        }

        if (StringUtils.isNotBlank(dunsNo)) {
          DnBCompany dnbData = DnBUtil.getDnBDetails(dunsNo);
          if (dnbData != null && StringUtils.isNotBlank(dnbData.getIbmIsic())) {
            if (!dnbData.getIbmIsic().equals(updatedValue)) {
              return error + "\n- Requested ISIC did not match value in D&B";
            } else {
              sql = ExternalizedQuery.getSql("AUTO.CA.GET_ISU_BY_ISIC");
              query = new PreparedQuery(entityManager, sql);
              query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
              query.setParameter("ISIC", updatedValue);
              query.setForReadOnly(true);
              String brsch = query.getSingleResult(String.class);
              if (!data.getIsuCd().equals(brsch)) {
                return error + "\n- ISU/Industry impact";
              } else {
                // check if isic and sicmen are equal if not set them equal
                if (data.getIsicCd() != null && !data.getIsicCd().equals(data.getUsSicmen())) {
                  if ("ISIC".equals(updatedDataModel.getDataField())) {
                    data.setUsSicmen(updatedValue);
                  } else {
                    data.setIsicCd(updatedValue);
                  }
                }

              }
            }
          } else {
            return error + "\n- Isic is blank";
          }
        } else {
          return error + "\n- Duns No. is blank";
        }
      }
    }
    return null;
  }

  private String getSbrFromCoverageId(String coverageId, EntityManager entityManager) {
    if (StringUtils.isNotBlank(coverageId)) {
      String covType = StringUtils.substring(coverageId, 0, 1);
      String covId = StringUtils.substring(coverageId, 1);

      String sql = ExternalizedQuery.getSql("AUTO.CA.GETSBO_BY_COVID");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
      query.setParameter("COVTYPE", covType);
      query.setParameter("COVID", covId);

      List<Object[]> results = query.getResults();
      if (results != null && results.size() > 0) {
        Object[] result = results.get(0);
        if (result != null && result.length > 0) {
          String sbr = (String) result[2];
          return sbr;
        }
      }
    }
    return null;
  }

  private String getArfarrBySbo(String sbo, EntityManager entityManager) {
    if (StringUtils.isNotBlank(sbo)) {
      String sql = ExternalizedQuery.getSql("QUERY.GET.SBODESC");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("ISSUING_CNTRY", SystemLocation.CANADA);
      query.setParameter("SALES_BO_CD", sbo);

      List<String> results = query.getResults(String.class);
      if (results != null && results.size() > 0) {
        String arfaar = results.get(0);
        return arfaar;
      }
    }
    return null;
  }

  private boolean isVatDocAttachmentProvided(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("QUERY.CHECK_VATD_ATTACHMENT");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ID", reqId);
    return query.exists();
  }

  private void setDefaultSBO(StringBuilder details, OverrideOutput overrides, String coverageId, Data data, String sbo) {
    details.append("Setting SBO based on Coverage ").append(coverageId).append(" to ").append(sbo).append("\n");
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "SALES_BO_CD", data.getSalesBusOffCd(), sbo);
    // set Install Branch Office if blank
    if (StringUtils.isBlank(data.getInstallBranchOff())) {
      details.append("Setting IBO based on Coverage ").append(coverageId).append(" to ").append(sbo).append("\n");
      overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "INSTALL_BRANCH_OFF", data.getInstallBranchOff(), sbo);
    }
  }

  @Override
  public boolean fillCoverageAttributes(RetrieveIBMValuesElement retrieveElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covType, String covId, String covDesc) throws Exception {
    LOG.debug("Performing Canada final fillCoverageAttributes...");
    Data data = requestData.getData();
    String coverageId = covType + covId;
    String sbo = "";
    if (StringUtils.isBlank(data.getSalesBusOffCd())) {
      if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0007992")) {
        sbo = "458";
        setDefaultSBO(details, overrides, coverageId, data, sbo);
      } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0008059")) {
        sbo = "570";
        setDefaultSBO(details, overrides, coverageId, data, sbo);
      } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000604")) {
        sbo = "486";
        setDefaultSBO(details, overrides, coverageId, data, sbo);
      } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000549")) {
        sbo = "481";
        setDefaultSBO(details, overrides, coverageId, data, sbo);
      } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000595")) {
        sbo = "457";
        setDefaultSBO(details, overrides, coverageId, data, sbo);
      } else if (StringUtils.isNotBlank(coverageId) && coverageId.equals("T0000597")) {
        sbo = "460";
        setDefaultSBO(details, overrides, coverageId, data, sbo);
      }
    }

    // ISU CTC Based on Coverage
    if (StringUtils.isNotBlank(coverageId)) {
      String isu = "";
      String ctc = "";

      String firstChar = coverageId.substring(0, 1);

      List<String> ECOSYSTEM_LIST = Arrays.asList("T0007992", "T0007993", "T0007994", "T0008059");

      if (("T").equalsIgnoreCase(firstChar) && !ECOSYSTEM_LIST.contains(coverageId)) {
        isu = "34";
        ctc = "Q";
        setISUCTCBasedOnCoverage(details, overrides, coverageId, data, isu, ctc);
      } else if (ECOSYSTEM_LIST.contains(coverageId)) {
        isu = "34";
        ctc = "Y";
        setISUCTCBasedOnCoverage(details, overrides, coverageId, data, isu, ctc);
      } else if (("A").equalsIgnoreCase(firstChar) || ("I").equalsIgnoreCase(firstChar)) {
        isu = ""; // apply logic to set isu based on sub industry code
        ctc = "";
        String subIndustryCd = data != null && data.getSubIndustryCd() != null ? data.getSubIndustryCd() : "";
        String firstCharSubIndustry = StringUtils.isNotEmpty(subIndustryCd) ? subIndustryCd.substring(0, 1) : "";

        Map<String, String> industryCodeISUMap = new HashMap<String, String>();

        industryCodeISUMap.put("A", "3T");
        industryCodeISUMap.put("U", "12");
        industryCodeISUMap.put("K", "05");
        industryCodeISUMap.put("R", "1R");
        industryCodeISUMap.put("D", "18");

        industryCodeISUMap.put("W", "18");
        industryCodeISUMap.put("T", "19");
        industryCodeISUMap.put("F", "04");
        industryCodeISUMap.put("S", "4F");
        industryCodeISUMap.put("N", "31");

        industryCodeISUMap.put("J", "4A");
        industryCodeISUMap.put("V", "14");
        industryCodeISUMap.put("L", "5E");
        industryCodeISUMap.put("P", "15");
        industryCodeISUMap.put("M", "4D");

        industryCodeISUMap.put("Y", "28");
        industryCodeISUMap.put("G", "28");
        industryCodeISUMap.put("E", "40");
        industryCodeISUMap.put("H", "11");
        industryCodeISUMap.put("X", "8C");

        industryCodeISUMap.put("B", "5B");
        industryCodeISUMap.put("C", "5B");

        if (industryCodeISUMap.containsKey(firstCharSubIndustry)) {
          isu = industryCodeISUMap.get(firstCharSubIndustry);
        }
        setISUCTCBasedOnCoverage(details, overrides, coverageId, data, isu, ctc);
      }
    }
    return true;
  }

  private void setISUCTCBasedOnCoverage(StringBuilder details, OverrideOutput overrides, String coverageId, Data data, String isu, String ctc) {
    LOG.debug("Setting ISU CTC based on coverage...");
    details.append("Setting ISU based on Coverage ").append(coverageId).append(" to ").append(isu).append("\n");
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), isu);

    details.append("Setting CTC based on Coverage ").append(coverageId).append(" to ").append(ctc).append("\n");
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), ctc);
  }

  /**
   * * Validates if address closely matches with DnB records matched.
   * 
   * @param entityManager
   * @param requestData
   * @param engineData
   * @param addrType
   * @param details
   * @param validation
   * @param output
   * @throws Exception
   */
  private void closelyMatchAddressWithDnbRecords(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      String addrType, StringBuilder details, ValidationOutput validation, AutomationResult<ValidationOutput> output) throws Exception {
    String addrDesc = "ZS01".equals(addrType) ? "Sold-To" : "Install-at";
    Addr addr = requestData.getAddress(addrType);
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
    boolean payGoAddredited = RequestUtils.isPayGoAccredited(entityManager, admin.getSourceSystId());
    MatchingResponse<DnBMatchingResponse> response = DnBUtil.getMatches(requestData, engineData, addrType);
    if (response.getSuccess()) {
      if (response.getMatched() && !response.getMatches().isEmpty()) {
        if (DnBUtil.hasValidMatches(response)) {
          boolean isAddressMatched = false;
          for (DnBMatchingResponse record : response.getMatches()) {
            if (record.getConfidenceCode() > 7 && DnBUtil.closelyMatchesDnb(data.getCmrIssuingCntry(), addr, admin, record)) {
              isAddressMatched = true;
              break;
            }
          }
          if (isAddressMatched) {
            details.append(addrDesc + " address details matched successfully with High Quality D&B Matches.").append("\n");
            validation.setMessage("Validated.");
            validation.setSuccess(true);
          } else {
            // company proof
            if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
              validation.setMessage("Validated");
              details.append("High confidence D&B matches did not match the " + addrDesc + " address data.").append("\n");
              details.append("Supporting documentation is provided by the requester as attachment for " + addrDesc).append("\n");
              validation.setSuccess(true);
            } else {
              validation.setMessage("Rejected");
              validation.setSuccess(false);
              details.append("High confidence D&B matches did not match the " + addrDesc + " address data.").append("\n");
              details.append("\nNo supporting documentation is provided by the requester for " + addrDesc + " address.");
              engineData.addRejectionComment("OTH", "No supporting documentation is provided by the requester for " + addrDesc + " address.", "", "");
              output.setOnError(true);
              output.setDetails(details.toString());
              if (payGoAddredited) {
                admin.setPaygoProcessIndc("Y");
              }
              LOG.debug("D&B matches were chosen to be overridden by the requester and needs to be reviewed");
            }
          }
        } else {
          // company proof
          if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
            validation.setMessage("Validated");
            details.append("No High Quality D&B Matches were found for " + addrDesc + " address.").append("\n");
            details.append("Supporting documentation is provided by the requester as attachment for " + addrDesc).append("\n");
            validation.setSuccess(true);
          } else {
            validation.setMessage("Rejected");
            validation.setSuccess(false);
            details.append("No High Quality D&B Matches were found for " + addrDesc + " address.").append("\n");
            details.append("\nNo supporting documentation is provided by the requester for " + addrDesc + " address.");
            engineData.addRejectionComment("OTH", "No supporting documentation is provided by the requester for " + addrDesc + " address.", "", "");
            output.setOnError(true);
            output.setDetails(details.toString());
            if (payGoAddredited) {
              admin.setPaygoProcessIndc("Y");
            }
            LOG.debug("D&B matches were chosen to be overridden by the requester and needs to be reviewed");
          }
        }
      } else {
        // company proof
        if (DnBUtil.isDnbOverrideAttachmentProvided(entityManager, admin.getId().getReqId())) {
          validation.setMessage("Validated");
          details.append("No D&B Matches were found for " + addrDesc + " address.").append("\n");
          details.append("Supporting documentation is provided by the requester as attachment for " + addrDesc).append("\n");
          validation.setSuccess(true);
        } else {
          validation.setMessage("Rejected");
          validation.setSuccess(false);
          details.append("No D&B Matches were found for " + addrDesc + " address.").append("\n");
          engineData.addRejectionComment("OTH", "No supporting documentation is provided by the requester for " + addrDesc + " address.", "", "");
          output.setOnError(true);
          output.setDetails(details.toString());
          if (payGoAddredited) {
            admin.setPaygoProcessIndc("Y");
          }
          LOG.debug("D&B matches were chosen to be overridden by the requester and needs to be reviewed");
        }
      }
    } else {
      engineData.addNegativeCheckStatus("DNB_MATCH_FAIL_" + "ZS01", "D&B Matching couldn't be performed for " + addrDesc + " address.");
      details.append("D&B Matching couldn't be performed for " + addrDesc + " address.").append("\n");
      validation.setMessage("Review needed");
      validation.setSuccess(false);
    }
  }
}
