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
  <cmr:column span="4" exceptForCountry="616,796">`
    <cmr:label fieldId="custNm1">
      <cmr:fieldLabel fieldId="CustomerName1" />: 
      <cmr:info text="${ui.info.custNm1AP}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm1" />
    <cmr:field fieldId="CustomerName1" id="custNm1" path="custNm1" size="400"  />
  </cmr:column>
  <cmr:column span="4" forCountry="616,796">
    <cmr:label fieldId="custNm1">
      <cmr:fieldLabel fieldId="CustomerName1" />: 
      <cmr:info text="${ui.info.custNm1AP}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm1" />
    <cmr:column span="1" width="400">
      <form:input path="custNm1" maxlength="35" dojoType="dijit.form.TextBox" cssStyle="width:400px;" />
	</cmr:column>
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4" exceptForCountry="616,796">
    <cmr:label fieldId="custNm2">
      <cmr:fieldLabel fieldId="CustomerName2" />: 
      <cmr:info text="${ui.info.custNm1AP}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm2" />
    <cmr:field fieldId="CustomerName2" id="custNm2" path="custNm2" size="400" />
  </cmr:column>
  <cmr:column span="4" forCountry="616,796">
    <cmr:label fieldId="custNm2">
      <cmr:fieldLabel fieldId="CustomerName2" />: 
      <cmr:info text="${ui.info.custNm1AP}" />
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm2" />
    <cmr:column span="1" width="400">
      <form:input path="custNm2" maxlength="35" dojoType="dijit.form.TextBox" cssStyle="width:400px;" />
    </cmr:column>
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
  <cmr:column span="2" forCountry="856,714,720,749,778,643,646,818,834,852,652,744,790,615">
    <p>
      <cmr:label fieldId="stateProv">
        <cmr:fieldLabel fieldId="StateProv" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProv" id="stateProv" path="stateProv" />
    </p>
  </cmr:column>
  <cmr:column span="2" containerForField="Department" forCountry="616,796">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      
      <cmr:column span="1" width="180">
        <form:input path="dept" maxlength="35" dojoType="dijit.form.TextBox" cssStyle="width:180px;" />
      </cmr:column>
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2" exceptForCountry="616,796">
    <p>
      <cmr:label fieldId="addrTxt">
        <cmr:fieldLabel fieldId="StreetAddress1" />: 
             <cmr:delta text="-" id="delta-addrTxt" />
      </cmr:label>
      <cmr:field fieldId="StreetAddress1" id="addrTxt" path="addrTxt" />
    </p>
  </cmr:column>
  <cmr:column span="2" forCountry="616,796">
    <p>
      <cmr:label fieldId="addrTxt">
        <cmr:fieldLabel fieldId="StreetAddress1" />: 
             <cmr:delta text="-" id="delta-addrTxt" />
        <cmr:info text="${ui.info.addrStAddr}" />
      </cmr:label>
      <cmr:column span="1" width="180">
        <form:input path="addrTxt" maxlength="35" dojoType="dijit.form.TextBox" cssStyle="width:180px;" />
      </cmr:column>   
    </p>
  </cmr:column>
  <cmr:column span="2" exceptForCountry="616,796">
    <p>
      <cmr:label fieldId="addrTxt2">
        <cmr:fieldLabel fieldId="StreetAddress2" />: 
          </cmr:label>
      <cmr:field fieldId="StreetAddress2" id="addrTxt2" path="addrTxt2" />
    </p>
  </cmr:column>
    <cmr:column span="2" forCountry="616,796">
    <p>
      <cmr:label fieldId="addrTxt2">
        <cmr:fieldLabel fieldId="StreetAddress2" />: 
      </cmr:label>
      <cmr:column span="1" width="180">
        <form:input path="addrTxt2" maxlength="35" dojoType="dijit.form.TextBox" cssStyle="width:180px;" />
      </cmr:column>   
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2" forCountry="736,738">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="City1" />:
             <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:field fieldId="City1" id="city1" path="city1" />
    </p>
  </cmr:column>
    <cmr:column span="2" forCountry="616,796">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="City1" />:
             <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:column span="1" width="180">
        <form:input path="city1" maxlength="35" dojoType="dijit.form.TextBox" cssStyle="width:180px;" />
      </cmr:column>   
      
    </p>
  </cmr:column>
  
  <cmr:column span="2" containerForField="Department" forCountry="856,714,720,749,778,643,646,818,834,852,652,744,790,615">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      <cmr:field fieldId="Department" id="dept" path="dept" />
    </p>
  </cmr:column>
    <cmr:column span="2" forCountry="616,796">
    <p>
      <cmr:label fieldId="stateProv">
        <cmr:fieldLabel fieldId="StateProv" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProv" id="stateProv" path="stateProv" />
    </p>
  </cmr:column>
<cmr:column span="2" forCountry="856,714,720,749,778,643,646,818,834,852,652,744,790,615,736,738">
    <p>
      <cmr:label fieldId="postCd">
        <cmr:fieldLabel fieldId="PostalCode" />:
             <cmr:delta text="-" id="delta-postCd" />
             <% if(!reqentry.getCmrIssuingCntry().equals("755")) {%>
             <cmr:info text="${ui.info.postalCodeFormat}" />
             <% } %>
      </cmr:label>
      <cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2" forCountry="616,796">
    <p>
      <cmr:label fieldId="postCd">
        <cmr:fieldLabel fieldId="PostalCode" />:
             <cmr:delta text="-" id="delta-postCd" />
             <%   if(reqentry.getCmrIssuingCntry().equals("616")) {%>
             	<cmr:info text="${ui.info.postalCodeFormatAU}" />
             <%} else if(!reqentry.getCmrIssuingCntry().equals("755")) {%>
             <cmr:info text="${ui.info.postalCodeFormat}" />
             <% } %>
      </cmr:label>
      <cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
    </p>
  </cmr:column>
    <cmr:column span="2" forCountry="856,714,720,749,778,643,646,818,834,852,652,744,790,615">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="City1" />:
             <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:field fieldId="City1" id="city1" path="city1" />
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
