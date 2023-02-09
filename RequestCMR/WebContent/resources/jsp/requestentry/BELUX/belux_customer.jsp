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
<cmr:row addBackground="true">
 <cmr:view forCountry="624">
 <% if("624LU".equalsIgnoreCase(reqentry.getCountryUse())) {%>
    <cmr:column span="2" containerForField="LocalTax_LU">
      <p>
        <cmr:label fieldId="taxCd1">
          <cmr:fieldLabel fieldId="LocalTax1" />: 
              <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}"/>
        </cmr:label>
        <cmr:field path="taxCd1" id="taxCd1" fieldId="LocalTax_LU" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  	<% } else if ("624".equalsIgnoreCase(reqentry.getCountryUse())) {%>
    <cmr:column span="2" containerForField="LocalTax_BE">
      <p>
        <cmr:label fieldId="taxCd1">
          <cmr:fieldLabel fieldId="LocalTax1" />: 
              <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}"/>
        </cmr:label>
        <cmr:field path="taxCd1" id="taxCd1" fieldId="LocalTax_BE" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  	<% } %>
	</cmr:view>
</cmr:row>
	
  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="AbbrevLocation">
      <p>
        <label for="abbrevLocn"> <cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
        <cmr:field fieldId="AbbrevLocation" id="abbrevLocn" path="abbrevLocn" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>

    <cmr:column span="2" containerForField="CollectionCd">
      <p>
        <cmr:label fieldId="collectionCd">
          <cmr:fieldLabel fieldId="CollectionCd" />: 
              <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-collectionCd" />
        </cmr:label>
        <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_CUST_TAB" />
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
  <cmr:row  addBackground="false">
        <cmr:column span="2" containerForField="InternalDept">
        <p>
          <cmr:label fieldId="ibmDeptCostCenter">
            <cmr:fieldLabel fieldId="InternalDept" />: 
          </cmr:label>
          <cmr:field path="ibmDeptCostCenter" id="ibmDeptCostCenter" fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <c:if test="${reqentry.reqType == 'U'}">
		<cmr:column span="2" containerForField="ModeOfPayment">
		 <p>
			<cmr:label fieldId="modeOfPayment">
				<cmr:fieldLabel fieldId="ModeOfPayment" />: 
			</cmr:label>
			<cmr:field path="modeOfPayment" id="modeOfPayment" fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />		 
		 </p>
		</cmr:column>
	  </c:if>
    </cmr:row>
</cmr:view>

