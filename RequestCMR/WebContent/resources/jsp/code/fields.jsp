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
      var url = cmr.CONTEXT_ROOT + '/fieldInfolist.json';
      CmrGrid.refresh('fieldInfoListGrid', url, 'fieldId=:fieldId&cmrIssuingCntry=:cmrIssuingCntry');
    });
    dojo.connect(FormManager.getField('cmrIssuingCntry'), 'onChange', function(value) {
      var url = cmr.CONTEXT_ROOT + '/fieldInfolist.json';
      CmrGrid.refresh('fieldInfoListGrid', url, 'fieldId=:fieldId&cmrIssuingCntry=:cmrIssuingCntry');
    });
    
    FormManager.ready();
  });
  
  function typeFormatter(value, index){
    if (value == 'D'){
      return 'Dropdown';
    }
    if (value == 'T'){
      return 'Text';
    }
    if (value == 'C'){
      return 'Checkbox';
    }
    if (value == 'R'){
      return 'Radio';
    }
    if (value == 'M'){
      return 'Memo';
    }
    return value;
  }
  
  function choiceFormatter(value, index){
    if (value == 'B'){
      return 'BDS';
    }
    if (value == 'L'){
      return 'LOV';
    }
    return value;
  }

  function requiredFormatter(value, index){
    if (value == 'R'){
      return 'Required';
    }
    if (value == 'C'){
      return 'Conditional';
    }
    if (value == 'H'){
      return 'Hidden';
    }
    if (value == 'G'){
      return 'Disabled';
    }
    if (value == 'D'){
      return 'Read Only';
    }
    if (value == 'O'){
      return 'Optional';
    }
    return value;
  }
  function condReqFormatter(value, index){
    if (value == 'Y'){
      return 'Yes';
    }
    if (value == 'N'){
      return 'No';
    }
    return value;
  }
  
  function removeFieldInfo(){
    var msg = 'Remove selected record(s)?';
    FormManager.gridAction('frmCMR', 'MASS_DELETE', msg);
  }
  function backToCodeMaintHome() {
    window.location = cmr.CONTEXT_ROOT + '/code';
  }
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/field_info/process" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="fieldInfo" id="frmCMR">
    <cmr:model model="fieldInfo" />
    <cmr:modelAction formName="frmCMR" />
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
          <h3>Field Information</h3>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="10" addBackground="false">
        <cmr:column span="6">
          <cmr:grid url="/fieldInfolist.json" id="fieldInfoListGrid" span="6" height="400" hasCheckbox="true" checkBoxKeys="fieldId,cmrIssuingCntry,seqNo" usePaging="false">
            <cmr:gridParam fieldId="fieldId" value=":fieldId" />
            <cmr:gridCol width="150px" field="fieldId" header="Field ID">
              <cmr:formatter functionName="FieldInfoService.fieldIdFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="50px" field="cmrIssuingCntry" header="CMR Issuing Country" />
            <cmr:gridCol width="50px" field="seqNo" header="Seq. No." />
            <cmr:gridCol width="60px" field="type" header="Type">
              <cmr:formatter functionName="typeFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="50px" field="choice" header="Choice" >
              <cmr:formatter functionName="choiceFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="60px" field="maxLength" header="Max Length" />
            <cmr:gridCol width="60px" field="validation" header="Validation" />
            <cmr:gridCol width="65px" field="required" header="Required" >
              <cmr:formatter functionName="requiredFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="150px" field="dependsOn" header="Depends On" />
            <cmr:gridCol width="60px" field="dependsSetting" header="Depends Setting" />
            <cmr:gridCol width="50px" field="condReqInd" header="Cond. Required" >
              <cmr:formatter functionName="condReqFormatter" />
            </cmr:gridCol>
            <cmr:gridCol width="150px" field="valDependsOn" header="Value Depends On" />

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
    <cmr:button label="New Field Information" onClick="FieldInfoService.addField()" highlight="true" />
    <cmr:button label="Remove Selected" onClick="removeFieldInfo()" pad="true" />
    <cmr:button label="Back to Code Maintenance Home" onClick="backToCodeMaintHome()" pad="true" />
  </cmr:buttonsRow>
</cmr:section>
