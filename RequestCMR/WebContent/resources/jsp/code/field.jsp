<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.FieldInfoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  FieldInfoModel field = (FieldInfoModel) request.getAttribute("field");
	boolean newEntry = false;
  if (field.getState() == BaseModel.STATE_NEW) {
		newEntry = true;
	} 
  boolean newSeq = "Y".equals(request.getParameter("newseq"));
  if (newSeq){
    field.setFieldId(request.getParameter("fieldId"));
    field.setSeqNo(Integer.parseInt(request.getParameter("seqNo")));
    field.setCmrIssuingCntry(request.getParameter("cntry"));
    field.setType(request.getParameter("type"));
  }
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>

    var ids1 = cmr.query('SYSTEM.SUPPCNTRY', {_qall  : 'Y'});
    var model1 = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    model1.items.push({id : '*', name : '* - General'});
    for (var i =0; i < ids1.length; i++){
      model1.items.push({id : ids1[i].ret1, name : ids1[i].ret1 + ' - ' + ids1[i].ret2});
    }
    var dropdown1 = {
        listItems : model1
    };
    FilteringDropdown.loadFixedItems('cmrIssuingCntry', null, dropdown1);
    
    FormManager.addValidator('fieldId', Validators.REQUIRED, [ 'Field ID' ]);
    <%if (!newSeq){%>
      FormManager.addValidator('fieldId', function(input){
        var val = FormManager.getActualValue('fieldId');
        if (val != '' && val.indexOf('##') != 0){
          return new ValidationResult(input, false, 'Field ID should start with ##.');
        }
        var check = cmr.query('SYSTEM.FIELDIDCHECK', {id : val, cntry : FormManager.getActualValue('cmrIssuingCntry')});
        if (check && check.ret1 == '1'){
          return new ValidationResult(input, false, 'Field ID '+val+' for '+FormManager.getActualValue('cmrIssuingCntry')+' already exists. Use the add sequence function instead.');
        }
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cmrIssuingCntry == '*' || cmrIssuingCntry == ''){
          return new ValidationResult(input, true);
        }
        check = cmr.query('SYSTEM.FIELDIDCHECK2', {id : val});
        if (check && !check.ret1){
          return new ValidationResult(input, false, 'Field ID '+val+' has no entry for CMR Issuing Country = *. Please create the base first.');
        }
  
        var qParams = {
          FIELD_ID : FormManager.getActualValue('fieldId'),
          SEQ_NO : FormManager.getActualValue('seqNo'),
          CMR_ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
        };
  
        check = cmr.query('CHECKFIELDINFO', qParams);
        if (check && check.ret1 == '1') {
          return new ValidationResult(input, false, 'This Field Info (Field ID, CMR Issuing Country, Sequence No) already exists in the system.');
        }
        
        return new ValidationResult(input, true);
      }, [ 'Field ID' ]);
      FormManager.setValue('seqNo', '1');
      FormManager.readOnly('seqNo');
    <%}%>
    FormManager.addValidator('seqNo', Validators.REQUIRED, [ 'Sequence Number' ]);
    FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
    
<%}%>
    var ids = cmr.query('SYSTEM.FIELDIDLIST', {_qall  : 'Y'});
    var model = { 
        identifier : "id", 
        label : "name",
        items : []
    };
    for (var i =0; i < ids.length; i++){
      model.items.push({id : ids[i].ret1, name : ids[i].ret1});
    }
    var dropdown = {
        listItems : model
    };
    FilteringDropdown.loadFixedItems('dependsOn', null, dropdown);
    
    FormManager.addValidator('type', Validators.REQUIRED, [ 'Type' ]);
    FormManager.addValidator('type', Validators.INVALID_VALUE, [ 'Type' ]);
    FormManager.addValidator('required', Validators.REQUIRED, [ 'Required' ]);
    FormManager.addValidator('maxLength', Validators.NUMBER, [ 'Max Length' ]);
    FormManager.addValidator('validation', Validators.REQUIRED, [ 'Validation' ]);
    FormManager.addValidator('validation', function(input){
      var val = FormManager.getActualValue('validation');
      if (val == 'DB' && FormManager.getActualValue('queryId') == ''){
        return new ValidationResult(input, false, 'Query ID should be specified if validation is DB Query.');
      }
      return new ValidationResult(input, true);
    }, [ 'Validation' ]);
    FormManager.addValidator('required', function(input){
      var val = FormManager.getActualValue('seqNo');
      if (val == '1' && FormManager.getActualValue('required') == 'C'){
        return new ValidationResult(input, false, 'Sequence 1 entries cannot be conditional');
      }
      return new ValidationResult(input, true);
    }, [ 'Required' ]);
    FormManager.disable('minLength');

    var typeHandler = dojo.connect(FormManager.getField('type'), 'onChange',function(value){
        FormManager.resetValidations('choice');
        FormManager.resetValidations('maxLength');
        FormManager.enable('maxLength');
        FormManager.disable('valDependsOn');
        FormManager.addValidator('maxLength', Validators.NUMBER, [ 'Max Length' ]);
        var type = FormManager.getActualValue('type');
        if (type == 'D' || type == 'R'){
          FormManager.enable('choice');
          FormManager.addValidator('choice', Validators.REQUIRED, [ 'Type' ]);
          FormManager.disable('maxLength');
          if (type == 'D'){
            FormManager.enable('valDependsOn');
          } 
        } else if (type == 'C') {
          FormManager.disable('maxLength');
          FormManager.disable('choice');
        } else if (type == 'T' || type == 'M' || type == 'B') {
          FormManager.setValue('minLength', '0');
          FormManager.addValidator('maxLength', Validators.REQUIRED, [ 'Max Length' ]);
          FormManager.disable('choice');
        } 
    });
    if (typeHandler){
      typeHandler[0].onChange();
    }

    var reqHandler = dojo.connect(FormManager.getField('required'), 'onChange',function(value){
      var req = FormManager.getActualValue('required');
      FormManager.resetValidations('dependsOn');
      FormManager.resetValidations('dependsSetting');
      FormManager.resetValidations('condReqInd');
      FormManager.disable('dependsOn');
      FormManager.disable('dependsSetting');
      FormManager.disable('condReqInd');
      if (req == 'C'){
        FormManager.addValidator('dependsOn', Validators.REQUIRED, [ 'Depends On' ]);
        FormManager.enable('dependsOn');
        FormManager.enable('dependsSetting');
        FormManager.enable('condReqInd');
        FormManager.addValidator('condReqInd', Validators.REQUIRED, [ 'Conditionally Required' ]);
      } 
    });
    if (reqHandler){
      reqHandler[0].onChange();
    }
    
    var valHandler = dojo.connect(FormManager.getField('validation'), 'onChange',function(value){
      var val = FormManager.getActualValue('validation');
      if (val == 'DB'){
        FormManager.enable('queryId');
      } else {
        FormManager.disable('queryId');
      }
    });
    if (valHandler){
      valHandler[0].onChange();
    }

  <%if (newSeq) {%>
     FormManager.readOnly('fieldId');
     FormManager.readOnly('seqNo');
     FormManager.readOnly('cmrIssuingCntry');
     FormManager.readOnly('type');
  <%}%>
  
    var seqNo = new Number(FormManager.getActualValue('seqNo'));
    if (seqNo > 1){
      FormManager.setValue('required', 'C');
      FormManager.readOnly('required');
    }
    if (FormManager) {
      FormManager.ready();
    }
  });

  function backToList(){
    var fieldId = encodeURIComponent('${field.fieldId}');
    window.location = '${contextPath}/code/field_info?fieldId='+fieldId;
  }
  </script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/field_info_details" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form"
    modelAttribute="field">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Field Configuration" : "Update Field Configuration"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="fieldId">Field ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>${field.fieldId}</p>
          <form:hidden id="fieldId" path="fieldId" />
        </cmr:column>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="seqNo">Sequence No.: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>${field.seqNo}</p>
          <form:hidden id="seqNo" path="seqNo" />
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country :</cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>${field.cmrIssuingCntry}</p>
          <form:hidden id="cmrIssuingCntry" path="cmrIssuingCntry" />
        </cmr:column>
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="fieldId">Field ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:input path="fieldId" dojoType="dijit.form.TextBox" placeHolder="Field ID"/>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="seqNo">Sequence No. : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:input path="seqNo" dojoType="dijit.form.TextBox" placeHolder="Sequence No"/>
          </p>
        </cmr:column>

      </cmr:row>


      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country :
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="cmrIssuingCntry" placeHolder="CMR Issuing Country">
          </form:select>
          </p>
        </cmr:column>
      </cmr:row>
      <%
        }
      %>

      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="type">Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="type" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="type" placeHolder="Type">
              <form:option value=""></form:option>
              <form:option value="T">Text</form:option>
              <form:option value="D">Dropdown</form:option>
              <form:option value="C">Checkbox</form:option>
              <form:option value="R">Radio Buttons</form:option>
              <form:option value="B">BluePages Lookup</form:option>
              <form:option value="M">Memo</form:option>
            </form:select>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="choice">Choice : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="choice" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="choice" placeHolder="Choice">
              <form:option value=""></form:option>
              <form:option value="B">BDS Table Info</form:option>
              <form:option value="L">List of Values</form:option>
            </form:select>
          </p>
        </cmr:column>

      </cmr:row>


      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="minLength">Minimum Length: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:input path="minLength" dojoType="dijit.form.TextBox" maxlength="3" cssStyle="width:40px"/>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="maxLength">Max Length : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:input path="maxLength" dojoType="dijit.form.TextBox" maxlength="3" cssStyle="width:40px"/>
          </p>
        </cmr:column>

      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="validation">Validation: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="validation" searchAttr="name" style="display: inline-block;" maxHeight="200" required="false" path="validation" cssStyle="width:110px" placeHolder="Validation">
              <form:option value=""></form:option>
              <form:option value="None">None</form:option>
              <form:option value="AddrStd">Address Std</form:option>
              <form:option value="DB">DB Query</form:option>
              <form:option value="Number">Numbers</form:option>
              <form:option value="BluePages">BluePages</form:option>
              <form:option value="ALPHA">ALPHA</form:option>
              <form:option value="ALPHANUM">ALPHANUM</form:option>
              <form:option value="DIGIT">DIGIT</form:option>
              <form:option value="NO_SPECIAL_CHAR">NO_SPECIAL_CHAR</form:option>
            </form:select>
            <form:input path="queryId" dojoType="dijit.form.TextBox" cssStyle="width:120px" placeHolder="Query ID"/>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="required">Required : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="required" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="required" placeHolder="Required" >
              <form:option value=""></form:option>
              <form:option value="O">Optional</form:option>
              <form:option value="R">Required</form:option>
              <form:option value="D">Read Only</form:option>
              <form:option value="H">Hidden</form:option>
              <form:option value="G">Disabled</form:option>
              <form:option value="C">Conditional</form:option>
            </form:select>
          </p>
        </cmr:column>

      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="dependsOn">Depends On: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="dependsOn" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="dependsOn" placeHolder="Field ID">
          </form:select>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="dependsSetting">Depends Setting: </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:input path="dependsSetting" dojoType="dijit.form.TextBox" placeHolder="Depends Setting"/>
          </p>
        </cmr:column>

      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="condReqInd">Condtionally Required : </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="condReqInd" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="condReqInd" cssStyle="width:60px" placeHolder="Y/N">
              <form:option value=""></form:option>
              <form:option value="Y">Yes</form:option>
              <form:option value="N">No</form:option>
            </form:select>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="valDependsOn">Value Depends On : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:input path="valDependsOn" dojoType="dijit.form.TextBox" placeHolder="Field ID"/>
          </p>
        </cmr:column>


      </cmr:row>
<!-- 
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="readOnlyInfoInd">Read Only (Info) : </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="readOnlyInfoInd" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="readOnlyInfoInd" cssStyle="width:60px">
              <form:option value="">&nbsp;</form:option>
              <form:option value="Y">Yes</form:option>
              <form:option value="N">No</form:option>
            </form:select>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="readOnlyRevInd">Read Only (Reviewer) : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="readOnlyRevInd" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="readOnlyRevInd" cssStyle="width:60px">
              <form:option value="">&nbsp;</form:option>
              <form:option value="Y">Yes</form:option>
              <form:option value="N">No</form:option>
            </form:select>
          </p>
        </cmr:column>

      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="readOnlyProcInd">Read Only (Processor) : </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="readOnlyProcInd" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="readOnlyProcInd" cssStyle="width:60px">
              <form:option value="">&nbsp;</form:option>
              <form:option value="Y">Yes</form:option>
              <form:option value="N">No</form:option>
            </form:select>
          </p>
        </cmr:column>

        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="readOnlyReqInd">Read Only (Requester) : </cmr:label>
          </p>
        </cmr:column>

        <cmr:column span="2" width="250">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="readOnlyReqInd" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="readOnlyReqInd" cssStyle="width:60px">
              <form:option value="">&nbsp;</form:option>
              <form:option value="Y">Yes</form:option>
              <form:option value="N">No</form:option>
            </form:select>
          </p>
        </cmr:column>

      </cmr:row>
-->
      <cmr:row>
        <cmr:column span="1" width="160">
          <p>
            <cmr:label fieldId="cmt">Comments : </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2" width="250">
          <p>
            <form:textarea path="cmt" rows="3" cols="23"/>
          </p>
        </cmr:column>


      </cmr:row>

    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%
      if (newEntry) {
    %>
    <cmr:button label="Save" onClick="FieldInfoService.saveFieldInfo(true)" highlight="true" />
    <%
      } else {
    %>
    <cmr:button label="Save" onClick="FieldInfoService.saveFieldInfo(false)" highlight="true" />
    <%
      }
    %>
    <%if (!newSeq && !newEntry){%>
    <cmr:button label="Add New Sequence" onClick="FieldInfoService.addSequence('${field.fieldId}','${field.cmrIssuingCntry}', '${field.type}')" highlight="false" pad="true" />
    <%} %>
    <cmr:button label="Back to Field Info  List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="field" />