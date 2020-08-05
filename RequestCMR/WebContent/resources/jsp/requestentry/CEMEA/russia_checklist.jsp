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
<cmr:checklist title1="Russia CMR Military and Proliferation Screening Checklist" title2="Diversion Risk Profile / Proliferation / Military">

  <cmr:chk-section name="DRP Questionnaire">
    <cmr:chk-entry>
      <span style="font-weight: bold; text-decoration: underline">Military End-User or End-Use involved:</span>
    </cmr:chk-entry>
    <cmr:chk-entry>
      Notes:
      <br>
      Answer question two with yes, if end-user/use is quasi-military
      <br>
      <br>
      Military end user/end use is broadly defined to include not only traditional military establishments and laboratories, 
      but also quasi-military organizations like Federal Security Service and may include seemingly non-military
      end users /end uses which may be involved in military contracts or in military-related activities.
      <br>
      <span style="font-weight: bold">Definitions for Reference:</span>
      <br>
      <span style="font-weight: bold; text-decoration: underline">Military End Users:</span>
      Include army, navy, air force, marines, coast guard, national guard, security or police, government defense, intelligence 
      and reconnaissance organizations, and their contractors or any person or entity whose actions or functions are intended
      to support military end uses.
      <br>
      <br>
      <span style="font-weight: bold; text-decoration: underline">Military End Uses:</span>
       That will be directly part, component or subsystems of weapons or defense articles, or that indirectly supports
       or contributes to the operation, installation, maintenance, repair, overhaul, refurbishing, development or production of military items.
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="A">
      Is the customer part of the military or involved in any military activity?
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="A">
      Will any military organization make use of or benefit from this transaction/installation?
      <br>
      <br>
    </cmr:chk-entry>

    <cmr:chk-entry>
      <span style="font-weight: bold; text-decoration: underline">Nuclear and proliferation activities:</span>
    </cmr:chk-entry>
    <cmr:chk-entry>
      Notes:
      <br>
      1. Nuclear explosive activities include research on or development, design, manufacture,
      construction, testing or maintenance of any nuclear explosive device, or components or subsystems of such a device.
      <br>
      2. Nuclear activities include research on, or development, design, manufacture, construction,
      operation, or maintenance of any "nuclear reactor" (including for non-weapon-related nuclear power generation), critical facility, 
      facility for the fabrication of nuclear fuel, facility for the conversion of nuclear material from one chemical form to another, or separate storage.
      <br>
      3. Sensitive nuclear activities are: nuclear fuel cycle activities, including research on or development design, manufacture, construction, 
      operation or maintenance of any of the following facilities or components for such facilities:
      <br>
      - Facilities for the chemical processing of irradiated special nuclear or source material;<br>
      - Facilities for the production of heavy water;<br>
      - Facilities for the separation of isotopes of source and special nuclear material; or<br>
      - Facilities for the fabrication of nuclear reactor fuel containing plutonium<br>
      <br>
      To the best of your knowledge/belief:
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="A">
      Is the customer involved in any nuclear activities?
    </cmr:chk-entry>
    <cmr:chk-entry number="4" section="A">
      Is the customer related to or owned or controlled by any organization involved with any nuclear activity?
    </cmr:chk-entry>
    <cmr:chk-entry number="5" section="A">
      Is the customer involved in the design, development, production or use of missiles?
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      Is the customer involved in the design, development, production, stockpiling or use of chemical or biological weapons?
      <br>
      <br>
    </cmr:chk-entry>

    <cmr:chk-entry>
      <span style="font-weight: bold; text-decoration: underline">Diversion and Re-export risk assessment</span>
    </cmr:chk-entry>
    <cmr:chk-entry>
      IBM is responsible for the information received during the normal course of conducting business; 
      if we receive information which indicates that there is a possibility of 'DIVERSION' to one of the above described activities 
      or an entity involved in these activities, the transaction must be stopped and all questions must be resolved. 
      The ERC will take the lead in resolving these questions.
      <br>
      <br>
      Some indicators of diversion are:
      <br>
      1. The order does not match the customer's business requirements.<br>
      2. The customer is not using normal installation, training, maintenance services<br>
      3. We are unable to determine the customer's business needs or how he will use our products<br>
      4. The customer is not well known or has no business background<br>
      5. The customer requests unusual payment or delivery terms and conditions.<br>
      <br>
      (The list is illustrative, not comprehensive)
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
      Based on your knowledge of this customer, have you identified any indicators of a potential diversion?
      <br>
    </cmr:chk-entry>

    <cmr:chk-entry number="8" section="A">
    <span style="font-weight: bold; text-decoration: underline">Re-export:</span>
    <br>
      To the best of your knowledge and belief, is there any indication that the customer intends to re-export the products? 
    </cmr:chk-entry>
    <cmr:chk-entry>
      If the Country is 'YES', list the country or countries involved and contact the ERC for assistance:
      <br>
    </cmr:chk-entry>
    <cmr:chk-lbl-field addSpace="false" boldLabel="false" label="">
       <%=RequestUtils.generateChecklistFreeTxtField1(request)%>
       <br>
    </cmr:chk-lbl-field>

    <cmr:chk-entry>
      <span style="font-weight: bold; text-decoration: underline">Embassies or consulates:</span>
    </cmr:chk-entry>
    <cmr:chk-entry number="9" section="A">
      Is the customer a representation of an embargoed/terrorist country?
      <br>
    </cmr:chk-entry>

    <cmr:chk-entry>
      <span style="font-weight: bold; text-decoration: underline">Controlled in fact customers</span>
    </cmr:chk-entry>
    <cmr:chk-entry number="10" section="A">
      To the best of your knowledge and belief, is the customer partially/fully owned or controlled by a
      government, - company/organisation or person of an embargoed/terrorist country (see USERP,
      chapter 1.5)
      <br>
    </cmr:chk-entry>
    </cmr:chk-section> 
    <cmr:chk-section name="Section B"> 
     <cmr:chk-entry number="1" section="B">
      Does the entity have any persons in its shareholder's structure that are subject to sanctions imposed by any government body,
      including the United Nations, the United States, and the European Union (collectively, "Sanctions" ) 
      see https://w3-03.ibm.com/legal/denied-parties-list/denied.nsf.
      <br>
    </cmr:chk-entry>
    <cmr:chk-entry>
      <span style="font-weight: bold; text-decoration: underline">Business Partners</span>
    </cmr:chk-entry>
    <cmr:chk-entry>
      Business Partners are subject to the same screening requirements as normal customers
    </cmr:chk-entry>
  </cmr:chk-section>    
    
  <cmr:chk-block boldText="false">
    <span style="font-weight: bold; text-decoration: underline">Other screening requirements:</span>
    <br>
    End Users who are identified by either our Business Partners or customers are also subject to the
    above screening requirements.
    <br>
    Please list names:
  </cmr:chk-block>
    
  <cmr:chk-lbl-field addSpace="false" boldLabel="false" labelWidth="12%" label="Name, Customer number">
   <%=RequestUtils.generateChecklistFreeTxtField2(request)%>
  </cmr:chk-lbl-field>
  <cmr:chk-lbl-field addSpace="false" boldLabel="false" labelWidth="12%" label="Name, Customer number">
   <%=RequestUtils.generateChecklistFreeTxtField3(request)%>
  </cmr:chk-lbl-field>

  <cmr:chk-block boldText="false">
    Screening is required for each new customer and for every new transaction in Armenia, Azerbaijan, Belarus, 
    Georgia, Kazakhstan, Kirghizia, Russia, Turkmenistan, Tajikistan, Ukraine and Uzbekistan. 
    <br>
    <br>
    If the customer status changes during the course of a transaction or whenever IBM becomes aware of a change 
    a new screening must be completed immediately to reflect the changes or the new information. 
  </cmr:chk-block>

  <cmr:chk-lbl-field addSpace="true" labelWidth="30%" boldLabel="true" label="Completed by Sales Representative:">
    ${reqentry.requesterNm} (${reqentry.requesterId})
  </cmr:chk-lbl-field>
</cmr:checklist>

