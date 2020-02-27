/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.DecisionServiceClient;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupInput;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupInputList;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupOutput;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupRequest;
import com.ibm.cmr.services.client.wodm.bg.BuyingGroupResponse;
import com.ibm.cmr.services.client.wodm.coverage.Coverage;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInputList;
import com.ibm.cmr.services.client.wodm.coverage.CoverageOutput;
import com.ibm.cmr.services.client.wodm.coverage.CoverageRequest;
import com.ibm.cmr.services.client.wodm.coverage.CoverageResponse;
import com.ibm.cmr.services.client.wodm.coverage.IBM;
import com.ibm.cmr.services.client.wodm.glc.GlcCoverageInput;
import com.ibm.cmr.services.client.wodm.glc.GlcCoverageInputList;
import com.ibm.cmr.services.client.wodm.glc.GlcRequest;
import com.ibm.cmr.services.client.wodm.glc.GlcResponse;

/**
 * @author Jeffrey Zamora
 * 
 */
public class CoverageBgGlcISUHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(CoverageBgGlcISUHandler.class);

  private static final List<String> COMMERCIAL_TIER = BatchUtil.getAsList("CTC_COMMERCIAL");
  private static final List<String> ENTERPRISE_TIER = BatchUtil.getAsList("CTC_ENTERPRISE");
  private static final List<String> UNASSIGNED_TIER = BatchUtil.getAsList("CTC_UNASSIGNED");
  private static final String COMMERCIAL_ISU = BatchUtil.getProperty("ISU_COMMERCIAL");
  private static final String ENTERPRISE_ISU = BatchUtil.getProperty("ISU_ENTERPRISE");
  private static final String UNASSIGNED_ISU = BatchUtil.getProperty("ISU_UNASSIGNED");
  private static final String CSP_ISU = "5B";

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    String currentISU = row.getData().getIsuCd();
    RowResult result = new RowResult();
    String batchUrl = SystemConfiguration.getValue("BATCH_SERVICES_URL");
    DecisionServiceClient client = CmrServicesFactory.getInstance().createClient(batchUrl, DecisionServiceClient.class);
    client.setUser(SystemConfiguration.getSystemProperty("cmrservices.user"));
    client.setPassword(SystemConfiguration.getSystemProperty("cmrservices.password"));

    MassCreateAddr addr = null;
    MassCreateData data = row.getData();

    for (MassCreateAddr mcAddr : row.getAddresses()) {
      if (CmrConstants.ADDR_TYPE.ZI01.toString().equals(mcAddr.getId().getAddrType())) {
        addr = mcAddr;
        break;
      }
    }
    if (addr == null && row.getAddresses().size() > 0) {
      addr = row.getAddresses().get(0);
    }

    if (getGeoLocationCode(data, addr, client)) {
      if (getBuyingGroup(data, addr, client)) {
        if (getCoverage(data, addr, client)) {

          if ("CSP".equals(data.getCustSubGrp())) {
            data.setClientTier(BatchUtil.getProperty("CTC_CSP"));
          }

          if (StringUtils.isBlank(data.getClientTier())) {
            if (StringUtils.isBlank(currentISU)) {
              LOG.debug("Client Tier is empty. Setting to unassigned. ");
              data.setIsuCd(UNASSIGNED_ISU);
              row.mapRawValue("ISU_CD", UNASSIGNED_ISU + " | Unassigned");
              row.addUpdateCol("ISU_CD");
            } else {
              LOG.debug("Client Tier is empty. User supplied ISU " + currentISU + " will not be overwritten");
            }
          } else {
            if (CSP_ISU.equals(currentISU)) {
              LOG.debug("ISU is set to 5B, will not overwrite for CSP Customers.");
            } else {
              if (COMMERCIAL_TIER.contains(data.getClientTier())) {
                LOG.debug("Setting to ISU 32");
                data.setIsuCd(COMMERCIAL_ISU);
                row.mapRawValue("ISU_CD", COMMERCIAL_ISU + " | Commercial");
                row.addUpdateCol("ISU_CD");
              } else if (ENTERPRISE_TIER.contains(data.getClientTier())) {
                LOG.debug("Setting to ISU 34");
                data.setIsuCd(ENTERPRISE_ISU);
                row.mapRawValue("ISU_CD", ENTERPRISE_ISU + " | Enterprise");
                row.addUpdateCol("ISU_CD");
              } else if (UNASSIGNED_TIER.contains(data.getClientTier())) {
                if (StringUtils.isBlank(currentISU)) {
                  LOG.debug("Setting to ISU 21");
                  data.setIsuCd(UNASSIGNED_ISU);
                  row.mapRawValue("ISU_CD", UNASSIGNED_ISU + " | Unassigned");
                  row.addUpdateCol("ISU_CD");
                } else {
                  LOG.debug("Client Tier is Z. User supplied ISU " + currentISU + " will not be overwritten");
                }
              } else {
                result.addError("Client Tier does not map to an ISU. Please assign ISU manually.");
              }
            }
          }
        } else {
          if (StringUtils.isBlank(data.getIsuCd())) {
            result.addError("Coverage cannot retrieved. ISU Cannot be assigned automatically.");
          }
        }
      } else {
        if (StringUtils.isBlank(data.getIsuCd())) {
          result.addError("Buying Group cannot retrieved. ISU Cannot be assigned automatically.");
        }
      }
    } else {
      if (StringUtils.isBlank(data.getIsuCd())) {
        result.addError("GLC cannot retrieved. ISU Cannot be assigned automatically.");
      }
    }

    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
  }

  private boolean getGeoLocationCode(MassCreateData data, MassCreateAddr addr, DecisionServiceClient client) throws Exception {
    LOG.debug("Retrieving GEO Location Code for Request ID " + data.getId().getParReqId());
    try {
      GlcRequest request = new GlcRequest();
      GlcCoverageInputList list = new GlcCoverageInputList();
      GlcCoverageInput input = new GlcCoverageInput();

      input.setCountryCode(addr.getLandCntry());
      input.setPostalCode(addr.getPostCd());
      input.setCity(addr.getCity1());
      input.setStateProvinceCode(addr.getStateProv());
      input.setSitePartyID(data.getSitePartyId());
      list.getCoverageInput().add(input);
      request.setCoverageInputList(list);

      GlcResponse responseObj = client.executeAndWrap(DecisionServiceClient.GLC_APP_ID, request, GlcResponse.class);
      if (responseObj.getCoverageInputList() != null && responseObj.getCoverageInputList().getCoverageInput() != null
          && responseObj.getCoverageInputList().getCoverageInput().size() > 0) {
        GlcCoverageInput output = responseObj.getCoverageInputList().getCoverageInput().get(0);
        LOG.debug("Got GLC: " + output.getGlcCode());
        data.setGeoLocCd(output.getGlcCode());
        data.setGeoLocDesc(output.getGlcDesc());
        return true;
      } else {
        LOG.debug("No GLC retrieved.");
        return true;
      }
    } catch (Exception e) {
      LOG.error("Error in retrieving GLC code", e);
      return false;
    }
  }

  private boolean getBuyingGroup(MassCreateData data, MassCreateAddr addr, DecisionServiceClient client) throws Exception {
    LOG.debug("Retrieving Buying Group for Request ID " + data.getId().getParReqId());
    try {
      BuyingGroupRequest request = new BuyingGroupRequest();
      request.setDecisionID(UUID.randomUUID().toString());
      BuyingGroupInputList list = new BuyingGroupInputList();
      BuyingGroupInput input = new BuyingGroupInput();

      input.setAffiliateGroup(data.getAffiliate());
      input.setCompanyNumber(data.getCompany());
      input.setCountryCode(addr.getLandCntry());
      input.setCustomerNumber(data.getCmrNo());
      input.setDb2ID(data.getCmrNo());
      input.setEnterprise(data.getEnterprise());
      input.setINAC(data.getInacCd());
      input.setIndustryClass(data.getSubIndustryCd());
      input.setNationalTaxID(data.getTaxCd1());
      input.setUnISIC(data.getIsicCd());
      input.setVatRegistrationNumber(data.getVat());

      list.getBuyingGroupInput().add(input);
      request.setBuyingGroupInputList(list);

      BuyingGroupResponse responseObj = client.executeAndWrap(DecisionServiceClient.BUYING_GROUP_APP_ID, request, BuyingGroupResponse.class);
      if (responseObj.getBuyingGroupOutputList() != null && responseObj.getBuyingGroupOutputList().getBuyingGroupOutput() != null
          && responseObj.getBuyingGroupOutputList().getBuyingGroupOutput().size() > 0) {
        BuyingGroupOutput output = responseObj.getBuyingGroupOutputList().getBuyingGroupOutput().get(0);
        LOG.debug("BG " + output.getBuyingGroupID() + " GBG: " + output.getGlobalBuyingGroupID());
        data.setBgId(output.getBuyingGroupID());
        data.setBgDesc(output.getBuyingGroupDesc());
        data.setGbgId(output.getGlobalBuyingGroupID());
        data.setGbgDesc(output.getGlobalBuyingGroupDesc());
        data.setBgRuleId(output.getOdmRuleID());
        return true;
      } else {
        LOG.debug("No Buying Group Returned.");
        return true;
      }
    } catch (Exception e) {
      LOG.error("Error in retrieving Buying Group", e);
      return false;
    }
  }

  private boolean getCoverage(MassCreateData data, MassCreateAddr addr, DecisionServiceClient client) throws Exception {
    LOG.debug("Retrieving Coverage for Request ID " + data.getId().getParReqId());
    try {
      CoverageRequest request = new CoverageRequest();
      request.setDecisionID(UUID.randomUUID().toString());

      CoverageInputList list = new CoverageInputList();

      CoverageInput coverage = new CoverageInput();

      coverage.setAffiliateGroup(data.getAffiliate());
      coverage.setClassification(data.getCustClass());
      coverage.setCompanyNumber(data.getCompany());
      coverage.setSegmentation("IBM".equals(data.getCmrOwner()) ? null : data.getCmrOwner());
      // String isoCntryCd =
      // SystemUtil.getISOCountryCode(data.getCmrIssuingCntry());
      String isoCntryCd = "US";
      coverage.setCountryCode(isoCntryCd != null ? isoCntryCd : data.getCmrIssuingCntry());
      coverage.setDb2ID(data.getCmrNo());
      coverage.setEnterprise(data.getEnterprise());
      coverage.setGbQuadSectorTier(data.getClientTier());
      coverage.setINAC(data.getInacCd());
      coverage.setIndustryClass(data.getSubIndustryCd());
      coverage.setIndustryCode(data.getSubIndustryCd() != null && data.getSubIndustryCd().length() > 0 ? data.getSubIndustryCd().substring(0, 1)
          : null);
      coverage.setIndustrySolutionUnit(data.getIsuCd());
      coverage.setNationalTaxID(data.getTaxCd1());
      coverage.setSORTL(data.getSearchTerm());
      coverage.setUnISIC(data.getIsicCd());
      coverage.setSitePartyID(data.getSitePartyId());
      coverage.setCity(addr.getCity1());
      coverage.setCounty(addr.getCounty());
      coverage.setPostalCode(addr.getPostCd());
      if (!"''".equals(addr.getStateProv())) {
        coverage.setStatePrefectureCode(addr.getStateProv());
      }
      coverage.setPhysicalAddressCountry(addr.getLandCntry());

      if (!StringUtils.isBlank(data.getBgId())) {
        IBM ibm = new IBM();
        ibm.setDomesticBuyingGroupID(data.getBgId());
        ibm.setGlobalBuyingGroupID(data.getGbgId());
        coverage.setIbm(ibm);
      }
      coverage.setGeoLocationCode(data.getGeoLocCd());

      if ("CSP".equals(data.getCustSubGrp()) || "N".equals(data.getClientTier())) {
        coverage.setClassification("52");
      }

      list.getCoverageInput().add(coverage);
      request.setCoverageInputList(list);
      CoverageResponse responseObj = client.executeAndWrap(DecisionServiceClient.COVERAGE_APP_ID, request, CoverageResponse.class);
      if (responseObj.getCoverageOutputList() != null && responseObj.getCoverageOutputList().getCoverageOutput() != null
          && responseObj.getCoverageOutputList().getCoverageOutput().size() > 0) {
        CoverageOutput output = responseObj.getCoverageOutputList().getCoverageOutput().get(0);
        Coverage coverageOutput = output.getCoverage().size() > 0 ? output.getCoverage().get(0) : new Coverage();
        LOG.debug("Coverage " + coverageOutput.getCoverageType() + coverageOutput.getCoverageID() + " Tier: "
            + coverageOutput.getCoverageGbQuadSectorTier());
        data.setCovId(coverageOutput.getCoverageType() + coverageOutput.getCoverageID());
        data.setCovDesc(coverageOutput.getCoverageDesc());
        data.setClientTier(coverageOutput.getCoverageGbQuadSectorTier());
        return true;
      } else {
        LOG.debug("No Coverage Returned.");
        return false;
      }
    } catch (Exception e) {
      LOG.error("Error in retrieving Coverage", e);
      return false;
    }
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
