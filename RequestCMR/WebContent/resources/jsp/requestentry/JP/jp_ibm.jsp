<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="com.ibm.cio.cmr.request.util.BluePagesHelper" %>
<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
  
  String requestFor = reqentry.getPrivIndc();
  
  String prodType1 = reqentry.getCodCondition();
  String prodType2 = reqentry.getRemoteCustInd();
  String prodType3 = reqentry.getDecentralizedOptIndc();
  String prodType4 = reqentry.getImportActIndc();
  String prodType5 = reqentry.getMailingCondition();
  String prodType6 = reqentry.getSizeCd();
  String prodType7 = reqentry.getFootnoteNo();
  String prodType8 = reqentry.getFomeZero();
  
  boolean isJPBlueGroupFlg = true;
  AppUser user = AppUser.getUser(request);
  isJPBlueGroupFlg = BluePagesHelper.isUserInJPBlueGroup(user.getIntranetId());
  
  boolean isKSCMemberFlg = true;
  isKSCMemberFlg = user.isKSCMember();
%>
<style>
  .jp-chk {
    width: 250px;
    display: inline-block;
  }
</style>
<cmr:view forGEO="JP">
  <cmr:row addBackground="true">
  	
    <cmr:column span="2" containerForField="SalRepNameNo">
      <p>
        <cmr:label fieldId="repTeamMemberNo">
          <cmr:fieldLabel fieldId="SalRepNameNo" />:
           <cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" id="delta-repTeamMemberNo" />
        </cmr:label>
        <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="SalesSR" >
      <p>
        <cmr:label fieldId="salesTeamCd">
          <cmr:fieldLabel fieldId="SalesSR" />:
             <cmr:delta text="${rdcdata.salesTeamCd}" oldValue="${reqentry.salesTeamCd}" id="delta-salesTeamCd" />
        </cmr:label>
        <cmr:field fieldId="SalesSR" id="salesTeamCd" path="salesTeamCd" tabId="MAIN_IBM_TAB" />
      </p>
      </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">    
    <cmr:column span="2" containerForField="PrivIndc">
        <cmr:label fieldId="privIndc" >
          <cmr:fieldLabel fieldId="PrivIndc" />:<span id="ast-privIndc"></span>
          <cmr:delta text="-" id="delta-privIndc" code="L" />
        </cmr:label>
        <div id="privIndcCheckboxes" style="display: block">
      	  <input type="radio" name="privIndc" value="1" id="privIndc_1" <%= ("1".equals(requestFor))?"checked":"" %> />
    		  <label class=" cmr-radio-check-label" for="privIndc_1">So Projec/FHS-OAK</label>
          <br>
    		  <input  type="radio" name="privIndc" value="2" id="privIndc_2" <%= ("2".equals(requestFor))?"checked":"" %> />
    		  <label class=" cmr-radio-check-label" for="privIndc_2">NOS Porject</label>
          <br>          
    		  <input type="radio" name="privIndc" value="3" id="privIndc_3" <%= ("3".equals(requestFor))?"checked":"" %> />
    		  <label class=" cmr-radio-check-label" for="privIndc_3">So Infra/FHS-OTR(Infra)</label>
    	</div>
    </cmr:column> 
    <cmr:column span="2" containerForField="SalesBusOff" >
      <p>
        <cmr:label fieldId="salesBusOffCd">
          <cmr:fieldLabel fieldId="SalesBusOff" />:
             <cmr:delta text="${rdcdata.salesBusOffCd}" oldValue="${reqentry.salesBusOffCd}" id="delta-salesBusOffCd" />
        </cmr:label>
        <cmr:field fieldId="SalesBusOff" id="salesBusOffCd" path="salesBusOffCd" tabId="MAIN_IBM_TAB" />
      </p>
      </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="CmrNoPrefix">
        <p>
          <cmr:label fieldId="cmrNoPrefix">
            <cmr:fieldLabel fieldId="CmrNoPrefix" />:
          </cmr:label>
          <cmr:field path="cmrNoPrefix" id="cmrNoPrefix" fieldId="CmrNoPrefix" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    <cmr:column span="2" containerForField="OriginatorNo">
        <p>
          <cmr:label fieldId="orgNo">
            <cmr:fieldLabel fieldId="OriginatorNo" />:
          </cmr:label>
          <cmr:field path="orgNo" id="orgNo" fieldId="OriginatorNo" tabId="MAIN_IBM_TAB" />
        </p>
    </cmr:column>
  </cmr:row>
  <cmr:row addBackground="false">
    <cmr:column span="4">
      <cmr:label fieldId="ibmBankNumber">
        <cmr:fieldLabel fieldId="ProdType" />: <span id="ast-ProdType"></span>
          <cmr:delta text="-" id="delta-ibmBankNumber" code="L" />
      </cmr:label>
      <div id="prodTypeCheckboxes" style="display: block">
        <div class="jp-chk">
          <input type="checkbox" name="codCondition" value="1" id="prodType_1" <%= ("1".equals(prodType1))?"checked":"" %> />
          <label class=" cmr-radio-check-label" for="prodType_1">AAS HW</label>
        </div>
        <div class="jp-chk">
      		<input  type="checkbox" name="remoteCustInd" value="1" id="prodType_2" <%= ("1".equals(prodType2))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_2">AAS z9/zSeries SW</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="decentralizedOptIndc" value="1" id="prodType_3" <%= ("1".equals(prodType3))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_3">Others expect AAS z9/zSeries SW</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="importActIndc" value="1" id="prodType_4" <%= ("1".equals(prodType4))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_4">QCOS</label>
        </div>
        <div class="jp-chk">
      		<input  type="checkbox" name="mailingCondition" value="1" id="prodType_5" <%= ("1".equals(prodType5))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_5">Lenovo</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="sizeCd" value="1" id="prodType_6" <%= ("1".equals(prodType6))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_6">CISCO</label>
        </div>
        <div class="jp-chk">
      		<input  type="checkbox" name="footnoteNo" value="1" id="prodType_7" <%= ("1".equals(prodType7))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_7">Demo used</label>
        </div>
        <div class="jp-chk">
      		<input type="checkbox" name="fomeZero" value="1" id="prodType_8" <%= ("1".equals(prodType8))?"checked":"" %> />
      		<label class=" cmr-radio-check-label" for="prodType_8">Others</label>
        </div>
      </div>
    </cmr:column>  
    <br>
    <br>
    <br>
  </cmr:row>
  <cmr:row addBackground="false">
    
    <cmr:column span="2" containerForField="ChargeCd">
        <p>
          <cmr:label fieldId="chargeCd">
            <cmr:fieldLabel fieldId="ChargeCd" />:
          </cmr:label>
         <cmr:field path="chargeCd" id="chargeCd" fieldId="ChargeCd" tabId="MAIN_IBM_TAB" />
       </p>
     </cmr:column>
     <cmr:column span="2" containerForField="ProjectCd">
        <p>
          <cmr:label fieldId="soProjectCd">
            <cmr:fieldLabel fieldId="ProjectCd" />:
          </cmr:label>
         <cmr:field path="soProjectCd" id="soProjectCd" fieldId="ProjectCd" tabId="MAIN_IBM_TAB" />
       </p>
     </cmr:column>
  </cmr:row>
  <cmr:row addBackground="true">
    <cmr:column span="2" containerForField="CSDiv">
        <p>
          <cmr:label fieldId="csDiv">
            <cmr:fieldLabel fieldId="CSDiv" />:
          </cmr:label>
          <cmr:field path="csDiv" id="csDiv" fieldId="CSDiv" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
  </cmr:row> 
  <cmr:row addBackground="true"> 
     <cmr:column span="2" containerForField="BillingProcCd">
        <p>
          <cmr:label fieldId="billingProcCd">
            <cmr:fieldLabel fieldId="BillingProcCd" />:
          </cmr:label>
          <cmr:field path="billingProcCd" id="billingProcCd" fieldId="BillingProcCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
           <cmr:column span="2" containerForField="InvoiceSplitCd">
        <p>
      <cmr:label fieldId="invoiceSplitCd">
            <cmr:fieldLabel fieldId="InvoiceSplitCd" />:
          </cmr:label>
          <cmr:field path="invoiceSplitCd" id="invoiceSplitCd" fieldId="InvoiceSplitCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="CreditToCustNo">
        <p>
      <cmr:label fieldId="creditToCustNo">
            <cmr:fieldLabel fieldId="CreditToCustNo" />:
          </cmr:label>
          <cmr:field path="creditToCustNo" id="creditToCustNo" fieldId="CreditToCustNo" tabId="MAIN_IBM_TAB" />          
          <%if (!readOnly){%>
          	<img title="Import the CMR" id="btnImportBp" class="cmr-proceed2-icon" src="${resourcesPath}/images/search2.png" onclick="findImportCMR()" />
          <%}%>
        </p>
      </cmr:column>          
  </cmr:row>  
  <cmr:row addBackground="false"> 
      <cmr:column span="2" containerForField="CSBOCd">
        <p>
      <cmr:label fieldId="csBo">
            <cmr:fieldLabel fieldId="CSBOCd" />:
          </cmr:label>
          <cmr:field path="csBo" id="csBo" fieldId="CSBOCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
      <cmr:column span="2" containerForField="Tier2">
        <p>
      <cmr:label fieldId="tier2">
            <cmr:fieldLabel fieldId="Tier2" />:
          </cmr:label>
          <cmr:field path="tier2" id="tier2" fieldId="Tier2" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>  
      <cmr:column span="2" containerForField="BillToCustNo">
        <p>
      <cmr:label fieldId="billToCustNo">
            <cmr:fieldLabel fieldId="BillToCustNo" />:
          </cmr:label>
          <cmr:field path="billToCustNo" id="billToCustNo" fieldId="BillToCustNo" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>  
    </cmr:row> 
  <cmr:row addBackground="false"> 
      <cmr:column span="2" containerForField="AdminDeptCd">
        <p>
      <cmr:label fieldId="adminDeptCd">
            <cmr:fieldLabel fieldId="AdminDeptCd" />:
          </cmr:label>
          <cmr:field path="adminDeptCd" id="adminDeptCd" fieldId="AdminDeptCd" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>  
      <cmr:column span="2" containerForField="AdminDeptLine">
        <p>
      <cmr:label fieldId="adminDeptLine">
            <cmr:fieldLabel fieldId="AdminDeptLine" />:
          </cmr:label>
          <cmr:field path="adminDeptLine" id="adminDeptLine" fieldId="AdminDeptLine" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>  
      <cmr:column span="2" containerForField="ROLAccount">
        <p>
          <cmr:label fieldId="identClient">
            <cmr:fieldLabel fieldId="ROLAccount" />:<cmr:info text="${ui.info.rol}" />
             <cmr:delta text="-" id="delta-identClient" />
          </cmr:label>
          <cmr:field path="identClient" id="identClient" fieldId="ROLAccount" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
          	  <cmr:column span="2">
      		<p>
        	  <cmr:label fieldId="territoryCd"><cmr:fieldLabel fieldId="TaigaCode" />: </cmr:label>
        	  <cmr:field path="territoryCd" id="territoryCd" fieldId="TaigaCode" tabId="MAIN_IBM_TAB" />
      		</p>
     </cmr:column>
    </cmr:row> 
     <cmr:row addBackground="true"> 
      <cmr:column span="2" containerForField="AECISubDate">
        <p>
      <cmr:label fieldId="agreementSignDate">
            <cmr:fieldLabel fieldId="AECISubDate" />:
          </cmr:label>
          <cmr:field path="agreementSignDate" id="agreementSignDate" fieldId="AECISubDate" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>  
      <cmr:column span="2" containerForField="ModeOfPayment">
        <p>
      <cmr:label fieldId="modeOfPayment">
            <cmr:fieldLabel fieldId="ModeOfPayment" />:
          </cmr:label>
          <cmr:field path="modeOfPayment" id="modeOfPayment" fieldId="ModeOfPayment" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>  
      <cmr:column span="2">
      		<p>
        	  <cmr:label fieldId="marketingContCd"><cmr:fieldLabel fieldId="MarketingContCd" />: </cmr:label>
        	  <cmr:field path="marketingContCd" id="marketingContCd" fieldId="MarketingContCd" tabId="MAIN_IBM_TAB" />
      		</p>
     </cmr:column>
    </cmr:row> 
      <cmr:row addBackground="true"> 
	  <cmr:column span="2" containerForField="DealerNo">
	        <p>
	          <cmr:label fieldId="dealerNo"><cmr:fieldLabel fieldId="DealerNo" />: </cmr:label>
	          <cmr:field path="dealerNo" id="dealerNo" fieldId="DealerNo" tabId="MAIN_IBM_TAB" />
	        </p>
	    </cmr:column>
	</cmr:row>  
    
    <cmr:row addBackground="true"> 
      <cmr:column span="2" containerForField="JpCloseDays1">
        <p>
      <cmr:label fieldId="jpCloseDays1">
            <cmr:fieldLabel fieldId="JpCloseDays1" />:
          </cmr:label>
          <cmr:field path="jpCloseDays1" id="jpCloseDays1" fieldId="JpCloseDays1" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
      <cmr:column span="2" containerForField="JpPayCycles1">
        <p>
      <cmr:label fieldId="jpPayCycles1">
            <cmr:fieldLabel fieldId="JpPayCycles1" />:
          </cmr:label>
          <cmr:field path="jpPayCycles1" id="jpPayCycles1" fieldId="JpPayCycles1" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="JpPayDays1">
        <p>
      <cmr:label fieldId="jpPayDays1">
            <cmr:fieldLabel fieldId="JpPayDays1" />:
          </cmr:label>
          <cmr:field path="jpPayDays1" id="jpPayDays1" fieldId="JpPayDays1" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true"> 
      <cmr:column span="2" containerForField="JpCloseDays2">
        <p>
      <cmr:label fieldId="jpCloseDays2">
            <cmr:fieldLabel fieldId="JpCloseDays2" />:
          </cmr:label>
          <cmr:field path="jpCloseDays2" id="jpCloseDays2" fieldId="JpCloseDays2" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
      <cmr:column span="2" containerForField="JpPayCycles2">
        <p>
      <cmr:label fieldId="jpPayCycles2">
            <cmr:fieldLabel fieldId="JpPayCycles2" />:
          </cmr:label>
          <cmr:field path="jpPayCycles2" id="jpPayCycles2" fieldId="JpPayCycles2" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="JpPayDays2">
        <p>
      <cmr:label fieldId="jpPayDays2">
            <cmr:fieldLabel fieldId="JpPayDays2" />:
          </cmr:label>
          <cmr:field path="jpPayDays2" id="jpPayDays2" fieldId="JpPayDays2" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true"> 
      <cmr:column span="2" containerForField="JpCloseDays3">
        <p>
      <cmr:label fieldId="jpCloseDays3">
            <cmr:fieldLabel fieldId="JpCloseDays3" />:
          </cmr:label>
          <cmr:field path="jpCloseDays3" id="jpCloseDays3" fieldId="JpCloseDays3" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
      <cmr:column span="2" containerForField="JpPayCycles3">
        <p>
      <cmr:label fieldId="jpPayCycles3">
            <cmr:fieldLabel fieldId="JpPayCycles3" />:
          </cmr:label>
          <cmr:field path="jpPayCycles3" id="jpPayCycles3" fieldId="JpPayCycles3" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="JpPayDays3">
        <p>
      <cmr:label fieldId="jpPayDays3">
            <cmr:fieldLabel fieldId="JpPayDays3" />:
          </cmr:label>
          <cmr:field path="jpPayDays3" id="jpPayDays3" fieldId="JpPayDays3" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <input type="hidden" id="isJPBlueGroupFlg" name="isJPBlueGroupFlg" value="<%= isJPBlueGroupFlg %>" />
      <input type="hidden" id="isKSCMemberFlg" name="isKSCMemberFlg" value="<%= isKSCMemberFlg %>" />
    </cmr:row>
    <cmr:row addBackground="true"> 
      <cmr:column span="2" containerForField="JpCloseDays4">
        <p>
      <cmr:label fieldId="jpCloseDays4">
            <cmr:fieldLabel fieldId="JpCloseDays4" />:
          </cmr:label>
          <cmr:field path="jpCloseDays4" id="jpCloseDays4" fieldId="JpCloseDays4" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
      <cmr:column span="2" containerForField="JpPayCycles4">
        <p>
      <cmr:label fieldId="jpPayCycles4">
            <cmr:fieldLabel fieldId="JpPayCycles1" />:
          </cmr:label>
          <cmr:field path="jpPayCycles4" id="jpPayCycles4" fieldId="JpPayCycles4" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="JpPayDays4">
        <p>
      <cmr:label fieldId="jpPayDays4">
            <cmr:fieldLabel fieldId="JpPayDays4" />:
          </cmr:label>
          <cmr:field path="jpPayDays4" id="jpPayDays4" fieldId="JpPayDays4" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true"> 
      <cmr:column span="2" containerForField="JpCloseDays5">
        <p>
      <cmr:label fieldId="jpCloseDays5">
            <cmr:fieldLabel fieldId="JpCloseDays5" />:
          </cmr:label>
          <cmr:field path="jpCloseDays5" id="jpCloseDays5" fieldId="JpCloseDays5" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column> 
      <cmr:column span="2" containerForField="JpPayCycles5">
        <p>
      <cmr:label fieldId="jpPayCycles5">
            <cmr:fieldLabel fieldId="JpPayCycles5" />:
          </cmr:label>
          <cmr:field path="jpPayCycles5" id="jpPayCycles5" fieldId="JpPayCycles5" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
      <cmr:column span="2" containerForField="JpPayDays5">
        <p>
      <cmr:label fieldId="jpPayDays5">
            <cmr:fieldLabel fieldId="JpPayDays5" />:
          </cmr:label>
          <cmr:field path="jpPayDays5" id="jpPayDays5" fieldId="JpPayDays5" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
    </cmr:row>
    <cmr:row addBackground="true">
    
  <!-- Day 6 -->
  <cmr:column span="2" containerForField="JpCloseDays6">
    <p>
      <cmr:label fieldId="jpCloseDays6">
        <cmr:fieldLabel fieldId="JpCloseDays6" />:
      </cmr:label>
      <cmr:field path="jpCloseDays6" id="jpCloseDays6" fieldId="JpCloseDays6" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <cmr:column span="2" containerForField="JpPayCycles6">
    <p>
      <cmr:label fieldId="jpPayCycles6">
        <cmr:fieldLabel fieldId="JpPayCycles6" />:
      </cmr:label>
      <cmr:field path="jpPayCycles6" id="jpPayCycles6" fieldId="JpPayCycles6" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <cmr:column span="2" containerForField="JpPayDays6">
    <p>
      <cmr:label fieldId="jpPayDays6">
        <cmr:fieldLabel fieldId="JpPayDays6" />:
      </cmr:label>
      <cmr:field path="jpPayDays6" id="jpPayDays6" fieldId="JpPayDays6" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <!-- Day 7 -->
  <cmr:column span="2" containerForField="JpCloseDays7">
    <p>
      <cmr:label fieldId="jpCloseDays7">
        <cmr:fieldLabel fieldId="JpCloseDays7" />:
      </cmr:label>
      <cmr:field path="jpCloseDays7" id="jpCloseDays7" fieldId="JpCloseDays7" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <cmr:column span="2" containerForField="JpPayCycles7">
    <p>
      <cmr:label fieldId="jpPayCycles7">
        <cmr:fieldLabel fieldId="JpPayCycles7" />:
      </cmr:label>
      <cmr:field path="jpPayCycles7" id="jpPayCycles7" fieldId="JpPayCycles7" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <cmr:column span="2" containerForField="JpPayDays7">
    <p>
      <cmr:label fieldId="jpPayDays7">
        <cmr:fieldLabel fieldId="JpPayDays7" />:
      </cmr:label>
      <cmr:field path="jpPayDays7" id="jpPayDays7" fieldId="JpPayDays7" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <!-- Day 8 -->
  <cmr:column span="2" containerForField="JpCloseDays8">
    <p>
      <cmr:label fieldId="jpCloseDays8">
        <cmr:fieldLabel fieldId="JpCloseDays8" />:
      </cmr:label>
      <cmr:field path="jpCloseDays8" id="jpCloseDays8" fieldId="JpCloseDays8" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <cmr:column span="2" containerForField="JpPayCycles8">
    <p>
      <cmr:label fieldId="jpPayCycles8">
        <cmr:fieldLabel fieldId="JpPayCycles8" />:
      </cmr:label>
      <cmr:field path="jpPayCycles8" id="jpPayCycles8" fieldId="JpPayCycles8" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>

  <cmr:column span="2" containerForField="JpPayDays8">
    <p>
      <cmr:label fieldId="jpPayDays8">
        <cmr:fieldLabel fieldId="JpPayDays8" />:
      </cmr:label>
      <cmr:field path="jpPayDays8" id="jpPayDays8" fieldId="JpPayDays8" tabId="MAIN_IBM_TAB" />
    </p>
  </cmr:column>
</cmr:row>
    
</cmr:view>