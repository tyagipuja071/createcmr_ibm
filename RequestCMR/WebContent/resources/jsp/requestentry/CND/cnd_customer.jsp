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

<cmr:view forGEO="CND">
<cmr:row addBackground="true">
    <cmr:column span="2" containerForField="PrivacyIndc">
      <p>
        <label for="privIndc"> <cmr:fieldLabel fieldId="PrivacyIndc" />: 
        </label>
        <cmr:field fieldId="PrivacyIndc" id="privIndc" path="privIndc" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="OrderBlock">
      <p>
        <label for="ordBlk"> 
          <cmr:fieldLabel fieldId="OrderBlock" />: 
          <cmr:delta text="${rdcdata.ordBlk}" oldValue="${reqentry.ordBlk}" />
        </label>
        <cmr:field path="ordBlk" id="ordBlk" fieldId="OrderBlock" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="ModeOfPayment">
      <p>
        <label for="modeOfPayment"> 
          <cmr:fieldLabel fieldId="ModeOfPayment" />: 
          <cmr:delta text="${rdcdata.modeOfPayment}" oldValue="${reqentry.modeOfPayment}" />
        </label>
        <cmr:field path="modeOfPayment" id="modeOfPayment" fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
 
</cmr:view>
