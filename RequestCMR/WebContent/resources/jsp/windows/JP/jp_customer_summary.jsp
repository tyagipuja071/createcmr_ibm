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
<cmr:view forGEO="JP">

  <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="AbbrevName" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.abbrevNm}
        </cmr:column>

    <cmr:column span="1" width="90">
      <label><cmr:fieldLabel fieldId="AbbrevLocation" />: </label>
    </cmr:column>
    <cmr:column span="1" width="100">
          ${summary.data.email2}
        </cmr:column>

  </cmr:row>
  
    <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="JSICCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.jsicCd}
        </cmr:column>

    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="OEMInd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.oemInd}
        </cmr:column>

  </cmr:row>
  
    <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="LeasingCompIndc" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.leasingCompanyIndc}
        </cmr:column>

    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="EducationAllowance" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.educAllowCd}
        </cmr:column>

  </cmr:row>
  
    <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CustAcctType" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.custAcctType}
        </cmr:column>

    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CustClass" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.custClass}
        </cmr:column>

  </cmr:row>
  
      <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="IinInd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.iinInd}
        </cmr:column>

    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ValueAddRem" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.valueAddRem}
        </cmr:column>

  </cmr:row>
  
        <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="ChannelCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.channelCd}
        </cmr:column>

    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="SiInd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.siInd}
        </cmr:column>

  </cmr:row>
  
          <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CrsCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.crsCd}
        </cmr:column>

    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="CreditCd" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.creditCd}
        </cmr:column>

  </cmr:row>
  
            <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="Government" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.govType}
        </cmr:column>

    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="OutsourcingServ" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.outsourcingService}
        </cmr:column>

  </cmr:row>
  
              <cmr:row addBackground="true">
    <cmr:column span="1" width="127">
    </cmr:column>
    <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="DirectBp" />: </label>
    </cmr:column>
    <cmr:column span="1" width="240">
          ${summary.data.creditBp}
        </cmr:column>
            <cmr:column span="1" width="130">
      <label><cmr:fieldLabel fieldId="zSeriesSw" />: </label>
    </cmr:column>
    <cmr:column span="1" width="200">
          ${summary.data.zseriesSw}
        </cmr:column>

  </cmr:row>
</cmr:view>
