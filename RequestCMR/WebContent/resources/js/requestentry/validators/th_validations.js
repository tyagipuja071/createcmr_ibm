/* Register AP Javascripts */
var _isicHandlerAP = null;
var _clusterHandlerAP = null;
var _vatExemptHandler = null;
var _isuHandler = null;
var _inacCdHandler = null;
var _customerTypeHandler = null;
var oldClusterCd = null;
var _vatRegisterHandlerTH = null;
var _apCustClusterIdTH = null;
var custSubGrpHandler = null;
function addHandlersForAP() {
	if (custSubGrpHandler == null) {
		custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
			if (FormManager.getActualValue('apCustClusterId')) {
				clearOldCluster(value);
			}
		});
	}

	if (_inacCdHandler == null) {
		_inacCdHandler = dojo.connect(FormManager.getField('inacCd'), 'onChange', function(value) {
			setInacType(value);
		});
	}

	if (_isicHandlerAP == null) {
		_isicHandlerAP = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {

			var allowedClustersFrISICISUDpndncy = ['04483', '08813', '08810', '08809'];
			if (allowedClustersFrISICISUDpndncy.includes(FormManager.getActualValue('apCustClusterId')))
				setIsuOnIsic();
		});
	}
	
	if (_apCustClusterIdTH == null) {
	  _apCustClusterIdTH = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {
      var allowedClustersFrISICISUDpndncy = ['04483', '08813', '08810', '08809'];
      if (allowedClustersFrISICISUDpndncy.includes(FormManager.getActualValue('apCustClusterId'))){
        setIsuOnIsic();
        updateIsbuCd();
      }
    });
  }

	if (_vatRegisterHandlerTH == null) {
		_vatRegisterHandlerTH = dojo.connect(FormManager.getField('taxCd1'), 'onChange', function(value) {
			cmr
				.showAlert(
					'<div align="center"><strong>VAT Registration Status validation </strong></div> <br/> Please note: <br/> <ul style="list-style-type:circle"> <li>You have to make sure the selection(Yes/No) of "VAT Registration Status" is correct for the Thailand VAT# you have filled. This is specific to the moment you submit this request.<br/>The status can be validated via VES Thailand: <a href="https://eservice.rd.go.th/rd-ves-web/search/vat" target="_blank" rel="noopener noreferrer"> https://eservice.rd.go.th/rd-ves-web/search/vat </a> </li><br/> <li> By selecting \'No - VAT unapplicable\', you are confirming that this customer has no VAT# then "VAT Registration Status" is not applicable for the same.</li> </ul>', 'VAT Registration Status validation', 'vatRegistrationForSG()', 'VatRegistrationStatus', {
					OK: 'I confirm',
				});
		});
	}
	handleObseleteExpiredDataForUpdate();
}

function setInacType() {
	var inacCd = FormManager.getActualValue('inacCd');
	if (inacCd != undefined && inacCd != '') {
		if (isNaN(inacCd)) {
			FormManager.setValue('inacType', 'N');
		} else {
			FormManager.setValue('inacType', 'I');
		}
	}
}


function clearOldCluster() {
	var currentScenario = FormManager.getActualValue('custSubGrp');
	var previousScenrio = window.localStorage.getItem('custSubGrp');
	var currentCluster = FormManager.getActualValue('apCustClusterId');
	var previousCluster = window.localStorage.getItem('cluster');
	if ((previousScenrio + previousCluster) != (currentScenario + currentCluster) && previousCluster == currentCluster && previousCluster != '00000') {
		FormManager.setValue('apCustClusterId', '');
		FormManager.setValue('isuCd', '');
		FormManager.setValue('inacCd', '');
		FormManager.setValue('clienTier', '');
		FormManager.setValue('inacType', '');
		FormManager.setValue('mrcCd', '');
	}
}

function addAfterConfigAP() {
	console.log('>>>> addAfterConfigAP >>>>');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var reqType = FormManager.getActualValue('reqType');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	if (reqType == 'U') {
		FormManager.removeValidator('vat', Validators.REQUIRED);
	}

	FormManager.removeValidator('cmrNo', Validators.REQUIRED);

	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		FormManager.readOnly('repTeamMemberName');
		FormManager.readOnly('isbuCd');
		FormManager.readOnly('ordBlk');
	}

	if (role == 'REQUESTER' || role == 'VIEWER') {
		if (role == 'VIEWER') {
			FormManager.readOnly('abbrevNm');
			FormManager.readOnly('clientTier');
			FormManager.readOnly('subIndustryCd');
		}
		FormManager.readOnly('isbuCd');
		FormManager.readOnly('sectorCd');
		FormManager.readOnly('abbrevLocn');
		FormManager.readOnly('territoryCd');
		FormManager.readOnly('IndustryClass');

		FormManager.enable('mrcCd');
	} else {
		FormManager.enable('mrcCd');
		FormManager.enable('isbuCd');
		FormManager.enable('abbrevNm');
		FormManager.enable('sectorCd');
		FormManager.enable('abbrevLocn');
		FormManager.enable('territoryCd');
		FormManager.enable('IndustryClass');
		FormManager.enable('subIndustryCd');
	}

	if (role != 'PROCESSOR') {
		FormManager.readOnly('miscBillCd');
	}

	FormManager.readOnly('cmrNo');
	if ((role == 'PROCESSOR' || role == 'VIEWER') && (custSubGrp.includes('DUM') || custSubGrp.includes('INT'))) {
		FormManager.readOnly('mrcCd');
	}


	if (role == 'REQUESTER' && reqType == 'C') {
		if (custSubGrp == 'XECO') {
			FormManager.setValue('mrcCd', '3');
			FormManager.setValue('clientTier', 'Y');
			FormManager.readOnly('clientTier');
			FormManager.setValue('isuCd', '34');
			FormManager.readOnly('isuCd');
		}
	}
	// CREATCMR-5269
	if (reqType == 'U') {
		handleObseleteExpiredDataForUpdate();
	}
	// CREATCMR-788
	addressQuotationValidatorAP();
	updateIsbuCd();
	if (reqType == 'U' || (reqType != 'U' && userRole == 'PROCESSOR')) {
		FormManager.enable('clientTier');
	}
}

function saveClusterVal() {
	console.log(">>>> saveClusterVal");
	if (oldClusterCd == null) {
		oldClusterCd = FormManager.getActualValue('apCustClusterId');
	}
}

function prospectFilter() {
	var ifProspect = FormManager.getActualValue('prospLegalInd');
	if (dijit.byId('prospLegalInd')) {
		ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
	}
	if (ifProspect == 'Y') {
		custSubGrpHandlerINAUSG();
	}
}

function setIsuOnIsic() {
	console.log('>>>> setIsuOnIsic >>>>');
	var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
	if (FormManager.getActualValue('reqType') != 'C' || (FormManager.getActualValue('viewOnlyPage') == 'true' && !(cmrIssuingCntry == '738'))) {
		return;
	}
	var isicCd = FormManager.getActualValue('isicCd');
	var ISU = [];
	if (isicCd != '') {
		var qParams = {
			_qall: 'Y',
			ISSUING_CNTRY: cmrIssuingCntry,
			REP_TEAM_CD: '%' + isicCd + '%'
		};
		var results = cmr.query('GET.ISULIST.BYISIC', qParams);
		if (results != null) {
			for (var i = 0; i < results.length; i++) {
				ISU.push(results[i].ret1);
			}
			if (ISU != null) {
				FormManager.limitDropdownValues(FormManager.getField('isuCd'), ISU);
				if (ISU.length >= 1) {
					FormManager.setValue('isuCd', ISU[0]);
				}
			}
		}
	}
}

function setMrc4IntDumForASEAN() {
	console.log('>>>> setMrc4IntDumForASEAN >>>>');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	if (custSubGrp == 'XINT') {
		FormManager.setValue('mrcCd', '2');
		FormManager.enable('isuCd');
	} else if (custSubGrp == 'XDUMM') {
		FormManager.setValue('mrcCd', '3');
		FormManager.enable('isuCd');
	}
}

/* SG defect : 1795335 */
function addFormatForCMRNumValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var cmrNo = FormManager.getActualValue('cmrNo');
				var reqType = FormManager.getActualValue('reqType');
				var role = FormManager.getActualValue('userRole').toUpperCase();
				var custGrp = FormManager.getActualValue('custGrp');
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				if (reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF') {
					if (cmrNo && cmrNo.length > 0 && !cmrNo.match("^[0-9]+$")) {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'Invalid format of CMR Number. Format should be NNNNNN');
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

/*
 * Story : 1787526 and Story 1813153 and defect 1792206 - APGCG Address Field
 * Format
 */

function addFormatFieldValidator() {
	FormManager.addFormValidator(
		(function() {
			return {
				validate: function() {
					var city1 = FormManager.getActualValue('city1');
					var custNm1 = FormManager.getActualValue('custNm1');
					var custNm2 = FormManager.getActualValue('custNm2');
					var addrTxt = FormManager.getActualValue('addrTxt');
					var addrTxt2 = FormManager.getActualValue('addrTxt2');
					var stateProv = FormManager.getActualValue('stateProv');
					var dept = FormManager.getActualValue('dept');
					// var reqType = FormManager.getActualValue('reqType');
					// var role = FormManager.getActualValue('userRole').toUpperCase();

					// if(role = 'REQUESTER' || role == 'PROCESSOR'){

					// if (city1 && city1.length > 0 &&
					// !city1.match("([0-9]{15})|^(X{3})$|^(x{3})$")) {
					if (city1 && city1.length > 0 && !city1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
						return new ValidationResult({
							id: 'city1',
							type: 'text',
							name: 'city1'
						}, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
						+ ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /   ');
					}

					if (custNm1 && custNm1.length > 0
						&& !custNm1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
						return new ValidationResult({
							id: 'custNm1',
							type: 'text',
							name: 'custNm1'
						}, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
						+ ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
					}

					if (custNm2 && custNm2.length > 0
						&& !custNm2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
						return new ValidationResult({
							id: 'custNm2',
							type: 'text',
							name: 'custNm2'
						}, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
						+ ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
					}

					if (addrTxt && addrTxt.length > 0
						&& !addrTxt.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
						return new ValidationResult({
							id: 'addrTxt',
							type: 'text',
							name: 'addrTxt'
						}, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
						+ ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
					}
					if (addrTxt2 && addrTxt2.length > 0
						&& !addrTxt2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
						return new ValidationResult({
							id: 'addrTxt2',
							type: 'text',
							name: 'addrTxt2'
						}, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
						+ ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
					}
					if (stateProv && stateProv.length > 0
						&& !stateProv.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
						return new ValidationResult({
							id: 'stateProv',
							type: 'text',
							name: 'stateProv'
						}, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
						+ ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
					}
					if (dept && dept.length > 0 && !dept.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
						return new ValidationResult({
							id: 'dept',
							type: 'text',
							name: 'dept'
						}, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
						+ ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
					}

					return new ValidationResult(null, true);
				}
			};
		})(), null, 'frmCMR_addressModal');
}

// Defect : 1815803

function addFieldFormatValidator() {
	console.log("addFieldFormatValidator..............");
	FormManager.addFormValidator((function() {
		return {
			validate: function() {

				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var recordList = null;
					var addrType = null;
					var updateIndicator = null;
					var addrSequence = null;

					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
							recordList = _allAddressData[i];
						}
						addrType = recordList.addrType;
						addrSequence = recordList.addrSeq;
						addrDept = recordList.dept;
						addrCustNm1 = recordList.custNm1;
						addrCustNm2 = recordList.custNm2;
						addrAddrTxt = recordList.addrTxt;
						addrAddrTxt2 = recordList.addrTxt2;
						addrCity1 = recordList.city1;

						/*
             * if (typeof (addrSequence) == 'object') { addrSequence =
             * addrSequence[0]; }
             */
						if (typeof (addrDept) == 'object') {
							addrDept = addrDept[0];
						}
						if (typeof (addrCustNm1) == 'object') {
							addrCustNm1 = addrCustNm1[0];
						}
						if (typeof (addrCustNm2) == 'object') {
							addrCustNm2 = addrCustNm2[0];
						}
						if (typeof (addrAddrTxt) == 'object') {
							addrAddrTxt = addrAddrTxt[0];
						}
						if (typeof (addrAddrTxt2) == 'object') {
							addrAddrTxt2 = addrAddrTxt2[0];
						}
						if (typeof (addrCity1) == 'object') {
							addrCity1 = addrCity1[0];
						}

						if ((addrDept != null && addrDept.length > 0 && !addrDept
							.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							|| (addrCustNm1 != null && addrCustNm1.length > 0 && !addrCustNm1
								.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							|| (addrCustNm2 != null && addrCustNm2.length > 0 && !addrCustNm2
								.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							|| (addrAddrTxt != null && addrAddrTxt.length > 0 && !addrAddrTxt
								.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							|| (addrAddrTxt2 != null && addrAddrTxt2.length > 0 && !addrAddrTxt2
								.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							|| (addrCity1 != null && addrCity1.length > 0 && !addrCity1
								.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))) {
							return new ValidationResult(null, false, 'Only the characters listed are allowed to input due to system limitation.' + ' (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 '
								+ " (3) ' " + ' (4) , ' + ' (5) : ' + ' (6) & ' + ' (7) ( ' + ' (8) ; ' + ' (9) ) ' + ' (10) - ' + ' (11) # ' + ' (12) .  ' + ' (13) / ');
						}

						return new ValidationResult(null, true);

					}
				}

				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addEROAttachmentValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {

				var reqType = FormManager.getActualValue('reqType');
				var custSubType = FormManager.getActualValue('custSubGrp');
				var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
				var reqReason = FormManager.getActualValue('reqReason');

				if (typeof (_pagemodel) != 'undefined') {
					if (reqType == 'U' && reqReason == 'TREC') {
						var id = FormManager.getActualValue('reqId');
						var ret = cmr.query('CHECK_ERO_ATTACHMENT', {
							ID: id
						});
						if (ret == null || ret.ret1 == null) {
							return new ValidationResult(null, false, 'ERO Approval Attachment is required for Temporary Reactivate Reqeusts.');
						} else {
							return new ValidationResult(null, true);
						}
					}
				}

			}
		};
	})(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function addAttachmentValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = FormManager.getActualValue('reqType');
				var custSubType = FormManager.getActualValue('custSubGrp');
				var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
				var reqReason = FormManager.getActualValue('reqReason');
				// var docContent = FormManager.getActualValue('docContent');
				if (typeof (_pagemodel) != 'undefined') {
					if (reqType == 'C' && ((cmrIssuingCntry == '796' && (custSubType == 'ESOSW' || custSubType == 'XESO')) || (cmrIssuingCntry == '616' && custSubType == 'ESOSW')) || (cmrIssuingCntry == '834' && custSubType == 'ASLOM')) {
						var id = FormManager.getActualValue('reqId');
						var ret = cmr.query('CHECK_ESA_MATCH_ATTACHMENT', {
							ID: id
						});
						if (ret == null || ret.ret1 == null) {
							return new ValidationResult(null, false, 'ESA Enrollment Form Attachment tab is required.');
						}
					}
					if (reqType == 'C'
						&& (custSubType != 'INTER' && custSubType != 'XINT' && custSubType != 'DUMMY' && custSubType != 'XDUMM' && custSubType != 'BLUMX' && custSubType != 'XBLUM' && custSubType != 'MKTPC'
							&& custSubType != 'XMKTP' && custSubType != 'IGF' && custSubType != 'XIGF')) {
						if (cmrIssuingCntry == '615' || cmrIssuingCntry == '652') {
							// For BL and SL check company proof
							var id = FormManager.getActualValue('reqId');
							var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
								ID: id
							});
							if (ret == null || ret.ret1 == null) {
								return new ValidationResult(null, false, 'Company Proof in Attachment tab is required.');
							} else {
								return new ValidationResult(null, true);
							}
						}
						else if (cmrIssuingCntry != '796' && cmrIssuingCntry != '616' && cmrIssuingCntry != '834') {
							var id = FormManager.getActualValue('reqId');
							var ret = cmr.query('CHECK_TERRITORY_ATTACHMENT', {
								ID: id
							});
							if (ret == null || ret.ret1 == null) {
								return new ValidationResult(null, false, 'TERRITORY Manager Approval in Attachment tab is required.');
							} else {
								return new ValidationResult(null, true);
							}
						}
					} else {
						return new ValidationResult(null, true);
					}
				}
			}
		};
	})(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function setAttachmentOnCluster() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = FormManager.getActualValue('reqType');
				var role = FormManager.getActualValue('userRole').toUpperCase();
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var cluster = FormManager.getActualValue('apCustClusterId');
				if (reqType == 'C' && role == 'REQUESTER' && (custSubGrp == 'NRML' || custSubGrp == 'AQSTN' || custSubGrp == 'CROSS' || custSubGrp == 'ESOSW') && (cluster == '08033' || cluster == '08034' || cluster == '08035')) {
					var id = FormManager.getActualValue('reqId');
					var ret = cmr.query('CHECK_ECSYS_ATTACHMENT', {
						ID: id
					});
					if (ret == null || ret.ret1 == null) {
						return new ValidationResult(null, false, 'Ecosystem AND Technology leader Approval in Attachment tab is required.');
					}
					else
						return new ValidationResult(null, true);
				}
				else
					return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function defaultCMRNumberPrefix() {
	console.log('>>>> defaultCMRNumberPrefix >>>>');
	if (FormManager.getActualValue('reqType') == 'U') {
		return

	}
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

	if ((role == 'PROCESSOR') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
		FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, ['CmrNoPrefix'], 'MAIN_IBM_TAB');
	}
	// 2333
	if ((role != 'PROCESSOR' && cmrIssuingCntry != '834') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
		FormManager.hide('CmrNoPrefix', 'cmrNoPrefix');
	} else {
		FormManager.show('CmrNoPrefix', 'cmrNoPrefix');
	}

	// default cmrNoPrefix '994---' for PROCESSOR role
	if (role == 'PROCESSOR' && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
		FormManager.setValue('cmrNoPrefix', '994---');
	}
}

// CREATCMR-7656
function setDefaultValueforCustomerServiceCode() {
	console.log('>>>> setDefaultValueforCustomerServiceCode >>>>');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	if (custSubGrp == 'NRML' || custSubGrp == 'INTER' || custSubGrp == 'DUMMY' || custSubGrp == 'AQSTN' || custSubGrp == 'BLUMX'
		|| custSubGrp == 'MKTPC' || custSubGrp == 'ECSYS' || custSubGrp == 'ESOSW' || custSubGrp == 'CROSS' || custSubGrp == 'XAQST' || custSubGrp == 'XBLUM' ||
		custSubGrp == 'XMKTP' || custSubGrp == 'XESO' || custSubGrp == 'PRIV' || custSubGrp == 'NRMLC' || custSubGrp == 'KYND') {
		FormManager.setValue('engineeringBo', '9920');
		FormManager.readOnly('engineeringBo');
		FormManager.removeValidator('engineeringBo', Validators.REQUIRED);
	}
}


function setISBUforBPscenario() {
	console.log('>>>> setISBUforBPscenario >>>>');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var custSubGrp = FormManager.getActualValue('custSubGrp');

	if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP') {
		FormManager.addValidator('isbuCd', Validators.REQUIRED, ['ISBU'], 'MAIN_IBM_TAB');
	}

	if (role != 'VIEWER' && (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP')) {
		FormManager.enable('isbuCd');
	} else if (role == 'REQUESTER' || role == 'VIEWER') {
		FormManager.readOnly('isbuCd');
	} else if (role == 'PROCESSOR') {
		FormManager.enable('isbuCd');
	}
}

function onCustSubGrpChange() {
	console.log('>>>> onCustSubGrpChange >>>>');
	if (FormManager.getActualValue('reqType') == 'U') {
		return
	}
	dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
		console.log('custSubGrp CHANGED here >>>>');
		FormManager.readOnly('subIndustryCd');
		// if (FormManager.getActualValue('viewOnlyPage') != 'true')
		// FormManager.enable('isicCd');
		//    
		setISBUScenarioLogic();

		var custSubGrp = FormManager.getActualValue('custSubGrp');
		var custSubGrpInDB = _pagemodel.custSubGrp;
		if (custSubGrpInDB != null && custSubGrp == custSubGrpInDB) {
			FormManager.setValue('abbrevNm', _pagemodel.abbrevNm);
			FormManager.setValue('abbrevLocn', _pagemodel.abbrevLocn);
			FormManager.setValue('isbuCd', _pagemodel.isbuCd);
			return;
		}

		setISBUScenarioLogic();
		autoSetAbbrevNmLocnLogic();
		setCollectionCd();
	});
}


function setCollectionCd() {
	console.log('>>>> setCollectionCd >>>>');
	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}

	var isbuCd = FormManager.getActualValue('isbuCd');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var collCd = null;
	if (isbuCd != '') {
		var qParams = {
			_qall: 'Y',
			ISSUING_CNTRY: cntry,
			ISBU: '%' + isbuCd + '%'
		};
		var result = cmr.query('GET.ARCODELIST.BYISBU', qParams);
		if (result.length > 0) {
			if (result != null && result[0].ret1 != result[0].ret2) {
				collCd = result[0].ret1;
				if (collCd != null) {
					FormManager.setValue('collectionCd', collCd);
				}
			}
		}
	}
}

function addSalesRepNameNoCntryValidator() {
	console.log(">>>repTeamMemberNo<<<===" + FormManager.getActualValue('repTeamMemberNo'));
	console.log(">>>repTeamMemberNo---pID<<<===" + localStorage.getItem("pID"));

	if (localStorage.getItem("pID") != null && (FormManager.getActualValue('repTeamMemberNo'))) {
		FormManager.addFormValidator((function() {
			return {
				validate: function() {
					console.log('>>>> addSalesRepNameCntryValidator >>>>');
					var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
					var lbl1 = FormManager.getLabel('SalRepNameNo');
					var temp = localStorage.getItem("pID").substring(6);
					var countryCd = (temp == 'PH1') ? '818' : temp;
					// if (cmrIssuCntry != countryCd && !(cmrIssuCntry == '615'
					// ||cmrIssuCntry ==
					// '652' ||cmrIssuCntry == '744' ||cmrIssuCntry == '790'
					// ||cmrIssuCntry == '736'
					// || cmrIssuCntry == '738' || cmrIssuCntry == '790')) {
					if (cmrIssuCntry != countryCd && !(included(cmrIssuCntry))) {
						return new ValidationResult({
							id: 'repTeamMemberNo',
							type: 'text',
							name: 'repTeamMemberNo'
						}, false, 'The value of ' + lbl1 + ' is invalid. CMR Issuing Country(' + cmrIssuCntry + ') and Sales Rep Country(' + countryCd + ') should be same.');
					}
					return new ValidationResult(null, true);
				}
			};
		})(), 'MAIN_IBM_TAB', 'frmCMR');
	} else {

	}
}

function included(cmrIssuCntry) {
	var excludedCntry = ["615", "652", "744", "790", "736", "738", "643", "749", "778", "818", "834", "852", "856", "646", "714", "720", "616", "796"];

	for (var i = 0; i < excludedCntry.length; i++) {
		console.log("---excluded Country is---" + excludedCntry[i]);
		console.log("---cmrIssuCntry Country is---" + cmrIssuCntry);
		if (cmrIssuCntry == excludedCntry[i])
			return false;
		else
			return true;
	}
	;
}

function setAbbrevNmLocnOnAddressSave(cntry, addressMode, saving, finalSave, force) {
	console.log('>>>> setAbbrevNmLocnOnAddressSave >>>>');
	var reqType = null;
	var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
	if (typeof (_pagemodel) != 'undefined') {
		reqType = FormManager.getActualValue('reqType');
	}
	// if (reqType == 'U') {
	// return;
	// }
	if ((finalSave || force) && cmr.addressMode) {
		var copyTypes = document.getElementsByName('copyTypes');
		var copyingToA = false;
		if (copyTypes != null && copyTypes.length > 0) {
			copyTypes.forEach(function(input, i) {
				if (input.value == 'ZS01' && input.checked) {
					copyingToA = true;
				}
			});
		}
		var addrType = FormManager.getActualValue('addrType');
		if (addrType == 'ZS01' || copyingToA) {
			autoSetAbbrevNmLocnLogic();
		}
	}
}

function autoSetAbbrevNmLocnLogic() {
	console.log("autoSetAbbrevNmLocnLogic");
	var _abbrevNm = null;
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var _abbrevLocn = null;
	var cntryDesc = null;

	var zs01ReqId = FormManager.getActualValue('reqId');
	var qParams = {
		REQ_ID: zs01ReqId,
	};
	var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
	var custNm1 = FormManager.getActualValue('custNm1');
	if (custNm1 == '')
		custNm1 = result.ret1;
	_abbrevNm = custNm1;
	if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
		if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
			_abbrevNm = "IGF INTERNAL_" + custNm1;
		} else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
			_abbrevNm = "ESA_" + custNm1;
		} else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
			_abbrevNm = "Softlayer_" + custNm1;
		} else {
			_abbrevNm = custNm1;
		}
	}

	if (_abbrevNm && _abbrevNm.length > 21) {
		_abbrevNm = _abbrevNm.substring(0, 21);
	}
	FormManager.setValue('abbrevNm', _abbrevNm);

	if (FormManager.getActualValue('landCntry') == "TH") {
		if (FormManager.getActualValue('city1') != 'undefined') {
			_abbrevLocn = FormManager.getActualValue('city1');
		}
	}
	if (FormManager.getActualValue('landCntry') != "TH") {
		var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
		_abbrevLocn = cntryDesc;
	}

	if (_abbrevLocn && _abbrevLocn.length > 9) {
		_abbrevLocn = _abbrevLocn.substring(0, 9);
	}
	FormManager.setValue('abbrevLocn', _abbrevLocn);
}

function getLandCntryDesc(cntryCd) {
	console.log('>>>> getLandCntryDesc >>>>');
	if (cntryCd != null) {
		reqParam = {
			COUNTRY: cntryCd,
		};
	}
	var results = cmr.query('GET.CNTRY_DESC', reqParam);
	_cntryDesc = results.ret1;
	return _cntryDesc;
}

function onSubIndustryChange() {
	console.log('>>>> onSubIndustryChange >>>>');
	var reqType = null;
	reqType = FormManager.getActualValue('reqType');
	if (reqType == 'U') {
		console.log('>>>> Exit onSubIndustChange for Update.');
		return;
	}
	_subIndCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
		if (!value) {
			return;
		}
		if (value != null && value.length > 1) {
			updateIndustryClass();
			addSectorIsbuLogicOnSubIndu();
		}
	});
	if (_subIndCdHandler && _subIndCdHandler[0]) {
		_subIndCdHandler[0].onChange();
	}
}
function addSectorIsbuLogicOnSubIndu() {
	console.log('>>>> addSectorIsbuLogicOnSubIndu >>>>');
	var arryIndCdForSectorCOM = ['K', 'U', 'A'];
	var arryIndCdForSectorDIS = ['D', 'W', 'T', 'R'];
	var arryIndCdForSectorFSS = ['F', 'S', 'N'];
	var arryIndCdForSectorIND = ['M', 'P', 'J', 'V', 'L'];
	var arryIndCdForSectorPUB = ['H', 'X', 'Y', 'G', 'E'];
	var arryIndCdForSectorCSI = ['B', 'C'];
	var arryIndCdForSectorEXC = ['Z'];

	FormManager.setValue('sectorCd', '');
	var subIndustryCd = FormManager.getActualValue('subIndustryCd');
	if (subIndustryCd != null && subIndustryCd.length > 1) {
		var _industryClass = subIndustryCd.substr(0, 1);
		searchArryAndSetValue(arryIndCdForSectorCOM, _industryClass, 'sectorCd', 'COM');
		searchArryAndSetValue(arryIndCdForSectorDIS, _industryClass, 'sectorCd', 'DIS');
		searchArryAndSetValue(arryIndCdForSectorFSS, _industryClass, 'sectorCd', 'FSS');
		searchArryAndSetValue(arryIndCdForSectorIND, _industryClass, 'sectorCd', 'IND');
		searchArryAndSetValue(arryIndCdForSectorPUB, _industryClass, 'sectorCd', 'PUB');
		searchArryAndSetValue(arryIndCdForSectorCSI, _industryClass, 'sectorCd', 'CSI');
		searchArryAndSetValue(arryIndCdForSectorEXC, _industryClass, 'sectorCd', 'EXC');
	}

	updateIsbuCd();
}

function updateIsbuCd() {
	console.log('>>>> updateIsbuCd >>>>');
	var _mrcCd = FormManager.getActualValue('mrcCd');
	var _sectorCd = FormManager.getActualValue('sectorCd');
	var _industryClass = FormManager.getActualValue('IndustryClass');
	var _isbuCd = null;
	if (_sectorCd == null) {
		console.log('>>>> Error, _sectorCd is null');
	}
	if (_industryClass == null) {
		console.log('>>>> Error, _industryClass is null');
	}
	if (_mrcCd == null) {
		console.log('>>>> Error, _mrcCd is null');
	}
	// FormManager.setValue('isbuCd', '');
	if (_mrcCd == '3' && _industryClass != '') {
		_isbuCd = 'GMB' + _industryClass;
		FormManager.setValue('isbuCd', _isbuCd);
	} else if (_mrcCd == '2' && _sectorCd != '' && _industryClass != '') {
		_isbuCd = _sectorCd + _industryClass;
		FormManager.setValue('isbuCd', _isbuCd);
	}

	setISBUScenarioLogic();
}


function setIsicCdIfCmrResultAccepted(value) {
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	console.log(">>>>setDefaultValueForCreate()>>>> SubGrp=" + custSubGrp);
	var reqId = FormManager.getActualValue('reqId');
	var isicCdInDB = '';
	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	var custSubGroups = ['BLUMX', 'MKTPC', 'AQSTN', 'NRML', 'NRMLC', 'KYNDR', 'ESOSW', 'ECSYS', 'CROSS', 'XAQST', 'XBLUM', 'XESO', 'XMKTP', 'ASLOM'];
	if (custSubGroups.includes(custSubGrp)) {
		var reqIdParams = {
			REQ_ID: reqId,
		};
		var isicCdResult = cmr.query('GET.ISIC_CD_BY_REQID', reqIdParams);

		if (isicCdResult.ret1 != undefined) {
			isicCdInDB = isicCdResult.ret1;
		}
		console.log(">>>>setDefaultValueForCreate()>>>> ISIC_CD in DB =" + isicCdInDB);
		FormManager.setValue('isicCd', isicCdInDB);
		FormManager.enable('isicCd');
		FormManager.enable('subIndustryCd');
	} else {
		FormManager.readOnly('isicCd');
		FormManager.readOnly('subIndustryCd');
		switch (custSubGrp) {
			case 'PRIV':
				// ISIC = 9500, - lock field
				FormManager.setValue('isicCd', '9500');
				break;
			case 'INTER':
				// ISIC = 8888, - lock field
				FormManager.setValue('isicCd', '0000');
				break;
			case 'DUMMY':
				// ISIC = 8888, - lock field
				FormManager.setValue('isicCd', '8888');
				break;
		}
	}
}

function getIsicDataRDCValue() {
	var result = cmr.query('GET.ISIC_OLD_BY_REQID', {
		REQ_ID: FormManager.getActualValue('reqId')
	});
	return result.ret1;
}

function setIsicCdIfDnbResultAccepted(value) {
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var cond2 = new Set(['AQSTN', 'BLUMX', 'ESOSW', 'ECSYS', 'MKTPC', 'NRML', 'CROSS', 'SPOFF', 'XBLUM', 'XAQST', 'XMKTP', 'BUSPR', 'ASLOM', 'NRMLC', 'KYNDR']);
	var cond3 = new Set(['INTER', 'PRIV', 'XPRIV', 'DUMMY', 'IGF']);
	if (cond2.has(custSubGrp)) {
		var oldISIC = getIsicDataRDCValue();
		FormManager.setValue('isicCd', oldISIC);
		FormManager.readOnly('isicCd');
	} else if (cond3.has(custSubGrp)) {
		FormManager.setValue('isicCd', value);
		FormManager.readOnly('isicCd');
	}
	if ((custSubGrp == '' || custSubGrp != '') && value != '') {
		FormManager.readOnly('isicCd');
	}
}

function setIsicCdIfDnbAndCmrResultOther(value) {
	var value = FormManager.getActualValue('isicCd');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var cond4 = new Set(['INTER', 'PRIV', 'XPRIV', 'DUMMY', 'IGF']);
	if (cond4.has(custSubGrp)) {
		FormManager.setValue('isicCd', value);
		FormManager.readOnly('isicCd');
	} else if (custSubGrp != '') {
		FormManager.setValue('isicCd', '');
		FormManager.enable('isicCd');
	}
	FormManager.setValue('isicCd', _pagemodel.isicCd);
}

function onIsicChange() {
	console.log('>>>> onIsicChange >>>>');
	var reqType = FormManager.getActualValue('reqType');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var value = FormManager.getActualValue('isicCd');
	var cmrResult = FormManager.getActualValue('findCmrResult');
	var dnbResult = FormManager.getActualValue('findDnbResult');

	if (reqType != 'C' && role != 'REQUESTER') {
		return;
	}

	if (cmrResult != '' && cmrResult == 'Accepted') {
		setIsicCdIfCmrResultAccepted(value);
	} else if (dnbResult != '' && dnbResult == 'Accepted') {
		setIsicCdIfDnbResultAccepted(value);
	} else if (cmrResult == 'No Results' || cmrResult == 'Rejected' || dnbResult == 'No Results' || dnbResult == 'Rejected') {
		FormManager.readOnly('isicCd');
		setIsicCdIfDnbAndCmrResultOther(value);
	}
}

function setPrivate() {
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	if (custSubGrp == 'XAQST' || custSubGrp == 'XMKTP' || custSubGrp == 'XBLUM') {
		FormManager.setValue('custSubGrp', 'XPRIV');
	} else {
		FormManager.setValue('custSubGrp', 'PRIV');
	}
}


function updateIndustryClass() {
	console.log('>>>> updateIndustryClass >>>>');
	var subIndustryCd = FormManager.getActualValue('subIndustryCd');
	if (subIndustryCd != null && subIndustryCd.length > 1) {
		var _industryClass = subIndustryCd.substr(0, 1);
		FormManager.setValue('IndustryClass', _industryClass);
		if (_industryClass == 'Y' || _industryClass == 'G') {
			FormManager.enable('taxCd2');
			FormManager.addValidator('taxCd2', Validators.REQUIRED, ['Government Customer Type'], 'MAIN_IBM_TAB');
		} else {
			FormManager.removeValidator('taxCd2', Validators.REQUIRED);
			FormManager.readOnly('taxCd2');
			FormManager.setValue('taxCd2', '');
		}
	}
}


function searchArryAndSetValue(arry, searchValue, fieldID, setValue) {
	console.log('>>>> searchArryAndSetValue >>>>');
	for (var i = 0; i < arry.length; i++) {
		if (arry[i] == searchValue) {
			FormManager.setValue(fieldID, setValue);
		}
	}
}


function updateIsbuCd() {
	console.log('>>>> updateIsbuCd >>>>');
	var _mrcCd = FormManager.getActualValue('mrcCd');
	var _sectorCd = FormManager.getActualValue('sectorCd');
	var _industryClass = FormManager.getActualValue('IndustryClass');
	var _isbuCd = null;
	if (_sectorCd == null) {
		console.log('>>>> Error, _sectorCd is null');
	}
	if (_industryClass == null) {
		console.log('>>>> Error, _industryClass is null');
	}
	if (_mrcCd == null) {
		console.log('>>>> Error, _mrcCd is null');
	}
	// FormManager.setValue('isbuCd', '');
	if (_mrcCd == '3' && _industryClass != '') {
		_isbuCd = 'GMB' + _industryClass;
		FormManager.setValue('isbuCd', _isbuCd);
	} else if (_mrcCd == '2' && _sectorCd != '' && _industryClass != '') {
		_isbuCd = _sectorCd + _industryClass;
		FormManager.setValue('isbuCd', _isbuCd);
	}

	setISBUScenarioLogic();
}

function setISBUScenarioLogic() {
	console.log('>>>> setISBUScenarioLogic >>>>');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var isbuList = null;

	if (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM') {
		FormManager.setValue('isbuCd', 'DUM1');

	} else if (custSubGrp == 'INTER' || custSubGrp == 'XINT') {
		FormManager.setValue('isbuCd', 'INT1');

	} else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP') {
		isbuList = ['BPN1', 'BPN2'];
		console.log("isbuList = " + isbuList);
		FormManager.enable('isbuCd');
		FormManager.setValue('isbuCd', '');
		FormManager.limitDropdownValues(FormManager.getField('isbuCd'), isbuList);

	}
}




function updateProvCd() {
	console.log('>>>> updateProvCd');
	FormManager.readOnly('territoryCd');
	_provNmHandler = dojo.connect(FormManager.getField('busnType'), 'onChange', function(value) {
		if (!value) {
			value = FormManager.getField('busnType');
		}
		if (!value) {
			FormManager.setValue('territoryCd', '');
		}
		if (value != null && value.length > 1) {
			var _ProvNm = FormManager.getActualValue('busnType');
			FormManager.setValue('territoryCd', _ProvNm);
			if (FormManager.getActualValue('cmrIssuingCntry') == '744' && FormManager.getActualValue('reqType') == 'C')
				setCollCdFrIndia();
		}
	});
	if (_provNmHandler && _provNmHandler[0]) {
		_provNmHandler[0].onChange();
	}
}

function updateRegionCd() {
	console.log('>>>> updateRegionCd');
	_provNmHandler = dojo.connect(FormManager.getField('busnType'), 'onChange', function(value) {
		if (!value) {
			return;
		}
		if (value != null && value.length > 1) {
			var provCd = FormManager.getActualValue('busnType');
			var _issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
			var qParams = {
				CNTRY: _issuingCntry,
				PROV_CD: provCd,
			};
			var _result = cmr.query('GET.RAGION_CD', qParams);
			var _regionCd = _result.ret1;
			FormManager.setValue('miscBillCd', _regionCd);
		}
	});
	if (_provNmHandler && _provNmHandler[0]) {
		_provNmHandler[0].onChange();
	}
}

function canRemoveAddress(value, rowIndex, grid) {
	console.log('>>>> canRemoveAddress >>>>');
	var rowData = grid.getItem(rowIndex);
	var importInd = rowData.importInd[0];
	var reqType = FormManager.getActualValue('reqType');
	if ('U' == reqType && 'Y' == importInd) {
		return false;
	}
	return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
	return canRemoveAddress(value, rowIndex, grid);
}


function addCmrNoValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				var custSubType = FormManager.getActualValue('custSubGrp');
				var custGrp = FormManager.getActualValue('custGrp');
				var cmrNo = FormManager.getActualValue('cmrNo');
				var role = FormManager.getActualValue('userRole').toUpperCase();

				if (FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('Processor') == 'true' && custGrp == 'cross' && custSubType == 'SPOFF') {
					return new ValidationResult(null, true);
				}
				if (FormManager.getActualValue('Processor') == 'true') {
					return new ValidationResult(null, true);
				}

				if (cmrNo != '' && cmrNo.length != 6) {
					return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}


function setAbbrevNameforGovType() {
	console.log('>>>> setAbbrevNameforGovType >>>>');
	var govCustType = FormManager.getActualValue('taxCd2');
	var abbrevNmPrefix = null;
	var abbrevNm = FormManager.getActualValue('abbrevNm');

	if (FormManager.getActualValue('reqType') == 'C' && (govCustType && govCustType != '') && (abbrevNm && abbrevNm != '')) {
		switch (govCustType) {
			case '012':
				abbrevNmPrefix = 'ZC';
				break;
			case '013':
				abbrevNmPrefix = 'ZS';
				break;
			case '014':
				abbrevNmPrefix = 'ZL';
				break;
			default:
				abbrevNmPrefix = '';
		}
		if (abbrevNmPrefix != null)
			abbrevNm = abbrevNmPrefix + FormManager.getActualValue('abbrevNm');
		FormManager.setValue('abbrevNm', abbrevNm);
	}
}

function addSoltToAddressValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var zs01ReqId = FormManager.getActualValue('reqId');
				var addrType = FormManager.getActualValue('addrType');
				qParams = {
					REQ_ID: zs01ReqId,
				};
				var record = cmr.query('GETZS01VALRECORDS', qParams);
				var zs01Reccount = record.ret1;
				if (addrType == 'ZS01' && Number(zs01Reccount) == 1 && cmr.addressMode != 'updateAddress') {
					return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), null, 'frmCMR_addressModal');
}

function addAddressInstancesValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
					return new ValidationResult(null, false, 'One Sold-To Address is mandatory. Only one address for each address type should be defined when sending for processing.');
				}
				var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
				var qParams = {
					_qall: 'Y',
					CMR_ISSUING_CNTRY: cmrCntry,
				};
				var results = cmr.query('GETADDR_TYPES', qParams);
				var duplicatesAddr = [];
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var type = null;
					if (results != null) {
						for (var j = 0; j < results.length; j++) {
							var addrCnt = 0;
							for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
								record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
								if (record == null && _allAddressData != null && _allAddressData[i] != null) {
									record = _allAddressData[i];
								}
								type = record.addrType;
								if (typeof (type) == 'object') {
									type = type[0];
								}
								if (results[j].ret1 == type) {
									// New requirement : Defect 1767113 : For HK and MO ->
									// Multiple
									// Billing & Billing CCR are to be allowed
									if ((cmrCntry == SysLoc.MACAO || cmrCntry == SysLoc.HONG_KONG) && (record.addrType == 'ZP01' || record.addrType == 'ZP02')) {
										continue;
									}
									addrCnt++;
									if (addrCnt > 1)
										duplicatesAddr.push(results[j].ret2);
								}
							}
						}
					}
					if (duplicatesAddr.length > 0) {
						return new ValidationResult(null, false, 'Only one instance of each address can be added.Please remove additional ' + duplicatesAddr + ' addresses');
					} else {
						return new ValidationResult(null, true);
					}
				}
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addContactInfoValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('reqType') == 'C') {
					var record = null;
					var type = null;
					var custName = null;
					var streetAddr = null;
					var streetAddrCont = null;
					var postCd = null;
					var city = null;
					var state = null;
					var cntry = FormManager.getActualValue('cmrIssuingCntry');
					var mandtDetails = 0;
					var mandtDetails_1 = 0;
					var mandtDetails_2 = 0;
					var mandtDetails_3 = 0;
					var mandtDetails_4 = 0;

					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						if (record == null && _allAddressData != null && _allAddressData[i] != null) {
							record = _allAddressData[i];
						}
						type = record.addrType;
						custName = record.custNm1;
						streetAddr = record.addrTxt;
						streetAddrCont = record.addrTxt2;
						city = record.city1;
						state = record.stateProv;

						if (typeof (type) == 'object') {
							type = type[0];
						}
						if (typeof (custName) == 'object') {
							custName = custName[0];
						}
						if (typeof (streetAddr) == 'object') {
							streetAddr = streetAddr[0];
						}
						if (typeof (streetAddrCont) == 'object') {
							streetAddrCont = streetAddrCont[0];
						}
						if (typeof (city) == 'object') {
							city = city[0];
						}
						if (typeof (state) == 'object') {
							state = state[0];
						}

						switch (cntry) {
							case '643':
							case '834':
								if ((custName == null || streetAddr == null)) {
									mandtDetails++;
								}
								break;
							case '778':
							case '818':
							case '852':
							case '856':
							case '749':
							case '615':
							case '744':
							case '752':
								if ((custName == null || streetAddr == null || city == null)) {
									mandtDetails_1++;
								}
								break;
							case '796':
								if ((custName == null || streetAddr == null || city == null)) {
									mandtDetails_1++;
								}
								break;
							case '616':
								var custGrp = FormManager.getActualValue('custGrp');
								if (custGrp != 'CROSS' && (custName == null || streetAddr == null || city == null || state == null)) {
									mandtDetails_2++;
								} else if (custGrp == 'CROSS' && (custName == null || streetAddr == null || city == null)) {
									mandtDetails_2++;
								}
								break;
							case '736':
							case '738':
								if ((custName == null || streetAddr == null)) {
									mandtDetails_3++;
								}
								break;
						}
					}
					if (mandtDetails > 0) {
						return new ValidationResult(null, false, "Customer Name  is required, Street Address is required");
					} else if (mandtDetails_1 > 0) {
						return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, City is required.");
					} else if (mandtDetails_2 > 0) {
						return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, , City is required and State is required.");
					} else if (mandtDetails_3 > 0) {
						return new ValidationResult(null, false, "Customer Name  is required, Street Address is required.");
					}
				}
				return new ValidationResult(null, true);

			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');

}

function similarAddrCheckValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var req_id = FormManager.getActualValue('reqId');
				var req_type = FormManager.getActualValue('reqType');
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var addrDupExists = [];
					var custNm1 = '';
					var custNm2 = '';
					var addrTxt = '';
					var city1 = '';
					var addrTxt2 = '';
					var stateProv = '';
					var landCntry = '';
					var importIndc = '';
					var dept = '';
					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						if (record == null && _allAddressData != null && _allAddressData[i] != null) {
							record = _allAddressData[i];
						}
						custNm1 = record.custNm1[0];
						custNm2 = record.custNm2[0] != null ? record.custNm2[0] : '';
						addrTxt = record.addrTxt[0];
						city1 = record.city1[0] != null ? record.city1[0] : '';
						stateProv = record.stateProv[0] != null ? record.stateProv[0] : '';
						addrTxt2 = record.addrTxt2[0] != null ? record.addrTxt2[0] : '';
						;
						landCntry = record.landCntry[0];
						dept = record.dept[0] != null ? record.dept[0] : '';
						importIndc = record.importInd[0] != null ? record.importInd[0] : '';

						if (req_type == 'U' && importIndc == 'Y') {
							continue; // skip the unique check for addresses in case of
							// import for Update requests.
						}
						var qParams = {
							REQ_ID: req_id,
							CUST_NM1: custNm1,
							CUST_NM2: custNm2,
							ADDR_TXT: addrTxt,
							ADDR_TXT_2: addrTxt2,
							CITY1: city1,
							STATE_PROV: stateProv,
							DEPT: dept,
							LAND_CNTRY: landCntry
						};
						var results = cmr.query('GET.SME_ADDR_RECORDS_AP', qParams);
						if (results.ret1 > 1) {
							addrDupExists.push(record.addrTypeText);
						}
					}
					if (addrDupExists.length > 0) {
						return new ValidationResult(null, false, 'Duplicate address details exist for Addresses ' + addrDupExists + '. Please make sure every address detail should be a unique combination.');
					} else {
						return new ValidationResult(null, true);
					}
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setFieldsForDoubleCreates() {
	console.log('>>>> setFieldsForDoubleCreates >>>>');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var custGrp = FormManager.getActualValue('custGrp');
	if (reqType == 'U') {
		return new ValidationResult(null, true);
	}

	if (cntry == '615' && role == 'PROCESSOR' && (custGrp == 'LOCAL' || custGrp == 'CROSS')) {
		FormManager.enable('cmrNo');
		FormManager.enable('cmrNoPrefix');
	}
	if (cntry == '652' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custGrp == 'LOCAL' || custGrp == 'CROSS')) {
		FormManager.enable('cmrNo');
		FormManager.enable('cmrNoPrefix');
	}
	if (cntry == '744' && role == 'PROCESSOR' && (custSubGrp == 'ESOSW' || custSubGrp == 'XESO')) {
		FormManager.enable('cmrNo');
		FormManager.enable('cmrNoPrefix');
	}
	if (cntry == '744' && role == 'REQUESTER' && custSubGrp == 'PRIV') {
		FormManager.readOnly('apCustClusterId');
		FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
		FormManager.removeValidator('repTeamMemberName', Validators.REQUIRED);
	}
	if (cntry == '744' && role == 'REQUESTER' && custSubGrp == 'CROSS') {
		FormManager.setValue('collectionCd', 'I001');
		FormManager.setValue('busnType', '709');
		FormManager.setValue('territoryCd', '709');
	}
	if (cntry == '852' && role == 'PROCESSOR' && (custSubGrp != 'BLUMX' || custSubGrp != 'MKTPC')) {
		FormManager.enable('cmrNo');
		FormManager.enable('cmrNoPrefix');
	}
}

/*
 * function addDoubleCreateValidatorSG(){
 * FormManager.addFormValidator((function() { return { validate : function() {
 * var role = FormManager.getActualValue('userRole').toUpperCase(); var reqType =
 * FormManager.getActualValue('reqType'); var cntry =
 * FormManager.getActualValue('cmrIssuingCntry'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); var custGrp =
 * FormManager.getActualValue('custGrp'); if (reqType == 'U') { return new
 * ValidationResult(null, true); } if (FormManager.getActualValue('cmrNo') != '' ) {
 * showError = true; }else{ showError = false; } if((cntry == '834' && reqType ==
 * 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF')){
 * if(showError) return new ValidationResult(null, false, 'For CMR Number and
 * CMR Number Prefix in case of double creates, only one can be filled.'); else
 * if(FormManager.getActualValue('cmrNo') != ''){ var cmrNumber =
 * FormManager.getActualValue('cmrNo'); var katr6 =
 * FormManager.getActualValue('cmrIssuingCntry'); qParams = { KATR6 : katr6,
 * MANDT : cmr.MANDT, ZZKV_CUSNO : cmrNumber, }; var record =
 * cmr.query('CHECK_CMR_RDC', qParams); var rdcCMRCount = record.ret1; qParams = {
 * KATR6 : '834', MANDT : cmr.MANDT, ZZKV_CUSNO : cmrNumber, }; var recordSG =
 * cmr.query('CHECK_CMR_RDC', qParams); var rdcCMRCountSG = recordSG.ret1; if
 * (Number(rdcCMRCount) > 0 || Number(rdcCMRCountSG) > 0 ) { return new
 * ValidationResult(null, false, 'Please enter another CMR Number as entered CMR
 * Number already exists in RDC.'); }
 * 
 * var resultWTAAS = CmrServices.checkWTAAS(cmrNumber,katr6); var resultWTAAS_SG =
 * CmrServices.checkWTAAS(cmrNumber,'834'); if ((resultWTAAS.success &&
 * resultWTAAS.data && resultWTAAS.Status && resultWTAAS.Status == 'F' &&
 * resultWTAAS.Error_Msg != null &&
 * resultWTAAS.Error_Msg.toLowerCase().includes("does not exist")) &&
 * (resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status &&
 * resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null &&
 * resultWTAAS_SG.Error_Msg.toLowerCase().includes("does not exist"))) { return
 * new ValidationResult(null, true); }else if(!resultWTAAS.success ||
 * !resultWTAAS_SG.success){ } } } } } } }
 */

/* Story : 1782082 - Singapore and defct : 1801410 */

function addDoubleCreateValidatorSG() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var role = FormManager.getActualValue('userRole').toUpperCase();
				var reqType = FormManager.getActualValue('reqType');
				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var custGrp = FormManager.getActualValue('custGrp');
				var cmrNumber = FormManager.getActualValue('cmrNo');
				if (reqType == 'U') {
					return new ValidationResult(null, true);
				}
				/*
         * if (FormManager.getActualValue('cmrNo') != '' ) { showError = true;
         * }else{ showError = false; }
         */
				if (cntry == '834' && reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF') {
					if (FormManager.getActualValue('cmrNo') != '' && cmrNumber.length == 6) {
						var cmrNumber = FormManager.getActualValue('cmrNo');

						qParams = {
							KATR6: '834',
							MANDT: cmr.MANDT,
							ZZKV_CUSNO: cmrNumber,
						};
						var recordSG = cmr.query('CHECK_CMR_RDC', qParams);
						var rdcCMRCountSG = recordSG.ret1;

						if (Number(rdcCMRCountSG) > 0) {

							return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in RDC.');
						}

						var resultWTAAS_SG = CmrServices.checkWTAAS(cmrNumber, '834');
						if ((resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status && resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null && resultWTAAS_SG.Error_Msg.toLowerCase()
							.includes("does not exist"))) {
							return new ValidationResult(null, true);
						} else if (!resultWTAAS_SG.success) {
							return new ValidationResult(null, false, 'Cannot connect to WTAAS at the moment. Please try again after sometime.');
						} else {
							return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in WTAAS.');
						}
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addDoubleCreateValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var role = FormManager.getActualValue('userRole').toUpperCase();
				var reqType = FormManager.getActualValue('reqType');
				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var custGrp = FormManager.getActualValue('custGrp');
				if (reqType == 'U') {
					return new ValidationResult(null, true);
				}

				if (FormManager.getActualValue('cmrNo') != '' && FormManager.getActualValue('cmrNoPrefix') != '') {
					showError = true;
				} else {
					showError = false;
				}

				if ((cntry == '615' && role == 'PROCESSOR' && (custGrp == 'LOCAL' || custGrp == 'CROSS'))
					|| (cntry == '652' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custGrp == 'LOCAL' || custGrp == 'CROSS'))
					|| (cntry == '744' && role == 'PROCESSOR' && (custSubGrp == 'ESOSW' || custSubGrp == 'XESO'))
					|| (cntry == '852' && role == 'PROCESSOR' && (custSubGrp != 'BLUMX' || custSubGrp != 'MKTPC'))) {
					if (showError)
						return new ValidationResult(null, false, 'For CMR Number and CMR Number Prefix in case of double creates, only one can be filled.');
					else if (FormManager.getActualValue('cmrNo') != '') {
						var cmrNumber = FormManager.getActualValue('cmrNo');
						var katr6 = FormManager.getActualValue('cmrIssuingCntry');
						qParams = {
							KATR6: katr6,
							MANDT: cmr.MANDT,
							ZZKV_CUSNO: cmrNumber,
						};
						var record = cmr.query('CHECK_CMR_RDC', qParams);
						var rdcCMRCount = record.ret1;
						qParams = {
							KATR6: '834',
							MANDT: cmr.MANDT,
							ZZKV_CUSNO: cmrNumber,
						};
						var recordSG = cmr.query('CHECK_CMR_RDC', qParams);
						var rdcCMRCountSG = recordSG.ret1;
						if (Number(rdcCMRCount) > 0 || Number(rdcCMRCountSG) > 0) {

							return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in RDC.');
						}

						var resultWTAAS = CmrServices.checkWTAAS(cmrNumber, katr6);
						var resultWTAAS_SG = CmrServices.checkWTAAS(cmrNumber, '834');
						if ((resultWTAAS.success && resultWTAAS.data && resultWTAAS.Status && resultWTAAS.Status == 'F' && resultWTAAS.Error_Msg != null && resultWTAAS.Error_Msg.toLowerCase().includes(
							"does not exist"))
							&& (resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status && resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null && resultWTAAS_SG.Error_Msg
								.toLowerCase().includes("does not exist"))) {
							return new ValidationResult(null, true);
						} else if (!resultWTAAS.success || !resultWTAAS_SG.success) {
							return new ValidationResult(null, false, 'Cannot connect to WTAAS at the moment. Please try again after sometime.');
						} else {
							return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in WTAAS.');
						}
					} else if (FormManager.getActualValue('cmrNoPrefix') != '') {
						return new ValidationResult(null, true);
					} else {
						// FormManager.resetValidations('cmrNoPrefix');
						FormManager.enable('cmrNo');
						FormManager.enable('cmrNoPrefix');
						return new ValidationResult(null, false, 'Please enter either CMR Number or CMR Number Prefix in case of double creates.');
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function setAddressDetailsForViewAP() {
	console.log('>>>> setAddressDetailsForViewAP >>>>');
	var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
	var asean_isa_cntries = [SysLoc.BRUNEI, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.SINGAPORE, SysLoc.PHILIPPINES, SysLoc.THAILAND, SysLoc.VIETNAM, SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH,
	SysLoc.HONG_KONG, SysLoc.MACAO];
	if ((cmrIssuingCntry == SysLoc.AUSTRALIA || cmrIssuingCntry == SysLoc.NEW_ZEALAND)) {
		$('label[for="city1_view"]').text('Suburb:');
	}
	if (asean_isa_cntries.indexOf(cmrIssuingCntry) > -1) {
		$('label[for="addrTxt_view"]').text("Address.(Flr,Bldg,lvl,Unit.):");
		$('label[for="addrTxt2_view"]').text("Address. Cont1(Street Name, Street No.):");
		$('label[for="dept_view"]').text("Address. Cont2(District,Town,Region):");

	}
}
/*
 * Story 1734332 : Customer Name & Customer Name Con't in Address tab need to be
 * set as non-editable for processors
 */
function lockCustMainNames() {
	console.log('>>>> lockCustMainNames >>>>');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	if (role == 'REQUESTER' || FormManager.getActualValue('viewOnlyPage') == 'true')
		return;
	if (cmr.addressMode == 'updateAddress') {
		FormManager.readOnly('custNm1');
		FormManager.readOnly('custNm2');
	} else {
		FormManager.enable('custNm1');
		FormManager.enable('custNm2');
	}
}


function setCollCdFrSGOnAddrSave(cntry, addressMode, saving, finalSave, force) {
	console.log('>>>> setCollCd for Singapore >>>>');
	var addrType = FormManager.getActualValue('addrType');
	var custSubType = FormManager.getActualValue('custSubGrp');
	if (custSubType != 'SPOFF')
		return;

	if (typeof (_pagemodel) != 'undefined') {
		reqType = FormManager.getActualValue('reqType');
	}
	if (reqType == 'U') {
		return;
	}
	if ((finalSave || force) && cmr.addressMode) {
		var copyTypes = document.getElementsByName('copyTypes');
		var copyingToA = false;
		if (copyTypes != null && copyTypes.length > 0) {
			copyTypes.forEach(function(input, i) {
				if (input.value == 'ZS01' && input.checked) {
					copyingToA = true;
				}
			});
		}
		if (addrType == 'ZS01' || copyingToA)
			setCollCdFrSingapore();
	}
}

function setCollCdFrSingapore() {
	console.log('>>>> setCollCdFrSingapore >>>>');
	var custSubType = FormManager.getActualValue('custSubGrp');
	if (custSubType != 'SPOFF')
		return;
	var zs01ReqId = FormManager.getActualValue('reqId');
	var collCd = null;
	var qParams = {
		REQ_ID: zs01ReqId,
	};
	var landCntry = FormManager.getActualValue('landCntry');
	if (landCntry == '' || !landCntry) {
		var result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', qParams);
		landCntry = result.ret1;
	}
	switch (landCntry) {
		case 'BD':
		case 'IN':
		case 'LK':
		case 'MV':
			collCd = 'I001';
			break;
		case 'HK':
		case 'MO':
			collCd = 'S014';
			break;
		case 'TW':
			collCd = 'S015';
			break;
		case 'CN':
			collCd = 'C013';
			break;
		case 'MY':
			collCd = 'M001';
			break;
		case 'PH':
			collCd = 'P001';
			break;
		case 'TH':
			collCd = 'T001';
			break;
		case 'VN':
			collCd = 'V001';
			break;
		case 'ID':
			collCd = 'S001';
			break;
		default:
			collCd = 'S013';
	}
	FormManager.setValue('collectionCd', collCd);
}

/*
 * Story CMR-1753 : For the condition, "When there is value in both street
 * address and street address con't. " Provide a validation on check request as
 * well as address level to ensure that length of 'street address' is 27
 */
function addAddressLengthValidators() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var addrTxt = FormManager.getActualValue('addrTxt');
				var addrTxt2 = FormManager.getActualValue('addrTxt2');

				if (addrTxt != '' && addrTxt2 != '') {
					if (addrTxt.length > 27) {
						return new ValidationResult(null, false, 'Street address should not exceed 27 characters when both Street and Street Con\'t present.');
					} else {
						return new ValidationResult(null, true);
					}
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), null, 'frmCMR_addressModal');
}

function addStreetValidationChkReq() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqId = FormManager.getActualValue('reqId');
				var qParams = {
					_qall: 'Y',
					REQID: reqId
				};

				var results = cmr.query('GET_STREET_LENGTH_AU', qParams);
				if (results != null) {
					for (var i = 0; i < results.length; i++) {
						if (results[i].ret1 != '' && results[i].ret2 != '' && results[i].ret1.length > 27) {
							return new ValidationResult(null, false, 'Street address should not exceed 27 characters when both Street and Street Con\'t present.');
						}
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function validateContractAddrAU() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custNm1 = FormManager.getActualValue('custNm1').toUpperCase();;
				var custNm2 = FormManager.getActualValue('custNm2').toUpperCase();;
				var dept = FormManager.getActualValue('dept').toUpperCase();;
				var addrTxt1 = FormManager.getActualValue('addrTxt').toUpperCase();;
				var addrTxt2 = FormManager.getActualValue('addrTxt2').toUpperCase();;
				var city1 = FormManager.getActualValue('city1').toUpperCase();;
				var addrType = FormManager.getActualValue('addrType');
				var address = custNm1 + custNm2 + dept + addrTxt1 + addrTxt2 + city1;
				if ((address.includes("PO BOX") || address.includes("POBOX")) && addrType == "ZS01") {
					return new ValidationResult(null, false, 'Contract address can not contain wording PO BOX');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');
}

function validateCustNameForNonContractAddrs() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custNm1 = FormManager.getActualValue('custNm1').toUpperCase();
				var custNm2 = FormManager.getActualValue('custNm2').toUpperCase();
				var custNm = custNm1 + custNm2;
				var reqId = FormManager.getActualValue('reqId');
				var addrType = FormManager.getActualValue('addrType');
				var reqType = FormManager.getActualValue('reqType');
				var contractCustNm = cmr.query('GET.CUSTNM_ADDR', {
					REQ_ID: reqId,
					ADDR_TYPE: 'ZS01'
				});
				if (contractCustNm != undefined) {
					zs01CustName = contractCustNm.ret1.toUpperCase() + contractCustNm.ret2.toUpperCase();
				}

				if (zs01CustName != custNm && addrType != "ZS01" && _pagemodel.reqType == 'U') {
					return new ValidationResult(null, false, 'customer name of additional address must be the same as the customer name of contract address');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');
}

function validateStreetAddrCont2() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var streetAddrCont1 = FormManager.getActualValue('addrTxt2');
				var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
				var streetAddrCont2 = "";
				if (cmrCntry == '738' || cmrCntry == '736') {
					streetAddrCont2 = FormManager.getActualValue('city1');
				} else {
					streetAddrCont2 = FormManager.getActualValue('dept');
				}
				if (streetAddrCont1 == '' && streetAddrCont2 != '') {
					return new ValidationResult(null, false, 'Street Address Con\'t2 cannot have a value as Street Address Con\'t1 is blank.');
				}

				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');
}

// API call for validating GST for India on Save Request and Send for Processing
/*
 * function validateGSTForIndia() {
 * 
 * FormManager.addFormValidator((function() { return { validate : function() {
 * var cntry = FormManager.getActualValue('cmrIssuingCntry'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); var reqTyp =
 * FormManager.getActualValue('reqType'); var vat =
 * FormManager.getActualValue('vat'); var reqId =
 * FormManager.getActualValue('reqId'); if
 * (dijit.byId('vatExempt').get('checked')) { return new ValidationResult(null,
 * true); } if (cntry != '744' || custSubGrp == 'CROSS') { return new
 * ValidationResult(null, true); } var country = ""; if (SysLoc.INDIA ==
 * FormManager.getActualValue('cmrIssuingCntry')) { country = "IN"; if (country !=
 * '') { if (vat == '') { return new ValidationResult(null, true); } else { if
 * (reqId != null) { reqParam = { REQ_ID : reqId }; } var results =
 * cmr.query('GET_ZS01', reqParam); if (results.ret1 != undefined) {
 * 
 * var name = results.ret1; var address = results.ret2; var postal =
 * results.ret3; var city = results.ret4; var state = results.ret5; var country =
 * '';
 * 
 * if (state != null && state != '') { reqParam = { STATE_PROV_CD : state, };
 * var stateResult = cmr.query('GET_STATE_DESC', reqParam); if (stateResult !=
 * null) { country = stateResult.ret1; } } var gstRet = cmr.validateGST(country,
 * vat, name, address, postal, city); if (!gstRet.success) { return new
 * ValidationResult({ id : 'vat', type : 'text', name : 'vat' }, false,
 * gstRet.errorMessage); } else { return new ValidationResult(null, true); }
 * }else { return new ValidationResult(null, true); } } } else { return new
 * ValidationResult(null, true); } } } }; })(), 'MAIN_CUST_TAB', 'frmCMR'); }
 */

// API call for validating ABN for Australia on Save Request and Send for
// Processing
function validateABNForAU() {

	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				var reqTyp = FormManager.getActualValue('reqType');
				var vat = FormManager.getActualValue('vat');
				var reqId = FormManager.getActualValue('reqId');
				var formerAbn = getFormerVatAU(reqId);
				if (reqTyp == 'C') {
					return new ValidationResult(null, true);
				}
				var country = "";
				if (SysLoc.AUSTRALIA == FormManager.getActualValue('cmrIssuingCntry')) {
					country = "AU";
					if (country != '') {
						if (vat == '') {
							return new ValidationResult(null, true);
						} else {
							if (reqId != null) {
								reqParam = {
									REQ_ID: reqId
								};
							}
							var abnRet = cmr.validateABN(vat, reqId, formerAbn);
							if (!abnRet.success) {
								return new ValidationResult({
									id: 'vat',
									type: 'text',
									name: 'vat'
								}, false, abnRet.errorMessage);
							} else {
								return new ValidationResult(null, true);
							}
						}
					} else {
						return new ValidationResult(null, true);
					}
				}
			}
		};
	})(), 'MAIN_CUST_TAB', 'frmCMR');
}

function getFormerVatAU(reqId) {
	console.log('>>>> getFormerVatAU >>>>');
	var formerVat = '';
	var qParams = {
		REQ_ID: reqId,
	};
	var result = cmr.query('GET.VAT_DATA_RDC', qParams);
	if (result != null) {
		formerVat = result.ret1;
	}
	return formerVat;
}

function getFormerCustNameAU(reqId) {
	console.log('>>>> getFormerCustNameAU >>>>');
	var custNm1 = '';
	var custNM2 = '';
	var formerCustNm = '';
	var qParams = {
		REQ_ID: reqId,
		ADDR_TYPE: "ZS01"
	};
	var result = cmr.query('GET.CUSTNM_DATA_RDC', qParams);
	if (result != null) {
		formerCustNm = result.ret1.toUpperCase() + " " + result.ret2.toUpperCase();
	}
	return formerCustNm;
}

function lockFieldsForERO() {
	console.log('>>>> Lock Fields for ERO Temporary Reactivate >>>>');
	var reqType = FormManager.getActualValue('reqType');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var reqReason = FormManager.getActualValue('reqReason');
	if (reqType == 'U' && role == 'REQUESTER' && reqReason == 'TREC') {
		FormManager.readOnly('abbrevNm');
		// FormManager.readOnly('abbrevLocn');
		FormManager.readOnly('custPrefLang');
		// FormManager.readOnly('subIndustryCd');
		FormManager.readOnly('isicCd');
		FormManager.readOnly('taxCd1');
		FormManager.readOnly('cmrNo');
		FormManager.readOnly('cmrOwner');
		FormManager.resetValidations('cmrOwner');

		// FormManager.readOnly('apCustClusterId');
		// FormManager.resetValidations('apCustClusterId');

		// FormManager.readOnly('clientTier');
		// FormManager.resetValidations('clientTier');

		// FormManager.readOnly('isuCd');
		// FormManager.readOnly('mrcCd');
		FormManager.readOnly('bpRelType');
		FormManager.readOnly('bpName');
		FormManager.readOnly('busnType');
		FormManager.resetValidations('busnType');

		// FormManager.readOnly('cmrNoPrefix');
		// FormManager.readOnly('collectionCd');
		// FormManager.resetValidations('collectionCd');

		// FormManager.readOnly('repTeamMemberNo');
		// FormManager.resetValidations('repTeamMemberNo');

		FormManager.readOnly('miscBillCd');
		FormManager.readOnly('inacType');
		FormManager.readOnly('inacCd');
		FormManager.readOnly('restrictInd');
		// FormManager.readOnly('govType');
		// FormManager.readOnly('repTeamMemberName');
		// FormManager.readOnly('covId');
		// FormManager.resetValidations('covId');

		FormManager.readOnly('dunsNo');
	}
}

function releaseFieldsFromERO() {
	console.log('>>>> Releasing fields from ERO Temporary Reactivate >>>>');
	var reqType = FormManager.getActualValue('reqType');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var reqReason = FormManager.getActualValue('reqReason');
	if (reqType == 'U' && role == 'REQUESTER' && reqReason != 'TREC') {
		FormManager.enable('abbrevNm');
		FormManager.enable('custPrefLang');
		FormManager.enable('isicCd');

		FormManager.enable('bpRelType');
		FormManager.enable('bpName');
		FormManager.enable('busnType');

		FormManager.enable('miscBillCd');
		FormManager.enable('inacType');
		FormManager.enable('inacCd');
		FormManager.enable('restrictInd');

		FormManager.enable('dunsNo');
	}
}

function lockFieldsForAU() {
	console.log('>>>> lockFieldsForAU >>>>');
	var clusterCd = FormManager.getActualValue('apCustClusterId');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	if (['ESOSW', 'NRML', 'AQSTN', 'SOFT', 'XAQST', 'CROSS'].includes(custSubGrp) && ['04500', '01150', '08039', '09057'].includes(clusterCd)) {
		FormManager.readOnly('repTeamMemberName');
	} else if (cntry = '796' && role == 'REQUESTER') {
		FormManager.readOnly('repTeamMemberName');
	} else {
		FormManager.enable('repTeamMemberName');
	}
}


function checkForCompanyProofAttachment() {
	var id = FormManager.getActualValue('reqId');
	var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
		ID: id
	});
	if (ret == null || ret.ret1 == null) {
		return true;
	} else {
		return false;
	}
}

// CMR-2830
function addressNameSimilarValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
					return new ValidationResult(null, true);
				}

				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var type = null;
					var name = null;
					var name1 = '';
					var name2 = '';
					var count = 0;
					var reqId = FormManager.getActualValue('reqId');
					var reqType = FormManager.getActualValue('reqType');
					var updateInd = null;
					qParams_z = {
						REQ_ID: reqId,
					};
					var record = cmr.query('GETZS01VALRECORDS', qParams_z);
					var zs01Reccount = record.ret1;
					if (zs01Reccount == 0) {
						return new ValidationResult(null, true);
					}
					var qParams = {
						REQ_ID: reqId,
						ADDR_TYPE: "ZS01",
					};
					var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
					var zs01Name = result != undefined ? result.ret1.concat(result.ret2) : '';
					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						if (record == null && _allAddressData != null && _allAddressData[i] != null) {
							record = _allAddressData[i];
						}
						type = record.addrType;
						name1 = record.custNm1;
						name2 = record.custNm2;
						updateInd = record.updateInd;
						if (typeof (type) == 'object') {
							type = type[0];
						}
						if (typeof (type) == 'object') {
							name1 = name1[0];
						}
						if (typeof (type) == 'object') {
							name2 = name2[0];
						}
						if (typeof (type) == 'object') {
							updateInd = updateInd[0];
						}
						name = name1 + name2;
						if (((reqType == 'U' && (updateInd == 'U' || updateInd == 'N')) || reqType == 'C') && type != 'ZS01' && zs01Name != name) {
							count++;
						}
					}
					if (count > 0) {
						return new ValidationResult(null, false, 'All Updated / New address customer name should be same as Mailing address.');
					} else {
						return new ValidationResult(null, true);
					}
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addValidatorBasedOnCluster() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custSubType = FormManager.getActualValue('custSubGrp');
				var cluster = FormManager.getActualValue('apCustClusterId');
				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				if (FormManager.getActualValue('reqType') != 'C' || cntry == '616' || cntry == '778' || cntry == '749' || cntry == '834') {
					return new ValidationResult(null, true);
				}
				if ((cntry == '856' && (custSubType == 'CROSS' || custSubType == 'XASLM') && cluster == '08047') ||
					(cntry == '818' && (custSubType == 'CROSS' || custSubType == 'XASLO') && cluster == '08044') ||
					(cntry == '852' && (custSubType == 'CROSS' || custSubType == 'XASLM') && cluster == '08046')) {
					return new ValidationResult(null, true);
				}
				if ((custSubType != 'ECSYS' && custSubType != 'XECO' && custSubType != 'ASLOM' && custSubType != 'ESOSW') && (cluster == '08039' || cluster == '08037' || cluster == '08038' || cluster == '08040' || cluster == '08042' || cluster == '08044' || cluster == '08047' || cluster == '08046')) {
					return new ValidationResult(null, false, 'Ecosystem Partners Cluster is not allowed for selected scenario.');
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function getAbbrvNameForBP(custNm1, bpRelTypeValChange) {
	var abbvNmRegex = /^(Dis|Sol|Res)(1|2|3)_/;
	var abbvNmVal = FormManager.getActualValue('abbrevNm');
	var bpRelPrefix = '';

	if (!abbvNmRegex.test(abbvNmVal) || bpRelTypeValChange) {
		var bpRelTypeVal = FormManager.getActualValue('bpRelType');
		if (bpRelTypeVal == 'DS') {
			bpRelPrefix = 'Dis2_';
		} else if (bpRelTypeVal == 'SP') {
			bpRelPrefix = 'Sol1_';
		} else if (bpRelTypeVal == 'RS') {
			bpRelPrefix = 'Res3_';
		}
	}

	if (bpRelPrefix != '') {
		return bpRelPrefix + custNm1;
	} else {
		return custNm1;
	}
}

function setAbbrvNameBPScen() {
	console.log('>>>> setAbbrvNameBPScen >>>>');
	var scenario = FormManager.getActualValue('custSubGrp');

	if (scenario == 'BUSPR' || scenario == 'XBUSP') {
		var zs01ReqId = FormManager.getActualValue('reqId');
		var qParams = {
			REQ_ID: zs01ReqId,
		};
		var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
		var custNm1 = FormManager.getActualValue('custNm1');
		if (custNm1 == '') {
			custNm1 = result.ret1;
		}
		var abbvName = getAbbrvNameForBP(custNm1, true);
		if (abbvName && abbvName.length > 21) {
			abbvName = abbvName.substring(0, 21);
		}
		FormManager.setValue('abbrevNm', abbvName);
	}
}

function lockAbbvNameOnScenarioChangeGCG() {
	console.log('>>>> lockAbbvNameOnScenarioChangeGCG >>>>');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var reqType = FormManager.getActualValue('reqType');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');

	if (role == 'REQUESTER' && reqType == 'C' && (cntry == SysLoc.HONG_KONG || cntry == SysLoc.MACAO)) {
		FormManager.readOnly('abbrevNm');
	}
}

function handleExpiredClusterGCG() {
	console.log('>>>> handleExpiredClusterGCG >>>>');
	var reqType = FormManager.getActualValue('reqType');
	if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}

	var clusterDataRdc = getAPClusterDataRdc();
	if (clusterDataRdc != null && clusterDataRdc != undefined && clusterDataRdc != '') {
		var clusterExpired = checkClusterExpired(clusterDataRdc);
		if (clusterExpired) {
			FormManager.readOnly('clientTier');
		}
	}
}

function getAPClusterDataRdc() {
	console.log('>>>> getAPClusterDataRdc >>>>');
	var clusterDataRdc = '';
	var reqId = FormManager.getActualValue('reqId');
	var qParams = {
		REQ_ID: reqId,
	};
	var result = cmr.query('GET.CLUSTER_DATA_RDC', qParams);
	if (result != null) {
		clusterDataRdc = result.ret1;
	}
	return clusterDataRdc;
}

function checkClusterExpired(clusterDataRdc) {
	console.log('>>>> checkClusterExpired >>>>');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var qParams = {
		ISSUING_CNTRY: cntry,
		AP_CUST_CLUSTER_ID: clusterDataRdc,
	};
	var results = cmr.query('CHECK.CLUSTER', qParams);
	if (results != null && results.ret1 == '1') {
		return false;
	}
	return true;
}

function checkExpiredData() {
	console.log('>>>> checkExpiredData >>>>');
	var reqId = FromManager.getActualValue('reqId')
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var DataRdc = {};

	var qParams = {
		REQ_ID: reqId,
	};
	var records = cmr.query('SUMMARY.OLDDATA', qParams);
	if (records != null && records.size() > 0) {
		return false;
	}
	return true;
}

function addCtcObsoleteValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = FormManager.getActualValue('reqType');
				var reqId = FormManager.getActualValue('reqId');
				var clientTier = FormManager.getActualValue('clientTier');
				var oldCtc;
				var qParams = {
					REQ_ID: reqId
				};

				var result = cmr.query('GET.DATA_RDC.CLIENT_TIER_REQID', qParams);
				if (result != null && result != '') {
					var oldCtc = result.ret1;
				}

				if (clientTier == "T" && FormManager.getActualValue('cmrIssuingCntry') == '856') {
					console.log('>>> Skip CTC Obsolete Validator clientTier = T for TH');
					return new ValidationResult(null, true);
				}

				if (clientTier == "T" && FormManager.getActualValue('cmrIssuingCntry') == '744') {
					console.log('>>> Skip CTC Obsolete Validator clientTier = T for IN');
					return new ValidationResult(null, true);
				}

				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				if (reqType == 'C' && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "B" || clientTier == "M" || clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
					// CREATCMR-7884
					if (clientTier == "T" && cntry == '796') {
						var custSubGrp = FormManager.getActualValue('custSubGrp');
						var custSubGrpList = ['NRML', 'ESOSW', 'XESO', 'CROSS'];
						if (custSubGrpList.includes(custSubGrp)) {
							console.log('>>> Skip CTC Obsolete Validator for NRML/ESOSW/XESO/CROSS when clientTier = T');
							return new ValidationResult(null, true);
						}
						if (clientTier == "T" && (cntry == '736' || cntry == '738')) {
							return new ValidationResult(null, true);
						}
						if (clientTier == "T" && cntry == '616') {
							console.log('>>> Skip CTC Obsolete Validator clientTier = T for AU');
							return new ValidationResult(null, true);
						}
						// CREATCMR-7887
						if (cntry == '778' || cntry == '749' || cntry == '834') {
							return new ValidationResult(null, true);
						}
						return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid value from list.');
					} else if (reqType == 'U' && oldCtc != null && oldCtc != clientTier && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "B" || clientTier == "M" || clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
						if (clientTier == "T" && (cntry == '736' || cntry == '738' || cntry == '834')) {
							return new ValidationResult(null, true);
						}
						return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid Client tier value from list.');
					} else {
						return new ValidationResult(null, true);
					}
				} else if (reqType == 'U' && oldCtc != null && oldCtc != clientTier && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "B" || clientTier == "M" || clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
					if (clientTier == "T" && (cntry == '736' || cntry == '738' || cntry == '834')) {
						return new ValidationResult(null, true);
					}
					return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid Client tier value from list.');
				} else {
					return new ValidationResult(null, true);
				}
			}
		}
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

var _custSubGrpHandler = null;
function custSubGrpHandlerINAUSG() {
	console.log('>>>> custSubGrpHandler >>>>');
	if (_custSubGrpHandler == null) {
		_custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
			onIsicChange();
		});
	}
}


function clusterCdValidatorAU() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var clusterCd = FormManager.getActualValue('apCustClusterId');
				var custSubGrp = FormManager.getActualValue('custSubGrp');

				if (['ESOSW', 'NRML', 'AQSTN', 'SOFT', 'XAQST', 'CROSS'].includes(custSubGrp)) {
					if (clusterCd == '00001') {
						return new ValidationResult({
							id: 'apCustClusterId',
							type: 'text',
							name: 'apCustClusterId'
						}, false, 'Please enter valid Cluster Code, for this scenario request should not be default \'00001\'.');
					} else {
						return new ValidationResult(null, true);
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateClusterBaseOnScenario() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custSubType = FormManager.getActualValue('custSubGrp');
				var cluster = FormManager.getActualValue('apCustClusterId');
				if (FormManager.getActualValue('reqType') != 'C') {
					return new ValidationResult(null, true);
				}
				var applicableScenarios = ["ASLOM", "BUSPR", "NRML", "CROSS", "AQSTN", "XAQST"];
				if (applicableScenarios.includes(custSubType) && cluster == '00000') {
					return new ValidationResult(null, false, "Cluster '00000 - Singapore Default' is not allowed on ASL/OEM, Acquisition, Business Partner, Foreign and Normal scenario sub-type.");
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateCustNameForInternal() {
	console.log("running validateCustNameForInternal . . .");
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custSubType = FormManager.getActualValue('custSubGrp');
				if (custSubType == 'INTER') {
					var custNm = '';
					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						type = record.addrType;

						if (typeof (type) == 'object') {
							type = type[0];
						}

						custNm = record.custNm1 == null ? '' : record.custNm1;
						custNm = custNm.toString().toUpperCase();
						console.log("validateCustNameForInternal Customer name value is " + custNm);
					}
					var result = custNm.startsWith("IBM");
					if (!result) {
						return new ValidationResult(null, false, "Customer Name should start with 'IBM' for Internal Sub-scenario.");
					} else {
						return new ValidationResult(null, true);
					}
				} else {
					return new ValidationResult(null, true);
				}
			}
		}
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function getImportIndForIndia(reqId) {
	var results = cmr.query('VALIDATOR.IMPORTED_IN', {
		REQID: reqId
	});
	var importInd = 'N';
	if (results != null && results.ret1) {
		importInd = results.ret1;
	}
	console.log('import indicator value for request ID: ' + reqId + ' is ' + importInd);
	return importInd;
}

function lockInacTypeForIGF() {
	console.log('>>>> lockInacTypeForIGF >>>>');
	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var reqId = FormManager.getActualValue('reqId');

	if (role == 'REQUESTER' && custSubGrp == 'IGF') {
		if (_importIndIN != null) {
			if (_importIndIN == 'Y') {
				FormManager.setValue('inacType', '');
				FormManager.readOnly('inacType');
			}
		}
	}
}

function lockInacCodeForIGF() {
	console.log('>>>> lockInacCodeForIGF >>>>');
	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var reqId = FormManager.getActualValue('reqId');

	if (role == 'REQUESTER' && custSubGrp == 'IGF') {
		var existingCmr = getImportIndForIndia(reqId);
		if (existingCmr == 'Y') {
			_importIndIN = existingCmr;
			FormManager.setValue('inacCd', '');
			FormManager.readOnly('inacCd');
			lockInacTypeForIGF();
		}
	}
}

// CMR - 5258
function addVatValidationforSingapore() {
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var flag = "N";
	if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
		for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
			record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
			landCntry = record.landCntry;
			if (landCntry == 'TH') {
				flag = "Y";
			}
		}
	}
	if (cntry == '834' && flag == "Y") {
		FormManager.addValidator('vat', Validators.REQUIRED, ['UEN#'], 'MAIN_CUST_TAB');
	}
}
// CREATCMR-5258
// CREATCMR -5269
function handleObseleteExpiredDataForUpdate() {
	console.log('>>>> handleObseleteExpiredDataForUpdate >>>>');
	var reqType = FormManager.getActualValue('reqType');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	// lock all the coverage fields and remove validator
	if (reqType == 'U' && cntry != SysLoc.HONG_KONG || cntry != SysLoc.MACAO) {
		FormManager.readOnly('repTeamMemberNo');
		FormManager.readOnly('repTeamMemberName');
		FormManager.readOnly('isbuCd');
		FormManager.readOnly('covId');
		FormManager.readOnly('cmrNoPrefix');
		FormManager.readOnly('engineeringBo');
		FormManager.readOnly('commercialFinanced');
		FormManager.readOnly('creditCd');
		FormManager.readOnly('contactName2');
		FormManager.readOnly('contactName3');
		FormManager.readOnly('busnType');
		FormManager.readOnly('taxCd2');
		FormManager.readOnly('cmrOwner');

		// setting all fields as not Mandt for update Req
		FormManager.removeValidator('apCustClusterId', Validators.REQUIRED);
		FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
		FormManager.removeValidator('taxCd2', Validators.REQUIRED);
		FormManager.removeValidator('cmrOwner', Validators.REQUIRED);
		FormManager.removeValidator('clientTier', Validators.REQUIRED);
		FormManager.removeValidator('isuCd', Validators.REQUIRED);
		FormManager.removeValidator('mrcCd', Validators.REQUIRED);
		FormManager.removeValidator('inacType', Validators.REQUIRED);
		FormManager.removeValidator('inacCd', Validators.REQUIRED);
		FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
		FormManager.removeValidator('repTeamMemberName', Validators.REQUIRED);
		FormManager.removeValidator('isbuCd', Validators.REQUIRED);
		FormManager.removeValidator('covId', Validators.REQUIRED);
		FormManager.removeValidator('collectionCd', Validators.REQUIRED);
		FormManager.removeValidator('engineeringBo', Validators.REQUIRED);
		FormManager.removeValidator('commercialFinanced', Validators.REQUIRED);
		FormManager.removeValidator('creditCd', Validators.REQUIRED);
		FormManager.removeValidator('contactName2', Validators.REQUIRED);
		FormManager.removeValidator('contactName3', Validators.REQUIRED);
		FormManager.removeValidator('busnType', Validators.REQUIRED);
	}
}

function executeBeforeSubmit() {
	console.log('>>>> executeBeforeSubmit >>>>');
	var reqType = FormManager.getActualValue('reqType');
	var action = FormManager.getActualValue('yourAction');

	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	if (cntry == SysLoc.SINGAPORE || cntry == SysLoc.INDIA) {
		if (reqType == 'U') {
			var errMsg = checkAnyChangesOnCustNameAddrGST(cntry);
			if (errMsg != '' && action == 'SFP') {
				cmr.showAlert(errMsg);
			} else {
				showVerificationModal();
			}
		} else {
			showVerificationModal();
		}
	}
}

function showVerificationModal() {
	cmr.showModal('addressVerificationModal');
}

function checkAnyChangesOnCustNameAddrGST(cntry) {
	console.log('>>>> checkAnyChangesOnCustNameAddrGST >>>>');
	var errorMsg = '';
	var isUpdated = false;

	if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
		var record = null;
		var updateInd = null;

		for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
			record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
			updateInd = record.updateInd;
			if (typeof (updateInd) == 'object') {
				updateInd = updateInd[0];
				if (updateInd == 'U' || updateInd == 'N') {
					isUpdated = true;
					break;
				}
			}
		}
	}
	if (!isUpdated) {
		var currentGst = FormManager.getActualValue('vat');
		var qParams = {
			REQ_ID: FormManager.getActualValue('reqId'),
		};

		var result = cmr.query('GET.OLD_VAT_BY_REQID', qParams);
		var oldGst = result.ret1;
		oldGst = oldGst == undefined ? '' : oldGst;

		if (result != null && oldGst != null && oldGst != currentGst) {
			isUpdated = true;
		}
	}
	if (!isUpdated) {
		if (cntry != '') {
			if (cntry == SysLoc.SINGAPORE) {
				errorMsg = 'You haven\'t updated anything on customer name/address or UEN#, please check and take relevant edit operation before submit this Update request.';
			} else if (cntry == SysLoc.INDIA) {
				errorMsg = 'You haven\'t updated anything on customer name/address or GST#, please check and take relevant edit operation before submit this Update request.';
			}
		}
	}
	return errorMsg;
}

// CREATCMR-6880
function vatRegistrationForSG() {
	console.log('>>>> vatRegistrationForSG >>>>');
	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	var reqId = FormManager.getActualValue('reqId');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var addrType = FormManager.getActualValue('addrType');
	var landCntry = '';
	if (addrType == 'ZS01') {
		landCntry = FormManager.getActualValue('landCntry');
	}
	if (landCntry == '') {
		var params = {
			REQ_ID: reqId,
			ADDR_TYPE: "ZS01"
		};
		var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
		landCntry = landCntryResult.ret1;
	}
	if (cntry == '834' && landCntry == 'TH') {
		FormManager.addValidator('taxCd1', Validators.REQUIRED, ['Vat Registration Status'], 'MAIN_IBM_TAB');
		var isVatRegistered = FormManager.getActualValue('taxCd1');
		if (isVatRegistered == 'NA') {
			FormManager.readOnly('vat');
			FormManager.setValue('vat', '');
			FormManager.removeValidator('vat', Validators.REQUIRED);
		} else {
			FormManager.addValidator('vat', Validators.REQUIRED, ['VAT'], 'MAIN_IBM_TAB');
			FormManager.enable('vat');
		}
	}
}

function displayVatRegistrartionStatus() {
	console.log(">>> Executing displayVatRegistrationStatus");
	var reqId = FormManager.getActualValue('reqId');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var addrType = FormManager.getActualValue('addrType');
	var landCntry = '';
	if (addrType == 'ZS01') {
		landCntry = FormManager.getActualValue('landCntry');
	}
	if (landCntry == '') {
		var params = {
			REQ_ID: reqId,
			ADDR_TYPE: "ZS01"
		};
		var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
		landCntry = landCntryResult.ret1;
	}
	if (cntry == '834' && landCntry == 'TH') {
		cmr.showNode('vatRegisterStatus');
	} else {
		cmr.hideNode('vatRegisterStatus');
	}
	vatRegistrationForSG();
}

// CREATCMR-6398
function businessParterValidator() {
	console.log("running businessParterValidator...");
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = FormManager.getActualValue('reqType');
				var custSubType = FormManager.getActualValue('custSubGrp');
				var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
				if (typeof (_pagemodel) != 'undefined') {
					if (cmrIssuingCntry == '834' && reqType == 'C' && (custSubType == 'BUSPR' || custSubType == 'XBUSP')) {
						var id = FormManager.getActualValue('reqId');
						var ret = cmr.query('CHECK_BUSP_MATCH_ATTACHMENT', {
							ID: id
						});

						if (ret == null || ret.ret1 == null || ret.ret1 == 0) {
							return new ValidationResult(null, false, 'Business Partner Proof attachment is required for Scenario Sub-type \'Business Partner\'.');
						} else {
							return new ValidationResult(null, true);
						}
					} else {
						return new ValidationResult(null, true);
					}
				}
			}
		};
	})(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

// CREATCMR-6358
function addressNameSameValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
					return new ValidationResult(null, true);
				}

				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var type = null;
					var name = null;
					var name1 = '';
					var name2 = '';
					var count = 0;
					var reqId = FormManager.getActualValue('reqId');
					var reqType = FormManager.getActualValue('reqType');
					// var updateInd = null;
					qParams_z = {
						REQ_ID: reqId,
					};
					var record = cmr.query('GETZS01VALRECORDS', qParams_z);
					var zs01Reccount = record.ret1;
					if (zs01Reccount == 0) {
						return new ValidationResult(null, true);
					}
					var qParams = {
						REQ_ID: reqId,
						ADDR_TYPE: "ZS01",
					};
					var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
					var zs01Name = result != undefined ? result.ret1.concat(result.ret2) : '';
					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						if (record == null && _allAddressData != null && _allAddressData[i] != null) {
							record = _allAddressData[i];
						}
						type = record.addrType;
						name1 = record.custNm1;
						name2 = record.custNm2;
						// updateInd = record.updateInd;
						if (typeof (type) == 'object') {
							type = type[0];
						}
						if (typeof (type) == 'object') {
							name1 = name1[0];
						}
						if (typeof (type) == 'object') {
							name2 = name2[0];
						}
						// if (typeof (type) == 'object') {
						// updateInd = updateInd[0];
						// }
						name = name1 + name2;
						// if(((reqType == 'U' && (updateInd == 'U' || updateInd ==
						// 'N')) || reqType == 'C') && type != 'ZS01' && zs01Name !=
						// name){
						if ((reqType == 'U' || reqType == 'C') && type != 'ZS01' && zs01Name != name) {
							count++;
						}
					}
					if (count > 0) {
						return new ValidationResult(null, false, 'All Additional address customer name should be same as Mailing address.');
					} else {
						return new ValidationResult(null, true);
					}
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

// CREATCMR-6358
function addCompanyProofForSG() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = FormManager.getActualValue('reqType');
				var hasAdditionalAddr = false;
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
					return new ValidationResult(null, true);
				}
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						if (CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i).addrType != 'ZS01') {
							hasAdditionalAddr = true;
							break;
						}
					}
				}
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var name = null;
					var count = 0;
					var updateInd = null;

					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						if (record == null && _allAddressData != null && _allAddressData[i] != null) {
							record = _allAddressData[i];
						}
						name = record.custNm1;
						updateInd = record.updateInd;

						if (typeof (type) == 'object') {
							updateInd = updateInd[0];
						}

						if ((updateInd == 'U' || updateInd == 'N')) {
							count++;
						}

					}
					if ((count > 0 || hasAdditionalAddr) && checkForCompanyProofAttachment()) {
						return new ValidationResult(null, false, 'Company proof is mandatory since address has been updated or added.');
					} else {
						return new ValidationResult(null, true);
					}
				}
			}
		};
	})(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

// CREATCMR-6825
function setRepTeamMemberNo() {
	console.log('>>>> setRepTeamMemberNo >>>>');
	var reqType = FormManager.getActualValue('reqType');
	if (reqType == 'C') {
		FormManager.setValue('repTeamMemberNo', '000000');
		FormManager.readOnly('repTeamMemberNo');
		FormManager.readOnly('repTeamMemberName');
	}
}

// CREATCMR-6358
function additionalAddrNmValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var currentCustNm1 = FormManager.getActualValue('custNm1');
				var currentCustNm2 = FormManager.getActualValue('custNm2');
				var currentCustNm = currentCustNm1 + currentCustNm2;

				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var type = null;
					var name = null;
					var name1 = '';
					var name2 = '';
					var count = 0;
					var reqId = FormManager.getActualValue('reqId');
					qParams_z = {
						REQ_ID: reqId,
					};
					var record = cmr.query('GETZS01VALRECORDS', qParams_z);
					var zs01Reccount = record.ret1;
					if (zs01Reccount == 0) {
						return new ValidationResult(null, true);
					}
					var qParams = {
						REQ_ID: reqId,
						ADDR_TYPE: "ZS01",
					};
					var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
					var zs01Name = result != undefined ? result.ret1.concat(result.ret2) : '';

					if (!FormManager.getField('addrType_ZS01').checked && currentCustNm1 != '' && currentCustNm != zs01Name) {
						return new ValidationResult({
							id: 'custNm1',
							type: 'text',
							name: 'custNm1'
						}, false, 'Additional address customer name should be same as Mailing address.');
					}
					return new ValidationResult(null, true);
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');
}
var _customerTypeHandler = null;
function addCustGrpHandler() {
	if (_customerTypeHandler == null) {
		_customerTypeHandler = dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
			var custGrp = FormManager.getActualValue('custGrp');
			var reqType = FormManager.getActualValue('reqType');
			if (reqType == 'C' && custGrp == 'CROSS') {
				FormManager.setValue('custSubGrp', 'CROSS');
				window.loadDefaultCrossSettings = true;
			}
		});
	}
}

// CREATCMR-788
function addressQuotationValidatorAP() {

	var cntry = FormManager.getActualValue('cmrIssuingCntry')
	var AP01 = [SysLoc.BRUNEI, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.MALASIA, SysLoc.PHILIPPINES, SysLoc.BANGLADESH, SysLoc.SINGAPORE, SysLoc.VIETNAM, SysLoc.THAILAND];
	if (AP01.indexOf(cntry) > -1) {
		FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name']);
		FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Con\'t']);
		FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Address.(Flr,Bldg,lvl,Unit.)']);
		FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Address.Cont1(Street Name, Street No.)']);
		FormManager.addValidator('dept', Validators.NO_QUOTATION, ['Address.Cont2(District,Town,Region)']);
		FormManager.addValidator('postCd', Validators.NO_QUOTATION, ['Postal Code']);
		if (cntry == SysLoc.INDONESIA || cntry == SysLoc.MALASIA || cntry == SysLoc.PHILIPPINES) {
			FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City/State/Province']);
		} else if (cntry == SysLoc.THAILAND) {
			FormManager.addValidator('city1', Validators.NO_QUOTATION, ['Street Address Cont\'2']);
		} else {
			FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City']);
		}
	} else if (cntry == SysLoc.AUSTRALIA || cntry == SysLoc.NEW_ZEALAND) {
		FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name']);
		FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Con\'t']);
		FormManager.addValidator('dept', Validators.NO_QUOTATION, ['Attn']);
		FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Street Address']);
		FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Street Address Con\'t']);
		FormManager.addValidator('city1', Validators.NO_QUOTATION, ['Suburb']);
		FormManager.addValidator('postCd', Validators.NO_QUOTATION, ['Postal Code']);
	}
	FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, ['Abbreviated Name (TELX1)'], 'MAIN_CUST_TAB');
	FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, ['Abbreviated Location'], 'MAIN_CUST_TAB');
}
function setLockIsicNZfromDNB() {
	console.log('>>>> setLockIsicNZfromDNB >>>>');
	var isDnbRecord = FormManager.getActualValue('findDnbResult');
	if (isDnbRecord == 'Accepted' && FormManager.getActualValue('isicCd') != '') {
		FormManager.readOnly('isicCd');
	} else {
		FormManager.enable('isicCd');
	}

}


var _customerTypeHandler = null;
function addCustGrpHandler() {
	console.log('>>>> addCustGrpHandler >>>>');
	if (_customerTypeHandler == null) {
		_customerTypeHandler = dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
			var cntry = FormManager.getActualValue('cmrIssuingCntry');
			var custGrp = FormManager.getActualValue('custGrp');
			var reqType = FormManager.getActualValue('reqType');
			var apaCntry = ['834', '818', '856', '778', '749', '643', '852', '744', '615', '652', '616', '796', '641', '738', '736', '858', '766'];
			if (reqType == 'C' && custGrp == 'CROSS' && apaCntry.includes(cntry)) {
				FormManager.setValue('custSubGrp', 'CROSS');
			}
		});
	}
}


// CREATCMR-7883
function checkCustomerNameForKYND() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var errorMsg = '';
				var action = FormManager.getActualValue('yourAction');
				var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();

				var reqType = FormManager.getActualValue('reqType');
				var role = FormManager.getActualValue('userRole').toUpperCase();
				var custSubGrp = FormManager.getActualValue('custSubGrp');

				if (reqType == 'C' && role == 'REQUESTER' && custSubGrp == 'KYNDR' && (action == 'SFP' || action == 'VAL')) {
					// CREATCMR-7884
					if (custNm1.indexOf('KYNDRYL') == -1) {
						errorMsg = 'Customer name must contain word \'Kyndryl\'';
					}
				}

				if (errorMsg != '') {
					return new ValidationResult(null, false, errorMsg);
				}

				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}


function validateCustnameForKynd() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var errorMsg = '';
				var action = FormManager.getActualValue('yourAction');
				var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();
				var reqType = FormManager.getActualValue('reqType');
				var role = FormManager.getActualValue('userRole').toUpperCase();
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				if (reqType == 'C' && custSubGrp == 'KYND') {
					if (custNm1.indexOf('KYNDRYL') == -1) {
						errorMsg = 'Customer name must contain word \'Kyndryl\'';
					}
				}
				if (errorMsg != '') {
					return new ValidationResult(null, false, errorMsg);
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setDefaultOnScenarioChangeTH(fromAddress, scenario, scenarioChanged) {
	console.log('>>>> setDefaultOnScenarioChangeTH >>>>');
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}

	if (scenarioChanged && scenario == 'CROSS') {
		FormManager.setValue('apCustClusterId', '00000');
	}

	if (scenarioChanged && scenario == 'DUMMY') {
		var isInacTypeReadOnlyFromScenarios = TemplateService.isFieldReadOnly('inacType');
		if (isInacTypeReadOnlyFromScenarios) {
			FormManager.readOnly('inacType');
		} else {
			FormManager.enable('inacType');
		}
	}
}


function checkCmrUpdateBeforeImport() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {

				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				var cmrNo = FormManager.getActualValue('cmrNo');
				var reqId = FormManager.getActualValue('reqId');
				var reqType = FormManager.getActualValue('reqType');
				var uptsrdc = '';
				var lastupts = '';

				if (reqType == 'C') {
					// console.log('reqType = ' + reqType);
					return new ValidationResult(null, true);
				}

				var resultsCC = cmr.query('GETUPTSRDC', {
					COUNTRY: cntry,
					CMRNO: cmrNo,
					MANDT: cmr.MANDT
				});

				if (resultsCC != null && resultsCC != undefined && resultsCC.ret1 != '') {
					uptsrdc = resultsCC.ret1;
					// console.log('lastupdatets in RDC = ' + uptsrdc);
				}

				var results11 = cmr.query('GETUPTSADDR', {
					REQ_ID: reqId
				});
				if (results11 != null && results11 != undefined && results11.ret1 != '') {
					lastupts = results11.ret1;
					// console.log('lastupdatets in CreateCMR = ' + lastupts);
				}

				if (lastupts != '' && uptsrdc != '') {
					if (uptsrdc > lastupts) {
						return new ValidationResult(null, false, 'This CMR has a new update , please re-import this CMR.');
					} else {
						return new ValidationResult(null, true);
					}
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_GENERAL_TAB', 'frmCMR');
}


// Coverage 2024 for THAILAND -> CREATCMR - 10535
function coverage2024ForTH() {
	console.log("---- coverage2024ForTH ----");
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	if (custSubGrp == 'PRIV') {
		FormManager.readOnly('isicCd');
		FormManager.setValue('isicCd', '9500');
	}
	var _clusterHandlerSG = null;
	FormManager.resetDropdownValues(FormManager.getField('clientTier'));
	if (_clusterHandlerSG == null && FormManager.getActualValue('reqType') != 'U') {
		_clusterHandlerSG = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {
			if (FormManager.getActualValue('apCustClusterId') != '') {
				window.localStorage.setItem('cluster', FormManager.getActualValue('apCustClusterId'));
				window.localStorage.setItem('custSubGrp', FormManager.getActualValue('custSubGrp'));
				setISUCTCByCluster();
				setInacByClusterTH();
			}
		});
	}

	if (window.localStorage.getItem('cluster') == FormManager.getActualValue('apCustClusterId') && FormManager.getActualValue('apCustClusterId') == '00000') {
		setISUCTCByCluster();
		setInacByClusterSG();
	}
}

function setISUCTCByCluster() {
	var qParams = {
		_qall: 'Y',
		ISSUING_CNTRY: FormManager.getActualValue('cmrIssuingCntry'),
		CLUSTER: FormManager.getActualValue('apCustClusterId'),
	};

	var results = cmr.query('GET.CTC_ISU_MRC_BY_CLUSTER_CNTRY', qParams);
	if (results != null && results.length == 1) {
		if (results[0].ret1 != null && results[0].ret1 != '') {
			FormManager.setValue('clientTier', results[0].ret1);
			FormManager.readOnly('clientTier');
		}
		if (results[0].ret2 != null && results[0].ret2 != '') {
			FormManager.resetDropdownValues(FormManager.getField('isuCd'));
			FormManager.setValue('isuCd', results[0].ret2);
			FormManager.readOnly('isuCd');
		}
		if (results[0].ret3 != null && results[0].ret3 != '') {
			FormManager.setValue('mrcCd', results[0].ret3);
		}
	} else if (results != null && results.length > 1) {
		var qParams1 = {
			ISSUING_CNTRY: FormManager.getActualValue('cmrIssuingCntry'),
			CLUSTER: FormManager.getActualValue('apCustClusterId'),
			SCENARIO: '%' + FormManager.getActualValue('custSubGrp') + '%'
		};

		var result = cmr.query('GET.CTC_ISU_MRC_BY_CLUSTER_CNTRY_SCENARIO', qParams1);
		if (result.ret1 != null && result.ret1 != '') {
			FormManager.setValue('clientTier', result.ret1);
			FormManager.readOnly('clientTier');
		}
		if (result.ret2 != null && result.ret2 != '') {
			FormManager.resetDropdownValues(FormManager.getField('isuCd'));
			FormManager.setValue('isuCd', result.ret2);
			FormManager.readOnly('isuCd');
		}
		if(result.ret2 == null || result.ret2 == ''){
			 FormManager.readOnly('isuCd');
			 setIsuOnIsic();
		}
		if (result.ret3 != null && result.ret3 != '') {
			FormManager.setValue('mrcCd', result.ret3);
		}
	}
}

function setInacByClusterTH() {
	var qParams = {
		_qall: 'Y',
		ISSUING_CNTRY: FormManager.getActualValue('cmrIssuingCntry'),
		CMT: '%' + FormManager.getActualValue('apCustClusterId') + '%',
	};
	var inacList = [];
	var results = cmr.query('GET.INAC_BY_CLUSTER', qParams);
	FormManager.resetDropdownValues(FormManager.getField('inacCd'));
	if (results != null && results.length > 0) {
		for (i = 0; i < results.length; i++) {
			inacList.push(results[i].ret1);
		}
		FormManager.enable('inacCd');
		FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacList);
		FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
	  FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
	} else {
		FormManager.clearValue('inacCd');
		FormManager.enable('inacCd');
		FormManager.clearValue('inacType');
		FormManager.enable('inacType');
		FormManager.resetDropdownValues(FormManager.getField('clientTier'));
		FormManager.removeValidator('inacCd', Validators.REQUIRED);
		FormManager.removeValidator('inacType', Validators.REQUIRED);
	}
	if (inacList.length == 1) {
		FormManager.setValue('inacCd', inacList[0]);
		FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
		FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
	}
}

dojo.addOnLoad(function() {
	console.log('adding THAILAND functions...');
	console.log('the value of person full id is ' + localStorage.getItem("pID"));
	GEOHandler.setRevertIsicBehavior(false);
	GEOHandler.registerValidator(addSalesRepNameNoCntryValidator, [SysLoc.THAILAND]);
	// 1558942: Update Address Details of different address types at one time
	GEOHandler.enableCopyAddress([SysLoc.THAILAND]);

	GEOHandler.addAfterConfig(addAfterConfigAP, [SysLoc.THAILAND]);
	GEOHandler.addAfterTemplateLoad(addAfterConfigAP, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(updateIndustryClass, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(updateProvCd, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(updateRegionCd, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(setCollectionCd, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(onSubIndustryChange, [SysLoc.THAILAND]);
	GEOHandler.enableCustomerNamesOnAddress([SysLoc.THAILAND]);
	GEOHandler.addAddrFunction(updateMainCustomerNames, [SysLoc.THAILAND]);
	GEOHandler.addAddrFunction(handleObseleteExpiredDataForUpdate, [SysLoc.THAILAND]);
	GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, [SysLoc.THAILAND]);

	GEOHandler.addAfterConfig(onCustSubGrpChange, [SysLoc.THAILAND]);

	// ERO specific
	GEOHandler.registerValidator(addFailedDPLValidator, [SysLoc.THAILAND]);
	GEOHandler.registerValidator(addDPLCheckValidator, [SysLoc.THAILAND], GEOHandler.ROLE_REQUESTER, true);

	// 2333
	GEOHandler.addAfterConfig(setISBUforBPscenario, [SysLoc.THAILAND]);
	GEOHandler.addAfterTemplateLoad(setISBUforBPscenario, [SysLoc.THAILAND]);
	// Address validations
	GEOHandler.registerValidator(addSoltToAddressValidator, [SysLoc.THAILAND]);
	GEOHandler.registerValidator(addAddressInstancesValidator, [SysLoc.THAILAND], null, true);
	GEOHandler.registerValidator(addContactInfoValidator, [SysLoc.THAILAND], GEOHandler.REQUESTER, true);
	GEOHandler.registerValidator(similarAddrCheckValidator, [SysLoc.THAILAND], null, true);
	GEOHandler.registerValidator(addFormatFieldValidator, [SysLoc.THAILAND], null, true);
	GEOHandler.registerValidator(addFieldFormatValidator, [SysLoc.THAILAND], null, true);

	GEOHandler.addAddrFunction(lockCustMainNames, [SysLoc.THAILAND]);
	GEOHandler.registerValidator(validateStreetAddrCont2, [SysLoc.THAILAND], null, true);

	GEOHandler.addAfterTemplateLoad(addHandlersForAP, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(addHandlersForAP, [SysLoc.THAILAND]);
	GEOHandler.registerValidator(addValidatorBasedOnCluster, [SysLoc.THAILAND], GEOHandler.ROLE_REQUESTER, true);
	GEOHandler.registerValidator(addCtcObsoleteValidator, [SysLoc.THAILAND], null, true);
	GEOHandler.addAfterTemplateLoad(handleObseleteExpiredDataForUpdate, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(handleObseleteExpiredDataForUpdate, [SysLoc.THAILAND]);
	// India Handler
	// CREATCMR-6825
	GEOHandler.addAfterConfig(setRepTeamMemberNo, [SysLoc.THAILAND]);
	GEOHandler.addAfterTemplateLoad(setRepTeamMemberNo, [SysLoc.THAILAND]);
	GEOHandler.addAfterConfig(addCustGrpHandler, [SysLoc.THAILAND]);

	// CREATCMR-8581
	GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [SysLoc.THAILAND], null, true);

	GEOHandler.registerValidator(validateCustnameForKynd, [SysLoc.THAILAND], null, true);
	GEOHandler.addAfterTemplateLoad(setDefaultOnScenarioChangeTH, [SysLoc.THAILAND]);

	// CREATCMR - 10575 -> Coverage 2024 for Thailand
	GEOHandler.addAfterConfig(coverage2024ForTH, [SysLoc.THAILAND]);
	GEOHandler.addAfterTemplateLoad(coverage2024ForTH, [SysLoc.THAILAND]);


});