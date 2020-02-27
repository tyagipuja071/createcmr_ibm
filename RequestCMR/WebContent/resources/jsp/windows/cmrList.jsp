<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@page import="com.ibm.cio.cmr.request.model.window.CMRListModel"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script>
</script>
<%
CMRListModel summary = (CMRListModel) request.getAttribute("cmrListModel");
  String contextPath = request.getContextPath();
  String statusetts = UIMgr.getText("grid.statusetts");
  String infoTxt = UIMgr.getText("info.timeStampInfo");
  String system = request.getParameter("system");
  String reqId = request.getParameter("reqId");
  String reqType = request.getParameter("reqType");
  String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
  String title=statusetts+" ("+tz+")";
  String statusettsHeader =title+"<img src=\""+contextPath+"/resources/images/info-bubble-icon.png\" title=\""+infoTxt+"\" class=\"cmr-info-bubble\">";
%> 
<cmr:window>
  
    <input type="hidden" name="reqId" value="<%=reqId%>"> 
    <input type="hidden" name="reqType" value="<%=reqType%>">
    <cmr:row>
    <cmr:column span="2" width="350">
      <label for="cmrId"> CMR List for the Request ID "<%=reqId%>" is : </label>
            
        <br>
        
    </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true" topPad="10">
      <cmr:column span="2">
            <cmr:grid  url="/requestentry/showCMRList.json" id="CMR_LIST_GRID" span="2" height="200" usePaging="false">
            
              <cmr:gridParam fieldId="reqId" value="<%=reqId%>" />   
              <cmr:gridParam fieldId="reqType" value="<%=reqType%>" />        
              <cmr:gridCol width="70%" field="cmrNo" header="CMR No" />
            </cmr:grid>
       </cmr:column>
    </cmr:row>
  <cmr:windowClose>
  <cmr:button label="${ui.btn.refresh}" onClick="window.location = window.location.href" pad="true" highlight="true" />
  </cmr:windowClose>
</cmr:window>