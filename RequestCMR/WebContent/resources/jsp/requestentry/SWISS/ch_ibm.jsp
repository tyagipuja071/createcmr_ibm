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
<cmr:view forGEO="SWISS">
  <cmr:column span="2" containerForField="InternalDept">
        <p>
          <cmr:label fieldId="ibmDeptCostCenter">
            <cmr:fieldLabel fieldId="InternalDept" />: 
            <cmr:delta text="${rdcdata.ibmDeptCostCenter}" oldValue="${reqentry.ibmDeptCostCenter}" />
          </cmr:label>
          <cmr:field path="ibmDeptCostCenter" id="ibmDeptCostCenter" fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      </cmr:view>