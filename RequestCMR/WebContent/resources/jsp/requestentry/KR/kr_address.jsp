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
      <cmr:field fieldId="AddressTypeInput" id="addrType" path="addrType" />
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
      <cmr:info text="${ui.info.custNm1BENELUX}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm1" />
    <cmr:field fieldId="CustomerName1" id="custNm1" path="custNm1" size="400" />
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm2">
      <cmr:fieldLabel fieldId="CustomerName2" />: 
      <cmr:info text="${ui.info.custNm2BENELUX}" /> 
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm2" />
    <cmr:field fieldId="CustomerName2" id="custNm2" path="custNm2" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true" topPad="10">
  <cmr:column span="4">
    <cmr:label fieldId="custNm3" >
      <cmr:fieldLabel fieldId="CustomerName3" />: 
      <cmr:info text="${ui.info.custNm3BENELUX}" /> 
          </cmr:label>
    <cmr:field fieldId="CustomerName3" id="custNm3" path="custNm3" size="400" />
    <cmr:delta text="-" id="delta-custNm3" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm4">
      <cmr:fieldLabel fieldId="CustomerName4" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm4" />
    <cmr:field fieldId="CustomerName4" id="custNm4" path="custNm4" size="400" />
  </cmr:column>
</cmr:row>
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
  <cmr:column span="2" containerForField="StateProv">
    <p>
      <cmr:label fieldId="stateProv">
        <cmr:fieldLabel fieldId="StateProv" />:
             <cmr:delta text="-" id="delta-stateProv" code="R" />
      </cmr:label>
      <cmr:field fieldId="StateProv" id="stateProv" path="stateProv" />
    </p>
   </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2">
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
      <cmr:label fieldId="city2">
        <cmr:fieldLabel fieldId="City2" />:
             <cmr:delta text="-" id="delta-city2" />
      </cmr:label>
      <cmr:field fieldId="City2" id="city2" path="city2" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrTxt">
        <cmr:fieldLabel fieldId="StreetAddress1" />: 
             <cmr:delta text="-" id="delta-addrTxt" />
      </cmr:label>
      <cmr:field fieldId="StreetAddress1" id="addrTxt" path="addrTxt" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrTxt2">
        <cmr:fieldLabel fieldId="StreetAddress2" />: 
        <cmr:delta text="-" id="delta-addrTxt2" />
      </cmr:label>
      <cmr:field fieldId="StreetAddress2" id="addrTxt2" path="addrTxt2" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="postCd">
        <cmr:fieldLabel fieldId="PostalCode" />:
             <cmr:delta text="-" id="delta-postCd" />
      </cmr:label>
      <cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
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

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="transportZone">
        <cmr:fieldLabel fieldId="TransportZone" />:
             <cmr:delta text="-" id="delta-transportZone" />
      </cmr:label>
      <cmr:field fieldId="TransportZone" id="transportZone" path="transportZone" />
    </p>
  </cmr:column>
  <cmr:column span="2" >
    <p>
      <cmr:label fieldId="contact">
        <cmr:fieldLabel fieldId="Contact" />:
             <cmr:delta text="-" id="delta-contact" />
      </cmr:label>
      <cmr:field fieldId="Contact" id="contact" path="contact" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      <cmr:field fieldId="Department" id="dept" path="dept" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="floor">
        <cmr:fieldLabel fieldId="Floor" />:
             <cmr:delta text="-" id="delta-floor" />
      </cmr:label>
      <cmr:field fieldId="Floor" id="floor" path="floor" />
    </p>
  </cmr:column>
</cmr:row>

 <cmr:row>
    <cmr:column span="2">
      <p>
        <cmr:label fieldId="countyName">
          <cmr:fieldLabel fieldId="LocalLangCountryName" />: 
               <cmr:delta text="-" id="delta-countyName" />
        </cmr:label>
        <cmr:field fieldId="LocalLangCountryName" id="countyName" path="countyName" />
      </p>
    </cmr:column>
    <cmr:column span="2">
    <p>
      <cmr:label fieldId="poBoxCity">
        <cmr:fieldLabel fieldId="POBoxCity" />:
             <cmr:delta text="-" id="delta-poBoxCity" />
      </cmr:label>
      <cmr:field fieldId="POBoxCity" id="poBoxCity" path="poBoxCity" />
    </p>
   </cmr:column>
</cmr:row>
 <cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="office">
        <cmr:fieldLabel fieldId="Office" />:
             <cmr:delta text="-" id="delta-office" />
      </cmr:label>
      <cmr:field fieldId="Office" id="office" path="office" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="taxOffice">
        <cmr:fieldLabel fieldId="TaxOffice" />:
          <cmr:delta text="-" id="delta-taxOffice" />
      </cmr:label>
      <cmr:field fieldId="TaxOffice" id="taxOffice" path="taxOffice" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="bldg">
        <cmr:fieldLabel fieldId="Building" />:
             <cmr:delta text="-" id="delta-bldg" />
      </cmr:label>
      <cmr:field fieldId="Building" id="bldg" path="bldg" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">

  <cmr:column span="2" containerForField="CustPhone">
    <p>
      <cmr:label fieldId="custPhone">
        <cmr:fieldLabel fieldId="CustPhone" />:
             <cmr:delta text="-" id="delta-custPhone" />
      </cmr:label>
      <cmr:field fieldId="CustPhone" id="custPhone" path="custPhone" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="sapNo" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SAPNumber" />:</cmr:label>
      <cmr:delta text="-" id="delta-sapNo" />
      <cmr:field fieldId="SAPNumber" id="sapNo" path="sapNo" />
    </p>
  </cmr:column>
</cmr:row>