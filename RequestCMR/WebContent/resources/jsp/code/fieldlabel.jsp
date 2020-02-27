<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/code';
  }
  dojo.addOnLoad(function() {
    var ids = cmr.query('SYSTEM.FIELDIDLIST', {_qall  : 'Y'});
    var model = { 
        identifier : "id", 
        label : "name",
        selectedItem : <%=request.getParameter("fieldId") != null ? "'"+request.getParameter("fieldId")+"'" : "null"%>,
        items : []
    };
    for (var i =0; i < ids.length; i++){
      model.items.push({id : ids[i].ret1, name : ids[i].ret1});
    }
    var dropdown = {
        listItems : model
    };
    FilteringDropdown.loadFixedItems('fieldId', null, dropdown);
    FilteringDropdown.loadItems('cmrIssuingCntry', 'cmrIssuingCntry_spinner', 'bds', 'fieldId=CMRIssuingCountry');

    dojo.connect(FormManager.getField('fieldId'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/fieldlbllist.json';
      CmrGrid.refresh('fieldlblGrid', url, 'fieldId=:fieldId&cmrIssuingCntry=:cmrIssuingCntry');
    });    
    dojo.connect(FormManager.getField('cmrIssuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/code/fieldlbllist.json';
      CmrGrid.refresh('fieldlblGrid', url, 'fieldId=:fieldId&cmrIssuingCntry=:cmrIssuingCntry');
    });    

    FormManager.ready();
  });
  
  var FieldLblService = (function() {
  return {
    fieldIdFormatter : function(value, rowIndex) {
    
      var rowData = this.grid.getItem(rowIndex);
      var fieldId = rowData.fieldId;
      var cmrIssuingCntry = rowData.cmrIssuingCntry;
      return '<a href="javascript: FieldLblService.open(\'' + fieldId + '\', \'' + cmrIssuingCntry + '\')">' + fieldId + '</a>';
    },
    open : function(fieldId, cmrIssuingCntry) {
      window.location = cmr.CONTEXT_ROOT + '/code/addfieldlbl/?fieldId=' + encodeURIComponent(fieldId) + '&cmrIssuingCntry=' + encodeURIComponent(cmrIssuingCntry);
    },
    addFieldLbl : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/addfieldlbl';
    }
  };
})();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/field_lbl" name="frmCMRSearch" class="ibm-column-form ibm-styled-form" modelAttribute="fieldlbl">
    <cmr:section>
     <cmr:row topPad="8" addBackground="true">
        <cmr:column span="1">
          <p>
            <label>Field ID: </label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <form:select dojoType="dijit.form.FilteringSelect" id="fieldId" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="fieldId" placeHolder="Field ID">
          </form:select>
        </cmr:column>
        <cmr:column span="3" width="400">
          for 
          <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: inline-block;" maxHeight="200"
            required="false" path="cmrIssuingCntry" placeHolder="CMR Issuing Country">
          </form:select>
        </cmr:column>
      </cmr:row>      
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>Field Label</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/code/fieldlbllist.json" id="fieldlblGrid" span="6" >
            <cmr:gridCol width="20%" field="fieldId" header="Field Id">
              <cmr:formatter functionName="FieldLblService.fieldIdFormatter" />
            </cmr:gridCol>
            
            <cmr:gridCol width="20%" field="cmrIssuingCntry" header="CMR Issuing Country"/>
              
                     
            <cmr:gridCol width="25%" field="lbl" header="Label"/>
            <cmr:gridCol width="20%" field="cmt" header="Cmt"/>
             
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
    <cmr:button label="Add Field Label" onClick="FieldLblService.addFieldLbl()" highlight="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
<cmr:model model="fieldlbl" />