<%@page import="com.ibm.cio.cmr.request.util.RequestUtils"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  
%>
<cmr:checklist title1="Laos Customer Screening Checklist" title2="(Diversion Risk Profile / Proliferation / Military, Defense)">
  <cmr:chk-block>
    Customer Company Full Name
  </cmr:chk-block>
  <cmr:chk-lbl-field addSpace="false" labelWidth="12%" boldLabel="false" label="(in English):">
    ${reqentry.mainCustNm1} ${reqentry.mainCustNm2}
  </cmr:chk-lbl-field>
  
  <cmr:chk-section name="DPL Check" >
    <cmr:chk-entry dplField="true">
       <span style="font-weight:bold">US DPL and UN Sanction List Check:</span>
    </cmr:chk-entry>
    <cmr:chk-entry matchField="true">
       <span style="font-weight:bold">Match / Potential Match Found:</span>
    </cmr:chk-entry>
  </cmr:chk-section>
  
  
  <cmr:chk-block boldText="false">
      Please check the Customer Full Name against DPL database. 
      (Notes workspace <a href="<%=SystemConfiguration.getValue("DPL_CHECK_DB")%>">DPL database</a>, 
      DPL on IBM ERO Web Page and Monthly DPL file distributed to the functions.)      
      <br>
      ***If there is any match found, please STOP and REPORT to your manager, providing 
      following information to country ERC or your manager for further guidance. 
  </cmr:chk-block>

  <cmr:chk-block>
    Checklist
  </cmr:chk-block>


  <cmr:chk-block boldText="false" >
      To be completed by Marketing / Client Representative for new customers.<br> 
      Please select either Yes or No. Responses to all questions are <span style="font-weight:bold">mandatory</span>.
  </cmr:chk-block>


  <cmr:chk-section name="Section A" >
    <cmr:chk-entry number="1" section="A">
      The order (hardware and software) does not match the customer's business requirements.    
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="A">
      The customer is not using normal installation, training and maintenance services.    
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="A">
      The customer business needs and use of IBM products is not well known and understood by IBM.    
    </cmr:chk-entry>
    <cmr:chk-entry number="4" section="A">
      The customer has requested unusual payment or delivery terms and conditions.
    </cmr:chk-entry>
    <cmr:chk-entry number="5" section="A">
      There is an indication that the products are destined for <span style="font-weight:bold">an embargoed/terrorist 
      supporting country: Cuba, Iran, North Korea, Sudan, and Syria. </span>
      <br>
      * Cases to Iraq, please stop and contact with country ERC immediately. 
      <br>
      * Cases to Myanmar (Burma), please STOP and contact with country ERC immediately.    
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      There is an indication that the products are destined for a prohibited proliferation end 
      use/user(missiles, chemical & biological weapons, nuclear) in any of the following countries:
      <br>
      <span style="font-weight:bold;text-decoration:underline">Missile: </span>
      <br>
      Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Kuwait, Korea (North), Lebanon, Libya, Macao, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen
      <br>
      <span style="font-weight:bold;text-decoration:underline">Chemical & Biological Weapons: </span>
      <br>
      Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, 
      Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen
      <br>
      <span style="font-weight:bold;text-decoration:underline">Nuclear: </span>
      <br>
      People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia
      <br>
    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-section name="Section B" >
    <cmr:chk-entry>
      There is an indication that customer has an unauthorized dealings with parties and/or products 
      are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the following countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight:bold;text-decoration:underline">Missile</span>
      ----To the best of your knowledge or belief is your customer involved in the design, development, production or use of missiles in or by one of the 21 countries listed below?   
      <br>
      <br>
      Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Kuwait, Korea (North), Lebanon, Libya, Macao, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen                                                            
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
      <span style="font-weight:bold;text-decoration:underline">Chemical or Biological Weapons</span> ----To the best of your knowledge or belief is your customer involved with design, development, production, stockpiling or use of Chemical or Biological Weapons involved with design, development, production, stockpiling or use of Chemical or Biological Weapons in or by one of the 38 countries listed below?    
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
      <span style="font-weight:bold;text-decoration:underline">Nuclear</span> ----To the best of your knowledge or belief is your customer involved in nuclear activities in any of the 10 countries listed below?     
      <br>
      <br>
      People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia
    </cmr:chk-entry>
  </cmr:chk-section>
  
  <cmr:chk-section name="Section C" >
    <cmr:chk-entry number="1" section="C">
      <span style="font-weight:bold;text-decoration:underline">Military</span>
      ----To the best of your knowledge or belief is your customer involved with Military end use in or by one of the 27 countries listed below?   
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Belarus, Cambodia, People's Republic of China, Georgia, Iraq, Kazakhstan, Korea (North), Kyrgyzstan, Laos, Libya, Macao, Moldova, Mongolia, Myanmar (Burma), Russia, Sudan, Tajikistan, Turkmenistan, Ukraine, Uzbekistan, Venezuela, Vietnam.                                                            
    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-lbl-field addSpace="true" labelWidth="30%" boldLabel="true" label="Completed by Sales Representative:">
    ${reqentry.requesterNm} (${reqentry.requesterId})
  </cmr:chk-lbl-field>
</cmr:checklist>
  
