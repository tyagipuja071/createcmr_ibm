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
        window.location = cmr.CONTEXT_ROOT + '/code/jpofficesectorinacform';
      },
      update : function(officeCd, sectorCd, inacCd, apCustClusterId) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpofficesectorinacform?officeCd=' + encodeURIComponent(officeCd);
        loc += '&sectorCd=' + encodeURIComponent(sectorCd);
        loc += '&inacCd=' + encodeURIComponent(inacCd);
        loc += '&apCustClusterId=' + encodeURIComponent(apCustClusterId);
        alert('loc='+loc);
        window.location = loc;
      },
      remove : function(officeCd, sectorCd, inacCd, apCustClusterId) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpofficesectorinacform?officeCd=' + officeCd;
        loc += '&sectorCd=' + encodeURIComponent(sectorCd);
        loc += '&inacCd=' + encodeURIComponent(inacCd);
        loc += '&apCustClusterId=' + encodeURIComponent(apCustClusterId);
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
        actionsTag += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.officeCd + '\',\'' + rowData.sectorCd + '\',\'' + rowData.inacCd + '\',\'' + rowData.apCustClusterId + '\')" />';
        actionsTag += '<img src="' + imgloc + 'addr-remove-icon.png" class="addr-icon" title="Remove" onclick="actions.remove(\'' + rowData.officeCd + '\',\'' + rowData.sectorCd + '\',\'' + rowData.inacCd + '\',\'' + rowData.apCustClusterId + '\')" />';
               
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/jp_office_sector_inac_map_list.json';
        CmrGrid.refresh('jpOfficeSectorInacMapGrid', url, 'officeCd=:officeCd');
      }
    };
  })();
</script>

<cmr:form method="POST" action="${contextPath}/code/jp_office_sector_inac_map" name="frmCMR" class="ibm-column-form ibm-styled-form"
   modelAttribute="jpofficesectorinacmap" id="frmCMR">
  
  
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>JP OFFICE SECTOR INAC MAPPING List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/jp_office_sector_inac_map_list.json" id="jpOfficeSectorInacMapGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="200px" field="officeCd" header="OFFICE_CD">
            <cmr:gridCol width="200px" field="sectorCd" header="SECTOR_CD" />
            <cmr:gridCol width="200px" field="inacCd" header="INAC_CD" />
            <cmr:gridCol width="200px" field="apCustClusterId" header="AP_CUST_CLUSTER_ID" />
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
    <cmr:button label="Add JP OFFICE SECTOR INAC MAPPING" onClick="actions.add()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="jpofficesectorinacmap" />