<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.code.USIbmBoModel"%>
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
  USIbmBoModel us_ibm_bo = (USIbmBoModel) request.getAttribute("us_ibm_bo");

  boolean newEntry = false;

  if (us_ibm_bo.getState() == BaseModel.STATE_NEW) {
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
      FormManager.addValidator('iOff', Validators.REQUIRED , ['I_OFF']);
    <%
      }
    %>
    
    FormManager.addValidator('aLevel1Value', Validators.REQUIRED , ['A_LEVEL_1_VALUE']);
    // FormManager.addValidator('aLevel2Value', Validators.REQUIRED , ['A_LEVEL_2_VALUE']);
    FormManager.addValidator('aLevel3Value', Validators.REQUIRED , ['A_LEVEL_3_VALUE']);
    // FormManager.addValidator('aLevel4Value', Validators.REQUIRED , ['A_LEVEL_4_VALUE']);
    
    FormManager.addValidator('nOff', Validators.REQUIRED , ['N_OFF']);
    // FormManager.addValidator('fDistrcOn', Validators.REQUIRED , ['F_DISTRC_ON']);
    FormManager.addValidator('iArOff', Validators.REQUIRED , ['I_AR_OFF']);
    // FormManager.addValidator('fApplicCash', Validators.REQUIRED , ['F_APPLIC_CASH']);
    FormManager.addValidator('fApplicColl', Validators.REQUIRED , ['F_APPLIC_COLL']);
    // FormManager.addValidator('fOffFunc', Validators.REQUIRED , ['F_OFF_FUNC']);
    
    FormManager.addValidator('qTieLineTelOff', Validators.REQUIRED , ['Q_TIE_LINE_TEL_OFF']);
    FormManager.addValidator('qTieLineTelOff', Validators.NUMBER , ['Q_TIE_LINE_TEL_OFF']);
    
    // FormManager.addValidator('tInqAddrLine1', Validators.REQUIRED , ['T_INQ_ADDR_LINE_1']);
    // FormManager.addValidator('tInqAddrLine2', Validators.REQUIRED , ['T_INQ_ADDR_LINE_2']);
    // FormManager.addValidator('nInqCity', Validators.REQUIRED , ['N_INQ_CITY']);
    // FormManager.addValidator('nInqSt', Validators.REQUIRED , ['N_INQ_ST']);
    
    // FormManager.addValidator('cInqZip', Validators.REQUIRED , ['C_INQ_ZIP']);
    // FormManager.addValidator('cInqZip', Validators.NUMBER , ['C_INQ_ZIP']);
    FormManager.addValidator('cInqCnty', Validators.REQUIRED , ['C_INQ_CNTY']);
    FormManager.addValidator('cInqCnty', Validators.NUMBER , ['C_INQ_CNTY']);
    FormManager.addValidator('nInqScc', Validators.REQUIRED , ['N_INQ_SCC']);
    
    // FormManager.addValidator('tRemitToAddrL1', Validators.REQUIRED , ['T_REMIT_TO_ADDR_L1']);
    // FormManager.addValidator('tRemitToAddrL2', Validators.REQUIRED , ['T_REMIT_TO_ADDR_L2']);
    // FormManager.addValidator('nRemitToCity', Validators.REQUIRED , ['N_REMIT_TO_CITY']);
    // FormManager.addValidator('nRemitToSt', Validators.REQUIRED , ['N_REMIT_TO_ST']);
    FormManager.addValidator('cRemitToZip', Validators.REQUIRED , ['C_REMIT_TO_ZIP']);
    FormManager.addValidator('cRemitToZip', Validators.NUMBER , ['C_REMIT_TO_ZIP']);
    FormManager.addValidator('cRemitToCnty', Validators.REQUIRED , ['C_REMIT_TO_CNTY']);
    FormManager.addValidator('cRemitToCnty', Validators.NUMBER , ['C_REMIT_TO_CNTY']);
    // FormManager.addValidator('nRemitToScc', Validators.REQUIRED , ['N_REMIT_TO_SCC']);
    
    // FormManager.addValidator('tPhysicAddrLn1', Validators.REQUIRED , ['T_PHYSIC_ADDR_LN1']);
    // FormManager.addValidator('tPhysicAddrLn2', Validators.REQUIRED , ['T_PHYSIC_ADDR_LN2']);
    // FormManager.addValidator('nPhysicCity', Validators.REQUIRED , ['N_PHYSIC_CITY']);
    // FormManager.addValidator('nPhysicSt', Validators.REQUIRED , ['N_PHYSIC_ST']);
    
    FormManager.addValidator('cPhysicZip', Validators.REQUIRED , ['C_PHYSIC_ZIP']);
    FormManager.addValidator('cPhysicZip', Validators.NUMBER , ['C_PHYSIC_ZIP']);
    FormManager.addValidator('cPhysicCnty', Validators.REQUIRED , ['C_PHYSIC_CNTY']);
    FormManager.addValidator('cPhysicCnty', Validators.NUMBER , ['C_PHYSIC_CNTY']);
    // FormManager.addValidator('nPhysicScc', Validators.REQUIRED , ['N_PHYSIC_SCC']);
    // FormManager.addValidator('iCtrlgOff', Validators.REQUIRED , ['I_CTRLG_OFF']);
    
    FormManager.ready();
  });
  
  var actions = (function() {
    return {
      save : function(flag) {
        if(flag){
          var check = cmr.query('US.US_IBM_BO_EXISTS', {
            MANDT: <%= mandt %>,
            I_OFF: FormManager.getActualValue('iOff')
          });
          
          if (check && check.ret1 == '1') {
            cmr.showAlert('This I_OFF Code already exists in the system.');
            return;
          }
          
        }
        FormManager.save('frmCMR');
      }
    };
  })();
  
  function backToList(){
    window.location = '${contextPath}/code/us_ibm_bo';
  }

</script>
<cmr:boxContent>
  <cmr:tabs />
  <cmr:form method="POST" action="${contextPath}/code/us_ibm_bo_form" id="frmCMR" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="us_ibm_bo">
    <cmr:modelAction formName="frmCMR" />
    <cmr:section>
      <cmr:row topPad="8">
        <cmr:column span="6">
          <h3>
            <%= newEntry ? "Add US IBM BO" : "Update US IBM BO" %>
          </h3>
        </cmr:column>
      </cmr:row>
      <form:hidden id="mandt" path="mandt" value="<%= mandt %>" />
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="iOff">I_OFF: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_bo.iOff != null}">
            ${us_ibm_bo.iOff}
            <form:hidden id="iOff" path="iOff" />
          </c:if>
          <c:if test="${us_ibm_bo.iOff == null}">
            <form:input id="iOff" path="iOff" dojoType="dijit.form.TextBox" placeHolder="I_OFF" maxlength="3" />
          </c:if>
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel1Value">A_LEVEL_1_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="aLevel1Value" path="aLevel1Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_1_VALUE" maxlength="2" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel2Value">A_LEVEL_2_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="aLevel2Value" path="aLevel2Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_2_VALUE" maxlength="2" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel3Value">A_LEVEL_3_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="aLevel3Value" path="aLevel3Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_3_VALUE" maxlength="2" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="aLevel4Value">A_LEVEL_4_VALUE: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="aLevel4Value" path="aLevel4Value" dojoType="dijit.form.TextBox" placeHolder="A_LEVEL_4_VALUE" maxlength="2" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nOff">N_OFF: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nOff" path="nOff" dojoType="dijit.form.TextBox" placeHolder="N_OFF" maxlength="15" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="fDistrcOn">F_DISTRC_ON: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="fDistrcOn" path="fDistrcOn" dojoType="dijit.form.TextBox" placeHolder="F_DISTRC_ON" maxlength="1" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="iArOff">I_AR_OFF: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="iArOff" path="iArOff" dojoType="dijit.form.TextBox" placeHolder="I_AR_OFF" maxlength="3" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="fApplicCash">F_APPLIC_CASH: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="fApplicCash" path="fApplicCash" dojoType="dijit.form.TextBox" placeHolder="F_APPLIC_CASH" maxlength="1" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="fApplicColl">F_APPLIC_COLL: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="fApplicColl" path="fApplicColl" dojoType="dijit.form.TextBox" placeHolder="F_APPLIC_COLL" maxlength="1" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="fOffFunc">F_OFF_FUNC: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="fOffFunc" path="fOffFunc" dojoType="dijit.form.TextBox" placeHolder="F_OFF_FUNC" maxlength="24" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="qTieLineTelOff">Q_TIE_LINE_TEL_OFF: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="qTieLineTelOff" path="qTieLineTelOff" dojoType="dijit.form.TextBox" placeHolder="Q_TIE_LINE_TEL_OFF" maxlength="7" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="tInqAddrLine1">T_INQ_ADDR_LINE_1: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="tInqAddrLine1" path="tInqAddrLine1" dojoType="dijit.form.TextBox" placeHolder="T_INQ_ADDR_LINE_1" maxlength="35" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="tInqAddrLine2">T_INQ_ADDR_LINE_2: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="tInqAddrLine2" path="tInqAddrLine2" dojoType="dijit.form.TextBox" placeHolder="T_INQ_ADDR_LINE_2" maxlength="25" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nInqCity">N_INQ_CITY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nInqCity" path="nInqCity" dojoType="dijit.form.TextBox" placeHolder="N_INQ_CITY" maxlength="13" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nInqSt">N_INQ_ST: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nInqSt" path="nInqSt" dojoType="dijit.form.TextBox" placeHolder="N_INQ_ST" maxlength="2" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cInqZip">C_INQ_ZIP: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="cInqZip" path="cInqZip" dojoType="dijit.form.TextBox" placeHolder="C_INQ_ZIP" maxlength="9" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cInqCnty">C_INQ_CNTY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="cInqCnty" path="cInqCnty" dojoType="dijit.form.TextBox" placeHolder="C_INQ_CNTY" maxlength="3" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nInqScc">N_INQ_SCC: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nInqScc" path="nInqScc" dojoType="dijit.form.TextBox" placeHolder="N_INQ_SCC" maxlength="9" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="tRemitToAddrL1">T_REMIT_TO_ADDR_L1: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="tRemitToAddrL1" path="tRemitToAddrL1" dojoType="dijit.form.TextBox" placeHolder="T_REMIT_TO_ADDR_L1" maxlength="35" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="tRemitToAddrL2">T_REMIT_TO_ADDR_L2: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="tRemitToAddrL2" path="tRemitToAddrL2" dojoType="dijit.form.TextBox" placeHolder="T_REMIT_TO_ADDR_L2" maxlength="25" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nRemitToCity">N_REMIT_TO_CITY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nRemitToCity" path="nRemitToCity" dojoType="dijit.form.TextBox" placeHolder="N_REMIT_TO_CITY" maxlength="13" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nRemitToSt">N_REMIT_TO_ST: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nRemitToSt" path="nRemitToSt" dojoType="dijit.form.TextBox" placeHolder="N_REMIT_TO_ST" maxlength="2" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cRemitToZip">C_REMIT_TO_ZIP: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="cRemitToZip" path="cRemitToZip" dojoType="dijit.form.TextBox" placeHolder="C_REMIT_TO_ZIP" maxlength="9" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cRemitToCnty">C_REMIT_TO_CNTY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="cRemitToCnty" path="cRemitToCnty" dojoType="dijit.form.TextBox" placeHolder="C_REMIT_TO_CNTY" maxlength="3" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nRemitToScc">N_REMIT_TO_SCC: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nRemitToScc" path="nRemitToScc" dojoType="dijit.form.TextBox" placeHolder="N_REMIT_TO_SCC" maxlength="9" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="tPhysicAddrLn1">T_PHYSIC_ADDR_LN1: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="tPhysicAddrLn1" path="tPhysicAddrLn1" dojoType="dijit.form.TextBox" placeHolder="T_PHYSIC_ADDR_LN1" maxlength="35" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="tPhysicAddrLn2">T_PHYSIC_ADDR_LN2: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="tPhysicAddrLn2" path="tPhysicAddrLn2" dojoType="dijit.form.TextBox" placeHolder="T_PHYSIC_ADDR_LN2" maxlength="25" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nPhysicCity">N_PHYSIC_CITY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nPhysicCity" path="nPhysicCity" dojoType="dijit.form.TextBox" placeHolder="N_PHYSIC_CITY" maxlength="13" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nPhysicSt">N_PHYSIC_ST: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nPhysicSt" path="nPhysicSt" dojoType="dijit.form.TextBox" placeHolder="N_PHYSIC_ST" maxlength="2" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cPhysicZip">C_PHYSIC_ZIP: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="cPhysicZip" path="cPhysicZip" dojoType="dijit.form.TextBox" placeHolder="C_PHYSIC_ZIP" maxlength="9" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="cPhysicCnty">C_PHYSIC_CNTY: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="cPhysicCnty" path="cPhysicCnty" dojoType="dijit.form.TextBox" placeHolder="C_PHYSIC_CNTY" maxlength="3" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="nPhysicScc">N_PHYSIC_SCC: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="nPhysicScc" path="nPhysicScc" dojoType="dijit.form.TextBox" placeHolder="N_PHYSIC_SCC" maxlength="9" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="iCtrlgOff">I_CTRLG_OFF: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <form:input id="iCtrlgOff" path="iCtrlgOff" dojoType="dijit.form.TextBox" placeHolder="I_CTRLG_OFF" maxlength="3" />
        </cmr:column>
      </cmr:row>
      
      <cmr:row>
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreateDt">Create Date: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_bo.createdTsStr != null}">
            ${us_ibm_bo.createdTsStr}
          </c:if>
          <c:if test="${us_ibm_bo.createdTsStr == null}">-</c:if>
          <form:hidden id="createDt" path="createDt" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="CreatedBy">Created By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_bo.createdBy != null}">
            ${us_ibm_bo.createdBy}
          </c:if>
          <c:if test="${us_ibm_bo.createdBy == null}">-</c:if>
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
          <c:if test="${us_ibm_bo.updatedTsStr != null}">
            ${us_ibm_bo.updatedTsStr}
          </c:if>
          <c:if test="${us_ibm_bo.updatedTsStr == null}">-</c:if>
          <form:hidden id="updateDt" path="updateDt" />
        </cmr:column>
        
        <cmr:column span="1" width="180">
          <p>
            <cmr:label fieldId="updatedBy">Last Updated By: </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <c:if test="${us_ibm_bo.updatedBy != null}">
            ${us_ibm_bo.updatedBy}
          </c:if>
          <c:if test="${us_ibm_bo.updatedBy == null}">-</c:if>
          <form:hidden id="updatedBy" path="updatedBy" />
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
    <cmr:button label="Back to US IBM BO List" onClick="backToList()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>

<cmr:model model="us_ibm_bo" />