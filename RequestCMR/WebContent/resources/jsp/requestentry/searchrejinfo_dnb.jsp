<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!--  Modal for the DnB Search Rejection Info Screen -->
<cmr:modal title="${ui.title.srchRejInfo}" id="DNBRejectionInfoModal"
	widthId="570">


		<cmr:row>
			<cmr:column  span="1" width="120">
				<label for="reqentry_findDnbUsrId">${ui.user}:</label>
			</cmr:column>
			<cmr:column span="2">
         		${reqentry.findDnbUsrId}
			</cmr:column>
		</cmr:row>
		<cmr:row>
			<cmr:column  span="1" width="120">
				<label for="reqentry_findDnbUsrNm"></label>
			</cmr:column>
			<cmr:column span="2">
         		${reqentry.findDnbUsrNm}
			</cmr:column>
		</cmr:row>


		<cmr:row>
			<cmr:column  span="1" width="120">
				<label for="reqentry_findDnbDate">${ui.Date}:</label>
			</cmr:column>

			<cmr:column span="2">
       			${reqentry.findDnbDate}
			</cmr:column>
		</cmr:row>



		<cmr:row>
			<cmr:column  span="1" width="120">
				<label for="reqentry_findDnbRejReason">${ui.RejectReason}:</label>
			</cmr:column>

			<cmr:column span="2">
       			 ${reqentry.findDnbRejReason}
     		 </cmr:column>
		</cmr:row>


			<cmr:row>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="findDnbRejCmt">
                 ${ui.Comments}:
              </cmr:label>
				</p>
				 </cmr:column>
        </cmr:row>
        <cmr:row>
          <cmr:column span="2">
				<p>
           <textarea cols="50" rows="5" readonly>${reqentry.findDnbRejCmt}</textarea>
            </p>
          </cmr:column>
        </cmr:row>
		<cmr:buttonsRow>
			<cmr:hr />
			<cmr:button label="${ui.btn.close}"
				onClick="cmr.hideModal('DNBRejectionInfoModal')" highlight="true" />
		</cmr:buttonsRow>
</cmr:modal>