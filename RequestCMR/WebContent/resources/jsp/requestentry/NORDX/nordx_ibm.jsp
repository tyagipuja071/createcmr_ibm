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
<cmr:view forGEO="NORDX">
  <cmr:row topPad="10">
    <cmr:column span="2" containerForField="SalesBusOff">
      <p>
        <cmr:label fieldId="salesBusOffCd">
          <cmr:fieldLabel fieldId="SalesBusOff" />:
             <cmr:delta text="${rdcdata.salesBusOffCd}" oldValue="${reqentry.salesBusOffCd}" id="delta-salesBusOffCd" />
        </cmr:label>
        <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
        <cmr:column span="2" containerForField="SalRepNameNo">
      <p>
        <cmr:label fieldId="repTeamMemberNo">
          <cmr:fieldLabel fieldId="SalRepNameNo" />:
           <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" id="delta-repTeamMemberNo" />
        </cmr:label>
        <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="SearchTerm">
      <p>
        <cmr:label fieldId="searchTerm">
          <cmr:fieldLabel fieldId="SearchTerm" />:
           <cmr:delta text="${rdcdata.searchTerm}" oldValue="${reqentry.searchTerm}" id="delta-searchTerm" />
        </cmr:label>
        <cmr:field fieldId="SearchTerm" id="searchTerm" path="searchTerm" tabId="MAIN_IBM_TAB"/>
        <!-- CREATCMR-5358 -->
        <input type="hidden" id="rdcSearchTerm" value="${rdcdata.searchTerm}" />
      </p>
    </cmr:column>
    
    <cmr:column span="2" containerForField="EngineeringBo">
      <p>
        <cmr:label fieldId="engineeringBo">
          <cmr:fieldLabel fieldId="EngineeringBo" />:
           <cmr:delta text="${rdcdata.engineeringBo}" oldValue="${reqentry.engineeringBo}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field fieldId="EngineeringBo" id="engineeringBo" path="engineeringBo" tabId="MAIN_IBM_TAB"/>
      </p>
    </cmr:column> 
    
  </cmr:row>
</cmr:view>