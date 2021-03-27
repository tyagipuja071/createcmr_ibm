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
<cmr:view forGEO="KR">
  <cmr:row topPad="10">
        <cmr:column span="2" containerForField="EngineeringBo">
      <p>
        <cmr:label fieldId="engineeringBo">
          <cmr:fieldLabel fieldId="EngineeringBo" />:
           <cmr:delta text="${rdcdata.engineeringBo}" oldValue="${reqentry.engineeringBo}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field fieldId="EngineeringBo" id="engineeringBo" path="engineeringBo" tabId="MAIN_IBM_TAB"/>
      </p>
    </cmr:column>  

    <cmr:column span="2" containerForField="CmrNoPrefix">
        <p>
          <cmr:label fieldId="cmrNoPrefix">
            <cmr:fieldLabel fieldId="CmrNoPrefix" />:
          </cmr:label>
          <cmr:field path="cmrNoPrefix" id="cmrNoPrefix" fieldId="CmrNoPrefix" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
  </cmr:row>
  
</cmr:view>