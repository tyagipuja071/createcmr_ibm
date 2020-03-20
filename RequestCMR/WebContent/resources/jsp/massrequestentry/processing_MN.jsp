<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.ui.FieldInformation"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.util.legacy.LegacyDirectUtil" %>
<%@page import="com.ibm.cio.cmr.request.util.geo.GEOHandler" %>
<%@page import="com.ibm.cio.cmr.request.util.RequestUtils" %>
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
  GEOHandler handler = RequestUtils.getGEOHandler(reqentry.getCmrIssuingCntry());
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  String mcFileVersion = SystemConfiguration.getValue("MASS_CREATE_TEMPLATE_VER");
  String procCenter = reqentry.getProcCenter() != null ? reqentry.getProcCenter() : "";
%>
<form:form method="POST" action="${contextPath}/massrequest/process" name="frmCMRProcess" class="ibm-column-form ibm-styled-form"
  modelAttribute="reqentry" id="frmCMRProcess" enctype="multipart/form-data" target="processFrame">
  <cmr:modelAction formName="frmCMRProcess" />
  <form:hidden path="reqId" id="procReqId" name="reqId" value="${reqentry.reqId}" />
  <form:hidden path="cmrIssuingCntry" id="proccmrIssuingCntry" name="cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}" />
  <form:hidden path="reqType" id="procReqType" name="reqType" value="${reqentry.reqType}" />

  <jsp:include page="/resources/jsp/requestentry/detailstrip.jsp" />
  <cmr:row addBackground="true" topPad="5">
    <cmr:column span="2">
      <p>
        <label for="iterId"> ${ui.massCurrIteration}: </label> <span style="margin: 0" id="iterationId"> <c:if test="${reqentry.iterId == 0}">
           Not assigned
           </c:if> <c:if test="${reqentry.iterId != 0}">
           ${reqentry.iterId}
           </c:if> </span>
        <form:hidden id="iterId" path="iterId" name="iterId" />
      </p>
    </cmr:column>
    <cmr:column span="2">
      <p>
        <c:if test="${fn:trim(reqentry.reqType) == 'M'}">
          <label for="fileName"> ${ui.massUpdtFl}: <cmr:info text="${ui.info.massUpdtFl}"></cmr:info> </label>
        </c:if>
        <c:if test="${fn:trim(reqentry.reqType) == 'N'}">
          <label for="fileName"> ${ui.massCreateFl}: <cmr:info text="${ui.info.massCreateFl}"></cmr:info> </label>
        </c:if>
        <span style="margin: 0" id="massFileDl"> <c:if test="${reqentry.iterId != 0}">
            <%
              if (!readOnly || (user.isProcessor() && "COM".equals(reqentry.getReqStatus()) && procCenter.equals(user.getProcessingCenter()))) {
            %>
            <%
              if (!readOnly) {
            %>
            <a href="javascript:downloadMassFile()">${ui.massDnlCurrVer}</a>
            <%
              } else {
            %>
            <a href="javascript:downloadMassFile()">Download Last Version</a>
            <%
              }
            %>
            <%
              if (reqentry.getIterId() > 1) {
            %>
            <br>
            <br>
            Previous Versions:
            <br>
            <%
              for (int i = 1; i < reqentry.getIterId(); i++) {
            %>
            <a href="javascript:downloadMassFileIter(<%=i%>)">Iter <%=i%></a>&nbsp;
            <%
              }
            %>
            <%
              }
            %>
            <%
              } else {
            %>
        Iteration ${reqentry.iterId} File Uploaded.         
        <%
              }
            %>
          </c:if> <c:if test="${reqentry.iterId == 0}">
        No File Uploaded.
           </c:if> </span>
      </p>
    </cmr:column>
    <cmr:column span="2">
      <p>
        <c:if test="${fn:trim(reqentry.reqType) == 'M'}">
          <label for="massUpdtTmpl"> ${ui.massUpdtTmpl}: <cmr:info text="${ui.info.massUpdtTmpl}"></cmr:info> </label>
        </c:if>
        <c:if test="${fn:trim(reqentry.reqType) == 'N'}">
          <label for="massCreateTmpl"> ${ui.massCreateTmpl}: <cmr:info text="${ui.info.massCreateTmpl}"></cmr:info> </label>
        </c:if>
        <a href="javascript:downloadMassTemplate()">${ui.massDnlTmpl}</a>
      </p>
    </cmr:column>
    <%
      if (reqentry.getIterId() > 1) {
    %>
    <%
      if (!readOnly && !newEntry) {
    %>
    <c:if test="${reqentry.cmrIssuingCntry == '631' && reqentry.userRole == 'Processor'}">
      <cmr:column span="2">
        <div style="padding-top: 7px">
          <p>
            <label for="massDplLog"> Mass DPL Result: <cmr:info text="${ui.info.massDplLog}"></cmr:info> </label> <a
              href="javascript:downloadMassDplResult()">Download DPL Result</a>
          </p>
        </div>
      </cmr:column>
    </c:if>
    <%
      }
    %>
    <%
      } else {
    %>
    <%
      if (!readOnly && !newEntry) {
    %>
    <c:if test="${reqentry.cmrIssuingCntry == '631' && reqentry.userRole == 'Processor'}">
      <cmr:column span="2"></cmr:column>
      <cmr:column span="2"></cmr:column>
      <cmr:column span="2">
        <div style="padding-top: 7px">
          <p>
            <label for="massDplLog"> Mass DPL Result: <cmr:info text="${ui.info.massDplLog}"></cmr:info> </label> <a
              href="javascript:downloadMassDplResult()">Download DPL Result</a>
          </p>
        </div>
      </cmr:column>
    </c:if>
    <%
      }
    %>
    <%
      }
    %>
  </cmr:row>
  <cmr:row addBackground="true" topPad="5">
    <%
      if (!readOnly) {
    %>
    <cmr:column span="2">
      <p>
        <label for="massFile"> ${ui.massUplNewVer}: </label>
        <c:if test="${fn:trim(reqentry.reqType) == 'N'}">
          <input type="file" id="massFile" accept=".xlsm" name="massFile">
        </c:if>
        <c:if test="${fn:trim(reqentry.reqType) == 'M'}">
          <input type="file" id="massFile" accept=".xlsx" name="massFile">
        </c:if>
      </p>
    </cmr:column>
    <cmr:column span="2">
      <div style="padding-top: 10px">
        <cmr:button label="${ui.btn.uploadFile}" onClick="submitMassFile()" highlight="true"></cmr:button>
        <cmr:info text="${ui.info.massUplNewVer}"></cmr:info>
      <%--1897817: ITALY - DPL check for mass update when customer name and/or customer name con't are changed	Help--%>
      <%if(handler.isNewMassUpdtTemplateSupported(reqentry.getCmrIssuingCntry()) && "Processor".equalsIgnoreCase(reqentry.getUserRole())) {%>
        <cmr:button label="DPL Check" onClick="doDplCheck()" highlight="false"></cmr:button>
        <cmr:info text="Pressing the button will generate DPL Check results for addresses that were added new Customer Names on the Mass Update template."></cmr:info>
        
      <%} %>
      </div>
      <input name="massTokenId" id="massTokenId" type="hidden">
    </cmr:column>
    <c:if test="${reqentry.cmrIssuingCntry == '631' && reqentry.userRole == 'Processor'}">
      <cmr:column span="2">
        <div style="padding-top: 12px">
          <cmr:button label="DPL Check File" onClick="doMassDplChecking()" highlight="true"></cmr:button>
          <span class="ibm-required cmr-required-spacer">*</span>
          <cmr:info text="${ui.info.dplCheckMass}"></cmr:info>
        </div>
      </cmr:column>
    </c:if>
   <%if(handler.isNewMassUpdtTemplateSupported(reqentry.getCmrIssuingCntry())  && "Processor".equalsIgnoreCase(reqentry.getUserRole())) {%>
       <cmr:column span="2">
        <div style="padding-top: 12px">
          <cmr:button label="DPL Summary" onClick="showDPLSummaryScreen()" highlight="true" id="btnDplSum"></cmr:button>
          <cmr:info text="Pressing the button will open a pop-up to view the DPL Check summary"></cmr:info>
        </div>
      </cmr:column>
   <%} %>
    <%
      } else {
    %>
    <cmr:column span="4">
    </cmr:column>
    <%
      }
    %>
    <%
      if (!readOnly && !CmrConstants.REQUEST_STATUS.DRA.toString().equals(reqentry.getReqStatus())
              && CmrConstants.PROCESSING_STATUS.E.toString().equals(reqentry.getProcessedFlag())
              && CmrConstants.REQ_TYPE_MASS_UPDATE.equals(reqentry.getReqType())) {
    %>
    <cmr:column span="2">
      <p>
        <label for="massErrorLog"> ${ui.massErrLog}: <cmr:info text="${ui.info.massErrLog}"></cmr:info> </label> <a
          href="javascript:downloadErrorLog()">${ui.massDnlErrLog}</a>
      </p>
    </cmr:column>
    <%
      } else if (CmrConstants.REQ_TYPE_MASS_CREATE.equals(reqentry.getReqType())) {
    %>
    <cmr:column span="2">
      <p>
        <label for="fileVersion"> Current Version: </label> <span style="margin: 0"> <%=mcFileVersion%> </span>
      </p>
    </cmr:column>
    <%
      }
    %>
  </cmr:row>
  <c:if test="${reqentry.cmrIssuingCntry == '631' && reqentry.userRole == 'Processor'}">
    <cmr:row addBackground="true" topPad="5">
      <cmr:column span="1" width="120">
        <p>
          <label>${ui.dplChkStatus}:</label>
        </p>
      </cmr:column>
      <cmr:column span="1" width="100">
        <p>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'AP'}">
                  All Passed
                  </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'AF'}">
                  All Failed
                  </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'SF'}">
                  Some Failed
                  </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'Not Done'}">
                  Not Done
                  </c:if>
          <c:if test="${fn:trim(reqentry.dplChkResult) == 'NR'}">
                  Not Required
                  </c:if>
        </p>
      </cmr:column>
      <cmr:column span="1" width="125">
        <p>
          <label>${ui.dplChkDate}:</label>
        </p>
      </cmr:column>
      <cmr:column span="1" width="100">
        <p>${reqentry.dplChkDate}</p>
      </cmr:column>
    </cmr:row>
  </c:if>
  <%
    if (!readOnly && CmrConstants.Role_Processor.equalsIgnoreCase(reqentry.getUserRole()) && !LegacyDirectUtil.isCountryLegacyDirectEnabled(null, reqentry.getCmrIssuingCntry())) {
  %>
  <cmr:row topPad="5" addBackground="true">
    <cmr:column span="2">
      <div style="padding-top: 18px">
        <cmr:button label="${ui.btn.markAsCompleted}" onClick="markAsCompleted()" highlight="false"></cmr:button>
      </div>
    </cmr:column>
    <cmr:column span="2">
    </cmr:column>
    <%
      if (!readOnly && !CmrConstants.REQUEST_STATUS.DRA.toString().equals(reqentry.getReqStatus())
                && CmrConstants.PROCESSING_STATUS.E.toString().equals(reqentry.getProcessedFlag())
                && CmrConstants.REQ_TYPE_MASS_CREATE.equals(reqentry.getReqType())) {
    %>
    <cmr:column span="2">
      <p>
        <label for="massErrorLog"> ${ui.massErrLog}: <cmr:info text="${ui.info.massErrLog}"></cmr:info> </label> <a
          href="javascript:downloadErrorLog()">${ui.massDnlErrLog}</a>
      </p>
    </cmr:column>
    <%
      }
    %>
  </cmr:row>
  <%
    }
  %>
  <form:hidden path="iterId" id="iterId"/>
</form:form>
