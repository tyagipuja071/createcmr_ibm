/*
 * File: tgmeaddrstd.js
 * Description: 
 * Contains the functions related to TGME address standardization
 * 
 */
/**
 * Opens the CMR serach reject reason for the given request
 * 
 */
function showAddrSTdRejectedModal() {
  cmr.showModal('AddrStdRejectionInfoModal');
}

/**
 * Shows no result available reason modal
 */
function showAddrStdNoResultModal() {
  cmr.showModal('AddrStdModal');
}

/**
 * Onload function for the rejection info modal
 */
function AddrStdModal_onLoad() {

}

function AddrStdModal_onClose() {
}

/**
 * Onload function for the rejection info modal
 */
function AddrStdRejectionInfoModal_onLoad() {

}

function AddrStdRejectionInfoModal_onClose() {
}

/**
 * Called for performing AddressStandardization
 */
function performAddressStandardization(saveDirect) {
  if (isTgmeNeeded()) {
    FormManager.setValue('stdCityNm', '');
  }
  cmr.currentModalId = 'addEditAddressModal';
  if (typeof (GEOHandler) != 'undefined') {
    GEOHandler.executeAddrFuncs(true, false);
  }
  if (!FormManager.validate('frmCMR_addressModal', true)) {
    cmr.showAlert('The address contains errors. Please check the list of errors on the page.');
    return;
  }
  MessageMgr.clearMessages(true);
  if (FormManager.getActualValue('landCntry') == '') {
    cmr.showAlert('At least Country (Landed) is required for this check.');
    return;
  }
  var validTest = cmr.query('GETTGMEVALID', {
    CD : FormManager.getActualValue('landCntry')
  });
  if (validTest && validTest.ret1 == FormManager.getActualValue('landCntry')) {
    cmr.addrStdSave = saveDirect ? saveDirect : false;
    cmr.checkAddrStd('frmCMR_addressModal', afterStandardizationResults);
    storeTgmeCompare(false);
  } else {
    var country = dijit.byId('landCntry')._lastDisplayedValue;
    cmr.showAlert('Address standardization for ' + country + ' is currently not supported by the service.<br>You must save this record to have this information recorded.', null,
        'saveNotSupportedTGME()');
    storeTgmeCompare(false);
  }
}

function storeTgmeCompare(newAddress, details) {
  if (newAddress) {
    dojo.byId('tgmeCompare').value = '';
  } else {
    if (details) {
      var txt = details.ret13;
      txt += '|' + details.ret8;
      txt += '|' + details.ret11;
      txt += '|' + details.ret9;
      txt += '|' + details.ret12;
      // txt += '|'+details.ret14;
      dojo.byId('tgmeCompare').value = txt;
    } else {
      var txt = FormManager.getActualValue('landCntry');
      txt += '|' + FormManager.getActualValue('addrTxt');
      txt += '|' + FormManager.getActualValue('stateProv');
      txt += '|' + FormManager.getActualValue('city1');
      txt += '|' + FormManager.getActualValue('postCd');
      // txt += '|'+FormManager.getActualValue('county');
      dojo.byId('tgmeCompare').value = txt;
    }
  }
}

function isTgmeNeeded() {
  var stored = FormManager.getActualValue('tgmeCompare');
  var txt = FormManager.getActualValue('landCntry');
  txt += '|' + FormManager.getActualValue('addrTxt');
  txt += '|' + FormManager.getActualValue('stateProv');
  txt += '|' + FormManager.getActualValue('city1');
  txt += '|' + FormManager.getActualValue('postCd');
  // txt += '|'+FormManager.getActualValue('county');
  return stored != txt;
}
function afterStandardizationResults(results) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var landCntry = FormManager.getActualValue('landCntry');
  cmr.addrStdResult = {
    stdResultDesc : results.result.stdResultDesc,
    stdResultCode : results.result.stdResultCode,
    stdAddrTxt : results.result.stdAddrTxt,
    stdAddrTxt2 : cntry == '897' ? null : results.result.stdAddrTxt2,
    addrTxt : results.result.addrTxt,
    stdCity : results.result.stdCity,
    city1 : results.result.city1,
    stdStateProv : results.result.stdStateProv,
    stdStateCode : results.result.stdStateCode,
    stateProv : results.result.stateProv,
    stateProvDesc : results.result.stateProvDesc,
    stdPostCd : cntry == '897' && landCntry != 'US' ? '00000 ' : results.result.stdPostCd,
    postCd : results.result.postCd,
    stdResultText : results.result.stdResultText,
    stdResultStatus : results.result.stdResultStatus,
  };
  // cmr.stdResult=results.result.stdResultDesc;
  if (results.result.stdResultCode == 'C') {
    cmr.showModal('addressStdResultModal');
  } else {
    if (results.result.stdResultCode == 'V') {
      if (cmr.addrStdResult.stdAddrTxt != cmr.addrStdResult.addrTxt || cmr.addrStdResult.stdCity != cmr.addrStdResult.city1 || cmr.addrStdResult.stdStateCode.trim() != cmr.addrStdResult.stateProv
          || cmr.addrStdResult.stdPostCd != cmr.addrStdResult.postCd) {
        cmr.showModal('addressStdResultModal');
      } else {
        cmr.showModal('addressStdGeneralResultModal');
      }
    } else {
      cmr.showModal('addressStdGeneralResultModal');
    }
  }
}

function saveNotSupportedTGME() {
  FormManager.setValue('addrStdAcceptInd', '');
  dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = 'Not Required';
  FormManager.setValue('addr_addrStdResult', 'X');
  FormManager.setValue('addrStdTsString', '');
  dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = '';
  FormManager.setValue('addr_addrStdRejReason', '');
  dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = '';
  FormManager.setValue('addr_addrStdRejCmt', '');
}

function addressStdResultModal_onLoad() {

  dojo.query('#addressStdResultModal #result_view')[0].innerHTML = cmr.addrStdResult.stdResultDesc;

  dojo.query('#addressStdResultModal #stdAddrTxt_view')[0].innerHTML = cmr.addrStdResult.stdAddrTxt
      + (cmr.addrStdResult.stdAddrTxt2 != null && cmr.addrStdResult.stdAddrTxt2 != '' ? '<br>' + cmr.addrStdResult.stdAddrTxt2 : '');
  dojo.query('#addressStdResultModal #addrTxt_view')[0].innerHTML = '(' + cmr.addrStdResult.addrTxt + ')';
  if (cmr.addrStdResult.stdAddrTxt != cmr.addrStdResult.addrTxt) {
    dojo.query('#addressStdResultModal #stdAddrTxt_view')[0].setAttribute('style', 'font-weight:bold');
    dojo.query('#addressStdResultModal #addrTxt_view')[0].setAttribute('style', 'font-weight:normal');
  } else {
    dojo.query('#addressStdResultModal #stdAddrTxt_view')[0].setAttribute('style', 'font-weight:normal');
    dojo.query('#addressStdResultModal #addrTxt_view')[0].setAttribute('style', 'font-weight:normal');
  }

  dojo.query('#addressStdResultModal #stdCity_view')[0].innerHTML = cmr.addrStdResult.stdCity;
  dojo.query('#addressStdResultModal #city1_view')[0].innerHTML = '(' + cmr.addrStdResult.city1 + ')';
  if (cmr.addrStdResult.stdCity != cmr.addrStdResult.city1) {
    dojo.query('#addressStdResultModal #stdCity_view')[0].setAttribute('style', 'font-weight:bold');
    dojo.query('#addressStdResultModal #city1_view')[0].setAttribute('style', 'font-weight:normal');
  } else {
    dojo.query('#addressStdResultModal #stdCity_view')[0].setAttribute('style', 'font-weight:normal');
    dojo.query('#addressStdResultModal #city1_view')[0].setAttribute('style', 'font-weight:normal');
  }

  dojo.query('#addressStdResultModal #stdStateProv_view')[0].innerHTML = cmr.addrStdResult.stdStateProv;
  dojo.query('#addressStdResultModal #stateProv_view')[0].innerHTML = '(' + (cmr.addrStdResult.stateProvDesc ? cmr.addrStdResult.stateProvDesc : '') + ')';
  if (cmr.addrStdResult.stdStateCode.trim() != cmr.addrStdResult.stateProv) {
    dojo.query('#addressStdResultModal #stdStateProv_view')[0].setAttribute('style', 'font-weight:bold');
    dojo.query('#addressStdResultModal #stateProv_view')[0].setAttribute('style', 'font-weight:normal');
  } else {
    dojo.query('#addressStdResultModal #stdStateProv_view')[0].setAttribute('style', 'font-weight:normal');
    dojo.query('#addressStdResultModal #stateProv_view')[0].setAttribute('style', 'font-weight:normal');
  }

  dojo.query('#addressStdResultModal #stdPostCd_view')[0].innerHTML = cmr.addrStdResult.stdPostCd;
  dojo.query('#addressStdResultModal #postCd_view')[0].innerHTML = '(' + cmr.addrStdResult.postCd + ')';
  if (cmr.addrStdResult.stdPostCd != cmr.addrStdResult.postCd) {
    dojo.query('#addressStdResultModal #stdPostCd_view')[0].setAttribute('style', 'font-weight:bold');
    dojo.query('#addressStdResultModal #postCd_view')[0].setAttribute('style', 'font-weight:normal');
  } else {
    dojo.query('#addressStdResultModal #stdPostCd_view')[0].setAttribute('style', 'font-weight:normal');
    dojo.query('#addressStdResultModal #postCd_view')[0].setAttribute('style', 'font-weight:normal');
  }

}

function addressStdResultModal_onClose() {
  // do something here
  // alert ("showing reject reason modal");
}

function doAcceptChanges() {
  actualAcceptChanges();
  // cmr.showConfirm('actualAcceptChanges()', 'Overwrite existing data on
  // address
  // screen with standardized data?');
}

function actualAcceptChanges() {
  cmr.hideModal('addressStdResultModal');
  var date = new Date();
  var streetLength = new Number(dojo.byId('addrTxt').maxLength);
  var street = cmr.addrStdResult.stdAddrTxt;
  var street2 = cmr.addrStdResult.stdAddrTxt2;
  if (street.length > streetLength) {
    var st1 = '';
    var st2 = '';
    var parts = street.split(' ');
    for ( var i = 0; i < parts.length; i++) {
      if ((st1 + ' ' + parts[i]).length > streetLength) {
        st2 = street.substring(st1.length);
        break;
      } else {
        st1 += i > 0 ? ' ' : '';
        st1 += parts[i];
      }
    }
    FormManager.setValue('addrTxt', st1.trim());
    FormManager.setValue('addrTxt2', st2.trim() + (street2 != null && street2 != '' ? ' ' + street2 : ''));
  } else {
    FormManager.setValue('addrTxt', street);
    FormManager.setValue('addrTxt2', (street2 != null && street2 != '' ? ' ' + street2 : ''));
  }
  FormManager.setValue('city1', cmr.addrStdResult.stdCity);
  FormManager.setValue('stdCityNm', cmr.addrStdResult.stdCity);
  FormManager.setValue('stateProv', (cmr.addrStdResult.stdStateCode != null && cmr.addrStdResult.stdStateCode != '' ? cmr.addrStdResult.stdStateCode.trim() : ''));
  FormManager.setValue('postCd', cmr.addrStdResult.stdPostCd);
  FormManager.setValue('addrStdAcceptInd', 'Y');
  if (FormManager.getActualValue('addrStdAcceptInd') == 'Y') {
    dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = 'Accepted';
  }
  FormManager.setValue('addr_addrStdResult', cmr.addrStdResult.stdResultCode);
  FormManager.setValue('addrStdTsString', moment(date).format("YYYY-MM-DD"));
  dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = moment(date).format("YYYY-MM-DD");
  FormManager.setValue('addr_addrStdRejReason', '');
  dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = '';
  FormManager.setValue('addr_addrStdRejCmt', '');
  if (cmr.addrStdSave) {
    storeTgmeCompare(false);
    doAddToAddressList();
  } else {
    cmr.showAlert('You must save this record to have the address standardization recorded.', null, null, true);
    storeTgmeCompare(false);
  }
}

function doRejectChanges() {
  cmr.showModal('addressStdRejectReasonModal');
  // cmr.showConfirm('actualAcceptChanges()', 'Overwrite existing data on
  // name/address screen with standardized data?');
}

/**
 * Called for cancel add/edit address
 */
function cancelStdResult() {
  actualCancelStdResultModal();
  // cmr.showConfirm('actualCancelStdResultModal()', 'Cancel ');
}
/**
 * Called for cancel add/edit address
 */
function actualCancelStdResultModal() {
  cmr.hideModal('addressStdResultModal');
  MessageMgr.clearMessages(true);
}
function addressStdRejectReasonModal_onLoad() {
  dojo.byId('addrStdRejCmtTA').value = '';
  FormManager.setValue('addrStdRejReason', '');
}

function addressStdRejectReasonModal_onClose() {
  // do something here
  // alert ("showing reject reason modal");
}

function addressStdGeneralResultModal_onLoad() {
  dojo.query('#addressStdGeneralResultModal #generalStdResult_view')[0].innerHTML = cmr.addrStdResult.stdResultText;
}

function addressStdGeneralResultModal_onClose() {
  // do something here
  // alert ("showing reject reason modal");
}

function doCloseGeneralStdResult() {
  cmr.hideModal('addressStdGeneralResultModal');
  var date = new Date();
  FormManager.setValue('addr_addrStdResult', cmr.addrStdResult.stdResultCode);
  FormManager.setValue('addrStdTsString', moment(date).format("YYYY-MM-DD"));
  FormManager.setValue('addrStdAcceptInd', '');
  dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = cmr.addrStdResult.stdResultStatus;
  dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = moment(date).format("YYYY-MM-DD");
  FormManager.setValue('addr_addrStdRejReason', '');
  dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = '';
  FormManager.setValue('addr_addrStdRejCmt', '');
  if (cmr.addrStdSave) {
    doAddToAddressList();
  } else {
    cmr.showAlert('You must save this record to have the address standardization recorded.', null, null, true);
  }
}

function doSaveRejectReason() {
  cmr.rejectMode = 'save';
  var rejreason = FormManager.getActualValue('addrStdRejReason');
  if (rejreason == '') {
    cmr.showAlert('Please select a reason for rejecting the result.', 'Error');
    return;
  }
  actualRejectChanges();
  // cmr.showConfirm('actualRejectChanges()', 'Are you sure you want to reject
  // the
  // results?');
}

function doCancelRejectReason() {
  cmr.hideModal('addressStdRejectReasonModal');
}

function actualRejectChanges() {
  cmr.hideModal('addressStdRejectReasonModal');
  if (cmr.rejectMode == 'save') {
    cmr.hideModal('addressStdResultModal');
    dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = "Rejected";
    FormManager.setValue('addrStdAcceptInd', "N");
    FormManager.setValue('addr_addrStdResult', cmr.addrStdResult.stdResultCode);
    FormManager.setValue('addr_addrStdRejReason', FormManager.getActualValue('addrStdRejReason'));
    dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = dijit.byId('addrStdRejReason')._lastDisplayedValue;
    FormManager.setValue('addr_addrStdRejCmt', FormManager.getActualValue('addrStdRejCmtTA'));
    var dtStr = moment(new Date()).format('YYYY-MM-DD');
    FormManager.setValue('addrStdTsString', dtStr);
    dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = dtStr;
    if (cmr.addrStdSave) {
      doAddToAddressList();
    } else {
      cmr.showAlert('You must save this record to have the address standardization recorded.', null, null, true);
    }
  }

}
