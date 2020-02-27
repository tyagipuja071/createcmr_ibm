<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.Map"%>
<%@page import="com.ibm.cio.cmr.request.util.mq.MQXml"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.system.MQXmlModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<style>
tr.xml-req {
  background: #f7fbfc; /* Old browsers */
  background: -moz-linear-gradient(top, #f7fbfc 0%, #d9edf2 40%, #add9e4 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top, #f7fbfc 0%,#d9edf2 40%,#add9e4 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom, #f7fbfc 0%,#d9edf2 40%,#add9e4 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#f7fbfc', endColorstr='#add9e4',GradientType=0 ); /* IE6-9 */
}
tr.xml-resp {
  background: #eeeeee; /* Old browsers */
  background: -moz-linear-gradient(top, #eeeeee 0%, #eeeeee 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top, #eeeeee 0%,#eeeeee 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom, #eeeeee 0%,#eeeeee 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#eeeeee', endColorstr='#eeeeee',GradientType=0 ); /* IE6-9 */
}
</style>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  MQXmlModel model = (MQXmlModel) request.getAttribute("xml");
			if (model == null) {
				model = new MQXmlModel();
				model.setError(true);
				model.setErrorMsg("An error occured while getting the XMLs.");
			}
  String uniqueId = request.getParameter("uniqueId");
%>
<script>
function downloadXML(fileName){
  dojo.byId('fileName').value = fileName;
  document.forms['xmlForm'].submit();
  
}
</script>
<style>
td { 
  font-family: Calibri;
  font-size: 13px;
}
tr {
  border-top: 1px Solid #AAAAAA;
}
</style>
<cmr:window>
    <cmr:row>
      <cmr:column span="6">
        <h3>XML Values for Unique ID <%=uniqueId%></h3>
      </cmr:column>
    </cmr:row>
<%if (model.isError()){ %>
    <cmr:row>
      <cmr:column span="6">
        <%=model.getErrorMsg()%>        
      </cmr:column>
    </cmr:row>
<%} else { %>
    <cmr:row>
      <cmr:column span="6">
        <table width="100%">
          <%int cnt = 1; %>
          <%for (MQXml xml : model.getXmls()){%>
            <tr class="<%=xml.isResponse() ? "xml-resp" : "xml-req"%>">
              <th width="15%">XML <%=cnt%> <%=xml.isResponse() ? "(Response)" : "(Request)"%></th>
              <th width="20%"><a title="Download XML" href="javascript: downloadXML('<%=xml.getName()%>')"><%=xml.getName()%></a></th>
              <td width="*">
                <%Map<String,String> map = xml.getValues();%>
                <strong>Data Elements (<%=xml.getRootName()%>)</strong>
                <table cellpadding="0" cellspacing="0" border="0">
                <%for (String key : map.keySet()){%>
                  <tr>
                    <td style="text-align:right;padding-right:3px;font-weight:bold"><%=key%>:</td>
                    <td><%=(map.get(key) != null ? StringEscapeUtils.escapeHtml(map.get(key)) : "")%>
                  </tr>                  
                <%}%>
                </table>
              </td>
            </tr>
          <%cnt++; %>
          <%} %>
        </table>
      </cmr:column>
    </cmr:row>
<%} %>
  <cmr:windowClose />

<iframe id="xmlFrame" style="display:none" name="attachDlFrame"></iframe>
<form id="xmlForm" name="xmlForm" method="POST" action="${contextPath}/code/mqstatus/xml">
  <input name="action" value="XML" type="hidden">
  <input name="queryReqId" value="<%=uniqueId%>" type="hidden">
  <input name="fileName" id="fileName" type="hidden">
</form>
</cmr:window>
