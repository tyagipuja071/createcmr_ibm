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
<cmr:checklist title1="India Customer Screening Checklist">
  
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
      The customer's business needs and use of IBM's products is not well known and understood by IBM.    
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
      Lookup failed for key Question-CN*New*SectionA7   
    </cmr:chk-entry>
    <cmr:chk-entry number="8" section="A">
      Lookup failed for key Question-CN*New*SectionA8
    </cmr:chk-entry>
    <cmr:chk-entry number="9" section="A">
      Lookup failed for key Question-CN*New*SectionA9
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
      Bahrain, China, Egypt, India, Iran, Iraq, Israel, Jordan, North Korea, Kuwait, Lebanon, Libya, Macau, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates and Yemen.                                                            
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
      <span style="font-weight:bold;text-decoration:underline">Chemical or Biological Weapons</span> ----To the best of your knowledge or belief is your customer involved with design; development; production; stockpiling or use of Chemical or Biological Weapons in or by one of the 39 countries listed below?    
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, Bulgaria, China, Cuba, Egypt, Georgia, India, Iran, Iraq, Israel, Jordan, Kazakhstan, North Korea, Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, Ukraine, United Arab Emirates, Uzbekistan, Vietnam, Yemen.
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
      <span style="font-weight:bold;text-decoration:underline">Nuclear</span> ----To the best of your knowledge or belief is your customer involved in nuclear activities in any of the 10 countries listed below?     
      <br>
      <br>
      China; Cuba; India; Iran; Iraq; Israel; North Korea; Libya; Pakistan & Russia
    </cmr:chk-entry>
    <cmr:chk-entry>
      NOTE:; Certain Missile Technology projects have been identified in the following countries:; 1. China: M Series Missiles CSS-2; 2. India:; Agni; Prithvi; SLV-3 Satellite Launch Vehicle; Augmented Satellite Launch Vehicle (ASLV); Polar Satellite Launch Vehicle (PSLV); Geostationary Satellite Launch Vehicle (GSLV); 3. Iran: Surface-to-Surface Missile Project; Scud Development Project; 4. North Korea: No Dong 1; Scud Development Project; 5. Pakistan: Haft Series Missiles
    </cmr:chk-entry>
  </cmr:chk-section>
  
  <cmr:chk-section name="Section C" >
    <cmr:chk-entry>
      Lookup failed for key Question - ASEAN*ASEAN Section C
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="C">
      <span style="font-weight:bold;text-decoration:underline">Military</span>
      ----To the best of your knowledge or belief is your customer involved with military end use in or by one of the 32 countries listed below?   
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Belarus, Bulgaria, Cambodia, China, Cuba, Estonia, Georgia, India, Iran, Iraq, Kazakhstan, North Korea, 
      Kyrgyzstan, Laos, Latvia, Libya, Lithuania, Macao, Moldova, Mongolia, Romania, Russia, Sudan, Syria, Tajikstan, Turkmenistan, Ukraine, 
      Uzbekistan, Vietnam                                                            
    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-lbl-field addSpace="true" labelWidth="30%" boldLabel="true" label="Completed by Sales Representative:">
    ${reqentry.requesterNm} (${reqentry.requesterId})
  </cmr:chk-lbl-field>
</cmr:checklist>
  
