/**
 * Retrieves the Request Details
 */
function retrieveRequest() {
  if (FormManager.validate('frmCMRSearch')) {
    document.forms['frmCMRSearch'].submit();
  }
}

/**
 * Initiates the change process
 */
function changeStatus() {
  if (FormManager.validate('frmCMR')) {
    cmr.showConfirm('actualChangeStatus()', 'The status of the specified Request ID(s) will be changed. Please ensure you have checked each request and there is a need to force change the status of each request.<br>Proceed?');
  }
}

/**
 * Actual change status function
 */
function actualChangeStatus() {
  FormManager.doAction('frmCMR', 'FORCE_CHANGE', true);
}

var FieldInfoService = (function() {
  return {
    addField : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/field_info_details';
    },
    fieldIdFormatter : function(value, rowIndex) {
      var id = this.grid.getItem(rowIndex).fieldId[0];
      var seq = this.grid.getItem(rowIndex).seqNo[0];
      var cmrIssuingCntry = this.grid.getItem(rowIndex).cmrIssuingCntry[0];
      return '<a href="javascript: FieldInfoService.open(\'' + id + '\', \'' + seq + '\', \'' + cmrIssuingCntry + '\')">' + value + '</a>';
    },
    open : function(id, seq, cmrIssuingCntry) {
      window.location = cmr.CONTEXT_ROOT + '/code/field_info_details?fieldId=' + encodeURIComponent(id) + '&&seqNo=' + encodeURIComponent(seq)
          + '&&cmrIssuingCntry=' + encodeURIComponent(cmrIssuingCntry);
    },
    addFieldInfo : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/field_info_details';
    },
    addSequence : function(fieldId, cntry, type) {
      var resp = cmr.query('SYSTEM.MAXSEQNO', {
        id : fieldId,
        cntry : cntry
      });
      console.log(resp);
      if (resp && resp.ret1 && resp.ret1 != '') {
        var seq = new Number(resp.ret1);
        seq = seq + 1;
      } else {
        seq = 1;
      }
      window.location = cmr.CONTEXT_ROOT + '/code/field_info_details?newseq=Y&type=' + type + '&cntry=' + cntry + '&fieldId='
          + encodeURIComponent(fieldId) + '&seqNo=' + seq;
    },
    saveFieldInfo : function(newField) {
      FormManager.save('frmCMR');
    },
  };
})();
var UserService = (function() {
  return {
    statusFormatter : function(value, rowIndex) {
      if (value == '0') {
        return '<span style="color:Red">Not Active</span>';
      } else {
        return 'Active';
      }
    },
    userIdFormatter : function(value, rowIndex) {
      var id = this.grid.getItem(rowIndex).userId[0];

      return '<a href="javascript: UserService.open(\'' + id + '\')">' + value + '</a>';
    },
    open : function(value) {
      window.location = cmr.CONTEXT_ROOT + '/user?userId=' + encodeURIComponent(value);
    },
    addUser : function() {
      window.location = cmr.CONTEXT_ROOT + '/user';
    },
    saveUser : function(newUser) {
      if (newUser) {
        var check = cmr.query('CHECKUSER', {
          userId : FormManager.getActualValue('userId')
        });
        if (check && check.ret1 == '1') {
          cmr.showAlert('This user already exists in the system.');
          return;
        }
      }
      FormManager.save('frmCMR');
    },
    removeRoles : function() {
      FormManager.gridAction('frmCMRRoles', 'REMOVE_ROLES', 'Remove selected roles from the user?');
    },
    addSelectedRoles : function() {
      var comments = FormManager.getActualValue('addrolecomments');
      if (comments == '') {
        cmr.showAlert('Please input the full request details for this change.', 'Stop');
        return;
      }
      dojo.byId('userrolecmt').value = comments;
      var roles = '';
      for (var i = 0; i < _CMR_ROLES.length; i++) {
        if (dojo.byId('chk_' + _CMR_ROLES[i]) && dojo.byId('chk_' + _CMR_ROLES[i]).checked) {
          roles += roles.length > 0 ? ',' : '';
          roles += _CMR_ROLES[i].replace('|', ':');
        }
      }
      if (roles.trim().length == 0) {
        cmr.showAlert('Please select roles to add.', 'Stop');
        return;
      }
      dojo.byId('userrolestoadd').value = roles;
      cmr.hideModal('addRolesModal');
      FormManager.doAction('frmCMRRoles', 'ADD_ROLES', true);
    },
    addRoles : function() {
      if (_CMR_ROLES.length == CmrGrid.GRIDS.userRoleListGrid_GRID.rowCount) {
        cmr.showAlert('No available roles to add');
        return;
      }
      cmr.showModal('addRolesModal');
    },
    filterUsers : function() {
      CmrGrid.refresh('userListGrid', cmr.CONTEXT_ROOT + '/userlist.json', 'userName=:userName&userId=:userId&role=:role&requesterFor=:requesterFor');
    }

  };
})();

var StatusDescService = (function() {
  return {
    addStatusDesc : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/addstatusdesc';
    },
    saveStatusDesc : function(newStatusDesc) {
      if (newStatusDesc) {
        var check = cmr.query('STATUSDESCR', {
          reqStatus : FormManager.getActualValue('reqStatus')
        });
        if (check && check.ret1 == '1') {
          cmr.showAlert('This status description already exists in the system.');
          return;
        }
      }
      FormManager.save('frmCMR');
    }
  };
})();

var BdsService = (function() {
  return {
    addBds : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/addbds';
    },
    saveBds : function(newBds) {
      if (newBds) {
        var check = cmr.query('BDS', {
          fieldId : FormManager.getActualValue('fieldId')
        });
        if (check && check.ret1 == '1') {
          cmr.showAlert('This business data source already exists in the system.');
          return;
        }
      }
      FormManager.save('frmCMR');
    }
  };
})();

function statusdescFormatter(value, rowIndex) {
  if (typeof (_wfgrid) != 'undefined' && typeof (_wfrec) != 'undefined' && dojo.cookie(_wfgrid + '_rec') != null) {
    if (dojo.cookie(_wfgrid + '_rec') == value) {
      _wfrec = rowIndex;
    }
  }
  return '<a href="javascript: submitStat(\'' + value + '\')">' + value + '</a>';
}

function submitStat(value) {
  document.forms['frmCMRSearch'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/statusdescdetails/' + value);
  document.forms['frmCMRSearch'].submit();
}

function statusdescFormatter(value, rowIndex) {
  if (typeof (_wfgrid) != 'undefined' && typeof (_wfrec) != 'undefined' && dojo.cookie(_wfgrid + '_rec') != null) {
    if (dojo.cookie(_wfgrid + '_rec') == value) {
      _wfrec = rowIndex;
    }
  }
  return '<a href="javascript: submitStat(\'' + value + '\')">' + value + '</a>';
}

function bdsrecFormatter(value, rowIndex) {
  if (typeof (_wfgrid) != 'undefined' && typeof (_wfrec) != 'undefined' && dojo.cookie(_wfgrid + '_rec') != null) {
    if (dojo.cookie(_wfgrid + '_rec') == value) {
      _wfrec = rowIndex;
    }
  }
  return '<a href="javascript: submitbds(\'' + value + '\')">' + value + '</a>';
}

function submitbds(value) {
  document.forms['frmCMRSearch'].setAttribute('action', cmr.CONTEXT_ROOT + '/code/bdsdetails/' + encodeURIComponent(value));
  document.forms['frmCMRSearch'].submit();
}

var _CMR_ROLES = [ 'ADMIN|', 'REQUESTER|', 'PROCESSOR|PROC_BASIC', 'PROCESSOR|PROC_VALIDATOR', 'PROCESSOR|PROC_SUBMITTER', 'USER|', 'CMDE|',
    'WS_ADMIN|', 'GTS_CROSS|' ];
function addRolesModal_onLoad() {
  for (var i = 0; i < _CMR_ROLES.length; i++) {
    cmr.showNode(_CMR_ROLES[i]);
    if (dojo.byId('chk_' + _CMR_ROLES[i])) {
      dojo.byId('chk_' + _CMR_ROLES[i]).checked = false;
    }
  }

  if (CmrGrid.GRIDS.userRoleListGrid_GRID == null) {
    return;
  }

  var key = '';
  for (var i = 0; i < CmrGrid.GRIDS.userRoleListGrid_GRID.rowCount; i++) {
    key = CmrGrid.GRIDS.userRoleListGrid_GRID.getItem(i).roleId[0];
    key += '|' + CmrGrid.GRIDS.userRoleListGrid_GRID.getItem(i).subRoleId[0];
    cmr.hideNode(key.trim());
  }
}

var SCCService = (function() {
  return {
    addSCC : function() {
      window.location = cmr.CONTEXT_ROOT + '/code/scc_details';
    },
    searchSCC : function() {
      if (FormManager.getActualValue('nSt') == '' && FormManager.getActualValue('nLand') == '') {
        cmr.showAlert('Please select the State or Land Cntry to filter results.');
        return;
      }
      CmrGrid.refresh('sccListGrid', cmr.CONTEXT_ROOT + '/scclist.json', 'nSt=:nSt&nCity=:nCity&nCnty=:nCnty&nLand=:nLand');
    },
    saveSCC : function(newSCC, sccId) {
      var cCity = FormManager.getActualValue('cCity');
      var cCnty = FormManager.getActualValue('cCnty');
      var cSt = FormManager.getActualValue('cSt');
      var nLand = FormManager.getActualValue('nLand');
      var cZip = FormManager.getActualValue('cZip');
      if (newSCC) {
        /* Create */
        if (cCity == '' || cCity == null) {
          cCity = 0;
        }
        if (cCnty == '' || cCnty == null) {
          cCnty = 0;
        }
        if (cSt == '' || cSt == null) {
          cSt = 0;
        }
        var result = null;
        if (nLand == 'US') {
          result = cmr.query('VALIDATOR.SCC_RECORD', {
            C_CITY : cCity,
            C_ST : cSt,
            C_CNTY : cCnty
          });
        } else {
          result = cmr.query('VALIDATOR.SCC_RECORD_NONUS', {
            C_CITY : cCity,
            N_LAND : nLand
          });
        }
        if (result.ret1 == null || result.ret1 == "") {
          FormManager.save('frmCMR');
        } else {
          if (nLand == 'US') {
            cmr
                .showAlert('The combination of State Code and City Code and County Code and Con already exist,please check your input and resend your request.');
          } else {
            cmr.showAlert('The combination of Landed Country and City Code already exist,please check your input and resend your request.');
          }
          return;
        }
      } else {
        /* Update */
        var result = null;
        result = cmr.query('VALIDATOR.GETCCITY', {
          SCCID : sccId
        });

        if (cZip.length > 5) {
          cmr.showAlert('Zip Code maxlength 5 char.');
          $('#cZip').attr('maxlength', '5');
          return;
        }

        if ((cSt != '99.0' && result.ret1 == cCity) || (cSt == '99.0' && result.ret1 == cCity && result.ret2 == nLand)) {
          FormManager.save('frmCMR');
        } else {
          if (cSt == '99.0') {
            result = cmr.query('VALIDATOR.SCC_RECORD_NONUS', {
              C_CITY : cCity,
              N_LAND : nLand
            });
          } else {
            result = cmr.query('VALIDATOR.SCC_RECORD', {
              C_CITY : cCity,
              C_ST : cSt,
              C_CNTY : cCnty
            });
          }
          if (result.ret1 == null || result.ret1 == "") {
            FormManager.save('frmCMR');
          } else {
            if (cSt == '99.0') {
              cmr.showAlert('The combination of Landed Country and City Code already exist,please check your input and resend your request.');
            } else {
              cmr
                  .showAlert('The combination of State Code and City Code and County Code already exist,please check your input and resend your request.');
            }
            return;
          }
        }
      }

    },
    stateFormatter : function(value, rowIndex) {
      if (value == "''") {
        return 'Non-US';
      } else {
        return value;
      }
    },
    removeSCC : function(sccId) {
      cmr.showConfirm('SCCService.actualRemoveSCC(\"' + sccId + '\")', 'Are you sure remove the SCC record?');
    },
    updateSCC : function(sccId) {
      window.location = cmr.CONTEXT_ROOT + '/code/sccdetails?sccId=' + encodeURIComponent(sccId);
    },
    actualRemoveSCC : function(sccId) {
      window.location = cmr.CONTEXT_ROOT + '/code/delete?sccId=' + sccId;
    },
    sccActionIcons : function(value, rowIndex) {
      var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';

      var rowData = this.grid.getItem(0);
      if (rowData == null) {
        return ''; // not more than 1 record
      }
      rowData = this.grid.getItem(rowIndex);
      var nSt = rowData.nSt[0];
      var nCnty = rowData.nCnty[0];
      var nCity = rowData.nCity[0];
      var cZip = rowData.cZip;
      var sccId = rowData.sccId[0];
      var actions = '';
      actions += '<img src="' + imgloc + 'addr-edit-icon.png" class="addr-icon" title="Update" onclick="SCCService.updateSCC(\'' + sccId + '\')">';
      actions += '<img src="' + imgloc + 'addr-remove-icon.png" class="addr-icon" title="Delete" onclick="SCCService.removeSCC(\'' + sccId + '\')">';
      return actions;
    },
    cityFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var city = rowData.nCity;
      var state = rowData.nSt;
      var county = rowData.nCnty;
      var zip = rowData.cZip;
      var sccId = rowData.sccId[0];
      if (state == '\'\'') {
        state = '\\\'\\\'';
      }
      return '<a href="javascript: SCCService.openSCC(\'' + sccId + '\')">' + value + '</a>';
    },
    zipFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var zip = rowData.cZip[0] + '';
      console.log('zip: ' + zip);
      while (zip.length < 9) {
        zip = '0' + zip;
      }
      zip = zip.substring(0, 5) + '-' + zip.substring(5);
      return zip;
    },
    newZipFormatter : function(value, rowIndex) {
      var rowData = this.grid.getItem(rowIndex);
      var zip = rowData.cZip[0] + '';

      if (zip.length < 5) {
        while (zip.length < 5) {
          zip = '0' + zip;
        }
        zip = zip.substring(0, 5);
        return zip;
      }

      if (zip.length == '5') {
        return zip;
      }

      if (zip.length > 5 && zip.length <= 9) {
        while (zip.length < 9) {
          zip = '0' + zip;
        }
        zip = zip.substring(0, 5);
        return zip;
      }
    },
    openSCC : function(sccId) {
      window.location = cmr.CONTEXT_ROOT + '/code/sccdetails?sccId=' + encodeURIComponent(sccId);
    },
    addSCC : function(nonUS) {
      window.location = cmr.CONTEXT_ROOT + '/code/sccdetails' + (nonUS ? '?nonUS=Y' : '');
    },
    exportToExcel : function(filterParams) {
	    console.log("to export the search results to Excel file ..." + filterParams)
      if (!filterParams || filterParams == '') {
        cmr.showAlert('Please select the State or Land Cntry to filter results.');
        return;
      }
      document.forms['sccListDownLoad'].action = cmr.CONTEXT_ROOT + '/code/exportToExcel?' + filterParams;
      document.forms['sccListDownLoad'].target = "exportFrame";
      document.forms['sccListDownLoad'].submit();
    }
  };
})();

function backToCodeMaintHome() {
  window.location = cmr.CONTEXT_ROOT + '/code';
}

function userRoleListGrid_showCheck(value, rowIndex, grid) {
  var rowData = grid.getItem(rowIndex);
  var role = rowData.roleId[0];
  console.log(role);
  if (role == 'USER' || role == 'REQUESTER' || role == 'PROCESSOR') {
    return false;
  }
  return true;
}
