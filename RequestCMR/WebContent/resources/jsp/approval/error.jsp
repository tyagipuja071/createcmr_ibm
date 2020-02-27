<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script>
  cmr.NOSESSIONCHECK = true;
</script>
<cmr:boxContent>
  <cmr:tabs />
  <cmr:section>
    <h3>
      A system error has occurred during the processing. Pls try again. If the error persists, please contact CCM Worldwide Support/Raleigh/Contr/IBM.
    </h3>
  </cmr:section>
</cmr:boxContent>
