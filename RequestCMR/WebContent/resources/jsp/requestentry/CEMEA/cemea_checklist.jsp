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
	<cmr:checklist title1="MULTIPURPOSE PROLIFERATION AND MILITARY SCREENING CHECKLIST (Supplement 2.5 to USERP Section 4 Part 2)">
		<cmr:chk-block>Applicable to the following countries:<br>
	Abu Dhabi, Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, Cambodia, People's Republic of China (including Hong Kong), 
	Egypt, Georgia, Iraq, Israel, Jordan, Kazakhstan, Kuwait, Kyrgyzstan, Laos, Lebanon, Libya, Macau, Moldova, Mongolia, 
	Myanmar (Burma), Oman, Pakistan, Palestine, Qatar, Russia, Saudi Arabia, Taiwan, Tajikistan, 
	Turkmenistan, Ukraine, United Arab Emirates, Uzbekistan, Venezuela, Vietnam, Yemen
		</cmr:chk-block>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Customer Company Full Name: ">
      <%=RequestUtils.generateChecklistLocalAddress(request)%>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Customer Complete Address: ">
      <%=RequestUtils.generateChecklistFreeTxtField1(request)%>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Nature of Business:  Provide web site (if available): ">
      <%=RequestUtils.generateChecklistFreeTxtField2(request)%>
   </cmr:chk-lbl-field>
   <cmr:chk-section name="Section A:  Denied Parties List Status" >
      <cmr:chk-entry number="1" section="A">
         Is the Customer on the DPL?
      </cmr:chk-entry>
   </cmr:chk-section>
   <cmr:chk-section name="Section B: Proliferation" >
      <cmr:chk-entry><span style="font-weight: bold; text-decoration: underline">Chemical or Biological Weapons:</span></cmr:chk-entry>
      <cmr:chk-entry number="2" section="B">
         To the best of your knowledge or belief is your customer involved in any activity related to or in support of Chemical or Biological Weapons? 
      </cmr:chk-entry>
      <cmr:chk-entry number="3" section="B">
         To the best of your knowledge or belief is your customer involved in any activity related to the design, development, repair, manufacture of chemical weapons precursors?
         <br><i>Note: This includes any activity related to or in support of rocket systems or unmanned aerial vehicles for the delivery of chemical, biological, or nuclear weapons</i> 
         <span id="checklist_txt_field_6" style="display:none"><label for="dijit_form_TextBox_6">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField6(request)%>
         </span>
      </cmr:chk-entry>
      <cmr:chk-entry><span style="font-weight: bold; text-decoration: underline">Nuclear:</span></cmr:chk-entry>
      <cmr:chk-entry number="4" section="B">
         To the best of your knowledge or belief, is this customer involved in any activity (design, development, manufacture etc.) of nuclear weapons or nuclear explosive devices?  
         <span id="checklist_txt_field_7" style="display:none"><label for="dijit_form_TextBox_7">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField7(request)%>
         </span>
      </cmr:chk-entry>
      <cmr:chk-entry><span style="font-weight: bold; text-decoration: underline">Missile:</span></cmr:chk-entry>
      <cmr:chk-entry number="5" section="B">
         To the best of your knowledge or belief is your customer involved in any activity related to or in support of rocket systems and/or unmanned air vehicles? 
         <ul>
            <li>"Rocket Systems" include, but are not limited to: ballistic missiles, space launch vehicles, and sounding rockets</li>
            <li>"Unmanned Air Vehicles" include, but are not limited to: cruise missiles, target drones and reconnaissance drones</li>
         </ul>
         <span id="checklist_txt_field_8" style="display:none"><label for="dijit_form_TextBox_8">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField8(request)%>
         </span>
      </cmr:chk-entry>
      <cmr:chk-entry><span style="font-weight: bold; text-decoration: underline">Military- Intelligence End user/Military-intelligence End Use:</span></cmr:chk-entry>
      <cmr:chk-entry number="6" section="B">
         To the best of your knowledge or belief is your customer involved business activities with a Intelligence and/or military-intelligence end user or items with military-intelligence end uses?
         <span id="checklist_txt_field_9" style="display:none"><label for="dijit_form_TextBox_9">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField9(request)%>
         </span>
         <br>
         <span style="font-weight: bold;">DEFINITIONS-</span>
         <ul>
            <li><span style="font-weight: bold;">Military-Intelligence End User:</span> means any intelligence or reconnaissance organization of the armed services (army, navy, marine, air force, or coast guard); or national guard. Examples include: GRU, IRGC. </li>
            <li><span style="font-weight: bold;">Intelligence End user:</span> means any government intelligence organization. Examples: CIA, FSB, Mossad.</li>
            <li><span style="font-weight: bold;">Military-Intelligence End Uses:</span> means the design, development, production, use, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, or refurbishing of, or incorporation into military items or items which are intended support the actions or functions of a 'military-intelligence end user,' as defined above.</li>
         </ul>
      </cmr:chk-entry>
      <cmr:chk-entry><span style="font-weight: bold; text-decoration: underline">Military End user/Military End Use:</span></cmr:chk-entry>
      <cmr:chk-entry number="7" section="B">
         To the best of your knowledge, is the customer a military end user or involved in business activities with a military end user or items with military end uses? 
         <span id="checklist_txt_field_10" style="display:none"><label for="dijit_form_TextBox_10">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField10(request)%>
         </span>
         <br>
         <span style="font-weight: bold;">DEFINITIONS-</span>
         <ul>
            <li><span style="font-weight: bold;">Military End Users:</span> Include army, navy, air force, marines, coast guard, national guard, security or police, government defense, and reconnaissance organizations, and their contractors or any person or entity whose actions or functions are intended to support military end uses. </li>
            <li><span style="font-weight: bold;">Military End Uses:</span> incorporation into a military item; or any item that supports or contributes to the operation, installation, maintenance, repair, overhaul, refurbishing, development, or production, of military items.  </li>
         </ul>
      </cmr:chk-entry>
      <cmr:chk-entry number="8" section="B">
         Does the customer's name contain any of the below proliferation keywords?
         <table border="0" cellpadding="1"  style="text-align:center">
            <tr>
               <th colspan="2" style="text-align:center">End User/End Use Keywords</th>
            </tr>
            <tr>
               <td style="width:150px">Aerospace</td>
               <td style="width:150px">Plutonium</td>
            </tr>
            <tr>
               <td>Artillery</td>
               <td>Propulsion</td>
            </tr>
            <tr>
               <td>Atom</td>
               <td>Radiological</td>
            </tr>
            <tr>
               <td>Atomic</td>
               <td>Reactor</td>
            </tr>
            <tr>
               <td>Biological</td>
               <td>Reconnaissance </td>
            </tr>
            <tr>
               <td>Bomb</td>
               <td>Rocket</td>
            </tr>
            <tr>
               <td>Chemical </td>
               <td>Ruggedized</td>
            </tr>
            <tr>
               <td>Defense </td>
               <td>Satellite </td>
            </tr>
            <tr>
               <td>Explosive </td>
               <td>Space</td>
            </tr>
            <tr>
               <td>Heavy Water </td>
               <td>Surveillance</td>
            </tr>
            <tr>
               <td>Military </td>
               <td>Uranium</td>
            </tr>
            <tr>
               <td>Missile </td>
               <td>Warfare </td>
            </tr>
            <tr>
               <td>Nuclear </td>
               <td>Weapon </td>
            </tr>
         </table>
         <span id="checklist_txt_field_11" style="display:none"><label for="dijit_form_TextBox_11">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField11(request)%>
         </span>
      </cmr:chk-entry>
   </cmr:chk-section>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Name, Title, and OU/BU  of Requester: ">
      <%=RequestUtils.generateChecklistFreeTxtField3(request)%>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Date completed: ">
      <%=RequestUtils.generateChecklistFreeTxtField4(request)%>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Pursuant to IBM's Business Conduct Guidelines (BCG), I certify that the information provided in this Proliferation Screening questionnaire, including all other support documentation related to this transaction, is true, accurate, and complete to the best of my knowledge and belief.
      Type name here to indicate acknowledgment of this certification: ">
      <%=RequestUtils.generateChecklistFreeTxtField5(request)%>
   </cmr:chk-lbl-field>
</cmr:checklist>
