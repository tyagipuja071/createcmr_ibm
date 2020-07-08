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

<cmr:view forGEO="MCO,MCO1,MCO2">

  <!-- Add hidden fields to keep imported values -->
  <form:hidden path="orgNo" />
  <form:hidden path="sourceCd" />
  <form:hidden path="mrcCd" />
  <form:hidden path="sitePartyId" />
  <form:hidden path="searchTerm" />

  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="LocationNumber" forCountry="838">
      <p>
        <cmr:label fieldId="locationNumber">
          <cmr:fieldLabel fieldId="LocationNumber" />: 
            <cmr:delta text="${rdcdata.locationNumber}" oldValue="${reqentry.locationNo}" id="delta-locationNumber" />
        </cmr:label>
            <cmr:field path="locationNo" id="locationNumber" fieldId="LocationNumber" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SpecialTaxCd" forCountry="822">
      <p>
        <label for="specialTaxCd"> <cmr:fieldLabel fieldId="SpecialTaxCd" />: </label>
        <cmr:field fieldId="SpecialTaxCd" id="specialTaxCd" path="specialTaxCd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:view forGEO="MCO1,MCO2">
      <cmr:column span="2" containerForField="SpecialTaxCd">
        <p>
          <label for="specialTaxCd"> <cmr:fieldLabel fieldId="SpecialTaxCd" />: </label>
          <cmr:field fieldId="SpecialTaxCd" id="specialTaxCd" path="specialTaxCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
    <cmr:column span="2" containerForField="AbbrevLocation">
      <p>
        <label for="abbrevLocn"> 
          <cmr:fieldLabel fieldId="AbbrevLocation" />: 
          <cmr:delta text="${rdcdata.abbrevLocn}" oldValue="${reqentry.abbrevLocn}" />
        </label>
        <cmr:field fieldId="AbbrevLocation" id="abbrevLocn" path="abbrevLocn" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
     <cmr:view forGEO="MCO,MCO1,MCO2">
      <cmr:column span="2" containerForField="EmbargoCode">
      <p>
        <cmr:label fieldId="embargoCd">
          <cmr:fieldLabel fieldId="EmbargoCode" />:
            <cmr:delta text="${rdcdata.embargoCd}" oldValue="${reqentry.embargoCd}" />
        </cmr:label>
        <cmr:field path="embargoCd" id="embargoCd" fieldId="EmbargoCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
	</cmr:view>
    <cmr:view forGEO="MCO1">
    <cmr:column span="2" containerForField="CommercialFinanced">
      <p>
        <cmr:label fieldId="commercialFinanced">
          <cmr:fieldLabel fieldId="CommercialFinanced" />:
            <cmr:delta text="${rdcdata.commercialFinanced}" oldValue="${reqentry.commercialFinanced}" />
        </cmr:label>
        <cmr:field path="commercialFinanced" id="commercialFinanced" fieldId="CommercialFinanced" tabId="MAIN_CUST_TAB"/>
      </p>
    </cmr:column>
    </cmr:view>
  </cmr:row>

  <cmr:view forCountry="XXXX">
    <cmr:row addBackground="false">
      <cmr:column span="2" containerForField="OrgNo">
        <p>
          <label for="orgNo"> <cmr:fieldLabel fieldId="OrgNo" />: </label>
          <cmr:field fieldId="OrgNo" id="orgNo" path="orgNo" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="SourceCd">
        <p>
          <label for="sourceCd"> <cmr:fieldLabel fieldId="SourceCd" />: </label>
          <cmr:field fieldId="SourceCd" id="sourceCd" path="sourceCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="AcAdminBo">
        <p>
          <label for="acAdminBo"> <cmr:fieldLabel fieldId="AcAdminBo" />: </label>
          <cmr:field fieldId="AcAdminBo" id="acAdminBo" path="acAdminBo" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
  </cmr:view>
   
  <cmr:view forCountry="822,838">
    <cmr:row addBackground="true">
      <cmr:column span="2" containerForField="CollectionCd">
        <p>
            <cmr:label fieldId="collectionCd">
              <cmr:fieldLabel fieldId="CollectionCd" />: 
              <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-collectionCd" />
            </cmr:label>
            <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="ModeOfPayment">
        <p>
          <cmr:label fieldId="modeOfPayment">
            <cmr:fieldLabel fieldId="ModeOfPayment" />: 
            <cmr:delta text="${rdcdata.modeOfPayment}" oldValue="${reqentry.paymentMode}" id="delta-modeOfPayment" />
          </cmr:label>
          <cmr:field path="paymentMode" id="modeOfPayment" fieldId="ModeOfPayment" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
  </cmr:view>
  
  <cmr:view forCountry="822">
    <cmr:row addBackground="false">
      <cmr:column span="2" containerForField="DistrictCd">
        <p>
          <label for="territoryCd"> <cmr:fieldLabel fieldId="DistrictCd" />: </label>
          <cmr:field fieldId="DistrictCd" id="territoryCd" path="territoryCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
  </cmr:view>
	
  <cmr:view forCountry="838">
    <cmr:row addBackground="false">
      <cmr:column span="2" containerForField="SpecialTaxCd">
        <p>
          <label for="specialTaxCd"> <cmr:fieldLabel fieldId="SpecialTaxCd" />: </label>
          <cmr:field fieldId="SpecialTaxCd" id="specialTaxCd" path="specialTaxCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CurrencyCode">
        <p>
          <cmr:label fieldId="legacyCurrencyCd">
            <cmr:fieldLabel fieldId="CurrencyCd" />: 
            <cmr:delta text="${rdcdata.legacyCurrencyCd}" oldValue="${reqentry.legacyCurrencyCd}" id="delta-legacyCurrencyCd" />
          </cmr:label>
          <cmr:field path="legacyCurrencyCd" id="legacyCurrencyCd" fieldId="CurrencyCd" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>   
     <cmr:row topPad="10" addBackground="false">
    <cmr:column span="2" containerForField="IbmDeptCostCenter">
      <p>
        <cmr:label fieldId="ibmDeptCostCenter">
          <cmr:fieldLabel fieldId="IbmDeptCostCenter" />: 
          </cmr:label>
        <cmr:field fieldId="IbmDeptCostCenter" id="ibmDeptCostCenter" path="ibmDeptCostCenter" tabId="MAIN_IBM_TAB" />
    </cmr:column>
    <cmr:column span="2" containerForField="CustClass">
      <p>
        <cmr:label fieldId="custClass">
          <cmr:fieldLabel fieldId="CustClass" />: 
          </cmr:label>
        <cmr:field fieldId="CustClass" id="custClass" path="custClass" tabId="MAIN_IBM_TAB" />
    </cmr:column>
    </cmr:row>
    </cmr:row>
  </cmr:view>
  
  <cmr:view forCountry="838">
    <cmr:row addBackground="true">
      <cmr:column span="2" containerForField="MailingCond">
        <p>
          <cmr:label fieldId="mailingCondition">
            <cmr:fieldLabel fieldId="MailingCond" />: 
            <cmr:delta text="${rdcdata.mailingCondition}" oldValue="${reqentry.mailingCondition}" />
          </cmr:label>
          <cmr:field path="mailingCondition" id="mailingCondition" fieldId="MailingCond" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
  </cmr:view>
  <cmr:view forCountry="838">
    <form:hidden path="acAdminBo" id="acAdminBo"/>
  </cmr:view>
    
  <form:hidden path="economicCd" id="economicCd"/>
</cmr:view>