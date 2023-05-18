/**
 * Scripts for additional contact information
 */

function newContactInfo() {
  cmr.currentModalAction = 'ADD';
  cmr.currentModalId = 'addEditContactInfoModal';
  cmr.setModalTitle(cmr.currentModalId, 'New Contact Information');
  cmr.currentReqId = FormManager.getActualValue('reqId');
  cmr.currentRequestType = FormManager.getActualValue('reqType');
  cmr.showModal(cmr.currentModalId);
}

function addToContactInfoList() {
  cmr.currentReqId = FormManager.getActualValue('reqId');
  cmr.currentModalId = 'addEditContactInfoModal';
  cmr.currentContactId = FormManager.getActualValue('contactInfoId');
  var contactTypeVal = FormManager.getActualValue('contactType');
  var contactSeqVal = FormManager.getActualValue('contactSeqNum');
  var emailVal = FormManager.getActualValue('contactEmail');
  var contactTypeCount = null;
  var contactCount = null;
  var queryParams = null;
  var email1FrmDB = null;
  cmr.currentCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (FormManager.validate('frmCMR_contactInfoModal', true)) {
    if (contactSeqVal && contactSeqVal.length > 0 && !contactSeqVal.match("^[0]{1}[0]{1}[1-3]{1}")) {
      MessageMgr.showErrorMessage('Invalid <strong>Contact Number Sequence</strong> value.  Only the formats 001, 002, or 003 are allowed.', true);
      return;
    }
    if (cmr.currentCntry == SysLoc.BRAZIL) {
      var email1Val = FormManager.getActualValue('email1');
      if (cmr.currentModalAction == "ADD" || cmr.currentModalAction == "COPY" || cmr.currentModalAction == "UPDATE") {
        if (cmr.currentRequestType == 'C') {
          if (cmr.currentModalAction == "ADD" || cmr.currentModalAction == "COPY") {
            queryParams = {
              REQ_ID : cmr.currentReqId
            };
            contactCount = cmr.query('COUNT_CONTACTINFO.BY_REQID', queryParams);
            queryParams = null;
            if (Number(contactCount.ret1) >= 5) {
              // grid onLoad prevent adding
            }
            queryParams = {
              REQ_ID : cmr.currentReqId,
              TYPE : contactTypeVal
            };
            contactTypeCount = cmr.query('COUNT_CONTACTINFO.BY_TYPE', queryParams);
            queryParams = null;

            // only one record for not EM contact types
            if (Number(contactTypeCount.ret1) >= 1 && (contactTypeVal != 'EM')) {
              MessageMgr.showErrorMessage('Only one record for not <strong>EM</strong> contact types is allowed.', true);
              return;
            }

            // for EM allow only three records
            if (Number(contactTypeCount.ret1) >= 3 && contactTypeVal == 'EM') {
              MessageMgr.showErrorMessage('Only three records for <strong>EM</strong> contact type is allowed.', true);
              return;
            }

            queryParams = {
              REQ_ID : cmr.currentReqId,
            };
            email1FrmDB = cmr.query('FIND_DATA_EMAIL1_FOR_CHECKING', queryParams);
            queryParams = null;

            // EM-001 contact email must be equal to Email1
            if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') == 'EM') {
              if (email1Val == '' || email1Val == null) {
                MessageMgr.showErrorMessage('<strong>Email1</Strong> field is required for adding <strong>EM-001</strong> contact.'
                    + '  <strong>Email</strong> field must be the same with <strong>Email1</strong>.', true);
                return;
              }

              if (email1FrmDB.ret1.toUpperCase() != email1Val.toUpperCase()) {
                // validation will be on la_validation.js for contact info grid
              }
            }
          }// add or copy action

          if (cmr.currentModalAction == 'UPDATE') {
            // EM-001 contact email must be equal to Email1 if Email1 has a
            // value
            if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') == 'EM') {
              if (email1Val.toUpperCase() != emailVal.toUpperCase()) {
                MessageMgr.showErrorMessage('<strong>Email</strong> field must be the same with <strong>Email1</strong>' + ' for contact <strong>EM-001</strong>.', true);
                return;
              }
            }
          } // update action
        }// create req

        if (cmr.currentRequestType == 'U') {
          if (cmr.currentModalAction == 'ADD' || cmr.currentModalAction == 'COPY' || cmr.currentModalAction == 'UPDATE') {
            queryParams = {
              REQ_ID : cmr.currentReqId,
              TYPE : contactTypeVal
            };
            contactTypeCount = cmr.query('COUNT_CONTACTINFO.BY_TYPE', queryParams);
            queryParams = null;

            // only three records per contact type is allowed
            if (Number(contactTypeCount.ret1) >= 3) {
              MessageMgr.showErrorMessage('Contact type <strong>' + contactTypeVal + '</strong> for this request already have three records.', true);
              return;
            }

            queryParams = {
              REQ_ID : cmr.currentReqId,
            };
            email1FrmDB = cmr.query('FIND_DATA_EMAIL1_FOR_CHECKING', queryParams);
            queryParams = null;

            // 001 contact types should be equal to Email1 if Email1 has a value
            if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') != '') {
              if (email1Val != null && email1Val != '') {
                if (email1Val.toUpperCase() != emailVal.toUpperCase()) {
                  MessageMgr.showErrorMessage('<strong>Email1</strong> field has a value and must be the same with' + ' <strong>Email</strong> field for <strong>001</strong> contacts.', true);
                  return;
                }
                if (email1FrmDB.ret1.toUpperCase() != email1Val.toUpperCase()) {
                  // validation will be on la_validation.js for contact info
                  // grid
                }
              }
            }
          } // add or copy action

          if (cmr.currentModalAction == 'UPDATE') {
            // during update for 001 contact types, check if Email1 has a value
            // and compare
            if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') != '') {
              if (email1Val != null && email1Val != '') {
                if (email1Val.toUpperCase() != emailVal.toUpperCase()) {
                  MessageMgr.showErrorMessage('<strong>Email1</strong> field has a value and must be the same with' + ' <strong>Email</strong> field for <strong>001</strong> contacts.', true);
                  return;
                }
              }
            }
          } // update action
        } // update req
      }// add update copy action
    } else if (cmr.currentCntry == SysLoc.PERU) {
      var email1Vall = FormManager.getActualValue('email1');
      // PERU
      if (cmr.currentRequestType == 'C') {
        if (cmr.currentModalAction == "ADD" || cmr.currentModalAction == "COPY") {
          queryParams = {
            REQ_ID : cmr.currentReqId
          };
          contactCount = cmr.query('COUNT_CONTACTINFO.BY_REQID', queryParams);
          queryParams = null;
          // PERU : only one record is allowed
          if (Number(contactCount.ret1) >= 1) {
            if (cmr.currentModalAction == "ADD") {
              MessageMgr.showErrorMessage('Create failed. Only one contact information is allowed.', true);
            } else {
              MessageMgr.showErrorMessage('Copy failed. Only one contact information is allowed.', true);
            }
            return;
          }
        } // add copy action PERU

        if (cmr.currentModalAction == "UPDATE") {
          if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') == 'EM') {
            if (email1Vall.toUpperCase() != emailVal.toUpperCase()) {
              MessageMgr.showErrorMessage('<strong>Email</strong> field must be the same with <strong>Email1</strong>' + ' for contact <strong>EM-001</strong>.', true);
              return;
            }
          }
          // if requester change the contact type
          var dets = cmr.currentContactInfoDetails;
          if (contactTypeVal != dets.ret3 || contactSeqVal != dets.ret7) {
            queryParams = {
              REQ_ID : cmr.currentReqId,
              TYPE : contactTypeVal
            };
            contactTypeCount = cmr.query('COUNT_CONTACTINFO.BY_TYPE', queryParams);
            queryParams = null;
            queryParams = {
              REQ_ID : cmr.currentReqId
            };
            contactCount = cmr.query('COUNT_CONTACTINFO.BY_REQID', queryParams);
            queryParams = null;
            if (Number(contactSeqVal.substring(2)) > 1 || Number(contactCount.ret1) >= 1 || Number(contactTypeCount.ret1 >= 1)) {
              MessageMgr.showErrorMessage('Update failed. Only one contact information is allowed. ' + 'Please revert to original sequence and type.', true);
              return;
            }
          }
        }// update action PERU
      }// create req type
      if (cmr.currentRequestType == 'U') {
        if (cmr.currentModalAction == 'ADD' || cmr.currentModalAction == 'COPY') {

          queryParams = {
            REQ_ID : cmr.currentReqId
          };
          contactCount = cmr.query('COUNT_CONTACTINFO.BY_REQID', queryParams);
          queryParams = null;

          // PERU : only one record is allowed
          if (Number(contactCount.ret1) >= 1) {
            if (cmr.currentModalAction == "ADD") {
              MessageMgr.showErrorMessage('Create failed. Only one contact information is allowed.', true);
            } else {
              MessageMgr.showErrorMessage('Copy failed. Only one contact information is allowed.', true);
            }
            return;
          }

          queryParams = {
            REQ_ID : cmr.currentReqId,
          };
          email1FrmDB = cmr.query('FIND_DATA_EMAIL1_FOR_CHECKING', queryParams);
          queryParams = null;

          if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') != '') {
            if (email1Vall != null && email1Vall != '') {
              if (email1Vall.toUpperCase() != emailVal.toUpperCase()) {
                MessageMgr.showErrorMessage('<strong>Email1</strong> field has a value and must be the same with' + ' <strong>Email</strong> field for <strong>001</strong> contacts.', true);
                return;
              }
            }
          }
        } // add or copy action

        if (cmr.currentModalAction == 'UPDATE') {
          if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') != '') {
            if (email1Vall != null && email1Vall != '') {
              if (email1Vall.toUpperCase() != emailVal.toUpperCase()) {
                MessageMgr.showErrorMessage('<strong>Email1</strong> field has a value and must be the same with' + ' <strong>Email</strong> field for <strong>001</strong> contacts.', true);
                return;
              }
            }
          }
          var detss = cmr.currentContactInfoDetails;
          if (contactTypeVal != detss.ret3 || contactSeqVal != detss.ret7) {
            queryParams = {
              REQ_ID : cmr.currentReqId,
              TYPE : contactTypeVal
            };
            contactTypeCount = cmr.query('COUNT_CONTACTINFO.BY_TYPE', queryParams);
            queryParams = null;
            queryParams = {
              REQ_ID : cmr.currentReqId
            };
            contactCount = cmr.query('COUNT_CONTACTINFO.BY_REQID', queryParams);
            queryParams = null;
            if (Number(contactSeqVal.substring(2)) > 1 || Number(contactCount.ret1) >= 1 || Number(contactTypeCount.ret1 >= 1)) {
              MessageMgr.showErrorMessage('Update failed. Only one contact information is allowed. ' + 'Please revert to original sequence and type.', true);
              return;
            }
          }
        } // update action
      }// update req type
    } else {
      // other SSA/MX : email1 is not needed (hidden)
      if (cmr.currentModalAction == "ADD" || cmr.currentModalAction == "COPY") {
        if (cmr.currentRequestType == 'U' || cmr.currentRequestType == 'C') {
          queryParams = {
            REQ_ID : cmr.currentReqId,
            TYPE : contactTypeVal
          };
          contactTypeCount = cmr.query('COUNT_CONTACTINFO.BY_TYPE', queryParams);
          queryParams = null;
          if (Number(contactTypeCount.ret1) >= 3) {
            MessageMgr.showErrorMessage('Contact type <strong>' + contactTypeVal + '</strong> for this request already have three records.', true);
            return;
          }
          /*
           * queryParams = { REQ_ID : cmr.currentReqId, }; email1FrmDB =
           * cmr.query('FIND_DATA_EMAIL1_FOR_CHECKING', queryParams); if
           * (email1FrmDB.ret1.toUpperCase() != email1Val.toUpperCase()) { //
           * validation will be on la_validation.js for contact info // grid }
           * queryParams = null;
           */
        } // create update request types
      } // add copy action

      if (cmr.currentModalAction == 'UPDATE') {
        if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') != '') {
          /*
           * if (email1Val != null && email1Val != '') { if
           * (email1Val.toUpperCase() != emailVal.toUpperCase()) {
           * MessageMgr.showErrorMessage('<strong>Email1</strong> field has a
           * value and must be the same with' + ' <strong>Email</strong> field
           * for <strong>001</strong> contacts.', true); return; } }
           */
        }

        // if requester change the contact type
        var detsss = cmr.currentContactInfoDetails;
        if (contactTypeVal != detsss.ret3 || contactSeqVal != detsss.ret7) {
          queryParams = {
            REQ_ID : cmr.currentReqId,
            TYPE : contactTypeVal
          };
          contactTypeCount = cmr.query('COUNT_CONTACTINFO.BY_TYPE', queryParams);
          queryParams = null;
          // queryParams = { REQ_ID : cmr.currentReqId };
          // contactCount = cmr.query('COUNT_CONTACTINFO.BY_REQID',
          // queryParams);
          // queryParams = null;
          if (Number(contactSeqVal.substring(2)) > 3 && Number(contactTypeCount.ret1 >= 3)) {
            MessageMgr.showErrorMessage('Update failed. Only three contact information is allowed for contact type <strong>' + contactTypeVal + '</strong>. '
                + 'Please revert to original sequence and type.', true);
            return;
          }
        }
      } // update action

      // Defect 1619113: MX: default values should be pre-defined for LE contact
      // type when sending to CROS
      if (cmr.currentCntry == SysLoc.MEXICO && FormManager.getActualValue("contactType") == "LE") {

        if (FormManager.getActualValue("contactPhone") == "") {
          FormManager.setValue("contactPhone", ".");
        }

        if (FormManager.getActualValue("contactName") == "") {
          FormManager.setValue("contactName", "n");
        }
      }

    }
    actualAddToContactList();
  } // validate
} // func end

function actualAddToContactList() {
  if (cmr.currentModalAction == 'ADD' || cmr.currentModalAction == 'COPY') {
    FormManager.doHiddenAction('frmCMR_contactInfoModal', 'ADD_CONTACTINFO', cmr.CONTEXT_ROOT + '/request/contactinfo/process.json?reqId=' + cmr.currentReqId, true, refreshContactInfoGrid, true);
  } else if (cmr.currentModalAction == 'UPDATE') {
    FormManager.doHiddenAction('frmCMR_contactInfoModal', 'UPDATE_CONTACTINFO', cmr.CONTEXT_ROOT + '/request/contactinfo/process.json?reqId=' + cmr.currentReqId, true, refreshContactInfoGrid, true);
  }
}

function refreshContactInfoGrid(result) {
  if (result.success) {
    CmrGrid.refresh('CONTACTINFO_GRID');
    if (cmr.currentModalAction == 'ADD' || cmr.currentModalAction == 'COPY' || cmr.currentModalAction == 'UPDATE') {
      cmr.noCreatePop = 'N';
      cmr.hideModal(cmr.currentModalId);
    }
  } else {
    dojo.query('#dialog_addEditContactInfoModal div.dijitDialogPaneContent')[0].scrollTo(0, 0);
    console.log(result.message);
  }
}

cmr.noCreatePop = 'N';

function addEditContactInfoModal_onLoad() {
  var contactInfoGrid = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID;
  cmr.currentCntry = FormManager.getActualValue('cmrIssuingCntry');

  /* just adding reference to the model */
  if (cmr.currentCntry == SysLoc.BRAZIL || cmr.currentCntry == SysLoc.PERU) {
    FormManager.setValue('currentEmail1', FormManager.getActualValue('email1'));
  }

  if (GEOHandler.getUserRole == 'Requester' && FormManager.getActualValue('reqType') == 'U') {
    // NO ACTION
  }

  if (cmr.currentModalAction == 'ADD') {
    if (contactInfoGrid) {
      if (contactInfoGrid.store._arrayOfAllItems.length == 0) {
        // NO ACTION
      }
    }
    FormManager.setValue('contactType', 'EM');
    FormManager.setValue('contactSeqNum', '');
    FormManager.setValue('contactEmail', '');
    FormManager.setValue('contactName', '');
    FormManager.setValue('contactPhone', '');
    FormManager.setValue('contactInfoId', 0);
    cmr.noCreatePop = 'Y';
  } // add action

  if (cmr.currentModalAction == 'COPY' || cmr.currentModalAction == 'UPDATE') {
    cmr.noCreatePop = 'Y';
    var retrievedDetails = cmr.currentContactInfoDetails;
    FormManager.setValue('contactName', retrievedDetails.ret4);
    FormManager.setValue('contactPhone', retrievedDetails.ret5);
    FormManager.setValue('contactEmail', retrievedDetails.ret6);
    FormManager.setValue('contactType', retrievedDetails.ret3);
    FormManager.setValue('contactTreatment', retrievedDetails.ret8);
    FormManager.setValue('contactFunc', retrievedDetails.ret9);
    if (cmr.currentModalAction == 'COPY') {
      FormManager.setValue('contactInfoId', 0);
      FormManager.setValue('contactSeqNum', '');
    }
    if (cmr.currentModalAction == 'UPDATE') {
      FormManager.setValue('contactInfoId', retrievedDetails.ret2);
      FormManager.setValue('contactSeqNum', retrievedDetails.ret7);

      /*
       * for auto created contactInfos in case email1 is required or user inputs
       * a value on email1 while it is optional
       */
      /*
       * if(cmr.currentCntry != SysLoc.BRAZIL && cmr.currentCntry !=
       * SysLoc.PERU) { var currEmail1 =
       * FormManager.getActualValue('currentEmail1'); if( retrievedDetails.ret7 ==
       * '001') {
       * 
       * if(currEmail1.toUpperCase() != retrievedDetails.ret6.toUpperCase()) {
       * FormManager.setValue('contactEmail',
       * FormManager.getActualValue('currentEmail1')); } } }
       */
    }
  }
  addContactTypeListener();
}

function contactTypeFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var type = rowData.contactType;
  var id = rowData.contactInfoId;
  return '<a href="javascript: showContactInformation(\'' + reqId + '\', \'' + type + '\',  \'' + id + '\')">' + value + '</a>';
}

function showContactInformation(reqId, type, contactId) {
  var queryParam = {
    REQ_ID : reqId,
    ID : contactId,
  };

  cmr.fromDetailsModal = true;

  var queryResult = cmr.query('FIND_CONTACTINFO_DETAILS', queryParam);
  if (queryResult) {
    cmr.currentContactInfoDetails = queryResult;
  }
  cmr.showModal('contactInfoDetailsModal');
}

function updateContactInfoFromDetails() {
  var reqId = dojo.query('#contactInfoDetailsModal #reqId_view')[0].innerHTML;
  var id = dojo.query('#contactInfoDetailsModal #contactInfoId_view')[0].innerHTML;
  var type = dojo.query('#contactInfoDetailsModal #contactType_view').innerHTML;
  var seqNum = dojo.query('#contactInfoDetailsModal #contactSeqNumber_view')[0].innerHTML;

  cmr.hideModal('contactInfoDetailsModal');
  updateContactInfo(reqId, type, id, seqNum);
}

function contactInfoDetailsModal_onLoad() {
  var currentRecord = cmr.currentContactInfoDetails;
  dojo.query('#contactInfoDetailsModal #reqId_view')[0].innerHTML = currentRecord.ret1;
  dojo.query('#contactInfoDetailsModal #contactInfoId_view')[0].innerHTML = currentRecord.ret2;
  dojo.query('#contactInfoDetailsModal #contactType_view')[0].innerHTML = currentRecord.ret3;
  dojo.query('#contactInfoDetailsModal #contactName_view')[0].innerHTML = currentRecord.ret4;
  dojo.query('#contactInfoDetailsModal #contactPhone_view')[0].innerHTML = currentRecord.ret5;
  dojo.query('#contactInfoDetailsModal #contactEmail_view')[0].innerHTML = currentRecord.ret6;
  dojo.query('#contactInfoDetailsModal #contactSeqNumber_view')[0].innerHTML = currentRecord.ret7;
  dojo.query('#contactInfoDetailsModal #contactTreatment_view')[0].innerHTML = currentRecord.ret8;
  dojo.query('#contactInfoDetailsModal #contactFunc_view')[0].innerHTML = currentRecord.ret9;
}

function contactInfoActionFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var type = rowData.contactType;
  var contactId = rowData.contactInfoId;
  var seqNum = rowData.contactSeqNum;
  var actions = '';
  actions += '<input type="button" value="Copy" class="cmr-grid-btn"' + 'onclick="copyContactInfo(\'' + reqId + '\', \'' + type + '\', \'' + contactId + '\', \'' + seqNum
      + '\')" id="copyContactInfoBtn" name="copyContactInfoBtn" />';
  actions += '<input type="button" value="Update" class="cmr-grid-btn"' + 'onclick="updateContactInfo(\'' + reqId + '\', \'' + type + '\', \'' + contactId + '\', \'' + seqNum
      + '\')" id="updateContactInfoBtn" name="updateContactInfoBtn" />';

  if (type != 'EM' && type != 'CF' && type != 'LE') {
    actions += '<input type="button" value="Remove" class="cmr-grid-btn"' + 'onclick="removeContactInfo(\'' + reqId + '\', \'' + type + '\', \'' + contactId + '\', \'' + seqNum
        + '\')" id="removeContactInfoBtn" name="removeContactInfoBtn">';
  } else if (seqNum != '001') {
    actions += '<input type="button" value="Remove" class="cmr-grid-btn"' + 'onclick="removeContactInfo(\'' + reqId + '\', \'' + type + '\', \'' + contactId + '\', \'' + seqNum
        + '\')" id="removeContactInfoBtn" name="removeContactInfoBtn">';
  }

  return actions;
}

function copyContactInfo(reqId, contactType, contactId, seqNum) {
  cmr.currentModalAction = "COPY";
  cmr.currentModalId = 'addEditContactInfoModal';
  cmr.setModalTitle('addEditContactInfoModal', 'Copy Contact Information');
  cmr.currentRequestType = FormManager.getActualValue('reqType');
  cmr.currentReqId = reqId;

  var queryParam = {
    REQ_ID : reqId,
    ID : contactId,
  };
  var queryResult = cmr.query('FIND_CONTACTINFO_DETAILS', queryParam);
  if (queryResult) {
    cmr.currentContactInfoDetails = queryResult;
  }
  cmr.showModal(cmr.currentModalId);
}

function updateContactInfo(reqId, contactType, contactId, seqNum) {
  cmr.currentModalAction = "UPDATE";
  cmr.currentModalId = 'addEditContactInfoModal';
  cmr.currentContactInfoDetails = null;
  cmr.currentRequestType = FormManager.getActualValue('reqType');
  cmr.currentReqId = reqId;

  var queryParam = {
    REQ_ID : reqId,
    ID : contactId,
  };

  var queryResult = cmr.query('FIND_CONTACTINFO_DETAILS', queryParam);

  if (queryResult) {
    cmr.currentContactInfoDetails = queryResult;
  }
  cmr.setModalTitle(cmr.currentModalId, 'Update Contact Information');
  cmr.showModal(cmr.currentModalId);
}

function removeContactInfo(reqId, contactType, contactId, seqNum) {
  cmr.currentModalId = 'addEditContactInfoModal';
  cmr.currentRequestType = FormManager.getActualValue('reqType');
  cmr.currentModalAction = "REMOVE";
  cmr.currentReqId = reqId;
  cmr.currentCntry = FormManager.getActualValue('cmrIssuingCntry');

  var btnRemoveLbl = {
    OK : 'Remove Contact',
    CANCEL : 'Cancel Remove'
  };

  contactDetailsToRemove = {
    REQ_ID : reqId,
    CONTACT_TYPE : contactType,
    CONTACT_ID : contactId,
    CURRENT_EMAIL1 : FormManager.getActualValue('email1'),
    CONTACT_SEQNUM : seqNum
  };

  if (SysLoc.BRAZIL == cmr.currentCntry || SysLoc.PERU == cmr.currentCntry) {
    if (cmr.currentRequestType == 'C') {
      if (seqNum == '001') {
        cmr.showConfirm('doRemoveContactInfo()', '<strong>WARNING</strong>: All Contact Info with a sequence number of <strong>' + seqNum
            + '</strong> will be removed from this request.<br> Continue removing?', 'Confirm Remove', null, btnRemoveLbl);
      } else {
        cmr.showConfirm('doRemoveContactInfo()', 'Remove contact info <strong>' + contactType + '-' + seqNum + '?', 'Confirm Remove', null, btnRemoveLbl);
      }
    } else {
      cmr.showConfirm('doRemoveContactInfo()', '<strong>WARNING</strong>: The sequence number of <strong>' + contactType + '</strong> contact types' + ' will be adjusted.<br> Continue removing?',
          null, btnRemoveLbl);
    }
  } else {
    cmr.showConfirm('doRemoveContactInfo()', '<strong>WARNING</strong>: The sequence number of <strong>' + contactType + '</strong> contact types' + ' will be adjusted.<br> Continue removing?', null,
        btnRemoveLbl);
  }
}

function doRemoveContactInfo() {
  FormManager.setValue('reqId', contactDetailsToRemove.REQ_ID);
  FormManager.setValue('contactType', contactDetailsToRemove.CONTACT_TYPE);
  FormManager.setValue('contactInfoId', contactDetailsToRemove.CONTACT_ID);
  /* just adding reference to the model */
  FormManager.setValue('currentEmail1', contactDetailsToRemove.CURRENT_EMAIL1);
  FormManager.setValue('contactSeqNum', contactDetailsToRemove.CONTACT_SEQNUM);

  FormManager.doHiddenAction('frmCMR_contactInfoModal', 'REMOVE_CONTACTINFO', cmr.CONTEXT_ROOT + '/request/contactinfo/process.json?reqId=' + cmr.currentReqId, true, refreshContactInfoGrid, false);
  contactDetailsToRemove = null;
}

function hideContactInfoModal() {
  if (cmr.currentModalAction == 'UPDATE') {
    cmr.showConfirm('doHideContactInfoModal()', 'Exit without saving this Contact Info row?');
  } else {
    cmr.showConfirm('doHideContactInfoModal()', 'Exit without saving this New Contact Info?');
  }
}

function doHideContactInfoModal() {
  cmr.hideModal(cmr.currentModalId);
  MessageMgr.clearMessages(true);
}

var contactTypeConnector = null;

function addEditContactInfoModal_onClose() {
  // dojo.disconnect(FormManager.getField('contactType'));
  cmr.noCreatePop = 'N';
  FormManager.clearValue('contactType');
  FormManager.clearValue('contactName');
  FormManager.clearValue('contactPhone');
  FormManager.clearValue('contactEmail');
  FormManager.clearValue('contactSeqNum');
  FormManager.clearValue('contactInfoId');
  FormManager.clearValue('contactTreatment');
  FormManager.clearValue('contactFunc');
  MessageMgr.clearMessages();
  if (contactTypeConnector != null) {
    dojo.disconnect(contactTypeConnector); // kill
    contactTypeConnector = null; // kill
  }
}

function contactInfoDetailsModal_onClose() {

}

function addContactTypeListener() {
  if (contactTypeConnector == null) {
    contactTypeConnector = dojo.connect(FormManager.getField('contactType'), 'onChange', function(value) {
      if (value != null) {
        contactTypeIdentifier(value);
      }
    });
  }
  if (contactTypeConnector != null) {
    contactTypeConnector[0].onChange();
  }
}

function contactTypeIdentifier(contactTypeVal) {
  cmr.currentModalId = 'addEditContactInfoModal';
  MessageMgr.clearMessages(true);
  var contNameLbl = dojo.query('#addEditContactInfoModal #cmr-fld-lbl-ContactName')[0];
  var phoneLbl = dojo.query('#addEditContactInfoModal #cmr-fld-lbl-Phone')[0];
  var emailLbl = dojo.query('#addEditContactInfoModal #cmr-fld-lbl-Email')[0];
  var seqLbl = dojo.query('#addEditContactInfoModal #cmr-fld-lbl-ContactSeqNumber')[0];
  var email1Val = FormManager.getActualValue('currentEmail1');
  cmr.currentCntry = FormManager.getActualValue('cmrIssuingCntry');
  var queryParams = null;

  /* styling styling styling */
  if (contactTypeVal != '' && contactTypeVal != null) {
    contNameLbl.innerHTML = "Contact Name " + contactTypeVal;
    phoneLbl.innerHTML = "Phone Number " + contactTypeVal;
    emailLbl.innerHTML = "Email Address " + contactTypeVal;
    seqLbl.innerHTML = "Contact Number Sequence " + contactTypeVal;
  } else {
    contNameLbl.innerHTML = "Contact Name";
    phoneLbl.innerHTML = "Phone Number";
    emailLbl.innerHTML = "Email Address";
    seqLbl.innerHTML = "Contact Number Sequence";
    FormManager.setValue('contactSeqNum', '');
  }
  if (contactTypeVal != '' && (cmr.currentModalAction == 'ADD' || cmr.currentModalAction == 'COPY')) {
    if (cmr.currentCntry == SysLoc.BRAZIL || cmr.currentCntry == SysLoc.PERU) {
      if (cmr.currentRequestType == 'U') {
        queryParams = {
          REQ_ID : cmr.currentReqId,
          TYPE : contactTypeVal
        };
        generateContactSeq(queryParams);
        if (FormManager.getActualValue('contactSeqNum') == '001' && FormManager.getActualValue('contactType') != '') {
          if (email1Val.toUpperCase() != null && email1Val.toUpperCase() != '') {
            FormManager.setValue('contactEmail', email1Val);
          }
        }
      } // update request type

      if (cmr.currentRequestType == 'C') { // create request type
        queryParams = {
          REQ_ID : cmr.currentReqId,
          TYPE : contactTypeVal,
        };
        generateContactSeq(queryParams, contactTypeVal);
        if (FormManager.getActualValue('contactType') == 'EM') {
          if (FormManager.getActualValue('contactSeqNum') == '001') {
            if (email1Val.toUpperCase() != null && email1Val.toUpperCase() != '') {
              FormManager.setValue('contactEmail', email1Val);
            }
          } else {
            FormManager.setValue('contactEmail', '');
          }
        }
      } // create request type
    } else {
      // other SSAMX
      if (cmr.currentRequestType == 'U' || cmr.currentRequestType == 'C') {
        queryParams = {
          REQ_ID : cmr.currentReqId,
          TYPE : contactTypeVal
        };
        generateContactSeq(queryParams, contactTypeVal);
        /*
         * if (FormManager.getActualValue('contactSeqNum') == '001' &&
         * FormManager.getActualValue('contactType') != '') { if
         * (email1Val.toUpperCase() != null && email1Val.toUpperCase() != '') {
         * FormManager.setValue('contactEmail', email1Val); } }
         */
      } // update request type
    }
  } else if (contactTypeVal != '' && cmr.currentModalAction == 'UPDATE') {
    var det = cmr.currentContactInfoDetails;
    if (contactTypeVal != det.ret3) {
      // contactType changed
      queryParams = {
        REQ_ID : cmr.currentReqId,
        TYPE : contactTypeVal
      };
      generateContactSeq(queryParams, contactTypeVal);
      if (FormManager.getActualValue('contactSeqNum') != det.ret7) {
        // action during validate
      }
    }
  }
}

function generateContactSeq(queryParams, contactTypeVal) {
  console.log("generateContactSeq : processing. . .");
  cmr.currentCntry = FormManager.getActualValue('cmrIssuingCntry');
  var contactTypeCount = cmr.query('COUNT_CONTACTINFO.BY_TYPE', queryParams);
  var countResult = contactTypeCount.ret1;
  if (SysLoc.BRAZIL == cmr.currentCntry || cmr.currentCntry == SysLoc.PERU) {
    if (FormManager.getActualValue('reqType') == 'C') {
      if (contactTypeVal == 'EM') {
        qParams = {
          REQ_ID : cmr.currentReqId,
          TYPE : contactTypeVal.trim(),
          SEQ : '001'
        };
        var EM001_COUNT = cmr.query('COUNT_CONTACTINFO.EM001', qParams);
        qParams = {
          REQ_ID : cmr.currentReqId,
          TYPE : contactTypeVal,
        };
        var MAX_SEQUENCE = cmr.query('CONTACTINFO_MAX_SEQUENCE', qParams);
        /* check for EM sequence */
        if (Number(EM001_COUNT.ret1) <= 0) {
          FormManager.setValue('contactSeqNum', '001');
        } else if (Number(EM001_COUNT.ret1) > 0 && Number(MAX_SEQUENCE.ret1) == 2) {
          FormManager.setValue('contactSeqNum', '003');
        } else if (Number(EM001_COUNT.ret1) > 0 && (Number(MAX_SEQUENCE.ret1) == 3) || Number(MAX_SEQUENCE.ret1) == 1) {
          if (Number(MAX_SEQUENCE.ret1) == Number(countResult)) {
            var countInt = Number(countResult) + 1;
            FormManager.setValue('contactSeqNum', '00' + countInt++);
          } else {
            FormManager.setValue('contactSeqNum', '002');
          }
        } else {
          var countInt = Number(countResult) + 1;
          FormManager.setValue('contactSeqNum', '00' + countInt++);
        }
      } else {
        if (countResult != '' && countResult != null) {
          var countInt = Number(countResult) + 1;
          var countResultStr = '00' + countInt;
          FormManager.setValue('contactSeqNum', countResultStr);
        } else {
          FormManager.setValue('contactSeqNum', '001');
        }
      }
    } else {
      if (countResult != '' && countResult != null) {
        var countInt = Number(countResult) + 1;
        var countResultStr = '00' + countInt;
        FormManager.setValue('contactSeqNum', countResultStr);
      } else {
        FormManager.setValue('contactSeqNum', '001');
      }
    }
  } else {
    // other SSAMX
    if (FormManager.getActualValue('reqType') == 'C' || FormManager.getActualValue('reqType') == 'U') {
      if (countResult != '' && countResult != null) {
        var countInt = Number(countResult) + 1;
        var countResultStr = '00' + countInt;
        FormManager.setValue('contactSeqNum', countResultStr);
      } else {
        FormManager.setValue('contactSeqNum', '001');
      }
    }
  }
}

function removeSelectedContacts() {
  cmr.currentModalAction = 'REMOVE_SELECTED';
  FormManager.gridHiddenAction('frmCMR', 'REMOVE_CONTACTINFOS', cmr.CONTEXT_ROOT + '/request/contactinfo/process.json', true, refreshContactInfoGrid, false, 'Remove selected contact records?');
}

function CONTACTINFO_GRID_GRID_onLoad() {
  var contactInfoGrid = CmrGrid.GRIDS.CONTACTINFO_GRID_GRID;
  var gridLength = null;
  cmr.currentCntry = FormManager.getActualValue('cmrIssuingCntry');
  cmr.currentRequestType = FormManager.getActualValue('reqType');
  if (typeof (contactInfoGrid) != 'undefined') {
    gridLength = contactInfoGrid.store._arrayOfAllItems.length;
    if (cmr.currentCntry == SysLoc.BRAZIL) {
      // only three records by contact type for BR(631)
      if (cmr.currentRequestType == 'U') {
        if (Number(gridLength) >= 9) {
          cmr.hideNode('addContactInfoBtn');
        } else {
          cmr.showNode('addContactInfoBtn');
        }
      }
      // three records for contact type 'EM' for BR(631)
      if (cmr.currentRequestType == 'C') {
        if (Number(gridLength) >= 5) {
          cmr.hideNode('addContactInfoBtn');
        } else {
          cmr.showNode('addContactInfoBtn');
        }
      }
    } else if (cmr.currentCntry == SysLoc.PERU) {
      if (cmr.currentRequestType == 'U' || cmr.currentRequestType == 'C') {
        if (Number(gridLength) >= 1) {
          cmr.hideNode('addContactInfoBtn');
        } else {
          cmr.showNode('addContactInfoBtn');
        }
      }
    } else {
      if (cmr.currentRequestType == 'C' || cmr.currentRequestType == 'U') {
        if (Number(gridLength) >= 6) { // 001 002 003 -> LE EM
          cmr.hideNode('addContactInfoBtn');
        } else {
          cmr.showNode('addContactInfoBtn');
        }
      }
    }
  }
}
