/* Register MCO Javascripts */
var fstCEWA = ["373", "382", "383", "635", "637", "656", "662", "667", "670", "691", "692", "700", "717", "718", "753", "810", "840", "841", "876", "879", "880", "881"];
var othCEWA = ["610", "636", "645", "669", "698", "725", "745", "764", "769", "770", "782", "804", "825", "827", "831", "833", "835", "842", "851", "857", "883"];
var validGmllcCntry = ['MU', 'ML', 'GQ', 'SN', 'CI', 'GA', 'CD', 'CG', 'DJ', 'GN', 'CM', 'MG', 'MR', 'TG', 'GM', 'CF', 'BJ', 'BF', 'SC', 'GW', 'NE', 'TD', 'AO', 'BW', 'BI', 'CV', 'ET', 'GH', 'ER',
	'MW', 'LR', 'MZ', 'NG', 'ZW', 'ST', 'RW', 'SL', 'SO', 'SS', 'TZ', 'UG', 'ZM', 'NA', 'LS', 'SZ'];

var landCntrySA = ['SZ', 'ZA', 'LS', 'NA'];
var landCntryCEWA = ['AO', 'CV', 'MZ', 'ST', 'BW', 'MW', 'ZM', 'CG', 'CD', 'CF', 'CM', 'GA', 'GQ', 'NE', 'TD', 'ET', 'GH', 'LR', 'SL', 'KE',
	'LY', 'MG', 'MU', 'SC', 'NG', 'GM', 'GN', 'GW', 'ML', 'MR', 'SN', 'BF', 'BJ', 'TG', 'CI', 'TN', 'BI', 'DJ', 'ER', 'RW',
	'SO', 'SD', 'TZ', 'UG', 'ZW', 'DZ'];
var landCntryMEA = ['AE', 'SA', 'KW', 'OM', 'IQ', 'SY', 'YE', 'JO', 'LB', 'PS', 'BH', 'EG', 'QA', 'LY', 'TN', 'MA', 'PK', 'AF'];
function addMCO1LandedCountryHandler(cntry, addressMode, saving, finalSave) {
	if (!saving) {
		if (addressMode == 'newAddress') {
			FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
			FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
		} else {
			FilteringDropdown['val_landCntry'] = null;
		}
	}

	var landCntry = FormManager.getActualValue('landCntry');
	var addrType = FormManager.getActualValue('addrType');
	if (finalSave == true && (cmr.addressMode == 'updateAddress' && cmr.addrdetails.ret2 == 'ZS01' && cmr.oldlandcntry && cmr.oldlandcntry != landCntry) || (cmr.addressMode == 'newAddress' && addrType == 'ZS01' && landCntry)) {
		setISUCTC();
		getMEAPreSelectedCBLogicEntp();
	}
}

/**
 * After config handlers
 */
var _ISUHandler = null;
var _CTCHandler = null;
var _SalesRepHandler = null;
var _reqLobHandler = null;
var _reqReasonHandler = null;
var _codHandler = null;
var _cofHandler = null;
var _vatExemptHandler = null;
var _streetHandler = null;
var _tinExemptHandler = null;
var _numeroExemptHandler = null;

function addHandlersForMCO2() {

	if (_ISUHandler == null) {
		_ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
			setClientTierFieldMandt(value);
			setSalesRepValues(value);
		});
	}

	if (_CTCHandler == null) {
		_CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
			setSalesRepValues(value);
		});
	}

	if (FormManager.getActualValue('reqType') == 'U') {
		if (_reqLobHandler == null) {
			_reqLobHandler = dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
				setCodFieldBehavior();
				setCofFieldBehavior();
			});
		}

		if (_reqReasonHandler == null) {
			_reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
				setCodFieldBehavior();
				setCofFieldBehavior();
			});
		}

		if (_codHandler == null) {
			_codHandler = dojo.connect(FormManager.getField('codFlag'), 'onChange', function(value) {
				setCofValueByCod();
			});
		}

		if (_cofHandler == null) {
			_cofHandler = dojo.connect(FormManager.getField('commercialFinanced'), 'onChange', function(value) {
				setCodValueByCof();
			});
		}
	}

	if (_streetHandler == null) {
		_streetHandler = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
			setStreetContBehavior();
		});
	}

	if (FormManager.getActualValue('reqType') == 'C') {
		if (_vatExemptHandler == null) {
			_vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
				resetVatRequired();
			});
		}

		if (_tinExemptHandler == null) {
			_tinExemptHandler = dojo.connect(FormManager.getField('taxCd2'), 'onClick', function(value) {
				resetTinRequired();
			});
		}

		if (_numeroExemptHandler == null) {
			_numeroExemptHandler = dojo.connect(FormManager.getField('taxCd2'), 'onClick', function(value) {
				resetNumeroRequired();
			});
		}
	}
}

var ctcHandler = null;
var custSubGrpHandler = null;
var subIndHandler = null;
var scenarioChanged = false;
var isicChanged = false;
var previousSelection = null;
var currentSelection = null;
var previousISIC = null;
var currentISIC = null;
var isPrvsSlctnBlank = true;
var isPrvsISICBlank = true;
var _landCntryHandler = null;

function addNewHandlersForMCO2() {
	localStorage.setItem("validateLogicCalled", false);
	if (ctcHandler == null) {
		ctcHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
			setEntpValue();
			getMEAPreSelectedCBLogicEntp();
		});
	}


	// first change to custSubGrp
	if (FormManager.getActualValue('custSubGrp') && _pagemodel['custSubGrp'] == null && (localStorage.getItem("oldCustGrp") == '' || localStorage.getItem("oldCustGrp") == null)) {
		setISUCTC();
		setEntpValue();
		getMEAPreSelectedCBLogicEntp();
		localStorage.setItem("oldCustGrp", FormManager.getActualValue('custSubGrp'));
	}


	if (custSubGrpHandler == null) {
		custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
			currentSelection = FormManager.getActualValue('custSubGrp');
			previousSelection = localStorage.getItem("oldCustGrp");
			if (previousSelection != null && previousSelection != '' && previousSelection != undefined) {
				isPrvsSlctnBlank = false;
			}
			if ((!isPrvsSlctnBlank && previousSelection != currentSelection && _pagemodel['custSubGrp'] == null) || (!isPrvsSlctnBlank && previousSelection == _pagemodel['custSubGrp'] && previousSelection != currentSelection)) {
				scenarioChanged = true;
			} else if ((!isPrvsSlctnBlank && (previousSelection == currentSelection && currentSelection == _pagemodel['custSubGrp']))) {
				scenarioChanged = false;
			}
			if (scenarioChanged) {
				setISUCTC();
				setEntpValue();
				getMEAPreSelectedCBLogicEntp();
			}
			localStorage.setItem("oldCustGrp", FormManager.getActualValue('custSubGrp'));
		});
	}

	if (subIndHandler == null) {
		subIndHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
			if (FormManager.getActualValue('subIndustryCd'))
				currentISIC = FormManager.getActualValue('subIndustryCd').substr(0, 2);

			if (localStorage.getItem("oldISIC"))
				previousISIC = localStorage.getItem("oldISIC").substr(0, 2);
			if (previousISIC != null && previousISIC != '' && previousISIC != undefined) {
				isPrvsISICBlank = false;
			}

			if ((!isPrvsISICBlank && previousISIC != currentISIC && _pagemodel['subIndustryCd'] == null) || (!isPrvsISICBlank && previousISIC == _pagemodel['subIndustryCd'] && previousISIC != currentISIC)) {
				isicChanged = true;
			} else if ((!isPrvsISICBlank && (previousISIC == currentISIC && currentISIC == _pagemodel['subIndustryCd'])) || (previousISIC == null && currentISIC == _pagemodel['subIndustryCd'])) {
				isicChanged = false;
			}
			if (isicChanged) {
				setISUCTC();
				getMEAPreSelectedCBLogicEntp();
			}
			localStorage.setItem("oldISIC", FormManager.getActualValue('subIndustryCd'));
		});
	}
}

function setISUCTC() {
	var zs01Landed = FormManager.getActualValue('landCntry');
	if (zs01Landed == '' || zs01Landed == undefined || zs01Landed == null) {
		zs01Landed = getZS01LandCntry();
	}
	var subInd = FormManager.getActualValue('subIndustryCd') ? FormManager.getActualValue('subIndustryCd') : localStorage.oldISIC;
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	if ((['XCOM', 'XLLCX', 'XTP', 'XGOV', 'XPRIC'].includes(custSubGrp) && !isMEACntry(zs01Landed)) || ['COMME', 'LLCEX', 'GOVRN', 'PRICU', 'THDPT'].includes(custSubGrp)) {
		FormManager.setValue('isuCd', '34');
		FormManager.setValue('clientTier', 'Q');
		return;
	}

	if (['XBP', 'BUSPR', 'LLCBP'].includes(custSubGrp)) {
		FormManager.setValue('isuCd', '8B');
		FormManager.setValue('clientTier', '');
		return;
	}

	if (['XIBME', 'XINTE', 'INTER', 'IBMEM'].includes(custSubGrp)) {
		FormManager.setValue('isuCd', '21');
		FormManager.setValue('clientTier', '');
		return;
	}



	if (zs01Landed == 'TR' || landCntrySA.includes(zs01Landed)) {
		FormManager.setValue('isuCd', '27');
		FormManager.setValue('clientTier', 'E');
	} else if (landCntryCEWA.includes(zs01Landed)) {
		FormManager.setValue('isuCd', '34');
		FormManager.setValue('clientTier', 'Q');
	} else if (landCntryMEA.includes(zs01Landed)) {
		if (['EG', 'QA', 'LY', 'TN', 'MA', 'PK', 'AF'].includes(zs01Landed)) {
			FormManager.setValue('isuCd', '34');
			FormManager.setValue('clientTier', 'Q');
		} else if (['AE', 'KW', 'OM', 'SY', 'IQ', 'YE', 'JO', 'PS', 'LB', 'BH'].includes(zs01Landed)) {
			FormManager.setValue('isuCd', '27');
			FormManager.setValue('clientTier', 'E');
		} else if (zs01Landed == 'SA' && subInd) {
			subInd = subInd.substr(0, 1);
			if (['E', 'G', 'V', 'Y', 'H', 'X'].includes(subInd)) {
				FormManager.setValue('isuCd', '34');
				FormManager.setValue('clientTier', 'Q');
			} else {
				FormManager.setValue('isuCd', '27');
				FormManager.setValue('clientTier', 'E');
			}
		}
	}
}


function setStreetContBehavior() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}

	var street = FormManager.getActualValue('addrTxt');
	if (street != null && street != '') {
		FormManager.enable('addrTxt2');
	} else {
		FormManager.clearValue('addrTxt2');
		FormManager.disable('addrTxt2');
	}
}

function setCodFieldBehavior() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}

	if (FormManager.getActualValue('requestingLob') == 'AR' && FormManager.getActualValue('reqReason') == 'COD') {
		FormManager.enable('codFlag');
	} else {
		FormManager.readOnly('codFlag');
	}
}

function setCofFieldBehavior() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}

	if (FormManager.getActualValue('requestingLob') == 'IGF' && FormManager.getActualValue('reqReason') == 'COPT') {
		FormManager.enable('commercialFinanced');
	} else {
		FormManager.readOnly('commercialFinanced');
	}
}

function setIbmDeptCostCenterBehavior() {
	var cmrNo = FormManager.getActualValue('cmrNo');
	var reqType = FormManager.getActualValue('reqType');

	if (cmrNo != '' && reqType == 'U' && (cmrNo.substring(0, 2) != '99')) {
		FormManager.readOnly('ibmDeptCostCenter');
	}
}

function setCodValueByCof() {
	var cof = FormManager.getActualValue('commercialFinanced');
	if (cof == 'R' || cof == 'S' || cof == 'T') {
		FormManager.setValue('codFlag', 'N');
	}
}

function setCofValueByCod() {
	var cod = FormManager.getActualValue('codFlag');
	if (cod == 'Y') {
		FormManager.setValue('commercialFinanced', '');
	}
}

var _addrTypesForCEWA = ['ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02'];
var _addrTypeHandler = [];
function addHandlersForCEWA() {
	for (var i = 0; i < _addrTypesForCEWA.length; i++) {
		_addrTypeHandler[i] = null;
		if (_addrTypeHandler[i] == null) {
			_addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForCEWA[i]), 'onClick', function(value) {
				disableAddrFieldsCEWA();
			});
		}
	}

}

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	var reqType = FormManager.getActualValue('reqType');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	if (role == 'REQUESTER') {
		if (reqType != 'U') {
			FormManager.readOnly('embargoCd');
		}
	} else {
		FormManager.enable('embargoCd');
	}
}

/*
 * Lock CMROwner and Preferred Language
 */
function lockCmrOwnerPrefLang() {
	var reqType = FormManager.getActualValue('reqType');
	var isCmrImported = getImportedIndc();

	if (reqType == 'C' && isCmrImported == 'Y') {
		FormManager.readOnly('cmrOwner');
		FormManager.readOnly('custPrefLang');
	}

	if (reqType == 'U') {
		FormManager.readOnly('custPrefLang');
	}
}
function afterConfigForMCO2() {
	FormManager.setValue('capInd', true);
	FormManager.readOnly('capInd');
	FormManager.addValidator('ibmDeptCostCenter', Validators.DIGIT, ['Internal Department Number'], 'MAIN_IBM_TAB');
	FormManager.addValidator('specialTaxCd', Validators.ALPHANUM, ['Tax Code'], 'MAIN_CUST_TAB');
	if (FormManager.getActualValue('reqType') == 'U') {
		FormManager.addValidator('collectionCd', Validators.ALPHANUM, ['Collection Code'], 'MAIN_IBM_TAB');

		var role = FormManager.getActualValue('userRole');
		var numero = FormManager.getActualValue('busnType');
		var tin = FormManager.getActualValue('taxCd1');
		if (role == GEOHandler.ROLE_REQUESTER) {
			if (numero != null && numero != '') {
				FormManager.readOnly('busnType');
			}
			if (tin != null && tin != '') {
				FormManager.readOnly('taxCd1');
			}
		}
	}
	// CREATCMR-788
	addressQuotationValidatorMCO2();
}

/**
 * sets fields to lock/mandatory, not scenario handled
 */
function lockRequireFieldsMCO2() {
	console.log('lockRequireFieldsMCO2 LD.....');
	var reqType = FormManager.getActualValue('reqType');
	var role = FormManager.getActualValue('userRole');

	// fields locked for Requester
	if (role == GEOHandler.ROLE_REQUESTER) {
		if (reqType != 'U') {
			FormManager.readOnly('specialTaxCd');
		}

		if (reqType == 'U') {
			FormManager.readOnly('salesBusOffCd');
		}

		// FormManager.readOnly('salesBusOffCd');
		// FormManager.readOnly('repTeamMemberNo');
		// FormManager.readOnly('isuCd');
		// FormManager.readOnly('clientTier');
	}

	if (reqType == 'U') {
		setCodFieldBehavior();
		setCofFieldBehavior();
	}
	FormManager.removeValidator('enterprise', Validators.DIGIT);
}

function disableAddrFieldsCEWA() {
	var custType = FormManager.getActualValue('custGrp');
	var addrType = FormManager.getActualValue('addrType');

	if (custType == 'LOCAL' && addrType == 'ZS01') {
		FormManager.readOnly('landCntry');
	} else {
		FormManager.enable('landCntry');
	}

	// Phone - for shipping and Sold-to (FST = Installing and Non-FST = Mailing )
	if (addrType == 'ZD01' || addrType == 'ZS01') {
		FormManager.enable('custPhone');
	} else {
		FormManager.setValue('custPhone', '');
		FormManager.disable('custPhone');
	}

	// PO Box allowed -> FST = Mail-to (ZS02), Bill-to (ZP01), Sold-to (ZS01)
	// Non FST = Mailing (ZS01), Billing (ZP01)
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var addrPOBoxEnabled = ['ZS01', 'ZP01']

	if (fstCEWA.includes(cntry)) {
		addrPOBoxEnabled.push('ZS02');
	}

	if (addrPOBoxEnabled.includes(addrType)) {
		FormManager.enable('poBox');
	} else {
		FormManager.setValue('poBox', '');
		FormManager.disable('poBox');
	}
}

function addAddressTypeValidator() {
	console.log("addAddressTypeValidator for MCO2..........");
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
					return new ValidationResult(null, false, 'All address types are mandatory.');
				}
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var type = null;
					var zs01Cnt = 0;
					var zp01Cnt = 0;
					var zi01Cnt = 0;
					var zd01Cnt = 0;
					var zs02Cnt = 0;

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
							zs01Cnt++;
						} else if (type == 'ZP01') {
							zp01Cnt++;
						} else if (type == 'ZI01') {
							zi01Cnt++;
						} else if (type == 'ZD01') {
							zd01Cnt++;
						} else if (type == 'ZS02') {
							zs02Cnt++;
						}
					}

					var cntry = FormManager.getActualValue('cmrIssuingCntry');

					if (zs01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zd01Cnt == 0 || zs02Cnt == 0) {
						return new ValidationResult(null, false, 'All address types are mandatory.');
					} else if (fstCEWA.includes(cntry)) {
						return fstAddressValidator(zp01Cnt, zs01Cnt, zs02Cnt);
					} else if (othCEWA.includes(cntry)) {
						return nonFstAddressValidator(zs01Cnt, zp01Cnt, zs02Cnt);
					}
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function fstAddressValidator(zp01Cnt, zs01Cnt, zs02Cnt) {
	if (zp01Cnt > 1) {
		return new ValidationResult(null, false, 'Only one Bill-to address is allowed.');
	} else if (zs02Cnt > 1) {
		return new ValidationResult(null, false, 'Only one Mail-to address is allowed.');
	} else if (zs01Cnt > 1) {
		return new ValidationResult(null, false, 'Only one Sold-to address is allowed.');
	} else {
		return new ValidationResult(null, true);
	}
}

function nonFstAddressValidator(zs01Cnt, zp01Cnt, zs02Cnt) {
	if (zp01Cnt > 1) {
		return new ValidationResult(null, false, 'Only one Billing address is allowed.');
	} else if (zs01Cnt > 1) {
		return new ValidationResult(null, false, 'Only one Mailing address is allowed.');
	} else if (zs02Cnt > 1) {
		return new ValidationResult(null, false, 'Only one EPL address is allowed.');
	} else {
		return new ValidationResult(null, true);
	}
}

function addAddressFieldValidators() {
	// City + PostCd should not exceed 28 characters
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var city = FormManager.getActualValue('city1');
				var postCd = FormManager.getActualValue('postCd');
				var val = city;

				if (postCd != '') {
					val += postCd;
					if (val.length > 28) {
						return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 28 characters.');
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');

	// addrCont + poBox should not exceed 21 characters
	// ",<space>PO<space>BOX<space>" is included when counting to 30 max
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var stCont = FormManager.getActualValue('addrTxt2');
				var poBox = FormManager.getActualValue('poBox');

				if (poBox != '' && stCont != '') {
					var stContPoBox = stCont + poBox;
					if (stContPoBox != null && stContPoBox.length > 21) {
						return new ValidationResult(null, false, 'Total computed length of Street Con\'t and PO Box should not exceed 21 characters.');
					}
				} else if (poBox != '') {
					if (poBox.length > 23) {
						return new ValidationResult(null, false, 'PO Box length should not exceed 23 characters.');
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');

	// phone + ATT should not exceed 29 characters (for Shipping & EPL only)
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var addrType = FormManager.getActualValue('addrType');

				if (addrType == 'ZD01' || addrType == 'ZS02') {
					var att = FormManager.getActualValue('custNm4');
					var custPhone = FormManager.getActualValue('custPhone');
					var val = att;

					if (custPhone != '') {
						val += custPhone;
						if (val != null && val.length > 29) {
							return new ValidationResult(null, false, 'Total computed length of Attention Person and Phone should not exceed 29 characters.');
						}
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');

}

function changeAbbrevNmLocn(cntry, addressMode, saving, finalSave, force) {
	if (finalSave || force || addressMode == 'COPY') {
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
			// generate Abbreviated Name/Location
			var role = FormManager.getActualValue('userRole').toUpperCase();
			var abbrevNm = FormManager.getActualValue('custNm1');
			var abbrevLocn = FormManager.getActualValue('city1');
			if (FormManager.getActualValue('reqType') != 'C') {
				return;
			}
			if (role == 'REQUESTER') {
				if (abbrevNm && abbrevNm.length > 22) {
					abbrevNm = abbrevNm.substring(0, 22);
				}
				if (abbrevLocn && abbrevLocn.length > 12) {
					abbrevLocn = abbrevLocn.substring(0, 12);
				}

				FormManager.setValue('abbrevNm', abbrevNm);
				FormManager.setValue('abbrevLocn', abbrevLocn);
			}
		}
	}
}

function setAbbrvNmLoc() {
	var role = FormManager.getActualValue('userRole').toUpperCase();
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}
	if (role != 'REQUESTER') {
		return;
	}
	var reqId = FormManager.getActualValue('reqId');
	if (reqId != null) {
		reqParam = {
			REQ_ID: reqId,
		};
	}
	var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
	var city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
	var abbrvNm = custNm.ret1;
	var abbrevLocn = city.ret1;

	if (abbrvNm && abbrvNm.length > 22) {
		abbrvNm = abbrvNm.substring(0, 22);
	}
	if (abbrevLocn && abbrevLocn.length > 12) {
		abbrevLocn = abbrevLocn.substring(0, 12);
	}

	if (abbrevLocn != null) {
		FormManager.setValue('abbrevLocn', abbrevLocn);
	}
	if (abbrvNm != null) {
		FormManager.setValue('abbrevNm', abbrvNm);
	}
}

function validateMCOCopy(addrType, arrayOfTargetTypes) {
	return null;
}

function crossborderScenariosAbbrvLoc() {
	var role = FormManager.getActualValue('userRole').toUpperCase();
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}
	if (role != 'REQUESTER') {
		return;
	}
	var custGroup = FormManager.getActualValue('custGrp');
	if (custGroup == "CROSS") {
		var reqId = FormManager.getActualValue('reqId');
		if (reqId != null) {
			reqParam = {
				REQ_ID: reqId,
				ADDR_TYPE: "ZS01",
			};
		}
		var results = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', reqParam);
		var abbrevLocn = results.ret1;
		if (abbrevLocn != null && abbrevLocn.length > 12) {
			abbrevLocn = abbrevLocn.substring(0, 12);
		}

		if (abbrevLocn != null) {
			FormManager.setValue('abbrevLocn', abbrevLocn);
		} else {
			FormManager.setValue('abbrevLocn', '');
		}
	}
}

function scenariosAbbrvLocOnChange() {
	dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
		var custGroup = FormManager.getActualValue('custGrp').toUpperCase();
		if (custGroup == 'LOCAL') {
			setAbbrvNmLoc();
		} else if (custGroup == 'CROSS') {
			crossborderScenariosAbbrvLoc();
		}
	});
}

function addAddrValidatorMCO2() {
	FormManager.addValidator('custNm1', Validators.LATIN, ['Customer Name']);
	FormManager.addValidator('custNm2', Validators.LATIN, ['Customer Name Continuation']);
	FormManager.addValidator('custNm4', Validators.LATIN, ['Additional Name or Address Information']);
	FormManager.addValidator('addrTxt', Validators.LATIN, ['Street']);
	FormManager.addValidator('addrTxt2', Validators.LATIN, ['Street Continuation']);
	FormManager.addValidator('city1', Validators.LATIN, ['City']);
	//  FormManager.addValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);

	FormManager.addValidator('custPhone', Validators.DIGIT, ['Phone #']);
	FormManager.addValidator('poBox', Validators.DIGIT, ['PO BOX']);
}

function addAbbrvNmAndLocValidator() {
	var role = FormManager.getActualValue('userRole').toUpperCase();
	if (role == 'PROCESSOR') {
		FormManager.addValidator('abbrevNm', Validators.LATIN, ['Abbreviated Name']);
		FormManager.addValidator('abbrevLocn', Validators.LATIN, ['Abbreviated Location']);
	}
}

function addAttachmentValidator() {
	console.log("addAttachmentValidator..............");
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var role = FormManager.getActualValue('userRole').toUpperCase();
				var req = FormManager.getActualValue('reqType').toUpperCase();
				var subCustGrp = FormManager.getActualValue('custSubGrp');
				if (req == 'C') {
					switch (subCustGrp.toUpperCase()) {
						case 'BUSPR':
							return new ValidationResult(null, true);
							break;
						case 'INTER':
							return new ValidationResult(null, true);
							break;
						case 'XBP':
							return new ValidationResult(null, true);
							break;
						case 'XINTE':
							return new ValidationResult(null, true);
							break;
						case 'XPRIC':
							return new ValidationResult(null, true);
							break;
						case 'LLCBP':
							return new ValidationResult(null, true);
							break;
						default:
							var reqId = FormManager.getActualValue('reqId');
							if (reqId != null) {
								reqParam = {
									ID: reqId
								};
							}
							var results = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', reqParam);
							var recordCount = results.ret1;

							if (recordCount != null) {
								if (recordCount > 0) {
									return new ValidationResult(null, true);
								} else if (recordCount == 0) {
									return new ValidationResult(null, false, 'Proof of address is mandatory. Please attach Company Proof.');
								}
							} else {
								return new ValidationResult(null, false, 'Proof of address is mandatory. Please attach Company Proof.');
							}
							break;
					}
				} else if (req == 'U') {
					if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
						var record = null;
						var updateInd = null;
						var importInd = null;
						var counter = 0;
						for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
							record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
							type = record.addrType;
							updateInd = record.updateInd;
							importInd = record.importInd;

							if ((updateInd == 'U' && type == 'ZS01') || (updateInd == 'U' && type == 'ZP01')) {

								var reqId = FormManager.getActualValue('reqId');
								if (reqId != null) {
									reqParam = {
										ID: reqId
									};
								}
								var resultAttachment = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', reqParam);
								var attachmentCount = resultAttachment.ret1;

								if (reqId != null) {
									reqParam = {
										REQ_ID: reqId,
										ADDR_TYPE: type,
									};
								}

								var resultOldAddr = cmr.query('ADDR.GET.OLDADDR.BY_REQID_ADDRTYP', reqParam);

								var oldCustName; // CUST_NM1
								var oldCustNameCon; // CUST_NM2
								var oldAddlName; // CUST_NM4
								var oldStreet; // ADDR_TXT
								var oldStreetCont; // ADDR_TXT_2
								var oldCity; // CITY1
								var oldPostCd; // POST_CD
								var oldLandedCntry; // LAND_CNTRY
								var oldPoBox; // PO_BOX
								var oldPhone; // CUST_PHONE

								var currentCustName;
								var currentCustNameCon;
								var currentAddlName;
								var currentStreet;
								var currentStreetCont;
								var currentCity;
								var currentPostCd;
								var currentLandedCntry;
								var currentPoBox;
								var currentPhone;

								if (resultOldAddr != null && resultOldAddr != '') {
									// pulled from addr_rdc
									oldCustName = resultOldAddr.ret2 != null ? resultOldAddr.ret2 : '';
									oldCustNameCon = resultOldAddr.ret3 != null ? resultOldAddr.ret3 : '';
									oldAddlName = resultOldAddr.ret4 != null ? resultOldAddr.ret4 : '';
									oldStreet = resultOldAddr.ret5 != null ? resultOldAddr.ret5 : '';
									oldStreetCont = resultOldAddr.ret6 != null ? resultOldAddr.ret6 : '';
									oldCity = resultOldAddr.ret7 != null ? resultOldAddr.ret7 : '';
									oldPostCd = resultOldAddr.ret8 != null ? resultOldAddr.ret8 : '';
									oldLandedCntry = resultOldAddr.ret9 != null ? resultOldAddr.ret9 : '';
									oldPoBox = resultOldAddr.ret10 != null ? resultOldAddr.ret10 : '';
									oldPhone = resultOldAddr.ret11 != null ? resultOldAddr.ret11 : '';

									// current address value
									currentCustName = record.custNm1[0] != null ? record.custNm1[0] : '';
									currentCustNameCon = record.custNm2[0] != null ? record.custNm2[0] : '';
									currentAddlName = record.custNm4[0] != null ? record.custNm4[0] : '';
									currentStreet = record.addrTxt[0] != null ? record.addrTxt[0] : '';
									currentStreetCont = record.addrTxt2[0] != null ? record.addrTxt2[0] : '';
									currentCity = record.city1[0] != null ? record.city1[0] : '';
									currentPostCd = record.postCd[0] != null ? record.postCd[0] : '';
									currentLandedCntry = record.landCntry[0] != null ? record.landCntry[0] : '';
									currentPoBox = record.poBox[0] != null ? record.poBox[0] : '';
									currentPhone = record.custPhone[0] != null ? record.custPhone[0] : '';

									if (oldCustName == currentCustName && oldCustNameCon == currentCustNameCon && oldAddlName == currentAddlName && oldStreet == currentStreet && oldStreetCont == currentStreetCont
										&& oldCity == currentCity && oldPostCd == currentPostCd && oldLandedCntry == currentLandedCntry && oldPoBox == currentPoBox) {
										if (type == 'ZP01' && oldPhone != currentPhone) {
											if (attachmentCount > 0) {
												return new ValidationResult(null, true);
											}
											return new ValidationResult(null, false, 'Proof of address is mandatory. Please attach Company Proof.');
										}
										return new ValidationResult(null, true);
									} else {
										if (attachmentCount > 0) {
											return new ValidationResult(null, true);
										}
										return new ValidationResult(null, false, 'Proof of address is mandatory. Please attach Company Proof.');
									}
								}
							}
						}
					}
				}
			}
		};
	})(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function setAddressDetailsForView() {
	var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

	if (viewOnlyPage == 'true') {
		$('label[for="custNm1_view"]').text('Customer Name:');
		$('label[for="custNm2_view"]').text('Customer Name Continuation:');
		$('label[for="landCntry_view"]').text('Country (Landed):');
		$('label[for="custNm4_view"]').text('Additional Name or Address Information:');
		$('label[for="addrTxt_view"]').text('Street:');
		$('label[for="addrTxt2_view"]').text('Street Continuation:');
		$('label[for="custPhone_view"]').text('Phone #:');
	}
}

function lockAbbrv() {
	var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var reqType = FormManager.getActualValue('reqType');

	if (viewOnlyPage == 'true') {
		FormManager.readOnly('abbrevLocn');
		FormManager.readOnly('abbrevNm');
	} else {
		if (role == 'REQUESTER') {
			if (reqType != 'U') {
				FormManager.readOnly('abbrevLocn');
				FormManager.readOnly('abbrevNm');
			}
		}
		if (role == 'PROCESSOR') {
			FormManager.addValidator('abbrevNm', Validators.REQUIRED, ['Abbreviated Name'], 'MAIN_CUST_TAB');
		}
	}
}

function showDeptNoForInternalsOnly(fromAddress, scenario, scenarioChanged) {
	if (scenario == 'INTER' || scenario == 'XINTE') {
		FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, ['Internal Department Number'], 'MAIN_IBM_TAB');
		FormManager.show('InternalDept', 'ibmDeptCostCenter');
	} else {
		FormManager.removeValidator('ibmDeptCostCenter', Validators.REQUIRED);
		FormManager.hide('InternalDept', 'ibmDeptCostCenter');
	}
	if (scenarioChanged && scenario != null && scenario != '') {
		FormManager.clearValue('ibmDeptCostCenter');
	}
}

/* 1430539 - do not allow delete of imported addresses on update requests */

function canRemoveAddress(value, rowIndex, grid) {
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

function setSalesRepValues(isuCd, clientTier) {

	var reqType = FormManager.getActualValue('reqType');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var isuCd = FormManager.getActualValue('isuCd');
	var clientTier = FormManager.getActualValue('clientTier');

	if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
		return;
	}
	var salesReps = [];
	if (isuCd != '') {
		if (cntry == '764' || cntry == '831' || cntry == '851' || cntry == '857') {
			if ((isuCd == '32' && clientTier == 'T') || (isuCd == '34' && clientTier == 'Q') || (isuCd == '34' && clientTier == 'Y')) {
				FormManager.setValue('salesBusOffCd', '0080');
			} else {
				FormManager.setValue('salesBusOffCd', '0010');
			}
		} else if (cntry == '698' || cntry == '745') {
			if ((isuCd == '32' && clientTier == 'T') || (isuCd == '34' && clientTier == 'Q') || (isuCd == '34' && clientTier == 'Y')) {
				FormManager.setValue('salesBusOffCd', '0060');
			} else {
				FormManager.setValue('salesBusOffCd', '0010');
			}
		} else if (cntry == '645' || cntry == '835' || cntry == '842') {
			if ((isuCd == '34' && clientTier == 'Q') || (isuCd == '34' && clientTier == 'Y')) {
				FormManager.setValue('salesBusOffCd', '0040');
			} else {
				FormManager.setValue('salesBusOffCd', '0010');
			}
		}
		/*
		 * if ('780' != FormManager.getActualValue('cmrIssuingCntry')) { var
		 * custSubGrp = FormManager.getActualValue('custSubGrp'); if (custSubGrp !=
		 * "BUSPR" && custSubGrp != "XBP") { FormManager.setValue('repTeamMemberNo',
		 * '016757'); } else { FormManager.setValue('repTeamMemberNo', '780780'); } }
		 */
		if (isuCd == '5K' && clientTier == '') {
			FormManager.setValue('salesBusOffCd', '0010');
		}

	}
}

var _addrTypesForMCO2 = ['ZD01', 'ZI01', 'ZP01', 'ZS01', 'ZS02'];
var addrTypeHandler = [];
/*
 * function diplayTinNumberforTZ() {
 * 
 * var role = FormManager.getActualValue('role');
 * 
 * if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' &&
 * FormManager.getActualValue('cmrIssuingCntry') == '851') {
 * cmr.hideNode('tin'); //FormManager.clearValue('dept'); for (var i = 0; i <
 * _addrTypesForMCO2.length; i++) { if (addrTypeHandler[i] == null) {
 * addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' +
 * _addrTypesForMCO2[i]), 'onClick', function(value) { if
 * (FormManager.getField('addrType_ZP01').checked &&
 * FormManager.getActualValue('custGrp') == 'LOCAL') { cmr.showNode('tin');
 * if(role == 'REQUESTER'){ FormManager.addValidator('dept',
 * Validators.REQUIRED, [ 'TIN#' ], null); } } else { cmr.hideNode('tin');
 * //FormManager.clearValue('dept'); FormManager.removeValidator('dept',
 * Validators.REQUIRED); } }); } else { if
 * (FormManager.getField('addrType_ZP01').checked &&
 * FormManager.getActualValue('custGrp') == 'LOCAL') cmr.showNode('tin'); } } }
 * if (cmr.addressMode == 'updateAddress') { if
 * (FormManager.getActualValue('addrType') == 'ZP01') { cmr.showNode('tin');
 * if(role == 'REQUESTER'){ FormManager.addValidator('dept',
 * Validators.REQUIRED, [ 'TIN#' ], null); } } else { cmr.hideNode('tin'); //
 * FormManager.clearValue('dept'); FormManager.removeValidator('dept',
 * Validators.REQUIRED); } } }
 */

function diplayTinNumberforTZ() {

	var role = FormManager.getActualValue('userRole').toUpperCase();
	var scenario = FormManager.getActualValue('custGrp');

	// var role = FormManager.getActualValue('role');

	if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' && FormManager.getActualValue('cmrIssuingCntry') == '851') {
		cmr.hideNode('tin');
		// FormManager.clearValue('dept');
		for (var i = 0; i < _addrTypesForMCO2.length; i++) {
			if (addrTypeHandler[i] == null) {
				addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForMCO2[i]), 'onClick', function(value) {
					if (FormManager.getField('addrType_ZP01').checked) {
						if (cmr.currentRequestType == 'C' && scenario == 'LOCAL') {
							cmr.showNode('tin');
							if (role == 'REQUESTER') {
								FormManager.addValidator('dept', Validators.REQUIRED, ['TIN#'], '');
							} else {
								FormManager.resetValidations('dept');
							}
						} else if (cmr.currentRequestType == 'U') {
							cmr.showNode('tin');
							FormManager.resetValidations('dept');
						} else {
							cmr.hideNode('tin');
							FormManager.resetValidations('dept');
						}
					} else {
						cmr.hideNode('tin');
						// FormManager.clearValue('dept');
						FormManager.resetValidations('dept');
						// FormManager.removeValidator('dept', Validators.REQUIRED);
					}
					// setTinNumber();
				});
			} else {
				if (FormManager.getField('addrType_ZP01').checked && ((cmr.currentRequestType == 'C' && scenario == 'LOCAL') || cmr.currentRequestType == 'U'))
					cmr.showNode('tin');
			}
		}
	}
	if (cmr.addressMode == 'updateAddress') {
		if (FormManager.getActualValue('addrType') == 'ZP01') {
			cmr.showNode('tin');
			if (role == 'REQUESTER' && cmr.currentRequestType == 'C' && scenario == 'LOCAL') {
				FormManager.addValidator('dept', Validators.REQUIRED, ['TIN#'], '');
			} else {
				FormManager.resetValidations('dept');
			}
		} else {
			cmr.hideNode('tin');
			// FormManager.clearValue('dept');
			FormManager.resetValidations('dept');
			// FormManager.removeValidator('dept', Validators.REQUIRED);
		}
	}
}

/*
 * function setTinNumber(){ if (FormManager.getField('addrType_ZS01').checked ||
 * FormManager.getField('addrType_ZI01').checked ||
 * FormManager.getField('addrType_ZD01').checked ||
 * FormManager.getField('addrType_ZS02').checked){ //cmr.showNode('tin');
 * FormManager.setValue('dept', ' '); } }
 */

function addTinFormatValidationTanzania() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var dept = FormManager.getActualValue('dept');
				// var lbl1 = FormManager.getLabel('LocalTax1');
				if (FormManager.getField('addrType_ZP01').checked && dept && dept.length > 0 && !dept.match("([0-9]{3}-[0-9]{3}-[0-9]{3})|^(X{3})$")) {
					return new ValidationResult({
						id: 'dept',
						type: 'text',
						name: 'dept'
					}, false, 'Invalid format of TIN#. Format should be NNN-NNN-NNN or "XXX".');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');
}

// TZ defect 1730979 : Tin validator at billing

function addTinBillingValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var billingBool = true;
				var reqType = FormManager.getActualValue('reqType');
				var reqId = FormManager.getActualValue('reqId');
				var addr = 'ZP01';
				var qParams = {
					_qall: 'Y',
					REQID: reqId,
					ADDR_TYPE: addr
				};

				var results = cmr.query('GET_TIN_ADDRSEQ', qParams);
				if (results != null) {
					for (var i = 0; i < results.length; i++) {
						if (results[i].ret1.length < 1) {
							billingBool = false;
						}
					}
				}

				if (billingBool) {
					return new ValidationResult(null, true);
				} else if (!billingBool && reqType == 'C') {
					return new ValidationResult(null, false, 'Tin should not be empty in Billing Address.');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}


function addStreetAddressFormValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (FormManager.getActualValue('addrTxt') == '' && FormManager.getActualValue('poBox') == '') {
					return new ValidationResult(null, false, 'Please fill-out either Street or PO Box.');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');
}

function addAdditionalNameStreetContPOBoxValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {

				var isLocalBasedOnLanded = FormManager.getActualValue('defaultLandedCountry') == FormManager.getActualValue('landCntry');
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var isGmllcScenario = custSubGrp == 'LLC' || custSubGrp == 'LLCBP' || custSubGrp == 'LLCEX';
				var addrType = FormManager.getActualValue('addrType');
				var kenyaCntryCd = '764';
				var isIssuingCntryKenya = FormManager.getActualValue('cmrIssuingCntry') == kenyaCntryCd;
				var isKenyaLocalGmllc = isIssuingCntryKenya && addrType == 'ZS01' && isLocalBasedOnLanded && (custSubGrp == 'LLC' || custSubGrp == 'LLCBP');

				if ((isLocalBasedOnLanded && !(isGmllcScenario && addrType == 'ZS01')) || isKenyaLocalGmllc) {
					return new ValidationResult(null, true);
				}

				var isAddlNameFilled = false;
				var isStreetContPOBOXFilled = false;

				if (FormManager.getActualValue('custNm4') != '') {
					isAddlNameFilled = true;
				}

				if (FormManager.getActualValue('addrTxt2') != '' || FormManager.getActualValue('poBox') != '') {
					isStreetContPOBOXFilled = true;
				}

				if (isAddlNameFilled && isStreetContPOBOXFilled) {
					return new ValidationResult(null, false, 'Please fill-out either \'Additional Name or Address Information\' or \'Street Continuation\' and/or \'PO Box\' only.');
				}

				return new ValidationResult(null, true);
			}
		};
	})(), null, 'frmCMR_addressModal');
}

function setTypeOfCustomerBehavior() {
	if (FormManager.getActualValue('viewOnlyPage') == 'true' && FormManager.getActualValue('reqType') == 'U') {
		FormManager.readOnly('crosSubTyp');
		return;
	}

	if (FormManager.getActualValue('reqType') == 'C') {
		FormManager.hide('CrosSubTyp', 'crosSubTyp');
	} else if (FormManager.getActualValue('reqType') == 'U') {
		FormManager.show('CrosSubTyp', 'crosSubTyp');
		var role = FormManager.getActualValue('userRole').toUpperCase();
		if (role == 'REQUESTER') {
			FormManager.readOnly('crosSubTyp');
		} else if (role == 'PROCESSOR') {
			FormManager.enable('crosSubTyp');
		}
	}
}

function clearPhoneNoFromGrid() {
	for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
		recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
		if (_allAddressData != null && _allAddressData[i] != null) {
			if (!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZD01')) {
				_allAddressData[i].custPhone[0] = '';
			}
		}
	}
}

function clearPOBoxFromGrid() {
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var addrPOBoxEnabled = ['ZS01', 'ZP01']

	if (fstCEWA.includes(cntry)) {
		addrPOBoxEnabled.push('ZS02');
	}
	for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
		recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
		if (_allAddressData != null && _allAddressData[i] != null) {
			if (!(addrPOBoxEnabled.includes(_allAddressData[i].addrType[0]))) {
				_allAddressData[i].poBox[0] = '';
			}
		}
	}
}

function addAddressGridValidatorStreetPOBox() {
	console.log("addAddressGridValidatorStreetPOBox..............");
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
					var record = null;
					var type = null;

					var missingPOBoxStreetAddrs = '';
					for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
						record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
						if (record == null && _allAddressData != null && _allAddressData[i] != null) {
							record = _allAddressData[i];
						}
						type = record.addrType;
						if (typeof (type) == 'object') {
							type = type[0];
						}

						var addrIsNewOrUpdated = null;
						var reqType = FormManager.getActualValue('reqType');

						if (reqType == 'U') {
							if (record.updateInd[0] == 'U' || record.updateInd[0] == 'N') {
								addrIsNewOrUpdated = true;
							} else {
								addrIsNewOrUpdated = false;
							}
						} else {
							addrIsNewOrUpdated = true;
						}

						var isPOBoxOrStreetFilled = (record.poBox[0] != null && record.poBox[0] != '') || (record.addrTxt[0] != null && record.addrTxt[0] != '');
						if (!isPOBoxOrStreetFilled && addrIsNewOrUpdated) {
							if (missingPOBoxStreetAddrs != '') {
								missingPOBoxStreetAddrs += ', ' + record.addrTypeText[0];
							} else {
								missingPOBoxStreetAddrs += record.addrTypeText[0];
							}
						}
					}

					if (missingPOBoxStreetAddrs != '') {
						return new ValidationResult(null, false, 'Please fill-out either Street or PO BOX for the following address: ' + missingPOBoxStreetAddrs);
					}

					return new ValidationResult(null, true);

				}
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addInternalDeptNumberValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = null;
				var scenario = null;
				if (typeof (_pagemodel) != 'undefined') {
					reqType = FormManager.getActualValue('reqType');
					scenario = FormManager.getActualValue('custSubGrp');
				}

				if (!(scenario == 'INTER' || scenario == 'XINTE' || reqType == 'U')) {
					return new ValidationResult(null, true);
				}

				if (FormManager.getActualValue('viewOnlyPage') == 'true') {
					return new ValidationResult(null, true);
				}

				var internalDept = FormManager.getActualValue('ibmDeptCostCenter');

				if (internalDept == '') {
					return new ValidationResult(null, true);
				} else {
					if (internalDept.length != 6) {
						return new ValidationResult(null, false, 'Internal Department Number should be 6 characters long.');
					} else {
						return new ValidationResult(null, true);
					}
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function retainImportValues(fromAddress, scenario, scenarioChanged) {
	var isCmrImported = getImportedIndc();

	if (FormManager.getActualValue('reqType') == 'C' && isCmrImported == 'Y' && scenarioChanged) {

		if (scenario == 'COMME' || scenario == 'XCOM' || scenario == 'GOVRN' || scenario == 'XGOV' || scenario == 'THDPT' || scenario == 'XTP') {
			var reqId = FormManager.getActualValue('reqId');

			var origISU;
			var origClientTier;
			var origEnterprise;
			var origInac;
			var origSbo;

			var result = cmr.query("GET.CMRINFO.IMPORTED_AFRICA", {
				REQ_ID: reqId
			});

			if (result != null && result != '') {
				origISU = result.ret1;
				origClientTier = result.ret2;
				origEnterprise = result.ret3;
				origInac = result.ret4;
				origSbo = result.ret5;

				FormManager.setValue('isuCd', origISU);
				FormManager.setValue('clientTier', origClientTier);
				FormManager.setValue('salesBusOffCd', origSbo);
				FormManager.setValue('inacCd', origInac);
				// FormManager.setValue('enterprise', origEnterprise);
			}
		} else {
			FormManager.setValue('inacCd', '');
		}
	}
}

function addTinNumberValidationTz() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var tinNumber = FormManager.getActualValue('taxCd1');

				if (tinNumber.length > 0 && !tinNumber.match("([0-9]{3}-[0-9]{3}-[0-9]{3})")) {
					return new ValidationResult({
						id: 'taxCd1',
						type: 'text',
						name: 'taxCd1'
					}, false, 'Invalid format of TIN Number. Format should be NNN-NNN-NNN.');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addTinNumberValidationKn() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var tinNumber = FormManager.getActualValue('taxCd1');

				if (tinNumber.length > 0 && (!tinNumber.match(/^[0-9A-Z]*$/) || tinNumber.length != 11)) {
					return new ValidationResult({
						id: 'taxCd1',
						type: 'text',
						name: 'taxCd1'
					}, false, 'Invalid format of TIN Number. It should be 11 characters long containing only upper-case latin and numeric characters.');
				}

				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addTaxRegFormatValidationMadagascar() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var taxReg = FormManager.getActualValue('busnType');

				if (taxReg.length > 0 && !taxReg.match("([0-9]{5} [0-9]{2} [0-9]{4} [0-9]{1} [0-9]{5})")) {
					return new ValidationResult({
						id: 'busnType',
						type: 'text',
						name: 'busnType'
					}, false, 'Invalid format of Numero Statistique du Client. Format should be NNNNN NN NNNN N NNNNN.');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_CUST_TAB', 'frmCMR');
}
function addAttachmentValidatorOnTaxRegMadagascar() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var numeroStat = FormManager.getActualValue('busnType');
				var reqId = FormManager.getActualValue('reqId');

				if (FormManager.getActualValue('reqType') == 'U') {
					var result = cmr.query("GET.OLD_BUSN_TYP", {
						REQ_ID: reqId
					});
					var oldNumerStat = result.ret1;
					var curNumerStat = numeroStat;
					if (oldNumerStat == curNumerStat) {
						return new ValidationResult(null, true);
					}
				}

				var ret = cmr.query('CHECK_VATD_ATTACHMENT', {
					ID: reqId
				});

				var isAttachmentRequired = true;
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var custType = FormManager.getActualValue('custGrp')
				if (custType == 'CROSS' || custSubGrp == 'IBMEM' || custSubGrp == 'PRICU') {
					isAttachmentRequired = false;
				}

				if (isAttachmentRequired) {
					if (ret == null || ret.ret1 == null) {
						return new ValidationResult(null, false, 'VAT/TAX Documentation has not been attached to the request.');
					}
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_ATTACH_TAB', 'frmCMR');
}
var _importedIndc = null;
function getImportedIndc() {
	if (_importedIndc) {
		return _importedIndc;
	}
	var results = cmr.query('VALIDATOR.IMPORTED_ZS01', {
		REQID: FormManager.getActualValue('reqId')
	});
	if (results != null && results.ret1) {
		_importedIndc = results.ret1;
	} else {
		_importedIndc = 'N';
	}
	return _importedIndc;
}

function validateCMRForMCO2GMLLCScenario() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				console.log('checking requested cmr number...');
				var requestCMR = FormManager.getActualValue('cmrNo');
				var reqType = FormManager.getActualValue('reqType');
				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				var action = FormManager.getActualValue('yourAction');
				var requestID = FormManager.getActualValue('reqId');
				var landed = 'LANDED COUNTRY';
				var subCustGrp = FormManager.getActualValue('custSubGrp');
				var targetCntry = 'Kenya';
				var kenyaCntryCd = '764';

				if (reqType == 'C' && requestCMR != '' && cmrNo && (subCustGrp == 'LLCEX' || subCustGrp == 'XLLCX')) {
					var cmrStatusOrig = getCMRStatus(cntry, requestCMR);
					var cmrStatusDupl = getCMRStatus(kenyaCntryCd, requestCMR);

					if (requestCMR.length < 6) {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'CMR: ' + requestCMR + ' is invalid. Please enter valid CMR Number');
					}

					var res = cmr.query('GET_LAND_CNTRY_ZS01', {
						REQ_ID: requestID
					});

					if (res && res.ret1) {
						landed = res.ret1;
					}
					var landedCd = getCntryCdByLanded(landed);
					var cmrStatusLanded = getCMRStatus(landedCd, requestCMR);

					if (!validGmllcCntry.includes(landed)) {
						return new ValidationResult(null, true);
					}

					var exists = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
						COUNTRY: cntry,
						CMR_NO: requestCMR,
						MANDT: cmr.MANDT
					});
					if (exists && exists.ret1 && action != 'PCM' && cmrStatusOrig != 'C') {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'CMR: ' + requestCMR + ' is already in use in ' + cntry + '. Please use GM LLC sub-scenario in ' + landed + ' to create new CMR under both ' + targetCntry + ' and ' + landed);
					} else {
						exists = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
							COUNTRY: cntry,
							CMR_NO: requestCMR,
							MANDT: cmr.MANDT
						});
						if (exists && exists.ret1 && cmrStatusOrig != 'C') {
							return new ValidationResult({
								id: 'cmrNo',
								type: 'text',
								name: 'cmrNo'
							}, false, 'CMR: ' + requestCMR + ' is already in use in ' + cntry + '. Please use GM LLC sub-scenario in ' + landed + ' to create new CMR under both Kenya and ' + landed);
						}
					}
				}
				return new ValidationResult({
					id: 'cmrNo',
					type: 'text',
					name: 'cmrNo'
				}, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');

}

function gmllcExistingCustomerAdditionalValidations() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				console.log('checking requested cmr number...');
				var requestCMR = FormManager.getActualValue('cmrNo');
				var reqType = FormManager.getActualValue('reqType');
				var cntry = FormManager.getActualValue('cmrIssuingCntry');
				var action = FormManager.getActualValue('yourAction');
				var requestID = FormManager.getActualValue('reqId');
				var landed = 'LANDED COUNTRY';
				var subCustGrp = FormManager.getActualValue('custSubGrp');
				var targetCntry = 'Kenya';
				var kenyaCntryCd = '764';
				var targetCntryCd = 'KE';

				if (reqType == 'C' && requestCMR != '' && cmrNo && (subCustGrp == 'LLCEX' || subCustGrp == 'XLLCX')) {
					if (requestCMR.length < 6) {
						return;
					}

					var res = cmr.query('GET_LAND_CNTRY_ZS01', {
						REQ_ID: requestID
					});

					if (res && res.ret1) {
						landed = res.ret1;
					}

					if (!validGmllcCntry.includes(landed)) {
						return new ValidationResult(null, true);
					}

					var landedCd = getCntryCdByLanded(landed);
					var existInLandedCntry = checkIfCmrExist(landedCd, requestCMR);
					var existInDuplCntry = checkIfCmrExist(kenyaCntryCd, requestCMR);
					var cmrStatusLanded = getCMRStatus(landedCd, requestCMR);
					var cmrStatusDupl = getCMRStatus(kenyaCntryCd, requestCMR);

					// 1 all cewa
					if (!existInLandedCntry && !existInDuplCntry && action != 'PCM') {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'CMR does not exist in either ' + landed + ' or Kenya. Please use GM LLC under ' + landed + '. Processors are able to enter specific CMR if needed.');
					}
					// 2A - All Cewa except kenya
					if (!existInLandedCntry && cmrStatusDupl == 'C' && cntry != kenyaCntryCd) {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'Please note CMR in Kenya is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed
						+ ' and Kenya using GM LLC scenario under ' + landed + '.');
					}

					// 2B - For Kenya only
					if (cmrStatusLanded == 'C' && !existInDuplCntry && cntry == kenyaCntryCd) {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'Please note CMR in ' + landed + ' is Cancelled. It needs to be first reactivated, then you can proceed. Or you can create a new CMR under both ' + landed
						+ ' country and Kenya using GM LLC scenario under ' + landed + '.');
					} else if ((cmrStatusLanded == 'C' && existInDuplCntry && cntry != kenyaCntryCd) || (cmrStatusDupl == 'C' && existInLandedCntry && cntry == kenyaCntryCd)) {
						var issuingCd = landed;
						if (cntry == kenyaCntryCd && cmrStatusDupl == 'C' && cmrStatusLanded != 'C') {
							issuingCd = 'KE';
						}
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'Please note CMR in ' + issuingCd + ' is Cancelled. It needs to be either reactivated, or you can create a new CMR under both ' + landed
						+ ' country and Kenya using GM LLC scenario under ' + landed + '.');
					} else if ((cmrStatusLanded == 'C' && !existInDuplCntry) || (cmrStatusDupl == 'C' && cntry == kenyaCntryCd && !existInLandedCntry)) {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'CMR: ' + requestCMR + ' is already in use in ' + cntry + '. Please use GM LLC sub-scenario in ' + landed + ' to create new CMR under both ' + targetCntry + ' and ' + landed);
					}
				}
				return new ValidationResult({
					id: 'cmrNo',
					type: 'text',
					name: 'cmrNo'
				}, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function checkIfCmrExist(cntry, requestCMR) {
	var cmrExist = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
		COUNTRY: cntry,
		CMR_NO: requestCMR,
		MANDT: cmr.MANDT
	});

	if (cmrExist.ret1 == 'Y') {
		return true;
	}

	return false;
}



function getCntryCdByLanded(landed) {
	var cntryCd = cmr.query('GET_CNTRY_CD_BY_LANDED', {
		COUNTRY: landed,
	});

	return cntryCd.ret1;
}

function getCMRStatus(cntry, requestCMR) {
	var cmrStatus = cmr.query('LD.GET_STATUS', {
		COUNTRY: cntry,
		CMR_NO: requestCMR
	});

	return cmrStatus.ret1;
}



function enableCMRNOMCO2GLLC() {
	console.log('enabling/disabling cmr no...');
	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var subCustGrp = FormManager.getActualValue('custSubGrp');

	if (role == 'REQUESTER' && (subCustGrp == 'LLCEX' || subCustGrp == 'XLLCX')) {
		FormManager.enable('cmrNo');
		FormManager.addValidator('cmrNo', Validators.REQUIRED, ['CMR Number'], 'MAIN_IBM_TAB');
	} else if (role == 'REQUESTER' && (subCustGrp != 'LLCEX' || subCustGrp != 'XLLCX')) {
		FormManager.readOnly('cmrNo');
		FormManager.resetValidations('cmrNo');
	}
}

function enableCmrNumForProcessor() {
	var reqType = FormManager.getActualValue('reqType');
	var role = FormManager.getActualValue('userRole').toUpperCase();
	var isProspect = FormManager.getActualValue('prospLegalInd');
	if (reqType != 'C') {
		return;
	}
	if (dijit.byId('prospLegalInd')) {
		isProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
	}
	console.log("validateCMRNumberForLegacy ifProspect:" + isProspect);
	if ('Y' == isProspect) {
		FormManager.readOnly('cmrNo');
	} else if (role == "PROCESSOR") {
		FormManager.enable('cmrNo');
	} else {
		FormManager.readOnly('cmrNo');
	}
}

function registerMCO2VatValidator() {
	var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
	GEOHandler.registerValidator(addGenericVATValidator(issuingCntry, 'MAIN_CUST_TAB', 'frmCMR'), [issuingCntry], null, true);
}

function resetTinRequired() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') == 'C') {
		var cntry = FormManager.getActualValue('cmrIssuingCntry');
		var tinRequired = isNumeroTinRequired();
		if (cntry == '851') {
			if (tinRequired) {
				if (dijit.byId('taxCd2').get('checked')) {
					FormManager.clearValue('taxCd1');
					FormManager.readOnly('taxCd1');
					FormManager.removeValidator('taxCd1', Validators.REQUIRED);
				} else {
					FormManager.enable('taxCd1');
					FormManager.addValidator('taxCd1', Validators.REQUIRED, ['TIN Number'], 'MAIN_CUST_TAB');
				}
			} else {
				if (dijit.byId('taxCd2').get('checked')) {
					FormManager.clearValue('taxCd1');
					FormManager.readOnly('taxCd1');
					FormManager.removeValidator('taxCd1', Validators.REQUIRED);
				} else {
					FormManager.enable('taxCd1');
					FormManager.removeValidator('taxCd1', Validators.REQUIRED);
				}
			}
		}
	}
}

function resetNumeroRequired() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') == 'C') {
		var cntry = FormManager.getActualValue('cmrIssuingCntry');
		var numeroRequired = isNumeroTinRequired();
		if (cntry == '700') {
			if (numeroRequired) {
				if (dijit.byId('taxCd2').get('checked')) {
					FormManager.clearValue('busnType');
					FormManager.readOnly('busnType');
					FormManager.removeValidator('busnType', Validators.REQUIRED);
				} else {
					FormManager.enable('busnType');
					FormManager.addValidator('busnType', Validators.REQUIRED, ['Numero Statistique du Client'], 'MAIN_CUST_TAB');
				}
			} else {
				if (dijit.byId('taxCd2').get('checked')) {
					FormManager.clearValue('busnType');
					FormManager.readOnly('busnType');
					FormManager.removeValidator('busnType', Validators.REQUIRED);
				} else {
					FormManager.enable('busnType');
					FormManager.removeValidator('busnType', Validators.REQUIRED);
				}
			}
		}
	}
}

function setClientTierFieldMandt() {
	var isuCd = FormManager.getActualValue('isuCd');
	if (['34', '27', '36'].includes(isuCd)) {
		FormManager.addValidator('clientTier', Validators.REQUIRED, ['Client Tier'], 'MAIN_IBM_TAB');
	} else {
		FormManager.setValue('clientTier', '');
		FormManager.resetValidations('clientTier');
	}

	if (isuCd == '34') {
		FormManager.setValue('clientTier', 'Q');
	} else if (isuCd == '36') {
		FormManager.setValue('clientTier', 'Y');
	} else if (isuCd == '27') {
		FormManager.setValue('clientTier', 'E');
	} else {
		FormManager.setValue('clientTier', '');
	}
}


function lockUnlockISUCTC() {
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	if (['BUSPR', 'IBMEM', 'INTER', 'LLCBP', 'XBP', 'XINTE', 'XIBME', 'PRICU', 'XPRIC'].includes(custSubGrp)) {
		FormManager.readOnly('isuCd');
		FormManager.readOnly('clientTier');
	} else {
		FormManager.enable('isuCd');
		FormManager.enable('clientTier');
	}
}

function validateCollectionCd() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = null;
				if (typeof (_pagemodel) != 'undefined') {
					reqType = FormManager.getActualValue('reqType');
				}
				if (reqType != 'U') {
					return new ValidationResult(null, true);
				}
				if (FormManager.getActualValue('viewOnlyPage') == 'true') {
					return new ValidationResult(null, true);
				}
				var collectionCd = FormManager.getActualValue('collectionCd');
				if (collectionCd == '') {
					return new ValidationResult(null, true);
				} else {
					if (collectionCd.length != 6) {
						return new ValidationResult(null, false, 'Collection Code should be 6 characters long.');
					} else {
						return new ValidationResult(null, true);
					}
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

var _checklistBtnHandler = [];
function addChecklistBtnHandler() {
	var checklist = dojo.query('table.checklist');
  var radioBtns = checklist.query('input[type="radio"]');
  for (var i = 0; i < radioBtns.length; i++) {
    _checklistBtnHandler[i] = null;
    if (_checklistBtnHandler[i] == null) {
      _checklistBtnHandler[i] = dojo.connect(FormManager.getField('dijit_form_RadioButton_' + i), 'onClick', function (value) {
		        freeTxtFieldShowHide(Number(value.target.id.split("_").pop()));
      });
    }
  }
}

function freeTxtFieldShowHide(buttonNo) {
  var shouldDisplay = false;
  var fieldIdNo = getCheckListFieldNo(buttonNo);
  var element = document.getElementById('checklist_txt_field_' + fieldIdNo);
  var textFieldElement = document.getElementsByName('freeTxtField' + fieldIdNo)[0];

  if (buttonNo % 2 == 0) {
    shouldDisplay = true;
  } else {
    shouldDisplay = false;
  }
  if (shouldDisplay) {
    element.style.display = 'block';
  } else {
    element.style.display = 'none';
    textFieldElement.value = '';
  }
}

function getCheckListFieldNo(buttonNo) {
	if(buttonNo % 2 == 0){
	 return (buttonNo / 2) + 4;	
	}else{
	 return ((buttonNo-1) / 2) + 4;	
	}
}

function checkChecklistButtons() {
var checklist = dojo.query('table.checklist');
  var radioBtns = checklist.query('input[type="radio"]');
  for (var i = 0; i < radioBtns.length; i=i+2) {
	    if (document.getElementById('dijit_form_RadioButton_' + i).checked) {
	    var fieldNo = getCheckListFieldNo(i);
      document.getElementById('checklist_txt_field_' + fieldNo).style.display = 'block';
    }
  }
}


function setChecklistStatus() {
  console.log('validating checklist..');
  var checklist = dojo.query('table.checklist');
  document.getElementById("checklistStatus").innerHTML = "Not Done";
  var reqId = FormManager.getActualValue('reqId');
  var questions = checklist.query('input[type="radio"]');
  var textBoxes = checklist.query('input[type="text"]');

  if (reqId != null && reqId.length > 0 && reqId != 0) {
    if (questions.length > 0) {
      var noOfQuestions = questions.length / 2;
      var noOfTextBoxes = textBoxes.length;
      for (var i = 0; i < noOfTextBoxes; i++) {
        if (checklist.query('input[type="text"]')[i].value.trimEnd() == ''  &&  document.getElementById('checklist_txt_field_' + i).style.display == 'block') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }
      }

      var checkCount = 0;
      for (var i = 0; i < questions.length; i++) {
        if (questions[i].checked) {
          checkCount++;
        }
      }
      if (noOfQuestions != checkCount) {
        document.getElementById("checklistStatus").innerHTML = "Incomplete";
        FormManager.setValue('checklistStatus', "Incomplete");
      } else {
        document.getElementById("checklistStatus").innerHTML = "Complete";
        FormManager.setValue('checklistStatus', "Complete");
      }
    } else {
      document.getElementById("checklistStatus").innerHTML = "Complete";
      FormManager.setValue('checklistStatus', "Complete");
    }
  }
}

function addCEMEAChecklistValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        console.log('validating checklist..');
        var checklist = dojo.query('table.checklist');

        var questions = checklist.query('input[type="radio"]');
        var textBoxes = checklist.query('input[type="text"]');
        if (questions.length > 0) {
          var noOfQuestions = questions.length / 2;
          var noOfTextBoxes = textBoxes.length;
          for (var i = 0; i < noOfTextBoxes; i++) {
            if (checklist.query('input[type="text"]')[i].value.trimEnd() == '' && document.getElementById('checklist_txt_field_' + i).style.display == 'block') {
              return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
            }
          }

          var checkCount = 0;
          for (var i = 0; i < questions.length; i++) {
            if (questions[i].checked) {
              checkCount++;
            }
          }
          if (noOfQuestions != checkCount) {
            return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
          }

          // if question 8 = YES, country field is required
          // add check for checklist on DB
          var reqId = FormManager.getActualValue('reqId');
          var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
            REQID: reqId
          });
          if (!record || !record.sectionA1) {
            return new ValidationResult(null, false, 'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CHECKLIST_TAB', 'frmCMR');
}



function addEmbargoCodeValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var embargoCd = FormManager.getActualValue('embargoCd').toUpperCase();
				var reqType = FormManager.getActualValue('reqType');
				var role = FormManager.getActualValue('userRole').toUpperCase();
				if (role == 'REQUESTER' && reqType == 'C') {
					return new ValidationResult(null, true);
				}
				embargoCd = embargoCd.trim();
				if (embargoCd == '' || embargoCd == 'Y' || embargoCd == 'J') {
					return new ValidationResult(null, true);
				} else {
					return new ValidationResult({
						id: 'embargoCd',
						type: 'text',
						name: 'embargoCd'
					}, false, 'Embargo Code value should be only Y, J or Blank.');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addSalesBusOffValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				if (FormManager.getActualValue('viewOnlyPage') == 'true') {
					return new ValidationResult(null, true);
				}
				var input = FormManager.getActualValue('salesBusOffCd');
				if (input && input.length > 0 && isNaN(input)) {
					return new ValidationResult(null, false, input + ' is not a valid numeric value for SBO/Search Term (SORTL).');
				} else {
					return new ValidationResult(input, true);
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addSBOLengthValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var reqType = null;
				var scenario = null;
				if (FormManager.getActualValue('viewOnlyPage') == 'true') {
					return new ValidationResult(null, true);
				}
				var input = FormManager.getActualValue('salesBusOffCd');
				if (input.length != 4) {
					return new ValidationResult(null, false, 'SBO/Search Term (SORTL) should be 4 characters long.');
				} else {
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateCMRNoFORGMLLC() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var cmrNo = FormManager.getActualValue('cmrNo');
				var _custSubGrp = FormManager.getActualValue('custSubGrp');
				var targetCntryCd = '764';
				var action = FormManager.getActualValue('yourAction');
				var requestID = FormManager.getActualValue('reqId');
				var landed = 'LANDED COUNTRY';

				if (FormManager.getActualValue('reqType') != 'C') {
					return new ValidationResult(null, true);
				}
				if (cmrNo == '') {
					return new ValidationResult(null, true);
				}

				var res = cmr.query('GET_LAND_CNTRY_ZS01', {
					REQ_ID: requestID
				});

				if (res && res.ret1) {
					landed = res.ret1;
				}

				if (_custSubGrp != '' && (_custSubGrp == 'LLC' || _custSubGrp == 'LLCBP')) {
					var exist = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
						COUNTRY: targetCntryCd,
						CMR_NO: cmrNo,
						MANDT: cmr.MANDT
					});
					if (exist && exist.ret1 && action != 'PCM') {
						return new ValidationResult({
							id: 'cmrNo',
							type: 'text',
							name: 'cmrNo'
						}, false, 'CMR: ' + cmrNo + ' already exist in the system for Kenya.');
					}
				}

				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateKenyaCBGmllc() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var cmrNo = FormManager.getActualValue('cmrNo');
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var requestID = FormManager.getActualValue('reqId');
				var landed = 'LANDED COUNTRY';

				if (FormManager.getActualValue('reqType') != 'C') {
					return new ValidationResult(null, true);
				}
				if (custSubGrp != '' && (custSubGrp == 'XLLCX')) {

					var res = cmr.query('GET_LAND_CNTRY_ZS01', {
						REQ_ID: requestID
					});

					if (res && res.ret1) {
						landed = res.ret1;
					}

					if (landed == 'KE') {
						return new ValidationResult(null, true);
					}

					if (!validGmllcCntry.includes(landed)) {
						return new ValidationResult(null, false, landed + ' cannot be created as GM LLC. Please select different Cross-border sub-scenario.');
					}
				}

				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');

}

function showVatExempt() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}

	var vatRequired = isVatRequired();

	if (vatRequired) {
		// show
		FormManager.show('VATExempt', 'vatExempt');
		checkAndAddValidator('vat', Validators.REQUIRED, ['VAT'], 'MAIN_CUST_TAB');
	} else {
		// hide
		FormManager.hide('VATExempt', 'vatExempt');
		FormManager.removeValidator('vat', Validators.REQUIRED);
	}
}

function resetVatRequired() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}
	if (FormManager.getActualValue('reqType') != 'C') {
		return;
	}
	var vatRequired = isVatRequired();
	if (vatRequired) {
		if (dijit.byId('vatExempt').get('checked')) {
			FormManager.clearValue('vat');
			FormManager.readOnly('vat');
			FormManager.removeValidator('vat', Validators.REQUIRED);
		} else {
			FormManager.enable('vat');
			checkAndAddValidator('vat', Validators.REQUIRED, ['VAT'], 'MAIN_CUST_TAB');
		}
	}
}

function preTickVatExempt(fromAddress, scenario, scenarioChanged) {

	if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
		if (scenario == 'IBMEM' || scenario == 'PRICU' || scenario == 'XIBME' || scenario == 'XPRIC') {
			FormManager.setValue('vatExempt', true);
		} else {
			FormManager.setValue('vatExempt', false);
		}
	}
	resetVatRequired();
}

function preTickNumeroExempt(fromAddress, scenario, scenarioChanged) {

	if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
		var numeroRequired = isNumeroTinRequired();
		if (!numeroRequired) {
			FormManager.setValue('taxCd2', true);
		} else {
			FormManager.setValue('taxCd2', false);
		}
	}
	resetNumeroRequired();
}

function preTickTinExempt(fromAddress, scenario, scenarioChanged) {

	if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
		var tinRequired = isNumeroTinRequired();
		if (!tinRequired) {
			FormManager.setValue('taxCd2', true);
		} else {
			FormManager.setValue('taxCd2', false);
		}
	}
	resetTinRequired();
}

function isNumeroTinRequired() {
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var scenarioRequired = ['BUSPR', 'COMME', 'GOVRN', 'INTER', 'LLC', 'LLCBP', 'LLCEX', 'THDPT'];

	if (scenarioRequired.indexOf(custSubGrp) >= 0) {
		return true;
	}
	return false;
}

function isVatRequired() {
	var zs01Cntry = FormManager.getActualValue('cmrIssuingCntry');
	var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
		REQID: FormManager.getActualValue('reqId'),
		TYPE: 'ZS01'
	});
	if (ret && ret.ret1 && ret.ret1 != '') {
		zs01Cntry = ret.ret1;
	}
	if (GEOHandler.VAT_RQD_CROSS_LNDCNTRY.indexOf(zs01Cntry) >= 0) {
		return true;
	}
	return false;
}

function addAddressGridValidatorGMLLC() {
	console.log("addAddressGridValidatorGMLLC..............");
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var isGmllcScenario = custSubGrp == 'LLC' || custSubGrp == 'LLCBP' || custSubGrp == 'LLCEX';
				var kenyaCntryCd = '764';
				var isIssuingCntryKenya = FormManager.getActualValue('cmrIssuingCntry') == kenyaCntryCd;
				var isKenyaLocalGmllc = isIssuingCntryKenya && (custSubGrp == 'LLC' || custSubGrp == 'LLCBP');

				if (!isGmllcScenario || isKenyaLocalGmllc) {
					return new ValidationResult(null, true);
				}
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

						if (type != 'ZS01') {
							continue;
						}

						var streetCont;
						var isAddlNameFilled = false;
						var isStreetContPOBOXFilled = false;

						if ((record.custNm4[0] != '' && record.custNm4[0] != null)) {
							isAddlNameFilled = true;
						}

						if ((record.addrTxt2[0] != '' && record.addrTxt2[0] != null) || (record.poBox[0] != '' && record.poBox[0] != null)) {
							isStreetContPOBOXFilled = true;
						}

						if (isAddlNameFilled && isStreetContPOBOXFilled) {
							return new ValidationResult(null, false,
								'Please update Sold-to (Main) Address. Fill-out either \'Additional Name or Address Information\' or \'Street Continuation\' and/or \'PO Box\' only.');
						}
					}
					return new ValidationResult(null, true);
				}
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
}

/* End 1430539 */

// CREATCMR-4293
function setCTCValues() {

	FormManager.removeValidator('clientTier', Validators.REQUIRED);

	var custSubGrp = FormManager.getActualValue('custSubGrp');

	// Business Partner
	var custSubGrpForBusinessPartner = ['BUSPR', 'LSBP', 'LSXBP', 'NABP', 'NAXBP', 'SZBP', 'SZXBP', 'XBP', 'ZABP', 'ZAXBP', 'LLCBP'];

	// Business Partner
	if (custSubGrpForBusinessPartner.includes(custSubGrp)) {
		FormManager.removeValidator('clientTier', Validators.REQUIRED);
		var isuCd = FormManager.getActualValue('isuCd');
		if (isuCd == '8B') {
			FormManager.setValue('clientTier', '');
		}
	}

	// Internal and IBM Employee
	var custSubGrpForInternal = ['INTER', 'XINTE', 'IBMEM', 'XIBME', 'LSIBM', 'LSXIB', 'LSXIN', 'LSINT', 'NAIBM', 'NAXIB', 'NAXIN', 'NAINT', 'SZIBM', 'SZXIB', 'SZXIN', 'SZINT', 'ZAIBM', 'ZAXIB',
		'ZAXIN', 'ZAINT'];

	// Internal
	if (custSubGrpForInternal.includes(custSubGrp)) {
		FormManager.removeValidator('clientTier', Validators.REQUIRED);
		var isuCd = FormManager.getActualValue('isuCd');
		if (isuCd == '21') {
			FormManager.setValue('clientTier', '');
		}
	}
}

function addPpsCeidValidator() {
	var _custType = FormManager.getActualValue('custSubGrp');
	var reqType = FormManager.getActualValue('reqType');

	if (reqType == 'C') {
		if (_custType == 'BUSPR' || _custType == 'XBP') {
			FormManager.show('PPSCEID', 'ppsceid');
			FormManager.enable('ppsceid');
			FormManager.addValidator('ppsceid', Validators.REQUIRED, ['PPS CEID'], 'MAIN_IBM_TAB');
		} else {
			FormManager.setValue('ppsceid', '');
			FormManager.readOnly('ppsceid');
			FormManager.removeValidator('ppsceid', Validators.REQUIRED);
		}
	}
}

function clientTierCodeValidator() {
	var isuCode = FormManager.getActualValue('isuCd');
	var clientTierCode = FormManager.getActualValue('clientTier');
	var reqType = FormManager.getActualValue('reqType');

	if (!['34', '36', '27'].includes(isuCode)) {
		if (clientTierCode == '') {
			$("#clientTierSpan").html('');

			return new ValidationResult(null, true);
		} else {
			$("#clientTierSpan").html('');

			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier can only accept blank.');
		}
	} else if (isuCode == '34') {
		if (clientTierCode == '') {
			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier code is Mandatory.');
		} else if (clientTierCode == 'Q') {
			return new ValidationResult(null, true);
		} else {
			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier can only accept \'Q\'\'.');
		}
	} else if (isuCode == '36') {
		if (clientTierCode == '') {
			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier code is Mandatory.');
		} else if (clientTierCode == 'Y') {
			return new ValidationResult(null, true);
		} else {
			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier can only accept \'Y\'\'.');
		}
	} else if (isuCode == '32' && clientTierCode == 'T') {
		return new ValidationResult({
			id: 'clientTier',
			type: 'text',
			name: 'clientTier'
		}, false, 'ISU-CTC combination is obsolete.');
	} else if (isuCode == '27') {
		if (clientTierCode == '') {
			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier code is Mandatory.');
		} else if (clientTierCode == 'E') {
			return new ValidationResult(null, true);
		} else {
			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier can only accept \'E\'\'.');
		}
	} else {
		if (clientTierCode == 'Q' || clientTierCode == 'Y' || clientTierCode == 'E' || clientTierCode == '') {
			$("#clientTierSpan").html('');

			return new ValidationResult(null, true);
		} else {
			$("#clientTierSpan").html('');
			$("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

			return new ValidationResult({
				id: 'clientTier',
				type: 'text',
				name: 'clientTier'
			}, false, 'Client Tier can only accept \'Q\', \'Y\' or blank.');
		}
	}
}

// CREATCMR-4293

function clientTierValidator() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var valResult = null;
				valResult = clientTierCodeValidator();

				return valResult;
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function enableTinNumber() {
	var viewOnly = FormManager.getActualValue('viewOnlyPage');
	if (viewOnly != '' && viewOnly == 'true') {
		return;
	}
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	if (cntry == '764') {
		if (FormManager.getActualValue('reqType') == 'C') {

			var custType = FormManager.getActualValue('custGrp')
			if (custType == 'LOCAL') {
				FormManager.enable('taxCd1');
			}
			if (custType == 'CROSS') {
				FormManager.clearValue('taxCd1');
				FormManager.readOnly('taxCd1');
			}
		} else if (FormManager.getActualValue('reqType') == 'U') {

			var soldToLandedCountry = getSoldToLanded();
			if (soldToLandedCountry == 'KE') {
				FormManager.enable('taxCd1');
			} else {
				FormManager.readOnly('taxCd1');
			}
		}
	}
}

function getSoldToLanded() {
	var countryCd = FormManager.getActualValue('landCntry');
	var _zs01ReqId = FormManager.getActualValue('reqId');
	var cntryCdParams = {
		REQ_ID: _zs01ReqId,
		ADDR_TYPE: 'ZS01',
	};
	var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);

	if (cntryCdResult.ret1 != undefined) {
		countryCd = cntryCdResult.ret1;
	}

	return countryCd;
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
// CREATCMR-788
function addressQuotationValidatorMCO2() {
	FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, ['Abbreviated Name'], 'MAIN_CUST_TAB');
	FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, ['Abbreviated Location'], 'MAIN_CUST_TAB');
	FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name']);
	FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Continuation']);
	FormManager.addValidator('custNm4', Validators.NO_QUOTATION, ['Additional Name or Address Information']);
	FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Street']);
	FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Street Continuation']);
	FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City']);
	//  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
	FormManager.addValidator('poBox', Validators.NO_QUOTATION, ['PO Box']);
	FormManager.addValidator('custPhone', Validators.NO_QUOTATION, ['Phone #']);

}


function StcOrderBlockValidation() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				// var role = FormManager.getActualValue('userRole').toUpperCase();
				var ordBlk = FormManager.getActualValue('embargoCd');
				var stcOrdBlk = FormManager.getActualValue('taxExemptStatus3');
				if (ordBlk == null || ordBlk == '') {
					if (stcOrdBlk == 'ST' || stcOrdBlk == '') {
					} else {
						return new ValidationResult(null, false, 'Only ST and blank STC order block code allowed.');
					}
				} else if (ordBlk != '' && stcOrdBlk != '') {
					return new ValidationResult(null, false, 'Please fill either STC order block code or Embargo Code field');
				}
				return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_CUST_TAB', 'frmCMR');
}

// Coverage 1H 2024
function setEntpValue() {
	console.log('----------- setEntpValue -------------');
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	var entp = 'noPreSelect';
	var existingEntp = FormManager.getActualValue('enterprise');
	var isuCd = FormManager.getActualValue('isuCd');
	var ctc = FormManager.getActualValue('clientTier');
	var landCntry = FormManager.getActualValue('landCntry');
	if (landCntry == '') {
		landCntry = getZS01LandCntry();
	}
	var isuCTC = isuCd + ctc;
	if (existingEntp == '' && _pagemodel.enterprise != '') {
		existingEntp = _pagemodel.enterprise;
	}
	if (['COMME', 'GOVRN', 'THDPT', 'LLC', 'LLCEX', 'PRICU'].includes(custSubGrp) || (['XCOM', 'XTP', 'XLLCX', 'XGOV', 'XPRIC'].includes(custSubGrp) && !isMEACntry(landCntry) && (landCntry != '' && landCntry != undefined))) {
		if (isuCTC == '34Q') {
			switch (cntry) {
				case '610':
				case '669':
				case '782':
				case '827':
					entp = '911741';
					break;

				case '636':
				case '769':
				case '883':
					entp = '901441';
					break;

				case '667':
				case '662':
				case '810':
				case '692':
				case '656':
				case '383':
				case '880':
				case '881':
					entp = '911757';
					break;

				case '698':
					entp = '911733';
					break;

				case '725':
				case '770':
				case '833':
					entp = '901444';
					break;

				case '764':
					entp = '911730';
					break;

				case '700':
				case '373':
				case '876':
					entp = '911740';
					break;

				case '804':
					entp = '911737';
					break;

				case '753':
				case '691':
				case '879':
				case '382':
				case '717':
				case '635':
					entp = '906402';
					break;

				case '840':
				case '841':
				case '637':
				case '718':
					entp = '911758';
					break;

				case '645':
				case '670':
				case '745':
				case '831':
				case '835':
				case '842':
				case '851':
				case '857':
					entp = '907897';
					break;

				case '825':
					entp = '911743';
					break;
			}
		} else if (((isuCTC == '36Y' && !['BUILD1', 'DISTR1', 'SRVCE1'].includes(existingEntp)) || (isuCTC == '5K')) && existingEntp != '') {
			entp = '';
		} else if (['36Y'].includes(isuCTC) && ['BUILD1', 'DISTR1', 'SRVCE1'].includes(existingEntp) && existingEntp != '') {
			entp = existingEntp;
		}
		FormManager.enable('enterprise');
		if (entp != 'noPreSelect')
			FormManager.setValue('enterprise', entp);
		if (['PRICU', 'XPRIC'].includes(custSubGrp)) {
			FormManager.readOnly('enterprise');
		}
	} else if (['BUSPR', 'LLCBP', 'INTER', 'IBMEM', 'XBP', 'XIBME', 'XINTE'].includes(custSubGrp)) {
		FormManager.readOnly('enterprise');
		FormManager.setValue('enterprise', '');
	}
}

function getZS01LandCntry() {
	var result1 = cmr.query('LANDCNTRY.IT', {
		REQID: FormManager.getActualValue('reqId')
	});
	if (result1 != null && result1.ret1 != undefined) {
		zs01landCntry = result1.ret1;
	}
	return result1.ret1;
}

function getCntryCd() {
	var result1 = cmr.query('QUICK.GET_DEFAULT_COUNTRY', {
		CNTRY: FormManager.getActualValue('cmrIssuingCntry')
	});
	if (result1 != null && result1.ret1 != undefined) {
		zs01landCntry = result1.ret1;
	}
	return result1.ret1;

}
function isMEACntry(landCntry) {
	var MEACntries = ['AE', 'AF', 'AO', 'BF', 'BH', 'BI', 'BJ', 'BW', 'CF', 'CG', 'CI', 'CM', 'CV', 'DJ', 'DZ', 'EG', 'ER',
		'ET', 'GA', 'GH', 'GM', 'GN', 'GQ', 'GW', 'IQ', 'JO', 'KE', 'KW', 'LB', 'LR', 'LS', 'LY', 'MA', 'MG', 'ML', 'MR', 'MU', 'MW',
		'MZ', 'NA', 'NE', 'NG', 'OM', 'PK', 'PS', 'QA', 'RW', 'SA', 'SC', 'SD', 'SL', 'SN', 'SO', 'ST', 'SY', 'SZ', 'TD', 'TG', 'TN',
		'TR', 'TZ', 'UG', 'YE', 'ZA', 'ZM', 'ZW'];

	if (MEACntries.includes(landCntry)) {
		return true;
	} else {
		return false;
	}
}

function validateISUCTCEnterprisefrLOCALAndNonMEA() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var custGrp = FormManager.getActualValue('custGrp');
				var isuCTC = FormManager.getActualValue('isuCd') + FormManager.getActualValue('clientTier');
				var entp = FormManager.getActualValue('enterprise');
				var valid = false;
				var valid_Entp = '';
				var reqType = FormManager.getActualValue('reqType');
				var landCntry = FormManager.getActualValue('landCntry');

				if (landCntry == '') {
					landCntry = getZS01LandCntry();
				}

				if (reqType == 'U' || (custGrp == 'CROSS' && isMEACntry(landCntry))) {
					return new ValidationResult(null, true);
				}

				if (['BUSPR', 'LLCBP', 'XBP'].includes(custSubGrp) && ['8B'].includes(isuCTC) && entp == '') {
					valid = true;
				} else if (['INTER', 'IBMEM', 'XINTE', 'XIBME'].includes(custSubGrp) && ['21'].includes(isuCTC) && entp == '') {
					valid = true;
				} else if (['COMME', 'GOVRN', 'THDPT', 'LLC', 'LLCEX', 'PRICU', 'XCOM', 'XGOV', 'XTP', 'XPRIC'].includes(custSubGrp)) {
					if (!['34Q', '36Y', '5K'].includes(isuCTC)) {
						valid = true;
					} else if (isuCTC == '34Q') {
						valid_Entp = getValidEntp();
						valid = (entp == valid_Entp);
					} else if (isuCTC == '36Y') {
						valid = ['BUILD1', 'DISTR1', 'SRVCE1'].includes(entp);
						valid_Entp = 'BUILD1, DISTR1, SRVCE1';
					} else if (isuCTC == '5K' && entp == '') {
						valid = true;
					}

				}
				if (valid) {
					return new ValidationResult(null, true);
				} else {
					return new ValidationResult(null, false, 'Please select correct ISU , CTC and Enterprise ' + valid_Entp + ' combination.');
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateISUCTCEnterprisefrCROSS() {
	FormManager.addFormValidator((function() {
		return {
			validate: function() {
				var custGrp = FormManager.getActualValue('custGrp');
				var custSubGrp = FormManager.getActualValue('custSubGrp');
				var req_entp = FormManager.getActualValue('enterprise');
				var isuCTC = FormManager.getActualValue('isuCd') + FormManager.getActualValue('clientTier');
				var valid = false;
				var valid_EntpList = [];
				localStorage.setItem("validateLogicCalled", true);
				var reqType = FormManager.getActualValue('reqType');
				var landCntry = getZS01LandCntry();

				if (reqType == 'U' || custGrp == 'LOCAL' || landCntry == '' || landCntry == undefined || (custGrp == 'CROSS' && !isMEACntry(landCntry))) {
					return new ValidationResult(null, true);
				}
				var preSelValidEntp = getMEAPreSelectedCBLogicEntp();
				localStorage.setItem("validateLogicCalled", false);
				if (preSelValidEntp)
					valid_EntpList.push(preSelValidEntp);
				var arr = getMEAEntpUserAdded();
				if (arr.length > 0) {
					valid_EntpList.push(...arr);
				}
				if (valid_EntpList.length > 0) {
					valid = valid_EntpList.includes(req_entp);
				}

				if (['XBP'].includes(custSubGrp) && ['8B'].includes(isuCTC) && req_entp == '') {
					valid = true;
				} else if (['XINTE', 'XIBME'].includes(custSubGrp) && ['21'].includes(isuCTC) && req_entp == '') {
					valid = true;
				}

				if (valid) {
					return new ValidationResult(null, true);
				} else {
					return new ValidationResult(null, false, 'Please select correct  Enterprise -> ' + valid_EntpList + ' for given ISU, CTC and SubIndustry combination.');
				}
			}
		};
	})(), 'MAIN_IBM_TAB', 'frmCMR');
}


function getMEAEntpUserAdded() {
	var landCntry = FormManager.getActualValue('landCntry');
	var isuCd = FormManager.getActualValue('isuCd');
	var ctc = FormManager.getActualValue('clientTier');

	var entpList = [];
	if (landCntry == '') {
		landCntry = getZS01LandCntry();
	}
	if (landCntrySA.includes(landCntry)) {
		entpList = getSAfricaEntpList(landCntry, isuCd, ctc);
	} else if (landCntryCEWA.includes(landCntry)) {
		entpList = getCEWAEntpList(landCntry, isuCd, ctc);
	} else if (landCntryMEA.includes(landCntry)) {
		entpList = getMEEntpList(landCntry, isuCd, ctc);
	} else if (landCntry == 'TR') {
		entpList = getTurkeyEntpList(landCntry, isuCd, ctc);
	}

	return entpList;
}


function getSAfricaEntpList(landCntry, isuCd, ctc) {
	var isuCTC = isuCd + ctc;
	var list = [];
	if (landCntry == 'ZA') {
		if (isuCTC == '27E') {
			list.push('011680', '011684', '011679', '011681', '011682', '011683');
		} else if (isuCTC == '34Q') {
			list.push('004179', '011678');
		} else if (isuCTC == '36Y') {
			list.push('008028', '010032', '012430');
		}
	} else if (landCntry == 'NA' && isuCTC == '36Y') {
		list.push('009814', '012095', '012096');
	} else if (landCntry == 'LS' && isuCTC == '36Y') {
		list.push('012097', '012098', '012099');
	} else if (landCntry == 'SZ' && isuCTC == '36Y') {
		list.push('012100', '012101', '012102');
	} else if (['04', '12', '28', '4F', '5K'].includes(isuCTC) && ['ZA', 'NA', 'LS', 'SZ'].includes(landCntry)) {
		list.push('');
	}
	return list;
}


function getCEWAEntpList(landCntry, isuCd, ctc) {
	var isuCTC = isuCd + ctc;
	var list = [];

	if (isuCTC == '36Y') {
		list.push('BUILD1', 'DISTR1', 'SRVCE1');
	} else if (isuCTC == '5K') {
		list.push('');
	}
	return list;
}

function getMEEntpList(landCntry, isuCd, ctc) {
	var isuCTC = isuCd + ctc;
	var list = [];
	if (landCntry == 'AE') {
		if (isuCTC == '34Q') {
			list.push('911811', '911810');
		} else if (isuCTC == '36Y') {
			list.push('912073', '912075', '912074');
		}
	} else if (landCntry == 'SA') {
		if (isuCTC == '34Q') {
			list.push('911690', '911685', '911691', '911702', '911687', '911697');
		} else if (isuCTC == '36Y') {
			list.push('908025', '912094', '912093');
		}
	} else if (landCntry == 'KW') {
		if (isuCTC == '27E') {
			list.push('911297');
		} else if (isuCTC == '36Y') {
			list.push('912085', '912087', '912086');
		}
	} else if (landCntry == 'OM') {
		if (isuCTC == '27E') {
			list.push('911823');
		} else if (isuCTC == '36Y') {
			list.push('912080', '912079', '912081');
		}
	} else if (['IR', 'SY', 'YE', 'JO', 'PS', 'LB'].includes(landCntry)) {
		if (isuCTC == '27E') {
			list.push('911826');
		} else if (isuCTC == '36Y') {
			list.push('908024', '912072', '912071');
		}
	} else if (landCntry == 'BH') {
		if (isuCTC == '36Y') {
			list.push('912084', '912082', '912083');
		}
	} else if (landCntry == 'EG') {
		if (isuCTC == '34Q') {
			list.push('911829', '911831', '911835', '911275', '911833');
		} else if (isuCTC == '36Y') {
			list.push('908023', '912070', '912069');
		}
	} else if (landCntry == 'QA') {
		if (isuCTC == '34Q') {
			list.push('911817');
		} else if (isuCTC == '36Y') {
			list.push('912088', '912090', '912089');
		}
	} else if (['TN', 'MO', 'LY'].includes(landCntry)) {
		if (isuCTC == '36Y') {
			list.push('BUILD1', 'DISTR1', 'SRVCE1');
		}
	} else if (['PK', 'AF'].includes(landCntry)) {
		if (isuCTC == '36Y') {
			list.push('908027', '912092', '912091');
		}
	} else if (isuCTC == '5K') {
		list.push('');
	}
	return list;
}

function getTurkeyEntpList(landCntry, isuCd, ctc) {
	var isuCTC = isuCd + ctc;
	var list = [];
	if (isuCTC == '34Q') {
		list.push('911703', '911716', '911704');
	} else if (isuCTC == '36Y') {
		list.push('908030', '912103', '912104');
	} else if ('04', '28', '5K') {
		list.push('');
	}
	return list;
}


function getValidEntp() {
	var cntry = FormManager.getActualValue('cmrIssuingCntry');
	switch (cntry) {
		case '610':
		case '669':
		case '782':
		case '827':
			entp = '911741';
			break;

		case '636':
		case '769':
		case '883':
			entp = '901441';
			break;

		case '667':
		case '662':
		case '810':
		case '692':
		case '656':
		case '383':
		case '880':
		case '881':
			entp = '911757';
			break;

		case '698':
			entp = '911733';
			break;

		case '725':
		case '770':
		case '833':
			entp = '901444';
			break;

		case '764':
			entp = '911730';
			break;

		case '700':
		case '373':
		case '876':
			entp = '911740';
			break;

		case '804':
			entp = '911737';
			break;

		case '753':
		case '691':
		case '879':
		case '382':
		case '717':
		case '635':
			entp = '906402';
			break;

		case '840':
		case '841':
		case '637':
		case '718':
			entp = '911758';
			break;

		case '645':
		case '670':
		case '745':
		case '831':
		case '835':
		case '842':
		case '851':
		case '857':
			entp = '907897';
			break;

		case '825':
			entp = '911743';
			break;
	}

	return entp;
}


//for Cross-Border
function getMEAPreSelectedCBLogicEntp() {
	var entp = undefined;
	var custSubGrp = FormManager.getActualValue('custSubGrp');
	if (['XBP', 'XINTE', 'XIBME'].includes(custSubGrp)) {
		FormManager.readOnly('enterprise');
		entp = '';
		return entp;
	}
	var landCntry = FormManager.getActualValue('landCntry');
	if (landCntry == '') {
		landCntry = getZS01LandCntry();
	}
	if ((landCntry && !isMEACntry(landCntry)) || (landCntry == '' || landCntry == undefined)) {
		return;
	}
	var cmrCntryCd = getCntryCd();
	var isuCd = FormManager.getActualValue('isuCd');
	var ctc = FormManager.getActualValue('clientTier');
	var subInd = FormManager.getActualValue('subIndustryCd') ? FormManager.getActualValue('subIndustryCd') : localStorage.getItem("oldISIC");
	var arr = [];

	if (FormManager.getActualValue('custGrp') != 'CROSS' || (cmrCntryCd == landCntry)) {
		return;
	}

	if (landCntrySA.includes(landCntry)) {
		entp = getSouthAfricaEntpPreSelected(landCntry, isuCd, ctc, subInd);
	} else if (landCntryCEWA.includes(landCntry)) {
		entp = getCEWAEntpPreSelected(landCntry, isuCd, ctc, subInd);
	} else if (landCntryMEA.includes(landCntry)) {
		entp = getMEEntpPreSelected(landCntry, isuCd, ctc, subInd);
	} else if (landCntry == 'TR') {
		entp = getTurkeyEntpPreSelected(landCntry, isuCd, ctc, subInd);
	}
	var toSetEntp = true;
	if (localStorage.getItem("validateLogicCalled") === 'true') {
		toSetEntp = false;
	}
	if (entp != 'noPreSelect' && toSetEntp)
		FormManager.setValue('enterprise', entp);

	if ((entp == '' && ['XBP', 'XIBME', 'XINTE'].includes(custSubGrp)) || custSubGrp == 'XPRIC') {
		FormManager.readOnly('enterprise');
	} else {
		FormManager.enable('enterprise');
	}

	if (entp == 'noPreSelect') {
		arr.push(...getMEAEntpUserAdded());
		if (arr.length > 0 && !arr.includes(FormManager.getActualValue('enterprise')))
			FormManager.setValue('enterprise', '');
		entp = '';
	}
	return entp;
}

var subIndEntpTRMapping = {
	'A': '911727',
	'B': '911710',
	'C': '911725',
	'D': '911711',
	'E': '911712',
	'F': '911708',
	'H': '911719',
	'J': '911706',
	'K': '911724',
	'L': '911713',
	'M': '911721',
	'N': '911722',
	'P': '911709',
	'R': '911726',
	'S': '911707',
	'T': '911728',
	'U': '911714',
	'V': '911705',
	'W': '911720',
	'X': '911723',
	'Y': '911716'
};

function getTurkeyEntpPreSelected(landCntry, isuCd, ctc, subInd) {
	var isuCTC = isuCd + ctc;
	var entp = undefined;
	if (isuCTC == '27E') {
		if (['GB', 'GF', 'GJ', 'GN'].includes(subInd)) {
			entp = '911715';
			return entp;
		} else if (['GA', 'GC', 'GD', 'GE', 'GG', 'GH', 'GK', 'GL', 'GM', 'GP', 'GR', 'GW'].includes(subInd)) {
			entp = '911718';
			return entp;
		}
		if (subInd) {
			subInd = subInd.substring(0, 1);
			entp = subIndEntpTRMapping[subInd];
		}
	}
	return entp;

}


function getSouthAfricaEntpPreSelected(landCntry, isuCd, ctc, subInd) {
	var isuCTC = isuCd + ctc;
	var entp = 'noPreSelect';
	if (isuCTC == '27E') {
		if (landCntry == 'ZA' && subInd) {
			subInd = subInd.substr(0, 1);
			if (['A', 'K', 'U'].includes(subInd)) {
				entp = '011675';
			} else if (['B', 'C'].includes(subInd)) {
				entp = '011677';
			} else if (['D', 'R', 'T', 'W'].includes(subInd)) {
				entp = '011676';
			} else if (['E', 'G', 'H', 'X', 'Y'].includes(subInd)) {
				entp = '011672';
			} else if (['F', 'N', 'S'].includes(subInd)) {
				entp = '011673';
			} else if (['J', 'L', 'M', 'P', 'V'].includes(subInd)) {
				entp = '011674';
			}
		} else if (landCntry == 'NA') {
			entp = '909813';
		} else if (landCntry == 'LS') {
			entp = '910510';
		} else if (landCntry == 'SZ') {
			entp = '910509';
		}

	} else if (['04', '12', '28', '4F', '5K'].includes(isuCTC)) {
		entp = '';
	}

	return entp;
}


function getCEWAEntpPreSelected(landCntry, isuCd, ctc, subInd) {
	var isuCTC = isuCd + ctc;
	var entp = 'noPreSelect';
	if (isuCTC == '34Q') {
		if (['AO', 'CV', 'MZ', 'ST'].includes(landCntry)) {
			entp = '911741';
		} else if (['BW', 'MW', 'ZM'].includes(landCntry)) {
			entp = '901441';
		} else if (['CG', 'CD', 'CF', 'CM', 'GA', 'GQ', 'NE', 'TD'].includes(landCntry)) {
			entp = '911757';
		} else if (['ET'].includes(landCntry)) {
			entp = '911733';
		} else if (['GH', 'LR', 'SL'].includes(landCntry)) {
			entp = '901444';
		} else if (['KE'].includes(landCntry)) {
			entp = '911730';
		} else if (['LY'].includes(landCntry)) {
			entp = '911756';
		} else if (['MG', 'MU', 'SC'].includes(landCntry)) {
			entp = '911740';
		} else if (['NG'].includes(landCntry)) {
			entp = '911737';
		} else if (['GM', 'GN', 'GW', 'ML', 'MR', 'SN'].includes(landCntry)) {
			entp = '906402';
		} else if (['BF', 'BJ', 'TG', 'CI'].includes(landCntry)) {
			entp = '911758';
		} else if (['TN'].includes(landCntry)) {
			entp = '901728';
		} else if (['BI', 'DJ', 'ER', 'RW', 'SO', 'SD', 'TZ', 'UG'].includes(landCntry)) {
			entp = '907897';
		} else if ('ZW' == landCntry) {
			entp = '911743';
		} else if ('DZ' == landCntry) {
			entp = '911755';
		}
	} else if (isuCTC == '5K') {
		entp = '';
	}
	return entp;
}

function getMEEntpPreSelected(landCntry, isuCd, ctc, subInd) {
	var isuCTC = isuCd + ctc;
	var entp = 'noPreSelect';
	if (isuCTC == '27E') {
		if (landCntry == 'AE' && subInd) {
			subInd = subInd.substr(0, 1);
			if (['E', 'G', 'H', 'X', 'Y'].includes(subInd)) {
				entp = '911812';
			} else if (['F', 'N', 'S'].includes(subInd)) {
				entp = '911813';
			} else if (['A', 'D', 'K', 'R', 'T', 'U', 'W'].includes(subInd)) {
				entp = '911814';
			} else if (['J', 'L', 'M', 'P', 'V'].includes(subInd)) {
				entp = '911815';
			} else if (['B', 'C'].includes(subInd)) {
				entp = '911816';
			}
		} else if (landCntry == 'SA' && subInd) {
			subInd = subInd.substr(0, 1);
			if (['A', 'K'].includes(subInd)) {
				entp = '911693';
			} else if (['B', 'C'].includes(subInd)) {
				entp = '911700';
			} else if (['D', 'L', 'R', 'W'].includes(subInd)) {
				entp = '911699';
			} else if (['J', 'T'].includes(subInd)) {
				entp = '911701';
			} else if (['M', 'P', 'U'].includes(subInd)) {
				entp = '901469';
			} else if (['N'].includes(subInd)) {
				entp = '911698';
			} else if (['F', 'S'].includes(subInd)) {
				entp = '911688';
			}
		} else if (landCntry == 'KW') {
			entp = '907695';
		} else if (landCntry == 'OM') {
			entp = '911824';
		} else if (['IQ', 'SY', 'YE', 'JO', 'LB', 'PS'].includes(landCntry)) {
			entp = '911827';
		} else if (landCntry == 'BH') {
			entp = '911825';
		}

	} else if (isuCTC == '34Q') {
		if (landCntry == 'SA' && subInd) {
			subInd = subInd.substr(0, 1);
			if (['E'].includes(subInd)) {
				entp = '911695';
			} else if (['G', 'V', 'Y'].includes(subInd)) {
				entp = '911692';
			} else if (['H', 'X'].includes(subInd)) {
				entp = '911696';
			}
		} else if (landCntry == 'EG' && subInd) {
			subInd = subInd.substr(0, 1);
			if (['A'].includes(subInd)) {
				entp = '911771';
			} else if (['B', 'C'].includes(subInd)) {
				entp = '911772';
			} else if (['D', 'J', 'K', 'L', 'R', 'T', 'W'].includes(subInd)) {
				entp = '911836';
			} else if (['E'].includes(subInd)) {
				entp = '911773';
			} else if (['F'].includes(subInd)) {
				entp = '911830';
			} else if (['G', 'V', 'Y'].includes(subInd)) {
				entp = '911832';
			} else if (['H', 'X'].includes(subInd)) {
				entp = '911768';
			} else if (['M', 'U'].includes(subInd)) {
				entp = '901456';
			} else if (['N'].includes(subInd)) {
				entp = '911770';
			} else if (['P'].includes(subInd)) {
				entp = '911834';
			} else if (['S'].includes(subInd)) {
				entp = '911769';
			}
		} else if (landCntry == 'QA' && subInd) {
			subInd = subInd.substr(0, 1);
			if (['A', 'D', 'K', 'R', 'T', 'U', 'W'].includes(subInd)) {
				entp = '911818';
			} else if (['J', 'L', 'M', 'P', 'V'].includes(subInd)) {
				entp = '911819';
			} else if (['F', 'N', 'S'].includes(subInd)) {
				entp = '911820';
			} else if (['E', 'G', 'H', 'X', 'Y'].includes(subInd)) {
				entp = '911821';
			} else if (['B', 'C'].includes(subInd)) {
				entp = '911822';
			}
		} else if (landCntry == 'LY') {
			entp = '911756';
		} else if (landCntry == 'TN') {
			entp = '901728';
		} else if (landCntry == 'MA') {
			entp = '901462';
		} else if (['PK', 'AF'].includes(landCntry)) {
			entp = '901459';
		}
	} else if (isuCTC == '5K') {
		entp = '';
	}

	return entp;
}


function setEnterpriseAfterSave() {
	if (FormManager.getActualValue('enterprise') != _pagemodel.enterprise) {
		FormManager.setValue('enterprise', _pagemodel.enterprise)
	}
	if (FormManager.getActualValue('custSubGrp') == '') {
		localStorage.setItem("oldCustGrp", '');
	}
	if (['BUSPR', 'LLCBP', 'INTER', 'IBMEM', 'XBP', 'XIBME', 'XINTE'].includes(FormManager.getActualValue('custSubGrp'))) {
		FormManager.readOnly('enterprise');
	} else {
		FormManager.enable('enterprise');
	}

}

function disableChecklist() {
	  var checklist = dojo.query('table.checklist');
	  var radioBtns = checklist.query('input[type="radio"]');
	  var textFields = checklist.query('input[type="text"]');

	if (FormManager.getActualValue('viewOnlyPage') == 'true') {
		for (var i = 0; i < radioBtns.length; i++) {
			FormManager.readOnly('dijit_form_RadioButton_' + i);
		}

		for (var j = 0; j < textFields.length; j++) {
			FormManager.readOnly('dijit_form_TextBox_' + j)
		}
	} else {
		for (var i = 0; i < radioBtns.length; i++) {
			FormManager.enable('dijit_form_RadioButton_' + i);
		}

		for (var j = 0; j < textFields.length; j++) {
			FormManager.enable('dijit_form_TextBox_' + j)
		}
	}

}

dojo.addOnLoad(function() {
	GEOHandler.MCO2 = ['373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770',
		'782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883'];
		GEOHandler.MCO2_CHECKLIST = ['810','662','745','835','825','842'];	

	console.log('adding MCO2 functions...');
	GEOHandler.addAddrFunction(addMCO1LandedCountryHandler, GEOHandler.MCO2);
	GEOHandler.enableCopyAddress(GEOHandler.MCO2, validateMCOCopy, ['ZD01', 'ZI01']);
	GEOHandler.enableCustomerNamesOnAddress(GEOHandler.MCO2);
	GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.MCO2);
	GEOHandler.setRevertIsicBehavior(false);

	GEOHandler.addAfterConfig(afterConfigForMCO2, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(lockRequireFieldsMCO2, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(lockRequireFieldsMCO2, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(addHandlersForCEWA, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(crossborderScenariosAbbrvLoc, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(scenariosAbbrvLocOnChange, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(setIbmDeptCostCenterBehavior, GEOHandler.MCO2);

	GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(setTypeOfCustomerBehavior, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(lockAbbrv, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(lockCmrOwnerPrefLang, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(showDeptNoForInternalsOnly, GEOHandler.MCO2);
	// GEOHandler.addAfterTemplateLoad(setSalesRepValue, GEOHandler.MCO2);

	// GEOHandler.addAfterConfig(showDeptNoForInternalsOnly, GEOHandler.MCO2);

	GEOHandler.registerValidator(addAddressTypeValidator, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.MCO2, null, true);
	// Story 1718889: Tanzania: new mandatory TIN number field fix
	// GEOHandler.addAddrFunction(diplayTinNumberforTZ, [ SysLoc.TANZANIA ]);
	// GEOHandler.registerValidator(addTinFormatValidationTanzania, [
	// SysLoc.TANZANIA ], null, true);

	// GEOHandler.registerValidator(addTinBillingValidator, [ SysLoc.TANZANIA ],
	// null, true);

	// GEOHandler.registerValidator(addTinInfoValidator, GEOHandler.MCO2,
	// GEOHandler.REQUESTER,true);

	GEOHandler.addAddrFunction(addAddrValidatorMCO2, GEOHandler.MCO2);
	GEOHandler.addAddrFunction(disableAddrFieldsCEWA, GEOHandler.MCO2);
	GEOHandler.addAddrFunction(changeAbbrevNmLocn, GEOHandler.MCO2);


	/* 1438717 - add DPL match validation for failed dpl checks */
	GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.MCO2, GEOHandler.ROLE_PROCESSOR, true);
	GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.MCO2);

	GEOHandler.addAfterConfig(addHandlersForMCO2, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(addNewHandlersForMCO2, GEOHandler.MCO2);

	GEOHandler.registerValidator(addStreetAddressFormValidator, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addAdditionalNameStreetContPOBoxValidator, GEOHandler.MCO2, null, true);
	GEOHandler.addAfterConfig(clearPhoneNoFromGrid, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(clearPOBoxFromGrid, GEOHandler.MCO2);
	GEOHandler.registerValidator(addAddressGridValidatorStreetPOBox, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addAddressGridValidatorGMLLC, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addInternalDeptNumberValidator, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addTaxRegFormatValidationMadagascar, [SysLoc.MADAGASCAR], null, true);
	GEOHandler.registerValidator(addAttachmentValidatorOnTaxRegMadagascar, [SysLoc.MADAGASCAR], null, true);
	GEOHandler.registerValidator(addTinNumberValidationTz, [SysLoc.TANZANIA], null, true);
	GEOHandler.registerValidator(addTinNumberValidationKn, [SysLoc.KENYA], null, true);
	GEOHandler.addAfterTemplateLoad(retainImportValues, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(addPpsCeidValidator, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(setClientTierFieldMandt, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(lockUnlockISUCTC, GEOHandler.MCO2);

	GEOHandler.registerValidator(validateCMRForMCO2GMLLCScenario, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(gmllcExistingCustomerAdditionalValidations, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(validateCMRNoFORGMLLC, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(validateKenyaCBGmllc, GEOHandler.MCO2, null, true);

	GEOHandler.addAfterConfig(enableCMRNOMCO2GLLC, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(enableCMRNOMCO2GLLC, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(enableCmrNumForProcessor, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(registerMCO2VatValidator, GEOHandler.MCO2);

	GEOHandler.addAfterTemplateLoad(preTickVatExempt, GEOHandler.MCO2);
	GEOHandler.addAfterTemplateLoad(preTickNumeroExempt, SysLoc.MADAGASCAR);
	GEOHandler.addAfterTemplateLoad(preTickTinExempt, SysLoc.TANZANIA);

	GEOHandler.addAfterConfig(addAbbrvNmAndLocValidator, GEOHandler.MCO2);
	GEOHandler.addAfterConfig(setStreetContBehavior, GEOHandler.MCO2);
	GEOHandler.registerValidator(validateCollectionCd, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addEmbargoCodeValidator, GEOHandler.MCO2, null, true);

	GEOHandler.registerValidator(addSalesBusOffValidator, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(addSBOLengthValidator, GEOHandler.MCO2, null, true);

	GEOHandler.addAfterConfig(showVatExempt, GEOHandler.MCO2);

	GEOHandler.addAfterConfig(resetNumeroRequired, SysLoc.MADAGASCAR);
	GEOHandler.addAfterConfig(resetTinRequired, SysLoc.TANZANIA);

	// CREATCMR-4293
	GEOHandler.registerValidator(clientTierValidator, GEOHandler.MCO2, null, true);

	GEOHandler.addAfterConfig(enableTinNumber, SysLoc.KENYA);
	GEOHandler.addAfterTemplateLoad(enableTinNumber, SysLoc.KENYA);
	GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.MCO2, null, true);

	 GEOHandler.addAfterConfig(setChecklistStatus, GEOHandler.MCO2_CHECKLIST);
  GEOHandler.registerValidator(addCEMEAChecklistValidator, GEOHandler.MCO2_CHECKLIST);
  GEOHandler.addAfterConfig(addChecklistBtnHandler, GEOHandler.MCO2_CHECKLIST);
  GEOHandler.addAfterConfig(checkChecklistButtons, GEOHandler.MCO2_CHECKLIST);
  GEOHandler.addAfterConfig(disableChecklist, GEOHandler.MCO2_CHECKLIST);
	GEOHandler.registerValidator(StcOrderBlockValidation, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(validateISUCTCEnterprisefrLOCALAndNonMEA, GEOHandler.MCO2, null, true);
	GEOHandler.registerValidator(validateISUCTCEnterprisefrCROSS, GEOHandler.MCO2, null, true);
	GEOHandler.addAfterConfig(setEnterpriseAfterSave, GEOHandler.MCO2);

});
