/* Register TW Javascripts */

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
  GEOHandler.TW = [ '858' ];
  console.log('adding Taiwan functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.TW);

  GEOHandler.addAfterConfig(afterConfigTW, GEOHandler.TW);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.TW);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.TW, GEOHandler.ROLE_PROCESSOR, true);

});