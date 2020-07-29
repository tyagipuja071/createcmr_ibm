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
  	var cntry = FormManager.getActualValue('cmrIssuingCntry');
  	
  	if(typeof (LAHandler) != 'undefined' && LAHandler.isLAIssuingCountry(cntry) && FormManager.getActualValue('reqType') == 'U'){
  		cmr.hideNode('removeAddressesButton');
  	} else{
  	  if (dojo.byId('removeAddressesButton')) {
  	    dojo.byId('removeAddressesButton').style.display = 'inline';
  	  }
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
    case '631':
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
<cmr:section id="NAME_REQ_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />

  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="1" width="180">
      <p>
        <cmr:label fieldId="addressList">
              ${ui.addressList}:
        </cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="1">
      <cmr:button label="${ui.btn.dplChk}" id="dplCheckBtn" onClick="doDplCheck()" highlight="true" styleClass="cmr-reqentry-btn"/>
      <%if ("Processor".equalsIgnoreCase(reqentry.getUserRole())){%>
      <span class="ibm-required cmr-required-spacer">*</span>
      <%} else {%>
      <span class="ibm-required cmr-required-spacer" style="visibility:hidden">*</span>
      <%} %>
      <cmr:info text="${ui.info.dplCheck}" />
    </cmr:column>
    <cmr:column span="1" width="120">
    <p>
        <label>${ui.dplChkStatus}:</label> 
        </p>
    </cmr:column>
    <cmr:column span="1" width="100">
    <p>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'AP'}">
                  All Passed
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'AF'}">
                  All Failed
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'SF'}">
                  Some Failed
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'Not Done'}">
                  Not Done
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'NR'}">
                  Not Required
                  </c:if>
                  </p>
    </cmr:column>
    <cmr:column span="1" width="125">
    <p>
        <label>${ui.dplChkDate}:</label>
        </p>
    </cmr:column>
    <cmr:column span="1" width="100">
    <p>
        ${reqentry.dplChkDate}
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
           <cmr:view exceptForGEO="SWISS">
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

          <!--  Customer Name and Name Con't except for JP -->
          <cmr:view exceptForGEO="JP">
            <cmr:gridCol width="120px" field="custNm1" header="${ui.grid.custNameUKI1}" >
              <cmr:formatter functionName="customerNameFormatter" />
            </cmr:gridCol>
          </cmr:view>
          
          <!--  Customer Name-KANJI and Name-KANJI Continue for JP -->
          <cmr:view forGEO="JP">
            <cmr:gridCol width="120px" field="custNm1" header="Customer Name-KANJI" >
              <cmr:formatter functionName="customerNameFormatter" />
            </cmr:gridCol>
          </cmr:view>
          
          <cmr:view forGEO="FR">
            <cmr:gridCol width="120px" field="custNm3" header="Customer Name/ Additional Address Information" />
          </cmr:view>
        <%} %>
        
        <!-- Defect : 1444422 for FR-->
        <!-- iERP Site Party ID -->
        <cmr:view forGEO="IERP,CND,FR,SWISS">
          <cmr:gridCol width="70px" field="ierpSitePrtyId" header="${ui.grid.ierpSitePrtyId}" />
        </cmr:view>
        
        <cmr:view forGEO="SWISS">
          <cmr:gridCol width="70px" field="custNm4" header="${ui.grid.custNm4}" />
          <cmr:gridCol width="70px" field="custNm3" header="${ui.grid.custNm3}" />
        </cmr:view>
        
        <!-- Street and Street Con't except BELUX,JP -->
        <cmr:view exceptForGEO="BELUX,JP,AP">
          <cmr:gridCol width="130px" field="addrTxt" header="${ui.grid.addrTxt}" >
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
        <cmr:view forGEO="BELUX">
          <cmr:gridCol width="140px" field="addrTxt" header="Stree/PO Box" >
              <cmr:formatter functionName="streetValueFormatterBELUX" />
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

		<!-- Address for Swiss -->
        <cmr:view forGEO="SWISS">
          <cmr:gridCol width="80px" field="custLangCd" header="Preferred Language" >
          </cmr:gridCol>
        </cmr:view>
        
        <!-- State Province -->
        <cmr:view forGEO="LA,US">
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
        
        <!-- PO Box for ES/PT/IL/GR/CY/TU/UKI -->
        <cmr:view forCountry="838,755,822,726,666,866,754">
          <cmr:gridCol width="90px" field="poBox" header="PO Box" />
        </cmr:view>
        
        
        <!--  City except for JP -->
        <cmr:view exceptForGEO="BELUX,JP">
          <cmr:gridCol width="80px" field="city1" header="${ui.grid.city1}" />
        </cmr:view>
        
        <!-- Postal Code -->
        <cmr:gridCol width="70px" field="postCd" header="${ui.grid.zipCode}" />  

        <!-- Dept / Attn -->
        <cmr:view forGEO="MCO,MCO1,MCO2,NORDX">
          <cmr:gridCol width="100px" field="custNm4" header="Dept/Attn" />
        </cmr:view>
        
        <!-- TIN# -->
        <cmr:view forCountry="851">
			<cmr:gridCol width="100px" field="dept" header="TIN#"></cmr:gridCol>  		    	
        </cmr:view>
        
         <!-- ICE# -->
        <cmr:view forCountry="642">
			<cmr:gridCol width="100px" field="dept" header="ICE#"></cmr:gridCol>  		    	
        </cmr:view> 
        
        <!-- Story : 1830918 -->
        
        <cmr:view forCountry="848">
			<cmr:gridCol width="100px" field="dept" header="Department"></cmr:gridCol>  		    	
        </cmr:view>
        <cmr:view forCountry="848">
			<cmr:gridCol width="100px" field="bldg" header="Building"></cmr:gridCol>  		    	
        </cmr:view>
        <cmr:view forCountry="848">
			<cmr:gridCol width="100px" field="floor" header="Floor"></cmr:gridCol>  		    	
        </cmr:view>
        
         <cmr:view forCountry="848">
			<cmr:gridCol width="100px" field="hwInstlMstrFlg" header="Hardware Master"></cmr:gridCol>     	
        </cmr:view>
 
        <!-- Attn for BELUX -->
        <cmr:view forGEO="BELUX">
          <cmr:gridCol width="100px" field="custNm4" header="Dept/Attn" >
            <cmr:formatter functionName="attnFormatterBELUX" />
          </cmr:gridCol>
        </cmr:view>

		<!-- Attn for NL -->
        <cmr:view forGEO="NL">
          <cmr:gridCol width="100px" field="custNm4" header="Dept/Attn" >
            <cmr:formatter functionName="attnFormatterNL" />
          </cmr:gridCol>
        </cmr:view>
        
        <cmr:view forGEO="EMEA" exceptForCountry="862,758">
          <cmr:gridCol width="100px" field="dept" header="Dept/Attn" />
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
        <cmr:view exceptForGEO="BELUX,JP" exceptForCountry="838,755,822,726,666,862,758,358,359,363,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,866,754">
          <cmr:gridCol width="90px" field="poBox" header="PO Box" />
        </cmr:view>

        <!-- Phone -->
        <cmr:view exceptForCountry="758,760,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,358,359,363">
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

        <!-- Tax Office and Occupation for Greece/Cyprus -->
        <cmr:view forCountry="726,666">
          <cmr:gridCol width="90px" field="taxOffice" header="Tax Office" />
          <cmr:gridCol width="90px" field="addrTxt2" header="Occupation" />
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
        <cmr:gridCol width="70px" field="sapNo" header="${ui.grid.sapNo}" />
        
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
