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
      <cmr:field fieldId="AddressTypeInput" id="addrType" path="addrType" breakAfter="8" />
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
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm1" />
    <cmr:field fieldId="CustomerName1" id="custNm1" path="custNm1" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm2">
      <cmr:fieldLabel fieldId="CustomerName2" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm2" />
    <cmr:field fieldId="CustomerName2" id="custNm2" path="custNm2" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm4">
      <cmr:fieldLabel fieldId="CustomerName4" />: 
      <cmr:info text="${ui.info.custNm2JP}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm4" />
    <cmr:field fieldId="CustomerName4" id="custNm4" path="custNm4" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="cnCustName1">
      <cmr:fieldLabel fieldId="CustomerName3" />: 
      <cmr:info text="${ui.info.custNm3JP}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm3" />
    <cmr:field fieldId="CustomerName3" id="cnCustName1" path="cnCustName1" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="cnCustName2">
      <cmr:fieldLabel fieldId="JpCustomerName2" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-cnCustName2" />
    <cmr:field fieldId="JpCustomerName2" id="cnCustName2" path="cnCustName2" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="addrTxt">
      <cmr:fieldLabel fieldId="AddressTxt" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-addrTxt" />
    <cmr:field fieldId="AddressTxt" id="addrTxt" path="addrTxt" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="cnAddrTxt">
    <cmr:fieldLabel fieldId="JpStreetAddress1" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-cnAddrTxt" />
    <cmr:field fieldId="JpStreetAddress1" id="cnAddrTxt" path="cnAddrTxt" size="400" />
  </cmr:column>
</cmr:row>
<cmr:row addBackground="true">  
  &nbsp;
</cmr:row>
<cmr:view exceptForGEO="JP">
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
</cmr:row>

<cmr:row>
  <cmr:column span="2" containerForField="StateProv">
    <p>
      <cmr:label fieldId="stateProv">
        <cmr:fieldLabel fieldId="StateProv" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProv" id="stateProv" path="stateProv" />
    </p>
    </cmr:column>
  <cmr:column span="2" >
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
 <cmr:row> 
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="cnCity">
        <cmr:fieldLabel fieldId="JpCityEN" />:
      </cmr:label>
      <cmr:field fieldId="JpCityEN" id="cnCity" path="cnCity" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="cnDistrict">
        <cmr:fieldLabel fieldId="JpDistrict" />:
      </cmr:label>
      <cmr:field fieldId="JpDistrict" id="cnDistrict" path="cnDistrict" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="postCd">
        <cmr:fieldLabel fieldId="PostalCode" />:<cmr:info text="${ui.info.postCd}" />
             <cmr:delta text="-" id="delta-postCd" />
        <!--     <cmr:info text="${ui.info.postalCodeFormatFR}" /> -->  
      </cmr:label>
      <cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="Department">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      <cmr:field fieldId="Department" id="dept" path="dept" />
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
  <cmr:column span="2" containerForField="CustPhone">
    <p>
      <cmr:label fieldId="custPhone">
        <cmr:fieldLabel fieldId="CustPhone" />:<cmr:info text="${ui.info.custPhone}" />
             <cmr:delta text="-" id="delta-custPhone" />
      </cmr:label>
      <cmr:field fieldId="CustPhone" id="custPhone" path="custPhone" />
    </p>
  </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="2" containerForField="LocationCode">
    <p>
      <cmr:label fieldId="locationCode">
        <cmr:fieldLabel fieldId="LocationCode" />:
             <cmr:delta text="-" id="delta-locationCode" />
      </cmr:label>
      <cmr:field fieldId="LocationCode" id="locationCode" path="locationCode" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="CustFAX">
    <p>
      <cmr:label fieldId="custFax">
        <cmr:fieldLabel fieldId="CustFAX" />:<cmr:info text="${ui.info.custFax}" />
             <cmr:delta text="-" id="delta-custFax" />
      </cmr:label>
      <cmr:field fieldId="CustFAX" id="custFax" path="custFax" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="EstabFuncCd">
    <p>
      <cmr:label fieldId="estabFuncCd">
        <cmr:fieldLabel fieldId="EstabFuncCd" />:
             <cmr:delta text="-" id="delta-estabFuncCd" />
      </cmr:label>
      <cmr:field fieldId="EstabFuncCd" id="estabFuncCd" path="estabFuncCd" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="Division">
    <p>
      <cmr:label fieldId="divn">
        <cmr:fieldLabel fieldId="Division" />:
             <cmr:delta text="-" id="delta-divn" />
      </cmr:label>
      <cmr:field fieldId="Division" id="divn" path="divn" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="City2">
    <p>
      <cmr:label fieldId="city2">
        <cmr:fieldLabel fieldId="City2" />:
             <cmr:delta text="-" id="delta-city2" />
      </cmr:label>
      <cmr:field fieldId="City2" id="city2" path="city2" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="CompanySize">
    <p>
      <cmr:label fieldId="companySize">
        <cmr:fieldLabel fieldId="CompanySize" />:
             <cmr:delta text="-" id="delta-companySize" />
      </cmr:label>
      <cmr:field fieldId="CompanySize" id="companySize" path="companySize" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="Contact">
    <p>
      <cmr:label fieldId="contact">
        <cmr:fieldLabel fieldId="Contact" />:
             <cmr:delta text="-" id="delta-contact" />
      </cmr:label>
      <cmr:field fieldId="Contact" id="contact" path="contact" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="ROL">
    <p>
      <cmr:label fieldId="rol">
        <cmr:fieldLabel fieldId="ROL" />:<cmr:info text="${ui.info.rol}" />
             <cmr:delta text="-" id="delta-rol" />
      </cmr:label>
      <cmr:field fieldId="ROL" id="rol" path="rol" />
    </p>
  </cmr:column>
 </cmr:row>
 <cmr:row topPad="10" addBackground="true">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="sapNo" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SAPNumber" />:</cmr:label>
      <cmr:delta text="-" id="delta-sapNo" />
      <cmr:field fieldId="SAPNumber" id="sapNo" path="sapNo" />
    </p>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="ierpSitePrtyId">
        <cmr:fieldLabel fieldId="IERPSitePrtyId" />:</cmr:label>
      <cmr:delta text="-" id="delta-ierpSitePrtyId" />
      <cmr:field fieldId="IERPSitePrtyId" id="ierpSitePrtyId" path="ierpSitePrtyId" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:view>
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
</cmr:view>