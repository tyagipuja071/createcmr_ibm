<%@page import="com.ibm.cio.cmr.request.ui.PageManager"%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
	<cmr:row addBackground="false">
        <cmr:column span="3" width="600">
            <p>
              
              <label for="cmt">
                <cmr:fieldLabel fieldId="Comments" />:
                <%if ("M".equals(PageManager.get().getManager("Comments").getType())){ %>
                  <cmr:memoLimit maxLength="2000" fieldId="cmt"/>
                <%}%>
              </label>
              <cmr:field  id="cmt" path="cmt" fieldId="Comments" rows="6" cols="60" />
            </p>
        </cmr:column>
      </cmr:row>
