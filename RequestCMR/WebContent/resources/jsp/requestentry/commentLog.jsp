<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String contextPath = request.getContextPath();
  String cmtDt = UIMgr.getText("grid.cmtDt");
  String infoTxt = UIMgr.getText("info.timeStampInfo");
  String tz = SystemConfiguration.getValue("DATE_TIMEZONE");
  String title = cmtDt + " (" + tz + ")";
  String cmtDtHeader = title + "<img src=\"" + contextPath + "/resources/images/info-bubble-icon.png\" title=\"" + infoTxt
      + "\" class=\"cmr-info-bubble\">";
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null){
    readOnly = false;
  }
  String autoEngineIndc = (String) request.getAttribute("autoEngineIndc");
  AppUser user = AppUser.getUser(request);
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
%>
<!--  Comment Log -->
<cmr:boxContent>
  <cmr:tabs noPad="true" />
  <cmr:section alwaysShown="true">

    <div class="cmtlog">
      <div class="section-header">Comments:</div>
      <div class="add-cmt">
      <%if ((user.isAdmin() || user.isCmde()) && "C".equals(reqentry.getReqType()) && "COM".equals(reqentry.getReqStatus())){%>
        <input type="button" id="recreateCMR" class="cmr-grid-btn-r" style="margin-left: 20px" onclick="recreateCMR()" value="RECREATE CMR">
      <%} %>
      <%if (user.isProcessor() && !readOnly && "PVA".equals(reqentry.getReqStatus())
        && ("C".equals(reqentry.getReqType()) || "U".equals(reqentry.getReqType())) 
        && ("A".equals(reqentry.getRdcProcessingStatus()) || "N".equals(reqentry.getRdcProcessingStatus())) ){ %>
        <input type="button" id="reprocessRdc" class="cmr-grid-btn-o" style="margin-left: 20px" onclick="cmr.reprocessRdc()" value="Reprocess RDC">
      <%} %> 
      <%if (user.isProcessor() && !readOnly){ %>
        <input type="button" id="superUserModeBtn" class="cmr-grid-btn-o" style="margin-left: 20px" onclick="cmr.superUserMode()" value="Super User Mode">
      <%} %> 
      <%if (("P".equals(autoEngineIndc) || "B".equals(autoEngineIndc)) && (user.isProcessor() || user.isCmde() || user.isAdmin())){ %>
        <input type="button" class="cmr-grid-btn-h" style="margin-left: 20px" onclick="Automation.viewResults()" value="View System Processing Results">
      <%}%>
        <input type="button" class="cmr-grid-btn" style="margin-left: 20px" onclick="CmrGrid.refresh('COMMENT_LIST_GRID')" value="Refresh Comments">
      <%if (!readOnly){%>
        <input type="button" class="cmr-grid-btn" style="margin-left: 20px" onclick="cmr.showModal('addCommentModal')" value="Add Comment">
      <%}%>
      </div>
    </div>

    <cmr:row>
      <cmr:column span="6">
        <cmr:grid url="/request/commentlog/list.json" id="COMMENT_LIST_GRID" span="6" usePaging="false" height="200">
          <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
          <cmr:gridCol width="150px" field="createTsString" header="<%=cmtDtHeader%>" />
          <cmr:gridCol width="26px" field="createById" header="&nbsp;" >
            <cmr:formatter functionName="commentImgFormatter" />
          </cmr:gridCol>
          <cmr:gridCol width="160px" field="createByNm" header="${ui.grid.cmtEnteredBy}" />
          <cmr:gridCol width="auto" field="cmt" header="${ui.grid.cmt}">
            <cmr:formatter functionName="commentFormatter" />
          </cmr:gridCol>
        </cmr:grid>
      </cmr:column>
    </cmr:row>

    <cmr:row>
      &nbsp;
    </cmr:row>

  </cmr:section>
</cmr:boxContent>

<!--  Embedded Modal for Adding Comments -->
<cmr:modal title="Add Comment" id="addCommentModal" widthId="570">
  <cmr:row>
    <cmr:column span="3" width="500">
      <cmr:label fieldId="addCommentTA" cssClass="cmr-status-cmt">
          Comment:
          <cmr:memoLimit maxLength="2000" fieldId="addCommentTA" />
      </cmr:label>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="3">
      <textarea id="addCommentTA" rows="5" cols="50" name="addCommentTA" autocomplete="off"></textarea>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.save}" onClick="doAddComment()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('addCommentModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
