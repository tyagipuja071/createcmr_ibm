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
var BPIService = (function() {
  return {
    addBPI : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/bpidetails';
    },
    searchBPI : function() {
      var bpCode = FormManager.getActualValue('bpCode');
      CmrGrid.refresh('bpintListGrid', cmr.CONTEXT_ROOT + '/bpilist.json', 'bpCode=:bpCode&nBpAbbrevNm=:nBpAbbrevNm&nBpFullNm=:nBpFullNm');
    },
    bpiActionIcons : function(value, rowIndex) {
      var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
      var rowData = this.grid.getItem(0);
      if (rowData == null) {
        return ''; // not more than 1 record
      }
      rowData = this.grid.getItem(rowIndex);
      var bpCode = rowData.bpCode[0];
      var delFlg = rowData.nLoevm[0]
      var actions = '';
      actions += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="BPIService.updateBPI(\'' + bpCode + '\')">';
      return actions;

    },
    addBPI : function(nonUS) {
      window.location = cmr.CONTEXT_ROOT + '/code/bpidetails' + (nonUS ? '?nonUS=Y' : '');
    },
    updateBPI : function(bpCode) {
      window.location = cmr.CONTEXT_ROOT + '/code/bpidetails?mandt='+mandt+'&bpCode=' + encodeURIComponent(bpCode);
    },
    bpCodeFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var bpCode = rowData.bpCode[0];
      return '<a href="javascript: BPIService.openBPI(\'' + bpCode + '\')">' + value + '</a>';
    },
    openBPI : function(bpCode) {
      window.location = cmr.CONTEXT_ROOT + '/code/bpidetails?mandt='+mandt+'&bpCode=' + encodeURIComponent(bpCode);
    },
  };

})();

</script>
<cmr:boxContent>
  <cmr:tabs />

  <cmr:form method="POST" action="${contextPath}/code/bpi/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="bpInt" id="frmCMR">
    <cmr:model model="bpInt" />
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1" width="90">
          <p>
            <label>BP_CODE: </label>
          </p>
        </cmr:column>
        <cmr:column span="1" width="170">
          <form:input path="bpCode" id="bpCode" dojoType="dijit.form.TextBox" style="width:150px"/>
        </cmr:column>
        <cmr:column span="1" width="130">
          <p>
            <label>BP_ABBREV_NM: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="150">
        <form:input path="nBpAbbrevNm" id="nBpAbbrevNm" dojoType="dijit.form.TextBox" style="width:150px"/>
        </cmr:column>
        <cmr:column span="1" width="130">
          <p>
            <label>BP_FULL_NM: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="150">
          <form:input path="nBpFullNm" id="nBpFullNm" dojoType="dijit.form.TextBox" style="width:130px"/>
        </cmr:column>
        <cmr:column span="1" width="100">
          <cmr:button label="Search" onClick="BPIService.searchBPI()" styleClass="cmr-filter"></cmr:button>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>BP CODE List</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
           <cmr:grid url="/bpilist.json" id="bpintListGrid" span="6" height="400" usePaging="false">
            <cmr:gridCol width="8%" field="bpCode" header="BP CODE" >
              <cmr:formatter functionName="BPIService.bpCodeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="18%" field="nBpAbbrevNm" header="BP ABBREV NM" />
            <cmr:gridCol width="19%" field="nBpFullNm" header="BP FULL NM" />
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
             <cmr:formatter functionName="BPIService.bpiActionIcons" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10">
      </cmr:row>
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Add US BP CODE" onClick="BPIService.addBPI()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
