<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.USCompanyModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.FieldInfoModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration" %>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  USCompanyModel us_company = (USCompanyModel) request.getAttribute("us_company");

  boolean newEntry = false;
  boolean readOnly = true;

  if (us_company.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
    readOnly = false;
  }
  
  String mandt = SystemConfiguration.getValue("MANDT").toString();
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script>
  dojo.addOnLoad(function() {
    <%
      if (newEntry){
    %>
      FormManager.addValidator('compNo', Validators.REQUIRED , ['COMP_NO']);
      FormManager.addValidator('entNo', Validators.REQUIRED , ['ENT_NO']);
      FormManager.addValidator('compLegalName', Validators.REQUIRED , ['COMP_NAME']);   
    <%
      }
    %>
    
    FormManager.addValidator('entNo', Validators.NUMBER , ['ENT_NO']);
    FormManager.addValidator('compNo', Validators.NUMBER , ['COMP_NO']);     
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var checkEnt = cmr.query('US_ENTERPRISE.GET_ENT_NO', {
            MANDT: <%= mandt %>,
            ENT_NO: FormManager.getActualValue('entNo')
          });
          
          if (checkEnt.ret1 == null  ||  checkEnt.ret1 == '') {
            cmr.showAlert('This ENTERPRISE_NUMBER does not exists in the system.');
            return;
          }
          
          var checkComp = cmr.query('US.US_COMPANY_EXISTS', {
            MANDT: <%= mandt %>,
            COMP_NO: FormManager.getActualValue('compNo')
          });
          
          if (checkComp.ret1 != null && checkComp.ret1 != '') {
            cmr.showAlert('This Company Number already exists in the system.');
            return;
          }
          
        } else {
        
	         var loevmValue = FormManager.getActualValue('loevm');
	         if(loevmValue == 'X'){
	         	var checkCmr = cmr.query('US.US_COMPANY_ACTIVE_CMR', {
	            MANDT: <%= mandt %>,
	            COMP_NO: FormManager.getActualValue('compNo')
	          });
	          
	           if (checkCmr.ret1 != null && checkCmr.ret1 == '1') {
	            cmr.showAlert('There are active CMR under this Company');
	            return;
	           }
	          }
         }
        FormManager.save('frmCMR');
      }
    };
  })();
  
  function backToList(){
    window.location = '${contextPath}/code/us_company';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/us_company_form" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="us_company">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>
            <%= newEntry ? "Add US COMPANY" : "Update US COMPANY" %>
          </h3>
        </cmr:column>
      </cmr:row>
      <form:hidden id="mandt" path="mandt" value="<%= mandt %>" />
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="compNo">Company Number: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
            <form:input id="compNo" path="compNo" dojoType="dijit.form.TextBox" placeHolder="COMP_NO" readOnly="<%= readOnly %>" maxlength="15"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="entNo">Enterprise Number: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="entNo" path="entNo" dojoType="dijit.form.TextBox" placeHolder="ENT_NO" readOnly="<%= readOnly %>" maxlength="15"/>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="compLegalName">Company Name: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="compLegalName" path="compLegalName" dojoType="dijit.form.TextBox" placeHolder="COMP_NAME" readOnly="<%= readOnly %>" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="loevm">LOEVM: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:radiobutton path="loevm" value="" checked="<%= newEntry %>" />
            Active &nbsp;&nbsp;
            <form:radiobutton path="loevm" value="X" />
            Deleted
          </p>
        </cmr:column>
      </cmr:row>
  
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateDt">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_company.createdTsStr != null}">
            ${us_company.createdTsStr}
          </c:if>
          <c:if test="${us_company.createdTsStr == null}">-</c:if>
          <form:hidden id="createDt" path="createDt" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_company.createBy != null}">
            ${us_company.createBy}
          </c:if>
          <c:if test="${us_company.createBy == null}">-</c:if>
          <form:hidden id="createBy" path="createBy" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateDt">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_company.updatedTsStr != null}">
            ${us_company.updatedTsStr}
          </c:if>
          <c:if test="${us_company.updatedTsStr == null}">-</c:if>
          <form:hidden id="updateDt" path="updateDt" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_company.updateBy != null}">
            ${us_company.updateBy}
          </c:if>
          <c:if test="${us_company.updateBy == null}">-</c:if>
          <form:hidden id="updateBy" path="updateBy" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
      </cmr:row>
      
    </cmr:section>
  </cmr:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%
      if (newEntry){
    %>
      <cmr:button label="Save" onClick="actions.save(true)" highlight="true" />
    <%
      } else {
    %>
      <cmr:button label="Update" onClick="actions.save(false)" highlight="true" />
    <%
      }
    %>
    <cmr:button label="Back to US COMPANY List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="us_company" />