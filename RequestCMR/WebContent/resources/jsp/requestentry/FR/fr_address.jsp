<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
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
%>
<cmr:row topPad="10">
  <cmr:column span="4">

    <cmr:label fieldId="addrType">
      <cmr:fieldLabel fieldId="AddressType" />: 
          <cmr:delta text="-" id="delta-addrType" code="L" />
      <cmr:info text="${ui.info.addressType}" />
    </cmr:label>
     <div id="addrTypeCheckboxes" style="display: block">
      <cmr:field fieldId="AddressTypeInput" id="addrType" path="addrType" breakAfter="5" />
    </div>
    <div id="addrTypeStaticText" style="display: none">ZS01</div>
  </cmr:column>  
  <br>
  <br>
  <br>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm1">
      <cmr:fieldLabel fieldId="CustomerName1" />: 
      <cmr:info text="${ui.info.custNm1FR}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm1" />
    <cmr:field fieldId="CustomerName1" id="custNm1" path="custNm1" size="400" />
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm2">
      <cmr:fieldLabel fieldId="CustomerName2" />: 
      <cmr:info text="${ui.info.privateCustomerName}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm2" />
    <cmr:field fieldId="CustomerName2" id="custNm2" path="custNm2" size="400" />
  </cmr:column>
</cmr:row>
<!-- 
  CREATCMR-273 REMOVE UI FIELD(Customer Name/ Additional Address Information)
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm3">
      <cmr:fieldLabel fieldId="CustomerName3" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm3" />
    <cmr:field fieldId="CustomerName3" id="custNm3" path="custNm3" size="400" />
  </cmr:column>
</cmr:row> -->
<cmr:row addBackground="true">  
  &nbsp;
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="landCntry">
        <cmr:fieldLabel fieldId="LandedCountry" />:
             <cmr:delta text="-" id="delta-landCntry" code="R" />
      </cmr:label>
      <cmr:field fieldId="LandedCountry" id="landCntry" path="landCntry" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="StreetAddress1">
    <p>
      <cmr:label fieldId="addrTxt">
        <cmr:fieldLabel fieldId="StreetAddress1" />: 
             <cmr:delta text="-" id="delta-addrTxt" />
      </cmr:label>
      <cmr:field fieldId="StreetAddress1" id="addrTxt" path="addrTxt" />
    </p>
  </cmr:column>
  <%--  defect : 1444422 : removed for FR
  <cmr:column span="2" forCountry="706">
    <p>
      <cmr:label fieldId="translate">&nbsp;
      </cmr:label>
      Translate: 
      <a class="translate" href="javascript: cmr.openTranslateWindow('en','fr', ['custNm1', 'custNm2', 'custNm3', 'city1', 'streetAddress1', 'streetAddress2'])" >to Local Language</a>
      <a class="translate" href="javascript: cmr.openTranslateWindow('fr','en', ['custNm1', 'custNm2', 'custNm3', 'city1', 'streetAddress1', 'streetAddress2'])" >to English</a>
    </p>
  </cmr:column>
  --%>
</cmr:row>

<cmr:row>
  <cmr:column span="2" containerForField="CustomerName3">
    <p>
      <cmr:label fieldId="custNm3">
        <cmr:fieldLabel fieldId="CustomerName3" />: 
             <cmr:delta text="-" id="delta-custNm3" />
      </cmr:label>
      <cmr:field fieldId="CustomerName3" id="custNm3" path="custNm3" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="CustomerName4">
    <p>
      <cmr:label fieldId="custNm4">
        <cmr:fieldLabel fieldId="CustomerName4" />: 
             <cmr:delta text="-" id="delta-custNm4" />
      </cmr:label>
      <cmr:field fieldId="CustomerName4" id="custNm4" path="custNm4" />
    </p>
  </cmr:column>
  <%-- 
  CREATCMR-273 REMOVE UI FIELD(Street Continuation) 
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrTxt2">
        <cmr:fieldLabel fieldId="StreetAddress2" />: 
          </cmr:label>
      <cmr:field fieldId="StreetAddress2" id="addrTxt2" path="addrTxt2" />
    </p>
  </cmr:column> --%>
</cmr:row>

<cmr:row>
  <cmr:column span="2" >
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="City1" />:
             <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:field fieldId="City1" id="city1" path="city1" />
    </p>
  </cmr:column>
  
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="postCd">
        <cmr:fieldLabel fieldId="PostalCode" />:
             <cmr:delta text="-" id="delta-postCd" />
        <!--     <cmr:info text="${ui.info.postalCodeFormatFR}" /> -->  
      </cmr:label>
      <cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row>
  <cmr:column span="2" containerForField="CustPhone">
    <p>
      <cmr:label fieldId="custPhone">
        <cmr:fieldLabel fieldId="CustPhone" />:
             <cmr:delta text="-" id="delta-custPhone" />
      </cmr:label>
      <cmr:field fieldId="CustPhone" id="custPhone" path="custPhone" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="POBox">
    <p>
      <cmr:label fieldId="poBox">
        <cmr:fieldLabel fieldId="POBox" />:
             <cmr:delta text="-" id="delta-poBox" />
      </cmr:label>
      <cmr:field fieldId="POBox" id="poBox" path="poBox" />
    </p>
  </cmr:column>
</cmr:row>
<%-- 
  CREATCMR-273 REMOVE UI FIELD(Contact Person) 
<cmr:row>
  <cmr:column span="2" containerForField="CustomerName4">
    <p>
      <cmr:label fieldId="custNm4">
        <cmr:fieldLabel fieldId="CustomerName4" />:
             <cmr:delta text="-" id="delta-custNm4" />
      </cmr:label>
      <cmr:field fieldId="CustomerName4" id="custNm4" path="custNm4" />
    </p>
  </cmr:column>
</cmr:row> --%>
<cmr:row topPad="10" addBackground="true">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="sapNo" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SAPNumber" />:</cmr:label>
      <cmr:delta text="-" id="delta-sapNo" />
      <cmr:field fieldId="SAPNumber" id="sapNo" path="sapNo" />
    </p>
  </cmr:column>
  <!-- defect : 1444422  -->
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="ierpSitePrtyId">
        <cmr:fieldLabel fieldId="IERPSitePrtyId" />:</cmr:label>
      <cmr:delta text="-" id="delta-ierpSitePrtyId" />
      <cmr:field fieldId="IERPSitePrtyId" id="ierpSitePrtyId" path="ierpSitePrtyId" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:view exceptForCountry="758">
<cmr:row addBackground="true">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrCreateDt">
        <cmr:fieldLabel fieldId="RDcCreateDate" />:
          </cmr:label>
    <div id="addrCreateDt_updt">-</div>
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrUpdateDt">
        <cmr:fieldLabel fieldId="RDCLastUpdateDate" />:
          </cmr:label>
    <div id="addrUpdateDt_updt">-</div>
    </p>
  </cmr:column>
</cmr:row>
<form:hidden path="stateProv" />
</cmr:view>