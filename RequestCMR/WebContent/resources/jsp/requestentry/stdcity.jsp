<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>


<!--  Modal for the Address Verification Rules modal -->
<cmr:modal title="Select City and County" id="stdcityModal" widthId="390">
  <cmr:row>
    <cmr:column span="2" width="350">
      The current city name <strong><span id="currcityname">&nbsp;</span></strong> cannot be mapped directly to a proper City/County combination. Please choose
      the correct city and county from the list below.
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <select name="stcdity" id="stdcitynames" style="width:340px">
      <option/>
    </select>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.ok}" onClick="doSelectStdCity()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('stdcityModal')" pad="true"/>
  </cmr:buttonsRow>
</cmr:modal>
