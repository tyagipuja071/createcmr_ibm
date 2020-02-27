<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!--  Modal for the address details creen -->
<cmr:modal title="${ui.title.addrStdResult}" id="addressStdGeneralResultModal" widthId="390">

  <cmr:row>
   
      <div id="generalStdResult_view"></div>

  </cmr:row>
  
  

  

  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.close}" onClick="doCloseGeneralStdResult()" highlight="true" />
  </cmr:buttonsRow>


</cmr:modal>