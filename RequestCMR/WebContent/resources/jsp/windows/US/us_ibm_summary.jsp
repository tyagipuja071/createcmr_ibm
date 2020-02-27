<%@page import="com.ibm.cio.cmr.request.entity.Data"%>
<%@page import="com.ibm.cio.cmr.request.controller.DropdownListController"%>
<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.entity.Admin"%>
<%@page import="com.ibm.cio.cmr.request.entity.Addr"%>
<%@page import="com.ibm.cio.cmr.request.model.window.RequestSummaryModel"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestSummaryModel summary = (RequestSummaryModel) request.getAttribute("summary");
  Data data = summary.getData();
  Admin admin = summary.getAdmin();
  Addr addr = summary.getAddr();
  String cntry = data.getCmrIssuingCntry();
%>
<cmr:view forGEO="US">
  <cmr:row addBackground="false">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label> 
        <cmr:fieldLabel fieldId="MarketingDept" />: 
      </label>
    </cmr:column>
    <cmr:column span="1" width="240">
        ${summary.data.mktgDept} 
    </cmr:column>
    <cmr:column span="1" width="160">
      <label> 
        <cmr:fieldLabel fieldId="MarketingARDept" />: 
      </label>
    </cmr:column>
    <cmr:column span="1" width="170">
        ${summary.data.mtkgArDept}
    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="false">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label> 
        <cmr:fieldLabel fieldId="PCCMarketingDept" />: 
      </label>
    </cmr:column>
    <cmr:column span="1" width="240">
        ${summary.data.pccMktgDept} 
    </cmr:column>
    <cmr:column span="1" width="160">
      <label> 
        <cmr:fieldLabel fieldId="PCCARDept" />: 
      </label>
    </cmr:column>
    <cmr:column span="1" width="170">
        ${summary.data.pccArDept}
    </cmr:column>
  </cmr:row>

  <cmr:row addBackground="false">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label> 
        <cmr:fieldLabel fieldId="SVCTerritoryZone" />: 
      </label>
    </cmr:column>
    <cmr:column span="1" width="240">
        ${summary.data.svcTerritoryZone} 
    </cmr:column>
    <cmr:column span="1" width="160">
      <label> 
        <cmr:fieldLabel fieldId="SVCAROffice" />: 
      </label>
    </cmr:column>
    <cmr:column span="1" width="170">
        ${summary.data.svcArOffice}
    </cmr:column>
  </cmr:row>

</cmr:view>
