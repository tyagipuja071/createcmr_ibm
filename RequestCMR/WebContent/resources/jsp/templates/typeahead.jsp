<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<cmr:modal title="Search Codes" id="typeaheadModal" widthId="390">
	<cmr:row>
		<cmr:column span="3">
    <cmr:note text="${ui.note.typeahead}"></cmr:note>
        <input id="typeaheadField" style="width:320px" name="typeaheadField">
		</cmr:column>
	</cmr:row>
	<cmr:buttonsRow>
		<cmr:hr />
		<cmr:button label="${ui.btn.cancel}" onClick="cmr.closeSearchCode()"
			highlight="true" />
	</cmr:buttonsRow>
</cmr:modal>
