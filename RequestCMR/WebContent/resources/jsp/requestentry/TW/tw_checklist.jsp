<%@page import="com.ibm.cio.cmr.request.util.RequestUtils"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
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
<cmr:checklist title1="Taiwan End Use/End User Screening Checklist" title2="最終用途/最終使用者篩選清單">
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

     <cmr:chk-section name="Section A: Diversion Risk Profile (DRP) Screening Checklist" >
     <cmr:chk-entry>
     To prevent delivery to parties who may direct products to unauthorized destinations and/or end users/uses. Some indicators of red flags include, but are not limited to:
     </cmr:chk-entry>
    <cmr:chk-entry number="1" section="A">
      The order (hardware, software, services, etc.) does not match the customer's business requirements.   <br>
     	 訂單（硬體，軟體或服務）與客戶的業務要求不匹配。 
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="A">
      The customer is not using normal installation, training and maintenance services.   <br>
      	客戶沒有使用正常的安裝、培訓和維護服務。 
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="A">
      The customer's business needs and use of IBM's products is not well known and understood by IBM.  <br>
      IBM對客戶的業務需求和IBM產品的使用並不熟知和瞭解。  
    </cmr:chk-entry>
    <cmr:chk-entry number="4" section="A">
      The customer has requested unusual payment or delivery terms and conditions.<br>
          客戶要求特殊的付款或交貨條款和條件。
    </cmr:chk-entry>
    <cmr:chk-entry number="5" section="A">
    A Business Partner (BP) places a second order for the same or similar products for a new / different end user, soon after being informed that the order for the original end user was delayed or rejected due to US export control restrictions.<br>
IBM合作夥伴 （BP） 在被告知原本的客戶訂單因美國出口管制限制而被延遲或拒絕後不久，為不同的客戶下了第二個相同或類似產品的訂單。
        </cmr:chk-entry>
    <cmr:chk-entry number="6" section="A">
    There is an indication that the products are destined for a sanctioned country/region (The latest list can be found in  <a href="https://w3.ibm.com/w3publisher/ibm-export-regulation-office/us-embargo-and-sanctions-programs" target="_blank">https://w3.ibm.com/w3publisher/ibm-export-regulation-office/us-embargo-and-sanctions-programs</a>)<br>
有跡象顯明，產品/服務/技術將被運往受制裁的國家/地區（古巴、伊朗、朝鮮、敘利亞、烏克蘭的克里米亞、頓涅茨克和盧甘斯克地區、俄羅斯和白俄羅斯）。但名單會變動，請查看 <a href="https://w3.ibm.com/w3publisher/ibm-export-regulation-office/us-embargo-and-sanctions-programs" target="_blank">https://w3.ibm.com/w3publisher/ibm-export-regulation-office/us-embargo-and-sanctions-programs</a>
    
    </cmr:chk-entry>
    <cmr:chk-entry number="7" section="A">
    There is an indication that the customer or supplier is owned or controlled by the government of a sanctioned country/region.</br>
       有跡象顯明，客戶或供應商由受制裁國家/地區的政府擁有或控制？
    </cmr:chk-entry>
    <cmr:chk-entry number="8" section="A">
    There is an indication (anywhere in the world) that our products will be used in the design, development, production, stockpiling (only applies to actual chemical and biological weapons), operation, installation (including on-site installation), maintenance (checking), repair, overhaul, or refurbishing of nuclear weapons (including foreign maritime nuclear propulsion projects), chemical or biological weapons and/or their delivery vehicles, (including a whole plant of chemical weapons precursors), 
    and/or missiles (a.k.a. rocket systems and/or unmanned aerial vehicles).<br>
有跡象顯明（在世界任何地方）我們的產品將用於核武器（包括外國海上核推進專案）、化學或生物武器和/或其運載工具（包括一整套化學武器前體）和/或導彈（又名火箭系統和/或無人駕駛飛行器）的設計、開發、生產、儲存（僅適用於實際的化學和生物武器）、操作、安裝（包括現場安裝）、維護（檢查）、修理、大修或翻新。
    </cmr:chk-entry>
    <cmr:chk-entry number="9" section="A">
    There is an indication that the products are destined for a prohibited proliferation end use/user (missiles, chemical & biological weapons, nuclear) in any of the following countries: Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, Cambodia, People's Republic of China (includes Hong Kong), Cuba, Egypt, Georgia, Iran, Iraq, Israel, Jordan, Kazakhstan, Korea (North), Kuwait, Kyrgyzstan, Lebanon, Libya, Macau, Moldova, Mongolia, Myanmar (Burma), Nicaragua, Oman, Pakistan, Qatar, 
    Russia, Saudi Arabia, Syria, Taiwan, Tajikistan, Turkmenistan, United Arab Emirates, Uzbekistan, Venezuela, Vietnam, Yemen.<br>
有跡象顯明，這些產品將在以下任何國家/地區用於被禁止的擴散最終用途/使用者（導彈、化學和生物武器、核武器）：
阿富汗、亞美尼亞、亞塞拜然、巴林、白俄羅斯、柬埔寨、中華人民共和國（包括香港）、古巴、埃及、喬治亞、伊朗、伊拉克、以色列、約旦、哈薩克、朝鮮（北部）、科威特、吉爾吉斯斯坦、黎巴嫩、利比亞、澳門、摩爾多瓦、蒙古、緬甸、尼加拉瓜、阿曼、巴基斯坦、卡達、俄羅斯、沙烏地阿拉伯、敘利亞、臺灣、塔吉克斯坦、土庫曼斯坦、阿拉伯聯合大公國、烏茲別克、委內瑞拉、越南、葉門
    
      </cmr:chk-entry>
  </cmr:chk-section>
    <cmr:chk-section name="Section B" >
    <cmr:chk-entry>
      To prevent the delivery of hardware, software and/or technology to an activity of proliferation concern (e.g. Chemical or 
      Biological Weapons, Nuclear & Missile) and the unauthorized delivery to an military/intelligence end user, military/military-intelligence end user or for a military/military-intelligence end use.
    </cmr:chk-entry>
    <cmr:chk-entry number="1" section="B">
      <span style="font-weight:bold;text-decoration:underline">Chemical or Biological Weapons</span> <B>（化學武器或生物武器）</B>
     	<ol>
     	<li>To the best of your knowledge or belief is your customer involved in any activity related to or in support of Chemical or Biological Weapons?<br> 
			據您所知或所確信的，您的客戶是否參與或支援與生化武器相關的任何活動？<br>
     	</li>
     	<li>
     	To the best of your knowledge or belief is your customer involved in any activity related to the design, development, repair, manufacture of chemical weapons precursors?</br>
			據您所知或所確信的，您的客戶是否參與化學武器前驅物相關的設計、開發、維修和製造的活動？
     	  	</li>
     	 </ol>
          <B>Note:</B> This includes any activity related to or in support of <B>rocket systems</B> or <B>unmanned aerial vehicles</B> for the delivery of chemical, biological, or nuclear weapons</br>
                           註：這包括與生化武器或者核武運載相關的火箭系統或無人機，或者為其提供的支援活動。          
      
           
    </cmr:chk-entry>
    <cmr:chk-entry number="2" section="B">
      <span style="font-weight:bold;text-decoration:underline">Nuclear</span><b> （核能）</b><br>
     <br>
     
	 To the best of your knowledge or belief, is this customer involved in any activity (design, development, manufacture etc.) of nuclear weapons or nuclear explosive devices?</br> 
	據您所知或所確信的，該客戶是否參與核武或核爆裝置的任何活動（設計、開發、製造等）？<br>
    </cmr:chk-entry>
    <cmr:chk-entry number="3" section="B">
      <span style="font-weight:bold;text-decoration:underline">Missile</span> <b> （導彈）</b><br>
      <br>
      
      To the best of your knowledge or belief is your customer involved in any activity related to or in support of <b>rocket systems</b> and/or unmanned air vehicles?<br>
據您所知或所確信的，您的客戶是否參與或支援與<b>火箭系統或無人機</b>相關的任何活動？<br>

     <br> 
     <ul>   
     <li>
    “Rocket Systems” include, but are not limited to: ballistic missiles, space launch vehicles, and sounding rockets<br>
「火箭系統」包括但不限於：彈道飛彈、太空運載火箭和探空火箭
	</li>
	<li>
	 “Unmanned Air Vehicles” include, but are not limited to: cruise missiles, target drones and reconnaissance drones<br>
    「無人機」包括但不限於：巡航飛彈、靶機和偵察機
    </li>
    </ul>
    

    </cmr:chk-entry>
	<cmr:chk-entry number="4" section="B">
      <span style="font-weight:bold;text-decoration:underline">Military- Intelligence End user/Military-intelligence End Use</span><B> （軍事情報最終用戶/軍事情報最終用途）</B><br>
      <br>
       To the best of your knowledge or belief is your customer involved business activities with a <b>Intelligence</b> and/or <b>military-intelligence end user</b> or items with <b>military-intelligence end uses</b>?<br>
據您所知或所確信的，您的客戶是否參與「情報/軍事情報最終用戶」的業務活動或者涉及「軍事情報最終用途」的項目？<br>
<BR>
  <span style="font-weight: bold; color:#c00000">DEFINITIONS-<br></span>

		<strong>Military-Intelligence End User:</strong> means any intelligence or reconnaissance organization of the armed services (army, navy, marine, air force, or coast guard); or national guard. Examples include: GRU, IRGC. <br>
         軍事情報最終用戶：指武裝部隊（陸軍、海軍、海軍陸戰隊、空軍或海岸警衛隊）的任何情報或偵察組織;或國民警衛隊。例子包括：格魯烏、伊斯蘭革命衛隊。
		<br>
		<br>
		<strong>Intelligence End user:</strong> means any government intelligence organization. Examples: CIA, FSB, Mossad.<br>
情報最終用戶：指任何政府情報組織。例如：中央情報局、俄羅斯聯邦安全域、摩薩德。
		<br>
		<br>
		<strong>Military-Intelligence End Uses:</strong> means the design, development, production, use, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, or refurbishing of, or incorporation into military items or items which are intended support the actions or functions of a ‘military-intelligence end user,’ as defined above.<br>
軍事情報最終用途：指對軍事物品或者用來支援上述定義的“軍事情報最終用戶”的行動或功用的物品所做的設計、開發、生產、使用、操作、安裝（包括現場安裝）、維護（檢查）、修理、大修或翻新，或者將物件併入軍事/情報用途的物品。
		<br>		
		
     </cmr:chk-entry>
	 <cmr:chk-entry number="5" section="B">
      <span style="font-weight:bold;text-decoration:underline">Military End user/Military End Use</span> <B>（軍事最終用戶/軍事最終用途）</B> <br>
      <br>
      To the best of your knowledge, is the customer a <b>military end user</b> or involved in business activities with a <b>military end user</b> or items with <b>military end uses</b>?<br>
據您所知，此客戶是「軍事最終用戶」，或者參與「軍事最終用戶」的業務活動或涉及「軍事最終用途」的項目？
     <br>
	 <br>
	 <span style="font-weight: bold; color:#c00000" >DEFINITIONS-<br></span>

		<strong>Military End Users: </strong> Include army, navy, air force, marines, coast guard, national guard, security or police, government defense, and reconnaissance organizations, and their contractors or any person or entity whose actions or functions are intended to support military end uses.<br>
軍事最終用戶：包括陸軍、海軍、空軍、海軍陸戰隊、海岸防衛隊、國民警衛隊、安全或警察、政府國防和偵察組織及其承包商或其行為或職能旨在支援軍事最終用途的任何個人或實體。
		<br>
		<br>
		<strong>Military End Uses: </strong> incorporation into a military item; or any item that supports or contributes to the operation, installation, maintenance, repair, overhaul, refurbishing, development, or production, of military items.  <br>
軍事最終用途：併入軍事項目的物品；或者任何支援或有助於軍事物品的操作、安裝、維護、修理、大修、翻新、開發或生產的物品。

		<br>
	 
     </cmr:chk-entry>
	 <cmr:chk-entry number="6" section="B">
	  Does the customer’s name contain any of the below proliferation keywords?<br>
         客戶的公司名稱是否包含以下風險擴散關鍵字？
         <br>
			<div class="table-container">
				<table id="table1">
					<tr>
						<th colspan="2">End User/End Use
							Keywords<br>最終用戶/最終用途關鍵字
						</th>
					</tr>
					<tr>
						<td>航空航太 Aerospace</td>
						<td>鈈 Plutonium</td>
					</tr>
					<tr>
						<td>火炮 Artillery</td>
						<td>推動力  Propulsion</td>
					</tr>
					<tr>
						<td>原子 Atom</td>
						<td>放射學 Radiological</td>
					</tr>
					<tr>
						<td>原子的  Atomic</td>
						<td>核反應爐 Reactor</td>
					</tr>
					<tr>
						<td>生物學的 Biological</td>
						<td>偵察 Reconnaissance</td>
					</tr>
					<tr>
						<td>炸彈 Bomb</td>
						<td>火箭 Rocket</td>
					</tr>
					<tr>
						<td>化學的 Chemical</td>
						<td>加固 Ruggedized</td>
					</tr>
					<tr>
						<td>防禦 Defense</td>
						<td>人造衛星 Satellite</td>
					</tr>
					<tr>
						<td>爆炸 Explosive</td>
						<td>太空 Space</td>
					</tr>
					<tr>
						<td>重水 Heavy Water</td>
						<td>監控 Surveillance</td>
					</tr>
					<tr>
						<td>軍事 Military</td>
						<td>鈾 Uranium</td>
					</tr>
					<tr>
						<td>導彈/飛彈  Missile</td>
						<td>戰爭 Warfare</td>
					</tr>
					<tr>
						<td>核/原子能  Nuclear</td>
						<td>武器 Weapon</td>
					</tr>
				</table>
			</div>
	 
     </cmr:chk-entry>
  </cmr:chk-section>
  <cmr:chk-block boldText="false">
    This checklist has been translated into Chinese but in the event of any conflict between the English and Chinese versions, the English version shall prevail.<br>
此清單已翻譯成中文，但如果兩者之間有任何衝突，以英文版為準。 
</cmr:chk-block>
        
</cmr:checklist>

