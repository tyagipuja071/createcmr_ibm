<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script src="${resourcesPath}/js/requestentry/geohandler.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/validators_mass/ww_validations_mass.js?${cmrv}" type="text/javascript"></script>

<cmr:view forGEO="EMEA">
  <script src="${resourcesPath}/js/requestentry/validators_mass/emea_validations_mass.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="FR">
  <script src="${resourcesPath}/js/requestentry/validators_mass/fr_validations_mass.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="JP">
  <script src="${resourcesPath}/js/requestentry/validators_mass/jp_validations_mass.js?${cmrv}" type="text/javascript"></script>
</cmr:view>
