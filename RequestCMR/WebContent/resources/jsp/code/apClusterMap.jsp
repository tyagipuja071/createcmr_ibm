<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  boolean admin = AppUser.getUser(request).isAdmin();
      String issuingCntry = (String) request.getParameter("cmrIssuingCntry");
%>
<script>
  dojo.addOnLoad(function() {
  dojo.connect(FormManager.getField('cmrIssuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/aplist.json';
      CmrGrid.refresh('clusterGrid', url, 'cmrIssuingCntry=:cmrIssuingCntry');
    });
    FormManager.ready();
  FilteringDropdown.loadItems('cmrIssuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');
    console.log(
<%=issuingCntry%>
  );
  }); 
  
 var ApCustClusterTierMapService = (function() {
  return {
      stateFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var issuingCntry = rowData.issuingCntry;      
      var apCustClusterId = rowData.apCustClusterId;
      var clientTierCd = rowData.clientTierCd;
      var isuCode = rowData.isuCode;

      return '<a href="javascript: ApCustClusterTierMapService.open(\'' + issuingCntry + '\', \'' + apCustClusterId + '\',\'' + clientTierCd + '\',\'' + isuCode + '\')">' + issuingCntry + '</a>';
    },
    open : function(issuingCntry,apCustClusterId,clientTierCd,isuCode) {
      document.forms['frmCMRAp'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/apClusterMapdetails/?issuingCntry=' + issuingCntry + '&apCustClusterId='+ apCustClusterId + '&clientTierCd=' + clientTierCd + '&isuCode='+isuCode);
      document.forms['frmCMRAp'].submit();    
    },
    addCluster : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/apClusterMapdetails';
    },
      removeSelectedClusters : function() {
        FormManager.gridHiddenAction('frmCMR', 'REMOVE_CLUSTERS', cmr.CONTEXT_ROOT + '/code/apClusters/process.json', true, refreshAfterClustersRemove, false, 'Remove selected record(s)?');
      }
  };
})();  


function refreshAfterClustersRemove(result) {
    if (result.success) {
      CmrGrid.refresh('clusterGrid');
    }
  }
  
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/apClusterMap" name="frmCMRAp" id="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="apClusterMap">
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
            id="cmrIssuingCntry" searchAttr="name" style="display: inline-block;"
            maxHeight="200" required="false" path="cmrIssuingCntry"
            placeHolder="CMR Issuing Country">
          </form:select>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>AP Cluster Tier Mapping</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/aplist.json" id="clusterGrid" span="6" useFilter="true" hasCheckbox="true" checkBoxKeys="issuingCntry,apCustClusterId,clientTierCd,isuCode">
            <cmr:gridCol width="5%" field="issuingCntry" header="Issuing Cntry">
            <cmr:formatter functionName="ApCustClusterTierMapService.stateFormatter" />
            </cmr:gridCol>  
            <cmr:gridCol width="13%" field="apCustClusterId" header="AP Cust Cluster Id" />
            <cmr:gridCol width="28%" field="clientTierCd" header="Client Tier Code"/>
            <cmr:gridCol width="13%" field="clusterDesc" header="Cluster Description" />
            <cmr:gridCol width="13%" field="isuCode" header="ISU Code" />
          </cmr:grid>
       </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  <cmr:model model="apClusterMap" />
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Add Cluster" onClick="ApCustClusterTierMapService.addCluster()" highlight="true" />
    <cmr:button label="Remove selected records"
			onClick="ApCustClusterTierMapService.removeSelectedClusters()" highlight="true"
			pad="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
