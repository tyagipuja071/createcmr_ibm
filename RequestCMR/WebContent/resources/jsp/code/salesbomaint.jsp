<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.SalesBoModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  boolean admin = AppUser.getUser(request).isAdmin();
			String issuingCntry = (String) request.getParameter("issuingCntry");
%>
<script>
  dojo.addOnLoad(function() {
    dojo.connect(FormManager.getField('issuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/salesBoList.json';
      CmrGrid.refresh('sboUrlsGrid', url, 'issuingCntry=:issuingCntry');
    });
    FormManager.ready();
    FilteringDropdown.loadItems('issuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
    console.log(
<%=issuingCntry%>
  );
  });

  var SboService = (function() {
    return {
      addSalesBo : function() {
        window.location = cmr.CONTEXT_ROOT + '/code/salesBoMaint';
      },
      linkFormatter : function(value, rowIndex) {
        var id1 = this.grid.getItem(rowIndex).issuingCntry[0];
        var id2 = this.grid.getItem(rowIndex).repTeamCd[0];
        var id3 = this.grid.getItem(rowIndex).salesBoCd[0];

        if (id1 == '' && !
<%=admin%>
  ) {
          return value;
        }
        return '<a href="javascript: SboService.open(\'' + id1 + '\',\'' + id2 + '\',\'' + id3 + '\')">' + value + '</a>';
      },
      open : function(value1, value2, value3) {
        window.location = cmr.CONTEXT_ROOT + '/code/salesBoMaint?issuingCntry=' + encodeURIComponent(value1) + '&repTeamCd=' + encodeURIComponent(value2) + '&salesBoCd=' + encodeURIComponent(value3);
      },
      removeSelectedMappings : function() {
        FormManager.gridHiddenAction('frmCMR', 'REMOVE_MAPPINGS', cmr.CONTEXT_ROOT + '/code/salesBo/process.json', true, refreshAfterMappingsRemove, false, 'Remove selected record(s)?');
      }
    };
  })();
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/code';
  }
  function refreshAfterMappingsRemove(result) {
    if (result.success) {
      CmrGrid.refresh('sboUrlsGrid');
    }
  }
</script>
<cmr:boxContent>
	<cmr:tabs />

	<form:form method="POST" action="${contextPath}/code/salesBo"
		name="frmCMRLov" class="ibm-column-form ibm-styled-form"
		modelAttribute="salesBoModel" id="frmCMR">

		<cmr:model model="salesBoModel" />
		<cmr:modelAction formName="frmCMR" />
		<cmr:section>
			<cmr:row topPad="8" addBackground="true">
				<cmr:column span="2">
					<p>
						<label>CMR Issuing Country: </label>
					</p>
				</cmr:column>
				<cmr:column span="2" width="250">
					<form:select dojoType="dijit.form.FilteringSelect"
						id="issuingCntry" searchAttr="name" style="display: inline-block;"
						maxHeight="200" required="false" path="issuingCntry"
						placeHolder="CMR Issuing Country">
					</form:select>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="8">
				<cmr:column span="6">
					<h3>Sales Branch Off</h3>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="10" addBackground="false">
				<cmr:column span="6">
					<cmr:grid url="/code/salesBoList.json" id="sboUrlsGrid" span="6"
						usePaging="true" hasCheckbox="true"
						checkBoxKeys="issuingCntry,repTeamCd,salesBoCd">
						<cmr:gridParam fieldId="issuingCntry" value=":issuingCntry" />
						<cmr:gridCol width="8%" field="issuingCntry"
							header="Issuing Cntry">
							<cmr:formatter functionName="SboService.linkFormatter" />
						</cmr:gridCol>
						<cmr:gridCol width="11%" field="repTeamCd" header="Rep Team Code" />
						<cmr:gridCol width="11%" field="salesBoCd" header="Sales BO Code" />
						<cmr:gridCol width="30%" field="salesBoDesc"
							header="Sales Bo Description" />
						<cmr:gridCol width="8%" field="mrcCd" header="MRC Code" />
						<cmr:gridCol width="15%" field="clientTier" header="Client Tier" />
						<cmr:gridCol width="15%" field="isuCd" header="ISU Code" />
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
		<cmr:button label="Add Sales BO" onClick="SboService.addSalesBo()"
			highlight="true" />
		<cmr:button label="Remove selected records"
			onClick="SboService.removeSelectedMappings()" highlight="true"
			pad="true" />
		<cmr:button label="Back to Code Maintenance Home"
			onClick="backToCodeMaintHome()" pad="true" />
	</cmr:buttonsRow>
</cmr:section>