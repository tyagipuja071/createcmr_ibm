<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<style>
#addressStdResultModal div.ibm-columns {
  width: 710px !important;
}

</style>
<!--  Modal for the address details creen -->
<cmr:modal title="${ui.title.addrStdResult}" id="addressStdResultModal" widthId="750">

  <cmr:row>
    <cmr:column span="1">
      <cmr:label fieldId="result_view">${ui.result}:</cmr:label>
    </cmr:column>
    <cmr:column span="2">
      <div id="result_view"></div>
    </cmr:column>

  </cmr:row>
  
  <cmr:row addBackground="true">
    <cmr:column span="2">
      <cmr:label fieldId="chngData_view">${ui.chngData}:</cmr:label>
    </cmr:column>
  </cmr:row>
  
  <cmr:row addBackground="true">
    <cmr:column span="1" width="130">
      <cmr:label fieldId="address_view">${ui.address}:</cmr:label>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="stdAddrTxt_view"></div>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="addrTxt_view"></div>
    </cmr:column>
  </cmr:row>    
  <cmr:row addBackground="true">
    <cmr:column span="1" width="130">
      <cmr:label fieldId="city_view">${ui.city}:</cmr:label>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="stdCity_view"></div>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="city1_view"></div>
    </cmr:column>
  </cmr:row>    
  <cmr:row addBackground="true">
    <cmr:column span="1" width="130">
      <cmr:label fieldId="statProvince_view">${ui.statProv}:</cmr:label>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="stdStateProv_view"></div>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="stateProv_view"></div>
    </cmr:column>
  </cmr:row>    
  <cmr:row addBackground="true">
    <cmr:column span="1" width="130">
      <cmr:label fieldId="postalCode_view">${ui.postalCode}:</cmr:label>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="stdPostCd_view"></div>
    </cmr:column>
    <cmr:column span="1" width="250">
      <div id="postCd_view"></div>
    </cmr:column>
  </cmr:row>

  

  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.acceptChanges}" onClick="doAcceptChanges()" highlight="true" />
    <cmr:info text="${ui.info.acceptChanges}" />
    <cmr:button label="${ui.btn.rejectChanges}" onClick="doRejectChanges()" highlight="false" pad="true"/>
    <cmr:button label="${ui.btn.cancel}" onClick="cancelStdResult()" highlight="false" pad="true" />
  </cmr:buttonsRow>


</cmr:modal>