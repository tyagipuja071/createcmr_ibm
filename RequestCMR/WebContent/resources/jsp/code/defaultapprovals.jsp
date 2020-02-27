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
  
  function descFormatter(value, rowIndex){
    var rowData = this.grid.getItem(rowIndex);    
    var id = rowData.defaultApprovalId;
    return '<a href="javascript: openDetails('+id+')">'+value+'</a>';
  }

  function countriesFormatter(value, rowIndex){
    var rowData = this.grid.getItem(rowIndex);    
    var countries = value;
    if (!value){
      return '';
    }
    countries = countries.replace(/,/gi,', ');
    return '<span style="word-wrap:break-word">'+countries+'</span>';
  }

  function typeFormatter(value, rowIndex){
    var rowData = this.grid.getItem(rowIndex);    
    var id = rowData.geoCd;
    return value + ' ('+id+')';
  }
  
  function openDetails(id){
    goToUrl(cmr.CONTEXT_ROOT+'/code/defaultapprdetails?defaultApprovalId='+id);
  }
  
  function newDefaultApproval(){
    goToUrl(cmr.CONTEXT_ROOT+'/code/defaultapprdetails');
  }
  
  function removeDefaultApprovals(){
    var msg = 'Remove selected record(s)?';
    FormManager.gridAction('frmCMR', 'MASS_DELETE', msg);
  }

</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/defaultappr/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="appr" id="frmCMR">
    <cmr:model model="appr" />
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row>
        <cmr:column span="6">
          <h3>Default Approvals</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/defaultapprlist.json" id="defaultApprGrid" span="6" height="400" hasCheckbox="true" checkBoxKeys="defaultApprovalId" usePaging="true" useFilter="true" >
            <cmr:gridCol width="50px" field="defaultApprovalId" header="ID" />
            <cmr:gridCol width="150px" field="requestTyp" header="Request Type" />
            <cmr:gridCol width="auto" field="defaultApprovalDesc" header="Description" >
              <cmr:formatter functionName="descFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="180px" field="lastUpdtBy" header="Countries" >
              <cmr:formatter functionName="countriesFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="200px" field="typDesc" header="Approval Type" >
              <cmr:formatter functionName="typeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="100px" field="createBy" header="Created By" />
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
    <cmr:button label="New Default Approval" onClick="newDefaultApproval()" highlight="true" />
    <cmr:button label="Remove Selected" onClick="removeDefaultApprovals()" pad="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
