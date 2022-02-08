<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>


<!--  Modal for the Address Verification Rules modal -->
<cmr:modal title="${ui.title.addressverification}"
	id="addressVerificationModal" widthId="570">
	<cmr:row>
		<cmr:column span="3" width="520">
      ${ui.addressverificationtext}
    </cmr:column>
	</cmr:row>
	
	<div style="display:none;" id="personalInformationDiv">
	<br><b>${ui.title.personalinformation}</b>
	<cmr:row>
		<cmr:column span="3" width="520">
      ${ui.personalinformationtext}
    </cmr:column>
	</cmr:row>
	</div>
	
	<cmr:row topPad="20">
		<cmr:column span="3" width="520">
			<input type="checkbox" id="addrVerAgree"
				dojoType="dijit.form.CheckBox" name="addrVerAgree" value="Y">
      ${ui.addressverificationagree}
    </cmr:column>
	</cmr:row>
	<div style="display:none;" id="dupCMRReasonDiv">
	<br><b>Duplicate CMR Override Reason</b>
	<cmr:row topPad="20">
		<cmr:column span="3" width="520"> 
      Please provide a reason for proceeding with duplicate CMR creation.
      <br><b>Note: </b>This action will trigger approvals.
    </cmr:column>
    </cmr:row>
	<cmr:row>
		<cmr:column span="3">
			<textarea id="dupCmrRsn" rows="5" cols="50"></textarea>
		</cmr:column>
	</cmr:row>
	<br>
	</div>
	<cmr:buttonsRow>
		<cmr:hr />
		<cmr:button label="${ui.btn.ok}"
			onClick="doAcceptAddressVerification()" highlight="true" />
		<cmr:button label="${ui.btn.cancel}"
			onClick="doCancelAddressVerification()" highlight="false" pad="true" />
	</cmr:buttonsRow>
</cmr:modal>
