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
        window.location = cmr.CONTEXT_ROOT + '/code/jpisictojsicmapform';
      },
      update : function(jsicCd, mandt) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpisictojsicmapform?jsicCd=' + encodeURIComponent(jsicCd);
        loc += '&mandt=' + encodeURIComponent(mandt);
        window.location = loc;
      },
      remove : function(jsicCd, mandt) {
        var str1 = 'actions.actualRemove(\"' + jsicCd + '\",\"' + mandt + '\")';
        var str2 = 'Are you sure remove this record? jsicCd: '+jsicCd + ' mandt: ' + mandt;
        cmr.showConfirm(str1, str2);
      },
      actualRemove : function(jsicCd, mandt) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpisictojsicmap/delete?jsicCd=' + encodeURIComponent(jsicCd);
        loc += '&mandt=' + encodeURIComponent(mandt);
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
        actionsTag += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.jsicCd + '\',\'' + rowData.mandt + '\')" />';
        actionsTag += '<img src="' + imgloc + 'addr-remove-icon.png" class="addr-icon" title="Remove" onclick="actions.remove(\'' + rowData.jsicCd + '\',\'' + rowData.mandt + '\')" />';
               
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/jp_isic_to_jsic_map_list.json';
        CmrGrid.refresh('jpIsicToJsicMapGrid', url, 'jsicCd=:jsicCd');
      }
    };
  })();
</script>

<cmr:form method="POST" action="${contextPath}/code/jpisictojsicmap" name="frmCMR" class="ibm-column-form ibm-styled-form"
   id="frmCMR">
  
  
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>JP ISIC To JSIC MAP List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/jp_isic_to_jsic_map_list.json" id="jpIsicToJsicMapGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="120px" field="mandt" header="MANDT">
            <cmr:gridCol width="120px" field="jsicCd" header="JSIC_CD" />
            <cmr:gridCol width="120px" field="isicCd" header="IsicCd" />
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
    <cmr:button label="Add JP ISIC TO JSIC MAP" onClick="actions.add()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="jpIsicToJsicMap" />