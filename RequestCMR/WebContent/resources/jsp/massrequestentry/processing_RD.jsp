<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.ui.FieldInformation"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  AppUser user = AppUser.getUser(request);
  boolean noFindCMR = user.getAuthCode() == null;
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  String mcFileVersion = SystemConfiguration.getValue("MASS_CREATE_TEMPLATE_VER");
  String procCenter = reqentry.getProcCenter() != null ? reqentry.getProcCenter() : "";
%>
<script>
  dojo.addOnLoad(function(){
  if (FormManager) {
  var cntry= _pagemodel.cmrIssuingCntry;
   if ((cntry == '838' || cntry == '866' || cntry == '754' || cntry == '758' || cntry == '822' || cntry == '666' || cntry == '864' || cntry=='780' ) && _pagemodel.reqType=='R') {
     cmr.hideNode('cmrsearchid');
   } 
  }
  });
</script>
<style>
.padtop {
  margin-top : 10px !important;
}
</style>
<cmr:form method="POST" action="${contextPath}/reactivaterequest/process" name="frmReactivate" class="ibm-column-form ibm-styled-form"
  modelAttribute="reqentry" id="frmReactivate">
  <cmr:modelAction formName="frmReactivate" />
  <form:hidden path="reqId" id="procReqId" name="reqId" value="${reqentry.reqId}" />
  <jsp:include page="/resources/jsp/requestentry/detailstrip.jsp" />
  <cmr:row addBackground="true" topPad="10">
    <cmr:column span="4">
      <%
        if (!readOnly) {
      %>
      <cmr:grid url="/requestentry/reactivate/cmrNolist.json" id="CMR_LIST_GRID" hasCheckbox="true" checkBoxKeys="cmrNo" span="4" height="200"
        usePaging="false">

        <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
        <cmr:gridParam fieldId="reqType" value="${reqentry.reqType}" />
        <cmr:gridParam fieldId="cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
        <cmr:gridCol width="20%" field="cmrNo" header="CMR No" />
        <cmr:gridCol width="*" field="name" header="Name" />
        <cmr:gridCol width="10%" field="orderBlock" header="Order Block" />
        <cmr:gridCol width="10%" field="deleted" header="Inactive" />
        <cmr:gridCol width="10%" field="dplChkResult" header="Dpl Check Result" />
      </cmr:grid>
      <%
        } else {
      %>
      <cmr:grid url="/requestentry/reactivate/cmrNolist.json" id="CMR_LIST_GRID" span="2" height="200" usePaging="false">

        <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
        <cmr:gridParam fieldId="reqType" value="${reqentry.reqType}" />
        <cmr:gridParam fieldId="cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
        <cmr:gridCol width="70%" field="cmrNo" header="CMR No" />
      </cmr:grid>
      <%
        }
      %>
    </cmr:column>
    <%
      if (!readOnly) {
    %>
    <cmr:view exceptForCountry="838,866,754,758,822,666,864,780">
    <cmr:column span="2">
      <cmr:row>
        <cmr:buttonsRow>
          <cmr:button label="CMR Search" onClick="doCMRSearch()" highlight="true" pad="true" styleClass="padtop"/>
        </cmr:buttonsRow>
        <cmr:buttonsRow>
          <cmr:button label="Add CMR Numbers" onClick="addCmrs()" highlight="true" pad="true" styleClass="padtop"/>
        </cmr:buttonsRow>
        <cmr:buttonsRow>
          <cmr:button label="Remove Selected" onClick="doRemoveFromCMRList()" pad="true" styleClass="padtop"/>
        </cmr:buttonsRow>
      </cmr:row>
    </cmr:column>
    </cmr:view>
    <cmr:view forCountry="838,866,754,758,822,666,864,780">
    <%if(!"E".equals(reqentry.getProcessedFlag())){%>
    <cmr:column span="2">
      <cmr:row>
        <cmr:buttonsRow>
          <cmr:button label="CMR Search" onClick="doCMRSearch()" highlight="true" pad="true" styleClass="padtop" id='cmrsearchid'/>
        </cmr:buttonsRow>
        <cmr:buttonsRow>
          <cmr:button label="Add CMR Numbers" onClick="addCmrs()" highlight="true" pad="true" styleClass="padtop"/>
        </cmr:buttonsRow>
        <cmr:buttonsRow>
          <cmr:button label="Remove Selected" onClick="doRemoveFromCMRList()" pad="true" styleClass="padtop"/>
        </cmr:buttonsRow>
      </cmr:row>
    </cmr:column>
    <%
      }
    %>
    </cmr:view>
    <%
      }
    %>
    <br>
    <br>
  </cmr:row>
  <jsp:include page="addcmrs.jsp" />
</cmr:form>
