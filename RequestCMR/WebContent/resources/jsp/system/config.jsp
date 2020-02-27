<%@page import="java.util.List"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collection"%>
<%@page import="org.springframework.ui.ModelMap"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  List fileList = new ArrayList();
			fileList.add("cmr-email.html");
			fileList.add("cmr-log4j.properties");
			fileList.add("cmr-messages.properties");
			fileList.add("cmr-queries.properties");
			fileList.add("cmr-ui.properties");
			fileList.add("createcmrconfig.xml");
      fileList.add("TgmeStanService15.wsdl");
      fileList.add("MassUpdateTemplate.xlsx");
  String status = (String) request.getParameter("uploadstatus");
%>
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<link rel="stylesheet" type="text/css"
  href="//w3.the.ibm.com/standards/design/theme/cdsstyle.css" />
<script>
  cmr.NOSESSIONCHECK = true;
  function downloadConfig(name) {
    var formId = 'frmCONFIG';
    dojo.byId('configdownload').value = name;
    var form = dojo.byId(formId);
    var oldTarget = form.target;
    form.target = 'configFrame';
    form.submit();
    form.target = oldTarget;
  }
  function uploadConfig() {
    var formId = 'frmUPLOAD';
    var val = (dojo.byId('frmCONFIG')['configuploaditem'].value);
    if (val == null || val == ''){
      alert('Select a file to replace from the list.');
      return;
    }
    var file = dojo.byId('frmUPLOAD').configupload.value;
    if (val != file){
      alert('Selected file does not match selected name from the list.');
      return;
    }
    dojo.byId('configuploadname').value = val;
    var form = dojo.byId(formId);
    form.submit();
  }</script>


<div class="ibm-columns">
  <!-- Main Content -->
  <div class="ibm-col-1-1">
    <div id="wwq-content">
    <%if ("Y".equals(status)){%>
      <div class="ibm-columns">
         <div class="ibm-col-1-1">
            <h3 style="color:green">File uploaded successfully.</h3>
         </div>
      </div>
     <%} %>

      <div class="ibm-columns">
        <div class="ibm-col-1-1" style="width: 915px">
          <form name="frmCONFIG" id="frmCONFIG" action="${contextPath}/config/process" method="POST">
            <input type="hidden" name="configdownload" id="configdownload"> 
            <table cellspacing="0" cellpadding="0" border="0" summary="Config" class="ibm-data-table ibm-sortable-table ibm-alternating">
              <caption>
                <em>Configuration</em>
              </caption>
              <thead>
                <tr>
                  <th scope="col" width="30%">Name</th>
                  <th scope="col" width="*">Actions</th>
                </tr>
              </thead>
              <tbody>
                <%
                  for (int i = 0; i < fileList.size(); i++) {
                %>
                <%
                  String name = (String) fileList.get(i);
                %>
                <tr>
                  <td><%=name%></td>
                  <td>
                    <input type="button" onclick="downloadConfig('<%=name%>')" value="Download"> 
                    <input type="radio" name="configuploaditem" value="<%=name%>">Upload contents here
                  </td>
                </tr>
                <%
                  }
                %>
              </tbody>
            </table>
            <iframe id="configFrame" style="display: none" name="configFrame"></iframe>
          </form>
        </div>
      </div>
      <div class="ibm-columns">
        <div class="ibm-col-1-1">
          <h3>Upload</h3>
          <form name="fmrUPLOAD" id="frmUPLOAD" method="POST" action="${contextPath}/config/upload" enctype="multipart/form-data">
          <input type="file" name="configupload">
          <input type="hidden" name="configuploadname" id="configuploadname">
          <br>
          <input type="button" onclick="uploadConfig()" value="Upload File">
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
