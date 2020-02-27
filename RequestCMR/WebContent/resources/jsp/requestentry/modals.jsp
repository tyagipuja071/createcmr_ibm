<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  String existing = (String) request.getParameter("isExisting");

%>

<%if ("E".equals(existing)){%>

<%-- Add here modals that will be added for existing requests --%>
<!--  LA Specific Modals -->
<cmr:view forGEO="LA">
  <jsp:include page="sucursalcollbranchoff.jsp" />
  <jsp:include page="addedittaxinfo.jsp" />
  <jsp:include page="taxinfodetails.jsp" />
      <jsp:include page="contactinfodetails.jsp" />
    <jsp:include page="addeditcontactinfo.jsp" />
</cmr:view>

<%} else if ("N".equals(existing)){ %>

<%-- Add here modals that will be added for new requests --%>

<%}%>

<%-- Add here modals that will be any requests --%>
<cmr:view forCountry="760">
  <jsp:include page="JP/jp_scenario.jsp" />
</cmr:view>
<cmr:view forCountry="758">
  <jsp:include page="EMEA/validate_fiscal_data.jsp" />
</cmr:view>
