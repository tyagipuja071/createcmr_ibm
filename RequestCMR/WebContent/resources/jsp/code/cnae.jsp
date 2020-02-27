<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}"
	type="text/javascript"></script>
<style>
.cmr-filter {
	height: 20px;
	font-size: 12px;
	padding-top: 3px !important;
}
</style>
<script>
  dojo.addOnLoad(function() {
    CmrGrid.refresh('cnaeGrid', cmr.CONTEXT_ROOT + '/cnae.json', 'cnaeNo= ');
  });

  function searchCnae() {
    CmrGrid.refresh('cnaeGrid', cmr.CONTEXT_ROOT + '/cnae.json', 'cnaeNo=:cnaeNo');
  }

  function cnaeFormatter(value, rowIndex) {
    var rowData = this.grid.getItem(rowIndex);
    var cnaeNo = rowData.cnaeNo;
    return '<a href="javascript: openCnae(\'' + cnaeNo + '\')">' + value + '</a>';
  }

  function openCnae(cnaeNo) {
    window.location = cmr.CONTEXT_ROOT + '/code/cnaedetails?cnaeNo=' + encodeURIComponent(cnaeNo);
  }
  
  function addCnae() {
      window.location = cmr.CONTEXT_ROOT + '/code/cnaedetails';
    }
</script>
<cmr:boxContent>
	<cmr:tabs />

	<form:form method="POST" action="javascript: searchCnae()"
		name="frmCMR" class="ibm-column-form ibm-styled-form"
		modelAttribute="cnae" id="frmCMR">
		<cmr:model model="cnae" />
		<cmr:modelAction formName="frmCMR" />
		<cmr:section>
			<cmr:row topPad="8" addBackground="true">
				<cmr:column span="1" width="100">
					<p>
						<label>CNAE No.: </label>
					</p>
				</cmr:column>
				<cmr:column span="2" width="150">
					<form:input path="cnaeNo" id="cnaeNo" dojoType="dijit.form.TextBox"
						style="width:130px" />
				</cmr:column>
				<cmr:column span="1" width="100">
					<cmr:button label="Search" onClick="searchCnae()"
						styleClass="cmr-filter"></cmr:button>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="8">
				<cmr:column span="6">
					<h3>CNAE Entries:</h3>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="10" addBackground="false">
				<cmr:column span="6">
					<cmr:grid url="/cnae.json" id="cnaeGrid" span="6" height="400"
						usePaging="true">
						<cmr:gridParam fieldId="cnaeNo" value=":cnaeNo" />
						<cmr:gridCol width="100px" field="cnaeNo" header="CNAE No.">
							<cmr:formatter functionName="cnaeFormatter" />
						</cmr:gridCol>
						<cmr:gridCol width="500px" field="cnaeDescrip"
							header="CNAE DESCRIPTION" />
						<cmr:gridCol width="100px" field="isicCd" header="ISIC CODE" />
						<cmr:gridCol width="100px" field="isuCd" header="ISU CODE" />
						<cmr:gridCol width="100px" field="subIndustryCd"
							header="SUB INDUSTRY CODE" />
					</cmr:grid>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="10">
			</cmr:row>
		</cmr:section>
	</form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
	<cmr:buttonsRow>
		<cmr:button label="Add CNAE Info" onClick="addCnae()" highlight="true" />
		<cmr:button label="Back to Code Maintenance Home"
			onClick="backToCodeMaintHome()" pad="true" />
	</cmr:buttonsRow>
</cmr:section>
