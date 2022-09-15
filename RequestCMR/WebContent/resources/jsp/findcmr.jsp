<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script src="${resourcesPath}/js/angular.min.js"></script>
<script src="${resourcesPath}/js/angular-route.min.js"></script>
<script src="${resourcesPath}/js/angular-sanitize.min.js"></script>
<script src="${resourcesPath}/js/ext/jquery-3.6.0.js"></script>
<script src="${resourcesPath}/js/ext/typeahead.bundle.js"></script>
<script src="${resourcesPath}/js/ext/dnb_utilities.js"></script>
<link rel="stylesheet" href="${resourcesPath}/css/quick_search.css?${cmrv}"/>
<%
AppUser user = AppUser.getUser(request);
%> 
<script>
var bypassqs = true;
</script>
<style>

div.findcmr {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  padding: 10px;
}
</style>
<cmr:boxContent>
  <cmr:tabs />
<cmr:section>
    <div ng-app="QuickSearchApp" ng-controller="FindCMRController">
      <div class="findcmr">{{hello}}</div>
    </div>
</cmr:section>
</cmr:boxContent>

<script src="${resourcesPath}/js/quick_search.js?${cmrv}"></script>

  