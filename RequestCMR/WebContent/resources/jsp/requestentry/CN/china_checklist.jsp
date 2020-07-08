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
<!--  
<cmr:checklist title1="China Customer Screening Checklist" title2="(Diversion Risk Profile / Proliferation / Military Checklist)">
  
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
      A Business Partner (BP) places a second order for the same or similar 
      products for a new / different end user, soon after being informed that the 
      order for the original end user was delayed or rejected due to US 
      export control restrictions?    
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      There is an indication that the products are destined for an embargoed or 
      terrorist country (Cuba, Iran, North Korea, Sudan, Syria)?   
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
      There is an indication that the customer or supplier is owned or controlled 
      by the government of an embargoed country?   
    </cmr:chk-entry>
    <cmr:chk-entry number="8" section="A">
      There is an indication (anywhere in the world) that our products will be used in the 
      design, development, production, stockpiling or use of nuclear, chemical or biological 
      weapons and/or their delivery vehicles (a.k.a. rocket systems and/or unmanned air vehicles, a.k.a. missiles)?
    </cmr:chk-entry>
    <cmr:chk-entry number="9" section="A">
      There is an indication that the products are destined for a prohibited proliferation end 
      use/user(missiles, chemical & biological weapons, nuclear) in any of the following countries: 
      <br>
      <br>
      Nuclear: People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia.
      <br>
      <br>
      Chemical & Biological Weapons: Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, 
      Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, 
      Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen.
      <br>
      <br>
      Missile: Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Korea (North), Kuwait, Lebanon, Libya, Macao, Oman, Pakistan, Qatar, 
      Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen.
    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-section name="Section B" >
    <cmr:chk-entry>
      There is an indication customer has an unauthorized dealings with parties and/or products 
      are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the following countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight:bold;text-decoration:underline">Missile</span>
      ----To the best of your knowledge or belief is your customer involved in the design, development, production or use of missiles in or by one of the countries listed below?   
      <br>
      <br>
      Bahrain, People's Republic of China, 
      Egypt, Iran, Iraq, Israel, Jordan, Kuwait, Korea (North), Lebanon, Libya, Macao, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen                                                            
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
      <span style="font-weight:bold;text-decoration:underline">Chemical or Biological Weapons</span> ----To the best of your knowledge or belief is your customer involved with design, development, production, stockpiling or use of Chemical or Biological Weapons in or by one of the countries listed below?    
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, 
      People's Republic of China,
      Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
      <span style="font-weight:bold;text-decoration:underline">Nuclear</span> ----To the best of your knowledge or belief is your customer involved in nuclear activities in any of the 10 countries listed below? (USERP Section 4 Part 2.2.2)      
      <br>
      <br>
      People's Republic of China, Cuba, India, Iran, Iraq, Israel, North Korea, Libya, Pakistan, Russia 
    </cmr:chk-entry>
    <cmr:chk-entry>
      NOTE:
    </cmr:chk-entry>
  </cmr:chk-section>
  
  <cmr:chk-section name="Section C" >
    <cmr:chk-entry>
      There is an indication customer has an unauthorized dealings with parties and/or products are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="C">
      To the best of your knowledge or belief is your customer involved with Military end use in or by one of the countries listed below?   
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Belarus, Cambodia, People's Republic of China, Georgia, 
      Iraq, Kazakhstan, Korea (North), Kyrgyzstan, Laos, Libya,  
      Macao, Moldova, Mongolia, Myanmar (Burma), Russia, Sudan, Tajikstan, Turkmenistan, Ukraine, Uzbekistan, Venezuela, Vietnam.                                                            
    </cmr:chk-entry>
  </cmr:chk-section>

</cmr:checklist>

-->
<!-- CMR - 4424 -->
<cmr:checklist title1="China Customer Screening Checklist" title2="(Diversion Risk Profile / Proliferation / Military , Defense)">
 <!--   <cmr:chk-block>
    Customer Company Full Name
  </cmr:chk-block>
  <cmr:chk-lbl-field addSpace="false" labelWidth="12%" boldLabel="false" label="(in English):">
    ${reqentry.mainCustNm1} ${reqentry.mainCustNm2}
  </cmr:chk-lbl-field>
  <cmr:chk-lbl-field addSpace="false" labelWidth="12%" boldLabel="false"  label= "" >
    <%=RequestUtils.generateChecklistLocalName(request)%>
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
      following information to China ERC or your manager for further guidance. 
  </cmr:chk-block>
  
   <cmr:chk-section name="Below are required to be filled in details to support customer eligibility determination." >
    <cmr:chk-lbl-field addSpace="true" boldLabel="false" labelWidth="40%" label="Customer Detail Address">
   <%=RequestUtils.generateChecklistLocalAddress(request)%>
  </cmr:chk-lbl-field>
  
  <cmr:chk-lbl-field addSpace="true" boldLabel="false" labelWidth="40%" label="Nature of Business: (Please provide web site if available)">
   <%=RequestUtils.generateChecklistFreeTxtField1(request)%>
  </cmr:chk-lbl-field>
  
  <cmr:chk-lbl-field addSpace="true" boldLabel="false" labelWidth="40%" label="What products/service etc. they want to buy?">
   <%=RequestUtils.generateChecklistFreeTxtField2(request)%>
  </cmr:chk-lbl-field>
  
  <cmr:chk-lbl-field addSpace="false" boldLabel="false" labelWidth="12%" label="Purpose of End Use">
   <%=RequestUtils.generateChecklistFreeTxtField3(request)%>
  </cmr:chk-lbl-field>
  </cmr:chk-section>
  
  <cmr:chk-block>Transactions (with customers found DPL matched) may only proceed as reviewed and authorized by IBM ERO (Export Regulation Office) and/or AP STC (Sensitive Transaction Council).
   To be completed by Marketing / Client Representative for new customers.
   <br>
   -->
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
      A Business Partner (BP) places a second order for the same or similar 
      products for a new / different end user, soon after being informed that the 
      order for the original end user was delayed or rejected due to US 
      export control restrictions?    
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      There is an indication that the products are destined for an embargoed or 
      terrorist country (Cuba, Iran, North Korea, Sudan, Syria or the Crimea Region of Ukraine)?   
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
      There is an indication that the customer or supplier is owned or controlled 
      by the government of any of the sanctioned countries or region?  
    </cmr:chk-entry>
    <cmr:chk-entry number="8" section="A">
      There is an indication (anywhere in the world) that our products will be used in the 
      design, development, production, stockpiling or use of nuclear, chemical or biological 
      weapons and/or their delivery vehicles (a.k.a. rocket systems and/or unmanned air vehicles, a.k.a. missiles)?
    </cmr:chk-entry>
    <cmr:chk-entry number="9" section="A">
      There is an indication that the products are destined for a prohibited proliferation end 
      use/user(missiles, chemical & biological weapons, nuclear) in any of the following countries: 
      <br>
      <br>
      Nuclear: People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia.
      <br>
      <br>
      Chemical & Biological Weapons: Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, 
      Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, 
      Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen.
      <br>
      <br>
      Missile: Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Korea (North), Kuwait, Lebanon, Libya, Macao, Oman, Pakistan, Qatar, 
      Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen.
    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-section name="Section B" >
    <cmr:chk-entry>
      There is an indication that the customer has unauthorized dealings with parties and/or products that
      are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the following countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight:bold;text-decoration:underline">Missile</span>
      ----To the best of your knowledge or belief is your customer involved in the design, development, production or use of missiles in or by one of the countries listed below?   
      <br>
      <br>
      Bahrain, People's Republic of China, 
      Egypt, Iran, Iraq, Israel, Jordan, Kuwait, Korea (North), Lebanon, Libya, Macao, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen                                                            
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
      <span style="font-weight:bold;text-decoration:underline">Chemical or Biological Weapons</span> ----To the best of your knowledge or belief is your customer involved with design, development, production, stockpiling or use of Chemical or Biological Weapons involved with design, development, production, stockpiling or use of Chemical or Biological Weapons in or by one of the countries listed below?
     <br>
     Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen
     </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
      <span style="font-weight:bold;text-decoration:underline">Nuclear</span> ----To the best of your knowledge or belief is your customer involved in nuclear activities in any of the countries listed below?
     <br>
     People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia
    </cmr:chk-entry>
  </cmr:chk-section>
  
  <cmr:chk-section name="Section C" >
    <cmr:chk-entry>
      There is an indication customer has an unauthorized dealings with parties and/or products are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="C">
      To the best of your knowledge or belief is your customer involved with Military end users or end uses (see definitions) in or by one of the countries listed below?   
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Belarus, Cambodia, People's Republic of China, Georgia, 
      Iraq, Kazakhstan, Korea (North), Kyrgyzstan, Laos, Libya,  
      Macao, Moldova, Mongolia, Myanmar (Burma), Russia, Sudan, Tajikistan, Turkmenistan, Ukraine, Uzbekistan, Venezuela, Vietnam.                                                            
   <br>
     <span style="font-weight:bold;text-decoration:underline">Definitions </span>
     <br>
       <span style="font-weight:bold;">Military End Users : </span>
      Include army, navy, air force, marines, coast guard, national guard, security or police, government defense, intelligence and reconnaissance organizations, and their contractors or any person or entity whose actions or functions are intended to support military end uses.<br>
     <span style="font-weight:bold;">Military End Uses : </span>
      That will be directly part, component or subsystems of weapons or defense articles, or that indirectly supports or contributes to the operation, installation, maintenance, repair, overhaul, refurbishing, development or production of military items.
    
    </cmr:chk-entry>     
  </cmr:chk-section>
</cmr:checklist>
  