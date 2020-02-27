<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>


<!--  Modal for the Address Verification Rules modal -->
<cmr:modal title="${ui.title.addressverification}" id="addressVerificationModal" widthId="570">
  <cmr:row>
    <cmr:column span="3" width="520">
      ${ui.addressverificationtext}
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="30">
    <cmr:column span="3" width="520">
      <input type="checkbox" id="addrVerAgree" dojoType="dijit.form.CheckBox" name="addrVerAgree" value="Y">
      ${ui.addressverificationagree}
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.ok}" onClick="doAcceptAddressVerification()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="doCancelAddressVerification()" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
