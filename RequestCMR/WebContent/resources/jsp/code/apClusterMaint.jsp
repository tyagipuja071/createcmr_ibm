<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.ApClusterModel"%>
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
    cmr.hideNode('add_ap_cluster');
    dojo.connect(FormManager.getField('issuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/apClusterList.json';
      CmrGrid.refresh('apClusterUrlsGrid', url, 'issuingCntry=:issuingCntry');
      var result = cmr.query('CHECK_AP_CLUSTER_RECORDS', {
        ISSUING_CNTRY : FormManager.getActualValue('issuingCntry')
      });

      if (result != null && result.ret1 == '1') {
        cmr.hideNode('add_ap_cluster');
      } else {
        cmr.showNode('add_ap_cluster');
      }
    });
    FormManager.ready();
    FilteringDropdown.loadItems('issuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
    console.log(
<%=issuingCntry%>
  );
  });

  var APClusterService = (function() {
    return {
      addApCluster : function() {
        var country = FormManager.getActualValue('issuingCntry');
        if (country == null || country == '') {
          cmr.showAlert("Please choose the issuing country from the dropdown before proceeding to add new records");
          return;
        }
        window.location = cmr.CONTEXT_ROOT + '/code/apClusterMaint?issuingCntry=' + country;
      },
      linkFormatter : function(value, rowIndex) {
        var id = this.grid.getItem(rowIndex).issuingCntry[0];
        var clusterId = this.grid.getItem(rowIndex).apCustClusterId[0];

        if (id == '' && !
<%=admin%>
  ) {
          return value;
        }
        return '<a href="javascript: APClusterService.open(\'' + id + '\',\''+clusterId+'\')">' + value + '</a>';
      },
      open : function(cntry,clusterId) {
        window.location = cmr.CONTEXT_ROOT + '/code/apClusterMaint?issuingCntry=' + encodeURIComponent(cntry)+'&apCustClusterId='+clusterId;
      },
      removeSelectedMappings : function() {
        FormManager.gridHiddenAction('frmCMR', 'REMOVE_MAPPINGS', cmr.CONTEXT_ROOT + '/code/apCluster/process.json', true, refreshAfterMappingsRemove, false, 'Remove selected record(s)?');
      }
    };
  })();
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/code';
  }
  function refreshAfterMappingsRemove(result) {
    if (result.success) {
      CmrGrid.refresh('apClusterUrlsGrid');
    }
  }
</script>
<cmr:boxContent>
	<cmr:tabs />

	<form:form method="POST" action="${contextPath}/code/apCluster"
		name="frmCMRLov" class="ibm-column-form ibm-styled-form"
		modelAttribute="apClusterModel" id="frmCMR">

		<cmr:model model="apClusterModel" />
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
					<h3>AP Customer Cluster Tier Mapping</h3>
				</cmr:column>
			</cmr:row>
			<cmr:row topPad="10" addBackground="false">
				<cmr:column span="6">
					<cmr:grid url="/code/apClusterList.json" id="apClusterUrlsGrid" span="6"
						usePaging="true" hasCheckbox="true"
						checkBoxKeys="issuingCntry,apCustClusterId,clientTierCd,isuCode">
						<cmr:gridParam fieldId="issuingCntry" value=":issuingCntry" />
						<cmr:gridCol width="10%" field="issuingCntry"
							header="Issuing Country" align="center">
							<cmr:formatter functionName="APClusterService.linkFormatter" />
						</cmr:gridCol>
						<cmr:gridCol width="4%" field="defaultIndc" header="Default Indc"  align="center"/>
						<cmr:gridCol width="20%" field="apCustClusterId" header="AP Customer Cluster ID"  align="center"/>
						<cmr:gridCol width="30%" field="clusterDesc" header="Cluster Description"  align="center"/>
						<cmr:gridCol width="14%" field="clientTierCd" header="Client Tier"  align="center" />
						<cmr:gridCol width="14%" field="isuCode" header="ISU Code"  align="center"/>
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
		<cmr:button label="Define Mapping For Country" id="add_ap_cluster"
			onClick="APClusterService.addApCluster()" highlight="true" />
		<cmr:button label="Remove selected records"
			onClick="APClusterService.removeSelectedMappings()" highlight="true" />
		<cmr:button label="Back to Code Maintenance Home"
			onClick="backToCodeMaintHome()" />
	</cmr:buttonsRow>
</cmr:section>