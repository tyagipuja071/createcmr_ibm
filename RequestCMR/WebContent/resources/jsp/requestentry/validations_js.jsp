<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
String processingType = PageManager.getProcessingType(reqentry.getCmrIssuingCntry(), reqentry.getReqType());
%>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

<script src="${resourcesPath}/js/requestentry/geohandler.js?${cmrv}" type="text/javascript"></script>
<script src="${resourcesPath}/js/requestentry/validators/ww_validations.js?${cmrv}" type="text/javascript"></script>

<!-- GEO Specific javascripts here -->
<cmr:view forCountry="897">
  <script src="${resourcesPath}/js/requestentry/validators/us_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>


<cmr:view forGEO="LA" exceptForCountry="631">
  <script src="${resourcesPath}/js/requestentry/validators/la_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="LA">
  <script src="${resourcesPath}/js/requestentry/taxinfo.js?${cmrv}" type="text/javascript"></script>
  <script src="${resourcesPath}/js/requestentry/contactinfo.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<!-- EMEA -->
<cmr:view forGEO="EMEA">
 <cmr:view forCountry="862">
       <script src="${resourcesPath}/js/requestentry/validators/tr_validations.js?${cmrv}"
        type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="726">
  <script src="${resourcesPath}/js/requestentry/validators/gr_validations.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="666">
  <script src="${resourcesPath}/js/requestentry/validators/cy_validations.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="866,754">
  <script src="${resourcesPath}/js/requestentry/validators/uki_validations.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="755">
    <%  if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) { %>  
  		<script src="${resourcesPath}/js/requestentry/validators/il_validations.js?${cmrv}" type="text/javascript"></script>
    <%  }  else { %>  
      	<script src="${resourcesPath}/js/requestentry/validators/emea_validations.js?${cmrv}" type="text/javascript"></script>
    <%  } %>
  </cmr:view>
  <cmr:view exceptForCountry="726,666,862,755,866,754,758">
  <script src="${resourcesPath}/js/requestentry/validators/emea_validations.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="754">
  <script src="${resourcesPath}/js/requestentry/licenses.js?${cmrv}" type="text/javascript"></script>
  </cmr:view>
  <cmr:view forCountry="758">
 <script src="${resourcesPath}/js/requestentry/validators/italy_validations.js?${cmrv}"
 type="text/javascript"></script>
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

<cmr:view forGEO="AP" exceptForCountry="616,796,778,749,818,852,744,615,652,736,834,856,738">
  <script src="${resourcesPath}/js/requestentry/validators/ap_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="738">
  <script src="${resourcesPath}/js/requestentry/validators/hk_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="736">
  <script src="${resourcesPath}/js/requestentry/validators/mo_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="778">
  <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/my_validations_dr.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/my_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forCountry="749">
  <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/id_validations_dr.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/id_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forCountry="818">
  <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/ph_validations_dr.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/ph_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forCountry="781">
	<script
		src="${resourcesPath}/js/requestentry/validators/mx_validations.js?${cmrv}"
		type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="852">
  <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/vn_validations_dr.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/vn_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forCountry="616">
  <script src="${resourcesPath}/js/requestentry/validators/au_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="796">
  <script src="${resourcesPath}/js/requestentry/validators/nz_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="MCO">
  <script src="${resourcesPath}/js/requestentry/validators/mco_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="MCO1">
  <script src="${resourcesPath}/js/requestentry/validators/mco1_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="MCO2" exceptForCountry="780">
  <%  if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/mco2_validations_ld.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
	<script src="${resourcesPath}/js/requestentry/validators/mco2_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forGEO="MCO2" forCountry="780">
  <script src="${resourcesPath}/js/requestentry/validators/malta_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="FR">
  <script src="${resourcesPath}/js/requestentry/validators/fr_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forGEO="JP">
    <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
    	<script src="${resourcesPath}/js/requestentry/validators/jp_validations_dr.js?${cmrv}" type="text/javascript"></script>
    	<script src="${resourcesPath}/js/requestentry/validators/jp_import_dr.js?${cmrv}" type="text/javascript"></script>
    <%  }  else { %>  
  		<script src="${resourcesPath}/js/requestentry/validators/jp_validations.js?${cmrv}" type="text/javascript"></script>
    	<script src="${resourcesPath}/js/requestentry/validators/jp_import.js?${cmrv}" type="text/javascript"></script>
    <%  } %>
</cmr:view>

<cmr:view forGEO="CEMEA">
	<cmr:view
		forCountry="620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729">
		<script
			src="${resourcesPath}/js/requestentry/validators/me_validations.js?${cmrv}"
			type="text/javascript"></script>
	</cmr:view>
	<cmr:view
		exceptForCountry="620,642,675,677,680,752,762,767,768,772,805,808,823,832,849,850,865,729,618,358,359,363,603,607,626,644,651,668,693,694,695,699,704,705,707,708,713,740,741,787,820,821,826,829">
		<script
			src="${resourcesPath}/js/requestentry/validators/cemea_validations.js?${cmrv}"
			type="text/javascript"></script>
	</cmr:view>
</cmr:view>
<cmr:view
	forCountry="358,359,363,603,607,626,644,651,668,693,694,695,699,704,705,707,708,713,740,741,787,820,821,826,889">
	<script
		src="${resourcesPath}/js/requestentry/validators/cee_validations.js?${cmrv}"
		type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="618">
	<script
		src="${resourcesPath}/js/requestentry/validators/at_validations.js?${cmrv}"
		type="text/javascript"></script>
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

<cmr:view forCountry="649">
  <script src="${resourcesPath}/js/requestentry/validators/ca_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="858">
  <script src="${resourcesPath}/js/requestentry/validators/tw_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="766">
  <script src="${resourcesPath}/js/requestentry/validators/kr_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="744">
  <script src="${resourcesPath}/js/requestentry/validators/in_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="643">
  <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
  <script src="${resourcesPath}/js/requestentry/validators/bn_validations_dr.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
  <script src="${resourcesPath}/js/requestentry/validators/bn_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forCountry="615">
  <script src="${resourcesPath}/js/requestentry/validators/bd_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="652">
  <script src="${resourcesPath}/js/requestentry/validators/lk_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>

<cmr:view forCountry="834">
  <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/sg_validations_dr.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/sg_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forCountry="856">
  <%  if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/th_validations_dr.js?${cmrv}" type="text/javascript"></script>
  <%  }  else { %>  
  	<script src="${resourcesPath}/js/requestentry/validators/th_validations.js?${cmrv}" type="text/javascript"></script>
  <%  } %>
</cmr:view>

<cmr:view forCountry="631">
<script src="${resourcesPath}/js/requestentry/validators/br_validations.js?${cmrv}" type="text/javascript"></script>
</cmr:view>