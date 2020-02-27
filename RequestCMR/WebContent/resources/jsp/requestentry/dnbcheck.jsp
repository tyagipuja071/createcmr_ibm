<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<style>

#dnbCheckModal div.ibm-columns {
  width: 550px !important;
}

table.dnb-table {
  width: 540px;
}

table.dnb-table td{
  font-size: 13px;
  padding: 2px;
  word-wrap : break-word;
}

table.dnb-table td.dnb-label{
  text-align: right;
  font-weight: bold;
  padding-right: 5px;
  width: 200px !important;
}
table.dnb-table td.dnb-sub {
  font-weight: bold;
  font-size:14px;
  text-decoration: underline;
}
</style>
<!--  Modal for the D&B Check Screen -->
<cmr:modal title="D&B Results" id="dnbCheckModal" widthId="570">
  <cmr:row>
    <cmr:column span="5" width="550">
      <table id="dnb_table" class="dnb-table" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td colspan="2" class="dnb-sub">General Details</td>
        </tr>
        <tr>
          <td class="dnb-label">Company Name:</td>
          <td id="dnb_companyName"></td>
        </tr>
        <tr>
          <td class="dnb-label">Tradestyle Name:</td>
          <td id="dnb_tradestyleName"></td>
        </tr>
        <tr>
          <td class="dnb-label">DUNS No.:</td>
          <td id="dnb_dunsNo"></td>
        </tr>
        <tr>
          <td class="dnb-label">Transferred from DUNS No.:</td>
          <td id="dnb_transferDunsNo"></td>
        </tr>
        <tr>
          <td class="dnb-label">DU DUNS No.:</td>
          <td id="dnb_duDunsNo"></td>
        </tr>
        <tr>
          <td class="dnb-label">DU Organization:</td>
          <td id="dnb_duOrganizationName"></td>
        </tr>
        <tr>
          <td class="dnb-label">GU DUNS No.:</td>
          <td id="dnb_guDunsNo"></td>
        </tr>
        <tr>
          <td class="dnb-label">GU Organization:</td>
          <td id="dnb_guOrganizationName"></td>
        </tr>
        <tr>
          <td class="dnb-label">IBM ISIC:</td>
          <td><span id="dnb_ibmIsic"></span>&nbsp;<span id="dnb_ibmIsicDesc"></span>
          </td>
        </tr>
        <tr>
          <td colspan="2" class="dnb-sub">Primary Address</td>
        </tr>
        <tr>
          <td class="dnb-label">Address:</td>
          <td id="dnb_primaryAddress"></td>
        </tr>
        <tr>
          <td class="dnb-label">City:</td>
          <td id="dnb_primaryCity"></td>
        </tr>
        <tr>
          <td class="dnb-label">State:</td>
          <td id="dnb_primaryStateName"></td>
        </tr>
        <tr>
          <td class="dnb-label">County:</td>
          <td id="dnb_primaryCounty"></td>
        </tr>
        <tr>
          <td class="dnb-label">Postal Code:</td>
          <td id="dnb_primaryPostalCode"></td>
        </tr>
        <tr>
          <td class="dnb-label">Country:</td>
          <td id="dnb_primaryCountry"></td>
        </tr>
        <tr>
          <td colspan="2" class="dnb-sub">Mailing Address</td>
        </tr>
        <tr>
          <td class="dnb-label">Address:</td>
          <td id="dnb_mailingAddress"></td>
        </tr>
        <tr>
          <td class="dnb-label">City:</td>
          <td id="dnb_mailingCity"></td>
        </tr>
        <tr>
          <td class="dnb-label">State:</td>
          <td id="dnb_mailingStateName"></td>
        </tr>
        <tr>
          <td class="dnb-label">County:</td>
          <td id="dnb_mailingCounty"></td>
        </tr>
        <tr>
          <td class="dnb-label">Postal Code:</td>
          <td id="dnb_mailingPostalCode"></td>
        </tr>
        <tr>
          <td class="dnb-label">Country:</td>
          <td id="dnb_mailingCountry"></td>
        </tr>
        <tr>
          <td colspan="2" class="dnb-sub">Organization</td>
        </tr>
        <tr>
          <td class="dnb-label">Organization Type:</td>
          <td id="dnb_organizationType"></td>
        </tr>
        <tr>
          <td class="dnb-label">Organization IDs:</td>
          <td id="dnb_organizationId"></td>
        </tr>
        <tr>
          <td class="dnb-label">D&B Industry Codes:</td>
          <td id="dnb_dnbStandardIndustryCodes"></td>
        </tr>
        <tr>
          <td class="dnb-label">Line of Business:</td>
          <td id="dnb_lineOfBusiness"></td>
        </tr>
        <tr>
          <td class="dnb-label"># of Employees:</td>
          <td id="dnb_individualEmployeeCount"></td>
        </tr>
        <tr>
          <td class="dnb-label"># of Employees (Consolidated):</td>
          <td id="dnb_consolidatedEmployeeCount"></td>
        </tr>
        <tr>
          <td class="dnb-label">Principal Person Name:</td>
          <td id="dnb_principalName"></td>
        </tr>
        <tr>
          <td class="dnb-label">Principal Person Title:</td>
          <td id="dnb_principalTitle"></td>
        </tr>
        <tr>
          <td class="dnb-label">Telephone No:</td>
          <td id="dnb_phoneNo"></td>
        </tr>
        <tr>
          <td class="dnb-label">Facsimile No:</td>
          <td id="dnb_faxNo"></td>
        </tr>
      </table>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Close" onClick="cmr.hideModal('dnbCheckModal')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
