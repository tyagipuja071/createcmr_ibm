package com.ibm.cio.cmr.request.automation.util.geo;

import java.util.ArrayList;
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
import com.ibm.cio.cmr.request.entity.Data;
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
        if(comment.equals("Strategic") || comment.equals("Signature"))
        {
          if (industryCodeISUMap.containsKey(firstCharSubIndustry)) {
            isu = industryCodeISUMap.get(firstCharSubIndustry);
          } 
          
        }

          LOG.debug("Setting ISU CTC based on GBG. (GBG Found)");
          details.append("Setting ISU CTC based on GBG.");
         
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), isu);
          overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), ctc);
         
          LOG.debug( "ISU : " + isu + " " + "CTC : " + ctc + " ");

      }
    } else {
      LOG.debug("GBG is BGNONE...");
      if (covId != null) {

        List<String> l1=Arrays.asList("ES","MG");
        List<String> l2=Arrays.asList("AM", "PA", "AC", "RO", "RR", "AP", "TO", "MA", "PI", "CE", "RN", "PB", "PE", "AL", "SE", "BA");
        List<String> l3=Arrays.asList("RJ");
        List<String> l4=Arrays.asList("PR", "SC", "RS");
        
        List<String> l5=Arrays.asList("DF", "GO", "MT", "MS");
         
        List<String> s1=Arrays.asList("Y","G","E");
        List<String> s2=Arrays.asList("F","S","N","B","C","J","V","P","M","L","H","X","R","T","D","W","A","U","K");
        List<String> s3=Arrays.asList("F","S","N");
        List<String> s4=Arrays.asList("B","C");
        List<String> s5=Arrays.asList("J","V","P","M","L");
        List<String> s6=Arrays.asList("H","X");
        List<String> s7=Arrays.asList("R","T");
        List<String> s8=Arrays.asList("D","W");
        List<String> s9=Arrays.asList("A","U","K");
        
        if (StringUtils.isNotBlank(stateProv)) {
         if(l1.contains(stateProv))
         {
           setIsuAndCtc(overrides,data);
         }
         if(l2.contains(stateProv))
         {
           setIsuAndCtc(overrides,data);
         }
         if(l3.contains(stateProv))
         {
           setIsuAndCtc(overrides,data);
         }
         if(l4.contains(stateProv))
         {
           setIsuAndCtc(overrides,data);
         }
        }
        
        if(StringUtils.isNotBlank(stateProv) && StringUtils.isNotBlank(firstCharSubIndustry))
        {
          if(l5.contains(stateProv) && s1.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if(l5.contains(stateProv) && s2.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if("SP".contains(stateProv) && s3.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if("SP".contains(stateProv) && s4.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if("SP".contains(stateProv) && s5.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if("SP".contains(stateProv) && s6.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if("SP".contains(stateProv) && s7.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if("SP".contains(stateProv) && s8.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
          if("SP".contains(stateProv) && s9.contains(firstCharSubIndustry) )
          {
            setIsuAndCtc(overrides,data);
          }
        }
      }
    }
    return true;

  }
  
  private void setIsuAndCtc(OverrideOutput overrides, Data data) {
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "ISU_CD", data.getIsuCd(), "27");
    overrides.addOverride(AutomationElementRegistry.GBL_FIELD_COMPUTE, "DATA", "CLIENT_TIER", data.getClientTier(), "E");
    
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
