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
        window.location = cmr.CONTEXT_ROOT + '/code/jpbocodesmapform';
      },
      update : function(subsidiaryCd, officeCd, subOfficeCd) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpbocodesmapform?subsidiaryCd=' + encodeURIComponent(subsidiaryCd);
        loc += '&officeCd=' + encodeURIComponent(officeCd);
        loc += '&subOfficeCd=' + encodeURIComponent(subOfficeCd);
        window.location = loc;
      },
      remove : function(subsidiaryCd, officeCd, subOfficeCd) {
        var str1 = 'actions.actualRemove(\"' + subsidiaryCd + '\",\"' + officeCd + '\",\"';
        str1 += subOfficeCd + '\")';
        var str2 = 'Are you sure remove this record? subsidiaryCd: '+subsidiaryCd + ' officeCd: ';
        str2 += officeCd + ' subOfficeCd: '+subOfficeCd;
        cmr.showConfirm(str1, str2);
      },
      actualRemove : function(subsidiaryCd, officeCd, subOfficeCd) {
        var loc = cmr.CONTEXT_ROOT + '/code/jpbocodesmap/delete?subsidiaryCd=' + encodeURIComponent(subsidiaryCd);
        loc += '&officeCd=' + encodeURIComponent(officeCd);
        loc += '&subOfficeCd=' + encodeURIComponent(subOfficeCd);
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
        actionsTag += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="actions.update(\'' + rowData.subsidiaryCd + '\',\'' + rowData.officeCd + '\',\'' + rowData.subOfficeCd + '\')" />';
        actionsTag += '<img src="' + imgloc + 'addr-remove-icon.png" class="addr-icon" title="Remove" onclick="actions.remove(\'' + rowData.subsidiaryCd + '\',\'' + rowData.officeCd + '\',\'' + rowData.subOfficeCd + '\')" />';
               
        return actionsTag;
      },
      refresh : function(){
        var url = cmr.CONTEXT_ROOT + '/code/jp_bo_codes_map_list.json';
        CmrGrid.refresh('jpBoCodesMapGrid', url, 'subsidiaryCd=:subsidiaryCd');
      }
    };
  })();
</script>

<cmr:form method="POST" action="${contextPath}/code/jpbocodesmap" name="frmCMR" class="ibm-column-form ibm-styled-form"
   id="frmCMR">
  
  
  <cmr:boxContent>
    <cmr:tabs />
    <cmr:section>
      
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>JP BO CODES MAP List</h3>
        </cmr:column>
      </cmr:row>
      
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/jp_bo_codes_map_list.json" id="jpBoCodesMapGrid" span="6" height="400" usePaging="true">
            <cmr:gridCol width="120px" field="subsidiaryCd" header="SUBSIDIARY_CD">
            <cmr:gridCol width="120px" field="officeCd" header="OFFICE_CD" />
            <cmr:gridCol width="120px" field="subOfficeCd" header="SUB_OFFICE_CD" />
            <cmr:gridCol width="120px" field="boCd" header="BO_CD" />
            <cmr:gridCol width="120px" field="fieldSalesCd" header="FIELD_SALES_CD" />
            <cmr:gridCol width="120px" field="salesOfficeCd" header="SALES_OFFICE_CD" />
            <cmr:gridCol width="120px" field="mktgDivCd" header="MKTG_DIV_CD" />
            <cmr:gridCol width="120px" field="mrcCd" header="MRC_CD" />
            <cmr:gridCol width="120px" field="deptCd" header="DEPT_CD" />
            <cmr:gridCol width="120px" field="mktgDeptName" header="MKTG_DEPT_NAME" />
            <cmr:gridCol width="120px" field="clusterId" header="CLUSTER_ID" />
            <cmr:gridCol width="120px" field="clientTierCd" header="CLIENT_TIER_CD" />
            <cmr:gridCol width="120px" field="isuCdOverride" header="ISU_CD_OVERRIDE" />
            <cmr:gridCol width="120px" field="isicCd" header="ISIC_CD" />
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
    <cmr:button label="Add JP BO CODES MAP" onClick="actions.add()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="jpBoCodesMap" />