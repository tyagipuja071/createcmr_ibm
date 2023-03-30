<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<% RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
   
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
%>
<!--  Modal for the address details screen -->
<cmr:modal title="${ui.title.addressDetails}" id="AddressDetailsModal" widthId="980">

  <input type="hidden" name="view_reqId" id="view_reqId">
  <input type="hidden" name="view_addrType" id="view_addrType">
  <input type="hidden" name="view_addrSeq" id="view_addrSeq">

  <cmr:row>
    <cmr:column span="1">
      <input type="button" style="opacity: 0">
      <!-- hack to focus on top, bad bad -->
      <cmr:label fieldId="addressType_view">
        <span class="lbl-AddressType">${ui.addressType}</span>:</cmr:label>
    </cmr:column>

    <cmr:column span="1">
      <div id="addressType_view">-</div>
    </cmr:column>
    <cmr:view exceptForGEO="CEMEA">
      <cmr:column span="1">
        <cmr:label fieldId="cmrNo_view">
          <span class="lbl-Number">${ui.cmrNumber}</span>:</cmr:label>
      </cmr:column>
    </cmr:view>

    <cmr:column span="1">
      <div id="cmrNumber_view">-</div>
    </cmr:column>

    <cmr:column span="1">
      <cmr:label fieldId="sapNumber_view">
        <span class="lbl-SAPNumber">${ui.sapNo}</span>:</cmr:label>
    </cmr:column>
    <cmr:column span="1">
      <div id="sapNumber_view">-</div>
    </cmr:column>
  </cmr:row>
<!-- Taiwan 858 -->
  <cmr:view forCountry="858">
    <cmr:row>
      <cmr:column span="1" width="250">
        <cmr:label fieldId="custNm1_view">
          <span class="lbl-CustomerName1">Customer English Name</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
      </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="250">
        <cmr:label fieldId="custNm2_view">
          <span class="lbl-CustomerName2">Customer English Name Con't</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
      </cmr:row>
      <cmr:row>
      <cmr:column span="1" width="250">
        <cmr:label fieldId="ChinaCustomerName1_view">
          <span class="lbl-ChinaCustomerName1">Customer Chinese Name</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm3_view">-</div>
      </cmr:column>
    </cmr:row>
      <cmr:row>
      <cmr:column span="1" width="250">
        <cmr:label fieldId="ChinaCustomerName2_view">
          <span class="lbl-ChinaCustomerName2">Customer Chinese Name Con't</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:row>
    <br>
      <cmr:row addBackground="true">
      <cmr:column  span="1" width="250">
       <cmr:label fieldId="StreetAddress1_view">
        <span class="lbl-StreetAddress1">Customer English Address</span>
       </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt_view">-</div>
      </cmr:column>
     </cmr:row>
      <cmr:row addBackground="true">
      <cmr:column  span="1" width="250">
       <cmr:label fieldId="StreetAddress2_view">
        <span class="lbl-StreetAddress2">Customer English Address Con't</span>
       </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt2_view">-</div>
      </cmr:column>
     </cmr:row>
      <cmr:row addBackground="true">
      <cmr:column span="1" width="250">
       <cmr:label fieldId="ChinaStreetAddress1_view">
        <span class="lbl-ChinaStreetAddress1">Customer Chinese Address</span>
       </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
       <div id="dept_view">-</div>
      </cmr:column>
     </cmr:row>
      <cmr:row addBackground="true">
      <cmr:column span="1" width="250">
       <cmr:label fieldId="ChinaStreetAddress2_view">
        <span class="lbl-ChinaStreetAddress2">Customer Chinese Address Con't</span>
       </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
       <div id="bldg_view">-</div>
      </cmr:column>
     </cmr:row>
  </cmr:view>
  
  <!-- Korea -->
  <cmr:view forCountry="766">
    <br>
     <cmr:row addBackground="true">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="custNm1_view">
          <span class="lbl-CustomerName1">Customer Name 1</span>:
        </cmr:label>
      </cmr:column>
      <cmr:column span="2" width="700">
        <div id="custNm1_view">-</div>
      </cmr:column>
      <br>
      </cmr:row>    
      <cmr:row addBackground="true">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="custNm2_view">
          <span class="lbl-CustomerName2">Customer Name Con't</span>:
        </cmr:label>
      </cmr:column>
      <cmr:column span="2" width="700">
        <div id="custNm2_view">-</div>
      </cmr:column>
      <br>
      </cmr:row>
      <cmr:row addBackground="true">
      <cmr:column span="1" width="200">
        <cmr:label fieldId="custNm3_view">
          <span class="lbl-CustomerName3">Customer Name_Korean</span>:
        </cmr:label>
      </cmr:column>
      <cmr:column span="2" width="700">
        <div id="custNm3_view">-</div>
      </cmr:column>
      <br>
      </cmr:row>
      <cmr:row addBackground="true">
      <cmr:column span="1" width="300">
        <cmr:label fieldId="billingPstlAddr_view">
          <span class="lbl-BillingPstlAddr">Customer Name_Korean Continue</span>:
        </cmr:label>
      </cmr:column>
      <cmr:column span="2" width="700">
        <div id="billingPstlAddr_view">-</div>
      </cmr:column>
      </cmr:row>
      <br> 
      <cmr:row>
      <cmr:column  span="1" >
       <cmr:label fieldId="StreetAddress1_view">
        <span class="lbl-StreetAddress1">Street Address1</span>:
       </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt_view">-</div>
      </cmr:column>
      </cmr:row>
      <cmr:row>
      <cmr:column  span="1" >
       <cmr:label fieldId="StreetAddress2_view">
        <span class="lbl-StreetAddress2">Address Con't</span>:
       </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt2_view">-</div>
      </cmr:column>
      </cmr:row>         
     <cmr:row>    
      <cmr:column span="1" width="300">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">Street address_Korean</span>:
        </cmr:label>
      </cmr:column>
      <cmr:column span="2" width="700">
        <div id="custNm4_view">-</div>
      </cmr:column>
      </cmr:row>   
      <cmr:row>
      <cmr:column span="1" width="300">
        <cmr:label fieldId="divn_view">
          <span class="lbl-Divn">Street address_Korean Continue</span>:
        </cmr:label>
      </cmr:column>
      <cmr:column span="2" width="700" >
        <div id="divn_view">-</div>
      </cmr:column>
      </cmr:row>  
      <br>  
      <cmr:row>
        <cmr:column span="1">
	      <cmr:label fieldId="city1_view">
			<span class="lbl-City1">City</span>:
			 </cmr:label>
      	  </cmr:column>
		  <cmr:column span="2">
			 <div id="city1_view">-</div>
		</cmr:column>
		<cmr:column span="1">
	      <cmr:label fieldId="city2_view">
			<span class="lbl-City2">City Korean</span>:
			 </cmr:label>
      	  </cmr:column>
		  <cmr:column span="2">
			 <div id="city2_view">-</div>
		 </cmr:column>
      </cmr:row>
      <cmr:row>
      <cmr:column span="1">
        <label for="landCntry_view" style="display: inline; width: 150px !important"> <span class="lbl-LandedCountry">Country (Landed)</span>: <cmr:info
          text="${ui.info.custmerCountry}" /> </label>
      </cmr:column>
      <cmr:column span="2">
        <div id="landCntry_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="stateProv_view">
          <span class="lbl-StateProv">State/Province</span>:
          </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="stateProv_view">-</div>
      </cmr:column>
      </cmr:row>      
      <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="postCd_view">
          <span class="lbl-PostalCode">Postal Code</span>:
         </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="postCd_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="transportZone_view">
          <span class="lbl-addrUpdateDt">Transport Zone</span>:
          </cmr:label>
      </cmr:column>
      <cmr:column span="2">
      <div id="transportZone_view">-</div>
      </cmr:column>
      </cmr:row>
      <cmr:row>
       <cmr:column span="1">
        <cmr:label fieldId="contact_view">
          <span class="lbl-Contact">Name of person in charge of Invoice_1</span>:
          </cmr:label>
       </cmr:column>
      <cmr:column span="2">
        <div id="contact_view">-</div>
     </cmr:column>
      <cmr:column span="1">
	    <cmr:label fieldId="countyName_view">
			<span class="lbl-LocalLangCountryName">Name of person in charge of Invoice_2</span>:
		</cmr:label>
      </cmr:column>
		<cmr:column span="2">
			<div id="countyName_view">-</div>
		</cmr:column>
     </cmr:row> 
      <cmr:row>
      <cmr:column span="1">
	    <cmr:label fieldId="dept_view">
			<span class="lbl-Department">Department Name_1</span>:
		</cmr:label>
      </cmr:column>
		<cmr:column span="2">
			<div id="dept_view">-</div>
		</cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="poBoxCity_view">
          <span class="lbl-POBoxCity">Department Name_2</span>:
          </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="poBoxCity_view">-</div>
      </cmr:column>
     </cmr:row>    
     <cmr:row>
       <cmr:column span="1">
        <cmr:label fieldId="poBox_view">
          <span class="lbl-POBox">Telephone No_1</span>:
         </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="poBox_view">-</div>
      </cmr:column>
      <cmr:column span="1" >
       <cmr:label fieldId="taxOffice_view">
        <span class="lbl-TaxOffice">Telephone No_2</span>:
        </cmr:label>
       </cmr:column>
      <cmr:column span="2">
        <div id="taxOffice_view">-</div>
     </cmr:column>
     </cmr:row>    
    <cmr:row>    
      <cmr:column span="1">
        <cmr:label fieldId="floor_view">
          <span class="lbl-Floor">E-mail_1</span>:
          </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="floor_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="office_view">
          <span class="lbl-Office">E-mail_2</span>:
          </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="office_view">-</div>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>     
	  <cmr:column span="1">
		<cmr:label fieldId="bldg_view">
			<span class="lbl-Building">E-mail_3</span>
		</cmr:label>:
      </cmr:column>
	  <cmr:column span="2">
		<div id="bldg_view">-</div>
	  </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">Company Phone #</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
    </cmr:row>  
  </cmr:view>
  
  <!-- defect : 1444422 for FR -->
  <cmr:view forGEO="IERP,CND,MCO1,MCO2,FR,CEMEA,CN,EMEA,SWISS,NORDX,LA" exceptForCountry="618,866,754,755,726,666">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="ierpSitePrtyId_view">
          <span class="lbl-IERPSitePrtyId">${ui.iERPSitePrtyId}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="1">
        <div id="ierpSitePrtyId_view">-</div>
      </cmr:column>
      
       <cmr:view forGEO="SWISS">
         <div id= "hardwareMaster"> 
      <cmr:column span="2">
        <cmr:label fieldId="hwInstlMstrFlg_view">
          <span class="lbl-HwInstlMasterFlag">${ui.hwInstlMstrFlg}</span>
        </cmr:label>:
      </cmr:column>
       
      <cmr:column span="2">
        <div id="hwInstlMstrFlg_view">-</div>
      </cmr:column> 
      </div>    
    </cmr:view> 
     
    </cmr:row>
  </cmr:view>
  <cmr:view forGEO="NORDX">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">
          <span class="lbl-CustomerName1">${ui.custName1}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">
          <span class="lbl-CustomerName2">${ui.custName2}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:view forGEO="NORDX">
      <cmr:row>
        <cmr:column span="1">
          <cmr:label fieldId="custNm3_view">
            <span class="lbl-CustomerName3">${ui.custName3}:</span>
            <cmr:info text="${ui.info.NordicsForAdditionalInfo}" />
          </cmr:label>:
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm3_view">-</div>
        </cmr:column>
      </cmr:row>
    </cmr:view>
    <cmr:row>
      <cmr:column span="1" exceptForCountry="618">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">${ui.custName4}</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
     <cmr:column span="1">
        <cmr:label fieldId="stateProv_view">
          <span class="lbl-StateProv">State/Province</span>:
          </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="stateProv_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>

  <cmr:view forGEO="BELUX,NL">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">
          <span class="lbl-CustomerName1">${ui.custName1}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">
          <span class="lbl-CustomerName2">${ui.custName2}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>    
      <cmr:column span="1">
        <cmr:label fieldId="custNm3_view">
          <span class="lbl-CustomerName3">${ui.custName3}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm3_view">-</div>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">${ui.custName4}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>
  
   <cmr:view forGEO="AP">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">
          <span class="lbl-CustomerName1">${ui.custName1}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">
          <span class="lbl-CustomerName2">${ui.custName2}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>

  <cmr:view forCountry="754,866,755,758">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">${ui.custNameUKI1}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">${ui.custNameUKI2}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    <input type="hidden" name="custNm3_view" id="custNm3_view">
    <input type="hidden" name="custNm4_view" id="custNm4_view">
  </cmr:view>
  
  <cmr:view forCountry="862">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">${ui.custNameUKI1}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">${ui.custNameUKI2}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    <input type="hidden" name="custNm3_view" id="custNm3_view">
  </cmr:view>

<!-- Address summary for GR and CY -->
  <cmr:view forCountry="726,666">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">${ui.custNameUKI1}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
		<cmr:label fieldId="custNm2_view">
        <span class="lbl-CustomerName2">${ui.custNameUKI2}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    <input type="hidden" name="custNm3_view" id="custNm3_view">
  </cmr:view>
  
  <cmr:view forCountry="726">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">${ui.custNameUKI1}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
		<cmr:label fieldId="custNm2_view">
        <span class="lbl-CustomerName2">${ui.custName2}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    <input type="hidden" name="custNm3_view" id="custNm3_view">
  </cmr:view>

  <cmr:view forGEO="MCO,MCO1,MCO2">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">
          <span class="lbl-CustomerName1">${ui.custName1}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">
          <span class="lbl-CustomerName2">${ui.custName2}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    
    <cmr:row>
      <cmr:column span="1" forCountry="780">
        <cmr:label fieldId="custNm3_view">
          <span class="lbl-CustomerName3">${ui.custName3}:</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2" forCountry="780">
        <div id="custNm3_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>

  <cmr:view forGEO="CN">
	<%-- <c:if test="${reqentry.reqType == 'C' && reqentry.mainAddrType == 'ZS01'}"> --%>
     <cmr:row addBackground="true">
      <%-- 
      <cmr:column span="1">
        <cmr:label fieldId="cnCustContPhone2_view">Customer Contact's Phone Number 2:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustContPhone2_view">${ui.custContPhone2}</div>
      </cmr:column>
      --%>
      <cmr:column span="1">
        <cmr:label fieldId="cnCustContJobTitle_view">Customer Contact's Job Title:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustContJobTitle_view">${ui.custContJobTitle}</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="cnCustContNm_view">Customer Contact's Name (include salutation):</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustContNm_view">${ui.custContNm}</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">${ui.phone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
    </cmr:row>
   <%-- </c:if> --%>
    <cmr:row addBackground="false">
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">${ui.custName1}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="false">
      <cmr:column span="1">
        <cmr:label fieldId="cnCustName1_view">Customer Name Chinese:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustName1_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="false">
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">Customer Name Con't:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="false">
      <cmr:column span="1">
        <cmr:label fieldId="cnCustName2_view">Customer Name Con't Chinese:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustName2_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
        <cmr:column span="1">
          <cmr:label fieldId="custNm3_view">Customer Name Con't 2:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm3_view">-</div>
        </cmr:column>
      </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="cnCustName3_view">Customer Name Con't Chinese 2:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustName3_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>

	<!--DY: START AT SPECIFIC  -->
	<cmr:view forCountry="618,724">
		<cmr:row addBackground="false">
			<cmr:column span="1">
				<cmr:label fieldId="custNm1_view">
					<span class="lbl-CustomerName1">${ui.custName1}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="custNm1_view">-</div>
			</cmr:column>
		</cmr:row>
		<cmr:row>
			<cmr:column span="1">
				<cmr:label fieldId="custNm2_view">
					<span class="lbl-CustomerName2">${ui.custName2}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="custNm2_view">-</div>
			</cmr:column>
			<br>
		</cmr:row>
		<cmr:row>
			<cmr:column span="1">
				<cmr:label fieldId="custNm3_view">
					<span class="lbl-CustomerName3">${ui.custName3}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="custNm3_view">-</div>
			</cmr:column>
		</cmr:row>
		<cmr:row addBackground="false">
			<cmr:column span="1">
				<cmr:label fieldId="custNm4_view">
					<span class="lbl-CustomerName4">${ui.custName4}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="custNm4_view">-</div>
			</cmr:column>
		</cmr:row>
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<cmr:label fieldId="bldg_view">
					<span class="lbl-Building">${ui.bldg}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="bldg_view">-</div>
			</cmr:column>
		</cmr:row>
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<cmr:label fieldId="dept_view">
					<span class="lbl-Department">${ui.Department}</span>
				</cmr:label>:
      </cmr:column>
			<cmr:column span="2">
				<div id="dept_view">-</div>
			</cmr:column>
		</cmr:row>
		<cmr:row addBackground="true">
			<cmr:column span="1" exceptForCountry="724">
				<cmr:label fieldId="stateProv_view">
					<span class="lbl-StateProv">${ui.stateProve}</span>:</cmr:label>
			</cmr:column>
			<cmr:column span="2">
				<div id="stateProv_view">-</div>
			</cmr:column>
		</cmr:row>
	</cmr:view>

	<cmr:view forGEO="IERP,CND,CEMEA" exceptForCountry="754,866,755,726,862,666,618,724,848">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">${ui.custName1}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="custNm2_view">${ui.custName2}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm2_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:view exceptForCountry="760,848">
      <cmr:row>
        <cmr:column span="1">
          <cmr:label fieldId="custNm3_view">${ui.custName3}:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm3_view">-</div>
        </cmr:column>
      </cmr:row>
      <cmr:view exceptForGEO="CEMEA,JP,SWISS" exceptForCountry="618">
        <cmr:row>
          <cmr:column span="1">
            <cmr:label fieldId="custNm4_view">${ui.custName4}</cmr:label>:
        </cmr:column>
          <cmr:column span="2">
            <div id="custNm4_view">-</div>
          </cmr:column>
          <br>
        </cmr:row>
      </cmr:view>
   </cmr:view>
   <cmr:view forCountry="758,760,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,358,359,363,620,642,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,675">
    <cmr:row>
      <cmr:column span="1">
       	<cmr:label fieldId="custNm4_view">
         <span class="lbl-CustomerName4">${ui.attnPerson}</span>
      	</cmr:label>:
     </cmr:column>
     <cmr:column span="2">
      <div id="custNm4_view">-</div>
      </cmr:column>
   	  </cmr:row>
  </cmr:view>     

<%--      <cmr:view forGEO="SWISS">
        <cmr:row>
          <cmr:column span="1">
            <cmr:label fieldId="custNm4_view">${ui.custNameSwiss}</cmr:label>:
        </cmr:column>
          <cmr:column span="2">
            <div id="custNm4_view">-</div>
          </cmr:column>
          <br>
        </cmr:row>
      </cmr:view> --%>
   
    <cmr:view forCountry="641">
      <cmr:row>
        <cmr:column span="1">
          <cmr:label fieldId="cnCustName1_view">${ui.cnCustName1}:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="cnCustName1_view">-</div>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1">
          <cmr:label fieldId="cnCustName2_view">${ui.cnCustName2}:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="cnCustName2_view">-</div>
        </cmr:column>
      </cmr:row>
    </cmr:view>
  </cmr:view>
  	
  	<cmr:view forGEO="SWISS" forCountry="848">
		<cmr:row addBackground="false">
			<cmr:column span="1">
				<cmr:label fieldId="custNm1_view">
					<span class="lbl-CustomerName1">${ui.custName1}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="custNm1_view">-</div>
			</cmr:column>
		</cmr:row>
		<cmr:row>
			<cmr:column span="1">
				<cmr:label fieldId="custNm2_view">
					<span class="lbl-CustomerName2">${ui.custName2}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="custNm2_view">-</div>
			</cmr:column>
			<br>
		</cmr:row>
		<cmr:row>
			<cmr:column span="1">
				<cmr:label fieldId="divn_view">
					<span class="lbl-Division">${ui.divn}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="divn_view">-</div>
			</cmr:column>
		</cmr:row>
		<cmr:row addBackground="false">
			<cmr:column span="1">
				<cmr:label fieldId="city2_view">
					<span class="lbl-City2">${ui.city2}</span>
				</cmr:label>:
      		</cmr:column>
			<cmr:column span="2">
				<div id="city2_view">-</div>
			</cmr:column>
		</cmr:row>
		
	</cmr:view>
  <!--DENNIS: START JAPAN SPECIFIC  -->
  <cmr:view forGEO="JP">
   <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="custNm1_view">Customer Name-KANJI:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm1_view">-</div>
      </cmr:column>  
      </cmr:row>   
        <cmr:row>
          <cmr:column span="1">
            <cmr:label fieldId="custNm2_view">Name-KANJI Continue:</cmr:label>:
        </cmr:column>
          <cmr:column span="2">
            <div id="custNm2_view">-</div>
          </cmr:column>
          <br>
    </cmr:row>
    <cmr:row addBackground="true">
        <cmr:column span="1">
          <cmr:label fieldId="custNm4_view">Katakana:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm4_view">-</div>
        </cmr:column>
     </cmr:row>
     <cmr:row>
     <cmr:column span="1">
      <cmr:label fieldId="custNm3_view">Full English Name:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm3_view">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:row addBackground="true">
     <cmr:column span="1">
      <cmr:label fieldId="addrTxt_view">Address:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt_view">-</div>
      </cmr:column>
     </cmr:row>
    </cmr:view>
  
  <cmr:row addBackground="true">
    <cmr:view exceptForGEO="JP,KR">
      <cmr:column span="1">
        <label for="landCntry_view" style="display: inline; width: 150px !important"> <span class="lbl-LandedCountry">${ui.custCntry}</span>: <cmr:info
          text="${ui.info.custmerCountry}" /> </label>
      </cmr:column>
      <cmr:column span="2">
        <div id="landCntry_view">-xxx</div>
      </cmr:column>
    </cmr:view>
    <cmr:view forGEO="CEMEA" exceptForGEO="AP" exceptForCountry="618">
      <cmr:column span="1">
        <cmr:label fieldId="bldg_view">
          <span class="lbl-Building">Country Name (Local Language)</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="bldg_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view forGEO="MCO,MCO1,MCO2" exceptForCountry="780,618">
      <cmr:column span="1">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">${ui.custName4}</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view forCountry="726,666" >
      <cmr:column span="1">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">${ui.custNameSwiss}</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:view>
    
    <%if ("U".equals(reqentry.getReqType())){%> 
    <cmr:view forCountry="862" >
      <cmr:column span="1">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">${ui.custName4}</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:view>
    <%}%> 
    
    <cmr:view forGEO="SWISS">
      <cmr:column span="1">
        <cmr:label fieldId="custLangCd_view">
          <span class="lbl-CustLangCd">${ui.custLangCd}</span>:
        </cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custLangCd_view">-</div>
      </cmr:column>
    </cmr:view>
  </cmr:row>
  <!--DENNIS: START JAPAN SPECIFIC  -->
  <cmr:view forGEO="JP">
       <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="postCd_view">
          <span class="lbl-PostalCode">Postal Code</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="postCd_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="dept_view">Department:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dept_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>     
      <cmr:column span="1">
        <cmr:label fieldId="office_view">
          <span class="lbl-Office">Branch/Office</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="office_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
     <cmr:column span="1">
        <cmr:label fieldId="bldg_view">
          <span class="lbl-Building">Building</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="bldg_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">Tel No</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="LocationCode_view">
          <span class="lbl-LocationCode">Location</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="locationCode_view">-</div>
      </cmr:column>
  <cmr:column span="1">
        <cmr:label fieldId="CustFAX_view">FAX:</cmr:label>
  </cmr:column>
  <cmr:column span="2">
        <div id="custFax_view">-</div>
      </cmr:column>
  </cmr:row>
  <cmr:row  addBackground="true">
  <cmr:column span="1">
        <cmr:label fieldId="EstabFuncCd_view">
          <span class="lbl-EstabFuncCd">Estab Function Code</span>:</cmr:label>
  </cmr:column>
  <cmr:column span="2">
        <div id="estabFuncCd_view">-</div>
  </cmr:column>
  <cmr:column span="1">
        <cmr:label fieldId="Division_view">
          <span class="lbl-Division">Estab No</span>:</cmr:label>
  </cmr:column>
  <cmr:column span="2">
        <div id="divn_view">-</div>
  </cmr:column>
  </cmr:row>
  <cmr:row>
  <cmr:column span="1">
        <cmr:label fieldId="City2_view">
          <span class="lbl-City2">Company No</span>:</cmr:label>
  </cmr:column>
  <cmr:column span="2">
        <div id="city2_view">-</div>
  </cmr:column>
  <cmr:column span="1">
        <cmr:label fieldId="CompanySize_view">
          <span class="lbl-CompanySize">Company Size</span>:</cmr:label>
  </cmr:column>
  <cmr:column span="2">
        <div id="companySize_view">-</div>
  </cmr:column>
  </cmr:row>
  <cmr:row  addBackground="true">
  <cmr:column span="1">
        <cmr:label fieldId="Contact_view">
          <span class="lbl-Contact">Contact</span>:</cmr:label>
  </cmr:column>
  <cmr:column span="2">
        <div id="contact_view">-</div>
  </cmr:column>
  <cmr:column span="1">
        <cmr:label fieldId="ROL_view">
          <span class="lbl-ROL">ROL Flag</span>:</cmr:label>
  </cmr:column>
  <cmr:column span="2">
        <div id="rol_view">-</div>
  </cmr:column>
 </cmr:row>
  </cmr:view>
  <cmr:view exceptForGEO="CN,JP,SWISS,TW,KR">
    <cmr:row addBackground="true" topPad="10">
      <cmr:view forGEO="FR" forCountry="706">
        <cmr:column span="1">
          <cmr:label fieldId="custNm1_view">
            <span class="lbl-CustomerName1">Customer legal name</span>:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm1_view">-</div>
        </cmr:column>
        <cmr:column span="1">
          <cmr:label fieldId="custNm2_view">
            <span class="lbl-CustomerName2">Legal name continued</span>:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm2_view">-</div>
        </cmr:column>
      </cmr:view>
    </cmr:row>
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="1">
        <cmr:label fieldId="addrTxt_view">
          <span class="lbl-StreetAddress1">${ui.street}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt_view">-</div>
      </cmr:column>
      <cmr:column span="1" exceptForGEO="IERP,CND,CEMEA,NL,NORDX,SWISS,TW,KR,US" exceptForCountry='758,858,766,897'>
        <cmr:label fieldId="addrTxt2_view">
          <span class="lbl-StreetAddress2">${ui.street2}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" exceptForGEO="NL,BELUX,SWISS,TW,KR,US" exceptForCountry='758,858,766,897'>
        <div id="addrTxt2_view">-</div>
      </cmr:column>
    </cmr:row>

  </cmr:view>
  
  <cmr:view forGEO="SWISS">
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="1">
        <cmr:label fieldId="addrTxt_view">
          <span class="lbl-StreetAddress1">${ui.streetSwiss}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt_view">-</div>
      </cmr:column>
     
    </cmr:row>

  </cmr:view>

  <!--DENNIS: START CHINA SPECIFIC  -->
  <cmr:view forGEO="CN">
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="1">
        <cmr:label fieldId="addrTxt_view">
          <span class="lbl-StreetAddress1">Street Address 1</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="cnAddrTxt_view">Street Address Chinese:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnAddrTxt_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="1">
        <cmr:label fieldId="addrTxt2_view">
          <span class="lbl-StreetAddress2">Address Con't</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrTxt2_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="cnAddrTxt2_view">Address Con't Chinese:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnAddrTxt2_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="stateProv_view">
          <span class="lbl-StateProv">${ui.stateProve}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="stateProv_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="city1_view">
          <span class="lbl-City">${ui.city}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="city1_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="cnCity_view">City Chinese:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCity_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="city2_view">
          <span class="lbl-City2">${ui.dist}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="city2_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="cnDistrict_view">District Chinese:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnDistrict_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="dept_view">
          <span class="lbl-Department">${ui.dept}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="dept_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="bldg_view">
          <span class="lbl-Building">${ui.bldng}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="bldg_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="office_view">
          <span class="lbl-Office">${ui.office}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="office_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="poBox_view">
          <span class="lbl-POBox">${ui.poBox}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="poBox_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="postCd_view">
          <span class="lbl-PostalCode">${ui.postCd}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="postCd_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <c:if test="${reqentry.reqType != 'C' && reqentry.mainAddrType != 'ZS01'}">
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">${ui.phone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
      </c:if>
      <cmr:column span="1">
        <cmr:label fieldId="transportZone_view">
          <span class="lbl-addrUpdateDt">${ui.transportZone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="transportZone_view">-</div>
      </cmr:column>
    </cmr:row>
	<c:if test="${reqentry.reqType != 'C' && reqentry.mainAddrType != 'ZS01'}">
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="cnCustContPhone2_view">Customer Contact's Phone Number 2:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustContPhone2_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="cnCustContJobTitle_view">Customer Contact's Job Title:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustContJobTitle_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="cnCustContNm_view">Customer Contact's Name (include salutation):</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="cnCustContNm_view">-</div>
      </cmr:column>
    </cmr:row>
	</c:if>
  </cmr:view>
  <!-- DENNIS: END CHINA -->

  <cmr:view exceptForGEO="CN,JP,TW,KR">
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="city1_view">
          <span class="lbl-City">${ui.city}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="city1_view">-</div>
      </cmr:column>
      <cmr:view exceptForGEO="MCO1,MCO2,CEMEA,NORDX,BELUX,NL,SWISS,FR,TW,KR" exceptForCountry="666,726,822,838,758,724,858,766">
        <cmr:column span="1">
          <cmr:label fieldId="dept_view">
            <span class="lbl-Department">${ui.dept}</span>:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="dept_view">-</div>
        </cmr:column>
      </cmr:view>
      <cmr:view forGEO="FR" forCountry="706">
        <cmr:column span="1">
          <cmr:label fieldId="custNm3_view">
            <span class="lbl-CustomerName3">Division/Department</span>:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm3_view">-</div>
        </cmr:column>
      </cmr:view>
       <cmr:view forGEO="SWISS">
        <cmr:column span="1">
          <cmr:label fieldId="dept_view">
            <span class="lbl-Department">${ui.deptSwiss}</span>:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="dept_view">-</div>
        </cmr:column>
      </cmr:view>
    </cmr:row>
  </cmr:view>

  <cmr:row addBackground="true">
    <cmr:view exceptForGEO="IERP,MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,SWISS,TW,KR" exceptForCountry="726,666,862,822,838,758,631,781,815,760,858,766">
      <cmr:column span="1">
        <cmr:label fieldId="city2_view">
          <span class="lbl-City2">${ui.dist}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="city2_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view exceptForGEO="MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,AP,FR,TW,KR" exceptForCountry="862,726,666,822,838,758,760,724,858,766">
      <cmr:column span="1">
        <cmr:label fieldId="bldg_view">
          <span class="lbl-Building">${ui.bldng}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="bldg_view">-</div>
      </cmr:column>
    </cmr:view>
  </cmr:row>
  
<%--   <cmr:row>
  	<cmr:view forGEO="SWISS">
  		<cmr:column span="1">
          <cmr:label fieldId="custNm3_view">${ui.custName3}:</cmr:label>
        </cmr:column>
        <cmr:column span="2">
          <div id="custNm3_view">-</div>
        </cmr:column>
  	</cmr:view>
  </cmr:row> --%>

  <cmr:row addBackground="true">
    <cmr:view exceptForGEO="CEMEA,CN,JP,SWISS,TW,KR,NORDX" exceptForCountry="862,858,766,758">
      <cmr:column span="1">
        <cmr:label fieldId="stateProv_view">
          <span class="lbl-StateProv">${ui.stateProve}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="stateProv_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view exceptForGEO="MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,AP,FR,TW,KR" exceptForCountry="862,726,666,822,838,758,760,724,848,858,766,649">
      <cmr:column span="1">
        <cmr:label fieldId="floor_view">
          <span class="lbl-Floor">${ui.floor}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="floor_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view forGEO="FR" forCountry="706">
      <cmr:column span="1">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">Attention to/Building/Floor/Office</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:view>
  </cmr:row>

  <cmr:view exceptForGEO="CN,TW,KR,NORDX" exceptForCountry="618,724,858,766,649">
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="stateProv_view">
          <span class="lbl-StateProv">${ui.stateProve}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" >
        <div id="stateProv_view">-</div>
      </cmr:column>
      <cmr:column span="1" exceptForCountry="848">
        <cmr:label fieldId="custNm4_view">
          <span class="lbl-CustomerName4">${ui.custName4}</span>
        </cmr:label>:
      </cmr:column>
      <cmr:column span="2" exceptForCountry="848">
        <div id="custNm4_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>

  <cmr:row addBackground="true">
    <cmr:view exceptForGEO="MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,AP,SWISS,TW,KR" exceptForCountry="862,726,666,822,838,758,760,858,766,649">
      <cmr:column span="1">
        <cmr:label fieldId="county_view">
          <span class="lbl-County">${ui.county}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="county_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view exceptForGEO="MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,AP,SWISS,FR,TW,KR" exceptForCountry="862,726,666,822,838,758,760,858,766,649">
      <cmr:column span="1" exceptForCountry="724">
        <cmr:label fieldId="office_view">
          <span class="lbl-Office">${ui.office}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" exceptForCountry="724">
        <div id="office_view">-</div>
      </cmr:column>
    </cmr:view>
  </cmr:row>

  <cmr:row addBackground="true">
    <cmr:view exceptForGEO="CN,JP,KR">
      <cmr:column span="1">
        <cmr:label fieldId="postCd_view">
          <span class="lbl-PostalCode">${ui.postCd}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="postCd_view">-</div>
      </cmr:column>
    </cmr:view>
   
    <cmr:view forCountry="666,864">
     <c:if test="${reqentry.mainAddrType == 'ZS01' || reqentry.mainAddrType == 'ZD01'}">
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">${ui.phone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
       </c:if>
    </cmr:view>
   
    <cmr:view forCountry="780">
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">${ui.phone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
    </cmr:view>
    
    <cmr:view exceptForGEO="IERP,CND,MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,AP,SWISS,FR,TW,KR,US" exceptForCountry="862,726,666,822,838,758,631,760,858,766,649,897">
      <cmr:column span="1">
        <cmr:label fieldId="divn_view">
          <span class="lbl-Division">${ui.divn}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="divn_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view forGEO="US" forCountry="897">
      <cmr:column span="1">
        <cmr:label fieldId="divn_view">
          <span class="lbl-Division">Division/Address Con't</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="divn_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view forGEO="NORDX,BELUX,NL,SWISS">
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">${ui.phone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
    </cmr:view>
  </cmr:row>
  <!--  
 <cmr:view forGEO="NORDX">
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="machineTyp_view">
          <span class="lbl-MachineType">${grid.machineType}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="machineTyp_view">-</div>
      </cmr:column>
        </cmr:row>
        <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="machineSerialNo_view">
          <span class="lbl-MachineSerialNo">${grid.serialNumber}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="machineSerialNo_view">-</div>
      </cmr:column>
  </cmr:row>
  </cmr:view>
  -->
  
  <div id="machineSerialAddrDetails">
  <cmr:view forGEO="NORDX">
   <cmr:row addBackground="true">
    <cmr:column span="2">
      <p>
        <cmr:label fieldId="machineSerialType">
              ${ui.MachineSerialType}:
          <cmr:info text="${ui.info.MachineSerialType}" />
        </cmr:label>
      </p>
    </cmr:column>
  </cmr:row>
   <cmr:row addBackground="true">
   <cmr:column span="1">
         <div id="dvTable"></div>       
      </cmr:column>
        </cmr:row>
  </cmr:view>
</div>


  <!-- // special case for BR(631) customer template scenario -->
  <cmr:view forCountry="631">
    <!-- // 1164561 special case for BR(631) customer template scenario -->
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="taxCd1_view">
          <span class="lbl-LocaTax1">${ui.taxCodeOne}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="taxCd1_view">-</div>
      </cmr:column>
      <!-- // 1164558 special case for BR(631) mapping -->
      <cmr:column span="1">
        <cmr:label fieldId="taxCd2_view">
          <span class="lbl-LocaTax2">${ui.taxCodeTwo}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="taxCd2_view">-</div>
      </cmr:column>
    </cmr:row>
    <!-- // 1164558 special case for BR(631) mapping -->
    <cmr:row addBackground="true">
      <cmr:column span="1">
        <cmr:label fieldId="vat_view">
          <span class="lbl-Vat">${ui.vat}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="vat_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>
	
  <cmr:view forCountry="862">
    <cmr:row addBackground="true">
      <cmr:column span="1" containerForField="taxOffice_view">
       <cmr:label fieldId="taxOffice_view">
        <span class="lbl-TaxOffice">${ui.taxOffice}</span>:</cmr:label>
       </cmr:column>
      <cmr:column span="2">
        <div id="taxOffice_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" containerForField="custPhone_view">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">${ui.phone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
    </cmr:row>
   </cmr:view>

  <cmr:row topPad="10">
    <cmr:view exceptForGEO="AP,TW,KR" exceptForCountry="758,358,359,363,603,607,626,644,651,668,693,694,695,699,704,705,707,708,713,740,741,787,820,821,826,862,889,641,760,858,766">
      <cmr:column span="1">
        <cmr:label fieldId="poBox_view">
          <span class="lbl-POBox">${ui.poBox}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="poBox_view">-</div>
      </cmr:column>
    </cmr:view>
    <cmr:view exceptForGEO="CN" forCountry="618,706,649">
      <cmr:column span="1">
        <cmr:label fieldId="custPhone_view">
          <span class="lbl-CustPhone">${ui.phone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custPhone_view">-</div>
      </cmr:column>
    </cmr:view>
  </cmr:row>

  <cmr:view forCountry="822,838">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="prefSeqNo_view">
          <span class="lbl-prefSeqNo">${ui.prefSeqNo}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="prefSeqNo_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>

  <cmr:view exceptForGEO="IERP,CND,MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,AP,SWISS,TW,KR" exceptForCountry="862,726,666,822,838,758,760,858,766">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="poBoxCity_view">
          <span class="lbl-POBoxCity">${ui.poBoxCity}</span>:</cmr:label>
      </cmr:column>

      <cmr:column span="2">
        <div id="poBoxCity_view">-</div>
      </cmr:column>
      <cmr:column span="1">
        <cmr:label fieldId="custFax_view">
          <span class="lbl-CustFAX">${ui.custFax}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custFax_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>
<%--   <cmr:view forGEO="SWISS">
   <cmr:column span="1">
        <cmr:label fieldId="custFax_view">
          <span class="lbl-CustFAX">${ui.custFax}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="custFax_view">-</div>
      </cmr:column>
  </cmr:view> --%>

  <cmr:view exceptForGEO="MCO1,MCO2,CEMEA,CN,NORDX,BELUX,NL,JP,AP,SWISS,TW,KR" exceptForCountry="862,726,666,822,838,758,760,858,766">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="poBoxPostCd_view">
          <span class="lbl-POBoxPostalCode">${ui.poBoxPostCd}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="poBoxPostCd_view">-</div>
      </cmr:column>
      <cmr:column span="1" exceptForCountry="724">
        <cmr:label fieldId="transportZone_view" >
          <span class="lbl-addrUpdateDt">${ui.transportZone}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" exceptForCountry="724">
        <div id="transportZone_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>
  
    <cmr:view forGEO="BELUX" >
  </cmr:view>


  <cmr:view forCountry="758">
    <cmr:row addBackground="true">
      <cmr:column span="1" containerForField="addrAbbrevLocn_view">
        <cmr:label fieldId="addrAbbrevLocn_view">
          <span class="lbl-addrAbbrevLocn">${ui.addrAbbrevLocn}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" containerForField="addrAbbrevLocn_view">
        <div id="addrAbbrevLocn_view">-</div>
      </cmr:column>
      <cmr:column span="1" containerForField="addrAbbrevName_view">
        <cmr:label fieldId="addrAbbrevName_view">
          <span class="lbl-addrAbbrevName">${ui.addrAbbrevName}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" containerForField="addrAbbrevName_view">
        <div id="addrAbbrevName_view">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" containerForField="streetAbbrev_view">
        <cmr:label fieldId="streetAbbrev_view">
          <span class="lbl-streetAbbrev">${ui.streetAbbrev}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" containerForField="streetAbbrev_view">
        <div id="streetAbbrev_view">-</div>
      </cmr:column>
    </cmr:row>
    <%--<cmr:row addBackground="true">
      <cmr:column span="1" containerForField="billingPstlAddr_view">
        <cmr:label fieldId="billingPstlAddr_view">
          <span class="lbl-billingPstlAddr">${ui.billingPstlAddr}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2" containerForField="billingPstlAddr_view">
        <div id="billingPstlAddr_view">-</div>
      </cmr:column>
    </cmr:row>
    --%>

  </cmr:view>

  <cmr:view exceptForCountry="758,766">
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="addrCreateDt_view">
          <span class="lbl-addrCreateDt">${ui.rdcCreateDt}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrCreateDt_view">-</div>
      </cmr:column>

      <cmr:column span="1">
        <cmr:label fieldId="addrUpdateDt_view">
          <span class="lbl-addrUpdateDt">${ui.lastUpdated}</span>:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrUpdateDt_view">-</div>
      </cmr:column>
    </cmr:row>
  </cmr:view>

	<cmr:row topPad="8" addBackground="true">
    <cmr:column span="1" width="200">
      <p>
        <cmr:label fieldId="dplCheckResult_view">${ui.dplChkResult}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="2">
      <div id="dplChkResult_view">-</div>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="1" width="200">
      <p>
        <cmr:label fieldId="dplchk_ts">${ui.dpl.Timestamp} (<%=SystemConfiguration.getValue("DATE_TIMEZONE", "GMT")%>):</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="2">
      <div id="dplchk_ts">-</div>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="1" width="200">
      <p>
        <cmr:label fieldId="dplchk_by">${ui.dpl.CheckBy}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="2">
      <div id="dplchk_by">-</div>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="1" width="200">
      <p>
        <cmr:label fieldId="dplchk_err">${ui.dpl.DenialCode}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="2">
      <div id="dplchk_err">-</div>
    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="true">
    <cmr:column span="1" width="200">
      <p>
        <cmr:label fieldId="dplChkInfo_view">${ui.dplChkInfo}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="2">
      <div id="dplChkInfo_view">-</div>
    </cmr:column>
    <br>
  </cmr:row>

  <div style="display:none">
    <cmr:row topPad="8">
      <cmr:column span="1">
        <cmr:label fieldId="addressStdResult_view">${ui.addrStdResult}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addressStdResult_view">-</div>
      </cmr:column>
    </cmr:row>
  
  
    <cmr:row>
      <cmr:column span="1">
        <cmr:label fieldId="addressStdTs_view">${ui.addrStdTs}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addressStdTs_view">-</div>
      </cmr:column>
    </cmr:row>
  
  
    <cmr:row>
      <cmr:column span="1" width="250">
        <cmr:label fieldId="addressStdRejReason_view">${ui.addrStdRejReason}:</cmr:label>
      </cmr:column>
      <cmr:column span="2">
        <div id="addressStdRejReason_view">-</div>
      </cmr:column>
    </cmr:row>
  
  
    <cmr:row>
      <cmr:column span="1" width="250">
        <cmr:label fieldId="addressStdRejCmt_view">${ui.addrStdRejCmt}:</cmr:label>
      </cmr:column>
      <br>
    </cmr:row>
  
  
    <cmr:row>
      <cmr:column span="1">
        <p>
          <textarea id="addressStdRejCmt_view" readonly="true" rows="5" cols="70">-</textarea>
        </p>
      </cmr:column>
    </cmr:row>
  </div>


  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('AddressDetailsModal')" />
    <%
      if (!readOnly) {
    %>
    <cmr:button label="${ui.btn.update}" id="updateButtonFromView" onClick="doUpdateAddrFromDetails()" highlight="true" pad="true" />
    <%
      }
    %>
  </cmr:buttonsRow>


</cmr:modal>