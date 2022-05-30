/*
 * Javascript for changelog pages
 */

/**
 * Does the search for change logs
 */
function doSearchChangeLogs() {
  var requestIdStr = FormManager.getActualValue('requestIdStr');
  var userId = FormManager.getActualValue('userId');
  var tablName = FormManager.getActualValue('tablName');
  var changeDateFrom = FormManager.getActualValue('changeDateFrom');
  var changeDateTo = FormManager.getActualValue('changeDateTo');
  var cmrNo = FormManager.getActualValue('cmrNo');
  var requestStatus = FormManager.getActualValue('requestStatus');
  var cmrIssuingCountry = FormManager.getActualValue('cmrIssuingCountry');

  if (requestIdStr == '' && userId == '' && tablName == '' && changeDateFrom == '' && changeDateTo == '' && cmrNo == '' && requestStatus == ''
      && cmrIssuingCountry == '') {
    cmr.showAlert('Please specify at least 1 search parameter.');
    return;
  }

  CmrGrid.refresh('changeLogGrid', cmr.CONTEXT_ROOT + '/changeloglist.json', 'requestIdStr=:requestIdStr&userId=:userId&tablName=:tablName'
      + '&changeDateFrom=:changeDateFrom&changeDateTo=:changeDateTo' + '&loadRec=Y&cmrNo=:cmrNo' + '&requestStatus=:requestStatus&'
      + 'cmrIssuingCountry=:cmrIssuingCountry');
}

function doDownloadExportFullReport() {
  var cmrNo = FormManager.getActualValue('cmrNo');

  if (cmrNo == '') {
    cmr.showAlert('CMR No is required.');
    return;
  }

  var cmrIssuingCountry = FormManager.getActualValue('cmrIssuingCountry');

  if (cmrIssuingCountry == '') {
    cmr.showAlert('CMR Issuing Country is required.');
    return;
  }

  cmr.showAlert('Please wait for the download prompt of the report file.', null, null, true);

  $("#katr6").val(cmrIssuingCountry.split('-')[1].trim());
  $("#zzkvCusNo").val(cmrNo);
  document.forms['frmCMRFullReportDownLoad'].action = cmr.CONTEXT_ROOT + "/changelog/exportFullReport";
  document.forms['frmCMRFullReportDownLoad'].target = "exportFrame";
  document.forms['frmCMRFullReportDownLoad'].submit();
}

function reqIdFormatter(value, rowIndex) {
  return '<a href="javascript: window.location = \'' + cmr.CONTEXT_ROOT + '/request/' + value + '\'">' + value + '</a>';
}