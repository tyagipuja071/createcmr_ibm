/* Register TW Javascripts */

function afterConfigTW() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var invoiceSplitCd = FormManager.getActualValue('invoiceSplitCd');
  var role = null;
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');

  FormManager.addValidator('custAcctType', Validators.REQUIRED, [ 'Customer Type' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('mktgDept', Validators.REQUIRED, [ 'Tax Location' ], 'MAIN_CUST_TAB');

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (custSubGrp == 'LOECO' || custSubGrp == 'LOINT' || custSubGrp == 'LOBLU' || custSubGrp == 'LOMAR' || custSubGrp == 'LOOFF') {
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Cluster ID' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'GB Segment' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ 'Market Responsibility Code (MRC)' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep No' ], 'MAIN_IBM_TAB');
  }
  if (custSubGrp == 'CBFOR') {
    FormManager.addValidator('invoiceSplitCd', Validators.REQUIRED, [ 'Tax Type' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Customer Location' ], 'MAIN_CUST_TAB');
  }
  if (custSubGrp == 'LOINT') {
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
  }

  setVatValidator();
}

/**
 * After config handlers
 */
var _taxTypeHandler = null;

function addHandlersForBELUX() {
  if (_taxTypeHandler == null) {
    _taxTypeHandler = dojo.connect(FormManager.getField('invoiceSplitCd'), 'onChange', function(value) {
      setVatValidator();
    });
  }
}

/* Unify Number Handler */
function setVatValidator() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var invoiceSplitCd = FormManager.getActualValue('invoiceSplitCd');
  if (invoiceSplitCd == 'TN') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'Unify Number' ], 'MAIN_CUST_TAB');
  }
}

dojo.addOnLoad(function() {
  GEOHandler.TW = [ '858' ];
  console.log('adding Taiwan functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.TW);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigTW, GEOHandler.TW);
  GEOHandler.addAfterConfig(addHandlersForBELUX, GEOHandler.TW);

  GEOHandler.addAfterTemplateLoad(afterConfigTW, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(addHandlersForBELUX, GEOHandler.TW);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.TW);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.TW, GEOHandler.ROLE_PROCESSOR, true);

});