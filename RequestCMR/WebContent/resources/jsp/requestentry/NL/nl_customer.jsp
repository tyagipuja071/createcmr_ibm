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

<cmr:view forGEO="NL">

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
    <cmr:column span="2" containerForField="LocalTax2">
        <p>
          <label for="taxCd2"> <cmr:fieldLabel fieldId="LocalTax2" />:
           <cmr:delta text="${rdcdata.taxCd2}" oldValue="${reqentry.taxCd2}" /> </label>
          <cmr:field path="taxCd2" id="taxCd2" fieldId="LocalTax2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
  </cmr:row>
  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="EmbargoCode">
      <p>
        <cmr:label fieldId="embargoCd">
          <cmr:fieldLabel fieldId="EmbargoCode" />:
            <cmr:delta text="${rdcdata.embargoCd}" oldValue="${reqentry.embargoCd}" />
        </cmr:label>
        <cmr:field path="embargoCd" id="embargoCd" fieldId="EmbargoCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="EconomicCd2">
        <p>
          <cmr:label fieldId="economicCd">
            <cmr:fieldLabel fieldId="EconomicCd2" />:
           <cmr:delta text="${rdcdata.economicCd}" oldValue="${reqentry.economicCd}" code="R" />
          </cmr:label>
          <cmr:field path="economicCd" id="economicCd" fieldId="EconomicCd2" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>

    <cmr:column span="2" containerForField="InternalDept">
        <p>
          <cmr:label fieldId="ibmDeptCostCenter">
            <cmr:fieldLabel fieldId="InternalDept" />: 
            <cmr:delta text="${rdcdata.ibmDeptCostCenter}" oldValue="${reqentry.ibmDeptCostCenter}" />
          </cmr:label>
          <cmr:field path="ibmDeptCostCenter" id="ibmDeptCostCenter" fieldId="InternalDept" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    
  </cmr:row>
    <cmr:row  addBackground="false">
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

