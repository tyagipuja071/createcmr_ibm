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
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      add : function() {
        window.location = cmr.CONTEXT_ROOT + '/code/jpjsiccodemapform';
      },
      update : function(jsicCd, subIndustryCd, isuCd, isicCd, dept) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpjsiccodemapform?jsicCd=' + encodeURIComponent(jsicCd);
        loc += '&subIndustryCd=' + encodeURIComponent(subIndustryCd);
        loc += '&isuCd=' + encodeURIComponent(isuCd);
        loc += '&isicCd=' + encodeURIComponent(isicCd);
        loc += '&dept=' + encodeURIComponent(dept);
        alert('loc='+loc);
        window.location = loc;
      },
      remove : function(jsicCd, subIndustryCd, isuCd, isicCd, dept) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpjsiccodemapform?jsicCd=' + jsicCd;
        loc += '&subIndustryCd=' + encodeURIComponent(subIndustryCd);
        loc += '&isuCd=' + encodeURIComponent(isuCd);
        loc += '&isicCd=' + encodeURIComponent(isicCd);
        loc += '&dept=' + encodeURIComponent(dept);
        alert('Are you sure to remove '+loc + '?');
        window.location = loc;
      },
      actionIcons : function(value, rowIndex) {
        var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
        var rowData = this.grid.getItem(0);
        
        if (rowData == null) {
          return ''; // not more than 1 record
        }
        
        rowData = this.grid.getItem(rowIndex);
        
        var actionsTag = '';
        actionsTag += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.jsicCd + '\',\'' + rowData.subIndustryCd + '\',\'' + rowData.isuCd + '\',\'' + rowData.isicCd + '\',\'' + rowData.dept + '\')" />';
        actionsTag += '<img src="' + imgloc + 'addr-remove-icon.png" class="addr-icon" title="Remove" onclick="actions.remove(\'' + rowData.jsicCd + '\',\'' + rowData.subIndustryCd + '\',\'' + rowData.isuCd + '\',\'' + rowData.isicCd + '\',\'' + rowData.dept + '\')" />';
               
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/jp_jsic_code_map_list.json';
        CmrGrid.refresh('jpJsicCodeMapGrid', url, 'jsicCd=:jsicCd');
      }
    };
  })();
</script>

<cmr:form method="POST" action="${contextPath}/code/jpjsiccodemap" name="frmCMR" class="ibm-column-form ibm-styled-form"
   id="frmCMR">
  
  
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>JP JSIC CODE MAP List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/jp_jsic_code_map_list.json" id="jpJsicCodeMapGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="120px" field="jsicCd" header="JSIC">
            <cmr:gridCol width="120px" field="subIndustryCd" header="SubIndustry" />
            <cmr:gridCol width="120px" field="isuCd" header="IsuCd" />
            <cmr:gridCol width="120px" field="isicCd" header="IsicCd" />
            <cmr:gridCol width="120px" field="dept" header="Dept" />
            <cmr:gridCol width="120px" field="sectorCd" header="Sector" />
            </cmr:gridCol>
            <cmr:gridCol width="120px" field="action" header="Action" > 
              <cmr:formatter functionName="actions.actionIcons" />
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      

      
    </cmr:section>
</cmr:boxContent>
</cmr:form>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Add JP JSIC CODE MAP" onClick="actions.add()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="jpJsicCodeMap" />