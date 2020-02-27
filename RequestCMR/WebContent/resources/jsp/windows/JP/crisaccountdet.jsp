<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.ibm.cmr.services.client.cris.CRISAddress"%>
<%@page import="com.ibm.cmr.services.client.cris.CRISAccount"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  String actionUrl = request.getContextPath() + "/window/crisprocess";
  String companyNo = request.getParameter("companyNo");
  CRISAccount account = (CRISAccount) request.getAttribute("record");
  if (companyNo == null){
    companyNo = "";
  }
  String establishmentNo = request.getParameter("establishmentNo");
  if (establishmentNo == null){
    establishmentNo = "";
  }
  String access = (String) request.getParameter("access");
  if (access == null){
    access = "";
  }
%>
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<script>
function backToSearch(){
  window.location = '<%=request.getContextPath()+"/window/crissearch"%>';
}
function backToAccountList(){
  window.location = '<%=request.getContextPath()+"/window/crisaccountlist?establishmentNo="+establishmentNo+"&companyNo="+companyNo+"&access="+access%>';
}

function chooseRecord(){
  var result = {
     accepted : 'y',
     type : 'A',
     data : {
       issuedBy : '760',
       issuedByDesc : 'Japan',
       cmrNum : '<%=account.getAccountNo()%>'
     }
  };
  if (window.opener){
    window.opener.cmr.hideProgress();
    window.opener.doImportCRISRecord(result);
    WindowMgr.closeMe();    
  }
  
}

function trackMe(){
  // noop
}
WindowMgr.trackMe = trackMe;  
</script>
<style>
div.ibm-columns {
  width: 1100px !important;
}

table.ibm-data-table th, table.ibm-data-table td {
  font-size: 12px !important;
}
</style>
<cmr:window>
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMR" class="ibm-column-form ibm-styled-form" modelAttribute="crit">
    <jsp:include page="criscriteria.jsp" />
  </form:form>
  <form:form method="POST" action="<%=actionUrl%>" name="frmCMRDet" class="ibm-column-form ibm-styled-form" modelAttribute="record">
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="companyNo">Company No:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${company.companyNo}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="establishmentNo">Establishment No:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${estab.establishmentNo}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="companyName">Company Name:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${company.nameKanji}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="establishmentName">Establishment Name:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${estab.nameKanji}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="companyAddress">Company Address:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${company.address}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="establishmentAddress">Establishment Address:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${estab.address}
      </cmr:column>
    </cmr:row>
    <cmr:hr />
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="estalishmentNo">Account No:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.accountNo}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="companyCd">Company Code:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.companyCd}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameAbbr">Abbreviated Name:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.nameAbbr}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="proxiLocnNo">IBM Related CMR:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.attach}
      </cmr:column>
    </cmr:row>
     <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameKanji">Account_Customer Name:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.addrNameKanji}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameKana">Account_Katakana:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.addrNameKana}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameKanji">Customer Name_Detail:</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.nameKanji}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="nameKana">Customer Name-Detail (Katakana):</label>
      </cmr:column>
      <cmr:column span="4">
        ${record.nameKana}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="jsic">JSIC:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.JSIC}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="locCode">Location Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.locCode}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="SBO">Office Code:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.SBO}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="SR">Rep. Sales No.:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.SR}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="salesTeamCode">Sales/Team No.:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.salesTeamCode}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="SRAssignDate">Date Salesman / Sales Team Change:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.SRAssignDate}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="channelCode">Channel Code:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.channelCode}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="dealerNo">Dealer No.:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.dealerNo}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="CSBO">CS BO:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.CSBO}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="CSDiv">CS Division:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.CSDiv}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="billingCustNo">Bill to Customer No.:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.billingCustNo}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="billingProcessCode">Billing Process Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.billingProcessCode}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="creditToCustNo">Credit Customer No.:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.creditToCustNo}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="invoiceSplitCode">Invoice Split Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.invoiceSplitCode}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="IPSS">IPSS:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.IPSS}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="inacCode">INAC/NAC Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.inacCode}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="CRSCode">CRS Code:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.CRSCode}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="CARCode">CAR Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.CARCode}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="OEM">OEM:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.OEMInd}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="leasingCompanyInd">Leasing Company:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.leasingCompanyInd}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="SIInd">SI:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.SIInd}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="prospectInd">Type of Customer/Prospect:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.prospectInd}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="valueAddRem">Value Added Remarketer:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.valueAddRem}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="agreementSignDate">Agreement Sign Date:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.agreementSignDate}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="1" width="180">
        <label for="educAllowanceGrp">Education Allowance Group:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.educAllowanceGrp}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="govOfficeDivCode">Government Division Code:</label>
      </cmr:column>
      <cmr:column span="2">
        ${record.govOfficeDivCode}
      </cmr:column>
    </cmr:row>
     <cmr:row>
      <cmr:column span="1" width="180">
        <label for="tier2">TIER-2 CODE:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.tier2}
      </cmr:column>
      <cmr:column span="1" width="150">
        <label for="companyCd">IIN:</label>
      </cmr:column>
      <cmr:column span="2" width="300">
        ${record.INNInd}
      </cmr:column>
    </cmr:row>
    <cmr:row>
      <cmr:column span="6" width="1100">
        <div class="result-cont" style="width:1100px; overflow-x:auto">
          <div class="result-cont-in" style="width:1800px;">
            <table cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating">
              <caption>
                <em>Addresses</em>
              </caption>
              <thead>
                <tr>
                  <th scope="col" width="6%">Address Type</th>
                  <th scope="col" width="10%">Account_Customer Name - KANJI</th>
                  <th scope="col" width="8%">Postal Code</th>
                  <th scope="col" width="*">Address</th>
                  <th scope="col" width="10%">Building</th>
                  <th scope="col" width="10%">Branch/Office</th>
                  <th scope="col" width="10%">Department</th>
                  <th scope="col" width="10%">Contact</th>
                  <th scope="col" width="6%">Phone</th>
                  <th scope="col" width="6%">Fax</th>
                  <th scope="col" width="8%">Class/Group</th>
                </tr>
              </thead>
              <tbody>
                <%for (CRISAddress address : account.getAddresses()){%>
                  <tr>
                    <td><%=address.getId().getAddrType()%></td>
                    <td><%=address.getCompanyNameKanji() != null ? StringUtils.strip(address.getCompanyNameKanji()) : ""%></td>
                    <td><%=address.getPostCode() != null ? StringUtils.strip(address.getPostCode()) : ""%></td>
                    <td><%=address.getAddress() != null ?  StringUtils.strip(address.getAddress()) : ""%></td>
                    <td><%=address.getBldg() != null ? StringUtils.strip(address.getBldg()) : ""%></td>
                    <td><%=address.getEstablishmentNameKanji() != null ? StringUtils.strip(address.getEstablishmentNameKanji()) : ""%></td>
                    <td><%=address.getDept() != null ? StringUtils.strip(address.getDept()) : ""%></td>
                    <td><%=address.getContact() != null ?  StringUtils.strip(address.getContact()) : ""%></td>
                    <td>
                      <%=address.getPhoneShi()%>-<%=address.getPhoneKyo()%>-<%=address.getPhoneBango()%>
                    </td>
                    <td>
                      <%=address.getFaxShi()%>-<%=address.getFaxKyo()%>-<%=address.getFaxBango()%>
                    </td>
                    <td><%=address.getCustClass() !=null ? StringUtils.strip(address.getCustClass()) : ""%>/<%=address.getCustGrp() != null ? StringUtils.strip(address.getCustGrp()) : ""%></td>
                  </tr>
                
                <%} %>
                
              </tbody>
              </table>
            </div>
          </div>
      </cmr:column>
    </cmr:row>
  </form:form>
  <cmr:windowClose>
    <cmr:button label="Import Account" onClick="chooseRecord()" highlight="true" pad="true"/>
    <cmr:button label="Search Again" onClick="backToSearch()" highlight="true" pad="true"/>
<%if (!StringUtils.isBlank(establishmentNo)){%>
    <cmr:button label="Back to Account List" onClick="backToAccountList()" highlight="false" pad="true" />
<%} %>
  </cmr:windowClose>
</cmr:window>
