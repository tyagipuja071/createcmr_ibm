<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script src="${resourcesPath}/js/system/system.js?${cmrv}"></script>
<script>

function descFormatter(value, rowIndex) {
  return value.replace(/\n/gi,'<br>');
}

function idFormatter(value, rowIndex) {
  console.log(value.replace(/\n/gi,'<br>'));
  return '<a href="./maint?configId='+value+'">'+value+'</a>';
}
function dateFormatter(value, rowIndex) {
  var time = new Number(value);
  var dt = new Date(time);
  return moment(dt).format('YYYY-MM-DD hh:mm:ss');
}

</script>

<cmr:boxContent>
  <cmr:tabs />
  
  <cmr:section>

  <cmr:row>
    <cmr:column span="6">
      <h3>List of Automation Engine Configurations</h3>
    </cmr:column>
  </cmr:row>
  
  <cmr:row topPad="10">
    <cmr:column span="6">
      <cmr:grid url="/auto/config/getlist.json" id="CONFIG_LIST" span="6" height="400" useFilter="true">
        <cmr:gridCol width="15%" field="configId" header="ID">
          <cmr:formatter functionName="idFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="auto" field="configDefn" header="Description">
          <cmr:formatter functionName="descFormatter" />
        </cmr:gridCol>
        <cmr:gridCol width="12%" field="lastUpdtBy" header="Last Updated By" />
        <cmr:gridCol width="12%" field="lastUpdtTs" header="Last Update Date">
          <cmr:formatter functionName="dateFormatter" />
        </cmr:gridCol>
      </cmr:grid>
    </cmr:column>
  </cmr:row>
  <cmr:row>
    &nbsp;
  </cmr:row>

  </cmr:section>
</cmr:boxContent>
<cmr:section alwaysShown="true">
  <cmr:buttonsRow>
    <cmr:button label="New Configuration"
      onClick="goToUrl('${contextPath}/auto/config/maint')" highlight="true"/>
    <cmr:button label="Maintain by Country Configuration"
      onClick="goToUrl('${contextPath}/auto/config/cntry')" pad="true"/>
    <cmr:button label="Scenario Exceptions"
      onClick="goToUrl('${contextPath}/auto/config/exceptions')" pad="true"/>
    <cmr:button label="Back to Code Maintenance Home"
      onClick="backToCodeMaintHome()" pad="true"/>
  </cmr:buttonsRow>
</cmr:section>
