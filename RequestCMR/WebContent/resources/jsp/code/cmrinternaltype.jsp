<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.CmrInternalTypesModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  CmrInternalTypesModel user = (CmrInternalTypesModel) request.getAttribute("cmrinternaltypesmain");
			boolean newEntry = false;
			if (user.getState() == BaseModel.STATE_NEW) {
				newEntry = true;
			} else {
			}
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script>
  dojo.addOnLoad(function() {
<%if (newEntry) {%>
  FormManager.addValidator('internalTyp', Validators.REQUIRED, [ 'Internal Type' ]);
  FormManager.addValidator('cmrIssuingCntry', Validators.REQUIRED, [ 'CMR Issuing Country' ]);
 
<%}%>
  FormManager.addValidator('reqTyp', Validators.REQUIRED, [ 'Request Type' ]);
  FormManager.addValidator('condition', Validators.REQUIRED, [ 'Condition' ]);
  FormManager.addValidator('status', Validators.REQUIRED, [ 'Status' ]);
  
  FormManager.ready();
  });


var CrmITyService = (function() {
  return {
     
    saveCrmITy : function(typeflag) {
      if (typeflag){
 var cmrIssuingCntry=FormManager.getActualValue('cmrIssuingCntry');
 
 var internalTyp=FormManager.getActualValue('internalTyp');
 
        var check = cmr.query('CHECKCMRINTERNALTYPES', {internalTyp : internalTyp,cmrIssuingCntry : cmrIssuingCntry });
        if (check && check.ret1 == '1'){
          cmr.showAlert('This CMRINTERNALTYPES already exists in the system.');
          return;
        }
      }
      FormManager.save('frmCMR');
    },
 
    removeCrmITy : function(){
 
   var formName='frmCMR';
     var action= 'REMOVE_CrmITy';
     var confirmMessage='Remove selected CrmIinternalType from the table?';
      
      
      if (confirmMessage) {
 
        if (!confirm(confirmMessage)) {
 
          return;
        }
      }
 
      dojo.byId(formName + '_modelState').value = 1;
 
      dojo.byId(formName + '_modelAction').value = 'D';
 
      dojo.byId(formName + '_modelMassAction').value = action;
      cmr.showProgress('Processing. Please wait...');
      document.forms[formName].submit();
    }
  };
})();
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/code/cmrinternaltypesmain" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="cmrinternaltypesmain">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3><%=newEntry ? "Create Internal Type" : "Update Internal Type"%></h3>
        </cmr:column>
      </cmr:row>
      <%
        if (!newEntry) {
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="internalTyp">Internal Type: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.internalTyp != null}">
              
         <form:input path="internalTyp" value="${cmrinternaltypesmain.internalTyp}" dojoType="dijit.form.TextBox" readonly="true" />
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.internalTyp == null}">
               <form:input path="internalTyp" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
         <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">CMR Issuing Country: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.cmrIssuingCntry != null}">
              
         <form:input path="cmrIssuingCntry" value="${cmrinternaltypesmain.cmrIssuingCntry}" dojoType="dijit.form.TextBox" readonly="true"/>
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.cmrIssuingCntry == null}">
               <form:input path="cmrIssuingCntry" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
         
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="internalTypDesc">Description: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.internalTypDesc != null}">
              
         <form:input path="internalTypDesc" value="${cmrinternaltypesmain.internalTypDesc}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.internalTypDesc == null}">
               <form:input path="internalTypDesc" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
          <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="reqTyp">Request Type: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.reqTyp != null}">
              
         <form:input path="reqTyp" value="${cmrinternaltypesmain.reqTyp}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.reqTyp == null}">
               <form:input path="reqTyp" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="priority">Priority: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.priority != 0}">
              
         <form:input path="priority" value="${cmrinternaltypesmain.priority}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.priority == 0}">
               <form:input path="priority" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
          <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="condition">Condition: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.condition != null}">
              
         <form:input path="condition" value="${cmrinternaltypesmain.condition}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.condition == null}">
               <form:input path="condition" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="sepValInd">Sep. Val. Ind.: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.sepValInd != null}">
              
         <form:input path="sepValInd" value="${cmrinternaltypesmain.sepValInd}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.sepValInd == null}">
               <form:input path="sepValInd" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
          <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="status">Status: </cmr:label>
          </p>
        </cmr:column>
      <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.status != null}">
              
         <form:input path="status" value="${cmrinternaltypesmain.status}" dojoType="dijit.form.TextBox" />
              
            </c:if>
            <c:if test="${cmrinternaltypesmain.status == null}">
               <form:input path="status" dojoType="dijit.form.TextBox" />
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>
       
      <%
        } else {
      %>
       <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="internalTyp">internalTyp: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="internalTyp" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="cmrIssuingCntry">cmrIssuingCntry: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="cmrIssuingCntry" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
       <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="internalTypDesc">internalTypDesc: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="internalTypDesc" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="reqTyp">reqTyp: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="reqTyp" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
       <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="priority">priority: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="priority" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="condition">condition: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="condition" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
       <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="sepValInd">sepValInd: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="sepValInd" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="status">status: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <form:input path="status" dojoType="dijit.form.TextBox" />
          </p>
        </cmr:column>
      </cmr:row>
     
      <%
        }
      %>
      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="createTs">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.createTs != null}">
              <fmt:formatDate type="date" value="${cmrinternaltypesmain.createTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="createTs" path="createTs" />
            </c:if>
            <c:if test="${cmrinternaltypesmain.createTs == null}">
              -
            </c:if>
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="updateTs">Last Updated: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.updateTs != null}">
              <fmt:formatDate type="date" value="${cmrinternaltypesmain.updateTs}" pattern="yyyy-MM-dd" />
              <form:hidden id="updateTs" path="updateTs" />
            </c:if>
            <c:if test="${cmrinternaltypesmain.updateTs == null}">
              -
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>


      <cmr:row>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="createTs">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.createBy != null}">
              ${cmrinternaltypesmain.createBy}
              <form:hidden id="createBy" path="createBy" />
            </c:if>
            <c:if test="${cmrinternaltypesmain.createBy == null}">
              -
            </c:if>
          </p>
        </cmr:column>
        <cmr:column span="1" width="110">
          <p>
            <cmr:label fieldId="updateTs">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <c:if test="${cmrinternaltypesmain.updateBy != null}">
              ${cmrinternaltypesmain.updateBy}
              <form:hidden id="updateBy" path="updateBy" />
            </c:if>
            <c:if test="${cmrinternaltypesmain.updateBy == null}">
              -
            </c:if>
          </p>
        </cmr:column>
      </cmr:row>

    </cmr:section>
  </form:form>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <%if (newEntry){ %>
      <cmr:button label="Save" onClick="CrmITyService.saveCrmITy(true)" highlight="true" />
    <%} else { %>
      <cmr:button label="Save" onClick="CrmITyService.saveCrmITy(false)" highlight="true" />
      <cmr:button label="Delete" onClick="CrmITyService.removeCrmITy()" highlight="false" pad="true"/>
    <%} %>
    <cmr:button label="Back to List" onClick="window.location = '${contextPath}/code/cmrinternaltypes'" pad="true" />
  </cmr:buttonsRow>
  <br>
</cmr:section>
<cmr:model model="cmrinternaltypesmain" />