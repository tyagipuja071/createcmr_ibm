/**
 * javascript functions for CreateCMR2.0 - automation
 */

var Automation = (function() {

  return {
    viewResults : function() {
      var reqId = FormManager.getActualValue('reqId');
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      console.log('viewing results for ' + reqId);
      WindowMgr.open('AUTO_RESULTS', reqId, cmr.CONTEXT_ROOT + '/auto/results/' + reqId + '?cntry=' + cntry + '&reqId=' + reqId, null, 550, true);
    },
    getParameterByName : function(name, url) {
      if (!url) {
        url = window.location.href;
      }
      name = name.replace(/[\[\]]/g, '\\$&');
      var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
      var results = regex.exec(url);
      if (!results) {
        return null;
      }
      if (!results[2]) {
        return '';
      }
      return decodeURIComponent(results[2].replace(/\+/g, ' '));
    },
    detailsFormatter : function(value, rowIndex) {
      return value.replace(/\n/gi, '<br>');
    },
    typeFormatter : function(value, rowIndex) {
      switch (value) {
      case 'D':
        return 'Data Override';
      case 'M':
        return 'Matching';
      case 'V':
        return 'Validation';
      case 'A':
        return 'Approvals';
      case 'S':
        return 'Standard Process';
      }

    },
    failFormatter : function(value, rowIndex) {
      if (value == 'P') {
        return '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/warn-icon.png" class="cmr-grid-check" title="Processing Failure" style="cursor:help">';
      }
      if (value == 'S') {
        return '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/error-icon.png" class="cmr-grid-check" title="System Failure" style="cursor:help">';
      }
      return '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/approve.png" class="cmr-grid-check" title="Success" style="cursor:help">';
    },
    actionsFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var type = rowData.processTyp[0];
      console.log('type ' + type);
      switch (type) {
      case 'D':
        var oImport = rowData.overrideImport[0];
        if (oImport == 'Y') {
          return '<span style="font-size:11px;font-weight:bold">Data Imported</span>';
        } else if (oImport == 'N') {
          return '<input type="button" class="cmr-grid-btn-h" style="font-size:11px" value="Import Data" onClick="importData(\'' + rowData.reqId + '\', \'' + rowData.automationResultId + '\', \''
              + rowData.processCd + '\')">';
        } else {
          return '';
        }
      case 'M':
        var mImport = rowData.matchImport[0];
        var failIndc = rowData.failureIndc[0];
        // if (failIndc == 'P' || failIndc == 'S') {
        // return '';
        // }
        if (mImport == 'Y') {
          var val = '<span style="font-size:11px;font-weight:bold">Match(es) Imported</span>';
          val += '<br><input type="button" class="cmr-grid-btn-h" style="font-size:11px" value="View Matches" onClick="viewMatches(\'' + rowData.reqId + '\', \'' + rowData.automationResultId
              + '\', \'' + rowData.processCd + '\', \'' + rowData.processDesc + '\')">';
          return val;
        } else if (mImport == 'N') {
          return '<input type="button" class="cmr-grid-btn-h"  style="font-size:11px" value="View Matches" onClick="viewMatches(\'' + rowData.reqId + '\', \'' + rowData.automationResultId + '\', \''
              + rowData.processCd + '\', \'' + rowData.processDesc + '\')">';
        } else {
          return '';
        }
      }
      return '';
    },
    goToOldRequestPage : function() {
      var cntry = _pagemodel.cmrIssuingCntry;
      var reqType = _pagemodel.reqType;
      window.location = './request?create=Y&newReqCntry=' + cntry + '&newReqType=' + reqType + '&_f=Y';
    },
    createRequest : function(formName) {
      if (!FormManager.validate(formName, null, false)) {
        return;
      }
      if (typeof (createRequestForCountry) != 'undefined') {
        createRequestForCountry(formName);
      } else {
        this.submitForCreation(formName);
      }
    },
    submitForCreation : function(formName) {
      console.log(dojo.formToObject(formName));
      cmr.showProgress('Saving request data..');
      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/auto/process.json',
        handleAs : 'json',
        method : 'POST',
        content : dojo.formToObject(formName),
        timeout : 120000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          console.log(data);
          if (!data.success) {
            cmr.showAlert(data.error, 'Error');
          } else {
            cmr.showAlert('Request created with ID ' + data.reqId + ' and has been sent for processing.', 'Success', 'Automation.redirectToWorkflow()', true);
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          cmr.showAlert('An error occurred while saving the request. Please contact your system administrator', 'Error');
        }
      });
    },
    redirectToWorkflow : function() {
      window.location = './workflow/open';
    },
    notifyActionFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);

      return '<input type="button" class="cmr-grid-btn-h" style="font-size:11px" value="Notify" onClick="addToNotifList(\'' + rowData.reqId + '\')">';
    }
  };
})();

function viewMatches(requestId, autoResId, processCd, processNm) {
  console.log('viewing matches for requestID' + requestId + " and Result Id -> " + autoResId);
  CmrGrid.refresh('autoResults_match_importGrid', cmr.CONTEXT_ROOT + '/auto/results/matches/import/matching_list.json', 'processCd=' + processCd + '&automationResultId=' + autoResId + '&requestId='
      + requestId);
  cmr.showModal('result_match_modal');

}

function matchImportFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var oImport = rowData.importedIndc[0];
  var keyValue = rowData.matchKeyValue[0];
  var formattedString = '';
  if (oImport == 'Y') {
    formattedString += '<strong>Match imported</strong><br>';
  } else if (oImport == 'N') {
    formattedString += '<input type="button" class="cmr-grid-btn-h" style="font-size:11px" value="Import Match" onClick="importMatch(\'' + rowData.automationResultId + '\', \'' + rowData.processCd
        + '\', \'' + rowData.itemNo + '\', \'' + rowData.requestId + '\')"><br>';
  } else {
    formattedString += '';
  }
  if (keyValue.indexOf('CMR_NO') >= 0 || keyValue.indexOf('REQ_ID') >= 0 || keyValue.indexOf('DUNS_NO' >= 0)) {
    var val = keyValue.substring(keyValue.indexOf('=') + 1).trim();
    if (keyValue.indexOf('CMR_NO') >= 0) {
      formattedString += '<input type="button" class="cmr-grid-btn" style="font-size:11px" value="View Details" onClick="openCMRDetailsPage(\'' + val + '\')">';
    } else if (keyValue.indexOf('REQ_ID') >= 0) {
      formattedString += '<input type="button" class="cmr-grid-btn" style="font-size:11px" value="View Details" onClick="showSummaryScreenV2(\'' + val + '\',\'C\')">';
    } else if (keyValue.indexOf('DUNS_NO') >= 0) {
      var dnbVal = val.substring(0, val.indexOf('\\n')).trim();
      formattedString += '<input type="button" class="cmr-grid-btn" style="font-size:11px" value="View Details" onClick="openDNBDetailsPage(\'' + dnbVal + '\')">';
    }
    if (formattedString.length > 0) {
      return formattedString;
    } else {
      return '';
    }
  }
}

function openCMRDetailsPage(cmrNo) {
  WindowMgr.open('COMPDET', 'CMR' + cmrNo, 'company_details?viewOnly=Y&issuingCountry=' + Automation.getParameterByName('cntry') + '&cmrNo=' + cmrNo, null, 550);
}

function openDNBDetailsPage(dunsNo) {
  WindowMgr.open('COMPDET', 'DNB' + dunsNo, 'company_details?viewOnly=Y&issuingCountry=' + Automation.getParameterByName('cntry') + '&dunsNo=' + dunsNo, null, 550);
}

function showSummaryScreenV2(requestId, type) {
  if ('C' == type || 'U' == type) {
    WindowMgr.open('SUMMARY', requestId, 'summary?reqId=' + requestId + '&reqType=' + type);
  } else {
    cmr.showAlert('Request Type has not been specified yet.');
  }
}

function matchDetailsFormatter(value, rowIndex) {
  return value.replace(/\\n/gim, '<br>');
}

function importMatch(autoResId, procCd, itemNo, reqId) {
  if (confirm('Importing Item No. ' + itemNo + ' to the request. The request details page\nwill be refreshed and all non-saved items will be lost.\nContinue with import?')) {
    cmr.showProgress('Importing record into request..');
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/auto/results/matches/importrecord.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        'automationResultId' : autoResId,
        'processCd' : procCd,
        'itemNo' : itemNo,
        'requestId' : reqId
      },
      timeout : 50000,
      sync : false,
      load : function(data, ioargs) {
        cmr.hideProgress();
        console.log(data);
        if (!data.success) {
          cmr.showAlert(data.error, 'Error');
        } else {
          CmrGrid.refresh('autoResults_match_importGrid', cmr.CONTEXT_ROOT + '/auto/results/matches/import/matching_list.json', 'processCd=' + procCd + '&automationResultId=' + autoResId
              + '&requestId=' + reqId);
          cmr.showAlert('Request updated with information from Item No. ' + itemNo, 'Success', 'hideModal_Window()', true);
          // reload parent window
          opener.location.reload();
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        cmr.showAlert('An error occurred while importing the record. Please contact your system administrator', 'Error');
      }
    });
  }
}

function importData(reqId, autoResId, procCd) {
  cmr.showProgress('Overrding data into request..');
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/auto/results/data/importdata.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      'automationResultId' : autoResId,
      'processCd' : procCd,
      'requestId' : reqId
    },
    timeout : 50000,
    sync : false,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      if (!data.success) {
        cmr.showAlert(data.error, 'Error');
      } else {
        window.location.reload();

        cmr.showAlert('The record with  Automation Result ID: ' + autoResId + ' and  Process Code: ' + procCd + ' and  has been imported sucessfully.', 'Success', 'hideModal_Window()', true);
        // reload parent window
        opener.location.reload();
      }
    },
    error : function(error, ioargs) {
      cmr.hideProgress();
      cmr.showAlert('An error occurred while overriding the data values. Please contact your system administrator', 'Error');
    }
  });
}
function hideModal_Window() {
  cmr.hideModal('result_match_modal');
}

/* Request Entry Scripts */
