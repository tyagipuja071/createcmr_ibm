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

<cmr:view forGEO="CEMEA">
  <!-- Add hidden fields to keep imported values -->
  <form:hidden path="orgNo" />
  <form:hidden path="sourceCd" />
  <form:hidden path="mrcCd" />
  <form:hidden path="sitePartyId" />
  <form:hidden path="searchTerm" />

  <cmr:row addBackground="true">
  	<cmr:column span="2" containerForField="Phone3" forCountry="642">
      <p>
        <cmr:label fieldId="phone3">
          <cmr:fieldLabel fieldId="Phone3" />:
        <cmr:delta text="-" id="delta-phone3" />
        </cmr:label>
        <cmr:field fieldId="Phone3" id="phone3" path="phone3" />
      </p>
    </cmr:column>  
    <!-- ICO & DIC fields for Slovakia -->
    <cmr:view forCountry="693">
      <cmr:column span="2" containerForField="Company">
        <p>
          <cmr:label fieldId="company">
            <cmr:fieldLabel fieldId="Company" />: 
              <cmr:delta text="${rdcdata.company}" oldValue="${reqentry.company}" />
          </cmr:label>
          <cmr:field fieldId="Company" id="company" path="company" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="LocalTax1">
        <p>
          <cmr:label fieldId="taxCd1">
            <cmr:fieldLabel fieldId="LocalTax1" />: 
              <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" />
          </cmr:label>
          <cmr:field fieldId="LocalTax1" id="taxCd1" path="taxCd1" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
    <cmr:view forCountry="668">
      <cmr:column span="2" containerForField="Company">
        <p>
          <cmr:label fieldId="company">
            <cmr:fieldLabel fieldId="Company" />: 
              <cmr:delta text="${rdcdata.company}" oldValue="${reqentry.company}" />
          </cmr:label>
          <cmr:field fieldId="Company" id="company" path="company" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="LocalTax1">
        <p>
          <cmr:label fieldId="taxCd1">
            <cmr:fieldLabel fieldId="LocalTax1" />:
              <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" />
          </cmr:label>
          <cmr:field fieldId="LocalTax1" id="taxCd1" path="taxCd1" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
    <!-- OIB field for Croatia -->
    <cmr:view forCountry="704">
      <cmr:column span="2" containerForField="LocalTax1">
        <p>
          <cmr:label fieldId="taxCd1">
            <cmr:fieldLabel fieldId="LocalTax1" />: 
              <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" />
          </cmr:label>
          <cmr:field fieldId="LocalTax1" id="taxCd1" path="taxCd1" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
  </cmr:row>
    <cmr:row addBackground="true">
      <!-- Fiscal code field for Romania -->
     <cmr:view forCountry="826">
      <cmr:column span="2" containerForField="LocalTax1">
        <p>
           <label for="taxCd1">
             <cmr:fieldLabel fieldId="LocalTax1" />: 
             <cmr:delta text="${rdcdata.taxCd1}" oldValue="${reqentry.taxCd1}" /> 
          </label>
          <cmr:field path="taxCd1" id="taxCd1" fieldId="LocalTax1" tabId="MAIN_CUST_TAB" />        
         </p>
      </cmr:column>
      <cmr:column span="2" containerForField="EndUserFiscalCode">
      <p>
       <cmr:label fieldId="EndUserFiscalCode">&nbsp;</cmr:label>
      <cmr:field fieldId="EndUserFiscalCode" id="endUserFiscalCode" path="taxCd2" tabId="MAIN_CUST_TAB" />
      <cmr:label fieldId="EndUserFiscalCode" forRadioOrCheckbox="true">
      <cmr:fieldLabel fieldId="EndUserFiscalCode" />      
      </cmr:label>
      </p>
      </cmr:column> 
    </cmr:view>
  </cmr:row>
  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="AbbrevLocation">
      <p>
        <label for="abbrevLocn"> <cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
        <cmr:field fieldId="AbbrevLocation" id="abbrevLocn" path="abbrevLocn" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>

    <cmr:view exceptForCountry="618">
      <cmr:column span="2" containerForField="TeleCoverageRep" forCountry="620,767,805,823,677,680,832">
        <p>
          <cmr:label fieldId="bpSalesRepNo">
            <cmr:fieldLabel fieldId="TeleCoverageRep" />:
              <cmr:delta text="" oldValue="" />
          </cmr:label>
          <cmr:field path="bpSalesRepNo" id="bpSalesRepNo" fieldId="TeleCoverageRep" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CommercialFinanced">
        <p>
          <cmr:label fieldId="commercialFinanced">
            <cmr:fieldLabel fieldId="CommercialFinanced" />:
              <cmr:delta text="${rdcdata.commercialFinanced}" oldValue="${reqentry.commercialFinanced}" />
          </cmr:label>
          <cmr:field path="commercialFinanced" id="commercialFinanced" fieldId="CommercialFinanced" tabId="MAIN_CUST_TAB" />
        </p>
      </cmr:column>
    </cmr:view>
  </cmr:row>

  <cmr:row addBackground="false">
    <cmr:column span="2" containerForField="EmbargoCode" exceptForCountry="618">
      <p>
        <cmr:label fieldId="embargoCd">
          <cmr:fieldLabel fieldId="EmbargoCode" />:
            <cmr:delta text="${rdcdata.embargoCd}" oldValue="${reqentry.embargoCd}" />
        </cmr:label>
        <cmr:field path="embargoCd" id="embargoCd" fieldId="EmbargoCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
     <!-- STC order block code field addn -->
  <cmr:column span="2" containerForField="TaxExemptStatus3">
      <p>
        <cmr:label fieldId="taxExemptStatus3">
          <cmr:fieldLabel fieldId="TaxExemptStatus3" />:
            <cmr:delta text="${rdcdata.taxExemptStatus3}" oldValue="${reqentry.taxExemptStatus3}" />
            <cmr:info text="${ui.info.StcOrderBlock}" /> 
        </cmr:label>
        <cmr:field path="taxExemptStatus3" id="taxExemptStatus3" fieldId="TaxExemptStatus3" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="OrdBlk" forCountry="618">
      <p>
        <cmr:label fieldId="ordBlk">
          <cmr:fieldLabel fieldId="OrdBlk" />:
            <cmr:delta text="${rdcdata.ordBlk}" oldValue="${reqentry.ordBlk}" />
        </cmr:label>
        <cmr:field path="ordBlk" id="ordBlk" fieldId="OrdBlk" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CustClass" forCountry="618,644,668,693,704,708,740,820,821,826,358,359,363,603,607,626,651,694,695,699,705,707,787,741,889,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729">
        <p>
          <cmr:label fieldId="custClass">
            <cmr:fieldLabel fieldId="CustClass" />:
          </cmr:label>
          <cmr:field path="custClass" id="custClass" fieldId="CustClass" tabId="MAIN_CUST_TAB" />
        </p>
    </cmr:column>
    <cmr:column span="2" containerForField="Phone1" exceptForCountry="618">
      <p>
        <cmr:label fieldId="phone1">
          <cmr:fieldLabel fieldId="Phone1" />:
        <cmr:delta text="-" id="delta-phone1" />
        </cmr:label>
        <cmr:field fieldId="Phone1" id="phone1" path="phone1" />
      </p>
    </cmr:column>    
        <cmr:column span="2" containerForField="TypeOfCustomer" forCountry="680">        
      <p>
      <label for="bpAcctTyp" style="width:300px"> 
        <cmr:label fieldId="bpAcctTyp">
          <cmr:fieldLabel fieldId="TypeOfCustomer" />:
             <cmr:delta text="${rdcdata.bpAcctTyp}" oldValue="${reqentry.typeofCustomer}" />
               <cmr:info text="${ui.info.typeofCustomer}" />
            </cmr:label>
          </label>
        <cmr:field path="bpAcctTyp" id="bpAcctTyp" fieldId="TypeOfCustomer" size="250" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>     
</cmr:view>
