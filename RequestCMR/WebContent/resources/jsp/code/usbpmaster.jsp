<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.USBusinessPartnerMasterModel"%>
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
  USBusinessPartnerMasterModel us_bp_master = (USBusinessPartnerMasterModel) request.getAttribute("us_bp_master");

  boolean newEntry = false;

  if (us_bp_master.getState() == BaseModel.STATE_NEW) {
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
        FormManager.addValidator('companyNo', Validators.REQUIRED, [ 'Company No' ]);
    <%
      }
    %>
    
    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR NO' ]);
    FormManager.addValidator('loevm', Validators.REQUIRED, [ 'LOEVM' ]);
    
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var check = cmr.query('US.US_BP_MASTER_EXISTS', {
            MANDT: <%= mandt %>,
            COMPANY_NO: FormManager.getActualValue('companyNo')
          });
          
          if (check && check.ret1 == '1') {
            cmr.showAlert('This Company Code already exists in the system.');
            return;
          }
          
        }
        FormManager.save('frmCMR');
      }
    };
  })();
  
  function backToList(){
    window.location = '${contextPath}/code/us_bp_master';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />
  <form:form method="POST" action="${contextPath}/code/us_bp_master_form" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="us_bp_master">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>
            <%= newEntry ? "Add US Business Partner" : "Update US Business Partner" %>
          </h3>
        </cmr:column>
      </cmr:row>
      
      <form:hidden id="mandt" path="mandt" value="<%= mandt %>" />
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="companyNo">COMPANY NO: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_bp_master.companyNo != null}">
            ${us_bp_master.companyNo}
            <form:hidden id="companyNo" path="companyNo" />
          </c:if>
          <c:if test="${us_bp_master.companyNo == null}">
            <form:input id="companyNo" path="companyNo" dojoType="dijit.form.TextBox" placeHolder="Company No" maxlength="15"/>
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="cmrNo">CMR NO: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="cmrNo" path="cmrNo" dojoType="dijit.form.TextBox" placeHolder="CMR No" maxlength="10" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="loevm">LOEVM: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:radiobutton path="loevm" value="" />
            Active &nbsp;&nbsp;
            <form:radiobutton path="loevm" value="X" />
            Deleted
          </p>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="CreateDt">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_bp_master.createdTsStr != null}">
            ${us_bp_master.createdTsStr}
          </c:if>
          <c:if test="${us_bp_master.createdTsStr == null}">-</c:if>
          <form:hidden id="createDt" path="createDt" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="CreatedBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_bp_master.createdBy != null}">
            ${us_bp_master.createdBy}
          </c:if>
          <c:if test="${us_bp_master.createdBy == null}">-</c:if>
          <form:hidden id="createdBy" path="createdBy" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="updateDt">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_bp_master.updatedTsStr != null}">
            ${us_bp_master.updatedTsStr}
          </c:if>
          <c:if test="${us_bp_master.updatedTsStr == null}">-</c:if>
          <form:hidden id="updateDt" path="updateDt" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="130">
          <p>
            <cmr:label fieldId="updatedBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_bp_master.updatedBy != null}">
            ${us_bp_master.updatedBy}
          </c:if>
          <c:if test="${us_bp_master.updatedBy == null}">-</c:if>
          <form:hidden id="updatedBy" path="updatedBy" />
        </cmr:column>
      </cmr:row>
      
    </cmr:section>
  </form:form>
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
    <cmr:button label="Back to US Business Partner Master List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="us_bp_master" />