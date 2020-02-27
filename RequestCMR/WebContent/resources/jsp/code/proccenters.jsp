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

var ProcCenterService = (function() {
  return {
    countryFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var cmrIssuingCntry = rowData.cmrIssuingCntry;

      return '<a href="javascript: ProcCenterService.open(\'' + cmrIssuingCntry + '\')">' + value + '</a>';
    },
    open : function(cmrIssuingCntry) {
      document.forms['frmCMRProcCenter'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/proccentermain/?cmrIssuingCntry=' + cmrIssuingCntry);
      document.forms['frmCMRProcCenter'].submit();
    },
    addProcCenter : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/proccentermain';
    }
  };
})();
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>

<cmr:boxContent>
  <cmr:tabs />
  <form:form method="POST" action="${contextPath}/code/proccenters" name="frmCMRProcCenter" class="ibm-column-form ibm-styled-form" modelAttribute="proccenters">
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Processing Center Table Maintenance</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/proccenterlist.json" id="proccenterlistGrid" span="6" useFilter="true">
            <cmr:gridCol width="25%" field="country" header="CMR Issuing Country" > 
              <cmr:formatter functionName="ProcCenterService.countryFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="25%" field="procCenterNm" header="Processing Center Name" />
            <cmr:gridCol width="auto" field="cmt" header="Comment" />
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
    <cmr:button label="Add Processing Center" onClick="ProcCenterService.addProcCenter()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />    
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="proccenters" />