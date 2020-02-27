<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page
	import="com.ibm.cio.cmr.request.model.code.ValidateFiscalDataModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  ValidateFiscalDataModel fiscal = (ValidateFiscalDataModel) request.getAttribute("fiscal");
%>

<script>
  function loadFiscalDataModal(result) {
  
    if (result.ret1!=null && result.ret1!='') {
      qParams = {
        MANDT : cmr.MANDT,
        ZZKV_CUSNO : result.ret1
      };

      var fiscalInfo = cmr.query('GET.RDC_FISCAL_INFO_IT', qParams);
      if (fiscalInfo.ret1!=null && fiscalInfo.ret1 != '') {
        document.getElementById('newLandCntry').innerText = fiscalInfo.ret1;
      }
      if (fiscalInfo.ret2!=null && fiscalInfo.ret2 != '') {
        document.getElementById('newCity1').innerText = fiscalInfo.ret2;
      }
      if (fiscalInfo.ret3!=null && fiscalInfo.ret3 != '') {
        document.getElementById('newAddrTxt').innerText = fiscalInfo.ret3;
      }
      if (fiscalInfo.ret4!=null && fiscalInfo.ret4 != '') {
        document.getElementById('newPostCd').innerText = fiscalInfo.ret4;
      }
      if (fiscalInfo.ret5!=null && fiscalInfo.ret5 != '') {
        document.getElementById('newTaxCd1').innerText = fiscalInfo.ret5;
      }
      if (fiscalInfo.ret6!=null && fiscalInfo.ret6 != '') {
        document.getElementById('newVat').innerText = fiscalInfo.ret6;
      }
      if (fiscalInfo.ret7!=null && fiscalInfo.ret7 != '') {
        document.getElementById('newEnterprise').innerText = fiscalInfo.ret7;
      }
      if (fiscalInfo.ret8!=null && fiscalInfo.ret8 != '') {
        document.getElementById('newCmrNo').innerText = fiscalInfo.ret8;
      }
      if (fiscalInfo.ret9!=null && fiscalInfo.ret9 != '') {
        document.getElementById('newAffiliate').innerText = fiscalInfo.ret9;
      }
      if (fiscalInfo.ret10!=null && fiscalInfo.ret10 != '') {
        document.getElementById('newIsuCd').innerText = fiscalInfo.ret10;
      }
      if (fiscalInfo.ret11!=null && fiscalInfo.ret11 != '') {
        document.getElementById('newClientTier').innerText = fiscalInfo.ret11;
      }
      if (fiscalInfo.ret12!=null && fiscalInfo.ret12 != '') {
        document.getElementById('newInacCd').innerText = fiscalInfo.ret12;
      }
      if (fiscalInfo.ret13!=null && fiscalInfo.ret13 != '') {
        document.getElementById('newCustNm1').innerText = fiscalInfo.ret13;
      }
      if (fiscalInfo.ret14!=null && fiscalInfo.ret14 != '') {
        document.getElementById('newCustNm2').innerText = fiscalInfo.ret14;
      }
      qParams = {
      	RCYAA:FormManager.getActualValue('cmrIssuingCntry'),
      	RCUXA:result.ret1
      };
      var newDb2FiscalInfo = cmr.query('GET.FISCAL_INFO_IT_DB2',qParams);
      console.log(newDb2FiscalInfo);
      if(newDb2FiscalInfo.ret1!=null && newDb2FiscalInfo.ret1!=''){
      	document.getElementById('newIdentClient').innerText = newDb2FiscalInfo.ret1;
      }
      if(newDb2FiscalInfo.ret2!=null && newDb2FiscalInfo.ret2!=''){
      	document.getElementById('newSalesBusOffCd').innerText = newDb2FiscalInfo.ret2;
      }
      if(newDb2FiscalInfo.ret3!=null && newDb2FiscalInfo.ret3!=''){
      	document.getElementById('newRepTeamMemberNo').innerText = newDb2FiscalInfo.ret3;
      }
	
      qParams = {
      	MANDT : cmr.MANDT,
        ZZKV_CUSNO : FormManager.getActualValue('cmrNo')
      };
      var zorgInfo = cmr.query('GET.RDC_FISCAL_INFO_IT', qParams);
      console.log(zorgInfo);
      if (zorgInfo.ret1!=null && zorgInfo.ret1 != '') {
        document.getElementById('oldLandCntry').innerText = zorgInfo.ret1;
      }
      if (zorgInfo.ret2!=null && zorgInfo.ret2 != '') {
        document.getElementById('oldCity1').innerText = zorgInfo.ret2;
      }
      if (zorgInfo.ret3!=null && zorgInfo.ret3 != '') {
        document.getElementById('oldAddrTxt').innerText = zorgInfo.ret3;
      }
      if (zorgInfo.ret4!=null && zorgInfo.ret4 != '') {
        document.getElementById('oldPostCd').innerText = zorgInfo.ret4;
      }
      if (zorgInfo.ret5!=null && zorgInfo.ret5 != '') {
        document.getElementById('oldTaxCd1').innerText = zorgInfo.ret5;
      }
      if (zorgInfo.ret6!=null && zorgInfo.ret6 != '') {
        document.getElementById('oldVat').innerText = zorgInfo.ret6;
      }
      if (zorgInfo.ret7!=null && zorgInfo.ret7 != '') {
        document.getElementById('oldEnterprise').innerText = zorgInfo.ret7;
      }
      if (zorgInfo.ret8!=null && zorgInfo.ret8 != '') {
        document.getElementById('oldCmrNo').innerText = zorgInfo.ret8;
      }
      if (zorgInfo.ret9!=null && zorgInfo.ret9 != '') {
        document.getElementById('oldAffiliate').innerText = zorgInfo.ret9;
      }
      if (zorgInfo.ret10!=null && zorgInfo.ret10 != '') {
        document.getElementById('oldIsuCd').innerText = zorgInfo.ret10;
      }
      if (zorgInfo.ret11!=null && zorgInfo.ret11 != '') {
        document.getElementById('oldClientTier').innerText = zorgInfo.ret11;
      }
      if (zorgInfo.ret12!=null && zorgInfo.ret12 != '') {
        document.getElementById('oldInacCd').innerText = zorgInfo.ret12;
      }
      if (zorgInfo.ret13!=null && zorgInfo.ret13 != '') {
        document.getElementById('oldCustName1').innerText = zorgInfo.ret13;
      }
      if (zorgInfo.ret14!=null && zorgInfo.ret14 != '') {
        document.getElementById('oldCustName2').innerText = zorgInfo.ret14;
      }
      qParams = {
      	RCYAA:FormManager.getActualValue('cmrIssuingCntry'),
      	RCUXA:FormManager.getActualValue('cmrNo')
      };
      var oldDb2FiscalInfo = cmr.query('GET.FISCAL_INFO_IT_DB2',qParams);
      if(oldDb2FiscalInfo.ret1!=null && oldDb2FiscalInfo.ret1!=''){
      	document.getElementById('oldIdentClient').innerText = oldDb2FiscalInfo.ret1;
      }
      if(oldDb2FiscalInfo.ret2!=null && oldDb2FiscalInfo.ret2!=''){
      	document.getElementById('oldSalesBusOffCd').innerText = oldDb2FiscalInfo.ret2;
      }
      if(oldDb2FiscalInfo.ret3!=null && oldDb2FiscalInfo.ret3!=''){
      	document.getElementById('oldRepTeamMemberNo').innerText = oldDb2FiscalInfo.ret3;
      }
    }
  }
</script>

<!--  Modal for the ValidateFiscalData Screen -->
<cmr:modal id="validateFiscalDataModal" title="Fiscal Data Modal"
	widthId="750">
	<form:form method="POST"
		action="${contextPath}/request/fiscalinfo/process"
		name="validateFiscalDataForm" class="ibm-column-form ibm-styled-form"
		modelAttribute="fiscal" id="validateFiscalDataForm">

		<cmr:row addBackground="false">
			<cmr:column span="2">
				<input type="radio" name="cmrNewOld" id="W" value="W">Imported CMR Details
          </cmr:column>
			<cmr:column span="2">
				<input type="radio" name="cmrNewOld" id="L" value="L">Found CMR Details
          </cmr:column>
		</cmr:row>


		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>CMRNo:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldCmrNo"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>CMRNo:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newCmrNo"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Customer Name:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldCustName1"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Customer Name:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newCustNm1"></div>
			</cmr:column>
		</cmr:row>
		
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Customer Name Continuation:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldCustName2"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Customer Name Continuation:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newCustNm2"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Landed Country:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldLandCntry"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Landed Country:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newLandCntry"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>City:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldCity1"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>City:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newCity1"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Street Address:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldAddrTxt"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Street Address:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newAddrTxt"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Postal Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldPostCd"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Postal Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newPostCd"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Fiscal Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldTaxCd1"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Fiscal Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newTaxCd1"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>VAT:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldVat"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>VAT:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newVat"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Enterprise:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldEnterprise"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Enterprise:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newEnterprise"></div>
			</cmr:column>
		</cmr:row>

		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Affiliate:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldAffiliate"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Affiliate:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newAffiliate"></div>
			</cmr:column>
		</cmr:row>
		
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>IdentClient:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldIdentClient"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>IdentClient:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newIdentClient"></div>
			</cmr:column>
		</cmr:row>
		
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>ISU Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldIsuCd"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>ISU Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newIsuCd"></div>
			</cmr:column>
		</cmr:row>
		
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Client Tier:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldClientTier"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Client Tier:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newClientTier"></div>
			</cmr:column>
		</cmr:row>
		
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>INAC/NAC Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldInacCd"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>INAC/NAC Code:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newInacCd"></div>
			</cmr:column>
		</cmr:row>
		
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>Sales Rep:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldRepTeamMemberNo"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>Sales Rep:</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newRepTeamMemberNo"></div>
			</cmr:column>
		</cmr:row>
		
		<cmr:row addBackground="true">
			<cmr:column span="1">
				<label>SBO (SORTL):</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="oldSalesBusOffCd"></div>
			</cmr:column>

			<cmr:column span="1">
				<label>SBO (SORTL):</label>
			</cmr:column>
			<cmr:column span="1">
				<div id="newSalesBusOffCd"></div>
			</cmr:column>
		</cmr:row>

		<cmr:buttonsRow>
			<cmr:hr />
			<cmr:button label="${ui.btn.save}" onClick="saveFiscalValues()"
				highlight="true" />
			<cmr:button label="${ui.btn.cancel}"
				onClick="cmr.hideModal('validateFiscalDataModal')" highlight="false"
				pad="true" />
		</cmr:buttonsRow>
	</form:form>
</cmr:modal>