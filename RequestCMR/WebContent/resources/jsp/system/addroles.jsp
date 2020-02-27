<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<style>
  span.role {
    margin-left: 20px;
    font-size: 13px;
  }
</style>
<!--  Modal for the Address Verification Rules modal -->
<cmr:modal title="Add Roles" id="addRolesModal" widthId="390">
  <cmr:row>
    <cmr:column span="2" width="350">
    <label>Select which roles to add to the user:<span style="color:red" class="cmr-ast">*</span></label>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <cmr:column span="2" width="350">
    <div>
      <span class="role" id="ADMIN|"><input type="checkbox" name="role" value="ADMIN|" id="chk_ADMIN|">Admin [All]<br></span>
      
      <span class="role" id="REQUESTER|"><input type="checkbox" name="role" value="REQUESTER|" id="chk_REQUESTER|">Requester [CreateCMR]<br></span>
      <span class="role" id="PROCESSOR|PROC_BASIC"><input type="checkbox" name="role" value="PROCESSOR|PROC_BASIC" id="chk_PROCESSOR|PROC_BASIC">Processor (Basic) [CreateCMR]<br></span>
      <span class="role" id="PROCESSOR|PROC_VALIDATOR"><input type="checkbox" name="role" value="PROCESSOR|PROC_VALIDATOR" id="chk_PROCESSOR|PROC_VALIDATOR">Processor (Complex/Validator) [CreateCMR]<br></span>
      <span class="role" id="PROCESSOR|PROC_SUBMITTER"><input type="checkbox" name="role" value="PROCESSOR|PROC_SUBMITTER" id="chk_PROCESSOR|PROC_SUBMITTER">Processor (Complex/Submitter) [CreateCMR]<br></span>

      <span class="role" id="USER|"><input type="checkbox" name="role" value="USER|" id="chk_USER|">General User [FindCMR]<br></span>
      <span class="role" id="WS_ADMIN|"><input type="checkbox" name="role" value="WS_ADMIN|" id="chk_WS_ADMIN|">WebService Administrator [FindCMR]<br></span>

      <span class="role" id="CMDE|"><input type="checkbox" name="role" value="CMDE|" id="chk_CMDE|">CMDE Administrator [All]<br></span>


    </div>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="15">
    <cmr:column span="2" width="350">
      <label>Comments:<span style="color:red" class="cmr-ast">*</span></label>
    </cmr:column>
  </cmr:row>
  <cmr:row topPad="10">
    <cmr:column span="2" width="350">
      <textarea style="width:300px" id="addrolecomments" rows="3" cols="25"></textarea>
    </cmr:column> 
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="${ui.btn.ok}" onClick="UserService.addSelectedRoles()" highlight="true" />
    <cmr:button label="${ui.btn.cancel}" onClick="cmr.hideModal('addRolesModal')" pad="true"/>
  </cmr:buttonsRow>
</cmr:modal>
