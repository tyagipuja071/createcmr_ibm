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
<cmr:checklist title1="CMR Military and Proliferation Screening Checklist" title2="CUSTOMER ELIGIBILITY CHECKLIST">

  <cmr:chk-section name="DPL Check" >
    <cmr:chk-entry dplField="true">
       <span style="font-weight:bold">Is the customer/vendor on the Denied Party List ?</span>
    </cmr:chk-entry>
  </cmr:chk-section>
    <cmr:chk-section name="Section A:  Diversion Risk Assessment" >
    <cmr:chk-entry number="1" section="A">
      The order (hardware, software, solution) does not match the customer's business requirements.    
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
      There is an indication that the products/services are destined for <span style="font-weight: bold;">sanctioned countries (Cuba, Iran, North Korea, & Syria)</span>.   
    </cmr:chk-entry>
  </cmr:chk-section>
  <cmr:chk-section name="Section B: Proliferation" >
  
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight: bold; text-decoration: underline">Missile:</span>
      To the best of your knowledge or belief is your customer involved in or supporting the design, 
      development, production, stockpiling, operation, installation (including on-site installation), 
      maintenance (checking), repair, overhaul, refurbishing, or use of, missiles (i.e. rocket systems and/or unmanned air vehicles)?
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
    <span style="font-weight: bold; text-decoration: underline">Chemical or Biological Weapons:</span>
     To the best of your knowledge or belief is your customer involved with or supporting the design, development,
      production, operation, installation (including on-site installation), maintenance (checking), repair, overhaul,
      refurbishing, stockpiling or use of chemical or biological weapons?
      <br>
      A. To the best of your knowledge or belief is your customer involved in any activity related to or in support of the design, development, production, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, refurbishing of a chemical plant? (NOTE: Questions A and B do not apply to IBM business by or in any of the following countries - Argentina, Australia, Austria, Belgium, Bulgaria, Canada, Croatia, Cyprus, Czech Republic, Denmark, Estonia, European Union, Finland, France, Germany, Greece, Hungary, Iceland, India, Ireland, Italy, Japan, Latvia, Lithuania, Luxembourg, Malta, Mexico, Netherlands, New Zealand, Norway, Poland, Portugal, South Korea, Turkey, Romania, Slovakia, Slovenia, Spain, Sweden, Switzerland, Ukraine, United Kingdom, United States.)
      <br>
      B. If yes to A above, is your customer involved in any activity related to, or in support of: the design, development, production, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, refurbishing of a whole plant of chemical weapons precursors specified in export control classification number<a href="https://www.bis.doc.gov/index.php/documents/regulations-docs/2332-category-1-materials-chemicals-microorganisms-and-toxins-4/file"> (ECCN) 1C350</a>?
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
    <span style="font-weight: bold; text-decoration: underline">Nuclear:</span>
     To the best of your knowledge or belief is your customer involved in any of the following nuclear activities :
     <br>
     A. <span style="font-weight: bold">Nuclear explosive activities </span> - including:   research on or development, design, manufacture,
      construction, testing or maintenance of any nuclear explosive device, or components or subsystems of such a device. 
      <br>
     B. <span style="font-weight: bold">Nuclear activities</span> - including:  research on, or development, design, manufacture, construction, operation, or maintenance of any "nuclear reactor" (including for non-weapon-related nuclear power generation), critical facility, facility for the fabrication of nuclear fuel,
        facility for the conversion of nuclear material from one chemical form to another, or separate storage, and nuclear activities in connection with a foreign maritime nuclear propulsion project. Or 
       <br>
     C. <span style="font-weight: bold">Sensitive nuclear activities</span> - including: nuclear fuel cycle activities, including research on or development, design, manufacture, construction, operation or maintenance of any of the following facilities, or components for such facilities:
     <br>
     <ul>
     <li>Facilities for the chemical processing of irradiated special nuclear or source material; </li>
     <li>Facilities for the production of heavy water; </li>
     <li>Facilities for the separation of isotopes of source and special nuclear material; or </li>
     <li>Facilities for the fabrication of nuclear reactor fuel containing plutonium. </li>
     </ul>
    </cmr:chk-entry>
    </cmr:chk-section>
    <cmr:chk-section name="Section C: Military" > 
    <cmr:chk-entry number="1" section="C">To the best of your knowledge or belief is your customer involved in
     <span style="font-weight: bold; text-decoration: underline"> Defense and/or Military*</span> activities? 
     <br>
     Involved in Military activities means falling under any of the two definitions below:
     <br>
     <span style="font-weight: bold; text-decoration: underline">Definitions </span>
     <br>
     <span style="font-weight: bold">Military End Users</span>: Include army, navy, air force, marines, coast guard, 
     national guard, security or police, government defense, intelligence and reconnaissance organizations
      (excludes organizations of the armed forces or national guard, that are covered under the military-intelligence
       end user definition below), and their contractors or any person or entity whose actions or functions are intended to 
       support military end uses.
      <br>
      <span style="font-weight: bold">Military End Uses</span>: incorporation into a military item described on the 
      U.S. Munitions List (USML) (22 CFR part 121, International Traffic in Arms Regulations); incorporation into items 
      classified under ECCNs ending in "A018" or under "600 series" ECCNs; or any item that supports or contributes to the
       operation, installation, maintenance, repair, overhaul, refurbishing, development, or production, of military items 
       described on the USML, or items classified under ECCNs ending in "A018" or under "600 series" ECCNs.
     </cmr:chk-entry>         
  </cmr:chk-section>   
  <cmr:chk-block boldText="false">
    If any of the above questions have answered 
    <span style="font-weight: bold; text-decoration: underline">YES</span>
    , please STOP and CONTACT your Country ERC or legal counsel for further guidance. 
    Transactions (with customers found to be engaged in prohibited activities) may only proceed as authorized by CHQ ERO and/or STC review.
  </cmr:chk-block>
 
</cmr:checklist>

