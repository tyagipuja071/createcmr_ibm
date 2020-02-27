<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.StatusActModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="java.util.Date"%> 
<%@page import="java.text.SimpleDateFormat"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
StatusActModel statusActModel = (StatusActModel) request.getAttribute("statusActModel");
    boolean newEntry = false;
    if (statusActModel.getState() == BaseModel.STATE_NEW) {
      newEntry = true;
    } 
    
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('actionId', Validators.REQUIRED, [ 'Action' ]);
  FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'Customer Master Record Issuing Country' ]);
  FormManager.addValidator('actionDesc', Validators.REQUIRED, [ 'Action Description' ]);
<%}%>
  var ids1 = cmr.query('SYSTEM.GET_SUPP_CNTRY', {
    _qall : 'Y'
  });
  var model1 = {
    identifier : "id",
    label : "name",
    items : []
  };
  
  for ( var i = 0; i < ids1.length; i++) {
    model1.items.push({
      id : ids1[i].ret1,
      name : ids1[i].ret2
    });
  }
  var dropdown1 = {
    listItems : model1
  };
  FilteringDropdown.loadFixedItems('cmrIssuingCntry', null, dropdown1);

  FormManager.ready();
  });
    
  var StatusActService = (function() {
    return {
      saveStatusAct : function(typeflag) {   
        var action = FormManager.getActualValue('actionId');  
        if (typeflag) {
          var check = cmr.query('STATUS_ACT.CHECK_URL', {
            ACTION : action
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('This Validation Url already exists in the system.');
            
            return;
          }
        }  
        FormManager.save('frmCMR');
      },
    };
  })();
  
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/addStatusAct" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="statusActModel">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add Status-Action" : "Update Status-Action"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="actionId">Action: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${statusActModel.action}</p>
          <form:hidden id="actionId" path="modelAction" value="${statusActModel.action}"/>
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="actionId">Action: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="modelAction" dojoType="dijit.form.TextBox" maxlength="3"/>
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
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: block;width:400px" maxHeight="200"
              required="false" path="cmrIssuingCntry" placeHolder="${statusActModel.cmrIssuingCntry}">
            </form:select>
          </p>
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:select dojoType="dijit.form.FilteringSelect" id="cmrIssuingCntry" searchAttr="name" style="display: block;width:400px" maxHeight="200"
              required="false" path="cmrIssuingCntry" placeHolder="Select Country Code">
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
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="actionDesc">Action Description: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p><form:input path="actionDesc" dojoType="dijit.form.TextBox" maxlength="100" value="${statusActModel.actionDesc}"/></p>
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="actionDesc">Action Description: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="actionDesc" dojoType="dijit.form.TextBox" maxlength="100"/>
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>        
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="StatusActService.saveStatusAct(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Update" onClick="StatusActService.saveStatusAct(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to Status-Action" onClick="window.location = '${contextPath}/code/statusAct'" pad="true" /> 
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="statusActModel" />