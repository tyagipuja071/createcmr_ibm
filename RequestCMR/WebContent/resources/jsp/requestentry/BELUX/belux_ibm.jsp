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
<cmr:view forGEO="BELUX">
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

    <cmr:column span="2" containerForField="EconomicCd2">
      <p>
        <cmr:label fieldId="economicCd">
          <cmr:fieldLabel fieldId="EconomicCd2" />:
           <cmr:delta text="${rdcdata.economicCd}" oldValue="${reqentry.economicCd}" code="R" />
        </cmr:label>
        <cmr:field path="economicCd" id="economicCd" fieldId="EconomicCd2" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
</cmr:view>