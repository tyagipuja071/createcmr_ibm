/* Register KR Javascripts */

function afterConfigKR() {
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');

  FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  // FormManager.readOnly('isuCd');
  // Non editable for requester, same as TW
  FormManager.disable('isuCd');
  FormManager.disable('cmrNoPrefix');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

}

dojo.addOnLoad(function() {
  GEOHandler.KR = [ '766' ];
  console.log('adding KOREA functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.KR);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigKR, GEOHandler.KR);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.KR);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.KR, GEOHandler.ROLE_PROCESSOR, true);

});