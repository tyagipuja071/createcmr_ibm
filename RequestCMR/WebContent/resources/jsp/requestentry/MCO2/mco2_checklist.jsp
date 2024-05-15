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
<cmr:checklist title1="ADVANCED SUPERCOMPUTER and SEMICONDUCTOR MANUFACTURING CUSTOMER SCREENING CHECKLIST
(USERP Section 4 Part 2)">
  <cmr:chk-block>"This Questionnaire is designed for evaluation of all customers/BPs given a Customer Number located in Applicable D:5 Countries"</cmr:chk-block>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Customer Company Full Name: ">
      <%=RequestUtils.generateChecklistLocalAddress(request)%>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Customer Complete Address: ">
      <%=RequestUtils.generateChecklistFreeTxtField1(request)%>
   </cmr:chk-lbl-field>
   <cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Nature of Business:  Provide web site (if available): ">
      <%=RequestUtils.generateChecklistFreeTxtField2(request)%>
   </cmr:chk-lbl-field>
<cmr:chk-lbl-field addSpace="true" boldLabel="true" labelWidth="40%" label="Industry Segment: ">
      <%=RequestUtils.generateChecklistFreeTxtField2(request)%>
   </cmr:chk-lbl-field>
<cmr:chk-section name="Section A">
		<cmr:chk-entry number="1" displayNumber="1.1" dplField="" section="A">
			<span style="font-weight: bold; text-decoration: underline">Supercomputer ---</span>To the best of your knowledge or belief is your customer involved in the design, development, 
    manufacturing, testing, etc. of a “supercomputer”?
    <br>
			<br>
			<ul>
				<li> <span>a) Does your customer
						build supercomputers?</span>
				</li>
			</ul>
		</cmr:chk-entry>
		<cmr:chk-entry number="2" displayNumber="1.2" dplField="" section="A">
			<span style="font-weight: bold; text-decoration: underline">Supercomputer ---</span>To the best of your knowledge or belief is your customer involved in the design, development, 
    manufacturing, testing, etc. of a “supercomputer”?
    <br>
			<br>
			<ul>
				<li><span>b) Does your customer
						own supercomputers?</span>
				</li>
			</ul>
		</cmr:chk-entry>
		<cmr:chk-entry number="3" displayNumber="1.3" section="A">
To the best of your knowledge or belief, is your customer involved in any activity to incorporate items into a supercomputer, or the design, development, manufacturing, testing, etc. of any components/items that will be used in a ‘‘supercomputer’’?
<br>
			<strong>Note:</strong> this question is asking if the customer makes or supports tools/components that are used in a supercomputer or to build a supercomputer. 
<br>
			<br>
			<span style="font-weight: bold; text-decoration: underline">
			</span>
			<br>
			<span style="font-weight: bold; text-decoration: underline">DEFINITIONS
			</span>
			<br>
			<br>

			<span style="font-weight: bold; text-decoration: underline">：</span> 
<br>
			<strong>Supercomputer:</strong> a high-performance multi-rack system having thousands of closely coupled compute cores connected in parallel with networking technology and having a high peak power capacity requiring cooling elements. They are used for computationally intensive tasks including scientific and engineering work. Supercomputers may include shared memory, distributed memory, or a combination of both.
		
		</cmr:chk-entry>
		<cmr:chk-entry number="4" displayNumber="2.1" section="A">
			<span style="font-weight: bold; text-decoration: underline">Semiconductor Manufacturing ---</span>To the best of your knowledge or belief is your customer involved in the design, development, manufacturing, testing, etc. of integrated circuits? 
<br>
			<br>
			<strong>Note:</strong> This question is asking if the customer is involved in the semiconductor or integrated circuit manufacturing process.
		
		</cmr:chk-entry>
		<cmr:chk-entry number="5" displayNumber="2.2" section="A">
To the best of your knowledge or belief is your customer involved with the design, development, manufacturing, testing, etc. of any parts, components, or equipment of the tools that are used in the production, manufacturing, testing, etc. of semiconductors. 
<br>
			
			<strong>Note: </strong> This question is asking if the customer builds tools that are used in the semiconductor manufacturing process
		
		</cmr:chk-entry>
	</cmr:chk-section>
	
	<cmr:chk-block><strong>If any of the above questions have answered <span style="font-weight: bold"> YES </span>, please <span style="font-color: red">
	 STOP and CONTACT your ERC </span> </strong></cmr:chk-block>

	
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

