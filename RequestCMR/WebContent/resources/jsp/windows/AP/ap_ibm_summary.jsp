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
<cmr:view forGEO="AP">

  <cmr:row addBackground="false" topPad="10">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130" >
      <label><cmr:fieldLabel fieldId="ProvinceName" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240" >
      <% 
        String prname = DropdownListController.getDescription("ProvinceName",data.getBusnType(), cntry);
      %>
      <%=prname != null ? prname : ""%>
    </cmr:column>
    <cmr:column span="1" width="130" >
      <label><cmr:fieldLabel fieldId="ProvinceCode" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String prcd = DropdownListController.getDescription("ProvinceCode", data.getTerritoryCd(), cntry);
      %>
      <%=prcd != null ? prcd : ""%>
    </cmr:column>

  </cmr:row>

  <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="GovIndicator" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String gInd = DropdownListController.getDescription("GovIndicator", data.getGovType(), cntry);
      %>
      <%=gInd != null ? gInd : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SalRepNameNo" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String salesRep = DropdownListController.getDescription("SalRepNameNo", data.getRepTeamMemberNo(), cntry);
      %>
      <%=salesRep != null ? salesRep : ""%>
    </cmr:column>
  </cmr:row>
  
   <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ISBU" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String isbu = DropdownListController.getDescription("ISBU", data.getIsbuCd(), cntry);
      %>
      <%=isbu != null ? isbu : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="Sector" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String sr = DropdownListController.getDescription("Sector", data.getSectorCd(), cntry);
      %>
      <%=sr != null ? sr : ""%>
    </cmr:column>

  </cmr:row>
  <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="IndustryClass" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
       
        String  value = data.getSubIndustryCd();
        String indCd =  value != null ? value.substring(0,1) : data.getSubIndustryCd();
        String indClass = DropdownListController.getDescription("IndustryClass",indCd , cntry);
      %>
      <%=indClass != null ? indClass : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="MrcCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String mrc = DropdownListController.getDescription("MrcCd", data.getMrcCd(), cntry);
      %>
      <%=mrc != null ? mrc : ""%>
    </cmr:column>

  </cmr:row>
  
   <cmr:row>
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="RegionCode" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String rc = DropdownListController.getDescription("RegionCode", data.getMiscBillCd(), cntry);
      %>
      <%=rc != null ? rc : ""%>
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CollectionCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
      <%
        String collCd = DropdownListController.getDescription("CollectionCd", data.getCollectionCd(), cntry);
      %>
      <%=collCd != null ? collCd : ""%>
    </cmr:column>

  </cmr:row>
  
<cmr:row>
<cmr:column span="1" width="127" >
    </cmr:column>
    <cmr:column span="1" width="130" forCountry="778,834">
      <label><cmr:fieldLabel fieldId="CustBillingContactNm" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String custBillNme = DropdownListController.getDescription("CustBillingContactNm", data.getContactName1(), cntry);
      %>
      <%=custBillNme != null ? custBillNme : ""%>
    </cmr:column>
     </cmr:row>
     <cmr:row>
    <cmr:column span="1" width="127" >
    </cmr:column>
    <cmr:column span="1" width="130" forCountry="796">
      <label><cmr:fieldLabel fieldId="CustomerServiceCd" />:</label>
    </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String cusCd = DropdownListController.getDescription("CustomerServiceCd", data.getEngineeringBo(), cntry);
      %>
      <%=cusCd != null ? cusCd : ""%>
    </cmr:column>
    

  </cmr:row>
   

</cmr:view>
