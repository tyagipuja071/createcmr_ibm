<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<style>
#quickSearchModal div.ibm-columns {
  width: 530px !important;
}
div#quick-new-det {
  font-weight:bold;
  padding: 15px;
  padding-left: 25px;
  text-transform: uppercase;
}
</style>

<cmr:modal title="Create new Request" id="quickSearchModal" widthId="570">
  
    <cmr:row>
      <cmr:column span="4" width="520">
          <span class="quick-choice">
            A new Create request will be started with the following information and address:
            <div id="quick-new-det"></div>
          </span>
      </cmr:column>
    </cmr:row>
    <cmr:buttonsRow>
      <cmr:hr />
      <cmr:button label="Create Request" onClick="_inscp.proceedCreateRequest()" highlight="true" />
      <cmr:button label="Cancel" onClick="cmr.hideModal('quickSearchModal')" highlight="false" pad="true" />
    </cmr:buttonsRow>
  
</cmr:modal>
