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
<cmr:checklist title1="Macao Customer Screening Checklist" title2="(DIVERSION RISK PROFILE/PROLIFERATION & MILITARY CHECKLIST)">
  
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
      There is an indication that the products are destined for a sanctioned country/region:  
      <span style="font-weight:bold">Cuba, Iran, North Korea, Syria, and the Crimea, Donetsk and Luhansk regions of Ukraine.</span>
 	  <br>
 	  <br>
 	  In addition, opportunities involving Iraq, please STOP and contact your local ERC.  (USERP Section 1, Part 2.1.4)
    </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
      There is an indication that the customer or supplier is owned or controlled by the government of any of the sanctioned countries or region?
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
      There is an indication (anywhere in the world) that our products will be involved in or supporting the 
      design, development, production, stockpiling, operation, installation (including on-site installation), 
      maintenance (checking), repair, overhaul, refurbishing, or use of chemical or biological weapons and/or 
      their delivery vehicles (a.k.a. rocket systems and/or unmanned air vehicles, missiles)? 
    </cmr:chk-entry>
    <cmr:chk-entry number="8" section="A">
      There is an indication that the products are destined for a prohibited proliferation end 
      use/user (missiles & nuclear) in any of the following countries:
	  <br>
	  <br>
	  <span style="font-weight:bold">Missile:</span> Bahrain, People's Republic of China (includes Hong Kong), Cuba, Egypt, Iran, Iraq, 
	  Israel, Jordan, Korea (North), Kuwait, Lebanon, Libya, Macau, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates,
	   Venezuela, Yemen
	  <br>
	  <br>
	  <span style="font-weight:bold">Nuclear:</span> People's Republic of China (includes Hong Kong), Cuba, Iran, Iraq, Israel, 
	  Korea (North), Libya, Pakistan, Russia, Venezuela
	  (USERP Section 4, Part 2.5)
    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-section name="Section B" >
    <cmr:chk-entry>
      There is an indication that the customer has unauthorized dealings with parties and/or products that
      are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the following countries shown in each category:
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight:bold;text-decoration:underline">Missile</span>
      ----To the best of your knowledge or belief is your customer involved in or supports the design, development, production, operation, 
      installation (including on-site installation), maintenance (checking), repair, overhaul, or refurbishing of missiles (e.g., rocket 
      systems and/or unmanned air vehicles) in or by one of the countries listed below?
      (USERP Section 4, Part 2.3.4)
      <br>
      <br>
      Bahrain, People's Republic of China, Egypt, Iran, Iraq, Israel, Jordan, Korea (North), Kuwait, Lebanon, 
      Libya, Macau, Oman, Pakistan, Qatar, Russia, Saudi Arabia, Syria, United Arab Emirates, Venezuela & Yemen.
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
      <span style="font-weight:bold;text-decoration:underline">Chemical or Biological Weapons</span> <br>
      a) To the best of your knowledge or belief is your customer involved in any activity related to or in support of the design, 
      development, production, operation, stockpiling, installation (including on-site installation), maintenance (checking), repair, 
      overhaul, refurbishing, or use of Chemical or Biological Weapons?
     <br>
     <br>
     b) To the best of your knowledge or belief is your customer involved in any activity related to or in support of the design, development, 
     production, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, refurbishing of a chemical 
     plant? 
     (<span style="font-weight:bold">NOTE:</span> Questions B and C <span style="font-weight:bold">do not apply</span> to IBM business by or 
     in any of the following countries - Argentina, Australia, Austria, Belgium, Bulgaria, Canada, Croatia, Cyprus, Czech Republic, Denmark, 
     Estonia, European Union, Finland, France, Germany, Greece, Hungary, Iceland, India, Ireland, Italy, Japan, Latvia, Lithuania, Luxembourg, 
     Malta, Mexico, Netherlands, New Zealand, Norway, Poland, Portugal, South Korea, Turkey, Romania, Slovakia, Slovenia, Spain, Sweden, 
     Switzerland, Ukraine, United Kingdom, United States)
     <br>
     <br>
     c) If yes to b above, is your customer involved in any activity related to, or in support of: the design, development, production, operation, 
     installation (including on-site installation), maintenance (checking), repair, overhaul, refurbishing of a whole plant of chemical weapons 
     precursors specified in export control classification number 
     <a href="https://www.bis.doc.gov/index.php/documents/regulations-docs/2332-category-1-materials-chemicals-microorganisms-and-toxins-4/file">(ECCN) 1C350</a>?
     <br>
     <br>
     (USERP Section 4, Part 2.3.3)<br>
     Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, People's Republic of China, Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, 
     Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macau, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, 
     Syria, Taiwan, Tajikistan, Turkmenistan, Ukraine, United Arab Emirates, Uzbekistan, Venezuela, Vietnam & Yemen.
     </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
      <span style="font-weight:bold;text-decoration:underline">Nuclear</span> 
      ----To the best of your knowledge or belief is your customer involved in nuclear activities in any of the countries listed below?
     (USERP Section 4, Part 2.3.2)
     <br>
     <br>
     People's Republic of China, Cuba, Iran, Iraq, Israel, Korea (North), Libya, Pakistan, Russia & Venezuela
    </cmr:chk-entry>
  </cmr:chk-section>
  
  <cmr:chk-section name="Section C" >
    <cmr:chk-entry number="1" section="C">
      To the best of your knowledge or belief is your customer involved with <span style="font-weight:bold">Military</span> end use and or is a 
      <span style="font-weight:bold">Military end user</span> in or by one of the countries listed below?  
      (USERP Section, 4 Part 2.3.1)
      <br>
      <br>
      Afghanistan, Armenia, Azerbaijan, Belarus, Cambodia, People's Republic of China, Cuba, Georgia, Iran, Iraq, Kazakhstan, Korea (North), 
      Kyrgyzstan, Laos, Libya, Macao, Moldova, Mongolia, Myanmar (Burma) Russia, Sudan, Syria, Tajikistan, Turkmenistan, Ukraine, Uzbekistan, 
      Venezuela & Vietnam 
	  <br>
	  <br>
	  <span style="font-weight:bold">NOTE: </span> For Myanmar (Burma), Cambodia, Russia, Belarus, China and Venezuela checklist must also provide 
	  Military-Intelligence End Use and End User definitions
      <br>
      <br>
      <span style="font-weight:bold;text-decoration:underline">DEFINITIONS </span>
      <br>
      <br>
      <span style="font-weight:bold;">Military End Users: </span>
      Include army, navy, air force, marines, coast guard, national guard, security or police, government defense, intelligence and reconnaissance 
      organizations (excludes organizations of the armed forces or national guard, that are covered under the military-intelligence end user 
      definition below), and their contractors or any person or entity whose actions or functions are intended to support military end uses.
      <br>
      <br>
      <span style="font-weight:bold;">Military End Uses: </span>
      incorporation into a military item described on the U.S. Munitions List (USML) (22 CFR part 121, International Traffic in Arms Regulations); 
      incorporation into items classified under ECCNs ending in "A018" or under "600 series" ECCNs; or any item that supports or contributes to 
      the operation, installation, maintenance, repair, overhaul, refurbishing, development, or production, of military items described on the USML, 
      or items classified under ECCNs ending in "A018" or under "600 series" ECCNs.
      <br>
      <br>
      <span style="font-weight:bold;">Military-Intelligence End User: </span>
      means any intelligence or reconnaissance organization of the armed services (army, navy, marine, air force, or coast guard); or national guard.
      <br>
      <span style="font-weight:bold;">Military-Intelligence End Uses: </span>
      means the design, development, production, use, operation, installation (including on-site installation), maintenance (checking), repair, 
      overhaul, or refurbishing of, or incorporation into, items described on the U.S. Munitions List (USML) (22 CFR part 121, International 
      Traffic in Arms Regulations), or classified under ECCNs ending in "A018" or under "600 series" ECCNs, which are intended to support the 
      actions or functions of a 'military-intelligence end user,' as defined above.
      <br>
    </cmr:chk-entry> 
  </cmr:chk-section>
  
  <cmr:chk-section name="Section D" >
    <cmr:chk-entry number="1" section="D">
      <span style="font-weight:bold;text-decoration:underline">Supercomputer</span>
      ---- To the best of your knowledge or belief is your customer involved in the design, development, manufacturing, testing, etc. of a "supercomputer"? 
      <br>
      <br>
      a) Does your customer build supercomputers?
      <br>
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="D">
      <span style="font-weight:bold;text-decoration:underline">Supercomputer</span>
      ---- To the best of your knowledge or belief is your customer involved in the design, development, manufacturing, testing, etc. of a "supercomputer"? 
      <br>
      b) Does your customer own supercomputers?
      <br>
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="D">
      To the best of your knowledge or belief, is your customer involved in any activity to incorporate items into a supercomputer, 
      or the design, development, manufacturing, testing, etc. of any components/items that will be used in a "supercomputer"?
      <br>
      <br>
      <span style="font-weight:bold">NOTE:</span>
      this question is asking if the customer makes or supports tools/components that are used in a supercomputer or to build a supercomputer.
      <br>
     </cmr:chk-entry>
    <cmr:chk-entry>
      <span style="font-weight:bold;text-decoration:underline">DEFINITIONS</span>
      <br>
      <br>
      <span style="font-weight:bold">Supercomputer: </span>
      a high-performance multi-rack system having thousands of closely coupled compute cores connected in parallel with networking technology 
      and having a high peak power capacity requiring cooling elements. They are used for computationally intensive tasks including 
      scientific and engineering work. Supercomputers may include shared memory, distributed memory, or a combination of both. 
     <br>
    </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-section name="Section E" >
    <cmr:chk-entry number="1" section="E">
      <span style="font-weight:bold;text-decoration:underline">Semiconductor Manufacturing </span>
      ---- To the best of your knowledge or belief is your customer involved in the design, development, manufacturing, 
      testing, etc. of integrated circuits? 
      <br>
      <br>
      <span style="font-weight:bold">NOTE: </span>
      This question is asking if the customer is involved in the semiconductor or integrated circuit manufacturing process. 
      <br>
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="E">
      To the best of your knowledge or belief is your customer involved with the design, development, manufacturing, testing, etc. of any parts, components, or equipment <span style="font-style:italic;text-decoration:underline">of the tools</span> that are used in the production, manufacturing, testing, etc. of semiconductors. 
      <br>
      <br>
      <span style="font-weight:bold">NOTE: </span>
      This question is asking if the customer builds <span style="text-decoration:underline">tools</span> that are <span style="text-decoration:underline">used in</span> the semiconductor manufacturing process 
      <br>
     </cmr:chk-entry>
  </cmr:chk-section>

  <cmr:chk-lbl-field addSpace="true" labelWidth="30%" boldLabel="true" label="Completed by Sales Representative:">
    ${reqentry.requesterNm} (${reqentry.requesterId})
  </cmr:chk-lbl-field>
</cmr:checklist>
  
