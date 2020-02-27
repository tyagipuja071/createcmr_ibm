 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script>
  dojo.addOnLoad(function() {
  FormManager.ready();
  });
    
  var StatusActService = (function() {
    return {
      statusActFormatter : function(value, rowIndex) {
        var rowData = this.grid.getItem(rowIndex);
        var action = rowData.action;
        
        return '<a href="javascript: StatusActService.open(\'' + action + '\')">' + action + '</a>';
      },
      open : function(action) {
        document.forms['frmCMRRoles'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/addStatusAct/?modelAction=' + action);
        document.forms['frmCMRRoles'].submit();  
      },
      addStatusAct : function() {
        window.location = cmr.CONTEXT_ROOT + '/code/addStatusAct';
      }
    };
  })();
  
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/statusAct" name="frmCMRRoles" class="ibm-column-form ibm-styled-form" modelAttribute="statusActModel">   
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Status-Action</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/statusActList.json" id="statusActListId" span="6">
            <cmr:gridCol width="10%" field="action" header="Action" >
             <cmr:formatter functionName="StatusActService.statusActFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="8%" field="cmrIssuingCntryDesc" header="CMR Issuing Country" />          
            <cmr:gridCol width="15%" field="actionDesc" header="Action Description" />
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
    <cmr:button label="Add" onClick="StatusActService.addStatusAct()" highlight="true" />
    <cmr:button label="Back to Code Maintenance" onClick="window.location = '${contextPath}/code'" pad="true" /> 
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="statusActModel" />