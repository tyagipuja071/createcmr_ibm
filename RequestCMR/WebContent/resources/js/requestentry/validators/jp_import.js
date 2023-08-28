/*
 *  Scripts used for Japan import and general scenario handling
 */

/* CRIS Search Related Script */

var _criswindow = null;
var _crisinterval;

/**
 * Override this function from findcmrsearch.js
 */
function doCMRSearch() {
  continueCRISSearch();
  // var confMsg = 'Do you want to search for records in FindCMR or CRIS?';
  // cmr.showConfirm('continueCMRSearch()', confMsg, null,
  // 'continueCRISSearch()', {
  // OK : 'Search FindCMR',
  // CANCEL : 'Search CRIS'
  // });
}

function continueCMRSearch() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var url = cmr.CONTEXT_ROOT + '/connect?system=cmr&cntry=' + cntry + '&reqId=' + reqId;
  var specs = 'location=no,menubar=no,resizable=no,scrollbars=yes,status=no,toolbar=no,height=' + screen.height + 'px,width=' + screen.width + 'px';
  _findCMRWin = window.open(url, 'win_findCMR', specs, true);
  _findCMROK = false;
  _findMode = 'cmr';
  window.setTimeout('waitForResult()', 1000);
  cmr.showProgress('Waiting for Find CMR Search results..');
}
function continueCRISSearch() {
  cmr.showProgress('Waiting for CRIS Search results..');
  _criswindow = WindowMgr.open('CRIS', 'CRIS', 'crissearch?clear=Y', 1200, 550);
  _crisinterval = window.setInterval('checkCRISWindow()', 1000);
}

function checkCRISWindow() {
  if ((_criswindow == null || _criswindow.closed)) {
    try {
      cmr.hideProgress();
      window.clearInterval(_crisinterval);
    } catch (e) {

    }
    return;
  }
}

function doImportCRISRecord(result) {
  var reqType = FormManager.getActualValue('reqType');
  if (result && result.type) {

    var custType = FormManager.getActualValue('custType');
    if (reqType == 'C') {
      var modalId = 'jpScenarioModalCreate' + result.type;
      cmr.currCRISResult = result;
      cmr.showModal(modalId);
    } else {
      var confMsg = '';
      switch (result.type) {
      case 'A':
        if (custType != 'CEA' && custType != 'AR') {
          cmr.showAlert('Accounts can only be imported when updating an existing Account.');
          return;
        }
        confMsg = 'Account No. ' + result.data.cmrNum + ' will be imported to the request together with the parent Establishment and Company. ';
        confMsg += '<br>Proceed?';
        cmr.showConfirm('continueCRISImport("A", "' + result.data.cmrNum + '")', confMsg, null, null, {
          OK : 'Yes',
          CANCEL : 'No'
        });
        break;
      case 'E':
        if (custType != 'CE') {
          cmr.showAlert('Establishments can only be imported when updating an existing Establishment.');
          return;
        }
        confMsg = 'Establishment No. ' + result.data.cmrNum + ' will be imported to the request together with the parent Company. ';
        confMsg += '<br>Proceed?';
        cmr.showConfirm('continueCRISImport("E", "' + result.data.cmrNum + '")', confMsg, null, null, {
          OK : 'Yes',
          CANCEL : 'No'
        });
        break;
      case 'C':
        if (custType != 'C' && custType != 'CR') {
          cmr.showAlert('Companies can only be imported when updating an existing Company.');
          return;
        }
        confMsg = 'Company No. ' + result.data.cmrNum + ' will be imported to the request. ';
        confMsg += '<br>Proceed?';
        cmr.showConfirm('continueCRISImport("C", "' + result.data.cmrNum + '")', confMsg, null, null, {
          OK : 'Yes',
          CANCEL : 'No'
        });
        break;
      }
    }
  } else if (result && result.nodata) {
    FormManager.setValue('saveRejectScore', 'cmr');
    if (reqType == 'C') {
      cmr.showModal('jpScenarioModalCreateX');
    } else {
      cmr.showAlert('No data were found in the search. The result will be saved.', null, 'forceSaveRequestNoData()');
    }
  } else if (result && result.reject) {
    FormManager.setValue('saveRejectScore', 'cmr');
    FormManager.setValue('findCmrRejCmt', result.comment);
    FormManager.setValue('findCmrRejReason', result.reason);
    cmr.showAlert('You have chosen to reject search results. The request will be saved and the action will be recorded.', 'Warning', 'forceSaveRequestReject()');
  }
}

function proceedWithImport(importType) {
  var radioName = 'jpScenario' + importType;
  var chosen = FormManager.getActualValue(radioName);
  if (chosen == '') {
    cmr.showAlert('Please select one from the list.');
    return;
  }
  FormManager.setValue('custType', chosen);
  if (importType == 'X') {
    var addressOnly = true;
    var addrType = 'X';
    cmr.showProgress('Processing, please wait..');
    document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/request/crisimport?addressOnly=' + addressOnly + '&addrType=' + addrType
        + '&addrSeq=0&cmrNum=&searchIssuingCntry=760&skipAddress=false');
    document.forms['frmCMR'].submit();
  } else {
    var result = cmr.currCRISResult;
    if (result) {
      continueCRISImport(result.type, result.data.cmrNum);
    }
  }
}
function continueCRISImport(type, id) {

  var typeDesc = type == 'A' ? 'Account' : (type == 'E' ? 'Establishment' : 'Company');
  var addressOnly = type == 'E' || type == 'C';
  var addrType = type == 'E' ? 'ZE01' : (type == 'C' ? 'ZC01' : 'ZS01');
  var addrSeq = '1';
  cmr.showProgress('Importing ' + typeDesc + ' record with ' + typeDesc + ' No.' + id + '.  This process might take a while. Please wait..');
  document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/request/crisimport?addressOnly=' + addressOnly + '&addrType=' + addrType + '&addrSeq=' + addrSeq + '&cmrNum=' + id
      + '&system=cmr&searchIssuingCntry=760&skipAddress=false');
  document.forms['frmCMR'].submit();
}

function chooseJapanScenario() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId == '0') {
    var reqType = FormManager.getActualValue('reqType');
    if (reqType == 'C') {
      cmr.showModal('jpScenarioModalCreate');
    } else if (reqType == 'U') {
      cmr.showModal('jpScenarioModalUpdate');
    } else if (reqType == '') {
      window.setTimeout('chooseJapanScenario()', 250);
    }
  }
}

function createRequest() {
  var reqType = FormManager.getActualValue('reqType');
  var val = FormManager.getActualValue('jpScenario' + reqType);
  if (val == '') {
    cmr.showAlert('Please choose what you want to request for.');
    return;
  }
  FormManager.setValue('custType', val);
  FormManager.setValue('yourAction', YourActions.Save);
  doSaveRequest();
}

function initialSaveForCreate() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != '0') {
    return;
  }
  if (!FormManager.isReady() || FilteringDropdown.pending()) {
    window.setTimeout('initialSaveForCreate()', 500);
    return;
  }
  FormManager.setValue('custType', 'CEA');
  FormManager.setValue('yourAction', YourActions.Save);
  doSaveRequest('Please wait while the system initializes the configurations.');
}

function initialJPProcessing() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != '0') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == '') {
    window.setTimeout('initialJPProcessing()', 500);
    return;
  } else if (reqType == 'C') {
    console.log('Showing Create INIT');
    initialSaveForCreate();
  } else {
    console.log('Showing Update INIT');
    chooseJapanScenario();
  }

}
dojo.addOnLoad(function() {
  window.setTimeout('initialJPProcessing()', 500);
});
