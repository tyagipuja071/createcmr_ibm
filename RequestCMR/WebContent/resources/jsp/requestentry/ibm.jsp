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
		<jsp:include page="detailstrip.jsp" />
    
    <!--  CMR No, CAP, CMR Owner -->
		<cmr:row topPad="10" addBackground="true">
			<cmr:column span="1" containerForField="CMRNumber">
				<p>
					<cmr:label fieldId="cmrNo">
             <cmr:fieldLabel fieldId="CMRNumber" />: 
             
             <span id="cmrDelta"><cmr:delta text="${rdcdata.cmrNo}" oldValue="${reqentry.cmrNo}" /></span>
          </cmr:label>
          <cmr:field fieldId="CMRNumber" size="18" id="cmrNo" path="cmrNo" tabId="MAIN_IBM_TAB" size="150" />
				</p>
			</cmr:column>
      <cmr:view>
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
      </cmr:view>
      <cmr:view forGEO="JP">
      	<cmr:column span="2" containerForField="CMRNumber2">
      		<p>
      			<cmr:label fieldId="cmrNo2">
      				<cmr:fieldLabel fieldId="CMRNumber2" />:
          		</cmr:label>
          		<cmr:field fieldId="CMRNumber2" id="cmrNo2" path="cmrNo2" tabId="MAIN_IBM_TAB" />
			</p>
		</cmr:column>
      </cmr:view>
	  <cmr:column span="2" containerForField="CMROwner">
		  <p>
			 <cmr:label fieldId="cmrOwner">
               <cmr:fieldLabel fieldId="CMROwner" />:
               <cmr:delta text="${rdcdata.cmrOwner}"  oldValue="${reqentry.cmrOwner}"/>
             </cmr:label>
             <cmr:field fieldId="CMROwner" id="cmrOwner" path="cmrOwner" tabId="MAIN_IBM_TAB" />
		  </p>
	  </cmr:column>
      <cmr:view forCountry="641">
        <cmr:column span="1" containerForField="Military">
          <p>
              <cmr:label fieldId="military">&nbsp;</cmr:label>
              <cmr:field fieldId="Military" path="military" tabId="MAIN_IBM_TAB"/>
              <cmr:label fieldId="military" forRadioOrCheckbox="true">
                 <cmr:fieldLabel fieldId="Military" />
                 <cmr:info text="Military Use means that the company is owned, managed, or intended to be used by the local government's Military. This is for Export Regulations."></cmr:info>
                 <cmr:delta text="${rdcdata.military}" oldValue="${reqentry.military == 'Y' ? 'Yes' : 'No'}"/>
              </cmr:label>
          </p>
        </cmr:column>
      </cmr:view>
    </cmr:row>
    
    <!-- Defect : 1444422 -->
    <!-- Site Party ID -->
    <cmr:row topPad="10" addBackground="true">
			<cmr:column span="2" containerForField="SitePartyID" forGEO="IERP,MCO2,EMEA,JP,TW" forCountry="846,806,702,678,858">
				<p>
					<cmr:label fieldId="sitePartyId">
            <cmr:fieldLabel fieldId="SitePartyID" />: 
            <cmr:delta text="${rdcdata.sitePartyId}" oldValue="${reqentry.sitePartyId}" />
          </cmr:label>
					<cmr:field id="sitePartyId" path="sitePartyId" fieldId="SitePartyID" tabId="MAIN_IBM_TAB" />
				</p>
			</cmr:column>
			<cmr:column span="2" containerForField="CMRDoubleCreation" forGEO="TW" forCountry="858" >
				<p>
					<cmr:label fieldId="DuplicateCMR">
		            <cmr:fieldLabel fieldId="DuplicateCMR" />: 
		           <%--  <cmr:delta text="${rdcdata.dupCmrIndc}" oldValue="${reqentry.dupCmrIndc}" />DuplicateCMR --%>
          			</cmr:label>
					<cmr:field id="dupCmrIndc" path="dupCmrIndc" fieldId="DuplicateCMR" tabId="MAIN_IBM_TAB" />
				</p>				
				<p id="duplicateCMR"></p>
			</cmr:column>
  			<cmr:column span="1" containerForField="CustomerIdCd" forGEO="TW" forCountry="858">
  				<p>
            		<cmr:label fieldId="customerIdCd2">&nbsp;</cmr:label>
              		<cmr:field fieldId="CustomerIdCd" path="customerIdCd" tabId="MAIN_IBM_TAB"/>
              		<cmr:label fieldId="customerIdCd" forRadioOrCheckbox="true">
                 	<cmr:fieldLabel fieldId="CustomerIdCd" />
                 	<cmr:delta text="${rdcdata.customerIdCd}" oldValue="${reqentry.customerIdCd == 'Y' ? 'Yes' : 'No'}"/>
              		</cmr:label>
  				</p>
  			</cmr:column>
		</cmr:row>
    <cmr:view forGEO="CN">
    <cmr:row topPad="10">
   		<cmr:column span="2" containerForField="ChinaSearchTerm" >
	    	<p>
	      	<cmr:label fieldId="searchTerm">
	        <cmr:fieldLabel fieldId="SearchTerm" />: 
	        <cmr:delta text="${rdcdata.searchTerm}" oldValue="${reqentry.searchTerm}"/>
	        <span id="cnsearchterminfoSpan" style="display:none"><cmr:info text="${ui.info.cnsearchterminfo}"></cmr:info></span>
	      	</cmr:label>
	      	<cmr:field fieldId="ChinaSearchTerm" id="searchTerm" path="searchTerm" tabId="MAIN_IBM_TAB" />
	    	</p>
	  	</cmr:column>
	</cmr:row>
    </cmr:view>
   <!-- ISU, Client Tier, INAC Type/Code -->
   <cmr:row topPad="10">
   <cmr:column span="2" containerForField="ISU">
        <p>
          <cmr:label fieldId="isuCd">
            <cmr:fieldLabel fieldId="ISU" />:
            <cmr:delta text="${rdcdata.isuCd}" oldValue="${reqentry.isuCd}" code="L" />
          </cmr:label>
          <cmr:field fieldId="ISU" id="isuCd" path="isuCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
        <cmr:column span="2" containerForField="ClientTier">
          <p>
            <cmr:label fieldId="clientTier">
               <cmr:fieldLabel fieldId="ClientTier" />: 
               <span id="clientTierSpan"></span>
               <cmr:delta text="${rdcdata.clientTier}" oldValue="${reqentry.clientTier}" code="L"/>
            </cmr:label>
            <cmr:field fieldId="ClientTier" id="clientTier" path="clientTier" tabId="MAIN_IBM_TAB" />
          </p>
        </cmr:column>
      <cmr:column span="1" width="120" containerForField="INACType" exceptForCountry="666,726,862,822,838,618,758,848,755" exceptForGEO="MCO1,MCO2,FR,CEMEA,NORDX,BELUX,NL">
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
          <cmr:view forCountry="760">
          <%if (!readOnly){%>
          <span class="cmr-bluepages-ro" title="INACCode Search" style="font-family:Geneva, Arial, Helvetica, sans-serif;font-size:12px;" id="geoLocDescCont"><a href="https://ibm.box.com/s/o0pi2baolsu2px94m5zgc25zkbxxsfbi" target="_blank">INAC/NAC Code Search</a></span>
          <%}%>
          </cmr:view>
        </p> 
          
      </cmr:column>
		</cmr:row>
    
    <!-- CIS Duplicate CMR -->
    <cmr:view forCountry="821">
    <cmr:row topPad="10">
      <cmr:column span="2" containerForField="ISU2">
        <p>
          <cmr:label fieldId="dupIsuCd">
            <cmr:fieldLabel fieldId="ISU2" />:
          </cmr:label>
          <cmr:field fieldId="ISU2" id="dupIsuCd" path="dupIsuCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="ClientTier2">
        <p>
          <cmr:label fieldId="dupClientTierCd">
            <cmr:fieldLabel fieldId="ClientTier2" />: 
          </cmr:label>
          <cmr:field fieldId="ClientTier2" id="dupClientTierCd" path="dupClientTierCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    </cmr:view>
    
    <!-- DEFECT 1205817 : Merge SORTL and SBO fields -->
    <cmr:view exceptForGEO="LA,EMEA,AP,MCO,MCO1,MCO2,FR,CEMEA,NORDX,NL,CN" exceptForCountry="649">
    <cmr:row topPad="10">
    <cmr:column span="2" containerForField="SearchTerm" >
        <p>
          <cmr:label fieldId="searchTerm">
            <cmr:fieldLabel fieldId="SearchTerm" />: 
            <cmr:delta text="${rdcdata.searchTerm}" oldValue="${reqentry.searchTerm}"/>
            <cmr:view forCountry="858">
              <cmr:info text="${ui.info.TWForCluster}" />
            </cmr:view>
          </cmr:label>
          <cmr:field fieldId="SearchTerm" id="searchTerm" path="searchTerm" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      
      <cmr:column span="2" containerForField="MrcCd" forCountry="760">
        <p>
          <cmr:label fieldId="mrcCd"> <cmr:fieldLabel fieldId="MrcCd" />: </cmr:label>
          <cmr:field path="mrcCd" id="mrcCd" fieldId="MrcCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>

      <cmr:column span="2" containerForField="IndustryClass" forCountry="858" >
        <p>
          <label style="">IndustryClass:</label>
          <input type="text" id="IndustryClass" name ="IndustryClass" value="${fn:substring(reqentry.subIndustryCd, 0, 1)}" readonly="readonly" style="width:15px;BACKGROUND: #FFFFEE;border: 1px Solid #DDDDDD"/>
        </p>
      </cmr:column>

      <cmr:column span="2" containerForField="OrdBlk" forCountry="897" >
        <p>
          <cmr:label fieldId="ordBlk">
            <cmr:fieldLabel fieldId="OrdBlk" />: 
            <cmr:delta text="${rdcdata.ordBlk}" oldValue="${reqentry.ordBlk}"/>
          </cmr:label>
          <cmr:field fieldId="OrdBlk" id="ordBlk" path="ordBlk" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CommercialFinanced" forCountry="624" >
        <p>
          <cmr:label fieldId="commercialFinanced">
            <cmr:fieldLabel fieldId="CommercialFinanced" />:
              <cmr:delta text="${rdcdata.commercialFinanced}" oldValue="${reqentry.commercialFinanced}" />
          </cmr:label>
          <cmr:field path="commercialFinanced" id="commercialFinanced" fieldId="CommercialFinanced" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      
    </cmr:row>
   </cmr:view>
   
   <cmr:view forCountry='788'>
   	<cmr:row topPad="10">
   	  <cmr:column span="2" containerForField="CommercialFinanced" >
        <p>
          <cmr:label fieldId="commercialFinanced">
            <cmr:fieldLabel fieldId="CommercialFinanced" />:
              <cmr:delta text="${rdcdata.commercialFinanced}" oldValue="${reqentry.commercialFinanced}" />
          </cmr:label>
          <cmr:field path="commercialFinanced" id="commercialFinanced" fieldId="CommercialFinanced" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
   </cmr:view>

    <cmr:view exceptForCountry="643,749,778,818,834,852,856,646,714,720,848,649,858" exceptForGEO="FR,JP,MCO2,TW,KR">
    <cmr:row topPad="10" addBackground="true">
      <cmr:column span="2" containerForField="Enterprise" exceptForGEO="MCO1,NORDX" exceptForCountry="755,821,358,359,363,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,826,889">
        <p>
          <cmr:label fieldId="enterprise">
            <cmr:fieldLabel fieldId="Enterprise" />:
              <cmr:delta text="${rdcdata.enterprise}" oldValue="${reqentry.enterprise}" />
          </cmr:label>
          <cmr:field id="enterprise" path="enterprise" fieldId="Enterprise" tabId="MAIN_IBM_TAB" />  
    <!-- Austria - Removal of logic for Company number -->
<%--           <cmr:view forCountry="618">
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
          </cmr:view>  --%>       
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="Company" exceptForCountry="666,726,862,822,838,724,858,821" exceptForGEO="MCO1,MCO2,CEMEA,BELUX,NL,TW">
        <p>
          <cmr:label fieldId="company">
            <cmr:fieldLabel fieldId="Company" />: 
              <cmr:delta text="${rdcdata.company}" oldValue="${reqentry.company}" />
          </cmr:label>
          <cmr:field fieldId="Company" id="company" path="company" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="SalesBusOff" forCountry="821">
        <p>
          <cmr:label fieldId="salesBusOffCd">
            <cmr:fieldLabel fieldId="SalesBusOff" />:
          </cmr:label>
          <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>  
      <cmr:column span="2" containerForField="Affiliate" exceptForCountry="666,726,862,822,838,641,858" exceptForGEO="MCO1,MCO2,CEMEA,NORDX,BELUX,NL,TW">
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
    
    <!-- CIS Duplicate CMR -->
    <cmr:view forCountry="821">
    <cmr:row topPad="10" addBackground="true">
    <cmr:column span="2" containerForField="DupSalesBusOffCd">
      <p>
        <cmr:label fieldId="dupSalesBoCd">
          <cmr:fieldLabel fieldId="DupSalesBusOffCd" />: 
        </cmr:label>
        <cmr:field fieldId="DupSalesBusOffCd" id="dupSalesBoCd" path="dupSalesBoCd" tabId="MAIN_IBM_TAB" />
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
      <cmr:view exceptForGEO="JP">
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
      </cmr:view>
      <cmr:column span="2" containerForField="DUNS">
        <p>
          <cmr:label fieldId="dunsNo">
             <cmr:fieldLabel fieldId="DUNS" />: 
             <cmr:delta text="${rdcdata.dunsNo}" oldValue="${reqentry.dunsNo}" />
          </cmr:label>
          <cmr:field fieldId="DUNS" id="dunsNo" path="dunsNo" tabId="MAIN_IBM_TAB" />
          <%if (!readOnly){%>
          <span class="cmr-bluepages-ro" title="Validate DUNS against D&B" style="font-family:Geneva, Arial, Helvetica, sans-serif;font-size:12px;" id="geoLocDescCont"><a href="javascript: checkDUNSWithDnB()">Open D&B Details</a></span>
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
      && !reqentry.getCmrIssuingCntry().equalsIgnoreCase("631") || reqentry.getCmrIssuingCntry().equalsIgnoreCase("641") && reqentry.getCustSubGrp()!=null && 
      (reqentry.getCustSubGrp().equalsIgnoreCase("ECOSY") || reqentry.getCustSubGrp().equalsIgnoreCase("NRMLC") || reqentry.getCustSubGrp().equalsIgnoreCase("AQSTN"))){%>
      <span class="ibm-required cmr-required-spacer">*</span>
      <%} else {%>
      <span class="ibm-required cmr-required-spacer" style="visibility:hidden">*</span>
      <%} %>
        <cmr:info text="${ui.info.coverageBg}"></cmr:info>
      </cmr:column>
      <br>
      &nbsp;
    </cmr:row>
  <%}%>

   <!--  PPSCEID, Membership Level, BP Relation Type -->    
   <cmr:view exceptForCountry="643,749,778,818,834,852,856,646,714,720,760,649,858,766">
		<cmr:row topPad="10" addBackground="true">
  			<cmr:column span="2" containerForField="PPSCEID">
  				<p>
  					<cmr:label fieldId="ppsceid">
              <cmr:fieldLabel fieldId="PPSCEID" />:
              <cmr:delta text="${rdcdata.ppsceid}" oldValue="${reqentry.ppsceid}"/>
            </cmr:label>
            <cmr:field fieldId="PPSCEID" id="ppsceid" path="ppsceid" tabId="MAIN_IBM_TAB" />
  				</p>
  			</cmr:column>
        <cmr:column exceptForGEO="MCO2,NORDX" span="2" containerForField="MembLevel" exceptForCountry="754,758,866,822,666,644,668,693,704,708,740,820,821,826,358,359,363,603,607,626,651,694,695,699,705,707,787,741,889,838,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,780,706,624,788,858,766,755,897,724,618,848">
          <p>
            <cmr:label fieldId="memLvl">
              <cmr:fieldLabel fieldId="MembLevel" />:
              <cmr:delta text="${rdcdata.memLvl}" oldValue="${reqentry.memLvl}" code="L"/>
            </cmr:label>
            <cmr:field fieldId="MembLevel" id="memLvl" path="memLvl" tabId="MAIN_IBM_TAB" />
          </p>
        </cmr:column>
        <cmr:column exceptForGEO="MCO2,NORDX" span="2" containerForField="BPRelationType" exceptForCountry="754,758,866,822,666,644,668,693,704,708,740,820,821,826,358,359,363,603,607,626,651,694,695,699,705,707,787,741,889,838,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,780,706,624,788,858,755,724,618,848">
          <p>
            <cmr:label fieldId="bpRelType">
              <cmr:fieldLabel fieldId="BPRelationType" />:
              <cmr:delta text="${rdcdata.bpRelType}" oldValue="${reqentry.bpRelType}" code="L"/>
            </cmr:label>
            <cmr:field fieldId="BPRelationType" id="bpRelType" path="bpRelType" tabId="MAIN_IBM_TAB" />
          </p>
        </cmr:column>
		</cmr:row>
 <%--   CMR-3869  <cmr:view exceptForGEO="MCO2,CN" exceptForCountry="644,668,693,704,708,740,820,821,826,358,359,363,603,607,626,651,694,695,699,705,707,787,741,889,838,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,618,624,788,862,754,866">
  		<cmr:row topPad="10" addBackground="true">
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

    <!-- Include Here IBM Specific fields for GEOs -->
    
    <!--  US fields -->
    <jsp:include page="US/us_ibm.jsp" />

    <!--  LA fields -->
    <jsp:include page="LA/la_ibm.jsp" />

    <!--  EMEA fields -->
    <jsp:include page="EMEA/emea_ibm.jsp" />    
    
    <!--  CND fields -->
    <jsp:include page="CND/cnd_ibm.jsp" />  
    
    <!--  DE fields -->
    <jsp:include page="DE/de_ibm.jsp" /> 
    
    <!--  CN fields -->
    <jsp:include page="CN/cn_ibm.jsp" /> 
    
    <!--  AP fields -->
    <jsp:include page="AP/ap_ibm.jsp" /> 
    
    <!--  MCO fields -->
    <jsp:include page="MCO/mco_ibm.jsp" />  
    
    <!--  FR fields -->
    <jsp:include page="FR/fr_ibm.jsp" />    

    <!--  CEMEA fields -->
    <jsp:include page="CEMEA/cemea_ibm.jsp" />
    
     <!--  NORDX fields -->
    <jsp:include page="NORDX/nordx_ibm.jsp" />
    
     <!--  BELUX fields -->
    <jsp:include page="BELUX/belux_ibm.jsp" />
    
    <!--  NL fields -->
    <jsp:include page="NL/nl_ibm.jsp" />
    
    <!--  JP fields -->
    <jsp:include page="JP/jp_ibm.jsp" /> 
    
     <!--  SWISS fields -->
    <jsp:include page="SWISS/ch_ibm.jsp" />
    
       <!--  Canada fields -->
    <jsp:include page="CA/ca_ibm.jsp" />
    
       <!--  Taiwan fields -->
    <jsp:include page="TW/tw_ibm.jsp" />

       <!--  Korea, Republic of, fields -->
    <jsp:include page="KR/kr_ibm.jsp" />
     
	</cmr:section>
