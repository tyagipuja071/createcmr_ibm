<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
%>
<script type="text/javascript">
  dojo.addOnLoad(function() {
    if (FormManager) {
  	  if (FormManager.getActualValue('cmrIssuingCntry') == ''|| FormManager.getActualValue('cmrIssuingCntry') == null) {
        window.setTimeout('setNotifyNmRequiredOnDelay()', 1000);
      } else {
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');  
        if (cmrIssuingCntry == "760") {
          FormManager.removeValidator('notifNm', Validators.REQUIRED);
        } else {
          FormManager.addValidator('notifNm', Validators.REQUIRED, [ '${ui.notifNm}' ], 'MAIN_NOTIFY_TAB');
        }
        FormManager.addValidator('notifNm', Validators.BLUEPAGES, [ '${ui.notifNm}' ], 'MAIN_NOTIFY_TAB');
        FormManager.ready();
      }
    }
  });
  
  function setNotifyNmRequiredOnDelay() {
    var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cmrIssuingCntry == "760") {
      FormManager.removeValidator('notifNm', Validators.REQUIRED);
    } else {
      FormManager.addValidator('notifNm', Validators.REQUIRED, [ '${ui.notifNm}' ], 'MAIN_NOTIFY_TAB');
    }
    FormManager.addValidator('notifNm', Validators.BLUEPAGES, [ '${ui.notifNm}' ], 'MAIN_NOTIFY_TAB');
    FormManager.ready();
  }
</script>
<form:form method="POST" action="${contextPath}/request/notify/process" name="frmCMRNotif" class="ibm-column-form ibm-styled-form" modelAttribute="notif" id="frmCMRNotif">
  <cmr:modelAction formName="frmCMRNotif" />
  <cmr:section id="NOTIFY_REQ_TAB" hidden="true">
    <jsp:include page="detailstrip.jsp" />
    <cmr:row addBackground="true">
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="notifylist">
              ${ui.notifylist}:
          <cmr:info text="${ui.info.notifylist}" />
          </cmr:label>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="4">
        <cmr:grid url="/request/notify/list.json" id="NOTIFY_LIST_GRID" span="4" usePaging="false" height="250">
          <cmr:gridParam fieldId="reqId" value="${reqentry.reqId}" />
          <cmr:gridCol width="150px" field="notifNm" header="${ui.grid.notifNm}" />
          <cmr:gridCol width="150px" field="notifId" header="${ui.grid.notifId}" />
          <cmr:gridCol width="100px" field="noEmail" header="${ui.grid.optOut}" />
          <cmr:gridCol width="auto" field="actions" header="${ui.grid.actions}">
<%if (!readOnly) {%>
            <cmr:formatter functionName="notifyListActionsFormatter" />
<%}%>
          </cmr:gridCol>

        </cmr:grid>
      </cmr:column>
    </cmr:row>
<%if (!readOnly) {%>
    <cmr:row addBackground="true" topPad="15">
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="addNewNotif">
            <strong>${ui.addNewNotif}</strong>
            <cmr:info text="${ui.info.addNewNotif}" />
          </cmr:label>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
      <cmr:column span="3">
        <p>
          <cmr:label fieldId="notifNm">
              ${ui.notifNm}:
          </cmr:label>
        <div class="cmr-inline">
          <cmr:bluepages model="notif" namePath="notifNm" idPath="notifId" />
          <span style="padding-left: 5px">&nbsp;</span>
          <cmr:button label="${ui.btn.addNewNotif}" onClick="doAddToNotifyList()" />
        </div>
        </p>
      </cmr:column>
    </cmr:row>
<%} else {%>
    <cmr:row addBackground="true" topPad="15">
    </cmr:row>
<%} %>
  </cmr:section>
</form:form>