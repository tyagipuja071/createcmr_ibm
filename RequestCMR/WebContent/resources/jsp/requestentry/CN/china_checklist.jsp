<%@page import="com.ibm.cio.cmr.request.util.RequestUtils"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
<style>
/* Style for the table container */
 .table-container {
     margin-left: 20px;
    /* Adjust the margin as needed */
}
/* Style for the table */
 #table1 {
     border-collapse: collapse;
     border: 1px solid black;
     margin-bottom: 20px;
     background-color: transparent;
}
 #table1 th, #table1 td {
     text-align: left;
}
 #table1 th {
     text-align: center;
     border-bottom: 1px solid black;
     font-weight: normal;
    /* Remove bold font-weight from table heading */
}
/* Add column separator lines */
 #table1 td :not ( :last-child ) {
     border-right : 1 px soliblack;
}

</style>
<cmr:checklist title1="中国最终用户/最终用途检查清单"
	title2="China End User/End Use Screening Checklist">
	<!--   <cmr:chk-block>
      Customer Company Full Name
      </cmr:chk-block>
      <cmr:chk-lbl-field addSpace="false" labelWidth="12%" boldLabel="false" label="(in English):">
      ${reqentry.mainCustNm1} ${reqentry.mainCustNm2}
      </cmr:chk-lbl-field>
      <cmr:chk-lbl-field addSpace="false" labelWidth="12%" boldLabel="false"  label= "" >
      <%=RequestUtils.generateChecklistLocalName(request)%>
      </cmr:chk-lbl-field>
      
      <cmr:chk-section name="DPL Check" >
      <cmr:chk-entry dplField="true">
         <span style="font-weight:bold">US DPL and UN Sanction List Check:</span>
      </cmr:chk-entry>
      <cmr:chk-entry matchField="true">
         <span style="font-weight:bold">Match / Potential Match Found:</span>
      </cmr:chk-entry>
      </cmr:chk-section>
      
      <cmr:chk-block boldText="false">
        Please check the Customer Full Name against DPL database. 
        (Notes workspace <a href="<%=SystemConfiguration.getValue("DPL_CHECK_DB")%>">DPL database</a>, 
        DPL on IBM ERO Web Page and Monthly DPL file distributed to the functions.)      
        <br>
        ***If there is any match found, please STOP and REPORT to your manager, providing 
        following information to China ERC or your manager for further guidance. 
      </cmr:chk-block>
      
      <cmr:chk-section name="Below are required to be filled in details to support customer eligibility determination." >
      <cmr:chk-lbl-field addSpace="true" boldLabel="false" labelWidth="40%" label="Customer Detail Address">
      <%=RequestUtils.generateChecklistLocalAddress(request)%>
      </cmr:chk-lbl-field>
      
      <cmr:chk-lbl-field addSpace="true" boldLabel="false" labelWidth="40%" label="Nature of Business: (Please provide web site if available)">
      <%=RequestUtils.generateChecklistFreeTxtField1(request)%>
      </cmr:chk-lbl-field>
      
      <cmr:chk-lbl-field addSpace="true" boldLabel="false" labelWidth="40%" label="What products/service etc. they want to buy?">
      <%=RequestUtils.generateChecklistFreeTxtField2(request)%>
      </cmr:chk-lbl-field>
      
      <cmr:chk-lbl-field addSpace="false" boldLabel="false" labelWidth="12%" label="Purpose of End Use">
      <%=RequestUtils.generateChecklistFreeTxtField3(request)%>
      </cmr:chk-lbl-field>
      </cmr:chk-section>
      
      <cmr:chk-block>Transactions (with customers found DPL matched) may only proceed as reviewed and authorized by IBM ERO (Export Regulation Office) and/or AP STC (Sensitive Transaction Council).
      To be completed by Marketing / Client Representative for new customers.
      <br>
      -->
	</cmr:chk-block>
	<cmr:chk-section name="Section A">
		<cmr:chk-entry number="1" section="A">
         订单（硬件，软件或服务）与客户的业务要求不匹配 <br>
         The order (hardware，software and service) does not match the customer’s business requirements
      </cmr:chk-entry>
		<cmr:chk-entry number="2" section="A">
         客户没有使用正常的安装、培训和维护服务。 <br>
         The customer is not using normal installation, training and maintenance services.
      </cmr:chk-entry>
		<cmr:chk-entry number="3" section="A">
         客户的业务需求和IBM产品的使用并没有为IBM所熟知和了解。<br>
         The customer business needs and use of IBM products is not well known and understood by IBM.
      </cmr:chk-entry>
		<cmr:chk-entry number="4" section="A">
         客户要求特殊的付款或交货条款和条件。<br>
         The customer has requested unusual payment or delivery terms and conditions.
      </cmr:chk-entry>
		<cmr:chk-entry number="5" section="A">
         有迹象表明，产品/服务/技术将被运往受制裁的国家/地区：
         <span style="font-weight: bold">古巴、伊朗、朝鲜、叙利亚、乌克兰的克里米亚、顿涅茨克和卢甘斯克地区、俄罗斯和白俄罗斯。</span>
			<br>    
         There is an indication that the products/services/technologies are destined for a sanctioned country/region:
         <span style="font-weight: bold">Cuba, Iran, North Korea,
				Syria, the Crimea, Donetsk and Luhansk regions of Ukraine, Russia
				and Belarus.</span>
		</cmr:chk-entry>
		<cmr:chk-entry number="6" section="A">
         有迹象表明客户或供应商由受制裁国家或地区的政府拥有或控制？<br>
         There is an indication that the customer or supplier is owned or controlled by the government of any of the sanctioned countries or region?
      </cmr:chk-entry>
		<cmr:chk-entry number="7" section="A">
         客户的公司名称中是否包含以下任何风险扩散关键字？<br>
         Does the customer’s name contain any of the below proliferation keywords?
         <br>
			<div class="table-container">
				<table id="table1">
					<tr>
						<th colspan="2">最终用户/最终用途关键字<br>End User/End Use
							Keywords
						</th>
					</tr>
					<tr>
						<td>航空航天 Aerospace</td>
						<td>钚 Plutonium</td>
					</tr>
					<tr>
						<td>火炮 Artillery</td>
						<td>推动力 Propulsion</td>
					</tr>
					<tr>
						<td>原子 Atom</td>
						<td>放射学 Radiological</td>
					</tr>
					<tr>
						<td>原子的 Atomic</td>
						<td>核反应堆 Reactor</td>
					</tr>
					<tr>
						<td>生物学的 Biological</td>
						<td>侦察 Reconnaissance</td>
					</tr>
					<tr>
						<td>炸弹 Bomb</td>
						<td>火箭 Rocket</td>
					</tr>
					<tr>
						<td>化学的 Chemical</td>
						<td>加固 Ruggedized</td>
					</tr>
					<tr>
						<td>防御 Defense</td>
						<td>人造卫星 Satellite</td>
					</tr>
					<tr>
						<td>爆炸 Explosive</td>
						<td>空间 Space</td>
					</tr>
					<tr>
						<td>重水 Heavy Water</td>
						<td>监控 Surveillance</td>
					</tr>
					<tr>
						<td>军事 Military</td>
						<td>铀 Uranium</td>
					</tr>
					<tr>
						<td>导弹 Missile</td>
						<td>战争 Warfare</td>
					</tr>
					<tr>
						<td>核/原子能 Nuclear</td>
						<td>武器 Weapon</td>
					</tr>
				</table>
			</div>
		</cmr:chk-entry>
	</cmr:chk-section>
	<cmr:chk-section name="Section B">
		<cmr:chk-entry>
         有迹象表明，客户在以下任何一个国家未经授权与目的地为禁止扩散的最终用户/最终用途（导弹、化学和生物武器、核武器，军事）的各方和/或产品进行交易：
         <br>
         There is an indication that the customer has unauthorized dealings with parties and/or products that are destined for a prohibited proliferation end user/end use (missiles, chemical & biological weapons, nuclear, military) in any of the following countries shown in each category:
         <br>
			<br>
         阿富汗、亚美尼亚、阿塞拜疆、巴林、白俄罗斯、柬埔寨、中华人民共和国（包括香港）、埃及、格鲁吉亚、伊拉克、以色列、约旦、哈萨克斯坦、科威特、吉尔吉斯斯坦、老挝、黎巴嫩、利比亚、澳门、摩尔多瓦、蒙古、缅甸、阿曼、巴基斯坦、卡塔尔、俄罗斯、沙特阿拉伯、台湾、塔吉克斯坦、土库曼斯坦、乌克兰、阿拉伯联合酋长国、乌兹别克斯坦、委内瑞拉、越南、也门
         <br>
         Afghanistan, Armenia, Azerbaijan, Bahrain, Belarus, Cambodia, People’s Republic of China (including Hong Kong), Egypt, Georgia, Iraq, Israel, Jordan, Kazakhstan, Kuwait, Kyrgyzstan, Laos, Lebanon, Libya, Macau, Moldova, Mongolia, Myanmar (Burma), Oman, Pakistan, Qatar, Russia, Saudi Arabia, Taiwan, Tajikistan, Turkmenistan, Ukraine, United Arab Emirates, Uzbekistan, Venezuela, Vietnam, Yemen	
      </cmr:chk-entry>
		<cmr:chk-entry number="1" section="B">
			<span style="font-weight: bold; text-decoration: underline">导弹
			</span>
         --- 据您所知或所信，您的客户是否参与了以下国家的任何与支持火箭系统和/或无人飞行器有关的活动？
         <br>
			<span style="font-weight: bold; text-decoration: underline">Missile</span>
         --- To the best of your knowledge or belief is your customer involved in any activity related to or in 
         support of rocket systems and/or unmanned air vehicles in the following countries?
         <ul>
				<li><em>“火箭系统”包括但不限于：弹道导弹、太空运载火箭和探空火箭 
				<br>“Rocket Systems” include, but are not limited to: ballistic missiles, space launch
					vehicles, and sounding rockets</em>
				</li>
				<li><em>“无人飞行器”包括但不限于：巡航导弹、目标无人机和侦察无人机 <br> “Unmanned Air
					Vehicles” include, but are not limited to: cruise missiles, target
					drones and reconnaissance drones </em>
				</li>
			</ul>
		</cmr:chk-entry>
		<cmr:chk-entry number="2" section="B">
			<span style="font-weight: bold; text-decoration: underline">生化武器</span>
			<br>
			<span style="font-weight: bold; text-decoration: underline">Chemical
				or Biological Weapons</span>
			<br>
			<ol>
				<li>据您所知或所信，您的客户是否参与了以下国家与化学武器或生物武器有关或支持化学武器或生物学武器的任何活动？<br>
					To the best of your knowledge or belief is your customer involved
					in any activity related to or in support of Chemical or Biological
					Weapons in following countries?
				</li>
				<li>据您所知或所信，您的客户是否参与了以下国家与化学武器前体的设计、开发、维修和制造有关的任何活动？<br>
					To the best of your knowledge or belief is your customer involved
					in any activity related to the design, development, repair,
					manufacture of chemical weapons precursors in following countries?
				</li>
			</ol>
			<br>
			<em> <strong><em>注：</em>这包括与运载化学、生物或核武器的火箭系统或无人机有关或为其提供支持的任何活动</strong><br>
				<span style="font-weight: bold">NOTE: </span> This includes any
				activity related to or in support of <strong>rocket systems
			</strong> or <strong>unmanned aerial vehicles </strong>for the delivery of
				chemical, biological, or nuclear weapons
			</em>
		</cmr:chk-entry>
		<cmr:chk-entry number="3" section="B">
			<span style="font-weight: bold; text-decoration: underline">核</span>
         --- 据您所知或所信，该客户是否参与了以下国家的核武器或核爆炸装置的任何活动（设计、开发、制造等）？
         <br>
			<span style="font-weight: bold; text-decoration: underline">Nuclear</span>
         --- To the best of your knowledge or belief, is this customer involved in any activity (design, development, manufacture etc.) 
         of nuclear weapons or nuclear explosive devices in the following counties? 
      </cmr:chk-entry>
		<cmr:chk-entry number="4" section="B">
			<span style="font-weight: bold; text-decoration: underline">军事-情报最终用户/军事情报最终用途
				---</span>
         据您所知或所信，您的客户是否参与了与以下国家的情报和/或军事情报最终用户或具有军事情报最终目的的物品的商业活动？
         <br>
			<span style="font-weight: bold; text-decoration: underline">Military-
				Intelligence End user/Military-intelligence End Use --- </span>
         To the best of your knowledge or belief is your customer involved business activities with a <strong>Intelligence</strong> and/or <strong>military-intelligence
				end user</strong> or items with <strong>military-intelligence end
				uses</strong> in the following counties? 
         <br>
			<br>
			<span style="font-weight: bold; text-decoration: underline">定义
			</span>
			<br>
			<span style="font-weight: bold; text-decoration: underline">DEFINITIONS
			</span>
			<br>
			<br>
			<strong>军事情报最终用户：</strong>指武装部队（陆军、海军、海军陆战队、空军或海岸警卫队）的任何情报或侦察组织；或国民警卫队。
         <br>
			<strong>Military-Intelligence End User:</strong> means any intelligence or reconnaissance organization of the armed services (army, navy, marine, air force, or coast guard); or national guard. 
         <br>
			<br>
			<strong>情报最终用户：</strong>指任何政府情报机构。例如：中央情报局、金融稳定局、摩萨德。
         <br>
			<strong>Intelligence End user:</strong> means any government intelligence organization. Examples: CIA, FSB, Mossad.
         <br>
			<br>
			<strong>军事情报最终用途：</strong>指设计、开发、生产、使用、操作、安装（包括现场安装）、维护（检查）、修理、大修或翻新，或并入军事物品或旨在支持上述“军事情报最终用户”行动或功能的物品。
         <br>
			<strong>Military-Intelligence End Uses:</strong> means the design, development, production, use, operation, installation (including on-site installation), maintenance (checking), repair, overhaul, or refurbishing of, or incorporation into military items or items which are intended support the actions or functions of a ‘military-intelligence end user,’ as defined above.
      </cmr:chk-entry>
		<cmr:chk-entry number="5" section="B">
			<span style="font-weight: bold; text-decoration: underline">
				军事最终用户/军事最终用途 ---</span>
			 据您所知，客户是否军事最终用户或与以下国家的军事最终用户或具有军事最终用途的物品一起参与商业活动？
       <br>
			<span style="font-weight: bold; text-decoration: underline">Military
				End user/Military End Use ---</span>
			To the best of your knowledge, is the customer a <strong>military end user</strong> or involved in business activities with a <strong>military end user</strong> or items with <strong>military end uses</strong> in the following countries?  
           <br>
			<br>
			<span style="font-weight: bold; text-decoration: underline">定义
			</span>
			<br>
			<span style="font-weight: bold; text-decoration: underline">DEFINITIONS
			</span>
			<br>
			<br>

			<span style="font-weight: bold; text-decoration: underline">军事最终用户：</span>
包括陆军、海军、空军、海军陆战队、海岸警卫队、国民警卫队、安全或警察、政府国防和侦察组织及其承包商，或任何行动或职能旨在支持军事最终用途的个人或实体。
<br>
			<span style="font-weight: bold; text-decoration: underline">Military
				End Users:</span>
 Include army, navy, air force, marines, coast guard, national guard, security or police, government defense, and reconnaissance organizations, and their contractors or any person or entity whose actions or functions are intended to support military end uses.
 <br>
			<span style="font-weight: bold; text-decoration: underline">军事最终用途：</span>纳入军事物品；或支持或有助于军事物品的操作、安装、维护、修理、大修、翻新、开发或生产的任何物品。
<br>
			<span style="font-weight: bold; text-decoration: underline">Military
				End Uses:</span>
incorporation into a military item; or any item that supports or contributes to the operation, installation, maintenance, repair, overhaul, refurbishing, development, or production, of military items.  
      
      </cmr:chk-entry>
	</cmr:chk-section>
	<cmr:chk-section name="Section C">
		<cmr:chk-entry number="1" displayNumber="1.1" dplField="" section="C">
			<span style="font-weight: bold; text-decoration: underline">超级计算机 ---</span> 据你所知或所信，你的客户是否参与了“超级计算机”的设计、开发、制造、测试等？<br />
			<span style="font-weight: bold; text-decoration: underline">Supercomputer ---</span>To the best of your knowledge or belief is your customer involved in the design, development, 
    manufacturing, testing, etc. of a “supercomputer”?
    <br>
			<br>
			<ul>
				<li>你的客户制造超级计算机吗？<br> <span>a) Does your customer
						build supercomputers?</span>
				</li>
				<li>您的客户拥有超级计算机吗？<br> <span>b) Does your customer
						own supercomputers?</span>
				</li>
			</ul>
		</cmr:chk-entry>
		<cmr:chk-entry number="2" displayNumber="1.2" section="C">
		据您所知或所信，您的客户是否参与了将物品纳入超级计算机的任何活动，或将用于“超级计算机”的任何组件/物品的设计、开发、制造、测试等？ 
<br>
To the best of your knowledge or belief, is your customer involved in any activity to incorporate items into a supercomputer, or the design, development, manufacturing, testing, etc. of any components/items that will be used in a ‘‘supercomputer’’?
<br>
			<br>
			<strong>注意：</strong>这个问题是问客户是否制造或支持用于超级计算机或构建超级计算机的工具/组件。
<br>
			<strong>Note:</strong> this question is asking if the customer makes or supports tools/components that are used in a supercomputer or to build a supercomputer. 
<br>
			<br>
			<span style="font-weight: bold; text-decoration: underline">定义
			</span>
			<br>
			<span style="font-weight: bold; text-decoration: underline">DEFINITIONS
			</span>
			<br>
			<br>

			<span style="font-weight: bold; text-decoration: underline">超级计算机：</span>一种高性能的多机架系统，具有数千个紧密耦合的计算核心，通过网络技术并联连接，并具有需要冷却元件的高峰值功率容量。它们用于计算密集型任务，包括科学和工程工作。超级计算机可能包括共享内存、分布式内存或两者的组合。 
<br>
			<strong>Supercomputer:</strong> a high-performance multi-rack system having thousands of closely coupled compute cores connected in parallel with networking technology and having a high peak power capacity requiring cooling elements. They are used for computationally intensive tasks including scientific and engineering work. Supercomputers may include shared memory, distributed memory, or a combination of both.
		
		</cmr:chk-entry>
		<cmr:chk-entry number="3" displayNumber="2.1" section="C">
			<span style="font-weight: bold; text-decoration: underline">半导体制造业 --- </span>，您的客户是否参与了集成电路的设计、开发、制造、测试等？<br>
			<span style="font-weight: bold; text-decoration: underline">Semiconductor Manufacturing ---</span>To the best of your knowledge or belief is your customer involved in the design, development, manufacturing, testing, etc. of integrated circuits? 
<br>
			<br>
			<strong>注意： </strong>这个问题是问客户是否参与了半导体或集成电路的制造过程。<br>
			<strong>Note:</strong> This question is asking if the customer is involved in the semiconductor or integrated circuit manufacturing process.
		
		</cmr:chk-entry>
		<cmr:chk-entry number="4" displayNumber="2.2" section="C">
		据您所知或所信，您的客户是否参与了半导体生产、制造、测试等所用工具的任何零件、组件或设备的设计、开发、制造和测试等。<br>
To the best of your knowledge or belief is your customer involved with the design, development, manufacturing, testing, etc. of any parts, components, or equipment of the tools that are used in the production, manufacturing, testing, etc. of semiconductors. 
<br>
			<br>
			<strong>注意：</strong>这个问题是问客户是否制造了半导体制造过程中使用的工具
<br>
			<strong>Note: </strong> This question is asking if the customer builds tools that are used in the semiconductor manufacturing process
		
		</cmr:chk-entry>
	</cmr:chk-section>
	<cmr:chk-section name="Section D">
		<cmr:chk-entry number="1" displayNumber=" " section="D">
			<span style="font-weight: bold; text-decoration: underline">转移到俄罗斯的风险 ---</span>据您所知或所信，您的客户直接或间接参与俄罗斯业务是否属于以下任何标准<br>
			<span style="font-weight: bold; text-decoration: underline">Diversion Risk to Russia --- </span>To the best of your knowledge or belief is your customer involved directly or indirectly in Russia fallen into any of the circumstances as
			<br>
			<ul>
				<li>a) 实体法人、股东或管理层是否为俄罗斯国籍？<br> If the entity legal
					persons or shareholders or management are Russian nationalities?
				</li>
				<li>b) 该实体的投资是否全部/部分来自俄罗斯或由任何俄罗斯企业所有？<br> If the entity's
					investment is fully/partially from Russia or owned by any of the
					Russia enterprises?
				</li>
				<li>c) 交易的最终用户/最终用途是否将为俄罗斯拥有或位于俄罗斯的任何企业/国籍的人员服务？<br> If
					the deal end user/end use will serve any enterprise/nationalities
					owned by Russia or located in Russia?
				</li>
				<li>d) IBM的订单（包括技术）是否会转移到俄罗斯？<br> If there will have any
					transfers of IBM orders (including technologies) destined to
					Russia?
				</li>
				<li>e) 所提供的IBM设备配置是否来源于任何俄罗斯企业或国籍之前购买的清单？<br> If the
					provided IBM equipment configuration originated from the list
					previously purchased by any Russia-owned enterprises or
					nationalities?
				</li>
			</ul>
		</cmr:chk-entry>
	</cmr:chk-section>
</cmr:checklist>
