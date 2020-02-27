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
<cmr:view forGEO="FR">

  <cmr:row addBackground="false" topPad="10">
     <cmr:column span="1" width="127">
   <!--     <label>${ui.ibmInfo}:</label> -->
    </cmr:column>  
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ISU" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String isu = DropdownListController.getDescription("ISU", data.getIsuCd(), cntry);
      %>
      <%=isu != null ? isu : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ClientTier" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String clientTier = DropdownListController.getDescription("ClientTier", data.getClientTier(), cntry);
      %>
      <%=clientTier != null ? clientTier : ""%>
    </cmr:column>
  </cmr:row>

  <cmr:row>
    <!-- INAC Type/Code removed as per req. -->
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CollectionCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String collCd = DropdownListController.getDescription("CollectionCd", data.getCollectionCd(), cntry);
      %>
      <%=collCd != null ? collCd : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalesBusOff" />:</label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String sbo = DropdownListController.getDescription("SalesBusOff", data.getSalesBusOffCd(), cntry);
      %>
      <%=sbo != null ? sbo : ""%>
    </cmr:column>
  </cmr:row>

  <cmr:row>
  <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalRepNameNo" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String sr = DropdownListController.getDescription("SalRepNameNo", data.getRepTeamMemberNo(), cntry);
      %>
      <%=sr != null ? sr : ""%>
    </cmr:column>
  </cmr:row>

</cmr:view>
