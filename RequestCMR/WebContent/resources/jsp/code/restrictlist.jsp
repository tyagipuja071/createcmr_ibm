<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  String mandt = SystemConfiguration.getValue("MANDT").toString();
%>
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<style>
 .cmr-filter {
   height:20px;
   font-size: 12px;
   padding-top: 3px !important;
 }
</style>
<script>
  var mandt = '<%= mandt %>';
  dojo.addOnLoad(function(){
  });
  var RSTService = (function() {
  return {
    addRST : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/rstdetails';
    },
    searchRST : function() {
      var restrictToCd = FormManager.getActualValue('restrictToCd');
      var nRestrictToAbbrevNm = FormManager.getActualValue('nRestrictToAbbrevNm');
      var nRestrictToNm = FormManager.getActualValue('nRestrictToNm');
      CmrGrid.refresh('rstListGrid', cmr.CONTEXT_ROOT + '/rstlist.json',
          'restrictToCd=:restrictToCd&nRestrictToAbbrevNm=:nRestrictToAbbrevNm&nRestrictToNm=:nRestrictToNm');
    },
    rstActionIcons : function(value, rowIndex) {
      var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
      var rowData = this.grid.getItem(0);
      if (rowData == null) {
        return ''; // not more than 1 record
      }
      rowData = this.grid.getItem(rowIndex);
      var restrictToCd = rowData.restrictToCd[0];
      var delFlg = rowData.nLoevm[0]
      var actions = '';
      actions += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="RSTService.updateRST(\'' + restrictToCd
          + '\')">';
      return actions;
    },
    addRST : function(nonUS) {
      window.location = cmr.CONTEXT_ROOT + '/code/rstdetails' + (nonUS ? '?nonUS=Y' : '');
    },
    updateRST : function(restrictToCd) {
      window.location = cmr.CONTEXT_ROOT + '/code/rstdetails?mandt='+mandt+'&restrictToCd=' + encodeURIComponent(restrictToCd);
    },
    rstCodeFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var restrictToCd = rowData.restrictToCd[0];
      return '<a href="javascript: RSTService.openRST(\'' + restrictToCd + '\')">' + value + '</a>';
    },
    openRST : function(restrictToCd) {
      window.location = cmr.CONTEXT_ROOT + '/code/rstdetails?mandt='+mandt+'&restrictToCd=' + encodeURIComponent(restrictToCd);
    },
  };

})();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/rst/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="restrict" id="frmCMR">
    <cmr:model model="restrict" />
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1" width="100">
          <p>
            <label>Restrict Code: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="150">
          <form:input path="restrictToCd" id="restrictToCd" dojoType="dijit.form.TextBox" style="width:150px"/>
        </cmr:column>
        <cmr:column span="1" width="150">
          <p>
            <label>Abbreviated Name: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="150">
        <form:input path="nRestrictToAbbrevNm" id="nRestrictToAbbrevNm" dojoType="dijit.form.TextBox" style="width:150px"/>
        </cmr:column>
        <cmr:column span="1" width="100">
          <p>
            <label>Name: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="150">
          <form:input path="nRestrictToNm" id="nRestrictToNm" dojoType="dijit.form.TextBox" style="width:130px"/>
        </cmr:column>
        <cmr:column span="1" width="150">
          <cmr:button label="Search" onClick="RSTService.searchRST()" styleClass="cmr-filter"></cmr:button>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>RESTRICT CODE List</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
           <cmr:grid url="/rstlist.json" id="rstListGrid" span="6" height="400" usePaging="false">
            <cmr:gridCol width="8%" field="restrictToCd" header="RESTRICT CODE" >
              <cmr:formatter functionName="RSTService.rstCodeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="18%" field="nRestrictToAbbrevNm" header="RESTRICT ABBREV NM" />
            <cmr:gridCol width="19%" field="nRestrictToNm" header="RESTRICT NM" />
            <cmr:gridCol width="8%" field="createdBy" header="CREATEBY" />
            <cmr:gridCol width="8%" field="createDt" header="CREATEDT" />
            <cmr:gridCol width="8%" field="updatedBy" header="UPDATEBY" />
            <cmr:gridCol width="8%" field="updateDt" header="UPDATEDT" />
            
            <cmr:gridCol width="8%" field="nLoevm" header="LOG DEL" >
            <cmr:formatter>
                function(value) {
                  if (value == 'X'){
                     return "<span style='color:red'>Deleted</span>";
                   } else {
                     return "<span style='color:green'>Active</span>";
                   }
                }
              </cmr:formatter>
            </cmr:gridCol>
             <cmr:gridCol width="5%" field="action" header="ACTION" > 
             <cmr:formatter functionName="RSTService.rstActionIcons" />
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
    <cmr:button label="Add US RESTRICT CODE" onClick="RSTService.addRST()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
