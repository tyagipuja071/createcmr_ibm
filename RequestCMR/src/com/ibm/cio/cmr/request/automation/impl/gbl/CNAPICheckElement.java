package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Text;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.CompanyVerifier;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.ValidatingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrRdc;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.IntlAddr;
import com.ibm.cio.cmr.request.entity.IntlAddrRdc;
import com.ibm.cio.cmr.request.model.CompanyRecordModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
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

    // ScenarioExceptionsUtil scenarioExceptions =
    // getScenarioExceptions(entityManager, requestData, engineData);
    // AutomationUtil countryUtil =
    // AutomationUtil.getNewCountryUtil(data.getCmrIssuingCntry());

    CNHandler handler = (CNHandler) RequestUtils.getGEOHandler(data.getCmrIssuingCntry());
    AutomationResult<ValidationOutput> result = buildResult(admin.getId().getReqId());
    // boolean matchDepartment = false;
    // if (engineData.get(AutomationEngineData.MATCH_DEPARTMENT) != null) {
    // matchDepartment = (boolean)
    // engineData.get(AutomationEngineData.MATCH_DEPARTMENT);
    // }

    ValidationOutput validation = new ValidationOutput();
    IntlAddr iAddr = new IntlAddr();
    String cnName = null;
    String cnAddr = null;
    iAddr = handler.getIntlAddrById(soldTo, entityManager);
    if (iAddr != null) {
      cnName = iAddr.getIntlCustNm1() + (iAddr.getIntlCustNm2() != null ? iAddr.getIntlCustNm2() : "");
      cnAddr = iAddr.getAddrTxt() + (iAddr.getIntlCustNm4() != null ? iAddr.getIntlCustNm4() : "");
    }

    LOG.debug("Entering DnB Check Element");
    if (requestData.getAdmin().getReqType().equalsIgnoreCase("C")) {
      // if (!scenarioExceptions.isSkipDuplicateChecks()) {
      if (StringUtils.isNotBlank(admin.getDupCmrReason())) {
        StringBuilder details = new StringBuilder();
        details.append("User requested to proceed with Duplicate CMR Creation.").append("\n\n");
        details.append("Reason provided - ").append("\n");
        details.append(admin.getDupCmrReason()).append("\n");
        result.setDetails(details.toString());
        result.setResults("Overridden");
        result.setOnError(false);
      } else if (soldTo != null) {
  
        String SCENARIO_LOCAL_NRML = "NRML";
        String SCENARIO_LOCAL_EMBSA = "EMBSA";
        String SCENARIO_CROSS_CROSS = "CROSS";
        String SCENARIO_LOCAL_AQSTN = "AQSTN";
        String SCENARIO_LOCAL_BLUMX = "BLUMX";
        String SCENARIO_LOCAL_MRKT = "MRKT";
        String SCENARIO_LOCAL_BUSPR = "BUSPR";
        String SCENARIO_LOCAL_INTER = "INTER";
        String SCENARIO_LOCAL_PRIV = "PRIV";
        boolean ifAQSTNHasCN = true;
        
        if (data.getCustSubGrp() != null && (SCENARIO_LOCAL_NRML.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_EMBSA.equals(data.getCustSubGrp())
            || SCENARIO_LOCAL_AQSTN.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_BLUMX.equals(data.getCustSubGrp())
            || SCENARIO_LOCAL_MRKT.equals(data.getCustSubGrp()) || SCENARIO_LOCAL_BUSPR.equals(data.getCustSubGrp()))) {
          CompanyRecordModel searchModel = new CompanyRecordModel();
          searchModel.setIssuingCntry(data.getCmrIssuingCntry());
          searchModel.setCountryCd(soldTo.getLandCntry());
          if (StringUtils.isNotEmpty(data.getBusnType())) {
            searchModel.setTaxCd1(data.getBusnType());
          } else {
            
  //          IntlAddr iAddr = new IntlAddr();
            // CompanyRecordModel cmrData = null;
            
            // searchModel.setCmrNo(cmrNo);
            if (iAddr != null) {
              searchModel.setTaxCd1(cnName);
            }
          }
          try {
            AutomationResponse<CNResponse> cmrsData = CompanyFinder.getCNApiInfo(searchModel, "TAXCD");
            if (cmrsData != null && cmrsData.isSuccess()) {
              // cmrData = cmrsData.get(0);
              
  //            result.setResults("Data Found");
              StringBuilder details = new StringBuilder();
  //            result.setResults("Found China API Data.");
  //            // engineData.addRejectionComment("DUPC", "Customer already exists /
  //            // duplicate CMR", "", "");
  //            // to allow overides later
  //            // requestData.getAdmin().setMatchIndc("C");
  //            result.setOnError(false);
  //            details.append("\n");
  //            // logDuplicateCMR(details, cmrData);
  //            result.setProcessOutput(validation);
  //            result.setDetails(details.toString().trim());
              
              if(cnName.equals(cmrsData.getRecord().getName()) && cnAddr.equals(cmrsData.getRecord().getRegLocation())) {
                
                result.setResults("Matches found");
                result.setDetails("High confidence Chinese name and address were found. No override from users was recorded.");
                /*
                 * engineData.addNegativeCheckStatus("DNBCheck",
                 * "Processor review is required as no high confidence D&B matches were found."
                 * );
                 */
                requestData.getAdmin().setMatchIndc("C");
                result.setOnError(false);
                details.append("\n");
                // logDuplicateCMR(details, cmrData);
                result.setProcessOutput(validation);
                result.setDetails(details.toString().trim());
                engineData.addNegativeCheckStatus("CNAPICheck", "High confidence Chinese name and address were found.");
                LOG.debug("High confidence Chinese name and address were found.\n");
              
              }else {
                result.setOnError(true);
                result.setResults("Review Needed");
                result.setDetails("Processor review is required as no high confidence Chinese name and address were found.");
                /*
                 * engineData.addNegativeCheckStatus("DNBCheck",
                 * "Processor review is required as no high confidence D&B matches were found."
                 * );
                 */
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
            //LOG.debug("Error on China API Validating" + e.getMessage());
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
        details.append("User requested to proceed with Duplicate CMR Creation.").append("\n\n");
        details.append("Reason provided - ").append("\n");
        details.append(admin.getDupCmrReason()).append("\n");
        result.setDetails(details.toString());
        result.setResults("Overridden");
        result.setOnError(false);
      } else if (soldTo != null) {
        AutomationResponse<CNResponse> cmrsData = null;
        CompanyRecordModel searchModel = new CompanyRecordModel();
        IntlAddr soldToIntlAddr = new IntlAddr();
        IntlAddrRdc soldToIntlAddrRdc = new IntlAddrRdc();
        
        if(StringUtils.isNotEmpty(data.getPpsceid())) {
          details.append("Skipping Chinese name and address check for BP scenario").append("\n");
          LOG.debug("Skipping Chinese name and address check for BP scenario");
          result.setResults("Skipped");
          result.setOnError(false);
        }else {
          
          searchModel.setIssuingCntry(data.getCmrIssuingCntry());
          searchModel.setCountryCd(soldTo.getLandCntry());
          if (StringUtils.isNotEmpty(data.getBusnType())) {
            searchModel.setTaxCd1(data.getBusnType());
          } else {
            if (iAddr != null) {
              searchModel.setTaxCd1(cnName);
            }
          }
        }
  
        List<IntlAddr> intlAddrList = new ArrayList<IntlAddr>();
        List<IntlAddrRdc> intlAddrRdcList = new ArrayList<IntlAddrRdc>();
        
        intlAddrList = handler.getINTLAddrCountByReqId(entityManager, requestData.getAdmin().getId().getReqId());
        intlAddrRdcList = handler.getINTLAddrRdcByReqId(entityManager, requestData.getAdmin().getId().getReqId());
  
        if (intlAddrList != null && intlAddrList.size() > 0) {
          for (IntlAddr intlAddr : intlAddrList) {
            if (intlAddr.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZS01.toString())) {
              soldToIntlAddr = intlAddr;
            }
          }
        } 
        
        if (intlAddrRdcList != null && intlAddrRdcList.size() > 0) {
          for (IntlAddrRdc intlAddrRdc : intlAddrRdcList) {
            if (intlAddrRdc.getId().getAddrType().contains(CmrConstants.ADDR_TYPE.ZS01.toString())) {
              soldToIntlAddrRdc = intlAddrRdc;
            }
          }
        }
        
        checkChineseName(soldToIntlAddr, soldToIntlAddrRdc, searchModel, cmrsData, result, details, engineData);
        checkChineseAddress(soldToIntlAddr, soldToIntlAddrRdc, searchModel, cmrsData, result, details, engineData);
        
        
        
        
        result.setProcessOutput(validation);
        result.setDetails(details.toString().trim());
      } else {
        details.append("Missing main address on the request.").append("\n");
        engineData.addRejectionComment("OTH", "Missing main address on the request.", "", "");
        result.setResults("No Matches");
        result.setOnError(true);
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
  
  public void checkChineseName(IntlAddr soldToIntlAddr, IntlAddrRdc soldToIntlAddrRdc, CompanyRecordModel searchModel, AutomationResponse<CNResponse> cmrsData, 
      AutomationResult<ValidationOutput> result, StringBuilder details, AutomationEngineData engineData) {
    
    String newCnName = soldToIntlAddr.getIntlCustNm1() + (soldToIntlAddr.getIntlCustNm2() != null ? soldToIntlAddr.getIntlCustNm2() : "");
    String oldCnName = soldToIntlAddrRdc.getIntlCustNm1() + (soldToIntlAddrRdc.getIntlCustNm2() != null ? soldToIntlAddrRdc.getIntlCustNm2() : "");
    
    if (!oldCnName.equals(newCnName)) {
      try {
        cmrsData = new AutomationResponse<CNResponse>();
        cmrsData = CompanyFinder.getCNApiInfo(searchModel, "TAXCD");
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
              result.setOnError(false);
              details.append("Original Chinese name matchs with historical name in API.").append("\n");
//              result.setProcessOutput(validation);
//              result.setDetails(details.toString().trim());
              if (newCnName.equals(cmrsData.getRecord().getName())) {
                details.append("Current Chinese name matchs with Chinese name in API.").append("\n");                   
              }else {                   
                overrideName(cmrsData.getRecord().getName(),soldToIntlAddr);
                result.setResults("Overridden");
                result.setOnError(true);
                details.append("Chinese name has been override by trust source,if you don't agree please attach supporting document.").append("\n");
                
              }
            }
          }
        }else {
          details.append("No Chinese history names were found in API.").append("\n");
          result.setResults("Fault Data");
          engineData.addRejectionComment("OTH", "No Chinese history names were found in API.", "", "");
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
      result.setOnError(false);
    }
    
  }
  
  public void checkChineseAddress(IntlAddr soldToIntlAddr, IntlAddrRdc soldToIntlAddrRdc, CompanyRecordModel searchModel, AutomationResponse<CNResponse> cmrsData, 
      AutomationResult<ValidationOutput> result, StringBuilder details, AutomationEngineData engineData) {
    
    String newCnAddr = soldToIntlAddr.getAddrTxt() + (soldToIntlAddr.getIntlCustNm4() != null ? soldToIntlAddr.getIntlCustNm4() : "");
    String oldCnAddr = soldToIntlAddrRdc.getAddrTxt() + (soldToIntlAddrRdc.getIntlCustNm4() != null ? soldToIntlAddrRdc.getIntlCustNm4() : "");
    
    if (!oldCnAddr.equals(newCnAddr)) {
      try {
        if (cmrsData == null) {
          cmrsData = CompanyFinder.getCNApiInfo(searchModel, "TAXCD");
        }       
      } catch (Exception e) {
        e.printStackTrace();
        details.append("Error on get China API Data Check.").append("\n");
        engineData.addRejectionComment("OTH", "Error on  get China API Data Check.", "", "");
        result.setOnError(true);
        result.setResults("Error on  get China API Data Check.");
      }
      
      if (cmrsData != null && cmrsData.isSuccess()) {
       
        if (newCnAddr.equals(cmrsData.getRecord().getName())) {
          result.setResults("Matches found");
          result.setOnError(false);
  //                result.setProcessOutput(validation);
  //                result.setDetails(details.toString().trim());
          details.append("Current Chinese address matchs with Chinese address in API.").append("\n");                   
        }else {                   
          overrideName(cmrsData.getRecord().getName(),soldToIntlAddr);
          result.setResults("Overridden");
          result.setOnError(true);
          details.append("Chinese address has been override by trust source,if you don't agree please attach supporting document.").append("\n");             
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
      result.setOnError(false);
    }
    
  }
  
  public void overrideName(String name, IntlAddr intlAddr) {
    // TODO
    
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
