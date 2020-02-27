<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />


<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script>
  dojo.addOnLoad(function() {
  FormManager.ready();
  });

var ClaimRolesService = (function() {
  return {
    statusFormatter : function(value, rowIndex) {
      if (value == '0') {
        return '<span style="color:Red">Not Active</span>';
      } else {
        return 'Active';
      }
    },
    claimRoleFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var cmrIssuingCntry = rowData.cmrIssuingCntry;
      var internalTyp = rowData.internalTyp;      
      var reqStatus = rowData.reqStatus;

      return '<a href="javascript: ClaimRolesService.open(\'' + cmrIssuingCntry + '\', \'' + internalTyp + '\', \'' + reqStatus + '\')">' + value + '</a>';
    },
    open : function(cmrIssuingCntry, internalTyp, reqStatus) {
      document.forms['frmCMRClaimRole'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/claimrolesmain/?cmrIssuingCntry=' + cmrIssuingCntry + '&internalTyp=' + internalTyp + '&reqStatus=' + reqStatus);
      document.forms['frmCMRClaimRole'].submit();
    },
    addClaimRole : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/claimrolesmain';
    }
  };
})();
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>

<cmr:boxContent>
  <cmr:tabs />
  <form:form method="POST" action="${contextPath}/code/claimroles" name="frmCMRClaimRole" class="ibm-column-form ibm-styled-form" modelAttribute="claimroles">
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Claim Roles Table Maintenance</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/claimroleslist.json" id="claimroleslistGrid" span="6" useFilter="true">
            <cmr:gridCol width="14%" field="claimRoleDesc" header="Claim Role ID" >          
              <cmr:formatter functionName="ClaimRolesService.claimRoleFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="12%" field="claimSubRoleDesc" header="Claim Sub Role ID" />
            <cmr:gridCol width="12%" field="country" header="CMR Issuing Country" />          
            <cmr:gridCol width="12%" field="internalTypDesc" header="Internal Type" />
            <cmr:gridCol width="auto" field="reqStatusDesc" header="Request Status" />             
            <cmr:gridCol width="8%" field="status" header="Status" >
              <cmr:formatter functionName="ClaimRolesService.statusFormatter" />
            </cmr:gridCol> 
            <cmr:gridCol width="10%" field="createTsString" header="Create Date" />                      
            <cmr:gridCol width="12%" field="createBy" header="Created By" />         
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
    <cmr:button label="Add Claim Role" onClick="ClaimRolesService.addClaimRole()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />    
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="claimroles" />