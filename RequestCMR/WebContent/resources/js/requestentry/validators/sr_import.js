/*
 *  Scripts used for Single reactivation import and general scenario handling
 */

var _criswindow = null;
var _crisinterval;

var _sreactivatewindow = null;
var _sreactivateinterval;

function singleReactCMRSearch(cntry, reqId) {

  console.log("singleReactCMRSearch...+cntry:" + cntry + " reqId:" + reqId);
  continueSingleReactSearch(cntry, reqId);
}

function continueSingleReactSearch(cntry, reqId) {
  cmr.showProgress('Waiting for Single Reactive Search results..');
  _sreactivatewindow = WindowMgr.open('SREACT', 'SREACT', 'singlereactsearch?clear=Y&cntry=' + cntry + '&reqId=' + reqId, 1200, 550);
  _sreactivateinterval = window.setInterval('checkSREACTWindow()', 1000);
}

function checkSREACTWindow() {
  if ((_sreactivatewindow == null || _sreactivatewindow.closed)) {
    try {
      cmr.hideProgress();
      window.clearInterval(_sreactivateinterval);
    } catch (e) {

    }
    return;
  }
}
function doImportSREACTRecord(result) {
  var confMsg = 'CMR No. ' + result.data.cmrNum + ' will be imported to the request. ';
  confMsg += '<br>Proceed?';
  cmr.showConfirm('continueSREACTImport("' + result.data.issuedBy + '", "' + result.data.cmrNum + '")', confMsg, null, null, {
    OK : 'Yes',
    CANCEL : 'No'
  });
}

function continueSREACTImport(cmrCntry, cmrNo) {
  console.log("continueSREACTImport cmrCntry>>:" + cmrCntry + "cmrNo:" + cmrNo);
  var addressOnly = true;
  var addrType = 'ZS01';
  // Import in Progress....
  cmr.showProgress('Importing  Customer record with CMR No.' + cmrNo + '. This process might take a while. Please wait..');
  document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/request/singlereactimport?addressOnly=' + addressOnly + '&cmrNum=' + cmrNo + '&addrType=' + addrType
      + '&addrSeq=1&system=cmr&searchIssuingCntry=' + cmrCntry + '&skipAddress=false');
  document.forms['frmCMR'].submit();
}
function initialSREACTProcessing() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != '0') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == '') {
    window.setTimeout('initialSREACTProcessing()', 500);
    return;
  }

}
dojo.addOnLoad(function() {
  window.setTimeout('initialSREACTProcessing()', 500);
});
