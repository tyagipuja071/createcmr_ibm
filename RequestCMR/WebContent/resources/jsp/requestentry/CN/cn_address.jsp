<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
%>
<cmr:row topPad="10">
  <cmr:column span="2">

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
  </cmr:row>
  <cmr:row>
    <cmr:column span="2">
    <p>
      <cmr:label fieldId="ierpSitePrtyId" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="IERPSitePrtyId" />:</cmr:label>
      <cmr:delta text="-" id="delta-ierpSitePrtyId" />
      <cmr:field fieldId="IERPSitePrtyId" id="ierpSitePrtyId" path="ierpSitePrtyId" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="sapNo" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SAPNumber" />:</cmr:label>
      <cmr:delta text="-" id="delta-sapNo" />
      <cmr:field fieldId="SAPNumber" id="sapNo" path="sapNo" size="100" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm1">
      <cmr:fieldLabel fieldId="CustomerName1" />: 
      <cmr:info text="${ui.info.privateCustomerName}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm1" />
    <cmr:field fieldId="CustomerName1" id="custNm1" path="custNm1" size="400" />
  </cmr:column>
  <cmr:column span="4">
    <cmr:label fieldId="cnCustName1">
      <cmr:fieldLabel fieldId="ChinaCustomerName1" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-cnCustName1" />
    <cmr:field fieldId="ChinaCustomerName1" id="cnCustName1" path="cnCustName1" size="400" />
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
  <cmr:column span="4">
    <cmr:label fieldId="cnCustName2">
      <cmr:fieldLabel fieldId="ChinaCustomerName2" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-cnCustName2" />
    <cmr:field fieldId="ChinaCustomerName2" id="cnCustName2" path="cnCustName2" size="400" />
  </cmr:column>  
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm3">
      <cmr:fieldLabel fieldId="CustomerName3" />: 
      <cmr:info text="${ui.info.privateCustomerName}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm3" />
    <cmr:field fieldId="CustomerName3" id="custNm3" path="custNm3" size="400" />
  </cmr:column>
  <cmr:column span="4">
    <cmr:label fieldId="cnCustName3">
      <cmr:fieldLabel fieldId="ChinaCustomerName3" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-cnCustName3" />
    <cmr:field fieldId="ChinaCustomerName3" id="cnCustName3" path="cnCustName3" size="400" />
  </cmr:column>  
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="translate">&nbsp;
      </cmr:label>
      Translate: 
      <a class="translate" href="javascript: cmr.openTranslateWindow('en','zh-CN', ['custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city2'])" >to Local Language</a>
      <a class="translate" href="javascript: cmr.openTranslateWindow('zh-CN','en', ['cnCustName1', 'cnCustName2', 'cnAddrTxt', 'cnAddrTxt2', 'cnDistrict'])" >to English</a>
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
      <cmr:label fieldId="cnAddrTxt">
        <cmr:fieldLabel fieldId="ChinaStreetAddress1" />: 
             <cmr:delta text="-" id="delta-cnAddrTxt" />
      </cmr:label>
      <cmr:field fieldId="ChinaStreetAddress1" id="cnAddrTxt" path="cnAddrTxt" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrTxt2">
        <cmr:fieldLabel fieldId="StreetAddress2" />: 
          </cmr:label>
      <cmr:field fieldId="StreetAddress2" id="addrTxt2" path="addrTxt2" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="cnAddrTxt2">
        <cmr:fieldLabel fieldId="ChinaStreetAddress2" />: 
          </cmr:label>
      <cmr:field fieldId="ChinaStreetAddress2" id="cnAddrTxt2" path="cnAddrTxt2" />
    </p>
  </cmr:column>	
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
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="stateProv">
        <cmr:fieldLabel fieldId="StateProvChina" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProvChina" id="stateProv" path="stateProv" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row>
  <cmr:column span="2" containerForField="DropDownCity">
    <p>
      <cmr:label fieldId="dropdowncity1">
        <cmr:fieldLabel fieldId="City1" />:
            <cmr:delta text="-" id="delta-city1" code="L"/>
      </cmr:label>
      <cmr:field fieldId="DropDownCity" id="dropdowncity1" path="city1DrpDown" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="City1">
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
      <cmr:label fieldId="cnCity">
        <cmr:fieldLabel fieldId="DropDownCityChina" />:
      </cmr:label>
      <cmr:field fieldId="DropDownCityChina" id="cnCity" path="cnCity" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="city2">
        <cmr:fieldLabel fieldId="City2" />:
      </cmr:label>
      <cmr:field fieldId="City2" id="city2" path="city2" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="cnDistrict">
        <cmr:fieldLabel fieldId="ChinaCity2" />:
      </cmr:label>
      <cmr:field fieldId="ChinaCity2" id="cnDistrict" path="cnDistrict" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:row>
  <cmr:column span="2" containerForField="Department">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      <cmr:field fieldId="Department" id="dept" path="dept" />
    </p>
  </cmr:column>
  <%--<cmr:column span="2" containerForField="Floor">
    <p>
      <cmr:label fieldId="floor">
        <cmr:fieldLabel fieldId="Floor" />:
             <cmr:delta text="-" id="delta-floor" />
      </cmr:label>
      <cmr:field fieldId="Floor" id="floor" path="floor" />
    </p>
  </cmr:column>--%>
  </cmr:row>
<cmr:row>
 <cmr:column span="2" containerForField="Building">
    <p>
      <cmr:label fieldId="bldg">
        <cmr:fieldLabel fieldId="Building" />:
             <cmr:delta text="-" id="delta-bldg" />
      </cmr:label>
      <cmr:field fieldId="Building" id="bldg" path="bldg" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="Office">
    <p>
      <cmr:label fieldId="office">
        <cmr:fieldLabel fieldId="Office" />:
             <cmr:delta text="-" id="delta-office" />
      </cmr:label>
      <cmr:field fieldId="Office" id="office" path="office" />
    </p>
  </cmr:column>
</cmr:row>
<%--<cmr:row>
	<cmr:column span="2">
    <p>
      <cmr:label fieldId="county">
        <cmr:fieldLabel fieldId="County" />:
             <cmr:delta text="-" id="delta-county" code="L" />
      </cmr:label>
      <cmr:field fieldId="County" id="county" path="county" />
    </p>
  </cmr:column>
  </cmr:row>--%>
<cmr:row>
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
      <cmr:label fieldId="postCd">
        <cmr:fieldLabel fieldId="PostalCode" />:
             <cmr:delta text="-" id="delta-postCd" />
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
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="transportZone">
        <cmr:fieldLabel fieldId="TransportZone" />:
             <cmr:delta text="-" id="delta-transportZone" code="L" />
        <cmr:info text="${ui.info.addrTransportZone}" />
      </cmr:label>
      <cmr:field fieldId="TransportZone" id="transportZone" path="transportZone" />
    </p>
  </cmr:column>
</cmr:row>


<cmr:row>
 <%-- 
 <cmr:column span="2" containerForField="CustomerCntPhone2">
    <p>
      <cmr:label fieldId="cnCustContPhone2">
        <cmr:fieldLabel fieldId="CustomerCntPhone2" />:
             <cmr:delta text="-" id="delta-cnCustContPhone2" />
      </cmr:label>
      <cmr:field fieldId="CustomerCntPhone2" id="cnCustContPhone2" path="cnCustContPhone2" />
    </p>
  </cmr:column>
  --%>
  <cmr:column span="2" containerForField="CustomerCntJobTitle">
    <p>
      <cmr:label fieldId="cnCustContJobTitle">
        <cmr:fieldLabel fieldId="CustomerCntJobTitle" />:
             <cmr:delta text="-" id="delta-cnCustContJobTitle" />
      </cmr:label>
      <cmr:field fieldId="CustomerCntJobTitle" id="cnCustContJobTitle" path="cnCustContJobTitle" />
    </p>
  </cmr:column>
</cmr:row>


<cmr:row>
 <cmr:column span="4" containerForField="ChinaCustomerCntName">
    <p>
      <cmr:label fieldId="cnCustContNm">
        <cmr:fieldLabel fieldId="ChinaCustomerCntName" />:
             <cmr:delta text="-" id="delta-cnCustContNm" />
      </cmr:label>
      <cmr:field fieldId="ChinaCustomerCntName" id="cnCustContNm" path="cnCustContNm" size="300" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
<cmr:column span="2" containerForField="InterAddrKey">
    <p>
      <cmr:label fieldId="cnInterAddrKey">
        <cmr:fieldLabel fieldId="InterAddrKey" />:
      </cmr:label>
      <cmr:field fieldId="InterAddrKey" id="cnInterAddrKey" path="cnInterAddrKey" />
    </p>
  </cmr:column>
</cmr:row>

<%-- <cmr:row>
  
  <cmr:column span="2" containerForField="POBoxPostalCode" exceptForCountry="897">
    <p>
      <cmr:label fieldId="poBoxPostCd">
        <cmr:fieldLabel fieldId="POBoxPostalCode" />:
             <cmr:delta text="-" id="delta-poBoxPostCd" />
      </cmr:label>
      <cmr:field fieldId="POBoxPostalCode" id="poBoxPostCd" path="poBoxPostCd" />
    </p>
  </cmr:column>
</cmr:row> --%>
 
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
