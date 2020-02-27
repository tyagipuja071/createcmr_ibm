<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>


<!--  Modal for template multiple values modal -->
<cmr:modal title="${ui.title.templatevalue}" id="templateValueModal" widthId="390">
  <cmr:row>
    <cmr:column span="2" width="350">
    The value for this field has been restricted to the choices below. 
    Please select the appropriate value for <strong><span id="templatevaluename">&nbsp;</span></strong>.
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <select name="templatevalues" id="templatevalues" style="width:340px">
      <option/>
    </select>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.ok}" onClick="TemplateService.chooseTemplateValue()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('templateValueModal')" pad="true"/>
  </cmr:buttonsRow>
</cmr:modal>
