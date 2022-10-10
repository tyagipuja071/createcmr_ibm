<%@page import="com.ibm.cio.cmr.request.model.BaseModel"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %> 
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<%
  RequestEntryModel reqentry = (RequestEntryModel) request.getAttribute("reqentry");
  Boolean readOnly = (Boolean) request.getAttribute("yourActionsViewOnly");
  if (readOnly == null) {
    readOnly = false;
  }
  boolean newEntry = BaseModel.STATE_NEW == reqentry.getState();
%>
<cmr:view forGEO="TW">
  <cmr:row topPad="10" addBackground="true" >
  	<cmr:column span="2" containerForField="MrcCd">
      <p>
        <cmr:label fieldId="mrcCd"> <cmr:fieldLabel fieldId="MrcCd" />: </cmr:label>
        <cmr:field path="mrcCd" id="mrcCd" fieldId="MrcCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
  		  <cmr:column span="2" containerForField="SalRepNameNo">
      		<p>
        	  <cmr:label fieldId="repTeamMemberNo">
          	    <cmr:fieldLabel fieldId="SalRepNameNo" />:<cmr:info text="No impact on CMR/account owner, just AP CMR legacy system(WTAAS) requirement. "></cmr:info>
           		<cmr:delta text="${rdcdata.repTeamMemberNo}" oldValue="${reqentry.repTeamMemberNo}" />
<%-- 		       	<cmr:view >
			      <div class="cmr-inline" >
			        <cmr:bluepages model="salesRepNameNo" namePath="repTeamMemberName" idPath="repTeamMemberNo" useUID="true" editableId="true"/>
			        <span style="padding-left: 5px">&nbsp;</span>
			      </div>
			    </cmr:view> --%>
        	  </cmr:label>
        	  <cmr:field fieldId="SalRepNameNo" id="repTeamMemberNo" path="repTeamMemberNo" tabId="MAIN_IBM_TAB" />
      		</p>
    	  </cmr:column>
    	  
    </cmr:row>
    
    <cmr:row topPad="10" addBackground="true">
    <cmr:column span="2" containerForField="CollectionCd">
      <p>
        <cmr:label fieldId="collectionCd">
        <cmr:fieldLabel fieldId="CollectionCd" />: 
           <cmr:delta text="${rdcdata.collectionCd}" oldValue="${reqentry.collectionCd}" id="delta-engineeringBo" />
        </cmr:label>
        <cmr:field path="collectionCd" id="collectionCd" fieldId="CollectionCd" tabId="MAIN_IBM_TAB" />
      </p>
    </cmr:column>
    <cmr:column span="2" containerForField="CmrNoPrefix">
        <p>
          <cmr:label fieldId="cmrNoPrefix">
            <cmr:fieldLabel fieldId="CmrNoPrefix" />:
          </cmr:label>
          <cmr:field path="cmrNoPrefix" id="cmrNoPrefix" fieldId="CmrNoPrefix" tabId="MAIN_IBM_TAB" />
        </p>
      </cmr:column>
  </cmr:row>
  
</cmr:view>