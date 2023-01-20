/* 
 * File: findcmrsearch.js
 * Description:
 * Contains the scripts for CMR Search and D&B search, as well as result acceptance and rejection
 */

var _findCMRWin = null;
var _findCMROK = false;
var _findMode = null;
var CNTRY_LIST_FOR_INVALID_CUSTOMERS = [ '838', '866', '754' ];
var CNTRY_LIST_INACTIVE_IMPORT_CHECK = [ '624', '358', '359', '363', '603', '607', '618', '620', '626', '642', '644', '651', '668', '675', '677', '680', '693', '694', '695', '699', '704', '705',
    '707', '708', '729', '740', '741', '752', '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889', '666', '726', '754', '755', '758', '862',
    '866', '822', '838', '864', '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769',
    '770', '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883', '788', '678', '702', '806', '846' ];

var _findCmrServer = '';
/**
 * Opens Find CMR search
 */
function doCMRSearch() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');

  if ("X" == reqType) {
    console.log("Calling Single Reactivation");
    singleReactCMRSearch(cntry, reqId);
  } else {
    var url = cmr.CONTEXT_ROOT + '/connect?system=cmr&cntry=' + cntry + '&reqId=' + reqId;
    var specs = 'location=no,menubar=no,resizable=no,scrollbars=yes,status=no,toolbar=no,height=' + screen.height + 'px,width=' + screen.width + 'px';
    _findCMRWin = window.open(url, 'win_findCMR', specs, true);
    _findCMROK = false;
    _findMode = 'cmr';
    window.setTimeout('waitForResult()', 1000);
    cmr.showProgress('Waiting for Find CMR Search results..');
  }
}

/**
 * Opens D&B search
 */
function doDnBSearch() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var land1 = cmr.query('GETISOCNTRY', {
    COUNTRY : cntry,
    MANDT : cmr.MANDT
  });
  dojo.cookie('connectCountry', land1.ret1);
  var url = cmr.CONTEXT_ROOT + '/connect?system=dnb&land1=' + land1.ret1 + '&cntry=' + cntry + '&reqId=' + reqId;
  var specs = 'location=no,menubar=no,resizable=no,scrollbars=yes,status=no,toolbar=no,height=' + screen.height + 'px,width=' + screen.width + 'px';
  _findCMRWin = window.open(url, 'win_findCMR', specs, true);
  _findCMROK = false;
  _findMode = 'dnb';
  window.setTimeout('waitForResult()', 1000);
  cmr.showProgress('Waiting for D&B Search results..');
}

/**
 * Opens Find CMR search
 */
function doAddressImportSearch() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var url = cmr.CONTEXT_ROOT + '/connect?system=cmr&cntry=' + cntry + '&reqId=' + reqId;
  var specs = 'location=no,menubar=no,resizable=no,scrollbars=yes,status=no,toolbar=no,height=' + screen.height + 'px,width=' + screen.width + 'px';
  _findCMRWin = window.open(url, 'win_findCMR', specs, true);
  _findCMROK = false;
  _findMode = 'addressOnly';
  window.setTimeout('waitForResult()', 1000);
  cmr.showProgress('Waiting for Find CMR Search results..');
}

/**
 * While Find CMR / D&B search is opened, poll the window and wait for a result
 * from Find CMR
 */
function waitForResult() {
  if ((_findCMRWin == null || _findCMRWin.closed) && !_findCMROK) {
    // if the window has been closed manually without returning any result, just
    // hide the progress modal
    cmr.hideProgress();
    return;
  }
  _findCMRWin.postMessage("cmrconnect", _findCmrServer);
  if (!_findCMROK) {
    window.setTimeout('waitForResult()', 2000);
  }
}

window.addEventListener("message", function(e) {
  // event listener for the Find CMR window
  if (e.origin == _findCmrServer && e.data && dojo.fromJson(e.data).src == 'cmrconnect') {
    var result = dojo.fromJson(e.data);
    if (result && result.accepted != 'x') {
      _findCMROK = true;
      if (_findCMRWin) {
        _findCMRWin.close();
        cmr.hideProgress();
      }
      if (result.accepted == 'y') {
        console.log(result);
        if ('dnb' == _findMode) {
          if (result.dnb) {
            importDnb(result);
          } else {
            importCMRConfirm(result);
          }
        } else if ('addressOnly' == _findMode) {
          /* add flow to import address only and not data */
          var reqCmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
          var importcntry = result.data.issuedBy;
          if (importcntry == '756') {
            importcntry = '755';
            cmr.searchIssuingCntry = '756';
          } else {
            cmr.searchIssuingCntry = null;
          }
          if (importcntry != null && importcntry != reqCmrIssuingCntry) {
            cmr.showAlert('The chosen record is not from the same CMR Issuing Country. The record cannot be processed by the system. Please choose another record to import.');
            return;
          }
          var ob = result.data.orderBlock;
          if (ob == '75' || ob == 'CL') {
            cmr.showAlert('Addresses can only be imported from regular CMRs. The chosen CMR is a ' + (ob == '75' ? 'Prospect CMR' : 'CMR Lite') + ' record.');
            return;
          }
          cmr.currentCmrResult = result;
          continueImportAddress();
        } else if (result.data && result.data.orderBlock == '75') {
          var reqCmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
          var importcntry = result.data.issuedBy;
          if (importcntry == '756') {
            importcntry = '755';
            cmr.searchIssuingCntry = '756';
          } else {
            cmr.searchIssuingCntry = null;
          }
          if ('R' == FormManager.getActualValue('reqType') || 'D' == FormManager.getActualValue('reqType')) {
            cmr.showAlert('Prospect CMRs cannot be imported for current request type');
            return;
          }
          if (importcntry != null && importcntry != reqCmrIssuingCntry) {
            cmr.showAlert('The chosen record is not from the same CMR Issuing Country. The record cannot be processed by the system. Please choose another record to import.');
            return;
          }
          var confMsg = 'Record with CMR Number ' + result.data.cmrNum + ' is a <strong>Prospect</strong>. The Request Type will be set to <strong>Create</strong>';
          confMsg += ' and will be tagged to convert it into a Legal CMR. Any current data on the request will also be replaced. Proceed?';
          cmr.currentCmrResult = result;
          cmr.showConfirm('continueCreateProspect()', confMsg, null, null, {
            OK : 'OK',
            CANCEL : 'Cancel'
          });
        } else if (result.data && result.data.orderBlock == '93' && 'U' == FormManager.getActualValue('reqType')) {
          var cntry = FormManager.getActualValue('cmrIssuingCntry');
          var laReactiveEnable = cmr.query('LA_REACTIVATE_COUNTRY', {
            CNTRY_CD : cntry,
          });
          var suppCntryFlag = laReactiveEnable.ret1.includes("1");
          if (suppCntryFlag) {
            var confMsg = 'Record with CMR Number ' + result.data.cmrNum + ' is a <strong>Deactivated CMR</strong>. The Request Type will be set to <strong>Update</strong>';
            confMsg += ' and will be tagged to convert it into a Reactivated CMR. Any current data on the request will also be replaced. Proceed?';
            cmr.currentCmrResult = result;
            cmr.showConfirm('continueUpdateDeactivatedCMR()', confMsg, null, 'continueCreateDeactivatedCMR()', {
              OK : 'Reactivate CMR',
              CANCEL : 'Create By Model'
            });
          } else {
            // Additional Country checks for deactivated records & update
            // requests
            if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GERMANY) {
              cmr.showAlert('Addresses can only be imported from active CMRs. The chosen CMR is an Inactive record.');
              return;
            } else {
              importCMRConfirm(result);
            }
          }
          if (CNTRY_LIST_FOR_INVALID_CUSTOMERS.includes(cntry)) {
            setRequestReason();
          }

        } else if (result.data && ('R' == FormManager.getActualValue('reqType') || 'D' == FormManager.getActualValue('reqType'))) {
          var ob = result.data.orderBlock;
          if (ob != '93' && 'R' == FormManager.getActualValue('reqType')) {
            cmr.showAlert('Addresses can only be imported from inactive CMRs. The chosen CMR is a ' + (ob == '75' ? 'Prospect CMR' : 'Regular') + ' record.');
            return;
          } else if ((ob == '93' || ob == '75' || ob == 'CL') && 'D' == FormManager.getActualValue('reqType')) {
            cmr.showAlert('Addresses can only be imported from active CMRs. The chosen CMR is a ' + (ob == '75' ? 'Prospect CMR' : (ob == 'CL' ? 'CMR Lite' : 'Inactive')) + ' record.');
            return;
          }
          if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SWITZERLAND) {
            var issuingCntry = FormManager.getActualValue('countryUse');
            var landCntry = result.data.countryDesc;
            var subRegion = FormManager.getField('countryUse').displayedValue;

            if ((issuingCntry == '848' && landCntry == 'Liechtenstein') || (issuingCntry == '848LI' && landCntry == 'Switzerland')) {
              cmr.showAlert('Addresses cannot be imported from ' + landCntry + ' (landed country) CMRs for issuing country ' + subRegion + ' .');
              return;
            }
          }
          cmr.currentCmrResult = result;
          continueUpdateCMR(result);

        } else if (result.data && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GERMANY) {
          var ordBlk = result.data.orderBlock;
          if (ordBlk == '93') {
            cmr.showAlert('Addresses can only be imported from active CMRs. The chosen CMR is an Inactive record.');
          }
        } else {
          if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SWITZERLAND) {
            var reqType = FormManager.getActualValue('reqType');
            var reqId = FormManager.getActualValue('reqId');
            var countryUse = FormManager.getActualValue('countryUse');
            if (reqType == 'C' || reqType == 'U') {
              if (result.data.addressType == 'ZS01' && (result.data.orderBlock == '90' || result.data.orderBlock == '93')) {
                cmr.showAlert('This Record has Primary ZS01 address with ' + result.data.orderBlock + '  order block which is not valid to be imported in C/U requests.');
                return;
              }
            }
            if (reqType == 'U') {
              if (result.data.addressType == 'ZS01') {
                if ((result.data.country == 'CH' && countryUse == '848LI') || (result.data.country == 'LI' && countryUse == '848')) {
                  cmr.showAlert('This record with landed country ' + result.data.countryDesc + ' for issuing country sub Region ' + dojo.byId('countryUse').value + ' cannot be imported.');
                  return;
                }
              }
            }
          } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ITALY) {
            var landCntry = result.data.country;
            var issuingCntry = FormManager.getActualValue('countryUse');
            if (issuingCntry == '758' && (result.data.addressType == 'ZP01' || result.data.addressType == 'ZORG' || result.data.addressType == 'ZS02') && landCntry == 'VA') {
              cmr.showAlert('Landed country ' + result.data.countryDesc + ' must be raised under CMR Issuing Country Vatican - 758.');
              return;
            } else if (issuingCntry == '758' && (result.data.addressType == 'ZP01' || result.data.addressType == 'ZORG' || result.data.addressType == 'ZS02') && landCntry == 'SM') {
              cmr.showAlert('Landed country ' + result.data.countryDesc + ' must be raised under CMR Issuing Country San Marino - 758.');
              return;
            }
          }

          importCMRConfirm(result);
        }
      } else {
        if (result.nodata) {
          FormManager.setValue('saveRejectScore', _findMode);
          cmr.showAlert('No data were found in the search. The result will be saved.', null, 'forceSaveRequestNoData()');
        } else {
          FormManager.setValue('saveRejectScore', _findMode);
          if ('dnb' == _findMode) {
            FormManager.setValue('findDnbRejCmt', result.comment);
            FormManager.setValue('findDnbRejReason', result.reason);
          } else {
            FormManager.setValue('findCmrRejCmt', result.comment);
            FormManager.setValue('findCmrRejReason', result.reason);
          }
          cmr.showAlert('You have chosen to reject search results. The request will be saved and the action will be recorded.', 'Warning', 'forceSaveRequestReject()');
        }

      }
    }
  }
}, false);

function setRequestReason() {
  FormManager.setValue('reqReason', 'REAC');
}

function importCMRConfirm(result) {
  var reqCmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var importcntry = result.data.issuedBy;
  if (importcntry == '756') {
    importcntry = '755';
    cmr.searchIssuingCntry = '756';
  } else {
    cmr.searchIssuingCntry = null;
  }
  if (importcntry != null && importcntry != reqCmrIssuingCntry) {
    cmr.showAlert('The chosen record is not from the same CMR Issuing Country. The record cannot be processed by the system. Please choose another record to import.');
    return;
  }
  
  var allowByModel = true;
  var byModel = cmr.query('CREATE_BY_MODEL_DISABLED', {CNTRY_CD : reqCmrIssuingCntry});
  if (byModel && byModel.ret1 == 'Y'){
    allowByModel = false;
  } 
  if (!allowByModel){
    continueUpdateCMR(result);
  } else {
    var confMsg = 'Do you want to Update the selected CMR or Create a new CMR modeled after this one?';
    cmr.currentCmrResult = result;
    cmr.showConfirm('continueCreateCMR()', confMsg, null, 'continueUpdateCMR(result)', {
      OK : 'Create New CMR',
      CANCEL : 'Update CMR'
    });
  }

}
/**
 * Continue importing the record with Create type
 */
function continueCreateCMR() {
  var result = cmr.currentCmrResult;
  if (result.data && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GERMANY) {
    var ordBlk = result.data.orderBlock;
    if (ordBlk == '93') {
      cmr.showAlert('Addresses can only be imported from active CMRs. The chosen CMR is an Inactive record.');
      return;
    }
  }
  FormManager.setValue('reqType', 'C');
  FormManager.setValue('enterCMRNo', '');
  importCMRs(result.data.cmrNum, result.data.issuedBy, result.data.issuedByDesc);
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ITALY) {
    cmr.showConfirm('importOnlyCompany()', 'Do you want to create a new Billing Address or use the Billing CMR ' + result.data.cmrNum + '?', null, 'importBillingCompany()', {
      OK : 'Create New',
      CANCEL : 'Use Existing'
    });
  }
}

function importOnlyCompany() {
  var result = cmr.currentCmrResult;
  FormManager.setValue('lockByNm', '');
  importCMRs(result.data.cmrNum, result.data.issuedBy, result.data.issuedByDesc);
}

function importBillingCompany() {
  var result = cmr.currentCmrResult;
  FormManager.setValue('lockByNm', 'impBill');
  importCMRs(result.data.cmrNum, result.data.issuedBy, result.data.issuedByDesc);
}

/**
 * Continue importing the record with Update type
 */
function continueUpdateCMR(result) {
  FormManager.setValue('reqType', 'U');
  FormManager.setValue('enterCMRNo', result.data.cmrNum);
  importCMRs(result.data.cmrNum, result.data.issuedBy, result.data.issuedByDesc);
}

/**
 * Continue importing the record with Update type
 */
function continueImportAddress() {
  var result = cmr.currentCmrResult;
  importCMRs(result.data.cmrNum, result.data.issuedBy, result.data.issuedByDesc, true);
}

/**
 * Continue importing the record with Create type for Prospect to Legal
 * Conversion
 */
function continueCreateProspect() {
  var result = cmr.currentCmrResult;
  FormManager.setValue('reqType', 'C');
  FormManager.setValue('prospLegalInd', 'Y');
  FormManager.setValue('enterCMRNo', result.data.cmrNum);
  cmr.importcmr = result.data.cmrNum;
  cmr.importcntry = result.data.issuedBy;
  doImportCmrs();
}

/**
 * Continue importing the record with update type for deactivated CMR
 */
function continueUpdateDeactivatedCMR() {
  var result = cmr.currentCmrResult;
  FormManager.setValue('reqType', 'U');
  FormManager.setValue('func', 'R');
  FormManager.setValue('enterCMRNo', result.data.cmrNum);
  cmr.importcmr = result.data.cmrNum;
  cmr.importcntry = result.data.issuedBy;
  doImportCmrs();
}

/**
 * Continue importing the record with Create type for deactivated CMR
 */
function continueCreateDeactivatedCMR() {
  var result = cmr.currentCmrResult;
  FormManager.setValue('reqType', 'C');
  FormManager.setValue('enterCMRNo', '');
  FormManager.setValue('func', '');
  importCMRs(result.data.cmrNum, result.data.issuedBy, result.data.issuedByDesc);
}

/**
 * Rejected results will save the search result
 */
function forceSaveRequestReject() {
  FormManager.doAction('frmCMR', 'REJECT_SEARCH', true);
}

/**
 * No results will save the search result
 */
function forceSaveRequestNoData() {
  FormManager.doAction('frmCMR', 'NO_DATA_SEARCH', true);
}

/**
 * Called when the direct method to import CMRs is executed. A CMR no must be
 * specified for this
 */
function findAndImportCMRs() {
  var disabled = dojo.byId('enterCMRNo').getAttribute('disabled');
  if (disabled != null) {
    return;
  }
  var cmrNo = FormManager.getActualValue('enterCMRNo');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cmrNo == '' || cntry == '') {
    cmr.showAlert('Please input the both CMR Issuing Country and CMR Number to search for.');
    return;
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SWITZERLAND) {
    var enterCMRNo = FormManager.getActualValue('enterCMRNo');

    var qParams = {
      ZZKV_CUSNO : enterCMRNo,
      MANDT : cmr.MANDT,
    };

    var result = cmr.query('GET.SWISS.PRIMARY_SOLD_TO_ORD_BLK', qParams);
    if (Object.keys(result).length === 0) {
      cmr.showAlert('This CMR is invalid. There were no active primary SOLD-TO found.');
      return;
    }
  } else if (FormManager.getActualValue('reqType') == 'U' && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GERMANY) {
    var enterCMRNo = FormManager.getActualValue('enterCMRNo');
    var qParams = {
      ZZKV_CUSNO : enterCMRNo,
      MANDT : cmr.MANDT,
    };

    var result = cmr.query('GET.GERMANY.CHECK_SOLD_TO_ACTIVE', qParams);
    if (Object.keys(result).length === 0) {
      cmr.showAlert('This CMR could not be imported. No active SOLD-TO records found.');
      return;
    }
  }

  var hasAccepted = dojo.byId('findCMRResult_txt').innerHTML.trim() == 'Accepted';
  if (hasAccepted) {
    cmr.importcmr = cmrNo;
    cmr.showConfirm('doImportCmrs()',
        'Results from a previous CMR Search have already been accepted for this request. Importing will overwrite existing data and address records. Continue importing the CMR records?', null, null,
        {
          OK : 'Yes',
          CANCEL : 'Cancel'
        });
  } else {
    importCMRs(cmrNo);
  }

}

/**
 * Imports the CMR to the request
 * 
 * @param cmrNo
 * @param cntry
 * @param cntryDesc
 */
function importCMRs(cmrNo, cntry, cntryDesc, addressOnly) {
  cmr.importcmr = cmrNo;
  cmr.importcntry = cntry;
  if (addressOnly) {
    var addrType = cmr.currentCmrResult != null ? cmr.currentCmrResult.data.addressTypeDesc + ' address ' : ' address ';
    var addrSeq = cmr.currentCmrResult != null ? ' with sequence ' + cmr.currentCmrResult.data.addressSeq : ' ';
    cmr.showConfirm('doImportCmrs(true)', 'The ' + addrType + addrSeq + ' from CMR Number ' + cmrNo + ' issued by ' + cntryDesc
        + ' will be imported by the system. The address will be added to the current list. Continue?', null, null, {
      OK : 'Yes',
      CANCEL : 'Cancel'
    });
  } else {
    if (cntryDesc != null) {
      cmr.showConfirm('doImportCmrs()', 'Records with CMR Number ' + cmrNo + ' issued by ' + cntryDesc
          + ' will be imported by the system. The data will replace all the current data on the request. Continue?', null, null, {
        OK : 'Yes',
        CANCEL : 'Cancel'
      });
    } else {
      cmr.showConfirm('doImportCmrs()', 'Records with CMR Number ' + cmrNo + ' will be imported by the system. The data will replace all the current data on the request. Continue?', null, null, {
        OK : 'Yes',
        CANCEL : 'Cancel'
      });
    }
  }
}

/**
 * Imports the D&B results to the CMR
 * 
 * @param result
 */
function importDnb(result) {
  cmr.importdnb = result.data;
  console.log(result.data);
  if (cmr.importdnb.items == null || cmr.importdnb.items.length == 0) {
    cmr.showAlert('No D&B record found.');
    return;
  }
  var hasSoldTo = false;
  if (_pagemodel && _pagemodel.userRole == 'Requester') {
    if (_allAddressData && _allAddressData.length > 0) {
      _allAddressData.forEach(function(addr, i) {
        if (addr.importInd && addr.addrType[0] == 'ZS01') {
          hasSoldTo = true;
        }
      });
    }
  }
  var msg = 'D&B record for ' + cmr.importdnb.items[0].cmrName1Plain + ' will be imported by the system. The extracted data will be added to the current records. Continue?';
  if (hasSoldTo) {
    var msg = 'D&B record for ' + cmr.importdnb.items[0].cmrName1Plain + ' will be imported by the system. The extracted data will REPLACE the current main address on the request. Continue?';
  }
  cmr.showConfirm('doImportDnb()', msg, null, null, {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

/**
 * Imports the D&B results to the CMR for automation
 * 
 * @param result
 */
function autoImportDnb(result) {
  cmr.importdnb = result.data;
  console.log(result.data);
  if (cmr.importdnb.results == null || cmr.importdnb.results.length == 0) {
    cmr.showAlert('No D&B record found.');
    return;
  }
  var hasSoldTo = false;
  if (_pagemodel && _pagemodel.userRole == 'Requester') {
    if (_allAddressData && _allAddressData.length > 0) {
      _allAddressData.forEach(function(addr, i) {
        if (addr.importInd && addr.addrType[0] == 'ZS01') {
          hasSoldTo = true;
        }
      });
    }
  }
  var msg = 'D&B record for ' + cmr.importdnb.items[0].cmrName1Plain + ' will be imported by the system. The extracted data will be added to the current records. Continue?';
  if (hasSoldTo) {
    var msg = 'D&B record for ' + cmr.importdnb.items[0].cmrName1Plain + ' will be imported by the system. The extracted data will REPLACE the current main address on the request. Continue?';
  }
  cmr.showConfirm('doImportDnb()', msg, null, null, {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

/**
 * Actual call to import CMRs
 */
function doImportCmrs(addressOnly) {
  var cmrNo = cmr.importcmr;
  if (cmr.importcntry != null) {
    cntry = cmr.importcntry;
    FormManager.setValue('cmrIssuingCntry', cmr.importcntry == '756' ? '755' : cmr.importcntry);
    cmr.importcntr = null;
  }

  var addrType = '';
  var addrSeq = '';
  var result = cmr.currentCmrResult;
  if (result && result.data.addressType) {
    addrType = result.data.addressType;
    addrSeq = result.data.addressSeq;
  }
  // to not allow records having no primary-sold to record, to be imported (for
  // SWISS)
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SWITZERLAND) {
    var result_valid_cmr = cmr.query('CHECK_VALID_CMRS_U', {
      MANDT : cmr.MANDT,
      ZZKV_CUSNO : cmrNo
    });
    if (result_valid_cmr.ret1 = 0) {
      cmr.showAlert('The cmr ' + cmrNo + ' cannot be imported as it doesn"t have a primary sold-to.Please enter another CMR.');
      return;
    }
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if ('U' == FormManager.getActualValue('reqType') && CNTRY_LIST_INACTIVE_IMPORT_CHECK.includes(cntry)) {
    var inactiveCmr = false;
    // check DB2 status
    var cmrStatus = cmr.query('LD.GET_STATUS', {
      COUNTRY : cntry,
      CMR_NO : cmrNo
    });

    if (cmrStatus.ret1 == 'C') {
      inactiveCmr = true;
    }

    if (inactiveCmr) {
      cmr.showAlert('CMR ' + cmrNo + ' is inactive and cannot be imported');
      return;
    }
  }

  cmr.showProgress('Importing records with CMR Number ' + cmrNo + '.  This process might take a while. Please wait..');
  document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/request/import?addressOnly=' + (addressOnly ? 'true' : 'false') + '&addrType=' + addrType + '&addrSeq=' + addrSeq + '&cmrNum='
      + cmrNo + '&system=cmr' + (cmr.searchIssuingCntry != null ? '&searchIssuingCntry=' + cmr.searchIssuingCntry : '') + (cmr.skipAddress != null ? '&skipAddress=' + cmr.skipAddress : ''));
  document.forms['frmCMR'].submit();
}

/**
 * Actual call to import D&B results
 */
function doImportDnb() {
  var data = cmr.importdnb;
  // quick fix for new field map
  if (data.items && data.items[0] && data.items[0].cmrPhone) {
    data.items[0].cmrCustPhone = data.items[0].cmrPhone;
  }
  if (data.items && data.items[0] && data.items[0].cmrFax) {
    data.items[0].cmrCustFax = data.items[0].cmrFax;
  }
  if (data.items && data.items[0] && data.items[0].cmrDuns) {
    data.items[0].cmrDuns = data.items[0].cmrDuns;
  }
  var json = JSON.stringify(data);
  json = encodeURIComponent(json);
  cmr.showProgress("Importing records for " + data.items[0].cmrName1Plain + ".  <br>This process might take a while. Please wait..");
  document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/request/dnbimport?productString=' + json + '&system=dnb');
  document.forms['frmCMR'].submit();
}

/**
 * Opens the DnB search reject reason for the given request
 * 
 */
function showDNBRejectedModal() {
  cmr.showModal('DNBRejectionInfoModal');
}

/**
 * Opens the CMR search reject reason for the given request
 * 
 */
function showCMRRejectedModal() {
  cmr.showModal('CMRRejectionInfoModal');
}

/**
 * Onload function for the rejection info modal
 */
function CMRRejectionInfoModal_onLoad() {
  // noop
}

/**
 * Onclose function for the rejection info modal
 */
function CMRRejectionInfoModal_onClose() {
  // noop
}

/**
 * Onload function for the rejection info modal
 */
function DNBRejectionInfoModal_onLoad() {
  // noop
}

/**
 * Onclose function for the rejection info modal
 */
function DNBRejectionInfoModal_onClose() {
  // noop

}

/**
 * #1308992 not used. will revise address filtering
 * 
 * @return boolean
 */
function isReqComplianceBeforeSearch(cntry) {
  var currCntry = cntry;
  var reqTypeVal = FormManager.getActualValue('reqType');
  var cusTypeVal = FormManager.getActualValue('custType');
  var reqIdVal = FormManager.getActualValue('reqId');
  /*
   * var qParams = null; var custTypeFrmDb = null; var custTypeValFrmDb = null;
   */

  if (currCntry == SysLoc.BRAZIL && reqTypeVal == 'C') {
    if (Number(reqIdVal) <= 0) {
      cmr.showConfirm('forceSaveRequest()', 'Please save the request first to choose a' + ' <strong>Scenario Type/Subtype</strong> before performing CMR Search. Save request?', null, null, {
        OK : 'Yes',
        CANCEL : 'Cancel'
      });
      return false;
    } else {
      /*
       * qParams = { REQ_ID : reqIdVal }; custTypeFrmDb =
       * cmr.query('GET.CUSTTYPE_VALUE_FORCHECKING', qParams); custTypeValFrmDb =
       * custTypeFrmDb.ret1;
       */
    }
    if (cusTypeVal == '') {
      cmr.showAlert('Please choose a <strong>Scenario Type/Subtype</strong> before performing CMR Search.');
      return false;
    }
  }
  return true;
}
