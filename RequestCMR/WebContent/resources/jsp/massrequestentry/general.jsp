<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.ui.FieldInformation"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
AppUser user = AppUser.getUser(request);
boolean noFindCMR = user.getAuthCode() == null;
RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
boolean autoProcCapable = PageManager.autoProcEnabled(reqentry.getCmrIssuingCntry(), reqentry.getReqType());
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />

    <cmr:section id="GENERAL_REQ_TAB">
      <cmr:row topPad="10">
          <cmr:column span="1" width="100">
            <p>
              <label for="requestId"><cmr:fieldLabel fieldId="RequestID" />: </label>
              <c:if test="${reqentry.reqId == 0}">
              Not assigned
              </c:if>
              <c:if test="${reqentry.reqId != 0}">
              ${reqentry.reqId}
              </c:if>
            <form:hidden id="reqId" path="reqId" />
            </p>
          </cmr:column>
          <cmr:column span="1" width="200">
            <p>
              <label for="overallStatus">${ui.genReqStat}: </label>
              ${reqentry.overallStatus}
            <form:hidden id="overallStatus" path="overallStatus" />
            </p>
          </cmr:column>
          <cmr:column span="1" width="170">
            <p>
              <label for="lockByNm">${ui.lockedBy}: </label>
             ${reqentry.lockByNm}
            <form:hidden id="lockByNm" path="lockByNm" />
            </p>
          </cmr:column>
          <cmr:column span="1" width="150">
            <p>
            <label for="userRole">${ui.yourRole}: </label>
            ${reqentry.userRole}
          <form:hidden id="userRole" path="userRole" />
          </p>
          </cmr:column>
          <cmr:column span="1" width="170">
              <p>
                <label for="yourId">${ui.yourId}: </label>
               ${reqentry.yourId}
              <form:hidden id="yourId" path="yourId" />
              <br/>
              ${reqentry.yourNm}
              <form:hidden id="yourNm" path="yourNm" />
              </p>
            </cmr:column>
      </cmr:row>
      <cmr:row>
          <cmr:column span="1" width="100">
            <p>
              <label for="createTs">${ui.createDate}: </label>
              ${reqentry.createDate}
            <form:hidden id="createDate" path="createDate" />
            </p>
          </cmr:column>
          <cmr:column span="1" width="200">
              <p>
                <label for="lastUpdtTs">${ui.lastUpdated}: </label>
                ${reqentry.lstUpdDate}
              <form:hidden id="lstUpdDate" path="lstUpdDate" />
              </p>
          </cmr:column>
          <cmr:column span="1" width="170">
            <p>
              <label for="lockTs">${ui.lockedDate}: </label>
             ${reqentry.lockedDate}
            <form:hidden id="lockedDate" path="lockedDate" />
            </p>
          </cmr:column>
          <cmr:column span="1" width="150">
            <p>
              <label for="lockTs">${ui.procStatus}: </label>
             ${reqentry.processingStatus}
            </p>
          </cmr:column>
    <%if (!newEntry){ %>
          <cmr:column span="1" width="170" >
            <div style="padding-top:10px">
            <cmr:button label="${ui.btn.requestSummary}" onClick="showSummaryScreen(${reqentry.reqId}, '${reqentry.reqType}')" highlight="false" pad="false"/>
            </div>
          </cmr:column>
    <%} %>
      </cmr:row>
      <cmr:row topPad="10">
<%if (!readOnly){ %>
        <cmr:column span="2" >
          <span style="color:red;font-size:16px;font-weight:bold">*</span> 
        <span style="font-size:14px;font-style:italic;color:#333">
          ${ui.mandatoryLegend}
        </span>
          </cmr:column>
<%}%>
      </cmr:row>
      <cmr:row addBackground="true">
        <cmr:column span="2">
          <cmr:row>
            <cmr:column span="2">
              <p>
              <cmr:label fieldId="cmrIssuingCntry"> 
                <cmr:fieldLabel fieldId="CMRIssuingCountry" />: 
                <cmr:delta text="${rdcdata.cmrIssuingCntry}" oldValue="${reqentry.cmrIssuingCntry}" code="R"/>
                <cmr:info text="${ui.info.cmrIssuingCntry}" /> 
              </cmr:label>
              <cmr:field id="cmrIssuingCntry" path="cmrIssuingCntry" fieldId="CMRIssuingCountry" tabId="MAIN_GENERAL_TAB"/>
              </p>
            </cmr:column>
          </cmr:row>
          <cmr:row>
            <cmr:column span="2">
            <p>
              <cmr:label fieldId="reqType">
                <cmr:fieldLabel fieldId="MassRequestType" />:
              </cmr:label>
              <cmr:field id="reqType" path="reqType" fieldId="MassRequestType" tabId="MAIN_GENERAL_TAB"/>

              <cmr:column span="2" containerForField="ProspectToLegalCMR" >
              <cmr:field fieldId="ProspectToLegalCMR" path="prospLegalInd" tabId="MAIN_GENERAL_TAB"/>
              <cmr:label fieldId="prospLegalInd" forRadioOrCheckbox="true">
                <cmr:fieldLabel fieldId="ProspectToLegalCMR" />
                <cmr:info text="${ui.info.makeProspectsLegal}"/>
              </cmr:label></cmr:column>
            </p>
            </cmr:column>
          </cmr:row>        
       </cmr:column>
       <cmr:column span="2">
         <cmr:row>
           <cmr:column span="2">
             <p>
             <label for="approvalResult"> ${ui.approvalsResult}: </label>
              <span style="margin:0">
                ${reqentry.approvalResult}
              </span>
             </p>
           </cmr:column>
           <cmr:view forCountry="866,838,754">
           <cmr:column span="2">
           <p>
            <cmr:label fieldId="muLimit">
              <cmr:fieldLabel fieldId="MassUpdateLimit" />:
            </cmr:label>
            <cmr:field id="muLimit" path="installTeamCd" fieldId="MassUpdateLimit" tabId="MAIN_GENERAL_TAB"/>
           </p>
           </cmr:column>
           </cmr:view>
         </cmr:row>
         <c:if test="${not empty reqentry.approvalDateStr}">
         <cmr:row>
           <cmr:column span="2">
             <p>
             <label for="approvalDateStr"> ${ui.approvalsUpdtTs}: </label>
              <span style="margin:0">
                ${reqentry.approvalDateStr}
              </span>
             </p>
           </cmr:column>
         </cmr:row>  
         </c:if>
         
       </cmr:column>
<%--       <cmr:view forGEO="LA,EMEA,IERP,CND,FR">         
       <cmr:column span="2" width="250" containerForField="DisableAutoProcessing">
        <p>
        <cmr:field fieldId="DisableAutoProcessing" path="disableAutoProc" />
        <cmr:label fieldId="disableAutoProc" forRadioOrCheckbox="true">
           <cmr:fieldLabel fieldId="DisableAutoProcessing" />
        </cmr:label>
        <cmr:info text="${ui.info.disableAutoProc}"></cmr:info>
       </p>
        </cmr:column>
        </cmr:view>  && (reqentry.getUserRole() != null && reqentry.getUserRole().equalsIgnoreCase("PROCESSOR"))--%>

              <%if (autoProcCapable){%>
              <%if (reqentry.getUserRole() != null && reqentry.getUserRole().equalsIgnoreCase("PROCESSOR")){ %>
              <cmr:column span="1" width="250" containerForField="DisableAutoProcessing">
              <p>
              <cmr:field fieldId="DisableAutoProcessing" path="disableAutoProc" />
              <cmr:label fieldId="disableAutoProc" forRadioOrCheckbox="true">
                 <cmr:fieldLabel fieldId="DisableAutoProcessing" />
              </cmr:label>
              <cmr:info text="${ui.info.disableAutoProc}"></cmr:info>
             </p>
             </cmr:column>
             <%} else { %>
             <input type="hidden" name="disableAutoProc" value="${reqentry.disableAutoProc}">
             <%} %>
             <%} else {%>
               <input type="hidden" name="disableAutoProc" value="Y">
             <%} %>

        <cmr:view exceptForGEO="LA">
        <cmr:column span="2">
          <p></p>
        </cmr:column>
        </cmr:view>  
      </cmr:row>
      
      <cmr:view forCountry="758,864,848">
 	<cmr:row addBackground="false">
    	<cmr:column span="2" containerForField="CountrySubRegion">
          <p>
            <cmr:label fieldId="countryUse">
              <cmr:fieldLabel fieldId="CountrySubRegion" />:
            </cmr:label>
            <cmr:field fieldId="CountrySubRegion" id="countryUse" path="countryUse" tabId="MAIN_GENERAL_TAB" size="250" />
          </p>
        </cmr:column>
        </cmr:row>
        </cmr:view> 
      <cmr:row>
        <cmr:column span="2">
          <cmr:row>
            <cmr:column span="2">
              <p>
              <cmr:label fieldId="expediteInd">
                <cmr:fieldLabel fieldId="Expedite" />:<cmr:info text="${ui.info.expediteInd}" />
              </cmr:label>
              <cmr:field id="expediteInd" path="expediteInd" fieldId="Expedite" tabId="MAIN_GENERAL_TAB"/>
              </p>
            </cmr:column>
          </cmr:row>
          <cmr:row>
            <cmr:column span="2">
            <p>
              <cmr:label fieldId="requestingLob">
                <cmr:fieldLabel fieldId="RequestingLOB" />:
                <cmr:info text="${ui.info.requestingLOB}" />
              </cmr:label>
              <cmr:field id="requestingLob" path="requestingLob" fieldId="RequestingLOB" tabId="MAIN_GENERAL_TAB"/>
            </p>
            </cmr:column>
          </cmr:row>
        </cmr:column>
        <cmr:column span="2">
          <cmr:row>
            <cmr:column span="2">
              <p>
              <cmr:label fieldId="expediteReason">
                <cmr:fieldLabel fieldId="ExpediteReason" />:
              </cmr:label>
              <cmr:field id="expediteReason" path="expediteReason" fieldId="ExpediteReason" tabId="MAIN_GENERAL_TAB"/>
              </p>
            </cmr:column>
          </cmr:row>
          <cmr:row>
            <cmr:column span="2">
            <p>
              <cmr:label fieldId="reqReason">
                <cmr:fieldLabel fieldId="RequestReason" />:
              </cmr:label>
              <cmr:field id="reqReason" path="reqReason" fieldId="RequestReason" tabId="MAIN_GENERAL_TAB"/>
            </p>
            </cmr:column>
          </cmr:row>
        </cmr:column>
        <cmr:view forGEO="JP">
          <cmr:column span="2">
            <cmr:row>
              <cmr:column span="2">
                <p>
                  <label for="requesterId">Requester ID:</label>
                  ${reqentry.requesterId}
                </p>
              </cmr:column>
            </cmr:row>
          </cmr:column>
          <cmr:column span="2">
            <cmr:row>
              <cmr:column span="2">
                <p>
                  <label for="reqFor"><cmr:fieldLabel fieldId="OriginatorName" />:<cmr:info text="${ui.info.orgName}" /></label>
                  <cmr:bluepages model="reqentry" namePath="originatorNm"  idPath="originatorId" useBothIds="true" showId="true"></cmr:bluepages>
                </p>
              </cmr:column>
            </cmr:row>
          </cmr:column>
        </cmr:view>
        <cmr:view exceptForGEO="JP">
          <cmr:column span="2">
            <cmr:row>
              <cmr:column span="2">
                <p>
                  <label for="reqFor"><cmr:fieldLabel fieldId="OriginatorName" />:<cmr:info text="${ui.info.orgName}" /></label>
                  <cmr:field model="reqentry" path="originatorNm" idPath="originatorId" fieldId="OriginatorName" />
                </p>
              </cmr:column>
            </cmr:row>
          </cmr:column>
        </cmr:view>
      </cmr:row>
      
      <cmr:view forGEO="JP">
        <cmr:row addBackground="false" topPad="10">
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="requestDueDate">
                <cmr:fieldLabel fieldId="NuclChecklstDate" />:
              </cmr:label>
              <cmr:date id="requestDueDateTemp" path="requestDueDateTemp" format="yyyy-MM-dd" />
            </p>
          </cmr:column>
        </cmr:row>
      </cmr:view>
    
      <form:hidden path="cmt"/>
      <form:hidden path="yourId"/>
      <form:hidden path="yourNm" />
    </cmr:section>
    


