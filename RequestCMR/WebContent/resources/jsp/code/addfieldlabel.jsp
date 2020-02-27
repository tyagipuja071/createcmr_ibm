<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.FieldLabelModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  FieldLabelModel fieldlbl = (FieldLabelModel) request.getAttribute("fieldlbl");
    boolean newEntry = false;
	   if (fieldlbl.getState() == BaseModel.STATE_NEW) {
        newEntry = true;
        
      } else {
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
  FilteringDropdown.loadFixedItems('fieldId', null, dropdown);


  FormManager.addValidator('fieldId', Validators.REQUIRED, [ 'Field ID' ]);
  FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
<%}%>

  FormManager.addValidator('lbl', Validators.REQUIRED, [ 'Label' ]);
  FormManager.ready();
  });
  
  var FieldLblService = (function() {
    return {
      saveFieldLbl : function(typeflag) {
        var fieldId = FormManager.getActualValue('fieldId');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');      
             
        if (typeflag) {
          var check = cmr.query('CHECKFIELDLBL', {
            FIELD_ID : fieldId,
            CMR_ISSUING_CNTRY : cmrIssuingCntry
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('This field label already exists in the system.');
            return;
          }
        }        
        FormManager.save('frmCMR');
      },
    };
  })();
  
  function backToList(){
    var fieldId = FormManager.getActualValue('fieldId');
    var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    window.location = cmr.CONTEXT_ROOT + '/code/field_lbl/?fieldId=' + encodeURIComponent(fieldId) + '&cmrIssuingCntry=' + encodeURIComponent(cmrIssuingCntry);
  }
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/addfieldlbl" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="fieldlbl">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Field Label" : "Update Field Label"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="170">
          <p>
            <cmr:label fieldId="fieldId">Field ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${fieldlbl.fieldId}</p>
          <form:hidden id="fieldId" path="fieldId" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="170">
          <p>
            <cmr:label fieldId="fieldId">Field ID: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="fieldId" searchAttr="name" style="display: block;" maxHeight="200"
            required="false" path="fieldId" placeHolder="Field ID">
          </form:select>
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>
      
            <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="170">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${fieldlbl.cmrIssuingCntry}</p>
          <form:hidden id="cmrIssuingCntry" path="cmrIssuingCntry" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="170">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
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
        <cmr:column span="1" width="170">
          <p>
            <cmr:label fieldId="lbl">Label: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="lbl" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>    
          
      <cmr:row>
        <cmr:column span="1" width="170">
          <p>
            <cmr:label fieldId="cmt">Comments: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="cmt" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
    
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="FieldLblService.saveFieldLbl(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="FieldLblService.saveFieldLbl(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to Field Label List" onClick="backToList()" pad="true" />
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="fieldlbl" />