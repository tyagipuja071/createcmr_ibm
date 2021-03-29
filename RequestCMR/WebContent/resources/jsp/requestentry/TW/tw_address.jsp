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
  <cmr:column span="4">
    <cmr:label fieldId="custNm3" >
      <cmr:fieldLabel fieldId="ChinaCustomerName1" />: 
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm3" />
    <cmr:field fieldId="ChinaCustomerName1" id="custNm3" path="custNm3" size="400" />
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">  
  &nbsp;
</cmr:row>
<cmr:row>
  <cmr:column span="2">
    <p>
      <a href="https://www.post.gov.tw/post/internet/Postal/index.jsp?ID=207"  target='_blank'>Address Translation</a>
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
      <cmr:label fieldId="bldg">
        <cmr:fieldLabel fieldId="ChinaStreetAddress1" />: 
             <cmr:delta text="-" id="delta-bldg" />
      </cmr:label>
      <cmr:field fieldId="ChinaStreetAddress1" id="bldg" path="bldg" />
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
 
 <cmr:row addBackground="true">
  <cmr:column span="2">
  <p>
    <cmr:label fieldId="SAPNumber" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SAPNumber" />:</cmr:label>
      <cmr:delta text="-" id="delta-sapNo" />
      <cmr:field fieldId="SAPNumber" id="sapNo" path="sapNo" size="100" />
  </p>
  </cmr:column>
</cmr:row>


