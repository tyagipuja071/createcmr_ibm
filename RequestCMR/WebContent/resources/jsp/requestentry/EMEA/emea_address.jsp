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
    <div id="hwFlag">
	<cmr:column span="1" containerForField="HwInstlMasterFlag" forCountry="866,754,838">
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
<cmr:view forCountry="726">
  <cmr:row>
      <cmr:column span="2">
      <p>
	<cmr:label fieldId="ierpSitePrtyId" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SitePartyID" />:</cmr:label>
      <cmr:delta text="-" id="delta-ierpSitePrtyId" />
      <cmr:field fieldId="IERPSitePrtyId" id="ierpSitePrtyId" path="ierpSitePrtyId" />
      </p>
    </cmr:column>
  </cmr:row>
</cmr:view>
<cmr:view forCountry="755">
  <cmr:row>
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
	  <cmr:label fieldId="ierpSitePrtyId" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SitePartyID" />:</cmr:label>
      <cmr:field fieldId="IERPSitePrtyId" id="ierpSitePrtyId" path="ierpSitePrtyId" />
      </p>
    </cmr:column>
  </cmr:row>
</cmr:view>
<cmr:row addBackground="true">
  <cmr:column span="4">
    <cmr:label fieldId="custNm1">
      <cmr:fieldLabel fieldId="CustomerName1" />: 
      <cmr:info text="${ui.info.privateCustomerName}" />
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
  <cmr:column span="2" forCountry="726,666">
    <p>
    <cmr:label fieldId="custNm4">
      <cmr:fieldLabel fieldId="CustomerName4" />: 
      <cmr:delta text="-" id="delta-custNm4" />
    </cmr:label>
    <cmr:field fieldId="CustomerName4" id="custNm4" path="custNm4"/>
    </p>
  </cmr:column>
  <%if ("U".equals(reqentry.getReqType())){%> 
  <cmr:column span="2" forCountry="862">
    <p>
    <cmr:label fieldId="custNm4">
      <cmr:fieldLabel fieldId="CustomerName4" />: 
      <cmr:delta text="-" id="delta-custNm4" />
    </cmr:label>
    <cmr:field fieldId="CustomerName4" id="custNm4" path="custNm4"/>
    </p>
  </cmr:column>
  <%}%> 
<!--  rollback TR address change
    <cmr:column span="2" forCountry="862">
    <p>
      <cmr:label fieldId="translate">&nbsp;
      </cmr:label>
      Translate: 
      <a class="translate" href="javascript: cmr.openTranslateWindow('en','el', ['custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'poBox', 'city1', 'taxOffice'])" >to Local Language</a>
      <a class="translate" href="javascript: cmr.openTranslateWindow('el','en', ['custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'poBox', 'city1', 'taxOffice'])" >to English</a>
    </p>
  </cmr:column>
  -->
</cmr:row>

<cmr:row>
  <cmr:column span="2" >
    <p>
      <cmr:label fieldId="addrTxt">
        <cmr:fieldLabel fieldId="StreetAddress1" />: 
             <cmr:delta text="-" id="delta-addrTxt" />
      </cmr:label>
      <cmr:field fieldId="StreetAddress1" id="addrTxt" path="addrTxt" />
    </p>
  </cmr:column>
  <%-- <cmr:view forCountry="758">
    <cmr:column span="2" containerForField="StreetAddress1">
    <p>
      <cmr:label fieldId="streetAddress1">
        <cmr:fieldLabel fieldId="StreetAddress1" />: 
             <cmr:delta text="-" id="delta-addrTxt" />
      </cmr:label>
      <cmr:field fieldId="StreetAddress1" id="streetAddress1" path="addrTxt" />
    </p>
  </cmr:column>
  </cmr:view> --%>
  <cmr:column span="2" exceptForCountry="758">
    <p>
      <cmr:label fieldId="addrTxt2">
        <cmr:fieldLabel fieldId="StreetAddress2" />: 
          </cmr:label>
      <cmr:field fieldId="StreetAddress2" id="addrTxt2" path="addrTxt2" />
    </p>
  </cmr:column>
</cmr:row>

<cmr:row>
  <cmr:column span="2" exceptForCountry="758">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="City1" />:
             <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:field fieldId="City1" id="city1" path="city1" />
    </p>
  </cmr:column>
  <cmr:view forCountry="758">
<%-- <cmr:column span="2" containerForField="DropDownCity">
    <p>
    DTN:Dropdown City field for when local customer for IT
      <cmr:label fieldId="dropDownCity">
        <cmr:fieldLabel fieldId="DropDownCity" />:
      </cmr:label>
      <cmr:field fieldId="DropDownCity" id="dropDownCity" path="city1" />
    </p>
  </cmr:column> --%> 
  <%--DTN:Textbox City field for when cross border customer for IT--%>
  <cmr:column span="2" containerForField="City1">
    <p>
      <cmr:label fieldId="city1">
        <cmr:fieldLabel fieldId="City1" />:
            <cmr:delta text="-" id="delta-city1" />
      </cmr:label>
      <cmr:field fieldId="City1" id="city1" path="city1" />
    </p>
  </cmr:column>
  </cmr:view>
  <cmr:column span="2" exceptForCountry="758,862,755">
    <p>
      <cmr:label fieldId="stateProv">
        <cmr:fieldLabel fieldId="StateProv" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProv" id="stateProv" path="stateProv" />
    </p>
  </cmr:column>
  <cmr:view forCountry="758">
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
    <cmr:column span="2" containerForField="StateProvItaly">
    <p>
      <cmr:label fieldId="stateProvItaly">
        <cmr:fieldLabel fieldId="StateProvItaly" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProvItaly" id="stateProvItaly" path="locationCode" />
    </p>
    </cmr:column>
    <cmr:column span="2" containerForField="StateProvItalyOth">
    <p>
      <cmr:label fieldId="stateProvItalyOth">
        <cmr:fieldLabel fieldId="StateProvItalyOth" />:
             <cmr:delta text="-" id="delta-stateProv" code="L" />
        <cmr:info text="${ui.info.addrStateProv}" />
      </cmr:label>
      <cmr:field fieldId="StateProvItalyOth" id="stateProvItalyOth" path="city2" />
    </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CrossbStateProvPostalMapIT">
    <p>
      <cmr:label fieldId="crossbStateProvPostalMapIT">
        <cmr:fieldLabel fieldId="CrossbStateProvPostalMapIT" />:
      </cmr:label>
      <cmr:field fieldId="CrossbStateProvPostalMapIT" id="crossbStateProvPostalMapIT" path="crossbStateProvPostalMapIT" />
    </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CrossbCntryStateProvMapIT">
    <p>
      <cmr:label fieldId="crossbCntryStateProvMapIT">
        <cmr:fieldLabel fieldId="CrossbCntryStateProvMapIT" />:
      </cmr:label>
      <cmr:field fieldId="CrossbCntryStateProvMapIT" id="crossbCntryStateProvMapIT" path="crossbCntryStateProvMapIT" />
    </p>
    </cmr:column>
  </cmr:view>
  <cmr:column span="2" containerForField="POBox" forCountry="755">
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
            <!--  <cmr:view exceptForCountry="755,726,666,862,758,754">
                <cmr:info text="${ui.info.postalCodeFormat}" />
             </cmr:view>
             <cmr:view forCountry="754">
                <cmr:info text="${ui.info.postalCodeFormatIE}" />
             </cmr:view>--> 
      </cmr:label>
      <cmr:field fieldId="PostalCode" id="postCd" path="postCd" />
    </p>
  </cmr:column>
  
  <cmr:column span="2" containerForField="Department" exceptForCountry="726,666">
    <p>
      <cmr:label fieldId="dept">
        <cmr:fieldLabel fieldId="Department" />:
             <cmr:delta text="-" id="delta-dept" />
      </cmr:label>
      <cmr:field fieldId="Department" id="dept" path="dept" />
    </p>
  </cmr:column>

  <form:hidden path="poBoxPostCd" forCountry="758"/>
  <form:hidden path="transportZone" forCountry="838,758,866,754"/>
</cmr:row>
<cmr:row>
	<cmr:column span="2" containerForField="TaxOffice" forCountry="862">
    <p>
      <cmr:label fieldId="taxOffice">
        <cmr:fieldLabel fieldId="TaxOffice" />:
      </cmr:label>
      <cmr:field fieldId="TaxOffice" id="taxOffice" path="taxOffice" />
    </p>
  </cmr:column>
</cmr:row>
<%--Story 1377871: For Postal Address  :Mukesh
<cmr:view forCountry="758">
	<cmr:row>
		 <cmr:column span="2" containerForField="BillingPstlAddr">
		    <p>
		      <cmr:label fieldId="billingPstlAddr">
		        <cmr:fieldLabel fieldId="BillingPstlAddr" />:
		           <cmr:delta text="-" id="delta-billingPstlAddr" />
		      </cmr:label>
		      <cmr:field fieldId="BillingPstlAddr" id="billingPstlAddr" path="billingPstlAddr" />
		    </p>
		 </cmr:column>
	</cmr:row>
</cmr:view>--%>
<cmr:view exceptForCountry="758">
<cmr:row>
  <cmr:column span="2" containerForField="POBox" exceptForCountry="755,862">
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
</cmr:view>


<cmr:row topPad="10" addBackground="true">
  <cmr:view exceptForCountry="755">
  <cmr:column span="2">
    <p>
      <cmr:label fieldId="sapNo" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="SAPNumber" />:</cmr:label>
      <cmr:delta text="-" id="delta-sapNo" />
      <cmr:field fieldId="SAPNumber" id="sapNo" path="sapNo" />
    </p>
  </cmr:column>
  </cmr:view>
  <cmr:view forCountry="758,862">
  	<cmr:column span="2">
     <p>
      <cmr:label fieldId="ierpSitePrtyId" cssClass="cmr-inline">
        <cmr:fieldLabel fieldId="IERPSitePrtyId" />:</cmr:label>
      <cmr:delta text="-" id="delta-ierpSitePrtyId" />
      <cmr:field fieldId="IERPSitePrtyId" id="ierpSitePrtyId" path="ierpSitePrtyId" />
     </p>
  	</cmr:column>
  </cmr:view>
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
</cmr:view>
<cmr:view forCountry="758">
<cmr:row>
<cmr:column span="2" containerForField="AddrAbbrevName" >
    <p>
      <cmr:label fieldId="addrAbbrevName">
        <cmr:fieldLabel fieldId="AddrAbbrevName" />:
      </cmr:label>
      <cmr:delta text="-" id="delta-bldg" />
      <cmr:field fieldId="AddrAbbrevName" id="addrAbbrevName" path="bldg" />
    </p>
  </cmr:column>
<cmr:column span="2" containerForField="StreetAbbrev">
    <p>
      <cmr:label fieldId="streetAbbrev">
        <cmr:fieldLabel fieldId="StreetAbbrev" />:
          </cmr:label>
         <cmr:delta text="-" id="delta-divn" />
      <cmr:field fieldId="StreetAbbrev" id="streetAbbrev" path="divn" />
    </p>
  </cmr:column> 
</cmr:row>

<cmr:row>
	<cmr:column span="2" containerForField="AddrAbbrevLocn">
    <p>
      <cmr:label fieldId="addrAbbrevLocn">
        <cmr:fieldLabel fieldId="AddrAbbrevLocn" />:
      </cmr:label>
      <cmr:delta text="-" id="delta-custFax" />
      <cmr:field fieldId="AddrAbbrevLocn" id="addrAbbrevLocn" path="custFax" />
    </p>
  </cmr:column>
  </cmr:row>
</cmr:view>


