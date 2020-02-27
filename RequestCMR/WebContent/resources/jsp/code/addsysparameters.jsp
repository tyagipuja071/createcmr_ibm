<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.SysParametersModel"%>
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
<style>
  span.reqtype {
    margin-left: 7px;
    font-size: 14px;
  }
  div.supported-type {
    padding-top: 10px;
  }
  
</style>
<%
SysParametersModel suppModel = (SysParametersModel) request.getAttribute("sysparametersmodel");
    boolean newEntry = false;
    if (suppModel.getState() == BaseModel.STATE_NEW) {
      newEntry = true;
      
    } 


%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
  // Default values
    var parameterNm = ["TEXT", "NUMBER"];
    var parameterVe = ["T", "N"];
    var typeModel = { identifier : "id", label : "name", items : [] };
    for ( var i = 0; i < parameterNm.length; i++) {
      typeModel.items.push({ id : parameterVe[i], name : parameterNm[i] });
    }
    var fixedParameterTypes = {
      listItems : typeModel
    };
    FilteringDropdown.loadFixedItems('parameterTyp', null, fixedParameterTypes);
    
<%if (newEntry) {%>
  FormManager.addValidator('parameterCd', Validators.REQUIRED, [ 'Code' ]);
  FormManager.addValidator('parameterName', Validators.REQUIRED, [ 'Name' ]);
  FormManager.addValidator('parameterTyp', Validators.REQUIRED, [ 'Type' ]);
  FormManager.addValidator('parameterValue', Validators.REQUIRED, [ 'Value' ]);
<%}%>

  FormManager.ready();
  });

  var SysParameterService = (function() {
    return {
      saveSysParameter : function(typeflag) {
        var paramCd = FormManager.getActualValue('parameterCd');
        if (typeflag) {
          var check = cmr.query('SYS_PARAM', {
            PARAMETER_CD : paramCd
          });
          if (check && check.ret1 == '1') {
            cmr.showAlert('This Parameter Code already exists in the system.');
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

  <form:form method="POST" action="${contextPath}/code/addsysparameterpage" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="sysparametersmodel">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Add System Parameters" : "Update System Parameters"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="parameterCd">Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>${sysparametersmodel.parameterCd}</p>
          <form:hidden id="parameterCd" path="parameterCd" />
        </cmr:column>
        
      </cmr:row>
      <%
        } else {
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="parameterCd">Code: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="parameterCd" dojoType="dijit.form.TextBox" maxlength="20"/>
          </p>
        </cmr:column>
       </cmr:row>
      <%
        }
      %>
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="parameterName">Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p><form:input path="parameterName" dojoType="dijit.form.TextBox" maxlength="50" value="${sysparametersmodel.parameterName}" cssStyle="width:500px"/></p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="parameterTyp">Type: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
          	<form:select dojoType="dijit.form.FilteringSelect" id="parameterTyp" searchAttr="name" style="display: block; width:100px" maxHeight="200"
              path="parameterTyp" placeHolder="Select Parameter Type">
            </form:select>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="parameterValue">Value: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="parameterValue" dojoType="dijit.form.TextBox" maxlength="1000" value="${sysparametersmodel.parameterValue}" cssStyle="width:700px"/>
          </p>
        </cmr:column>
      </cmr:row>

      <cmr:row>
        <cmr:column span="1" width="150">
          <p>
            <cmr:label fieldId="cmdeMaintainableIndc">CMDE Maintainable: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:checkbox id="checkCmdeMaintIndc" path="cmdeMaintIndcCheckBox"/>
          </p>
        </cmr:column>
      </cmr:row>
      <form:hidden path="createBy" id="createBy"></form:hidden>
    
    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="SysParameterService.saveSysParameter(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Update" onClick="SysParameterService.saveSysParameter(false)" highlight="true" />
    <%} %>
    <cmr:button label="Back to System Parameters List" onClick="window.location = '${contextPath}/code/sysparameters'" pad="true" /> 
  </cmr:buttonsRow>
  <br>
  
</cmr:section>
<cmr:model model="sysparametersmodel" />