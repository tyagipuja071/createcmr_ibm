<%@page import="com.ibm.cio.cmr.request.entity.Data"%>
<%@page import="com.ibm.cio.cmr.request.controller.DropdownListController"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.entity.Admin"%>
<%@page import="com.ibm.cio.cmr.request.entity.Addr"%>
<%@page import="com.ibm.cio.cmr.request.model.window.RequestSummaryModel"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestSummaryModel summary = (RequestSummaryModel) request.getAttribute("summary");
  Data data = summary.getData();
  Admin admin = summary.getAdmin();
  Addr addr = summary.getAddr();
  String cntry = data.getCmrIssuingCntry();
%>
<style>
div.cmr-summary {
  border: 1px Solid #999999;
  border-radius: 5px;
  width: 950px;
}

form.ibm-column-form .ibm-columns label,form.ibm-column-form label {
  font-size: 13px !important;
}

#ibm-content .ibm-columns {
  padding: 10px 10px 5px;
}

.ibm-col-4-2,.ibm-col-4-3,.ibm-col-5-2,.ibm-col-5-3,.ibm-col-5-4,.ibm-col-6-3,.ibm-col-6-4,.ibm-col-6-5 {
  font-size: 14px;
  line-height: 1.9rem;
}

.ibm-col-5-1,.ibm-col-6-1,#ibm-content-sidebar {
  font-size: 14px;
  line-height: 1.9rem;
}
</style>
<cmr:window>
  <div class="cmr-summary">
    <cmr:form method="GET" action="${contextPath}/window/summary/create" name="frmCMR" class="ibm-column-form ibm-styled-form"
      modelAttribute="summary">
      <!--  Main Details Section -->
      <jsp:include page="summary_main.jsp" />

      <cmr:view exceptForGEO="EMEA,IERP,CND,MCO,MCO1,FR,CEMEA,NORDX,BELUX,NL,JP,SWISS">
        <cmr:row addBackground="true">
          <cmr:column span="1" width="127">
            <label>${ui.customerName} 1:</label>
          </cmr:column>
          <cmr:column span="3" width="460">
            ${summary.admin.mainCustNm1}
          </cmr:column>
        </cmr:row>
        <cmr:row addBackground="true">
          <cmr:column span="1" width="127">
            <label>${ui.customerName} 2:</label>
          </cmr:column>
          <cmr:column span="3" width="460">
            ${summary.admin.mainCustNm2}
          </cmr:column>
        </cmr:row>
      </cmr:view>
      <cmr:view forGEO="IERP,CND,JP">
        <cmr:row addBackground="true">
          <cmr:column span="1" width="127">
            <label>${ui.requestingLOB}</label>
          </cmr:column>
          <cmr:column span="3" width="460">
            ${summary.admin.requestingLob}
          </cmr:column>
        </cmr:row>
      </cmr:view>

      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">
          <label>${ui.nameAddressSldTo}:</label>
        </cmr:column>
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="AddressType" />:</label>
        </cmr:column>
        <cmr:column span="1" width="240">
				 ${summary.addr.id.addrType} - ${summary.addrtypetxt}
				  <br>
          <c:if test="${summary.isOthraddrexist() == true}">
            <form:checkbox id="otherAddrExist" path="othraddrexist" disabled="true" value="Y" />
          </c:if>
          <c:if test="${summary.isOthraddrexist() == false}">
            <form:checkbox id="otherAddrExist" path="othraddrexist" disabled="true" value="N" />
          </c:if>
          <label style="display: inline; width: 160px !important" for="otherAddrExist">${ui.otherAddrExist}</label>
        </cmr:column>
        <cmr:view exceptForGEO="JP">
        <cmr:column span="1" width="130">
          <label>${ui.cntry}:</label>
        </cmr:column>
        <cmr:column span="1" width="130">
          ${summary.addr.landCntry}
          ${summary.landedcountry != null ? " - ".concat(summary.landedcountry) : ""}
				</cmr:column>
		</cmr:view>
      </cmr:row>

      <cmr:view forGEO="EMEA,IERP,CND,MCO,MCO1,MCO2,FR,CEMEA,NORDX,BELUX,NL,CN,JP,SWISS" exceptForCountry="618">
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240"><span style="word-wrap: break-word">
            ${summary.addr.custNm1}</span>
          </cmr:column>
          <cmr:column span="1" width="130" exceptForCountry="755">
            <label><cmr:fieldLabel fieldId="CustomerName2" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170" exceptForCountry="755"><span style="word-wrap: break-word">
            ${summary.addr.custNm2}</span>
          </cmr:column>
        </cmr:row>
      </cmr:view>
      
<cmr:view forCountry="618">
	<cmr:row>
		<cmr:column span="1" width="127" />
			<cmr:column span="1" width="130">
				<label><cmr:fieldLabel fieldId="CustomerName1" />:</label>
			</cmr:column>
		<cmr:column span="1" width="460">
			${summary.addr.custNm1}
		</cmr:column>		
	</cmr:row>
	
	<cmr:row>
		<cmr:column span="1" width="127" />
			<cmr:column span="1" width="130">
				<label><cmr:fieldLabel fieldId="CustomerName2" />:</label>
			</cmr:column>
		<cmr:column span="1" width="460">
            ${summary.addr.custNm2}
		</cmr:column>
	</cmr:row>
			
	<cmr:row addBackground="false">
		<cmr:column span="1" width="127"/>
			<cmr:column span="1" width="135">
				<label><cmr:fieldLabel fieldId="CustomerName3" />:</label>
			</cmr:column>
		<cmr:column span="1" width="460">
       		${summary.addr.custNm3}
     	</cmr:column>
	</cmr:row>
				
	<cmr:row addBackground="false">
		<cmr:column span="1" width="127"/>
			<cmr:column span="1" width="130">
				<label><cmr:fieldLabel fieldId="CustomerName4" />:</label>
			</cmr:column>
		<cmr:column span="1" width="460">
			${summary.addr.custNm4}
		</cmr:column>
	</cmr:row>
				
	<cmr:row>
		<cmr:column span="1" width="127" />
		<cmr:column span="1" width="130">
			<label><cmr:fieldLabel fieldId="StateProv" />:</label>
				</cmr:column>
		<cmr:column span="1" width="240">
			${summary.addr.stateProv}
			${summary.stateprovdesc != null ? " - ".concat(summary.stateprovdesc) : ""}
		</cmr:column>
	</cmr:row>
				
	<cmr:row addBackground="false">
		<cmr:column span="1" width="127"/>
		<cmr:column span="1" width="130">
			<label><cmr:fieldLabel fieldId="Building" />:</label>
			</cmr:column>
		<cmr:column span="1" width="460">
        	${summary.addr.bldg}
        </cmr:column>
	</cmr:row>
			
	<cmr:row addBackground="false">
		<cmr:column span="1" width="127"/>
		<cmr:column span="1" width="130">
			<label><cmr:fieldLabel fieldId="Department" />:</label>
				</cmr:column>
		<cmr:column span="1" width="460">
			${summary.addr.dept}
		</cmr:column>
	</cmr:row>
			
</cmr:view>
      
      <cmr:view forCountry="780">
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName3" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm3}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130" forCountry="780">
			<label><cmr:fieldLabel fieldId="POBox" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170" forCountry="780">
			${summary.addr.poBox}
		</cmr:column>
		
        </cmr:row>
        
      </cmr:view>
      
      <cmr:view forCountry="858">
      	<cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm1}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="CustomerName2" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.custNm2}
		</cmr:column>		
        </cmr:row>
        
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="ChinaCustomerName1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm3}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="ChinaCustomerName2" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.custNm4}
		</cmr:column>	
			
        </cmr:row>
        
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="StreetAddress1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.addrTxt}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="StreetAddress2" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.addrTxt2}
		</cmr:column>		
        </cmr:row>
        
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="ChinaStreetAddress1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.dept}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="ChinaStreetAddress2" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.bldg}
		</cmr:column>		
        </cmr:row>
        
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="PostalCode" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.postCd}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="LandedCountry" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.landCntry}
		</cmr:column>		
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="SAPNumber" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.sapNo}
          </cmr:column>
	 	        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">

		</cmr:column>
        <cmr:column span="2" width="170">
		</cmr:column>
        </cmr:row>
        
      </cmr:view>
      
      <cmr:view forCountry="766">
      	<cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm1}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="CustomerName2" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.custNm2}
		</cmr:column>		
        </cmr:row>
        
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName3" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm3}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="BillingPstlAddr" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.billingPstlAddr}
		</cmr:column>	
			
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName4" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm4}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="DIVN" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.divn}
		</cmr:column>		
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="LandedCountry" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.landCntry}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="StateProv" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.stateProv}
		</cmr:column>		
        </cmr:row>
                <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="City1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.city1}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="City2" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.city2}
		</cmr:column>		
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="StreetAddress1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.addrTxt}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="StreetAddress2" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.addrTxt2}
		</cmr:column>		
        </cmr:row>
        
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="PostalCode" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.postCd}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="POBox" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.poBox}
		</cmr:column>		
        </cmr:row>
        
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="TransportZone" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.transportZone}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="Contact" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.contact}
		</cmr:column>		
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="Department" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.dept}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="Floor" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.floor}
		</cmr:column>		
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="Office" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.office}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="TaxOffice" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.taxOffice}
		</cmr:column>		
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="Building" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.bldg}
          </cmr:column>
        
        <cmr:column span="1" width="50">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="CustPhone" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.custPhone}
		</cmr:column>		
        </cmr:row>
        <cmr:row>
        <cmr:column span="1" width="127">
          </cmr:column>
		<cmr:column span="2" width="130">
			<label><cmr:fieldLabel fieldId="POBoxCity" />:</label>
		</cmr:column>
        <cmr:column span="2" width="170">
			${summary.addr.poBoxCity}
		</cmr:column>		
		<cmr:column span="1" width="50">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="SAPNumber" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.sapNo}
          </cmr:column>

        </cmr:row>
        
      </cmr:view>
      

      <cmr:view forGEO="CEMEA,BELUX,JP" exceptForCountry="618">
        <cmr:column span="1" width="137">
        </cmr:column>
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="CustomerName3" />:</label>
        </cmr:column>
        <cmr:column span="1" width="240">
          ${summary.addr.custNm3}
        </cmr:column>
     	   <cmr:view forGEO="BELUX,JP" exceptForCountry="780">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName4" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm4}
          </cmr:column>
        </cmr:view>
      </cmr:view>

      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">
        </cmr:column>
        <cmr:view exceptForGEO="JP,TW,KR">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="StreetAddress1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240"><span style="word-wrap: break-word">
            ${summary.addr.addrTxt}</span>
		  </cmr:column>
		</cmr:view>
		<cmr:view forGEO="JP">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="AddressTxt" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.addrTxt}
		  </cmr:column>
		</cmr:view>
        <cmr:column span="1" width="130" exceptForCountry="755,848,724,897" exceptForGEO="CEMEA,NL,NORDX,JP,TW,KR,US">
          <label><cmr:fieldLabel fieldId="StreetAddress2" />:</label>
        </cmr:column>
        <cmr:column span="1" width="170" exceptForCountry="755,848,724,897" exceptForGEO="CEMEA,NL,NORDX,JP,TW,KR,US">
          ${summary.addr.addrTxt2}
				</cmr:column>
        <cmr:column span="1" width="130" forCountry="755">
          <label><cmr:fieldLabel fieldId="POBox" />:</label>
        </cmr:column>
        <cmr:column span="1" width="170" forCountry="755">
          ${summary.addr.poBox}
        </cmr:column>
        <cmr:view forGEO="NL">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName4" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm4}
          </cmr:column>
          </cmr:view>
        <cmr:view forGEO="NORDX">
          <cmr:column span="1" width="130">
            <label>
              <cmr:fieldLabel fieldId="CustomerName3" />:
              <cmr:info text="${ui.info.NordicsForAdditionalInfo}" />
            </label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm3}
          </cmr:column>
        </cmr:view>
      </cmr:row>

      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">
        </cmr:column>
        <cmr:view exceptForGEO="JP,TW,KR">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="City1" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240"><span style="word-wrap: break-word">
            ${summary.addr.city1}</span>
		  </cmr:column>
		</cmr:view>
		<cmr:view forGEO="JP">
		  <cmr:column span="1" width="130">
            <label> <cmr:fieldLabel fieldId="PostalCode" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.postCd}
		  </cmr:column>
		</cmr:view>
        <cmr:view exceptForGEO="MCO,MCO1,MCO2,NORDX,CEMEA,TW,KR">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="Department" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.dept}
          </cmr:column>
        </cmr:view>
        <cmr:view forGEO="MCO,MCO1,MCO2,NORDX" exceptForCountry="780">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustomerName4" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custNm4}
          </cmr:column>
        </cmr:view>
      </cmr:row>

      <cmr:view exceptForGEO="EMEA,MCO,MCO1,MCO2,CEMEA,NORDX,BELUX,NL,TW,KR">
        <cmr:row addBackground="false">
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:view exceptForGEO="IERP,CND,JP" exceptForCountry="780">
            <cmr:column span="1" width="130">
              <label><cmr:fieldLabel fieldId="City2" />:</label>
            </cmr:column>
            <cmr:column span="1" width="240">
            ${summary.addr.city2}
  				</cmr:column>
          </cmr:view>
          <cmr:view forGEO="JP" exceptForCountry="724">
          <cmr:column span="1" width="130">
            <label> <cmr:fieldLabel fieldId="Office" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.office}
          </cmr:column>
          </cmr:view>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="Building" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.bldg}
          </cmr:column>
        </cmr:row>
        
        <cmr:view exceptForGEO="JP,SWISS,TW,KR">
        <cmr:row addBackground="false">
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="StateProv" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.stateProv}
            ${summary.stateprovdesc != null ? " - ".concat(summary.stateprovdesc) : ""}
  				</cmr:column>
          <cmr:column span="1" width="130" exceptForCountry="724">
            <label> <cmr:fieldLabel fieldId="Floor" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170" exceptForCountry="724">
            ${summary.addr.floor}
          </cmr:column>
        </cmr:row>

        <cmr:row addBackground="false">
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="County" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.countyDesc}
          </cmr:column>
          <cmr:column span="1" width="130" exceptForCountry="724">
            <label> <cmr:fieldLabel fieldId="Office" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170" exceptForCountry="724">
            ${summary.addr.office}
          </cmr:column>
        </cmr:row>
        
        </cmr:view>
        
      </cmr:view>

      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">
        </cmr:column>
        <cmr:view exceptForGEO="JP,TW,KR">
        <cmr:column span="1" width="130">
          <label> <cmr:fieldLabel fieldId="PostalCode" />:</label>
        </cmr:column>
        <cmr:column span="1" width="240">
          ${summary.addr.postCd}
				</cmr:column>
		</cmr:view>
		<cmr:view forGEO="JP">
		<cmr:column span="1" width="130">
          <label> <cmr:fieldLabel fieldId="Contact" />:</label>
        </cmr:column>
        <cmr:column span="1" width="240">
          ${summary.addr.contact}
				</cmr:column>
		</cmr:view>
        <cmr:view forGEO="MCO,MCO1,MCO2,FR,NORDX,BELUX,NL,JP">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustPhone" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.custPhone}
          </cmr:column>
        </cmr:view>

        <cmr:column span="1" width="130" exceptForCountry="755">
          <label><cmr:fieldLabel fieldId="StateProv" />:</label>
        </cmr:column>
        <cmr:column span="1" width="200" exceptForCountry="755">
            ${summary.addr.stateProv}
            ${summary.stateprovdesc != null ? " - ".concat(summary.stateprovdesc) : ""}
        </cmr:column>
        <cmr:column span="1" width="130" exceptForGEO="EMEA,IERP,CND,MCO,MCO1,MCO2,CEMEA,FR,NORDX,BELUX,NL,JP,TW,KR" exceptForCountry="631">
          <label> <cmr:fieldLabel fieldId="Division" />:</label>
        </cmr:column>
        <cmr:column span="1" width="170" exceptForGEO="EMEA,IERP,CND,MCO,MCO1,MCO2,CEMEA,FR,NORDX,BELUX,NL,JP,TW,KR" exceptForCountry="631">
          ${summary.addr.divn}
        </cmr:column>
      </cmr:row>
      
      <cmr:view forGEO="CN">
      	<cmr:row>
      	<cmr:column span="1" width="127">
          </cmr:column>
      	<cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="ChinaCustomerName1" />:</label>
        </cmr:column>	
        <cmr:column span="1" width="240">
          ${summary.cnCustName1}
        </cmr:column>
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="ChinaCustomerName2" />:</label>
        </cmr:column>	
        <cmr:column span="1" width="170">
          ${summary.cnCustName2}
        </cmr:column>
      	</cmr:row>
      	<cmr:column span="1" width="127">
          </cmr:column>
      	<cmr:row>
      	<cmr:column span="1" width="127">
          </cmr:column>
      	<cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="ChinaStreetAddress1" />:</label>
        </cmr:column>	
        <cmr:column span="1" width="240">
          ${summary.cnAddrTxt}
        </cmr:column>
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="ChinaStreetAddress2" />:</label>
        </cmr:column>	
        <cmr:column span="1" width="170">
          ${summary.cnAddrTxt2}
        </cmr:column>	
      	</cmr:row>
      	<cmr:row>
      	<cmr:column span="1" width="127">
        </cmr:column>
      	<cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="DropDownCityChina" />:</label>
        </cmr:column>	
        <cmr:column span="1" width="240">
          ${summary.cnCity}
        </cmr:column>
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="ChinaCity2" />:</label>
        </cmr:column>	
        <cmr:column span="1" width="170">
          ${summary.cnDistrict}
        </cmr:column>	
      	</cmr:row>
      	
      	<cmr:row>
      	<cmr:column span="1" width="127">
          </cmr:column>
      	<cmr:column span="1" width="130">
      	  <label><cmr:fieldLabel fieldId="CustPhone" />:</label>
      	</cmr:column>
      	<cmr:column span="1" width="240">
      	  ${summary.addr.custPhone}
      	</cmr:column>
      	<cmr:column span="1" width="130">
      	  <label><cmr:fieldLabel fieldId="CustomerCntJobTitle" />:</label>
      	</cmr:column>
      	<cmr:column span="1" width="170">
      	   ${summary.cnCustContJobTitle}
      	</cmr:column>
      	</cmr:row>
      	<cmr:row>
      	<cmr:column span="1" width="127">
          </cmr:column>
      	<cmr:column span="1" width="130">
      	  <label><cmr:fieldLabel fieldId="ChinaCustomerCntName" />:</label>
      	</cmr:column>
      	<cmr:column span="1" width="240">
      	   ${summary.cnCustContNm}
      	</cmr:column>
      	</cmr:row>
      </cmr:view>

      <cmr:view forGEO="FR">
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label> <cmr:fieldLabel fieldId="CustomerName4" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.custNm4}
          </cmr:column>
          <cmr:view forGEO="FR">
            <cmr:column span="1" width="130">
              <label> <cmr:fieldLabel fieldId="CustomerName3" />:</label>
            </cmr:column>
            <cmr:column span="1" width="240">
              ${summary.addr.custNm3}
            </cmr:column>
          </cmr:view>
        </cmr:row>
      </cmr:view>

<%--       <cmr:view forGEO="BELUX">
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
         <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="StateProv" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
            ${summary.addr.stateProv}
            ${summary.stateprovdesc != null ? " - ".concat(summary.stateprovdesc) : ""}
          </cmr:column>
        </cmr:row>
      </cmr:view> --%>

      <cmr:view forGEO="MCO,NORDX,NL">
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="POBox" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.poBox}
          </cmr:column>
          <cmr:view forCountry="618,846,806,702,678,788">
            <cmr:column span="1" width="130">
              <label><cmr:fieldLabel fieldId="StateProv" />:</label>
            </cmr:column>
            <cmr:column span="1" width="240">
	              ${summary.addr.stateProv}
	              ${summary.stateprovdesc != null ? " - ".concat(summary.stateprovdesc) : ""}
	          </cmr:column>
          </cmr:view>
        </cmr:row>
      </cmr:view>


      <cmr:view forGEO="MCO">
        <cmr:row addBackground="false">
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="PrefSeqNo" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
              ${summary.addr.prefSeqNo}
            </cmr:column>
        </cmr:row>
      </cmr:view>
      
      <cmr:view forGEO="JP">
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="LocationCode" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.locationCode}
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustFAX" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
	        ${summary.addr.custFax}
	      </cmr:column>
        </cmr:row>
        
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="EstabFuncCd" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.estabFuncCd}
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="Division" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
	        ${summary.addr.divn}
	      </cmr:column>
        </cmr:row>
        
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="City2" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.city2}
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CompanySize" />:</label>
          </cmr:column>
          <cmr:column span="1" width="170">
	        ${summary.addr.companySize}
	      </cmr:column>
        </cmr:row>
        
        <cmr:row>
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="ROL" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            ${summary.addr.rol}
          </cmr:column>
        </cmr:row>
      </cmr:view>
      
      <!--  Start Customer Information -->
      <cmr:row topPad="10" addBackground="true">
        <cmr:column span="1" width="127">
          <label>${ui.customerInfo}:</label>
        </cmr:column>
        <cmr:view exceptForGEO="CEMEA,FR,SWISS">
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="CustLang" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            <%
              String custLang = DropdownListController.getDescription("CustLang", data.getCustPrefLang(), cntry, true);
            %>
            <%=custLang != null ? custLang : ""%>
          </cmr:column>
        </cmr:view>

        <cmr:column span="1" width="90">
          <label><cmr:fieldLabel fieldId="SensitiveFlag" />:</label>
        </cmr:column>
        <cmr:column span="1" width="170">
          <%
            String sensitiveFlag = DropdownListController.getDescription("SensitiveFlag", data.getSensitiveFlag(), cntry);
          %>
          <%=sensitiveFlag != null ? sensitiveFlag : ""%>
        </cmr:column>
      </cmr:row>



      <cmr:row addBackground="true">
        <cmr:column span="1" width="127">
        </cmr:column>
        <cmr:column span="1" width="130">
          <label>${ui.isicuncd}:</label>
        </cmr:column>
        <cmr:column span="1" width="240">
          <%
            String isic = summary.getIsicDesc();
          %>
          <%=isic != null ? data.getIsicCd() + " - " + isic : ""%>
        </cmr:column>

        <cmr:column span="1" width="90">
          <label><cmr:fieldLabel fieldId="Subindustry" />:</label>
        </cmr:column>
        <cmr:column span="1" width="240">
          <%
            String subind = DropdownListController.getDescription("Subindustry", data.getSubIndustryCd(), cntry);
          %>
          <%=subind != null ? subind : ""%>
        </cmr:column>

      </cmr:row>


      <cmr:row addBackground="true">
        <cmr:column span="1" width="127">
        </cmr:column>
        <cmr:column span="1" width="130" exceptForCountry="780">
          <label><cmr:fieldLabel fieldId="VAT" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240" exceptForCountry="780">
          ${summary.data.vat}
				</cmr:column>

        <cmr:column span="1" width="90" forGEO="EMEA,MCO,NORDX,CEMEA">
          <cmr:view exceptForCountry="758">
            <label><cmr:fieldLabel fieldId="VATExempt" />: </label>
          </cmr:view>
        </cmr:column>
        <cmr:column span="1" width="100" exceptForCountry="780">
          ${summary.data.vatExempt == 'Y' ? "Yes" : ""}
        </cmr:column>
      </cmr:row>

      <!-- US Customer Fields -->
      <jsp:include page="US/us_customer_summary.jsp" />

      <!-- EMEA Customer Fields -->
      <jsp:include page="EMEA/emea_customer_summary.jsp" />

      <!-- DE Customer Fields -->
      <jsp:include page="DE/de_customer_summary.jsp" />

      <!-- CND Customer Fields -->
      <jsp:include page="CND/cnd_customer_summary.jsp" />

      <!-- AP Customer Fields -->
      <jsp:include page="AP/ap_customer_summary.jsp" />

      <!-- MCO Customer Fields -->
      <jsp:include page="MCO/mco_customer_summary.jsp" />

      <!-- FR Customer Fields -->
      <jsp:include page="FR/fr_customer_summary.jsp" />

      <!-- JP Customer Fields -->
      <jsp:include page="JP/jp_customer_summary.jsp" />

      <!-- CEMEA Customer Fields -->
      <jsp:include page="CEMEA/cemea_customer_summary.jsp" />

      <!-- NORDX Customer Fields -->
      <jsp:include page="NORDX/nordx_customer_summary.jsp" />

      <!-- BELUX Customer Fields -->
      <jsp:include page="BELUX/belux_customer_summary.jsp" />
      
      <!-- NL Customer Fields -->
      <jsp:include page="NL/nl_customer_summary.jsp"></jsp:include>
      
      <!-- CN Customer Fields -->
      <jsp:include page="CN/cn_customer_summary.jsp"></jsp:include>
      
      <!-- TW Customer Fields -->
      <jsp:include page="TW/tw_customer_summary.jsp"></jsp:include>
      
      <!-- KR Customer Fields -->
      <jsp:include page="KR/kr_customer_summary.jsp"></jsp:include>


      <!--  Start IBM Information -->
      <cmr:view exceptForGEO="EMEA,FR">
        <cmr:row addBackground="false" topPad="10">
          <cmr:column span="1" width="127">
            <label>${ui.ibmInfo}:</label>
          </cmr:column>
          <cmr:view exceptForGEO="MCO,MCO1,MCO2,CEMEA,NORDX,BELUX,NL" exceptForCountry="780">
            <cmr:column span="1" width="130">
              <label> <cmr:fieldLabel fieldId="CustClassCode" />: </label>
            </cmr:column>
            <cmr:column span="1" width="240">

              <%
                String custClass = DropdownListController.getDescription("CustClassCode", data.getCustClass(), cntry);
              %>
              <%=custClass != null ? custClass : ""%>
            </cmr:column>
          </cmr:view>
          <cmr:view exceptForGEO="IERP,CND,MCO,MCO1,MCO2,CEMEA,NORDX,NL" exceptForCountry="780">
            <cmr:column span="1" width="130">
              <label><cmr:fieldLabel fieldId="Affiliate" />: </label>
            </cmr:column>
            <cmr:column span="1" width="170">
            ${summary.data.affiliate}
  				</cmr:column>
          </cmr:view>
        </cmr:row>

        <cmr:row addBackground="false">
          <cmr:column span="1" width="127">

          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="ISU" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            <%
              String isu = DropdownListController.getDescription("ISU", data.getIsuCd(), cntry);
            %>
            <%=isu != null ? isu : ""%>
          </cmr:column>
          <cmr:view exceptForGEO="MCO1,MCO2,NORDX">
            <cmr:column span="1" width="130">
              <label><cmr:fieldLabel fieldId="Enterprise" />:</label>
            </cmr:column>
            <cmr:column span="1" width="170">
              ${summary.data.enterprise}
    				</cmr:column>
          </cmr:view>
          
          <cmr:view forCountry="780">
            <cmr:column span="1" width="130">
              <label><cmr:fieldLabel fieldId="Enterprise" />:</label>
            </cmr:column>
            <cmr:column span="1" width="170">
              ${summary.data.enterprise}
    				</cmr:column>
          </cmr:view>
        </cmr:row>

        <cmr:row addBackground="false">
          <cmr:column span="1" width="127">

          </cmr:column>
          <cmr:column span="1" width="130">
            <label><cmr:fieldLabel fieldId="ClientTier" />:</label>
          </cmr:column>
          <cmr:column span="1" width="240">
            <%
              String clientTier = DropdownListController.getDescription("ClientTier", data.getClientTier(), cntry);
            %>
            <%=clientTier != null ? clientTier : ""%>
          </cmr:column>
          <cmr:view exceptForGEO="IERP,CND,MCO,MCO1,MCO2,CEMEA,NL" exceptForCountry="780">
            <cmr:column span="1" width="130">
              <label> <cmr:fieldLabel fieldId="Company" />: </label>
            </cmr:column>
            <cmr:column span="1" width="170">
            ${summary.data.company}
  				</cmr:column>
          </cmr:view>
        </cmr:row>

        <cmr:row addBackground="false">
          <cmr:column span="1" width="127">
          </cmr:column>
          <cmr:view exceptForGEO="IERP,CND,MCO,MCO1,MCO2">
            <cmr:column span="1" width="130">
              <label>${ui.inactypecd}:</label>
            </cmr:column>
            <cmr:column span="1" width="240">
              <c:if test="${summary.data.inacType == 'I'}">
               INAC
             </c:if>
              <c:if test="${summary.data.inacType == 'N'}">
               NAC
             </c:if>
             ${summary.data.inacCd != null ? summary.data.inacCd : ""}
  				</cmr:column>
            <cmr:view exceptForGEO="MCO1,MCO2,CEMEA,NORDX,NL">
              <cmr:column span="1" width="130">
                <label><cmr:fieldLabel fieldId="SearchTerm" />: </label>
              </cmr:column>
              <cmr:column span="1" width="170">
              ${summary.data.searchTerm}
    				  </cmr:column>
            </cmr:view>
          </cmr:view>
        </cmr:row>
      </cmr:view>

      <!-- EMEA IBM Fields (on top-->
      <jsp:include page="EMEA/emea_ibm_summary.jsp" />
      <!-- LA Fields -->
	  <jsp:include page="LA/la_ibm_summary.jsp" />

      <cmr:row addBackground="false">
        <cmr:column span="1" width="127" forGEO="FR">
          <label>${ui.ibmInfo}:</label>
        </cmr:column>
        <cmr:column span="1" width="127" forCountry="758,760">
          <!-- !I AM IMPORTANT for indention -->
        </cmr:column>
        <cmr:column span="1" width="127" forGEO="NORDX,BELUX,NL,TW,KR">
        </cmr:column>
        <cmr:column span="1" width="130">
          <label> <cmr:fieldLabel fieldId="BuyingGroupID" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
           ${summary.data.bgId}  
           ${summary.data.bgDesc != null ? " - ".concat(summary.data.bgDesc) : ""}
        </cmr:column>
        <cmr:column span="1" width="160">
          <label> <cmr:fieldLabel fieldId="GlobalBuyingGroupID" />: </label>
        </cmr:column>
        <cmr:column span="1" width="170">
           ${summary.data.gbgId} 
           ${summary.data.gbgDesc != null ? " - ".concat(summary.data.gbgDesc) : ""}
        </cmr:column>
      </cmr:row>


      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">
        </cmr:column>
        <cmr:column span="1" width="130">
          <label> <cmr:fieldLabel fieldId="CoverageID" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
           ${summary.data.covId} 
           ${summary.data.covDesc != null ? " - ".concat(summary.data.covDesc) : ""}
        </cmr:column>
        <cmr:column span="1" width="130">
          <label> <cmr:fieldLabel fieldId="BGLDERule" />: </label>
        </cmr:column>
        <cmr:column span="1" width="170">
           ${summary.data.bgRuleId}
        </cmr:column>
      </cmr:row>

      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">
        </cmr:column>
        <cmr:column span="1" width="130">
          <label> <cmr:fieldLabel fieldId="GeoLocationCode" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
           ${summary.data.geoLocationCd} 
           ${summary.data.geoLocDesc != null ? " - ".concat(summary.data.geoLocDesc) : ""}
        </cmr:column>
        <cmr:column span="1" width="160">
          <label> <cmr:fieldLabel fieldId="DUNS" />: </label>
        </cmr:column>
        <cmr:column span="1" width="170">
           ${summary.data.dunsNo}
        </cmr:column>
      </cmr:row>

      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">

        </cmr:column>
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="PPSCEID" />:</label>
        </cmr:column>
        <cmr:column span="1" width="240">
           ${summary.data.ppsceid}
        </cmr:column>
        <cmr:view exceptForCountry="758,760,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,358,359,363,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,780,706,755,862">
        <cmr:column span="1" width="130" exceptForGEO="MCO1">
          <label><cmr:fieldLabel fieldId="MembLevel" />:</label>
        </cmr:column>
        <cmr:column span="1" width="170" exceptForGEO="MCO1">
          <%
            String membLevel = DropdownListController.getDescription("MembLevel", data.getMemLvl(), cntry);
          %>
          <%=membLevel != null ? membLevel : ""%>
        </cmr:column>
        </cmr:view>
      </cmr:row>


      <cmr:row addBackground="false">
        <cmr:column span="1" width="127">

        </cmr:column>
 <%--     CMR-3869   <cmr:view exceptForGEO="MCO2" exceptForCountry="758,760,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,358,359,363,838,618,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,624,788,862">
          <cmr:column span="1" width="130">
            <label>${ui.engagesupprotreqnumber}:</label>
          </cmr:column>
            <cmr:column span="1" width="240">
             ${summary.admin.soeReqNo}
  				</cmr:column>
        </cmr:view>
        --%>
        <cmr:view exceptForCountry="758,760,603,607,626,644,651,668,693,694,695,699,704,705,707,708,740,741,787,820,821,826,889,358,359,363,620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,780,706,755,862">
        <cmr:column span="1" width="130" exceptForGEO="MCO1">
          <label><cmr:fieldLabel fieldId="BPRelationType" />:</label>
        </cmr:column>
        <cmr:column span="1" width="170" exceptForGEO="MCO1">
          <%
            String bpRelType = DropdownListController.getDescription("BPRelationType", data.getBpRelType(), cntry);
          %>
          <%=bpRelType != null ? bpRelType : ""%>
        </cmr:column>
        </cmr:view>
      </cmr:row>

      <!-- US Fields -->
      <jsp:include page="US/us_ibm_summary.jsp" />

      <!-- DE Fields -->
      <jsp:include page="DE/de_ibm_summary.jsp" />

      <!-- CND Fields -->
      <jsp:include page="CND/cnd_ibm_summary.jsp" />

      <!-- AP Fields -->
      <jsp:include page="AP/ap_ibm_summary.jsp" />

      <!-- FR Fields -->
      <jsp:include page="FR/fr_ibm_summary.jsp" />

      <!-- JP Fields -->
      <jsp:include page="JP/jp_ibm_summary.jsp" />

      <!-- MCO Fields -->
      <jsp:include page="MCO/mco_ibm_summary.jsp" />

      <!-- CEMEA Fields -->
      <jsp:include page="CEMEA/cemea_ibm_summary.jsp" />

      <!-- NORDX Fields -->
      <jsp:include page="NORDX/nordx_ibm_summary.jsp" />

      <!-- BELUX Fields -->
      <jsp:include page="BELUX/belux_ibm_summary.jsp" />
      
      <!-- NL Fields -->
      <jsp:include page="NL/nl_ibm_summary.jsp" />
      
      <!-- CN Fields -->
	  <jsp:include page="CN/cn_ibm_summary.jsp" />
	  
	  <!-- TW Fields -->
	  <jsp:include page="TW/tw_ibm_summary.jsp" />
	  
	  <!-- KR Fields -->
	  <jsp:include page="KR/kr_ibm_summary.jsp" />
	  

	  
      <cmr:view forCountry="631">
        <cmr:row addBackground="true" topPad="10">
          <cmr:column span="1">
            <label>${ui.title.contactInfoDetails}:</label>
          </cmr:column>
        </cmr:row>
        <cmr:row addBackground="true" topPad="10">
          <cmr:column span="5" forCountry="631" width="900">
            <cmr:grid url="/summary/addlcontacts.json" id="summaryCreateContactGrid" span="6" width="900" height="200" innerWidth="900"
              usePaging="false" useFilter="false">
              <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />
              <cmr:gridParam fieldId="issuingCntry" value="${summary.data.cmrIssuingCntry}" />
              <cmr:gridCol width="100px" field="contactType" header="${ui.grid.contactTypeHeader}" />
              <cmr:gridCol width="200px" field="contactSeqNum" header="${ui.grid.contactSequenceHeader}" />
              <cmr:gridCol width="200px" field="contactName" header="${ui.grid.contactNameHeader}" />
              <cmr:gridCol width="200px" field="contactEmail" header="${ui.grid.contactEmailHeader}" />
              <cmr:gridCol width="200px" field="contactPhone" header="${ui.grid.contactPhoneHeader}" />
            </cmr:grid>
          </cmr:column>
        </cmr:row>
      </cmr:view>

    </cmr:form>
  </div>
  <cmr:windowClose>
    <cmr:button label="${ui.btn.refresh}" onClick="window.location.reload()" pad="true" highlight="true" />
  </cmr:windowClose>
</cmr:window>