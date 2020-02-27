<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<!--  Modal for the Search Rejection Info Screen -->
<cmr:modal title="${ui.title.addrStdRejInfo}" id="AddrStdRejectionInfoModal"
	widthId="750">
	<form:form method="GET" action="${contextPath}/request/addrstdrejreason"
		name="frmCMR_srchRejAddrStdRej" class="ibm-column-form ibm-styled-form"
		modelAttribute="reqentry">
		
		<form:hidden path="yourId" />
		<form:hidden path="yourNm" />
		<form:hidden path="addrStdRejReason" />
		<form:hidden path="addrStdRejCmt" />
		<form:hidden path="addrStdDate" />

		
		<cmr:row>
			<cmr:column span="2">
				<label for="reqentry_yourId">${ui.user}:</label>
			</cmr:column>
			<cmr:column span="2">
         		${reqentry.yourId}
			</cmr:column>
		</cmr:row>
		<cmr:row>
			<cmr:column span="2">
				<label for="reqentry_yourNm"></label>
			</cmr:column>
			<cmr:column span="2">
         		${reqentry.yourNm}
			</cmr:column>
		</cmr:row>


		<cmr:row>
			<cmr:column span="2">
				<label for="reqentry_addrStdDate">${ui.Date}:</label>
			</cmr:column>

			<cmr:column span="2">
       			${reqentry.addrStdDate}
			</cmr:column>
		</cmr:row>



		<cmr:row>
			<cmr:column span="2">
				<label for="reqentry_addrStdRejReason">${ui.RejectReason}:</label>
			</cmr:column>

			<cmr:column span="2">
       			 ${reqentry.addrStdRejReason}
     		 </cmr:column>
		</cmr:row>


			<cmr:row>
          <cmr:column span="2">
            <p>
              <cmr:label fieldId="addrStdRejCmt">
                 ${ui.Comments}:
              </cmr:label>
				</p>
				 </cmr:column>
        </cmr:row>
        <cmr:row>
          <cmr:column span="2">
				<p>
              <form:textarea  path="addrStdRejCmt" readonly="true" rows="5" cols="50" />
            </p>
          </cmr:column>
        </cmr:row>
		<cmr:buttonsRow>
			<cmr:hr />
			<cmr:button label="${ui.btn.close}"
				onClick="cmr.hideModal('AddrStdRejectionInfoModal')" highlight="true" />
		</cmr:buttonsRow>
	</form:form>
</cmr:modal>