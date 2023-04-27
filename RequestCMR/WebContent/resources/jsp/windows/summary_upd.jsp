<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.entity.Admin"%>
<%@page import="com.ibm.cio.cmr.request.model.window.RequestSummaryModel"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script>
  function sapNumberFormatter(value, rowIndex) {
    var rowData = this.grid.getItem(rowIndex)
    if (value == '[new]') {
      return '<span style="color:green;font-weight:bold">New Address</span>';
    } else if (value == '[removed]') {
      return '<span style="color:red;font-weight:bold">Removed</span>';
    } else {
      return value;
    }
  }
  
  
  function licenseFormatter(value, rowIndex) {
    if (value == 'N') {
      return '<span style="color:green;font-weight:bold">New License</span>';
    } else {
      return '';
    }
  }

  function addressTypeComparator(a1, b1) {
    var a = a1 == 'Sold To' ? 'ZS01' : (a1 == 'Install At' ? 'ZI01' : (a1 == 'Bill To' ? 'ZP01' : (a1 == 'Ship To' ? 'ZD01' : a1)));
    var b = b1 == 'Sold To' ? 'ZS01' : (b1 == 'Install At' ? 'ZI01' : (b1 == 'Bill To' ? 'ZP01' : (b1 == 'Ship To' ? 'ZD01' : b1)));
    var val1 = a == 'ZS01' ? 1 : (a == 'ZI01' ? 2 : (a == 'ZP01' ? 3 : (a == 'ZD01' ? 4 : 5)));
    var val2 = b == 'ZS01' ? 1 : (b == 'ZI01' ? 2 : (b == 'ZP01' ? 3 : (b == 'ZD01' ? 4 : 5)));
    return val1
  }
</script>
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
    <cmr:form method="GET" action="${contextPath}/window/summary/update" name="frmCMR" class="ibm-column-form ibm-styled-form"
      modelAttribute="summary">
      <!--  Main Details Section -->
      <jsp:include page="summary_main.jsp" />

      <!--  Customer Name Handling -->
      <cmr:view exceptForGEO="EMEA,IERP,CND"  exceptForCountry="706,864,838,848">
        <c:if test="${summary.admin.mainCustNm1 != summary.admin.oldCustNm1}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              <label>${ui.customerName} 1:</label>
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.originalCMRData}:" />  ${summary.admin.oldCustNm1}
              </cmr:column>
          </cmr:row>
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              &nbsp;
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.changeToData}:" /> ${summary.admin.mainCustNm1}
              </cmr:column>
          </cmr:row>
        </c:if>
        <c:if test="${summary.admin.mainCustNm1 == summary.admin.oldCustNm1}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              <label>${ui.customerName} 1:</label>
            </cmr:column>
            <cmr:column span="4">
              No changes
            </cmr:column>
          </cmr:row>
        </c:if>

        <c:if test="${summary.admin.mainCustNm2 != summary.admin.oldCustNm2}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              <label>${ui.customerName} 2:</label>
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.originalCMRData}:" />  ${summary.admin.oldCustNm2}
              </cmr:column>
          </cmr:row>
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              &nbsp;
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.changeToData}:" /> ${summary.admin.mainCustNm2}
              </cmr:column>
          </cmr:row>
        </c:if>
        <c:if test="${summary.admin.mainCustNm2 == summary.admin.oldCustNm2}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              <label>${ui.customerName} 2:</label>
            </cmr:column>
            <cmr:column span="4">
              No changes
            </cmr:column>
          </cmr:row>
        </c:if>
      </cmr:view>
      
      <cmr:view forCountry="848">
        <c:if test="${summary.admin.mainCustNm1 != summary.admin.oldCustNm1}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              <label>Customer legal name:</label>
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.originalCMRData}:" />  ${summary.admin.oldCustNm1}
              </cmr:column>
          </cmr:row>
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              &nbsp;
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.changeToData}:" /> ${summary.admin.mainCustNm1}
              </cmr:column>
          </cmr:row>
        </c:if>
        <c:if test="${summary.admin.mainCustNm1 == summary.admin.oldCustNm1}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130" forCountry="848">
              <label>Customer legal name:</label>
            </cmr:column>
            <cmr:column span="4">
              No changes
            </cmr:column>
          </cmr:row>
        </c:if>

        <c:if test="${summary.admin.mainCustNm2 != summary.admin.oldCustNm2}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              <label>Legal name continued:</label>
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.originalCMRData}:" />  ${summary.admin.oldCustNm2}
              </cmr:column>
          </cmr:row>
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              &nbsp;
            </cmr:column>
            <cmr:column span="2">
              <cmr:note text="${ui.grid.changeToData}:" /> ${summary.admin.mainCustNm2}
              </cmr:column>
          </cmr:row>
        </c:if>
        <c:if test="${summary.admin.mainCustNm2 == summary.admin.oldCustNm2}">
          <cmr:row addBackground="true" topPad="10">
            <cmr:column span="1" width="130">
              <label>Legal name continued:</label>
            </cmr:column>
            <cmr:column span="4">
              No changes
            </cmr:column>
          </cmr:row>
        </c:if>
      </cmr:view>
      <cmr:view forGEO="IERP,CND">
        <cmr:row addBackground="true">
          <cmr:column span="1" width="127">
            <label>${ui.requestingLOB}</label>
          </cmr:column>
          <cmr:column span="3" width="460">
            ${summary.admin.requestingLob}
          </cmr:column>
        </cmr:row>
      </cmr:view>

      <!--  Address Handling -->
      <cmr:row addBackground="false" topPad="10">
        <cmr:column span="1" width="130">
          <label>${ui.nameAddress}:</label>
        </cmr:column>
        <cmr:column span="5" width="750">
          <cmr:grid url="/updated/nameaddr.json" id="summaryUpdateNameAddrGrid" span="6" width="750" height="300" innerWidth="750">
            <cmr:gridCol width="90px" field="addrType" header="${ui.grid.addrType}">
              <cmr:comparator functionName="addressTypeComparator" />
            </cmr:gridCol>
            <cmr:gridCol width="100px" field="sapNumber" header="${ui.grid.sapNo}">
              <cmr:formatter functionName="sapNumberFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="150px" field="dataField" header="${ui.grid.dataField}" />
            <cmr:gridCol width="205px" field="oldData" header="${ui.grid.originalCMRData}" />
            <cmr:gridCol width="205px" field="newData" header="${ui.grid.changeToData}" />
            <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="true">
        <cmr:column span="1" width="130">
          <label>${ui.customerInfo}:</label>
        </cmr:column>
        <cmr:column span="5" width="750">
          <cmr:grid url="/updated/cust.json" id="summaryUpdateCustGrid" span="6" width="750" height="200" innerWidth="750">
            <cmr:gridCol width="200px" field="dataField" header="${ui.grid.dataField}" />
            <cmr:gridCol width="275px" field="oldData" header="${ui.grid.originalCMRData}" />
            <cmr:gridCol width="275px" field="newData" header="${ui.grid.changeToData}" />
            <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="false" topPad="10">
        <cmr:column span="1" width="130">
          <label>${ui.ibmInfo}:</label>
        </cmr:column>
        <cmr:column span="5" width="750">
          <cmr:grid url="/updated/ibm.json" id="summaryUpdateIBMGrid" span="6" width="750" height="200" innerWidth="750">
            <cmr:gridCol width="200px" field="dataField" header="${ui.grid.dataField}" />
            <cmr:gridCol width="275px" field="oldData" header="${ui.grid.originalCMRData}" />
            <cmr:gridCol width="275px" field="newData" header="${ui.grid.changeToData}" />
            <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      
      <%-- 
      <cmr:view forCountry="631">
        <cmr:row addBackground="true" topPad="10">
           <cmr:column span="1" width="130">
            <label>${ui.title.contactInfoDetails}:</label>
          </cmr:column>
          <cmr:column span="5" width="750">
            <cmr:grid url="/summary/addlcontacts.json" id="summaryUpdateContactGrid" span="6" width="750" height="200" innerWidth="750" usePaging="false" useFilter="false">
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
      --%>
      
      <cmr:view forCountry="631,613,629,655,661,663,681,683,731,735,781,799,811,813,815,829,869,871">
      <cmr:row addBackground="true" topPad="10">
           <cmr:column span="1" width="130">
            <label>${ui.title.contactInfoDetails}:</label>
          </cmr:column>
          <cmr:column span="5" width="750">
            	<cmr:grid url="/summary/origAddlcontacts.json" id="summaryUpdateOrigContactGrid" span="6" width="750" height="200" innerWidth="750" usePaging="false" useFilter="false">
	              <cmr:gridParam fieldId="cmr" value="${summary.data.cmrNo}" />
	              <cmr:gridParam fieldId="issuingCntry" value="${summary.data.cmrIssuingCntry}" />	    
	              <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />    
	              <cmr:gridCol width="75px" field="removed" header="Action taken" />      
	              <cmr:gridCol width="100px" field="contactType" header="${ui.grid.contactTypeHeader}" />
	              <cmr:gridCol width="200px" field="contactSeqNum" header="${ui.grid.contactSequenceHeader}" />
	              <cmr:gridCol width="200px" field="contactName" header="${ui.grid.contactNameHeader}" />
	              <cmr:gridCol width="200px" field="contactEmail" header="${ui.grid.contactEmailHeader}" />
	              <cmr:gridCol width="200px" field="contactPhone" header="${ui.grid.contactPhoneHeader}" />
	            </cmr:grid>
          	<cmr:row>
          	&nbsp;
          	</cmr:row>
          	<%-- 
          	<cmr:row>
          		Current Contact Detail Values
          	</cmr:row>
            	<cmr:grid url="/summary/addlcontacts.json" id="summaryUpdateCurrContactGrid" span="6" width="750" height="200" innerWidth="750" usePaging="false" useFilter="false">
	              <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />
	              <cmr:gridParam fieldId="issuingCntry" value="${summary.data.cmrIssuingCntry}" />
	              <cmr:gridCol width="100px" field="contactType" header="${ui.grid.contactTypeHeader}" />
	              <cmr:gridCol width="200px" field="contactSeqNum" header="${ui.grid.contactSequenceHeader}" />
	              <cmr:gridCol width="200px" field="contactName" header="${ui.grid.contactNameHeader}" />
	              <cmr:gridCol width="200px" field="contactEmail" header="${ui.grid.contactEmailHeader}" />
	              <cmr:gridCol width="200px" field="contactPhone" header="${ui.grid.contactPhoneHeader}" />
	            </cmr:grid>
          	--%>
          </cmr:column>
        </cmr:row>
      </cmr:view>
	<cmr:view forCountry="631,613,629,655,661,663,681,683,731,735,781,799,811,813,815,829,869,871">
      <cmr:row addBackground="false" topPad="10">
           <cmr:column span="1" width="130">
            <label>Tax Info:</label>
          </cmr:column>
          <cmr:column span="5" width="750">
          	<cmr:row>
          	&nbsp;
          	</cmr:row>
          	<cmr:row>
          		Current Tax Info Values
          	</cmr:row>
            	<cmr:grid url="/summary/currtaxinfo.json" id="summaryUpdateCurrTaxInfoGrid" span="6" width="750" height="200" innerWidth="750" usePaging="false" useFilter="false">
	              <cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />
	              <cmr:gridParam fieldId="issuingCntry" value="${summary.data.cmrIssuingCntry}" />
	              <cmr:gridCol width="90px" field="taxCd" header="${ui.grid.laTaxCd}" />
			      <cmr:view exceptForCountry="631">
			      <cmr:gridCol width="90px" field="taxNum" header="${ui.grid.taxNumber}" />
				  </cmr:view>
			      <cmr:gridCol width="90px" field="taxSeparationIndc" header="${ui.grid.taxSepInd}"/>
			      <cmr:gridCol width="90px" field="billingPrintIndc" header="${ui.grid.billingPrintInd}" />
			      <cmr:gridCol width="90px" field="contractPrintIndc" header="${ui.grid.contractPrintInd}" />
			      <cmr:gridCol width="250px" field="cntryUse" header="${ui.grid.cntryUse}" />
	            </cmr:grid>
          </cmr:column>
        </cmr:row>
      </cmr:view>

			<cmr:view forCountry="754">
				<cmr:row addBackground="false" topPad="10">
					<cmr:column span="1" width="130">
						<label>License Details:</label>
					</cmr:column>
					<cmr:column span="5" width="750">
						<cmr:grid url="/summary/newlicenses.json"
							id="summaryNewLicenseGrid" span="6" width="750" height="200"
							innerWidth="750" usePaging="false" useFilter="false">
							<cmr:gridParam fieldId="reqId" value="${summary.admin.id.reqId}" />
							<cmr:gridCol width="200px" field="licenseNum"
								header="${ui.grid.licenseNumber}" />
							<cmr:gridCol width="200px" field="validFrom"
								header="${ui.grid.validDateFrom}" />
							<cmr:gridCol width="200px" field="validTo"
								header="${ui.grid.validDateTo}" />
							<cmr:gridCol width="150px" field="currentIndc"
								header="${ui.grid.change}">
								<cmr:formatter functionName="licenseFormatter" />
							</cmr:gridCol>
						</cmr:grid>
					</cmr:column>
				</cmr:row>
			</cmr:view>

		</cmr:form>
  </div>
  <cmr:windowClose>
    <cmr:button label="${ui.btn.refresh}" onClick="window.location = window.location.href" pad="true" highlight="true" />
  </cmr:windowClose>
</cmr:window>