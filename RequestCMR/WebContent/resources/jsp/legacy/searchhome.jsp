<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.util.BluePagesHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<%
AppUser user = AppUser.getUser(request);
%>

<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<style>
  table.ibm-data-table th, table.ibm-data-table td, table.ibm-data-table a, .ibm-type table caption em {
    letter-spacing: 1px;
  }
  table.ibm-data-table td:NTH-CHILD(3) {
    font-size: 12px;
  }
  th.subhead {
    font-size: 12px;
    text-transform: uppercase;
  }
  div.code-filter {
    float:right;
    font-size: 12px;
    font-family: IBM Plex Sans, Calibri;  
    text-transform: uppercase;
    font-weight:bold;
  }
  div.code-filter input {
    font-size: 12px;
    font-family: IBM Plex Sans, Calibri;  
    margin-left: 4px;
  }
</style>
<div class="ibm-columns">
  <!-- Main Content -->
  <div class="ibm-col-1-1">
    <div id="wwq-content">

      <div class="ibm-columns">
        <div class="ibm-col-1-1" style="width: 1060px">
          <div >
          <table id="codeTable" cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating">
            <tbody>
              <tr>
                <td><a style="cursor:pointer;font-size:13px" title="Search Denied Parties List (DPL)" href="${contextPath}/dplsearch">Search Denied Parties List (DPL)</a></td>
                <td style="font-size:13px">Searches against the Denied Parties List (DPL) from the Export Regulartions Office (ERO).</td>
              </tr>
              <tr>
                <td><a style="cursor:pointer;font-size:13px" title="Search Legacy DB2 Records" href="${contextPath}/legacysearch">Search Legacy DB2 Records</a></td>
                <td style="font-size:13px">Searches against the Legacy DB2 (CMRDB2D).</td>
              </tr>
              <tr>
                <td><a style="cursor:pointer;font-size:13px" title="Search SOF/WTAAS Records" href="${contextPath}/mqsearch">Search SOF/WTAAS Records</a></td>
                <td style="font-size:13px">Searches against SOF and WTAAS using the MQ query services.</td>
              </tr>
            <%if (user != null && (user.isAdmin() || user.isCmde() || user.isProcessor()) ){%>
              <tr>
                <td><a style="cursor:pointer;font-size:13px" title="File Attachments" href="${contextPath}/attachlist">File Attachments</a></td>
                <td style="font-size:13px">Lists file attachments related to requests.</td>
              </tr>
            <%}%>
            <%
              if(BluePagesHelper.isUserInUSTAXBlueGroup(user.getIntranetId()) || (user != null && (user.isAdmin()))){
            %>
              <tr>
                <td><a style="cursor:pointer;font-size:13px" title="US SCC" href="${contextPath}/code/scclist?taxTeamFlag=Y">US SCC</a></td>
                <td style="font-size:13px">Lists off State/County/City(SCC) Registered on the System.</td>
              </tr>
            <%
              }
            %>
            <%if (!"PROD".equals(SystemConfiguration.getValue("SYSTEM_TYPE"))){%>
              <tr>
                <td><a style="cursor:pointer;font-size:13px" title="KYC Compare" href="${contextPath}/kyccompare">KYC vs EVS</a></td>
                <td style="font-size:13px">Compare KYC results against EVS (NON PROD FUNCTION)</td>
              </tr>
            <%}%>
            </tbody>
          </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
