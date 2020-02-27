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
  <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AbbrevName" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.abbrevNm}
    </cmr:column>    
  </cmr:row>
  <cmr:row addBackground="true">
  <cmr:column span="1" width="127">
    </cmr:column>
  <cmr:column span="1" width="130">
        <label><cmr:fieldLabel fieldId="EmbargoCode" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
        ${summary.data.embargoCd}
      </cmr:column>
     <cmr:column span="1" width="90">
      <label><cmr:fieldLabel fieldId="SpecialTaxCd" />: </label>
     </cmr:column>
    <cmr:column span="1" width="240">
      <%
        String taxCd = DropdownListController.getDescription("SpecialTaxCd", data.getSpecialTaxCd(), cntry);
      %>
      <%=taxCd != null ? taxCd : ""%>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
   </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.abbrevLocn}
    </cmr:column>
     <cmr:column span="1" width="110" exceptForGEO="MCO1,MCO2">
      <label><cmr:fieldLabel fieldId="ModeOfPayment" />: </label>
    </cmr:column>
    <cmr:column span="1" width="100" exceptForGEO="MCO1,MCO2">
          ${summary.data.modeOfPayment}
    </cmr:column>
    <cmr:view forGEO="MCO1">
      <cmr:column span="1" width="110">
        <label><cmr:fieldLabel fieldId="CommercialFinanced" />: </label>
      </cmr:column>
      <cmr:column span="1" width="100">
            ${summary.data.commercialFinanced}
      </cmr:column>
    </cmr:view>
   </cmr:row>
  <cmr:row addBackground="true">   
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:view exceptForGEO="MCO1,MCO2" exceptForCountry="822">
      <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="LocationNumber" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
        <%
          String locationNumber = DropdownListController.getDescription("LocationNumber", data.getLocationNumber(), cntry);
        %>
        <%=locationNumber != null ? locationNumber : ""%>
      </cmr:column>
    </cmr:view>
    
    </cmr:row>
    <cmr:row addBackground="true"> 
    <cmr:view exceptForGEO="MCO1,MCO2" exceptForCountry="822">
    <cmr:column span="1" width="127">
        </cmr:column>
      <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="CurrencyCd" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240">
           <!--  ${summary.data.currencyCd} -->
             <%
            String legacyCyCd = DropdownListController.getDescription("CurrencyCd", data.getLegacyCurrencyCd(), cntry);
          %>
          <%=legacyCyCd != null ? legacyCyCd : ""%>
      </cmr:column>
    </cmr:view>
    
      <cmr:column span="1" width="90" forCountry="838">
        <label><cmr:fieldLabel fieldId="MailingCond" />: </label>
      </cmr:column>
      <cmr:column span="1" width="240" forCountry="838">
           <!--${summary.data.mailingCondition}  --> 
            <%
            String mailingCd = DropdownListController.getDescription("MailingCond", data.getMailingCondition(), cntry);
          %>
          <%=mailingCd != null ? mailingCd : ""%>
      </cmr:column>
   </cmr:row>
   
   <cmr:row addBackground="true"> 
      <cmr:column span="1" width="127">
    </cmr:column>
      <cmr:view exceptForGEO="MCO1,MCO2">
        <cmr:column span="1" width="130">
          <label><cmr:fieldLabel fieldId="CollectionCd" />: </label>
        </cmr:column>
        <cmr:column span="1" width="240">
            <!--   ${summary.data.collectionCd} -->
             <%
            String collCd = DropdownListController.getDescription("CollectionCd", data.getCollectionCd(), cntry);
          %>
          <%=collCd != null ? collCd : ""%>
        </cmr:column>
      </cmr:view>
   </cmr:row>
</cmr:view>