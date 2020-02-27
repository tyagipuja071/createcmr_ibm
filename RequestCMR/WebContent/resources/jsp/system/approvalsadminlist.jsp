<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
String contextPath = request.getContextPath();
String createTs = UIMgr.getText("grid.approvalsCreateDateTime");
String infoTxt = UIMgr.getText("info.timeStampInfo");
String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
String title=createTs+" ("+tz+")";
String createTsHeader =title+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
%>
<script src="${resourcesPath}/js/workflow/workflow.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
  });
  
  function requestIdFormatter(value, rowIndex) {
    var rowData = this.grid.getItem(rowIndex);
    var reqType = rowData.reqType;
    if (typeof (_wfgrid) != 'undefined' && typeof (_wfrec) != 'undefined' && dojo.cookie(_wfgrid + '_rec') != null) {
      if (dojo.cookie(_wfgrid + '_rec') == value) {
        _wfrec = rowIndex;
      }
    }
    return '<a title="Override Approvals for Request  ' + value + '" href="'+cmr.CONTEXT_ROOT+'/approvalsadmin?reqId='+value+'">' + value + '</a>';
  }
  function claimFormatter(value, rowIndex) {
    var rowData = this.grid.getItem(rowIndex);
    var reqId = rowData.reqId;
    var status = rowData.overallStatus;
    var canClaim = rowData.canClaim;
    var canClaimAll = rowData.canClaimAll;
    if ('[R]' == value || '[P]' == value) {
      return '';
    }
    return value;
  }
  
</script>
<cmr:boxContent>
  <cmr:tabs />

  <form:form method="POST" action="${contextPath}/approvalsadmin" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="approval">
    <cmr:section id="GRIDSECTION">
      <cmr:row>
        <cmr:column span="6">
          <h3>Requests with Pending Approvals</h3>
        </cmr:column>
      </cmr:row>
      <cmr:grid url="/workflow/search/results/list.json" id="requestResultGrid" useFilter="true">
        <cmr:gridParam fieldId="pendingAppr" value="Y" />
        <cmr:gridCol width="80px" field="reqId" header="${ui.grid.requestID}" align="right">
          <cmr:formatter functionName="requestIdFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="75px" field="cmrIssuingCntry" header="${ui.grid.cmrIssuingCntry}" >
         <cmr:formatter functionName="countryFormatter" />
        </cmr:gridCol>
         <cmr:gridCol width="70px" field="reqTypeText" header="${ui.grid.requestType}" >
           <cmr:formatter functionName="reqTypeFormatter"/>
         </cmr:gridCol>
        <cmr:gridCol width="auto" field="custName" header="${ui.grid.customerName}" >
          <cmr:formatter functionName="wfNameFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="cmrNo" header="${ui.grid.cmrNo}" >
          <cmr:formatter functionName="cmrNoFormatter" />
        </cmr:gridCol> 
        <cmr:gridCol width="130px" field="overallStatus" header="${ui.grid.requestStatus}">
          <cmr:formatter functionName="overallStatusFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="120px" field="claimField" header="Locked By" >
          <cmr:formatter functionName="claimFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="80px" field="pendingAppr" header="${ui.grid.pendingAppr}">
          <cmr:formatter functionName="pendingApprFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="120px" field="requesterNm" header="${ui.grid.requesterNm}" />
      </cmr:grid>
      <br>
    </cmr:section>
  </form:form>

</cmr:boxContent>
<cmr:model model="approval" />