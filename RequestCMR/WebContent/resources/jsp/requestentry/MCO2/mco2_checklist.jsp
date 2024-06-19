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
    
 <cmr:checklist title1="ADVANCED SUPERCOMPUTER and SEMICONDUCTOR MANUFACTURING CUSTOMER SCREENING CHECKLIST (USERP Section 4 Part 2)"
   title2="This Questionnaire is designed for evaluation of all customers/BPs given a Customer Number located in Applicable D:5 Countries"> 
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Customer Company Full Name: ">
     <span id="checklist_txt_field_0" style="display:block"> <%=RequestUtils.generateChecklistLocalAddress(request)%> </span>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Customer Complete Address: ">
   <span id="checklist_txt_field_1" style="display:block">   <%=RequestUtils.generateChecklistFreeTxtField1(request)%> </span>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Nature of Business:  Provide web site (if available): ">
   <span id="checklist_txt_field_2" style="display:block">   <%=RequestUtils.generateChecklistFreeTxtField2(request)%> </span>
   </cmr:chk-lbl-field>
    <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Industry Segment: ">
  <span id="checklist_txt_field_3" style="display:block">    <%=RequestUtils.generateChecklistFreeTxtField3(request)%> </span>
   </cmr:chk-lbl-field>
   <br>
   <cmr:chk-block>
   <span style="font-weight: bold;">To be completed by Marketing / Client Representative for new customers.</span>
   <br>
  <span style="text-decoration: underline">Please circle YES or NO to answer all questions. </span>
   </cmr:chk-block>
   <cmr:chk-section name="Section A:  Denied Parties List Status" >
      <cmr:chk-entry number="1" section="A">
             Is the Customer on the DPL?        
            <span id="checklist_txt_field_4" style="display:none"><label for="dijit_form_TextBox_4">If yes, provide details of DPL denial code and DPL entry information. </label>
         <%=RequestUtils.generateChecklistFreeTxtField4(request)%>
         </span> 
      </cmr:chk-entry>
   </cmr:chk-section> 
   <cmr:chk-section name="Section B:">
    <cmr:chk-entry><span style="font-weight: bold; text-decoration: underline">Supercomputer</span> ---- To the best of your knowledge or belief is your customer involved in the design, development, manufacturing, testing, etc. of a 'supercomputer'?</cmr:chk-entry>
      <cmr:chk-entry number="1" section="B">
         Does your customer build supercomputers?
          <span id="checklist_txt_field_5" style="display:none"><label for="dijit_form_TextBox_5">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField5(request)%>
         </span>
      </cmr:chk-entry>
      <cmr:chk-entry number="2" section="B">
         Does your customer own supercomputers?
          <span id="checklist_txt_field_6" style="display:none"><label for="dijit_form_TextBox_6">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField6(request)%>
         </span>
      </cmr:chk-entry>
      <cmr:chk-entry number="3" section="B">
         To the best of your knowledge or belief, is your customer involved in any activity to incorporate items into a supercomputer, or the design, development, manufacturing, testing, etc. of any components/items that will be used in a 'supercomputer'?
         <br><i> <b>Note:</b> this question is asking if the customer makes or supports tools/components that are used in a supercomputer or to build a supercomputer.</i>
          <br>
         <span style="font-weight: bold;">DEFINITIONS-</span>
         <ul>
            <li><span style="font-weight: bold;">Supercomputer: </span> A high-performance multi-rack system having thousands of closely coupled compute cores connected in parallel with networking technology and having a high peak power capacity requiring cooling elements. They are used for computationally intensive tasks including scientific and engineering work. Supercomputers may include shared memory, distributed memory, or a combination of both. </li>            
         </ul>
         <span id="checklist_txt_field_7" style="display:none"><label for="dijit_form_TextBox_7">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField7(request)%>
         </span>
      </cmr:chk-entry>
   </cmr:chk-section>
   <cmr:chk-section name="Section C:">
   <cmr:chk-entry number="1" section="C">
         <span style="font-weight: bold; text-decoration: underline">Semiconductor Manufacturing</span> ---- To the best of your knowledge or belief is your customer involved in the design, development, manufacturing, testing, etc. of integrated circuits?
         <br><i> <b>Note:</b> This question is asking if the customer is involved in the semiconductor or integrated circuit manufacturing process.</i>
      <span id="checklist_txt_field_8" style="display:none"><label for="dijit_form_TextBox_8">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField8(request)%>
         </span>
      </cmr:chk-entry>
       <cmr:chk-entry number="2" section="C">
         To the best of your knowledge or belief is your customer involved with the design, development, manufacturing, testing, etc. of any parts, components, or equipment <span style="font-weight: bold; text-decoration: underline">of the tools </span>that are used in the production, manufacturing, testing, etc. of semiconductors.
         <br><i> <b>Note:</b> This question is asking if the customer builds tools that are used in the semiconductor manufacturing process.</i>
       <span id="checklist_txt_field_9" style="display:none"><label for="dijit_form_TextBox_9">If yes, please provide an explanation below detailing this customer's business:</label>
         <%=RequestUtils.generateChecklistFreeTxtField9(request)%>
         </span>
      </cmr:chk-entry>
   </cmr:chk-section>
   <cmr:chk-block>
  If any of the above questions have answered  <span style="font-weight: bold; color:red"> YES </span>, please  <span style="font-weight: bold; color: red">STOP </span> and <span style="font-weight: bold; color: red">CONTACT your ERC</span>
 </span>
</cmr:chk-block>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Name, Title, and OU/BU  of Requester: ">
  <span id="checklist_txt_field_10" style="display:block">    <%=RequestUtils.generateChecklistFreeTxtField10(request)%> </span>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Date completed: ">
   <span id="checklist_txt_field_11" style="display:block">   <%=RequestUtils.generateChecklistFreeTxtField11(request)%> </span>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="">
    <cmr:chk-block>
      <div><i>Pursuant to <a href="https://w3.ibm.com/w3publisher/trust-compliance/bcgs">IBM's Business Conduct Guidelines (BCG)</a>, 
      <br>
      I certify that the information provided in this 
      <br>
     questionnaire is true, accurate, and complete to the best 
     <br>
     of my knowledge and belief.
   <br>
   </i>
  <span style="text-decoration: underline"> Type name here to indicate acknowledgment of this certification: </span>
   </div>
    <span id="checklist_txt_field_12" style="display:block">    <%=RequestUtils.generateChecklistFreeTxtField12(request)%> </span>
   
   </cmr:chk-block>
    </cmr:chk-lbl-field>
</cmr:checklist>