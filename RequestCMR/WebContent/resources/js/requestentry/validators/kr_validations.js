/* Register KR Javascripts */

function afterConfigKR() {
  var role = null;
  var reqType = null;

   reqType = FormManager.getActualValue('reqType');
   if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');
  //FormManager.addValidator('OriginatorName',Validators.REQUIRED, ['Requested For Name (Originator)'], 'MAIN_GENERAL_TAB');
  FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');

  
  FormManager.disable('cmrNoPrefix');
  // Non editable for requester
  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('isuCd');
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