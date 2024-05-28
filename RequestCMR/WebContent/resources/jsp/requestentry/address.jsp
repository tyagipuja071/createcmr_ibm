<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.util.RequestUtils"%>
<%@page import="com.ibm.cio.cmr.request.util.geo.GEOHandler"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
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
GEOHandler handler = RequestUtils.getGEOHandler(reqentry.getCmrIssuingCntry());
boolean showName = handler != null && handler.customerNamesOnAddress();
//Defect 1361141: By Mukesh
boolean processingStatus =  "Processed".equalsIgnoreCase(reqentry.getProcessingStatus()) ? true : false ;
String updateInd = "U".equals(reqentry.getReqType()) ? "Y" : "N"; 

String AP_ASEAN = "643,646,749,818,834,852,856,778,714,720";
String AP_ISA = "744,652,615,790";
String AP_HK = "736,738";
String AP_ANZ = "616,796";
%>
<style>
.cmr-middle {
  text-align:center;
}

.cmr-hide-Me{
display: none !IMPORTANT;
visibility: hidden !IMPORTANT;
}
</style>
<script>

  function ADDRESS_GRID_GRID_onLoad(data){
    window.setTimeout(toggleButton, 1000); 
    _allAddressData = data;
  }
  
  function toggleRemoveButtonLA(noRepeat){
  	if (dojo.byId('removeAddressesButton')) {
  	  dojo.byId('removeAddressesButton').style.display = 'inline';
  	}
  }
  
  function toggleButton(noRepeat){
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    switch (cntry){
    case '897':
      if (CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 1){
        cmr.hideNode('addAddressButton');
      } else if (FormManager.getActualValue('reqType') == 'U'){
        cmr.hideNode('addAddressButton');
      } else {
        if (dojo.byId('addAddressButton')){
          dojo.byId('addAddressButton').style.display = 'inline';
        }
      }
      break;
    case '758': //Defect 1509289 :Mukesh
    	console.log("Italy address rowCount="+CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount);
      if (FormManager.getActualValue('reqType') == 'C' && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 2){
        cmr.hideNode('addAddressButton');
      } else if (FormManager.getActualValue('reqType') == 'U'){
      	console.log("Italy address in Update Request..");
        cmr.hideNode('addAddressButton');
        cmr.hideNode('removeAddressesButton');
      } else {
        if (dojo.byId('addAddressButton')){
          dojo.byId('addAddressButton').style.display = 'inline';
        }
      }
      break;  
    default : 
        if (dojo.byId('addAddressButton')){
          dojo.byId('addAddressButton').style.display = 'inline';
        }
    }
    
    if (!noRepeat){
      window.setTimeout('toggleButton(true)', 1000);
      window.setTimeout('toggleRemoveButtonLA(true)', 1000);
    }
  }
</script>
<style>
 .btn-search, .btn-reset {
   min-width: 100px;
   height: 30px !important;
 }
</style>
<cmr:section id="NAME_REQ_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />

  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="1" width="220">
      <p>
        <cmr:label fieldId="dplCheck">
              DPL Check Result:
              <%if ("Processor".equalsIgnoreCase(reqentry.getUserRole())){%>
                <span class="ibm-required cmr-required-spacer">*</span>
              <%} %>
              <cmr:info text="The results of the automated DPL check done against Export Validation Services (EVS). Results are returned as pass or fail only per address." />
        </cmr:label>
        <div>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'AP'}">
            All Passed
          </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'AF'}">
            <span style="color:red;font-weight:bold">All Failed</span>
          </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'SF'}">
            <span style="color:red;font-weight:bold">Some Failed</span>
          </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'Not Done'}">
            Not Done
          </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'NR'}">
            Not Required
          </c:if>
        </div>
      </p>
    </cmr:column>
    <cmr:column span="1" width="220">
      <p>
        <cmr:label fieldId="dplCheck">
          Performed By:
        </cmr:label>
        <div>
          ${reqentry.dplChkUsrId}
        </div>
      </p>
    </cmr:column>
    <cmr:column span="1" width="220">
      <p>
        <cmr:label fieldId="dplCheck">
          DPL Check Date:
        </cmr:label>
        <div>
          ${reqentry.dplChkDate}
        </div>
      </p>
    </cmr:column>
    <cmr:column span="1" width="250">
      <%if (!readOnly){ %>
      <p>
        <input type="button" value="DPL Check" class="cmr-grid-btn-h btn-search" onclick="doDplCheck()"> 
        <cmr:info text="${ui.info.dplCheck}" />
      </p>
      <%} %>
    </cmr:column>
  </cmr:row>
  
  <!-- // CREATCMR-5447 -->
  <cmr:view forCountry="897">
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="1" width="220">
        <p>
          <cmr:label fieldId="dplCheck">
            SCC: 
          </cmr:label>
          <div>
            <span id="addressTabSccInfo"></span>
            <input type="hidden" id="scc" name="scc" />
          </div>
        </p>
      </cmr:column>
    </cmr:row>
  </cmr:view>
  <!-- // CREATCMR-5447 -->
  
  <c:if test="${fn:trim(reqentry.dplChkResult) == 'AF' || fn:trim(reqentry.dplChkResult) == 'SF'}">
    <cmr:row>
      <cmr:column span="1" width="220">
        <p>
          <cmr:label fieldId="dplCheck">
              DPL Assessment Result:
              <cmr:info text="The final assessment made by a user when searching directly against the DPL database and comparing the customer information against the list of DPL entities returned." />
          </cmr:label>
          <div>
            <c:if test="${fn:trim(reqentry.intDplAssessmentResult) == 'Y'}">
              <span style="color:red;font-weight:bold">Matched DPL entities</span>
            </c:if>
            <c:if test="${fn:trim(reqentry.intDplAssessmentResult) == 'N'}">
              <span>No Actual Matches</span>
            </c:if>
            <c:if test="${fn:trim(reqentry.intDplAssessmentResult) == 'U'}">
              <span>Cannot determine, needs further review</span>
            </c:if>
            <c:if test="${fn:trim(reqentry.intDplAssessmentResult) == ''}">
              <span>Not Done</span>
            </c:if>
          </div>
        </p>
      </cmr:column>
      <cmr:column span="1" width="220">
        <p>
          <cmr:label fieldId="dplCheck">
            Assessed By:
          </cmr:label>
          <div>
            ${reqentry.intDplAssessmentBy}
          </div>
        </p>
      </cmr:column>
      <cmr:column span="1" width="220">
        <p>
          <cmr:label fieldId="dplCheck">
            Assessment Date:
          </cmr:label>
          <div>
            ${reqentry.intDplAssessmentDate}
          </div>
        </p>
      </cmr:column>
      <cmr:column span="1" width="250">
        <p>
          <%if (!readOnly) {%>
            <input type="button" title="Assess DPL Results" value="Search and Assess DPL Matches" class="cmr-grid-btn-h btn-search" onclick="openDPLSearch()">
            <cmr:info text="Opens a new window and searches directly against the DPL Database" />
          <%}%>
        </p>
      </cmr:column>
    </cmr:row>
  </c:if>

  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="1" width="170">
      <p>
        <cmr:label fieldId="addressList">
              ${ui.addressList}:
        </cmr:label>
      </p>
    </cmr:column>
  </cmr:row>
  <%
  String contextPath = request.getContextPath();
  String actions = UIMgr.getText("grid.actions");
  String infoTxt = UIMgr.getText("info.addrActionInfo");
  String actionsHeader = actions+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
  %> 
  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="6">
      <cmr:grid url="/request/address/list.json" id="ADDRESS_GRID" span="6" height="250" usePaging="false"
        hasCheckbox="<%=!readOnly%>" checkBoxKeys="reqId,addrType,addrSeq">
        <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
        <cmr:gridParam fieldId="updateInd" value="<%=updateInd%>" />
        <cmr:gridParam fieldId="cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
        
        <!-- Address Type (except Japan) -->
        <cmr:view exceptForCountry="760">
          <cmr:gridCol width="50px" field="addrTypeText" header="${ui.grid.addrType}">
            <cmr:formatter functionName="addressTypeFormatter" />
          </cmr:gridCol>
        </cmr:view>

        <!-- Type (Japan) -->
        <cmr:view forCountry="760">
          <cmr:gridCol width="90px" field="addrTypeText" header="Type">
            <cmr:formatter functionName="addressTypeFormatter" />
          </cmr:gridCol>
        </cmr:view>
        
        <%if (showName){%>       
          <!--  Sequence -->
           <cmr:view exceptForGEO="SWISS,LA">
          <cmr:gridCol width="45px" field="addrSeq" header="Seq." >
            <cmr:formatter functionName="addrSeqFormatter" />
          </cmr:gridCol>
          </cmr:view>
          <cmr:view forCountry="848">
          <cmr:gridCol width="105px" field="addrSeq" header="Seq." >
            <cmr:formatter functionName="addrSeqFormatter" />
          </cmr:gridCol>
          </cmr:view>
          <cmr:view forCountry="706">
            <cmr:gridCol width="60px" field="parCmrNo" header="Parent CMR No." />
          </cmr:view>
          <cmr:view forCountry="758">
            <cmr:gridCol width="60px" field="parCmrNo" header="Address CMR No." />
          </cmr:view>
          <!-- Japan -->
          <cmr:view forCountry="760">
            <cmr:gridCol width="60px" field="parCmrNo" header="Record No." />
            <cmr:gridCol width="100px" field="custNm3" header="Full English Name" />
          </cmr:view>

          <!--  Customer Name and Name Con't except for JP,FR -->
          <cmr:view exceptForGEO="JP,FR,TW" exceptForCountry="618,724,848,858">
            <cmr:gridCol width="120px" field="custNm1" header="${ui.grid.custNameUKI1}" >
              <cmr:formatter functionName="customerNameFormatter" />
            </cmr:gridCol>
          </cmr:view>
          
          <!--  Customer English Name For Taiwan -->
          <cmr:view forGEO="TW" >
            <cmr:gridCol width="180px" field="custNm1" header="Customer English Name" >
              <cmr:formatter functionName="customerNameFormatter" />
            </cmr:gridCol>
          </cmr:view>
          
          <!--  Customer legal name and Legal name continued for FR -->	
          <cmr:view forGEO="FR">	
            <cmr:gridCol width="120px" field="custNm1" header="Customer legal name" >	
              <cmr:formatter functionName="customerNameFormatter" />	
            </cmr:gridCol>	
          </cmr:view>
          
          <!--  Customer Name-KANJI and Name-KANJI Continue for JP -->
          <cmr:view forGEO="JP" exceptForCountry="618,724">
            <cmr:gridCol width="120px" field="custNm1" header="Customer Name-KANJI" >
              <cmr:formatter functionName="customerNameFormatter" />
            </cmr:gridCol>
          </cmr:view>
          
          <!-- Additional Info for Nordics -->
          <cmr:view forGEO="NORDX" exceptForCountry="780">
            <cmr:gridCol width="100px" field="custNm3" header="Additional Info" />
          </cmr:view>

			<!-- MALTA LEGACY -->	
			<cmr:view forCountry="780">	
				<cmr:gridCol width="120px" field="custNm3" header="Name 3" />	
			</cmr:view>					
          
          <cmr:view forGEO="FR">
         <%-- <cmr:gridCol width="120px" field="custNm3" header="Customer Name/ Additional Address Information" /> --%>
         <cmr:gridCol width="120px" field="custNm3" header="Division/Department" />	
         <cmr:gridCol width="120px" field="custNm4" header="Attention to/Building/Floor/Office" />          
         </cmr:view>
			    	
        <cmr:view forCountry="618,724">
				<cmr:gridCol width="120px" field="custNm1" header="Customer Legal name <br> Legal Name Continued"> 
				<cmr:formatter functionName="customerNameFormatter" />
				</cmr:gridCol>
				<cmr:gridCol width="120px" field="custNm3" header="Division/Department"/>
				<cmr:gridCol width="120px" field="custNm4" header="Attention to /Building/Floor/Office"/>
				<cmr:gridCol width="100px" field="bldg" header="Building_Ext" />
				<cmr:gridCol width="100px" field="dept" header="Department_Ext" />
				<cmr:gridCol width="140px" field="addrTxt" header="Street Name and Number" />
			</cmr:view>
			
			<cmr:view forCountry="848">
				<cmr:gridCol width="140px" field="custNm1" header="Customer Legal name <br> Legal Name Continued" > 
				<cmr:formatter functionName="customerNameFormatter" />
				</cmr:gridCol>
				<cmr:gridCol width="120px" field="divn" header="Division/Department"/>
				<cmr:gridCol width="120px" field="city2" header="Attention to /Building/Floor/Office"/>
				<cmr:gridCol width="140px" field="addrTxt" header="Street Name and Number" />
			</cmr:view>
        <%} %>
        
        <cmr:view forCountry="649">
        	<cmr:gridCol width="60px" field="addrSeq" header="Sequence" />
        </cmr:view>
         
        <cmr:view forCountry="862">
          <cmr:gridCol width="70px" field="custNm4" header="Name 4" />
        </cmr:view>
        
        <!-- Street and Street Con't except BELUX,NL,JP -->
        <cmr:view exceptForGEO="BELUX,NL,JP,AP,FR,TW" exceptForCountry="618,724,848,858">
          <cmr:gridCol width="130px" field="addrTxt" header="${ui.grid.addrTxt}" >
              <cmr:formatter functionName="streetValueFormatter" />
          </cmr:gridCol>
        </cmr:view>
        <!-- Customer English Address For Taiwan -->
        <cmr:view forGEO="TW">
          <cmr:gridCol width="180px" field="addrTxt" header="Customer English Address" >
              <cmr:formatter functionName="streetValueFormatter" />
          </cmr:gridCol>
        </cmr:view>
        <!--  Street name and number for FR -->	
          <cmr:view forGEO="FR">	
            <cmr:gridCol width="120px" field="addrTxt" header="Street name and number" >	
              <cmr:formatter functionName="streetValueFormatter" />	
            </cmr:gridCol>	
          </cmr:view>	

          <!-- Street and Street Con't for AP -->
        <cmr:view forGEO="AP">
          <cmr:gridCol width="130px" field="addrTxt" header="Address. <br> Address. Cont1" >
              <cmr:formatter functionName="streetValueFormatter" />
          </cmr:gridCol>
        </cmr:view>
        

        <!-- Street and Street Con't for BELUX -->
        <cmr:view forGEO="BELUX,NL">
          <cmr:gridCol width="140px" field="custNm3" header="CustName3/ATT/PO Box" >
              <cmr:formatter functionName="streetValueFormatterBELUX" />
          </cmr:gridCol>
          <cmr:gridCol width="140px" field="addrTxt" header="Street Address" >
          </cmr:gridCol>
        </cmr:view>
        
		<!-- Address for JP -->
        <cmr:view forGEO="JP">
          <cmr:gridCol width="140px" field="addrTxt" header="Address" >
          </cmr:gridCol>
        </cmr:view>

        <cmr:view forGEO="AP" exceptForCountry="616,736,738,796">
          <cmr:gridCol width="110px" field="dept" header="Address. Cont2" />
        </cmr:view>

        
        <!-- State Province -->
        <cmr:view forGEO="LA,US,SWISS,NORDX">
          <cmr:gridCol width="80px" field="stateProv" header="${ui.grid.stateProvince}" >
            <cmr:formatter functionName="stateProvFormatter" />
          </cmr:gridCol>
        </cmr:view>
        
        <!--  ANZ State Prov -->
        <cmr:view forCountry="616,796">
          <cmr:gridCol width="80px" field="stateProv" header="${ui.grid.stateProvince}" >
            <cmr:formatter functionName="stateProvFormatter" />
          </cmr:gridCol>
        </cmr:view>
        
         <cmr:view forCountry="618">
          <cmr:gridCol width="80px" field="stateProv" header="${ui.grid.stateProvince}" >
            <cmr:formatter functionName="stateProvFormatter" />
          </cmr:gridCol>
        </cmr:view>
        
        
        <cmr:view forCountry="666">
          <cmr:gridCol width="90px" field="addrTxt2" header="${ui.grid.occupation}" />
        </cmr:view>
        
        <!-- PO Box for ES/PT/IL/GR/CY/TU/UKI -->
        <cmr:view forCountry="838,755,822,726,666,866,754">
          <cmr:gridCol width="90px" field="poBox" header="PO Box" />
        </cmr:view>
        
        
        <!--  City except for JP -->
        <cmr:view exceptForGEO="JP,TW">
          <cmr:gridCol width="80px" field="city1" header="${ui.grid.city1}" />
        </cmr:view>
        
        <!-- Postal Code -->
        <cmr:gridCol width="70px" field="postCd" header="${ui.grid.zipCode}" />  

        <!-- Dept / Attn -->
        <cmr:view forGEO="MCO,MCO1,MCO2,NORDX" exceptForCountry="780">
          <cmr:gridCol width="100px" field="custNm4" header="Dept/Attn" />
        </cmr:view>
        
        <!-- TIN# -->
        <cmr:view forCountry="851">
			<cmr:gridCol width="100px" field="dept" header="TIN#"></cmr:gridCol>  		    	
        </cmr:view>
        
        <!-- Story : 1830918 -->
        
        <cmr:view forCountry="848">
			<cmr:gridCol width="100px" field="dept" header="Department_ext"></cmr:gridCol>  		    	
        </cmr:view>
        <cmr:view forCountry="848">
			<cmr:gridCol width="100px" field="bldg" header="Building_ext"></cmr:gridCol>  		    	
        </cmr:view>
        
         <cmr:view forCountry="848">
			<cmr:gridCol width="100px" field="hwInstlMstrFlg" header="Hardware Master"></cmr:gridCol>     	
        </cmr:view>
        
        <cmr:view forGEO="EMEA" exceptForCountry="862,758,726,666">
          <cmr:gridCol width="100px" field="dept" header="Dept/Attn" />
        </cmr:view>
		<!-- Attn for Greece & Cyprus-->
        <cmr:view forCountry="726,666">
          <cmr:gridCol width="100px" field="custNm4" header="Dept/Attn" />
        </cmr:view>
        <!--  District for Turkey -->
        <cmr:view forCountry="862">
          <cmr:gridCol width="100px" field="dept" header="District" />
        </cmr:view>
        <cmr:view forGEO="LA" exceptForCountry="631">
          <cmr:gridCol width="100px" field="divn" header="Division" />
        </cmr:view>
        <cmr:view forGEO="LA">
        	<cmr:gridCol width="100px" field="vat" header="VAT#" />
        </cmr:view>
     
        <!-- Change indicator -->
        <%if ("U".equals(reqentry.getReqType())){ %>
          <cmr:gridCol width="70px" field="updateInd" header="Change" >
            <cmr:formatter functionName="addrUpdateIndFormatter" />
          </cmr:gridCol>
        <%} %>
        
        <!-- DPL Check Result -->
        <cmr:gridCol width="70px" field="dplChkResult" header="${ui.grid.dplCheck}" >
          <cmr:formatter functionName="dplFormatter" />
        </cmr:gridCol>
        
        <!-- Action Buttons -->
        <cmr:gridCol width="90px" field="action" header="<%=actionsHeader%>">
          <%if (!readOnly) {%>
            <cmr:formatter functionName="addrFormatterIcons" />
          <%}%>
        </cmr:gridCol>


        <!-- Landed Country except for JP -->
        <cmr:view exceptForGEO="JP">
          <cmr:gridCol width="70px" field="landCntry" header="Country" >
            <cmr:formatter functionName="countryFormatter" />
          </cmr:gridCol>
        </cmr:view>

  		<!-- County for UKI IT -->
        <cmr:view forCountry="866,754,758">
          <cmr:gridCol width="90px" field="stateProv" header="County" />
          <cmr:formatter functionName="stateProvFormatter" />
        </cmr:view>
        
        <!-- PO Box for non ES/PT/IL/GR/CY/TU/CEE -->
        <cmr:view exceptForGEO="BELUX,JP,TW" exceptForCountry="838,755,822,726,666,862,758,358,359,363,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,866,754,858">
          <cmr:gridCol width="90px" field="poBox" header="PO Box" />
        </cmr:view>

        <!-- Phone -->
        <cmr:view exceptForCountry="758,760,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,358,359,363,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,858">
          <cmr:gridCol width="90px" field="custPhone" header="Phone #" />
        </cmr:view>
        
        <!-- Phone for JP -->
        <cmr:view forGEO="JP">
          <cmr:gridCol width="90px" field="custPhone" header="Tel No" />
        </cmr:view>

        <!--  Extra Billing fields -->
        <cmr:view forCountry="758">
          <cmr:gridCol width="90px" field="bldg" header="Name Abbrev." />
          <cmr:gridCol width="90px" field="divn" header="Street Abbrev." />
          <cmr:gridCol width="90px" field="custFax" header="Loc. Abbrev." />
        </cmr:view>

        <!-- Tax Office for Turkey -->
        <cmr:view forCountry="862">
          <cmr:gridCol width="90px" field="taxOffice" header="Tax Office" />
        </cmr:view>

        <!-- ANZ Attn -->
        <cmr:view forCountry="616,796">
          <cmr:gridCol width="100px" field="dept" header="Attn" />
        </cmr:view>

        <!--  Tax Codes, LA only -->
        <cmr:view forGEO="LA">
          <cmr:gridCol width="100px" field="taxCd1" header="Tax Code1/SFC" />
          <cmr:gridCol width="100px" field="taxCd2" header="Tax Code2/MFC" />
        </cmr:view>

		<!-- Estab Function Code for JP -->
        <cmr:view forGEO="JP">
          <cmr:gridCol width="100px" field="estabFuncCd" header="Estab Function Code" />
        </cmr:view>
        
        <!-- ROL Flag for JP -->
        <cmr:view forGEO="JP">
          <cmr:gridCol width="100px" field="rol" header="ROL Flag" />
        </cmr:view>
        
        <!-- Data Source -->
        <cmr:gridCol width="70px" field="importInd" header="${ui.grid.datasource}" >
          <cmr:formatter functionName="importIndFormatter" />
        </cmr:gridCol>
        
        <!--  SAP No -->
        <cmr:view exceptForGEO="LA">
        <cmr:gridCol width="70px" field="sapNo" header="${ui.grid.sapNo}" />
        </cmr:view>
        
             <!-- Defect : 1444422 for FR-->
        <!-- iERP Site Party ID -->
<!--         CREATCMR-531 -->
        <cmr:view forGEO="IERP,CND,FR,SWISS,LA">
          <cmr:gridCol width="70px" field="ierpSitePrtyId" header="${ui.grid.ierpSitePrtyId}" />
        </cmr:view>
        
        <cmr:view forCountry="780">	
           <cmr:gridCol width="70px" field="ierpSitePrtyId" header="${ui.grid.ierpSitePrtyId}" />	
        </cmr:view>
        
        <!-- Addr Std Results -->
        <cmr:view exceptForGEO="EMEA,LA,MCO,JP">
          <cmr:gridCol width="70px" field="addrStdResultText" header="${ui.grid.addrStdTxt}" />
        </cmr:view>
        
        <!-- Create Date -->
        <%if (!showName){%>        
          <cmr:gridCol width="80px" field="createTsString" header="${ui.grid.createDate}" />
        <%} %>
      </cmr:grid>
    </cmr:column>
  </cmr:row>

  <cmr:row topPad="5" addBackground="true">
    <div class="ibm-col-1-1 cmr-middle">
<%if (!readOnly) {%>
      <cmr:button id="addAddressButton" label="${ui.btn.addAddress}" onClick="doAddAddress()" styleClass="cmr-middle"/>
      <cmr:button id="removeAddressesButton" label="${ui.btn.removeaddresses}" onClick="removeSelectedAddresses()" styleClass="cmr-middle" pad="true"/>
    <%if ("C".equals(reqentry.getReqType()) && !"Y".equals(reqentry.getProspLegalInd())) {%>
      <cmr:view forCountry="706">
        <cmr:button id="importAddressesButton" label="${ui.btn.importAddress}" onClick="doAddressImportSearch()" styleClass="cmr-middle" pad="true"/>
      </cmr:view>
    <%}%>
<%} %>
    </div>
    <br><br>
  </cmr:row>
</cmr:section>