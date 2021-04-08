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
  FormManager.readOnly('isuCd');

  FormManager.addValidator('custAcctType', Validators.REQUIRED, [ 'Customer Type' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('mktgDept', Validators.REQUIRED, [ 'Tax Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('requesterId', Validators.REQUIRED, [ 'Requester' ], 'MAIN_GENERAL_TAB');
  FormManager.addValidator('originatorNm', Validators.REQUIRED, [ 'Requester' ], 'MAIN_GENERAL_TAB');

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
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
  }

  setVatValidator();
}

/**
 * After config handlers
 */
var _taxTypeHandler = null;
var _ISICHandler = null;
var _dupCmrIndcHandler = null;

function addHandlersForTW() {
  if (_taxTypeHandler == null) {
    _taxTypeHandler = dojo.connect(FormManager.getField('invoiceSplitCd'), 'onChange', function(value) {
      setVatValidator();
    });
  }

  if (_ISICHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setISUCodeValues(value);
    });
  }

  if (_dupCmrIndcHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('dupCmrIndc'), 'onChange', function(value) {
      setDupCmrIndcWarning();
    });
  }
}

/*
 * Set CMR Double Creation warning when its value is Y
 */
function setDupCmrIndcWarning() {
  var dupCmrIndc = FormManager.getActualValue('dupCmrIndc');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == null) {
    return;
  }
  var duplicateCMR = $('#duplicateCMR');
  if (duplicateCMR != null) {
    if (dupCmrIndc == 'Y') {
      $('#duplicateCMR').text('Singapore Offshore CMR request must be submitted also in CreateCMR');
      $('#duplicateCMR').css({
        "color" : "red",
        "font-weight" : "bold"
      });
    } else {
      $('#duplicateCMR').text('');
    }
  }
}

/*
 * Set ISU Code Values based On ISIC
 */
function setISUCodeValues(isicCd) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isicCd = FormManager.getActualValue('isicCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');

  if (custSubGrp == '') {
    return;
  }

  if ((isicCd == _pagemodel.isicCd) && (custSubGrp == _pagemodel.custSubGrp)) {
    return;
  }

  var isuCode = [];

  if (cntry == '858') {
    if (isicCd != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + cntry,
        REP_TEAM_CD : '%' + isicCd + '%'
      };
      var results = cmr.query('GET.ICLIST.BYISIC', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          isuCode.push(results[i].ret1);
        }
        if (isuCode != null) {
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCode);

          if (isuCode.length == 1) {
            FormManager.setValue('isuCd', isuCode[0]);
          }
        }
      }
    }
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
  GEOHandler.addAfterConfig(addHandlersForTW, GEOHandler.TW);
  // GEOHandler.addAfterConfig(setISUCodeValues, GEOHandler.TW);
  GEOHandler.addAfterConfig(setDupCmrIndcWarning, GEOHandler.TW);

  GEOHandler.addAfterTemplateLoad(afterConfigTW, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(addHandlersForTW, GEOHandler.TW);
  // GEOHandler.addAfterTemplateLoad(setISUCodeValues, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(setDupCmrIndcWarning, GEOHandler.TW);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.TW);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.TW, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.TW, GEOHandler.ROLE_REQUESTER, true);

});