package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.gbl.RetrieveIBMValuesElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.automation.out.ValidationOutput;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.automation.util.BrazilFieldsContainer;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cmr.services.client.AutomationServiceClient;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.ServiceClient.Method;
import com.ibm.cmr.services.client.automation.AutomationResponse;
import com.ibm.cmr.services.client.automation.la.br.ConsultaCCCResponse;
import com.ibm.cmr.services.client.automation.la.br.MidasRequest;
import com.ibm.cmr.services.client.automation.la.br.SintegraResponse;

public class BrazilUtil extends AutomationUtil {

  private static final Logger LOG = Logger.getLogger(BrazilUtil.class);

  @Override
  public AutomationResult<OverrideOutput> doCountryFieldComputations(EntityManager entityManager, AutomationResult<OverrideOutput> results,
      StringBuilder details, OverrideOutput overrides, RequestData requestData, AutomationEngineData engineData) throws Exception {
    return null;
  }

  @Override
  public boolean performScenarioValidation(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData,
      AutomationResult<ValidationOutput> result, StringBuilder details, ValidationOutput output) {
    return true;
  }

  /**
   * Calls the sintegra service
   * 
   * @param vat
   * @param state
   * @return
   * @throws Exception
   */
  public static AutomationResponse<SintegraResponse> querySintegra(String vat, String state) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 5);
    client.setRequestMethod(Method.Get);

    // calling SINTEGRA
    LOG.debug("Calling Sintegra Service for VAT " + vat);
    MidasRequest requestSoldTo = new MidasRequest();
    requestSoldTo.setCnpj(vat);
    requestSoldTo.setUf(state);

    LOG.debug("Connecting to the Sintegra Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponseSoldTo = client.executeAndWrap(AutomationServiceClient.BR_SINTEGRA_SERVICE_ID, requestSoldTo,
        AutomationResponse.class);
    ObjectMapper mapperSoldTo = new ObjectMapper();
    String jsonSoldTo = mapperSoldTo.writeValueAsString(rawResponseSoldTo);
    LOG.trace("Sintegra Service Response for VAT " + vat + ": " + jsonSoldTo);

    TypeReference<AutomationResponse<SintegraResponse>> refSoldTo = new TypeReference<AutomationResponse<SintegraResponse>>() {
    };
    AutomationResponse<SintegraResponse> response = mapperSoldTo.readValue(jsonSoldTo, refSoldTo);

    return response;
  }

  /**
   * Calls the ConsultaCCC service
   * 
   * @param vat
   * @param state
   * @return
   * @throws Exception
   */
  public static AutomationResponse<ConsultaCCCResponse> querySintegraByConsulata(String vat, String state) throws Exception {
    AutomationServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
        AutomationServiceClient.class);
    client.setReadTimeout(1000 * 60 * 3);
    client.setRequestMethod(Method.Get);

    // calling SINTEGRA
    LOG.debug("Calling ConsultaCCC Service for VAT " + vat);
    MidasRequest requestSoldTo = new MidasRequest();
    requestSoldTo.setCnpj(vat);
    requestSoldTo.setUf(state);

    LOG.debug("Connecting to the ConsultaCCC Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
    AutomationResponse<?> rawResponseSoldTo = client.executeAndWrap(AutomationServiceClient.BR_CONSULTA_SERVICE_ID, requestSoldTo,
        AutomationResponse.class);
    ObjectMapper mapperSoldTo = new ObjectMapper();
    String jsonSoldTo = mapperSoldTo.writeValueAsString(rawResponseSoldTo);
    LOG.trace("ConsultaCCC Service Response for VAT " + vat + ": " + jsonSoldTo);

    TypeReference<AutomationResponse<ConsultaCCCResponse>> refSoldTo = new TypeReference<AutomationResponse<ConsultaCCCResponse>>() {
    };
    AutomationResponse<ConsultaCCCResponse> response = mapperSoldTo.readValue(jsonSoldTo, refSoldTo);

    return response;
  }

  public static boolean hasScenarioCheck(String issuingCntry) {
    return true;
  }

  @Override
  public boolean fillCoverageAttributes(RetrieveIBMValuesElement retrieveElement, EntityManager entityManager,
      AutomationResult<OverrideOutput> results, StringBuilder details, OverrideOutput overrides, RequestData requestData,
      AutomationEngineData engineData, String covType, String covId, String covDesc, String gbgId) throws Exception {
    Data data = requestData.getData();
    Admin admin = requestData.getAdmin();
   String custGrp= data.getCustGrp();
   String custSubGrp= data.getCustSubGrp();
   String custType=admin.getCustType();
   RequestEntryModel model = requestData.createModelFromRequest();
   List<String> custArray = Arrays.asList("INTER","PRIPE","IBMEM","BUSPR");
   if(!custArray.contains(custType))
   {
    List<Addr> addr = requestData.getAddresses();
    String stateProv=null;  
      for(Addr address1:addr)
      {
        if(address1.getId().getAddrType().equals("ZS01"))
        {
          stateProv=address1.getStateProv();
          
        }
      }
    String isu = "";
    String ctc = "";
    
    String subIndustryCd = data != null && data.getSubIndustryCd() != null ? data.getSubIndustryCd() : "";
    String firstCharSubIndustry = StringUtils.isNotEmpty(subIndustryCd) ? subIndustryCd.substring(0, 1) : "";
    Map<String, String> industryCodeISUMap = new HashMap<String, String>();
    
    industryCodeISUMap.put("A", "3T");
    industryCodeISUMap.put("B", "5B");
    industryCodeISUMap.put("C", "5B");
    industryCodeISUMap.put("D", "18");
    industryCodeISUMap.put("E", "40");

    industryCodeISUMap.put("F", "04");
    industryCodeISUMap.put("G", "28");
    industryCodeISUMap.put("H", "11");
    industryCodeISUMap.put("J", "4A");

    industryCodeISUMap.put("K", "05");
    industryCodeISUMap.put("L", "5E");
    industryCodeISUMap.put("M", "4D");
    industryCodeISUMap.put("N", "31");

    industryCodeISUMap.put("P", "15");
    industryCodeISUMap.put("R", "1R");
    industryCodeISUMap.put("S", "4F");
    industryCodeISUMap.put("T", "19");
    industryCodeISUMap.put("U", "12");

    industryCodeISUMap.put("V", "14");
    industryCodeISUMap.put("W", "18");
    industryCodeISUMap.put("X", "8C");
    industryCodeISUMap.put("Y", "28");
    industryCodeISUMap.put("Z", "21");
    
    LOG.debug("BR performing fillCoverageAttributes based on GBG");
    LOG.debug("GBG Id: " + gbgId);

    if (gbgId != null && !"BGNONE".equals(gbgId.trim())) {
      BrazilFieldsContainer cmt = getGbgSortlMapping(entityManager, requestData, gbgId.trim());
      String comment = StringUtils.isNotBlank(cmt.getComment()) ? cmt.getComment() : "";
      if (StringUtils.isNotBlank(comment)) {
 
        if(comment.equals("Client Squads"))
        {
              isu = "32";
              ctc = "J";
        }
        if(comment.equals("Select BPS - T"))
        {
              isu = "34";
              ctc = "Q";
        }
        if(comment.equals("Build/Service"))
        {
              isu = "36";
              ctc = "Y";
        }
        
        if(comment.equals("Strategic") || comment.equals("Signature"))
        {
          if (industryCodeISUMap.containsKey(firstCharSubIndustry)) {
            isu = industryCodeISUMap.get(firstCharSubIndustry);
            ctc=" ";
          } 
          
        }

          LOG.debug("\n Setting ISU CTC based on GBG. (GBG Found)");
          details.append("\n Setting ISU CTC based on GBG. (GBG Found) \n");
          
          setIsuAndCtc(overrides,data,details,isu,ctc);   
          
          LOG.debug( "ISU : " + isu + " " + "CTC : " + ctc + " ");

      }
    } else {

      if(!"CROSS".equalsIgnoreCase(custGrp) )
      {
      LOG.debug("GBG is BGNONE...");
      if (covId != null) {

        List<String> statprovCd1=Arrays.asList("ES","MG");
        List<String> statprovCd2=Arrays.asList("AM", "PA", "AC", "RO", "RR", "AP", "TO", "MA", "PI", "CE", "RN", "PB", "PE", "AL", "SE", "BA");
        List<String> statprovCd3=Arrays.asList("RJ");
        List<String> statprovCd4=Arrays.asList("PR", "SC", "RS");
        List<String> statprovCd5=Arrays.asList("DF", "GO", "MT", "MS");
         
        List<String> indCd1=Arrays.asList("Y","G","E");
        List<String> indCd2=Arrays.asList("F","S","N","B","C","J","V","P","M","L","H","X","R","T","D","W","A","U","K");
        List<String> indCd3=Arrays.asList("F","S","N");
        List<String> indCd4=Arrays.asList("B","C");
        List<String> indCd5=Arrays.asList("J","V","P","M","L");
        List<String> indCd6=Arrays.asList("H","X");
        List<String> indCd7=Arrays.asList("R","T");
        List<String> indCd8=Arrays.asList("D","W");
        List<String> indCd9=Arrays.asList("A","U","K");
        String isu27="27";
        String ctc27="E";
        
        if (StringUtils.isNotBlank(stateProv)) {
         if(statprovCd1.contains(stateProv))
         {
           covType="T";
           covId="0001556";
           covDesc="BR - DSS MG";
           setCoverageDetails(details,overrides,data,covType,covId,covDesc);
           setIsuAndCtc(overrides,data,details,isu27,ctc27);
         }
         if(statprovCd2.contains(stateProv))
         {
           covType="T";
           covId="0006763";
           covDesc="BR - DSS N/NE";
           setCoverageDetails(details,overrides,data,covType,covId,covDesc);
           setIsuAndCtc(overrides,data,details,isu27,ctc27);
         }
         if(statprovCd3.contains(stateProv))
         {
           covType="T";
           covId="0006761";
           covDesc="BR - DSS RJ";
           setCoverageDetails(details,overrides,data,covType,covId,covDesc);
           setIsuAndCtc(overrides,data,details,isu27,ctc27);
         }
         if(statprovCd4.contains(stateProv))
         {
           covType="T";
           covId="0006758";
           covDesc="BR - DSS Sul";
           setCoverageDetails(details,overrides,data,covType,covId,covDesc);
           setIsuAndCtc(overrides,data,details,isu27,ctc27);
         }
        }
        
        if(StringUtils.isNotBlank(stateProv) && StringUtils.isNotBlank(firstCharSubIndustry))
        {
          if(statprovCd5.contains(stateProv) && indCd1.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0006504";
            covDesc="BR - DSS Gov/Educ. Centro Oeste";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
          }
          if(statprovCd5.contains(stateProv) && indCd2.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0011212";
            covDesc="BR - DSS Multi Ind Centro Oeste";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
          }
          if("SP".contains(stateProv) && indCd3.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0006764";
            covDesc="BR - DSS SP Finance";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
            
          }
          if("SP".contains(stateProv) && indCd4.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0011205";
            covDesc="BR - DSS SP Computer Services";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
          }
          if("SP".contains(stateProv) && indCd5.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0011207";
            covDesc="BR - DSS SP Industrial";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27); 
          }
          if("SP".contains(stateProv) && indCd6.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0011208";
            covDesc="BR - DSS SP HealthCare & LifeSciences";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
          }
          if("SP".contains(stateProv) && indCd7.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0011209";
            covDesc="BR - DSS SP Retail";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
          }
          if("SP".contains(stateProv) && indCd8.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0011210";
            covDesc="BR - DSS SP Consumer Product";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
          }
          if("SP".contains(stateProv) && indCd9.contains(firstCharSubIndustry) )
          {
            covType="T";
            covId="0011211";
            covDesc="BR - DSS SP Communications";
            setCoverageDetails(details,overrides,data,covType,covId,covDesc);
            setIsuAndCtc(overrides,data,details,isu27,ctc27);
          }
        }
      }
    }
    }   
   }
   
   if("CROSS".equalsIgnoreCase(custGrp) && "BUSPR".equalsIgnoreCase(custType))
   
   {
     covType="P";
     covId="0000077";
     covDesc="Pool - BR";
     setCoverageDetails(details,overrides,data,covType,covId,covDesc);
     
   } else if(custArray.contains(custType) && !"BUSPR".equalsIgnoreCase(custType)){
     covType="T";
     covId="0000461";
     covDesc="DEFAULT - BR";
     setCoverageDetails(details,overrides,data,covType,covId,covDesc);
     
   } 
   
   return true;

  }
  
  private void setCoverageDetails(StringBuilder details, OverrideOutput overrides, Data data,String covType, String covId, String covDesc) {
    details.append("\n \n"+ "GBG id is not found. Covergae id, ISU & CTC will be assigned based on State Province/Industry code "+ "\n \n" +"Coverage = " + covType.trim() + covId.trim() + " (" + covDesc + ")\n");
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "COV_ID", data.getCovId(), covType.trim() + covId.trim());
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "COV_DESC", data.getCovDesc(), covDesc);
  }

  private void setIsuAndCtc(OverrideOutput overrides, Data data, StringBuilder details, String isu, String ctc) {
    details.append("ISU = " + isu +"\n");
    details.append("CTC = " + ctc +"\n");
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), isu);
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), ctc);
   
    
  }

   private BrazilFieldsContainer getGbgSortlMapping(EntityManager entityManager, RequestData requestData, String code) {
     BrazilFieldsContainer lov = new BrazilFieldsContainer();
     String sql = ExternalizedQuery.getSql("GET.CMT.BY.GBG");
     PreparedQuery query = new PreparedQuery(entityManager, sql);
     query.setParameter("CMR_ISSUING_CNTRY", requestData.getData().getCmrIssuingCntry());
     query.setParameter("FIELD_ID", "##GBG");
     query.setParameter("CD", code);
     List<Object[]> codes = query.getResults(1);
     if (codes != null && codes.size() > 0) {
       lov.setCode((String) codes.get(0)[0]);
       lov.setComment((String) codes.get(0)[2]);
     }
     return lov;
   }
}
