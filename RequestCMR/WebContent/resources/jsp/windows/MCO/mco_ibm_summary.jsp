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
<cmr:view forGEO="MCO,MCO1,MCO2">
  <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalRepNameNo" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String collCd = DropdownListController.getDescription("SalRepNameNo", data.getRepTeamMemberNo(), cntry);
      %>
      <%=collCd != null ? collCd : ""%>
    </cmr:column>
    
    <cmr:column span="1" width="130">
      <label>${ui.inactypecd}:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      ${summary.data.inacCd != null ? summary.data.inacCd : ""}
    </cmr:column>
  </cmr:row>
  <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalesBusOff" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String sbo = DropdownListController.getDescription("SalesBusOff", data.getSalesBusOffCd(), cntry);
      %>
      <%=sbo != null ? sbo : ""%>
    </cmr:column>
    <cmr:view exceptForGEO="MCO1,MCO2" exceptForCountry="822">
      <cmr:column span="1" width="130">
        <label><cmr:fieldLabel fieldId="EngineeringBo" />: </label>
      </cmr:column>
      <cmr:column span="1" width="200">
        <%
          String cebo = DropdownListController.getDescription("EngineeringBo", data.getEngineeringBo(), cntry);
        %>
        <%=cebo != null ? cebo : ""%>
      </cmr:column>
    </cmr:view>
    <cmr:view forGEO="MCO1,MCO2">
      <cmr:column span="1" width="130">
        <label><cmr:fieldLabel fieldId="InternalDept" />: </label>
      </cmr:column>
      <cmr:column span="1" width="130">
        ${summary.data.ibmDeptCostCenter}
      </cmr:column>
    </cmr:view>
  </cmr:row>
</cmr:view>
