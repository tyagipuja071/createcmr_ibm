 
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

var CrmITService = (function() {
  return {
    statusFormatter : function(value, rowIndex) {
      if (value == '0') {
        return '<span style="color:Red">Not Active</span>';
      } else {
        return 'Active';
      }
    },
    typeFormatter : function(value, rowIndex) {
      var id = this.grid.getItem(rowIndex).internalTyp[0];
      var id1 =  this.grid.getItem(rowIndex).cmrIssuingCntry[0];

      return '<a href="javascript: CrmITService.open(\'' + id +'\',\''+id1+ '\')">' + value + '</a>';
    },
    open : function(value,value2) {
      window.location = cmr.CONTEXT_ROOT + '/code/cmrinternaltypesmain?internalTyp=' + encodeURIComponent(value)+'&cmrIssuingCntry='+encodeURIComponent(value2);
    } 
  };
})();
function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/cmrinternaltypes"   name="frmCMRSearch" class="ibm-column-form ibm-styled-form" modelAttribute="cmrinternaltypes">
     
       
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Cmr Internal Types Table Maintenance</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/cmrinternaltypelist.json" id="cmrinternaltypelistGrid" span="6">
            <cmr:gridCol width="15%" field="internalTyp" header="Internal Type">
            <cmr:formatter functionName="CrmITService.typeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="10%" field="internalTypDesc" header="Description" />
            <cmr:gridCol width="10%" field="cmrIssuingCntry" header="CMR Issuing Country" />          
            <cmr:gridCol width="10%" field="reqTyp" header="Request Type" />
            <cmr:gridCol width="8%" field="priority" header="Priority" />
            <cmr:gridCol width="auto" field="condition" header="Condition" />
            <cmr:gridCol width="8%" field="sepValInd" header="Sep. Val. Ind." />

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
    <cmr:button label="Add Internal Type" onClick="goToUrl('${contextPath}/code/cmrinternaltypesmain')" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="cmrinternaltypes" />