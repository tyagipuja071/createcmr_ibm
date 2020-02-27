<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<cmr:modal title="${ui.title.viewFile}" id="viewAttachmentModal" widthId="570">
  <cmr:row>
    <cmr:column span="1" width="100">
      <p>
        <cmr:label fieldId="filename_v">${ui.filename}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="3" width="420">
      <div id="filename_v">-</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="1" width="100">
      <p>
        <cmr:label fieldId="docContent_v">${ui.content}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="3" width="420">
      <div id="docContent_v">-</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="1" width="100">
      <p>
        <cmr:label fieldId="attachById_v">${ui.attachby}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="3" width="420">
      <div id="docAttachByNm_v">-</div>
      <div id="docAttachById_v">-</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="1" width="100">
      <p>
        <cmr:label fieldId="attachOn_v">${ui.attachon}:</cmr:label>
      </p>
    </cmr:column>
    <cmr:column span="3" width="420">
      <div id="attachOn_v">-</div>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="2" width="500">
      <p>
        <cmr:label fieldId="attach_cmt">${ui.attachcmt}:
          </cmr:label>
        <textarea id="attachcmt_v" rows="3" cols="50" readonly></textarea>
      </p>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.close}" onClick="cmr.hideModal('viewAttachmentModal')" highlight="true" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
