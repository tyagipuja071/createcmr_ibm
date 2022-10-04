<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.USIbmOrgModel"%>
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
  USIbmOrgModel us_ibm_org = (USIbmOrgModel) request.getAttribute("us_ibm_org");

  boolean newEntry = false;

  if (us_ibm_org.getState() == BaseModel.STATE_NEW) {
    newEntry = true;
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
      FormManager.addValidator('aLevel1Value', Validators.REQUIRED, ['A_LEVEL_1_VALUE']);
      FormManager.addValidator('aLevel2Value', Validators.REQUIRED, ['A_LEVEL_2_VALUE']);
      FormManager.addValidator('aLevel3Value', Validators.REQUIRED, ['A_LEVEL_3_VALUE']);
      FormManager.addValidator('aLevel4Value', Validators.REQUIRED, ['A_LEVEL_4_VALUE']);
    <%
      }
    %>

    FormManager.addValidator('iOrgPrimry', Validators.REQUIRED, ['I_ORG_PRIMRY']);
    FormManager.addValidator('iOrgSecndr', Validators.REQUIRED, ['I_ORG_SECNDR']);
    FormManager.addValidator('iOrgPrimryAbbv', Validators.REQUIRED, ['I_ORG_PRIMRY_ABBV']);
    FormManager.addValidator('iOrgSecndrAbbv', Validators.REQUIRED, ['I_ORG_SECNDR_ABBV']);
    FormManager.addValidator('nOrgFull', Validators.REQUIRED, ['N_ORG_FULL']);
    
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var check = cmr.query('US.US_IBM_ORG_EXISTS', {
            MANDT: <%= mandt %>,
            A_LEVEL_1_VALUE: FormManager.getActualValue('aLevel1Value'),
            A_LEVEL_2_VALUE: FormManager.getActualValue('aLevel2Value'),
            A_LEVEL_3_VALUE: FormManager.getActualValue('aLevel3Value'),
            A_LEVEL_4_VALUE: FormManager.getActualValue('aLevel4Value')
          });
          
          if (check && check.ret1 == '1') {
            cmr.showAlert('This A_LEVEL_1_VALUE, A_LEVEL_2_VALUE, A_LEVEL_3_VALUE, A_LEVEL_4_VALUE Code already exists in the system.');
            return;
          }
          
        }
        FormManager.save('frmCMR');
      }
    };
  })();
  
  function backToList(){
    window.location = '${contextPath}/code/us_ibm_org';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/us_ibm_org_form" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="us_ibm_org">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>
            <%= newEntry ? "Add US IBM ORG" : "Update US IBM ORG" %>
          </h3>
        </cmr:column>
      </cmr:row>
      <form:hidden id="mandt" path="mandt" value="<%= mandt %>" />
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel1Value">A_LEVEL_1_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.aLevel1Value != null}">
            ${us_ibm_org.aLevel1Value}
            <form:hidden id="aLevel1Value" path="aLevel1Value" />
          </c:if>
          <c:if test="${us_ibm_org.aLevel1Value == null}">
            <form:input id="aLevel1Value" path="aLevel1Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_1_VALUE" maxlength="2" />
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel2Value">A_LEVEL_2_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.aLevel2Value != null}">
            ${us_ibm_org.aLevel2Value}
            <form:hidden id="aLevel2Value" path="aLevel2Value" />
          </c:if>
          <c:if test="${us_ibm_org.aLevel2Value == null}">
            <form:input id="aLevel2Value" path="aLevel2Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_2_VALUE" maxlength="2" />
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel3Value">A_LEVEL_3_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.aLevel3Value != null}">
            ${us_ibm_org.aLevel3Value}
            <form:hidden id="aLevel3Value" path="aLevel3Value" />
          </c:if>
          <c:if test="${us_ibm_org.aLevel3Value == null}">
            <form:input id="aLevel3Value" path="aLevel3Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_3_VALUE" maxlength="2" />
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel4Value">A_LEVEL_4_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.aLevel4Value != null}">
            ${us_ibm_org.aLevel4Value}
            <form:hidden id="aLevel4Value" path="aLevel4Value" />
          </c:if>
          <c:if test="${us_ibm_org.aLevel4Value == null}">
            <form:input id="aLevel4Value" path="aLevel4Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_4_VALUE" maxlength="2" />
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="iOrgPrimry">I_ORG_PRIMRY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="iOrgPrimry" path="iOrgPrimry" dojoType="dijit.form.TextBox" placeHolder="I_ORG_PRIMRY" maxlength="15" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="iOrgSecndr">I_ORG_SECNDR: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="iOrgSecndr" path="iOrgSecndr" dojoType="dijit.form.TextBox" placeHolder="I_ORG_SECNDR" maxlength="3" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="iOrgPrimryAbbv">I_ORG_PRIMRY_ABBV: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="iOrgPrimryAbbv" path="iOrgPrimryAbbv" dojoType="dijit.form.TextBox" placeHolder="I_ORG_PRIMRY_ABBV" maxlength="5" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="iOrgSecndrAbbv">I_ORG_SECNDR_ABBV: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="iOrgSecndrAbbv" path="iOrgSecndrAbbv" dojoType="dijit.form.TextBox" placeHolder="I_ORG_SECNDR_ABBV" maxlength="3" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nOrgFull">N_ORG_FULL: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nOrgFull" path="nOrgFull" dojoType="dijit.form.TextBox" placeHolder="N_ORG_FULL" maxlength="40" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateDt">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.createdTsStr != null}">
            ${us_ibm_org.createdTsStr}
          </c:if>
          <c:if test="${us_ibm_org.createdTsStr == null}">-</c:if>
          <form:hidden id="createDt" path="createDt" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreatedBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.createdBy != null}">
            ${us_ibm_org.createdBy}
          </c:if>
          <c:if test="${us_ibm_org.createdBy == null}">-</c:if>
          <form:hidden id="createdBy" path="createdBy" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updateDt">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.updatedTsStr != null}">
            ${us_ibm_org.updatedTsStr}
          </c:if>
          <c:if test="${us_ibm_org.updatedTsStr == null}">-</c:if>
          <form:hidden id="updateDt" path="updateDt" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updatedBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_org.updatedBy != null}">
            ${us_ibm_org.updatedBy}
          </c:if>
          <c:if test="${us_ibm_org.updatedBy == null}">-</c:if>
          <form:hidden id="updatedBy" path="updatedBy" />
        </cmr:column>
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
    <cmr:button label="Back to US IBM ORG List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="us_ibm_org" />