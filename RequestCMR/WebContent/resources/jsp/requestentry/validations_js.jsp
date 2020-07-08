<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script src="${resourcesPath}/js/requestentry/geohandler.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/validators/ww_validations.js?${cmrv}" type="text/javascript"></script>

<!-- GEO Specific javascripts here -->
<cmr:view forCountry="897">
  <script src="${resourcesPath}/js/requestentry/validators/us_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>


<cmr:view forGEO="LA">
  <script src="${resourcesPath}/js/requestentry/validators/la_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="LA">
  <script src="${resourcesPath}/js/requestentry/taxinfo.js?${cmrv}" type="text/javascript"></script>
  <script src="${resourcesPath}/js/requestentry/contactinfo.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="EMEA">
  <cmr:view forCountry="862">
  <script src="${resourcesPath}/js/requestentry/validators/emea_validations_tr.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="726">
  <script src="${resourcesPath}/js/requestentry/validators/emea_validations_gr.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="666">
  <script src="${resourcesPath}/js/requestentry/validators/emea_validations_cy.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view exceptForCountry="726,666,862">
  <script src="${resourcesPath}/js/requestentry/validators/emea_validations.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
</cmr:view>
<cmr:view forCountry="758">
  <script src="${resourcesPath}/js/requestentry/validators/sr_import.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="CND">
  <script src="${resourcesPath}/js/requestentry/validators/cnd_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="724">
  <script src="${resourcesPath}/js/requestentry/validators/de_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="AP">
  <script src="${resourcesPath}/js/requestentry/validators/ap_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="MCO">
  <script src="${resourcesPath}/js/requestentry/validators/mco_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="MCO1">
  <script src="${resourcesPath}/js/requestentry/validators/mco1_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="MCO2">
  <script src="${resourcesPath}/js/requestentry/validators/mco2_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="FR">
  <script src="${resourcesPath}/js/requestentry/validators/fr_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="JP">
  <script src="${resourcesPath}/js/requestentry/validators/jp_validations.js?${cmrv}" type="text/javascript"></script>
  <script src="${resourcesPath}/js/requestentry/validators/jp_import.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="CEMEA">
  <script src="${resourcesPath}/js/requestentry/validators/cemea_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="CN">
  <script src="${resourcesPath}/js/requestentry/validators/cn_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="NORDX">
  <script src="${resourcesPath}/js/requestentry/validators/nordx_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="BELUX">
  <script src="${resourcesPath}/js/requestentry/validators/belux_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="NL">
  <script src="${resourcesPath}/js/requestentry/validators/nl_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="SWISS">
  <script src="${resourcesPath}/js/requestentry/validators/swiss_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

