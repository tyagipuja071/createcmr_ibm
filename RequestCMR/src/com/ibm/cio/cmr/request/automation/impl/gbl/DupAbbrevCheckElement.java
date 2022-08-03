package com.ibm.cio.cmr.request.automation.impl.gbl;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.automation.impl.OverridingElement;
import com.ibm.cio.cmr.request.automation.out.AutomationResult;
import com.ibm.cio.cmr.request.automation.out.OverrideOutput;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;

public class DupAbbrevCheckElement extends OverridingElement{
	private static final Logger log = Logger.getLogger(FieldComputationElement.class);
	public static final List<String> EMEA_COUNTRIES = Arrays.asList(SystemLocation.BELGIUM , SystemLocation.UNITED_KINGDOM, SystemLocation.SPAIN , SystemLocation.NETHERLANDS ,SystemLocation.IRELAND);
	public DupAbbrevCheckElement(String requestTypes, String actionOnError, boolean overrideData, boolean stopOnError){ 
	 super(requestTypes, actionOnError, overrideData, stopOnError);
	     }
	 @Override
	  public AutomationResult<OverrideOutput> executeElement(EntityManager entityManager, RequestData requestData, AutomationEngineData engineData)
	      throws Exception {
		    Data data = requestData.getData();
		    long reqId = requestData.getAdmin().getId().getReqId();
		    OverrideOutput overrides = new OverrideOutput(false);
		    AutomationResult<OverrideOutput> results = buildResult(reqId);
		    log.debug("Entering AbbrevCheckElement()");
		    if(EMEA_COUNTRIES.contains(data.getCmrIssuingCntry())){
		    String sqlKey = ExternalizedQuery.getSql("AUTO.EMEA.DUP_ABBREV_CHECK");
		    PreparedQuery query = new PreparedQuery(entityManager, sqlKey);
		    query.setParameter("CNTRY", data.getCmrIssuingCntry());
		    query.setParameter("ABBREV_NM", data.getAbbrevNm() != null ? data.getAbbrevNm().toUpperCase() : data.getAbbrevNm());
        query.setParameter("ABBREV_LOCN", data.getAbbrevLocn() != null ? data.getAbbrevLocn().toUpperCase() : data.getAbbrevLocn());
        query.setForReadOnly(true);
		      if (query.exists()) {
		        //condition 
		    	  String abbrNm = data.getAbbrevNm();
		    	  char ch = '1';
		    	  abbrNm = abbrNm.substring(0, abbrNm.length() - 1) + ch ;
		    	  overrides.addOverride(AutomationElementRegistry.EMEA_ABBREV_CHECK, "DATA", "ABBREV_NM", data.getAbbrevNm() , abbrNm);
		    	  }
		      results.setResults("Calculated.");
              results.setProcessOutput(overrides);
		    } else{
		    	results.setResults("Skipped");
	            results.setDetails("Skipping for Non-Emea Countries");
		    }
		    return results ;
	  }

	  @Override
	  public String getProcessCode() {
	    return AutomationElementRegistry.EMEA_ABBREV_CHECK;
	  }

	  @Override
	  public String getProcessDesc() {
	    return "EMEA Abbrev Check";
	  }
	}


