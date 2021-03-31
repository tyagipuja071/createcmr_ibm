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
<cmr:checklist title1="DIVERSION RISK PROFILE/PROLIFERATION & MILITARY CHECKLIST">
  <!--
  <cmr:chk-section name="Customer Information:">
     <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label=" Customer Company: ">
     ${reqentry.mainCustNm1} ${reqentry.mainCustNm2}
     </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Address: ">
   <%=RequestUtils.generateChecklistLocalAddress(request)%>
  </cmr:chk-lbl-field>
    </cmr:chk-section>
  <cmr:chk-section name="DPL Check" >
    <cmr:chk-entry dplField="true">
       <span style="font-weight:bold">Is the customer/vendor on the Denied Party List ?</span>
    </cmr:chk-entry>
  </cmr:chk-section>
  -->

    <cmr:chk-section name="Section A" >
    <cmr:chk-entry number="1" section="A">
      The order (hardware and software) does not match the customer's business requirements.    
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="A">
      The customer is not using normal installation, training and maintenance services.    
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="A">
      The customer's business needs and use of IBM's products is not well known and understand by IBM.    
    </cmr:chk-entry>
    <cmr:chk-entry number="4" section="A">
      The customer has requested unusual payment or delivery terms and conditions.
    </cmr:chk-entry>
    <cmr:chk-entry number="5" section="A">
      A Business Partner (BP) places a second order for the same or similar products for a new / different end user, soon after being informed that the order for the original end user was delayed or rejected due to US export control restrictions?   
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      There is an indication that the products are destined for an embargoed or terrorist country (Cuba, Iran, North Korea, Sudan, Syria)?    
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
      There is an indication that the customer or supplier is owned or controlled by the government of an embargoed country?    
    </cmr:chk-entry>
    <cmr:chk-entry number="8" section="A">
      There is an indication (anywhere in the world) that our products will be used in the design, development, production, stockpiling or use of nuclear, chemical or biological weapons and/or their delivery vehicles (a.k.a. rocket systems and/or unmanned air vehicles, a.k.a. missiles)?
    </cmr:chk-entry>
    <cmr:chk-entry number="9" section="A">
      There is an indication that the products are destined for a prohibited proliferation end use/user(missiles, chemical & biological weapons, nuclear) in any of the following countries: 
Nuclear: People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia.
Chemical & Biological Weapons: Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen.
Missile: Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Korea (North), Kuwait, Lebanon, Libya, Macao, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen   
    </cmr:chk-entry>
  </cmr:chk-section>
    <cmr:chk-section name="Section B" >
    <cmr:chk-entry>
      There is an indication customer has an unauthorized dealings with parties and/or products are
      destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight:bold;text-decoration:underline">Missile</span>
      ----To the best of your knowledge or belief is your customer involved in the design, development, production or use of missiles in or by one of the countries listed below?   
      <br>
      Bahrain, People's Republic of China, 
      Egypt, Iran, Iraq, Israel, Jordan, Kuwait, Korea (North), Lebanon, Libya, Macao, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen.
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
      <span style="font-weight:bold;text-decoration:underline">Chemical or Biological Weapons</span> ----To the best of your knowledge or belief is your customer involved with design, development, production, stockpiling or use of Chemical or Biological Weapons involved with design, development, production, stockpiling or use of Chemical or Biological Weapons in or by one of the countries listed below?
     <br>
     Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Vietnam, Yemen.
     </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
      <span style="font-weight:bold;text-decoration:underline">Nuclear</span> ----To the best of your knowledge or belief is your customer involved in nuclear activities in any of the countries listed below?
     <br>
     People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia.
    </cmr:chk-entry>
  </cmr:chk-section>
  
  <cmr:chk-section name="Section C" >
    <cmr:chk-entry>
      There is an indication customer has an unauthorized dealings with parties and/or products are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="C">
      To the best of your knowledge or belief is your customer involved with Military end use in or by one of the countries listed below?    
      <br>
      Afghanistan, Armenia, Azerbaijan, Belarus, Cambodia, People's Republic of China, Georgia, 
      Iraq, Kazakhstan, Korea (North), Kyrgyzstan, Laos, Libya,  
      Macao, Moldova, Mongolia, Myanmar (Burma), Russia, Sudan, Tajikistan, Turkmenistan, Ukraine, Uzbekistan, Venezuela, Vietnam.                                                            
       <br>
       <br>
       <span style="font-weight: bold;">CERTIFICATION </span>
    </cmr:chk-entry>     
  </cmr:chk-section>
    <cmr:chk-section name="Address Verification" > 
    <cmr:chk-entry number="1" section="D">
     By selecting 'I confirm' you are certifying that any address you are entering into this customer record is a valid customer 
     address (not a location owned, leased or otherwise controlled by IBM), and if not a customer address is specifically 
     identified as an IBM or third party address in the entry.
     </cmr:chk-entry>   
  </cmr:chk-section>   
  <cmr:chk-block boldText="false">
    If any of the above questions have answered 
    <span style="font-weight: bold; text-decoration: underline">YES</span>
    , please STOP and CONTACT your Country ERC or legal counsel for further guidance. 
    Transactions (with customers found to be engaged in prohibited activities) may only proceed as authorized by CHQ ERO and/or AP STC.
  </cmr:chk-block>
 
</cmr:checklist>

