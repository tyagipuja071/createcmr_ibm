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
  if (readOnly == null) {
    readOnly = false;
  }
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
%>
<cmr:view forGEO="AP">
  <cmr:row topPad="10" addBackground="true">
    <cmr:column span="2" containerForField="ProvinceName">
      <p>
        <cmr:label fieldId="busnType"> <cmr:fieldLabel fieldId="ProvinceName" />: </cmr:label>
        <cmr:field path="busnType" id="busnType" fieldId="ProvinceName" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="ProvinceCode">
      <p>
        <cmr:label fieldId="territoryCd">
          <cmr:fieldLabel fieldId="ProvinceCode" />: 
        </cmr:label>
        <cmr:field path="territoryCd" id="territoryCd" fieldId="ProvinceCode" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="Cluster">
      <p>
        <cmr:label fieldId="apCustClusterId"> <cmr:fieldLabel fieldId="Cluster" />: </cmr:label>
        <cmr:field path="apCustClusterId" id="apCustClusterId" fieldId="Cluster" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  
  <cmr:row topPad="10" addBackground="true">
  	<cmr:column span="2" containerForField="ISBU">
      <p>
        <cmr:label fieldId="isbuCd"> <cmr:fieldLabel fieldId="ISBU" />: </cmr:label>
        <cmr:field path="isbuCd" id="isbuCd" fieldId="ISBU" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="Sector">
      <p>
        <cmr:label fieldId="sectorCd"> <cmr:fieldLabel fieldId="Sector" />: </cmr:label>
        <cmr:field path="sectorCd" id="sectorCd" fieldId="Sector" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
     <cmr:column span="2" containerForField="IndustryClass">
      <p>
      	<Label style="">IndustryClass:</Label>
		<input type="text" id="IndustryClass" name ="IndustryClass" value="${fn:substring(reqentry.subIndustryCd, 0, 1)}" readonly="readonly" style="width:15px;BACKGROUND: #FFFFEE;border: 1px Solid #DDDDDD"/>
      </p>
    </cmr:column> 
    
  </cmr:row>

  <cmr:row topPad="10">
  	<cmr:column span="2" containerForField="MrcCd">
      <p>
        <cmr:label fieldId="mrcCd"> <cmr:fieldLabel fieldId="MrcCd" />: </cmr:label>
        <cmr:field path="mrcCd" id="mrcCd" fieldId="MrcCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="RegionCode">
      <p>
        <cmr:label fieldId="miscBillCd"> <cmr:fieldLabel fieldId="RegionCode" />: </cmr:label>
        <cmr:field path="miscBillCd" id="miscBillCd" fieldId="RegionCode" tabId="MAIN_IBM_TAB" />
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
  <cmr:row topPad="10">
  	<cmr:column span="2" containerForField="SalRepNameNo">
      <p>
        <cmr:label fieldId="repTeamMemberNo">
          <cmr:fieldLabel fieldId="SalRepNameNo" />:
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
    <cmr:column span="2" containerForField="GovIndicator">
      <p>
        <cmr:label fieldId="govType"> <cmr:fieldLabel fieldId="GovIndicator" />: </cmr:label>
        <cmr:field path="govType" id="govType" fieldId="GovIndicator" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <cmr:column span="2" containerForField="CollBoId" forCountry = "749,852,856,778,818">
      <p>
        <cmr:label fieldId="collBoId"> <cmr:fieldLabel fieldId="CollBoId" />: </cmr:label>
        <cmr:field path="collBoId" id="collBoId" fieldId="CollBoId" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CustomerServiceCd" forCountry="796">
      <p>
        <cmr:label fieldId="engineeringBo"> <cmr:fieldLabel fieldId="CustomerServiceCd" />: </cmr:label>
        <cmr:field path="engineeringBo" id="engineeringBo" fieldId="CustomerServiceCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CustBillingContactNm" forCountry="778,834">
      <p>
        <cmr:label fieldId="contactName1"> <cmr:fieldLabel fieldId="CustBillingContactNm" />: </cmr:label>
        <cmr:field path="contactName1" id="contactName1" fieldId="CustBillingContactNm" tabId="MAIN_IBM_TAB" />
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
  </cmr:row>     
</cmr:view>