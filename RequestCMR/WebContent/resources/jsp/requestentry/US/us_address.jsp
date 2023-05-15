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
    <p id = "addrTxt2Div" style= "display : none">
      <cmr:label fieldId="addrTxt2">
        <cmr:fieldLabel fieldId="StreetAddress2" />: 
          </cmr:label>
      <cmr:field fieldId="StreetAddress2" id="addrTxt2" path="addrTxt2" />
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
  <cmr:column span="2" containerForField="Department">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      <cmr:field fieldId="Department" id="dept" path="dept" />
    </p>
  </cmr:column>
</cmr:row>
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
  <cmr:column span="2" containerForField="Building">
    <p>
      <cmr:label fieldId="bldg">
        <cmr:fieldLabel fieldId="Building" />:
             <cmr:delta text="-" id="delta-bldg" />
      </cmr:label>
      <cmr:field fieldId="Building" id="bldg" path="bldg" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="stateProv">
      <!--CREATCMR-8323 change StateProv to StateProvUS-->
        <cmr:fieldLabel fieldId="StateProvUS" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <!--CREATCMR-8323 change StateProv to StateProvUS-->
      <cmr:field fieldId="StateProvUS" id="stateProv" path="stateProv" />
    </p>
  </cmr:column>


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
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="county">
        <cmr:fieldLabel fieldId="County" />:
             <cmr:delta text="-" id="delta-county" code="L" />
      </cmr:label>
      <cmr:field fieldId="County" id="county" path="county" />
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
</cmr:row>

<cmr:row addBackground="true">
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
