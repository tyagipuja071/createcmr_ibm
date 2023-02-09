<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page
	import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
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

<cmr:view forCountry="848">
    <form:hidden path="currencyCd" />
	<cmr:row>
		<cmr:column span="2" containerForField="LocalTax1">
			<p>
				<label for="taxCd1"> <cmr:fieldLabel fieldId="LocalTax1" />:
					<cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" />
				</label>
				<cmr:field path="taxCd1" id="taxCd1" fieldId="LocalTax1"
					tabId="MAIN_CUST_TAB" />
			</p>
		</cmr:column>
	</cmr:row>	
	<cmr:row addBackground="false">
		<cmr:column span="2" containerForField="CustClass">
			<p>
				<cmr:label fieldId="custClass">
					<cmr:fieldLabel fieldId="CustClass" />: 
            <cmr:delta text="${rdcdata.custClass}"
						oldValue="${reqentry.custClass}" code="L" />
				</cmr:label>
				<cmr:field fieldId="CustClass" id="custClass" path="custClass"
					tabId="MAIN_CUST_TAB" />
		</cmr:column>
				<cmr:column span="2" containerForField="EmbargoCode">
			<p>
				<cmr:label fieldId="embargoCd">
					<cmr:fieldLabel fieldId="EmbargoCode" />:
            <cmr:delta text="${rdcdata.ordBlk}"
						oldValue="${reqentry.ordBlk}" />
				</cmr:label>
				<cmr:field path="ordBlk" id="ordBlk" fieldId="EmbargoCode"
					tabId="MAIN_CUST_TAB" />
			</p>
		</cmr:column>
	</cmr:row>
</cmr:view>
