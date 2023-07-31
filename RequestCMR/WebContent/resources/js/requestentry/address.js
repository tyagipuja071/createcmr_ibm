/*
 * File: address.js
 * Description: 
 * Contains the functions related to address modal functions.
 * This file does not include TGME Address Standardization scripts
 * 
 */

var _allAddressData = null;
var currentIndc = null;

/**
 * Formats the Address type
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function addressTypeFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var addrType = rowData.addrType;
  var addrSeq = rowData.addrSeq;
  var mandt = FormManager.getActualValue('mandt');
  return '<a href="javascript: openAddressDetails(\'' + reqId + '\', \'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt + '\')">' + value + '</a>';
}

function addrUpdateIndFormatter(value, rowIndex) {
  switch (value) {
  case 'U':
    return '<span style="color:green;font-weight:bold">Updated</span>';
  case 'N':
    return '<span style="color:red;font-weight:bold">New</span>';
  default:
    return '';
  }
}
function addrSeqFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var importInd = rowData.importInd;

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var status = FormManager.getActualValue('reqStatus');
  if (cntry == '' && typeof (_pagemodel) != 'undefined') {
    cntry = _pagemodel.cmrIssuingCntry;
    reqType = _pagemodel.reqType;
  }
  var paired = rowData.pairedSeq && rowData.pairedSeq[0] ? rowData.pairedSeq[0] : '';
  if (cntry == SysLoc.ISRAEL) {
    return paired == '' ? value : value + '<br>(' + paired + ')';
  } else if (cntry == SysLoc.GERMANY || cntry == SysLoc.BAHAMAS || cntry == SysLoc.BARBADOS || cntry == SysLoc.BERMUDA || cntry == SysLoc.CAYMAN_ISLANDS || cntry == SysLoc.GUYANA
      || cntry == SysLoc.JAMAICA || cntry == SysLoc.SAINT_LUCIA || cntry == SysLoc.NETH_ANTILLES || cntry == SysLoc.SURINAME || cntry == SysLoc.TRINIDAD_TOBAGO) {
    if (reqType == 'C' && (status == 'COM' || status == 'PCO')) {
      return paired == '' ? 'N/A' : paired;
    } else if ((reqType == 'U' || reqType == 'X') && importInd == 'Y') {
      return paired == '' ? 'N/A' : paired;
    }
    return value;
  } else {
    return value;
  }
}
/**
 * Formats the Import Indicator column
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function importIndFormatter(value, rowIndex) {
  if ('Y' == value) {
    return 'CMR Search';
  } else if ('D' == value) {
    return 'D&B Search';
  }
  return 'Manual';
}

/**
 * Formatter for address List Actions column
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function addrFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var addrType = rowData.addrType;
  var addrSeq = rowData.addrSeq;
  var name = rowData.custNm1;
  if (name == null) {
    name = '';
  }
  name = name && name.length > 0 && name[0] != null ? name[0].replace(/\'/g, '') : '';
  var addrTypeTxt = rowData.addrTypeText;
  var mandt = FormManager.getActualValue('mandt');
  var actions = '';
  // for non-US, return all options
  if (FormManager.getActualValue('cmrIssuingCntry') != '897') {
    if (FormManager.getActualValue('cmrIssuingCntry') == '755') {
      name = name.replace(/"/g, '&quot;');
    }

    if (canCopyAddress(value, rowIndex, this.grid)) {
      actions += '<input type="button" value="Copy" class="cmr-grid-btn" onclick="doCopyAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt + '\',\'' + name + '\')">';
    }
    if (canUpdateAddress(value, rowIndex, this.grid)) {
      actions += '<input type="button" value="Update" class="cmr-grid-btn" onclick="doUpdateAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt + '\')">';
    }
    if (canRemoveAddress(value, rowIndex, this.grid)) {
      actions += '<input type="button" value="Remove" class="cmr-grid-btn" onclick="doRemoveAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt + '\', \'' + name + '\', \''
          + addrTypeTxt + '\')">';
    }
    return actions;
  }
  if (addrType == 'ZS01' || addrType == 'ZI01' || addrType == 'ZP01') {
    if (canCopyAddress(value, rowIndex, this.grid)) {
      actions += '<input type="button" value="Copy" class="cmr-grid-btn" onclick="doCopyAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt + '\',\'' + name + '\')">';
    }
    if (canUpdateAddress(value, rowIndex, this.grid)) {
      actions += '<input type="button" value="Update" class="cmr-grid-btn" onclick="doUpdateAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt + '\')">';
    }
    if (canRemoveAddress(value, rowIndex, this.grid)) {
      actions += '<input type="button" value="Remove" class="cmr-grid-btn" onclick="doRemoveAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt + '\', \'' + name + '\', \''
          + addrTypeTxt + '\')">';
    }
  }
  return actions;
}

function canUpdateAddress(value, rowIndex, grid) {
  return true;
}
function canCopyAddress(value, rowIndex, grid) {
  return true;
}
function canRemoveAddress(value, rowIndex, grid) {
  return true;
}

/**
 * Formats the DPL scorecard result of an address
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function dplFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var addrType = rowData.addrType;
  var addrSeq = rowData.addrSeq;
  if ('N' == value) {
    return 'Not Required';
  } else if ('P' == value) {
    return 'Passed';
  } else if ('F' == value) {
    return '<a href="javascript: openDplDetails(' + reqId + ', \'' + addrType + '\', \'' + addrSeq + '\')" title="Click to open failure details">Failed</a>';
  }
  return 'Not Done';
}

// Hideing Hardware master for Swiss in address details page
function hideHarwareonSummary() {

  if (openAddressDetails.addrType != 'ZS01' && openAddressDetails.addrType != 'ZI01' && openAddressDetails.addrType != 'ZS02') {
    cmr.hideNode('hardwareMaster');

  } else {
    cmr.showNode('hardwareMaster');

  }

}

/**
 * Opens the Address Details screen (view only)
 * 
 * @param AddrType
 */
function openAddressDetails(reqId, addrType, addrSeq, mandt) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var nordx_cntries = [ '678', '846', '806' ];
  var reqType = FormManager.getActualValue('reqType');
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : addrType,
    ADDR_SEQ : addrSeq,
    MANDT : mandt,
  };
  var result = cmr.query('ADDRDETAIL', qParams);
  cmr.addrdetails = result;
  cmr.showModal('AddressDetailsModal');
  openAddressDetails.addrType = addrType;
  if (nordx_cntries.indexOf(cntry) > -1 && (addrType == 'ZP02' || addrType == 'ZI01') && (reqType == 'U' || reqType == 'X')) {
    // cmr.showNode("machineSerialAddrDetails");
    // setAddrDetailsForView(addrType, addrSeq);
    cmr.hideNode("machineSerialAddrDetails");
  } else if (nordx_cntries.indexOf(cntry) > -1 || cntry == '702') {
    cmr.hideNode("machineSerialAddrDetails");
  }
  if (GEOHandler.AP != undefined) {
    setAddressDetailsForViewAP();
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == '848' || FormManager.getActualValue('cmrIssuingCntry') == '838' || FormManager.getActualValue('cmrIssuingCntry') == '866'
      || FormManager.getActualValue('cmrIssuingCntry') == '754') {
    hideHarwareonSummary();
  }
}

/**
 * When the Address Details (view only) modal is shown, connect to the DB and
 * retrieve the values
 */
function AddressDetailsModal_onLoad() {
  var details = cmr.addrdetails;
  var role = FormManager.getActualValue('userRole').toUpperCase();
  dojo.byId('dplChkResult_view').innerHTML = '';
  dojo.byId('dplChkInfo_view').innerHTML = '';
  _assignDetailsValue('#AddressDetailsModal #custNm1_view', details.ret4);
  _assignDetailsValue('#AddressDetailsModal #custNm2_view', details.ret5);
  _assignDetailsValue('#AddressDetailsModal #custNm3_view', details.ret6);
  _assignDetailsValue('#AddressDetailsModal #custNm4_view', details.ret7);
  _assignDetailsValue('#AddressDetailsModal #addrTxt_view', details.ret8);

  _assignDetailsValue('#AddressDetailsModal #city1_view', details.ret9);
  _assignDetailsValue('#AddressDetailsModal #city2_view', details.ret10);
  _assignDetailsValue('#AddressDetailsModal #postCd_view', details.ret12);

  _assignDetailsValue('#AddressDetailsModal #bldg_view', details.ret15);

  _assignDetailsValue('#AddressDetailsModal #floor_view', details.ret16);

  _assignDetailsValue('#AddressDetailsModal #office_view', details.ret17);
  _assignDetailsValue('#AddressDetailsModal #dept_view', details.ret18);
  _assignDetailsValue('#AddressDetailsModal #poBox_view', details.ret19);

  _assignDetailsValue('#AddressDetailsModal #poBoxCity_view', details.ret20);

  _assignDetailsValue('#AddressDetailsModal #poBoxPostCd_view', details.ret21);

  _assignDetailsValue('#AddressDetailsModal #custFax_view', details.ret22);
  _assignDetailsValue('#AddressDetailsModal #custPhone_view', details.ret23);

  if (FormManager.getActualValue('cmrIssuingCntry') == '897' && details.ret2 != 'ZI01' && details.ret2 != 'ZS01') {
    if (details.ret2 == 'ZP01' && role == 'PROCESSOR') {
      cmr.showNode('updateButtonFromView');
    } else {
      cmr.hideNode('updateButtonFromView');
    }
  } else {
    cmr.showNode('updateButtonFromView');
  }

  // Defect 1518423: PP: Update Address functionality is visible for Imported
  // Billing Address for SM Create :Mukesh
  if ('758' == FormManager.getActualValue('cmrIssuingCntry') && 'C' == FormManager.getActualValue('reqType') && 'Y' == details.ret31) {
    if ((details.ret2 == 'ZI01' || details.ret2 == 'ZP01')) {
      var ifProspect = 'N';
      if (dijit.byId('prospLegalInd')) {
        ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
      }
      if (ifProspect == 'N') {
        console.log("Hiding the update button in company and billing address of Italy..");
        cmr.hideNode('updateButtonFromView');
      }
    }
  } else if ('758' == FormManager.getActualValue('cmrIssuingCntry') && ('U' == FormManager.getActualValue('reqType') || 'X' == FormManager.getActualValue('reqType')) && 'Y' == details.ret31) {
    if (details.ret68 != undefined && details.ret68 != '' && details.ret68 != FormManager.getActualValue('cmrNo')) {
      cmr.hideNode('updateButtonFromView');
    }
  }

  if ('Y' == details.ret26) {
    _assignDetailsValue('#AddressDetailsModal #addressStdResult_view', 'Accepted');
  } else if ('N' == details.ret26) {
    _assignDetailsValue('#AddressDetailsModal #addressStdResult_view', 'Rejected');
  } else {
    if (details.ret25 != null && details.ret25 != '') {
      var qParams;
      qParams = {
        CD : details.ret25,
      };
      var result = cmr.query('GETTGMESTATUS', qParams);
      _assignDetailsValue('#AddressDetailsModal #addressStdResult_view', result.ret1);
    } else {
      var validTest = cmr.query('GETTGMEVALID', {
        CD : FormManager.getActualValue('landCntry')
      });
      if (validTest && validTest.ret1 == FormManager.getActualValue('landCntry')) {
        _assignDetailsValue('#AddressDetailsModal #addressStdResult_view', 'Not Required');
      } else {
        _assignDetailsValue('#AddressDetailsModal #addressStdResult_view', 'Not Required');
      }
    }

  }
  if ('N' == details.ret26) {
    _assignDetailsValue('#AddressDetailsModal #addressStdRejCmt_view', details.ret28);
    var reasonTxt = cmr.query('GETADDRSTDREASON', {
      CD : details.ret27
    });
    _assignDetailsValue('#AddressDetailsModal #addressStdRejReason_view', reasonTxt ? reasonTxt.ret1 : '');
  } else {
    _assignDetailsValue('#AddressDetailsModal #addressStdRejReason_view', '');
    _assignDetailsValue('#AddressDetailsModal #addressStdRejCmt_view', '');
  }

  var dplText = details.ret32 == 'P' ? 'Passed' : (details.ret32 == 'F' ? 'Failed' : 'Not Done');
  _assignDetailsValue('#AddressDetailsModal #dplChkResult_view', dplText);
  _assignDetailsValue('#AddressDetailsModal #dplChkInfo_view', details.ret33);

  var date = details.ret29;
  var formattedDate = moment(date, "YYYY-MM-DD HH:mm:SS.sss").format("YYYY-MM-DD");
  if (formattedDate == 'Invalid date') {
    _assignDetailsValue('#AddressDetailsModal #addressStdTs_view', '');
  } else {
    _assignDetailsValue('#AddressDetailsModal #addressStdTs_view', formattedDate);
  }

  _assignDetailsValue('#AddressDetailsModal #sapNumber_view', details.ret30);
  _assignDetailsValue('#AddressDetailsModal #cmrNumber_view', details.ret34);
  _assignDetailsValue('#AddressDetailsModal #addressType_view', details.ret35 && details.ret35 != '' ? details.ret35 : details.ret2);

  _assignDetailsValue('#AddressDetailsModal #stateProv_view', details.ret11 + '-' + details.ret36);
  if (FormManager.getActualValue('cmrIssuingCntry') == '897') {
    _assignDetailsValue('#AddressDetailsModal #county_view', details.ret45);
  } else {
    _assignDetailsValue('#AddressDetailsModal #county_view', details.ret37);
  }
  _assignDetailsValue('#AddressDetailsModal #landCntry_view', details.ret13 + '-' + details.ret38);
  _assignDetailsValue('#AddressDetailsModal #transportZone_view', details.ret24);

  _assignDetailsValue('#AddressDetailsModal #dplchk_ts', details.ret42 ? details.ret42 : '');
  _assignDetailsValue('#AddressDetailsModal #dplchk_by', details.ret41 ? details.ret41 + ' (' + details.ret40 + ')' : '');
  _assignDetailsValue('#AddressDetailsModal #dplchk_err', details.ret43 ? details.ret43 : '');

  _assignDetailsValue('#AddressDetailsModal #addrTxt2_view', details.ret47);
  _assignDetailsValue('#AddressDetailsModal #divn_view', details.ret46);
  _assignDetailsValue('#AddressDetailsModal #addrCreateDt_view', details.ret48);
  _assignDetailsValue('#AddressDetailsModal #addrUpdateDt_view', details.ret49);
  _assignDetailsValue('#AddressDetailsModal #custLangCd_view', details.ret73);
  _assignDetailsValue('#AddressDetailsModal #hwInstlMstrFlg_view', details.ret75);

  FormManager.setValue('view_reqId', details.ret1);
  FormManager.setValue('view_addrType', details.ret2);
  FormManager.setValue('view_addrSeq', details.ret3);

  if (FormManager.getActualValue('cmrIssuingCntry') == '631') {
    // 1164561
    if (details.ret50 != '' && details.ret50 != null) {
      _assignDetailsValue('#AddressDetailsModal #taxCd1_view', details.ret50);
    }
    // 1164558
    if (details.ret51 != '' && details.ret51 != null) {
      _assignDetailsValue('#AddressDetailsModal #taxCd2_view', details.ret51);
    }
    // 1164561
    if (details.ret52 != '' && details.ret52 != null) {
      _assignDetailsValue('#AddressDetailsModal #vat_view', details.ret52);
    }
  }

  _assignDetailsValue('#AddressDetailsModal #taxOffice_view', details.ret54);
  _assignDetailsValue('#AddressDetailsModal #prefSeqNo_view', details.ret55);
  _assignDetailsValue('#AddressDetailsModal #vat_view', details.ret52);
  _assignDetailsValue('#AddressDetailsModal #ierpSitePrtyId_view', details.ret53);

  if (FormManager.getActualValue('cmrIssuingCntry') == '758' && details.ret2 == 'ZP01') {
    FormManager.show('streetAbbrev_view', 'streetAbbrev');
    FormManager.show('addrAbbrevLocn_view', 'addrAbbrevLocn');
    FormManager.show('addrAbbrevName_view', 'addrAbbrevName');
    _assignDetailsValue('#AddressDetailsModal #addrAbbrevLocn_view', details.ret22);
    _assignDetailsValue('#AddressDetailsModal #addrAbbrevName_view', details.ret15);
    _assignDetailsValue('#AddressDetailsModal #streetAbbrev_view', details.ret46);
    // _assignDetailsValue('#AddressDetailsModal #billingPstlAddr_view',
    // details.ret58);
  } else if (FormManager.getActualValue('cmrIssuingCntry') == '758' && details.ret2 != 'ZP01') {
    _assignDetailsValue('#AddressDetailsModal #addrAbbrevLocn_view', '');
    _assignDetailsValue('#AddressDetailsModal #addrAbbrevName_view', '');
    _assignDetailsValue('#AddressDetailsModal #streetAbbrev_view', '');
    // _assignDetailsValue('#AddressDetailsModal #billingPstlAddr_view', '');
    FormManager.hide('streetAbbrev_view', 'streetAbbrev_view');
    FormManager.hide('addrAbbrevLocn_view', 'addrAbbrevLocn_view');
    FormManager.hide('addrAbbrevName_view', 'addrAbbrevName_view');
    // FormManager.hide('billingPstlAddr_view', 'billingPstlAddr_view');
  }

  // FormManager.setValue('cnCustName1', details.ret59);
  // FormManager.setValue('cnCustName2', details.ret60);
  // FormManager.setValue('cnAddrTxt2', details.ret61);
  // FormManager.setValue('cnAddrTxt', details.ret62);
  // FormManager.setValue('cnCity', details.ret63);
  // FilteringDropdown.val_cnCity = details.ret63;
  // FormManager.setValue('city1', details.ret9);
  // FilteringDropdown.val_city1 = details.ret9;
  // FormManager.setValue('cnDistrict', details.ret64);
  // FormManager.setValue('cnCustContNm', details.ret65);
  // FormManager.setValue('cnCustContJobTitle', details.ret66);
  // FormManager.setValue('cnCustContPhone2', details.ret67);

  if (FormManager.getActualValue('cmrIssuingCntry') == '641') {
    _assignDetailsValue('#AddressDetailsModal #cnCustName1_view', details.ret59);
    _assignDetailsValue('#AddressDetailsModal #cnCustName2_view', details.ret60);
    _assignDetailsValue('#AddressDetailsModal #cnAddrTxt2_view', details.ret61);
    _assignDetailsValue('#AddressDetailsModal #cnAddrTxt_view', details.ret62);
    _assignDetailsValue('#AddressDetailsModal #cnCity_view', details.ret63);
    _assignDetailsValue('#AddressDetailsModal #cnDistrict_view', details.ret64);
    _assignDetailsValue('#AddressDetailsModal #cnCustContNm_view', details.ret65);
    _assignDetailsValue('#AddressDetailsModal #cnCustContJobTitle_view', details.ret66);
    _assignDetailsValue('#AddressDetailsModal #cnCustName3_view', details.ret73);
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == '766') {
    _assignDetailsValue('#AddressDetailsModal #billingPstlAddr_view', details.ret58);
    _assignDetailsValue('#AddressDetailsModal #contact_view', details.ret71);
    _assignDetailsValue('#AddressDetailsModal #countyName_view', details.ret45);
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == '760') {
    _assignDetailsValue('#AddressDetailsModal #locationCode_view', details.ret56);
    _assignDetailsValue('#AddressDetailsModal #estabFuncCd_view', details.ret69);
    _assignDetailsValue('#AddressDetailsModal #companySize_view', details.ret70);
    _assignDetailsValue('#AddressDetailsModal #contact_view', details.ret71);
    _assignDetailsValue('#AddressDetailsModal #rol_view', details.ret72);

    var custType = FormManager.getActualValue('custType');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var reqType = FormManager.getActualValue('reqType');
    var addrType = details.ret2;
    if (addrType == 'ZC01' && custType.includes('C')) {
      if (reqType == 'U' && (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI' || custSubGrp == 'INTER')) {
        cmr.hideNode('updateButtonFromView');
      } else {
        cmr.showNode('updateButtonFromView');
      }
    } else if (addrType == 'ZE01' && custType.includes('E')) {
      if (reqType == 'U' && (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI' || custSubGrp == 'INTER')) {
        cmr.hideNode('updateButtonFromView');
      } else {
        cmr.showNode('updateButtonFromView');
      }
    }
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == '862') {
    var addrType = details.ret2;
    if (addrType == 'ZS01') {
      FormManager.hide('taxOffice_view', 'taxOffice_view');
      FormManager.show('custPhone_view', 'custPhone_view');
    } else {
      FormManager.hide('custPhone_view', 'custPhone_view');
      FormManager.show('taxOffice_view', 'taxOffice_view');
      _assignDetailsValue('#AddressDetailsModal #custPhone_view', '');
    }
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == '706') {
    var reqTypes = FormManager.getActualValue('reqType');
    if (reqTypes == 'U') {
      if (details.ret30 != null) {
        _assignDetailsValue('#AddressDetailsModal #ierpSitePrtyId_view', 'S' + details.ret30);
      }
    }
  }
}

function displayHwMstInstallFlagNew() {
  console.log(">>> Executing displayHwMstInstallFlagNew");
  console.log('>> cmr.addressMode >>' + cmr.addressMode);
  if (cmr.addressMode == 'newAddress'
      && (FormManager.getActualValue('cmrIssuingCntry') == '838' || FormManager.getActualValue('cmrIssuingCntry') == '866' || FormManager.getActualValue('cmrIssuingCntry') == '754')) {
    var _addrTypesForEMEA = [ 'ZD01', 'ZP01', 'ZI01', 'ZS01', 'ZS02' ];
    console.log('>> BEGIN displayHwMstInstallFlagNew newAddress or copyAddress function ');
    for (var i = 0; i < _addrTypesForEMEA.length; i++) {
      console.log('>> INSIDE for loop');

      addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForEMEA[i]), 'onClick', function(value) {
        if (FormManager.getField('addrType_ZI01').checked || FormManager.getField('addrType_ZS01').checked) {
          console.log(' >> 1 >> ' + FormManager.getField('addrType_ZI01').checked);
          console.log(' >> 2 >> ' + FormManager.getField('addrType_ZS01').checked);
          cmr.showNode('hwFlag');
        } else {
          FormManager.getField('hwInstlMstrFlg').set('checked', false);
          cmr.hideNode('hwFlag');
        }
      });
    }
  }
}
/**
 * 
 * @param queryId
 * @param value
 */
function _assignDetailsValue(queryId, value) {
  var result = dojo.query(queryId);
  if (result != null && result.length > 0) {
    result[0].innerHTML = value;
  }
}

function AddressDetailsModal_onClose() {
}

/**
 * Executed when Update is clicked from Address Details modal
 */
function doUpdateAddrFromDetails() {
  cmr.hideModal('AddressDetailsModal');
  var req_id = FormManager.getActualValue('view_reqId');
  var addr_type = FormManager.getActualValue('view_addrType');
  var addr_seq = FormManager.getActualValue('view_addrSeq');
  var mandt = cmr.MANDT;
  doUpdateAddr(req_id, addr_type, addr_seq, mandt);
}

/**
 * Called for cancel add/edit address
 */
function cancelAddressModal() {
  if (cmr.addressMode == 'updateAddress') {
    cmr.showConfirm('actualCancelAddressModal()', 'Exit without saving this address row?');
  } else {
    cmr.showConfirm('actualCancelAddressModal()', 'Exit without saving this new address row?');
  }
}
/**
 * Called for cancel add/edit address
 */
function actualCancelAddressModal() {
  cmr.hideModal('addEditAddressModal');
  MessageMgr.clearMessages(true);
  if (FormManager.getActualValue('cmrIssuingCntry') == '631') {
    // 1164561
    FormManager.clearValue('taxCd1');
    // 1164558
    FormManager.clearValue('taxCd2');
    // 1164561
    FormManager.clearValue('vat');
  }
}

/**
 * Adds ZS01 to address list
 */
function doAddZS01ToAddressList() {
  cmr.currentModalId = 'addEditAddressModal';
  actualAddToAddressList();
}

/**
 * Determines if address standardization is needed
 * 
 * @returns {Number}
 */
function tgmePreSave() {
  var resultTxt = FormManager.getActualValue('addr_addrStdResult');
  if (resultTxt == '') {
    return 1;
  }
  resultTxt = dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML;
  if (resultTxt == 'Not Done') {
    return 2;
  }
  if (isTgmeNeeded()) {
    FormManager.setValue('addrStdAcceptInd', '');
    dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = 'Not Required';
    FormManager.setValue('addr_addrStdResult', 'X');
    FormManager.setValue('addrStdTsString', '');
    dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = '';
    FormManager.setValue('addr_addrStdRejReason', '');
    dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = '';
    FormManager.setValue('addr_addrStdRejCmt', '');
    return 3;
  }
  return 0;
}

/**
 * The 'save' function for address records
 */
function doAddToAddressList() {

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var asean_isa_cntries = [ '643', '744', '736', '738', '796', '778', '749', '834', '818', '852', '856', '616', '652', '615' ];
  var ero_countries = ['641'];
  var reqReason = FormManager.getActualValue('reqReason');
  
  if (ero_countries.indexOf(cntry) >= 0 && reqReason == 'TREC') {
    cmr.showAlert('This is a Temporary Embargo Removal Request. Updation or creation of new records is not allowed.');
    return;
  }
  
  // CREATCMR-5741 no TGME Addr Std
  // if (GEOHandler.isTGMERequired(cntry)) {
  // var tgmeResult = tgmePreSave();
  // if (tgmeResult > 0) {
  // performAddressStandardization(true);
  // return;
  // }
  // } else {
  // saveNotSupportedTGME();
  // }

  cmr.currentModalId = 'addEditAddressModal';
  cmr.addressReqId = FormManager.getActualValue('reqId');
  cmr.addressSequence = FormManager.getActualValue('addrSeq');
  cmr.addressType = FormManager.getActualValue('addrType');
  var dummyseq = "xx";
  var qParams;

  if (typeof (GEOHandler) != 'undefined') {
    GEOHandler.executeAddrFuncs(true, false);
  }

  if (FormManager.validate('frmCMR_addressModal', true)) {
    if (cmr.addressMode == 'updateAddress') {
      qParams = {
        REQ_ID : cmr.addressReqId,
        ADDR_SEQ : cmr.addressSequence,
      };
    } else {
      qParams = {
        REQ_ID : cmr.addressReqId,
        ADDR_SEQ : dummyseq,
      };
    }
    var showSoldToError = false;
    var showMailingError = false;
    var showEPLError = false;
    var noSoldToCheck = new Set([ SysLoc.USA, SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
    if (!noSoldToCheck.has(cntry)) {
      var result = cmr.query('GETZS01RECORDS', qParams);
      var zs01count = result.ret1;
      showSoldToError = Number(zs01count) >= 1 && cmr.addressType == 'ZS01';

      var resultZP01 = cmr.query('GETZP01RECORDS', qParams);
      var zp01count = resultZP01.ret1;
      showMailingError = Number(zp01count) >= 1 && cmr.addressType == 'ZP01';

      var resultZS02 = cmr.query('GETZS02RECORDS', qParams);
      var zs02count = resultZS02.ret1;
      showEPLError = Number(zs02count) >= 1 && cmr.addressType == 'ZS02';
    }

    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL && (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress')) {
      if (cmr.addressType == 'ZD01') {
        cmr.showConfirm('actualAddToAddressList()', 'Country Use C (Shipping) is required to be created after the Shipping address.');
        return;
      }
    }

    if (FormManager.getActualValue('cmrIssuingCntry') == '838') {
      if (showSoldToError) {
        cmr.showAlert('This request already has a Billing record. Only one is permitted. The existing record has to be deleted to create a new record.');
        return;
      }
      if (showMailingError) {
        cmr.showAlert('This request already has a Mailing record. Only one is permitted. The existing record has to be deleted to create a new record.');
        return;
      }
      if (showEPLError) {
        cmr.showAlert('This request already has an EPL record. Only one is permitted. The existing record has to be deleted to create a new record.');
        return;
      }
    }
    if ((showSoldToError) && (FormManager.getActualValue('cmrIssuingCntry') != '838')) {
      cmr.showConfirm('doAddZS01ToAddressList()',
          'This request already has a Sold To record. Only one is permitted. You will have to remove one before completing the request processing. Do you still want to add this row?', null, null, {
            OK : 'Add Address Row',
            CANCEL : 'Cancel'
          });
    } else {

      if (FormManager.getActualValue('cmrIssuingCntry') == '641') {
        // do
        if (isCNAddressDetailsExisting()) {
          // cmr.showAlert('The details of this address is the same as the
          // existing Sold-To. This address can not be added.');
          // return;
        }
      }
      if (asean_isa_cntries.indexOf(cntry) >= 0) {
        // do
        if (isAPAddressDetailsExisting()) {
          cmr.showAlert('The details of this address is the same as the existing address. This address can not be added.');
          return;
        }
      }

      if (FormManager.getActualValue('cmrIssuingCntry') != '897') {
        // until the encoding issue on non latin names is fixed, this will be
        // hardcoded
        actualAddToAddressList();
        return;
      }
      var standardCityResult = CmrServices.getStandardCity('frmCMR_addressModal', FormManager.getActualValue('cmrIssuingCntry'));

      var standardCity = standardCityResult.result;

      cmr.standardCity = standardCity;

      if (standardCity.success == false) {
        if (cmr.addressMode == 'updateAddress') {
          cmr.showConfirm('actualAddToAddressList()', 'Cannot verify City name right now. Save the changes to the Address?');
        } else {
          cmr.showConfirm('actualAddToAddressList()', 'Cannot verify City name right now. Add the new address row to the Address List?');
        }
      } else {
        var currentCity = FormManager.getActualValue('city1');
        var countyCd = FormManager.getActualValue('county');
        if (standardCity.cityMatched) {
          // there is a match
          if (standardCity.standardCity && standardCity.standardCity.toUpperCase() != currentCity.toUpperCase()) {
            // show prompt only if the values are not the same
            cmr.showModal('stdcitynameModal');
          } else if (standardCity.standardCountyCd && standardCity.standardCountyCd != countyCd) {
            // FormManager.setValue('county', standardCity.standardCountyCd);
            actualAddToAddressList();
          } else {
            // we can save, everything fine
            actualAddToAddressList();
          }
        } else if (standardCity.suggested != null && standardCity.suggested.length > 0) {
          cmr.showModal('stdcityModal');
        } else {
          // CREATCMR-8323 : add SAVE when landedCntry!='US'
          var landedCntry = FormManager.getActualValue('landCntry');
          if(cntry=='897' && landedCntry!='US'){
            // We can save here.
            actualAddToAddressList();
          } else {
            cmr.showAlert('The system cannot find a match for the city name <strong>' + currentCity + '</strong>. Kindly recheck the address and specify a valid city and/or county name.');
            return;
          }
        }
      }
    }
  }
}

/**
 * Does the actual adding to the list
 */
function actualAddToAddressList() {
  cmr.addrReqId = FormManager.getActualValue('reqId');
  cmr.currentModalId = 'addEditAddressModal';
  if (typeof (GEOHandler) != 'undefined') {
    GEOHandler.executeAddrFuncs(true, true);
  }
  cmr.addressSaved = true;
  cmr.currentAddressType = FormManager.getActualValue('addrType');
  cmr.currentAddressSeq = FormManager.getActualValue('addrSeq');
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    FormManager.doHiddenAction('frmCMR_addressModal', 'ADD_ADDRESS', cmr.CONTEXT_ROOT + '/request/address/process.json?reqId=' + cmr.addrReqId, true, refreshAddressAfterResult, true);
  } else if (cmr.addressMode == 'updateAddress') {
    FormManager.doHiddenAction('frmCMR_addressModal', 'UPDATE_ADDRESS', cmr.CONTEXT_ROOT + '/request/address/process.json?reqId=' + cmr.addrReqId, true, refreshAddressAfterResult, true);

    if (FormManager.getActualValue('cmrIssuingCntry') == '649' && cmr.currentAddressType == 'ZS01' && cmr.currentAddressSeq == '00001') {
      toggleCATaxFieldsByProvCd(FormManager.getActualValue('stateProv'));
      setPrefLangByProvCd(cmr.currentAddressSeq, FormManager.getActualValue('stateProv'));
    }
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == '631') {
    // 1164561
    FormManager.clearValue('taxCd1');
    // 1164558
    FormManager.clearValue('taxCd2');
    // 1164561
    FormManager.clearValue('vat');
  }
}

/**
 * Refreshes the Address grid after saving
 * 
 * @param result
 */
function refreshAddressAfterResult(result, skipCopy, skipHideModal) {
  if (result.success) {
    if (!skipCopy && (typeof (GEOHandler) != 'undefined') && cmr.addressMode != 'removeAddress') {
      if (GEOHandler.isCopyAddressEnabled()) {
        cmr.currentAddressResult = result;
        cmr.hideModal('addEditAddressModal');
        CmrGrid.refresh('ADDRESS_GRID');
        return;
      }
    }
    if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
      FormManager.setValue('dplMessage', result.message);
      FormManager.setValue('showAddress', 'Y');
      if (FormManager.getActualValue('addrType') == 'ZS01') {
        FormManager.setValue('mainAddrType', 'ZS01');
      }
      if (cmr.currentRequestType) {
        FormManager.setValue('reqType', cmr.currentRequestType);
      }
      cmr.noCreatePop = 'N';
      // enable all checkboxes
      var cb = dojo.query('[type=checkbox]');
      for (var i = 0; i < cb.length; i++) {
        if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
          cb[i].disabled = false;
          cb[i].removeAttribute('disabled');
        }
      }
      FormManager.doAction('frmCMR', 'DPL', true);
    } else {
      if (cmr.addressMode == 'updateAddress') {
        // 1557838
        var katr6 = FormManager.getActualValue("cmrIssuingCntry");

        if (katr6 == "641") { // CN
          // 1557838
          doCNDPLPageRefresh(result);
        } else if (isSSACountry()) { // SSA W/O MX
          doSSADPLPageRefresh(result);
        } else if (katr6 == '781') { // MX
          doMXDPLPageRefresh(result);
        } else if (katr6 == '631') { // BR
          doBRDPLPageRefresh(result);
        } else {
          // DENNIS: Default DPL refresh validation
          if (FormManager.getActualValue('custNm1') != cmr.oldname1 || FormManager.getActualValue('custNm2') != cmr.oldname2 || FormManager.getActualValue('landCntry') != cmr.oldlandcntry
          // CMR-1947 refresh page to update abbrv_loc
          || FormManager.getActualValue('city1') != cmr.oldcity1) {
            FormManager.setValue('dplMessage', result.message);
            FormManager.setValue('showAddress', 'Y');
            if (FormManager.getActualValue('addrType') == 'ZS01') {
              FormManager.setValue('mainAddrType', 'ZS01');
            }
            // enable all checkboxes
            var cb = dojo.query('[type=checkbox]');
            for (var i = 0; i < cb.length; i++) {
              if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
                cb[i].disabled = false;
                cb[i].removeAttribute('disabled');
              }
            }
            FormManager.doAction('frmCMR', 'DPL', true);
            return;
          }
        }

      }
      if (cmr.addressMode != 'removeAddress') {
        if (!skipHideModal) {
          cmr.hideModal('addEditAddressModal');
        }
        if ('ZS01' == FormManager.getActualValue('addrType')) {
          FormManager.setValue('mainAddrType', 'ZS01');
        }
      }
      CmrGrid.refresh('ADDRESS_GRID');

      // CREATCMR-5447
      if (FormManager.getActualValue('cmrIssuingCntry') == '897') {
        // window.location.reload();
        checkSCCValidate();
      }
      // CREATCMR-5447
    }
  } else {
    dojo.query('#dialog_addEditAddressModal div.dijitDialogPaneContent')[0].scrollTo(0, 0);
  }

  // call the addr std result calculation for score card here..
  SetAddrStdResult();
}

/**
 * Adds a new Address to the list
 */
function doAddAddress() {
  // 1377871 -Postal Address
  /*
   * if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ITALY) {
   * FormManager.clearValue('billingPstlAddr');
   * FormManager.hide('BillingPstlAddr', 'billingPstlAddr'); }
   */
  cmr.addressMode = 'newAddress';
  cmr.showModal('addEditAddressModal');
}
cmr.noCreatePop = 'N';

/**
 * Onload function for Address Add/Update screen Connects to the DB and
 * retrieves the current values
 */
function addEditAddressModal_onLoad() {

  var details = cmr.addrdetails;
  cmr.addressSaved = false;
  cmr.currentAddressType = null;
  cmr.currentAddressSeq = null;
  cmr.currentAddressResult = null;
  dojo.byId('dplChkResultEdit').innerHTML = '';
  dojo.byId('dplChkTsEdit').innerHTML = '';
  dojo.byId('dplChkByEdit').innerHTML = '';
  dojo.byId('dplChkErrEdit').innerHTML = '';
  dojo.byId('dplChkInfoEdit').innerHTML = '';
  FormManager.setValue('landCntry', '');

  // toggle the boxes
  if (cmr.addressMode == 'updateAddress') {
    dojo.byId('addrTypeCheckboxes').style.display = 'none';
    dojo.byId('addrTypeStaticText').style.display = 'block';
  } else {
    dojo.byId('addrTypeCheckboxes').style.display = 'block';
    dojo.byId('addrTypeStaticText').style.display = 'none';
  }

  if (typeof (GEOHandler) != 'undefined') {
    GEOHandler.executeToggleTypeFuncs(cmr.addressMode, details);
  }

  // automatically assign the name1 and name2 for new addresses

  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    storeTgmeCompare(true);
    cmr.currentRequestType = FormManager.getActualValue('reqType');
    if (cmr.currentRequestType != null && cmr.currentRequestType.length > 1) {
      cmr.currentRequestType = cmr.currentRequestType.substring(0, 1);
      FormManager.setValue('transportZone', 'Z000000001');
    }
    cmr.noCreatePop = 'Y';
    if (cmr.currentRequestType != 'C') {
      FormManager.setValue('reqType', 'C');
    }

  }
  if (cmr.addressMode == 'newAddress') {

    populateAddressDeltaInd({}, true);
    FormManager.clearValue('custNm1');
    FormManager.clearValue('custNm2');
    FormManager.clearValue('custNm3');
    FormManager.clearValue('custNm4');
    FormManager.clearValue('addrTxt');
    FormManager.clearValue('city1');
    FormManager.clearValue('city2');
    FormManager.clearValue('postCd');
    FormManager.clearValue('bldg');
    FormManager.clearValue('floor');
    FormManager.clearValue('office');
    FormManager.clearValue('dept');
    FormManager.clearValue('poBox');
    FormManager.clearValue('poBoxCity');
    FormManager.clearValue('poBoxPostCd');
    FormManager.clearValue('custFax');
    FormManager.clearValue('custPhone');
    FormManager.clearValue('divn');
    FormManager.clearValue('addrTxt2');
    FormManager.clearValue('addrStdResult');

    FormManager.clearValue('addrStdRejReason');
    FormManager.clearValue('addrStdRejCmt');
    FormManager.clearValue('addrStdTsString');
    FormManager.clearValue('sapNo');
    FormManager.clearValue('ierpSitePrtyId');
    FormManager.clearValue('taxOffice');
    // FormManager.clearValue('billingPstlAddr');
    FormManager.clearValue('custlangCd');

    if (FormManager.getActualValue('cmrIssuingCntry') != '897') {
      FormManager.clearValue('addrType');
      FormManager.setValue('addrType', 'ZS01'); // default to ZS01
      cmr.showNode('radiocont_ZS01');
    }
    FormManager.clearValue('stateProv');
    FormManager.clearValue('county');
    FormManager.clearValue('landCntry');
    if (FormManager.getActualValue('cmrIssuingCntry') == '754') {
      FormManager.setValue('landCntry', 'IE');
      // FormManager.setValue('postCd', 'II1 1II');
    }
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
      FormManager.setValue('addrType', 'ZP01');
    }
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.PORTUGAL || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SPAIN
        || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
      FormManager.setValue('addrType', 'ZS01');
    }
    FormManager.clearValue('transportZone');
    cmr.hideNode('cmr-findcmr-record');
    cmr.hideNode('cmr-dnb-record');

    FilteringDropdown.val_stateProv = null;
    FilteringDropdown.val_county = null;
    FilteringDropdown.val_transportZone = 'Z000000001';

    cmr.setModalTitle('addEditAddressModal', 'Add an Address');
    dojo.byId('addressBtn').value = 'Save Address';

    FormManager.setValue('importInd', 'N');
    // FormManager.enable('sapNo');
    // FormManager.enable('ierpSitePrtyId');
    FormManager.readOnly('sapNo');
    FormManager.readOnly('ierpSitePrtyId');

    FormManager.setValue('addrStdAcceptInd', '');
    FormManager.setValue('addr_addrStdResult', 'X');
    FormManager.setValue('addr_addrStdRejReason', '');
    dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = '';
    FormManager.setValue('addr_addrStdRejCmt', '');
    FormManager.setValue('addrStdTsString', '');
    dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = '';
    dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = 'Not Required';

    if (dojo.byId('addrCreateDt_updt')) {
      dojo.byId('addrCreateDt_updt').innerHTML = '-';
    }
    if (dojo.byId('addrUpdateDt_updt')) {
      dojo.byId('addrUpdateDt_updt').innerHTML = '-';
    }

    if (cmr.addressMode == 'newAddress') {
      FormManager.resetValidations('addrType');
      FormManager.addValidator('addrType', Validators.REQUIRED, [ 'Address Type' ], null);
      FormManager.setValue('transportZone', 'Z000000001');
    }

    if (cmr.addressMode == 'newAddress'
        && (FormManager.getActualValue('cmrIssuingCntry') == '848' || FormManager.getActualValue('cmrIssuingCntry') == '866' || FormManager.getActualValue('cmrIssuingCntry') == '754')) {
      FormManager.clearValue('custLangCd');
      FormManager.getField('hwInstlMstrFlg').set('checked', false);
    }

    if (cmr.addressMode == 'newAddress' && FormManager.getActualValue('cmrIssuingCntry') == '758') {
      FormManager.setValue('addrType', 'ZI01'); // default to Company
      FormManager.clearValue('streetAbbrev');
      FormManager.clearValue('addrAbbrevLocn');
      FormManager.clearValue('addrAbbrevName');

      console.log("Hiding company address if it is already Imported for Italy...");
      var reqId = FormManager.getActualValue('reqId');
      var isCompayImport = cmr.query('VALIDATOR.IMPORTED_IT', {
        REQID : reqId
      });
      console.log("Italy :isCompayImport.....>" + isCompayImport.ret1);
      if (isCompayImport != undefined && isCompayImport.ret1 == 'Y') {
        FormManager.clearValue('addrType');
        cmr.hideNode('radiocont_ZI01');
      }
    }

  } else if (cmr.addressMode == 'copyAddress' || cmr.addressMode == 'updateAddress' || cmr.addressMode == 'removeAddress') {

    if (cmr.addressMode == 'updateAddress') {
      FormManager.resetValidations('addrType');
      cmr.currentRequestType = FormManager.getActualValue('reqType');
      if (cmr.currentRequestType != null && cmr.currentRequestType.length > 1) {
        cmr.currentRequestType = cmr.currentRequestType.substring(0, 1);
      }
    }
    if (cmr.addressMode == 'updateAddress' || cmr.addressMode == 'removeAddress') {
      FormManager.setValue('addrType', details.ret2);
      FormManager.setValue('saveAddrType', details.ret2);
      FormManager.setValue('custLangCd', details.ret73);
      dojo.byId('addrTypeStaticText').innerHTML = details.ret35 && details.ret35 != '' ? details.ret35 : details.ret2;
      FormManager.readOnly('sapNo');
      FormManager.readOnly('ierpSitePrtyId');
      FormManager.setValue('addrSeq', details.ret3);
      FormManager.setValue('sapNo', details.ret30);
      FormManager.setValue('ierpSitePrtyId', details.ret53);
      FormManager.setValue('taxOffice', details.ret54);
      FormManager.setValue('prefSeqNo', details.ret55);

      if (FormManager.getActualValue('cmrIssuingCntry') == '848' || FormManager.getActualValue('cmrIssuingCntry') == '866' || FormManager.getActualValue('cmrIssuingCntry') == '754') {
        if (dijit.byId('hwInstlMstrFlg') != undefined && FormManager.getField('hwInstlMstrFlg') != null) {
          FormManager.getField('hwInstlMstrFlg').set('checked', details.ret75 == 'Y' ? true : false);
        }
      }

      if (FormManager.getActualValue('cmrIssuingCntry') == '760') {
        FormManager.setValue('companySize', details.ret70);
        FormManager.setValue('estabFuncCd', details.ret69);
        FormManager.setValue('locationCode', details.ret56);
        FormManager.setValue('contact', details.ret71);
        FormManager.setValue('rol', details.ret72);
      }
      // if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ITALY) {
      // if (FormManager.getActualValue('addrType') != '' &&
      // FormManager.getActualValue('addrType') == 'ZP01') {
      // FormManager.show('BillingPstlAddr', 'billingPstlAddr');
      // FormManager.setValue('billingPstlAddr', details.ret58);
      // } else {
      // FormManager.clearValue('billingPstlAddr');
      // FormManager.hide('BillingPstlAddr', 'billingPstlAddr');
      // }
      // }

      if (FormManager.getActualValue('cmrIssuingCntry') == '766') {
        FormManager.setValue('billingPstlAddr', details.ret58);
        FormManager.setValue('contact', details.ret71);
        FormManager.setValue('countyName', details.ret45);
      }

      if ('Y' == details.ret26) {
        FormManager.setValue('addrStdAcceptInd', "Y");
        dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = "Accepted";
      } else if ('N' == details.ret26) {
        FormManager.setValue('addrStdAcceptInd', "N");
        dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = "Rejected";
      } else {
        if (details.ret25 != null && details.ret25 != '') {
          var qParams;
          qParams = {
            CD : details.ret25,
          };
          var result = cmr.query('GETTGMESTATUS', qParams);
          dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = result.ret1;
        } else {
          var validTest = cmr.query('GETTGMEVALID', {
            CD : details.ret13
          });
          if (validTest && validTest.ret1 == details.ret13) {
            dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = 'Not Required';
          } else {
            dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = 'Not Required';
          }
        }

      }
      // address std fields
      FormManager.setValue('addr_addrStdResult', details.ret25);
      if ('N' == details.ret26) {
        FormManager.setValue('addr_addrStdRejReason', details.ret27);
        var reasonTxt = cmr.query('GETADDRSTDREASON', {
          CD : details.ret27
        });
        dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = reasonTxt ? reasonTxt.ret1 : '';
        FormManager.setValue('addr_addrStdRejCmt', details.ret28);
      } else {
        FormManager.setValue('addr_addrStdRejReason', '');
        dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = '';
        FormManager.setValue('addr_addrStdRejCmt', '');
      }

      var date = details.ret29;
      var formattedDate = moment(date, "YYYY-MM-DD HH:mm:SS.sss").format("YYYY-MM-DD");
      if (formattedDate == 'Invalid date') {
        FormManager.setValue('addrStdTsString', '');
      } else {
        FormManager.setValue('addrStdTsString', formattedDate);
      }
      dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = FormManager.getActualValue('addrStdTsString');
    } else if (cmr.addressMode == 'copyAddress') {
      if (FormManager.getActualValue('cmrIssuingCntry') != '897') {
        FormManager.clearValue('addrType');
      }
      FormManager.clearValue('addrSeq');
      FormManager.clearValue('sapNo');
      FormManager.clearValue('ierpSitePrtyId');
      FormManager.clearValue('addrStdResult');
      FormManager.clearValue('addrStdRejReason');
      FormManager.clearValue('addrStdRejCmt');
      FormManager.clearValue('addrStdTsString');
      FormManager.clearValue('addrStdAcceptInd');
      cmr.hideNode('cmr-findcmr-record');
      cmr.hideNode('cmr-dnb-record');
    }
    FormManager.setValue('addrSeq', details.ret3);
    FormManager.setValue('custNm1', details.ret4);
    cmr.oldname1 = details.ret4;
    FormManager.setValue('custNm2', details.ret5);
    cmr.oldname2 = details.ret5 ? details.ret5 : '';
    FormManager.setValue('custNm3', details.ret6);
    FormManager.setValue('custNm4', details.ret7);
    FormManager.setValue('addrTxt', details.ret8);
    cmr.oldaddrtxt = details.ret8;
    FormManager.setValue('city1', details.ret9);
    cmr.oldcity1 = details.ret9;
    FormManager.setValue('city2', details.ret10);
    cmr.oldcity2 = details.ret10;
    FormManager.setValue('postCd', details.ret12);
    cmr.oldpostcd = details.ret12;
    FormManager.setValue('bldg', details.ret15);
    cmr.oldbldg = details.ret15;
    FormManager.setValue('floor', details.ret16);
    FormManager.setValue('office', details.ret17);
    cmr.oldoffice = details.ret17;
    FormManager.setValue('dept', details.ret18);
    cmr.olddept = details.ret18;
    FormManager.setValue('poBox', details.ret19);
    cmr.oldpobox = details.ret19;
    FormManager.setValue('poBoxCity', details.ret20);
    FormManager.setValue('poBoxPostCd', details.ret21);
    FormManager.setValue('custFax', details.ret22);
    FormManager.setValue('custPhone', details.ret23);
    cmr.oldcustphone = details.ret23;
    // FormManager.setValue('cmrNo',details.ret34);

    FormManager.setValue('stateProv', details.ret11);
    FilteringDropdown.val_stateProv = details.ret11;
    cmr.oldstateprov = details.ret11;
    FormManager.setValue('county', details.ret14);
    FilteringDropdown.val_county = details.ret14;
    FormManager.setValue('landCntry', details.ret13);
    cmr.oldlandcntry = details.ret13;
    FormManager.setValue('transportZone', details.ret24);
    FilteringDropdown.val_transportZone = details.ret24;
    cmr.oldtransportzone = details.ret24;
    FormManager.setValue('stdCityNm', details.ret45);
    FormManager.setValue('divn', details.ret46);
    cmr.olddivn = details.ret46;
    FormManager.setValue('addrTxt2', details.ret47);
    cmr.oldaddrtxt2 = details.ret47;
    if (FormManager.getActualValue('cmrIssuingCntry') == '760') {
      FormManager.setValue('companySize', details.ret70);
      FormManager.setValue('estabFuncCd', details.ret69);
      FormManager.setValue('locationCode', details.ret56);
      FormManager.setValue('contact', details.ret71);
      FormManager.setValue('rol', details.ret72);
    }

    var cemeaCountries = [ '358', '359', '363', '603', '607', '620', '626', '644', '642', '651', '668', '677', '680', '693', '694', '695', '699', '704', '705', '707', '708', '740', '741', '752',
        '762', '767', '768', '772', '787', '805', '808', '820', '821', '823', '826', '832', '849', '850', '865', '889' ];

    if (cemeaCountries.indexOf(FormManager.getActualValue('cmrIssuingCntry')) > -1) {
      FormManager.setValue('ierpSitePrtyId', details.ret53);
    }

    if (isLACountry()) {
      FilteringDropdown.val_city1 = details.ret9;
    }

    if (FormManager.getActualValue('cmrIssuingCntry') == '758') {
      if (FormManager.getActualValue('landCntry') == 'IT') {
        FormManager.setValue('stateProv', details.ret11);
        FilteringDropdown.val_stateProv = details.ret11;
        // FormManager.setValue('dropDownCity', details.ret9);
        // FilteringDropdown.val_dropDownCity = details.ret9;
        FormManager.setValue('city1', details.ret9);
      } else {
        FormManager.setValue('stateProvItaly', details.ret56);
        FilteringDropdown.val_stateProvItaly = details.ret56;
        FormManager.setValue('city1', details.ret9);
        FormManager.setValue('stateProvItalyOth', details.ret11);
      }
      if (FormManager.getActualValue('addrType') == 'ZP01' && cmr.currentRequestType == 'C') {
        FormManager.setValue('addrAbbrevName', details.ret15);
        FormManager.setValue('streetAbbrev', details.ret46);
        FormManager.setValue('addrAbbrevLocn', details.ret22);
      }
      if (FormManager.getActualValue('addrType') == 'ZP01' && cmr.currentRequestType == 'U') {
        FormManager.setValue('addrAbbrevName', details.ret15);
        FormManager.setValue('streetAbbrev', details.ret46);
        FormManager.setValue('addrAbbrevLocn', details.ret22);
      }

    }

    if (cmr.addressMode == 'copyAddress' && FormManager.getActualValue('cmrIssuingCntry') == '848') {
      FormManager.setValue('custLangCd', details.ret73);
    }

    // IERP: China specific address load
    if (FormManager.getActualValue('cmrIssuingCntry') == '641' && (cmr.addressMode == 'copyAddress' || cmr.addressMode == 'updateAddress' || cmr.addressMode == 'removeAddress')) {
      FormManager.setValue('cnCustName1', details.ret59);
      cmr.oldcncustname = details.ret59;
      FormManager.setValue('cnCustName2', details.ret60);
      cmr.oldcncustname2 = details.ret60;
      FormManager.setValue('cnAddrTxt2', details.ret61);
      cmr.oldcnaddrtxt2 = details.ret61;
      FormManager.setValue('cnAddrTxt', details.ret62);
      cmr.oldcnaddrtxt = details.ret62;
      FormManager.setValue('cnCity', details.ret63);
      FilteringDropdown.val_cnCity = details.ret63;
      FormManager.setValue('cnDistrict', details.ret64);
      cmr.oldcndistrict = details.ret64;
      FormManager.setValue('cnCustContNm', details.ret65);
      FormManager.setValue('cnCustContJobTitle', details.ret66);
      FormManager.setValue('custPhone', details.ret23);
      FormManager.setValue('cnCustName3', details.ret73);
      cmr.oldcncustname3 = details.ret73;

      FilteringDropdown.val_landCntry = details.ret13;

      if (details.ret13 == 'CN') {
        FormManager.setValue('dropdowncity1', details.ret9);
        FilteringDropdown.val_dropdowncity1 = details.ret9;
      } else {
        FormManager.setValue('city1', details.ret9);
      }

    }

    storeTgmeCompare(false, details);

    if (cmr.addressMode == 'updateAddress') {

      FormManager.setValue('addr_dplChkResult', details.ret32);
      var dplText = details.ret32 == 'P' ? 'Passed' : (details.ret32 == 'F' ? 'Failed' : 'Not Done');
      dojo.byId('dplChkResultEdit').innerHTML = dplText;
      dojo.byId('dplChkInfoEdit').innerHTML = details.ret33;
      dojo.byId('dplChkTsEdit').innerHTML = details.ret42 ? details.ret42 : '';
      dojo.byId('dplChkByEdit').innerHTML = details.ret41 ? details.ret41 + ' (' + details.ret40 + ')' : '';
      dojo.byId('dplChkErrEdit').innerHTML = details.ret43 ? details.ret43 : '';
      FormManager.setValue('dplChkInfo', details.ret33);
      if ('Y' == details.ret31) {
        FormManager.setValue('importInd', 'Y');
        // FormManager.removeValidator('sapNo',
        // Validators.DB('DB-SAPR3.KNA1',{mandt : cmr.MANDT}));
        if (FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') {
          // set to readOnly instead of disabled
          FormManager.readOnly('sapNo');
          FormManager.readOnly('ierpSitePrtyId');
        }
        cmr.showNode('cmr-findcmr-record');
        cmr.hideNode('cmr-dnb-record');
      } else {
        if ('D' == details.ret31) {
          FormManager.setValue('importInd', 'D');
          cmr.hideNode('cmr-findcmr-record');
          cmr.showNode('cmr-dnb-record');
        } else {
          FormManager.setValue('importInd', 'N');
          cmr.hideNode('cmr-findcmr-record');
          cmr.hideNode('cmr-dnb-record');
        }
        // FormManager.enable('sapNo');
        // FormManager.enable('ierpSitePrtyId');
        FormManager.readOnly('sapNo');
        FormManager.readOnly('ierpSitePrtyId');
        // FormManager.addValidator('sapNo',
        // Validators.DB('DB-SAPR3.KNA1',{mandt : cmr.MANDT}), [ 'SAP Number
        // (KUNNR)' ], null);
      }
    } else if (cmr.addressMode == 'copyAddress') {

      // if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ITALY) {
      // if (FormManager.getActualValue('addrType') != '' &&
      // FormManager.getActualValue('addrType') == 'ZP01') {
      // FormManager.show('BillingPstlAddr', 'billingPstlAddr');
      // FormManager.setValue('billingPstlAddr', details.ret58);
      // } else {
      // FormManager.clearValue('billingPstlAddr');
      // FormManager.hide('BillingPstlAddr', 'billingPstlAddr');
      // }
      // }

      FormManager.setValue('importInd', 'N');
      // FormManager.enable('sapNo');
      // FormManager.enable('ierpSitePrtyId');
      FormManager.readOnly('sapNo');
      FormManager.readOnly('ierpSitePrtyId');
      // FormManager.addValidator('sapNo', Validators.DB('DB-SAPR3.KNA1',{mandt
      // : cmr.MANDT}), [ 'SAP Number (KUNNR)' ], null);
      FormManager.setValue('addrStdAcceptInd', '');
      FormManager.setValue('addr_addrStdResult', '');
      FormManager.setValue('addr_addrStdRejReason', '');
      FormManager.setValue('addr_addrStdRejCmt', '');
      FormManager.setValue('addrStdTsString', '');
      dojo.query('#addEditAddressModal #addrStdRejReason_modal')[0].innerHTML = '';
      dojo.query('#addEditAddressModal #addrStdResult_modal')[0].innerHTML = 'Not Required';
      dojo.query('#addEditAddressModal #addrStdTsString_modal')[0].innerHTML = '';
    }
    if (cmr.addressMode == 'copyAddress') {
      cmr.setModalTitle('addEditAddressModal', 'Copy an Address');
    } else {
      cmr.setModalTitle('addEditAddressModal', 'Update an Address');
    }
    dojo.byId('addressBtn').value = 'Save Address';

    if (cmr.addressMode == 'updateAddress') {
      var rdcaddr = cmr.getRecord('ADDR_RDC', 'AddrRdc', {
        REQ_ID : FormManager.getActualValue('reqId'),
        ADDR_TYPE : details.ret2,
        ADDR_SEQ : details.ret3,
        MANDT : cmr.MANDT
      });
      populateAddressDeltaInd(rdcaddr, false);
    }

    if (dojo.byId('addrCreateDt_updt')) {
      dojo.byId('addrCreateDt_updt').innerHTML = cmr.addressMode == 'updateAddress' ? (details.ret48 == '' ? '-' : details.ret48) : '-';
    }
    if (dojo.byId('addrUpdateDt_updt')) {
      dojo.byId('addrUpdateDt_updt').innerHTML = cmr.addressMode == 'updateAddress' ? (details.ret49 == '' ? '-' : details.ret49) : '-';
    }

  }
  var requestType = FormManager.getActualValue('reqType');
  var importInd = FormManager.getActualValue('importInd');
  // apply template for all create scenarios, and for updates with manually
  // inputted data
  // for updates, apply to newly created addresses only
  if ((requestType != 'U' && requestType != 'X') || ((requestType == 'U' || requestType == 'X') && (importInd == '' || importInd == 'N'))) {
    var template = TemplateService.getCurrentTemplate();
    if (template) {
      TemplateService.loadAddressTemplate(template, 'reqentry', FormManager.getActualValue('addrType'));
    }
  }

  if (typeof (GEOHandler) != 'undefined') {
    GEOHandler.executeAddrFuncs(false);
  }
  // if (cmr.addressMode == 'newAddress') {
  // FilteringDropdown['val_landCntry'] = 'US';
  // FormManager.setValue('landCntry', 'US');
  // } else {
  // FilteringDropdown['val_landCntry'] = null;
  // }

}

/**
 * Populate the delta indicators for address fields
 * 
 * @param rdcaddr
 * @param copyMode
 */
function populateAddressDeltaInd(rdcaddr, copyMode) {
  if (rdcaddr && rdcaddr.id) {
    rdcaddr.addrSeq = rdcaddr.id.addrSeq;
    rdcaddr.addrType = rdcaddr.id.addrType;
  }
  var imgs = dojo.query('#addEditAddressModal img.cmr-delta-icon');
  if (imgs && imgs.length > 0) {
    for (var i = 0; i < imgs.length; i++) {
      var id = imgs[i].id;
      var code = imgs[i].getAttribute('coded');
      var val = '';
      if (id && id.indexOf('delta') == 0) {
        if (copyMode) {
          imgs[i].title = 'Old Value : -';
          imgs[i].style.display = 'none';
        } else {
          id = id.substring(6);
          if (rdcaddr[id] != null) {
            if ('' == rdcaddr[id].trim()) {
              imgs[i].title = 'Old Value : -';
            } else {
              imgs[i].title = 'Old Value : ' + rdcaddr[id].trim();
            }
          } else {
            imgs[i].title = 'Old Value : -';
          }
          val = rdcaddr[id] != null ? rdcaddr[id].trim() : '';
          if (code != null && val != null && val.trim().length > 0 && val.indexOf('-') > 0) {
            if (code == 'L') {
              val = val.substring(0, val.indexOf('-')).trim();
            } else if (code == 'R') {
              val = val.substring(val.indexOf('-') + 1).trim();
            }
          }
          if (rdcaddr.importInd != 'Y' && rdcaddr.importInd != 'D') {
            imgs[i].style.display = 'none';
          } else {
            if (val != FormManager.getActualValue(id)) {
              imgs[i].style.display = '';
            } else {
              imgs[i].style.display = 'none';
            }
          }
        }
      }
    }
  }
}

/**
 * Executed when the add/edit address modal is closed
 */
function addEditAddressModal_onClose() {
  if (cmr.currentRequestType) {
    FormManager.setValue('reqType', cmr.currentRequestType);
  }
  cmr.noCreatePop = 'N';
  if (FormManager.getActualValue('cmrIssuingCntry') == 631) {
    // 1164561
    FormManager.clearValue('taxCd1');
    // 1164558
    FormManager.clearValue('taxCd2');
    // 1164561
    FormManager.clearValue('vat');
  }
  if (cmr.addressSaved && (typeof (GEOHandler) != 'undefined')) {
    if (GEOHandler.isCopyAddressEnabled()) {
      cmr.showConfirm('showApplyChangesModal()', 'Do you want to copy the address data to other addresses?', 'Confirm', 'noCopyAddress()', {
        OK : 'Yes',
        CANCEL : 'No'
      });
    }
  }

}

/**
 * Selects the standard city name to use from the list
 */
function doSelectStdCity() {
  if (FormManager.getActualValue('stdcitynames') == '') {
    cmr.showAlert('Please select a city name from the list.');
    return;
  }
  var values = FormManager.getActualValue('stdcitynames').split(',');
  FormManager.setValue('city1', values[1].trim());
  FormManager.setValue('county', values[0].trim());
  FormManager.setValue('countyName', values[2].trim());
  cmr.hideModal('stdcityModal');
  actualAddToAddressList();
}

/**
 * Confirms the single standard city suggestion
 */
function doSelectStdCityName() {
  FormManager.setValue('city1', cmr.standardCity.standardCity);
  if (cmr.standardCity.standardCountyCd && cmr.standardCity.standardCountyCd != '') {
    FormManager.setValue('county', cmr.standardCity.standardCountyCd);
    FormManager.setValue('countyName', cmr.standardCity.standardCountyName);
  }
  cmr.hideModal('stdcitynameModal');
  actualAddToAddressList();
}

/**
 * Loads the choices for the standard city for selection
 */
function stdcityModal_onLoad() {
  var city = FormManager.getActualValue('city1');
  dojo.byId('currcityname').innerHTML = city;
  if (cmr.standardCity && cmr.standardCity.suggested && cmr.standardCity.suggested.length > 0) {
    var options = '';
    for (var i = 0; i < cmr.standardCity.suggested.length; i++) {
      options += '<option value="' + cmr.standardCity.suggested[i].code + ',' + cmr.standardCity.suggested[i].city + ',' + cmr.standardCity.suggested[i].name + '">'
          + cmr.standardCity.suggested[i].city + ', ' + cmr.standardCity.suggested[i].name + ' County</option>';
    }
    dojo.byId('stdcitynames').innerHTML = options;
  }
}

/**
 * Places the single-result city name suggestion on the modal
 */
function stdcitynameModal_onLoad() {
  var city = FormManager.getActualValue('city1');
  dojo.byId('currcityname2').innerHTML = city;
  dojo.byId('stdcityname').innerHTML = cmr.standardCity.standardCity;
}

/**
 * Opens the Address Details screen
 * 
 * @param AddrType
 */
function doCopyAddr(reqId, addrType, addrSeq, mandt, name, type) {
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : addrType,
    ADDR_SEQ : addrSeq,
    MANDT : mandt,
  };
  var result = cmr.query('ADDRDETAIL', qParams);
  cmr.addrdetails = result;
  cmr.addressMode = 'copyAddress';
  cmr.showModal('addEditAddressModal');
}

/**
 * Opens the Address Details screen for update
 * 
 * @param AddrType
 */
function doUpdateAddr(reqId, addrType, addrSeq, mandt, skipDnb) {
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : addrType,
    ADDR_SEQ : addrSeq,
    MANDT : mandt,
  };
  var result = cmr.query('ADDRDETAIL', qParams);
  cmr.addrdetails = result;
  cmr.addressMode = 'updateAddress';
  if (result && result.ret31 == 'D' && !skipDnb) {
    cmr
        .showConfirm(
            'continueEditDnbAddress()',
            'This is an address imported from D&B. Modifying the Name, Street, City, State, and/or Postal Code information can cause company checks to fail. The request can also be potentially <strong>rejected</strong>.<br>Updates to other fields will not affect the checks.<br><br>Proceed?',
            'D&B Address', null, {
              OK : 'Proceed',
              CANCEL : 'Don\'t Edit'
            });
  } else {
    cmr.showModal('addEditAddressModal');
  }
}

function continueEditDnbAddress() {
  cmr.showModal('addEditAddressModal');
  // CMR-2383
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    if (addrType == 'ZS01' || addrType == 'ZD01' || addrType == 'ZI01') {
      FormManager.disable('taxOffice');
      dojo.byId('ast-taxOffice').style.display = 'none';
    } else if (addrType == 'ZP01') {
      dojo.byId('ast-taxOffice').style.display = 'inline-block';
    }
  }
}

/**
 * Remove one address from the list via the remove button
 */
function doRemoveAddr(reqId, addrType, addrSeq, mandt, name, type) {

  cmr.removeDetails = {
    remReqId : reqId,
    remAddrType : addrType,
    remAddrSeq : addrSeq,
    remMandt : mandt,
    remName : name,
    remType : type
  };
  cmr.showConfirm('actualRemoveAddr()', 'Remove <strong>' + type + '</strong> address for Customer <strong>' + name + '</strong>?');

}

/**
 * Opens the Address Details screen
 * 
 * @param AddrType
 */
function actualRemoveAddr() {
  cmr.addressMode = 'removeAddress';
  FormManager.setValue('reqId', cmr.removeDetails.remReqId);
  FormManager.setValue('addrType', '');
  FormManager.setValue('saveAddrType', '');
  FormManager.setValue('remAddrType', cmr.removeDetails.remAddrType);
  FormManager.setValue('addrSeq', cmr.removeDetails.remAddrSeq);
  cmr.addrReqId = FormManager.getActualValue('reqId');
  FormManager.doHiddenAction('frmCMR_addressModal', 'REMOVE_ADDRESS', cmr.CONTEXT_ROOT + '/request/address/process.json?reqId=' + cmr.addrReqId, true, refreshAddressAfterResult, false);
  if (FormManager.getActualValue('countryUse').substring(0, 3) == '706' && FormManager.getActualValue('custSubGrp') != '' && cmr.removeDetails.remAddrType == 'ZS01') {
    FormManager.setValue('taxCd2', '');
    FormManager.readOnly('taxCd2');
    console.log(">>>> TaX Code Disabled beacuse it's value needs to be based upon installing address");
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == '706' && cmr.removeDetails.remAddrType == 'ZD02') {
    resetAbbNmOnActualRemoveAddrFR();
  }
  // Defect 1579880: PP: On removing installing address, abbrv name not becoming
  // blank and value is retained
  if (FormManager.getActualValue('cmrIssuingCntry') == '758' && cmr.removeDetails.remAddrType == 'ZS01') {
    setBlankAbbrevNmLocationIT();
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == '649' && cmr.removeDetails.remAddrType == 'ZS01') {
    clearCATaxFields();
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == '678' || FormManager.getActualValue('cmrIssuingCntry') == '702' || FormManager.getActualValue('cmrIssuingCntry') == '806'
      || FormManager.getActualValue('cmrIssuingCntry') == '846') {
    if (cmr.removeDetails.remAddrType == 'ZI01') {
      resetAbbNmRemoveAddrNORDX();
    }
  }
}

function setBlankAbbrevNmLocationIT() {
  console.log(">>> setBlankAbbrevNmLocationIT >> In address JS");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (role != 'REQUESTER') {
    return;
  }
  FormManager.setValue('abbrevNm', '');
  FormManager.setValue('abbrevLocn', '');
}

function dropdownval_transportZone() {
  return 'Z000000001';
}

/**
 * Saves the scorecard value after address standardization had been performed
 */
var SetAddrStdResult = function() {
  var scoreCardUrl = cmr.CONTEXT_ROOT + '/request/scorecard.json';
  dojo.xhrGet({
    url : scoreCardUrl,
    handleAs : 'json',
    method : 'GET',
    timeout : 50000,
    load : function(data, ioargs) {

      var addrStdresult = data.result;
      dojo.byId('addrstdResultDisplay').innerHTML = addrStdresult;

      var today = new Date();
      var dateString = today.format("yyyy-MM-dd");

      if (addrStdresult == 'Not Done' || addrStdresult == null || addrStdresult == '') {
        dojo.byId('addrstdResultDateDisplay').innerHTML = null;
      }
      ;
      if (addrStdresult == 'Completed' || addrStdresult == 'Completed with Issues') {
        dojo.byId('addrstdResultDateDisplay').innerHTML = dateString;
      }
      ;
    },
    error : function(error, ioargs) {
    }
  });
};

/**
 * Executes the DPL check
 */
function doDplCheck() {
  try {
    var count = CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount;
    if (count == 0) {
      cmr.showAlert('There are no address records under this request.');
      return;
    }
  } catch (e) {

  }
  if (typeof (FilteringDropdown) != 'undefined' && FilteringDropdown.pending()) {
    cmr.showAlert('Please wait a while for the page to completely load then run DPL Check again.');
    return;
  }
  cmr.showConfirm('doRunDpl()', 'DPL Check will be performed on the addresses on the list. Proceed?');
}

/**
 * Opens the DPL details for Failed results
 * 
 * @param reqId
 * @param addrType
 * @param addrSeq
 */
function openDplDetails(reqId, addrType, addrSeq) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var dplDetails = 'DPLDETAILS';
  if ((typeof (GEOHandler) != 'undefined')) {
    if (GEOHandler.customerNamesOnAddress(cntry)) {
      dplDetails = 'DPLDETAILS_BYADDR';
    }
  }
  var result = cmr.getRecord(dplDetails, 'DplChkDetails', {
    REQ_ID : reqId,
    ADDR_SEQ : addrSeq,
    ADDR_TYPE : addrType,
    MANDT : cmr.MANDT
  });
  if (!result) {
    cmr.showAlert('The details cannot be shown at the moment. Please contact your system administrator.');
  }
  var chkTs = '';
  if (result && result.dplChkTs > 0) {
    chkTs = cmr.formatDate(result.dplChkTs, 'T');
  }
  dojo.byId('dpl_result').innerHTML = result.dplChkResult ? result.dplChkResult : '&nbsp;';
  dojo.byId('dpl_errorlist').innerHTML = result.dplChkErrList ? result.dplChkErrList : '&nbsp;';
  dojo.byId('dpl_ts').innerHTML = chkTs;
  dojo.byId('dpl_by').innerHTML = result.dplChkById ? result.dplChkByNm + ' (' + result.dplChkById + ')' : '';

  dojo.byId('dpl_custnm').innerHTML = result.custNm1 ? result.custNm1 : '&nbsp;';
  dojo.byId('dpl_cntry').innerHTML = result.landCntry ? result.landCntry : '&nbsp;';

  cmr.showModal('DplDetailsModal');
}

/**
 * Executed when the modal for the failed DPL details is opened
 */
function DplDetailsModal_onLoad() {
  // noop
}

/**
 * Does the actual DPL check
 */
function doRunDpl() {
  var reqId = FormManager.getActualValue('reqId');
  var url = cmr.CONTEXT_ROOT + '/dpl/' + reqId + '.json';
  FormManager.doHiddenAction('frmCMR_addressModal', 'DPL', url, true, refreshAfterDpl, false, 1000 * 60 * 3);
}

/**
 * Refresh the result (or the whole page) after DPL check is performed
 * 
 * @param result
 */
function refreshAfterDpl(result) {
  if (result.success) {
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    FormManager.setValue('dplMessage', result.message);
    FormManager.doAction('frmCMR', 'DPL', true);
  }
}

/* UKI + EMEA Additions */

/**
 * Shows the modal for Copy Data
 */
function showApplyChangesModal() {
  cmr.showModal('applyAddrChangesModal');
}

/**
 * When the Copy Data address is loaded, initialize the available types to copy
 * TO
 */
function applyAddrChangesModal_onLoad() {
  cmr.currentModalId = 'applyAddrChangesModal';
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var addressDesc = null;
  var types = cmr.query('GET_ADDRESS_TYPES', {
    CNTRY : cntry,
    _qall : 'Y'
  });
  if (types && types.length > 0) {
    var choices = '';
    var type = null;
    var useCntry = false;
    var cEECountries = [ '644', '668', '693', '704', '708', '740', '820', '821', '826', '358', '359', '363', '603', '607', '626', '651', '694', '695', '699', '705', '707', '787', '741', '889' ];
    document.getElementById('copy_createOnly').value = '';
    for (var i = 0; i < types.length; i++) {
      type = types[i];
      if (useCntry && type.ret3 != cntry) {
        break;
      }

      // update TR
      // Rollback TR change
      /*
       * Removed for Cyprus if ((SysLoc.CYPRUS == cntry) && reqType == 'C' &&
       * type.ret1 == 'ZD01') { break; }
       */

      // update For TR
      if (SysLoc.TURKEY == cntry && type.ret1 == 'ZP01') {
        if (FormManager.getActualValue('custGrp') == 'CROSS' && FormManager.getActualValue('addrType') == 'ZS01') {
          continue;
        }
      }

      if (SysLoc.TURKEY == cntry && type.ret1 == 'ZS01') {
        if (FormManager.getActualValue('custGrp') == 'CROSS' && FormManager.getActualValue('addrType') == 'ZP01') {
          continue;
        }
      }

      if (SysLoc.GREECE == cntry && type.ret1 == 'ZP01') {
        if (FormManager.getActualValue('custGrp') == 'LOCAL') {
          continue;
        } else if (FormManager.getActualValue('reqType') == 'U' && FormManager.getActualValue('landCntry') == 'GR') {
          continue;
        } else if (FormManager.getActualValue('custGrp') == 'CROSS' && FormManager.getActualValue('addrType') == 'ZS01') {
          continue;
        }
      }

      if (SysLoc.GREECE == cntry && type.ret1 == 'ZS01') {
        if (FormManager.getActualValue('custGrp') == 'CROSS' && FormManager.getActualValue('addrType') == 'ZP01') {
          continue;
        }
      }

      if (SysLoc.CANADA == cntry && [ 'ZD02', 'ZP08', 'ZP04', 'ZP05', 'ZE01', 'ZP06', 'ZP09' ].includes(type.ret1)) {
        continue;
      }

      if (SysLoc.ISRAEL == cntry && type.ret1 == 'CTYC') {
        var countShipping = 0;
        for (var a = 0; a < _allAddressData.length; a++) {
          if (_allAddressData[a].addrType[0] == 'ZD01') {
            countShipping++;
          }
        }

        if ((reqType == 'C' && countShipping == 0) || (reqType == 'U' && countShipping > 1)) {
          continue;
        }
      }

      if (SysLoc.ISRAEL == cntry && reqType == 'U'
          && (FormManager.getActualValue('addrType') == 'ZD01' || FormManager.getActualValue('addrType') == 'ZS01' || FormManager.getActualValue('addrType') == 'ZP01') && type.ret1 == 'ZD01') {
        if (cmr.addressMode == 'newAddress' && FormManager.getActualValue('addrType') == 'ZD01') {
          continue;
        } else if (cmr.addressMode == 'updateAddress') {
          var countShipping = 0;
          var countCtyc = 0;
          for (var a = 0; a < _allAddressData.length; a++) {
            if (_allAddressData[a].addrType[0] == 'ZD01') {
              countShipping++;
            } else if (_allAddressData[a].addrType[0] == 'CTYC') {
              countCtyc++;
            }
          }
          if (countShipping != countCtyc) {
            continue;
          }
        }
      }

      if (type.ret3 == cntry) {
        useCntry = true;
      }

      // for updates, check here if there are multiple instances of the same
      // addresses
      var single = true;
      if ((reqType == 'U' || reqType == 'X') && !GEOHandler.canCopyAddressType(type.ret1) && typeof (_allAddressData) != 'undefined') {
        var count = 0;
        console.log('checking instances of address type ' + type.ret1);
        for (var j = 0; j < _allAddressData.length; j++) {
          if (type.ret1 == _allAddressData[j].addrType[0]) {
            count++;
          }
        }
        console.log('count: ' + count);
        if (count > 1) {
          single = false;
          var curVal = document.getElementById('copy_createOnly').value;
          curVal += curVal != '' ? '|' : '';
          curVal += type.ret1;
          document.getElementById('copy_createOnly').value = curVal;
        }
      }

      var cntryRegion = FormManager.getActualValue('countryUse');
      if ((cntry == '702' || cntry == '846' || cntry == '678' || cntry == '806') && (FormManager.getActualValue('reqType') == 'C' || FormManager.getActualValue('reqType') == 'U')) {

        if (SysLoc.FINLAND == cntry && cntryRegion == '702') {
          // if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType)
          // {
          // if (type.ret2 != 'Shipping' && type.ret2 != 'Installing
          // (Additional)') {
          // choices += '<input type="checkbox" name="copyTypes" value ="' +
          // type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2
          // + '</label><br>';
          // }
          // } else {
          // if (type.ret2 != 'Shipping' && type.ret2 != 'Installing
          // (Additional)') {
          // choices += '<input type="checkbox" name="copyTypes" value ="' +
          // type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2
          // + ' (copy only if others exist)</label><br>';
          // addressDesc = type.ret2;
          // }
          //
          // }
          if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
            if (type.ret2 != 'Installing (Additional)') {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
            }
          } else {
            if (type.ret2 != 'Installing (Additional)') {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
              addressDesc = type.ret2;
            }
          }
        } else {
          if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
            if (type.ret2 != 'Installing (Additional)') {
              if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
                choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
              } else {
                choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
              }
            }
          } else {
            if (type.ret2 != 'Installing (Additional)') {
              if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
                choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
              } else {
                choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
              }
              addressDesc = type.ret2;
            }

          }

        }

      } else if (SysLoc.FINLAND == cntry && cntryRegion == '702') {
        // if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
        // if (type.ret2 != 'Shipping') {
        // if (reqType != 'C' && typeof (GEOHandler) != 'undefined' &&
        // !GEOHandler.canCopyAddressType(type.ret1) && !single) {
        // choices += '<input type="checkbox" name="copyTypes" value ="' +
        // type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '
        // (create additional only)</label><br>';
        // } else {
        // choices += '<input type="checkbox" name="copyTypes" value ="' +
        // type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 +
        // '</label><br>';
        // }
        // }
        // } else {
        // if (type.ret2 != 'Shipping') {
        // if (reqType != 'C' && typeof (GEOHandler) != 'undefined' &&
        // !GEOHandler.canCopyAddressType(type.ret1) && !single) {
        // choices += '<input type="checkbox" name="copyTypes" value ="' +
        // type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '
        // (create additional only)</label><br>';
        // } else {
        // choices += '<input type="checkbox" name="copyTypes" value ="' +
        // type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '
        // (copy only if others exist)</label><br>';
        // }
        // addressDesc = type.ret2;
        // }
        // }
        if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
          if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
            choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
          } else {
            choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
          }
        } else {
          if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
            choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
          } else {
            choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
          }
          addressDesc = type.ret2;
        }
      } else if (cntry == '788') {
        if (type.ret1 != 'ZKVK' && type.ret1 != 'ZVAT') {
          var requestingLob = FormManager.getActualValue('requestingLob');
          var reqReason = FormManager.getActualValue('reqReason');
          if ((requestingLob != 'IGF' || reqReason != 'IGF') && type.ret1 == 'ZP02') {
            continue;
          }
          if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
            if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
            } else {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
            }
          } else {
            if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
            } else {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
            }
            addressDesc = type.ret2;
          }
        }
      } else if (cntry == '760') {
        // 1743139 - remove ZC01/ZE01
        // 1791691 - remove ADU 5,8,9 from address list
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (!(reqType == 'U' && (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'INTER') && (type.ret1 == 'ZC01' || type.ret1 == 'ZE01'))) {
          if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
            choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
          } else if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
            choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
          } else {
            choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
            addressDesc = type.ret2;
          }
        }
      } else if (cntry == '848') {
        var reqReason = FormManager.getActualValue('reqReason');
        if ((type.ret1 == 'ZP02' || type.ret1 == 'ZD02') && (reqReason != 'IGF' || !isZD01OrZP01ExistOnCMR(type.ret1))) {
          continue;
        }
        if (type.ret1 != 'ZS02') {
          var reqType = FormManager.getActualValue('reqType');
          if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
            if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
            } else {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
            }
          } else {
            if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
            } else {
              choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
            }
            addressDesc = type.ret2;
          }
        }
      } else if (cntry == '618') {
        var reqReason = FormManager.getActualValue('reqReason');
        if ((type.ret1 == 'ZP02' || type.ret1 == 'ZD02') && (reqReason != 'IGF' || !isZD01OrZP01ExistOnCMR())) {
          continue;
        }
        if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
        } else if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
        } else {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
          addressDesc = type.ret2;
        }
      } else if (cntry == '724') {
        var reqReason = FormManager.getActualValue('reqReason');
        if ((type.ret1 == 'ZP02' || type.ret1 == 'ZD02') && (reqReason != 'IGF' || !isZD01OrZP01ExistOnCMR(type.ret1))) {
          continue;
        }
        if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
        } else if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
        } else {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
          addressDesc = type.ret2;
        }
      } else if (cEECountries.indexOf(cntry) > -1) {
        var reqReason = FormManager.getActualValue('reqReason');
        if ((type.ret1 == 'ZP03' || type.ret1 == 'ZD02') && (reqReason != 'IGF' || !isZD01OrZP01ExistOnCMR(type.ret1))) {
          continue;
        }
        if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
        } else if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
        } else {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
          addressDesc = type.ret2;
        }
      } else if (cntry == '706') {
        var reqReason = FormManager.getActualValue('reqReason');
        if ((type.ret1 == 'ZP02' || type.ret1 == 'ZD02') && (reqReason != 'IGF' || !isZD01OrZP01ExistOnCMR(type.ret1))) {
          continue;
        }
        if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
        } else if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
        } else {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
          addressDesc = type.ret2;
        }
      } else {
        if (reqType != 'C' && typeof (GEOHandler) != 'undefined' && !GEOHandler.canCopyAddressType(type.ret1) && !single) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (create additional only)</label><br>';
        } else if (cmr.currentAddressType && type.ret1 != cmr.currentAddressType) {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + '</label><br>';
        } else {
          choices += '<input type="checkbox" name="copyTypes" value ="' + type.ret1 + '"><label class="cmr-radio-check-label">' + type.ret2 + ' (copy only if others exist)</label><br>';
          addressDesc = type.ret2;
        }
      }
    }
    if (addressDesc) {
      cmr.setModalTitle('applyAddrChangesModal', 'Copy ' + addressDesc + ' Address Data');
    }
    FormManager.setValue('copy_addrType', cmr.currentAddressType);
    FormManager.setValue('copy_addrSeq', cmr.currentAddressSeq);
    dojo.byId('applyAddressTypesChoices').innerHTML = choices;
  }
}
/**
 * Perform the backend process of copying data
 */
function copyAddressData() {
  var options = document.getElementsByName('copyTypes');
  var trgType = 'ZS01';
  if (typeof (GEOHandler) != 'undefined') {
    trgType = GEOHandler.getAddressTypeForName();
  }
  console.log('Target Address Type: ' + trgType);
  var hasZS01 = false;
  var targetCopyTypes = new Array();
  if (options && options.length > 0) {
    var checked = false;
    options.forEach(function(option, i) {
      if (option.checked) {
        checked = true;
        targetCopyTypes.push(option.value);
        if (option.value == trgType) {
          hasZS01 = true;
        }
      }
    });

    if (typeof (GEOHandler) != 'undefined') {
      var msg = GEOHandler.validateCopy(cmr.currentAddressType, targetCopyTypes);
      if (msg) {
        cmr.showAlert(msg);
        return;
      }
    }
    if (!checked) {
      cmr.showAlert('Please select at least one address type to copy to.');
      return;
    }
  }
  if (hasZS01) {
    updateMainCustomerNames('', '', '', true, true);
  }
  if (typeof (GEOHandler) != 'undefined') {
    GEOHandler.executeAddrFuncs(true, false, true);
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL && targetCopyTypes.includes('ZD01')) {
    showConfirmCopyShippingIL();
  } else {
    FormManager.doHiddenAction('frmCMRCopyAddrChanges', 'COPY_DATA', cmr.CONTEXT_ROOT + '/request/address/copydata.json', true, refreshAfterAddressCopy, true);
  }
}

function showConfirmCopyShippingIL() {
  var reqType = FormManager.getActualValue('reqType');
  var countShipping = 0;
  for (var a = 0; a < _allAddressData.length; a++) {
    if (_allAddressData[a].addrType[0] == 'ZD01') {
      countShipping++;
    }
  }
  if (('C' == reqType && countShipping == 0) || ('U' == reqType && countShipping > 1)) {
    cmr.showConfirm('copyDoHiddenAction()', 'Country Use C (Shipping) is required to be created after copying Shipping address.');
  } else {
    copyDoHiddenAction();
  }
}

function copyDoHiddenAction() {
  FormManager.doHiddenAction('frmCMRCopyAddrChanges', 'COPY_DATA', cmr.CONTEXT_ROOT + '/request/address/copydata.json', true, refreshAfterAddressCopy, true);
}

/**
 * Cancels the copy address modal. When this is invoked, system will rerun the
 * refresh function from the original address save
 */
function cancelCopyAddress() {
  cmr.hideModal('applyAddrChangesModal');
  if (cmr.currentAddressResult) {
    refreshAddressAfterResult(cmr.currentAddressResult, true, true);
  }
}

/**
 * Invoked when 'No' is selected on the copy confirm. When this is invoked,
 * system will rerun the refresh function from the original address save
 */
function noCopyAddress() {
  if (cmr.currentAddressResult) {
    refreshAddressAfterResult(cmr.currentAddressResult, true, true);
  }
}

/**
 * After copying the data, always refresh page to trigger DPL recalculation
 * 
 * @param result
 */
function refreshAfterAddressCopy(result) {
  if (result.success) {
    FormManager.setValue('dplMessage', result.message);
    FormManager.setValue('showAddress', 'Y');
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.setValue('mainAddrType', 'ZS01');
    }
    if (cmr.currentRequestType) {
      FormManager.setValue('reqType', cmr.currentRequestType);
    }
    cmr.noCreatePop = 'N';
    // enable all checkboxes
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    FormManager.doAction('frmCMR', 'DPL', true);
  } else {
    dojo.query('#dialog_applyAddrChangesModal div.dijitDialogPaneContent')[0].scrollTo(0, 0);
  }

  // call the addr std result calculation for score card here..
  SetAddrStdResult();
  
  // Ensure quick search parameter is preserved after reloading
  var isFromQuickSearch = new URLSearchParams(location.search).get('qs');
  dojo.cookie('qs', isFromQuickSearch ? isFromQuickSearch : 'N');
}

function removeSelectedAddresses() {
  FormManager.gridHiddenAction('frmCMR', 'REMOVE_ADDRESSES', cmr.CONTEXT_ROOT + '/request/address/process.json', true, refreshAfterAddressRemove, false, 'Remove selected address records?');
}

function refreshAfterAddressRemove(result) {
  if (result.success) {
    CmrGrid.refresh('ADDRESS_GRID');
  }

  // 1517472 - Have to wait 3 seconds so that to make sure refresh completed
  if (FormManager.getActualValue('cmrIssuingCntry') == '706') {
    window.setTimeout('resetAbbNmOnRemoveSelectedAddrsFR()', 3000);
  }

  // CREATCMR-2262
  if (FormManager.getActualValue('cmrIssuingCntry') == '678' || FormManager.getActualValue('cmrIssuingCntry') == '702' || FormManager.getActualValue('cmrIssuingCntry') == '806'
      || FormManager.getActualValue('cmrIssuingCntry') == '846') {
    // if (cmr.removeDetails.remAddrType == 'ZI01') {
    // resetAbbNmRemoveAddrNORDX();
    resetAbbNmOnRemoveSelectedAddrNORDX();
    // }
  }
  // CREATCMR-2262
  // CREATCMR-5447
  if (FormManager.getActualValue('cmrIssuingCntry') == '897') {
    resetSccInfo();
  }
  // CREATCMR-5447

}

/* Generalized Address Grid New Formatters */
function customerNameFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var custNm2 = rowData.custNm2;
  if (custNm2 && custNm2[0]) {
    return value + '<br>' + custNm2;
  }
  return value;
}

/* Generalized Address Grid New Formatters */
function streetValueFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var streetCont = rowData.addrTxt2;
  if (streetCont && streetCont[0]) {
    return value + '<br>' + streetCont;
  }
  return value;
}

function addrFormatterIcons(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var addrType = rowData.addrType;
  var addrSeq = rowData.addrSeq;
  var name = rowData.custNm1;
  if (name == null) {
    name = '';
  }
  name = name && name.length > 0 && name[0] != null ? name[0].replace(/\'/g, '') : '';
  var addrTypeTxt = rowData.addrTypeText;
  var mandt = FormManager.getActualValue('mandt');
  var actions = '';
  // for non-US, return all options
  var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
  if (FormManager.getActualValue('cmrIssuingCntry') != '897') {
    if (FormManager.getActualValue('cmrIssuingCntry') == '755') {
      name = name.replace(/"/g, '&quot;');
    }

    if (canCopyAddress(value, rowIndex, this.grid)) {
      actions += '<img src="' + imgloc + 'addr-copy-icon.png" class="addr-icon" title="Copy Address" onclick="doCopyAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt
          + '\',\'' + name + '\')">';
    }
    if (canUpdateAddress(value, rowIndex, this.grid)) {
      actions += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update Address" onclick="doUpdateAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt
          + '\')">';
    }
    if (canRemoveAddress(value, rowIndex, this.grid)) {
      actions += '<img src="' + imgloc + 'addr-remove-icon.png" class="addr-icon" title="Remove Address" onclick="doRemoveAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt
          + '\', \'' + name + '\', \'' + addrTypeTxt + '\')">';
    }
    return actions;
  }
  if (addrType == 'ZS01' || addrType == 'ZI01' || addrType == 'ZP01') {
    if (canCopyAddress(value, rowIndex, this.grid)) {
      actions += '<img src="' + imgloc + 'addr-copy-icon.png" class="addr-icon" title="Copy Address" onclick="doCopyAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt
          + '\',\'' + name + '\')">';
    }
    if (canUpdateAddress(value, rowIndex, this.grid)) {
      actions += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update Address" onclick="doUpdateAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt
          + '\')">';
    }
    if (canRemoveAddress(value, rowIndex, this.grid)) {
      actions += '<img src="' + imgloc + 'addr-remove-icon.png" class="addr-icon" title="Remove Address" onclick="doRemoveAddr(\'' + reqId + '\',\'' + addrType + '\',\'' + addrSeq + '\',\'' + mandt
          + '\', \'' + name + '\', \'' + addrTypeTxt + '\')">';
    }
  }
  return actions;
}

function countryFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var desc = rowData.countryDesc;
  if (value) {
    if (desc && '' != desc) {
      return '<span class="cmr-grid-tooltip" title="' + desc + '">' + value + '</span>';
    } else {
      return value;
    }
  } else {
    return '';
  }
}

function stateProvFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var desc = rowData.stateProvDesc;
  if (value) {
    if (desc && '' != desc) {
      return '<span class="cmr-grid-tooltip" title="' + desc + '">' + value + '</span>';
    } else {
      return value;
    }
  } else {
    return '';
  }
}

// dtn: added general method for validations if country is LA
function isLACountry() {
  if (FormManager.getActualValue('cmrIssuingCntry') == '631' || FormManager.getActualValue('cmrIssuingCntry') == '781' || FormManager.getActualValue('cmrIssuingCntry') == '613'
      || FormManager.getActualValue('cmrIssuingCntry') == '629' || FormManager.getActualValue('cmrIssuingCntry') == '655' || FormManager.getActualValue('cmrIssuingCntry') == '661'
      || FormManager.getActualValue('cmrIssuingCntry') == '663' || FormManager.getActualValue('cmrIssuingCntry') == '681' || FormManager.getActualValue('cmrIssuingCntry') == '683'
      || FormManager.getActualValue('cmrIssuingCntry') == '731' || FormManager.getActualValue('cmrIssuingCntry') == '735' || FormManager.getActualValue('cmrIssuingCntry') == '799'
      || FormManager.getActualValue('cmrIssuingCntry') == '811' || FormManager.getActualValue('cmrIssuingCntry') == '813' || FormManager.getActualValue('cmrIssuingCntry') == '815'
      || FormManager.getActualValue('cmrIssuingCntry') == '829' || FormManager.getActualValue('cmrIssuingCntry') == '869' || FormManager.getActualValue('cmrIssuingCntry') == '871') {
    return true;
  } else {
    return false;
  }
}

// dtn: added general method for validations if country is LA
function isSSACountry() {
  if (FormManager.getActualValue('cmrIssuingCntry') == '631' || FormManager.getActualValue('cmrIssuingCntry') == '781' || FormManager.getActualValue('cmrIssuingCntry') == '613'
      || FormManager.getActualValue('cmrIssuingCntry') == '629' || FormManager.getActualValue('cmrIssuingCntry') == '655' || FormManager.getActualValue('cmrIssuingCntry') == '661'
      || FormManager.getActualValue('cmrIssuingCntry') == '663' || FormManager.getActualValue('cmrIssuingCntry') == '681' || FormManager.getActualValue('cmrIssuingCntry') == '683'
      || FormManager.getActualValue('cmrIssuingCntry') == '731' || FormManager.getActualValue('cmrIssuingCntry') == '735' || FormManager.getActualValue('cmrIssuingCntry') == '799'
      || FormManager.getActualValue('cmrIssuingCntry') == '811' || FormManager.getActualValue('cmrIssuingCntry') == '813' || FormManager.getActualValue('cmrIssuingCntry') == '815'
      || FormManager.getActualValue('cmrIssuingCntry') == '829' || FormManager.getActualValue('cmrIssuingCntry') == '869' || FormManager.getActualValue('cmrIssuingCntry') == '871') {
    return true;
  } else {
    return false;
  }
}

// recalculate AbbrevNm when actual remove address for FR;
function resetAbbNmOnActualRemoveAddrFR() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  if (role != 'Requester') {
    return;
  }

  var abbrevNmValue = null;
  var cntryCd = null;
  var singleIndValue = null;
  var departmentNumValue = null;

  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZS01",
  };
  var abbrevNmResult = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
  abbrevNmValue = abbrevNmResult.ret1;

  var landCntryResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', qParams);
  cntryCd = landCntryResult.ret1;

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == "INTER" || custSubGrp == "CBTER") {
    if (cntryCd == "DZ") {
      departmentNumValue = "0371";
    } else if (cntryCd == "TN") {
      departmentNumValue = "0382";
    } else if (cntryCd == "YT" || cntryCd == "RE" || cntryCd == "VU") {
      departmentNumValue = "0381";
    } else if (cntryCd == "MQ") {
      departmentNumValue = "0385";
    } else if (cntryCd == "GP") {
      departmentNumValue = "0392";
    } else if (cntryCd == "GF" || cntryCd == "PM") {
      departmentNumValue = "0388";
    } else if (cntryCd == "NC" || cntryCd == "PF") {
      departmentNumValue = "0386";
    } else {
      departmentNumValue = FormManager.getActualValue('ibmDeptCostCenter');
    }
    singleIndValue = departmentNumValue;
  } else if (custSubGrp == "BPIEU" || custSubGrp == "BPUEU" || custSubGrp == "CBIEU" || custSubGrp == "CBUEU") {
    singleIndValue = "R5";
  } else if (custSubGrp == "CBTSO" || custSubGrp == "INTSO") {
    singleIndValue = "FM";
  } else if (custSubGrp == "CBIFF" || custSubGrp == "CBIFL" || custSubGrp == "CBFIN" || custSubGrp == "LCIFF" || custSubGrp == "LCIFL" || custSubGrp == "OTFIN") {
    singleIndValue = "F3";
  } else if (custSubGrp == "LEASE" || custSubGrp == "CBASE") {
    singleIndValue = "L3";
  } else {
    singleIndValue = "D3";
  }
  if (abbrevNmValue != null && abbrevNmValue.length > 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
    abbrevNmValue = abbrevNmValue.substring(0, 19);
  } else if (abbrevNmValue != null && abbrevNmValue.length < 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
    for (var i = abbrevNmValue.length; i < 19; i++) {
      abbrevNmValue += ' ';
    }
  } else if (abbrevNmValue != null && abbrevNmValue.length > 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
    abbrevNmValue = abbrevNmValue.substring(0, 17);
  } else if (abbrevNmValue != null && abbrevNmValue.length < 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
    for (var i = abbrevNmValue.length; i < 17; i++) {
      abbrevNmValue += ' ';
    }
  }

  if (singleIndValue != null && abbrevNmValue != null) {
    abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
    FormManager.setValue('abbrevNm', abbrevNmValue);
  }
}

// recalculate AbbrevNm after remove selected addresses for FR;
function resetAbbNmOnRemoveSelectedAddrsFR() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  if (role != 'Requester') {
    return;
  }

  var abbrevNmValue = null;
  var cntryCd = null;
  var singleIndValue = null;
  var departmentNumValue = null;

  var havingZS01 = false;
  var havingZD02 = false;
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    var record = null;
    var type = null;
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (record == null && _allAddressData != null && _allAddressData[i] != null) {
        record = _allAddressData[i];
      }
      type = record.addrType;
      if (typeof (type) == 'object') {
        type = type[0];
      }
      if (type == 'ZS01') {
        havingZS01 = true;
      }
      if (type == 'ZD02') {
        havingZD02 = true;
      }
    }
  }

  if (!havingZS01) {
    FormManager.setValue('abbrevNm', '');
    console.log("havingZS01 = " + havingZS01);
    return;
  }

  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZS01",
  };
  var abbrevNmResult = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
  abbrevNmValue = abbrevNmResult.ret1;

  var landCntryResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', qParams);
  cntryCd = landCntryResult.ret1;

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == "INTER" || custSubGrp == "CBTER") {
    if (cntryCd == "DZ") {
      departmentNumValue = "0371";
    } else if (cntryCd == "TN") {
      departmentNumValue = "0382";
    } else if (cntryCd == "YT" || cntryCd == "RE" || cntryCd == "VU") {
      departmentNumValue = "0381";
    } else if (cntryCd == "MQ") {
      departmentNumValue = "0385";
    } else if (cntryCd == "GP") {
      departmentNumValue = "0392";
    } else if (cntryCd == "GF" || cntryCd == "PM") {
      departmentNumValue = "0388";
    } else if (cntryCd == "NC" || cntryCd == "PF") {
      departmentNumValue = "0386";
    } else {
      departmentNumValue = FormManager.getActualValue('ibmDeptCostCenter');
    }
    singleIndValue = departmentNumValue;
  } else if (custSubGrp == "BPIEU" || custSubGrp == "BPUEU" || custSubGrp == "CBIEU" || custSubGrp == "CBUEU") {
    singleIndValue = "R5";
  } else if (custSubGrp == "CBTSO" || custSubGrp == "INTSO") {
    singleIndValue = "FM";
  } else if (custSubGrp == "CBIFF" || custSubGrp == "CBIFL" || custSubGrp == "CBFIN" || custSubGrp == "LCIFF" || custSubGrp == "LCIFL" || custSubGrp == "OTFIN") {
    singleIndValue = "F3";
  } else if (custSubGrp == "LEASE" || custSubGrp == "CBASE") {
    singleIndValue = "L3";
  } else {
    singleIndValue = "D3";
  }
  if (abbrevNmValue != null && abbrevNmValue.length > 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
    abbrevNmValue = abbrevNmValue.substring(0, 19);
  } else if (abbrevNmValue != null && abbrevNmValue.length < 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
    for (var i = abbrevNmValue.length; i < 19; i++) {
      abbrevNmValue += ' ';
    }
  } else if (abbrevNmValue != null && abbrevNmValue.length > 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
    abbrevNmValue = abbrevNmValue.substring(0, 17);
  } else if (abbrevNmValue != null && abbrevNmValue.length < 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
    for (var i = abbrevNmValue.length; i < 17; i++) {
      abbrevNmValue += ' ';
    }
  }

  if (havingZD02) {
    singleIndValue = 'DF';
  }

  if (singleIndValue != null && abbrevNmValue != null) {
    abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
    FormManager.setValue('abbrevNm', abbrevNmValue);
  }
}

function isCNAddressDetailsUpdated() {
  console.log(">>> RUNNING isCNAddressDetailsUpdated");
  var addrRecParams = {
    REQ_ID : reqId,
  };
  var addrDtlsResult = cmr.query('GET.ADDR_BY_ID_AND_DETAILS', addrRecParams);
}

/**
 * Called to validate if the CN address already exists
 * 
 * @returns {Boolean}
 */
function isCNAddressDetailsExisting() {
  console.log(">>> RUNNING isAddressDetailsExisting!!");
  var reqId = FormManager.getActualValue('reqId');
  var custNm1 = FormManager.getActualValue('custNm1');
  var custNm2 = FormManager.getActualValue('custNm2');
  var addrTxt = FormManager.getActualValue('addrTxt');
  var addrTxt2 = FormManager.getActualValue('addrTxt2');
  var landCntry = FormManager.getActualValue('landCntry');
  var city2 = FormManager.getActualValue('city2');
  var addrType = FormManager.getActualValue('addrType');
  var city1;

  if (landCntry == 'CN') {
    city1 = FormManager.getActualValue('dropdowncity1');
  } else {
    city1 = FormManager.getActualValue('city1');
  }

  var addrRecParams = {
    REQ_ID : reqId,
    ADDR_TYPE : 'ZS01',
    CUST_NM1 : custNm1,
    // CUST_NM2 : custNm2,
    ADDR_TXT : addrTxt,
    // ADDR_TXT_2 : addrTxt2,
    CITY1 : city1,
    // CITY2 : city2,
    LAND_CNTRY : landCntry
  };

  var addrDtlsResult = cmr.query('GET.ADDR_BY_ID_AND_DETAILS', addrRecParams);
  console.log(addrDtlsResult);

  var isExisting = false;

  if (addrDtlsResult.ret1 > 0 && addrType != 'ZS01') {
    isExisting = true;
  } else {
    isExisting = false;
  }

  return isExisting;

}

/**
 * Called to validate if the AP address already exists
 * 
 * @returns {Boolean}
 */

function isAPAddressDetailsExisting() {
  console.log(">>> RUNNING isAPAddressDetailsExisting!!");
  var reqType = null;
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var isExisting = false;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  // if (reqType == 'U' && rowData.importInd == 'Y') {
  // return;
  // }
  var custNm1 = FormManager.getActualValue('custNm1');
  var custNm2 = FormManager.getActualValue('custNm2');
  var city1 = FormManager.getActualValue('city1');
  var addrTxt = FormManager.getActualValue('addrTxt');
  var addrTxt2 = FormManager.getActualValue('addrTxt2');
  var stateProv = FormManager.getActualValue('stateProv');
  var landCntry = FormManager.getActualValue('landCntry');
  var dept = FormManager.getActualValue('dept');

  var addrRecParams = {
    REQ_ID : reqId,
    CUST_NM1 : custNm1,
    CUST_NM2 : custNm2,
    ADDR_TXT : addrTxt,
    ADDR_TXT_2 : addrTxt2,
    CITY1 : city1,
    STATE_PROV : stateProv,
    LAND_CNTRY : landCntry,
    DEPT : dept
  };

  var addrDtlsResult = cmr.query('GET.SME_ADDR_RECORDS_AP', addrRecParams);
  console.log(addrDtlsResult);

  var isExisting = false;

  if (addrDtlsResult.ret1 > 0 && dojo.byId('addEditAddressModalTitle').innerHTML != "Update an Address") {
    isExisting = true;
  } else {
    isExisting = false;
  }

  return isExisting;

}

// 1557838
function doCNDPLPageRefresh(result) {
  var cityVal = "";

  // DENNIS: For CN, city is store differently on the page depending on the
  // landed country
  if (FormManager.getActualValue('landCntry') == "CN") {
    cityVal = FormManager.getActualValue('cnCity');
  } else {
    cityVal = FormManager.getActualValue('city1');
  }

  if (FormManager.getActualValue('custNm1') != cmr.oldname1 || FormManager.getActualValue('custNm2') != cmr.oldname2 || FormManager.getActualValue('landCntry') != cmr.oldlandcntry
      || FormManager.getActualValue('addrTxt') != cmr.oldaddrtxt || FormManager.getActualValue('addrTxt2') != cmr.oldaddrtxt2 || FormManager.getActualValue('city2') != cmr.oldcity2
      || cityVal != cmr.oldcity1 || FormManager.getActualValue('postCd') != cmr.oldpostcd || FormManager.getActualValue('bldg') != cmr.oldbldg
      || FormManager.getActualValue('stateProv') != cmr.oldstateprov || FormManager.getActualValue('cnDistrict') != cmr.oldcndistrict || FormManager.getActualValue('dept') != cmr.olddept
      || FormManager.getActualValue('office') != cmr.oldoffice || FormManager.getActualValue('poBox') != cmr.oldpobox || FormManager.getActualValue('custPhone') != cmr.oldcustphone
      || FormManager.getActualValue('transportZone') != cmr.oldtransportzone || FormManager.getActualValue('cnCustName1') != cmr.oldcncustname
      || FormManager.getActualValue('cnCustName2') != cmr.oldcncustname2 || FormManager.getActualValue('cnAddrTxt') != cmr.oldcnaddrtxt
      || FormManager.getActualValue('cnAddrTxt2') != cmr.oldcnaddrtxt2) {

    FormManager.setValue('dplMessage', result.message);
    FormManager.setValue('showAddress', 'Y');
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.setValue('mainAddrType', 'ZS01');
    }
    // enable all checkboxes
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    FormManager.doAction('frmCMR', 'DPL', true);
    return;
  }
}

function doSSADPLPageRefresh(result) {
  if (FormManager.getActualValue('custNm1') != cmr.oldname1 || FormManager.getActualValue('custNm2') != cmr.oldname2 || FormManager.getActualValue('landCntry') != cmr.oldlandcntry
      || FormManager.getActualValue('addrTxt') != cmr.oldaddrtxt || FormManager.getActualValue('addrTxt2') != cmr.oldaddrtxt2 || FormManager.getActualValue('city2') != cmr.oldcity2
      || FormManager.getActualValue('val_city1') != cmr.oldcity1 || FormManager.getActualValue('postCd') != cmr.oldpostcd || FormManager.getActualValue('divn') != cmr.olddivn
      || FormManager.getActualValue('stateProv') != cmr.oldstateprov || FormManager.getActualValue('transportZone') != cmr.oldtransportzone) {

    FormManager.setValue('dplMessage', result.message);
    FormManager.setValue('showAddress', 'Y');
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.setValue('mainAddrType', 'ZS01');
    }
    // enable all checkboxes
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    FormManager.doAction('frmCMR', 'DPL', true);
    return;
  }
}

function doMXDPLPageRefresh(result) {

  // DENNIS: Compared to SSA, MX does not use City2
  if (FormManager.getActualValue('custNm1') != cmr.oldname1 || FormManager.getActualValue('custNm2') != cmr.oldname2 || FormManager.getActualValue('landCntry') != cmr.oldlandcntry
      || FormManager.getActualValue('addrTxt') != cmr.oldaddrtxt || FormManager.getActualValue('addrTxt2') != cmr.oldaddrtxt2 || FormManager.getActualValue('val_city1') != cmr.oldcity1
      || FormManager.getActualValue('postCd') != cmr.oldpostcd || FormManager.getActualValue('divn') != cmr.olddivn || FormManager.getActualValue('stateProv') != cmr.oldstateprov
      || FormManager.getActualValue('transportZone') != cmr.oldtransportzone) {

    FormManager.setValue('dplMessage', result.message);
    FormManager.setValue('showAddress', 'Y');
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.setValue('mainAddrType', 'ZS01');
    }
    // enable all checkboxes
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    FormManager.doAction('frmCMR', 'DPL', true);
    return;
  }
}

function doBRDPLPageRefresh(result) {

  // DENNIS: Aside from the MX Address values, BR also evaluates tax information
  if (FormManager.getActualValue('custNm1') != cmr.oldname1 || FormManager.getActualValue('custNm2') != cmr.oldname2 || FormManager.getActualValue('landCntry') != cmr.oldlandcntry
      || FormManager.getActualValue('addrTxt') != cmr.oldaddrtxt || FormManager.getActualValue('addrTxt2') != cmr.oldaddrtxt2 || FormManager.getActualValue('val_city1') != cmr.oldcity1
      || FormManager.getActualValue('postCd') != cmr.oldpostcd || FormManager.getActualValue('divn') != cmr.olddivn || FormManager.getActualValue('stateProv') != cmr.oldstateprov
      || FormManager.getActualValue('transportZone') != cmr.oldtransportzone || FormManager.getActualValue('taxCd1') != cmr.oldtaxcd1 || FormManager.getActualValue('taxCd2') != cmr.oldtaxcd2
      || FormManager.getActualValue('vat') != cmr.oldvat) {

    FormManager.setValue('dplMessage', result.message);
    FormManager.setValue('showAddress', 'Y');
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.setValue('mainAddrType', 'ZS01');
    }
    // enable all checkboxes
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    FormManager.doAction('frmCMR', 'DPL', true);
    return;
  }
}

function doSaveCnAddressToList() {
  var cnName1 = FormManager.getActualValue('cnCustName1');
  var cnName2 = FormManager.getActualValue('cnCustName2');
  var cnAddrTxt1 = FormManager.getActualValue('cnAddrTxt');
  var cnAddrTxt2 = FormManager.getActualValue('cnAddrTxt2');

  if (CNHandler.lengthInUtf8Bytes(cnName1) > CNHandler.CN_NAME1_MAX_BYTE_LEN) {
    var newCnName1 = CNHandler.substr_utf8_bytes(cnName1, 0, CNHandler.CN_NAME1_MAX_BYTE_LEN);
    console.log("NEW cnName1 >> " + newCnName1);
    var newLen = newCnName1.length;
    console.log("NEW newCnName1 length >> " + newLen);
    var excessName1 = cnName1.substr(newLen);
    console.log("excessName1 >> " + excessName1);
    FormManager.setValue('cnCustName1', newCnName1);
    FormManager.setValue('cnCustName2', cnName2 + excessName1);
  }

  cnName2 = FormManager.getActualValue('cnCustName2');
  console.log("cnName2 size >> " + CNHandler.lengthInUtf8Bytes(cnName2));
  if (CNHandler.lengthInUtf8Bytes(cnName2) > CNHandler.CN_NAME2_MAX_BYTE_LEN) {
    var newCnName2 = CNHandler.substr_utf8_bytes(cnName2, 0, CNHandler.CN_NAME2_MAX_BYTE_LEN);
    console.log("NEW cnName2 >> " + newCnName2);
    FormManager.setValue('cnCustName2', newCnName2);
  }

  if (CNHandler.lengthInUtf8Bytes(cnAddrTxt1) > CNHandler.CN_ADDRTXT_MAX_BYTE_LEN) {
    var newCnAddrTxt1 = CNHandler.substr_utf8_bytes(cnAddrTxt1, 0, CNHandler.CN_ADDRTXT_MAX_BYTE_LEN);
    console.log("NEW cnAddrTxt1 >> " + newCnAddrTxt1);
    var newLen = newCnAddrTxt1.length;
    var excessStrAddrTxt = cnAddrTxt1.substr(newLen);
    console.log("excessStrAddrTxt >> " + excessStrAddrTxt);
    FormManager.setValue('cnAddrTxt', newCnAddrTxt1);
    FormManager.setValue('cnAddrTxt2', cnAddrTxt2 + excessStrAddrTxt);
  }

  cnAddrTxt2 = FormManager.getActualValue('cnAddrTxt2');
  console.log("cnAddrTxt2 size >> " + CNHandler.lengthInUtf8Bytes(cnAddrTxt2));
  if (CNHandler.lengthInUtf8Bytes(cnAddrTxt2) > CNHandler.CN_ADDRTXT2_MAX_BYTE_LEN) {
    var newCnAddrTxt2 = CNHandler.substr_utf8_bytes(cnAddrTxt2, 0, CNHandler.CN_NAME2_MAX_BYTE_LEN);
    console.log("NEW cnAddrTxt2 >> " + newCnAddrTxt2);
    FormManager.setValue('cnAddrTxt2', newCnAddrTxt2);
  }

  doAddToAddressList();

}

function doValidateSave() {
  var name1 = FormManager.getActualValue('custNm1');
  var name2 = FormManager.getActualValue('custNm2');
  var name3 = FormManager.getActualValue('custNm3');
  var cnName1 = FormManager.getActualValue('cnCustName1');
  var cnName2 = FormManager.getActualValue('cnCustName2');
  var cnAddrTxt1 = FormManager.getActualValue('cnAddrTxt');
  var cnAddrTxt2 = FormManager.getActualValue('cnAddrTxt2');
  var bTruncateCnName2 = false;
  var bTruncateCnStrAddrTxt2 = false;

  // var format = /[!@#$%^&*()_+\-=\[\]{};':"\\|<>\/?]/;
  var genericMsg;

  if (CNHandler.hasDoubleByte(name1) || CNHandler.hasDoubleByte(name2) || CNHandler.hasDoubleByte(name3)) {
    genericMsg = 'Customer Name 1, Customer Name Con\'t and Customer Name Con\'t 2 must not contain non-Latin1 characters.';
    cmr.showAlert(genericMsg);
    return;
  }

  if (CNHandler.lengthInUtf8Bytes(cnName1) > CNHandler.CN_NAME1_MAX_BYTE_LEN) {
    var newCnName1 = CNHandler.substr_utf8_bytes(cnName1, 0, CNHandler.CN_NAME1_MAX_BYTE_LEN);
    console.log("NEW cnName1 >> " + newCnName1);
    var newLen = newCnName1.length;
    var excessName1 = cnName1.substr(newLen + 1);
    console.log("excessName1 >> " + excessName1);
    cnName2 += excessName1;
  }

  console.log("cnName2 size >> " + CNHandler.lengthInUtf8Bytes(cnName2));
  if (CNHandler.lengthInUtf8Bytes(cnName2) > CNHandler.CN_NAME2_MAX_BYTE_LEN) {
    bTruncateCnName2 = true;
  }

  if (CNHandler.lengthInUtf8Bytes(cnAddrTxt1) > CNHandler.CN_ADDRTXT_MAX_BYTE_LEN) {
    var newCnAddrTxt1 = CNHandler.substr_utf8_bytes(cnAddrTxt1, 0, CNHandler.CN_ADDRTXT_MAX_BYTE_LEN);
    console.log("NEW cnAddrTxt1 >> " + newCnAddrTxt1);
    var newLen = newCnAddrTxt1.length;
    var excessStrAddrTxt = cnAddrTxt1.substr(newLen + 1);
    console.log("excessStrAddrTxt >> " + excessStrAddrTxt);
    cnAddrTxt2 += excessStrAddrTxt;
  }

  console.log("cnAddrTxt2 size >> " + CNHandler.lengthInUtf8Bytes(cnAddrTxt2));
  if (CNHandler.lengthInUtf8Bytes(cnAddrTxt2) > CNHandler.CN_ADDRTXT2_MAX_BYTE_LEN) {
    bTruncateCnStrAddrTxt2 = true;
  }

  if (bTruncateCnName2 && !bTruncateCnStrAddrTxt2) {
    cmr.showConfirm('doSaveCnAddressToList()', 'After moving excess values from Customer Name Chinese to Customer Name Con\'t Chinese, '
        + 'the current value of Customer Name Con\'t Chinese is too large in terms of size and will ' + 'be truncated. Would you like to continue?');
    return;
  } else if (!bTruncateCnName2 && bTruncateCnStrAddrTxt2) {
    cmr.showConfirm('doSaveCnAddressToList()', 'After moving excess values from Street Address Chinese to Street Address Con\'t Chinese, '
        + 'the current value of Street Address Con\'t Chinese is too large in terms of size and will ' + 'be truncated. Would you like to continue?');
    return;
  } else if (bTruncateCnName2 && bTruncateCnStrAddrTxt2) {
    cmr.showConfirm('doSaveCnAddressToList()', 'After moving excess values from Customer Name Chinese to Customer Name Con\'t Chinese and Street'
        + ' Address Chinese to Street Address Con\'t Chinese, the current value of Customer Name Con\'t Chinese and Street Address Con\'t Chinese and is too large in terms of size and will '
        + 'be truncated. Would you like to continue?');
    return;
  } else {
    doAddToAddressList();
  }

}

function openDPLSearch() {
  var reqId = FormManager.getActualValue('reqId');
  WindowMgr.open('DPLSEARCH', 'DPLSEARCH' + reqId, 'dpl/request?reqId=' + reqId, null, 550);
}

function doDplSearchRequest() {
  cmr.hideModal('DplDetailsModal');
  openDPLSearch();
}

function resetAbbNmRemoveAddrNORDX() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'DK3PA' || custSubGrp == 'FO3PA' || custSubGrp == 'GL3PA' || custSubGrp == 'IS3PA' || custSubGrp == 'FI3PA' || custSubGrp == 'EE3PA' || custSubGrp == 'LT3PA'
      || custSubGrp == 'LV3PA' || custSubGrp == 'THDPT') {
    FormManager.setValue('abbrevNm', "");
  }

  if (custSubGrp == 'CBISO' || custSubGrp == 'DKISO' || custSubGrp == 'FOISO' || custSubGrp == 'GLISO' || custSubGrp == 'ISISO' || custSubGrp == 'FIISO' || custSubGrp == 'EEISO'
      || custSubGrp == 'LTISO' || custSubGrp == 'LVISO' || custSubGrp == 'INTSO') {
    FormManager.setValue('abbrevNm', "IBM c/o ");
  }
}

function resetAbbNmOnRemoveSelectedAddrNORDX() {
  var havingZI01 = false;

  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    var record = null;
    var type = null;
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (record == null && _allAddressData != null && _allAddressData[i] != null) {
        record = _allAddressData[i];
      }
      type = record.addrType;
      if (typeof (type) == 'object') {
        type = type[0];
      }

      if (type == 'ZI01') {
        havingZI01 = true;
      }

    }
  }

  if (havingZI01) {

    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp == 'DK3PA' || custSubGrp == 'FO3PA' || custSubGrp == 'GL3PA' || custSubGrp == 'IS3PA' || custSubGrp == 'FI3PA' || custSubGrp == 'EE3PA' || custSubGrp == 'LT3PA'
        || custSubGrp == 'LV3PA' || custSubGrp == 'THDPT') {
      FormManager.setValue('abbrevNm', "");
    }

    if (custSubGrp == 'CBISO' || custSubGrp == 'DKISO' || custSubGrp == 'FOISO' || custSubGrp == 'GLISO' || custSubGrp == 'ISISO' || custSubGrp == 'FIISO' || custSubGrp == 'EEISO'
        || custSubGrp == 'LTISO' || custSubGrp == 'LVISO' || custSubGrp == 'INTSO') {
      FormManager.setValue('abbrevNm', "IBM c/o ");
    }

    return;
  }
}
// CREATCMR-5447
function resetSccInfo() {

  var landCntry = '';
  var st = '';
  var cnty = '';
  var city = '';

  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {

      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      type = record.addrType;

      if (typeof (type) == 'object') {
        type = type[0];
      }

      if (type == 'ZS01') {
        landCntry = record.landCntry;
        st = record.stateProv;
        cnty = record.county;
        city = record.city1.toString().toUpperCase();
      }
    }

    var numeric = /^[0-9]*$/;

    if (numeric.test(cnty)) {

      var ret = cmr.query('US_CMR_SCC.GET_SCC_MULTIPLE_BY_LAND_CNTRY_ST_CITY', {
        _qall : 'Y',
        LAND_CNTRY : landCntry,
        N_ST : st,
        N_CITY : city
      });

      if (ret.length > 1) {
        $("#addressTabSccInfo").html('');
        $('#sccMultipleWarn').show();
      }

      var ret1 = cmr.query('US_CMR_SCC.GET_SCC_BY_LAND_CNTRY_ST_CNTY_CITY', {
        LAND_CNTRY : landCntry,
        N_ST : st,
        C_CNTY : cnty,
        N_CITY : city
      });

      var sccValue = '';

      if (ret1 && ret1.ret1 && ret1.ret1 != '') {
        sccValue = ret1.ret1;
        $("#addressTabSccInfo").html(sccValue);
      } else {
        $('#sccWarn').show();
      }

    } else {
      $('#sccWarn').show();
    }
  }

}
// CREATCMR-5447

