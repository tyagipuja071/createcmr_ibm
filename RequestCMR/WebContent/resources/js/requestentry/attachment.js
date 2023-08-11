/*
 * File: attachment.js
 * Description: 
 * Contains the functions for the attachment tab
 * 
 */

dojo.require("dojo.io.iframe");

var _attachmode = 'F';
/**
 * Formatter for the Action column of the Attachment list grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function attchActFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var docLink = rowData.docLink[0];
  var link = docLink;
  if (link.indexOf('.zip') > 0) {
    link = link.substring(0, link.lastIndexOf('.'));
  }
  var openBtn = '<input type="button" value="Open" class="cmr-grid-btn" onclick="doOpenFile(\'' + link + '\')">';
  var removeBtn = '<input type="button" value="Remove" class="cmr-grid-btn" onclick="doRemoveFile(\'' + link + '\')">';
  return openBtn + removeBtn;
}

/**
 * Formatter for the Action column of the Attachment list grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function attchActFormatterDL(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var docLink = rowData.docLink[0];
  var link = docLink;
  if (link.indexOf('.zip') > 0) {
    link = link.substring(0, link.lastIndexOf('.'));
  }
  var openBtn = '<input type="button" value="Open" class="cmr-grid-btn" onclick="doOpenFile(\'' + link + '\')">';
  return openBtn;
}

/**
 * Formatter for the File Name column of the Attachment list grid
 * 
 * @param value
 * @param rowIndex
 */
function fileNameFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var cmt = rowData.cmt[0];
  cmt = cmt.replace("'", "\\'");
  var link = value;
  if (link.indexOf('.zip') > 0) {
    link = link.substring(0, link.lastIndexOf('.'));
  }
  var display = link.substring(link.lastIndexOf('/') + 1);
  link = link.replace(/'/gi, '\\\'');

  return '<a title="' + cmt + '" href="javascript: openFieNameDetails(\'' + link + '\')">' + display + '</a>';
}

/**
 * Opens the File Name Details screen
 * 
 * @param FileName
 */
function openFieNameDetails(fileName) {
  var params = {
    REQ_ID : FormManager.getActualValue('reqId'),
    LINK : fileName + '.zip'
  };
  var record = cmr.getRecord('GETATTACHMENT', 'Attachment', params);
  dojo.byId('filename_v').innerHTML = record.id.docLink;
  dojo.byId('docContent_v').innerHTML = record.docContent;
  dojo.byId('docAttachById_v').innerHTML = record.docAttachById;
  dojo.byId('docAttachByNm_v').innerHTML = record.docAttachByNm;
  var ts = moment(new Date(record.attachTs)).format('YYYY-MM-DD');
  dojo.byId('attachOn_v').innerHTML = ts;
  dojo.byId('attachcmt_v').innerHTML = record.cmt;
  cmr.showModal('viewAttachmentModal');
}

/**
 * Removes the file with the given file name and content
 * 
 * @param name
 * @param content
 */
function doRemoveFile(value, content) {
  var link = value;
  if (link.indexOf('.zip') > 0) {
    link = link.substring(0, link.lastIndexOf('.'));
  }
  // dojo.byId('docLink').value = name;
  cmr.removefile = link;
  cmr.showConfirm('actualRemoveFile()', 'Remove <strong>' + link + '</strong> from the Attachment List?');
}

/**
 * Called after confirm, removes the file
 */
function actualRemoveFile() {
  dojo.byId('attachRemoveReqId').value = FormManager.getActualValue('reqId');
  dojo.byId('attachRemoveDocLink').value = cmr.removefile;
  FormManager.doHiddenAction('attachRemoveForm', 'REMOVE_FILE', cmr.CONTEXT_ROOT + '/request/attachment.json', true, refreshAfterRemoveFile);
}

/**
 * Refresh the list after removing a file
 * 
 * @param result
 */
function refreshAfterRemoveFile(result) {
  if (result.success) {
    CmrGrid.refresh('ATTACHMENT_GRID');
    dojo.byId('attachRemoveReqId').value = '';
    dojo.byId('attachRemoveDocLink').value = '';
  }
  cmr.removefile = null;
}
/**
 * Opens the file with the given file name and content
 * 
 * @param name
 * @param content
 */
function doOpenFile(docLink) {
  cmr.showProgress('Downloading file. Please wait...');
  var reqId = FormManager.getActualValue('reqId');
  var token = new Date().getTime();
  FormManager.setValue('attachDlReqId', reqId);
  FormManager.setValue('attachDlDocLink', docLink);
  FormManager.setValue('attachDlTokenId', token);
  document.forms['attachDlForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);
}

/**
 * Shows the add attachment modal
 */
function doAddAttachment() {
  FormManager.resetValidations('filename');
  FormManager.addValidator('filename', Validators.REQUIRED, [ 'File Name' ]);
  _attachmode = 'F';
  dojo.byId('attachmentmode').value = 'F';
  cmr.showNode('attachmentfilename');
  cmr.hideNode('attachmentscreenshot');
  cmr.currentModalId = 'addAttachmentModal';
  MessageMgr.clearMessages(true);
  FormManager.setValue('filename', '');
  FormManager.setValue('docContent', '');
  FormManager.setValue('attach_cmt', '');
  cmr.showModal('addAttachmentModal');
}

/**
 * Adds the attachment to the request
 */
function addAttachment() {

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var typeContent = FormManager.getActualValue('docContent');

  if (cmrIssuingCntry == '796' && (custSubGrp == 'ESOSW' || custSubGrp == 'XESO') && typeContent == 'ESA') {
    ANZshowConfirmEsa()
  } else {
    if (FormManager.validate('frmCMR_addAttachmentModal', true)) {
      cmr.aftertoken = refreshAttachmentGrid;
      cmr.modalmode = true;
      FormManager.doHiddenFileAction('frmCMR_addAttachmentModal', 'ADD_ATTACHMENT', cmr.CONTEXT_ROOT + '/request/addattachment.json', true, 'attachToken', true);
    }
  }

}

/**
 * Refresh the attachments grid after a save
 */
function refreshAttachmentGrid() {
  cmr.hideModal('addAttachmentModal');
  CmrGrid.refresh('ATTACHMENT_GRID');
}

function doAddScreenshot() {
  FormManager.resetValidations('filename');
  _attachmode = 'S';
  dojo.byId('attachmentmode').value = 'S';
  dojo.byId('pasteimg').src = cmr.CONTEXT_ROOT + '/resources/images/no-image.png';
  cmr.hideNode('attachmentfilename');
  cmr.showNode('attachmentscreenshot');
  cmr.currentModalId = 'addAttachmentModal';
  MessageMgr.clearMessages(true);
  FormManager.setValue('filename', '');
  FormManager.setValue('docContent', '');
  FormManager.setValue('attach_cmt', '');
  cmr.showModal('addAttachmentModal');
}

function addScreenshotValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (_attachmode != 'S') {
          return new ValidationResult(null, true);
        }
        var img = dojo.byId('pasteimg');
        if (img && img.src.indexOf('base64') < 0) {
          return new ValidationResult(null, false, 'Please paste an image to upload.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addAttachmentModal');
}
function addAttachmentModal_onLoad() {
  if (_attachmode == 'S') {
    dojo.byId('addAttachmentModalTitle').innerHTML = 'Attach a Screenshot';
  } else {
    dojo.byId('addAttachmentModalTitle').innerHTML = 'Attach a File';
  }
}
function addAttachmentModal_onClose() {
  _attachmode = '';
}

dojo.addOnLoad(function() {
  addScreenshotValidator();
  console.log('adding event listener');
  // document.designMode = 'On';
  document.addEventListener('paste', function(e) {
    if (_attachmode != 'S') {
      console.log('no attach mode');
      return;
    }
    console.log('listening');
    try {
      if (e.clipboardData) {
        var items = e.clipboardData.items;
        for (var i = 0; i < items.length; i++) {
          console.log(items[i].type);
          if (items[i].type != null && items[i].type.indexOf('image') >= 0) {
            var reader = new FileReader();
            reader.onload = function(event) {
              imgBase64 = event.target.result.replace(/^data:image\/(png|jpg);base64,/, "");
              dojo.byId('pasteimg').src = event.target.result;
              dojo.byId('imgContent').innerHTML = event.target.result;
            };
            reader.readAsDataURL(items[i].getAsFile());// Blob to base64
          }
          if (items[i].type != null && items[i].type == 'text/rtf') {
            console.log(items[i]);
            var d = items[i].getAsString(tempCallb);
            console.log(d);
            d = items[i].getAsFile();
            console.log(d);
          }
        }
      } else if (window.clipboardData) {
        console.log(window.clipboardData);
      }
    } catch (e) {
      console.log(e);
    }
  }, false);
});

function tempCallb(input) {
  console.log(input);
}

function exportToPdf() {
  cmr.showProgress('Exporting request details. Please wait...');
  var reqId = FormManager.getActualValue('reqId');
  var token = new Date().getTime();
  FormManager.setValue('pdfReqId', reqId);
  FormManager.setValue('pdfTokenId', token);
  document.forms['frmPDF'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);
}
function ANZshowConfirmEsa() {

  var res = cmr
      .showConfirm(
          'setAnzConfirmYes()',
          'By selecting "I confirm" you are certifying that you are aware of the attachment you are going to save is official document of ESA Enrollment Form to support this CMR creation of particular type, and agree to take the responsibility',
          null, null, {
            CANCEL : 'No',
            OK : 'I Confirm'
          });

}
function setAnzConfirmYes() {
  if (FormManager.validate('frmCMR_addAttachmentModal', true)) {
    cmr.aftertoken = refreshAttachmentGrid;
    cmr.modalmode = true;
    FormManager.doHiddenFileAction('frmCMR_addAttachmentModal', 'ADD_ATTACHMENT', cmr.CONTEXT_ROOT + '/request/addattachment.json', true, 'attachToken', true);
  }
}
