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
div#supportal {
  border : 1px Solid Black;
  border-radius : 5px;
  font-size: 15px;
  position: fixed;
  bottom: 5px;
  right : 5px;
  width: 300px;
  padding: 10px;
  z-index: 9999;
  background: rgb(167,199,220); /* Old browsers */
  background: -moz-linear-gradient(top, rgba(167,199,220,1) 0%, rgba(133,178,211,1) 100%); /* FF3.6-15 */
  background: -webkit-linear-gradient(top, rgba(167,199,220,1) 0%,rgba(133,178,211,1) 100%); /* Chrome10-25,Safari5.1-6 */
  background: linear-gradient(to bottom, rgba(167,199,220,1) 0%,rgba(133,178,211,1) 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
  filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#a7c7dc', endColorstr='#85b2d3',GradientType=0 );
  font-family : IBM Plex Sans;
  text-shadow: none;
}
button.supportal-btn {  
  border: 1px Solid Black;
  border-radius: 2px;
  min-width: 25px;
  cursor: pointer;
}
button.supportal-btn:hover {
  background: #CCCCCC;
}
</style>
<div id="supportal" style="display:none">
  It seems you have encountered some error on the application. Do you want to open the support 
  site and report the issue?<br>
  <button class="supportal-btn" onclick="openCISupportal()">Yes</button>
  <button class="supportal-btn" onclick="cmr.hideNode('supportal')">No</button>
</div>