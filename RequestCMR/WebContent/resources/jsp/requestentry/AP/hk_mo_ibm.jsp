<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %> 
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
%>

<style>
img.cmr-change-icon {
}
</style>
<script>
dojo.addOnLoad(function(){
  FilteringDropdown.loadItems(null, null, 'bds', 'fieldId=Subindustry');
  FilteringDropdown.loadItems(null, null, 'bds', 'fieldId=ISIC');
  FilteringDropdown.loadItems(null, null, 'lov', 'fieldId=AddressType');
});
</script>
	<cmr:section id="IBM_REQ_TAB" hidden="true">
		<jsp:include page="/resources/jsp/requestentry/detailstrip.jsp" />
    
    <!-- CMR No, CAP, CMR Owner -->
		<cmr:row addBackground="true">
		  <cmr:column span="1" containerForField="CMRNumber">
			<p>
			<cmr:label fieldId="cmrNo">
                <cmr:fieldLabel fieldId="CMRNumber" />: 
                <span id="cmrDelta"><cmr:delta text="${rdcdata.cmrNo}" oldValue="${reqentry.cmrNo}" /></span>
            </cmr:label>
          	<cmr:field fieldId="CMRNumber" size="18" id="cmrNo" path="cmrNo" tabId="MAIN_IBM_TAB" size="150" />
		    </p>
		  </cmr:column>
  		  <cmr:column span="1" containerForField="CAP">
  		    <p>
            <cmr:label fieldId="capInd2">&nbsp;</cmr:label>
            <cmr:field fieldId="CAP" path="capInd" tabId="MAIN_IBM_TAB"/>
            <cmr:label fieldId="capInd" forRadioOrCheckbox="true">
               <cmr:fieldLabel fieldId="CAP" />
               <cmr:delta text="${rdcdata.capInd}" oldValue="${reqentry.capInd == 'Y' ? 'Yes' : 'No'}"/>
            </cmr:label>
  			</p>
  		  </cmr:column>
		  <cmr:column span="2" containerForField="CMROwner">
		  	<p>
			<cmr:label fieldId="cmrOwner">
			  <cmr:fieldLabel fieldId="CMROwner" />:
              <cmr:delta text="${rdcdata.cmrOwner}"  oldValue="${reqentry.cmrOwner}"/>
            </cmr:label>
            <cmr:field fieldId="CMROwner" id="cmrOwner" path="cmrOwner" tabId="MAIN_IBM_TAB" />
			</p>
		  </cmr:column>
    	</cmr:row>
    
     <!-- Cluster, GB Segment, ISU Code -->
   		<cmr:row topPad="10" addBackground="true">
	   	  <cmr:column span="2" containerForField="Cluster">
	      	<p>
	        <cmr:label fieldId="apCustClusterId"> 
	         <cmr:fieldLabel fieldId="Cluster" />:
             <cmr:view forCountry="744">
                <cmr:info text="${ui.info.ClusterIndia}" />
             </cmr:view> 
	         <cmr:delta text="${rdcdata.apCustClusterId}" oldValue="${reqentry.apCustClusterId}"/> 
	         <cmr:view forCountry="796">
				<cmr:info text="${ui.info.NZForCluster}" />
	         </cmr:view>
	         <cmr:view forCountry="616,749,778,834,615,652">
				<cmr:info text="${ui.info.AUForCluster}" />
	         </cmr:view>
	         <cmr:view forCountry="852,856,818">
            	<cmr:info text="${ui.info.clusterReminderMsgTH_VN_PH}"></cmr:info>
        	 </cmr:view> 
	        </cmr:label>
	        <cmr:field path="apCustClusterId" id="apCustClusterId" fieldId="Cluster" tabId="MAIN_IBM_TAB" />
	      	</p>
	      </cmr:column>
	      <cmr:column span="2" containerForField="ClientTier">
          <p>
            <cmr:label fieldId="clientTier">
               <cmr:fieldLabel fieldId="ClientTier" />: 
               <cmr:delta text="${rdcdata.clientTier}" oldValue="${reqentry.clientTier}" code="L"/>
            </cmr:label>
            <cmr:field fieldId="ClientTier" id="clientTier" path="clientTier" tabId="MAIN_IBM_TAB" />
          </p>
        </cmr:column>
   		  <cmr:column span="2" containerForField="ISU">
        	<p>
          	<cmr:label fieldId="isuCd">
              <cmr:fieldLabel fieldId="ISU" />:
              <cmr:delta text="${rdcdata.isuCd}" oldValue="${reqentry.isuCd}" code="L" />
          	</cmr:label>
          	<cmr:field fieldId="ISU" id="isuCd" path="isuCd" tabId="MAIN_IBM_TAB" />
          	</p>
      	  </cmr:column>
      	</cmr:row>
      	
     <!-- MRC Code, BP Relation Type, BP Function, Province Name, Province Code, Customer Service Code -->
      	<cmr:row topPad="10">
      	  <cmr:column span="2" containerForField="MrcCd">
      		<p>
        	<cmr:label fieldId="mrcCd"> <cmr:fieldLabel fieldId="MrcCd" />: </cmr:label>
        	<cmr:field path="mrcCd" id="mrcCd" fieldId="MrcCd" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="BPRelationType" exceptForCountry="643,749,778,818,834,852,856,646,714,720">
            <p>
            <cmr:label fieldId="bpRelType">
              <cmr:fieldLabel fieldId="BPRelationType" />:
              <cmr:delta text="${rdcdata.bpRelType}" oldValue="${reqentry.bpRelType}" code="L"/>
            </cmr:label>
            <cmr:field fieldId="BPRelationType" id="bpRelType" path="bpRelType" tabId="MAIN_IBM_TAB" />
            </p>
          </cmr:column>
          <cmr:column span="2" containerForField="BPName" forCountry="738,736">
      	    <p>
        	  <label for="bpName"> 
          	    <cmr:fieldLabel fieldId="BPName" />: 
          	    <cmr:delta text="${rdcdata.bpName}" oldValue="${reqentry.bpName}" code="L"/>
        	  </label>
        	  <cmr:field path="bpName" id="bpName" fieldId="BPName" tabId="MAIN_IBM_TAB" />
      	  	</p>
      	  </cmr:column>
    	  <cmr:column span="2" containerForField="ProvinceName" exceptForCountry="738,736">
      		<p>
        	  <cmr:label fieldId="busnType"> <cmr:fieldLabel fieldId="ProvinceName" />: </cmr:label>
        	  <cmr:field path="busnType" id="busnType" fieldId="ProvinceName" tabId="MAIN_IBM_TAB" />
      	  	</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="ProvinceCode" exceptForCountry="738,736">
      		<p>
        	  <cmr:label fieldId="territoryCd"><cmr:fieldLabel fieldId="ProvinceCode" />: </cmr:label>
        	  <cmr:field path="territoryCd" id="territoryCd" fieldId="ProvinceCode" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="CustomerServiceCd" forCountry="796">
      		<p>
        	  <cmr:label fieldId="engineeringBo"> <cmr:fieldLabel fieldId="CustomerServiceCd" />: </cmr:label>
        	  <cmr:field path="engineeringBo" id="engineeringBo" fieldId="CustomerServiceCd" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	</cmr:row>
    	
     <!-- Sales Rep No, Cmr No Prefix, IBM Collection Responsibility -->
    	<cmr:row topPad="10">
  		  <cmr:column span="2" containerForField="SalRepNameNo">
      		<p>
        	  <cmr:label fieldId="repTeamMemberNo">
          	    <cmr:fieldLabel fieldId="SalRepNameNo" />:<cmr:info text="No impact on CMR/account owner, just AP CMR legacy system(WTAAS) requirement. "></cmr:info> 
           		<cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" />
		       	<cmr:view exceptForCountry="736,738">
			      <div class="cmr-inline" >
			        <cmr:bluepages model="salesRepNameNo" namePath="repTeamMemberName" idPath="repTeamMemberNo" useUID="true" editableId="true"/>
			        <span style="padding-left: 5px">&nbsp;</span>
			      </div>
			    </cmr:view>
        	  </cmr:label>
        	  <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="CmrNoPrefix">
      		<p>
        	  <label for="cmrNoPrefix"> 
          	  <cmr:fieldLabel fieldId="CmrNoPrefix" />: 
        	  </label>
        	  <cmr:field path="cmrNoPrefix" id="cmrNoPrefix" fieldId="CmrNoPrefix" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="CollectionCd">
      		<p>
        	  <cmr:label fieldId="collectionCd">
        	    <cmr:fieldLabel fieldId="CollectionCd" />: 
           	    <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-engineeringBo" />
        	  </cmr:label>
        	  <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_IBM_TAB" />
      	 	</p>
    	  </cmr:column>
  		</cmr:row>
    	
     <!-- INAC Type, INAC Code, ISBU, Industry Class -->
      	<cmr:row topPad="10" addBackground="true">
      	<cmr:view forCountry="643,818,615,852,856,749,778,834,652,744,616,796">
      	  <cmr:column  span="1" width="100" containerForField="INACType">
        	<p>
              <cmr:label fieldId="inacType">
                <cmr:fieldLabel fieldId="INACType" />
                <cmr:delta text="${rdcdata.inacType}" oldValue="${reqentry.inacType}" code="L"/>
              </cmr:label>
              <cmr:field id="inacType" path="inacType" fieldId="INACType" tabId="MAIN_IBM_TAB" size="80"/>
            </p>          
      	  </cmr:column>
      	  <cmr:column span="1" width="230" containerForField="INACCode">
        	<p>
          	  <cmr:label fieldId="inacCd">
                <cmr:fieldLabel fieldId="INACCode" />:
            	<cmr:delta text="${rdcdata.inacCd}" oldValue="${reqentry.inacCd}" />
          	  </cmr:label>
              <cmr:field  size="180" fieldId="INACCode" id="inacCd" path="inacCd" tabId="MAIN_IBM_TAB" />
        	</p> 
      	  </cmr:column>
      	  </cmr:view>
      	  <cmr:view forCountry="736,738">
      	   <cmr:column span="1" width="140" containerForField="INACType">
        	<p>
              <cmr:label fieldId="inacType">
                <cmr:fieldLabel fieldId="INACType" />
                <cmr:delta text="${rdcdata.inacType}" oldValue="${reqentry.inacType}" code="L"/>
              </cmr:label>
              <cmr:field id="inacType" path="inacType" fieldId="INACType" tabId="MAIN_IBM_TAB" size="100"/>
            </p>          
      	  </cmr:column>
      	  <cmr:column span="1" width="140" containerForField="INACCode">
        	<p>
          	  <cmr:label fieldId="inacCd">
                <cmr:fieldLabel fieldId="INACCode" />:
            	<cmr:delta text="${rdcdata.inacCd}" oldValue="${reqentry.inacCd}" />
          	  </cmr:label>
          	  <cmr:field size="80" fieldId="INACCode" id="inacCd" path="inacCd" tabId="MAIN_IBM_TAB" />
        	</p> 
      	  </cmr:column>
      	  </cmr:view>
      	  <cmr:column span="2" containerForField="ISBU">
      		<p>
        	  <cmr:label fieldId="isbuCd"> <cmr:fieldLabel fieldId="ISBU" />: </cmr:label>
        	  <cmr:field path="isbuCd" id="isbuCd" fieldId="ISBU" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
      	  <cmr:column span="2" containerForField="IndustryClass">
      		<p>
      		  <Label style="">IndustryClass:</Label>
			        <input type="text" id="IndustryClass" name ="IndustryClass" value="${fn:substring(reqentry.subIndustryCd, 0, 1)}" readonly="readonly" style="width:15px;BACKGROUND: #FFFFEE;border: 1px Solid #DDDDDD"/>
      		</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="RestrictedInd">
	  		<p>
			  <cmr:label fieldId="restrictInd"> <cmr:fieldLabel fieldId="RestrictedInd" />: </cmr:label>
			  <cmr:field path="restrictInd" id="restrictInd" fieldId="RestrictedInd" tabId="MAIN_IBM_TAB" />
      		</p>
		  </cmr:column>
		</cmr:row>
    
     <!-- Region Code, Government Indicator, Sector, Customer Billing Contact Name, Branch ID -->
      	<cmr:row topPad="10" addBackground="true">
      	  <cmr:column span="2" containerForField="RegionCode">
      		<p>
        	  <cmr:label fieldId="miscBillCd"> <cmr:fieldLabel fieldId="RegionCode" />: </cmr:label>
        	  <cmr:field path="miscBillCd" id="miscBillCd" fieldId="RegionCode" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
      	  <cmr:column span="2" containerForField="GovIndicator">
      		<p>
        	  <cmr:label fieldId="govType"> <cmr:fieldLabel fieldId="GovIndicator" />: </cmr:label>
        	  <cmr:field path="govType" id="govType" fieldId="GovIndicator" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	   <cmr:view forCountry="616">
			 <cmr:column span="2" containerForField="LocalTax2">
          <p>
            <label for="taxCd2"> <cmr:fieldLabel fieldId="LocalTax2" />: <cmr:delta text="${rdcdata.taxCd2}" oldValue="${reqentry.taxCd2}" />
            </label>
            <cmr:field path="taxCd2" id="taxCd2" fieldId="LocalTax2" tabId="MAIN_CUST_TAB" />
          </p>
        </cmr:column>
			</cmr:view>
    	  <cmr:column span="2" containerForField="Sector">
      		<p>
        	  <cmr:label fieldId="sectorCd"> <cmr:fieldLabel fieldId="Sector" />: </cmr:label>
        	  <cmr:field path="sectorCd" id="sectorCd" fieldId="Sector" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="CustBillingContactNm" forCountry="778,834">
      		<p>
        	  <cmr:label fieldId="contactName1"> <cmr:fieldLabel fieldId="CustBillingContactNm" />: </cmr:label>
        	  <cmr:field path="contactName1" id="contactName1" fieldId="CustBillingContactNm" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	  <cmr:column span="2" containerForField="CollBoId" forCountry = "749,852,856,778,818">
      		<p>
        	  <cmr:label fieldId="collBoId"> <cmr:fieldLabel fieldId="CollBoId" />: </cmr:label>
        	  <cmr:field path="collBoId" id="collBoId" fieldId="CollBoId" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
		</cmr:row>
    
    <cmr:view exceptForCountry="643,749,778,818,834,852,856,646,714,720" exceptForGEO="FR">
    <cmr:row topPad="10" addBackground="true">
      <cmr:column span="2" containerForField="Enterprise" exceptForGEO="MCO1,NORDX" exceptForCountry="755">
        <p>
          <cmr:label fieldId="enterprise">
            <cmr:fieldLabel fieldId="Enterprise" />:
              <cmr:delta text="${rdcdata.enterprise}" oldValue="${reqentry.enterprise}" />
          </cmr:label>
          <cmr:field id="enterprise" path="enterprise" fieldId="Enterprise" tabId="MAIN_IBM_TAB" />  
          <cmr:view forCountry="618">
			       <%
  			       if (reqentry.getReqType().equalsIgnoreCase("C")) {
  			       String findCmrJs = "onclick=\"importByEnterprise()\"";
  			       if (readOnly){
  			         findCmrJs = "";
  			       }
			       %>           
            <img title="Search for the CMR" class="cmr-proceed2-icon" src="${resourcesPath}/images/search-icon2.png" <%=findCmrJs%>>
             <%
               }
             %>
          </cmr:view>        
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="Company" exceptForCountry="666,726,862,822,838" exceptForGEO="MCO1,MCO2,CEMEA,BELUX,NL">
        <p>
          <cmr:label fieldId="company">
            <cmr:fieldLabel fieldId="Company" />: 
              <cmr:delta text="${rdcdata.company}" oldValue="${reqentry.company}" />
          </cmr:label>
          <cmr:field fieldId="Company" id="company" path="company" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="Affiliate" exceptForCountry="666,726,862,822,838,641" exceptForGEO="MCO1,MCO2,CEMEA,NORDX,BELUX,NL">
        <p>
          <cmr:label fieldId="affiliate">
            <cmr:fieldLabel fieldId="Affiliate" />: 
              <cmr:delta text="${rdcdata.affiliate}" oldValue="${reqentry.affiliate}" />
          </cmr:label>
          <cmr:field fieldId="Affiliate" id="affiliate" path="affiliate" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    </cmr:view>
    
    
    <!-- Buying Group, Global Buying Group, BG LDE Rule -->
    <cmr:row topPad="10" >
      <cmr:column span="2" containerForField="BuyingGroupID">
        <p>
          <cmr:label fieldId="buyingGroupId">
             <cmr:fieldLabel fieldId="BuyingGroupID" />: 
             <cmr:delta text="${rdcdata.bgId}" oldValue="${reqentry.bgId}" />
          </cmr:label>
          <cmr:field fieldId="BuyingGroupID" id="bgId" path="bgId" tabId="MAIN_IBM_TAB" />
         <form:hidden path="bgDesc" id="bgDesc"/>
        <span class="cmr-bluepages-ro" id="bgDescCont">${reqentry.bgDesc != null ? reqentry.bgDesc : "(no description available)"}</span>
         </p>
      </cmr:column>
      <cmr:column span="2" containerForField="GlobalBuyingGroupID">
        <p>
          <cmr:label fieldId="globalBuyingGroupId">
             <cmr:fieldLabel fieldId="GlobalBuyingGroupID" />: 
             <cmr:delta text="${rdcdata.gbgId}" oldValue="${reqentry.gbgId}" />
          </cmr:label>
          <cmr:field fieldId="GlobalBuyingGroupID" id="gbgId" path="gbgId" tabId="MAIN_IBM_TAB" />
         <form:hidden path="gbgDesc" id="gbgDesc"/>
        <span class="cmr-bluepages-ro" id="gbgDescCont">${reqentry.gbgDesc != null ? reqentry.gbgDesc : "(no description available)"}</span>
         </p>
      </cmr:column>
        <cmr:column span="2" containerForField="BGLDERule">
          <p>
            <cmr:label fieldId="globalBuyingGroupId">
               <cmr:fieldLabel fieldId="BGLDERule" />: 
               <cmr:delta text="${rdcdata.bgRuleId}" oldValue="${reqentry.bgRuleId}" />
            </cmr:label>
            <cmr:field fieldId="BGLDERule" id="bgRuleId" path="bgRuleId" tabId="MAIN_IBM_TAB" />
           </p>
        </cmr:column>
    </cmr:row>
    
    <!-- Coverage ID, GLC, DUNS -->
    <cmr:row topPad="10" >
        <cmr:column span="2" containerForField="CoverageID">
          <p>
            <cmr:label fieldId="covId">
               <cmr:fieldLabel fieldId="CoverageID" />: 
               <cmr:delta text="${rdcdata.covId}" oldValue="${reqentry.covId}" />
            </cmr:label>
            <cmr:field fieldId="CoverageID" id="covId" path="covId" tabId="MAIN_IBM_TAB" />
            <form:hidden path="covDesc" id="covDesc"/>
            <span class="cmr-bluepages-ro" id="covDescCont">${reqentry.covDesc != null ? reqentry.covDesc : "(no description available)"}</span>
          </p>
        </cmr:column>
      <cmr:column span="2" containerForField="GeoLocationCode">
        <p>
          <cmr:label fieldId="geoLocationCode">
             <cmr:fieldLabel fieldId="GeoLocationCode" />: 
             <cmr:delta text="${rdcdata.geoLocCd}" oldValue="${reqentry.geoLocationCd}" />
          </cmr:label>
          <cmr:field fieldId="GeoLocationCode" id="geoLocationCd" path="geoLocationCd" tabId="MAIN_IBM_TAB" />
          <form:hidden path="geoLocDesc" id="geoLocDesc"/>
          <span class="cmr-bluepages-ro" id="geoLocDescCont">${reqentry.geoLocDesc != null ? reqentry.geoLocDesc : "(no description available)"}</span>
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="DUNS">
        <p>
          <cmr:label fieldId="dunsNo">
             <cmr:fieldLabel fieldId="DUNS" />: 
             <cmr:delta text="${rdcdata.dunsNo}" oldValue="${reqentry.dunsNo}" />
          </cmr:label>
          <cmr:field fieldId="DUNS" id="dunsNo" path="dunsNo" tabId="MAIN_IBM_TAB" />
          <%if (!readOnly){%>
          <span class="cmr-bluepages-ro" title="Validate DUNS against D&B" style="font-family:Geneva, Arial, Helvetica, sans-serif;font-size:12px;" id="geoLocDescCont"><a href="javascript: checkDUNSWithDnB()">Check D&B</a></span>
          <%}%>
        </p>
      </cmr:column>
    </cmr:row>

  <%if (!newEntry && !readOnly){%>
    <cmr:row topPad="10" >
      <cmr:column span="4">
        <cmr:button label="Retrieve Values" onClick="retrieveInterfaceValues()" highlight="true">
        </cmr:button>
      <%if ("Processor".equalsIgnoreCase(reqentry.getUserRole()) 
      && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("631")){%>
      <span class="ibm-required cmr-required-spacer">*</span>
      <%} else {%>
      <span class="ibm-required cmr-required-spacer" style="visibility:hidden">*</span>
      <%} %>
      <%if (reqentry.getCmrIssuingCntry().equalsIgnoreCase("796") && "Requester".equalsIgnoreCase(reqentry.getUserRole()) && reqentry.getCustSubGrp()!=null
       && (reqentry.getCustSubGrp().equalsIgnoreCase("NRMLC") || reqentry.getCustSubGrp().equalsIgnoreCase("AQSTN"))){%>
      <span class="ibm-required cmr-required-spacer">*</span>
      <%}%>
        <cmr:info text="${ui.info.coverageBg}"></cmr:info>
      </cmr:column>
      <br>
      &nbsp;
    </cmr:row>
  <%}%>

   <!--  PPSCEID, Membership Level, BP Relation Type -->    
   <cmr:view exceptForCountry="643,749,778,818,834,852,856,646,714,720">
		<cmr:row topPad="10">
  			<cmr:column span="2" containerForField="PPSCEID">
  				<p>
  					<cmr:label fieldId="ppsceid">
              <cmr:fieldLabel fieldId="PPSCEID" />:
              <cmr:delta text="${rdcdata.ppsceid}" oldValue="${reqentry.ppsceid}"/>
            </cmr:label>
            <cmr:field fieldId="PPSCEID" id="ppsceid" path="ppsceid" tabId="MAIN_IBM_TAB" />
  				</p>
  			</cmr:column>
        	<cmr:column span="2" containerForField="MembLevel">
	          <p>
	            <cmr:label fieldId="memLvl">
	              <cmr:fieldLabel fieldId="MembLevel" />:
	              <cmr:delta text="${rdcdata.memLvl}" oldValue="${reqentry.memLvl}" code="L"/>
	            </cmr:label>
	            <cmr:field fieldId="MembLevel" id="memLvl" path="memLvl" tabId="MAIN_IBM_TAB" />
	          </p>
	        </cmr:column>
        
		</cmr:row>
 <%--  CMR-3869 <cmr:view exceptForGEO="MCO2,CN">
  		<cmr:row topPad="10">
        <cmr:column span="2" containerForField="SOENumber">
          <p>
            <cmr:label fieldId="soeReqNo">
              <cmr:fieldLabel fieldId="SOENumber" />:
            </cmr:label>
            <cmr:field fieldId="SOENumber" id="soeReqNo" path="soeReqNo" tabId="MAIN_IBM_TAB" />
          </p>
        </cmr:column>
  		</cmr:row>
    </cmr:view>
   --%>
		</cmr:view>
     
	</cmr:section>
