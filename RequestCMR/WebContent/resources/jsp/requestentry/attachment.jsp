<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
AppUser user = AppUser.getUser(request);
String procCenter = reqentry.getProcCenter() != null ? reqentry.getProcCenter() : "";
%>
<style>
#addAttachmentModal div.ibm-columns {
  width: 500px !important;
}
</style>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<cmr:section id="ATTACH_REQ_TAB" hidden="true">
  <jsp:include page="detailstrip.jsp" />
      <cmr:row addBackground="true">
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="attachmentList">
                      ${ui.attachments}:
                    </cmr:label>
          </p>
        </cmr:column>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="6">
          <cmr:grid url="/search/attachment.json" id="ATTACHMENT_GRID" span="6" height="250" usePaging="false">
            <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
            <cmr:view forCountry="897">
            <cmr:gridCol width="120px" field="attachTsStr" header="${ui.grid.attachTs}" ></cmr:gridCol>
            <cmr:gridCol width="auto" field="docLink" header="${ui.grid.docLink}" >
              <cmr:formatter functionName="fileNameFormatter" />
            </cmr:gridCol>
            </cmr:view>
            <cmr:view exceptForCountry="897">
            <cmr:gridCol width="auto" field="docLink" header="${ui.grid.docLink}" >
              <cmr:formatter functionName="fileNameFormatter" />
            </cmr:gridCol>
            </cmr:view>
            <cmr:gridCol width="200px" field="docContent" header="${ui.grid.docContent}" />
            <cmr:gridCol width="200px" field="action" header="${ui.grid.actions}">
<%if (!readOnly) {%>
              <cmr:formatter functionName="attchActFormatter" />
<%} else {%>
              <cmr:formatter functionName="attchActFormatterDL" />
<%} %>
            </cmr:gridCol>
          </cmr:grid>
        </cmr:column>
      </cmr:row>
      <cmr:row topPad="5" addBackground="true">
          <div class="ibm-col-1-1 cmr-middle">
<%if (!readOnly ||  ("COM".equals(reqentry.getReqStatus()) 
      && user.isProcessor() && user.getProcessingCenter() != null 
      && user.getProcessingCenter().equalsIgnoreCase(reqentry.getProcCenter()))) {%>
            <cmr:button label="${ui.btn.attachment}" onClick="doAddAttachment()" />
            <%if (!readOnly) {%>
            	<cmr:button label="${ui.btn.addscreenshot}" onClick="doAddScreenshot()" pad="true"/>
            <%}%>
            
<%}%>
          </div>
        <br><br>
      </cmr:row>
</cmr:section>
