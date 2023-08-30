<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<%
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
String processingType = PageManager.getProcessingType(reqentry.getCmrIssuingCntry(), reqentry.getReqType());
%>

<style>
table.checklist {
  width: 100%;
}

table.checklist th.header {
  text-align: center;
  font-size:16px;
}

table.checklist .subheader {
  padding-top:15px;
}
table.checklist table.checklist-questions {
  margin-top:20px;
  width:100%;
}

table.checklist table.checklist-questions td, table.checklist table.checklist-questions th {
  padding:3px;
  border-right: 1px Solid #777777;
  border-bottom: 1px Solid #777777;
}

table.checklist table.checklist-questions tr:first-child th {
  border-top: 1px Solid #777777;
  border-left: 1px Solid #777777;
}

table.checklist table.checklist-questions tr:nth-child(even) {
  background: #CCCCCC;
}

table.checklist table.checklist-questions tr:hover {
  background: #EEEEEE;
  cursor: pointer;
}

table.checklist table.checklist-questions tr  td:first-child {
  border-left: 1px Solid #777777;
}

table.checklist span.checklist-radio {
  padding-right:10px;
}
</style>
<cmr:section id="CHECKLIST_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />
  <cmr:row addBackground="true">
    <cmr:column span="6">
    
    <cmr:view forCountry="852">
      <jsp:include page="AP/vietnam_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="720">
      <jsp:include page="AP/cambodia_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="834">
      <jsp:include page="AP/singapore_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="738">
      <jsp:include page="AP/hongkong_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="736">
      <jsp:include page="AP/macao_checklist.jsp" />
    </cmr:view>
     <cmr:view forCountry="646">
      <jsp:include page="AP/myanmar_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="714">
      <jsp:include page="AP/laos_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="358,359,363,607,620,626,651,675,677,680,694,695,713,741,752,762,767,768,772,787,805,808,821,823,832,842,849,865,889,871">
      <jsp:include page="CEMEA/cemea_checklist.jsp" />
    </cmr:view> 
    <cmr:view forCountry="641">
      <jsp:include page="CN/china_checklist.jsp" />
    </cmr:view>   
    <cmr:view forCountry="858">
      <jsp:include page="TW/tw_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="766">
      <jsp:include page="KR/kr_checklist.jsp" />
    </cmr:view>
    <cmr:view forCountry="755">
    <%  if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) { %>  
      <jsp:include page="IL/israel_checklist.jsp" />
    <%  } %>  
    </cmr:view>    
    </cmr:column>
  </cmr:row>
</cmr:section>
