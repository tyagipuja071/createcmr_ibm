<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/system.js?${cmrv}" type="text/javascript"></script>
<script>
  dojo.addOnLoad(function() {
  });
</script>
<cmr:boxContent>
  <cmr:tabs />

    <cmr:section>
      <cmr:row topPad="10" addBackground="true">
        <cmr:column span="6">
          <span style="color:red;font-size:16px;font-weight:bold">
            You do not have authority to access this page. The incident will be logged.          
          </span>
        </cmr:column>
      </cmr:row>
    </cmr:section>
</cmr:boxContent>
