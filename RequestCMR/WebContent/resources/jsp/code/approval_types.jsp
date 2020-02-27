<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FormManager.ready();
  });
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/code';
  }  
  
  function grpApprovalFormatter(value, rowIndex){
    return value == 'Y' ? 'Yes' : 'No';
  }

  function titleFormatter(value, rowIndex){
    var rowData = this.grid.getItem(rowIndex);    
    var id = rowData.typId;
    var geo = rowData.geoCd;
    return '<a href="'+cmr.CONTEXT_ROOT+'/code/approval_type_details?typId='+id+'&geoCd='+geo+'">'+value+'</a>';
  }

  function openDetails(id){
    goToUrl(cmr.CONTEXT_ROOT+'/code/approval_type_details?typId='+id);
  }
  
  function newApprovalType(){
    goToUrl(cmr.CONTEXT_ROOT+'/code/approval_type_details');
  }
  
  function removeDefaultApprovals(){
    var msg = 'Remove selected record(s)?';
    FormManager.gridAction('frmCMR', 'MASS_DELETE', msg);
  }

</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/approval_types" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="typ" id="frmCMR">
    <cmr:model model="typ" />
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row>
        <cmr:column span="6">
          <h3>Approval Types</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/approval_types_list.json" id="approvalTypesGrid" span="6" height="400" hasCheckbox="false" checkBoxKeys="typId,geoCd" usePaging="true" useFilter="true" >
            <cmr:gridCol width="50px" field="typId" header="ID" />
            <cmr:gridCol width="70px" field="geoCd" header="GEO" />
            <cmr:gridCol width="180px" field="title" header="Type/Mail Subject" >
              <cmr:formatter functionName="titleFormatter"/>
            </cmr:gridCol>
            <cmr:gridCol width="auto" field="description" header="Mail Content" />
            <cmr:gridCol width="120px" field="templateName" header="Template Name" />
            <cmr:gridCol width="100px" field="grpApprovalIndc" header="Group Approval" >
              <cmr:formatter functionName="grpApprovalFormatter"/>
            </cmr:gridCol>
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
    <cmr:button label="New Approval Type" onClick="newApprovalType()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
