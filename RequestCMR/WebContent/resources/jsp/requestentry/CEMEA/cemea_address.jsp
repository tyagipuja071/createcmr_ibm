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
      <cmr:field fieldId="AddressTypeInput" id="addrType" path="addrType" breakAfter="6" />
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
      <cmr:view exceptForCountry="618">
        <cmr:info text="${ui.info.custNm1CEMEA}" />
      </cmr:view>
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm1" />
    <cmr:field fieldId="CustomerName1" id="custNm1" path="custNm1" size="400" />
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4" containerForField="CustomerName2">
    <cmr:label fieldId="custNm2">
      <cmr:fieldLabel fieldId="CustomerName2" />: 
      <cmr:view exceptForCountry="618,740">
        <cmr:info text="${ui.info.custNm2CEMEA}" />
      </cmr:view>
      <cmr:view forCountry="740">
      	<a id = 'custNm2HUInfoBubble'>
        	<cmr:info text="${ui.info.custNm2HU}" />
        </a>
      </cmr:view>
      <cmr:view forCountry="618">
        <cmr:info text="${ui.info.nameAustria}" />
      </cmr:view>
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm2" />
    <cmr:field fieldId="CustomerName2" id="custNm2" path="custNm2" size="400" />
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm3">
      <cmr:fieldLabel fieldId="CustomerName3" />: 
      <cmr:view exceptForCountry="618,740">
        <cmr:info text="${ui.info.custNm3CEMEA}" />
      </cmr:view>
      <!--<cmr:view forCountry="740">
        <cmr:info text="${ui.info.custNm3HU}" />
      </cmr:view>-->
      <cmr:view forCountry="618">
        <cmr:info text="${ui.info.nameAustria}" />
      </cmr:view>
    </cmr:label>
    <cmr:delta text="-" id="delta-custNm3" />
    <cmr:field fieldId="CustomerName3" id="custNm3" path="custNm3" size="400" />
  </cmr:column>
</cmr:row>

<cmr:row addBackground="true">
	<cmr:column span="4" forCountry="618">
		<cmr:column span="2" width="370" containerForField="CustomerName4">
			<cmr:label fieldId="custNm4">
				<cmr:fieldLabel fieldId="CustomerName4" />: 
        <cmr:delta text="-" id="delta-custNm4" />
			</cmr:label>
			<cmr:field fieldId="CustomerName4" id="custNm4" path="custNm4" size="400" />
		</cmr:column>
	</cmr:column>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="4" forCountry="758,760,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,358,359,363,620,642,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,675">
    <cmr:label fieldId="custNm4">
      <cmr:fieldLabel fieldId="CustomerName4" />: 
      <cmr:view>
        <cmr:info text="${ui.attnPerson}" />
      </cmr:view>
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
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="City1" />:
             <cmr:delta text="-" id="delta-city1" />
             <cmr:view forCountry="826">
             	<a id = 'cityRomaniaInfoBubble'>
        			<cmr:info text="${ui.info.cityCEMEA}" />
        	 	</a>
             </cmr:view>
      </cmr:label>
      <cmr:field fieldId="City1" id="city1" path="city1" />
    </p>
  </cmr:column> 
</cmr:row>
<cmr:row>
	<cmr:view forCountry="618">
		<cmr:column span="2" containerForField="Building">
			<p>
				<cmr:label fieldId="bldg">
					<cmr:fieldLabel fieldId="Building" />:
             <cmr:delta text="-" id="delta-bldg" />
				</cmr:label>
				<cmr:field fieldId="Building" id="bldg" path="bldg" />
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
	</cmr:view>
</cmr:row>

<cmr:row>
  <cmr:column span="2" containerForField="StreetAddress1">
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
      <cmr:label fieldId="postCd">
        <cmr:fieldLabel fieldId="PostalCode" />:
             <cmr:delta text="-" id="delta-postCd" />
      </cmr:label>
      <cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
    </p>
  </cmr:column>
</cmr:row>
<cmr:view exceptForCountry="618">
  <cmr:row>
    <cmr:column span="2" containerForField="LocalLangCountryName">
      <p>
        <cmr:label fieldId="bldg">
          <cmr:fieldLabel fieldId="LocalLangCountryName" />: 
               <cmr:delta text="-" id="delta-bldg" />
        </cmr:label>
        <cmr:field fieldId="LocalLangCountryName" id="bldg" path="bldg" />
      </p>
    </cmr:column>
    <!-- Remove ICE to customer tab -->
      <!-- We are using for ICE Field -->
  <!-- <div id="ice">
  <cmr:column span="2" containerForField="Department" forCountry="642">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      <cmr:field fieldId="Department" id="dept" path="dept"/>
    </p>
  </cmr:column>
  </div> -->
       
  </cmr:row>
</cmr:view>
<cmr:row>
  <cmr:column span="2" containerForField="CustPhone" forCountry="618">
    <p>
      <cmr:label fieldId="custPhone">
        <cmr:fieldLabel fieldId="CustPhone" />:
        <cmr:delta text="-" id="delta-custPhone" />
      </cmr:label>
      <cmr:field fieldId="CustPhone" id="custPhone" path="custPhone" />
    </p>
  </cmr:column>
  
  <cmr:column span="2" containerForField="POBox" forCountry="618,620,642,677,680,752,762,767,768,772,805,808,823,832,849,850,865">
    <p>
      <cmr:label fieldId="poBox">
        <cmr:fieldLabel fieldId="POBox" />:
                 <cmr:delta text="-" id="delta-poBox" />
      </cmr:label>
      <cmr:field fieldId="POBox" id="poBox" path="poBox" />
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
  <cmr:view exceptForCountry="618">
  	<cmr:column span="2">
     <p>
      <cmr:label fieldId="ierpSitePrtyId" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SitePartyID" />:</cmr:label>
      <cmr:delta text="-" id="delta-ierpSitePrtyId" />
      <cmr:field fieldId="SitePartyID" id="ierpSitePrtyId" path="ierpSitePrtyId" />
     </p>
  	</cmr:column>
  </cmr:view>
</cmr:row>

<cmr:row addBackground="true">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrCreateDt">
        <cmr:fieldLabel fieldId="RDcCreateDate" />:
          </cmr:label>
    <div id="addrCreateDt_updt">-</div>
  </cmr:column>
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="addrUpdateDt">
        <cmr:fieldLabel fieldId="RDCLastUpdateDate" />:
          </cmr:label>
    <div id="addrUpdateDt_updt">-</div>
  </cmr:column>
</cmr:row>
