<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script>
  dojo.addOnLoad(function() {
    FilteringDropdown.loadItems('docContent', 'docContent_spinner', 'lov', 'fieldId=DocContent');
    FormManager.addValidator('filename', Validators.REQUIRED, [ '${ui.filename}' ]);
    FormManager.addValidator('docContent', Validators.REQUIRED, [ '${ui.content}' ]);
  });
</script>


<!--  Modal for the Status Change Screen -->
<cmr:modal title="${ui.title.addFile}" id="addAttachmentModal" widthId="570">
  <form:form method="POST" action="${contextPath}/request/attach/process" name="frmCMRAttachAdd" class="ibm-column-form ibm-styled-form" modelAttribute="attach" id="frmCMR_addAttachmentModal" enctype="multipart/form-data">
    <cmr:modelAction formName="frmCMR_addAttachmentModal" />
    <form:hidden path="reqId" id="attach_reqId" value="${reqentry.reqId}" />
    <form:hidden path="attachMode" id="attachmentmode" />
    <cmr:row>
      <cmr:column span="3">
        <jsp:include page="../templates/messages_modal.jsp">
          <jsp:param value="addAttachmentModal" name="modalId" />
        </jsp:include>
      </cmr:column>
    </cmr:row>
    <div id="attachmentscreenshot">
    <cmr:row>
      <cmr:column span="3">
        <cmr:info text="After capturing and copying a screenshot, press Ctrl-V on this screen to add the image."></cmr:info>
        <cmr:note text="After capturing and copying a screenshot, press Ctrl-V on this screen to add the image."></cmr:note>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1">
        <p>
          <cmr:label fieldId="screenshot">Image:</cmr:label>
        </p>
      </cmr:column>
      <cmr:column span="2">
          <img src="${resourcesPath}/images/no-image.png" id="pasteimg" style="width:150px;height:150px">
          <textarea name="imgContent" style="display:none" id="imgContent"></textarea>
      </cmr:column>
    </cmr:row>
    </div>
    <div id="attachmentfilename">
    <cmr:row>
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="filename">${ui.filename}:</cmr:label>
          <input type="file" id="filename" name="filecontent">
        </p>
      </cmr:column>
    </cmr:row>
    </div>
    <cmr:row>
      <cmr:column span="2">
        <p>
          <cmr:label fieldId="docContent">${ui.content}:</cmr:label>
          <form:select dojoType="dijit.form.FilteringSelect" id="docContent" searchAttr="name" style="display: block;" maxHeight="200" required="false" path="docContent" placeHolder="Select a Content Type"></form:select>
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="2" width="500">
        <p>
          <cmr:label fieldId="attach_cmt">${ui.attachcmt}:
        <cmr:memoLimit maxLength="250" fieldId="attach_cmt" />
          </cmr:label>
          <form:textarea path="cmt" id="attach_cmt" rows="5" cols="50" />
        </p>
      </cmr:column>
      <input type="hidden" name="attachToken" id="attachToken">
    </cmr:row>
    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="${ui.btn.saveattachment}" onClick="addAttachment()" highlight="true" />
      <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('addAttachmentModal')" highlight="false" pad="true" />
    </cmr:buttonsRow>
  </form:form>
</cmr:modal>
