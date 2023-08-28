<%@page import="com.ibm.cio.cmr.request.CmrConstants"%>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="java.util.List"%>
<%@page import="com.ibm.cio.cmr.request.ui.FieldInformation"%>
<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.util.geo.impl.LAHandler"%>
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
boolean dnbPrimary = "Y".equals(request.getAttribute("dnbPrimary"));
Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
if (readOnly == null){
  readOnly = false;
}
boolean autoProcCapable = PageManager.autoProcEnabled(reqentry.getCmrIssuingCntry(), reqentry.getReqType());
boolean laReactivateCapable = PageManager.laReactivateEnabled(reqentry.getCmrIssuingCntry(), reqentry.getReqType());
%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
    <cmr:section id="GENERAL_REQ_TAB">
      <form:hidden id="reqId" path="reqId" />
      <jsp:include page="detailstrip.jsp" />
      <cmr:row addBackground="true">
        <cmr:column span="2">
          <cmr:row noPad="true">
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
          <cmr:row noPad="true">
            <cmr:column span="2">
            <p>
              <cmr:label fieldId="reqType">
                <cmr:fieldLabel fieldId="RequestType" />:
              </cmr:label>
              <cmr:field id="reqType" path="reqType" fieldId="RequestType" tabId="MAIN_GENERAL_TAB"/>
              <cmr:field id="enterCMRNo" path="enterCMRNo" fieldId="EnterCMR" tabId="MAIN_GENERAL_TAB" placeHolder="Enter CMR Number" size="215"/>
<%
String findCmrJs = "onclick=\"findAndImportCMRs()\"";
if (readOnly){
  findCmrJs = "";
}
%>              
              <img title="Search and Import the CMR" class="cmr-proceed2-icon" src="${resourcesPath}/images/search2.png" <%=findCmrJs%>>
            <cmr:view exceptForGEO="IERP,CND">
            <cmr:column span="2" containerForField="ProspectToLegalCMR">
              <cmr:field fieldId="ProspectToLegalCMR" path="prospLegalInd" tabId="MAIN_GENERAL_TAB"/>
              <cmr:label fieldId="prospLegalInd" forRadioOrCheckbox="true">
                <cmr:fieldLabel fieldId="ProspectToLegalCMR" />
                <cmr:info text="${ui.info.makeProspectsLegal}"/>
              </cmr:label></cmr:column></cmr:view>
        </p>
            </cmr:column>
            <cmr:view forGEO="IERP,CND">
            <cmr:column span="2" containerForField="ProspectToLegalCMR">
              <cmr:field fieldId="ProspectToLegalCMR" path="prospLegalInd" tabId="MAIN_GENERAL_TAB"/>
              <cmr:label fieldId="prospLegalInd" forRadioOrCheckbox="true">
                <cmr:fieldLabel fieldId="ProspectToLegalCMR" />
                <cmr:info text="${ui.info.makeProspectsLegal}"/>
              </cmr:label></cmr:column></cmr:view>
			<%
				if (laReactivateCapable) {
			%>  
            <cmr:view forGEO="LA">
            <cmr:column span="2" containerForField="DeactivateToActivateCMR">
              <cmr:field fieldId="DeactivateToActivateCMR" path="func" tabId="MAIN_GENERAL_TAB"/>
              <cmr:label fieldId="func" forRadioOrCheckbox="true">
                <cmr:fieldLabel fieldId="DeactivateToActivateCMR" />
              </cmr:label></cmr:column></cmr:view>
              <%}%>
            
          </cmr:row>
       </cmr:column>
       <cmr:column span="2" >
	<cmr:view forCountry="724,858,766" >
 	<% autoProcCapable = true ;%>
        </cmr:view>
            <cmr:row topPad="15" noPad="true">
                <cmr:column span="2" >
                  <cmr:buttonsRow>
                    <cmr:button id="cmrSearchBtn" label="${ui.btn.cmrSrch}" onClick="doCMRSearch()" highlight="true" styleClass="cmr-reqentry-btn"/>
                      <span class="ibm-required cmr-required-spacer">
                    <%if (!readOnly){%>
                      *
                    <%} else {%>
                      &nbsp;
                    <%} %>
                    </span>
                    <cmr:info text="${ui.info.cmrSearch}" />
                    <%if (noFindCMR){%>
                    <img src="${resourcesPath}/images/warn-icon.png" class="cmr-warn-icon" title="${ui.info.nofindcmr}">
                    <%}%>
                  </cmr:buttonsRow>
                </cmr:column>
            </cmr:row>
            <cmr:row noPad="true" topPad="20">
                <cmr:column span="2">
                  <cmr:buttonsRow>
                    <cmr:button  id="dnbSearchBtn" label="${ui.btn.dnbSrch}" onClick="doDnBSearch()" highlight="true" styleClass="cmr-reqentry-btn"/>
                    <span class="ibm-required cmr-required-spacer" id="dnbRequiredIndc" >*</span>
                    <cmr:info text="${ui.info.dnbSearch}" />
                    <%if (noFindCMR){%>
                    <img src="${resourcesPath}/images/warn-icon.png" class="cmr-warn-icon" title="${ui.info.nofindcmr}">
                    <%}%>
                  </cmr:buttonsRow>
              </cmr:column>
            </cmr:row>
              <%if (autoProcCapable){%>
            <cmr:row noPad="true" topPad="10">
              <cmr:column span="2" containerForField="DisableAutoProcessing">
              <p>
              <cmr:field fieldId="DisableAutoProcessing" path="disableAutoProc" />
              <cmr:label fieldId="disableAutoProc" forRadioOrCheckbox="true">
                 <cmr:fieldLabel fieldId="DisableAutoProcessing" />
              </cmr:label>
              <cmr:info text="${ui.info.disableAutoProc}"></cmr:info>
             </p>
             </cmr:column>
            </cmr:row>
             <%} else {%>
               <input type="hidden" name="disableAutoProc" value="Y">
             <%} %>
        </cmr:column>
        <div class="ibm-col-6-2">
            
            <table border="2" cellspacing="0" cellpadding="0" class="cmr-scorecard">
              <tr>
                <td colspan="3" class="scorecard">
                  <div class="scorecard">
                    SCORECARD
                    <img src="${resourcesPath}/images/info-bubble-icon.png" title="${ui.info.scoreCard}" class="cmr-info-bubble scorecard-icon">                    
                  </div>
                  
                </td>
              </tr>
              <tr>
                <td width="100px">CMR Search &nbsp;<%if (!readOnly){%><span class="ibm-required cmr-required-spacer">*</span>
                    <%} %>
                  <form:hidden path="findCmrResult"/>
                </td>
                <td width="120px" id="findCMRResult_txt">
                  <c:if test="${reqentry.findCmrResult == 'Rejected'}">
                  <a href="javascript:showCMRRejectedModal()">${reqentry.findCmrResult}</a>
                  </c:if>
                  <c:if test="${reqentry.findCmrResult != 'Rejected'}">
                  ${reqentry.findCmrResult}
                  </c:if>
                </td>
                <td width="105px">${reqentry.findCmrDate}</td>
              </tr>
              <tr>
                <td>D&B Search&nbsp;<span id="dnbRequired" class="ibm-required cmr-required-spacer">*</span>
                  <form:hidden path="findDnbResult"/>
                </td>
                <td>
                  <c:if test="${reqentry.findDnbResult == 'Rejected'}">
                  <a href="javascript:showDNBRejectedModal()">${reqentry.findDnbResult}</a>
                  </c:if>
                  <c:if test="${reqentry.findDnbResult != 'Rejected'}">
                  ${reqentry.findDnbResult}
                  </c:if>
                </td>
                <td>${reqentry.findDnbDate}</td>
              </tr>
              <tr>
                <td>DPL Check &nbsp;
                    <%if (!readOnly){%>
                      <span class="ibm-required cmr-required-spacer">*</span>
                    <%} %>
                </td>
                <td>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'AP'}">
                  All Passed
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'AF'}">
                  All Failed
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'SF'}">
                  Some Failed
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'Not Done'}">
                  Not Done
                  </c:if>
                  <c:if test="${fn:trim(reqentry.dplChkResult) == 'NR'}">
                  Not Required
                  </c:if>                  
                </td>
                <td>${reqentry.dplChkDate}</td>
              </tr>
              <tr style="display:none">
                <td>Address Std &nbsp;<span class="ibm-required">
                    <%boolean laFlage = false;
                      
                      if(LAHandler.isLACountry(reqentry.getCmrIssuingCntry()) || 
                         CmrConstants.DE_ISSUING_COUNTRY_VAL.equals(reqentry.getCmrIssuingCntry())){
                      	laFlage = true;
                      }
                    
                      if (!readOnly){
                      if(laFlage){
                    %>
                     &nbsp;   
                     <%} else {%>
                        *
                     <%} %>
                    <%} else {%>
                    &nbsp;
                    <%} %>
                </span>
                  <form:hidden path="addrStdResult"/>
                </td>
                <td id ="addrstdResultDisplay">
                  <c:if test="${reqentry.addrStdResult == 'Rejected'}">
                 <a href="javascript:showAddrSTdRejectedModal()">${reqentry.addrStdResult}</a>
                  </c:if>
                  <c:if test="${reqentry.addrStdResult == 'No Result'}">
                  <a href="javascript:showAddrStdNoResultModal()">${reqentry.addrStdResult}</a>
                  </c:if>
                  <c:if test="${reqentry.addrStdResult != 'Rejected' && reqentry.addrStdResult != 'No Result'}">
                  ${reqentry.addrStdResult}
                  </c:if>
                </td>
                <td id ="addrstdResultDateDisplay">${reqentry.addrStdDate}</td>
              </tr>
              <tr>
                <td>Approvals</td>
                <td>${reqentry.approvalResult}</td>
                <td>${reqentry.approvalDateStr}</td>
              </tr>
              <cmr:view forCountry="866,754,724,848,618,788,624,678,702,806,846">
	              <tr>
	                <td>VAT Acknowledge</td>
	                <td id='vatAckknowledge'>${reqentry.vatAcknowledge}</td>
	                <td>${reqentry.findCmrDate}</td>
	              </tr>
              </cmr:view>
              <%if (null != reqentry.getCmrIssuingCntry() && ("852".equals(reqentry.getCmrIssuingCntry()) || "720".equals(reqentry.getCmrIssuingCntry()) || "738".equals(reqentry.getCmrIssuingCntry()) || "736".equals(reqentry.getCmrIssuingCntry()) || "646".equals(reqentry.getCmrIssuingCntry()) || "714".equals(reqentry.getCmrIssuingCntry()))) {%>
              <tr>
                <td>Proliferation Checklist
                    <%if (!readOnly){%>
                      <span class="ibm-required cmr-required-spacer">*</span>
                    <%} %>
                </td>
                <td id="checklistStatus">
                  
                </td>
                <td>-</td>
              </tr>
              <%}%>
              <cmr:view forCountry="358,359,363,607,620,626,651,675,677,680,694,695,713,741,752,762,767,768,772,787,805,808,821,823,832,849,850,865,889,641,766,858,755,871">
              <tr>
                <td>Proliferation Checklist
                    <%if (!readOnly){%>
                      <% if("755".equals(reqentry.getCmrIssuingCntry()) && "U".equals(reqentry.getReqType())) %>
                      <% else { %>
                      	<span class="ibm-required cmr-required-spacer">*</span>
                      <% } %>	
                    <%} %>
                </td>
                <td id="checklistStatus">
                  
                </td>
                <td>-</td>
              </tr>
              </cmr:view>              
            </table>
        </div>
      </cmr:row>

	<cmr:view forCountry="758,864,706,707,762,808,702,678,624,848" >
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
  <%if (!newEntry){%>      
  
      <!--  Customer Group, Subgroup, Type -->
    
    <cmr:row addBackground="false">
      <c:if test="${reqentry.reqType != 'U'}">
      <cmr:view forCountry="631,724,815,661,629,613,655,663,681,683,731,735,781,799,811,813,829,869,871,641,846,806,702,678,788,624,848,858,766">
        <cmr:column span="2" containerForField="CustomerScenarioType">
          <p>
            <cmr:label fieldId="custGrp">
              <cmr:fieldLabel fieldId="CustomerScenarioType" />:
            </cmr:label>
            <cmr:field fieldId="CustomerScenarioType" id="custGrp" path="custGrp" tabId="MAIN_GENERAL_TAB" size="250" />
          </p>
        </cmr:column>
        <cmr:column span="2" containerForField="CustomerScenarioSubType">
          <p>
            <cmr:label fieldId="custSubGrp">
              <cmr:fieldLabel fieldId="CustomerScenarioSubType" />:
            </cmr:label>
            <cmr:field fieldId="CustomerScenarioSubType" id="custSubGrp" path="custSubGrp" tabId="MAIN_GENERAL_TAB" size="250" />
          </p>
        </cmr:column>
      </cmr:view>
      </c:if>
      <cmr:view forGEO="EMEA,MCO,MCO1,MCO2,FR,CEMEA,JP,CA">
        <cmr:column span="2" containerForField="CustomerScenarioType">
          <p>
            <cmr:label fieldId="custGrp">
              <cmr:fieldLabel fieldId="CustomerScenarioType" />:
            </cmr:label>
            <cmr:field fieldId="CustomerScenarioType" id="custGrp" path="custGrp" tabId="MAIN_GENERAL_TAB" size="250" />
          </p>
        </cmr:column>
        <cmr:column span="2" containerForField="CustomerScenarioSubType">
          <p>
            <cmr:label fieldId="custSubGrp">
              <cmr:fieldLabel fieldId="CustomerScenarioSubType" />:
            </cmr:label>
            <cmr:field fieldId="CustomerScenarioSubType" id="custSubGrp" path="custSubGrp" tabId="MAIN_GENERAL_TAB" size="250" />
          </p>
        </cmr:column>
        <!--  requested records for japan  -->
        <cmr:view forCountry="760">
          <jsp:include page="JP/jp_request.jsp" />
        </cmr:view>
      </cmr:view>
      <cmr:view forCountry="616,615,643,720,738,744,749,714,736,778,646,796,818,834,652,856,852,790">
        <cmr:column span="2" containerForField="CustomerScenarioType">
          <p>
            <cmr:label fieldId="custGrp">
              <cmr:fieldLabel fieldId="CustomerScenarioType" />:
            </cmr:label>
            <cmr:field fieldId="CustomerScenarioType" id="custGrp" path="custGrp" tabId="MAIN_GENERAL_TAB" size="250" />
          </p>
        </cmr:column>
        <cmr:column span="2" containerForField="CustomerScenarioSubType">
          <p>
            <cmr:label fieldId="custSubGrp">
              <cmr:fieldLabel fieldId="CustomerScenarioSubType" />:
            </cmr:label>
            <cmr:field fieldId="CustomerScenarioSubType" id="custSubGrp" path="custSubGrp" tabId="MAIN_GENERAL_TAB" size="250" />
          </p>
        </cmr:column>
      </cmr:view>
      <cmr:view forGEO="US">
        <cmr:column span="2" containerForField="CustomerGroup">
          <p>
            <cmr:label fieldId="custGrp">
              <cmr:fieldLabel fieldId="CustomerGroup" />:
            </cmr:label>
            <cmr:field fieldId="CustomerGroup" id="custGrp" path="custGrp" tabId="MAIN_GENERAL_TAB" size="250"/>
          </p>
        </cmr:column>
        <cmr:column span="2" containerForField="CustomerSubGroup">
          <p>
            <cmr:label fieldId="custSubGrp">
              <cmr:fieldLabel fieldId="CustomerSubGroup" />:
            </cmr:label>
            <cmr:field fieldId="CustomerSubGroup" id="custSubGrp" path="custSubGrp" tabId="MAIN_GENERAL_TAB" size="250"/>
          </p>
        </cmr:column>
      </cmr:view>
      <cmr:view forGEO="US">
      <cmr:column span="2" containerForField="CustomerType">
        <p>
          <cmr:label fieldId="custType">
            <cmr:fieldLabel fieldId="CustomerType" />:
          </cmr:label>
          <cmr:field fieldId="CustomerType" id="custType" path="custType" tabId="MAIN_GENERAL_TAB" size="250" />
        </p>
      </cmr:column>
      </cmr:view> 
      <cmr:view forGEO="LA">
      <c:if test="${reqentry.reqType != 'U'}">
      <cmr:column span="2" containerForField="CustomerType">
        <p>
          <cmr:label fieldId="custType">
            <cmr:fieldLabel fieldId="CustomerType" />:
          </cmr:label>
          <cmr:field fieldId="CustomerType" id="custType" path="custType" tabId="MAIN_GENERAL_TAB" size="250" />
        </p>
      </cmr:column>
      </c:if>
      </cmr:view>
    </cmr:row>
    
    <cmr:view forCountry="821">
      <cmr:row>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="cisServiceCustIndc">
              <cmr:field fieldId="CISServiceCustomer" id="cisServiceCustIndc" path="cisServiceCustIndc" tabId="MAIN_GENERAL_TAB" />
              <cmr:fieldLabel fieldId="CISServiceCustomer" />
            </cmr:label>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="dupIssuingCntryCd">
              <cmr:fieldLabel fieldId="CountryDuplicate" />
            </cmr:label>
            <cmr:field fieldId="CountryDuplicate" id="dupIssuingCntryCd" path="dupIssuingCntryCd" tabId="MAIN_GENERAL_TAB" />
          </p>
        </cmr:column>             
      </cmr:row>
    </cmr:view>
    <cmr:view forCountry="677,680,620,832,805,767,823,762,768,772,849,752">
      <cmr:row>    
        <cmr:column span="2">
          <p>
            <cmr:label fieldId="dupCmrIndc">
              <cmr:field fieldId="DuplicateCMR" id="dupCmrIndc" path="dupCmrIndc" tabId="MAIN_GENERAL_TAB" />
              <cmr:fieldLabel fieldId="DuplicateCMR" />
            </cmr:label>
          </p>
        </cmr:column>
      </cmr:row>      
    </cmr:view>
    
    <cmr:view forGEO="US,LA,CA">
      <cmr:row addBackground="false" topPad="10">
        <cmr:column span="2">
          <p>
              <cmr:label fieldId="mainCustNm1">
              <cmr:fieldLabel fieldId="MainCustomerName1"></cmr:fieldLabel>:
              <cmr:info text="${ui.info.privateCustomerName}" />
              <cmr:delta text="${reqentry.oldCustNm1}"  oldValue="${reqentry.mainCustNm1}"/> 
              </cmr:label>
              <cmr:field fieldId="MainCustomerName1" path="mainCustNm1" tabId="MAIN_GENERAL_TAB" size="250" placeHolder="Name 1"/>
          </p>
        </cmr:column>
        <cmr:column span="2">
          <p>
              <cmr:label fieldId="mainCustNm2">
                <cmr:fieldLabel fieldId="MainCustomerName2"></cmr:fieldLabel>:
                <cmr:info text="${ui.info.privateCustomerName}" />
                <cmr:delta text="${reqentry.oldCustNm2}"  oldValue="${reqentry.mainCustNm2}"/>
              </cmr:label>
              <cmr:field fieldId="MainCustomerName2" path="mainCustNm2" tabId="MAIN_GENERAL_TAB" size="250" placeHolder="Name 2"/>
          </p>
        </cmr:column>
        <%
        if (!readOnly) {
        %>
        <cmr:column span="1" containerForField="OldMainCustomerName1">
          <p>
              <cmr:label fieldId="oldCustNm1">
              <cmr:fieldLabel fieldId="OldMainCustomerName1"></cmr:fieldLabel>:
              </cmr:label>
              <cmr:field fieldId="OldMainCustomerName1" path="oldCustNm1" tabId="MAIN_GENERAL_TAB"/>
          </p>
        </cmr:column>
        <cmr:column span="1" containerForField="OldMainCustomerName2">
          <p>
              <cmr:label fieldId="oldCustNm2">
              <cmr:fieldLabel fieldId="OldMainCustomerName2"></cmr:fieldLabel>:
              </cmr:label>
              <cmr:field fieldId="OldMainCustomerName2" path="oldCustNm2" tabId="MAIN_GENERAL_TAB"/>
          </p>
        </cmr:column>
      <%
      }
      %>
      </cmr:row>
    </cmr:view>
    <cmr:view forGEO="EMEA,IERP,CND,AP,MCO,FR,MCO1,MCO2,CEMEA,JP,CN,NORDX,BELUX,NL,SWISS,TW">
      <form:hidden path="mainCustNm1" id="mainCustNm1"/>
      <form:hidden path="mainCustNm2" id="mainCustNm2"/>
    </cmr:view>
  <%} %>
      <cmr:row addBackground="true">
            <cmr:column span="2">
            <p>
              <cmr:label fieldId="requestingLob">
                <cmr:fieldLabel fieldId="RequestingLOB" />:
                <cmr:info text="${ui.info.requestingLOB}" />
              </cmr:label>
              <cmr:field id="requestingLob" path="requestingLob" fieldId="RequestingLOB" tabId="MAIN_GENERAL_TAB" size="250"/>
            </p>
            </cmr:column>
            <cmr:column span="2">
            <p>
              <cmr:label fieldId="reqReason">
                <cmr:fieldLabel fieldId="RequestReason" />:
              </cmr:label>
              <cmr:field id="reqReason" path="reqReason" fieldId="RequestReason" tabId="MAIN_GENERAL_TAB" size="250"/>
            </p>
            </cmr:column>
        <cmr:view forGEO="JP,TW,KR">
            <cmr:column span="2">
              <p>
                <label for="requesterId">Requester ID:</label>
                ${reqentry.requesterId} (${requesterId_UID})
              </p>
            </cmr:column>
        </cmr:view>
        <cmr:view forGEO="JP,TW,KR">
            <cmr:column span="2">
              <p>
                <label for="reqFor"><cmr:fieldLabel fieldId="OriginatorName" />:<cmr:info text="${ui.info.orgName}" /></label>
                <cmr:bluepages model="reqentry" namePath="originatorNm"  idPath="originatorId" useBothIds="false" showId="true"></cmr:bluepages>
              </p>
            </cmr:column>
        </cmr:view>
        <cmr:view exceptForGEO="JP,KR,TW">
              <cmr:column span="2">
                <p>
                  <label for="requesterId">Requester:</label>
                  <cmr:field model="reqentry" path="originatorNm" idPath="originatorId" fieldId="OriginatorName"/>
                </p>
              </cmr:column>
        </cmr:view>
      </cmr:row>
      <cmr:row addBackground="true">
            <cmr:column span="2">
              <p>
              <cmr:label fieldId="expediteInd">
                <cmr:fieldLabel fieldId="Expedite" />:<cmr:info text="${ui.info.expediteInd}" />
              </cmr:label>
              <cmr:field id="expediteInd" path="expediteInd" fieldId="Expedite" tabId="MAIN_GENERAL_TAB" size="250"/>
              </p>
            </cmr:column>
            <cmr:column span="2">
              <p>
              <cmr:label fieldId="expediteReason">
                <cmr:fieldLabel fieldId="ExpediteReason" />:
              </cmr:label>
              <cmr:field id="expediteReason" path="expediteReason" fieldId="ExpediteReason" tabId="MAIN_GENERAL_TAB" size="250"/>
              </p>
            </cmr:column>
      </cmr:row>
      <cmr:view forGEO="JP">
        <cmr:row addBackground="false" topPad="10">
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="icmsInd">
                <cmr:fieldLabel fieldId="ICMSContribution" />:
              </cmr:label>
              <cmr:field id="icmsInd" path="icmsInd" fieldId="ICMSContribution" tabId="MAIN_GENERAL_TAB"/>
              <div id="ofcdMessage" style="display: none; font-size: 14px; font-family: 'HelveticaNeue-Light','Helvetica Neue Light','Helvetica Neue','HelvLightIBM',Arial,sans-serif;">
                Please refer to the guide in
                <a href="notes://D19DBM06/4925745800402C1F/7B7D2EAD91F3FBCB4825701200264650/2500E3C8B1A1B7224925745E002C28CB">Notes</a>
              </div>
            </p>
          </cmr:column>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="requestDueDate">
                <cmr:fieldLabel fieldId="NuclChecklstDate" />:
              </cmr:label>
              <cmr:date id="requestDueDateTemp" path="requestDueDateTemp" format="yyyy-MM-dd" />
            </p>
          </cmr:column>
          <cmr:column span="2">
              <p>
              <cmr:label fieldId="email3">
                <cmr:fieldLabel fieldId="Email3" />:
              </cmr:label>
              <cmr:field id="email3" path="email3" fieldId="Email3" tabId="MAIN_GENERAL_TAB" size="255"/>
              </p>
            </cmr:column>
        </cmr:row>
      </cmr:view>
      
      <form:hidden path="cmt"/>
      <form:hidden path="yourId"/>
      <form:hidden path="yourNm" />
    </cmr:section>
    


