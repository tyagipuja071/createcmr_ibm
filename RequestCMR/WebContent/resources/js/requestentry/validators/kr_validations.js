/* Register KR Javascripts */

function afterConfigTW() {
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

}

dojo.addOnLoad(function() {
  GEOHandler.KR = [ '766' ];
  console.log('adding KOREA functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.KR);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigTW, GEOHandler.KR);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.KR);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.KR, GEOHandler.ROLE_PROCESSOR, true);

});