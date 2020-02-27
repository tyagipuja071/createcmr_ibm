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
  if (requestIdStr == '' && userId == '' && tablName == '' && changeDateFrom == '' && changeDateTo == '' && cmrNo == '') {
    cmr.showAlert('Please specify at least 1 search parameter.');
    return;
  }

  CmrGrid.refresh('changeLogGrid', cmr.CONTEXT_ROOT + '/changeloglist.json', 'requestIdStr=:requestIdStr&userId=:userId&tablName=:tablName'
      + '&changeDateFrom=:changeDateFrom&changeDateTo=:changeDateTo' + '&loadRec=Y&cmrNo=:cmrNo');
}

function reqIdFormatter(value, rowIndex) {
  return '<a href="javascript: window.location = \'' + cmr.CONTEXT_ROOT + '/request/' + value + '\'">' + value + '</a>';
}