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
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="sapNo" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SAPNumber" />:</cmr:label>
      <cmr:delta text="-" id="delta-sapNo" />
      <cmr:field fieldId="SAPNumber" id="sapNo" path="sapNo" />
    </p>
  </cmr:column> 
<!--   CREATCMR-531 -->
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="ierpSitePrtyId">
        <cmr:fieldLabel fieldId="IERPSitePrtyId" />:</cmr:label>
      <cmr:field fieldId="IERPSitePrtyId" id="ierpSitePrtyId" path="ierpSitePrtyId" />
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
  <cmr:column span="2" exceptForCountry="631">
    <p>
      <cmr:label fieldId="Division">
        <cmr:fieldLabel fieldId="Division" />:
          </cmr:label>
      <cmr:field fieldId="Division" id="divn" path="divn" />
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
          </cmr:label>
      <cmr:field fieldId="StreetAddress2" id="addrTxt2" path="addrTxt2" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:view forGEO="EMEA">
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
  </cmr:row>
</cmr:view>
<%--DTN:LA COUNTRY SPECIFIC LAYOUT --%>
<%-- 
<cmr:view forCountry="613,629,655,661,663,681,683,731,735,781,799,811,813,815,829,869,871">
  <cmr:row>
    <cmr:column span="2">
      <p>
        <cmr:label fieldId="city2">
          <cmr:fieldLabel fieldId="City2" />:
           <cmr:delta text="-" id="delta-city2" />
        </cmr:label>
        <cmr:field fieldId="City2" id="city2" path="city2" />
      </p>
    </cmr:column>
    <cmr:column span="2">
      <p>
        <cmr:label fieldId="city1">
          <cmr:fieldLabel fieldId="City1" />:
           <cmr:delta text="-" id="delta-city1" />
        </cmr:label>
        <cmr:field fieldId="City1" id="city1" path="city1" />
      </p>
    </cmr:column>
  </cmr:row>
</cmr:view>
--%>
<cmr:view forCountry="613,629,655,661,663,681,683,731,735,781,799,811,813,815,829,869,871">
<cmr:row>
  <cmr:column span="2" exceptForCountry="815,631,781">
      <p>
        <cmr:label fieldId="city2">
          <cmr:fieldLabel fieldId="City2" />:
           <cmr:delta text="-" id="delta-city2" />
        </cmr:label>
        <cmr:field fieldId="City2" id="city2" path="city2" />
      </p>
  </cmr:column>
  
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="DropDownCity" />:
            <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:field fieldId="DropDownCity" id="city1" path="city1" />
    </p>
  </cmr:column>
  <%-- 
  <cmr:column span="2">
      <p>
        <cmr:label fieldId="city1">
          <cmr:fieldLabel fieldId="City1" />:
           <cmr:delta text="-" id="delta-city1" />
        </cmr:label>
        <cmr:field fieldId="City1" id="city1" path="city1" />
      </p>
    </cmr:column>
  	--%>
</cmr:row>
</cmr:view>
<cmr:view forCountry="631">
<cmr:row>
  <%-- <cmr:column span="2">
      <p>
        <cmr:label fieldId="city2">
          <cmr:fieldLabel fieldId="City2" />:
           <cmr:delta text="-" id="delta-city2" />
        </cmr:label>
        <cmr:field fieldId="City2" id="city2" path="city2" />
      </p>
  </cmr:column> --%>
<%-- 
  <cmr:column span="2">
      <p>
        <cmr:label fieldId="city1">
          <cmr:fieldLabel fieldId="City1" />:
           <cmr:delta text="-" id="delta-city1" />
        </cmr:label>
        <cmr:field fieldId="City1" id="city1" path="city1" />
      </p>
    </cmr:column>
--%>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="DropDownCity" />:
            <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:field fieldId="DropDownCity" id="city1" path="city1" />
    </p>
  </cmr:column>
</cmr:row>
</cmr:view>

<%--DTN:DISTRICT FIELD ROW--%>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="stateProv">
        <cmr:fieldLabel fieldId="StateProv" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProv" id="stateProv" path="stateProv" />
    </p>
  </cmr:column>

  <!-- // 1164561 -->
  <cmr:view forCountry="631">
    <cmr:column span="2" containerForField="LocalTax1">
      <p>
        <label for="taxCd1"> <cmr:fieldLabel fieldId="LocalTax1" />: </label>
        <cmr:field fieldId="LocalTax1" id="taxCd1" path="taxCd1" />
      </p>
    </cmr:column>
  </cmr:view>

  <cmr:column span="2" containerForField="Floor">
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
  <!-- // 1164558 -->
  <cmr:view forCountry="631">
    <cmr:column span="2" containerForField="LocalTax2">
      <p>
        <label for="taxCd2"> <cmr:fieldLabel fieldId="LocalTax2" /> : </label>
        <cmr:field fieldId="LocalTax2" path="taxCd2" id="taxCd2" />
      </p>
    </cmr:column>
  </cmr:view>

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

  <cmr:view forCountry="631">
    <cmr:column span="2" containerForField="VAT">
      <p>
        <label for="vat"> <cmr:fieldLabel fieldId="VAT" />: </label>
        <cmr:field path="vat" id="vat" fieldId="VAT" />
      </p>
    </cmr:column>
  </cmr:view>

</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="2" containerForField="POBox" exceptForCountry="897">
    <p>
      <cmr:label fieldId="poBox">
        <cmr:fieldLabel fieldId="POBox" />:
             <cmr:delta text="-" id="delta-poBox" />
      </cmr:label>
      <cmr:field fieldId="POBox" id="poBox" path="poBox" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="CustPhone">
    <p>
      <cmr:label fieldId="custPhone">
        <cmr:fieldLabel fieldId="CustPhone" />:
             <cmr:delta text="-" id="delta-custPhone" />
      </cmr:label>
      <cmr:field fieldId="CustPhone" id="custPhone" path="custPhone" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="2" containerForField="POBoxCity" exceptForCountry="897">
    <p>
      <cmr:label fieldId="poBoxCity">
        <cmr:fieldLabel fieldId="POBoxCity" />:
             <cmr:delta text="-" id="delta-poBoxCity" />
      </cmr:label>
      <cmr:field fieldId="POBoxCity" id="poBoxCity" path="poBoxCity" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="CustFAX">
    <p>
      <cmr:label fieldId="custFax">
        <cmr:fieldLabel fieldId="CustFAX" />:
             <cmr:delta text="-" id="delta-custFax" />
      </cmr:label>
      <cmr:field fieldId="CustFAX" id="custFax" path="custFax" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="2" containerForField="POBoxPostalCode" exceptForCountry="897">
    <p>
      <cmr:label fieldId="poBoxPostCd">
        <cmr:fieldLabel fieldId="POBoxPostalCode" />:
             <cmr:delta text="-" id="delta-poBoxPostCd" />
      </cmr:label>
      <cmr:field fieldId="POBoxPostalCode" id="poBoxPostCd" path="poBoxPostCd" />
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
