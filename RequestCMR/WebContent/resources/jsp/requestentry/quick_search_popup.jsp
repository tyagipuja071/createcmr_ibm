<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<style>
div#qs_pop {
  border : 1px Solid Black;
  border-radius : 5px;
  font-size: 15px;
  position: fixed;
  bottom: 5px;
  right : 5px;
  width: 400px;
  padding: 10px;
  z-index: 9999;
  background: #a9db80; /* Old browsers */
  background: -moz-linear-gradient(top,  #a9db80 0%, #96c56f 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top,  #a9db80 0%,#96c56f 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom,  #a9db80 0%,#96c56f 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#a9db80', endColorstr='#96c56f',GradientType=0 ); /* IE6-9 */
  font-family : IBM Plex Sans;
  text-shadow: none;
}
button.qs_pop-btn {  
  border: 1px Solid Black;
  border-radius: 2px;
  min-width: 25px;
  cursor: pointer;
}
button.qs_pop-btn:hover {
  background: #CCCCCC;
}
</style>
<div id="qs_pop" style="display:none">
  D&B Search is required for create requests under this country. Quick Search is suggested for better searching
  against both current CMRs and D&B. <br><br>Open Quick Search?<br>
  <button class="qs_pop-btn" onclick="openQuickSearchDirect()">Yes</button>
  <button class="qs_pop-btn" onclick="cmr.hideNode('qs_pop')">No</button>
</div>