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

<cmr:view forCountry="897">
  <cmr:row>
    <cmr:column span="2" containerForField="RestrictTo" >
      <p>
        <label for="restrictTo"> 
          <cmr:fieldLabel fieldId="RestrictTo" />: 
          <cmr:delta text="${rdcdata.restrictTo}" oldValue="${reqentry.restrictTo}" code="L"/>
        </label>
        <cmr:field path="restrictTo" id="restrictTo" fieldId="RestrictTo" tabId="MAIN_CUST_TAB" />

        <cmr:field fieldId="RestrictedInd" path="restrictInd" tabId="MAIN_CUST_TAB" />
        <cmr:label fieldId="RestrictInd" forRadioOrCheckbox="true">
          <cmr:fieldLabel fieldId="RestrictedInd" />
        </cmr:label>

      </p>
    </cmr:column>
    <cmr:column span="1" containerForField="OEMInd" width="70">
      <p>
        <label for="oemInd"> &nbsp; </label>
        <cmr:field fieldId="OEMInd" path="oemInd" tabId="MAIN_CUST_TAB" />
        <cmr:label fieldId="OEMInd" forRadioOrCheckbox="true">
          <cmr:fieldLabel fieldId="OEMInd" />
        </cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="1" containerForField="OutOfCityLimits" width="130">
      <p>
        <label for="outCityLimit"> &nbsp; </label>
        <cmr:field fieldId="OutOfCityLimits" path="outCityLimit" tabId="MAIN_CUST_TAB" />
        <cmr:label fieldId="OutOfCityLimits" forRadioOrCheckbox="true">
          <cmr:fieldLabel fieldId="OutOfCityLimits" />
        </cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="1" containerForField="FederalSiteInd" width="130">
      <p>
        <label for="fedSiteInd"> &nbsp; </label>
        <cmr:field fieldId="FederalSiteInd" path="fedSiteInd" tabId="MAIN_CUST_TAB" />
        <cmr:label fieldId="FederalSiteInd" forRadioOrCheckbox="true">
          <cmr:fieldLabel fieldId="FederalSiteInd" />
        </cmr:label>
      </p>
    </cmr:column>
  </cmr:row>


  <cmr:row>
    <cmr:column span="2" containerForField="NonIBMCompInd">
      <p>
        <cmr:label fieldId="NonIBMCompInd">
          <cmr:fieldLabel fieldId="NonIBMCompInd" />:
          <cmr:delta text="${rdcdata.nonIbmCompanyInd}" oldValue="${reqentry.nonIbmCompanyInd}" code="L"/>
        </cmr:label>
        <cmr:field fieldId="NonIBMCompInd" path="nonIbmCompanyInd" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CSOSite">
      <p>
        <label for="csoSite"> 
          <cmr:fieldLabel fieldId="CSOSite" />: 
          <cmr:delta text="${rdcdata.csoSite}" oldValue="${reqentry.csoSite}"/>
        </label>
        <cmr:field path="csoSite" id="csoSite" fieldId="CSOSite" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2" containerForField="ICCTaxClass">
      <p>
        <label for="iccTaxClass"> 
          <cmr:fieldLabel fieldId="ICCTaxClass" />: 
          <cmr:delta text="${rdcdata.iccTaxClass}" oldValue="${reqentry.iccTaxClass}"/>
        </label>
        <cmr:field path="iccTaxClass" id="iccTaxClass" fieldId="ICCTaxClass" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="ICCTaxExemptStatus">
      <p>
        <label for="iccTaxExemptStatus"> 
          <cmr:fieldLabel fieldId="ICCTaxExemptStatus" />: 
          <cmr:delta text="${rdcdata.iccTaxExemptStatus}" oldValue="${reqentry.iccTaxExemptStatus}"/>
        </label>
        <cmr:field path="iccTaxExemptStatus" id="iccTaxExemptStatus" fieldId="ICCTaxExemptStatus" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2" containerForField="SizeCode">
      <p>
        <label for="sizeCd"> 
          <cmr:fieldLabel fieldId="SizeCode" />: 
          <cmr:delta text="${rdcdata.sizeCd}" oldValue="${reqentry.sizeCd}"/>
        </label>
        <cmr:field path="sizeCd" id="sizeCd" fieldId="SizeCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="MiscBillCode">
      <p>
        <label for="miscBillCd"> 
          <cmr:fieldLabel fieldId="MiscBillCode" />:
          <cmr:delta text="${rdcdata.miscBillCd}" oldValue="${reqentry.miscBillCd}"/>
        </label>
        <cmr:field path="miscBillCd" id="miscBillCd" fieldId="MiscBillCode" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <cmr:column span="2" containerForField="BPAccountType">
      <p>
        <label for="bpAcctTyp"> 
          <cmr:fieldLabel fieldId="BPAccountType" />: 
          <cmr:delta text="${rdcdata.bpAcctTyp}" oldValue="${reqentry.bpAcctTyp}"/>
        </label>
        <cmr:field path="bpAcctTyp" id="bpAcctTyp" fieldId="BPAccountType" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="BPName">
      <p>
        <label for="bpName"> 
          <cmr:fieldLabel fieldId="BPName" />: 
          <cmr:delta text="${rdcdata.bpName}" oldValue="${reqentry.bpName}" code="L"/>
        </label>
        <cmr:field path="bpName" id="bpName" fieldId="BPName" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="InternalDivision">
      <p>
        <label for="div"> <cmr:fieldLabel fieldId="InternalDivision" />: </label>
        <cmr:field path="div" id="div" fieldId="InternalDivision" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="InternalDivDept">
      <p>
        <label for="dept_int"> <cmr:fieldLabel fieldId="InternalDivDept" />: </label>
        <cmr:field path="dept" id="dept_int" fieldId="InternalDivDept" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="InternalUser">
      <p>
        <label for="user"> <cmr:fieldLabel fieldId="InternalUser" />: </label>
        <cmr:field path="user" id="user" fieldId="InternalUser" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="InternalFunction">
      <p>
        <label for="func"> <cmr:fieldLabel fieldId="InternalFunction" />: </label>
        <cmr:field path="func" id="func" fieldId="InternalFunction" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="InternalLocation">
      <p>
        <label for="loc"> <cmr:fieldLabel fieldId="InternalLocation" />: </label>
        <cmr:field path="loc" id="loc" fieldId="InternalLocation" tabId="MAIN_CUST_TAB" />
      </p>
    </cmr:column>
  </cmr:row>
</cmr:view>