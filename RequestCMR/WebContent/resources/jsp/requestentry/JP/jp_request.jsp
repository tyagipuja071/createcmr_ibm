<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  String reqType = reqentry.getReqType();
  String custType = reqentry.getCustType(); 
%>
  <cmr:column span="2">
    <p>
      <label for="custType" id="custType_label" style="display:inline-block"> 
      <span id="cmr-fld-lbl-CustomerType">Requested Records:</span> 
      </label>
      <%if ("C".equals(reqType)){%>
        <%if ("CEA".equals(custType)){ %>
          Company, Establishment, Account
        <%} else if  ("EA".equals(custType)){%>
          Establishment, Account
        <%} else if  ("C".equals(custType)){%>
          Subsidiary Company
        <%} else if  ("A".equals(custType)){%>
          Account
        <%}%>
      <%} else if ("U".equals(reqType)){ %>
        <%if ("CEA".equals(custType)){ %>
          Company, Establishment, Account
        <%} else if  ("CE".equals(custType)){%>
          Company, Establishment
        <%} else if  ("C".equals(custType)){%>
          Company
        <%}%>
      <%} %>
      </label>
    </p>
  </cmr:column>
