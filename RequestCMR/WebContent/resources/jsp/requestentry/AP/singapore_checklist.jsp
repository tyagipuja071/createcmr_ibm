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
<cmr:checklist title1="IBM Singapore Diversion Risk Red Flag Indicators">
   
  <cmr:chk-block>
    Checklist
  </cmr:chk-block>


  <cmr:chk-block boldText="false" >
      To be completed by Marketing / Client Representative<br> 
      Please circle Yes or No to answer all questions in the Checklist. 
      If any of the above questions have answered YES please STOP and CONTACT IBM Singapore ERC (Teo Soh Geok) at NOTES ID : Soh Geok Teo/Singapore/IBM 
      for further guidance. Transactions (wherein customers found to be engaged in prohibited activities) may only proceed as reviewed and authorized 
      by IBM ERC and / or AP STC.
      <br>
      <br>
      Each employee's obligation not to proceed if/when abnormal circumstances - or red flags - arise unless/until the matter has been satisfactorily 
      resolved.
      <br>
      <br>
      <span style="font-weight:bold">NOTE:</span> These abnormal circumstances - or red flags - indicate an export may be destined to an inappropriate end use end user or destination contrary to 
      US or local law. Some indicators of red flags include but are not limited to:
  </cmr:chk-block>


  <cmr:chk-section name="Section A" >
    <cmr:chk-entry number="1" section="A">
      The order (hardware, software, services, etc.) does not match the customer's business requirements.    
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
      A Business Partner (BP) places a second order for the same or similar products for a new / different 
      end user, soon after being informed that the order for the original end user was delayed or rejected due 
      to US export control restrictions.    
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      There is an indication that the products are destined for a sanctioned country/region.  
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
      There is an indication that the customer or supplier is owned or controlled by the government of a sanctioned country/region. 
    </cmr:chk-entry>
    <cmr:chk-entry number="8" section="A">
      There is an indication (anywhere in the world) that our products will be used in the design, development, production, 
      stockpiling or use of nuclear, chemical or biological weapons and/or their delivery vehicles 
      (a.k.a. rocket systems and/or unmanned air vehicles, a.k.a. missiles).
    </cmr:chk-entry>
    <cmr:chk-entry number="9" section="A">
      There is an indication that the products are destined for a 
      prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) 
      in any of the following countries: 
      <br>
      <span style="font-weight:bold">NUCLEAR: </span>
      People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia
      <br>
      <span style="font-weight:bold">CHEMICAL & BIOLOGICAL WEAPONS: </span>
      Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, 
      Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macao, 
      Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, 
      United Arab Emirates, Uzbekistan, Vietnam, Yemen
      <br>
      <span style="font-weight:bold">MISSILE: </span>
      Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Korea (North), Kuwait, Lebanon, Libya, Macao, Oman, Pakistan, 
      Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Yemen

    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-lbl-field addSpace="true" labelWidth="30%" boldLabel="true" label="Completed by Sales Representative:">
    ${reqentry.requesterNm} (${reqentry.requesterId})
  </cmr:chk-lbl-field>
</cmr:checklist>
  