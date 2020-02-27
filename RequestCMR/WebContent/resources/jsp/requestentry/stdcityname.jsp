<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>


<!--  Modal for the Address Verification Rules modal -->
<cmr:modal title="Confirm City Name" id="stdcitynameModal" widthId="390">
  <cmr:row>
    <cmr:column span="2" width="350">
      The current city name <strong><span id="currcityname2">&nbsp;</span></strong> does not match any standard city names the system can use. 
      The city name <strong><span id="stdcityname">&nbsp;</span></strong> closely matches this and is usable by the system. Proceed to use the suggested name?
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.ok}" onClick="doSelectStdCityName()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('stdcitynameModal')" pad="true"/>
  </cmr:buttonsRow>
</cmr:modal>
