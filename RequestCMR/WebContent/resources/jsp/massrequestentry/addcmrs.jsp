<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request
          .getAttribute("reqentry");
%>
<style>
  span.role {
    margin-left: 20px;
  }
</style>
<!--  Modal for adding CMRs -->
<cmr:modal title="Please input the list of CMR# values" id="addCMRsModal" widthId="390">
  
  <cmr:row>
    <cmr:column span="2" width="350">
    <span class="ibm-item-note" style="padding-left:10px">
            Each value should be separated by a comma,
        </span><br>
        <span class="ibm-item-note" style="padding-left:10px">
          and/or be placed in new lines.
        </span>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <cmr:column span="2" width="350">
      <textarea id="cmrList" name="cmrList" rows="10" cols="33"></textarea>
    </cmr:column> 
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.ok}" onClick="doAddToCMRList()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('addCMRsModal')" pad="true"/>
  </cmr:buttonsRow>
</cmr:modal>
