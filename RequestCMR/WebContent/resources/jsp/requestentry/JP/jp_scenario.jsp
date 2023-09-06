<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<style>

#jpScenarioModalCreate div.ibm-columns {
  width: 550px !important;
}


</style>
<!--  Modal Company Import -->
<cmr:modal title="What do you want to create?" id="jpScenarioModalCreateC" widthId="570">
  <cmr:row>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="550">
      <input type="radio" name="jpScenarioC" value="CEA">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Company (modeled after chosen record), Establishment, and Account
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioC" value="EA">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Establishment and Account under the chosen Company
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioC" value="C">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Subsidiary Company (BC or BF) modeled after chosen Company
      </cmr:label>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Proceed" onClick="proceedWithImport('C')" highlight="true" pad="true" />
    <cmr:button label="Cancel" onClick="cmr.hideModal('jpScenarioModalCreateC')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>

<!--  Modal Establishment Import -->

<cmr:modal title="What do you want to create?" id="jpScenarioModalCreateE" widthId="570">
  <cmr:row>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="550">
<%--      <input type="radio" name="jpScenarioE" value="CEA">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Company (modeled after the chosen record's parent Company), Establishment (modeled after the chosen record), and Account. 
      </cmr:label>
      <br> --%>
      <input type="radio" name="jpScenarioE" value="EA">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Establishment (modeled after the chosen record) and Account under the Company of the chosen Establishment.
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioE" value="A">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Account under the chosen Establishment and its parent Company
      </cmr:label>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Proceed" onClick="proceedWithImport('E')" highlight="true" pad="true" />
    <cmr:button label="Cancel" onClick="cmr.hideModal('jpScenarioModalCreateE')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>

<!--  Modal Account Import -->
<cmr:modal title="What do you want to create?" id="jpScenarioModalCreateA" widthId="570">
  <cmr:row>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="550">
<%--      <input type="radio" name="jpScenarioA" value="CEA">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Company, Establishment, and Account, all modeled after the chosen Account's details and parent records.
      </cmr:label>
      <br> --%>
      <input type="radio" name="jpScenarioA" value="A" checked>
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Account (modeled after the chosen record) under the Establishment and Company of the chosen Account.
      </cmr:label>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Proceed" onClick="proceedWithImport('A')" highlight="true" pad="true" />
    <cmr:button label="Cancel" onClick="cmr.hideModal('jpScenarioModalCreateA')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>

<!--  Modal Company Import -->
<cmr:modal title="What do you want to create?" id="jpScenarioModalCreateX" widthId="570">
  <cmr:row>
    <cmr:note text="No record was found during the search." />
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="550">
      <input type="radio" name="jpScenarioX" value="CEA">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Company, Establishment, and Account
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioX" value="C">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        A new Subsidiary Company (BC or BF) 
      </cmr:label>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Proceed" onClick="proceedWithImport('X')" highlight="true" pad="true" />
    <cmr:button label="Cancel" onClick="cmr.hideModal('jpScenarioModalCreateX')" highlight="false" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>

<!-- Modal Update -->
<cmr:modal title="What do you want to update?" id="jpScenarioModalUpdate" widthId="570">
  <cmr:row>
  </cmr:row>
  <cmr:row>
    <cmr:column span="6" width="550">
      <input type="radio" name="jpScenarioU" value="CEA">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        Update Account only/Account & Establishment/ Account & Establishment & Company
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioU" value="CE">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        Update Establishment only/Establishment & Company
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioU" value="C">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        Update Company only
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioU" value="CR">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        ROL Flag Change on Company No
      </cmr:label>
      <br>
      <input type="radio" name="jpScenarioU" value="AR">
      <cmr:label fieldId="" forRadioOrCheckbox="true">
        ROL Flag Change on Account No
      </cmr:label>
    </cmr:column>
  </cmr:row>
  <cmr:buttonsRow>
    <cmr:hr />
    <cmr:button label="Proceed" onClick="createRequest()" highlight="true" pad="true" />
  </cmr:buttonsRow>
</cmr:modal>
