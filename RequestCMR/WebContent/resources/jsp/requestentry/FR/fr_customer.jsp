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
    <cmr:column span="2" containerForField="AbbrevLocation">
      <p>
        <label for="abbrevLocn"> 
          <cmr:fieldLabel fieldId="AbbrevLocation" />: 
        </label>
        <cmr:field fieldId="AbbrevLocation" id="abbrevLocn" path="abbrevLocn" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="EmbargoCode">
      <p>
        <cmr:label fieldId="embargoCd">
          <cmr:fieldLabel fieldId="EmbargoCode" />:
            <cmr:delta text="${rdcdata.embargoCd}" oldValue="${reqentry.embargoCd}" />
        </cmr:label>
        <cmr:field path="embargoCd" id="embargoCd" fieldId="EmbargoCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="CurrencyCode">
      <p>
        <cmr:label fieldId="currencyCd">
          <cmr:fieldLabel fieldId="CurrencyCode" />:
          <cmr:delta text="${rdcdata.currencyCd}" oldValue="${reqentry.currencyCd}" id="delta-currencyCd" />
        </cmr:label>
        <cmr:field fieldId="CurrencyCode" id="currencyCd" path="currencyCd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>  
    <cmr:column span="2" containerForField="DoubleCreate">
        <p>
          <cmr:label fieldId="identClient">
            <cmr:fieldLabel fieldId="DoubleCreate" />:
          </cmr:label>
          <cmr:field path="identClient" id="identClient" fieldId="DoubleCreate" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
</cmr:view>