<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script src="${resourcesPath}/js/system/system.js?${cmrv}"></script>

<style>
  a.auto-link {
    font-weight: bold;
    font-size: 13px;
    letter-spacing: 1px;
  }
  img.auto-icon {
    width: 20px;
    height: 20px;
    vertical-align: middle;
  }
  div.auto-cont {
    margin-top: 20px
  }
  span.header {
    font-size: 14px;
    letter-spacing: 1px;
  }
</style>
<cmr:boxContent>
  <cmr:tabs />
  
  <cmr:section>

  <cmr:row>
    <cmr:column span="6">
      <h3>Automation Engine Maintenance</h3>
    </cmr:column>
  </cmr:row>
  <cmr:row>
   <cmr:column span="6">
    <p>
      <span class="header">
        Choose between maintaining Automation Engine configurations via engine level or country level, or define scenario exceptions.
      </span>
    </p>
   </cmr:column>
  </cmr:row>
  
  <cmr:row>
   <cmr:column span="6">
     <div class="auto-cont">
       <img src="${resourcesPath}/images/engine.png" class="auto-icon">
       <a class="auto-link" href="javascript: goToUrl('${contextPath}/auto/config/list')">Maintain by Engine Configuration</a>
     </div>
   </cmr:column>
   <cmr:column span="6">
     <div class="auto-cont">
       <img src="${resourcesPath}/images/engine.png" class="auto-icon">
       <a class="auto-link" href="javascript: goToUrl('${contextPath}/auto/config/cntry')">Maintain by Country Configuration</a>
     </div>
   </cmr:column>
   <cmr:column span="6">
     <div class="auto-cont">
       <img src="${resourcesPath}/images/engine.png" class="auto-icon">
       <a class="auto-link" href="javascript: goToUrl('${contextPath}/auto/config/exceptions')">Define Scenario Exceptions</a>
     </div>
   </cmr:column>
  </cmr:row>
  <cmr:row>
    &nbsp;
  </cmr:row>

  </cmr:section>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="Back to Code Maintenance Home"
      onClick="backToCodeMaintHome()" />
  </cmr:buttonsRow>
</cmr:section>
