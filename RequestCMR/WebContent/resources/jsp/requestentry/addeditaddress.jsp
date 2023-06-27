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

<script src="https://unpkg.com/read-excel-file@5.x/bundle/read-excel-file.min.js">
</script>
<style>
#addEditAddressModal div.ibm-columns {
  width: 730px !important;
}
</style>
<!--  Modal for the Add/Edit Screen -->
<cmr:modal title="${ui.title.addEditAddress}" id="addEditAddressModal" widthId="750">
  <cmr:form method="GET" action="${contextPath}/request/address/process" name="frmCMR_addressModal" class="ibm-column-form ibm-styled-form"
    modelAttribute="addressModal" id="frmCMR_addressModal">
    <cmr:modelAction formName="frmCMR_addressModal" />
    <form:hidden path="cmrIssuingCntry" id="addr_cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
    <form:hidden path="reqId" id="addr_reqId" value="${reqentry.reqId}" />
    <form:hidden path="addrSeq" id="addrSeq" />
    <form:hidden path="importInd" id="importInd" />
    <form:hidden path="dplChkResult" id="addr_dplChkResult" />
    <form:hidden path="dplChkInfo" id="dplChkInfo" />
    <form:hidden path="remAddrType" id="remAddrType" />
    <form:hidden path="countyName" id="countyName" />
    <form:hidden path="stdCityNm" id="stdCityNm" />
    <form:hidden path="saveAddrType" id="saveAddrType" />

    <cmr:row topPad="10">
      <cmr:column span="4">
        <jsp:include page="../templates/messages_modal.jsp">
          <jsp:param value="addEditAddressModal" name="modalId" />
        </jsp:include>
        <span class="cmr-findcmr-record" id="cmr-findcmr-record" style="display: none">CMR Search #${rdcdata.cmrNo}</span>
        <span class="cmr-dnb-record" id="cmr-dnb-record" style="display: none">D&B Search</span>
        <input type="button" style="opacity: 0; position: absolute; right: 0; top: 0">
        <!-- hack to focus on top, bad bad -->
      </cmr:column>
    </cmr:row>

   <!-- Different Views Per Geo --> 
    <cmr:view forGEO="US">
      <jsp:include page="US/us_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="LA">
      <jsp:include page="LA/la_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="EMEA">
      <jsp:include page="EMEA/emea_address.jsp"></jsp:include>
    </cmr:view>
     <cmr:view forGEO="CND">
      <jsp:include page="CND/cnd_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="IERP">
      <jsp:include page="DE/de_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="CN">
      <jsp:include page="CN/cn_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="AP">
      <jsp:include page="AP/ap_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="MCO,MCO1,MCO2">
      <jsp:include page="MCO/mco_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="FR">
      <jsp:include page="FR/fr_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="JP">
      <jsp:include page="JP/jp_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forGEO="CEMEA">
      <jsp:include page="CEMEA/cemea_address.jsp"></jsp:include>
    </cmr:view>
     <cmr:view forGEO="NORDX">
      <jsp:include page="NORDX/nordx_address.jsp"></jsp:include>
    </cmr:view>  
     <cmr:view forGEO="BELUX">
      <jsp:include page="BELUX/belux_address.jsp"></jsp:include>
    </cmr:view>
     <cmr:view forGEO="NL">
      <jsp:include page="NL/nl_address.jsp"></jsp:include>
    </cmr:view>     
    <cmr:view forGEO="SWISS">
      <jsp:include page="SWISS/ch_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forCountry="649">
      <jsp:include page="CA/ca_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forCountry="858">
      <jsp:include page="TW/tw_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view forCountry="766">
      <jsp:include page="KR/kr_address.jsp"></jsp:include>
    </cmr:view>
    <cmr:view exceptForGEO="US,LA,EMEA,CND,IERP,AP,MCO,MCO1,MCO2,FR,CEMEA,JP,CN,NORDX,BELUX,NL,SWISS" exceptForCountry="649,858,766">
      <jsp:include page="default_address.jsp"></jsp:include>
    </cmr:view>
    
    <cmr:row>
      <cmr:column span="1" width="200">
        <p>
          <label for="dplChkResultEdit">${ui.dplChkResult}:</label>
        </p>
      </cmr:column>
      <cmr:column span="3">
        <div id="dplChkResultEdit">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="200">
        <p>
          <label for="dplChkTsEdit">${ui.dpl.Timestamp} (<%=SystemConfiguration.getValue("DATE_TIMEZONE", "GMT")%>):</label>
        </p>
      </cmr:column>
      <cmr:column span="3">
        <div id="dplChkTsEdit">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="200">
        <p>
          <label for="dplChkByEdit">${ui.dpl.CheckBy}:</label>
        </p>
      </cmr:column>
      <cmr:column span="3">
        <div id="dplChkByEdit">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="200">
        <p>
          <label for="dplChkErrEdit">${ui.dpl.DenialCode}:</label>
        </p>
      </cmr:column>
      <cmr:column span="3">
        <div id="dplChkErrEdit">-</div>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="200">
        <p>
          <label for="dplChkInfoEdit">${ui.dplChkInfo}:</label>
        </p>
      </cmr:column>
      <cmr:column span="3">
        <div id="dplChkInfoEdit">-</div>
      </cmr:column>
    </cmr:row>

    <cmr:view forCountry="760">
    	<cmr:row>
      		<cmr:column span="1" width="200">
    			<label for="endUserFile"> ${ui.endUserFile}: </label>
    		</cmr:column>
    		<cmr:column span="3">
				<input type="file" id="endUserFile" accept=".xls" name="endUserFile">
			</cmr:column>
		</cmr:row>
    </cmr:view>
    
    <div style="display:none">
    <cmr:row addBackground="true">
      
        <cmr:column span="1" width="170">
          <p>
            <label for="addrStdResult">${ui.addrStdStat}: <cmr:info text="${ui.info.addrStdInfo}" /> </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <div id="addrStdResult_modal"></div>
          <form:hidden id="addrStdAcceptInd" path="addrStdAcceptInd" />
          <form:hidden id="addr_addrStdResult" path="addrStdResult" />
          <input type="hidden" id="tgmeCompare" name="tgmeCompare" value="">
        </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" width="170">
        &nbsp;
      </cmr:column>
      <cmr:column span="2" width="250">
        <cmr:button styleClass="addrStd" label="${ui.btn.addStd}" onClick="performAddressStandardization()" highlight="false" />
        <cmr:info text="${ui.info.addrStdBtn}" />
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" width="110">
        <p>
          <cmr:label fieldId="addrStdTsString">${ui.datePer}: </cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2" width="250">
        <div id="addrStdTsString_modal">${addressModal.addrStdTsString}</div>
        <form:hidden id="addrStdTsString" path="addrStdTsString" />
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="1" width="230">
        <p>
          <cmr:label fieldId="addrStdRejReason">${ui.addrStdRejReason}:</cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2">
        <div id="addrStdRejReason_modal">${addressModal.addrStdRejReason}</div>
        <form:hidden id="addr_addrStdRejReason" path="addrStdRejReason" />
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="4" width="710">
        <p>
          <label for="addrStdRejCmt"> ${ui.addrStdRejCmt}: <cmr:memoLimit maxLength="250" fieldId="addr_addrStdRejCmt" /> </label>
          <form:textarea size="30" id="addr_addrStdRejCmt" path="addrStdRejCmt" maxlength="250" rows="6" cols="72" style="overflow-y:scroll"
            readonly="true" />
        </p>
      </cmr:column>
    </cmr:row>
  </div>


    <cmr:buttonsRow>
      <cmr:hr />
 	 <cmr:view exceptForCountry="641">
      <cmr:button label="${ui.btn.addAddress}" onClick="doAddToAddressList()" highlight="true" pad="true" id="addressBtn" />
	 </cmr:view>
      <cmr:view forCountry="641">
      <cmr:button label="${ui.btn.addAddress}" onClick="doValidateSave()" highlight="true" pad="true" id="addressBtn" />
      </cmr:view>
      <cmr:button label="${ui.btn.cancel}" onClick="cancelAddressModal()" highlight="false" pad="true" />
      
      <cmr:view forCountry="760">
        <cmr:button label="${ui.btn.upload}" onClick="uploadFile()" highlight="true" pad="true" id="uploadBtn" />
      </cmr:view>
    
    </cmr:buttonsRow>
  </cmr:form>
</cmr:modal>