<%@page import="com.ibm.cio.cmr.request.util.RequestUtils"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<% 
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
%>
<cmr:section>
<form>
	<input name="dummyform" type="hidden">
</form>
<form _csrf="GhtjeYhfngleOImde2" id="fileTemplateDownloadForm" name="fileTemplateDownloadForm" method="POST" action="${contextPath}/template/download" target="fileTemplateDownloadFrame">
  <input name="dlTokenId" id="dlTokenId" type="hidden">
  <input name="dlDocType" id="dlDocType" type="hidden">
  <input name="dlReqId" id="dlReqId" type="hidden">
  <input name="dlIterId" id="dlIterId" type="hidden">
  <input type="hidden" name="cmrIssuingCntry" value="${reqentry.cmrIssuingCntry}">
  <input type="hidden" name="_csrf" id="_csrf" value="GhtjeYhfngleOImde2" />
</form>
</cmr:section>
<iframe id="fileTemplateDownloadFrame" style="display:none" name="fileTemplateDownloadFrame"></iframe>
<cmr:checklist title1="CMR Military and Proliferation Screening Checklist" title2="CUSTOMER ELIGIBILITY CHECKLIST">
  <cmr:column span="7">
    <p align="right">
      <label for="massUpdtTmpl"> ${ui.il.dplchecklisttemplate}: <cmr:info text="${ui.info.dplchecklisttemplateIL}"></cmr:info> </label>
      <a href="javascript:downloadDPLChecklistTemplate()">${ui.massDnlTmpl}</a>
    </p>
  </cmr:column>
  <cmr:chk-section name="Section A">
    <cmr:chk-entry number="1" section="A">
      Is the customer on the DPL?    
    </cmr:chk-entry>  
    <cmr:chk-entry number="2" section="A">
      The order (hardware and software) does not match the customer's business requirements    
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="A">
      The customer is not using normal installation, training and maintenance services.    
    </cmr:chk-entry>
    <cmr:chk-entry number="4" section="A">
      The customer business needs and use of IBM products is not well known and understood by IBM.    
    </cmr:chk-entry>
    <cmr:chk-entry number="5" section="A">
      The customer has requested unusual payment or delivery terms and conditions. 
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      There is an indication that the products are destined for a <span style="font-weight: bold;">sanctioned country/region: (Cuba, Iran, North Korea, Syria and the Crimea, Donetsk and Luhansk regions of Ukraine)</span>.
      <br>
      <span style="font-weight: bold;">In addition,</span> opportunities involving Iraq, please STOP and contact your local ERC.  (USERP Section 1, Part 2.1.4)   
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
      There is an indication that the products are destined for a prohibited proliferation end use/user (missiles & nuclear) in any of the following countries: <br>
      <span style="font-weight: bold;">Missile:</span> Bahrain, People's Republic of China (includes Hong Kong), Cuba, Egypt, Iran, Iraq, Israel, Jordan, Korea (North), Kuwait, Lebanon, Libya, Macau, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Venezuela, Yemen <br>
      <span style="font-weight: bold;">Nuclear:</span> People's Republic of China (includes Hong Kong), Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia, Venezuela (USERP Section 4, Part 2.5)
    </cmr:chk-entry>    
  </cmr:chk-section>
  <cmr:chk-section name="Section B">
  
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight: bold; text-decoration: underline">Missile:</span>
      To the best of your knowledge or belief is your customer involved in or supports the design, development, production, 
      operation, installation (including on-site installation), maintenance (checking), repair, overhaul, or refurbishing 
      of missiles (e.g., rocket systems and/or unmanned air vehicles) in or by one of the countries listed below? (USERP Section 4, Part 2.3.4) <br>   
	  Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Korea (North), Kuwait, Lebanon, Libya, Macau, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Venezuela & Yemen. 
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
    <span style="font-weight: bold; text-decoration: underline">Chemical or Biological Weapons:</span>
      a) To the best of your knowledge or belief is your customer involved in any activity related to or in support of the design, development, production, operation, stockpiling, installation (including on-site installation), maintenance (checking), repair, overhaul, refurbishing, or use of Chemical or Biological Weapons? 
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
	  b) To the best of your knowledge or belief is your customer involved in any activity related to or in support of the design, development, production, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, refurbishing of a chemical plant? 
	  (<span style="font-weight: bold;">NOTE:</span> Questions B and C <span style="font-weight: bold;">do not apply</span> to IBM business by or in any of the following countries - Argentina, Australia, Austria, Belgium, Bulgaria, Canada, Croatia, Cyprus, Czech Republic, Denmark, Estonia, European Union, Finland, France, Germany, Greece, Hungary, Iceland, India, Ireland, Italy, Japan, Latvia, Lithuania, Luxembourg, Malta, Mexico, Netherlands, New Zealand, Norway, Poland, Portugal, South Korea, Turkey, Romania, Slovakia, Slovenia, Spain, Sweden, Switzerland, Ukraine, United Kingdom, United States)
    </cmr:chk-entry>
    <cmr:chk-entry number="4" section="B">
      c) If yes to b above, is your customer involved in any activity related to, or in support of: the design, development, production, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, refurbishing of a whole plant of chemical weapons precursors specified in export control classification number<a href="https://www.bis.doc.gov/index.php/documents/regulations-docs/2332-category-1-materials-chemicals-microorganisms-and-toxins-4/file"> (ECCN) 1C350</a>? <br>
      (USERP Section 4, Part 2.3.3)     
      Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macau, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, Ukraine, United Arab Emirates, Uzbekistan, Venezuela, Vietnam & Yemen.          
    </cmr:chk-entry>
    <cmr:chk-entry number="5" section="B">
      <span style="font-weight: bold; text-decoration: underline">Nuclear:</span>
      To the best of your knowledge or belief is your customer involved in nuclear activities in any of the countries listed below?
      (USERP Section 4, Part 2.3.2)    <br>
      People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia & Venezuela
    </cmr:chk-entry>
    </cmr:chk-section>
    <cmr:chk-section name="Section C" > 
    <cmr:chk-entry number="1" section="C">To the best of your knowledge or belief is your customer involved with
     <span style="font-weight: bold; text-decoration: underline">Military</span> end use and or is a
     <span style="font-weight: bold;">Military end user</span> in or by one of the countries listed below?  (USERP Section, 4 Part 2.3.1) <br>
     Afghanistan, Armenia, Azerbaijan, Belarus, Cambodia, People's Republic of China, Cuba, Georgia, Iran, Iraq, Kazakhstan, Korea (North), Kyrgyzstan, Laos, Libya, Macau, Moldova, Mongolia, Myanmar (Burma) Russia, Sudan, Syria, Tajikistan, Turkmenistan, Ukraine, Uzbekistan, Venezuela & Vietnam 
     <br>
     <span style="font-weight: bold; text-decoration: underline">Definitions </span>
     <br>
     <span style="font-weight: bold">Military End Users</span>: Include army, navy, air force, marines, coast guard, national guard, security or police, government defense, 
     intelligence and reconnaissance organizations (excludes organizations of the armed forces or national guard, that are covered under the military-intelligence end user definition below), 
     and their contractors or any person or entity whose actions or functions are intended to support military end uses.
      <br>
      <span style="font-weight: bold">Military End Uses</span>: incorporation into a military item described on the U.S. Munitions List (USML) (22 CFR part 121, 
      International Traffic in Arms Regulations); incorporation into items classified under ECCNs ending in "A018" or under "600 series" ECCNs; 
      or any item that supports or contributes to the operation, installation, maintenance, repair, overhaul, refurbishing, development, or production, 
      of military items described on the USML, or items classified under ECCNs ending in "A018" or under "600 series" ECCNs.
      <br>
      <span style="font-weight: bold">Military-Intelligence End User</span>: means any intelligence or reconnaissance organization of the armed services (army, navy, marine, air force, or coast guard); or national guard. <br>
      <span style="font-weight: bold">Military-Intelligence End Uses</span>: means the design, development, production, use, operation, installation (including on-site installation), 
      maintenance (checking), repair, overhaul, or refurbishing of, or incorporation into, items described on the U.S. Munitions List (USML) (22 CFR part 121, International Traffic in Arms Regulations), 
      or classified under ECCNs ending in "A018" or under "600 series" ECCNs, which are intended to support the actions or functions of a 'military-intelligence end user,' as defined above.  
     </cmr:chk-entry>         
  </cmr:chk-section>   
  <cmr:chk-block boldText="false">
    If any of the above questions have answered 
    <span style="font-weight: bold; text-decoration: underline">YES</span>
    , please STOP and CONTACT <span id="checklistcontact">
    <% if("CROSS".equals(reqentry.getCustGrp())) {
     %> your Country ERC  <% } else {%>
       Israeli ERC (Yifat Singer @ NOTES ID: IJB@il.ibm.com, Yifat Singer/Israel/IBM)
     <% } %> </span>
     or legal counsel for further guidance.
    Transactions (with customers found to be engaged in prohibited activities) may only proceed as reviewed and authorized by IBM ERC and/or STC.
  </cmr:chk-block>
 
</cmr:checklist>

