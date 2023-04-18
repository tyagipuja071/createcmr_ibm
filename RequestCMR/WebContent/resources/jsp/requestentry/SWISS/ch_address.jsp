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
	  <div id="hwFlag">
	<cmr:column span="1" containerForField="HwInstlMasterFlag">
          <p>
            <cmr:label fieldId="hwInstlMstrFlg2">&nbsp;</cmr:label>
            <cmr:field fieldId="HwInstlMasterFlag" id="hwInstlMstrFlg" path="hwInstlMstrFlg"/>
            <cmr:label fieldId="hwInstlMstrFlg" forRadioOrCheckbox="true">
              <cmr:fieldLabel fieldId="HwInstlMasterFlag" />
              <cmr:delta text="${rdcdata.hwInstlMstrFlg}" oldValue="${reqentry.hwInstlMstrFlg == 'Y' ? 'Yes' : 'No'}" />
            </cmr:label>
          </p>
        </cmr:column>
        	  </div>
	<br>
	<br>
	<br>
</cmr:row>

<cmr:row addBackground="true">
	<cmr:column span="2">
		<cmr:label fieldId="custNm1">
			<cmr:fieldLabel fieldId="CustomerName1" />: 
    </cmr:label>
		<cmr:delta text="-" id="delta-custNm1" />
		<cmr:field fieldId="CustomerName1" id="custNm1" path="custNm1"
			size="250" />
	</cmr:column>
	<cmr:column span="2">
		<cmr:label fieldId="custNm2">
			<cmr:fieldLabel fieldId="CustomerName2" />: 
    </cmr:label>
		<cmr:delta text="-" id="delta-custNm2" />
		<cmr:field fieldId="CustomerName2" id="custNm2" path="custNm2"
			size="250" />
	</cmr:column>
</cmr:row>

<cmr:row addBackground="true">
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
				<cmr:fieldLabel fieldId="StateProv" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
				<cmr:info text="${ui.info.addrStateProv}" />
			</cmr:label>
			<cmr:field fieldId="StateProv" id="stateProv" path="stateProv" />
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
			<cmr:label fieldId="city1">
				<cmr:fieldLabel fieldId="City1" />:
             <cmr:delta text="-" id="delta-city1" />
			</cmr:label>
			<cmr:field fieldId="City1" id="city1" path="city1" />
		</p>
	</cmr:column>
</cmr:row>

<!-- Story : 1830918 -->
<cmr:row>
 <cmr:column span="2">
    <p>
	    <cmr:label fieldId="divn">
	      <cmr:fieldLabel fieldId="Division" />: 
	    </cmr:label>
	    <cmr:delta text="-" id="delta-divn" />
	    <cmr:field fieldId="Division" id="divn" path="divn"/>
    </p>
  </cmr:column>

  <cmr:column span="2">
    <p>
	    <cmr:label fieldId="city2">
	      <cmr:fieldLabel fieldId="City2" />: 
	    </cmr:label>
	    <cmr:delta text="-" id="delta-city2" />
	    <cmr:field fieldId="City2" id="city2" path="city2"  />
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

<cmr:row addBackground="true">
	<cmr:column span="2">
		<p>
			<cmr:label fieldId="postCd">
				<cmr:fieldLabel fieldId="PostalCode" />:
             <cmr:delta text="-" id="delta-postCd" />
			</cmr:label>
			<cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
		</p>
	</cmr:column>
	
	<!-- Preferred Language for Swiss -->
	<cmr:column span="2">
		<p>
			<cmr:label fieldId="custLangCd">
				<cmr:fieldLabel fieldId="CustLangCd" />:
			 <cmr:delta text="-" id="delta-custLangCd" code="L" />
			</cmr:label>
			<cmr:field path="custLangCd" id="custLangCd" fieldId="CustLangCd"/>
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
	<!--     <cmr:column span="2">
    <p>
      <cmr:label fieldId="transportZone">
        <cmr:fieldLabel fieldId="TransportZone" />:
             <cmr:delta text="-" id="delta-transportZone" code="L" />
        <cmr:info text="${ui.info.addrTransportZone}" />
      </cmr:label>
      <cmr:field fieldId="TransportZone" id="transportZone" path="transportZone" />
    </p>
  </cmr:column> -->

</cmr:row>

<cmr:row addBackground="true">
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

<cmr:row >
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

