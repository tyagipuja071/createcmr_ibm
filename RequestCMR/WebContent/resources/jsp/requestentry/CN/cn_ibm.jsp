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
%>
<cmr:view forGEO="CN" forCountry="641">
<cmr:row topPad="10" addBackground="false">
    <cmr:column span="2" containerForField="IbmDeptCostCenter">
      <p>
        <cmr:label fieldId="ibmDeptCostCenter">
          <cmr:fieldLabel fieldId="IbmDeptCostCenter" />: 
          </cmr:label>
        <cmr:field fieldId="IbmDeptCostCenter" id="ibmDeptCostCenter" path="ibmDeptCostCenter" tabId="MAIN_IBM_TAB" />
    </cmr:column>
	<%-- 
	<cmr:column span="2" containerForField="ChinaSearchTerm" >
	    <p>
	      <cmr:label fieldId="searchTerm">
	        <cmr:fieldLabel fieldId="SearchTerm" />: 
	        <cmr:delta text="${rdcdata.searchTerm}" oldValue="${reqentry.searchTerm}"/>
	      </cmr:label>
	      <cmr:field fieldId="ChinaSearchTerm" id="searchTerm" path="searchTerm" tabId="MAIN_IBM_TAB" />
	    </p>
	  </cmr:column>
	--%>
    <cmr:column span="2" containerForField="ClassCode">
    <p>
      <cmr:label fieldId="custClass">
        <cmr:fieldLabel fieldId="ClassCode" />:
           <cmr:delta text="-" id="delta-custClass" code="L" />
      </cmr:label>
      <cmr:field fieldId="ClassCode" id="custClass" path="custClass" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="GovIndicator">
      		<p>
        	  <cmr:label fieldId="govType"> <cmr:fieldLabel fieldId="GovIndicator" />: </cmr:label>
        	  <cmr:field path="govType" id="govType" fieldId="GovIndicator" tabId="MAIN_IBM_TAB" />
      		</p>
  </cmr:column>
<%--   <cmr:column span="2" containerForField="GoeIndicator"> --%>
<!--       		<p> -->
<%--         	  <cmr:label fieldId="goeType"> <cmr:fieldLabel fieldId="GoeIndicator" />: </cmr:label> --%>
<%--         	  <cmr:field path="goeType" id="goeType" fieldId="GoeIndicator" tabId="MAIN_IBM_TAB" /> --%>
<!--       		</p> -->
<%--   </cmr:column> --%>
  </cmr:row> 
  <cmr:row> 
  <cmr:column span="2" containerForField="ParentCompanyNo">
        <p>
          <cmr:label fieldId="dealerNo">
            <cmr:fieldLabel fieldId="ParentCompanyNo" />:
            <cmr:info text="${ui.info.parentcompanyNo}"></cmr:info>
          </cmr:label>
          <cmr:field path="dealerNo" id="dealerNo" fieldId="ParentCompanyNo" tabId="MAIN_IBM_TAB" />
        </p>
    </cmr:column>
</cmr:row>  

</cmr:view>
