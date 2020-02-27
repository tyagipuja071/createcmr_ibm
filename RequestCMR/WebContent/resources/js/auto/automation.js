/**
 * javascript functions for CreateCMR2.0 - automation
 */

var Automation = (function() {

  return {
    viewResults : function() {
      var reqId = FormManager.getActualValue('reqId');
      console.log('viewing results for ' + reqId);
      WindowMgr.open('AUTO_RESULTS', reqId, cmr.CONTEXT_ROOT + '/auto/results/' + reqId + '?reqId=' + reqId, null, 550, true);
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
      switch (type) {
      case 'D':
        var oImport = rowData.overrideImport[0];
        if (oImport == 'Y') {
          return '<strong>Data Imported</strong>';
        } else if (oImport == 'N') {
          return '<input type="button" class="cmr-grid-btn" value="Import Data" onClick="importData(\'' + rowData.reqId + '\', \'' + rowData.automationResultId + '\', \'' + rowData.processCd
              + '\')">';
        } else {
          return '';
        }
      case 'M':
        var mImport = rowData.matchImport[0];
        var failIndc = rowData.failureIndc[0];
        if (failIndc == 'P' || failIndc == 'S') {
          return '';
        }
        if (mImport == 'Y') {
          var val = '<strong>Match(es) Imported</strong>';
          val += '<br><input type="button" class="cmr-grid-btn" value="View Matches" onClick="viewMatches(\'' + rowData.reqId + '\', \'' + rowData.automationResultId + '\', \'' + rowData.processCd
              + '\', \'' + rowData.processDesc + '\')">';
          return val;
        } else if (mImport == 'N') {
          return '<input type="button" class="cmr-grid-btn" value="View Matches" onClick="viewMatches(\'' + rowData.reqId + '\', \'' + rowData.automationResultId + '\', \'' + rowData.processCd
          + '\', \'' + rowData.processDesc + '\')">';
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

      return '<input type="button" class="cmr-grid-btn" value="Notify" onClick="addToNotifList(\'' + rowData.reqId + '\')">';
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
  if (oImport == 'Y') {
    return '<strong>Match imported</strong>';
  } else if (oImport == 'N') {
    return '<input type="button" class="cmr-grid-btn" value="Import Match" onClick="importMatch(\'' + rowData.automationResultId + '\', \'' + rowData.processCd + '\', \'' + rowData.itemNo + '\', \''
        + rowData.requestId + '\')">';
  } else {
    return '';
  }
}

function matchDetailsFormatter(value, rowIndex) {
  return value.replace(/\\n/gim, '<br>');
}

function importMatch(autoResId, procCd, itemNo, reqId) {
  if (confirm('Importing Item No. '+itemNo+' to the request. The request details page\nwill be refreshed and all non-saved items will be lost.\nContinue with import?')) {
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
          cmr.showAlert('Request updated with information from Item No. ' + itemNo, 'Success',
              'hideModal_Window()', true);
          //reload parent window
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
        cmr.showAlert('The record with  Automation Result ID: ' + autoResId + ' and  Process Code: ' + procCd + ' and  has been imported sucessfully.', 'Success', null, true);
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
