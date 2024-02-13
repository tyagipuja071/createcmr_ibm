<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<style>
div#ibm-leadspace-body {
	display: none !important;
}
}
</style>
<div class="ibm-columns">

	<div class="ibm-col-1-1"></div>
	<div class="ibm-col-1-1"
		style="padding-bottom: 20px; margin-left: 100px;">
		<div class="ibm-col-7-1">
			<img src="/CreateCMR/resources/images/CreateCMRLogo.png"
				style="width: 60px; height: 60px; border-radius: 8px; margin-right: 10px">
		</div>
		<div class="ibm-col-3-1">
			<h1 style="width: 100%; display: inline-block; vertical-align: top">You
				were successfully logged out</h1>
			Please close this window or <a href='login'>log in again</a>.
		</div>
	</div>


</div>