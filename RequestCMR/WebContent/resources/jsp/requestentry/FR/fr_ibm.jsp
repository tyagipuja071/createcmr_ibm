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
<cmr:view forGEO="FR">
  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="SalRepNameNo">
      <p>
        <cmr:label fieldId="repTeamMemberNo">
          <cmr:fieldLabel fieldId="SalRepNameNo" />:
           <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" id="delta-repTeamMemberNo" />
        </cmr:label>
        <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SalesBusOff" >
      <p>
        <cmr:label fieldId="salesBusOffCd">
          <cmr:fieldLabel fieldId="SalesBusOff" />:
             <cmr:delta text="${rdcdata.salesBusOffCd}" oldValue="${reqentry.salesBusOffCd}" id="delta-salesBusOffCd" />
        </cmr:label>
        <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="InstallBranchOff">
      <p>
        <cmr:label fieldId="installBranchOff">
        <cmr:fieldLabel fieldId="InstallBranchOff" />: 
           <cmr:delta text="${rdcdata.installBranchOff}" oldValue="${reqentry.installBranchOff}" id="delta-installBranchOff" />
        </cmr:label>
        <cmr:field path="installBranchOff" id="installBranchOff" fieldId="InstallBranchOff" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="InternalDept">
      <p>
        <cmr:label fieldId="ibmDeptCostCenter">
          <cmr:fieldLabel fieldId="InternalDept" />: 
        </cmr:label>
        <cmr:field path="ibmDeptCostCenter" id="ibmDeptCostCenter" fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>  
    <cmr:column span="2" containerForField="CollectionCd">
      <p>
        <cmr:label fieldId="collectionCd">
        <cmr:fieldLabel fieldId="CollectionCd" />: 
           <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-collectionCd" />
        </cmr:label>
        <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">    
    <cmr:column span="2" containerForField="PrivIndc">
      <p>
        <cmr:label fieldId="privIndc">
          <cmr:fieldLabel fieldId="PrivIndc" />:
        </cmr:label>
        <cmr:field fieldId="PrivIndc" id="privIndc" path="privIndc" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="DuplicateCMR">
      <p>
        <cmr:label fieldId="dupCmrIndc">
          <cmr:fieldLabel fieldId="DuplicateCMR" />:
        </cmr:label>
        <cmr:field fieldId="DuplicateCMR" id="dupCmrIndc" path="dupCmrIndc" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column> 
    <%
      if (("U").equalsIgnoreCase(reqentry.getReqType())) {
    %>      
    <cmr:column span="2" containerForField="CommercialFinanced">
      <p>
        <cmr:label fieldId="commercialFinanced">
          <cmr:fieldLabel fieldId="CommercialFinanced" />:
        </cmr:label>
        <cmr:field fieldId="CommercialFinanced" id="commercialFinanced" path="commercialFinanced" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <% } %>
  </cmr:row>  
</cmr:view>