<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
  
  String prodType1 = reqentry.getCodCondition();
  String prodType2 = reqentry.getRemoteCustInd();
  String prodType3 = reqentry.getDecentralizedOptIndc();
  String prodType4 = reqentry.getImportActIndc();
  String prodType5 = reqentry.getMailingCondition();
  String prodType6 = reqentry.getSizeCd();
  String prodType7 = reqentry.getFootnoteNo();
  String prodType8 = reqentry.getFomeZero();
%>

<cmr:view forGEO="KR">
  <cmr:row addBackground="true">
<%--      <cmr:column span="2" containerForField="EngineeringBo">
      <p>
        <cmr:label fieldId="engineeringBo">
          <cmr:fieldLabel fieldId="EngineeringBo" />:
           <cmr:delta text="${rdcdata.engineeringBo}" oldValue="${reqentry.engineeringBo}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field fieldId="EngineeringBo" id="engineeringBo" path="engineeringBo" tabId="MAIN_IBM_TAB"/>
      </p>
    </cmr:column> --%>
 
    <cmr:column span="2" containerForField="CmrNoPrefix">
        <p>
          <cmr:label fieldId="cmrNoPrefix">
            <cmr:fieldLabel fieldId="CmrNoPrefix" />:
          </cmr:label>
          <cmr:field path="cmrNoPrefix" id="cmrNoPrefix" fieldId="CmrNoPrefix" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
 
    <cmr:column span="2" containerForField="SalRepNameNo">
      <p>
        <cmr:label fieldId="repTeamMemberNo">
          <cmr:fieldLabel fieldId="SalRepNameNo" />:<cmr:info text="No impact on CMR/account owner, just AP CMR legacy system(WTAAS) requirement. "></cmr:info>
           <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" id="delta-repTeamMemberNo" />
        </cmr:label>
        <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  
  
   <%-- <cmr:row addBackground="true">  --%>
  <cmr:column span="2" containerForField="ParentCompanyNo">
        <p>
          <cmr:label fieldId="dealerNo">
            <cmr:fieldLabel fieldId="ParentCompanyNo" />:
            <cmr:info text="${ui.info.parentcompanyNo}"></cmr:info>
          </cmr:label>
          <cmr:field path="dealerNo" id="dealerNo" fieldId="ParentCompanyNo" tabId="MAIN_IBM_TAB" />
        </p>
    </cmr:column></cmr:row>
    
<%--           <cmr:column span="2" containerForField="MrcCd">
      		<p>
        	<cmr:label fieldId="mrcCd"> <cmr:fieldLabel fieldId="MrcCd" />: </cmr:label>
        	<cmr:field path="mrcCd" id="mrcCd" fieldId="MrcCd"/>
      		</p>
    	  </cmr:column> --%>
    	 <cmr:row addBackground="true"> 
    <cmr:column span="2" containerForField="mrcCd" >
      <p>
        <label for="MrcCd"> 
          <cmr:fieldLabel fieldId="MrcCd" />: 
			<cmr:delta text="-" id="delta-mrcCd" />
        </label>
        <cmr:field fieldId="MrcCd" path="mrcCd" id="mrcCd" tabId="MAIN_IBM_TAB"/>
      </p>
    </cmr:column>
    	  
<%--     	  <cmr:column span="2" containerForField="SOENumber">
          <p>
           <cmr:label fieldId="soeReqNo">
              <cmr:fieldLabel fieldId="SOENumber" />:
            </cmr:label>
            <cmr:field fieldId="SOENumber" id="soeReqNo" path="soeReqNo" tabId="MAIN_IBM_TAB" />
          </p>
        </cmr:column> --%>
 

<%-- <cmr:row addBackground="true">  --%>
    <cmr:column span="2" containerForField="OriginatorNo">
    
        <p>
          <cmr:label fieldId="orgNo">          
            <cmr:fieldLabel fieldId="OrgNo" />:            
          </cmr:label>
          <cmr:field path="orgNo" id="orgNo" fieldId="OrgNo" tabId="MAIN_IBM_TAB" />          
        </p>
    </cmr:column>
    
        <cmr:column span="2" containerForField="CommercialFinanced" >
        <p>
          <cmr:label fieldId="commercialFinanced">
            <cmr:fieldLabel fieldId="CommercialFinanced" />:
              <cmr:delta text="${rdcdata.commercialFinanced}" oldValue="${reqentry.commercialFinanced}" />
          </cmr:label>
          <cmr:field path="commercialFinanced" id="commercialFinanced" fieldId="CommercialFinanced" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      	<!-- Membership Level-->      
<%--         	<cmr:column span="2" containerForField="MembLevel">
          <p>
            <cmr:label fieldId="memLvl">
              <cmr:fieldLabel fieldId="MembLevel" />:
              <cmr:delta text="${rdcdata.memLvl}" oldValue="${reqentry.memLvl}" code="L"/>
            </cmr:label>
            <cmr:field fieldId="MembLevel" id="memLvl" path="memLvl" tabId="MAIN_IBM_TAB" />
          </p>
  			</cmr:column> --%>
    </cmr:row>
    <!-- Business Type for KR -->
    <cmr:row>
          <cmr:column span="2" containerForField="ContactName2">
        <p>
          <label for="contactName2"> <cmr:fieldLabel fieldId="ContactName2" />: </label>
          <cmr:field fieldId="ContactName2" id="contactName2" path="contactName2" />
        </p>
      </cmr:column>
      <!-- MCI Code  -->
      <cmr:column span="2" containerForField="CreditCd">
        <p>
          <cmr:label fieldId="creditCd">
            <cmr:fieldLabel fieldId="CreditCd" />:
          </cmr:label>
          <cmr:field path="creditCd" id="creditCd" fieldId="CreditCd" />
        </p>
      </cmr:column>
      
      <!-- BP Relation Type -->
  <%--     <cmr:column span="2" containerForField="BPRelationType">
                <p>
            <cmr:label fieldId="bpRelType">
              <cmr:fieldLabel fieldId="BPRelationType" />:
              <cmr:delta text="${rdcdata.bpRelType}" oldValue="${reqentry.bpRelType}" code="L"/>
            </cmr:label>
            <cmr:field fieldId="BPRelationType" id="bpRelType" path="bpRelType" tabId="MAIN_IBM_TAB" />
          </p>
        </cmr:column> --%>
      </cmr:row>
    
  <cmr:row addBackground="false">
    <cmr:column span="4">
     <p> <cmr:label fieldId="contactName3">
        <cmr:fieldLabel fieldId="ContactName3" />: <span id="ast-ProdType"></span>
          <cmr:delta text="-" id="delta-contactName3" code="L" />
      </cmr:label>
      <cmr:field fieldId="ContactName3" id="contactName3" path="contactName3" tabId="MAIN_IBM_TAB" />
      </p>      
      <%-- <div id="prodTypeCheckboxes" style="display: block">
        <div class="jp-chk">
          <input type="checkbox" name="codCondition" value="1" id="prodType_1" <%= ("1".equals(prodType1))?"checked":"" %> />
          <label class=" cmr-radio-check-label" for="prodType_1">AAS HW</label>
        </div>
        <div class="jp-chk">
      		<input  type="checkbox" name="remoteCustInd" value="1" id="prodType_2" <%= ("1".equals(prodType2))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_2">AAS z9/zSeries SW</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="decentralizedOptIndc" value="1" id="prodType_3" <%= ("1".equals(prodType3))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_3">Others expect AAS z9/zSeries SW</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="importActIndc" value="1" id="prodType_4" <%= ("1".equals(prodType4))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_4">QCOS</label>
        </div>
        <div class="jp-chk">
      		<input  type="checkbox" name="mailingCondition" value="1" id="prodType_5" <%= ("1".equals(prodType5))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_5">Lenovo</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="sizeCd" value="1" id="prodType_6" <%= ("1".equals(prodType6))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_6">CISCO</label>
        </div>
        <div class="jp-chk">
      		<input  type="checkbox" name="footnoteNo" value="1" id="prodType_7" <%= ("1".equals(prodType7))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_7">Demo used</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="fomeZero" value="1" id="prodType_8" <%= ("1".equals(prodType8))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_8">Others</label>
        </div>
      </div> --%>
    </cmr:column>  
    <br>
    <br>
    <br>
  </cmr:row>
</cmr:view>