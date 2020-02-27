<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!--  Modal for the Search Rejection Info Screen -->
<cmr:modal title="${ui.title.srchRejInfo}" id="CMRRejectionInfoModal" widthId="570">

		<cmr:row>
			<cmr:column span="1" width="120">
				<label for="reqentry_findCmrUsrId">${ui.user}:</label>
			</cmr:column>
			<cmr:column span="2">
         		${reqentry.findCmrUsrId}
			</cmr:column>
		</cmr:row>
		<cmr:row>
			<cmr:column span="1" width="120">
				<label for="reqentry_findCmrUsrNm"></label>
			</cmr:column>
			<cmr:column span="2">
         		${reqentry.findCmrUsrNm}
			</cmr:column>
		</cmr:row>


		<cmr:row>
			<cmr:column span="1" width="120">
				<label for="reqentry_findCmrDate">${ui.Date}:</label>
			</cmr:column>

			<cmr:column span="2">
       			${reqentry.findCmrDate}
			</cmr:column>
		</cmr:row>



		<cmr:row>
			<cmr:column span="1" width="120">
				<label for="reqentry_findCmrRejReason">${ui.RejectReason}:</label>
			</cmr:column>

			<cmr:column span="2">
       			 ${reqentry.findCmrRejReason}
     		 </cmr:column>
		</cmr:row>


		<cmr:row>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="findCmrRejCmt">
                 ${ui.Comments}:
              </cmr:label>
				</p>
				 </cmr:column>
        </cmr:row>
        <cmr:row>
          <cmr:column span="2">
				<p>
           <textarea cols="50" rows="5" readonly>${reqentry.findCmrRejCmt}</textarea>
            </p>
          </cmr:column>
        </cmr:row>
		<cmr:buttonsRow>
			<cmr:hr />
			<cmr:button label="${ui.btn.close}"
				onClick="cmr.hideModal('CMRRejectionInfoModal')" highlight="true" />
		</cmr:buttonsRow>
</cmr:modal>