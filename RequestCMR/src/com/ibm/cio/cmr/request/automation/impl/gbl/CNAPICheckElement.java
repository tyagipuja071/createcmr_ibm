package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.IntlAddrRdc;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.impl.CNHandler;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.cn.CNResponse;

public class CNAPICheckElement extends ValidatingElement implements CompanyVerifier {

  private static final Logger LOG = Logger.getLogger(CNAPICheckElement.class);

  // private static final String COMPANY_VERIFIED_INDC_YES = "Y";
  public static final String RESULT_ACCEPTED = "Accepted";
  public static final String MATCH_INDC_YES = "Y";
  public static final String RESULT_REJECTED = "Rejected";

  public CNAPICheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError) {
    super(requestTypes, actionOnError, overrideData, stopOnError);

  }

  @Override
  public AutomationResult<ValidationOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
      throws Exception {
    Addr soldTo = requestData.getAddress("ZS01");
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();

    String SCENARIO_LOCAL_NRML = "NRML";
    String SCENARIO_LOCAL_EMBSA = "EMBSA";
    String SCENARIO_CROSS_CROSS = "CROSS";
    String SCENARIO_LOCAL_AQSTN = "AQSTN";
    String SCENARIO_LOCAL_BLUMX = "BLUMX";
    String SCENARIO_LOCAL_MRKT = "MRKT";
    String SCENARIO_LOCAL_BUSPR = "BUSPR";
    String SCENARIO_LOCAL_INTER = "INTER";
    String SCENARIO_LOCAL_PRIV = "PRIV";
    boolean ifAQSTNHasCN = false;

    CNHandler handler = (CNHandler) RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    AutomationResult<ValidationOutput> result = buildResult(admin.getId().getReqId());
    result.setOnError(false);

    ValidationOutput validation = new ValidationOutput();
    IntlAddr iAddr = new IntlAddr();
    String cnName = null;
    String cnAddr = null;
    iAddr = handler.getIntlAddrById(soldTo, entityManager);

    if (iAddr != null) {

      if (data.getCustSubGrp() != null && !SCENARIO_LOCAL_AQSTN.equals(data.getCustSubGrp())) {
        cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? iAddr.getIntlCustNm2() : "");
        cnAddr = iAddr.getAddrTxt() + (iAddr.getIntlCustNm4() != null ? iAddr.getIntlCustNm4() : "");
      } else if (data.getCustSubGrp() != null && SCENARIO_LOCAL_AQSTN.equals(data.getCustSubGrp())) {

        if (StringUtils.isNotBlank(iAddr.getIntlCustNm1())) {
          cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? iAddr.getIntlCustNm2() : "");
          cnAddr = iAddr.getAddrTxt() + (iAddr.getIntlCustNm4() != null ? iAddr.getIntlCustNm4() : "");
          ifAQSTNHasCN = true;
        } else {
          ifAQSTNHasCN = false;
        }
      }
    }

    LOG.debug("Entering DnB Check Element");
    if (requestData.getAdmin().getReqType().equalsIgnoreCase("C")) {
      // if (!scenarioExceptions.isSkipDuplicateChecks()) {
      if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
        StringBuilder details = new StringBuilder();
        details.append("User requested to proceed with Duplicate CMR Creation.").append("\n");
        details.append("Reason provided - ").append("\n");
        details.append(admin.getDupCmrReason()).append("\n");
        result.setDetails(details.toString());
        result.setResults("Overridden");
        // result.setOnError(false);
      } else if (soldTo != null) {

        if (data.getCustSubGrp() != null && (SCENARIO_LOCAL_NRML.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_EMBSA.equals(data.getCustSubGrp())
            || (SCENARIO_LOCAL_AQSTN.equals(data.getCustSubGrp()) && ifAQSTNHasCN) || SCENARIO_LOCAL_BLUMX.equals(data.getCustSubGrp())
            || SCENARIO_LOCAL_MRKT.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_BUSPR.equals(data.getCustSubGrp()))) {
          CompanyRecordModel searchModel = new CompanyRecordModel();
          searchModel.setIssuingCntry(data.getCmrIssuingCntry());
          searchModel.setCountryCd(soldTo.getLandCntry());

          if (StringUtils.isNotEmpty(data.getBusnType())) {
            searchModel.setTaxCd1(data.getBusnType());
          } else {

            if (iAddr != null) {
              searchModel.setTaxCd1(cnName);
            }
          }
          try {
            AutomationResponse<CNResponse> cmrsData = CompanyFinder.getCNApiInfo(searchModel, "DEFAULT");
            if (cmrsData != null && cmrsData.isSuccess()) {

              StringBuilder details = new StringBuilder();

              if (cnName.equals(cmrsData.getRecord().getName()) && cnAddr.equals(cmrsData.getRecord().getRegLocation())) {

                result.setResults("Matches found");
                details.append("High confidence Chinese name and address were found.");

                requestData.getAdmin().setMatchIndc("C");
                // result.setOnError(false);
                details.append("\n");
                // logDuplicateCMR(details, cmrData);
                result.setProcessOutput(validation);
                result.setDetails(details.toString().trim());
                // engineData.addNegativeCheckStatus("CNAPICheck", "High
                // confidence Chinese name and address were found.");
                LOG.debug("High confidence Chinese name and address were found.\n");

              } else {
                overrideNameAndAddress(cmrsData.getRecord().getName(), cmrsData.getRecord().getRegLocation(), iAddr, entityManager);

                result.setOnError(true);
                result.setResults("Overridden");

                details.append("Processor review is required as no high confidence Chinese name and address were found. ").append("\n");
                details.append("Request name: " + cnName + "\n" + " API name: " + cmrsData.getRecord().getName() + "\n");
                details.append("Request address: " + cnAddr + "\n" + " API address: " + cmrsData.getRecord().getRegLocation() + "\n");
                details.append(
                    "Chinese name and address have been override by trust source. if you agree, please Send for Processing. if you don't agree, please attach supporting document and click Disable automatic processing checkbox.")
                    .append("\n");
                result.setDetails(details.toString().trim());
                engineData.addNegativeCheckStatus("CNAPICheck", "No high confidence China API matches were found.");
                LOG.debug("Processor review is required as no high confidence Chinese name and address were found.");
              }
            } else {
              result.setDetails("No China API Data were found.");
              result.setResults("No Matches");
              engineData.addRejectionComment("NOCN", "No China API Data were found.", "", "");
              result.setOnError(true);
            }
          } catch (Exception e) {
            e.printStackTrace();
            result.setDetails("Error on get China API Data Check.");
            engineData.addRejectionComment("OTH", "Error on  get China API Data Check.", "", "");
            result.setOnError(true);
            result.setResults("Error on  get China API Data Check.");
            // LOG.debug("Error on China API Validating" + e.getMessage());
          }

        }
      } else {
        result.setDetails("Missing main address on the request.");
        engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
        result.setResults("No Matches");
        result.setOnError(true);
      }
    } else if (requestData.getAdmin().getReqType().equalsIgnoreCase("U")) {

      StringBuilder details = new StringBuilder();
      if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
        details.append("User requested to proceed with Duplicate CMR Creation.").append("\n");
        details.append("Reason provided - ").append("\n");
        details.append(admin.getDupCmrReason()).append("\n");
        result.setDetails(details.toString());
        result.setResults("Overridden");
        // result.setOnError(false);
      } else if (soldTo != null) {
        AutomationResponse<CNResponse> cmrsData = new AutomationResponse<CNResponse>();
        CompanyRecordModel searchModel = new CompanyRecordModel();
        IntlAddr soldToIntlAddr = new IntlAddr();
        IntlAddrRdc soldToIntlAddrRdc = new IntlAddrRdc();

        // if(StringUtils.isNotEmpty(data.getPpsceid())) {
        // details.append("Skipping Chinese name and address check for BP
        // scenario").append("\n");
        // LOG.debug("Skipping Chinese name and address check for BP scenario");
        // result.setResults("Skipped");
        // result.setOnError(false);
        // }else {

        searchModel.setIssuingCntry(data.getCmrIssuingCntry());
        searchModel.setCountryCd(soldTo.getLandCntry());
        if (StringUtils.isNotEmpty(data.getBusnType())) {
          searchModel.setTaxCd1(data.getBusnType());
        } else {
          if (iAddr != null) {
            searchModel.setTaxCd1(cnName);
          }
        }

        List<IntlAddr> intlAddrList = new ArrayList<IntlAddr>();
        List<IntlAddrRdc> intlAddrRdcList = new ArrayList<IntlAddrRdc>();
        // List<IntlAddr> zi01AddrList =
        // Map<String, IntlAddr> noSoldToAddrListMap = new HashMap<String,
        // IntlAddr>();

        List<IntlAddr> zi01AddrList = new ArrayList<IntlAddr>();
        List<IntlAddrRdc> zi01AddrRdcList = new ArrayList<IntlAddrRdc>();
        List<IntlAddr> zp01AddrList = new ArrayList<IntlAddr>();
        List<IntlAddrRdc> zp01AddrRdcList = new ArrayList<IntlAddrRdc>();
        List<IntlAddr> zd01AddrList = new ArrayList<IntlAddr>();
        List<IntlAddrRdc> zd01AddrRdcList = new ArrayList<IntlAddrRdc>();

        intlAddrList = handler.getINTLAddrCountByReqId(entityManager, requestData.getAdmin().getId().getReqId());
        intlAddrRdcList = handler.getINTLAddrRdcByReqId(entityManager, requestData.getAdmin().getId().getReqId());

        if (intlAddrList != null && intlAddrList.size() > 0) {
          for (IntlAddr intlAddr : intlAddrList) {
            if (intlAddr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZS01.toString())) {
              soldToIntlAddr = intlAddr;
            } else if (intlAddr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZI01.toString())) {
              zi01AddrList.add(intlAddr);
            } else if (intlAddr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZP01.toString())) {
              zp01AddrList.add(intlAddr);
            } else if (intlAddr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZD01.toString())) {
              zd01AddrList.add(intlAddr);
            }
          }
        }

        if (intlAddrRdcList != null && intlAddrRdcList.size() > 0) {
          for (IntlAddrRdc intlAddrRdc : intlAddrRdcList) {
            if (intlAddrRdc.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZS01.toString())) {
              soldToIntlAddrRdc = intlAddrRdc;
            } else if (intlAddrRdc.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZI01.toString())) {
              zi01AddrRdcList.add(intlAddrRdc);
            } else if (intlAddrRdc.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZP01.toString())) {
              zp01AddrRdcList.add(intlAddrRdc);
            } else if (intlAddrRdc.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZD01.toString())) {
              zd01AddrRdcList.add(intlAddrRdc);
            }
          }
        }

        // if non-sodeTo address type's Chinese name and address has changed
        if (zi01AddrList != null && zi01AddrList.size() > 0) {
          compareAddr(soldToIntlAddr, soldToIntlAddrRdc, zi01AddrList, zi01AddrRdcList, result, details, engineData);
        }
        if (zi01AddrList != null && zi01AddrList.size() > 0) {
          compareAddr(soldToIntlAddr, soldToIntlAddrRdc, zp01AddrList, zp01AddrRdcList, result, details, engineData);
        }
        if (zi01AddrList != null && zi01AddrList.size() > 0) {
          compareAddr(soldToIntlAddr, soldToIntlAddrRdc, zd01AddrList, zd01AddrRdcList, result, details, engineData);
        }
        if (result.isOnError()) {
          details.append("The Chinese name or address of the non-SOLDTO address type has been changed." + "Please attach supporting document.")
              .append("\n");
          result.setDetails(details.toString().trim());
          return result;
        }

        // Boolean nameCheckSuccess = true;
        checkChineseName(soldToIntlAddr, soldToIntlAddrRdc, searchModel, cmrsData, result, details, engineData, entityManager);
        details.append("\n");
        // if (result.isOnError()) {
        // nameCheckSuccess = false;
        // }
        checkChineseAddress(soldToIntlAddr, soldToIntlAddrRdc, searchModel, cmrsData, result, details, engineData, entityManager);
        // if (!nameCheckSuccess) {
        // result.setOnError(true);
        // }

        result.setProcessOutput(validation);
        result.setDetails(details.toString().trim());

      } else {
        details.append("Missing main address on the request.").append("\n");
        engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
        result.setResults("No Matches");
        result.setOnError(true);
        result.setDetails(details.toString().trim());
      }
    }
    // } else {
    // result.setDetails("Skipping Duplicate CMR checks for scenario");
    // log.debug("Skipping Duplicate CMR checks for scenario");
    // result.setResults("Skipped");
    // result.setOnError(false);
    // }

    return result;
  }

  private void compareAddr(IntlAddr soldToIntlAddr, IntlAddrRdc soldToIntlAddrRdc, List<IntlAddr> addrList, List<IntlAddrRdc> addrRdcList,
      AutomationResult<ValidationOutput> result, StringBuilder details, AutomationEngineData engineData) {

    String cnAddressAddr = null;
    String cnAddressAddrRdc = null;
    String cnNameAddr = null;
    String cnNameAddrRdc = null;
    String newCnName = soldToIntlAddr.getIntlCustNm1() + (soldToIntlAddr.getIntlCustNm2() != null ? soldToIntlAddr.getIntlCustNm2() : "");
    String oldCnName = soldToIntlAddrRdc.getIntlCustNm1() + (soldToIntlAddrRdc.getIntlCustNm2() != null ? soldToIntlAddrRdc.getIntlCustNm2() : "");
    String newCnAddr = soldToIntlAddr.getAddrTxt() + (soldToIntlAddr.getIntlCustNm4() != null ? soldToIntlAddr.getIntlCustNm4() : "");
    String oldCnAddr = soldToIntlAddrRdc.getAddrTxt() + (soldToIntlAddrRdc.getIntlCustNm4() != null ? soldToIntlAddrRdc.getIntlCustNm4() : "");
    if (oldCnName.equals(newCnName) || oldCnAddr.equals(newCnAddr)) {
      return;
    }

    for (IntlAddr intlAddr : addrList) {
      for (IntlAddrRdc intlAddrRdc : addrRdcList) {

        if (intlAddr.getId().getAddrSeq().equals(intlAddrRdc.getId().getAddrSeq())) {
          cnNameAddr = intlAddr.getIntlCustNm1() + (intlAddr.getIntlCustNm2() != null ? intlAddr.getIntlCustNm2() : "");
          cnNameAddrRdc = intlAddrRdc.getIntlCustNm1() + (intlAddrRdc.getIntlCustNm2() != null ? intlAddrRdc.getIntlCustNm2() : "");

          if (StringUtils.isNotBlank(intlAddr.getAddrTxt())) {
            cnAddressAddr = intlAddr.getAddrTxt() + (intlAddr.getIntlCustNm4() != null ? intlAddr.getIntlCustNm4() : "");
          } else {
            cnAddressAddr = intlAddr.getCity1() + (intlAddr.getCity2() != null ? intlAddr.getCity2() : "");
          }
          if (StringUtils.isNotBlank(intlAddrRdc.getAddrTxt())) {
            cnAddressAddr = intlAddrRdc.getAddrTxt() + (intlAddrRdc.getIntlCustNm4() != null ? intlAddrRdc.getIntlCustNm4() : "");
          } else {
            cnAddressAddr = intlAddrRdc.getCity1() + (intlAddrRdc.getCity2() != null ? intlAddrRdc.getCity2() : "");
          }

          if (!cnNameAddr.equals(cnNameAddrRdc)) {
            result.setResults("Review Needed");
            engineData.addRejectionComment("OTH", "The Chinese name of the " + intlAddr.getId().getAddrType() + " address type has been changed.", "",
                "");
            // details.append("The Chinese name of the non-SOLDTO address type
            // is changed from " + cnNameAddrRdc + " to " +
            // cnNameAddr).append("\n");
            result.setOnError(true);
          }
          if (!cnAddressAddr.equals(cnAddressAddrRdc)) {
            result.setResults("Review Needed");
            engineData.addRejectionComment("OTH", "The Chinese address of the " + intlAddr.getId().getAddrType() + " address type has been changed.",
                "", "");
            // details.append("The Chinese address of the non-SOLDTO address
            // type is changed from " + cnAddressAddrRdc + " to " +
            // cnAddressAddr)
            // .append("\n");
            result.setOnError(true);
          }
          if (result.isOnError()) {
            engineData.addRejectionComment("OTH", intlAddr.getId().getAddrType() + " address type has been changed.", "", "");
            // details.append(intlAddr.getId().getAddrType() + " address type
            // has been changed. " + "Please attach supporting
            // document.").append("\n");
            result.setDetails(details.toString().trim());
          }

        }
      }
    }
  }

  private void checkChineseName(IntlAddr soldToIntlAddr, IntlAddrRdc soldToIntlAddrRdc, CompanyRecordModel searchModel,
      AutomationResponse<CNResponse> cmrsData, AutomationResult<ValidationOutput> result, StringBuilder details, AutomationEngineData engineData,
      EntityManager entityManager) {

    String newCnName = soldToIntlAddr.getIntlCustNm1() + (soldToIntlAddr.getIntlCustNm2() != null ? soldToIntlAddr.getIntlCustNm2() : "");
    String oldCnName = soldToIntlAddrRdc.getIntlCustNm1() + (soldToIntlAddrRdc.getIntlCustNm2() != null ? soldToIntlAddrRdc.getIntlCustNm2() : "");

    if (!oldCnName.equals(newCnName)) {
      try {

        cmrsData = CompanyFinder.getCNApiInfo(searchModel, "DEFAULT");
      } catch (Exception e) {
        e.printStackTrace();
        details.append("Error on get China API Data Check.").append("\n");
        engineData.addRejectionComment("OTH", "Error on  get China API Data Check.", "", "");
        result.setOnError(true);
        result.setResults("Error on  get China API Data Check.");
      }

      if (cmrsData != null && cmrsData.isSuccess()) {
        String historyNames = cmrsData.getRecord().getHistoryNames();

        if (StringUtils.isNotBlank(historyNames)) {
          List<String> historyNameList = Arrays.asList(historyNames.split(";"));
          for (String historyName : historyNameList) {
            if (oldCnName.equals(historyName)) {

              result.setResults("Matches found");
              // result.setOnError(false);
              details.append("Original Chinese name matchs with historical name in API.").append("\n");
              // result.setProcessOutput(validation);
              // result.setDetails(details.toString().trim());
              if (newCnName.equals(cmrsData.getRecord().getName())) {
                details.append("Current Chinese name matchs with Chinese name in API.").append("\n");
              } else {
                overrideNameAndAddress(cmrsData.getRecord().getName(), cmrsData.getRecord().getRegLocation(), soldToIntlAddr, entityManager);

                // TODO do pop up to let user choose
                result.setResults("Overridden");
                result.setOnError(true);
                details.append(
                    "Chinese name has been override by trust source. if you agree, please Send for Processing. if you don't agree, please attach supporting document and click Disable automatic processing checkbox.")
                    .append("\n");

              }
            } else {
              details.append("Original Chinese name does not match with historical name in API.").append("\n");
              result.setResults("Fault Data");
              engineData.addRejectionComment("OTH", "Original Chinese name does not match with historical name in API.", "", "");
              result.setOnError(true);
            }
          }
        } else {
          details.append("The Chinese name has been updated, but no Chinese historical name is found in the API.").append("\n");
          result.setResults("Fault Data");
          engineData.addRejectionComment("OTH", "The Chinese name has been updated, but no Chinese historical name is found in the API.", "", "");
          result.setOnError(true);
        }

      } else {
        details.append("No China API Data were found.").append("\n");
        result.setResults("No Matches");
        engineData.addRejectionComment("NOCN", "No China API Data were found.", "", "");
        result.setOnError(true);
      }

    } else {
      details.append("Skipping Chinese name check as Chinese name has not been updated.").append("\n");
      LOG.debug("Skipping Chinese name check as Chinese name has not been updated");
      result.setResults("Skipped");
      // result.setOnError(false);
    }

  }

  private void checkChineseAddress(IntlAddr soldToIntlAddr, IntlAddrRdc soldToIntlAddrRdc, CompanyRecordModel searchModel,
      AutomationResponse<CNResponse> cmrsData, AutomationResult<ValidationOutput> result, StringBuilder details, AutomationEngineData engineData,
      EntityManager entityManager) {

    String newCnAddr = soldToIntlAddr.getAddrTxt() + (soldToIntlAddr.getIntlCustNm4() != null ? soldToIntlAddr.getIntlCustNm4() : "");
    String oldCnAddr = soldToIntlAddrRdc.getAddrTxt() + (soldToIntlAddrRdc.getIntlCustNm4() != null ? soldToIntlAddrRdc.getIntlCustNm4() : "");

    if (!oldCnAddr.equals(newCnAddr)) {
      try {
        if (cmrsData.getRecord() == null) {
          cmrsData = CompanyFinder.getCNApiInfo(searchModel, "DEFAULT");
        }
      } catch (Exception e) {
        e.printStackTrace();
        details.append("Error on get China API Data Check.").append("\n");
        engineData.addRejectionComment("OTH", "Error on  get China API Data Check.", "", "");
        result.setOnError(true);
        result.setResults("Error on  get China API Data Check.");
      }

      if (cmrsData != null && cmrsData.isSuccess()) {

        if (newCnAddr.equals(cmrsData.getRecord().getRegLocation())) {
          result.setResults("Matches found");
          // result.setOnError(false);
          // result.setProcessOutput(validation);
          // result.setDetails(details.toString().trim());
          details.append("Current Chinese address matchs with Chinese address in API.").append("\n");
        } else {
          overrideNameAndAddress(cmrsData.getRecord().getName(), cmrsData.getRecord().getRegLocation(), soldToIntlAddr, entityManager);
          result.setResults("Overridden");
          result.setOnError(true);
          details.append(
              "Chinese address has been override by trust source. if you agree, please Send for Processing. if you don't agree, please attach supporting document and click Disable automatic processing checkbox.")
              .append("\n");
        }

      } else {
        details.append("No China API Data were found.").append("\n");
        result.setResults("No Matches");
        engineData.addRejectionComment("NOCN", "No China API Data were found.", "", "");
        result.setOnError(true);
      }
    } else {
      details.append("Skipping Chinese address check as Chinese address has not been updated.").append("\n");
      LOG.debug("Skipping Chinese address check as Chinese address has not been updated");
      result.setResults("Skipped");
      // result.setOnError(false);
    }

  }

  private void overrideNameAndAddress(String name, String address, IntlAddr intlAddr, EntityManager entityManager) {

    List<IntlAddr> intlAddrList = new ArrayList<IntlAddr>();
    String sql = ExternalizedQuery.getSql("QUERY.INTL_ADDR_BY_REQ_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", intlAddr.getId().getReqId());

    try {
      intlAddrList = query.getResults(IntlAddr.class);
    } catch (Exception ex) {
      LOG.error("An error occured in getting the INTL_ADDR records");
      throw ex;
    }
    if (intlAddrList != null && intlAddrList.size() > 0) {
      for (IntlAddr iAddr : intlAddrList) {

        updateCNIntlAddr(name, address, iAddr, entityManager);
        entityManager.merge(iAddr);
      }
      entityManager.flush();
    }

  }

  private boolean updateCNIntlAddr(String cnName, String cnAddress, IntlAddr intlAddr, EntityManager entityManager) {

    int tempNewLen = 0;
    String newTxt = "";

    if (CNHandler.getLengthInUtf8(cnAddress) > CNHandler.CN_STREET_ADD_TXT) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(cnAddress, CNHandler.CN_STREET_ADD_TXT, CNHandler.CN_STREET_ADD_TXT);
      newTxt = cnAddress != null ? cnAddress.substring(0, tempNewLen) : "";
      String excess = cnAddress.substring(tempNewLen);
      intlAddr.setAddrTxt(newTxt);
      intlAddr.setIntlCustNm4(excess);
      // model.setCnAddrTxt2(excess + model.getCnAddrTxt2());
    } else {
      intlAddr.setAddrTxt(cnAddress);
    }

    if (CNHandler.getLengthInUtf8(cnName) > CNHandler.CN_CUST_NAME_1) {
      tempNewLen = CNHandler.getMaxWordLengthInUtf8(cnName, CNHandler.CN_CUST_NAME_1, CNHandler.CN_CUST_NAME_1);
      newTxt = cnName != null ? cnName.substring(0, tempNewLen) : "";
      String excess = cnName.substring(tempNewLen);
      intlAddr.setIntlCustNm1(newTxt);
      intlAddr.setIntlCustNm2(excess);
      // model.setCnCustName2(excess + model.getCnCustName2());
    } else {
      intlAddr.setIntlCustNm1(cnName);
    }

    return true;
  }

  @Override
  public String getProcessCode() {
    return AutomationElementRegistry.CN_API_CHECK;
  }

  @Override
  public String getProcessDesc() {

    return "China - API Check Element";
  }

}
